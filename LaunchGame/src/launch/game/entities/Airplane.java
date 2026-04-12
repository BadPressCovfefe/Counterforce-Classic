/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.systems.LaunchSystem;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Airplane extends Movable implements LaunchSystemListener, FuelableInterface, NamableInterface, AirplaneInterface, HaulerInterface, LauncherInterface
{
    public static final int DATA_SIZE = 51;
    
    private EntityType type;
    private int lOwnerID;
    private byte cMode;
    private EntityPointer homebase;  
    private String strName;
    private float fltCurrentFuel; 
    private boolean bAutoReturn;
    private MissileSystem missiles = null;
    private MissileSystem interceptors = null;
    private CargoSystem cargo = null;                       
    private ShortDelay dlyCannon;
    private ShortDelay dlyElecWar;
    private boolean bRadarActive;
    
    //Server-only.
    private int lTimeAirborne = Defs.MS_PER_HOUR;
    
    /**
     * Aircraft are never spawned on the map directly during run-time, they are either 
     * converted from StoredAircraft, received by comms, or loaded from save. So there 
     * is no "new aircraft" constructor as there is for other map entities.
     */
    
    //From save.
    public Airplane(int lID, 
            GeoCoord geoPosition, 
            GeoCoord geoTarget,
            short nHP, 
            short nMaxHP, 
            int lOwnerID, 
            EntityType type,
            MoveOrders orders, 
            EntityPointer homebase,
            String strName,
            float fltCurrentFuel,
            boolean bAutoReturn,
            MissileSystem missiles,
            MissileSystem interceptors,
            CargoSystem cargo,
            int lCannonReload,
            EntityPointer target,
            int lEAReload, 
            boolean bVisible, 
            int lVisibleTime,
            int lTimeAirborne,
            boolean bRadarActive,
            byte cMode,
            Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, orders, geoTarget, target, bVisible, lVisibleTime, Coordinates);
        this.type = type;
        this.lOwnerID = lOwnerID;
        this.strName = strName;
        this.fltCurrentFuel = fltCurrentFuel;
        this.homebase = homebase;                   
        this.bAutoReturn = bAutoReturn;
        this.dlyCannon = new ShortDelay(lCannonReload);
        this.dlyElecWar = new ShortDelay(lEAReload);
        this.lTimeAirborne = lTimeAirborne;
        this.bRadarActive = bRadarActive;
        
        if(missiles != null)
        {
            this.missiles = missiles;
            this.missiles.SetSystemListener(this);
        }
        
        if(interceptors != null)
        {
            this.interceptors = interceptors;
            this.interceptors.SetSystemListener(this);
        }
        
        if(cargo != null)
        {
            this.cargo = cargo;
            this.cargo.SetSystemListener(this);
        }
        
        SetPointer();
    }
    
    //From comms.
    public Airplane(ByteBuffer bb)
    {
        super(bb);
        lOwnerID = bb.getInt();
        type = EntityType.values()[bb.get()];
        cMode = bb.get();
        strName = LaunchUtilities.StringFromData(bb);
        fltCurrentFuel = bb.getFloat();
        if(bb.get() != 0x00)
            homebase = new EntityPointer(bb);                  
        bAutoReturn = (bb.get() != 0x00);
        dlyCannon = new ShortDelay(bb.getInt());
        dlyElecWar = new ShortDelay(bb.getInt());
        bRadarActive = (bb.get() != 0x00);
        
        if((bb.get() != 0x00))
            missiles = new MissileSystem(this, bb);
        if((bb.get() != 0x00))
            interceptors = new MissileSystem(this, bb);
        if((bb.get() != 0x00))
            cargo = new CargoSystem(this, bb);
        
        SetPointer();
    }
    
    //Converting a storedaircraft to a flying one.
    public Airplane(int lNewID, StoredAirplane storedAircraft, MapEntity host, GeoCoord geoTarget, Map<Integer, GeoCoord> Coordinates)
    {
        super(lNewID, host.GetPosition().GetCopy(), storedAircraft.GetHP(), storedAircraft.GetMaxHP(), MoveOrders.MOVE, geoTarget, null, false, 0, Coordinates);
        this.lOwnerID = storedAircraft.GetOwnerID();
        this.type = storedAircraft.GetAircraftType();
        this.strName = storedAircraft.GetName();
        this.fltCurrentFuel = storedAircraft.GetCurrentFuel();
        this.homebase = storedAircraft.GetHomeBase();                   
        this.bAutoReturn = storedAircraft.WillAutoReturn();
        this.dlyCannon = new ShortDelay(0);
        this.dlyElecWar = new ShortDelay(storedAircraft.GetElecWarfareReload());
        this.lTimeAirborne = 0;
        this.bRadarActive = false;
        this.cMode = storedAircraft.GetMode();
        
        this.missiles = storedAircraft.GetMissileSystem();
        if(missiles != null)
            this.missiles.SetSystemListener(this);
        
        this.interceptors = storedAircraft.GetInterceptorSystem();
        if(interceptors != null)
            this.interceptors.SetSystemListener(this);
        
        this.cargo = storedAircraft.GetCargoSystem();
        if(cargo != null)
        {
            this.cargo.SetSystemListener(this);
            this.cargo.ReHostStoredEntities(this);
        }
        
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        if(missiles != null)
        {
            missiles.Tick(lMS);
        }
        
        if(interceptors != null)
        {
            interceptors.Tick(lMS);
        }
        
        lTimeAirborne++;
        
        dlyCannon.Tick(lMS);
        dlyElecWar.Tick(lMS);
    }
    
    /**
     * Get data to communicate, base class. Subclasses should override and super call this method.
     * @param lAskingID The ID of the player this data will be sent to.
     * @return The data to communicate.
     */
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        //If missiles or interceptors is null, put no data in the bytebuffer. The client will not pull data from the bytebuffer unless the aircraft type dictates their should be data to pull.
        byte[] cMissilesData = missiles != null ? missiles.GetData(lAskingID) : new byte[0]; 
        byte[] cInterceptorsData = interceptors != null ? interceptors.GetData(lAskingID) : new byte[0];
        byte[] cCargoData = cargo != null ? cargo.GetData(lAskingID) : new byte[0];
        byte[] cHomebaseData = homebase != null ? homebase.GetData() : new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + LaunchUtilities.GetStringDataSize(strName) + cMissilesData.length + cInterceptorsData.length + cCargoData.length + cHomebaseData.length);
        
        bb.put(cBaseData);
        bb.putInt(lOwnerID);
        bb.put((byte)type.ordinal());
        bb.put(cMode);
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.putFloat(fltCurrentFuel);
        bb.put((byte)(homebase != null ? 0xFF : 0x00));
        bb.put(cHomebaseData);                    
        bb.put((byte)(bAutoReturn? 0xFF : 0x00));
        bb.putInt(dlyCannon.GetRemaining());
        bb.putInt(dlyElecWar.GetRemaining());
        bb.put((byte)(bRadarActive? 0xFF : 0x00));
        
        bb.put((byte)(missiles != null? 0xFF : 0x00));
        bb.put(cMissilesData);
        bb.put((byte)(interceptors != null? 0xFF : 0x00));
        bb.put(cInterceptorsData);
        bb.put((byte)(cargo != null? 0xFF : 0x00));
        bb.put(cCargoData);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public int GetID()
    {
        return this.lID;
    }
    
    @Override
    public LaunchEntity GetAirplane()
    {
        return this;
    }
    
    @Override
    public int GetOwnerID()
    {
        return lOwnerID;
    }
    
    @Override
    public float GetMaxFuel()
    {
        return 1.0f;
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
    
    public boolean FuelFull()
    {
        return this.fltCurrentFuel == 1.0f;
    }
    
    @Override
    public void Refuel()
    {
        this.fltCurrentFuel = 1.0f;
        Changed(false);
    }
    
    public void UseFuel(float fltFuel)
    {
        this.fltCurrentFuel -= fltFuel;
        
        if(this.fltCurrentFuel < 0.0f)
            fltCurrentFuel = 0.0f;
    }
    
    public void AddFuel(float fltFuel)
    {
        if(this.fltCurrentFuel + fltFuel > 1.0f)
        {
            this.fltCurrentFuel = 1.0f;
            Changed(false);
        }
        else
        {
            this.fltCurrentFuel += fltFuel;
            Changed(false);
        }
    }
    
    /**
     * 
     * @param fltFuel the fuel to add.
     * @return the amount of fuel transferred.
     */
    public float TransferRefuel(float fltFuel)
    {
        if(this.fltCurrentFuel + fltFuel > 1.0f)
        {
            float fltFuelTransferred = 1.0f - fltCurrentFuel;
            this.fltCurrentFuel = 1.0f;
            Changed(false);
            return fltFuelTransferred;
        }
        else
        {
            this.fltCurrentFuel += fltFuel;
            Changed(false);
            return fltFuel;
        }
    }
    
    @Override
    public boolean OutOfFuel()
    {
        return this.fltCurrentFuel <= 0.1f;
    }
    
    @Override
    public EntityPointer GetHomeBase()
    {
        return this.homebase;
    }
    
    public void SetHomeBase(EntityPointer homebase)
    {
        this.homebase = homebase;
        Changed(false);
    }
    
    public void ReturnToBase()
    {
        this.moveOrders = MoveOrders.RETURN;
        Changed(false);
    }
    
    public boolean Orphaned()
    {
        return this.homebase == null;
    }
    
    public void MakeOrphan()
    {
        this.homebase = null;
        Changed(false);
    }
    
    @Override
    public String GetName() { return strName; }
    
    @Override
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
    
    @Override
    public void ToggleAutoReturn()
    {
        this.bAutoReturn = !bAutoReturn;
        Changed(false);
    }
    
    @Override
    public boolean WillAutoReturn()
    {
        return this.bAutoReturn;
    }
    
    @Override
    public boolean GetStealth()
    {
        return this.type == EntityType.STEALTH_BOMBER || this.type == EntityType.STEALTH_FIGHTER;
    }
    
    /**
     * To prevent null pointer crashes, any call to this method should be prefaced by a call to aircraft.HasMissiles.
     * @return 
     */
    @Override
    public MissileSystem GetMissileSystem()
    {
        return this.missiles;
    }
    
    /**
     * To prevent null pointer crashes, any call to this method should be prefaced by a call to aircraft.HasInterceptors.
     * @return 
     */
    @Override
    public MissileSystem GetInterceptorSystem()
    {
        return this.interceptors;
    }
    
    /**
     * To prevent null pointer crashes, any call to this method should be prefaced by a call to aircraft.HasCargo.
     * @return 
     */
    @Override
    public CargoSystem GetCargoSystem()
    {
        return this.cargo;
    }
    
    @Override
    public boolean HasMissiles()
    {
        return missiles != null;
    }
    
    @Override
    public boolean HasInterceptors()
    {
        return interceptors != null;
    }
    
    @Override
    public boolean HasScanner()
    {
        return this.type == EntityType.AWACS;
    }
    
    @Override
    public boolean HasCannon()
    {
        return this.type == EntityType.FIGHTER || this.type == EntityType.ATTACK_AIRCRAFT;
    }
    
    @Override
    public boolean HasCargo()
    {
        return cargo != null;
    }
    
    @Override
    public boolean Flying()
    {
        return true;
    }
    
    /**
     * For checking if an entity references the same conceptual entity.For inspecting entity updates etc. DO NOT USE for containers/memory management (memory-referential equality)!
     * @param entity The entity to check.
     * @return Whether it appears to reference the same entity.
     */
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Airplane)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(false);
    }
    
    public boolean CannonReady()
    {
        return this.dlyCannon.Expired();
    }
    
    public void FireCannon()
    {
        if(HasCannon())
        {
            dlyCannon.Set(Defs.AIRCRAFT_CANNON_RELOAD_TIME);
        }
    }
    
    public boolean Armed()
    {
        return HasMissiles() || HasInterceptors() || HasCannon();
    }
    
    public boolean EWReady()
    {
        return dlyElecWar.Expired();
    }
    
    @Override
    public int GetElecWarfareReload()
    {
        return this.dlyElecWar.GetRemaining();
    }
    
    public void ElectronicWarfareUsed()
    {
        dlyElecWar.Set(Defs.ELECTRONIC_WARFARE_RELOAD);
    }
    
    public int GetCannonReloadRemaining()
    {
        return this.dlyCannon.GetRemaining();
    }
    
    @Override
    public boolean GetCarrierCompliant()
    {
        return this.type == EntityType.MULTI_ROLE;
    }
    
    @Override
    public boolean CanTransferFuel()
    {
        return this.type == EntityType.REFUELER;
    }
    
    @Override
    public boolean GroundAttack()
    {
        return this.type == EntityType.ATTACK_AIRCRAFT;
    }
    
    @Override
    public boolean GetElectronicWarfare()
    {
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return type;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Airplane;
    }
    
    @Override
    public void AttackTarget(EntityPointer target)
    {
        this.target = new EntityPointer(target.GetID(), target.GetType());
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    /**
     * This aircraft is going to be refueled by another aircraft.
     * @param tanker a pointer to the aircraft providing the refueling.
     */
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        this.moveOrders = MoveOrders.SEEK_FUEL;
        this.target = tanker;
    }
    
    /**
     * This aircraft is going to refuel another aircraft.
     * @param refuelee a pointer to the aircraft being refueled.
     */
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        if(CanTransferFuel())
        {
            this.moveOrders = MoveOrders.PROVIDE_FUEL;
            this.target = refuelee;
        }
    }
   
    @Override
    public void CaptureTarget(EntityPointer target)
    {
        //Aircraft don't use this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Aircraft don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //TODO: Aircraft actually could use this to airdrop stuff.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Aircraft don't do this.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Aircraft don't do this.
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
    public String GetTypeName()
    {
        switch(type)
        {
            case FIGHTER: return "fighter";
            case BOMBER: return "bomber";
            case STEALTH_FIGHTER: return "stealth fighter";
            case STEALTH_BOMBER: return "stealth bomber";
            case ATTACK_AIRCRAFT: return "attack plane";
            case AWACS: return "AWACS";
            case REFUELER: return "refueling tanker";
            case CARGO_PLANE: return "cargo plane";
            case MULTI_ROLE: return "multi-role fighter";
            case SSB: return "supersonic bomber";
        }
        
        return "AIRCRAFT TYPE UNKNOWN [aircraft.GetTypeName()]";
    }
    
    @Override
    public boolean IsOffensive()
    {
        return HasMissiles() || HasScanner() || GroundAttack() || HasCannon();
    }
    
    @Override
    public boolean IsDefensive()
    {
        return HasInterceptors();
    }
    
    @Override
    public boolean IsNeutral()
    {
        return (!IsOffensive() && !IsDefensive());
    }
    
    public int GetTimeAirborne()
    {
        return this.lTimeAirborne;
    }
    
    @Override
    public boolean GetVisible()
    {
        return super.GetVisible() || bRadarActive;
    }
    
    @Override
    public void Wait()
    {
        moveOrders = MoveOrders.MOVE;
        Coordinates.clear();
        geoTarget = geoPosition.GetCopy();
        target = null;
        Changed(false);
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
    public void Refuel(float fltAmount)
    {
        this.fltCurrentFuel += fltAmount;
        
        if(fltCurrentFuel > 1.0f)
            fltCurrentFuel = 1.0f;
                    
        Changed(false);
    }
    
    @Override
    public EntityType GetAircraftType()
    {
        return this.type;
    }
}
