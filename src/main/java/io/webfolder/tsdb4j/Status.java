package io.webfolder.tsdb4j;

public enum Status {

    AKU_SUCCESS(0, "Success"),
    AKU_ENO_DATA(1, "No data, can't proceed"),
    AKU_ENO_MEM (2, "Not enough memory"),
    AKU_EBUSY(3, "Device is busy"),
    AKU_ENOT_FOUND(4, "Can't find result"),
    AKU_EBAD_ARG(5, "Bad argument"),
    AKU_EOVERFLOW(6, "Overflow error"),
    AKU_EBAD_DATA(7, "The suplied data is invalid"),
    AKU_EGENERAL(8, "Error, no details available"),
    AKU_ELATE_WRITE(9, "Late write error"),
    AKU_ENOT_IMPLEMENTED(10, "Not implemented error"),
    AKU_EQUERY_PARSING_ERROR(11, "Invalid query"),
    AKU_EANOMALY_NEG_VAL(12, "Anomaly detector doesn't supports negative values (now)"),
    AKU_EMERGE_REQUIRED(13, "Stale data in sequencer, merge to disk required"),
    AKU_ECLOSED(14, "Operation on device can't be completed because device was closed"),
    AKU_ETIMEOUT(15, "Timeout detected"),
    AKU_ERETRY(16, "Retry required"),
    AKU_EACCESS(17, "Access denied"),
    AKU_ENOT_PERMITTED(18, "Operation not permitted"),
    AKU_EUNAVAILABLE(19, "Resource is not available"),
    AKU_EHIGH_CARDINALITY(20, "Error code for queries that doesn't support high cardinality"),
    AKU_EREGULLAR_EXPECTED(21, "Error code for queries that doesn't support irregular series"),
    AKU_EMISSING_DATA_NOT_SUPPORTED(22, "Function can't handle missing values"),
    AKU_EIO(23, "I/O error"),
    AKU_EMAX_ERROR(24, "All error codes should be less then AKU_EMAX_ERROR");
    
    public int code;
   
    public String message;
    
    private Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static Status fromCode(int code) {
        switch(code) {
            case 0 : return AKU_SUCCESS;
            case 1 : return AKU_ENO_DATA;
            case 2 : return AKU_ENO_MEM;
            case 3 : return AKU_EBUSY;
            case 4 : return AKU_ENOT_FOUND;
            case 5 : return AKU_EBAD_ARG;
            case 6 : return AKU_EOVERFLOW;
            case 7 : return AKU_EBAD_DATA;
            case 8 : return AKU_EGENERAL;
            case 9 : return AKU_ELATE_WRITE;
            case 10: return AKU_ENOT_IMPLEMENTED;
            case 11: return AKU_EQUERY_PARSING_ERROR;
            case 12: return AKU_EANOMALY_NEG_VAL;
            case 13: return AKU_EMERGE_REQUIRED;
            case 14: return AKU_ECLOSED;
            case 15: return AKU_ETIMEOUT;
            case 16: return AKU_ERETRY;
            case 17: return AKU_EACCESS;
            case 18: return AKU_ENOT_PERMITTED;
            case 19: return AKU_EUNAVAILABLE;
            case 20: return AKU_EHIGH_CARDINALITY;
            case 21: return AKU_EREGULLAR_EXPECTED;
            case 22: return AKU_EMISSING_DATA_NOT_SUPPORTED;
            case 23: return AKU_EIO;
            case 24: return AKU_EMAX_ERROR;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
