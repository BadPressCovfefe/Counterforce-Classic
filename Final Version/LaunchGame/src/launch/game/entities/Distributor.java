/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.ResourceSystem;


/**
 *
 * @author tobster
 */
public class Distributor extends Structure
{
    private static final int DATA_SIZE = 1;
    
    private ResourceType type;
    
    /** New. */
    public Distributor(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceType type, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.type = type;
    }
    
    /** From save. */
    public Distributor(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, ResourceType type, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.type = type;
    }
    
    /** From comms. */
    public Distributor(ByteBuffer bb, int lReceivingID)
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
            case OIL: return "Oil Pipeline";
            case IRON: return "Iron Distributor";
            case COAL: return "Coal Distributor";
            case CROPS: return "Crop Distributor";
            case URANIUM: return "Uranium Distributor";
            case ELECTRICITY: return "Power Substation";
            case WEALTH: return "Bank";
            case CONCRETE: return "Concrete Distributor";
            case LUMBER: return "Lumber Distributor";
            case FOOD: return "Food Distributor";
            case FUEL: return "Fuel Depot";
            case STEEL: return "Steel Supplier";
            case CONSTRUCTION_SUPPLIES: return "Material Supplier";
            case MACHINERY: return "Industrial Supplier";
            case ELECTRONICS: return "Electronics Supplier";
            case MEDICINE: return "Pharmaceutical Supplier";
            case ENRICHED_URANIUM: return "Radioactive Material Supplier";
            default: return "UNKNOWN DISTRIBUTOR TYPE";
        }
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Distributor)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.DISTRIBUTOR;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Distributor;
    }
}
