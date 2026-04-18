/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.game.entities.Airplane;
import launch.game.entities.CargoTruck;
import launch.game.entities.Infantry;
import launch.game.entities.LandUnit;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Loot;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.entities.Structure;
import launch.game.entities.Tank;
import launch.game.entities.Warehouse;

/**
 *
 * @author Corbin
 */
public class EntityPointer
{
    public enum EntityType
    { 
        MISSILE_SITE,
        NUCLEAR_MISSILE_SITE,
        SAM_SITE,
        ABM_SILO,
        SENTRY_GUN,
        RADAR_STATION,
        COMMAND_POST,
        ORE_MINE,
        AIRBASE,
        MISSILE_FACTORY,
        BANK,
        WAREHOUSE,
        MISSILE,
        AIRPLANE,
        STORED_AIRPLANE,
        PLAYER,
        INTERCEPTOR,
        LOOT,
        RESOURCE,
        BLUEPRINT,
        RADIATION,
        AIRDROP,
        SHIP,
        SUBMARINE,
        TANK,
        NAVAL_MINE,
        INFANTRY,
        STORED_TANK,
        STORED_INFANTRY,
        ARMORY,
        BARRACKS,
        RESOURCE_DEPOSIT,
        CARGO_TRUCK,
        STORED_CARGO_TRUCK,
        PROCESSOR,
        SHIPYARD,
        TORPEDO,
        ARTILLERY_GUN,
        SCRAP_YARD,
        FACTORY,
        LAUNCH_SITE,
        RAIL_TERMINAL,
        DISTRIBUTOR,
        RUBBLE,
        MARKET_LISTING,
        MARKET,
        MBT,            //MBT, SPAAG, and MINING_TRUCK are not real entity types used anywhere else except for the UI in PurchaseButton on the client.
        SPAAG,
        MISSILE_TANK,
        SAM_TANK,
        HOWITZER,
        CHEMICALS,
        FRIGATE,
        DESTROYER,
        SUPER_CARRIER,
        CARGO_SHIP,
        AMPHIB,
        FLEET_OILER,
        ATTACK_SUB,
        SSBN,
        FIGHTER,
        BOMBER,
        STEALTH_FIGHTER,
        STEALTH_BOMBER,
        ATTACK_AIRCRAFT,
        AWACS,
        REFUELER,
        CARGO_PLANE,
        MULTI_ROLE,
        SSB,
        WATCH_TOWER,
        KOTH,
    }
    
    public static final int DATA_SIZE = 5;
    
    private int lID;
    private EntityType type;
    
    public EntityPointer(int lID, EntityType type)
    {
        this.lID = lID;
        this.type = type;
    }
    
    public EntityPointer(ByteBuffer bb)
    {
        this.lID = bb.getInt();
        this.type = EntityType.values()[bb.get()];
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        
        bb.putInt(lID);
        bb.put((byte)type.ordinal());
        
        return bb.array();
    }
    
    public int GetID()
    {
        return this.lID;
    }
    
    public EntityType GetType()
    {
        return this.type;
    }
    
