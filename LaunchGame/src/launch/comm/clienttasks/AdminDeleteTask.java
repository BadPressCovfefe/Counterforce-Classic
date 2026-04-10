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
import launch.game.entities.Structure;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class AdminDeleteTask extends Task
{
    public AdminDeleteTask(LaunchClientGameInterface gameInterface, EntityPointer pointer)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(pointer.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.AdminDelete, cData);
    }
}
