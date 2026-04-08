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
import launch.utilities.Security;


/**
 *
 * @author tobster
 */
public class RegisterTask extends Task
{
    public RegisterTask(LaunchClientGameInterface gameInterface, String strGoogleID, String strUsername, int lAvatarID, byte[] cDeviceID)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.REGISTERING);
        
        ByteBuffer bb = ByteBuffer.allocate(cDeviceID.length + LaunchUtilities.GetStringDataSize(strGoogleID) + LaunchUtilities.GetStringDataSize(strUsername) + 4);
        bb.put(LaunchUtilities.GetStringData(strGoogleID));
        bb.put(LaunchUtilities.GetStringData(strUsername));
        bb.put(cDeviceID);
        bb.putInt(lAvatarID);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.Registration, 0, 0, cData);
    }

    @Override
    public void HandleCommand(int lCommand, int lInstanceNumber)
    {
        switch(lCommand)
        {
            case LaunchSession.AccountCreateSuccess:
            {
                //Do nothing. Client session has already sent the relevant commands and passed this onto us so we can die.
            }
            break;
            
            case LaunchSession.NameTaken:
            {
                //Notify game that the requested name is taken.
                gameInterface.AccountNameTaken();
            }
            break;
            
            case LaunchSession.DeviceIDUsed:
            {
                gameInterface.DeviceIDUsed();
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
