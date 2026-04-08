/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.utilities;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchGame;
import launch.game.entities.*;

/**
 *
 * @author conta
 */
public class StructureStats 
{
    public static final short HP_PER_MISSILESLOT = 25;
    public static final short HARDNESS_MULTIPLIER = 3;
    public static final short BASE_STRUCTURE_HP = 50;
    public static final float METERS_PER_MULTIPLIER = 100;
    
    public static final int CIVILIAN_STRUCTURE_HARDNESS = 2;
    public static final int MILITARY_STRUCTURE_HARDNESS = 5;
    public static final int HARDENED_STRUCTURE_HARDNESS = 10;
            
    public static short GetMaxHPByStructure(Structure structure)
    {
        short semiFinalHP = GetMaxHPByType(structure.GetEntityType(), null);
        
        if(structure instanceof MissileSite && !((MissileSite)structure).CanTakeICBM())
            semiFinalHP += ((MissileSite)structure).GetMissileSystem().GetSlotCount() * HP_PER_MISSILESLOT;
        else if(structure instanceof SAMSite && !((SAMSite)structure).GetIsABMSilo())
            semiFinalHP += ((SAMSite)structure).GetInterceptorSystem().GetSlotCount() * HP_PER_MISSILESLOT;
        
        return (short)((Math.ceil(semiFinalHP/10.0))*10);
    }
    
    public static short GetMaxHPByType(EntityType type, LaunchGame game)
    {
        switch(type)
        {
            case NUCLEAR_MISSILE_SITE: return Defs.ICBM_SILO_HP;
            case COMMAND_POST: return Defs.COMMAND_POST_HP;
            case ABM_SILO: return Defs.ABM_SILO_HP;
            case BANK: return Defs.BANK_HP;
            case SAM_SITE: return Defs.SAM_SITE_HP;
            case AIRBASE: return Defs.AIRBASE_HP;
            case MISSILE_SITE: return Defs.MISSILE_SITE_HP;
            case SENTRY_GUN: return Defs.SENTRY_GUN_HP;
            case RADAR_STATION: return Defs.RADAR_STATION_HP;
            case ORE_MINE: return Defs.ORE_MINE_HP;
            case MISSILE_FACTORY: return Defs.MISSILE_FACTORY_HP;
            case WAREHOUSE: return Defs.WAREHOUSE_HP;
            case PROCESSOR: return Defs.PROCESSOR_MAX_HP;
            case DISTRIBUTOR: return Defs.DISTRIBUTOR_MAX_HP;
            default: return 69;
        }
    }
}
