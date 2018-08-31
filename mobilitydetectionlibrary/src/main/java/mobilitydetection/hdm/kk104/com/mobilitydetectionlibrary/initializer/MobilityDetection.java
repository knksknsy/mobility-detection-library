package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.initializer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.MobilityDetectionConstants;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners.MobilityDetectionListener;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService;

/**
 * Singleton class for building mobility detection library
 */
public class MobilityDetection {

    private static final String TAG = MobilityDetection.class.getSimpleName();
    private static final MobilityDetection mobilityDetection = new MobilityDetection();

    private Context context;
    private MobilityDetectionListener listener;

    private long interval;
    private long fastInterval;
    private long mediumInterval;
    private long slowInterval;
    private int loiteringDelayWifi;
    private int loiteringDelayPower;
    private int loiteringDelayActivity;
    private long radiusPower;
    private long radiusWifi;
    private long radiusActivity;
    private int loiteringDelayStationaryWifi;

    public MobilityDetectionService mobilityDetectionService;
    private boolean serviceBound = false;

    private IntentFilter filter = new IntentFilter();

    private MobilityDetection() {
    }

    private static MobilityDetection getInstance() {
        return mobilityDetection;
    }

    private MobilityDetection setContext(Context context) {
        this.context = context;
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_ACTION);
        filter.addAction(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        filter.addAction(Actions.ACTIVITY_LIST_ACTION);
        filter.addAction(Actions.STOP_MOBILITY_DETECTION_ACTION);
        filter.addAction(Actions.POWER_CONNECTION_ACTION);
        filter.addAction(Actions.WIFI_CONNECTION_ACTION);
        filter.addAction(Actions.GEOFENCE_ADDED_ACTION);
        filter.addAction(Actions.GEOFENCE_REMOVED_ACTION);
        filter.addAction(Actions.GEOFENCES_REMOVED_ACTION);
        filter.addAction(Actions.ROUTE_ENDED_ACTION);
        filter.addAction(Actions.ROUTES_LOADED_ACTION);
        return this;
    }

    private MobilityDetection setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    private MobilityDetection setFastInterval(long fastInterval) {
        this.fastInterval = fastInterval;
        return this;
    }

    private MobilityDetection setMediumInterval(long mediumInterval) {
        this.mediumInterval = mediumInterval;
        return this;
    }

    private MobilityDetection setSlowInterval(long slowInterval) {
        this.slowInterval = slowInterval;
        return this;
    }

    private MobilityDetection setLoiteringDelayWifi(int loiteringDelayWifi) {
        this.loiteringDelayWifi = loiteringDelayWifi;
        return this;
    }

    private MobilityDetection setLoiteringDelayPower(int loiteringDelayPower) {
        this.loiteringDelayPower = loiteringDelayPower;
        return this;
    }

    private MobilityDetection setLoiteringDelayActivity(int loiteringDelayActivity) {
        this.loiteringDelayActivity = loiteringDelayActivity;
        return this;
    }

    private MobilityDetection setRadiusPower(long radiusPower) {
        this.radiusPower = radiusPower;
        return this;
    }

    private MobilityDetection setRadiusWifi(long radiusWifi) {
        this.radiusWifi = radiusWifi;
        return this;
    }

    private MobilityDetection setRadiusActivity(long radiusActivity) {
        this.radiusActivity = radiusActivity;
        return this;
    }

    private MobilityDetection setLoiteringDelayStationaryWifi(int loiteringDelayStationaryWifi) {
        this.loiteringDelayStationaryWifi = loiteringDelayStationaryWifi;
        return this;
    }

    private MobilityDetection setListener(MobilityDetectionListener listener) {
        this.listener = listener;
        return this;
    }

