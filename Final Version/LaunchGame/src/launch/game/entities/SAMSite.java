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
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.game.systems.ResourceSystem;


/**
 *
 * @author tobster
 */
public class SAMSite extends Structure implements LaunchSystemListener, LauncherInterface
{
    private static final int DATA_SIZE = 6;
    
    public static final byte MODE_AUTO = 0;        //Will automatically engage threats.
    public static final byte MODE_SEMI_AUTO = 1;   //Will automatically engage threats if the player is offline.
    public static final byte MODE_MANUAL = 2;      //Will not automatically engage threats.
    
    private byte cMode;
    private boolean bAntiBallistic;
    private float fltEngageSpeed;
    
    private MissileSystem interceptors = null;
    
    /** New. */
    public SAMSite(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, int lReloadTime, byte cInterceptorSlots, boolean bAntiBallistic, float fltEngageDistance, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.cMode = MODE_AUTO;
        interceptors = new MissileSystem(this, lReloadTime, cInterceptorSlots);
        this.bAntiBallistic = bAntiBallistic;
        this.fltEngageSpeed = fltEngageDistance;
        this.bVisible = false;
    }
    
    /** From save. */
    public SAMSite(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cState, int lStateTime, byte cMode, MissileSystem interceptorSystem, boolean bAntiBallistic, float fltEngageDistance, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cState, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.cMode = cMode;
        interceptors = interceptorSystem;
        interceptors.SetSystemListener(this);
        this.bAntiBallistic = bAntiBallistic;
        this.fltEngageSpeed = fltEngageDistance;
        this.bVisible = bVisible;
    }
    
    /** From comms. */
    public SAMSite(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        
        cMode = bb.get();
        
        if(lReceivingID == lOwnerID || lReceivingID == ID_NONE)
        {
            interceptors = new MissileSystem(this, bb);
        }
        else
        {
            interceptors = new MissileSystem();
        }
        
        bAntiBallistic = (bb.get() != 0x00);
        fltEngageSpeed = bb.getFloat();
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        interceptors.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cInterceptorSystemData = (lAskingID == lOwnerID || lAskingID == ID_NONE) ? interceptors.GetData(lAskingID) : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cInterceptorSystemData.length);
        
        bb.put(cBaseData);
        bb.put(cMode);
        bb.put(cInterceptorSystemData);
        bb.put((byte)(bAntiBallistic? 0xFF : 0x00));
        bb.putFloat(fltEngageSpeed);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public MissileSystem GetInterceptorSystem() { return interceptors; }
    
    public boolean GetAuto() { return cMode == MODE_AUTO; }
    
    public boolean GetSemiAuto() { return cMode == MODE_SEMI_AUTO; }
    
    public boolean GetManual() { return cMode == MODE_MANUAL; }
    
    public byte GetMode() { return cMode; }
    
    public boolean GetIsABMSilo() { return bAntiBallistic; }
    
    public float GetEngagementSpeed() { return fltEngageSpeed; }
    
    public void SetEngagementSpeed(float fltDistance)
    {
        this.fltEngageSpeed = fltDistance;
        Changed(true);
    }
    
    public void SetMode(byte cMode)
    {
        this.cMode = cMode;
        Changed(true);
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(false);
    }

    @Override
    public String GetTypeName()
    {
        if(bAntiBallistic)
        {
            return "ABM silo";
        }
        else
        {
            return "SAM battery";
        }       
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof SAMSite)
            return entity.GetID() == lID;
        return false;
    }  
    
    @Override
    public EntityType GetEntityType()
    {
        return bAntiBallistic? EntityType.ABM_SILO : EntityType.SAM_SITE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.SamSite;
    }
    
    @Override
    public MissileSystem GetMissileSystem()
    {
        return GetInterceptorSystem();
    }
    
    @Override
    public boolean HoldsMissiles()
    {
        return false;
    }
    
    @Override 
    public boolean HoldsInterceptors()
    {
        return true;
    }
}
