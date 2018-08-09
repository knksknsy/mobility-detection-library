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
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.JSONManager;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.TransitionedActivity;

public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    private static final long INTERVAL_AR = 1000;
    private static final long INTERVAL = 1000;
    private static final long INTERVAL_LOCATION = 1000; // * 60;

    IBinder binder = new MobilityDetectionService.LocalBinder();

    private JSONManager jsonManager;

    private PendingIntent activityPendingIntent;
    // private PendingIntent trackingPendingIntent;
    // private PendingIntent transitionPendingIntent;
    // private PendingIntent fencePendingIntent;

    private ActivityRecognitionClient activityRecognitionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    // private FenceClient fenceClient;

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

    private final BroadcastReceiver databaseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.SAVE_DATA_ACTION)) {
                Log.e(TAG, Actions.SAVE_DATA_ACTION);
                jsonManager.writeJSONFile();
            }
            if (action.equals(Actions.LOCATION_ACTION)) {
                Log.e(TAG, Actions.LOCATION_ACTION);
                DetectedLocation detectedLocation = intent.getParcelableExtra(DetectedLocation.class.getSimpleName());
                jsonManager.writeDetectedLocation(detectedLocation);
            }
            if (action.equals(Actions.ACTIVITY_DETECTED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_DETECTED_ACTION);

                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                jsonManager.writeDetectedActivity(detectedActivities);

                /*int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            DetectedActivities detectedActivities;

                            if (locationResult != null) {
                                Location location = locationResult.getLastLocation();
                                DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);

                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                                detectedActivities.setDetectedLocation(detectedLocation);
                            } else {
                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                            }

                            jsonManager.writeDetectedActivity(detectedActivities);

                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                        }
                    }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
                } else {
                    DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                    jsonManager.writeDetectedActivity(detectedActivities);
                }*/
            }
            if (action.equals(Actions.ACTIVITY_VALIDATED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_VALIDATED_ACTION);

                String validation = intent.getStringExtra("validation");
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                jsonManager.writeValidation(validation, detectedActivities);

                /*int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            String validation = intent.getStringExtra("validation");
                            DetectedActivities detectedActivities;

                            if (locationResult != null) {
                                Location location = locationResult.getLastLocation();
                                DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);

                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                                detectedActivities.setDetectedLocation(detectedLocation);
                            } else {
                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                            }

                            jsonManager.writeValidation(validation, detectedActivities);

                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                        }
                    }, new HandlerThread("ACTIVITY_VALIDATED_ACTION_LOCATION_LOOPER").getLooper());
                } else {
                    String validation = intent.getStringExtra("validation");
                    DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                    jsonManager.writeValidation(validation, detectedActivities);
                }*/
            }
            if (action.equals(Actions.ACTIVITY_TRANSITIONED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_TRANSITIONED_ACTION);

                TransitionedActivity transitionedActivity = intent.getParcelableExtra(TransitionedActivity.class.getSimpleName());
                jsonManager.writeTransitionedActivity(transitionedActivity);

                /*int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            TransitionedActivity transitionedActivity;

                            if (locationResult != null) {
                                Location location = locationResult.getLastLocation();
                                DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);
                                transitionedActivity = intent.getParcelableExtra(TransitionedActivity.class.getSimpleName());

                                transitionedActivity.setDetectedLocation(detectedLocation);
                            } else {
                                transitionedActivity = intent.getParcelableExtra(TransitionedActivity.class.getSimpleName());
                            }

                            jsonManager.writeTransitionedActivity(transitionedActivity);

                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                        }
                    }, new HandlerThread("ACTIVITY_TRANSITIONED_ACTION_LOCATION_LOOPER").getLooper());
                } else {
                    TransitionedActivity transitionedActivity = intent.getParcelableExtra(TransitionedActivity.class.getSimpleName());
                    jsonManager.writeTransitionedActivity(transitionedActivity);
                }*/
            }
            if (action.equals("STOP_MOBILITY_DETECTION_ACTION")) {
                Log.e(TAG, "STOP_MOBILITY_DETECTION_ACTION");
                unregisterReceiver(databaseReceiver);
                stopSelf();
            }
            if (action.equals(Actions.VALIDATE_ACTIVITY_ACTION)) {
                Log.e(TAG, Actions.VALIDATE_ACTIVITY_ACTION);
                int requestCode = 4;
                Intent validationIntent = new Intent(MobilityDetectionService.this, ValidationService.class);
                validationIntent.putExtra("validation", intent.getStringExtra("validation"));

                PendingIntent validationPendingIntent = PendingIntent.getService(MobilityDetectionService.this, requestCode, validationIntent, PendingIntent.FLAG_ONE_SHOT);

                activityRecognitionClient.requestActivityUpdates(0, validationPendingIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG, "requestActivityUpdates onSuccess");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });

                /*Awareness.getSnapshotClient(getApplicationContext()).getDetectedActivity()
                        .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                            @Override
                            public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                DetectedActivities detectedActivities = new DetectedActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());

                                Intent fbDbIntent = new Intent("VALIDATION_ACTIVITY_ACTION");
                                fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), (Parcelable) detectedActivities);
                                fbDbIntent.putExtra("validation", activity);
                                sendBroadcast(fbDbIntent, null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });*/
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        jsonManager = new JSONManager(this);

        filter.addAction("STOP_MOBILITY_DETECTION_ACTION");
        filter.addAction(Actions.SAVE_DATA_ACTION);
        filter.addAction(Actions.LOCATION_ACTION);
        filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
        filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_ACTION);
        filter.addAction(Actions.VALIDATE_ACTIVITY_ACTION);

        registerReceiver(databaseReceiver, filter);

        activityRecognitionClient = new ActivityRecognitionClient(this);

        Intent activityIntent = new Intent(this, DetectedActivitiesService.class);
        // Intent trackingIntent = new Intent(this, TrackingService.class);
        // Intent transitionIntent = new Intent(this, ActivityTransitionService.class);
        // Intent fenceIntent = new Intent(this, FenceService.class);

        activityPendingIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // trackingPendingIntent = PendingIntent.getService(this, 1, trackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // transitionPendingIntent = PendingIntent.getService(this, 3, transitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // fencePendingIntent = PendingIntent.getService(this, 2, fenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        buildNotification();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        requestActivityRecognitionUpdates();
        // requestActivityRecognitionTransitionUpdates();
        // initLocationProvider();
        // requestAwarenessUpdates();
        // requestLocationUpdates();

        return START_STICKY;
    }

    /*public void initLocationProvider() {
        locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        // removeActivityRecognitionUpdates();
        unregisterReceiver(databaseReceiver);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (filter.countActions() == 0) {
            filter.addAction("STOP_MOBILITY_DETECTION_ACTION");
            filter.addAction(Actions.SAVE_DATA_ACTION);
            filter.addAction(Actions.LOCATION_ACTION);
            filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
            filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);
            filter.addAction(Actions.ACTIVITY_TRANSITIONED_ACTION);
            filter.addAction(Actions.VALIDATE_ACTIVITY_ACTION);
        }
        registerReceiver(databaseReceiver, filter);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /*public void requestActivityRecognitionTransitionUpdates() {
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        );

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        activityRecognitionClient.requestActivityTransitionUpdates(request, transitionPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "requestActivityTransitionUpdates successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "requestActivityTransitionUpdates failed");
                    }
                });
    }*/

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
        /*SnapshotClient snapshotClient = Awareness.getSnapshotClient(this);

        Task<DetectedActivityResponse> snapshotTask = snapshotClient.getDetectedActivity();

        snapshotTask.addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
            @Override
            public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                ActivityRecognitionResult result = detectedActivityResponse.getActivityRecognitionResult();
                ArrayList<DetectedActivity> activities = (ArrayList<DetectedActivity>) result.getProbableActivities();

                Intent intent = new Intent(Actions.ACTIVITY_LIST_ACTION);
                intent.putParcelableArrayListExtra("activities", activities);
                LocalBroadcastManager.getInstance(MobilityDetectionService.this).sendBroadcast(intent);

                DetectedActivities detectedActivities = new DetectedActivities(activities);

                Intent fbDbIntent = new Intent(Actions.ACTIVITY_DETECTED_ACTION);
                fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                sendBroadcast(fbDbIntent, null);
            }
        });

        snapshotTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });*/

        /*AwarenessFence stillFence = DetectedActivityFence.starting(DetectedActivityFence.STILL);
        AwarenessFence footFence = DetectedActivityFence.starting(DetectedActivityFence.ON_FOOT);
        AwarenessFence walkingFence = DetectedActivityFence.starting(DetectedActivityFence.WALKING);
        AwarenessFence runningFence = DetectedActivityFence.starting(DetectedActivityFence.RUNNING);
        AwarenessFence bicycleFence = DetectedActivityFence.starting(DetectedActivityFence.ON_BICYCLE);
        AwarenessFence vehicleFence = DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        AwarenessFence unknownFence = DetectedActivityFence.starting(DetectedActivityFence.UNKNOWN);

        *//*AwarenessFence slowActivityFence = AwarenessFence.or(walkingFence, runningFence, footFence);
        AwarenessFence fastActivityFence = AwarenessFence.or(bicycleFence, vehicleFence);*//*

        AwarenessFence activityFence = AwarenessFence.or(stillFence, footFence, walkingFence, runningFence, bicycleFence, vehicleFence, unknownFence);

        fenceClient = Awareness.getFenceClient(this);

        FenceUpdateRequest fenceRequest = new FenceUpdateRequest.Builder()
                *//*.addFence("stillActivityFenceKey", stillFence, fencePendingIntent)
                .addFence("footActivityFenceKey", footFence, fencePendingIntent)
                .addFence("walkingActivityFenceKey", walkingFence, fencePendingIntent)
                .addFence("runningActivityFenceKey", runningFence, fencePendingIntent)
                .addFence("bicycleActivityFenceKey", bicycleFence, fencePendingIntent)
                .addFence("vehicleActivityFenceKey", vehicleFence, fencePendingIntent)
                .addFence("slowActivityFenceKey", slowActivityFence, fencePendingIntent)
                .addFence("fastActivityFenceKey", fastActivityFence, fencePendingIntent)*//*
                .addFence("activityFenceKey", activityFence, fencePendingIntent)
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
        });*/
    }

    /*private void requestLocationUpdates() {
        locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    }*/

    // todo
    private void buildNotification() {
        Intent stopIntent = new Intent("STOP_MOBILITY_DETECTION_ACTION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.tracking_enabled_notify))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .build();
        startForeground(1, notification);
    }

}

