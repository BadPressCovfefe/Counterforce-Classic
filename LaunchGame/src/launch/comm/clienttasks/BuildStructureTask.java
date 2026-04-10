/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.conceptuals.Resource.ResourceType;


/**
 *
 * @author tobster
 */
public class BuildStructureTask extends Task
{
    public BuildStructureTask(LaunchClientGameInterface gameInterface, EntityType structureType, ResourceType type, int lCommandPostID, GeoCoord geoRemoteBuild, boolean bUseSubstitutes)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONSTRUCTING);
                
        ByteBuffer bb = ByteBuffer.allocate(8 + (geoRemoteBuild != null ? 8 : 0));
        bb.put((byte)structureType.ordinal());
        bb.put(type == null ? (byte) 0 : (byte)type.ordinal());
        bb.putInt(lCommandPostID);
        bb.put((byte)(geoRemoteBuild != null ? 0xFF : 0x00));
        
        if(geoRemoteBuild != null)
        {
            bb.putFloat(geoRemoteBuild.GetLatitude());
            bb.putFloat(geoRemoteBuild.GetLongitude());
        }
        
        bb.put((byte)(bUseSubstitutes ? 0xFF : 0x00));
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.BuildStructure, 0, 0, cData);
    }
}
