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
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.MobilityDetectionConstants;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.DataManager;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.Timestamp;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

/**
 * Service responsible for detecting mobility status and optimizing battery usage
 */
public class MobilityDetectionService extends Service {

    private static final String TAG = MobilityDetectionService.class.getSimpleName();

    private boolean activityDetectionInProgress;
    private boolean isCharging;
    private boolean isWifiConnected;
    private boolean isInGeofence;

    private IntentFilter filter = new IntentFilter();

    private IBinder binder = new MobilityDetectionService.LocalBinder();

    private DataManager dataManager;

    private ActivityRecognitionClient activityRecognitionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofencingClient geofencingClient;

    private List<Geofence> geofenceList;

    private PendingIntent detectedActivityPendingIntent;
    private PendingIntent geofencingPendingIntent;

    public long interval = MobilityDetectionConstants.INTERVAL;
    public long fastInterval = MobilityDetectionConstants.FAST_INTERVAL;
    public long mediumInterval = MobilityDetectionConstants.MEDIUM_INTERVAL;
    public long slowInterval = MobilityDetectionConstants.SLOW_INTERVAL;
    public int loiteringDelayWifi = MobilityDetectionConstants.LOITERING_DELAY_WIFI;
    public int loiteringDelayStationaryWifi = MobilityDetectionConstants.LOITERING_DELAY_STATIONARY_WIFI;
    public int loiteringDelayPower = MobilityDetectionConstants.LOITERING_DELAY_POWER;
    public int loiteringDelayActivity = MobilityDetectionConstants.LOITERING_DELAY_ACTIVITY;

    public long radiusPower = MobilityDetectionConstants.RADIUS_POWER;
    public long radiusWifi = MobilityDetectionConstants.RADIUS_WIFI;
    public long radiusActivity = MobilityDetectionConstants.RADIUS_ACTIVITY;

    // private LocationRequest locationRequestTracking;
    // private FenceClient fenceClient;
    // private PendingIntent locationPendingIntent;
    // private PendingIntent transitionPendingIntent;
    // private PendingIntent fencePendingIntent;

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
        removeGeofenceUpdates();
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

    /**
     * Binder for clients to interact with the service
     */
    public class LocalBinder extends Binder {
        /**
         * Getting the service instance
         *
         * @return MobilityDetectionService
         */
        public MobilityDetectionService getServiceInstance() {
            return MobilityDetectionService.this;
        }
    }

