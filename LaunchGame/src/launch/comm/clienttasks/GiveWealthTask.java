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
import launch.game.entities.conceptuals.Resource.ResourceType;


/**
 *
 * @author tobster
 */
public class GiveWealthTask extends Task
{
    public GiveWealthTask(LaunchClientGameInterface gameInterface, int lPlayerID, int lAmount, ResourceType type)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.GIVING);
        
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.putInt(lAmount);
        bb.putInt(lPlayerID);
        bb.put((byte)type.ordinal());
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.GiveWealth, cData);
    }
}
