/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.MissileSystem;
import launch.game.types.*;
import launch.utilities.LaunchBannedApp;
import launch.utilities.LaunchUtilities;
import launch.utilities.MissileStats;

/**
 * @author tobster
 */
public class Config
{
    protected static final int RULES_DATA_SIZE = 517 + (3 * 4);  //Rules, plus a 32-bit int count for missile/interceptor/torpedo type lists.

    //Server-only, not communicated.
    protected int lPort;

    //Server email address.
    protected String strServerEmailAddress;
    protected String strServerName;
    protected String strServerDescription;

    //Rules.
    protected byte cVariant;                      //Variant doesn't do anything but provide the server with a means of forcing a config change.
    protected boolean bDebugMode;
    protected int lStartingWealth;
    protected int lRespawnWealth;
    protected int lRespawnTime;
    protected int lRespawnProtectionTime;
    protected int lHourlyWealth;
    protected int lCMSSystemCost;
    protected int lSAMSystemCost;
    protected int lCMSStructureCost;
    protected int lNukeCMSStructureCost;
    protected int lSAMStructureCost;
    protected int lSentryGunStructureCost;
    protected int lOreMineStructureCost;
    protected float fltRubbleMinValue;
    protected float fltRubbleMaxValue;
    protected int lRubbleMinTime;
    protected int lRubbleMaxTime;
    protected float fltStructureSeparation;
    protected short nPlayerBaseHP;
    protected short nStructureBaseHP;
    protected int lStructureBootTime;
    protected byte cInitialMissileSlots;
    protected byte cInitialInterceptorSlots;
    protected float fltRequiredAccuracy;
    protected int lMinRadiationTime;
    protected int lMaxRadiationTime;
    protected int lMissileUpgradeBaseCost;
    protected byte cMissileUpgradeCount;
    
    protected float fltResaleValue;
    protected int lDecommissionTime;
    protected int lReloadTimeBase;
    protected int lReloadTimeStage1;
    protected int lReloadTimeStage2;
    protected int lReloadTimeStage3;
    protected int lReloadStage1Cost;
    protected int lReloadStage2Cost;
    protected int lReloadStage3Cost;
    protected float fltRepairSalvageDistance;
    protected int lMissileSiteMaintenanceCost;
    protected int lSAMSiteMaintenanceCost;
    protected int lSentryGunMaintenanceCost;
    protected int lOreMineMaintenanceCost;
    protected int lHealthInterval;
    protected int lRadiationInterval;
    protected int lPlayerRepairCost;
    protected int lStructureRepairCost;
    protected long oAWOLTime;
    protected long oRemoveTime;
    protected int lAllianceCooloffTime;
    protected float fltECMInterceptorChanceReduction;
    protected float fltManualInterceptorChanceIncrease;
    protected int lSentryGunReloadTime;
    protected float fltSentryGunRange;
    protected float fltSentryGunHitChance;
    protected float fltOreCollectRadius;
    protected int lOreMineGenerateTime;
    
    protected int lRadarStationMaintenanceCost; 
    protected int lRadarStationStructureCost;       
    protected float fltRadarRangeBase;
    protected float fltRadarRangeStage1;
    protected float fltRadarRangeStage2;
    protected float fltRadarRangeStage3;
    protected int lRadarRangeStage1Cost;
    protected int lRadarRangeStage2Cost;
    protected int lRadarRangeStage3Cost;
    protected int lABMSiloStructureCost;
    protected float fltBaseRadarAccuracyBonus;
    protected float fltRadarAccuracyBoostDistance;
    protected int lRadarBoostUpgradeCost;
    protected int lSentryGunRangeUpgradeCost;
    protected float fltStealthPerTickChance;
    protected int lJihadistThreshold;
    protected float fltOreGenerateRadius;
    protected int lOreMinExpiry;
    protected int lOreMaxExpiry;
    protected int lMaxOreValue;
    protected int lSolarPanelMaintenanceCost;
    protected int lSolarPanelStructureCost;
    protected short nSolarPanelBaseHP;
    protected int lSolarPanelGenerateTime;
    protected int lElecMinExpiry;
    protected int lElecMaxExpiry; 
    protected int lElecMinValue;
    protected int lElecMaxValue;
    
    protected float fltCommandPostShelterRadius;
    protected int lCommandPostHPUpgradeCost;
    protected short nCommandPostHPUpgradeHP;
    protected int lSiloBootUpgradeCost;
    protected int lSiloBootUpgradeTime;
    protected int lSiloMaxBootUpgrade;
    protected short nCommandPostMaxHPUpgrade;
    protected int lCommandPostStructureCost;
    protected short nCommandPostBaseHP;
    protected int lCommandPostMaintenanceCost;
    protected float fltMaxAccuracy;
    protected int lFarmGrowthTime;
    protected int lFarmStructureCost;
    protected int lFarmMaintenanceCost;
    protected short nFarmBaseHP;
    protected int lCropMaxValue;
    protected int lCropMinExpiry;
    protected int lCropMaxExpiry;
    protected float fltFarmGenerateRadius;
    protected int lMinCropCount;
    protected int lMaxCropCount;
    protected float fltFarmWorkRadius;
    protected int lAirbaseStructureCost;
    protected int lAirbaseMaintenanceCost;
    protected int lAirbaseBaseCapacity;
    protected short nAirbaseBaseHP;
    protected int lBomberCost;
    protected int lFighterCost;
    protected float fltAirspaceDistance;
    
    protected int lAircraftChaffCount;
    protected int lStrikeFighterCost;
    protected int lFastBomberCost;
    protected int lBankStructureCost;
    protected int lBankUpgradeCost;
    protected int lBankUpgradeCapacity;
    protected int lMaxBankCapacity;
    protected short nBankBaseHP;
    protected int lBankMaintenanceCost;
    protected float fltBankInterestRate;
    protected float fltLogDepotLinkRange;
    protected int lLogDepotMaintenanceCost;
    protected float fltLogDepotCollectRadius;
    protected int lLogDepotStructureCost;
    protected short nLogDepotBaseHP;
    protected int lMaxLogDepotCapacity;
    protected int lMissileFactoryStructureCost;
    protected int lMissileFactoryCooldown;
    protected int lMissileFactoryMaintenanceCost;
    protected int lBlueprintCost;
    protected int lBlueprintExpiry;
    protected float fltBlueprintBuildDistance;
    
    //Server-only.
    protected List<LaunchBannedApp> MinorCheatingApps = new ArrayList();
    protected List<LaunchBannedApp> MajorCheatingApps = new ArrayList();

    //Types.
    protected Map<Integer, MissileType> MissileTypes = new LinkedHashMap<>();
    protected Map<Integer, InterceptorType> InterceptorTypes = new LinkedHashMap<>();
    protected Map<Integer, TorpedoType> TorpedoTypes = new LinkedHashMap<>();

    //Communicable data.
    protected int lSize;
    protected byte[] cData;
    protected int lChecksum;
    
    //For testing.
    public Config()
    {}

