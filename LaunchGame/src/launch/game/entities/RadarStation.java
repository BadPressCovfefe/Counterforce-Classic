/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import launch.game.EntityPointer.EntityType;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.systems.ResourceSystem;

import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class RadarStation extends Structure implements ScannerInterface
{
    //If the client receiving the data is the client of the player that owns the structure, the data size transmitted will be 10. Otherwise it will be 0.
    private static final int DATA_SIZE = 1;
    
    private boolean bRadarActive;
    
    /** New. */
    public RadarStation(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.bRadarActive = false;
    }
    
    /** From save. */
    public RadarStation(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, boolean bRadarActive, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.bRadarActive = bRadarActive;
    }
    
    /** From comms. */
    public RadarStation(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        bRadarActive = (bb.get() != 0x00);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);        
        bb.put((byte)(bRadarActive? 0xFF : 0x00));
        
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
        return "radar station";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof RadarStation)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.RADAR_STATION;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.RadarStation;
    }
    
    @Override
    public boolean GetRadarActive()
    {
        return this.bRadarActive;
    }
    
    @Override
    public void SetRadarActive(boolean bActive)
    {
        this.bRadarActive = bActive;
    }
    
    @Override
    public float GetRadarRange()
    {
        return Defs.RADAR_STATION_SCANNER_RANGE;
    }
    
    @Override
    public boolean GetVisible()
    {
        return super.GetVisible() || bRadarActive;
    }
}
