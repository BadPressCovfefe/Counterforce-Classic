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
public class PurchaseInfantryTask extends Task
{
    public PurchaseInfantryTask(LaunchClientGameInterface gameInterface, int lArmoryID, boolean bUseSubstitutes)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.PURCHASING);
        
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.putInt(lArmoryID);
        bb.put((byte)(bUseSubstitutes ? 0xFF : 0x00));  
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.PurchaseInfantry, cData);
    }
}