    //From config data.
    public Config(String strServerEmailAddress,
            byte cVariant,
            String strServerName,
            String strServerDescription,
            boolean bDebugMode,
            int lStartingWealth,
            int lRespawnWealth,
            int lRespawnTime,
            int lRespawnProtectionTime,
            int lHourlyWealth,
            int lCMSSystemCost,
            int lSAMSystemCost,
            int lCMSStructureCost,
            int lNukeCMSStructureCost,
            int lSAMStructureCost,
            int lSentryGunStructureCost,
            int lOreMineStructureCost,
            float fltRubbleMinValue,
            float fltRubbleMaxValue,
            int lRubbleMinTime,
            int lRubbleMaxTime,
            float fltStructureSeparation,
            short nPlayerBaseHP,
            short nStructureBaseHP,
            int lStructureBootTime,
            byte cInitialMissileSlots,
            byte cInitialInterceptorSlots,
            float fltRequiredAccuracy,
            int lMinRadiationTime,
            int lMaxRadiationTime,
            int lMissileUpgradeBaseCost,
            byte cMissileUpgradeCount,
            float fltResaleValue,
            int lDecommissionTime,
            int lReloadTimeBase,
            int lReloadTimeStage1,
            int lReloadTimeStage2,
            int lReloadTimeStage3,
            int lReloadStage1Cost,
            int lReloadStage2Cost,
            int lReloadStage3Cost,
            float fltRepairSalvageDistance,
            int lMissileSiteMaintenanceCost,
            int lSAMSiteMaintenanceCost,
            int lSentryGunMaintenanceCost,
            int lOreMineMaintenanceCost,
            int lHealthInterval,
            int lRadiationInterval,
            int lPlayerRepairCost,
            int lStructureRepairCost,
            long oAWOLTime,
            long oRemoveTime,
            int lAllianceCooloffTime,
            float fltECMInterceptorChanceReduction,
            float fltManualInterceptorChanceIncrease,
            int lSentryGunReloadTime,
            float fltSentryGunRange,
            float fltSentryGunHitChance,
            float fltOreCollectRadius,
            int lOreMineGenerateTime,
            int lHourlyBonusDiplomaticPresence,
            int lHourlyBonusPoliticalEngagement,
            int lHourlyBonusDefenderOfTheNation,
            int lHourlyBonusNuclearSuperpower,
            int lHourlyBonusWeeklyKillsBatch,
            int lHourlyBonusSurvivor,
            int lHourlyBonusHippy,
            int lHourlyBonusPeaceMaker,
            int lHourlyBonusWarMonger,
            int lHourlyBonusLoneWolf,
            float fltLoneWolfDistance,
            int lRadarStationMaintenanceCost,
            int lRadarStationStructureCost,
            float fltRadarRangeBase,
            float fltRadarRangeStage1,
            float fltRadarRangeStage2,
            float fltRadarRangeStage3,
            int lRadarRangeStage1Cost,
            int lRadarRangeStage2Cost,
            int lRadarRangeStage3Cost,
            int lABMSiloStructureCost,
            float fltBaseRadarAccuracyBonus,
            float fltRadarAccuracyBoostDistance,
            int lRadarBoostUpgradeCost,
            int lSentryGunRangeUpgradeCost,
            float fltStealthPerTickChance,
            int lJihadistThreshold,
            float fltOreGenerateRadius,
            float fltOreMineSpacing,
            int lOreMinExpiry,
            int lOreMaxExpiry,
            int lMaxOreValue,
            int lSolarPanelMaintenanceCost,
            int lSolarPanelStructureCost,
            short nSolarPanelBaseHP,
            int lSolarPanelGenerateTime,
            int lElecMinExpiry,
            int lElecMaxExpiry,
            int lElecMinValue,
            int lElecMaxValue,
            float fltSolarPanelSpacing,
            float fltCommandPostShelterRadius,
            int lCommandPostHPUpgradeCost,
            short nCommandPostHPUpgradeHP,
            int lSiloBootUpgradeCost,
            int lSiloBootUpgradeTime,
            int lSiloMaxBootUpgrade,
            short nCommandPostMaxHPUpgrade,
            int lCommandPostStructureCost,
            short nCommandPostBaseHP,
            int lCommandPostMaintenanceCost,
            float fltMaxAccuracy,
            float fltFarmSpacing,
            int lFarmStructureCost,
            int lFarmMaintenanceCost,
            short nFarmBaseHP,
            int lCropMaxValue,
            int lCropMinExpiry,
            int lCropMaxExpiry,
            float fltFarmGenerateRadius,
            int lFarmGrowthTime,
            int lMinCropCount,
            int lMaxCropCount,
            float fltFarmWorkRadius,
            int lAirbaseStructureCost,
            int lAirbaseMaintenanceCost,
            int lAirbaseBaseCapacity,
            short nAirbaseBaseHP,
            int lBomberCost,
            int lFighterCost,
            float fltAirspaceDistance,
            int lAircraftChaffCount,
            int lStrikeFighterCost,
            int lFastBomberCost,
            int lBankStructureCost,
            int lBankUpgradeCost,
            int lBankUpgradeCapacity,
            int lMaxBankCapacity,
            short nBankBaseHP,
            int lBankMaintenanceCost,
            float fltBankInterestRate,
            float fltLogDepotLinkRange,
            int lLogDepotMaintenanceCost,
            float fltLogDepotCollectRadius,
            int lLogDepotStructureCost,
            short nLogDepotBaseHP,
            int lMaxLogDepotCapacity,
            int lMissileFactoryStructureCost,
            int lMissileFactoryCooldown,
            int lMissileFactoryMaintenanceCost,
            int lBlueprintCost,
            int lBlueprintExpiry,
            float fltBlueprintBuildDistance)            
    {
        this.strServerEmailAddress = strServerEmailAddress;

        this.cVariant = cVariant;
        this.strServerName = strServerName;
        this.strServerDescription = strServerDescription;
        this.bDebugMode = bDebugMode;
        this.lStartingWealth = lStartingWealth;
        this.lRespawnWealth = lRespawnWealth;
        this.lRespawnTime = lRespawnTime;
        this.lRespawnProtectionTime = lRespawnProtectionTime;
        this.lHourlyWealth = lHourlyWealth;
        this.lCMSSystemCost = lCMSSystemCost;
        this.lSAMSystemCost = lSAMSystemCost;
        this.lCMSStructureCost = lCMSStructureCost;
        this.lNukeCMSStructureCost = lNukeCMSStructureCost;
        this.lSAMStructureCost = lSAMStructureCost;
        this.lSentryGunStructureCost = lSentryGunStructureCost;
        this.lOreMineStructureCost = lOreMineStructureCost;
        this.fltRubbleMinValue = fltRubbleMinValue;
        this.fltRubbleMaxValue = fltRubbleMaxValue;
        this.lRubbleMinTime = lRubbleMinTime;
        this.lRubbleMaxTime = lRubbleMaxTime;
        this.fltStructureSeparation = fltStructureSeparation;
        this.nPlayerBaseHP = nPlayerBaseHP;
        this.nStructureBaseHP = nStructureBaseHP;
        this.lStructureBootTime = lStructureBootTime;
        this.cInitialMissileSlots = cInitialMissileSlots;
        this.cInitialInterceptorSlots = cInitialInterceptorSlots;
        this.fltRequiredAccuracy = fltRequiredAccuracy;
        this.lMinRadiationTime = lMinRadiationTime;
        this.lMaxRadiationTime = lMaxRadiationTime;
        this.lMissileUpgradeBaseCost = lMissileUpgradeBaseCost;
        this.cMissileUpgradeCount = cMissileUpgradeCount;
        this.fltResaleValue = fltResaleValue;
        this.lDecommissionTime = lDecommissionTime;
        this.lReloadTimeBase = lReloadTimeBase;
        this.lReloadTimeStage1 = lReloadTimeStage1;
        this.lReloadTimeStage2 = lReloadTimeStage2;
        this.lReloadTimeStage3 = lReloadTimeStage3;
        this.lReloadStage1Cost = lReloadStage1Cost;
        this.lReloadStage2Cost = lReloadStage2Cost;
        this.lReloadStage3Cost = lReloadStage3Cost;
        this.fltRepairSalvageDistance = fltRepairSalvageDistance;
        this.lMissileSiteMaintenanceCost = lMissileSiteMaintenanceCost;
        this.lSAMSiteMaintenanceCost = lSAMSiteMaintenanceCost;
        this.lSentryGunMaintenanceCost = lSentryGunMaintenanceCost;
        this.lOreMineMaintenanceCost = lOreMineMaintenanceCost;
        this.lHealthInterval = lHealthInterval;
        this.lRadiationInterval = lRadiationInterval;
        this.lPlayerRepairCost = lPlayerRepairCost;
        this.lStructureRepairCost = lStructureRepairCost;
        this.oAWOLTime = oAWOLTime;
        this.oRemoveTime = oRemoveTime;
        this.lAllianceCooloffTime = lAllianceCooloffTime;
        this.fltECMInterceptorChanceReduction = fltECMInterceptorChanceReduction;
        this.fltManualInterceptorChanceIncrease = fltManualInterceptorChanceIncrease;
        this.lSentryGunReloadTime = lSentryGunReloadTime;
        this.fltSentryGunRange = fltSentryGunRange;
        this.fltSentryGunHitChance = fltSentryGunHitChance;
        this.fltOreCollectRadius = fltOreCollectRadius;
        this.lOreMineGenerateTime = lOreMineGenerateTime;
        this.lRadarStationMaintenanceCost = lRadarStationMaintenanceCost;
        this.lRadarStationStructureCost = lRadarStationStructureCost;
        this.fltRadarRangeBase = fltRadarRangeBase;
        this.fltRadarRangeStage1 = fltRadarRangeStage1;
        this.fltRadarRangeStage2 = fltRadarRangeStage2;
        this.fltRadarRangeStage3 = fltRadarRangeStage3;
        this.lRadarRangeStage1Cost = lRadarRangeStage1Cost;
        this.lRadarRangeStage2Cost = lRadarRangeStage2Cost;
        this.lRadarRangeStage3Cost = lRadarRangeStage3Cost;
        this.lABMSiloStructureCost = lABMSiloStructureCost;
        this.fltBaseRadarAccuracyBonus = fltBaseRadarAccuracyBonus;
        this.fltRadarAccuracyBoostDistance = fltRadarAccuracyBoostDistance;
        this.lRadarBoostUpgradeCost = lRadarBoostUpgradeCost;
        this.lSentryGunRangeUpgradeCost = lSentryGunRangeUpgradeCost;
        this.fltStealthPerTickChance = fltStealthPerTickChance;
        this.lJihadistThreshold = lJihadistThreshold;
        this.fltOreGenerateRadius = fltOreGenerateRadius;
        this.lOreMinExpiry = lOreMinExpiry;
        this.lOreMaxExpiry = lOreMaxExpiry;
        this.lMaxOreValue = lMaxOreValue;
        this.lSolarPanelMaintenanceCost = lSolarPanelMaintenanceCost;
        this.lSolarPanelStructureCost = lSolarPanelStructureCost;
        this.nSolarPanelBaseHP = nSolarPanelBaseHP;
        this.lSolarPanelGenerateTime = lSolarPanelGenerateTime;
        this.lElecMinExpiry = lElecMinExpiry;
        this.lElecMaxExpiry = lElecMaxExpiry;
        this.lElecMinValue = lElecMinValue;
        this.lElecMaxValue = lElecMaxValue;
        this.fltCommandPostShelterRadius = fltCommandPostShelterRadius;
        this.lCommandPostHPUpgradeCost = lCommandPostHPUpgradeCost;
        this.nCommandPostHPUpgradeHP = nCommandPostHPUpgradeHP;
        this.lSiloBootUpgradeCost = lSiloBootUpgradeCost;
        this.lSiloBootUpgradeTime = lSiloBootUpgradeTime;
        this.lSiloMaxBootUpgrade = lSiloMaxBootUpgrade;
        this.nCommandPostMaxHPUpgrade = nCommandPostMaxHPUpgrade;
        this.lCommandPostStructureCost = lCommandPostStructureCost;
        this.nCommandPostBaseHP = nCommandPostBaseHP;
        this.lCommandPostMaintenanceCost = lCommandPostMaintenanceCost;
        this.fltMaxAccuracy = fltMaxAccuracy;
        this.lFarmStructureCost = lFarmStructureCost;
        this.lFarmMaintenanceCost = lFarmMaintenanceCost;
        this.nFarmBaseHP = nFarmBaseHP;
        this.lCropMaxValue = lCropMaxValue;
        this.lCropMinExpiry = lCropMinExpiry;
        this.lCropMaxExpiry = lCropMaxExpiry;
        this.fltFarmGenerateRadius = fltFarmGenerateRadius;
        this.lFarmGrowthTime = lFarmGrowthTime;
        this.lMinCropCount = lMinCropCount;
        this.lMaxCropCount = lMaxCropCount;
        this.fltFarmWorkRadius = fltFarmWorkRadius;
        this.lAirbaseStructureCost = lAirbaseStructureCost;
        this.lAirbaseMaintenanceCost = lAirbaseMaintenanceCost;
        this.lAirbaseBaseCapacity = lAirbaseBaseCapacity;
        this.nAirbaseBaseHP = nAirbaseBaseHP;
        this.lBomberCost = lBomberCost;
        this.lFighterCost = lFighterCost;
        this.fltAirspaceDistance = fltAirspaceDistance;
        this.lAircraftChaffCount = lAircraftChaffCount;
        this.lStrikeFighterCost = lStrikeFighterCost;
        this.lFastBomberCost = lFastBomberCost;
        this.lBankStructureCost = lBankStructureCost;
        this.lBankUpgradeCost = lBankUpgradeCost;
        this.lBankUpgradeCapacity = lBankUpgradeCapacity;
        this.lMaxBankCapacity = lMaxBankCapacity;
        this.nBankBaseHP = nBankBaseHP;
        this.lBankMaintenanceCost = lBankMaintenanceCost;
        this.fltBankInterestRate = fltBankInterestRate;
        this.fltLogDepotLinkRange = fltLogDepotLinkRange;
        this.lLogDepotMaintenanceCost = lLogDepotMaintenanceCost;
        this.fltLogDepotCollectRadius = fltLogDepotCollectRadius;
        this.lLogDepotStructureCost = lLogDepotStructureCost;
        this.nLogDepotBaseHP = nLogDepotBaseHP;
        this.lMaxLogDepotCapacity = lMaxLogDepotCapacity;
        this.lMissileFactoryStructureCost = lMissileFactoryStructureCost;
        this.lMissileFactoryCooldown = lMissileFactoryCooldown;
        this.lMissileFactoryMaintenanceCost = lMissileFactoryMaintenanceCost;
        this.lBlueprintCost = lBlueprintCost;
        this.lBlueprintExpiry = lBlueprintExpiry;
        this.fltBlueprintBuildDistance = fltBlueprintBuildDistance;
    }

