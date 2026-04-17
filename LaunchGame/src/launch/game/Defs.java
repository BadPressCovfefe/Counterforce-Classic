/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class Defs
{
    //---------------------------------------------------------------------------------------------------------------------------------
    // Boring administrative stuff starts here.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public static final boolean DEBUG_MODE = false;
    
    public static final long COUNTERFORCE_START_DATE = 1682908800000L;
    public static final int SERVER_TICK_RATE = 1000;
    
    public static final int MAX_PLAYER_NAME_LENGTH = 32;
    public static final int MAX_ALLIANCE_NAME_LENGTH = 32;
    public static final int MAX_ALLIANCE_DESCRIPTION_LENGTH = 140;
    public static final int MAX_STRUCTURE_NAME_LENGTH = 32;
    
    public static int THE_GREAT_BIG_NOTHING = -1;                               //Initialiser for properties indicating uninitialised/unassigned/doesn't have one/not in one/etc.
    
    public static final String LOCATION_AVATARS = "avatars";
    public static final String LOCATION_IMGASSETS = "imgassets";
    public static final String IMAGE_FILE_FORMAT = "%s/%d.png";
    
    public static final short MAJOR_VERSION = 88;                               //Clients must be compliant with the major version, or they won't be allowed to log in.
    public static final short MINOR_VERSION = 4;                                //Clients with a lower minor version can log in, but will be advised to update.
    
    public static final int AVATAR_SIZE = 128;                                  //Total avatar size, including allegiance ring.
    public static final int AVATAR_IMAGE = 120;                                 //Avatar size within the allegiance ring.
    
    public static final double EARTH_RADIUS_KM = 6372.8;
    public static final float MILE_TO_KM = 0.621371192f;

    public static final float METRES_PER_KM = 1000.0f;
    public static final float MPS_PER_KPH = 0.277778f;
    public static final float KPH_PER_MPS = 3.6f;
    public static final float KPH_PER_MACH = 1234.8f;
    public static final float KPH_PER_KNOT = 1.852f;
    
    public static final String SERVER_EMAIL = "contact@ballisticgamesus.com";
    public static final float REQUIRED_ACCURACY = 0.25f;
    
    public static final int KG_PER_TON = 1000;
    public static final int TONS_PER_KT = 1000;
    
    public static final int SEC_PER_HOUR = 3600;
    public static final int MS_PER_SEC = 1000;
    public static final int MS_PER_MIN = MS_PER_SEC * 60;
    public static final int MS_PER_HOUR = MS_PER_MIN * 60;
    public static final int MS_PER_DAY = MS_PER_HOUR * 24;
    public static final double MS_PER_HOUR_DBL = (double)MS_PER_HOUR;
    public static final float MS_PER_HOUR_FLT = (float)MS_PER_HOUR;
    public static final long MS_PER_QUARTER = 7776000000L;
    public static final long NINETY_DAYS = 7776000000L;
    public static final int HOURS_PER_DAY = 24;
    public static final int DAYS_PER_WEEK = 7;
    
    public static final float ONE_DEGREE = 0.01f;

    public static final int LATENCY_DISCONNECTED = -1;
    
    public static final int MAX_EVENTS = 3;
    public static final int MAX_REPORTS = 100;

    public static final long PLAYER_ONLINE_TIME = 20000; //Time since player last updated to consider them 'online'.
    
    public static final float LOCATION_SPOOF_SUSPECT_SPEED = 1000.0f;      //Player speed to record a possible location spoof, KPH.
    public static final float LOCATION_SPOOF_SUSPECT_DISTANCE = 1.0f;      //Player movement distance to record a possible location spoof, KPH.
    
    /**
     * Time since a player's last update which if less than this, will cause a linear collision check with world entities as they're "on the move".
     */
    public static final long ON_THE_MOVE_TIME_THRESHOLD = 60000;
    
    /**
     * Number of kilometres to step when walking a player's "on the move" collision detection path.
     */
    public static final float ON_THE_MOVE_STEP_DISTANCE = 0.05f;
    
    /**
     * Chance of performing a running process check.
     */
    public static final float PROCESS_CHECK_RANDOM_CHANCE = 0.01f;
    
    public static final int AUTH_FLAG_RES1 = 0x01;
    public static final int AUTH_FLAG_MOBILE = 0x02;
    public static final int AUTH_FLAG_RES2 = 0x04;
    public static final int AUTH_FLAG_RES3 = 0x08;
    public static final int AUTH_FLAG_RES4 = 0x10;
    public static final int AUTH_FLAG_RES5 = 0x20;
    public static final int AUTH_FLAG_RES6 = 0x40;
    public static final int AUTH_FLAG_RES7 = 0x80;
    
    /** Identity for special player-carried stuff such as the site ID for player-carried missiles and interceptors. */
    public static final int PLAYER_CARRIED = -1;
    
    /** Cost value that generically indicates something is no longer upgradeable. */
    public static final int UPGRADE_COST_MAXED = Integer.MAX_VALUE;
    
    public static final float MULTIACCOUNT_CONSIDERATION_DISTANCE = 0.5f; //Proximity of players to consider during multiaccounting checks, km.
 
    /** For emulator detection. */
    public static final int SUSPICIOUSLY_LOW_PROCESS_NUMBER = 80;
    public static final int THREATENINGLY_LOW_PROCESS_NUMBER = 20;
    public static final int BANNABLY_LOW_PROCESS_NUMBER = 8;
    public static final int WEALTH_YESTERDAY_SUS_THRESHOLD = 7;                 //If players gain this many times more than the average amount of wealth per day, admins will be alerted.
    public static final int EXPECTED_DATA_DIR_LENGTH = 41;
    public static final String DATA_DIR_SHOULD_MATCH = "com.apps.fast.counterforceclassic";
    public static final String DATA_DIR_TEST = "com.apps.fast.counterforceclassictest";
    public static final float LOGIN_LOCATION_SPOOF_THRESHOLD = 10.0f;           //Distance from last login location that a user with a changed device ID will be denied access.
    
    public static final long IP_POISON_TIME = 172800000;
        
    public static final boolean IsMobile(byte cAuthFlags)
    {
        return ( cAuthFlags & AUTH_FLAG_MOBILE ) != 0x00;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Exciting gameplay-related defs start here.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public static final float PRIVACY_OFFSET = 0.1f;                            //The distance that in-person structures will be build away from the player to protect privacy.
    public static final int RESPAWN_TIME = MS_PER_MIN * 60;
    public static final int WEALTH_CAP = 300000;                                //TO DO: Move back to config in a major release.
    public static final int RADIATION_MIN_EXPIRY = MS_PER_HOUR * 24;
    public static final int RADIATION_MAX_EXPIRY = MS_PER_HOUR * 72;
    public static final int CHEMICAL_MIN_EXPIRY = MS_PER_HOUR * 12;
    public static final int CHEMICAL_MAX_EXPIRY = MS_PER_HOUR * 24;
    public static final int RADIATION_SPREAD_HOUR = 15;                         //The number of KM per hour radiation will spread.
    public static final short RADIATION_DMG_HOUR = 10;                          //The amount of HP done by radiation to players and infantry per hour.
    public static final int RADIATION_POP_LOSS_HOUR = 100;                      //The amount of city population killed by radiation per hour.
    public static final int MAX_MISSILE_SLOTS = 127;                            //TO DO: Change this functionality. This is just a preliminary measure to prevent an overflow crash.
    public static final float WAR_REWARD_FACTOR = 0.3f;
    public static final float WAR_WON_XP_PER_WEALTH = 0.001f;
    public static final float RELATIONSHIP_BONUS_THRESHOLD = 0.25f;             //The threshold that must be passed to be eligible for player relationship based bonuses as a ratio of all players.
    public static final float MAX_TAX_RATE = 0.9f;
    public static final float MIN_TAX_RATE = 0.1f;
    public static final float DEFAULT_TAX_RATE = 0.15f;                         //The default tax rate for new alliances.
    public static final float NOOB_WARNING = 0.1f;
    public static final float ELITE_WARNING = 5.0f;
    public static final float MINIMUM_INTERCEPTOR_ACCURACY = 0.3f;
    public static final float MAXIMUM_INTERCEPTOR_ACCURACY = 0.9f;
    public static final float ABM_EMP_RADIUS_MULTIPLIR = 15.0f;
    public static final float DESIRED_SAM_HIT_CHANCE = 0.8f;                    //We want the total number of interceptors on a missile to add to this hit chance before we stop firing.
    public static final float INTERCEPTOR_ACCURACY = 0.65f;
    public static final float NON_ABM_ACCURACY = 0.15f;                         //Accuracy of normal interceptors against MIRVs and ICBMs.
    public static final float ABM_HIT_CHANCE = 0.35f;                           //Accuracy of non-nuclear ABMs.
    public static final float NUCLEAR_INTERCEPTOR_HIT_CHANCE = 0.8f;            //Accuracy of nuclear interceptors.
    public static final float INTERCEPTOR_NUKE_KILL_CHANCE = 0.9f;              //The chance of a nuclear interceptor destroying missiles or aircraft in its blast radius.
    public static final float AIRCRAFT_CANNON_HIT_CHANCE = 0.40f;
    public static final float MAXIMUM_AIRCRAFT_CANNON_ACCURACY = 0.7f;
    public static final float MINIMUM_AIRCRAFT_CANNON_ACCURACY = 0.07f;
    public static final float AIRCRAFT_DODGE_CHANCE_PER_KILL = 0.05f;           //Aircraft receive 5% dodge chance for every kill they have.
    public static final float MINIMUM_SENTRY_ACCURACY = 0.28f;
    public static final float OFFLINE_PLAYER_AIRCRAFT_DODGE_BUFF = 0.0f;        //Aircraft belonging to offline players have a better chance of dodging itnerceptors.
    public static final float OFFLINE_PLAYER_INTERCEPTOR_HIT_BUFF = 0.0f;      //Interceptors belonging to offline players have a better chance of hitting aircraft.
    public static final float MAXIMUM_ABM_ACCURACY = 0.28f;                     //Because f the players, that's why.
    public static final float ICBM_ACCELERATION = 29.42f;                       //9g of acceleration for ICBMs.
    public static final float SENTRY_GUN_ACCURACY = 0.15f;
    public static final float OFFLINE_PLAYER_SENTRY_ACCURACY = SENTRY_GUN_ACCURACY; //0.0f;
    public static final short SENTRY_GUN_MIN_DMG = 5;
    public static final short SENTRY_GUN_MAX_DMG = 25;
    public static final float SENTRY_RANGE = 1.5f;
    public static final short MAX_SENTRY_GUN_DAMAGE = 30;
    public static final int ATTACK_VISIBLE_TIME = MS_PER_MIN * 5;
    public static final int SUBMARINE_ACTION_VISIBLE_TIME = MS_PER_MIN * 20;
    public static final float STEALTH_DETECTION_DISTANCE = 70.0f;
    public static final float STEALTH_DETECTION_FRACTION = 0.3f;
    public static final float STEALTH_ENGAGEMENT_DISTANCE = 120.0f;             //Distance at which stealth missiles and aircraft can be engaged automatically. 
    public static final float AUTO_DETECTION_DISTANCE = 50.0f;                  //Distance from hostile players at which land units become automatically seen.
    public static final float SHIP_REFUELING_DISTANCE = 0.5f;
    public static final float ELECTRONIC_WARFARE_RANGE = 150f;
    public static final int ELECTRONIC_WARFARE_REBOOT_TIME = MS_PER_MIN * 15;
    public static final int AIRCRAFT_CANNON_RELOAD_TIME = MS_PER_SEC * 15;
    public static final int ELECTRONIC_WARFARE_RELOAD = MS_PER_MIN * 1;
    public static final float AWACS_SCANNER_RANGE = 400;
    
    public static final float MAX_AIRDROP_DISTANCE = 1.0f;                      //1 kilometer
    public static final int AIRDROP_COOLDOWN = MS_PER_HOUR * 9;                 //Cooldown of 5 minutes between each airdrop.
    public static final int PROSPECT_COOLDOWN = MS_PER_HOUR * 9;
    public static final int AIRDROP_ARRIVAL_TIME = MS_PER_MIN * 30;             //The minimum number of minutes that the airdrop will arrive in.
    public static final int AIRDROP_EXPIRY = Defs.MS_PER_HOUR * 24;             //Once the airdrop arrives, this is the minimum lifespan of the loot it drops.
    public static final int FIRE_VISIBILITY_TIME = MS_PER_MIN * 3;              //How long a structure is visible after being fired, such as when a missilesite or sentrygun fires. 
    public static final int RADAR_SCAN_VISIBILITY_TIME = MS_PER_MIN * 15;       //How long an entity is visible after being hit by a radar scan.
    public static final int SONAR_SCAN_VISIBILITY_TIME = MS_PER_MIN * 5;        //How long a ship or submarine is visible after being hit with a sonar ping (or after DOING a sonar ping.)
    public static final int ICBM_FIRE_VISIBILITY_TIME = Integer.MAX_VALUE;      //ICBMS are visible at all times.
    public static final int MIRV_FIRE_VISIBILITY_TIME = Integer.MAX_VALUE;      //MIRVs are visible at all times.
    public static final int STEALTH_MISSILE_FIRE_VISIBILITY_TIME = MS_PER_MIN * 1;
    public static final int MISSILE_FIRE_VISIBILITY_TIME = MS_PER_MIN * 3;
    public static final int AUTO_FIRE_RELOAD_TIME = MS_PER_MIN * 15;
    public static final int BOMB_TRAVEL_TIME_MS = MS_PER_SEC * 43;
    public static final int BOMB_TRAVEL_TIME_AIRBURST_MS = MS_PER_SEC * 35;
    public static final float ENRICHED_URANIUM_DEPOSIT_CHANCE = 0.01f;
    public static final float GOLD_DEPOSIT_CHANCE = 0.02f;
    public static final float T3_DEPOSIT_CHANCE = 0.09f;
    public static final float T2_DEPOSIT_CHANCE = 0.15f;
    public static final long ENRICHED_URANIUM_DEPOSIT_MIN_RESERVES = 10;
    public static final long ENRICHED_URANIUM_DEPOSIT_MAX_RESERVES = 50;
    public static final long GOLD_DEPOSIT_MIN_RESERVES = 10;
    public static final long GOLD_DEPOSIT_MAX_RESERVES = 150;
    public static final long DEPOSIT_MIN_RESERVES = 100000;
    public static final long DEPOSIT_MAX_RESERVES = 150000;
    public static final float DEPOSIT_VARIANCE_KM = 3f;
    public static final float DEPOSIT_RADIUS = 0.5f;
    public static final float MISSILE_EXTRACTION_EFFICIENCY = 0.8f; //Missiles destroy 30% of what they take out of deposits.
    
    public static final float T1_SUBSTITUTION_VALUE_KG = 1.0f;
    public static final float T2_SUBSTITUTION_VALUE_KG = 4.0f;
    public static final float T3_SUBSTITUTION_VALUE_KG = 9.0f;
    public static final float FISSILE_SCRAP_VALUE_KG = 1500f;
    public static final float GOLD_SCRAP_VALUE_KG = 1000f;
    public static final float SCRAP_PROPORTION = 0.5f;
    public static final long SCRAPYARD_RATE_PER_HOUR = 10000;
    
    public static final float GetResourceScrapValuePerKG(ResourceType type)
    {
        switch(type)
        {
            case IRON:
            case COAL:
            case OIL:
            case CROPS:
            case URANIUM:
            case ELECTRICITY:
            case CONCRETE:
            case LUMBER: return T1_SUBSTITUTION_VALUE_KG * SCRAP_PROPORTION;
            case FOOD:
            case FUEL:
            case ELECTRONICS:
            case STEEL:
            case CONSTRUCTION_SUPPLIES: return T2_SUBSTITUTION_VALUE_KG * SCRAP_PROPORTION;
            case MACHINERY:
            case MEDICINE: return T3_SUBSTITUTION_VALUE_KG * SCRAP_PROPORTION;
            case ENRICHED_URANIUM: return FISSILE_SCRAP_VALUE_KG;
            case GOLD: return GOLD_SCRAP_VALUE_KG;
        }
        
        return 0;
    }
    
    public static final float LANDING_APPROACH_DISTANCE = 10.0f;                //The distance at which a landing aircraft is considered to be "on approach," and will slow down and have a tighter turn radius.
    public static final float LANDING_APPROACH_SPEED = 300f;
    public static final float LANDING_APPROACH_TURN_RATE = 0.6f;
    public static final float ATTACK_APPROACH_DISTANCE = 3.0f;                  //The same concept as LANDING_APPROACH_SPEED, but for ground attack aircraft that are strafing a target.
    public static final float ATTACK_APPROACH_SPEED = 600f;
    public static final float ATTACK_APPROACH_TURN_RATE = 0.03f;
    public static final float AIRCRAFT_TURN_RATE = 0.45f;  
    public static final float TANKER_TURN_RATE = 0.1f;                          //The turn rate of a tanker that is providing fuel, in orer to make it easier for the receiving plane to get close.
    public static final float REFUELEE_TURN_RATE = 0.6f;                        //The turn rate of a plane that is refueling and is within the refuel distance.
    
    public static final short MAX_INTERCEPTOR_DAMAGE = 1500;
    public static final short MIN_INTERCEPTOR_DAMAGE = 200;
    public static final float AIRCRAFT_CANNON_RANGE = 2.0f;
    public static final short AIRCRAFT_CANNON_MIN_DMG = 50;
    public static final short AIRCRAFT_CANNON_MAX_DMG = 150;
    public static final float AIRCRAFT_REFUEL_RANGE = 1.5f;
    public static final float AIRCRAFT_REFUEL_APPROACH_BUFFER = 10.0f;          //Within this distance, 
    public static final float AIRCRAFT_REFUEL_APPROACH_SPEED_MULT = 0.65f;      //If a tanker aircraft is faster than the aircraft it is going to refuel, it will slow down to this percentage of the refuelee's speed. 
    public static final float AIRCRAFT_FUEL_TRANSFER_PER_TICK = 500.0f;         //KG
    
    public static final float SENTRY_DETONATE_NUKE_CHANCE = 0.0f;               //Chance that sentry guns will detonate a nuclear warhead upon shooting it, as opposed to merely destroying the missile.
    public static final int GROUND_UNIT_RELOAD_TIME = MS_PER_SEC * 15;
    public static final int SENTRY_RELOAD_TIME = MS_PER_SEC * 6;
    public static final int ARTILLERY_GUN_RELOAD_TIME = MS_PER_SEC * 15;
    
    public static final int INCOME_PER_RANK = 50;
    public static final int XP_PER_RANK = 2000;

    public static final float LAND_UNIT_SPEED = 65f;
    public static final float LAND_UNIT_COMBAT_SPEED = 25f;
    public static final float LANDING_CRAFT_SPEED = 65.0f;
    
    //Hourly Maintenance
    
    public static final float STORED_UNIT_MAINTENANCE_MULTIPLIER = 0.0f;
    public static final int OFFLINE_MAINTENANCE_COST = 0;
    public static final int ONLINE_MAINTENANCE_COST = 25;
    public static final int EXTRACTOR_MAINTENANCE_COST = 25;
    public static final int MIN_MAINT_COST_AIRCRAFT = 150;
    public static final int WEIGHT_PER_MAINTENANCE_AIRCRAFT = 300;
    public static final float STEALTH_MAINT_MULT = 1.5f;
    public static final int MISSILE_MAINTENANCE = 0;
    public static final int TORPEDO_MAINTENANCE = 0;
    public static final int MISSILE_ICBM_MAINTENANCE = 250;
    public static final int INTERCEPTOR_ABM_MAINTENANCE = 200;
    public static final int INTERCEPTOR_MAINTENANCE = 0;
    public static final int TANK_MAINTENANCE_COST = 45;
    public static final int CARGO_TRUCK_MAINTENANCE_COST = 45;
    public static final int INFANTRY_MAINTENANCE_COST = 45;
    public static final short AIRCRAFT_NEGLECT_HP = 50;
    
    public static final int FIGHTER_HOURLY_MAINTENANCE = 50;
    public static final int BOMBER_HOURLY_MAINTENANCE = 75;
    public static final int STEALTH_FIGHTER_HOURLY_MAINTENANCE = 150;
    public static final int STEALTH_BOMBER_HOURLY_MAINTENANCE = 175;
    public static final int ATTACK_AIRCRAFT_HOURLY_MAINTENANCE = 50;
    public static final int AWACS_HOURLY_MAINTENANCE = 50;
    public static final int CARGO_PLANE_HOURLY_MAINTENANCE = 50;
    public static final int REFUELER_HOURLY_MAINTENANCE = 50;
    public static final int MULTI_ROLE_HOURLY_MAINTENANCE = 50;
    public static final int SSB_HOURLY_MAINTENANCE = 50;
    
    public static final int GetAircraftMaintenanceCost(EntityType type)
    {
        switch(type)
        {
            case FIGHTER: return FIGHTER_HOURLY_MAINTENANCE;
            case BOMBER: return BOMBER_HOURLY_MAINTENANCE;
            case STEALTH_FIGHTER: return STEALTH_FIGHTER_HOURLY_MAINTENANCE;
            case STEALTH_BOMBER: return STEALTH_BOMBER_HOURLY_MAINTENANCE;
            case ATTACK_AIRCRAFT: return ATTACK_AIRCRAFT_HOURLY_MAINTENANCE;
            case AWACS: return AWACS_HOURLY_MAINTENANCE;
            case CARGO_PLANE: return CARGO_PLANE_HOURLY_MAINTENANCE;
            case REFUELER: return REFUELER_HOURLY_MAINTENANCE;
            case MULTI_ROLE: return MULTI_ROLE_HOURLY_MAINTENANCE;
            case SSB: return SSB_HOURLY_MAINTENANCE;
        }
        
        return Integer.MAX_VALUE;
    }
    
    public static final float FIGHTER_RANGE = 2400f;
    public static final float BOMBER_RANGE = 14000f;
    public static final float STEALTH_FIGHTER_RANGE = 2000f;
    public static final float STEALTH_BOMBER_RANGE = 11000f;
    public static final float ATTACK_AIRCRAFT_RANGE = 1800f;
    public static final float AWACS_RANGE = 7400f;
    public static final float CARGO_PLANE_RANGE = 8900f;
    public static final float REFUELER_RANGE = 3600f;
    public static final float MULTI_ROLE_RANGE = 2400f;
    public static final float SSB_RANGE = 10000f;
    
    public static final float GetAircraftRange(EntityType type)
    {
        switch(type)
        {
            case FIGHTER: return FIGHTER_RANGE;
            case BOMBER: return BOMBER_RANGE;
            case STEALTH_FIGHTER: return STEALTH_FIGHTER_RANGE;
            case STEALTH_BOMBER: return STEALTH_BOMBER_RANGE;
            case ATTACK_AIRCRAFT: return ATTACK_AIRCRAFT_RANGE;
            case AWACS: return AWACS_RANGE;
            case CARGO_PLANE: return CARGO_PLANE_RANGE;
            case REFUELER: return REFUELER_RANGE;
            case MULTI_ROLE: return MULTI_ROLE_RANGE;
            case SSB: return SSB_RANGE;
        }
        
        return -1f;
    }
    
    public static final float FIGHTER_SPEED = 2000f;
    public static final float BOMBER_SPEED = 1050f;
    public static final float STEALTH_FIGHTER_SPEED = 2000f;
    public static final float STEALTH_BOMBER_SPEED = 11000f;
    public static final float ATTACK_AIRCRAFT_SPEED = 1800f;
    public static final float AWACS_SPEED = 1050f;
    public static final float CARGO_PLANE_SPEED = 1050f;
    public static final float REFUELER_SPEED = 1050f;
    public static final float MULTI_ROLE_SPEED = 2000f;
    public static final float SSB_SPEED = 2500f;
    
    public static final float GetAircraftSpeed(EntityType type)
    {
        switch(type)
        {
            case FIGHTER: return FIGHTER_SPEED;
            case BOMBER: return BOMBER_SPEED;
            case STEALTH_FIGHTER: return STEALTH_FIGHTER_SPEED;
            case STEALTH_BOMBER: return STEALTH_BOMBER_SPEED;
            case ATTACK_AIRCRAFT: return ATTACK_AIRCRAFT_SPEED;
            case AWACS: return AWACS_SPEED;
            case CARGO_PLANE: return CARGO_PLANE_SPEED;
            case REFUELER: return REFUELER_SPEED;
            case MULTI_ROLE: return MULTI_ROLE_SPEED;
            case SSB: return SSB_SPEED;
        }
        
        return -1f;
    }
    
    //Max HP defs.
    
    public static final short SHIPYARD_MAX_HP = 15000;
    public static final short TANK_MAX_HP = 3000;
    
    //TODO
    public static final List<ResourceType> GetTankResourceTypes(EntityType type)
    {
        switch(type)
        {
            case SPAAG:
            case MBT: return Arrays.asList(ResourceType.STEEL, ResourceType.FUEL);
            case HOWITZER: return Arrays.asList(ResourceType.STEEL, ResourceType.FUEL);
            case MISSILE_TANK: return Arrays.asList(ResourceType.STEEL, ResourceType.FUEL);
            case SAM_TANK: return Arrays.asList(ResourceType.STEEL, ResourceType.FUEL);
        }
        
        return null;
    }
    
    public static final short LANDING_CRAFT_HP = 100;
    public static final short MISSILE_SITE_HP = 1500;
    public static final short ICBM_SILO_HP = 5000;
    public static final short SAM_SITE_HP = 1500;
    public static final short ABM_SILO_HP = 5000;
    public static final short SENTRY_GUN_HP = 300;
    public static final short AIRBASE_HP = 3000;
    public static final short COMMAND_POST_HP = 5000;
    public static final short WAREHOUSE_HP = 2500;
    public static final short BANK_HP = 1000;
    public static final short ARTILLERY_GUN_HP = 1500;
    
    public static final int MISSILE_SLOT_UPGRADE_COST = 10000;
    public static final int RELOAD_TIME_BASE = MS_PER_SEC * 120;
    public static final int RELOAD_TIME_STAGE_1 = MS_PER_SEC * 60;
    public static final int RELOAD_TIME_STAGE_2 = MS_PER_SEC * 30;
    public static final int RELOAD_TIME_STAGE_3 = MS_PER_SEC * 15;
    
    public static final long STRUCTURE_RESOURCE_CAPACITY = 500000;
    public static final long LAND_UNIT_RESOURCE_CAPACITY = 150000;
    public static final long DISTRIBUTOR_RESOURCE_CAPACITY = 750000;
    
    public static final long BANK_CAPACITY = 1000000;
    
    //TODO
    public static final List<ResourceType> MISSILE_SITE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> ARTILLERY_GUN_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY, ResourceType.OIL);
    public static final List<ResourceType> SAM_SITE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> ICBM_SILO_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> ABM_SILO_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> ORE_MINE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> SENTRY_GUN_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY, ResourceType.OIL);
    public static final List<ResourceType> AIRBASE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY, ResourceType.STEEL, ResourceType.FUEL, ResourceType.ELECTRONICS);
    public static final List<ResourceType> RADAR_STATION_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES);
    public static final List<ResourceType> WAREHOUSE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> ARMORY_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> BARRACKS_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES, ResourceType.MACHINERY);
    public static final List<ResourceType> SCRAPYARD_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES);
    public static final List<ResourceType> BASIC_STRUCTURE_TYPES = Arrays.asList(ResourceType.ELECTRICITY, ResourceType.CONSTRUCTION_SUPPLIES);
    public static final List<ResourceType> INFANTRY_TYPES = Arrays.asList(ResourceType.MEDICINE, ResourceType.FOOD);
    
    //Structure cost
    public static final Map<ResourceType, Long> MISSILE_SITE_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)3000), entry(ResourceType.STEEL, (long)(30000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ARTILLERY_GUN_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)3000), entry(ResourceType.STEEL, (long)(30000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SAM_SITE_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)3000), entry(ResourceType.STEEL, (long)(30000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ICBM_SILO_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)10000), entry(ResourceType.CONCRETE, (long)(75000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(25000/T3_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ABM_SILO_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)10000), entry(ResourceType.CONCRETE, (long)(75000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(25000/T3_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ORE_MINE_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)5000), entry(ResourceType.STEEL, (long)(50000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SENTRY_GUN_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)750), entry(ResourceType.STEEL, (long)(5000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(2500/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> WATCH_TOWER_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)750), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(7500/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> AIRBASE_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)10000), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(75000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.STEEL, (long)(25000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> COMMAND_POST_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)5000), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(50000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RADAR_STATION_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)10000), entry(ResourceType.ELECTRONICS, (long)(25000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CONCRETE, (long)(75000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> WAREHOUSE_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)7500), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(75000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ARMORY_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)5000), entry(ResourceType.STEEL, (long)(25000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CONCRETE, (long)(25000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> BARRACKS_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)5000), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(50000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SCRAPYARD_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)7500), entry(ResourceType.CONCRETE, (long)(50000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(25000/T3_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> DISTRIBUTOR_STRUCTURE_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)5000), entry(ResourceType.MACHINERY, (long)5000), entry(ResourceType.CONSTRUCTION_SUPPLIES, (long)(45000/T2_SUBSTITUTION_VALUE_KG)));
    
    public static final Map<ResourceType, Long> RESOURCE_COST_GRANARY = Map.ofEntries(entry(ResourceType.WEALTH, (long)30000), entry(ResourceType.LUMBER, (long)(150000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CONCRETE, (long)(150000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_OIL_REFINERY = Map.ofEntries(entry(ResourceType.WEALTH, (long)30000), entry(ResourceType.IRON, (long)(300000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_FOUNDRY = Map.ofEntries(entry(ResourceType.WEALTH, (long)30000), entry(ResourceType.CONCRETE, (long)(300000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_POWER_PLANT = Map.ofEntries(entry(ResourceType.WEALTH, (long)30000), entry(ResourceType.IRON, (long)(150000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CONCRETE, (long)(150000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_CONSTRUCTION_YARD = Map.ofEntries(entry(ResourceType.WEALTH, (long)60000), entry(ResourceType.STEEL, (long)(150000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(150000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_LABORATORY = Map.ofEntries(entry(ResourceType.WEALTH, (long)60000), entry(ResourceType.STEEL, (long)(150000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CROPS, (long)(150000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_MACHINE_SHOP = Map.ofEntries(entry(ResourceType.WEALTH, (long)60000), entry(ResourceType.STEEL, (long)(150000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(150000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_LITHOGRAPHY_PLANT = Map.ofEntries(entry(ResourceType.WEALTH, (long)90000), entry(ResourceType.MACHINERY, (long)150000), entry(ResourceType.ELECTRICITY, (long)(150000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_NUCLEAR_POWER_PLANT = Map.ofEntries(entry(ResourceType.WEALTH, (long)90000), entry(ResourceType.CONCRETE, (long)(600000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(150000/T3_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> RESOURCE_COST_ENRICHMENT_FACILITY = Map.ofEntries(entry(ResourceType.WEALTH, (long)90000), entry(ResourceType.CONCRETE, (long)(600000/T1_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(300000/T3_SUBSTITUTION_VALUE_KG)));
    
    public static Map<ResourceType, Long> GetProcessorCost(ResourceType type)
    {
        switch(type)
        {
            case FOOD: return RESOURCE_COST_GRANARY;
            case STEEL: return RESOURCE_COST_FOUNDRY;
            case FUEL: return RESOURCE_COST_OIL_REFINERY;
            case CONSTRUCTION_SUPPLIES: return RESOURCE_COST_CONSTRUCTION_YARD;
            case ELECTRICITY: return RESOURCE_COST_POWER_PLANT;
            case NUCLEAR_ELECTRICITY: return RESOURCE_COST_NUCLEAR_POWER_PLANT;
            case MACHINERY: return RESOURCE_COST_MACHINE_SHOP;
            case MEDICINE: return RESOURCE_COST_LABORATORY;
            case ELECTRONICS: return RESOURCE_COST_LITHOGRAPHY_PLANT;
            case ENRICHED_URANIUM: return RESOURCE_COST_ENRICHMENT_FACILITY;
        }
        
        return null;
    }
    
    //Experience
    public static final float XP_LOSS_VS_GAIN_MULTIPLIER = 0.5f;                
    public static final int CHAMPION_XP_GAIN = 10000;
    public static final int INFANTRY_KILLED_XP = 75;                            //The amount of XP a player gains when they kill an infantry unit.
    public static final int INFANTRY_LOST_XP = (int)(INFANTRY_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int AIRCRAFT_KILLED_XP = 150;
    public static final int AIRCRAFT_LOST_XP = (int)(AIRCRAFT_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int TANK_KILLED_XP = 50;
    public static final int TANK_LOST_XP = (int)(TANK_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int ICBM_SHOOTDOWN_XP = 125;
    public static final int STRUCTURE_BUILT_XP = 50;
    public static final int STRUCTURE_KILLED_XP = STRUCTURE_BUILT_XP;
    public static final int STRUCTURE_LOST_XP = (int)(STRUCTURE_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int TANK_PURCHASED_XP = 50;
    
    public static final int CARGO_TRUCK_PURCHASED_XP = 50;
    
    public static final int WAR_WON_XP = 300;
    public static final int WAR_LOST_XP = (int)(WAR_WON_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int SHIPYARD_KILLED_XP = 1000;
    public static final int SHIPYARD_LOST_XP = (int)(SHIPYARD_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int SHIPYARD_UPGRADE_XP = 1000;
    public static final float KM_TRAVELED_PER_XP = 0.05f;
    public static final int FRIGATE_KILLED_XP = 75;
    public static final int DESTROYER_KILLED_XP = 125;
    public static final int AMPHIB_KILLED_XP = 200;
    public static final int CARGO_SHIP_KILLED_XP = 100;
    public static final int FLEET_OILER_KILLED_XP = 150;
    public static final int SUPER_CARRIER_KILLED_XP = 300;
    public static final int ATTACK_SUB_KILLED_XP = 125;
    public static final int SSBN_KILLED_XP = 400;
    public static final int FRIGATE_LOST_XP = (int)(FRIGATE_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int DESTROYER_LOST_XP = (int)(DESTROYER_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int AMPHIB_LOST_XP = (int)(AMPHIB_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int CARGO_SHIP_LOST_XP = (int)(CARGO_SHIP_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int FLEET_OILER_LOST_XP = (int)(FLEET_OILER_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int SUPER_CARRIER_LOST_XP = (int)(SUPER_CARRIER_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int ATTACK_SUB_LOST_XP = (int)(ATTACK_SUB_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    public static final int SSBN_LOST_XP = (int)(SSBN_KILLED_XP * XP_LOSS_VS_GAIN_MULTIPLIER);
    
    //TODO: Add different aircraft XP.
    public static int GetXPGainForKill(MapEntity entity)
    {
        if(entity instanceof Structure)
            return STRUCTURE_KILLED_XP;
        else if(entity instanceof Airplane)
            return AIRCRAFT_KILLED_XP;
        else if(entity instanceof Infantry)
            return INFANTRY_KILLED_XP;
        else if(entity instanceof NavalVessel vessel)
        {
            switch(vessel.GetEntityType())
            {
                case FRIGATE: return Defs.FRIGATE_KILLED_XP;
                case DESTROYER: return Defs.DESTROYER_KILLED_XP;
                case AMPHIB: return Defs.AMPHIB_KILLED_XP;
                case CARGO_SHIP: return Defs.CARGO_SHIP_KILLED_XP;
                case FLEET_OILER: return Defs.FLEET_OILER_KILLED_XP;
                case SUPER_CARRIER: return Defs.SUPER_CARRIER_KILLED_XP;
                case ATTACK_SUB: return Defs.ATTACK_SUB_KILLED_XP;
                case SSBN: return Defs.SSBN_KILLED_XP;
            }
        }
        else if(entity instanceof Tank)
            return TANK_KILLED_XP;
        else if(entity instanceof Shipyard)
            return SHIPYARD_KILLED_XP;
        
        return -1;
    }
    
    //TODO: Add different aircraft XP.
    public static int GetXPLossForKill(MapEntity entity)
    {
        if(entity instanceof Structure)
            return STRUCTURE_LOST_XP;
        else if(entity instanceof Airplane)
            return AIRCRAFT_LOST_XP;
        else if(entity instanceof Infantry)
            return INFANTRY_LOST_XP;
        else if(entity instanceof NavalVessel vessel)
        {
            switch(vessel.GetEntityType())
            {
                case FRIGATE: return Defs.FRIGATE_LOST_XP;
                case DESTROYER: return Defs.DESTROYER_LOST_XP;
                case AMPHIB: return Defs.AMPHIB_LOST_XP;
                case CARGO_SHIP: return Defs.CARGO_SHIP_LOST_XP;
                case FLEET_OILER: return Defs.FLEET_OILER_LOST_XP;
                case SUPER_CARRIER: return Defs.SUPER_CARRIER_LOST_XP;
                case ATTACK_SUB: return Defs.ATTACK_SUB_LOST_XP;
                case SSBN: return Defs.SSBN_LOST_XP;
            }
        }
        else if(entity instanceof Tank)
            return TANK_LOST_XP;
        else if(entity instanceof Shipyard)
            return SHIPYARD_LOST_XP;
        
        return -1;
    }
    
    public static final int SHIPYARD_UPGRADE_WEALTH_COST = 300000;
    public static final byte MAX_SHIPYARD_CAPACITY = 6;
    
    public static final int TANK_BUILD_TIME = MS_PER_HOUR * 1;
    public static final float BATTLE_TANK_FIRING_RANGE = 3.5f;
    public static final int BATTLE_TANK_RELOAD_TIME = GROUND_UNIT_RELOAD_TIME;
    public static final short EFFECTIVE_MIN_DMG = 15;
    public static final short EFFECTIVE_MAX_DMG = 50;
    public static final short AVERAGE_MIN_DMG = 10;
    public static final short AVERAGE_MAX_DMG = 30;
    public static final short INEFFECTIVE_MIN_DMG = 3;
    public static final short INEFFECTIVE_MAX_DMG = 15;
    public static final float NON_ANTISHIP_DAMAGE_MULTIPLIER = 0.5f;            //Non anti-ship missiles that hit a ship have their damage reduced by this amount.
   
    public static final int RUBBLE_EXPIRY = MS_PER_DAY * 10;
    public static final int LOOT_EXPIRY = MS_PER_DAY;
    public static final float LOOT_DROP_MIN = 0.02f;
    public static final float LOOT_DROP_MAX = 0.10f;
    public static final float LOOT_COMBINE_RADIUS = 0.65f;                      //In KM, as all distance stats are. Loots of the same type will combine if they are closer together than this.

    public static final Map<ResourceType, Long> CARGO_TRUCK_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)17000), entry(ResourceType.STEEL, (long)(17000/T2_SUBSTITUTION_VALUE_KG)));

    public static final Map<ResourceType, Long> SHIPYARD_REPAIR_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)100000), entry(ResourceType.MACHINERY, (long)(25000/T3_SUBSTITUTION_VALUE_KG)), entry(ResourceType.CONCRETE, (long)(75000/T1_SUBSTITUTION_VALUE_KG)));

    public static final int KG_FISSILE_MATERIAL_PER_KT = 1;
    public static final int ELECTRONICS_COST_SONOBUOY = 500;
    public static final int ELECTRONICS_COST_ECM = 100;
    public static final int ELECTRONICS_COST_STEALTH = 400;
    public static final int ELECTRONICS_COST_TRACKING = 50;
    public static final int ELECTRONICS_COST_ANTISHIP = 300;
    public static final int FUEL_COST_TRACKING_SABOT = 100;
    public static final int FUEL_COST_ANTISHIP_SABOT = 500;
    public static final int ELECTRONICS_COST_EMP_SABOT = 500;
    public static final int SABOT_KINETIC_YIELD = 500;
    public static final int STEEL_COST_SABOT = 1000;
    public static final int MACHINERY_COST_ANTISUBMARINE = 100;
    public static final int MACHINERY_PER_MIRV = 1000;
    public static final int ELECTRONICS_COST_PER_ACCURACY = 1;
    public static final float MISSILE_ACCURACY_COST_THRESHOLD = 0.5f;           //Threshold after which accuracy will require electronics.
    public static final float MISSILE_RANGE_COST_THRESHOLD = 1000f;
    public static final int MISSILE_SPEED_COST_THRESHOLD = 2000;
    public static final int MISSILE_YIELD_COST_THRESHOLD = 500;
    
    public static final int ARTILLERY_GUN_RELOAD = MS_PER_SEC * 15;
    public static final float RANDOM_MOVEMENT_VARIATION = 0.075f;               //This exists to stop moveable entities from stacking directly on top of eachother. 
    
    //Ship values. 
    
    public static final int SONAR_PING_COOLDOWN = MS_PER_MIN * 5;
    public static final int ARTILLERY_RELOAD = MS_PER_SEC * 15;
    public static final int AIRCRAFT_LAUNCH_COOLDOWN = 0; //MS_PER_SEC * 30;
    public static final int AIRBASE_DEFAULT_SLOTS = 3;
    public static final int AIRBASE_CAPACITY_UPGRADE_AMOUNT = 1;
    public static final Map<ResourceType, Long> AIRBASE_CAPACITY_UPGRADE_COST = Map.ofEntries(entry(ResourceType.WEALTH, 750L));
    public static final int MAX_AIRBASE_CAPACITY = 150;
    public static final short AIRBASE_HP_PER_SLOT_UPGRADE = 150;
    public static final float AIRCRAFT_LANDING_DISTANCE = 0.8f;                 //Distance from an airbase at which a landing aircraft will be "pulled in."
    public static final int SUBMARINE_SUBMERGE_TIME = MS_PER_MIN * 30;
    public static final int SUBMARINE_ICBM_RELOAD = MS_PER_MIN * 3;
    public static final int SHIP_ARTILLERY_DEFAULT_RELOAD = MS_PER_SEC * 30;
    public static final int SHIP_ARTILLERY_SLOTS_PER_GUN = 12;
    public static final float SHIP_REFUEL_DISTANCE = 0.3f;
    public static final float SHIP_REFUEL_RATE_PER_TICK_TONS = 50f;
    public static final float SHIP_MAX_FUEL_EFFICIENCY = 10f;
    public static final float SHIP_MIN_FUEL_EFFICIENCY = 1f;
    public static final float SHIP_MIN_FUEL_EFFICIENCY_BUFFER = 0.5f;           //Ships are speed-limited to prevent them from going faster than the minimum fuel efficiency would allow. This is a cheap way to make sure no such accelerate to get lower than the minimum efficiency.
    public static final float SHIP_FUEL_EFFICIENCY_SLOPE = -0.056f;
    public static final float BASELINE_SPEED_FUEL_EFFICIENCY = 37.04f;          //In KPH.
    public static final float SONAR_RANGE = 25f;
    public static final float CONTACT_BEARING_MAX_RANGE = 200.0f;
    public static final float CONTACT_BEARING_CHANCE = 0.3f;                    //A 50% chance of getting a contact bearing each minute.
    public static final int RADAR_SCAN_COOLDOWN = MS_PER_MIN * 30;
    public static final float CIWS_HIT_CHANCE = 0.2f;
    public static final float CIWS_RANGE = 2.2f;
    public static final int CIWS_RELOAD_TIME = MS_PER_SEC * 3;
    public static final float MINIMUM_CIWS_ACCURACY = 0.25f;
    public static final float TORPEDO_HOMING_CHANCE = 0.2f;                     //Torpedo has a 30% chance of acquiring a target each second.
    public static final float TORPEDO_HOMING_RANGE = 2.778f;                    //The maximum distance at which a homing torpedo can acquire a target.
    public static final float TORPEDO_TURN_RATE = 0.1f;
    public static final float TORPEDO_DETONATE_DISTANCE = 0.025f;
    public static final float TORPEDO_LOSE_TARGET_CHANCE = 0.1f;
    public static final float REACHED_GEOTARGET_DISTANCE = 0.3f;
    public static final float TORPEDO_DESTROY_CHANCE = 0.5f;                    //The chance for a torpeo to be destroyed by another torpedo if it is inside the blast radius.
    public static final int NUKE_TORPEDO_VS_SUBMARINE_MULTIPLIER = 2;
    public static final int UNDER_ATTACK_TIME = MS_PER_SEC * 30;
    
    public static float GetKPHFromKnots(float fltKnots)
    {
        return fltKnots * KPH_PER_KNOT;
    }
    
    //Missile stuff.
    
    public static final float COMMAND_POST_BUSTER_DMG_MULT = 7.0f;
    public static final float ARTILLERY_TO_SHIP_DMG_MULT = 5.0f;
    public static final float ARTILLERY_GENERAL_DMG_MULTIPLIER = 2.5f;
    public static final float ANTI_SHIP_DMG_MULT = 1.0f;
    public static final float NUKE_CITY_DMG_MULT = 1.5f;
    
    //Server-manager and history-related stuff. 
    
    public static final long PLAYER_RECORD_AGE_THRESHOLD = MS_PER_DAY * 30;     //Players alive for 30 days or more will be recorded.
    public static final long PLAYER_RECORD_WEALTH_THRESHOLD = 5000000;          //Players younger than the age threshold will be recorded anyway if they are worth more than this.
    public static final long ALLIANCE_RECORD_AGE_THRESHOLD = MS_PER_DAY * 30;
    public static final int ALLIANCE_RECORD_MEMBER_THRESHOLD = 15;              //Once an alliance hits this number of players, it will be recorded regardless of age.
    
    public static final Map<ResourceType, Long> TANK_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)125000), entry(ResourceType.STEEL, (long)(125000/T2_SUBSTITUTION_VALUE_KG)));
        
    public static final float LOOT_DROP_CHANCE = 0.25f;
    public static final int WEAKLING_VALUE_THRESHOLD = 1000;
    public static final float COMMAND_POST_RADIUS = 1.0f;
    
    public static final float ATTACK_ICBM_SILO_MAD_CHANCE = 0.04f;
    public static final float NUKE_ICBM_SILO_MAD_CHANCE = 0.1f;
    public static final float NUKE_CIVILIAN_STRUCTURE_MAD_CHANCE = 0.0f;
    
    //Naval stats.
    public static final float NAVAL_SPEED = 65f;
    public static final float NAVAL_RANGE = 4000f;
    public static final short ATTACK_SUB_MAX_HP = 900;
    public static final int NAVAL_MISSILE_RELOAD = MS_PER_SEC * 15;
    public static final int ATTACK_SUB_MISSILE_SLOT_COUNT = 36;
    public static final int NAVAL_TORPEDO_RELOAD = MS_PER_SEC * 30;
    public static final int ATTACK_SUB_TORPEDO_SLOT_COUNT = 18;
    
    public static final short SSBN_MAX_HP = 1500;
    public static final int SSBN_MISSILE_SLOT_COUNT = 72;
    public static final int SSBN_TORPEDO_SLOT_COUNT = 36;
    public static final int SSBN_ICBM_RELOAD = MS_PER_SEC * 90;
    public static final int SSBN_ICBM_SLOT_COUNT = 6;
    
    public static final short FRIGATE_MAX_HP = 1200;
    public static final int FRIGATE_MISSILE_SLOT_COUNT = 36;
    public static final int FRIGATE_TORPEDO_SLOT_COUNT = 18;
    public static final float FRIGATE_SONAR_RANGE = 40;
    public static final float FRIGATE_SCANNER_RANGE = 200f;
    public static final int FRIGATE_SENTRY_COUNT = 2;
    public static final int NAVAL_INTERCEPTOR_RELOAD = MS_PER_SEC * 20;
    public static final int FRIGATE_INTERCEPTOR_SLOT_COUNT = 36;
    
    public static final short DESTROYER_MAX_HP = 2400;
    public static final int DESTROYER_MISSILE_SLOT_COUNT = 72;
    public static final int DESTROYER_TORPEDO_SLOT_COUNT = 36;
    public static final float DESTROYER_SONAR_RANGE = 80;
    public static final float DESTROYER_SCANNER_RANGE = 300f;
    public static final int DESTROYER_SENTRY_COUNT = 4;
    public static final int DESTROYER_INTERCEPTOR_SLOT_COUNT = 72;
    
    public static final short CARGO_SHIP_MAX_HP = 2700;
    public static final int CARGO_SHIP_CAPACITY = 50000000;
    
    public static final short FLEET_OILER_MAX_HP = 1500;
    public static final int FLEET_OILER_SENTRY_COUNT = 4;
    
    public static final short AMPHIB_MAX_HP = 2700;
    public static final int AMPHIB_SENTRY_COUNT = 4;
    public static final int AMPHIB_CAPACITY = 10000000;
    public static final int AMPHIB_AIRCRAFT_RELOAD_TIME = MS_PER_SEC * 30;
    public static final int AMPHIB_AIRCRAFT_SLOT_COUNT = 18;
    
    public static final short SUPER_CARRIER_MAX_HP = 5400;
    public static final float SUPER_CARRIER_SCANNER_RANGE = 400f;
    public static final int SUPER_CARRIER_AIRCRAFT_RELOAD_TIME = MS_PER_SEC * 15;
    public static final int SUPER_CARRIER_AIRCRAFT_SLOT_COUNT = 72;
    public static final int FRIGATE_HOURLY_MAINTENANCE = 100;
    public static final int DESTROYER_HOURLY_MAINTENANCE = 200;
    public static final int AMPHIB_HOURLY_MAINTENANCE = 300;
    public static final int CARGO_SHIP_HOURLY_MAINTENANCE = 150;
    public static final int FLEET_OILER_HOURLY_MAINTENANCE = 300;
    public static final int SUPER_CARRIER_HOURLY_MAINTENANCE = 400;
    public static final int ATTACK_SUB_HOURLY_MAINTENANCE = 150;
    public static final int SSBN_HOURLY_MAINTENANCE = 500;
    
    public static final float GetFuelUsagePerTick(int lMS, float fltDesiredMaxRange, float fltSpeed)
    {
        //Time (in hours) to travel full range at naval speed
        float fltTimeToEmptyHours = fltDesiredMaxRange/fltSpeed;

        //Convert hours to milliseconds
        float fltTimeToEmptyMS = fltTimeToEmptyHours * MS_PER_HOUR;

        //Fuel used in this tick
        return lMS/fltTimeToEmptyMS;
    }
    
    public static final int GetNavalMaintenanceCost(EntityType type)
    {
        switch(type)
        {
            case FRIGATE: return Defs.FRIGATE_HOURLY_MAINTENANCE;
            case DESTROYER: return Defs.DESTROYER_HOURLY_MAINTENANCE;
            case AMPHIB: return Defs.AMPHIB_HOURLY_MAINTENANCE;
            case CARGO_SHIP: return Defs.CARGO_SHIP_HOURLY_MAINTENANCE;
            case FLEET_OILER: return Defs.FLEET_OILER_HOURLY_MAINTENANCE;
            case SUPER_CARRIER: return Defs.SUPER_CARRIER_HOURLY_MAINTENANCE;
            case ATTACK_SUB: return Defs.ATTACK_SUB_HOURLY_MAINTENANCE;
            case SSBN: return Defs.SSBN_HOURLY_MAINTENANCE;
        }
        
        return Integer.MAX_VALUE;
    }
    
    public static final int FRIGATE_BUILD_TIME = MS_PER_MIN * 30;
    public static final int DESTROYER_BUILD_TIME = MS_PER_MIN * 60;
    public static final int AMPHIB_BUILD_TIME = MS_PER_MIN * 72;
    public static final int CARGO_SHIP_BUILD_TIME = MS_PER_MIN * 45;
    public static final int FLEET_OILER_BUILD_TIME = MS_PER_MIN * 65;
    public static final int SUPER_CARRIER_BUILD_TIME = MS_PER_MIN * 120;
    public static final int ATTACK_SUB_BUILD_TIME = MS_PER_MIN * 45;
    public static final int SSBN_BUILD_TIME = MS_PER_MIN * 90;
    
    public static final int GetNavalBuildTime(EntityType type)
    {
        switch(type)
        {
            case FRIGATE: return Defs.FRIGATE_BUILD_TIME;
            case DESTROYER: return Defs.DESTROYER_BUILD_TIME;
            case AMPHIB: return Defs.AMPHIB_BUILD_TIME;
            case CARGO_SHIP: return Defs.CARGO_SHIP_BUILD_TIME;
            case FLEET_OILER: return Defs.FLEET_OILER_BUILD_TIME;
            case SUPER_CARRIER: return Defs.SUPER_CARRIER_BUILD_TIME;
            case ATTACK_SUB: return Defs.ATTACK_SUB_BUILD_TIME;
            case SSBN: return Defs.SSBN_BUILD_TIME;
        }
        
        return Integer.MAX_VALUE;
    }
    
    public static final Map<ResourceType, Long> FRIGATE_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)90000), entry(ResourceType.STEEL, (long)(80000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(10000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> DESTROYER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)150000), entry(ResourceType.STEEL, (long)(130000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(20000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> AMPHIB_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)300000), entry(ResourceType.STEEL, (long)(250000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(50000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> CARGO_SHIP_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)120000), entry(ResourceType.STEEL, (long)(100000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(20000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> FLEET_OILER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)240000), entry(ResourceType.STEEL, (long)(200000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(40000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SUPER_CARRIER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)600000), entry(ResourceType.STEEL, (long)(550000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.ENRICHED_URANIUM, (long)1000));
    public static final Map<ResourceType, Long> ATTACK_SUB_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)130000), entry(ResourceType.STEEL, (long)(100000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(20000/T3_SUBSTITUTION_VALUE_KG)), entry(ResourceType.OIL, (long)(10000/T1_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SSBN_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)500000), entry(ResourceType.STEEL, (long)(400000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.MACHINERY, (long)(75000/T3_SUBSTITUTION_VALUE_KG)), entry(ResourceType.ENRICHED_URANIUM, (long)500));
    
    public static final Map<ResourceType, Long> GetNavalBuildCost(EntityType type)
    {
        switch(type)
        {
            case FRIGATE: return Defs.FRIGATE_BUILD_COST;
            case DESTROYER: return Defs.DESTROYER_BUILD_COST;
            case AMPHIB: return Defs.AMPHIB_BUILD_COST;
            case CARGO_SHIP: return Defs.CARGO_SHIP_BUILD_COST;
            case FLEET_OILER: return Defs.FLEET_OILER_BUILD_COST;
            case SUPER_CARRIER: return Defs.SUPER_CARRIER_BUILD_COST;
            case ATTACK_SUB: return Defs.ATTACK_SUB_BUILD_COST;
            case SSBN: return Defs.SSBN_BUILD_COST;
        }
        
        return null;
    }
    
    public static final int AIRCRAFT_INTERCEPTOR_RELOAD_TIME = MS_PER_SEC * 8;
    public static final int AIRCRAFT_MISSILE_RELOAD_TIME = MS_PER_SEC * 5;
    public static final int FIGHTER_BUILD_TIME = MS_PER_MIN * 30;
    public static final int BOMBER_BUILD_TIME = MS_PER_MIN * 45;
    public static final int STEALTH_FIGHTER_BUILD_TIME = MS_PER_MIN * 45;
    public static final int STEALTH_BOMBER_BUILD_TIME = MS_PER_MIN * 60;
    public static final int AWACS_BUILD_TIME = MS_PER_MIN * 40;
    public static final int REFUELER_BUILD_TIME = MS_PER_MIN * 40;
    public static final int ATTACK_AIRCRAFT_BUILD_TIME = MS_PER_MIN * 35;
    public static final int SSB_BUILD_TIME = MS_PER_MIN * 50;
    public static final int CARGO_PLANE_BUILD_TIME = MS_PER_MIN * 25;
    public static final int MULTI_ROLE_BUILD_TIME = MS_PER_MIN * 38;
    public static final int FIGHTER_INTERCEPTOR_SLOTS = 18;
    public static final int STEALTH_FIGHTER_INTERCEPTOR_SLOTS = 12;
    public static final int BOMBER_MISSILE_SLOTS = 24;
    public static final int STEALTH_BOMBER_MISSILE_SLOTS = 15;
    public static final int MULTI_ROLE_MISSILE_SLOTS = 9;
    public static final int MULTI_ROLE_INTERCEPTOR_SLOTS = 9;
    public static final int SSB_MISSILE_SLOTS = 18;
    public static final int CARGO_PLANE_CAPACITY = 125000;
    
    public static final Map<ResourceType, Long> FIGHTER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)27000), entry(ResourceType.STEEL, (long)(10000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(50000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> BOMBER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)42000), entry(ResourceType.STEEL, (long)(30000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(7000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> STEALTH_FIGHTER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)37000), entry(ResourceType.ELECTRONICS, (long)(25000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(5000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> STEALTH_BOMBER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)74000), entry(ResourceType.ELECTRONICS, (long)(67000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(7000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> ATTACK_AIRCRAFT_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)24000), entry(ResourceType.STEEL, (long)(12000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(5000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> AWACS_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)50000), entry(ResourceType.ELECTRONICS, (long)(40000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(10000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> REFUELER_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)45000), entry(ResourceType.STEEL, (long)(20000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(25000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> CARGO_PLANE_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)30000), entry(ResourceType.STEEL, (long)(25000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(5000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> SSB_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)55000), entry(ResourceType.STEEL, (long)(45000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(10000/T2_SUBSTITUTION_VALUE_KG)));
    public static final Map<ResourceType, Long> MULTI_ROLE_BUILD_COST = Map.ofEntries(entry(ResourceType.WEALTH, (long)25000), entry(ResourceType.STEEL, (long)(20000/T2_SUBSTITUTION_VALUE_KG)), entry(ResourceType.FUEL, (long)(5000/T2_SUBSTITUTION_VALUE_KG)));
    
    public static final Map<ResourceType, Long> GetAircraftBuildCost(EntityType type)
    {
        switch(type)
        {
            case FIGHTER: return Defs.FIGHTER_BUILD_COST;
            case BOMBER: return Defs.BOMBER_BUILD_COST;
            case STEALTH_BOMBER: return Defs.STEALTH_BOMBER_BUILD_COST;
            case STEALTH_FIGHTER: return Defs.STEALTH_FIGHTER_BUILD_COST;
            case ATTACK_AIRCRAFT: return Defs.ATTACK_AIRCRAFT_BUILD_COST;
            case AWACS: return Defs.AWACS_BUILD_COST;
            case REFUELER: return Defs.REFUELER_BUILD_COST;
            case CARGO_PLANE: return Defs.CARGO_PLANE_BUILD_COST;
            case SSB: return Defs.SSB_BUILD_COST;
            case MULTI_ROLE: return Defs.MULTI_ROLE_BUILD_COST;
        }
        
        return null;
    }
    
    public static final float XP_PER_WEALTH_SPENT = 0.01f;
    
    public static final int GetPurchaseXP(Map<ResourceType, Long> costs)
    {
        if(costs.get(ResourceType.WEALTH) != null)
        {
            return (int)(costs.get(ResourceType.WEALTH) * XP_PER_WEALTH_SPENT);
        }
        
        return 0;
    }
}   
