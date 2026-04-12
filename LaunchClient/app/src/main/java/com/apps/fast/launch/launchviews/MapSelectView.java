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
import launch.game.entities.ScrapYard;
import launch.game.entities.Ship;
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
                final List<ArtilleryGun> OurArtilleryGuns = new ArrayList<>();
                final List<SAMSite> OurSAMs = new ArrayList<>();
                final List<MissileSite> OurMissileSites = new ArrayList<>();
                final List<SAMSite> OurABMs = new ArrayList<>();
                final List<MissileSite> OurICBMSilos = new ArrayList<>();
                final List<OreMine> OurOreMines = new ArrayList<>();
                final List<RadarStation> OurRadarStations = new ArrayList<>();
                final List<CommandPost> OurCommandPosts = new ArrayList<>();
                final List<Airbase> OurAirbases = new ArrayList<>();
                final List<Armory> OurArmory = new ArrayList<>();
                final List<Bank> OurBanks = new ArrayList<>();
                final List<Warehouse> OurWarehouses = new ArrayList<>();
                final List<ScrapYard> OurScrapYards = new ArrayList<>();
                final List<Processor> OurProcessors = new ArrayList<>();
                final List<Distributor> OurDistributors = new ArrayList<>();
                final List<Airplane> OurAircrafts = new ArrayList<>();
                final List<Infantry> OurInfantry = new ArrayList<>();
                final List<Ship> OurShips = new ArrayList<>();
                final List<CargoTruck> OurCaravans = new ArrayList<>();
                final List<CargoTruck> OurCargoTrucks = new ArrayList<>();
                final List<CargoTruck> OurMiningTrucks = new ArrayList<>();
                final List<Tank> OurTanks = new ArrayList<>();
                final List<Tank> OurSPAAGs = new ArrayList<>();
                final List<Tank> OurMissileTanks = new ArrayList<>();
                final List<Tank> OurSAMTanks = new ArrayList<>();
                final List<Tank> OurHowitzers = new ArrayList<>();
                final List<Submarine> OurSubmarines = new ArrayList<>();
                final List<MapEntity> EverythingElse = new ArrayList<>();

                FillPlayerOrEverythingElseContainer(game.GetNormalSentryGuns(), OurSentries, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetNormalSAMSites(), OurSAMs, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetNormalMissileSites(), OurMissileSites, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetABMSites(), OurABMs, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetICBMSilos(), OurICBMSilos, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetOreMines(), OurOreMines, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetRadarStations(), OurRadarStations, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetCommandPosts(), OurCommandPosts, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetAirbases(), OurAirbases, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetArmories(), OurArmory, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetBanks(), OurBanks, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetWarehouses(), OurWarehouses, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetAirplanes(), OurAircrafts, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetSubmarines(), OurSubmarines, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetShips(), OurShips, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetInfantries(), OurInfantry, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetArtilleryGuns(), OurArtilleryGuns, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetScrapYards(), OurScrapYards, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetProcessors(), OurProcessors, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetDistributors(), OurDistributors, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetMainBattleTanks(), OurTanks, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetSPAAGs(), OurSPAAGs, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetMissileTanks(), OurMissileTanks, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetInterceptorTanks(), OurSAMTanks, EverythingElse);
                FillPlayerOrEverythingElseContainer(game.GetHowitzers(), OurHowitzers, EverythingElse);
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

                        if(OurOreMines.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurOreMines));
                            AllEntities.addAll(OurOreMines);
                        }

                        if(OurRadarStations.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurRadarStations));
                            AllEntities.addAll(OurRadarStations);
                        }

                        if(OurCommandPosts.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurCommandPosts));
                            AllEntities.addAll(OurCommandPosts);
                        }

                        if(OurScrapYards.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurScrapYards));
                            AllEntities.addAll(OurScrapYards);
                        }

                        if(OurProcessors.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurProcessors));
                            AllEntities.addAll(OurProcessors);
                        }

                        if(OurDistributors.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurDistributors));
                            AllEntities.addAll(OurDistributors);
                        }

                        if(OurTanks.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurTanks));
                            AllEntities.addAll(OurTanks);
                        }

                        if(OurSPAAGs.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurSPAAGs));
                            AllEntities.addAll(OurSPAAGs);
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

                        if(OurBanks.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurBanks));
                            AllEntities.addAll(OurBanks);
                        }

                        if(OurWarehouses.size() > 0)
                        {
                            lytGroups.addView(new StructureMaintenanceView(game, activity, OurWarehouses));
                            AllEntities.addAll(OurWarehouses);
                        }

                        if(OurAircrafts.size() > 0)
                        {
                            lytGroups.addView(new AircraftMaintenanceView(game, activity, OurAircrafts));
                            AllEntities.addAll(OurAircrafts);
                        }

                        if(OurShips.size() > 0)
                        {
                            lytGroups.addView(new ShipMaintenanceView(game, activity, OurShips));
                            AllEntities.addAll(OurShips);
                        }

                        if(OurSubmarines.size() > 0)
                        {
                            lytGroups.addView(new SubmarineMaintenanceView(game, activity, OurSubmarines));
                            AllEntities.addAll(OurSubmarines);
                        }

                        if(OurMissileTanks.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurMissileTanks));
                            AllEntities.addAll(OurMissileTanks);
                        }

                        if(OurSAMTanks.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurSAMTanks));
                            AllEntities.addAll(OurSAMTanks);
                        }

                        if(OurHowitzers.size() > 0)
                        {
                            lytGroups.addView(new TankMaintenanceView(game, activity, OurHowitzers));
                            AllEntities.addAll(OurHowitzers);
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