    //Communicated.
    public Config(byte[] cConfigData)
    {
        cData = cConfigData;
        lSize = cConfigData.length;
        ByteBuffer bb = ByteBuffer.wrap(cData);

        strServerEmailAddress = LaunchUtilities.StringFromData(bb);

        //Assign rules.
        cVariant = bb.get();
        strServerName = LaunchUtilities.StringFromData(bb);
        strServerDescription = LaunchUtilities.StringFromData(bb);
        bDebugMode = (bb.get() != 0x00);
        lStartingWealth = bb.getInt();
        lRespawnWealth = bb.getInt();
        lRespawnTime = bb.getInt();
        lRespawnProtectionTime = bb.getInt();
        lHourlyWealth = bb.getInt();
        lCMSSystemCost = bb.getInt();
        lSAMSystemCost = bb.getInt();
        lCMSStructureCost = bb.getInt();
        lNukeCMSStructureCost = bb.getInt();
        lSAMStructureCost = bb.getInt();
        lSentryGunStructureCost = bb.getInt();
        lOreMineStructureCost = bb.getInt();
        fltRubbleMinValue = bb.getFloat();
        fltRubbleMaxValue = bb.getFloat();
        lRubbleMinTime = bb.getInt();
        lRubbleMaxTime = bb.getInt();
        fltStructureSeparation = bb.getFloat();
        nPlayerBaseHP = bb.getShort();
        nStructureBaseHP = bb.getShort();
        lStructureBootTime = bb.getInt();
        cInitialMissileSlots = bb.get();
        cInitialInterceptorSlots = bb.get();
        fltRequiredAccuracy = bb.getFloat();
        lMinRadiationTime = bb.getInt();
        lMaxRadiationTime = bb.getInt();
        lMissileUpgradeBaseCost = bb.getInt();
        cMissileUpgradeCount = bb.get();
        fltResaleValue = bb.getFloat();
        lDecommissionTime = bb.getInt();
        lReloadTimeBase = bb.getInt();
        lReloadTimeStage1 = bb.getInt();
        lReloadTimeStage2 = bb.getInt();
        lReloadTimeStage3 = bb.getInt();
        lReloadStage1Cost = bb.getInt();
        lReloadStage2Cost = bb.getInt();
        lReloadStage3Cost = bb.getInt();
        fltRepairSalvageDistance = bb.getFloat();
        lMissileSiteMaintenanceCost = bb.getInt();
        lSAMSiteMaintenanceCost = bb.getInt();
        lSentryGunMaintenanceCost = bb.getInt();
        lOreMineMaintenanceCost = bb.getInt();
        lHealthInterval = bb.getInt();
        lRadiationInterval = bb.getInt();
        lPlayerRepairCost = bb.getInt();
        lStructureRepairCost = bb.getInt();
        oAWOLTime = bb.getLong();
        oRemoveTime = bb.getLong();
        lAllianceCooloffTime = bb.getInt();
        fltECMInterceptorChanceReduction = bb.getFloat();
        fltManualInterceptorChanceIncrease = bb.getFloat();
        lSentryGunReloadTime = bb.getInt();
        fltSentryGunRange = bb.getFloat();
        fltSentryGunHitChance = bb.getFloat();
        fltOreCollectRadius = bb.getFloat();
        lOreMineGenerateTime = bb.getInt();
        lRadarStationMaintenanceCost = bb.getInt();
        lRadarStationStructureCost = bb.getInt();     
        fltRadarRangeBase = bb.getFloat();
        fltRadarRangeStage1 = bb.getFloat();
        fltRadarRangeStage2 = bb.getFloat();
        fltRadarRangeStage3 = bb.getFloat();
        lRadarRangeStage1Cost = bb.getInt();
        lRadarRangeStage2Cost = bb.getInt();
        lRadarRangeStage3Cost = bb.getInt();
        lABMSiloStructureCost = bb.getInt();
        fltBaseRadarAccuracyBonus = bb.getFloat();
        fltRadarAccuracyBoostDistance = bb.getFloat();
        lRadarBoostUpgradeCost = bb.getInt();
        lSentryGunRangeUpgradeCost = bb.getInt();
        fltStealthPerTickChance = bb.getFloat();
        lJihadistThreshold = bb.getInt();
        fltOreGenerateRadius = bb.getFloat();
        lOreMinExpiry = bb.getInt();
        lOreMaxExpiry = bb.getInt();
        lMaxOreValue = bb.getInt();
        lSolarPanelMaintenanceCost = bb.getInt();
        lSolarPanelStructureCost = bb.getInt();
        nSolarPanelBaseHP = bb.getShort();
        lSolarPanelGenerateTime = bb.getInt();
        lElecMinExpiry = bb.getInt();
        lElecMaxExpiry = bb.getInt();
        lElecMinValue = bb.getInt();
        lElecMaxValue = bb.getInt();
        fltCommandPostShelterRadius = bb.getFloat();
        lCommandPostHPUpgradeCost = bb.getInt();
        nCommandPostHPUpgradeHP = bb.getShort();
        lSiloBootUpgradeCost = bb.getInt();
        lSiloBootUpgradeTime = bb.getInt();
        lSiloMaxBootUpgrade = bb.getInt();
        nCommandPostMaxHPUpgrade = bb.getShort();
        lCommandPostStructureCost = bb.getInt();
        nCommandPostBaseHP = bb.getShort();
        lCommandPostMaintenanceCost = bb.getInt();
        fltMaxAccuracy = bb.getFloat();
        lFarmStructureCost = bb.getInt();
        lFarmMaintenanceCost = bb.getInt();
        nFarmBaseHP = bb.getShort();
        lCropMaxValue = bb.getInt();
        lCropMinExpiry = bb.getInt();
        lCropMaxExpiry = bb.getInt();
        fltFarmGenerateRadius = bb.getFloat();
        lFarmGrowthTime = bb.getInt();
        lMinCropCount = bb.getInt();
        lMaxCropCount = bb.getInt();
        fltFarmWorkRadius = bb.getFloat();
        lAirbaseStructureCost = bb.getInt();
        lAirbaseMaintenanceCost = bb.getInt();
        lAirbaseBaseCapacity = bb.getInt();
        nAirbaseBaseHP = bb.getShort();
        lBomberCost = bb.getInt();
        lFighterCost = bb.getInt();
        fltAirspaceDistance = bb.getFloat();
        lAircraftChaffCount = bb.getInt();
        lStrikeFighterCost = bb.getInt();
        lFastBomberCost = bb.getInt();
        lBankStructureCost = bb.getInt();
        lBankUpgradeCost = bb.getInt();
        lBankUpgradeCapacity = bb.getInt();
        lMaxBankCapacity = bb.getInt();
        nBankBaseHP = bb.getShort();
        lBankMaintenanceCost = bb.getInt();
        fltBankInterestRate = bb.getFloat();
        fltLogDepotLinkRange = bb.getFloat();
        lLogDepotMaintenanceCost = bb.getInt();
        fltLogDepotCollectRadius = bb.getFloat();
        lLogDepotStructureCost = bb.getInt();
        nLogDepotBaseHP = bb.getShort();
        lMaxLogDepotCapacity = bb.getInt();
        lMissileFactoryStructureCost = bb.getInt();
        lMissileFactoryCooldown = bb.getInt();
        lMissileFactoryMaintenanceCost = bb.getInt();
        lBlueprintCost = bb.getInt();
        lBlueprintExpiry = bb.getInt();
        fltBlueprintBuildDistance = bb.getFloat();
        
        //Assign types.
        int lMissileTypes = bb.getInt();

        for(int i = 0; i < lMissileTypes; i++)
        {
            MissileType missileType = new MissileType(bb);
            MissileTypes.put(missileType.GetID(), missileType);
        }

        int lInterceptorTypes = bb.getInt();

        for(int i = 0; i < lInterceptorTypes; i++)
        {
            InterceptorType interceptorType = new InterceptorType(bb);
            InterceptorTypes.put(interceptorType.GetID(), interceptorType);
        }
        
        int lTorpedoTypes = bb.getInt();

        for(int i = 0; i < lTorpedoTypes; i++)
        {
            TorpedoType torpedoType = new TorpedoType(bb);
            TorpedoTypes.put(torpedoType.GetID(), torpedoType);
        }

        
        //Compute checksum.
        CRC32 crc32 = new CRC32();
        crc32.update(cData, 0, cData.length);
        lChecksum = (int)crc32.getValue();
    }

