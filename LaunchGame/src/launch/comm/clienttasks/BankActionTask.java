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
public class BankActionTask extends Task
{
    public BankActionTask(LaunchClientGameInterface gameInterface, int lBankID, int lAmount, boolean bWithdraw)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.putInt(lBankID);
        bb.putInt(lAmount);
        bb.put((byte)(bWithdraw? 0xff : 0x00));
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.BankAction, cData);
    }
}
