/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.game.GeoCoord;

/**
 *
 * @author Corbin
 * The fire order class carries info for artillery gun and battleship bombardment orders.
 */
public class FireOrder 
{
    public static final int DATA_SIZE = 12;
    
    private GeoCoord geoTarget;
    private float fltRadius;
    
    public FireOrder(GeoCoord geoTarget, float fltRadius)
    {
        this.geoTarget = geoTarget;
        this.fltRadius = fltRadius;
    }
    
    public FireOrder(ByteBuffer bb)
    {
        geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
        fltRadius = bb.getFloat();
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.putFloat(fltRadius);
        
        return bb.array();
    }
    
    public GeoCoord GetGeoTarget()
    {
        return this.geoTarget;
    }
    
    public float GetRadius()
    {
        return this.fltRadius;
    }
}
