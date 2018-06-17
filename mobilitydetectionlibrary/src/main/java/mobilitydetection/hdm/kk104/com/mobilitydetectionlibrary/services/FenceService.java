package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

public class FenceService extends IntentService {

    private static final String TAG = FenceService.class.getSimpleName();

    public FenceService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.e(TAG, "Fence Receiver Received");

        if (TextUtils.equals(fenceState.getFenceKey(), "headphoneFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG, "Fence > Headphones are plugged in.");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "Fence > Headphones are NOT plugged in.");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "Fence > The Headphone fence is in an unknown state.");
                    break;
            }
        }
    }
}
