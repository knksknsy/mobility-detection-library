package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

/**
 * Interface for methods which are overridden by the application which has included the mobility detection library
 */
public interface MobilityDetectionListener {

    /**
     * Called when a activity transition has happened.
     *
     * @param activity DetectedActivities
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities
     */
    void onTransitioned(DetectedActivities activity);

    /**
     * Called when the activity transitions are loaded.
     *
     * @param activities ArrayList of DetectedActivities objects
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities
     */
    void onTransitionsLoaded(ArrayList<DetectedActivities> activities);

    /**
     * Called when a activity has been detected.
     *
     * @param activities ArrayList of DetectedActivity object
     */
    void onActivityDetected(ArrayList<DetectedActivity> activities);

    /**
     * Called when the MobilityDetectionService has been stopped.
     *
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService
     */
    void onStopService();

    /**
     * Called when the power connection has been changed.
     *
     * @param hasPower current state of the power connection.
     */
    void onPowerConnectionChanged(boolean hasPower);

    /**
     * Called when the wifi connection has been changed.
     *
     * @param hasWifiConnection current state of the wifi connection
     */
    void onWifiConnectionChanged(boolean hasWifiConnection);

    /**
     * Called when a geofence has been added
     *
     * @param key request id of the geofence.
     */
    void onGeofenceAdded(String key);

    /**
     * Called when a geofence has been removed
     *
     * @param keys ArrayList of the request ids of the removed geofences.
     */
    void onGeofenceRemoved(ArrayList<String> keys);

    /**
     * Called when all geofences has been removed.
     */
    void onGeofencesRemoved();

    /**
     * Called when the user's route has been endend.
     *
     * @param routes all routes the user has completed
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route
     */
    void onRouteEnded(ArrayList<Route> routes);

    /**
     * Called when the user's routes has been loaded.
     *
     * @param routes all routes the user has completed
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route
     */
    void onRoutesLoaded(ArrayList<Route> routes);
}
