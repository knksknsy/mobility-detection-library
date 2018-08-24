package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.DataManager;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.Timestamp;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    public boolean receiverInProgress, isCharging, isWifiConnected, isInGeofence;

    private IntentFilter filter = new IntentFilter();

    IBinder binder = new MobilityDetectionService.LocalBinder();

    private DataManager dataManager;

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

    public MobilityDetectionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dataManager = new DataManager(this);

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

        buildNotification();
        loadTransitions();
        loadRoutes();
        checkChargingStatus();

        // requestActivityRecognitionTransitionUpdates();
        // requestAwarenessUpdates();
        // requestTrackingLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityRecognitionUpdates();
        removeAllGeofenceUpdates(getGeofencePendingIntent());
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
                handlePowerConnectionChange(intent);
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
                dataManager.writeDetectedLocation(detectedLocation);
            }
        }
    };

    private void handleGeofenceTransition(final Intent intent) {
        int geofenceTransition = intent.getIntExtra("geofenceTransition", -1);
        ArrayList<String> keys = intent.getStringArrayListExtra("keys");

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.e(TAG, "GEOFENCE_TRANSITION_DWELL");

            isInGeofence = true;
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e(TAG, "GEOFENCE_TRANSITION_EXIT");

            removeGeofenceUpdates(keys);
            isInGeofence = false;
        }

        changeConfiguration();
    }

    private void handleConnectivityChange(final Intent intent) {
        final String HANDLER_NAME = "WIFI_CONNECTION_ACTION_LOCATION_LOOPER";
        final long RADIUS = 50L;
        // todo: test
        // final int LOITERING_DELAY = 1000 * 60 * 5;
        final int LOITERING_DELAY = 1000 * 5;

        int connectionType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);

        if (connectionType == ConnectivityManager.TYPE_WIFI) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                Log.e(TAG, "CONNECTED_CONNECTIVITY_ACTION");
                isWifiConnected = true;
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    final String ssid = wifiInfo.getSSID();
                    dataManager.writeWifiConnectionTime(ssid);
                    if (dataManager.hasWifiLocation(ssid)) {
                        if (dataManager.isWifiLocationStationary(ssid)) {
                            isInGeofence = true;
                        } else {
                            Double[] location = dataManager.getWifiLocation(ssid);
                            if (location[0] != null && location[1] != null) {
                                DetectedLocation detectedLocation = new DetectedLocation();
                                detectedLocation.setLatitude(location[0]);
                                detectedLocation.setLatitude(location[1]);
                                addGeofence(detectedLocation, RADIUS, LOITERING_DELAY, "CONNECTED_CONNECTIVITY_ACTION hasWifiLocation");
                                requestGeofenceUpdates();
                            }
                        }
                    } else if (checkLocationPermission()) {
                        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                if (locationResult != null) {
                                    DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());

                                    addGeofence(detectedLocation, RADIUS, LOITERING_DELAY, "CONNECTED_CONNECTIVITY_ACTION !hasWifiLocation");
                                    requestGeofenceUpdates();

                                    dataManager.writeWifiLocation(ssid, detectedLocation);
                                    dataManager.saveJSONFile();
                                }
                                fusedLocationProviderClient.removeLocationUpdates(this);
                            }
                        }, new HandlerThread(HANDLER_NAME).getLooper());
                    }
                }
            }
        } else {
            Log.e(TAG, "DISCONNECTED_CONNECTIVITY_ACTION");
            String disconnectionTimestamp = Timestamp.generateTimestamp();
            String ssid = dataManager.getLastWifiConnectionSSID();
            if (ssid != null) {
                dataManager.removeLastWifiConnectionSSID();
                String connectionTimestamp = dataManager.getWifiConnectionTime(ssid);
                if (connectionTimestamp != null) {
                    long totalConnectionTime = Timestamp.getDifference(connectionTimestamp, disconnectionTimestamp);
                    if (totalConnectionTime >= 1000 * 60 * 60 * 2) {
                        dataManager.updateWifiConnectionCount(ssid);
                    }
                }
            }
            isWifiConnected = false;
        }

        changeConfiguration();

        Intent i = new Intent(Actions.WIFI_CONNECTION_ACTION);
        i.putExtra("isWifi", connectionType == ConnectivityManager.TYPE_WIFI);
        sendBroadcast(i, null);
    }

    private void handlePowerConnectionChange(final Intent intent) {
        final String HANDLER_NAME = "ACTION_POWER_CONNECTED_LOCATION_LOOPER";
        final long RADIUS = 50L;
        // todo: test
        // final int LOITERING_DELAY = 1000 * 60 * 5;
        final int LOITERING_DELAY = 1000 * 5;

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_POWER_CONNECTED) && checkLocationPermission()) {
            Log.e(TAG, "ACTION_POWER_CONNECTED");

            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    if (locationResult != null) {
                        DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());

                        addGeofence(detectedLocation, RADIUS, LOITERING_DELAY, "ACTION_POWER_CONNECTED");
                        requestGeofenceUpdates();

                        isCharging = true;
                        changeConfiguration();
                    }
                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }, new HandlerThread(HANDLER_NAME).getLooper());
        }

        if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Log.e(TAG, "ACTION_POWER_DISCONNECTED");
            isCharging = false;
            changeConfiguration();
        }

        Intent i = new Intent(Actions.POWER_CONNECTION_ACTION);
        i.putExtra("isCharging", isCharging);
        sendBroadcast(i, null);
    }

    private void handleActivityDetection(final Intent intent) {
        if (!receiverInProgress) {
            Log.e(TAG, Actions.ACTIVITY_DETECTED_ACTION);

            final String HANDLER_NAME = "ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER";
            final long RADIUS = 100L;
            // todo: test
            // final long LOITERING_DELAY = 1000 * 60 * 15;
            final long LOITERING_DELAY = 1000 * 5;
            long timeDifference;

            DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
            final DetectedActivities exitedActivity = dataManager.getLastActivityTransition();
            String enteredActivityString = detectedActivities.getProbableActivities().evaluateActivity(exitedActivity, detectedActivities);

            if (exitedActivity.getTimestamp() != null) {
                timeDifference = Timestamp.getDifference(exitedActivity.getTimestamp(), detectedActivities.getTimestamp());
            } else {
                timeDifference = LOITERING_DELAY;
            }

            boolean activityChanged = !enteredActivityString.isEmpty() && !exitedActivity.getProbableActivities().getActivity().equals(enteredActivityString);
            final boolean continuousActivity = timeDifference > LOITERING_DELAY && exitedActivity.getProbableActivities().getActivity().equals(enteredActivityString);
            final boolean stillActivity = exitedActivity.getProbableActivities().getActivity().equals(Activities.STILL);

            if ((activityChanged || continuousActivity) && checkLocationPermission()) {
                receiverInProgress = true;

                fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());

                        if (locationResult != null) {
                            DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());
                            detectedActivities.setDetectedLocation(detectedLocation);

                            if (continuousActivity && stillActivity) {
                                addGeofence(detectedLocation, RADIUS, (int) LOITERING_DELAY, Actions.ACTIVITY_DETECTED_ACTION + " equalActivity && stillActivity");
                                requestGeofenceUpdates();

                                endRoute();
                            }
                        }
                        dataManager.writeActivityTransition(detectedActivities);
                        dataManager.saveJSONFile();

                        Intent i = new Intent(Actions.ACTIVITY_TRANSITIONED_RECEIVER_ACTION);
                        i.putExtra(DetectedActivities.class.getSimpleName(), detectedActivities);
                        sendBroadcast(i, null);

                        fusedLocationProviderClient.removeLocationUpdates(this);
                        receiverInProgress = false;
                    }
                }, new HandlerThread(HANDLER_NAME).getLooper());
            }
        }
    }

    public void endRoute() {
        dataManager.writeRoute();

        Intent intent = new Intent(Actions.ROUTE_ENDED_ACTION);
        intent.putParcelableArrayListExtra(Route.class.getSimpleName(), dataManager.getRoutes());
        sendBroadcast(intent, null);
    }

    private void handleActivityValidation(final Intent intent) {
        Log.e(TAG, Actions.ACTIVITY_VALIDATED_ACTION);

        final String HANDLER_NAME = "ACTIVITY_VALIDATED_ACTION_LOCATION_LOOPER";

        final String validation = intent.getStringExtra("validation");
        final DetectedActivities detectedActivities = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());

        if (checkLocationPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    if (locationResult != null) {
                        DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());
                        detectedActivities.setDetectedLocation(detectedLocation);
                    }

                    dataManager.writeValidation(validation, detectedActivities);

                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }, new HandlerThread(HANDLER_NAME).getLooper());
        } else {
            dataManager.writeValidation(validation, detectedActivities);
        }
    }

    public void changeConfiguration() {
        Log.e(TAG, "isCharging: " + isCharging + ", isWifiConnected: " + isWifiConnected + ", isInGeofence: " + isInGeofence);

        if (isCharging && isWifiConnected && !isInGeofence) {
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000);
        }
        if (isCharging && !isWifiConnected && !isInGeofence) {
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000);
        }
        if (!isCharging && isWifiConnected && !isInGeofence) {
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000 * 20);
        }
        if (!isCharging && !isWifiConnected && !isInGeofence) {
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000 * 10);
        }

        if (isCharging && isWifiConnected && isInGeofence) {
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            // todo: test
            // removeActivityRecognitionUpdates();
        }
        if (isCharging && !isWifiConnected && isInGeofence) {
            removeActivityRecognitionUpdates();
        }
        if (!isCharging && isWifiConnected && isInGeofence) {
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000 * 60 * 6);
        }
        if (!isCharging && !isWifiConnected && isInGeofence) {
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(1000 * 60 * 3);
        }
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
        dataManager.saveJSONFile();
    }


    private void loadTransitions() {
        Intent transitionsLoadedIntent = new Intent(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        transitionsLoadedIntent.putParcelableArrayListExtra("activities", dataManager.getActivityTransitions());
        sendBroadcast(transitionsLoadedIntent, null);

        DetectedActivities detectedActivities = dataManager.getLastActivityTransition();
        String lastActivityTimestamp = detectedActivities.getTimestamp();
        if (lastActivityTimestamp != null) {
            if (Timestamp.getDifference(lastActivityTimestamp, Timestamp.generateTimestamp()) > 1000 * 5) {
                dataManager.writeRoute();
            }
        }
    }

    private void loadRoutes() {
        Intent intent = new Intent(Actions.ROUTES_LOADED_ACTION);
        intent.putParcelableArrayListExtra(Route.class.getSimpleName(), dataManager.getRoutes());
        sendBroadcast(intent, null);
    }

    private void checkChargingStatus() {
        BatteryManager batteryManager = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
        isCharging = batteryManager.isCharging();
        changeConfiguration();
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

    public void requestActivityRecognitionUpdates(final long INTERVAL) {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(INTERVAL, getDetectedActivityPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "Successfully requested activity updates");
                // Toast.makeText(getApplicationContext(), "Successfully requested activity updates", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Requesting activity updates failed to start");
                // Toast.makeText(getApplicationContext(), "Requesting activity updates failed to start", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeActivityRecognitionUpdates() {
        activityRecognitionClient.removeActivityUpdates(getDetectedActivityPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "Removed activity updates successfully");
                        // Toast.makeText(getApplicationContext(), "Removed activity updates successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to remove activity updates");
                        // Toast.makeText(getApplicationContext(), "Failed to remove activity updates!", Toast.LENGTH_SHORT).show();
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

    private void removeGeofenceUpdates(ArrayList<String> keys) {
        for (String key : keys) {
            for (int i = geofenceList.size() - 1; i >= 0; i--) {
                if (geofenceList.get(i).getRequestId().equals(key)) {
                    Log.e(TAG, "REMOVED GEOFENCE KEY: " + key);
                    geofenceList.remove(i);
                }
            }
        }
        Intent in = new Intent(Actions.GEOFENCE_REMOVED_ACTION);
        in.putStringArrayListExtra("geofenceKey", keys);
        sendBroadcast(in, null);
        geofencingClient.removeGeofences(keys);
    }

    private void removeAllGeofenceUpdates(PendingIntent pendingIntent) {
        for (Geofence geofence : geofenceList) {
            Log.e(TAG, "REMOVED GEOFENCE KEY: " + geofence.getRequestId());
        }

        geofenceList.clear();

        Intent in = new Intent(Actions.GEOFENCES_REMOVED_ACTION);
        sendBroadcast(in, null);
        geofencingClient.removeGeofences(pendingIntent);
    }

    private void addGeofence(DetectedLocation detectedLocation, long radius, int loiteringDelay, String caller) {
        Log.e(TAG, "addGeofence() called from " + caller);

        Log.e(TAG, "ADDED GEOFENCE KEY: " + detectedLocation.getTimestamp());

        if (geofenceList.size() > 0) {
            List<String> keys = new ArrayList<>();
            for (Geofence geofence : geofenceList) {
                keys.add(geofence.getRequestId());
            }
            geofencingClient.removeGeofences(keys);
        }

        Intent in = new Intent(Actions.GEOFENCE_ADDED_ACTION);
        in.putExtra("geofenceKey", detectedLocation.getTimestamp());
        sendBroadcast(in, null);

        geofenceList.add(new Geofence.Builder()
                .setRequestId(detectedLocation.getTimestamp())
                .setCircularRegion(detectedLocation.getLatitude(), detectedLocation.getLongitude(), radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(loiteringDelay)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)
                .setMaxWaitTime(5000);

        return locationRequest;
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

