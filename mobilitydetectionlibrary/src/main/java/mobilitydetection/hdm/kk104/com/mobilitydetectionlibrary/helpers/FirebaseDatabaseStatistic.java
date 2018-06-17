package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Validation;

public class FirebaseDatabaseStatistic {

    private static final String TAG = FirebaseDatabaseStatistic.class.getSimpleName();

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";

    private FirebaseAuth auth;
    private DatabaseReference dbStatistic;

    public FirebaseDatabaseStatistic() {
        auth = FirebaseAuth.getInstance();
        dbStatistic = FirebaseDatabase.getInstance().getReference(DB_NAME);
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public DatabaseReference getDbStatistic() {
        return dbStatistic;
    }

    public void setDbStatistic(DatabaseReference dbStatistic) {
        this.dbStatistic = dbStatistic;
    }

    public void uploadDetectedActivity(DetectedActivities detectedActivities) {
        dbStatistic.child(getDateShort()).child(ACTIVITIES).child(getTime()).setValue(detectedActivities);
    }

    public void uploadDetectedLocation(DetectedLocation detectedLocation) {
        dbStatistic.child(getDateShort()).child(LOCATION).child(getTime()).setValue(detectedLocation);
    }

    public void uploadValidation(String activity) {
        dbStatistic.child(getDateShort()).child(VALIDATION).child(getTime()).setValue(new Validation(activity));
    }

    private String getDateShort() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }

    private String getTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }

    private String getDateLong() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }

}
