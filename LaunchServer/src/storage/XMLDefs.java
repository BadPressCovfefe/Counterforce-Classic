/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

/**
 *
 * @author tobster
 */
public class XMLDefs
{
    public static final String GAME = "Game";
    
    public static final String PORT = "Port";
    public static final String SERVER_EMAIL = "ServerEmail";
    public static final String VARIANT = "Variant";
    public static final String DEBUG_MODE = "DebugMode";
    public static final String STARTING_WEALTH = "StartingWealth";
    public static final String RESPAWN_WEALTH = "RespawnWealth";
    public static final String RESPAWN_TIME = "RespawnTime";
    public static final String RESPAWN_PROTECTION_TIME = "RespawnProtectionTime";
    public static final String NOOB_PROTECTION_TIME = "NoobProtectionTime";
    public static final String HOURLY_WEALTH = "HourlyWealth";
    public static final String CMS_SYSTEM_COST = "CMSSystemCost";
    public static final String SAM_SYSTEM_COST = "SAMSystemCost";
    public static final String CMS_STRUCTURE_COST = "CMSStructureCost";
    public static final String NCMS_STRUCTURE_COST = "NCMSStructureCost";
    public static final String SAM_STRUCTURE_COST = "SAMStructureCost";
    public static final String SENTRY_GUN_STRUCTURE_COST = "SentryGunStructureCost";
    public static final String ORE_MINE_STRUCTURE_COST = "OreMineStructureCost";
    public static final String INTERCEPTOR_BASE_HIT_CHANCE = "InterceptorBaseHitChance";
    public static final String RUBBLE_MIN_VALUE = "RubbleMinValue";
    public static final String RUBBLE_MAX_VALUE = "RubbleMaxValue";
    public static final String RUBBLE_MIN_TIME = "RubbleMinTime";
    public static final String RUBBLE_MAX_TIME = "RubbleMaxTime";
    public static final String STRUCTURE_SEPARATION = "StructureSeparation";
    public static final String PLAYER_BASE_HP = "PlayerBaseHP";
    public static final String STRUCTURE_BASE_HP = "StructureBaseHP";
    public static final String STRUCTURE_BOOT_TIME = "StructureBootTime";
    public static final String INITIAL_CSM_SLOTS = "InitialCMSSlots";
    public static final String INITIAL_SAM_SLOTS = "InitialSAMSlots";
    public static final String REQUIRED_ACCURACY = "RequiredAccuracy";
    public static final String MIN_RADIATION_TIME = "MinRadiationTime";
    public static final String MAX_RADIATION_TIME = "MaxRadiationTime";
    public static final String MISSILE_SLOT_UPGRADE_BASE_COST = "MissileSlotUpgradeBaseCost";
    public static final String MISSILE_SLOT_UPGRADE_COUNT = "MissileSlotUpgradeCount";
    public static final String RESALE_VALUE = "ResaleValue";
    public static final String DECOMMISSION_TIME = "DecommissionTime";
    public static final String RELOAD_TIME_BASE = "ReloadTimeBase";
    public static final String RELOAD_TIME_STAGE1 = "ReloadTimeStage1";
    public static final String RELOAD_TIME_STAGE2 = "ReloadTimeStage2";
    public static final String RELOAD_TIME_STAGE3 = "ReloadTimeStage3";
    public static final String RELOAD_STAGE1_COST = "ReloadStage1Cost";
    public static final String RELOAD_STAGE2_COST = "ReloadStage2Cost";
    public static final String RELOAD_STAGE3_COST = "ReloadStage3Cost";
    public static final String REPAIR_SALVAGE_DISTANCE = "RepairSalvageDistance";
    public static final String MISSILE_SITE_MAINTENANCE_COST = "MissileSiteMaintenanceCost";
    public static final String SAM_SITE_MAINTENANCE_COST = "SAMSiteMaintenanceCost";
    public static final String SENTRY_GUN_MAINTENANCE_COST = "SentryGunMaintenanceCost";
    public static final String ORE_MINE_MAINTENANCE_COST = "OreMineMaintenanceCost";
    public static final String HEALTH_INTERVAL = "HealthInterval";
    public static final String RADIATION_INTERVAL = "RadiationInterval";
    public static final String PLAYER_REPAIR_COST = "PlayerRepairCost";
    public static final String STRUCTURE_REPAIR_COST = "StructureRepairCost";
    public static final String AWOL_TIME = "AWOLTime";
    public static final String REMOVE_TIME = "RemoveTime";
    public static final String NUKE_UPGRADE_COST = "NukeUpgradeCost";
    public static final String ALLIANCE_COOLOFF_TIME = "AllianceCooloffTime";
    public static final String MISSILE_SPEED_COST = "MissileSpeedCost";
    public static final String MISSILE_RANGE_COST = "MissileRangeCost";
    public static final String MISSILE_BLAST_RADIUS_COST = "MissileBlastRadiusCost";
    public static final String MISSILE_BLAST_RADIUS_NUKES_COST = "MissileBlastRadiusNukesCost";
    public static final String MISSILE_MAX_DAMAGE_COST = "MissileMaxDamageCost";
    public static final String MISSILE_NUCLEAR_COST = "MissileNuclearCost";
    public static final String MISSILE_TRACKING_COST = "MissileTrackingCost";
    public static final String MISSILE_ECM_COST = "MissileECMCost";
    public static final String INTERCEPTOR_SPEED_COST_ABOVE_THRESHOLD = "InterceptorSpeedCostAboveThreshold";
    public static final String INTERCEPTOR_SPEED_COST_BELOW_THRESHOLD = "InterceptorSpeedCostBelowThreshold";
    public static final String INTERCEPTOR_SPEED_COST_THRESHOLD = "InterceptorSpeedCostThreshold";
    public static final String INTERCEPTOR_RANGE_COST_ABOVE_THRESHOLD = "InterceptorRangeCostAboveThreshold";
    public static final String INTERCEPTOR_RANGE_COST_BELOW_THRESHOLD = "InterceptorRangeCostBelowThreshold";
    public static final String INTERCEPTOR_RANGE_COST_THRESHOLD = "InterceptorRangeCostThreshold";
    public static final String EMP_CHANCE = "EMPChance";
    public static final String EMP_RADIUS_MULTIPLIER = "EMPRadiusMultiplier";
    public static final String ECM_INTERCEPTOR_CHANCE_REDUCTION = "ECMInterceptorChanceReduction";
    public static final String MANUAL_INTERCEPTOR_CHANCE_INCREASE = "ManualInterceptorChanceIncrease";
    public static final String SENTRY_GUN_RELOAD_TIME = "SentryGunReloadTime";
    public static final String SENTRY_GUN_RANGE = "SentryGunRange";
    public static final String SENTRY_GUN_HIT_CHANCE = "SentryGunHitChance";
    public static final String MISSILE_SPEED_INDEX_COST = "MissileSpeedIndexCost";
    public static final String MISSILE_SPEED_INDEX_COST_POW = "MissileSpeedIndexCostPow";
    public static final String MISSILE_RANGE_INDEX_COST = "MissileRangeIndexCost";
    public static final String MISSILE_RANGE_INDEX_COST_POW = "MissileRangeIndexCostPow";
    public static final String MISSILE_BLAST_RADIUS_INDEX_COST = "MissileBlastRadiusIndexCost";
    public static final String MISSILE_BLAST_RADIUS_INDEX_COST_POW = "MissileBlastRadiusIndexCostPow";
    public static final String NUKE_BLAST_RADIUS_INDEX_COST = "NukeBlastRadiusIndexCost";
    public static final String NUKE_BLAST_RADIUS_INDEX_COST_POW = "NukeBlastRadiusIndexCostPow";
    public static final String MISSILE_MAX_DAMAGE_INDEX_COST = "MissileMaxDamageIndexCost";
    public static final String MISSILE_MAX_DAMAGE_INDEX_COST_POW = "MissileMaxDamageIndexCostPow";
    public static final String INTERCEPTOR_SPEED_INDEX_COST = "InterceptorSpeedIndexCost";
    public static final String INTERCEPTOR_SPEED_INDEX_COST_POW = "InterceptorSpeedIndexCostPow";
    public static final String INTERCEPTOR_RANGE_INDEX_COST = "InterceptorRangeIndexCost";
    public static final String INTERCEPTOR_RANGE_INDEX_COST_POW = "InterceptorRangeIndexCostPow";
    public static final String MISSILE_PREP_TIME_PER_MAGNITUDE = "MissilePrepTimePerMagnitude";
    public static final String INTERCEPTOR_PREP_TIME_PER_MAGNITUDE = "InterceptorPrepTimePerMagnitude";
    public static final String HOURLY_BONUS_DIPLOMATIC_PRESENCE = "HourlyBonusDiplomaticPresence";
    public static final String HOURLY_BONUS_POLITICAL_ENGAGEMENT = "HourlyBonusPoliticalEngagement";
    public static final String HOURLY_BONUS_DEFENDER_OF_THE_NATION = "HourlyBonusDefenderOfTheNation";
    public static final String HOURLY_BONUS_NUCLEAR_SUPERPOWER = "HourlyBonusNuclearSuperpower";
    public static final String HOURLY_BONUS_WEEKLY_KILLS_BATCH = "HourlyBonusWeeklyKillsBatch";
    public static final String HOURLY_BONUS_SURVIVOR = "HourlyBonusSurvivor";
    public static final String HOURLY_BONUS_HIPPY = "HourlyBonusHippy";
    public static final String HOURLY_BONUS_PEACE_MAKER = "HourlyBonusPeaceMaker";
    public static final String HOURLY_BONUS_WAR_MONGER = "HourlyBonusWarMonger";
    public static final String HOURLY_BONUS_LONE_WOLF = "HourlyBonusLoneWolf";
    public static final String LONE_WOLF_DISTANCE = "LoneWolfDistance";
    public static final String RADAR_STATION_MAINTENANCE_COST = "RadarStationMaintenanceCost";
    public static final String RADAR_STATION_STRUCTURE_COST = "RadarStationStructureCost";
    public static final String RADAR_RANGE_BASE = "RadarRangeBase";
    public static final String RADAR_RANGE_STAGE1 = "RadarRangeStage1";
    public static final String RADAR_RANGE_STAGE2 = "RadarRangeStage2";
    public static final String RADAR_RANGE_STAGE3 = "RadarRangeStage3";
    public static final String RADAR_RANGE_STAGE1_COST = "RadarRangeStage1Cost";
    public static final String RADAR_RANGE_STAGE2_COST = "RadarRangeStage2Cost";
    public static final String RADAR_RANGE_STAGE3_COST = "RadarRangeStage3Cost";   
    public static final String ENTITY_DEFAULT_LOS = "EntityDefaultLOS";
    
