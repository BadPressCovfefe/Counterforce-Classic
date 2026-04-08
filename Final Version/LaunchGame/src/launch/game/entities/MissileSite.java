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
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchLog;
import launch.utilities.ShortDelay;

/**
 *
 * @author tobster
 */
public class MissileSite extends Structure implements LaunchSystemListener, LauncherInterface
{
    private static final int DATA_SIZE = 3;
    
    MissileSystem missiles = null;
    
    private byte cMode;
    private boolean bICBM;
    private boolean bRetaliating = false;
    
    public static final byte MODE_MANUAL = 0;        //Will not automatically respond to incoming ICBMs.
    public static final byte MODE_SEMI_AUTO = 1;     //Will automatically respond to incoming ICBMs if the player is offline.
    public static final byte MODE_AUTO = 2;          //Will automatically respond to incoming ICBMs.
    
    //These retaliation-related fields are all server-only, for now.
    private ShortDelay dlyRetaliate = new ShortDelay();
    private int lTargetPlayerID;
    private boolean bLaunchReady = false;
    
    /** New. */
    public MissileSite(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, int lReloadTime, byte cMissileSlots, boolean bICBM, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        missiles = new MissileSystem(this, lReloadTime, cMissileSlots);
        this.bICBM = bICBM;
        this.cMode = MODE_AUTO;
        this.bVisible = false;
    }
    
    /** From save. */
    public MissileSite(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cState, int lStateTime, boolean bICBM, MissileSystem missileSystem, byte cMode, boolean bRetaliating, int lDelayRetaliate, int lTargetPlayerID, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cState, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        missiles = missileSystem;
        missiles.SetSystemListener(this);
        this.bICBM = bICBM;
        this.cMode = cMode;
        this.bVisible = bVisible;
        this.bRetaliating = bRetaliating;
        this.dlyRetaliate = new ShortDelay(lDelayRetaliate);
        this.lTargetPlayerID = lTargetPlayerID;
    }
    
    /** From comms. */
    public MissileSite(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        
        bICBM = (bb.get() != 0x00);
        bRetaliating = (bb.get() != 0x00);
        cMode = bb.get();
        
        if(lReceivingID == lOwnerID || lReceivingID == ID_NONE)
        {
            missiles = new MissileSystem(this, bb);
        }
        else
        {
            missiles = new MissileSystem();
        } 
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        missiles.Tick(lMS);
        
        if(dlyRetaliate != null && !dlyRetaliate.Expired())
        {
            dlyRetaliate.Tick(lMS);
        }
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cMissileSystemData = (lAskingID == lOwnerID || lAskingID == ID_NONE) ? missiles.GetData(lAskingID) : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cMissileSystemData.length);
        
        bb.put(cBaseData);
        bb.put((byte)(bICBM? 0xFF : 0x00));
        bb.put((byte)(bRetaliating? 0xFF : 0x00));
        bb.put(cMode);
        bb.put(cMissileSystemData);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public boolean CanTakeICBM()
    {
        return bICBM;
    }
    
    @Override
    public MissileSystem GetMissileSystem() { return missiles; }
    
    public void UpgradeToNuclear()
    {
        //"Upgrade to nuclear" is a removed feature. -Corbin
        //bNuclear = true;
        //Changed(false);
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        LaunchLog.Log(LaunchLog.LogType.GAME, "MissileSite", String.format("MisileSite %s system updated.", String.valueOf(lID)));
        Changed(false);
    }

    @Override
    public String GetTypeName()
    {
        if(bICBM)
        {
            return "ICBM silo";
        }
        else
        {
            return "missile battery";   
        }        
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

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof MissileSite)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return bICBM ? EntityType.NUCLEAR_MISSILE_SITE : EntityType.MISSILE_SITE;
    }
    
    public void Retaliate(int lTargetPlayerID, int lRetaliateTimeMS)
    {
        bRetaliating = true;
        dlyRetaliate = new ShortDelay(lRetaliateTimeMS);
        this.lTargetPlayerID = lTargetPlayerID;
        Changed(false);
    }
    
    public void DoneRetaliating()
    {
        bRetaliating = false;
        this.dlyRetaliate = new ShortDelay();
        bLaunchReady = false;
        Changed(false);
    }
    
    public boolean Retaliating()
    {
        return this.bRetaliating;
    }
    
    public int GetRetaliateTime()
    {
        return dlyRetaliate.GetRemaining();
    }
    
    public boolean ReadyToFire()
    {
        return dlyRetaliate.Expired() && bRetaliating;
    }
    
    public int GetRetaliationTarget()
    {
        return this.lTargetPlayerID;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.MissileSite;
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
}
