/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.systems.ResourceSystem;

import launch.utilities.ShortDelay;

/**
 *
 * @author tobster
 */
public class SentryGun extends Structure
{
    private static final int DATA_SIZE = 5;
    
    private ShortDelay dlyReload;
    private boolean bGround;
    
    /** New. */
    public SentryGun(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources, boolean bGround)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        dlyReload = new ShortDelay();
        this.bGround = bGround;
    }
    
    /** From save. */
    public SentryGun(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, int lReloadTime, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources, boolean bGround)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        dlyReload = new ShortDelay(lReloadTime);
        this.bVisible = bVisible;
        this.bGround = bGround;
    }
    
    /** From comms. */
    public SentryGun(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        dlyReload = new ShortDelay(bb.getInt());
        bGround = (bb.get() != 0x00);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        dlyReload.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        
        bb.put(cBaseData);
        dlyReload.GetData(bb);
        bb.put((byte)(bGround ? 0xFF : 0x00));
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public void SetReloadTime(int lTime)
    {
        dlyReload.Set(lTime);
        Changed(false);
    }
    
    public boolean GetCanFire()
    {
        return dlyReload.Expired();
    }
    
    public int GetReloadTimeRemaining()
    {
        return dlyReload.GetRemaining();
    }
    
    @Override
    public String GetTypeName()
    {
        return bGround ? "artillery gun" : "sentry gun";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof SentryGun)
            return entity.GetID() == lID;
        
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return bGround ? EntityType.WATCH_TOWER : EntityType.SENTRY_GUN;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.SentryGun;
    }
    
    public boolean GetIsWatchTower()
    {
        return bGround;
    }
}