    public void AddMissileType(int lID, MissileType missileType)
    {
        MissileTypes.put(lID, missileType);
    }

    public void AddInterceptorType(int lID, InterceptorType interceptorType)
    {
        InterceptorTypes.put(lID, interceptorType);
    }
    
    public void AddTorpedoType(int lID, TorpedoType torpedoType)
    {
        TorpedoTypes.put(lID, torpedoType);
    }

    public void AddMinorCheatingApp(LaunchBannedApp app)
    {
        MinorCheatingApps.add(app);
    }

    public void AddMajorCheatingApp(LaunchBannedApp app)
    {
        MajorCheatingApps.add(app);
    }

    public void Finalise()
    {
        //Compute data size.
        lSize = RULES_DATA_SIZE + LaunchUtilities.GetStringDataSize(strServerEmailAddress) + LaunchUtilities.GetStringDataSize(strServerName) + LaunchUtilities.GetStringDataSize(strServerDescription);

        for(MissileType missileType : MissileTypes.values())
        {
            lSize += missileType.GetData().length;
        }

        for(InterceptorType interceptorType : InterceptorTypes.values())
        {
            lSize += interceptorType.GetData().length;
        }
        
        int lTorpedoDataSize = 0;
        
        for(TorpedoType torpedoType : TorpedoTypes.values())
        {
            lSize += torpedoType.GetData().length;
            lTorpedoDataSize += torpedoType.GetData().length;
        }

        //Populate data object.
        ByteBuffer bb = ByteBuffer.allocate(lSize);

        bb.put(LaunchUtilities.GetStringData(strServerEmailAddress));

        bb.put(cVariant);
        bb.put(LaunchUtilities.GetStringData(strServerName));
        bb.put(LaunchUtilities.GetStringData(strServerDescription));
        bb.put((byte)(bDebugMode? 0xff : 0x00));
        bb.putInt(lStartingWealth);
        bb.putInt(lRespawnWealth);
        bb.putInt(lRespawnTime);
        bb.putInt(lRespawnProtectionTime);
        bb.putInt(lHourlyWealth);
        bb.putInt(lCMSSystemCost);
        bb.putInt(lSAMSystemCost);
        bb.putInt(lCMSStructureCost);
        bb.putInt(lNukeCMSStructureCost);
        bb.putInt(lSAMStructureCost);
        bb.putInt(lSentryGunStructureCost);
        bb.putInt(lOreMineStructureCost);
        bb.putFloat(fltRubbleMinValue);
        bb.putFloat(fltRubbleMaxValue);
        bb.putInt(lRubbleMinTime);
        bb.putInt(lRubbleMaxTime);
        bb.putFloat(fltStructureSeparation);
        bb.putShort(nPlayerBaseHP);
        bb.putShort(nStructureBaseHP);
        bb.putInt(lStructureBootTime);
        bb.put(cInitialMissileSlots);
        bb.put(cInitialInterceptorSlots);
        bb.putFloat(fltRequiredAccuracy);
        bb.putInt(lMinRadiationTime);
        bb.putInt(lMaxRadiationTime);
        bb.putInt(lMissileUpgradeBaseCost);
        bb.put(cMissileUpgradeCount);
        bb.putFloat(fltResaleValue);
        bb.putInt(lDecommissionTime);
        bb.putInt(lReloadTimeBase);
        bb.putInt(lReloadTimeStage1);
        bb.putInt(lReloadTimeStage2);
        bb.putInt(lReloadTimeStage3);
        bb.putInt(lReloadStage1Cost);
        bb.putInt(lReloadStage2Cost);
        bb.putInt(lReloadStage3Cost);
        bb.putFloat(fltRepairSalvageDistance);
        bb.putInt(lMissileSiteMaintenanceCost);
        bb.putInt(lSAMSiteMaintenanceCost);
        bb.putInt(lSentryGunMaintenanceCost);
        bb.putInt(lOreMineMaintenanceCost);
        bb.putInt(lHealthInterval);
        bb.putInt(lRadiationInterval);
        bb.putInt(lPlayerRepairCost);
        bb.putInt(lStructureRepairCost);
        bb.putLong(oAWOLTime);
        bb.putLong(oRemoveTime);
        bb.putInt(lAllianceCooloffTime);
        bb.putFloat(fltECMInterceptorChanceReduction);
        bb.putFloat(fltManualInterceptorChanceIncrease);
        bb.putInt(lSentryGunReloadTime);
        bb.putFloat(fltSentryGunRange);
        bb.putFloat(fltSentryGunHitChance);
        bb.putFloat(fltOreCollectRadius);
        bb.putInt(lOreMineGenerateTime);
        bb.putInt(lRadarStationMaintenanceCost);
        bb.putInt(lRadarStationStructureCost);
        bb.putFloat(fltRadarRangeBase);
        bb.putFloat(fltRadarRangeStage1);
        bb.putFloat(fltRadarRangeStage2);
        bb.putFloat(fltRadarRangeStage3);
        bb.putInt(lRadarRangeStage1Cost);
        bb.putInt(lRadarRangeStage2Cost);
        bb.putInt(lRadarRangeStage3Cost);
        bb.putInt(lABMSiloStructureCost);
        bb.putFloat(fltBaseRadarAccuracyBonus);
        bb.putFloat(fltRadarAccuracyBoostDistance);
        bb.putInt(lRadarBoostUpgradeCost);
        bb.putInt(lSentryGunRangeUpgradeCost);
        bb.putFloat(fltStealthPerTickChance);
        bb.putInt(lJihadistThreshold);
        bb.putFloat(fltOreGenerateRadius);
        bb.putInt(lOreMinExpiry);
        bb.putInt(lOreMaxExpiry);
        bb.putInt(lMaxOreValue);
        bb.putInt(lSolarPanelMaintenanceCost);
        bb.putInt(lSolarPanelStructureCost);
        bb.putShort(nSolarPanelBaseHP);
        bb.putInt(lSolarPanelGenerateTime);
        bb.putInt(lElecMinExpiry);
        bb.putInt(lElecMaxExpiry);
        bb.putInt(lElecMinValue);
        bb.putInt(lElecMaxValue);
        bb.putFloat(fltCommandPostShelterRadius);
        bb.putInt(lCommandPostHPUpgradeCost);
        bb.putShort(nCommandPostHPUpgradeHP);
        bb.putInt(lSiloBootUpgradeCost);
        bb.putInt(lSiloBootUpgradeTime);
        bb.putInt(lSiloMaxBootUpgrade);
        bb.putShort(nCommandPostMaxHPUpgrade);
        bb.putInt(lCommandPostStructureCost);
        bb.putShort(nCommandPostBaseHP);
        bb.putInt(lCommandPostMaintenanceCost);
        bb.putFloat(fltMaxAccuracy);
        bb.putInt(lFarmStructureCost);
        bb.putInt(lFarmMaintenanceCost);
        bb.putShort(nFarmBaseHP);
        bb.putInt(lCropMaxValue);
        bb.putInt(lCropMinExpiry);
        bb.putInt(lCropMaxExpiry);
        bb.putFloat(fltFarmGenerateRadius);
        bb.putInt(lFarmGrowthTime);
        bb.putInt(lMinCropCount);
        bb.putInt(lMaxCropCount);
        bb.putFloat(fltFarmWorkRadius);
        bb.putInt(lAirbaseStructureCost);
        bb.putInt(lAirbaseMaintenanceCost);
        bb.putInt(lAirbaseBaseCapacity);
        bb.putShort(nAirbaseBaseHP);
        bb.putInt(lBomberCost);
        bb.putInt(lFighterCost);
        bb.putFloat(fltAirspaceDistance);
        bb.putInt(lAircraftChaffCount);
        bb.putInt(lStrikeFighterCost);
        bb.putInt(lFastBomberCost);
        bb.putInt(lBankStructureCost);
        bb.putInt(lBankUpgradeCost);
        bb.putInt(lBankUpgradeCapacity);
        bb.putInt(lMaxBankCapacity);
        bb.putShort(nBankBaseHP);
        bb.putInt(lBankMaintenanceCost);
        bb.putFloat(fltBankInterestRate);
        bb.putFloat(fltLogDepotLinkRange);
        bb.putInt(lLogDepotMaintenanceCost);
        bb.putFloat(fltLogDepotCollectRadius);
        bb.putInt(lLogDepotStructureCost);
        bb.putShort(nLogDepotBaseHP);
        bb.putInt(lMaxLogDepotCapacity);
        bb.putInt(lMissileFactoryStructureCost);
        bb.putInt(lMissileFactoryCooldown);
        bb.putInt(lMissileFactoryMaintenanceCost);
        bb.putInt(lBlueprintCost);
        bb.putInt(lBlueprintExpiry);
        bb.putFloat(fltBlueprintBuildDistance);
        
        bb.putInt(MissileTypes.size());
        for(MissileType missileType : MissileTypes.values())
        {
            bb.put(missileType.GetData());
        }

        bb.putInt(InterceptorTypes.size());
        for(InterceptorType interceptorType : InterceptorTypes.values())
        {
            bb.put(interceptorType.GetData());
        }
        
        bb.putInt(TorpedoTypes.size());
        for(TorpedoType torpedoType : TorpedoTypes.values())
        {
            bb.put(torpedoType.GetData());
        }

        cData = bb.array();
        

        //Compute checksum.
        CRC32 crc32 = new CRC32();
        crc32.update(cData, 0, lSize);
        lChecksum = (int)crc32.getValue();


        //Compute nuclear escalation radius.
        float fltBiggestNukeRadius = 0.0f;

        for(MissileType type : MissileTypes.values())
        {
            if(type.GetNuclear())
            {
                fltBiggestNukeRadius = Math.max(fltBiggestNukeRadius, MissileStats.GetBlastRadius(type, true));
            }
        }

    }

