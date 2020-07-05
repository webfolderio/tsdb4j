#include <jni.h>

#include <string>
#include <cmath>
#include <vector>
#include <thread>
#include <atomic>

#include "jnipp/jnipp.h"

#include "akumuli.h"

/**
 * Row iterator interface
 */
struct Cursor {

    struct CursorRow {
        char* series;
        double* values;
        size_t len_values;
        aku_Timestamp timestamp{};

        ~CursorRow() {
            if (values) {
                delete []values;
            }
            if (series) {
                delete []series;
            }
        }
    };

    virtual ~Cursor() = default;

    virtual bool can_proceed() = 0;

    //! Check completion
    virtual bool done() = 0;

    //! Get next row
    virtual bool get_next_row(CursorRow *row) = 0;
};

struct LocalCursor : Cursor {
    aku_Session *session_;
    aku_Cursor *cursor_;
    std::vector<char>   rdbuf_;      //! Read buffer
    size_t              rdbuf_pos_;  //! Read position in buffer
    size_t              rdbuf_top_;  //! Last initialized item _index_ in `rdbuf_`

    LocalCursor(aku_Session *s, aku_Cursor *cursor)
            : session_(s), cursor_(cursor),
            rdbuf_top_(0), rdbuf_pos_(0) {
        rdbuf_.resize(1024);
    }

    bool can_proceed() override {
        aku_Status status = AKU_SUCCESS;
        return aku_cursor_is_error(cursor_, &status) == 0;
    }

    ~LocalCursor() override {
        if (cursor_) {
            aku_cursor_close(cursor_);
        }
    }

    virtual bool done() override {
        if (rdbuf_pos_ < rdbuf_top_) {
            return false;
        }
        if (cursor_) {
            return aku_cursor_is_done(cursor_);
        }
        return true;
    }

    bool get_next_row(CursorRow *result) override {
        if (rdbuf_top_ == rdbuf_pos_) {
            rdbuf_top_ = aku_cursor_read(cursor_, rdbuf_.data(), rdbuf_.size());
            rdbuf_pos_ = 0u;
        }
        aku_Status status;
        if (aku_cursor_is_error(cursor_, &status) != 0) {
            return false;
        }
        if (rdbuf_pos_ < rdbuf_top_) {
            const aku_Sample* sample = reinterpret_cast<const aku_Sample*>(rdbuf_.data() + rdbuf_pos_);
            if (sample->payload.type & aku_PData::PARAMID_BIT) {
                result->series = (char*) malloc(sizeof(char) * AKU_LIMITS_MAX_SNAME);
                auto len = aku_param_id_to_series(session_, sample->paramid, result->series, AKU_LIMITS_MAX_SNAME);
                if (len <= 0) {
                    // Error, no such id
                    return false;
                }
                result->series[len] = '\0';
            }
            if (sample->payload.type & aku_PData::PARAMID_BIT) {
                result->timestamp = sample->timestamp;
            }
            if (sample->payload.type & aku_PData::FLOAT_BIT) {
                double* values = new double[1];
                values[0] = sample->payload.float64;
                result->values = values;
                result->len_values = 1;
            } else if (sample->payload.type & aku_PData::TUPLE_BIT) {
                union {
                    u64 u;
                    double d;
                } bits;
                bits.d = sample->payload.float64;
                int nelements = bits.u >> 58;  // top 6 bits contains number of elements
                double const* tuple = reinterpret_cast<double const*>(sample->payload.data);
                int tup_ix = 0;
                double* values = nelements > 0 ? new double[nelements] : nullptr;
                for (int ix = 0; ix < nelements; ix++) {
                    if (bits.u & (1 << ix)) {
                        values[ix] = tuple[tup_ix];
                    } else {
                        // empty tuple
                        values[ix] = NAN;
                    }
                    tup_ix++;
                }
                result->values = values;
                result->len_values = nelements;
            } else {
                result->values = nullptr;
                result->len_values = 0;
            }
            rdbuf_pos_ += sample->payload.size;
        }
        return true;
    }
};

