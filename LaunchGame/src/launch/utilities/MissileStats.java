/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.utilities;

import java.util.ArrayList;
import java.util.List;
import launch.game.Defs;
import launch.game.Explosion;
import launch.game.GeoCoord;
import launch.game.entities.CommandPost;
import launch.game.entities.LandUnit;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.NavalVessel;
import launch.game.entities.SAMSite;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;

/**
 *
 * @author conta
 */
public class MissileStats 
{
    /*
     * All the stats in this class pertaining to blast radius size and air pressure (PSI) have been pulled from https://nuclearsecrecy.com/nukemap/.
     * Since Counterforce is supposed to be a semi-realistic combat simulator, it would be ideal if these values were not changed.
     */
    public static final float PSI_9000_MULT_GROUND = 0.019f;
    public static final float PSI_6000_MULT_GROUND = 0.022f;
    public static final float PSI_3000_MULT_GROUND = 0.028f;
    public static final float PSI_200_MULT_GROUND = 0.068f;
    public static final float PSI_20_MULT_GROUND = 0.185f;
    public static final float PSI_5_MULT_GROUND = 0.389f;
    
    public static final short MAX_DMG_9000PSI = (short)32000;
    public static final short MAX_DMG_6000PSI = (short)16000;
    public static final short MAX_DMG_3000PSI = (short)12000;
    public static final short MAX_DMG_200PSI = (short)9000;
    public static final short MAX_DMG_20PSI = (short)6400;
    public static final short MAX_DMG_5PSI = (short)3200;
    public static final short MAX_DMG_1PSI = (short)800;
    
    public static final int YIELD_THRESHOLD_9000PSI = 100;
    public static final int YIELD_THRESHOLD_6000PSI = 60;
    public static final int YIELD_THRESHOLD_3000PSI = 30;
    public static final int YIELD_THRESHOLD_200PSI = 2;
    public static final int YIELD_THRESHOLD_20PSI = 1;
    
    public static final short CONV_900_DMG_YIELD = 3000;
    public static final short CONV_750_DMG_YIELD = 1500;
    public static final short CONV_500_DMG_YIELD = 500;
    public static final short CONV_400_DMG_YIELD = 300;
    public static final short CONV_300_DMG_YIELD = 200;
    public static final short CONV_200_DMG_YIELD = 100;
    public static final short CONV_BASE_DMG = 100;
    
    public static final float CONV_MAX_DMG_RADIUS = 0.5f;
    public static final short MAX_TORPEDO_DAMAGE = 1000;
    public static final short MIN_NUKE_DAMAGE = 75;
    
    public static final short INCENDIARY_MAX_DMG = 250;
    
    public static final double KPH_PER_MACH = 1234.8;
    public static final short DMG_PER_MACH = 15;
    public static final int MIN_MISSILE_PRICE = 100;
    
    public static final int MIN_TORPEDO_PRICE = 5000;
    public static final int TORPEDO_COST_PER_WEIGHT = 270;
    
    public static final float EMP_KM_PER_WEIGHT = 30;
    public static final float KT_PER_WEIGHT = 75;
    public static final int COST_PER_WEIGHT = 40;
    public static final int NUCLEAR_WARHEAD_BASE_WEIGHT = 15;
    public static final float ICBM_COST_WEIGHT_MULTIPLIER = 10;
    public static final float ABM_COST_WEIGHT_MULTIPLIER = 15;
    public static final float MIRV_COST_MULTIPLIER = 0.05f;
    public static final float AIR_LAUNCHED_INT_MULTI = 0.6f;
    public static final float SLBM_MULTIPLIER = 1.0f;
    public static final int INCENDIARY_WEIGHT = 60;
    
    public static final float ICBM_COST_MULTIPLIER = 10.0f;
    public static final float ABM_COST_MULTIPLIER = 4.3f;
    public static final float INTERCEPTOR_COST_MULTIPLIER = 1.1f;
    public static final float ICBM_PREP_WEIGHT_MULTIPLIER = 3.0f;
    public static final int TORPEDO_PREP_PER_WEIGHT = Defs.MS_PER_SEC * 60;
    public static final int MISSILE_PREP_PER_WEIGHT = Defs.MS_PER_SEC * 24;
    public static final int SAM_PREP_PER_WEIGHT = Defs.MS_PER_SEC * 38;
    public static final int ICBM_PREP_PER_WEIGHT = Defs.MS_PER_SEC * 38;
    public static final int MIN_MISSILE_PREP = Defs.MS_PER_MIN * 5;
    public static final int NUCLEAR_PREP_BASE_WEIGHT = 50;
    public static final int ICBM_MIN_PREP = Defs.MS_PER_DAY * 2;
    
