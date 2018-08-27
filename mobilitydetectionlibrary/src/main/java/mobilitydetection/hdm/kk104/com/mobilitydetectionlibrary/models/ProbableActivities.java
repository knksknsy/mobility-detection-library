package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.DetectedActivitiesEvaluation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.Timestamp;

/**
 * Containing each activity's probability
 */
public class ProbableActivities implements Parcelable {

    private static final String TAG = ProbableActivities.class.getSimpleName();

    public int IN_VEHICLE;
    public int ON_BICYCLE;
    public int ON_FOOT;
    public int STILL;
    public int UNKNOWN;
    public int TILTING;
    public int WALKING;
    public int RUNNING;

    /**
     * Most probable activity type according to DetectedActivity
     */
    public String mostProbableType;
    /**
     * Most probable activity confidence according to DetectedActivity
     */
    public int mostProbableConfidence;

    /**
     * Containing all activities and their probabilities
     */
    private ArrayList<DetectedActivity> activities;

    /**
     * The estimated activity by DetectedActivitiesEvaluation
     *
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.DetectedActivitiesEvaluation
     */
    private String activity = new String();

    public ProbableActivities() {

    }

    public ProbableActivities(ArrayList<DetectedActivity> activities) {
        initProbableActivities(activities);
        this.activities = sortActivitiesByConfidence(activities);

        mostProbableType = Activities.getActivityType(getMostProbableActivity().getType());
        mostProbableConfidence = getMostProbableActivity().getConfidence();
    }

    /**
     * Constructor for creating a ProbableActivities object by reading a Parcel
     *
     * @param in
     */
    public ProbableActivities(Parcel in) {
        IN_VEHICLE = in.readInt();
        ON_BICYCLE = in.readInt();
        ON_FOOT = in.readInt();
        STILL = in.readInt();
        UNKNOWN = in.readInt();
        TILTING = in.readInt();
        WALKING = in.readInt();
        RUNNING = in.readInt();
        activities = in.readArrayList(DetectedActivity.class.getClassLoader());
        mostProbableType = in.readString();
        mostProbableConfidence = in.readInt();
        activity = in.readString();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Converts the ProbableActivities object to a Parcel
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(IN_VEHICLE);
        dest.writeInt(ON_BICYCLE);
        dest.writeInt(ON_FOOT);
        dest.writeInt(STILL);
        dest.writeInt(UNKNOWN);
        dest.writeInt(TILTING);
        dest.writeInt(WALKING);
        dest.writeInt(RUNNING);
        dest.writeList(activities);
        dest.writeString(mostProbableType);
        dest.writeInt(mostProbableConfidence);
        dest.writeString(activity);
    }

    public static final Parcelable.Creator<ProbableActivities> CREATOR = new Parcelable.Creator<ProbableActivities>() {
        public ProbableActivities createFromParcel(Parcel in) {
            return new ProbableActivities(in);
        }

        public ProbableActivities[] newArray(int size) {
            return new ProbableActivities[size];
        }
    };

