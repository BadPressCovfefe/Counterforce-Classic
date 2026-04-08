/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.game.EntityPointer.EntityType;
import launch.utilities.LongDelay;

/**
 *
 * @author Corbin
 */
public class ShipProductionOrder
{
    /**
     * A production order for a ship in a shipyard.
     */
    private static final int DATA_SIZE = 15;
    
    private int lProducingForID;
    private LongDelay dlyProductionTime;
    private EntityType typeUnderConstruction;
    private boolean bCompleted = false;
    
    public ShipProductionOrder(int lProducingForID, long lBuildTimeRemaining, EntityType typeToBuild)
    {
        this.lProducingForID = lProducingForID;
        this.dlyProductionTime = new LongDelay(lBuildTimeRemaining);
        this.typeUnderConstruction = typeToBuild;
    }
    
    public ShipProductionOrder(ByteBuffer bb)
    {
        bCompleted = (bb.get() != 0x00);
        lProducingForID = bb.getInt();
        dlyProductionTime = new LongDelay(bb.getLong());
        typeUnderConstruction = EntityType.values()[bb.get()];
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        bb.put((byte)(bCompleted? 0xFF : 0x00));
        bb.putInt(lProducingForID);
        bb.putLong(dlyProductionTime.GetRemaining());
        bb.put((byte)typeUnderConstruction.ordinal());

        return bb.array();
    }
    
    public void Tick(int lMS)
    {
        dlyProductionTime.Tick(lMS);
    }
    
    public int GetProducingForID()
    {
        return this.lProducingForID;
    }
    
    public long GetConstructionTimeRemaining()
    {
        return dlyProductionTime.GetRemaining();
    }
    
    public EntityType GetTypeUnderConstruction()
    {
        return this.typeUnderConstruction;
    }
    
    public boolean Finished()
    {
        return this.dlyProductionTime.Expired();
    }
    
    public void SetCompleted()
    {
        this.bCompleted = true;
    }
    
    public boolean Completed()
    {
        return this.bCompleted;
    }
    
    public void SetProducingForID(int lNewOwner)
    {
        this.lProducingForID = lNewOwner;
    }
}
