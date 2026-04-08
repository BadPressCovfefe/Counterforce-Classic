/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.systems;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import launch.game.Defs;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.utilities.LaunchLog;

/**
 * @author Corbin
 * ResourceContainer is the class for structures or other entities that can hold resources for some benefit to them.
 * For example, infantry can hold medicine that heals them. Structures hold electricity that gives them benefit. 
 * Refinery structures hold certain resources that they convert into other resources, etc. 
 * The Map "types" is used to limit what kind of resource the entity that possesses the resourcecontainer can hold.
 * For example, we might only want Infantry to be able to hold medicine, so on initialization, the ResourceContainer for 
 * infantry will only be given "ResourceType.MEDICINE, 0" for the map. As such, never remove keys completely from the map, 
 * only set the corresponding value to 0.
 */
public class ResourceSystem
{
    public static final int DATA_SIZE = 8;
    public static final int MAP_ENTRY_DATA_SIZE = 9;
    public static final int MAP_ENTRY_COUNT_DATA_SIZE = 1; //A short is used to tell the receiver how many different entries to expect for the types list. Shorts take up 2 bytes.
    
    private Map<ResourceType, Long> resources = new HashMap();
    private long oCapacity; //In kg. 
    
    /**
     * 
     * @param oCapacity the capacity of the container in metric tons. Will be converted to kg.
     * @param types the resource types this container can hold.
     */
    public ResourceSystem(long oCapacity, List<ResourceType> resources)
    {
        this.oCapacity = oCapacity;
        
        for(ResourceType type : resources)
        {
            this.resources.put(type, 0L);
        }
    }
    
    public ResourceSystem(long oCapacity, Map<ResourceType, Long> resources)
    {
        this.oCapacity = oCapacity;
        this.resources = resources;
    }
    
    /** Dummy */
    public ResourceSystem()
    {
        
    }
    
    /**
     * For adding a new type to the container. Not currently used for in-game functions. Rather, I use it to add a new type if I decide a certain entity should contain an additional type. -Corbin
     * @param type 
     */
    public void AddType(ResourceType type)
    {
        if(!resources.containsKey(type))
            this.resources.put(type, 0L);
    }
    
    public void AddType(ResourceType type, long oAmount)
    {
        this.resources.put(type, oAmount);
    }
    
    public ResourceSystem(ByteBuffer bb)
    {
        this.oCapacity = bb.getLong();
        byte cCount = bb.get();
        
        for(long i = 0; i < cCount; i++)
        {
            byte cType = bb.get();
            ResourceType type = ResourceType.values()[cType];
            long oQuantity = bb.getLong();
            
            resources.put(type, oQuantity);
        }
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + MAP_ENTRY_COUNT_DATA_SIZE + (MAP_ENTRY_DATA_SIZE * resources.size()));
        bb.putLong(oCapacity);
        bb.put((byte)resources.size());
        
        for(Entry<ResourceType, Long> entry : resources.entrySet())
        {
            bb.put((byte)entry.getKey().ordinal());
            bb.putLong(entry.getValue());
        }
        
