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
import launch.utilities.LaunchLog;


/**
 *
 * @author tobster
 */
public class UpdateSubscriptionTask extends Task
{
    public UpdateSubscriptionTask(LaunchClientGameInterface gameInterface, boolean bSubscribed)
    {
        super(gameInterface);
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte)(bSubscribed? 0xFF : 0x00));
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SubscriptionUpdate, cData);   
        Finish();
    }
}
