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
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.ResourceSystem;


/**
 *
 * @author Corbin
 */
public class CargoTruck extends LandUnit implements LaunchSystemListener, NamableInterface, CargoTruckInterface, Haulable, HaulerInterface
{
    private static final int DATA_SIZE = 9;
    
    private CargoSystem cargo;
    private LootType typeToDeliver; //What kind of cargo thing are we delivering? (Trucks can hold missiles and resources.)
    private int lDeliverTypeID; //What kind of that thing are we delivering? If deliverType is missiles, this umber will be the ID of the missile. If resources, it will be the ordinal of the resource type.
    private int lQuantity; //How much of the resource are we going to deliver?
    
    /** New. */
    public CargoTruck(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, ResourceSystem resources)
    { 
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, resources);
        this.typeToDeliver = LootType.RESOURCES; //Just to give it a value so there is no null stuff later.
        cargo = new CargoSystem(this, 0);
    }
    
    /** From save. */
    public CargoTruck(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, int lUnderAttack, MoveOrders moveOrder, GeoCoord geoTarget, CargoSystem cargoSystem, LootType typeToDeliver, int lDeliverTypeID, int lQuantity, boolean bVisible, int lVisibleTime, ResourceSystem resources, float fltFuel, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, lUnderAttack, moveOrder, geoTarget, null, bVisible, lVisibleTime, resources, fltFuel, Coordinates);
        cargo = cargoSystem;
        cargo.SetSystemListener(this);
        this.typeToDeliver = typeToDeliver;
        this.lDeliverTypeID = lDeliverTypeID;
        this.lQuantity = lQuantity;
    }
    
    /** From stored cargo truck. */
    public CargoTruck(int lNewID, StoredCargoTruck truck, GeoCoord geoPosition)
    {
        super(lNewID, geoPosition, truck.GetHP(), (short)69, truck.GetName(), truck.GetOwnerID(), 0, MoveOrders.WAIT, new GeoCoord(), null, false, 0, truck.GetResourceSystem(), truck.GetCurrentFuel(), null);
        cargo = truck.GetCargoSystem();
        cargo.SetSystemListener(this);
        this.typeToDeliver = LootType.RESOURCES;
    }
    
    /** From comms. */
    public CargoTruck(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        typeToDeliver = LootType.values()[bb.get()];
        lDeliverTypeID = bb.getInt();
        lQuantity = bb.getInt();
        cargo = new CargoSystem(this, bb);
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
        byte[] cCargoSystemData = cargo.GetData(lAskingID);
        byte[] cBaseData = super.GetData(lAskingID);
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cCargoSystemData.length);
        bb.put(cBaseData);
        bb.put((byte)typeToDeliver.ordinal());
        bb.putInt(lDeliverTypeID);
        bb.putInt(lQuantity);
        bb.put(cCargoSystemData);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public LootType GetTypeToDeliver()
    {
        return typeToDeliver;
    }
    
    public int GetDeliverTypeID()
    {
        return lDeliverTypeID;
    }
    
    public int GetQuantityToDeliver()
    {
        return lQuantity;
    }
    
    @Override
    public CargoSystem GetCargoSystem()
    {
        return cargo;
    }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public boolean IsCivilian()
    {
        return true;
    }
    
    @Override
    public String GetTypeName()
    {
        return "cargo truck";
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.CargoTruck;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.CARGO_TRUCK;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof CargoTruck)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        this.target = receiver;
        this.moveOrders = MoveOrders.UNLOAD;
        this.typeToDeliver = typeToDeliver;
        this.lDeliverTypeID = lTypeToDeliver;
        this.lQuantity = lQuantityToDeliver;
        Changed(false);
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        this.geoTarget = geoDropOff;
        this.moveOrders = MoveOrders.UNLOAD;
        this.typeToDeliver = typeToDeliver;
        this.lDeliverTypeID = lTypeToDeliver;
        this.lQuantity = lQuantityToDeliver;
        Changed(false);
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        this.target = loot;
        this.moveOrders = MoveOrders.LOAD;
        Changed(false);
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
        //Trucks don't do this.
    }
    
    @Override
    public void CaptureTarget(EntityPointer target)
    {
        //Trucks don't do this.
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //TODO: ?
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //TODO: ?
    }
    
    @Override
    public void DefendPosition()
    {
        this.moveOrders = MoveOrders.WAIT;
        Changed(false);
    }

    @Override
    public long GetWeight()
    {
        return 1;
    }

    @Override
    public long GetQuantity()
    {
        return 1;
    }

    @Override
    public LootType GetLootType()
    {
        return LootType.CARGO_TRUCK;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    @Override
    public void SetName(String strName)
    {
        super.SetName(strName);
    }
    
    @Override
    public LaunchEntity GetCargoTruck()
    {
        return this;
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        //Cargo trucks don't attack.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Trucks don't do this.
    }
}