    /**
     * BroadcastReceiver defining the procedures for detecting activites and optimizing battery usage. The procedures are triggered after the PendingIntents have sent a broadcast
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTIVITY_DETECTED_ACTION)) {
                handleActivityDetection(intent);
            }
            if (action.equals(Actions.GEOFENCE_TRANSITION_ACTION)) {
                handleGeofenceTransition(intent);
            }
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                handleConnectivityChange(intent);
            }
            if (action.equals(Intent.ACTION_POWER_CONNECTED) || action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                handlePowerConnectionChange(intent);
            }
            /*if (action.equals(Actions.ACTIVITY_VALIDATED_ACTION)) {
                handleActivityValidation(intent);
            }
            if (action.equals(Actions.LOCATION_ACTION)) {
                Log.e(TAG, Actions.LOCATION_ACTION);
                DetectedLocation detectedLocation = intent.getParcelableExtra(DetectedLocation.class.getSimpleName());
                dataManager.writeDetectedLocation(detectedLocation);
            }*/
        }
    };

    /**
     * Handling GeofencingEvents
     *
     * @param intent passed from GeofenceIntentService
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.GeofenceIntentService
     */
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

    /**
     * Handling wifi connection changes. Checks if user uses a wifi hotspot or a stationary wifi network. Changes configuration of mobility detection accordingly.
     *
     * @param intent passed from ConnectivityManager broadcast
     */
    private void handleConnectivityChange(final Intent intent) {
        final String HANDLER_NAME = "WIFI_CONNECTION_ACTION_LOCATION_LOOPER";

        int connectionType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);

        // Connecting to a wifi network
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
                    // For calculating total connection time
                    dataManager.writeWifiConnectionTime(ssid);

                    // Check if the wifi connection is known to the device
                    if (dataManager.hasWifiLocation(ssid)) {

                        if (dataManager.isWifiLocationStationary(ssid)) {
                            isInGeofence = true;

                            // Adding activity STILL in wifi's location
                            DetectedActivities exitedActivity = dataManager.getLastActivityTransition();
                            if (exitedActivity.getTimestamp() != null) {
                                if (!exitedActivity.getProbableActivities().getActivity().equals(Activities.STILL)) {
                                    DetectedLocation detectedLocation = detectedLocationFromSSID(ssid);
                                    if (detectedLocation != null) {
                                        DetectedActivities enteredActivity = new DetectedActivities();
                                        enteredActivity.setTimestamp(Timestamp.generateTimestamp());
                                        enteredActivity.getProbableActivities().setActivity(Activities.STILL);
                                        enteredActivity.setDetectedLocation(detectedLocation);

                                        dataManager.writeActivityTransition(enteredActivity);
                                    }
                                }
                            }

                        } else {
                            // Creating a geofence for the possibility of a mobile hotspot
                            DetectedLocation detectedLocation = detectedLocationFromSSID(ssid);
                            if (detectedLocation != null) {
                                addGeofence(detectedLocation, radiusWifi, loiteringDelayWifi, "CONNECTED_CONNECTIVITY_ACTION hasWifiLocation");
                                requestGeofenceUpdates();
                            }
                        }

                    } else if (checkLocationPermission()) {
                        // First connection with the wifi network
                        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                if (locationResult != null) {
                                    DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());

                                    addGeofence(detectedLocation, radiusWifi, loiteringDelayWifi, "CONNECTED_CONNECTIVITY_ACTION !hasWifiLocation");
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
            // Disconnecting from a wifi network
        } else {
            Log.e(TAG, "DISCONNECTED_CONNECTIVITY_ACTION");
            String disconnectionTimestamp = Timestamp.generateTimestamp();
            String ssid = dataManager.getLastWifiConnectionSSID();

            if (ssid != null) {
                dataManager.removeLastWifiConnectionSSID();
                String connectionTimestamp = dataManager.getWifiConnectionTime(ssid);
                if (connectionTimestamp != null) {
                    // Calculate total wifi connection time
                    long totalConnectionTime = Timestamp.getDifference(connectionTimestamp, disconnectionTimestamp);
                    if (totalConnectionTime >= loiteringDelayStationaryWifi) {
                        // Update wifi: network is stationary and is frequently used
                        dataManager.updateWifiConnectionCount(ssid);
                    }
                }
            }
            isWifiConnected = false;
        }

        changeConfiguration();

        // Notify MobilityDetection class for connection change
        Intent i = new Intent(Actions.WIFI_CONNECTION_ACTION);
        i.putExtra("isWifi", connectionType == ConnectivityManager.TYPE_WIFI);
        sendBroadcast(i, null);
    }

    private DetectedLocation detectedLocationFromSSID(String ssid) {
        Double[] location = dataManager.getWifiLocation(ssid);

        DetectedLocation detectedLocation = new DetectedLocation();

        if (location[0] != null && location[1] != null) {
            detectedLocation.setLatitude(location[0]);
            detectedLocation.setLatitude(location[1]);
            return detectedLocation;
        }
        return null;
    }

    /**
     * Handling power connection changes. Creates a new geofence to check if user uses a power bank or a socket for charging his mobile device. Changes configuration of mobility detection accordingly.
     *
     * @param intent passed from BatteryManager broadcast
     */
    private void handlePowerConnectionChange(final Intent intent) {
        final String HANDLER_NAME = "ACTION_POWER_CONNECTED_LOCATION_LOOPER";

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_POWER_CONNECTED) && checkLocationPermission()) {
            Log.e(TAG, "ACTION_POWER_CONNECTED");

            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    if (locationResult != null) {
                        DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());

                        addGeofence(detectedLocation, radiusPower, loiteringDelayPower, "ACTION_POWER_CONNECTED");
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

    /**
     * Handling detected activities and their evaluation, deciding whether the user's route has ended or continues. Changes the configuration of the ActivityRecognitionClient for battery usage.
     *
     * @param intent passed from DetectedActivitiesIntentService
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.DetectedActivitiesIntentService
     */
    private void handleActivityDetection(final Intent intent) {
        if (!activityDetectionInProgress) {
            Log.e(TAG, Actions.ACTIVITY_DETECTED_ACTION);

            final String HANDLER_NAME = "ACTIVITY_DETECTED_ACTION_LOCATION_LOOPER";
            long timeDifference;

            // Attributes for comparing exited and newly detected activity
            final DetectedActivities enteredActivity = intent.getParcelableExtra(DetectedActivities.class.getSimpleName());
            final DetectedActivities exitedActivity = dataManager.getLastActivityTransition();
            String enteredActivityString = enteredActivity.getProbableActivities().evaluateActivity(exitedActivity, enteredActivity);

            // Calculating the time difference between the exited and newly detected activity
            if (exitedActivity.getTimestamp() != null) {
                timeDifference = Timestamp.getDifference(exitedActivity.getTimestamp(), enteredActivity.getTimestamp());
            } else {
                timeDifference = loiteringDelayActivity;
            }

            boolean activityChanged = !enteredActivityString.isEmpty() && !exitedActivity.getProbableActivities().getActivity().equals(enteredActivityString);
            // The user performs the same activity for the amount of <loiteringDelayActivity>
            final boolean continuousActivity = timeDifference > loiteringDelayActivity && exitedActivity.getProbableActivities().getActivity().equals(enteredActivityString);
            final boolean stillActivity = exitedActivity.getProbableActivities().getActivity().equals(Activities.STILL);

            if ((activityChanged || continuousActivity) && checkLocationPermission()) {
                activityDetectionInProgress = true;

                fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        if (locationResult != null) {
                            DetectedLocation detectedLocation = new DetectedLocation(getApplicationContext(), locationResult.getLastLocation());
                            enteredActivity.setDetectedLocation(detectedLocation);

                            // The user hasn't moved since the time <loiteringDelayActivity>
                            if (continuousActivity && stillActivity) {
                                addGeofence(detectedLocation, radiusActivity, loiteringDelayActivity, Actions.ACTIVITY_DETECTED_ACTION + " equalActivity && stillActivity");
                                requestGeofenceUpdates();

                                endRoute();
                            }
                        }
                        dataManager.writeActivityTransition(enteredActivity);
                        dataManager.saveJSONFile();

                        // Notify MobilityDetection class for activity transition
                        Intent i = new Intent(Actions.ACTIVITY_TRANSITIONED_ACTION);
                        i.putExtra(DetectedActivities.class.getSimpleName(), enteredActivity);
                        sendBroadcast(i, null);

                        fusedLocationProviderClient.removeLocationUpdates(this);
                        activityDetectionInProgress = false;
                    }
                }, new HandlerThread(HANDLER_NAME).getLooper());
            }
        }
    }

    /**
     * Changes the mobility detection configuration for ideal battery consumption.
     */
    public void changeConfiguration() {
        Log.e(TAG, "isCharging: " + isCharging + ", isWifiConnected: " + isWifiConnected + ", isInGeofence: " + isInGeofence);

        boolean charging = isCharging;
        boolean wifi = isWifiConnected;
        boolean geofence = isInGeofence;

        if (charging && wifi && !geofence) { // power bank + mobile hotspot
            removeActivityRecognitionUpdates();
            if (!isStationaryWifi()) {
                requestActivityRecognitionUpdates(fastInterval);
            }
        }
        if (charging && !wifi && !geofence) { // power bank
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(fastInterval);
        }
        if (!charging && wifi && !geofence) { // mobile hotspot
            removeActivityRecognitionUpdates();
            if (!isStationaryWifi()) {
                requestActivityRecognitionUpdates(interval);
            }
        }
        if (!charging && !wifi && !geofence) { // default
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(interval);
        }

        if (charging && wifi && geofence) { // fully stationary
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            removeActivityRecognitionUpdates();
            // todo: test
            // removeActivityRecognitionUpdates();
        }
        if (charging && !wifi && geofence) { // charging stationary
            removeActivityRecognitionUpdates();
        }
        if (!charging && wifi && geofence) { // wifi stationary
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            removeActivityRecognitionUpdates();
            if (!isStationaryWifi()) {
                requestActivityRecognitionUpdates(slowInterval);
            }
        }
        if (!charging && !wifi && geofence) { // default stationary
            removeAllGeofenceUpdates(getGeofencePendingIntent());
            isInGeofence = false;
            removeActivityRecognitionUpdates();
            requestActivityRecognitionUpdates(mediumInterval);
        }
    }

    /**
     * Completing the route of an user and notifies the application which is running the mobility detection library
     */
    private void endRoute() {
        dataManager.writeRoute();

        Intent intent = new Intent(Actions.ROUTE_ENDED_ACTION);
        intent.putParcelableArrayListExtra(Route.class.getSimpleName(), dataManager.getRoutes());
        sendBroadcast(intent, null);
    }

    /**
     * Checking if the current or lastly connected network is or was stationary.
     *
     * @return boolean
     */
    private boolean isStationaryWifi() {
        String ssid = dataManager.getLastWifiConnectionSSID();
        if (ssid != null) {
            return dataManager.isWifiLocationStationary(ssid);
        } else {
            return false;
        }
    }

    /**
     * Checking if location permissions are granted.
     *
     * @return boolean
     */
    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * Loads activity transitions and notifies the application running the library.
     */
    private void loadTransitions() {
        Intent transitionsLoadedIntent = new Intent(Actions.ACTIVITY_TRANSITIONS_LOADED_ACTION);
        transitionsLoadedIntent.putParcelableArrayListExtra("activities", dataManager.getActivityTransitions());
        sendBroadcast(transitionsLoadedIntent, null);

        // End user's route if no activity has been recognized for <loiteringDelayActivity> milliseconds
        DetectedActivities detectedActivities = dataManager.getLastActivityTransition();
        String lastActivityTimestamp = detectedActivities.getTimestamp();
        if (lastActivityTimestamp != null) {
            if (Timestamp.getDifference(lastActivityTimestamp, Timestamp.generateTimestamp()) > loiteringDelayActivity) {
                dataManager.writeRoute();
            }
        }
    }

    /**
     * Loads all user routes and notifies the application running the library.
     */
    private void loadRoutes() {
        Intent intent = new Intent(Actions.ROUTES_LOADED_ACTION);
        intent.putParcelableArrayListExtra(Route.class.getSimpleName(), dataManager.getRoutes());
        sendBroadcast(intent, null);
    }

    /**
     * Checks if the device is currently being charged or not and changes the configuration accordingly.
     */
    private void checkChargingStatus() {
        BatteryManager batteryManager = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
        isCharging = batteryManager.isCharging();
        changeConfiguration();
    }

    /**
     * Builds the notification for the foreground service.
     */
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

    /**
     * Adds IntentFilter actions for registered BroadcastReceiver.
     */
    private void addFilterActions() {
        if (filter != null) {
            filter.addAction(Actions.ACTIVITY_DETECTED_ACTION);
            filter.addAction(Actions.ACTIVITY_LIST_ACTION);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(Actions.GEOFENCE_TRANSITION_ACTION);
            /*filter.addAction(Actions.LOCATION_ACTION);
            filter.addAction(Actions.ACTIVITY_VALIDATED_ACTION);*/
        }
    }

    /**
     * Starts requesting updates for activity recognition.
     *
     * @param INTERVAL milliseconds of update interval
     */
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

    /**
     * Removes activity recognition updates.
     */
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

    /**
     * Start requesting geofence updates.
     */
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

    /**
     * Removes geofence updates.
     */
    private void removeGeofenceUpdates() {
        ArrayList<String> keys = new ArrayList<>();
        for (Geofence geofence : geofenceList) {
            keys.add(geofence.getRequestId());
        }
        geofenceList.clear();

        Intent in = new Intent(Actions.GEOFENCE_REMOVED_ACTION);
        in.putStringArrayListExtra("geofenceKey", keys);
        sendBroadcast(in, null);

        if (keys.size() > 0) {
            geofencingClient.removeGeofences(keys);
        }
    }

    /**
     * Removes geofence updates for provided geofence request ids.
     *
     * @param keys geofence request ids
     */
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

    /**
     * Removes all geofence updates.
     *
     * @param pendingIntent
     */
    private void removeAllGeofenceUpdates(PendingIntent pendingIntent) {
        for (Geofence geofence : geofenceList) {
            Log.e(TAG, "REMOVED GEOFENCE KEY: " + geofence.getRequestId());
        }
        geofenceList.clear();

        Intent in = new Intent(Actions.GEOFENCES_REMOVED_ACTION);
        sendBroadcast(in, null);

        geofencingClient.removeGeofences(pendingIntent);
    }

    /**
     * Adds a geofence for the transition type GEOFENCE_TRANSITION_DWELL or GEOFENCE_TRANSITION_EXIT
     *
     * @param detectedLocation DetectedLocation object containing the coordinates of the geofence
     * @param RADIUS           radius of the geofence
     * @param LOITERING_DELAY  time in milliseconds the user has to dwell in the geofence for triggering the transition type GEOFENCE_TRANSITION_DWELL
     * @param caller           for logging debug information
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation
     */
    private void addGeofence(DetectedLocation detectedLocation, final long RADIUS, final int LOITERING_DELAY, String caller) {
        Log.e(TAG, "addGeofence() called from " + caller);

        Log.e(TAG, "ADDED GEOFENCE KEY: " + detectedLocation.getTimestamp());

        removeGeofenceUpdates();

        Intent in = new Intent(Actions.GEOFENCE_ADDED_ACTION);
        in.putExtra("geofenceKey", detectedLocation.getTimestamp());
        sendBroadcast(in, null);

        geofenceList.add(new Geofence.Builder()
                .setRequestId(detectedLocation.getTimestamp())
                .setCircularRegion(detectedLocation.getLatitude(), detectedLocation.getLongitude(), RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(LOITERING_DELAY)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    /**
     * Getting geofencing request. Initial triggers are set to INITIAL_TRIGGER_DWELL and INITIAL_TRIGGER_EXIT
     *
     * @return GeofencingRequest
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    /**
     * Getting location request.
     *
     * @return LocationRequest
     */
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(fastInterval)
                .setFastestInterval(fastInterval)
                .setExpirationDuration(5000);
        //.setMaxWaitTime(5000);

        return locationRequest;
    }

    /**
     * Getting the PendingIntent for starting DetectedActivitiesIntentService. The class is responsible for retrieving activity information.
     *
     * @return PendingIntent
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.DetectedActivitiesIntentService
     */
    private PendingIntent getDetectedActivityPendingIntent() {
        if (detectedActivityPendingIntent != null) {
            return detectedActivityPendingIntent;
        }
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        detectedActivityPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return detectedActivityPendingIntent;
    }

    /**
     * Getting the PendingIntent for starting GeofenceIntentService. The class is responsible for checking the transition types of the geofence.
     *
     * @return PendingIntent
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.GeofenceIntentService
     */
    private PendingIntent getGeofencePendingIntent() {
        if (geofencingPendingIntent != null) {
            return geofencingPendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        geofencingPendingIntent = PendingIntent.getService(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencingPendingIntent;
    }

    /**
     * Write JSON file to storage.
     */
    public void saveData() {
        Log.e(TAG, Actions.SAVE_DATA_ACTION);
        dataManager.saveJSONFile();
    }

    /*private void handleActivityValidation(final Intent intent) {
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
    }*/

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

    /*private PendingIntent getLocationPendingIntent() {
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