    public static final String MINOR_BANNED_APP = "MinorBannedApp";
    public static final String MAJOR_BANNED_APP = "MajorBannedApp";
    public static final String SIGNATURE = "Signature";
    public static final String DESCRIPTION = "Description";
    
    public static final String ID = "ID";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String NAME = "Name";
    public static final String ASSET_ID = "AssetID";
    
    public static final String GAME_STATS = "GameStats";
    public static final String NEXT_PLAYER_ID = "NextPlayerID";
    public static final String USERS = "Users";
    public static final String USER = "User";
    public static final String IMEI = "IMEI";
    public static final String GOOGLE_ID = "GoogleID";
    public static final String PLAYERID = "PlayerID";
    public static final String BAN_STATE = "BanState";
    public static final String NEXT_BAN_TIME = "NextBanTime";
    public static final String BAN_DURATION_REMAINING = "BanDurationRemaining";
    public static final String BAN_REASON = "BanReason";
    public static final String LAST_IP = "LastIP";
    public static final String LAST_CONNECTION_MOBILE = "LastConnectionMobile";
    public static final String LAST_CHECKED = "LastChecked";
    public static final String LAST_CHECK_FAILED = "LastCheckFailed";
    public static final String CHECK_API_FAILED = "CheckAPIFailed";
    public static final String PROSCRIBED = "Proscribed";
    public static final String CHECK_FAIL_CODE = "CheckFailCode";
    public static final String PROFILE_MATCH = "ProfileMatch";
    public static final String BASIC_INTEGRITY = "BasicIntegrity";
    public static final String APPROVED = "Approved";
    public static final String EXPIRED = "Expired";
    public static final String DEVICE_HASH = "DeviceHash";
    public static final String APP_LIST_HASH = "AppListHash";
    public static final String REPORTS = "Reports";
    public static final String REPORT = "Report";
    public static final String TIME_START = "TimeStart";
    public static final String TIME_END = "TimeEnd";
    public static final String MESSAGE = "Message";
    public static final String IS_MAJOR = "IsMajor";
    public static final String LEFT_ID = "LeftID";
    public static final String RIGHT_ID = "RightID";
    public static final String TIMES = "Times";
    
