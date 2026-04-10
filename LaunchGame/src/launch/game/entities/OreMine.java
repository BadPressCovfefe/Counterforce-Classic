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
import launch.game.systems.ResourceSystem;


/**
 *
 * @author tobster
 */
public class OreMine extends Structure
{
    private static final int DATA_SIZE = 5;
    
    private int lDepositID;
    private ResourceType type;
    
    /** New. */
    public OreMine(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, int lDepositID, ResourceType type, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.lDepositID = lDepositID;
        this.type = type;
    }
    
    /** From save. */
    public OreMine(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, int lDepositID, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceType type, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.lDepositID = lDepositID;
        this.type = type;
    }
    
    /** From comms. */
    public OreMine(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.lDepositID = bb.getInt();
        this.type = ResourceType.values()[bb.get()];
        
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
        bb.putInt(lDepositID);
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
            case COAL: return "coal mine";
            case IRON: return "iron mine";
            case OIL: return "oil well";
            case CROPS: return "farm";
            case LUMBER: return "lumber mill";
            case CONCRETE: return "quarry";
            case URANIUM: return "uranium mine";
            case GOLD: return "gold mine";
            case STEEL: return "metal recycling team";
            case MACHINERY: return "industrial salvage unit";
            case ELECTRICITY: return "power recovery unit";
            case FUEL: return "fuel recovery team";
            case FOOD: return "ration recovery unit";
            case ELECTRONICS: return "tech salvage unit";
            case MEDICINE: return "medical recovery unit";
            case ENRICHED_URANIUM: return "nuclear recovery unit";
            default: return "ORE MINE TYPE UNKNOWN";
        }
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof OreMine)
            return entity.GetID() == lID;
        
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.ORE_MINE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.OreMine;
    }
    
    public void SetDeposit(int lDepositID)
    {
        this.lDepositID = lDepositID;
    }
    
    public int GetDepositID()
    {
        return this.lDepositID;
    }
    
    public ResourceType GetType()
    {
        return this.type;
    }
}
