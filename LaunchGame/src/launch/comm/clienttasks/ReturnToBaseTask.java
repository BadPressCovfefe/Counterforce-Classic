/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import launch.comm.LaunchSession;
import launch.game.Defs;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class ReturnToBaseTask extends Task
{
    private int lAircraftID;
	
    public ReturnToBaseTask(LaunchClientGameInterface gameInterface, int lAircraftID)
    {
        super(gameInterface);
        this.lAircraftID = lAircraftID;
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendCommand(LaunchSession.AircraftReturn, lAircraftID);
    }
}
