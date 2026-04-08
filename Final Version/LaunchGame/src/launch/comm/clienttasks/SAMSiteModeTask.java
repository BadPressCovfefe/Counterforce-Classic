/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.MapEntity;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class SAMSiteModeTask extends Task
{
    public SAMSiteModeTask(LaunchClientGameInterface gameInterface, EntityPointer pointer, byte cMode)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cPointerListData = LaunchUtilities.BBFromPointers(Arrays.asList(pointer));
        
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + cPointerListData.length);
        bb.put(cMode);
        bb.put(cPointerListData);
        cData = bb.array();
    }
    
    public SAMSiteModeTask(LaunchClientGameInterface gameInterface, List<EntityPointer> Pointers, byte cMode)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cPointerListData = LaunchUtilities.BBFromPointers(Pointers);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + cPointerListData.length);
        bb.put(cMode);
        bb.put(cPointerListData);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.SAMSiteModeChange, cData);
    }
}
