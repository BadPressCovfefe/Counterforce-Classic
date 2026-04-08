/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import tobcomm.TobComm;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;
import launch.utilities.LaunchUtilities;


/**
 *
 * @author tobster
 */
public class MoveOrderTask extends Task
{
    public MoveOrderTask(LaunchClientGameInterface gameInterface, List<EntityPointer> Movables, Map<Integer, GeoCoord> Coordinates)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cPointerData = LaunchUtilities.BBFromPointers(Movables);
        byte[] cCoordinateData = LaunchUtilities.BBFromGeoCoords(Coordinates);
        
        ByteBuffer bb = ByteBuffer.allocate(cPointerData.length + cCoordinateData.length);
        bb.put(cPointerData);
        bb.put(cCoordinateData);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.MoveOrder, cData);
    }
}
