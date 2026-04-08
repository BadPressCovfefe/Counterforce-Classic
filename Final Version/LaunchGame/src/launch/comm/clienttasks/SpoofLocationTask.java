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
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class SpoofLocationTask extends Task
{
    private LaunchClientLocation spoofedLocation;
    
    public SpoofLocationTask(LaunchClientGameInterface gameInterface, LaunchClientLocation spoofedLocation)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        this.spoofedLocation = spoofedLocation;
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SpoofLocation, spoofedLocation.GetData());
    }
}
