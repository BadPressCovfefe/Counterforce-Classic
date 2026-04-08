/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer.EntityType;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.MapEntity;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class SiteOnlineOfflineTask extends Task
{
    private int lSiteID = MapEntity.ID_NONE;
    
    public SiteOnlineOfflineTask(LaunchClientGameInterface gameInterface, int lSiteID, EntityType structureType, boolean bOnline)
    {
        super(gameInterface);
        
        this.lSiteID = lSiteID;
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put((byte)(bOnline ? 0xFF : 0x00));
        bb.put((byte) structureType.ordinal());
        
        cData = bb.array();
    }
    
    public SiteOnlineOfflineTask(LaunchClientGameInterface gameInterface, List<Integer> SiteIDs, EntityType structureType, boolean bOnline)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cIntListData = LaunchUtilities.GetIntListData(SiteIDs);
        ByteBuffer bb = ByteBuffer.allocate(2 + cIntListData.length);
        bb.put((byte)(bOnline ? 0xFF : 0x00));
        bb.put(cIntListData);
        bb.put((byte) structureType.ordinal());
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.StructuresOnOff, lSiteID, 0, cData);
    }
}
