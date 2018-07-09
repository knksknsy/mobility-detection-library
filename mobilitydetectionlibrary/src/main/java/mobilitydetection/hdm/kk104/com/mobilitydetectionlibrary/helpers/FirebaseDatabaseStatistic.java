package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Validation;

public class FirebaseDatabaseStatistic {

    private static final String TAG = FirebaseDatabaseStatistic.class.getSimpleName();

    private Context context;

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";

    private ArrayList<DetectedActivities> detectedActivitiesArrayList;
    private ArrayList<DetectedLocation> detectedLocationArrayList;
    private ArrayList<Validation> validationArrayList;

    private FirebaseAuth auth;
    private DatabaseReference dbStatistic;
    private DatabaseReference connectedRef;

    public FirebaseDatabaseStatistic(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        dbStatistic = FirebaseDatabase.getInstance().getReference(DB_NAME);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        detectedActivitiesArrayList = new ArrayList<>();
        detectedLocationArrayList = new ArrayList<>();
        validationArrayList = new ArrayList<>();
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

    public void uploadDetectedActivity(final DetectedActivities detectedActivities) {
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    if (detectedActivitiesArrayList.size() > 0) {
                        new ActivityUploader().execute(detectedActivitiesArrayList.toArray(new DetectedActivities[detectedActivitiesArrayList.size()]));
                        detectedActivitiesArrayList = new ArrayList<>();
                    }
                    new ActivityUploader().execute(detectedActivities);
                } else {
                    detectedActivitiesArrayList.add(detectedActivities);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadDetectedLocation(final DetectedLocation detectedLocation) {
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    if (detectedLocationArrayList.size() > 0) {
                        new LocationUploader().execute(detectedLocationArrayList.toArray(new DetectedLocation[detectedLocationArrayList.size()]));
                        detectedLocationArrayList = new ArrayList<>();
                    }
                    new LocationUploader().execute(detectedLocation);
                } else {
                    detectedLocationArrayList.add(detectedLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadValidation(final String activity, final DetectedActivities detectedActivities) {
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    if (validationArrayList.size() > 0) {
                        new ValidationUploader().execute(validationArrayList.toArray(new Validation[validationArrayList.size()]));
                        validationArrayList = new ArrayList<>();
                    }
                    new ValidationUploader().execute(new Validation(activity, detectedActivities));
                } else {
                    validationArrayList.add(new Validation(activity, detectedActivities));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadValidation(final String activity, final List<DetectedActivity> activityList) {
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    if (validationArrayList.size() > 0) {
                        new ValidationUploader().execute(validationArrayList.toArray(new Validation[validationArrayList.size()]));
                        validationArrayList = new ArrayList<>();
                    }
                    new ValidationUploader().execute(new Validation(activity, activityList));
                } else {
                    validationArrayList.add(new Validation(activity, activityList));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDateShort() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(date);
    }

    public class ValidationUploader extends AsyncTask<Validation, Void, Void> {
        @Override
        protected Void doInBackground(Validation... validations) {
            for (Validation validation : validations) {
                dbStatistic.child(getDateShort()).child(VALIDATION).child(validation.getTimestamp().split(" ")[1]).setValue(validation);
            }
            return null;
        }
    }

    public class ActivityUploader extends AsyncTask<DetectedActivities, Void, Void> {
        @Override
        protected Void doInBackground(DetectedActivities... detectedActivities) {
            for (DetectedActivities activity : detectedActivities) {
                dbStatistic.child(getDateShort()).child(ACTIVITIES).child(activity.getTimestamp().split(" ")[1]).setValue(activity);
            }
            return null;
        }
    }

    public class LocationUploader extends AsyncTask<DetectedLocation, Void, Void> {
        @Override
        protected Void doInBackground(DetectedLocation... locations) {
            for (DetectedLocation location : locations) {
                dbStatistic.child(getDateShort()).child(LOCATION).child(location.getTimestamp().split(" ")[1]).setValue(location);
            }
            return null;
        }
    }

}