        return bb.array();
    }
    
    /**
     * Add a quantity of a resource to this container.
     * @param type
     * @param lQuantity
     * @return the leftover amount that didn't fit.
     */
    public long AddQuantity(ResourceType type, long oQuantity)
    {
        if(!Full() || type == ResourceType.WEALTH)
        {
            //If the cargo system already contains the same type of resource, add it to the existing resource.
            if(resources.containsKey(type))
            {
                long oQuantityPresent = resources.get(type);
                
                if(type == ResourceType.WEALTH)
                {
                    resources.put(type, oQuantityPresent + oQuantity); //Money has infinite capacity.
                    return 0;
                }
                else
                {
                    long oQuantityToAdd = Math.min(GetRemainingCapacity(), oQuantity);
                    long oLeftover = oQuantity - oQuantityToAdd;
                    resources.put(type, oQuantityPresent + oQuantityToAdd);
                    return oLeftover;
                }
            }
        }
        
        return oQuantity;
    }
    
    /**
     * Remove a quantity of a resourcetype from the types list. DO NOT call Map.remove(), as we do not want the keys to be removed.
     * @param type
     * @param lQuantity
     * @return the amount removed from the list.
     */
    public long RemoveQuantity(ResourceType type, long oQuantity)
    {
        if(resources.containsKey(type))
        {
            long oQuantityPresent = resources.get(type);
            
            if(oQuantity >= oQuantityPresent)
            {
                resources.put(type, 0L);
                return oQuantityPresent;
            }
            else
            {
                resources.put(type, oQuantityPresent - oQuantity);
                return oQuantity;
            }
        }
        
        return 0;
    }
    
    public boolean ChargeQuantity(ResourceType type, long oQuantity)
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
    
    public void SetQuantity(ResourceType type, long oAmount)
    {
        if(resources.containsKey(type))
            resources.put(type, oAmount);
    }
    
    public void RemoveResources(Map<ResourceType, Integer> typesToRemove)
    {
        for(Entry<ResourceType, Integer> type : typesToRemove.entrySet())
        {
            RemoveQuantity(type.getKey(), type.getValue());
        }
    }
    
    public long GetUsedCapacity()
    {
        long oUsedCapacity = 0;
        
        for(Entry<ResourceType, Long> type : resources.entrySet())
        {
            if(type.getKey() != ResourceType.WEALTH)
                oUsedCapacity += type.getValue();
        }
        
        return oUsedCapacity;
    }
    
    public boolean Full()
    {
        return GetUsedCapacity() >= oCapacity;
    }
    
    public long GetRemainingCapacity()
    {
        return oCapacity - GetUsedCapacity();
    }
    
    public long GetCapacity()
    {
        return oCapacity;
    }
    
    public Map<ResourceType, Long> GetTypes()
    {
        return resources;
    }
    
    public boolean CanHoldType(ResourceType type)
    {
        return resources.containsKey(type);
    }
    
    public boolean HasQuantity(ResourceType type, long oWeight)
    {
        if(oWeight <= 0)
        {
            return true;
        }
        else
        {
            return resources.containsKey(type) && resources.get(type) >= oWeight;
        }
    }
    
    /**
     * Check to see if this resource container contains the required resources for some action.
     * @param requiredTypes a map of esourcetypes keyed with the required quantity.
     * @return tru if it contains the required quantity of each resource; false if it doesn't.
     */
    public boolean HasNecessaryResources(Map<ResourceType, Long> requiredTypes)
    {
        for(Entry<ResourceType, Long> type : requiredTypes.entrySet())
        {
            if(!resources.containsKey(type.getKey()) || resources.get(type.getKey()) < type.getValue())
                return false;
        }
        
        return true;
    }
    
    /**
     * @param type the type to check the value of.
     * @return How much the container has.
     */
    public long GetAmountOfType(ResourceType type)
    {
        return resources.get(type);
    }
    
    public Resource GetAndRemove(ResourceType type, long oQuantity)
    {
        if(resources.containsKey(type) && resources.get(type) > 0)
        {
            return new Resource(type, Math.min(resources.get(type), oQuantity));
        }
        
        return null;
    }
    
    public void SetCapacity(long oNewCapacity)
    {
        this.oCapacity = oNewCapacity;
    }
    
    public void RemoveType(ResourceType type)
    {
        if(this.resources.containsKey(type))
            resources.remove(type);
    }
    
    public void Clear()
    {
        for(Entry<ResourceType, Long> Type : resources.entrySet())
        {
            resources.put(Type.getKey(), 0L);
        }
    }
    
    public boolean ContainsType(ResourceType type)
    {
        return resources.containsKey(type) && resources.get(type) > 0;
    }
}