#ifdef __cplusplus
extern "C" {
#endif

static jni::Object *GLOBAL_LOGGER = nullptr;

static jfieldID CURSOR_FILED_VALUE;
static jfieldID CURSOR_FILE_TIMESTAMP;

void console_logger(aku_LogLevel tag, const char *msg) {
    if (!GLOBAL_LOGGER) {
        return;
    }
    switch (tag) {
        case AKU_LOG_ERROR:
            GLOBAL_LOGGER->call<void>("error", msg);
            break;
        case AKU_LOG_INFO:
            GLOBAL_LOGGER->call<void>("info", msg);
            break;
        case AKU_LOG_TRACE:
            GLOBAL_LOGGER->call<void>("trace", msg);
            break;
    }
}

// ----------------------------------------------------------------------------
// JNI Load & UnLoad
// ----------------------------------------------------------------------------

static std::atomic<bool> panic_flag(false);
static std::string panic_message;

void tsdb4j_panic_handler(const char* message) {
    if (!panic_flag) {
        panic_flag = true;
        panic_message = std::string(message);
    }
}

inline bool tsdb4j_check_panic() {
    return panic_flag;
}

static void tsdb4j_throw_panic(JNIEnv* env) {
    if (panic_flag) {
        auto exception_class = jni::Class("io/webfolder/tsdb4j/PanicException");
        auto exception_object = exception_class.newInstance(panic_message.c_str());
        env->Throw((jthrowable) exception_object.getHandle());
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_8) != JNI_OK) {
        fprintf(stderr, "Failed to get the environment");
        return -1;
    }
    jni::init(env);
    jni::Class logger_class = jni::Class("io/webfolder/tsdb4j/Logger");
    jni::Object logger_object = logger_class.newInstance();

    jni::Class cursor_class("io/webfolder/tsdb4j/Cursor");

    CURSOR_FILED_VALUE = cursor_class.getField("values", "[D");
    CURSOR_FILE_TIMESTAMP = cursor_class.getField("timestamp", "J");

    GLOBAL_LOGGER = new jni::Object(logger_object);
    aku_initialize(&tsdb4j_panic_handler, &console_logger);
    return JNI_VERSION_1_8;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (GLOBAL_LOGGER) {
        delete GLOBAL_LOGGER;
    }
}

// ----------------------------------------------------------------------------
// Database
// ----------------------------------------------------------------------------

JNIEXPORT jshort JNICALL Java_io_webfolder_tsdb4j_Database__1create(
        JNIEnv *env,
        jobject that,
        jstring path,
        jstring name,
        jint volumes,
        jlong page_size,
        jboolean allocate) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    const char *c_path = env->GetStringUTFChars(path, JNI_FALSE);
    const char *c_name = env->GetStringUTFChars(name, JNI_FALSE);
    apr_status_t result = aku_create_database_ex(c_name,
                                                 c_path,
                                                 c_path,
                                                 (i32) volumes,
                                                 (u64) page_size,
                                                 (bool) allocate);
    env->ReleaseStringUTFChars(path, c_path);
    env->ReleaseStringUTFChars(path, c_name);
    return (jint) result;
}

JNIEXPORT jlong JNICALL Java_io_webfolder_tsdb4j_Database__1open(JNIEnv *env,
                                                                 jobject that,
                                                                 jstring path,
                                                                 jint walConcurrency,
                                                                 jlong walVolumeSize,
                                                                 jlong walNumberOfVolumes,
                                                                 jstring walPath) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    auto *c_wal_path = walPath != nullptr ? env->GetStringUTFChars(walPath, JNI_FALSE) : nullptr;
    aku_FineTuneParams params = {};
    params.logger = &aku_console_logger;
    if (c_wal_path != nullptr) {
        params.input_log_concurrency = (u32) walConcurrency;
        params.input_log_volume_size = (u64) walVolumeSize;
        params.input_log_volume_numb = (u64) walNumberOfVolumes;
        params.input_log_path = c_wal_path;
    }
    auto *c_path = env->GetStringUTFChars(path, JNI_FALSE);
    aku_Database *db = aku_open_database(c_path, params);
    env->ReleaseStringUTFChars(path, c_path);
    if (walPath) {
        env->ReleaseStringUTFChars(walPath, c_wal_path);
    }
    if (db) {
        return (jlong) db;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_io_webfolder_tsdb4j_Database__1delete(
        JNIEnv *env,
        jobject that,
        jstring path) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    const char *c_path = env->GetStringUTFChars(path, JNI_FALSE);
    if (!c_path) {
        return AKU_EGENERAL;
    }
    apr_status_t result = aku_remove_database(c_path, c_path, true);
    env->ReleaseStringUTFChars(path, c_path);
    return (int) result;
}

JNIEXPORT void JNICALL Java_io_webfolder_tsdb4j_Database__1close(JNIEnv *env, jobject that, jlong db) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return;
    }
    auto *c_db = (aku_Database *) db;
    if (c_db) {
        aku_close_database(c_db);
    }
}

// ----------------------------------------------------------------------------
// Session
// ----------------------------------------------------------------------------

JNIEXPORT jlong JNICALL Java_io_webfolder_tsdb4j_Session__1open(JNIEnv *env, jobject that, jlong db) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    auto *c_db = (aku_Database *) db;
    if (c_db) {
        auto *session = aku_create_session(c_db);
        return (jlong) session;
    }
    return 0;
}

JNIEXPORT void JNICALL Java_io_webfolder_tsdb4j_Session__1close(JNIEnv *env, jobject that, jlong session) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return;
    }
    auto *c_session = (aku_Session *) session;
    if (c_session) {
        aku_destroy_session(c_session);
    }
}

