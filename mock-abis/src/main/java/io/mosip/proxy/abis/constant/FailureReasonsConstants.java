package io.mosip.proxy.abis.constant;

/**
 * Constants for failure reasons used throughout the application.
 * <p>
 * This class provides a set of predefined constants that represent various
 * failure reasons. These constants can be used to standardize error handling
 * and messaging across different parts of the application.
 * </p>
 * <p>
 * The {@code FailureReasonsConstants} class is a utility class and is not meant to be
 * instantiated. It provides a private constructor to prevent instantiation.
 * </p>
 * 
 * @since 1.0.0
 */
public class FailureReasonsConstants {
    
    /**
     * Private constructor to prevent instantiation.
     * Throws {@code IllegalStateException} if attempted to instantiate.
     */
    private FailureReasonsConstants() {
        throw new IllegalStateException("FailureReasonsConstants class");
    }

    /** Unknown internal error. */
    public static final String INTERNAL_ERROR_UNKNOWN = "1";

    /** Operation aborted. */
    public static final String ABORTED = "2";

    /** Unexpected error occurred. */
    public static final String UNEXPECTED_ERROR = "3";

    /** Invalid request structure. */
    public static final String UNABLE_TO_SERVE_THE_REQUEST_INVALID_REQUEST_STRUCTURE = "4";

    /** Missing reference ID. */
    public static final String MISSING_REFERENCEID = "5";

    /** Missing request ID. */
    public static final String MISSING_REQUESTID = "6";

    /** Unable to fetch biometric details. */
    public static final String UNABLE_TO_FETCH_BIOMETRIC_DETAILS = "7";

    /** Missing reference URL. */
    public static final String MISSING_REFERENCE_URL = "8";

    /** Missing request time. */
    public static final String MISSING_REQUESTTIME = "9";

    /** Reference ID already exists. */
    public static final String REFERENCEID_ALREADY_EXISTS = "10";

    /** CBEFF has no data. */
    public static final String CBEFF_HAS_NO_DATA = "11";

    /** Reference ID not found. */
    public static final String REFERENCEID_NOT_FOUND = "12";

    /** Invalid version. */
    public static final String INVALID_VERSION = "13";

    /** Invalid ID. */
    public static final String INVALID_ID = "14";

    /** Invalid request time format. */
    public static final String INVALID_REQUESTTIME_FORMAT = "15";

    /** Invalid CBEFF format. */
    public static final String INVALID_CBEFF_FORMAT = "16";

    /** Data share URL expired. */
    public static final String DATA_SHARE_URL_EXPIRED = "17";

    /** Biometric quality check failed. */
    public static final String BIOMETRIC_QUALITY_CHECK_FAILED = "18";
}