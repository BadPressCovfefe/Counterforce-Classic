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
public class Missile extends MapEntity
{
    private static final int DATA_SIZE = 32;
    
    private int lType;
    private int lOwnerID;
    private GeoCoord geoTarget;
    private GeoCoord geoOrigin;
    private EntityPointer target;
    private boolean bAirburst;
    protected float fltSpeed;
    private boolean bFlying = true;         //Indicates the missile has not reached its target or been shot down.
    private boolean bDetonate = false;
    
    //"Threatens player" optimisations. Server only.
    private List<Integer> StructureThreatenedPlayers = new ArrayList<>();
    
    //Client-only. Used to tell if a missile marker should render with a threat overlay.
    private boolean bThreatensUs = false;
    private boolean bThreatensAllies = false;
    
    public Missile(int lID, GeoCoord geoPosition, int lType, int lOwnerID, float fltSpeed, GeoCoord geoTarget, EntityPointer target, GeoCoord geoOrigin, boolean bAirburst)
    {
        super(lID, geoPosition, true, 0);
        this.lType = lType;
        this.lOwnerID = lOwnerID;
        this.geoTarget = geoTarget;
        this.geoOrigin = geoOrigin;
        this.bAirburst = bAirburst;
        this.target = target;
        this.fltSpeed = fltSpeed;
    }
    
    public Missile(ByteBuffer bb)
    {
        super(bb);
        lType = bb.getInt();
        fltSpeed = bb.getFloat();
        lOwnerID = bb.getInt();
        geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
        geoOrigin = new GeoCoord(bb.getFloat(), bb.getFloat());
        bAirburst = (bb.get() != 0x00);
        bFlying = (bb.get() != 0x00);
        bDetonate = (bb.get() != 0x00);
        
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
        bb.putInt(lType);
        bb.putFloat(fltSpeed);
        bb.putInt(lOwnerID);
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.putFloat(geoOrigin.GetLatitude());
        bb.putFloat(geoOrigin.GetLongitude());
        bb.put((byte)(bAirburst ? 0xFF : 0x00));
        bb.put((byte)(bFlying ? 0xFF : 0x00));
        bb.put((byte)(bDetonate ? 0xFF : 0x00));
        
        bb.put((byte)(target != null ? 0xFF : 0x00));
        bb.put(cTargetData);
        
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
    
    public boolean GetTracking() { return target != null; }
    
    /**
     * Get the target location of this missile. WARNING: This won't actually be where the missile's heading if it's tracking a player! Callers must check this!
     * @return The missile's target location, or gibberish if it's player tracking.
     */
    public GeoCoord GetTarget() { return geoTarget; }
    
    public GeoCoord GetOrigin() { return geoOrigin; }
    
    public EntityPointer GetTargetEntity() { return target; }
    
    public boolean Flying()
    {
        return bFlying;
    }
    
    public boolean GetDetonate()
    {
        return bDetonate;
    }
    
    public void Detonate()
    {
        bDetonate = true;
    }
    
    public void Destroy()
    {
        bFlying = false;
    }
    
    public void SelfDestruct()
    {
        //To be used to self-destruct missiles to prevent an error only. Will cause an explosion. Don't give this power to players.
        target = null;
        geoTarget = geoPosition;
        Destroy();
    }
    
    public void ClearStructureThreatenedPlayers()
    {
        StructureThreatenedPlayers.clear();
    }
    
    public void AddStructureThreatenedPlayer(int lPlayerID)
    {
        StructureThreatenedPlayers.add(lPlayerID);
    }
    
    public List<Integer> GetStructureThreatenedPlayers()
    {
        return StructureThreatenedPlayers;
    }
    
    public boolean ThreatensPlayersStructures(int lPlayerID)
    {
        return StructureThreatenedPlayers.contains(lPlayerID);
    }
    
    public boolean GetAirburst()
    {
        return this.bAirburst;
    }
    
    public float GetSpeed()
    {
        return this.fltSpeed;
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Missile)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.MISSILE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Missile;
    }
    
    @Override
    public String GetTypeName()
    {
        return "missile";
    }
    
    public void SetThreatensUs()
    {
        this.bThreatensUs = true;
    }
    
    public void SetThreatensAllies()
    {
        this.bThreatensAllies = true;
    }
    
    public boolean GetThreatensUs()
    {
        return this.bThreatensUs;
    }
    
    public boolean GetThreatensAllies()
    {
        return this.bThreatensAllies;
    }
}
