package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchableSelectionView;

import java.util.ArrayList;
import java.util.List;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Missile;
import launch.game.entities.Player;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Tank;
import launch.game.systems.MissileSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

public class SelectInterceptorView extends LaunchView
{
    private int lTargetID;
    private LinearLayout lytExistingSites;
    private String strTargetName;

    private LaunchEntity targetEntity;

    private TextView txtNoMissiles;

    public SelectInterceptorView(LaunchClientGame game, MainActivity activity, int lTargetID, String strTargetName, LaunchEntity targetEntity)
    {
        super(game, activity, true);

        this.lTargetID = lTargetID;
        this.strTargetName = strTargetName;
        this.targetEntity = targetEntity;

        Setup();
        //Update();
        RebuildList();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_select_interceptor, this);

        ((TextView)findViewById(R.id.txtAttacking)).setText(context.getString(R.string.engaging, strTargetName));

        txtNoMissiles = (TextView)findViewById(R.id.txtNoMissiles);

        //Existing missile sites.
        lytExistingSites = (LinearLayout)findViewById(R.id.lytAvailableMissiles);
    }

    @Override
    public void Update()
    {
        //RebuildList();
    }

    private void RebuildList()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytExistingSites.removeAllViews();

                boolean bInterceptorsAvailable = false;
                LaunchEntity target = targetEntity;

                Player ourPlayer = game.GetOurPlayer();

                for(final SAMSite samSite : game.GetSAMSites())
                {
                    if((samSite.GetOwnerID() == ourPlayer.GetID()) && samSite.GetOnline() && samSite.GetInterceptorSystem().ReadyToFire())
                    {
                        MissileSystem missileSystem = samSite.GetInterceptorSystem();

                        if(AddUIForSystem(samSite.GetPosition(), target, missileSystem, false, samSite.GetID(), SystemType.SAM_SITE))
                        {
                            bInterceptorsAvailable = true;
                        }
                    }
                }

                for(final Airplane aircraft : game.GetAirplanes())
                {
                    if(aircraft.GetOwnerID() == ourPlayer.GetID() && aircraft.HasInterceptors() && aircraft.GetInterceptorSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(aircraft.GetPosition(), target, aircraft.GetInterceptorSystem(), false, aircraft.GetID(), SystemType.AIRCRAFT_INTERCEPTORS))
                        {
                            bInterceptorsAvailable = true;
                        }
                    }
                }

                for(Tank tank : game.GetTanks())
                {
                    if(tank.GetOwnerID() == ourPlayer.GetID() && tank.HasInterceptors() && tank.GetMissileSystem().ReadyToFire() && !tank.GetOnWater())
                    {
                        if(AddUIForSystem(tank.GetPosition(), target, tank.GetMissileSystem(), false, tank.GetID(), SystemType.TANK_INTERCEPTORS))
                        {
                            bInterceptorsAvailable = true;
                        }
                    }
                }

                for(Ship ship : game.GetShips())
                {
                    if(ship.GetOwnerID() == ourPlayer.GetID() && ship.HasInterceptors() && ship.GetInterceptorSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(ship.GetPosition(), target, ship.GetInterceptorSystem(), false, ship.GetID(), SystemType.SHIP_INTERCEPTORS))
                        {
                            bInterceptorsAvailable = true;
                        }
                    }
                }

                txtNoMissiles.setVisibility(bInterceptorsAvailable ? GONE : VISIBLE);
            }
        });
    }

    private boolean AddUIForSystem(GeoCoord geoFrom, final LaunchEntity targetEntity, MissileSystem system, final boolean bIsPlayer, final int lSAMSiteID, SystemType systemType)
    {
        TextView txtSiteName = new TextView(context);

        if(systemType == SystemType.SAM_SITE)
            txtSiteName.setText(bIsPlayer ? game.GetOurPlayer().GetName() : Utilities.GetEntityName(context, game.GetSAMSite(lSAMSiteID)));
        else if(systemType == SystemType.SHIP_INTERCEPTORS)
            txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lSAMSiteID)));
        else
            txtSiteName.setVisibility(GONE);

        lytExistingSites.addView(txtSiteName);

        List<Integer> SuitableTypes = new ArrayList<>();

        boolean bInterceptorsAvailable = false;

        for(int c = 0; c < system.GetSlotCount(); c++)
        {
            if(system.GetSlotReadyToFire(c))
            {
                int lType = system.GetSlotMissileType(c);

                if(!SuitableTypes.contains(lType))
                {
                    SuitableTypes.add(lType);
                    InterceptorType type = game.GetConfig().GetInterceptorType(lType);

                    if(targetEntity instanceof Missile)
                    {
                        Missile missile = ((Missile)targetEntity);
                        MissileType missileType = game.GetConfig().GetMissileType(missile.GetType());
                        GeoCoord geoMissileTarget = game.GetMissileTarget(missile);

                        if((type.GetABM() && missileType.GetICBM()) || (!type.GetABM() && !missileType.GetICBM()))
                        {
                            //Check it's in range.
                            if(geoFrom.DistanceTo(missile.GetPosition()) <= game.GetConfig().GetInterceptorRange(type))
                            {
                                GeoCoord geoIntercept = missile.GetPosition().InterceptPoint(geoMissileTarget, game.GetConfig().GetMissileSpeed(missileType), geoFrom, game.GetConfig().GetInterceptorSpeed(type));
                                long oTimeToIntercept = game.GetTimeToTarget(geoFrom, geoIntercept, game.GetConfig().GetInterceptorSpeed(type));

                                //Check it can get to it in time and is fast enough.
                                if(oTimeToIntercept < game.GetTimeToTarget(missile))
                                {
                                    bInterceptorsAvailable = true;

                                    final LaunchableSelectionView interceptorSelectionView = new LaunchableSelectionView(activity, game, type, oTimeToIntercept, lSAMSiteID, true);

                                    //Count number of this type that are ready.
                                    int lNumber = 0;
                                    for (int lSlot = 0; lSlot < system.GetSlotCount(); lSlot++)
                                    {
                                        if (system.GetSlotReadyToFire(lSlot))
                                            if (system.GetSlotMissileType(lSlot) == lType)
                                                lNumber++;
                                    }
                                    interceptorSelectionView.SetNumber(lNumber);

                                    final int lSlotNo = c;

                                    interceptorSelectionView.setOnClickListener(new OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            activity.DesignateInterceptorTarget(lSAMSiteID, lSlotNo, missile, systemType);
                                        }
                                    });

                                    lytExistingSites.addView(interceptorSelectionView);
                                }
                            }
                        }
                    }
                    else if(targetEntity instanceof Airplane)
                    {
                        Airplane aircraft = ((Airplane)targetEntity);

                        if(!type.GetABM())
                        {
                            //Check it's in range.
                            if (geoFrom.DistanceTo(aircraft.GetPosition()) <= game.GetConfig().GetInterceptorRange(type))
                            {
                                bInterceptorsAvailable = true;

                                final LaunchableSelectionView interceptorSelectionView = new LaunchableSelectionView(activity, game, type, 0, lSAMSiteID, false);

                                //Count number of this type that are ready.
                                int lNumber = 0;
                                for (int lSlot = 0; lSlot < system.GetSlotCount(); lSlot++)
                                {
                                    if (system.GetSlotReadyToFire(lSlot))
                                        if (system.GetSlotMissileType(lSlot) == lType)
                                            lNumber++;
                                }
                                interceptorSelectionView.SetNumber(lNumber);

                                final int lSlotNo = c;

                                interceptorSelectionView.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.DesignateInterceptorTarget(lSAMSiteID, lSlotNo, aircraft, systemType);
                                    }
                                });

                                lytExistingSites.addView(interceptorSelectionView);
                            }
                        }
                    }
                }
            }
        }

        if(!bInterceptorsAvailable)
        {
            lytExistingSites.removeView(txtSiteName);
            /*TextView textNone = new TextView(context);
            textNone.setText(context.getString(R.string.none));
            textNone.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            lytExistingSites.addView(textNone);*/
        }

        return bInterceptorsAvailable;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.GetOwnedBy(game.GetOurPlayerID()) && (entity.ApparentlyEquals(game.GetOurPlayer()) || entity instanceof SAMSite || (entity instanceof Airplane && ((Airplane)entity).HasInterceptors())))
        {
            RebuildList();
        }
    }
}
