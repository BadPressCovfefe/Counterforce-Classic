package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.views.DistancedEntityView;
import com.apps.fast.launch.views.EntityControls;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.Airplane;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.Bank;
import launch.game.entities.Armory;
import launch.game.entities.CommandPost;
import launch.game.entities.CargoTruck;
import launch.game.entities.Distributor;
import launch.game.entities.Infantry;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Processor;
import launch.game.entities.LogisticsDepot;
import launch.game.entities.Ship;
import launch.game.entities.Structure;
import launch.game.entities.Submarine;
import launch.game.entities.Tank;
import launch.game.entities.Warehouse;
import launch.game.entities.MapEntity;
import launch.game.entities.Loot;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.RadarStation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;

/**
 * Created by tobster on 20/04/20.
 */
public class MapSelectView extends LaunchView
{
    private TextView txtCalculating;
    private LinearLayout lytGroups;
    private LinearLayout lytEntities;

    private GeoCoord geoFrom;
    private GeoCoord geoTo;

    //Thread interruption, to prevent crashes if the user gets bored and dismisses the view.
    private boolean bCanInterruptSetupThread = false;
    private Thread setupThread = null;

    public MapSelectView(LaunchClientGame game, MainActivity activity, LatLng from, LatLng to)
    {
        super(game, activity, true);
        geoFrom = new GeoCoord(from.latitude, from.longitude, true);
        geoTo = new GeoCoord(to.latitude, to.longitude, true);;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_map_select, this);
        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        txtCalculating = findViewById(R.id.txtCalculating);
        lytGroups = findViewById(R.id.lytGroups);
        lytEntities = findViewById(R.id.lytEntities);

        //Spark up the comparisons on another thread as they're a bit intensive.
        setupThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final List<SentryGun> OurSentries = new ArrayList<>();
                final List<SentryGun> OurArtilleryGuns = new ArrayList<>();
                final List<SAMSite> OurSAMs = new ArrayList<>();
                final List<MissileSite> OurMissileSites = new ArrayList<>();
                final List<SAMSite> OurABMs = new ArrayList<>();
                final List<MissileSite> OurICBMSilos = new ArrayList<>();
                final List<CommandPost> OurCommandPosts = new ArrayList<>();
                final List<Airbase> OurAirbases = new ArrayList<>();
                final List<Armory> OurArmory = new ArrayList<>();
                final List<Warehouse> OurWarehouses = new ArrayList<>();
                final List<Infantry> OurInfantry = new ArrayList<>();
                final List<Ship> OurShips = new ArrayList<>();
                final List<Tank> OurTanks = new ArrayList<>();
                final List<Submarine> OurSubmarines = new ArrayList<>();
                final List<MapEntity> EverythingElse = new ArrayList<>();
                final List<LogisticsDepot> OurLogisticsDepots = new ArrayList<>();
                final List<OreMine> OurSolarPanels = new ArrayList<>();
                final List<OreMine> OurFarms = new ArrayList<>();
                final List<OreMine> OurOreMines = new ArrayList<>();
                final List<Airplane> OurFighters = new ArrayList<>();
                final List<Airplane> OurBombers = new ArrayList<>();
                final List<Airplane> OurRefuelers = new ArrayList<>();
                final List<Airplane> OurAttackAircraft = new ArrayList<>();
                final List<Airplane> OurSSBs = new ArrayList<>();
                final List<Airplane> OurMultiRoles = new ArrayList<>();
                final List<Ship> OurFrigates = new ArrayList<>();
                final List<Ship> OurDestroyers = new ArrayList<>();
                final List<Ship> OurSuperCarriers = new ArrayList<>();
                final List<Submarine> OurAttackSubs = new ArrayList<>();
                final List<Submarine> OurSSBNs = new ArrayList<>();

