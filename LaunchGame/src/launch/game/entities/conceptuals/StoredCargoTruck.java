/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.CargoTruck;
import launch.game.entities.CargoTruckInterface;
import launch.game.entities.Haulable;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.NamableInterface;
import launch.game.entities.ResourceInterface;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.ResourceSystem;


/**
 *
 * @author Corbin
 */
public class StoredCargoTruck extends StoredDamagable implements LaunchSystemListener, NamableInterface, CargoTruckInterface, Haulable, HaulerInterface, ResourceInterface
{
    private static final int DATA_SIZE = 9;
    private CargoSystem cargo;
    private EntityPointer host;
    private ResourceSystem resources;
    private float fltFuel;
    
    /** From save. */
    public StoredCargoTruck(int lID, short nHP, short nMaxHP, String strName, int lOwnerID, int lPrep, CargoSystem cargoSystem, EntityPointer host, ResourceSystem resources, float fltFuel)
    {
        super(lID, lOwnerID, nHP, nMaxHP, lPrep, strName);
        cargo = cargoSystem;
        cargo.SetSystemListener(this);
        this.host = host;
        this.resources = resources;
        this.fltFuel = fltFuel;
    }
    
    /** From stored cargo truck. */
    public StoredCargoTruck(int lNewID, CargoTruck truck, EntityPointer host)
    {
        super(lNewID, truck.GetOwnerID(), truck.GetHP(), (short)69, 0, truck.GetName());
        cargo = truck.GetCargoSystem();
        cargo.SetSystemListener(this);
        this.host = host;
        this.resources = truck.GetResourceSystem();
        this.fltFuel = truck.GetCurrentFuel();
        
    }
    
    /** From comms. */
    public StoredCargoTruck(ByteBuffer bb)
    {
        super(bb);
        cargo = new CargoSystem(this, bb);
        host = new EntityPointer(bb);
        resources = new ResourceSystem(bb);
        fltFuel = bb.getFloat();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        cargo.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cResourceData = resources.GetData();
        byte[] cCargoSystemData = cargo.GetData(lAskingID);
        byte[] cBaseData = super.GetData(lAskingID);
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cCargoSystemData.length + cResourceData.length);
        bb.put(cBaseData);
        bb.put(cCargoSystemData);
        bb.put(host.GetData());
        bb.put(cResourceData);
        bb.putFloat(fltFuel);
        
        return bb.array();
    }
    
    @Override
    public long GetWeight()
    {
        return 1;
    }
    
    @Override
    public long GetStoredWeight()
    {
        return GetWeight();
    }

    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof StoredCargoTruck)
            return entity.GetID() == lID;
        return false;
    }

    @Override
    public EntityPointer.EntityType GetEntityType()
    {
        return EntityType.STORED_CARGO_TRUCK;
    }

    @Override
    public int GetSessionCode()
    {
        return LaunchSession.StoredCargoTruck;
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public CargoSystem GetCargoSystem()
    {
        return cargo;
    }

    @Override
    public long GetQuantity()
    {
        return 1; //Trucks are not stackable.
    }
    
    @Override
    public String GetTypeName()
    {
        return "cargo truck";
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.STORED_CARGO_TRUCK;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    @Override
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
    
    public void SetHost(EntityPointer newHost)
    {
        this.host = newHost;
    }
    
    @Override
    public EntityPointer GetHost()
    {
        return this.host;
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
    public LaunchEntity GetCargoTruck()
    {
        return this;
    }
    
    public float GetCurrentFuel()
    {
        return this.fltFuel;
    }
    
    @Override
    public ResourceSystem GetResourceSystem()
    {
        return resources;
    }
    
    public float GetFuelDeficit()
    {
        return 1.0f - fltFuel;
    }
}
