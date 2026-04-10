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
public class ListOnMarketTask extends Task
{
    public ListOnMarketTask(LaunchClientGameInterface gameInterface, int lMarketID, EntityPointer pointerDeliverer, LootType typeOfListing, int lTypeID, int lQuantity, float fltPriceEach)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        
        byte[] cDelivererData = pointerDeliverer.GetData();
        
        ByteBuffer bb = ByteBuffer.allocate(17 + cDelivererData.length);
        bb.putInt(lMarketID);
        bb.put(cDelivererData);
        bb.put((byte)typeOfListing.ordinal());
        bb.putInt(lTypeID);
        bb.putInt(lQuantity);
        bb.putFloat(fltPriceEach);
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        comm.SendObject(LaunchSession.ListOnMarket, cData);
    }
}
