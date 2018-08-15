package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;

import com.google.android.gms.location.LocationResult;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

public class TrackingService extends IntentService {

    private static final String TAG = TrackingService.class.getSimpleName();

    public TrackingService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                DetectedLocation coordinate = new DetectedLocation(this, location);

                Intent locationIntent = new Intent(Actions.LOCATION_ACTION);
                locationIntent.putExtra(DetectedLocation.class.getSimpleName(), (Parcelable) coordinate);
                sendBroadcast(locationIntent, null);
            }
        }
    }

}
