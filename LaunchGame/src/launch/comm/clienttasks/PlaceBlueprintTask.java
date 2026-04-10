/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.conceptuals.Resource.ResourceType;


/**
 *
 * @author tobster
 */
public class PlaceBlueprintTask extends Task
{
    public PlaceBlueprintTask(LaunchClientGameInterface gameInterface, EntityPointer.EntityType structureType, ResourceType resourceType, GeoCoord geoPosition)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONSTRUCTING);
        
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put((byte) structureType.ordinal());
        bb.put((byte) resourceType.ordinal());
        bb.putFloat(geoPosition.GetLatitude());
        bb.putFloat(geoPosition.GetLongitude());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.PlaceBlueprint, 0, 0, cData);
    }
}