    public static final float AIRBURST_BLASTRAD_MULTIPLIER = 1.681f;
    public static float PSI_5_MULT_AIR = 0.355f;
    public static final float FUEL_TO_EXP_MULT = 0.15f;
    
    public static final float COMMAND_POST_BUSTER_RADIUS_MULT = 0.1f;
    public static final float MAX_MIRV_SPREAD_KM = 1500;
    public static final float KM_FOR_MAX_MIRV_SPREAD = 7000;
    
    public static short GetDamageAtPosition(MapEntity target, GeoCoord geoImpact, Explosion explosion)
    {
        boolean bAirburst = explosion.GetAirburst();
        boolean bNuclear = explosion.GetNuclear();
        float fltBlastRadius = GetBlastRadius(explosion);
        float fltDistance = geoImpact.DistanceTo(target.GetPosition().GetCopy());
        float fltYield = explosion.GetYield();
        
        if(bAirburst && bNuclear)
        {
            float PSI_5_RANGE = (fltBlastRadius * AIRBURST_BLASTRAD_MULTIPLIER) * PSI_5_MULT_GROUND;

            if(fltDistance <= PSI_5_RANGE)
                return (short)LaunchUtilities.GetRandomIntInBounds((short)(MAX_DMG_5PSI * 0.5f), MAX_DMG_5PSI);
            else
                return (short)LaunchUtilities.GetRandomIntInBounds(1, MAX_DMG_1PSI);
        }
        else if(explosion.GetNuclear())
        {
            float PSI_9000_RANGE = fltBlastRadius * PSI_9000_MULT_GROUND;
            float PSI_6000_RANGE = fltBlastRadius * PSI_6000_MULT_GROUND;
            float PSI_3000_RANGE = fltBlastRadius * PSI_3000_MULT_GROUND;
            float PSI_200_RANGE = fltBlastRadius * PSI_200_MULT_GROUND;
            float PSI_20_RANGE = fltBlastRadius * PSI_20_MULT_GROUND;
            float PSI_5_RANGE = fltBlastRadius * PSI_5_MULT_GROUND;

            if(fltYield >= YIELD_THRESHOLD_9000PSI && fltDistance <= PSI_9000_RANGE)
                return (short)MAX_DMG_9000PSI;//LaunchUtilities.GetRandomIntInBounds((int)(MAX_DMG_6000PSI * 1.5f), MAX_DMG_9000PSI);
            else if(fltYield >= YIELD_THRESHOLD_6000PSI && fltDistance <= PSI_6000_RANGE)
                return (short)MAX_DMG_6000PSI;//LaunchUtilities.GetRandomIntInBounds((int)(MAX_DMG_3000PSI * 1.5f), MAX_DMG_6000PSI);
            else if(fltYield >= YIELD_THRESHOLD_3000PSI && fltDistance <= PSI_3000_RANGE)
                return (short)MAX_DMG_3000PSI;//LaunchUtilities.GetRandomIntInBounds(MAX_DMG_200PSI, MAX_DMG_3000PSI);
            else if(fltYield >= YIELD_THRESHOLD_200PSI && fltDistance <= PSI_200_RANGE)
                return (short)LaunchUtilities.GetRandomIntInBounds((short)(MAX_DMG_200PSI * 0.8f), MAX_DMG_200PSI);
            else if(fltYield >= YIELD_THRESHOLD_20PSI && fltDistance <= PSI_20_RANGE)
                return (short)LaunchUtilities.GetRandomIntInBounds((short)(MAX_DMG_20PSI * 0.7f), MAX_DMG_20PSI);
            else if(fltDistance <= PSI_5_RANGE)
                return (short)LaunchUtilities.GetRandomIntInBounds((short)(MAX_DMG_5PSI * 0.5f), MAX_DMG_5PSI);
            else
                return (short)LaunchUtilities.GetRandomIntInBounds(1, MAX_DMG_1PSI);
        }
        else
        {
            short nDamage = 0;
            
            if(explosion.GetYield() >= CONV_900_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(750, 900);
            else if(explosion.GetYield() >= CONV_750_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(500, 750);
            else if(explosion.GetYield() >= CONV_500_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(400, 500);
            else if(explosion.GetYield() >= CONV_400_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(300, 400);
            else if(explosion.GetYield() >= CONV_300_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(200, 300);
            else if(explosion.GetYield() >= CONV_200_DMG_YIELD)
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(100, 200);
            else
                nDamage = (short)LaunchUtilities.GetRandomIntInBounds(25, 100);
            
            if(explosion.GetBunkerBuster())
            {
                if(target instanceof NavalVessel || target instanceof CommandPost)
                    nDamage *= Defs.COMMAND_POST_BUSTER_DMG_MULT;
                else if(target instanceof MissileSite)
                {
                    if(((MissileSite)target).CanTakeICBM())
                        nDamage *= Defs.COMMAND_POST_BUSTER_DMG_MULT;
                }
                else if(target instanceof SAMSite)
                {
                    if(((SAMSite)target).GetIsABMSilo())
                        nDamage *= Defs.COMMAND_POST_BUSTER_DMG_MULT;
                }
            }
            else if(explosion.GetArtillery() && !explosion.GetNuclear() && target instanceof NavalVessel)
            {
                nDamage *= Defs.ARTILLERY_TO_SHIP_DMG_MULT;
            }
            else if(explosion.GetArtillery())
            {
                nDamage *= Defs.ARTILLERY_GENERAL_DMG_MULTIPLIER;
            }
            else if(explosion.GetAntiShip() && target instanceof NavalVessel)
            {
                nDamage *= Defs.ANTI_SHIP_DMG_MULT;
            }
            
            float fltDistanceProportion = fltDistance/fltBlastRadius;
            short nSemiFinalDamage = (short)(nDamage - (nDamage * fltDistanceProportion));
            
            return (short)LaunchUtilities.GetRandomIntInBounds((int)(nSemiFinalDamage * 0.8f), nSemiFinalDamage);
        }
    }
    
    //For torpedoes.
    public static short GetDamageAtPosition(GeoCoord geoPosition, GeoCoord geoImpact, TorpedoType type)
    {
        return (short)LaunchUtilities.GetRandomIntInBounds((int)(0.25f * MAX_TORPEDO_DAMAGE), MAX_TORPEDO_DAMAGE);
    }
    
    public static short GetMaxDamage(MissileType type)
    {
        float fltYield = type.GetYield();
        
        if(type.GetNuclear())
        {
            if(fltYield >= YIELD_THRESHOLD_9000PSI)
                return MAX_DMG_9000PSI;
            else if(fltYield >= YIELD_THRESHOLD_6000PSI)
                return MAX_DMG_6000PSI;
            else if(fltYield >= YIELD_THRESHOLD_3000PSI)
                return MAX_DMG_3000PSI;
            else if(fltYield >= YIELD_THRESHOLD_200PSI)
                return MAX_DMG_200PSI;
            else if(fltYield >= YIELD_THRESHOLD_20PSI)
                return MAX_DMG_20PSI;
            else
                return MAX_DMG_5PSI;
        }
        else
        {
            if(type.GetYield() >= CONV_900_DMG_YIELD)
                return 900;
            else if(type.GetYield() >= CONV_750_DMG_YIELD)
                return 750;
            if(type.GetYield() >= CONV_500_DMG_YIELD)
                return 500;
            else if(type.GetYield() >= CONV_400_DMG_YIELD)
                return 400;
            else if(type.GetYield() >= CONV_300_DMG_YIELD)
                return 300;
            else if(type.GetYield() >= CONV_200_DMG_YIELD)
                return 200;
            else
                return CONV_BASE_DMG;
        }
    }
    
    public static short GetMaxDamage(TorpedoType type)
    {
        return MAX_TORPEDO_DAMAGE;
    }
    
    public static float GetBlastRadius(MissileType type, boolean bAirburst)
    {
        long oYieldInKG = type.GetYield();
        int lBaseYieldInKG = 1;
        float fltBaseBlastRadiusInKM = 0.0118f;
        
        //Nuclear wareheads have yields measured in Kt, not kg, so do the conversion first.
        if(type.GetNuclear())
            oYieldInKG *= Defs.KG_PER_TON * Defs.TONS_PER_KT;
        else if(!type.GetBunkerBuster())
            fltBaseBlastRadiusInKM *= 3;
        
        float fltBlastRadius = (float)(Math.cbrt(oYieldInKG/lBaseYieldInKG) * fltBaseBlastRadiusInKM);
        
        if(bAirburst)
        {
            fltBlastRadius *= AIRBURST_BLASTRAD_MULTIPLIER;
        }
        else if(type.GetBunkerBuster())
        {
            fltBlastRadius *= COMMAND_POST_BUSTER_RADIUS_MULT;
        }
        
        return fltBlastRadius;
    }
    
    public static float GetBlastRadius(Explosion explosion)
    {
        long oYieldInKG = explosion.GetYield();
        int lBaseYieldInKG = 1;
        float fltBaseBlastRadiusInKM = 0.0118f;
        
        //Nuclear wareheads have yields measured in Kt, not kg, so do the conversion first.
        if(explosion.GetNuclear())
            oYieldInKG *= Defs.KG_PER_TON * Defs.TONS_PER_KT;
        else if(!explosion.GetBunkerBuster())
            fltBaseBlastRadiusInKM *= 3;
        
        float fltBlastRadius = (float)(Math.cbrt(oYieldInKG/lBaseYieldInKG) * fltBaseBlastRadiusInKM);
        
        if(explosion.GetAirburst())
        {
            fltBlastRadius *= AIRBURST_BLASTRAD_MULTIPLIER;
        }
        else if(explosion.GetBunkerBuster())
        {
            fltBlastRadius *= COMMAND_POST_BUSTER_RADIUS_MULT;
        }
        
        return fltBlastRadius;
    }
    
    public static List<Float> GetBlastRadii(MissileType type, boolean bAirburst)
    {
        int lYield = type.GetYield();
        List<Float> Radii = new ArrayList<>();
        float fltBlastRadius = GetBlastRadius(type, bAirburst);
        
        if(bAirburst)
        {
            Radii.add(fltBlastRadius);
            Radii.add(fltBlastRadius * PSI_5_MULT_AIR);
        }
        else
        {
            if(type.GetNuclear())
            {
                if(lYield >= YIELD_THRESHOLD_9000PSI)
                    Radii.add(fltBlastRadius * PSI_9000_MULT_GROUND);
                if(lYield >= YIELD_THRESHOLD_6000PSI)
                    Radii.add(fltBlastRadius * PSI_6000_MULT_GROUND);
                if(lYield >= YIELD_THRESHOLD_3000PSI)
                    Radii.add(fltBlastRadius * PSI_3000_MULT_GROUND);
                if(lYield >= YIELD_THRESHOLD_200PSI)
                    Radii.add(fltBlastRadius * PSI_200_MULT_GROUND);
                if(lYield >= YIELD_THRESHOLD_20PSI)
                    Radii.add(fltBlastRadius * PSI_20_MULT_GROUND);
            }
                
            Radii.add(fltBlastRadius * PSI_5_MULT_GROUND);
            Radii.add(fltBlastRadius);
        }
        
        return Radii;
    }
    
    public static int GetMissileBuildTime(MissileType type, boolean bHostIsUnderWay, boolean bPlayerisBoss)
    {
        int lBasePrepTime = MISSILE_PREP_PER_WEIGHT;
        
        if(bPlayerisBoss)
            return 0;
        
        float fltLaunchVehicleWeight = 0;
        
        fltLaunchVehicleWeight += (type.GetMissileSpeed()/KPH_PER_MACH) * 3;
        fltLaunchVehicleWeight += type.GetMissileRange()/700;
        
        if(type.GetECM())
            fltLaunchVehicleWeight += 12;
                
        if(type.GetStealth())
            fltLaunchVehicleWeight += 25;
        
        if(type.GetICBM())
        {
            fltLaunchVehicleWeight *= ICBM_PREP_WEIGHT_MULTIPLIER;
            lBasePrepTime = ICBM_PREP_PER_WEIGHT;
        }
        
        float accuracyModifier = type.GetAccuracy() * 5; //((1f - type.GetCEP()) < 0.6f ? 0.6f : (1f - type.GetCEP())) > 1f ? 1f : ((1f - type.GetCEP()) < 0.6f ? 0.6f : (1f - type.GetCEP()));
        fltLaunchVehicleWeight -= accuracyModifier;
        
        if(type.GetICBM() && fltLaunchVehicleWeight * lBasePrepTime < ICBM_MIN_PREP)
            fltLaunchVehicleWeight = ICBM_MIN_PREP/lBasePrepTime;
                    
        float fltWarheadWeight = 0;
        
        if(type.GetNuclear())
            fltWarheadWeight += NUCLEAR_PREP_BASE_WEIGHT + (type.GetYield()/1000);
        else
            fltWarheadWeight += type.GetYield()/100;
        
        fltWarheadWeight += type.GetEMPRadius()/50;
        int lPrep = ((int)fltLaunchVehicleWeight + (int)fltWarheadWeight) * lBasePrepTime < MIN_MISSILE_PREP ? MIN_MISSILE_PREP : ((int)fltLaunchVehicleWeight + (int)fltWarheadWeight) * lBasePrepTime;
        
        return lPrep;
    }
    
    public static int GetTorpedoBuildTime(TorpedoType type, boolean bHostIsUnderWay, boolean bPlayerisBoss)
    {
        if(bPlayerisBoss)
            return 0;
        
        float fltLaunchVehicleWeight = 0;
        
        if(type.GetHoming())
            fltLaunchVehicleWeight += 25;
                    
        float fltWarheadWeight = 0;
        
        if(type.GetNuclear())
            fltWarheadWeight += NUCLEAR_PREP_BASE_WEIGHT + (type.GetYield()/1000);
        else
            fltWarheadWeight += type.GetYield()/100;
        
        int lPrep = ((int)fltLaunchVehicleWeight + (int)fltWarheadWeight) * TORPEDO_PREP_PER_WEIGHT < MIN_MISSILE_PREP ? MIN_MISSILE_PREP : ((int)fltLaunchVehicleWeight + (int)fltWarheadWeight) * TORPEDO_PREP_PER_WEIGHT;
        
        return lPrep;
    }
    
    public static int GetInterceptorBuildTime(InterceptorType type, boolean bHostIsUnderWay, boolean bPlayerisBoss)
    {
        if(bPlayerisBoss)
            return 0;
        
        float fltWeight = 0;
        
        fltWeight += type.GetInterceptorSpeed()/KPH_PER_MACH * 1.55;
        fltWeight += type.GetInterceptorRange()/450;
        
        if(type.GetAirLaunched())
            fltWeight *= AIR_LAUNCHED_INT_MULTI;
        
        if(type.GetNuclear())
            fltWeight += NUCLEAR_PREP_BASE_WEIGHT + (type.GetYield()/1000);
        
        if(type.GetABM())
            fltWeight *= (ICBM_PREP_WEIGHT_MULTIPLIER * 0.55f);
        
        int lPrep = (int)((fltWeight * SAM_PREP_PER_WEIGHT) * 2.5f);
        
        return Math.max(lPrep, Defs.MS_PER_MIN * 9);
    }
    
    public static short GetDamageToLandUnit(LandUnit unit, GeoCoord geoImpact, Explosion explosion)
    {
        if(explosion.GetAirburst())
        {
            return (short)(GetDamageAtPosition(unit, geoImpact, explosion)/7);
        }
        else
        {
            if(!explosion.GetArtillery())
            {
                if((unit.GetStationary()))
                {
                    return (short)((GetDamageAtPosition(unit, geoImpact, explosion)/3)/2);
                }
                else
                {
                    return (short)(GetDamageAtPosition(unit, geoImpact, explosion)/3);
                }
            }
            else
            {
                return (short)(GetDamageAtPosition(unit, geoImpact, explosion)/2);
            }
        }     
    }
    
    public static float GetMissileEMPRadius(MissileType type, boolean bAirburst)
    {
        if(type.GetNuclear() && bAirburst)
        {
            return type.GetEMPRadius();
        }
        
        return 0.0f;
    }
}
