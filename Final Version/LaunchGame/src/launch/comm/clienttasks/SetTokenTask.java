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
public class SetTokenTask extends Task
{
    public SetTokenTask(LaunchClientGameInterface gameInterface, String strToken)
    {
        super(gameInterface);
        
        byte[] cToken = LaunchUtilities.GetStringData(strToken);
        ByteBuffer bb = ByteBuffer.allocate(LaunchUtilities.GetStringDataSize(strToken));
        bb.put(cToken);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SetFirebaseToken, cData);
    }
}
