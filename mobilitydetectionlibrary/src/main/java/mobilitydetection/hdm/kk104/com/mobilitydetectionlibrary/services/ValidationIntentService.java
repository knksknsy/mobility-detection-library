package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public class ValidationIntentService extends IntentService {

    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public ValidationIntentService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String validationActivity = intent.getStringExtra("validation");

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            ArrayList<DetectedActivity> activities = (ArrayList) result.getProbableActivities();
            Log.e(TAG, "ValidationIntentService onHandleIntent: " + validationActivity);
            broadcastValidationActivities(validationActivity, activities);
        }
    }

    private void broadcastValidationActivities(String validation, ArrayList<DetectedActivity> activities) {
        DetectedActivities detectedActivities = new DetectedActivities(activities);
        Intent intent = new Intent(Actions.ACTIVITY_VALIDATED_ACTION);
        intent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
        intent.putExtra("validation", validation);
        sendBroadcast(intent, null);
    }

}
