package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public interface ActivityTransitionListener {

    void onTransitioned(DetectedActivities activity);

    void onTransitionsLoaded(ArrayList<DetectedActivities> activities);

    void onActivityDetected(ArrayList<DetectedActivity> activities);
}