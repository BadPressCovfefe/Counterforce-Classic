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
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author tobster
 */
public class Waypoint extends MapEntity
{
    private static final int DATA_SIZE = 13;
    
    public enum Intention
    {
        ATTACK,
        DEFEND,
        INFO,
    }
    
    private Intention intention;
    private ShortDelay dlyExpiry;
    private String strName;
    private int lOwnerID;
    
    //Flags (not transmitted).
    private boolean bCollected = false;     //Indicates the loot has been collected and may be cleared up.
    
    public Waypoint(int lID, GeoCoord geoPosition, int lOwnerID, String strName, byte cIntention, int lExpiry)
    {
        super(lID, geoPosition, true, 0);
        this.intention = Intention.values()[cIntention];
        this.dlyExpiry = new ShortDelay(lExpiry);
        this.strName = strName;
        this.lOwnerID = lOwnerID;
    }
    
    public Waypoint(ByteBuffer bb)
    {
        super(bb);
        intention = Intention.values()[bb.get()];
        dlyExpiry = new ShortDelay(bb.getInt());
        //TODO
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
        //TODO
        dlyExpiry.GetData(bb);
        
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
        
    /**
     * Mark the loot as collected, such that it will be removed from the game during the next tick.
     */
    public void Collect() { bCollected = true; }
    
    public boolean Collected() { return bCollected; }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        //Everyone owns money.
        return true;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Waypoint)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.LOOT; //TODO
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Loot; //TODO
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    @Override
    public String GetTypeName()
    {
        return "waypoint";
    }
}
