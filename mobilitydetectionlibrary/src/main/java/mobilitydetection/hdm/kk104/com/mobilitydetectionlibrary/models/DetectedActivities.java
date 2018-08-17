package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.Timestamp;

public class DetectedActivities implements Parcelable {

    private static final String TAG = DetectedActivities.class.getSimpleName();

    private String timestamp;
    private ProbableActivities probableActivities;
    private DetectedLocation detectedLocation;

    public DetectedActivities() {
        this.probableActivities = new ProbableActivities();
    }

    public DetectedActivities(ArrayList<DetectedActivity> detectedActivities) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities(detectedActivities);
    }

    public DetectedActivities(List<DetectedActivity> detectedActivities) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities((ArrayList<DetectedActivity>) detectedActivities);
    }

    public DetectedActivities(ArrayList<DetectedActivity> detectedActivities, Context context, Location location) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities(detectedActivities);
        this.detectedLocation = new DetectedLocation(context, location);
    }

    public DetectedActivities(List<DetectedActivity> detectedActivities, Context context, Location location) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities((ArrayList<DetectedActivity>) detectedActivities);
        this.detectedLocation = new DetectedLocation(context, location);
    }

    public DetectedActivities(Parcel in) {
        timestamp = in.readString();
        probableActivities = in.readParcelable(getClass().getClassLoader());
        detectedLocation = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeParcelable(probableActivities, flags);
        dest.writeParcelable(detectedLocation, flags);
    }

    public static final Parcelable.Creator<DetectedActivities> CREATOR = new Parcelable.Creator<DetectedActivities>() {
        public DetectedActivities createFromParcel(Parcel in) {
            return new DetectedActivities(in);
        }

        public DetectedActivities[] newArray(int size) {
            return new DetectedActivities[size];
        }
    };

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getShortTime() {
        return Timestamp.getTime(timestamp);
    }

    public ProbableActivities getProbableActivities() {
        return probableActivities;
    }

    public void setProbableActivities(ProbableActivities probableActivities) {
        this.probableActivities = probableActivities;
    }

    public DetectedLocation getDetectedLocation() {
        return detectedLocation;
    }

    public void setDetectedLocation(DetectedLocation detectedLocation) {
        this.detectedLocation = detectedLocation;
    }

    private String generateTimestamp() {
        return Timestamp.generateTimestamp();
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("timestamp", this.timestamp);
            object.put("detectedActivities", probableActivities.toJSON());
            if (detectedLocation != null) {
                object.put("detectedLocation", detectedLocation.toJSON());
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
