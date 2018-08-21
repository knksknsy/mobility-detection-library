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
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService;

public class MobilityDetection {

    private static final String TAG = MobilityDetection.class.getSimpleName();
    private static final MobilityDetection mobilityDetection = new MobilityDetection();

    private Context context;
    private MobilityDetectionListener listener;

    public MobilityDetectionService mobilityDetectionService;
    private boolean serviceBound = false;

    private IntentFilter filter = new IntentFilter();

    private MobilityDetection() {
    }

    public static MobilityDetection getInstance() {
        return mobilityDetection;
    }

    private MobilityDetection setContext(Context context) {
        this.context = context;
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
        filter.addAction(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        filter.addAction(Actions.ACTIVITY_LIST_ACTION);
        filter.addAction(Actions.STOP_MOBILITY_DETECTION_ACTION);
        filter.addAction(Actions.POWER_CONNECTION_ACTION);
        filter.addAction(Actions.WIFI_CONNECTION_ACTION);
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
                Log.e(TAG, Actions.POWER_CONNECTION_ACTION);
                intent.getBooleanExtra("usbCharge", false);
                intent.getBooleanExtra("acCharge", false);
                /*mobilityDetectionService.isCharging = intent.getBooleanExtra("isCharging", false);
                mobilityDetectionService.changeConfiguration();*/
                if (listener != null) {
                    listener.onBatteryManagerChanged();
                }
            }
            if (action.equals(Actions.WIFI_CONNECTION_ACTION)) {
                Log.e(TAG, Actions.WIFI_CONNECTION_ACTION);
                /*mobilityDetectionService.isWifiConnected = intent.getBooleanExtra("isWifi", false);
                mobilityDetectionService.changeConfiguration();*/
                if (listener != null) {
                    listener.onWifiConnectionChanged();
                }
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MobilityDetectionService.LocalBinder binder = (MobilityDetectionService.LocalBinder) service;
            mobilityDetectionService = binder.getServiceInstance();
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
            context.stopService(intent);
            context.unbindService(serviceConnection);
            context.unregisterReceiver(listenerReceiver);
            serviceBound = false;
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

    public static class Builder {
        private Context context;
        private MobilityDetectionListener listener;

        public Builder() {

        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setListener(MobilityDetectionListener listener) {
            this.listener = listener;
            return this;
        }

        public MobilityDetection build() {
            return MobilityDetection.getInstance().setContext(context).setListener(listener);
        }
    }

}
