package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

/**
 * IntentService for getting ActivityRecognitionResult.
 */
public class DetectedActivitiesIntentService extends IntentService {

    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*int requestCode = intent.getIntExtra("requestCode", -1);
        String validationActivity = intent.getStringExtra("validation");*/
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            /*if (requestCode == 4) {
                Log.e(TAG, "validation activity: " + validationActivity);
                broadcastValidationActivities(validationActivity, detectedActivities);
            } else if (requestCode == 0) {
                broadcastActivities(detectedActivities);
            }*/

            broadcastActivities(detectedActivities);
        }
    }

    private void broadcastActivities(ArrayList<DetectedActivity> activities) {
        Intent intent = new Intent(Actions.ACTIVITY_LIST_ACTION);
        intent.putParcelableArrayListExtra("activities", activities);
        sendBroadcast(intent, null);

        DetectedActivities detectedActivities = new DetectedActivities(activities);

        broadcastDetectedActivities(detectedActivities);
    }

    private void broadcastDetectedActivities(DetectedActivities detectedActivities) {
        Intent intent = new Intent(Actions.ACTIVITY_DETECTED_ACTION);
        intent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
        sendBroadcast(intent, null);
    }

}
