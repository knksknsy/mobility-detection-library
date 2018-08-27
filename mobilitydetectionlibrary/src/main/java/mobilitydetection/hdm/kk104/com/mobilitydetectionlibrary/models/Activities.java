package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;

public class Activities {
    public static String IN_VEHICLE = "IN_VEHICLE";
    public static String ON_BICYCLE = "ON_BICYCLE";
    public static String ON_FOOT = "ON_FOOT";
    public static String STILL = "STILL";
    public static String UNKNOWN = "UNKNOWN";
    public static String TILTING = "TILTING";
    public static String WALKING = "WALKING";
    public static String RUNNING = "RUNNING";

    public static String ENTER = "ENTER";
    public static String EXIT = "EXIT";

    final public static String[] activities = new String[]{IN_VEHICLE, ON_BICYCLE, ON_FOOT, STILL, UNKNOWN, TILTING, WALKING, RUNNING};

    /**
     * Converting the activity type returned from DetectedActivity into a human readable output.
     *
     * @param type
     * @return Human readable string of activity
     */
    public static String getActivityType(int type) {
        String activity = "";
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                activity = Activities.IN_VEHICLE;
                break;
            case DetectedActivity.ON_BICYCLE:
                activity = Activities.ON_BICYCLE;
                break;
            case DetectedActivity.ON_FOOT:
                activity = Activities.ON_FOOT;
                break;
            case DetectedActivity.STILL:
                activity = Activities.STILL;
                break;
            case DetectedActivity.UNKNOWN:
                activity = Activities.UNKNOWN;
                break;
            case DetectedActivity.TILTING:
                activity = Activities.TILTING;
                break;
            case DetectedActivity.WALKING:
                activity = Activities.WALKING;
                break;
            case DetectedActivity.RUNNING:
                activity = Activities.RUNNING;
                break;
        }
        return activity;
    }

    /**
     * Converting the transition type returned from ActivityTransitionEvent into a human readable output.
     *
     * @param type
     * @return Human readable string of transition
     * @deprecated
     */
    public static String getTransitionType(int type) {
        String sType = "";
        switch (type) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                sType = Activities.ENTER;
                break;
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                sType = Activities.EXIT;
                break;
        }
        return sType;
    }
}
