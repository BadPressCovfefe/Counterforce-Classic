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
 * This class details MIRV target data for ICBMs combined into 1 class to make it easier to serialize through TobComm.
 */
public class TargetData 
{
    public static final int DATA_SIZE = 9; //8 for the geocoord since it contains 2 floats, 1 for the boolean bAirburst.
    
    private GeoCoord geoTarget;
    private boolean bAirburst;
    
    public TargetData(GeoCoord geoTarget, boolean bAirburst)
    {
        this.geoTarget = geoTarget;
        this.bAirburst = bAirburst;
    }
    
    public TargetData(ByteBuffer bb)
    {
        geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
        bAirburst = (bb.get() != 0x00);
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.put((byte)(bAirburst ? 0xFF : 0x00));
        
        return bb.array();
    }
    
    public GeoCoord GetGeoTarget()
    {
        return this.geoTarget;
    }
    
    public boolean GetAirburst()
    {
        return this.bAirburst;
    }  
}
