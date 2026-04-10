/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Airdrop extends MapEntity 
{
    private static final int DATA_SIZE = 8;
    
    private int lOwnerID;
    private ShortDelay dlyArrival;
    
    boolean bArrived = false; //Server-only indicator that the airdrop has arrived and has been removed.
    
    public Airdrop(int lID, int lOwnerID, GeoCoord geoLocation, int lArriveTime)
    {
        super(lID, geoLocation, true, 0);
        this.lOwnerID = lOwnerID;
        this.dlyArrival = new ShortDelay(lArriveTime);
    }
    
    public Airdrop(ByteBuffer bb)
    {
        super(bb);
        this.lOwnerID = bb.getInt();
        this.dlyArrival = new ShortDelay(bb.getInt());
    }
    
    @Override
    public int GetOwnerID()
    {
        return this.lOwnerID;
    }
    
    public boolean Arrived()
    {
        return bArrived;
    }
    
    public int GetArrivalRemaining()
    {
        return this.dlyArrival.GetRemaining();
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.putInt(lOwnerID);
        bb.putInt(dlyArrival.GetRemaining());
        return bb.array();
    } 
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public void Tick(int lMS)
    {
        dlyArrival.Tick(lMS);
        
        if(dlyArrival.Expired() && !bArrived)
            bArrived = true;
    }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lOwnerID == lID;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.AIRDROP;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        return entity instanceof Airdrop && entity.GetID() == this.lID;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Airdrop;
    }
    
    @Override
    public String GetTypeName()
    {
        return "airdrop";
    }
}