    public static final String ALLIANCES = "Alliances";
    public static final String ALLIANCE = "Alliance";
    
    public static final String TREATIES = "Treaties";
    public static final String TREATY = "Treaty";
    public static final String WARS = "Wars";
    public static final String WAR = "War";
    public static final String AFFILIATIONS = "Affiliations";
    public static final String AFFILIATION = "Affiliation";
    public static final String AFFILIATION_REQUESTS = "AffiliationRequests";
    public static final String AFFILIATION_REQUEST = "AffiliationRequest";
    public static final String SURRENDER_PROPOSALS = "SurrenderProposals";
    public static final String SURRENDER_PROPOSAL = "SurrenderProposal";
    public static final String ALLIANCE1 = "Alliance1";
    public static final String ALLIANCE2 = "Alliance2";
    
    public static final String PLAYERS = "Players";
    public static final String PLAYER = "Player";
    public static final String POSITION = "Position";
    public static final String HP = "HP";
    public static final String MAX_HP = "MaxHP";
    public static final String AVATAR = "Avatar";
    public static final String WEALTH = "Wealth";
    public static final String LAST_SEEN = "LastSeen";
    public static final String STATE_CHANGE = "StateChange";
    public static final String ALLIANCE_ID = "AllianceID";
    public static final String FLAGS = "Flags";
    public static final String FLAGS1 = "Flags1";
    public static final String FLAGS2 = "Flags2";
    public static final String KILLS = "Kills";
    public static final String DEATHS = "Deaths";
    public static final String OFFENCE_SPENDING = "OffenceSpending";
    public static final String DEFENCE_SPENDING = "DefenceSpending";
    public static final String DAMAGE_INFLICTED = "DamageInflicted";
    public static final String DAMAGE_RECEIVED = "DamageReceived";
    
    public static final String COST = "Cost";
    public static final String COSTS = "Costs";
    public static final String RANGE = "Range";
    public static final String RELOAD_TIME = "ReloadTime";
    
    public static final String MISSILE_BLAST_RADIUS = "MissileBlastRadius";
    public static final String NUKE_BLAST_RADIUS = "NukeBlastRadius";
    public static final String MISSILE_MAX_DAMAGE = "MissileMaxDamage";
    public static final String INTERCEPTOR_RANGE = "InterceptorRange";
    
    public static final String MISSILE_TYPE = "MissileType";
    public static final String SPEED = "Speed";
    public static final String NUCLEAR = "Nuclear";
    public static final String TRACKING = "Tracking";
    public static final String ECM = "ECM";
    public static final String CHARGE_OWNER_TIME = "ChargeOwnerTime";
    public static final String PURCHASABLE = "Purchasable";
    public static final String MISSILE_SPEED = "MissileSpeed"; //"Added" by Corbin
    public static final String MISSILE_COST = "MissileCost"; //Added by Corbin
    public static final String BLAST_RADIUS = "BlastRadius";
    public static final String MISSILE_RANGE = "MissileRange";
    public static final String MAX_DAMAGE = "Damage";
    public static final String PREP_TIME = "PrepTime"; //Added by Corbin
    public static final String ICBM = "ICBM";   //Added by Corbin
    public static final String ICBM_SILO = "ICBMSilo";   //Added by Corbin
    public static final String ICBMS = "ICBMs";   //Added by Corbin
    public static final String EMP_RADIUS = "EMPRadius";    //Added by Corbin
    
    public static final String INTERCEPTOR_TYPE = "InterceptorType";
    public static final String INTERCEPTOR_COST = "InterceptorCost"; //Added by Corbin
    public static final String INTERCEPTOR_HIT_CHANCE = "HitChance"; //Added by Corbin
    public static final String INTERCEPTOR_SPEED = "InterceptorSpeed"; //"Added" by Corbin
    
    public static final String TYPE = "Type";
    public static final String STRUCTURE_TYPE = "StructureType";
    public static final String RESOURCE_TYPE = "ResourceType";
    public static final String TARGET = "Target";
    public static final String ORIGIN = "Origin";
    public static final String OWNER_ID = "OwnerID";
    public static final String TIME_TO_TARGET = "TimeToTarget";
    
    public static final String MISSILES = "Missiles";
    public static final String MISSILE = "Missile";
    
    public static final String INTERCEPTORS = "Interceptors";
    public static final String INTERCEPTOR = "Interceptor";
    public static final String TARGET_ID = "TargetID";
    public static final String PLAYER_LAUNCHED = "PlayerLaunched";
    
    public static final String MISSILE_SITES = "MissileSites";
    public static final String MISSILE_SITE = "MissileSite";
    public static final String STATE = "State";
    public static final String STATE_TIME = "StateTime";
    
    public static final String SAM_SITES = "SAMSites";
    public static final String SAM_SITE = "SAMSite";
    public static final String MODE = "Mode";
    
    public static final String ORE_MINES = "OreMines";
    public static final String ORE_MINE = "OreMine";
    public static final String GENERATE_TIME = "GenerateTime";
    
