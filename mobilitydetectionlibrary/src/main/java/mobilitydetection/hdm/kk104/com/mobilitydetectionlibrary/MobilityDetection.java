package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners.ActivityTransitionListener;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService;

public class MobilityDetection {

    private ActivityTransitionListener transitionListener;

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
        return this;
    }

    public void setTransitionListener(ActivityTransitionListener listener) {
        this.transitionListener = listener;
    }

    private final BroadcastReceiver activityTransitionedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION)) {
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                if (transitionListener != null) {
                    transitionListener.onTransitioned(detectedActivities);
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
            context.startService(intent);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            context.registerReceiver(activityTransitionedReceiver, filter);
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

    public void stopMobilityDetection() throws NullPointerException {
        if (context != null) {
            Intent intent = new Intent(context, MobilityDetectionService.class);
            context.stopService(intent);
            context.unbindService(serviceConnection);
            context.unregisterReceiver(activityTransitionedReceiver);
            serviceBound = false;
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

}
