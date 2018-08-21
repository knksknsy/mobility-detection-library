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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.JSONManager;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.Timestamp;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    private static final long INTERVAL_AR = 1000;
    private static final long INTERVAL = 1000;
    private static final long INTERVAL_LOCATION = 1000 * 60;

    IBinder binder = new MobilityDetectionService.LocalBinder();

    private JSONManager jsonManager;

    private ActivityRecognitionClient activityRecognitionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofencingClient geofencingClient;

    private List<Geofence> geofenceList;

    private PendingIntent detectedActivityPendingIntent;
    private PendingIntent geofencingPendingIntent;
    private PendingIntent locationPendingIntent;
    private PendingIntent transitionPendingIntent;
    private PendingIntent fencePendingIntent;

    // private LocationRequest locationRequestTracking;
    // private FusedLocationProviderClient fusedLocationProviderClientTracking;
    // private FenceClient fenceClient;

    private IntentFilter filter = new IntentFilter();

    private boolean receiverInProgress = false;
    public boolean isCharging, isWifiConnected, isInGeofence;

    public MobilityDetectionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isCharging = false;
        isWifiConnected = false;
        isInGeofence = false;

        jsonManager = new JSONManager(this);

        addFilterActions();
        registerReceiver(receiver, filter);

        activityRecognitionClient = new ActivityRecognitionClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceList = new ArrayList<>();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Intent transitionsLoadedIntent = new Intent(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        transitionsLoadedIntent.putParcelableArrayListExtra("activities", jsonManager.getActivityTransitions());
        sendBroadcast(transitionsLoadedIntent, null);

        requestActivityRecognitionUpdates();
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
            addFilterActions();
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.GEOFENCE_TRANSITION_ACTION)) {
                handleGeofenceTransition(intent);
            }
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                handleConnectivityChange(intent);
            }
            if (action.equals(Intent.ACTION_POWER_CONNECTED) || action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                handlePowerChange(intent);
            }
            if (action.equals(Actions.ACTIVITY_DETECTED_ACTION)) {
                handleActivityDetection(intent);
            }
            if (action.equals(Actions.ACTIVITY_VALIDATED_ACTION)) {
                handleActivityValidation(intent);
            }
            if (action.equals(Actions.LOCATION_ACTION)) {
                Log.e(TAG, Actions.LOCATION_ACTION);
                DetectedLocation detectedLocation = intent.getParcelableExtra(DetectedLocation.class.getSimpleName());
                jsonManager.writeDetectedLocation(detectedLocation);
            }
        }
    };

    public void changeConfiguration() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);

                    addGeofence(detectedLocation);
                    requestGeofenceUpdates();
                }

                fusedLocationProviderClient.removeLocationUpdates(this);
            }
        };
        /*if (!isInGeofence && checkLocationPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);

                        addGeofence(detectedLocation);
                        requestGeofenceUpdates();
                    }

                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
        }*/

        if (isCharging && isWifiConnected && isInGeofence) {
            removeActivityRecognitionUpdates();
            // inactive
        }
        if (isCharging && isWifiConnected && !isInGeofence) {
            requestActivityRecognitionUpdates();
            // active
        }
        if (isCharging && !isWifiConnected && isInGeofence) {
            removeActivityRecognitionUpdates();
            // inactive
        }
        if (isCharging && !isWifiConnected && !isInGeofence) {
            requestActivityRecognitionUpdates();
            // active
        }
        if (!isCharging && isWifiConnected && isInGeofence) {
            removeActivityRecognitionUpdates();
            // inactive
        }
        if (!isCharging && isWifiConnected && !isInGeofence) {
            requestActivityRecognitionUpdates();
            // active
        }
        if (!isCharging && !isWifiConnected && isInGeofence) {
            removeActivityRecognitionUpdates();
            // inactive
        }
        if (!isCharging && !isWifiConnected && !isInGeofence) {
            // special
            requestActivityRecognitionUpdates();
            // active
        }

        Log.e(TAG, "isCharging: " + isCharging);
        Log.e(TAG, "isWifiConnected: " + isWifiConnected);
        Log.e(TAG, "isInGeofence: " + isInGeofence);
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void saveData() {
        Log.e(TAG, Actions.SAVE_DATA_ACTION);
        jsonManager.saveJSONFile();
    }

    public void requestActivityRecognitionUpdates() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(INTERVAL_AR, getDetectedActivityPendingIntent());

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

    private void requestGeofenceUpdates() {
        if (checkLocationPermission()) {
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private void addGeofence(DetectedLocation detectedLocation) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(detectedLocation.getTimestamp())
                .setCircularRegion(detectedLocation.getLatitude(), detectedLocation.getLongitude(), 50L)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000 * 5)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private void removeGeofence(String key) {
        for (int i = geofenceList.size() - 1; i >= 0; i--) {
            if (geofenceList.get(i).getRequestId().equals(key)) {
                Log.e(TAG, "removed");
                geofenceList.remove(i);
            }
        }
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

    public void removeActivityRecognitionUpdates() {
        activityRecognitionClient.removeActivityUpdates(getDetectedActivityPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Toast.makeText(getApplicationContext(), "Removed activity updates successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(getApplicationContext(), "Failed to remove activity updates!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGeofenceTransition(final Intent intent) {
        int geofenceTransition = intent.getIntExtra("geofenceTransition", -1);
        ArrayList<String> keys = intent.getStringArrayListExtra("keys");
        for (String key : keys) {
            Log.e(TAG, "key: " + key);
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.e(TAG, "GEOFENCE_TRANSITION_DWELL");
            isInGeofence = true;

            changeConfiguration();
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e(TAG, "GEOFENCE_TRANSITION_EXIT");

            for (int i = keys.size() - 1; i >= 0; i--) {
                removeGeofence(keys.get(i));
            }
            isInGeofence = false;

            changeConfiguration();
        }
    }

    private void handleConnectivityChange(final Intent intent) {
        Log.e(TAG, "CONNECTIVITY_ACTION");
        int connectionType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);

        if (connectionType == ConnectivityManager.TYPE_WIFI) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                if (info.isConnected()) {
                    isWifiConnected = true;
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    final String ssid = wifiInfo.getSSID();

                    if (!jsonManager.hasWifiLocation(ssid)) {
                        if (checkLocationPermission()) {
                            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    if (locationResult != null) {
                                        Location location = locationResult.getLastLocation();
                                        DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);
                                        jsonManager.writeWifiLocation(ssid, detectedLocation);
                                        jsonManager.saveJSONFile();
                                    }
                                    fusedLocationProviderClient.removeLocationUpdates(this);
                                }
                            }, new HandlerThread("WIFI_CONNECTION_ACTION_LOCATION_LOOPER").getLooper());
                        }
                    }
                }
            }
        } else {
            isWifiConnected = false;
        }

        changeConfiguration();

        Intent i = new Intent(Actions.WIFI_CONNECTION_ACTION);
        i.putExtra("isWifi", connectionType == ConnectivityManager.TYPE_WIFI);
        sendBroadcast(i, null);
    }

    private void handlePowerChange(final Intent intent) {
        Log.e(TAG, "ACTION_POWER_CONNECTED || ACTION_POWER_DISCONNECTED");
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        this.isCharging = isCharging;
        changeConfiguration();

        Intent i = new Intent(Actions.POWER_CONNECTION_ACTION);
        i.putExtra("isCharging", isCharging);
        i.putExtra("usbCharge", usbCharge);
        i.putExtra("acCharge", acCharge);
        sendBroadcast(i, null);
    }

    private void handleActivityDetection(final Intent intent) {
        if (!receiverInProgress) {
            Log.e(TAG, Actions.ACTIVITY_DETECTED_ACTION);
            /*if (checkLocationPermission()) {
                fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
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

                        fusedLocationProviderClient.removeLocationUpdates(this);
                    }
                }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
            }*/

            DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());

            DetectedActivities exitedActivity = jsonManager.getLastActivityTransition();

            String enteredActivity = detectedActivities.getProbableActivities().evaluateActivity(exitedActivity, detectedActivities);

            long diff;
            long dwellingTime = 1000 * 60 * 5;
            if (exitedActivity.getTimestamp() != null) {
                diff = Timestamp.getDifference(exitedActivity.getTimestamp(), detectedActivities.getTimestamp());
            } else {
                diff = dwellingTime;
            }

            boolean activityChanged = !enteredActivity.isEmpty() && !exitedActivity.getProbableActivities().getActivity().equals(enteredActivity);
            final boolean equalActivity = diff > dwellingTime && exitedActivity.getProbableActivities().getActivity().equals(enteredActivity);

            if (activityChanged || equalActivity) {
                receiverInProgress = true;
                if (checkLocationPermission()) {
                    fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            DetectedActivities detectedActivities;

                            if (locationResult != null) {
                                Location location = locationResult.getLastLocation();
                                DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), location);

                                if (equalActivity) {
                                    addGeofence(detectedLocation);
                                    requestGeofenceUpdates();
                                }

                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                                detectedActivities.setDetectedLocation(detectedLocation);
                            } else {
                                detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
                            }
                            jsonManager.writeActivityTransition(detectedActivities);
                            jsonManager.saveJSONFile();
                            Intent i = new Intent(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
                            i.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                            sendBroadcast(i, null);

                            fusedLocationProviderClient.removeLocationUpdates(this);
                            receiverInProgress = false;
                        }
                    }, new HandlerThread("ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER").getLooper());
                }
            }
        }
    }

    private void handleActivityValidation(final Intent intent) {
        Log.e(TAG, Actions.ACTIVITY_VALIDATED_ACTION);

        if (checkLocationPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
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
            }, new HandlerThread("ACTIVITY_VALIDATED_ACTION_LOCATION_LOOPER").getLooper());
        } else {
            String validation = intent.getStringExtra("validation");
            DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
            jsonManager.writeValidation(validation, detectedActivities);
        }
    }

    private void addFilterActions() {
        if (filter != null) {
            filter.addAction(Actions.LOCATION_ACTION);
            filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
            filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);
            filter.addAction(Actions.ACTIVITY_LIST_ACTION);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(Actions.GEOFENCE_TRANSITION_ACTION);
        }
    }

    private PendingIntent getDetectedActivityPendingIntent() {
        if (detectedActivityPendingIntent != null) {
            return detectedActivityPendingIntent;
        }
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        detectedActivityPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return detectedActivityPendingIntent;
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencingPendingIntent != null) {
            return geofencingPendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        geofencingPendingIntent = PendingIntent.getService(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencingPendingIntent;
    }

    private PendingIntent getLocationPendingIntent() {
        if (locationPendingIntent != null) {
            return locationPendingIntent;
        }
        Intent intent = new Intent(this, LocationIntentService.class);
        locationPendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return locationPendingIntent;
    }

    private PendingIntent getFencePendingIntent() {
        if (fencePendingIntent != null) {
            return fencePendingIntent;
        }
        Intent intent = new Intent(this, FenceIntentService.class);
        fencePendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return fencePendingIntent;
    }

    private PendingIntent getTransitionPendingIntent() {
        if (transitionPendingIntent != null) {
            return transitionPendingIntent;
        }
        Intent intent = new Intent(this, ActivityTransitionIntentService.class);
        transitionPendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return transitionPendingIntent;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)
                .setMaxWaitTime(5000);

        return locationRequest;
    }

    /*public void validateActivity(String activity) {
        Log.e(TAG, Actions.VALIDATE_ACTIVITY_ACTION);
        int requestCode = 4;
        Intent validationIntent = new Intent(this, ValidationIntentService.class);
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

        if (checkLocationPermission()) {
            Task<Void> task = fusedLocationProviderClientTracking.requestLocationUpdates(locationRequestTracking, locationPendingIntent);

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

}