                for(OreMine oreMine : game.GetOreMines())
                {
                    if(oreMine.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
                    {
                        switch(oreMine.GetEntityType())
                        {
                            case SOLAR_PANEL:
                            {
                                if(oreMine.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurSolarPanels.add(oreMine);
                                }
                                else
                                {
                                    EverythingElse.add(oreMine);
                                }
                            }
                            break;

                            case FARM:
                            {
                                if(oreMine.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurFarms.add(oreMine);
                                }
                                else
                                {
                                    EverythingElse.add(oreMine);
                                }
                            }
                            break;

                            case ORE_MINE:
                            {
                                if(oreMine.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurOreMines.add(oreMine);
                                }
                                else
                                {
                                    EverythingElse.add(oreMine);
                                }
                            }
                            break;
                        }
                    }
                }

                for(Airplane aircraft : game.GetAirplanes())
                {
                    if(aircraft.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
                    {
                        switch(aircraft.GetEntityType())
                        {
                            case FIGHTER:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurFighters.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;

                            case BOMBER:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurBombers.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;

                            case REFUELER:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurRefuelers.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;

                            case ATTACK_AIRCRAFT:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurAttackAircraft.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;

                            case SSB:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurSSBs.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;

                            case MULTI_ROLE:
                            {
                                if(aircraft.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurMultiRoles.add(aircraft);
                                }
                                else
                                {
                                    EverythingElse.add(aircraft);
                                }
                            }
                            break;
                        }
                    }
                }

                for(Ship ship : game.GetShips())
                {
                    if(ship.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
                    {
                        switch(ship.GetEntityType())
                        {
                            case FRIGATE:
                            {
                                if(ship.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurFrigates.add(ship);
                                }
                                else
                                {
                                    EverythingElse.add(ship);
                                }
                            }
                            break;

                            case DESTROYER:
                            {
                                if(ship.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurDestroyers.add(ship);
                                }
                                else
                                {
                                    EverythingElse.add(ship);
                                }
                            }
                            break;

                            case SUPER_CARRIER:
                            {
                                if(ship.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurSuperCarriers.add(ship);
                                }
                                else
                                {
                                    EverythingElse.add(ship);
                                }
                            }
                            break;
                        }
                    }
                }

                for(Submarine submarine : game.GetSubmarines())
                {
                    if(submarine.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
                    {
                        switch(submarine.GetEntityType())
                        {
                            case ATTACK_SUB:
                            {
                                if(submarine.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurAttackSubs.add(submarine);
                                }
                                else
                                {
                                    EverythingElse.add(submarine);
                                }
                            }
                            break;

                            case SSBN:
                            {
                                if(submarine.GetOwnerID() == game.GetOurPlayerID())
                                {
                                    OurSSBNs.add(submarine);
                                }
                                else
                                {
                                    EverythingElse.add(submarine);
                                }
                            }
                            break;
                        }
                    }
                }

                FillPlayerOrEverythingElseContainer(game.GetNormalSentryGuns(), OurSentries, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetNormalSAMSites(), OurSAMs, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetNormalMissileSites(), OurMissileSites, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetABMSites(), OurABMs, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetICBMSilos(), OurICBMSilos, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetCommandPosts(), OurCommandPosts, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetAirbases(), OurAirbases, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetArmories(), OurArmory, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetWarehouses(), OurWarehouses, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetSubmarines(), OurSubmarines, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetShips(), OurShips, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetInfantries(), OurInfantry, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetTanks(), OurTanks, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetLogisticsDepots(), OurLogisticsDepots, EverythingElse);
                FillEverythingElseContainer(game.GetPlayers(), EverythingElse);
                FillEverythingElseContainer(game.GetMissiles(), EverythingElse);
                FillEverythingElseContainer(game.GetInterceptors(), EverythingElse);
                FillEverythingElseContainer(game.GetLoots(), EverythingElse);
                FillEverythingElseContainer(game.GetAirdrops(), EverythingElse);

                //Containers complete, onto UI, where we now cannot interrupt this thread.
                bCanInterruptSetupThread = false;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtCalculating.setVisibility(GONE);

                        List<MapEntity> AllEntities = new ArrayList<>();

                        if(OurSentries.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurSentries));
                            AllEntities.addAll(OurSentries);
                        }

                        if(OurArtilleryGuns.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurArtilleryGuns));
                            AllEntities.addAll(OurArtilleryGuns);
                        }

