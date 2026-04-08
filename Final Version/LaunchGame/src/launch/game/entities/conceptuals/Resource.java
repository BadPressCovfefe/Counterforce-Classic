/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.game.entities.Haulable;
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author Corbin
 */
public class Resource implements Haulable
{
    public static final int DATA_SIZE = 9;
    
    public enum ResourceType
    {
        IRON,
        COAL,
        OIL,
        CROPS,
        URANIUM,
        ELECTRICITY,
        WEALTH,
        CONCRETE,
        LUMBER,
        FOOD,
        FUEL,
        STEEL,
        CONSTRUCTION_SUPPLIES,
        MACHINERY,
        ELECTRONICS,
        MEDICINE,
        ENRICHED_URANIUM,
        GOLD,
        NUCLEAR_ELECTRICITY, //Not an actual resource type. Used to distinguish processors.
    }
    
    private ResourceType type;
    private Long oQuantity;
    
    public Resource(ResourceType type, long oQuantity)
    {
        this.type = type;
        this.oQuantity = oQuantity;
    }
    
    public Resource(ByteBuffer bb)
    {
        this.type = ResourceType.values()[bb.get()];
        this.oQuantity = bb.getLong();
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        
        bb.put((byte)type.ordinal());
        bb.putLong(oQuantity);
        
        return bb.array();
    }

    @Override
    public long GetQuantity()
    {
        return oQuantity;
    }
    
    public ResourceType GetResourceType()
    {
        return this.type;
    }

    @Override
    public long GetWeight()
    {
        return oQuantity;
    }
    
    public void SubtractQuantity(long oQuantity)
    {
        this.oQuantity -= oQuantity;
    }
    
    public void IncreaseQuantity(long oQuantity)
    {
        this.oQuantity += oQuantity;
    }
    
    public void SetQuantity(long oQuantity)
    {
        this.oQuantity = oQuantity;
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.RESOURCES;
    }

    @Override
    public int GetCargoID()
    {
        return type.ordinal();
    }
    
    public static String GetTypeName(ResourceType type)
    {
        switch(type)
        {
            case IRON: return "ore";
            case COAL: return "coal";
            case OIL: return "oil";
            case CROPS: return "crops";
            case GOLD: return "gold";
            case URANIUM: return "uranium";
            case ELECTRICITY: return "electricity";
            case WEALTH: return "wealth";
            case CONCRETE: return "concrete";
            case LUMBER: return "lumber";
            case FOOD: return "food";
            case FUEL: return "fuel";
            case STEEL: return "metal";
            case CONSTRUCTION_SUPPLIES: return "construction supplies";
            case MACHINERY: return "machinery";
            case ELECTRONICS: return "electronics";
            case MEDICINE: return "medicine";
            case ENRICHED_URANIUM: return "fissile material";
            case NUCLEAR_ELECTRICITY: return "[GLITCHED RESOURCE]";
        }
        
        return "[UNKNOWN RESOURCE TYPE]";
    }
}
