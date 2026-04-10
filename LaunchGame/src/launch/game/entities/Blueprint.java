/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Blueprint extends MapEntity
{
    private static final int DATA_SIZE = 10;
    private EntityType type;
    private ResourceType resourceType;
    private ShortDelay dlyExpiry;
    private int lOwnerID;
    
    public Blueprint(int lID, GeoCoord geoPosition, int lExpiry, int lOwnerID, EntityType type, ResourceType resourceType)
    {
        super(lID, geoPosition, true, 0);
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.bVisible = true;
        this.lOwnerID = lOwnerID;
        this.type = type;
        this.resourceType = resourceType;
    }
    
    public Blueprint(ByteBuffer bb)
    {
        super(bb);
        type = EntityType.values()[bb.get()];
        dlyExpiry = new ShortDelay(bb);
        lOwnerID = bb.getInt();
        resourceType = ResourceType.values()[bb.get()];
    }

    @Override
    public void Tick(int lMS)
    {
        dlyExpiry.Tick(lMS);
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte cBaseData[] = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.put((byte) type.ordinal());
        dlyExpiry.GetData(bb);
        bb.putInt(lOwnerID);
        bb.put((byte)resourceType.ordinal());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public boolean Expired()
    {
        return dlyExpiry.Expired();
    }
    
    public int GetExpiryRemaining() 
    { 
        return dlyExpiry.GetRemaining(); 
    }
    
    public void SetRemove()
    {
        dlyExpiry.Set(0);
    }
    
    @Override
    public int GetOwnerID()
    {
        return lOwnerID;
    }
    
    public EntityType GetType()
    {
        return this.type;
    }
    
    public ResourceType GetResourceType()
    {
        return this.resourceType;
    }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return this.lOwnerID == lID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Blueprint)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.BLUEPRINT;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Blueprint;
    }
    
    @Override
    public String GetTypeName()
    {
        return "blueprint";
    }
}