                        if(OurSAMs.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurSAMs));
                            AllEntities.addAll(OurSAMs);
                        }

                        if(OurMissileSites.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurMissileSites));
                            AllEntities.addAll(OurMissileSites);
                        }

                        if(OurABMs.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurABMs));
                            AllEntities.addAll(OurABMs);
                        }

                        if(OurICBMSilos.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurICBMSilos));
                            AllEntities.addAll(OurICBMSilos);
                        }

                        if(OurCommandPosts.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurCommandPosts));
                            AllEntities.addAll(OurCommandPosts);
                        }

                        if(OurTanks.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurTanks));
                            AllEntities.addAll(OurTanks);
                        }

                        if(OurAirbases.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurAirbases));
                            AllEntities.addAll(OurAirbases);
                        }

                        if(OurArmory.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurArmory));
                            AllEntities.addAll(OurArmory);
                        }

                        if(OurWarehouses.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurWarehouses));
                            AllEntities.addAll(OurWarehouses);
                        }

                        if(OurLogisticsDepots.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurLogisticsDepots));
                            AllEntities.addAll(OurLogisticsDepots);
                        }

                        if(OurSolarPanels.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurSolarPanels));
                            AllEntities.addAll(OurSolarPanels);
                        }

                        if(OurFarms.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurFarms));
                            AllEntities.addAll(OurFarms);
                        }

                        if(OurOreMines.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurOreMines));
                            AllEntities.addAll(OurOreMines);
                        }

                        if(OurFighters.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurFighters));
                            AllEntities.addAll(OurFighters);
                        }

                        if(OurBombers.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurBombers));
                            AllEntities.addAll(OurBombers);
                        }

                        if(OurRefuelers.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurRefuelers));
                            AllEntities.addAll(OurRefuelers);
                        }

                        if(OurAttackAircraft.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurAttackAircraft));
                            AllEntities.addAll(OurAttackAircraft);
                        }

                        if(OurMultiRoles.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurMultiRoles));
                            AllEntities.addAll(OurMultiRoles);
                        }

                        if(OurSSBs.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurSSBs));
                            AllEntities.addAll(OurSSBs);
                        }

                        if(OurFrigates.size() > 0)
                        {
                            lytGroups.addView(new ShipMaintenanceView(game, activity, OurFrigates));
                            AllEntities.addAll(OurFrigates);
                        }

                        if(OurDestroyers.size() > 0)
                        {
                            lytGroups.addView(new ShipMaintenanceView(game, activity, OurDestroyers));
                            AllEntities.addAll(OurDestroyers);
                        }

                        if(OurSuperCarriers.size() > 0)
                        {
                            lytGroups.addView(new ShipMaintenanceView(game, activity, OurSuperCarriers));
                            AllEntities.addAll(OurSuperCarriers);
                        }

                        if(OurAttackSubs.size() > 0)
                        {
                            lytGroups.addView(new SubmarineMaintenanceView(game, activity, OurAttackSubs));
                            AllEntities.addAll(OurAttackSubs);
                        }

                        if(OurSSBNs.size() > 0)
                        {
                            lytGroups.addView(new SubmarineMaintenanceView(game, activity, OurSSBNs));
                            AllEntities.addAll(OurSSBNs);
                        }

                        for(final MapEntity entity : EverythingElse)
                        {
                            AllEntities.add(entity);

                            DistancedEntityView nev = new DistancedEntityView(context, activity, entity, game);

                            nev.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    activity.SelectEntity(entity);
                                }
                            });

                            lytEntities.addView(nev);
                        }

                        activity.MultiSelectEntities(AllEntities);
                    }
                });
            }
        });

        bCanInterruptSetupThread = true;
        setupThread.start();
    }

    public void FillPlayerOrEverythingElseContainer(Collection entities, List OurContainer, List EverythingElse)
    {
        for(Object object : entities)
        {
            MapEntity entity = (MapEntity)object;

            if(game.EntityIsFriendly(entity, game.GetOurPlayer()) || entity.GetVisible())
            {
                if(entity.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
                {
                    if(entity.GetOwnedBy(game.GetOurPlayerID()))
                        OurContainer.add(entity);
                    else
                        EverythingElse.add(entity);
                }
            }
        }
    }

    public void FillEverythingElseContainer(Collection Structures, List EverythingElse)
    {
        for(Object object : Structures)
        {
            MapEntity entity = (MapEntity)object;

            if(entity.GetPosition().IsInsideGeoRect(geoFrom, geoTo))
            {
                if(entity.GetVisible() || entity.GetOwnedBy(game.GetOurPlayerID()))
                {
                    if(!(entity instanceof Loot))
                    {
                        EverythingElse.add(entity);
                    }
                }
            }
        }
    }

    @Override
    public void Update()
    {

    }

    @Override
    protected void Finish(boolean bClearSelectedEntity)
    {
        super.Finish(bClearSelectedEntity);

        if(bCanInterruptSetupThread)
        {
            if(setupThread.isAlive())
                setupThread.stop();
        }
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        for(int i = 0; i < lytGroups.getChildCount(); i++)
        {
            View view = lytGroups.getChildAt(i);

            if(view instanceof StructureMaintenanceView)
            {
                ((StructureMaintenanceView) view).EntityUpdated(entity);
            }

            if(view instanceof AircraftMaintenanceView)
            {
                ((AircraftMaintenanceView) view).EntityUpdated(entity);
            }

            if(view instanceof TankMaintenanceView)
            {
                ((TankMaintenanceView) view).EntityUpdated(entity);
            }

            if(view instanceof ShipMaintenanceView)
            {
                ((ShipMaintenanceView) view).EntityUpdated(entity);
            }

            if(view instanceof SubmarineMaintenanceView)
            {
                ((SubmarineMaintenanceView) view).EntityUpdated(entity);
            }
        }
    }
}
