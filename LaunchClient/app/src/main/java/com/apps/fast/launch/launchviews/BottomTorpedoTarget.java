package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
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
import launch.game.entities.MapEntity;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.types.TorpedoType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomTorpedoTarget extends LaunchView
{
    private TextView txtInstructions;
    private TextView txtOutOfRange;
    private TextView txtTorpedoName;
    private TextView txtFlightTime;
    private TextView txtFriendlyFire;
    private LinearLayout btnFire;
    private LinearLayout btnCancel;
    private ImageView imgHoming;

    private int lSiteID;
    private int lSlotNo;
    private TorpedoType torpedoType;

    private GeoCoord geoTarget = null;
    private EntityPointer target;

    private GoogleMap map;
    private Polyline targetTrajectory;

    private SystemType systemType;
    private GeoCoord geoSource = null;
    private MissileSystem system;

    //To not repeat elite/noob attack warnings.
    private static int lLastWarnedID = -1;

    public BottomTorpedoTarget(LaunchClientGame game, MainActivity activity, int lSiteID, int lSlotNo, SystemType systemType)
    {
        super(game, activity, true);
        this.lSiteID = lSiteID;
        this.lSlotNo = lSlotNo;
        this.systemType = systemType;

        switch(systemType)
        {
            case SHIP_TORPEDOES:
            {
                geoSource = game.GetShip(lSiteID).GetPosition();
                system = game.GetShip(lSiteID).GetTorpedoSystem();
                torpedoType = game.GetConfig().GetTorpedoType(game.GetShip(lSiteID).GetTorpedoSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                geoSource = game.GetSubmarine(lSiteID).GetPosition();
                system = game.GetSubmarine(lSiteID).GetTorpedoSystem();
                torpedoType = game.GetConfig().GetTorpedoType(game.GetSubmarine(lSiteID).GetTorpedoSystem().GetSlotMissileType(lSlotNo));
            }
            break;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_torpedo_target, this);

        txtInstructions = findViewById(R.id.txtInstructions);
        txtOutOfRange = findViewById(R.id.txtOutOfRange);
        txtTorpedoName = findViewById(R.id.txtTorpedoName);
        txtFlightTime = findViewById(R.id.txtFlightTime);
        txtFriendlyFire = findViewById(R.id.txtFriendlyFire);
        btnFire = findViewById(R.id.btnFire);
        btnCancel = findViewById(R.id.btnCancel);
        imgHoming = findViewById(R.id.imgHoming);

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
                else if(geoSource.DistanceTo(geoTarget) > torpedoType.GetTorpedoRange())
                {
                    //Target out of range.
                    activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                }
                else if(game.ThreatensFriendlies(game.GetOurPlayerID(), geoTarget, torpedoType, false))
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.deliberate_friendly_fire));
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
                            SecondStateFiringChecks();
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
                    SecondStateFiringChecks();
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

    private void SecondStateFiringChecks()
    {
        //Splitting like this allows chaining of warning messages (breaking own respawn invulnerability, followed by everything else).
        List<Player> AffectedPlayers = game.GetAffectedPlayers(geoTarget, torpedoType.GetBlastRadius());

        if(AffectedPlayers.size() == 1)
        {
            final Player affectedPlayer = AffectedPlayers.get(0);

            if(lLastWarnedID != affectedPlayer.GetID() && affectedPlayer.GetRespawnProtected())
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(context.getString(R.string.attacking_respawn_protected, affectedPlayer.GetName(), TextUtilities.GetTimeAmount(affectedPlayer.GetStateTimeRemaining())));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        lLastWarnedID = affectedPlayer.GetID();
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
            else if(lLastWarnedID != affectedPlayer.GetID() && game.GetNetWorthMultiplier(game.GetOurPlayer(), affectedPlayer) > Defs.ELITE_WARNING)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(context.getString(R.string.attacking_elite, affectedPlayer.GetName()));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        lLastWarnedID = affectedPlayer.GetID();
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
        else
        {
            Launch();
        }
    }

    private void Launch()
    {
        activity.InformationMode(false);
        game.LaunchTorpedo(lSiteID, lSlotNo, geoTarget, target, systemType);
    }

    public void LocationSelected(GeoCoord geoLocation, Polyline targetTrajectory, GoogleMap map)
    {
        geoTarget = geoLocation;
        this.targetTrajectory = targetTrajectory;
        this.map = map;

        //Spawn a thread to check for friendly fire, so as not to hang the UI thread.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean bThreatensFriendlies = game.ThreatensFriendlies(game.GetOurPlayerID(), geoTarget, torpedoType, false);

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

    public void TargetSelected(MapEntity target, Polyline targetTrajectory, GoogleMap map)
    {
        if(target instanceof NavalVessel)
        {
            this.target = target.GetPointer();
            this.targetTrajectory = targetTrajectory;
            geoTarget = target.GetPosition();
            this.map = map;

            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(game.EntityIsFriendly(target, game.GetOurPlayer()))
                    {
                        txtFriendlyFire.setVisibility(VISIBLE);
                    }
                    else
                    {
                        txtFriendlyFire.setVisibility(GONE);
                    }
                }
            });

            Update();
        }
        else
        {
            LocationSelected(target.GetPosition(), targetTrajectory, map);
        }
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
                    txtFlightTime.setVisibility(GONE);
                }
                else
                {
                    float fltDistance = geoSource.DistanceTo(geoTarget);

                    txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTimeToTarget(geoSource, geoTarget, torpedoType.GetTorpedoSpeed()))));
                    txtInstructions.setVisibility(GONE);
                    txtFlightTime.setVisibility(VISIBLE);
                    txtOutOfRange.setVisibility(fltDistance > torpedoType.GetTorpedoRange() ? VISIBLE : GONE);
                    imgHoming.setVisibility(target != null ? VISIBLE : GONE);

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
        });
    }
}
