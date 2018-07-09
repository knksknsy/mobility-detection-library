package mobilitydetection.hdm.kk104.com.mobilitydetection;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.MobilityDetection;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.FirebaseDatabaseStatistic;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Activities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Credentials;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.DetectedActivitiesService;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MobilityDetection mobilityDetection;

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 200;

    private TextView textActivity, textConfidence, textList;
    private Button btnSend;
    private Spinner spinner;

    // private FirebaseDatabaseStatistic fbStatistic;

    private ActivityRecognitionClient arClient;
    private PendingIntent activityPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arClient = new ActivityRecognitionClient(MainActivity.this);

        // fbStatistic = new FirebaseDatabaseStatistic();

        createNotificationChannel();
        initView();
        initMobilityDetection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
        textList = findViewById(R.id.txt_list);
        textActivity = findViewById(R.id.txt_activity);
        textConfidence = findViewById(R.id.txt_confidence);

        spinner = findViewById(R.id.spinner);
        String[] activityItems = Activities.activities;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityItems);
        spinner.setAdapter(spinnerAdapter);

        btnSend = findViewById(R.id.btn_send);

        /*// Awareness
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String activity = spinner.getSelectedItem().toString();
                // todo outsource
                Awareness.getSnapshotClient(getApplicationContext()).getDetectedActivity()
                        .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                            @Override
                            public void onSuccess(DetectedActivityResponse detectedActivityResponse) {
                                List<DetectedActivity> detectedActivity = detectedActivityResponse.getActivityRecognitionResult().getProbableActivities();
                                fbStatistic.uploadValidation(activity, detectedActivity);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });
            }
        });*/

        // ActivityRecognition
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String activity = spinner.getSelectedItem().toString();

                Intent activityIntent = new Intent(MainActivity.this, DetectedActivitiesService.class);
                int requestCode = 3;
                activityIntent.putExtra("requestCode", requestCode);
                activityIntent.putExtra("validation", activity);

                activityPendingIntent = PendingIntent.getService(MainActivity.this, requestCode, activityIntent, PendingIntent.FLAG_ONE_SHOT);

                Task<Void> task = arClient.requestActivityUpdates(0, activityPendingIntent);

                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "successfully requested");
                        // arClient.removeActivityUpdates(activityPendingIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                        // arClient.removeActivityUpdates(activityPendingIntent);
                    }
                });
            }
        });
    }

    @Subscribe()
    public void removeActivityRecognitionUpdates(String placeholder) {
        if (placeholder == "REMOVE_ACTIVITY_RECOGNITION") {
            arClient.removeActivityUpdates(activityPendingIntent);
            Log.e(TAG, "updates successfully removed");
        }
    }

    private void initMobilityDetection() {
        Credentials credentials;
        try {
            credentials = new Credentials(Util.getProperty("login", getApplicationContext()), Util.getProperty("password", getApplicationContext()));
            mobilityDetection = MobilityDetection.getInstance()
                    .setContext(MainActivity.this)
                    .setFirebaseCredentials(credentials);

            initiateLocationSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initiateLocationSettings() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (permission == PackageManager.PERMISSION_GRANTED) {

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleUserActivity(final ArrayList<DetectedActivity> activities) {
        ArrayList<DetectedActivity> copyActivities = new ArrayList<>(activities);
        textActivity.setText("");
        textConfidence.setText("");
        textList.setText("");

        for (DetectedActivity activity : copyActivities) {
            int type = activity.getType();
            int confidence = activity.getConfidence();

            String label = "UNKNOWN";

            switch (type) {
                case DetectedActivity.IN_VEHICLE: {
                    label = "IN_VEHICLE";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    label = "ON_BICYCLE";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    label = "ON_FOOT";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.RUNNING: {
                    label = "RUNNING";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.STILL: {
                    label = "STILL";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.TILTING: {
                    label = "TILTING";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.WALKING: {
                    label = "WALKING";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    label = "UNKNOWN";
                    textList.append(label + ": " + confidence);
                    textList.append("\n");
                    break;
                }
            }

            Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

            if (confidence > 75) {
                textActivity.setText(label);
                textConfidence.setText("Confidence: " + confidence);
            }
        }
    }

}
