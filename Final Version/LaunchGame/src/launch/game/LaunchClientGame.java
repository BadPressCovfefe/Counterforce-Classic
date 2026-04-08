/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.treaties.Treaty;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import launch.comm.LaunchClientComms;
import launch.comm.clienttasks.Task;
import launch.game.entities.*;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.MissileFactory.ChatChannel;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.entities.conceptuals.TerrainData;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.treaties.AffiliationRequest;
import launch.game.treaties.SurrenderProposal;
import launch.game.treaties.War;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchReport;
import launch.utilities.LaunchUtilities;
import launch.utilities.LongDelay;
import launch.utilities.PrivacyZone;

/**
 *
 * @author tobster
 */
public class LaunchClientGame extends LaunchGame implements LaunchClientGameInterface
{
    private final LaunchClientAppInterface application;
    private LaunchClientComms comms = null;
    
    private List<PrivacyZone> PrivacyZones = new ArrayList<>();
    private boolean bInPrivacyZone = false;
    
    private int lOurPlayerID = MapEntity.ID_NONE;
    
    private int lLatency = Defs.LATENCY_DISCONNECTED;
    
    private boolean bAuthenticated = false;
    
    private LinkedList<LaunchEvent> Events = new LinkedList();
    private final LinkedHashMap<String, LaunchReport> NewReports = new LinkedHashMap<>();
    private final LinkedHashMap<String, LaunchReport> OldReports = new LinkedHashMap<>();
    
    private Map<Integer, Player> NewPlayers;
    private Map<Integer, Missile> NewMissiles;
    private Map<Integer, Interceptor> NewInterceptors;
    private Map<Integer, MissileSite> NewMissileSites;
    private Map<Integer, SAMSite> NewSAMSites;
    private Map<Integer, SentryGun> NewSentryGuns;
    private Map<Integer, ArtilleryGun> NewArtilleryGuns;
    private Map<Integer, OreMine> NewOreMines;
    private Map<Integer, Processor> NewProcessors;
    private Map<Integer, Distributor> NewDistributors;
    private Map<Integer, MissileFactory> NewMissileFactorys;
    private Map<Integer, RadarStation> NewRadarStations;
    private Map<Integer, CommandPost> NewCommandPosts;
    private Map<Integer, Airbase> NewAirbases;
    private Map<Integer, Armory> NewArmories;
    private Map<Integer, Bank> NewBanks;
    private Map<Integer, ScrapYard> NewScrapYards;
    private Map<Integer, Warehouse> NewWarehouses;
    private Map<Integer, AirplaneInterface> NewAircrafts;
    private Map<Integer, InfantryInterface> NewInfantries;
    private Map<Integer, CargoTruckInterface> NewCargoTrucks;
    private Map<Integer, TankInterface> NewTanks;
    private Map<Integer, Ship> NewShips;
    private Map<Integer, Submarine> NewSubmarines;
    private Map<Integer, Loot> NewLoots;
    private Map<Integer, Rubble> NewRubbles;
    private Map<Integer, ResourceDeposit> NewResourceDeposits;
    private Map<Integer, Radiation> NewRadiations;
    private Map<Integer, Alliance> NewAlliances;
    private Map<Integer, Treaty> NewTreaties;
    private Map<Integer, Shipyard> NewShipyards;
    private List<Structure> NewAllStructures;
    private boolean bReceivingSnapshot = false;
    
    private Map<Integer, Blueprint> NewBlueprints;
    private Map<Integer, Airdrop> NewAirdrops;
    private Map<Integer, MissileType> NewMissileTypes;
    private Map<Integer, InterceptorType> NewInterceptorTypes;
    private Map<Integer, TorpedoType> NewTorpedoTypes;
    
    private LongDelay dlyUntilCommsAttempts = new LongDelay();
    
    private boolean bClosingAccount = false; //Used to suppress comms activities when the player is trying to close their account.

    //When downloading a snapshot the game is effectively stalled, so this accumulates the time for one big "catchup tick".
    private int lExtraTickTime = 0;
    
    public LaunchClientGame(Config config, LaunchClientAppInterface application, List<PrivacyZone> PrivacyZones, String strURL, int lPort, int lTickRate)
    {
        super(config, lTickRate);
        this.application = application;
        this.PrivacyZones = PrivacyZones;
        comms = new LaunchClientComms(this, strURL, lPort);
        
        StartServices();
    }
    
    public void SetDeviceID(byte[] cDeviceID, String strGoogleID, String strDeviceName, String strProcessName)
    {
        comms.SetDeviceID(cDeviceID, strGoogleID, strDeviceName, strProcessName);
    }
    
    public void SetToken(String strToken)
    {
        comms.SetToken(strToken);
    }

    @Override
    protected void CommsTick(int lMS)
    {
        lCommTickStarts++;
        comms.Tick(lMS);
        lCommTickEnds++;
    }

    @Override
    protected void GameTick(int lMS)
    {
        //For unban reconnection.
        dlyUntilCommsAttempts.Tick(lMS);
        
        //We may not have a config yet. Don't do anything until we do, and it's been verified (bAuthenticated won't be true until the config is known to be good).
        if(config != null && bAuthenticated)
        {
            //Don't tick while receiving a snapshot, just accumulate the time for one big catch-up tick when it arrives.
            if(bReceivingSnapshot)
            {
                lExtraTickTime += lMS;
            }
            else
            {
                super.GameTick(lMS + lExtraTickTime);

                lExtraTickTime = 0;
            }
        }
        
        application.GameTicked(lMS);
        
        lGameTickEnds++;
    }
    
    public void Suspend()
    {
        comms.Suspend();
        
        //Current task will have been killed to prevent confusing player. Dismiss any remaining task messages.
        application.DismissTaskMessage();
    }
    
    public void Resume()
    {
        if(dlyUntilCommsAttempts.Expired())
        {
            comms.Resume();
        }
    }
    
    public int GetCommsReinitRemaining()
    {
        return comms.GetReinitRemaining();
    }

    public boolean GetCommsDoingAnything()
    {
        return comms.GetDoingAnything();
    }
    
    public int GetCommsDownloadRate()
    {
        return comms.GetDownloadRate();
    }
    
    public List<MapEntity> GetNearestEntities(GeoCoord geoPosition, int lMaxEntities)
    {
        List<MapEntity> Result = new ArrayList<>();
        
        //Return up to a number of the nearest physically interactable entities from the specified position.
        //Create a list of all relevant entities.
        List<MapEntity> AllEntities = new ArrayList();
        AllEntities.addAll(Players.values());
        AllEntities.addAll(Missiles.values());
        AllEntities.addAll(Interceptors.values());
        AllEntities.addAll(GetAllStructures());
        AllEntities.addAll(Loots.values());
        AllEntities.addAll(GetInfantries());
        AllEntities.addAll(GetAirplanes());
        AllEntities.addAll(GetTanks());
        AllEntities.addAll(GetShipyards());
        AllEntities.addAll(GetShips());
        AllEntities.addAll(GetSubmarines());
        AllEntities.addAll(GetRubbles());
        AllEntities.addAll(GetCargoTrucks());

        //Find the nearest entities and store them in a list.
        int lExcluded = 0;
        while((Result.size() < lMaxEntities) && ((Result.size() + lExcluded) < AllEntities.size())) //Second operand of '&&' caps against there not being enough valid entities.
        {
            MapEntity nextNearest = null;
            float fltDistanceNearest = Float.MAX_VALUE;

            for (MapEntity entity : AllEntities)
            {
                boolean bCanShow = true;

                //Don't include unshowable entities. 
                if(!entity.GetVisible() && !LaunchUtilities.GetEntityVisibility(this, entity) || !entity.GetPosition().GetValid())
                {
                    lExcluded++;
                    bCanShow = false;
                }
                else if(entity instanceof Player)
                {
                    //Don't include dead or AWOL players.
                    if(!((Player)entity).Functioning())
                    {
                        lExcluded++;
                        bCanShow = false;
                    }
                }

                if(bCanShow)
                {
                    float fltDistanceTo = entity.GetPosition().DistanceTo(geoPosition);

                    if ((fltDistanceTo <= fltDistanceNearest) && (!Result.contains(entity)))
                    {
                        nextNearest = entity;
                        fltDistanceNearest = fltDistanceTo;
                    }
                }
            }

            if(nextNearest != null)
            {
                Result.add(nextNearest);
            }
        }
        
        return Result;
    }
    
