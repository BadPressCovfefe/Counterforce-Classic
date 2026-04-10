/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Submarine extends NavalVessel implements FuelableInterface
{
    private static final int DATA_SIZE = 7;
    
    private boolean bSubmerged;
    private boolean bDiving;
    private ShortDelay dlySubmerge;
    private MissileSystem icbms;
    
    /** New. */
    public Submarine(int lID, GeoCoord geoPosition, int lOwnerID, short nHP, short nMaxHP, EntityType type, MissileSystem missiles, MissileSystem torpedoes, MissileSystem icbms)
    {
        super(lID, geoPosition, lOwnerID, nHP, nMaxHP, type, missiles, torpedoes);
        this.bSubmerged = false;
        this.dlySubmerge = new ShortDelay();
        this.icbms = icbms;
        
        if(icbms != null)
            icbms.SetSystemListener(this);
        
        SetPointer();
    }
    
    /** From save. */
    public Submarine(int lID, 
            GeoCoord geoPosition, 
            short nHP, 
            short nMaxHP, 
            EntityType type, 
            String strName, 
            int lOwnerID, 
            MoveOrders moveOrders, 
            GeoCoord geoTarget, 
            EntityPointer target, 
            int lSonarCooldown, 
            float fltCurrentFuel, 
            MissileSystem missiles, 
            MissileSystem torpedoes, 
            boolean bSubmerged, 
            boolean bDiving, 
            int lSubmergeTime, 
            MissileSystem icbms, 
            boolean bVisible, 
            int lVisibleTime, 
            Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, lOwnerID, nHP, nMaxHP, type, strName, moveOrders, geoTarget, target, lSonarCooldown, fltCurrentFuel, missiles, torpedoes, bVisible, lVisibleTime, Coordinates);
        this.bSubmerged = bSubmerged;
        this.bDiving = bDiving;
        this.dlySubmerge = new ShortDelay(lSubmergeTime);
        this.icbms = icbms;
        if(this.icbms != null)
            this.icbms.SetSystemListener(this);
        
        SetPointer();
    }
    
    /** From comms. */
    public Submarine(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        bSubmerged = (bb.get() != 0x00);
        dlySubmerge = new ShortDelay(bb.getInt());
        
        if(bb.get() != 0x00) //Corresponds to the boolean sent for HasICBMs() in GetData.
            icbms = new MissileSystem(this, bb);
        
        type = EntityType.values()[bb.get()];
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        if(icbms != null)
            icbms.Tick(lMS);
        
        dlySubmerge.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cICBMData = icbms != null ? icbms.GetData(lAskingID) : new byte[0];
        
        byte[] cBaseData = super.GetData(lAskingID);
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cICBMData.length);
        bb.put(cBaseData);
        bb.put((byte)(bSubmerged? 0xFF : 0x00));
        bb.putInt(dlySubmerge.GetRemaining());
        bb.put((byte)(HasICBMs() ? 0xFF : 0x00));
        bb.put(cICBMData);
        bb.put((byte)type.ordinal());
        
        return bb.array();
    }
    
    public boolean HasICBMs()
    {
        return icbms != null;
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Submarine;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Submarine)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void MoveToPosition(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.MOVE;
        Changed(false);
    }
    
    public void DiveOrSurface()
    {
        bSubmerged = !bSubmerged;
        /*bDiving = true;
        dlySubmerge.Set(Defs.SUBMARINE_SUBMERGE_TIME);
        Changed(false);*/
    }
    
    public boolean Submerged()
    {
        return bSubmerged;
    }
    
    public boolean Diving()
    {
        return bDiving;
    }
    
    public int GetSubmergeTimeRemaining()
    {
        return dlySubmerge.GetRemaining();
    }
    
    public boolean ReadyToDiveOrSurface()
    {
        return dlySubmerge.Expired();
    }
    
    public boolean CanFireMissiles()
    {
        return !Diving() && !Submerged();
    }
    
    @Override
    public void AttackTarget(EntityPointer target)
    {
        this.target = new EntityPointer(target.GetID(), target.GetType());
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    @Override
    public void CaptureTarget(EntityPointer target)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Submarines don't do this.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Submarines don't do this.
    }
    
    public MissileSystem GetICBMSystem()
    {
        return icbms;
    }

    @Override
    public String GetTypeName()
    {
        if(GetNuclear())
            return "nuclear submarine";
        else if(HasICBMs())
            return "SSBN";
        else
            return "attack submarine";
    }
    
    @Override
    public void Refuel()
    {
        this.fltCurrentFuel = 1.0f;
        Changed(false);
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return type;
    }
}
