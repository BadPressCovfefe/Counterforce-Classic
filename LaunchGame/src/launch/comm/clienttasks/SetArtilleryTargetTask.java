/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;



public class SetArtilleryTargetTask extends Task
{
    public SetArtilleryTargetTask(LaunchClientGameInterface gameInterface, EntityPointer pointer, GeoCoord geoTarget, float fltRadius)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(17);
        bb.put(pointer.GetData());
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.putFloat(fltRadius);
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SetArtilleryTarget, cData);
    }
}
