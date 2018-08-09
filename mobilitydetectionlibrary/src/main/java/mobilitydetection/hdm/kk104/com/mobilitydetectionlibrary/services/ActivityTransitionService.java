package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;

import com.google.android.gms.location.ActivityTransitionResult;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.TransitionedActivity;

public class ActivityTransitionService extends IntentService {

    private static final String TAG = ActivityTransitionService.class.getSimpleName();

    private ActivityTransitionResult result;

    public ActivityTransitionService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            result = ActivityTransitionResult.extractResult(intent);

            TransitionedActivity transitionedActivity = new TransitionedActivity(result);

            broadcastTransition(transitionedActivity);

            /*int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "PERMISSION_GRANTED");

                locationRequest = new LocationRequest();

                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(0);
                locationRequest.setFastestInterval(0);

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);
                                    TransitionedActivity transitionedActivity = new TransitionedActivity(result, detectedLocation);

                                    broadcastTransition(transitionedActivity);
                                } else {
                                    TransitionedActivity transitionedActivity = new TransitionedActivity(result);
                                    broadcastTransition(transitionedActivity);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });
            } else {
                Log.e(TAG, "!PERMISSION_GRANTED");
                TransitionedActivity transitionedActivity = new TransitionedActivity(result);
                broadcastTransition(transitionedActivity);
            }*/
        } else {
            result = null;
        }
    }

    private void broadcastTransition(TransitionedActivity activity) {
        Intent intent = new Intent(Actions.ACTIVITY_TRANSITIONED_ACTION);
        intent.putExtra(TransitionedActivity.class.getSimpleName(), (Parcelable) activity);
        sendBroadcast(intent, null);
    }

}
