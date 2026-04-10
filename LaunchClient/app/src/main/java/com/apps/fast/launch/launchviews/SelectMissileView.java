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
import launch.game.entities.ArtilleryGun;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.Tank;
import launch.game.entities.conceptuals.TerrainData;
import launch.game.systems.MissileSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;

/**
 * Created by tobster on 16/10/15.
 */
public class SelectMissileView extends LaunchView
{
    private GeoCoord geoTarget;
    private MapEntity entityTarget;
    private LinearLayout lytExistingSites;
    private String strTargetName;

    private TextView txtNoMissiles;
    private TextView txtNoTorpedoes;

    boolean bGeoTargetIsWater = false;

    public SelectMissileView(LaunchClientGame game, MainActivity activity, GeoCoord geoTarget, MapEntity entityTarget, String strTargetName)
    {
        super(game, activity, true);

        this.geoTarget = geoTarget;
        this.strTargetName = strTargetName;
        this.entityTarget = entityTarget;

        if(entityTarget != null)
            game.GetTerrainData(entityTarget.GetPosition());
        else
            game.GetTerrainData(geoTarget);

        Setup();
        Update();
        RebuildList();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_select_missile, this);

        ((TextView)findViewById(R.id.txtAttacking)).setText(context.getString(R.string.attacking, strTargetName));

        txtNoMissiles = (TextView)findViewById(R.id.txtNoMissiles);
        txtNoTorpedoes = (TextView)findViewById(R.id.txtNoTorpedoes);

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

                boolean bMissilesAvailable = false;

                Player ourPlayer = game.GetOurPlayer();

                for(MissileSite missileSite : game.GetMissileSites())
                {
                    if((missileSite.GetOwnerID() == ourPlayer.GetID()) && missileSite.GetOnline() && missileSite.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(missileSite.GetPosition(), missileSite.GetID(), SystemType.MISSILE_SITE))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(ArtilleryGun artillery : game.GetArtilleryGuns())
                {
                    if((artillery.GetOwnerID() == ourPlayer.GetID()) && artillery.GetOnline() && artillery.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(artillery.GetPosition(), artillery.GetID(), SystemType.ARTILLERY_GUN))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(Airplane aircraft : game.GetAirplanes())
                {
                    if(aircraft.GetOwnerID() == ourPlayer.GetID() && aircraft.HasMissiles() && aircraft.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(aircraft.GetPosition(), aircraft.GetID(), SystemType.AIRCRAFT_MISSILES))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(Tank tank : game.GetTanks())
                {
                    if(tank.GetOwnerID() == ourPlayer.GetID() && tank.HasMissiles() && tank.GetMissileSystem().ReadyToFire() && !tank.GetOnWater())
                    {
                        if(AddUIForSystem(tank.GetPosition(), tank.GetID(), SystemType.TANK_MISSILES))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(Tank tank : game.GetTanks())
                {
                    if(tank.GetOwnerID() == ourPlayer.GetID() && tank.HasArtillery() && tank.GetMissileSystem().ReadyToFire() && !tank.GetOnWater())
                    {
                        if(AddUIForSystem(tank.GetPosition(), tank.GetID(), SystemType.TANK_ARTILLERY))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(Ship ship : game.GetShips())
                {
                    if(ship.GetOwnerID() == ourPlayer.GetID() && ship.HasMissiles() && ship.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(ship.GetPosition(), ship.GetID(), SystemType.SHIP_MISSILES))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                for(Submarine submarine : game.GetSubmarines())
                {
                    if(submarine.GetOwnerID() == ourPlayer.GetID() && submarine.HasMissiles() && submarine.GetMissileSystem().ReadyToFire() && !submarine.Submerged())
                    {
                        if(AddUIForSystem(submarine.GetPosition(), submarine.GetID(), SystemType.SUBMARINE_MISSILES))
                        {
                            bMissilesAvailable = true;
                        }
                    }

                    if(submarine.GetOwnerID() == ourPlayer.GetID() && submarine.HasICBMs() && submarine.GetICBMSystem().ReadyToFire() && !submarine.Submerged())
                    {
                        if(AddUIForSystem(submarine.GetPosition(), submarine.GetID(), SystemType.SUBMARINE_ICBM))
                        {
                            bMissilesAvailable = true;
                        }
                    }
                }

                if(entityTarget instanceof NavalVessel || bGeoTargetIsWater)
                {
                    boolean bTorpedoesAvailable = false;

                    for(Ship ship : game.GetShips())
                    {
                        if(ship.GetOwnerID() == ourPlayer.GetID() && ship.HasTorpedoes() && ship.GetTorpedoSystem().ReadyToFire())
                        {
                            if(AddUIForSystem(ship.GetPosition(), ship.GetID(), SystemType.SHIP_TORPEDOES))
                            {
                                bTorpedoesAvailable = true;
                            }
                        }
                    }

                    for(Submarine submarine : game.GetSubmarines())
                    {
                        if(submarine.GetOwnerID() == ourPlayer.GetID() && submarine.HasTorpedoes() && submarine.GetTorpedoSystem().ReadyToFire())
                        {
                            if(AddUIForSystem(submarine.GetPosition(), submarine.GetID(), SystemType.SUBMARINE_TORPEDO))
                            {
                                bTorpedoesAvailable = true;
                            }
                        }
                    }

                    txtNoTorpedoes.setVisibility(bTorpedoesAvailable ? GONE : VISIBLE);
                }

                txtNoMissiles.setVisibility(bMissilesAvailable ? GONE : VISIBLE);
            }
        });
    }

    private boolean AddUIForSystem(GeoCoord geoFrom, final int lSiteID, SystemType systemType)
    {
        TextView txtSiteName = new TextView(context);
        MissileSystem system = null;
        boolean bTorpedoes = false;

        switch(systemType)
        {
            case MISSILE_SITE:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetMissileSite(lSiteID)));
                system = game.GetMissileSite(lSiteID).GetMissileSystem();
            }
            break;

            case ARTILLERY_GUN:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetArtilleryGun(lSiteID)));
                system = game.GetArtilleryGun(lSiteID).GetMissileSystem();
            }
            break;

