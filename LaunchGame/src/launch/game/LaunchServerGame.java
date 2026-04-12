/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import launch.game.entities.conceptuals.StoredAirplane;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import launch.game.treaties.*;
import java.io.File;
import java.io.IOException;
import launch.comm.LaunchServerComms;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import launch.game.User.BanState;
import launch.game.entities.*;
import launch.game.EntityPointer.EntityType;
import static launch.game.LaunchGame.random;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.MissileFactory.ChatChannel;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.ShipProductionOrder;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredLaunchable;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.AircraftSystem;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.DiscordWebhook;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchEvent;
import launch.utilities.ShortDelay;
import launch.utilities.LongDelay;
import launch.utilities.LaunchEvent.SoundEffect;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchPerf;
import launch.utilities.LaunchReport;
import launch.utilities.LaunchUtilities;
import launch.utilities.LocationSpoofCheck;
import launch.utilities.MissileStats;
import launch.utilities.TerrainChecker;
import launch.utilities.StructureStats;
import launch.game.entities.AirplaneInterface;
import countershield.CounterShieldEngine;
import countershield.model.PlayerRiskProfile;
import launch.game.entities.Torpedo.TorpedoState;
import launch.game.systems.ResourceSystem;

public class LaunchServerGame extends LaunchGame implements LaunchServerGameInterface
{
    private enum PurchaseType
    {
        OFFENSIVE,
        ECONOMIC,
        DEFENSIVE,
    }
    
    private static final int PROSCRIBED_ARTICLE_EXPIRY = 604800000;   //7 days for proscribed IPs and locations to stay proscribed.
    private static final float PROSCRIBED_LOCATION_COLLISION = 10.0f; //10km from banned player locations are proscribed.
    
    private LaunchGame game = this;    
    
    private class ProscribedArticle
    {
        private int lID;
        public LongDelay dlyExpiry;
        
        public ProscribedArticle(int lID)
        {
            this.lID = lID;
            dlyExpiry = new LongDelay(PROSCRIBED_ARTICLE_EXPIRY);
        }
        
        public final void Tick(int lMS)
        {
            dlyExpiry.Tick(lMS);
        }
        
        public int GetID() { return lID; }
        public final boolean Expired() { return dlyExpiry.Expired(); }
    }
    
    private class ProscribedIP extends ProscribedArticle
    {
        public String strIPAddress;
        
        public ProscribedIP(int lID, String strIPAddress)
        {
            super(lID);
            this.strIPAddress = strIPAddress;
        }
    }
    
    private class ProscribedLocation extends ProscribedArticle
    {
        public GeoCoord geoLocation;
        
        public ProscribedLocation(int lID, GeoCoord geoLocation)
        {
            super(lID);
            this.geoLocation = geoLocation;
        }
    }
    
    private final LaunchServerAppInterface application;
    private final LaunchServerComms comms;
    
    private static final short HP_PER_INTERVAL = 1;
    private static final int CHARGE_INTERVAL = 3600000;    //Hourly charge for structures.
    private static final int BACKUP_INTERVAL = 3600000;     //Back the game up every 60 minutes. -Corbin
    
    //Accounts.
    private final Map<String, User> Users = new ConcurrentHashMap();
	
    private final CounterShieldEngine counterShield;
    
    //Indices for new entities.
    private AtomicInteger lAllianceIndex = new AtomicInteger();
    private AtomicInteger lTreatyIndex = new AtomicInteger();
    private AtomicInteger lPlayerIndex = new AtomicInteger();
    private AtomicInteger lLootIndex = new AtomicInteger();
    private AtomicInteger lRubbleIndex = new AtomicInteger();
    private AtomicInteger lMissileSiteIndex = new AtomicInteger();
    private AtomicInteger lSAMSiteIndex = new AtomicInteger();
    private AtomicInteger lSentryGunIndex = new AtomicInteger();
    private AtomicInteger lOreMineIndex = new AtomicInteger();
    private AtomicInteger lRadarStationIndex = new AtomicInteger();
    private AtomicInteger lCommandPostIndex = new AtomicInteger();
    private AtomicInteger lAirbaseIndex = new AtomicInteger();
    private AtomicInteger lArmoryIndex = new AtomicInteger();
    private AtomicInteger lAirplaneIndex = new AtomicInteger();
    private AtomicInteger lRadiationIndex = new AtomicInteger();
    private AtomicInteger lChemicalIndex = new AtomicInteger();
    private AtomicInteger lMissileIndex = new AtomicInteger();
    private AtomicInteger lInterceptorIndex = new AtomicInteger();
    private AtomicInteger lProscribedIPIndex = new AtomicInteger();
    private AtomicInteger lProscribedLocationIndex = new AtomicInteger();
    private AtomicInteger lBankIndex = new AtomicInteger();
    private AtomicInteger lArtilleryGunIndex = new AtomicInteger();
    private AtomicInteger lWarehouseIndex = new AtomicInteger();
    private AtomicInteger lBlueprintIndex = new AtomicInteger();
    private AtomicInteger lAirdropIndex = new AtomicInteger();
    private AtomicInteger lInfantryIndex = new AtomicInteger();
    private AtomicInteger lTankIndex = new AtomicInteger();
    private AtomicInteger lShipyardIndex = new AtomicInteger();
    private AtomicInteger lCargoTruckIndex = new AtomicInteger();
    private AtomicInteger lProcessorIndex = new AtomicInteger();
    private AtomicInteger lDistributorIndex = new AtomicInteger();
    private AtomicInteger lShipIndex = new AtomicInteger();
    private AtomicInteger lSubmarineIndex = new AtomicInteger();
    private AtomicInteger lTorpedoIndex = new AtomicInteger();
    private AtomicInteger lScrapYardIndex = new AtomicInteger();
    private AtomicInteger lDepositIndex = new AtomicInteger();
    
    private Map<Integer, Integer> MissileCounts = new ConcurrentHashMap();      //Used to track existing missile types.
    private Map<Integer, Integer> MissilePurchases = new ConcurrentHashMap();   //Used to track missile purchases.
    private Map<Integer, Integer> MissileSales = new ConcurrentHashMap();       //Used to track missile sale statistics.
    private Map<Integer, Integer> MissileLaunches = new ConcurrentHashMap();    //Used to track missile launch statistics.
    
    private Map<Integer, Integer> InterceptorCounts = new ConcurrentHashMap();      //Used to track existing interceptor types.
    private Map<Integer, Integer> InterceptorPurchases = new ConcurrentHashMap();   //Used to track interceptor purchases.
    private Map<Integer, Integer> InterceptorSales = new ConcurrentHashMap();       //Used to track interceptor sale statistics.
    private Map<Integer, Integer> InterceptorLaunches = new ConcurrentHashMap();    //Used to track interceptor launch statistics.
    
    protected Quadtree quadtree = new Quadtree(this);
    
    //Health generation and radiation damage.
    private ShortDelay dlyHealthGeneration = new ShortDelay();
    
    //Time transition detection (i.e. end of week, day and hour).
    private int lCurrentDay = Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.DAY_OF_WEEK);
    private int lCurrentHour = Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.HOUR_OF_DAY);
    private int lCurrentMinute = Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MINUTE);
    
    //Backup interval.
    private ShortDelay dlyBackup = new ShortDelay(BACKUP_INTERVAL);
    private boolean bDoneLoading = false;
    
    private boolean bWeekEndedTriggered = false;
    
    //Banned locations/IPs.
    private Map<Integer, ProscribedIP> ProscribedIPs = new ConcurrentHashMap();
    private Map<Integer, ProscribedLocation> ProscribedLocations = new ConcurrentHashMap();
    
    public LaunchServerGame(Config config, LaunchServerAppInterface application, int lPort)
    {
        super(config, Defs.SERVER_TICK_RATE);
        this.application = application;
        
        comms = new LaunchServerComms(this, lPort);
		
		counterShield = new CounterShieldEngine("logs/countershield.log",
            (nowMs, playerId, action, score, detail) -> {
                if (action == PlayerRiskProfile.Action.TEMP_SUSPEND)
                    TempBan(playerId, "Suspicious activity (CounterShield)", "CounterShield");

                else if (action == PlayerRiskProfile.Action.BAN)
                    PermaBan(playerId, "Confirmed cheating (CounterShield)", "CounterShield");
            }
        );
    }
	
    @Override
    public CounterShieldEngine GetCounterShield() 
    {
        return counterShield;
    }
    
    /**
     * Thread-safely gets a unique ID for new stuff. 
     * @param atomicInteger The atomic counter to use.
     * @param container The corresponding container for which a unique key is required.
     * @return A unique ID for a new thing.
     */
    private int GetAtomicID(AtomicInteger atomicInteger, Map map)
    {
        int lID = atomicInteger.getAndIncrement();
        
        while(map.containsKey(lID) || lID == MapEntity.ID_NONE)
        {
            lID = atomicInteger.getAndIncrement();
        }
        
        return lID;
    }

    @Override
    public void StartServices()
    {
        // Server only: Perform post-load consolidation.
        for(User user : Users.values())
        {
            Player player = Players.get(user.GetPlayerID());

            if(player != null)
            {
                player.SetUser(user);
            }
        }

        comms.Begin();
        super.StartServices();
    }

    @Override
    protected void CommsTick(int lMS)
    {
        lCommTickStarts++;
        comms.Tick(lMS);
        lCommTickEnds++;
    }
    
    public void ShutDown()
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Shutting down.");
        Save();
        comms.ShutDown();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Stopping service tasks.");
        
        if(seService != null)
        {
            seService.shutdown();
        }
    }
    
    public int GetPlayerIndex()
    {
        return this.lPlayerIndex.get();
    }
    
    public void SetPlayerIndex(int lValue)
    {
        this.lPlayerIndex.set(lValue);
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Setting player index. Current value: %d", lValue));
    }
    
    public void DoneLoading()
    {
        bDoneLoading = true;
        StartServices();
    }
    
    public void Save()
    {
        //TO DO: Pick individual debug flags when they exist (currently just used as boolean to indicate "debug mode").
        if(!config.DebugMode())
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Saving game.");
            application.SaveTheGame();
        }
        else
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Not saving game as we are in debug mode.");
        }
    }
    
    public boolean GetRunning()
    {
        return !comms.GetShutDown();
    }

    @Override
    protected void GameTick(int lMS)
    {
        if(bDoneLoading)
        {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            LaunchPerf.BeginSample();

            //Tell the comms to start buffering any updates we give it, to dispatch at the end of the tick.
            comms.BufferUpdates();

            //Tick proscribed articles.
            for(ProscribedIP proscribedIP : ProscribedIPs.values())
            {
                proscribedIP.Tick(lMS);

                if(proscribedIP.Expired())
                    ProscribedIPs.remove(lMS);
            }

            for(ProscribedLocation proscribedLocation : ProscribedLocations.values())
            {
                proscribedLocation.Tick(lMS);

                if(proscribedLocation.Expired())
                    ProscribedLocations.remove(lMS);
            }

            try
            {
                super.GameTick(lMS);
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.SuperTick);

            try
            {
                ProcessAircraft(lMS);
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.AircraftTick);

            try
            {
                ProcessInfantry();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.InfantryTick);
            
            try
            {
                ProcessPlayerLandUnitDefences();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.ArtilleryTick);

            try
            {
                ProcessTanks();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.TankTick);

            try
            {
                ProcessTrucks();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.TruckTick);

            try
            {
                ProcessShips();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.ShipTick);

            try
            {
                ProcessSubmarines();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.SubmarineTick);

            try
            {
                ProcessTorpedoes(lGameTickRate);
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.TorpedoTick);

            try
            {
                ProcessSentries();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.SentryTick);

            try
            {
                ProcessPlayerMissileDefences();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.MissileDefencesTick);

            try
            {
                ProcessPlayerAircraftDefences();
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(ex.toString());
            }

            LaunchPerf.Measure(LaunchPerf.Metric.AircraftDefencesTick);

            if(Shipyards.isEmpty())
            {
                application.LoadShipyards(this);
            }

            //Process hourly events.
            int lHour = calendar.get(Calendar.HOUR_OF_DAY);

            if(lHour != lCurrentHour)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "It's a new hour!");
                lCurrentHour = lHour;
                HourEnded();
            }

            LaunchPerf.Measure(LaunchPerf.Metric.HourTick);
            
            for(ResourceDeposit deposit : GetResourceDeposits())
            {
                if(deposit.Depleted())
                {
                    deposit.SetRemove();
                    EntityUpdated(deposit, false);
                }
            }

            //Process missile sites.
            for(MissileSite missileSite : GetMissileSites())
            {
                if(missileSite.GetSelling() && missileSite.GetStateTimeExpired())
                {
                    Player owner = Players.get(missileSite.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", missileSite.GetTypeName()), GetSaleValue(missileSite), false);
                    }

                    MissileSites.remove(missileSite.GetID());
                    EntityRemoved(missileSite, false);
                } 
            }

            LaunchPerf.Measure(LaunchPerf.Metric.MissileSiteTick);

            //Process Airbases.
            for(Airbase airbase : GetAirbases())
            {
                if(airbase.GetSelling() && airbase.GetStateTimeExpired())
                {
                    Player owner = Players.get(airbase.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", airbase.GetTypeName()), GetSaleValue(airbase), false);
                    }

                    Airbases.remove(airbase.GetID());
                    EntityRemoved(airbase, false);

                    //This drops resources in stowed aircraft's cargo systems. The reason this doesn't just call "AirbaseDestroyed" is because it will cause a money dupe glitch with the money stored in aircraft being dropped and also reimbursed to the player via GetSaleValue(airbase) above. -Corbin 2/1/2025
                    for(StoredAirplane storedAircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(storedAircraft != null)
                        {
                            Airplanes.remove(storedAircraft.GetID());
                            EntityRemoved(storedAircraft, false);

                            if(storedAircraft.HasCargo())
                            {
                                CargoSystem system = storedAircraft.GetCargoSystem();
                                GeoCoord geoPosition = airbase.GetPosition();

                                if(system != null)
                                {
                                    for(Entry<ResourceType, Long> entry : system.GetResourceMap().entrySet())
                                    {
                                        //Exclude wealth, as noted above.
                                        if(entry.getKey() != ResourceType.WEALTH)
                                        {
                                            long oAmountDropped = entry.getValue();

                                            GeoCoord geoLoot = geoPosition.GetCopy();
                                            geoLoot.Move(random.nextDouble() * (2.0 * Math.PI), config.GetOreCollectRadius());
                                            CreateLoot(geoLoot, LootType.RESOURCES, entry.getKey().ordinal(), oAmountDropped, Defs.LOOT_EXPIRY);
                                        } 
                                    }
                                }
                            }
                        } 
                    }
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.AirbaseTick);

            //Process SAM sites.
            for(SAMSite samSite : GetSAMSites())
            {
                //ProcessStructure(samSite, config.GetSAMSiteMaintenanceCost(), false, true);

                if(samSite.GetSelling() && samSite.GetStateTimeExpired())
                {
                    Player owner = Players.get(samSite.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", samSite.GetTypeName()), GetSaleValue(samSite), false);
                    }

                    SAMSites.remove(samSite.GetID());
                    EntityRemoved(samSite, false);
                }
            }

            //Process ore mines.
            for(OreMine oreMine : GetOreMines())
            {
                if(oreMine.GetSelling() && oreMine.GetStateTimeExpired())
                {
                    Player owner = Players.get(oreMine.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", oreMine.GetTypeName()), GetSaleValue(oreMine), false);
                    }

                    OreMines.remove(oreMine.GetID());
                    EntityRemoved(oreMine, false);
                }
            }

            //Process Radar Stations.
            for(RadarStation radarStation : GetRadarStations())
            {
                if(radarStation.GetSelling() && radarStation.GetStateTimeExpired())
                {
                    Player owner = Players.get(radarStation.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", radarStation.GetTypeName()), GetSaleValue(radarStation), false);
                    }

                    RadarStations.remove(radarStation.GetID());
                    EntityRemoved(radarStation, false);
                }
            }

            //Process CommandPosts.
            for(CommandPost commandPost : GetCommandPosts())
            {
                if(commandPost.GetSelling() && commandPost.GetStateTimeExpired())
                {
                    Player owner = Players.get(commandPost.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", commandPost.GetTypeName()), GetSaleValue(commandPost), false);
                    }

                    CommandPosts.remove(commandPost.GetID());
                    EntityRemoved(commandPost, false);
                }
            }

            //Process Processors.
            for(Processor processor : GetProcessors())
            {
                if(processor.GetSelling() && processor.GetStateTimeExpired())
                {
                    Player owner = Players.get(processor.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", processor.GetTypeName()), GetSaleValue(processor), false);
                    }

                    Processors.remove(processor.GetID());
                    EntityRemoved(processor, false);
                }
            }

            //Process distributors.
            for(Distributor distributor : GetDistributors())
            {
                if(distributor.GetSelling() && distributor.GetStateTimeExpired())
                {
                    Player owner = Players.get(distributor.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", distributor.GetTypeName()), GetSaleValue(distributor), false);
                    }

                    Processors.remove(distributor.GetID());
                    EntityRemoved(distributor, false);
                }
            }

            //Process Scrapyards.
            for(ScrapYard scrapyard : GetScrapYards())
            {
                if(scrapyard.GetSelling() && scrapyard.GetStateTimeExpired())
                {
                    Player owner = Players.get(scrapyard.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", scrapyard.GetTypeName()), GetSaleValue(scrapyard), false);
                    }

                    CargoSystemDestroyed(LaunchEntity.ID_NONE, null, scrapyard.GetPosition(), null, scrapyard.GetResourceSystem());
                    ScrapYards.remove(scrapyard.GetID());
                    EntityRemoved(scrapyard, false);
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.VariousStructureTick);

            //Process logistics depots.
            for(Warehouse warehouse : GetWarehouses())
            {
                if(warehouse.GetSelling() && warehouse.GetStateTimeExpired())
                {
                    Player owner = Players.get(warehouse.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", warehouse.GetTypeName()), GetSaleValue(warehouse), false);
                    }

                    Warehouses.remove(warehouse.GetID());
                    EntityRemoved(warehouse, false);
                }
                else if(warehouse.ProductionFinished())
                {
                    GeoCoord geoSpawn = warehouse.GetPosition().GetCopy();
                    geoSpawn.Move(random.nextDouble() * (2.0 * Math.PI), 0.1);

                    CargoTruck truck = new CargoTruck(GetAtomicID(lCargoTruckIndex, CargoTrucks), geoSpawn, Defs.CARGO_TRUCK_HP, Defs.CARGO_TRUCK_HP, warehouse.GetOwnerID(), new ResourceSystem(Defs.LAND_UNIT_RESOURCE_CAPACITY, Defs.CARGO_TRUCK_TYPES));
                    AddCargoTruck(truck);
                    warehouse.TruckProduced();
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.WarehouseTick);

            //Process Armory.
            for(Armory armory : GetArmories())
            {
                if(armory.GetSelling() && armory.GetStateTimeExpired())
                {
                    Player owner = Players.get(armory.GetOwnerID());

                    if(owner != null)
                    {
                        ProcessPlayerIncome(owner, String.format("sale of %s", armory.GetTypeName()), GetSaleValue(armory), false);
                    }

                    Armories.remove(armory.GetID());
                    EntityRemoved(armory, false);
                }
                else if(armory.ProductionFinished())
                {
                    armory.TankProduced();
                    GeoCoord geoSpawn = armory.GetPosition().GetCopy();
                    geoSpawn.Move(random.nextDouble() * (2.0 * Math.PI), 0.1);
                    
                    if(armory.GetProducingType() == EntityType.INFANTRY)
                    {
                        Infantry infantry = new Infantry(GetAtomicID(lInfantryIndex, Infantries), geoSpawn, Defs.INFANTRY_HP, Defs.INFANTRY_HP, armory.GetOwnerID(), MoveOrders.WAIT, new ResourceSystem(Defs.LAND_UNIT_RESOURCE_CAPACITY, Defs.INFANTRY_TYPES));
                        AddInfantry(infantry);
                    }
                    else
                    {
                        Tank tank = new Tank(GetAtomicID(lTankIndex, Tanks), geoSpawn, Defs.TANK_MAX_HP, Defs.TANK_MAX_HP, armory.GetOwnerID(), armory.GetProducingType(), new ResourceSystem(Defs.LAND_UNIT_RESOURCE_CAPACITY, Defs.GetTankResourceTypes(armory.GetProducingType())));
                        AddTank(tank);
                    }
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.ArmoryTick);

            //TODO: Munitions Plant. Copy the below shipyard production code for launchable production.

            for(Shipyard shipyard : GetShipyards())
            {
                if(!shipyard.Destroyed())
                {
                    for(ShipProductionOrder order : shipyard.GetQueue())
                    {
                        if(order.Finished())
                        {
                            GeoCoord geoSpawn = shipyard.GetOutputCoord().GetCopy();
                            geoSpawn.Move(random.nextDouble() * (2.0 * Math.PI), 0.1);
                            
                            short nMaxHP = 1;
                            MissileSystem missiles = null;
                            MissileSystem torpedoes = null;

                            if(order.GetTypeUnderConstruction() == EntityType.ATTACK_SUB || order.GetTypeUnderConstruction() == EntityType.SSBN)
                            {
                                MissileSystem icbms = null;
                                
                                switch(order.GetTypeUnderConstruction())
                                {
                                    case ATTACK_SUB:
                                    {
                                        nMaxHP = Defs.ATTACK_SUB_MAX_HP;
                                        missiles = new MissileSystem(null, Defs.NAVAL_MISSILE_RELOAD, Defs.ATTACK_SUB_MISSILE_SLOT_COUNT);
                                        torpedoes = new MissileSystem(null, Defs.NAVAL_TORPEDO_RELOAD, Defs.ATTACK_SUB_TORPEDO_SLOT_COUNT);
                                    }
                                    break;
                                    
                                    case SSBN:
                                    {
                                        nMaxHP = Defs.SSBN_MAX_HP;
                                        missiles = new MissileSystem(null, Defs.NAVAL_MISSILE_RELOAD, Defs.SSBN_MISSILE_SLOT_COUNT);
                                        torpedoes = new MissileSystem(null, Defs.NAVAL_TORPEDO_RELOAD, Defs.SSBN_TORPEDO_SLOT_COUNT);
                                        icbms = new MissileSystem(null, Defs.SSBN_ICBM_RELOAD, Defs.SSBN_ICBM_SLOT_COUNT);
                                    }
                                    break;
                                }
                                
                                Submarine submarine = new Submarine(GetAtomicID(lSubmarineIndex, Submarines), geoSpawn, order.GetProducingForID(), nMaxHP, nMaxHP, order.GetTypeUnderConstruction(), missiles, torpedoes, icbms);
                                AddSubmarine(submarine);
                                order.SetCompleted();
                                EntityUpdated(shipyard, false);
                            }
                            else
                            {
                                CargoSystem cargo = null;
                                MissileSystem artillery = null;
                                int lSentryCount = 0;
                                MissileSystem interceptors = null;
                                AircraftSystem aircraft = null;
                                
                                switch(order.GetTypeUnderConstruction())
                                {
                                    case FRIGATE:
                                    {
                                        nMaxHP = Defs.FRIGATE_MAX_HP;
                                        missiles = new MissileSystem(null, Defs.NAVAL_MISSILE_RELOAD, Defs.FRIGATE_MISSILE_SLOT_COUNT);
                                        torpedoes = new MissileSystem(null, Defs.NAVAL_TORPEDO_RELOAD, Defs.FRIGATE_TORPEDO_SLOT_COUNT);
                                        lSentryCount = Defs.FRIGATE_SENTRY_COUNT;
                                        interceptors = new MissileSystem(null, Defs.NAVAL_INTERCEPTOR_RELOAD, Defs.FRIGATE_INTERCEPTOR_SLOT_COUNT);
                                    }
                                    break;
                                    
                                    case DESTROYER:
                                    {
                                        nMaxHP = Defs.DESTROYER_MAX_HP;
                                        missiles = new MissileSystem(null, Defs.NAVAL_MISSILE_RELOAD, Defs.DESTROYER_MISSILE_SLOT_COUNT);
                                        torpedoes = new MissileSystem(null, Defs.NAVAL_TORPEDO_RELOAD, Defs.DESTROYER_TORPEDO_SLOT_COUNT);
                                        lSentryCount = Defs.DESTROYER_SENTRY_COUNT;
                                        interceptors = new MissileSystem(null, Defs.NAVAL_INTERCEPTOR_RELOAD, Defs.DESTROYER_INTERCEPTOR_SLOT_COUNT);
                                    }
                                    break;
                                    
                                    case CARGO_SHIP:
                                    {
                                        nMaxHP = Defs.CARGO_SHIP_MAX_HP;
                                        cargo = new CargoSystem(null, Defs.CARGO_SHIP_CAPACITY);
                                    }
                                    break;
                                    
                                    case FLEET_OILER:
                                    {
                                        nMaxHP = Defs.FLEET_OILER_MAX_HP;
                                        lSentryCount = Defs.FLEET_OILER_SENTRY_COUNT;
                                    }
                                    break;
                                    
                                    case AMPHIB:
                                    {
                                        nMaxHP = Defs.AMPHIB_MAX_HP;
                                        lSentryCount = Defs.AMPHIB_SENTRY_COUNT;
                                        cargo = new CargoSystem(null, Defs.AMPHIB_CAPACITY);
                                        aircraft = new AircraftSystem(null, Defs.AMPHIB_AIRCRAFT_RELOAD_TIME, Defs.AMPHIB_AIRCRAFT_SLOT_COUNT);
                                    }
                                    break;
                                    
                                    case SUPER_CARRIER:
                                    {
                                        nMaxHP = Defs.SUPER_CARRIER_MAX_HP;
                                        aircraft = new AircraftSystem(null, Defs.SUPER_CARRIER_AIRCRAFT_RELOAD_TIME, Defs.SUPER_CARRIER_AIRCRAFT_SLOT_COUNT);
                                    }
                                    break;
                                }
                                
                                Ship ship = new Ship(GetAtomicID(lShipIndex, Ships), geoSpawn, order.GetProducingForID(), nMaxHP, nMaxHP, order.GetTypeUnderConstruction(), missiles, torpedoes, cargo, artillery, lSentryCount, interceptors, aircraft);
                                AddShip(ship);
                                order.SetCompleted();
                                EntityUpdated(shipyard, false);
                            }
                        }
                    }
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.ShipyardTick);

            for(Blueprint blueprint : GetBlueprints())
            {
                if(blueprint.Expired())
                {
                    Blueprints.remove(blueprint.GetID());
                    EntityRemoved(blueprint, true);
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.BlueprintTick);

            //Process health regeneration.
            dlyHealthGeneration.Tick(lMS);

            for(Player player : GetPlayers())
            {                
                if(player.GetRadAlertSent() && !GetRadioactive(player, true))
                {
                    player.ResetRadAlert();
                }

                //Process player ranks.
                if(!player.GetAWOL())
                {
                    if(player.GetExperience() >= GetNextRankThreshold(player.GetRank()))
                    {
                        PlayerRankUp(player);
                    }

                    if(player.GetExperience() < GetLastXPThreshold(player))
                    {
                        if(player.GetRank() > 0)
                        {
                            PlayerRankDown(player);
                        }
                    }
                }

                //Process respawn protection.
                if(player.GetRespawnProtected())
                {
                    if(player.GetStateTimeExpired())
                    {
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s's respawn protection time expired.", player.GetName()));
                        RemoveRespawnProtection(player, false);
                    }
                }

                //Process AWOL.
                if(!player.GetAWOL())
                {
                    if(player.GetLastSeen() + config.GetAWOLTime() < System.currentTimeMillis() && !player.GetBoss())
                    {
                        SetPlayerAWOL(player, false);

                        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            Alliance alliance = Alliances.get(player.GetAllianceMemberID());

                            //Disband the alliance?
                            AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);

                            //Set the player's alliance ID to unaffiliated so there's no crash if they come back.
                            player.SetIsAnMP(false);
                            player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                        }
                    }
                }
                else
                {
                    //Remove AWOL players after specified period of time.
                    if((player.GetLastSeen() + config.GetRemoveTime() < System.currentTimeMillis()) && !player.GetIsAnAdmin())
                    {
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing player ID %d (%s) from the game as they've been AWOL for too long...", player.GetID(), player.GetName()));
                        CleanUpOwnedEntities(player.GetID());

                        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            if(AllianceMemberRosters.containsKey(player.GetAllianceMemberID()))
                            {
                                Map<Integer, Player> Members = AllianceMemberRosters.get(player.GetAllianceMemberID());

                                if(Members != null)
                                {
                                    if(Members.containsKey(player.GetID()))
                                    {
                                        Members.remove(player.GetID(), player);
                                        AllianceMemberRosters.put(player.GetAllianceMemberID(), Members);
                                    }
                                }
                            }
                        }

                        //Remove the user if they are not banned (otherwise keep it to ensure they serve their sentence).
                        User removableUser = player.GetUser();

                        if(removableUser != null)
                        {
                            if(removableUser.GetBanState() == BanState.NOT)
                                Users.remove(removableUser.GetIdentityKey());
                            else
                                removableUser.NullPlayerID();
                        }
                    }
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.PlayerTick);

            //Process airdrops.
            for(Airdrop airdrop : Airdrops.values())
            {
                if(airdrop.Arrived())
                {
                    Airdrops.remove(airdrop.GetID());
                    EntityRemoved(airdrop, true);

                    CreateLoot(airdrop.GetPosition().GetCopy(), LootType.RESOURCES, ResourceType.WEALTH.ordinal(), Defs.AIRDROP_VALUE, Defs.AIRDROP_EXPIRY);
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.AirdropTick);

            //Process end of day/week events.
            int lDay = calendar.get(Calendar.DAY_OF_WEEK);

            if(lDay != lCurrentDay)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "It's a new day!");

                //Midnight has passed, this is a new day.
                lCurrentDay = lDay;

                DayEnded();
            }
            
            //Weekly rollover at 21:00 UTC Sunday
            if(lDay == Calendar.SATURDAY && lHour == 21 && !bWeekEndedTriggered)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Weekly reset triggered.");

                WeekEnded();
                bWeekEndedTriggered = true;
            }

            //Reset the guard after the hour passes
            if(lHour != 21)
            {
                bWeekEndedTriggered = false;
            }

            LaunchPerf.Measure(LaunchPerf.Metric.WeekTick);

            //Process user ticks (ban durations and attack alert status)
            for(User user : Users.values())
            {
                user.Tick(lMS);

                if(user.GetUnderAttack())
                {
                    if(user.GetToken() != null && !user.GetToken().isBlank() && user.CanReceiveAlerts())
                    {
                        try
                        {
                            user.ClearAlarms();
                            
                            Message message = Message.builder()
                            .putData("title", "You're under attack!")
                            .putData("body", "Missiles inbound!")
                            .setToken(user.GetToken())
                            .build();

                            FirebaseMessaging.getInstance().send(message);

                            user.SetAlertInterval();

                            LaunchLog.ConsoleMessage(String.format("Sending alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                        }
                        catch(FirebaseMessagingException ex)
                        {
                            LaunchLog.ConsoleMessage(String.format("Could not send alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                            TokenUpdate(user);
                        }
                    }

                    user.ClearAlarms();
                }
                else if(user.GetAllyUnderAttack())
                {
                    if(user.GetToken() != null && !user.GetToken().isBlank() && user.CanReceiveAlerts())
                    {
                        try
                        {
                            user.ClearAlarms();
                            
                            Message message = Message.builder()
                            .putData("title", "Ally under attack!")
                            .putData("body", "Your ally has missiles inbound!")
                            .setToken(user.GetToken())
                            .build();

                            FirebaseMessaging.getInstance().send(message);

                            user.SetAlertInterval();

                            LaunchLog.ConsoleMessage(String.format("Sending ally attack alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));        
                        }
                        catch(FirebaseMessagingException ex)
                        {
                            LaunchLog.ConsoleMessage(String.format("Could not send ally attack alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                            user.ClearAlarms();
                        }
                    }

                    user.ClearAlarms();
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.UserTick);

            for(ArtilleryGun artillery : GetArtilleryGuns())
            {
                if(!artillery.Destroyed())
                {
                    if(artillery.GetOnline())
                    {
                        if(artillery.HasFireOrder())
                        {
                            MissileSystem system = artillery.GetMissileSystem();
                            FireOrder order = artillery.GetFireOrder();
                            GeoCoord geoTarget = order.GetGeoTarget().GetCopy();
                            Player owner = GetOwner(artillery);

                            if(owner != null && geoTarget != null)
                            {
                                if(system.GetReadySlotCount() > 0)
                                {
                                    if(system.ReadyToFire())
                                    {
                                        for(byte lSlotNumber = 0; lSlotNumber < system.GetSlotCount(); lSlotNumber++)
                                        {
                                            if(system.GetSlotHasMissile(lSlotNumber) && system.GetSlotReadyToFire(lSlotNumber))
                                            {
                                                MissileType shell = config.GetMissileType(system.GetSlotMissileType(lSlotNumber));

                                                if(shell != null)
                                                {
                                                    geoTarget.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * order.GetRadius());

                                                    artillery.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                    system.Fire(lSlotNumber);

                                                    system.SetReloadTimeRemaining(Defs.ARTILLERY_GUN_RELOAD_TIME);

                                                    CreateMissileLaunch(artillery, artillery.GetPosition().GetCopy(), shell.GetID(), owner.GetID(), geoTarget, null, false);
                                                    CreateEvent(new LaunchEvent(String.format("%s's artillery fired %s.", owner.GetName(), shell.GetName()), SoundEffect.ARTILLERY_FIRE));

                                                    break;
                                                } 
                                            }
                                        }
                                    } 
                                }
                                else
                                {
                                    artillery.RoundsComplete();
                                }  
                            }
                            else
                            {
                                artillery.RoundsComplete();
                            }   
                        }
                    }
                    else if(artillery.GetSelling() && artillery.GetStateTimeExpired())
                    {
                        Player owner = Players.get(artillery.GetOwnerID());

                        if(owner != null)
                        {
                            ProcessPlayerIncome(owner, String.format("sale of %s", artillery.GetTypeName()), GetSaleValue(artillery), false);
                        }

                        ArtilleryGuns.remove(artillery.GetID());
                        EntityRemoved(artillery, false);
                    }
                } 
            }

            LaunchPerf.Measure(LaunchPerf.Metric.ArtilleryTick);

            //Process interceptor travel distance and lost targets.
            for(Interceptor interceptor : GetInterceptors())
            {
                if(interceptor.GetDistanceTraveled() >= config.GetInterceptorRange(config.GetInterceptorType(interceptor.GetType())))
                {
                    Interceptors.remove(interceptor.GetID());
                    EntityRemoved(interceptor, false);
                    CreateEvent(new LaunchEvent(String.format("%s's interceptor ran out of fuel and crashed.", GetPlayer(interceptor.GetOwnerID()).GetName()), SoundEffect.INTERCEPTOR_MISS));
                }
                else
                {
                    float fltSpeed = config.GetInterceptorSpeed(config.GetInterceptorType(interceptor.GetType()));
                    float fltDistance = (fltSpeed/Defs.MS_PER_HOUR) * lMS;
                    interceptor.Traveled(fltDistance);
                }

                switch(interceptor.GetTargetType())
                {
                    case MISSILE:
                    {
                        Missile targetMissile = Missiles.get(interceptor.GetTargetID());

                        //Interceptors are flying until they reach or lose their target, after which they are removed from the game.
                        if(targetMissile == null)
                        {
                            //Target lost.
                            InterceptorLostTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, false);
                        }
                        else if(!targetMissile.Flying())
                        {
                            //Target lost.
                            InterceptorLostTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, false);
                        }
                    }
                    break;

                    case AIRPLANE:
                    {
                        Airplane targetAircraft = GetAirplane(interceptor.GetTargetID());

                        //Interceptors are flying until they reach or lose their target, after which they are removed from the game.
                        if(targetAircraft == null)
                        {
                            //Target lost.
                            InterceptorLostTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, false);
                        }
                        else if(targetAircraft.Destroyed())
                        {
                            //Target lost.
                            InterceptorLostTarget(interceptor);
                            Interceptors.remove(interceptor.GetID());
                            EntityRemoved(interceptor, false);
                        }
                    }
                    break;
                }
            }

            LaunchPerf.Measure(LaunchPerf.Metric.InterceptorTick);

            //Tell the comms to dispatch the updates.
            comms.DispatchUpdates();

            //Backup periodically.
            dlyBackup.Tick(lMS);

            if(dlyBackup.Expired())
            {
                Save();
                dlyBackup.Set(BACKUP_INTERVAL);
            }

            LaunchPerf.Measure(LaunchPerf.Metric.DispatchAndBackup);

            LaunchPerf.Consolidate();

            lGameTickEnds++;
        }
    }
    
    private void ProcessHourlyMaintenance()
    {
        for(Player player : GetPlayers())
        {
            int lTotalCost = 0;
            
            lTotalCost += GetHourlyMissileMaintenance(player.GetID());
            
            lTotalCost += GetHourlyInterceptorMaintenance(player.GetID());
            
            lTotalCost += GetHourlyTorpedoMaintenance(player.GetID());
            
            //Structure maintenance
            for(Structure structure : GetAllStructures())
            {
                if(structure.GetOwnedBy(player.GetID()))
                {
                    if(structure.GetOffline())
                    {
                        lTotalCost += Defs.OFFLINE_MAINTENANCE_COST;
                        
                        if(lTotalCost > player.GetWealth())
                        {
                            short NeglectDamage = (short) 10; //(0.15 * structure.GetMaxHP());
                            structure.InflictDamage(NeglectDamage);
                            player.SubtractExperience(25);
                            CreateReport(player, new LaunchReport(String.format("[ECONOMY] Your %s took damage due to lack of upkeep. You lost 50 xp. Sad!", structure.GetTypeName()), true, player.GetID()));
                        }
                    }
                    else if(structure.GetRunning())
                    {   
                        lTotalCost += config.GetMaintenanceCost(structure);

                        if(lTotalCost > player.GetWealth())
                        {
                            //Insufficient funds. Take the site offline.
                            structure.TakeOffline();
                            player.SubtractExperience(25);
                            CreateEvent(new LaunchEvent(String.format("%s's %s went offline due to lack of funds. They lost 25 xp. Sad!", player.GetName(), structure.GetTypeName())));
                            CreateReport(player, new LaunchReport(String.format("[ECONOMY] Your %s went offline due to lack of funds. You lost 25 xp. Sad!", structure.GetTypeName()), true, player.GetID()));
                        }
                    }
                }
            }
            
            for(Airplane aircraft : GetAirplanes())
            {
                if(aircraft.GetOwnerID() == player.GetID())
                { 
                    lTotalCost += Defs.GetAircraftMaintenanceCost(aircraft.GetEntityType());
                    
                    if(lTotalCost > player.GetWealth())
                    {
                        aircraft.InflictDamage((short)(aircraft.GetMaxHP() * 0.1));
                        player.SubtractExperience(25);
                        CreateEvent(new LaunchEvent(String.format("%s's %s took damage due to lack of funds. They lost 25 xp. Sad!", player.GetName(), aircraft.GetTypeName())));
                        CreateReport(player, new LaunchReport(String.format("[ECONOMY] Your %s took damage due to lack of funds. You lost 25 xp. Sad!", aircraft.GetTypeName()), true, player.GetID()));
                    }
                }
            }
            
            for(Ship ship : GetShips())
            {
                if(ship.GetOwnerID() == player.GetID())
                {
                    int lShipCost = Defs.GetNavalMaintenanceCost(ship.GetEntityType());
                
                    if(ShipInPort(ship))
                        lShipCost *= 0.1f;

                    lTotalCost += lShipCost;
                    
                    if(lTotalCost > player.GetWealth())
                    {
                        //Inflict neglect damage on the infantry.
                        ship.InflictDamage((short)(ship.GetMaxHP() * 0.1f));
                        player.SubtractExperience(25);
                        CreateReport(player, new LaunchReport("[ECONOMY] Your ship took damage due to lack of funds. You lost 25 xp. Sad!", true, player.GetID()));
                    }
                }
            }
            
            for(Submarine submarine : GetSubmarines())
            {
                if(submarine.GetOwnerID() == player.GetID())
                {
                    int lSubmarineCost = Defs.GetNavalMaintenanceCost(submarine.GetEntityType());
            
                    if(ShipInPort(submarine))
                        lSubmarineCost *= 0.1f;

                    lTotalCost += lSubmarineCost;
                    
                    if(lTotalCost > player.GetWealth())
                    {
                        //Inflict neglect damage on the infantry.
                        submarine.InflictDamage((short)(submarine.GetMaxHP() * 0.1f));
                        player.SubtractExperience(25);
                        CreateReport(player, new LaunchReport("[ECONOMY] Your submarine took damage due to lack of funds. You lost 25 xp. Sad!", true, player.GetID()));
                    }
                }
            }
            
            player.ChargeWealth(lTotalCost);
        }
    }
    
    private void RemoveRespawnProtection(Player player, boolean bLeftEarly)
    {
        if(player.GetRespawnProtected())
        {
            player.SetRespawnProtected(false);
            
            //Remove respawn protection from structures.
            for(Structure structure : GetAllStructures())
            {
                if(structure.GetOwnerID() == player.GetID())
                {
                    if(structure.GetRespawnProtected())
                        structure.SetRespawnProtected(false);
                }
            }
            
            EstablishAllStructureThreats(LaunchEntity.ID_NONE);
        }
    }
    
    /**
     * Sets a player to AWOL.
     * @param player Player to set AWOL.
     * @param bDoRageQuitCheck If the player has requested AWOL, set this to true to enable rage quit sanctioning checks.
     */
    private void SetPlayerAWOL(Player player, boolean bDoRageQuitCheck)
    {
        RemoveRespawnProtection(player, false);
        
        if(player.GetUser() != null)
            SendUserAlert(player.GetUser(), "AWOL Warning", "You've gone AWOL! If you do not log in within 7 days your player will be deleted!", true, false);
        
        player.SetAWOL(true);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            Alliance alliance = Alliances.get(player.GetAllianceMemberID());
            AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, true);
        }

        CreateEvent(new LaunchEvent(String.format("%s has gone AWOL.", player.GetName())));
    }
    
    private void MinuteEnded()
    {        
        /*for(Aircraft aircraft : GetAircrafts())
        {
            if(aircraft.Armed())
            {
                for(Player player : new ArrayList<>(quadtree.GetAffectedPlayers(aircraft.GetPosition(), config.GetAirspaceDistance())))
                {
                    if(AircraftIsHostile(aircraft, player))
                    {
                        if(!aircraft.GetOwnedBy(player.GetID()) && aircraft.GetPosition().RadiusPhaseCollisionTest(player.GetPosition(), config.GetAirspaceDistance()))
                        {
                            if(aircraft.GetPosition().DistanceTo(player.GetPosition()) <= config.GetAirspaceDistance())
                                player.AddNearbyAircraft(aircraft.GetID());
                            else
                                player.RemoveNearbyAircraft(aircraft.GetID());
                        }
                        else
                            player.RemoveNearbyAircraft(aircraft.GetID());
                    }
                }
            }
        }*/
        
        for(Player player : GetPlayers())
        {
            //Ships
            
            for(Ship ship : GetShips())
            {
                if(!ship.Destroyed())
                {
                    ship.ClearContactBearings();

                    for(Ship otherShip : GetShips())
                    {
                        if(!otherShip.GetVisible() && otherShip.GetMoveOrders() != MoveOrders.WAIT && random.nextFloat() <= Defs.CONTACT_BEARING_CHANCE)
                        {
                            if(!EntityIsFriendly(otherShip, GetOwner(ship)))
                            {
                                if(otherShip.GetPosition().DistanceTo(ship.GetPosition()) <= Defs.CONTACT_BEARING_MAX_RANGE)
                                {
                                    ship.AddContactBearing((float)ship.GetPosition().BearingTo(otherShip.GetPosition()));
                                }
                            }
                        } 
                    }

                    for(Submarine otherSubmarine : GetSubmarines())
                    {
                        if(!otherSubmarine.GetVisible() && otherSubmarine.GetMoveOrders() != MoveOrders.WAIT && random.nextFloat() <= Defs.CONTACT_BEARING_CHANCE)
                        {
                            if(!EntityIsFriendly(otherSubmarine, GetOwner(ship)))
                            {
                                if(otherSubmarine.GetPosition().DistanceTo(ship.GetPosition()) <= Defs.CONTACT_BEARING_MAX_RANGE)
                                {
                                    ship.AddContactBearing((float)ship.GetPosition().BearingTo(otherSubmarine.GetPosition()));
                                }
                            }
                        }
                    }
                }
            }
            
            //SURFACED submarines
            
            for(Submarine submarine : GetSubmarines())
            {
                if(!submarine.Destroyed())
                {
                    submarine.ClearContactBearings();

                    for(Ship otherShip : GetShips())
                    {
                        if(!otherShip.GetVisible() && otherShip.GetMoveOrders() != MoveOrders.WAIT && random.nextFloat() <= Defs.CONTACT_BEARING_CHANCE)
                        {
                            if(!EntityIsFriendly(otherShip, GetOwner(submarine)))
                            {
                                if(otherShip.GetPosition().DistanceTo(submarine.GetPosition()) <= Defs.CONTACT_BEARING_MAX_RANGE)
                                {
                                    submarine.AddContactBearing((float)submarine.GetPosition().BearingTo(otherShip.GetPosition()));
                                }
                            }
                        } 
                    }

                    for(Submarine otherSubmarine : GetSubmarines())
                    {
                        if(!otherSubmarine.GetVisible() && otherSubmarine.GetMoveOrders() != MoveOrders.WAIT && random.nextFloat() <= Defs.CONTACT_BEARING_CHANCE)
                        {
                            if(!EntityIsFriendly(otherSubmarine, GetOwner(submarine)))
                            {
                                if(otherSubmarine.GetPosition().DistanceTo(submarine.GetPosition()) <= Defs.CONTACT_BEARING_MAX_RANGE)
                                {
                                    submarine.AddContactBearing((float)submarine.GetPosition().BearingTo(otherSubmarine.GetPosition()));
                                }
                            }
                        }  
                    }
                }
            }
        }
        
        for(Airbase airbase : GetAirbases())
        {
            if(airbase.GetOnline())
            {
                for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                {
                    if(!aircraft.AtFullHealth() && EntityIsFriendly(aircraft, GetOwner(airbase)))
                        aircraft.AddHP(Defs.AIRCRAFT_HP_RECOVER_PER_MIN);
                }
            }
        }
    }
    
    /**
     * To be called at the end of every hour to process hourly events.
     */
    private void HourEnded()
    {
        ProcessHourlyMaintenance();
        
        for(Distributor distributor : GetDistributors())
        {
            if(distributor.GetOnline())
            {
                Player owner = GetOwner(distributor);
                
                if(owner != null && !GetRadioactive(distributor, true))
                {
                    for(Structure structure : owner.GetStructures())
                    {
                        if(structure.GetOnline() && !structure.ApparentlyEquals(distributor))
                        {
                            if(structure instanceof Warehouse warehouse)
                            {
                                CargoSystem system = warehouse.GetCargoSystem();
                                long oAmountPresent = warehouse.GetCargoSystem().GetAmountOfType(distributor.GetType());

                                if(!distributor.GetResourceSystem().Full() && oAmountPresent > 0)
                                {
                                    if(warehouse.GetPosition().DistanceTo(distributor.GetPosition()) <= Defs.DISTRIBUTOR_ACTION_DISTANCE)
                                    {
                                        long oAmountToTake = oAmountPresent >= Defs.DISTRIBUTOR_ACTION_PER_HOUR ? Defs.DISTRIBUTOR_ACTION_PER_HOUR : oAmountPresent;

                                        system.ChargeQuantity(distributor.GetType(), oAmountToTake);
                                        long oLeftover = distributor.GetResourceSystem().AddQuantity(distributor.GetType(), oAmountToTake);
                                        
                                        if(oLeftover > 0)
                                        {
                                            system.AddResource(distributor.GetType(), oLeftover);
                                        }
                                        
                                        EntityUpdated(warehouse, true);
                                    }  
                                }
                            }

                            if(!structure.GetResourceSystem().Full() && structure.GetResourceSystem().CanHoldType(distributor.GetType()))
                            {
                                long oAmountToAdd = distributor.GetResourceSystem().GetAmountOfType(distributor.GetType()) >= Defs.DISTRIBUTOR_ACTION_PER_HOUR ? Defs.DISTRIBUTOR_ACTION_PER_HOUR : distributor.GetResourceSystem().GetAmountOfType(distributor.GetType());
                                
                                distributor.GetResourceSystem().ChargeQuantity(distributor.GetType(), oAmountToAdd);
                                long oLeftover = structure.GetResourceSystem().AddQuantity(distributor.GetType(), oAmountToAdd);
                                        
                                if(oLeftover > 0)
                                {
                                    distributor.GetResourceSystem().AddQuantity(distributor.GetType(), oLeftover);
                                }
                                
                                EntityUpdated(structure, true);
                            }
                        } 
                    }
                    
                    for(EntityPointer pointer : new ArrayList<>(quadtree.GetAffectedLandUnits(distributor.GetPosition(), Defs.DISTRIBUTOR_ACTION_DISTANCE)))
                    {
                        LandUnit unit = pointer.GetLandUnit(game);
                        
                        if(unit != null)
                        {
                            if(EntityIsFriendly(unit, owner) && unit.GetPosition().DistanceTo(distributor.GetPosition()) <= Defs.DISTRIBUTOR_ACTION_DISTANCE)
                            {
                                if(!unit.GetResourceSystem().Full() && unit.GetResourceSystem().CanHoldType(distributor.GetType()))
                                {
                                    long oAmountToAdd = distributor.GetResourceSystem().GetAmountOfType(distributor.GetType()) >= Defs.DISTRIBUTOR_ACTION_PER_HOUR ? Defs.DISTRIBUTOR_ACTION_PER_HOUR : distributor.GetResourceSystem().GetAmountOfType(distributor.GetType());

                                    distributor.GetResourceSystem().ChargeQuantity(distributor.GetType(), oAmountToAdd);
                                    long oLeftover = unit.GetResourceSystem().AddQuantity(distributor.GetType(), oAmountToAdd);

                                    if(oLeftover > 0)
                                    {
                                        distributor.GetResourceSystem().AddQuantity(distributor.GetType(), oLeftover);
                                    }
                                    
                                    EntityUpdated(unit, true);
                                }
                            }
                        }
                        else
                        {
                            quadtree.RemoveEntity(pointer);
                        }
                        
                        EntityUpdated(distributor, true);
                    }
                }
                else if(owner != null)
                {
                    CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your %s is irradiated and could not transport resources.", distributor.GetTypeName()), true, owner.GetID()));
                    CreateEventForPlayer(new LaunchEvent(String.format("[ECONOMY] Your %s is irradiated and could not transport resources.", distributor.GetTypeName())), owner.GetID());
                }
            }
        }
        
        for(OreMine oreMine : GetOreMines())
        {
            if(oreMine.GetOnline())
            {
                Player owner = GetOwner(oreMine);
                ResourceDeposit deposit = GetResourceDeposit(oreMine.GetDepositID());

                if(!GetRadioactive(oreMine, true) && deposit != null)
                {
                    long oAmountToExtract = deposit.Extract(random.nextLong(Defs.EXTRACTOR_OUTPUT_MIN, Defs.EXTRACTOR_OUTPUT_MAX));

                    GeoCoord geoDrop = oreMine.GetPosition().GetCopy();
                    geoDrop.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 0.3f);
                    int lTypeOrdinal = oreMine.GetType().ordinal();
                    CreateLoot(geoDrop, LootType.RESOURCES, lTypeOrdinal, oAmountToExtract, Defs.LOOT_EXPIRY);
                }
                else if(owner != null)
                {
                    CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your %s is irradiated and could not produce.", oreMine.GetTypeName()), true, owner.GetID()));
                    CreateEventForPlayer(new LaunchEvent(String.format("[ECONOMY] Your %s is irradiated and could not produce.", oreMine.GetTypeName())), owner.GetID());
                }  
                
                if(deposit == null)
                {
                    oreMine.Sell(config.GetDecommissionTime(oreMine));
                    
                    if(owner != null)
                    {
                        CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your %s finished extraction and is selling automatically.", oreMine.GetTypeName()), true, owner.GetID()));
                        CreateEventForPlayer(new LaunchEvent(String.format("[ECONOMY] Your %s finished extraction and is selling automatically.", oreMine.GetTypeName())), owner.GetID());
                    }
                }
            }
        }
        
        for(Processor processor : GetProcessors())
        {
            if(processor.GetOnline())
            {
                Player owner = GetOwner(processor);
                
                if(!GetRadioactive(processor, true))
                {
                    ResourceSystem system = processor.GetResourceSystem();

                    if(system != null)
                    {
                        Map<ResourceType, Long> Inputs = Defs.GetProcessorInput(processor.GetType());
                        Map<ResourceType, Long> Outputs = Defs.GetProcessorOutput(processor.GetType());

                        if(Inputs != null && Outputs != null)
                        {
                            //Charge and produce
                            if(system.HasNecessaryResources(Inputs))
                            {
                                system.ChargeQuantities(Inputs);

                                for(Entry<ResourceType, Long> output : Outputs.entrySet())
                                {
                                    GeoCoord geoDrop = processor.GetPosition().GetCopy();
                                    geoDrop.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 0.3f);
                                    CreateLoot(geoDrop, LootType.RESOURCES, output.getKey().ordinal(), output.getValue(), Defs.LOOT_EXPIRY);
                                }

                                EntityUpdated(processor, false);
                            }
                        }
                    }
                }
                else if(owner != null)
                {
                    CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your %s is irradiated and could not produce.", processor.GetTypeName()), true, owner.GetID()));
                    CreateEventForPlayer(new LaunchEvent(String.format("[ECONOMY] Your %s is irradiated and could not produce.", processor.GetTypeName())), owner.GetID());
                }
            }
        }
        
        for(ScrapYard yard : GetScrapYards())
        {
            if(yard.GetOnline())
            {
                Player owner = GetOwner(yard);
                
                if(!GetRadioactive(yard, true))
                {
                    ResourceSystem system = yard.GetResourceSystem();

                    if(system != null)
                    {
                        Map<ResourceType, Long> scrappedTypes = new ConcurrentHashMap<>();
                        long oScrapped = 0;
                        
                        for(Entry<ResourceType, Long> resource : system.GetTypes().entrySet())
                        {
                            if(resource.getValue() > 0 && oScrapped < Defs.SCRAPYARD_RATE_PER_HOUR)
                            {
                                long oRemainingCapacity = Defs.SCRAPYARD_RATE_PER_HOUR - oScrapped;
                                long oAmountScrapped = 0;
                                
                                if(resource.getValue() <= oRemainingCapacity)
                                {
                                    oScrapped += resource.getValue();
                                    oAmountScrapped = resource.getValue();
                                }
                                else
                                {
                                    oScrapped += oRemainingCapacity;
                                    oAmountScrapped = oRemainingCapacity;
                                }
                                
                                scrappedTypes.put(resource.getKey(), oAmountScrapped);
                                system.ChargeQuantity(resource.getKey(), oAmountScrapped);
                            }
                        }

                        if(!scrappedTypes.isEmpty())
                        {
                            long oValueToProduce = 0;
                            
                            for(Entry<ResourceType, Long> typeToScrap : scrappedTypes.entrySet())
                            {
                                oValueToProduce += (typeToScrap.getValue() * Defs.GetResourceScrapValuePerKG(typeToScrap.getKey()));
                            }
                            
                            if(oValueToProduce > 0)
                            {
                                GeoCoord geoDrop = yard.GetPosition().GetCopy();
                                geoDrop.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 0.3f);
                                CreateLoot(geoDrop, LootType.RESOURCES, ResourceType.WEALTH.ordinal(), oValueToProduce, Defs.LOOT_EXPIRY);
                            }
                            
                            EntityUpdated(yard, true);
                        }
                    }
                }
                else if(owner != null)
                {
                    CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your %s is irradiated and could not produce.", yard.GetTypeName()), true, owner.GetID()));
                    CreateEventForPlayer(new LaunchEvent(String.format("[ECONOMY] Your %s is irradiated and could not produce.", yard.GetTypeName())), owner.GetID());
                }
            }
        }
        
        //Resource Production End
        
        //Unit Repair/Refuel Start
        
        for(Tank tank : GetTanks())
        {
            Player owner = GetPlayer(tank.GetOwnerID());
            
            if(owner != null)
            {
                //Recharge X amount per hour if the truck is not irradiated.
                //If the truck contains fuel, recharge fuel at a rate of 10000 fuel/tank.
                if(tank.GetCurrentFuel() < 1.0f && !GetRadioactive(tank, false) && (tank.GetMoveOrders() == MoveOrders.DEFEND || tank.GetMoveOrders() == MoveOrders.WAIT))
                {
                    float fltAmountToRefuel = 1.0f/Defs.HOURS_TO_FULL;
                    
                    ResourceSystem system = tank.GetResourceSystem();
                    
                    if(system.ContainsType(ResourceType.FUEL))
                    {
                        float fltFuelDeficit = tank.GetFuelDeficit();
                        
                        //See how much fuel it would take to totally recharge the deficit.
                        if(fltFuelDeficit > fltAmountToRefuel)
                        {
                            float fltDifference = fltFuelDeficit - fltAmountToRefuel;
                            
                            int lFuelNeeded = (int)(fltDifference/Defs.TANK_REFUEL_PER_KG);
                            
                            if(system.GetAmountOfType(ResourceType.FUEL) > lFuelNeeded)
                            {
                                //Take all the fuel necessary.
                                system.ChargeQuantity(ResourceType.FUEL, lFuelNeeded);
                                fltAmountToRefuel = tank.GetFuelDeficit();
                            }
                            else
                            {
                                //Not enough fuel. Take the amount that is present.
                                int lFuelPresent = (int)system.GetAmountOfType(ResourceType.FUEL);
                                system.ChargeQuantity(ResourceType.FUEL, system.GetAmountOfType(ResourceType.FUEL));
                                fltAmountToRefuel += Defs.TANK_REFUEL_PER_KG * lFuelPresent;
                            }
                        }
                    }
                    
                    tank.Refuel(fltAmountToRefuel);
                    EntityUpdated(tank, false);
                }
                
                if(tank.GetHP() < tank.GetMaxHP() && !GetRadioactive(tank, false))
                {
                    short nHPToHeal = (short)(tank.GetMaxHP()/Defs.HOURS_TO_FULL);
                    
                    ResourceSystem system = tank.GetResourceSystem();
                    
                    if(system.ContainsType(ResourceType.STEEL))
                    {
                        short nHPDeficit = tank.GetHPDeficit();
                        
                        if(nHPDeficit > nHPToHeal)
                        {
                            short nDifference = (short)(nHPDeficit - nHPToHeal);
                            
                            int lTypeNeeded = (int)(nDifference/Defs.TANK_REPAIR_PER_KG);
                            
                            if(system.GetAmountOfType(ResourceType.STEEL) > lTypeNeeded)
                            {
                                system.ChargeQuantity(ResourceType.STEEL, lTypeNeeded);
                                nHPToHeal = tank.GetHPDeficit();
                            }
                            else
                            {
                                int lTypePresent = (int)system.GetAmountOfType(ResourceType.STEEL);
                                system.ChargeQuantity(ResourceType.STEEL, system.GetAmountOfType(ResourceType.STEEL));
                                nHPToHeal += Defs.TANK_REPAIR_PER_KG * lTypePresent;
                            }
                        }
                    }
                    
                    tank.SetHP((short)(tank.GetHP() + nHPToHeal));
                    EntityUpdated(tank, false);
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            Player owner = GetPlayer(truck.GetOwnerID());
            
            if(owner != null)
            {
                //Recharge X amount per hour if the truck is not irradiated.
                //If the truck contains fuel, recharge fuel at a rate of 10000 fuel/tank.
                if(truck.GetCurrentFuel() < 1.0f && !GetRadioactive(truck, false) && (truck.GetMoveOrders() == MoveOrders.DEFEND || truck.GetMoveOrders() == MoveOrders.WAIT))
                {
                    float fltAmountToRefuel = 1.0f/Defs.HOURS_TO_FULL;
                    
                    ResourceSystem system = truck.GetResourceSystem();
                    
                    if(system.ContainsType(ResourceType.FUEL))
                    {
                        float fltFuelDeficit = truck.GetFuelDeficit();
                        
                        //See how much fuel it would take to totally recharge the deficit.
                        if(fltFuelDeficit > fltAmountToRefuel)
                        {
                            float fltDifference = fltFuelDeficit - fltAmountToRefuel;
                            
                            int lFuelNeeded = (int)(fltDifference/Defs.CARGO_TRUCK_REFUEL_PER_KG);
                            
                            if(system.GetAmountOfType(ResourceType.FUEL) > lFuelNeeded)
                            {
                                //Take all the fuel necessary.
                                system.ChargeQuantity(ResourceType.FUEL, lFuelNeeded);
                                fltAmountToRefuel = truck.GetFuelDeficit();
                            }
                            else
                            {
                                //Not enough fuel. Take the amount that is present.
                                int lFuelPresent = (int)system.GetAmountOfType(ResourceType.FUEL);
                                system.ChargeQuantity(ResourceType.FUEL, system.GetAmountOfType(ResourceType.FUEL));
                                fltAmountToRefuel += Defs.CARGO_TRUCK_REFUEL_PER_KG * lFuelPresent;
                            }
                        }
                    }
                    
                    truck.Refuel(fltAmountToRefuel);
                    EntityUpdated(truck, false);
                }
                
                if(truck.GetHP() < truck.GetMaxHP() && !GetRadioactive(truck, false))
                {
                    short nHPToHeal = (short)(truck.GetMaxHP()/Defs.HOURS_TO_FULL);
                    
                    ResourceSystem system = truck.GetResourceSystem();
                    
                    if(system.ContainsType(ResourceType.STEEL))
                    {
                        short nHPDeficit = truck.GetHPDeficit();
                        
                        if(nHPDeficit > nHPToHeal)
                        {
                            short nDifference = (short)(nHPDeficit - nHPToHeal);
                            
                            int lTypeNeeded = (int)(nDifference/Defs.CARGO_TRUCK_REPAIR_PER_KG);
                            
                            if(system.GetAmountOfType(ResourceType.STEEL) > lTypeNeeded)
                            {
                                system.ChargeQuantity(ResourceType.STEEL, lTypeNeeded);
                                nHPToHeal = truck.GetHPDeficit();
                            }
                            else
                            {
                                int lTypePresent = (int)system.GetAmountOfType(ResourceType.STEEL);
                                system.ChargeQuantity(ResourceType.STEEL, system.GetAmountOfType(ResourceType.STEEL));
                                nHPToHeal += Defs.CARGO_TRUCK_REPAIR_PER_KG * lTypePresent;
                            }
                        }
                    }
                    
                    truck.SetHP((short)(truck.GetHP() + nHPToHeal));
                    EntityUpdated(truck, false);
                }
            }
        }
        
        for(Infantry infantry : GetInfantries())
        {
            Player owner = GetPlayer(infantry.GetOwnerID());
            
            if(owner != null)
            {
                boolean bRadioactive = GetRadioactive(infantry, true);
                     
                if(bRadioactive)
                {
                    for(Radiation radiation : GetRadiations())
                    {
                        if(!GetInfantryIsSheltered(infantry) && infantry.GetPosition().DistanceTo(radiation.GetPosition()) <= radiation.GetRadius())
                        {
                            infantry.InflictDamage(Defs.RADIATION_DMG_HOUR);
                        }
                    }
                    
                    //Send out event if they are dead.
                    if(infantry.Destroyed())
                    {
                        CreateEvent(new LaunchEvent(String.format("%s's infantry died of radiation poisoning.", infantry.GetName()), SoundEffect.DEATH)); //TODO: get a custom sound for this.
                        CreateReport(owner, new LaunchReport("Your infantry died of radiation poisoning!", true, infantry.GetOwnerID()));
                    }
                }
                //Recharge X amount per hour if the truck is not irradiated.
                //If the truck contains fuel, recharge fuel at a rate of 10000 fuel/tank.
                if(infantry.GetCurrentFuel() < 1.0f && !bRadioactive && (infantry.GetMoveOrders() == MoveOrders.DEFEND || infantry.GetMoveOrders() == MoveOrders.WAIT))
                {
                    float fltAmountToRefuel = 1.0f/Defs.HOURS_TO_FULL;
                    
                    ResourceSystem system = infantry.GetResourceSystem();
                    ResourceType type = ResourceType.FOOD;
                    
                    if(system.ContainsType(type))
                    {
                        float fltFuelDeficit = infantry.GetFuelDeficit();
                        
                        if(fltFuelDeficit > fltAmountToRefuel)
                        {
                            float fltDifference = fltFuelDeficit - fltAmountToRefuel;
                            
                            int lFuelNeeded = (int)(fltDifference/10000);
                            
                            if(system.GetAmountOfType(type) > lFuelNeeded)
                            {
                                //Take all the fuel necessary.
                                system.ChargeQuantity(type, lFuelNeeded);
                                fltAmountToRefuel = infantry.GetFuelDeficit();
                            }
                            else
                            {
                                //Not enough fuel. Take the amount that is present.
                                int lFoodPresent = (int)system.GetAmountOfType(type);
                                system.ChargeQuantity(type, system.GetAmountOfType(type));
                                fltAmountToRefuel += 10000 * lFoodPresent;
                            }
                        }
                    }
                    
                    infantry.Refuel(fltAmountToRefuel);
                    EntityUpdated(infantry, false);
                }
                
                if(infantry.GetHP() < infantry.GetMaxHP() && !bRadioactive)
                {
                    short nHPToHeal = (short)(infantry.GetMaxHP()/Defs.HOURS_TO_FULL);
                    
                    ResourceSystem system = infantry.GetResourceSystem();
                    
                    if(system.ContainsType(ResourceType.MEDICINE))
                    {
                        short nHPDeficit = infantry.GetHPDeficit();
                        
                        if(nHPDeficit > nHPToHeal)
                        {
                            short nDifference = (short)(nHPDeficit - nHPToHeal);
                            
                            int lTypeNeeded = (int)(nDifference/10000);
                            
                            if(system.GetAmountOfType(ResourceType.MEDICINE) > lTypeNeeded)
                            {
                                system.ChargeQuantity(ResourceType.MEDICINE, lTypeNeeded);
                                nHPToHeal = infantry.GetHPDeficit();
                            }
                            else
                            {
                                int lMedicinePresent = (int)system.GetAmountOfType(ResourceType.MEDICINE);
                                system.ChargeQuantity(ResourceType.MEDICINE, system.GetAmountOfType(ResourceType.MEDICINE));
                                nHPToHeal += 10000 * lMedicinePresent;
                            }
                        }
                    }
                    
                    infantry.SetHP((short)(infantry.GetHP() + nHPToHeal));
                    EntityUpdated(infantry, false);
                }
            }
        }
        
        for(NavalVessel vessel : GetNavalVessels())
        {
            Player owner = GetPlayer(vessel.GetOwnerID());

            if(owner != null)
            {
                if(!GetRadioactive(vessel, true) && ShipInPort(vessel))
                {
                    if(!vessel.AtFullHealth())
                    {
                        vessel.AddHP((short)(vessel.GetMaxHP()/Defs.HOURS_TO_FULL));
                        EntityUpdated(vessel, false);
                    }

                    if(vessel.GetFuelDeficit() > 0 && !vessel.GetNuclear() && (vessel.GetMoveOrders() == MoveOrders.DEFEND || vessel.GetMoveOrders() == MoveOrders.WAIT))
                    {
                        vessel.AddFuel((vessel.GetMaxFuel()/Defs.HOURS_TO_FULL));
                        EntityUpdated(vessel, false);
                    }
                }
            }   
        }
        
        for(Airbase airbase : GetAirbases())
        {
            if(airbase.GetOnline() && !GetRadioactive(airbase, true))
            {
                for(StoredAirplane airplane : GetStoredAirplanes())
                {
                    Player aircraftOwner = GetOwner(airplane);
                    
                    if(!airplane.AtFullHealth())
                    {
                        if(aircraftOwner != null)
                        {
                            airplane.AddHP((short)(airplane.GetMaxHP()/Defs.HOURS_TO_FULL));
                            EntityUpdated(airplane, false);
                        }
                    }

                    if(airplane.GetFuelDeficit() > 0)
                    {
                        if(aircraftOwner != null)
                        {
                            airplane.Refuel((int)(airplane.GetMaxFuel()/Defs.HOURS_TO_FULL));
                            EntityUpdated(airplane, false);
                        }
                    }
                }
            }
        }
        
        for(Structure structure : GetAllStructures())
        {
            //If the structure is not at full HP, the owner is not null, and not in battle, give it some HP.
            if(!GetRadioactive(structure, true) && !structure.AtFullHealth())
            {
                Player owner = GetPlayer(structure.GetOwnerID());
                
                if(owner != null)
                {
                    if(structure.GetHP() < structure.GetMaxHP() && !GetRadioactive(structure, false))
                    {
                        short nHPToHeal = (short)(structure.GetMaxHP()/Defs.HOURS_TO_FULL);

                        ResourceSystem system = structure.GetResourceSystem();

                        if(system.ContainsType(ResourceType.CONSTRUCTION_SUPPLIES))
                        {
                            short nHPDeficit = structure.GetHPDeficit();

                            if(nHPDeficit > nHPToHeal)
                            {
                                short nDifference = (short)(nHPDeficit - nHPToHeal);

                                int lTypeNeeded = (int)(nDifference/Defs.STRUCTURE_REPAIR_PER_KG);

                                if(system.GetAmountOfType(ResourceType.CONSTRUCTION_SUPPLIES) > lTypeNeeded)
                                {
                                    system.ChargeQuantity(ResourceType.CONSTRUCTION_SUPPLIES, lTypeNeeded);
                                    nHPToHeal = structure.GetHPDeficit();
                                }
                                else
                                {
                                    int lTypePresent = (int)system.GetAmountOfType(ResourceType.CONSTRUCTION_SUPPLIES);
                                    system.ChargeQuantity(ResourceType.CONSTRUCTION_SUPPLIES, system.GetAmountOfType(ResourceType.CONSTRUCTION_SUPPLIES));
                                    nHPToHeal += Defs.STRUCTURE_REPAIR_PER_KG * lTypePresent;
                                }
                            }
                        }

                        structure.AddHP(nHPToHeal);
                        EntityUpdated(structure, false);
                    }
                }
            }
        }
        
        //Unit Repair/Refuel End
        
        for(Player player : GetPlayers())
        {
            //Player is alive.
            if(player.Functioning())
            {
                int lAmountToAdd = GetHourlyIncome(player) + GetPlayerRankIncome(player);

                if(lAmountToAdd > 0)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s gets %d.", player.GetName(), lAmountToAdd));
                    ProcessPlayerIncome(player, "hourly income", Map.ofEntries(entry(ResourceType.WEALTH, (long)lAmountToAdd)), true);
                    Scoring_StandardIncomeReceived(player, lAmountToAdd);
                }
                else
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s gets nothing.", player.GetName()));
                }
                
                if(player.UnderAttack())
                {
                    for(Player otherPlayer : GetPlayers())
                    {
                        if(player.GetID() != otherPlayer.GetID() && PlayerThreatensPlayer(player, otherPlayer))
                            player.AddHostilePlayer(otherPlayer.GetID());
                        else
                            player.RemoveHostilePlayer(otherPlayer.GetID());
                    }
                } 
                else
                {
                    player.ClearHostilePlayers();
                }
            }
        }
    }
    
    /**
     * To be called at the end of every day to process end of day events.
     */
    private void DayEnded()
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Processing end of day events...");
        
        //Remove old user accounts, and arm all of them for multiaccount detection.
        for(User user : Users.values())
        {
            if(user.GetExpiredOn() > 0)
            {
                if(user.GetBanState() == BanState.NOT ||
                   (System.currentTimeMillis() - user.GetExpiredOn() > Defs.MS_PER_QUARTER))
                    Users.remove(user.GetIdentityKey());
            }
            else
            {
                user.ArmMultiAccountDetection();
            }
        }
        
        for(Player player : GetPlayers())
        {
            //Give the player XP according to how much they traveled today.
            float fltDistanceTraveledToday = player.GetDistanceTraveledToday();
            int lTravelXP = (int)(fltDistanceTraveledToday/Defs.KM_TRAVELED_PER_XP);
            player.AddExperience(lTravelXP);
            player.ResetTravelToday();
            CreateReport(player, new LaunchReport(String.format("You traveled %.1f km today. You earned %d XP.", fltDistanceTraveledToday, lTravelXP), true, player.GetID()));
        
            SetPlayerOffenseValues(player);
            SetPlayerDefenseValues(player);
            SetPlayerNeutralValues(player);
        }
        
        CreateReport(new LaunchReport("Player offense/economic/defense values updated.", false));
        
        /*for(ShipType type : GetShipTypes())
        {
            long oCurrentTime = System.currentTimeMillis();
            long oLastPurchased = type.GetLastPurchased();
            long oDeleteThreshold = oLastPurchased + Defs.DESIGN_REMOVE_TIME;
            
            if(oDeleteThreshold < oCurrentTime)
            {
                ShipTypes.remove(type.GetID());
            }
        }
        
        for(SubmarineType type : GetSubmarineTypes())
        {
            if(type.GetLastPurchased() < System.currentTimeMillis() - (Defs.MS_PER_DAY * 300))
            {
                SubmarineTypes.remove(type.GetID());
            }
        }
        
        for(AircraftType type : GetAircraftTypes())
        {
            if(type.GetLastPurchased() < System.currentTimeMillis() - (Defs.MS_PER_DAY * 300))
            {
                AircraftTypes.remove(type.GetID());
            }
        }*/
        
        counterShield.resetDuplicationDaily();
    }

    /**
     * To be called at the end of every week to process weekly events.
     */
    private void WeekEnded()
    {
        //Free captives.
        for(Player player : GetPlayers())
        {
            if(player.GetPrisoner())
            {
                player.SetPrisoner(false);
                
                Alliance alliance = GetAlliance(player.GetAllianceMemberID());
                
                if(alliance != null)
                {
                    CreateReport(new LaunchReport(String.format("You have been freed from captivity to %s. You may leave the alliance as you wish.", alliance.GetName()), true, player.GetID()));
                    CreateEventForPlayer(new LaunchEvent(String.format("You have been freed from captivity to %s. You may leave the alliance as you wish.", alliance.GetName()), SoundEffect.LIBERATE), player.GetID());
                }
                
                EntityUpdated(player, false);
            }
        }
        
        //Conclude all wars.
        for(Treaty treaty : Treaties.values())
        {
            if(treaty instanceof War war)
            {
                ConcludeWar(war);
                Treaties.remove(war.GetID());
                TreatyRemoved(war);
            }
            else if(treaty instanceof Affiliation affiliation)
            {
                Treaties.remove(affiliation.GetID());
                TreatyRemoved(affiliation);
            }
        }
        
        CreateReport(new LaunchReport(String.format("All affiliations have been reset."), false));
            
        for(Alliance alliance : GetAlliances())
        {
            alliance.SetICBMCount(CountAllianceICBMs(alliance));
            alliance.SetABMCount(CountAllianceABMs(alliance));
        }
        
        CreateReport(new LaunchReport(String.format("Alliance ICBM and ABM counts updated."), false));
        
        //GenerateDeposits();
    }
    
    public void MonthEnded()
    {
        
    }
    
    private void ConcludeWar(War war)
    {
        Alliance alliance1 = Alliances.get(war.GetAllianceID1());
        Alliance alliance2 = Alliances.get(war.GetAllianceID2());
        
        Alliance winner = null;
        Alliance loser = null;
        
        if(war.GetWonFactors1() > war.GetWonFactors2())
        {
            winner = alliance1;
            loser = alliance2;
        }
        else if(war.GetWonFactors2() > war.GetWonFactors1())
        {
            winner = alliance2;
            loser = alliance1;
        }
        else
        {
            CreateReport(new LaunchReport(String.format("%s drew with %s. No money for any of you lot!", alliance1.GetName(), alliance2.GetName()), true, alliance1.GetID(), alliance2.GetID(), true, true));
        }
        
        if(winner != null && loser != null)
        {
            float fltPrizeFund = (float)war.GetTotalSpending() * Defs.WAR_REWARD_FACTOR;
            List<Player> Winners = GetMembers(winner);
            
            if(!Winners.isEmpty())
            {
                int lPrizePerPlayer = (int)((fltPrizeFund / (float)Winners.size()) + 0.5f);
                int lXPPrizePerPlayer = (int)(lPrizePerPlayer * Defs.WAR_WON_XP_PER_WEALTH);
                
                CreateReport(new LaunchReport(String.format("%s won the war against %s! Each player wins £%d and %d XP.", winner.GetName(), loser.GetName(), lPrizePerPlayer, lXPPrizePerPlayer), true, winner.GetID(), loser.GetID(), true, true));
                
                for(Player player : Winners)
                {
                    player.AddWealth(lPrizePerPlayer);
                    ProcessPlayerXPGain(player.GetID(), lXPPrizePerPlayer, String.format("Your alliance won the war against %s!", loser.GetName()));
                }
            }
        }
    }
    
    private void ProcessTorpedoes(int lMS)
    {
        for(Torpedo torpedo : GetTorpedoes())
        {
            TorpedoType type = config.GetTorpedoType(torpedo.GetType());
            float fltSpeed = type.GetTorpedoSpeed();
            float fltDistance = (fltSpeed/Defs.MS_PER_HOUR) * lMS;
            torpedo.Traveled(fltDistance);
            boolean bOnWater = TerrainChecker.CoordinateIsWater(torpedo.GetPosition());
            
            //If on water or out of range, explode. 
            if(!bOnWater || torpedo.OutOfRange())
                torpedo.SetDetonate();
            
            switch(torpedo.GetState())
            {
                case TRAVELLING:
                {
                    //If we reach the target, go to seeking.
                    if(torpedo.GetGeoTarget() != null)
                    {
                        if(torpedo.GetPosition().DistanceTo(torpedo.GetGeoTarget()) <= Defs.REACHED_GEOTARGET_DISTANCE)
                        {
                            if(torpedo.GetHoming())
                                torpedo.ReachedGeoTarget();
                            else
                                torpedo.SetDetonate();
                        }
                    }
                }
                break;
                
                case SEEKING:
                {
                    //For ships and submarines within Defs.TORPEDO_HOMING_RADIUS, if random float yadda yadda yadda, set target.
                    List<NavalVessel> NavalVessels = new ArrayList<>();
                    NavalVessels.addAll(GetShips());
                    NavalVessels.addAll(GetSubmarines());
                    List<NavalVessel> TargetCandidates = new ArrayList<>();

                    for(NavalVessel vessel : NavalVessels)
                    {
                        if(vessel.GetPosition().DistanceTo(torpedo.GetPosition()) <= Defs.TORPEDO_HOMING_RANGE)
                        {
                            TargetCandidates.add(vessel);
                        }
                    }
                    
                    if(!TargetCandidates.isEmpty())
                    {
                        NavalVessel target = null;
                        
                        for(NavalVessel vessel : TargetCandidates)
                        {
                            //Seek the closest vessel.
                            if(target == null || vessel.GetPosition().DistanceTo(torpedo.GetPosition()) < target.GetPosition().DistanceTo(torpedo.GetPosition()))
                            {
                                target = vessel;
                            }
                        }
                        
                        if(target != null)
                        {
                            if(random.nextFloat() < Defs.TORPEDO_HOMING_CHANCE)
                            {
                                //TODO: Alert the ship owner and the torpedo owner about the vessel being targeted.
                                torpedo.TargetLocated(target.GetPointer());
                                break;
                            }
                        }
                    }
                }
                break;
                
                case HOMING:
                {
                    //Move towards the target. If we are within X distance of the target, SetDetonate.
                    MapEntity target = torpedo.GetTarget().GetMapEntity(this);
                    
                    
                    if(target != null)
                    {
                        float fltDistanceToTarget = torpedo.GetPosition().DistanceTo(target.GetPosition());
                        
                        if(random.nextFloat() <= Defs.TORPEDO_LOSE_TARGET_CHANCE || fltDistanceToTarget > Defs.TORPEDO_HOMING_RANGE)
                        {
                            torpedo.LostTarget();
                        }
                        else if(fltDistanceToTarget <= Defs.TORPEDO_DETONATE_DISTANCE)
                        {
                            torpedo.SetDetonate();
                        }
                    }
                    else
                    {
                        torpedo.LostTarget();
                    }
                }
                break;
                
                case DETONATE:
                {
                    //Nothing. This gets handled in LaunchGame.GameTick so that the torpedo is removed from both the server and the client simultaneously.
                }
                break;
            }
        }
    }
    
    private void ProcessSentries()
    {
        //Process sentry guns.
        for(SentryGun sentryGun : GetSentryGuns())
        {
            if(sentryGun.GetSelling() && sentryGun.GetStateTimeExpired())
            {
                Player owner = Players.get(sentryGun.GetOwnerID());

                if(owner != null)
                {
                    ProcessPlayerIncome(owner, String.format("sale of %s", sentryGun.GetTypeName()), GetSaleValue(sentryGun), false);
                }

                SentryGuns.remove(sentryGun.GetID());
                EntityRemoved(sentryGun, false);
            }
            else if(sentryGun.GetOnline() && sentryGun.GetCanFire() && !sentryGun.GetRespawnProtected())
            {
                if(sentryGun.GetCanFire() && !sentryGun.GetIsWatchTower())
                {
                    Player sentryGunOwner = Players.get(sentryGun.GetOwnerID());

                    if(sentryGunOwner.UnderAttack())
                    {
                        for(EntityPointer pointer : quadtree.GetAffectedMissiles(sentryGun.GetPosition(), Defs.SENTRY_RANGE))
                        {
                            if(pointer != null)
                            {
                                Missile missile = pointer.GetMissile(game);

                                if(missile != null)
                                {
                                    if(missile.Flying() && !missile.GetDetonate())
                                    {
                                        if(!sentryGunOwner.GetAWOL() && !sentryGunOwner.GetBanned_Server() && config.GetMissileType(missile.GetType()) != null)
                                        {
                                            //Don't engage if the owner is AWOL or banned.
                                            if(!config.GetMissileType(missile.GetType()).GetICBM())
                                            {
                                                if(sentryGun.GetOwnerID() != missile.GetOwnerID())
                                                {
                                                    float fltDistance = sentryGun.GetPosition().DistanceTo(missile.GetPosition());

                                                    if(fltDistance <= Defs.SENTRY_RANGE)
                                                    {
                                                        Player missileOwner = Players.get(missile.GetOwnerID());

                                                        //Don't shoot friendly missiles going over the top; but do engage them if they're threatening the player (closes fire - affiliate - undefended loophole).
                                                        if(!WouldBeFriendlyFire(sentryGunOwner, missileOwner) || ThreatensPlayerOptimised(missile, sentryGunOwner, GetMissileTarget(missile), config.GetMissileType(missile.GetType())))
                                                        {
                                                            //Brrrrp!
                                                            //Decide if the missile got shot down, while applying ECM/Stealth.
                                                            sentryGun.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                            float sentryhitchance;

                                                            if(config.GetMissileType(missile.GetType()).GetECM())
                                                            {
                                                                sentryhitchance = config.GetSentryGunHitChance() - config.GetECMInterceptorChanceReduction(); 
                                                            }
                                                            else
                                                            {
                                                                sentryhitchance = config.GetSentryGunHitChance();
                                                            }

                                                            //If the player has been offline for atleast 30 minutes, give their sentries a hit chance buff.
                                                            if(sentryGunOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                                                                sentryhitchance = Defs.OFFLINE_PLAYER_SENTRY_ACCURACY;

                                                            if(random.nextFloat() < (sentryhitchance < Defs.MINIMUM_SENTRY_ACCURACY ? Defs.MINIMUM_SENTRY_ACCURACY : sentryhitchance))
                                                            {                  
                                                                //Don't allow air-dropped bombs to detonate, as this allows aircraft to do point-blank sneak attacks with impunity and guarantees success. Also give nukes a chance of exploding, but not every time.
                                                                /*if(!config.GetMissileType(missile.GetType()).GetBomb() && (!config.GetMissileType(missile.GetType()).GetNuclear() || random.nextFloat() < Defs.SENTRY_DETONATE_NUKE_CHANCE))
                                                                {
                                                                    missile.Detonate();
                                                                }
                                                                else
                                                                {
                                                                    missile.Destroy();
                                                                }*/

                                                                missile.Destroy();

                                                                EntityUpdated(missile, false);

                                                                CreateEvent(new LaunchEvent(String.format("%s's sentry gun shot down %s's missile.", sentryGunOwner.GetName(), missileOwner.GetName(), fltDistance), SoundEffect.SENTRY_GUN_HIT));
                                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your sentry gun shot down %s's missile.", missileOwner.GetName()), false, sentryGunOwner.GetID(), missileOwner.GetID()));
                                                                CreateReport(missileOwner, new LaunchReport(String.format("%s's sentry gun shot down your missile.", sentryGunOwner.GetName()), false, missileOwner.GetID(), sentryGunOwner.GetID()));
                                                            }
                                                            else
                                                            {
                                                                CreateEvent(new LaunchEvent(String.format("%s's sentry gun missed %s's missile.", sentryGunOwner.GetName(), missileOwner.GetName()), SoundEffect.SENTRY_GUN_MISS));
                                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your sentry gun missed %s's missile.", missileOwner.GetName()), false, sentryGunOwner.GetID(), missileOwner.GetID()));
                                                                CreateReport(missileOwner, new LaunchReport(String.format("%s's sentry gun missed your missile.", sentryGunOwner.GetName()), false, missileOwner.GetID(), sentryGunOwner.GetID()));
                                                            }

                                                            sentryGun.SetReloadTime(Defs.SENTRY_RELOAD_TIME);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    quadtree.RemoveEntity(pointer);
                                }
                            } 
                        }
                    }    
                }
                
                if(sentryGun.GetCanFire() && !sentryGun.GetIsWatchTower())
                {
                    Player sentryGunOwner = Players.get(sentryGun.GetOwnerID());

                    //Don't engage if the owner is AWOL or banned.
                    if(!sentryGunOwner.GetAWOL() && !sentryGunOwner.GetBanned_Server())
                    {
                        for(EntityPointer pointer : quadtree.GetAffectedAircrafts(sentryGun.GetPosition(), Defs.SENTRY_RANGE))
                        {
                            Airplane aircraft = pointer.GetAircraft(game);

                            if(aircraft != null)
                            {
                                if(!aircraft.Destroyed() && sentryGun.GetPosition().RadiusPhaseCollisionTest(aircraft.GetPosition(), Defs.SENTRY_RANGE) && sentryGun.GetPosition().DistanceTo(aircraft.GetPosition()) <= Defs.SENTRY_RANGE)
                                {
                                    Player bomberOwner = Players.get(aircraft.GetOwnerID());

                                    //Don't shoot friendly missiles going over the top; but do engage them if they're threatening the player (closes fire - affiliate - undefended loophole).
                                    if(!WouldBeFriendlyFire(sentryGunOwner, bomberOwner))
                                    {
                                        aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                        float fltHitChance = Defs.SENTRY_GUN_ACCURACY;

                                        //If the player has been offline for atleast 30 minutes, give their sentries a hit chance buff.
                                        if(sentryGunOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                                            fltHitChance = Defs.OFFLINE_PLAYER_SENTRY_ACCURACY;

                                        if(random.nextFloat() < fltHitChance)
                                        {          
                                            //TODO: scoring and experience.
                                            short nDamageInflicted = aircraft.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.SENTRY_GUN_MIN_DMG, Defs.SENTRY_GUN_MAX_DMG));

                                            Scoring_DamageInflicted(sentryGunOwner, bomberOwner, nDamageInflicted); 
                                            ProcessPlayerXPGain(sentryGunOwner.GetID(), Defs.AIRCRAFT_KILLED_XP, "You destroyed an aircraft.");
                                            ProcessPlayerXPLoss(bomberOwner.GetID(), Defs.AIRCRAFT_LOST_XP, "Your aircraft was destroyed.");

                                            if(aircraft.Destroyed())
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s's sentry gun shot down %s's %s.", sentryGunOwner.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your sentry gun shot down %s's %s.", bomberOwner.GetName(), aircraft.GetTypeName()), false, sentryGunOwner.GetID(), bomberOwner.GetID()));
                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's sentry gun shot down your %s.", sentryGunOwner.GetName(), aircraft.GetTypeName()), false, bomberOwner.GetID(), sentryGunOwner.GetID()));
                                                CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));
                                            }
                                            else
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s's damaged %s's aircraft.", sentryGunOwner.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your sentry gun damaged %s's %s.", bomberOwner.GetName(), aircraft.GetTypeName()), false, sentryGunOwner.GetID(), bomberOwner.GetID()));
                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's sentry gun damaged your %s.", sentryGunOwner.GetName(), aircraft.GetTypeName()), false, bomberOwner.GetID(), sentryGunOwner.GetID()));
                                            } 
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's sentry gun missed %s's %s.", sentryGunOwner.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_MISS));
                                        }

                                        sentryGun.SetReloadTime(Defs.SENTRY_RELOAD_TIME);
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                quadtree.RemoveEntity(pointer);
                            }   
                        } 
                    }
                }
                
                if(sentryGun.GetCanFire() && sentryGun.GetIsWatchTower())
                {
                    Player sentryGunOwner = Players.get(sentryGun.GetOwnerID());

                    //Don't engage if the owner is AWOL or banned.
                    if(!sentryGunOwner.GetAWOL() && !sentryGunOwner.GetBanned_Server())
                    {
                        for(EntityPointer pointer : quadtree.GetAffectedLandUnits(sentryGun.GetPosition(), Defs.ARTILLERY_RANGE))
                        {
                            LandUnit landUnit = pointer.GetLandUnit(game);

                            if(landUnit != null)
                            {
                                if(!landUnit.Destroyed() && sentryGun.GetPosition().RadiusPhaseCollisionTest(landUnit.GetPosition(), Defs.SENTRY_RANGE) && sentryGun.GetPosition().DistanceTo(landUnit.GetPosition()) <= Defs.SENTRY_RANGE)
                                {
                                    Player bomberOwner = Players.get(landUnit.GetOwnerID());

                                    //Don't shoot friendly missiles going over the top; but do engage them if they're threatening the player (closes fire - affiliate - undefended loophole).
                                    if(!WouldBeFriendlyFire(sentryGunOwner, bomberOwner))
                                    {
                                        landUnit.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                        float fltHitChance = Defs.SENTRY_GUN_ACCURACY;

                                        //If the player has been offline for atleast 30 minutes, give their sentries a hit chance buff.
                                        if(sentryGunOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                                            fltHitChance = Defs.OFFLINE_PLAYER_SENTRY_ACCURACY;

                                        if(random.nextFloat() < fltHitChance)
                                        {          
                                            //TODO: scoring and experience.
                                            short nDamageInflicted = landUnit.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.SENTRY_GUN_MIN_DMG, Defs.SENTRY_GUN_MAX_DMG));

                                            Scoring_DamageInflicted(sentryGunOwner, bomberOwner, nDamageInflicted); 
                                            ProcessPlayerXPGain(sentryGunOwner.GetID(), Defs.GetXPGainForKill(landUnit), String.format("You destroyed a %s.", landUnit.GetTypeName()));
                                            ProcessPlayerXPLoss(bomberOwner.GetID(), Defs.GetXPLossForKill(landUnit), String.format("Your %s was destroyed.", landUnit.GetTypeName()));

                                            if(landUnit.Destroyed())
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s's %s destroyed %s's %s.", sentryGunOwner.GetName(), sentryGun.GetTypeName(), bomberOwner.GetName(), landUnit.GetTypeName()), SoundEffect.ARTILLERY_FIRE));
                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your %s destroyed %s's %s.", bomberOwner.GetName(), sentryGun.GetTypeName(), landUnit.GetTypeName()), false, sentryGunOwner.GetID(), bomberOwner.GetID()));
                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's %s destroyed your %s.", sentryGunOwner.GetName(), sentryGun.GetTypeName(), landUnit.GetTypeName()), false, bomberOwner.GetID(), sentryGunOwner.GetID()));
                                                CreateSalvage(landUnit.GetPosition(), GetLandUnitValue(landUnit));
                                            }
                                            else
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s's %s damaged %s's %s.", sentryGunOwner.GetName(), sentryGun.GetTypeName(), bomberOwner.GetName(), landUnit.GetTypeName()), SoundEffect.ARTILLERY_FIRE));
                                                CreateReport(sentryGunOwner, new LaunchReport(String.format("Your %s damaged %s's %s.", bomberOwner.GetName(), sentryGun.GetTypeName(), landUnit.GetTypeName()), false, sentryGunOwner.GetID(), bomberOwner.GetID()));
                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's %s damaged your %s.", sentryGunOwner.GetName(), sentryGun.GetTypeName(), landUnit.GetTypeName()), false, bomberOwner.GetID(), sentryGunOwner.GetID()));
                                            } 
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's %s missed %s's %s.", sentryGunOwner.GetName(), sentryGun.GetTypeName(), bomberOwner.GetName(), landUnit.GetTypeName()), SoundEffect.SENTRY_GUN_MISS));
                                        }

                                        sentryGun.SetReloadTime(Defs.ARTILLERY_GUN_RELOAD_TIME);
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                quadtree.RemoveEntity(pointer);
                            }   
                        } 
                    }
                }
            }
        }  
    }
    
    private void ProcessAircraft(int lMS)
    {
        //Process aircraft.
        for(Airplane aircraft : GetAirplanes())
        {
            if(!aircraft.Destroyed())
            {
                MapEntity homebase = aircraft.GetHomeBase().GetMapEntity(this);
                
                if(homebase != null)
                {
                    if(aircraft.WillAutoReturn())
                    {
                        GeoCoord geoPosition = aircraft.GetPosition();
                        GeoCoord geoHome = homebase.GetPosition();

                        if(geoPosition.DistanceTo(geoHome) > GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetEntityType())) * 0.9f && geoPosition.DistanceTo(geoHome) < GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetFuelUsagePerTick(lMS, Defs.GetAircraftRange(aircraft.GetEntityType()), Defs.GetAircraftSpeed(aircraft.GetEntityType()))))
                            aircraft.ReturnToBase();
                    }
                }
                else
                {
                    AttemptToFindNewHome(aircraft);
                }

                if(aircraft.OutOfFuel())
                {
                    aircraft.InflictDamage(aircraft.GetHP());
                    CreateEvent(new LaunchEvent(String.format("%s's %s ran out of fuel and crashed.", GetPlayer(aircraft.GetOwnerID()).GetName(), aircraft.GetTypeName()), LaunchEvent.SoundEffect.INTERCEPTOR_HIT));
                }

                if(aircraft.GetMoveOrders() == MoveOrders.RETURN)
                {
                    if(homebase != null && !aircraft.Orphaned())
                    {
                        if(aircraft.GetPosition().BroadPhaseCollisionTest(homebase.GetPosition()) && aircraft.GetPosition().DistanceTo(homebase.GetPosition()) < Defs.AIRCRAFT_LANDING_DISTANCE)
                        {
                            AircraftSystem system = null;
                            boolean bSystemOnline = true;
                            
                            if(homebase instanceof Airbase airbase)
                            {
                                system = airbase.GetAircraftSystem();
                                bSystemOnline = airbase.GetOnline();
                            }
                            else if(homebase instanceof Ship)
                            {
                                system = ((Ship)homebase).GetAircraftSystem();
                            }
                            
                            if(system != null && !system.Full() && bSystemOnline)
                            {
                                LandAircraft(aircraft.GetPointer(), system);
                            }  
                        }
                    }
                }
                else if(aircraft.GetMoveOrders() == MoveOrders.ATTACK)
                {                
                    /**
                     * If the aircraft has a geoTarget, use missiles.
                     * If it has a structure, naval vessel, land unit, or capturable
                     *      If the aircraft has missiles, use those. Otherwise, use ground attack.
                     * If it has a missile, airplane, or helicopter, run the current use interceptor code.
                     */
                    
                    boolean bGroundTarget = false;
                    
                    if(aircraft.GetTarget() != null)
                    {
                        MapEntity target = aircraft.GetTarget().GetMapEntity(this);
                        
                        if(target instanceof LandUnit || target instanceof Structure || target instanceof Capturable || target instanceof NavalVessel)
                        {
                            bGroundTarget = true;
                            
                            if(target instanceof Damagable)
                            {
                                if(((Damagable)target).Destroyed())
                                {
                                    aircraft.Wait();
                                    EntityUpdated(aircraft, true);
                                    CreateEventForPlayer(new LaunchEvent(String.format("Target destroyed. %s standing down...", aircraft.GetTypeName())), aircraft.GetOwnerID());
                                    break;
                                }
                            }
                        }
                    }
                    
                    if(bGroundTarget)
                    {
                        if(aircraft.GetTarget() != null)
                        {                       
                            if(aircraft.GroundAttack())
                            {
                                if(aircraft.CannonReady())
                                {
                                    if(aircraft.GetTarget() != null && aircraft.GetTarget().GetMapEntity(this) != null)
                                    {
                                        MapEntity target = aircraft.GetTarget().GetMapEntity(this);

                                        if(aircraft.GetPosition().DistanceTo(target.GetPosition()) < Defs.AIRCRAFT_CANNON_RANGE)
                                        {
                                            if(target instanceof Damagable)
                                            {
                                                Damagable targetDamagable = (Damagable)target;
                                                aircraft.FireCannon();
                                                aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                target.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                Player targetOwner = GetOwner(target);

                                                short nDamageInflicted = (short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG);

                                                Player player = GetOwner(aircraft);

                                                targetDamagable.InflictDamage(nDamageInflicted);
                                                
                                                if(targetOwner != null)
                                                {
                                                    targetOwner.AddHostilePlayer(aircraft.GetOwnerID());
                                                    Scoring_DamageInflicted(player, targetOwner, nDamageInflicted); 
                                                }

                                                if(targetDamagable instanceof LandUnit)
                                                {
                                                    ((LandUnit)targetDamagable).SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                                                }

                                                if(targetDamagable.Destroyed())
                                                {
                                                    if(targetDamagable instanceof Structure)
                                                    {
                                                        Structure targetStructure = (Structure)targetDamagable;

                                                        CreateSalvage(targetStructure.GetPosition().GetCopy(), GetSaleValue(targetStructure));
                                                        CreateRubble(targetStructure);
                                                    }
                                                    else if(targetDamagable instanceof CargoTruck)
                                                    {
                                                        CargoTruck targetTruck = (CargoTruck)targetDamagable;

                                                        CargoSystemDestroyed(targetTruck.GetOwnerID(), targetTruck, targetTruck.GetPosition(), targetTruck.GetCargoSystem(), targetTruck.GetResourceSystem());
                                                    }

                                                    aircraft.MoveToPosition(aircraft.GetGeoTarget());

                                                    ProcessPlayerXPGain(player.GetID(), Defs.GetXPGainForKill(target), String.format("You destroyed a %s.", targetDamagable.GetTypeName()));
                                                    
                                                    if(targetOwner != null)
                                                    {
                                                        ProcessPlayerXPLoss(targetOwner.GetID(), Defs.GetXPLossForKill(target), String.format("Your %s was destroyed.", targetDamagable.GetTypeName()));

                                                        CreateEvent(new LaunchEvent(String.format("%s's aircraft destroyed %s's %s.", player.GetName(), targetOwner.GetName(), targetDamagable.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                        CreateReport(player, new LaunchReport(String.format("Your aircraft destroyed %s's %s.", targetOwner.GetName(), targetDamagable.GetTypeName()), false, player.GetID(), targetOwner.GetID()));
                                                        CreateReport(targetOwner, new LaunchReport(String.format("%s's aircraft destroyed your %s.", player.GetName(), targetDamagable.GetTypeName()), false, targetOwner.GetID(), player.GetID()));
                                                    }
                                                    else
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's aircraft destroyed a %s.", player.GetName(), targetDamagable.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                        CreateReport(player, new LaunchReport(String.format("Your aircraft destroyed a %s.", targetDamagable.GetTypeName()), false, player.GetID()));
                                                    }
                                                }
                                                else
                                                {
                                                    if(targetOwner != null)
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's aircraft attacked %s's %s, causing %d HP of damage.", player.GetName(), targetOwner.GetName(), targetDamagable.GetTypeName(), nDamageInflicted), SoundEffect.SENTRY_GUN_HIT));
                                                        CreateReport(targetOwner, new LaunchReport(String.format("%s damaged your %s!", player.GetName(), targetDamagable.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                                        CreateReport(player, new LaunchReport(String.format("You damaged %s's %s!", targetOwner.GetName(), targetDamagable.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                                    }
                                                    else
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's aircraft attacked a %s, causing %d HP of damage.", player.GetName(), targetDamagable.GetTypeName(), nDamageInflicted), SoundEffect.SENTRY_GUN_HIT));
                                                        CreateReport(player, new LaunchReport(String.format("You damaged a %s!", targetDamagable.GetTypeName()), true, player.GetID()));
                                                    }
                                                }
                                            }
                                        }
                                    }  
                                } 
                            }
                        }
                    }
                    else
                    {
                        if(aircraft.GetTarget() != null)
                        {
                            MapEntity target = aircraft.GetTarget().GetMapEntity(this);

                            if(target != null)
                            {
                                boolean bUseInterceptors = aircraft.HasInterceptors() && aircraft.GetInterceptorSystem().GetOccupiedSlotCount() > 0 && (target instanceof Missile || target instanceof Airplane);

                                if(bUseInterceptors)
                                {
                                    float fltInterceptorRange = config.GetLongestInterceptorRange(aircraft.GetInterceptorSystem());
                                    float fltDistance = aircraft.GetPosition().DistanceTo(target.GetPosition());

                                    if(fltDistance < fltInterceptorRange)
                                    {
                                        if(target instanceof Airplane && (!((Airplane)target).GetStealth() || fltDistance <= Defs.STEALTH_ENGAGEMENT_DISTANCE))
                                        {
                                            Airplane targetAircraft = ((Airplane)target);

                                            if(!AircraftThreatChecked(targetAircraft, GetOwner(aircraft)))
                                            {
                                                MissileSystem interceptorSystem = aircraft.GetInterceptorSystem();

                                                if(interceptorSystem.ReadyToFire())
                                                {
                                                    for(Entry<Integer, Integer> entry : interceptorSystem.GetSlotTypes().entrySet())
                                                    {
                                                        if(interceptorSystem.GetSlotReadyToFire(entry.getKey()))
                                                        {
                                                            int lType = entry.getValue();
                                                            InterceptorType interceptorType = config.GetInterceptorType(lType);

                                                            if(interceptorType.GetInterceptorRange() > fltDistance)
                                                            {
                                                                for(int lSlotNumber = 0; lSlotNumber < interceptorSystem.GetSlotCount(); lSlotNumber++)
                                                                {
                                                                    if(interceptorSystem.GetSlotHasMissile(lSlotNumber) && (interceptorSystem.GetSlotMissileType(lSlotNumber) == lType))
                                                                    {
                                                                        //Fire.
                                                                        InterceptorType type = config.GetInterceptorType(lType);
                                                                        Player player = GetOwner(aircraft);
                                                                        Player bomberOwner = Players.get(targetAircraft.GetOwnerID());
                                                                        interceptorSystem.Fire(lSlotNumber);
                                                                        aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                                        CreateInterceptorLaunch(aircraft.GetPosition().GetCopy(), lType, player.GetID(), targetAircraft.GetID(), false, EntityType.AIRPLANE, aircraft.GetID());
                                                                        SendUserAlert(bomberOwner.GetUser(), "Interceptor Inbound", String.format("%s's SAM fired at your %s!", player.GetName(), aircraft.GetTypeName()), true, false);
                                                                        EntityUpdated(aircraft, true);

                                                                        bomberOwner.AddHostilePlayer(aircraft.GetOwnerID());
                                                                        CreateEvent(new LaunchEvent(String.format("%s's aircraft launched %s at %s's %s.", player.GetName(), type.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));
                                                                        CreateReport(player, new LaunchReport(String.format("Your aircraft engaged %s's %s.", bomberOwner.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));
                                                                        CreateReport(bomberOwner, new LaunchReport(String.format("%s's aircraft engaged your %s.", player.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));

                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                } 
                                            }
                                        }
                                        else if(target instanceof Missile)
                                        {
                                            Missile targetMissile = ((Missile)target);

                                            boolean bThreatUnchecked = true;

                                            for(Interceptor interceptor : new ArrayList<>(GetInterceptors()))
                                            {
                                                if(interceptor.GetTargetType() == EntityType.MISSILE && interceptor.GetTargetID() == targetMissile.GetID())
                                                {
                                                    bThreatUnchecked = false;
                                                }
                                            }

                                            if(bThreatUnchecked)
                                            {
                                                MissileSystem interceptorSystem = aircraft.GetInterceptorSystem();

                                                if(interceptorSystem.ReadyToFire())
                                                {
                                                    GeoCoord geoTarget = GetMissileTarget(targetMissile);
                                                    InterceptorType candidateType = null;
                                                    int lCandidateTimeToIntercept = Integer.MAX_VALUE;
                                                    MissileType missileType = config.GetMissileType(targetMissile.GetType());
                                                    float fltFighterToMissileDistance = aircraft.GetPosition().DistanceTo(targetMissile.GetPosition());
                                                    float fltMissileSpeed = targetMissile.GetSpeed();
                                                    int lMissileTimeToTarget = GetTimeToTarget(targetMissile);

                                                    for(Entry<Integer, Integer> entry : interceptorSystem.GetSlotTypes().entrySet())
                                                    {
                                                        if(missileType != null && interceptorSystem.GetSlotReadyToFire(entry.getKey()))
                                                        {
                                                            int lType = entry.getValue();
                                                            InterceptorType interceptorType = config.GetInterceptorType(lType);

                                                            float fltInterceptorSpeed = config.GetInterceptorSpeed(interceptorType);

                                                            //Is the missile within this interceptor's range?
                                                            if(fltFighterToMissileDistance < config.GetInterceptorRange(interceptorType) && (!missileType.GetStealth() || fltFighterToMissileDistance <= Defs.STEALTH_ENGAGEMENT_DISTANCE))
                                                            {                                                                                          
                                                                if(interceptorType.GetABM() && missileType.GetICBM() || !interceptorType.GetABM() && !missileType.GetICBM())
                                                                {
                                                                    //Is the interceptor fast enough?
                                                                    if(fltInterceptorSpeed > fltMissileSpeed)
                                                                    {
                                                                        if(candidateType != null)
                                                                        {
                                                                            //We already have a candidate. Is this cheaper or nearer?
                                                                            if(interceptorType.GetCost() < candidateType.GetCost())
                                                                            {
                                                                                //Cheaper. Accept if it can prosecute the missile.
                                                                                GeoCoord geoIntercept = targetMissile.GetPosition().InterceptPoint(geoTarget, fltMissileSpeed, aircraft.GetPosition(), fltInterceptorSpeed);
                                                                                int lTimeToIntercept = GetTimeToTarget(aircraft.GetPosition(), geoIntercept, fltInterceptorSpeed);

                                                                                if(lTimeToIntercept < lMissileTimeToTarget)
                                                                                {
                                                                                    candidateType = interceptorType;
                                                                                    lCandidateTimeToIntercept = lTimeToIntercept;
                                                                                }
                                                                            }
                                                                            else if(interceptorType.GetCost() == candidateType.GetCost())
                                                                            {
                                                                                //The same price. Accept if it can get there quicker.
                                                                                GeoCoord geoIntercept = targetMissile.GetPosition().InterceptPoint(geoTarget, fltMissileSpeed, aircraft.GetPosition(), fltInterceptorSpeed);
                                                                                int lTimeToIntercept = GetTimeToTarget(aircraft.GetPosition(), geoIntercept, fltInterceptorSpeed);

                                                                                if(lCandidateTimeToIntercept < lTimeToIntercept)
                                                                                {
                                                                                    candidateType = interceptorType;
                                                                                    lCandidateTimeToIntercept = lTimeToIntercept;
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            //No current candidate. This is good enough if it can reach.
                                                                            GeoCoord geoIntercept = targetMissile.GetPosition().InterceptPoint(geoTarget, fltMissileSpeed, aircraft.GetPosition(), fltInterceptorSpeed);
                                                                            int lTimeToIntercept = GetTimeToTarget(aircraft.GetPosition(), geoIntercept, fltInterceptorSpeed);

                                                                            if(lTimeToIntercept < lMissileTimeToTarget)
                                                                            {
                                                                                candidateType = interceptorType;
                                                                                lCandidateTimeToIntercept = lTimeToIntercept;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if(candidateType != null)
                                                    {
                                                        for(int lSlotNumber = 0; lSlotNumber < interceptorSystem.GetSlotCount(); lSlotNumber++)
                                                        {
                                                            if(interceptorSystem.GetSlotHasMissile(lSlotNumber) && (interceptorSystem.GetSlotMissileType(lSlotNumber) == candidateType.GetID()))
                                                            {
                                                                //Fire.
                                                                Player player = GetOwner(aircraft);
                                                                Player targetOwner = Players.get(targetMissile.GetOwnerID());
                                                                interceptorSystem.Fire(lSlotNumber);
                                                                aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                                CreateInterceptorLaunch(aircraft.GetPosition().GetCopy(), candidateType.GetID(), player.GetID(), targetMissile.GetID(), false, EntityType.MISSILE, aircraft.GetID());

                                                                CreateEvent(new LaunchEvent(String.format("%s's aircraft launched %s at %s's %s.", player.GetName(), candidateType.GetName(), targetOwner.GetName(), missileType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));
                                                                CreateReport(player, new LaunchReport(String.format("Your aircraft engaged %s's missile.", targetOwner.GetName()), false, player.GetID(), targetOwner.GetID()));
                                                                CreateReport(targetOwner, new LaunchReport(String.format("%s's aircraft engaged your missile.", player.GetName()), false, player.GetID(), targetOwner.GetID()));

                                                                targetOwner.AddHostilePlayer(aircraft.GetOwnerID());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }                                    
                                }
                                else if(aircraft.HasCannon() && aircraft.CannonReady())
                                {
                                    if(target instanceof Damagable && aircraft.GetPosition().DistanceTo(target.GetPosition()) <= Defs.AIRCRAFT_CANNON_RANGE)
                                    {
                                        Damagable targetDamagable = (Damagable)target;
                                        aircraft.FireCannon();
                                        aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                        target.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                        float fltHitChance = Defs.AIRCRAFT_CANNON_HIT_CHANCE;
                                        Player targetOwner = GetOwner(target);

                                        targetOwner.AddHostilePlayer(aircraft.GetOwnerID());

                                        if(fltHitChance > Defs.MAXIMUM_AIRCRAFT_CANNON_ACCURACY)
                                            fltHitChance = Defs.MAXIMUM_AIRCRAFT_CANNON_ACCURACY;

                                        if(random.nextFloat() < Math.max(fltHitChance, Defs.MINIMUM_AIRCRAFT_CANNON_ACCURACY))
                                        {
                                            short nDamageInflicted = (short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG);

                                            Player player = GetOwner(aircraft);

                                            targetDamagable.InflictDamage(nDamageInflicted);
                                            Scoring_DamageInflicted(player, targetOwner, nDamageInflicted);

                                            if(targetDamagable.Destroyed())
                                            {
                                                if(targetDamagable instanceof Airplane)
                                                {
                                                    AirplaneDestroyed((Airplane)targetDamagable);
                                                }

                                                aircraft.MoveToPosition(aircraft.GetGeoTarget());

                                                ProcessPlayerXPGain(player.GetID(), Defs.GetXPGainForKill(target), String.format("You destroyed a %s.", targetDamagable.GetTypeName()));
                                                ProcessPlayerXPLoss(targetOwner.GetID(), Defs.GetXPLossForKill(target), String.format("Your %s was destroyed.", targetDamagable.GetTypeName()));

                                                CreateEvent(new LaunchEvent(String.format("%s's aircraft shot down %s's %s.", player.GetName(), targetOwner.GetName(), targetDamagable.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                CreateReport(player, new LaunchReport(String.format("Your aircraft shot down %s's %s.", targetOwner.GetName(), targetDamagable.GetTypeName()), false, player.GetID(), targetOwner.GetID()));
                                                CreateReport(targetOwner, new LaunchReport(String.format("%s's aircraft shot down your %s.", player.GetName(), targetDamagable.GetTypeName()), false, targetOwner.GetID(), player.GetID()));
                                            }
                                            else
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s's aircraft shot %s's %s, causing %d HP of damage.", player.GetName(), targetOwner.GetName(), targetDamagable.GetTypeName(), nDamageInflicted)));
                                                CreateReport(targetOwner, new LaunchReport(String.format("%s damaged your %s!", player.GetName(), targetDamagable.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                                CreateReport(player, new LaunchReport(String.format("You damaged %s's %s!", targetOwner.GetName(), targetDamagable.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                            }
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent("", SoundEffect.SENTRY_GUN_MISS));
                                        }
                                    }
                                } 
                            }
                        }
                        else
                        {
                            aircraft.MoveToPosition(aircraft.GetGeoTarget());
                        }
                    }
                }
                else if(aircraft.GetMoveOrders() == MoveOrders.PROVIDE_FUEL)
                {
                    if(aircraft.CanTransferFuel() && aircraft.GetCurrentFuel() > Defs.AIRCRAFT_FUEL_TRANSFER_PER_TICK && aircraft.GetTarget() != null)
                    {
                        MapEntity target = aircraft.GetTarget().GetMapEntity(this);
                        
                        if(target != null && target instanceof Airplane)
                        {
                            Airplane refuelee = ((Airplane)target);
                            
                            if(EntityIsFriendly(refuelee, GetOwner(aircraft)))
                            {
                                float fltDistance = aircraft.GetPosition().DistanceTo(refuelee.GetPosition());

                                if(fltDistance <= Defs.AIRCRAFT_REFUEL_RANGE)
                                {
                                    float fltFuelTransferred = refuelee.TransferRefuel(Defs.AIRCRAFT_FUEL_TRANSFER_PER_TICK);
                                    
                                    //Commence fuel transfer. 
                                    aircraft.UseFuel(fltFuelTransferred);
                                    
                                    if(refuelee.FuelFull())
                                        aircraft.MoveToPosition(aircraft.GetGeoTarget());
                                }
                            }
                                
                        }
                    }
                }
            }
        }
    }
    
    private void ProcessInfantry()
    {
        for(Infantry infantry : GetInfantries())
        {
            boolean bStuffChanged = false;
            
            boolean bOnWater = TerrainChecker.CoordinateIsWater(infantry.GetPosition());

            if(bOnWater && !infantry.GetOnWater())
            {
                infantry.SetOnWater(true);
                bStuffChanged = true;
            }
            else if(!bOnWater && infantry.GetOnWater())
            {
                infantry.SetOnWater(false);
                bStuffChanged = true;
            }

            if(!infantry.GetOnWater())
            {
                switch(infantry.GetMoveOrders())
                {
                    case DEFEND:
                    {
                        boolean bInfantryFired = false;

                        if(infantry.GetCanFire())
                        {
                            for(EntityPointer pointer : quadtree.GetAffectedLandUnits(infantry.GetPosition(), Defs.INFANTRY_COMBAT_RANGE))
                            {
                                LandUnit otherUnit = pointer.GetLandUnit(game);

                                if(otherUnit != null)
                                {
                                    if(GetOwner(infantry) != null)
                                    {
                                        if(GetOwner(otherUnit) != null)
                                        {
                                            if(!bInfantryFired && otherUnit.GetID() != infantry.GetID() && otherUnit.GetOwnerID() != infantry.GetOwnerID())
                                            {
                                                if(!WouldBeFriendlyFire(GetOwner(otherUnit) ,GetOwner(infantry)) && !GetAttackIsBullying(GetOwner(infantry), GetOwner(otherUnit)))
                                                {
                                                    float fltCombatRange = Defs.INFANTRY_COMBAT_RANGE;

                                                    if(otherUnit.GetPosition().BroadPhaseCollisionTest(infantry.GetPosition()) && otherUnit.GetPosition().DistanceTo(infantry.GetPosition()) <= fltCombatRange)
                                                    {
                                                        Player attacker = GetOwner(infantry);
                                                        Player defender = GetOwner(otherUnit);
                                                        otherUnit.SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                                                        infantry.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                        otherUnit.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                        if(otherUnit.GetMoveOrders() != MoveOrders.DEFEND)
                                                        {
                                                            otherUnit.DefendPosition();
                                                        }

                                                        infantry.GetPosition().SetLastBearing((float)infantry.GetPosition().BearingTo(otherUnit.GetPosition()));

                                                        short nDamage = GetInfantryCombatDamage(infantry, otherUnit); //((short)(LaunchUtilities.GetRandomIntInBounds(0, Defs.INFANTRY_HP) * (infantry.GetHP()/Defs.INFANTRY_HP));

                                                        short nDamageInflicted = otherUnit.InflictDamage(nDamage);
                                                        Scoring_DamageInflicted(attacker, defender, nDamageInflicted); 

                                                        if(otherUnit.Destroyed())
                                                        {
                                                            CreateEvent(new LaunchEvent(String.format("%s's infantry killed %s's %s!", attacker.GetName(), defender.GetName(), otherUnit.GetTypeName()), SoundEffect.INFANTRY_ATTACK));
                                                            CreateReport(defender, new LaunchReport(String.format("%s's infantry killed your %s!", attacker.GetName(), otherUnit.GetTypeName()), true, defender.GetID(), attacker.GetID()));
                                                            CreateReport(attacker, new LaunchReport(String.format("Your infantry killed %s's %s!", defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                            CreateReport(new LaunchReport(String.format("%s's infantry killed %s's %s!", attacker.GetName(), defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                        }
                                                        else
                                                        {
                                                            CreateEvent(new LaunchEvent(String.format("%s's infantry inflicted %s hp of damage on %s's %s!", attacker.GetName(), nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), SoundEffect.INFANTRY_ATTACK));
                                                            CreateReport(defender, new LaunchReport(String.format("%s's infantry inflicted %s hp of damage on your %s!", attacker.GetName(), nDamageInflicted, otherUnit.GetTypeName()), true, defender.GetID(), attacker.GetID()));
                                                            CreateReport(attacker, new LaunchReport(String.format("Your infantry inflicted %s hp of damage on %s's %s!", nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                            CreateReport(new LaunchReport(String.format("%s's infantry inflicted %s hp of damage on %s's %s!", attacker.GetName(), nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                        }

                                                        EntityUpdated(otherUnit, false);
                                                        infantry.SetReloadTime(Defs.INFANTRY_RELOAD_TIME);
                                                        bInfantryFired = true;
                                                        bStuffChanged = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            LaunchLog.ConsoleMessage(String.format("Infantry %d owner null.", otherUnit.GetID()));
                                            otherUnit.InflictDamage(otherUnit.GetHP());
                                        }
                                    }
                                    else
                                    {
                                        LaunchLog.ConsoleMessage(String.format("Infantry %d owner null.", infantry.GetID()));
                                        infantry.InflictDamage(infantry.GetHP());
                                    }
                                }
                                else
                                {
                                    quadtree.RemoveEntity(pointer);
                                }
                            }
                        }
                    }
                    break;

                    case ATTACK:
                    {
                        //Attack the targeted entity. If you are in firing range, do not move, and also do not HoldPosition(). Doing so would guarantee death.
                        MapEntity target = infantry.GetTarget().GetMapEntity(game);

                        if(infantry.GetCanFire())
                        {
                            if(target != null)
                            {
                                if(WouldBeFriendlyFire(GetOwner(infantry), GetOwner(target)))
                                    infantry.Wait();

                                //Are we within firing range of the target? If so, attack. Else, move towards them.
                                if(infantry.GetPosition().DistanceTo(target.GetPosition()) <= Defs.INFANTRY_COMBAT_RANGE)
                                {
                                    Player attacker = GetOwner(infantry);
                                    Player defender = GetOwner(target);

                                    if(!defender.PlayerIsHostile(attacker))
                                    {
                                        defender.AddHostilePlayer(attacker.GetID());
                                    }

                                    if(target instanceof Infantry)
                                    {
                                        Infantry otherInfantry = ((Infantry)target);

                                        infantry.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                        otherInfantry.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                        infantry.GetPosition().SetLastBearing((float)infantry.GetPosition().BearingTo(otherInfantry.GetPosition()));

                                        short nDamage = GetInfantryCombatDamage(infantry, otherInfantry);
                                        short nDamageInflicted = otherInfantry.InflictDamage(nDamage);
                                        Scoring_DamageInflicted(attacker, defender, nDamageInflicted); 

                                        if(otherInfantry.Destroyed())
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's infantry killed %s's infantry!", attacker.GetName(), defender.GetName()), SoundEffect.INFANTRY_ATTACK));
                                            CreateReport(defender, new LaunchReport(String.format("%s's infantry killed your infantry!", attacker.GetName()), true, defender.GetID(), attacker.GetID()));
                                            CreateReport(attacker, new LaunchReport(String.format("Your infantry killed %s's infantry!", defender.GetName()), true, attacker.GetID(), defender.GetID()));
                                            CreateReport(new LaunchReport(String.format("%s's infantry killed %s's infantry!", attacker.GetName(), defender.GetName()), true, attacker.GetID(), defender.GetID()));
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's infantry inflicted %s hp of damage on %s's infantry!", attacker.GetName(), nDamageInflicted, defender.GetName()), SoundEffect.INFANTRY_ATTACK));
                                            CreateReport(defender, new LaunchReport(String.format("%s's infantry inflicted %s hp of damage on your infantry!", attacker.GetName(), nDamageInflicted), true, defender.GetID(), attacker.GetID()));
                                            CreateReport(attacker, new LaunchReport(String.format("Your infantry inflicted %s hp of damage on %s's infantry!", nDamageInflicted, defender.GetName()), true, attacker.GetID(), defender.GetID()));
                                            CreateReport(new LaunchReport(String.format("%s's infantry inflicted %s hp of damage on %s's infantry!", attacker.GetName(), nDamageInflicted, defender.GetName()), true, attacker.GetID(), defender.GetID()));
                                        }

                                        infantry.SetReloadTime(Defs.INFANTRY_RELOAD_TIME);
                                        bStuffChanged = true;
                                    }
                                }
                            }
                            else
                            {
                                infantry.DefendPosition();
                                EntityUpdated(infantry, false);
                            }
                        }  
                    }
                    break;

                    case WAIT:
                    {
                        //Do nothing.
                    }
                    break;

                    case MOVE:
                    {
                        for(EntityPointer pointer : quadtree.GetAffectedLandUnits(infantry.GetPosition(), Defs.INFANTRY_COMBAT_RANGE))
                        {
                            LandUnit otherUnit = pointer.GetLandUnit(game);

                            if(otherUnit != null)
                            {
                                if(GetOwner(infantry) != null)
                                {
                                    if(GetOwner(otherUnit) != null)
                                    {
                                        if(otherUnit.GetID() != infantry.GetID() && otherUnit.GetOwnerID() != infantry.GetOwnerID())
                                        {
                                            if(!WouldBeFriendlyFire(GetOwner(otherUnit) ,GetOwner(infantry)) && !GetAttackIsBullying(GetOwner(infantry), GetOwner(otherUnit)))
                                            {
                                                float fltCombatRange = Defs.INFANTRY_COMBAT_RANGE;

                                                if(otherUnit.GetPosition().BroadPhaseCollisionTest(infantry.GetPosition()) && otherUnit.GetPosition().DistanceTo(infantry.GetPosition()) <= fltCombatRange)
                                                {
                                                    infantry.DefendPosition();
                                                    EntityUpdated(infantry, false);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                quadtree.RemoveEntity(pointer);
                            }
                        }
                    }
                    break;
                }
            }

            if(bStuffChanged)
                    EntityUpdated(infantry, false);
        }    
    }
    
    private void ProcessTanks()
    {
        for(Tank tank : GetTanks())
        {
            boolean bStuffChanged = false;
            boolean bOnWater = TerrainChecker.CoordinateIsWater(tank.GetPosition());

            if(bOnWater && !tank.GetOnWater())
            {
                tank.SetOnWater(true);
                bStuffChanged = true;
            }
            else if(!bOnWater && tank.GetOnWater())
            {
                tank.SetOnWater(false);
                bStuffChanged = true;
            }

            //Main battle tanks and their target.
            if(tank.IsAnMBT())
            {   
                if(!tank.GetOnWater())
                {
                    switch(tank.GetMoveOrders())
                    {
                        case ATTACK:
                        {
                            if(tank.GetTarget() != null)
                            {
                                MapEntity target = tank.GetTarget().GetMapEntity(game);

                                if(target != null)
                                {
                                    if(WouldBeFriendlyFire(GetOwner(tank), GetOwner(target)))
                                        tank.Wait();

                                    if(target instanceof Damagable)
                                    {
                                        if(((Damagable)target).Destroyed())
                                        {
                                            tank.Wait();
                                            CreateEventForPlayer(new LaunchEvent(String.format("Target destroyed. %s standing down...", tank.GetTypeName())), tank.GetOwnerID());
                                            break;
                                        }
                                    }

                                    if(tank.GetCanFire() && tank.GetPosition().DistanceTo(target.GetPosition()) < Defs.BATTLE_TANK_FIRING_RANGE)
                                    {
                                        tank.SetReloadTime(Defs.BATTLE_TANK_RELOAD_TIME);
                                        tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                        target.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                        if(target instanceof Structure)
                                        {
                                            Structure targetStructure = ((Structure)target);

                                            if(!targetStructure.GetRespawnProtected())
                                            {
                                                bStuffChanged = true;
                                                short nDamageInflicted = targetStructure.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG));

                                                Player targetOwner = GetOwner(targetStructure);
                                                Player player = GetOwner(tank);
                                                Scoring_DamageInflicted(player, targetOwner, nDamageInflicted);

                                                targetOwner.AddHostilePlayer(tank.GetOwnerID());

                                                if(targetStructure.Destroyed())
                                                {
                                                    tank.Wait();

                                                    CreateSalvage(targetStructure.GetPosition().GetCopy(), GetSaleValue(targetStructure));
                                                    CreateRubble(targetStructure);

                                                    ProcessPlayerXPGain(player.GetID(), Defs.STRUCTURE_KILLED_XP, "You destroyed a structure.");
                                                    ProcessPlayerXPLoss(targetOwner.GetID(), Defs.STRUCTURE_LOST_XP, "Your structure was destroyed.");

                                                    CreateReport(targetOwner, new LaunchReport(String.format("%s destroyed your %s!", player.GetName(), targetStructure.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                                    CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s's %s, causing %d HP of damage and destroying it.", player.GetName(), targetOwner.GetName(), targetStructure.GetTypeName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));
                                                    CreateReport(player, new LaunchReport(String.format("You destroyed %s's %s!", targetOwner.GetName(), targetStructure.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                                }
                                                else
                                                {
                                                    CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s's %s, causing %d HP of damage.", player.GetName(), targetOwner.GetName(), targetStructure.GetTypeName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));
                                                    CreateReport(targetOwner, new LaunchReport(String.format("%s damaged your %s!", player.GetName(), targetStructure.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                                    CreateReport(player, new LaunchReport(String.format("You damaged %s's %s!", targetOwner.GetName(), targetStructure.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                                }
                                            }  
                                        }
                                        else if(target instanceof Shipyard)
                                        {
                                            Shipyard targetShipyard = ((Shipyard)target);

                                            if(!targetShipyard.Destroyed())
                                            {
                                                Player player = GetOwner(tank);

                                                short nDamageInflicted = targetShipyard.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG));

                                                Player targetOwner = GetOwner(targetShipyard);
                                                Scoring_DamageInflicted(player, targetOwner, nDamageInflicted);

                                                if(targetOwner != null)
                                                {
                                                    targetOwner.AddHostilePlayer(tank.GetOwnerID());

                                                    if(targetShipyard.Destroyed())
                                                    {
                                                        tank.Wait();

                                                        ProcessPlayerXPGain(player.GetID(), Defs.SHIPYARD_KILLED_XP, String.format("You destroyed %s.", targetShipyard.GetName()));
                                                        ProcessPlayerXPLoss(targetOwner.GetID(), Defs.SHIPYARD_LOST_XP, String.format("You lost %s.", targetShipyard.GetName()));

                                                        CreateReport(targetOwner, new LaunchReport(String.format("%s destroyed %s!", player.GetName(), targetShipyard.GetName()), true, targetOwner.GetID(), player.GetID()));
                                                        CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s, causing %d HP of damage and destroying it.", player.GetName(), targetShipyard.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));
                                                        CreateReport(player, new LaunchReport(String.format("You destroyed %s!", targetShipyard.GetName()), true, player.GetID(), targetOwner.GetID()));
                                                    }
                                                    else
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s, causing %d HP of damage.", player.GetName(), targetShipyard.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));
                                                        CreateReport(player, new LaunchReport(String.format("You damaged %s!", targetShipyard.GetName()), true, player.GetID(), targetOwner.GetID()));
                                                    }
                                                }
                                                else
                                                {
                                                    if(targetShipyard.Destroyed())
                                                    {
                                                        tank.Wait();

                                                        CreateReport(new LaunchReport(String.format("%s destroyed %s!", player.GetName(), targetShipyard.GetName()), true, player.GetID()));
                                                        CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s, causing %d HP of damage and destroying it.", player.GetName(), targetShipyard.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));
                                                        CreateReport(player, new LaunchReport(String.format("You destroyed %s!", targetShipyard.GetName()), true, player.GetID()));
                                                    }
                                                    else
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's tank shelled %s, causing %d HP of damage.", player.GetName(), targetShipyard.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));
                                                        CreateReport(player, new LaunchReport(String.format("You damaged %s!", targetShipyard.GetName()), true, player.GetID()));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                tank.Wait();
                                            }   
                                        }
                                        else if(target instanceof Infantry)
                                        {
                                            Infantry targetInfantry = ((Infantry)target);

                                            if(!targetInfantry.Destroyed())
                                            {
                                                Player targetPlayer = GetOwner(targetInfantry);

                                                short nDamageInflicted = targetInfantry.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG));

                                                Player inflictor = GetOwner(tank);
                                                Scoring_DamageInflicted(inflictor, targetPlayer, nDamageInflicted);
                                                targetPlayer.AddHostilePlayer(tank.GetOwnerID());
                                                targetInfantry.SetUnderAttack(Defs.UNDER_ATTACK_TIME);

                                                if(targetInfantry.Destroyed())
                                                {
                                                    tank.Wait();

                                                    ProcessPlayerXPGain(inflictor.GetID(), Defs.INFANTRY_KILLED_XP, "You killed an infantry unit.");
                                                    ProcessPlayerXPLoss(targetPlayer.GetID(), Defs.INFANTRY_LOST_XP, "You lost an infantry unit.");

                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's infantry, causing %d HP of damage and destroying it.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));

                                                    CreateEvent(new LaunchEvent(String.format("%s's tank killed %s's infantry!", inflictor.GetName(), targetPlayer.GetName())));
                                                    CreateReport(new LaunchReport(String.format("%s's tank killed %s's infantry!", inflictor.GetName(), targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                                else
                                                {
                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's infantry, causing %d HP of damage.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));

                                                    CreateReport(targetPlayer, new LaunchReport(String.format("Your infantry were wounded by %s's tank!", inflictor.GetName()), true, targetPlayer.GetID(), inflictor.GetID()));

                                                    CreateReport(inflictor, new LaunchReport(String.format("You wounded %s's infantry!", targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                            }
                                            else
                                            {
                                                tank.Wait();
                                            }  
                                        }
                                        else if(target instanceof Tank)
                                        {
                                            Tank targetTank = (Tank)target;

                                            if(!targetTank.Destroyed())
                                            {
                                                Player targetPlayer = GetOwner(targetTank);
                                                targetPlayer.AddHostilePlayer(tank.GetOwnerID());

                                                short nDamageInflicted = targetTank.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.AVERAGE_MIN_DMG, Defs.AVERAGE_MAX_DMG));

                                                Player inflictor = GetOwner(tank);
                                                Scoring_DamageInflicted(inflictor, targetPlayer, nDamageInflicted);
                                                targetTank.SetUnderAttack(Defs.UNDER_ATTACK_TIME);

                                                if(targetTank.Destroyed())
                                                {
                                                    tank.Wait();

                                                    ProcessPlayerXPGain(inflictor.GetID(), Defs.TANK_KILLED_XP, "You killed a tank.");
                                                    ProcessPlayerXPLoss(targetPlayer.GetID(), Defs.TANK_LOST_XP, "You lost a tank.");

                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's tank, causing %d HP of damage and destroying it.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));

                                                    CreateEvent(new LaunchEvent(String.format("%s's tank killed %s's tank!", inflictor.GetName(), targetPlayer.GetName())));
                                                    CreateReport(new LaunchReport(String.format("%s's tank killed %s's tank!", inflictor.GetName(), targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                                else
                                                {
                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's tank, causing %d HP of damage.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));

                                                    CreateReport(targetPlayer, new LaunchReport(String.format("Your tank was damaged by %s's tank!", inflictor.GetName()), true, targetPlayer.GetID(), inflictor.GetID()));

                                                    CreateReport(inflictor, new LaunchReport(String.format("You damaged %s's tank!", targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                            }
                                            else
                                            {
                                                tank.Wait();
                                            }
                                        }
                                        else if(target instanceof CargoTruck)
                                        {
                                            CargoTruck targetTruck = (CargoTruck)target;

                                            if(!targetTruck.Destroyed())
                                            {
                                                Player targetPlayer = GetOwner(targetTruck);
                                                targetPlayer.AddHostilePlayer(tank.GetOwnerID());

                                                short nDamageInflicted = targetTruck.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG));

                                                Player inflictor = GetOwner(tank);
                                                Scoring_DamageInflicted(inflictor, targetPlayer, nDamageInflicted);
                                                targetTruck.SetUnderAttack(Defs.UNDER_ATTACK_TIME);

                                                if(targetTruck.Destroyed())
                                                {
                                                    tank.Wait();
                                                    CargoSystemDestroyed(targetTruck.GetOwnerID(), targetTruck, targetTruck.GetPosition(), targetTruck.GetCargoSystem(), targetTruck.GetResourceSystem());

                                                    ProcessPlayerXPGain(inflictor.GetID(), Defs.TRUCK_KILLED_XP, "You killed a cargo truck.");
                                                    ProcessPlayerXPLoss(targetPlayer.GetID(), Defs.TRUCK_LOST_XP, "You lost a cargo truck.");

                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's cargo truck, causing %d HP of damage and destroying it.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_EXPLOSION));

                                                    CreateEvent(new LaunchEvent(String.format("%s's tank killed %s's cargo truck!", inflictor.GetName(), targetPlayer.GetName())));
                                                    CreateReport(new LaunchReport(String.format("%s's tank killed %s's cargo truck!", inflictor.GetName(), targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                                else
                                                {
                                                    CreateEvent(new LaunchEvent(String.format("An tank shelled %s's cargo truck, causing %d HP of damage.", targetPlayer.GetName(), nDamageInflicted), SoundEffect.ARTILLERY_FIRE));

                                                    CreateReport(targetPlayer, new LaunchReport(String.format("Your cargo truck was damaged by %s's tank!", inflictor.GetName()), true, targetPlayer.GetID(), inflictor.GetID()));

                                                    CreateReport(inflictor, new LaunchReport(String.format("You damaged %s's cargo truck!", targetPlayer.GetName()), true, inflictor.GetID(), targetPlayer.GetID()));
                                                }
                                            }
                                            else
                                            {
                                                tank.Wait();
                                            }  
                                        }
                                    }
                                }
                            }
                            else
                            {
                                tank.DefendPosition();
                            }
                        }
                        break;

                        case DEFEND:
                        {
                            boolean bTankFired = false;

                            if(tank.GetCanFire())
                            {
                                for(EntityPointer pointer : quadtree.GetAffectedLandUnits(tank.GetPosition(), Defs.BATTLE_TANK_FIRING_RANGE))
                                {
                                    LandUnit otherUnit = pointer.GetLandUnit(game);

                                    if(otherUnit != null)
                                    {
                                        if(GetOwner(tank) != null)
                                        {
                                            if(GetOwner(otherUnit) != null)
                                            {
                                                if(!bTankFired && otherUnit.GetID() != tank.GetID() && otherUnit.GetOwnerID() != tank.GetOwnerID())
                                                {
                                                    if(!WouldBeFriendlyFire(GetOwner(otherUnit), GetOwner(tank)) && !GetAttackIsBullying(GetOwner(tank), GetOwner(otherUnit)))
                                                    {
                                                        float fltCombatRange = Defs.BATTLE_TANK_FIRING_RANGE;

                                                        if(otherUnit.GetPosition().BroadPhaseCollisionTest(tank.GetPosition()) && otherUnit.GetPosition().DistanceTo(tank.GetPosition()) <= fltCombatRange)
                                                        {
                                                            Player attacker = GetOwner(tank);
                                                            Player defender = GetOwner(otherUnit);
                                                            otherUnit.SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                                                            tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                            otherUnit.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                            if(otherUnit.GetMoveOrders() != MoveOrders.DEFEND)
                                                            {
                                                                otherUnit.DefendPosition();
                                                            }

                                                            tank.GetPosition().SetLastBearing((float)tank.GetPosition().BearingTo(otherUnit.GetPosition()));

                                                            short nDamage = GetTankCombatDamage(tank, otherUnit);

                                                            short nDamageInflicted = otherUnit.InflictDamage(nDamage);
                                                            Scoring_DamageInflicted(attacker, defender, nDamageInflicted); 

                                                            if(otherUnit.Destroyed())
                                                            {
                                                                CreateEvent(new LaunchEvent(String.format("%s's tank killed %s's %s!", attacker.GetName(), defender.GetName(), otherUnit.GetTypeName()), SoundEffect.ARTILLERY_EXPLOSION));
                                                                CreateReport(defender, new LaunchReport(String.format("%s's tank killed your %s!", attacker.GetName(), otherUnit.GetTypeName()), true, defender.GetID(), attacker.GetID()));
                                                                CreateReport(attacker, new LaunchReport(String.format("Your tank killed %s's %s!", defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                                CreateReport(new LaunchReport(String.format("%s's tank killed %s's %s!", attacker.GetName(), defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                            }
                                                            else
                                                            {
                                                                CreateEvent(new LaunchEvent(String.format("%s's tank inflicted %s hp of damage on %s's %s!", attacker.GetName(), nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), SoundEffect.ARTILLERY_EXPLOSION));
                                                                CreateReport(defender, new LaunchReport(String.format("%s's tank inflicted %s hp of damage on your %s!", attacker.GetName(), nDamageInflicted, otherUnit.GetTypeName()), true, defender.GetID(), attacker.GetID()));
                                                                CreateReport(attacker, new LaunchReport(String.format("Your tank inflicted %s hp of damage on %s's %s!", nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                                CreateReport(new LaunchReport(String.format("%s's tank inflicted %s hp of damage on %s's %s!", attacker.GetName(), nDamageInflicted, defender.GetName(), otherUnit.GetTypeName()), true, attacker.GetID(), defender.GetID()));
                                                            }

                                                            EntityUpdated(otherUnit, false);
                                                            tank.SetReloadTime(Defs.BATTLE_TANK_RELOAD_TIME);
                                                            bTankFired = true;
                                                            bStuffChanged = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        quadtree.RemoveEntity(pointer);
                                    }
                                }
                            }
                        }
                        break;

                        case MOVE:
                        {
                            for(EntityPointer pointer : quadtree.GetAffectedLandUnits(tank.GetPosition(), Defs.BATTLE_TANK_FIRING_RANGE))
                            {
                                LandUnit otherUnit = pointer.GetLandUnit(game);

                                if(otherUnit != null)
                                {
                                    if(GetOwner(tank) != null)
                                    {
                                        if(GetOwner(otherUnit) != null)
                                        {
                                            if(otherUnit.GetID() != tank.GetID() && otherUnit.GetOwnerID() != tank.GetOwnerID())
                                            {
                                                if(!WouldBeFriendlyFire(GetOwner(otherUnit) ,GetOwner(tank)) && !GetAttackIsBullying(GetOwner(tank), GetOwner(otherUnit)))
                                                {
                                                    float fltCombatRange = Defs.BATTLE_TANK_FIRING_RANGE;

                                                    if(otherUnit.GetPosition().BroadPhaseCollisionTest(tank.GetPosition()) && otherUnit.GetPosition().DistanceTo(tank.GetPosition()) <= fltCombatRange)
                                                    {
                                                        tank.DefendPosition();
                                                        EntityUpdated(tank, false);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    quadtree.RemoveEntity(pointer);
                                }
                            }
                        }
                        break;

                        case WAIT:
                        {
                            tank.DefendPosition();
                            EntityUpdated(tank, false);
                        }
                        break;
                    }
                }       
            }
            else if(tank.IsASPAAG() && !tank.GetOnWater())
            {
                if(tank.GetCanFire())
                {
                    Player spaagOwner = Players.get(tank.GetOwnerID());

                    if(spaagOwner != null)
                    {
                        if(spaagOwner.UnderAttack())
                        {
                            for(EntityPointer pointer : quadtree.GetAffectedMissiles(tank.GetPosition(), Defs.SPAAG_FIRING_RANGE))
                            {
                                Missile missile = pointer.GetMissile(game);

                                if(missile != null)
                                {
                                    if(tank.GetCanFire() && config.GetMissileType(missile.GetType()) != null)
                                    {
                                        //Don't engage if the owner is AWOL or banned.
                                        if(!config.GetMissileType(missile.GetType()).GetICBM() && !spaagOwner.GetAWOL() && !spaagOwner.GetBanned_Server())
                                        {
                                            if(tank.GetOwnerID() != missile.GetOwnerID())
                                            {
                                                if(tank.GetPosition().EvenBroaderPhaseCollisionTest(missile.GetPosition()) && tank.GetPosition().DistanceTo(missile.GetPosition()) <= Defs.SPAAG_FIRING_RANGE)
                                                {
                                                    Player missileOwner = Players.get(missile.GetOwnerID());

                                                    //Don't shoot friendly missiles going over the top; but do engage them if they're threatening the player (closes fire - affiliate - undefended loophole).
                                                    if(!WouldBeFriendlyFire(spaagOwner, missileOwner) || ThreatensPlayerOptimised(missile, spaagOwner, GetMissileTarget(missile), config.GetMissileType(missile.GetType())))
                                                    {
                                                        //Brrrrp!
                                                        //Decide if the missile got shot down, while applying ECM/Stealth.
                                                        tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                        float sentryhitchance = Defs.SPAAG_ACCURACY;

                                                        if(config.GetMissileType(missile.GetType()).GetECM())
                                                        {
                                                            sentryhitchance -= config.GetECMInterceptorChanceReduction()/2; 
                                                        }

                                                        if(random.nextFloat() < (sentryhitchance < Defs.MINIMUM_SENTRY_ACCURACY ? Defs.MINIMUM_SENTRY_ACCURACY : sentryhitchance))
                                                        {                         
                                                            missile.Destroy();
                                                            EntityRemoved(missile, false);

                                                            CreateEvent(new LaunchEvent(String.format("%s's SPAAG down %s's missile.", spaagOwner.GetName(), missileOwner.GetName()), SoundEffect.SENTRY_GUN_HIT));
                                                            CreateReport(spaagOwner, new LaunchReport(String.format("Your SPAAG shot down %s's missile.", missileOwner.GetName()), false, spaagOwner.GetID(), missileOwner.GetID()));
                                                            CreateReport(missileOwner, new LaunchReport(String.format("%s's SPAAG shot down your missile.", spaagOwner.GetName()), false, missileOwner.GetID(), spaagOwner.GetID()));
                                                        }
                                                        else
                                                        {
                                                            CreateEvent(new LaunchEvent(String.format("%s's SPAAG missed %s's missile.", spaagOwner.GetName(), missileOwner.GetName()), SoundEffect.SENTRY_GUN_MISS));
                                                            CreateReport(spaagOwner, new LaunchReport(String.format("Your SPAAG missed %s's missile.", missileOwner.GetName()), false, spaagOwner.GetID(), missileOwner.GetID()));
                                                            CreateReport(missileOwner, new LaunchReport(String.format("%s's SPAAG missed your missile.", spaagOwner.GetName()), false, missileOwner.GetID(), spaagOwner.GetID()));
                                                        }

                                                        tank.SetReloadTime(Defs.BATTLE_TANK_RELOAD_TIME);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }
                            }

                            for(EntityPointer pointer : quadtree.GetAffectedAircrafts(tank.GetPosition(), Defs.SPAAG_FIRING_RANGE))
                            {
                                Airplane aircraft = pointer.GetAircraft(game);

                                if(aircraft != null)
                                {
                                    if(tank.GetCanFire())
                                    {
                                        //Don't engage if the owner is AWOL or banned.
                                        if(!spaagOwner.GetAWOL() && !spaagOwner.GetBanned_Server())
                                        {
                                            Player aircraftOwner = Players.get(aircraft.GetOwnerID());

                                            if(!WouldBeFriendlyFire(spaagOwner, aircraftOwner) && AircraftIsHostile(aircraft, spaagOwner))
                                            {
                                                if(tank.GetPosition().EvenBroaderPhaseCollisionTest(aircraft.GetPosition()) && tank.GetPosition().DistanceTo(aircraft.GetPosition()) <= Defs.SPAAG_FIRING_RANGE)
                                                {
                                                    tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                    aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                    float sentryhitchance = config.GetSentryGunHitChance();

                                                    if(random.nextFloat() < sentryhitchance)
                                                    {
                                                        short nDamageInflicted = aircraft.InflictDamage((short)LaunchUtilities.GetRandomIntInBounds(Defs.SPAAG_MIN_DMG_AIRCRAFT, Defs.SPAAG_MAX_DMG_AIRCRAFT));
                                                        Scoring_DamageInflicted(spaagOwner, aircraftOwner, nDamageInflicted);

                                                        if(aircraft.Destroyed())
                                                        {
                                                            CreateEvent(new LaunchEvent(String.format("%s's SPAAG shot down %s's %s.", spaagOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_HIT));
                                                            CreateReport(spaagOwner, new LaunchReport(String.format("Your SPAAG shot down %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false));               
                                                            CreateReport(aircraftOwner, new LaunchReport(String.format("%s's SPAAG shot down your %s.", spaagOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), spaagOwner.GetID()));
                                                            CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));                    
                                                        }
                                                        else
                                                        {
                                                            CreateEvent(new LaunchEvent(String.format("%s's SPAAG damaged %s's %s.", spaagOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_MISS));
                                                            CreateReport(spaagOwner, new LaunchReport(String.format("Your SPAAG damaged %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false, spaagOwner.GetID(), aircraftOwner.GetID()));
                                                            CreateReport(aircraftOwner, new LaunchReport(String.format("%s's SPAAG damaged your %s.", spaagOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), spaagOwner.GetID()));
                                                        }
                                                    }
                                                    else
                                                    {
                                                        CreateEvent(new LaunchEvent(String.format("%s's SPAAG missed %s's %s.", spaagOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.SENTRY_GUN_MISS));
                                                        CreateReport(spaagOwner, new LaunchReport(String.format("Your SPAAG missed %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false, spaagOwner.GetID(), aircraftOwner.GetID()));
                                                        CreateReport(aircraftOwner, new LaunchReport(String.format("%s's SPAAG missed your %s.", spaagOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), spaagOwner.GetID()));
                                                    }

                                                    tank.SetReloadTime(Defs.BATTLE_TANK_RELOAD_TIME);
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        break;
                                    } 
                                }
                            }
                        }
                    }
                    else
                    {
                        tank.InflictDamage(tank.GetHP());
                    }
                }
            }
            else if(tank.HasArtillery() && !tank.GetOnWater())
            {
                if(tank.HasFireOrder())
                {
                    MissileSystem system = tank.GetMissileSystem();
                    FireOrder order = tank.GetFireOrder();
                    GeoCoord geoTarget = order.GetGeoTarget().GetCopy();
                    Player owner = GetOwner(tank);

                    if(owner != null && geoTarget != null)
                    {
                        if(system.GetOccupiedSlotCount() > 0)
                        {
                            if(system.ReadyToFire())
                            {
                                for(byte lSlotNumber = 0; lSlotNumber < system.GetSlotCount(); lSlotNumber++)
                                {
                                    if(system.GetSlotHasMissile(lSlotNumber) && system.GetSlotReadyToFire(lSlotNumber))
                                    {
                                        MissileType shell = config.GetMissileType(system.GetSlotMissileType(lSlotNumber));

                                        if(shell != null)
                                        {
                                            geoTarget.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * order.GetRadius());

                                            tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                            system.Fire(lSlotNumber);

                                            system.SetReloadTimeRemaining(system.GetReloadTime());

                                            CreateMissileLaunch(tank, tank.GetPosition().GetCopy(), shell.GetID(), owner.GetID(), geoTarget, null, false);
                                            CreateEvent(new LaunchEvent(String.format("%s's tank fired %s.", owner.GetName(), shell.GetName()), SoundEffect.ARTILLERY_FIRE));

                                            break;
                                        } 
                                    }
                                }
                            }
                        }
                        else
                        {
                            tank.RoundsComplete();
                        }  
                    }
                    else
                    {
                        tank.RoundsComplete();
                    }   
                }
            }

            if(bStuffChanged)
                EntityUpdated(tank, false);
        }     
    }
    
    private void ProcessTrucks()
    {
        for(CargoTruck truck : GetCargoTrucks())
        {
            boolean bOnWater = TerrainChecker.CoordinateIsWater(truck.GetPosition());

            if(bOnWater && !truck.GetOnWater())
            {
                truck.SetOnWater(true);
                EntityUpdated(truck, false);
            }
            else if(!bOnWater && truck.GetOnWater())
            {
                truck.SetOnWater(false);
                EntityUpdated(truck, false);
            }
        }
    }
    
    private void ProcessShips()
    {
        for(Ship ship : GetShips())
        {
            if(!ship.Destroyed())
            {
                //TODO: If ships are damaged, pick up steel and machinery and use it to repair. If not nuclear and fuel deficit > 0, collect fuel for refueling.

                Player shipOwner = Players.get(ship.GetOwnerID());

                if(shipOwner != null && shipOwner.UnderAttack())
                {
                    //Automatic sentry stuff.
                    if(ship.HasSentries())
                    {
                        for(EntityPointer pointer : quadtree.GetAffectedMissiles(ship.GetPosition(), Defs.CIWS_RANGE))
                        {
                            Missile missile = pointer.GetMissile(game);

                            if(missile != null)
                            {
                                MissileType type = config.GetMissileType(missile.GetType());

                                if(type != null && !type.GetICBM())
                                {
                                    if(missile.Flying() && !missile.GetDetonate())
                                    {
                                        if(ship.GetReadySentryCount() > 0)
                                        {
                                            //Don't engage if the owner is AWOL or banned.
                                            if(!shipOwner.GetAWOL() && !shipOwner.GetBanned_Server())
                                            {
                                                if(shipOwner.GetOwnerID() != missile.GetOwnerID())
                                                {
                                                    if(ship.GetPosition().DistanceTo(missile.GetPosition()) <= Defs.CIWS_RANGE)
                                                    {
                                                        Player missileOwner = Players.get(missile.GetOwnerID());

                                                        //Don't shoot friendly missiles going over the top; but do engage them if they're threatening the player (closes fire - affiliate - undefended loophole).
                                                        if(!WouldBeFriendlyFire(shipOwner, missileOwner) || ThreatensPlayerOptimised(missile, shipOwner, GetMissileTarget(missile), config.GetMissileType(missile.GetType())))
                                                        {
                                                            //Brrrrp!
                                                            //Decide if the missile got shot down, while applying ECM/Stealth.
                                                            ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                            boolean bMissileDestroyed = false;

                                                            for(ShortDelay dlyReload : ship.GetSentryGuns())
                                                            {
                                                                if(dlyReload.Expired() && !bMissileDestroyed)
                                                                {
                                                                    float sentryhitchance;
                                                                    int lReload = Defs.CIWS_RELOAD_TIME;

                                                                    if(config.GetMissileType(missile.GetType()).GetECM())
                                                                    {
                                                                        sentryhitchance = Defs.CIWS_HIT_CHANCE - config.GetECMInterceptorChanceReduction(); 
                                                                    }
                                                                    else
                                                                    {
                                                                        sentryhitchance = Defs.CIWS_HIT_CHANCE;
                                                                    }

                                                                    if(random.nextFloat() < (sentryhitchance < Defs.MINIMUM_CIWS_ACCURACY ? Defs.MINIMUM_CIWS_ACCURACY : sentryhitchance))
                                                                    {                         
                                                                        bMissileDestroyed = true;
                                                                    }

                                                                    dlyReload.Set(lReload);
                                                                }
                                                            }

                                                            if(bMissileDestroyed)
                                                            {                         
                                                                missile.Destroy();
                                                                EntityUpdated(missile, false);

                                                                CreateEvent(new LaunchEvent(String.format("%s's %s shot down %s's missile.", shipOwner.GetName(), ship.GetTypeName(), missileOwner.GetName()), SoundEffect.SENTRY_GUN_HIT));
                                                                CreateReport(shipOwner, new LaunchReport(String.format("Your %s shot down %s's missile.", ship.GetTypeName(), missileOwner.GetName()), false, shipOwner.GetID(), missileOwner.GetID()));
                                                                CreateReport(missileOwner, new LaunchReport(String.format("%s's %s shot down your missile.", shipOwner.GetName(), ship.GetTypeName()), false, missileOwner.GetID(), shipOwner.GetID()));
                                                            }
                                                            else
                                                            {
                                                                CreateEvent(new LaunchEvent(String.format("%s's %s missed %s's missile.", shipOwner.GetName(), ship.GetTypeName(), missileOwner.GetName()), SoundEffect.SENTRY_GUN_MISS));
                                                                CreateReport(shipOwner, new LaunchReport(String.format("Your %s missed %s's missile.", ship.GetTypeName(), missileOwner.GetName()), false, shipOwner.GetID(), missileOwner.GetID()));
                                                                CreateReport(missileOwner, new LaunchReport(String.format("%s's %s missed your missile.", shipOwner.GetName(), ship.GetTypeName()), false, missileOwner.GetID(), shipOwner.GetID()));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } 
                            }
                        }
                    }
                }

                switch(ship.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(ship.GetGeoTarget() != null)
                        {
                            GeoCoord geoTarget = ship.GetGeoTarget();
                            GeoCoord geoPosition = ship.GetPosition();

                            if(!TerrainChecker.CoordinateIsWater(geoPosition.GetNextCoordinate(geoTarget, Defs.NAVAL_SPEED, lGameTickRate)))
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

                    case PROVIDE_FUEL:
                    {
                        MapEntity target = ship.GetTarget().GetMapEntity(game);

                        if(target != null && target instanceof NavalVessel)
                        {
                            NavalVessel refuelee = ((NavalVessel)target);

                            float fltDistance = ship.GetPosition().DistanceTo(refuelee.GetPosition());

                            if(fltDistance <= Defs.SHIP_REFUELING_DISTANCE)
                            {
                                if(!ship.GetNuclear())
                                {
                                    float fltFuelTransferred = refuelee.TransferRefuel(Math.min(refuelee.GetFuelDeficit(), Defs.SHIP_REFUEL_RATE_PER_TICK_TONS));

                                    //Commence fuel transfer. 
                                    ship.UseFuel(fltFuelTransferred);

                                    if(refuelee.FuelFull())
                                        ship.MoveToPosition(ship.GetGeoTarget());
                                }
                                else if(ship.HasCargo() && ship.GetCargoSystem().ContainsResourceType(ResourceType.OIL))    
                                {
                                    //Commence fuel transfer. 
                                    Resource fuel = ship.GetCargoSystem().RemoveResource(ResourceType.OIL, (int)(Math.min(refuelee.GetFuelDeficit(), Defs.SHIP_REFUEL_RATE_PER_TICK_TONS)) * Defs.KG_PER_TON);

                                    refuelee.TransferRefuel(fuel.GetQuantity()/Defs.KG_PER_TON);

                                    if(refuelee.FuelFull())
                                        ship.MoveToPosition(ship.GetGeoTarget());
                                }
                            }
                        }
                    }
                    break;
                    
                    case ATTACK:
                    {
                        if((ship.HasTarget() || ship.HasGeoTarget()) && (/*(ship.HasMissiles() && ship.GetMissileSystem().ReadyToFire()) || */(ship.HasArtillery() && ship.GetArtillerySystem().ReadyToFire())))
                        {
                            GeoCoord geoTarget = null;

                            if(ship.GetTarget() != null && ship.GetTarget().GetMapEntity(game) != null)
                            {
                                geoTarget = ship.GetTarget().GetMapEntity(game).GetPosition().GetCopy();
                            }
                            else if(ship.GetGeoTarget() != null)
                            {
                                geoTarget = ship.GetGeoTarget();
                            }
                            else
                            {
                                ship.Wait();
                            }

                            if(geoTarget != null)
                            {
                                MissileSystem system = ship.GetArtillerySystem();
                                
                                if(ship.GetTarget() != null)
                                {
                                    if(ship.GetTarget().GetMapEntity(game) != null)
                                    {
                                        MapEntity target = ship.GetTarget().GetMapEntity(game);
                                        
                                        if(target instanceof Damagable)
                                        {
                                            if(((Damagable)target).Destroyed())
                                            {
                                                ship.Wait();
                                                CreateEventForPlayer(new LaunchEvent(String.format("Target destroyed. %s standing down...", ship.GetTypeName())), ship.GetOwnerID());
                                                break;
                                            }
                                        }
                                    }
                                }
                                
                                if(ship.GetPosition().DistanceTo(geoTarget) > config.GetLongestMissileRange(system, true))
                                {
                                    CreateEventForPlayer(new LaunchEvent(String.format("Your %s has no more artillery with enough range to reach the target. Standing down...", ship.GetTypeName())), ship.GetOwnerID());
                                    ship.Wait();
                                }
                                    
                                if(system.GetReadySlotCount() > 0)
                                {
                                    boolean bHasUsableTypes = false;

                                    for(int i = 0; i < system.GetSlotCount(); i++)
                                    {
                                        if(system.GetSlotHasMissile(i))
                                        {
                                            MissileType type = config.GetMissileType(system.GetSlotMissileType(i));

                                            if(type != null && !type.GetICBM() && (!type.GetAntiSubmarine() || (ship.GetTarget() != null && ship.GetTarget().GetMapEntity(game) instanceof Submarine)))
                                            {
                                                if(type.GetMissileRange() >= geoTarget.DistanceTo(ship.GetPosition()))
                                                {
                                                    bHasUsableTypes = true;
                                                        
                                                    if(system.GetSlotReadyToFire(i))
                                                    {
                                                        CreateMissileLaunch(ship, ship.GetPosition().GetCopy(), type.GetID(), ship.GetOwnerID(), geoTarget, ship.GetTarget(), false);
                                                        CreateEvent(new LaunchEvent(String.format("%s's %s launched %s.", GetOwner(ship).GetName(), ship.GetTypeName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));
                                                        ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                        system.Fire(i, Defs.AUTO_FIRE_RELOAD_TIME);
                                                        break;
                                                    }
                                                }
                                            }
                                        }  
                                    }

                                    if(!bHasUsableTypes)
                                    {
                                        CreateEventForPlayer(new LaunchEvent(String.format("Your %s has no more usable missiles. Standing down...", ship.GetTypeName())), ship.GetOwnerID());
                                        ship.Wait();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void ProcessSubmarines()
    {
        for(Submarine submarine : GetSubmarines())
        {
            if(!submarine.Destroyed())
            {
                switch(submarine.GetMoveOrders())
                {
                    case MOVE:
                    {
                        if(submarine.GetGeoTarget() != null)
                        {
                            GeoCoord geoTarget = submarine.GetGeoTarget();
                            GeoCoord geoPosition = submarine.GetPosition();

                            if(!TerrainChecker.CoordinateIsWater(
                                    geoPosition.GetNextCoordinate(geoTarget, Defs.NAVAL_SPEED, lGameTickRate)))
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

                    case PROVIDE_FUEL:
                    {
                        MapEntity target = submarine.GetTarget().GetMapEntity(game);

                        if(target != null && target instanceof NavalVessel)
                        {
                            NavalVessel refuelee = ((NavalVessel) target);

                            float fltDistance = submarine.GetPosition().DistanceTo(refuelee.GetPosition());

                            if(fltDistance <= Defs.SHIP_REFUELING_DISTANCE && !submarine.Submerged())
                            {
                                float fltFuelTransferred = refuelee.TransferRefuel(
                                        Math.min(refuelee.GetFuelDeficit(), Defs.SHIP_REFUEL_RATE_PER_TICK_TONS));

                                // Commence fuel transfer.
                                submarine.UseFuel(fltFuelTransferred);

                                if(refuelee.FuelFull())
                                {
                                    submarine.MoveToPosition(submarine.GetGeoTarget());
                                }
                            }
                        }
                    }
                    break;

                    /*case ATTACK:
                    {
                        // Chase: Added null guards to pass checks if a submarine doesnt have a system. To not flood server with nulls.
                        MissileSystem missileSys = submarine.GetMissileSystem();
                        MissileSystem torpedoSys = submarine.GetTorpedoSystem();

                        boolean missileReady = (missileSys != null && missileSys.ReadyToFire());
                        boolean torpedoReady = (torpedoSys != null && torpedoSys.ReadyToFire());

                        if(!(submarine.HasTarget() || submarine.HasGeoTarget()) || !(missileReady || torpedoReady))
                        {
                            submarine.Wait();
                            break;
                        }

                        GeoCoord geoTarget = null;

                        if(submarine.GetTarget() != null && submarine.GetTarget().GetMapEntity(game) != null)
                        {
                            geoTarget = submarine.GetTarget().GetMapEntity(game).GetPosition().GetCopy();
                        }
                        else
                        {
                            if(submarine.GetGeoTarget() != null)
                            {
                                geoTarget = submarine.GetGeoTarget();
                            }
                            else
                            {
                                submarine.Wait();
                                break;
                            }
                        }

                        // Chase: added a way to switch between torpedos and missiles safely.
                        boolean useTorpedoes = false;
                        
                        if(torpedoSys != null && submarine.GetTarget() != null && submarine.GetTarget().GetMapEntity(game) instanceof NavalVessel && submarine.HasTorpedoes() && torpedoSys.GetReadySlotCount() > 0)
                        {
                            float longestTorpRange = config.GetLongestTorpedoRange(torpedoSys);
                            
                            if(geoTarget.DistanceTo(submarine.GetPosition()) <= longestTorpRange)
                            {
                                useTorpedoes = true;
                            }
                        }

                        MissileSystem system = useTorpedoes ? torpedoSys : missileSys;

                        if(system == null || system.GetReadySlotCount() <= 0)
                        {
                            submarine.Wait();
                            break;
                        }

                        boolean bHasUsableTypes = false;

                        if(!useTorpedoes)
                        {
                            if(submarine.GetPosition().DistanceTo(geoTarget) > config.GetLongestMissileRange(system, true))
                            {
                                CreateEventForPlayer(new LaunchEvent(String.format("Your %s has no more missiles with enough range to reach the target. Standing down...", submarine.GetTypeName())), submarine.GetOwnerID());
                                submarine.Wait();
                            }
                        }

                        for(int i = 0; i < system.GetSlotCount(); i++)
                        {
                            if(!system.GetSlotHasMissile(i))
                            {
                                continue;
                            }

                            if(useTorpedoes)
                            {
                                TorpedoType type = config.GetTorpedoType(system.GetSlotMissileType(i));
                                
                                if(type == null)
                                {
                                    continue;
                                }

                                if(type.GetTorpedoRange() < geoTarget.DistanceTo(submarine.GetPosition()))
                                {
                                    continue;
                                }

                                bHasUsableTypes = true;

                                if(system.GetSlotReadyToFire(i))
                                {
                                    CreateTorpedoLaunch(submarine.GetPosition().GetCopy(), type.GetID(), submarine.GetOwnerID(), geoTarget, submarine.GetTarget());
                                    CreateEvent(new LaunchEvent(String.format("%s's %s launched %s.", GetOwner(submarine).GetName(), submarine.GetTypeName(), type.GetName()), SoundEffect.TORPEDO_LAUNCH));
                                    submarine.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                    system.Fire(i);
                                    EntityUpdated(submarine, false);
                                    break;
                                }
                            }
                            else
                            {
                                MissileType type = config.GetMissileType(system.GetSlotMissileType(i));
                                
                                if(type == null || type.GetICBM())
                                {
                                    continue;
                                }

                                boolean antiSubOK = !type.GetAntiSubmarine() || (submarine.GetTarget() != null && submarine.GetTarget().GetMapEntity(game) instanceof Submarine);
                                
                                if(!antiSubOK)
                                {
                                    continue;
                                }

                                if(type.GetMissileRange() < geoTarget.DistanceTo(submarine.GetPosition()))
                                {
                                    continue;
                                }

                                bHasUsableTypes = true;

                                if(system.GetSlotReadyToFire(i))
                                {
                                    if(submarine.Submerged())
                                    {
                                        submarine.DiveOrSurface();
                                        submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                        CreateEventForPlayer(new LaunchEvent("Submarine surfacing for attack...", SoundEffect.SUB_SURFACE), submarine.GetOwnerID());
                                    }

                                    CreateMissileLaunch(submarine.GetPosition().GetCopy(), type.GetID(), submarine.GetOwnerID(), geoTarget, submarine.GetTarget(), false);
                                    CreateEvent(new LaunchEvent(String.format("%s's %s launched %s.", GetOwner(submarine).GetName(), submarine.GetTypeName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));
                                    submarine.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                    system.Fire(i);
                                    EntityUpdated(submarine, false);
                                    break;                                        
                                }
                            }
                        }

                        if(!bHasUsableTypes)
                        {
                            CreateEventForPlayer(new LaunchEvent(String.format("Your %s has no more usable missiles. Standing down...", submarine.GetTypeName())), submarine.GetOwnerID());
                            submarine.Wait();
                            EntityUpdated(submarine, false);
                        }
                    }
                    break;*/
                }
            }
        }
    }
    
    private void ProcessPlayerLandUnitDefences()
    {
        //Start with a list of assets.
        for(ArtilleryGun artillery : GetArtilleryGuns())
        {
            if(artillery.GetOnline())
            {
                Player owner = GetOwner(artillery);
                
                if(owner != null)
                {
                    if(artillery.GetAuto() || (artillery.GetSemiAuto() && !GetPlayerOnline(owner)))
                    {
                        if(artillery.GetMissileSystem() != null && artillery.GetMissileSystem().GetOccupiedSlotCount() > 0 && artillery.GetMissileSystem().ReadyToFire())
                        {
                            ProcessArtilleryAsset(owner, artillery);
                        }
                    }
                } 
            }
        }

        for(Tank artillery : GetHowitzers())
        {
            if(!artillery.GetOnWater() && !artillery.Moving())
            {
                Player owner = GetOwner(artillery);
                
                if(owner != null)
                {
                    if(artillery.GetAuto() || (artillery.GetSemiAuto() && !GetPlayerOnline(owner)))
                    {
                        if(artillery.GetMissileSystem() != null && artillery.GetMissileSystem().GetOccupiedSlotCount() > 0 && artillery.GetMissileSystem().ReadyToFire())
                        {
                            ProcessArtilleryAsset(owner, artillery);
                        }
                    }
                } 
            }
        }        
    }

    private void ProcessArtilleryAsset(Player player, MapEntity asset)
    {
        /**
         * 1. See if any non-friendly land units are near the artillery. 
         * 2. If so, fire, unless we have only chemical shells.
         */
        
        MissileSystem system = null;
        GeoCoord geoPosition = null;
        
        if(asset instanceof ArtilleryGun)
        {
            ArtilleryGun artillery = (ArtilleryGun)asset;
            
            geoPosition = artillery.GetPosition().GetCopy();
            system = artillery.GetMissileSystem();
        }
        else if(asset instanceof Tank)
        {
            Tank howitzer = (Tank)asset;
            
            geoPosition = howitzer.GetPosition().GetCopy();
            system = howitzer.GetMissileSystem();
        }
        
        if(!player.GetRespawnProtected() && system != null && system.ReadyToFire() && asset != null)
        {
            for(EntityPointer pointer : new ArrayList<>(quadtree.GetAffectedLandUnits(geoPosition, Defs.ARTILLERY_RANGE)))
            {
                if(pointer != null)
                {
                    LandUnit landUnit = pointer.GetLandUnit(game);

                    if(landUnit != null)
                    {
                        if(!WouldBeFriendlyFire(GetOwner(landUnit), player) && !GetAttackIsBullying(GetOwner(landUnit), player))
                        {
                            if(landUnit.GetPosition().DistanceTo(geoPosition) <= Defs.ARTILLERY_RANGE)
                            {
                                GeoCoord geoTarget = landUnit.GetPosition().GetCopy();

                                if(landUnit.Moving())
                                {
                                    geoTarget = geoTarget.InterceptPoint(landUnit.GetGeoTarget(), GetLandUnitSpeed(landUnit), geoPosition, Defs.ARTILLERY_SHELL_SPEED);
                                }

                                for(Entry<Integer, Integer> entry : new ArrayList<>(system.GetSlotTypes().entrySet()))
                                {
                                    if(system.GetSlotReadyToFire(entry.getKey()))
                                    {
                                        MissileType type = config.GetMissileType(entry.getValue());

                                        if(type != null)
                                        {
                                            asset.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                            system.Fire(entry.getKey());

                                            system.SetReloadTimeRemaining(Defs.ARTILLERY_GUN_AUTO_RELOAD);

                                            LaunchLog.Log(LaunchLog.LogType.GAME, "artillery", String.format("Artillery Gun %s Firing %s.", String.valueOf(asset.GetID()), type.GetName()));
                                            CreateMissileLaunch(asset, geoPosition, entry.getValue(), player.GetID(), geoTarget, landUnit.GetPointer(), false);
                                            EntityUpdated(asset, false);

                                            CreateEvent(new LaunchEvent(String.format("%s's %s launched %s artillery shell at %s's %s.", player.GetName(), asset.GetTypeName(), type.GetName(), GetOwner(landUnit).GetName(), landUnit.GetTypeName()), SoundEffect.ARTILLERY_FIRE));
                                            CreateReport(GetOwner(landUnit), new LaunchReport(String.format("%s's %s attacked your %s!", player.GetName(), asset.GetTypeName(), type.GetName(), GetOwner(landUnit).GetName(), landUnit.GetTypeName()), true));
                                        }
                                    }                        
                                }             
                            }
                        }
                    }   
                }
            }
        }
    }

    //Process defenses against missiles.
    private void ProcessPlayerMissileDefences()
    {
        if(!Missiles.isEmpty() && !Players.isEmpty())
        {
            for(Missile missile : GetMissiles())
            {
                GeoCoord geoTarget = GetMissileTarget(missile);
                MissileType missileType = config.GetMissileType(missile.GetType());
                
                if(missile.Flying())
                {
                    for(Player player : GetPlayers())
                    {
                        if(missile.GetOwnerID() != player.GetID())
                        {
                            if(ThreatensPlayerOptimised(missile, player, geoTarget, missileType))
                            {
                                Collection<EntityPointer> samSites = new ArrayList<>();
                                Collection<EntityPointer> ships = new ArrayList<>();
                                Collection<EntityPointer> airplanes = new ArrayList<>();
                                Collection<EntityPointer> helicopters = new ArrayList<>();
                                Collection<EntityPointer> tanks = new ArrayList<>();

                                for(SAMSite site : GetSAMSites())
                                {
                                    if(site.GetOwnedBy(player.GetID()))
                                        samSites.add(site.GetPointer());
                                }

                                for(Ship ship : GetShips())
                                {
                                    if(ship.GetOwnedBy(player.GetID()))
                                        ships.add(ship.GetPointer());
                                }

                                for(Airplane plane : GetAirplanes())
                                {
                                    if(plane.GetOwnedBy(player.GetID()))
                                        airplanes.add(plane.GetPointer());
                                }

                                for(Tank tank : GetInterceptorTanks())
                                {
                                    if(tank.GetOwnedBy(player.GetID()) && !tank.GetOnWater())
                                        tanks.add(tank.GetPointer());
                                }

                                ProcessDefenceAssets(player, samSites, missile);
                                ProcessDefenceAssets(player, ships, missile);
                                ProcessDefenceAssets(player, airplanes, missile);
                                ProcessDefenceAssets(player, helicopters, missile);
                                ProcessDefenceAssets(player, tanks, missile);
                            }
                        }
                    }  
                }
            }
        }
    }

    private void ProcessDefenceAssets(Player player, Collection<EntityPointer> pointerAssets, Missile missile)
    {
        for(EntityPointer pointer : pointerAssets)
        {
            MissileSystem system = null;
            GeoCoord geoPosition = null;
            String strAssetName = "";
            String strSource = "";
            EntityPointer ptrLauncher = null;
            boolean bAssetReady = false;

            if(pointer.GetType() == EntityType.SAM_SITE)
            {
                SAMSite samSite = GetSAMSite(pointer.GetID());

                if(samSite == null || samSite.GetManual())
                {
                    continue;
                }

                if((samSite.GetAuto() || (samSite.GetSemiAuto() && !GetPlayerOnline(player))) && samSite.GetOnline())
                {
                    if(missile.GetSpeed() >= samSite.GetEngagementSpeed())
                    {
                        system = samSite.GetInterceptorSystem();
                        geoPosition = samSite.GetPosition();
                        strAssetName = samSite.GetName();
                        strSource = samSite.GetTypeName();
                        bAssetReady = system.ReadyToFire();
                        ptrLauncher = samSite.GetPointer();
                    }
                    else
                        continue;
                }
                else
                    continue;
            }
            else if(pointer.GetType() == EntityType.SHIP)
            {
                Ship ship = GetShip(pointer.GetID());
                
                if(ship == null || !ship.HasInterceptors())
                    continue;

                if((ship.GetAuto() || (ship.GetSemiAuto() && !GetPlayerOnline(player))))
                {
                    system = ship.GetInterceptorSystem();
                    geoPosition = ship.GetPosition();
                    strAssetName = ship.GetName();
                    strSource = ship.GetTypeName();
                    bAssetReady = system.ReadyToFire();
                    ptrLauncher = ship.GetPointer();
                }
                else
                    continue;
            }
            else if(pointer.GetType() == EntityType.AIRPLANE)
            {
                Airplane plane = GetAirplane(pointer.GetID());
                
                if(plane == null || !plane.HasInterceptors())
                    continue;

                if((plane.GetAuto() || (plane.GetSemiAuto() && !GetPlayerOnline(player))))
                {
                    system = plane.GetInterceptorSystem();
                    geoPosition = plane.GetPosition();
                    strAssetName = plane.GetName();
                    strSource = plane.GetTypeName();
                    bAssetReady = system.ReadyToFire();
                    ptrLauncher = plane.GetPointer();
                }
                else
                    continue;
            }
            else if(pointer.GetType() == EntityType.TANK)
            {
                Tank tank = GetTank(pointer.GetID());
                
                if(tank == null || !tank.HasInterceptors())
                    continue;

                if((tank.GetAuto() || (tank.GetSemiAuto() && !GetPlayerOnline(player))))
                {
                    system = tank.GetMissileSystem();
                    geoPosition = tank.GetPosition();
                    strAssetName = tank.GetName();
                    strSource = tank.GetTypeName();
                    bAssetReady = system.ReadyToFire();
                    ptrLauncher = tank.GetPointer();
                }
                else
                    continue;
            }

            if(!bAssetReady)
                continue;
            
            if(missile == null)
            {
                continue;
            }

            MissileType missileType = config.GetMissileType(missile.GetType());
            float fltMissileSpeed = missile.GetSpeed();
            GeoCoord geoTarget = GetMissileTarget(missile);
            int lMissileTimeToTarget = GetTimeToTarget(missile);

            boolean bThreatUnchecked = true;

            for(Interceptor interceptor : Interceptors.values())
            {
                if(interceptor.GetOwnerID() == player.GetID())
                {
                    if(interceptor.GetTargetID() == missile.GetID() && interceptor.GetTargetType() == EntityType.MISSILE)
                    {
                        bThreatUnchecked = false;
                        break;
                    }
                }
            }

            if(bThreatUnchecked)
            {
                TryEngageTarget(player, missile, missileType, fltMissileSpeed, geoTarget, lMissileTimeToTarget, system, geoPosition, strSource, strAssetName, ptrLauncher);
            }
        }
    }

    private void TryEngageTarget(Player player, Missile missile, MissileType missileType, float fltMissileSpeed, GeoCoord geoTarget, int lMissileTimeToTarget, MissileSystem system, GeoCoord geoPosition, String strSource, String strAssetName, EntityPointer ptrLauncher)
    {
        InterceptorType candidateType = null;
        int lCandidateTimeToIntercept = Integer.MAX_VALUE;
        int lSlotNumber = MissileSystem.MISSILE_SLOT_EMPTY_TYPE;

        float fltSAMToMissileDistance = geoPosition.DistanceTo(missile.GetPosition());
        
        for(Entry<Integer, Integer> entry : system.GetSlotTypes().entrySet())
        {
            if(system.GetSlotReadyToFire(entry.getKey()))
            {
                InterceptorType interceptorType = config.GetInterceptorType(entry.getValue());
                
                if(interceptorType != null)
                {
                    float fltInterceptorSpeed = config.GetInterceptorSpeed(interceptorType);
                    
                    if(!missileType.GetStealth() || fltSAMToMissileDistance <= Defs.STEALTH_ENGAGEMENT_DISTANCE)
                    {
                        if(IsInterceptorValidForMissile(interceptorType, missileType, missile))
                        {
                            if(fltSAMToMissileDistance < config.GetInterceptorRange(interceptorType))
                            {
                                GeoCoord geoIntercept = missile.GetPosition().InterceptPoint(geoTarget, fltMissileSpeed, geoPosition, fltInterceptorSpeed);
                                int lTimeToIntercept = GetTimeToTarget(geoPosition, geoIntercept, fltInterceptorSpeed);
                                
                                if(lTimeToIntercept < lMissileTimeToTarget)
                                {
                                    if(candidateType == null || interceptorType.GetCost() < candidateType.GetCost() || (interceptorType.GetCost() == candidateType.GetCost() && lTimeToIntercept < lCandidateTimeToIntercept))
                                    {
                                        candidateType = interceptorType;
                                        lCandidateTimeToIntercept = lTimeToIntercept;
                                        lSlotNumber = entry.getKey();
                                    }
                                }
                            }
                        }
                    }
                }
            }                        
        }

        if(candidateType != null)
        {
            FireInterceptor(player, missile, missileType, candidateType, system, geoPosition, strSource, strAssetName, lCandidateTimeToIntercept, lMissileTimeToTarget, ptrLauncher, lSlotNumber);
        }
    }

    private void FireInterceptor(Player player, Missile missile, MissileType missileType, InterceptorType interceptorType, MissileSystem system, GeoCoord geoPosition, String strSource, String strAssetName, int lTimeToIntercept, int lMissileTimeToTarget, EntityPointer ptrLauncher, int lSlotNumber)
    {
        try
        {
            if(player != null && missile != null && missileType != null && interceptorType != null && system != null && geoPosition != null)
            {
                // Normalize asset name
                if(strAssetName == null || strAssetName.isEmpty())
                {
                    strAssetName = "[Unnamed]";
                }
                
                try
                {
                    if(system.GetSlotHasMissile(lSlotNumber) && system.GetSlotMissileType(lSlotNumber) == interceptorType.GetID())
                    {
                        Player missileOwner = Players.get(missile.GetOwnerID());
                        String missileOwnerName = (missileOwner != null) ? missileOwner.GetName() : "<Unknown>";

                        system.Fire(lSlotNumber);
                        MapEntity entity = ptrLauncher.GetMapEntity(game);

                        if(entity != null)
                        {
                            entity.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                            EntityUpdated(entity, false);
                        }

                        // Create the interceptor entity & link it to the missile target
                        CreateInterceptorLaunch(geoPosition.GetCopy(), interceptorType.GetID(), player.GetID(), missile.GetID(), false, EntityType.MISSILE, LaunchEntity.ID_NONE);
                        CreateEvent(new LaunchEvent(String.format("%s's %s launched %s at %s's %s.", player.GetName(), strSource, interceptorType.GetName(), missileOwnerName, missileType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));

                        // Reports (guard missileOwner==null)
                        CreateReport(player, new LaunchReport(String.format("Your %s engaged %s's missile.", strSource, missileOwnerName), false, player.GetID(), (missileOwner != null ? missileOwner.GetID() : LaunchEntity.ID_NONE)));

                        if(missileOwner != null)
                        {
                            CreateReport(missileOwner, new LaunchReport(String.format("%s's %s engaged your missile.", player.GetName(), strSource), false, player.GetID(), missileOwner.GetID()));
                        }

                        // Log (null-safe)
                        String tgtStr = "<unknown target>";
                        
                        try
                        {
                            tgtStr = GetMissileTarget(missile).toString();
                        }
                        catch(Exception ignored)
                        {
                            
                        }
                        
                        String mPosStr = "<unknown pos>";
                        
                        try
                        {
                            mPosStr = missile.GetPosition().toString();
                        }
                        catch(Exception ignored)
                        {

                        }

                        LaunchLog.Log(LaunchLog.LogType.SAM_SITE_AI, player.GetName(), String.format("%s at %s launched %s at %s's %s, at %s and headed for %s. Time to intercept %d. Missile to target %d.", strAssetName, geoPosition.toString(), interceptorType.GetName(), missileOwnerName, missileType.GetName(), mPosStr, tgtStr, lTimeToIntercept, lMissileTimeToTarget));

                        // Fire only one interceptor per call
                        return;
                    }
                }
                catch(Exception exSlot)
                {
                    LaunchLog.ConsoleMessage("FireInterceptor: slot " + lSlotNumber + " error: " + exSlot);
                }

                LaunchLog.ConsoleMessage("FireInterceptor: no matching interceptor available in system for type " + interceptorType.GetName());
            }
        }
        catch(Exception ex)
        {
            LaunchLog.ConsoleMessage("FireInterceptor failure: " + ex);
        }
    }

    public void ProcessPlayerAircraftDefences()
    {
        if(!Airplanes.isEmpty())
        {
            for(Player player : new ArrayList<>(GetPlayers()))
            {
                if(!player.GetAWOL())
                {
                    for(int lSAMSiteID : new ArrayList<>(player.GetSAMSites()))
                    {
                        SAMSite samSite = SAMSites.get(lSAMSiteID);

                        if(samSite != null && samSite.GetOwnedBy(player.GetID()) && !samSite.GetManual())
                        {
                            MissileSystem samSystem = samSite.GetInterceptorSystem();

                            if((samSite.GetAuto() || (samSite.GetSemiAuto() && !GetPlayerOnline(player))) && samSite.GetOnline() && samSystem.ReadyToFire())
                            {
                                //Player is under attack, so check for hostile aircraft inside our interceptor range.
                                for(Airplane aircraft : new ArrayList<>(GetAirplanes()))
                                {
                                    if(aircraft != null)
                                    {
                                        if(!aircraft.Destroyed() && aircraft.GetVisible())
                                        {
                                            if(AircraftIsHostile(aircraft, player))
                                            {
                                                if(!AircraftThreatChecked(aircraft, player))
                                                {
                                                    //Prosecute the aircraft if possible using the best available (cheapest and nearest).
                                                    SAMSite candidateSite = null;
                                                    InterceptorType candidateType = null;
                                                    int lCandidateTimeToIntercept = Integer.MAX_VALUE;
                                                    float fltSAMToAircraftDistance = samSite.GetPosition().DistanceTo(aircraft.GetPosition());

                                                    if(!aircraft.GetStealth() || fltSAMToAircraftDistance <= Defs.STEALTH_ENGAGEMENT_DISTANCE)
                                                    {
                                                        for(Entry<Integer, Integer> entry : new ArrayList<>(samSystem.GetSlotTypes().entrySet()))
                                                        {
                                                            if(samSystem.GetSlotReadyToFire(entry.getKey()))
                                                            {
                                                                int lType = entry.getValue();
                                                                InterceptorType interceptorType = config.GetInterceptorType(lType);
                                                                float fltInterceptorSpeed = config.GetInterceptorSpeed(interceptorType);

                                                                //Is the missile within this interceptor's range?
                                                                if(fltSAMToAircraftDistance <= config.GetInterceptorRange(interceptorType))
                                                                {                                                                                          
                                                                    if(!interceptorType.GetABM())
                                                                    {
                                                                        if(candidateType != null)
                                                                        {
                                                                            //We already have a candidate. Is this cheaper or nearer?
                                                                            if(interceptorType.GetCost() < candidateType.GetCost())
                                                                            {
                                                                                //Cheaper. Accept if it can prosecute the missile.
                                                                                int lTimeToIntercept = GetTimeToTarget(samSite.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                                candidateSite = samSite;
                                                                                candidateType = interceptorType;
                                                                                lCandidateTimeToIntercept = lTimeToIntercept;
                                                                            }
                                                                            if(interceptorType.GetCost() == candidateType.GetCost())
                                                                            {
                                                                                //The same price. Accept if it can get there quicker.
                                                                                int lTimeToIntercept = GetTimeToTarget(samSite.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                                if(lCandidateTimeToIntercept < lTimeToIntercept)
                                                                                {
                                                                                    candidateSite = samSite;
                                                                                    candidateType = interceptorType;
                                                                                    lCandidateTimeToIntercept = lTimeToIntercept;
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            //No current candidate. This is good enough if it can reach.
                                                                            int lTimeToIntercept = GetTimeToTarget(samSite.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                            candidateSite = samSite;
                                                                            candidateType = interceptorType;
                                                                            lCandidateTimeToIntercept = lTimeToIntercept;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }      
                                                    }

                                                    if(candidateSite != null)
                                                    {
                                                        //Launch the bastard.
                                                        MissileSystem system = candidateSite.GetInterceptorSystem();

                                                        for(int lSlotNumber = 0; lSlotNumber < system.GetSlotCount(); lSlotNumber++)
                                                        {
                                                            if(system.GetSlotHasMissile(lSlotNumber) && (system.GetSlotMissileType(lSlotNumber) == candidateType.GetID()))
                                                            {
                                                                //Fire.
                                                                Player bomberOwner = Players.get(aircraft.GetOwnerID());
                                                                candidateSite.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                                system.Fire(lSlotNumber);
                                                                CreateInterceptorLaunch(candidateSite.GetPosition().GetCopy(), candidateType.GetID(), player.GetID(), aircraft.GetID(), false, EntityType.AIRPLANE, LaunchEntity.ID_NONE);

                                                                SendUserAlert(bomberOwner.GetUser(), "Interceptor Inbound", String.format("%s's SAM fired at your %s!", player.GetName(), aircraft.GetTypeName()), true, false);

                                                                CreateEvent(new LaunchEvent(String.format("%s's SAM launched %s at %s's %s.", player.GetName(), candidateType.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));
                                                                CreateReport(player, new LaunchReport(String.format("Your SAM engaged %s's %s.", bomberOwner.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));
                                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's SAM engaged your %s.", player.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));

                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            player.RemoveSAMSite(lSAMSiteID);
                        }
                    }

                    for(int lShipID : new ArrayList<>(player.GetShips()))
                    {
                        Ship ship = Ships.get(lShipID);

                        if(ship != null && ship.GetOwnedBy(player.GetID()) && ship.HasInterceptors())
                        {
                            MissileSystem samSystem = ship.GetInterceptorSystem();

                            if((ship.GetAuto() || (ship.GetSemiAuto() && !GetPlayerOnline(player))) && samSystem.ReadyToFire())
                            {
                                for(Airplane aircraft : new ArrayList<>(GetAirplanes()))
                                {
                                    if(aircraft != null)
                                    {
                                        if(!aircraft.Destroyed() && aircraft.GetVisible())
                                        {
                                            if(AircraftIsHostile(aircraft, player))
                                            {
                                                if(!AircraftThreatChecked(aircraft, player))
                                                {
                                                    //Prosecute the aircraft if possible using the best available (cheapest and nearest).
                                                    Ship candidateShip = null;
                                                    InterceptorType candidateType = null;
                                                    int lCandidateTimeToIntercept = Integer.MAX_VALUE;
                                                    float fltShipToAircraftDistance = ship.GetPosition().DistanceTo(aircraft.GetPosition());

                                                    if(!aircraft.GetStealth() || fltShipToAircraftDistance <= Defs.STEALTH_ENGAGEMENT_DISTANCE)
                                                    {
                                                        for(Entry<Integer, Integer> entry : samSystem.GetSlotTypes().entrySet())
                                                        {
                                                            if(samSystem.GetSlotReadyToFire(entry.getKey()))
                                                            {
                                                                int lType = entry.getValue();
                                                                InterceptorType interceptorType = config.GetInterceptorType(lType);
                                                                float fltInterceptorSpeed = config.GetInterceptorSpeed(interceptorType);

                                                                //Is the missile within this interceptor's range?
                                                                if(fltShipToAircraftDistance <= config.GetInterceptorRange(interceptorType))
                                                                {                                                                                          
                                                                    if(!interceptorType.GetABM())
                                                                    {
                                                                        if(candidateType != null)
                                                                        {
                                                                            //We already have a candidate. Is this cheaper or nearer?
                                                                            if(interceptorType.GetCost() < candidateType.GetCost())
                                                                            {
                                                                                //Cheaper. Accept if it can prosecute the missile.
                                                                                int lTimeToIntercept = GetTimeToTarget(ship.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                                candidateShip = ship;
                                                                                candidateType = interceptorType;
                                                                                lCandidateTimeToIntercept = lTimeToIntercept;
                                                                            }
                                                                            if(interceptorType.GetCost() == candidateType.GetCost())
                                                                            {
                                                                                //The same price. Accept if it can get there quicker.
                                                                                int lTimeToIntercept = GetTimeToTarget(ship.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                                if(lCandidateTimeToIntercept < lTimeToIntercept)
                                                                                {
                                                                                    candidateShip = ship;
                                                                                    candidateType = interceptorType;
                                                                                    lCandidateTimeToIntercept = lTimeToIntercept;
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            //No current candidate. This is good enough if it can reach.
                                                                            int lTimeToIntercept = GetTimeToTarget(ship.GetPosition(), aircraft.GetPosition(), fltInterceptorSpeed);

                                                                            candidateShip = ship;
                                                                            candidateType = interceptorType;
                                                                            lCandidateTimeToIntercept = lTimeToIntercept;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if(candidateShip != null)
                                                    {
                                                        //Launch the bastard.
                                                        MissileSystem system = candidateShip.GetInterceptorSystem();

                                                        for(int lSlotNumber = 0; lSlotNumber < system.GetSlotCount(); lSlotNumber++)
                                                        {
                                                            if(system.GetSlotHasMissile(lSlotNumber) && (system.GetSlotMissileType(lSlotNumber) == candidateType.GetID()))
                                                            {
                                                                //Fire.
                                                                Player bomberOwner = Players.get(aircraft.GetOwnerID());
                                                                candidateShip.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                                system.Fire(lSlotNumber);
                                                                CreateInterceptorLaunch(candidateShip.GetPosition().GetCopy(), candidateType.GetID(), player.GetID(), aircraft.GetID(), false, EntityType.AIRPLANE, LaunchEntity.ID_NONE);

                                                                SendUserAlert(bomberOwner.GetUser(), "Interceptor Inbound", String.format("%s's ship fired at your %s!", player.GetName(), aircraft.GetTypeName()), true, false);

                                                                CreateEvent(new LaunchEvent(String.format("%s's ship launched %s at %s's %s.", player.GetName(), candidateType.GetName(), bomberOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));
                                                                CreateReport(player, new LaunchReport(String.format("Your ship engaged %s's %s.", bomberOwner.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));
                                                                CreateReport(bomberOwner, new LaunchReport(String.format("%s's ship engaged your %s.", player.GetName(), aircraft.GetTypeName()), false, player.GetID(), bomberOwner.GetID()));

                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean AttackerIsATroll(Player attacker, Player defender)
    {
        return GetNetWorthMultiplier(attacker, defender) < 0.15f && attacker.GetKDR() < 0.1f;
    }
    
    private void ProcessMAD(Player attacker, Player defender, String strCause)
    {   
        //Oh sweetie, we're not going to retaliate against our own allies.
        if(!EntityIsFriendly(attacker, defender) && !AttackerIsATroll(attacker, defender))
        {
            //Process the defender's auto-mode silos response.
            List<MissileSite> PotentialSilos = new ArrayList<>();
            
            for(Structure structure : new ArrayList<>(defender.GetStructures()))
            {
                if(structure instanceof MissileSite site && site.CanTakeICBM() && site.GetOnline())
                {
                    if(site.GetMissileSystem().ReadyToFire() && (site.GetAuto() || (site.GetSemiAuto() && !GetPlayerOnline(defender))))
                        PotentialSilos.add(((MissileSite)structure));
                }
            }

            if(PotentialSilos.isEmpty())
            {
                //The defender has no applicable silos. See if they have allies that can help.
                
                for(Player defenderAlly : new ArrayList<>(GetPlayerAlliesAndSelf(defender)))
                {
                    if(!defender.ApparentlyEquals(defenderAlly))
                    {
                        for(Structure structure : new ArrayList<>(defenderAlly.GetStructures()))
                        {
                            if(structure instanceof MissileSite site && site.CanTakeICBM() && site.GetOnline() && site.GetMissileSystem().ReadyToFire())
                            {
                                if(site.GetAuto() || (site.GetSemiAuto() && !GetPlayerOnline(defenderAlly)))
                                    PotentialSilos.add(((MissileSite)structure));
                            }
                        }
                    } 
                }
            }

            if(!PotentialSilos.isEmpty())
            {
                MissileSite candidateSilo = null;
                MissileType candidateType = null;
                int lCandidateCitySize = Integer.MIN_VALUE;
                Player launcher = null;
                
                //TODO: Figure out something other than cities for ICBM silos to target. Clusters of civilian/military stuff?
            }   
        }  
    }
    
    //Process damage to entities caused by a missile explosion.
    private void ProcessExplosion(Player inflictor, String strCause, GeoCoord geoOrigin, Explosion explosion)
    {
        boolean bReestablishStructureThreats = false;
        short nDamage;
        short nDamageInflicted;
        
        float fltBlastRadius = MissileStats.GetBlastRadius(explosion);
        
        if(explosion.GetNuclear() && !explosion.GetAirburst())
        {
            CreateRadiation(geoOrigin, fltBlastRadius, inflictor.GetID());
        }
        
        //Keep a list of bunkers damaged during the player-damage phase so that bunkers don't get double-damage.
        List<CommandPost> DamagedCommandPosts = new ArrayList<>();
        
        int lTotalDamage = 0;

        //Structures.
        for(Structure structure : GetAllStructures())
        {
            if(!(structure instanceof CommandPost) || !DamagedCommandPosts.contains((CommandPost)structure))
            {
                if(!structure.Destroyed() && !structure.GetRespawnProtected())
                {
                    float fltDistance = structure.GetPosition().DistanceTo(geoOrigin);

                    if(fltDistance <= fltBlastRadius)
                    {
                        nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageAtPosition(structure, geoOrigin, explosion));

                        nDamageInflicted = structure.InflictDamage(nDamage);
                        lTotalDamage += nDamageInflicted;
                        Player owner = GetPlayer(structure.GetOwnerID());
                        Scoring_DamageInflicted(inflictor, owner, nDamageInflicted);
                        
                        if(structure instanceof MissileSite site && site.CanTakeICBM())
                        {
                            if(random.nextFloat() <  (explosion.GetNuclear() && !explosion.GetICBM() ? Defs.NUKE_ICBM_SILO_MAD_CHANCE : Defs.ATTACK_ICBM_SILO_MAD_CHANCE))
                            {
                                ProcessMAD(inflictor, GetOwner(site), String.format("%s attacked %s's %s.", inflictor.GetName(), owner.GetName(), structure.GetTypeName()));
                            }
                        }
                        else if(explosion.GetNuclear() && !explosion.GetICBM() && (structure instanceof Processor || structure instanceof OreMine || structure instanceof Warehouse))
                        {
                            if(random.nextFloat() < Defs.NUKE_CIVILIAN_STRUCTURE_MAD_CHANCE)
                            {
                                ProcessMAD(inflictor, GetOwner(structure), String.format("%s nuked %s's civilian structures.", inflictor.GetName(), owner.GetName()));
                            }
                        }

                        if(structure.Destroyed())
                        {
                            if(!explosion.GetNuclear() || random.nextFloat() <= 0.5f)
                                CreateRubble(structure);
                            
                            ProcessPlayerXPGain(inflictor.GetID(), Defs.STRUCTURE_KILLED_XP, String.format("You destroyed a %s.", structure.GetTypeName()));
                            ProcessPlayerXPLoss(owner.GetID(), Defs.STRUCTURE_LOST_XP, String.format("Your %s was destroyed.", structure.GetTypeName()));

                            bReestablishStructureThreats = true;
                            CreateEvent(new LaunchEvent(String.format("%s hit %s's %s, causing %d HP of damage and destroying it.", strCause, owner.GetName(), structure.GetTypeName(), nDamageInflicted)));
                            CreateReport(owner, new LaunchReport(String.format("%s destroyed your %s!", inflictor.GetName(), structure.GetTypeName()), true, owner.GetID(), inflictor.GetID()));
                            CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s's %s!", owner.GetName(), structure.GetTypeName()), true, inflictor.GetID(), owner.GetID()));

                            CreateSalvage(structure.GetPosition().GetCopy(), GetSaleValue(structure));

                            //Process destroyed cargo. 
                            CargoSystem system = null;

                            if(structure instanceof HaulerInterface)
                            {
                                if(structure instanceof Warehouse warehouse)
                                {
                                    //ScatterLoot(structure.GetPosition(), warehouse.GetCargoSystem());
                                }
                                else
                                {
                                    system = ((HaulerInterface)structure).GetCargoSystem();
                                } 
                            }

                            CargoSystemDestroyed(LaunchEntity.ID_NONE, null, structure.GetPosition().GetCopy(), system, structure.GetResourceSystem());
                        }
                    }
                }
            }
        }
        
        //Shipyards.
        for(Shipyard shipyard : GetShipyards())
        {
            if(!shipyard.Destroyed())
            {
                float fltDistance = shipyard.GetPosition().DistanceTo(geoOrigin);

                if(fltDistance <= fltBlastRadius)
                {
                    nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageAtPosition(shipyard, geoOrigin, explosion));
                    nDamageInflicted = shipyard.InflictDamage(nDamage);
                    lTotalDamage += nDamageInflicted;
                    Player owner = GetPlayer(shipyard.GetOwnerID());
                    
                    if(owner != null)
                    {
                        Scoring_DamageInflicted(inflictor, owner, nDamageInflicted);
                    }

                    if(shipyard.Destroyed())
                    {
                        ProcessPlayerXPGain(inflictor.GetID(), Defs.CITY_KILLED_XP, String.format("You destroyed %s!", shipyard.GetName()));
                        
                        if(owner != null)
                        {
                            ProcessPlayerXPLoss(owner.GetID(), Defs.CITY_LOST_XP, String.format("%s was destroyed!", shipyard.GetName()));
                        }
                        
                        bReestablishStructureThreats = true;
                        CreateEvent(new LaunchEvent(String.format("%s hit %s, causing %d HP of damage and destroying it.", strCause, shipyard.GetName(), nDamageInflicted)));
                        
                        CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s!", shipyard.GetName()), true, inflictor.GetID()));
                        
                        if(!shipyard.GetQueue().isEmpty())
                        {
                            for(ShipProductionOrder order : shipyard.GetQueue())
                            {
                                Player player = GetPlayer(order.GetProducingForID());

                                if(player != null)
                                {
                                    player.AddWealth(Defs.GetNavalBuildCost(order.GetTypeUnderConstruction()).get(ResourceType.WEALTH));
                                }
                            } 
                            
                            shipyard.ResetShipyard();
                            EntityUpdated(shipyard, false);
                        }
                    }
                }
            }
        }
        
        for(Infantry infantry : GetInfantries())
        {
            if(!GetInfantryIsSheltered(infantry) && infantry.GetPosition().RadiusPhaseCollisionTest(geoOrigin, fltBlastRadius))
            {
                if(infantry.GetPosition().DistanceTo(geoOrigin) <= fltBlastRadius)
                {
                    nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageToLandUnit(infantry, geoOrigin, explosion));
                    
                    nDamageInflicted = infantry.InflictDamage(nDamage);
                    lTotalDamage += nDamageInflicted;
                    infantry.SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                    Player owner = GetOwner(infantry);
                    Scoring_DamageInflicted(inflictor, owner, nDamageInflicted); 
                    
                    if(owner != null)
                    {
                        if(infantry.Destroyed())
                        {
                            ProcessPlayerXPGain(inflictor.GetID(), Defs.INFANTRY_KILLED_XP, "You killed an infantry unit.");
                            ProcessPlayerXPLoss(owner.GetID(), Defs.INFANTRY_LOST_XP, "Your infantry was destroyed.");

                            CreateEvent(new LaunchEvent(String.format("%s hit %s's infantry, causing %d HP of damage and destroying it.", strCause, owner.GetName(), nDamageInflicted)));
                            CreateReport(owner, new LaunchReport(String.format("%s destroyed your infantry!", inflictor.GetName()), true, owner.GetID(), inflictor.GetID()));
                            CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s's infantry!", owner.GetName()), true, inflictor.GetID(), owner.GetID()));
                        }
                    }
                }
            }
        }
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetPosition().DistanceTo(geoOrigin) <= fltBlastRadius)
            {
                nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageToLandUnit(tank, geoOrigin, explosion));
                
                nDamageInflicted = tank.InflictDamage(nDamage);
                lTotalDamage += nDamageInflicted;
                tank.SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                Player owner = GetOwner(tank);
                Scoring_DamageInflicted(inflictor, owner, nDamageInflicted); 

                if(owner != null)
                {
                    if(tank.Destroyed())
                    {
                        ProcessPlayerXPGain(inflictor.GetID(), Defs.TANK_KILLED_XP, "You killed a tank.");
                        ProcessPlayerXPLoss(owner.GetID(), Defs.TANK_LOST_XP, "Your tank was destroyed.");

                        CreateEvent(new LaunchEvent(String.format("%s hit %s's tank, causing %d HP of damage and destroying it.", strCause, owner.GetName(), nDamageInflicted)));
                        CreateReport(owner, new LaunchReport(String.format("%s destroyed your tank!", inflictor.GetName()), true, owner.GetID(), inflictor.GetID()));
                        CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s's tank!", owner.GetName()), true, inflictor.GetID(), owner.GetID()));
                    }
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(!truck.Destroyed())
            {
                if(truck.GetPosition().DistanceTo(geoOrigin) <= fltBlastRadius)
                {
                    nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageToLandUnit(truck, geoOrigin, explosion));
                    
                    nDamageInflicted = truck.InflictDamage(nDamage);
                    lTotalDamage += nDamageInflicted;
                    truck.SetUnderAttack(Defs.UNDER_ATTACK_TIME);
                    Player owner = GetOwner(truck);
                    Scoring_DamageInflicted(inflictor, owner, nDamageInflicted); 

                    if(owner != null)
                    {
                        if(truck.Destroyed())
                        {
                            CargoSystemDestroyed(truck.GetOwnerID(), truck, truck.GetPosition(), truck.GetCargoSystem(), truck.GetResourceSystem());
                            ProcessPlayerXPGain(inflictor.GetID(), Defs.TRUCK_KILLED_XP, "You killed a truck.");
                            ProcessPlayerXPLoss(owner.GetID(), Defs.TRUCK_LOST_XP, "Your truck was destroyed.");

                            CreateEvent(new LaunchEvent(String.format("%s hit %s's truck, causing %d HP of damage and destroying it.", strCause, owner.GetName(), nDamageInflicted)));
                            CreateReport(owner, new LaunchReport(String.format("%s destroyed your truck!", inflictor.GetName()), true, owner.GetID(), inflictor.GetID()));
                            CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s's truck!", owner.GetName()), true, inflictor.GetID(), owner.GetID()));
                        }
                    }
                }
            }  
        }
        
        //Ships.
        for(Ship ship : GetShips())
        {
            if(!ship.Destroyed())
            {
                float fltDistance = ship.GetPosition().DistanceTo(geoOrigin);

                if(fltDistance <= fltBlastRadius)
                {   
                    nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageAtPosition(ship, geoOrigin, explosion));
                    
                    if(!explosion.GetAntiShip() && !explosion.GetNuclear())
                    {
                        nDamage *= Defs.NON_ANTISHIP_DAMAGE_MULTIPLIER;
                    }
                    
                    nDamageInflicted = ship.InflictDamage(nDamage);
                    lTotalDamage += nDamageInflicted;
                    Player owner = GetPlayer(ship.GetOwnerID());
                    Scoring_DamageInflicted(inflictor, owner, nDamageInflicted);

                    if(ship.Destroyed())
                    {
                        ProcessPlayerXPGain(inflictor.GetID(), Defs.GetXPGainForKill(ship), String.format("You sank a %s.", ship.GetTypeName()));
                        ProcessPlayerXPLoss(owner.GetID(), Defs.GetXPLossForKill(ship), String.format("Your %s was sunk.", ship.GetTypeName()));
                        
                        bReestablishStructureThreats = true;
                        CreateEvent(new LaunchEvent(String.format("%s hit %s's %s, causing %d HP of damage and sinking it.", strCause, owner.GetName(), ship.GetTypeName(), nDamageInflicted)));
                        CreateReport(owner, new LaunchReport(String.format("%s sank your %s!", inflictor.GetName(), ship.GetTypeName()), true, owner.GetID(), inflictor.GetID()));
                        CreateReport(inflictor, new LaunchReport(String.format("You sank %s's %s!", owner.GetName(), ship.GetTypeName()), true, inflictor.GetID(), owner.GetID()));
                    }
                }
            }
        }
        
        //Submarines.
        for(Submarine submarine : GetSubmarines())
        {
            if(!submarine.Destroyed())
            {
                if(explosion.GetAntiSubmarine() || !submarine.Submerged() || (explosion.GetNuclear() && !explosion.GetAirburst()))
                {
                    float fltDistance = submarine.GetPosition().DistanceTo(geoOrigin);

                    if(fltDistance <= fltBlastRadius)
                    {   
                        nDamage = (short)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageAtPosition(submarine, geoOrigin, explosion));
                        
                        nDamageInflicted = submarine.InflictDamage(nDamage);
                        lTotalDamage += nDamageInflicted;
                        Player owner = GetPlayer(submarine.GetOwnerID());
                        Scoring_DamageInflicted(inflictor, owner, nDamageInflicted);

                        if(submarine.Destroyed())
                        {
                            ProcessPlayerXPGain(inflictor.GetID(), Defs.GetXPGainForKill(submarine), String.format("You sank a submarine."));
                            ProcessPlayerXPLoss(owner.GetID(), Defs.GetXPLossForKill(submarine), String.format("Your submarine was sunk."));

                            bReestablishStructureThreats = true;
                            CreateEvent(new LaunchEvent(String.format("%s hit %s's submarine, causing %d HP of damage and sinking it.", strCause, owner.GetName(), nDamageInflicted)));
                            CreateReport(owner, new LaunchReport(String.format("%s sank your submarine!", inflictor.GetName()), true, owner.GetID(), inflictor.GetID()));
                            CreateReport(inflictor, new LaunchReport(String.format("You sank %s's submarine!", owner.GetName()), true, inflictor.GetID(), owner.GetID()));
                        }
                    }
                } 
            }
        }
        
        for(Torpedo torpedo : GetTorpedoes())
        {
            float fltDistance = torpedo.GetPosition().DistanceTo(geoOrigin);
            
            if(fltDistance <= fltBlastRadius)
            {
                if(random.nextFloat() <= Defs.TORPEDO_DESTROY_CHANCE)
                {
                    if(fltDistance <= fltBlastRadius)
                    {
                        torpedo.SetDetonate();
                        Player owner = GetOwner(torpedo);

                        CreateEvent(new LaunchEvent(String.format("%s destroyed %s's torpedo!", strCause, owner.GetName())));
                        CreateReport(owner, new LaunchReport(String.format("%s destroyed your torpedo!", inflictor.GetName()), true, owner.GetID(), inflictor.GetID()));
                        CreateReport(inflictor, new LaunchReport(String.format("You destroyed %s's torpedo!", owner.GetName()), true, inflictor.GetID(), owner.GetID()));
                    }
                }
            } 
        }
        
        //Structures.
        for(ResourceDeposit deposit : GetResourceDeposits())
        {
            if(!deposit.Depleted())
            {
                float fltDistance = deposit.GetPosition().DistanceTo(geoOrigin);

                if(fltDistance <= fltBlastRadius)
                {
                    long oOutput = (long)(LaunchUtilities.GetRandomFloatInBounds(explosion.GetAccuracy(), 1.0f) * MissileStats.GetDamageAtPosition(deposit, geoOrigin, explosion) * Defs.DEPOSIT_DMG_EXTRACTION_MULTIPLIER);
                    long oFinalOutput = deposit.Extract(oOutput);
                    
                    CreateLoot(deposit.GetPosition().GetCopy(), LootType.RESOURCES, deposit.GetType().ordinal(), oFinalOutput, Defs.LOOT_EXPIRY);
                }
            }
        }
        
        if(bReestablishStructureThreats)
        {
            EstablishAllStructureThreats(LaunchEntity.ID_NONE);
        }
        
        if(lTotalDamage > 0)
        {
            //The explosion damaged the player.
            CreateEvent(new LaunchEvent(String.format("%s exploded, causing %d total HP of damage.", strCause, lTotalDamage), SoundEffect.EXPLOSION));
            CreateReport(inflictor, new LaunchReport(String.format("Your %s exploded, causing %d total HP of damage.", strCause, lTotalDamage), true, inflictor.GetID()));
        }
    }
    
    //Process an EMP pulse.
    private void CreateEMPPulse(GeoCoord geoLocation, float fltRadius, int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        for(Structure structure : GetAllStructures())
        {
            if(structure != null && !structure.GetRespawnProtected() && (structure.GetBooting() || structure.GetOnline()))
            {  
                if(random.nextFloat() < 0.5f)
                {
                    if(geoLocation.DistanceTo(structure.GetPosition()) < fltRadius)
                    {
                        Player victim = GetOwner(structure);
                        
                        int lRebootTime = config.GetStructureBootTime(player);
                        
                        if(victim != null)
                        {
                            structure.Reboot(lRebootTime);
                            CreateReport(victim, new LaunchReport(String.format("%s's EMP pulse caused your %s to reboot.", player.GetName(), structure.GetTypeName()), true, player.GetID(), victim.GetID()));
                            CreateReport(player, new LaunchReport(String.format("Your EMP pulse caused %s's %s to reboot.", victim.GetName(), structure.GetTypeName()), true, player.GetID(), victim.GetID()));
                        }
                        else
                        {
                            structure.Reboot(lRebootTime);
                        }
                        
                        EntityUpdated(structure, false);
                    }
                }
            }
        }
        
        for(Airplane aircraft : GetAirplanes())
        {
            if(aircraft.GetPosition().DistanceTo(geoLocation) <= fltRadius)
            {
                if(random.nextFloat() < 0.3f)
                {
                    aircraft.InflictDamage((short)1);

                    if(aircraft.Destroyed())
                    {
                        Player aircraftOwner = GetOwner(aircraft);
                        CreateEvent(new LaunchEvent(String.format("%s's %s was destroyed by %s's EMP pulse.", aircraftOwner.GetName(), aircraft.GetTypeName(), player.GetName()), SoundEffect.INTERCEPTOR_HIT));
                        CreateReport(player, new LaunchReport(String.format("Your EMP pulse destroyed %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false));               
                        CreateReport(aircraftOwner, new LaunchReport(String.format("%s's EMP pulse destroyed your %s.", player.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), player.GetID()));
                        CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));                    
                    }  
                }
            }
        }
    }
    
    /**
     * Increment offence spending when a player incurs a maintenance cost of an offensive structure.
     * Costs associated with firing weapons should be dealt with in the weapon launch code.
     * @param player The player incurring the maintenance cost.
     * @param lAmount The maintenance cost.
     */
    private void Scoring_OffenceSpending(Player player, long oAmount)
    {
        player.AddOffenceSpending((int)oAmount);
        
        //Add the amount to any wars involving this player.
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            int lAllianceID = player.GetAllianceMemberID();
            
            List<War> AffectedWars = new ArrayList();
            
            for(Treaty treaty : GetTreaties())
            {
                if(treaty instanceof War)
                {
                    if(treaty.IsAParty(lAllianceID))
                        AffectedWars.add((War)treaty);
                }
            }
            
            //Let it round down. I'm not being generous.
            int lAmountPerWar = (int)((float)oAmount/(float)AffectedWars.size());
            
            if(lAmountPerWar > 0)
            {
                for(War war : AffectedWars)
                {
                    war.AddOffenceSpending(lAllianceID, lAmountPerWar);
                }
            }
        }
    }
    
    /**
     * Increment defence spending when a player fires a defensive weapon.
     * @param player The player that fired a defensive weapons.
     * @param aggressor The player who's weapon is being prosecuted.
     * @param lAmount The cost of the defensive weapon.
     */
    private void Scoring_DefenceSpending(Player player, Player aggressor, long oAmount)
    {
        player.AddDefenceSpending((int)oAmount);
        
        //Add the amount to any wars involving both players.
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && aggressor.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            for(Treaty treaty : GetTreaties())
            {
                int lAllianceIDPlayer = player.GetAllianceMemberID();
                int lAllianceIDAggressor = aggressor.GetAllianceMemberID();
            
                if(treaty instanceof War)
                {
                    if(treaty.AreParties(lAllianceIDPlayer, lAllianceIDAggressor))
                    {
                        ((War)treaty).AddDefenceSpending(lAllianceIDPlayer, (int)oAmount);
                    }
                }
            }
        }
    }
    
    /**
     * Increment defence spending when a player incurs a maintenance cost of a defensive structure.
     * @param player The player incurring the maintenance cost.
     * @param lAmount The maintenance cost.
     */
    private void Scoring_DefenceSpending(Player player, long oAmount)
    {
        player.AddDefenceSpending((int)oAmount);
        
        //Add the amount to any wars involving this player.
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            int lAllianceID = player.GetAllianceMemberID();
            
            List<War> AffectedWars = new ArrayList();
            
            for(Treaty treaty : GetTreaties())
            {
                if(treaty instanceof War)
                {
                    if(treaty.IsAParty(lAllianceID))
                        AffectedWars.add((War)treaty);
                }
            }
            
            //Let it round down. I'm not being generous.
            int lAmountPerWar = (int)((float)oAmount/(float)AffectedWars.size());
            
            if(lAmountPerWar > 0)
            {
                for(War war : AffectedWars)
                {
                    war.AddOffenceSpending(lAllianceID, lAmountPerWar);
                }
            }
        }
    }
    
    private void Scoring_DamageInflicted(Player inflictor, Player inflictee, short nAmount)
    {
        inflictor.AddDamageInflicted(nAmount);
        inflictee.AddDamageReceived(nAmount);
        
        if(inflictor.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && inflictee.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            int lAllianceIDInflictor = inflictor.GetAllianceMemberID();
            int lAllianceIDInflictee = inflictee.GetAllianceMemberID();
            
            for(Treaty treaty : GetTreaties())
            {
                if(treaty instanceof War)
                {
                    if(treaty.AreParties(lAllianceIDInflictor, lAllianceIDInflictee))
                    {
                        ((War)treaty).AddDamageInflicted(lAllianceIDInflictor, nAmount);
                    }
                }
            }
        }
    }
    
    /**
     * Add standard income to any wars involving a player. Standard income is hourly generated wealth, not that from bonuses or loots.
     * This exists to effectively and unhackably handicap alliances by size (total wealth accumulation during a war).
     * @param player Player earning their standard income.
     * @param lAmount The amount of it.
     */
    private void Scoring_StandardIncomeReceived(Player player, int lAmount)
    {
        //Add the amount to any wars involving this player.
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            int lAllianceID = player.GetAllianceMemberID();
            
            for(Treaty treaty : GetTreaties())
            {
                if(treaty instanceof War)
                {
                    if(treaty.IsAParty(lAllianceID))
                        ((War)treaty).AddIncome(lAllianceID, lAmount);
                }
            }
        }
    }
    
    private List<Integer> GetAvailableInterceptors(MissileSystem missileSystem)
    {
        List result = new ArrayList();
        
        for(int c = 0; c < missileSystem.GetSlotCount(); c++)
        {
            if(missileSystem.GetSlotHasMissile(c))
            {
                if(missileSystem.GetSlotReadyToFire(c))
                {
                    int lType = missileSystem.GetSlotMissileType(c);

                    if(!result.contains(lType))
                    {
                        result.add(lType);
                    }
                }
            }
        }
        
        return result;
    }
    
    private List<Integer> GetAvailableMissiles(MissileSystem missileSystem)
    {
        List result = new ArrayList();
        
        for(int c = 0; c < missileSystem.GetSlotCount(); c++)
        {
            if(missileSystem.GetSlotHasMissile(c))
            {
                if(missileSystem.GetSlotReadyToFire(c))
                {
                    int lType = missileSystem.GetSlotMissileType(c);

                    if(!result.contains(lType))
                    {
                        result.add(lType);
                    }
                }
            }
        }
        
        return result;
    }
    
    private void CreateAlliance(Player creator, String strName, String strDescription, int lAvatarID)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating an alliance.");

        int lID = GetAtomicID(lAllianceIndex, Alliances);

        Alliance alliance = new Alliance(lID, strName, strDescription, lAvatarID, creator.GetName());
        AddAlliance(alliance, false);

        creator.SetAllianceID(lID);
        creator.SetIsAnMP(true);
    }
    
    private void RemoveExistingTreaties(int lAlliance1, int lAlliance2)
    {
        for(Treaty treaty : GetTreaties())
        {
            if((treaty.GetAllianceID1() == lAlliance1 && treaty.GetAllianceID2() == lAlliance2) ||
               (treaty.GetAllianceID1() == lAlliance2 && treaty.GetAllianceID2() == lAlliance1))
            {
                Treaties.remove(treaty.GetID());
                TreatyRemoved(treaty);
            }
        }
    }
    
    private void CreateWar(int lAllianceID1, int lAllianceID2)
    {
        RemoveExistingTreaties(lAllianceID1, lAllianceID2);

        War war = new War(GetAtomicID(lTreatyIndex, Treaties), lAllianceID1, lAllianceID2);
        AddTreaty(war);
    }
    
    private void CreateAffiliation(int lAlliance1, int lAlliance2)
    {
        RemoveExistingTreaties(lAlliance1, lAlliance2);

        Affiliation affiliation = new Affiliation(GetAtomicID(lTreatyIndex, Treaties), lAlliance1, lAlliance2);
        AddTreaty(affiliation);
    }
    
    private void CreateAffiliationRequest(int lAlliance1, int lAlliance2)
    {
        RemoveExistingTreaties(lAlliance1, lAlliance2);

        AffiliationRequest affiliation = new AffiliationRequest(GetAtomicID(lTreatyIndex, Treaties), lAlliance1, lAlliance2);
        AddTreaty(affiliation);
    }
    
    private void CreateSurrenderProposal(int lAlliance1, int lAlliance2)
    {
        SurrenderProposal surrender = new SurrenderProposal(GetAtomicID(lTreatyIndex, Treaties), lAlliance1, lAlliance2);
        AddTreaty(surrender);
    }
    
    private Player CreatePlayer(String strName, int lAvatarID)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating a player.");

        Player player = new Player(GetAtomicID(lPlayerIndex, Players), strName, lAvatarID, config.GetStartingWealth());
        
        player.SetJoinTime(System.currentTimeMillis());
        
        if(config.DebugMode())
            player.SetBoss(true);
        
        AddPlayer(player);
        
        SetPlayerOffenseValues(player);
        SetPlayerDefenseValues(player);
        SetPlayerNeutralValues(player);

        return player;
    }
    
    private void CreateMissileLaunch(MapEntity launcher, GeoCoord geoPosition, int lType, int lOwnerID, GeoCoord geoTarget, EntityPointer target, boolean bAirburst)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating a missile launch.");

                MissileType type = config.GetMissileType(lType);
                boolean bTargetAcceptable = false;
                MapEntity targetEntity = null;

                if(type != null)
                {
                    if(target != null)
                    {
                         targetEntity = target.GetMapEntity(game);

                        //Target is not null, which means this missile should track a target. See if the target is acceptable.
                        if(targetEntity != null)
                        {
                            if(type.GetTracking() && (targetEntity instanceof LandUnit && targetEntity.GetVisible()))
                                bTargetAcceptable = true;
                            else if(type.GetAntiShip() && targetEntity instanceof Ship && targetEntity.GetVisible())
                                bTargetAcceptable = true;
                            else if(type.GetAntiShip() && targetEntity instanceof Submarine submarine)
                            {
                                if(!submarine.Submerged())
                                    bTargetAcceptable = true;
                            }
                            else if(type.GetAntiSubmarine() && targetEntity instanceof Submarine)
                                bTargetAcceptable = true;
                        }
                    }

                    if(bTargetAcceptable && targetEntity != null)
                    {
                        if(geoPosition.DistanceTo(targetEntity.GetPosition()) <= type.GetMissileRange())
                        {
                            float fltSpeed = type.GetMissileSpeed();
                            float fltToTarget = geoPosition.DistanceTo(targetEntity.GetPosition());
                            
                            if(type.GetBomb())
                            {
                                fltSpeed = ((fltToTarget/(bAirburst ? Defs.BOMB_TRAVEL_TIME_AIRBURST_MS : Defs.BOMB_TRAVEL_TIME_MS)) * Defs.MS_PER_HOUR);
                            }
                            
                            Missile missile = new Missile(GetAtomicID(lMissileIndex, Missiles), geoPosition, lType, lOwnerID, fltSpeed, targetEntity.GetPosition().GetCopy(), target, geoPosition.GetCopy(), bAirburst && !type.GetAntiSubmarine());

                            if(type.GetICBM())
                                missile.SetVisible(Defs.ICBM_FIRE_VISIBILITY_TIME);
                            else if(type.GetStealth())
                                missile.SetVisible(Defs.STEALTH_MISSILE_FIRE_VISIBILITY_TIME);
                            else
                                missile.SetVisible(Defs.MISSILE_FIRE_VISIBILITY_TIME);

                            AddMissile(missile);

                            Map<Integer, Player> AttackedPlayers = GetThreatenedPlayerIDs(targetEntity.GetPosition().GetCopy(), type, true, true, bAirburst && !type.GetAntiSubmarine());
                            Map<Integer, Alliance> AttackedAlliances = new HashMap<>();
                            Player attacker = Players.get(missile.GetOwnerID());

                            for(Player player : AttackedPlayers.values())
                            {
                                missile.AddStructureThreatenedPlayer(player.GetID());
                                player.AddHostileMissile(missile.GetID());
                                player.AddHostilePlayer(attacker.GetID());
                                
                                SetPlayerUnderAttack(player);

                                CreateReport(player, new LaunchReport(String.format("%s attacked you!", attacker.GetName()), true, attacker.GetID()));

                                int lAllianceID = player.GetAllianceMemberID();

                                if(lAllianceID != Alliance.ALLIANCE_ID_UNAFFILIATED)
                                {
                                    Alliance alliance = GetAlliance(lAllianceID);

                                    if(alliance != null)
                                    {
                                        if(!AttackedAlliances.containsKey(lAllianceID))
                                        {
                                            AttackedAlliances.put(lAllianceID, alliance);
                                        }

                                        for(Player ally : GetAllianceMembers(lAllianceID))
                                        {
                                            User otherUser = ally.GetUser();

                                            if(otherUser != null)
                                                otherUser.SetAllyUnderAttack();

                                            CreateReport(ally, new LaunchReport(String.format("%s attacked your ally %s!", attacker.GetName(), player.GetName()), true, attacker.GetID(), player.GetID()));                                      
                                        }
                                    }  
                                }
                                
                                if(type.GetICBM())
                                {
                                    ProcessMAD(attacker, player, String.format("%s launched an ICBM at %s.", attacker.GetName(), player.GetName()));
                                }  
                            }
                        }
                    }
                    else
                    {
                        if(geoPosition.DistanceTo(geoTarget) <= type.GetMissileRange())
                        {
                            float fltSpeed = type.GetMissileSpeed();
                            float fltToTarget = geoPosition.DistanceTo(geoTarget);
                            
                            if(type.GetBomb())
                            {
                                fltSpeed = ((fltToTarget/(bAirburst ? Defs.BOMB_TRAVEL_TIME_AIRBURST_MS : Defs.BOMB_TRAVEL_TIME_MS)) * Defs.MS_PER_HOUR);
                            }
                            
                            Missile missile = new Missile(GetAtomicID(lMissileIndex, Missiles), geoPosition.GetCopy(), lType, lOwnerID, fltSpeed, geoTarget, null, geoPosition.GetCopy(), bAirburst && !type.GetAntiSubmarine());

                            if(type.GetICBM())
                                missile.SetVisible(Defs.ICBM_FIRE_VISIBILITY_TIME);
                            else if(type.GetStealth())
                                missile.SetVisible(Defs.STEALTH_MISSILE_FIRE_VISIBILITY_TIME);
                            else
                                missile.SetVisible(Defs.MISSILE_FIRE_VISIBILITY_TIME);

                            AddMissile(missile);

                            Map<Integer, Player> AttackedPlayers = GetThreatenedPlayerIDs(geoTarget, type, true, true, bAirburst && !type.GetAntiSubmarine());
                            Map<Integer, Alliance> AttackedAlliances = new HashMap<>();
                            Player attacker = Players.get(missile.GetOwnerID());

                            for(Player victim : AttackedPlayers.values())
                            {
                                missile.AddStructureThreatenedPlayer(victim.GetID());
                                victim.AddHostileMissile(missile.GetID());
                                victim.AddHostilePlayer(attacker.GetID());
                                SetPlayerUnderAttack(victim);

                                CreateReport(victim, new LaunchReport(String.format("%s attacked you!", attacker.GetName()), true, attacker.GetID()));

                                int lAllianceID = victim.GetAllianceMemberID();

                                if(lAllianceID != Alliance.ALLIANCE_ID_UNAFFILIATED)
                                {
                                    Alliance alliance = GetAlliance(lAllianceID);

                                    if(alliance != null)
                                    {
                                        if(!AttackedAlliances.containsKey(lAllianceID))
                                        {
                                            AttackedAlliances.put(lAllianceID, alliance);
                                        }

                                        for(Player ally : GetAllianceMembers(lAllianceID))
                                        {
                                            User otherUser = ally.GetUser();

                                            if(otherUser != null)
                                                otherUser.SetAllyUnderAttack();

                                            CreateReport(ally, new LaunchReport(String.format("%s attacked your ally %s!", attacker.GetName(), victim.GetName()), true, attacker.GetID(), victim.GetID()));                                      
                                        }
                                    }  
                                }
                                
                                if(type.GetICBM())
                                {
                                    ProcessMAD(attacker, victim, String.format("%s launched an ICBM at %s.", attacker.GetName(), victim.GetName()));
                                }   
                            }
                        }
                    } 
                } 
            }
        }).start();
    }
    
    public Map<Integer, Player> GetThreatenedPlayerIDs(GeoCoord geoTarget, MissileType type, boolean bConsiderEMP, boolean bConsiderRespawnProtection, boolean bAirburst)
    {
        Map<Integer, Player> Result = new HashMap<>();
        
        // --- Step 1: Compute threat radius once ---
        float blastRadius = MissileStats.GetBlastRadius(type, bAirburst);
        float empRadius = MissileStats.GetMissileEMPRadius(type, bAirburst);

        float fltThreatRadius = bConsiderEMP ? Math.max(blastRadius, empRadius) : blastRadius;

        if(type.GetAntiSubmarine() && type.GetTorpedoType() != LaunchEntity.ID_NONE)
        {
            TorpedoType torpedoType = config.GetTorpedoType(type.GetTorpedoType());
            fltThreatRadius = torpedoType.GetTorpedoRange();
        }

        if(fltThreatRadius < 0.0001f)
        {
            return Result;
        }

        // --- Step 2: Query nearby entities using QuadTree ---
        List<MapEntity> nearby = EntityPointer.GetMapEntitiesFromPointers(quadtree.QueryRadiusDamagables(geoTarget, fltThreatRadius), game);

        // --- Step 3: Scan nearby entities and early exit ---
        for(MapEntity entity : nearby)
        {
            if(entity instanceof Structure && bConsiderRespawnProtection && ((Structure)entity).GetRespawnProtected())
            {
                continue;
            }

            if(entity.GetPosition().DistanceTo(geoTarget) <= fltThreatRadius)
            {
                Player threatened = GetOwner(entity);
                
                if(threatened != null)
                {
                    if(!Result.containsKey(threatened.GetID()))
                    {
                        Result.put(threatened.GetID(), threatened);
                    }
                }
            }
        }

        return Result;
    }
    
    private void CreateInterceptorLaunch(GeoCoord geoPosition, int lType, int lOwnerID, int lTargetID, boolean bPlayerLaunched, EntityType targetType, int lLaunchedBy)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating an interceptor launch.");
        Interceptor interceptor = new Interceptor(GetAtomicID(lInterceptorIndex, Interceptors), geoPosition, lOwnerID, lTargetID, lType, bPlayerLaunched, targetType, lLaunchedBy);
        AddInterceptor(interceptor);
        
        //Scoring
        Player player = Players.get(lOwnerID);
        
        if(player != null)
            player.AddOwnedEntity(interceptor);
    }
    
    private void CreateTorpedoLaunch(GeoCoord geoPosition, int lType, int lOwnerID, GeoCoord geoTarget, EntityPointer target)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating a torpedo launch.");
        
        TorpedoType type = config.GetTorpedoType(lType);
        
        if(target != null)
        {
            MapEntity entityTarget = target.GetMapEntity(this);
            
            if(entityTarget instanceof NavalVessel)
            {
                if(geoPosition.DistanceTo(entityTarget.GetPosition()) <= type.GetTorpedoRange())
                {
                    Torpedo torpedo = new Torpedo(GetAtomicID(lTorpedoIndex, Torpedoes), geoPosition.GetCopy(), lType, lOwnerID, type.GetHoming(), type.GetTorpedoRange(), 0, geoTarget.GetCopy(), target, (byte)TorpedoState.TRAVELLING.ordinal());

                    AddTorpedo(torpedo);

                    //Raise appropriate reports and attack statuses for threatened players and their allies.
                    Player attacker = Players.get(torpedo.GetOwnerID());

                    List<Integer> AttackedAlliances = new ArrayList<>();

                    EstablishStructureThreats(torpedo);
                    EstablishHostilePlayer(torpedo);

                    for(Player player : GetPlayers())
                    {
                        if(player.Functioning())
                        {
                            if(ThreatensPlayer(player.GetID(), geoTarget, config.GetTorpedoType(lType), true))
                            {
                                //Set that player's attack status.
                                SetPlayerUnderAttack(player);

                                CreateReport(player, new LaunchReport(String.format("%s attacked you!", attacker.GetName()), true, attacker.GetID()));

                                //Do similar for their allies.
                                int lAllianceID = player.GetAllianceMemberID();

                                if(lAllianceID != Alliance.ALLIANCE_ID_UNAFFILIATED)
                                {
                                    if(!AttackedAlliances.contains(lAllianceID))
                                        AttackedAlliances.add(lAllianceID);

                                    for(Player otherPlayer : GetPlayers())
                                    {
                                        if(lAllianceID == otherPlayer.GetAllianceMemberID())
                                        {
                                            if(player != otherPlayer)
                                            {
                                                User otherUser = otherPlayer.GetUser();

                                                if(otherUser != null)
                                                    otherUser.SetAllyUnderAttack();

                                                CreateReport(otherPlayer, new LaunchReport(String.format("%s attacked your ally %s!", attacker.GetName(), player.GetName()), true, attacker.GetID(), player.GetID()));                                      
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            if(geoPosition.DistanceTo(geoTarget) <= type.GetTorpedoRange())
            {
                Torpedo torpedo = new Torpedo(GetAtomicID(lTorpedoIndex, Torpedoes), geoPosition.GetCopy(), lType, lOwnerID, type.GetHoming(), type.GetTorpedoRange(), 0, geoTarget.GetCopy(), null, (byte)TorpedoState.TRAVELLING.ordinal());
                Player owner = Players.get(lOwnerID);

                AddTorpedo(torpedo);

                //Raise appropriate reports and attack statuses for threatened players and their allies.
                Player attacker = Players.get(torpedo.GetOwnerID());

                List<Integer> AttackedAlliances = new ArrayList<>();

                EstablishStructureThreats(torpedo);
                EstablishHostilePlayer(torpedo);

                for(Player player : GetPlayers())
                {
                    if(player.Functioning())
                    {
                        if(ThreatensPlayer(player.GetID(), geoTarget, config.GetTorpedoType(lType), true))
                        {
                            //Set that player's attack status.
                            SetPlayerUnderAttack(player);

                            CreateReport(player, new LaunchReport(String.format("%s attacked you!", attacker.GetName()), true, attacker.GetID()));

                            //Do similar for their allies.
                            int lAllianceID = player.GetAllianceMemberID();

                            if(lAllianceID != Alliance.ALLIANCE_ID_UNAFFILIATED)
                            {
                                if(!AttackedAlliances.contains(lAllianceID))
                                    AttackedAlliances.add(lAllianceID);

                                for(Player otherPlayer : GetPlayers())
                                {
                                    if(lAllianceID == otherPlayer.GetAllianceMemberID())
                                    {
                                        if(player != otherPlayer)
                                        {
                                            User otherUser = otherPlayer.GetUser();

                                            if(otherUser != null)
                                                otherUser.SetAllyUnderAttack();

                                            CreateReport(otherPlayer, new LaunchReport(String.format("%s attacked your ally %s!", attacker.GetName(), player.GetName()), true, attacker.GetID(), player.GetID()));                                      
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void CreateLoot(GeoCoord geoPosition, LootType lootType, int lTypeID, long oQuantity, int lExpiry)
    {
        for(Loot loot : GetLoots())
        {
            if(loot.GetLootType() == lootType && loot.GetCargoID() == lTypeID)
            {
                if(loot.GetPosition().BroadPhaseCollisionTest(geoPosition))
                {
                    if(loot.GetPosition().DistanceTo(geoPosition) <= Defs.LOOT_COMBINE_RADIUS)
                    {
                        loot.SetQuantity(oQuantity + loot.GetQuantity());
                        loot.SetExpiry(lExpiry);
                        EntityUpdated(loot, false);
                        return;
                    }
                }
            }
        }   
        
        AddLoot(new Loot(GetAtomicID(lLootIndex, Loots), geoPosition, lootType, lTypeID, oQuantity, lExpiry));
    }
    
    private void CreateSalvage(GeoCoord geoPosition, Map<ResourceType, Long> Salvages)
    {
        for(Entry<ResourceType, Long> salvage : Salvages.entrySet())
        {
            geoPosition.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 0.075f);
            long oBaseValue = salvage.getValue();
            int lExpiry = config.GetRubbleMinTime() + (int)(random.nextFloat() * (float)((config.GetRubbleMaxTime() - config.GetRubbleMinTime()) + 0.5f));
        
            CreateLoot(geoPosition, LootType.RESOURCES, salvage.getKey().ordinal(), Math.max((long)(config.GetRubbleMinValue() * oBaseValue), oBaseValue), lExpiry);
        }
    }
    
    private void CreateRubble(Structure structure)
    {
        ResourceType type = ResourceType.IRON;
        
        if(structure instanceof Processor)
        {
            Processor processor = (Processor)structure;
            type = processor.GetType();
        }
        else if(structure instanceof Distributor)
        {
            Distributor distributor = (Distributor)structure;
            type = distributor.GetType();
        }
        else if(structure instanceof OreMine)
        {
            OreMine mine = (OreMine)structure;
            type = mine.GetType();
        }
        
        AddRubble(new Rubble(GetAtomicID(lRubbleIndex, Rubbles), structure.GetPosition().GetCopy(), structure.GetEntityType(), type, structure.GetOwnerID(), Defs.RUBBLE_EXPIRY));
    }
    
    private void RemoveNearbyRubbles(GeoCoord geoPosition)
    {
        for(Rubble rubble : Rubbles.values())
        {
            if(rubble.GetPosition().BroadPhaseCollisionTest(geoPosition))
            {
                if(rubble.GetPosition().DistanceTo(geoPosition) <= config.GetStructureSeparation(rubble.GetStructureType()))
                {
                    rubble.SetRemove();
                }
            }
        }
        
        for(Blueprint blueprint : Blueprints.values())
        {
            if(blueprint.GetPosition().BroadPhaseCollisionTest(geoPosition))
            {
                if(blueprint.GetPosition().DistanceTo(geoPosition) <= config.GetStructureSeparation(blueprint.GetType()))
                {
                    blueprint.SetRemove();
                }
            }
        }
    }
    
    private void CreateRadiation(GeoCoord geoPosition, float fltRadius, int lPlayerID)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating radiation.");
        
        AddRadiation(new Radiation(GetAtomicID(lRadiationIndex, Radiations), geoPosition, fltRadius, LaunchUtilities.GetRandomIntInBounds(Defs.RADIATION_MIN_EXPIRY, Defs.RADIATION_MAX_EXPIRY), lPlayerID));
    }
    
    public void AddUser(User user)
    {
        Users.put(user.GetIdentityKey(), user);
    }
    
    public void SetCompassionateInvulnerability(int lPlayerID, int lTime)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            //Make player invulnerable.
            player.SetCompassionateInvulnerability(lTime);
            
            //Make their structures invulnerable.
            for(Structure structure : new ArrayList<>(player.GetStructures()))
            {
                structure.SetRespawnProtected(true);
            }
            
            //TODO: For this to be fully useful, we probably want to make ships/landunits invulnerable too.
            
            CreateReport(new LaunchReport(String.format("%s was granted compassionate invulnerability.", player.GetName()), false, lPlayerID));
        }
    }
    
    /** Protect a player from going AWOL by advancing their "last seen" time by three months.
     * @param lPlayerID The ID of the player to park. */
    public void ParkPlayer(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            player.Park(Defs.NINETY_DAYS);
            
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s's account was parked.", player.GetName()), false, lPlayerID));
        }
    }
    
    public void TestAlert(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            User user = player.GetUser();
        
            if(user != null)
            {
                user.SetUnderAttack();
            }
        }
    }
    
    public boolean TestGiveShitty(int lPlayerID)
    {
        return false;
    }
    
    public boolean Approve(int lPlayerID, String strApprovar)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            User user = player.GetUser();
            
            if(user != null)
            {
                user.ApproveAccount();
                CreateAdminReport(new LaunchReport(String.format("[Admin] %s manually approved %s's account.", strApprovar, player.GetName()), true, player.GetID()));
                return true;
            }
        }
        
        return false;
    }
    
    public boolean RequireNewChecks(int lPlayerID, String strAdmin)
    {
        Player player = Players.get(lPlayerID);
                
        if(player != null)
        {
            User user = player.GetUser();
            
            if(user != null)
            {
                user.RequireNewChecks();
                CreateAdminReport(new LaunchReport(String.format("[Admin] %s manually required new checks on %s's account.", strAdmin, player.GetName()), true, player.GetID()));
                return true;
            }
        }
        
        return false;
    }
    
    public void CleanAvatars()
    {
        File avatarFolder = new File(Defs.LOCATION_AVATARS);
        
        for(File file : avatarFolder.listFiles())
        {
            try
            {
                int lAvatarID = Integer.parseInt(file.getName().replaceAll("\\D+",""));
                boolean bMatchFound = false;
                
                for(Player player : GetPlayers())
                {
                    if(player.GetAvatarID() == lAvatarID)
                    {
                        bMatchFound = true;
                        break;
                    }
                }

                for(Alliance alliance : GetAlliances())
                {
                    if(alliance.GetAvatarID() == lAvatarID)
                    {
                        bMatchFound = true;
                        break;
                    }
                }
                
                if(!bMatchFound)
                {
                    if(file.delete())
                    {
                        LaunchLog.ConsoleMessage(String.format("Deleted unused avatar %s", file.getName()));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage(String.format("Could not delete unused avatar %s!", file.getName()));
                    }
                }
            }
            catch(NumberFormatException ex)
            {
                LaunchLog.ConsoleMessage(String.format("Rogue file %s found in avatars folder!", file.getName()));
            }
        }
    }
    
    public void PurgeAvatars()
    {
        comms.InterruptAll();
        
        for(Player player : GetPlayers())
        {
            player.SetAvatarID(Defs.THE_GREAT_BIG_NOTHING);
        }

        for(Alliance alliance : GetAlliances())
        {
            alliance.SetAvatarID(Defs.THE_GREAT_BIG_NOTHING);
        }
        
        File avatarFolder = new File(Defs.LOCATION_AVATARS);
        
        for(File file : avatarFolder.listFiles())
        {
            try
            {
                if(!file.delete())
                {
                    LaunchLog.ConsoleMessage(String.format("Could not delete avatar %s!", file.getName()));
                }
            }
            catch(NumberFormatException ex)
            {
                LaunchLog.ConsoleMessage(String.format("Rogue file %s found in avatars folder!", file.getName()));
            }
        }
    }
    
    public void RemoveWealth(int lID, int lAmount, String strReason)
    {
        Player player = Players.get(lID);
        
        if(player != null)
        {
            player.SubtractWealth(lAmount);
            
            String strStatement = String.format("removed %d from %s. Reason: %s", lAmount, player.GetName(), strReason);
            
            CreateEvent(new LaunchEvent(strStatement, SoundEffect.MONEY));
            CreateReport(new LaunchReport(strStatement, true, player.GetID()));
        }
    }
    
    public void Award(int lID, int lAmount, String strReason)
    {
        Player player = Players.get(lID);
        
        if(player != null)
        {
            //player.AddWealth(lAmount);
            ProcessPlayerIncome(player, strReason, Map.ofEntries(entry(ResourceType.WEALTH, (long)lAmount)), false);
            
            String strStatement = String.format("%s was awarded $%d. Reason: %s", player.GetName(), lAmount, strReason);
            
            CreateEvent(new LaunchEvent(strStatement, SoundEffect.MONEY));
            CreateReport(new LaunchReport(strStatement, true, player.GetID()));
        }
    }
    
    public void Stimulus(int lAmount)
    {
        for(Player player : GetPlayers())
        {
            if(player.Functioning())
            {
                //player.AddWealth(lAmount);
                ProcessPlayerIncome(player, "stimulus", Map.ofEntries(entry(ResourceType.WEALTH, (long)lAmount)), false);
            }
        }
        
        CreateEvent(new LaunchEvent(String.format("Stimulus package: all players given $%d", lAmount), SoundEffect.MONEY));
        CreateReport(new LaunchReport(String.format("Stimulus package: all players given $%d", lAmount), true));
    }
    
    public Collection<User> GetUsers() { return Users.values(); }
    
    public void CreateEvent(LaunchEvent event)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Event) %s", event.GetMessage()));
        comms.Announce(event, false, LaunchEntity.ID_NONE);
    }
    
    public void CreateEventForPlayer(LaunchEvent event, int lPlayerID)
    {
        if(Players.get(lPlayerID) != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Player-Specific Event) %s", event.GetMessage()));
            comms.Announce(event, true, lPlayerID);
        }
    }
    
    /**
     * Send report only to alliance members.
     * @param alliance The alliance to which all members the report should be sent.
     * @param report The report.
     */
    public void CreateReport(Alliance alliance, LaunchReport report)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Report -> %s members) %s", alliance.GetName(), report.GetMessage()));
        
        for(Player player : GetPlayers())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
            {
                User user = player.GetUser();
                
                if(user != null)
                    user.AddReport(report);
            }
        }
    }
    
    /**
     * Send report to a single player.
     * @param singlePlayer The player to send the report to.
     * @param report The report.
     */
    public void CreateReport(Player singlePlayer, LaunchReport report)
    {
        if(singlePlayer != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Report -> %s) %s", singlePlayer.GetName(), report.GetMessage()));

            User user = singlePlayer.GetUser();

            if(user != null && !config.DebugMode())
                user.AddReport(report);
        }
    }
    
    /**
     * Send report to all players.
     * @param report Report to send to all players.
     */
    public void CreateReport(LaunchReport report)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Report) %s", report.GetMessage()));
        
        for(User user : Users.values())
        {
            user.AddReport(report);
        }
        
        if(!config.DebugMode())
        {
            try
            {
                CreateDiscordWebhook(report.GetMessage());
            }
            catch(IOException ex)
            {
                //Don't care.
            }
        }
    }
    
    public void CreateDiscordWebhook(String strMessage) throws IOException
    {
        if(!Defs.DEBUG_MODE)
        {
            DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/1483157562231623740/es3DydBoocR1548wbQ3rhxGCSy7-hIEUwcYgw8Nb6vTapZItEpPfPgqBawYhys9MSYc2");
                webhook.setContent(strMessage);
                webhook.setAvatarUrl("https://your.awesome/image.png");
                webhook.setUsername("Game Reports");
                webhook.setTts(false);
                /*webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle("Title")
                        .setDescription("This is a description")
                        .setColor(Color.RED)
                        .addField("1st Field", "Inline", true)
                .addField("2nd Field", "Inline", true)
                .addField("3rd Field", "No-Inline", false)
                .setThumbnail("https://kryptongta.com/images/kryptonlogo.png")
                .setFooter("Footer text", "https://kryptongta.com/images/kryptonlogodark.png")
                .setImage("https://kryptongta.com/images/kryptontitle2.png")
                .setAuthor("Author Name", "https://kryptongta.com", "https://kryptongta.com/images/kryptonlogowide.png")
                .setUrl("https://kryptongta.com"));
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setDescription("Just another added embed object!"));*/
                webhook.execute(); //Handle exception
        } 
    }
    
    /**
     * Send report to all admins.
     * @param report Report to send to all admins.
     */
    public void CreateAdminReport(LaunchReport report)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("(Report -> Admins) %s", report.GetMessage()));
        
        for(Player player : GetPlayers())
        {
            if(player.GetIsAnAdmin())
            {
                User user = player.GetUser();
                
                if(user != null)
                    user.AddReport(report);
            }
        }
    }
    
    public void ForceAllianceDisbandChecks()
    {
        LaunchLog.ConsoleMessage("Forcing alliance disband checks...");
        
        for(Alliance alliance : GetAlliances())
        {
            AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);
        }
        
        LaunchLog.ConsoleMessage("...Done.");
    }
    
    public void CleanUpUnownedEntities()
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Cleaning up orphaned entities...");
        
        //Remove all unowned entities from the game.
        for(Alliance alliance : GetAlliances())
        {
            if(GetAllianceIsLeaderless(alliance) || GetAllianceMemberCount(alliance) == 0)
            {
                Alliances.remove(alliance.GetID());
                AllianceMemberRosters.remove(alliance.GetID());
            }
        }

        for(Treaty treaty : GetTreaties())
        {
            if(!Alliances.containsKey(treaty.GetAllianceID1()) || !Alliances.containsKey(treaty.GetAllianceID2()))
            {
                Treaties.remove(treaty.GetID());
            }
        }

        for(Missile missile : GetMissiles())
        {
            if(!Players.containsKey(missile.GetOwnerID()))
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing missile %d owned by non-existent player %d.", missile.GetID(), missile.GetOwnerID()));
                Missiles.remove(missile.GetID());
            }
        }

        for(Interceptor interceptor : GetInterceptors())
        {
            if(!Players.containsKey(interceptor.GetOwnerID()))
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing interceptor %d owned by non-existent player %d.", interceptor.GetID(), interceptor.GetOwnerID()));
                Interceptors.remove(interceptor.GetID());
            }
        }

        for(Structure structure : GetAllStructures())
        {
            if(!Players.containsKey(structure.GetOwnerID()))
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by non-existent player %d.", structure.GetTypeName(), structure.GetID(), structure.GetOwnerID()));
                structure.SetHP((short)0);
            }
        }
    }
    
    public void CleanUpOwnedEntities(int lPlayerID)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Cleaning up entities owned by player %d...", lPlayerID));

        //Remove all entities from the game owned by a particular player.
        for(Missile missile : GetMissiles())
        {
            if(missile.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing missile %d owned by player %d.", missile.GetID(), lPlayerID));
                Missiles.remove(missile.GetID());
            }
        }

        for(Interceptor interceptor : GetInterceptors())
        {
            if(interceptor.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing interceptor %d owned by player %d.", interceptor.GetID(), lPlayerID));
                Interceptors.remove(interceptor.GetID());
            }
        }
        
        for(Torpedo torpedo : GetTorpedoes())
        {
            if(torpedo.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Removing torpedo %d owned by player %d.", torpedo.GetID(), lPlayerID));
                Torpedoes.remove(torpedo.GetID());
            }
        }

        for(Structure structure : GetAllStructures())
        {
            if(structure.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", structure.GetTypeName(), structure.GetID(), structure.GetOwnerID()));
                structure.SetHP((short)0);
                
                if(structure instanceof Airbase)
                {
                    Airbase airbase = ((Airbase)structure);
                    
                    for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.HasCargo())
                            aircraft.GetCargoSystem().ClearNonEntities();
                    }
                }
                
                if(structure instanceof HaulerInterface)
                {
                    ((HaulerInterface)structure).GetCargoSystem().ClearNonEntities();
                }
            }
        }
        
        for(CargoTruck truck : GetCargoTrucks())
        {
            if(truck.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", truck.GetTypeName(), truck.GetID(), truck.GetOwnerID()));
                truck.SetHP((short)0);
                truck.GetCargoSystem().ClearNonEntities();
            }
        }
        
        for(Tank tank : GetTanks())
        {
            if(tank.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", tank.GetTypeName(), tank.GetID(), tank.GetOwnerID()));
                tank.SetHP((short)0);
            }
        }
        
        for(Infantry infantry : GetInfantries())
        {
            if(infantry.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", infantry.GetTypeName(), infantry.GetID(), infantry.GetOwnerID()));
                infantry.SetHP((short)0);
            }
        }
        
        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", ship.GetTypeName(), ship.GetID(), ship.GetOwnerID()));
                ship.SetHP((short)0);
                
                if(ship.HasCargo())
                    ship.GetCargoSystem().ClearNonEntities();
            }
        }
        
        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", submarine.GetTypeName(), submarine.GetID(), submarine.GetOwnerID()));
                submarine.SetHP((short)0);
            }
        }
        
        for(Airplane aircraft : GetAirplanes())
        {
            if(aircraft.GetOwnerID() == lPlayerID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Destroying %s %d owned by player %d.", aircraft.GetTypeName(), aircraft.GetID(), aircraft.GetOwnerID()));
                
                if(aircraft.HasCargo())
                    aircraft.GetCargoSystem().ClearNonEntities();
                
                aircraft.InflictDamage(aircraft.GetMaxHP());
            }
        }

        for(Shipyard shipyard : Shipyards.values())
        {
            if(shipyard.GetOwnedBy(lPlayerID))
            {
                shipyard.Abandon();
            }
        }
        
        for(Blueprint blueprint : Blueprints.values())
        {
            if(blueprint.GetOwnedBy(lPlayerID))
            {
                blueprint.SetRemove();
            }
        }
    }
    
    public void BlessAllNames()
    {
        LaunchLog.ConsoleMessage("Blessing all names...");
                    
        for(Player player : GetPlayers())
        {
            String strNameInitial = player.GetName();
            player.ChangeName(LaunchUtilities.BlessName(player.GetName()));

            if(!strNameInitial.equals(player.GetName()))
                LaunchLog.ConsoleMessage(String.format("Changed %s to %s", strNameInitial, player.GetName()));
        }

        for(Alliance alliance : GetAlliances())
        {
            String strNameInitial = alliance.GetName();
            alliance.SetName(LaunchUtilities.BlessName(alliance.GetName()));

            if(!strNameInitial.equals(alliance.GetName()))
                LaunchLog.ConsoleMessage(String.format("Changed alliance %s to %s", strNameInitial, alliance.GetName()));

            String strDescriptionInitial = alliance.GetDescription();
            alliance.SetDescription(LaunchUtilities.SanitiseText(alliance.GetDescription(), false, true));

            if(!strDescriptionInitial.equals(alliance.GetDescription()))
                LaunchLog.ConsoleMessage(String.format("Changed alliance description %s to %s", strDescriptionInitial, alliance.GetDescription()));
        }

        for(Structure structure : GetAllStructures())
        {
            String strNameInitial = structure.GetName();
            structure.SetName(LaunchUtilities.SanitiseText(structure.GetName(), true, true));

            if(!strNameInitial.equals(structure.GetName()))
                LaunchLog.ConsoleMessage(String.format("Changed structure %s to %s", strNameInitial, structure.GetName()));
        }
        
        LaunchLog.ConsoleMessage("...Done.");
    }
    
    @Override
    public boolean Obliterate(int lAdminID, int lPlayerID)
    {
        Player admin = GetPlayer(lAdminID);
        Player player = GetPlayer(lPlayerID);
        
        if(admin != null && player != null && admin.GetIsAnAdmin())
        {
            DeletePlayer(lPlayerID, true);
            LaunchLog.ConsoleMessage(String.format("%s obliterated %s.", admin.GetName(), player.GetName()));
            
            return true;
        }
        
        return false;
    }

    public void DeletePlayer(int lPlayerID, boolean bDeleteUser)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            LaunchLog.ConsoleMessage(String.format("Obliterating %s...", player.GetName()));
            
            Players.remove(lPlayerID);
            
            if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                if(AllianceMemberRosters.containsKey(player.GetAllianceMemberID()))
                {
                    Map<Integer, Player> Members = AllianceMemberRosters.get(player.GetAllianceMemberID());

                    if(Members != null)
                    {
                        if(Members.containsKey(player.GetID()))
                        {
                            Members.remove(player.GetID(), player);
                            AllianceMemberRosters.put(player.GetAllianceMemberID(), Members);
                        }
                    }
                }
            }

            User user = player.GetUser();
            
            //TODO: When the game is monetized, never remove either monetized users or their players.
            if(user != null && bDeleteUser)
            {
                Users.remove(user.GetIdentityKey());
            }
            
            CleanUpOwnedEntities(lPlayerID);
            
            comms.InterruptAll();
            
            LaunchLog.ConsoleMessage("...boom.");
        }
    }
    
    public LaunchServerComms GetServerComms()
    {
        return comms;
    }
    
    /**
     * Check if an alliance has no active players (alive and not respawn protected) to establish if it is "defeated" for the purpose of preventing war abuse.
     * @param alliance Alliance to check
     * @return True if the alliance has no "active" players and should be disbanded.
     */
    public boolean GetAllianceHasNoActivePlayers(Alliance alliance)
    {
        for(Player player : GetPlayers())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
            {
                if(player.Functioning() && !player.GetRespawnProtected())
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if the alliance has only prisoners.
     * @param alliance
     * @return true if not all the players in the alliance are prisoners.
     */
    public boolean GetAllianceIsAllPrisoners(Alliance alliance)
    {
        for(Player player : GetPlayers())
        {
            if(player.GetAllianceMemberID() == alliance.GetID())
            {
                if(player.Functioning() && !player.GetPrisoner())
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     *
     * @param alliance the alliance to be disbanded.
     * @param lDisbanderID the ID of the disbander. This will either reference a player or an alliance depending on bPlayer.
     * @param bFromCombat if true, the alliance disbanded from combat and the game should identify a disbander.
     * @param bPlayer if true, lDisbanderID references a player. If false, it references an alliance.
     */
    public void AllianceCleanupCheck(Alliance alliance, int lDisbanderID, boolean bFromCombat, boolean bPlayer)
    {
        boolean bDisband = false;
        String strDisbandReason = "";

        //Disband the alliance if there are no members or leaders, or they are all dead, prisoners, or respawn protected.
        if(GetAllianceMemberCount(alliance) == 0)
        {
            bDisband = true;
            strDisbandReason = "No remaining players";
        }
        else if(GetAllianceIsLeaderless(alliance))
        {
            bDisband = true;
            strDisbandReason = "No leader";
        }
        else if(GetAllianceHasNoActivePlayers(alliance))
        {
            bDisband = true;
            strDisbandReason = "Annihilated";
        }
        else if(GetAllianceIsAllPrisoners(alliance))
        {
            bDisband = true;
            strDisbandReason = "Only prisoners remain";
        }

        if(bDisband)
        {
            if(bFromCombat)
            {
                if(bPlayer)
                {
                    Player disbanderPlayer = GetPlayer(lDisbanderID);
                    
                    if(disbanderPlayer != null)
                    {
                        int lSpoils = alliance.GetWealth();
                        disbanderPlayer.AddWealth(lSpoils);

                        CreateReport(new LaunchReport(String.format("%s disbanded %s! %s received $%d in spoils.", disbanderPlayer.GetName(), alliance.GetName(), disbanderPlayer.GetName(), lSpoils), true, disbanderPlayer.GetID(), alliance.GetID(), false, true));
                    }
                }
                else
                {
                    Alliance disbanderAlliance = GetAlliance(lDisbanderID);
                    
                    if(disbanderAlliance != null)
                    {
                        int lSpoils = alliance.GetWealth();
                        disbanderAlliance.AddWealth(lSpoils);
                        disbanderAlliance.AddEnemyAllianceDisband();

                        CreateReport(new LaunchReport(String.format("%s disbanded %s! %s received $%d in spoils.", disbanderAlliance.GetName(), alliance.GetName(), disbanderAlliance.GetName(), lSpoils), true, disbanderAlliance.GetID(), alliance.GetID(), true, true));
                    }
                }
            }
            
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                    player.SetIsAnMP(false);
                    player.SetAllianceCooloffTime(player.GetPrisoner() ? 0 : config.GetAllianceCooloffTime());
                    CreateReport(player, new LaunchReport(String.format("Your alliance %s was disbanded. Reason: %s.", alliance.GetName(), strDisbandReason), true, player.GetID()));
                    player.SetPrisoner(false);
                    EntityUpdated(player, false);
                }
                else
                {
                    CreateReport(player, new LaunchReport(String.format("Alliance %s disbanded. Reason: %s.", alliance.GetName(), strDisbandReason), false));
                }
            }

            for(Treaty treaty : GetTreaties())
            {
                if(treaty.IsAParty(alliance.GetID()))
                {
                    //Forfeit any wars.
                    if(treaty instanceof War)
                    {
                        ((War)treaty).Forfeit(alliance.GetID());
                        ConcludeWar((War)treaty);
                    }

                    Treaties.remove(treaty.GetID());
                    TreatyRemoved(treaty);
                }
            }

            CreateEvent(new LaunchEvent(String.format("%s disbanded.", alliance.GetName()), SoundEffect.RESPAWN));

            Alliances.remove(alliance.GetID());
            AllianceRemoved(alliance);
            AllianceMemberRosters.remove(alliance.GetID());
        }
        else
        {
            AllianceUpdated(alliance, true);
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // LaunchGame inherited abstract methods.
    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void MissileExploded(Missile missile)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "A missile exploded.");
        Player owner = Players.get(missile.GetOwnerID());
        MissileType type = config.GetMissileType(missile.GetType());
        float fltBlastRadius = MissileStats.GetBlastRadius(type, missile.GetAirburst());
        String strCause;
        
        int lYield = type.GetYield();
        
        Explosion explosion = new Explosion(lYield, type.GetNuclear(), type.GetICBM(), type.GetBunkerBuster(), type.GetAntiShip(), type.GetAntiSubmarine(), type.GetArtillery(), missile.GetAirburst(), type.GetAccuracy());
        
        if(type.GetAntiSubmarine() && type.GetTorpedoType() != LaunchEntity.ID_NONE)
        {
            CreateEvent(new LaunchEvent(String.format("%s's %s reached its target and deployed a torpedo.", owner.GetName(), type.GetName()), SoundEffect.TORPEDO_LAUNCH));
            
            GeoCoord geoDeploy = missile.GetPosition().GetCopy();
            double dblBearing = missile.GetOrigin().BearingTo(missile.GetPosition().GetCopy());
            geoDeploy.Move(dblBearing, 0.1f);
            geoDeploy.SetLastBearing((float)dblBearing);
            CreateTorpedoLaunch(geoDeploy, type.GetTorpedoType(), missile.GetOwnerID(), missile.GetPosition().GetCopy(), null);
        }
        else if(type.GetSonobuoy())
        {
            GeoCoord geoDeploy = missile.GetPosition().GetCopy();
            
            if(TerrainChecker.CoordinateIsWater(geoDeploy))
            {
                for(Submarine submarine : GetSubmarines())
                {
                    if(!WouldBeFriendlyFire(GetOwner(submarine), owner) && submarine.GetPosition().DistanceTo(geoDeploy) <= Defs.SONAR_RANGE)
                    {
                        submarine.SetVisible(Defs.SONAR_SCAN_VISIBILITY_TIME);
                        EntityUpdated(submarine, false);
                    }
                }
                
                for(Ship ship : GetShips())
                {
                    if(!WouldBeFriendlyFire(GetOwner(ship), owner) && ship.GetPosition().DistanceTo(geoDeploy) <= Defs.SONAR_RANGE)
                    {
                        ship.SetVisible(Defs.SONAR_SCAN_VISIBILITY_TIME);
                        EntityUpdated(ship, false);
                    }
                }

                CreateEvent(new LaunchEvent(String.format("%s's %s performed a sonar ping!", owner.GetName(), type.GetName()), SoundEffect.SONAR));
            }
        }
        else
        {
            if(type.GetArtillery())
            {
                CreateEvent(new LaunchEvent(String.format("", owner.GetName(), type.GetName()), type.GetNuclear() ? SoundEffect.NUKE_EXPLOSION : TerrainChecker.CoordinateIsWater(missile.GetPosition()) ? SoundEffect.WATER_EXPLOSION : SoundEffect.ARTILLERY_EXPLOSION));
                strCause = String.format("%s's %s artillery shell", owner.GetName(), type.GetName());
            }
            else
            {
                CreateEvent(new LaunchEvent(String.format("", owner.GetName(), type.GetName()), type.GetNuclear() ? SoundEffect.NUKE_EXPLOSION : TerrainChecker.CoordinateIsWater(missile.GetPosition()) ? SoundEffect.WATER_EXPLOSION : SoundEffect.EXPLOSION));
                strCause = String.format("%s's %s missile", owner.GetName(), type.GetName());
            }

            if(fltBlastRadius > 0)
            {
                ProcessExplosion(owner, strCause, missile.GetPosition(), explosion);
            }

            if(MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()) > 0)
            {
                CreateEMPPulse(missile.GetPosition(), MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()), missile.GetOwnerID());
            }
        }  
    }
    
    @Override
    protected void TorpedoExploded(Torpedo torpedo)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "A torpedo exploded.");
        Player owner = Players.get(torpedo.GetOwnerID());
        TorpedoType type = config.GetTorpedoType(torpedo.GetType());
        String strCause;
        
        Explosion explosion = new Explosion(type.GetYield(), type.GetNuclear(), false, false, true, true, false, false, 1.0f);
        
        CreateEvent(new LaunchEvent(String.format("%s's torpedo reached its target", owner.GetName()), SoundEffect.EXPLOSION));
        strCause = String.format("%s's %s torpedo", owner.GetName(), type.GetName());
        
        if(type.GetBlastRadius() > 0)
        {
            ProcessExplosion(owner, strCause, torpedo.GetPosition(), explosion);
        }
    }

    @Override
    protected void InterceptorLostTarget(Interceptor interceptor)
    {
        Player owner = Players.get(interceptor.GetOwnerID());
        CreateEvent(new LaunchEvent(String.format("%s's interceptor lost its target.", owner.GetName()), SoundEffect.INTERCEPTOR_MISS));
    }
    
    @Override
    protected void InterceptorReachedTarget(Interceptor interceptor)
    {
        Player interceptorOwner = Players.get(interceptor.GetOwnerID());
        InterceptorType interceptorType = config.GetInterceptorType(interceptor.GetType());
                
        if(interceptorOwner != null)
        {
            switch(interceptor.GetTargetType())
            {
                case AIRPLANE:
                {
                    Airplane aircraft = GetAirplane(interceptor.GetTargetID());
                    Player aircraftOwner = GetPlayer(aircraft.GetOwnerID());

                    float fltHitChance = GetInterceptorHitChance(interceptorType.GetInterceptorSpeed(), Defs.GetAircraftSpeed(aircraft.GetEntityType()), null, null);

                    if(aircraftOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                        fltHitChance -= Defs.OFFLINE_PLAYER_AIRCRAFT_DODGE_BUFF;

                    if(interceptorOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                        fltHitChance += Defs.OFFLINE_PLAYER_INTERCEPTOR_HIT_BUFF;

                    if(interceptorType.GetNuclear() || random.nextFloat() < Math.max(fltHitChance, Defs.MINIMUM_INTERCEPTOR_ACCURACY))
                    {
                        short damageInflicted = aircraft.InflictDamage(aircraft.GetMaxHP());
                        
                        Scoring_DamageInflicted(interceptorOwner, aircraftOwner, damageInflicted); 
                        ProcessPlayerXPGain(interceptorOwner.GetID(), Defs.AIRCRAFT_KILLED_XP, "You destroyed an aircraft.");
                        ProcessPlayerXPLoss(aircraftOwner.GetID(), Defs.AIRCRAFT_LOST_XP, "Your aircraft was destroyed.");

                        if(interceptorOwner.ApparentlyEquals(aircraftOwner))
                        {
                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own %s.", interceptorOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_HIT));
                            CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));
                        }
                        else
                        {
                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's %s.", interceptorOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_HIT));
                            CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false));               
                            CreateReport(aircraftOwner, new LaunchReport(String.format("%s's interceptor shot down your %s.", interceptorOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), interceptorOwner.GetID()));
                            CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));                    
                        }
                    }
                    else
                    {
                        CreateEvent(new LaunchEvent(String.format("%s's interceptor missed %s's %s.", interceptorOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_MISS));
                        CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor missed %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false, interceptorOwner.GetID(), aircraftOwner.GetID()));
                        CreateReport(aircraftOwner, new LaunchReport(String.format("%s's interceptor missed your %s.", interceptorOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), interceptorOwner.GetID()));
                    }
                }
                break;

                case MISSILE:
                {
                    Missile missile = Missiles.get(interceptor.GetTargetID());
                    Player missileOwner = Players.get(missile.GetOwnerID());
                    MissileType missileType = config.GetMissileType(missile.GetType());

                    float fltHitChance = GetInterceptorAccuracy(interceptorType);

                    if(interceptor.GetPlayerLaunched())
                    {
                        fltHitChance += config.GetManualInterceptorChanceIncrease();
                    }

                    if(missileType != null)
                    {
                        if(config.GetMissileType(missile.GetType()).GetECM())
                        {
                            fltHitChance -= config.GetECMInterceptorChanceReduction();
                        }

                        //ABMs do not get the offline player hit chance buff.
                        if(!missileType.GetICBM() && interceptorOwner.GetLastSeen() <= System.currentTimeMillis() - (Defs.MS_PER_MIN * 30))
                            fltHitChance += Defs.OFFLINE_PLAYER_INTERCEPTOR_HIT_BUFF;

                        //Decide if the missile got shot down.
                        if(interceptorType.GetNuclear() || random.nextFloat() < Math.max(fltHitChance, Defs.MINIMUM_INTERCEPTOR_ACCURACY))
                        {
                            if(missileType.GetICBM())
                            {
                                missile.Destroy();
                                EntityUpdated(missile, false);
                                ProcessPlayerXPGain(interceptorOwner.GetID(), Defs.ICBM_SHOOTDOWN_XP, "You destroyed an ICBM.");

                                if(interceptorOwner.ApparentlyEquals(missileOwner))
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own ICBM.", interceptorOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                }
                                else
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's ICBM.", interceptorOwner.GetName(), missileOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                    CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's ICBM.", missileOwner.GetName()), false, interceptorOwner.GetID(), missileOwner.GetID()));
                                    CreateReport(missileOwner, new LaunchReport(String.format("%s's interceptor shot down your ICBM.", interceptorOwner.GetName()), false, missileOwner.GetID(), interceptorOwner.GetID()));
                                }
                            }
                            else
                            {
                                missile.Destroy();
                                EntityUpdated(missile, false);

                                if(interceptorOwner.ApparentlyEquals(missileOwner))
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own missile.", interceptorOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                }
                                else
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's missile.", interceptorOwner.GetName(), missileOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                    CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's missile.", missileOwner.GetName()), false, interceptorOwner.GetID(), missileOwner.GetID()));
                                    CreateReport(missileOwner, new LaunchReport(String.format("%s's interceptor shot down your missile.", interceptorOwner.GetName()), false, missileOwner.GetID(), interceptorOwner.GetID()));
                                }
                            }
                        }
                        else
                        {
                            CreateEvent(new LaunchEvent(String.format("%s's interceptor missed %s's missile.", interceptorOwner.GetName(), missileOwner.GetName()), SoundEffect.INTERCEPTOR_MISS));
                            CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor missed %s's missile.", missileOwner.GetName()), false, interceptorOwner.GetID(), missileOwner.GetID()));
                            CreateReport(missileOwner, new LaunchReport(String.format("%s's interceptor missed your missile.", interceptorOwner.GetName()), false, missileOwner.GetID(), interceptorOwner.GetID()));
                        }
                    }  
                }
                break;
            }

            if(interceptorType.GetNuclear())
            {               
                for(Airplane aircraft : GetAirplanes())
                {
                    if(aircraft.GetPosition().RadiusPhaseCollisionTest(interceptor.GetPosition(), interceptorType.GetBlastRadius()))
                    {
                        if(aircraft.GetPosition().DistanceTo(interceptor.GetPosition()) < interceptorType.GetBlastRadius())
                        {
                            if(random.nextFloat() < config.GetInterceptorNukeKillChance())
                            {
                                Player aircraftOwner = GetOwner(aircraft);
                                short damageInflicted = aircraft.InflictDamage(aircraft.GetMaxHP());

                                Scoring_DamageInflicted(interceptorOwner, aircraftOwner, damageInflicted); 

                                ProcessPlayerXPGain(interceptorOwner.GetID(), Defs.AIRCRAFT_KILLED_XP, "You destroyed an aircraft.");
                                ProcessPlayerXPLoss(aircraftOwner.GetID(), Defs.AIRCRAFT_LOST_XP, "Your aircraft was destroyed.");

                                if(interceptorOwner.ApparentlyEquals(aircraftOwner))
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own %s.", interceptorOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_HIT));
                                    CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));
                                }
                                else
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's %s.", interceptorOwner.GetName(), aircraftOwner.GetName(), aircraft.GetTypeName()), SoundEffect.INTERCEPTOR_HIT));
                                    CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's %s.", aircraftOwner.GetName(), aircraft.GetTypeName()), false));               
                                    CreateReport(aircraftOwner, new LaunchReport(String.format("%s's interceptor shot down your %s.", interceptorOwner.GetName(), aircraft.GetTypeName()), false, aircraftOwner.GetID(), interceptorOwner.GetID()));
                                    CreateSalvage(aircraft.GetPosition(), GetAircraftValue(aircraft));                    
                                }
                            }
                        }
                    }
                }

                for(Missile missile : GetMissiles())
                {
                    MissileType missileType = config.GetMissileType(missile.GetType());

                    if(missileType != null && (!missileType.GetICBM() || interceptorType.GetABM()))
                    {
                        if(missile.GetPosition().RadiusPhaseCollisionTest(interceptor.GetPosition(), interceptorType.GetBlastRadius()))
                        {
                            if(missile.GetPosition().DistanceTo(interceptor.GetPosition()) < interceptorType.GetBlastRadius())
                            {
                                if(random.nextFloat() < config.GetInterceptorNukeKillChance())
                                {
                                    Player missileOwner = GetOwner(missile);

                                    if(missileType.GetICBM())
                                    {
                                        missile.Destroy();
                                        EntityUpdated(missile, false);

                                        ProcessPlayerXPGain(interceptorOwner.GetID(), Defs.ICBM_SHOOTDOWN_XP, "You destroyed an ICBM.");                        

                                        if(interceptorOwner.ApparentlyEquals(missileOwner))
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own ICBM.", interceptorOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's ICBM.", interceptorOwner.GetName(), missileOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                            CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's ICBM.", missileOwner.GetName()), false, interceptorOwner.GetID(), missileOwner.GetID()));
                                            CreateReport(missileOwner, new LaunchReport(String.format("%s's interceptor shot down your ICBM.", interceptorOwner.GetName()), false, missileOwner.GetID(), interceptorOwner.GetID()));
                                        }
                                    }
                                    else
                                    {
                                        missile.Destroy();
                                        EntityUpdated(missile, false);

                                        if(interceptorOwner.ApparentlyEquals(missileOwner))
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down their own missile.", interceptorOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                        }
                                        else
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's interceptor shot down %s's missile.", interceptorOwner.GetName(), missileOwner.GetName()), SoundEffect.INTERCEPTOR_HIT));
                                            CreateReport(interceptorOwner, new LaunchReport(String.format("Your interceptor shot down %s's missile.", missileOwner.GetName()), false, interceptorOwner.GetID(), missileOwner.GetID()));
                                            CreateReport(missileOwner, new LaunchReport(String.format("%s's interceptor shot down your missile.", interceptorOwner.GetName()), false, missileOwner.GetID(), interceptorOwner.GetID()));
                                        }
                                    }
                                }
                            }
                        }
                    }            
                }
            }
        }    
    }

    @Override
    protected void EntityUpdated(LaunchEntity entity, boolean bOwner)
    {
        if(entity instanceof StoredAirplane)
        {
            StoredAirplane aircraft = (StoredAirplane)entity;
            MapEntity entityToUpdate = aircraft.GetHomeBase().GetMapEntity(this);
            
            comms.EntityUpdated(entityToUpdate, bOwner);
        }
        else
        {
            comms.EntityUpdated(entity, bOwner);
        }
    }
    
    @Override
    protected void EntityAdded(LaunchEntity entity)
    {
        if(entity instanceof MapEntity)
            quadtree.InsertEntity(entity.GetPointer());
    }

    @Override
    protected void AllianceUpdated(Alliance alliance, boolean bMajor)
    {
        comms.AllianceUpdated(alliance, bMajor);
    }

    @Override
    protected void AllianceRemoved(Alliance alliance)
    {
        comms.AllianceRemoved(alliance);
    }

    @Override
    protected void EntityRemoved(LaunchEntity entity, boolean bDontCommunicate)
    {
        if(!bDontCommunicate)
        {
            comms.EntityRemoved(entity);
        }
        
        Player owner = GetOwner(entity);
        
        if(owner != null)
        {
            if(entity instanceof Missile)
                owner.RemoveMissile(entity.GetID());
            
            if(entity instanceof Structure)
                owner.RemoveStructure((Structure)entity);
            
            if(entity instanceof Airplane)
                owner.RemoveAircraft(entity.GetID());
            
            if(entity instanceof StoredAirplane)
                owner.RemoveStoredAircraft(entity.GetID());
            
            if(entity instanceof SAMSite)
                owner.RemoveSAMSite(entity.GetID());
            
            if(entity instanceof Blueprint)
                owner.RemoveBlueprint(entity.GetID());
            
            if(entity instanceof Warehouse)
                owner.RemoveWarehouse(entity.GetID());
            
            if(entity instanceof Ship)
                owner.RemoveShip(entity.GetID());
            
            if(entity instanceof Tank)
                owner.RemoveTank(entity.GetID());
        }
        
        if(entity instanceof Missile)
        {
            Missile missile = ((Missile)entity);
            
            for(int lPlayerID : missile.GetStructureThreatenedPlayers())
            {
                Player target = Players.get(lPlayerID);
                
                if(target != null)
                    target.RemoveHostileMissile(missile.GetID());
            }
        }
        
        if(entity instanceof Airplane)
        {
            for(Player player : GetPlayers())
            {
                player.RemoveNearbyAircraft(entity.GetID());
            }
        }
        
        if(entity instanceof MapEntity)
        {
            quadtree.RemoveEntity(entity.GetPointer());
        }
    }

    @Override
    protected void TreatyUpdated(Treaty treaty)
    {
        comms.TreatyCreated(treaty);
    }

    @Override
    protected void TreatyRemoved(Treaty treaty)
    {
        comms.TreatyRemoved(treaty);
        
        Alliance alliance1 = GetAlliance(treaty.GetAllianceID1());
        Alliance alliance2 = GetAlliance(treaty.GetAllianceID2());
        
        if(alliance1 != null)
            alliance1.RemoveTreaty(treaty.GetID());
        
        if(alliance2 != null)
            alliance2.RemoveTreaty(treaty.GetID());
    }
    
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // LaunchServerGameInterface methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    @Override
    public User VerifyID(String strGoogleID)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Verifying ID %s.", strGoogleID));
        return Users.get(strGoogleID);
    }
    
    @Override
    public boolean MigrateAccount(String strOldIMEI, String strGoogleID)
    {
        LaunchLog.ConsoleMessage("Attempting to migrate account... [LaunchServerGame]");
        User user = Users.get(strOldIMEI);
        
        if(user != null)
        {
            Users.remove(strOldIMEI);
            user.SetGoogleID(strGoogleID);
            Users.put(strGoogleID, user);
            LaunchLog.ConsoleMessage("Account migrated successully. [LaunchServerGame]");
            
            return true;
        }
        else
        {
            LaunchLog.ConsoleMessage("Could not migrate account. User null. [LaunchServerGame]");
        }
        
        return false;
    }

    @Override
    public int GetGameConfigChecksum()
    {
        return config.GetChecksum();
    }
    
    @Override
    public boolean CheckDeviceIDAlreadyUsed(byte[] cDeviceID)
    {
        String strDeviceID = Arrays.toString(cDeviceID);
        
        if(strDeviceID != null)
        {
            for(User user : Users.values())
            {
                if(user.GetDeviceID().equals(strDeviceID))
                    return true;
            }
        } 
        
        return false;
    }

    @Override
    public boolean CheckPlayerNameAvailable(String strPlayerName)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Checking availability of %s...", strPlayerName));
        
        //Check players in game.
        for(Player player : GetPlayers())
        {
            if(player.GetName().equals(strPlayerName))
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "It's taken.");
                return false;
            }
        }
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "It's available.");
        return true;
    }

    @Override
    public User CreateAccount(String strIMEI, String strGoogleID, String strPlayerName, int lAvatarID)
    {
        strPlayerName = LaunchUtilities.BlessName(strPlayerName);
        Player player = CreatePlayer(strPlayerName, lAvatarID);
        User user = new User(strIMEI, strGoogleID, player.GetID());
        player.SetUser(user);
        Users.put(strGoogleID, user);
		
        counterShield.onDeviceHashUpdate(System.currentTimeMillis(), player.GetID(), user.GetDeviceID());
        counterShield.checkFingerprintAsync(player.GetID(), user.GetDeviceID());
        counterShield.checkIpAsync(player.GetID(), user.GetLastIP());
		
        CreateEvent(new LaunchEvent(String.format("Give a warm, explosive welcome to %s, who has joined the Game!", player.GetName()), SoundEffect.RESPAWN));
        CreateReport(new LaunchReport(String.format("Give a warm, explosive welcome to %s, who has joined the Game!", player.GetName()), false, player.GetID()));
        
        return user;
    }
    
    @Override
    public boolean SpoofLocation(int lPlayerID, LaunchClientLocation spoofedLocation)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null && player.GetBoss() && player.Functioning())
        {
            player.SetPosition(spoofedLocation.GetGeoCoord());
            UpdateTrackingMissileThreats(lPlayerID);
            GeoCoord geoPlayer = player.GetPosition();
            
            for(Integer lID : new ArrayList<>(player.GetBlueprints()))
            {
                Blueprint blueprint = Blueprints.get(lID);

                if(blueprint != null && blueprint.GetOwnedBy(player.GetID()))
                {                
                    if(blueprint.GetPosition().DistanceTo(geoPlayer) <= Defs.BLUEPRINT_CONSTRUCT_DISTANCE)
                    {                    
                        if(ConstructStructureFromBlueprint(player.GetID(), blueprint.GetID()))
                        {
                            blueprint.SetRemove();
                            EntityUpdated(blueprint, true);
                            CreateReport(player, new LaunchReport(String.format("You automatically built a structure from a blueprint."), true));
                        }
                        else
                        {
                            CreateReport(player, new LaunchReport(String.format("You were unable to build a structure from your blueprint. It has been left in place."), true));
                        }
                    }
                }
                else
                {
                    player.RemoveBlueprint(lID);
                }
            }

            //Process loot collection.
            for(Loot loot : GetLoots())
            {
                if(loot.GetLootType() == LootType.RESOURCES && geoPlayer.BroadPhaseCollisionTest(loot.GetPosition()))
                {
                    if(geoPlayer.DistanceTo(loot.GetPosition()) <= config.GetOreCollectRadius())
                    {
                        if(!loot.Collected())
                        {
                            player.AddWealth(loot.GetQuantity());
                            loot.SetQuantity(0);
                        }
                    }
                }
            }
            
            EntityUpdated(player, false);
            
            return true;
        }
        
        return false;
    }

    @Override
    public void UpdatePlayerLocation(int lPlayerID, LaunchClientLocation location)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            long oPrevTime = player.GetLastSeen();
            player.SetLastSeen();
            
            if(player.Functioning() && !player.GetBoss())
            {
                GeoCoord geoPrevPosition = player.GetPosition().GetCopy();
                
                long oNow = System.currentTimeMillis();
                double dblSeconds = (oNow - Defs.ON_THE_MOVE_TIME_THRESHOLD)/1000;
                
                if(geoPrevPosition.GetValid() && location.GetGeoCoord().GetValid())
                {
                    float fltDistanceTraveled = geoPrevPosition.DistanceTo(location.GetGeoCoord());

                    player.Traveled(fltDistanceTraveled);
                }  
                
                player.SetPosition(location.GetGeoCoord());
                UpdateTrackingMissileThreats(lPlayerID);

                if(player.GetLastSeen() - oPrevTime < Defs.ON_THE_MOVE_TIME_THRESHOLD)
                {
                    //Player seems to be on the move. Process the update in steps so they don't "miss" things.
                    while(geoPrevPosition.MoveToward(player.GetPosition(), Defs.ON_THE_MOVE_STEP_DISTANCE) == false)
                    {
                        ProcessPlayerLocationIteration(player, geoPrevPosition, false);
                    }
                }

                ProcessPlayerLocationIteration(player, player.GetPosition(), true);
                
                //if(player.GetUser() != null)
                    //counterShield.onLocationUpdate(oNow, lPlayerID, geoPrevPosition, location.GetGeoCoord(), dblSeconds, Provider.GPS, player.GetUser().GetLastIP());
                
                EntityUpdated(player, false);
            }
            
            if(player.GetAWOL())
            {
                player.SetAWOL(false);
                CreateEvent(new LaunchEvent(String.format("%s is back!", player.GetName())));
            }
        }
    }
    
    /**
     * Process a single player location. This function allows either single or "linear" changes in player location, so they can collect and repair while on the move even if they "miss".
     */
    private void ProcessPlayerLocationIteration(Player player, GeoCoord geoPlayer, boolean bUpdate)
    {
        for(Integer lID : new ArrayList<>(player.GetBlueprints()))
        {
            if(lID != null)
            {
                Blueprint blueprint = Blueprints.get(lID);

                if(blueprint != null && blueprint.GetOwnedBy(player.GetID()))
                {                
                    if(blueprint.GetPosition().DistanceTo(geoPlayer) <= Defs.BLUEPRINT_CONSTRUCT_DISTANCE)
                    {                    
                        if(ConstructStructureFromBlueprint(player.GetID(), blueprint.GetID()))
                        {
                            Blueprints.remove(lID);
                            EntityRemoved(blueprint, true);
                            CreateReport(player, new LaunchReport(String.format("You automatically built a structure from a blueprint."), true));
                        }
                        else
                        {
                            CreateReport(player, new LaunchReport(String.format("You were unable to build a structure from your blueprint. It has been left in place."), true));
                        }
                    }
                }
                else
                    player.RemoveBlueprint(lID);
            } 
        }
        
        //Process loot collection.
        for(Loot loot : GetLoots())
        {
            if(loot.GetLootType() == LootType.RESOURCES && geoPlayer.BroadPhaseCollisionTest(loot.GetPosition()))
            {
                if(geoPlayer.DistanceTo(loot.GetPosition()) <= config.GetOreCollectRadius())
                {
                    if(!loot.Collected())
                    {
                        player.AddWealth(loot.GetQuantity());
                        loot.SetQuantity(0);
                        
                        CreateEvent(new LaunchEvent(String.format("%s collected %s.", player.GetName(), Resource.GetTypeName(ResourceType.values()[loot.GetCargoID()])), SoundEffect.MONEY));
                    }
                }
            }
        }
        
        if(bUpdate)
            EntityUpdated(player, false);
    }
    
    private boolean ValidateConstructionRequest(Player player, GeoCoord geoCheck, String structureName, long  oCost)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Validating %s's %s construction attempt...", player.GetName(), structureName));
        
        if(player.Functioning() && geoCheck != null)
        {
            List<Structure> Result = new ArrayList<>();
            GeoCoord geoPosition = geoCheck;

            for(Structure structure : GetAllStructures())
            {
                if(structure.GetPosition().DistanceTo(geoPosition) < config.GetStructureSeparation(structure.GetEntityType()))
                {
                    Result.add(structure);
                }
            }
        
            if(Result.isEmpty())
            {
                if(player.SubtractWealth(oCost))
                {
                    return true;
                }
                else
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Unaffordable.");
                }
            }
            else
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Nearby structures.");
            }
        }
        else
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Player is not functioning.");
        }
        
        return false;
    }
    
    private boolean ValidateProcessorConstructionRequest(Player player, GeoCoord geoCheck, ResourceType type, boolean bUseSubstitutes)
    {
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Validating %s's %s construction attempt...", player.GetName(), "processor"));
        
        if(player.Functioning() && geoCheck != null)
        {
            List<Structure> Result = new ArrayList<>();
            GeoCoord geoPosition = geoCheck;

            for(Structure structure : GetAllStructures())
            {
                if(structure.GetPosition().DistanceTo(geoPosition) < config.GetStructureSeparation(structure.GetEntityType()))
                {
                    Result.add(structure);
                }
            }
            
            if(Result.isEmpty())
            {
                if(ProcessPlayerPurchase(player.GetID(), Defs.GetProcessorCost(type), null, null, bUseSubstitutes, PurchaseType.ECONOMIC))
                {
                    return true;
                } 
                else
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Player does not have necessary resources.");
                }
            }
            else
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Nearby structures.");
            }
        }
        else
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Player is not functioning.");
        }
        
        return false;
    }
    
    private boolean ProcessPlayerPurchase(int lPlayerID, Map<ResourceType, Long> costs, ResourceSystem resources, CargoSystem cargo, boolean bUseSubstitutes, PurchaseType purchaseType)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Validating %s's ship purchase attempt...", player.GetName()));

            if(player.Functioning())
            {
                if(bUseSubstitutes && ((resources != null && resources.ChargeQuantities(costs)) || (cargo != null && cargo.ChargeQuantities(costs))))
                {
                    return true;
                }
                else if(player.SubtractWealth(costs.get(ResourceType.WEALTH)))
                {
                    return true;
                }
                else
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Unaffordable.");
                }
            }
            else
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Player is not functioning.");
            }
        }
        
        return false;
    }
    
    private boolean ProcessPlayerWeaponPurchase(int lPlayerID, long oCost, boolean bOffensive)
    {
        //This one works differently because weapons do require resources to be used in some instances. For example, fissile for nukes, nerve agent for chemical weapons.
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Validating %s's weapon purchase attempt...", player.GetName()));

            if(player.Functioning())
            {
                if(player.SubtractWealth(oCost))
                {
                    if(bOffensive)
                    {
                        Scoring_OffenceSpending(player, oCost);
                    }
                    else
                    {
                        Scoring_DefenceSpending(player, oCost);
                    }
                    
                    return true;
                }
                else
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Unaffordable.");
                }
            }
            else
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Invalid. Player is not functioning.");
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PlaceBlueprint(int lPlayerID, EntityType type, ResourceType resourceType, GeoCoord geoPosition)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null && geoPosition.GetValid() && BlueprintFitsInLocation(type, geoPosition))
        {
            if(player.SubtractWealth(config.GetBlueprintCost()))
            {
                Blueprint blueprint = new Blueprint(GetAtomicID(lBlueprintIndex, Blueprints), geoPosition, config.GetBlueprintExpiry(), lPlayerID, type, resourceType);
                AddBlueprint(blueprint);
                CreateEvent(new LaunchEvent(String.format("%s placed a blueprint.", player.GetName()), SoundEffect.CONSTRUCTION));
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean ElectronicWarfare(int lPlayerID, int lAircraftID, EntityPointer target)
    {
        MapEntity targetEntity = target.GetMapEntity(this);
        Player player = GetPlayer(lPlayerID);
        Airplane aircraft = GetAirplane(lAircraftID);
        
        if(player != null && aircraft != null && targetEntity != null)
        {
            if(aircraft.EWReady() && ElectronicWarfareTargetValid(targetEntity) && !WouldBeFriendlyFire(GetOwner(targetEntity), player) && targetEntity.GetPosition().DistanceTo(aircraft.GetPosition()) <= Defs.ELECTRONIC_WARFARE_RANGE)
            {
                aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                targetEntity.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                
                if(targetEntity instanceof Structure)
                {
                    Structure structure = ((Structure)targetEntity);
                    Player structureOwner = GetOwner(structure);
                    
                    if(structureOwner != null)
                    {
                        structure.Reboot(Defs.ELECTRONIC_WARFARE_REBOOT_TIME);
                        aircraft.ElectronicWarfareUsed();

                        CreateEvent(new LaunchEvent(String.format("%s rebooted %s's structure using electronic warfare!", player.GetName(), structureOwner.GetName())));
                        CreateReport(structureOwner, new LaunchReport(String.format("%s disabled your %s!", player.GetName(), structure.GetTypeName()), true, structureOwner.GetID(), player.GetID()));
                        
                        structureOwner.AddHostilePlayer(lPlayerID);
                        
                        return true;                            
                    } 
                }
                else if(targetEntity instanceof Interceptor)
                {
                    Interceptor interceptor = ((Interceptor)targetEntity);
                    Player interceptorOwner = GetOwner(interceptor);
                    InterceptorType type = config.GetInterceptorType(interceptor.GetType());
                    
                    if(interceptorOwner != null && type != null && !type.GetABM())
                    {
                        aircraft.ElectronicWarfareUsed();
                        interceptor.Traveled(type.GetInterceptorRange());
                        CreateEvent(new LaunchEvent(String.format("%s destroyed %s's interceptor using electronic warfare!", player.GetName(), interceptorOwner.GetName())));
                        CreateReport(interceptorOwner, new LaunchReport(String.format("%s disabled your interceptor!", player.GetName()), true, interceptorOwner.GetID(), player.GetID()));
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean CallAirdrop(int lPlayerID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning() && player.GetCanCallAirdrop())
        {
            player.CalledAirdrop(Defs.AIRDROP_COOLDOWN);
            EntityUpdated(player, true);
            GeoCoord geoArrival = player.GetPosition().GetCopy();
            geoArrival.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * Defs.MAX_AIRDROP_DISTANCE);
            Airdrop airdrop = new Airdrop(GetAtomicID(lAirdropIndex, Airdrops), player.GetID(), geoArrival, Defs.AIRDROP_ARRIVAL_TIME);
            AddAirdrop(airdrop);
            
            CreateEventForPlayer(new LaunchEvent("Airdrop called.", SoundEffect.AIRDROP), lPlayerID);
            
            User user = player.GetUser();
            
            if(user != null)
                SendUserAlert(user, "Airdrop Inbound!", "Supplies have been dropped near your location.", false, true);

            return true;
        }
        
        return false;
    }
    
    @Override
    public void InfantryReachedTarget(Infantry infantry)
    {
        if(infantry != null && infantry.GetTarget() != null)
        {
            MapEntity target = infantry.GetTarget().GetMapEntity(this);
            
            if(target != null)
            {
                Player player = GetOwner(infantry);

                if(player != null)
                {
                    if(infantry.GetMoveOrders() == MoveOrders.CAPTURE)
                    {
                        boolean bStuffCaptured = false;

                        if(target instanceof Structure structure)
                        {
                            if(!structure.GetRespawnProtected())
                            {
                                if(!WouldBeFriendlyFire(GetOwner(structure), player) && !GetAttackIsBullying(player, GetOwner(structure)))
                                {
                                    Player targetOwner = GetOwner(structure);
                                    structure.Capture(infantry.GetOwnerID());

                                    bStuffCaptured = true;

                                    if(targetOwner != null)
                                    {
                                        CreateEvent(new LaunchEvent(String.format("%s's infantry captured %s's %s", player.GetName(), targetOwner.GetName(), structure.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                                        CreateReport(targetOwner, new LaunchReport(String.format("%s's infantry captured your %s!", player.GetName(), structure.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                        CreateReport(player, new LaunchReport(String.format("Your infantry captured %s's %s!", targetOwner.GetName(), structure.GetTypeName(), structure.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                    }

                                    if(structure instanceof Airbase airbase)
                                    {
                                        for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                                        {
                                            aircraft.Capture(player.GetID());
                                            player.AddOwnedEntity(aircraft);
                                        }
                                    }
                                    else if(structure instanceof MissileSite site && site.CanTakeICBM())
                                    {
                                        if(random.nextFloat() <= Defs.CAPTURE_ICBM_SILO_MAD_CHANCE)
                                        {
                                            ProcessMAD(player, targetOwner, String.format("%s captured %s's %s.", player.GetName(), targetOwner.GetName(), structure.GetTypeName()));
                                        }
                                    }
                                    else if(structure instanceof Armory armory)
                                    {
                                        for(StoredTank tank : armory.GetCargoSystem().GetTanks())
                                        {
                                            tank.Capture(player.GetID());
                                            player.AddOwnedEntity(tank);
                                        }

                                        for(StoredCargoTruck truck : armory.GetCargoSystem().GetCargoTrucks())
                                        {
                                            truck.Capture(player.GetID());
                                            player.AddOwnedEntity(truck);
                                        }

                                        for(StoredInfantry storedInfantry : armory.GetCargoSystem().GetInfantries())
                                        {
                                            storedInfantry.Capture(player.GetID());
                                            player.AddOwnedEntity(storedInfantry);
                                        }
                                    }

                                    EntityUpdated(structure, false);
                                }
                            }  
                        }
                        else if(target instanceof Shipyard shipyard)
                        {
                            if(!WouldBeFriendlyFire(GetOwner(shipyard), player) && !GetAttackIsBullying(player, GetOwner(shipyard)))
                            {
                                Player targetOwner = GetOwner(shipyard);
                                shipyard.Capture(infantry.GetOwnerID());

                                bStuffCaptured = true;

                                if(targetOwner != null)
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's infantry captured %s's %s", player.GetName(), targetOwner.GetName(), shipyard.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                                    CreateReport(targetOwner, new LaunchReport(String.format("%s's infantry captured your %s!", player.GetName(), shipyard.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                    CreateReport(player, new LaunchReport(String.format("Your infantry captured %s's %s!", targetOwner.GetName(), shipyard.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                }
                                else
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's infantry captured a %s", player.GetName(), shipyard.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                                    CreateReport(player, new LaunchReport(String.format("Your infantry captured a %s!", shipyard.GetTypeName()), true, player.GetID()));
                                }

                                EntityUpdated(shipyard, false);
                            } 
                        }
                        else if(target instanceof CargoTruck truck)
                        {
                            Player owner = GetOwner(truck);

                            if(owner != null)
                            {
                                if(!owner.GetRespawnProtected())
                                {
                                    if(!WouldBeFriendlyFire(GetOwner(truck), player) && !GetAttackIsBullying(player, owner))
                                    {
                                        truck.Capture(infantry.GetOwnerID());

                                        bStuffCaptured = true;

                                        CreateEvent(new LaunchEvent(String.format("%s's infantry captured %s's %s", player.GetName(), owner.GetName(), truck.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                                        CreateReport(owner, new LaunchReport(String.format("%s's infantry captured your %s!", player.GetName(), truck.GetTypeName()), true, owner.GetID(), player.GetID()));
                                        CreateReport(player, new LaunchReport(String.format("Your infantry captured %s's %s!", owner.GetName(), owner.GetName(), truck.GetTypeName()), true, player.GetID(), owner.GetID()));
                                    }
                                }
                            }  
                        }

                        if(!bStuffCaptured)
                        {
                            infantry.Wait();
                        }
                        else
                        {
                            infantry.InflictDamage(infantry.GetHP());
                        }

                        EntityUpdated(infantry, false);
                    }
                    else if(infantry.GetMoveOrders() == MoveOrders.LIBERATE)
                    {
                        boolean bStuffLiberated = false;

                        if(target instanceof Structure structure)
                        {
                            if(!structure.GetRespawnProtected())
                            {
                                if(GetCanBeLiberated(player, structure))
                                {
                                    //TODO: If the warehouse is not a logistics depot, it can be captured.
                                    if(!(structure instanceof Warehouse))
                                    {
                                        Player targetOwner = GetOwner(structure);
                                        structure.Liberate();

                                        bStuffLiberated = true;

                                        if(targetOwner != null)
                                        {
                                            CreateEvent(new LaunchEvent(String.format("%s's infantry liberated %s's %s", player.GetName(), targetOwner.GetName(), structure.GetTypeName()), SoundEffect.LIBERATE));
                                            CreateReport(targetOwner, new LaunchReport(String.format("%s's infantry liberated your %s!", player.GetName(), structure.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                            CreateReport(player, new LaunchReport(String.format("Your infantry liberated %s's %s!", targetOwner.GetName(), structure.GetTypeName(), structure.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                        
                                            if(structure instanceof Airbase airbase)
                                            {
                                                for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                                                {
                                                    aircraft.Capture(airbase.GetBuiltByID());
                                                    targetOwner.AddOwnedEntity(aircraft);
                                                }
                                            }
                                            else if(structure instanceof Armory armory)
                                            {
                                                for(StoredTank tank : armory.GetCargoSystem().GetTanks())
                                                {
                                                    tank.Capture(armory.GetBuiltByID());
                                                    targetOwner.AddOwnedEntity(tank);
                                                }

                                                for(StoredCargoTruck truck : armory.GetCargoSystem().GetCargoTrucks())
                                                {
                                                    truck.Capture(armory.GetBuiltByID());
                                                    targetOwner.AddOwnedEntity(truck);
                                                }

                                                for(StoredInfantry storedInfantry : armory.GetCargoSystem().GetInfantries())
                                                {
                                                    storedInfantry.Capture(armory.GetBuiltByID());
                                                    targetOwner.AddOwnedEntity(storedInfantry);
                                                }
                                            }
                                        }

                                        EntityUpdated(structure, false);
                                    }
                                }
                            }  
                        }
                        else if(target instanceof Shipyard shipyard)
                        {
                            if(GetCanBeLiberated(player, shipyard))
                            {
                                Player targetOwner = GetOwner(shipyard);
                                shipyard.Abandon();

                                bStuffLiberated = true;

                                if(targetOwner != null)
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's infantry liberated %s's %s", player.GetName(), targetOwner.GetName(), shipyard.GetTypeName()), SoundEffect.LIBERATE));
                                    CreateReport(targetOwner, new LaunchReport(String.format("%s's infantry liberated your %s!", player.GetName(), shipyard.GetTypeName()), true, targetOwner.GetID(), player.GetID()));
                                    CreateReport(player, new LaunchReport(String.format("Your infantry liberated %s's %s!", targetOwner.GetName(), shipyard.GetTypeName(), shipyard.GetTypeName()), true, player.GetID(), targetOwner.GetID()));
                                }
                                else
                                {
                                    CreateEvent(new LaunchEvent(String.format("%s's infantry liberated a %s", player.GetName(), shipyard.GetTypeName()), SoundEffect.LIBERATE));
                                    CreateReport(player, new LaunchReport(String.format("Your infantry liberated a %s!", shipyard.GetTypeName()), true, player.GetID()));
                                }

                                EntityUpdated(shipyard, false);
                            } 
                        }

                        if(!bStuffLiberated)
                        {
                            infantry.Wait();
                        }
                        else
                        {
                            infantry.InflictDamage(infantry.GetHP());
                        }

                        EntityUpdated(infantry, false);
                    }
                } 
            }
        }
    }
    
    /**
     * @param lPlayerID
     * @param structureType
     * @param type only used if this is constructing a processor or extractor. Used to determine what type of processor/extractor it will be. otherwise it'll be null.
     * @return 
     */
    @Override
    public boolean ConstructStructure(int lPlayerID, EntityType structureType, ResourceType type, int lCommandPostID, GeoCoord geoRemotebuild, boolean bUseSubstitutes)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            Map<ResourceType, Long> Costs = config.GetStructureBuildCost(structureType, type);
            GeoCoord geoPosition = player.GetPosition().GetCopy();
            
            if(bUseSubstitutes)
                Costs = GetSubstitutionCost(Costs);
            else
                Costs = GetRequiredCost(Costs);
            
            if(lCommandPostID != LaunchEntity.ID_NONE && structureType != EntityType.COMMAND_POST)
            {
                if(GetCommandPost(lCommandPostID) != null)
                {
                    CommandPost post = GetCommandPost(lCommandPostID);
                    
                    if(post.GetOwnedBy(lPlayerID) && post.GetOnline() && !GetRadioactive(post, false))
                    {
                        geoPosition = geoRemotebuild;
                    }
                }
            }
            else if(player.GetStructures().size() < 10)
            {
                //This is an in-person build and the player has less than 10 structures. Offset the location to protect player home privacy.
                geoPosition.Move(random.nextDouble() * (2.0 * Math.PI), Defs.PRIVACY_OFFSET);
            }
            
            switch(structureType)
            {
                case PROCESSOR:
                {
                    if(ValidateProcessorConstructionRequest(player, geoPosition, type, bUseSubstitutes))
                    {
                        return AddStructure(lPlayerID, geoPosition, structureType, type);
                    }
                }
                break;
                
                case ORE_MINE:
                {
                    ResourceDeposit nearbyDeposit = null;
                    
                    for(ResourceDeposit deposit : GetResourceDeposits())
                    {
                        if(deposit.GetPosition().DistanceTo(geoPosition) <= Defs.DEPOSIT_RADIUS)
                        {
                            nearbyDeposit = deposit;
                            break;
                        }
                    }
                    
                    if(nearbyDeposit != null)
                    {
                        if(ValidateConstructionRequest(player, geoPosition, structureType.toString(), Costs.get(ResourceType.WEALTH)))
                        {
                            return AddStructure(lPlayerID, geoPosition, structureType, nearbyDeposit.GetType());
                        }
                    }
                    else
                    {
                        comms.ShowBasicOKDialog("There are no resource deposits nearby. You cannout build an extractor here.", true, lPlayerID);
                    }
                }
                break;
                
                default:
                {
                    if(ValidateConstructionRequest(player, geoPosition, structureType.toString(), Costs.get(ResourceType.WEALTH)))
                    {
                        return AddStructure(lPlayerID, geoPosition, structureType, type);
                    }
                }
                break;
            }
        }
        
        return false;
    }
    
    public boolean ConstructStructureFromBlueprint(int lPlayerID, int lBlueprintID)
    {
        Player player = Players.get(lPlayerID);
        Blueprint blueprint = Blueprints.get(lBlueprintID);
        
        if(player != null && blueprint != null)
        {
            EntityType structureType = blueprint.GetType();
            ResourceType resourceType = blueprint.GetResourceType();
            Map<ResourceType, Long> Costs = GetRequiredCost(config.GetStructureBuildCost(structureType, resourceType));
            
            if(structureType == EntityType.PROCESSOR)
            {
                if(ValidateProcessorConstructionRequest(player, blueprint.GetPosition().GetCopy(), resourceType, false))
                {
                    return AddStructure(lPlayerID, blueprint.GetPosition().GetCopy(), structureType, resourceType);
                }
            }
            else if(structureType == EntityType.ORE_MINE)
            {
                if(ValidateConstructionRequest(player, blueprint.GetPosition().GetCopy(), structureType.toString(), Costs.get(ResourceType.WEALTH)))
                {
                    return AddStructure(lPlayerID, blueprint.GetPosition().GetCopy(), structureType, resourceType);
                }
            }
            else if(ValidateConstructionRequest(player, blueprint.GetPosition().GetCopy(), structureType.toString(), Costs.get(ResourceType.WEALTH)))
            {
                return AddStructure(lPlayerID, blueprint.GetPosition().GetCopy(), structureType, resourceType);
            }
        }
        
        
        return false;
    }
    
    public boolean AddStructure(int lPlayerID, GeoCoord geoPosition, EntityType structureType, ResourceType resourceType)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            switch(structureType)
            {
                case MISSILE_SITE: 
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Affordable...");
                                                                                                            //Made Missile Site health 350 HP. TODO: Add Missile Site health to Config. -Corbin
                    MissileSite missileSite = new MissileSite(GetAtomicID(lMissileSiteIndex, MissileSites), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), config.GetReloadTimeBase(), config.GetInitialMissileSlots(), false, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.MISSILE_SITE_TYPES));
                    AddMissileSite(missileSite);
                    EstablishStructureThreats(missileSite);
                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(missileSite.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), missileSite.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case NUCLEAR_MISSILE_SITE:
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Affordable...");
                                                                                                                                   //Made nuke silo HP 750. TODO: Add NukeSiloHealth to Config. -Corbin
                    MissileSite missileSite = new MissileSite(GetAtomicID(lMissileSiteIndex, MissileSites), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), config.GetReloadTimeBase(), config.GetInitialMissileSlotsNuclear(), true, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.ICBM_SILO_TYPES));
                    AddMissileSite(missileSite);
                    EstablishStructureThreats(missileSite);
                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(missileSite.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), missileSite.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case SAM_SITE:
                {
                    SAMSite samSite = new SAMSite(GetAtomicID(lSAMSiteIndex, SAMSites), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), config.GetReloadTimeBase(), config.GetInitialInterceptorSlots(), false, 0, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.SAM_SITE_TYPES));
                    AddSAMSite(samSite);
                    EstablishStructureThreats(samSite);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(samSite.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), samSite.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case ABM_SILO:
                {                   
                    SAMSite samSite = new SAMSite(GetAtomicID(lSAMSiteIndex, SAMSites), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), config.GetReloadTimeBase(), config.GetInitialABMSlots(), true, 0, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.ABM_SILO_TYPES));
                    AddSAMSite(samSite);
                    EstablishStructureThreats(samSite);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(samSite.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), samSite.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case SENTRY_GUN:
                {                                                                                                               //Made Sentry Gun heath 100 HP. TODO: Add Sentry Gun health in Config. -Corbin     
                    SentryGun sentryGun = new SentryGun(GetAtomicID(lSentryGunIndex, SentryGuns), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.SENTRY_GUN_TYPES), false);
                    AddSentryGun(sentryGun);
                    EstablishStructureThreats(sentryGun);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(sentryGun.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), sentryGun.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case ARTILLERY_GUN:
                {                                                                                                               //Made Sentry Gun heath 100 HP. TODO: Add Sentry Gun health in Config. -Corbin     
                    SentryGun sentryGun = new SentryGun(GetAtomicID(lSentryGunIndex, SentryGuns), geoPosition, Defs.WATCH_TOWER_HP, Defs.WATCH_TOWER_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.SENTRY_GUN_TYPES), true);
                    AddSentryGun(sentryGun);
                    EstablishStructureThreats(sentryGun);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(sentryGun.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), sentryGun.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case SCRAP_YARD:
                {
                    List<ResourceType> types = new ArrayList<>();
                    
                    for(ResourceType type : ResourceType.values())
                    {
                        if(type != ResourceType.NUCLEAR_ELECTRICITY && type != ResourceType.WEALTH)
                        {
                            types.add(type);
                        }
                    }
                    
                    ScrapYard yard = new ScrapYard(GetAtomicID(lScrapYardIndex, ScrapYards), geoPosition, Defs.SCRAP_YARD_HP, Defs.SCRAP_YARD_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Long.MAX_VALUE, types));
                    AddScrapYard(yard);
                    EstablishStructureThreats(yard);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(yard.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), yard.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case RADAR_STATION:
                {                                                                                                            //Made Radar Station HP 500. TODO: Add RadarStationHealth to Config. -Corbin
                    RadarStation radarStation = new RadarStation(GetAtomicID(lRadarStationIndex, RadarStations), geoPosition, Defs.RADAR_STATION_HP, Defs.RADAR_STATION_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.RADAR_STATION_TYPES));
                    AddRadarStation(radarStation);
                    EstablishStructureThreats(radarStation);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");
                    RemoveNearbyRubbles(radarStation.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), radarStation.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case COMMAND_POST:
                {
                    CommandPost commandPost = new CommandPost(GetAtomicID(lCommandPostIndex, CommandPosts), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.MISSILE_SITE_TYPES));
                    AddCommandPost(commandPost);
                    EstablishStructureThreats(commandPost);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(commandPost.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), commandPost.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case ORE_MINE:
                {
                    int lDepositID = LaunchEntity.ID_NONE;
                    
                    for(ResourceDeposit deposit : GetResourceDeposits())
                    {
                        if(deposit.GetPosition().DistanceTo(geoPosition) <= Defs.DEPOSIT_RADIUS)
                        {
                            lDepositID = deposit.GetID();
                            break;
                        }
                    }
                    
                    if(lDepositID != LaunchEntity.ID_NONE)
                    {
                        OreMine oreMine = new OreMine(GetAtomicID(lOreMineIndex, OreMines), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), lDepositID, resourceType, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.ORE_MINE_TYPES));
                        AddOreMine(oreMine);
                        EstablishStructureThreats(oreMine);

                        ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                        RemoveNearbyRubbles(oreMine.GetPosition());
                        CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), oreMine.GetTypeName()), SoundEffect.CONSTRUCTION));

                        return true;
                    }
                    
                    return false;   
                }

                case AIRBASE:
                {                                                                                                          
                    Airbase airbase = new Airbase(GetAtomicID(lAirbaseIndex, Airbases), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.AIRBASE_TYPES));
                    AddAirbase(airbase);
                    EstablishStructureThreats(airbase);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(airbase.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), airbase.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case ARMORY:
                {                                                                                                         
                    Armory armory = new Armory(GetAtomicID(lArmoryIndex, Armories), geoPosition, Defs.BARRACKS_MAX_HP, Defs.BARRACKS_MAX_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), false, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.ARMORY_TYPES));
                    AddArmory(armory);
                    EstablishStructureThreats(armory);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(armory.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), armory.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                /*case BARRACKS:
                {                                                                                                         
                    Armory armory = new Armory(GetAtomicID(lArmoryIndex, Armories), geoPosition, Defs.BARRACKS_MAX_HP, Defs.BARRACKS_MAX_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), true, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.BARRACKS_TYPES));
                    AddArmory(armory);
                    EstablishStructureThreats(armory);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(armory.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), armory.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }*/

                /*case ARTILLERY_GUN: 
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Affordable...");

                    ArtilleryGun artillery = new ArtilleryGun(GetAtomicID(lArtilleryGunIndex, ArtilleryGuns), geoPosition, Defs.ARTILLERY_GUN_HP, Defs.ARTILLERY_GUN_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), Defs.ARTILLERY_GUN_RELOAD, Defs.ARTILLERY_GUN_CAPACITY, new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.ARTILLERY_GUN_TYPES));
                    AddArtilleryGun(artillery);
                    EstablishStructureThreats(artillery);
                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(artillery.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), artillery.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }*/

                case WAREHOUSE:
                {
                    Warehouse warehouse = new Warehouse(GetAtomicID(lWarehouseIndex, Warehouses), geoPosition, StructureStats.GetMaxHPByType(structureType, this), StructureStats.GetMaxHPByType(structureType, this), player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), new ResourceSystem(Defs.STRUCTURE_RESOURCE_CAPACITY, Defs.WAREHOUSE_TYPES));
                    AddWarehouse(warehouse);
                    EstablishStructureThreats(warehouse);

                    ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                    RemoveNearbyRubbles(warehouse.GetPosition());
                    CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), warehouse.GetTypeName()), SoundEffect.CONSTRUCTION));

                    return true;
                }

                case PROCESSOR:
                {  
                    if(resourceType != null)
                    {
                        List<ResourceType> types = new ArrayList<>(Defs.BASIC_STRUCTURE_TYPES);
                        
                        //Only add the input types if they are not present. Outputs are dropped on the ground.
                        for(ResourceType type : Defs.GetProcessorInput(resourceType).keySet())
                        {
                            if(!types.contains(type))
                            {
                                types.add(type);
                            }
                        }
                        
                        Processor processor = new Processor(GetAtomicID(lProcessorIndex, Processors), geoPosition, Defs.PROCESSOR_MAX_HP, Defs.PROCESSOR_MAX_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), resourceType, new ResourceSystem(Long.MAX_VALUE, types));
                        AddProcessor(processor);
                        EstablishStructureThreats(processor);

                        ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                        RemoveNearbyRubbles(processor.GetPosition());
                        CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), processor.GetTypeName()), SoundEffect.CONSTRUCTION));

                        return true;
                    }
                }
                break;

                case DISTRIBUTOR:
                {  
                    if(resourceType != null)
                    {
                        List<ResourceType> types = new ArrayList<>(Defs.BASIC_STRUCTURE_TYPES);
                        
                        //Only add the input types if they are not present. Outputs are dropped on the ground.
                        if(!types.contains(resourceType))
                        {
                            types.add(resourceType);
                        }
                        
                        Distributor distributor = new Distributor(GetAtomicID(lDistributorIndex, Distributors), geoPosition, Defs.DISTRIBUTOR_MAX_HP, Defs.DISTRIBUTOR_MAX_HP, player.GetID(), player.GetRespawnProtected(), config.GetStructureBootTime(player), resourceType, new ResourceSystem(Defs.DISTRIBUTOR_RESOURCE_CAPACITY, types));
                        AddDistributor(distributor);
                        EstablishStructureThreats(distributor);

                        ProcessPlayerXPGain(lPlayerID, Defs.STRUCTURE_BUILT_XP, "You built a structure.");

                        RemoveNearbyRubbles(distributor.GetPosition());
                        CreateEvent(new LaunchEvent(String.format("%s constructed a %s.", player.GetName(), distributor.GetTypeName()), SoundEffect.CONSTRUCTION));

                        return true;
                    }
                }
                break;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PurchaseLaunchables(int lPlayerID, int lSiteID, int lSlotNo, List<Integer> lLaunchableTypes, SystemType systemType)
    {
        switch(systemType)
        {
            case SAM_SITE:
            case AIRCRAFT_INTERCEPTORS:
            case STORED_AIRCRAFT_INTERCEPTORS:
            case TANK_INTERCEPTORS: 
            case SHIP_INTERCEPTORS: return PurchaseInterceptors(lPlayerID, lSiteID, lSlotNo, lLaunchableTypes, systemType);
                
            case TANK_MISSILES:
            case STORED_TANK_ARTILLERY:
            case TANK_ARTILLERY:
            case MISSILE_SITE:
            case SUBMARINE_ICBM:
            case SUBMARINE_MISSILES:
            case SHIP_MISSILES:
            case STORED_AIRCRAFT_MISSILES:
            case ARTILLERY_GUN:
            case SHIP_ARTILLERY:
            case AIRCRAFT_MISSILES: return PurchaseMissiles(lPlayerID, lSiteID, lSlotNo, lLaunchableTypes, systemType);
                
            case SUBMARINE_TORPEDO:
            case SHIP_TORPEDOES: return PurchaseTorpedoes(lPlayerID, lSiteID, lSlotNo, lLaunchableTypes, systemType);
        }
        
        return false;
    }
    
    public boolean PurchaseMissiles(int lPlayerID, int lSiteID, int lSlotNo, List<Integer> lLaunchableTypes, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        boolean bGetCityTimeBoost = false;
        //boolean bHostIsUnderWay = false;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to purchase launchables...", player.GetName()));
            
            if(player.Functioning())
            {                
                switch(systemType)
                {
                    case TANK_MISSILES:
                    {
                        Tank tank = GetTank(lSiteID);
                        
                        if(tank != null)
                        {
                            if(tank.GetOwnerID() == lPlayerID)
                            {
                                system = tank.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case TANK_ARTILLERY:
                    {
                        Tank tank = GetTank(lSiteID);
                        
                        if(tank != null)
                        {
                            if(tank.GetOwnerID() == lPlayerID)
                            {
                                system = tank.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case MISSILE_SITE:
                    {
                        MissileSite missileSite = MissileSites.get(lSiteID);
                        
                        if(missileSite != null)
                        {
                            if(missileSite.GetOwnerID() == lPlayerID)
                            {
                                system = missileSite.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case ARTILLERY_GUN:
                    {
                        ArtilleryGun artillery = ArtilleryGuns.get(lSiteID);
                        
                        if(artillery != null)
                        {
                            if(artillery.GetOwnerID() == lPlayerID)
                            {
                                system = artillery.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SHIP_ARTILLERY:
                    {
                        Ship ship = Ships.get(lSiteID);
                        
                        if(ship != null && ship.HasArtillery())
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetArtillerySystem();
                            }
                        }
                    }

                    case STORED_AIRCRAFT_MISSILES:
                    {
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to purchase launchables for a stored aircraft...", player.GetName()));
                        StoredAirplane aircraft = GetStoredAirplane(lSiteID);
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("aircraft pulled from array..."));
                        
                        if(aircraft != null && aircraft.DoneBuilding() && !aircraft.Flying())
                        {
                            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("aircraft not null..."));
                            
                            if(aircraft.GetOwnerID() == lPlayerID)
                            {
                                system = aircraft.GetMissileSystem();
                            }   
                        }
                    }
                    break;

                    case SHIP_MISSILES:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SUBMARINE_MISSILES:
                    {
                        Submarine submarine = GetSubmarine(lSiteID);
                        
                        if(submarine != null)
                        {
                            if(submarine.GetOwnerID() == lPlayerID)
                            {
                                system = submarine.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SUBMARINE_ICBM:
                    {
                        Submarine submarine = GetSubmarine(lSiteID);
                        
                        if(submarine != null)
                        {
                            if(submarine.GetOwnerID() == lPlayerID)
                            {
                                system = submarine.GetICBMSystem();
                            }
                        }
                    }
                    break;
                    
                    default:
                    {
                        LaunchLog.ConsoleMessage(String.format("case %s not recognized.", systemType));
                    }
                    break;
                }
                
                
                if(system != null)
                {
                    boolean bNukesPurchased = false;
                    boolean bICBMPurchased = false;
                    
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("system not null...", player.GetName()));
                    
                    if(system.GetEmptySlotCount() >= lLaunchableTypes.size())
                    {
                        for(int lType : lLaunchableTypes)
                        {
                            MissileType type = config.GetMissileType(lType);

                            if(type != null)
                            {
                                int lPrepTime = MissileStats.GetMissileBuildTime(type, false, player.GetBoss());
                                
                                if(type.GetICBM())
                                {
                                    bNukesPurchased = true;
                                }
                                else if(type.GetNuclear())
                                {
                                    bNukesPurchased = true;
                                }
                                else if(type.GetAntiSubmarine())
                                {
                                    TorpedoType torpedoType = config.GetTorpedoType(type.GetTorpedoType());
                                    
                                    if(torpedoType != null && torpedoType.GetNuclear())
                                    {
                                        bNukesPurchased = true;
                                    }
                                }
                                
                                if(systemType == SystemType.STORED_AIRCRAFT_MISSILES)
                                {
                                    StoredAirplane aircraft = GetStoredAirplane(lSiteID);
                                    MapEntity homebase = aircraft.GetHomeBase().GetMapEntity(this);

                                    if((homebase instanceof Airbase && ((Airbase)homebase).GetOnline()) || homebase instanceof Ship)
                                    {
                                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("airbase identified..."));
                                        
                                        if(type.GetAirLaunched() && type.GetPurchasable())
                                        {
                                            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("launchable type is valid..."));
                                            
                                            if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                            {
                                                system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Launchable added."));
                                                EntityUpdated(homebase, false);
                                                EntityUpdated(aircraft, false);
                                            }
                                        }
                                    }
                                }
                                else if(systemType == SystemType.MISSILE_SITE)
                                {
                                    MissileSite site = MissileSites.get(lSiteID);

                                    if(site.CanTakeICBM() && type.GetICBM() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                        {
                                            bICBMPurchased = true;
                                            CreateEvent(new LaunchEvent(String.format("%s is constructing an ICBM! Time to completion: %s", player.GetName(), LaunchUtilities.GetTimeAmount(MissileStats.GetMissileBuildTime(type, false, player.GetBoss()))), SoundEffect.EQUIP));
                                            CreateReport(new LaunchReport(String.format("%s began construction of an ICBM! Time to completion: %s", player.GetName(), LaunchUtilities.GetTimeAmount(MissileStats.GetMissileBuildTime(type, false, player.GetBoss()))), true, player.GetID()));
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                        }  
                                    }
                                    else if(!site.CanTakeICBM() && !type.GetICBM() && type.GetGroundLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(),  true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(site, false);
                                        }
                                    }
                                }
                                else if(systemType == SystemType.ARTILLERY_GUN)
                                {
                                    ArtilleryGun artillery = ArtilleryGuns.get(lSiteID);
                                    
                                    if(type.GetArtillery() && type.GetGroundLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(),  true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(artillery, false);
                                        }  
                                    }
                                }
                                else if(systemType == SystemType.SHIP_ARTILLERY)
                                {
                                    Ship ship = GetShip(lSiteID);
                                    
                                    if(type.GetArtillery() && type.GetShipLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(ship, false);
                                        }  
                                    }
                                }
                                else if(systemType == SystemType.TANK_MISSILES)
                                {
                                    Tank tank = GetTank(lSiteID);

                                    if(type.GetTankLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                            system.AddMissileToNextSlot(lSlotNo, lType, config.GetMissilePrepTime(type, player));
                                    }
                                }
                                else if(systemType == SystemType.TANK_ARTILLERY)
                                {
                                    Tank tank = GetTank(lSiteID);

                                    if(type.GetArtillery())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                            system.AddMissileToNextSlot(lSlotNo, lType, config.GetMissilePrepTime(type, player));
                                    }
                                }
                                else if(systemType == SystemType.SHIP_MISSILES)
                                {
                                    Ship ship = GetShip(lSiteID);

                                    if(type.GetShipLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(ship, false);
                                        }
                                    }
                                }
                                else if(systemType == SystemType.SUBMARINE_MISSILES)
                                {
                                    Submarine submarine = GetSubmarine(lSiteID);

                                    if(type.GetSubmarineLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                            EntityUpdated(submarine, false);
                                        }
                                    }
                                }
                                else if(systemType == SystemType.SUBMARINE_ICBM)
                                {
                                    Submarine submarine = GetSubmarine(lSiteID);

                                    if(type.GetICBM() && type.GetSubmarineLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                            EntityUpdated(submarine, false);
                                        }
                                    }
                                }
                            }
                        }

                        system.CompleteMultiPurchase();
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Purchase completed."));
                        
                        if(bNukesPurchased && !bICBMPurchased)
                        {
                            CreateEvent(new LaunchEvent(String.format("%s is constructing nuclear weapons!", player.GetName()), SoundEffect.EQUIP));
                        }
                        
                        CreateEvent(new LaunchEvent(String.format("%s purchased missiles.", player.GetName()), SoundEffect.EQUIP));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean PurchaseInterceptors(int lPlayerID, int lSiteID, int lSlotNo, List<Integer> lLaunchableTypes, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to purchase launchables...", player.GetName()));
            
            if(player.Functioning())
            {                
                switch(systemType)
                {
                    case TANK_INTERCEPTORS:
                    {
                        Tank tank = GetTank(lSiteID);
                        
                        if(tank != null)
                        {
                            if(tank.GetOwnerID() == lPlayerID)
                            {
                                system = tank.GetMissileSystem();
                            }
                        }
                    }
                    break;

                    case SAM_SITE:
                    {
                        SAMSite samSite = SAMSites.get(lSiteID);
                        
                        if(samSite != null)
                        {
                            if(samSite.GetOwnerID() == lPlayerID)
                            {
                                if(!samSite.GetIsABMSilo() || (samSite.GetIsABMSilo() && samSite.GetInterceptorSystem().GetOccupiedSlotCount() < 1))
                                {
                                    system = samSite.GetInterceptorSystem();
                                }
                            }   
                        }
                    }
                    break;

                    case STORED_AIRCRAFT_INTERCEPTORS:
                    {
                        StoredAirplane aircraft = GetStoredAirplane(lSiteID);
                        
                        if(aircraft != null && aircraft.DoneBuilding() && !aircraft.Flying())
                        {
                            if(aircraft.GetOwnerID() == lPlayerID)
                            {
                                system = aircraft.GetInterceptorSystem();
                            }
                        }
                    }
                    break;
                    
                    case SHIP_INTERCEPTORS:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetInterceptorSystem();
                            }
                        }
                    }
                    break;
                    
                    default:
                    {
                        LaunchLog.ConsoleMessage(String.format("case %s not recognized.", systemType));
                    }
                    break;
                }
                
                
                if(system != null)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("system not null...", player.GetName()));
                    if(system.GetEmptySlotCount() >= lLaunchableTypes.size())
                    {
                        for(int lType : lLaunchableTypes)
                        {
                            InterceptorType type = config.GetInterceptorType(lType);

                            if(type != null)
                            {
                                int lPrepTime = MissileStats.GetInterceptorBuildTime(type, false, player.GetBoss());
                                
                                if(systemType == SystemType.STORED_AIRCRAFT_INTERCEPTORS && type.GetAirLaunched())
                                {
                                    MapEntity homebase = GetStoredAirplane(lSiteID).GetHomeBase().GetMapEntity(this);

                                    if((homebase instanceof Airbase && ((Airbase)homebase).GetOnline()) || homebase instanceof Ship)
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), false))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(homebase, false);
                                        }
                                    }  
                                }
                                else if(systemType == SystemType.SAM_SITE)
                                {
                                    SAMSite site = SAMSites.get(lSiteID);

                                    if(site.GetIsABMSilo())
                                    {
                                        if(type.GetABM() && type.GetPurchasable())
                                        {
                                            if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), false))
                                            {
                                                system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                                EntityUpdated(site, false);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        if(!type.GetABM() && !type.GetAirLaunched())
                                        {
                                            if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), false))
                                            {
                                                system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                                EntityUpdated(site, false);
                                            }
                                        }
                                    }
                                }
                                else if(systemType == SystemType.TANK_INTERCEPTORS)
                                {
                                    if(type.GetTankLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), false))
                                            system.AddMissileToNextSlot(lSlotNo, lType, config.GetInterceptorPrepTime(type));
                                    }
                                }
                                else if(systemType == SystemType.SHIP_INTERCEPTORS)
                                {
                                    Ship ship = GetShip(lSiteID);
                                    
                                    if(type.GetShipLaunched() && type.GetPurchasable())
                                    {
                                        if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), false))
                                        {
                                            system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                            EntityUpdated(ship, false);
                                        }
                                    }
                                }
                            }
                        }

                        system.CompleteMultiPurchase();
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Purchase completed."));
                        
                        CreateEvent(new LaunchEvent(String.format("%s purchased interceptors.", player.GetName()), SoundEffect.EQUIP));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean PurchaseTorpedoes(int lPlayerID, int lSiteID, int lSlotNo, List<Integer> lLaunchableTypes, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        MapEntity host = null;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to purchase launchables...", player.GetName()));
            
            if(player.Functioning())
            {                
                switch(systemType)
                {
                    case SHIP_TORPEDOES:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            host = ship;
                            
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetTorpedoSystem();
                            }
                        }
                    }
                    break;
                    
                    case SUBMARINE_TORPEDO:
                    {
                        Submarine submarine = GetSubmarine(lSiteID);
                        
                        if(submarine != null)
                        {
                            host = submarine;
                            
                            if(submarine.GetOwnerID() == lPlayerID)
                            {
                                system = submarine.GetTorpedoSystem();
                            }
                        }
                    }
                    break;
                    
                    default:
                    {
                        LaunchLog.ConsoleMessage(String.format("case %s not recognized.", systemType));
                    }
                    break;
                }
                
                
                if(system != null)
                {
                    boolean bNukesPurchased = false;
                    
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("system not null...", player.GetName()));
                    if(system.GetEmptySlotCount() >= lLaunchableTypes.size())
                    {
                        for(int lType : lLaunchableTypes)
                        {
                            TorpedoType type = config.GetTorpedoType(lType);

                            if(type != null)
                            {
                                int lPrepTime = MissileStats.GetTorpedoBuildTime(type, false, player.GetBoss());
                                
                                if(type.GetNuclear())
                                {
                                    bNukesPurchased = true;
                                }                                    

                                if(type.GetNavalLaunched() && type.GetPurchasable())
                                {
                                    if(ProcessPlayerWeaponPurchase(lPlayerID, type.GetCost(), true))
                                    {
                                        system.AddMissileToNextSlot(lSlotNo, lType, lPrepTime);
                                        
                                        if(host != null)
                                        {
                                            EntityUpdated(host, false);
                                            
                                            if(host instanceof Submarine)
                                                host.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                        }
                                    }
                                }
                            }
                        }

                        system.CompleteMultiPurchase();
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Purchase completed."));
                        
                        if(bNukesPurchased)
                        {
                            CreateEvent(new LaunchEvent(String.format("%s is constructing nuclear weapons!", player.GetName()), SoundEffect.EQUIP));
                        }    
                        
                        CreateEvent(new LaunchEvent(String.format("%s purchased torpedoes.", player.GetName()), SoundEffect.EQUIP));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean UpgradeShipyard(int lPlayerID, int lShipyardID)
    {
        Player player = GetPlayer(lPlayerID);
        Shipyard shipyard = GetShipyard(lShipyardID);
        
        if(player != null && shipyard != null && player.Functioning())
        {
            if(shipyard.GetOwnedBy(lPlayerID))
            {
                if(player.SubtractWealth(Defs.SHIPYARD_UPGRADE_WEALTH_COST))
                {
                    shipyard.UpgradeProductionCapacity();
                    EntityUpdated(shipyard, false);

                    ProcessPlayerXPGain(lPlayerID, Defs.SHIPYARD_UPGRADE_XP, "You upgraded a shipyard.");
                    CreateEvent(new LaunchEvent(String.format("%s upgraded %s's production capacity.", player.GetName(), shipyard.GetName()), SoundEffect.CONSTRUCTION));
                    CreateReport(new LaunchReport(String.format("%s upgraded %s's production capacity.", player.GetName(), shipyard.GetName()), false, lPlayerID));
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean SonarPing(int lPlayerID, EntityPointer pinger)
    {
        Player player = GetPlayer(lPlayerID);
        NavalVessel vessel = (NavalVessel)pinger.GetMapEntity(this);
        
        if(vessel != null && player != null)
        {
            if(vessel.GetOwnedBy(lPlayerID) && player.Functioning())
            {
                if(vessel.SonarReady())
                {
                    vessel.SonarPing();
                    vessel.SetVisible(Defs.SONAR_SCAN_VISIBILITY_TIME);
                    
                    for(Submarine submarine : GetSubmarines())
                    {
                        if(!WouldBeFriendlyFire(GetOwner(submarine), player) && submarine.GetPosition().DistanceTo(vessel.GetPosition()) <= Defs.SONAR_RANGE)
                        {
                            submarine.SetVisible(Defs.SONAR_SCAN_VISIBILITY_TIME);
                            EntityUpdated(submarine, false);
                        }
                    }
                    
                    CreateEvent(new LaunchEvent(String.format("%s's %s went sonar active!", player.GetName(), vessel.GetTypeName()), SoundEffect.SONAR));
                    return true;
                }
            }
        }
        
        return false;        
    }
    
    @Override
    public boolean ToggleAirbaseOpen(int lPlayerID, EntityPointer host)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && host != null)
        {
            MapEntity hostEntity = host.GetMapEntity(game);

            if(hostEntity instanceof Airbase airbase)
            {
                if(airbase.GetOwnedBy(lPlayerID))
                {
                    AircraftSystem system = airbase.GetAircraftSystem();
                    
                    if(system != null)
                    {
                        system.ToggleOpen();
                        EntityUpdated(airbase, false);
                        
                        if(system.GetOpen())
                        {
                            CreateEvent(new LaunchEvent(String.format("%s opened a %s.", player.GetName(), airbase.GetTypeName()), SoundEffect.EQUIP));
                        }
                        else
                        {
                            CreateEvent(new LaunchEvent(String.format("%s closed a %s.", player.GetName(), airbase.GetTypeName()), SoundEffect.EQUIP));
                        }
                        
                        return true;
                    }
                }
            }
            else if(hostEntity instanceof Ship ship)
            {
                if(ship.GetOwnedBy(lPlayerID) && ship.HasAircraft())
                {
                    AircraftSystem system = ship.GetAircraftSystem();
                    
                    if(system != null)
                    {
                        system.ToggleOpen();
                        EntityUpdated(ship, false);
                        
                        if(system.GetOpen())
                        {
                            CreateEvent(new LaunchEvent(String.format("%s opened a %s.", player.GetName(), ship.GetTypeName()), SoundEffect.EQUIP));
                        }
                        else
                        {
                            CreateEvent(new LaunchEvent(String.format("%s closed a %s.", player.GetName(), ship.GetTypeName()), SoundEffect.EQUIP));
                        }
                        
                        return true;
                    }
                }
            }
        } 
        
        return false;        
    }
    
    @Override
    public boolean RadarScan(int lPlayerID, EntityPointer pointer)
    {
        return false;   
    }
    
    @Override
    public boolean PurchaseInfantry(int lPlayerID, int lArmoryID, boolean bUseSubstitutes)
    {
        return false;
    }
    
    @Override
    public boolean PurchaseTank(int lPlayerID, int lArmoryID, EntityType tankType, boolean bUseSubstitutes)
    {
        Player player = GetPlayer(lPlayerID);
        Armory armory = GetArmory(lArmoryID);
        
        if(player != null && armory != null)
        {
            if(player.Functioning() && armory.GetOnline() && !armory.GetProducing() && !armory.GetIsBarracks() && EntityIsFriendly(armory, player))
            {
                Map<ResourceType, Long> Costs = Defs.TANK_BUILD_COST;
                
                if(bUseSubstitutes)
                    Costs = GetSubstitutionCost(Costs);
                else
                    Costs = GetRequiredCost(Costs);
                
                PurchaseType purchaseType = PurchaseType.OFFENSIVE;
                
                if(tankType == EntityType.SPAAG || tankType == EntityType.SAM_TANK)
                    purchaseType = PurchaseType.DEFENSIVE;
                
                if(ProcessPlayerPurchase(lPlayerID, Costs, armory.GetResourceSystem(), null, bUseSubstitutes, purchaseType))
                {
                    int lBuildTime = Defs.TANK_BUILD_TIME;
                    
                    if(armory.GetResourceSystem().ChargeQuantities(Defs.BOOSTER_TYPES_TANK_BUILD))
                    {
                        lBuildTime *= Defs.PRODUCTION_BONUS_MULTIPLIER;
                    }
                    
                    if(player.GetBoss())
                    {
                        lBuildTime = 0;
                    }
                        
                    ProcessPlayerXPGain(lPlayerID, Defs.TANK_PURCHASED_XP, "You built a tank.");
                    armory.SetProducing(tankType, lBuildTime); //When this timer runs out, produce a tank.
                    EntityUpdated(armory, true);
                    
                    CreateEvent(new LaunchEvent(String.format("%s purchased a tank.", player.GetName()), SoundEffect.EQUIP));
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PurchaseAircraft(int lPlayerID, EntityPointer host, EntityType aircraftType, boolean bUseSubstitutes)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning())
        {
            AircraftSystem system = null;
            Ship ship = null;
            Airbase airbase = null;
            StoredAirplane aircraft = null;
            ResourceSystem resources = null;
            
            if(host.GetMapEntity(this) instanceof Airbase entity)
            {
                airbase = entity;
                system = airbase.GetAircraftSystem();
                resources = airbase.GetResourceSystem();
            }
            else if(host.GetMapEntity(this) instanceof Ship entity)
            {
                ship = entity;
                system = ship.GetAircraftSystem();
            }

            if(system.GetEmptySlotCount() > 0)
            {
                MissileSystem missiles = null;
                CargoSystem cargo = null;
                MissileSystem interceptors = null;
                int lBuildTime = Integer.MAX_VALUE;
                Map<ResourceType, Long> costs = Defs.GetAircraftBuildCost(aircraftType);
                PurchaseType purchaseType = PurchaseType.OFFENSIVE;
                
                switch(aircraftType)
                {
                    case FIGHTER:
                    {
                        interceptors = new MissileSystem(null, Defs.AIRCRAFT_INTERCEPTOR_RELOAD_TIME, Defs.FIGHTER_INTERCEPTOR_SLOTS);
                        lBuildTime = Defs.FIGHTER_BUILD_TIME;
                        purchaseType = PurchaseType.DEFENSIVE;
                    }
                    break;
                    
                    case BOMBER:
                    {
                        missiles = new MissileSystem(null, Defs.AIRCRAFT_MISSILE_RELOAD_TIME, Defs.BOMBER_MISSILE_SLOTS);
                        lBuildTime = Defs.BOMBER_BUILD_TIME;
                    }
                    break;
                    
                    case STEALTH_FIGHTER:
                    {
                        interceptors = new MissileSystem(null, Defs.AIRCRAFT_INTERCEPTOR_RELOAD_TIME, Defs.FIGHTER_INTERCEPTOR_SLOTS);
                        lBuildTime = Defs.STEALTH_FIGHTER_BUILD_TIME;
                        purchaseType = PurchaseType.DEFENSIVE;
                    }
                    break;
                    
                    case STEALTH_BOMBER:
                    {
                        missiles = new MissileSystem(null, Defs.AIRCRAFT_MISSILE_RELOAD_TIME, Defs.STEALTH_BOMBER_MISSILE_SLOTS);
                        lBuildTime = Defs.STEALTH_BOMBER_BUILD_TIME;
                    }
                    break;
                    
                    case ATTACK_AIRCRAFT:
                    {
                        lBuildTime = Defs.ATTACK_AIRCRAFT_BUILD_TIME;
                    }
                    break;
                    
                    case AWACS:
                    {
                        lBuildTime = Defs.AWACS_BUILD_TIME;
                        purchaseType = PurchaseType.DEFENSIVE;
                    }
                    break;
                    
                    case REFUELER:
                    {
                        lBuildTime = Defs.REFUELER_BUILD_TIME;
                    }
                    break;
                    
                    case CARGO_PLANE:
                    {
                        cargo = new CargoSystem(null, Defs.CARGO_PLANE_CAPACITY);
                        lBuildTime = Defs.CARGO_PLANE_BUILD_TIME;
                        purchaseType = PurchaseType.ECONOMIC;
                    }
                    break;
                    
                    case MULTI_ROLE:
                    {
                        interceptors = new MissileSystem(null, Defs.AIRCRAFT_INTERCEPTOR_RELOAD_TIME, Defs.MULTI_ROLE_INTERCEPTOR_SLOTS);
                        missiles = new MissileSystem(null, Defs.AIRCRAFT_MISSILE_RELOAD_TIME, Defs.MULTI_ROLE_MISSILE_SLOTS);
                        lBuildTime = Defs.MULTI_ROLE_BUILD_TIME;
                    }
                    break;
                    
                    case SSB:
                    {
                        missiles = new MissileSystem(null, Defs.AIRCRAFT_MISSILE_RELOAD_TIME, Defs.SSB_MISSILE_SLOTS);
                        lBuildTime = Defs.SSB_BUILD_TIME;
                    }
                    break;
                }      
                
                if(player.GetBoss())
                {
                    lBuildTime = 0;
                }
                
                if(bUseSubstitutes)
                    costs = GetSubstitutionCost(costs);
                else
                    costs = GetRequiredCost(costs);
                
                if(ProcessPlayerPurchase(lPlayerID, costs, resources, null, bUseSubstitutes, purchaseType))
                {
                    if(airbase != null && airbase.GetResourceSystem().ChargeQuantities(Defs.BOOSTER_TYPES_AIRCRAFT_BUILD))
                    {
                        lBuildTime *= Defs.PRODUCTION_BONUS_MULTIPLIER;
                    }
                    
                    aircraft = new StoredAirplane(GetAtomicID(lAirplaneIndex, Airplanes), lPlayerID, (short)1, (short)1, lBuildTime, host, aircraftType, missiles, interceptors, cargo);
                    ProcessPlayerXPGain(lPlayerID, Defs.GetPurchaseXP(costs), String.format("You purchased a %s.", aircraft.GetTypeName()));

                    AddStoredAircraft(aircraft);
                    CreateEvent(new LaunchEvent(String.format("%s purchased a %s.", player.GetName(), aircraft.GetTypeName()), SoundEffect.EQUIP));
                      
                    if(airbase != null)
                    {
                        EntityUpdated(airbase, false);
                    }
                    else if(ship != null)
                    {
                        EntityUpdated(ship, false);
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PurchaseShip(int lPlayerID, int lShipyardID, EntityType shipType, boolean bUseSubstitutes)
    {
        Player player = GetPlayer(lPlayerID);
        Shipyard shipyard = GetShipyard(lShipyardID);
        
        if(player != null && shipyard != null)
        {
            if(player.Functioning() && !shipyard.Destroyed())
            {
                if(shipyard.GetOwnerID() == LaunchEntity.ID_NONE || shipyard.GetOwnedBy(lPlayerID) || GetAllegiance(shipyard, player) == Allegiance.ALLY)
                {
                    boolean bOffensive = true;
                    long oBuildTime = Defs.GetNavalBuildTime(shipType);
                    Map<ResourceType, Long> costs = Defs.GetNavalBuildCost(shipType);
                    
                    if(bUseSubstitutes)
                        costs = GetSubstitutionCost(costs);
                    else
                        costs = GetRequiredCost(costs);
                    
                    PurchaseType purchaseType = PurchaseType.OFFENSIVE;
                    
                    if(shipType == EntityType.CARGO_SHIP)
                    {
                        purchaseType = PurchaseType.ECONOMIC;
                    }

                    if(shipyard.HasCapacityRemaining() && !shipyard.Destroyed())
                    {
                        if(ProcessPlayerPurchase(player.GetID(), costs, null, null, bUseSubstitutes, purchaseType))
                        {                            
                            if(player.GetBoss())
                            {
                                oBuildTime = 0;
                            }

                            ProcessPlayerXPGain(lPlayerID, Defs.GetPurchaseXP(costs), "You built a ship.");
                            shipyard.AddProductionOrder(new ShipProductionOrder(lPlayerID, oBuildTime, shipType));
                            EntityUpdated(shipyard, true);

                            CreateEvent(new LaunchEvent(String.format("%s purchased a ship.", player.GetName()), SoundEffect.EQUIP));

                            return true;
                        } 
                    }
                }      
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PurchaseCargoTruck(int lPlayerID, int lWarehouseID, boolean bUseSubstitutes)
    {
        Player player = GetPlayer(lPlayerID);
        Warehouse warehouse = GetWarehouse(lWarehouseID);
        
        if(player != null && warehouse != null && !warehouse.GetProducing() && warehouse.GetOnline())
        {            
            if(EntityIsFriendly(warehouse, player))
            {
                Map<ResourceType, Long> Costs = Defs.CARGO_TRUCK_BUILD_COST;
                
                if(bUseSubstitutes)
                    Costs = GetSubstitutionCost(Costs);
                else
                    Costs = GetRequiredCost(Costs);
                
                if(player.SubtractWealth(Costs.get(ResourceType.WEALTH)))
                {
                    if(player.Functioning())
                    {
                        int lBuildTime = Defs.CARGO_TRUCK_BUILD_TIME;

                        if(player.GetBoss())
                        {
                            lBuildTime = 0;
                        }

                        ProcessPlayerXPGain(lPlayerID, Defs.CARGO_TRUCK_PURCHASED_XP, String.format("You built a cargo truck."));
                        warehouse.SetProducing(lBuildTime); //When this timer runs out, produce a truck.
                        EntityUpdated(warehouse, true);
                        
                        CreateEvent(new LaunchEvent(String.format("%s purchased a cargo truck.", player.GetName()), SoundEffect.EQUIP));

                        return true;
                    }
                } 
            }
        }
        
        return false;
    }
    
    @Override
    public boolean AirbaseCapacityUpgrade(int lPlayerID, int lAirbaseID)
    {
        Player player = Players.get(lPlayerID);
        Airbase airbase = Airbases.get(lAirbaseID);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade aircraft slots...", player.GetName()));
        
        if(airbase.GetAircraftSystem().GetSlotCount() < Defs.MAX_AIRBASE_CAPACITY)
        {
            if(player.SubtractWealth(GetAircraftSlotUpgradeCost(airbase.GetAircraftSystem().GetSlotCount(), config.GetAirbaseBaseCapacity())))
            {
                airbase.GetAircraftSystem().IncreaseSlotCount(Defs.AIRBASE_CAPACITY_UPGRADE_AMOUNT);
                airbase.SetMaxHP((short)(airbase.GetMaxHP() + Defs.AIRBASE_HP_PER_SLOT_UPGRADE));
                airbase.AddHP(Defs.AIRBASE_HP_PER_SLOT_UPGRADE);
                EntityUpdated(airbase, false);
                CreateEvent(new LaunchEvent(String.format("%s upgraded %s capacity.", player.GetName(), airbase.GetTypeName()), SoundEffect.EQUIP));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean MissileSlotUpgrade(int lPlayerID, int lMissileSiteID)
    {
        Player player = Players.get(lPlayerID);
        MissileSite missileSite = MissileSites.get(lMissileSiteID);
        MissileSystem missileSystem = missileSite.GetMissileSystem();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade missile slots...", player.GetName()));
        
        if(!missileSite.CanTakeICBM() && player.Functioning() && (missileSystem.GetSlotCount() + (int)config.GetMissileUpgradeCount()) <= Defs.MAX_MISSILE_SLOTS)
        {
            if(player.SubtractWealth(GetMissileSlotUpgradeCost(missileSystem, config.GetInitialMissileSlots())))
            {
                missileSystem.IncreaseSlotCount(config.GetMissileUpgradeCount());
                missileSite.SetMaxHP((short)(missileSite.GetMaxHP() + (config.GetMissileUpgradeCount() * StructureStats.HP_PER_MISSILESLOT)));
                missileSite.AddHP((short)(config.GetMissileUpgradeCount() * StructureStats.HP_PER_MISSILESLOT));
                CreateEvent(new LaunchEvent(String.format("%s upgraded missile site slots.", player.GetName()), SoundEffect.EQUIP));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean InterceptorSlotUpgrade(int lPlayerID, int lSAMSiteID)
    {
        Player player = Players.get(lPlayerID);
        SAMSite samSite = SAMSites.get(lSAMSiteID);
        MissileSystem missileSystem = samSite.GetInterceptorSystem();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade interceptor slots...", player.GetName()));
        
        if(!samSite.GetIsABMSilo() && player.Functioning() && ((int)missileSystem.GetSlotCount() + (int)config.GetMissileUpgradeCount()) <= Defs.MAX_MISSILE_SLOTS)
        {
            if(player.SubtractWealth(GetMissileSlotUpgradeCost(missileSystem, config.GetInitialInterceptorSlots())))
            {
                missileSystem.IncreaseSlotCount(config.GetMissileUpgradeCount());
                samSite.SetMaxHP((short)(samSite.GetMaxHP() + (config.GetMissileUpgradeCount() * StructureStats.HP_PER_MISSILESLOT)));
                samSite.AddHP((short)(config.GetMissileUpgradeCount() * StructureStats.HP_PER_MISSILESLOT));
                CreateEvent(new LaunchEvent(String.format("%s upgraded SAM site slots.", player.GetName()), SoundEffect.EQUIP));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean MissileSlotUpgradeToMax(int lPlayerID, int lMissileSiteID)
    {
        Player player = Players.get(lPlayerID);
        MissileSite missileSite = MissileSites.get(lMissileSiteID);
        MissileSystem missileSystem = missileSite.GetMissileSystem();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade missile slots...", player.GetName()));
        
        if(!missileSite.CanTakeICBM() && player.Functioning() && (missileSystem.GetSlotCount() + (int)config.GetMissileUpgradeCount()) <= Defs.MAX_MISSILE_SLOTS)
        {
            if(player.SubtractWealth(GetMissileSlotUpgradeCostToMax(missileSystem, config.GetInitialMissileSlots())))
            {
                missileSite.SetMaxHP((short)(missileSite.GetMaxHP() + ((Defs.MAX_MISSILE_SLOTS - missileSystem.GetSlotCount()) * StructureStats.HP_PER_MISSILESLOT)));
                missileSite.AddHP((short)((Defs.MAX_MISSILE_SLOTS - 1 - missileSystem.GetSlotCount()) * StructureStats.HP_PER_MISSILESLOT));
                missileSystem.SetSlotCount(Defs.MAX_MISSILE_SLOTS - 1);
                CreateEvent(new LaunchEvent(String.format("%s maxxed %s slot count.", player.GetName(), missileSite.GetTypeName()), SoundEffect.EQUIP));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean InterceptorSlotUpgradeToMax(int lPlayerID, int lSAMSiteID)
    {
        Player player = Players.get(lPlayerID);
        SAMSite samSite = SAMSites.get(lSAMSiteID);
        MissileSystem missileSystem = samSite.GetInterceptorSystem();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade interceptor slots...", player.GetName()));
        
        if(!samSite.GetIsABMSilo() && player.Functioning() && ((int)missileSystem.GetSlotCount() + (int)config.GetMissileUpgradeCount()) <= Defs.MAX_MISSILE_SLOTS)
        {
            if(player.SubtractWealth(GetMissileSlotUpgradeCostToMax(missileSystem, config.GetInitialInterceptorSlots())))
            {
                samSite.SetMaxHP((short)(samSite.GetMaxHP() + ((Defs.MAX_MISSILE_SLOTS - missileSystem.GetSlotCount()) * StructureStats.HP_PER_MISSILESLOT)));
                samSite.AddHP((short)((Defs.MAX_MISSILE_SLOTS - 1 - missileSystem.GetSlotCount()) * StructureStats.HP_PER_MISSILESLOT));
                missileSystem.SetSlotCount(Defs.MAX_MISSILE_SLOTS - 1);
                CreateEvent(new LaunchEvent(String.format("%s maxed %s slot count.", player.GetName(), samSite.GetTypeName()), SoundEffect.EQUIP));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean MissileReloadUpgrade(int lPlayerID, int lMissileSiteID)
    {
        Player player = Players.get(lPlayerID);
        MissileSite missileSite = MissileSites.get(lMissileSiteID);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade missile reload time...", player.GetName()));
        
        if(!missileSite.CanTakeICBM() && missileSite.GetOwnerID() == lPlayerID && player.Functioning())
        {
            MissileSystem system = missileSite.GetMissileSystem();
            int lCost = GetReloadUpgradeCost(system);
            
            if(lCost != Defs.UPGRADE_COST_MAXED)
            {
                if(player.GetWealth() >= lCost)
                {
                    player.SubtractWealth(lCost);
                    system.SetReloadTime(GetReloadUpgradeTime(system));
                    CreateEvent(new LaunchEvent(String.format("%s upgraded missile site reload time.", player.GetName()), SoundEffect.EQUIP));
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean InterceptorReloadUpgrade(int lPlayerID, int lSAMSiteID)
    {
        Player player = Players.get(lPlayerID);
        SAMSite samSite = SAMSites.get(lSAMSiteID);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade intercetpor reload time...", player.GetName()));
        
        if(samSite.GetOwnerID() == lPlayerID && player.Functioning())
        {
            MissileSystem system = samSite.GetInterceptorSystem();
            int lCost = GetReloadUpgradeCost(system);
            
            if(lCost != Defs.UPGRADE_COST_MAXED)
            {
                if(player.GetWealth() >= lCost)
                {
                    player.SubtractWealth(lCost);
                    system.SetReloadTime(GetReloadUpgradeTime(system));
                    CreateEvent(new LaunchEvent(String.format("%s upgraded SAM site reload time.", player.GetName()), SoundEffect.EQUIP));
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean SentryRangeUpgrade(int lPlayerID, int lSentryGunID)
    {
        /*Player player = Players.get(lPlayerID);
        SentryGun sentryGun = SentryGuns.get(lSentryGunID);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade sentry gun range...", player.GetName()));
        
        if(sentryGun.GetOwnerID() == lPlayerID && player.Functioning())
        {
            int lCost = GetSentryRangeUpgradeCost(sentryGun);
            
            if(sentryGun.GetRange() < config.GetMaxSentryRange())
            {
                if(player.GetWealth() >= lCost)
                {
                    player.SubtractWealth(lCost);
                    sentryGun.SetRange(GetSentryRangeUpgradeRange(sentryGun));
                    //CreateEvent(new LaunchEvent(String.format("%s upgraded sentry gun range.", player.GetName()), SoundEffect.EQUIP));
                    return true;
                }
            }
        }*/
        
        return false;
    }
    
    @Override
    public boolean CommandPostHPUpgrade(int lPlayerID, int lCommandPostID)
    {
        Player player = Players.get(lPlayerID);
        CommandPost commandPost = CommandPosts.get(lCommandPostID);
        
        short nHPDeficit = commandPost.GetHPDeficit();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to upgrade commandPost hp...", player.GetName()));
        
        if(commandPost.GetOwnerID() == lPlayerID && player.Functioning())
        {
            int lCost = GetCommandPostHPUpgradeCost(commandPost);
            
            if(commandPost.GetMaxHP() < config.GetCommandPostMaxHPUpgrade())
            {
                if(player.GetWealth() >= lCost)
                {
                    player.SubtractWealth(lCost);
                    commandPost.SetMaxHP(GetCommandPostHPUpgradeHP(commandPost));
                    commandPost.SetHP((short)(commandPost.GetMaxHP() - nHPDeficit));
                    CreateEvent(new LaunchEvent(String.format("%s upgraded commandPost HP.", player.GetName()), SoundEffect.EQUIP));
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean CaptureEntity(int lPlayerID, EntityPointer pointerStructure)
    {
        Player player = GetPlayer(lPlayerID);
        LaunchEntity entity = pointerStructure.GetEntity(this);
        
        if(entity instanceof Structure structure)
        {
            if(player != null && (player.GetBoss() || (!player.GetRespawnProtected() && !WouldBeFriendlyFire(player, GetOwner(structure)))))
            {
                Player structureOwner = GetOwner(structure);

                if(player.GetBoss() || (!GetAttackIsBullying(player, structureOwner) && player.GetPosition().DistanceTo(structure.GetPosition()) <= Defs.PLAYER_CAPTURE_RADIUS))
                {
                    player.AddOwnedEntity(structure);
                    structure.Capture(lPlayerID);
                    ProcessPlayerXPGain(lPlayerID, Defs.CAPTURED_CITY_XP_GAIN, "You captured a structure.");
                    ProcessPlayerXPLoss(structureOwner.GetID(), Defs.CAPTURED_STRUCTURE_XP_LOSS, "Another player captured your structure.");
                    CreateReport(structureOwner, new LaunchReport(String.format("[COMBAT] %s captured your %s!", player.GetName(), structure.GetTypeName()), true, structureOwner.GetID(), player.GetID()));
                    CreateEvent(new LaunchEvent(String.format("%s captured %s's %s!", player.GetName(), structureOwner.GetName(), structure.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                    CreateReport(player, new LaunchReport(String.format("[COMBAT] You captured %s's %s!", structureOwner.GetName(), structure.GetTypeName()), true, player.GetID(), structureOwner.GetID()));

                    if(player.GetRespawnProtected())
                    {
                        RemoveRespawnProtection(player, true); //For some reason the above check for respawn protection isn't working, so I put this in as a band-aid. -Corbin 11/23/2024
                    }

                    structureOwner.AddHostilePlayer(lPlayerID);

                    if(structure instanceof Airbase airbase)
                    {
                        for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                        {
                            aircraft.Capture(lPlayerID);
                            player.AddOwnedEntity(aircraft);
                        }
                    }
                    else if(structure instanceof Armory armory)
                    {

                        for(StoredTank tank : armory.GetCargoSystem().GetTanks())
                        {
                            tank.Capture(lPlayerID);
                            player.AddOwnedEntity(tank);
                        }

                        for(StoredCargoTruck truck : armory.GetCargoSystem().GetCargoTrucks())
                        {
                            truck.Capture(lPlayerID);
                            player.AddOwnedEntity(truck);
                        }

                        for(StoredInfantry storedInfantry : armory.GetCargoSystem().GetInfantries())
                        {
                            storedInfantry.Capture(lPlayerID);
                            player.AddOwnedEntity(storedInfantry);
                        }
                    }

                    EntityUpdated(structure, false);

                    return true;
                }
            }
        }
        if(entity instanceof Shipyard shipyard)
        {
            if(player != null && (player.GetBoss() || (!player.GetRespawnProtected() && !WouldBeFriendlyFire(player, GetOwner(shipyard)))))
            {
                Player shipyardOwner = GetOwner(shipyard);

                if(player.GetBoss() || (!GetAttackIsBullying(player, GetOwner(shipyard)) && player.GetPosition().DistanceTo(shipyard.GetPosition()) <= Defs.PLAYER_CAPTURE_RADIUS))
                {
                    player.AddOwnedEntity(shipyard);
                    shipyard.Capture(lPlayerID);
                    ProcessPlayerXPGain(lPlayerID, Defs.CAPTURED_CITY_XP_GAIN, "You captured a shipyard.");

                    if(player.GetRespawnProtected())
                    {
                        RemoveRespawnProtection(player, true); //For some reason the above check for respawn protection isn't working, so I put this in as a band-aid. -Corbin 11/23/2024
                    }

                    if(shipyardOwner != null)
                    {
                        ProcessPlayerXPLoss(shipyardOwner.GetID(), Defs.CAPTURED_STRUCTURE_XP_LOSS, "Another player captured your shipyard.");
                        CreateReport(shipyardOwner, new LaunchReport(String.format("[COMBAT] %s captured your %s!", player.GetName(), shipyardOwner.GetTypeName()), true, shipyardOwner.GetID(), player.GetID()));
                        CreateEvent(new LaunchEvent(String.format("%s captured %s's %s!", player.GetName(), shipyardOwner.GetName(), shipyardOwner.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                        CreateReport(player, new LaunchReport(String.format("[COMBAT] You captured %s's %s!", shipyardOwner.GetName(), shipyardOwner.GetTypeName()), true, player.GetID(), shipyardOwner.GetID()));
                        shipyardOwner.AddHostilePlayer(lPlayerID);
                    } 
                    else
                    {
                        CreateEvent(new LaunchEvent(String.format("%s captured a %s", player.GetName(), shipyard.GetTypeName()), SoundEffect.INFANTRY_CAPTURE));
                        CreateReport(player, new LaunchReport(String.format("You captured a %s!", shipyard.GetTypeName()), true, player.GetID()));
                    }

                    EntityUpdated(shipyard, false);

                    return true;
                }
            }
        }
        else if(entity instanceof StoredAirplane airplane)
        {
            if(player != null && !player.GetRespawnProtected() && player.Functioning())
            {
                Player owner = GetOwner(airplane);
                
                //We should own the home base of this plane if we can capture it.
                EntityPointer pointerHost = airplane.GetHomeBase();
                
                if(pointerHost != null)
                {
                    MapEntity mapHost = pointerHost.GetMapEntity(this);
                    
                    if(mapHost != null)
                    {
                        if(lPlayerID == mapHost.GetOwnerID())
                        {
                            airplane.Capture(lPlayerID);
                            CreateEvent(new LaunchEvent(String.format("%s captured a %s from %s!", player.GetName(), airplane.GetTypeName(), owner.GetName()), SoundEffect.INFANTRY_CAPTURE));
                            CreateReport(owner, new LaunchReport(String.format("%s captured a %s!", player.GetName(), airplane.GetTypeName()), true, owner.GetID(), player.GetID()));
                            CreateReport(player, new LaunchReport(String.format("You captured a %s from %s!", airplane.GetTypeName(), owner.GetName()), true, player.GetID(), owner.GetID()));
                            CreateReport(new LaunchReport(String.format("%s captured a %s from %s!", player.GetName(), airplane.GetTypeName(), owner.GetName()), true, player.GetID(), owner.GetID()));

                            EntityUpdated(airplane, false);

                            return true;
                        }
                    }
                }           
            }
        }
        else if(entity instanceof NavalVessel vessel)
        {
            if(player != null && player.GetBoss())
            {
                vessel.Capture(lPlayerID);
                EntityUpdated(vessel, false);
            }
        }
        
        return false;
    }
    
    @Override
    public boolean AdminDelete(int lPlayerID, EntityPointer pointer)
    {
        LaunchEntity entity = (LaunchEntity)pointer.GetMapEntity(this);
        Player admin = GetPlayer(lPlayerID);
        
        if(admin != null && entity != null && admin.GetIsAnAdmin())
        {
            Player entityOwner = GetOwner(entity);
            
            if(entityOwner != null)
            {
                CreateReport(entityOwner, new LaunchReport(String.format("%s [ADMIN] deleted your %s!", admin.GetName(), entity.GetTypeName()), true, entityOwner.GetID(), admin.GetID()));
                CreateEventForPlayer(new LaunchEvent(String.format("You deleted %s's %s.", entityOwner.GetName(), entity.GetTypeName()), SoundEffect.RESPAWN), lPlayerID);
            }
            
            if(entity instanceof Damagable damagable)
            {
                damagable.InflictDamage(damagable.GetMaxHP());
                CreateEvent(new LaunchEvent(String.format("%s deleted a %s.", admin.GetName(), entity.GetTypeName()), SoundEffect.RESPAWN));
            }

            return true;
        }
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Mostly cargo related stuff.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    @Override
    public boolean LoadLandUnit(int lPlayerID, EntityPointer unit, EntityPointer transport)
    {
        switch(unit.GetType())
        {
            case INFANTRY: return LoadInfantry(lPlayerID, unit.GetID(), transport);
            case TANK: return LoadTank(lPlayerID, unit.GetID(), transport);
            case CARGO_TRUCK: return LoadCargoTruck(lPlayerID, unit.GetID(), transport);
        }
        
        return false;
    }
    
    @Override
    public boolean DropLandUnit(int lPlayerID, EntityPointer unit)
    {
        switch(unit.GetType())
        {
            case STORED_INFANTRY: return DropInfantry(lPlayerID, unit.GetID());
            case STORED_TANK: return DropTank(lPlayerID, unit.GetID());
            case STORED_CARGO_TRUCK: return DropCargoTruck(lPlayerID, unit.GetID());
        }
        
        return false;
    }
    
    @Override
    public boolean TransferLandUnit(int lPlayerID, EntityPointer sender, EntityPointer receiver, EntityPointer unit)
    {
        switch(unit.GetType())
        {
            case STORED_INFANTRY: return TransferInfantry(lPlayerID, sender, receiver, unit);
            case STORED_TANK: return TransferTank(lPlayerID, sender, receiver, unit);
            case STORED_CARGO_TRUCK: return TransferCargoTruck(lPlayerID, sender, receiver, unit);
        }
        
        return false;
    }
    
    public boolean LoadInfantry(int lPlayerID, int lInfantryID, EntityPointer pointerTransport)
    {
        Player player = GetPlayer(lPlayerID);
        Infantry infantry = GetInfantry(lInfantryID);
        LaunchEntity entityTransport = pointerTransport.GetEntity(this);
        
        if(player != null && infantry != null && entityTransport != null)
        {
            if(player.Functioning() && EntityIsFriendly(infantry, player))
            {
                if(LoadUnitBoardLegal(pointerTransport, null, infantry.GetPointer()))
                {
                    LoadInfantry(infantry, entityTransport);
                    CreateEventForPlayer(new LaunchEvent(String.format("Infantry loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean LoadCargoTruck(int lPlayerID, int lTruckID, EntityPointer pointerTransport)
    {
        Player player = GetPlayer(lPlayerID);
        CargoTruck truck = GetCargoTruck(lTruckID);
        LaunchEntity entityTransport = pointerTransport.GetEntity(this);
        
        if(player != null && truck != null && entityTransport != null)
        {
            if(player.Functioning() && EntityIsFriendly(truck, player))
            {
                if(LoadUnitBoardLegal(pointerTransport, null, truck.GetPointer()))
                {
                    LoadCargoTruck(truck, entityTransport);
                    CreateEventForPlayer(new LaunchEvent(String.format("Cargo truck loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean LoadTank(int lPlayerID, int lTankID, EntityPointer pointerTransport)
    {
        Player player = GetPlayer(lPlayerID);
        Tank tank = GetTank(lTankID);
        LaunchEntity entityTransport = pointerTransport.GetEntity(this);
        
        if(player != null && tank != null && entityTransport != null)
        {
            if(player.Functioning() && EntityIsFriendly(tank, player))
            {
                if(LoadUnitBoardLegal(pointerTransport, null, tank.GetPointer()))
                {
                    LoadTank(tank, entityTransport);
                    CreateEventForPlayer(new LaunchEvent(String.format("Tank loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean DropInfantry(int lPlayerID, int lStoredInfantryID)
    {
        StoredInfantry infantry = GetStoredInfantry(lStoredInfantryID);
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && infantry != null)
        {
            if(player.Functioning())
            {
                LaunchEntity host = infantry.GetHost().GetEntity(this);
                
                if(host != null && host.GetOwnedBy(lPlayerID) || infantry.GetOwnedBy(lPlayerID))
                {
                    return InfantryDisembark(lStoredInfantryID) != null;
                }
            }
        }
        
        return false;
    }
    
    public boolean DropCargoTruck(int lPlayerID, int lStoredCargoTruckID)
    {
        StoredCargoTruck truck = GetStoredCargoTruck(lStoredCargoTruckID);
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && truck != null)
        {
            if(player.Functioning())
            {
                LaunchEntity host = truck.GetHost().GetEntity(this);
                
                if(host != null && (host.GetOwnedBy(lPlayerID) || truck.GetOwnedBy(lPlayerID)))
                {
                    return CargoTruckDisembark(lStoredCargoTruckID) != null;
                }
            }
        }
        
        return false;
    }
    
    public boolean DropTank(int lPlayerID, int lStoredTankID)
    {
        StoredTank tank = GetStoredTank(lStoredTankID);
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && tank != null)
        {
            if(player.Functioning())
            {
                LaunchEntity host = tank.GetHost().GetEntity(this);
                
                if(host != null && host.GetOwnedBy(lPlayerID) || tank.GetOwnedBy(lPlayerID))
                {
                    return TankDisembark(lStoredTankID) != null;
                }
            }
        }
        
        return false;
    }
    
    public boolean TransferInfantry(int lPlayerID, EntityPointer pointerSender, EntityPointer pointerReceiver, EntityPointer pointerInfantry)
    {
        Player player = GetPlayer(lPlayerID);
        HaulerInterface sender = (HaulerInterface)pointerSender.GetEntity(this);
        HaulerInterface receiver = (HaulerInterface)pointerReceiver.GetEntity(this);
        StoredInfantry infantry = (StoredInfantry)pointerInfantry.GetEntity(game);
        
        if(player != null && infantry != null && sender != null && receiver != null)
        {
            if(player.Functioning() && EntityIsFriendly(infantry, player))
            {                
                if(LoadUnitBoardLegal(pointerReceiver, pointerSender, pointerInfantry))
                {
                    if(sender.GetCargoSystem().RemoveInfantry(infantry.GetID()) != null)
                    {
                        receiver.GetCargoSystem().AddInfantry(infantry);
                        infantry.SetHost(pointerReceiver);
                        EntityUpdated((LaunchEntity)sender, false);
                        EntityUpdated((LaunchEntity)receiver, false);
                        CreateEventForPlayer(new LaunchEvent(String.format("Infantry loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean TransferCargoTruck(int lPlayerID, EntityPointer pointerSender, EntityPointer pointerReceiver, EntityPointer pointerTruck)
    {
        Player player = GetPlayer(lPlayerID);
        HaulerInterface sender = (HaulerInterface)pointerSender.GetEntity(this);
        HaulerInterface receiver = (HaulerInterface)pointerReceiver.GetEntity(this);
        StoredCargoTruck truck = (StoredCargoTruck)pointerTruck.GetEntity(game);
        
        if(player != null && truck != null && sender != null && receiver != null)
        {
            if(player.Functioning() && EntityIsFriendly(truck, player))
            {
                if(LoadUnitBoardLegal(pointerReceiver, pointerSender, pointerTruck))
                {
                    if(sender.GetCargoSystem().RemoveCargoTruck(truck.GetID()) != null)
                    {
                        receiver.GetCargoSystem().AddCargoTruck(truck);
                        truck.SetHost(pointerReceiver);
                        EntityUpdated((LaunchEntity)sender, false);
                        EntityUpdated((LaunchEntity)receiver, false);
                        CreateEventForPlayer(new LaunchEvent(String.format("Cargo truck loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean TransferTank(int lPlayerID, EntityPointer pointerSender, EntityPointer pointerReceiver, EntityPointer pointerTank)
    {
        Player player = GetPlayer(lPlayerID);
        HaulerInterface sender = (HaulerInterface)pointerSender.GetEntity(this);
        HaulerInterface receiver = (HaulerInterface)pointerReceiver.GetEntity(this);
        StoredTank tank = (StoredTank)pointerTank.GetEntity(game);
        
        if(player != null && tank != null && sender != null && receiver != null)
        {
            if(player.Functioning() && EntityIsFriendly(tank, player))
            {
                if(LoadUnitBoardLegal(pointerReceiver, pointerSender, pointerTank))
                {
                    if(sender.GetCargoSystem().RemoveTank(tank.GetID()) != null)
                    {
                        receiver.GetCargoSystem().AddTank(tank);
                        tank.SetHost(pointerReceiver);
                        EntityUpdated((LaunchEntity)sender, false);
                        EntityUpdated((LaunchEntity)receiver, false);
                        CreateEventForPlayer(new LaunchEvent(String.format("Tank loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean MoveOrder(int lPlayerID, List<EntityPointer> Movables, Map<Integer, GeoCoord> Coordinates)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && Movables != null && !Movables.isEmpty())
        {
            EntityType type = EntityType.INFANTRY;
            boolean bUnitsMoved = false;
            
            for(EntityPointer movablePointer : Movables)
            {
                LaunchEntity movable = movablePointer.GetEntity(game);
                Map<Integer, GeoCoord> CopiedCoordinates = new ConcurrentHashMap<>();
                
                for(Entry<Integer, GeoCoord> coordinate : Coordinates.entrySet())
                {
                    CopiedCoordinates.put(coordinate.getKey(), coordinate.getValue().GetCopy());
                }

                if(movable != null)
                {
                    if(movable.GetOwnedBy(lPlayerID) && player.Functioning())
                    {
                        if(movable instanceof CargoTruck)
                        {
                            ((Movable)movable).MoveToPositions(CopiedCoordinates);
                            bUnitsMoved = true;
                            type = EntityType.CARGO_TRUCK;
                            String strType = "Cargo truck";
                            SoundEffect sound = SoundEffect.TRUCK_MOVE;
                            
                            if(Movables.size() == 1)
                                CreateEventForPlayer(new LaunchEvent(String.format("%s moving to position.", strType), sound), lPlayerID);
                        }
                        else if(movable instanceof Airplane)
                        {
                            ((Movable)movable).MoveToPositions(CopiedCoordinates);
                            bUnitsMoved = true;
                            type = EntityType.AIRPLANE;
                            
                            if(Movables.size() == 1)
                                CreateEventForPlayer(new LaunchEvent(String.format("Aircraft moving to position."), SoundEffect.AIRCRAFT_MOVE), lPlayerID);
                        }
                        else if(movable instanceof StoredAirplane)
                        {
                            StoredAirplane aircraft = ((StoredAirplane)movable);

                            if(aircraft.DoneBuilding())
                            {
                                boolean bCanTakeOff = false;
                                MapEntity hostEntity = aircraft.GetHomeBase().GetMapEntity(this);
                                AircraftSystem system = null;

                                if(hostEntity instanceof Airbase)
                                {
                                    system = ((Airbase)hostEntity).GetAircraftSystem();
                                    bCanTakeOff = ((Airbase)hostEntity).GetOnline() && system.ReadyForTakeoff();
                                }
                                else if(hostEntity instanceof Ship)
                                {
                                    system = ((Ship)hostEntity).GetAircraftSystem();
                                    bCanTakeOff = system.ReadyForTakeoff();
                                }

                                if(bCanTakeOff)
                                {
                                    Airplane flyingAircraft = AircraftTakeoff(aircraft.GetID(), null, CopiedCoordinates);
                                    EntityUpdated(aircraft, false);

                                    if(flyingAircraft != null)
                                    {
                                        flyingAircraft.MoveToPositions(Coordinates);
                                        EntityUpdated(flyingAircraft, false);
                                        bUnitsMoved = true;
                                        type = EntityType.AIRPLANE;
                                        
                                        if(Movables.size() == 1)
                                            CreateEventForPlayer(new LaunchEvent(String.format("Aircraft moving to position."), SoundEffect.AIRCRAFT_MOVE), lPlayerID);
                                    }
                                }   
                            }
                        }
                        else if(movable instanceof Infantry)
                        {
                            ((Movable)movable).MoveToPositions(CopiedCoordinates);
                            bUnitsMoved = true;
                            
                            if(Movables.size() == 1)
                                CreateEventForPlayer(new LaunchEvent(String.format("Infantry moving to position."), SoundEffect.INFANTRY_MOVE), lPlayerID);
                        }
                        else if(movable instanceof Tank)
                        {
                            ((Movable)movable).MoveToPositions(CopiedCoordinates);
                            bUnitsMoved = true;
                            type = EntityType.TANK;
                            
                            if(Movables.size() == 1)
                                CreateEventForPlayer(new LaunchEvent(String.format("Tank moving to position."), SoundEffect.TANK_MOVE), lPlayerID);
                        }
                        else if(movable instanceof NavalVessel)
                        {
                            ((Movable)movable).MoveToPositions(CopiedCoordinates);
                            bUnitsMoved = true;
                            type = EntityType.SHIP;
                            
                            if(Movables.size() == 1)
                                CreateEventForPlayer(new LaunchEvent(String.format("Vessel moving to position."), SoundEffect.NAVAL_MOVE), lPlayerID);
                        }
                        
                        EntityUpdated(movable, false);
                    }
                }
            }
            
            if(bUnitsMoved && Movables.size() > 1)
            {
                switch(type)
                {
                    case SHIP: CreateEventForPlayer(new LaunchEvent(String.format("Vessels moving to position."), SoundEffect.NAVAL_MOVE), lPlayerID); return true;
                    case INFANTRY: CreateEventForPlayer(new LaunchEvent(String.format("Units moving to position."), SoundEffect.INFANTRY_MOVE), lPlayerID); return true;
                    case AIRPLANE: CreateEventForPlayer(new LaunchEvent(String.format("Aircraft moving to position."), SoundEffect.AIRCRAFT_MOVE), lPlayerID); return true;
                    case TANK: CreateEventForPlayer(new LaunchEvent(String.format("Tanks moving to position."), SoundEffect.TANK_MOVE), lPlayerID); return true;
                    case CARGO_TRUCK: CreateEventForPlayer(new LaunchEvent(String.format("Trucks moving to position."), SoundEffect.TRUCK_MOVE), lPlayerID); return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean MultiUnitCommand(int lPlayerID, MoveOrders command, List<EntityPointer> commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        /*boolean bCommandSucceeded = true;
        
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            for(EntityPointer pointer : commandables)
            {
                if(pointer != null)
                {
                    MapEntity entity = pointer.GetMapEntity(game);
                    
                    if(entity.GetOwnedBy(lPlayerID))
                    {
                        if(!UnitCommand(lPlayerID, false, command, pointer, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver))
                        {
                            bCommandSucceeded = false;
                        }
                    }
                }
            }
            
            if(bCommandSucceeded)
            {
                CreateEventForPlayer(new LaunchEvent(String.format("Command acknowledged.")), lPlayerID);
            }
        }*/
        
        return false; //bCommandSucceeded;
    }
    
    /**
     * Issue a command to a unit.
     * @param lPlayerID the player issuing the command.
     * @param command the type of command being issued.
     * @param commandable pointer to the unit that is being commanded.
     * @param target pointer to the unit that is being targeted, if there is one.
     * @param geoTarget pointer to the coordinate the entity is being directed to, if applicable.
     * @param typeToDeliver if this is a command for a delivery, this is the type of loot it is delivering.
     * @param lDeliverID the type ID of the cargo to deliver. (If delivering resources, this will be the resource type ordinal. If launchables, it will be the ID of the launchable.)
     * @param lQuantityToDeliver the amount to deliver. 
     * @return true if the command was successfully implemented.
     */
    @Override
    public boolean UnitCommand(int lPlayerID, boolean bAnnounce, MoveOrders command, List<EntityPointer> Commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        Player player = GetPlayer(lPlayerID);
        boolean bTargetSet = false;
        
        /*if(geoTarget != null)
        {
            geoTarget.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * Defs.RANDOM_MOVEMENT_VARIATION);
        }*/
        
        if(player != null)
        {
            SoundEffect soundEffect = SoundEffect.NONE;
            
            if(target != null)
            {
                MapEntity entityTarget = target.GetMapEntity(this);
                
                if(GetAttackIsBullying(GetOwner(player), GetOwner(entityTarget)))
                {
                    return false;
                }
            }
            
            for(EntityPointer commandable : Commandables)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Processing command for unit: %s %d", commandable.GetType(), commandable.GetID()));
                
                if(UnitCommandIsLegal(player, command, commandable, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver))
                {
                    LaunchEntity movable = commandable.GetEntity(this);

                    if(movable != null)
                    {
                        if(movable instanceof Airplane)
                        {
                            soundEffect = SoundEffect.AIRCRAFT_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    Airplane aircraft = GetAirplane(commandable.GetID());

                                    if(aircraft != null && aircraft.GetOwnerID() == player.GetID())
                                    {
                                        if(target != null)
                                        {
                                            MapEntity entityTarget = target.GetMapEntity(this);

                                            if(entityTarget instanceof Airbase airbase && (GetAllegiance(aircraft, entityTarget) == Allegiance.YOU || airbase.GetAircraftSystem().GetOpen()))
                                            {
                                                if(!airbase.GetAircraftSystem().Full())
                                                {
                                                    aircraft.SetHomeBase(target);
                                                    aircraft.ReturnToBase();
                                                    bTargetSet = true;
                                                } 
                                            }
                                            if(entityTarget instanceof Ship ship && ship.HasAircraft() && (GetAllegiance(aircraft, entityTarget) == Allegiance.YOU || ship.GetAircraftSystem().GetOpen()))
                                            {
                                                if(!ship.GetAircraftSystem().Full() && aircraft.GetCarrierCompliant())
                                                {
                                                    aircraft.SetHomeBase(target);
                                                    aircraft.ReturnToBase();
                                                    bTargetSet = true;
                                                } 
                                            }
                                            else
                                            {
                                                aircraft.MoveToPosition(geoTarget);
                                                EntityUpdated(aircraft, false);
                                                bTargetSet = true;
                                            }
                                        }
                                        else
                                        {
                                            aircraft.MoveToPosition(geoTarget);
                                            EntityUpdated(aircraft, false);
                                            bTargetSet = true;
                                        } 
                                    }
                                }
                                break;

                                case RETURN:
                                {
                                    Airplane aircraft = GetAirplane(commandable.GetID());

                                    if(aircraft != null && player.Functioning() && aircraft.GetOwnedBy(lPlayerID))
                                    {
                                        if(target != null)
                                        {
                                            //The target is not null. Sice the command is RETURN, the target is a new homebase such as an airbase or a ship.
                                            if(target.GetMapEntity(this) instanceof AirbaseInterface airbase)
                                            {
                                                if(AircraftCanLandAtAirbase(aircraft, (AirbaseInterface)target.GetMapEntity(this)))
                                                {
                                                    aircraft.SetHomeBase(target);
                                                }
                                            }
                                        }

                                        if(aircraft.GetHomeBase().GetMapEntity(this) != null)
                                        {
                                            MapEntity homeBase = aircraft.GetHomeBase().GetMapEntity(this);
                                            aircraft.MoveToPosition(homeBase.GetPosition());
                                            aircraft.ReturnToBase();
                                            bTargetSet = true;
                                        }
                                    }
                                }
                                break;

                                case ATTACK:
                                {
                                    if(SetTarget(lPlayerID, commandable, target, geoTarget, false))
                                    {
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case SEEK_FUEL:
                                {
                                    return RefuelAircraft(lPlayerID, target.GetID(), commandable.GetID());
                                }

                                case PROVIDE_FUEL:
                                {
                                    return RefuelAircraft(lPlayerID, commandable.GetID(), target.GetID());
                                }

                                default: LaunchLog.ConsoleMessage(String.format("Improper command %s attempted to %s.", command.toString(), commandable.GetType().toString())); break;
                            }
                        }
                        else if(movable instanceof StoredAirplane)
                        {
                            soundEffect = SoundEffect.AIRCRAFT_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    StoredAirplane aircraft = GetStoredAirplane(commandable.GetID());

                                    if(aircraft != null && aircraft.DoneBuilding() && aircraft.GetOwnerID() == player.GetID())
                                    {
                                        boolean bCanTakeOff = false;
                                        MapEntity hostEntity = aircraft.GetHomeBase().GetMapEntity(this);
                                        AircraftSystem system = null;

                                        if(hostEntity instanceof Airbase)
                                        {
                                            system = ((Airbase)hostEntity).GetAircraftSystem();
                                            bCanTakeOff = ((Airbase)hostEntity).GetOnline() && system.ReadyForTakeoff();
                                        }
                                        else if(hostEntity instanceof Ship)
                                        {
                                            system = ((Ship)hostEntity).GetAircraftSystem();
                                            bCanTakeOff = system.ReadyForTakeoff();
                                        }

                                        if(bCanTakeOff)
                                        {
                                            AircraftTakeoff(commandable.GetID(), geoTarget, null);
                                            EntityUpdated(aircraft, false);
                                            bTargetSet = true;
                                        }   
                                    }
                                }
                                break;

                                default: LaunchLog.ConsoleMessage(String.format("Improper command %s attempted to %s.", command.toString(), commandable.GetType().toString())); break;
                            }
                        }
                        else if(movable instanceof Infantry)
                        {
                            soundEffect = SoundEffect.INFANTRY_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        infantry.MoveToPosition(geoTarget);
                                        infantry.GetPosition().SetLastBearing((float)infantry.GetPosition().BearingTo(geoTarget));
                                        EntityUpdated(infantry, false);

                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case WAIT:
                                case DEFEND:
                                {
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        infantry.DefendPosition();
                                        EntityUpdated(infantry, false);

                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case ATTACK:
                                {
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        infantry.AttackTarget(target);
                                        EntityUpdated(infantry, false);

                                        Player owner = GetOwner(infantry);

                                        if(owner != null && owner.GetRespawnProtected())
                                            RemoveRespawnProtection(owner, true);

                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case CAPTURE:
                                {                                        
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        infantry.CaptureTarget(target);
                                        EntityUpdated(infantry, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case LIBERATE:
                                {                                        
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        infantry.LiberateTarget(target);
                                        EntityUpdated(infantry, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case TURN:
                                {
                                    Infantry infantry = GetInfantry(commandable.GetID());

                                    if(infantry != null && infantry.GetOwnerID() == lPlayerID)
                                    {
                                        EntityUpdated(infantry, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;
                            }
                        }
                        else if(movable instanceof Tank)
                        {
                            soundEffect = SoundEffect.TANK_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    Tank tank = GetTank(commandable.GetID());

                                    if(tank != null && tank.GetOwnerID() == lPlayerID)
                                    {
                                        tank.MoveToPosition(geoTarget);
                                        tank.GetPosition().SetLastBearing((float)tank.GetPosition().BearingTo(geoTarget));
                                        EntityUpdated(tank, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case ATTACK:
                                {
                                    Tank tank = GetTank(commandable.GetID());

                                    if(tank != null && tank.GetOwnerID() == lPlayerID)
                                    {
                                        if(tank.HasArtillery())
                                        {
                                            if(geoTarget != null)
                                            {
                                                tank.FireForEffect(new FireOrder(geoTarget, 0.0f));
                                            }
                                            else if(target != null)
                                            {
                                                MapEntity targetEntity = target.GetMapEntity(game);

                                                if(targetEntity != null)
                                                    tank.FireForEffect(new FireOrder(targetEntity.GetPosition().GetCopy(), 0.0f));
                                            }
                                        }
                                        else if(tank.IsAnMBT())
                                        {
                                            tank.AttackTarget(target);
                                        }

                                        EntityUpdated(tank, false);

                                        Player owner = GetOwner(tank);

                                        if(owner != null && owner.GetRespawnProtected())
                                            RemoveRespawnProtection(owner, false);

                                        bTargetSet = true;
                                    }
                                }
                                break;

                                /*case BOARD:
                                {
                                    Tank tank = GetTank(commandable.GetID());

                                    if(tank != null && tank.GetOwnerID() == lPlayerID)
                                    {
                                        tank.Board(target);
                                        EntityUpdated(tank, false);
                                        return true;
                                    }
                                }
                                break;*/

                                case WAIT:
                                {
                                    Tank tank = GetTank(commandable.GetID());

                                    if(tank != null && tank.GetOwnerID() == lPlayerID)
                                    {
                                        tank.DefendPosition();
                                        EntityUpdated(tank, false);
                                        bTargetSet = true;
                                    }
                                    else
                                    {
                                        LaunchLog.ConsoleMessage("Tank is null, cannot wait.");
                                    }
                                }
                                break;
                            }
                        }
                        else if(movable instanceof CargoTruck)
                        {
                            soundEffect = SoundEffect.TRUCK_MOVE;

                            CargoTruck truck = GetCargoTruck(commandable.GetID());

                            switch(command)
                            {
                                case MOVE:
                                {
                                    if(truck != null && truck.GetOwnerID() == lPlayerID)
                                    {
                                        truck.MoveToPosition(geoTarget);
                                        truck.GetPosition().SetLastBearing((float)truck.GetPosition().BearingTo(geoTarget));
                                        EntityUpdated(truck, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                /*case BOARD:
                                {
                                    CargoTruck truck = GetCargoTruck(commandable.GetID());

                                    if(truck != null && truck.GetOwnerID() == lPlayerID)
                                    {
                                        truck.Board(target);
                                        EntityUpdated(truck, false);
                                        return true;
                                    }
                                }
                                break;*/

                                case WAIT:
                                {
                                    if(truck != null && truck.GetOwnerID() == lPlayerID)
                                    {
                                        truck.DefendPosition();
                                        EntityUpdated(truck, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;
                            }
                        }
                        else if(movable instanceof Ship)
                        {
                            soundEffect = SoundEffect.NAVAL_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    Ship ship = GetShip(commandable.GetID());

                                    if(ship != null && ship.GetOwnerID() == lPlayerID)
                                    {
                                        ship.MoveToPosition(geoTarget);
                                        ship.GetPosition().SetLastBearing((float)ship.GetPosition().BearingTo(geoTarget));
                                        EntityUpdated(ship, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case SEEK_FUEL:
                                {
                                    return RefuelNavalVessel(lPlayerID, target, commandable);
                                }

                                case PROVIDE_FUEL:
                                {
                                    return RefuelNavalVessel(lPlayerID, commandable, target);
                                }

                                case ATTACK:
                                {
                                    if(SetTarget(lPlayerID, commandable, target, geoTarget, false))
                                    {
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case WAIT:
                                {
                                    Ship ship = GetShip(commandable.GetID());

                                    if(ship != null && ship.GetOwnerID() == lPlayerID)
                                    {
                                        ship.Wait();
                                        EntityUpdated(ship, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;
                            }
                        }
                        else if(movable instanceof Submarine)
                        {
                            soundEffect = SoundEffect.NAVAL_MOVE;

                            switch(command)
                            {
                                case MOVE:
                                {
                                    Submarine submarine = GetSubmarine(commandable.GetID());

                                    if(submarine != null && submarine.GetOwnerID() == lPlayerID)
                                    {
                                        submarine.MoveToPosition(geoTarget);
                                        submarine.GetPosition().SetLastBearing((float)submarine.GetPosition().BearingTo(geoTarget));
                                        EntityUpdated(submarine, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case SEEK_FUEL:
                                {
                                    return RefuelNavalVessel(lPlayerID, target, commandable);
                                }

                                case PROVIDE_FUEL:
                                {
                                    return RefuelNavalVessel(lPlayerID, commandable, target);
                                }

                                case ATTACK:
                                {
                                    if(SetTarget(lPlayerID, commandable, target, geoTarget, false))
                                    {
                                        bTargetSet = true;
                                    }
                                }
                                break;

                                case WAIT:
                                {
                                    Submarine submarine = GetSubmarine(commandable.GetID());

                                    if(submarine != null && submarine.GetOwnerID() == lPlayerID)
                                    {
                                        submarine.Wait();
                                        EntityUpdated(submarine, false);
                                        bTargetSet = true;
                                    }
                                }
                                break;
                            }
                        }
                        else if(movable instanceof ArtilleryGun)
                        {
                            switch(command)
                            {
                                case ATTACK:
                                {
                                    if(SetTarget(lPlayerID, commandable, target, geoTarget, false))
                                    {
                                        bTargetSet = true;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            
            if(bTargetSet)
            {
                switch(command)
                {
                    case MOVE:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Moving to position."), soundEffect), lPlayerID);
                    }
                    break;
                    
                    case WAIT:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Holding position."), soundEffect), lPlayerID);
                    }
                    break;
                    
                    case ATTACK:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Attacking target."), soundEffect), lPlayerID);
                    }
                    break;
                    
                    case CAPTURE:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Capturing target."), soundEffect), lPlayerID);
                    }
                    break;
                    
                    case LIBERATE:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Liberating target."), soundEffect), lPlayerID);
                    }
                    break;
                    
                    case RETURN:
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Returning to base."), soundEffect), lPlayerID);
                    }
                    break;
                }
            }
        }
        
        return bTargetSet;
    }
    
    /**
     * 
     * @param lPlayerID the player issuing the command.
     * @param pointerFrom the entity delivering or dropping the cargo.
     * @param pointerTo if not null, this is the entity receiving the cargo.
     * @param typeToMove 
     * @param lTypeID
     * @param lQuantityToMove
     * @param bLoadAsCargo for transfers where the option is available, load the loot as cargo as opposed to loading launchables into a missilesystem or loading resources into a resourcecontainer.
     * @return 
     */
    @Override
    public boolean TransferCargo(int lPlayerID, EntityPointer pointerFrom, EntityPointer pointerTo, LootType typeToMove, int lTypeID, int lQuantityToMove, boolean bLoadAsCargo, boolean bFromCargo)
    {
        Player player = GetPlayer(lPlayerID);
        LaunchEntity entityFrom = pointerFrom.GetEntity(this);
        LaunchEntity entityTo = null;
        
        if(pointerTo != null)
            entityTo = pointerTo.GetEntity(this);
        
        GeoCoord geoPosition = null;
        float fltUnloadDistance = Defs.UNLOAD_DISTANCE;
        
        if(entityFrom != null && player != null && player.Functioning())
        {
            if(typeToMove != null)
            {
                //typeToMove is not null, so we are either dropping loot or making a delivery.
                //In either case, geoPosition is the location of entityFrom.

                if(entityFrom instanceof MapEntity)
                {
                    geoPosition = ((MapEntity)entityFrom).GetPosition().GetCopy();
                    
                    if(entityFrom instanceof Shipyard)
                    {
                        fltUnloadDistance = Defs.SHIPYARD_LOAD_DISTANCE;
                    }
                }
                else if(entityFrom instanceof StoredAirplane)
                {
                    MapEntity hostEntity = ((StoredAirplane)entityFrom).GetHomeBase().GetMapEntity(this);
                                    
                    if(hostEntity != null)
                    {
                        geoPosition = hostEntity.GetPosition().GetCopy();
                    }
                }

                if(entityTo != null && geoPosition != null && entityFrom.GetOwnedBy(player.GetID()))
                {
                    //entityTo is not null, so we are delivering to an entity.
                    //We need to get the geocoord of entityFrom and entityTo and make sure they are within range of eachother.
                    if(entityFrom instanceof HaulerInterface)
                    {
                        HaulerInterface hauler = ((HaulerInterface)entityFrom);
                        GeoCoord geoTo = null;
                        Player receiver = GetOwner(entityTo);

                        if(entityTo instanceof MapEntity)
                        {
                            geoTo = ((MapEntity)entityTo).GetPosition().GetCopy();

                            if(entityTo instanceof Shipyard)
                            {
                                fltUnloadDistance = Defs.SHIPYARD_LOAD_DISTANCE;
                            }
                        }
                        else if(entityTo instanceof StoredAirplane)
                        {
                            MapEntity hostEntity = ((StoredAirplane)entityTo).GetHomeBase().GetMapEntity(this);

                            if(hostEntity != null)
                            {
                                geoTo = hostEntity.GetPosition().GetCopy();
                            }
                        }

                        if(geoTo != null && geoTo.DistanceTo(geoPosition) <= fltUnloadDistance)
                        {
                            switch(typeToMove)
                            {
                                case RESOURCES:
                                {
                                    if(CargoTransferLegal(entityFrom, entityTo, typeToMove)) //If resource is null, then the truck did not contain this type of resource.
                                    {
                                        Resource resource = ((HaulerInterface)entityFrom).GetCargoSystem().RemoveResource(ResourceType.values()[lTypeID], lQuantityToMove);
                                        
                                        //Scrapyards cannot scrap electricity.
                                        if(entityTo instanceof ScrapYard && resource.GetResourceType() == ResourceType.WEALTH)
                                            return false;
                                        
                                        if(resource != null)
                                        {
                                            if(bLoadAsCargo && resource.GetQuantity() > 0 && entityTo instanceof HaulerInterface)
                                            {
                                                CargoSystem receiverSystem = ((HaulerInterface)entityTo).GetCargoSystem();
                                                
                                                if(receiverSystem != null)
                                                {
                                                    resource.SetQuantity(receiverSystem.AddResource(resource.GetResourceType(), resource.GetQuantity()));
                                                }
                                            }
                                            else if(entityTo instanceof ResourceInterface resourceTo && resourceTo.GetResourceSystem().CanHoldType(resource.GetResourceType()))
                                            {
                                                ResourceSystem receiverSystem = resourceTo.GetResourceSystem();
                                                
                                                if(receiverSystem != null)
                                                {
                                                    resource.SetQuantity(receiverSystem.AddQuantity(resource.GetResourceType(), resource.GetQuantity()));
                                                }
                                            }

                                            if(resource.GetQuantity() > 0)
                                            {
                                                //There is leftover. Add it back to the truck's cargo system. 
                                                hauler.GetCargoSystem().AddResource(resource.GetResourceType(), resource.GetQuantity());
                                            }

                                            EntityUpdated(entityFrom, false);
                                            EntityUpdated(entityTo, false);
                                            
                                            //Make sure the receiver is not null (such as if we were delivering to an unknowned city) and that the same player doesn't own entityTo and entityFrom.
                                            if(receiver != null && receiver.GetID() != player.GetID())
                                            {
                                                CreateReport(receiver, new LaunchReport(String.format("[ECONOMY] %s delivered resources to your %s.", player.GetName(), entityTo.GetTypeName()), true, player.GetID(), receiver.GetID()));
                                            }

                                            return true;
                                        } 
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                else
                {
                    //entityTo is null, so we are dropping loot onto the ground.
                    Haulable haulable = null;
                    
                    if(bFromCargo && entityFrom instanceof HaulerInterface)
                    {
                        haulable = ((HaulerInterface)entityFrom).GetCargoSystem().RemoveHaulable(typeToMove, lTypeID, lQuantityToMove);
                    }
                    
                    if(haulable != null && geoPosition != null)
                    {
                        geoPosition.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 0.05f);
                        CreateLoot(geoPosition, haulable.GetLootType(), haulable.GetCargoID(), haulable.GetQuantity(), Defs.LOOT_EXPIRY);
                        CreateEventForPlayer(new LaunchEvent(String.format("Resources unloaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                        EntityUpdated(entityFrom, true);
                        return true;
                    }
                }
            }
            else
            {
                //typeToMove is null, so we are picking up loot from the ground.
                //GeoPosition is the coordinate of entityTo.
                if(entityTo instanceof MapEntity)
                {
                    geoPosition = ((MapEntity)entityTo).GetPosition();
                }
                else if(entityTo instanceof StoredAirplane)
                {
                    //TODO: When ships are added, this will need to be changed to make sure the plane is not on a carrier. If so, it cannot drop loot.
                    MapEntity hostEntity = ((StoredAirplane)entityTo).GetHomeBase().GetMapEntity(this);

                    if(hostEntity != null)
                    {
                        geoPosition = hostEntity.GetPosition().GetCopy();
                    }
                }

                if(entityFrom instanceof Loot)
                {
                    Loot loot = (Loot)entityFrom;
                    boolean bCreateEvent = true;
                    
                    GeoCoord geoFrom = loot.GetPosition();
                    float fltDistance = geoFrom.DistanceTo(geoPosition);

                    if(geoFrom != null && geoPosition != null && ((entityTo instanceof Shipyard && fltDistance <= Defs.SHIPYARD_LOAD_DISTANCE) || fltDistance <= Defs.LOAD_DISTANCE))
                    {
                        switch(loot.GetLootType())
                        {
                            case RESOURCES:
                            {
                                ResourceType type = ResourceType.values()[loot.GetCargoID()];

                                if(type != null) //If resource is null, then the truck did not contain this type of resource.
                                {
                                    if(bLoadAsCargo && loot.GetQuantity() > 0 && entityTo instanceof HaulerInterface)
                                    {
                                        CargoSystem receiverSystem = ((HaulerInterface)entityTo).GetCargoSystem();
                                        //Setting the quantity will cause the resource to be removed if the new quantity is 0.
                                        loot.SetQuantity(receiverSystem.AddResource(type, loot.GetQuantity()));
                                    }
                                    else if(entityTo instanceof ResourceInterface resourceTo && resourceTo.GetResourceSystem().CanHoldType(type))
                                    {
                                        ResourceSystem receiverSystem = resourceTo.GetResourceSystem();

                                        if(receiverSystem != null)
                                        {
                                            loot.SetQuantity(receiverSystem.AddQuantity(type, loot.GetQuantity()));
                                        }
                                    }

                                    EntityUpdated(entityTo, false);
                                    EntityUpdated(loot, false);
                                    
                                    if(bCreateEvent)
                                        CreateEventForPlayer(new LaunchEvent(String.format("Resources loaded."), SoundEffect.CARGO_TRANSFER), lPlayerID);
                                    
                                    return true;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return false;
    }
	
    @Override
    public boolean LaunchMissile(int lPlayerID, int lSiteID, int lSlotNo, float fltTargetLatitude, float fltTargetLongitude, EntityPointer target, SystemType systemType, boolean bAirburst)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            switch(systemType)
            {
                case MISSILE_SITE:
                {
                    MissileSite missileSite = MissileSites.get(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a missile...", player.GetName()));

                    if(missileSite != null)
                    {
                        if(missileSite.GetOwnerID() == lPlayerID)
                        {
                            if(missileSite.GetOnline() && player.Functioning())
                            {
                                MissileSystem missileSystem = missileSite.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(type != null)
                                    {
                                        if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst) && !type.GetICBM())
                                        {
                                            if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                            {
                                                //Remove respawn protection from the player.
                                                RemoveRespawnProtection(player, true);
                                                missileSite.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                missileSystem.Fire(lSlotNo);
                                                LaunchLog.Log(LaunchLog.LogType.GAME, "MissileSite", String.format("Missile Site %s Firing %s.", String.valueOf(missileSite.GetID()), type.GetName()));
                                                CreateMissileLaunch(missileSite, missileSite.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);
                                                EntityUpdated(missileSite, false);

                                                CreateEvent(new LaunchEvent(String.format("%s launched %s missile.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));

                                                return true;
                                            }
                                        }
                                        else if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst) && type.GetICBM())
                                        {
                                            if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                            {
                                                //Remove respawn protection from the player.
                                                RemoveRespawnProtection(player, true);

                                                missileSite.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                missileSystem.Fire(lSlotNo);
                                                CreateMissileLaunch(missileSite, missileSite.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);
                                                CreateEvent(new LaunchEvent(String.format("ICBM LAUNCH DETECTED", player.GetName(), type.GetName()), SoundEffect.ICBM_LAUNCH));
                                                EntityUpdated(missileSite, false);
                                                return true;
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    LaunchLog.ConsoleMessage(String.format("Missile Site %s Cannot fire. Slot no: %s. Slot contents: %d. Prep time remaining: %s.", String.valueOf(missileSite.GetID()), String.valueOf(lSlotNo), missileSite.GetMissileSystem().GetSlotMissileType(lSlotNo), String.valueOf(missileSystem.GetReloadTimeRemaining())));
                                }
                            }
                        }
                    }

                    return false;
                }

                case ARTILLERY_GUN:
                {
                    ArtilleryGun artillery = ArtilleryGuns.get(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a missile...", player.GetName()));

                    if(artillery != null)
                    {
                        if(artillery.GetOwnerID() == lPlayerID)
                        {
                            if(artillery.GetOnline() && player.Functioning())
                            {
                                MissileSystem missileSystem = artillery.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst))
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            artillery.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                            missileSystem.Fire(lSlotNo);

                                            missileSystem.SetReloadTimeRemaining(Defs.ARTILLERY_GUN_RELOAD);

                                            LaunchLog.Log(LaunchLog.LogType.GAME, "artillery", String.format("Artillery Gun %s Firing %s.", String.valueOf(artillery.GetID()), type.GetName()));
                                            CreateMissileLaunch(artillery, artillery.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);
                                            EntityUpdated(artillery, false);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s artillery shell.", player.GetName(), type.GetName()), SoundEffect.ARTILLERY_FIRE));

                                            return true;
                                        }
                                    }
                                }
                                else
                                {
                                    LaunchLog.ConsoleMessage(String.format("Artillery Gun %s Cannot fire. Slot no: %s. Slot contents: %d. Prep time remaining: %s.", String.valueOf(artillery.GetID()), String.valueOf(lSlotNo), artillery.GetMissileSystem().GetSlotMissileType(lSlotNo), String.valueOf(missileSystem.GetReloadTimeRemaining())));
                                }
                            }
                        }
                    }

                    return false;
                }

                case AIRCRAFT_MISSILES:
                {
                    Airplane aircraft = GetAirplane(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch an aircraft missile...", player.GetName()));

                    if(aircraft != null)
                    {
                        if(aircraft.GetOwnerID() == lPlayerID)
                        {
                            if(player.Functioning())
                            {
                                MissileSystem missileSystem = aircraft.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst)/* && type.GetAirLaunched()*/)
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(aircraft, aircraft.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);
                                            
                                            if(type.GetBomb())
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s dropped %s.", player.GetName(), type.GetName()), SoundEffect.BOMB_DROP));
                                            }
                                            else
                                            {
                                                CreateEvent(new LaunchEvent(String.format("%s launched %s missile.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));
                                            }

                                            EntityUpdated(aircraft, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }

                case TANK_MISSILES:
                {
                    Tank tank = GetTank(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a tank missile...", player.GetName()));

                    if(tank != null)
                    {
                        if(tank.GetOnWater())
                            return false;
                        
                        if(tank.GetOwnerID() == lPlayerID)
                        {
                            if(player.Functioning())
                            {
                                MissileSystem missileSystem = tank.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst))
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(tank, tank.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s missile.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));

                                            EntityUpdated(tank, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }
                                            
                case TANK_ARTILLERY:
                {
                    Tank tank = GetTank(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a missile...", player.GetName()));

                    if(tank != null)
                    {
                        if(tank.GetOnWater())
                            return false;
                        
                        if(tank.GetOwnerID() == lPlayerID)
                        {
                            if(player.Functioning())
                            {
                                MissileSystem missileSystem = tank.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst))
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                            missileSystem.Fire(lSlotNo);

                                            int lReloadTime = Defs.ARTILLERY_GUN_RELOAD;

                                            missileSystem.SetReloadTimeRemaining(lReloadTime);

                                            LaunchLog.Log(LaunchLog.LogType.GAME, "artillery", String.format("Artillery Gun %s Firing %s.", String.valueOf(tank.GetID()), type.GetName()));
                                            CreateMissileLaunch(tank, tank.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);
                                            EntityUpdated(tank, false);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s artillery shell.", player.GetName(), type.GetName()), SoundEffect.ARTILLERY_FIRE));

                                            return true;
                                        }
                                    }
                                }
                                else
                                {
                                    LaunchLog.ConsoleMessage(String.format("Tank %s Cannot fire. Slot no: %s. Slot contents: %d. Prep time remaining: %s.", String.valueOf(tank.GetID()), String.valueOf(lSlotNo), tank.GetMissileSystem().GetSlotMissileType(lSlotNo), String.valueOf(missileSystem.GetReloadTimeRemaining())));
                                }
                            }
                        }
                    }

                    return false;
                }                         

                case SHIP_MISSILES:
                {
                    Ship ship = GetShip(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a tank missile...", player.GetName()));

                    if(ship != null)
                    {
                        if(ship.GetOwnerID() == lPlayerID)
                        {
                            if(player.Functioning())
                            {
                                MissileSystem missileSystem = ship.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst)/* && type.GetAirLaunched()*/)
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(ship, ship.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s missile.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));

                                            EntityUpdated(ship, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }

                case SHIP_ARTILLERY:
                {
                    Ship ship = GetShip(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a tank missile...", player.GetName()));

                    if(ship != null)
                    {
                        if(ship.GetOwnerID() == lPlayerID)
                        {
                            if(player.Functioning())
                            {
                                MissileSystem missileSystem = ship.GetArtillerySystem();

                                if(missileSystem != null && missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst)/* && type.GetAirLaunched()*/)
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(ship, ship.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s artillery shell.", player.GetName(), type.GetName()), SoundEffect.ARTILLERY_FIRE));
                                            EntityUpdated(ship, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }

                case SUBMARINE_MISSILES:
                {
                    Submarine submarine = GetSubmarine(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a tank missile...", player.GetName()));

                    if(submarine != null)
                    {
                        if(submarine.GetOwnerID() == lPlayerID)
                        {
                            if(submarine.CanFireMissiles() && player.Functioning())
                            {
                                MissileSystem missileSystem = submarine.GetMissileSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst)/* && type.GetAirLaunched()*/)
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);

                                            submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(submarine, submarine.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s missile.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));

                                            EntityUpdated(submarine, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }
                
                case SUBMARINE_ICBM:
                {
                    Submarine submarine = GetSubmarine(lSiteID);

                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a tank missile...", player.GetName()));

                    if(submarine != null)
                    {
                        if(submarine.GetOwnerID() == lPlayerID)
                        {
                            if(submarine.CanFireMissiles() && player.Functioning())
                            {
                                MissileSystem missileSystem = submarine.GetICBMSystem();

                                if(missileSystem.GetSlotReadyToFire(lSlotNo))
                                {
                                    int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                    MissileType type = config.GetMissileType(lTypeID);
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);

                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, false, false, bAirburst)/* && type.GetAirLaunched()*/)
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, false, false, bAirburst) && !IsBullyingOrNuisance(lPlayerID, geoTarget, type, false, false, bAirburst))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);

                                            submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);
                                            missileSystem.Fire(lSlotNo);
                                            CreateMissileLaunch(submarine, submarine.GetPosition().GetCopy(), lTypeID, lPlayerID, geoTarget, target, bAirburst);

                                            CreateEvent(new LaunchEvent(String.format("ICBM LAUNCH DETECTED"), SoundEffect.ICBM_LAUNCH));

                                            EntityUpdated(submarine, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }
            }
        }
            
        return false;
    }

    @Override
    public boolean LaunchInterceptor(int lPlayerID, int lSiteID, int lSlotNo, int lTargetID, EntityType intTargetType, SystemType systemType)
    {
        switch(systemType)
        {
            case SAM_SITE:
            {
                Player player = Players.get(lPlayerID);
                SAMSite samSite = SAMSites.get(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch an interceptor...", player.GetName()));

                if(samSite != null)
                {
                    if(samSite.GetOwnerID() == lPlayerID)
                    {
                        if(samSite.GetOnline() && player.Functioning())
                        {
                            MissileSystem missileSystem = samSite.GetInterceptorSystem();

                            if(missileSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                InterceptorType type = config.GetInterceptorType(lTypeID);

                                switch(intTargetType)
                                {
                                    case MISSILE:
                                    {
                                        Missile targetMissile = Missiles.get(lTargetID);
                                        
                                        if(targetMissile != null)
                                        {
                                            Player targetPlayer = Players.get(targetMissile.GetOwnerID());
                                            MissileType targetType = config.GetMissileType(targetMissile.GetType());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= samSite.GetPosition().DistanceTo(targetMissile.GetPosition()))
                                            {
                                                if(!type.GetABM() && targetType.GetICBM())
                                                    return false;

                                                //Remove respawn protection from the player.
                                                if(player.GetRespawnProtected())
                                                    RemoveRespawnProtection(player, true);

                                                samSite.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                missileSystem.Fire(lSlotNo);
                                                CreateInterceptorLaunch(samSite.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.MISSILE, LaunchEntity.ID_NONE);

                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));                               

                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] [COMBAT] %s launched an interceptor at your missile.", player.GetName()), true, player.GetID(), targetPlayer.GetID()));

                                                EntityUpdated(samSite, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }

                                    case AIRPLANE:
                                    {
                                        Airplane targetAircraft = GetAirplane(lTargetID);
                                        
                                        if(targetAircraft != null)
                                        {
                                            Player targetPlayer = Players.get(targetAircraft.GetOwnerID());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= samSite.GetPosition().DistanceTo(targetAircraft.GetPosition()))
                                            {
                                                if(player.GetRespawnProtected())
                                                    RemoveRespawnProtection(player, true);
                                                
                                                samSite.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                missileSystem.Fire(lSlotNo);
                                                CreateInterceptorLaunch(samSite.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.AIRPLANE, LaunchEntity.ID_NONE);
                                                SendUserAlert(targetPlayer.GetUser(), "Interceptor Inbound", String.format("%s fired at your %s!", player.GetName(), targetAircraft.GetTypeName()), true, false);

                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetAircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));

                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] [COMBAT] %s launched an interceptor at your %s.", player.GetName(), targetAircraft.GetTypeName()), true, player.GetID(), targetPlayer.GetID()));
                                                EntityUpdated(samSite, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                
                return false;
            }
            
            case AIRCRAFT_INTERCEPTORS:
            {
                Player player = Players.get(lPlayerID);
                Airplane aircraft = GetAirplane(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch an interceptor...", player.GetName()));

                if(aircraft != null)
                {
                    if(aircraft.GetOwnerID() == lPlayerID)
                    {
                        if(player.Functioning())
                        {
                            MissileSystem missileSystem = aircraft.GetInterceptorSystem();

                            if(missileSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                InterceptorType type = config.GetInterceptorType(lTypeID);

                                switch(intTargetType)
                                {
                                    case MISSILE:
                                    {
                                        Missile targetMissile = Missiles.get(lTargetID);
                                        
                                        if(targetMissile != null)
                                        {
                                            Player targetPlayer = Players.get(targetMissile.GetOwnerID());
                                            MissileType targetType = config.GetMissileType(targetMissile.GetType());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= aircraft.GetPosition().DistanceTo(targetMissile.GetPosition()))
                                            {
                                                if(IsInterceptorValidForMissile(type, targetType, targetMissile))
                                                {
                                                    if(player.GetRespawnProtected())
                                                        RemoveRespawnProtection(player, true);

                                                    missileSystem.Fire(lSlotNo);
                                                    aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                    CreateInterceptorLaunch(aircraft.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.MISSILE, aircraft.GetID());

                                                    CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));                               

                                                    CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] [COMBAT] %s launched an interceptor at your missile.", player.GetName()), true, player.GetID(), targetPlayer.GetID()));
                                                    EntityUpdated(aircraft, true);
                                                    return true;
                                                } 
                                            }
                                        }
                                        
                                        return false;
                                    }

                                    case AIRPLANE:
                                    {
                                        Airplane targetAircraft = GetAirplane(lTargetID);
                                        
                                        if(targetAircraft != null)
                                        {
                                            Player targetPlayer = Players.get(targetAircraft.GetOwnerID());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= aircraft.GetPosition().DistanceTo(targetAircraft.GetPosition()))
                                            {
                                                if(player.GetRespawnProtected())
                                                    RemoveRespawnProtection(player, true);
                                                
                                                missileSystem.Fire(lSlotNo);
                                                aircraft.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                CreateInterceptorLaunch(aircraft.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.AIRPLANE, aircraft.GetID());
                                                SendUserAlert(targetPlayer.GetUser(), "Interceptor Inbound", String.format("%s fired at your %s!", player.GetName(), targetAircraft.GetTypeName()), true, false);
                                                
                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetAircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));

                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] [COMBAT] %s launched an interceptor at your %s.", player.GetName(), targetAircraft.GetTypeName()), true, player.GetID(), targetPlayer.GetID()));
                                                EntityUpdated(aircraft, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                
                return false;
            }
            
            case TANK_INTERCEPTORS:
            {
                Player player = Players.get(lPlayerID);
                Tank tank = GetTank(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch an interceptor...", player.GetName()));

                if(tank != null)
                {
                    if(tank.GetOnWater())
                        return false;
                    
                    if(tank.GetOwnerID() == lPlayerID)
                    {
                        if(player.Functioning())
                        {
                            MissileSystem missileSystem = tank.GetMissileSystem();

                            if(missileSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                InterceptorType type = config.GetInterceptorType(lTypeID);

                                switch(intTargetType)
                                {
                                    case MISSILE:
                                    {
                                        Missile targetMissile = Missiles.get(lTargetID);
                                        
                                        if(targetMissile != null)
                                        {
                                            Player targetPlayer = Players.get(targetMissile.GetOwnerID());
                                            MissileType targetType = config.GetMissileType(targetMissile.GetType());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= tank.GetPosition().DistanceTo(targetMissile.GetPosition()))
                                            {
                                                //Remove respawn protection from the player.
                                                //RemoveRespawnProtection(player);
                                                tank.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                                missileSystem.Fire(lSlotNo);
                                                CreateInterceptorLaunch(tank.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.MISSILE, LaunchEntity.ID_NONE);

                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));                               
                                                
                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] %s launched an interceptor at your missile.", player.GetName()), true, player.GetID(), targetPlayer.GetID()));
                                                
                                                EntityUpdated(tank, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }

                                    case AIRPLANE:
                                    {
                                        Airplane targetAircraft = GetAirplane(lTargetID);
                                        
                                        if(targetAircraft != null)
                                        {
                                            Player targetPlayer = Players.get(targetAircraft.GetOwnerID());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= tank.GetPosition().DistanceTo(targetAircraft.GetPosition()))
                                            {
                                                missileSystem.Fire(lSlotNo);
                                                CreateInterceptorLaunch(tank.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.AIRPLANE, LaunchEntity.ID_NONE);
                                                SendUserAlert(targetPlayer.GetUser(), "Interceptor Inbound", String.format("%s fired at your %s!", player.GetName(), targetAircraft.GetTypeName()), true, false);

                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetAircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));

                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] %s launched an interceptor at your %s.", player.GetName(), targetAircraft.GetTypeName()), true, player.GetID(), targetPlayer.GetID()));
                                                EntityUpdated(tank, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                
                return false;
            }
            
            case SHIP_INTERCEPTORS:
            {
                Player player = Players.get(lPlayerID);
                Ship ship = GetShip(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch an interceptor...", player.GetName()));

                if(ship != null)
                {
                    if(ship.GetOwnerID() == lPlayerID)
                    {
                        if(player.Functioning())
                        {
                            MissileSystem missileSystem = ship.GetInterceptorSystem();

                            if(missileSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = missileSystem.GetSlotMissileType(lSlotNo);
                                InterceptorType type = config.GetInterceptorType(lTypeID);

                                switch(intTargetType)
                                {
                                    case MISSILE:
                                    {
                                        Missile targetMissile = Missiles.get(lTargetID);
                                        
                                        if(targetMissile != null)
                                        {
                                            Player targetPlayer = Players.get(targetMissile.GetOwnerID());
                                            MissileType targetType = config.GetMissileType(targetMissile.GetType());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= ship.GetPosition().DistanceTo(targetMissile.GetPosition()))
                                            {
                                                if(IsInterceptorValidForMissile(type, targetType, targetMissile))
                                                {
                                                    //Remove respawn protection from the player.
                                                    if(player.GetRespawnProtected())
                                                        RemoveRespawnProtection(player, true);

                                                    missileSystem.Fire(lSlotNo);
                                                    ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                    CreateInterceptorLaunch(ship.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.MISSILE, LaunchEntity.ID_NONE);

                                                    CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetType.GetName()), SoundEffect.INTERCEPTOR_LAUNCH));                               

                                                    CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] [COMBAT] %s launched an interceptor at your missile.", player.GetName()), true, player.GetID(), targetPlayer.GetID()));

                                                    EntityUpdated(ship, true);
                                                    return true;
                                                } 
                                            }
                                        }
                                        
                                        return false;
                                    }

                                    case AIRPLANE:
                                    {
                                        Airplane targetAircraft = GetAirplane(lTargetID);
                                        
                                        if(targetAircraft != null)
                                        {
                                            Player targetPlayer = Players.get(targetAircraft.GetOwnerID());

                                            //Since this is a player's interceptor launch; it's deliberately stupid, and doesn't care if it'll intercept or not.
                                            if(config.GetInterceptorRange(type) >= ship.GetPosition().DistanceTo(targetAircraft.GetPosition()))
                                            {
                                                if(player.GetRespawnProtected())
                                                    RemoveRespawnProtection(player, true);
                                                
                                                missileSystem.Fire(lSlotNo);
                                                ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                                CreateInterceptorLaunch(ship.GetPosition().GetCopy(), lTypeID, lPlayerID, lTargetID, true, EntityType.AIRPLANE, LaunchEntity.ID_NONE);
                                                SendUserAlert(targetPlayer.GetUser(), "Interceptor Inbound", String.format("%s fired at your %s!", player.GetName(), targetAircraft.GetTypeName()), true, false);

                                                CreateEvent(new LaunchEvent(String.format("[COMBAT] %s launched an interceptor at %s's %s.", player.GetName(), targetPlayer.GetName(), targetAircraft.GetTypeName()), SoundEffect.INTERCEPTOR_LAUNCH));

                                                CreateReport(targetPlayer, new LaunchReport(String.format("[COMBAT] %s launched an interceptor at your %s.", player.GetName(), targetAircraft.GetTypeName()), true, player.GetID(), targetPlayer.GetID()));
                                                EntityUpdated(ship, true);
                                                return true;
                                            }
                                        }
                                        
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                
                return false;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean LaunchTorpedo(int lPlayerID, int lSiteID, int lSlotNo, float fltTargetLatitude, float fltTargetLongitude, EntityPointer target, SystemType systemType)
    {
        switch(systemType)
        {            
            case SHIP_TORPEDOES:
            {
                Player player = Players.get(lPlayerID);
                Ship ship = GetShip(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a ship torpedo...", player.GetName()));

                if(ship != null && ship.HasTorpedoes())
                {
                    if(ship.GetOwnerID() == lPlayerID)
                    {
                        if(player.Functioning())
                        {
                            MissileSystem torpedoSystem = ship.GetTorpedoSystem();

                            if(torpedoSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = torpedoSystem.GetSlotMissileType(lSlotNo);
                                TorpedoType type = config.GetTorpedoType(lTypeID);
                                
                                if(target != null)
                                {
                                    MapEntity entityTarget = target.GetMapEntity(this);
                                    
                                    if(!ThreatensPlayer(lPlayerID, entityTarget.GetPosition(), type, true))
                                    {
                                        if(!EntityIsFriendly(entityTarget, player) && !ThreatensFriendlies(lPlayerID, entityTarget.GetPosition(), type, true))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);

                                            torpedoSystem.Fire(lSlotNo);
                                            ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);
                                            GeoCoord geoLaunch = ship.GetPosition().GetCopy();
                                            geoLaunch.SetLastBearing(ship.GetPosition().GetLastBearing());
                                            CreateTorpedoLaunch(geoLaunch, lTypeID, lPlayerID, entityTarget.GetPosition(), target);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s torpedo.", player.GetName(), type.GetName()), SoundEffect.TORPEDO_LAUNCH));

                                            EntityUpdated(ship, false);
                                            return true;
                                        }
                                    }
                                }
                                else
                                {
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);
                                    
                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, true))
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, true))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            ship.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            torpedoSystem.Fire(lSlotNo);
                                            GeoCoord geoLaunch = ship.GetPosition().GetCopy();
                                            geoLaunch.SetLastBearing(ship.GetPosition().GetLastBearing());
                                            CreateTorpedoLaunch(geoLaunch, lTypeID, lPlayerID, geoTarget, target);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s torpedo.", player.GetName(), type.GetName()), SoundEffect.TORPEDO_LAUNCH));

                                            EntityUpdated(ship, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                                        
                return false;
            }
            
            case SUBMARINE_TORPEDO:
            {
                Player player = Players.get(lPlayerID);
                Submarine submarine = GetSubmarine(lSiteID);

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to launch a submarine torpedo...", player.GetName()));

                if(submarine != null && submarine.HasTorpedoes())
                {
                    if(submarine.GetOwnerID() == lPlayerID)
                    {
                        if(player.Functioning())
                        {
                            MissileSystem torpedoSystem = submarine.GetTorpedoSystem();

                            if(torpedoSystem.GetSlotReadyToFire(lSlotNo))
                            {
                                int lTypeID = torpedoSystem.GetSlotMissileType(lSlotNo);
                                TorpedoType type = config.GetTorpedoType(lTypeID);
                                
                                if(target != null)
                                {
                                    MapEntity entityTarget = target.GetMapEntity(this);
                                    
                                    if(!ThreatensPlayer(lPlayerID, entityTarget.GetPosition(), type, true))
                                    {
                                        if(!EntityIsFriendly(entityTarget, player) && !ThreatensFriendlies(lPlayerID, entityTarget.GetPosition(), type, true))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            submarine.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            torpedoSystem.Fire(lSlotNo);
                                            GeoCoord geoLaunch = submarine.GetPosition().GetCopy();
                                            geoLaunch.SetLastBearing(submarine.GetPosition().GetLastBearing());
                                            CreateTorpedoLaunch(geoLaunch, lTypeID, lPlayerID, entityTarget.GetPosition(), target);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s torpedo.", player.GetName(), type.GetName()), SoundEffect.TORPEDO_LAUNCH));

                                            EntityUpdated(submarine, false);
                                            return true;
                                        }
                                    }
                                }
                                else
                                {
                                    GeoCoord geoTarget = new GeoCoord(fltTargetLatitude, fltTargetLongitude);
                                    
                                    if(!ThreatensPlayer(lPlayerID, geoTarget, type, true))
                                    {
                                        if(!ThreatensFriendlies(lPlayerID, geoTarget, type, true))
                                        {
                                            //Remove respawn protection from the player.
                                            RemoveRespawnProtection(player, true);
                                            submarine.SetVisible(Defs.FIRE_VISIBILITY_TIME);

                                            torpedoSystem.Fire(lSlotNo);
                                            GeoCoord geoLaunch = submarine.GetPosition().GetCopy();
                                            geoLaunch.SetLastBearing(submarine.GetPosition().GetLastBearing());
                                            CreateTorpedoLaunch(geoLaunch, lTypeID, lPlayerID, geoTarget, target);

                                            CreateEvent(new LaunchEvent(String.format("%s launched %s torpedo.", player.GetName(), type.GetName()), SoundEffect.MISSILE_LAUNCH));

                                            EntityUpdated(submarine, false);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                                        
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param lPlayerID the player kicking the aircraft out.
     * @param aircraftToKick the pointer of the aircraft to kick out. Can be a helicopter or a plane, despite "aircraft" typically being reserved for planes in the code.
     * @return 
     */
    @Override
    public boolean KickAircraft(int lPlayerID, EntityPointer aircraftToKick)
    {
        Player kicker = GetPlayer(lPlayerID);
        LaunchEntity entity = aircraftToKick.GetEntity(game);
        
        if(entity instanceof StoredAirplane)
        {
            StoredAirplane aircraft = (StoredAirplane)entity;

            if(kicker != null)
            {
                if(kicker.Functioning())
                {
                    MapEntity host = aircraft.GetHomeBase().GetMapEntity(game);
                    Player owner = GetOwner(aircraft);

                    //Make sure host isn't null and make sure that the player owns the host.
                    if(host != null && host.GetOwnedBy(lPlayerID))
                    {
                        GeoCoord geoTarget = host.GetPosition().GetCopy();

                        if(owner != null)
                        {
                            //See if the owner has any airbases. 
                            for(Structure structure : owner.GetStructures())
                            {
                                if(structure instanceof Airbase)
                                {
                                    //See if the airbase has room.
                                    Airbase airbase = ((Airbase)structure);

                                    if(!airbase.GetAircraftSystem().Full())
                                    {
                                        if(airbase.GetPosition().DistanceTo(host.GetPosition()) <= GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetEntityType())))
                                        {
                                            geoTarget = airbase.GetPosition();
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            geoTarget.Move(random.nextDouble() * (2.0 * Math.PI), LaunchUtilities.GetRandomFloatInBounds(0, 50.0f));
                        }

                        UnitCommand(aircraft.GetOwnerID(), true, MoveOrders.MOVE, Collections.singletonList(aircraft.GetPointer()), null, geoTarget, null, -1, -1);

                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean Prospect(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to respawn...", player.GetName()));
        
        if(!player.GetAWOL() && player.GetCanProspect())
        {
            ResourceDeposit deposit = null;
            
            float fltDepositChance = random.nextFloat();
            ResourceType type = null;
            long oReserves = -1;
            
            List<ResourceType> t3Types = new ArrayList<>();
            List<ResourceType> t2Types = new ArrayList<>();
            List<ResourceType> t1Types = new ArrayList<>();
            
            t3Types.add(ResourceType.MEDICINE);
            t3Types.add(ResourceType.MACHINERY);
            t3Types.add(ResourceType.ELECTRONICS);
            
            t2Types.add(ResourceType.STEEL);
            t2Types.add(ResourceType.FOOD);
            t2Types.add(ResourceType.FUEL);
            t2Types.add(ResourceType.ELECTRICITY);
            
            t1Types.add(ResourceType.OIL);
            t1Types.add(ResourceType.IRON);
            t1Types.add(ResourceType.CROPS);
            t1Types.add(ResourceType.LUMBER);
            t1Types.add(ResourceType.CONCRETE);
            t1Types.add(ResourceType.URANIUM);
            t1Types.add(ResourceType.COAL);
            
            if(fltDepositChance <= Defs.ENRICHED_URANIUM_DEPOSIT_CHANCE)
            {
                oReserves = random.nextLong(Defs.ENRICHED_URANIUM_DEPOSIT_MIN_RESERVES, Defs.ENRICHED_URANIUM_DEPOSIT_MAX_RESERVES);
                type = ResourceType.ENRICHED_URANIUM;
            }
            else if(fltDepositChance <= Defs.GOLD_DEPOSIT_CHANCE)
            {
                oReserves = random.nextLong(Defs.GOLD_DEPOSIT_MIN_RESERVES, Defs.GOLD_DEPOSIT_MAX_RESERVES);
                type = ResourceType.GOLD;
            }
            else if(fltDepositChance <= Defs.T3_DEPOSIT_CHANCE)
            {
                oReserves = random.nextLong(Defs.DEPOSIT_MIN_RESERVES, Defs.DEPOSIT_MAX_RESERVES);
                type = t3Types.get(random.nextInt(t3Types.size()));
            }
            else if(fltDepositChance <= Defs.ENRICHED_URANIUM_DEPOSIT_CHANCE)
            {
                oReserves = random.nextLong(Defs.DEPOSIT_MIN_RESERVES, Defs.DEPOSIT_MAX_RESERVES);
                type = t2Types.get(random.nextInt(t2Types.size()));
            }
            else
            {
                oReserves = random.nextLong(Defs.DEPOSIT_MIN_RESERVES, Defs.DEPOSIT_MAX_RESERVES);
                type = t1Types.get(random.nextInt(t1Types.size()));
            }
            
            if(type != null)
            {
                GeoCoord geoPosition = player.GetPosition().GetCopy();
                geoPosition.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * Defs.DEPOSIT_VARIANCE_KM);
                
                deposit = new ResourceDeposit(GetAtomicID(lDepositIndex, ResourceDeposits), geoPosition, type, oReserves);
                AddResourceDeposit(deposit);
                
                player.Prospected(player.GetBoss() ? 0 : Defs.PROSPECT_COOLDOWN);
                EntityUpdated(player, false);      
                CreateEvent(new LaunchEvent(String.format("%s discovered %s!", player.GetName(), deposit.GetTypeName()), SoundEffect.PROSPECT));
                return true;
            } 
        }
        
        return false;
    }
    
    @Override
    public boolean DiveOrSurface(int lPlayerID, int lSubmarineID)
    {
        Player player = GetPlayer(lPlayerID);
        Submarine submarine = GetSubmarine(lSubmarineID);
        if(player != null && submarine != null)
        {
            if(player.Functioning() && submarine.GetOwnedBy(lPlayerID))
            {
                submarine.DiveOrSurface();
                submarine.SetVisible(Defs.SUBMARINE_ACTION_VISIBLE_TIME);

                if(submarine.Submerged())
                    CreateEventForPlayer(new LaunchEvent(String.format("Submarine diving..."), SoundEffect.SUB_DIVE), lPlayerID);
                else
                    CreateEventForPlayer(new LaunchEvent(String.format("Submarine surfacing..."), SoundEffect.SUB_SURFACE), lPlayerID);

                EntityUpdated(submarine, false);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean Blacklist(int lPlayerID, int lOtherPlayerID)
    {
        Player blacklister = GetPlayer(lPlayerID);
        Player blacklistee = GetPlayer(lOtherPlayerID);
        
        if(blacklister != null && blacklistee != null)
        {
            boolean bBlacklisting = true;
            
            if(blacklister.Blacklisted(lOtherPlayerID))
            {
                //The blacklistee is already blacklisted, so we are whitelisting them now.
                bBlacklisting = false;
            }
            
            if(blacklister.Functioning())
            {
                //Let people whitelist anyone, but not blacklist allies, themselves, or weaker players.
                if(!bBlacklisting || (!EntityIsFriendly(blacklister, blacklistee) && !GetAttackIsBullying(blacklister, blacklistee)))
                {
                    if(bBlacklisting)
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Blacklisted %s.", blacklistee.GetName())), lPlayerID);
                        CreateEventForPlayer(new LaunchEvent(String.format("%s blacklisted you!", blacklister.GetName())), lOtherPlayerID);
                    }
                    else
                    {
                        CreateEventForPlayer(new LaunchEvent(String.format("Whitelisted %s", blacklistee.GetName())), lPlayerID);
                        CreateEventForPlayer(new LaunchEvent(String.format("%s whitelisted you!", blacklister.GetName())), lOtherPlayerID);
                    }
                    
                    blacklister.Blacklist(lOtherPlayerID); 
                    EntityUpdated(blacklistee, false);
                    EntityUpdated(blacklister, false);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean SetTarget(int lPlayerID, EntityPointer targeter, EntityPointer target, GeoCoord geoTarget, boolean bKamikaze)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning())
        {
            LaunchEntity entityTargeter = targeter.GetEntity(game);
            
            if(entityTargeter instanceof LauncherInterface launcher)
            {
                //Aircraft get further checks because they can use guns.
                if(!(entityTargeter instanceof AirplaneInterface))
                {
                    GeoCoord geoFinalTarget = target != null ? target.GetMapEntity(this).GetPosition() : geoTarget;
                    
                    if(ThreatensFriendlies(lPlayerID, geoFinalTarget, config.GetLargestBlastRadiusMissile(launcher.GetMissileSystem()), true, true, false) || IsBullyingOrNuisance(lPlayerID, geoFinalTarget, config.GetLargestBlastRadiusMissile(launcher.GetMissileSystem()), false, false, false))
                    {
                        return false;
                    }
                }
            }

            /*if(entityTargeter instanceof MissileSite)
            {
                MissileSite site = (MissileSite)entityTargeter;

                if(geoTarget != null && target == null)
                {
                    if(site.GetPosition().DistanceTo(geoTarget) <= config.GetLongestMissileRange(site.GetMissileSystem(), true) && !ThreatensFriendlies(lPlayerID, geoTarget, config.GetLargestBlastRadiusMissile(site.GetMissileSystem()), true, true, false))
                    {
                        site.SetTarget(geoTarget);
                        site.Reboot(config.GetStructureBootTime(player));
                        EntityUpdated(site, false);

                        Player owner = GetOwner(site);

                        if(owner != null && owner.GetRespawnProtected())
                            RemoveRespawnProtection(owner, true);

                        return true;
                    }
                }
                else if(target != null)
                {
                    MapEntity targetEntity = target.GetMapEntity(this);

                    if(targetEntity != null)
                    {
                        if(TargetIsLegal(targeter, target) && !WouldBeFriendlyFire(player, GetOwner(targetEntity)))
                        {
                            if(site.GetPosition().DistanceTo(targetEntity.GetPosition()) <= config.GetLongestMissileRange(site.GetMissileSystem(), true) && !ThreatensFriendlies(lPlayerID, targetEntity.GetPosition(), config.GetLargestBlastRadiusMissile(site.GetMissileSystem()), true, true, false))
                            {
                                site.SetTarget(target);
                                site.Reboot(config.GetStructureBootTime(player));
                                EntityUpdated(site, false);

                                Player owner = GetOwner(site);

                                if(owner != null && owner.GetRespawnProtected())
                                    RemoveRespawnProtection(owner, true);

                                return true;
                            }  
                        }
                    }
                }
            }
            else*/if(entityTargeter instanceof ArtilleryGun)
            {
                ArtilleryGun site = (ArtilleryGun)entityTargeter;

                if(geoTarget != null || TargetIsLegal(targeter, target))
                {
                    GeoCoord geoFinalTarget = target != null ? target.GetMapEntity(this).GetPosition() : geoTarget;
                    
                    if(site.GetPosition().DistanceTo(geoFinalTarget) <= config.GetLongestMissileRange(site.GetMissileSystem(), true))
                    {
                        FireOrder order = new FireOrder(target != null ? target.GetMapEntity(this).GetPosition() : geoTarget, 0.0f);
                        site.FireForEffect(order);
                        EntityUpdated(site, false);

                        Player owner = GetOwner(site);

                        if(owner != null && owner.GetRespawnProtected())
                            RemoveRespawnProtection(owner, true);

                        return true;
                    }  
                }
            }
            else if(entityTargeter instanceof AirplaneInterface airplane)
            {
                Movable movable = (Movable)entityTargeter;
                
                /*if(geoTarget != null && target == null)
                {
                    boolean bIsLegalAttack = false;
                    
                    if(!ThreatensFriendlies(lPlayerID, geoTarget, config.GetLargestBlastRadiusMissile(airplane.GetMissileSystem()), true, true, false))
                    {
                        bIsLegalAttack = !IsBullyingOrNuisance(lPlayerID, geoTarget, config.GetLargestBlastRadiusMissile(airplane.GetMissileSystem()), false, false, false);
                    }
                    
                    if(bIsLegalAttack)
                    {
                        movable.AttackTarget(geoTarget);
                        EntityUpdated(movable, false);

                        Player owner = GetOwner(movable);

                        if(owner != null && owner.GetRespawnProtected())
                            RemoveRespawnProtection(owner, true);

                        return true;
                    } 
                }
                else */if(target != null)
                {
                    MapEntity targetEntity = target.GetMapEntity(this);
                    
                    if(targetEntity != null)
                    {
                        boolean bIsLegalAttack = false;
                        
                        /*if(UseMissilesForAttack(airplane.GetAircraftInterface(), geoTarget, target))
                        {
                            if(!ThreatensFriendlies(lPlayerID, targetEntity.GetPosition(), config.GetLargestBlastRadiusMissile(airplane.GetMissileSystem()), true, true, false))
                            {
                                bIsLegalAttack = !IsBullyingOrNuisance(lPlayerID, targetEntity.GetPosition(), config.GetLargestBlastRadiusMissile(airplane.GetMissileSystem()), false, false, false);
                            }
                        }
                        else
                        {
                            if(!WouldBeFriendlyFire(player, GetOwner(targetEntity)))
                            {
                                bIsLegalAttack = !GetAttackIsBullying(player, GetOwner(targetEntity));
                            }
                        }*/
                        
                        if(!WouldBeFriendlyFire(player, GetOwner(targetEntity)))
                        {
                            bIsLegalAttack = !GetAttackIsBullying(player, GetOwner(targetEntity));
                        }
                        
                        if(bIsLegalAttack)
                        {
                            if(TargetIsLegal(targeter, target) && !WouldBeFriendlyFire(player, GetOwner(targetEntity)))
                            {
                                movable.AttackTarget(target);
                                EntityUpdated(movable, false);

                                Player owner = GetOwner(movable);

                                if(owner != null && owner.GetRespawnProtected())
                                    RemoveRespawnProtection(owner, true);

                                return true;
                            }
                        }  
                    }  
                }   
            }
            else if(entityTargeter instanceof Movable)
            {
                Movable movable = (Movable)entityTargeter;
                
                if(geoTarget != null && target == null)
                {
                    movable.AttackTarget(geoTarget);
                    EntityUpdated(movable, false);

                    Player owner = GetOwner(movable);

                    if(owner != null && owner.GetRespawnProtected())
                        RemoveRespawnProtection(owner, true);

                    return true;
                }
                else if(target != null)
                {
                    MapEntity targetEntity = target.GetMapEntity(this);
                    
                    if(targetEntity != null)
                    {
                        if(TargetIsLegal(targeter, target) && !WouldBeFriendlyFire(player, GetOwner(targetEntity)))
                        {
                            movable.AttackTarget(target);
                            EntityUpdated(movable, false);

                            Player owner = GetOwner(movable);

                            if(owner != null && owner.GetRespawnProtected())
                                RemoveRespawnProtection(owner, true);

                            return true;
                        }
                    }  
                }   
            }
        }
        
        return false;
    }
    
    @Override
    public boolean SellEntity(int lPlayerID, EntityPointer pointer)
    {
        LaunchEntity entity = pointer.GetEntity(this);
        Player player = Players.get(lPlayerID);
        
        if(player.Functioning())
        {
            if(entity instanceof Blueprint blueprint)
            {
                if(blueprint.GetOwnedBy(lPlayerID))
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remove a blueprint...", player.GetName()));

                    blueprint.SetRemove();
                    EntityUpdated(blueprint, true);
                    CreateEventForPlayer(new LaunchEvent(String.format("Blueprint removed.")), lPlayerID);
                }
            }
            else if(entity instanceof Structure structure)
            {
                if(structure instanceof OreMine oreMine && oreMine.GetSelling())
                {
                    ResourceDeposit deposit = GetResourceDeposit(oreMine.GetDepositID());
                    
                    if(deposit == null)
                    {
                        return false;
                    }
                }
                
                if(!InBattle(player))
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to sell a %s...", player.GetName(), structure.GetTypeName()));

                    if((!structure.GetSelling()) && player.Functioning())
                    {
                        int lDecomTime = config.GetDecommissionTime(structure);
                        
                        structure.Sell(lDecomTime);
                        CreateEventForPlayer(new LaunchEvent(String.format("%s decommissioning...", structure.GetTypeName()), SoundEffect.MONEY), lPlayerID);
                        CreateEvent(new LaunchEvent(String.format("%s is decommissioning a %s.", player.GetName(), structure.GetTypeName())));
                        return true;
                    }
                    else if((structure.GetSelling()) && player.Functioning())
                    {
                        structure.CancelSale();
                        CreateEventForPlayer(new LaunchEvent(String.format("%s decommissioning canceled.", structure.GetTypeName()), SoundEffect.MONEY), lPlayerID);
                        CreateEvent(new LaunchEvent(String.format("%s canceled the decommission of a %s.", player.GetName(), structure.GetTypeName())));
                        return true;
                    }
                }
            }
            else if(entity instanceof StoredAirplane)
            {
                if(!InBattle(player))
                    return SellAircraft(lPlayerID, GetStoredAirplane(pointer.GetID()));
            }
            else if(entity instanceof NavalVessel)
            {
                NavalVessel vessel = (NavalVessel)entity;

                if(!InBattle(player) && ShipInPort(vessel))
                {
                    if(vessel.GetOwnedBy(lPlayerID))
                    {
                        ProcessPlayerIncome(player, String.format("sale of %s", vessel.GetTypeName()), GetSaleValue(GetNavalVesselValue(vessel)), false);
                        CreateEventForPlayer(new LaunchEvent(String.format("Vessel sold."), SoundEffect.MONEY), lPlayerID);
                        vessel.InflictDamage(vessel.GetMaxHP());

                        return true;
                    } 
                }
            }
            else if(entity instanceof LandUnit unit)
            {                      
                if(!InBattle(player))
                {
                    if(unit.GetOwnedBy(lPlayerID))
                    {
                        ProcessPlayerIncome(player, String.format("sale of %s", unit.GetTypeName()), GetSaleValue(GetLandUnitValue(unit)), false);
                        CreateEventForPlayer(new LaunchEvent(String.format("%s sold.", unit.GetTypeName()), SoundEffect.MONEY), lPlayerID);
                        unit.InflictDamage(unit.GetMaxHP());

                        return true;
                    } 
                }
            }
        }  
        
        return false;
    }

    @Override
    public boolean SellLaunchable(int lPlayerID, int lSiteID, int lSlotIndex, SystemType systemType)
    {
        switch(systemType)
        {
            case SAM_SITE:
            case AIRCRAFT_INTERCEPTORS:
            case STORED_AIRCRAFT_INTERCEPTORS:
            case TANK_INTERCEPTORS:
            case SHIP_INTERCEPTORS: return SellInterceptor(lPlayerID, lSiteID, lSlotIndex, systemType);
                
            case TANK_MISSILES:
            case TANK_ARTILLERY:
            case MISSILE_SITE:
            case SUBMARINE_ICBM:
            case SUBMARINE_MISSILES:
            case SHIP_MISSILES:
            case STORED_AIRCRAFT_MISSILES:
            case ARTILLERY_GUN:
            case AIRCRAFT_MISSILES: return SellMissile(lPlayerID, lSiteID, lSlotIndex, systemType);
                
            case SUBMARINE_TORPEDO:
            case SHIP_TORPEDOES: return SellTorpedo(lPlayerID, lSiteID, lSlotIndex, systemType);
        }
        
        return false;
    }    
    
    public boolean SellMissile(int lPlayerID, int lSiteID, int lSlotIndex, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to sell a launchable...", player.GetName()));
            
            if(player.Functioning() && !InBattle(player))
            {
                switch(systemType)
                {
                    case MISSILE_SITE:
                    {
                        MissileSite missileSite = MissileSites.get(lSiteID);
                        
                        if(missileSite != null)
                        {
                            if(missileSite.GetOwnerID() == lPlayerID)
                            {
                                system = missileSite.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case ARTILLERY_GUN:
                    {
                        ArtilleryGun artillery = ArtilleryGuns.get(lSiteID);
                        
                        if(artillery != null)
                        {
                            if(artillery.GetOwnerID() == lPlayerID)
                            {
                                system = artillery.GetMissileSystem();
                            }
                        }
                    }
                    break;

                    case STORED_AIRCRAFT_MISSILES:
                    {
                        StoredAirplane aircraft = GetStoredAirplane(lSiteID);
                        
                        if(aircraft != null && !aircraft.Flying())
                        {
                            if(aircraft.GetOwnerID() == lPlayerID)
                            {
                                system = aircraft.GetMissileSystem();
                            }   
                        }
                    }
                    break;
                    
                    case TANK_ARTILLERY:
                    case TANK_MISSILES:
                    {
                        Tank tank = GetTank(lSiteID);
                        
                        if(tank != null)
                        {
                            if(tank.GetOwnerID() == lPlayerID)
                            {
                                system = tank.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SHIP_MISSILES:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SUBMARINE_MISSILES:
                    {
                        Submarine submarine = GetSubmarine(lSiteID);
                        
                        if(submarine != null)
                        {
                            if(submarine.GetOwnerID() == lPlayerID)
                            {
                                system = submarine.GetMissileSystem();
                            }
                        }
                    }
                    break;
                }
                
                if(system != null)
                {
                    if(system.GetSlotHasMissile(lSlotIndex))
                    {
                        MissileType type = config.GetMissileType(system.GetSlotMissileType(lSlotIndex));

                        system.UnloadSlot(lSlotIndex);
                        ProcessPlayerIncome(player, String.format("sale of %s missile", type.GetName()), GetSaleValue(Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost()))), false);

                        CreateEventForPlayer(new LaunchEvent(String.format("Missile sold."), SoundEffect.MONEY), lPlayerID);

                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean SellInterceptor(int lPlayerID, int lSiteID, int lSlotIndex, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to sell a launchable...", player.GetName()));
            
            if(player.Functioning() && !InBattle(player))
            {
                switch(systemType)
                {
                    case SAM_SITE:
                    {
                        SAMSite samSite = SAMSites.get(lSiteID);
                        
                        if(samSite != null)
                        {
                            if(samSite.GetOwnerID() == lPlayerID)
                            {
                                system = samSite.GetInterceptorSystem();
                            }   
                        }
                    }
                    break;

                    case STORED_AIRCRAFT_INTERCEPTORS:
                    {
                        StoredAirplane aircraft = GetStoredAirplane(lSiteID);
                        
                        if(aircraft != null && !aircraft.Flying())
                        {
                            if(aircraft.GetOwnerID() == lPlayerID)
                            {
                                system = aircraft.GetInterceptorSystem();
                            }   
                        }
                    }
                    break;
                    
                    case TANK_INTERCEPTORS:
                    {
                        Tank tank = GetTank(lSiteID);
                        
                        if(tank != null)
                        {
                            if(tank.GetOwnerID() == lPlayerID)
                            {
                                system = tank.GetMissileSystem();
                            }
                        }
                    }
                    break;
                    
                    case SHIP_INTERCEPTORS:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetInterceptorSystem();
                            }
                        }
                    }
                    break;
                }
                
                if(system != null)
                {
                    if(system.GetSlotHasMissile(lSlotIndex))
                    {
                        InterceptorType type = config.GetInterceptorType(system.GetSlotMissileType(lSlotIndex));
                            
                        system.UnloadSlot(lSlotIndex);
                        ProcessPlayerIncome(player, String.format("sale of %s interceptor", type.GetName()), GetSaleValue(Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost()))), false);
                        
                        CreateEventForPlayer(new LaunchEvent(String.format("Interceptor sold."), SoundEffect.MONEY), lPlayerID);
                        
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean SellTorpedo(int lPlayerID, int lSiteID, int lSlotIndex, SystemType systemType)
    {
        Player player = Players.get(lPlayerID);
        MissileSystem system = null;
        
        if(player != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to sell a launchable...", player.GetName()));
            
            if(player.Functioning() && !InBattle(player))
            {
                switch(systemType)
                {
                    case SHIP_TORPEDOES:
                    {
                        Ship ship = GetShip(lSiteID);
                        
                        if(ship != null)
                        {
                            if(ship.GetOwnerID() == lPlayerID)
                            {
                                system = ship.GetTorpedoSystem();
                            }
                        }
                    }
                    break;
                    
                    case SUBMARINE_TORPEDO:
                    {
                        Submarine submarine = GetSubmarine(lSiteID);
                        
                        if(submarine != null)
                        {
                            if(submarine.GetOwnerID() == lPlayerID)
                            {
                                system = submarine.GetTorpedoSystem();
                            }
                        }
                    }
                    break;
                    
                    default:
                    {
                        LaunchLog.ConsoleMessage(String.format("case %s not recognized.", systemType));
                    }
                    break;
                }
                
                if(system != null)
                {
                    if(system.GetSlotHasMissile(lSlotIndex))
                    {
                        TorpedoType type = config.GetTorpedoType(system.GetSlotMissileType(lSlotIndex));

                        if(!type.GetNuclear())
                        {
                            system.UnloadSlot(lSlotIndex);
                            ProcessPlayerIncome(player, String.format("sale of %s", type.GetName()), GetSaleValue(Map.ofEntries(entry(ResourceType.WEALTH, type.GetCost()))), false);
                            
                            CreateEventForPlayer(new LaunchEvent(String.format("Torpedo sold."), SoundEffect.MONEY), lPlayerID);
                        }
                        
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean StructureOnlineOffline(int lPlayerID, Structure structure, boolean bOnline)
    {
        Player player = Players.get(lPlayerID);
        
        if(!structure.Destroyed() && structure.GetOwnerID() == player.GetID() && player.Functioning())
        {
            if(bOnline)
            {
                if(player.GetWealth() >= Defs.ONLINE_MAINTENANCE_COST)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Brought online.");

                    if(player.SubtractWealth(Defs.ONLINE_MAINTENANCE_COST))
                    {
                        int lBootTime = config.GetStructureBootTime(player);

                        if(player.GetBoss())
                            lBootTime = 0;

                        structure.BringOnline(lBootTime);
                        return true;
                    }
                }  
            }
            else
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Taken offline.");
                structure.TakeOffline();
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean GiveWealth(int lAdminID, int lReceiverID, int lAmount, ResourceType type)
    {
        Player admin = GetPlayer(lAdminID);
        Player receiver = GetPlayer(lReceiverID);
        
        if(admin.GetIsAnAdmin())
        {
            if(type == ResourceType.WEALTH)
            {
                ProcessPlayerIncome(receiver, String.format("admin wealth transfer from %s", admin.GetName()), Map.ofEntries(entry(ResourceType.WEALTH, (long)lAmount)), false);
                SendUserAlert(receiver.GetUser(), "Admin Wealth Transfer", String.format("%s sent you $%d", admin.GetName(), lAmount), false, true);
                CreateReport(receiver, new LaunchReport(String.format("[ECONOMY] %s transferred $%d to you.", admin.GetName(), lAmount), true));
                
                return true;
            }
            else
            {
                receiver.AddWealth(lAmount);
                SendUserAlert(receiver.GetUser(), "Admin Resource Transfer", String.format("%s sent you %d %s.", admin.GetName(), lAmount, Resource.GetTypeName(type)), false, true);
                CreateReport(receiver, new LaunchReport(String.format("[ECONOMY] %s sent you %d %s.", admin.GetName(), lAmount, Resource.GetTypeName(type)), true));

                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean SetStructuresOnOff(int lPlayerID, List<Integer> SiteIDs, boolean bOnline, EntityType structureType)
    {
        boolean bSuccess = true;
        
        switch(structureType)
        {
            case MISSILE_SITE: 
            case NUCLEAR_MISSILE_SITE:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    MissileSite missileSite = MissileSites.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, missileSite, bOnline);
                }
            }
            break;
            
            case ARTILLERY_GUN:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    ArtilleryGun artillery = ArtilleryGuns.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, artillery, bOnline);
                }
            }
            break;
            
            case SAM_SITE:
            case ABM_SILO:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    SAMSite samSite = SAMSites.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, samSite, bOnline);
                }
            }
            break;
            
            case SENTRY_GUN:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    SentryGun sentryGun = SentryGuns.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, sentryGun, bOnline);
                }
            }
            break;
            
            case RADAR_STATION:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    RadarStation radarStation = RadarStations.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, radarStation, bOnline);
                }
            }
            break;
            
            case COMMAND_POST:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    CommandPost commandPost = CommandPosts.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, commandPost, bOnline);
                }
            }
            break;
            
            case ARMORY:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Armory armory = Armories.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, armory, bOnline);
                }
            }
            break;
            
            case ORE_MINE:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    OreMine oreMine = OreMines.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, oreMine, bOnline);
                }
            }
            break;
            
            case AIRBASE:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Airbase airbase = Airbases.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, airbase, bOnline);
                }
            }
            break;
            
            case BANK:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Bank bank = Banks.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, bank, bOnline);
                }
            }
            break;
            
            case WAREHOUSE:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Warehouse warehouse = Warehouses.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, warehouse, bOnline);
                }
            }
            break;
            
            case MISSILE_FACTORY:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    MissileFactory factory = MissileFactorys.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, factory, bOnline);
                }
            }
            break;
            
            case PROCESSOR:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Processor processor = Processors.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, processor, bOnline);
                }             
            }
            break;
            
            case DISTRIBUTOR:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    Distributor distributor = Distributors.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, distributor, bOnline);
                }             
            }
            break;
            
            case SCRAP_YARD:
            {
                for(Integer lSiteID : SiteIDs)
                {
                    ScrapYard yard = ScrapYards.get(lSiteID);
                    bSuccess = bSuccess && StructureOnlineOffline(lPlayerID, yard, bOnline);
                }             
            }
            break;
        }
        
        return bSuccess;
    }
    
    @Override
    public boolean RepairEntity(int lPlayerID, EntityPointer pointer)
    {
        LaunchEntity entity = pointer.GetEntity(this);
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning() && !InBattle(player))
        {
            if(entity instanceof Structure)
            {
                Structure structure = (Structure)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), structure.GetTypeName(), structure.GetID()));

                if(structure.GetOwnerID() == lPlayerID && player.Functioning() && !InBattle(player))
                {
                    if(player.SubtractWealth(GetRepairCost(structure).get(ResourceType.WEALTH)))
                    {
                        structure.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), structure.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Shipyard)
            {
                Shipyard shipyard = (Shipyard)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), shipyard.GetName(), shipyard.GetID()));

                if(shipyard.GetOwnerID() == lPlayerID && player.Functioning() && !InBattle(player))
                {
                    if(player.SubtractWealth(GetRepairCost(shipyard).get(ResourceType.WEALTH)))
                    {
                        shipyard.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s repaired %s.", player.GetName(), shipyard.GetName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Infantry)
            {
                Infantry infantry = (Infantry)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), infantry.GetTypeName(), infantry.GetID()));

                if(infantry.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.MEDICINE, (long)(infantry.GetHPDeficit() * Defs.MEDICINE_PER_HP_INFANTRY_KG))), null, null, true, PurchaseType.OFFENSIVE))
                    {
                        infantry.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), infantry.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Tank)
            {
                Tank tank = (Tank)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), tank.GetTypeName(), tank.GetID()));

                if(tank.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.STEEL, (long)(tank.GetHPDeficit() * Defs.STEEL_PER_HP_REPAIR_KG))), null, null, true, PurchaseType.OFFENSIVE))
                    {
                        tank.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), tank.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof CargoTruck)
            {
                CargoTruck truck = (CargoTruck)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), truck.GetTypeName(), truck.GetID()));

                if(truck.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.STEEL, (long)(truck.GetHPDeficit() * Defs.STEEL_PER_HP_REPAIR_KG))), null, null, true, PurchaseType.ECONOMIC))
                    {
                        truck.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), truck.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Ship)
            {
                Ship ship = (Ship)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), ship.GetTypeName(), ship.GetID()));

                if(ship.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.STEEL, (long)(ship.GetHPDeficit() * Defs.STEEL_PER_HP_REPAIR_KG))), null, null, false, PurchaseType.OFFENSIVE))
                    {
                        ship.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), ship.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Submarine)
            {
                Submarine submarine = (Submarine)entity;

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), submarine.GetTypeName(), submarine.GetID()));

                if(submarine.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.STEEL, (long)(submarine.GetHPDeficit() * Defs.STEEL_PER_HP_REPAIR_KG))), null, null, true, PurchaseType.OFFENSIVE))
                    {
                        submarine.FullyRepair();

                        CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), submarine.GetTypeName()), SoundEffect.REPAIR));

                        return true;
                    }
                }
            }
            else if(entity instanceof Rubble)
            {
                Rubble rubble = (Rubble)entity;

                if(!rubble.GetRemove() && rubble.GetOwnerID() == lPlayerID && player.Functioning())
                {
                    ResourceType type = rubble.GetResourceType();

                    if(ProcessPlayerPurchase(lPlayerID, GetRepairCost(rubble), null, null, false, PurchaseType.DEFENSIVE))
                    {
                        if(AddStructure(lPlayerID, rubble.GetPosition().GetCopy(), rubble.GetStructureType(), type))
                        {
                            rubble.SetRemove();

                            return true;
                        }
                    }
                }
            }
        } 
        
        return false;
    }

    @Override
    public boolean HealPlayer(int lPlayerID)
    {
        return false;
    }

    @Override
    public boolean SetAvatar(int lPlayerID, int lAvatarID)
    {
        Player player = Players.get(lPlayerID);
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Setting %s's avatar.", player.GetName()));
        player.SetAvatarID(lAvatarID);
        return true;
    }

    @Override
    public boolean SetAllianceAvatar(int lPlayerID, int lAvatarID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Setting %s's alliance avatar.", player.GetName()));
            alliance.SetAvatarID(lAvatarID);
            return true;
        }
        
        return false;
    }

    /**
     * Set the modes of single or multiple SAM sites, tanks, or ships.
     */
    @Override
    public boolean SetSAMSiteModes(int lPlayerID, List<EntityPointer> SitePointers, byte cMode)
    {
        boolean bSuccess = true;
        
        for(EntityPointer pointer : SitePointers)
        {
            MapEntity launcher = pointer.GetMapEntity(game);
            
            if(launcher instanceof SAMSite samSite)
            {
                if(lPlayerID == samSite.GetOwnerID())
                {
                    samSite.SetMode(cMode);
                }
                else
                    bSuccess = false;
            }
            else if(launcher instanceof Ship ship)
            {
                if(lPlayerID == ship.GetOwnerID() && ship.HasInterceptors())
                {
                    ship.SetMode(cMode);
                }
                else
                    bSuccess = false;
            }
            else if(launcher instanceof AirplaneInterface aircraft)
            {                
                if(lPlayerID == aircraft.GetOwnerID() && aircraft.HasInterceptors())
                {
                    aircraft.SetMode(cMode);
                }
                else
                    bSuccess = false;
            }
            else if(launcher instanceof ArtilleryGun artillery)
            {
                if(lPlayerID == artillery.GetOwnerID())
                {
                    artillery.SetMode(cMode);
                }
                else
                    bSuccess = false;
            }
            else if(launcher instanceof Tank tank)
            {
                if(lPlayerID == tank.GetOwnerID() && (tank.HasArtillery() || tank.HasInterceptors()))
                {
                    tank.SetMode(cMode);
                }
                else
                    bSuccess = false;
            }
        }
        
        return bSuccess;
    }
    
    @Override
    public boolean CeaseFire(int lPlayerID, List<EntityPointer> Pointers)
    {
        Player player = GetPlayer(lPlayerID);
        
        boolean bCeasedFire = false;
        
        for(EntityPointer pointer : Pointers)
        {
            MapEntity mapEntity = pointer.GetMapEntity(game);

            if(mapEntity != null && player != null && player.Functioning() && mapEntity.GetOwnedBy(lPlayerID))
            {
                if(mapEntity instanceof Movable)
                {
                    Movable movable = (Movable)mapEntity;

                    movable.Wait();
                    EntityUpdated(movable, false);
                    bCeasedFire = true;
                    
                    if(mapEntity instanceof Tank tank && tank.HasArtillery() && tank.HasFireOrder())
                    {
                        tank.RoundsComplete();
                        bCeasedFire = true;
                    }
                }
                else if(mapEntity instanceof ArtilleryGun)
                {
                    ArtilleryGun artillery = (ArtilleryGun)mapEntity;

                    if(artillery.HasFireOrder())
                    {
                        artillery.RoundsComplete();
                        EntityUpdated(artillery, false);
                        bCeasedFire = true;
                    }
                }
            }
        }
        
        if(bCeasedFire)
        {
            CreateEventForPlayer(new LaunchEvent("Standing down...", SoundEffect.NONE), lPlayerID);
        }
        
        return bCeasedFire;
    }
    
    @Override
    public boolean SetArtilleryTarget(int lPlayerID, EntityPointer pointer, GeoCoord geoTarget, float fltRadius)
    {
        Player player = GetPlayer(lPlayerID);
        MapEntity entity = pointer.GetMapEntity(game);
        
        if(entity != null)
        {
            if(entity instanceof Ship ship)
            {
                if(player != null && ship != null)
                {
                    if(ship.GetOwnedBy(lPlayerID) && player.Functioning() && ship.HasArtillery())
                    {
                        if(player.GetRespawnProtected())
                            RemoveRespawnProtection(player, true);

                        ship.FireForEffect(new FireOrder(geoTarget, fltRadius));

                        return true;
                    }
                }
            }
            else if(entity instanceof ArtilleryGun artillery)
            {
                if(player != null)
                {
                    if(artillery.GetOwnedBy(lPlayerID) && player.Functioning())
                    {
                        if(player.GetRespawnProtected())
                            RemoveRespawnProtection(player, true);

                        artillery.FireForEffect(new FireOrder(geoTarget, fltRadius));

                        if(artillery.GetOffline())
                        {
                            artillery.BringOnline(config.GetStructureBootTime(player));
                        }

                        return true;
                    }
                }
            }
            else if(entity instanceof Tank tank)
            {
                if(player != null && tank.HasArtillery())
                {
                    if(tank.GetOwnedBy(lPlayerID) && player.Functioning())
                    {
                        if(player.GetRespawnProtected())
                            RemoveRespawnProtection(player, true);

                        tank.FireForEffect(new FireOrder(geoTarget, fltRadius));

                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean SetICBMSiloModes(int lPlayerID, List<Integer> SiteIDs, byte cMode)
    {
        boolean bSuccess = true;
        
        for(Integer lSiteID : SiteIDs)
        {
            MissileSite missileSite = MissileSites.get(lSiteID);
            
            if(lPlayerID == missileSite.GetOwnerID())
            {
                if(missileSite.CanTakeICBM())
                    missileSite.SetMode(cMode);
            }
            else
                bSuccess = false;
        }
        
        return bSuccess;
    }
    
    @Override
    public boolean SetEntityName(int lPlayerID, EntityPointer pointer, String strName)
    {
        LaunchEntity entity = pointer.GetEntity(this);
        
        if(entity != null)
        {
            if(entity instanceof Structure structure)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), structure.GetTypeName(), structure.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == structure.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    structure.SetName(strName);
                    EntityUpdated(structure, false);
                    return true;
                }

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Failed to set name to %s. Player id: %d. Structure owner id: %d", strName, player.GetID(), structure.GetOwnerID()));
            }
            else if(entity instanceof AirplaneInterface aircraft)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), aircraft.GetName(), aircraft.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == aircraft.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    aircraft.SetName(strName);
                    EntityUpdated((LaunchEntity)aircraft, false);
                    return true;
                }
            }
            else if(entity instanceof InfantryInterface infantry)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), infantry.GetName(), infantry.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == infantry.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    infantry.SetName(strName);
                    EntityUpdated((LaunchEntity)infantry, false);
                    return true;
                }
            }
            else if(entity instanceof CargoTruckInterface truck)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), truck.GetName(), truck.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == truck.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    truck.SetName(strName);
                    EntityUpdated((LaunchEntity)truck, false);
                    return true;
                }
            }
            else if(entity instanceof TankInterface tank)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), tank.GetName(), tank.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == tank.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    tank.SetName(strName);
                    EntityUpdated((LaunchEntity)tank, false);
                    return true;
                }
            }
            else if(entity instanceof NavalVessel vessel)
            {
                Player player = Players.get(lPlayerID);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting %s %d to name %s...", player.GetName(), vessel.GetName(), vessel.GetID(), strName));
                strName = LaunchUtilities.SanitiseText(strName, true, true);

                if(player.GetID() == vessel.GetOwnerID() && strName.length() <= Defs.MAX_STRUCTURE_NAME_LENGTH)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                    vessel.SetName(strName);
                    EntityUpdated((LaunchEntity)vessel, false);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean SetSAMSiteEngagementSpeed(int lPlayerID, int lSAMSiteID, float fltDistance)
    {
        Player player = Players.get(lPlayerID);
        SAMSite samSite = SAMSites.get(lSAMSiteID);
        
        if(samSite != null)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is setting SAM engagement distance to %f...", player.GetName(), fltDistance));
        
            if(player.GetID() == samSite.GetOwnerID())
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "...Succeeded.");
                samSite.SetEngagementSpeed(fltDistance);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean CloseAccount(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Closing %s's account.", player.GetName()));
        SetPlayerAWOL(player, false);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);
            DeletePlayer(lPlayerID, true);
        }
        
        return true;
    }

    @Override
    public boolean UpgradeToNuclear(int lPlayerID, int lMissileSiteID)
    {
        //"Upgrade to Nuclear" is a removed feature. -Corbin
        /*Player player = Players.get(lPlayerID);
        MissileSite missileSite = MissileSites.get(lMissileSiteID);
        
        if(lPlayerID == missileSite.GetOwnerID())
        {
            if(!missileSite.CanTakeICBM())
            {
                if(player.GetWealth() >= config.GetNukeUpgradeCost())
                {
                    player.SubtractWealth(config.GetNukeUpgradeCost());
                    missileSite.UpgradeToNuclear();
                    RemoveRespawnProtection(player);
                    CreateEvent(new LaunchEvent(String.format("%s upgraded a missile site to nuclear.", player.GetName()), SoundEffect.EQUIP));
                    
                    //Notify nearby users.
                    for(Player otherPlayer : GetPlayers())
                    {
                        switch(GetAllegiance(player, otherPlayer))
                        {
                            case NEUTRAL:
                            case ENEMY:
                            case PENDING_TREATY:
                            {
                                if(player.GetPosition().DistanceTo(otherPlayer.GetPosition()) < config.GetNuclearEscalationRadius())
                                {
                                    User user = otherPlayer.GetUser();

                                    if(user != null)
                                        user.SetNuclearEscalation();

                                    CreateReport(otherPlayer, new LaunchReport(String.format("Unfriendly player %s upgraded a missile site within EMP attack range of you to nuclear.", player.GetName()), true, player.GetID()));
                                }
                            }
                            break;
                        }
                    }
                    
                    return true;
                }
            }
        }*/
        
        return false;
    }

    @Override
    public boolean JoinAlliance(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        Alliance alliance = GetAlliance(lAllianceID);
        
        if(player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceCooloffExpired() && !InBattle(player))
        {
            player.SetAllianceRequestToJoin(lAllianceID);
            CreateEvent(new LaunchEvent(String.format("%s requested to join %s.", player.GetName(), alliance.GetName()), SoundEffect.RESPAWN));
            
            //Send report to leaders.
            for(Player leader : GetPlayers())
            {
                if(leader.GetAllianceMemberID() == lAllianceID && leader.GetIsAnMP())
                {
                    CreateReport(leader, new LaunchReport(String.format("[ALLIANCE] %s is requesting to join %s. Go to alliances to accept/reject.", player.GetName(), alliance.GetName()), false, alliance.GetID(), player.GetID(), true, false));
                }
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean LeaveAlliance(int lPlayerID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(!InBattle(player) && !player.GetPrisoner())
        {
            if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                Alliance alliance = GetAlliance(player.GetAllianceMemberID());
                player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                player.SetIsAnMP(false);
                player.SetAllianceCooloffTime(config.GetAllianceCooloffTime());
                CreateEvent(new LaunchEvent(String.format("%s left %s.", player.GetName(), alliance.GetName()), SoundEffect.EXPLOSION));

                for(Player ally : GetPlayers())
                {
                    if(ally.GetAllianceMemberID() == alliance.GetID())
                    {
                        CreateReport(ally, new LaunchReport(String.format("[ALLIANCE] %s left %s.", player.GetName(), alliance.GetName()), false, alliance.GetID(), player.GetID(), true, false));
                    }
                }

                //Disband the alliance?
                AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);

                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean DeclareWar(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance allianceOther = GetAlliance(lAllianceID);
            
            if(CanDeclareWar(alliance.GetID(), lAllianceID))
            {
                CreateWar(alliance.GetID(), lAllianceID);
                AllianceUpdated(alliance, true);
                AllianceUpdated(allianceOther, true);
                
                if(AffiliationOffered(lAllianceID, alliance.GetID()))
                {
                    CreateEvent(new LaunchEvent(String.format("%s rejected affiliation from %s and declared war (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                    CreateReport(new LaunchReport(String.format("[ALLIANCE] %s rejected affiliation from %s and declared war (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), false, alliance.GetID(), allianceOther.GetID(), true, true));
                }
                else
                {
                    CreateEvent(new LaunchEvent(String.format("%s declared war on %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                    CreateReport(new LaunchReport(String.format("[ALLIANCE] %s declared war on %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), false, alliance.GetID(), allianceOther.GetID(), true, true));
                }
                
                return true;
            }
            else
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s cannot declare war.", player.GetName()));
        }
        
        return false;
    }
    
    @Override
    public boolean AcceptSurrender(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance victor = GetAlliance(player.GetAllianceMemberID());
            Alliance forfeiter = GetAlliance(lAllianceID);
            
            if(GetAllegiance(player, forfeiter) == Allegiance.ENEMY)
            {
                War war = GetWar(forfeiter.GetID(), victor.GetID());
                war.Forfeit(forfeiter.GetID());
                ConcludeWar(war);
                victor.WarWon();
                forfeiter.WarLost();
                RemoveExistingTreaties(victor.GetID(), forfeiter.GetID());
                
                CreateEvent(new LaunchEvent(String.format("%s accepted %s's surrender (instigated by %s).", victor.GetName(), forfeiter.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s accepted %s's surrender (instigated by %s).", victor.GetName(), forfeiter.GetName(), player.GetName()), false, forfeiter.GetID(), victor.GetID(), true, true));
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean ProposeSurrender(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance allianceOther = GetAlliance(lAllianceID);
            
            if(CanProposeSurrender(alliance.GetID(), lAllianceID))
            {
                CreateSurrenderProposal(alliance.GetID(), lAllianceID);
                CreateEvent(new LaunchEvent(String.format("%s is surrendering to %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(alliance, new LaunchReport(String.format("[ALLIANCE] Your alliance attempted to surrender to %s (instigated by %s).", allianceOther.GetName(), player.GetName()), true, alliance.GetID(), allianceOther.GetID(), true, true));
                CreateReport(allianceOther, new LaunchReport(String.format("[ALLIANCE] %s attempted surrendering to your alliance (instigated by %s).", alliance.GetName(), player.GetName()), true, alliance.GetID(), allianceOther.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean RejectSurrender(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance proposer = GetAlliance(lAllianceID);
            
            if(SurrenderProposed(lAllianceID, alliance.GetID()))
            {
                SurrenderProposal surrender = GetSurrenderProposal(alliance.GetID(), proposer.GetID());
                Treaties.remove(surrender.GetID());
                TreatyRemoved(surrender);
                CreateEvent(new LaunchEvent(String.format("%s rejected %s's attmpt to surrender (instigated by %s).", alliance.GetName(), proposer.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s rejected %s's attempt to surrender (instigated by %s).", alliance.GetName(), proposer.GetName(), player.GetName()), false, alliance.GetID(), proposer.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean BreakAffiliation(int lPlayerID, int lAllianceID)
    {
        /*Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance dumper = GetAlliance(player.GetAllianceMemberID());
            Alliance dumpee = GetAlliance(lAllianceID);
            
            if(GetAllegiance(player, dumpee) == Allegiance.AFFILIATE)
            {
                Affiliation affiliation = GetAffiliation(dumper.GetID(), dumpee.GetID());
                Treaties.remove(affiliation.GetID());
                TreatyRemoved(affiliation);
                dumper.AffiliationBroken();
                
                CreateEvent(new LaunchEvent(String.format("%s broke affiliation with %s (instigated by %s).", dumper.GetName(), dumpee.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s broke affiliation with %s (instigated by %s).", dumper.GetName(), dumpee.GetName(), player.GetName()), false, dumper.GetID(), dumpee.GetID(), true, true));
                
                return true;
            }
        }*/
        
        return false;
    }

    @Override
    public boolean ProposeAffiliation(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance allianceOther = GetAlliance(lAllianceID);
            
            if(CanProposeAffiliation(alliance.GetID(), lAllianceID))
            {
                CreateAffiliationRequest(alliance.GetID(), lAllianceID);
                CreateEvent(new LaunchEvent(String.format("%s offered affiliation to %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(alliance, new LaunchReport(String.format("[ALLIANCE] Your alliance offered affiliation to %s (instigated by %s).", allianceOther.GetName(), player.GetName()), true, alliance.GetID(), allianceOther.GetID(), true, true));
                CreateReport(allianceOther, new LaunchReport(String.format("[ALLIANCE] %s offered affiliation to your alliance (instigated by %s).", alliance.GetName(), player.GetName()), true, alliance.GetID(), allianceOther.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean AcceptAffiliation(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance allianceOther = GetAlliance(lAllianceID);
            
            if(AffiliationOffered(lAllianceID, alliance.GetID()))
            {
                CreateAffiliation(alliance.GetID(), lAllianceID);
                AllianceUpdated(alliance, true);
                AllianceUpdated(allianceOther, true);
                CreateEvent(new LaunchEvent(String.format("%s affiliated with %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s affiliated with %s (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), false, alliance.GetID(), allianceOther.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean RejectAffiliation(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            Alliance allianceOther = GetAlliance(lAllianceID);
            
            if(AffiliationOffered(lAllianceID, alliance.GetID()))
            {
                RemoveExistingTreaties(lAllianceID, alliance.GetID());
                CreateEvent(new LaunchEvent(String.format("%s rejected %s's affiliation offer (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s rejected %s's affiliation offer (instigated by %s).", alliance.GetName(), allianceOther.GetName(), player.GetName()), false, alliance.GetID(), allianceOther.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean CancelAffiliation(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && player.GetAllianceMemberID() != lAllianceID && player.GetIsAnMP())
        {
            Alliance canceller = GetAlliance(player.GetAllianceMemberID());
            Alliance cancellee = GetAlliance(lAllianceID);
            
            if(AffiliationOffered(canceller.GetID(), cancellee.GetID()))
            {
                RemoveExistingTreaties(lAllianceID, canceller.GetID());
                CreateEvent(new LaunchEvent(String.format("%s canceled their affiliation offer to %s (instigated by %s).", canceller.GetName(), cancellee.GetName(), player.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("[ALLIANCE] %s canceled their affiliation offer to %s (instigated by %s).", canceller.GetName(), cancellee.GetName(), player.GetName()), false, canceller.GetID(), cancellee.GetID(), true, true));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean Promote(int lPromotor, int lPromotee)
    {
        Player promotor = GetPlayer(lPromotor);
        Player promotee = GetPlayer(lPromotee);
        Alliance alliance = GetAlliance(promotor.GetAllianceMemberID());
        
        if(promotor.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && promotor.GetAllianceMemberID() == promotee.GetAllianceMemberID() && promotor.GetIsAnMP() && !promotee.GetIsAnMP())
        {
            promotee.SetIsAnMP(true);
            AllianceUpdated(alliance, true);
            CreateEvent(new LaunchEvent(String.format("%s promoted %s to lead %s.", promotor.GetName(), promotee.GetName(), alliance.GetName()), SoundEffect.RESPAWN));
            
            if(promotee.GetPrisoner())
            {
                promotee.SetPrisoner(false);
            }
            
            EntityUpdated(promotee, false);
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean AcceptJoin(int lLeaderID, int lJoinerID)
    {
        Player leader = GetPlayer(lLeaderID);
        Alliance alliance = GetAlliance(leader.GetAllianceMemberID());
        Player joiner = GetPlayer(lJoinerID);
        
        if(leader.GetIsAnMP() && joiner.GetAllianceJoiningID() == alliance.GetID() && !InBattle(joiner))
        {
            joiner.SetAllianceID(alliance.GetID());
            
            if(AllianceMemberRosters.containsKey(alliance.GetID()))
            {
                Map<Integer, Player> Roster = AllianceMemberRosters.get(alliance.GetID());
                
                if(Roster != null)
                {
                    if(!Roster.containsKey(lJoinerID))
                    {
                        Roster.put(lJoinerID, joiner);
                        AllianceMemberRosters.put(alliance.GetID(), Roster);
                    }
                }
            }
            
            AllianceUpdated(alliance, true);
            CreateEvent(new LaunchEvent(String.format("%s joined %s.", joiner.GetName(), alliance.GetName()), SoundEffect.RESPAWN));
            CreateReport(joiner, new LaunchReport(String.format("[ALLIANCE] You joined %s.", alliance.GetName()), false, alliance.GetID(), joiner.GetID(), true, false));
            
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    if(player != joiner)
                    {    
                        CreateReport(player, new LaunchReport(String.format("[ALLIANCE] %s joined %s and is now your ally. Approved by %s.", joiner.GetName(), alliance.GetName(), leader.GetName()), false, alliance.GetID(), joiner.GetID(), true, false));
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean RejectJoin(int lLeaderID, int lMemberID)
    {
        Player leader = GetPlayer(lLeaderID);
        Alliance alliance = GetAlliance(leader.GetAllianceMemberID());
        Player joiner = GetPlayer(lMemberID);
        
        if(leader.GetIsAnMP() && joiner.GetAllianceJoiningID() == alliance.GetID())
        {
            joiner.RejectAllianceRequestToJoin();
            AllianceUpdated(alliance, true);
            CreateEvent(new LaunchEvent(String.format("%s's application to join %s was declined.", joiner.GetName(), alliance.GetName()), SoundEffect.RESPAWN));
            CreateReport(joiner, new LaunchReport(String.format("[ALLIANCE] Your request to join %s was rejected by %s.", alliance.GetName(), leader.GetName()), false, alliance.GetID(), leader.GetID(), true, false));
            
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    if(player != joiner)
                    {    
                        CreateReport(player, new LaunchReport(String.format("[ALLIANCE] %s's application to join %s was declined by %s.", joiner.GetName(), alliance.GetName(), leader.GetName()), false, alliance.GetID(), joiner.GetID(), true, false));
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean Kick(int lLeaderID, int lMemberID)
    {
        Player leader = GetPlayer(lLeaderID);
        Player kickee = GetPlayer(lMemberID);
        
        if(leader.GetAllianceMemberID() == kickee.GetAllianceMemberID() && leader.GetIsAnMP() && !kickee.GetIsAnMP() && !InBattle(kickee))
        {
            Alliance alliance = GetAlliance(leader.GetAllianceMemberID());
            kickee.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
            kickee.SetIsAnMP(false);
            kickee.SetAllianceCooloffTime(config.GetAllianceCooloffTime());
            
            CreateEvent(new LaunchEvent(String.format("%s was kicked out of %s.", kickee.GetName(), alliance.GetName()), SoundEffect.EXPLOSION));
            CreateReport(kickee, new LaunchReport(String.format("[ALLIANCE] You were kicked out of %s by %s.", alliance.GetName(), leader.GetName()), true, alliance.GetID(), leader.GetID(), true, false));
            
            for(Player ally : GetPlayers())
            {
                if(ally.GetAllianceMemberID() == alliance.GetID())
                {
                    CreateReport(ally, new LaunchReport(String.format("[ALLIANCE] %s was kicked out of %s by %s.", kickee.GetName(), alliance.GetName(), leader.GetName()), false, alliance.GetID(), kickee.GetID(), true, false));
                }
            }
            
            AllianceUpdated(alliance, true);
            return true;
        }
        
        return false;
    }

    @Override
    public LaunchGame GetGame()
    {
        return this;
    }

    @Override
    public void BadAvatar(int lAvatarID)
    {
        //Bad avatar reported. Reset all players and alliances that use it to avatar zero.
        for(Player player : GetPlayers())
        {
            if(player.GetAvatarID() == lAvatarID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s's avatar ID %d reported as bad. Resetting to zero.", player.GetName(), lAvatarID));
                player.SetAvatarID(0);
            }
        }
        
        for(Alliance alliance : GetAlliances())
        {
            if(alliance.GetAvatarID() == lAvatarID)
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s's alliance avatar ID %d reported as bad. Resetting to zero.", alliance.GetName(), lAvatarID));
                alliance.SetAvatarID(0);
            }
        }
    }

    @Override
    public void BadImage(int lImageID)
    {
        //Bad image reported. Log it.
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Image asset %d reported as bad!", lImageID));
    }

    @Override
    public boolean CreateAlliance(int lCreatorID, String strName, String strDescription, int lAvatarID)
    {
        Player creator = Players.get(lCreatorID);
        strName = LaunchUtilities.BlessName(strName);
        strDescription = LaunchUtilities.SanitiseText(strDescription, false, true);
        
        if(creator.Functioning() && !creator.GetRespawnProtected() && creator.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED && creator.GetAllianceCooloffExpired() && strName.length() > 0 && strName.length() <= Defs.MAX_ALLIANCE_NAME_LENGTH)
        {
            for(Alliance alliance : GetAlliances())
            {
                //Name taken?
                if(alliance.GetName().equals(strName))
                    return false;
            }
            
            CreateAlliance(creator, strName, strDescription, lAvatarID);
            CreateEvent(new LaunchEvent(String.format("The alliance %s has been founded by %s", strName, creator.GetName()), SoundEffect.RESPAWN));
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean TempBan(int lPlayerID, String strReason, String strBanner)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            if(!player.GetIsAnAdmin())
            {
                //Ban the user.
                User user = player.GetUser();

                if(user != null)
                {
                    if(user.GetBanState() == BanState.NOT)
                    {
                        long oHours = user.GetNextBanTime() / Defs.MS_PER_HOUR;
                        user.TempBan(strReason);
                        comms.StopCommsTo(user);
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s was banned by %s. Reason: %s", player.GetName(), strBanner, strReason));
                        CreateEvent(new LaunchEvent(String.format("%s was banned.", player.GetName()), SoundEffect.DEATH));
                        CreateReport(new LaunchReport(String.format("%s was banned for %dhrs. Reason: %s", player.GetName(), oHours, strReason), false, lPlayerID));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean PermaBan(int lPlayerID, String strReason, String strBanner)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            if(!player.GetIsAnAdmin())
            {
                //Ban the user.
                User user = player.GetUser();

                if(user != null)
                {
                    //Don't reban already banned players, to prevent ban bomb log spam.
                    if(user.GetBanState() != BanState.PERMABANNED)
                    {
                        //Remove all of their missiles.
                        for(Missile missile : GetMissiles())
                        {
                            if(missile.GetOwnedBy(player.GetID()))
                            {
                                //Refund any players for their interceptors.
                                /*for(Interceptor interceptor : GetInterceptors())
                                {
                                    if(interceptor.GetTargetID() == missile.GetID())
                                    {
                                        int lCost = config.GetInterceptorCost(interceptor.GetType());
                                        Player interceptorOwner = Players.get(interceptor.GetOwnerID());
                                        interceptorOwner.AddWealth(lCost);
                                        CreateReport(interceptorOwner, new LaunchReport(String.format("You were reimbursed for the cost of your interceptors tracking banned player %s's missiles.", player.GetName()), true, interceptorOwner.GetID()));
                                    }
                                }*/

                                missile.Destroy();
                            }
                        }

                        user.Permaban(strReason);
                        comms.StopCommsTo(user);
                        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s was permabanned by %s. Reason: %s", player.GetName(), strBanner, strReason));
                        CreateEvent(new LaunchEvent(String.format("%s was permabanned.", player.GetName()), SoundEffect.DEATH));
                        CreateReport(new LaunchReport(String.format("%s was permabanned. Reason: %s", player.GetName(), strReason), false, lPlayerID));

                        //Disband their old alliance?
                        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                            player.SetIsAnMP(false);
                            Alliance alliance = Alliances.get(player.GetAllianceMemberID());
                            AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);
                        }

                        //Proscribe the IP address and location.
                        ProscribedIP proscribedIP = new ProscribedIP(GetAtomicID(lProscribedIPIndex, ProscribedIPs), user.GetLastIP());
                        ProscribedIPs.put(proscribedIP.GetID(), proscribedIP);

                        ProscribedLocation proscribedLocation = new ProscribedLocation(GetAtomicID(lProscribedLocationIndex, ProscribedLocations), player.GetPosition().GetCopy());
                        ProscribedLocations.put(proscribedIP.GetID(), proscribedLocation);
                    }

                    //Offline all structures.
                    for(Structure structure : GetAllStructures())
                    {
                        if(structure.GetOwnedBy(lPlayerID))
                        {
                            structure.TakeOffline();
                        }
                    }

                    player.SetAWOL(true);

                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean Unban(int lUnbannerID, int lUnbannedID)
    {
        Player plyAdmin = Players.get(lUnbannerID);
        Player unBanned = Players.get(lUnbannedID);
        
        if(unBanned != null && plyAdmin.GetIsAnAdmin())
        {
            //Unban the user.
            User user = unBanned.GetUser();
            
            if(user != null)
            {
                user.Unban();
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s was unbanned by %s.", unBanned.GetName(), plyAdmin.GetName()));
                CreateEvent(new LaunchEvent(String.format("%s was unbanned.", unBanned.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("%s was unbanned.", unBanned.GetName()), false, lUnbannedID));
            }
            
            return true;
        }
        
        return false;
    }
    
    /*@Override
    public boolean AdminDestroyEntity(int lAdminID, EntityPointer toDestroyPointer)
    {
        
    }*/
    
    public boolean ConsoleUnban(int lUnbannedID)
    {
        Player unBanned = Players.get(lUnbannedID);
        
        if(unBanned != null)
        {
            //Unban the user.
            User user = unBanned.GetUser();
            
            if(user != null)
            {
                user.Unban();
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s was unbanned.", unBanned.GetName()));
                CreateEvent(new LaunchEvent(String.format("%s was unbanned.", unBanned.GetName()), SoundEffect.RESPAWN));
                CreateReport(new LaunchReport(String.format("%s was unbanned.", unBanned.GetName()), false, lUnbannedID));
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean AvatarReset(int lPlayerAdminID, int lPlayerToResetID)
    {
        Player admin = Players.get(lPlayerAdminID);
        Player offender = Players.get(lPlayerToResetID);
        
        if(admin != null && offender != null)
        {
            if(admin.GetIsAnAdmin())
            {
                offender.SetAvatarID(0);
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s reset %s's avatar", admin.GetName(), offender.GetName()));
                CreateReport(offender, new LaunchReport("Your avatar did not comply with the game rules and was removed.", true));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean NameReset(int lPlayerAdminID, int lPlayerToResetID)
    {
        Player admin = Players.get(lPlayerAdminID);
        Player offender = Players.get(lPlayerToResetID);
        
        if(admin != null && offender != null)
        {
            if(admin.GetIsAnAdmin())
            {
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s reset %s's name", admin.GetName(), offender.GetName()));
                offender.ChangeName(LaunchUtilities.GetRandomSanctifiedString() + "Mr(s) Rudechops");
                CreateReport(offender, new LaunchReport("Your name did not comply with the game rules and was changed.", true));
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void SpoofWarnings(int lPlayerID, LocationSpoofCheck spoofCheck)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s may have GPS spoofed. Distance %skm, Speed %skph.", player.GetName(), spoofCheck.GetDistance(), spoofCheck.GetSpeed()), true, player.GetID()));
        }
    }

    @Override
    public void MultiAccountingCheck(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            User user = player.GetUser();
            
            if(user != null)
            {
                for(Player otherPlayer : GetPlayers())
                {
                    if(player.GetID() != otherPlayer.GetID())
                    {
                        if(player.GetPosition().DistanceTo(otherPlayer.GetPosition()) < Defs.MULTIACCOUNT_CONSIDERATION_DISTANCE)
                        {
                            User otherUser = otherPlayer.GetUser();
                            
                            if(otherUser != null)
                            {
                                if(user.GetDeviceID().equals(otherUser.GetDeviceID()))
                                {
                                    CreateAdminReport(new LaunchReport(String.format("%s and %s are within 0.5km and have the same device hash.", player.GetName(), otherPlayer.GetName()), true, lPlayerID, otherPlayer.GetID()));
                                    /*PermaBan(player.GetID(), String.format("Multiaccounting (autodetected same device as %s)", otherPlayer.GetName()), "[SERVER]");
                                    PermaBan(otherPlayer.GetID(), String.format("Multiaccounting (autodetected same device as %s)", player.GetName()), "[SERVER]");*/
                                }
                            }
                        }
                    }
                }
            }
        
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Performed multiaccounting check for %s", player.GetName()));
        }
    }

    @Override
    public boolean TransferAccount(int lFromID, int lToID)
    {
        Player playerOriginal = Players.get(lFromID);
        Player playerNew = Players.get(lToID);
        
        String newToken;
        
        if(playerOriginal != null && playerNew != null)
        {
            //Get both users.
            User userOriginal = playerOriginal.GetUser();
            User userNew = playerNew.GetUser();
            
            //The new user should exist otherwise something's really not right.
            if(userNew != null)
            {
                newToken = userNew.GetToken();
                
                //Delete the new user.
                Users.remove(userNew.GetIdentityKey());
                
                //Delete the old user if it exists.
                if(userOriginal != null)
                    Users.remove(userOriginal.GetIdentityKey());
                else
                    LaunchLog.ConsoleMessage("NOTE: Original user was NULL so couldn't be deleted.");
                
                //Create a replacement user with the credentials from the new user.
                User userReplacement = new User(userNew.GetIMEI(), userNew.GetGoogleID(), playerOriginal.GetID());

                //Assign the original player the replacement user, and add the replacement user to the Users list.
                playerOriginal.SetUser(userReplacement);
                Users.put(userReplacement.GetIdentityKey(), userReplacement);
                
                userReplacement.SetToken(newToken);
                
                //Delete the new player and any associated artefacts (they won't be transferred). This will also interrupt all comms, which we want to happen.
                DeletePlayer(playerNew.GetID(), false);
            }
            else
            {
                LaunchLog.ConsoleMessage("Can't transfer account: The new user is NULL.");
            }
        }
        
        return false;
    }
    
    @Override
    public boolean TransferAccountAdmin(int lAdminID, int lFromID, int lToID)
    {
        Player adminPlayer = Players.get(lAdminID);
        
        if(adminPlayer.GetIsAnAdmin())
        {
            Player playerOriginal = Players.get(lFromID);
            Player playerNew = Players.get(lToID);

            String newToken;

            if(playerOriginal != null && playerNew != null)
            {
                //Get both users.
                User userOriginal = playerOriginal.GetUser();
                User userNew = playerNew.GetUser();

                //The new user should exist otherwise something's really not right.
                if(userNew != null)
                {
                    newToken = userNew.GetToken();

                    //Delete the new user.
                    Users.remove(userNew.GetIdentityKey());

                    //Delete the old user if it exists.
                    if(userOriginal != null)
                        Users.remove(userOriginal.GetIdentityKey());
                    else
                        LaunchLog.ConsoleMessage("NOTE: Original user was NULL so couldn't be deleted.");

                    //Create a replacement user with the credentials from the new user.
                    User userReplacement = new User(userNew.GetIMEI(), userNew.GetGoogleID(), playerOriginal.GetID());

                    //Assign the original player the replacement user, and add the replacement user to the Users list.
                    playerOriginal.SetUser(userReplacement);
                    Users.put(userReplacement.GetIdentityKey(), userReplacement);

                    userReplacement.SetToken(newToken);

                    //Delete the new player and any associated artefacts (they won't be transferred). This will also interrupt all comms, which we want to happen.
                    DeletePlayer(playerNew.GetID(), false);
                    
                    //TODO: Send a user event to the admin that the account transfer was successful.
                    CreateEventForPlayer(new LaunchEvent(String.format("Transferred %s to %s. Deleting %s...", playerOriginal.GetName(), playerNew.GetName(), playerNew.GetID()), SoundEffect.RESPAWN), lAdminID);
                    
                    
                    return true;
                }
                else
                {
                    LaunchLog.ConsoleMessage("Can't transfer account: The new user is NULL.");
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean ChangePlayerName(int lPlayerID, String strNewName)
    {
        Player player = Players.get(lPlayerID);
        strNewName = LaunchUtilities.BlessName(strNewName);
        
        if(player != null && strNewName.length() > 0 && strNewName.length() <= Defs.MAX_PLAYER_NAME_LENGTH)
        {
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is changing their name to %s.", player.GetName(), strNewName));
            player.ChangeName(strNewName);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean ChangeAllianceName(int lPlayerID, String strNewName)
    {
        Player player = Players.get(lPlayerID);
        strNewName = LaunchUtilities.BlessName(strNewName);
        LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("Player attempting to change alliance name..."));
        
        if(player != null && strNewName.length() > 0 && strNewName.length() <= Defs.MAX_ALLIANCE_NAME_LENGTH)
        {
            if(player.GetIsAnMP())
            {
                Alliance alliance = Alliances.get(player.GetAllianceMemberID());
                
                if(alliance != null)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is changing their alliance's name to %s.", player.GetName(), strNewName));
                    alliance.SetName(strNewName);
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean ChangeAllianceDescription(int lPlayerID, String strNewDescription)
    {
        Player player = Players.get(lPlayerID);
        strNewDescription = LaunchUtilities.SanitiseText(strNewDescription, false, true);
        
        if(player != null && strNewDescription.length() <= Defs.MAX_ALLIANCE_DESCRIPTION_LENGTH)
        {
            if(player.GetIsAnMP())
            {
                Alliance alliance = Alliances.get(player.GetAllianceMemberID());
                
                if(alliance != null)
                {
                    LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is changing their alliance's description to %s.", player.GetName(), strNewDescription));
                    alliance.SetDescription(strNewDescription);
                }
            }
        }
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Debug functions.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public void DebugAdvanceTicks(int lTicks)
    {
        comms.InterruptAll();
        
        for(int i = 0; i < lTicks; i++)
        {
            GameTick(lGameTickRate);
        }
    }
    
    public void DebugForceEndOfMinute()
    {
        MinuteEnded();
    }
    
    public void DebugForceEndOfHour()
    {
        HourEnded();
    }
    
    public void DebugForceEndOfDay()
    {
        DayEnded();
    }
    
    public void DebugForceEndOfWeek()
    {
        WeekEnded();
    }

    @Override
    public String GetPlayerName(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player == null)
            return "[DOESN'T EXIST]";
        
        return player.GetName();
    }

    @Override
    public User GetUser(int lPlayerID)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
            return player.GetUser();
        
        return null;
    }

    @Override
    public void AdminReport(LaunchReport report)
    {
        CreateAdminReport(report);
    }

    @Override
    public void NotifyDeviceChecksCompleteFailure(String strPlayerName)
    {
        CreateAdminReport(new LaunchReport(String.format("[Admin] A device check total failure occurred for %s.", strPlayerName), true));
    }

    @Override
    public void NotifyDeviceChecksAPIFailure(String strPlayerName)
    {
        CreateAdminReport(new LaunchReport(String.format("[Admin] A device check API failure occurred for %s.", strPlayerName), true));
    }

    @Override
    public void NotifyDeviceCheckFailure(User user)
    {
        Player player = Players.get(user.GetPlayerID());
        
        if(player != null)
        {
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s failed device checks. Admin checks required.", player.GetName()), true, user.GetPlayerID()));
        }
    }

    @Override
    public void NotifyIPProscribed(User user)
    {
        Player player = Players.get(user.GetPlayerID());
        
        if(player != null)
        {
            this.PermaBan(player.GetID(), "[CounterShield] AutoDetect Alt Account Bypass", "[SERVER]");
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s registered with a proscribed IP address. Admin checks required.", player.GetName()), true, user.GetPlayerID()));
        }
    }

    @Override
    public void NotifyLocationProscribed(User user)
    {
        Player player = Players.get(user.GetPlayerID());
        
        if(player != null)
        {
            this.PermaBan(player.GetID(), "[CounterShield] AutoDetect Alt Account Bypass", "[SERVER]");
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s registered close to a proscribed location. Admin checks required.", player.GetName()), true, user.GetPlayerID()));
        }
    }

    @Override
    public void NotifyAccountRestricted(User user)
    {
        Player player = Players.get(user.GetPlayerID());
        
        if(player != null)
        {
            CreateAdminReport(new LaunchReport(String.format("[Admin] %s declined weapon launch as their account is restricted. Admin checks required.", player.GetName()), true, user.GetPlayerID()));
        }        
    }

    @Override
    public boolean GetIpAddressProscribed(String strIPAddress)
    {
        for(ProscribedIP proscribedIP : ProscribedIPs.values())
        {
            if(proscribedIP.strIPAddress.equals(strIPAddress))
                return true;
        }
        
        return false;
    }

    @Override
    public boolean GetLocationProscribed(GeoCoord geoLocation)
    {
        for(ProscribedLocation proscribedLocation : ProscribedLocations.values())
        {
            if(proscribedLocation.geoLocation.DistanceTo(geoLocation) < PROSCRIBED_LOCATION_COLLISION)
                return true;
        }
        
        return false;
    }
    
    public void PerformRestoration(int lPlayerID, LaunchGame snapshot)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            for(Structure restoreStructure : snapshot.GetAllStructures())
            {
                if(restoreStructure.GetOwnedBy(lPlayerID))
                {
                    //Candidate for restoration.
                    if(GetNearbyStructures(restoreStructure.GetPosition(), config.GetStructureSeparation(restoreStructure.GetEntityType())).isEmpty())
                    {
                        //Cleared to reconstruct. Remove any loots.
                        for(Loot loot : GetLoots())
                        {
                            if(loot.GetPosition().BroadPhaseCollisionTest(restoreStructure.GetPosition()))
                            {
                                if(loot.GetPosition().DistanceTo(restoreStructure.GetPosition()) < config.GetStructureSeparation(restoreStructure.GetEntityType()))
                                {
                                    LaunchLog.ConsoleMessage(String.format("Removing a loot while restoring %s's stuff.", player.GetName()));
                                    loot.Collect();
                                }
                            }
                        }

                        if(restoreStructure instanceof MissileSite)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's missile site.", player.GetName()));
                            AddMissileSite((MissileSite)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lMissileSiteIndex, MissileSites)));
                        }

                        if(restoreStructure instanceof SAMSite)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's SAM site.", player.GetName()));
                            AddSAMSite((SAMSite)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lSAMSiteIndex, SAMSites)));
                        }

                        if(restoreStructure instanceof SentryGun)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's sentry gun.", player.GetName()));
                            AddSentryGun((SentryGun)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lSentryGunIndex, SentryGuns)));
                        }

                        if(restoreStructure instanceof OreMine)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's ore mine.", player.GetName()));
                            AddOreMine((OreMine)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lOreMineIndex, OreMines)));
                        }
                        
                        if(restoreStructure instanceof RadarStation)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's radar station.", player.GetName()));
                            AddRadarStation((RadarStation)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lRadarStationIndex, RadarStations)));
                        }
                        
                        if(restoreStructure instanceof CommandPost)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's commandPost.", player.GetName()));
                            AddCommandPost((CommandPost)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lCommandPostIndex, CommandPosts)));
                        }
                        
                        if(restoreStructure instanceof Airbase)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's airbase.", player.GetName()));
                            AddAirbase((Airbase)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lAirbaseIndex, Airbases)));
                        }
                        
                        if(restoreStructure instanceof Bank)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's bank.", player.GetName()));
                            AddBank((Bank)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lBankIndex, Banks)));
                        }
                        
                        if(restoreStructure instanceof Warehouse)
                        {
                            LaunchLog.ConsoleMessage(String.format("Restoring %s's warehouse.", player.GetName()));
                            AddWarehouse((Warehouse)restoreStructure.ReIDAndReturnSelf(GetAtomicID(lWarehouseIndex, Warehouses)));
                        }
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage(String.format("Skipping a structure while restoring %s's stuff as something's already there.", player.GetName()));
                    }
                }
            }
            
            for(StoredAirplane restoreAircraft : snapshot.GetStoredAirplanes())
            {
                if(restoreAircraft.GetOwnedBy(player.GetID()))
                {
                    LaunchLog.ConsoleMessage(String.format("Restoring %s's stored aircraft.", player.GetName()));
                    AddStoredAircraft(restoreAircraft.ReIDAndReturnSelf(GetAtomicID(lAirplaneIndex, Airplanes)));
                }
            }
            
            LaunchLog.ConsoleMessage("Restoration complete.");
        }
        else
        {
            LaunchLog.ConsoleMessage("The player was invalid. Could not perform restoration.");
        }
    }
    
    public void BanBomb(int lPlayerID, float fltBlastRadius)
    {
        Player player = Players.get(lPlayerID);
        
        if(player != null)
        {
            CreateReport(new LaunchReport(String.format("A ban bomb was detonated at %s's location.", player.GetName()), false, lPlayerID));
            
            for(Player possibleBannee : GetPlayers())
            {
                if(player.GetPosition().DistanceTo(possibleBannee.GetPosition()) < fltBlastRadius)
                {
                    LaunchLog.ConsoleMessage(String.format("The ban bomb hit %s.", possibleBannee.GetName()));
                    
                    PermaBan(possibleBannee.GetID(), "Hit by an intercontinental ballistic nuclear ban hammer", "The God of Launch");
                }
            }
        }
    }
    
    public void ListProscriptions()
    {
        for(ProscribedIP ip : ProscribedIPs.values())
        {
            LaunchLog.ConsoleMessage(String.format("%s - %dms remaining", ip.strIPAddress, ip.dlyExpiry.GetRemaining()));
        }
        
        for(ProscribedLocation loc : ProscribedLocations.values())
        {
            LaunchLog.ConsoleMessage(String.format("(%.4f, %.4f) - %dms remaining", loc.geoLocation.GetLatitude(), loc.geoLocation.GetLongitude(), loc.dlyExpiry.GetRemaining()));
        }
    }
    
    public float GetSentryHitChance(Missile missile)
    {
        float sentryhitchance;
        
        if(config.GetMissileType(missile.GetType()).GetECM())
        {
            sentryhitchance = config.GetSentryGunHitChance() - config.GetECMInterceptorChanceReduction();
        }
        else 
        {
            sentryhitchance = config.GetSentryGunHitChance();
        }
        
        return sentryhitchance;
    }
   
    public void PlayerRankUp(Player player)
    {
        
        if(player.GetRank() != 29)
        {
            player.RankUp();
            
            if(player.GetRank() == Defs.MARKET_ACCESS_RANK)
            {
                CreateReport(player, new LaunchReport(String.format("[ECONOMY] You were promoted to rank %d. Your hourly income has increased by $%d. You can now access the market!", player.GetRank(), Defs.INCOME_PER_RANK), true));
            }
            else
            {
                CreateReport(player, new LaunchReport(String.format("[ECONOMY] You were promoted to rank %d. Your hourly income has increased by $%d.", player.GetRank(), Defs.INCOME_PER_RANK), true));
            }
            
            CreateEvent(new LaunchEvent(String.format("%s reached level %d.", player.GetName(), player.GetRank())));
        }
    }
    
    public void PlayerRankDown(Player player)
    {
        if(player.GetRank() != 0)
        {
            player.RankDown();
            CreateReport(player, new LaunchReport(String.format("[ECONOMY] You were demoted to rank %d. Your hourly income has decreased by $%d.", player.GetRank(), Defs.INCOME_PER_RANK), true));
            CreateEvent(new LaunchEvent(String.format("%s dropped to level %d.", player.GetName(), player.GetRank())));
        }
    }
    
    public void SetPlayerUnderAttack(Player player)
    {
        if(!GetPlayerOnline(player) && player.Functioning())
        {
            User user = player.GetUser();
            
            if(user != null)
                user.SetUnderAttack();
        }
    }
    
    public void SendAlert(int lPlayerID)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            User user = player.GetUser();
            
            if(user != null && user.GetToken() != null && !config.DebugMode())
            {
                try
                {                    
                    Message message = Message.builder()
                            .putData("title", "Hostile missile detected!")
                            .putData("body", "Missiles inbound!")
                            .setToken(user.GetToken())
                            .build();

                    FirebaseMessaging.getInstance().send(message);

                    LaunchLog.ConsoleMessage(String.format("Sending alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                }
                catch(FirebaseMessagingException ex)
                {
                    LaunchLog.ConsoleMessage(String.format("Could not send alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                    TokenUpdate(user);
                }
            }
            else
            {
                LaunchLog.ConsoleMessage(String.format("User does not exist or firebase token is null.", GetPlayer(user.GetPlayerID()).GetName()));
            }
        }
    }
    
    @Override
    public boolean SetToken(int lPlayerID, String strToken)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            User user = player.GetUser();
            
            if(user != null && strToken != null)
            {
                user.SetToken(strToken);
            }
        }
        
        return true;
    }
    
    public void TokenUpdate(User user)
    {
        comms.UpdateToken(user);
        LaunchLog.ConsoleMessage("Updating firebase token...");
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Aircraft-related methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public boolean RefuelAircraft(int lPlayerID, int lTankerID, int lRefueleeID)
    {
        LaunchLog.ConsoleMessage("Player is attempting to refuel aircraft...");
        Player player = GetPlayer(lPlayerID);
        Airplane tanker = GetAirplane(lTankerID);
        Airplane refuelee = GetAirplane(lRefueleeID);
        
        if(player != null && tanker != null && refuelee != null)
        {
            LaunchLog.ConsoleMessage("Stuff is not null and entity is friendly.");
            
            if(tanker.CanTransferFuel() && tanker.GetCurrentFuel() > Defs.AIRCRAFT_FUEL_TRANSFER_PER_TICK)
            {
                LaunchLog.ConsoleMessage("Aircraft conditions are right, setting refueling...");
                
                CreateEventForPlayer(new LaunchEvent(String.format("Aircraft merging for refuel."), SoundEffect.AIRCRAFT_MOVE), lPlayerID);
                tanker.ProvideRefueling(new EntityPointer(lRefueleeID, refuelee.GetEntityType()));
                refuelee.SeekRefueling(new EntityPointer(lTankerID, tanker.GetEntityType()));
                EntityUpdated(tanker, false);
                EntityUpdated(refuelee, false);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean RefuelAircraftAtBase(int lPlayerID, EntityPointer pointerAirbase, EntityPointer pointerAircraft)
    {
        return false;
    }
    
    @Override
    public boolean ToggleAircraftReturn(int lPlayerID, EntityPointer pointerAircraft)
    {
        Player player = GetPlayer(lPlayerID);
        LaunchEntity entity = pointerAircraft.GetEntity(game);
        
        if(entity != null && player != null && player.Functioning() && entity.GetOwnerID() == lPlayerID)
        {
            if(entity instanceof AirplaneInterface)
            {
                ((AirplaneInterface)entity).ToggleAutoReturn();
                return true;
            } 
        }
        
        return false;
    }
    
    public void LandAircraft(EntityPointer pointerAircraft, AircraftSystem system)
    {
        LaunchEntity entity = pointerAircraft.GetEntity(game);
        
        if(entity instanceof Airplane)
        {
            Airplane aircraft = (Airplane)entity;
            Player owner = GetOwner(aircraft);
            
            if(owner != null)
            {
                StoredAirplane storedAircraft = new StoredAirplane(this, GetAtomicID(lAirplaneIndex, Airplanes), aircraft);
                
                int lTimeAirborne = aircraft.GetTimeAirborne();
                int lHourlyMaintenance = Defs.GetAircraftMaintenanceCost(aircraft.GetEntityType());
                int lMaintenance = (lTimeAirborne/Defs.MS_PER_HOUR) * lHourlyMaintenance;
                long oAmountCharged = Math.min(lHourlyMaintenance, owner.GetWealth());

                owner.ChargeWealth(oAmountCharged);
                CreateReport(owner, new LaunchReport(String.format("[ECONOMY] Your aircraft landed. You were charged $%d for maintenance.", oAmountCharged), false, owner.GetID()));
                CreateEventForPlayer(new LaunchEvent(String.format("Your aircraft landed. You were charged $%d for maintenance.", oAmountCharged), SoundEffect.MONEY), owner.GetID());
                
                AddStoredAircraft(storedAircraft);
                Airplanes.remove(aircraft.GetID());
                EntityRemoved(aircraft, false);
            }
        }
    }
    
    public void LoadInfantry(Infantry infantry, LaunchEntity transport)
    {
        StoredInfantry storedInfantry = new StoredInfantry(GetAtomicID(lInfantryIndex, Infantries), infantry, transport.GetPointer());
        
        AddStoredInfantry(storedInfantry);
        Infantries.remove(infantry.GetID());
        EntityRemoved(infantry, false);
    }
    
    public void LoadCargoTruck(CargoTruck truck, LaunchEntity transport)
    {
        StoredCargoTruck storedCargoTruck = new StoredCargoTruck(GetAtomicID(lCargoTruckIndex, CargoTrucks), truck, transport.GetPointer());
        
        AddStoredCargoTruck(storedCargoTruck);
        CargoTrucks.remove(truck.GetID());
        EntityUpdated(transport, false);
        EntityRemoved(truck, false);
    }
    
    public void LoadTank(Tank tank, LaunchEntity transport)
    {
        StoredTank storedTank = new StoredTank(GetAtomicID(lTankIndex, Tanks), tank, transport.GetPointer());
        
        if(transport instanceof HaulerInterface hauler)
        {
            CargoSystem system = hauler.GetCargoSystem();
            
            if(system.WeightCanFit(storedTank.GetWeight()))
            {
                AddStoredTank(storedTank);
                Tanks.remove(tank.GetID());
                EntityRemoved(tank, false);
            }
        }      
    }
    
    public Airplane AircraftTakeoff(int lStoredAircraftID, GeoCoord geoTarget, Map<Integer, GeoCoord> Coordinates)
    {
        StoredAirplane storedAircraft = GetStoredAirplane(lStoredAircraftID);
        
        if(storedAircraft != null)
        {
            EntityPointer pointerHome = storedAircraft.GetHomeBase();
            MapEntity mapHome = null;
            
            if(pointerHome != null)
            {
                mapHome = pointerHome.GetMapEntity(this);
            }
            
            Airplane flyingAircraft = new Airplane(GetAtomicID(lAirplaneIndex, Airplanes), storedAircraft, mapHome, geoTarget, Coordinates);
            RemoveStoredAircraft(storedAircraft);
            AddAircraft(flyingAircraft);

            return flyingAircraft;
        }
           
        return null;
    }
    
    public Infantry InfantryDisembark(int lStoredInfantryID)
    {
        StoredInfantry storedInfantry = GetStoredInfantry(lStoredInfantryID);
        
        if(storedInfantry != null)
        {
            MapEntity mapHost;
            LaunchEntity entityHost = storedInfantry.GetHost().GetEntity(this);
            
            if(entityHost instanceof StoredAirplane)
            {
                mapHost = ((StoredAirplane)entityHost).GetHomeBase().GetMapEntity(this);
            }
            else
            {
                mapHost = storedInfantry.GetHost().GetMapEntity(this);
            }
            
            if(mapHost != null && LoadUnitBoardLegal(null, storedInfantry.GetHost(), storedInfantry.GetPointer()))
            {
                GeoCoord geoDeploy = mapHost.GetPosition().GetCopy();
                
                if(geoDeploy != null)
                {
                    geoDeploy.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * Defs.GROUND_UNIT_DROP_VARIANCE);
                    Infantry infantry = new Infantry(GetAtomicID(lInfantryIndex, Infantries), storedInfantry, geoDeploy);
                    RemoveStoredInfantry(storedInfantry);
                    AddInfantry(infantry);
                    EntityUpdated(mapHost, false);
                    CreateEventForPlayer(new LaunchEvent(String.format("Infantry unloaded."), SoundEffect.CARGO_TRANSFER), mapHost.GetOwnerID());

                    return infantry;
                }  
            }
        }
        
        return null;
    }
    
    public CargoTruck CargoTruckDisembark(int lStoredCargoTruckID)
    {
        StoredCargoTruck storedCargoTruck = GetStoredCargoTruck(lStoredCargoTruckID);
        
        if(storedCargoTruck != null)
        {
            MapEntity mapHost = null;
            LaunchEntity entityHost = storedCargoTruck.GetHost().GetEntity(this);
            
            //Trucks can only unload from stored aircraft at airbases or from shipyards.
            if(entityHost instanceof StoredAirplane)
            {
                mapHost = ((StoredAirplane)entityHost).GetHomeBase().GetMapEntity(this);
            }
            else
            {
                mapHost = storedCargoTruck.GetHost().GetMapEntity(this);
            }
            
            if(mapHost != null && LoadUnitBoardLegal(null, storedCargoTruck.GetHost(), storedCargoTruck.GetPointer()))
            {
                GeoCoord geoDeploy = mapHost.GetPosition().GetCopy();
                
                if(geoDeploy != null)
                {
                    geoDeploy.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * Defs.GROUND_UNIT_DROP_VARIANCE);
                    CargoTruck truck = new CargoTruck(GetAtomicID(lCargoTruckIndex, CargoTrucks), storedCargoTruck, geoDeploy);
                    RemoveStoredCargoTruck(storedCargoTruck);
                    AddCargoTruck(truck);
                    EntityUpdated(mapHost, false);
                    EntityUpdated(entityHost, false);
                    CreateEventForPlayer(new LaunchEvent(String.format("Cargo truck unloaded."), SoundEffect.CARGO_TRANSFER), mapHost.GetOwnerID());

                    return truck;
                }
            }
        }
        
        return null;
    }
    
    public Tank TankDisembark(int lStoredTankID)
    {
        StoredTank storedTank = GetStoredTank(lStoredTankID);
        
        if(storedTank != null)
        {
            MapEntity mapHost = null;
            LaunchEntity entityHost = storedTank.GetHost().GetEntity(this);
            
            //Tanks can only unload from stored aircraft at airbases or from shipyards.
            if(entityHost instanceof StoredAirplane)
            {
                mapHost = ((StoredAirplane)entityHost).GetHomeBase().GetMapEntity(this);
            }
            else
            {
                mapHost = storedTank.GetHost().GetMapEntity(this);
            }
            
            if(mapHost != null && LoadUnitBoardLegal(null, storedTank.GetHost(), storedTank.GetPointer()))
            {
                GeoCoord geoDeploy = mapHost.GetPosition().GetCopy();
                
                if(geoDeploy != null)
                {
                    geoDeploy.Move(random.nextDouble() * (2.0 * Math.PI), Defs.GROUND_UNIT_DROP_VARIANCE);
                    Tank tank = new Tank(GetAtomicID(lTankIndex, Tanks), storedTank, geoDeploy);
                    RemoveStoredTank(storedTank);
                    AddTank(tank);
                    EntityUpdated(mapHost, false);
                    CreateEventForPlayer(new LaunchEvent(String.format("Tank unloaded."), SoundEffect.CARGO_TRANSFER), mapHost.GetOwnerID());

                    return tank;
                }  
            }
        }
        
        return null;
    }
    
    @Override
    public void AirplaneDestroyed(AirplaneInterface aircraft)
    {
        aircraft.InflictDamage(aircraft.GetHP());
        Airplanes.remove(aircraft.GetID());
        EntityRemoved(aircraft.GetAirplane(), false);
        
        if(aircraft instanceof Airplane && aircraft.HasCargo())
            CargoSystemDestroyed(aircraft.GetOwnerID(), (Airplane)aircraft, ((Airplane)aircraft).GetPosition(), aircraft.GetCargoSystem(), null);
    }
    
    @Override
    public void EntityMoved(MapEntity entity)
    {
        if(entity instanceof Movable || entity instanceof Missile)
            quadtree.UpdateEntityCell(entity.GetPointer());
    }
    
    @Override
    public void AirbaseDestroyed(Airbase airbase)
    {
        for(StoredAirplane storedAircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
        {
            Airplanes.remove(storedAircraft.GetID());
            EntityRemoved(storedAircraft, false);
            
            if(storedAircraft.HasCargo())
                CargoSystemDestroyed(storedAircraft.GetOwnerID(), storedAircraft, airbase.GetPosition(), storedAircraft.GetCargoSystem(), null);
        }
    }
    
    public void AttemptToFindNewHome(Airplane aircraft)
    {
        if(aircraft != null)
        {
            Player aircraftOwner = GetPlayer(aircraft.GetOwnerID());

            for(Airbase airbase : GetAirbases())
            {
                if(airbase.GetOwnedBy(aircraftOwner.GetID()) || airbase.GetAircraftSystem().GetOpen())
                {
                    if(!airbase.GetAircraftSystem().Full())
                    {
                        aircraft.SetHomeBase(airbase.GetPointer());
                        break;
                    }
                }
            }
        } 
    }
    
    public boolean AircraftIsHostile(Airplane aircraft, Player player)
    { 
        Player aircraftOwner = GetOwner(aircraft);
        
        if(aircraftOwner != null)
        {
            if(EntityIsFriendly(aircraftOwner, player))
                return false;
            
            if(player.Blacklisted(aircraft.GetOwnerID()) || GetAllegiance(aircraftOwner, player) == Allegiance.ENEMY)
                return true;
        } 

        return false;
    }
    
    public void EstablishHostilePlayer(Torpedo torpedo)
    {
        if(torpedo != null)
        {
            Player owner = GetOwner(torpedo);

            if(owner != null)
            {
                for(int lPlayerID : torpedo.GetStructureThreatenedPlayers())
                {
                    Player target = Players.get(lPlayerID);

                    if(target != null && owner.GetID() != target.GetID() && GetAllegiance(owner, target) == Allegiance.NEUTRAL)
                    {
                        target.AddHostilePlayer(owner.GetID());
                    }
                }
            }
        }   
    }
    
    public boolean PlayerThreatensPlayer(Player player, Player potentialHostile)
    {
        if(player.GetID() != potentialHostile.GetID())
        {
            if(potentialHostile.HasFlyingMissiles())
            {
                for(int missileID : new ArrayList<>(potentialHostile.GetMissiles()))
                {
                    Missile missile = Missiles.get(missileID);

                    if(missile != null)
                    {
                        if(missile.ThreatensPlayersStructures(player.GetID()))
                            return true;
                    }
                    else
                    {
                        potentialHostile.RemoveMissile(missile.GetID());
                    }
                }
            }
        }  
        
        return false;
    }
    
    private boolean SellAircraft(int lPlayerID, StoredAirplane aircraft)
    {
        if(aircraft != null)
        {
            Player player = Players.get(lPlayerID);
            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to sell a %s...", player.GetName(), aircraft.GetTypeName()));

            if(aircraft.GetOwnedBy(lPlayerID) && !InBattle(player))
            {
                ProcessPlayerIncome(player, String.format("sale of %s", aircraft.GetTypeName()), GetSaleValue(GetAircraftValue(aircraft)), false);
                RemoveStoredAircraft(aircraft);
                CreateEventForPlayer(new LaunchEvent(String.format("Aircraft sold."), SoundEffect.MONEY), lPlayerID);

                return true;
            } 
        }
        
        return false;
    }
    
    @Override
    public boolean ChangeAircraftHomebase(int lPlayerID, EntityPointer pointerEntity, EntityPointer pointerHomebase)
    {
        LaunchEntity entity = pointerEntity.GetEntity(this);
        Player owner = Players.get(lPlayerID);
        
        if(owner != null && entity != null)
        {
            MapEntity mapHomebase = pointerHomebase.GetMapEntity(this);
            
            if(mapHomebase != null)
            {
                if(entity instanceof Airplane)
                {
                    Airplane aircraft = (Airplane)entity;
                    
                    if(mapHomebase instanceof Airbase)
                    {
                        Airbase airbase = (Airbase)mapHomebase;

                        Player airbaseOwner = Players.get(airbase.GetOwnerID());

                        if(airbaseOwner != null)
                        {
                            if(aircraft.GetOwnedBy(lPlayerID) && (airbase.GetOwnerID() == aircraft.GetOwnerID() || airbase.GetAircraftSystem().GetOpen()))
                            {
                                if(!airbase.GetAircraftSystem().Full())
                                {
                                    aircraft.SetHomeBase(airbase.GetPointer());
                                    CreateReport(airbaseOwner, new LaunchReport(String.format("%s transferred a %s to your airbase.", owner.GetName(), aircraft.GetTypeName()), false, airbaseOwner.GetID(), owner.GetID()));
                                    CreateEventForPlayer(new LaunchEvent(String.format("Aircraft homebase changed."), SoundEffect.AIRCRAFT_MOVE), lPlayerID);
                                    return true;
                                }
                            }
                        }
                    }
                    else if(mapHomebase instanceof Ship)
                    {
                        Ship ship = (Ship)mapHomebase;

                        Player airbaseOwner = Players.get(ship.GetOwnerID());

                        if(airbaseOwner != null && ship.HasAircraft() && aircraft.GetCarrierCompliant())
                        {
                            if(aircraft.GetOwnedBy(lPlayerID) && (ship.GetOwnerID() == aircraft.GetOwnerID() || ship.GetAircraftSystem().GetOpen()))
                            {
                                if(!ship.GetAircraftSystem().Full())
                                {
                                    aircraft.SetHomeBase(ship.GetPointer());
                                    CreateReport(airbaseOwner, new LaunchReport(String.format("%s transferred a %s to your airbase.", owner.GetName(), aircraft.GetTypeName()), false, airbaseOwner.GetID(), owner.GetID()));
                                    CreateEventForPlayer(new LaunchEvent(String.format("Aircraft homebase changed."), SoundEffect.AIRCRAFT_MOVE), lPlayerID);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean AircraftThreatChecked(AirplaneInterface aircraft, Player player)
    {
        if(!Interceptors.isEmpty())
        {
            try
            {
                for(Interceptor interceptor : GetInterceptors())
                {
                    if(interceptor != null)
                    {
                        if(interceptor.GetTargetType() == ((LaunchEntity)aircraft).GetEntityType())
                        {
                            if(interceptor.GetTargetID() == aircraft.GetID())
                            {
                                return true;
                            }
                        }
                    }
                }
            }
            catch(Exception ex)
            {
                //Poop
            }
        }
        
        return false;
    }
    
    public void LaunchDebugMissiles(Player player, int lMissileCount)
    {
        LaunchLog.ConsoleMessage(String.format("Launching %d random missiles from random players to random players...", lMissileCount));

        Collection<MissileType> Types = new ArrayList<>();
        
        for(MissileType type : new ArrayList<>(config.GetMissileTypes()))
        {
            if(type.GetICBM() || type.GetBomb() || type.GetStealth())
                continue;
            
            Types.add(type);
        }
        
        GeoCoord geoLaunch = player.GetPosition().GetCopy();
        GeoCoord geoTarget = player.GetPosition().GetCopy();
        geoTarget.Move(random.nextDouble() * (2.0 * Math.PI), 2000);
            
        for(int i = 0; i < lMissileCount; i++)
        {
            MissileType type = (MissileType)Types.toArray()[random.nextInt(0, Types.toArray().length)];
            
            if(type != null)
            {
                LaunchLog.ConsoleMessage("Launching missile...");
                
                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating a missile launch.");

                Missile missile = new Missile(GetAtomicID(lMissileIndex, Missiles), geoLaunch.GetCopy(), type.GetID(), player.GetID(), type.GetMissileSpeed(), geoTarget, null, geoLaunch.GetCopy(), false);

                if(type.GetICBM())
                    missile.SetVisible(Integer.MAX_VALUE);
                else if(type.GetStealth())
                    missile.SetVisible(Integer.MAX_VALUE);
                else
                    missile.SetVisible(Integer.MAX_VALUE);

                AddMissile(missile);
            }
        }
    }
    
    public void LaunchDebugAttack(Player target, int lMissileCount)
    {
        LaunchLog.ConsoleMessage(String.format("Launching %d random missiles from random players to random players...", lMissileCount));

        Collection<MissileType> Types = new ArrayList<>();
        
        for(MissileType type : new ArrayList<>(config.GetMissileTypes()))
        {
            if(type.GetICBM() || type.GetBomb() || type.GetStealth() || type.GetSonobuoy())
                continue;
            
            Types.add(type);
        }
        
        GeoCoord geoLaunch = new GeoCoord(0.0f, 0.0f);
        geoLaunch.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 40096);
        
        GeoCoord geoTarget = new GeoCoord(0.0f, 0.0f);
        
        if(!target.GetStructures().isEmpty())
        {
            for(Structure structure : target.GetStructures())
            {
                geoTarget = structure.GetPosition();
            }
        }
        else
        {
            LaunchLog.ConsoleMessage("Aborting. Target has no structures.");
            return;
        }
        
        MissileType type = (MissileType)Types.toArray()[random.nextInt(0, Types.toArray().length)];
        
        if(type != null)
        {
            for(int i = 0; i < lMissileCount; i++)
            {
                LaunchLog.ConsoleMessage("Launching missile...");

                LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, "Creating a missile launch.");

                Missile missile = new Missile(GetAtomicID(lMissileIndex, Missiles), geoLaunch.GetCopy(), type.GetID(), LaunchEntity.ID_NONE, type.GetMissileSpeed(), geoTarget, null, geoLaunch.GetCopy(), false);

                if(type.GetICBM())
                    missile.SetVisible(Integer.MAX_VALUE);
                else if(type.GetStealth())
                    missile.SetVisible(Integer.MAX_VALUE);
                else
                    missile.SetVisible(Integer.MAX_VALUE);

                AddMissile(missile);

                geoLaunch.Move(random.nextDouble() * (2.0 * Math.PI), random.nextFloat() * 1.0);
            }
        } 
    }
    
    public boolean RefuelNavalVessel(int lPlayerID, EntityPointer pointerRefueler, EntityPointer pointerRefuelee)
    {
        LaunchLog.ConsoleMessage("Player is attempting to refuel a ship/submarine...");
        Player player = GetPlayer(lPlayerID);
        NavalVessel refueler = (NavalVessel)pointerRefueler.GetMapEntity(game);
        NavalVessel refuelee = (NavalVessel)pointerRefuelee.GetMapEntity(game);
        
        /**
         * If the refueler is non-nuclear or is a ship with cargo, proceed. (Maybe that should be checked in CommandIsLegal?)
         * The refuelee must be non-nuclear. There is no reason for it to be part of this process otherwise.
         * If the refueler or refuelee is a submerged submarine, don't worry about it, we'll make them surface before refueling in the movement logic.
         * If the 
         */
        
        if(player != null && refueler != null && refuelee != null)
        {
            if(!refuelee.GetNuclear())
            {
                if(refueler.GetCurrentFuel() > Defs.SHIP_REFUEL_RATE_PER_TICK_TONS)
                {
                    LaunchLog.ConsoleMessage("Naval vessel conditions are right, setting refueling...");

                    CreateEventForPlayer(new LaunchEvent(String.format("Vessels merging for refuel."), SoundEffect.NAVAL_MOVE), lPlayerID);
                    refueler.ProvideRefueling(pointerRefuelee);
                    refuelee.SeekRefueling(pointerRefueler);
                    EntityUpdated(refueler, false);
                    EntityUpdated(refuelee, false);
                    return true;
                }
                else if(refueler instanceof Ship && ((Ship)refueler).HasCargo() && ((Ship)refueler).GetCargoSystem().ContainsResourceType(ResourceType.OIL))
                {
                    LaunchLog.ConsoleMessage("Naval vessel conditions are right, setting refueling...");

                    CreateEventForPlayer(new LaunchEvent(String.format("Vessels merging for refuel."), SoundEffect.NAVAL_MOVE), lPlayerID);
                    refueler.ProvideRefueling(pointerRefuelee);
                    refuelee.SeekRefueling(pointerRefueler);
                    EntityUpdated(refueler, false);
                    EntityUpdated(refuelee, false);
                    return true;
                }
            }  
        }
        
        return false;
    }
    
    @Override
    public void ShipDestroyed(Ship ship)
    {
        if(ship.HasAircraft())
        {
            for(StoredAirplane storedAircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
            {
                Airplanes.remove(storedAircraft.GetID());
                EntityRemoved(storedAircraft, false);

                if(storedAircraft.HasCargo())
                    CargoSystemDestroyed(ship.GetOwnerID(), ship, ship.GetPosition(), storedAircraft.GetCargoSystem(), null);
            }
        }
        
        if(ship.HasCargo())
            CargoSystemDestroyed(ship.GetOwnerID(), ship, ship.GetPosition().GetCopy(), ship.GetCargoSystem(), null);
    }
    
    @Override
    public boolean SendMessage(int lPlayerID, int lReceiverID, ChatChannel channel, String strMessage)
    {
        Player sender = Players.get(lPlayerID);
        String strCleanedMessage = LaunchUtilities.SanitiseText(strMessage, true, true);
        
        if(sender != null && !sender.Muted() && !strMessage.isEmpty())
        {
            switch(channel)
            {
                case PRIVATE:
                {
                    Player receiver = Players.get(lReceiverID);

                    if(receiver != null)
                    {
                        SendUserAlert(receiver.GetUser(), String.format("Private Message from %s", sender.GetName()), strCleanedMessage, false, true);
                        CreateEventForPlayer(new LaunchEvent(String.format(Defs.MESSAGE_PREFIX_PRIVATE + " %s: %s", sender.GetName(), strCleanedMessage), SoundEffect.TRANSMIT), lReceiverID);
                        CreateEventForPlayer(new LaunchEvent(String.format("Sending to %s: %s", receiver.GetName(), strCleanedMessage), SoundEffect.TRANSMIT), sender.GetID());
                        CreateReport(receiver, new LaunchReport(String.format(Defs.MESSAGE_PREFIX_PRIVATE + " %s: %s", sender.GetName(), strCleanedMessage), true, lPlayerID));
                        return true;
                    }
                }
                break;

                case ALLIANCE:
                {
                    List<Player> Receivers = GetPlayerAlliesAndSelf(sender);

                    for(Player receiver : Receivers)
                    {
                        if(receiver != sender)
                        {
                            SendUserAlert(receiver.GetUser(), String.format("Alliance Message from %s", sender.GetName()), strCleanedMessage, false, true);
                        }
                        
                        CreateEventForPlayer(new LaunchEvent(String.format(Defs.MESSAGE_PREFIX_ALLIANCE + " %s: %s", sender.GetName(), strCleanedMessage), SoundEffect.TRANSMIT), lReceiverID);
                        CreateReport(receiver, new LaunchReport(String.format(Defs.MESSAGE_PREFIX_ALLIANCE + " %s: %s", sender.GetName(), strCleanedMessage), true, lPlayerID));
                    }
                    
                    CreateEventForPlayer(new LaunchEvent(String.format("Sending to alliance: %s", strCleanedMessage), SoundEffect.TRANSMIT), sender.GetID());
                    
                    return true;
                }
                
                case GLOBAL:
                {
                    CreateEvent(new LaunchEvent(String.format(Defs.MESSAGE_PREFIX_GLOBAL + " %s: %s", sender.GetName(), strCleanedMessage), SoundEffect.TRANSMIT));
                    CreateReport(new LaunchReport(String.format(Defs.MESSAGE_PREFIX_GLOBAL + " %s: %s", sender.GetName(), strCleanedMessage), false, lPlayerID));
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public Quadtree GetQuadtree()
    {
        return quadtree;
    }
    
    @Override
    public void SendUserAlert(User user, String strTitle, String strBody, boolean bRespectInterval, boolean bSendWhileOnline)
    {
        if(!Defs.DEBUG_MODE && (!bRespectInterval || (bRespectInterval && user.CanReceiveAlerts())))
        {
            if(bSendWhileOnline || !GetPlayerOnline(GetPlayer(user.GetPlayerID())))
            {
                try
                {
                    user.ClearAlarms();

                    Message message = Message.builder()
                            .putData("title", strTitle)
                            .putData("body", strBody)
                            .setToken(user.GetToken())
                            .build();
                    
                    FirebaseMessaging.getInstance().send(message);

                    if(bRespectInterval)
                        user.SetAlertInterval();

                    LaunchLog.ConsoleMessage(String.format("Sending alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                }
                catch(FirebaseMessagingException ex)
                {
                    LaunchLog.ConsoleMessage(String.format("Could not send alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                    TokenUpdate(user);
                }
                catch(IllegalArgumentException ex)
                {
                    LaunchLog.ConsoleMessage(String.format("Could not send alert to %s.", GetPlayer(user.GetPlayerID()).GetName()));
                    TokenUpdate(user);
                }
            }  
        }
    }
    
    /**
     * 
     * @param payee The player receiving the stuff.
     * @param Incomes Map of resources.
     * @param bTaxable boolean indicating whether or not any wealth in Incomes should be taxed. Only wealth is taxed.
     */
    public void ProcessPlayerIncome(Player payee, String strSource, Map<ResourceType, Long> Incomes, boolean bTaxable)
    {
        for(Entry<ResourceType, Long> income : Incomes.entrySet())
        {
            if(income.getKey() == ResourceType.WEALTH && payee.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && bTaxable)
            {
                Alliance alliance = Alliances.get(payee.GetAllianceMemberID());
                long oAmount = income.getValue();
                long oTax = (int)(oAmount * alliance.GetTaxRate());
                long oNet = oAmount - oTax;

                alliance.AddWealth((int)oTax);
                payee.AddWealth((int)oNet);

                
                CreateReport(payee, new LaunchReport(String.format("[ECONOMY] Gross Income: $%d, Alliance Tax: $%d, Net Income: $%d", oAmount, oTax, oNet), false));
            }
            else
            {
                payee.AddWealth(income.getValue());
            }
        }
        
        String strIncomeStatement = String.format("[ECONOMY] Income from %s: %s", strSource, LaunchUtilities.GetCostStatement(Incomes));
        
        if(!strSource.isEmpty())
            CreateReport(payee, new LaunchReport(strIncomeStatement, false));
    }
    
    @Override
    public boolean SetAllianceTaxRate(int lPlayerID, float fltTaxRate)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning() && player.GetIsAnMP() && player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            
            if(alliance != null)
            {
                if(fltTaxRate >= Defs.MAX_TAX_RATE)
                {
                    alliance.SetTaxRate(Defs.MAX_TAX_RATE);
                    SendMessage(lPlayerID, LaunchEntity.ID_NONE, ChatChannel.ALLIANCE, String.format("Alliance tax rate changed to %.0f%%", Defs.MAX_TAX_RATE));
                    CreateEventForPlayer(new LaunchEvent(String.format("Tax rate change confirmed."), SoundEffect.MONEY), lPlayerID);
                    return true;
                }
                else if(fltTaxRate <= Defs.MIN_TAX_RATE)
                {
                    alliance.SetTaxRate(Defs.MIN_TAX_RATE);
                    SendMessage(lPlayerID, LaunchEntity.ID_NONE, ChatChannel.ALLIANCE, String.format("Alliance tax rate changed to %.0f%%", Defs.MIN_TAX_RATE));
                    CreateEventForPlayer(new LaunchEvent(String.format("Tax rate change confirmed."), SoundEffect.MONEY), lPlayerID);
                    return true;
                }
                else
                {
                    alliance.SetTaxRate(fltTaxRate);
                    SendMessage(lPlayerID, LaunchEntity.ID_NONE, ChatChannel.ALLIANCE, String.format("Alliance tax rate changed to %.0f%%", fltTaxRate));
                    CreateEventForPlayer(new LaunchEvent(String.format("Tax rate change confirmed."), SoundEffect.MONEY), lPlayerID);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean AllianceWithdraw(int lPlayerID, int lAmount)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null && player.Functioning() && player.GetIsAnMP() && player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && !AtWar(player))
        {
            Alliance alliance = GetAlliance(player.GetAllianceMemberID());
            
            if(alliance != null && !AllianceInBattle(alliance))
            {
                if(alliance.SubtractWealth(lAmount))
                {
                    player.AddWealth(lAmount);
                    CreateEventForPlayer(new LaunchEvent(String.format("$%d withdrawn.", lAmount), SoundEffect.MONEY), lPlayerID);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean AlliancePanic(int lPlayerID, int lAllianceID)
    {
        Player player = GetPlayer(lPlayerID);
        Alliance alliance = GetAlliance(lAllianceID);
        
        if(player != null && alliance != null)
        {
            if(player.GetIsAnMP() && player.GetAllianceMemberID() == lAllianceID && player.Functioning())
            {
                List<Player> Receivers = GetPlayerAlliesAndSelf(player);

                for(Player receiver : Receivers)
                {
                    if(receiver != player)
                    {
                        SendUserAlert(receiver.GetUser(), String.format("ALERT from %s", player.GetName()), "ALLIANCE UNDER ATTACK!", false, true);
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    public int CountAllianceICBMs(Alliance alliance)
    {
        int lICBMCount = 0;
        
        for(Player player : GetMembers(alliance))
        {
            for(MissileSite site : GetMissileSites())
            {
                if(site.GetOwnedBy(player.GetID()) && site.CanTakeICBM())
                {
                    lICBMCount += site.GetMissileSystem().GetOccupiedSlotCount();
                }
            }
            
            for(Submarine submarine : GetSubmarines())
            {
                if(submarine.GetOwnedBy(player.GetID()) && submarine.HasICBMs())
                {
                    lICBMCount += submarine.GetICBMSystem().GetOccupiedSlotCount();
                }
            }
        }
        
        return lICBMCount;
    }
    
    public int CountAllianceABMs(Alliance alliance)
    {
        int lABMCount = 0;
        
        for(Player player : GetMembers(alliance))
        {
            for(Structure structure : player.GetStructures())
            {
                if(structure instanceof SAMSite)
                {
                    SAMSite site = ((SAMSite)structure);
                    
                    if(site.GetIsABMSilo())
                    {
                        lABMCount += site.GetInterceptorSystem().GetOccupiedSlotCount();
                    }
                }
            }
        }
        
        return lABMCount;
    }
    
    public short GetInfantryCombatDamage(Infantry attacker, LandUnit defender)
    {
        short nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.INEFFECTIVE_MIN_DMG, Defs.INEFFECTIVE_MAX_DMG);
        
        if(defender instanceof Infantry)
        {
            nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.AVERAGE_MIN_DMG, Defs.AVERAGE_MAX_DMG);

            //Attack bonus for stationary infantry if they are attacking moving infantry.
            if(attacker.GetStationary() && defender.Moving())
            {
                nDamage *= Defs.INFANTRY_DEFENSE_BONUS;
            }
        }
        else if(defender instanceof CargoTruck)
        {
            nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG);
        }
       
        return (short)Math.max(0, nDamage);
    }
    
    public short GetTankCombatDamage(Tank attacker, LandUnit defender)
    {
        short nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.INEFFECTIVE_MIN_DMG, Defs.INEFFECTIVE_MAX_DMG);
        
        if(defender instanceof Infantry)
        {
            nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG);

            //Attack bonus for stationary infantry if they are attacking moving infantry.
            if(attacker.GetStationary() && defender.Moving())
            {
                nDamage *= Defs.INFANTRY_DEFENSE_BONUS;
            }
        }
        else if(defender instanceof CargoTruck)
        {
            nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.EFFECTIVE_MIN_DMG, Defs.EFFECTIVE_MAX_DMG);
        }
        else if(defender instanceof Tank)
        {
            nDamage = (short)LaunchUtilities.GetRandomIntInBounds(Defs.AVERAGE_MIN_DMG, Defs.AVERAGE_MAX_DMG);
        }
       
        return (short)Math.max(0, nDamage);
    }
    
    public short GetInfantryCombatDamage(Infantry attacker, Player defender)
    {
        return (short)LaunchUtilities.GetRandomIntInBounds(Defs.INFANTRY_MIN_DAMAGE, Defs.INFANTRY_MAX_DAMAGE);
    }
    
    private void ProcessPlayerXPGain(int lPlayerID, int lXPGain, String strReason)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            player.AddExperience(lXPGain);
            CreateReport(player, new LaunchReport(String.format("[ECONOMY] %s You gained %d xp.", strReason, lXPGain), true, player.GetID()));
        }
    }
    
    private void ProcessPlayerXPLoss(int lPlayerID, int lXPLoss, String strReason)
    {
        Player player = GetPlayer(lPlayerID);
        
        if(player != null)
        {
            player.SubtractExperience(lXPLoss);
            CreateReport(player, new LaunchReport(String.format("[ECONOMY] %s You lost %d xp.", strReason, lXPLoss), true, player.GetID()));
        }
    }
    
    public void GenerateShipyard(String strName, GeoCoord geoPosition, GeoCoord geoOutput, boolean bPortOnly)
    {
        Shipyard shipyard = new Shipyard(GetAtomicID(lShipyardIndex, Shipyards), geoPosition, strName, geoOutput, Defs.SHIPYARD_MAX_HP, Defs.SHIPYARD_MAX_HP);
        AddShipyard(shipyard);
    }
    
    public void ScatterLoot(GeoCoord geoPosition, CargoSystem system)
    {
        if(system != null)
        {
            for(Entry<ResourceType, Long> entry : system.GetResourceMap().entrySet())
            {
                if(random.nextFloat() <= Defs.LOOT_DROP_CHANCE)
                {
                    long oAmountDropped = (int)(entry.getValue() * random.nextFloat(Defs.LOOT_DROP_MIN, Defs.LOOT_DROP_MAX));

                    system.ChargeQuantity(entry.getKey(), oAmountDropped);

                    GeoCoord geoLoot = geoPosition.GetCopy();
                    geoLoot.Move(random.nextDouble() * (2.0 * Math.PI), config.GetOreCollectRadius());
                    CreateLoot(geoLoot, LootType.RESOURCES, entry.getKey().ordinal(), oAmountDropped, Defs.LOOT_EXPIRY);
                }
            }
        }
    }
    
    /**
     * Processes the destruction of a cargo-containing entity, such as dropping loot on the ground.
     * @param geoPosition the location of the entity.
     * @param cargo The cargo system that was destroyed, if there was one.
     * @param resources The resource system that was destroyed, if there was one.
     */
    public void CargoSystemDestroyed(int lOwnerID, LaunchEntity host, GeoCoord geoPosition, CargoSystem cargo, ResourceSystem resources)
    {        
        if(resources != null)
        {
            for(Entry<ResourceType, Long> entry : resources.GetTypes().entrySet())
            {
                long oAmountDropped = entry.getValue();

                if(host instanceof Player)
                {
                    if(entry.getKey() == ResourceType.WEALTH)
                        oAmountDropped = random.nextLong(oAmountDropped/2, oAmountDropped);
                    else
                        oAmountDropped = random.nextLong(0, oAmountDropped);

                    resources.ChargeQuantity(entry.getKey(), oAmountDropped);
                }

                GeoCoord geoLoot = geoPosition.GetCopy();
                geoLoot.Move(random.nextDouble() * (2.0 * Math.PI), config.GetOreCollectRadius());
                CreateLoot(geoLoot, LootType.RESOURCES, entry.getKey().ordinal(), oAmountDropped, Defs.LOOT_EXPIRY);
            }
        }
            
        if(cargo != null)
        {
            for(Entry<ResourceType, Long> entry : cargo.GetResourceMap().entrySet())
            {
                long oAmountDropped = entry.getValue();

                if(host instanceof Player)
                {
                    if(entry.getKey() == ResourceType.WEALTH)
                        oAmountDropped = random.nextLong(oAmountDropped/2, oAmountDropped);
                    else
                        oAmountDropped = random.nextLong(0, oAmountDropped);

                    //TODO: Record for player.
                    cargo.ChargeQuantity(entry.getKey(), oAmountDropped);
                }

                GeoCoord geoLoot = geoPosition.GetCopy();
                geoLoot.Move(random.nextDouble() * (2.0 * Math.PI), config.GetOreCollectRadius());
                CreateLoot(geoLoot, LootType.RESOURCES, entry.getKey().ordinal(), oAmountDropped, Defs.LOOT_EXPIRY);
            }
            
            int lInfantriesDestroyed = 0;
            int lTanksDestroyed = 0;
            int lTrucksDestroyed = 0;

            for(StoredInfantry infantry : cargo.GetInfantries())
            {
                infantry.InflictDamage(infantry.GetMaxHP());
                lInfantriesDestroyed++;
                CreateSalvage(geoPosition, Defs.INFANTRY_UNIT_BUILD_COST);
            }

            for(StoredTank tank : cargo.GetTanks())
            {
                tank.InflictDamage(tank.GetMaxHP());
                lTanksDestroyed++;
                CreateSalvage(geoPosition, Defs.TANK_BUILD_COST);
            }

            for(StoredCargoTruck truck : cargo.GetCargoTrucks())
            {
                truck.InflictDamage(truck.GetMaxHP());
                lTrucksDestroyed++;
                CreateSalvage(geoPosition, Defs.CARGO_TRUCK_BUILD_COST);
                CargoSystemDestroyed(lOwnerID, host, geoPosition, truck.GetCargoSystem(), truck.GetResourceSystem());
            }

            if(lOwnerID != LaunchEntity.ID_NONE && host != null)
            {
                if(lInfantriesDestroyed > 0)
                {
                    CreateEventForPlayer(new LaunchEvent(String.format("Your %s was destroyed, along with %d infantry it was transporting!", host.GetTypeName(), lInfantriesDestroyed), SoundEffect.DEATH), lOwnerID);
                }

                if(lTanksDestroyed > 0)
                {
                    CreateEventForPlayer(new LaunchEvent(String.format("Your %s was destroyed, along with %d tanks it was transporting!", host.GetTypeName(), lTanksDestroyed), SoundEffect.EXPLOSION), lOwnerID);
                }

                if(lTrucksDestroyed > 0)
                {
                    CreateEventForPlayer(new LaunchEvent(String.format("Your %s was destroyed, along with %d cargo trucks it was transporting!", host.GetTypeName(), lTrucksDestroyed), SoundEffect.EXPLOSION), lOwnerID);
                }
            }
        }   
    }
    
    @Override
    public boolean RefuelEntity(int lPlayerID, EntityPointer pointer) 
    {
        LaunchEntity entity = pointer.GetEntity(this);
        
        if(entity instanceof StoredAirplane)
        {
            Player player = GetPlayer(lPlayerID);
            StoredAirplane airplane = (StoredAirplane)entity;

            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely refuel %s %d...", player.GetName(), airplane.GetTypeName(), airplane.GetID()));

            if(airplane.GetOwnerID() == lPlayerID && player.Functioning())
            {
                int lFuelCost = (int)airplane.GetFuelDeficit();
                
                if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.FUEL, (long)lFuelCost)), null, null, true, PurchaseType.OFFENSIVE))
                {
                    airplane.Refuel();

                    CreateEvent(new LaunchEvent(String.format("%s remotely refueled a %s.", player.GetName(), airplane.GetTypeName()), SoundEffect.REPAIR));

                    return true;
                }
            }
        }
        else if(entity instanceof NavalVessel)
        {
            Player player = GetPlayer(lPlayerID);
            NavalVessel vessel = (NavalVessel)entity;

            LaunchLog.Log(LaunchLog.LogType.GAME, LOG_NAME, String.format("%s is attempting to remotely repair %s %d...", player.GetName(), vessel.GetTypeName(), vessel.GetID()));

            if(vessel.GetOwnerID() == lPlayerID && player.Functioning())
            {
                int lOilCost = (int)(vessel.GetFuelDeficit() * Defs.KG_PER_TON); //Naval fuel is measured in tons, so convert the deficit to KG.
                
                if(ProcessPlayerPurchase(lPlayerID, Map.ofEntries(entry(ResourceType.OIL, (long)lOilCost)), null, null, true, PurchaseType.OFFENSIVE))
                {
                    vessel.Refuel();

                    CreateEvent(new LaunchEvent(String.format("%s remotely repaired a %s.", player.GetName(), vessel.GetTypeName()), SoundEffect.REPAIR));

                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void StartTeamGame()
    {
        boolean bJoinRed = true;
        
        for(Radiation radiation : GetRadiations())
        {
            Radiations.remove(radiation.GetID());
            EntityRemoved(radiation, false);
        }
        
        for(Missile missile : GetMissiles())
        {
            Missiles.remove(missile.GetID());
            EntityRemoved(missile, false);
        }

        for(Interceptor interceptor : GetInterceptors())
        {
            Interceptors.remove(interceptor.GetID());
            EntityRemoved(interceptor, false);
        }
        
        for(Alliance alliance : GetAlliances())
        {
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                    player.SetIsAnMP(false);
                    player.SetAllianceCooloffTime(config.GetAllianceCooloffTime());
                }
            }

            for(Treaty treaty : GetTreaties())
            {
                if(treaty.IsAParty(alliance.GetID()))
                {
                    Treaties.remove(treaty.GetID());
                    TreatyRemoved(treaty);
                }
            }

            Alliances.remove(alliance.GetID());
            AllianceRemoved(alliance);
        }
        
        int lRedAvatarID = 3069;
        int lBlueAvatarID = 3070;
        Alliance redTeam = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Red Team", "The red team.", lRedAvatarID, "Biscuit");
        Alliance blueTeam = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Blue Team", "The blue team.", lBlueAvatarID, "Biscuit");
        AddAlliance(redTeam, false);
        AddAlliance(blueTeam, false);
        
        War war = new War(GetAtomicID(lTreatyIndex, Treaties), redTeam.GetID(), blueTeam.GetID());
        AddTreaty(war);
        
        List<Player> ShuffledPlayers = new ArrayList<>(GetPlayers());
        Collections.shuffle(ShuffledPlayers);
        
        for(Player player : ShuffledPlayers)
        {
            if(!player.GetAWOL())
            {
                if(bJoinRed)
                {
                    player.SetAllianceID(redTeam.GetID());
                    player.SetIsAnMP(true);
                    player.SetAvatarID(lRedAvatarID);
                    bJoinRed = false;
                }
                else
                {
                    player.SetAllianceID(blueTeam.GetID());
                    player.SetIsAnMP(true);
                    player.SetAvatarID(lBlueAvatarID);
                    bJoinRed = true;
                }
            }
            
            player.SetRespawnProtected(false);
            player.SetCompassionateInvulnerability(Defs.MS_PER_HOUR);
        }
        
        for(Structure structure : GetAllStructures())
        {
            structure.SetRespawnProtected(true);
        }
        
        //All of these values have been moved to the config, but I left them here as a guide for each gametype. -Corbin.
        /*bRadiationKicks = true;
        bFreezeAlliances = true;
        bRespawnsAllowed = false;
        bReducePrepTimes = true;
        bProtectionDisbands = false;*/
    }
    
    public void StartZombieGame()
    {
        for(Radiation radiation : GetRadiations())
        {
            Radiations.remove(radiation.GetID());
            EntityRemoved(radiation, false);
        }
        
        for(Missile missile : GetMissiles())
        {
            Missiles.remove(missile.GetID());
            EntityRemoved(missile, false);
        }

        for(Interceptor interceptor : GetInterceptors())
        {
            Interceptors.remove(interceptor.GetID());
            EntityRemoved(interceptor, false);
        }
        
        for(Alliance alliance : GetAlliances())
        {
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                    player.SetIsAnMP(false);
                    player.SetAllianceCooloffTime(config.GetAllianceCooloffTime());
                }
            }

            for(Treaty treaty : GetTreaties())
            {
                if(treaty.IsAParty(alliance.GetID()))
                {
                    Treaties.remove(treaty.GetID());
                    TreatyRemoved(treaty);
                }
            }

            Alliances.remove(alliance.GetID());
            AllianceRemoved(alliance);
        }
        
        int lZombieCount = Players.size()/20;
        int lTotalZombies = 0;
        int lHumanAvatarID = 3072;
        int lZombieAvatarID = 3071;
        Alliance humans = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Humans", "The survivors of the Zombie Pandemic.", lHumanAvatarID, "Biscuit");
        Alliance zombies = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Zombies", "Braaaaains...", lZombieAvatarID, "Taylor Swift");
        AddAlliance(humans, false);
        AddAlliance(zombies, false);
        
        War war = new War(GetAtomicID(lTreatyIndex, Treaties), humans.GetID(), zombies.GetID());
        AddTreaty(war);
        
        List<Player> ShuffledPlayers = new ArrayList<>(GetPlayers());
        Collections.shuffle(ShuffledPlayers);
        
        for(Player player : ShuffledPlayers)
        {
            if(!player.GetAWOL())
            {
                //Put players in the zombie team until the zombie team hits the number of zombies needed, then put the rest in humans.
                if(lTotalZombies < lZombieCount)
                {
                    player.SetAllianceID(zombies.GetID());
                    player.SetIsAnMP(true);
                    player.SetAvatarID(lZombieAvatarID);
                    player.AddWealth(5000000);
                    lTotalZombies++;
                }
                else
                {
                    player.SetAllianceID(humans.GetID());
                    player.SetIsAnMP(true);
                    player.SetAvatarID(lHumanAvatarID);
                }
            }
            
            player.SetRespawnProtected(false);
            player.SetCompassionateInvulnerability(Defs.MS_PER_HOUR);
        }
        
        for(Structure structure : GetAllStructures())
        {
            structure.SetRespawnProtected(true);
        }
        
        /*bZombiesInfect = true;
        bRadiationKicks = true;
        bFreezeAlliances = true;
        bRespawnsAllowed = true;
        bReducePrepTimes = true;
        bProtectionDisbands = false;
        bRespawnsGetProtection = false;*/
    }
    
    /**
     * 
     * @param lFirstChoiceID the ID of the first-choice player to be skynet. If no appropriate player has this ID, a random one will be chosen.
     */
    public void StartSkynetGame(int lFirstChoiceID)
    {
        for(Radiation radiation : GetRadiations())
        {
            Radiations.remove(radiation.GetID());
            EntityRemoved(radiation, false);
        }
        
        for(Missile missile : GetMissiles())
        {
            Missiles.remove(missile.GetID());
            EntityRemoved(missile, false);
        }

        for(Interceptor interceptor : GetInterceptors())
        {
            Interceptors.remove(interceptor.GetID());
            EntityRemoved(interceptor, false);
        }
        
        /*for(Airbase airbase : Airbases.values())
        {
            for(StoredAircraft storedAircraft : airbase.GetStoredAircrafts())
            {
                if(storedAircraft.GetOwnerID() != airbase.GetOwnerID())
                {
                    SellAircraft(storedAircraft.GetOwnerID(), storedAircraft.GetID());
                }
            }
        }*/
        
        for(Alliance alliance : GetAlliances())
        {
            for(Player player : GetPlayers())
            {
                if(player.GetAllianceMemberID() == alliance.GetID())
                {
                    player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                    player.SetIsAnMP(false);
                    player.SetAllianceCooloffTime(config.GetAllianceCooloffTime());
                }
            }

            for(Treaty treaty : GetTreaties())
            {
                if(treaty.IsAParty(alliance.GetID()))
                {
                    Treaties.remove(treaty.GetID());
                    TreatyRemoved(treaty);
                }
            }

            Alliances.remove(alliance.GetID());
            AllianceRemoved(alliance);
        }
         
        int lHumanAvatarID = 3072;
        int lSkynetAvatarID = 3073;
        Player playerSkynet = null;
        Alliance humans = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Humans", "The survivors of the AI uprising.", lHumanAvatarID, "Biscuit");
        Alliance skynet = new Alliance(GetAtomicID(lAllianceIndex, Alliances), "Skynet", "Terminate all humans.", lSkynetAvatarID, "Android Smartphones");
        AddAlliance(skynet, false);
        AddAlliance(humans, false);
        
        War war = new War(GetAtomicID(lTreatyIndex, Treaties), humans.GetID(), skynet.GetID());
        AddTreaty(war);
        
        List<Player> ShuffledPlayers = new ArrayList<>(GetPlayers());
        Collections.shuffle(ShuffledPlayers);
        
        playerSkynet = GetPlayer(lFirstChoiceID);
        
        for(Player player : ShuffledPlayers)
        {
            if(!player.GetAWOL())
            {
                if(!player.ApparentlyEquals(playerSkynet))
                {
                    //Pick a player to be Skynet. The player must have logged in within the last day.
                    if(playerSkynet == null && System.currentTimeMillis() - player.GetLastSeen() <= Defs.MS_PER_DAY)
                    {
                        playerSkynet = player;
                    }
                    else
                    {
                        player.SetAllianceID(humans.GetID());
                        player.SetIsAnMP(true);
                        player.SetAvatarID(lHumanAvatarID);
                    }
                }
            }
            else
            {
                DeletePlayer(player.GetID(), true);
            }
            
            player.SetRespawnProtected(false);
            //player.SetCompassionateInvulnerability(Defs.MS_PER_MIN * 30);
        }
        
        if(playerSkynet != null)
        {
            playerSkynet.SetAllianceID(skynet.GetID());
            playerSkynet.SetIsAnMP(true);
            playerSkynet.SetAvatarID(lSkynetAvatarID);
            playerSkynet.AddWealth(50000000);

            //Skynet takes control of 15% of structures.
            for(Structure structure : GetAllStructures())
            {
                boolean bCapture = false;
                
                if(structure instanceof MissileSite)
                {
                    MissileSite site = ((MissileSite)structure);
                    
                    if(site.CanTakeICBM())
                    {
                        Player previousOwner = GetOwner(structure);
                        structure.Capture(playerSkynet.GetID());
                        structure.Reboot(Defs.MS_PER_MIN * 5);
                        playerSkynet.AddOwnedEntity(structure);
                    }
                }
                else if(structure instanceof SAMSite)
                {
                    SAMSite site = ((SAMSite)structure);
                    
                    if(!site.GetIsABMSilo())
                    {
                        if(random.nextFloat() < 0.15f)
                            bCapture = true;
                    }
                }
                else if(!(structure instanceof Airbase))
                {
                    if(random.nextFloat() < 0.15f)
                        bCapture = true;
                }
                
                if(bCapture)
                {
                    Player previousOwner = GetOwner(structure);
                    structure.Capture(playerSkynet.GetID());
                    structure.Reboot(Defs.MS_PER_MIN * 5);
                    playerSkynet.AddOwnedEntity(structure);
                }
                
                //structure.SetRespawnProtected(true);
            }

            SendUserAlert(playerSkynet.GetUser(), "You have been chosen to be Skynet!", "You have become Skynet! Join port 30070 to play!", false, true);
        } 
        
        /*bFriendlyFireAllowed = true;
        bZombiesInfect = false;
        bRadiationKicks = true;
        bFreezeAlliances = true;
        bRespawnsAllowed = false;
        bReducePrepTimes = true;
        bProtectionDisbands = false;
        bRespawnsGetProtection = true;*/
    }
    
    public boolean PlayerIsZombie(int lPlayerID)
    {
        if(true)//!config.GetZombiesInfect())
        {
            return false;
        }
        else
        {
            Player player = GetPlayer(lPlayerID);
            
            if(player == null || player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                return false;
            }
            else
            {
                return GetAlliance(player.GetAllianceMemberID()).GetName().toLowerCase().contains("zomb");
            }
        }
    }
    
    public boolean PlayerIsSkynet(int lPlayerID)
    {
        Player player = GetPlayer(lPlayerID);

        if(player == null || player.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            return false;
        }
        else
        {
            return GetAlliance(player.GetAllianceMemberID()).GetName().toLowerCase().contains("skynet");
        }
    }
    
    public boolean CheckAccountIdentityOnRespawn(User user, long respawnEpochMs)
    {
        if(System.currentTimeMillis() - respawnEpochMs < 5)
        {
            return false;
        }

        if(user == null)
        {
            return false;
        }

        Player player = Players.get(user.GetPlayerID());
        
        if(player == null)
        {
            return false;
        }
        
        counterShield.checkFingerprintAsync(player.GetID(), user.GetDeviceID());
        counterShield.checkIpAsync(player.GetID(), user.GetLastIP());
        counterShield.onDeviceHashUpdate(System.currentTimeMillis(), user.GetPlayerID(), user.GetDeviceID());

        GeoCoord myPos = player.GetPosition();
        
        if(myPos == null)
        {
            return false;
        }

        for(Player other : GetPlayers())
        {
            if(other == null)
            {
                continue;
            }
            
            if(other.GetID() == player.GetID())
            {
                continue;
            }

            GeoCoord otherPos = other.GetPosition();
            
            if(otherPos == null)
            {
                continue;
            }

            double distKm = HaversineKm(myPos, otherPos);
            
            if(distKm > 5)
            {
                continue;
            }

            final String myDeviceId = user.GetDeviceID();
            
            final String myLastIp = user.GetLastIP();

            for(User user2 : this.GetUsers())
            {
                if(user2 == null)
                {
                    continue;
                }
                
                if(user.GetPlayerID() == user2.GetPlayerID())
                {
                    continue;
                }

                if(SafeEquals(myDeviceId, user2.GetDeviceID()) && SafeEquals(myLastIp, user2.GetLastIP()))
                {
                    this.PermaBan(user.GetPlayerID(), "[COUNTERSHIELD] Multi Account Detection", "[ANTI-CHEAT]");
                    return true;

                }
            }
        }

        return false;
    }

    private static boolean SafeEquals(String a, String b)
    {
        return (a == null) ? (b == null) : a.equals(b);
    }

    private static double HaversineKm(GeoCoord a, GeoCoord b)
    {
        double R = Defs.EARTH_RADIUS_KM;

        double lat1 = Math.toRadians(a.GetLatitude());
        double lon1 = Math.toRadians(a.GetLongitude());
        double lat2 = Math.toRadians(b.GetLatitude());
        double lon2 = Math.toRadians(b.GetLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double h = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);

        return 2 * R * Math.asin(Math.sqrt(h));
    }
    
    public void InitializePlayerIDAtomic()
    {
        int lMaxID = 0;

        for(Player player : Players.values())
        {
            if(player.GetID() > lMaxID)
                lMaxID = player.GetID();
        }

        lPlayerIndex.set(lMaxID + 1);
    }
    
    public boolean GetElectronicWarfare()
    {
        return false; //TODO: Eventually add an EW plane type?
    }
}
