/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.nio.ByteBuffer;

/**
 *
 * @author Corbin
 */
public class GeoRectangle
{
    private float fltNorthLat;
    private float fltSouthLat;
    private float fltWestLong;
    private float fltEastLong;
    private GeoCoord geoSW;
    private GeoCoord geoNE;
    
    public GeoRectangle(float fltNorthLat, float fltSouthLat, float fltWestLong, float fltEastLong)
    {
        this.fltNorthLat = fltNorthLat;
        this.fltSouthLat = fltSouthLat;
        this.fltWestLong = fltWestLong;
        this.fltEastLong = fltEastLong;
        this.geoSW = new GeoCoord(fltSouthLat, fltWestLong);
        this.geoNE = new GeoCoord(fltNorthLat, fltEastLong);
    }
    
    public GeoRectangle(ByteBuffer bb)
    {
        this.fltNorthLat = bb.getFloat();
        this.fltSouthLat = bb.getFloat();
        this.fltWestLong = bb.getFloat();
        this.fltEastLong = bb.getFloat();
        this.geoSW = new GeoCoord(fltSouthLat, fltWestLong);
        this.geoNE = new GeoCoord(fltNorthLat, fltEastLong);
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(16); //Because each float is 4 bytes.
        bb.putFloat(fltNorthLat);
        bb.putFloat(fltSouthLat);
        bb.putFloat(fltWestLong);
        bb.putFloat(fltEastLong);
        
        return bb.array();
    }
    
    public boolean Contains(GeoCoord geoCoord)
    {
        return geoCoord.IsInsideGeoRect(geoSW, geoNE);
    }
}
