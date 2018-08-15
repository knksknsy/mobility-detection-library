package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class DetectedAddress implements Parcelable, Serializable {

    private static final String TAG = DetectedAddress.class.getSimpleName();

    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String featureName;

    public DetectedAddress() {

    }

    public DetectedAddress(List<Address> address) {
        this.address = address.get(0).getAddressLine(0);
        this.city = address.get(0).getLocality();
        this.state = address.get(0).getAdminArea();
        this.country = address.get(0).getCountryName();
        this.postalCode = address.get(0).getPostalCode();
        this.featureName = address.get(0).getFeatureName();
    }

    public DetectedAddress(Parcel in) {
        address = in.readString();
        city = in.readString();
        state = in.readString();
        country = in.readString();
        postalCode = in.readString();
        featureName = in.readString();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(country);
        dest.writeString(postalCode);
        dest.writeString(featureName);
    }

    public static final Parcelable.Creator<DetectedAddress> CREATOR = new Parcelable.Creator<DetectedAddress>() {
        public DetectedAddress createFromParcel(Parcel in) {
            return new DetectedAddress(in);
        }

        public DetectedAddress[] newArray(int size) {
            return new DetectedAddress[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("address", this.address);
            object.put("city", this.city);
            object.put("state", this.state);
            object.put("country", this.country);
            object.put("postalCode", this.postalCode);
            object.put("featureName", this.featureName);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return object;
    }
}
