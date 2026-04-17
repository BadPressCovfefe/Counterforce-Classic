package com.apps.fast.launch.launchviews;

import static launch.utilities.LaunchLog.LogType.APPLICATION;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
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
import launch.game.entities.ArtilleryGun;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.entities.Submarine;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.MissileSystem;
import launch.game.types.MissileType;
import launch.game.systems.LaunchSystem.SystemType;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchLog;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomMissileTarget extends LaunchView
{
    private TextView txtHeading;
    private TextView txtInstructions;
    private TextView txtOutOfRange;
    private TextView txtMissileName;
    private TextView txtFlightTime;
    private TextView txtFriendlyFire;
    private LinearLayout btnFire;
    private LinearLayout btnCancel;
    private LinearLayout btnToggleFuze;
    private ImageView imgTracking;
    private ImageView imgFuze;

    private boolean bFromPlayer = false;
    private int lSiteID;
    private int lSlotNo;
    private MissileType missileType;

    private GeoCoord geoTarget = null;
    private MapEntity target;

    private GoogleMap map;
    private Polyline targetTrajectory;
    private TextView txtElectricityCost;
    private LinearLayout lytElectricityCost;
    private SystemType systemType;
    private GeoCoord geoSource = null;
    private boolean bAirburst = false;
    private MissileSystem system;

    //To not repeat elite/noob attack warnings.
    private static int lLastWarnedID = -1;

    public BottomMissileTarget(LaunchClientGame game, MainActivity activity, int lSiteID, int lSlotNo, SystemType systemType)
    {
        super(game, activity, true);
        this.lSiteID = lSiteID;
        this.lSlotNo = lSlotNo;
        this.systemType = systemType;

        switch(systemType)
        {
            case MISSILE_SITE:
            {
                geoSource = game.GetMissileSite(lSiteID).GetPosition();
                system = game.GetMissileSite(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetMissileSite(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case ARTILLERY_GUN:
            {
                geoSource = game.GetArtilleryGun(lSiteID).GetPosition();
                system = game.GetArtilleryGun(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetArtilleryGun(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case AIRCRAFT_MISSILES:
            {
                geoSource = game.GetAirplane(lSiteID).GetPosition();
                system = game.GetAirplane(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetAirplane(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case TANK_MISSILES:
            case TANK_ARTILLERY:
            {
                geoSource = game.GetTank(lSiteID).GetPosition();
                system = game.GetTank(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetTank(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SHIP_MISSILES:
            {
                geoSource = game.GetShip(lSiteID).GetPosition();
                system = game.GetShip(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetShip(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SHIP_ARTILLERY:
            {
                geoSource = game.GetShip(lSiteID).GetPosition();
                system = game.GetShip(lSiteID).GetArtillerySystem();
                missileType = game.GetConfig().GetMissileType(game.GetShip(lSiteID).GetArtillerySystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SUBMARINE_MISSILES:
            {
                geoSource = game.GetSubmarine(lSiteID).GetPosition();
                system = game.GetSubmarine(lSiteID).GetMissileSystem();
                missileType = game.GetConfig().GetMissileType(game.GetSubmarine(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SUBMARINE_ICBM:
            {
                geoSource = game.GetSubmarine(lSiteID).GetPosition();
                system = game.GetSubmarine(lSiteID).GetICBMSystem();
                missileType = game.GetConfig().GetMissileType(game.GetSubmarine(lSiteID).GetICBMSystem().GetSlotMissileType(lSlotNo));
            }
            break;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_missile_target, this);

        txtHeading = findViewById(R.id.txtHeading);
        txtInstructions = findViewById(R.id.txtInstructions);
        txtOutOfRange = findViewById(R.id.txtOutOfRange);
        txtMissileName = findViewById(R.id.txtMissileName);
        txtFlightTime = findViewById(R.id.txtFlightTime);
        txtFriendlyFire = findViewById(R.id.txtFriendlyFire);
        btnFire = findViewById(R.id.btnFire);
        btnCancel = findViewById(R.id.btnCancel);
        imgTracking = findViewById(R.id.imgTracking);
        btnToggleFuze = findViewById(R.id.btnToggleFuze);
        imgFuze = findViewById(R.id.imgFuze);
        lytElectricityCost = findViewById(R.id.lytElectricityCost);
        txtElectricityCost = findViewById(R.id.txtElectricityCost);

        if(missileType != null)
        {
            txtMissileName.setText(missileType.GetName());

            if(missileType.GetNuclear() && MissileStats.GetBlastRadius(missileType, bAirburst) > 0.001f && !missileType.GetAntiSubmarine())
            {
                btnToggleFuze.setVisibility(VISIBLE);
                imgFuze.setImageDrawable(context.getDrawable(bAirburst ? R.drawable.button_airburst : R.drawable.button_groundburst));

                btnToggleFuze.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        bAirburst = !bAirburst;
                        activity.bAirburst = !activity.bAirburst;

                        if(geoTarget != null)
                            activity.TargetMissile(geoTarget, null, bAirburst);
                    }
                });
            }
            else
            {
                btnToggleFuze.setVisibility(GONE);
            }
        }

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
                else if(geoSource.DistanceTo(geoTarget) > game.GetConfig().GetMissileRange(missileType))
                {
                    //Target out of range.
                    activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                }
                else if(target instanceof Submarine && ((Submarine)target).Submerged() && !missileType.GetAntiSubmarine())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.attack_submerged_submarine_confirm));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            SecondStateFiringChecks();
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
                            SecondStateFiringChecks();
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
        List<Player> AffectedPlayers = game.GetAffectedPlayers(geoTarget, MissileStats.GetBlastRadius(missileType, bAirburst));

        if(AffectedPlayers.size() == 1)
        {
            final Player affectedPlayer = AffectedPlayers.get(0);

            if(game.WouldBeFriendlyFire(game.GetOurPlayer(), affectedPlayer))
            {
                activity.ShowBasicOKDialog(context.getString(R.string.deliberate_friendly_fire));
            }
            else if(game.GetAttackIsBullying(game.GetOurPlayer(), affectedPlayer))
            {
                if(game.GetOurPlayer().GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD)
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_elites, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
                else
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_noobs, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
            }
            else if(lLastWarnedID != affectedPlayer.GetID() && affectedPlayer.GetRespawnProtected())
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(context.getString(R.string.attacking_respawn_protected, affectedPlayer.GetName(), TextUtilities.GetTimeAmount(affectedPlayer.GetStateTimeRemaining())));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        lLastWarnedID = affectedPlayer.GetID();
                        Launch();
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
            else if(lLastWarnedID != affectedPlayer.GetID() && game.GetNetWorthMultiplier(game.GetOurPlayer(), affectedPlayer) > Defs.ELITE_WARNING)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(context.getString(R.string.attacking_elite, affectedPlayer.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        lLastWarnedID = affectedPlayer.GetID();
                        Launch();
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
        game.LaunchMissile(lSiteID, lSlotNo, geoTarget, target != null ? target.GetPointer() : null, systemType, bAirburst);
    }

    public void LocationSelected(GeoCoord geoLocation, Polyline targetTrajectory, GoogleMap map)
    {
        geoTarget = geoLocation;
        this.targetTrajectory = targetTrajectory;
        this.map = map;
        this.target = null;

        //Spawn a thread to check for friendly fire, so as not to hang the UI thread.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean bThreatensFriendlies = game.ThreatensFriendlies(game.GetOurPlayerID(), geoTarget, missileType, false, false, bAirburst);

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtFriendlyFire.setVisibility(bThreatensFriendlies ? VISIBLE : GONE);
                    }
                });

                if(!bThreatensFriendlies)
                {
                    final boolean bBullying = game.IsBullyingOrNuisance(game.GetOurPlayerID(), geoTarget, missileType, false, false, bAirburst);

                    activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            txtFriendlyFire.setVisibility(bBullying ? VISIBLE : GONE);

                            if(bBullying)
                                txtFriendlyFire.setText(context.getString(R.string.player_size_difference_warning));
                        }
                    });
                }
            }
        }).start();

        Update();
    }

    public void TargetSelected(MapEntity target, Polyline targetTrajectory, GoogleMap map)
    {
        this.target = target;
        this.targetTrajectory = targetTrajectory;
        this.map = map;
        this.geoTarget = target.GetPosition();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(game.EntityIsFriendly(target, game.GetOurPlayer()))
                {
                    txtFriendlyFire.setVisibility(VISIBLE);
                }
                else if(game.GetAttackIsBullying(game.GetOurPlayer(), game.GetOwner(target)))
                {
                    txtFriendlyFire.setVisibility(VISIBLE);
                    txtFriendlyFire.setText(context.getString(R.string.player_size_difference_warning));
                }
                else if(target instanceof Submarine && ((Submarine)target).Submerged() && !missileType.GetAntiSubmarine())
                {
                    txtFriendlyFire.setText(context.getString(R.string.submarine_submerged));
                }
                else
                {
                    txtFriendlyFire.setVisibility(GONE);
                }
            }
        });

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
                if(missileType.GetNuclear())
                {
                    imgFuze.setImageDrawable(context.getDrawable(bAirburst ? R.drawable.button_airburst : R.drawable.button_groundburst));
                }

                if(geoTarget == null && target == null)
                {
                    txtInstructions.setVisibility(VISIBLE);
                    txtFlightTime.setVisibility(GONE);
                }
                else
                {
                    if(geoSource != null && geoTarget != null)
                    {
                        float fltDistance = geoSource.DistanceTo(geoTarget);
                        int lTimeToTarget = game.GetTimeToTarget(geoSource, geoTarget, game.GetConfig().GetMissileSpeed(missileType));

                        if(missileType.GetBomb())
                        {
                            if(bAirburst)
                                lTimeToTarget = Defs.BOMB_TRAVEL_TIME_AIRBURST_MS;
                            else
                                lTimeToTarget = Defs.BOMB_TRAVEL_TIME_MS;
                        }

                        txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(lTimeToTarget)));
                        txtInstructions.setVisibility(GONE);
                        txtFlightTime.setVisibility(VISIBLE);
                        txtOutOfRange.setVisibility(fltDistance > game.GetConfig().GetMissileRange(missileType) ? VISIBLE : GONE);
                        //imgTracking.setVisibility(target != null ? VISIBLE : GONE);

                        //Update trajectory.
                        List<LatLng> points = new ArrayList<LatLng>();
                        points.add(Utilities.GetLatLng(geoSource));

                        if(target != null)
                        {
                            geoTarget = target.GetPosition();
                        }

                        points.add(Utilities.GetLatLng(geoTarget));

                        targetTrajectory.setPoints(points);
                    }
                    else
                    {
                        activity.InformationMode(false);
                    }
                }
            }
        });
    }
}
