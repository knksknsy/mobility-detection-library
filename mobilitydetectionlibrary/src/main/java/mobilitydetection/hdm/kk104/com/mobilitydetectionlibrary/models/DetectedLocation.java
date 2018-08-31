package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils.Timestamp;

/**
 * Class containing coordinates and address information from Geocoder
 *
 * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedAddress
 */
public class DetectedLocation implements Parcelable {

    private static final String TAG = DetectedLocation.class.getSimpleName();

    private Context context;

    /**
     * Timestamp in the following pattern: yyyy-MM-ddTHH:mm:ss
     */
    private String timestamp;
    private double latitude;
    private double longitude;
    private Location location;
    /**
     * Containing address information from Geocoder
     */
    private DetectedAddress detectedAddress;

    public DetectedLocation() {
        this.timestamp = Timestamp.generateTimestamp();
    }

    public DetectedLocation(String timestamp) {
        this.timestamp = timestamp;
    }

    public DetectedLocation(Context context, Location location) {
        this.context = context;
        this.timestamp = Timestamp.generateTimestamp();
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.detectedAddress = detectAddress(this.latitude, this.longitude);
    }

    /**
     * Constructor for creating a DetectedLocation object by reading a Parcel
     *
     * @param in
     */
    public DetectedLocation(Parcel in) {
        timestamp = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
        detectedAddress = in.readParcelable(DetectedAddress.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Converts the DetectedLocation object to a Parcel
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeParcelable(location, flags);
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public DetectedAddress getDetectedAddress() {
        return detectedAddress;
    }

    public void setDetectedAddress(DetectedAddress address) {
        this.detectedAddress = address;
    }

    /**
     * Getting the address information from Geocoder
     *
     * @param latitude
     * @param longitude
     * @return DetectedAddress object
     * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedAddress
     */
    private DetectedAddress detectAddress(double latitude, double longitude) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting()) {
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
        }
        return null;
    }

    public String getShortTime() {
        return Timestamp.getTime(timestamp);
    }

    /**
     * Converts a DetectedLocation object to a JSONObject.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("timestamp", this.timestamp);
            object.put("latitude", this.latitude);
            object.put("longitude", this.longitude);
            if (detectedAddress != null) {
                object.put("detectedAddress", detectedAddress.toJSON());
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