    public static final String SENTRY_GUNS = "SentryGuns";
    public static final String SENTRY_GUN = "SentryGun";
    
    public static final String LOOTS = "Loots";
    public static final String LOOT = "Loot";
    public static final String VALUE = "Value";
    public static final String EXPIRY = "Expiry";
    
    public static final String CHEMICALS = "Chemicals";
    public static final String CHEMICAL = "Chemical";
    
    public static final String RADIATIONS = "Radiations";
    public static final String RADIATION = "Radiation";
    public static final String RADIUS = "Radius";
    
    public static final String MISSILE_SYSTEM = "MissileSystem";
    public static final String TANK_MISSILE_SYSTEM = "TankMissileSystem";
    public static final String INTERCEPTOR_SYSTEM = "InterceptorSystem";
    public static final String TORPEDO_SYSTEM = "TorpedoSystem";
    public static final String AIRCRAFT_SYSTEM = "AircraftSystem";
    public static final String ARTILLERY_SYSTEMS = "ArtillerySystems";
    public static final String ARTILLERY_SYSTEM = "ArtillerySystem";
    public static final String SENTRY_SYSTEM = "SentrySystem";
    public static final String ICBM_SYSTEM = "ICBMSystem";
    public static final String RELOAD_REMAINING = "ReloadRemaining";
    public static final String SLOT_COUNT = "SlotCount";
    public static final String SLOTS = "Slots";
    public static final String SLOT = "Slot";
    public static final String NUMBER = "Number";
    
    public static final String EMULATORS = "Emulators";
    public static final String MULTI_ACCOUNTORS = "MultiAccountors";
    public static final String SPOOF_APPS = "SpoofApps";
    public static final String IP = "IP";
    public static final String EMULATOR = "Emulator";
    public static final String MULTI_ACCOUNTOR = "MultiAccountor";
    public static final String SPOOF_APP = "SpoofApp";
    
    public static final String KILLS1 = "Kills1";
    public static final String DEATHS1 = "Deaths1";
    public static final String OFFENCE_SPENDING1 = "OffenceSpending1";
    public static final String DEFENCE_SPENDING1 = "DefenceSpending1";
    public static final String DAMAGE_INFLICTED1 = "DamageInflicted1";
    public static final String DAMAGE_RECEIVED1 = "DamageReceived1";
    public static final String INCOME1 = "Income1";
    public static final String KILLS2 = "Kills2";
    public static final String DEATHS2 = "Deaths2";
    public static final String OFFENCE_SPENDING2 = "OffenceSpending2";
    public static final String DEFENCE_SPENDING2 = "DefenceSpending2";
    public static final String DAMAGE_INFLICTED2 = "DamageInflicted2";
    public static final String DAMAGE_RECEIVED2 = "DamageReceived2";
    public static final String INCOME2 = "Income2";
    
    public static final String RADAR_STATIONS = "RadarStations";
    public static final String RADAR_STATION = "RadarStation";
    public static final String RADAR_RANGE = "RadarRange";
    public static final String ABM_SILO_STRUCTURE_COST = "ABMSiloStructureCost";
    public static final String ANTI_BALLISTIC = "IsABM";
    public static final String ENGAGEMENT_SPEED = "EngageSpeed";
    public static final String BASE_RADAR_BOOST = "BaseAccuracyBoost";
    public static final String RADAR_BOOST_DISTANCE = "AccuracyBoostDistance";
    public static final String ACCURACY_BOOST = "AccuracyBoost";
    public static final String RADAR_BOOST_UPGRADE_COST = "AccuracyBonusUpgradeCost";
    public static final String SENTRY_GUN_RANGE_UPGRADE_COST = "SentryRangeUpgradeCost";
    public static final String RANK = "Rank";
    public static final String EXPERIENCE = "Experience";
    public static final String TOTAL_KILLS = "TotalKills";
    public static final String TOTAL_DEATHS = "TotalDeaths";
    public static final String BOUNTY = "Bounty";
    public static final String TOKEN = "FirebaseToken";
    public static final String STEALTH = "Stealth";
    public static final String RADAR_DETECTION_RANGE = "RadarDetectionRange";
    public static final String PLAYER_DETECTION_RANGE = "PlayerDetectionRange";
    public static final String AIRCRAFT_DETECTION_RANGE = "AircraftDetectionRange";
    
    public static final String YIELD = "Yield";
    public static final String STEALTH_PER_TICK_CHANCE = "StealthPerTickChance"; //Stealth missiles have a chance every tick to become permanently visible to radar. This field in the XML is that chance.
    public static final String DETECTED = "Detected"; //Indicates whether the entity in question has been detected by radar. Currently only used for stealth missiles.
    public static final String NEWB = "Newb";
    public static final String JIHADIST = "Suicider";
    public static final String JOIN_TIME = "JoinTimeMS";
    public static final String SUICIDE_COUNT = "SuicideCount";
    public static final String JIHADIST_THRESHOLD = "JihadistThreshold";
    public static final String SUPPORT_TIER = "SupportTier";
    public static final String INTENSITY = "Intensity";
    
    public static final String ORE_GENERATE_RADIUS = "OreGenerateRadius";
    public static final String ORE_COLLECT_RADIUS = "OreCollectRadius";
    public static final String ORE_COMPETE_RADIUS = "OreCompeteRadius";
    public static final String MAX_ORE_VALUE = "MaxOreValue";
    public static final String ORE_MINE_GENERATE_TIME = "OreMineGenerateTime";
    public static final String ORE_MIN_EXPIRY = "OreMinExpiry";
    public static final String ORE_MAX_EXPIRY = "OreMaxExpiry";
    public static final String OFFLINE_UPKEEP_COST = "OfflineUpkeepCost";
    public static final String MISSILE_UPKEEP_COST = "MissileUpkeepCost";
    public static final String ICBM_UPKEEP_COST = "ICBMUpkeepCost";
    public static final String SILO_UPKEEP_PER_UPGRADE = "SiloUpkeepPerUpgrade";
    
