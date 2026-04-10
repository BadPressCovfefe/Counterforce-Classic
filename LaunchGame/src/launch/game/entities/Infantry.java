/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.ResourceSystem;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Infantry extends LandUnit implements InfantryInterface, Haulable, NamableInterface
{
    private static final int DATA_SIZE = 4;
    
    private ShortDelay dlyReload;
    
    /** New. */
    public Infantry(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, MoveOrders moveOrder, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, resources);
        this.dlyReload = new ShortDelay();
    }
    
    /** From save. */
    public Infantry(int lID, 
            GeoCoord geoPosition, 
            float fltLastBearing, 
            short nHP, 
            short nMaxHP, 
            String strName, 
            int lOwnerID, 
            int lUnderAttack, 
            MoveOrders moveOrder, 
            GeoCoord geoTarget, 
            EntityPointer target, 
            boolean bVisible, 
            int lVisibleTime, 
            ResourceSystem resources, 
            float fltFuel, 
            Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, lUnderAttack, moveOrder, geoTarget, target, bVisible, lVisibleTime, resources, fltFuel, Coordinates);
        this.geoPosition.SetLastBearing(fltLastBearing);
        this.dlyReload = new ShortDelay(Defs.INFANTRY_RELOAD_TIME);
    }
    
    //Convert storedinfantry to infantry.
    public Infantry(int lNewID, StoredInfantry storedInfantry, GeoCoord geoDeploy)
    {
        super(lNewID, geoDeploy, storedInfantry.GetHP(), storedInfantry.GetMaxHP(), storedInfantry.GetName(), storedInfantry.GetOwnerID(), 0, MoveOrders.WAIT, geoDeploy, null, false, 0, storedInfantry.GetResourceSystem(), storedInfantry.GetCurrentFuel(), null);
        this.dlyReload = new ShortDelay(Defs.INFANTRY_RELOAD_TIME);
    }
    
    public Infantry(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.dlyReload = new ShortDelay(bb);
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        dlyReload.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        
        bb.put(cBaseData);
        bb.putInt(dlyReload.GetRemaining());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public LaunchEntity GetInfantry()
    {
        return this;
    }
    
    @Override
    public boolean Deployed()
    {
        return true;
    }
    
    @Override
    public boolean IsCivilian()
    {
        return false;
    }
    
    @Override
    public String GetTypeName()
    {
        return "infantry";
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Infantry;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.INFANTRY;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Infantry)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void DefendPosition()
    {
        //if(GetMobile())
            //ToggleMoveOut(Defs.INFANTRY_PACK_TIME);
        
        this.moveOrders = MoveOrders.DEFEND;
        Changed(false);
    }
    
    @Override
    public void AttackTarget(EntityPointer target)
    {
        //if(!GetMobile())
            //ToggleMoveOut(Defs.INFANTRY_PACK_TIME);
        
        this.moveOrders = MoveOrders.ATTACK;
        this.target = target;
        Changed(false);
    }
    
    @Override
    public void CaptureTarget(EntityPointer target)
    {
        this.moveOrders = MoveOrders.CAPTURE;
        this.target = target;
        
        Changed(false);
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        this.moveOrders = MoveOrders.LIBERATE;
        this.target = target;
        
        Changed(false);
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //Infantry don't do this.
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //Infantry don't do this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Infantry don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Infantry don't do this.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Infantry don't do this.
    }
    
    @Override
    public long GetWeight()
    {
        return Defs.WEIGHT_STORED_INFANTRY;
    }
    
    @Override
    public long GetQuantity()
    {
        return 1;
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.INFANTRY;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    public void SetReloadTime(int lTime)
    {
        dlyReload.Set(lTime);
        Changed(false);
    }
    
    public boolean GetCanFire()
    {
        return dlyReload.Expired();
    }
    
    public int GetReloadTimeRemaining()
    {
        return dlyReload.GetRemaining();
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
}
