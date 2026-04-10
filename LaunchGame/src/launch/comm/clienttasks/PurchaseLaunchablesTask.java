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
import java.util.List;
import java.util.ArrayList;
import launch.game.systems.LaunchSystem.SystemType;


/**
 *
 * @author tobster
 */
public class PurchaseLaunchablesTask extends Task
{
    private SystemType systemType;
    
    public PurchaseLaunchablesTask(LaunchClientGameInterface gameInterface, int lSiteID, int lSlotNo, int[] lTypes, SystemType systemType)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.PURCHASING);
        this.systemType = systemType;
        
        //List<Integer> intList = Arrays.stream(lTypes).boxed().collect(Collectors.toList());
        
        List<Integer> intList = new ArrayList<Integer>(lTypes.length);
        for(int i : lTypes)
        {
            intList.add(i);
        }

        byte[] cLaunchableList = LaunchUtilities.GetIntListData(intList);
        
        ByteBuffer bb = ByteBuffer.allocate(9 + cLaunchableList.length);
        bb.putInt(lSiteID);
        bb.putInt(lSlotNo);
        bb.put(cLaunchableList);
        bb.put((byte) systemType.ordinal());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.PurchaseLaunchables, cData);
    }
}
