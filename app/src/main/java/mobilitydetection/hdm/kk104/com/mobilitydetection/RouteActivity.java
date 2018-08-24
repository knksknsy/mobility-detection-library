package mobilitydetection.hdm.kk104.com.mobilitydetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;
import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.Route;

public class RouteActivity extends AppCompatActivity {

    private Route route;

    private TextView activityList;
    private ListView activityListView;

    private ArrayList<DetectedActivities> activities;
    private ActivityListAdapter activitiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        route = intent.getParcelableExtra(Route.class.getSimpleName());

        activities = new ArrayList<>();
        activityListView = findViewById(R.id.activities_list);
        activitiesAdapter = new ActivityListAdapter(this, activities);
        activityListView.setAdapter(activitiesAdapter);

        activitiesAdapter.addAll(route.getActivities());
    }
}
