/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.entities.Movable.MoveOrders;
import launch.game.systems.CargoSystem.LootType;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author Corbin
 */
public class UnitCommandTask extends Task
{    
    public UnitCommandTask(LaunchClientGameInterface gameInterface, MoveOrders command, List<EntityPointer> commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        
        byte[] cCommandablesData = LaunchUtilities.BBFromPointers(commandables);
        byte[] cTargetData = target != null ? target.GetData() : new byte[0];
        
        if(geoTarget == null)
            geoTarget = new GeoCoord();
        
        ByteBuffer bb = ByteBuffer.allocate(19 + cCommandablesData.length + cTargetData.length);
        bb.put((byte)command.ordinal());
        bb.put(cCommandablesData);
        bb.put((byte)(target != null ? 0xFF : 0x00));
        bb.put(cTargetData);
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.put((byte)typeToDeliver.ordinal());
        bb.putInt(lDeliverID);
        bb.putInt(lQuantityToDeliver);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.UnitCommand, cData);
    }
}
