package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomRangeFinder extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtDistanceTo;

    private GeoCoord geoPoint1;
    private GeoCoord geoPoint2;

    private GoogleMap map;
    private Polyline targetTrajectory;

    public BottomRangeFinder(LaunchClientGame game, MainActivity activity, GeoCoord geoPoint1)
    {
        super(game, activity, true);
        this.geoPoint1 = geoPoint1;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_range_finder, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtDistanceTo = findViewById(R.id.txtDistanceTo);
        txtDistanceTo.setVisibility(GONE);

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    public void LocationSelected(GeoCoord geoLocation, Polyline targetTrajectory, GoogleMap map)
    {
        geoPoint2 = geoLocation;
        this.targetTrajectory = targetTrajectory;
        this.map = map;

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(geoPoint1 != null && geoPoint2 != null)
                {
                    txtDistanceTo.setVisibility(VISIBLE);
                    txtDistanceTo.setText(String.format(context.getString(R.string.distance_to), TextUtilities.GetDistanceStringFromKM(geoPoint1.DistanceTo(geoPoint2))));
                }
                else
                {
                    txtDistanceTo.setVisibility(GONE);
                }
            }
        });
    }
}
