package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary;

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
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners.MobilityDetectionListener;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService;

public class MobilityDetection {

    private static final String TAG = MobilityDetection.class.getSimpleName();
    private static final MobilityDetection mobilityDetection = new MobilityDetection();

    private Context context;
    private MobilityDetectionListener listener;

    private long interval;
    private long fastInterval;
    private long mediumInterval;
    private long slowInterval;
    private int loiteringDelayWifiConnectionChanged;
    private int loiteringDelayWifiConnectionTime;
    private int loiteringDelayPowerConnectionChanged;
    private int loiteringDelayActivity;

    public MobilityDetectionService mobilityDetectionService;
    private boolean serviceBound = false;

    private IntentFilter filter = new IntentFilter();

    private MobilityDetection() {
    }

    private static MobilityDetection getInstance() {
        return mobilityDetection;
    }

    public MobilityDetection setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public MobilityDetection setFastInterval(long fastInterval) {
        this.fastInterval = fastInterval;
        return this;
    }

    public MobilityDetection setMediumInterval(long mediumInterval) {
        this.mediumInterval = mediumInterval;
        return this;
    }

    public MobilityDetection setSlowInterval(long slowInterval) {
        this.slowInterval = slowInterval;
        return this;
    }

    public MobilityDetection setLoiteringDelayWifiConnectionChanged(int loiteringDelayWifiConnectionChanged) {
        this.loiteringDelayWifiConnectionChanged = loiteringDelayWifiConnectionChanged;
        return this;
    }

    public MobilityDetection setLoiteringDelayWifiConnectionTime(int loiteringDelayWifiConnectionTime) {
        this.loiteringDelayWifiConnectionTime = loiteringDelayWifiConnectionTime;
        return this;
    }

    public MobilityDetection setLoiteringDelayPowerConnectionChanged(int loiteringDelayPowerConnectionChanged) {
        this.loiteringDelayPowerConnectionChanged = loiteringDelayPowerConnectionChanged;
        return this;
    }

    public MobilityDetection setLoiteringDelayActivity(int loiteringDelayActivity) {
        this.loiteringDelayActivity = loiteringDelayActivity;
        return this;
    }

    private MobilityDetection setContext(Context context) {
        this.context = context;
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
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

    private MobilityDetection setListener(MobilityDetectionListener listener) {
        this.listener = listener;
        return this;
    }

    private final BroadcastReceiver listenerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
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
            mobilityDetectionService.loiteringDelayWifiConnectionChanged = loiteringDelayWifiConnectionChanged;
            mobilityDetectionService.loiteringDelayWifiConnectionTime = loiteringDelayWifiConnectionTime;
            mobilityDetectionService.loiteringDelayPowerConnectionChanged = loiteringDelayPowerConnectionChanged;
            mobilityDetectionService.loiteringDelayActivity = loiteringDelayActivity;
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

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

    public static class Builder {
        private Context context;
        private long interval = 1000 * 10;
        private long fastInterval = 1000;
        private long mediumInterval = 1000 * 60 * 3;
        private long slowInterval = 1000 * 60 * 6;
        private int loiteringDelayWifiConnectionChanged = 1000 * 60 * 5;
        private int loiteringDelayWifiConnectionTime = 1000 * 60 * 60 * 2;
        private int loiteringDelayPowerConnectionChanged = 1000 * 60 * 5;
        private int loiteringDelayActivity = 1000 * 60 * 15;
        private MobilityDetectionListener listener;

        public Builder() {

        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setFastInterval(long fastInterval) {
            this.fastInterval = fastInterval;
            return this;
        }

        public Builder setMediumInterval(long mediumInterval) {
            this.mediumInterval = mediumInterval;
            return this;
        }

        public Builder setSlowInterval(long slowInterval) {
            this.slowInterval = slowInterval;
            return this;
        }

        public Builder setLoiteringDelayWifiConnectionChanged(int loiteringDelayWifiConnectionChanged) {
            this.loiteringDelayWifiConnectionChanged = loiteringDelayWifiConnectionChanged;
            return this;
        }

        public Builder setLoiteringDelayWifiConnectionTime(int loiteringDelayWifiConnectionTime) {
            this.loiteringDelayWifiConnectionTime = loiteringDelayWifiConnectionTime;
            return this;
        }

        public Builder setLoiteringDelayPowerConnectionChanged(int loiteringDelayPowerConnectionChanged) {
            this.loiteringDelayPowerConnectionChanged = loiteringDelayPowerConnectionChanged;
            return this;
        }

        public Builder setLoiteringDelayActivity(int loiteringDelayActivity) {
            this.loiteringDelayActivity = loiteringDelayActivity;
            return this;
        }

        public Builder setListener(MobilityDetectionListener listener) {
            this.listener = listener;
            return this;
        }

        public MobilityDetection build() {
            return MobilityDetection.getInstance()
                    .setContext(context)
                    .setInterval(interval)
                    .setFastInterval(fastInterval)
                    .setMediumInterval(mediumInterval)
                    .setSlowInterval(slowInterval)
                    .setLoiteringDelayWifiConnectionChanged(loiteringDelayWifiConnectionChanged)
                    .setLoiteringDelayWifiConnectionTime(loiteringDelayWifiConnectionTime)
                    .setLoiteringDelayPowerConnectionChanged(loiteringDelayPowerConnectionChanged)
                    .setLoiteringDelayActivity(loiteringDelayActivity)
                    .setListener(listener);
        }
    }

}
