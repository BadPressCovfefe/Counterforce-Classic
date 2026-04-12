/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.treaties.Treaty;
import launch.game.treaties.War;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import launch.game.entities.*;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.AircraftSystem;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import static launch.game.systems.CargoSystem.LootType.RESOURCES;
import static launch.game.systems.CargoSystem.LootType.STORED_INFANTRY;
import static launch.game.systems.CargoSystem.LootType.STORED_TANK;
import launch.game.systems.MissileSystem;
import launch.game.treaties.Affiliation;
import launch.game.treaties.AffiliationRequest;
import launch.game.treaties.SurrenderProposal;
import launch.game.types.InterceptorType;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchPerf;
import launch.utilities.MissileStats;
import launch.game.entities.AirplaneInterface;
import launch.utilities.LaunchUtilities;
import launch.utilities.StructureStats;

/**
 *
 * @author tobster
 */
public abstract class LaunchGame implements LaunchEntityListener
{
    public enum Allegiance
    {
        YOU,
        ALLY,
        AFFILIATE,
        ENEMY,
        NEUTRAL,
        PENDING_TREATY,
        UNAFFILIATED,
    }
    
    protected static final int TICK_RATE_COMMS = 20;
    //protected static final int TICK_RATE_GAME = 1000;
    protected static final int TICK_RATE_AIRCRAFT_DEFENCES = 3000;
    protected int lGameTickRate;
    
    protected static final String LOG_NAME = "Game";
    
    public static final Random random = new Random();
    
    private ScheduledFuture<?> gameTickFuture;
    
    protected ScheduledExecutorService seService;
    
    protected Config config;

    //Entities.
    protected Map<Integer, Alliance> Alliances = new ConcurrentHashMap<>();
    protected Map<Integer, Treaty> Treaties = new ConcurrentHashMap<>();
    protected Map<Integer, Player> Players = new ConcurrentHashMap<>();
    protected Map<Integer, Missile> Missiles = new ConcurrentHashMap<>();
    protected Map<Integer, Interceptor> Interceptors = new ConcurrentHashMap<>();
    protected Map<Integer, Torpedo> Torpedoes = new ConcurrentHashMap<>();
    protected Map<Integer, MissileSite> MissileSites = new ConcurrentHashMap<>();
    protected Map<Integer, SAMSite> SAMSites = new ConcurrentHashMap<>();
    protected Map<Integer, SentryGun> SentryGuns = new ConcurrentHashMap<>();
    protected Map<Integer, ArtilleryGun> ArtilleryGuns = new ConcurrentHashMap<>();
    protected Map<Integer, OreMine> OreMines = new ConcurrentHashMap<>();
    protected Map<Integer, RadarStation> RadarStations = new ConcurrentHashMap<>();
    protected Map<Integer, CommandPost> CommandPosts = new ConcurrentHashMap<>();
    protected Map<Integer, Airbase> Airbases = new ConcurrentHashMap<>();
    protected Map<Integer, Armory> Armories = new ConcurrentHashMap<>();
    protected Map<Integer, MissileFactory> MissileFactorys = new ConcurrentHashMap<>();
    protected Map<Integer, AirplaneInterface> Airplanes = new ConcurrentHashMap<>();
    protected Map<Integer, Loot> Loots = new ConcurrentHashMap<>();
    protected Map<Integer, ResourceDeposit> ResourceDeposits = new ConcurrentHashMap<>();
    protected Map<Integer, Radiation> Radiations = new ConcurrentHashMap<>();
    protected Map<Integer, Bank> Banks = new ConcurrentHashMap<>();
    protected Map<Integer, Warehouse> Warehouses = new ConcurrentHashMap<>();
    protected Map<Integer, InfantryInterface> Infantries = new ConcurrentHashMap<>();
    protected Map<Integer, CargoTruckInterface> CargoTrucks = new ConcurrentHashMap<>();
    protected Map<Integer, TankInterface> Tanks = new ConcurrentHashMap<>();
    protected Map<Integer, Distributor> Distributors = new ConcurrentHashMap<>();
    protected Map<Integer, Shipyard> Shipyards = new ConcurrentHashMap<>();
    protected Map<Integer, Processor> Processors = new ConcurrentHashMap<>();
    protected Map<Integer, ScrapYard> ScrapYards = new ConcurrentHashMap<>();
    protected Map<Integer, Ship> Ships = new ConcurrentHashMap<>();
    protected Map<Integer, Submarine> Submarines = new ConcurrentHashMap<>();
    protected Map<Integer, Rubble> Rubbles = new ConcurrentHashMap<>();
    
    protected Map<Integer, Blueprint> Blueprints = new ConcurrentHashMap<>();
    protected Map<Integer, Airdrop> Airdrops = new ConcurrentHashMap<>();
    protected Map<Integer, MissileType> MissileTypes = new ConcurrentHashMap<>();
    protected Map<Integer, InterceptorType> InterceptorTypes = new ConcurrentHashMap<>();
    protected Map<Integer, TorpedoType> TorpedoTypes = new ConcurrentHashMap<>();
    
    protected Map<Integer, Map<Integer, Player>> AllianceMemberRosters = new ConcurrentHashMap<>();  //Key of outer map is alliance ID, key of inner map is player id.
    protected Map<Integer, Map<EntityType, Map<Integer, LaunchEntity>>> PlayerOwnedStuff = new ConcurrentHashMap<>(); //Key of outer map is player ID, key of middle map is entity type, key of innermost map is entity id.
    
    //Statistics.
    protected int lGameTickStarts = 0;
    protected int lGameTickEnds = 0;
    protected int lCommTickStarts = 0;
    protected int lCommTickEnds = 0;
    
    protected LaunchGame(Config config, int lGameTickRate)
    {
        this.config = config;
        this.lGameTickRate = lGameTickRate;
    }
    
    public void StartServices()
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Starting services...");
        
        EstablishAllStructureThreats(LaunchEntity.ID_NONE);
        
        //Services are created here, called at the end of the child constructors, rather than in this constructor; so the full stack of Launch objects is initialised & ready to be ticked..
        seService = Executors.newScheduledThreadPool(2);
        
