/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;

/**
 *
 * @author tobster
 */
public class Interceptor extends MapEntity
{
    private static final int DATA_SIZE = 14;
    
    private int lType;             //The type of interceptor this is.
    private int lOwnerID;           //The player that launched the interceptor missile.
    private int lTargetID;          //The missile the interceptor is chasing.
    private boolean bPlayerLaunched;//Interceptors launched manually by the player gain an accuracy bonus.
    //private float fltHitChanceBonus;  //The bonus conferred by radar.
    
    //Server-only. 
    private float fltDistanceTraveled = 0.0f;
    private float fltCurrentSpeedKPH = 0.0f;
    private int lLaunchedByID; //The id of the aircraft that launched this interceptor, if it was launched by an aircraft. Used to track kills for aircraft.
    
    private EntityType targetType;
    
    public Interceptor(int lID, GeoCoord geoPosition, int lOwnerID, int lTargetID, int lType, boolean bPlayerLaunched, EntityType targetType, int lLaunchedBy)
    {
        super(lID, geoPosition, true, 0);
        this.lType = lType;
        this.lOwnerID = lOwnerID;
        this.lTargetID = lTargetID;
        this.bPlayerLaunched = bPlayerLaunched;
        this.targetType = targetType;
        this.lLaunchedByID = lLaunchedBy;
    }
    
    //From save
    public Interceptor(int lID, GeoCoord geoPosition, int lOwnerID, int lTargetID, int lType, boolean bPlayerLaunched, byte cTargetType, int lLaunchedBy, float fltDistanceTraveled)
    {
        super(lID, geoPosition, true, 0);
        this.lType = lType;
        this.lOwnerID = lOwnerID;
        this.lTargetID = lTargetID;
        this.bPlayerLaunched = bPlayerLaunched;
        this.targetType = EntityType.values()[cTargetType];
        this.lLaunchedByID = lLaunchedBy;
        this.fltDistanceTraveled = fltDistanceTraveled;
    }
    
    public Interceptor(ByteBuffer bb)
    {
        super(bb);
        this.lType = bb.getInt();
        this.lOwnerID = bb.getInt();
        this.lTargetID = bb.getInt();
        bPlayerLaunched = (bb.get() != 0x00);
        //this.fltHitChanceBonus = bb.getFloat();
        this.targetType = EntityType.values()[bb.get()];
    }

    @Override
    public void Tick(int lMS)
    {
        //Nothing to do here.
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        
        bb.put(cBaseData);
        bb.putInt(lType);
        bb.putInt(lOwnerID);
        bb.putInt(lTargetID);
        bb.put((byte)(bPlayerLaunched? 0xFF : 0x00));
        //bb.putFloat(fltHitChanceBonus);
        bb.put((byte)GetTargetType().ordinal());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public int GetType() { return lType; }
    
    @Override
    public int GetOwnerID() { return lOwnerID; }
    
    public int GetTargetID() { return lTargetID; }
    
    public boolean GetPlayerLaunched() { return bPlayerLaunched; }
    
    //public float GetHitChanceBonus() { return fltHitChanceBonus; }
    
    public float GetDistanceTraveled() { return this.fltDistanceTraveled; }
    
    public void Traveled(float fltDistance)
    {
        this.fltDistanceTraveled += fltDistance;
    }
    
    public void SetTargetType(EntityType newType)
    {
        targetType = newType;
    }
    
    public EntityType GetTargetType()
    {
        return this.targetType;
    }
    
    public int GetLaunchedByID()
    {
        return this.lLaunchedByID;
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Interceptor)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.INTERCEPTOR;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Interceptor;
    }
    
    @Override
    public String GetTypeName()
    {
        return "interceptor";
    }
    
    public void SetCurrentSpeed(float fltSpeedKPH)
    {
        this.fltCurrentSpeedKPH = fltSpeedKPH;
    }
    
    public float GetCurrentSpeed()
    {
        return this.fltCurrentSpeedKPH;
    }
    
    public EntityPointer GetTargetEntity()
    {
        return new EntityPointer(lTargetID, targetType);
    }
}
