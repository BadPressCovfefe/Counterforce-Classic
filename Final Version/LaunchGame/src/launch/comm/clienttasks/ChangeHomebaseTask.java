/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;


/**
 *
 * @author tobster
 */
public class ChangeHomebaseTask extends Task
{
    public ChangeHomebaseTask(LaunchClientGameInterface gameInterface, EntityPointer entity, EntityPointer homebase)
    {
        super(gameInterface);
        
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put(entity.GetData());
        bb.put(homebase.GetData());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.ChangeAircraftHomebase, cData);
    }
}
