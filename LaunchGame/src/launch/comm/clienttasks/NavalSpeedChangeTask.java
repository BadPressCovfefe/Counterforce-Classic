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
import launch.game.EntityPointer.EntityType;
import tobcomm.TobComm;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class NavalSpeedChangeTask extends Task
{
    public NavalSpeedChangeTask(LaunchClientGameInterface gameInterface, EntityPointer vessel, boolean bIncrease)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte)(bIncrease ? 0xFF : 0x00));
        bb.put(vessel.GetData());
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.NavalVesselSpeedChange, cData);
    }
}
