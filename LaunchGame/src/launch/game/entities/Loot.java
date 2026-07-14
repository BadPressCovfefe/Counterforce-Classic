/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.GeoCoord;
import launch.utilities.ShortDelay;
import launch.game.EntityPointer.EntityType;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public class Loot extends MapEntity
{
    private static final int DATA_SIZE = 12;
    
    private String strTitle;                                                    
    private long oQuantity;                                                     
    private ShortDelay dlyExpiry;
    
    //Flags (not transmitted).
    private boolean bCollected = false;                                         //Indicates the loot has been collected and may be cleared up.
    
    public Loot(int lID, GeoCoord geoPosition, String strTitle, long oQuantity, int lExpiry)
    {
        super(lID, geoPosition, true, 0);
        this.strTitle = strTitle;
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.bVisible = true;
        this.oQuantity = oQuantity;
    }
    
    public Loot(ByteBuffer bb)
    {
        super(bb);
        strTitle = LaunchUtilities.StringFromData(bb);
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
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + LaunchUtilities.GetStringDataSize(strTitle));
        bb.put(cBaseData);
        bb.put(LaunchUtilities.GetStringData(strTitle));
        bb.putLong(oQuantity);
        dlyExpiry.GetData(bb);
        
        return bb.array();
    }
    
    public String GetDescription()
    {
        return strTitle;
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
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    public long GetValue()
    {
        return oQuantity;
    }
    
    @Override
    public String GetTypeName()
    {
        return strTitle;
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
