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

/**
 *
 * @author tobster
 */
public class ResourceDeposit extends MapEntity
{
    private static final int DATA_SIZE = 10;
    
    private ResourceType type;
    private boolean bRemove;
    private long oReserves;

    //New
    public ResourceDeposit(int lID, GeoCoord geoPosition, ResourceType type, long oReserves)
    {
        super(lID, geoPosition, true, 0);
        this.type = type;
        this.bRemove = false;
        this.oReserves = oReserves;
    }
    
    //From comms
    public ResourceDeposit(ByteBuffer bb)
    {
        super(bb);
        this.type = ResourceType.values()[bb.get()];
        this.bRemove = (bb.get() != 0x00);
        this.oReserves = bb.getLong();
    }

    @Override
    public void Tick(int lMS)
    {
        
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte cBaseData[] = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.put((byte)type.ordinal());
        bb.put((byte)(bRemove? 0xFF : 0x00));
        bb.putLong(oReserves);
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public ResourceType GetType()
    {
        return type;
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        //No concept of "ownership".
        return true;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof ResourceDeposit)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.RESOURCE_DEPOSIT;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.ResourceDeposit;
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    @Override
    public String GetTypeName()
    {
        switch(type)
        {
            case OIL: return "a crude oil deposit";
            case IRON: return "an ore deposit";
            case COAL: return "a coal deposit";
            case CROPS: return "fertile soil";
            case LUMBER: return "a rich forest";
            case URANIUM: return "a uranium deposit";
            case CONCRETE: return "aggregate";
            case ELECTRICITY: return "a ruined substation";
            case GOLD: return "a gold deposit";
            case STEEL: return "a scrap heap";
            case FUEL: return "a wrecked convoy";
            case FOOD: return "a ration stockpile";
            case ELECTRONICS: return "data center ruins";
            case MACHINERY: return "an abandoned factory";
            case MEDICINE: return "an abandoned hospital";
            case ENRICHED_URANIUM: return "nuclear waste";
            default: return "UNKNOWN DEPOSIT TYPE";
        }
    }
    
    public void SetRemove()
    {
        this.bRemove = true;
    }
    
    public boolean GetRemove()
    {
        return this.bRemove;
    }
    
    public long GetReserves()
    {
        return this.oReserves;
    }
    
    public long Extract(long oAmount)
    {
        synchronized(this)
        {
            long oAmountExtracted = Math.min(Math.abs(oAmount), oReserves);

            oReserves -= oAmountExtracted;

            if(oReserves < 0)
                oReserves = 0;

            Changed(false);

            return oAmountExtracted;
        }
    }
    
    public boolean Depleted()
    {
        return this.oReserves == 0;
    }
}
