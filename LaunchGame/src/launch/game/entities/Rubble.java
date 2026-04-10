/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.GeoCoord;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource.ResourceType;

/**
 *
 * @author tobster
 */
public class Rubble extends MapEntity
{
    private static final int DATA_SIZE = 10;
    
    private int lOwnerID;
    private EntityType structureType;
    private ResourceType resourceType;                                                  //Only used for processor rubble to tell what type of processor it is.
    private ShortDelay dlyExpiry;
    
    private boolean bRemove = false; //Server only. Indicates the rubble is ready to remove.
    
    public Rubble(int lID, GeoCoord geoPosition, EntityType structureType, ResourceType resourceType, int lOwnerID, int lExpiry)
    {
        super(lID, geoPosition, true, 0);
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.structureType = structureType;
        this.resourceType = resourceType;
        this.lOwnerID = lOwnerID;
    }
    
    public Rubble(ByteBuffer bb)
    {
        super(bb);
        structureType = EntityType.values()[bb.get()];
        resourceType = ResourceType.values()[bb.get()];
        dlyExpiry = new ShortDelay(bb);
        lOwnerID = bb.getInt();
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
        bb.put((byte)structureType.ordinal());
        bb.put((byte)resourceType.ordinal());
        bb.putInt(dlyExpiry.GetRemaining());
        bb.putInt(lOwnerID);
        
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
    
    public int GetExpiryRemaining() { return dlyExpiry.GetRemaining(); }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Rubble)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.RUBBLE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Rubble;
    }
    
    public void SetExpiry(int lExpiry)
    {
        this.dlyExpiry.Set(lExpiry);
        Changed(true);
    }
    
    @Override
    public int GetOwnerID()
    {
        return lOwnerID;
    }
    
    @Override
    public String GetTypeName()
    {
        return "rubble";
    }
    
    public EntityType GetStructureType()
    {
        return structureType;
    }
    
    public ResourceType GetResourceType()
    {
        return resourceType;
    }
    
    public void SetRemove()
    {
        this.bRemove = true;
    }
    
    public boolean GetRemove()
    {
        return this.bRemove;
    }
}
