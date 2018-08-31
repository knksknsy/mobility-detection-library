package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants;

/**
 * Class containing the default values for: intervals for ActivityRecognitionClient, loitering delays and radii for Geofences
 */
public class MobilityDetectionConstants {

    /**
     * Default update interval for ActivityRecognitionClient
     */
    public static final long INTERVAL = 1000 * 10;
    /**
     * Fastest update interval for ActivityRecognitionClient
     */
    public static final long FAST_INTERVAL = 1000;
    /**
     * Medium update interval for ActivityRecognitionClient
     */
    public static final long MEDIUM_INTERVAL = 1000 * 60 * 3;
    /**
     * Slowest update interval for ActivityRecognitionClient
     */
    public static final long SLOW_INTERVAL = 1000 * 60 * 6;
    /**
     * Loitering delay for a geofence when a wifi connection has been established.
     */
    public static final int LOITERING_DELAY_WIFI = 1000 * 60 * 5;
    /**
     * Loitering delay is used for deciding whether a wifi connection is a mobile hotspot or a stationary network.
     */
    public static final int LOITERING_DELAY_STATIONARY_WIFI = 1000 * 60 * 60 * 2;
    /**
     * Loitering delay for a geofence when the power connection has been established. The value is used for deciding whether an user is charging its mobile device with a power bank or from an socket.
     */
    public static final int LOITERING_DELAY_POWER = 1000 * 60 * 5;
    /**
     * Loitering delay for a geofence and when no new activities are recognized by the ActivityRecognitionClient. The value is used for changing the configuration of the ActivityRecognitionClient in order to prevent battery drainage.
     */
    public static final int LOITERING_DELAY_ACTIVITY = 1000 * 60 * 15;

    /**
     * Radius for a geofence when a power connection is established. The value is used for deciding whether an user is charging its mobile device with a power bank or from an socket.
     */
    public static final long RADIUS_POWER = 50L;
    /**
     * Radius for a geofence when a wifi connection is established. The value is used for deciding whether a wifi connection is a mobile hotspot or a stationary network.
     */
    public static final long RADIUS_WIFI = 50L;
    /**
     * Radius for a geofence when no new activities are recognized by the ActivityRecognitionClient. The value is used for changing the configuration of the ActivityRecognitionClient in order to prevent battery drainage.
     */
    public static final long RADIUS_ACTIVITY = 100L;
}
