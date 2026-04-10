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
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class MigrateAccountTask extends Task
{
    public MigrateAccountTask(LaunchClientGameInterface gameInterface, byte[] cDeviceID, String strGoogleID)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.MIGRATING);
        
        ByteBuffer bb = ByteBuffer.allocate(cDeviceID.length + 3 + LaunchUtilities.GetStringDataSize(strGoogleID));

        bb.put(cDeviceID);
        bb.put(LaunchUtilities.GetStringData(strGoogleID));
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.MigrateAccount, 0, 0, cData);
    }

    @Override
    public void HandleCommand(int lCommand, int lInstanceNumber)
    {
        switch(lCommand)
        {
            case LaunchSession.AccountMigrated:
            {
                gameInterface.AccountMigrated(true);
            }
            break;
            
            default:
            {
                super.HandleCommand(lCommand, lInstanceNumber);
            }
        }
        
        Finish();
    }
}