    //Farm strings.
    public static final String FARM_STRUCTURE_COST = "FarmStructureCost";
    public static final String FARM_MAINTENANCE_COST = "FarmMaintenanceCost";
    public static final String FARM_BASE_HP = "FarmBaseHP";
    public static final String FARM_GENERATE_RADIUS = "FarmGenerateRadius";
    public static final String FARM_GROWTH_TIME = "FarmGrowthTime";
    public static final String CROP_MIN_EXPIRY = "CropMinExpiry";
    public static final String CROP_MAX_EXPIRY = "CropMaxExpiry";
    public static final String CROP_MAX_VALUE = "CropMaxValue"; 
    public static final String MIN_CROP_COUNT = "MinCropCount";
    public static final String MAX_CROP_COUNT = "MaxCropCount";
    public static final String FARM_SPACING = "FarmSpacing";
    public static final String FARMS = "Farms";
    public static final String FARM = "Farm";
    public static final String FARM_WORK_RADIUS = "FarmWorkRadius";
    
    public static final String ORE_MINE_SPACING = "OreMineSpacing";
    
    //CommandPost strings
    public static final String COMMAND_POST = "CommandPost";
    public static final String COMMAND_POST_SHELTER_RADIUS = "CommandPostShelterRadius";
    public static final String COMMAND_POST_HP_UPGRADE_COST = "CommandPostHPUpgradeCost";
    public static final String COMMAND_POST_HP_UPGRADE_HP = "CommandPostHPUpgradeHP";
    public static final String COMMAND_POST_MAX_HP_UPGRADE = "CommandPostMaxHPUpgrade";
    public static final String COMMAND_POST_STRUCTURE_COST = "CommandPostStructureCost";
    public static final String COMMAND_POST_BASE_HP = "CommandPostBaseHP";
    public static final String COMMAND_POST_MAINTENANCE_COST = "CommandPostMaintenanceCost";
    
    //ICBM/ABM Silo Upgrade Strings
    public static final String SILO_BOOT_UPGRADE_COST = "SiloBootUpgradeCost";
    public static final String SILO_BOOT_UPGRADE_TIME = "SiloBootUpgradeTime";
    public static final String SILO_MAX_BOOT_UPGRADE = "SiloMaxBootUpgrade";
    public static final String BOOT_TIME = "SiloBootTime";
    
    //Solar Panel strings.
    public static final String SOLAR_PANEL = "SolarPanel";
    public static final String SPANEL_STRUCTURE_COST = "SPanelStructureCost";
    public static final String SPANEL_MAINTENANCE_COST = "SPanelMaintenanceCost";
    public static final String SPANEL_STRUCTURE_HP = "SPanelStructureHP";
    public static final String SPANEL_GENERATE_RADIUS = "SPanelGenerateRadius";
    public static final String SPANEL_GENERATE_TIME = "SPanelGenerateTime";
    public static final String ELEC_MIN_EXPIRY = "ElecMinExpiry";
    public static final String ELEC_MAX_EXPIRY = "ElecMaxExpiry";
    public static final String ELEC_MIN_VALUE = "ElecMinValue";
    public static final String ELEC_MAX_VALUE = "ElecMaxValue";
    public static final String SPANEL_SPACING = "SPanelSpacing";
    
    public static final String START_TIME = "WarStartTime";
    
    public static final String SOLAR_PANELS = "SolarPanels";
    public static final String COMMAND_POSTS = "CommandPosts";
    
    public static final String MAX_RADAR_ACCURACY = "MaxRadarAccuracy";
    
    public static final String IMPACT_COORD = "ImpactCoord";
    
    public static final String AIR_LAUNCHED = "AirLaunched";
    public static final String GROUND_LAUNCHED = "GroundLaunched";
    public static final String TANK_LAUNCHED = "TankLaunched";
    public static final String SHIP_LAUNCHED = "ShipLaunched";
    public static final String SUBMARINE_LAUNCHED = "SubmarineLaunched";
    public static final String NAVAL_LAUNCHED = "NavalLaunched";
    public static final String TORPEDO = "Torpedo";
    public static final String TORPEDOES = "Torpedoes";
    public static final String TORPEDO_TYPE = "TorpedoType";
    public static final String TORPEDO_TYPES = "TorpedoTypes";
    public static final String HOMING = "Homing";
    public static final String AIRCRAFTS = "Aircrafts";
    public static final String AIRCRAFT = "Aircraft";
    public static final String FIGHTERS = "Fighters";
    public static final String FIGHTER = "Fighter";
    public static final String BOMBERS = "Bombers";
    public static final String BOMBER = "Bomber";
    public static final String MOVE_ORDERS = "MoveOrders";
    public static final String COORDINATES = "Coordinates";
    public static final String COORDINATE = "Coordinate";
    public static final String COORDINATE_CHAIN = "CoordinateChain";
    public static final String ORDER = "Order";
    public static final String MAX_FUEL = "MaxFuel";
    public static final String CURRENT_FUEL = "CurrentFuel";
    public static final String HOMEBASE_ID = "HomeBaseID";
    public static final String FLYING = "Flying";
    public static final String AIRBASES = "Airbases";
    public static final String AIRBASE = "Airbase";
    public static final String AIRBASE_CAPACITY = "AirbaseCapacity";
    public static final String AIRBASE_BASE_HP = "AirbaseBaseHP";
    
    public static final String AIRBASE_STRUCTURE_COST = "AirbaseStructureCost";
    public static final String AIRBASE_MAINTENANCE_COST = "AirbaseMaintenanceCost";
    public static final String AIRBASE_BASE_CAPACITY = "AirbaseBaseCapacity";
    
    public static final String BOMBER_RELOAD_TIME = "BomberReloadTime";
    public static final String BOMBER_MISSILE_SLOTS = "BomberMissileSlots";
    public static final String BOMBER_FUEL_CAPACITY = "BomberFuelCapacity";
    public static final String BOMBER_SPEED_KPH = "BomberSpeedKPH";
    public static final String BOMBER_TURN_RATE = "BomberTurnRate";    
    public static final String BOMBER_COST = "BomberCost";
    
