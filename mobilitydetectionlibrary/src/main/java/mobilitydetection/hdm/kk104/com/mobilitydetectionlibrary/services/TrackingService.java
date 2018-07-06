package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

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
                DetectedLocation coordinate = new DetectedLocation(this, location.getLatitude(), location.getLongitude());

                Intent fbDbintent = new Intent("LOCATION_ACTION");
                fbDbintent.putExtra(DetectedLocation.class.getSimpleName(), coordinate);
                sendBroadcast(fbDbintent, null);
            }
        }
    }

}
