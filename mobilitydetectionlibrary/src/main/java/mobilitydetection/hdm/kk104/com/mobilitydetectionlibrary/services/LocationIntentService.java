package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedLocation;

/**
 * IntentService for getting location updates.
 *
 * @deprecated
 */
public class LocationIntentService extends IntentService {

    private static final String TAG = LocationIntentService.class.getSimpleName();

    public LocationIntentService() {
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
                locationIntent.putExtra(DetectedLocation.class.getSimpleName(), coordinate);
                sendBroadcast(locationIntent, null);
            }
        }
    }

}
