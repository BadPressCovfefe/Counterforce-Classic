package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Ship;

public class BottomNewHomebase extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtSelectAirbase;
    private LinearLayout btnSetHomebase;

    private AirplaneInterface aircraft;
    private MapEntity newHomebase;
    private GoogleMap map;
    private Polyline targetTrajectory;

    public BottomNewHomebase(LaunchClientGame game, MainActivity activity, AirplaneInterface aircraft)
    {
        super(game, activity, true);
        this.aircraft = aircraft;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_new_homebase, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtSelectAirbase = findViewById(R.id.txtSelectAirbase);
        btnSetHomebase = findViewById(R.id.btnSetHomebase);

        btnSetHomebase.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(newHomebase != null)
                {
                    if(newHomebase instanceof Ship && !((Ship)newHomebase).HasAircraft())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.must_be_carrier));
                    }
                    else if(newHomebase.GetOwnedBy(game.GetOurPlayerID()) || (newHomebase instanceof Ship && ((Ship)newHomebase).HasAircraft() && ((Ship)newHomebase).GetAircraftSystem().GetOpen()) || (newHomebase instanceof Airbase && ((Airbase)newHomebase).GetAircraftSystem().GetOpen()))
                    {
                        SetNewHomebase();
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.homebase_must_be_open));
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_new_airbase));
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(true);
            }
        });

        Update();
    }

    private void SetNewHomebase()
    {
        if(newHomebase != null)
        {
            game.ChangeAircraftHomebase(((LaunchEntity)aircraft).GetPointer(), newHomebase.GetPointer());
        }

        activity.InformationMode(true);
    }

    public void AirbaseSelected(MapEntity homebase, Polyline targetTrajectory, GoogleMap map)
    {
        this.targetTrajectory = targetTrajectory;
        this.map = map;

        if(homebase instanceof Airbase || (homebase instanceof Ship && ((Ship)homebase).HasAircraft()))
        {
            newHomebase = homebase;
        }

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
                if(newHomebase == null)
                {
                    txtSelectAirbase.setVisibility(VISIBLE);
                }
                else
                {
                    GeoCoord geoAirbase = newHomebase.GetPosition();
                    List<LatLng> points = new ArrayList<LatLng>();

                    GeoCoord geoFrom = ((MapEntity)aircraft).GetPosition();

                    points.add(Utilities.GetLatLng(geoFrom));
                    points.add(Utilities.GetLatLng(geoAirbase));
                    targetTrajectory.setPoints(points);

                    txtSelectAirbase.setVisibility(GONE);
                }
            }
        });
    }
}
