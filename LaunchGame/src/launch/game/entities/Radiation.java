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

/**
 *
 * @author tobster
 */
public class Radiation extends MapEntity
{
    private static final int DATA_SIZE = 12;
    
    private float fltRadius;
    private ShortDelay dlyExpiry;
    private int lCreatedByID;

    public Radiation(int lID, GeoCoord geoPosition, float fltRadius, int lExpiry, int lCreatedByID)
    {
        super(lID, geoPosition, true, 0);
        this.fltRadius = fltRadius;
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.lCreatedByID = lCreatedByID;
    }
    
    public Radiation(ByteBuffer bb)
    {
        super(bb);
        this.fltRadius = bb.getFloat();
        this.dlyExpiry = new ShortDelay(bb.getInt());
        this.lCreatedByID = bb.getInt();
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
        bb.putFloat(fltRadius);
        bb.putInt(dlyExpiry.GetRemaining());
        bb.putInt(lCreatedByID);
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public float GetRadius() { return this.fltRadius; }
    
    public void SetRadius(float fltNewRadius)
    {
        this.fltRadius = fltNewRadius;
        Changed(true);
    }
    
    public boolean GetExpired()
    {
        return this.dlyExpiry.Expired();
    }
    
    public int GetExpiryRemaining()
    {
        return dlyExpiry.GetRemaining();
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return true;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Radiation)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.RADIATION;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Radiation;
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    @Override
    public String GetTypeName()
    {
        return "radiation";
    }
    
    public int GetCreatedByID()
    {
        return lCreatedByID;
    }
}
