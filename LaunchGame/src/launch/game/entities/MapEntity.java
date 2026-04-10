/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import launch.game.Quadtree;
import launch.utilities.ShortDelay;

/**
 *
 * @author tobster
 */
public abstract class MapEntity extends LaunchEntity
{
    private static final int DATA_SIZE = 13;
    
    protected GeoCoord geoPosition;
    protected boolean bVisible;
    protected ShortDelay dlyVisible;
    
    private int lNearbyCityID = LaunchEntity.ID_NONE; //Server only.
    
    //Server-only, atleast for now. Used to track the quad of moving entities such as aircraft and missiles. Interceptors don't need to be put in the quadtree.
    public Quadtree quad;
    
    public MapEntity(int lID, GeoCoord geoPosition)
    {
        super(lID);
        this.geoPosition = geoPosition;
        this.bVisible = false;
        dlyVisible = new ShortDelay();
    }
    
    public MapEntity(int lID, GeoCoord geoPosition, boolean bVisible, int lVisibleTime)
    {
        super(lID);
        this.geoPosition = geoPosition;
        this.bVisible = bVisible;
        dlyVisible = new ShortDelay(lVisibleTime);
    }
    
    public MapEntity(ByteBuffer bb)
    {
        super(bb);
        this.geoPosition = new GeoCoord(bb.getFloat(), bb.getFloat());
        bVisible = (bb.get() != 0x00);
        dlyVisible = new ShortDelay(bb);
    }
    
    /**
     * Get data to communicate, base class. Subclasses should override and super call this method.
     * @param lAskingID The ID of the player this data will be sent to.
     * @return The data to communicate.
     */
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.putFloat(geoPosition.GetLatitude());
        bb.putFloat(geoPosition.GetLongitude());
        bb.put((byte)(bVisible? 0xFF : 0x00));
        bb.putInt(dlyVisible.GetRemaining());
        
        return bb.array();
    }
    
    /*
        For feeding dummy coordinates for players.
    */
    public byte[] GetData(int lAskingID, GeoCoord geoPosition)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.putFloat(geoPosition.GetLatitude());
        bb.putFloat(geoPosition.GetLongitude());
        bb.put((byte)(bVisible? 0xFF : 0x00));
        bb.putInt(dlyVisible.GetRemaining());
        
        return bb.array();
    }
    
    @Override
    public void Tick(int lMS)
    {
        dlyVisible.Tick(lMS);
        
        if(bVisible && dlyVisible.Expired())
        {
            SetInvisible();
        }
    }
    
    public GeoCoord GetPosition() { return geoPosition; }
    
    public void SetPosition(GeoCoord geoPosition)
    {
        float fltLastBearing = this.geoPosition.GetLastBearing();
        this.geoPosition = geoPosition;
        this.geoPosition.SetLastBearing(fltLastBearing);
        
        Changed(false);
    }
    
    public boolean GetVisible()
    {
        return !this.dlyVisible.Expired();
    }
    
    public int GetVisibleTimeRemaining()
    {
        return this.dlyVisible.GetRemaining();
    }
    
    public void SetVisible(int lForTime)
    {
        this.bVisible = true;
        dlyVisible.Set(lForTime);
        Changed(false);
    }
    
    public void SetInvisible()
    {
        this.bVisible = false;
        dlyVisible.Set(0);
        Changed(false);
    }
    
    public int GetNearbyCityID()
    {
        return lNearbyCityID;
    }
    
    public void SetNearbyCityID(int lID)
    {
        this.lNearbyCityID = lID;
    }
    
    public void SetNoNearbyCity()
    {
        this.lNearbyCityID = LaunchEntity.ID_NONE;
    }
    
    public boolean HasNearbyCity()
    {
        return this.lNearbyCityID != LaunchEntity.ID_NONE;
    }
}
