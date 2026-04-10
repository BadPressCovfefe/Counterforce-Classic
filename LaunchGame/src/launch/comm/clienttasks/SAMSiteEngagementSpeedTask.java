/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.List;
import launch.comm.LaunchSession;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.MapEntity;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class SAMSiteEngagementSpeedTask extends Task
{
    private int lSiteID = MapEntity.ID_NONE;
    
    public SAMSiteEngagementSpeedTask(LaunchClientGameInterface gameInterface, int lSiteID, float fltSpeed)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        this.lSiteID = lSiteID;
        
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(lSiteID);
        bb.putFloat(fltSpeed);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.EngagementSpeedChange, cData);
    }
}
