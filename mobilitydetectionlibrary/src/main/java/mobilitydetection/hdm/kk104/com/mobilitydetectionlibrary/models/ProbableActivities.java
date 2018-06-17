package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProbableActivities implements Parcelable {

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

    public ProbableActivities(ArrayList<DetectedActivity> activities) {
        initProbableActivities(activities);
        this.activities = sortActivitiesByConfidence(activities);

        mostProbableType = convertActivityType(getMostProbableActivity().getType());
        mostProbableConfidence = getMostProbableActivity().getConfidence();
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

    @Exclude
    public DetectedActivity getMostProbableActivity() {
        return activities.get(0);
    }

    private String convertActivityType(int type) {
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

}
