package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

public class DatabaseStatistic {

    private static final String TAG = DatabaseStatistic.class.getSimpleName();

    private Context context;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private String fileName = "statistic";

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";

    private JSONObject statistic;

    private String shortDate;

    public DatabaseStatistic(Context context) {
        this.context = context;
        shortDate = getDateShort();
        readJSONFile();
    }

    public void writeJSONFile() {
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(statistic.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void readJSONFile() {
        try {
            fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            if (stringBuilder.toString().length() > 0) {
                statistic = new JSONObject(stringBuilder.toString());
                if (!statistic.getJSONObject(DB_NAME).has(shortDate)) {
                    initializeDay();
                }
            } else {
                statistic = null;
            }
        } catch (IOException e) {
            initializeStatistic();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void initializeStatistic() {
        try {
            if (statistic == null) {
                statistic = new JSONObject();
            }
            JSONObject day = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject activities = new JSONObject();
            JSONObject location = new JSONObject();
            JSONObject validation = new JSONObject();

            data.put(ACTIVITIES, activities);
            data.put(LOCATION, location);
            data.put(VALIDATION, validation);
            day.put(shortDate, data);
            statistic.put(DB_NAME, day);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void initializeDay() {
        try {
            JSONObject data = new JSONObject();
            JSONObject activities = new JSONObject();
            JSONObject location = new JSONObject();
            JSONObject validation = new JSONObject();

            data.put(ACTIVITIES, activities);
            data.put(LOCATION, location);
            data.put(VALIDATION, validation);
            statistic.getJSONObject(DB_NAME).put(shortDate, data);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeDetectedActivity(final DetectedActivities detectedActivities) {
        try {
            statistic.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(ACTIVITIES).put(detectedActivities.getShortTime(), detectedActivities.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeValidation(final String activity, final DetectedActivities detectedActivities) {
        try {
            statistic.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(VALIDATION).put(detectedActivities.getShortTime(), detectedActivities.toJSON());
            statistic.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(VALIDATION).getJSONObject(detectedActivities.getShortTime()).put("activity", activity);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeDetectedLocation(final DetectedLocation detectedLocation) {
        try {
            statistic.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONObject(LOCATION).put(detectedLocation.getShortTime(), detectedLocation.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getDateShort() {
        return Timestamp.getDate();
    }
}
