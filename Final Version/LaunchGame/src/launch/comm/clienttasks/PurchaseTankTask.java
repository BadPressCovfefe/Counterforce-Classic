/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;


public class PurchaseTankTask extends Task
{    
    public PurchaseTankTask(LaunchClientGameInterface gameInterface, int lArmoryID, EntityPointer.EntityType type, boolean bUseSubstitutes)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.PURCHASING);
        
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.putInt(lArmoryID);
        bb.put((byte)type.ordinal());
        bb.put((byte)(bUseSubstitutes ? 0xFF : 0x00));  
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.PurchaseTank, cData);
    }
}
