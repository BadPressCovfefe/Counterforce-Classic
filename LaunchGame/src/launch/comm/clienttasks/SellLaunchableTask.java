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
import launch.game.systems.LaunchSystem.SystemType;


/**
 *
 * @author tobster
 */
public class SellLaunchableTask extends Task
{
    public SellLaunchableTask(LaunchClientGameInterface gameInterface, int lSiteID, int lSlotNo, SystemType systemType)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.SELLING);
        
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.putInt(lSiteID);
        bb.putInt(lSlotNo);
        bb.put((byte) systemType.ordinal());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SellLaunchable, cData);
    }
}
