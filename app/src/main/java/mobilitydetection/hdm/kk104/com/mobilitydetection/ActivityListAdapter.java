package mobilitydetection.hdm.kk104.com.mobilitydetection;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public class ActivityListAdapter extends ArrayAdapter<DetectedActivities> {

    private List<DetectedActivities> detectedActivities;
    private Activity activity;

    public ActivityListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ActivityListAdapter(Context context, List<DetectedActivities> detectedActivities) {
        super(context, 0, detectedActivities);
        this.detectedActivities = detectedActivities;
        this.activity = (Activity) context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (getCount() > 0) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.activity_list_row, null);
            }

            final DetectedActivities detectedActivities = getItem(position);

            if (detectedActivities != null) {
                TextView name = v.findViewById(R.id.name);
                TextView time = v.findViewById(R.id.time);
                TextView location = v.findViewById(R.id.location);

                if (name != null && detectedActivities.getProbableActivities().getActivity() != null) {
                    name.setText(detectedActivities.getProbableActivities().getActivity());
                }

                if (time != null) {
                    time.setText(detectedActivities.getTimestamp());
                }

                if (location != null) {
                    String locationText;
                    if (detectedActivities.getDetectedLocation() != null) {
                        locationText = detectedActivities.getDetectedLocation().getDetectedAddress().getAddress() + "\n"
                                + detectedActivities.getDetectedLocation().getDetectedAddress().getPostalCode() + " " + detectedActivities.getDetectedLocation().getDetectedAddress().getCity();
                    } else {
                        locationText = "lat: " + String.valueOf(detectedActivities.getDetectedLocation().getLatitude()) + ", long: " + String.valueOf(detectedActivities.getDetectedLocation().getLongitude());
                    }
                    location.setText(locationText);
                }
            }
            return v;
        }
        return null;
    }

    public int getCount() {
        return detectedActivities.size();
    }


}
