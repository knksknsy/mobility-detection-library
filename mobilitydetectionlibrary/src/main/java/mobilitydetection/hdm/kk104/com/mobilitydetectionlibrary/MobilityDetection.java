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

    private MobilityDetectionListener transitionListener;

    private static final String TAG = MobilityDetection.class.getSimpleName();

    private static final MobilityDetection mobilityDetection = new MobilityDetection();

    private Context context;

    public MobilityDetectionService mobilityDetectionService;
    private boolean serviceBound = false;

    private IntentFilter filter = new IntentFilter();

    private MobilityDetection() {
    }

    public Context getContext() {
        return context;
    }

    public MobilityDetection setContext(Context context) {
        this.context = context;
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
        filter.addAction(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        filter.addAction(Actions.ACTIVITY_LIST_ACTION);
        filter.addAction(Actions.STOP_MOBILITY_DETECTION_ACTION);
        return this;
    }

    public void setListener(MobilityDetectionListener listener) {
        this.transitionListener = listener;
    }

    private final BroadcastReceiver activityTransitionedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                if (transitionListener != null) {
                    transitionListener.onTransitioned(detectedActivities);
                }
            }
            if (action.equals(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
                ArrayList<DetectedActivities> detectedActivities = intent.getParcelableArrayListExtra("activities");
                if (transitionListener != null) {
                    transitionListener.onTransitionsLoaded(detectedActivities);
                }
            }
            if (action.equals(Actions.ACTIVITY_LIST_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_LIST_ACTION);
                ArrayList<DetectedActivity> activities = intent.getParcelableArrayListExtra("activities");
                if (transitionListener != null) {
                    transitionListener.onActivityDetected(activities);
                }
            }
            if (action.equals(Actions.STOP_MOBILITY_DETECTION_ACTION)) {
                Log.e(TAG, Actions.STOP_MOBILITY_DETECTION_ACTION);
                stopMobilityDetection();
                if (transitionListener != null) {
                    transitionListener.onStopService();
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

    public static MobilityDetection getInstance() {
        return mobilityDetection;
    }

    public void startMobilityDetection() throws NullPointerException {
        if (context != null) {
            Intent intent = new Intent(context, MobilityDetectionService.class);
            ContextCompat.startForegroundService(context, intent);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            context.registerReceiver(activityTransitionedReceiver, filter);
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
            context.unregisterReceiver(activityTransitionedReceiver);
            serviceBound = false;
            Log.e(TAG, "service stopped");
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

}
