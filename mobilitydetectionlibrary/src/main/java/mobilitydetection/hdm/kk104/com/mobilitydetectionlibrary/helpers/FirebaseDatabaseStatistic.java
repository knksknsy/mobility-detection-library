/*
package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Validation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.ObjectSerializer;

public class FirebaseDatabaseStatistic {

    private static final String TAG = FirebaseDatabaseStatistic.class.getSimpleName();

    private Context context;

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";
    private final String EVENTS = "events";

    private final String ACTIVITIES_LIST_KEY = "ACTIVITIES_LIST_KEY";
    private final String LOCATIONS_LIST_KEY = "LOCATIONS_LIST_KEY";
    private final String VALIDATIONS_LIST_KEY = "VALIDATIONS_LIST_KEY";
    private final String EVENTS_LIST_KEY = "EVENTS_LIST_KEY";

    private SharedPreferences preferences;

    private ArrayList<DetectedActivities> detectedActivitiesArrayList;
    private ArrayList<DetectedLocation> detectedLocationArrayList;
    private ArrayList<Validation> validationArrayList;
    private ArrayList<String> eventArrayList;

    private FirebaseAuth auth;
    private DatabaseReference dbStatistic;
    private DatabaseReference connectedRef;

    public FirebaseDatabaseStatistic(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        dbStatistic = FirebaseDatabase.getInstance().getReference(DB_NAME);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        try {
            loadSharedPreferences();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
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

    private void loadSharedPreferences() throws IOException {
        preferences = context.getSharedPreferences("CACHE_ARRAY_LISTS", Context.MODE_PRIVATE);

        detectedActivitiesArrayList = (ArrayList<DetectedActivities>) ObjectSerializer.deserialize(preferences.getString(ACTIVITIES_LIST_KEY, ObjectSerializer.serialize(new ArrayList<DetectedActivities>())));
        detectedLocationArrayList = (ArrayList<DetectedLocation>) ObjectSerializer.deserialize(preferences.getString(LOCATIONS_LIST_KEY, ObjectSerializer.serialize(new ArrayList<DetectedLocation>())));
        validationArrayList = (ArrayList<Validation>) ObjectSerializer.deserialize(preferences.getString(VALIDATIONS_LIST_KEY, ObjectSerializer.serialize(new ArrayList<Validation>())));
        eventArrayList = (ArrayList<String>) ObjectSerializer.deserialize(preferences.getString(EVENTS_LIST_KEY, ObjectSerializer.serialize(new ArrayList<String>())));

        Log.e(TAG, "validationArrayList size: " + validationArrayList.size());
        Log.e(TAG, "detectedLocationArrayList size: " + detectedLocationArrayList.size());
        Log.e(TAG, "detectedActivitiesArrayList size: " + detectedActivitiesArrayList.size());
        Log.e(TAG, "eventArrayList size: " + eventArrayList.size());
    }

    private void saveSharedPreference(String key, ArrayList arrayList) {
        Log.e(TAG, "saveSharedPreference() " + key);
        SharedPreferences.Editor editor = context.getSharedPreferences("CACHE_ARRAY_LISTS", Context.MODE_PRIVATE).edit();
        try {
            editor.putString(key, ObjectSerializer.serialize(arrayList));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        editor.apply();
    }

    public void uploadDetectedActivity(final DetectedActivities detectedActivities) {
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    new ActivityUploader().execute(detectedActivities);
                    if (detectedActivitiesArrayList.size() > 0) {
                        new ActivityUploader().execute(detectedActivitiesArrayList.toArray(new DetectedActivities[detectedActivitiesArrayList.size()]));
                        detectedActivitiesArrayList.clear();
                    }
                } else {
                    detectedActivitiesArrayList.add(detectedActivities);
                }
                saveSharedPreference(ACTIVITIES_LIST_KEY, detectedActivitiesArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadDetectedLocation(final DetectedLocation detectedLocation) {
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    new LocationUploader().execute(detectedLocation);
                    if (detectedLocationArrayList.size() > 0) {
                        new LocationUploader().execute(detectedLocationArrayList.toArray(new DetectedLocation[detectedLocationArrayList.size()]));
                        detectedLocationArrayList.clear();
                    }
                } else {
                    detectedLocationArrayList.add(detectedLocation);
                }
                saveSharedPreference(LOCATIONS_LIST_KEY, detectedLocationArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadValidation(final String activity, final DetectedActivities detectedActivities) {
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    new ValidationUploader().execute(new Validation(activity, detectedActivities));
                    Log.e(TAG, "connected: " + connected + ", size: " + validationArrayList.size());
                    if (validationArrayList.size() > 0) {
                        new ValidationUploader().execute(validationArrayList.toArray(new Validation[validationArrayList.size()]));
                        validationArrayList.clear();
                    }
                } else {
                    validationArrayList.add(new Validation(activity, detectedActivities));
                    Log.e(TAG, "connected: " + connected + ", size: " + validationArrayList.size());
                }
                saveSharedPreference(VALIDATIONS_LIST_KEY, validationArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadValidation(final String activity, final List<DetectedActivity> activityList) {
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    new ValidationUploader().execute(new Validation(activity, activityList));
                    if (validationArrayList.size() > 0) {
                        new ValidationUploader().execute(validationArrayList.toArray(new Validation[validationArrayList.size()]));
                        validationArrayList.clear();
                    }
                } else {
                    validationArrayList.add(new Validation(activity, activityList));
                }
                saveSharedPreference(VALIDATIONS_LIST_KEY, validationArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadEvent(final String event) {
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    new EventUploader().execute(event);
                    if (eventArrayList.size() > 0) {
                        new EventUploader().execute(eventArrayList.toArray(new String[eventArrayList.size()]));
                        eventArrayList.clear();
                    }
                } else {
                    eventArrayList.add(event);
                }
                saveSharedPreference(EVENTS_LIST_KEY, eventArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDateShort() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        return dateFormat.format(date);
    }

    private String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss+SSS");
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class EventUploader extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... events) {
            for (String event : events) {
                dbStatistic.child(getDateShort()).child(EVENTS).child(generateTimestamp().split(" ")[1]).setValue(event);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
*/
