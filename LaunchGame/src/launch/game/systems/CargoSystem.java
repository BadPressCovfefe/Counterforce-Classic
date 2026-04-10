/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.systems;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.Defs;
import launch.game.entities.Haulable;
import launch.game.entities.LaunchEntity;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredLaunchable;
import launch.game.entities.conceptuals.StoredTank;
import launch.utilities.LaunchUtilities;

/**
 * The LootSystem is a cargo system for players to use to transport loot and infantry inside other entities.
 * @author Corbin
 */
public class CargoSystem extends LaunchSystem
{
    public enum LootType
    {
        MISSILES,
        INTERCEPTORS,
        RESOURCES,
        TORPEDOES,
        STORED_INFANTRY,
        STORED_TANK,
        STORED_CARGO_TRUCK,
        INFANTRY,
        CARGO_TRUCK,
        TANK,
        NONE,
    }
    
    private static final int DATA_SIZE = 8;
    
    private long oCapacityInKG;
    
    private Map<ResourceType, Long> resources = new ConcurrentHashMap<>();
    private Map<Integer, StoredInfantry> infantries = new ConcurrentHashMap<>();
    private Map<Integer, StoredTank> tanks = new ConcurrentHashMap<>();
    private Map<Integer, StoredCargoTruck> trucks = new ConcurrentHashMap<>();
    
    /** New. */
    public CargoSystem(LaunchSystemListener listener, long oCapacityInKG)
    {
        super(listener);
        this.oCapacityInKG = oCapacityInKG;
    }
    
    /** From save. */
    public CargoSystem(long oCapacityInKG, Map<ResourceType, Long> resources, Map<Integer, StoredInfantry> infantries, Map<Integer, StoredTank> tanks, Map<Integer, StoredCargoTruck> trucks)
    {
        super();
        this.oCapacityInKG = oCapacityInKG;
        this.resources = resources;
        this.infantries = infantries;
        this.tanks = tanks;
        this.trucks = trucks;
    }
    
    /** Dummy from comms. */
    public CargoSystem()
    {
        
    }
    
    /** From comms. */
    public CargoSystem(LaunchSystemListener listener, ByteBuffer bb)
    {
        super(listener);
        
        oCapacityInKG = bb.getLong();
        
        resources = LaunchUtilities.ResourcesFromData(bb);
        
        int lInfantryCount = bb.getInt();
        
        for(int i = 0; i < lInfantryCount; i++)
        {
            AddInfantry(new StoredInfantry(bb));
        }
        
        int lTankCount = bb.getInt();
        
        for(int i = 0; i < lTankCount; i++)
        {
            AddTank(new StoredTank(bb));
        }
        
        int lTruckCount = bb.getInt();
        
        for(int i = 0; i < lTruckCount; i++)
        {
            AddCargoTruck(new StoredCargoTruck(bb));
        }
    }
    
    @Override
    public void Tick(int lMS)
    {
        for(StoredInfantry infantry : infantries.values())
        {
            infantry.Tick(lMS);
        }
        
        for(StoredTank tank : tanks.values())
        {
            tank.Tick(lMS);
        }
        
        for(StoredCargoTruck truck : trucks.values())
        {
            truck.Tick(lMS);
        }
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cResourcesData = LaunchUtilities.GetResourceData(resources);
        byte[] cInfantryData = GetInfantryData(lAskingID);
        byte[] cTankData = GetTankData(lAskingID);
        byte[] cCargoTruckData = GetCargoTruckData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cResourcesData.length + cInfantryData.length + cTankData.length + cCargoTruckData.length);
        
        bb.putLong(oCapacityInKG);
        bb.put(cResourcesData);
        bb.put(cInfantryData);
        bb.put(cTankData);
        bb.put(cCargoTruckData);
        
