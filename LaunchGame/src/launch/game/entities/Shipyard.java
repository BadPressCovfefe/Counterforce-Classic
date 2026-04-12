/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.ShipProductionOrder;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;


/**
 *
 * @author Corbin
 */
public class Shipyard extends Capturable implements LaunchSystemListener
{
    public static final int DATA_SIZE = 9;
    
    private GeoCoord geoOutput;
    
    private List<ShipProductionOrder> Queue = new ArrayList<>();
    private byte cProductionCapacity;
    
    
    /** New. */
    public Shipyard(int lID, GeoCoord geoPosition, String strName, GeoCoord geoOutput, short nHP, short nMaxHP)
    {
        super(lID, geoPosition, strName, nHP, nMaxHP, LaunchEntity.ID_NONE, false);
        this.geoOutput = geoOutput;
        this.cProductionCapacity = 1;
    }
    
    /** From save. */
    public Shipyard(int lID, String strName, GeoCoord geoPosition, GeoCoord geoOutput, int lOwnerID, short nHP, short nMaxHP, boolean bContested, byte cCapacity, List<ShipProductionOrder> Queue)
    {
        super(lID, geoPosition, strName, nHP, nMaxHP, lOwnerID, bContested);
        this.geoOutput = geoOutput;
        this.cProductionCapacity = cCapacity;
        this.Queue = Queue;
    }
    
    /** From comms. */
    public Shipyard(ByteBuffer bb, int lReceivingID)
    {
        super(bb);
        geoOutput = new GeoCoord(bb.getFloat(), bb.getFloat());
        cProductionCapacity = bb.get();
        Queue = QueueFromData(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        for(ShipProductionOrder order : new ArrayList<>(Queue))
        {
            order.Tick(lMS);
        
            if(order.Completed())
            {
                Queue.remove(order);
                Changed(false);
            }
        }
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cQueueData = GetQueueData();

        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE + cQueueData.length);
        bb.put(cBaseData);
        bb.putFloat(geoOutput.GetLatitude());
        bb.putFloat(geoOutput.GetLongitude());
        bb.put(cProductionCapacity);
        bb.put(cQueueData);

        return bb.array();
    }
    
    public static List<ShipProductionOrder> QueueFromData(ByteBuffer bb)
    {
        List<ShipProductionOrder> Result = new ArrayList<>();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            ShipProductionOrder order = new ShipProductionOrder(bb);
            Result.add(order);
        }
        
        return Result;
    }
    
    public byte[] GetQueueData()
    {
        ByteBuffer bb = ByteBuffer.allocate(GetQueueDataSize());
        bb.putShort((short)Queue.size());
        
        for(ShipProductionOrder order : Queue)
        {
            bb.put(order.GetData());
        }
        
        return bb.array();
    }
    
    public short GetQueueDataSize()
    {
        short nDataSize = 0;
        
        nDataSize += Short.BYTES;
        
        for(ShipProductionOrder order : Queue)
        {
            nDataSize += order.GetData().length;
        }
        
        return nDataSize;
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Shipyard)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public String GetTypeName()
    {
        return "shipyard";
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.SHIPYARD;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Shipyard;
    }
    
    public GeoCoord GetOutputCoord()
    {
        return geoOutput;
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public void Abandon()
    {
        super.Abandon();
    }
    
    public Collection<ShipProductionOrder> GetQueue()
    {
        return new ArrayList<>(Queue);
    }
    
    public byte GetProductionCapacity()
    {
        return cProductionCapacity;
    }
    
    public boolean UpgradeProductionCapacity()
    {
        if(cProductionCapacity < Defs.MAX_SHIPYARD_CAPACITY)
        {
            cProductionCapacity++;
            return true;
        }
        
        return false;
    }
    
    public boolean AddProductionOrder(ShipProductionOrder order)
    {
        if(Queue.size() < cProductionCapacity)
        {
            Queue.add(order);
            return true;
        }
        
        return false;
    }
    
    public void ResetShipyard()
    {
        Queue.clear();
        cProductionCapacity = 1;
    }
    
    public boolean FullyUpgraded()
    {
        return cProductionCapacity == Defs.MAX_SHIPYARD_CAPACITY;
    }
    
    public boolean HasCapacityRemaining()
    {
        return Queue.size() < cProductionCapacity;
    }
    
    public boolean GetProducing()
    {
        return !Queue.isEmpty();
    }
    
    public byte GetRemainingCapacity()
    {
        return (byte)(cProductionCapacity - Queue.size());
    }
}
