/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.systems.AircraftSystem;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.MissileSystem;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Ship extends NavalVessel implements AirbaseInterface, FuelableInterface, HaulerInterface, ArtilleryInterface
{
    private static final int DATA_SIZE = 9;
    
    private byte cMode;
    private CargoSystem cargo;
    private MissileSystem artillery;
    private List<ShortDelay> Sentries = new ArrayList<>();
    private MissileSystem interceptors;
    private AircraftSystem aircrafts;
    private FireOrder fireOrder = null;
    private boolean bRadarActive;
    
    /** New. */
    public Ship(int lID, GeoCoord geoPosition, int lOwnerID, short nHP, short nMaxHP, EntityType type, MissileSystem missiles, MissileSystem torpedoes, CargoSystem cargo, MissileSystem artillery, int lSentryCount, MissileSystem interceptors, AircraftSystem aircrafts)
    {
        super(lID, geoPosition, lOwnerID, nHP, nMaxHP, type, missiles, torpedoes);
        this.cargo = cargo;
        this.artillery = artillery;
        this.cMode = SAMSite.MODE_AUTO;
        this.bRadarActive = false;
        
        if(lSentryCount > 0)
        {
            for(int i = 0; i < lSentryCount; i++)
            {
                Sentries.add(new ShortDelay());
            }
        }
        
        this.interceptors = interceptors;
        this.aircrafts = aircrafts;
        
        SetPointer();
    }
    
    /** From save. */
    public Ship(int lID, 
            GeoCoord geoPosition, 
            int lOwnerID, 
            short nHP, 
            short nMaxHP, 
            EntityType type, 
            String strName, 
            MoveOrders moveOrders, 
            GeoCoord geoTarget, 
            EntityPointer target,
            int lSonarCooldown, 
            float fltCurrentFuel, 
            MissileSystem missiles, 
            MissileSystem torpedoes, 
            byte cMode, 
            CargoSystem cargo, 
            MissileSystem artillery, 
            List<ShortDelay> Sentries, 
            MissileSystem interceptors, 
            AircraftSystem aircraft, 
            boolean bVisible, 
            int lVisibleTime, 
            FireOrder fireOrder, 
            boolean bRadarActive, 
            Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, lOwnerID, nHP, nMaxHP, type, strName, moveOrders, geoTarget, target, lSonarCooldown, fltCurrentFuel, missiles, torpedoes, bVisible, lVisibleTime, Coordinates);
        this.cMode = cMode;
        this.Sentries = Sentries == null ? new ArrayList<>() : Sentries;
        this.cargo = cargo;
        this.bRadarActive = bRadarActive;
        
        if(this.cargo != null)
            this.cargo.SetSystemListener(this);
        this.artillery = artillery;
        if(this.artillery != null)
            this.artillery.SetSystemListener(this);
        this.interceptors = interceptors;
        if(this.interceptors != null)
            this.interceptors.SetSystemListener(this);
        this.aircrafts = aircraft;
        if(this.aircrafts != null)
            aircrafts.SetSystemListener(this);
        if(fireOrder != null)
            this.fireOrder = fireOrder;
        
        SetPointer();
    }
    
    /** From comms. */
    public Ship(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        cMode = bb.get();
        bRadarActive = (bb.get() != 0x00);
        
        boolean bHasCargo = (bb.get() != 0x00);
        
        if(bHasCargo)
            cargo = new CargoSystem(this, bb);
        
        boolean bHasArtillery = (bb.get() != 0x00);
        
        if(bHasArtillery)
            artillery = new MissileSystem(this, bb);
        
        boolean bHasSentries = (bb.get() != 0x00);
        
        if(bHasSentries)
            Sentries = GetSentryListFromData(bb);
        
        boolean bHasInterceptors = (bb.get() != 0x00);
        
        if(bHasInterceptors)
            interceptors = new MissileSystem(this, bb);
        
        boolean bHasAircraft = (bb.get() != 0x00);
        
        if(bHasAircraft)
            aircrafts = new AircraftSystem(this, bb);
        
        if((bb.get() != 0x00)) //HasFireOrder()
            fireOrder = new FireOrder(bb);
        
        type = EntityType.values()[bb.get()];
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        if(interceptors != null)
            interceptors.Tick(lMS);
        
        if(cargo != null)
            cargo.Tick(lMS);
        
        if(Sentries != null && !Sentries.isEmpty())
        {
            for(ShortDelay dlyReload : Sentries)
            {
                dlyReload.Tick(lMS);
            }
        }
        
        if(artillery != null)
            artillery.Tick(lMS);
        
        if(aircrafts != null)
            aircrafts.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cCargoData = cargo != null ? cargo.GetData(lAskingID) : new byte[0];
        byte[] cArtilleryGunData = artillery != null ? artillery.GetData(lAskingID) : new byte[0];
        byte[] cSentryData = HasSentries() ? GetSentryListData() : new byte[0]; 
        byte[] cInterceptorData = interceptors != null ? interceptors.GetData(lAskingID) : new byte[0];
        byte[] cAircraftData = aircrafts != null ? aircrafts.GetData(lAskingID) : new byte[0];
        byte[] cFireOrderData = fireOrder != null ? fireOrder.GetData() : new byte[0];
        
        byte[] cBaseData = super.GetData(lAskingID);
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cCargoData.length + cSentryData.length + cInterceptorData.length + cAircraftData.length + cArtilleryGunData.length + cFireOrderData.length);
        bb.put(cBaseData);
        bb.put(cMode);
        bb.put((byte)(bRadarActive ? 0xFF : 0x00));
        bb.put((byte)(HasCargo() ? 0xFF : 0x00));
        bb.put(cCargoData);
        bb.put((byte)(HasArtillery() ? 0xFF : 0x00));
        bb.put(cArtilleryGunData);
        bb.put((byte)(HasSentries() ? 0xFF : 0x00));
        bb.put(cSentryData);
        bb.put((byte)(HasInterceptors() ? 0xFF : 0x00));
        bb.put(cInterceptorData);
        bb.put((byte)(HasAircraft() ? 0xFF : 0x00));
        bb.put(cAircraftData);
        bb.put((byte)(HasFireOrder() ? 0xFF : 0x00));
        bb.put(cFireOrderData);
        bb.put((byte)type.ordinal());
        
        return bb.array();
    }
    
    public byte[] GetSentryListData()
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + (Integer.BYTES * Sentries.size()));

        bb.putInt(Sentries.size());

        for(ShortDelay dlyReload : Sentries)
        {
            bb.putInt(dlyReload.GetRemaining());
        }

        return bb.array();
    }
    
    private List<ShortDelay> GetSentryListFromData(ByteBuffer bb)
    {
        List<ShortDelay> list = new ArrayList<>();
        
        int lListSize = bb.getInt();
        
        for(int i = 0; i < lListSize; i++)
        {
            list.add(new ShortDelay(bb.getInt()));
        }
        
        return list;
    }
    
    public boolean HasScanner()
    {
        return this.type == EntityType.FRIGATE || this.type == EntityType.DESTROYER || this.type == EntityType.SUPER_CARRIER;
    }
    
    public boolean HasCargo()
    {
        return cargo != null;
    }
    
    public boolean HasSentries()
    {
        return Sentries != null && !Sentries.isEmpty();
    }
    
    @Override
    public CargoSystem GetCargoSystem()
    {
        return this.cargo;
    }
    
    public boolean HasArtillery()
    {
        return artillery != null;
    }
    
    public MissileSystem GetArtillerySystem()
    {
        return artillery;
    }
    
    public boolean HasAircraft()
    {
        return aircrafts != null;
    }
    
    @Override
    public AircraftSystem GetAircraftSystem()
    {
        return aircrafts;
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Ship;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Ship)
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
        //Ships don't do this.
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //Ships don't do this.
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //Ships don't do this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Ships don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Ships don't do this.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Ships don't do this.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Ships don't do this.
    }
    
    public MissileSystem GetInterceptorSystem()
    {
        return interceptors;
    }
    
    public boolean HasInterceptors()
    {
        return interceptors != null;
    }
    
    public List<ShortDelay> GetSentryGuns()
    {
        return this.Sentries;
    }
    
    public int GetSentryCount()
    {
        return Sentries.size();
    }
    
    public int GetReadySentryCount()
    {
        if(!Sentries.isEmpty())
        {
            int lReadyCount = 0;
            
            for(ShortDelay dlyReload : Sentries)
            {
                if(dlyReload.Expired())
                    lReadyCount++;
            }
            
            return lReadyCount;
        }
        
        return 0;
    }
    
    public boolean IsOffensive()
    {
        return HasMissiles() || HasScanner() || HasTorpedoes() || HasSonar() || HasAircraft();
    }
    
    public boolean IsDefensive()
    {
        return HasInterceptors();
    }
    
    public boolean IsEconomic()
    {
        return !IsOffensive() && !IsDefensive() && HasCargo();
    }
    
    public boolean IsNeutral()
    {
        return !IsOffensive() && !IsDefensive();
    }
    
    public boolean GetAuto() { return cMode == SAMSite.MODE_AUTO; }
    
    public boolean GetSemiAuto() { return cMode == SAMSite.MODE_SEMI_AUTO; }
    
    public boolean GetManual() { return cMode == SAMSite.MODE_MANUAL; }
    
    public byte GetMode() { return cMode; }
    
    public void SetMode(byte cMode)
    {
        this.cMode = cMode;
        Changed(true);
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
    
    public boolean HasAmphibious()
    {
        return this.type == EntityType.AMPHIB;
    }
    
    public boolean HasSupport()
    {
        return this.type == EntityType.FLEET_OILER;
    }
    
    @Override
    public boolean GetVisible()
    {
        return super.GetVisible() || bRadarActive;
    }
    
    @Override
    public void Wait()
    {
        super.Wait();
        RoundsComplete();
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