        return bb.array();
    }
    
    /**
     * Stored trucks, tanks, and infantry track their hosts. When an aircraft changes into a stored aircraft or vice-versa, for example, the host needs to be updated.
     * @param newHost the new host.
     */
    public void ReHostStoredEntities(LaunchEntity newHost)
    {
        for(StoredInfantry infantry : infantries.values())
        {
            infantry.SetHost(newHost.GetPointer());
        }
        
        for(StoredTank tank : tanks.values())
        {
            tank.SetHost(newHost.GetPointer());
        }
        
        for(StoredCargoTruck truck : trucks.values())
        {
            truck.SetHost(newHost.GetPointer());
        }
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Comms-related methods. These handle converting the contents of the maps into 
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public byte[] GetInfantryData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + GetInfantryDataSize(lAskingID));
        
        bb.putInt(infantries.size());
        
        for(StoredInfantry infantry : infantries.values())
        {
            bb.put(infantry.GetData(lAskingID));
        }
        
        return bb.array();
    }
    
    public int GetInfantryDataSize(int lAskingID)
    {
        int lSize = 0;
        
        for(StoredInfantry infantry : infantries.values())
        {
            lSize += infantry.GetData(lAskingID).length;
        }
        
        return lSize;
    }
    
    public byte[] GetTankData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + GetTankDataSize(lAskingID));
        
        bb.putInt(tanks.size());
        
        for(StoredTank tank : tanks.values())
        {
            bb.put(tank.GetData(lAskingID));
        }
        
        return bb.array();
    }
    
    public int GetTankDataSize(int lAskingID)
    {
        int lSize = 0;
        
        for(StoredTank tank : tanks.values())
        {
            lSize += tank.GetData(lAskingID).length;
        }
        
        return lSize;
    }
    
    public byte[] GetCargoTruckData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + GetCargoTruckDataSize(lAskingID));
        
        bb.putInt(trucks.size());
        
        for(StoredCargoTruck truck : trucks.values())
        {
            bb.put(truck.GetData(lAskingID));
        }
        
        return bb.array();
    }
    
    public int GetCargoTruckDataSize(int lAskingID)
    {
        int lSize = 0;
        
        for(StoredCargoTruck truck : trucks.values())
        {
            lSize += truck.GetData(lAskingID).length;
        }
        
        return lSize;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Content adding methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public long AddHaulable(Haulable haulable)
    {
        if(!Full())
        {
            switch(haulable.GetLootType())
            {
                case RESOURCES:
                {
                    return AddResource(ResourceType.values()[haulable.GetCargoID()], haulable.GetQuantity());
                }
            }
        }
        
        return haulable.GetQuantity();
    }
    
    /*
     * Add a resource to the cargo system and return the quantity of resource that didn't fit, if any.
     */
    public long AddResource(ResourceType type, long oQuantity)
    {
        if(!Full() || type == ResourceType.WEALTH)
        {
            if(resources.containsKey(type))
            {
                //If the cargo system already contains the same type of resource, add it to the existing resource.
                long oQuantityPresent = resources.get(type);
                long oRemainingCapacity = GetRemainingCapacity();
                long oQuantityToAdd = Math.min(oRemainingCapacity, oQuantity);
                
                if(type == ResourceType.WEALTH)
                    oQuantityToAdd = oQuantity;
                
                long oLeftover = oQuantity - oQuantityToAdd;
                resources.put(type, oQuantityPresent + oQuantityToAdd);

                return oLeftover;
            }
            else
            {
                if(type == ResourceType.WEALTH)
                {
                    resources.put(type, oQuantity); //Money has infinite capacity.
                    return 0;
                }
                else
                {
                    long oQuantityToAdd = Math.min(GetRemainingCapacity(), oQuantity);
                    long oLeftover = oQuantity - oQuantityToAdd;
                    resources.put(type, oQuantityToAdd);

                    return oLeftover;
                }  
            }
        }
        else
            return oQuantity; //The system is full, so the whole quantity is leftover.
    }
    
    public boolean AddInfantry(StoredInfantry infantry)
    {
        if(WeightCanFit(infantry.GetWeight()))
        {
            infantries.put(infantry.GetID(), infantry);
            Changed();
            
            return true;
        }
        
        return false;
    }
    
    public boolean AddTank(StoredTank tank)
    {
        if(WeightCanFit(tank.GetWeight()))
        {
            this.tanks.put(tank.GetID(), tank);
            Changed();
            return true;
        }
        
        return false;
    }
    
    public boolean AddCargoTruck(StoredCargoTruck truck)
    {
        if(WeightCanFit(truck.GetWeight()))
        {
            this.trucks.put(truck.GetID(), truck);
            Changed();
            return true;
        }
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Content removal methods. (These actually remove things from the cargo system.)
    //---------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * All callers of these methods should be careful to do a null-check or make sure to confirm the cargo system actually contains the desired haulable.
     */
    public Haulable RemoveHaulable(LootType typeToMove, int lTypeID, int lQuantityToMove)
    {
        switch(typeToMove)
        {
            case RESOURCES:
            {
                return RemoveResource(ResourceType.values()[lTypeID], lQuantityToMove);
            }
        }
        
        return null;
    }
    
    /**
     * Removes a resource from the cargo system and returns it.
     * @param type
     * @param lQuantity
     * @return the new resource.
     */
    public Resource RemoveResource(ResourceType type, int lQuantity)
    {
        if(resources.containsKey(type))
        {
            long oQuantityPresent = resources.get(type);
            long oQuantityToRemove = Math.min(oQuantityPresent, lQuantity);
            
            if(oQuantityPresent - oQuantityToRemove == 0)
            {
                resources.remove(type);
            }
            else
            {
                resources.put(type, oQuantityPresent - oQuantityToRemove);
            }
            
            return new Resource(type, oQuantityToRemove);
        }  
        
        return null;
    }
    
    public long RemoveQuantity(ResourceType type, int lQuantity)
    {
        if(resources.containsKey(type))
        {
            long oQuantityPresent = resources.get(type);
            
            if(lQuantity >= oQuantityPresent)
            {
                resources.put(type, (long)0);
                return oQuantityPresent;
            }
            else
            {
                resources.put(type, oQuantityPresent - lQuantity);
                return lQuantity;
            }
        }
        
        return 0;
    }
    
    public StoredInfantry RemoveInfantry(int lID)
    {
        return infantries.remove(lID);
    }
    
    public StoredTank RemoveTank(int lID)
    {
        return tanks.remove(lID);
    }
    
    public StoredCargoTruck RemoveCargoTruck(int lID)
    {
        return trucks.remove(lID);
    }
    
    public boolean ChargeQuantity(ResourceType type, long oQuantity)
    {
        //This is just a quick fix for a bug that was charging players negative amounts of steel, which was giving them free steel.
        if(oQuantity >= 0)
        {
            if(resources.containsKey(type))
            {
                long oQuantityPresent = resources.get(type);

                if(oQuantity <= oQuantityPresent)
                {
                    resources.put(type, oQuantityPresent - oQuantity);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean ChargeQuantities(Map<ResourceType, Long> types)
    {
        boolean bAmountsPresent = true;
        
        for(Entry<ResourceType, Long> entry : types.entrySet())
        {
            if(resources.containsKey(entry.getKey()))
            {
                if(entry.getValue() > resources.get(entry.getKey()))
                {
                    bAmountsPresent = false;
                    break;
                }
            }
            else
            {
                bAmountsPresent = false;
                break;
            }
        }  
        
        if(bAmountsPresent)
        {
            for(Entry<ResourceType, Long> entry : types.entrySet())
            {
                resources.put(entry.getKey(), resources.get(entry.getKey()) - entry.getValue());
            }
            
            return true;
        }
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Content accessor methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public Collection<Haulable> GetContents()
    {
        List<Haulable> result = new ArrayList<>();
        
        for(Entry<ResourceType, Long> entry : resources.entrySet())
        {
            result.add(new Resource(entry.getKey(), entry.getValue()));
        }
        
        result.addAll(infantries.values());
        result.addAll(tanks.values());
        result.addAll(trucks.values());
        
        return result;
    }
    
    public boolean ContainsResourceType(ResourceType type)
    {
        return resources.containsKey(type);
    }
    
    public Collection<Resource> GetResources()
    {
        List<Resource> result = new ArrayList<>();
        
        for(Entry<ResourceType, Long> entry : resources.entrySet())
        {
            result.add(new Resource(entry.getKey(), entry.getValue()));
        }
        
        return result;
    }
    
    public boolean ContainsHaulable(LootType type, int lID, int lQuantity)
    {
        switch(type)
        {
            case TORPEDOES:
            {
                return false;
            }
            
            case STORED_INFANTRY:
            {
                return infantries.containsKey(lID);
            }
            
            case STORED_TANK:
            {
                return tanks.containsKey(lID);
            }
            
            case STORED_CARGO_TRUCK:
            {
                return trucks.containsKey(lID);
            }
        }
        
        return false;
    }
    
    public boolean ContainsTanks()
    {
        return !tanks.isEmpty();
    }
    
    public boolean ContainsCargoTrucks()
    {
        return !trucks.isEmpty();
    }
    
    public boolean ContainsInfantry()
    {
        return !infantries.isEmpty();
    }
    
    public Collection<StoredInfantry> GetInfantries()
    {
        return infantries.values();
    }
    
    public Collection<StoredTank> GetTanks()
    {
        return tanks.values();
    }
    
    public Collection<StoredCargoTruck> GetCargoTrucks()
    {
        return trucks.values();
    }
    
    public Map<ResourceType, Long> GetResourceMap()
    {
        return resources;
    }
    
    public boolean ContainsQuantities(Map<ResourceType, Long> types)
    {
        for(Entry<ResourceType, Long> entry : types.entrySet())
        {
            if(resources.containsKey(entry.getKey()))
            {
                if(entry.getValue() > resources.get(entry.getKey()))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }  
        
        return true;
    }
    
    /**
     * Queries how much of this resource type is present.
     * @param type the type to check.
     * @return the amount of each type.
     */
    public long GetAmountOfType(ResourceType type)
    {
        if(ContainsResourceType(type))
            return resources.get(type);
        else
            return 0;
    }
    
    public boolean HasQuantity(ResourceType type, long oQuantity)
    {
        return ContainsResourceType(type) && resources.get(type) >= oQuantity;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Capacity-measuring methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public long GetCapacity() { return oCapacityInKG; }
    
    public boolean WeightCanFit(long oWeight)
    {
        return oWeight <= GetRemainingCapacity();
    }
    
    public long GetUsedCapacity()
    {
        long oCurrentTonnage = 0;
        
        for(Haulable haulable : GetContents())
        {
            if(!(haulable.GetLootType() == LootType.RESOURCES && haulable.GetCargoID() == ResourceType.WEALTH.ordinal()))
                oCurrentTonnage += haulable.GetWeight();
        }
        
        return oCurrentTonnage;
    }
    
    public boolean Full()
    {
        return GetUsedCapacity() >= GetCapacity();
    }

    public long GetRemainingCapacity()
    {
        long oCapacity = GetCapacity();
        long oUsedCapacity = GetUsedCapacity();
        return oCapacity - oUsedCapacity;
    }
    
    /**
     * WARNING: DO NOT give this method the ability to clear trucks, tanks, or infantry. Those MUST be removed via LaunchGame.RemoveStoredInfanty, etc, otherwise the game will have no way to know they've been removed. 
     */
    public void ClearNonEntities()
    {
        resources.clear();
    }
    
    public void SetCapacity(long oNewCapacity)
    {
        this.oCapacityInKG = oNewCapacity;
    }
}
