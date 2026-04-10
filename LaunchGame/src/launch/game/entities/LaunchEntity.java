/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;

/**
 *
 * @author tobster
 */
public abstract class LaunchEntity
{    
    private static final int DATA_SIZE = 4;
    public static final int ID_NONE = Defs.THE_GREAT_BIG_NOTHING;
    protected boolean bHasFullStats;
    
    private LaunchEntityListener listener = null;
    
    protected int lID;
    
    //Not transmitted. Assigned on instantiation.
    protected EntityPointer pointer;
    
    public LaunchEntity(int lID)
    {
        this.lID = lID;
        SetPointer();
    }
    
    public LaunchEntity(ByteBuffer bb)
    {
        this.lID = bb.getInt();
        SetPointer();
    }
    
    public abstract void Tick(int lMS);
    
    /**
     * Get data to communicate, base class. Subclasses should override and super call this method.
     * @param lAskingID The ID of the player this data will be sent to.
     * @return The data to communicate.
     */
    public byte[] GetData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        bb.putInt(lID);
        return bb.array();
    }
    
    public abstract byte[] GetFullStatsData(int lAskingID);
    
    public int GetID() { return lID; }
    
    public void SetListener(LaunchEntityListener listener) { this.listener = listener; }
    
    /**
     * Call when something changes that should be communicated to the players.
     * @param bOwner Whatever changed is only relevant to the player that owns this entity. 
     */
    protected final void Changed(boolean bOwner)
    {
        if(listener != null)
        {
            listener.EntityChanged(this, bOwner);
        }
    }
    
    public abstract boolean GetOwnedBy(int lID);
    
    /**
     * For checking if an entity references the same conceptual entity.For inspecting entity updates etc. DO NOT USE for containers/memory management (memory-referential equality)!
     * @param entity The entity to check.
     * @return Whether it appears to reference the same entity.
     */
    
    public abstract boolean ApparentlyEquals(LaunchEntity entity);
    
    public abstract EntityType GetEntityType();
    
    public void SetPointer()
    {
        this.pointer = new EntityPointer(lID, GetEntityType());
    }
    
    public EntityPointer GetPointer()
    {
        if(pointer != null)
        {
            return pointer;
        }
        else
        {
            SetPointer();
            return pointer;
        }
    }
    
    public abstract int GetSessionCode();
    
    public abstract int GetOwnerID();
    
    public abstract String GetTypeName();
    
    public boolean GetHasFullStats()
    {
        return bHasFullStats;
    }
}