    public static final String FIGHTER_RELOAD_TIME = "FighterReloadTime";
    public static final String FIGHTER_COST = "FighterCost";
    public static final String FIGHTER_FUEL_CAPACITY = "FighterFuelCapacity";
    public static final String FIGHTER_SPEED_KPH = "FighterSpeedKPH";
    public static final String FIGHTER_TURN_RATE = "FighterTurnRate";
    public static final String FIGHTER_INTERCEPTOR_SLOTS = "FighterInterceptorSlots";
    
    public static final String STRIKE_FIGHTER_COST = "StrikeFighterCost";
    public static final String STRIKE_FIGHTER_SLOTS = "StrikeFighterSlots";
    public static final String STRIKE_FIGHTER_FUEL_CAPACITY = "StrikeFighterFuelCapacity";
    public static final String FAST_BOMBER_COST = "FastBomberCost";
    public static final String FAST_BOMBER_FUEL_CAPACITY = "FastBomberFuelCapacity";
    public static final String FAST_BOMBER_SPEED_KPH = "FastBomberSpeedKPH";
    public static final String FAST_BOMBER_MISSILE_SLOTS = "FastBomberMissileSlots";
    public static final String STEALTH_UPGRADE_COST = "StealthUpgradeCost";
    
    public static final String TARGET_TYPE = "TargetType";
    public static final String LAST_BEARING = "LastBearing";
    public static final String AIRSPACE_DISTANCE = "AirspaceDistance";
    public static final String AIRCRAFT_CHAFF_COUNT = "AircraftChaffCount";
    public static final String CHAFF_COUNT = "ChaffCount";
    public static final String FIGHTER_CANNON_RANGE = "FighterCannonRange";
    public static final String FIGHTER_CANNON_ACCURACY = "FighterCannonAccuracy";
    public static final String AUTO_RETURN = "AutoReturn";
    
    public static final String OFFENSE_VALUE = "OffenseValue";
    public static final String DEFENSE_VALUE = "DefenseValue";
    public static final String NEUTRAL_VALUE = "NeutralValue";
    
    public static final String AIRCRAFT_ROLE = "AircraftRole";
    
    public static final String STORED_HELICOPTERS = "StoredHelicopters";
    public static final String STORED_HELICOPTER = "StoredHelicopter";
    public static final String STORED_AIRCRAFTS = "StoredAircrafts";
    public static final String STORED_LANDUNITS = "StoredLandUnits";
    public static final String STORED_AIRCRAFT = "StoredAircraft";
    public static final String STORED_LANDUNIT = "StoredLandUnit";
    
    public static final String BANK_BASE_CAPACITY = "BankBaseCapacity";
    public static final String BANK_BASE_HP = "BankBaseHP";
    public static final String BANK_STRUCTURE_COST = "BankStructureCost";
    public static final String BANK_MAINTENANCE_COST = "BankMaintenanceCost";
    public static final String BANK_UPGRADE_COST = "BankUpgradeCost";
    public static final String BANK_MAX_CAPACITY = "BankMaxCapacity";
    public static final String BANKS = "Banks";
    public static final String BANK = "Bank";
    public static final String MARKETS = "Markets";
    public static final String MARKET = "Market";
    public static final String MAX_CAPACITY = "MaxCapacity";
    public static final String BANK_INTEREST_RATE = "BankInterestRate";
    
    public static final String LOG_DEPOT_LINK_RANGE = "LogDepotLinkRange";
    public static final String LOG_DEPOT_MAINTENANCE_COST = "LogDepptMaintenanceCost";
    public static final String LOG_DEPOT_COLLECT_RADIUS = "LogDepotCollectRadius";
    public static final String LOG_DEPOT_STRUCTURE_COST = "LogDepotStructureCost";
    public static final String LOG_DEPOT_BASE_HP = "LogDepotBaseHP";
    public static final String LINKED_TO_ID = "LinkedToID";
    public static final String WAREHOUSE = "Warehouse";
    public static final String WAREHOUSES = "Warehouses";
    public static final String MAX_LOG_DEPOT_CAPACITY = "MaxLogDepotCapacity";
    
    public static final String ACCURACY = "Accuracy";
    
    public static final String SERVER_NAME = "ServerName";
    public static final String SERVER_DESCRIPTION = "ServerDescription";
    
    public static final String CREATED_BY_ID = "CreatedByID";
    public static final String FUZE_MODE = "Airburst";
    
    public static final String RETALIATING = "Retaliating";
    public static final String RETALIATE_TIME = "RetaliateTime";
    public static final String MIRV_TYPE = "MirvType";
    public static final String MIRV_COUNT = "MirvCount";
    public static final String MIRV = "MIRV";
    public static final String ICBM_TYPE = "ICBMType";
    public static final String ICBM_TARGETS = "ICBMTargets";
    public static final String TARGET_DATA = "TargetData";
    public static final String MAX_SPEED = "MaxSpeed";
    
    public static final String MISSILE_FACTORYS = "MissileFactorys";
    public static final String MISSILE_FACTORY = "MissileFactory";
    public static final String TRANSMIT_COOLDOWN = "TransmitCooldown";
    public static final String MISSILE_FACTORY_COOLDOWN = "MissileFactoryCooldown";
    public static final String MISSILE_FACTORY_MAINTENANCE_COST = "MissileFactoryMaintenanceCost";
    public static final String MISSILE_FACTORY_STRUCTURE_COST = "MissileFactoryStructureCost";
    
    public static final String BLUEPRINT = "Blueprint";
    public static final String BLUEPRINTS = "Blueprints";
    public static final String BLUEPRINT_COST = "BlueprintCost";
    public static final String BLUEPRINT_EXPIRY = "BlueprintExpiry";
    public static final String BLUEPRINT_BUILD_DISTANCE = "BlueprintBuildDistance";
    
