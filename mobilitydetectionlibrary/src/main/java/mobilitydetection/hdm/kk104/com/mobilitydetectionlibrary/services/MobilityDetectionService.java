package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.FenceClient;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.FirebaseDatabaseStatistic;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Credentials;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    private static final long INTERVAL_AR = 1000 * 5;
    private static final long INTERVAL = 1000;
    private static final long INTERVAL_LOCATION = 1000 * 60;

    IBinder binder = new MobilityDetectionService.LocalBinder();

    private PendingIntent activityPendingIntent, trackingPendingIntent, fencePendingIntent;
    private FirebaseDatabaseStatistic fbStatistic;

    private ActivityRecognitionClient activityRecognitionClient;
    private FenceClient fenceClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private IntentFilter filter = new IntentFilter();

    public MobilityDetectionService() {
    }

    public class LocalBinder extends Binder {
        public MobilityDetectionService getServiceInstance() {
            return MobilityDetectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final BroadcastReceiver firebaseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("LOCATION_ACTION")) {
                DetectedLocation detectedLocation = intent.getParcelableExtra(DetectedLocation.class.getSimpleName());
                Log.e(TAG, "detectedLocation: " + detectedLocation.getLatitude() + "," + detectedLocation.getLongitude());
                fbStatistic.uploadDetectedLocation(detectedLocation);
            }
            if (action.equals("ACTIVITY_DETECTED_ACTION")) {
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                Log.e(TAG, "detectedActivity: " + detectedActivities.getDetectedActivities());
                fbStatistic.uploadDetectedActivity(detectedActivities);
            }
            if (action.equals("VALIDATION_ACTIVITY_ACTION")) {
                String validation = intent.getStringExtra("validation");
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                Log.e(TAG, "validation: " + detectedActivities.getDetectedActivities());
                fbStatistic.uploadValidation(validation, detectedActivities);
                fbStatistic.uploadEvent("EVENT");
                // EventBus.getDefault().post("REMOVE_ACTIVITY_RECOGNITION");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        fbStatistic = new FirebaseDatabaseStatistic(this);
        fbStatistic.uploadEvent("MobilityDetectionService onCreate");

        filter.addAction("LOCATION_ACTION");
        filter.addAction("ACTIVITY_DETECTED_ACTION");
        filter.addAction("VALIDATION_ACTIVITY_ACTION");

        registerReceiver(firebaseReceiver, filter);

        activityRecognitionClient = new ActivityRecognitionClient(this);

        Intent activityIntent = new Intent(this, DetectedActivitiesService.class);
        Intent trackingIntent = new Intent(this, TrackingService.class);
        // Intent fenceIntent = new Intent(this, FenceService.class);

        activityPendingIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        trackingPendingIntent = PendingIntent.getService(this, 1, trackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // fencePendingIntent = PendingIntent.getService(this, 2, fenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        buildNotification();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        fbStatistic.uploadEvent("MobilityDetectionService onStartCommand");
        Credentials credentials = intent.getParcelableExtra("credentials");
        loginToFirebase(credentials);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // removeActivityUpdates();
        unregisterReceiver(firebaseReceiver);
        fbStatistic.uploadEvent("MobilityDetectionService onDestroy");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (filter.countActions() == 0) {
            filter.addAction("LOCATION_ACTION");
            filter.addAction("ACTIVITY_DETECTED_ACTION");
            filter.addAction("VALIDATION_ACTIVITY_ACTION");
        }
        registerReceiver(firebaseReceiver, filter);
        fbStatistic.uploadEvent("MobilityDetectionService onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        fbStatistic.uploadEvent("MobilityDetectionService onUnbind");
        return super.onUnbind(intent);
    }

    public void requestActivityRecognitionUpdates() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(INTERVAL_AR, activityPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Toast.makeText(getApplicationContext(), "Successfully requested activity updates", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Toast.makeText(getApplicationContext(), "Requesting activity updates failed to start", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeActivityRecognitionUpdates() {
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(activityPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Toast.makeText(getApplicationContext(), "Removed activity updates successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Toast.makeText(getApplicationContext(), "Failed to remove activity updates!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void requestAwarenessUpdates() {
        AwarenessFence stillFence = DetectedActivityFence.during(DetectedActivityFence.STILL);
        AwarenessFence footFence = DetectedActivityFence.during(DetectedActivityFence.ON_FOOT);
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        AwarenessFence runningFence = DetectedActivityFence.during(DetectedActivityFence.RUNNING);
        AwarenessFence bicycleFence = DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE);
        AwarenessFence vehicleFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);

        AwarenessFence slowActivityFence = AwarenessFence.or(walkingFence, runningFence, footFence);
        AwarenessFence fastActivityFence = AwarenessFence.or(bicycleFence, vehicleFence);

        /*GoogleApiClient client = new GoogleApiClient.Builder(this).addApi(Awareness.API).build();
        client.connect();*/

        /*SnapshotClient snapshotClient = Awareness.getSnapshotClient(this);

        Task<DetectedActivityResponse> snapshotTask = snapshotClient.getDetectedActivity();

        snapshotTask.addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
            @Override
            public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                ActivityRecognitionResult result = detectedActivityResponse.getActivityRecognitionResult();
                ArrayList<DetectedActivity> activities = (ArrayList<DetectedActivity>) result.getProbableActivities();

                Intent intent = new Intent("ACTIVITY_INTENT");
                intent.putParcelableArrayListExtra("activities", activities);
                LocalBroadcastManager.getInstance(MobilityDetectionService.this).sendBroadcast(intent);

                DetectedActivities detectedActivities = new DetectedActivities(activities);

                Intent fbDbIntent = new Intent("ACTIVITY_DETECTED_ACTION");
                fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                sendBroadcast(fbDbIntent, null);
            }
        });

        snapshotTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });*/

        fenceClient = Awareness.getFenceClient(this);

        FenceUpdateRequest fenceRequest = new FenceUpdateRequest.Builder()
                .addFence("stillActivityFenceKey", stillFence, fencePendingIntent)
                .addFence("footActivityFenceKey", footFence, fencePendingIntent)
                .addFence("walkingActivityFenceKey", walkingFence, fencePendingIntent)
                .addFence("runningActivityFenceKey", runningFence, fencePendingIntent)
                .addFence("bicycleActivityFenceKey", bicycleFence, fencePendingIntent)
                .addFence("vehicleActivityFenceKey", vehicleFence, fencePendingIntent)
                /*.addFence("slowActivityFenceKey", slowActivityFence, fencePendingIntent)
                .addFence("fastActivityFenceKey", fastActivityFence, fencePendingIntent)*/
                .build();
        Task<Void> task = fenceClient.updateFences(fenceRequest);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "FenceUpdateRequest successfully requested");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FenceUpdateRequest has failed + " + e);
            }
        });
    }

    private void requestLocationUpdates() {
        locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // locationRequest.setInterval(INTERVAL);
        locationRequest.setInterval(INTERVAL_LOCATION);
        locationRequest.setFastestInterval(INTERVAL);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Task<Void> task = fusedLocationProviderClient.requestLocationUpdates(locationRequest, trackingPendingIntent);

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(TAG, "Location listener successful.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Location listener failed.");
                }
            });
        }
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 4, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notify))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_stat_name);
        startForeground(1, builder.build());
    }

    private void loginToFirebase(Credentials credentials) {
        Log.e(TAG, "login: " + credentials.getLogin() + ", password: " + credentials.getPassword());
        FirebaseAuth.getInstance().signInWithEmailAndPassword(credentials.getLogin(), credentials.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error: " + e);
                            }
                        });
                        if (task.isSuccessful()) {
                            requestActivityRecognitionUpdates();
                            // requestAwarenessUpdates();
                            requestLocationUpdates();

                        } else {
                            Log.e(TAG, "Firebase authentication failed");
                        }
                    }
                });
    }
}

