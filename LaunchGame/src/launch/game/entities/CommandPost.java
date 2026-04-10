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
import launch.game.systems.ResourceSystem;


/**
 *
 * @author tobster
 */
public class CommandPost extends Structure
{
    /** New. */
    public CommandPost(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
    }
    
    /** From save. */
    public CommandPost(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
    }
    
    /** From comms. */
    public CommandPost(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
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
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length);
        bb.put(cBaseData);
        
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
        return "command post";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof CommandPost)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.COMMAND_POST;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.CommandPost;
    }
    
    @Override
    public boolean GetVisible()
    {
        return true;
    }
}
