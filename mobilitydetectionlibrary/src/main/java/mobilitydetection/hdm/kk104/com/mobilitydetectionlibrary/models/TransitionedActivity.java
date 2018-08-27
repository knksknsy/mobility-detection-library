package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import org.json.JSONException;
import org.json.JSONObject;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.Timestamp;

/**
 * Containing activity transition information.
 *
 * @deprecated
 */
public class TransitionedActivity implements Parcelable {

    private static final String TAG = TransitionedActivity.class.getSimpleName();

    private String timestamp;
    private String enteredActivity;
    private String exitedActivity;
    private DetectedLocation detectedLocation;

    public TransitionedActivity(ActivityTransitionResult result, DetectedLocation detectedLocation) {
        this.timestamp = generateTimestamp();
        this.detectedLocation = detectedLocation;
        initTransitions(result);
    }

    public TransitionedActivity(ActivityTransitionResult result) {
        this.timestamp = generateTimestamp();
        initTransitions(result);
    }

    private void initTransitions(ActivityTransitionResult result) {
        for (ActivityTransitionEvent event : result.getTransitionEvents()) {
            if (Activities.getTransitionType(event.getTransitionType()).equals(Activities.ENTER)) {
                this.enteredActivity = Activities.getActivityType(event.getActivityType());
            } else if (Activities.getTransitionType(event.getTransitionType()).equals(Activities.EXIT)) {
                this.exitedActivity = Activities.getActivityType(event.getActivityType());
            }
        }
    }

    public TransitionedActivity(Parcel in) {
        timestamp = in.readString();
        detectedLocation = in.readParcelable(getClass().getClassLoader());
        enteredActivity = in.readString();
        exitedActivity = in.readString();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeParcelable(detectedLocation, flags);
        dest.writeString(enteredActivity);
        dest.writeString(exitedActivity);
    }

    public static final Parcelable.Creator<TransitionedActivity> CREATOR = new Parcelable.Creator<TransitionedActivity>() {
        public TransitionedActivity createFromParcel(Parcel in) {
            return new TransitionedActivity(in);
        }

        public TransitionedActivity[] newArray(int size) {
            return new TransitionedActivity[size];
        }
    };

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getShortTime() {
        return Timestamp.getTime(timestamp);
    }

    public DetectedLocation getDetectedLocation() {
        return detectedLocation;
    }

    public void setDetectedLocation(DetectedLocation detectedLocation) {
        this.detectedLocation = detectedLocation;
    }

    private String generateTimestamp() {
        return Timestamp.generateTimestamp();
    }

    /**
     * Converts a TransitionedActivity object to a JSONObject.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("timestamp", this.timestamp);
            if (detectedLocation != null) {
                object.put("detectedLocation", detectedLocation.toJSON());
            }
            object.put("enteredActivity", this.enteredActivity);
            object.put("exitedActivity", this.exitedActivity);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }

}
