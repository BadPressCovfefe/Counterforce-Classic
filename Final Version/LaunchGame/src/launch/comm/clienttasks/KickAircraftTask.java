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
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class KickAircraftTask extends Task
{    
    public KickAircraftTask(LaunchClientGameInterface gameInterface, EntityPointer aircraft)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.PROMOTING);
        
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(aircraft.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.KickAircraft, cData);
    }
}
