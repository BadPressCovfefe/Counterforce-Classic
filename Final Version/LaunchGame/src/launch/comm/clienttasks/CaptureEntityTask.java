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
 * @author Corbin
 */
public class CaptureEntityTask extends Task
{
    public CaptureEntityTask(LaunchClientGameInterface gameInterface, EntityPointer structure)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CAPTURING);
        
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(structure.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.CaptureEntity, cData);
    }
}
