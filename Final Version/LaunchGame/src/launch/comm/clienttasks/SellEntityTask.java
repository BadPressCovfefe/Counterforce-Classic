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


/**
 *
 * @author tobster
 */
public class SellEntityTask extends Task
{
    public SellEntityTask(LaunchClientGameInterface gameInterface, EntityPointer pointer)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.DECOMISSIONING);
        
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(pointer.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SellEntity, cData);
    }
}
