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
 * @author tobster
 */
public class AddBountyTask extends Task
{
    public AddBountyTask(LaunchClientGameInterface gameInterface, int lPlayerID, int lAmount, boolean bPlayer)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.GIVING);
        
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.putInt(lAmount);
        bb.putInt(lPlayerID);
        bb.put((byte)(bPlayer ? 0xFF : 0x00));
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.AddBounty, cData);
    }
}
