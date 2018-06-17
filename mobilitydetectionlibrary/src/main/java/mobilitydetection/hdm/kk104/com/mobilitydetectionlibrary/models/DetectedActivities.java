package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DetectedActivities implements Parcelable {
    private String timestamp;
    private ProbableActivities probableActivities;

    public DetectedActivities(ArrayList<DetectedActivity> detectedActivities) {
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

    public ProbableActivities getDetectedActivities() {
        return probableActivities;
    }

    public void setDetectedActivities(ProbableActivities probableActivities) {
        this.probableActivities = probableActivities;
    }

    private String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }
}