    public void SetPort(int lPort) { this.lPort = lPort; }
    public int GetPort() { return lPort; }

    public String GetServerEmail() { return strServerEmailAddress; }
    public String GetServerName() { return strServerName; }
    public String GetServerDescription() { return strServerDescription; }

    public boolean DebugMode() { return bDebugMode; }
    
    public int GetCMSSystemCost() { return lCMSSystemCost; }
    public int GetSAMSystemCost() { return lSAMSystemCost; }
    public int GetCMSStructureCost() { return lCMSStructureCost; }
    public int GetNukeCMSStructureCost() { return lNukeCMSStructureCost; }
    public int GetSAMStructureCost() { return lSAMStructureCost; }
    public int GetSentryGunStructureCost() { return lSentryGunStructureCost; }
    public int GetOreMineStructureCost() { return lOreMineStructureCost; }
    public float GetRubbleMinValue() { return fltRubbleMinValue; }
    public float GetRubbleMaxValue() { return fltRubbleMaxValue; }
    public int GetRubbleMinTime() { return lRubbleMinTime; }
    public int GetRubbleMaxTime() { return lRubbleMaxTime; }
    public short GetPlayerBaseHP() { return nPlayerBaseHP; }
    public short GetStructureBaseHP() { return nStructureBaseHP; }
    public byte GetInitialMissileSlots() { return cInitialMissileSlots; }
    public byte GetInitialMissileSlotsNuclear() { return 1; }
    public byte GetInitialInterceptorSlots() { return cInitialInterceptorSlots; }
    public byte GetInitialABMSlots() { return 1; }
    public float GetRequiredAccuracy() { return fltRequiredAccuracy; }
    public int GetMinRadiationTime() { return lMinRadiationTime; }
    public int GetMaxRadiationTime() { return lMaxRadiationTime; }
    public int GetMissileUpgradeBaseCost() { return lMissileUpgradeBaseCost; }
    public byte GetMissileUpgradeCount() { return cMissileUpgradeCount; }
    public float GetResaleValue() { return fltResaleValue; }
    public int GetReloadTimeBase() { return lReloadTimeBase; }
    public int GetReloadTimeStage1() { return lReloadTimeStage1; }
    public int GetReloadTimeStage2() { return lReloadTimeStage2; }
    public int GetReloadTimeStage3() { return lReloadTimeStage3; }
    public int GetReloadStage1Cost() { return lReloadStage1Cost; }
    public int GetReloadStage2Cost() { return lReloadStage2Cost; }
    public int GetReloadStage3Cost() { return lReloadStage3Cost; }
    public float GetRepairSalvageDistance() { return fltRepairSalvageDistance; }
    public int GetHealthInterval() { return lHealthInterval; }
    public int GetRadiationInterval() { return lRadiationInterval; }
    public int GetPlayerRepairCost() { return lPlayerRepairCost; }
    public int GetStructureRepairCost() { return lStructureRepairCost; }
    public long GetAWOLTime() { return oAWOLTime; }
    public long GetRemoveTime() { return oRemoveTime; }
    public int GetAllianceCooloffTime() { return lAllianceCooloffTime; }
    public float GetECMInterceptorChanceReduction() { return fltECMInterceptorChanceReduction; }
    public float GetManualInterceptorChanceIncrease() { return fltManualInterceptorChanceIncrease; }
    public int GetSentryGunReloadTime() { return lSentryGunReloadTime; }
    public float GetSentryGunRange() { return fltSentryGunRange; }
    public float GetSentryGunHitChance() { return fltSentryGunHitChance; }
    public float GetOreCollectRadius() { return fltOreCollectRadius; }
    public int GetOreMineGenerateTime() { return bDebugMode ? 1800000 : lOreMineGenerateTime; }
    public int GetABMSiloStructureCost() { return lABMSiloStructureCost; }
    
