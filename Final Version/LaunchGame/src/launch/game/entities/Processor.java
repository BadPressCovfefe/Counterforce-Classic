/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;

import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.ResourceSystem;

/**
 *
 * @author tobster
 */
public class Processor extends Structure
{    
    private static final int DATA_SIZE = 1;
    
    private ResourceType type;
    
    /** New. */
    public Processor(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceType type, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.type = type;
    }
    
    /** From save. */
    public Processor(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, ResourceType type, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.type = type;
    }
    
    /** From comms. */
    public Processor(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.type = ResourceType.values()[bb.get()];
    }
    
    public ResourceType GetType()
    {
        return type;
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.put((byte)type.ordinal());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public String GetTypeName()
    {
        switch(type)
        {
            case FUEL: return "oil refinery";
            case FOOD: return "granary";
            case STEEL: return "foundry";
            case NUCLEAR_ELECTRICITY: return "nuclear power plant";
            case ELECTRICITY: return "power plant";
            case CONSTRUCTION_SUPPLIES: return "construction yard";
            case MEDICINE: return "laboratory";
            case MACHINERY: return "machine shop";
            case ELECTRONICS: return "lithography plant";
            case ENRICHED_URANIUM: return "enrichment facility";
            case COAL:
            case WEALTH: return "coin mint";
            default: return "PROCESSOR TYPE UNKNOWN";
        }
    }
    
    public static String GetPluralTypeName(ResourceType type)
    {
        switch(type)
        {
            case FUEL: return "oil refineries";
            case FOOD: return "granaries";
            case STEEL: return "foundries";
            case NUCLEAR_ELECTRICITY: return "nuclear power plants";
            case ELECTRICITY: return "power plants";
            case CONSTRUCTION_SUPPLIES: return "construction yards";
            case MEDICINE: return "laboratories";
            case MACHINERY: return "machine shops";
            case ELECTRONICS: return "lithography plants";
            case ENRICHED_URANIUM: return "enrichment facilities";
            case COAL:
            case WEALTH: return "coin mints";
            default: return "PROCESSOR TYPES UNKNOWN";
        }
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Processor)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.PROCESSOR;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Processor;
    }
}
