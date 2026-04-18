/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import launch.game.Alliance;
import launch.game.Config;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchServerGame;
import launch.game.User;
import launch.game.entities.Airbase;
import launch.game.entities.Airplane;
import launch.game.entities.Blueprint;
import launch.game.entities.CommandPost;
import launch.game.entities.Interceptor;
import launch.game.entities.Warehouse;
import launch.game.entities.Loot;
import launch.game.entities.Missile;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.Player;
import launch.game.entities.Radiation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.Airdrop;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.Armory;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.CargoTruck;
import launch.game.entities.Infantry;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.Processor;
import launch.game.entities.RadarStation;
import launch.game.entities.ResourceDeposit;
import launch.game.entities.Rubble;
import launch.game.entities.ScrapYard;
import launch.game.entities.Shipyard;
import launch.game.entities.Tank;
import launch.game.entities.Torpedo;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.ShipProductionOrder;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.AircraftSystem;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.treaties.Treaty.Type;
import launch.game.systems.MissileSystem;

import launch.game.treaties.Affiliation;
import launch.game.treaties.AffiliationRequest;
import launch.game.treaties.War;
import launch.game.types.*;
import launch.utilities.LaunchBannedApp;
import launch.utilities.LaunchLog;
import static launch.utilities.LaunchLog.LogType.APPLICATION;
import launch.utilities.ShortDelay;
import launch.utilities.TerrainChecker;
import launch.utilities.WaterMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public class XMLGameLoader
{
    private static final String LOG_NAME = "Loader";
    
    private static GameLoadSaveListener currentListener;
    
    private static String strLastHandled = "";
    
    private static boolean bMissilesLoaded;
    private static boolean bInterceptorsLoaded;
    private static boolean bTorpedoesLoaded;
    private static boolean bMissileSitesLoaded;
    //private static boolean bArtilleryGunsLoaded;
    private static boolean bSAMsLoaded;
    private static boolean bSentryGunsLoaded;
    private static boolean bScrapyardsLoaded;
    private static boolean bOreMinesLoaded;
    private static boolean bRadarStationsLoaded;
    private static boolean bProcessorsLoaded;
    private static boolean bCommandPostsLoaded;
    private static boolean bWarehousesLoaded;
    private static boolean bAirbasesLoaded;
    private static boolean bArmoriesLoaded;
    private static boolean bAircraftsLoaded;
    private static boolean bInfantriesLoaded;
    private static boolean bTanksLoaded;
    private static boolean bTrucksLoaded;
    private static boolean bShipsLoaded;
    private static boolean bSubmarinesLoaded;
    private static boolean bBlueprintsLoaded;
    private static boolean bLootsLoaded;
    private static boolean bRubblesLoaded;
    private static boolean bAirdropsLoaded;
    private static boolean bDepositsLoaded;
    private static boolean bRadiationsLoaded;
    private static boolean bShipyardsLoaded;
    private static boolean bNewGame;
    
    public static Config LoadConfig(GameLoadSaveListener listener, String strConfigFile)
    {
        currentListener = listener;
        Config config = null;
        
        try
        {
            File file = new File(strConfigFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            LaunchLog.Log(APPLICATION, LOG_NAME, "Loading config xml...");
            
            config = new Config(GetStringRootElement(doc, XMLDefs.SERVER_EMAIL),
                GetByteRootElement(doc, XMLDefs.VARIANT),
                GetStringRootElement(doc, XMLDefs.SERVER_NAME),
                GetStringRootElement(doc, XMLDefs.SERVER_DESCRIPTION),    
                GetBooleanRootElement(doc, XMLDefs.DEBUG_MODE),
                GetIntRootElement(doc, XMLDefs.STARTING_WEALTH),
                GetIntRootElement(doc, XMLDefs.RESPAWN_WEALTH),
                GetIntRootElement(doc, XMLDefs.RESPAWN_TIME),
                GetIntRootElement(doc, XMLDefs.RESPAWN_PROTECTION_TIME),
                GetIntRootElement(doc, XMLDefs.HOURLY_WEALTH),
                GetIntRootElement(doc, XMLDefs.CMS_SYSTEM_COST),
                GetIntRootElement(doc, XMLDefs.SAM_SYSTEM_COST),
                GetIntRootElement(doc, XMLDefs.CMS_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.NCMS_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.SAM_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.SENTRY_GUN_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.ORE_MINE_STRUCTURE_COST),
                GetFloatRootElement(doc, XMLDefs.RUBBLE_MIN_VALUE),
                GetFloatRootElement(doc, XMLDefs.RUBBLE_MAX_VALUE),
                GetIntRootElement(doc, XMLDefs.RUBBLE_MIN_TIME),
                GetIntRootElement(doc, XMLDefs.RUBBLE_MAX_TIME),
                GetFloatRootElement(doc, XMLDefs.STRUCTURE_SEPARATION),
                GetShortRootElement(doc, XMLDefs.PLAYER_BASE_HP),
                GetShortRootElement(doc, XMLDefs.STRUCTURE_BASE_HP),
                GetIntRootElement(doc, XMLDefs.STRUCTURE_BOOT_TIME),
                GetByteRootElement(doc, XMLDefs.INITIAL_CSM_SLOTS),
                GetByteRootElement(doc, XMLDefs.INITIAL_SAM_SLOTS),
                GetFloatRootElement(doc, XMLDefs.REQUIRED_ACCURACY),
                GetIntRootElement(doc, XMLDefs.MIN_RADIATION_TIME),
                GetIntRootElement(doc, XMLDefs.MAX_RADIATION_TIME),
                GetIntRootElement(doc, XMLDefs.MISSILE_SLOT_UPGRADE_BASE_COST),
                GetByteRootElement(doc, XMLDefs.MISSILE_SLOT_UPGRADE_COUNT),
                GetFloatRootElement(doc, XMLDefs.RESALE_VALUE),
                GetIntRootElement(doc, XMLDefs.DECOMMISSION_TIME),
                GetIntRootElement(doc, XMLDefs.RELOAD_TIME_BASE),
                GetIntRootElement(doc, XMLDefs.RELOAD_TIME_STAGE1),
                GetIntRootElement(doc, XMLDefs.RELOAD_TIME_STAGE2),
                GetIntRootElement(doc, XMLDefs.RELOAD_TIME_STAGE3),
                GetIntRootElement(doc, XMLDefs.RELOAD_STAGE1_COST),
                GetIntRootElement(doc, XMLDefs.RELOAD_STAGE2_COST),
                GetIntRootElement(doc, XMLDefs.RELOAD_STAGE3_COST),
                GetFloatRootElement(doc, XMLDefs.REPAIR_SALVAGE_DISTANCE),
                GetIntRootElement(doc, XMLDefs.MISSILE_SITE_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.SAM_SITE_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.SENTRY_GUN_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.ORE_MINE_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.HEALTH_INTERVAL),
                GetIntRootElement(doc, XMLDefs.RADIATION_INTERVAL),
                GetIntRootElement(doc, XMLDefs.PLAYER_REPAIR_COST),
                GetIntRootElement(doc, XMLDefs.STRUCTURE_REPAIR_COST),
                GetLongRootElement(doc, XMLDefs.AWOL_TIME),
                GetLongRootElement(doc, XMLDefs.REMOVE_TIME),
                GetIntRootElement(doc, XMLDefs.ALLIANCE_COOLOFF_TIME),
                GetFloatRootElement(doc, XMLDefs.ECM_INTERCEPTOR_CHANCE_REDUCTION),
                GetFloatRootElement(doc, XMLDefs.MANUAL_INTERCEPTOR_CHANCE_INCREASE),
                GetIntRootElement(doc, XMLDefs.SENTRY_GUN_RELOAD_TIME),
                GetFloatRootElement(doc, XMLDefs.SENTRY_GUN_RANGE),
                GetFloatRootElement(doc, XMLDefs.SENTRY_GUN_HIT_CHANCE),
                GetFloatRootElement(doc, XMLDefs.ORE_COLLECT_RADIUS),
                GetIntRootElement(doc, XMLDefs.ORE_MINE_GENERATE_TIME),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_DIPLOMATIC_PRESENCE),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_POLITICAL_ENGAGEMENT),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_DEFENDER_OF_THE_NATION),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_NUCLEAR_SUPERPOWER),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_WEEKLY_KILLS_BATCH),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_SURVIVOR),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_HIPPY),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_PEACE_MAKER),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_WAR_MONGER),
                GetIntRootElement(doc, XMLDefs.HOURLY_BONUS_LONE_WOLF),
                GetFloatRootElement(doc, XMLDefs.LONE_WOLF_DISTANCE),
                GetIntRootElement(doc, XMLDefs.RADAR_STATION_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.RADAR_STATION_STRUCTURE_COST),
                GetFloatRootElement(doc, XMLDefs.RADAR_RANGE_BASE),
                GetFloatRootElement(doc, XMLDefs.RADAR_RANGE_STAGE1),
                GetFloatRootElement(doc, XMLDefs.RADAR_RANGE_STAGE2),
                GetFloatRootElement(doc, XMLDefs.RADAR_RANGE_STAGE3),
                GetIntRootElement(doc, XMLDefs.RADAR_RANGE_STAGE1_COST),
                GetIntRootElement(doc, XMLDefs.RADAR_RANGE_STAGE2_COST),
                GetIntRootElement(doc, XMLDefs.RADAR_RANGE_STAGE3_COST),
                GetIntRootElement(doc, XMLDefs.ABM_SILO_STRUCTURE_COST),
                GetFloatRootElement(doc, XMLDefs.BASE_RADAR_BOOST),
                GetFloatRootElement(doc, XMLDefs.RADAR_BOOST_DISTANCE),
                GetIntRootElement(doc, XMLDefs.RADAR_BOOST_UPGRADE_COST),
                GetIntRootElement(doc, XMLDefs.SENTRY_GUN_RANGE_UPGRADE_COST),
                GetFloatRootElement(doc, XMLDefs.STEALTH_PER_TICK_CHANCE),
                GetByteRootElement(doc, XMLDefs.JIHADIST_THRESHOLD),
                GetFloatRootElement(doc, XMLDefs.ORE_GENERATE_RADIUS),
                GetFloatRootElement(doc, XMLDefs.ORE_MINE_SPACING),
                GetIntRootElement(doc, XMLDefs.ORE_MIN_EXPIRY),
                GetIntRootElement(doc, XMLDefs.ORE_MAX_EXPIRY),
                GetIntRootElement(doc, XMLDefs.MAX_ORE_VALUE),
                GetIntRootElement(doc, XMLDefs.SPANEL_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.SPANEL_STRUCTURE_COST),
                GetShortRootElement(doc, XMLDefs.SPANEL_STRUCTURE_HP),
                GetIntRootElement(doc, XMLDefs.SPANEL_GENERATE_TIME),
                GetIntRootElement(doc, XMLDefs.ELEC_MIN_EXPIRY),
                GetIntRootElement(doc, XMLDefs.ELEC_MAX_EXPIRY),
                GetIntRootElement(doc, XMLDefs.ELEC_MIN_VALUE),
                GetIntRootElement(doc, XMLDefs.ELEC_MAX_VALUE),
                GetFloatRootElement(doc, XMLDefs.SPANEL_SPACING),
                GetFloatRootElement(doc, XMLDefs.COMMAND_POST_SHELTER_RADIUS),
                GetIntRootElement(doc, XMLDefs.COMMAND_POST_HP_UPGRADE_COST),
                GetShortRootElement(doc, XMLDefs.COMMAND_POST_HP_UPGRADE_HP),
                GetIntRootElement(doc, XMLDefs.SILO_BOOT_UPGRADE_COST),
                GetIntRootElement(doc, XMLDefs.SILO_BOOT_UPGRADE_TIME),
                GetIntRootElement(doc, XMLDefs.SILO_MAX_BOOT_UPGRADE),
                GetShortRootElement(doc, XMLDefs.COMMAND_POST_MAX_HP_UPGRADE),
                GetIntRootElement(doc, XMLDefs.COMMAND_POST_STRUCTURE_COST),
                GetShortRootElement(doc, XMLDefs.COMMAND_POST_BASE_HP),
                GetIntRootElement(doc, XMLDefs.COMMAND_POST_MAINTENANCE_COST),
                GetFloatRootElement(doc, XMLDefs.MAX_RADAR_ACCURACY),
                GetFloatRootElement(doc, XMLDefs.FARM_SPACING),
                GetIntRootElement(doc, XMLDefs.FARM_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.FARM_MAINTENANCE_COST),
                GetShortRootElement(doc, XMLDefs.FARM_BASE_HP),
                GetIntRootElement(doc, XMLDefs.CROP_MAX_VALUE),
                GetIntRootElement(doc, XMLDefs.CROP_MIN_EXPIRY),
                GetIntRootElement(doc, XMLDefs.CROP_MAX_EXPIRY),
                GetFloatRootElement(doc, XMLDefs.FARM_GENERATE_RADIUS),
                GetIntRootElement(doc, XMLDefs.FARM_GROWTH_TIME),
                GetIntRootElement(doc, XMLDefs.MIN_CROP_COUNT),
                GetIntRootElement(doc, XMLDefs.MAX_CROP_COUNT),
                GetFloatRootElement(doc, XMLDefs.FARM_WORK_RADIUS),
                GetIntRootElement(doc, XMLDefs.AIRBASE_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.AIRBASE_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.AIRBASE_BASE_CAPACITY),
                GetShortRootElement(doc, XMLDefs.AIRBASE_BASE_HP),
                GetIntRootElement(doc, XMLDefs.BOMBER_COST),
                GetIntRootElement(doc, XMLDefs.FIGHTER_COST),
                GetFloatRootElement(doc, XMLDefs.AIRSPACE_DISTANCE),
                GetIntRootElement(doc, XMLDefs.AIRCRAFT_CHAFF_COUNT),
                GetIntRootElement(doc, XMLDefs.STRIKE_FIGHTER_COST),
                GetIntRootElement(doc, XMLDefs.FAST_BOMBER_COST),
                GetIntRootElement(doc, XMLDefs.BANK_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.BANK_UPGRADE_COST),
                GetIntRootElement(doc, XMLDefs.BANK_BASE_CAPACITY),
                GetIntRootElement(doc, XMLDefs.BANK_MAX_CAPACITY),
                GetShortRootElement(doc, XMLDefs.BANK_BASE_HP),
                GetIntRootElement(doc, XMLDefs.BANK_MAINTENANCE_COST),
                GetFloatRootElement(doc, XMLDefs.BANK_INTEREST_RATE),
                GetFloatRootElement(doc, XMLDefs.LOG_DEPOT_LINK_RANGE),
                GetIntRootElement(doc, XMLDefs.LOG_DEPOT_MAINTENANCE_COST),
                GetFloatRootElement(doc, XMLDefs.LOG_DEPOT_COLLECT_RADIUS),
                GetIntRootElement(doc, XMLDefs.LOG_DEPOT_STRUCTURE_COST),
                GetShortRootElement(doc, XMLDefs.LOG_DEPOT_BASE_HP),
                GetIntRootElement(doc, XMLDefs.MAX_LOG_DEPOT_CAPACITY),
                GetIntRootElement(doc, XMLDefs.MISSILE_FACTORY_STRUCTURE_COST),
                GetIntRootElement(doc, XMLDefs.MISSILE_FACTORY_COOLDOWN),
                GetIntRootElement(doc, XMLDefs.MISSILE_FACTORY_MAINTENANCE_COST),
                GetIntRootElement(doc, XMLDefs.BLUEPRINT_COST),
                GetIntRootElement(doc, XMLDefs.BLUEPRINT_EXPIRY),
                GetFloatRootElement(doc, XMLDefs.BLUEPRINT_BUILD_DISTANCE));
                        
            config.SetPort(GetIntRootElement(doc, XMLDefs.PORT));
            
            //Load missile types.
            NodeList ndeMissileTypes = doc.getElementsByTagName(XMLDefs.MISSILE_TYPE);
            
            for(int i = 0; i < ndeMissileTypes.getLength(); i++)
            {
                try
                {
                    Element ndeMissileType = (Element)ndeMissileTypes.item(i);
                    int lID = GetIntAttribute(ndeMissileType, XMLDefs.ID);
                    String strName = GetStringAttribute(ndeMissileType, XMLDefs.NAME);
                    int lAssetID = GetIntElement(ndeMissileType, XMLDefs.ASSET_ID);
                    boolean bPurchasable = GetBooleanElement(ndeMissileType, XMLDefs.PURCHASABLE);
                    boolean bArtillery = GetBooleanElement(ndeMissileType, XMLDefs.ARTILLERY);
                    float fMissileSpeed = GetFloatElement(ndeMissileType, XMLDefs.MISSILE_SPEED, 0.0f);
                    float fMissileRange = GetFloatElement(ndeMissileType, XMLDefs.RANGE, 0);
                    boolean bNuclear = GetBooleanElement(ndeMissileType, XMLDefs.NUCLEAR);
                    boolean bTracking = GetBooleanElement(ndeMissileType, XMLDefs.TRACKING);
                    boolean bECM = GetBooleanElement(ndeMissileType, XMLDefs.ECM);
                    boolean bAirLaunched = GetBooleanElement(ndeMissileType, XMLDefs.AIR_LAUNCHED);
                    boolean bGroundLaunched = GetBooleanElement(ndeMissileType, XMLDefs.GROUND_LAUNCHED);
                    boolean bICBM = GetBooleanElement(ndeMissileType, XMLDefs.ICBM);
                    float fEMPRadius = GetFloatElement(ndeMissileType, XMLDefs.EMP_RADIUS, 0);
                    boolean bStealth = GetBooleanElement(ndeMissileType, XMLDefs.STEALTH);
                    int lYield = GetIntElement(ndeMissileType, XMLDefs.YIELD);
                    float fltAccuracy = GetFloatElement(ndeMissileType, XMLDefs.ACCURACY, 0.0f);
                    boolean bTankLaunched = GetBooleanElement(ndeMissileType, XMLDefs.TANK_LAUNCHED);
                    boolean bShipLaunched = GetBooleanElement(ndeMissileType, XMLDefs.SHIP_LAUNCHED);
                    boolean bSubmarineLaunched = GetBooleanElement(ndeMissileType, XMLDefs.SUBMARINE_LAUNCHED);
                    boolean bAntiShip = GetBooleanElement(ndeMissileType, XMLDefs.ANTI_SHIP);
                    boolean bAntiSubmarine = GetBooleanElement(ndeMissileType, XMLDefs.ANTI_SUBMARINE);
                    boolean bBomb = GetBooleanElement(ndeMissileType, XMLDefs.BOMB);
                    boolean bBunkerBuster = GetBooleanElement(ndeMissileType, XMLDefs.BUNKER_BUSTER);
                    int lTorpedoType = GetIntElement(ndeMissileType, XMLDefs.TORPEDO_DEPLOY_TYPE);
                    boolean bSonobuoy = GetBooleanElement(ndeMissileType, XMLDefs.SONOBUOY);
                    Map<ResourceType, Long> costs = GetResourceMap(ndeMissileType, XMLDefs.COSTS);
                    
                    long oCost = costs.get(ResourceType.WEALTH);
                    
                    config.AddMissileType(lID, new MissileType(lID, bPurchasable, strName, lAssetID, bNuclear, bTracking, bECM, bAirLaunched, bGroundLaunched, bArtillery, fMissileSpeed, fMissileRange, bICBM, fEMPRadius, bStealth, lYield, fltAccuracy, bTankLaunched, bShipLaunched, bSubmarineLaunched, bAntiShip, bAntiSubmarine, bBomb, bBunkerBuster, lTorpedoType, bSonobuoy, oCost));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading missile type at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load interceptor types.
            NodeList ndeInterceptorTypes = doc.getElementsByTagName(XMLDefs.INTERCEPTOR_TYPE);
            
            for(int i = 0; i < ndeInterceptorTypes.getLength(); i++)
            {
                try
                {
                    Element ndeInterceptorType = (Element)ndeInterceptorTypes.item(i);
                    int lID = GetIntAttribute(ndeInterceptorType, XMLDefs.ID);
                    String strName = GetStringAttribute(ndeInterceptorType, XMLDefs.NAME);
                    int lAssetID = GetIntElement(ndeInterceptorType, XMLDefs.ASSET_ID);
                    boolean bPurchasable = GetBooleanElement(ndeInterceptorType, XMLDefs.PURCHASABLE);
                    int lInterceptorCost = GetIntElement(ndeInterceptorType, XMLDefs.INTERCEPTOR_COST);
                    int lInterceptorSpeed = GetIntElement(ndeInterceptorType, XMLDefs.INTERCEPTOR_SPEED);
                    float fInterceptorRange = GetFloatElement(ndeInterceptorType, XMLDefs.RANGE, 0);
                    int lPrepTime = GetIntElement(ndeInterceptorType, XMLDefs.PREP_TIME);
                    boolean bABM = GetBooleanElement(ndeInterceptorType, XMLDefs.ANTI_BALLISTIC);
                    boolean bAirLaunched = GetBooleanElement(ndeInterceptorType, XMLDefs.AIR_LAUNCHED);
                    float fltBlastRadius = GetFloatElement(ndeInterceptorType, XMLDefs.BLAST_RADIUS, 0);
                    float fltYield = GetFloatElement(ndeInterceptorType, XMLDefs.YIELD, 0);
                    boolean bTankLaunched = GetBooleanElement(ndeInterceptorType, XMLDefs.TANK_LAUNCHED);
                    boolean bShipLaunched = GetBooleanElement(ndeInterceptorType, XMLDefs.SHIP_LAUNCHED);
                    Map<ResourceType, Long> costs = GetResourceMap(ndeInterceptorType, XMLDefs.COSTS);
                    
                    long oCost = costs.get(ResourceType.WEALTH);
                    
                    config.AddInterceptorType(lID, new InterceptorType(lID, bPurchasable, strName, lAssetID, lInterceptorCost, lInterceptorSpeed, fInterceptorRange, lPrepTime, bABM, bAirLaunched, fltBlastRadius, fltYield, bTankLaunched, bShipLaunched, oCost));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading interceptor type at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load torpedo types.
            NodeList ndeTorpedoTypes = doc.getElementsByTagName(XMLDefs.TORPEDO_TYPE);
            
            for(int i = 0; i < ndeTorpedoTypes.getLength(); i++)
            {
                try
                {
                    Element ndeTorpedoType = (Element)ndeTorpedoTypes.item(i);
                    int lID = GetIntAttribute(ndeTorpedoType, XMLDefs.ID);
                    String strName = GetStringAttribute(ndeTorpedoType , XMLDefs.NAME);
                    int lAssetID = GetIntElement(ndeTorpedoType , XMLDefs.ASSET_ID);
                    boolean bPurchasable = GetBooleanElement(ndeTorpedoType , XMLDefs.PURCHASABLE);
                    int lTorpedoSpeed = GetIntElement(ndeTorpedoType , XMLDefs.SPEED);
                    float fltTorpedoBlastRadius = GetFloatElement(ndeTorpedoType , XMLDefs.BLAST_RADIUS, 0);
                    float fltTorpedoRange = GetFloatElement(ndeTorpedoType , XMLDefs.RANGE, 0);
                    int lYield = GetIntElement(ndeTorpedoType, XMLDefs.YIELD);
                    boolean bNuclear = GetBooleanElement(ndeTorpedoType , XMLDefs.NUCLEAR);
                    boolean bHoming = GetBooleanElement(ndeTorpedoType , XMLDefs.HOMING);
                    boolean bAirLaunched = GetBooleanElement(ndeTorpedoType , XMLDefs.AIR_LAUNCHED);
                    boolean bNavalLaunched = GetBooleanElement(ndeTorpedoType , XMLDefs.NAVAL_LAUNCHED);
                    Map<ResourceType, Long> costs = GetResourceMap(ndeTorpedoType, XMLDefs.COSTS);
                    
                    long oCost = costs.get(ResourceType.WEALTH);
                    
                    config.AddTorpedoType(lID, new TorpedoType(lID, bPurchasable, strName, lAssetID, bNuclear, bHoming, bAirLaunched, bNavalLaunched, fltTorpedoRange, fltTorpedoBlastRadius, lYield, lTorpedoSpeed, oCost));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading missile type at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load minor banned apps.
            NodeList ndeMinorCheatingApps = doc.getElementsByTagName(XMLDefs.MINOR_BANNED_APP);
            
            for(int i = 0; i < ndeMinorCheatingApps.getLength(); i++)
            {
                try
                {
                    Element ndeMinorCheatingApp = (Element)ndeMinorCheatingApps.item(i);
                    String strName = GetStringAttribute(ndeMinorCheatingApp, XMLDefs.NAME);
                    String strSignature = GetStringElement(ndeMinorCheatingApp, XMLDefs.SIGNATURE);
                    String strDescription = GetStringElement(ndeMinorCheatingApp, XMLDefs.DESCRIPTION);
                    
                    config.AddMinorCheatingApp(new LaunchBannedApp(strName, strSignature, strDescription));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading minor cheating app at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load major banned apps.
            NodeList ndeMajorCheatingApps = doc.getElementsByTagName(XMLDefs.MAJOR_BANNED_APP);
            
            for(int i = 0; i < ndeMajorCheatingApps.getLength(); i++)
            {
                try
                {
                    Element ndeMajorCheatingApp = (Element)ndeMajorCheatingApps.item(i);
                    String strName = GetStringAttribute(ndeMajorCheatingApp, XMLDefs.NAME);
                    String strSignature = GetStringElement(ndeMajorCheatingApp, XMLDefs.SIGNATURE);
                    String strDescription = GetStringElement(ndeMajorCheatingApp, XMLDefs.DESCRIPTION);
                    
                    config.AddMajorCheatingApp(new LaunchBannedApp(strName, strSignature, strDescription));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading major cheating app at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load water maps.
            NodeList ndeWaterMaps = doc.getElementsByTagName(XMLDefs.WATER_MAP);
            List<WaterMap> HighResMaps = new ArrayList<>();
            
            for(int i = 0; i < ndeWaterMaps.getLength(); i++)
            {
                try
                {
                    Element ndeWaterMap = (Element)ndeWaterMaps.item(i);
                    String strFileName = GetStringAttribute(ndeWaterMap, XMLDefs.FILE_NAME);
                    GeoCoord geoNW = GetPositionElement(ndeWaterMap, XMLDefs.NORTHWEST_BOUND);
                    GeoCoord geoSE = GetPositionElement(ndeWaterMap, XMLDefs.SOUTHEAST_BOUND);
                    
                    HighResMaps.add(new WaterMap(strFileName, geoNW, geoSE));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading water map at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            TerrainChecker.LoadHighResMaps(HighResMaps);
            
            config.Finalise();
        }
        catch (ParserConfigurationException ex)
        {
            listener.LoadError(String.format("XML parser configuration error when loading config from %s. Last handled: %s", strConfigFile, strLastHandled));
        }
        catch (SAXException ex)
        {
            listener.LoadError(String.format("SAX exception when loading config from %s. Last handled: %s", strConfigFile, strLastHandled));
        }
        catch (IOException ex)
        {
            listener.LoadError(String.format("IO error when loading config from %s. Last handled: %s", strConfigFile, strLastHandled));
        }
        catch(Exception ex)
        {
            listener.LoadError(String.format("Other error when loading config from %s. Last handled: %s", strConfigFile, strLastHandled));
        }
        
        return config;
    }
    
    public static void LoadGame(GameLoadSaveListener listener, String strGameFile, LaunchServerGame game)
    {
        currentListener = listener;
        
        try
        {
            File file = new File(strGameFile);
            
            if(!file.exists())
            {
                //No game file. Start without one.
                LaunchLog.ConsoleMessage("No save file found. Starting without one...");
                bNewGame = true;
                AttemptFinalizeLoading(game, listener);
                return;
            }
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            // Load game stats
            NodeList ndeGameStats = doc.getElementsByTagName(XMLDefs.GAME_STATS);

            if(ndeGameStats.getLength() > 0)
            {
                Element eleGameStats = (Element)ndeGameStats.item(0);

                if(GetHasNode(eleGameStats, XMLDefs.NEXT_PLAYER_ID))
                {
                    int lNextPlayerID = GetIntElement(eleGameStats, XMLDefs.NEXT_PLAYER_ID);

                    game.SetPlayerIndex(lNextPlayerID);
                }
            }

            //Load users.
            NodeList ndeUsers = doc.getElementsByTagName(XMLDefs.USER);

            for(int i = 0; i < ndeUsers.getLength(); i++)
            {
                try
                {
                    Element eleUser = (Element)ndeUsers.item(i);
                    
                    String strGoogleID = "";
                    
                    String strIMEI = GetStringElement(eleUser, XMLDefs.IMEI);
                    
                    if(GetHasNode(eleUser, XMLDefs.GOOGLE_ID))
                        strGoogleID = GetStringElement(eleUser, XMLDefs.GOOGLE_ID);
                    
                    int lPlayerID = GetIntElement(eleUser, XMLDefs.PLAYERID);
                    byte cBanState = GetByteElement(eleUser,XMLDefs.BAN_STATE);
                    long oNextBanTime = GetLongElement(eleUser, XMLDefs.NEXT_BAN_TIME);
                    long oBanDurationRemaining = GetLongElement(eleUser, XMLDefs.BAN_DURATION_REMAINING);
                    String strBanReason = GetStringElement(eleUser, XMLDefs.BAN_REASON);
                    String strLastIP = GetStringElement(eleUser, XMLDefs.LAST_IP);
                    boolean bLastTypeMobile = GetBooleanElement(eleUser, XMLDefs.LAST_CONNECTION_MOBILE);
                    long oCheckedDate = GetLongElement(eleUser, XMLDefs.LAST_CHECKED);
                    boolean bLastCheckFailed = GetBooleanElement(eleUser, XMLDefs.LAST_CHECK_FAILED);
                    boolean bCheckAPIFailed = GetBooleanElement(eleUser, XMLDefs.CHECK_API_FAILED);
                    boolean bProscribed = GetBooleanElement(eleUser, XMLDefs.PROSCRIBED);
                    int lCheckFailCode = GetIntElement(eleUser, XMLDefs.CHECK_FAIL_CODE);
                    boolean bProfileMatch = GetBooleanElement(eleUser, XMLDefs.PROFILE_MATCH);
                    boolean bBasicIntegrity = GetBooleanElement(eleUser, XMLDefs.BASIC_INTEGRITY);
                    boolean bApproved = GetBooleanElement(eleUser, XMLDefs.APPROVED);
                    String strDeviceHash = GetStringElement(eleUser, XMLDefs.DEVICE_HASH);
                    String strAppListHash = GetStringElement(eleUser, XMLDefs.APP_LIST_HASH);
                    String strToken = GetStringElement(eleUser, XMLDefs.TOKEN);
                    long oExpired = GetLongElement(eleUser, XMLDefs.EXPIRED);
                    
                    User user = new User(strIMEI, strGoogleID, lPlayerID, cBanState, oNextBanTime, oBanDurationRemaining, strBanReason, strLastIP, bLastTypeMobile, oCheckedDate, bLastCheckFailed, bCheckAPIFailed, bProscribed, lCheckFailCode, bProfileMatch, bBasicIntegrity, bApproved, oExpired, strDeviceHash, strAppListHash, strToken);
                    
                    /*NodeList ndeReports = GetNodes(eleUser, XMLDefs.REPORT);
                    
                    for(int j = 0; j < ndeReports.getLength(); j++)
                    {
                        Element eleReport = (Element)ndeReports.item(j);
                        long oTimeStart = GetLongElement(eleReport, XMLDefs.TIME_START);
                        long oTimeEnd = GetLongElement(eleReport, XMLDefs.TIME_END);
                        String strMessage = GetStringElement(eleReport, XMLDefs.MESSAGE);
                        boolean bIsMajor = GetBooleanElement(eleReport, XMLDefs.IS_MAJOR);
                        int lLeftID = GetIntElement(eleReport, XMLDefs.LEFT_ID);
                        int lRightID = GetIntElement(eleReport, XMLDefs.RIGHT_ID);
                        byte cTimes = GetByteElement(eleReport, XMLDefs.TIMES);
                        byte cFlags = GetByteElement(eleReport, XMLDefs.FLAGS);
                        
                        user.AddReport(new LaunchReport(oTimeStart, oTimeEnd, strMessage, bIsMajor, lLeftID, lRightID, cTimes, cFlags));
                    }*/
                    
                    game.AddUser(user);
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading user at index %d: %s. Last element: %s", i, ex.getMessage(), strLastHandled));
                }
            }
            
            //Load alliances.
            NodeList nodeAlliances = doc.getElementsByTagName(XMLDefs.ALLIANCE);
            
            for(int i = 0; i < nodeAlliances.getLength(); i++)
            {
                try
                {
                    Element eleAlliance = (Element)nodeAlliances.item(i);
                    int lID = GetIntAttribute(eleAlliance, XMLDefs.ID);
                    String strName = GetStringAttribute(eleAlliance, XMLDefs.NAME);
                    String strDescription = GetStringElement(eleAlliance, XMLDefs.DESCRIPTION);
                    int lAvatarID = GetIntElement(eleAlliance, XMLDefs.AVATAR);
                    int lWealth = GetIntElement(eleAlliance, XMLDefs.WEALTH);
                    float fltTaxRate = GetFloatElement(eleAlliance, XMLDefs.TAX_RATE, Defs.DEFAULT_TAX_RATE);
                    int lWarsWon = GetIntElement(eleAlliance, XMLDefs.WARS_WON);
                    int lWarsLost = GetIntElement(eleAlliance, XMLDefs.WARS_LOST);
                    int lEnemyAllianceDisbands = GetIntElement(eleAlliance, XMLDefs.ALLIANCE_DISBAND_COUNT);
                    String strFounderName = GetStringElement(eleAlliance, XMLDefs.FOUNDER_NAME);
                    int lAffiliationsBroken = GetIntElement(eleAlliance, XMLDefs.AFFILIATIONS_BROKEN);
                    int lICBMCount = GetIntElement(eleAlliance, XMLDefs.ICBM_COUNT);
                    int lABMCount = GetIntElement(eleAlliance, XMLDefs.ABM_COUNT);
                    long oFoundedTime = GetLongElement(eleAlliance, XMLDefs.FOUNDED_TIME);
                    
                    game.AddAlliance(new Alliance(lID, strName, strDescription, lAvatarID, lWealth, fltTaxRate, lWarsWon, lWarsLost, lEnemyAllianceDisbands, strFounderName, lAffiliationsBroken, lICBMCount, lABMCount, oFoundedTime), false);
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading alliance at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load treaties.
            NodeList nodeTreaties = doc.getElementsByTagName(XMLDefs.TREATY);
            
            for(int i = 0; i < nodeTreaties.getLength(); i++)
            {
                try
                {
                    Element eleTreaty = (Element)nodeTreaties.item(i);
                    int lID = GetIntAttribute(eleTreaty, XMLDefs.ID);
                    int lAlliance1 = GetIntElement(eleTreaty, XMLDefs.ALLIANCE1);
                    int lAlliance2 = GetIntElement(eleTreaty, XMLDefs.ALLIANCE2);
                    Type type = Type.values()[GetIntElement(eleTreaty, XMLDefs.TYPE)];
                    
                    if(type == Type.WAR)
                    {
                        short nKills1 = GetShortElement(eleTreaty, XMLDefs.KILLS1);
                        short nDeaths1 = GetShortElement(eleTreaty, XMLDefs.DEATHS1);
                        int lOffenceSpending1 = GetIntElement(eleTreaty, XMLDefs.OFFENCE_SPENDING1);
                        int lDefenceSpending1 = GetIntElement(eleTreaty, XMLDefs.DEFENCE_SPENDING1);
                        int lDamageInflicted1 = GetIntElement(eleTreaty, XMLDefs.DAMAGE_INFLICTED1);
                        int lDamageReceived1 = GetIntElement(eleTreaty, XMLDefs.DAMAGE_RECEIVED1);
                        int lIncome1 = GetIntElement(eleTreaty, XMLDefs.INCOME1);
                        short nKills2 = GetShortElement(eleTreaty, XMLDefs.KILLS2);
                        short nDeaths2 = GetShortElement(eleTreaty, XMLDefs.DEATHS2);
                        int lOffenceSpending2 = GetIntElement(eleTreaty, XMLDefs.OFFENCE_SPENDING2);
                        int lDefenceSpending2 = GetIntElement(eleTreaty, XMLDefs.DEFENCE_SPENDING2);
                        int lDamageInflicted2 = GetIntElement(eleTreaty, XMLDefs.DAMAGE_INFLICTED2);
                        int lDamageReceived2 = GetIntElement(eleTreaty, XMLDefs.DAMAGE_RECEIVED2);
                        int lIncome2 = GetIntElement(eleTreaty, XMLDefs.INCOME2);
                        long oStartTime = GetLongElement(eleTreaty, XMLDefs.START_TIME);

                        game.AddTreaty(new War(lID, lAlliance1, lAlliance2, nKills1, nDeaths1, lOffenceSpending1, lDefenceSpending1, lDamageInflicted1, lDamageReceived1, lIncome1, nKills2, nDeaths2, lOffenceSpending2, lDefenceSpending2, lDamageInflicted2, lDamageReceived2, lIncome2, oStartTime));
                    }
                    else
                    {
                        switch(type)
                        {
                            case AFFILIATION: game.AddTreaty(new Affiliation(lID, lAlliance1, lAlliance2)); break;
                            case AFFILIATION_REQUEST: game.AddTreaty(new AffiliationRequest(lID, lAlliance1, lAlliance2)); break;
                        }
                    }
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading treaty at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            //Load players.
            NodeList nodes = doc.getElementsByTagName(XMLDefs.PLAYER);

            for(int i = 0; i < nodes.getLength(); i++)
            {
                try
                {
                    Element elePlayer = (Element)nodes.item(i);
                    int lID = GetIntAttribute(elePlayer, XMLDefs.ID);
                    GeoCoord geoPosition = GetPositionElement(elePlayer, XMLDefs.POSITION);
                    String strName = GetStringAttribute(elePlayer, XMLDefs.NAME);
                    int lAvatarID = GetIntElement(elePlayer, XMLDefs.AVATAR);
                    long oLastSeen = GetLongElement(elePlayer, XMLDefs.LAST_SEEN);
                    int lStateChange = GetIntElement(elePlayer, XMLDefs.STATE_CHANGE);
                    int lAllianceID = GetIntElement(elePlayer, XMLDefs.ALLIANCE_ID);
                    byte cFlags1 = GetByteElement(elePlayer, XMLDefs.FLAGS1);
                    byte cFlags2 = GetByteElement(elePlayer, XMLDefs.FLAGS2);
                    int lAllianceCooloffTime = GetIntElement(elePlayer, XMLDefs.ALLIANCE_COOLOFF_TIME);
        
                    short nKills = GetShortElement(elePlayer, XMLDefs.KILLS);
                    short nDeaths = GetShortElement(elePlayer, XMLDefs.DEATHS);
                    int lOffenceSpending = GetIntElement(elePlayer, XMLDefs.OFFENCE_SPENDING);
                    int lDefenceSpending = GetIntElement(elePlayer, XMLDefs.DEFENCE_SPENDING);
                    int lDamageInflicted = GetIntElement(elePlayer, XMLDefs.DAMAGE_INFLICTED);
                    int lDamageReceived = GetIntElement(elePlayer, XMLDefs.DAMAGE_RECEIVED);
                    int lRank = GetIntElement(elePlayer, XMLDefs.RANK);
                    int lExperience = GetIntElement(elePlayer, XMLDefs.EXPERIENCE);
                    int lTotalKills = GetIntElement(elePlayer, XMLDefs.TOTAL_KILLS);
                    int lTotalDeaths = GetIntElement(elePlayer, XMLDefs.TOTAL_DEATHS);
                    boolean bMember = GetBooleanElement(elePlayer, XMLDefs.IS_A_MEMBER);
                    boolean bAdminMember = GetBooleanElement(elePlayer, XMLDefs.ADMIN_MEMBER);
                    long oJoinTime = GetLongElement(elePlayer, XMLDefs.JOIN_TIME);
                    int lDefenseValue = GetIntElement(elePlayer, XMLDefs.DEFENSE_VALUE);
                    int lOffenseValue = GetIntElement(elePlayer, XMLDefs.OFFENSE_VALUE);
                    int lNeutralValue = GetIntElement(elePlayer, XMLDefs.NEUTRAL_VALUE);
                    int lAirdropCooldown = GetIntElement(elePlayer, XMLDefs.AIRDROP_COOLDOWN);
                    int lProspectCooldown = GetIntElement(elePlayer, XMLDefs.PROSPECT_COOLDOWN);
                    int lCityCountLastWeek = GetIntElement(elePlayer, XMLDefs.CITY_COUNT_LAST_WEEK);
                    int lChampCount = GetIntElement(elePlayer, XMLDefs.CHAMP_COUNT);
                    float fltDistanceTraveled = GetFloatElement(elePlayer, XMLDefs.DISTANCE_TRAVELED, 0);
                    float fltDistanceTraveledToday = GetFloatElement(elePlayer, XMLDefs.DISTANCE_TRAVELED_TODAY, 0);
                    List<Integer> Blacklist = new ArrayList<>();
                    
                    if(GetHasNode(elePlayer, XMLDefs.BLACKLIST))
                        Blacklist = LaunchUtilities.IntListFromData(ByteBuffer.wrap(GetByteArrayElement(elePlayer, XMLDefs.BLACKLIST)));
                    
                    CargoSystem cargo = null;
                    long oWealth = 0;
                    
                    if(lExperience < 0)
                        lExperience = 0;
                    
                    if(GetHasNode(elePlayer, XMLDefs.CARGO_SYSTEM))
                    {
                        cargo = GetCargoSystem(elePlayer, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.PLAYER));
                        cargo.SetCapacity(Long.MAX_VALUE);
                        oWealth = cargo.GetResourceMap().get(ResourceType.WEALTH);
                    }
                    else if(GetHasNode(elePlayer, XMLDefs.WEALTH))
                    {
                        oWealth = GetLongElement(elePlayer, XMLDefs.WEALTH);
                    }
                    
                    game.AddPlayer(new Player(lID, geoPosition, strName, lAvatarID, oLastSeen, lStateChange, lAllianceID, cFlags1, cFlags2, lAllianceCooloffTime, nKills, nDeaths, lOffenceSpending, lDefenceSpending, lDamageInflicted, lDamageReceived, lRank, lExperience, lTotalKills, lTotalDeaths, bMember, oJoinTime, lDefenseValue, lOffenseValue, lNeutralValue, fltDistanceTraveled, fltDistanceTraveledToday, lAirdropCooldown, lProspectCooldown, lCityCountLastWeek, lChampCount, bAdminMember, oWealth, Blacklist));
                }
                catch(Exception ex)
                {
                    listener.LoadError(String.format("Error loading player at index %d: %s.", i, ex.getMessage()));
                }
            }
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //Load icbms.
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading missiles...");
            
                    //Load missiles.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.MISSILE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleMissile = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleMissile, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleMissile, XMLDefs.POSITION);
                            int lType = GetIntElement(eleMissile, XMLDefs.TYPE);
                            int lOwnerID = GetIntElement(eleMissile, XMLDefs.OWNER_ID);
                            GeoCoord geoTarget = GetPositionElement(eleMissile, XMLDefs.TARGET);
                            GeoCoord geoOrigin = GetPositionElement(eleMissile, XMLDefs.ORIGIN);
                            boolean bAirburst = GetBooleanElement(eleMissile, XMLDefs.FUZE_MODE);
                            float fltSpeed = GetFloatElement(eleMissile, XMLDefs.SPEED, 1000f);

                            EntityPointer target = null;
                            if(GetHasNode(eleMissile, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleMissile, XMLDefs.TARGET_ENTITY);

                            Missile missile = new Missile(lID, geoPosition, lType, lOwnerID, fltSpeed, geoTarget, target, geoOrigin, bAirburst);
                            game.AddMissile(missile);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading missile at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Missiles loaded.");
                    bMissilesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading interceptors...");

                    //Load interceptors.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.INTERCEPTOR);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleInterceptor = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleInterceptor, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleInterceptor, XMLDefs.POSITION);
                            int lOwnerID = GetIntElement(eleInterceptor, XMLDefs.OWNER_ID);
                            int lTargetID = GetIntElement(eleInterceptor, XMLDefs.TARGET_ID);
                            int lType = GetIntElement(eleInterceptor, XMLDefs.TYPE);
                            boolean bPlayerLaunched = GetBooleanElement(eleInterceptor, XMLDefs.PLAYER_LAUNCHED);
                            byte cTargetType = GetByteElement(eleInterceptor, XMLDefs.TARGET_TYPE);
                            int lLaunchedBy = GetIntElement(eleInterceptor, XMLDefs.LAUNCHED_BY_ID);
                            float fltDistanceTraveled = GetFloatElement(eleInterceptor, XMLDefs.DISTANCE_TRAVELED, 0);

                            game.AddInterceptor(new Interceptor(lID, geoPosition, lOwnerID, lTargetID, lType, bPlayerLaunched, cTargetType, lLaunchedBy, fltDistanceTraveled));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading interceptor at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Interceptors loaded.");
                    bInterceptorsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading torpedoes...");

                    //Load torpedoes.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.TORPEDO);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleTorpedo = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleTorpedo, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleTorpedo, XMLDefs.POSITION);
                            int lType = GetIntElement(eleTorpedo, XMLDefs.TYPE);
                            int lOwnerID = GetIntElement(eleTorpedo, XMLDefs.OWNER_ID);
                            GeoCoord geoTarget = GetPositionElement(eleTorpedo, XMLDefs.TARGET);
                            boolean bHoming = GetBooleanElement(eleTorpedo, XMLDefs.HOMING);
                            float fltRange = GetFloatElement(eleTorpedo, XMLDefs.RANGE, 0);
                            float fltDistanceTraveled = GetFloatElement(eleTorpedo, XMLDefs.DISTANCE_TRAVELED, 0);
                            byte cState = GetByteElement(eleTorpedo, XMLDefs.STATE);

                            EntityPointer target = null;
                            if(GetHasNode(eleTorpedo, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleTorpedo, XMLDefs.TARGET_ENTITY);

                            Torpedo torpedo = new Torpedo(lID, geoPosition, lType, lOwnerID, bHoming, fltRange, fltDistanceTraveled, geoTarget, target, cState);
                            game.AddTorpedo(torpedo);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading torpedo at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Torpedoes loaded.");
                    bTorpedoesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading missile sites...");

                    //Load missile sites.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.MISSILE_SITE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleMissileSite = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleMissileSite, XMLDefs.ID);
                            String strName = GetStringAttribute(eleMissileSite, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleMissileSite, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleMissileSite, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleMissileSite, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleMissileSite, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleMissileSite, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleMissileSite, XMLDefs.STATE_TIME);
                            boolean bCanTakeICBM = GetBooleanElement(eleMissileSite, XMLDefs.ICBM_SILO);
                            MissileSystem missileSystem = GetMissileSystem(eleMissileSite, XMLDefs.MISSILE_SYSTEM);
                            byte cMode = GetByteElement(eleMissileSite, XMLDefs.MODE);
                            boolean bRetaliating = GetBooleanElement(eleMissileSite, XMLDefs.RETALIATING);
                            int lDelayRetaliate = GetIntElement(eleMissileSite, XMLDefs.RETALIATE_TIME);
                            int lTargetID = GetIntElement(eleMissileSite, XMLDefs.TARGET_ID);
                            boolean bVisible = GetBooleanElement(eleMissileSite, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleMissileSite, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleMissileSite, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleMissileSite, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleMissileSite, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddMissileSite(new MissileSite(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bCanTakeICBM, missileSystem, cMode, bRetaliating, lDelayRetaliate, lTargetID, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            listener.LoadError(String.format("Error loading missile site at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Missile sites loaded.");
                    bMissileSitesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            /*new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading artillery guns...");

                    //Load artillery guns
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.ARTILLERY_GUN);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleArtilleryGun = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleArtilleryGun, XMLDefs.ID);
                            String strName = GetStringAttribute(eleArtilleryGun, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleArtilleryGun, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleArtilleryGun, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleArtilleryGun, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleArtilleryGun, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleArtilleryGun, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleArtilleryGun, XMLDefs.STATE_TIME);
                            MissileSystem missileSystem = GetMissileSystem(eleArtilleryGun, XMLDefs.MISSILE_SYSTEM);
                            boolean bVisible = GetBooleanElement(eleArtilleryGun, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleArtilleryGun, XMLDefs.VISIBLE_TIME);
                            byte cMode = GetByteElement(eleArtilleryGun, XMLDefs.MODE);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleArtilleryGun, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleArtilleryGun, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleArtilleryGun, XMLDefs.BUILT_BY_ID);
                            }

                            FireOrder order = null;

                            if(GetHasNode(eleArtilleryGun, XMLDefs.RADIUS) && GetHasNode(eleArtilleryGun, XMLDefs.TARGET))
                            {
                                GeoCoord geoTarget = GetPositionElement(eleArtilleryGun, XMLDefs.ARTILLERY_TARGET);
                                float fltRadius = GetFloatElement(eleArtilleryGun, XMLDefs.RADIUS, 0);

                                order = new FireOrder(geoTarget, fltRadius);
                            }

                            game.AddArtilleryGun(new ArtilleryGun(lID, 
                                    geoPosition, 
                                    nHP, 
                                    nMaxHP, 
                                    strName, 
                                    lOwnerID, 
                                    cFlags, 
                                    lStateTime, 
                                    missileSystem, 
                                    bVisible, 
                                    lVisibleTime, 
                                    cMode, 
                                    order, 
                                    lBuiltByID, 
                                    resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading artillery gun at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Artillery guns loaded.");
                    bArtilleryGunsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();*/
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading SAM sites...");

                    //Load SAM sites.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SAM_SITE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleSAMSite = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleSAMSite, XMLDefs.ID);
                            String strName = GetStringAttribute(eleSAMSite, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleSAMSite, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleSAMSite, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleSAMSite, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleSAMSite, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleSAMSite, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleSAMSite, XMLDefs.STATE_TIME);
                            byte cMode = GetByteElement(eleSAMSite, XMLDefs.MODE);
                            MissileSystem missileSystem = GetMissileSystem(eleSAMSite, XMLDefs.INTERCEPTOR_SYSTEM);
                            boolean bAntiBallistic = GetBooleanElement(eleSAMSite, XMLDefs.ANTI_BALLISTIC);
                            float fltEngageSpeed = GetFloatElement(eleSAMSite, XMLDefs.ENGAGEMENT_SPEED, 0);
                            boolean bVisible = GetBooleanElement(eleSAMSite, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleSAMSite, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleSAMSite, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleSAMSite, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleSAMSite, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddSAMSite(new SAMSite(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, cMode, missileSystem, bAntiBallistic, fltEngageSpeed, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading SAM site at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "SAM sites loaded.");
                    bSAMsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading sentry guns...");

                    //Load sentry guns.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SENTRY_GUN);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleSentryGun = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleSentryGun, XMLDefs.ID);
                            String strName = GetStringAttribute(eleSentryGun, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleSentryGun, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleSentryGun, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleSentryGun, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleSentryGun, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleSentryGun, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleSentryGun, XMLDefs.STATE_TIME);
                            int lReloadRemaining = GetIntElement(eleSentryGun, XMLDefs.RELOAD_REMAINING);
                            boolean bVisible = GetBooleanElement(eleSentryGun, XMLDefs.VISIBLE);
                            boolean bWatchTower = GetBooleanElement(eleSentryGun, XMLDefs.WATCH_TOWER);
                            int lVisibleTime = GetIntElement(eleSentryGun, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleSentryGun, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleSentryGun, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleSentryGun, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddSentryGun(new SentryGun(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, lReloadRemaining, bVisible, lVisibleTime, lBuiltByID, resources, bWatchTower));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading sentry gun at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Sentry guns loaded.");
                    bSentryGunsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading scrap yards...");

                    //Load sentry guns.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SCRAP_YARD);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleScrapYard = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleScrapYard, XMLDefs.ID);
                            String strName = GetStringAttribute(eleScrapYard, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleScrapYard, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleScrapYard, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleScrapYard, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleScrapYard, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleScrapYard, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleScrapYard, XMLDefs.STATE_TIME);
                            boolean bVisible = GetBooleanElement(eleScrapYard, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleScrapYard, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleScrapYard, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleScrapYard, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleScrapYard, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddScrapYard(new ScrapYard(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading scrap yard at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Scrapyards loaded.");
                    bScrapyardsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading ore mines...");

                    //Load ore mines.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.ORE_MINE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleOreMine = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleOreMine, XMLDefs.ID);
                            String strName = GetStringAttribute(eleOreMine, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleOreMine, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleOreMine, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleOreMine, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleOreMine, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleOreMine, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleOreMine, XMLDefs.STATE_TIME);
                            ResourceType type = ResourceType.values()[GetIntElement(eleOreMine, XMLDefs.TYPE)];
                            boolean bVisible = GetBooleanElement(eleOreMine, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleOreMine, XMLDefs.VISIBLE_TIME);
                            int lDepositID = GetIntElement(eleOreMine, XMLDefs.DEPOSIT_ID);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleOreMine, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleOreMine, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleOreMine, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddOreMine(new OreMine(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, lDepositID, bVisible, lVisibleTime, lBuiltByID, type, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading ore mine at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Ore mines loaded.");
                    bOreMinesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading radar stations...");

                    //Load radar stations.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.RADAR_STATION);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleRadarStation = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleRadarStation, XMLDefs.ID);
                            String strName = GetStringAttribute(eleRadarStation, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleRadarStation, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleRadarStation, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleRadarStation, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleRadarStation, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleRadarStation, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleRadarStation, XMLDefs.STATE_TIME);
                            boolean bRadarActive = GetBooleanElement(eleRadarStation, XMLDefs.RADAR_ACTIVE);
                            boolean bVisible = GetBooleanElement(eleRadarStation, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleRadarStation, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleRadarStation, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleRadarStation, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleRadarStation, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddRadarStation(new RadarStation(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bRadarActive, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading radar station at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Radar stations loaded.");
                    bRadarStationsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading processors...");

                    //Load ore mines.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.PROCESSOR);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleProcessor = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleProcessor, XMLDefs.ID);
                            String strName = GetStringAttribute(eleProcessor, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleProcessor, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleProcessor, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleProcessor, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleProcessor, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleProcessor, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleProcessor, XMLDefs.STATE_TIME);
                            ResourceType resourceType = ResourceType.values()[GetIntElement(eleProcessor, XMLDefs.TYPE)];
                            boolean bVisible = GetBooleanElement(eleProcessor, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleProcessor, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleProcessor, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleProcessor, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleProcessor, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddProcessor(new Processor(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, resourceType, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading processor at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Processors loaded.");
                    bProcessorsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading command posts...");

                    //Load radar stations.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.COMMAND_POST);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleCommandPost = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleCommandPost, XMLDefs.ID);
                            String strName = GetStringAttribute(eleCommandPost, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleCommandPost, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleCommandPost, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleCommandPost, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleCommandPost, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleCommandPost, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleCommandPost, XMLDefs.STATE_TIME);
                            boolean bVisible = GetBooleanElement(eleCommandPost, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleCommandPost, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleCommandPost, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleCommandPost, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleCommandPost, XMLDefs.BUILT_BY_ID);
                            }

                            game.AddCommandPost(new CommandPost(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading commandPost at index %d: %s.", i, ex.getMessage()));
                        }
                    }
            
                    LaunchLog.Log(APPLICATION, LOG_NAME, "CommandPosts loaded.");
                    bCommandPostsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading warehouses...");

                    //Load banks.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.WAREHOUSE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleWarehouse = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleWarehouse, XMLDefs.ID);
                            String strName = GetStringAttribute(eleWarehouse, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleWarehouse, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleWarehouse, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleWarehouse, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleWarehouse, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleWarehouse, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleWarehouse, XMLDefs.STATE_TIME);
                            int lBuildTime = GetIntElement(eleWarehouse, XMLDefs.PREP_TIME);
                            boolean bVisible = GetBooleanElement(eleWarehouse, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleWarehouse, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            long oWealth = GetLongElement(eleWarehouse, XMLDefs.WEALTH);

                            game.AddWarehouse(new Warehouse(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, lBuildTime, bVisible, lVisibleTime, lBuiltByID, oWealth));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading warehouse at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Warehouses loaded.");
                    bWarehousesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading airbases...");

                    //Load airbases.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.AIRBASE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleAirbase = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleAirbase, XMLDefs.ID);
                            String strName = GetStringAttribute(eleAirbase, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleAirbase, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleAirbase, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleAirbase, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleAirbase, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleAirbase, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleAirbase, XMLDefs.STATE_TIME);
                            AircraftSystem aircrafts = GetAircraftSystem(eleAirbase, XMLDefs.AIRCRAFT_SYSTEM, game, new EntityPointer(lID, EntityType.AIRBASE));
                            boolean bVisible = GetBooleanElement(eleAirbase, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleAirbase, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleAirbase, XMLDefs.RESOURCE_CONTAINER);
                            
                            if(GetHasNode(eleAirbase, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleAirbase, XMLDefs.BUILT_BY_ID);
                            }

                            Airbase airbase = new Airbase(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, aircrafts, bVisible, lVisibleTime, lBuiltByID, resources);
                            game.AddAirbase(airbase);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading air base at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Airbases loaded.");
                    bAirbasesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading armories...");

                    //Load armories.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.ARMORY);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleArmory = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleArmory, XMLDefs.ID);
                            String strName = GetStringAttribute(eleArmory, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleArmory, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleArmory, XMLDefs.HP);
                            short nMaxHP = GetShortElement(eleArmory, XMLDefs.MAX_HP);
                            int lOwnerID = GetIntElement(eleArmory, XMLDefs.OWNER_ID);
                            byte cFlags = GetByteElement(eleArmory, XMLDefs.FLAGS);
                            int lStateTime = GetIntElement(eleArmory, XMLDefs.STATE_TIME);
                            int lBuildTime = GetIntElement(eleArmory, XMLDefs.PREP_TIME);
                            boolean bProducing = GetBooleanElement(eleArmory, XMLDefs.PRODUCING);
                            EntityType type = EntityType.values()[GetByteElement(eleArmory, XMLDefs.STRUCTURE_TANK_TYPE)];
                            
                            if(type == EntityType.BARRACKS)
                                continue;
                            
                            boolean bVisible = GetBooleanElement(eleArmory, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleArmory, XMLDefs.VISIBLE_TIME);
                            int lBuiltByID = lOwnerID;
                            ResourceSystem resources = GetResourceSystem(eleArmory, XMLDefs.RESOURCE_CONTAINER);
                            boolean bBarracks = GetBooleanElement(eleArmory, XMLDefs.BARRACKS);
                            CargoSystem cargo = GetCargoSystem(eleArmory, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.ARMORY));
                            
                            if(GetHasNode(eleArmory, XMLDefs.BUILT_BY_ID))
                            {
                                lBuiltByID = GetIntElement(eleArmory, XMLDefs.BUILT_BY_ID);
                            }

                            Armory armory = new Armory(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, lBuildTime, bProducing, type, bVisible, lVisibleTime, cargo, lBuiltByID, bBarracks, resources);
                            game.AddArmory(armory);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading armory at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Armories loaded.");
                    bArmoriesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading aircrafts...");

                    //Load aircrafts.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.AIRCRAFT);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleAircraft = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleAircraft, XMLDefs.ID);
                            byte cMode = GetByteElement(eleAircraft, XMLDefs.MODE);
                            GeoCoord geoPosition = GetPositionElement(eleAircraft, XMLDefs.POSITION);
                            GeoCoord geoTarget = GetPositionElement(eleAircraft, XMLDefs.TARGET);
                            short nHP = GetShortElement(eleAircraft, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleAircraft, XMLDefs.MAX_HP); 
                            int lOwnerID = GetIntElement(eleAircraft, XMLDefs.OWNER_ID); 
                            MoveOrders orders = MoveOrders.values()[GetByteElement(eleAircraft, XMLDefs.MOVE_ORDERS)];
                            EntityPointer homebase = GetEntityPointerElement(eleAircraft, XMLDefs.HOME_BASE);
                            String strName = GetStringElement(eleAircraft, XMLDefs.NAME);
                            float fltCurrentFuel = GetFloatElement(eleAircraft, XMLDefs.CURRENT_FUEL, 0);
                            boolean bAutoReturn = GetBooleanElement(eleAircraft, XMLDefs.AUTO_RETURN);
                            boolean bRadarActive = GetBooleanElement(eleAircraft, XMLDefs.RADAR_ACTIVE);
                            int lCannonReload = GetIntElement(eleAircraft, XMLDefs.RELOAD_REMAINING);
                            int lElecWarReload = GetIntElement(eleAircraft, XMLDefs.ELECTRONIC_WARFARE_RELOAD);
                            boolean bVisible = GetBooleanElement(eleAircraft, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleAircraft, XMLDefs.VISIBLE_TIME);
                            int lTimeAirborne = GetIntElement(eleAircraft, XMLDefs.TIME_AIRBORNE);
                            Map<Integer, GeoCoord> Coordinates = null;
                            EntityType type = EntityType.values()[GetByteElement(eleAircraft, XMLDefs.AIRCRAFT_ROLE)];

                            if(GetHasNode(eleAircraft, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleAircraft, XMLDefs.COORDINATE_CHAIN);

                            MissileSystem missiles = null;
                            if(GetHasNode(eleAircraft, XMLDefs.MISSILE_SYSTEM))
                                 missiles = GetMissileSystem(eleAircraft, XMLDefs.MISSILE_SYSTEM);

                            MissileSystem interceptors = null;
                            if(GetHasNode(eleAircraft, XMLDefs.INTERCEPTOR_SYSTEM))
                                interceptors = GetMissileSystem(eleAircraft, XMLDefs.INTERCEPTOR_SYSTEM);

                            CargoSystem cargo = null;
                            if(GetHasNode(eleAircraft, XMLDefs.CARGO_SYSTEM))
                                cargo = GetCargoSystem(eleAircraft, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.AIRPLANE));

                            EntityPointer target = null;
                            if(GetHasNode(eleAircraft, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleAircraft, XMLDefs.TARGET_ENTITY);

                            game.AddAircraft(new Airplane(lID, 
                                                        geoPosition, 
                                                        geoTarget,
                                                        nHP, 
                                                        nMaxHP, 
                                                        lOwnerID, 
                                                        type,
                                                        orders, 
                                                        homebase,
                                                        strName,
                                                        fltCurrentFuel,
                                                        bAutoReturn,
                                                        missiles,
                                                        interceptors,
                                                        cargo,
                                                        lCannonReload,
                                                        target,
                                                        lElecWarReload, 
                                                        bVisible, 
                                                        lVisibleTime,
                                                        lTimeAirborne,
                                                        bRadarActive,
                                                        cMode,
                                                        Coordinates));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading aircraft at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Airplanes loaded.");
                    bAircraftsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            /*new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading infantries...");

                    //Load infantry.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.INFANTRY);

                    //TODO
                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleInfantry = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleInfantry, XMLDefs.ID);
                            String strName = GetStringAttribute(eleInfantry, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleInfantry, XMLDefs.POSITION);
                            GeoCoord geoTarget = GetPositionElement(eleInfantry, XMLDefs.TARGET);
                            short nHP = GetShortElement(eleInfantry, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleInfantry, XMLDefs.MAX_HP); 
                            int lOwnerID = GetIntElement(eleInfantry, XMLDefs.OWNER_ID); 
                            MoveOrders orders = MoveOrders.values()[GetByteElement(eleInfantry, XMLDefs.MOVE_ORDERS)];
                            float fltLastBearing = GetFloatElement(eleInfantry, XMLDefs.LAST_BEARING, 0);
                            boolean bVisible = GetBooleanElement(eleInfantry, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleInfantry, XMLDefs.VISIBLE_TIME);
                            int lUnderAttack = GetIntElement(eleInfantry, XMLDefs.UNDER_ATTACK_TIME);
                            float fltFuel = GetFloatElement(eleInfantry, XMLDefs.CURRENT_FUEL, 1.0f);
                            ResourceSystem resources = GetResourceSystem(eleInfantry, XMLDefs.RESOURCE_CONTAINER);
                            Map<Integer, GeoCoord> Coordinates = null;

                            if(GetHasNode(eleInfantry, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleInfantry, XMLDefs.COORDINATE_CHAIN);

                            EntityPointer target = null;
                            if(GetHasNode(eleInfantry, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleInfantry, XMLDefs.TARGET_ENTITY);

                            game.AddInfantry(new Infantry(lID, 
                                    geoPosition, 
                                    fltLastBearing, 
                                    nHP, 
                                    nMaxHP, 
                                    strName, 
                                    lOwnerID,
                                    lUnderAttack, 
                                    orders, 
                                    geoTarget,
                                    target, 
                                    bVisible, 
                                    lVisibleTime, 
                                    resources,
                                    fltFuel,
                                    Coordinates));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading infantry at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Infantries loaded.");
                    bInfantriesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();*/
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading tanks...");

                    //Load tanks.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.TANK);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleTank = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleTank, XMLDefs.ID);
                            String strName = GetStringAttribute(eleTank, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleTank, XMLDefs.POSITION);
                            GeoCoord geoTarget = GetPositionElement(eleTank, XMLDefs.TARGET);
                            short nHP = GetShortElement(eleTank, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleTank, XMLDefs.MAX_HP); 
                            int lOwnerID = GetIntElement(eleTank, XMLDefs.OWNER_ID);
                            MoveOrders orders = MoveOrders.values()[GetByteElement(eleTank, XMLDefs.MOVE_ORDERS)];
                            byte cMode = GetByteElement(eleTank, XMLDefs.MODE);
                            EntityType type = EntityType.values()[GetIntElement(eleTank, XMLDefs.TANK_TYPE)];
                            
                            if(type != EntityType.MBT)
                                continue;
                            
                            boolean bVisible = GetBooleanElement(eleTank, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleTank, XMLDefs.VISIBLE_TIME);
                            int lReloadRemaining = GetIntElement(eleTank, XMLDefs.RELOAD_REMAINING);
                            int lUnderAttack = GetIntElement(eleTank, XMLDefs.UNDER_ATTACK_TIME);
                            float fltFuel = GetFloatElement(eleTank, XMLDefs.CURRENT_FUEL, 1.0f);
                            ResourceSystem resources = GetResourceSystem(eleTank, XMLDefs.RESOURCE_CONTAINER);
                            Map<Integer, GeoCoord> Coordinates = null;
                            
                            MissileSystem launchables = new MissileSystem();
            
                            if(GetHasNode(eleTank, XMLDefs.MISSILE_SYSTEM))
                                launchables = GetMissileSystem(eleTank, XMLDefs.MISSILE_SYSTEM);

                            if(GetHasNode(eleTank, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleTank, XMLDefs.COORDINATE_CHAIN);

                            game.AddTank(new Tank(lID, geoPosition, nHP, nMaxHP, cMode, strName, lOwnerID, lUnderAttack, orders, geoTarget, lReloadRemaining, launchables, type, bVisible, lVisibleTime, resources, fltFuel, Coordinates));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading tank at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Tanks loaded.");
                    bTanksLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading trucks...");

                    //Load cargo trucks.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.CARGO_TRUCK);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleCargoTruck = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleCargoTruck, XMLDefs.ID);
                            String strName = GetStringAttribute(eleCargoTruck, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleCargoTruck, XMLDefs.POSITION);
                            GeoCoord geoTarget = GetPositionElement(eleCargoTruck, XMLDefs.TARGET);
                            short nHP = GetShortElement(eleCargoTruck, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleCargoTruck, XMLDefs.MAX_HP); 
                            int lOwnerID = GetIntElement(eleCargoTruck, XMLDefs.OWNER_ID);
                            CargoSystem cargo = GetCargoSystem(eleCargoTruck, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.CARGO_TRUCK));
                            LootType typeToDeliver = LootType.values()[GetIntElement(eleCargoTruck, XMLDefs.DELIVER_TYPE)];
                            int lCargoID = GetIntElement(eleCargoTruck, XMLDefs.CARGO_ID);
                            int lQuantityToDeliver = GetIntElement(eleCargoTruck, XMLDefs.QUANTITY_TO_DELIVER);
                            boolean bVisible = GetBooleanElement(eleCargoTruck, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleCargoTruck, XMLDefs.VISIBLE_TIME);
                            int lUnderAttack = GetIntElement(eleCargoTruck, XMLDefs.UNDER_ATTACK_TIME);
                            MoveOrders orders = MoveOrders.values()[GetByteElement(eleCargoTruck, XMLDefs.MOVE_ORDERS)];
                            float fltFuel = GetFloatElement(eleCargoTruck, XMLDefs.CURRENT_FUEL, 1.0f);
                            ResourceSystem resources = GetResourceSystem(eleCargoTruck, XMLDefs.RESOURCE_CONTAINER);
                            Map<Integer, GeoCoord> Coordinates = null;

                            if(GetHasNode(eleCargoTruck, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleCargoTruck, XMLDefs.COORDINATE_CHAIN);

                            game.AddCargoTruck(new CargoTruck(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, lUnderAttack, orders, geoTarget, cargo, typeToDeliver, lCargoID, lQuantityToDeliver, bVisible, lVisibleTime, resources, fltFuel, Coordinates));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading cargo truck at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Trucks loaded.");
                    bTrucksLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //Load ships.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SHIP);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleShip = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleShip, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleShip, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleShip, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleShip, XMLDefs.MAX_HP);
                            String strName = GetStringAttribute(eleShip, XMLDefs.NAME);
                            int lOwnerID = GetIntElement(eleShip, XMLDefs.OWNER_ID); 
                            MoveOrders moveOrders = MoveOrders.values()[GetByteElement(eleShip, XMLDefs.MOVE_ORDERS)];
                            GeoCoord geoTarget = GetPositionElement(eleShip, XMLDefs.TARGET);
                            int lSonarCooldown = GetIntElement(eleShip, XMLDefs.SONAR_COOLDOWN);
                            float fltCurrentFuel = GetFloatElement(eleShip, XMLDefs.CURRENT_FUEL, 0);
                            EntityType type = EntityType.values()[GetByteElement(eleShip, XMLDefs.SHIP_TYPE)];
                            boolean bVisible = GetBooleanElement(eleShip, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleShip, XMLDefs.VISIBLE_TIME);
                            byte cMode = GetByteElement(eleShip, XMLDefs.MODE);
                            boolean bRadarActive = GetBooleanElement(eleShip, XMLDefs.RADAR_ACTIVE);
                            Map<Integer, GeoCoord> Coordinates = null;

                            if(GetHasNode(eleShip, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleShip, XMLDefs.COORDINATE_CHAIN);

                            MissileSystem missiles = null;
                            MissileSystem torpedoes = null;
                            MissileSystem interceptors = null;
                            CargoSystem cargo = null;
                            AircraftSystem aircrafts = null;
                            MissileSystem artillery = null;
                            List<ShortDelay> Sentries = null;

                            if(GetHasNode(eleShip, XMLDefs.MISSILE_SYSTEM))
                                missiles = GetMissileSystem(eleShip, XMLDefs.MISSILE_SYSTEM);

                            if(GetHasNode(eleShip, XMLDefs.TORPEDO_SYSTEM))
                                torpedoes = GetMissileSystem(eleShip, XMLDefs.TORPEDO_SYSTEM);

                            if(GetHasNode(eleShip, XMLDefs.INTERCEPTOR_SYSTEM))
                                interceptors = GetMissileSystem(eleShip, XMLDefs.INTERCEPTOR_SYSTEM);

                            if(GetHasNode(eleShip, XMLDefs.CARGO_SYSTEM))
                                cargo = GetCargoSystem(eleShip, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.SHIP));

                            if(GetHasNode(eleShip, XMLDefs.AIRCRAFT_SYSTEM))
                                aircrafts = GetAircraftSystem(eleShip, XMLDefs.AIRCRAFT_SYSTEM, game, new EntityPointer(lID, EntityType.SHIP));

                            if(GetHasNode(eleShip, XMLDefs.ARTILLERY_SYSTEM))
                                artillery = GetMissileSystem(eleShip, XMLDefs.ARTILLERY_SYSTEM);

                            if(GetHasNode(eleShip, XMLDefs.SENTRY_SYSTEM))
                                Sentries = GetSentrySystem(eleShip, XMLDefs.SENTRY_SYSTEM);

                            EntityPointer target = null;
                            if(GetHasNode(eleShip, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleShip, XMLDefs.TARGET_ENTITY);

                            FireOrder order = null;

                            if(GetHasNode(eleShip, XMLDefs.RADIUS) && GetHasNode(eleShip, XMLDefs.TARGET))
                            {
                                GeoCoord geoArtilleryTarget = GetPositionElement(eleShip, XMLDefs.ARTILLERY_TARGET);
                                float fltRadius = GetFloatElement(eleShip, XMLDefs.RADIUS, 0);

                                order = new FireOrder(geoArtilleryTarget, fltRadius);
                            }

                            Ship ship = new Ship(lID, 
                                    geoPosition, 
                                    lOwnerID,
                                    nHP, 
                                    nMaxHP, 
                                    type, 
                                    strName, 
                                    moveOrders, 
                                    geoTarget, 
                                    target, 
                                    lSonarCooldown, 
                                    fltCurrentFuel, 
                                    missiles, 
                                    torpedoes, 
                                    cMode, 
                                    cargo, 
                                    artillery, 
                                    Sentries, 
                                    interceptors, 
                                    aircrafts, 
                                    bVisible, 
                                    lVisibleTime, 
                                    order, 
                                    bRadarActive, 
                                    Coordinates);
                            
                            game.AddShip(ship);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading ship at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Ships loaded.");
                    bShipsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //Load submarine.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SUBMARINE);

                    //TODO
                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleSubmarine = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleSubmarine, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleSubmarine, XMLDefs.POSITION);
                            short nHP = GetShortElement(eleSubmarine, XMLDefs.HP); 
                            short nMaxHP = GetShortElement(eleSubmarine, XMLDefs.MAX_HP);
                            String strName = GetStringAttribute(eleSubmarine, XMLDefs.NAME);
                            int lOwnerID = GetIntElement(eleSubmarine, XMLDefs.OWNER_ID); 
                            MoveOrders moveOrders = MoveOrders.values()[GetByteElement(eleSubmarine, XMLDefs.MOVE_ORDERS)];
                            GeoCoord geoTarget = GetPositionElement(eleSubmarine, XMLDefs.TARGET);
                            int lSonarCooldown = GetIntElement(eleSubmarine, XMLDefs.SONAR_COOLDOWN);
                            float fltCurrentFuel = GetFloatElement(eleSubmarine, XMLDefs.CURRENT_FUEL, 0);
                            boolean bSubmerged = GetBooleanElement(eleSubmarine, XMLDefs.SUBMERGED);
                            boolean bDiving = GetBooleanElement(eleSubmarine, XMLDefs.DIVING);
                            int lSubmergeTime = GetIntElement(eleSubmarine, XMLDefs.SUBMERGE_TIME);
                            boolean bVisible = GetBooleanElement(eleSubmarine, XMLDefs.VISIBLE);
                            int lVisibleTime = GetIntElement(eleSubmarine, XMLDefs.VISIBLE_TIME);
                            EntityType type = EntityType.values()[GetByteElement(eleSubmarine, XMLDefs.SUBMARINE_TYPE)];

                            Map<Integer, GeoCoord> Coordinates = null;

                            if(GetHasNode(eleSubmarine, XMLDefs.COORDINATE_CHAIN))
                                Coordinates = GetGeoCoordMap(eleSubmarine, XMLDefs.COORDINATE_CHAIN);

                            MissileSystem missiles = null;
                            MissileSystem torpedoes = null;
                            MissileSystem icbms = null;

                            if(GetHasNode(eleSubmarine, XMLDefs.MISSILE_SYSTEM))
                                missiles = GetMissileSystem(eleSubmarine, XMLDefs.MISSILE_SYSTEM);

                            if(GetHasNode(eleSubmarine, XMLDefs.TORPEDO_SYSTEM))
                                torpedoes = GetMissileSystem(eleSubmarine, XMLDefs.TORPEDO_SYSTEM);

                            if(GetHasNode(eleSubmarine, XMLDefs.ICBM_SYSTEM))
                                icbms = GetMissileSystem(eleSubmarine, XMLDefs.ICBM_SYSTEM);

                            EntityPointer target = null;
                            
                            if(GetHasNode(eleSubmarine, XMLDefs.TARGET_ENTITY))
                                target = GetEntityPointerElement(eleSubmarine, XMLDefs.TARGET_ENTITY);

                            Submarine submarine = new Submarine(lID, 
                                    geoPosition, 
                                    nHP, 
                                    nMaxHP, 
                                    type,
                                    strName, 
                                    lOwnerID, 
                                    moveOrders, 
                                    geoTarget, 
                                    target, 
                                    lSonarCooldown, 
                                    fltCurrentFuel, 
                                    missiles,
                                    torpedoes, 
                                    bSubmerged, 
                                    bDiving, 
                                    lSubmergeTime, 
                                    icbms, 
                                    bVisible, 
                                    lVisibleTime, 
                                    Coordinates);
                            
                            game.AddSubmarine(submarine);
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading submarine at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Submarines loaded.");
                    bSubmarinesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading blueprints...");

                    //Load loots.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.BLUEPRINT);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleBlueprint = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleBlueprint, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleBlueprint, XMLDefs.POSITION);
                            int lExpiry = GetIntElement(eleBlueprint, XMLDefs.EXPIRY);
                            int lCreatedByID = GetIntElement(eleBlueprint, XMLDefs.CREATED_BY_ID);
                            int lType = GetIntElement(eleBlueprint, XMLDefs.TYPE);
                            int lResourceType = GetIntElement(eleBlueprint, XMLDefs.RESOURCE_TYPE);

                            game.AddBlueprint(new Blueprint(lID, geoPosition, lExpiry, lCreatedByID, EntityType.values()[lType], ResourceType.values()[lResourceType]));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading blueprint at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Blueprints loaded.");
                    bBlueprintsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading loots...");

                    //Load loots.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.LOOT);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleLoot = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleLoot, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleLoot, XMLDefs.POSITION);
                            int lLootType = GetIntElement(eleLoot, XMLDefs.LOOT_TYPE);
                            int lType = GetIntElement(eleLoot, XMLDefs.TYPE);
                            long oQuantity = GetLongElement(eleLoot, XMLDefs.QUANTITY);
                            int lExpiry = GetIntElement(eleLoot, XMLDefs.EXPIRY);

                            game.AddLoot(new Loot(lID, geoPosition, LootType.values()[lLootType], lType, oQuantity, lExpiry));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading loot at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loots loaded.");
                    bLootsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading rubbles...");

                    //Load rubbles.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.RUBBLE);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleRubble = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleRubble, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleRubble, XMLDefs.POSITION);
                            EntityType structureType = EntityType.values()[GetByteElement(eleRubble, XMLDefs.STRUCTURE_TYPE)];
                            ResourceType resourceType = ResourceType.values()[GetByteElement(eleRubble, XMLDefs.RESOURCE_TYPE)];
                            int lOwnerID = GetIntElement(eleRubble, XMLDefs.OWNER_ID);
                            int lExpiry = GetIntElement(eleRubble, XMLDefs.EXPIRY);

                            game.AddRubble(new Rubble(lID, geoPosition, structureType, resourceType, lOwnerID, lExpiry));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading rubble at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Rubbles loaded.");
                    bRubblesLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading airdrops...");

                    //Load airdrops.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.AIRDROP);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleAirdrop = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleAirdrop, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleAirdrop, XMLDefs.POSITION);
                            int lExpiry = GetIntElement(eleAirdrop, XMLDefs.EXPIRY);
                            int lOwnerID = GetIntElement(eleAirdrop, XMLDefs.OWNER_ID);

                            game.AddAirdrop(new Airdrop(lID, lOwnerID, geoPosition, lExpiry));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading airdrop at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Airdrops loaded.");
                    bAirdropsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading resource deposits...");

                    //Load resource deposits.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.RESOURCE_DEPOSIT);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleDeposit = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleDeposit, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleDeposit, XMLDefs.POSITION);
                            ResourceType type = ResourceType.values()[GetIntElement(eleDeposit, XMLDefs.TYPE)];
                            long oReserves = GetLongElement(eleDeposit, XMLDefs.RESOURCES);

                            game.AddResourceDeposit(new ResourceDeposit(lID, geoPosition, type, oReserves));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading resource deposit at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Deposits loaded.");
                    bDepositsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading radiations...");

                    //Load radiations.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.RADIATION);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleRadiation = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleRadiation, XMLDefs.ID);
                            GeoCoord geoPosition = GetPositionElement(eleRadiation, XMLDefs.POSITION);
                            float fltRadius = GetFloatElement(eleRadiation, XMLDefs.RADIUS, 0);
                            int lExpiry = GetIntElement(eleRadiation, XMLDefs.EXPIRY);
                            int lOwnerID = GetIntElement(eleRadiation, XMLDefs.OWNER_ID);

                            game.AddRadiation(new Radiation(lID, geoPosition, fltRadius, lExpiry, lOwnerID));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading radiation at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Radiations loaded.");
                    bRadiationsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Loading shipyards...");

                    //Load shipyards.
                    NodeList nodes = doc.getElementsByTagName(XMLDefs.SHIPYARD);

                    for(int i = 0; i < nodes.getLength(); i++)
                    {
                        try
                        {
                            Element eleShipyard = (Element)nodes.item(i);
                            int lID = GetIntAttribute(eleShipyard, XMLDefs.ID);
                            String strName = GetStringElement(eleShipyard, XMLDefs.NAME);
                            GeoCoord geoPosition = GetPositionElement(eleShipyard, XMLDefs.POSITION);
                            GeoCoord geoOutput = GetPositionElement(eleShipyard, XMLDefs.OUTPUT_COORD);
                            int lOwnerID = GetIntElement(eleShipyard, XMLDefs.OWNER_ID);
                            short nHP = GetShortElement(eleShipyard, XMLDefs.HP);
                            short nMaxHP = Defs.SHIPYARD_MAX_HP;
                            boolean bContested = GetBooleanElement(eleShipyard, XMLDefs.CONTESTED);
                            byte cCapacity = GetByteElement(eleShipyard, XMLDefs.PRODUCTION_CAPACITY);

                            if(cCapacity == 0)
                                cCapacity = 1;

                            List<ShipProductionOrder> Queue = new ArrayList<>();

                            if(GetHasNode(eleShipyard, XMLDefs.QUEUE))
                            {
                                Queue = GetShipyardQueue(eleShipyard, XMLDefs.QUEUE);
                            }
                            
                            game.AddShipyard(new Shipyard(lID, strName, geoPosition, geoOutput, lOwnerID, nHP, nMaxHP, bContested, cCapacity, Queue));
                        }
                        catch(Exception ex)
                        {
                            listener.LoadError(String.format("Error loading shipyard at index %d: %s.", i, ex.getMessage()));
                        }
                    }
                    
                    LaunchLog.Log(APPLICATION, LOG_NAME, "Shipyards loaded.");
                    bShipyardsLoaded = true;
                    AttemptFinalizeLoading(game, listener);
                }
            }).start();
        }
        catch(ParserConfigurationException ex)
        {
            listener.LoadError(String.format("XML parser configuration error when loading game from %s.", strGameFile));
        }
        catch(SAXException ex)
        {
            listener.LoadError(String.format("SAX exception when loading game from %s.", strGameFile));
        }
        catch(IOException ex)
        {
            listener.LoadError(String.format("IO error when loading game from %s.", strGameFile));
        }
    }
    
    private static void AttemptFinalizeLoading(LaunchServerGame game, GameLoadSaveListener listener)
    {
        if((bMissilesLoaded
        && bInterceptorsLoaded
        && bTorpedoesLoaded
        && bMissileSitesLoaded
        /*&& bArtilleryGunsLoaded*/
        && bSAMsLoaded
        && bSentryGunsLoaded
        && bScrapyardsLoaded
        && bOreMinesLoaded
        && bRadarStationsLoaded
        && bProcessorsLoaded
        && bCommandPostsLoaded
        && bWarehousesLoaded
        && bAirbasesLoaded
        && bArmoriesLoaded
        && bAircraftsLoaded
        /*&& bInfantriesLoaded*/
        && bTanksLoaded
        && bTrucksLoaded
        && bShipsLoaded
        && bSubmarinesLoaded
        && bBlueprintsLoaded
        && bLootsLoaded
        && bRubblesLoaded
        && bAirdropsLoaded
        && bDepositsLoaded
        && bRadiationsLoaded
        && bShipyardsLoaded)
        || bNewGame)
        {
            game.EstablishAllStructureThreats(LaunchEntity.ID_NONE);
            game.AssignEntityRelations();
            game.DoneLoading();
            listener.StartAPI();
            LaunchLog.Log(APPLICATION, LOG_NAME, "GAME LOADED.");
        }
    }
    
    public static void LoadShipyards(String strShipyardFile, LaunchServerGame game)
    {
        LaunchLog.Log(APPLICATION, LOG_NAME, "Loading shipyards...");
        
        try
        {            
            FileInputStream fstream = new FileInputStream("shipyards.txt");

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while((strLine = br.readLine()) != null)   
            {
                String replace = strLine.replace("\"", "");
                String stats[] = replace.split(",");
                
                try
                {
                    String strName = stats[0];
                    float fltLat = Float.parseFloat(stats[1]);
                    float fltLng = Float.parseFloat(stats[2]);
                    float fltOutputLat = Float.parseFloat(stats[3]);
                    float fltOutputLng = Float.parseFloat(stats[4]);
                    boolean bPortOnly = Boolean.parseBoolean(stats[5]);
                    GeoCoord geoPosition = new GeoCoord(fltLat, fltLng);
                    GeoCoord geoOutput = new GeoCoord(fltOutputLat, fltOutputLng);
                    
                    game.GenerateShipyard(strName, geoPosition, geoOutput, bPortOnly);
                    
                    LaunchLog.ConsoleMessage(String.format("Generated %s.", strName));
                }
                catch(Exception ex)
                {
                    LaunchLog.ConsoleMessage(ex.getMessage());
                }  
            }
            
            //Close the input stream
            in.close();
        }
        catch(Exception ex)
        {
            LaunchLog.ConsoleMessage(ex.getMessage());
        }
    }
    
    private static LinkedHashMap GetFloatHashMapByYield(Document doc, String strOfNamedType, String strAttrName, GameLoadSaveListener listener)
    {
        LinkedHashMap<Byte, Float> Result = new LinkedHashMap();
        
        NodeList ndeMissileSpeeds = doc.getElementsByTagName(strOfNamedType);
        for(int i = 0; i < ndeMissileSpeeds.getLength(); i++)
        {
            try
            {
                Element ndeMissileSpeed = (Element)ndeMissileSpeeds.item(i);
                byte cID = GetByteAttribute(ndeMissileSpeed, XMLDefs.ID);
                float fltAttribute = GetFloatAttribute(ndeMissileSpeed, strAttrName);
                Result.put(cID, fltAttribute);
            }
            catch(Exception ex)
            {
                listener.LoadError(String.format("Error loading %s at index %d: %s.", strOfNamedType, i, ex.getMessage()));
            }
        }
        
        return Result;
    }
    
    private static LinkedHashMap GetShortHashMap(Document doc, String strOfNamedType, String strAttrName, GameLoadSaveListener listener)
    {
        LinkedHashMap<Byte, Short> Result = new LinkedHashMap();
        
        NodeList ndeMissileSpeeds = doc.getElementsByTagName(strOfNamedType);
        
        for(int i = 0; i < ndeMissileSpeeds.getLength(); i++)
        {
            try
            {
                Element ndeMissileSpeed = (Element)ndeMissileSpeeds.item(i);
                byte cID = GetByteAttribute(ndeMissileSpeed, XMLDefs.ID);
                short nAttribute = GetShortAttribute(ndeMissileSpeed, strAttrName);
                Result.put(cID, nAttribute);
            }
            catch(Exception ex)
            {
                listener.LoadError(String.format("Error loading %s at index %d: %s.", strOfNamedType, i, ex.getMessage()));
            }
        }
        
        return Result;
    }
    
    private static MissileSystem GetMissileSystem(Element eleParent, String strTagName)
    {
        strLastHandled = "Missile system " + strTagName;
        
        Element eleMissileSystem = GetNode(eleParent, strTagName);
        
        int lReloadRemaining = GetIntElement(eleMissileSystem, XMLDefs.RELOAD_REMAINING);
        int lReloadTime = GetIntElement(eleMissileSystem, XMLDefs.RELOAD_TIME);
        int lSlotCount = GetIntElement(eleMissileSystem, XMLDefs.SLOT_COUNT);

        Map<Integer, Integer> SlotTypes = new HashMap();
        Map<Integer, ShortDelay> PrepTimes = new HashMap();
        NodeList ndeSlots = GetNodes(eleMissileSystem, XMLDefs.SLOT);

        for(int j = 0; j < ndeSlots.getLength(); j++)
        {
            Element eleSlot = (Element)ndeSlots.item(j);
            int lNumber = GetIntAttribute(eleSlot, XMLDefs.NUMBER);
            SlotTypes.put(lNumber, GetIntAttribute(eleSlot, XMLDefs.TYPE));
            PrepTimes.put(lNumber, new ShortDelay(GetIntAttribute(eleSlot, XMLDefs.PREP_TIME)));
        }
        
        return new MissileSystem(lReloadRemaining, lReloadTime, lSlotCount, SlotTypes, PrepTimes);
    }
    
    private static List<ShortDelay> GetSentrySystem(Element eleParent, String strTagName)
    {
        strLastHandled = "Sentry system " + strTagName;
        
        Element eleSentrySystem = GetNode(eleParent, strTagName);
        List<ShortDelay> Sentries = new ArrayList<>();
        NodeList ndeSentryGuns = GetNodes(eleSentrySystem, XMLDefs.CIWS_GUN);

        for(int j = 0; j < ndeSentryGuns.getLength(); j++)
        {
            Element eleSentry = (Element)ndeSentryGuns.item(j);
            int lReload = GetIntAttribute(eleSentry, XMLDefs.RELOAD_REMAINING);
            Sentries.add(new ShortDelay(lReload));
        }
        
        return Sentries;
    }
    
    private static CargoSystem GetCargoSystem(Element eleParent, String strTagName, LaunchServerGame game, EntityPointer host)
    {
        strLastHandled = "Cargo system " + strTagName;
        
        Element eleCargoSystem = GetNode(eleParent, strTagName);
        long oTonnageCapacity = GetLongElement(eleCargoSystem, XMLDefs.CAPACITY);

        Map<ResourceType, Long> resources = GetResourceMap(eleCargoSystem, XMLDefs.RESOURCES);
        Map<Integer, StoredInfantry> infantries = new HashMap();
        Map<Integer, StoredTank> tanks = new HashMap();
        Map<Integer, StoredCargoTruck> trucks = new HashMap();
        
        /*Element eleInfantries = GetNode(eleCargoSystem, XMLDefs.STORED_INFANTRIES);
        NodeList nodes = eleInfantries.getElementsByTagName(XMLDefs.STORED_INFANTRY);
        
        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleInfantry = (Element)nodes.item(j);
            int lID = GetIntAttribute(eleInfantry, XMLDefs.ID);
            String strName = GetStringElement(eleInfantry, XMLDefs.NAME);
            short nHP = GetShortElement(eleInfantry, XMLDefs.HP); 
            short nMaxHP = GetShortElement(eleInfantry, XMLDefs.MAX_HP); 
            int lOwnerID = GetIntElement(eleInfantry, XMLDefs.OWNER_ID); 
            int lPrep = GetIntElement(eleInfantry, XMLDefs.PREP_TIME);
            float fltFuel = GetFloatElement(eleInfantry, XMLDefs.CURRENT_FUEL, 1.0f);
            ResourceSystem resourceSystem = GetResourceSystem(eleInfantry, XMLDefs.RESOURCE_CONTAINER);
                    
            StoredInfantry infantry = new StoredInfantry(lID, nHP, nMaxHP, lOwnerID, strName, lPrep, host, resourceSystem, fltFuel);
            infantries.put(infantry.GetID(), infantry);
        }*/
        
        Element eleTanks = GetNode(eleCargoSystem, XMLDefs.STORED_TANKS);
        NodeList nodes = eleTanks.getElementsByTagName(XMLDefs.STORED_TANK);
        
        for(int j = 0; j < nodes.getLength(); j++)
        {     
            Element eleTank = (Element)nodes.item(j);
            int lID = GetIntAttribute(eleTank, XMLDefs.ID);
            String strName = GetStringElement(eleTank, XMLDefs.NAME);
            short nHP = GetShortElement(eleTank, XMLDefs.HP); 
            short nMaxHP = GetShortElement(eleTank, XMLDefs.MAX_HP); 
            int lOwnerID = GetIntElement(eleTank, XMLDefs.OWNER_ID); 
            int lPrep = GetIntElement(eleTank, XMLDefs.PREP_TIME);
            byte cMode = GetByteElement(eleTank, XMLDefs.MODE);
            EntityType type = EntityType.values()[GetIntElement(eleTank, XMLDefs.TANK_TYPE)];
            
            if(type == EntityType.SAM_TANK || type == EntityType.SPAAG || type == EntityType.HOWITZER || type == EntityType.MISSILE_TANK)
                continue;
            
            float fltFuel = GetFloatElement(eleTank, XMLDefs.CURRENT_FUEL, 1.0f);
            ResourceSystem resourceSystem = GetResourceSystem(eleTank, XMLDefs.RESOURCE_CONTAINER);
            
            MissileSystem launchables = new MissileSystem();
            
            if(GetHasNode(eleTank, XMLDefs.TANK_MISSILE_SYSTEM))
                launchables = GetMissileSystem(eleTank, XMLDefs.TANK_MISSILE_SYSTEM);
                    
            StoredTank tank = new StoredTank(lID, nHP, nMaxHP, strName, lOwnerID, lPrep, cMode, type, launchables, host, resourceSystem, fltFuel);
            tanks.put(tank.GetID(), tank);
        }
        
        Element eleCargoTrucks = GetNode(eleCargoSystem, XMLDefs.STORED_CARGO_TRUCKS);
        nodes = eleCargoTrucks.getElementsByTagName(XMLDefs.STORED_CARGO_TRUCK);
        
        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleTruck = (Element)nodes.item(j);
            int lID = GetIntAttribute(eleTruck, XMLDefs.ID);
            String strName = GetStringElement(eleTruck, XMLDefs.NAME);
            short nHP = GetShortElement(eleTruck, XMLDefs.HP); 
            short nMaxHP = GetShortElement(eleTruck, XMLDefs.MAX_HP); 
            int lOwnerID = GetIntElement(eleTruck, XMLDefs.OWNER_ID); 
            int lPrep = GetIntElement(eleTruck, XMLDefs.PREP_TIME);
            CargoSystem system = GetCargoSystem(eleTruck, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.STORED_CARGO_TRUCK));
            float fltFuel = GetFloatElement(eleTruck, XMLDefs.CURRENT_FUEL, 1.0f);
            ResourceSystem resourceSystem = GetResourceSystem(eleTruck, XMLDefs.RESOURCE_CONTAINER);
            
            StoredCargoTruck truck = new StoredCargoTruck(lID, nHP, nMaxHP, strName, lOwnerID, lPrep, system, host, resourceSystem, fltFuel);
            trucks.put(truck.GetID(), truck);
        }
        
        CargoSystem cargo = new CargoSystem(oTonnageCapacity, resources, infantries, tanks, trucks);
        return cargo;
    }
    
    private static ResourceSystem GetResourceSystem(Element eleParent, String strTagName)
    {
        strLastHandled = "Resource system " + strTagName;
        
        Element eleCargoSystem = GetNode(eleParent, strTagName);
        long oTonnageCapacity = GetLongElement(eleCargoSystem, XMLDefs.CAPACITY);

        Map<ResourceType, Long> resources = GetResourceMap(eleCargoSystem, XMLDefs.RESOURCES);
        
        ResourceSystem cargo = new ResourceSystem(oTonnageCapacity, resources);
        return cargo;
    }
    
    private static Map<ResourceType, Long> GetResourceMap(Element eleParent, String strTagName)
    {
        strLastHandled = "Resource map " + strTagName;
        
        Map<ResourceType, Long> resources = new HashMap();
        
        Element eleResources = GetNode(eleParent, strTagName);
        NodeList nodes = eleResources.getElementsByTagName(XMLDefs.RESOURCE);

        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleResource = (Element)nodes.item(j);
            String strResource = GetStringAttribute(eleResource, XMLDefs.TYPE);
            
            try
            {
                ResourceType type = ResourceType.valueOf(strResource);
                long oQuantity = GetLongAttribute(eleResource, XMLDefs.QUANTITY);
                resources.put(type, oQuantity);
            }
            catch(Exception ex)
            {
                LaunchLog.ConsoleMessage(String.format("Error loading resource map: Resource type %s not found.", strResource));
            }    
        }
        
        return resources;
    }
    
    private static List<ShipProductionOrder> GetShipyardQueue(Element eleParent, String strTagName)
    {
        strLastHandled = "Shipyard Queue " + strTagName;
        
        Element eleQueue = GetNode(eleParent, strTagName);
        List<ShipProductionOrder> Queue = new ArrayList<>();
        
        NodeList nodes = eleQueue.getElementsByTagName(XMLDefs.PRODUCTION_ORDER);
        
        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleOrder = (Element)nodes.item(j);
            int lID = GetIntElement(eleOrder, XMLDefs.PRODUCING_FOR_ID);
            long oBuildTime = GetLongElement(eleOrder, XMLDefs.PREP_TIME);
            EntityType typeUnderConstruction = null;

            if(GetHasNode(eleOrder, XMLDefs.SHIP_TYPE))
                typeUnderConstruction = EntityType.values()[GetByteElement(eleOrder, XMLDefs.SHIP_TYPE)];
            
            ShipProductionOrder order = new ShipProductionOrder(lID, oBuildTime, typeUnderConstruction);
            Queue.add(order);
        }
        
        return Queue;
    }
    
    private static AircraftSystem GetAircraftSystem(Element eleParent, String strTagName, LaunchServerGame game, EntityPointer host)
    {
        strLastHandled = "Aircraft system " + strTagName;
        
        Element eleAircraftSystem = GetNode(eleParent, strTagName);
        boolean bOpen = GetBooleanElement(eleAircraftSystem, XMLDefs.OPEN);
        //int lTakeoffTime = GetIntElement(eleAircraftSystem, XMLDefs.RELOAD_REMAINING);
        int lSlotCount = GetIntElement(eleAircraftSystem, XMLDefs.SLOT_COUNT);

        Map<Integer, StoredAirplane> AircraftMap = new HashMap();
        
        Element eleAircrafts = GetNode(eleAircraftSystem, XMLDefs.STORED_AIRCRAFTS);
        NodeList nodes = eleAircrafts.getElementsByTagName(XMLDefs.STORED_AIRCRAFT);

        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleStoredAircraft = (Element)nodes.item(j);
            int lID = GetIntAttribute(eleStoredAircraft, XMLDefs.ID);
            short nHP = GetShortElement(eleStoredAircraft, XMLDefs.HP); 
            short nMaxHP = GetShortElement(eleStoredAircraft, XMLDefs.MAX_HP); 
            int lOwnerID = GetIntElement(eleStoredAircraft, XMLDefs.OWNER_ID);
            String strName = GetStringElement(eleStoredAircraft, XMLDefs.NAME);
            float fltCurrentFuel = GetFloatElement(eleStoredAircraft, XMLDefs.CURRENT_FUEL, 0);
            boolean bAutoReturn = GetBooleanElement(eleStoredAircraft, XMLDefs.AUTO_RETURN);
            int lPrep = GetIntElement(eleStoredAircraft, XMLDefs.PREP_TIME);
            int lElecWarReload = GetIntElement(eleStoredAircraft, XMLDefs.ELECTRONIC_WARFARE_RELOAD);
            byte cMode = GetByteElement(eleStoredAircraft, XMLDefs.MODE);
            EntityType type = EntityType.values()[GetByteElement(eleStoredAircraft, XMLDefs.AIRCRAFT_ROLE)];

            MissileSystem missiles = null;
            if(GetHasNode(eleStoredAircraft, XMLDefs.MISSILE_SYSTEM))
                 missiles = GetMissileSystem(eleStoredAircraft, XMLDefs.MISSILE_SYSTEM);

            MissileSystem interceptors = null;
            if(GetHasNode(eleStoredAircraft, XMLDefs.INTERCEPTOR_SYSTEM))
                interceptors = GetMissileSystem(eleStoredAircraft, XMLDefs.INTERCEPTOR_SYSTEM);

            CargoSystem cargo = null;
            if(GetHasNode(eleStoredAircraft, XMLDefs.CARGO_SYSTEM))
                cargo = GetCargoSystem(eleStoredAircraft, XMLDefs.CARGO_SYSTEM, game, new EntityPointer(lID, EntityType.STORED_AIRPLANE));

            StoredAirplane aircraft = new StoredAirplane(lID, 
                nHP,
                nMaxHP,
                type,
                lOwnerID,
                strName,
                fltCurrentFuel,
                host,
                bAutoReturn,
                missiles,
                interceptors,
                cargo,
                lPrep,
                lElecWarReload,
                cMode);
                    
            AircraftMap.put(aircraft.GetID(), aircraft);
        }
        
        AircraftSystem aircraftSystem = new AircraftSystem(bOpen, 0, lSlotCount, AircraftMap);
        
        return aircraftSystem;
    }
    
    private static Element GetNode(Element element, String strNodeName)
    {
        strLastHandled = "Node " + strNodeName;
        return (Element)element.getElementsByTagName(strNodeName).item(0);
    }
    
    private static NodeList GetNodes(Element element, String strNodeName)
    {
        strLastHandled = "Nodes " + strNodeName;
        return element.getElementsByTagName(strNodeName);
    }
    
    private static boolean GetHasNode(Element element, String strTagName)
    {
        strLastHandled = "Has Node " + strTagName;
        return element.getElementsByTagName(strTagName).getLength() > 0;
    }
    
    private static Element GetElement(Element element, String strTagName)
    {
        strLastHandled = "Element " + strTagName;
        return (Element)element.getElementsByTagName(strTagName).item(0);
    }
    
    private static String GetStringAttribute(Element element, String strAttribute)
    {
        strLastHandled = "String attribute " + strAttribute;
        
        try
        {
            return element.getAttribute(strAttribute);
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get string attribute %s of %s. Returning empty string.", strAttribute, element.getNodeName()));
            return "";
        }
    }
    
    private static String GetStringRootElement(Document doc, String strTagName)
    {
        strLastHandled = "String root element " + strTagName;
        return doc.getElementsByTagName(strTagName).item(0).getTextContent();
    }
    
    private static int GetIntRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Int root element " + strTagName;
        return Integer.parseInt(doc.getElementsByTagName(strTagName).item(0).getTextContent());
    }
    
    private static long GetLongRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Long root element " + strTagName;
        return Long.parseLong(doc.getElementsByTagName(strTagName).item(0).getTextContent());
    }
    
    private static float GetFloatRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Float root element " + strTagName;
        return Float.parseFloat(doc.getElementsByTagName(strTagName).item(0).getTextContent());
    }
    
    private static short GetShortRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Short root element " + strTagName;
        return Short.parseShort(doc.getElementsByTagName(strTagName).item(0).getTextContent());
    }
    
    private static byte GetByteRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Byte root element " + strTagName;
        
        try
        {
            return Byte.parseByte(doc.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get byte root element %s. Returning default 0.", strTagName));
            return 0;
        }
    }
    
    private static boolean GetBooleanRootElement(Document doc, String strTagName)
    {
        strLastHandled = "Boolean element " + strTagName;
        
        try
        {
            return Boolean.parseBoolean(doc.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get boolean root element %s. Returning false.", strTagName));
            
            return false;
        }
    }
    
    private static byte GetByteElement(Element element, String strTagName)
    {
        strLastHandled = "Byte root element " + strTagName;
        
        try
        {
            return Byte.parseByte(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get byte element %s of %s.", strTagName, element.getNodeName()));
            
            if(strTagName.equals(XMLDefs.FLAGS) && (element.getNodeName().equals(XMLDefs.SAM_SITE) || element.getNodeName().equals(XMLDefs.MISSILE_SITE)))
            {
                currentListener.LoadWarning(String.format("Loading default state of %s for %s.", strTagName, element.getNodeName()));
                return (byte)0x80;
            }
            
            return 0;
        }
    }
    
    private static int GetIntElement(Element element, String strTagName)
    {
        strLastHandled = "Int element " + strTagName;
        
        try
        {
            return Integer.parseInt(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            if(strTagName.equals(XMLDefs.ASSET_ID))
            {
                currentListener.LoadWarning(String.format("Unable to get asset ID for %s. Returning default.", element.getNodeName()));
                return LaunchType.ASSET_ID_DEFAULT;
            }
            
            currentListener.LoadWarning(String.format("Unable to get int element %s of %s. Returning zero.", strTagName, element.getNodeName()));
            return 0;
        }
    }
    
    private static long GetLongElement(Element element, String strTagName)
    {
        strLastHandled = "Long element " + strTagName;
        
        try
        {
            return Long.parseLong(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            if(strTagName.equals(XMLDefs.NEXT_BAN_TIME))
            {
                currentListener.LoadWarning("Initialising as-yet uninitialised ban time.");
                return User.BAN_DURATION_INITIAL;
            }
            
            currentListener.LoadWarning(String.format("Unable to get long element %s of %s. Returning 0.", strTagName, element.getNodeName()));
            return 0;
        }
    }
    
    private static short GetShortElement(Element element, String strTagName)
    {
        strLastHandled = "Short element " + strTagName;
        
        try
        {
            return Short.parseShort(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get short element %s of %s. Returning 0.", strTagName, element.getNodeName()));
            return (short)0;
        }
    }
    
    private static String GetStringElement(Element element, String strTagName)
    {
        strLastHandled = "String element " + strTagName;
        
        try
        {
            return element.getElementsByTagName(strTagName).item(0).getTextContent();
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get string element %s of %s. Returning empty string.", strTagName, element.getNodeName()));
            return "";
        }
    }
    
    private static byte[] GetByteArrayElement(Element parent, String tag)
    {
        try 
        {
            String txt = parent.getElementsByTagName(tag).item(0).getTextContent().trim();

            return Base64.getDecoder().decode(txt);
        } 
        catch (Exception ex) 
        {
            currentListener.LoadWarning("Unable to load byte[] element " + tag + "; returning empty.");
            
            return new byte[0];
        }
    }
    
    private static boolean GetBooleanElement(Element element, String strTagName)
    {
        strLastHandled = "Boolean element " + strTagName;
        
        try
        {
            return Boolean.parseBoolean(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get boolean element %s of %s. Returning false.", strTagName, element.getNodeName()));
            
            return false;
        }
    }
    
    private static int GetIntAttribute(Element element, String strAttribute)
    {
        strLastHandled = "Int attribute " + strAttribute;
        return Integer.parseInt(element.getAttribute(strAttribute));
    }
    
    private static byte GetByteAttribute(Element element, String strAttribute)
    {
        strLastHandled = "Byte attribute " + strAttribute;
        return Byte.parseByte(element.getAttribute(strAttribute));
    }
    
    private static long GetLongAttribute(Element element, String strAttribute)
    {
        strLastHandled = "Long attribute " + strAttribute;
        return Long.parseLong(element.getAttribute(strAttribute));
    }
    
    private static float GetFloatAttribute(Element element, String strAttribute)
    {
        strLastHandled = "Float attribute " + strAttribute;
        return Float.parseFloat(element.getAttribute(strAttribute));
    }
    
    private static short GetShortAttribute(Element element, String strAttribute)
    {
        strLastHandled = "Short attribute " + strAttribute;
        return Short.parseShort(element.getAttribute(strAttribute));
    }
    
    private static float GetFloatElement(Element element, String strTagName, float fltDefault)
    {
        strLastHandled = "Float element " + strTagName;
        
        try
        {
            return Float.parseFloat(element.getElementsByTagName(strTagName).item(0).getTextContent());
        }
        catch(Exception ex)
        {
            currentListener.LoadWarning(String.format("Unable to get float element %s of %s. Returning %f.", strTagName, element.getNodeName(), fltDefault > 0 ? fltDefault : 0));
            
            return fltDefault > 0 ? fltDefault : 0;
        }
        
    }
    
    private static GeoCoord GetPositionElement(Element element, String strTagName)
    {
        strLastHandled = "Position element " + strTagName;
        Element elePosition = GetElement(element, strTagName);
        float fltLatitude = GetFloatElement(elePosition, XMLDefs.LATITUDE, 0);
        float fltLongitude = GetFloatElement(elePosition, XMLDefs.LONGITUDE, 0);
        
        return new GeoCoord(fltLatitude, fltLongitude);
    }
    
    private static EntityPointer GetEntityPointerElement(Element element, String strTagName)
    {
        strLastHandled = "Entity pointer element " + strTagName;
        Element eleEntity = GetElement(element, strTagName);
        EntityType type = EntityType.values()[GetIntElement(eleEntity, XMLDefs.TYPE)];
        int lID = GetIntElement(eleEntity, XMLDefs.ID);
        
        return new EntityPointer(lID, type);
    }
    
    private static Map<Integer, GeoCoord> GetGeoCoordMap(Element eleParent, String strTagName)
    {
        strLastHandled = "GeoCoord Map " + strTagName;
        
        Element eleCoordMap = GetNode(eleParent, strTagName);
        Map<Integer, GeoCoord> Map = new HashMap<>();
        
        NodeList nodes = eleCoordMap.getElementsByTagName(XMLDefs.COORDINATE);
        
        for(int j = 0; j < nodes.getLength(); j++)
        {
            Element eleCoordinate = (Element)nodes.item(j);
            int lOrder = GetIntAttribute(eleCoordinate, XMLDefs.ORDER);
            float fltLatitude = GetFloatAttribute(eleCoordinate, XMLDefs.LATITUDE);
            float fltLongitude = GetFloatAttribute(eleCoordinate, XMLDefs.LONGITUDE);
            
            Map.put(lOrder, new GeoCoord(fltLatitude, fltLongitude));
        }
        
        return Map;
    }
}
