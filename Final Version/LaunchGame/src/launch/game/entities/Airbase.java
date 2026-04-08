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
import launch.game.EntityPointer.EntityType;
import launch.game.systems.AircraftSystem;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.ResourceSystem;

/**
 *
 * @author tobster
 */
public class Airbase extends Structure implements LaunchSystemListener, AirbaseInterface
{
    private static final int DATA_SIZE = 1;
    
    private AircraftSystem aircrafts;
    
    /** New. */
    public Airbase(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.bVisible = false;
        this.aircrafts = new AircraftSystem(this, Defs.AIRCRAFT_LAUNCH_COOLDOWN, Defs.AIRBASE_DEFAULT_SLOTS);
    }
    
    /** From save. */
    public Airbase(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, AircraftSystem aircrafts, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.bVisible = bVisible;
        this.aircrafts = aircrafts;
        aircrafts.SetSystemListener(this);
    }
    
    /** From comms. */
    public Airbase(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
                
        //Boolean indicating whether the full system was sent or not.
        if(bb.get() != 0x00)
            this.aircrafts = new AircraftSystem(this, bb);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        aircrafts.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cAircraftData = aircrafts.GetData(lAskingID);
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE + cAircraftData.length);
        bb.put(cBaseData);
        bb.put((byte)0xFF);
        bb.put(cAircraftData);
        
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
        return "airbase";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Airbase)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.AIRBASE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Airbase;
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public AircraftSystem GetAircraftSystem()
    {
        return this.aircrafts;
    }
}
