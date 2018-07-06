package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public class DetectedActivitiesService extends IntentService {

    private static final String TAG = DetectedActivitiesService.class.getSimpleName();

    public DetectedActivitiesService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int requestCode = intent.getIntExtra("requestCode", -1);
        String validationActivity = intent.getStringExtra("validation");
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            if (requestCode == 3) {
                Log.e(TAG, "requestCode: " + requestCode);
                Log.e(TAG, "validation activity: " + validationActivity);
                broadcastValidationActivities(validationActivity, detectedActivities);
            } else {
                broadcastActivities(detectedActivities);
            }
        }
    }

    private void broadcastActivities(ArrayList<DetectedActivity> activities) {
        /*Intent intent = new Intent("ACTIVITY_INTENT");
        intent.putParcelableArrayListExtra("activities", activities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/

        EventBus.getDefault().post(activities);

        DetectedActivities detectedActivities = new DetectedActivities(activities);

        Intent fbDbIntent = new Intent("ACTIVITY_DETECTED_ACTION");
        fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
        sendBroadcast(fbDbIntent, null);
    }

    private void broadcastValidationActivities(String validation, ArrayList<DetectedActivity> activities) {
        DetectedActivities detectedActivities = new DetectedActivities(activities);

        Intent fbDbIntent = new Intent("VALIDATION_ACTIVITY_ACTION");
        fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
        fbDbIntent.putExtra("validation", validation);
        sendBroadcast(fbDbIntent, null);
    }

}
