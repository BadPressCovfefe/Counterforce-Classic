/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import launch.game.entities.conceptuals.StoredTank;
import java.nio.ByteBuffer;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.game.systems.ResourceSystem;

import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Tank extends LandUnit implements LaunchSystemListener, NamableInterface, TankInterface, Haulable, LauncherInterface, ArtilleryInterface
{
    private static final int DATA_SIZE = 9;
    
    private byte cMode;
    private EntityType type;
    private ShortDelay dlyReload;
    private MissileSystem launchables;
    private FireOrder fireOrder = null;
    
    /** New. */
    public Tank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, EntityType type, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, resources);
        this.type = type;
        dlyReload = new ShortDelay();
        
        this.cMode = SAMSite.MODE_AUTO;
        SetPointer();
    }
    
    /** From save. */
    public Tank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, byte cMode, String strName, int lOwnerID, int lUnderAttack, MoveOrders moveOrder, GeoCoord geoTarget, int lReloadRemaining, MissileSystem missileSystem, EntityType type, boolean bVisible, int lVisibleTime, ResourceSystem resources, float fltFuel, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, lUnderAttack, moveOrder, geoTarget, null, bVisible, lVisibleTime, resources, fltFuel, Coordinates);
        this.cMode = cMode;
        this.type = type;
        dlyReload = new ShortDelay(lReloadRemaining);
        launchables = missileSystem;
        
        if(launchables != null)
            launchables.SetSystemListener(this);
        
        SetPointer();
    }
    
    /** From stored missile tank. */
    public Tank(int lNewID, StoredTank tank, GeoCoord geoPosition)
    {
        super(lNewID, geoPosition, tank.GetHP(), Defs.TANK_MAX_HP, tank.GetName(), tank.GetOwnerID(), 0, MoveOrders.WAIT, new GeoCoord(), null, false, 0, tank.GetResourceSystem(), tank.GetCurrentFuel(), null);
        this.cMode = tank.GetMode();
        this.type = tank.GetType();
        this.dlyReload = new ShortDelay(Defs.BATTLE_TANK_RELOAD_TIME);
        launchables = tank.GetMissileSystem();
        
        if(launchables != null)
            launchables.SetSystemListener(this);
        
        SetPointer();
    }
    
    /** From comms. */
    public Tank(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        cMode = bb.get();
        type = EntityType.values()[bb.get()];
        dlyReload = new ShortDelay(bb);
        
        if(bb.get() != 0x00)
            fireOrder = new FireOrder(bb);
        
        if(bb.get() != 0x00)
            launchables = new MissileSystem(this, bb);
        
        type = EntityType.values()[bb.get()];
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        dlyReload.Tick(lMS);
        
        if(launchables != null)
            launchables.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cMissileSystemData = launchables != null ? launchables.GetData(lAskingID) : new byte[0];
        byte[] cBaseData = super.GetData(lAskingID);
        byte[] cFireOrderData = fireOrder != null ? fireOrder.GetData() : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cMissileSystemData.length + cFireOrderData.length);
        bb.put(cBaseData);
        bb.put(cMode);
        bb.put((byte)type.ordinal());
        bb.putInt(dlyReload.GetRemaining());
        
        bb.put((byte)(HasFireOrder() ? 0xFF : 0x00));
        bb.put(cFireOrderData);
        
        bb.put((byte)(launchables != null ? 0xFF : 0x00));
        
        if(launchables != null)
            bb.put(cMissileSystemData);
        
        bb.put((byte)type.ordinal());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public MissileSystem GetMissileSystem()
    {
        return launchables;
    }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public boolean IsCivilian()
    {
        return false;
    }
    
    @Override
    public String GetTypeName()
    {
        switch(type)
        {
            case MBT: return "main battle tank";
            case SPAAG: return "SPAAG";
            case MISSILE_TANK: return "missile artillery";
            case SAM_TANK: return "SAM artillery";
            case HOWITZER: return "self-propelled howitzer";
        }
        
        return "UNKNOWN TANK TYPE";
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Tank;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.TANK;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Tank)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void MoveToPosition(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.MOVE;
        
        if(HasFireOrder())
            RoundsComplete();
        
        Changed(false);
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
        //Tanks don't do this.
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void DefendPosition()
    {
        this.moveOrders = MoveOrders.DEFEND;
        Changed(false);
    }
    
    public boolean ReadyToFire()
    {
        return this.moveOrders == MoveOrders.DEFEND;
    }
    
    /*@Override
    public MissileSystem GetInterceptorSystem()
    {
        return launchables;
    }*/

    @Override
    public boolean IsAnMBT()
    {
        return type == EntityType.MBT;
    }
    
    @Override
    public boolean IsASPAAG()
    {
        return type == EntityType.SPAAG;
    }
    
    @Override
    public boolean IsMissiles()
    {
        return type == EntityType.MISSILE_TANK;
    }
    
    @Override
    public boolean IsInterceptors()
    {
        return type == EntityType.SAM_TANK;
    }
    
    @Override
    public boolean IsArtillery()
    {
        return type == EntityType.HOWITZER;
    }
    
    @Override
    public boolean HasMissiles()
    {
        return IsMissiles() && launchables != null;
    }
    
    @Override
    public boolean HasInterceptors()
    {
        return IsInterceptors() && launchables != null;
    }
    
    @Override
    public boolean HasArtillery()
    {
        return IsArtillery() && launchables != null;
    }
    
    @Override
    public long GetWeight()
    {
        return Defs.MISSILE_TANK_WEIGHT/* + (launchables.GetOccupiedSlotCount() * Defs.LAUNCHABLE_WEIGHT)*/;
    }
    
    @Override
    public long GetQuantity()
    {
        return 1;
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.TANK;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    @Override
    public boolean HoldsMissiles()
    {
        return HasMissiles();
    }
    
    @Override 
    public boolean HoldsInterceptors()
    {
        return HasInterceptors();
    }
    
    @Override
    public void SetName(String strName)
    {
        super.SetName(strName);
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
    public boolean GetAuto() { return cMode == SAMSite.MODE_AUTO; }
    
    @Override
    public boolean GetSemiAuto() { return cMode == SAMSite.MODE_SEMI_AUTO; }
    
    @Override
    public boolean GetManual() { return cMode == SAMSite.MODE_MANUAL; }
    
    @Override
    public byte GetMode() { return cMode; }
    
    @Override
    public void SetMode(byte cMode)
    {
        this.cMode = cMode;
        Changed(true);
    }
    
    @Override
    public LaunchEntity GetTank()
    {
        return this;
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    @Override
    public EntityType GetType()
    {
        return this.type;
    }
    
    @Override
    public FireOrder GetFireOrder()
    {
        return fireOrder;
    }

    @Override
    public boolean HasFireOrder()
    {
        return fireOrder != null;
    }

    @Override
    public void RoundsComplete()
    {
        this.fireOrder = null;
    }

    @Override
    public void FireForEffect(FireOrder order)
    {
        this.fireOrder = order;
    }
}
