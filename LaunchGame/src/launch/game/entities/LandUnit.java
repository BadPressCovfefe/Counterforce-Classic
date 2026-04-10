/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import java.util.Map;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

/**
 *
 * @author tobster
 */
public abstract class LandUnit extends Movable implements FuelableInterface, ResourceInterface
{
    private static final int DATA_SIZE = 9;
    
    private String strName;
    protected int lOwnerID;
    private ShortDelay dlyUnderAttack;
    private boolean bOnWater;
    private ResourceSystem resources;
    private float fltCurrentFuel;
    
    private boolean bRemove = false; //Server-only. Used to indicate that a land unit has been sold and needs to be removed. At this point, only used by trucks and tanks.
    
    /** New. */
    public LandUnit(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP);
        this.strName = "";
        this.lOwnerID = lOwnerID;
        dlyUnderAttack = new ShortDelay(0);
        this.resources = resources;
        this.fltCurrentFuel = 1.0f;
    }
    
    /** From save. */
    public LandUnit(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, int lUnderAttack, MoveOrders moveOrders, GeoCoord geoTarget, EntityPointer target, boolean bVisible, int lVisibleTime, ResourceSystem resources, float fltCurrentFuel, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, moveOrders, geoTarget, target, bVisible, lVisibleTime, Coordinates);
        this.strName = strName;
        this.lOwnerID = lOwnerID;
        dlyUnderAttack = new ShortDelay(lUnderAttack);
        this.resources = resources;
        this.fltCurrentFuel = fltCurrentFuel;
    }
    
    /**
     * A backup feature that allows a structure from a seperate game snapshot to be re-ID'd for incorporation into a different game.
     * @param lNewID The new ID to assign. Should use an atomic method to determine the ID.
     * @return The structure instance with a new ID.
     */
    public LandUnit ReIDAndReturnSelf(int lNewID)
    {
        this.lID = lNewID;
        return this;
    }
    
    /** From comms.
     * @param bb The byte buffer of received data.
     * @param lReceivingID The player ID of the client for unpacking potentially player-only data. */
    public LandUnit(ByteBuffer bb, int lReceivingID)
    {
        super(bb);
        strName = LaunchUtilities.StringFromData(bb);
        lOwnerID = bb.getInt();
        dlyUnderAttack = new ShortDelay();
        bOnWater = (bb.get() != 0x00);
        resources = new ResourceSystem(bb);
        fltCurrentFuel = bb.getFloat();
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        dlyUnderAttack.Tick(lMS);
    }

    @Override
    public byte[] GetData(int lAskingID)
    {        
        byte[] cResourceData = resources.GetData();
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cResourceData.length + LaunchUtilities.GetStringDataSize(strName));
        bb.put(cBaseData);
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.putInt(lOwnerID);
        bb.put((byte)(bOnWater? 0xFF : 0x00));
        bb.put(cResourceData);
        bb.putFloat(fltCurrentFuel);
        
        return bb.array();
    }
    
    public String GetName() { return strName; }
    
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
    
    @Override
    public int GetOwnerID() { return lOwnerID; }
    
    public void SetOwner(int lNewOwnerID)
    {
        this.lOwnerID = lNewOwnerID;
    }
    
    public boolean TakingFire()
    {
        return !dlyUnderAttack.Expired();
    }
    
    public void SetUnderAttack(int lTime)
    {
        this.dlyUnderAttack = new ShortDelay(lTime);
    }
    
    public int GetUnderAttackTimeRemaining()
    {
        return dlyUnderAttack.GetRemaining();
    }
    
    public abstract boolean IsCivilian();
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }
    
    public abstract void DefendPosition();
    
    /**
     * @return whether the unit has a MoveOrder that allows it to move.
     */
    public boolean GetMobile() 
    {
        switch(moveOrders)
        {
            case MOVE:
            case ATTACK:
            case CAPTURE: return true;
        }
        
        return false;
    }
    
    /**
     * @return whether the unit has a MoveOrder that requires it to stand still.
     */
    public boolean GetStationary()
    {
        return moveOrders == MoveOrders.DEFEND;
    }
    
    public boolean Moving()
    {
        switch(moveOrders)
        {
            case MOVE:
            case LOAD:
            case UNLOAD:
            case CAPTURE:
            case LIBERATE:
            case ATTACK: return true;
        }
        
        return false;
    }
    
    public void SetOnWater(boolean bOnWater)
    {
        this.bOnWater = bOnWater;
    }
    
    public boolean GetOnWater()
    {
        return this.bOnWater;
    }
    
    public void SetRemove()
    {
        this.bRemove = true;
    }
    
    public boolean GetRemove()
    {
        return this.bRemove;
    }
    
    public void Capture(int lNewOwnerID)
    {
        this.lOwnerID = lNewOwnerID;
    }
    
    @Override    
    public short InflictDamage(short nDamage)
    {
        short nDamageToInflict = nDamage;
        
        if(bOnWater)
        {
            nDamageToInflict *= Defs.ON_WATER_DAMAGE_MULTIPLIER;
        }
        
        return super.InflictDamage(nDamageToInflict);
    }
    
    @Override
    public ResourceSystem GetResourceSystem()
    {
        return this.resources;
    }
    
    @Override
    public float GetCurrentFuel()
    {
        return this.fltCurrentFuel;
    }
    
    @Override
    public float GetFuelDeficit()
    {
        return 1.0f - fltCurrentFuel;
    }
    
    @Override
    public boolean OutOfFuel()
    {
        return fltCurrentFuel == 0.0f;
    }
    
    @Override
    public void Refuel()
    {
        this.fltCurrentFuel = 1.0f;
        Changed(false);
    }
    
    @Override
    public void Refuel(float fltAmount)
    {
        this.fltCurrentFuel += fltAmount;
        
        if(fltCurrentFuel > 1.0f)
            fltCurrentFuel = 1.0f;
                    
        Changed(false);
    }
    
    public void UseFuel(float fltFuel)
    {
        this.fltCurrentFuel -= fltFuel;
        
        if(this.fltCurrentFuel < 0.0f)
            fltCurrentFuel = 0.0f;
    }
    
    @Override
    public float GetMaxFuel()
    {
        return 1.0f;
    }
}
