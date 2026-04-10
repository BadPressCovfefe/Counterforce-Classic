/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;

/**
 *
 * @author Corbin
 */
public class TransferAccountTask extends Task
{
    public TransferAccountTask(LaunchClientGameInterface gameInterface, int lFromID, int lToID)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(lFromID);
        bb.putInt(lToID);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.TransferAccount, cData);
    }
}
