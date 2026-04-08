/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author Corbin
 */
public class TransferCargoTask extends Task
{
    public TransferCargoTask(LaunchClientGameInterface gameInterface, EntityPointer entityFrom, EntityPointer entityTo, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver, boolean bLoadAsCargo, boolean bFromCargo)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cEntityFromData = entityFrom != null ? entityFrom.GetData() : new byte[0];
        byte[] cEntityToData = entityTo != null ? entityTo.GetData() : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(13 + cEntityFromData.length + cEntityToData.length + (typeToDeliver != null ? 1 : 0));
        bb.put((byte)(entityFrom != null ? 0xFF : 0x00));
        bb.put(cEntityFromData);
        bb.put((byte)(entityTo != null ? 0xFF : 0x00));
        bb.put(cEntityToData);
        bb.put((byte)(typeToDeliver != null ? 0xFF : 0x00));
        if(typeToDeliver != null)
            bb.put((byte)typeToDeliver.ordinal());
        bb.putInt(lDeliverID);
        bb.putInt(lQuantityToDeliver);
        bb.put((byte)(bLoadAsCargo ? 0xFF : 0x00));
        bb.put((byte)(bFromCargo ? 0xFF : 0x00));
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.TransferCargo, cData);
    }
}
