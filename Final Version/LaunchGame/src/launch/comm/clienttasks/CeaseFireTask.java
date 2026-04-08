/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class CeaseFireTask extends Task
{    
    public CeaseFireTask(LaunchClientGameInterface gameInterface, List<EntityPointer> Pointers)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cPointerData = LaunchUtilities.BBFromPointers(Pointers);
        
        ByteBuffer bb = ByteBuffer.allocate(cPointerData.length);
        bb.put(cPointerData);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.CeaseFire, cData);
    }
}
