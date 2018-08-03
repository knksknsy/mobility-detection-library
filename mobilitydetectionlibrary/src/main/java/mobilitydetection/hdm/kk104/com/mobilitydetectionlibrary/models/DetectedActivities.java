package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.Timestamp;

public class DetectedActivities implements Parcelable, Serializable {

    private static final String TAG = DetectedActivities.class.getSimpleName();

    private String timestamp;
    private ProbableActivities probableActivities;

    public DetectedActivities(ArrayList<DetectedActivity> detectedActivities) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities(detectedActivities);
    }

    public DetectedActivities(List<DetectedActivity> detectedActivities) {
        this.timestamp = generateTimestamp();
        this.probableActivities = new ProbableActivities(detectedActivities);
    }

    public DetectedActivities(Parcel in) {
        timestamp = in.readString();
        probableActivities = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeParcelable(probableActivities, flags);
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

    public ProbableActivities getDetectedActivities() {
        return probableActivities;
    }

    public void setDetectedActivities(ProbableActivities probableActivities) {
        this.probableActivities = probableActivities;
    }

    private String generateTimestamp() {
        return Timestamp.generateTimestamp();
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("timestamp", this.timestamp);
            object.put("detectedActivities", probableActivities.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