    private final BroadcastReceiver listenerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTIVITY_TRANSITIONED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONED_ACTION);
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                if (listener != null) {
                    listener.onTransitioned(detectedActivities);
                }
            }
            if (action.equals(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
                ArrayList<DetectedActivities> detectedActivities = intent.getParcelableArrayListExtra("activities");
                if (listener != null) {
                    listener.onTransitionsLoaded(detectedActivities);
                }
            }
            if (action.equals(Actions.ACTIVITY_LIST_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_LIST_ACTION);
                ArrayList<DetectedActivity> activities = intent.getParcelableArrayListExtra("activities");
                if (listener != null) {
                    listener.onActivityDetected(activities);
                }
            }
            if (action.equals(Actions.STOP_MOBILITY_DETECTION_ACTION)) {
                Log.e(TAG, Actions.STOP_MOBILITY_DETECTION_ACTION);
                stopMobilityDetection();
                if (listener != null) {
                    listener.onStopService();
                }
            }
            if (action.equals(Actions.POWER_CONNECTION_ACTION)) {
                boolean hasPowerConnection = intent.getBooleanExtra("isCharging", false);
                if (listener != null) {
                    listener.onPowerConnectionChanged(hasPowerConnection);
                }
            }
            if (action.equals(Actions.WIFI_CONNECTION_ACTION)) {
                boolean hasWifiConnection = intent.getBooleanExtra("isWifi", false);
                if (listener != null) {
                    listener.onWifiConnectionChanged(hasWifiConnection);
                }
            }
            if (action.equals(Actions.GEOFENCE_ADDED_ACTION)) {
                Log.e(TAG, Actions.GEOFENCE_ADDED_ACTION);
                String key = intent.getStringExtra("geofenceKey");
                if (listener != null) {
                    listener.onGeofenceAdded(key);
                }
            }
            if (action.equals(Actions.GEOFENCE_REMOVED_ACTION)) {
                Log.e(TAG, Actions.GEOFENCE_REMOVED_ACTION);
                ArrayList<String> keys = intent.getStringArrayListExtra("geofenceKey");
                if (listener != null) {
                    listener.onGeofenceRemoved(keys);
                }
            }
            if (action.equals(Actions.GEOFENCES_REMOVED_ACTION)) {
                Log.e(TAG, Actions.GEOFENCES_REMOVED_ACTION);
                if (listener != null) {
                    listener.onGeofencesRemoved();
                }
            }
            if (action.equals(Actions.ROUTE_ENDED_ACTION)) {
                Log.e(TAG, Actions.ROUTE_ENDED_ACTION);
                ArrayList<Route> routes = intent.getParcelableArrayListExtra(Route.class.getSimpleName());
                if (listener != null) {
                    listener.onRouteEnded(routes);
                }
            }
            if (action.equals(Actions.ROUTES_LOADED_ACTION)) {
                Log.e(TAG, Actions.ROUTES_LOADED_ACTION);
                ArrayList<Route> routes = intent.getParcelableArrayListExtra(Route.class.getSimpleName());
                if (listener != null) {
                    listener.onRoutesLoaded(routes);
                }
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MobilityDetectionService.LocalBinder binder = (MobilityDetectionService.LocalBinder) service;
            mobilityDetectionService = binder.getServiceInstance();
            mobilityDetectionService.interval = interval;
            mobilityDetectionService.fastInterval = fastInterval;
            mobilityDetectionService.mediumInterval = mediumInterval;
            mobilityDetectionService.slowInterval = slowInterval;
            mobilityDetectionService.loiteringDelayWifi = loiteringDelayWifi;
            mobilityDetectionService.loiteringDelayPower = loiteringDelayPower;
            mobilityDetectionService.loiteringDelayActivity = loiteringDelayActivity;
            mobilityDetectionService.radiusPower = radiusPower;
            mobilityDetectionService.radiusWifi = radiusWifi;
            mobilityDetectionService.radiusActivity = radiusActivity;
            mobilityDetectionService.loiteringDelayStationaryWifi = loiteringDelayStationaryWifi;
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    /**
     * Starts the mobility detection library
     *
     * @throws NullPointerException
     */
    public void startMobilityDetection() throws NullPointerException {
        if (context != null) {
            Intent intent = new Intent(context, MobilityDetectionService.class);
            ContextCompat.startForegroundService(context, intent);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            context.registerReceiver(listenerReceiver, filter);
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

    /**
     * Stops the mobility detection library
     *
     * @throws NullPointerException
     */
    public void stopMobilityDetection() throws NullPointerException {
        if (context != null) {
            mobilityDetectionService.saveData();
            Intent intent = new Intent(context, MobilityDetectionService.class);
            if (serviceBound) {
                context.unbindService(serviceConnection);
                context.unregisterReceiver(listenerReceiver);
            }
            context.stopService(intent);
            serviceBound = false;
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

    /**
     * Class for building the MobilityDetection class
     */
    public static class Builder {

        private Context context;

        private long interval = MobilityDetectionConstants.INTERVAL;
        private long fastInterval = MobilityDetectionConstants.FAST_INTERVAL;
        private long mediumInterval = MobilityDetectionConstants.MEDIUM_INTERVAL;
        private long slowInterval = MobilityDetectionConstants.SLOW_INTERVAL;

        private int loiteringDelayWifi = MobilityDetectionConstants.LOITERING_DELAY_WIFI;
        private int loiteringDelayPower = MobilityDetectionConstants.LOITERING_DELAY_POWER;
        private int loiteringDelayActivity = MobilityDetectionConstants.LOITERING_DELAY_ACTIVITY;

        private long radiusPower = MobilityDetectionConstants.RADIUS_POWER;
        private long radiusWifi = MobilityDetectionConstants.RADIUS_WIFI;
        private long radiusActivity = MobilityDetectionConstants.RADIUS_ACTIVITY;

        private int loiteringDelayStationaryWifi = MobilityDetectionConstants.LOITERING_DELAY_STATIONARY_WIFI;

        private MobilityDetectionListener listener;

        public Builder() {

        }

        /**
         * Setting the context of the application where the library is included
         *
         * @param context Context
         * @return Builder
         */
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Setting the update interval for ActivityRecognitionClient
         *
         * @param interval Update interval in milliseconds
         * @return Builder
         */
        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        /**
         * Setting the fastest update interval for ActivityRecognitionClient
         *
         * @param fastInterval Update interval in milliseconds
         * @return Builder
         */
        public Builder setFastInterval(long fastInterval) {
            this.fastInterval = fastInterval;
            return this;
        }

        /**
         * Setting the medium update interval for ActivityRecognitionClient
         *
         * @param mediumInterval Update interval in milliseconds
         * @return Builder
         */
        public Builder setMediumInterval(long mediumInterval) {
            this.mediumInterval = mediumInterval;
            return this;
        }

        /**
         * Setting the slowest update interval for ActivityRecognitionClient
         *
         * @param slowInterval Update interval in milliseconds
         * @return Builder
         */
        public Builder setSlowInterval(long slowInterval) {
            this.slowInterval = slowInterval;
            return this;
        }

        /**
         * Setting the loitering delay for a geofence when a wifi connection has been established. The value is used for deciding whether a wifi connection is a mobile hotspot or a stationary network.
         *
         * @param loiteringDelayWifi loitering delay in milliseconds
         * @return Builder
         */
        public Builder setLoiteringDelayWifi(int loiteringDelayWifi) {
            this.loiteringDelayWifi = loiteringDelayWifi;
            return this;
        }

        /**
         * Setting the loitering delay for a geofence when the power connection has been established. The value is used for deciding whether an user is charging its mobile device with a power bank or from an socket.
         *
         * @param loiteringDelayPower loitering delay in milliseconds
         * @return Builder
         */
        public Builder setLoiteringDelayPower(int loiteringDelayPower) {
            this.loiteringDelayPower = loiteringDelayPower;
            return this;
        }

        /**
         * Setting the loitering delay for a geofence and when no new activities are recognized by the ActivityRecognitionClient. The value is used for changing the configuration of the ActivityRecognitionClient in order to prevent battery drainage.
         *
         * @param loiteringDelayActivity loitering delay in milliseconds
         * @return Builder
         */
        public Builder setLoiteringDelayActivity(int loiteringDelayActivity) {
            this.loiteringDelayActivity = loiteringDelayActivity;
            return this;
        }

        public Builder setLoiteringDelayStationaryWifi(int loiteringDelayStationaryWifi) {
            this.loiteringDelayStationaryWifi = loiteringDelayStationaryWifi;
            return this;
        }

        /**
         * Setting the radius for a geofence when a power connection is established. The value is used for deciding whether an user is charging its mobile device with a power bank or from an socket.
         *
         * @param radiusPower radius in meters
         * @return Builder
         */
        public Builder setRadiusPower(long radiusPower) {
            this.radiusPower = radiusPower;
            return this;
        }

        /**
         * Setting the radius for a geofence when a wifi connection is established. The value is used for deciding whether a wifi connection is a mobile hotspot or a stationary network.
         *
         * @param radiusWifi radius in meters
         * @return Builder
         */
        public Builder setRadiusWifi(long radiusWifi) {
            this.radiusWifi = radiusWifi;
            return this;
        }

        /**
         * Setting the radius for a geofence when no new activities are recognized by the ActivityRecognitionClient. The value is used for changing the configuration of the ActivityRecognitionClient in order to prevent battery drainage.
         *
         * @param radiusActivity radius in meters
         * @return Builder
         */
        public Builder setRadiusActivity(long radiusActivity) {
            this.radiusActivity = radiusActivity;
            return this;
        }

        /**
         * Setting the listener for retrieving events in the application
         *
         * @param listener MobilityDetectionListener
         * @return Builder
         */
        public Builder setListener(MobilityDetectionListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Builds a singleton object of MobilityDetection class
         *
         * @return MobilityDetection
         */
        public MobilityDetection build() {
            return MobilityDetection.getInstance()
                    .setContext(context)
                    .setInterval(interval)
                    .setFastInterval(fastInterval)
                    .setMediumInterval(mediumInterval)
                    .setSlowInterval(slowInterval)
                    .setLoiteringDelayWifi(loiteringDelayWifi)
                    .setLoiteringDelayPower(loiteringDelayPower)
                    .setLoiteringDelayActivity(loiteringDelayActivity)
                    .setRadiusPower(radiusPower)
                    .setRadiusWifi(radiusWifi)
                    .setRadiusActivity(radiusActivity)
                    .setLoiteringDelayStationaryWifi(loiteringDelayStationaryWifi)
                    .setListener(listener);
        }
    }

}
