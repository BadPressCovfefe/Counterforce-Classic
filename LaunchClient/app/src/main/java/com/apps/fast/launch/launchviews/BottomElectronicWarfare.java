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

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomElectronicWarfare extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtFriendlyFire;
    private TextView txtOutOfRange;
    private TextView txtTargetInvalid;
    private TextView txtInstructions;
    private LinearLayout btnFire;

    private Airplane aircraft;
    private EntityPointer target;
    private GeoCoord geoTarget;

    private GoogleMap map;
    private Polyline targetTrajectory;

    public BottomElectronicWarfare(LaunchClientGame game, MainActivity activity, Airplane aircraft)
    {
        super(game, activity, true);
        this.aircraft = aircraft;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_electronic_warfare, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtFriendlyFire = findViewById(R.id.txtFriendlyFire);
        btnFire = findViewById(R.id.btnFire);
        txtTargetInvalid = findViewById(R.id.txtTargetInvalid);
        txtOutOfRange = findViewById(R.id.txtOutOfRange);
        txtInstructions = findViewById(R.id.txtInstructions);

        btnFire.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(target == null)
                {
                    //Target not selected.
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_target_map));
                }
                else if(aircraft.GetPosition().DistanceTo(target.GetMapEntity(game).GetPosition()) > Defs.ELECTRONIC_WARFARE_RANGE)
                {
                    //Target out of range.
                    activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                }
                else if(game.WouldBeFriendlyFire(game.GetOwner(target.GetMapEntity(game)), game.GetOurPlayer()))
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.deliberate_friendly_fire));
                }
                else if(game.GetOurPlayer().GetRespawnProtected())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.attacking_respawn_protected_you, TextUtilities.GetTimeAmount(game.GetOurPlayer().GetStateTimeRemaining())));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            Fire();
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
                else
                {
                    Fire();
                }
            }
        });

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

    private void Fire()
    {
        activity.InformationMode(false);

        game.FireElectronicWarfare(aircraft.GetID(), target);
    }

    public void TargetSelected(MapEntity mapTarget, Polyline targetTrajectory, GoogleMap map)
    {
        this.targetTrajectory = targetTrajectory;
        this.map = map;
        target = mapTarget.GetPointer();
        geoTarget = mapTarget.GetPosition();

        if(game.ElectronicWarfareTargetValid(mapTarget))
        {
            txtTargetInvalid.setVisibility(GONE);
        }
        else
        {
            txtTargetInvalid.setVisibility(VISIBLE);
        }

        if(game.WouldBeFriendlyFire(game.GetOwner(mapTarget), game.GetOurPlayer()))
            txtFriendlyFire.setVisibility(VISIBLE);
        else
            txtFriendlyFire.setVisibility(GONE);

        if(mapTarget.GetPosition().DistanceTo(aircraft.GetPosition()) > Defs.ELECTRONIC_WARFARE_RANGE)
            txtOutOfRange.setVisibility(VISIBLE);
        else
            txtOutOfRange.setVisibility(GONE);

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
                if(target == null)
                {
                    txtInstructions.setVisibility(VISIBLE);
                }
                else
                {
                    GeoCoord geoTarget = target.GetMapEntity(game).GetPosition();
                    List<LatLng> points = new ArrayList<LatLng>();

                    GeoCoord geoFrom = aircraft.GetPosition();

                    points.add(Utilities.GetLatLng(geoFrom));
                    points.add(Utilities.GetLatLng(geoTarget));
                    targetTrajectory.setPoints(points);

                    txtInstructions.setVisibility(GONE);
                }
            }
        });
    }
}
