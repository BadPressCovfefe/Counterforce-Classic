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
public class DropLandUnitTask extends Task
{
    public DropLandUnitTask(LaunchClientGameInterface gameInterface, EntityPointer unit)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cUnitData = unit != null ? unit.GetData() : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(cUnitData.length);
        bb.put(cUnitData);
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.DropLandUnit, cData);
    }
}
