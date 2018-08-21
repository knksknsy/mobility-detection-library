package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.constants.Actions;

public class GeofenceIntentService extends IntentService {

    private static final String TAG = GeofenceIntentService.class.getSimpleName();

    public GeofenceIntentService() {
        super(GeofenceIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, "errorMessage: " + errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        ArrayList<String> keys = new ArrayList<>();

        for (Geofence geofence : triggeringGeofences) {
            keys.add(geofence.getRequestId());
        }

        Intent i = new Intent(Actions.GEOFENCE_TRANSITION_ACTION);
        i.putExtra("geofenceTransition", geofenceTransition);
        i.putStringArrayListExtra("keys", keys);
        sendBroadcast(i, null);
    }

}
