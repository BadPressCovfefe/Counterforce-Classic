/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.game.Defs;
import launch.game.entities.Haulable;
import launch.game.systems.CargoSystem;
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author Corbin
 */
public class StoredLaunchable implements Haulable
{
    private static final int DATA_SIZE = 13;
    
    private final int lType;
    private long oQuantity;
    private boolean bMissiles;
    
    public StoredLaunchable(int lType, long oQuantity, boolean bMissiles)
    {
        this.lType = lType;
        this.oQuantity = oQuantity;
        this.bMissiles = bMissiles;
    }
    
    public StoredLaunchable(ByteBuffer bb)
    {
        this.lType = bb.getInt();
        this.oQuantity = bb.getLong();
        this.bMissiles = (bb.get() != 0x00);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        
        bb.putInt(lType);
        bb.putLong(oQuantity);
        bb.put((byte)(bMissiles? 0xFF : 0x00));
        
        return bb.array();
    }
    
    public boolean IsMissiles()
    {
        return bMissiles;
    }
    
    public boolean IsInterceptors()
    {
        return !bMissiles;
    }
    
    public int GetType()
    {
        return this.lType;
    }
    
    @Override
    public long GetQuantity()
    {
        return this.oQuantity;
    }
    
    public void SetQuantity(long oQuantity)
    {
        this.oQuantity = oQuantity;
    }
        
    /**
     * Remove a quantity of the launchable.
     * @param lQuantityToRemove the amount to remove.
     * @return the amount leftover if not everything could fit.
     */
    public long SubtractQuantity(int lQuantityToRemove)
    {
        this.oQuantity = Math.max(this.oQuantity - lQuantityToRemove, 0);
        return Math.abs(this.oQuantity - lQuantityToRemove);
    }

    @Override
    public long GetWeight()
    {
        return oQuantity * Defs.LAUNCHABLE_WEIGHT;
    }
    
    @Override
    public LootType GetLootType()
    {
        return bMissiles ? LootType.MISSILES : LootType.INTERCEPTORS;
    }

    @Override
    public int GetCargoID()
    {
        return lType;
    }
}
