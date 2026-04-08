/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.LaunchServerGame;
import launch.game.entities.Airplane;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.game.entities.LaunchEntity;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.FuelableInterface;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LauncherInterface;
import launch.game.entities.NamableInterface;
import launch.game.systems.CargoSystem;
import launch.utilities.ShortDelay;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.SAMSite;

/**
 *
 * @author Corbin
 */
public class StoredAirplane extends StoredDamagable implements LaunchSystemListener, NamableInterface, AirplaneInterface, FuelableInterface, HaulerInterface, LauncherInterface
{
    public static final int DATA_SIZE = 15;
    
    private EntityType type;                                                     
    private EntityPointer homebase;
    private float fltCurrentFuel; 
    private boolean bAutoReturn;
    private MissileSystem missiles = null;
    private MissileSystem interceptors = null;
    private CargoSystem cargo = null;
    private ShortDelay dlyElecWar;
    private byte cMode;
    
    //A brand new StoredAircraft.
    public StoredAirplane(int lID, int lOwnerID, short nHP, short nMaxHP, int lPrepTime, EntityPointer homebase, EntityType type, MissileSystem missiles, MissileSystem interceptors, CargoSystem cargo)
    {
        super(lID, lOwnerID, nHP, nMaxHP, lPrepTime);
        this.type = type;
        this.fltCurrentFuel = 1.0f;
        this.homebase = homebase;                   
        this.bAutoReturn = true;
        this.missiles = missiles;
        this.interceptors = interceptors;
        this.cargo = cargo;
        this.dlyElecWar = new ShortDelay(0);
        this.cMode = SAMSite.MODE_AUTO;
        
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
            
            SetPointer();
        }
    }
    
    //From save.
    public StoredAirplane(int lID, 
                          short nHP,
                          short nMaxHP,
                          EntityType type,
                          int lOwnerID,
                          String strName,
                          float fltCurrentFuel,
                          EntityPointer homebase,
                          boolean bAutoReturn,
                          MissileSystem missiles,
                          MissileSystem interceptors,
                          CargoSystem cargo,
                          int lPrepRemaining,
                          int lElecWar,
                          byte cMode)
    {
        super(lID, lOwnerID, nHP, nMaxHP, lPrepRemaining, strName);
        this.type = type;
        this.fltCurrentFuel = fltCurrentFuel;
        this.homebase = homebase;                   
        this.bAutoReturn = true;
        this.dlyElecWar = new ShortDelay(lElecWar);
        
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
    
    //Convert flying aircraft to stored one.
    public StoredAirplane(LaunchServerGame game, int lNewID, Airplane aircraft)
    {
        super(lNewID, aircraft.GetOwnerID(), aircraft.GetHP(), aircraft.GetMaxHP(), 0, aircraft.GetName()); //0 because the aircraft isn't being built new.

        fltCurrentFuel = aircraft.GetCurrentFuel();
        homebase = aircraft.GetHomeBase();
        bAutoReturn = aircraft.WillAutoReturn();
        missiles = aircraft.HasMissiles() ? aircraft.GetMissileSystem() : null;
        interceptors = aircraft.HasInterceptors() ? aircraft.GetInterceptorSystem() : null;
        cargo = aircraft.HasCargo() ? aircraft.GetCargoSystem() : null;
        this.type = aircraft.GetAircraftType();
        
        if(cargo != null)
        {
            this.cargo.SetSystemListener(this);
            this.cargo.ReHostStoredEntities(this);
        } 
        
        dlyElecWar = new ShortDelay(aircraft.GetElecWarfareReload());
        cMode = aircraft.GetMode();
        SetPointer();
    }
    
    //From comms.
    public StoredAirplane(ByteBuffer bb)
    {
        super(bb);
        fltCurrentFuel = bb.getFloat();
        
        if(bb.get() != 0x00)
            homebase = new EntityPointer(bb);
        
        bAutoReturn = (bb.get() != 0x00);
        type = EntityType.values()[bb.get()];
        dlyElecWar = new ShortDelay(bb.getInt());
        cMode = bb.get();
        
        if((bb.get() != 0x00))
            missiles = new MissileSystem(this, bb);
        
        if((bb.get() != 0x00))
            interceptors = new MissileSystem(this, bb);
        
        if((bb.get() != 0x00))
            cargo = new CargoSystem(this, bb);
        
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
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cMissilesData.length + cInterceptorsData.length + cCargoData.length + cHomebaseData.length);
        
        bb.put(cBaseData);
        bb.putFloat(fltCurrentFuel);
        bb.put((byte)(homebase != null ? 0xFF : 0x00));
        bb.put(cHomebaseData);                 
        bb.put((byte)(bAutoReturn? 0xFF : 0x00));
        bb.put((byte)type.ordinal());
        bb.putInt(dlyElecWar.GetRemaining());
        bb.put(cMode);
        
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
    public float GetMaxFuel()
    {
        return 1.0f;
    }
    
    @Override
    public float GetCurrentFuel()
    {
        return this.fltCurrentFuel;
    }
    
    public void SetFuelLevel(int fuelLevel)
    {
        this.fltCurrentFuel = fuelLevel;
        Changed(false);
    }
    
    @Override
    public float GetFuelDeficit()
    {
        return 1.0f - fltCurrentFuel;
    }
    
    @Override
    public void Refuel()
    {
        this.fltCurrentFuel = 1.0f;
        Changed(false);
    }
    
    @Override
    public boolean OutOfFuel()
    {
        return this.fltCurrentFuel == 0;
    }
    
    @Override
    public EntityPointer GetHomeBase()
    {
        return this.homebase;
    }
    
    @Override
    public boolean WillAutoReturn()
    {
        return bAutoReturn;
    }
    
    @Override
    public boolean GetStealth()
    {
        return this.type == EntityType.STEALTH_BOMBER || this.type == EntityType.STEALTH_FIGHTER;
    }
    
    /**
     * Any call to this method should first check if HasMissiles(), otherwise you could get a NullPointerException.
     * @return 
     */
    @Override
    public MissileSystem GetMissileSystem()
    {
        return this.missiles;
    }
    
    /**
     * Any call to this method should first check if HasInterceptors(), otherwise you could get a NullPointerException.
     * @return 
     */
    @Override
    public MissileSystem GetInterceptorSystem()
    {
        return interceptors;
    }
    
    public CargoSystem GetLootSystem()
    {
        return cargo;
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
    public CargoSystem GetCargoSystem()
    {
        return this.cargo;
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
    
    public boolean EAReady()
    {
        return dlyElecWar.Expired();
    }
    
    @Override
    public int GetElecWarfareReload()
    {
        return this.dlyElecWar.GetRemaining();
    }
    
    @Override
    public boolean Flying()
    {
        return false;
    }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(false);
    }
    
    @Override
    public void ToggleAutoReturn()
    {
        this.bAutoReturn = !bAutoReturn;
        Changed(false);
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof StoredAirplane)
            return entity.GetID() == lID;
        return false;
    }
    
    public StoredAirplane ReIDAndReturnSelf(int lNewID)
    {
        this.lID = lNewID;
        return this;
    }
    
    public EntityType GetAircraftType()
    {
        return this.type;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.STORED_AIRPLANE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.StoredAirplane;
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
        return "stored aircraft";
    }
    
    public boolean Capture(int lPlayerID)
    {
        if(lOwnerID != lPlayerID)
        {
            lOwnerID = lPlayerID;
            Changed(false);
            return true;
        }
        
        return false;
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
    
    @Override
    public EntityPointer GetHost()
    {
        return GetHomeBase();
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
    
    public void SetHomeBase(EntityPointer homebase)
    {
        this.homebase = homebase;
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
    
    @Override
    public long GetStoredWeight()
    {
        return 1;
    }
    
    @Override
    public boolean GetElectronicWarfare()
    {
        return false;
    }
}
