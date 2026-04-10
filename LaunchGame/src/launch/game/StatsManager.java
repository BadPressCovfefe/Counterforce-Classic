/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;


/**
 *
 * @author Corbin
 */
public class StatsManager //TODO: Needs to implement AchievementListener.
{
    private static final int DATA_SIZE = 336;
    /**
     * This class tracks all server stats and accomplishments/achievements.
     * This data should update once per day, and prior to each update it should 
     * print a file with the current data into a folder for that day.
     * Achievement tracking will need an AchievementListener shared by the LaunchServerGame.
     */
    
    //private static final int DATA_SIZE = very big lol;
    
    //Server-wide stats
    private long oStartedRecordingTime;                                         //The precise time when the StatsManager started recording information.
    
    private Map<ResourceType, Long> TotalResources = new ConcurrentHashMap<>();
    private Map<Integer, AllianceRecord> AllianceRecords = new ConcurrentHashMap<>();
    private Map<Integer, Integer> MissileCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> InterceptorCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> ICBMCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> TankCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> SPAAGCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> ShipCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> ShipTonnage = new ConcurrentHashMap<>();
    private Map<Integer, Integer> SubmarineCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> SubmarineTonnage = new ConcurrentHashMap<>();
    private Map<Integer, Integer> InfantryCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> CargoTruckCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> HelicopterCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> ABMCounts = new ConcurrentHashMap<>();
    private Map<Integer, Integer> AircraftCounts = new ConcurrentHashMap<>();

    private long oTotalMissiles;
    private long oTotalInterceptors;
    private long oTotalTorpedoes;
    private long oTotalNukes;                                                   //Does not count ICBMs or nuclear interceptors.
    private long oTotalICBMs;
    private long oTotalABMs;
    private long oTotalAircraft;
    private long oTotalTrucks;
    private long oTotalShips;
    private long oTotalShipsTonnage;
    private long oTotalInfantry;
    private long oTotalStructures;
    private long oNukesDetonated;                                               //All nukes ever detonated.
    private long oMissilesLaunched;
    private long oInterceptorsLaunched;
    
    //Players
    private int lHighestMoneyPlayerID;                                          //The ID of the player with the most money.
    private int lHighestOffensePlayerID;
    private int lHighestDefensePlayerID;
    private int lMostKillsPlayerID;
    private int lMostStructuresPlayerID;
    private int lMostICBMsPlayerID;
    private int lMostMissilesPlayerID;
    private int lMostInterceptorsPlayerID;
    private int lMostAircraftPlayerID;
    private int lMostShipsPlayerID;
    private int lMostInfantryPlayerID;
    private int lMostTanksPlayerID;
    private int lMostShipTonnagePlayerID;
    private int lMostTrucksPlayerID;
    
    //Alliances
    private int lWealthiestAllianceID;
    private int lBiggestAllianceID;
    private int lOffensiveAllianceID;                                           //ID of the alliance with the most offense.
    private int lDefensiveAllianceID;                                           //ID of the alliance with the most defense.
    private int lMostKillsAllianceID;
    private int lMostWarWinsAllianceID;
    private int lMostFaithfulAllianceID;                                        //The alliance that has broken the least number of affiliations.
    private int lMostStructuresAllianceID;
    private int lMostICBMsAllianceID;
    private int lMostMissilesAllianceID;
    private int lMostInterceptorsAllianceID;
    private int lMostAircraftAllianceID;
    private int lMostShipsAllianceID;
    private int lMostInfantryAllianceID;
    private int lMostTanksAllianceID;
    private int lMostShipTonnageAllianceID;
    private int lMostTrucksAllianceID;
    
    //Firsts
    
    //These values need to be prepared to be recorded before the server goes live. Nothing else in here is time-sensitive and can be calculated later.
    private int lFirstPlayer;
    private int lFirstIcbmSilo;
    private int lFirstShipyardCapture;
    private int lFirstShipBuilt;
    private int lFirstIcbmBuilt;
    private int lFirstKill;
    private int lFirstKilled;
    private int lFirstWarWin;
    private int lFirstWarLoss;
    private int lFirstAllianceFounded;
    private int lFirstIcbmLaunch;
    private int lFirstShipDestroyed;
    private int lFirstAircraftDestroyed;
    private int lFirstFissileProduced;
    private int lFirstUraniumMined;
    private int lFirstNukeProduced;
    private int lFirstNukeDetonated;
    private int lFirstAntimatterProduced;
    private int lFirstAntimatterBombDetonated;
    private int lFirstMissileManufactured;
    private int lFirstListingOnMarket;
    private int lFirstSatelliteLaunch;
    
    private LaunchServerGame game;
    