JNIEXPORT void JNICALL Java_io_webfolder_tsdb4j_Session__1add(
        JNIEnv *env,
        jobject that,
        jlong session,
        jlong timestamp,
        jstring series,
        jdouble value) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return;
    }
    auto *c_session = (aku_Session *) session;
    if (!c_session) {
        return;
    }
    auto status = AKU_EBUSY;
    auto c_series = env->GetStringUTFChars(series, JNI_FALSE);
    auto c_series_len = env->GetStringLength(series);
    jni::Object exception_object = nullptr;
    while (status == AKU_EBUSY) {
        aku_Sample sample;
        aku_Status status_param_id = aku_series_to_param_id(c_session, c_series, &c_series[c_series_len], &sample);
        if (status_param_id != AKU_SUCCESS) {
            jni::Class exception_class = jni::Class("io/webfolder/tsdb4j/InvalidSeriesException");
            exception_object = exception_class.newInstance((int) status_param_id, c_series);
            goto done;
        }
        sample.timestamp = (aku_Timestamp) timestamp;
        sample.payload.type = AKU_PAYLOAD_FLOAT;
        sample.payload.float64 = value;
        // TODO: add exception handling
        status = aku_write(c_session, &sample);
    }
    done:
    env->ReleaseStringUTFChars(series, c_series);
    if (exception_object != nullptr) {
        env->Throw((jthrowable) exception_object.getHandle());
    }
}

JNIEXPORT jlong JNICALL Java_io_webfolder_tsdb4j_Session__1metadata(
        JNIEnv *env,
        jobject that,
        jlong session,
        jstring query) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    auto *c_session = (aku_Session *) session;
    if (!c_session) {
        return 0;
    }
    if (query == nullptr) {
        return 0;
    }
    auto c_query = env->GetStringUTFChars(query, JNI_FALSE);
    if (c_query == nullptr) {
        return 0;
    }
    auto c_cursor = aku_query(c_session, c_query);
    env->ReleaseStringUTFChars(query, c_query);
    auto cursor = new LocalCursor(c_session, c_cursor);
    return (jlong) cursor;
}


JNIEXPORT jlong JNICALL Java_io_webfolder_tsdb4j_Session__1query(
        JNIEnv *env,
        jobject that,
        jlong session,
        jstring query) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return 0;
    }
    auto c_session = (aku_Session *) session;
    if (!c_session) {
        return 0;
    }
    if (query == nullptr) {
        return 0;
    }
    auto c_query = env->GetStringUTFChars(query, JNI_FALSE);
    if (c_query == nullptr) {
        return 0;
    }
    auto c_cursor = aku_query(c_session, c_query);
    env->ReleaseStringUTFChars(query, c_query);
    auto cursor = new LocalCursor(c_session, c_cursor);
    return (jlong) cursor;
}

// ----------------------------------------------------------------------------
// MetaDataCursor
// ----------------------------------------------------------------------------

JNIEXPORT jstring JNICALL Java_io_webfolder_tsdb4j_MetaDataCursor__1next(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return nullptr;
    }
    auto c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return nullptr;
    }
    Cursor::CursorRow row;
    if (!c_cursor->get_next_row(&row)) {
        return nullptr;
    }
    jstring seriesname = env->NewStringUTF(row.series);
    return seriesname;
}

JNIEXPORT jboolean JNICALL Java_io_webfolder_tsdb4j_MetaDataCursor__1done(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return JNI_FALSE;
    }
    auto c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return JNI_FALSE;
    }
    if (!c_cursor->can_proceed()) {
        return JNI_FALSE;
    } else {
        return c_cursor->done() ? JNI_TRUE : JNI_FALSE;
    }
}

JNIEXPORT void JNICALL Java_io_webfolder_tsdb4j_MetaDataCursor__1close(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return;
    }
    auto c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return;
    }
    delete c_cursor;
}

// ----------------------------------------------------------------------------
// Cursor
// ----------------------------------------------------------------------------

JNIEXPORT jstring JNICALL Java_io_webfolder_tsdb4j_Cursor__1next(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return nullptr;
    }
    auto *c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return nullptr;
    }
    Cursor::CursorRow row;
    if (!c_cursor->get_next_row(&row)) {
        return nullptr;
    }
    const char *series = row.series;
    if (series == nullptr) {
        return nullptr;
    }
    jstring series_name = env->NewStringUTF(series);
    auto cursor_object = jni::Object(that);
    if (row.values != nullptr) {
        jdoubleArray values = env->NewDoubleArray((jsize) row.len_values);
        env->SetDoubleArrayRegion(values, 0, (jsize) row.len_values, row.values);
        env->SetObjectField(that, CURSOR_FILED_VALUE, values);
    }
    if (row.timestamp > 0) {
        env->SetLongField(that, CURSOR_FILE_TIMESTAMP, (jlong) row.timestamp);
    } else {
        env->SetLongField(that, CURSOR_FILE_TIMESTAMP, (jlong) NAN);
    }
    return series_name;
}

JNIEXPORT jboolean JNICALL Java_io_webfolder_tsdb4j_Cursor__1done(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return JNI_FALSE;
    }
    auto c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return JNI_FALSE;
    }
    auto done = (jboolean) c_cursor->done();
    return done;
}

JNIEXPORT void JNICALL Java_io_webfolder_tsdb4j_Cursor__1close(
        JNIEnv *env,
        jobject that,
        jlong cursor) {
    if (tsdb4j_check_panic()) {
        tsdb4j_throw_panic(env);
        return;
    }
    auto *c_cursor = (Cursor *) cursor;
    if (!c_cursor) {
        return;
    }
    delete c_cursor;
}

#ifdef __cplusplus
}
#endif