        //Comms service.
        seService.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CommsTick(TICK_RATE_COMMS);
                }
                catch(Exception ex)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, "CommsServiceErrors", "Unhandled comms tick error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, TICK_RATE_COMMS, TimeUnit.MILLISECONDS);
        
        //Game service.
        gameTickFuture = seService.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GameTick(lGameTickRate);
                }
                catch(Exception ex)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, "GameServiceErrors", "Unhandled game tick error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, lGameTickRate, TimeUnit.MILLISECONDS);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Started.");
    }
    
    public void UpdateGameTickRate(int lNewRateMS)
    {
        lGameTickRate = lNewRateMS;

        if(gameTickFuture != null)
            gameTickFuture.cancel(false);

        gameTickFuture = seService.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GameTick(lGameTickRate);
                }
                catch(Exception ex)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, "GameServiceErrors", "Unhandled game tick error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, lGameTickRate, TimeUnit.MILLISECONDS);
    }
    
    protected abstract void CommsTick(int lMS);
    
    protected void GameTick(int lMS)
    {
        lGameTickStarts++;
        
        //Tick all entities.
        for(Player player : Players.values())
        {
            if(!player.GetAWOL())
            {
                player.Tick(lMS);
            }
        }
        
        LaunchPerf.Measure(LaunchPerf.Metric.PlayerTick);

        for(Airdrop airdrop : Airdrops.values())
        {
            if(airdrop.Arrived())
            {
                Airdrops.remove(airdrop.GetID());
                EntityRemoved(airdrop, true);
            }
            else
            {
                airdrop.Tick(lMS);
            } 
        }
        
        for(Missile missile : Missiles.values())
        {
            missile.Tick(lMS);

            //Missiles are flying until they reach their target or are shot down, after which they are removed from the game.
            if(missile.GetDetonate())
            {
                MissileExploded(missile);
                Missiles.remove(missile.GetID());
                EntityRemoved(missile, true);
            }
            else if(!missile.Flying())
            {
                Missiles.remove(missile.GetID());
                EntityRemoved(missile, true);
            }
            else if(missile.Flying())
            {
                if(missile.GetTargetEntity() != null)
                {
                    MapEntity target = missile.GetTargetEntity().GetMapEntity(this);

                    if(target != null)
                    {
                        if(!target.GetVisible())
                        {
                            target.SetVisible(GetTimeToTarget(missile));
                            EntityUpdated(target, false);
                        }
                    }
                }

                if(missile.GetPosition().MoveToward(GetMissileTarget(missile), missile.GetSpeed(), lMS))
                {
                    //Missile has reached target and exploded.
                    //missile.SetPosition(geoTarget); //(Correct explosion location).
                    missile.Detonate();
                    EntityUpdated(missile, false);
                }
                else
                {
                    EntityMoved(missile);
                }
            }
        }

        LaunchPerf.Measure(LaunchPerf.Metric.MissileTick);

        for(Interceptor interceptor : Interceptors.values())
        {
            interceptor.Tick(lMS);

            switch(interceptor.GetTargetType())
            {
                case MISSILE:
                {
                    Missile targetMissile = Missiles.get(interceptor.GetTargetID());
                    
                    if(targetMissile != null)
                    {
                        InterceptorType type = config.GetInterceptorType(interceptor.GetType());
                        
                        if(interceptor.GetPosition().MoveToIntercept(config.GetInterceptorSpeed(type), targetMissile.GetPosition(), targetMissile.GetSpeed(), GetMissileTarget(targetMissile), lMS))
                        {
                            //Interceptor has caught up with target missile.
                            InterceptorReachedTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, false);
                        }
                    }
                    else
                    {
                        InterceptorLostTarget(interceptor);
                        Interceptors.remove(interceptor.GetID());
                        EntityRemoved(interceptor, false);
                    }
                    
                }
                break;
                
                case AIRPLANE:
                {
                    Airplane targetAircraft = GetAirplane(interceptor.GetTargetID());
                    
                    if(targetAircraft != null)
                    {
                        InterceptorType type = config.GetInterceptorType(interceptor.GetType());
                        
                        if(interceptor.GetPosition().MoveToward(targetAircraft.GetPosition(), config.GetInterceptorSpeed(type), lMS))
                        {
                            //Interceptor has caught up with target missile.
                            InterceptorReachedTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, true);
                        }
                    }
                    else
                    {
                        InterceptorLostTarget(interceptor);
                        Interceptors.remove(interceptor.GetID());
                        EntityRemoved(interceptor, false);
                    }
                    
                }
                break;
                
                default: /*Do nothing.*/ break;
            }
        }

        LaunchPerf.Measure(LaunchPerf.Metric.InterceptorTick);
        
        for(Torpedo torpedo : Torpedoes.values())
        {
            torpedo.Tick(lMS);

            TorpedoType type = config.GetTorpedoType(torpedo.GetType());

            switch(torpedo.GetState())
            {
                case TRAVELLING:
                case SEEKING:
                {
                    //In this mode, the torpedo should not have an entity target. 
                    //Spin in a circle around the geoTarget.
                    GeoCoord geoTarget = torpedo.GetGeoTarget();

                    if(torpedo.GetPosition().TurnToward(geoTarget, Defs.TORPEDO_TURN_RATE, type.GetTorpedoSpeed(), lMS))
                    {
                        torpedo.GetPosition().MoveToward(geoTarget, type.GetTorpedoSpeed(), lMS);
                    }
                    
                    EntityMoved(torpedo);
                }
                break;
                
                case HOMING:
                {
                    //Torpedo is in HOMING state, so it should have a target entity that should be moved towards.
                    MapEntity entityTarget = torpedo.GetTarget().GetMapEntity(this);

                    if(entityTarget != null)
                    {
                        if(torpedo.GetPosition().TurnToward(entityTarget.GetPosition(), Defs.TORPEDO_TURN_RATE, type.GetTorpedoSpeed(), lMS))
                        {
                            torpedo.GetPosition().MoveToward(entityTarget.GetPosition(), type.GetTorpedoSpeed(), lMS);
                        }
                    }
                    
                    EntityMoved(torpedo);
                }
                break;
                
                case DETONATE:
                {
                    //TorpedoExploded.
                    TorpedoExploded(torpedo);
                    Torpedoes.remove(torpedo.GetID());
                    EntityRemoved(torpedo, false);
                }
                break;
            }
        }
        
        for(MissileSite missileSite : MissileSites.values())
        {
            missileSite.Tick(lMS);
            
            if(missileSite.Destroyed())
            {
                MissileSites.remove(missileSite.GetID());
                EntityRemoved(missileSite, true);
            }
        }
        
        for(ArtilleryGun artillery : ArtilleryGuns.values())
        {
            artillery.Tick(lMS);
            
            if(artillery.Destroyed())
            {
                ArtilleryGuns.remove(artillery.GetID());
                EntityRemoved(artillery, true);
            }
        }
        
        for(SAMSite samSite : SAMSites.values())
        {
            samSite.Tick(lMS);
            
            if(samSite.Destroyed())
            {
                SAMSites.remove(samSite.GetID());
                EntityRemoved(samSite, true);
            }
        }
        
        for(SentryGun sentryGun : SentryGuns.values())
        {
            sentryGun.Tick(lMS);
            
            if(sentryGun.Destroyed())
            {
                SentryGuns.remove(sentryGun.GetID());
                EntityRemoved(sentryGun, true);
            }
        }
        
        for(Blueprint blueprint : Blueprints.values())
        {
            blueprint.Tick(lMS);
            
            if(blueprint.Expired())
            {
                Blueprints.remove(blueprint.GetID());
                EntityRemoved(blueprint, true);
            }
        }
        
        for(MissileFactory missileFactory : MissileFactorys.values())
        {
            missileFactory.Tick(lMS);
            
            if(missileFactory.Destroyed())
            {
                MissileFactorys.remove(missileFactory.GetID());
                EntityRemoved(missileFactory, true);
            }
        }
        
        for(OreMine oreMine : OreMines.values())
        {
            oreMine.Tick(lMS);
            
            if(oreMine.Destroyed())
            {
                OreMines.remove(oreMine.GetID());
                EntityRemoved(oreMine, true);
            }
        }
        
        for(RadarStation radarStation : RadarStations.values())
        {
            radarStation.Tick(lMS);
            
            if(radarStation.Destroyed())
            {
                RadarStations.remove(radarStation.GetID());
                EntityRemoved(radarStation, true);
            }
        }
        
        for(CommandPost commandPost : CommandPosts.values())
        {
            commandPost.Tick(lMS);
            
            if(commandPost.Destroyed())
            {
                CommandPosts.remove(commandPost.GetID());
                EntityRemoved(commandPost, true);
            }
        }
        
        for(Airbase airbase : Airbases.values())
        {
            airbase.Tick(lMS);
            
            if(airbase.Destroyed())
            {
                Airbases.remove(airbase.GetID());
                EntityRemoved(airbase, true);
                AirbaseDestroyed(airbase);
            }
            
            for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
            {
                if(aircraft.Destroyed())
                {
                    airbase.GetAircraftSystem().RemoveAndGetAirplane(aircraft.GetID());
                    AirplaneDestroyed(aircraft);
                }
            }
        }
        
        for(Armory armory : Armories.values())
        {
            armory.Tick(lMS);
            
            if(armory.Destroyed())
            {
                Armories.remove(armory.GetID());
                EntityRemoved(armory, true);
            }
        }
        
        for(Bank bank : Banks.values())
        {
            bank.Tick(lMS);
            
            if(bank.Destroyed())
            {
                Banks.remove(bank.GetID());
                EntityRemoved(bank, true);
            }
        }
        
        for(Warehouse warehouse : Warehouses.values())
        {
            warehouse.Tick(lMS);
            
            if(warehouse.Destroyed())
            {
                Warehouses.remove(warehouse.GetID());
                EntityRemoved(warehouse, true);
            }
        }
        
        //Process infantry.
        for(Infantry infantry : GetInfantries())
        {
            if(infantry.Destroyed())
            {
                Infantries.remove(infantry.GetID());
                EntityRemoved(infantry, true);
            }
            else
            {
                infantry.Tick(lMS);
                
                switch(infantry.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(infantry.HasGeoCoordChain())
                        {
                            GeoCoord geoTarget = infantry.GetNextCoordinate();
                            GeoCoord geoPosition = infantry.GetPosition();
                            GeoCoord geoFirstPos = infantry.GetPosition().GetCopy();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                infantry.ReachedFirstCoordinate();

                                if(infantry.GetNextCoordinate() == null)
                                {
                                    infantry.Wait();
                                }
                            }

                            EntityMoved(infantry);
                        }
                        else if(infantry.HasGeoTarget())
                        {
                            GeoCoord geoTarget = infantry.GetGeoTarget();
                            GeoCoord geoPosition = infantry.GetPosition();
                            GeoCoord geoFirstPos = infantry.GetPosition().GetCopy();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                infantry.Wait();
                            }

                            EntityMoved(infantry);
                        }
                        else
                        {
                            infantry.Wait();
                        }
                    }
                    break;

                    case CAPTURE:
                    case LIBERATE:
                    {
                        MapEntity target = infantry.GetTarget().GetMapEntity(this);

                        if(target != null)
                        {
                            if(infantry.GetPosition().MoveToward(target.GetPosition(), Defs.LAND_UNIT_SPEED, lMS))
                            {
                                //Reached target.
                                InfantryReachedTarget(infantry);
                            }                                

                            EntityMoved(infantry);
                        }
                        else
                        {
                            infantry.Wait();
                        } 
                    }
                    break;

                    case ATTACK:
                    {
                        MapEntity target = infantry.GetTarget().GetMapEntity(this);

                        if(target != null)
                        {
                            GeoCoord geoTarget = target.GetPosition().GetCopy();
                            GeoCoord geoPosition = infantry.GetPosition();

                            if(infantry.GetPosition().DistanceTo(geoTarget) > Defs.INFANTRY_COMBAT_RANGE)
                            {
                                geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS);
                            }

                            EntityMoved(infantry);
                        }
                    }
                    break;

                    case WAIT:
                    {
                        infantry.DefendPosition();
                        EntityUpdated(infantry, false);
                    }
                    break;
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.Destroyed() || truck.GetRemove())
            {
                CargoTrucks.remove(truck.GetID());
                EntityRemoved(truck, true);
            }
            else
            {
                truck.Tick(lMS);
                
                switch(truck.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(truck.HasGeoCoordChain())
                        {
                            GeoCoord geoTarget = truck.GetNextCoordinate();
                            GeoCoord geoPosition = truck.GetPosition();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                truck.ReachedFirstCoordinate();

                                if(truck.GetNextCoordinate() == null)
                                {
                                    truck.Wait();
                                }
                            }
                            else
                            {
                                truck.UseFuel(Defs.GetFuelUsagePerTick(lMS, Defs.CARGO_TRUCK_RANGE, Defs.LAND_UNIT_SPEED));
                            }

                            EntityMoved(truck);
                        }
                        else if(truck.HasGeoTarget())
                        {
                            GeoCoord geoTarget = truck.GetGeoTarget();
                            GeoCoord geoPosition = truck.GetPosition();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                truck.Wait();
                            }
                            else
                            {
                                truck.UseFuel(Defs.GetFuelUsagePerTick(lMS, Defs.CARGO_TRUCK_RANGE, Defs.LAND_UNIT_SPEED));
                            }

                            EntityMoved(truck);
                        }
                        else
                        {
                            truck.Wait();
                        }
                    }
                    break;

                    case WAIT:
                    {
                        //Do nothing.
                    }
                    break;
                }
            }
        }
        
        for(Processor processor : GetProcessors())
        {
            if(!processor.Destroyed())
            {
                processor.Tick(lMS);
            }
            else
            {
                Processors.remove(processor.GetID());
                EntityRemoved(processor, true);
            }
        }
        
        for(Distributor distributor : GetDistributors())
        {
            if(!distributor.Destroyed())
            {
                distributor.Tick(lMS);
            }
            else
            {
                Distributors.remove(distributor.GetID());
                EntityRemoved(distributor, true);
            }
        }
        
        for(ScrapYard scrapyard : GetScrapYards())
        {
            if(!scrapyard.Destroyed())
            {
                scrapyard.Tick(lMS);
            }
            else
            {
                ScrapYards.remove(scrapyard.GetID());
                EntityRemoved(scrapyard, true);
            }
        }
        
        for(Shipyard shipyard : GetShipyards())
        {
            if(!shipyard.Destroyed())
            {
                shipyard.Tick(lMS);
            }
        }
        
        for(Tank tank : GetTanks())
        {
            if(tank.Destroyed() || tank.GetRemove())
            {
                Tanks.remove(tank.GetID());
                EntityRemoved(tank, true);
            }
            else
            {
                tank.Tick(lMS);
                
                switch(tank.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(tank.HasGeoCoordChain())
                        {
                            GeoCoord geoTarget = tank.GetNextCoordinate();
                            GeoCoord geoPosition = tank.GetPosition();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                tank.ReachedFirstCoordinate();

                                if(tank.GetNextCoordinate() == null)
                                {
                                    tank.DefendPosition();
                                }
                            }
                            else
                            {
                                tank.UseFuel(Defs.GetFuelUsagePerTick(lMS, Defs.TANK_RANGE, Defs.LAND_UNIT_SPEED));
                            }

                            EntityMoved(tank);
                        }
                        else if(tank.HasGeoTarget())
                        {
                            GeoCoord geoTarget = tank.GetGeoTarget();
                            GeoCoord geoPosition = tank.GetPosition();

                            if(geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS))
                            {
                                tank.DefendPosition();
                            }
                            else
                            {
                                tank.UseFuel(Defs.GetFuelUsagePerTick(lMS, Defs.TANK_RANGE, Defs.LAND_UNIT_SPEED));
                            }

                            EntityMoved(tank);
                        }
                        else
                        {
                            tank.Wait();
                        }
                    }
                    break;

                    case ATTACK:
                    {
                        if(tank.GetTarget() != null)
                        {
                            //Attack the targeted entity. If you are in firing range, do not move, and also do not HoldPosition(). Doing so would guarantee death.
                            MapEntity target = tank.GetTarget().GetMapEntity(this);

                            if(target != null)
                            {
                                GeoCoord geoTarget = target.GetPosition().GetCopy();
                                GeoCoord geoPosition = tank.GetPosition();

                                if(tank.GetPosition().DistanceTo(geoTarget) > Defs.BATTLE_TANK_FIRING_RANGE)
                                {
                                    geoPosition.MoveToward(geoTarget, Defs.LAND_UNIT_SPEED, lMS);
                                    tank.UseFuel(Defs.GetFuelUsagePerTick(lMS, Defs.TANK_RANGE, Defs.LAND_UNIT_SPEED));
                                    EntityMoved(tank);
                                }
                            }
                        }  
                        else
                        {
                            tank.DefendPosition();
                        }
                    }
                    break;
                } 
            }
        }
        
        //Process ships.
        for(Ship ship : GetShips())
        {
            if(ship.Destroyed())
            {
                Ships.remove(ship.GetID());
                EntityRemoved(ship, true);
                ShipDestroyed(ship);
            }
            else
            {
                ship.Tick(lMS);
                
                if(ship.HasAircraft())
                {
                    for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.Destroyed())
                        {
                            ship.GetAircraftSystem().RemoveAndGetAirplane(aircraft.GetID());
                            AirplaneDestroyed(aircraft);
                        }
                    }
                } 
                
                switch(ship.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(ship.HasFuelRemaining())
                        {
                            if(ship.HasGeoCoordChain())
                            {
                                GeoCoord geoTarget = ship.GetNextCoordinate();
                                GeoCoord geoPosition = ship.GetPosition();

                                if(geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS))
                                {
                                    ship.ReachedFirstCoordinate();
                                    
                                    if(ship.GetNextCoordinate() == null)
                                    {
                                        ship.Wait();
                                    }
                                }
                                
                                EntityMoved(ship);

                                if(!ship.GetNuclear())
                                {
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                    ship.UseFuel(fltFuelUsed);
                                }
                            }
                            else if(ship.HasGeoTarget())
                            {
                                GeoCoord geoTarget = ship.GetGeoTarget();
                                GeoCoord geoPosition = ship.GetPosition();

                                if(geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS))
                                {
                                    ship.Wait();
                                }
                                
                                EntityMoved(ship);

                                if(!ship.GetNuclear())
                                {
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                    ship.UseFuel(fltFuelUsed);
                                }
                            }
                            else
                            {
                                ship.Wait();
                            }  
                        }
                        else
                        {
                            ship.Wait();
                        } 
                    }
                    break;
                    
                    case SEEK_FUEL:
                    {
                        if(ship.GetTarget() != null)
                        {
                            NavalVessel tanker = (NavalVessel)ship.GetTarget().GetMapEntity(this);
                            
                            if(tanker != null && tanker.GetMoveOrders() == MoveOrders.PROVIDE_FUEL)
                            {
                                GeoCoord geoTarget = tanker.GetPosition().GetCopy();
                                GeoCoord geoPosition = ship.GetPosition();
                                float fltDistance = ship.GetPosition().DistanceTo(tanker.GetPosition());

                                //If the ship is outside of refueling distance, move toward the tanker. otherwise do nothing, meaning that when the ship gets within refueling distance, it will stop.
                                if(fltDistance > Defs.SHIP_REFUELING_DISTANCE)
                                {
                                    geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS);
                                    EntityMoved(ship);

                                    if(!ship.GetNuclear())
                                    {
                                        float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                        ship.UseFuel(fltFuelUsed);
                                    }
                                }
                            }
                            else
                            {
                                ship.MoveToPosition(ship.GetGeoTarget());
                            }
                        }
                        else
                        {
                            ship.MoveToPosition(ship.GetGeoTarget());
                        } 
                    }
                    break;
                    
                    case PROVIDE_FUEL:
                    {
                        if(ship.GetTarget() != null && ship.GetCurrentFuel() > Defs.SHIP_REFUEL_RATE_PER_TICK_TONS)
                        {
                            NavalVessel receiver = (NavalVessel)ship.GetTarget().GetMapEntity(this);

                            if(receiver != null && receiver.GetMoveOrders() == MoveOrders.SEEK_FUEL)
                            {
                                if(receiver.GetMoveOrders() == MoveOrders.SEEK_FUEL)
                                {
                                    GeoCoord geoTarget = ship.GetGeoTarget();
                                    GeoCoord geoPosition = ship.GetPosition();
                                    float fltDistance = ship.GetPosition().DistanceTo(receiver.GetPosition());

                                    if(fltDistance > Defs.SHIP_REFUELING_DISTANCE)
                                    { 
                                        geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS);
                                        EntityMoved(ship);
                                        
                                        if(!ship.GetNuclear())
                                        {
                                            float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                            ship.UseFuel(fltFuelUsed);
                                        }
                                    }
                                }
                                else
                                {
                                    ship.MoveToPosition(ship.GetGeoTarget());
                                }
                            }
                            else
                            {
                                ship.MoveToPosition(ship.GetGeoTarget());
                            }
                        }   
                        else
                        {
                            ship.MoveToPosition(ship.GetGeoTarget());
                        }
                    }
                    break;
                    
                    case ATTACK:
                    {
                        //TODO: Notes in AUTOMATIC_MISSILE_FIRING.txt. Actually, since no movement is performed for submarine/ship attacks, this should only be in LaunchServerGame.
                    }
                    break;
                }
            }
        }
        
        //Process submarines.
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.Destroyed())
            {
                Submarines.remove(submarine.GetID());
                EntityRemoved(submarine, true);
            }
            else
            {                
                submarine.Tick(lMS);
                
                switch(submarine.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(submarine.HasFuelRemaining())
                        {
                            if(submarine.HasGeoCoordChain())
                            {
                                GeoCoord geoTarget = submarine.GetNextCoordinate();
                                GeoCoord geoPosition = submarine.GetPosition();

                                if(geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS))
                                {
                                    submarine.ReachedFirstCoordinate();

                                    if(submarine.GetNextCoordinate() == null)
                                    {
                                        submarine.Wait();
                                    }
                                }
                                
                                EntityMoved(submarine);

                                if(!submarine.GetNuclear())
                                {
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                    submarine.UseFuel(fltFuelUsed);
                                }
                            }
                            else if(submarine.HasGeoTarget())
                            {
                                GeoCoord geoTarget = submarine.GetGeoTarget();
                                GeoCoord geoPosition = submarine.GetPosition();

                                if(geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS))
                                {
                                    submarine.Wait();
                                }
                                
                                EntityMoved(submarine);

                                if(!submarine.GetNuclear())
                                {
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                    submarine.UseFuel(fltFuelUsed);
                                }
                            }
                            else
                            {
                                submarine.Wait();
                            }  
                        }
                        else
                        {
                            submarine.Wait();
                        } 
                    }
                    break;
                    
                    case SEEK_FUEL:
                    {
                        if(submarine.GetTarget() != null)
                        {
                            NavalVessel tanker = (NavalVessel)submarine.GetTarget().GetMapEntity(this);
                            
                            if(tanker != null && tanker.GetMoveOrders() == MoveOrders.PROVIDE_FUEL)
                            {
                                GeoCoord geoTarget = tanker.GetPosition().GetCopy();
                                GeoCoord geoPosition = submarine.GetPosition();
                                float fltDistance = submarine.GetPosition().DistanceTo(tanker.GetPosition());

                                //If the submarine is outside of refueling distance, move toward the tanker. otherwise do nothing, meaning that when the submarine gets within refueling distance, it will stop.
                                if(fltDistance > Defs.SHIP_REFUELING_DISTANCE)
                                {
                                    geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS);
                                    EntityMoved(submarine);

                                    if(!submarine.GetNuclear())
                                    {
                                        float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                        submarine.UseFuel(fltFuelUsed);
                                    }
                                }
                                else
                                {
                                    if(submarine.Submerged())
                                        submarine.DiveOrSurface();
                                }
                            }
                            else
                            {
                                submarine.MoveToPosition(submarine.GetGeoTarget());
                            }
                        }
                        else
                        {
                            submarine.MoveToPosition(submarine.GetGeoTarget());
                        } 
                    }
                    break;
                    
                    case PROVIDE_FUEL:
                    {
                        if(submarine.GetTarget() != null && submarine.GetCurrentFuel() > Defs.SHIP_REFUEL_RATE_PER_TICK_TONS)
                        {
                            NavalVessel receiver = (NavalVessel)submarine.GetTarget().GetMapEntity(this);

                            if(receiver != null && receiver.GetMoveOrders() == MoveOrders.SEEK_FUEL)
                            {
                                if(receiver.GetMoveOrders() == MoveOrders.SEEK_FUEL)
                                {
                                    GeoCoord geoTarget = submarine.GetGeoTarget();
                                    GeoCoord geoPosition = submarine.GetPosition();
                                    float fltDistance = submarine.GetPosition().DistanceTo(receiver.GetPosition());

                                    if(fltDistance > Defs.SHIP_REFUELING_DISTANCE)
                                    { 
                                        geoPosition.MoveToward(geoTarget, Defs.NAVAL_SPEED, lMS);
                                        EntityMoved(submarine);
                                        
                                        if(!submarine.GetNuclear())
                                        {
                                            float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.NAVAL_RANGE, Defs.NAVAL_SPEED);
                                            submarine.UseFuel(fltFuelUsed);
                                        }
                                    }
                                    else
                                    {
                                        if(submarine.Submerged())
                                            submarine.DiveOrSurface();
                                    }
                                }
                                else
                                {
                                    submarine.MoveToPosition(submarine.GetGeoTarget());
                                }
                            }
                            else
                            {
                                submarine.MoveToPosition(submarine.GetGeoTarget());
                            }
                        }   
                        else
                        {
                            submarine.MoveToPosition(submarine.GetGeoTarget());
                        }
                    }
                    break;
                    
                    case ATTACK:
                    {
                        //TODO: Notes in AUTOMATIC_MISSILE_FIRING.txt. Actually, since no movement is performed for submarine/ship attacks, this should only be in LaunchServerGame.
                    }
                    break;
                }
            }
        }
        
        //Process aircraft.
        for(Airplane aircraft : GetAirplanes())
        {
            if(aircraft.Destroyed())
            {
                Airplanes.remove(aircraft.GetID());
                EntityRemoved(aircraft, true);
            }
            else
            {
                aircraft.Tick(lMS);

                switch(aircraft.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(!aircraft.OutOfFuel())
                        {
                            if(aircraft.HasGeoCoordChain())
                            {
                                GeoCoord geoTarget = aircraft.GetNextCoordinate();
                                GeoCoord geoPosition = aircraft.GetPosition();

                                if(geoPosition.TurnToward(geoTarget, Defs.AIRCRAFT_TURN_RATE, Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS))
                                { 
                                    if(geoPosition.MoveToward(geoTarget, Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS))
                                    {
                                        if(aircraft.GetNextCoordinate() == null)
                                        {
                                            aircraft.MoveToPosition(geoTarget);
                                        }
                                        else
                                        {
                                            aircraft.ReachedFirstCoordinate();
                                        }
                                    }
                                }
                                
                                EntityMoved(aircraft);

                                float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                                aircraft.UseFuel(fltFuelUsed);
                            }
                            else if(aircraft.HasGeoTarget())
                            {
                                GeoCoord geoTarget = aircraft.GetGeoTarget();
                                GeoCoord geoPosition = aircraft.GetPosition();

                                if(geoPosition.TurnToward(geoTarget, Defs.AIRCRAFT_TURN_RATE, Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS))
                                { 
                                    if(geoPosition.MoveToward(geoTarget, Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS))
                                    {
                                        geoPosition.Move(geoPosition.GetLastBearing(), Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS);
                                    }
                                } 
                                
                                EntityMoved(aircraft);

                                float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                                aircraft.UseFuel(fltFuelUsed);
                            }
                            else
                            {
                                aircraft.MoveToPosition(aircraft.GetPosition().GetCopy());
                            }  
                        }
                        else
                        {
                            aircraft.MoveToPosition(aircraft.GetPosition().GetCopy());
                        } 
                    }
                    break;

                    case RETURN:
                    {
                        MapEntity entityHome = aircraft.GetHomeBase().GetMapEntity(this);
                        
                        GeoCoord geoPosition = aircraft.GetPosition();
                        GeoCoord geoHome = entityHome == null ? aircraft.GetPosition() : entityHome.GetPosition().GetCopy();
                        float fltSpeed = Defs.GetAircraftSpeed(aircraft.GetEntityType());
                        float fltTurnRate = Defs.AIRCRAFT_TURN_RATE;
                        float fltDistance = geoPosition.DistanceTo(geoHome);
                        
                        //If the aircraft is close to the homebase, reduce speed and increase turn rate so it can land effectively.
                        if(fltDistance <= Defs.LANDING_APPROACH_DISTANCE)
                        {
                            fltSpeed = Defs.LANDING_APPROACH_SPEED;
                            fltTurnRate = Defs.LANDING_APPROACH_TURN_RATE;
                        }

                        if(geoPosition.TurnToward(geoHome, fltTurnRate, fltSpeed, lMS))
                        {
                            if(geoPosition.MoveToward(geoHome, fltSpeed, lMS))
                            {
                                geoPosition.Move(geoPosition.GetLastBearing(), fltSpeed, lMS);
                            }
                        }
                        
                        EntityMoved(aircraft);

                        float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                        aircraft.UseFuel(fltFuelUsed);
                    }
                    break;
                    
                    case ATTACK:
                    {
                        if(aircraft.GetTarget() != null || aircraft.GetGeoTarget() != null)
                        {
                            GeoCoord geoTarget = null;
                            MapEntity target = null;
                            
                            if(aircraft.GetTarget() != null && aircraft.GetTarget().GetMapEntity(this) != null)
                            {
                                target = aircraft.GetTarget().GetMapEntity(this);
                                geoTarget = aircraft.GetTarget().GetMapEntity(this).GetPosition().GetCopy();
                            }
                            else if(aircraft.GetGeoTarget() != null)
                            {
                                geoTarget = aircraft.GetGeoTarget();
                            }

                            if(geoTarget != null)
                            {
                                GeoCoord geoPosition = aircraft.GetPosition();
                                float fltSpeed = Defs.GetAircraftSpeed(aircraft.GetEntityType());
                                float fltTurnRate = Defs.AIRCRAFT_TURN_RATE;
                                float fltDistance = geoPosition.DistanceTo(geoTarget);

                                //If the aircraft is close to the target, reduce speed and increase turn rate so it can attack effectively.
                                if(fltDistance <= Defs.ATTACK_APPROACH_DISTANCE)
                                {
                                    if(fltSpeed > Defs.ATTACK_APPROACH_SPEED)
                                        fltSpeed = Defs.ATTACK_APPROACH_SPEED;
                                }
                                
                                if(target != null)
                                {
                                    if(target instanceof Missile)
                                    {
                                        MissileType type = config.GetMissileType(((Missile)target).GetType());
                                        Missile missile = (Missile)target;

                                        if(type != null)
                                        {
                                            //Make our speed 50% higher than the targets if we are going faster than that. Otherwise, this aircraft just isn't fast enough.
                                            if(fltSpeed > 1.5f * missile.GetSpeed() && fltDistance <= Defs.ATTACK_APPROACH_DISTANCE)
                                                fltSpeed = 1.5f * missile.GetSpeed();
                                        }
                                    }
                                    else if(target instanceof Airplane targetAircraft)
                                    {
                                        //Make our speed 50% higher than the targets if we are going faster than that. Otherwise, this aircraft just isn't fast enough.
                                        if(fltSpeed > 1.5f * Defs.GetAircraftSpeed(targetAircraft.GetAircraftType()) && fltDistance <= Defs.ATTACK_APPROACH_DISTANCE)
                                            fltSpeed = 1.5f * Defs.GetAircraftSpeed(targetAircraft.GetAircraftType());
                                    }
                                } 

                                if(target == null || (!(target instanceof Submarine && ((Submarine)target).Submerged())))
                                {
                                    if(geoPosition.TurnToward(geoTarget, fltTurnRate, fltSpeed, lMS))
                                    { 
                                        if(geoPosition.MoveToward(geoTarget, fltSpeed, lMS))
                                        {
                                            geoPosition.Move(geoPosition.GetLastBearing(), fltSpeed, lMS);
                                        }
                                    }
                                    
                                    EntityMoved(aircraft);

                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                                    aircraft.UseFuel(fltFuelUsed);
                                }
                                else
                                {
                                    aircraft.MoveToPosition(geoTarget);
                                }
                            }
                            else
                            {
                                aircraft.MoveToPosition(aircraft.GetGeoTarget());
                            }
                        }
                        else
                        {
                            aircraft.MoveToPosition(aircraft.GetGeoTarget());
                        } 
                    }
                    break;
                    
                    //This aircraft is approaching another aircraft to refuel. If it is faster than the tanker, it should slow down once within Defs.AIRCRAFT_REFUEL_BUFFER.
                    case SEEK_FUEL:
                    {
                        if(aircraft.GetTarget() != null)
                        {
                            MapEntity target = aircraft.GetTarget().GetMapEntity(this);

                            if(target instanceof Airplane)
                            {
                                Airplane tanker = ((Airplane)target);
                                
                                if(tanker.CanTransferFuel() && tanker.GetMoveOrders() == MoveOrders.PROVIDE_FUEL)
                                {
                                    GeoCoord geoTarget = tanker.GetPosition().GetCopy();
                                    GeoCoord geoPosition = aircraft.GetPosition();
                                    float fltTankerSpeed = Defs.GetAircraftSpeed(tanker.GetAircraftType());
                                    float fltOurSpeed = Defs.GetAircraftSpeed(aircraft.GetEntityType());
                                    float fltDistance = aircraft.GetPosition().DistanceTo(tanker.GetPosition());
                                    float fltApproachSpeed = fltOurSpeed;
                                    float fltTurnRate = Defs.AIRCRAFT_TURN_RATE;
                                    
                                    if(fltDistance <= Defs.AIRCRAFT_REFUEL_RANGE)
                                    {
                                        fltTurnRate = Defs.REFUELEE_TURN_RATE;
                                        
                                        //If the tanker is faster than us, maintain our speed. The tanker will slow to our speed.
                                        if(fltOurSpeed > fltTankerSpeed)
                                        {
                                            fltApproachSpeed = fltTankerSpeed;
                                        } 
                                    }
                                    else if(fltDistance < Defs.AIRCRAFT_REFUEL_APPROACH_BUFFER && fltOurSpeed > fltTankerSpeed)
                                    {
                                        
                                        fltApproachSpeed = ((fltDistance/Defs.AIRCRAFT_REFUEL_APPROACH_BUFFER) * (fltOurSpeed - fltTankerSpeed)) + fltTankerSpeed;
                                    }

                                    if(geoPosition.TurnToward(geoTarget, fltTurnRate, fltApproachSpeed, lMS))
                                    { 
                                        if(geoPosition.MoveToward(geoTarget, fltApproachSpeed, lMS))
                                        {
                                            geoPosition.Move(geoPosition.GetLastBearing(), fltApproachSpeed, lMS);
                                        }
                                    }
                                    
                                    EntityMoved(aircraft);
                                    
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                                    aircraft.UseFuel(fltFuelUsed);
                                }
                                else
                                {
                                    aircraft.MoveToPosition(aircraft.GetGeoTarget());
                                }
                            }
                            else
                            {
                                aircraft.MoveToPosition(aircraft.GetGeoTarget());
                            }
                        }
                        else
                        {
                            aircraft.MoveToPosition(aircraft.GetGeoTarget());
                        } 
                    }
                    break;
                    
                    case PROVIDE_FUEL:
                    {
                        if(aircraft.GetTarget() != null && aircraft.GetCurrentFuel() > Defs.AIRCRAFT_FUEL_TRANSFER_PER_TICK)
                        {
                            MapEntity target = aircraft.GetTarget().GetMapEntity(this);

                            if(target instanceof Airplane)
                            {
                                Airplane refuelee = ((Airplane)target);
                                
                                if(refuelee.GetMoveOrders() == MoveOrders.SEEK_FUEL)
                                {
                                    GeoCoord geoTarget = aircraft.GetGeoTarget();
                                    GeoCoord geoPosition = aircraft.GetPosition();
                                    float fltApproachSpeed = Defs.GetAircraftSpeed(aircraft.GetEntityType());
                                    float fltOurSpeed = Defs.GetAircraftSpeed(aircraft.GetEntityType());
                                    float fltRefueleeSpeed = Defs.GetAircraftSpeed(refuelee.GetAircraftType());

                                    if(aircraft.GetPosition().DistanceTo(refuelee.GetPosition()) <= Defs.AIRCRAFT_REFUEL_RANGE)
                                    {
                                        if(fltOurSpeed > fltRefueleeSpeed)
                                        {
                                            fltApproachSpeed = fltRefueleeSpeed;
                                        }
                                    }
                                    else if(fltOurSpeed > Defs.AIRCRAFT_REFUEL_APPROACH_SPEED_MULT * fltRefueleeSpeed)
                                    {
                                        fltApproachSpeed = fltRefueleeSpeed * Defs.AIRCRAFT_REFUEL_APPROACH_SPEED_MULT;
                                    }

                                    if(geoPosition.TurnToward(geoTarget, Defs.TANKER_TURN_RATE, fltApproachSpeed, lMS))
                                    { 
                                        if(geoPosition.MoveToward(geoTarget, fltApproachSpeed, lMS))
                                        {
                                            geoPosition.Move(geoPosition.GetLastBearing(), fltApproachSpeed, lMS);
                                        }
                                    }
                                    
                                    EntityMoved(aircraft);
                                    
                                    float fltFuelUsed = Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetAircraftType()), Defs.GetAircraftSpeed(aircraft.GetAircraftType()));
                                    aircraft.UseFuel(fltFuelUsed);
                                }
                                else
                                {
                                    aircraft.MoveToPosition(aircraft.GetGeoTarget());
                                }
                            }
                            else
                            {
                                aircraft.MoveToPosition(aircraft.GetGeoTarget());
                            }
                        }   
                        else
                        {
                            aircraft.MoveToPosition(aircraft.GetGeoTarget());
                        }
                    }
                    break;

                    case WAIT:
                    {
                        GeoCoord geoTarget = aircraft.GetGeoTarget();
                        GeoCoord geoPosition = aircraft.GetPosition();

                        if(geoPosition.TurnToward(geoTarget, Defs.AIRCRAFT_TURN_RATE, Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS))
                        {
                            if(geoPosition.MoveToward(geoTarget, Defs.GetAircraftSpeed(aircraft.GetAircraftType()), lMS))
                            {
                                geoPosition.Move(geoPosition.GetLastBearing(), Defs.GetAircraftSpeed(aircraft.GetEntityType()), lMS);
                            }
                        }
                        
                        EntityMoved(aircraft);
                    }
                    break;
                }
            }
        }
        
        for(ResourceDeposit deposit : ResourceDeposits.values())
        {
            if(deposit.GetRemove())
            {
                ResourceDeposits.remove(deposit.GetID());
                EntityRemoved(deposit, true);
            }
        }

        for(Loot loot : Loots.values())
        {
            loot.Tick(lMS);

            if(loot.Expired() || loot.Collected() || loot.Depleted())
            {
                Loots.remove(loot.GetID());
                EntityRemoved(loot, true);
            }
        }
        
        for(Rubble rubble : Rubbles.values())
        {
            rubble.Tick(lMS);

            if(rubble.Expired() || rubble.GetRemove())
            {
                Rubbles.remove(rubble.GetID());
                EntityRemoved(rubble, true);
            }
        }
        
        for(Radiation radiation : Radiations.values())
        {
            radiation.Tick(lMS);

            if(radiation.GetExpired())
            {
                Radiations.remove(radiation.GetID());
                EntityRemoved(radiation, true);
            }
        }
    }
    
    public List<Damagable> GetNearbyDamagables(GeoCoord geoPosition, float fltDistance)
    {
        //Return all damagable entities within the specified distance of the specified position.
        List<Damagable> Result = new ArrayList<>();
        
        for(Structure structure : GetNearbyStructures(geoPosition, fltDistance))
        {
            Result.add(structure);
        }
        
        for(Infantry infantry : GetNearbyInfantries(geoPosition, fltDistance))
        {
            Result.add(infantry);
        }
        
        for(Tank tank : GetNearbyTanks(geoPosition, fltDistance))
        {
            Result.add(tank);
        }
        
        for(Shipyard shipyard : GetNearbyShipyards(geoPosition, fltDistance))
        {
            Result.add(shipyard);
        }
        
        return Result;
    }
    
    /**
     * Return a list of nearby structures for the purpose of player construction, considering the attack range of enemy sentries.
     * @param player The player intending to construct something.
     * @return 
     */
    public List<Structure> GetNearbyStructures(Player player)
    {
        List<Structure> Result = new ArrayList<>();
        GeoCoord geoPosition = player.GetPosition();
        
        for(Structure structure : GetAllStructures())
        {
            if(structure.GetPosition().DistanceTo(geoPosition) < config.GetStructureSeparation(structure.GetEntityType()))
            {
                Result.add(structure);
            }
            /*else if(structure instanceof SentryGun)
            {
                SentryGun sentry = (SentryGun)structure;
                
                switch(GetAllegiance(player, sentry))
                {
                    case ENEMY:
                    case NEUTRAL:
                    {
                        if(structure.GetPosition().DistanceTo(geoPosition) < config.GetSentryGunRange())
                        {
                            Result.add(structure);
                        }
                    }
                    break;
                }
            }*/
        }
        
        return Result;
    }
    
    public float GetDistanceToNearestStructure(GeoCoord geoPosition)
    {
        float fltDistanceToReturn = Float.MAX_VALUE;
        
        for(Structure structure : GetAllStructures())
        {
            float fltDistanceToStructure = structure.GetPosition().DistanceTo(geoPosition);
            
            if(fltDistanceToStructure < fltDistanceToReturn)
            {
                fltDistanceToReturn = fltDistanceToStructure;
            }
        }
        
        return fltDistanceToReturn;
    }
    
    /**
     * Return a list of nearby structures for general cases.
     * @param geoPosition Position
     * @param fltDistance Distance from this position to include structures within.
     * @return 
     */
    public List<Structure> GetNearbyStructures(GeoCoord geoPosition, float fltDistance)
    {
        List<Structure> Result = new ArrayList<>();
        
        for(Structure structure : GetAllStructures())
        {
            if(structure.GetPosition().DistanceTo(geoPosition) < fltDistance)
            {
                Result.add(structure);
            }
        }
        
        return Result;
    }
    
    public List<Infantry> GetNearbyInfantries(GeoCoord geoPosition, float fltDistance)
    {
        List<Infantry> Result = new ArrayList<>();
        
        for(Infantry infantry : GetInfantries())
        {
            if(infantry.GetPosition().RadiusPhaseCollisionTest(geoPosition, fltDistance) && infantry.GetPosition().DistanceTo(geoPosition) <= fltDistance)
            {
                Result.add(infantry);
            }
        }
        
        return Result;
    }
    
    public List<Tank> GetNearbyTanks(GeoCoord geoPosition, float fltDistance)
    {
        List<Tank> Result = new ArrayList<>();
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetPosition().RadiusPhaseCollisionTest(geoPosition, fltDistance) && tank.GetPosition().DistanceTo(geoPosition) <= fltDistance)
            {
                Result.add(tank);
            }
        }
        
        return Result;
    }
    
    
    
    public List<CargoTruck> GetNearbyTrucks(GeoCoord geoPosition, float fltDistance)
    {
        List<CargoTruck> Result = new ArrayList<>();
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.GetPosition().RadiusPhaseCollisionTest(geoPosition, fltDistance) && truck.GetPosition().DistanceTo(geoPosition) <= fltDistance)
            {
                Result.add(truck);
            }
        }
        
        return Result;
    }
    
    public List<Shipyard> GetNearbyShipyards(GeoCoord geoPosition, float fltDistance)
    {
        List<Shipyard> Result = new ArrayList<>();
        
        for(Shipyard shipyard : GetShipyards())
        {
            if(shipyard.GetPosition().RadiusPhaseCollisionTest(geoPosition, fltDistance) && shipyard.GetPosition().DistanceTo(geoPosition) <= fltDistance)
            {
                Result.add(shipyard);
            }
        }
        
        return Result;
    }
    
    public boolean BlueprintFitsInLocation(EntityType type, GeoCoord geoLocation)
    {
        for(Structure structure : GetAllStructures())
        {
            if(geoLocation.EvenBroaderPhaseCollisionTest(structure.GetPosition()))
            {
                float fltDistance = geoLocation.DistanceTo(structure.GetPosition());
            
                if(fltDistance <= config.GetStructureSeparation(structure.GetEntityType()) || fltDistance <= config.GetStructureSeparation(type))
                    return false;
            }
        }
        
        for(Blueprint blueprint : GetBlueprints())
        {
            if(geoLocation.EvenBroaderPhaseCollisionTest(blueprint.GetPosition()))
            {
                float fltDistance = geoLocation.DistanceTo(blueprint.GetPosition());
            
                if(fltDistance <= config.GetStructureSeparation(blueprint.GetType()) || fltDistance <= config.GetStructureSeparation(type))
                    return false;
            }
        }
        
        return true;
    }
    
    protected final void UpdateTrackingMissileThreats(int lPlayerID)
    {
        for(Missile missile : Missiles.values())
        {
            if(missile.GetTracking())
            {
                MapEntity targetEntity = missile.GetTargetEntity().GetMapEntity(this);
                    
                if(targetEntity != null && targetEntity.GetOwnedBy(lPlayerID))
                    EstablishStructureThreats(missile, lPlayerID);
            }
        }
    }
    
    /**
     * 
     * @param lPlayerID Used to set ThreatensUs and ThreatensFriendlies in missiles on the client-side. Pass in LaunchEntity.ID_NONE when calling from the back-end.
     */
    public final void EstablishStructureThreats(Missile missile, int lPlayerID)
    {
        MissileType type = config.GetMissileType(missile.GetType());
        GeoCoord geoTarget = GetMissileTarget(missile);
        missile.ClearStructureThreatenedPlayers();

        float a = MissileStats.GetBlastRadius(type, missile.GetAirburst());
        float b = MissileStats.GetMissileEMPRadius(type, missile.GetAirburst());
        float fltThreatRadius = a > b ? a : b;

        for(Structure structure : GetAllStructures())
        {
            if(!missile.ThreatensPlayersStructures(structure.GetOwnerID()))
            {
                if(!structure.GetRespawnProtected() && !structure.Destroyed())
                {
                    if(structure.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                    {
                        missile.AddStructureThreatenedPlayer(structure.GetOwnerID());

                        Player player = Players.get(structure.GetOwnerID());

                        if(player != null)
                        {
                            player.AddHostileMissile(missile.GetID());
                        }
                    }
                }
            }
        }

        for(Tank tank : GetTanks())
        {
            if(!missile.ThreatensPlayersStructures(tank.GetOwnerID()) && !tank.Destroyed())
            {
                if(tank.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    missile.AddStructureThreatenedPlayer(tank.GetOwnerID());

                    Player player = Players.get(tank.GetOwnerID());

                    if(player != null)
                    {
                        player.AddHostileMissile(missile.GetID());
                    }
                }
            }
        }

        for(CargoTruck truck : GetCargoTrucks())
        {
            if(!missile.ThreatensPlayersStructures(truck.GetOwnerID()) && !truck.Destroyed())
            {
                if(truck.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    missile.AddStructureThreatenedPlayer(truck.GetOwnerID());

                    Player player = Players.get(truck.GetOwnerID());

                    if(player != null)
                    {
                        player.AddHostileMissile(missile.GetID());
                    }
                }
            }
        }

        for(Infantry infantry : GetInfantries())
        {
            if(!missile.ThreatensPlayersStructures(infantry.GetOwnerID()) && !infantry.Destroyed())
            {
                if(infantry.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    missile.AddStructureThreatenedPlayer(infantry.GetOwnerID());

                    Player player = Players.get(infantry.GetOwnerID());

                    if(player != null)
                    {
                        player.AddHostileMissile(missile.GetID());
                    }
                }
            }
        }

        for(Ship ship : GetShips())
        {
            if(!missile.ThreatensPlayersStructures(ship.GetOwnerID()))
            {
                if(!ship.Destroyed())
                {
                    if(ship.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                    {
                        missile.AddStructureThreatenedPlayer(ship.GetOwnerID());

                        Player player = Players.get(ship.GetOwnerID());

                        if(player != null)
                        {
                            player.AddHostileMissile(missile.GetID());
                        }
                    }
                }
            }
        }

        for(Submarine submarine : GetSubmarines())
        {
            if(!missile.ThreatensPlayersStructures(submarine.GetOwnerID()))
            {
                if(!submarine.Destroyed())
                {
                    if(submarine.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                    {
                        missile.AddStructureThreatenedPlayer(submarine.GetOwnerID());

                        Player player = Players.get(submarine.GetOwnerID());

                        if(player != null)
                        {
                            player.AddHostileMissile(missile.GetID());
                        }
                    }
                }
            }
        }

        for(Capturable capturable : GetAllCapturables())
        {
            if(capturable.IsCaptured() && !missile.ThreatensPlayersStructures(capturable.GetOwnerID()) && !capturable.Destroyed())
            {
                if(capturable.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    missile.AddStructureThreatenedPlayer(capturable.GetOwnerID());

                    Player player = Players.get(capturable.GetOwnerID());

                    if(player != null)
                    {
                        player.AddHostileMissile(missile.GetID());
                    }
                }
            }
        }
        
        if(lPlayerID != LaunchEntity.ID_NONE)
        {
            Player player = GetPlayer(lPlayerID);
            
            if(player != null)
            {
                if(missile.ThreatensPlayersStructures(lPlayerID))
                {
                    missile.SetThreatensUs();
                }
                
                for(Player ally : GetPlayerAlliesAndSelf(player))
                {
                    if(ally.GetID() != lPlayerID)
                    {
                        if(missile.ThreatensPlayersStructures(ally.GetID()))
                        {
                            missile.SetThreatensAllies();
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public final void EstablishStructureThreats(Torpedo torpedo)
    {
        TorpedoType type = config.GetTorpedoType(torpedo.GetType());
        GeoCoord geoTarget = torpedo.GetGeoTarget();
        torpedo.ClearStructureThreatenedPlayers();

        for(Structure structure : GetAllStructures())
        {
            if(!torpedo.ThreatensPlayersStructures(structure.GetOwnerID()))
            {
                if(!structure.GetRespawnProtected() && !structure.Destroyed())
                {
                    if(structure.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                    {
                        torpedo.AddStructureThreatenedPlayer(structure.GetOwnerID());
                    }
                }
            }
        }
        
        for(Ship ship : GetShips())
        {
            if(!torpedo.ThreatensPlayersStructures(ship.GetOwnerID()))
            {
                if(!ship.Destroyed())
                {
                    if(ship.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                    {
                        torpedo.AddStructureThreatenedPlayer(ship.GetOwnerID());
                    }
                }
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(!torpedo.ThreatensPlayersStructures(submarine.GetOwnerID()))
            {
                if(!submarine.Destroyed())
                {
                    if(submarine.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                    {
                        torpedo.AddStructureThreatenedPlayer(submarine.GetOwnerID());
                    }
                }
            }
        }
    }
    
    protected final void EstablishStructureThreats(Structure structure)
    {
        for(Missile missile : Missiles.values())
        {
            if(!missile.ThreatensPlayersStructures(structure.GetOwnerID()))
            {
                MissileType type = config.GetMissileType(missile.GetType());
                GeoCoord geoTarget = GetMissileTarget(missile);
                
                float a = MissileStats.GetBlastRadius(type, missile.GetAirburst());
                float b = MissileStats.GetMissileEMPRadius(type, missile.GetAirburst());
                float fltThreatRadius = a > b ? a : b;
                
                if(structure.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    missile.AddStructureThreatenedPlayer(structure.GetOwnerID());
                }
            }
        }
    }
    
    /**
     * 
     * @param lPlayerID Used to set ThreatensUs and ThreatensFriendlies in missiles on the client-side. Pass in LaunchEntity.ID_NONE when calling from the back-end.
     */
    public final void EstablishAllStructureThreats(int lPlayerID)
    {
        for(Missile missile : Missiles.values())
        {
            EstablishStructureThreats(missile, lPlayerID);
        }
        
        for(Torpedo torpedo : Torpedoes.values())
        {
            EstablishStructureThreats(torpedo);
        }
    }

    /**
     * Returns true if the specified missile threatens the player. Uses cached structure threats for optimisation. It's necessary to call EstablishStructureThreats() as appropriate for this to be up to date.
     * @param missile The missile to query if it is a threat.
     * @param player The player to query if they are threatened by the missile.
     * @param geoTarget The location that the missile is targetting. This must come as a separate parameter as it can vary by whether the missile is tracking a player or not.
     * @param type The missile's type. NOTE: This parameter has already been derived from config by the caller, so passing it instead of deriving it again improves performance.
     * @return A boolean value indicating whether the specified missile threatens the specified player.
     */
    public boolean ThreatensPlayerOptimised(Missile missile, Player player, GeoCoord geoTarget, MissileType type)
    {
        if(missile != null)
        {
            if(!missile.GetVisible())
                return false;

            if(player != null)
            {
                if(missile.ThreatensPlayersStructures(player.GetID()))
                    return true;

                //Tracking the player?
                if(missile.GetTracking())
                {
                    if(missile.GetTargetEntity().GetMapEntity(this).ApparentlyEquals(player))
                    {
                        return true;
                    }
                }

                //Player within blast radius?
                if((!player.GetRespawnProtected()) || type.GetNuclear())
                {
                    if(player.GetPosition().DistanceTo(geoTarget) <= MissileStats.GetBlastRadius(type, missile.GetAirburst()))
                    {
                        if(MissileStats.GetBlastRadius(type, missile.GetAirburst()) > 0)
                            return true;
                    }
                }
            }
        } 
            
        return false;
    }
    
    public boolean GetPlayerIsSheltered(Player player)
    {
        for(CommandPost commandPost : CommandPosts.values())
        {
            if(player.GetPosition().DistanceTo(commandPost.GetPosition()) < config.GetCommandPostShelterRadius())
            {
                if(GetAllegiance(player, commandPost) == Allegiance.YOU || GetAllegiance(player, commandPost) == Allegiance.ALLY)
                {
                    if(commandPost.GetOnline())
                        return true;
                }
            }
        }
        
        return false;
    }
    
    public CommandPost GetPlayerCurrentCommandPost(Player player)
    {
        if(GetPlayerIsSheltered(player))
        {
            for(CommandPost commandPost : CommandPosts.values())
            {
                if(player.GetPosition().DistanceTo(commandPost.GetPosition()) < config.GetCommandPostShelterRadius())
                {
                    if(GetAllegiance(player, commandPost) == Allegiance.YOU || GetAllegiance(player, commandPost) == Allegiance.ALLY)
                    {
                        if(commandPost.GetOnline())
                            return commandPost;
                    }
                }
            }
        }
        
        return null;
    }
    
    public boolean GetInfantryIsSheltered(Infantry infantry)
    {
        for(CommandPost commandPost : CommandPosts.values())
        {
            if(commandPost.GetPosition().BroadPhaseCollisionTest(infantry.GetPosition()) && infantry.GetPosition().DistanceTo(commandPost.GetPosition()) < config.GetCommandPostShelterRadius())
            {
                if(GetAllegiance(infantry, commandPost) == Allegiance.YOU || GetAllegiance(infantry, commandPost) == Allegiance.ALLY)
                {
                    if(commandPost.GetOnline())
                        return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Returns true if the specified missile type threatens friendlies (the player or allies) at the specified location. NOT OPTIMISED! Use ThreatensPlayerOptimised() for per-tick status!
     * @param lPlayerID Player to query if they or their allies would be threatened.
     * @param geoTarget The target of the missile.
     * @param type The type of the missile.
     * @param bConsiderEMP Consider EMP from nukes to be a threat.
     * @param bConsiderRespawnProtection Consider respawn protected stuff not to be threatened.
     * @return Boolean value indicating whether a threat would exist.
     */
    public boolean ThreatensFriendlies(int lPlayerID, GeoCoord geoTarget, MissileType type, boolean bConsiderEMP, boolean bConsiderRespawnProtection, boolean bAirburst)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            //Player is not in an alliance. Just see if it threatens them.
            return ThreatensPlayer(lPlayerID, geoTarget, type, bConsiderEMP, bConsiderRespawnProtection, bAirburst);
        }
        else
        {
            //Player is in an alliance. See if this threatens any of their allies or affiliates.
            for(Player otherPlayer : Players.values())
            {
                switch(GetAllegiance(player, otherPlayer))
                {
                    case AFFILIATE:
                    case ALLY:
                    case YOU:
                    {
                        if(type != null && ThreatensPlayer(otherPlayer.GetID(), geoTarget, type, bConsiderEMP, bConsiderRespawnProtection, bAirburst))
                        {
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        
        return false;
    }
    
    public boolean IsBullyingOrNuisance(int lPlayerID, GeoCoord geoTarget, MissileType type, boolean bConsiderEMP, boolean bConsiderRespawnProtection, boolean bAirburst)
    {
        Player inflictor = GetPlayer(lPlayerID);
        
        if(inflictor != null)
        {
            boolean bPlayersThreatened = false;
            
            for(Player inflictee : Players.values())
            {
                if(!inflictor.ApparentlyEquals(inflictee))
                {
                    if(ThreatensPlayer(inflictee.GetID(), geoTarget, type, bConsiderEMP, bConsiderRespawnProtection, bAirburst))
                    {
                        if(!GetAttackIsBullying(inflictor, inflictee))
                        {
                            return false;
                        }
                        
                        bPlayersThreatened = true;
                    }
                }
            }
            
            if(!bPlayersThreatened)
            {
                return false;
            }
        }
         
        return true; //Default to true so that strong players cannot hide behind weak players.
    }
    
    public boolean ThreatensAllies(int lPlayerID, GeoCoord geoTarget, MissileType type, boolean bConsiderEMP, boolean bConsiderRespawnProtection, boolean bAirburst)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            return false;
        }
        else
        {
            //Player is in an alliance. See if this threatens any of their allies.
            for(Player otherPlayer : Players.values())
            {
                switch(GetAllegiance(player, otherPlayer))
                {
                    case ALLY:
                    {
                        if(ThreatensPlayer(otherPlayer.GetID(), geoTarget, type, bConsiderEMP, bConsiderRespawnProtection, bAirburst))
                        {
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        
        return false;
    }
    
    /** Check if a missile targeted at the specified location would threaten a player or their assets.
     * @param lPlayerID The ID of the player to check.
     * @param geoTarget The target coordinates.
     * @param type The missile type ID.
     * @param bConsiderEMP A boolean indicating whether the EMP radius of nukes should be considered as "threatening" in this context. (In this particular instance, this has been changed to include the max radiation distance.
     * @param bConsiderRespawnProtection A boolean indicating whether respawn protected players or assets should be ignored.
     * @return 
     */
    public boolean ThreatensPlayer(int lPlayerID, GeoCoord geoTarget, MissileType type, boolean bConsiderEMP, boolean bConsiderRespawnProtection, boolean bAirburst)
    {
        float a = MissileStats.GetBlastRadius(type, bAirburst);
        float b = MissileStats.GetMissileEMPRadius(type, bAirburst);
        float fltThreatRadius;
        
        if(bConsiderEMP)
            fltThreatRadius = a > b ? a : b;
        else
            fltThreatRadius = a;
        
        if(type.GetAntiSubmarine() && type.GetTorpedoType() != LaunchEntity.ID_NONE)
        {
            TorpedoType torpedoType = config.GetTorpedoType(type.GetTorpedoType());
            
            if(torpedoType != null)
            {
                fltThreatRadius = torpedoType.GetTorpedoRange();
            }
        }
        
        if(fltThreatRadius < 0.001f)
        {
            return false;
        }
        
        Player player = GetPlayer(lPlayerID);

        //Return true if whatever this is will threaten the identified player or their assets.
        if(!player.GetRespawnProtected() || !bConsiderRespawnProtection || type.GetNuclear())
        {
            if(player.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
            {
                return true;
            }
        }
        
        for(Structure structure : GetAllStructures())
        {
            if(structure.GetOwnerID() == lPlayerID)
            {
                if(!structure.GetRespawnProtected() || !bConsiderRespawnProtection)
                {
                    if(structure.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                    {
                        return true;
                    }
                }
            }
        }
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                if(tank.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    return true;
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.GetOwnerID() == lPlayerID)
            {
                if(truck.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    return true;
                }
            }
        }  
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    return true;
                }
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                if(submarine.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    return true;
                }
            }
        }
        
        for(Shipyard shipyard : GetShipyards())
        {
            if(shipyard.GetOwnerID() == lPlayerID)
            {
                if(shipyard.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
                {
                    return true;
                }
            }
        }
            
        return false;
    }
    
    public boolean ThreatensFriendlies(int lPlayerID, GeoCoord geoTarget, TorpedoType type, boolean bConsiderRespawnProtection)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            //Player is not in an alliance. Just see if it threatens them.
            return ThreatensPlayer(lPlayerID, geoTarget, type, bConsiderRespawnProtection);
        }
        else
        {
            //Player is in an alliance. See if this threatens any of their allies or affiliates.
            for(Player otherPlayer : Players.values())
            {
                switch(GetAllegiance(player, otherPlayer))
                {
                    case AFFILIATE:
                    case ALLY:
                    case YOU:
                    {
                        if(ThreatensPlayer(otherPlayer.GetID(), geoTarget, type, bConsiderRespawnProtection))
                        {
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        
        return false;
    }
    
    public boolean ThreatensPlayer(int lPlayerID, GeoCoord geoTarget, TorpedoType type, boolean bConsiderRespawnProtection)
    {
        Player player = GetPlayer(lPlayerID);

        //Return true if whatever this is will threaten the identified player or their assets.
        if(!player.GetRespawnProtected() || !bConsiderRespawnProtection || type.GetNuclear())
        {
            if(player.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
            {
                return true;
            }
        }
        
        for(Structure structure : GetAllStructures())
        {
            if(structure.GetOwnerID() == lPlayerID)
            {
                if(!structure.GetRespawnProtected() || !bConsiderRespawnProtection)
                {
                    if(structure.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                    {
                        return true;
                    }
                }
            }
        }
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                if(tank.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                {
                    return true;
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.GetOwnerID() == lPlayerID)
            {
                if(truck.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                {
                    return true;
                }
            }
        }
        
        for(Infantry infantry : GetInfantries())
        {
            if(infantry.GetOwnerID() == lPlayerID)
            {
                if(infantry.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                {
                    return true;
                }
            }
        }
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                {
                    return true;
                }
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                if(submarine.GetPosition().DistanceTo(geoTarget) <= type.GetBlastRadius())
                {
                    return true;
                }
            }
        }
            
        return false;
    }
    
    /**
     * Indicates that the specified player is "in battle", for the purpose of checking if it's okay for them to join/leave an alliance.
     * @param player Player to query.
     * @return True if the player is attacking or under attack. False if they're idle.
     */
    public boolean InBattle(Player player)
    {
        for(Missile missile : Missiles.values())
        {
            //If the player owns any in-flight missiles, they are in battle.
            if(missile.GetOwnerID() == player.GetID())
                return true;

            //If any missiles threaten the player, they are in battle.
            if(ThreatensPlayer(player.GetID(), GetMissileTarget(missile), config.GetMissileType(missile.GetType()), false, false, missile.GetAirburst()))
                return true;
        }
        
        return false;
    }
    
    public boolean AllianceInBattle(Alliance alliance)
    {
        for(Player player : GetMembers(alliance))
        {
            if(InBattle(player))
                return true;
        }
        
        return false;
    }
    
    public boolean AtWar(Player player)
    {
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            for(Treaty treaty : Treaties.values())
            {
                if(treaty != null && treaty instanceof War && treaty.IsAParty(player.GetAllianceMemberID()))
                    return true;
            }
        }
        
        return false;
    }
    
    public int GetAircraftSlotUpgradeCost(int airbaseCapacity, int startingCapacity)
    {
        long oCost = Defs.AIRBASE_CAPACITY_UPGRADE_COST.get(ResourceType.WEALTH);

        return (int)oCost;
    }
    
    public int GetMissileSlotUpgradeCost(MissileSystem missileSystem, byte cBaseSlotCount)
    {
        return (((missileSystem.GetSlotCount() - cBaseSlotCount) / config.GetMissileUpgradeCount()) + 1) * config.GetMissileUpgradeBaseCost();
    }
    
    public int GetMissileSlotUpgradeCostToMax(MissileSystem missileSystem, byte cBaseSlotCount)
    {
        int lTotalCost = 0;

        int lCurrentSlots = missileSystem.GetSlotCount();

        int lUpgradeCount = config.GetMissileUpgradeCount();

        int lSlots = lCurrentSlots;

        while(lSlots < Defs.MAX_MISSILE_SLOTS)
        {
            int stepIndex = (lSlots - cBaseSlotCount) / lUpgradeCount;

            int stepCost = (stepIndex + 1) * config.GetMissileUpgradeBaseCost();
            lTotalCost += stepCost;

            lSlots += lUpgradeCount;
        }

        return lTotalCost;
    }
    
    public Map<ResourceType, Long> GetMissileSlotSaleValue(MissileSystem missileSystem, int lBaseSlotCount)
    {
        Map<ResourceType, Long> Value = new ConcurrentHashMap<>();
        
        try
        {
            int lUpgradeCount = missileSystem.GetSlotCount() / config.GetMissileUpgradeCount() - config.GetInitialMissileSlots();
            long oValue = 0;

            for(int i = 0; i < lUpgradeCount; i++)
            {
                oValue += (config.GetMissileUpgradeBaseCost() * i);
            }
            
            Value.put(ResourceType.WEALTH, oValue);

            return Value;
        }
        catch(Exception ex)
        {
            return Value;
        }
    }
    
    public int GetReloadUpgradeCost(MissileSystem system)
    {
        if(system.GetReloadTime() >= config.GetReloadTimeBase())
        {
            return config.GetReloadStage1Cost();
        }
        else if(system.GetReloadTime() >= config.GetReloadTimeStage1())
        {
            return config.GetReloadStage2Cost();
        }
        else if(system.GetReloadTime() >= config.GetReloadTimeStage2())
        {
            return config.GetReloadStage3Cost();
        }
        
        return Defs.UPGRADE_COST_MAXED;
    }
    
    public Map<ResourceType, Long> GetReloadUpgradeSaleValue(MissileSystem system)
    {
        Map<ResourceType, Long> Value = new ConcurrentHashMap<>();
        
        if(system.GetReloadTime() == config.GetReloadTimeStage1())
        {
            Value.put(ResourceType.WEALTH, (long)config.GetReloadStage1Cost());
        }
        else if(system.GetReloadTime() == config.GetReloadTimeStage2())
        {
            Value.put(ResourceType.WEALTH, (long)(config.GetReloadStage1Cost() + config.GetReloadStage2Cost()));
        }
        else if(system.GetReloadTime() == config.GetReloadTimeStage3())
        {
            Value.put(ResourceType.WEALTH, (long)(config.GetReloadStage1Cost() + config.GetReloadStage2Cost() + config.GetReloadStage3Cost()));
        }
        else
        {
            Value.put(ResourceType.WEALTH, (long)420);
        }
        
        return Value;
    }

    public int GetReloadUpgradeTime(MissileSystem system)
    {
        if(system.GetReloadTime() >= Defs.RELOAD_TIME_BASE)
        {
            return Defs.RELOAD_TIME_STAGE_1;
        }
        else if(system.GetReloadTime() >= Defs.RELOAD_TIME_STAGE_1)
        {
            return Defs.RELOAD_TIME_STAGE_2;
        }
        
        return Defs.RELOAD_TIME_STAGE_3;
    }
    
    public short GetCommandPostHPUpgradeHP(CommandPost commandPost)
    {
        return (short) (commandPost.GetMaxHP() + config.GetCommandPostHPUpgradeHP());
    }
    
    public int GetCommandPostHPUpgradeCost(CommandPost commandPost)
    {
        return (commandPost.GetMaxHP()/config.GetCommandPostHPUpgradeHP()) * config.GetCommandPostHPUpgradeCost();
    }
    
    public int GetRadarBoostUpgradeCost(RadarStation radarStation)
    {
        return config.GetRadarBoostUpgradeCost();
    }
    
    private Map<ResourceType, Long> GetStructureValue(Structure structure)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        MissileSystem missileSystem = null;
        
        if(structure != null)
            LaunchUtilities.AddResourceMapsTogether(Values, structure.GetResourceSystem().GetTypes());
        
        if(structure instanceof MissileSite)
        {
            MissileSite missileSite = (MissileSite)structure;
            missileSystem = missileSite.GetMissileSystem();
            
            if(missileSite.CanTakeICBM())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.ICBM_SILO_STRUCTURE_COST);
            }
            else
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.MISSILE_SITE_STRUCTURE_COST);
                LaunchUtilities.AddResourceMapsTogether(Values, GetMissileSlotSaleValue(missileSystem, config.GetInitialMissileSlots()));
                LaunchUtilities.AddResourceMapsTogether(Values, GetReloadUpgradeSaleValue(missileSystem));
            }
        }
        else if(structure instanceof SAMSite)
        {
            SAMSite samSite = ((SAMSite)structure);
            missileSystem = ((SAMSite)structure).GetInterceptorSystem();
            
            if(!samSite.GetIsABMSilo())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.SAM_SITE_STRUCTURE_COST);
                LaunchUtilities.AddResourceMapsTogether(Values, GetMissileSlotSaleValue(missileSystem, config.GetInitialInterceptorSlots()));
                LaunchUtilities.AddResourceMapsTogether(Values, GetReloadUpgradeSaleValue(missileSystem));
            }
            else
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.ABM_SILO_STRUCTURE_COST);
            }
        }
        else if(structure instanceof ArtilleryGun artillery)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.ARTILLERY_GUN_STRUCTURE_COST);
            missileSystem = artillery.GetMissileSystem();
        }
        else if(structure instanceof SentryGun)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.SENTRY_GUN_STRUCTURE_COST);
        }
        else if(structure instanceof OreMine)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.ORE_MINE_STRUCTURE_COST);
        }
        else if(structure instanceof ScrapYard)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.SCRAPYARD_STRUCTURE_COST);
        }
        else if(structure instanceof RadarStation)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.RADAR_STATION_STRUCTURE_COST);
        }
        else if(structure instanceof CommandPost)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.COMMAND_POST_STRUCTURE_COST);
        }
        else if(structure instanceof Airbase)
        {
            Airbase airbase = ((Airbase)structure);
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.AIRBASE_STRUCTURE_COST);
            
            if(airbase.GetAircraftSystem().GetSlotCount() > Defs.AIRBASE_DEFAULT_SLOTS)
            {
                ResourceType typeCost = Defs.AIRBASE_CAPACITY_UPGRADE_TYPE;
            
                Map<ResourceType, Long> UpgradeValue = new ConcurrentHashMap<>();
                UpgradeValue.put(typeCost, (long)((airbase.GetAircraftSystem().GetSlotCount() - Defs.AIRBASE_DEFAULT_SLOTS)/Defs.AIRBASE_CAPACITY_UPGRADE_AMOUNT) * Defs.AIRBASE_CAPACITY_UPGRADE_COST.get(typeCost));
                LaunchUtilities.AddResourceMapsTogether(Values, UpgradeValue);
            }
            
            LaunchUtilities.AddResourceMapsTogether(Values, GetAircraftSystemValue(airbase.GetAircraftSystem()));
        }
        else if(structure instanceof Armory)
        {
            Armory armory = ((Armory)structure);
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.BARRACKS_STRUCTURE_COST);
            
            if(armory.GetProducing())
            {
                if(armory.GetIsBarracks())
                    LaunchUtilities.AddResourceMapsTogether(Values, Defs.INFANTRY_UNIT_BUILD_COST);
                else
                    LaunchUtilities.AddResourceMapsTogether(Values, Defs.TANK_BUILD_COST);
            }
            
            LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemTotalValue(armory.GetCargoSystem()));
        }
        else if(structure instanceof Processor)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.GetProcessorCost(((Processor)structure).GetType()));
        }
        else if(structure instanceof Warehouse)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.WAREHOUSE_STRUCTURE_COST);
            
            Warehouse warehouse = ((Warehouse)structure);
            
            if(warehouse.GetProducing())
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.CARGO_TRUCK_BUILD_COST);
        }
        else if(structure instanceof Distributor)
        {
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.DISTRIBUTOR_STRUCTURE_COST);
        }
        else
        {
            throw new RuntimeException("Structure value queried for unknown structure.");
        }
        
        if(missileSystem != null)
        {
            for(int d = 0; d < missileSystem.GetSlotCount(); d++)
            {
                if(missileSystem.GetSlotHasMissile(d))
                {
                    if(structure instanceof MissileSite)
                    {
                        MissileType type = config.GetMissileType(missileSystem.GetSlotMissileType(d));
                        
                        LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                    }
                    else if(structure instanceof SAMSite)
                    {
                        InterceptorType type = config.GetInterceptorType(missileSystem.GetSlotMissileType(d));
                        
                        LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                    }
                }
            }
        }
        
        return Values;
    }
    
    public Map<ResourceType, Long> GetAircraftValue(AirplaneInterface aircraft)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(aircraft.HasMissiles())
        {
            LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(aircraft.GetMissileSystem(), true));
        }
        
        if(aircraft.HasInterceptors())
        {
            LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(aircraft.GetMissileSystem(), false));
        }
        
        if(aircraft.HasCargo())
        {
            LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemTotalValue(aircraft.GetCargoSystem()));
        }
        
        LaunchUtilities.AddResourceMapsTogether(Values, Defs.GetAircraftBuildCost(aircraft.GetAircraftType()));
        
        return Values;
    }
    
    public final Map<ResourceType, Long> GetLandUnitValue(LandUnit unit)
    {
        if(unit instanceof TankInterface)
        {
            return GetTankValue((TankInterface)unit);
        }
        else if(unit instanceof InfantryInterface infantry)
        {
            return GetInfantryValue(infantry);
        }
        else if(unit instanceof CargoTruckInterface)
        {
            return GetCargoTruckValue((CargoTruckInterface)unit);
        }
        
        return null;
    }
    
    public final Map<ResourceType, Long> GetInfantryValue(InfantryInterface infantry)
    {
        Map<ResourceType, Long> BaseValue = new ConcurrentHashMap<>();
        
        LaunchUtilities.AddResourceMapsTogether(BaseValue, Defs.INFANTRY_UNIT_BUILD_COST);
        
        return BaseValue;
    }
    
    public final Map<ResourceType, Long> GetCargoTruckValue(CargoTruckInterface truck)
    {
        Map<ResourceType, Long> BaseValue = new ConcurrentHashMap<>();
        
        LaunchUtilities.AddResourceMapsTogether(BaseValue, Defs.CARGO_TRUCK_BUILD_COST);
        LaunchUtilities.AddResourceMapsTogether(BaseValue, GetCargoSystemTotalValue(truck.GetCargoSystem()));
        
        return BaseValue;
    }
    
    public final Map<ResourceType, Long> GetCargoSystemTotalValue(CargoSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        //Only count money for now, though in the future we can also have it figure out the scrap value of each resource in the cargo system too.
        /*if(system.ContainsResourceType(ResourceType.WEALTH))
        {
            lValue += system.GetAmountOfType(ResourceType.WEALTH);
        }*/
        
        if(!system.GetInfantries().isEmpty())
        {
            for(StoredInfantry infantry : system.GetInfantries())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.INFANTRY_UNIT_BUILD_COST);
            }
        }
        
        if(!system.GetTanks().isEmpty())
        {
            for(StoredTank tank : system.GetTanks())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetTankValue(tank));
            }
        }
        
        if(!system.GetCargoTrucks().isEmpty())
        {
            for(StoredCargoTruck truck : system.GetCargoTrucks())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetCargoTruckValue(truck));
            }
        }
        
        return Values;
    }
    
    public final Map<ResourceType, Long> GetCargoSystemNeutralValue(CargoSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        LaunchUtilities.AddResourceMapsTogether(Values, system.GetResourceMap());
        
        if(!system.GetCargoTrucks().isEmpty())
        {
            for(StoredCargoTruck truck : system.GetCargoTrucks())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.CARGO_TRUCK_BUILD_COST);
                LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemNeutralValue(truck.GetCargoSystem()));
            }
        }
        
        return Values;
    }
    
    public final Map<ResourceType, Long> GetCargoSystemOffenseValue(CargoSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(!system.GetInfantries().isEmpty())
        {
            for(StoredInfantry infantry : system.GetInfantries())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, Defs.INFANTRY_UNIT_BUILD_COST);
            }
        }
        
        if(!system.GetTanks().isEmpty())
        {
            for(StoredTank tank : system.GetTanks())
            {
                if(tank.IsAnMBT())
                {
                    LaunchUtilities.AddResourceMapsTogether(Values, Defs.TANK_BUILD_COST);
                }
            }
        }
        
        if(!system.GetCargoTrucks().isEmpty())
        {
            for(StoredCargoTruck truck : system.GetCargoTrucks())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemOffenseValue(truck.GetCargoSystem()));
            }
        }
        
        return Values;
    }
    
    public final Map<ResourceType, Long> GetCargoSystemDefenseValue(CargoSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(!system.GetTanks().isEmpty())
        {
            for(StoredTank tank : system.GetTanks())
            {
                if(tank.IsASPAAG())
                {
                    LaunchUtilities.AddResourceMapsTogether(Values, Defs.TANK_BUILD_COST);
                }
            }
        }
        
        if(!system.GetCargoTrucks().isEmpty())
        {
            for(StoredCargoTruck truck : system.GetCargoTrucks())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemDefenseValue(truck.GetCargoSystem()));
            }
        }
        
        return Values;
    }
    
    public Map<ResourceType, Long> GetTankValue(TankInterface tank)
    {
        Map<ResourceType, Long> BaseValue = new ConcurrentHashMap<>();
        
        LaunchUtilities.AddResourceMapsTogether(BaseValue, Defs.TANK_BUILD_COST);
        
        if(tank.HasMissiles() || tank.HasArtillery())
        {
            LaunchUtilities.AddResourceMapsTogether(BaseValue, GetGeneralMissileSystemValue(tank.GetMissileSystem(), true));
        }
        
        if(tank.HasInterceptors())
        {
            LaunchUtilities.AddResourceMapsTogether(BaseValue, GetGeneralMissileSystemValue(tank.GetMissileSystem(), false));
        }
        
        return BaseValue;
    }
    
    public Map<ResourceType, Long> GetAircraftSystemValue(AircraftSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        for(StoredAirplane aircraft : system.GetStoredAirplanes().values())
        {
            LaunchUtilities.AddResourceMapsTogether(Values, GetAircraftValue(aircraft));
        }
        
        return Values;
    }
    
    public Map<ResourceType, Long> GetNavalVesselValue(NavalVessel vessel)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(vessel instanceof Ship)
        {
            Ship ship = (Ship)vessel;
            
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.GetNavalBuildCost(vessel.GetEntityType()));
            
            if(ship.HasCargo())
                LaunchUtilities.AddResourceMapsTogether(Values, GetCargoSystemTotalValue(ship.GetCargoSystem()));
            
            if(ship.HasMissiles())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(ship.GetMissileSystem(), true));
            }

            if(ship.HasInterceptors())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(ship.GetInterceptorSystem(), false));
            }
            
            if(ship.HasTorpedoes())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralTorpedoSystemValue(ship.GetTorpedoSystem()));
            }
            
            if(ship.HasAircraft())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetAircraftSystemValue(ship.GetAircraftSystem()));
            }
        }
        else if(vessel instanceof Submarine)
        {
            Submarine submarine = (Submarine)vessel;
            
            LaunchUtilities.AddResourceMapsTogether(Values, Defs.GetNavalBuildCost(vessel.GetEntityType()));
            
            if(submarine.HasMissiles())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(submarine.GetMissileSystem(), true));
            }

            if(submarine.HasICBMs())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralMissileSystemValue(submarine.GetICBMSystem(), true));
            }
            
            if(submarine.HasTorpedoes())
            {
                LaunchUtilities.AddResourceMapsTogether(Values, GetGeneralTorpedoSystemValue(submarine.GetTorpedoSystem()));
            }
        }
        
        return Values;
    }
    
    /**
     * GetThe value of a player-hosted missile system. Do not use this garbage for anything other than player missile systems.
     * @param system
     * @param bMissiles
     * @return 
     */
    private Map<ResourceType, Long> GetSystemValue(MissileSystem system, boolean bMissiles)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(bMissiles)
        {
            Values.put(ResourceType.WEALTH, (long)config.GetCMSSystemCost());
        }
        else
        {
            Values.put(ResourceType.WEALTH, (long)config.GetSAMSystemCost());
        }
        
        LaunchUtilities.AddResourceMapsTogether(Values, GetMissileSlotSaleValue(system, bMissiles ? config.GetInitialMissileSlots() : config.GetInitialInterceptorSlots()));
        LaunchUtilities.AddResourceMapsTogether(Values, GetReloadUpgradeSaleValue(system));
        
        for(int d = 0; d < system.GetSlotCount(); d++)
        {
            if(system.GetSlotHasMissile(d))
            {
                if(bMissiles)
                {
                    MissileType type = config.GetMissileType(system.GetSlotMissileType(d));
                    
                    if(type != null)
                    {
                        LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                    }
                }
                else
                {
                    InterceptorType type = config.GetInterceptorType(system.GetSlotMissileType(d));
                    
                    if(type != null)
                    {
                        LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                    }
                }                
            }
        }
        
        return Values;
    }
    
    /**
     * Get the value of a NON UPGRADABLE missile system, such as that in a tank, ship, aircraft, or artillery gun.
     * @param system
     * @param bMissiles
     * @return 
     */
    private Map<ResourceType, Long> GetGeneralMissileSystemValue(MissileSystem system, boolean bMissiles)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        if(system != null)
        {
            for(int d = 0; d < system.GetSlotCount(); d++)
            {
                if(system.GetSlotHasMissile(d))
                {
                    if(bMissiles)
                    {
                        MissileType type = config.GetMissileType(system.GetSlotMissileType(d));

                        if(type != null)
                        {
                            LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                        }
                    }
                    else
                    {
                        InterceptorType type = config.GetInterceptorType(system.GetSlotMissileType(d));

                        if(type != null)
                        {
                            LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                        }
                    }                
                }
            }
        }
        
        return Values;
    }
    
    private Map<ResourceType, Long> GetGeneralTorpedoSystemValue(MissileSystem system)
    {
        Map<ResourceType, Long> Values = new ConcurrentHashMap<>();
        
        for(int d = 0; d < system.GetSlotCount(); d++)
        {
            if(system.GetSlotHasMissile(d))
            {
                TorpedoType type = config.GetTorpedoType(system.GetSlotMissileType(d));

                if(type != null)
                {
                    LaunchUtilities.AddResourceMapsTogether(Values, Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost())));
                }              
            }
        }
        
        return Values;
    }
    
    public Map<ResourceType, Long> GetSaleValue(Map<ResourceType, Long> Values)
    {
        Map<ResourceType, Long> SaleValues = new ConcurrentHashMap<>();
        
        for(Entry<ResourceType, Long> entry : Values.entrySet())
        {
            SaleValues.put(entry.getKey(), (long)(entry.getValue() * config.GetResaleValue()));
        }
        
        return GetRequiredCost(SaleValues);
    }
    
    public Map<ResourceType, Long> GetSaleValue(Structure structure)
    {
        return GetSaleValue(GetRequiredCost(GetStructureValue(structure)));
    }
    
    public Map<ResourceType, Long> GetSaleValue(MissileSystem system, boolean bIsMissiles)
    {
        return GetSaleValue(GetRequiredCost(GetSystemValue(system, bIsMissiles)));
    }
    
    public Map<ResourceType, Long> GetRepairCost(MapEntity entity)
    {
        if(entity != null)
        {
            Map<ResourceType, Long> RepairCosts = new ConcurrentHashMap<>();
            
            if(entity instanceof Shipyard shipyard)
            {
                float fltHPProportion = shipyard.GetHPDeficit()/shipyard.GetMaxHP();
                LaunchUtilities.AddResourceMapsTogether(RepairCosts, LaunchUtilities.ScaleResourceMap(Defs.SHIPYARD_REPAIR_COST, fltHPProportion));
            }
            else if(entity instanceof Structure structure)
            {
                float fltHPProportion = structure.GetHPDeficit()/structure.GetMaxHP();
                
                for(Entry<ResourceType, Long> buildCost : config.GetStructureBuildCost(structure).entrySet())
                {
                    RepairCosts.put(buildCost.getKey(), (long)(buildCost.getValue() * fltHPProportion));
                }
                
                RepairCosts.put(ResourceType.CONSTRUCTION_SUPPLIES, (long)(structure.GetHPDeficit() * Defs.CONSUP_PER_HP_REPAIR_KG));
            }
            else if(entity instanceof Rubble rubble)
            {
                short nHP = StructureStats.GetMaxHPByType(rubble.GetStructureType(), this);
                
                for(Entry<ResourceType, Long> buildCost : config.GetStructureBuildCost(rubble.GetStructureType(), rubble.GetResourceType()).entrySet())
                {
                    RepairCosts.put(buildCost.getKey(), buildCost.getValue());
                }
                
                LaunchUtilities.AddResourceMapsTogether(RepairCosts, Map.ofEntries(entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(nHP * Defs.CONSUP_PER_HP_REPAIR_KG))));
            }
            
            return GetRequiredCost(RepairCosts);
        }
        
        return null;
    }
    
    public int GetHealCost(Player player)
    {
        return Integer.MAX_VALUE;
    }
    
    public int GetTimeToTarget(Missile missile)
    {
        MissileType type = config.GetMissileType(missile.GetType());
        GeoCoord geoTarget = GetMissileTarget(missile);
        
        return GetTimeToTarget(missile.GetPosition(), geoTarget, missile.GetSpeed());
    }
    
    public int GetTimeToTarget(GeoCoord geoFrom, GeoCoord geoTo, float fltSpeed)
    {
        return (int)((geoFrom.DistanceTo(geoTo) / fltSpeed) * Defs.MS_PER_HOUR_FLT);
    }
    
    public boolean GetInterceptorTooSlow(int lInterceptorType, int lMissileType)
    {
        return config.GetInterceptorSpeed(config.GetInterceptorType(lInterceptorType)) <= config.GetMissileSpeed(config.GetMissileType(lMissileType));
    }
    
    public GeoCoord GetMissileTarget(Missile missile)
    {
        if(missile.GetTracking())
        {
            MapEntity target = missile.GetTargetEntity().GetMapEntity(this);
            
            if(target != null)
            {
                return target.GetPosition();  
            }
            else
            {
                //Self destruct the missile if it doesn't know where it's going.
                missile.SelfDestruct();
                return missile.GetPosition();
            }
        }
        
        return missile.GetTarget();
    }
    
    public boolean GetRadioactive(MapEntity entity, boolean bConsiderRespawnProtection)
    {
        for(Radiation radiation : Radiations.values())
        {
            if(bConsiderRespawnProtection)
            {
                if(entity instanceof Player)
                {
                    if(((Player)entity).GetRespawnProtected())
                        return false;
                }
                else if(entity instanceof Structure)
                {
                    if(((Structure)entity).GetRespawnProtected())
                        return false;
                }
            }

            if(entity != null && entity.GetPosition().DistanceTo(radiation.GetPosition()) <= radiation.GetRadius())
            {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean GetRadioactive(GeoCoord geoCoord)
    {
        for(Radiation radiation : Radiations.values())
        {
            if(geoCoord != null && geoCoord.DistanceTo(radiation.GetPosition()) <= radiation.GetRadius())
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean GetPlayerOnline(Player player)
    {
        return System.currentTimeMillis() - player.GetLastSeen() <= Defs.PLAYER_ONLINE_TIME;
    }
    
    public long GetEndOfWeekTime()
    {
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        int lDaysDifference = Calendar.MONDAY - date.get(Calendar.DAY_OF_WEEK);
        
        if(lDaysDifference <= 0)
            lDaysDifference += 7;
        
        date.add(Calendar.DAY_OF_MONTH, lDaysDifference);
        
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        
        return date.getTimeInMillis();
    }
    
    /**
     * Return The current relationship between a pair of alliances.
     * @param lAlliance1
     * @param lAlliance2
     * @return The Allegiance type corresponding to this relationship.
     */
    public Allegiance GetAllianceRelationship(int lAlliance1, int lAlliance2)
    {
        Alliance alliance1 = GetAlliance(lAlliance1);
        
        if(alliance1 != null)
        {
            for(Treaty treaty : GetTreaties())
            {
                if(treaty != null)
                {
                    if(treaty.IsAParty(lAlliance1) && treaty.IsAParty(lAlliance2))
                    {
                        switch(treaty.GetType())
                        {
                            case AFFILIATION: return Allegiance.AFFILIATE;
                            case WAR: 
                            case SURRENDER_PROPOSAL:
                            {
                                return Allegiance.ENEMY;
                            }
                            case AFFILIATION_REQUEST:
                            {
                                return Allegiance.PENDING_TREATY;
                            } 
                        }
                    }
                }
            }
        }
        
        return Allegiance.NEUTRAL;
    }
    
    public boolean AffiliationOffered(int lAllianceBy, int lAllianceTo)
    {
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof AffiliationRequest)
            {
                if(treaty.GetAllianceID1() == lAllianceBy && treaty.GetAllianceID2() == lAllianceTo)
                    return true;
            }
        }
        
        return false;
    }
    
    public boolean SurrenderProposed(int lAllianceBy, int lAllianceTo)
    {
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof SurrenderProposal)
            {
                if(treaty.GetAllianceID1() == lAllianceBy && treaty.GetAllianceID2() == lAllianceTo)
                    return true;
            }
        }
        
        return false;
    }
    
    /**
     * A war can be declared between the stated alliances.
     * @param lAllianceDeclarer
     * @param lAllianceDeclaree
     * @return Whether a war declaration is possible.
     */
    public boolean CanDeclareWar(int lAllianceDeclarer, int lAllianceDeclaree)
    {
        switch(GetAllianceRelationship(lAllianceDeclarer, lAllianceDeclaree))
        {
            case NEUTRAL: return true; //Obviously you can declare war.
            
            case PENDING_TREATY:
            {
                //You can only declare war if you're not offering the treaty.
                return !AffiliationOffered(lAllianceDeclarer, lAllianceDeclaree);
            }
        }
        
        //In all other cases, no.
        return false;
    }
    
    /**
     * Whether an alliance can offer affiliation to another alliance.
     * @param lAllianceDeclarer
     * @param lAllianceDeclaree
     * @return Whether affiliation can be offered.
     */
    public boolean CanProposeAffiliation(int lAllianceDeclarer, int lAllianceDeclaree)
    {
        //This is only doable if the alliances are neutral. In all other cases (including affiliation already offered), it's not.
        switch(GetAllianceRelationship(lAllianceDeclarer, lAllianceDeclaree))
        {
            case NEUTRAL: return true;
        }
        
        return false;
    }
    
    public boolean CanProposeSurrender(int lAllianceDeclarer, int lAllianceDeclaree)
    {
        //This is only doable if the alliances are at war. In all other cases (including surrender already offered), it's not.
        switch(GetAllianceRelationship(lAllianceDeclarer, lAllianceDeclaree))
        {
            case ENEMY: return true;
        }
        
        return false;
    }
    
    public boolean CanBreakAffiliation(int lAllianceBreaker, int lAllianceBreakee)
    {
        /*switch(GetAllianceRelationship(lAllianceBreaker, lAllianceBreakee))
        {
            case AFFILIATE: return true;
        }
        
        Alliance breaker = GetAlliance(lAllianceBreaker);
        
        if(breaker != null)
        {
            for(int lTreatyID : new ArrayList<>(breaker.GetTreaties()))
            {
                Treaty treaty = Treaties.get(lTreatyID);
                
                if(treaty != null)
                {
                    if(treaty.GetType() == Treaty.Type.AFFILIATION_BREAK)
                    {
                        return treaty.IsAParty(lAllianceBreakee);
                    }  
                }
                else
                    breaker.RemoveTreaty(lTreatyID);
            }
        }*/
        
        return false;
    }
    
    /**
     * Get the owner of an entity. Can be null.
     * @param entity
     * @return The owner, if owned, by an existent player.
     */
    public Player GetOwner(LaunchEntity entity)
    {
        if(entity != null)
        {
            Player player = GetPlayer(entity.GetOwnerID());
        
            if(player != null)
                return player;
        }
        
        return null;
    }
    
    /**
     * Get the allegiance between two known players.
     * @param player1
     * @param player2
     * @return The allegiance between the players.
     */
    public Allegiance GetAllegiance(Player player1, Player player2)
    {
        if(player1 != null && player2 != null)
        {
            if(player1.GetID() == player2.GetID())
            {
                //It's you, you wally.
                return Allegiance.YOU;
            }
            else if(player1.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED || player2.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
                return Allegiance.UNAFFILIATED;
            else if(player1.GetAllianceMemberID() == player2.GetAllianceMemberID())
                return Allegiance.ALLY;
            else if(player2.GetAllianceJoiningID() == player1.GetAllianceMemberID() || player1.GetAllianceJoiningID() == player2.GetAllianceMemberID())
                return Allegiance.PENDING_TREATY;
            else if(player1.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player2.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                return GetAllianceRelationship(player1.GetAllianceMemberID(), player2.GetAllianceMemberID());
        }  
        
        return Allegiance.NEUTRAL;
    }
    
    /**
     * Get the allegiance between two entities.
     * @param entity1
     * @param entity2
     * @return The allegiance between the entities.
     */
    public Allegiance GetAllegiance(LaunchEntity entity1, LaunchEntity entity2)
    {
        try
        {
            Player player1 = GetOwner(entity1);
            Player player2 = GetOwner(entity2);

            if(player1 != null && player2 != null)
            {
                return GetAllegiance(player1, player2);
            }

            if(entity1.GetOwnerID() == LaunchEntity.ID_NONE || entity2.GetOwnerID() == LaunchEntity.ID_NONE)
                return Allegiance.UNAFFILIATED;

            return Allegiance.NEUTRAL;
        }
        catch(Exception ex)
        {
            return Allegiance.NEUTRAL;
        }   
    }
    
    public Allegiance GetAllegiance(Player player, Alliance alliance)
    {
        if(player != null && alliance != null)
        {
            if(player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                //All alliances that the player isn't joining are neutral.
                if(player.GetAllianceJoiningID() == alliance.GetID())
                    return Allegiance.PENDING_TREATY;
                else
                    return Allegiance.NEUTRAL;
            }
            else if(player.GetAllianceMemberID() == alliance.GetID())
            {
                //Is the player in this alliance?
                return Allegiance.YOU;
            }

            for(Treaty treaty : Treaties.values())
            {
                if(treaty.AreParties(player.GetAllianceMemberID(), alliance.GetID()))
                {
                    if(treaty instanceof War || treaty instanceof SurrenderProposal)
                        return Allegiance.ENEMY;
                    if(treaty instanceof Affiliation)
                        return Allegiance.AFFILIATE;
                    if(treaty instanceof AffiliationRequest)
                        return Allegiance.PENDING_TREATY;
                }
            }
        }  
        
        //In all other cases, it's neutral.
        return Allegiance.NEUTRAL;
    }
    
    public boolean WouldBeFriendlyFire(Player player, Player otherPlayer)
    {
        switch(GetAllegiance(player, otherPlayer))
        {
            case YOU:
            case AFFILIATE:
            case ALLY:
                return true;
        }
        
        return false;
    }
    
    public int GetAllianceMemberCount(Alliance alliance)
    {
        //TODO: Optimize
        int lResult = 0;
        
        if(alliance != null)
        {
            for(Player player : Players.values())
            {
                if(player.GetAllianceMemberID() == alliance.GetID() && !player.GetAWOL())
                    lResult++;
            }
        }  
        
        return lResult;
    }
    
    public boolean GetAllianceIsLeaderless(Alliance alliance)
    {
        //TODO: Optimize
        int lLeaders = 0;
        
        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID() && player.GetIsAnMP() && !player.GetAWOL() && player.Functioning())
                lLeaders++;
        }
        
        return lLeaders == 0;
    }
    
    /**
     * Return all enemies of an alliance (those the alliance is engaged in wars with).
     * @param alliance The alliance to query.
     * @return A list of alliances this alliance is at war with.
     */
    public List<Alliance> GetEnemies(Alliance alliance)
    {
        List<Alliance> Result = new ArrayList();
        
        for(Treaty treaty : Treaties.values())
        {
            if(treaty.GetType() == Treaty.Type.WAR)
            {
                if(treaty.IsAParty(alliance.GetID()))
                    Result.add(Alliances.get(treaty.OtherParty(alliance.GetID())));
            }
        }
        
        return Result;
    }
    
    /**
     * Return all affiliates of an alliance.
     * @param alliance The alliance to query.
     * @return A list of alliances this alliance is affiliated with.
     */
    public List<Alliance> GetAffiliates(Alliance alliance)
    {
        List<Alliance> Result = new ArrayList();
        
        for(Treaty treaty : Treaties.values())
        {
            if(treaty.GetType() == Treaty.Type.AFFILIATION)
            {
                if(treaty.IsAParty(alliance.GetID()))
                    Result.add(Alliances.get(treaty.OtherParty(alliance.GetID())));
            }
        }
        
        return Result;
    }
    
    /**
     * Get the war involving both specified alliances. Return NULL if there isn't one.
     * @param lAllianceID1
     * @param lAllianceID2
     * @return The war the alliances are having with eachother, otherwise NULL.
     */
    public War GetWar(int lAllianceID1, int lAllianceID2)
    {
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof War)
            {
                War war = (War)treaty;

                if(war.AreParties(lAllianceID1, lAllianceID2))
                    return war;
            }
        }
        
        return null;
    }
    
    public Affiliation GetAffiliation(int lAllianceID1, int lAllianceID2)
    {
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof Affiliation)
            {
                Affiliation affiliation = (Affiliation)treaty;

                if(affiliation.AreParties(lAllianceID1, lAllianceID2))
                    return affiliation;
            }
        }
        
        return null;
    }
    
    public SurrenderProposal GetSurrenderProposal(int lAllianceID1, int lAllianceID2)
    {
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof SurrenderProposal)
            {
                SurrenderProposal surrender = (SurrenderProposal)treaty;

                if(surrender.AreParties(lAllianceID1, lAllianceID2))
                    return surrender;
            }
        }
        
        return null;
    }
    
    /**
     * Return all members of an alliance.
     * @param alliance The alliance to query.
     * @return A list of players who are members of this alliance.
     */
    public List<Player> GetMembers(Alliance alliance)
    {
        //TODO: Optimize
        List<Player> Result = new ArrayList();
        
        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
                Result.add(player);
        }
        
        return Result;
    }
    
    public void SetPlayerOffenseValues(Player player)
    {
        if(player != null)
        {
            LaunchLog.ConsoleMessage(String.format("Processing player offense Value: %s", player.GetName()));
            
            int lValue = 0;
            
            //MissileSites & ICBM silos
            
            for(MissileSite site : GetMissileSites())
            {
                if(site.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(site).get(ResourceType.WEALTH);
                }
            }
            
            //Artillery Guns
            
            for(ArtilleryGun gun : GetArtilleryGuns())
            {
                if(gun.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(gun).get(ResourceType.WEALTH);
                }
            }
            
            //Tanks
            
            for(TankInterface tank : GetAllTanks())
            {
                if(tank.GetOwnedBy(player.GetID()))
                {
                    lValue += Defs.TANK_BUILD_COST.get(ResourceType.WEALTH);
                    
                    if(tank.GetType() == EntityType.HOWITZER || tank.GetType() == EntityType.MISSILE_TANK)
                    {
                        if(GetGeneralMissileSystemValue(tank.GetMissileSystem(), true).get(ResourceType.WEALTH) != null)
                        {
                            lValue += GetGeneralMissileSystemValue(tank.GetMissileSystem(), true).get(ResourceType.WEALTH);
                        }
                    }
                }
            }
            
            //Infantry
            
            lValue += (GetInfantryCount(player.GetID()) * Defs.INFANTRY_UNIT_BUILD_COST.get(ResourceType.WEALTH));
            
            for(Ship ship : GetShips())
            {
                if(ship.GetOwnedBy(player.GetID()))
                {
                    if(ship.IsOffensive())
                    {
                        lValue += GetNavalVesselValue(ship).get(ResourceType.WEALTH);
                    }
                }
            }
            
            //Submarines
            
            for(Submarine submarine : GetSubmarines())
            {
                if(submarine.GetOwnedBy(submarine.GetID()))
                {
                    lValue += GetNavalVesselValue(submarine).get(ResourceType.WEALTH);
                }
            }
            
            //Airplanes
            
            for(AirplaneInterface airplane : GetAllAirplanes())
            {
                if(airplane.GetOwnedBy(player.GetID()))
                {
                    if(airplane.GetAircraftType() == EntityType.BOMBER 
                        || airplane.GetAircraftType() == EntityType.STEALTH_BOMBER
                        || airplane.GetAircraftType() == EntityType.SSB
                        || airplane.GetAircraftType() == EntityType.AWACS
                        || airplane.GetAircraftType() == EntityType.ATTACK_AIRCRAFT)
                    {
                        lValue += Defs.GetAircraftBuildCost(airplane.GetAircraftType()).get(ResourceType.WEALTH);
                    }
                    else if(airplane.GetAircraftType() == EntityType.MULTI_ROLE)
                    {
                        lValue += Defs.GetAircraftBuildCost(airplane.GetAircraftType()).get(ResourceType.WEALTH)/2;
                    }
                    
                    if(airplane.GetMissileSystem() != null)
                    {
                        if(GetGeneralMissileSystemValue(airplane.GetMissileSystem(), true).get(ResourceType.WEALTH) != null)
                        {
                            lValue += GetGeneralMissileSystemValue(airplane.GetMissileSystem(), true).get(ResourceType.WEALTH);
                        }
                    }
                }
            }

            player.SetOffenseValue(lValue);
            LaunchLog.ConsoleMessage(String.format("%s offense value: $%d", player.GetName(), lValue));
        }
    }
    
    public void SetPlayerDefenseValues(Player player)
    {
        if(player != null)
        {
            LaunchLog.ConsoleMessage(String.format("Processing player defense Value: %s", player.GetName()));
            
            int lValue = 0;
            
            for(SAMSite site : GetSAMSites())
            {
                if(site.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(site).get(ResourceType.WEALTH);
                }
            }
            
            //Sentry Guns
            
            for(SentryGun sentry : GetSentryGuns())
            {
                if(sentry.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(sentry).get(ResourceType.WEALTH);
                }
            }
            
            //SPAAGs and SAM tanks
            
            for(TankInterface tank : GetAllTanks())
            {
                if(tank.GetOwnedBy(player.GetID()))
                {
                    lValue += Defs.TANK_BUILD_COST.get(ResourceType.WEALTH);
                    
                    if(tank.GetType() == EntityType.SAM_TANK)
                    {
                        if(GetGeneralMissileSystemValue(tank.GetMissileSystem(), false).get(ResourceType.WEALTH) != null)
                        {
                            lValue += GetGeneralMissileSystemValue(tank.GetMissileSystem(), false).get(ResourceType.WEALTH);
                        }
                    }
                }
            }
            
            //Airplanes
            
            for(AirplaneInterface airplane : GetAllAirplanes())
            {
                if(airplane.GetOwnedBy(player.GetID()))
                {
                    if(airplane.GetAircraftType() == EntityType.FIGHTER || airplane.GetAircraftType() == EntityType.STEALTH_FIGHTER)
                    {
                        lValue += Defs.GetAircraftBuildCost(airplane.GetAircraftType()).get(ResourceType.WEALTH);
                    }
                    else if(airplane.GetAircraftType() == EntityType.MULTI_ROLE)
                    {
                        lValue += Defs.GetAircraftBuildCost(airplane.GetAircraftType()).get(ResourceType.WEALTH)/2;
                    }
                    
                    if(airplane.GetInterceptorSystem() != null)
                    {
                        Map<ResourceType, Long> value = GetGeneralMissileSystemValue(airplane.GetInterceptorSystem(), false);
                        
                        if(value != null && value.get(ResourceType.WEALTH) != null)
                        {
                            lValue += value.get(ResourceType.WEALTH);
                        }
                    }
                }
            }
            
            //Ships
            
            for(Ship ship : GetShips())
            {
                if(ship.GetOwnedBy(player.GetID()))
                {
                    Map<ResourceType, Long> value = GetGeneralMissileSystemValue(ship.GetInterceptorSystem(), false);
                    
                    if(ship.GetInterceptorSystem() != null && value.get(ResourceType.WEALTH) != null)
                    {
                        lValue += value.get(ResourceType.WEALTH);
                    }
                }   
            }

            player.SetDefenseValue(lValue);
            LaunchLog.ConsoleMessage(String.format("%s defense value: $%d", player.GetName(), lValue));
        }
    }
    
    public void SetPlayerNeutralValues(Player player)
    {
        if(player != null)
        {
            LaunchLog.ConsoleMessage(String.format("Processing player neutral Value: %s", player.GetName()));
            
            int lValue = 0;
            
            for(Ship ship : GetShips())
            {
                if(ship.GetOwnedBy(player.GetID()))
                {
                    if(ship.GetEntityType() == EntityType.CARGO_SHIP)
                    {
                        lValue += Defs.CARGO_SHIP_BUILD_COST.get(ResourceType.WEALTH);
                        
                        if(ship.GetCargoSystem() != null && ship.GetCargoSystem().ContainsResourceType(ResourceType.WEALTH))
                        {
                            lValue += ship.GetCargoSystem().GetAmountOfType(ResourceType.WEALTH);
                        }
                    }
                }
            }
            
            //Airplanes
            
            for(AirplaneInterface airplane : GetAllAirplanes())
            {
                if(airplane.GetOwnedBy(player.GetID()))
                {
                    if(airplane.GetAircraftType() == EntityType.CARGO_PLANE || airplane.GetAircraftType() == EntityType.REFUELER)
                    {
                        lValue += Defs.CARGO_PLANE_BUILD_COST.get(ResourceType.WEALTH);
                        
                        if(airplane.GetCargoSystem() != null && airplane.GetCargoSystem().ContainsResourceType(ResourceType.WEALTH))
                        {
                            lValue += airplane.GetCargoSystem().GetAmountOfType(ResourceType.WEALTH);
                        }
                    }
                }
            }
            
            //Cargo trucks
            
            for(CargoTruckInterface truck : GetAllCargoTrucks())
            {
                if(truck.GetOwnedBy(player.GetID()))
                {
                    lValue += Defs.CARGO_TRUCK_BUILD_COST.get(ResourceType.WEALTH);
                    
                    if(truck.GetCargoSystem().ContainsResourceType(ResourceType.WEALTH))
                    {
                        lValue += truck.GetCargoSystem().GetAmountOfType(ResourceType.WEALTH);
                    }
                }
            }
            
            //Warehouses
            
            for(Warehouse warehouse : GetWarehouses())
            {
                if(warehouse.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(warehouse).get(ResourceType.WEALTH);
                }
            }
            
            //Extractors
            
            for(OreMine oreMine : GetOreMines())
            {
                if(oreMine.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(oreMine).get(ResourceType.WEALTH);
                }
            }
            
            //Processors
            
            for(Processor processor : GetProcessors())
            {
                if(processor.GetOwnedBy(player.GetID()))
                {
                    lValue += GetStructureValue(processor).get(ResourceType.WEALTH);
                }
            }
            
            lValue += player.GetWealth();

            player.SetNeutralValue(lValue);
            LaunchLog.ConsoleMessage(String.format("%s neutral value: $%d", player.GetName(), lValue));
        }
    }
    
    public long GetPlayerTotalResources(int lPlayerID)
    {
        long lTotal = 0;
        
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            //Trucks
            for(CargoTruckInterface truck : GetAllCargoTrucks())
            {
                if(truck.GetOwnedBy(lPlayerID))
                {
                    for(Resource resource : truck.GetCargoSystem().GetResources())
                    {
                        lTotal += resource.GetQuantity();
                    }
                }  
            }

            //Planes
            for(AirplaneInterface plane : GetAllAirplanes())
            {
                if(plane.GetOwnedBy(lPlayerID) && plane.HasCargo())
                {
                    for(Resource resource : plane.GetCargoSystem().GetResources())
                    {
                        lTotal += resource.GetQuantity();
                    }
                }  
            }

            //Ships
            for(Ship ship : GetShips())
            {
                if(ship.GetOwnedBy(lPlayerID) && ship.HasCargo())
                {
                    for(Resource resource : ship.GetCargoSystem().GetResources())
                    {
                        lTotal += resource.GetQuantity();
                    }
                }  
            }
        }
        
        return lTotal;
    }
    
    public boolean IsInterceptorValidForMissile(InterceptorType interceptorType, MissileType missileType, Missile missile)
    {
        return (interceptorType.GetABM() && missileType.GetICBM()) || (!interceptorType.GetABM() && !missileType.GetICBM() && interceptorType.GetInterceptorSpeed() >= missileType.GetMissileSpeed());
    }

    public float GetNetWorthMultiplier(Player inflictor, Player inflictee)
    {
        float fltInflictorWorth = inflictor.GetTotalValue();
        float fltInflicteeWorth = inflictee.GetTotalValue();
        
        if(inflictor.GetTotalValue() == 0 && inflictee.GetTotalValue() == 0)
        {
            return 1.0f;
        }

        return fltInflicteeWorth / fltInflictorWorth;
    }
    
    public int GetAllianceNeutralValue(Alliance alliance)
    {
        int lValue = 0;
        
        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
                lValue += player.GetNeutralValue();
        }
        
        return lValue;
    }
    
    public int GetAllianceOffenseValue(Alliance alliance)
    {
        int lValue = 0;
        
        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
                lValue += player.GetOffenseValue();
        }
        
        return lValue;
    }
    
    public int GetAllianceDefenseValue(Alliance alliance)
    {
        int lValue = 0;

        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
                lValue += player.GetDefenseValue();
        }
        
        return lValue;
    }
    
    public int GetAllianceTotalValue(Alliance alliance)
    {
        int lValue = 0;
        
        for(Player player : Players.values())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
                lValue += player.GetTotalValue();
        }
        
        lValue += alliance.GetWealth();
        
        return lValue;
    }
    
    public int GetPlayerRankIncome(Player player)
    {
        if(!player.GetAWOL())
        {
            return player.GetRank() * Defs.INCOME_PER_RANK;
        }
        
        return 0;
    }
    
    public int GetHourlyIncome(Player player)
    {
        if(!player.GetAWOL())
        {
            int lAmountToAdd = 0;

            if(GetBasicIncomeEligible(player))
                lAmountToAdd += config.GetHourlyWealth(player);
            
            return lAmountToAdd;
        }
        
        return 0;
    }
    
    public boolean GetBasicIncomeEligible(Player player)
    {
        return !player.GetAWOL();
    }
    
    public boolean GetDiplomaticPresenceEligible(Player player)
    {
        return player.GetAvatarID() != Defs.THE_GREAT_BIG_NOTHING && !player.GetAWOL();
    }
    
    public boolean GetPoliticalEngagementEligible(Player player)
    {
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && !player.GetAWOL())
        {
            Alliance alliance = Alliances.get(player.GetAllianceMemberID());
            
            return alliance.GetAvatarID() != Defs.THE_GREAT_BIG_NOTHING;
        }
        
        return false;
    }
    
    public boolean GetDefenderOfTheNationEligible(Player player)
    {
        if(!player.GetAWOL())
        {
            for(SAMSite samSite : SAMSites.values())
            {
                if(samSite.GetOwnedBy(player.GetID()))
                {
                    if(samSite.GetOnline())
                        return true;
                }
            }

            for(SentryGun sentryGun : SentryGuns.values())
            {
                if(sentryGun.GetOwnedBy(player.GetID()))
                {
                    if(sentryGun.GetOnline())
                        return true;
                }
            } 
        }
        
        return false;
    }
    
    public boolean GetNuclearSuperpowerEligible(Player player)
    {
        if(!player.GetAWOL())
        {
            for(MissileSite site : MissileSites.values())
            {
                if(site.CanTakeICBM())
                {
                    if(site.GetOwnedBy(player.GetID()))
                        return true;
                }
            }    
        }
        
        return false;
    }
    
    public boolean GetSurvivorEligible(Player player)
    {
        return player.GetDeaths() == 0;
    }
    
    public boolean GetHippyEligible(Player player)
    {
        return player.GetOffenceSpending() == 0 && !player.GetAWOL();
    }
    
    public boolean GetPeaceMakerEligible(Player player)
    {
        if(!player.GetAWOL())
        {
            int lFriends = 0;
            int lActivePlayers = 0;

            for(Player otherPlayer : Players.values())
            {
                if(!otherPlayer.GetAWOL())
                {
                    lActivePlayers++;

                    switch(GetAllegiance(player, otherPlayer))
                    {
                        case YOU:
                        case ALLY:
                        case AFFILIATE:
                            lFriends++;
                            break;
                    }
                }
            }

            if(lActivePlayers > 0)
                return ((float)lFriends / (float)lActivePlayers > Defs.RELATIONSHIP_BONUS_THRESHOLD);    
        }
        
        
        return false;
    }
    
    public boolean GetWarMongerEligible(Player player)
    {
        if(!player.GetAWOL())
        {
            int lEnemies = 0;
            int lActivePlayers = 0;

            for(Player otherPlayer : Players.values())
            {
                if(!otherPlayer.GetAWOL())
                {
                    lActivePlayers++;

                    switch(GetAllegiance(player, otherPlayer))
                    {
                        case ENEMY:
                            lEnemies++;
                            break;
                    }
                }
            }

            if(lActivePlayers > 0)
                return ((float)lEnemies / (float)lActivePlayers > Defs.RELATIONSHIP_BONUS_THRESHOLD);    
        }
        
        
        return false;
    }
    
    public boolean GetLoneWolfEligible(Player player)
    {
        /*if(!player.Destroyed())
        {
            for(Player otherPlayer : Players.values())
            {
                if(!otherPlayer.GetAWOL())
                {
                    if(!otherPlayer.ApparentlyEquals(player))
                    {
                        if(otherPlayer.GetPosition().DistanceTo(player.GetPosition()) < config.GetLoneWolfDistance())
                            return false;
                    }
                }
            }    
        }*/
        
        return false;
    }

    public float GetInterceptorAccuracy(InterceptorType type)
    {
        return Defs.INTERCEPTOR_ACCURACY;
    }
    
    public float GetECMHitChanceReduction()
    {
        return config.GetECMInterceptorChanceReduction();   
    }
    
    public int GetTotalPlayerCount()
    {
        int lPlayerCount = 0;
        
        for(Player player : Players.values())
        {
            lPlayerCount++;
        }
        
        return lPlayerCount;
    }
    
    public int GetOnlinePlayerCount()
    {
        int lPlayerCount = 0;
        
        for(Player player : Players.values())
        {
            if(GetPlayerOnline(player))
            {
                lPlayerCount += 1;
            }            
        }
        
        return lPlayerCount;
    }
    
    public boolean MissileCanReach(MissileType type, GeoCoord geoOrigin, GeoCoord geoTarget)
    {
        float range = type.GetMissileRange();
        float targetDistance = geoOrigin.DistanceTo(geoTarget);
        
        return range >= targetDistance;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Public entity creators.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public void AddBlueprint(Blueprint blueprint)
    {
        blueprint.SetListener(this);
        Blueprints.put(blueprint.GetID(), blueprint);
        
        EntityUpdated(blueprint, true);
        
        Player owner = GetOwner(blueprint);
        
        if(owner != null)
            owner.AddOwnedEntity(blueprint);
        
        EntityAdded(blueprint);
    }
    
    public void AddAlliance(Alliance alliance, boolean bMajor)
    {
        alliance.SetListener(this);
        
        Alliances.put(alliance.GetID(), alliance);
        
        if(!AllianceMemberRosters.containsKey(alliance.GetID()))
        {
            Map<Integer, Player> Roster = new ConcurrentHashMap<>();
            
            AllianceMemberRosters.put(alliance.GetID(), Roster);
        }
        
        AllianceUpdated(alliance, bMajor);
    }
    
    public void AddTreaty(Treaty treaty)
    {
        Treaties.put(treaty.GetID(), treaty);
        
        TreatyUpdated(treaty);
        
        Alliance alliance1 = GetAlliance(treaty.GetAllianceID1());
        Alliance alliance2 = GetAlliance(treaty.GetAllianceID2());
        
        if(alliance1 != null)
            alliance1.AddTreaty(treaty.GetID());
        
        if(alliance2 != null)
            alliance2.AddTreaty(treaty.GetID());
    }
    
    public void AddCargoSystem(CargoSystem system, EntityPointer host)
    {
        if(!system.GetInfantries().isEmpty())
        {
            for(StoredInfantry infantry : system.GetInfantries())
            {
                infantry.SetHost(host);
                AddStoredInfantry(infantry);
            }
        }
        
        if(!system.GetTanks().isEmpty())
        {
            for(StoredTank tank : system.GetTanks())
            {
                tank.SetHost(host);
                AddStoredTank(tank);
            }
        }
        
        if(!system.GetCargoTrucks().isEmpty())
        {
            for(StoredCargoTruck truck : system.GetCargoTrucks())
            {
                truck.SetHost(host);
                AddStoredCargoTruck(truck);
            }
        }
    }
    
    public void AddPlayer(Player player)
    {
        player.SetListener(this);
        Players.put(player.GetID(), player);
        player.SetGame(this);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            if(AllianceMemberRosters.containsKey(player.GetAllianceMemberID()))
            {
                Map<Integer, Player> Members = AllianceMemberRosters.get(player.GetAllianceMemberID());
                
                if(Members != null)
                {
                    if(!Members.containsKey(player.GetID()))
                    {
                        Members.put(player.GetID(), player);
                        AllianceMemberRosters.put(player.GetAllianceMemberID(), Members);
                    }
                }
            }
        }
        
        EntityUpdated(player, false);
        EntityAdded(player);
    }
    
    public void AddMissile(Missile missile)
    {
        missile.SetListener(this);
        Missiles.put(missile.GetID(), missile);
        
        EntityUpdated(missile, false);
        
        Player owner = GetOwner(missile);
        
        if(owner != null)
            owner.AddOwnedEntity(missile);
        
        EntityAdded(missile);
    }
    
    public void AddTorpedo(Torpedo torpedo)
    {
        torpedo.SetListener(this);
        Torpedoes.put(torpedo.GetID(), torpedo);
        
        EntityUpdated(torpedo, false);
        
        Player owner = GetOwner(torpedo);
        
        if(owner != null)
            owner.AddOwnedEntity(torpedo);
        
        EntityAdded(torpedo);
    }
    
    public void AddInterceptor(Interceptor interceptor)
    {
        interceptor.SetListener(this);
        Interceptors.put(interceptor.GetID(), interceptor);
        
        EntityUpdated(interceptor, false);
        
        //Interceptors don't need to be added to the quadtree.
    }
    
    public void AddMissileSite(MissileSite missileSite)
    {
        missileSite.SetListener(this);
        MissileSites.put(missileSite.GetID(), missileSite);
        
        EntityUpdated(missileSite, false);
        
        Player owner = GetOwner(missileSite);
        
        if(owner != null)
            owner.AddOwnedEntity(missileSite);
        
        EntityAdded(missileSite);
    }
    
    public void AddAirdrop(Airdrop airdrop)
    {
        airdrop.SetListener(this);
        Airdrops.put(airdrop.GetID(), airdrop);
        EntityUpdated(airdrop, false);
        EntityAdded(airdrop);
    }
    
    public void AddSAMSite(SAMSite samSite)
    {
        samSite.SetListener(this);
        SAMSites.put(samSite.GetID(), samSite);
        
        EntityUpdated(samSite, false);
        
        Player owner = GetOwner(samSite);
        
        if(owner != null)
            owner.AddOwnedEntity(samSite);
        
        EntityAdded(samSite);
    }
    
    public void AddOreMine(OreMine oreMine)
    {
        oreMine.SetListener(this);
        OreMines.put(oreMine.GetID(), oreMine);
        
        EntityUpdated(oreMine, false);
        
        Player owner = GetOwner(oreMine);
        
        if(owner != null)
            owner.AddOwnedEntity(oreMine);
        
        EntityAdded(oreMine);
    }
    
    public void AddRadarStation(RadarStation radarStation)
    {
        radarStation.SetListener(this);
        RadarStations.put(radarStation.GetID(), radarStation);
        
        EntityUpdated(radarStation, false);
        
        Player owner = GetOwner(radarStation);
        
        if(owner != null)
            owner.AddOwnedEntity(radarStation);
        
        EntityAdded(radarStation);
    }
    
    public void AddCommandPost(CommandPost commandPost)
    {
        commandPost.SetListener(this);
        CommandPosts.put(commandPost.GetID(), commandPost);
        
        EntityUpdated(commandPost, false);
        
        Player owner = GetOwner(commandPost);
        
        if(owner != null)
            owner.AddOwnedEntity(commandPost);
        
        EntityAdded(commandPost);
    }
    
    public void AddAirbase(Airbase airbase)
    {
        airbase.SetListener(this);
        Airbases.put(airbase.GetID(), airbase);
        
        for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
        {
            AddStoredAircraft(aircraft);
        }
        
        EntityUpdated(airbase, false);
        
        Player owner = GetOwner(airbase);
        
        if(owner != null)
            owner.AddOwnedEntity(airbase);
        
        EntityAdded(airbase);
    }
    
    public void AddArmory(Armory armory)
    {
        armory.SetListener(this);
        Armories.put(armory.GetID(), armory);
        
        EntityUpdated(armory, false);
        
        Player owner = GetOwner(armory);
        
        if(owner != null)
            owner.AddOwnedEntity(armory);
        
        AddCargoSystem(armory.GetCargoSystem(), armory.GetPointer());
        
        EntityAdded(armory);
    }
    
    public void AddBank(Bank bank)
    {
        bank.SetListener(this);
        Banks.put(bank.GetID(), bank);
        
        EntityUpdated(bank, false);
        
        Player owner = GetOwner(bank);
        
        if(owner != null)
            owner.AddOwnedEntity(bank);
        
        EntityAdded(bank);
    }
    
    public void AddWarehouse(Warehouse warehouse)
    {
        warehouse.SetListener(this);
        Warehouses.put(warehouse.GetID(), warehouse);
        
        EntityUpdated(warehouse, false);
        
        Player owner = GetOwner(warehouse);
        
        if(owner != null)
        {
            owner.AddOwnedEntity(warehouse);
        }
        
        EntityAdded(warehouse);
    }
    
    public void AddMissileFactory(MissileFactory factory)
    {
        factory.SetListener(this);
        MissileFactorys.put(factory.GetID(), factory);
        
        EntityUpdated(factory, false);
        
        Player owner = GetOwner(factory);
        
        if(owner != null)
            owner.AddOwnedEntity(factory);
        
        EntityAdded(factory);
    }
    
    public void AddAircraft(Airplane aircraft)
    {
        aircraft.SetListener(this);
        Airplanes.put(aircraft.GetID(), aircraft);
        EntityUpdated(aircraft, false);
        
        Player owner = GetOwner(aircraft);
        
        if(owner != null)
            owner.AddOwnedEntity(aircraft);
        
        if(aircraft.HasCargo())
        {
            AddCargoSystem(aircraft.GetCargoSystem(), aircraft.GetPointer());
        }
        
        EntityAdded(aircraft);
    }
    
    /**
     * This doesn't work the same way as other entities. Inside the AircraftSystem, as all StoredAircraft objects are, they are treated more like missiles. 
     * As such, the system and the host entity are the "accountants" for the StoredAircraft.
     * @param aircraft 
     */
    public void AddStoredAircraft(StoredAirplane aircraft)
    {
        Airplanes.put(aircraft.GetID(), aircraft);
        
        AircraftSystem system = null;
        MapEntity hostEntity = aircraft.GetHomeBase().GetMapEntity(this);
        
        if(hostEntity instanceof Airbase)
        {
            system = ((Airbase)hostEntity).GetAircraftSystem();
        }
        else if(hostEntity instanceof Ship)
        {
            system = ((Ship)hostEntity).GetAircraftSystem();
        }
        
        if(aircraft.HasCargo())
        {
            AddCargoSystem(aircraft.GetCargoSystem(), aircraft.GetPointer());
        }
        
        if(system != null)
            system.AddAirplane(aircraft);
        else
            Airplanes.remove(aircraft.GetID());
        
        Player owner = GetOwner(aircraft);
        
        if(owner != null)
            owner.AddOwnedEntity(aircraft);
    }
    
    public void AddStoredInfantry(StoredInfantry infantry)
    {
        Infantries.put(infantry.GetID(), infantry);
        LaunchEntity hostEntity = infantry.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();
        
        if(system != null)
            system.AddInfantry(infantry);
        else
            Infantries.remove(infantry.GetID());
        
        Player owner = GetOwner(infantry);
        
        if(owner != null)
            owner.AddOwnedEntity(infantry);
    }
    
    public void AddStoredCargoTruck(StoredCargoTruck truck)
    {
        CargoTrucks.put(truck.GetID(), truck);
        LaunchEntity hostEntity = truck.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();
        
        if(system != null)
            system.AddCargoTruck(truck);
        else
            CargoTrucks.remove(truck.GetID());
        
        Player owner = GetOwner(truck);
        
        if(owner != null)
            owner.AddOwnedEntity(truck);
    }
    
    public void AddStoredTank(StoredTank tank)
    {
        Tanks.put(tank.GetID(), tank);
        LaunchEntity hostEntity = tank.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();
        
        if(system != null)
            system.AddTank(tank);
        else
            Tanks.remove(tank.GetID());
        
        Player owner = GetOwner(tank);
        
        if(owner != null)
            owner.AddOwnedEntity(tank);
    }
    
    public void AddInfantry(InfantryInterface infantry)
    {
        ((LaunchEntity)infantry).SetListener(this);
        Infantries.put(infantry.GetID(), infantry);
        EntityUpdated(((LaunchEntity)infantry), false);
        
        Player owner = GetOwner(((LaunchEntity)infantry));
        
        if(owner != null)
            owner.AddOwnedEntity(((LaunchEntity)infantry));
        
        EntityAdded(((LaunchEntity)infantry));
    }
    
    public void AddCargoTruck(CargoTruck truck)
    {
        truck.SetListener(this);
        CargoTrucks.put(truck.GetID(), truck);
        EntityUpdated(truck, false);
        
        Player owner = GetOwner(truck);
        
        if(owner != null)
            owner.AddOwnedEntity(truck);
        
        AddCargoSystem(truck.GetCargoSystem(), truck.GetPointer());
        
        EntityAdded(truck);
    }
    
    public void AddShipyard(Shipyard shipyard)
    {
        shipyard.SetListener(this);
        Shipyards.put(shipyard.GetID(), shipyard);
        EntityUpdated(shipyard, false);
        
        Player owner = GetOwner(shipyard);
        
        if(owner != null)
            owner.AddOwnedEntity(shipyard);
        
        EntityAdded(shipyard);
    }
    
    public void AddProcessor(Processor processor)
    {
        processor.SetListener(this);
        Processors.put(processor.GetID(), processor);
        EntityUpdated(processor, false);
        
        Player owner = GetOwner(processor);
        
        if(owner != null)
            owner.AddOwnedEntity(processor);
        
        EntityAdded(processor);
    }
    
    public void AddDistributor(Distributor distributor)
    {
        distributor.SetListener(this);
        Distributors.put(distributor.GetID(), distributor);
        EntityUpdated(distributor, false);
        
        Player owner = GetOwner(distributor);
        
        if(owner != null)
            owner.AddOwnedEntity(distributor);
        
        EntityAdded(distributor);
    }
    
    public void AddTank(TankInterface tank)
    {
        ((LaunchEntity)tank).SetListener(this);
        Tanks.put(tank.GetID(), tank);
        EntityUpdated(((LaunchEntity)tank), false);
        
        Player owner = GetOwner(((LaunchEntity)tank));
        
        if(owner != null)
            owner.AddOwnedEntity(((LaunchEntity)tank));
        
        EntityAdded(((LaunchEntity)tank));
    }
    
    public void AddShip(Ship ship)
    {
        ship.SetListener(this);
        Ships.put(ship.GetID(), ship);
        
        if(ship.HasAircraft())
        {
            for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
            {
                AddStoredAircraft(aircraft);
            }
        }
        
        if(ship.HasCargo())
        {
            AddCargoSystem(ship.GetCargoSystem(), ship.GetPointer());
        }
        
        EntityUpdated(ship, false);
        
        Player owner = GetOwner(ship);
        
        if(owner != null)
            owner.AddOwnedEntity(ship);
        
        EntityAdded(ship);
    }
    
    public void AddSubmarine(Submarine submarine)
    {
        submarine.SetListener(this);
        Submarines.put(submarine.GetID(), submarine);
        EntityUpdated(submarine, false);
        
        Player owner = GetOwner(submarine);
        
        if(owner != null)
            owner.AddOwnedEntity(submarine);
        
        EntityAdded(submarine);
    }
    
    public void RemoveStoredAircraft(StoredAirplane aircraft)
    {
        MapEntity hostEntity = aircraft.GetHomeBase().GetMapEntity(this);
        AircraftSystem system = null;

        if(hostEntity instanceof Airbase)
        {
            system = ((Airbase)hostEntity).GetAircraftSystem();
        }
        else if(hostEntity instanceof Ship)
        {
            system = ((Ship)hostEntity).GetAircraftSystem();
        }
        
        Airplanes.remove(aircraft.GetID());
        EntityRemoved(aircraft, false);
        
        if(system != null)
            system.RemoveAndGetAirplane(aircraft.GetID());
    }
    
    public void RemoveStoredInfantry(StoredInfantry infantry)
    {
        LaunchEntity hostEntity = infantry.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();

        Infantries.remove(infantry.GetID());
        EntityRemoved(infantry, false);
        
        if(system != null)
        {
            system.RemoveInfantry(infantry.GetID());
        }
    }
    
    public void RemoveStoredCargoTruck(StoredCargoTruck truck)
    {
        LaunchEntity hostEntity = truck.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();

        CargoTrucks.remove(truck.GetID());
        EntityRemoved(truck, false);
        
        if(system != null)
        {
            system.RemoveCargoTruck(truck.GetID());
            EntityUpdated(hostEntity, false);
        }            
    }
    
    public void RemoveStoredTank(StoredTank tank)
    {
        LaunchEntity hostEntity = tank.GetHost().GetEntity(this);
        CargoSystem system = null;
        
        if(hostEntity != null)
            system = ((HaulerInterface)hostEntity).GetCargoSystem();

        Tanks.remove(tank.GetID());
        EntityRemoved(tank, false);
        
        if(system != null)
        {
            system.RemoveTank(tank.GetID());
        }
    }
    
    public void AddSentryGun(SentryGun sentryGun)
    {
        sentryGun.SetListener(this);
        SentryGuns.put(sentryGun.GetID(), sentryGun);
        
        EntityUpdated(sentryGun, false);
        
        Player owner = GetOwner(sentryGun);
        
        if(owner != null)
            owner.AddOwnedEntity(sentryGun);
        
        EntityAdded(sentryGun);
    }
    
    public void AddArtilleryGun(ArtilleryGun artillery)
    {
        artillery.SetListener(this);
        ArtilleryGuns.put(artillery.GetID(), artillery);
        
        EntityUpdated(artillery, false);
        
        Player owner = GetOwner(artillery);
        
        if(owner != null)
            owner.AddOwnedEntity(artillery);
        
        EntityAdded(artillery);
    }
    
    public void AddScrapYard(ScrapYard yard)
    {
        yard.SetListener(this);
        ScrapYards.put(yard.GetID(), yard);
        
        EntityUpdated(yard, false);
        
        Player owner = GetOwner(yard);
        
        if(owner != null)
            owner.AddOwnedEntity(yard);
        
        EntityAdded(yard);
    }
    
    public void AddLoot(Loot loot)
    {
        loot.SetListener(this);
        Loots.put(loot.GetID(), loot);
        
        EntityUpdated(loot, false);
        
        EntityAdded(loot);
    }
    
    public void AddRubble(Rubble rubble)
    {
        rubble.SetListener(this);
        Rubbles.put(rubble.GetID(), rubble);
        
        EntityUpdated(rubble, false);
        
        EntityAdded(rubble);
    }
    
    public void AddResourceDeposit(ResourceDeposit deposit)
    {
        deposit.SetListener(this);
        ResourceDeposits.put(deposit.GetID(), deposit);
        
        EntityUpdated(deposit, false);
        
        EntityAdded(deposit);
    }
    
    public void AddRadiation(Radiation radiation)
    {
        radiation.SetListener(this);
        Radiations.put(radiation.GetID(), radiation);
        
        EntityUpdated(radiation, false);
        
        EntityAdded(radiation);
    }
    
    public int GetGameTickStarts() { return lGameTickStarts; }
    public int GetGameTickEnds() { return lGameTickEnds; }
    public int GetCommTickStarts() { return lCommTickStarts; }
    public int GetCommTickEnds() { return lCommTickEnds; }
    
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Config accessor.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public Config GetConfig() { return config; }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Public entity accessors.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public Collection<Alliance> GetAlliances() { return Alliances.values(); }
    public Collection<Treaty> GetTreaties() { return Treaties.values(); }
    public Collection<Player> GetPlayers() { return Players.values(); }
    public Collection<Missile> GetMissiles() { return Missiles.values(); }
    public Collection<Interceptor> GetInterceptors() { return Interceptors.values(); }
    public Collection<Torpedo> GetTorpedoes() { return Torpedoes.values(); }
    public Collection<MissileSite> GetMissileSites() { return MissileSites.values(); }
    public Collection<SAMSite> GetSAMSites() { return SAMSites.values(); }
    public Collection<SentryGun> GetSentryGuns() { return SentryGuns.values(); }
    public Collection<ArtilleryGun> GetArtilleryGuns() { return ArtilleryGuns.values(); }
    public Collection<OreMine> GetOreMines() { return OreMines.values(); }
    public Collection<RadarStation> GetRadarStations() { return RadarStations.values(); }
    public Collection<CommandPost> GetCommandPosts() { return CommandPosts.values(); }
    public Collection<Airbase> GetAirbases() { return Airbases.values(); }
    public Collection<Armory> GetArmories() { return Armories.values(); }
    public Collection<Loot> GetLoots() { return Loots.values(); }
    public Collection<Blueprint> GetBlueprints() { return Blueprints.values(); }
    public Collection<Airdrop> GetAirdrops() { return Airdrops.values(); }
    public Collection<ResourceDeposit> GetResourceDeposits() { return ResourceDeposits.values(); }
    public Collection<Radiation> GetRadiations() { return Radiations.values(); }
    public Collection<Bank> GetBanks() { return Banks.values(); }
    public Collection<Warehouse> GetWarehouses() { return Warehouses.values(); }
    public Collection<MissileFactory> GetMissileFactorys() { return MissileFactorys.values(); }
    public Collection<Shipyard> GetShipyards() { return Shipyards.values(); }
    public Collection<Processor> GetProcessors() { return Processors.values(); }
    public Collection<Distributor> GetDistributors() { return Distributors.values(); }
    public Collection<ScrapYard> GetScrapYards() { return ScrapYards.values(); }
    public Collection<Ship> GetShips() { return Ships.values(); }
    public Collection<Submarine> GetSubmarines() { return Submarines.values(); }
    public Collection<Rubble> GetRubbles() { return Rubbles.values(); }
    
    
    public Collection<Player> GetAllianceMembers(int lAllianceID)
    {
        if(AllianceMemberRosters.containsKey(lAllianceID))
        {
            return AllianceMemberRosters.get(lAllianceID).values();
        }
        
        return null;
    }
    
    public Collection<SentryGun> GetNormalSentryGuns() 
    { 
        List<SentryGun> Result = new ArrayList<>();
        
        for(SentryGun sentryGun : GetSentryGuns())
        {
            Result.add(sentryGun);            
        }
        
        return Result;
    }
    
    public Collection<NavalVessel> GetNavalVessels()
    {
        List<NavalVessel> Result = new ArrayList<>();
        
        Result.addAll(GetShips());
        Result.addAll(GetSubmarines());
        
        return Result;
    }
    
    public Collection<HaulerInterface> GetAllCargoInterfaces()
    {
        List<HaulerInterface> Result = new ArrayList<>();
        
        for(CargoTruck truck : GetCargoTrucks())
            Result.add(truck);
        
        for(StoredCargoTruck truck : GetStoredCargoTrucks())
            Result.add(truck);
        
        for(Airplane aircraft : GetAirplanes())
        {
            if(aircraft.HasCargo())
                Result.add(aircraft);
        }
        
        for(StoredAirplane aircraft : GetStoredAirplanes())
        {
            if(aircraft.HasCargo())
                Result.add(aircraft);
        }
        
        for(Ship ship : GetShips())
        {
            if(ship.HasCargo())
                Result.add(ship);
        }
        
        for(Warehouse warehouse : GetWarehouses())
            Result.add(warehouse);
        
        //TODO: Trains
        //TODO: Rail yards
        
        return Result;
    }
    
    public Collection<AirplaneInterface> GetAllAirplanes() { return Airplanes.values(); }
    
    public Collection<Airplane> GetAirplanes() 
    { 
        List<Airplane> Result = new ArrayList<>();
        
        for(AirplaneInterface aircraft : Airplanes.values())
        {
            if(aircraft instanceof Airplane)
            {
                Result.add((Airplane)aircraft);
            }
        }
        
        return Result;
    }
    
    public Collection<StoredAirplane> GetStoredAirplanes() 
    { 
        List<StoredAirplane> Result = new ArrayList<>();
        
        for(AirplaneInterface aircraft : Airplanes.values())
        {
            if(!aircraft.Flying())
            {
                Result.add((StoredAirplane)aircraft);
            }
        }
        
        return Result;
    }
    
    public Collection<InfantryInterface> GetAllInfantries() { return Infantries.values(); }
            
    public Collection<Infantry> GetInfantries() 
    { 
        List<Infantry> Result = new ArrayList<>();
        
        for(InfantryInterface infantry : Infantries.values())
        {
            if(infantry instanceof Infantry)
                Result.add((Infantry)infantry);
        }
        
        return Result;
    }
    
    public Collection<StoredInfantry> GetStoredInfantries() 
    { 
        List<StoredInfantry> Result = new ArrayList<>();
        
        for(InfantryInterface infantry : Infantries.values())
        {
            if(infantry instanceof StoredInfantry)
            {
                Result.add((StoredInfantry)infantry);
            }
        }
        
        return Result;
    }
    
    public Collection<TankInterface> GetAllTanks() { return Tanks.values(); }
    
    public Collection<Tank> GetTanks()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank)
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<StoredTank> GetStoredTanks() 
    { 
        List<StoredTank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof StoredTank)
            {
                Result.add((StoredTank)tank);
            }
        }
        
        return Result;
    }
    
    public Collection<CargoTruckInterface> GetAllCargoTrucks() { return CargoTrucks.values(); }
    
    public Collection<CargoTruck> GetCargoTrucks()
    {
        List<CargoTruck> Result = new ArrayList<>();
        
        for(CargoTruckInterface truck : CargoTrucks.values())
        {
            if(truck instanceof CargoTruck)
                Result.add((CargoTruck)truck);
        }
        
        return Result;
    }
    
    public Collection<Tank> GetMainBattleTanks()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank && tank.IsAnMBT())
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<Tank> GetSPAAGs()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank && tank.IsASPAAG())
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<Tank> GetMissileTanks()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank && tank.IsMissiles())
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<Tank> GetInterceptorTanks()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank && tank.IsInterceptors())
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<Tank> GetHowitzers()
    {
        List<Tank> Result = new ArrayList<>();
        
        for(TankInterface tank : Tanks.values())
        {
            if(tank instanceof Tank && tank.IsArtillery())
                Result.add((Tank)tank);
        }
        
        return Result;
    }
    
    public Collection<StoredCargoTruck> GetStoredCargoTrucks() 
    { 
        List<StoredCargoTruck> Result = new ArrayList<>();
        
        for(CargoTruckInterface truck : CargoTrucks.values())
        {
            if(truck instanceof StoredCargoTruck)
            {
                Result.add((StoredCargoTruck)truck);
            }
        }
        
        return Result;
    }
    public Collection<MissileType> GetMissileTypes() { return MissileTypes.values(); }
    public Collection<InterceptorType> GetInterceptorTypes() { return InterceptorTypes.values(); }
    public Collection<TorpedoType> GetTorpedoTypes() { return TorpedoTypes.values(); }
    
    public Collection<Movable> GetAllMovables()
    {
        List<Movable> Result = new ArrayList<>();
        
        for(Ship ship : GetShips())
            Result.add(ship);

        for(Submarine submarine : GetSubmarines())
            Result.add(submarine);

        for(Tank tank : GetTanks())
            Result.add(tank);

        for(CargoTruck truck : GetCargoTrucks())
            Result.add(truck);

        for(Infantry infantry : GetInfantries())
            Result.add(infantry);

        for(Airplane aircraft : GetAirplanes())
            Result.add(aircraft);
        
        return Result;
    }
    
    public Collection<Structure> GetAllStructures()
    {
        List<Structure> Result = new ArrayList();
        
        for(MissileSite missileSite : MissileSites.values())
            Result.add(missileSite);
        
        for(SAMSite samSite : SAMSites.values())
            Result.add(samSite);
        
        for(SentryGun sentryGun : SentryGuns.values())
            Result.add(sentryGun);
        
        for(ArtilleryGun artillery : ArtilleryGuns.values())
            Result.add(artillery);
        
        for(OreMine oreMine : OreMines.values())
            Result.add(oreMine);
        
        for(RadarStation radarStation : RadarStations.values())
            Result.add(radarStation);
        
        for(CommandPost commandPost : CommandPosts.values())
            Result.add(commandPost);
        
        for(Airbase airbase : Airbases.values())
            Result.add(airbase);
        
        for(Armory armory : Armories.values())
            Result.add(armory);
        
        for(Bank bank : Banks.values())
            Result.add(bank);
        
        for(Warehouse warehouse : Warehouses.values())
            Result.add(warehouse);
        
        for(MissileFactory factory : MissileFactorys.values())
            Result.add(factory);
        
        for(Processor processor : Processors.values())
            Result.add(processor);
        
        for(Distributor distributor : Distributors.values())
            Result.add(distributor);
        
        for(ScrapYard yard : ScrapYards.values())
            Result.add(yard);
                
        return Result;
    }
    
    public Collection<Capturable> GetAllCapturables()
    {
        List<Capturable> Result = new ArrayList();
        
        for(Shipyard shipyard : GetShipyards())
            Result.add(shipyard);
        
        return Result;
    }
    
    public Collection<LandUnit> GetAllLandUnits()
    {
        List<LandUnit> Result = new ArrayList<>();
        
        for(Tank tank : GetTanks())
            Result.add(tank);
        
        for(CargoTruck truck : GetCargoTrucks())
            Result.add(truck);
        
        for(Infantry infantry : GetInfantries())
            Result.add(infantry);
        
        return Result;
    }
    
    public Collection<MapEntity> GetAllMapEntities()
    {
        List<MapEntity> Result = new ArrayList();
        
        Result.addAll(GetAllStructures());
        Result.addAll(GetAirplanes());
        Result.addAll(Missiles.values());
        Result.addAll(Loots.values());
        Result.addAll(Players.values());
        Result.addAll(GetTanks());
        Result.addAll(GetInfantries());
        Result.addAll(Radiations.values());
        Result.addAll(Shipyards.values());
        Result.addAll(Ships.values());
        Result.addAll(Submarines.values());
        
        return Result;
    }
    
    public Collection<SAMSite> GetABMSites()
    {
        List<SAMSite> Result = new ArrayList();
        
        for(SAMSite samSite : SAMSites.values())
        {
            if(samSite.GetIsABMSilo())
            {
                Result.add(samSite);
            }
        }
        
        return Result;
    }
    
    public Collection<SAMSite> GetNormalSAMSites()
    {
        List<SAMSite> Result = new ArrayList();
        
        for(SAMSite samSite : SAMSites.values())
        {
            if(!samSite.GetIsABMSilo())
            {
                Result.add(samSite);
            }
        }
        
        return Result;
    }
    
    public Collection<MissileSite> GetNormalMissileSites()
    {
        List<MissileSite> Result = new ArrayList();
        
        for(MissileSite missileSite : MissileSites.values())
        {
            if(!missileSite.CanTakeICBM())
            {
                Result.add(missileSite);
            }
        }
        
        return Result;
    }
    
    public Collection<MissileSite> GetICBMSilos()
    {
        List<MissileSite> Result = new ArrayList();
        
        for(MissileSite missileSite : MissileSites.values())
        {
            if(missileSite.CanTakeICBM())
            {
                Result.add(missileSite);
            }
        }
        
        return Result;
    }
            
    
    public Alliance GetAlliance(int lID) { return Alliances.get(lID); }
    public Treaty GetTreaty(int lID) { return Treaties.get(lID); }
    public Player GetPlayer(int lID) { return Players.get(lID); }
    public Missile GetMissile(int lID) { return Missiles.get(lID); }
    public Interceptor GetInterceptor(int lID) { return Interceptors.get(lID); }
    public Torpedo GetTorpedo(int lID) { return Torpedoes.get(lID); }
    public MissileSite GetMissileSite(int lID) { return MissileSites.get(lID); }
    public SAMSite GetSAMSite(int lID) { return SAMSites.get(lID); }
    public SentryGun GetSentryGun(int lID) { return SentryGuns.get(lID); }
    public ArtilleryGun GetArtilleryGun(int lID) { return ArtilleryGuns.get(lID); }
    public ScrapYard GetScrapYard(int lID) { return ScrapYards.get(lID); } 
    public OreMine GetOreMine(int lID) { return OreMines.get(lID); }
    public RadarStation GetRadarStation(int lID) { return RadarStations.get(lID); }
    public CommandPost GetCommandPost(int lID) { return CommandPosts.get(lID); }
    public Airbase GetAirbase(int lID) { return Airbases.get(lID); }
    public Armory GetArmory(int lID) { return Armories.get(lID); }
    public MissileFactory GetMissileFactory(int lID) { return MissileFactorys.get(lID); }
    public Bank GetBank(int lID) { return Banks.get(lID); }
    public Warehouse GetWarehouse(int lID) { return Warehouses.get(lID); }
    public Loot GetLoot(int lID) { return Loots.get(lID); }
    public ResourceDeposit GetResourceDeposit(int lID) { return ResourceDeposits.get(lID); }
    public Blueprint GetBlueprint(int lID) { return Blueprints.get(lID); }
    public Airdrop GetAirdrop(int lID) { return Airdrops.get(lID); }
    public MissileType GetMissileType(int lID) { return MissileTypes.get(lID); }
    public InterceptorType GetInterceptorType(int lID) { return InterceptorTypes.get(lID); }
    public TorpedoType GetTorpedoType(int lID) { return TorpedoTypes.get(lID); }
    public Shipyard GetShipyard(int lID) { return Shipyards.get(lID); }
    public Processor GetProcessor(int lID) { return Processors.get(lID); } 
    public Distributor GetDistributor(int lID) { return Distributors.get(lID); }
    public Ship GetShip(int lID) { return Ships.get(lID); }
    public Submarine GetSubmarine(int lID) { return Submarines.get(lID); }
    public Rubble GetRubble(int lID) { return Rubbles.get(lID); }
    
    public CargoTruck GetCargoTruck(int lID) 
    { 
        CargoTruckInterface truck = CargoTrucks.get(lID);
        
        if(truck != null && truck instanceof CargoTruck)
            return (CargoTruck)CargoTrucks.get(lID);
        
        return null;
    }
    
    public StoredCargoTruck GetStoredCargoTruck(int lID)
    {
        CargoTruckInterface truck = CargoTrucks.get(lID);
        
        if(truck != null && truck instanceof StoredCargoTruck)
            return (StoredCargoTruck)truck;
        
        return null;
    }
    
    public Tank GetTank(int lID) 
    { 
        TankInterface tank = Tanks.get(lID);
        
        if(tank != null && tank instanceof Tank)
            return (Tank)tank;
        
        return null;
    }
    
    public StoredTank GetStoredTank(int lID)
    {
        TankInterface tank = Tanks.get(lID);
        
        if(tank != null && tank instanceof StoredTank)
            return (StoredTank)tank;
        
        return null;
    }
    
    public Infantry GetInfantry(int lID) 
    { 
        InfantryInterface infantry = Infantries.get(lID);
        
        if(infantry != null && infantry instanceof Infantry)
            return (Infantry)infantry;
        
        return null;
    }
    
    public StoredInfantry GetStoredInfantry(int lID) 
    { 
        InfantryInterface infantry = Infantries.get(lID);
        
        if(infantry != null && !infantry.Deployed())
            return (StoredInfantry)infantry;
        
        return null;
    }
    
    public TankInterface GetTankInterface(int lID) { return Tanks.get(lID); }
    
    public InfantryInterface GetInfantryInterface(int lID) { return Infantries.get(lID); }
    
    public CargoTruckInterface GetCargoTruckInterface(int lID) { return CargoTrucks.get(lID); }
    
    public AirplaneInterface GetAirplaneInterface(int lID) { return Airplanes.get(lID); }
    
    public Airplane GetAirplane(int lID) 
    { 
        AirplaneInterface aircraftInterface = Airplanes.get(lID);
        
        if(aircraftInterface != null && aircraftInterface instanceof Airplane)
        {
            return ((Airplane)aircraftInterface);
        }
        else
        {
            return null;
        }
    }
    
    public StoredAirplane GetStoredAirplane(int lID) 
    { 
        AirplaneInterface aircraft = Airplanes.get(lID); 
        
        if(aircraft instanceof StoredAirplane)
            return (StoredAirplane)aircraft;
        else
            return null;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Abstract methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    protected abstract void MissileExploded(Missile missile);
    protected abstract void TorpedoExploded(Torpedo torpedo);
    protected abstract void InterceptorLostTarget(Interceptor interceptor);
    protected abstract void InterceptorReachedTarget(Interceptor interceptor);
    protected abstract void InfantryReachedTarget(Infantry infantry);
    protected abstract void AirbaseDestroyed(Airbase airbase);
    protected abstract void ShipDestroyed(Ship ship);
    protected abstract void AirplaneDestroyed(AirplaneInterface aircraft);
    protected abstract void EntityMoved(MapEntity entity);
    
    /**
     * An entity was updated or created.
     * @param entity The entity that was updated or created.
     * @param bOwner Whether only the player that "owns" the entity should be notified of the update. False if everyone.
     */
    protected abstract void EntityUpdated(LaunchEntity entity, boolean bOwner);
    protected abstract void EntityAdded(LaunchEntity entity);
    protected abstract void EntityRemoved(LaunchEntity entity, boolean bDontCommunicate);   //Use don't communicate flag in instances where clients should be aware (e.g. due to time expiry, etc).
    protected abstract void AllianceUpdated(Alliance alliance, boolean bMajor);
    protected abstract void AllianceRemoved(Alliance alliance);
    protected abstract void TreatyUpdated(Treaty treaty);
    protected abstract void TreatyRemoved(Treaty treaty);
    
        
    //---------------------------------------------------------------------------------------------------------------------------------
    // LaunchEntityListener methods.
    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public final void EntityChanged(LaunchEntity entity, boolean bOwner)
    {
        EntityUpdated(entity, bOwner);
    }

    @Override
    public void EntityChanged(Alliance alliance)
    {
        AllianceUpdated(alliance, false);
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Player Rank Methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public int GetNextRankThreshold(int lRank)
    {
        double base = Defs.XP_PER_RANK;
        double multiplier = 1.1;
        return (int)(base * (Math.pow(multiplier, lRank) - 1) / (multiplier - 1));
    }

    public int GetLastXPThreshold(Player player)
    {
        double base = Defs.XP_PER_RANK;
        double multiplier = 1.1;
        return (int)(base * (Math.pow(multiplier, player.GetRank() - 1) - 1) / (multiplier - 1));
    }

    
    public float GetSentryHitChance(SentryGun sentryGun)
    {
        return config.GetSentryGunHitChance();        
    }
    
    /**
     * Interceptor accuracy is based on its speed compared to the speed of its target, which can be a missile or an aircraft.
     * @return The accuracy of the interceptor given its speed compared to the target's speed.
     */
    public float GetInterceptorHitChance(float fltInterceptorSpeedKPH, float fltTargetSpeedKPH, InterceptorType interceptorType, MissileType missileType)
    {
        if(interceptorType != null && missileType != null)
        {
            if(!interceptorType.GetABM() && missileType.GetICBM())
            {
                return Defs.NON_ABM_ACCURACY;
            }
            
            if(interceptorType.GetABM() && !interceptorType.GetNuclear())
            {
                return Defs.ABM_HIT_CHANCE;
            }
            
            if(interceptorType.GetNuclear())
            {
                return Defs.NUCLEAR_INTERCEPTOR_HIT_CHANCE;
            }
        }
        
        /*float fltHitChance = (100f - ((fltTargetSpeedKPH/fltInterceptorSpeedKPH) * 20f));
        
        if(fltHitChance > Defs.MAXIMUM_INTERCEPTOR_ACCURACY)
            return Defs.MAXIMUM_INTERCEPTOR_ACCURACY;
        else if(fltHitChance < Defs.MINIMUM_INTERCEPTOR_ACCURACY)
            return Defs.MINIMUM_INTERCEPTOR_ACCURACY;
        else
            return fltHitChance;*/
        
        return Defs.INTERCEPTOR_ACCURACY;
    }
    
    public float GetFuelableRange(float fltCurrentFuel, float fltMaxRange)
    {
        return fltCurrentFuel/1.0f * fltMaxRange;
    }
    
    public int GetTravelTime(float fltSpeed, GeoCoord geoStart, GeoCoord geoDestination)
    {
        return (int) (geoStart.DistanceTo(geoDestination)/(fltSpeed/Defs.SEC_PER_HOUR)) * lGameTickRate;
    }
    
    public int GetAircraftRefuelCost(AirplaneInterface aircraft)
    {
        return (int)aircraft.GetFuelDeficit()/10;
    }
    
    public GeoCoord GetAircraftPosition(AirplaneInterface aircraft)
    {
        if(aircraft != null)
        {
            if(aircraft.Flying())
            {
                return ((MapEntity)aircraft).GetPosition();
            }
            else
            {
                MapEntity hostEntity = aircraft.GetHomeBase().GetMapEntity(this);
                
                if(hostEntity != null)
                    return hostEntity.GetPosition();
            }
        }
        
        return null;
    }
    
    public GeoCoord GetPosition(LaunchEntity entity)
    {
        if(entity instanceof StoredAirplane)
        {
            return ((StoredAirplane)entity).GetHomeBase().GetMapEntity(this).GetPosition();
        }
        else if(entity instanceof MapEntity)
        {
            return ((MapEntity)entity).GetPosition();
        }
        
        //TODO: StoredTrains will need to be added.

        return null;
    }
    
    public boolean EntityIsFriendly(LaunchEntity entity, Player player)
    {
        if(entity instanceof Structure && ((Structure)entity).Unowned())
            return true;
        
        if(entity != null && player != null)
        {
            switch(GetAllegiance(entity, player))
            {
                case YOU:
                case ALLY:
                {
                    return true;
                }
            }
        }  
        
        return false;
    }
    
    public MapEntity GetInterceptorTarget(Interceptor interceptor)
    {
        switch(interceptor.GetTargetType())
        {
            case MISSILE:
            {
                if(GetMissile(interceptor.GetTargetID()) != null)
                    return GetMissile(interceptor.GetTargetID());
            }
            
            case AIRPLANE:
            {
                if(GetAirplane(interceptor.GetTargetID()) != null)
                    return GetAirplane(interceptor.GetTargetID());
            }
        }
        
        return null;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Optimization Methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public void AssignEntityRelations()
    {
        for(Structure structure : GetAllStructures())
        {
            Player owner = GetOwner(structure);
            
            if(owner != null)
            {
                owner.AddOwnedEntity(structure);
                
                if(structure instanceof Warehouse)
                {
                    ((Warehouse)structure).SetOwner(owner.GetID());
                }
            }
        } 
        
        for(Ship ship : GetShips())
        {
            Player owner = GetOwner(ship);
            
            if(owner != null)
            {
                owner.AddOwnedEntity(ship);
            }
        }
        
        for(AirplaneInterface aircraft : Airplanes.values())
        {
            Player owner = GetOwner(aircraft.GetAirplane());
            
            if(owner != null)
            {
                owner.AddOwnedEntity(aircraft.GetAirplane());
            }
        }
        
        for(Missile missile : Missiles.values())
        {
            Player owner = GetOwner(missile);
            
            if(owner != null)
            {
                owner.AddOwnedEntity(missile);
            }
            
            for(int lPlayerID : missile.GetStructureThreatenedPlayers())
            {
                Player target = Players.get(lPlayerID);
                
                if(target != null)
                    target.AddHostileMissile(missile.GetID());
            }
        }
        
        for(Blueprint blueprint : Blueprints.values())
        {
            Player owner = GetOwner(blueprint);
            
            if(owner != null)
                owner.AddOwnedEntity(blueprint);
        }
    }
    
    public List<Player> GetPlayerAlliesAndSelf(Player player)
    {
        List<Player> Result = new ArrayList<>();
        Result.add(player);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            for(Player otherPlayer : Players.values())
            {
                if(player.GetAllianceMemberID() == otherPlayer.GetAllianceMemberID())
                {
                    Result.add(otherPlayer);
                }
            }
        }
        
        return Result;
    }
    
    public boolean AircraftIsNuclearArmed(AirplaneInterface aircraft)
    {
        if(aircraft.HasMissiles() && aircraft.GetMissileSystem().GetOccupiedSlotCount() > 0)
        {
            for(byte cSlotNumber = 0; cSlotNumber < aircraft.GetMissileSystem().GetSlotCount(); cSlotNumber++)
            {
                if(aircraft.GetMissileSystem().GetSlotHasMissile(cSlotNumber) && config.GetMissileType(aircraft.GetMissileSystem().GetSlotMissileType(cSlotNumber)) != null && config.GetMissileType(aircraft.GetMissileSystem().GetSlotMissileType(cSlotNumber)).GetNuclear())
                    return true;
            }
        }
        
        return false;
    }
    
    public boolean PlayerHasMissileFactory(int lPlayerID)
    {
        /*for(MissileFactory factory : MissileFactorys.values())
        {
            if(factory.GetOnline() && factory.GetOwnedBy(lPlayerID))
                return true;
        }
        
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            for(Structure structure : player.GetStructures())
            {
                if(structure instanceof MissileFactory && structure.GetOnline())
                    return true;
            }
        }*/
        
        return true;
    }
    
    public boolean EntityIsCapturable(LaunchEntity entity)
    {
        Player player = GetOwner(entity);
        
        if(player != null && player.GetPlayerIsNoob())
        {
            return false;
        }
        
        if(entity instanceof Structure)
        {
            return true;
        }
        
        return false;
    }
    
    public int GetHourlyMissileMaintenance(int lPlayerID)
    {
        int lCost = 0;
        
        for(MissileSite site : GetMissileSites())
        {
            if(site.GetOwnedBy(lPlayerID))
            {
                for(int lType : site.GetMissileSystem().GetSlotTypes().values())
                {
                    MissileType type = config.GetMissileType(lType);
                    
                    if(type != null && !type.GetICBM())
                    {
                        lCost += type.GetHourlyMaintenance();
                    }
                } 
            }
        }
        
        for(AirplaneInterface aircraft : GetAllAirplanes())
        {
            if(aircraft.GetOwnerID() == lPlayerID)
            {
                if(aircraft.HasMissiles())
                {
                    for(int lType : aircraft.GetMissileSystem().GetSlotTypes().values())
                    {
                        MissileType type = config.GetMissileType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        /*for(TankInterface tank : GetAllTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                if(tank.HasMissiles())
                {
                    for(int lType : tank.GetMissileSystem().GetSlotTypes().values())
                    {
                        MissileType type = config.GetMissileType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }*/
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.HasMissiles())
                {
                    for(int lType : ship.GetMissileSystem().GetSlotTypes().values())
                    {
                        MissileType type = config.GetMissileType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                if(submarine.HasMissiles())
                {
                    for(int lType : submarine.GetMissileSystem().GetSlotTypes().values())
                    {
                        MissileType type = config.GetMissileType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        return lCost;
    }
    
    public int GetHourlyInterceptorMaintenance(int lPlayerID)
    {
        int lCost = 0;
        
        for(SAMSite site : GetSAMSites())
        {
            if(site.GetOwnedBy(lPlayerID))
            {
                for(int lType : site.GetInterceptorSystem().GetSlotTypes().values())
                {
                    InterceptorType type = config.GetInterceptorType(lType);
                    
                    if(type != null && !type.GetABM())
                    {
                        lCost += type.GetHourlyMaintenance();
                    }
                } 
            }
        }
        
        for(AirplaneInterface aircraft : GetAllAirplanes())
        {
            if(aircraft.GetOwnerID() == lPlayerID)
            {
                if(aircraft.HasInterceptors())
                {
                    for(int lType : aircraft.GetInterceptorSystem().GetSlotTypes().values())
                    {
                        InterceptorType type = config.GetInterceptorType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        /*for(TankInterface tank : GetAllTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                if(tank.HasInterceptors())
                {
                    for(int lType : tank.GetInterceptorSystem().GetSlotTypes().values())
                    {
                        InterceptorType type = config.GetInterceptorType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }*/
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.HasInterceptors())
                {
                    for(int lType : ship.GetInterceptorSystem().GetSlotTypes().values())
                    {
                        InterceptorType type = config.GetInterceptorType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        return lCost;
    }
    
    public int GetHourlyTorpedoMaintenance(int lPlayerID)
    {
        int lCost = 0;
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.HasTorpedoes())
                {
                    for(int lType : ship.GetTorpedoSystem().GetSlotTypes().values())
                    {
                        TorpedoType type = config.GetTorpedoType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                if(submarine.HasTorpedoes())
                {
                    for(int lType : submarine.GetTorpedoSystem().GetSlotTypes().values())
                    {
                        TorpedoType type = config.GetTorpedoType(lType);

                        if(type != null)
                        {
                            lCost += type.GetHourlyMaintenance();
                        }
                    }
                }
            }
        }
        
        return lCost;
    }
    
    public int GetHourlyInfantryMaintenance(int lPlayerID)
    {
        int lCost = 0;
        
        
        
        return lCost;
    }
    
    public boolean ElectronicWarfareTargetValid(MapEntity target)
    {
        if(target instanceof Structure || target instanceof Interceptor)
            return true;
        
        return false;
    }
    
    public ResourceDeposit GetNearbyDeposit(Player player)
    {
        if(player != null)
        {
            for(ResourceDeposit deposit : GetResourceDeposits())
            {
                if(deposit.GetPosition().DistanceTo(player.GetPosition()) <= Defs.DEPOSIT_RADIUS)
                {
                    return deposit;
                }
            }
        }
        
        return null;
    }
    
    public ResourceDeposit GetNearbyDeposit(GeoCoord geoPosition)
    {
        if(geoPosition != null)
        {
            for(ResourceDeposit deposit : GetResourceDeposits())
            {
                if(deposit.GetPosition().DistanceTo(geoPosition) <= Defs.DEPOSIT_RADIUS)
                {
                    return deposit;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks the terrain type to see what speed the unit should travel, starting with the most restrictive.
     * @param unit the unit whose position is being checked.
     * @return what speed the unit should travel.
     */
    public float GetLandUnitSpeed(LandUnit unit)
    {
        if(unit.GetOnWater())
            return Defs.LANDING_CRAFT_SPEED;
        
        if(unit.TakingFire())
            return Defs.LAND_UNIT_COMBAT_SPEED;
        
        return Defs.LAND_UNIT_SPEED;
    }
    
    /**
     * @return whether the command is legal and should be allowed.
     */
    public boolean UnitCommandIsLegal(Player player, MoveOrders command, EntityPointer commandable, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        LaunchEntity targetEntity = null;
        
        if(target != null)
             targetEntity = target.GetEntity(this);
        
        if(command == MoveOrders.MOVE || command == MoveOrders.TURN || command == MoveOrders.RETURN || command == MoveOrders.DEFEND || command == MoveOrders.WAIT)
            return true; //Movement commands are always legal. 
        else if(command == MoveOrders.LOAD && targetEntity instanceof Loot)
            return true; //Loading loot is always legal.
        else if(player != null)
        {
            boolean bIntentIsLegal = false;
            
            boolean bHostileIntent = command == MoveOrders.ATTACK || command == MoveOrders.CAPTURE || command == MoveOrders.LIBERATE;
            
            if(bHostileIntent)
            {
                if(geoTarget != null && targetEntity == null)
                {
                    bIntentIsLegal = true;
                }
                else if(targetEntity != null)
                {
                    Player targetPlayer = GetOwner(targetEntity);
                    
                    bIntentIsLegal = targetPlayer == null || (!WouldBeFriendlyFire(GetOwner(targetEntity), player) && !GetAttackIsBullying(player, targetPlayer));
                }
            }
            else
            {
                boolean bFriendlyIntent = command == MoveOrders.UNLOAD || command == MoveOrders.SEEK_FUEL || command == MoveOrders.PROVIDE_FUEL;
                
                if(bFriendlyIntent)
                {
                    if(targetEntity != null)
                    {
                        Player targetPlayer = GetOwner(targetEntity);

                        bIntentIsLegal = targetPlayer == null || EntityIsFriendly(GetOwner(targetEntity), player);
                    }
                }
            }
            
            if(bIntentIsLegal)
            {
                LaunchEntity entity = commandable.GetEntity(this);
                
                if(entity != null)
                {
                    //The general intention of the command is legal, so move on to entity-specific rules.
                    if(entity instanceof Airplane)
                    {
                        Airplane aircraft = (Airplane)commandable.GetEntity(this);

                        switch(command)
                        {
                            case ATTACK:
                            {
                                if(targetEntity != null)
                                {
                                    boolean bUseInterceptors = aircraft.HasInterceptors() && aircraft.GetInterceptorSystem().GetOccupiedSlotCount() > 0 && (targetEntity instanceof Missile || targetEntity instanceof Airplane);

                                    if(aircraft.HasMissiles() || aircraft.GroundAttack())
                                    {
                                        if(targetEntity instanceof Capturable || targetEntity instanceof ResourceDeposit || targetEntity instanceof Loot || targetEntity instanceof LandUnit || targetEntity instanceof Ship || (targetEntity instanceof Structure && !((Structure)targetEntity).GetRespawnProtected()))
                                            return true;
                                    }

                                    if((bUseInterceptors || aircraft.HasCannon()) && (targetEntity instanceof Airplane || targetEntity instanceof Missile))
                                    {
                                        return true;
                                    }
                                }   
                            }

                            case PROVIDE_FUEL:
                            {
                                return targetEntity instanceof Airplane;
                            }

                            case SEEK_FUEL:
                            {
                                return targetEntity instanceof Airplane && ((Airplane)targetEntity).CanTransferFuel();
                            }

                            case UNLOAD: 
                            {
                                //Unload at a coordinate.
                                CargoSystem cargo = ((HaulerInterface)commandable.GetEntity(this)).GetCargoSystem();
                                boolean bContainsType = cargo != null && cargo.ContainsHaulable(typeToDeliver, lDeliverID, lQuantityToDeliver);

                                return bContainsType;
                            }
                        }
                    }
                    else if(entity instanceof Infantry)
                    {
                        switch(command)
                        {
                            case ATTACK: return targetEntity instanceof Infantry;
                            case CAPTURE: return targetEntity instanceof CargoTruck || targetEntity instanceof Structure || targetEntity instanceof Shipyard;
                            case LIBERATE:
                            {
                                if(targetEntity instanceof MapEntity mapEntity)
                                    return GetCanBeLiberated(player, mapEntity);
                            }
                        }
                    }
                    else if(entity instanceof Tank)
                    {
                        switch(command)
                        {
                            case ATTACK:
                            {
                                Tank tank = (Tank)commandable.GetEntity(this);

                                if(tank.IsAnMBT())
                                {
                                    return targetEntity instanceof LandUnit || (targetEntity instanceof Structure && !((Structure)targetEntity).GetRespawnProtected()) || targetEntity instanceof Capturable || targetEntity instanceof Loot || targetEntity instanceof ResourceDeposit;
                                }
                            }
                            break;
                        }
                    }
                    else if(entity instanceof CargoTruck)
                    {
                        switch(command)
                        {
                            case UNLOAD: 
                            {
                                if(targetEntity != null)
                                {
                                    //Unload to an entity.
                                    if(targetEntity instanceof HaulerInterface)
                                    {
                                        CargoSystem cargo = ((HaulerInterface)commandable.GetEntity(this)).GetCargoSystem();
                                        boolean bContainsType = cargo != null && cargo.ContainsHaulable(typeToDeliver, lDeliverID, lQuantityToDeliver);

                                        return bContainsType;
                                    }
                                }
                                else
                                {
                                    //Unload at a coordinate.
                                    CargoSystem cargo = ((HaulerInterface)commandable.GetEntity(this)).GetCargoSystem();
                                    boolean bContainsType = cargo != null && cargo.ContainsHaulable(typeToDeliver, lDeliverID, lQuantityToDeliver);

                                    return bContainsType;
                                } 
                            }
                            break;
                        }
                    }
                    else if(entity instanceof NavalVessel)
                    {
                        switch(command)
                        {
                            case PROVIDE_FUEL:
                            {
                                NavalVessel refuelerVessel = (NavalVessel)commandable.GetMapEntity(this);

                                if(refuelerVessel != null && target != null)
                                {
                                    if(refuelerVessel instanceof Ship)
                                    {
                                        Ship refuelerShip = (Ship)refuelerVessel;

                                        if(!refuelerShip.GetNuclear() || (refuelerShip.HasCargo() && refuelerShip.GetCargoSystem().ContainsResourceType(ResourceType.FUEL)))
                                        {
                                            NavalVessel receiverVessel = (NavalVessel)target.GetMapEntity(this);

                                            if(receiverVessel != null && !receiverVessel.GetNuclear())
                                                return true;
                                        }
                                    }
                                    else
                                    {
                                        if(!refuelerVessel.GetNuclear())
                                        {
                                            NavalVessel receiverVessel = (NavalVessel)target.GetMapEntity(this);

                                            if(receiverVessel != null && !receiverVessel.GetNuclear())
                                                return true;
                                        }
                                    }  
                                }
                            }
                            break;

                            case SEEK_FUEL:
                            {
                                if(target != null)
                                {
                                    NavalVessel refuelerVessel = (NavalVessel)target.GetMapEntity(this);

                                    if(refuelerVessel != null)
                                    {
                                        if(refuelerVessel instanceof Ship)
                                        {
                                            Ship refuelerShip = (Ship)refuelerVessel;

                                            if(!refuelerShip.GetNuclear() || refuelerShip.HasCargo() && refuelerShip.GetCargoSystem().ContainsResourceType(ResourceType.FUEL))
                                            {
                                                NavalVessel receiverVessel = (NavalVessel)commandable.GetMapEntity(this);

                                                if(receiverVessel != null && !receiverVessel.GetNuclear())
                                                    return true;
                                            }
                                        }
                                        else
                                        {
                                            if(!refuelerVessel.GetNuclear())
                                            {
                                                NavalVessel receiverVessel = (NavalVessel)commandable.GetMapEntity(this);

                                                if(receiverVessel != null && !receiverVessel.GetNuclear())
                                                    return true;
                                            }
                                        }  
                                    }
                                }
                            }
                            break;

                            case ATTACK:
                            {
                                return geoTarget != null || targetEntity instanceof LandUnit || targetEntity instanceof Structure || targetEntity instanceof Capturable || targetEntity instanceof NavalVessel || targetEntity instanceof Loot || targetEntity instanceof ResourceDeposit;
                            }
                        }
                    }
                    else if(entity instanceof ArtilleryGun)
                    {
                        return geoTarget != null || targetEntity instanceof LandUnit || targetEntity instanceof Structure || targetEntity instanceof Capturable || targetEntity instanceof NavalVessel || targetEntity instanceof Loot || targetEntity instanceof ResourceDeposit;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean GetCanBeLiberated(Player player, MapEntity target)
    {
        if(target != null)
        {
            if(player != null)
            {
                int lBuilderID = LaunchEntity.ID_NONE;

                if(target instanceof Shipyard shipyard)
                {
                    return shipyard.IsCaptured() && !WouldBeFriendlyFire(player, GetOwner(shipyard));
                }
                
                if(target instanceof Structure structure)
                    lBuilderID = structure.GetBuiltByID();

                //If the target is not a player, the entity should be captured and the owner of the entity should not be a prisoner.
                Player builder = GetPlayer(lBuilderID);
                Player owner = GetOwner(target);

                if(builder != null && !builder.GetPrisoner() && !builder.GetAWOL() && !WouldBeFriendlyFire(player, owner))
                {
                    return (target instanceof Structure structure && structure.Captured());
                }
            }  
        }  
        
        return false;
    }
    
    public boolean TargetIsLegal(EntityPointer pointerTargeter, EntityPointer pointerTarget)
    {
        boolean bTargetIsValid = false;
        LaunchEntity targeter = pointerTargeter.GetEntity(this);
        LaunchEntity target = pointerTarget.GetEntity(this);
        
        if(target instanceof Player)
            return false;
        
        if(targeter != null && target != null)
        {
            if(targeter instanceof AirplaneInterface)
            {
                AirplaneInterface aircraft = (AirplaneInterface)targeter;
                
                if((aircraft.GroundAttack()/* || aircraft.HasMissiles()*/) && (target instanceof NavalVessel || target instanceof Structure || target instanceof LandUnit || target instanceof Capturable || target instanceof Loot))
                {
                    bTargetIsValid = true;
                }
                else if((aircraft.HasCannon() || aircraft.HasInterceptors()) && (target instanceof Airplane || target instanceof Missile))
                {
                    //Make sure the target is not an ICBM or a MIRV.
                    if(target instanceof Missile)
                    {
                        MissileType type = config.GetMissileType(((Missile)target).GetType());

                        if(!type.GetICBM())
                            bTargetIsValid = true;
                    }
                    else
                    {
                        bTargetIsValid = true;
                    }
                }
            }
            else if(targeter instanceof MissileSite || targeter instanceof NavalVessel || targeter instanceof ArtilleryGun)
            {
                if(target instanceof Structure || target instanceof LandUnit || target instanceof NavalVessel || target instanceof ResourceDeposit || target instanceof Loot || target instanceof Capturable)
                {
                    bTargetIsValid = true;
                }
            }
            else if(targeter instanceof TankInterface)
            {
                if(target instanceof Structure || target instanceof LandUnit || target instanceof ResourceDeposit || target instanceof Loot || target instanceof Capturable)
                {
                    bTargetIsValid = true;
                }
            }
        }
        
        return bTargetIsValid;
    }
    
    /**
     * 
     * @param receiver the entity receiving the launchables.
     * @param lTypeID the launchable type id.
     * @param bMissile whether this is a missile or an interceptor.
     * @return true if the launchable can be transferred into the system of the receiver.
     */
    public boolean LaunchableTransferLegal(LaunchEntity sender, LaunchEntity receiver, int lTypeID, boolean bMissile, boolean bLoadMissilesAsCargo)
    {
        if(sender instanceof Ship && !(receiver instanceof Shipyard || receiver instanceof Ship))
        {
            return false;
        }
        else if(receiver instanceof Ship && !(sender instanceof Shipyard || sender instanceof Ship))
        {
            return false;
        }
        else if(bLoadMissilesAsCargo && receiver instanceof HaulerInterface)
        {
            return true;
        }
        else if(bMissile)
        {
            MissileType type = config.GetMissileType(lTypeID);
            
            if(receiver instanceof MissileSite)
            {
                MissileSite site = ((MissileSite)receiver);
                
                if(site.CanTakeICBM())
                {
                    return type.GetICBM();
                }
                else
                {
                    return !type.GetICBM();
                }
            }
            else if(receiver instanceof StoredAirplane)
            {
                StoredAirplane aircraft = ((StoredAirplane)receiver);
                
                return aircraft.HasMissiles() && type.GetAirLaunched();
            }
            else if(receiver instanceof Tank)
            {
                return false;
                /*Tank tank = ((Tank)receiver);
                
                return tank.HasMissiles() && type.GetTankLaunched();*/
            }
            else if(receiver instanceof Ship)
            {
                Ship ship = ((Ship)receiver);
                
                return !type.GetICBM() && ship.HasMissiles() && type.GetShipLaunched();
            }
            else if(receiver instanceof Submarine)
            {
                Submarine submarine = ((Submarine)receiver);
                
                if(type.GetICBM())
                {
                    return type.GetSubmarineLaunched() && submarine.HasICBMs();
                }
                else
                {
                    return type.GetSubmarineLaunched() && submarine.HasMissiles();
                }
            }
            
        }
        else
        {
            InterceptorType type = config.GetInterceptorType(lTypeID);
            
            if(receiver instanceof SAMSite)
            {
                SAMSite site = ((SAMSite)receiver);
                
                if(site.GetIsABMSilo())
                {
                    return type.GetABM();
                }
                else
                {
                    return !type.GetABM();
                }
            }
            else if(receiver instanceof StoredAirplane)
            {
                StoredAirplane aircraft = ((StoredAirplane)receiver);
                
                return aircraft.HasInterceptors() && type.GetAirLaunched();
            }
            else if(receiver instanceof Tank)
            {
                return false;
                /*Tank tank = ((Tank)receiver);
                
                return tank.HasInterceptors() && type.GetTankLaunched();*/
            }
            else if(receiver instanceof Ship)
            {
                Ship ship = ((Ship)receiver);
                
                return !type.GetABM() && ship.HasInterceptors() && type.GetShipLaunched();
            }
        }
        
        return false;
    }
    
    /**
     * This checks to see if non-launchable cargo transfers are legal. If transferring launchables, use LaunchableTransferValid instead.
     */
    public boolean CargoTransferLegal(LaunchEntity sender, LaunchEntity receiver, LootType type)
    {
        if(sender.ApparentlyEquals(receiver))
        {
            return false;
        }
        
        if(receiver instanceof Airplane)
            return false;
        
        //TODO: This if block accounts for ships transferring cargo to and from things. When trains are added, we will need a similar block for rail yards and trains.
        if(sender instanceof Ship)
        {
            if(receiver instanceof StoredAirplane)
            {
                StoredAirplane aircraft = (StoredAirplane)receiver;
                
                MapEntity host = aircraft.GetHomeBase().GetMapEntity(this);
                
                if(host != null)
                {
                    if(host.ApparentlyEquals(sender))
                    {
                        return true;
                    }
                }
            }
            else if(!(receiver instanceof Shipyard || receiver instanceof Ship))
            {
                return false;
            }
        }
        else if(receiver instanceof Ship && !(sender instanceof Shipyard || sender instanceof Ship || sender instanceof Loot))
        {
            Ship receiverShip = ((Ship)receiver);
            
            if(receiverShip.HasAmphibious())
            {
                return true;
            }
            else if(sender instanceof StoredAirplane)
            {
                StoredAirplane aircraft = (StoredAirplane)sender;
                
                MapEntity host = aircraft.GetHomeBase().GetMapEntity(this);
                
                if(host != null)
                {
                    if(host.ApparentlyEquals(receiver))
                    {
                        return true;
                    }
                }
            }
            else if(!(sender instanceof Shipyard || sender instanceof Ship))
            {
                return false;
            }
        }
        else if(receiver instanceof Structure structure && !structure.GetOnline())
        {
            return false;
        }
        else if(sender instanceof Structure structure && !structure.GetOnline())
        {
            return false;
        }
        
        switch(type)
        {
            case RESOURCES:
            {
                return (receiver instanceof HaulerInterface && !(receiver instanceof Armory)) || (receiver instanceof ResourceInterface);
            }

            case STORED_INFANTRY:
            case STORED_TANK:
            {
                return receiver instanceof StoredAirplane || receiver instanceof Ship;
            }
        } 
        
        return false;
    }
    
    public boolean AircraftCanLandAtAirbase(AirplaneInterface aircraft, AirbaseInterface airbase)
    {
        if(airbase instanceof Ship)
        {
            Ship carrier = ((Ship)airbase);
            
            if(carrier.HasAircraft())
            {
                if(!carrier.GetAircraftSystem().Full())
                {
                    if(carrier.GetOwnerID() == ((LaunchEntity)aircraft).GetOwnerID() || carrier.GetAircraftSystem().GetOpen())
                    {
                        if(aircraft.GetCarrierCompliant())
                        {
                            return true;
                        }
                    }
                }
            }
        }
        else if(airbase instanceof Airbase)
        {
            Airbase structure = (Airbase)airbase;
            
            if(!structure.GetAircraftSystem().Full())
            {
                if(structure.GetOwnerID() == ((LaunchEntity)aircraft).GetOwnerID() || structure.GetAircraftSystem().GetOpen())
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean LoadUnitBoardLegal(EntityPointer receiver, EntityPointer sender, EntityPointer unit)
    {
        if(receiver != null && (receiver.GetEntity(this) instanceof Warehouse || receiver.GetEntity(this) instanceof Airplane))
        {
            return false;
        }
        
        if(sender != null && sender.GetEntity(this) instanceof Armory && !((Armory)sender.GetEntity(this)).GetOnline())
        {
            return false;
        }
        
        if(receiver != null && receiver.GetEntity(this) instanceof Armory && !((Armory)receiver.GetEntity(this)).GetOnline())
        {
            return false;
        }
        
        if(sender == null && receiver != null)
        {
            if(receiver.GetEntity(this) instanceof HaulerInterface)
            {
                //Sender is null, so the land unit is loading into a shipyard or storedaircraft.
                HaulerInterface entityReceiver = (HaulerInterface)receiver.GetEntity(this);
                Haulable haulable = (Haulable)unit.GetEntity(this);
                CargoSystem system = entityReceiver.GetCargoSystem();
                
                float fltLoadDistance = Defs.LOAD_DISTANCE;
                MapEntity mapSender = null;
                MapEntity mapReceiver = null;
                
                if(haulable instanceof MapEntity)
                {
                    mapSender = ((MapEntity)haulable);
                }
                
                if(entityReceiver instanceof StoredAirplane)
                {
                    mapReceiver = ((StoredAirplane)entityReceiver).GetHomeBase().GetMapEntity(this);
                }
                else if(entityReceiver instanceof MapEntity)
                {
                    mapReceiver = ((MapEntity)entityReceiver);
                    
                    if(entityReceiver instanceof Shipyard)
                    {
                        fltLoadDistance = Defs.SHIPYARD_REPAIR_DISTANCE;
                    }
                }

                if(mapSender != null && mapReceiver != null && haulable != null)
                {
                    if(mapSender.GetPosition().DistanceTo(mapReceiver.GetPosition()) <= fltLoadDistance)
                    {
                        return system != null && system.WeightCanFit(haulable.GetWeight()) && haulable instanceof LandUnit && (entityReceiver instanceof Shipyard || entityReceiver instanceof StoredAirplane || entityReceiver instanceof Armory || (entityReceiver instanceof Ship && ((Ship)entityReceiver).HasAmphibious()));
                    }
                }
            }
        }
        else if(receiver == null)
        {
            //Receiver null, so this should be a storedinfantry attempting an airdrop from an Aircraft or a stored land unit of any type disembarking from a shipyard or stored aircraft or warehouse.
            LaunchEntity entitySender = sender.GetEntity(this);

            if(entitySender instanceof Airplane)
            {
                return true;
            }
            else if(entitySender instanceof Shipyard)
            {
                return true;
            }
            else if(entitySender instanceof Warehouse)
            {
                return true;
            }
            else if(entitySender instanceof Armory)
            {
                return true;
            }
            else if(entitySender instanceof Ship && ((Ship)entitySender).HasAmphibious())
            {
                return true;
            }
            else if(entitySender instanceof StoredAirplane || entitySender instanceof Airbase)
            {
                return true;
            }
        }
        else if(unit != null)
        {
            if(receiver.GetEntity(this) instanceof HaulerInterface)
            {
                //None are null, so a ship is transferring directly to a shipyard or vice-versa.
                LaunchEntity entitySender = sender.GetEntity(this);
                HaulerInterface entityReceiver = (HaulerInterface)receiver.GetEntity(this);
                Haulable haulable = (Haulable)unit.GetEntity(this);
                CargoSystem system = entityReceiver.GetCargoSystem();
                
                float fltLoadDistance = Defs.LOAD_DISTANCE;
                MapEntity mapSender = null;
                MapEntity mapReceiver = null;
                
                if(entitySender instanceof Shipyard || entityReceiver instanceof Shipyard)
                {
                    fltLoadDistance = Defs.SHIPYARD_REPAIR_DISTANCE;
                }
                
                if(entitySender instanceof StoredAirplane)
                {
                    mapSender = ((StoredAirplane)entitySender).GetHomeBase().GetMapEntity(this);
                }
                /*else if(sender instanceof StoredTrain)
                {
                    TODO
                }*/
                else if(entitySender instanceof MapEntity)
                {
                    mapSender = ((MapEntity)entitySender);
                }
                
                if(entityReceiver instanceof StoredAirplane)
                {
                    mapReceiver = ((StoredAirplane)entityReceiver).GetHomeBase().GetMapEntity(this);
                }
                /*else if(sender instanceof StoredTrain)
                {
                    TODO
                }*/
                else if(entityReceiver instanceof MapEntity)
                {
                    mapReceiver = ((MapEntity)entityReceiver);
                }
                
                if(mapSender != null && mapReceiver != null)
                {
                    if(mapSender.GetPosition().DistanceTo(mapReceiver.GetPosition()) <= fltLoadDistance)
                    {
                        return system != null && system.WeightCanFit(haulable.GetWeight()) && (haulable instanceof StoredInfantry || haulable instanceof StoredTank || haulable instanceof StoredCargoTruck) && (entitySender instanceof Ship || entitySender instanceof Shipyard) && (entityReceiver instanceof Ship || entityReceiver instanceof Shipyard);
                    }
                }
            }  
        }
            
        return false;
    }
    
    public boolean LoadHaulableValid(Haulable haulable, LaunchEntity receiver, LaunchEntity hauler)
    {
        if((haulable instanceof Loot || haulable instanceof Resource) && receiver != null)
        {            
            switch(haulable.GetLootType())
            {
                case MISSILES:
                {
                    if(receiver instanceof HaulerInterface)
                    {
                        return true;
                    }

                    if(receiver instanceof LauncherInterface)
                    {
                        return ((LauncherInterface)receiver).HoldsMissiles();
                    }
                }
                break;

                case INTERCEPTORS:
                {
                    if(receiver instanceof HaulerInterface)
                    {
                        return true;
                    }

                    if(receiver instanceof LauncherInterface)
                    {
                        return ((LauncherInterface)receiver).HoldsInterceptors();
                    }
                }
                break;

                case RESOURCES:
                {
                    return receiver instanceof HaulerInterface || receiver instanceof ResourceInterface;
                }
            }
        }
        else
        {
            if(haulable instanceof LaunchEntity)
            {
                EntityPointer pointerHauler = null;
                
                if(hauler != null)
                {
                    pointerHauler = hauler.GetPointer();
                }
                
                if(LoadUnitBoardLegal(receiver.GetPointer(), pointerHauler, ((LaunchEntity)haulable).GetPointer()))
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Determines whether an entity is a valid target for a tracking missile. If the entity is valid, the missile will track the entity. If not, the missile will go to the entity's current location.
     * @param entity the entity to be potentially tracked.
     * @param type The type of missile doing the tracking.
     * @return True if the missile should track the entity, false if it should go to the entity's current location. THE MISSILE WILL LAUNCH EITHER WAY.
     */
    public boolean TrackingTargetValid(MapEntity entity, MissileType type)
    {
        //Tracking missiles can track land targets, but not ships.
        if(entity instanceof LandUnit)
            return type.GetTracking();   
        else if(entity instanceof Ship)
            return type.GetAntiShip();
        else if(entity instanceof Submarine)
            return type.GetAntiShip() || type.GetAntiSubmarine();
        
        return false;
    }
    
    public boolean ShipInPort(NavalVessel vessel)
    {
        if(vessel instanceof Submarine && ((Submarine)vessel).Submerged())
        {
            return false;
        }
        else
        {
            Player owner = GetPlayer(vessel.GetOwnerID());
                
            if(owner != null)
            {
                if(owner.GetBoss())
                    return true;
                
                for(Shipyard shipyard : GetShipyards())
                {
                    if(EntityIsFriendly(shipyard, owner))
                    {
                        if(vessel.GetPosition().DistanceTo(shipyard.GetPosition()) <= Defs.SHIPYARD_REPAIR_DISTANCE)
                        {
                            return true;
                        }
                    }   
                }
                
                if(!(vessel instanceof Ship) || !((Ship)vessel).HasSupport())
                {
                    for(Ship ship : GetShips())
                    {
                        if(EntityIsFriendly(owner, GetOwner(ship)))
                        {
                            if(ship.HasSupport())
                            {
                                if(vessel.GetPosition().DistanceTo(ship.GetPosition()) <= Defs.SHIPYARD_REPAIR_DISTANCE)
                                    return true;
                            }
                        }
                    }
                }  
            }
        }
        
        return false;
    }
    
    public boolean LandUnitNearScrapYard(LandUnit unit)
    {
        Player owner = GetOwner(unit);
        
        if(owner != null && owner.Functioning())
        {
            for(ScrapYard yard : GetScrapYards())
            {
                if(yard.GetOnline())
                {
                    if(EntityIsFriendly(yard, owner))
                    {
                        if(unit.GetPosition().DistanceTo(yard.GetPosition()) <= Defs.LOAD_DISTANCE)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public int GetTankMaintenanceCost(int lPlayerID)
    {
        return GetTankCount(lPlayerID) * Defs.TANK_MAINTENANCE_COST;
    }
    
    public int GetTankCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetCargoTruckMaintenanceCost(int lPlayerID)
    {        
        return GetCargoTruckCount(lPlayerID) * Defs.CARGO_TRUCK_MAINTENANCE_COST;
    }
    
    public int GetCargoTruckCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetInfantryMaintenanceCost(int lPlayerID)
    {
        return GetInfantryCount(lPlayerID) * Defs.INFANTRY_MAINTENANCE_COST;
    }
    
    public int GetOnlineProcessorCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Processor processor : GetProcessors())
        {
            if(!processor.GetOffline() && processor.GetOwnedBy(lPlayerID))
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetOnlineExtractorCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(OreMine mine : GetOreMines())
        {
            if(!mine.GetOffline() && mine.GetOwnedBy(lPlayerID))
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetStructureMaintenanceCost(int lPlayerID)
    {
        int lCost = 0;
        
        for(Structure structure : GetAllStructures())
        {
            if(structure != null && structure.GetOwnerID() == lPlayerID && !structure.GetOffline())
            {
                lCost += Defs.ONLINE_MAINTENANCE_COST;
            }
        }
        
        return lCost;
    }
    
    public int GetInfantryCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Infantry infantry : GetInfantries())
        {
            if(infantry.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetShipMaintenanceCost(int lPlayerID)
    {
        int lCost = 0;
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                int lShipCost = Defs.GetNavalMaintenanceCost(ship.GetEntityType());
                
                if(ShipInPort(ship))
                    lShipCost *= 0.1f;
                
                lCost += lShipCost;
            }
        }
        
        return lCost;
    }
    
    public int GetShipCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetSubmarineMaintenanceCost(int lPlayerID)
    {
        int lCost = 0;
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                int lSubmarineCost = Defs.GetNavalMaintenanceCost(submarine.GetEntityType());
            
                if(ShipInPort(submarine))
                    lSubmarineCost *= 0.1f;
                
                lCost += lSubmarineCost;
            }
        }
        
        return lCost;
    }
    
    public int GetSubmarineCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetAircraftMaintenanceCost(int lPlayerID)
    {
        int lCost = 0;
        
        for(AirplaneInterface aircraft : GetAllAirplanes())
        {
            if(aircraft.GetOwnerID() == lPlayerID)
            {
                if(aircraft instanceof StoredAirplane stored)
                    lCost += Defs.GetAircraftMaintenanceCost(stored.GetAircraftType());
                else
                    lCost += Defs.GetAircraftMaintenanceCost(aircraft.GetAirplane().GetEntityType());
            }
        }
        
        return lCost;
    }
    
    public int GetAircraftCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(AirplaneInterface aircraft : GetAllAirplanes())
        {
            if(aircraft.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public int GetMissileCount(int lPlayerID)
    {
        int lCount = 0;
        
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            for(MissileSite site : GetMissileSites())
            {
                if(site.GetOwnedBy(lPlayerID) && !site.CanTakeICBM())
                {
                    lCount += site.GetMissileSystem().GetOccupiedSlotCount();
                }
            }
            
            for(AirplaneInterface aircraft : GetAllAirplanes())
            {
                if(aircraft.GetOwnerID() == lPlayerID)
                {
                    if(aircraft.HasMissiles())
                        lCount += aircraft.GetMissileSystem().GetOccupiedSlotCount();
                }
            }

            /*for(TankInterface tank : GetAllTanks())
            {
                if(tank.GetOwnerID() == lPlayerID)
                {
                    if(tank.HasMissiles())
                        lCount += tank.GetMissileSystem().GetOccupiedSlotCount();
                }
            }*/

            for(Ship ship : GetShips())
            {
                if(ship.GetOwnerID() == lPlayerID)
                {
                    if(ship.HasMissiles())
                        lCount += ship.GetMissileSystem().GetOccupiedSlotCount();
                }
            }

            for(Submarine submarine : GetSubmarines())
            {
                if(submarine.GetOwnerID() == lPlayerID)
                {
                    if(submarine.HasMissiles())
                        lCount += submarine.GetMissileSystem().GetOccupiedSlotCount();
                    
                    if(submarine.HasICBMs())
                        lCount += submarine.GetICBMSystem().GetOccupiedSlotCount();
                }
            }
        }
        
        return lCount;
    }
    
    public int GetICBMMaintenanceCost(int lPlayerID)
    {
        return GetICBMCount(lPlayerID) * Defs.MISSILE_ICBM_MAINTENANCE;
    }
    
    public int GetICBMCount(int lPlayerID)
    {
        int lCount = 0;

        Player player = GetPlayer(lPlayerID);

        if(player != null)
        {
            for(MissileSite site : GetMissileSites())
            {
                if(site.CanTakeICBM() && site.GetOwnedBy(lPlayerID))
                {
                    MissileSystem system = site.GetMissileSystem();

                    if(system.GetOccupiedSlotCount() > 0)
                    {
                        for(Entry<Integer, Integer> slot : new ArrayList<>(system.GetSlotTypes().entrySet()))
                        {
                            MissileType type = config.GetMissileType(slot.getValue());

                            if(type.GetICBM())
                            {
                                lCount++;
                            } 
                        }
                    }
                }
            }
            
            for(Submarine submarine : GetSubmarines())
            {
                if(submarine.GetOwnerID() == lPlayerID)
                {
                    if(submarine.HasICBMs())
                    {
                        MissileSystem system = submarine.GetICBMSystem();

                        if(system.GetOccupiedSlotCount() > 0)
                        {
                            for(Entry<Integer, Integer> slot : new ArrayList<>(system.GetSlotTypes().entrySet()))
                            {
                                MissileType type = config.GetMissileType(slot.getValue());

                                if(type.GetICBM() && system.GetSlotPrepTimeRemaining(slot.getKey()) == 0)
                                {
                                    lCount++;
                                } 
                            }
                        }
                    }
                }
            }
        }

        return lCount;
    }

    public int GetABMMaintenanceCost(int lPlayerID)
    {
        return GetABMCount(lPlayerID) * Defs.INTERCEPTOR_ABM_MAINTENANCE;
    }

    public int GetABMCount(int lPlayerID)
    {
        int lCount = 0;
        Player player = GetPlayer(lPlayerID);

        if(player != null)
        {
            for(Structure structure : new ArrayList<>(player.GetStructures()))
            {
                if(structure instanceof SAMSite)
                {
                    SAMSite site = ((SAMSite)structure);

                    if(site.GetIsABMSilo())
                    {
                        lCount += site.GetInterceptorSystem().GetReadySlotCount();
                    }
                }
            }
        }

        return lCount;
    }
    
    public int GetInterceptorCount(int lPlayerID)
    {
        int lCount = 0;
        
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            for(SAMSite site : GetSAMSites())
            {
                if(site.GetOwnedBy(lPlayerID) && !site.GetIsABMSilo())
                {
                    lCount += site.GetInterceptorSystem().GetOccupiedSlotCount();
                }
            }
            
            for(AirplaneInterface aircraft : GetAllAirplanes())
            {
                if(aircraft.GetOwnerID() == lPlayerID)
                {
                    if(aircraft.HasInterceptors())
                        lCount += aircraft.GetInterceptorSystem().GetOccupiedSlotCount();
                }
            }

            /*for(TankInterface tank : GetAllTanks())
            {
                if(tank.GetOwnerID() == lPlayerID)
                {
                    if(tank.HasInterceptors())
                        lCount += tank.GetInterceptorSystem().GetOccupiedSlotCount();
                }
            }*/

            for(Ship ship : GetShips())
            {
                if(ship.GetOwnerID() == lPlayerID)
                {
                    if(ship.HasInterceptors())
                        lCount += ship.GetInterceptorSystem().GetOccupiedSlotCount();
                }
            }
        }
        
        return lCount;
    }
    
    public int GetTorpedoCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                if(ship.HasTorpedoes())
                    lCount += ship.GetTorpedoSystem().GetOccupiedSlotCount();
            }
        }

        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                if(submarine.HasTorpedoes())
                    lCount += submarine.GetTorpedoSystem().GetOccupiedSlotCount();
            }
        }
        
        return lCount;
    }
    
    public GeoCoord GetMovableTarget(Movable movable)
    {
        switch(movable.GetMoveOrders())
        {
            case MOVE: return movable.GetGeoTarget();
            
            case ATTACK:
            case CAPTURE:
            {
                if(movable.HasTarget() && movable.GetGeoTarget() == null)
                {
                    LaunchEntity target = movable.GetTarget().GetEntity(this);

                    if(target != null)
                    {
                        return ((MapEntity)target).GetPosition().GetCopy();
                    }
                }
                else if(movable.HasGeoTarget())
                {
                    return movable.GetGeoTarget();
                }
            }
        }
        
        if(movable instanceof Airplane)
        {
            Airplane aircraft = (Airplane)movable;
            
            switch(aircraft.GetMoveOrders())
            {
                case PROVIDE_FUEL: return aircraft.GetGeoTarget();
                case SEEK_FUEL:
                {
                    MapEntity target = aircraft.GetTarget().GetMapEntity(this);
                    
                    if(target != null)
                        return target.GetPosition();
                    else 
                        return new GeoCoord();
                }
                
                case RETURN:
                {
                    MapEntity homebase = aircraft.GetHomeBase().GetMapEntity(this);
                    
                    if(homebase != null)
                        return homebase.GetPosition();
                    else
                        return new GeoCoord();
                }
            }
        }
        else if(movable instanceof Infantry)
        {
            Infantry infantry = (Infantry)movable;
            
            switch(infantry.GetMoveOrders())
            {
                case DEFEND: return infantry.GetPosition();
            }
        }
        
        return new GeoCoord();
    }
    
    public int GetPlayerWarehouseCount(Player player)
    {
        int lCount = 0;
        
        if(player != null)
        {
            for(Structure structure : player.GetStructures())
            {
                if(structure instanceof Warehouse)
                {
                    lCount++;
                }
            }
        }
        
        return lCount;
    }
    
    public boolean GetAttackIsBullying(Player inflictor, Player inflictee)
    {
        return false;
        
        /*if(inflictor != null && inflictee != null)
        {
            //If the players are part of alliances that are at war with eachother, return false.
            if(inflictor.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && inflictee.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                if(GetAllianceRelationship(inflictor.GetAllianceMemberID(), inflictee.GetAllianceMemberID()) == Allegiance.ENEMY)
                {
                    return false;
                }
            }
        
            //Instead of a difference check, see if one player is above or below a certain value.
            if(inflictee.GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD && inflictor.GetTotalValue() > Defs.WEAKLING_VALUE_THRESHOLD)
                return true;

            return inflictee.GetTotalValue() > Defs.WEAKLING_VALUE_THRESHOLD && inflictor.GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD; 
        }
        
        return false;*/
    }
    
    public int GetExtractorCount(int lPlayerID)
    {
        int lCount = 0;
        
        for(OreMine mine : GetOreMines())
        {
            if(mine.GetOwnerID() == lPlayerID)
            {
                lCount++;
            }
        }
        
        return lCount;
    }
    
    public Map<ResourceType, Long> GetRequiredCost(Map<ResourceType, Long> costs)
    {
        Map<ResourceType, Long> requirements = new ConcurrentHashMap<>();
        
        for(Entry<ResourceType, Long> cost : costs.entrySet())
        {
            //Fissile, wealth, and nerve agent are never optional when they appear. Nerve agent for chemical weapons, fissile for SSBNs, super carriers, and nukes, and wealth for everything.
            if(cost.getKey() == ResourceType.WEALTH || cost.getKey() == ResourceType.ENRICHED_URANIUM)
            {
                requirements.put(cost.getKey(), cost.getValue());
            }
        }
        
        return requirements;
    }
    
    public Map<ResourceType, Long> GetSubstitutionCost(Map<ResourceType, Long> costs)
    {
        Map<ResourceType, Long> substitutes = new ConcurrentHashMap<>();
        
        for(Entry<ResourceType, Long> cost : costs.entrySet())
        {
            if(cost.getKey() != ResourceType.WEALTH)
            {
                substitutes.put(cost.getKey(), cost.getValue());
            }
        }
        
        return substitutes;
    }
    
    public boolean GetInterceptorIsFast(InterceptorType interceptorType)
    {
        float fltInterceptorSpeed = interceptorType.GetInterceptorSpeed();
        
        //"Fast" means that it can intercept any missile.
        for(MissileType missileType : config.GetMissileTypes())
        {
            if(!missileType.GetICBM() && missileType.GetMissileSpeed() > fltInterceptorSpeed)
                return false;
        }
        
        return true;
    }
    
    public boolean GetMissileIsFast(MissileType missileType)
    {
        float fltMissileSpeed = missileType.GetMissileSpeed();
        
        //"Fast" means that it can outrun some interceptor types.
        for(InterceptorType interceptorType : config.GetInterceptorTypes())
        {
            if(interceptorType.GetInterceptorSpeed() < fltMissileSpeed)
                return true;
        }
        
        return false;
    }
    
    public Map<ResourceType, Long> GetNextCommandPostCost(Player player)
    {
        int lCurrentCount = 0;
        
        for(Structure structure : player.GetStructures())
        {
            if(structure instanceof CommandPost)
            {
                lCurrentCount++;
            }
        }
        
        Map<ResourceType, Long> cost = new ConcurrentHashMap<>();
        Map<ResourceType, Long> baseCost = Defs.COMMAND_POST_STRUCTURE_COST;
        
        LaunchUtilities.AddResourceMapsTogether(cost, LaunchUtilities.ScaleResourceMap(baseCost, (float)lCurrentCount)); 
        
        return cost;
    }
}
