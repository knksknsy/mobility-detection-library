package mobilitydetection.hdm.kk104.com.mobilitydetection;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.MobilityDetection;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners.MobilityDetectionListener;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MobilityDetection mobilityDetection;

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 200;
    private static final int REQUEST_CODE_ROUTE_ACTIVITY = 300;

    private List<String> geofences;
    private TextView geofenceList;
    private TextView activityList;
    private ListView activityListView;
    private ListView routesListView;
    private Button btnSave, btnDelete;
    private Vibrator vibe;

    // private Button btnSend;
    // private Spinner spinner;

    private ArrayList<DetectedActivities> activities;
    private ArrayList<Route> routes;
    private ActivityListAdapter activitiesAdapter;
    private RoutesListAdapter routesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        createNotificationChannel();
        initView();
        initMobilityDetection();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mobilityDetection.stopMobilityDetection();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel);
            String description = getString(R.string.notification_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initView() {
        geofences = new ArrayList<>();
        geofenceList = findViewById(R.id.geofence_list);
        geofenceList.setMovementMethod(new ScrollingMovementMethod());
        activityList = findViewById(R.id.activity_list);

        activities = new ArrayList<>();
        activityListView = findViewById(R.id.activity_list_view);
        activitiesAdapter = new ActivityListAdapter(this, activities);
        activityListView.setAdapter(activitiesAdapter);

        routes = new ArrayList<>();
        routesListView = findViewById(R.id.routes_list_view);
        routesAdapter = new RoutesListAdapter(this, routes);
        routesListView.setAdapter(routesAdapter);
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                openRouteActivity(routes.get(i));
            }
        });

        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobilityDetection.mobilityDetectionService.saveData();
            }
        });

        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activitiesAdapter.clear();
            }
        });

        /*spinner = findViewById(R.id.spinner);
        String[] activityItems = Activities.activities;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityItems);
        spinner.setAdapter(spinnerAdapter);*/

        /*btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(100);
                final String activity = spinner.getSelectedItem().toString();
                mobilityDetection.mobilityDetectionService.validateActivity(activity);
            }
        });*/
    }

    private void openRouteActivity(Route route) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra(Route.class.getSimpleName(), route);
        startActivityForResult(intent, REQUEST_CODE_ROUTE_ACTIVITY);
    }

    private void initMobilityDetection() {
        mobilityDetection = new MobilityDetection.Builder()
                .setContext(MainActivity.this)
                /*.setInterval(1000 * 5)
                .setFastInterval(1000 * 5)
                .setMediumInterval(1000 * 5)
                .setSlowInterval(1000 * 5)
                .setLoiteringDelayWifiConnectionChanged(1000 * 60 * 5)
                .setLoiteringDelayWifiConnectionTime(1000 * 60 * 5)
                .setLoiteringDelayPowerConnectionChanged(1000 * 60 * 5)
                .setLoiteringDelayActivity(1000 * 60 * 5)*/
                .setListener(new MobilityDetectionListener() {
                    @Override
                    public void onStopService() {
                        finish();
                    }

                    @Override
                    public void onTransitioned(DetectedActivities activity) {
                        activitiesAdapter.add(activity);
                    }

                    @Override
                    public void onTransitionsLoaded(ArrayList<DetectedActivities> activities) {
                        activitiesAdapter.clear();
                        activitiesAdapter.addAll(activities);
                    }

                    @Override
                    public void onActivityDetected(ArrayList<DetectedActivity> activities) {
                        ArrayList<DetectedActivity> copyActivities = new ArrayList<>(activities);
                        activityList.setText("");

                        for (DetectedActivity activity : copyActivities) {
                            int type = activity.getType();
                            int confidence = activity.getConfidence();

                            switch (type) {
                                case DetectedActivity.IN_VEHICLE: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.IN_VEHICLE) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.ON_BICYCLE: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.ON_BICYCLE) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.ON_FOOT: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.ON_FOOT) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.RUNNING: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.RUNNING) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.STILL: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.STILL) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.TILTING: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.TILTING) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.WALKING: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.WALKING) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                                case DetectedActivity.UNKNOWN: {
                                    activityList.append(Activities.getActivityType(DetectedActivity.UNKNOWN) + ": " + confidence);
                                    activityList.append("\n");
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onPowerConnectionChanged(boolean hasPowerConnection) {
                        if (hasPowerConnection) {
                            vibe.vibrate(1000);
                        } else {
                            vibe.vibrate(500);
                        }
                    }

                    @Override
                    public void onWifiConnectionChanged(boolean hasWifiConnection) {
                        if (hasWifiConnection) {
                            vibe.vibrate(1000);
                        } else {
                            vibe.vibrate(500);
                        }
                    }

                    @Override
                    public void onGeofenceAdded(String key) {
                        geofences.add(key);
                        setText();
                    }

                    @Override
                    public void onGeofenceRemoved(ArrayList<String> keys) {
                        for (String key : keys) {
                            for (int i = geofences.size() - 1; i >= 0; i--) {
                                if (geofences.get(i).equals(key)) {
                                    geofences.remove(i);
                                }
                            }
                        }
                        setText();
                    }

                    @Override
                    public void onGeofencesRemoved() {
                        geofences.clear();
                        setText();
                    }

                    @Override
                    public void onRouteEnded(ArrayList<Route> routes) {
                        activitiesAdapter.clear();
                        routesAdapter.clear();
                        for (Route route : routes) {
                            routesAdapter.add(route);
                        }
                    }

                    @Override
                    public void onRoutesLoaded(ArrayList<Route> routes) {
                        routesAdapter.clear();
                        for (Route route : routes) {
                            routesAdapter.add(route);
                        }
                    }
                })
                .build();

        initiateLocationSettings();
    }

    private void setText() {
        geofenceList.setText("");
        String text = "";
        for (String geofence : geofences) {
            text += geofence + "\n";
        }
        geofenceList.setText(text);
    }

    private void initiateLocationSettings() {
        if (checkPermission()) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Log.e(TAG, "PERMISSION_GRANTED for ACCESS_FINE_LOCATION");

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                Log.e(TAG, "GPS_PROVIDER disabled. Requesting location settings...");
                checkLocationSettings();
            } else {

                Log.e(TAG, "GPS_PROVIDER enabled");
                mobilityDetection.startMobilityDetection();
            }
        } else {
            Log.e(TAG, "PERMISSION_DENIEND for ACCESS_FINE_LOCATION. Requesting permission...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Log.e(TAG, "PERMISSION_GRANTED for ACCESS_FINE_LOCATION");
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                Log.e(TAG, "GPS_PROVIDER disabled. Requesting location settings...");
                checkLocationSettings();
            } else {

                Log.e(TAG, "GPS_PROVIDER enabled");
                mobilityDetection.startMobilityDetection();
            }
        } else {
            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
            mobilityDetection.stopMobilityDetection();
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e(TAG, "All location settings are satisfied");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_LOCATION_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, sendEx.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "LOCATION_SETTINGS RESULT_OK");
                mobilityDetection.startMobilityDetection();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "LOCATION_SETTINGS RESULT_CANCELED");
                checkLocationSettings();
            }
        }
        if (requestCode == REQUEST_CODE_ROUTE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

}
