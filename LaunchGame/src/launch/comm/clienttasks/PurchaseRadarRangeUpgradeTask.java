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
public class PurchaseRadarRangeUpgradeTask extends Task
{
    private int lSiteID;
    
    public PurchaseRadarRangeUpgradeTask(LaunchClientGameInterface gameInterface)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.UPGRADING);
    }
    
    public PurchaseRadarRangeUpgradeTask(LaunchClientGameInterface gameInterface, int lSiteID)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.UPGRADING);
        
        this.lSiteID = lSiteID;
    }
    
    @Override
    public void Start(TobComm comm)
    {        
        comm.SendCommand(LaunchSession.RadarRangeUpgrade, lSiteID);       
    }
}
