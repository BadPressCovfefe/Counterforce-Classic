/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.GeoCoord;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;

/**
 *
 * @author tobster
 */
public class BuildShipyardTask extends Task
{
    public BuildShipyardTask(LaunchClientGameInterface gameInterface, GeoCoord geoOutput)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONSTRUCTING);
                
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putFloat(geoOutput.GetLatitude());
        bb.putFloat(geoOutput.GetLongitude());
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.BuildShipyard, 0, 0, cData);
    }
}
