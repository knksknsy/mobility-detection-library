package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import android.content.Context;
import android.content.SharedPreferences;
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
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

public class DataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    private Context context;

    private final String DB_NAME = "statistic";
    private final String LOCATION = "location";
    private final String ACTIVITIES = "activities";
    private final String VALIDATION = "validation";
    private final String TRANSITIONS = "transitions";
    private final String WIFI = "wifi";
    private final String ROUTES = "routes";

    private final String WIFI_CONNECTION_TIMES = "WIFI_CONNECTION_TIMES";
    private final String LAST_WIFI_CONNECTION_SSID = "LAST_WIFI_CONNECTION_SSID";
    private final String WIFI_SSID = "WIFI_SSID";

    private JSONObject root;

    private String shortDate;

    public DataManager(Context context) {
        this.context = context;
        shortDate = getDateShort();
        readJSONFile();
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

    public void writeActivityTransition(final DetectedActivities detectedActivities) {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS).put(detectedActivities.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void writeRoute() {
        try {
            ArrayList<DetectedActivities> activities = getActivityTransitions();
            removeActivityTransitions();
            if (activities.size() > 0) {
                Route route = new Route(activities);
                root.getJSONObject(DB_NAME).getJSONArray(ROUTES).put(route.toJSON());
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<Route> getRoutes() {
        ArrayList<Route> routes = new ArrayList<>();
        try {
            JSONArray routesArray = root.getJSONObject(DB_NAME).getJSONArray(ROUTES);
            for (int i = 0; i < routesArray.length(); i++) {
                JSONObject routeObject = routesArray.getJSONObject(i);

                JSONArray objectRoutes = routeObject.getJSONArray("route");
                ArrayList<DetectedActivities> routeActivities = new ArrayList<>();
                for (int j = 0; j < objectRoutes.length(); j++) {
                    JSONObject objectRoute = objectRoutes.getJSONObject(j);
                    DetectedActivities detectedActivities = new DetectedActivities();
                    detectedActivities.setTimestamp(objectRoute.getString("timestamp"));
                    DetectedLocation detectedLocation = new DetectedLocation(objectRoute.getJSONObject("detectedLocation").getString("timestamp"));
                    detectedLocation.setLatitude(objectRoute.getJSONObject("detectedLocation").getDouble("latitude"));
                    detectedLocation.setLongitude(objectRoute.getJSONObject("detectedLocation").getDouble("longitude"));
                    DetectedAddress detectedAddress = new DetectedAddress();
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("address")) {
                        detectedAddress.setAddress(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("address"));
                    }
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("city")) {
                        detectedAddress.setCity(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("city"));
                    }
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("state")) {
                        detectedAddress.setState(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("state"));
                    }
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("country")) {
                        detectedAddress.setCountry(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("country"));
                    }
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("postalCode")) {
                        detectedAddress.setPostalCode(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("postalCode"));
                    }
                    if (objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").has("featureName")) {
                        detectedAddress.setFeatureName(objectRoute.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("featureName"));
                    }
                    detectedLocation.setDetectedAddress(detectedAddress);
                    detectedActivities.setDetectedLocation(detectedLocation);
                    detectedActivities.getProbableActivities().setActivity(objectRoute.getJSONObject("detectedActivities").getString("activity"));

                    routeActivities.add(detectedActivities);
                }

                Route route = new Route(routeActivities);
                routes.add(route);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return routes;
    }

    public void writeWifiLocation(String ssid, DetectedLocation location) {
        try {
            JSONObject object = root.getJSONObject(DB_NAME).getJSONObject(WIFI);
            if (object != JSONObject.NULL) {
                if (!root.getJSONObject(DB_NAME).getJSONObject(WIFI).has(ssid)) {
                    root.getJSONObject(DB_NAME).getJSONObject(WIFI).put(ssid, location.toJSON());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateWifiConnectionCount(String ssid) {
        try {
            JSONObject object = root.getJSONObject(DB_NAME).getJSONObject(WIFI);
            if (object != JSONObject.NULL) {
                if (object.has(ssid)) {
                    if (object.getJSONObject(ssid).has("dwellingCount")) {
                        int dwellingCount = object.getJSONObject(ssid).getInt("dwellingCount");
                        object.getJSONObject(ssid).put("dwellingCount", ++dwellingCount);
                    } else {
                        object.getJSONObject(ssid).put("dwellingCount", 1);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public boolean isWifiLocationStationary(String ssid) {
        boolean isStationary = false;
        try {
            JSONObject object = root.getJSONObject(DB_NAME).getJSONObject(WIFI);
            if (object != JSONObject.NULL) {
                if (object.has(ssid)) {
                    if (object.getJSONObject(ssid).has("dwellingCount")) {
                        isStationary = true;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return isStationary;
    }

    public Double[] getWifiLocation(String ssid) {
        Double[] location = new Double[2];
        try {
            location[0] = root.getJSONObject(DB_NAME).getJSONObject(WIFI).getJSONObject(ssid).getDouble("latitude");
            location[1] = root.getJSONObject(DB_NAME).getJSONObject(WIFI).getJSONObject(ssid).getDouble("longitude");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return location;
    }

    public boolean hasWifiLocation(String ssid) {
        boolean hasSSID = false;
        try {
            hasSSID = root.getJSONObject(DB_NAME).getJSONObject(WIFI).has(ssid);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return hasSSID;
    }

    public void writeWifiConnectionTime(String ssid) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(WIFI_CONNECTION_TIMES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ssid, Timestamp.generateTimestamp());
        editor.apply();
        writeLastWifiConnectionSSID(ssid);
    }

    public String getWifiConnectionTime(String ssid) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(WIFI_CONNECTION_TIMES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ssid, null);
    }

    private void writeLastWifiConnectionSSID(String ssid) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(LAST_WIFI_CONNECTION_SSID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIFI_SSID, ssid);
        editor.apply();
    }

    public String getLastWifiConnectionSSID() {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(LAST_WIFI_CONNECTION_SSID, Context.MODE_PRIVATE);
        return sharedPreferences.getString(WIFI_SSID, null);
    }

    public void removeLastWifiConnectionSSID() {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(LAST_WIFI_CONNECTION_SSID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(WIFI_SSID);
        editor.apply();
    }

    public ArrayList<DetectedActivities> getActivityTransitions() {
        ArrayList<DetectedActivities> detectedActivities = new ArrayList<>();
        try {
            JSONArray transitions = root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS);
            for (int i = 0; i < transitions.length(); i++) {
                JSONObject transition = transitions.getJSONObject(i);

                DetectedActivities activity = initActivityTransition(transition);

                detectedActivities.add(activity);
            }
        } catch (JSONException e) {
            e.getMessage();
        }
        return detectedActivities;
    }

    private void removeActivityTransitions() {
        try {
            root.getJSONObject(DB_NAME).getJSONObject(shortDate).put(TRANSITIONS, new JSONArray());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public DetectedActivities getLastActivityTransition() {
        try {
            JSONArray transitions = root.getJSONObject(DB_NAME).getJSONObject(shortDate).getJSONArray(TRANSITIONS);
            if (transitions.length() > 0) {
                JSONObject transition = transitions.getJSONObject(transitions.length() - 1);
                return initActivityTransition(transition);
            } else {
                return new DetectedActivities();
            }
        } catch (JSONException e) {
            e.getMessage();
        }
        return new DetectedActivities();
    }

    private DetectedActivities initActivityTransition(JSONObject transition) {
        try {
            DetectedActivities activity = new DetectedActivities();
            activity.setTimestamp(transition.getString("timestamp"));
            ProbableActivities probableActivity = new ProbableActivities();
            probableActivity.setActivity(transition.getJSONObject("detectedActivities").getString("activity"));
            activity.setProbableActivities(probableActivity);
            DetectedLocation detectedLocation = new DetectedLocation(activity.getTimestamp());
            detectedLocation.setLatitude(transition.getJSONObject("detectedLocation").getDouble("latitude"));
            detectedLocation.setLongitude(transition.getJSONObject("detectedLocation").getDouble("longitude"));
            if (transition.getJSONObject("detectedLocation").has("detectedAddress")) {
                DetectedAddress detectedAddress = new DetectedAddress();
                detectedAddress.setAddress(transition.getJSONObject("detectedLocation").getJSONObject("detectedAddress").getString("address"));
                detectedLocation.setDetectedAddress(detectedAddress);
            }
            activity.setDetectedLocation(detectedLocation);
            return activity;
        } catch (JSONException e) {
            e.getMessage();
        }
        return new DetectedActivities();
    }

    private void readJSONFile() {
        try {
            FileInputStream fileInputStream = context.openFileInput(DB_NAME);
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
                    JSONObject wifi = new JSONObject();
                    JSONArray routes = new JSONArray();

                    data.put(ACTIVITIES, activities);
                    data.put(LOCATION, location);
                    data.put(VALIDATION, validation);
                    data.put(TRANSITIONS, transitions);
                    root.getJSONObject(DB_NAME).put(shortDate, data);
                    root.getJSONObject(DB_NAME).put(WIFI, wifi);
                    root.getJSONObject(DB_NAME).put(ROUTES, routes);
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

    private void initializeJSON() {
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
            JSONObject wifi = new JSONObject();
            JSONArray routes = new JSONArray();

            data.put(ACTIVITIES, activities);
            data.put(LOCATION, location);
            data.put(VALIDATION, validation);
            data.put(TRANSITIONS, transitions);
            day.put(shortDate, data);
            root.put(DB_NAME, day);
            root.getJSONObject(DB_NAME).put(WIFI, wifi);
            root.getJSONObject(DB_NAME).put(ROUTES, routes);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void saveJSONFile() {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(DB_NAME, Context.MODE_PRIVATE);
            fileOutputStream.write(root.toString().getBytes());
            fileOutputStream.close();
            Log.e(TAG, "SAVED");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

        }
    }

    private String getDateShort() {
        return Timestamp.getDate();
    }
}
