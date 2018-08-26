package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Class containing route information and every activity transitions which happened on route.
 */
public class Route implements Parcelable {

    private static final String TAG = Route.class.getSimpleName();

    /**
     * Containing all activity transitions of route.
     */
    private ArrayList<DetectedActivities> activities;
    /**
     * State of the first detected activity.
     */
    private String startActivity;
    /**
     * State of the last detected activity.
     */
    private String endActivity;
    /**
     * Timestamp of the starting activity in the following pattern: yyyy-MM-ddTHH:mm:ss
     */
    private String startTime;
    /**
     * Timestamp of the last activity in the following pattern: yyyy-MM-ddTHH:mm:ss
     */
    private String endTime;
    /**
     * Location information of the starting activity,
     *
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation
     */
    private DetectedLocation startLocation;
    /**
     * Location information of the last activity.
     *
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation
     */
    private DetectedLocation endLocation;

    public Route(ArrayList<DetectedActivities> activities) {

        this.activities = activities;

        startActivity = this.activities.get(0).getProbableActivities().getActivity();
        endActivity = this.activities.get(this.activities.size() - 1).getProbableActivities().getActivity();

        startTime = this.activities.get(0).getTimestamp();
        endTime = this.activities.get(this.activities.size() - 1).getTimestamp();

        startLocation = this.activities.get(0).getDetectedLocation();
        endLocation = this.activities.get(this.activities.size() - 1).getDetectedLocation();
    }

    /**
     * Constructor for creating a Route object by reading a Parcel
     *
     * @param in
     */
    public Route(Parcel in) {
        activities = in.readArrayList(DetectedActivities.class.getClassLoader());
        startActivity = in.readString();
        endActivity = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        startLocation = in.readParcelable(DetectedLocation.class.getClassLoader());
        endLocation = in.readParcelable(DetectedLocation.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Converts the Route object to a Parcel
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(activities);
        dest.writeString(startActivity);
        dest.writeString(endActivity);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeParcelable(startLocation, flags);
        dest.writeParcelable(endLocation, flags);
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public ArrayList<DetectedActivities> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<DetectedActivities> activities) {
        this.activities = activities;
    }

    public String getStartActivity() {
        return startActivity;
    }

    public void setStartActivity(String startActivity) {
        this.startActivity = startActivity;
    }

    public String getEndActivity() {
        return endActivity;
    }

    public void setEndActivity(String endActivity) {
        this.endActivity = endActivity;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public DetectedLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(DetectedLocation startLocation) {
        this.startLocation = startLocation;
    }

    public DetectedLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(DetectedLocation endLocation) {
        this.endLocation = endLocation;
    }

    /**
     * Converts a Route object to a JSONObject.
     *
     * @return
     */
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("startTime", startTime);
            object.put("endTime", endTime);
            if (startActivity != null) {
                object.put("startActivity", activities.get(0).getProbableActivities().getActivity());
            }
            if (endActivity != null) {
                object.put("endActivity", activities.get(activities.size() - 1).getProbableActivities().getActivity());
            }
            if (startLocation != null) {
                object.put("startLocation", startLocation.toJSON());
            }
            if (endLocation != null) {
                object.put("endLocation", endLocation.toJSON());

            }
            object.put("route", new JSONArray());
            for (DetectedActivities activity : activities) {
                JSONObject activityObject = activity.toJSON();
                activityObject.remove("detectedActivities");
                object.getJSONArray("route").put(activity.toJSON());
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