    public LaunchEntity GetEntity(LaunchGame game)
    {
        switch(type)
        {
            case MISSILE_SITE:
            case NUCLEAR_MISSILE_SITE: return game.GetMissileSite(lID);
            case SAM_SITE:
            case ABM_SILO: return game.GetSAMSite(lID);
            case SENTRY_GUN: return game.GetSentryGun(lID);
            case RADAR_STATION: return game.GetRadarStation(lID);
            case COMMAND_POST: return game.GetCommandPost(lID);
            case ORE_MINE: return game.GetOreMine(lID);
            case AIRBASE: return game.GetAirbase(lID);
            case MISSILE_FACTORY: return game.GetMissileFactory(lID);
            case BANK: return game.GetBank(lID);
            case WAREHOUSE: return game.GetWarehouse(lID);
            case MISSILE: return game.GetMissile(lID);
            case FIGHTER:
            case BOMBER:
            case STEALTH_FIGHTER:
            case STEALTH_BOMBER:
            case ATTACK_AIRCRAFT:
            case AWACS:
            case REFUELER:
            case CARGO_PLANE:
            case MULTI_ROLE:
            case SSB:
            case AIRPLANE: return game.GetAirplane(lID);
            case STORED_AIRPLANE: return game.GetStoredAirplane(lID);
            case PLAYER: return game.GetPlayer(lID);
            case INTERCEPTOR: return game.GetInterceptor(lID);
            case INFANTRY: return game.GetInfantry(lID);
            case STORED_INFANTRY: return game.GetStoredInfantry(lID);
            case HOWITZER:
            case MISSILE_TANK:
            case SAM_TANK:
            case MBT:
            case SPAAG:
            case TANK: return game.GetTank(lID);
            case STORED_TANK: return game.GetStoredTank(lID);
            case BARRACKS:
            case ARMORY: return game.GetArmory(lID);
            case RESOURCE_DEPOSIT: return game.GetResourceDeposit(lID);
            case CARGO_TRUCK: return game.GetCargoTruck(lID);
            case STORED_CARGO_TRUCK: return game.GetStoredCargoTruck(lID);
            case PROCESSOR: return game.GetProcessor(lID);
            case FRIGATE:
            case DESTROYER:
            case AMPHIB:
            case CARGO_SHIP:
            case FLEET_OILER:
            case SUPER_CARRIER:
            case SHIP: return game.GetShip(lID);
            case ATTACK_SUB:
            case SSBN:
            case SUBMARINE: return game.GetSubmarine(lID);
            case LOOT: return game.GetLoot(lID);
            case TORPEDO: return game.GetTorpedo(lID);
            case SHIPYARD: return game.GetShipyard(lID);
            case ARTILLERY_GUN: return game.GetArtilleryGun(lID);
            case SCRAP_YARD: return game.GetScrapYard(lID);
            case DISTRIBUTOR: return game.GetDistributor(lID);
            case RUBBLE: return game.GetRubble(lID);
            case BLUEPRINT: return game.GetBlueprint(lID);
            case AIRDROP: return game.GetAirdrop(lID);
            default: return null;
        }
    }
    
    public MapEntity GetMapEntity(LaunchGame game)
    {
        switch(type)
        {
            case MISSILE_SITE:
            case NUCLEAR_MISSILE_SITE: return game.GetMissileSite(lID);
            case SAM_SITE:
            case ABM_SILO: return game.GetSAMSite(lID);
            case SENTRY_GUN: return game.GetSentryGun(lID);
            case RADAR_STATION: return game.GetRadarStation(lID);
            case COMMAND_POST: return game.GetCommandPost(lID);
            case ORE_MINE: return game.GetOreMine(lID);
            case AIRBASE: return game.GetAirbase(lID);
            case MISSILE_FACTORY: return game.GetMissileFactory(lID);
            case BANK: return game.GetBank(lID);
            case WAREHOUSE: return game.GetWarehouse(lID);
            case MISSILE: return game.GetMissile(lID);
            case FIGHTER:
            case BOMBER:
            case STEALTH_FIGHTER:
            case STEALTH_BOMBER:
            case ATTACK_AIRCRAFT:
            case AWACS:
            case REFUELER:
            case CARGO_PLANE:
            case MULTI_ROLE:
            case SSB:
            case AIRPLANE: return game.GetAirplane(lID);
            case PLAYER: return game.GetPlayer(lID);
            case INTERCEPTOR: return game.GetInterceptor(lID);
            case TORPEDO: return game.GetTorpedo(lID);
            case INFANTRY: return game.GetInfantry(lID);
            case BARRACKS:
            case ARMORY: return game.GetArmory(lID);
            case HOWITZER:
            case MISSILE_TANK:
            case SAM_TANK:
            case MBT:
            case SPAAG:
            case TANK: return game.GetTank(lID);
            case RESOURCE_DEPOSIT: return game.GetResourceDeposit(lID);
            case CARGO_TRUCK: return game.GetCargoTruck(lID);
            case PROCESSOR: return game.GetProcessor(lID);
            case FRIGATE:
            case DESTROYER:
            case AMPHIB:
            case CARGO_SHIP:
            case FLEET_OILER:
            case SUPER_CARRIER:
            case SHIP: return game.GetShip(lID);
            case ATTACK_SUB:
            case SSBN:
            case SUBMARINE: return game.GetSubmarine(lID);
            case LOOT: return game.GetLoot(lID);
            case SHIPYARD: return game.GetShipyard(lID);
            case ARTILLERY_GUN: return game.GetArtilleryGun(lID);
            case SCRAP_YARD: return game.GetScrapYard(lID);
            case DISTRIBUTOR: return game.GetDistributor(lID);
            case RUBBLE: return game.GetRubble(lID);
            case BLUEPRINT: return game.GetBlueprint(lID);
            case AIRDROP: return game.GetAirdrop(lID);
            default: return null;
        }
    }
    
