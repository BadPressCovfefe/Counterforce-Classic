package com.apps.fast.launch.launchviews;

import android.view.View;
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
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.systems.LaunchSystem.SystemType;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomInterceptorTarget extends LaunchView
{
    private TextView txtInstructions;
    private TextView txtOutOfRange;
    private TextView txtInterceptorName;
    private TextView txtFlightTime;
    private TextView txtFriendlyFire;
    private LinearLayout btnFire;
    private TextView txtTooSlow;
    private LinearLayout btnCancel;

    private boolean bFromPlayer = false;
    private int lSiteID;
    private int lSlotNo;
    private InterceptorType interceptorType;
    private MissileSystem system;

    private MapEntity target = null;
    private Polyline targetTrajectory;
    private SystemType systemType;
    private GeoCoord geoSource;

    public BottomInterceptorTarget(LaunchClientGame game, MainActivity activity, int lSiteID, int lSlotNo, SystemType systemType)
    {
        super(game, activity, true);
        this.lSiteID = lSiteID;
        this.lSlotNo = lSlotNo;
        this.systemType = systemType;

        switch(systemType)
        {
            case SAM_SITE:
            {
                geoSource = game.GetSAMSite(lSiteID).GetPosition();
                system = game.GetSAMSite(lSiteID).GetInterceptorSystem();
                interceptorType = game.GetConfig().GetInterceptorType(game.GetSAMSite(lSiteID).GetInterceptorSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case AIRCRAFT_INTERCEPTORS:
            {
                geoSource = game.GetAirplane(lSiteID).GetPosition();
                system = game.GetAirplane(lSiteID).GetInterceptorSystem();
                interceptorType = game.GetConfig().GetInterceptorType(game.GetAirplane(lSiteID).GetInterceptorSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case TANK_INTERCEPTORS:
            {
                geoSource = game.GetTank(lSiteID).GetPosition();
                system = game.GetTank(lSiteID).GetMissileSystem();
                interceptorType = game.GetConfig().GetInterceptorType(game.GetTank(lSiteID).GetMissileSystem().GetSlotMissileType(lSlotNo));
            }
            break;

            case SHIP_INTERCEPTORS:
            {
                geoSource = game.GetShip(lSiteID).GetPosition();
                system = game.GetShip(lSiteID).GetInterceptorSystem();
                interceptorType = game.GetConfig().GetInterceptorType(game.GetShip(lSiteID).GetInterceptorSystem().GetSlotMissileType(lSlotNo));
            }
            break;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_interceptor_target, this);

        txtInstructions = (TextView)findViewById(R.id.txtInstructions);
        txtOutOfRange = (TextView)findViewById(R.id.txtOutOfRange);
        txtInterceptorName = (TextView)findViewById(R.id.txtInterceptorName);
        txtFlightTime = (TextView)findViewById(R.id.txtFlightTime);
        txtFriendlyFire = (TextView)findViewById(R.id.txtFriendlyFire);
        btnFire = (LinearLayout)findViewById(R.id.btnFire);
        txtTooSlow = (TextView)findViewById(R.id.txtTooSlow);
        btnCancel = (LinearLayout)findViewById(R.id.btnCancel);

        txtInterceptorName.setText(interceptorType.GetName());

        btnFire.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(target == null)
                {
                    //Target not selected.
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_interceptor_target));
                }
                /*else if(game.GetAttackIsBullying(game.GetOurPlayer(), game.GetOwner(target)))
                {
                    if(game.GetOurPlayer().GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD)
                        activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_elites, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
                    else
                        activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_noobs, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
                }*/
                else
                {
                    if(target instanceof Missile)
                    {
                        Missile targetMissile = ((Missile)target);
                        MissileType missileType = game.GetConfig().GetMissileType(targetMissile.GetType());

                        float fltDistance = geoSource.DistanceTo(targetMissile.GetPosition());

                        if(fltDistance > game.GetConfig().GetInterceptorRange(interceptorType) || (missileType.GetICBM() && !interceptorType.GetABM()))
                        {
                            //Cannot intercept.
                            activity.ShowBasicOKDialog(context.getString(R.string.cannot_intercept));
                        }
                        else
                        {
                            Launch();
                        }
                    }
                    else if(target instanceof Airplane)
                    {
                        Airplane targetAircraft = ((Airplane)target);

                        float fltDistance = geoSource.DistanceTo(targetAircraft.GetPosition());

                        if (fltDistance > game.GetConfig().GetInterceptorRange(interceptorType))
                        {
                            //Cannot intercept.
                            activity.ShowBasicOKDialog(context.getString(R.string.cannot_intercept));
                        }
                        else
                        {
                            Launch();
                        }
                    }
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

    private void Launch()
    {
        //LAUNCH!
        activity.InformationMode(false);

        if(system != null)
            system.Fire(lSlotNo);

        if(target instanceof Missile)
        {
            game.LaunchInterceptor(lSiteID, lSlotNo, target.GetID(), EntityType.MISSILE, systemType);
        }
        else if(target instanceof Airplane)
        {
            game.LaunchInterceptor(lSiteID, lSlotNo, target.GetID(), EntityType.AIRPLANE, systemType);
        }
    }

    public void TargetSelected(MapEntity targetEntity, Polyline targetTrajectory, GoogleMap map)
    {
        this.target = targetEntity;
        this.targetTrajectory = targetTrajectory;
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

                if(target == null)
                {
                    txtInstructions.setVisibility(VISIBLE);
                    txtFlightTime.setVisibility(GONE);
                }
                else
                {
                    if(target instanceof Missile)
                    {
                        Missile targetMissile = ((Missile)target);

                        MissileType missileType = game.GetConfig().GetMissileType(targetMissile.GetType());
                        GeoCoord geoMissileTarget = game.GetMissileTarget(targetMissile);
                        GeoCoord geoIntercept = targetMissile.GetPosition().InterceptPoint(geoMissileTarget, game.GetConfig().GetMissileSpeed(missileType), geoSource, game.GetConfig().GetInterceptorSpeed(interceptorType));
                        long oInterceptTime = game.GetTimeToTarget(geoSource, geoIntercept, game.GetConfig().GetInterceptorSpeed(interceptorType));
                        float fltDistance = geoSource.DistanceTo(targetMissile.GetPosition());

                        txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(oInterceptTime)));
                        txtInstructions.setVisibility(GONE);
                        txtFlightTime.setVisibility(VISIBLE);
                        txtOutOfRange.setVisibility(fltDistance > game.GetConfig().GetInterceptorRange(interceptorType) ? VISIBLE : GONE);
                        txtTooSlow.setVisibility(interceptorType.GetInterceptorSpeed() < missileType.GetMissileSpeed() ? VISIBLE : GONE);

                        if(game.GetOurPlayerID() == targetMissile.GetOwnerID())
                        {
                            txtFriendlyFire.setText(context.getString(R.string.friendly_fire_warning));
                            txtFriendlyFire.setVisibility(VISIBLE);
                        }
                        else
                        {
                            txtFriendlyFire.setVisibility(GONE);
                        }
                    }
                    else if(target instanceof Airplane)
                    {
                        Airplane targetAircraft = ((Airplane)target);

                        float fltDistance = geoSource.DistanceTo(targetAircraft.GetPosition());

                        txtInstructions.setVisibility(GONE);
                        txtFlightTime.setVisibility(GONE);
                        txtOutOfRange.setVisibility(fltDistance > game.GetConfig().GetInterceptorRange(interceptorType) ? VISIBLE : GONE);

                        if(game.GetOurPlayerID() == targetAircraft.GetOwnerID())
                        {
                            txtFriendlyFire.setText(context.getString(R.string.friendly_fire_warning));
                            txtFriendlyFire.setVisibility(VISIBLE);
                        }
                        else
                        {
                            txtFriendlyFire.setVisibility(GONE);
                        }
                    }

                    //Update trajectory.
                    List<LatLng> points = new ArrayList<LatLng>();
                    points.add(Utilities.GetLatLng(geoSource));
                    points.add(Utilities.GetLatLng(target.GetPosition()));
                    targetTrajectory.setPoints(points);
                }
            }
        });
    }
}