    public static final String TAX_RATE = "TaxRate";
    public static final String WARS_WON = "WarsWon";
    public static final String WARS_LOST = "WarsLost";
    public static final String ALLIANCE_DISBAND_COUNT = "DisbandCount";
    public static final String FOUNDER_NAME = "FounderName";
    public static final String AFFILIATIONS_BROKEN = "AffiliationsBroken";
    public static final String ICBM_COUNT = "ICBMCount";
    public static final String ABM_COUNT = "ABMCount";
    public static final String FOUNDED_TIME = "FoundedTime";
    
    public static final String AIRDROPS_USED_TODAY = "AirdropsUsedToday";
    public static final String AIRDROP_COOLDOWN = "AirdropCooldown";
    
    public static final String AIRCRAFT_TYPE = "AircraftType";
    public static final String AIRCRAFT_TYPES = "AircraftTypes";
    
    public static final String CARGO_SYSTEM = "CargoSystem";
    public static final String TONS = "Tons";
    public static final String CAPACITY = "Capacity";
    
    public static final String DESIGN_NAME = "DesignName";
    public static final String FUEL_EFFICIENCY = "FuelEfficiency";
    public static final String FLARE_CAPACITY = "FlareCapacity";
    public static final String FLARES_REMAINING = "FlaresRemaining";
    public static final String FUEL_TRANSFER = "CanTransferFuel";
    public static final String GROUND_ATTACK = "CanGroundAttack";
    public static final String ELECTRONIC_WARFARE = "HasEWSuite";
    public static final String ELECTRONIC_WARFARE_RELOAD = "EWReloadRemaining";
    public static final String HAS_SCANNER = "HasScanner";
    public static final String HAS_CANNON = "HasCannon";
    public static final String HAS_AMPHIB = "Amphibious";
    public static final String HAS_SUPPORT = "Support";
    public static final String WEIGHT = "Weight";
    public static final String CARGO_CAPACITY = "CargoCapacity";
    public static final String MISSILE_COUNT = "MissileCount";
    public static final String INTERCEPTOR_COUNT = "InterceptorCount";
    public static final String DESIGNER_ID = "DesignerID";
    public static final String PUBLIC = "Public";
    public static final String LAST_PURCHASED = "LastPurchased";
    public static final String UNITS_PURCHASED = "UnitsPurchased";
    public static final String AIRDROP = "Airdrop";
    public static final String AIRDROPS = "Airdrops";
    public static final String LAUNCHED_BY_ID = "LaunchedByID";
    public static final String TARGET_ENTITY = "TargetEntity";
    public static final String GEO_TARGET = "GeoTarget";
    
    public static final String ARMORIES = "Armories";
    public static final String ARMORY = "Armory";
    public static final String INFANTRIES = "Infantries";
    public static final String INFANTRY = "Infantry";
    public static final String PRODUCING = "Producing";
    public static final String PACK_TIME = "PackTime";
    public static final String PACKING = "Packing";
    public static final String MOVABLE = "Movable";
    public static final String STORED_INFANTRIES = "StoredInfantries";
    public static final String STORED_INFANTRY = "StoredInfantry";
    
    public static final String STRUCTURE_CAPTURES = "StructureCaptures";
    public static final String PLAYER_CAPTURES = "PlayerCaptures";
    public static final String SHIP_CAPTURES = "ShipCaptures";
    public static final String INFANTRY_KILLS = "InfantryKills";
    public static final String PLAYER_KILLS = "PlayerKills";
    
    public static final String TANK = "Tank";
    public static final String TANKS = "Tanks";
    public static final String STORED_TANK = "StoredTank";
    public static final String STORED_TANKS = "StoredTanks";
    public static final String IS_MISSILES = "IsMissiles";
    public static final String QUANTITY = "Quantity";
    public static final String USED_RECENTLY = "UsedRecently";
    
    public static final String STORED_MISSILES = "StoredMissiles";
    public static final String STORED_MISSILE = "StoredMissile";
    public static final String STORED_INTERCEPTORS = "StoredInterceptors";
    public static final String STORED_INTERCEPTOR = "StoredInterceptor";
    public static final String RESOURCES = "Resources";
    public static final String RESOURCE = "Resource";
    public static final String RESOURCE_CONTAINER = "ResourceContainer";
    public static final String TRUCK_RESOURCE_CONTAINER = "TruckResourceContainer";
    public static final String INFANTRY_RESOURCE_CONTAINER = "InfantryResourceContainer";
    public static final String TANK_RESOURCE_CONTAINER = "TankResourceContainer";
    
    public static final String CITIES = "Cities";
    public static final String CITY = "City";
    public static final String CITY_SIZE = "CitySize";
    public static final String POPULATION = "Population";
    public static final String MAX_POPULATION = "MaxPopulation";
    public static final String CONTESTED = "Contested";
    
    public static final String RESOURCE_DEPOSITS = "Deposits";
    public static final String RESOURCE_DEPOSIT = "Deposit";
    public static final String PROSPECT_COOLDOWN = "ProspectCooldown";
    public static final String CITY_COUNT_LAST_WEEK = "CityCountLastWeek";
    public static final String CHAMP_COUNT = "ChampCount";
    public static final String DEPOSIT_ID = "DepositID";
    public static final String ADMIN_MEMBER = "AdminMember";
    
    public static final String MINING_TRUCKS = "MiningTrucks";
    public static final String CARGO_TRUCKS = "CargoTrucks";
    public static final String CARGO_TRUCK = "CargoTruck";
    public static final String MINING_TRUCK = "MiningTruck";
    public static final String TRUCK_TYPE = "TruckType";
    public static final String STRUCTURE_TRUCK_TYPE = "StructureTruckType";
    public static final String DELIVER_TYPE = "TypeToDeliver";
    public static final String QUANTITY_TO_DELIVER = "QuantityToDeliver";
    public static final String CARGO_ID = "CargoID";
    public static final String LOOT_TYPE = "LootType";
    
    public static final String PROCESSORS = "Processors";
    public static final String PROCESSOR = "Processor";
    public static final String DISTRIBUTORS = "Distributors";
    public static final String DISTRIBUTOR = "Distributor";
    