    public Structure GetStructure(LaunchGame game)
    {
        switch(type)
        {
            case MISSILE_SITE:
            case NUCLEAR_MISSILE_SITE: return game.GetMissileSite(lID);
            case SAM_SITE:
            case ABM_SILO: return game.GetSAMSite(lID);
            case SENTRY_GUN: return game.GetSentryGun(lID);
            case RADAR_STATION: return game.GetRadarStation(lID);
            case COMMAND_POST: return game.GetCommandPost(lID);
            case ORE_MINE: return game.GetOreMine(lID);
            case AIRBASE: return game.GetAirbase(lID);
            case MISSILE_FACTORY: return game.GetMissileFactory(lID);
            case BANK: return game.GetBank(lID);
            case WAREHOUSE: return game.GetWarehouse(lID);
            case BARRACKS:
            case ARMORY: return game.GetArmory(lID);
            case PROCESSOR: return game.GetProcessor(lID);
            case ARTILLERY_GUN: return game.GetArtilleryGun(lID);
            case SCRAP_YARD: return game.GetScrapYard(lID);
            case DISTRIBUTOR: return game.GetDistributor(lID);
            default: return null;
        }
    }
    
    public LandUnit GetLandUnit(LaunchGame game)
    {
        switch(type)
        {
            case INFANTRY: return game.GetInfantry(lID);
            case TANK: 
            case SPAAG:
            case MBT:
            case HOWITZER:
            case MISSILE_TANK:
            case SAM_TANK: return game.GetTank(lID);
            case CARGO_TRUCK: return game.GetCargoTruck(lID);
            default: return null;
        }
    }
    
    public Warehouse GetWarehouse(LaunchGame game)
    {
        if(type == EntityType.WAREHOUSE)
            return game.GetWarehouse(lID);
        
        return null;
    }
    
    public CargoTruck GetCargoTruck(LaunchGame game)
    {
        switch(type)
        {
            case CARGO_TRUCK: return game.GetCargoTruck(lID);
        }
        
        return null;
    }
    
    public Tank GetTank(LaunchGame game)
    {
        switch(type)
        {
            case TANK: 
            case SPAAG:
            case MBT:
            case HOWITZER:
            case MISSILE_TANK:
            case SAM_TANK: return game.GetTank(lID);
        }
        
        return null;
    }
    
    public Loot GetLoot(LaunchGame game)
    {
        if(type == EntityType.LOOT)
            return game.GetLoot(lID);
        
        return null;
    }
    
    public Airplane GetAircraft(LaunchGame game)
    {
        switch(type)
        {
            case FIGHTER:
            case BOMBER:
            case STEALTH_FIGHTER:
            case STEALTH_BOMBER:
            case ATTACK_AIRCRAFT:
            case AWACS:
            case REFUELER:
            case CARGO_PLANE:
            case MULTI_ROLE:
            case SSB:
            case AIRPLANE: return game.GetAirplane(lID);
        }
        
        return null;
    }
    
    public Infantry GetInfantry(LaunchGame game)
    {
        if(type == EntityType.INFANTRY)
            return game.GetInfantry(lID);
        
        return null;
    }
    
    public Missile GetMissile(LaunchGame game)
    {
        if(type == EntityType.MISSILE)
            return game.GetMissile(lID);
        
        return null;
    }
    
    public static List<MapEntity> GetMapEntitiesFromPointers(List<EntityPointer> Pointers, LaunchGame game)
    {
        List<MapEntity> MapEntities = new ArrayList<>();
        
        for(EntityPointer pointer : new ArrayList<>(Pointers))
        {
            if(pointer != null)
            {
                MapEntity entity = pointer.GetMapEntity(game);

                if(entity != null)
                {
                    MapEntities.add(entity);
                }
            } 
        }
        
        return MapEntities;
    }
}
