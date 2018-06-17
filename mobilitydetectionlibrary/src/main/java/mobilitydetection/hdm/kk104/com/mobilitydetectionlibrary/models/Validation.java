package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Validation {
    private String timestamp;
    private String activity;

    public Validation(String activity) {
        this.activity = activity;
        this.timestamp = generateTimestamp();
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

    private String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }
}
