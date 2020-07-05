package io.webfolder.tsdb4j;

import static io.webfolder.tsdb4j.Status.AKU_SUCCESS;
import static io.webfolder.tsdb4j.Status.fromCode;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.load;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Locale.ENGLISH;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Database {

    // temporary directory location
    private static final Path tmpdir = get(System.getProperty("java.io.tmpdir")).toAbsolutePath();

    private static final String version = "0.1.0";

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(ENGLISH);

    private static final boolean WINDOWS = OS_NAME.startsWith("windows");

    private static final boolean MAC = OS_NAME.contains("mac");

    private static boolean loaded;

    public static synchronized boolean loadJni() {
        if (loaded) {
            return true;
        }
        String name = WINDOWS ? "tsdb4j.dll" : MAC ? "libtsdb4j.dylib" : "libtsdb4j.so";
        Path libFile = tmpdir.resolve("tsdb4j-" + version).resolve(name);
        if ( ! exists(libFile) ) {
            ClassLoader cl = Database.class.getClassLoader();
            try (InputStream is = cl.getResourceAsStream("META-INF/" + name)) {
                if ( ! exists(libFile.getParent()) ) {
                    createDirectory(libFile.getParent());
                }
                if ( ! exists(libFile) ) {
                    createFile(libFile);
                }
                copy(is, libFile, REPLACE_EXISTING);
            } catch (IOException e) {
                throw new TsdbException(e.getMessage());
            }
        }
        load(libFile.toString());
        loaded = true;
        return true;
    }

    static {
        loadJni();
    }

    public static final long VOLUME_MIN_SIZE = 1024L * 1024L;             //   1 MB

    public static final long VOLUME_MAX_SIZE = 1024L * 1024L * 1024L * 4; //   4 GB

    public static final long WAL_DEFAULT_SIZE = 1024L * 1024L;            // 256 MB

    public static final long WAL_MIN_SIZE = 1024L * 1024L;                //   1 MB

    public static final long WAL_MAX_SIZE = 1024L * 1024L * 1024L;        //   1 GB

    private native short _create(String path, String name, int volumes, long pageSize, boolean allocate);

    private native long _open(String path,
                              int walConcurrency,
                              long walVolumeSize,
                              long walNumberOfVolumes,
                              String walPath);

    private native void _close(long db);

    private native int _delete(String path);

    public native static String getProperty(String key);

    private long db;

    private final Path dbPath;

    private final Path dbFile;

    private final String name;

    public Database(Path path, String name) {
        this.dbPath = path.toAbsolutePath();
        this.dbFile = dbPath.resolve(name + ".akumuli");
        this.name = name;
    }

    public void create(int volumes, long volumeSize, boolean allocate) {
        if (volumeSize < VOLUME_MIN_SIZE) {
            throw new IllegalArgumentException("Volume size is too small: [" + volumeSize + "], it can't be less than 1MB");
        }
        if (volumeSize > VOLUME_MAX_SIZE) {
            throw new IllegalArgumentException("invalid [volumes] parameter. value should not exceed 4 GB.");
        }
        if (volumes < 1) {
            throw new IllegalArgumentException("[volumes] must be greater or equal than: [1]");
        }
        int code = _create(dbPath.toString(), name, volumes, volumeSize, allocate);
        Status status = fromCode(code);
        if (AKU_SUCCESS != status) {
            throw new DatabaseException(status);
        }
    }

    public boolean open() {
        return open(getRuntime().availableProcessors(),
                    WAL_MIN_SIZE,
                    getRuntime().availableProcessors());
    }

    public boolean open(int walConcurrency,
                        long walVolumeSize,
                        long walNumberOfVolumes) {
        if (!exists(dbFile)) {
            throw new DatabaseNotException(dbFile.toString());
        }
        try {
            if (size(dbFile) < 0) {
                throw new DatabaseNotException(dbFile.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (walConcurrency < 0) {
            throw new IllegalArgumentException("[walConcurrency] must be greater than: [1]");
        }
        if (walNumberOfVolumes < 0 || walNumberOfVolumes == 1 || walNumberOfVolumes > 1000) {
            throw new IllegalArgumentException("invalid [walNumberOfVolumes] parameter. value should not exceed 1000 or be equal to 1.");
        }
        if (walVolumeSize > 0 && walVolumeSize < WAL_MIN_SIZE) {
            throw new IllegalArgumentException("[walVolumeSize] must be greater than: [" + WAL_MIN_SIZE + " GB]");
        }
        if (walVolumeSize > 0 && walVolumeSize > WAL_MAX_SIZE) {
            throw new IllegalArgumentException("[walVolumeSize] must be less than: [" + WAL_MAX_SIZE + " GB]");
        }
        boolean enableWal = walConcurrency > 0 &&
                                walVolumeSize > 0 &&
                                walNumberOfVolumes > 0 ? true : false;
        db = _open(dbFile.toString(),
                    walConcurrency,
                    walVolumeSize,
                    walNumberOfVolumes,
                    enableWal ? dbPath.toAbsolutePath().toString() : null);
        return db > 0 ? true : false;
    }

    public Session createSession() {
        return new Session(db);
    }

    public void delete() {
        if (db > 0) {
            throw new IllegalStateException("close the database before deleting it");
        }
        if (dbPath != null) {
            int code = _delete(dbFile.toString());
            Status status = fromCode(code);
            if (AKU_SUCCESS != status) {
                throw new DatabaseException(status);
            }
        }
    }

    public void close() {
        _close(db);
        db = 0;
    }

    public Path getPath() {
        return dbPath;
    }

    @Override
    public String toString() {
        return "Database [path=" + dbFile + ", name=" + name + "]";
    }
}
