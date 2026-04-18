/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.entities.LaunchEntity;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.Haulable;
import launch.game.entities.Infantry;
import launch.game.entities.InfantryInterface;
import launch.game.entities.NamableInterface;
import launch.game.entities.ResourceInterface;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.ResourceSystem;


public class StoredInfantry extends StoredDamagable implements InfantryInterface, NamableInterface, Haulable, ResourceInterface
{
    private static final int DATA_SIZE = 9;
    
    private EntityPointer host;
    private ResourceSystem resources;
    private float fltFuel;
    
    //From save.
    public StoredInfantry(int lID, short nHP, short nMaxHP, int lOwnerID, String strName, int lPrepRemaining, EntityPointer host, ResourceSystem resources, float fltFuel)
    {
        super(lID, lOwnerID, nHP, nMaxHP, lPrepRemaining, strName);
        this.host = host;
        this.fltFuel = fltFuel;
        this.resources = resources;
    }
    
    //Convert infantry to stored infantry.
    public StoredInfantry(int lNewID, Infantry infantry, EntityPointer host)
    {
        super(lNewID, infantry.GetOwnerID(), infantry.GetHP(), infantry.GetMaxHP(), 0, infantry.GetName());
        this.host = host;
        this.fltFuel = infantry.GetCurrentFuel();
        this.resources = infantry.GetResourceSystem();
    }
    
    //From comms.
    public StoredInfantry(ByteBuffer bb)
    {
        super(bb);
        host = new EntityPointer(bb);
        resources = new ResourceSystem(bb);
        fltFuel = bb.getFloat();
    }
    
    @Override
    public void Tick(int lMS)
    {
        
    }
    
    /**
     * Get data to communicate, base class. Subclasses should override and super call this method.
     * @param lAskingID The ID of the player this data will be sent to.
     * @return The data to communicate.
     */
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cResourceData = resources.GetData();
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cResourceData.length);
        
        bb.put(cBaseData);
        bb.put(host.GetData());
        bb.put(cResourceData);
        bb.putFloat(fltFuel);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof StoredInfantry)
            return entity.GetID() == lID;
        
        return false;
    }
    
    public StoredInfantry ReIDAndReturnSelf(int lNewID)
    {
        this.lID = lNewID;
        return this;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.STORED_INFANTRY;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.StoredInfantry;
    }
    
    @Override
    public long GetWeight()
    {
        return 1;
    }
    
    @Override
    public long GetStoredWeight()
    {
        return GetWeight();
    }
    
    @Override
    public LaunchEntity GetInfantry()
    {
        return this;
    }
    
    @Override
    public boolean Deployed()
    {
        return false;
    }
    
    @Override
    public long GetQuantity()
    {
        return 1; //StoredInfantry are not stackable in cargo systems.
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.STORED_INFANTRY;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    @Override
    public EntityPointer GetHost()
    {
        return this.host;
    }
    
    public void SetHost(EntityPointer newHost)
    {
        this.host = newHost;
    }
    
    @Override
    public String GetTypeName()
    {
        return "stored infantry";
    }
    
    public boolean Capture(int lPlayerID)
    {
        if(lOwnerID != lPlayerID)
        {
            lOwnerID = lPlayerID;
            Changed(false);
            return true;
        }
        
        return false;
    }
    
    public float GetCurrentFuel()
    {
        return this.fltFuel;
    }
    
    @Override
    public ResourceSystem GetResourceSystem()
    {
        return resources;
    }
}
