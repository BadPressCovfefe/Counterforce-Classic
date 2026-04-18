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
import launch.utilities.ShortDelay;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author tobster
 */
public class Loot extends MapEntity implements Haulable
{
    private static final int DATA_SIZE = 17;
    
    private LootType lootType;
    private int lType;                                                          //This int indicates what sort of thing of the given loottype we are dealing with. For example, if LootType is resources, lTypeID is the ordinal of the ResourceType. If it is missiles, then lTypeID is the MissileType id. 
    private long oQuantity;                                                      //How many of whatever loot thing is this loot holding? (In tons if resources, in number of missiles if missiles/interceptors.)
    private ShortDelay dlyExpiry;
    
    //Flags (not transmitted).
    private boolean bCollected = false;     //Indicates the loot has been collected and may be cleared up.
    
    public Loot(int lID, GeoCoord geoPosition, LootType lootType, int lType, long oQuantity, int lExpiry)
    {
        super(lID, geoPosition, true, 0);
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.bVisible = true;
        this.lootType = lootType;
        this.lType = lType;
        this.oQuantity = oQuantity;
    }
    
    public Loot(ByteBuffer bb)
    {
        super(bb);
        lootType = LootType.values()[bb.get()];
        lType = bb.getInt();
        oQuantity = bb.getLong();
        dlyExpiry = new ShortDelay(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        dlyExpiry.Tick(lMS);
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte cBaseData[] = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.put((byte)lootType.ordinal());
        bb.putInt(lType);
        bb.putLong(oQuantity);
        dlyExpiry.GetData(bb);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public boolean Expired()
    {
        return dlyExpiry.Expired();
    }
    
    public int GetExpiryRemaining() { return dlyExpiry.GetRemaining(); }
        
    /**
     * Mark the loot as collected, such that it will be removed from the game during the next tick.
     */
    public void Collect() { bCollected = true; }
    
    public boolean Collected() { return bCollected; }
    
    @Override
    public long GetWeight() 
    { 
        return 1;
    }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return true;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Loot)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.LOOT;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Loot;
    }
    
    public void SetQuantity(long oQuantity)
    {
        this.oQuantity = oQuantity;
        Changed(true);
    }
    
    public void SetExpiry(int lExpiry)
    {
        this.dlyExpiry.Set(lExpiry);
        Changed(true);
    }
    
    public boolean Depleted()
    {
        return this.oQuantity <= 0;
    }
    
    @Override
    public long GetQuantity()
    {
        return this.oQuantity;
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    public boolean IsMoney()
    {
        return lootType == LootType.RESOURCES && lType == ResourceType.WEALTH.ordinal();
    }
    
    public long GetValue()
    {
        return oQuantity;
    }

    @Override
    public LootType GetLootType()
    {
        return lootType;
    }

    @Override
    public int GetCargoID()
    {
        return lType;
    }
    
    @Override
    public String GetTypeName()
    {
        return "loot";
    }
    
    /**
     * Subtract an amount from the loot's quantity. If the amount is more than what is left, return what is left. If it is more, return the amount.
     * @param lQuantity the quantity to remove.
     * @return The amount of loot remaining if less than lQuantity, otherwise lQuantity.
     */
    public long SubtractQuantity(long oQuantity)
    {
        if(oQuantity < this.oQuantity)
        {
            this.oQuantity -= oQuantity;
            return oQuantity;
        }
        else
        {
            long oAmountPresent = this.oQuantity;
            this.oQuantity = 0;
            return oAmountPresent;
        }
    }
    
    public void AddQuantity(long oQuantity)
    {
        this.oQuantity += oQuantity;
    }
}
