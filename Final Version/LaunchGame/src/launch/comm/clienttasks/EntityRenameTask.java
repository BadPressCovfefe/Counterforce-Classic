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
public class EntityRenameTask extends Task
{
    public EntityRenameTask(LaunchClientGameInterface gameInterface, EntityPointer pointer, String strName)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(5 + LaunchUtilities.GetStringDataSize(strName));
        bb.put(pointer.GetData());
        bb.put(LaunchUtilities.GetStringData(strName));
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.EntityNameChange, cData);
    }
}
