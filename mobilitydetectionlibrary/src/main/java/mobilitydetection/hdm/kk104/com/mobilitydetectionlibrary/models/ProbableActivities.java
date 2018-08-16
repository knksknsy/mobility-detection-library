package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.DetectedActivitiesEvaluation;

public class ProbableActivities implements Parcelable, Serializable {

    private static final String TAG = ProbableActivities.class.getSimpleName();

    public int IN_VEHICLE;
    public int ON_BICYCLE;
    public int ON_FOOT;
    public int STILL;
    public int UNKNOWN;
    public int TILTING;
    public int WALKING;
    public int RUNNING;

    public String mostProbableType;
    public int mostProbableConfidence;

    private ArrayList<DetectedActivity> activities;
    private List<DetectedActivity> activityList;

    private String activity;

    public ProbableActivities() {

    }

    public ProbableActivities(ArrayList<DetectedActivity> activities) {
        initProbableActivities(activities);
        this.activities = sortActivitiesByConfidence(activities);

        mostProbableType = Activities.getActivityType(getMostProbableActivity().getType());
        mostProbableConfidence = getMostProbableActivity().getConfidence();

        activity = evaluateActivity();
    }

    public ProbableActivities(List<DetectedActivity> activities) {
        initProbableActivities(activities);
        this.activityList = sortActivitiesByConfidence(activities);

        mostProbableType = Activities.getActivityType(getMostProbableActivity().getType());
        mostProbableConfidence = getMostProbableActivity().getConfidence();

        activity = evaluateActivity();
    }

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

    public void setActivityList(List<DetectedActivity> activityList) {
        this.activityList = activityList;
    }

    public List<DetectedActivity> getActivityList() {
        return activityList;
    }

    public void setActivities(ArrayList<DetectedActivity> activities) {
        this.activities = activities;
    }

    public DetectedActivity getMostProbableActivity() {
        return activities != null ? activities.get(0) : activityList.get(0);
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

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

    private void initProbableActivities(final List<DetectedActivity> activities) {
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

    private List<DetectedActivity> sortActivitiesByConfidence(List<DetectedActivity> activities) {
        Collections.sort(activities, new Comparator<DetectedActivity>() {
            @Override
            public int compare(DetectedActivity o1, DetectedActivity o2) {
                return Integer.compare(o2.getConfidence(), o1.getConfidence());
            }
        });

        return activities;
    }

    /*if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average - Acceleration.InVehicle.std_deviation / 2
            && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average + Acceleration.InVehicle.std_deviation / 2) {

    } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average - Acceleration.InVehicle.mean_deviation / 2
            && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average + Acceleration.InVehicle.mean_deviation / 2) {

    } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.median - Acceleration.InVehicle.median_deviation / 2
            && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.median + Acceleration.InVehicle.median_deviation / 2) {

    } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.quartile_distance - Acceleration.InVehicle.semi_quartile_distance / 2
            && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.quartile_distance + Acceleration.InVehicle.semi_quartile_distance / 2) {

    }*/

    public String evaluateActivity() {
        String activity = new String();

        if (ON_FOOT >= 80) {
            activity = Activities.ON_FOOT;
        }
        if (WALKING >= 80) {
            activity = Activities.WALKING;
        }

        if (STILL >= 90) {
            activity = Activities.STILL;
        } else if (DetectedActivitiesEvaluation.Deceleration.checkState(IN_VEHICLE, STILL)) {
            activity = Activities.STILL;
        }

        if (IN_VEHICLE >= 80) {
            activity = Activities.IN_VEHICLE;
        } else if (DetectedActivitiesEvaluation.InVehicleMotion.checkState(IN_VEHICLE, STILL)) {
            activity = Activities.IN_VEHICLE;
        } else if (DetectedActivitiesEvaluation.Acceleration.checkState(IN_VEHICLE, STILL)) {
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

        return activity;
    }

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