            case TANK_MISSILES:
            case TANK_ARTILLERY:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetTank(lSiteID)));
                system = game.GetTank(lSiteID).GetMissileSystem();
            }
            break;

            case AIRCRAFT_MISSILES:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetAirplane(lSiteID)));
                system = game.GetAirplane(lSiteID).GetMissileSystem();
            }
            break;

            case SHIP_MISSILES:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lSiteID)));
                system = game.GetShip(lSiteID).GetMissileSystem();
            }
            break;

            case SUBMARINE_MISSILES:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetSubmarine(lSiteID)));
                system = game.GetSubmarine(lSiteID).GetMissileSystem();
            }
            break;

            case SUBMARINE_ICBM:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetSubmarine(lSiteID)));
                system = game.GetSubmarine(lSiteID).GetICBMSystem();
            }
            break;

            case SHIP_TORPEDOES:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lSiteID)));
                system = game.GetShip(lSiteID).GetTorpedoSystem();
                bTorpedoes = true;
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lSiteID)));
                system = game.GetSubmarine(lSiteID).GetTorpedoSystem();
                bTorpedoes = true;
            }
            break;
        }

        lytExistingSites.addView(txtSiteName);

        List<Integer> SuitableTypes = new ArrayList<>();

        boolean bMissilesAvailable = false;

        if(system != null)
        {
            for(int c = 0; c < system.GetSlotCount(); c++)
            {
                if(system.GetSlotReadyToFire(c))
                {
                    int lType = system.GetSlotMissileType(c);

                    if(!SuitableTypes.contains(lType))
                    {
                        SuitableTypes.add(lType);

                        if(bTorpedoes)
                        {
                            TorpedoType type = game.GetConfig().GetTorpedoType(system.GetSlotMissileType(c));

                            //Check it's in range.
                            if(geoFrom.DistanceTo(geoTarget) <= type.GetTorpedoRange())
                            {
                                bMissilesAvailable = true;

                                LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type, true);

                                //Count number of this type that are ready.
                                int lNumber = 0;
                                for(int lSlot = 0; lSlot < system.GetSlotCount(); lSlot++)
                                {
                                    if(system.GetSlotReadyToFire(lSlot))
                                        if(system.GetSlotMissileType(lSlot) == lType)
                                            lNumber++;
                                }
                                launchableSelectionView.SetNumber(lNumber);

                                final int lSlotNo = c;

                                launchableSelectionView.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.DesignateTorpedoTarget(lSiteID, lSlotNo, geoTarget, entityTarget, systemType);
                                    }
                                });

                                lytExistingSites.addView(launchableSelectionView);
                            }
                        }
                        else
                        {
                            MissileType type = game.GetConfig().GetMissileType(system.GetSlotMissileType(c));
                            boolean bRequireAntiSub = false;

                            if(entityTarget != null && entityTarget instanceof Submarine && ((Submarine)entityTarget).Submerged())
                            {
                                bRequireAntiSub = true;
                            }

                            //Check it's in range.
                            if((geoFrom.DistanceTo(geoTarget) <= game.GetConfig().GetMissileRange(type)) && (!bRequireAntiSub || type.GetAntiSubmarine()))
                            {
                                bMissilesAvailable = true;
                                LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type, geoTarget, geoFrom, systemType);

                                //Count number of this type that are ready.
                                int lNumber = 0;
                                for(int lSlot = 0; lSlot < system.GetSlotCount(); lSlot++)
                                {
                                    if(system.GetSlotReadyToFire(lSlot))
                                        if(system.GetSlotMissileType(lSlot) == lType)
                                            lNumber++;
                                }
                                launchableSelectionView.SetNumber(lNumber);

                                final int lSlotNo = c;

                                launchableSelectionView.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.DesignateMissileTarget(lSiteID, lSlotNo, geoTarget, entityTarget, systemType);
                                    }
                                });

                                lytExistingSites.addView(launchableSelectionView);
                            }
                        }
                    }
                }
            }
        }

        if(!bMissilesAvailable)
        {
            lytExistingSites.removeView(txtSiteName);
            /*TextView textNone = new TextView(context);
            textNone.setText(context.getString(R.string.none));
            textNone.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            lytExistingSites.addView(textNone);*/
        }

        return bMissilesAvailable;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof TerrainData)
        {
            bGeoTargetIsWater = ((TerrainData)entity).GetWater();
            RebuildList();
        }

        //I'm not sure if this auto-updating list feature is good. In testing it seems more like a nuisance.
        /*if(entity.GetOwnedBy(game.GetOurPlayerID()) && (entity.ApparentlyEquals(game.GetOurPlayer()) || entity instanceof MissileSite || (entity instanceof Airplane && ((Airplane)entity).CanHaveMissiles())))
        {
            RebuildList();
        }*/
    }
}
