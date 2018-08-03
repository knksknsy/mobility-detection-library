package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers.Timestamp;

public class DetectedLocation implements Parcelable, Serializable {

    private static final String TAG = DetectedLocation.class.getSimpleName();

    private Context context;

    private String timestamp;
    private double latitude;
    private double longitude;
    private DetectedAddress detectedAddress;

    public DetectedLocation(Context context, double latitude, double longitude) {
        this.context = context;
        this.timestamp = generateTimestamp();
        this.latitude = latitude;
        this.longitude = longitude;
        this.detectedAddress = detectAddress(this.latitude, this.longitude);
    }

    public DetectedLocation(Parcel in) {
        timestamp = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        detectedAddress = in.readParcelable(DetectedAddress.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeParcelable(detectedAddress, flags);
    }

    public static final Parcelable.Creator<DetectedLocation> CREATOR = new Parcelable.Creator<DetectedLocation>() {
        public DetectedLocation createFromParcel(Parcel in) {
            return new DetectedLocation(in);
        }

        public DetectedLocation[] newArray(int size) {
            return new DetectedLocation[size];
        }
    };

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public DetectedAddress getDetectedAddress() {
        return detectedAddress;
    }

    public void setDetectedAddress(DetectedAddress address) {
        this.detectedAddress = address;
    }

    private DetectedAddress detectAddress(double latitude, double longitude) {
        List<Address> address = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "Cannot get address for location: lat: " + latitude + ", long: " + longitude);
        }

        if (!address.isEmpty()) {
            return new DetectedAddress(address);
        }
        return null;
    }

    private String generateTimestamp() {
        return Timestamp.generateTimestamp();
    }

    public String getShortTime() {
        return Timestamp.getTime(timestamp);
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("timestamp", this.timestamp);
            object.put("longitude", this.longitude);
            object.put("latitude", this.latitude);
            object.put("detectedAddress", detectedAddress.toJSON());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