    public ArrayList<DetectedActivity> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<DetectedActivity> activities) {
        this.activities = activities;
    }

    private DetectedActivity getMostProbableActivity() {
        return activities.get(0);
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * Initializes activity attributes from an ArrayList of DetectedActivity objects.
     * The following activities are available: IN_VEHICLE, ON_BICYCLE, ON_FOOT, STILL, UNKNOWN, TILTING, WALKING, RUNNING
     *
     * @param activities
     */
    private void initProbableActivities(final ArrayList<DetectedActivity> activities) {
        for (DetectedActivity activity : activities) {
            int type = activity.getType();
            int confidence = activity.getConfidence();

            switch (type) {
                case DetectedActivity.IN_VEHICLE: {
                    IN_VEHICLE = confidence;
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    ON_BICYCLE = confidence;
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    ON_FOOT = confidence;
                    break;
                }
                case DetectedActivity.STILL: {
                    STILL = confidence;
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    UNKNOWN = confidence;
                    break;
                }
                case DetectedActivity.TILTING: {
                    TILTING = confidence;
                    break;
                }
                case DetectedActivity.WALKING: {
                    WALKING = confidence;
                    break;
                }
                case DetectedActivity.RUNNING: {
                    RUNNING = confidence;
                    break;
                }
            }
        }
    }

    private ArrayList<DetectedActivity> sortActivitiesByConfidence(ArrayList<DetectedActivity> activities) {
        Collections.sort(activities, new Comparator<DetectedActivity>() {
            @Override
            public int compare(DetectedActivity o1, DetectedActivity o2) {
                return Integer.compare(o2.getConfidence(), o1.getConfidence());
            }
        });

        return activities;
    }

    /**
     * Evaluates the activity which is detected.
     *
     * @param exitedActivity  DetectedActivities object of the last detected activity.
     * @param enteredActivity DetectedActivities object of the currently detected activity.
     * @return Human readable String containing the evaluated activity.
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities
     */
    public String evaluateActivity(final DetectedActivities exitedActivity, final DetectedActivities enteredActivity) {
        String activity = new String();
        /*boolean exitedActivityStill = false;*/
        boolean exitedActivityVehicle = false;

        if (exitedActivity.getProbableActivities().getActivity().equals(Activities.STILL) || exitedActivity.getProbableActivities().getActivity().equals(Activities.IN_VEHICLE)) {
            long diff = Timestamp.getDifference(exitedActivity.getTimestamp(), enteredActivity.getTimestamp());
            long interval = 1000 * 60;

            if (diff <= interval) {
                /*if (exitedActivity.getProbableActivities().getActivity().equals(Activities.STILL) && enteredActivity.getProbableActivities().STILL >= 70) {
                    exitedActivityStill = true;
                }*/
                if (exitedActivity.getProbableActivities().getActivity().equals(Activities.IN_VEHICLE)) {
                    exitedActivityVehicle = true;
                }
            }
        }

        if (WALKING >= 80 || ON_FOOT >= 80) {
            activity = Activities.WALKING;
        }

        if (STILL >= 80) {
            activity = Activities.STILL;
        } else if (DetectedActivitiesEvaluation.Deceleration.checkState(enteredActivity.getProbableActivities())) {
            activity = Activities.STILL;
        }

        /*if (exitedActivityStill) {
            final ProbableActivities probableActivities = enteredActivity.getProbableActivities();
            probableActivities.STILL = probableActivities.STILL - 15;
            if (DetectedActivitiesEvaluation.Deceleration.checkState(probableActivities)) {
                activity = Activities.STILL;
            }
        }*/
        if (exitedActivityVehicle && enteredActivity.getProbableActivities().STILL >= 60) {
            activity = Activities.STILL;
        }

        if (IN_VEHICLE >= 69) {
            activity = Activities.IN_VEHICLE;
        } else if (DetectedActivitiesEvaluation.InVehicleMotion.checkState(enteredActivity.getProbableActivities())) {
            activity = Activities.IN_VEHICLE;
        } else if (DetectedActivitiesEvaluation.Acceleration.checkState(enteredActivity.getProbableActivities())) {
            activity = Activities.IN_VEHICLE;
        }

        if (UNKNOWN >= 80) {
            activity = Activities.UNKNOWN;
        }
        if (RUNNING >= 80) {
            activity = Activities.RUNNING;
        }
        if (ON_BICYCLE >= 80) {
            activity = Activities.ON_BICYCLE;
        }

        setActivity(activity);

        return activity;
    }

    /**
     * Converts a ProbableActivities object to a JSONObject.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("IN_VEHICLE", IN_VEHICLE);
            object.put("ON_BICYCLE", ON_BICYCLE);
            object.put("ON_FOOT", ON_FOOT);
            object.put("STILL", STILL);
            object.put("UNKNOWN", UNKNOWN);
            object.put("TILTING", TILTING);
            object.put("WALKING", WALKING);
            object.put("RUNNING", RUNNING);

            object.put("mostProbableType", mostProbableType);
            object.put("mostProbableConfidence", mostProbableConfidence);

            if (!activity.isEmpty()) {
                object.put("activity", activity);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return object;
    }

}
