package io.arex.inst.runtime.model;

public class ArexConstants {
    private ArexConstants() {}

    public static final String RECORD_ID = "arex-record-id";
    public static final String REPLAY_ID = "arex-replay-id";
    public static final String REPLAY_WARM_UP = "arex-replay-warm-up";
    public static final String FORCE_RECORD = "arex-force-record";
    public static final String REDIRECT_REQUEST_METHOD = "arex-redirect-request-method";
    public static final String REDIRECT_REFERER = "arex-redirect-referer";
    public static final String REDIRECT_PATTERN = "arex-redirect-pattern";
    /**
     * mock template
     */
    public static final String HEADER_EXCLUDE_MOCK = "X-AREX-Exclusion-Operations";
    /**
     * dubbo stream protocol:triple
     */
    public static final String DUBBO_STREAM_PROTOCOL = ":tri";
    /**
     * dubbo stream protocol:streaming
     */
    public static final String DUBBO_STREAM_NAME = "streaming";

    public static final String UUID_SIGNATURE = "java.util.UUID.randomUUID";
    public static final String CURRENT_TIME_MILLIS_SIGNATURE = "java.lang.System.currentTimeMillis";
    public static final String NEXT_INT_SIGNATURE = "java.util.Random.nextInt";
    public static final String SERIALIZE_SKIP_INFO_CONFIG_KEY = "serializeSkipInfoList";
}
