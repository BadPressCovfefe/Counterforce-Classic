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
public class TransferLandUnitTask extends Task
{
    public TransferLandUnitTask(LaunchClientGameInterface gameInterface, EntityPointer sender, EntityPointer receiver, EntityPointer unit)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(15);
        bb.put(sender.GetData());
        bb.put(receiver.GetData());
        bb.put(unit.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.TransferLandUnit, cData);
    }
}