    public static final String SUBMARINE_TYPE = "SubmarineType";
    public static final String SUBMARINE_TYPES = "SubmarineTypes";
    public static final String SHIP_TYPE = "ShipType";
    public static final String SHIP_TYPES = "ShipTypes";
    public static final String SENTRY_COUNT = "SentryCount";
    public static final String ARTILLERY_COUNT = "ArtilleryCount";
    public static final String SHIP_CLASS = "ShipClass";
    public static final String TONNAGE = "Tonnage";
    public static final String TORPEDO_COUNT = "TorpedoCount";
    public static final String HAS_SONAR = "HasSonar";
    public static final String AIRCRAFT_COUNT = "AircraftCount";
    
    public static final String SHIPYARDS = "Shipyards";
    public static final String SHIPYARD = "Shipyard";
    public static final String QUEUE = "Queue";
    public static final String PRODUCTION_ORDERS = "ProductionOrders";
    public static final String PRODUCTION_ORDER = "ProductionOrder";
    public static final String PRODUCING_FOR_ID = "ProducingForID";
    public static final String OUTPUT_COORD = "OutputCoord";
    public static final String PRODUCTION_CAPACITY = "ProductionCapacity";
    public static final String PRODUCE_BIG = "ProducingBig";
    
    public static final String ANTI_SHIP = "AntiShip";
    public static final String ANTI_SUBMARINE = "AntiSubmarine";
    public static final String BOMB = "Bomb";
    public static final String BUNKER_BUSTER = "BunkerBuster";
    
    public static final String ARTILLERY = "IsArtillery";
    public static final String ARTILLERY_GUNS = "ArtilleryGuns";
    public static final String ARTILLERY_GUN = "ArtilleryGun";
    
    public static final String BUILDING_FOR_ID = "BuildingForID";
    public static final String SHIPS = "Ships";
    public static final String SHIP = "Ship";
    public static final String SONAR_COOLDOWN = "SonarCooldown";
    public static final String SUBMERGED = "Submerged";
    public static final String DIVING = "Diving";
    public static final String SUBMERGE_TIME = "SubmergeTime";
    public static final String SUBMARINES = "Submarines";
    public static final String SUBMARINE = "Submarine";
    public static final String HOME_BASE = "Homebase";
    public static final String CIWS_GUNS = "CIWSGuns";
    public static final String CIWS_GUN = "CIWSGun";
    
    public static final String COLLECT = "Collect";
    public static final String VISIBLE = "Visible";
    public static final String VISIBLE_TIME = "VisibleTime";
    public static final String TORPEDO_DEPLOY_TYPE = "TorpedoDeployType";
    public static final String SONOBUOY = "Sonobuoy";
    public static final String INCENDIARY = "Incendiary";
    public static final String ENHANCED_RADIATION = "Neutron";
    
    public static final String RUBBLES = "Rubbles";
    public static final String RUBBLE = "Rubble";
    
    public static final String DISTANCE_TRAVELED = "DistanceTraveled";
    public static final String DISTANCE_TRAVELED_TODAY = "DistanceTraveledToday";
    public static final String BLACKLIST = "Blacklist";
    public static final String FILE_NAME = "FileName";
    public static final String NORTHWEST_BOUND = "NorthwestBound";
    public static final String SOUTHEAST_BOUND = "SoutheastBound";
    public static final String WATER_MAP = "WaterMap";
    public static final String STAMINA = "Stamina";
    public static final String HOST = "Host";
    
    public static final String SCRAP_YARDS = "ScrapYards";
    public static final String SCRAP_YARD = "ScrapYard";
    
    public static final String STORED_CARGO_TRUCKS = "StoredCargoTrucks";
    public static final String STORED_CARGO_TRUCK = "StoredCargoTruck";
    
    public static final String STORED_MINING_TRUCKS = "StoredMiningTrucks";
    public static final String STORED_MINING_TRUCK = "StoredMiningTruck";
    
    public static final String ARTILLERY_TARGET = "ArtilleryTarget";
    public static final String UNDER_ATTACK_TIME = "UnderAttackTime";
    public static final String LAUNCHING = "Launching";
    public static final String LAUNCH_PREP_REMAINING = "LaunchPrepRemaining";
    public static final String SLOT_TO_LAUNCH = "SlotToLaunch";
    
    public static final String HAPPINESS = "Happiness";
    public static final String CAPITAL = "IsCapital";
    
    public static final String TIME_AIRBORNE = "TimeAirborne";
    public static final String LASER_DEFENSE = "LaserDefense";
    public static final String RADAR_ACTIVE = "RadarActive";
    public static final String IS_A_MEMBER = "IsAMember";
    public static final String HELICOPTER_LAUNCHED = "HeliLaunched";
    public static final String HELICOPTER = "Helicopter";
    public static final String HELICOPTERS = "Helicopters";
    
    public static final String CANT_ATTACK_TIME = "CantAttackTime";
    public static final String GO_LIVE_TIME = "GoLiveTime";
    public static final String LIVE = "Live";
    public static final String PRICE = "Price";
    public static final String MARKET_LISTINGS = "MarketListings";
    public static final String MARKET_LISTING = "MarketListing";
    
    public static final String UPGRADE_LEVEL = "UpgradeLevel";
    public static final String TOTAL_OUTPUT = "TotalOutput";
    public static final String TANK_TYPE = "TankType";
    public static final String STRUCTURE_TANK_TYPE = "StructureTankType";
    public static final String RAILGUN = "Railgun";
    public static final String OPEN = "Open";
    public static final String FACTORY = "Factory";
    public static final String BUILT_BY_ID = "BuiltByID";
    public static final String BARRACKS = "Barracks";
    public static final String WATCH_TOWER = "IsWatchTower";
    
    public static final String KOTH = "KOTH";
    public static final String KING_ID = "KingID";
    public static final String CONTROLLED_BY_ALLIANCE = "AllianceControl";
    public static final String KOTH_WINS = "KOTHWins";
}