    public int GetSentryRangeUpgradeCost() { return lSentryGunRangeUpgradeCost; }
    public float GetStealthPerTickChance() { return fltStealthPerTickChance; }
    public int GetJihadistThreshold() { return lJihadistThreshold; }
    
    public float GetBaseRadarAccuracyBonus() { return fltBaseRadarAccuracyBonus; }
    public float GetRadarAccuracyBoostDistance() { return fltRadarAccuracyBoostDistance; }
    public int GetRadarBoostUpgradeCost() { return lRadarBoostUpgradeCost; }
    public int GetRadarStationStructureCost() { return lRadarStationStructureCost; }
    public float GetRadarRangeBase() { return fltRadarRangeBase; }
    public float GetRadarRangeStage1() { return fltRadarRangeStage1; }
    public float GetRadarRangeStage2() { return fltRadarRangeStage2; }
    public float GetRadarRangeStage3() { return fltRadarRangeStage3; }
    public int GetRadarRangeStage1Cost() { return lRadarRangeStage1Cost; }
    public int GetRadarRangeStage2Cost() { return lRadarRangeStage2Cost; }
    public int GetRadarRangeStage3Cost() { return lRadarRangeStage3Cost; }
    
    public float GetOreGenerateRadius() { return fltOreGenerateRadius; }
    public int GetOreMinExpiry() { return lOreMinExpiry; }
    public int GetOreMaxExpiry() { return lOreMaxExpiry; }
    public int GetMaxOreValue() { return lMaxOreValue; }
    
    public int GetSolarPanelStructureCost() { return lSolarPanelStructureCost; }
    public short GetSolarPanelBaseHP() { return nSolarPanelBaseHP; }
    public int GetSolarPanelGenerateTime() { return bDebugMode ? 300000 : lSolarPanelGenerateTime; }
    public int GetElecMinExpiry() { return lElecMinExpiry; }
    public int GetElecMaxExpiry() { return lElecMaxExpiry; }
    public int GetElecMinValue() { return lElecMinValue; }
    public int GetElecMaxValue() { return lElecMaxValue; }
    
    public int GetSiloBootUpgradeCost() { return lSiloBootUpgradeCost; }
    public int GetSiloBootUpgradeTime() { return lSiloBootUpgradeTime; }
    public int GetSiloMaxBootUpgrade() { return lSiloMaxBootUpgrade; }
    
