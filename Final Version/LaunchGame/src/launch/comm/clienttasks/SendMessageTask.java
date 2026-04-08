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
import launch.game.entities.MissileFactory.ChatChannel;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class SendMessageTask extends Task
{
    private ChatChannel channel;
    
    public SendMessageTask(LaunchClientGameInterface gameInterface, int lReceiverID, ChatChannel channel, String strName)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.TRANSMITTING);
        this.channel = channel;
        
        ByteBuffer bb = ByteBuffer.allocate(5 + LaunchUtilities.GetStringDataSize(strName));
        bb.putInt(lReceiverID);
        bb.put((byte)channel.ordinal());
        bb.put(LaunchUtilities.GetStringData(strName));
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SendMessage, cData);
    }
    
    @Override
    public void HandleCommand(int lCommand, int lInstanceNumber)
    {
        if(lCommand == LaunchSession.ActionSuccess && channel == ChatChannel.PRIVATE)
        {
            gameInterface.EventReceived(new LaunchEvent("Message sent."));
            Finish();
        }
        else
        {
            super.HandleCommand(lCommand, lInstanceNumber);
        }
    }
}