    //New stats manager.
    public StatsManager(LaunchServerGame game)
    {
        this.game = game;
        this.lFirstPlayer = LaunchEntity.ID_NONE;
        this.lFirstIcbmSilo = LaunchEntity.ID_NONE;
        this.lFirstShipyardCapture = LaunchEntity.ID_NONE;
        this.lFirstShipBuilt = LaunchEntity.ID_NONE;
        this.lFirstIcbmBuilt = LaunchEntity.ID_NONE;
        this.lFirstKill = LaunchEntity.ID_NONE;
        this.lFirstKilled = LaunchEntity.ID_NONE;
        this.lFirstWarWin = LaunchEntity.ID_NONE;
        this.lFirstWarLoss = LaunchEntity.ID_NONE;
        this.lFirstAllianceFounded = LaunchEntity.ID_NONE;
        this.lFirstIcbmLaunch = LaunchEntity.ID_NONE;
        this.lFirstShipDestroyed = LaunchEntity.ID_NONE;
        this.lFirstAircraftDestroyed = LaunchEntity.ID_NONE;
        this.lFirstFissileProduced = LaunchEntity.ID_NONE;
        this.lFirstUraniumMined = LaunchEntity.ID_NONE;
        this.lFirstNukeProduced = LaunchEntity.ID_NONE;
        this.lFirstNukeDetonated = LaunchEntity.ID_NONE;
        this.lFirstAntimatterProduced = LaunchEntity.ID_NONE;
        this.lFirstAntimatterBombDetonated = LaunchEntity.ID_NONE;
        this.lFirstMissileManufactured = LaunchEntity.ID_NONE;
        this.lFirstListingOnMarket = LaunchEntity.ID_NONE;
    }
    
    public StatsManager(LaunchServerGame game, long oStartedRecordingTime, 
                    int lFirstPlayer, int lFirstIcbmSilo, int lFirstShipyardCapture, int lFirstShipBuilt, 
                    int lFirstIcbmBuilt, int lFirstKill, int lFirstKilled, int lFirstWarWin, int lFirstWarLoss, 
                    int lFirstAllianceFounded, int lFirstIcbmLaunch, int lFirstShipDestroyed, int lFirstAircraftDestroyed, 
                    int lFirstFissileProduced, int lFirstUraniumMined, int lFirstNukeProduced, int lFirstNukeDetonated, 
                    int lFirstAntimatterProduced, int lFirstAntimatterBombDetonated, int lFirstMissileManufactured, 
                    int lFirstListingOnMarket)
    {
        this.game = game;

        this.oStartedRecordingTime = oStartedRecordingTime;

        // Initialize the "First" stats from the saved data
        this.lFirstPlayer = lFirstPlayer;
        this.lFirstIcbmSilo = lFirstIcbmSilo;
        this.lFirstShipyardCapture = lFirstShipyardCapture;
        this.lFirstShipBuilt = lFirstShipBuilt;
        this.lFirstIcbmBuilt = lFirstIcbmBuilt;
        this.lFirstKill = lFirstKill;
        this.lFirstKilled = lFirstKilled;
        this.lFirstWarWin = lFirstWarWin;
        this.lFirstWarLoss = lFirstWarLoss;
        this.lFirstAllianceFounded = lFirstAllianceFounded;
        this.lFirstIcbmLaunch = lFirstIcbmLaunch;
        this.lFirstShipDestroyed = lFirstShipDestroyed;
        this.lFirstAircraftDestroyed = lFirstAircraftDestroyed;
        this.lFirstFissileProduced = lFirstFissileProduced;
        this.lFirstUraniumMined = lFirstUraniumMined;
        this.lFirstNukeProduced = lFirstNukeProduced;
        this.lFirstNukeDetonated = lFirstNukeDetonated;
        this.lFirstAntimatterProduced = lFirstAntimatterProduced;
        this.lFirstAntimatterBombDetonated = lFirstAntimatterBombDetonated;
        this.lFirstMissileManufactured = lFirstMissileManufactured;
        this.lFirstListingOnMarket = lFirstListingOnMarket;

        SetStats();
    }
    
