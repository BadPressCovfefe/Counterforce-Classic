/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import launch.comm.LaunchSession;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class DepotCollectTask extends Task
{
    int lSiteID;
    
    public DepotCollectTask(LaunchClientGameInterface gameInterface, int lSiteID)
    {
        super(gameInterface);
        this.lSiteID = lSiteID;
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendCommand(LaunchSession.DepotCollect, lSiteID);
    }
}
