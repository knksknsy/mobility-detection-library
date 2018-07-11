package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import com.google.android.gms.location.DetectedActivity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Validation implements Serializable {
    private String timestamp;
    private String activity;
    private DetectedActivities detectedActivities;

    public Validation(String activity) {
        this.activity = activity;
        this.timestamp = generateTimestamp();
    }

    public Validation(String activity, DetectedActivities detectedActivities) {
        this.activity = activity;
        this.timestamp = detectedActivities.getTimestamp();
        this.detectedActivities = detectedActivities;
    }

    public Validation(String activity, List<DetectedActivity> activityList) {
        this.activity = activity;
        this.detectedActivities = new DetectedActivities(activityList);
        this.timestamp = this.detectedActivities.getTimestamp();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public DetectedActivities getDetectedActivities() {
        return detectedActivities;
    }

    public void setDetectedActivities(DetectedActivities detectedActivities) {
        this.detectedActivities = detectedActivities;
    }

    private String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }
}
