package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.listeners;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public interface ActivityTransitionListener {

    void onTransitioned(DetectedActivities activity);
}
