package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedAddress;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities;

public class JSONManager {

    private static final String TAG = JSONManager.class.getSimpleName();

    private Context context;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";
    private final String TRANSITIONS = "transitions";

    private JSONObject root;

    private String shortDate;

    public JSONManager(Context context) {
        this.context = context;
        shortDate = getDateShort();
        readJSONFile();
    }

    public void writeJSONFile() {
        try {
            fileOutputStream = context.openFileOutput(DB_NAME, Context.MODE_PRIVATE);
            fileOutputStream.write(root.toString().getBytes());
            fileOutputStream.close();
            Log.e(TAG, "SAVED");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

        }
    }

    public void readJSONFile() {
        try {
            fileInputStream = context.openFileInput(DB_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            if (stringBuilder.toString().length() > 0) {
                root = new JSONObject(stringBuilder.toString());
                if (!root.getJSONObject(DB_NAME).has(shortDate)) {
                    JSONObject data = new JSONObject();
                    JSONObject activities = new JSONObject();
                    JSONObject location = new JSONObject();
                    JSONObject validation = new JSONObject();
                    JSONArray transitions = new JSONArray();

                    data.put(ACTIVITIES, activities);
                    data.put(LOCATION, location);
                    data.put(VALIDATION, validation);
                    data.put(TRANSITIONS, transitions);
                    root.getJSONObject(DB_NAME).put(shortDate, data);
                }
            } else {
                root = null;
            }
        } catch (IOException e) {
            initializeJSON();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void initializeJSON() {
        try {
            if (root == null) {
                root = new JSONObject();
            }
            JSONObject day = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject activities = new JSONObject();
            JSONObject location = new JSONObject();
            JSONObject validation = new JSONObject();
            JSONArray transitions = new JSONArray();

            data.put(ACTIVITIES, activities);
            data.put(LOCATION, location);
            data.put(VALIDATION, validation);
            data.put(TRANSITIONS, transitions);
            day.put(shortDate, data);
            root.put(DB_NAME, day);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeDetectedActivity(final DetectedActivities detectedActivities) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(ACTIVITIES).put(detectedActivities.getShortTime(), detectedActivities.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeValidation(final String activity, final DetectedActivities detectedActivities) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(VALIDATION).put(detectedActivities.getShortTime(), detectedActivities.toJSON());
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(VALIDATION).getJSONObject(detectedActivities.getShortTime()).put("activity", activity);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeDetectedLocation(final DetectedLocation detectedLocation) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(LOCATION).put(detectedLocation.getShortTime(), detectedLocation.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<DetectedActivities> getActivityTransitions() {
        ArrayList<DetectedActivities> detectedActivities = new ArrayList<>();
        try {
            JSONArray transitions = root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS);
            for (int i = 0; i <= transitions.length(); i++) {
                JSONObject object = transitions.getJSONObject(i);

                DetectedActivities activity = new DetectedActivities();
                activity.setTimestamp(object.getString("timestamp"));
                ProbableActivities probableActivity = new ProbableActivities();
                probableActivity.setActivity(object.getJSONObject("detectedActivities").getString("activity"));
                activity.setProbableActivities(probableActivity);
                DetectedLocation detectedLocation = new DetectedLocation();
                detectedLocation.setLatitude(object.getJSONObject("detectedLocation").getDouble("latitude"));
                detectedLocation.setLongitude(object.getJSONObject("detectedLocation").getDouble("longitude"));
                if (object.getJSONObject("detectedLocation").has("detectedAddress")) {
                    DetectedAddress detectedAddress = new DetectedAddress();
                    detectedAddress.setAddress(object.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("address"));
                    detectedLocation.setDetectedAddress(detectedAddress);
                }
                activity.setDetectedLocation(detectedLocation);

                detectedActivities.add(activity);
            }
        } catch (JSONException e) {
            e.getMessage();
        }
        return detectedActivities;
    }

    public int countActivityTransitions() {
        int count = 0;
        try {
            JSONArray transitions = root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS);
            count = transitions.length();
        } catch (JSONException e) {
            e.getMessage();
        }
        return count;
    }

    public String readLastActivityTransition() {
        try {
            JSONArray transitions = root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS);
            if (transitions.length() > 0) {
                JSONObject transition = transitions.getJSONObject(transitions.length() - 1);
                return transition.getJSONObject("detectedActivities").getString("activity");
            } else {
                return "";
            }
        } catch (JSONException e) {
            e.getMessage();
        }
        return "";
    }

    public void writeActivityTransition(final DetectedActivities detectedActivities) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS).put(detectedActivities.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /*public void writeTransitionedActivity(final TransitionedActivity transitionedActivity) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(TRANSITIONS).put(transitionedActivity.getShortTime(), transitionedActivity.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }*/

    private String getDateShort() {
        return Timestamp.getDate();
    }
}
