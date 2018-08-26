package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

/**
 * IntentService for handling Awareness API's fences.
 *
 * @deprecated
 */
public class FenceIntentService extends IntentService {

    private static final String TAG = FenceIntentService.class.getSimpleName();

    public FenceIntentService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.e(TAG, "onHandleIntent called");

        /*if (TextUtils.equals(fenceState.getFenceKey(), "stillActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "stillActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "stillActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "footActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "footActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "footActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "walkingActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "walkingActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "walkingActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "runningActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "runningActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "runningActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "bicycleActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "bicycleActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "bicycleActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "vehicleActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    *//*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*//*
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "vehicleActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "vehicleActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "slowActivityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG, "ON_FOOT, WALKING or RUNNING");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "slowActivityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "slowActivityFenceKey > UNKNOWN");
                    break;
            }
        }

        if (TextUtils.equals(fenceState.getFenceKey(), "fastActivityFenceKey")) {

        }*/

        if (TextUtils.equals(fenceState.getFenceKey(), "activityFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.e(TAG, "activityFenceKey > TRUE");
                    Awareness.getSnapshotClient(this).getDetectedActivity()
                            .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                                @Override
                                public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                    /*DetectedActivity activity = detectedActivityResponse.getActivityRecognitionResult().getMostProbableActivity();
                                    Log.e(TAG, "Activity: " + Activities.getActivityType(activity.getType()) + ", Confidence: " + activity.getConfidence());
                                    if (activity.getConfidence() >= 65) {
                                        broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                    }*/
                                    broadcastActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "activityFenceKey > FALSE");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "activityFenceKey > UNKNOWN");
                    break;
            }
        }

    }

    private void broadcastActivities(List<DetectedActivity> activities) {
        Intent intent = new Intent(Actions.ACTIVITY_LIST_ACTION);

        intent.putParcelableArrayListExtra("activities", (ArrayList<DetectedActivity>) activities);
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