    public void SetStats()
    {
        TotalResources = new ConcurrentHashMap<>();
        
        //Calculate resource totals.
        /*for(ResourceType type : ResourceType.values())
        {
            long oTotalOfType = 0;
            
            for(Player player : game.GetPlayers())
            {
                player.ResetResourceCount();
                CargoSystem system = player.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            for(Loot loot : game.GetLoots())
            {
                if(loot.GetLootType() == LootType.RESOURCES)
                {
                    if(loot.GetCargoID() == type.ordinal())
                    {
                        oTotalOfType += loot.GetQuantity();
                    }
                }
            }
            
            for(Structure structure : game.GetAllStructures())
            {
                ResourceContainer container = structure.GetResourceContainer();
                
                if(container != null)
                {
                    oTotalOfType += container.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(structure);
                    
                    if(player != null)
                        player.AddAmountOfResource() += container.GetAmountOfType(type);
                }
            }
            
            for(Warehouse warehouse : game.GetWarehouses())
            {
                CargoSystem system = warehouse.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(warehouse);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            for(Shipyard shipyard : game.GetShipyards())
            {
                CargoSystem system = shipyard.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(shipyard);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
                
                ResourceContainer container = shipyard.GetResourceContainer();
                
                if(container != null)
                {
                    oTotalOfType += container.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(shipyard);
                    
                    if(player != null)
                        player.AddAmountOfResource() += container.GetAmountOfType(type);
                }
            }
            
            for(City city : game.GetCities())
            {
                ResourceContainer container = city.GetResourceContainer();
                
                if(container != null)
                {
                    oTotalOfType += container.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(city);
                    
                    if(player != null)
                        player.AddAmountOfResource() += container.GetAmountOfType(type);
                }
            }
            
            for(CargoTruckInterface truck : game.GetAllCargoTrucks())
            {
                CargoSystem system = truck.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    
                    Player player = game.GetOwner((LaunchEntity)truck);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            for(Ship ship : game.GetShips())
            {
                CargoSystem system = ship.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    
                    Player player = game.GetOwner(ship);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            for(AircraftInterface aircraft : game.GetAllAircrafts())
            {
                CargoSystem system = aircraft.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
                    
                    Player player = game.GetOwner((LaunchEntity)aircraft);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            //TODO: When trains are added.
            
            /*for(RailTerminal terminal : game.GetRailTerminals())
            {
                CargoSystem system = terminal.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
            
                    Player player = game.GetOwner(terminal);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            for(TrainInterface train : game.GetAllTrains())
            {
                CargoSystem system = train.GetCargoSystem();
                
                if(system != null)
                {
                    oTotalOfType += system.GetAmountOfType(type);
            
                    Player player = game.GetOwner((LaunchEntity)train);
                    
                    if(player != null)
                        player.AddAmountOfResource() += system.GetAmountOfType(type);
                }
            }
            
            TotalResources.put(type, oTotalOfType);
        }*/
        
        long oMissiles = 0;
        long oInterceptors = 0;
        long oTorpedoes = 0;
        long oNukes = 0;                                                   //Does not count ICBMs or nuclear interceptors.
        long oICBMs = 0;
        long oABMs = 0;
        
        for(MissileSite site : game.GetMissileSites())
        {
            if(site.CanTakeICBM())
            {
                MissileSystem system = site.GetMissileSystem();
                
                if(system != null)
                {
                    oICBMs += system.GetOccupiedSlotCount();
                }
            }
            else
            {
                MissileSystem system = site.GetMissileSystem();
                
                if(system != null)
                {
                    oMissiles += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetMissileType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                    
                    Player owner = game.GetOwner(site);
                    
                    if(owner != null)
                    {
                        //Add to the nuke count map.
                    }
                }
            }
        }
        
        for(SAMSite site : game.GetSAMSites())
        {
            if(site.GetIsABMSilo())
            {
                MissileSystem system = site.GetInterceptorSystem();
                
                if(system != null)
                {
                    oABMs += system.GetOccupiedSlotCount();
                }
            }
            else
            {
                MissileSystem system = site.GetInterceptorSystem();
                
                if(system != null)
                {
                    oInterceptors += system.GetOccupiedSlotCount();
                    
                    Player owner = game.GetOwner(site);

                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
        }
        
        for(Ship ship : game.GetShips())
        {
            if(ship.HasMissiles())
            {
                MissileSystem system = ship.GetMissileSystem();
                
                if(system != null)
                {
                    oMissiles += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetMissileType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                    
                    Player owner = game.GetOwner(ship);
                    
                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
            
            if(ship.HasInterceptors())
            {
                MissileSystem system = ship.GetInterceptorSystem();
                
                if(system != null)
                {
                    oInterceptors += system.GetOccupiedSlotCount();
                    
                    Player owner = game.GetOwner(ship);

                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
            
            if(ship.HasTorpedoes())
            {
                MissileSystem system = ship.GetTorpedoSystem();
                
                if(system != null)
                {
                    oTorpedoes += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetTorpedoType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                }
            }
        }
        
        for(Submarine submarine : game.GetSubmarines())
        {
            if(submarine.HasMissiles())
            {
                MissileSystem system = submarine.GetMissileSystem();
                
                if(system != null)
                {
                    oMissiles += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetMissileType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                    
                    Player owner = game.GetOwner(submarine);
                    
                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
            
            if(submarine.HasICBMs())
            {
                MissileSystem system = submarine.GetICBMSystem();
                
                if(system != null)
                {
                    oICBMs += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetMissileType(lMissileType).GetICBM())
                            oNukes++;
                    }
                    
                    Player owner = game.GetOwner(submarine);
                    
                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
            
            if(submarine.HasTorpedoes())
            {
                MissileSystem system = submarine.GetTorpedoSystem();
                
                if(system != null)
                {
                    oTorpedoes += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetTorpedoType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                }
            }
        }
        
        for(AirplaneInterface aircraft : game.GetAllAirplanes())
        {
            Player owner = game.GetOwner((LaunchEntity)aircraft);
            
            if(aircraft.HasMissiles())
            {
                MissileSystem system = aircraft.GetMissileSystem();
                
                if(system != null)
                {
                    oMissiles += system.GetOccupiedSlotCount();
                    
                    for(Integer lMissileType : system.GetSlotTypes().values())
                    {
                        if(game.GetConfig().GetMissileType(lMissileType).GetNuclear())
                        {
                            oNukes++;
                        }
                    }
                    
                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
            
            if(aircraft.HasInterceptors())
            {
                MissileSystem system = aircraft.GetInterceptorSystem();
                
                if(system != null)
                {
                    oInterceptors += system.GetOccupiedSlotCount();
                    
                    if(owner != null)
                    {
                        //Map
                    }
                }
            }
        }
        
        for(ArtilleryGun artillery : game.GetArtilleryGuns())
        {
            MissileSystem system = artillery.GetMissileSystem();

            if(system != null)
            {
                oMissiles += system.GetOccupiedSlotCount();
                
                for(Integer lMissileType : system.GetSlotTypes().values())
                {
                    if(game.GetConfig().GetMissileType(lMissileType).GetNuclear())
                    {
                        oNukes++;
                    }
                }
                
                Player owner = game.GetOwner(artillery);
                    
                if(owner != null)
                {
                    //Map
                }
            }
        }
        
        for(Player player : game.GetPlayers())
        {
            //Set derivitive stats like number of infantry, total ship tonnage, etc.
            int lCount = 0;
            
            for(InfantryInterface infantry : game.GetAllInfantries())
            {
                if(infantry.GetOwnerID() == player.GetID())
                {
                    lCount++;
                }
            }
            
            //Map
            
            /*lCount = 0;
            
            for(Ship ship : game.GetShips())
            {
                if(ship.GetOwnerID() == player.GetID())
                {
                    lCount += ship.GetTonnage();
                }
            }
            
            //Map
            
            lCount = 0;
            int lSubmarineTonnage = 0;
            
            for(Submarine submarine : game.GetSubmarines())
            {
                if(submarine.GetOwnerID() == player.GetID())
                {
                    lCount++;
                    lSubmarineTonnage += submarine.GetTonnage();
                }
            }*/
            
            //Map player.SetSubmarineCount(lCount);
            //Map player.SetTotalSubmarineTonnage(lSubmarineTonnage);
            
            lCount = 0;
            int lSPAAGCount = 0;
            
            for(TankInterface tank : game.GetAllTanks())
            {
                if(tank.GetOwnerID() == player.GetID())
                {
                    if(tank.IsAnMBT())
                        lCount++;
                    else if(tank.IsASPAAG())
                        lSPAAGCount++;
                }
            }
            
            //Map player.SetMainBattleTankCount(lCount);
            //Map player.SetSPAAGCount(lSPAAGCount);
            
            lCount = 0;
            
            for(CargoTruckInterface truck : game.GetAllCargoTrucks())
            {
                if(truck.GetOwnerID() == player.GetID())
                {
                    lCount++;
                }
            }
            
            //Map player.SetCargoTruckCount(lCount);
        }
        
        oTotalMissiles = oMissiles;
        oTotalInterceptors = oInterceptors;
        oTotalTorpedoes = oTorpedoes;
        oTotalNukes = oNukes;
        oTotalICBMs = oICBMs;
        oTotalABMs = oABMs;
        oTotalAircraft = game.GetAllAirplanes().size();
        oTotalTrucks = game.GetAllCargoTrucks().size();
        oTotalShips = game.GetShips().size();
        
        /*long oTotalTonnage = 0;
        
        for(NavalVessel vessel : game.GetNavalVessels())
        {
            oTotalTonnage += vessel.GetTonnage();
        }
        
        oTotalShipsTonnage = oTotalTonnage;*/
        
        oTotalInfantry = game.GetAllInfantries().size();
        oTotalStructures = game.GetAllStructures().size();

        //Players
        
        Player wealthiestCandidate = null;
        Player offensiveCandidate = null;
        Player defensiveCandidate = null;
        Player mostKillsCandidate = null;
        Player mostStructuresCandidate = null;
        Player mostICBMsCandidate = null;
        Player mostMissilesCandidate = null;
        Player mostInterceptorsCandidate = null;
        Player mostAircraftCandidate = null;
        Player mostShipsCandidate = null;
        Player mostInfantryCandidate = null;
        Player mostTanksCandidate = null;
        Player mostShipTonnageCandidate = null;
        Player mostTrucksCandidate = null;
        
        for(Alliance alliance : game.GetAlliances())
        {
            alliance.ResetTrackableStats();
        }
        
        /*for(Player player : game.GetPlayers())
        {
            if(wealthiestCandidate == null || wealthiestCandidate.GetWealth() < player.GetWealth())
                wealthiestCandidate = player;
            
            if(offensiveCandidate == null || offensiveCandidate.GetOffenseValue() < player.GetOffenseValue())
                offensiveCandidate = player;
            
            if(defensiveCandidate == null || defensiveCandidate.GetDefenseValue() < player.GetDefenseValue())
                defensiveCandidate = player;
            
            if(mostKillsCandidate == null || mostKillsCandidate.GetKills() < player.GetKills())
                mostKillsCandidate = player;
            
            if(mostStructuresCandidate == null || mostStructuresCandidate.GetStructures().size() < player.GetStructures().size())
                mostStructuresCandidate = player;
            
            if(mostICBMsCandidate == null || mostICBMsCandidate.GetICBMCount() < player.GetICBMCount())
                mostICBMsCandidate = player;
            
            if(mostMissilesCandidate == null || mostMissilesCandidate.GetMissileCount() < player.GetMissileCount())
                mostMissilesCandidate = player;
            
            if(mostInterceptorsCandidate == null || mostInterceptorsCandidate.GetInterceptorCount() < player.GetInterceptorCount())
                mostInterceptorsCandidate = player;
            
            if(mostAircraftCandidate == null || mostAircraftCandidate.GetAircraftCount() < player.GetAircraftCount())
                mostAircraftCandidate = player;
            
            if(mostShipsCandidate == null || mostShipsCandidate.GetShipCount() < player.GetShipCount())
                mostShipsCandidate = player;
            
            if(mostInfantryCandidate == null || mostInfantryCandidate.GetInfantryCount() < player.GetInfantryCount())
                mostInfantryCandidate = player;
            
            if(mostTanksCandidate == null || mostTanksCandidate.GetTankCount() < player.GetTankCount())
                mostTanksCandidate = player;
            
            if(mostShipTonnageCandidate == null || mostShipTonnageCandidate.GetTotalShipTonnage() < player.GetTotalShipTonnage())
                mostShipTonnageCandidate = player;
            
            if(mostTrucksCandidate == null || mostTrucksCandidate.GetTruckCount() < player.GetTruckCount())
                mostTrucksCandidate = player;
            
            //While we are iterating the list of players, use player resource counts to set alliance resource counts.
            if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
            {
                Alliance alliance = game.GetAlliance(player.GetAllianceMemberID());
                
                if(alliance != null)
                {
                    alliance.AddResourceCountFromPlayer(player);
                }
            }
        }
        
        lHighestMoneyPlayerID = (wealthiestCandidate != null) ? wealthiestCandidate.GetID() : LaunchEntity.ID_NONE;
        lHighestOffensePlayerID = (offensiveCandidate != null) ? offensiveCandidate.GetID() : LaunchEntity.ID_NONE;
        lHighestDefensePlayerID = (defensiveCandidate != null) ? defensiveCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostKillsPlayerID = (mostKillsCandidate != null) ? mostKillsCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostStructuresPlayerID = (mostStructuresCandidate != null) ? mostStructuresCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostICBMsPlayerID = (mostICBMsCandidate != null) ? mostICBMsCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostMissilesPlayerID = (mostMissilesCandidate != null) ? mostMissilesCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostInterceptorsPlayerID = (mostInterceptorsCandidate != null) ? mostInterceptorsCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostAircraftPlayerID = (mostAircraftCandidate != null) ? mostAircraftCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostShipsPlayerID = (mostShipsCandidate != null) ? mostShipsCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostInfantryPlayerID = (mostInfantryCandidate != null) ? mostInfantryCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostTanksPlayerID = (mostTanksCandidate != null) ? mostTanksCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostShipTonnagePlayerID = (mostShipTonnageCandidate != null) ? mostShipTonnageCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostTrucksPlayerID = (mostTrucksCandidate != null) ? mostTrucksCandidate.GetID() : LaunchEntity.ID_NONE;

        //Alliances
        Alliance wealthiestAllianceCandidate = null;
        Alliance biggestAllianceCandidate = null;
        Alliance offensiveAllianceCandidate = null;
        Alliance defensiveAllianceCandidate = null;
        Alliance mostWarWinsAllianceCandidate = null;
        Alliance mostStructuresAllianceCandidate = null;
        Alliance mostICBMsAllianceCandidate = null;
        Alliance mostMissilesAllianceCandidate = null;
        Alliance mostInterceptorsAllianceCandidate = null;
        Alliance mostAircraftAllianceCandidate = null;
        Alliance mostShipsAllianceCandidate = null;
        Alliance mostInfantryAllianceCandidate = null;
        Alliance mostTanksAllianceCandidate = null;
        Alliance mostShipTonnageAllianceCandidate = null;
        Alliance mostTrucksAllianceCandidate = null;
        
        for(Alliance alliance : game.GetAlliances())
        {
            if(wealthiestAllianceCandidate == null || wealthiestAllianceCandidate.GetWealth() < alliance.GetWealth())
                wealthiestAllianceCandidate = alliance;
            
            if(biggestAllianceCandidate == null || game.GetAllianceMemberCount(biggestAllianceCandidate) < game.GetAllianceMemberCount(alliance))
                biggestAllianceCandidate = alliance;
            
            if(offensiveAllianceCandidate == null || offensiveAllianceCandidate.GetOffenseValue() < alliance.GetOffenseValue())
                offensiveAllianceCandidate = alliance;
            
            if(defensiveAllianceCandidate == null || defensiveAllianceCandidate.GetDefenseValue() < alliance.GetDefenseValue())
                defensiveAllianceCandidate = alliance;
            
            if(mostWarWinsAllianceCandidate == null || mostWarWinsAllianceCandidate.GetWarsWon() < alliance.GetWarsWon())
                mostWarWinsAllianceCandidate = alliance;
            
            if(mostStructuresAllianceCandidate == null || mostStructuresAllianceCandidate.GetStructureCount() < alliance.GetStructureCount())
                mostStructuresAllianceCandidate = alliance;
            
            if(mostICBMsAllianceCandidate == null || mostICBMsAllianceCandidate.GetICBMCount() < alliance.GetICBMCount())
                mostICBMsAllianceCandidate = alliance;
            
            if(mostMissilesAllianceCandidate == null || mostMissilesAllianceCandidate.GetMissileCount() < alliance.GetMissileCount())
                mostMissilesAllianceCandidate = alliance;
            
            if(mostInterceptorsAllianceCandidate == null || mostInterceptorsAllianceCandidate.GetInterceptorCount() < alliance.GetInterceptorCount())
                mostInterceptorsAllianceCandidate = alliance;
            
            if(mostAircraftAllianceCandidate == null || mostAircraftAllianceCandidate.GetAircraftCount() < alliance.GetAircraftCount())
                mostAircraftAllianceCandidate = alliance;
            
            if(mostShipsAllianceCandidate == null || mostShipsAllianceCandidate.GetShipCount() < alliance.GetShipCount())
                mostShipsAllianceCandidate = alliance;
            
            if(mostInfantryAllianceCandidate == null || mostInfantryAllianceCandidate.GetInfantryCount() < alliance.GetInfantryCount())
                mostInfantryAllianceCandidate = alliance;
            
            if(mostTanksAllianceCandidate == null || mostTanksAllianceCandidate.GetTankCount() < alliance.GetTankCount())
                mostTanksAllianceCandidate = alliance;
            
            if(mostShipTonnageAllianceCandidate == null || mostShipTonnageAllianceCandidate.GetTotalShipTonnage() < alliance.GetTotalShipTonnage())
                mostShipTonnageAllianceCandidate = alliance;
            
            if(mostTrucksAllianceCandidate == null || mostTrucksAllianceCandidate.GetTruckCount() < alliance.GetTruckCount())
                mostTrucksAllianceCandidate = alliance;
        }
        
        lWealthiestAllianceID = (wealthiestAllianceCandidate != null) ? wealthiestAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lBiggestAllianceID = (biggestAllianceCandidate != null) ? biggestAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lOffensiveAllianceID = (offensiveAllianceCandidate != null) ? offensiveAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lDefensiveAllianceID = (defensiveAllianceCandidate != null) ? defensiveAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostWarWinsAllianceID = (mostWarWinsAllianceCandidate != null) ? mostWarWinsAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostStructuresAllianceID = (mostStructuresAllianceCandidate != null) ? mostStructuresAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostICBMsAllianceID = (mostICBMsAllianceCandidate != null) ? mostICBMsAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostMissilesAllianceID = (mostMissilesAllianceCandidate != null) ? mostMissilesAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostInterceptorsAllianceID = (mostInterceptorsAllianceCandidate != null) ? mostInterceptorsAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostAircraftAllianceID = (mostAircraftAllianceCandidate != null) ? mostAircraftAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostShipsAllianceID = (mostShipsAllianceCandidate != null) ? mostShipsAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostInfantryAllianceID = (mostInfantryAllianceCandidate != null) ? mostInfantryAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostTanksAllianceID = (mostTanksAllianceCandidate != null) ? mostTanksAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostShipTonnageAllianceID = (mostShipTonnageAllianceCandidate != null) ? mostShipTonnageAllianceCandidate.GetID() : LaunchEntity.ID_NONE;
        lMostTrucksAllianceID = (mostTrucksAllianceCandidate != null) ? mostTrucksAllianceCandidate.GetID() : LaunchEntity.ID_NONE;*/
    }
    
    public void NukeDetonated()
    {
        oNukesDetonated++;
    }
    
    public void MissileLaunched()
    {
        oMissilesLaunched++;
    }
    
    public void InterceptorLaunched()
    {
        oInterceptorsLaunched++;
    }
    
    public long GetStartedRecordingTime()
    {
        return oStartedRecordingTime;
    }

    public Map<ResourceType, Long> GetTotalResources()
    {
        return TotalResources;
    }

    public Map<Integer, AllianceRecord> GetAllianceRecords()
    {
        return AllianceRecords;
    }

    public long GetTotalMissiles()
    {
        return oTotalMissiles;
    }

    public long GetTotalInterceptors()
    {
        return oTotalInterceptors;
    }

    public long GetTotalTorpedoes()
    {
        return oTotalTorpedoes;
    }

    public long GetTotalNukes()
    {
        return oTotalNukes;
    }

    public long GetTotalICBMs()
    {
        return oTotalICBMs;
    }

    public long GetTotalABMs()
    {
        return oTotalABMs;
    }

    public long GetTotalAircraft()
    {
        return oTotalAircraft;
    }

    public long GetTotalTrucks()
    {
        return oTotalTrucks;
    }

    public long GetTotalShips()
    {
        return oTotalShips;
    }

    public long GetTotalShipsTonnage()
    {
        return oTotalShipsTonnage;
    }

    public long GetTotalInfantry()
    {
        return oTotalInfantry;
    }

    public long GetTotalStructures()
    {
        return oTotalStructures;
    }

    public long GetNukesDetonated()
    {
        return oNukesDetonated;
    }

    public long GetMissilesLaunched()
    {
        return oMissilesLaunched;
    }

    public long GetInterceptorsLaunched()
    {
        return oInterceptorsLaunched;
    }

    public int GetHighestMoneyPlayerID()
    {
        return lHighestMoneyPlayerID;
    }

    public int GetHighestOffensePlayerID()
    {
        return lHighestOffensePlayerID;
    }

    public int GetHighestDefensePlayerID()
    {
        return lHighestDefensePlayerID;
    }

    public int GetMostKillsPlayerID()
    {
        return lMostKillsPlayerID;
    }

    public int GetMostStructuresPlayerID()
    {
        return lMostStructuresPlayerID;
    }

    public int GetMostICBMsPlayerID()
    {
        return lMostICBMsPlayerID;
    }

    public int GetMostMissilesPlayerID()
    {
        return lMostMissilesPlayerID;
    }

    public int GetMostInterceptorsPlayerID()
    {
        return lMostInterceptorsPlayerID;
    }

    public int GetMostAircraftPlayerID()
    {
        return lMostAircraftPlayerID;
    }

    public int GetMostShipsPlayerID()
    {
        return lMostShipsPlayerID;
    }

    public int GetMostInfantryPlayerID()
    {
        return lMostInfantryPlayerID;
    }

    public int GetMostTanksPlayerID()
    {
        return lMostTanksPlayerID;
    }

    public int GetMostShipTonnagePlayerID()
    {
        return lMostShipTonnagePlayerID;
    }

    public int GetMostTrucksPlayerID()
    {
        return lMostTrucksPlayerID;
    }

    public int GetWealthiestAllianceID()
    {
        return lWealthiestAllianceID;
    }

    public int GetBiggestAllianceID()
    {
        return lBiggestAllianceID;
    }

    public int GetOffensiveAllianceID()
    {
        return lOffensiveAllianceID;
    }

    public int GetDefensiveAllianceID()
    {
        return lDefensiveAllianceID;
    }

    public int GetMostKillsAllianceID()
    {
        return lMostKillsAllianceID;
    }

    public int GetMostWarWinsAllianceID()
    {
        return lMostWarWinsAllianceID;
    }

    public int GetMostFaithfulAllianceID()
    {
        return lMostFaithfulAllianceID;
    }

    public int GetMostStructuresAllianceID()
    {
        return lMostStructuresAllianceID;
    }

    public int GetMostICBMsAllianceID()
    {
        return lMostICBMsAllianceID;
    }

    public int GetMostMissilesAllianceID()
    {
        return lMostMissilesAllianceID;
    }

    public int GetMostInterceptorsAllianceID()
    {
        return lMostInterceptorsAllianceID;
    }

    public int GetMostAircraftAllianceID()
    {
        return lMostAircraftAllianceID;
    }

    public int GetMostShipsAllianceID()
    {
        return lMostShipsAllianceID;
    }

    public int GetMostInfantryAllianceID()
    {
        return lMostInfantryAllianceID;
    }

    public int GetMostTanksAllianceID()
    {
        return lMostTanksAllianceID;
    }

    public int GetMostShipTonnageAllianceID()
    {
        return lMostShipTonnageAllianceID;
    }

    public int GetMostTrucksAllianceID()
    {
        return lMostTrucksAllianceID;
    }

    public int GetFirstPlayer()
    {
        return lFirstPlayer;
    }

    public int GetFirstIcbmSilo()
    {
        return lFirstIcbmSilo;
    }

    public int GetFirstShipyardCapture()
    {
        return lFirstShipyardCapture;
    }

    public int GetFirstShipBuilt()
    {
        return lFirstShipBuilt;
    }

    public int GetFirstIcbmBuilt()
    {
        return lFirstIcbmBuilt;
    }

    public int GetFirstKill()
    {
        return lFirstKill;
    }

    public int GetFirstKilled()
    {
        return lFirstKilled;
    }

    public int GetFirstWarWin()
    {
        return lFirstWarWin;
    }

    public int GetFirstWarLoss()
    {
        return lFirstWarLoss;
    }

    public int GetFirstAllianceFounded()
    {
        return lFirstAllianceFounded;
    }

    public int GetFirstIcbmLaunch()
    {
        return lFirstIcbmLaunch;
    }

    public int GetFirstShipDestroyed()
    {
        return lFirstShipDestroyed;
    }

    public int GetFirstAircraftDestroyed()
    {
        return lFirstAircraftDestroyed;
    }

    public int GetFirstFissileProduced()
    {
        return lFirstFissileProduced;
    }

    public int GetFirstUraniumMined()
    {
        return lFirstUraniumMined;
    }

    public int GetFirstNukeProduced()
    {
        return lFirstNukeProduced;
    }

    public int GetFirstNukeDetonated()
    {
        return lFirstNukeDetonated;
    }

    public int GetFirstAntimatterProduced()
    {
        return lFirstAntimatterProduced;
    }

    public int GetFirstAntimatterBombDetonated()
    {
        return lFirstAntimatterBombDetonated;
    }

    public int GetFirstMissileManufactured()
    {
        return lFirstMissileManufactured;
    }

    public int GetFirstListingOnMarket()
    {
        return lFirstListingOnMarket;
    }

    public void SetFirstPlayer(int value)
    {
        if (lFirstPlayer == LaunchEntity.ID_NONE)
            lFirstPlayer = value;
    }

    public void SetFirstIcbmSilo(int value)
    {
        if (lFirstIcbmSilo == LaunchEntity.ID_NONE)
            lFirstIcbmSilo = value;
    }

    public void SetFirstShipyardCapture(int value)
    {
        if (lFirstShipyardCapture == LaunchEntity.ID_NONE)
            lFirstShipyardCapture = value;
    }

    public void SetFirstShipBuilt(int value)
    {
        if (lFirstShipBuilt == LaunchEntity.ID_NONE)
            lFirstShipBuilt = value;
    }

    public void SetFirstIcbmBuilt(int value)
    {
        if (lFirstIcbmBuilt == LaunchEntity.ID_NONE)
            lFirstIcbmBuilt = value;
    }

    public void SetFirstKill(int value)
    {
        if (lFirstKill == LaunchEntity.ID_NONE)
            lFirstKill = value;
    }

    public void SetFirstKilled(int value)
    {
        if (lFirstKilled == LaunchEntity.ID_NONE)
            lFirstKilled = value;
    }

    public void SetFirstWarWin(int value)
    {
        if (lFirstWarWin == LaunchEntity.ID_NONE)
            lFirstWarWin = value;
    }

    public void SetFirstWarLoss(int value)
    {
        if (lFirstWarLoss == LaunchEntity.ID_NONE)
            lFirstWarLoss = value;
    }

    public void SetFirstAllianceFounded(int value)
    {
        if (lFirstAllianceFounded == LaunchEntity.ID_NONE)
            lFirstAllianceFounded = value;
    }

    public void SetFirstIcbmLaunch(int value)
    {
        if (lFirstIcbmLaunch == LaunchEntity.ID_NONE)
            lFirstIcbmLaunch = value;
    }

    public void SetFirstShipDestroyed(int value)
    {
        if (lFirstShipDestroyed == LaunchEntity.ID_NONE)
            lFirstShipDestroyed = value;
    }

    public void SetFirstAircraftDestroyed(int value)
    {
        if (lFirstAircraftDestroyed == LaunchEntity.ID_NONE)
            lFirstAircraftDestroyed = value;
    }

    public void SetFirstFissileProduced(int value)
    {
        if (lFirstFissileProduced == LaunchEntity.ID_NONE)
            lFirstFissileProduced = value;
    }

    public void SetFirstUraniumMined(int value)
    {
        if (lFirstUraniumMined == LaunchEntity.ID_NONE)
            lFirstUraniumMined = value;
    }

    public void SetFirstNukeProduced(int value)
    {
        if (lFirstNukeProduced == LaunchEntity.ID_NONE)
            lFirstNukeProduced = value;
    }

    public void SetFirstNukeDetonated(int value)
    {
        if (lFirstNukeDetonated == LaunchEntity.ID_NONE)
            lFirstNukeDetonated = value;
    }

    public void SetFirstAntimatterProduced(int value)
    {
        if (lFirstAntimatterProduced == LaunchEntity.ID_NONE)
            lFirstAntimatterProduced = value;
    }

    public void SetFirstAntimatterBombDetonated(int value)
    {
        if (lFirstAntimatterBombDetonated == LaunchEntity.ID_NONE)
            lFirstAntimatterBombDetonated = value;
    }

    public void SetFirstMissileManufactured(int value)
    {
        if (lFirstMissileManufactured == LaunchEntity.ID_NONE)
            lFirstMissileManufactured = value;
    }

    public void SetFirstListingOnMarket(int value)
    {
        if (lFirstListingOnMarket == LaunchEntity.ID_NONE)
            lFirstListingOnMarket = value;
    }
}
