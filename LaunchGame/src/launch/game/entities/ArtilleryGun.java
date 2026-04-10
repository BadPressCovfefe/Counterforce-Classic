/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchLog;

/**
 *
 * @author tobster
 */
public class ArtilleryGun extends Structure implements LaunchSystemListener, LauncherInterface, ArtilleryInterface
{
    private static final int DATA_SIZE = 2;
    
    MissileSystem shells = null;
    private FireOrder fireOrder = null;
    
    public static final byte MODE_AUTO = 0;        //Will automatically engage threats.
    public static final byte MODE_SEMI_AUTO = 1;   //Will automatically engage threats if the player is offline.
    public static final byte MODE_MANUAL = 2;      //Will not automatically engage threats.
    
    private byte cMode;
    
    private boolean bFire;
    
    /** New. */
    public ArtilleryGun(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, int lReloadTime, int lMissileSlots, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        shells = new MissileSystem(this, lReloadTime, lMissileSlots);
        this.bFire = false;
        this.cMode = MODE_AUTO;
    }
    
    /** From save. */
    public ArtilleryGun(int lID, 
            GeoCoord geoPosition, 
            short nHP, 
            short nMaxHP, 
            String strName, 
            int lOwnerID, 
            byte cState, 
            int lStateTime, 
            MissileSystem shells, 
            boolean bVisible, 
            int lVisibleTime, 
            byte cMode, 
            FireOrder fireOrder, 
            int lBuiltByID, 
            ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cState, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.shells = shells;
        this.shells.SetSystemListener(this);
        this.cMode = cMode;
        if(fireOrder != null)
            this.fireOrder = fireOrder;
    }
    
    /** From comms. */
    public ArtilleryGun(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        shells = new MissileSystem(this, bb);
        cMode = bb.get();
        if(bb.get() != 0x00)
            fireOrder = new FireOrder(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        shells.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cMagazineData = shells.GetData(lAskingID);
        byte[] cFireOrderData = fireOrder != null ? fireOrder.GetData() : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cFireOrderData.length + cBaseData.length + cMagazineData.length);
        
        bb.put(cBaseData);
        bb.put(cMagazineData);
        bb.put(cMode);
        bb.put((byte)(HasFireOrder() ? 0xFF : 0x00));
        bb.put(cFireOrderData);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public MissileSystem GetMissileSystem() { return shells; }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(false);
    }

    @Override
    public String GetTypeName()
    {
        return "artillery gun";       
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof ArtilleryGun)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.ARTILLERY_GUN;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.ArtilleryGun;
    }
    
    @Override
    public boolean HoldsMissiles()
    {
        return true;
    }
    
    @Override 
    public boolean HoldsInterceptors()
    {
        return false;
    }

    @Override
    public FireOrder GetFireOrder()
    {
        return fireOrder;
    }

    @Override
    public boolean HasFireOrder()
    {
        return fireOrder != null;
    }

    @Override
    public void RoundsComplete()
    {
        this.fireOrder = null;
    }

    @Override
    public void FireForEffect(FireOrder order)
    {
        this.fireOrder = order;
    }
    
    public boolean GetAuto() { return cMode == MODE_AUTO; }
    
    public boolean GetSemiAuto() { return cMode == MODE_SEMI_AUTO; }
    
    public boolean GetManual() { return cMode == MODE_MANUAL; }
    
    public byte GetMode() { return cMode; }
    
    public void SetMode(byte cMode)
    {
        this.cMode = cMode;
        Changed(true);
    }
}
