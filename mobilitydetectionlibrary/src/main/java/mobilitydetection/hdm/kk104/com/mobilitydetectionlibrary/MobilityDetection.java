package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary;

import android.content.Context;
import android.content.Intent;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.services.MobilityDetectionService;

public class MobilityDetection {

    private static final String TAG = MobilityDetection.class.getSimpleName();

    private static final MobilityDetection mobilityDetection = new MobilityDetection();

    private Context context;

    private MobilityDetection() {

    }

    public Context getContext() {
        return context;
    }

    public MobilityDetection setContext(Context context) {
        this.context = context;
        return this;
    }

    public static MobilityDetection getInstance() {
        return mobilityDetection;
    }

    public void startMobilityDetection() throws NullPointerException {
        if (context != null) {
            Intent intent = new Intent(context, MobilityDetectionService.class);
            context.startService(intent);
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

    public void stopMobilityDetection() throws NullPointerException {
        if (context != null) {
            Intent intent = new Intent(context, MobilityDetectionService.class);
            context.stopService(intent);
        } else {
            throw new NullPointerException("Context is not defined.");
        }
    }

}
