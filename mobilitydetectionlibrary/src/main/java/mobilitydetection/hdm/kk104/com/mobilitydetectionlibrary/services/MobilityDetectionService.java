package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.JSONManager;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    private static final long INTERVAL_AR = 1000;
    private static final long INTERVAL = 1000;
    private static final long INTERVAL_LOCATION = 1000 * 60;

    IBinder binder = new MobilityDetectionService.LocalBinder();

    private JSONManager jsonManager;

    private PendingIntent activityPendingIntent;
    // private PendingIntent trackingPendingIntent;
    // private PendingIntent transitionPendingIntent;
    // private PendingIntent fencePendingIntent;

    private ActivityRecognitionClient activityRecognitionClient;
    // private FusedLocationProviderClient fusedLocationProviderClientTracking;
    // private LocationRequest locationRequestTracking;
    private FusedLocationProviderClient fusedLocationProviderClientTransition;
    private LocationRequest locationRequestTransition;
    // private FenceClient fenceClient;

    private IntentFilter filter = new IntentFilter();

    private boolean receiverInProgress = false;
    public boolean isCharging, isWifiConnected, isInGeofence;

    public void changeConfiguration() {
        Log.e(TAG, "isCharging: " + isCharging);
        Log.e(TAG, "isWifiConnected: " + isWifiConnected);
        Log.e(TAG, "isInGeofence: " + isInGeofence);
    }

    public MobilityDetectionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isCharging = false;
        isWifiConnected = false;
        isInGeofence = false;

        jsonManager = new JSONManager(this);

        Intent transitionsLoadedIntent = new Intent(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        transitionsLoadedIntent.putParcelableArrayListExtra("activities", jsonManager.getActivityTransitions());
        sendBroadcast(transitionsLoadedIntent, null);

        filter.addAction(Actions.LOCATION_ACTION);
        filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
        filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);
        filter.addAction(Actions.ACTIVITY_TRANSITIONED_ACTION);
        filter.addAction(Actions.ACTIVITY_LIST_ACTION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(receiver, filter);

        activityRecognitionClient = new ActivityRecognitionClient(this);

        Intent activityIntent = new Intent(this, DetectedActivitiesService.class);
        // Intent trackingIntent = new Intent(this, TrackingService.class);
        // Intent transitionIntent = new Intent(this, ActivityTransitionService.class);
        // Intent fenceIntent = new Intent(this, FenceService.class);

        activityPendingIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // trackingPendingIntent = PendingIntent.getService(this, 1, trackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // transitionPendingIntent = PendingIntent.getService(this, 3, transitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // fencePendingIntent = PendingIntent.getService(this, 2, fenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        requestActivityRecognitionUpdates();
        requestTransitionLocationUpdates();
        // requestActivityRecognitionTransitionUpdates();
        // requestAwarenessUpdates();
        // requestTrackingLocationUpdates();

        buildNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // removeActivityRecognitionUpdates();
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (filter.countActions() == 0) {
            filter.addAction(Actions.LOCATION_ACTION);
            filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
            filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);
            filter.addAction(Actions.ACTIVITY_TRANSITIONED_ACTION);
            filter.addAction(Actions.ACTIVITY_LIST_ACTION);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        }
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public MobilityDetectionService getServiceInstance() {
            return MobilityDetectionService.this;
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                int connectionType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                boolean isWifi = connectionType == ConnectivityManager.TYPE_WIFI;
                Log.e(TAG, "WIFI connected: " + isWifi);
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected()) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();

                        Log.e(TAG, "SSID: " + ssid);
                    }
                }
            }
            if (action.equals(Intent.ACTION_POWER_CONNECTED) || action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

                // todo: change configuration on MobilityDetectionConfigurator

                Intent i = new Intent(Actions.POWER_CONNECTION_ACTION);
                i.putExtra("isCharging", isCharging);
                i.putExtra("usbCharge", usbCharge);
                i.putExtra("acCharge", acCharge);
                context.sendBroadcast(i, null);
            }
            if (action.equals(Actions.ACTIVITY_DETECTED_ACTION)) {
                if (!receiverInProgress) {
                    Log.e(TAG, Actions.ACTIVITY_DETECTED_ACTION);
                    /*if (checkPermission()) {
                    fusedLocationProviderClientTransition.requestLocationUpdates(locationRequestTransition, new LocationCallback() {
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
                            jsonManager.writeActivityTransition(detectedActivities);
                            Intent i = new Intent(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
                            i.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                            sendBroadcast(i, null);

                            fusedLocationProviderClientTransition.removeLocationUpdates(this);
                        }
                    }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
                }*/

                    DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());

                    DetectedActivities exitedActivity = jsonManager.getLastActivityTransition();

                    String enteredActivity = detectedActivities.getProbableActivities().evaluateActivity(exitedActivity, detectedActivities);

                    if (!enteredActivity.isEmpty() && !exitedActivity.getProbableActivities().getActivity().equals(enteredActivity)) {
                        receiverInProgress = true;
                        if (checkPermission()) {
                            fusedLocationProviderClientTransition.requestLocationUpdates(locationRequestTransition, new LocationCallback() {
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
                                    jsonManager.writeActivityTransition(detectedActivities);
                                    jsonManager.writeJSONFile();
                                    Intent i = new Intent(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
                                    i.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                                    sendBroadcast(i, null);

                                    fusedLocationProviderClientTransition.removeLocationUpdates(this);
                                    receiverInProgress = false;
                                }
                            }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
                        }
                    }
                }
            }
            if (action.equals(Actions.ACTIVITY_VALIDATED_ACTION)) {
                Log.e(TAG, Actions.ACTIVITY_VALIDATED_ACTION);

                /*String validation = intent.getStringExtra("validation");
                DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                jsonManager.writeValidation(validation, detectedActivities);*/

                /*if (checkPermission()) {
                    fusedLocationProviderClientTransition.requestLocationUpdates(locationRequestTransition, new LocationCallback() {
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

                            fusedLocationProviderClientTransition.removeLocationUpdates(this);
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
                /*Log.e(TAG, Actions.ACTIVITY_TRANSITIONED_ACTION);

                TransitionedActivity transitionedActivity = intent.getParcelableExtra(TransitionedActivity.class.getSimpleName());
                jsonManager.writeTransitionedActivity(transitionedActivity);


                if (checkPermission()) {
                    fusedLocationProviderClientTransition.requestLocationUpdates(locationRequestTransition, new LocationCallback() {
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

                            fusedLocationProviderClientTransition.removeLocationUpdates(this);
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
            if (action.equals(Actions.LOCATION_ACTION)) {
                Log.e(TAG, Actions.LOCATION_ACTION);
                /*DetectedLocation detectedLocation = intent.getParcelableExtra(DetectedLocation.class.getSimpleName());
                jsonManager.writeDetectedLocation(detectedLocation);*/
            }
        }
    };

    public void saveData() {
        Log.e(TAG, Actions.SAVE_DATA_ACTION);
        jsonManager.writeJSONFile();
    }

    public void requestTransitionLocationUpdates() {
        locationRequestTransition = new LocationRequest();

        locationRequestTransition.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)
                .setMaxWaitTime(5000);

        fusedLocationProviderClientTransition = LocationServices.getFusedLocationProviderClient(this);
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

    private void buildNotification() {
        Intent intent = new Intent(Actions.STOP_MOBILITY_DETECTION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.notification_id))
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.tracking_enabled_notify))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name);
        startForeground(1, notification.build());
    }

    /*public void requestAwarenessUpdates() {
     *//*SnapshotClient snapshotClient = Awareness.getSnapshotClient(this);

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
        });*//*

     *//*AwarenessFence stillFence = DetectedActivityFence.starting(DetectedActivityFence.STILL);
        AwarenessFence footFence = DetectedActivityFence.starting(DetectedActivityFence.ON_FOOT);
        AwarenessFence walkingFence = DetectedActivityFence.starting(DetectedActivityFence.WALKING);
        AwarenessFence runningFence = DetectedActivityFence.starting(DetectedActivityFence.RUNNING);
        AwarenessFence bicycleFence = DetectedActivityFence.starting(DetectedActivityFence.ON_BICYCLE);
        AwarenessFence vehicleFence = DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        AwarenessFence unknownFence = DetectedActivityFence.starting(DetectedActivityFence.UNKNOWN);

        *//**//*AwarenessFence slowActivityFence = AwarenessFence.or(walkingFence, runningFence, footFence);
        AwarenessFence fastActivityFence = AwarenessFence.or(bicycleFence, vehicleFence);*//**//*

        AwarenessFence activityFence = AwarenessFence.or(stillFence, footFence, walkingFence, runningFence, bicycleFence, vehicleFence, unknownFence);

        fenceClient = Awareness.getFenceClient(this);

        FenceUpdateRequest fenceRequest = new FenceUpdateRequest.Builder()
                *//**//*.addFence("stillActivityFenceKey", stillFence, fencePendingIntent)
                .addFence("footActivityFenceKey", footFence, fencePendingIntent)
                .addFence("walkingActivityFenceKey", walkingFence, fencePendingIntent)
                .addFence("runningActivityFenceKey", runningFence, fencePendingIntent)
                .addFence("bicycleActivityFenceKey", bicycleFence, fencePendingIntent)
                .addFence("vehicleActivityFenceKey", vehicleFence, fencePendingIntent)
                .addFence("slowActivityFenceKey", slowActivityFence, fencePendingIntent)
                .addFence("fastActivityFenceKey", fastActivityFence, fencePendingIntent)*//**//*
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
        });*//*
    }*/

    /*private void requestTrackingLocationUpdates() {
        locationRequestTracking = new LocationRequest();

        locationRequestTracking.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequestTracking.setInterval(INTERVAL_LOCATION);
        locationRequestTracking.setFastestInterval(INTERVAL);

        fusedLocationProviderClientTracking = LocationServices.getFusedLocationProviderClient(this);

        if (checkPermission()) {
            Task<Void> task = fusedLocationProviderClientTracking.requestLocationUpdates(locationRequestTracking, trackingPendingIntent);

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

    /*public void validateActivity(String activity) {
        Log.e(TAG, Actions.VALIDATE_ACTIVITY_ACTION);
        int requestCode = 4;
        Intent validationIntent = new Intent(this, ValidationService.class);
        validationIntent.putExtra("validation", activity);

        PendingIntent validationPendingIntent = PendingIntent.getService(this, requestCode, validationIntent, PendingIntent.FLAG_ONE_SHOT);

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

                *//*Awareness.getSnapshotClient(getApplicationContext()).getDetectedActivity()
                        .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                            @Override
                            public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                DetectedActivities detectedActivities = new DetectedActivities(detectedActivityResponse.getActivityRecognitionResult().getProbableActivities());

                                Intent fbDbIntent = new Intent("VALIDATION_ACTIVITY_ACTION");
                                fbDbIntent.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                                fbDbIntent.putExtra("validation", activity);
                                sendBroadcast(fbDbIntent, null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });*//*
    }*/

    /*public void removeActivityRecognitionUpdates() {
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
    }*/

}

