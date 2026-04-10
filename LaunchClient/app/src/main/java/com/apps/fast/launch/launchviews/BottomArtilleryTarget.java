package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable;
import launch.game.entities.Player;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomArtilleryTarget extends LaunchView
{
    private TextView txtInstructions;
    private TextView txtFlightTime;
    private TextView txtFriendlyFire;
    private LinearLayout btnFire;
    private LinearLayout btnCancel;
    private SeekBar seekRadius;

    private boolean bFromPlayer = false;
    private int lSiteID;
    private int lSlotNo;
    private MissileType missileType;

    private GeoCoord geoTarget = null;
    private EntityPointer target;
    private float fltRadius;

    private GoogleMap map;
    private Polyline targetTrajectory;
    private Circle radiusCircle;
    private MapEntity host;
    private SystemType systemType;
    private GeoCoord geoSource = null;
    private boolean bAirburst = false;
    private MissileSystem system;

    //To not repeat elite attack warnings.
    private static int lLastWarnedID = -1;

    public BottomArtilleryTarget(LaunchClientGame game, MainActivity activity, int lSiteID, SystemType systemType)
    {
        super(game, activity, true);
        this.lSiteID = lSiteID;
        this.lSlotNo = lSlotNo;
        this.systemType = systemType;

        switch(systemType)
        {
            case MISSILE_SITE:
            {
                host = game.GetMissileSite(lSiteID);
                geoSource = game.GetMissileSite(lSiteID).GetPosition();
                system = game.GetMissileSite(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetMissileSite(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case ARTILLERY_GUN:
            {
                host = game.GetArtilleryGun(lSiteID);
                geoSource = game.GetArtilleryGun(lSiteID).GetPosition();
                system = game.GetArtilleryGun(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetArtilleryGun(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case TANK_ARTILLERY:
            {
                host = game.GetTank(lSiteID);
                geoSource = game.GetTank(lSiteID).GetPosition();
                system = game.GetTank(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetTank(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_artillery_target, this);

        txtInstructions = findViewById(R.id.txtInstructions);
        txtFlightTime = findViewById(R.id.txtFlightTime);
        txtFriendlyFire = findViewById(R.id.txtFriendlyFire);
        btnFire = findViewById(R.id.btnFire);
        btnCancel = findViewById(R.id.btnCancel);
        seekRadius = findViewById(R.id.seekRadius);

        seekRadius.setVisibility(GONE);

        btnFire.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(geoTarget == null)
                {
                    //Target not selected.
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_target_map));
                }
                else if(geoSource.DistanceTo(geoTarget) > Defs.ARTILLERY_RANGE)
                {
                    //Target out of range.
                    activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                }
                else if(game.GetOurPlayer().GetRespawnProtected())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.attacking_respawn_protected_you, TextUtilities.GetTimeAmount(game.GetOurPlayer().GetStateTimeRemaining())));
                    launchDialog.SetOnClickYes(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            Launch();
                        }
                    });
                    launchDialog.SetOnClickNo(new OnClickListener()
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
                    Launch();
                }
            }
        });

        /*seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                float fltValue = (float)seekBar.getProgress();

                fltRadius = (fltValue/100) * geoSource.DistanceTo(geoTarget);

                if(radiusCircle != null)
                {
                    radiusCircle.setRadius(fltRadius * Defs.METRES_PER_KM);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });*/

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

    private void Launch()
    {
        activity.InformationMode(false);
        game.UnitCommand(Movable.MoveOrders.ATTACK, Collections.singletonList(host.GetPointer()), target, geoTarget, CargoSystem.LootType.NONE, 0, 0);
    }

    public void LocationSelected(GeoCoord geoLocation, Polyline targetTrajectory, GoogleMap map)
    {
        this.geoTarget = geoLocation;
        this.targetTrajectory = targetTrajectory;
        this.map = map;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(radiusCircle != null)
                    radiusCircle.remove();

                radiusCircle = map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(geoTarget))
                        .radius(fltRadius * Defs.METRES_PER_KM * Defs.METRES_PER_KM)
                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                        .strokeWidth(5.0f));
            }
        });

        //Spawn a thread to check for friendly fire, so as not to hang the UI thread.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean bThreatensFriendlies = game.ThreatensFriendlies(game.GetOurPlayerID(), geoTarget, game.GetConfig().GetLargestBlastRadiusMissile(system), true, true, false);

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtFriendlyFire.setVisibility(bThreatensFriendlies ? VISIBLE : GONE);
                    }
                });
            }
        }).start();

        Update();
    }

    public void TargetSelected(MapEntity targetEntity, Polyline trajectory, GoogleMap map)
    {
        this.target = targetEntity.GetPointer();
        this.map = map;
        this.targetTrajectory = trajectory;
        this.geoTarget = targetEntity.GetPosition();

        txtFlightTime = findViewById(R.id.txtFlightTime); //This is only here because the next line threw a nullpointer on txtFlightTime for some reason.

        if(txtFlightTime != null)
            txtFlightTime.setText(context.getString(R.string.target_format, TextUtilities.GetEntityTypeAndName(targetEntity, game)));

        //Spawn a thread to check for friendly fire, so as not to hang the UI thread.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean bThreatensFriendlies = game.ThreatensFriendlies(game.GetOurPlayerID(), geoTarget, game.GetConfig().GetLargestBlastRadiusMissile(system), true, true, false);

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtFriendlyFire.setVisibility(bThreatensFriendlies ? VISIBLE : GONE);
                    }
                });
            }
        }).start();

        Update();
    }

    @Override
    public void Update()
    {
        //TO DO: Kill the UI if anything happens?

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(geoTarget == null && target == null)
                {
                    txtInstructions.setVisibility(VISIBLE);
                }
                else
                {
                    if(geoSource != null && geoTarget != null)
                    {
                        txtInstructions.setVisibility(GONE);

                        //seekRadius.setMax((int)fltDistance);
                        //seekRadius.setVisibility(VISIBLE);

                        //Update trajectory.
                        List<LatLng> points = new ArrayList<LatLng>();
                        points.add(Utilities.GetLatLng(geoSource));

                        if(target != null)
                        {
                            geoTarget = target.GetMapEntity(game).GetPosition();
                        }

                        points.add(Utilities.GetLatLng(geoTarget));

                        targetTrajectory.setPoints(points);
                    }
                }
            }
        });
    }

    public void MapCleared()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(geoTarget != null)
                {
                    if(radiusCircle != null)
                        radiusCircle.remove();

                    radiusCircle = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoTarget))
                            .radius(fltRadius * Defs.METRES_PER_KM * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
        });
    }
}
