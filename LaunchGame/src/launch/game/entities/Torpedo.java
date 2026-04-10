/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;

/**
 *
 * @author tobster
 */
public class Torpedo extends MapEntity
{
    private static final int DATA_SIZE = 31;
    
    /**
     * TorpedoState is used in LaunchServerGame.ProcessTorpedoes() to control torpedo behavior.
     */
    public enum TorpedoState
    {
        TRAVELLING, //The torpedo is moving to its geoTarget.
        SEEKING,    //The torpedo is searching for a new target, circling its geoTarget whil doing so.
        HOMING,     //The torpedo has found a new target and is now heading towards it. 
        DETONATE,   //Obvious.
    }
    
    private int lType;
    private int lOwnerID;
    private boolean bHoming;
    private float fltRange;
    private float fltDistanceTraveled;
    private GeoCoord geoTarget;
    private EntityPointer target;
    private TorpedoState torpedoState;
    
    //"Threatens player" optimisations. Server only.
    private List<Integer> ThreatenedPlayers = new ArrayList<>();
    
    public Torpedo(int lID, GeoCoord geoPosition, int lType, int lOwnerID, boolean bHoming, float fltRange, float fltDistanceTraveled, GeoCoord geoTarget, EntityPointer target, byte cState)
    {
        super(lID, geoPosition, true, 0);
        this.lType = lType;
        this.lOwnerID = lOwnerID;
        this.bHoming = bHoming;
        this.fltRange = fltRange;
        this.fltDistanceTraveled = fltDistanceTraveled;
        this.geoTarget = geoTarget;
        this.target = target;
        this.torpedoState = TorpedoState.values()[cState];
    }
    
    public Torpedo(ByteBuffer bb)
    {
        super(bb);
        geoPosition.SetLastBearing(bb.getFloat());
        lType = bb.getInt();
        lOwnerID = bb.getInt();
        bHoming = (bb.get() != 0x00);
        fltRange = bb.getFloat();
        fltDistanceTraveled = bb.getFloat();
        geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
        torpedoState = TorpedoState.values()[bb.get()];
        if(bb.get() != 0x00)
            target = new EntityPointer(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        //Nothing to do here.
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cTargetData = target != null ? target.GetData() : new byte[0];
            
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cTargetData.length);
        
        bb.put(cBaseData);
        bb.putFloat(geoPosition.GetLastBearing());
        bb.putInt(lType);
        bb.putInt(lOwnerID);
        bb.put((byte)(bHoming ? 0xFF : 0x00));
        bb.putFloat(fltRange);
        bb.putFloat(fltDistanceTraveled);
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.put((byte)torpedoState.ordinal());
        bb.put((byte)(target != null ? 0xFF : 0x00));
        if(target != null)
            bb.put(target.GetData());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public int GetType() { return lType; }
    
    @Override
    public int GetOwnerID() { return lOwnerID; }
    
    public boolean GetHoming() { return bHoming; }
    
    public boolean HasGeoTarget()
    {
        return geoTarget != null;
    }
    
    public boolean HasTarget()
    {
        return target != null;
    }
    
    public boolean OutOfRange()
    {
        return fltDistanceTraveled >= fltRange;
    }
    
    public TorpedoState GetState()
    {
        return torpedoState;
    }
    
    public void ReachedGeoTarget()
    {
        if(bHoming)
            this.torpedoState = TorpedoState.SEEKING;
        else
            this.torpedoState = TorpedoState.DETONATE;
    }
    
    public void TargetLocated(EntityPointer ptrTarget)
    {
        if(bHoming)
        {
            target = ptrTarget;
            this.torpedoState = TorpedoState.HOMING;
            Changed(false);
        }
    }
    
    /**
     * Get the target location of this missile. WARNING: This won't actually be where the missile's heading if it's tracking a player! Callers must check this!
     * @return The missile's target location, or gibberish if it's player tracking.
     */
    public GeoCoord GetGeoTarget() { return geoTarget; }
    
    public EntityPointer GetTarget() { return target; }
    
    public void LostTarget()
    {
        geoTarget = geoPosition;
    }
    
    public boolean GetDetonate()
    {
        return torpedoState == TorpedoState.DETONATE;
    }
    
    public void SetDetonate()
    {
        torpedoState = TorpedoState.DETONATE;
        Changed(false);
    }
    
    public void ClearStructureThreatenedPlayers()
    {
        ThreatenedPlayers.clear();
    }
    
    public void AddStructureThreatenedPlayer(int lPlayerID)
    {
        ThreatenedPlayers.add(lPlayerID);
    }
    
    public List<Integer> GetStructureThreatenedPlayers()
    {
        return ThreatenedPlayers;
    }
    
    public boolean ThreatensPlayersStructures(int lPlayerID)
    {
        return ThreatenedPlayers.contains(lPlayerID);
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Torpedo)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.TORPEDO;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Torpedo;
    }
    
    public float GetRange()
    {
        return fltRange;
    }
    
    public float GetDistanceTraveled()
    {
        return fltDistanceTraveled;
    }
    
    public void Traveled(float fltDistance)
    {
        this.fltDistanceTraveled += fltDistance;
    }
    
    @Override
    public String GetTypeName()
    {
        return "torpedo";
    }
}
