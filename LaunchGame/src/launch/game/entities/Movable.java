/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.EntityPointer;
import launch.game.systems.CargoSystem.LootType;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public abstract class Movable extends Damagable
{
    public enum MoveOrders
    {
        MOVE,
        RETURN,
        WAIT,         //This unit is waiting, but is ready to move.
        ATTACK,
        SEEK_FUEL,    //This unit is approaching another aircraft for fuel.
        PROVIDE_FUEL, //This unit is refueling another unit. 
        CAPTURE,      //This infantry unit is going to capture a structure or other entity.
        NONE,
        TURN,         //Turn the infantry to face a new direction.
        LOAD,         //This cargo truck is going to pick up loot off the ground.
        UNLOAD,       //This cargo truck/aircraft is going to unload loot somewhere.
        DEFEND,       //This landunit is dug in to defend.
        LIBERATE,
    }
    
    public enum MovableType
    {
        AIRCRAFT,
        INFANTRY,
        TANK,
        CARGO_TRUCK,
        SHIP,
        SUBMARINE,
    }
    
    private static final int DATA_SIZE = 7;
    
    protected MoveOrders moveOrders;
    protected GeoCoord geoTarget;
    protected EntityPointer target;
    
    /**
     * The keys in this map are the orders of the coordinates. The first coordinate is one, the second is two, etc. 
     * Every time a movable hits the next coordinate, it is removed from the map. Thus the size of the map is decreasing while
     * the highest key stays the same. This leads to interesting behavior when handled by a for-loop. See LaunchUtilities.GetTotalTravelDistance
     * for some more info on this.
     */
    protected Map<Integer, GeoCoord> Coordinates = new ConcurrentHashMap<>();
    
    /** New. */
    public Movable(int lID, GeoCoord geoPosition, short nHP, short nMaxHP)
    {
        super(lID, geoPosition, nHP, nMaxHP, false, 0);
        this.moveOrders = MoveOrders.WAIT;
        this.geoTarget = new GeoCoord();
        this.Coordinates = new ConcurrentHashMap<>();
    }
    
    /** From save. */
    public Movable(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, MoveOrders moveOrders, GeoCoord geoTarget, EntityPointer target, boolean bVisible, int lVisibleTime, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, bVisible, lVisibleTime);
        this.moveOrders = moveOrders;
        this.geoTarget = geoTarget;
        this.target = target;
        this.Coordinates = Coordinates == null ? new ConcurrentHashMap<>() : Coordinates;
    }
    
    public Movable(ByteBuffer bb)
    {
        super(bb);
        moveOrders = MoveOrders.values()[bb.get()];  
        
        if((bb.get() != 0x00))
            geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
        else
            geoTarget = null;
        
        geoPosition.SetLastBearing(bb.getFloat());
        
        if((bb.get() != 0x00))
            target = new EntityPointer(bb);
        
        Coordinates = LaunchUtilities.GeoCoordMapFromData(bb);
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cTargetData = target != null ? target.GetData() : new byte[0];
        byte[] cCoordinateData = LaunchUtilities.BBFromGeoCoords(Coordinates);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + (geoTarget != null ? 8 : 0) + cBaseData.length + cTargetData.length + cCoordinateData.length);
        bb.put(cBaseData);
        bb.put((byte)moveOrders.ordinal());
        
        bb.put((byte)(geoTarget != null? 0xFF : 0x00));
        
        if(geoTarget != null)
        {
            bb.putFloat(geoTarget.GetLatitude());
            bb.putFloat(geoTarget.GetLongitude());
        } 
        
        bb.putFloat(geoPosition.GetLastBearing());
        bb.put((byte)(target != null? 0xFF : 0x00));
        bb.put(cTargetData);
        bb.put(cCoordinateData);
        
        return bb.array();
    }
    
    public MoveOrders GetMoveOrders()
    {
        return this.moveOrders;
    }
    
    public GeoCoord GetGeoTarget()
    {
        if(HasGeoTarget())
        {
            return this.geoTarget;
        }
        else if(HasGeoCoordChain())
        {
            return GetNextCoordinate();
        }
        
        return null;
    }
    
    public boolean HasGeoTarget()
    {
        return this.geoTarget != null;
    }
    
    public void ReachedTarget()
    {
        this.geoTarget = new GeoCoord();
        this.moveOrders = MoveOrders.WAIT;
        Changed(false);
    }
            
    public void MoveToPosition(GeoCoord newTarget)
    {
        if(this.target == null || this.moveOrders != MoveOrders.PROVIDE_FUEL)
            this.moveOrders = MoveOrders.MOVE;
        
        this.target = null;
        this.geoTarget = newTarget != null ? newTarget.GetCopy() : newTarget;
        this.Coordinates.clear();
        Changed(false);
    }
    
    public void MoveToPositions(Map<Integer, GeoCoord> Coordinates)
    {
        this.moveOrders = MoveOrders.MOVE;
        
        this.target = null;
        this.geoTarget = null;
        this.Coordinates = new ConcurrentHashMap<>(Coordinates);
        Changed(false);
    }
    
    public boolean HasGeoCoordChain()
    {
        return !this.Coordinates.isEmpty();
    }
    
    public Map<Integer, GeoCoord> GetCoordinates()
    {
        return new ConcurrentHashMap<>(this.Coordinates);
    }
    
    public void ReachedFirstCoordinate()
    {
        if(HasGeoCoordChain())
        {
            this.Coordinates.remove(LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()));
        }
    }
    
    public GeoCoord GetNextCoordinate()
    {
        if(HasGeoCoordChain())
        {
            return this.Coordinates.get(LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()));
        }
        
        return null;
    }
    
    public GeoCoord GetLastCoordinate()
    {
        if(HasGeoCoordChain())
        {
            return this.Coordinates.get(LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()));
        }
        
        return null;
    }
    
    public void Wait()
    {
        this.moveOrders = MoveOrders.WAIT;
        this.Coordinates.clear();
        this.geoTarget = geoPosition.GetCopy();
        Changed(false);
    }
    
    public void Resume()
    {
        this.moveOrders = MoveOrders.MOVE;
        Changed(false);
    }
    
    public EntityPointer GetTarget()
    {
        return this.target;
    }
    
    public boolean HasTarget()
    {
        return this.target != null;
    }
    
    public abstract void AttackTarget(EntityPointer target);
    
    public abstract void AttackTarget(GeoCoord geoTarget);
    
    public abstract void SeekRefueling(EntityPointer tanker);
    
    public abstract void ProvideRefueling(EntityPointer refuelee);
        
    public abstract void CaptureTarget(EntityPointer target);
    
    public abstract void LiberateTarget(EntityPointer target);
    
    public abstract void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver);
    
    public abstract void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver);
    
    public abstract void LoadLoot(EntityPointer loot);
}
