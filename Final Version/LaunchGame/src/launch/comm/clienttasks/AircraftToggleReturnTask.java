/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class AircraftToggleReturnTask extends Task
{
    public AircraftToggleReturnTask(LaunchClientGameInterface gameInterface, EntityPointer aircraft)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);

        byte[] cPointerData = aircraft.GetData();
        ByteBuffer bb = ByteBuffer.allocate(cPointerData.length);
        bb.put(aircraft.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.ToggleAircraftReturn, cData);
    }
}