    public short GetCommandPostMaxHPUpgrade() { return nCommandPostMaxHPUpgrade; }
    public int GetCommandPostStructureCost() { return lCommandPostStructureCost; }
    public short GetCommandPostBaseHP() { return nCommandPostBaseHP; }
    public float GetCommandPostShelterRadius() { return fltCommandPostShelterRadius; }
    public int GetCommandPostHPUpgradeCost() { return lCommandPostHPUpgradeCost; }
    public short GetCommandPostHPUpgradeHP() { return nCommandPostHPUpgradeHP; }
    
    public float GetMaxWeaponSystemAccuracy() { return fltMaxAccuracy; }
    
    public int GetFarmStructureCost() { return lFarmStructureCost; }
    public short GetFarmBaseHP() { return nFarmBaseHP; }
    public int GetCropMaxValue() { return lCropMaxValue; }
    public int GetMinCropExpiry() { return lCropMinExpiry; }
    public int GetMaxCropExpiry() { return lCropMaxExpiry; }
    public float GetFarmGenerateRadius() { return fltFarmGenerateRadius; }
    public int GetFarmGrowthTime() { return bDebugMode ? 300000 : lFarmGrowthTime; }
    public int GetMinCropCount() { return lMinCropCount; }
    public int GetMaxCropCount() { return lMaxCropCount; }
    public float GetFarmWorkRadius() { return fltFarmWorkRadius; }
    
    public int GetAirbaseStructureCost() { return lAirbaseStructureCost; }
    public int GetAirbaseBaseCapacity() { return lAirbaseBaseCapacity; }
    public short GetAirbaseBaseHP() { return nAirbaseBaseHP; }
    public int GetBomberCost() { return lBomberCost; }
    public int GetFastBomberCost() { return lFastBomberCost; }
    public int GetFighterCost() { return lFighterCost; }
    public float GetAirspaceDistance() { return fltAirspaceDistance; }
    public int GetAircraftChaffCount() { return lAircraftChaffCount; }
    
    public int GetBankStructureCost() { return lBankStructureCost; }
    public short GetBankBaseHP() { return nBankBaseHP; }
    public int GetBankUpgradeCost() { return lBankUpgradeCost; }
    public int GetBankUpgradeCapacity() { return lBankUpgradeCapacity; }
    public int GetMaxBankCapacity() { return lMaxBankCapacity; }
    public float GetBankInterestRate() { return fltBankInterestRate; }
    
    public float GetWarehouseLinkRange() { return fltLogDepotLinkRange; }
    public float GetWarehouseCollectRadius() { return fltLogDepotCollectRadius; }
    public int GetWarehouseStructureCost() { return lLogDepotStructureCost; }
    public short GetWarehouseBaseHP() { return nLogDepotBaseHP; }
    public int GetMaxLogDepotCapacity() { return lMaxLogDepotCapacity; }
    
    public int GetMissileFactoryStructureCost() { return lMissileFactoryStructureCost; }
    public int GetMissileFactoryCooldown() { return lMissileFactoryCooldown; }
    public int GetBlueprintCost() { return lBlueprintCost; }
    public int GetBlueprintExpiry() { return lBlueprintExpiry; }
    public float GetBlueprintBuildDistance() { return fltBlueprintBuildDistance; }

    public Collection<MissileType> GetMissileTypes() { return MissileTypes.values(); }
    public Collection<InterceptorType> GetInterceptorTypes() { return InterceptorTypes.values(); }
    public Collection<TorpedoType> GetTorpedoTypes() { return TorpedoTypes.values(); }

    public MissileType GetMissileType(int lID) { return MissileTypes.get(lID); }
    public InterceptorType GetInterceptorType(int lID) { return InterceptorTypes.get(lID); }
    public TorpedoType GetTorpedoType(int lID) { return TorpedoTypes.get(lID); }
    
    public int GetStructureBootTime(Player player) 
    { 
        return !bDebugMode && player != null && !player.GetBoss() ? lStructureBootTime : 3000 ; 
    }
    
    public float GetStructureSeparation(EntityType type) 
    { 
        return fltStructureSeparation;
    }
    
    public int GetStartingWealth()
    { 
        return bDebugMode ? 100000000 : lStartingWealth; 
    }
    
    public int GetRespawnWealth() 
    { 
        return bDebugMode ? 50000000 : lRespawnWealth; 
    }
    
    public int GetRespawnTime() 
    { 
        return bDebugMode ? 3000 : lRespawnTime; 
    }
    
    public int GetRespawnProtectionTime() 
    { 
        return bDebugMode ? 300000 : lRespawnProtectionTime; 
    }
    
    public int GetNoobProtectionTime() 
    { 
        return bDebugMode ? 300000 : lRespawnProtectionTime; 
    }
    
    public int GetHourlyWealth() 
    { 
        return bDebugMode ? 100000000 : lHourlyWealth; 
    }
    
    public int GetDecommissionTime(Structure structure) 
    { 
        if(structure.Captured())
        {
            return bDebugMode ? 3000 : Defs.CAPTURED_STRUCTURE_DECOM_TIME; 
        }
        else
        {
            return bDebugMode ? 3000 : lDecommissionTime; 
        }
    }

    public int GetSize() { return lSize; }
    public byte[] GetData() { return cData; }
    public int GetChecksum() { return lChecksum; }

    public float GetLongestInterceptorRange(boolean bConsiderABM)
    {
        float furthestRange = 0.0f;
        
        for(InterceptorType type : GetInterceptorTypes())
        {
            if(!type.GetABM() || (type.GetABM() && bConsiderABM))
            {
                if(type.GetInterceptorRange() > furthestRange)
                    furthestRange = type.GetInterceptorRange();
            }
        }
        
        return furthestRange;
    }
    
    public float GetLongestInterceptorRange(MissileSystem missileSystem)
    {
        float lLongestRange = 0;
        
        for(int lType : missileSystem.GetSlotTypes().values())
        {
            if(GetInterceptorType(lType) != null)
            {
                InterceptorType type = GetInterceptorType(lType);
                
                //if(!type.GetABM() || (type.GetABM() && considerABM))
                if(type.GetInterceptorRange() > lLongestRange)
                    lLongestRange = type.GetInterceptorRange();
            }
        }
        
        return lLongestRange;
    }
    
    public float GetLongestMissileRange(MissileSystem missileSystem, boolean bConsiderAntiSub)
    {
        float fltLongestRange = 0;
        
        for(int lType : missileSystem.GetSlotTypes().values())
        {
            if(GetMissileType(lType) != null)
            {
                MissileType type = GetMissileType(lType);
                
                if((bConsiderAntiSub || !type.GetAntiSubmarine()) && type.GetMissileRange() > fltLongestRange)
                    fltLongestRange = type.GetMissileRange();
            }
        }
        
        return fltLongestRange;
    }
    
    public MissileType GetLargestBlastRadiusMissile(MissileSystem missileSystem)
    {
        MissileType largestBlastType = null;
        
        for(int lType : missileSystem.GetSlotTypes().values())
        {
            if(GetMissileType(lType) != null)
            {
                MissileType type = GetMissileType(lType);
                
                if(largestBlastType == null || MissileStats.GetBlastRadius(type, false) > MissileStats.GetBlastRadius(largestBlastType, false))
                    largestBlastType = type;
            }
        }
        
        return largestBlastType;
    }
    
    public float GetLongestTorpedoRange(MissileSystem missileSystem)
    {
        float lLongestRange = 0;
        
        for(int lType : missileSystem.GetSlotTypes().values())
        {
            if(GetTorpedoType(lType) != null)
            {
                TorpedoType type = GetTorpedoType(lType);
                
                //if(!type.GetABM() || (type.GetABM() && considerABM))
                if(type.GetTorpedoRange() > lLongestRange)
                    lLongestRange = type.GetTorpedoRange();
            }
        }
        
        return lLongestRange;
    }
    