    public boolean GetPlayerHasNoAirCover(Player player)
    {
        for(SAMSite samSite : SAMSites.values())
        {
            if(samSite.GetOwnerID() == player.GetID())
            {
                return false;
            }
        }

        for(SentryGun sentryGun : SentryGuns.values())
        {
            if(sentryGun.GetOwnerID() == player.GetID())
            {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean GetPlayerHasNoMissileFactory(Player player)
    {
        /*for(MissileFactory factory : GetMissileFactorys())
        {
            if(factory.GetOwnedBy(player.GetID()) && factory.GetOnline())
            {
                return false;
            }
        }*/
        
        return false;
    }
    
    public boolean GetPlayerHasNoAirOffenseCapability(Player player)
    {
        //TODO: This is inefficient.
        for(MissileSite missileSite : MissileSites.values())
        {
            if(missileSite.GetOwnerID() == player.GetID())
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int GetPlayerTotalHourlyIncome(Player player)
    {
        int lIncome = GetHourlyIncome(player);
        
        if(player != null)
        {
            for(Structure structure : new ArrayList<>(player.GetStructures()))
            {
                if(structure != null && structure.GetOwnerID() == player.GetID())
                {
                    if(structure.GetOnline() || structure.GetBooting())
                    {
                        lIncome -= config.GetMaintenanceCost(structure);
                    }
                }
            }
        }
        
        //TODO: This needs to factor in all maintenance items: Planes, ships, submarines, tanks, infantry, missiles, and interceptors, abms, and ICBMs.
        
        return lIncome;
    }
    
    private Map<Byte, Integer> GetPricesFromData(byte[] cData)
    {
        Map<Byte, Integer> Result = new HashMap<>();
        
        ByteBuffer bb = ByteBuffer.wrap(cData);
        
        while(bb.hasRemaining())
        {
            Result.put(bb.get(), bb.getInt());
        }
        
        return Result;
    }
    
    public boolean GetAnyAlerts(Player player)
    {
        if(player != null)
        {
            if(GetPlayerHasNoAirCover(player))
                return true;

            if(GetPlayerHasNoAirOffenseCapability(player))
                return true;

            if(GetPlayerTotalHourlyIncome(player) <= 0)
                return true;

            if(GetRadioactive(player, true))
                return true;
        }
        
        return false;
    }
    
    public int GetTimeToPlayerFullHealth(Player player)
    {
        return Integer.MAX_VALUE;
    }

    public int GetTimeToPlayerRadiationDeath(Player player)
    {
        return Integer.MAX_VALUE;
    }
    
    public Player GetOurPlayer()
    {
        if(lOurPlayerID == MapEntity.ID_NONE || !GetInteractionReady())
        {
            return null;
        }
        
        return GetPlayer(lOurPlayerID);
    }
    
    public List<Structure> GetOurStructures()
    {
        List<Structure> OurStructures = new ArrayList<>();

        for(Structure structure : GetAllStructures())
        {
            if(structure.GetOwnerID() == lOurPlayerID)
            {
                OurStructures.add(structure);
            }
        }
        
        return OurStructures;
    }
    
    public List<Ship> GetOurShips()
    {
        List<Ship> OurShips = new ArrayList<>();

        for(Ship ship : GetShips())
        {
            if(ship.GetOwnerID() == lOurPlayerID)
            {
                OurShips.add(ship);
            }
        }
        
        return OurShips;
    }
    
    public List<Submarine> GetOurSubmarines()
    {
        List<Submarine> OurSubmarines = new ArrayList<>();

        for(Submarine submarine : GetSubmarines())
        {
            if(submarine.GetOwnerID() == lOurPlayerID)
            {
                OurSubmarines.add(submarine);
            }
        }
        
        return OurSubmarines;
    }
    
    public List<InfantryInterface> GetOurInfantries()
    {
        List<InfantryInterface> OurInfantries = new ArrayList<>();

        for(InfantryInterface infantry : GetAllInfantries())
        {
            if(infantry.GetOwnerID() == lOurPlayerID)
            {
                OurInfantries.add(infantry);
            }
        }
        
        return OurInfantries;
    }
    
    public List<TankInterface> GetOurTanks()
    {
        List<TankInterface> OurTanks = new ArrayList<>();

        for(TankInterface tank : GetAllTanks())
        {
            if(tank.GetOwnerID() == lOurPlayerID)
            {
                OurTanks.add(tank);
            }
        }
        
        return OurTanks;
    }
    
    public List<CargoTruckInterface> GetOurCargoTrucks()
    {
        List<CargoTruckInterface> OurCargoTrucks = new ArrayList<>();

        for(CargoTruckInterface truck : GetAllCargoTrucks())
        {
            if(truck.GetOwnerID() == lOurPlayerID)
            {
                OurCargoTrucks.add(truck);
            }
        }
        
        return OurCargoTrucks;
    }
    
    public List<AirplaneInterface> GetOurAircrafts()
    {
        List<AirplaneInterface> OurAircrafts = new ArrayList<>();

        Player ourPlayer = GetPlayer(lOurPlayerID);
        
        for(Integer lID : ourPlayer.GetAircrafts())
        {
            if(lID != null)
                OurAircrafts.add(GetAirplaneInterface(lID));
        }
        
        for(Integer lID : ourPlayer.GetStoredAircrafts())
        {
            if(lID != null)
                OurAircrafts.add(GetAirplaneInterface(lID));
        }
        
        return OurAircrafts;
    }
    
    public List<RadarStation> GetOurRadarStations()
    {
        List<RadarStation> OurRadarStations = new ArrayList<>();
        
        for(RadarStation radarStation : RadarStations.values())
        {
            if(radarStation.GetOwnerID() == lOurPlayerID)
            {
                OurRadarStations.add(radarStation);
            }
        }
        
        return OurRadarStations;
    }
    
    public boolean IAmTheOnlyLeader()
    {
        Player ourPlayer = GetOurPlayer();
        
        if(ourPlayer.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED && ourPlayer.GetIsAnMP())
        {
            Alliance alliance = GetAlliance(ourPlayer.GetAllianceMemberID());
            
            for(Player player : Players.values())
            {
                if(player.GetAllianceMemberID() == ourPlayer.GetAllianceMemberID())
                {
                    if(player.GetIsAnMP() && player.GetID() != ourPlayer.GetID())
                    {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean PendingDiplomacyItems()
    {
        Player ourPlayer = GetOurPlayer();

        if(ourPlayer != null)
        {
            //Are we an alliance leader?
            if (ourPlayer.GetIsAnMP())
            {
                //Return true if any players in the game are requesting to join our alliance.
                for (Player player : Players.values())
                {
                    if (player.GetAllianceJoiningID() == ourPlayer.GetAllianceMemberID())
                        return true;
                }
                
                //Return true if any other alliances wish to affiliate with ours.
                for(Treaty treaty : Treaties.values())
                {
                    if(treaty instanceof AffiliationRequest || treaty instanceof SurrenderProposal)
                    {
                        if(treaty.GetAllianceID2() == ourPlayer.GetAllianceMemberID())
                            return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public List<Player> GetAffectedPlayers(GeoCoord geoLocation, float fltRange)
    {
        List<Player> Affected = new ArrayList();
        
        for(Structure structure : GetAllStructures())
        {
            if(geoLocation.DistanceTo(structure.GetPosition()) <= fltRange)
            {
                Player owner = Players.get(structure.GetOwnerID());

                if(!Affected.contains(owner))
                    Affected.add(owner);
            }
        }
        
        return Affected;
    }
    
    public Allegiance GetAllegiance(Player player)
    {
        return GetAllegiance(GetOurPlayer(), player);
    }
    
    public Allegiance GetAllegiance(Alliance alliance)
    {
        return GetAllegiance(GetOurPlayer(), alliance);
    }
    
    public int GetOurPlayerID() { return lOurPlayerID; }
    
    public boolean GetInPrivacyZone() { return bInPrivacyZone; }
    public List<PrivacyZone> GetPrivacyZones() { return PrivacyZones; }
    
    public void AddPrivacyZone(PrivacyZone privacyZone)
    {
        PrivacyZones.add(privacyZone);
    }
    
    public void ClearPrivacyZones()
    {
        PrivacyZones.clear();
    }
    
    public void RemovePrivacyZone(PrivacyZone zone)
    {
        PrivacyZones.remove(zone);
    }
    
    public int GetLatency()
    {
        return lLatency;
    }
    
    public boolean GetReceivingSnapshot()
    {
        return bReceivingSnapshot;
    }
    
    public boolean GetAuthenticated()
    {
        return bAuthenticated;
    }
    
    public boolean GetInteractionReady()
    {
        return bAuthenticated && !Players.isEmpty();
    }
    
    public void Register(String strGoogleID, String strPlayer, int lAvatarID, byte[] cDeviceID)
    {
        comms.Register(strGoogleID, strPlayer, lAvatarID, cDeviceID);
    }
    
    public void UploadAvatar(byte[] cData)
    {
        comms.UploadAvatar(cData);
    }
    
    public void DownloadAvatar(int lAvatarID)
    {
        comms.DownloadAvatar(lAvatarID);
    }
    
    public void DownloadImage(int lAssetID)
    {
        comms.DownloadImage(lAssetID);
    }
    
    public void Respawn()
    {
        comms.Respawn();
    }
    
    public void LaunchMissile(int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, SystemType systemType, boolean bAirburst)
    {
        comms.LaunchMissile(lSiteID, lSlotNo, geoTarget, target, systemType, bAirburst);
    }
    
    public void LaunchInterceptor(int lSiteID, int lSlotNo, int lTargetID, EntityType targetType, SystemType systemType)
    {
        comms.LaunchInterceptor(lSiteID, lSlotNo, lTargetID, targetType, systemType);
    }
    
    public void LaunchTorpedo(int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, SystemType systemType)
    {
        comms.LaunchTorpedo(lSiteID, lSlotNo, geoTarget, target, systemType);
    }
    
    public void PurchaseLaunchables(int lSiteID, int lSlotNumber, int[] lTypes, MissileSystem.SystemType systemType)
    {
        comms.PurchaseLaunchables(lSiteID, lSlotNumber, lTypes, systemType);
    }
    
    public void SellLaunchable(int lSiteID, int lSlotNumber, MissileSystem.SystemType systemType)
    {
        comms.SellLaunchable(lSiteID, lSlotNumber, systemType);
    }
    
    public void PurchaseMissileSystem()
    {
        comms.PurchaseMissileSystem();
    }
    
    public void PurchaseAircraftSlotUpgrade(int lSiteID)
    {
        comms.PurchaseAircraftSlotUpgrade(lSiteID);
    }
    
    public void PurchaseBankCapacityUpgrade(int lSiteID)
    {
        comms.PurchaseBankCapacityUpgrade(lSiteID);
    }

    public void PurchaseMissileSlotUpgradePlayer()
    {
        comms.PurchaseMissileSlotUpgradePlayer();
    }
            
    public void PurchaseMissileSlotUpgrade(int lSiteID)
    {
        comms.PurchaseMissileSlotUpgrade(lSiteID);
    }

    public void PurchaseMissileReloadUpgradePlayer()
    {
        comms.PurchaseMissileReloadUpgradePlayer();
    }
            
    public void PurchaseMissileReloadUpgrade(int lSiteID)
    {
        comms.PurchaseMissileReloadUpgrade(lSiteID);
    }
    
    public void PurchaseSAMSystem()
    {
        comms.PurchaseSAMSystem();
    }

    public void PurchaseSAMSlotUpgradePlayer()
    {
        comms.PurchaseSAMSlotUpgradePlayer();
    }
            
    public void PurchaseSAMSlotUpgrade(int lSiteID)
    {
        comms.PurchaseSAMSlotUpgrade(lSiteID);
    }
    
    public void PurchaseMissileSlotUpgradeToMax(int lSiteID)
    {
        comms.PurchaseMissileSlotUpgradeToMax(lSiteID);
    }
    
    public void PurchaseInterceptorSlotUpgradeToMax(int lSiteID)
    {
        comms.PurchaseInterceptorSlotUpgradeToMax(lSiteID);
    }

    public void PurchaseSAMReloadUpgradePlayer()
    {
        comms.PurchaseSAMReloadUpgradePlayer();
    }
            
    public void PurchaseSAMReloadUpgrade(int lSiteID)
    {
        comms.PurchaseSAMReloadUpgrade(lSiteID);
    }
    
    public void PurchaseRadarRangeUpgrade(int lSiteID)
    {
        comms.PurchaseRadarRangeUpgrade(lSiteID);
    }
    
    public void PurchaseRadarBoostUpgrade(int lSiteID)
    {
        comms.PurchaseRadarBoostUpgrade(lSiteID);
    }
    
    public void PurchaseSentryRangeUpgrade(int lSiteID)
    {
        comms.PurchaseSentryRangeUpgrade(lSiteID);
    }
    
    public void ConstructStructure(EntityType structureType, ResourceType type, int lCommandPostID, GeoCoord geoRemoteBuild, boolean bUseSubstitutes)
    {
        comms.ConstructStructure(structureType, type, lCommandPostID, geoRemoteBuild, bUseSubstitutes);
    }
    
    public void ConstructAircraft(EntityPointer homebase, EntityType type, boolean bUseSubstitutes)
    {
        comms.ConstructAircraft(homebase, type, bUseSubstitutes);
    }
    
    public void ConstructShip(int lShipyardID, EntityType type, boolean bUseSubstitutes)
    {
        comms.ConstructShip(lShipyardID, type, bUseSubstitutes);
    }
    
    public void PurchaseCommandPostHPUpgrade(int lSiteID)
    {
        comms.PurchaseCommandPostHPUpgrade(lSiteID);
    }
    
    public void SellEntity(EntityPointer pointer)
    {
        comms.SellEntity(pointer);
    }
    
    public void SellMissileSystem()
    {
        comms.SellMissileSystem();
    }
    
    public void SellSAMSystem()
    {
        comms.SellSAMSystem();
    }
    
    public void SetStructureOnOff(int lSiteID, EntityType type, boolean bOnline)
    {
        comms.SetOnlineOffline(lSiteID, type, bOnline);
    }
    
    public void SetStructuresOnOff(List<Integer> SiteIDs, EntityType type, boolean bOnline)
    {
        comms.SetMultipleOnlineOffline(SiteIDs, type, bOnline);
    }
    
    public void RepairEntity(EntityPointer pointer)
    {
        comms.RepairEntity(pointer);
    }
    
    public void HealPlayer()
    {
        comms.HealPlayer();
    }
    
    public void GiveWealth(int lReceiver, int lAmount, ResourceType type)
    {
        comms.GiveWealth(lReceiver, lAmount, type);
    }
    
    public void SetSAMSiteMode(EntityPointer pointer, byte cMode)
    {
        comms.SetSAMSiteMode(pointer, cMode);
    }
    
    /**
     * This method needs renamed; it is now used for any entity that has modes, not just SAM batteries.
     * @param Pointers entities to change the modes of.
     * @param cMode the mode to change it to.
     */
    public void SetSAMSiteModes(List<EntityPointer> Pointers, byte cMode)
    {
        comms.SetMultipleSAMSiteModes(Pointers, cMode);
    }
    
    public void SetICBMSiloMode(int lSiteID, byte cMode)
    {
        comms.SetICBMSiloMode(lSiteID, cMode);
    }
    
    public void SetICBMSiloModes(List<Integer> SiteIDs, byte cMode)
    {
        comms.SetMultipleICBMSiloModes(SiteIDs, cMode);
    }
    
    public void SetSAMEngagementSpeed(int lSiteID, float fltDistance)
    {
        comms.SetSAMEngagementSpeed(lSiteID, fltDistance);
    }
    
    public void SetEntityName(EntityPointer pointer, String strName)
    {
        comms.SetEntityName(pointer, strName);
    }
    
    public void SetPlayerName(String strName)
    {
        comms.SetPlayerName(strName);
    }
    
    public void SetAllianceName(String strName)
    {
        comms.SetAllianceName(strName);
    }
    
    public void SetAllianceDescription(String strName)
    {
        comms.SetAllianceDescription(strName);
    }
    
    public void CloseAccount()
    {
        bClosingAccount = true;
        comms.CloseAccount();
    }
    
    public void SetAvatar(int lAvatarID, boolean bIsAlliance)
    {
        comms.SetAvatar(lAvatarID, bIsAlliance);
    }
    
    public void UpgradeToNuclear(int lMissileSiteID)
    {
        comms.UpgradeToNuclear(lMissileSiteID);
    }
    
    public void CreateAlliance(String strName, String strDescription, int lAvatarID)
    {
        comms.CreateAlliance(strName, strDescription, lAvatarID);
    }
    
    public void JoinAlliance(int lAllianceID)
    {
        comms.JoinAlliance(lAllianceID);
    }
    
    public void LeaveAlliance()
    {
        comms.LeaveAlliance();
    }
    
    public void DeclareWar(int lAllianceID)
    {
        comms.DeclareWar(lAllianceID);
    }
    
    public void SurrenderWar(int lAllianceID)
    {
        comms.SurrenderWar(lAllianceID);
    }
    
    public void CancelAffiliation(int lAllianceID)
    {
        comms.CancelAffiliation(lAllianceID);
    }
    
    public void OfferAffiliation(int lAllianceID)
    {
        comms.OfferAffiliation(lAllianceID);
    }
    
    public void AcceptAffiliation(int lAllianceID)
    {
        comms.AcceptAffiliation(lAllianceID);
    }
    
    public void RejectAffiliation(int lAllianceID)
    {
        comms.RejectAffiliation(lAllianceID);
    }
    
    public void AcceptSurrender(int lAllianceID)
    {
        comms.AcceptSurrender(lAllianceID);
    }
    
    public void RejectSurrender(int lAllianceID)
    {
        comms.RejectSurrender(lAllianceID);
    }
    
    public void Promote(int lPromotee)
    {
        comms.Promote(lPromotee);
    }
    
    public void AcceptJoin(int lPromotee)
    {
        comms.AcceptJoin(lPromotee);
    }
    
    public void RejectJoin(int lPromotee)
    {
        comms.RejectJoin(lPromotee);
    }
    
    public void Kick(int lPromotee)
    {
        comms.Kick(lPromotee);
    }
    
    public void Ban(String strReason, int lPlayerID, boolean bPermanent)
    {
        comms.Ban(strReason, lPlayerID, bPermanent);
    }
    
    public void Unban(int lPlayerID)
    {
        comms.Unban(lPlayerID);
    }
    
    public void ResetAvatar(int lPlayerID)
    {
        comms.ResetAvatar(lPlayerID);
    }
    
    public void ResetName(int lPlayerID)
    {
        comms.ResetName(lPlayerID);
    }
    
    public void DeviceCheck(boolean bCompleteFailure, boolean bAPIFailure, int lFailureCode, boolean bProfileMatch, boolean bBasicIntegrity)
    {
        comms.DeviceCheck(bCompleteFailure, bAPIFailure, lFailureCode, bProfileMatch, bBasicIntegrity);
    }
    
    /**
     * Attempt to get the stats for a war, via experimental direct-access comms.
     * @param war The war to get stats for.
     * @return True if we think it might work. False if the comms report as broken. The stats come out of a @ref TreatyUpdated callback later.
     */
    public boolean GetWarStats(War war)
    {
        return comms.GetWarStats(war.GetID());
    }

    /**
     * Attempt to get a player's stats, via experimental direct-access comms.
     * @param player The player to get stats for.
     * @return True if we think it might work. False if the comms report as broken. The stats come out of a @ref PlayerStatsUpdated callback later.
     */
    public boolean GetPlayerStats(Player player)
    {
        return comms.GetPlayerStats(player.GetID());
    }
    
    public boolean GetFullStats(LaunchEntity entity)
    {
        return comms.GetFullStats(entity.GetPointer());
    }
    
    /**
     * Attempt to get the entities within an area, by experimental direct-access comms.
     * @param geoRect The area to get the entities from.
     * @return True if we think it might work. False if the comms report as broken. The stats come out of a @ref PlayerStatsUpdated callback later.
     */
    public boolean GetLocationEntities(GeoRectangle geoRect)
    {
        return comms.GetLocationEntities(geoRect);
    }
    
    public boolean GetTerrainData(GeoCoord geoCoord)
    {
        return comms.GetTerrainData(geoCoord);
    }

    /**
     * Attempt to get user data, via experimental direct-access comms.
     * @param player The player to get user data for.
     * @return True if we think it might work. False if the comms report as broken. The stats come out of a @ref ReceiveUser callback later.
     */
    public boolean GetUserData(Player player)
    {
        return comms.GetUserData(player.GetID());
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // LaunchGame inherited abstract methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    @Override
    protected void MissileExploded(Missile missile)
    {
        application.MissileExploded(missile);
    }
    
    @Override 
    protected void TorpedoExploded(Torpedo torpedo)
    {
        application.TorpedoExploded(torpedo);
    }

    @Override
    protected void InterceptorLostTarget(Interceptor interceptor)
    {
        //Do nothing. Dealt with by events.
    }

    @Override
    protected void InterceptorReachedTarget(Interceptor interceptor)
    {
        application.InterceptorReachedTarget(interceptor);
    }

    @Override
    protected void InfantryReachedTarget(Infantry infantry)
    {
        //Do nothing. Dealt with by events.
    }
    
    @Override
    public void AirbaseDestroyed(Airbase airbase)
    {
        //Do nothing. Dealt with by events.
    }
    
    @Override
    public void ShipDestroyed(Ship ship)
    {
        //Do nothing. Dealt with by events.
    }
    
    @Override
    public void AirplaneDestroyed(AirplaneInterface aircraft)
    {
        //Do nothing. Dealt with by events.
    }
    
    @Override
    public void EntityMoved(MapEntity entity)
    {
        //Do nothing. Dealt with by events.
    }
    
    @Override
    protected void EntityUpdated(LaunchEntity entity, boolean bOwner)
    {
        //The client isn't "aware" of ownership at this level; that goes on via the server and other layers.
        application.EntityUpdated(entity);
    }
    
    @Override
    protected void EntityAdded(LaunchEntity entity)
    {
        if(!bReceivingSnapshot)
        {
            Player owner = GetOwner(entity);
            
            if(owner != null)
                owner.AddOwnedEntity(entity);
        }  
    }

    @Override
    protected void EntityRemoved(LaunchEntity entity, boolean bDontCommunicate)
    {
        application.EntityRemoved(entity);
        
        Player owner = GetOwner(entity);
        
        if(owner != null)
        {
            if(entity instanceof Missile)
                owner.RemoveMissile(entity.GetID());
            else if(entity instanceof Structure)
                owner.RemoveStructure((Structure)entity);
            else if(entity instanceof Airplane)
                owner.RemoveAircraft(entity.GetID());
            else if(entity instanceof StoredAirplane)
                owner.RemoveStoredAircraft(entity.GetID());
            else if(entity instanceof SAMSite)
                owner.RemoveSAMSite(entity.GetID());
            else if(entity instanceof Warehouse)
                owner.RemoveWarehouse(entity.GetID());
        }
    }

    @Override
    protected void AllianceUpdated(Alliance alliance, boolean bMajor)
    {
        if(bMajor)
        {
            application.MajorChanges();
        }
        
        //TO DO: Respond to minor changes such as point updates?
    }

    @Override
    protected void AllianceRemoved(Alliance alliance)
    {
        application.MajorChanges();
    }

    @Override
    protected void TreatyUpdated(Treaty treaty)
    {
        application.TreatyUpdated(treaty);
        application.MajorChanges();
    }

    @Override
    protected void TreatyRemoved(Treaty treaty)
    {
        application.MajorChanges();
        
        if(treaty != null)
        {
            Alliance alliance1 = GetAlliance(treaty.GetAllianceID1());
            Alliance alliance2 = GetAlliance(treaty.GetAllianceID2());

            if(alliance1 != null)
                alliance1.RemoveTreaty(treaty.GetID());

            if(alliance2 != null)
                alliance2.RemoveTreaty(treaty.GetID());
        }  
    }
    
    public List<LaunchEvent> GetEvents()
    {
        //Synchronised and cloned as events are coming in on the comms all the time, which modifies the list.
        synchronized (Events)
        {
            return new ArrayList<>(Events);
        }
    }

    public List<LaunchReport> GetNewReports() { return new ArrayList<>(NewReports.values()); }
    public List<LaunchReport> GetOldReports() { return new ArrayList<>(OldReports.values()); }
    
    public void TransferNewReportsToOld()
    {
        for(LaunchReport report : NewReports.values())
        {
            String strMessage = report.GetMessage();

            if(OldReports.containsKey(strMessage))
            {
                OldReports.get(strMessage).Update(report);
            }
            else
            {
                OldReports.put(strMessage, report);

                //Remove first report if we've breached the limit.
                if(OldReports.size() > Defs.MAX_REPORTS)
                {
                    OldReports.remove(OldReports.entrySet().iterator().next().getKey());
                }
            }
        }

        NewReports.clear();
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // LaunchClientGameInterface methods.
    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void ReceivePlayer(Player player)
    {
        Player statsCopy = Players.get(player.GetID());
        
        if(statsCopy != null && !player.GetHasFullStats())
        {
            player.StatsCopy(statsCopy);
        }
        
        if(bReceivingSnapshot)
        {
            player.SetListener(this);
            NewPlayers.put(player.GetID(), player);
            player.SetGame(this);
        }
        else
        {
            AddPlayer(player);
            
            if(player.GetID() == lOurPlayerID)
                AssignEntityRelations();
        }        
    }
    
    @Override
    public void ReceiveTerrainData(TerrainData data)
    {
        application.EntityUpdated(data);
    }
    
    @Override
    public void ReceiveBlueprint(Blueprint blueprint)
    {
        if(bReceivingSnapshot)
        {
            blueprint.SetListener(this);
            NewBlueprints.put(blueprint.GetID(), blueprint);
        }
        else
        {
            AddBlueprint(blueprint);
        }
    }
    
    @Override
    public void ReceiveAirdrop(Airdrop airdrop)
    {
        if(bReceivingSnapshot)
        {
            airdrop.SetListener(this);
            NewAirdrops.put(airdrop.GetID(), airdrop);
        }
        else
        {
            AddAirdrop(airdrop);
        }
    }
    
    @Override
    public void ReceiveMissile(Missile missile)
    {
        if(bReceivingSnapshot)
        {
            missile.SetListener(this);
            NewMissiles.put(missile.GetID(), missile);
        }
        else
        {
            AddMissile(missile);
            EstablishStructureThreats(missile);
        }
    }
    
    @Override
    public void ReceiveTorpedo(Torpedo torpedo)
    {
        if(bReceivingSnapshot)
        {
            torpedo.SetListener(this);
            Torpedoes.put(torpedo.GetID(), torpedo);
        }
        else
        {
            AddTorpedo(torpedo);
        }
    }
    
    @Override
    public void ReceiveInterceptor(Interceptor interceptor)
    {
        if(bReceivingSnapshot)
        {
            interceptor.SetListener(this);
            NewInterceptors.put(interceptor.GetID(), interceptor);
        }
        else
        {
            AddInterceptor(interceptor);
        }
    }
    
    @Override
    public void ReceiveMissileSite(MissileSite missileSite)
    {
        if(bReceivingSnapshot)
        {
            missileSite.SetListener(this);
            NewMissileSites.put(missileSite.GetID(), missileSite);
            NewAllStructures.add(missileSite);
        }
        else
        {
            AddMissileSite(missileSite);
            EstablishStructureThreats(missileSite);
        }
    }
    
    @Override
    public void ReceiveSAMSite(SAMSite samSite)
    {
        if(bReceivingSnapshot)
        {
            samSite.SetListener(this);
            NewSAMSites.put(samSite.GetID(), samSite);
            NewAllStructures.add(samSite);
        }
        else
        {
            AddSAMSite(samSite);
            EstablishStructureThreats(samSite);
        }
    }
    
    @Override
    public void ReceiveOreMine(OreMine oreMine)
    {
        if(bReceivingSnapshot)
        {
            oreMine.SetListener(this);
            NewOreMines.put(oreMine.GetID(), oreMine);
            NewAllStructures.add(oreMine);
        }
        else
        {
            AddOreMine(oreMine);
            EstablishStructureThreats(oreMine);
        }
    }
    
    @Override
    public void ReceiveProcessor(Processor processor)
    {
        if(bReceivingSnapshot)
        {
            processor.SetListener(this);
            NewProcessors.put(processor.GetID(), processor);
            NewAllStructures.add(processor);
        }
        else
        {
            AddProcessor(processor);
            EstablishStructureThreats(processor);
        }
    }
    
    @Override
    public void ReceiveDistributor(Distributor distributor)
    {
        if(bReceivingSnapshot)
        {
            distributor.SetListener(this);
            NewDistributors.put(distributor.GetID(), distributor);
            NewAllStructures.add(distributor);
        }
        else
        {
            AddDistributor(distributor);
            EstablishStructureThreats(distributor);
        }
    }
    
    @Override
    public void ReceiveMissileFactory(MissileFactory factory)
    {
        if(bReceivingSnapshot)
        {
            factory.SetListener(this);
            NewMissileFactorys.put(factory.GetID(), factory);
            NewAllStructures.add(factory);
        }
        else
        {
            AddMissileFactory(factory);
            EstablishStructureThreats(factory);
        }
    }
    
    @Override
    public void ReceiveRadarStation(RadarStation radarStation)
    {
        if(bReceivingSnapshot)
        {
            radarStation.SetListener(this);
            NewRadarStations.put(radarStation.GetID(), radarStation);
            NewAllStructures.add(radarStation);
        }
        else
        {
            AddRadarStation(radarStation);
            EstablishStructureThreats(radarStation);
        }
    }
    
    @Override
    public void ReceiveCommandPost(CommandPost commandPost)
    {
        if(bReceivingSnapshot)
        {
            commandPost.SetListener(this);
            NewCommandPosts.put(commandPost.GetID(), commandPost);
            NewAllStructures.add(commandPost);
        }
        else
        {
            AddCommandPost(commandPost);
            EstablishStructureThreats(commandPost);
        }
    }
    
    @Override
    public void ReceiveAirbase(Airbase airbase)
    {
        if(bReceivingSnapshot)
        {
            airbase.SetListener(this);
            NewAirbases.put(airbase.GetID(), airbase);
            NewAllStructures.add(airbase);
            
            for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
            {
                aircraft.SetListener(this);
                NewAircrafts.put(aircraft.GetID(), aircraft);
            }
            
            LaunchLog.ConsoleMessage("LaunchClientGame ReceiveAirbase: Adding airbase to snapshot.");
        }
        else
        {
            LaunchLog.ConsoleMessage("LaunchClientGame ReceiveAirbase: Adding Airbase");
            AddAirbase(airbase);
            EstablishStructureThreats(airbase);
        }
    }
    
    @Override
    public void ReceiveArmory(Armory armory)
    {
        if(bReceivingSnapshot)
        {
            armory.SetListener(this);
            NewArmories.put(armory.GetID(), armory);
            NewAllStructures.add(armory);
        }
        else
        {
            AddArmory(armory);
            EstablishStructureThreats(armory);
        }
    }
    
    @Override
    public void ReceiveBank(Bank bank)
    {
        if(bReceivingSnapshot)
        {
            bank.SetListener(this);
            NewBanks.put(bank.GetID(), bank);
            NewAllStructures.add(bank);
        }
        else
        {
            AddBank(bank);
            EstablishStructureThreats(bank);
        }
    }
    
    @Override
    public void ReceiveWarehouse(Warehouse warehouse)
    {
        if(bReceivingSnapshot)
        {
            warehouse.SetListener(this);
            NewWarehouses.put(warehouse.GetID(), warehouse);
            NewAllStructures.add(warehouse);
        }
        else
        {
            AddWarehouse(warehouse);
            EstablishStructureThreats(warehouse);
        }
    }
    
    @Override
    public void ReceiveAircraft(Airplane aircraft)
    {
        if(bReceivingSnapshot)
        {
            aircraft.SetListener(this);
            NewAircrafts.put(aircraft.GetID(), aircraft);
        }
        else
        {
            AddAircraft(aircraft);
        }
    }
    
    /*@Override
    public void ReceiveStoredAircraft(StoredAircraft aircraft)
    {
        if(bReceivingSnapshot)
        {
            aircraft.SetListener(this);
            NewAircrafts.put(aircraft.GetID(), aircraft);
        }
        else
        {
            AddStoredAircraft(aircraft);
        }
    }*/
    
    @Override
    public void ReceiveInfantry(Infantry infantry)
    {
        if(bReceivingSnapshot)
        {
            infantry.SetListener(this);
            NewInfantries.put(infantry.GetID(), infantry);
        }
        else
        {
            AddInfantry(infantry);
        }
    }
    
    @Override
    public void ReceiveCargoTruck(CargoTruck truck)
    {
        if(bReceivingSnapshot)
        {
            truck.SetListener(this);
            NewCargoTrucks.put(truck.GetID(), truck);
        }
        else
        {
            AddCargoTruck(truck);
        }
    }
    
    @Override
    public void ReceiveShipyard(Shipyard shipyard)
    {
        if(bReceivingSnapshot)
        {
            shipyard.SetListener(this);
            NewShipyards.put(shipyard.GetID(), shipyard);
        }
        else
        {
            AddShipyard(shipyard);
        }
    }
    
    @Override
    public void ReceiveTank(Tank tank)
    {
        if(bReceivingSnapshot)
        {
            tank.SetListener(this);
            NewTanks.put(tank.GetID(), tank);
        }
        else
        {
            AddTank(tank);
        }
    }
    
    @Override
    public void ReceiveShip(Ship ship)
    {
        if(bReceivingSnapshot)
        {
            ship.SetListener(this);
            NewShips.put(ship.GetID(), ship);
            
            if(ship.HasAircraft())
            {
                for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                {
                    aircraft.SetListener(this);
                    NewAircrafts.put(aircraft.GetID(), aircraft);
                }
            } 
        }
        else
        {
            AddShip(ship);
        }
    }
    
    @Override
    public void ReceiveSubmarine(Submarine submarine)
    {
        if(bReceivingSnapshot)
        {
            submarine.SetListener(this);
            NewSubmarines.put(submarine.GetID(), submarine);
        }
        else
        {
            AddSubmarine(submarine);
        }
    }
    
    @Override
    public void ReceiveContactBearing(EntityPointer pointer, float fltBearing)
    {
        NavalVessel vessel = (NavalVessel)pointer.GetMapEntity(this);
        
        if(vessel != null)
        {
            vessel.AddContactBearing(fltBearing);
            EntityUpdated(vessel, false);
        }
    }
    
    @Override
    public void ReceiveStoredInfantry(StoredInfantry infantry)
    {
        if(bReceivingSnapshot)
        {
            infantry.SetListener(this);
            NewInfantries.put(infantry.GetID(), infantry);
        }
        else
        {
            AddInfantry(infantry);
        }
    }
    
    @Override
    public void ReceiveSentryGun(SentryGun sentryGun)
    {
        if(bReceivingSnapshot)
        {
            sentryGun.SetListener(this);
            NewSentryGuns.put(sentryGun.GetID(), sentryGun);
            NewAllStructures.add(sentryGun);
        }
        else
        {
            AddSentryGun(sentryGun);
            EstablishStructureThreats(sentryGun);
        }
    }
    
    @Override
    public void ReceiveArtilleryGun(ArtilleryGun artillery)
    {
        if(bReceivingSnapshot)
        {
            artillery.SetListener(this);
            NewArtilleryGuns.put(artillery.GetID(), artillery);
            NewAllStructures.add(artillery);
        }
        else
        {
            AddArtilleryGun(artillery);
            EstablishStructureThreats(artillery);
        }
    }
    
    @Override
    public void ReceiveScrapYard(ScrapYard yard)
    {
        if(bReceivingSnapshot)
        {
            yard.SetListener(this);
            NewScrapYards.put(yard.GetID(), yard);
            NewAllStructures.add(yard);
        }
        else
        {
            AddScrapYard(yard);
            EstablishStructureThreats(yard);
        }
    }
    
    @Override
    public void ReceiveLoot(Loot loot)
    {
        if(bReceivingSnapshot)
        {
            loot.SetListener(this);
            NewLoots.put(loot.GetID(), loot);
        }
        else
        {
            AddLoot(loot);
        }
    }
    
    @Override
    public void ReceiveRubble(Rubble rubble)
    {
        if(bReceivingSnapshot)
        {
            rubble.SetListener(this);
            NewRubbles.put(rubble.GetID(), rubble);
        }
        else
        {
            AddRubble(rubble);
        }
    }
    
    @Override
    public void ReceiveResourceDeposit(ResourceDeposit deposit)
    {
        if(bReceivingSnapshot)
        {
            deposit.SetListener(this);
            NewResourceDeposits.put(deposit.GetID(), deposit);
        }
        else
        {
            AddResourceDeposit(deposit);
        }
    }
    
    @Override
    public void ReceiveRadiation(Radiation radiation)
    {
        if(bReceivingSnapshot)
        {
            radiation.SetListener(this);
            NewRadiations.put(radiation.GetID(), radiation);
        }
        else
        {
            AddRadiation(radiation);
        }
    }

    @Override
    public void ReceiveAlliance(Alliance alliance, boolean bMajor)
    {
        if(bReceivingSnapshot)
        {
            alliance.SetListener(this);
            NewAlliances.put(alliance.GetID(), alliance);
        }
        else
        {
            AddAlliance(alliance, bMajor);
        }
    }

    @Override
    public void ReceiveTreaty(Treaty treaty)
    {
        if(bReceivingSnapshot)
        {
            NewTreaties.put(treaty.GetID(), treaty);
        }
        else
        {
            AddTreaty(treaty);
        }
    }

    @Override
    public void ReceiveUser(User user)
    {
        application.ReceiveUser(user);
    }
    
    @Override
    public void RemovePlayer(int lID)
    {
        Player player = Players.get(lID);
        Players.remove(lID);
        
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
        
        application.EntityRemoved(player);
    }
    
    @Override
    public void RemoveBlueprint(int lID)
    {
        Blueprint blueprint = Blueprints.get(lID);
        Blueprints.remove(lID);
        application.EntityRemoved(blueprint);
    }
    
    @Override
    public void RemoveAirdrop(int lID)
    {
        Airdrop airdrop = Airdrops.get(lID);
        Airdrops.remove(lID);
        application.EntityRemoved(airdrop);
    }
    
    @Override
    public void RemoveMissile(int lID)
    {
        Missile missile = Missiles.get(lID);
        Missiles.remove(lID);
        application.EntityRemoved(missile);
    }
    
    @Override
    public void RemoveInterceptor(int lID)
    {
        Interceptor interceptor = Interceptors.get(lID);
        Interceptors.remove(lID);
        application.EntityRemoved(interceptor);
    }
    
    @Override
    public void RemoveTorpedo(int lID)
    {
        Torpedo torpedo = Torpedoes.get(lID);
        Torpedoes.remove(lID);
        application.EntityRemoved(torpedo);
    }
    
    @Override
    public void RemoveMissileSite(int lID)
    {
        MissileSite missileSite = MissileSites.get(lID);
        MissileSites.remove(lID);
        application.EntityRemoved(missileSite);

        //TODO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveSAMSite(int lID)
    {
        SAMSite samSite = SAMSites.get(lID);
        SAMSites.remove(lID);
        application.EntityRemoved(samSite);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveSentryGun(int lID)
    {
        SentryGun sentryGun = SentryGuns.get(lID);
        SentryGuns.remove(lID);
        application.EntityRemoved(sentryGun);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveArtilleryGun(int lID)
    {
        ArtilleryGun artillery = ArtilleryGuns.get(lID);
        ArtilleryGuns.remove(lID);
        application.EntityRemoved(artillery);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveScrapYard(int lID)
    {
        ScrapYard yard = ScrapYards.get(lID);
        ScrapYards.remove(lID);
        application.EntityRemoved(yard);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveOreMine(int lID)
    {
        OreMine oreMine = OreMines.get(lID);
        OreMines.remove(lID);
        application.EntityRemoved(oreMine);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveProcessor(int lID)
    {
        Processor processor = Processors.get(lID);
        Processors.remove(lID);
        application.EntityRemoved(processor);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveDistributor(int lID)
    {
        Distributor distributor = Distributors.get(lID);
        Distributors.remove(lID);
        application.EntityRemoved(distributor);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveRadarStation(int lID)
    {
        RadarStation radarStation = RadarStations.get(lID);
        RadarStations.remove(lID);
        application.EntityRemoved(radarStation);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveCommandPost(int lID)
    {
        CommandPost commandPost = CommandPosts.get(lID);
        CommandPosts.remove(lID);
        application.EntityRemoved(commandPost);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveAirbase(int lID)
    {
        Airbase airbase = Airbases.get(lID);
        Airbases.remove(lID);
        application.EntityRemoved(airbase);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveArmory(int lID)
    {
        Armory armory = Armories.get(lID);
        Armories.remove(lID);
        application.EntityRemoved(armory);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveBank(int lID)
    {
        Bank bank = Banks.get(lID);
        Banks.remove(lID);
        application.EntityRemoved(bank);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveWarehouse(int lID)
    {
        Warehouse warehouse = Warehouses.get(lID);
        Warehouses.remove(lID);
        application.EntityRemoved(warehouse);

        //TO DO: Very expensive operation that should be optimised.
        EstablishAllStructureThreats();
    }
    
    @Override
    public void RemoveAircraft(int lID)
    {
        AirplaneInterface aircraft = GetAirplane(lID);
        
        Airplanes.remove(lID);
        
        LaunchEntity entity = (LaunchEntity)aircraft;
        
        if(entity != null)
            application.EntityRemoved(entity);
    }
    
    @Override
    public void RemoveInfantry(int lID)
    {
        InfantryInterface infantry = GetInfantryInterface(lID);
        
        Infantries.remove(lID);
        
        if(infantry != null)
            application.EntityRemoved((LaunchEntity)infantry);
    }
    
    @Override
    public void RemoveTank(int lID)
    {
        Tank tank = GetTank(lID);
        
        Tanks.remove(lID);
        application.EntityRemoved(tank);
    }
    
    @Override
    public void RemoveShip(int lID)
    {
        Ship ship = GetShip(lID);
        
        Ships.remove(lID);
        application.EntityRemoved(ship);
    }
    
    @Override
    public void RemoveSubmarine(int lID)
    {
        Submarine submarine = GetSubmarine(lID);
        
        Submarines.remove(lID);
        application.EntityRemoved(submarine);
    }
    
    @Override
    public void RemoveCargoTruck(int lID)
    {
        CargoTruckInterface truck = GetCargoTruckInterface(lID);
        
        CargoTrucks.remove(lID);
        application.EntityRemoved((CargoTruck)truck);
    }
    
    @Override
    public void RemoveStoredAircraft(int lID)
    {
        StoredAirplane storedAircraft = GetStoredAirplane(lID);
        
        if(storedAircraft != null)
        {
            Airplanes.remove(lID);
        
            MapEntity homebase = storedAircraft.GetHomeBase().GetMapEntity(this);
            
            if(homebase instanceof Airbase)
            {
                Airbase airbase = (Airbase)homebase;
                
                airbase.GetAircraftSystem().RemoveAndGetAirplane(storedAircraft.GetID());
            }
            else if(homebase instanceof Ship)
            {
                Ship ship = (Ship)homebase;
                
                ship.GetAircraftSystem().RemoveAndGetAirplane(storedAircraft.GetID());
            }
        }
    }
    
    @Override
    public void RemoveLoot(int lID)
    {
        Loot loot = Loots.get(lID);
        Loots.remove(lID);
        application.EntityRemoved(loot);
    }
    
    @Override
    public void RemoveRubble(int lID)
    {
        Rubble rubble = Rubbles.get(lID);
        Rubbles.remove(lID);
        application.EntityRemoved(rubble);
    }
    
    @Override
    public void RemoveResourceDeposit(int lID)
    {
        ResourceDeposit depost = ResourceDeposits.get(lID);
        ResourceDeposits.remove(lID);
        application.EntityRemoved(depost);
    }
    
    @Override
    public void RemoveRadiation(int lID)
    {
        Radiation radiation = Radiations.get(lID);
        Radiations.remove(lID);
        application.EntityRemoved(radiation);
    }

    @Override
    public void RemoveAlliance(int lID)
    {
        Alliance alliance = Alliances.get(lID);
        Alliances.remove(lID);
        AllianceMemberRosters.remove(lID);
        
        application.MajorChanges();
    }

    @Override
    public void RemoveWar(int lID)
    {
        Treaty war = Treaties.get(lID);
        Treaties.remove(lID);
        
        TreatyRemoved(war);
        application.MajorChanges();
    }

    @Override
    public boolean PlayerLocationAvailable()
    {
        return application.PlayerLocationAvailable();
    }

    @Override
    public LaunchClientLocation GetPlayerLocation()
    {
        LaunchClientLocation playerLocation = application.GetPlayerLocation();
        GeoCoord geoPlayerLocation = playerLocation.GetGeoCoord();

        //Check against privacy zones.
        bInPrivacyZone = false;

        for (PrivacyZone privacyZone : PrivacyZones)
        {
            float fltDistance = geoPlayerLocation.DistanceTo(privacyZone.GetPosition()) * Defs.METRES_PER_KM;
            float fltRadius = privacyZone.GetRadius();

            if (fltDistance < fltRadius)
            {
                bInPrivacyZone = true;

                Player ourPlayer = GetOurPlayer();
                GeoCoord playerGamePosition = null;
                boolean bNoPreviousPosition = true;

                if(ourPlayer != null)
                {
                    //We have our player. Does it have a valid location?
                    playerGamePosition = ourPlayer.GetPosition();    
                    
                    if(playerGamePosition.GetValid())
                    {
                        //Yes.
                        bNoPreviousPosition = false;
                    }
                }

                if(bNoPreviousPosition)
                {
                    //Move "a negative amount towards" the privacy zone. The amount is the negative of the distance to the edge of the zone.
                    geoPlayerLocation.Move(geoPlayerLocation.BearingTo(privacyZone.GetPosition()), -(fltRadius - fltDistance) / Defs.METRES_PER_KM);
                    playerLocation.SetLocationBecauseOfPrivacyZone(geoPlayerLocation);
                }
                else
                {
                    //Send our player's last location.
                    playerLocation.SetLocationBecauseOfPrivacyZone(playerGamePosition);
                }

                break;
            }
        }

        return playerLocation;
    }

    @Override
    public int GetGameConfigChecksum()
    {
        if(config == null)
        {
            //Return a duff checksum if there's no config.
            return MapEntity.ID_NONE;
        }
        
        return config.GetChecksum();
    }

    @Override
    public void SetConfig(Config config)
    {
        this.config = config;
        application.SaveConfig(config);
    }

    @Override
    public void AvatarReceived(int lAvatarID, byte[] cData)
    {
        application.SaveAvatar(lAvatarID, cData);
    }

    @Override
    public void AvatarUploaded(int lAvatarID)
    {
        application.AvatarUploaded(lAvatarID);
    }

    @Override
    public void ImageReceived(int lImageID, byte[] cData)
    {
        application.SaveImage(lImageID, cData);
    }
    
    @Override
    public void Authenticated()
    {
        bAuthenticated = true;
        application.Authenticated();
    }

    @Override
    public void AccountUnregistered()
    {
        application.AccountUnregistered();
    }

    @Override
    public void AccountNameTaken()
    {
        application.AccountNameTaken();
    }

    @Override
    public void DeviceIDUsed()
    {
        application.DeviceIDUsed();
    }

    @Override
    public void SetOurPlayerID(int lPlayerID)
    {
        lOurPlayerID = lPlayerID;
    }

    @Override
    public boolean GetReadyToUpdatePlayer()
    {
        return GetInteractionReady();
    }

    @Override
    public boolean VerifyVersion(short nMajorVersion, short nMinorVersion)
    {       
        if(nMajorVersion != Defs.MAJOR_VERSION)
        {
            application.MajorVersionMismatch();
            return false;
        }
        
        if(nMinorVersion > Defs.MINOR_VERSION)
        {
            application.MinorVersionMismatch();
        }
        
        return true;
    }

    @Override
    public void MajorVersionInvalid()
    {
        application.MajorVersionMismatch();
    }

    @Override
    public void SetLatency(int lLatency)
    {
        this.lLatency = lLatency;
    }

    @Override
    public void SnapshotBegin()
    {
        //Create new entity containers.
        NewPlayers = new ConcurrentHashMap<>();
        NewMissiles = new ConcurrentHashMap<>();
        NewInterceptors = new ConcurrentHashMap<>();
        NewMissileSites = new ConcurrentHashMap<>();
        NewSAMSites = new ConcurrentHashMap<>();
        NewOreMines = new ConcurrentHashMap<>();
        NewProcessors = new ConcurrentHashMap<>();
        NewDistributors = new ConcurrentHashMap<>();
        NewMissileFactorys = new ConcurrentHashMap<>();
        NewRadarStations = new ConcurrentHashMap<>();
        NewAirbases = new ConcurrentHashMap<>();
        NewInfantries = new ConcurrentHashMap<>();
        NewCargoTrucks = new ConcurrentHashMap<>();
        NewTanks = new ConcurrentHashMap<>();
        NewShips = new ConcurrentHashMap<>();
        NewSubmarines = new ConcurrentHashMap<>();
        NewArmories = new ConcurrentHashMap<>();
        NewBanks = new ConcurrentHashMap<>();
        NewWarehouses = new ConcurrentHashMap<>();
        NewAircrafts = new ConcurrentHashMap<>();
        NewCommandPosts = new ConcurrentHashMap<>();
        NewSentryGuns = new ConcurrentHashMap<>();
        NewArtilleryGuns = new ConcurrentHashMap<>();
        NewScrapYards = new ConcurrentHashMap<>();
        NewLoots = new ConcurrentHashMap<>();
        NewRubbles = new ConcurrentHashMap<>();
        NewResourceDeposits = new ConcurrentHashMap<>();
        NewRadiations = new ConcurrentHashMap<>();
        NewAlliances = new ConcurrentHashMap<>();
        NewTreaties = new ConcurrentHashMap<>();
        NewAllStructures = new ArrayList();
        NewBlueprints = new ConcurrentHashMap<>();
        NewAirdrops = new ConcurrentHashMap<>();
        NewMissileTypes = new ConcurrentHashMap<>();
        NewInterceptorTypes = new ConcurrentHashMap<>();
        NewTorpedoTypes = new ConcurrentHashMap<>();
        NewShipyards = new ConcurrentHashMap<>();
        
        bReceivingSnapshot = true;
    }

    @Override
    public void SnapshotFinish()
    {
        //Commit newly populated containers.
        Players = NewPlayers;
        Missiles = NewMissiles;
        Interceptors = NewInterceptors;
        MissileSites = NewMissileSites;
        SAMSites = NewSAMSites;
        SentryGuns = NewSentryGuns;
        ArtilleryGuns = NewArtilleryGuns;
        ScrapYards = NewScrapYards;
        OreMines = NewOreMines;
        Processors = NewProcessors;
        Distributors = NewDistributors;
        MissileFactorys = NewMissileFactorys;
        RadarStations = NewRadarStations;
        Airbases = NewAirbases;
        Armories = NewArmories;
        Banks = NewBanks;
        Warehouses = NewWarehouses;
        Airplanes = NewAircrafts;
        CommandPosts = NewCommandPosts;
        Loots = NewLoots;
        Rubbles = NewRubbles;
        ResourceDeposits = NewResourceDeposits;
        Radiations = NewRadiations;
        Alliances = NewAlliances;
        Treaties = NewTreaties;
        Blueprints = NewBlueprints;
        Airdrops = NewAirdrops;
        MissileTypes = NewMissileTypes;
        InterceptorTypes = NewInterceptorTypes;
        TorpedoTypes = NewTorpedoTypes;
        Infantries = NewInfantries;
        CargoTrucks = NewCargoTrucks;
        Shipyards = NewShipyards;
        Tanks = NewTanks;
        Ships = NewShips;
        Submarines = NewSubmarines;
        
        bReceivingSnapshot = false;
        
        EstablishAllStructureThreats();
        AssignEntityRelations();
        
        for(HaulerInterface hauler : GetAllCargoInterfaces())
        {
            CargoSystem system = hauler.GetCargoSystem();
            
            if(system != null)
            {
                if(system.ContainsTanks())
                {
                    for(StoredTank tank : system.GetTanks())
                        Tanks.put(tank.GetID(), tank);
                }

                if(system.ContainsCargoTrucks())
                {
                    for(StoredCargoTruck truck : system.GetCargoTrucks())
                        CargoTrucks.put(truck.GetID(), truck);
                }

                if(system.ContainsInfantry())
                {
                    for(StoredInfantry infantry : system.GetInfantries())
                        Infantries.put(infantry.GetID(), infantry);
                }
            }   
        }
        
        for(Treaty treaty : Treaties.values())
        {
            Alliance alliance1 = Alliances.get(treaty.GetAllianceID1());
            Alliance alliance2 = Alliances.get(treaty.GetAllianceID2());
            
            if(alliance1 != null)
            {
                alliance1.AddTreaty(treaty.GetID());
            }
            
            if(alliance2 != null)
            {
                alliance2.AddTreaty(treaty.GetID());
            }
        }
        
        for(Alliance alliance : Alliances.values())
        {
            if(!AllianceMemberRosters.containsKey(alliance.GetID()))
            {
                Map<Integer, Player> MemberRoster = new ConcurrentHashMap<>();
                
                for(Player player : Players.values())
                {
                    if(player.GetAllianceMemberID() == alliance.GetID() && !MemberRoster.containsKey(player.GetID()))
                    {
                        MemberRoster.put(player.GetID(), player);
                    }
                }
                
                AllianceMemberRosters.put(alliance.GetID(), MemberRoster);
            }
        }
        
        application.MajorChanges();
    }

    @Override
    public void ShowTaskMessage(Task.TaskMessage message)
    {
        application.ShowTaskMessage(message);
    }

    @Override
    public void DismissTaskMessage()
    {
        application.DismissTaskMessage();
    }
    
    @Override
    public void ShowBasicOKDialog(String strMessage)
    {
        application.ShowBasicOKDialog(strMessage);
    }

    @Override
    public void EventReceived(LaunchEvent event)
    {
        synchronized (Events)
        {
            Events.addFirst(event);

            //Purge old events.
            while (Events.size() > Defs.MAX_EVENTS)
            {
                Events.removeLast();
            }
        }

        application.NewEvent(event);
    }

    @Override
    public void ReportReceived(LaunchReport report)
    {
        String strMessage = report.GetMessage();
        
        LaunchLog.Log(LaunchLog.LogType.GAME, "Report debug", "Received report: " + strMessage);

        if(NewReports.containsKey(strMessage))
        {
            NewReports.get(strMessage).Update(report);
        }
        else
        {
            NewReports.put(strMessage, report);

            //Remove first report if we've breached the limit.
            if(NewReports.size() > Defs.MAX_REPORTS)
            {
                NewReports.remove(NewReports.entrySet().iterator().next().getKey());
            }
        }

        application.NewReport(report);
    }

    @Override
    public void AccountClosed()
    {
        Suspend();
        seService.shutdown();
        application.Quit();
    }

    @Override
    public boolean ClosingAccount()
    {
        return bClosingAccount;
    }

    @Override
    public void AllianceCreated()
    {
        application.AllianceCreated();
    }

    @Override
    public String GetProcessNames()
    {
        return application.GetProcessNames();
    }

    @Override
    public boolean GetConnectionMobile()
    {
        return application.GetConnectionMobile();
    }

    @Override
    public void DeviceCheckRequested()
    {
        application.DeviceChecksRequested();
    }

    @Override
    public void DisplayGeneralError()
    {
        application.DisplayGeneralError();
    }

    @Override
    public void TempBanned(String strReason, long oDuration)
    {
        dlyUntilCommsAttempts.Set(oDuration);
        comms.Suspend();
        application.TempBanned(strReason, oDuration);
    }

    @Override
    public void PermBanned(String strReason)
    {
        dlyUntilCommsAttempts.Set(Long.MAX_VALUE);
        comms.Suspend();
        application.PermBanned(strReason);
    }
    
    public LaunchClientComms GetComms() { return comms; }
    
    @Override
    public void UpdateToken()
    {
        application.UpdateFirebaseToken();
    } 
    
    public void ToggleAircraftReturn(EntityPointer aircraft)
    {
        comms.ToggleAircraftReturn(aircraft);
    }
    
    public void ChangeAircraftHomebase(EntityPointer entity, EntityPointer homebase)
    {
        comms.ChangeAircraftHomebase(entity, homebase);
    }
    
    public void SpoofLocation(GeoCoord geoSpoof)
    {
        comms.SpoofLocation(new LaunchClientLocation(geoSpoof.GetLatitude(), geoSpoof.GetLongitude(), 0.0f, "GPS"));
    }
    
    public void BankAction(int lBankID, int lAmount, boolean bWithdraw)
    {
        comms.BankAction(lBankID, lAmount, bWithdraw);
    }
    
    public void LinkDepots(int lFrom, int lTo)
    {
        comms.LinkDepots(lFrom, lTo);
    }
    
    public void UnlinkDepot(int lDepotID)
    {
        comms.UnlinkDepot(lDepotID);
    }
    
    public void AdminAccountTransfer(int lFrom, int lTo)
    {
        comms.AdminAccountTransfer(lFrom, lTo);
    }
    
    public void SendMessage(int lReceiverID, ChatChannel channel, String strMessage)
    {
        comms.SendMessage(lReceiverID, channel, strMessage);
    }
    
    public void PlaceBlueprint(EntityType type, ResourceType resourceType, GeoCoord geoPosition)
    {
        comms.PlaceBlueprint(type, resourceType, geoPosition);
    }
    
    public void SetAllianceTaxRate(float fltTaxRate)
    {
        comms.SetAllianceTaxRate(fltTaxRate);
    }
    
    public void AllianceWithdraw(int lAmount)
    {
        comms.AllianceWithdraw(lAmount);
    }
    
    public void AlliancePanic(int lAllianceID)
    {
        comms.AlliancePanic(lAllianceID);
    }
    
    public void CallAirdrop()
    {
        comms.CallAirdrop();
    }
    
    public void PurchaseTank(int lArmoryID, EntityType type, boolean bUseSubstitutes)
    {
        comms.PurchaseTank(lArmoryID, type, bUseSubstitutes);
    }
    
    public void PurchaseAircraft(EntityPointer origin, EntityType type, boolean bUseSubstitutes)
    {
        comms.PurchaseAircraft(origin, type, bUseSubstitutes);
    }
    
    public void PurchaseShip(int lShipyardID, EntityType type, boolean bUseSubstitutes)
    {
        comms.PurchaseShip(lShipyardID, type, bUseSubstitutes);
    }
    
    public void PurchaseInfantry(int lArmoryID, boolean bUseSubstitutes)
    {
        comms.PurchaseInfantry(lArmoryID, bUseSubstitutes);
    }
    
    public void PurchaseCargoTruck(int lWarehouseID, boolean bUseSubstitutes)
    {
        comms.PurchaseCargoTruck(lWarehouseID, bUseSubstitutes);
    }
    
    public void UnitCommand(MoveOrders command, List<EntityPointer> Commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        comms.UnitCommand(command, Commandables, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver);
    }
    
    public void TransferCargo(EntityPointer entityFrom, EntityPointer entityTo, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver, boolean bLoadAsCargo, boolean bFromCargo)
    {
        comms.TransferCargo(entityFrom, entityTo, typeToDeliver, lDeliverID, lQuantityToDeliver, bLoadAsCargo, bFromCargo);
    }
    
    public void FireElectronicWarfare(int lAircraftID, EntityPointer target)
    {
        comms.FireElectronicWarfare(lAircraftID, target);
    }
    
    public void Prospect()
    {
        comms.Prospect();
    }
    
    public void NavalSpeedChange(EntityPointer vessel, boolean bIncrease)
    {
        comms.NavalSpeedChange(vessel, bIncrease);
    }
    
    public void DiveOrSurface(int lSubmarineID)
    {
        comms.DiveOrSurface(lSubmarineID);
    }
    
    public void SonarPing(EntityPointer pinger)
    {
        comms.SonarPing(pinger);
    }
    
    public void ToggleAirbaseOpen(EntityPointer host)
    {
        comms.ToggleAirbaseOpen(host);
    }
    
    public void RadarScan(EntityPointer scanner)
    {
        comms.RadarScan(scanner);
    }
    
    public void KickAircraft(EntityPointer aircraft)
    {
        comms.KickAircraft(aircraft);
    }
    
    public void SetArtilleryTarget(EntityPointer pointer, GeoCoord geoTarget, float fltRadius)
    {
        comms.SetArtilleryTarget(pointer, geoTarget, fltRadius);
    }
    
    public void CeaseFire(List<EntityPointer> Pointers)
    {
        comms.CeaseFire(Pointers);
    }
    
    public void RefuelAircraftAtBase(EntityPointer airbase, EntityPointer aircraft)
    {
        comms.RefuelAircraftAtBase(airbase, aircraft);
    }
    
    public void LoadLandUnit(EntityPointer unit, EntityPointer transport)
    {
        comms.LoadLandUnit(unit, transport);
    }
    
    public void DropLandUnit(EntityPointer unit)
    {
        comms.DropLandUnit(unit);
    }
    
    public void TransferLandUnit(EntityPointer sender, EntityPointer receiver, EntityPointer unit)
    {
        comms.TransferLandUnit(sender, receiver, unit);
    }
    
    public void CaptureEntity(EntityPointer pointer)
    {
        comms.CaptureEntity(pointer);
    }
    
    public void UpgradeShipyard(int lShipyardID)
    {
        comms.UpgradeShipyard(lShipyardID);
    }
    
    public void MoveOrder(List<EntityPointer> Movables, Map<Integer, GeoCoord> Coordinates)
    {
        comms.MoveOrder(Movables, Coordinates);
    }
    
    public void AdminDelete(EntityPointer pointer)
    {
        comms.AdminDelete(pointer);
    }
    
    public void ListOnMarketTask(LaunchClientGameInterface gameInterface, int lMarketID, EntityPointer pointerDeliverer, LootType typeOfListing, int lTypeID, int lQuantity, float fltPriceEach)
    {
        comms.ListOnMarket(gameInterface, lMarketID, pointerDeliverer, typeOfListing, lTypeID, lQuantity, fltPriceEach);
    }
    
    public void PurchaseListing(int lListingID, int lAmountToPurchase)
    {
        comms.PurchaseListing(lListingID, lAmountToPurchase);
    }
    
    public void Obliterate(int lPlayerID)
    {
        comms.Obliterate(lPlayerID);
    }
    
    public void MigrateAccount(byte[] cIMEI, String strGoogleID)
    {
        comms.MigrateAccount(cIMEI, strGoogleID);
    }

    @Override
    public void AccountUnmigrated() 
    {
        LaunchLog.ConsoleMessage("void AccountUnmigrated [LaunchClientGame]");
        application.AccountUnmigrated();
    }

    @Override
    public void FailedSpoofCheck() 
    {
        application.FailedSpoofCheck();
    }

    @Override
    public void AccountMigrated(boolean bDisplaySuccess) 
    {
        application.AccountMigrated(bDisplaySuccess);
    }
    
    public void NotifySubscriptionUpdate(boolean bSubscribed)
    {
        comms.NotifySubscriptionUpdate(bSubscribed);
    }
    
    public void VerifyPurchase(String strPurchaseToken)
    {
        comms.VerifyPurchase(strPurchaseToken);
    }
    
    public void ToggleTruckAutoCollect(int lTruckID)
    {
        comms.ToggleTruckAutoCollect(lTruckID);
    }
    
    public void Blacklist(int lPlayerID)
    {
        comms.Blacklist(lPlayerID);
    }
}
