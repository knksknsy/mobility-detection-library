package mobilitydetection.hdm.kk104.com.mobilitydetection.adapters;

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

import mobilitydetection.hdm.kk104.com.mobilitydetection.R;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

public class RoutesListAdapter extends ArrayAdapter<Route> {

    private List<Route> routes;
    private Activity activity;

    public RoutesListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RoutesListAdapter(Context context, List<Route> routes) {
        super(context, 0, routes);
        this.routes = routes;
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
                v = vi.inflate(R.layout.routes_list_row, null);
            }

            final Route routes = getItem(position);

            if (routes != null) {
                TextView startTime = v.findViewById(R.id.startTime);
                TextView startActivity = v.findViewById(R.id.startActivity);
                TextView startLocation = v.findViewById(R.id.startLocation);
                TextView endTime = v.findViewById(R.id.endTime);
                TextView endActivity = v.findViewById(R.id.endActivity);
                TextView endLocation = v.findViewById(R.id.endLocation);

                if (startTime != null && routes.getStartTime() != null) {
                    startTime.setText(routes.getStartTime());
                }
                if (startActivity != null && routes.getActivities().get(0).getProbableActivities().getActivity() != null) {
                    startActivity.setText(routes.getActivities().get(0).getProbableActivities().getActivity());
                }
                if (startLocation != null && routes.getStartLocation() != null) {
                    String locationText;
                    if (routes.getStartLocation() != null && routes.getStartLocation().getDetectedAddress() != null) {
                        locationText = routes.getStartLocation().getDetectedAddress().getAddress();
                    } else {
                        locationText = "lat: " + String.valueOf(routes.getStartLocation().getLatitude()) + ", long: " + String.valueOf(routes.getStartLocation().getLongitude());
                    }
                    startLocation.setText(locationText);
                }
                if (endTime != null && routes.getEndTime() != null) {
                    endTime.setText(routes.getEndTime());
                }
                if (endActivity != null && routes.getActivities().get(routes.getActivities().size() - 1).getProbableActivities().getActivity() != null) {
                    endActivity.setText(routes.getActivities().get(routes.getActivities().size() - 1).getProbableActivities().getActivity());
                }
                if (endLocation != null && routes.getEndLocation() != null) {
                    String locationText;
                    if (routes.getEndLocation() != null && routes.getEndLocation().getDetectedAddress() != null) {
                        locationText = routes.getEndLocation().getDetectedAddress().getAddress();
                    } else {
                        locationText = "lat: " + String.valueOf(routes.getEndLocation().getLatitude()) + ", long: " + String.valueOf(routes.getStartLocation().getLongitude());
                    }
                    endLocation.setText(locationText);
                }
            }
            return v;
        }
        return null;
    }

    public int getCount() {
        return routes.size();
    }


}
