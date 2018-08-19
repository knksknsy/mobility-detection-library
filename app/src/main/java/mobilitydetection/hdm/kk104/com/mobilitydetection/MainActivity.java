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
import android.util.Log;
import android.view.View;
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

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.MobilityDetection;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners.MobilityDetectionListener;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MobilityDetection mobilityDetection;

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 200;

    private TextView activityList;
    private ListView activityListView;
    // private Button btnSend;
    private Button btnSave, btnDelete;
    // private Spinner spinner;
    private Vibrator vibe;

    private ArrayList<DetectedActivities> activities;
    private ActivityListAdapter adapter;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel);
            String description = getString(R.string.notification_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initView() {
        activityList = findViewById(R.id.activity_list);

        /*spinner = findViewById(R.id.spinner);
        String[] activityItems = Activities.activities;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityItems);
        spinner.setAdapter(spinnerAdapter);*/

        activities = new ArrayList<>();
        activityListView = findViewById(R.id.activity_list_view);
        adapter = new ActivityListAdapter(this, activities);
        activityListView.setAdapter(adapter);

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
                adapter.clear();
            }
        });

        // btnSend = findViewById(R.id.btn_send);
        /*btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(100);
                final String activity = spinner.getSelectedItem().toString();
                mobilityDetection.mobilityDetectionService.validateActivity(activity);
            }
        });*/
    }

    private void initMobilityDetection() {
        mobilityDetection = new MobilityDetection.Builder()
                .setContext(MainActivity.this)
                .setListener(new MobilityDetectionListener() {
                    @Override
                    public void onStopService() {
                        finish();
                    }

                    @Override
                    public void onTransitioned(DetectedActivities activity) {
                        adapter.add(activity);
                    }

                    @Override
                    public void onTransitionsLoaded(ArrayList<DetectedActivities> activities) {
                        adapter.clear();
                        adapter.addAll(activities);
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
                })
                .build();

        initiateLocationSettings();
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
