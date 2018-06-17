package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

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
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            broadcastActivities(detectedActivities);
        }
    }

    private void broadcastActivities(ArrayList<DetectedActivity> activities) {
        Intent intent = new Intent("activity_intent");
        intent.putParcelableArrayListExtra("activities", activities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        DetectedActivities detectedActivities = new DetectedActivities(activities);

        Intent fbDbIntent = new Intent("ACTIVITY_DETECTED_ACTION");
        fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
        sendBroadcast(fbDbIntent, null);
    }

}