    public float GetHitChance(InterceptorType type) 
    {
        return Defs.INTERCEPTOR_ACCURACY;    
    }

    public int GetMissilePrepTime(MissileType type, Player player)
    {
        if(!bDebugMode && player != null && !player.GetBoss())
        {
            if(type != null)
            {
                return MissileStats.GetMissileBuildTime(type, false, false); 
            }
        }
        else
        {
            return 0;
        }
        
        return 0; 
    }

    public int GetInterceptorPrepTime(InterceptorType type)
    {
        if(!bDebugMode)
        {
            if(type != null)
            {
                return MissileStats.GetInterceptorBuildTime(type, false, false); 
            }
        }
        else
        {
            return 0;
        }
        
        return 0;
    }
    
    public float GetCommandPostSpacing() 
    { 
        //Must be * 2 because otherwise the shelter radii will overlap and cause weirdness. 
        return fltCommandPostShelterRadius * 2; 
    }

    public int GetMaintenanceCost(Structure structure)
    {
        if(structure instanceof OreMine)
            return Defs.EXTRACTOR_MAINTENANCE_COST;
        else
            return Defs.ONLINE_MAINTENANCE_COST;
    }

    public float GetMissileSpeed(MissileType type)
    {
        if(type != null)
        {
            return type.GetMissileSpeed();
        }
        
        return 0;
    }

    public float GetMissileRange(MissileType type)
    {
        if(type != null)
        {
            return type.GetMissileRange();
        }
        
        return 0;
    }

    public short GetMissileMaxDamage(MissileType type)
    {
        if(type != null)
        {
            return MissileStats.GetMaxDamage(type);
        }
        
        return 0;
    }
    
    public short GetTorpedoMaxDamage(TorpedoType type)
    {
        if(type != null)
        {
            return MissileStats.GetMaxDamage(type);
        }
        
        return 0;
    }

    public float GetInterceptorSpeed(InterceptorType type)
    {
        if(type != null)
        {
            return type.GetInterceptorSpeed();
        }
        
        return 0;
    }

    public float GetInterceptorRange(InterceptorType type)
    {
        if(type != null)
        {
            return type.GetInterceptorRange();
        }
        
        return 0;
    }
    
    public float GetMaxRadarAccuracyBoost()
    {
        return GetBaseRadarAccuracyBonus() * 10;
    }
    
    public float GetMaxSentryRange()
    {
        return GetSentryGunRange() * 5;
    }
    
    public int GetHourlyWealth(Player player) 
    { 
        if(!bDebugMode)
            return lHourlyWealth + (player.GetRank() * 50);
        else
            return 1000000;
    }
    
    public List<LaunchBannedApp> GetMinorCheatingApps() { return MinorCheatingApps; }
    public List<LaunchBannedApp> GetMajorCheatingApps() { return MajorCheatingApps; }
    
    public byte GetMaxOreMineLevel()
    {
        return 3;
    }
    
    public Map<ResourceType, Long> GetStructureBuildCost(Structure structure)
    {
        //TODO: Move this to StructureStats, since I guess I'm keeping that stupid class. -Corbin
        if(structure instanceof SAMSite)
        {
            SAMSite samSite = ((SAMSite)structure);
            
            if(samSite.GetIsABMSilo())
            {
                return Defs.ABM_SILO_STRUCTURE_COST;
            }
            else
            {
                return Defs.SAM_SITE_STRUCTURE_COST;
            }
        }
        else if(structure instanceof MissileSite)
        {
            MissileSite missileSite = ((MissileSite)structure);
            
            if(missileSite.CanTakeICBM())
            {
                return Defs.ICBM_SILO_STRUCTURE_COST;
            }
            else
            {
                return Defs.MISSILE_SITE_STRUCTURE_COST;
            }
        }
        else if(structure instanceof SentryGun gun)
        {
            if(gun.GetIsWatchTower())
                return Defs.WATCH_TOWER_STRUCTURE_COST;
            
            return Defs.SENTRY_GUN_STRUCTURE_COST;
        }
        else if(structure instanceof RadarStation)
            return Defs.RADAR_STATION_STRUCTURE_COST;
        else if(structure instanceof OreMine)
            return Defs.ORE_MINE_STRUCTURE_COST;
        else if(structure instanceof ArtilleryGun)
            return Defs.ARTILLERY_GUN_STRUCTURE_COST;
        else if(structure instanceof CommandPost)
            return Defs.COMMAND_POST_STRUCTURE_COST;
        else if(structure instanceof Airbase)
            return Defs.AIRBASE_STRUCTURE_COST;
        else if(structure instanceof Armory)
            return Defs.BARRACKS_STRUCTURE_COST;
        else if(structure instanceof Warehouse)
            return Defs.WAREHOUSE_STRUCTURE_COST;
        else if(structure instanceof ScrapYard)
            return Defs.SCRAPYARD_STRUCTURE_COST;
        else if(structure instanceof Distributor)
            return Defs.DISTRIBUTOR_STRUCTURE_COST;
        else if(structure instanceof Processor)
        {
            Processor processor = (Processor)structure;
            
            return Defs.GetProcessorCost(processor.GetType());
        }
        
        return null;
    }
    
    public Map<ResourceType, Long> GetStructureBuildCost(EntityType structureType, ResourceType resourceType)
    {
        switch(structureType)
        {
            case SAM_SITE: return Defs.SAM_SITE_STRUCTURE_COST;
            case ABM_SILO: return Defs.ABM_SILO_STRUCTURE_COST;
            case MISSILE_SITE: return Defs.MISSILE_SITE_STRUCTURE_COST;
            case NUCLEAR_MISSILE_SITE: return Defs.ICBM_SILO_STRUCTURE_COST;
            case SENTRY_GUN: return Defs.SENTRY_GUN_STRUCTURE_COST;
            case RADAR_STATION: return Defs.RADAR_STATION_STRUCTURE_COST;
            case ORE_MINE: return Defs.ORE_MINE_STRUCTURE_COST;
            case ARTILLERY_GUN: return Defs.ARTILLERY_GUN_STRUCTURE_COST;
            case COMMAND_POST: return Defs.COMMAND_POST_STRUCTURE_COST;
            case AIRBASE: return Defs.AIRBASE_STRUCTURE_COST;
            case ARMORY: return Defs.ARMORY_STRUCTURE_COST;
            case BARRACKS: return Defs.BARRACKS_STRUCTURE_COST;
            case WAREHOUSE: return Defs.WAREHOUSE_STRUCTURE_COST;
            case SCRAP_YARD: return Defs.SCRAPYARD_STRUCTURE_COST;
            case DISTRIBUTOR: return Defs.DISTRIBUTOR_STRUCTURE_COST;
            case WATCH_TOWER: return Defs.WATCH_TOWER_STRUCTURE_COST;
            case PROCESSOR: return Defs.GetProcessorCost(resourceType);
        }
        
        return null;
    }
    
    public int GetMaxAirbaseCapacity() 
    { 
        return Defs.MAX_AIRBASE_CAPACITY; 
    }
    
    public int GetAircraftSlotUpgradeCostBase() 
    { 
        return 1000; 
    }
    
    public int GetAircraftSlotUpgradeCount()
    {
        return Defs.AIRBASE_CAPACITY_UPGRADE_AMOUNT;
    }
    
    public float GetFlareHitChanceReduction()
    {
        return 0.55f;
    }
    
    public float GetFighterCannonRange()
    {
        return 0.5f;
    }
    
    public float GetFighterCannonAccuracy()
    {
        return 0.3f;
    }
    
    public int GetAircraftReloadTime()
    {
        //All aircraft have the same reload time.
        return 3000;
    }
    
    public float GetInterceptorNukeKillChance()
    {
        return Defs.INTERCEPTOR_NUKE_KILL_CHANCE;
    }
}
