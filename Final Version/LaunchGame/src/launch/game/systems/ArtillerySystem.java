/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.systems;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import launch.utilities.LaunchLog;
import launch.utilities.ShortDelay;

//The missile system is a generic missile container platform for both cruise missiles and interceptor missiles, managing weapon slots, reloading, and preperation times.
public class ArtillerySystem extends LaunchSystem
{
    public static final int MISSILE_SLOT_EMPTY_TYPE = -1;
    private static final int MISSILE_SLOT_EMPTY_TIME = -1;
    
    private static final int DATA_SIZE = 12;
    private static final int DATA_SIZE_MISSILE_SLOT = 8;
    
    private ShortDelay dlyReload;
    private int lReloadTime;
    private int lMissileSlotCount;
    private boolean bReadyToFire = true;
    
    private Map<Integer, Integer> MissileSlotTypes = new ConcurrentHashMap();
    private Map<Integer, ShortDelay> MissileSlotPrepTimes = new ConcurrentHashMap();
    
    /** New. */
    public ArtillerySystem(LaunchSystemListener listener, int lReloadTime, int lMissileSlotCount)
    {
        super(listener);
        dlyReload = new ShortDelay();
        this.lReloadTime = lReloadTime;
        this.lMissileSlotCount = lMissileSlotCount;
    }
    
    /** From save. */
    public ArtillerySystem(int lReloadRemaining, int lReloadTime, int lMissileSlotCount, Map<Integer, Integer> SlotTypes, Map<Integer, ShortDelay> PrepTimes)
    {
        super();
        dlyReload = new ShortDelay(lReloadRemaining);
        this.lReloadTime = lReloadTime;
        this.lMissileSlotCount = lMissileSlotCount;
        MissileSlotTypes = SlotTypes;
        MissileSlotPrepTimes = PrepTimes;
    }
    
    /** From comms. */
    public ArtillerySystem(LaunchSystemListener listener, ByteBuffer bb)
    {
        super(listener);
        dlyReload = new ShortDelay(bb);
        lReloadTime = bb.getInt();
        lMissileSlotCount = bb.getInt();
        
        for(int i = 0; i < lMissileSlotCount; i++)
        {
            int lType = bb.getInt();
            
            if(lType == MISSILE_SLOT_EMPTY_TYPE)
            {
                //Empty slot. Just prune the empty time value off the byte buffer.
                bb.getInt();
            }
            else
            {
                //Assign to missile slot.
                MissileSlotTypes.put(i, lType);
                MissileSlotPrepTimes.put(i, new ShortDelay(bb));
            }
        }
    }
    
    /** Dummy from comms (other players). */
    public ArtillerySystem()
    {
        dlyReload = new ShortDelay();
    }
    
    @Override
    public void Tick(int lMS)
    {
        dlyReload.Tick(lMS);
        
        if(dlyReload.Expired() && !bReadyToFire)
        {
            bReadyToFire = true;
            Changed();
        }
        
        for(ShortDelay dlyPrepTime : MissileSlotPrepTimes.values())
        {
            dlyPrepTime.Tick(lMS);
        }
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + (lMissileSlotCount * DATA_SIZE_MISSILE_SLOT));
        
        dlyReload.GetData(bb);
        bb.putInt(lReloadTime);
        bb.putInt(lMissileSlotCount);
        
        for(int i = 0; i < lMissileSlotCount; i++)
        {
            if(MissileSlotTypes.containsKey(i))
            {
                bb.putInt(MissileSlotTypes.get(i));
                MissileSlotPrepTimes.get(i).GetData(bb);
            }
            else
            {
                bb.putInt(MISSILE_SLOT_EMPTY_TYPE);
                bb.putInt(MISSILE_SLOT_EMPTY_TIME);
            }
        }
        
        return bb.array();
    }
    
    public void AddMissileToSlot(int lSlotNo, int lType, int lPrepTimeRemaining)
    {
        MissileSlotTypes.put(lSlotNo, lType);
        MissileSlotPrepTimes.put(lSlotNo, new ShortDelay(lPrepTimeRemaining));
        Changed();
    }
    
    public boolean AddMissileToNextSlot(int lSlotNo, int lType, int lPrepTimeRemaining)
    {
        if(GetEmptySlotCount() > 0)
        {
            while(GetSlotHasMissile(lSlotNo))
            {
                lSlotNo++;
                if(lSlotNo >= lMissileSlotCount)
                    lSlotNo = 0;
            }
            
            MissileSlotTypes.put(lSlotNo, lType);
            MissileSlotPrepTimes.put(lSlotNo, new ShortDelay(lPrepTimeRemaining));
            
            return true;
        }
        
        return false;
    }
    
    public void CompleteMultiPurchase()
    {
        Changed();
    }

    public int GetSlotCount() { return lMissileSlotCount; }
    
    public boolean GetSlotHasMissile(int lSlotNumber)
    {
        return MissileSlotTypes.containsKey(lSlotNumber);
    }
    
    public int GetSlotMissileType(int lSlotNumber)
    {
        Integer lSlotType = MissileSlotTypes.get(lSlotNumber);
        
        if(lSlotType != null)
            return MissileSlotTypes.get(lSlotNumber);
        else
            return MISSILE_SLOT_EMPTY_TYPE;
    }
    
    public int GetSlotPrepTimeRemaining(int lSlotNumber)
    {
        return MissileSlotPrepTimes.get(lSlotNumber).GetRemaining();
    }
    
    public boolean ReadyToFire() { return dlyReload.Expired(); }
    
    public boolean GetSlotReadyToFire(int lSlotNumber)
    {
        if(dlyReload.Expired())
        {
            if(MissileSlotTypes.containsKey(lSlotNumber))
            {
                if(MissileSlotPrepTimes.get(lSlotNumber) != null)
                    return MissileSlotPrepTimes.get(lSlotNumber).Expired();
            }
        }
        
        return false;
    }
    
    public int GetReloadTimeRemaining() { return dlyReload.GetRemaining(); }
    
    public int GetReloadTime() { return lReloadTime; }
    
    public void SetReloadTime(int lReloadTime)
    {
        this.lReloadTime = lReloadTime;
        
        if(dlyReload.GetRemaining() > lReloadTime)
        {
            dlyReload.Set(lReloadTime);
        }
        
        Changed();
    }
    
    public void Fire(int lSlot)
    {
        MissileSlotTypes.remove(lSlot);
        MissileSlotPrepTimes.remove(lSlot);
        dlyReload.Set(lReloadTime);
        bReadyToFire = false;
        LaunchLog.ConsoleMessage(String.format("Slot number %s fired. Current slot type: %s.", String.valueOf(lSlot), String.valueOf(GetSlotMissileType(lSlot))));
        Changed();
        LaunchLog.Log(LaunchLog.LogType.GAME, "Game", "Missile system fired. Updating entity...");
    }
    
    public void UnloadSlot(int lSlot)
    {
        MissileSlotTypes.remove(lSlot);
        MissileSlotPrepTimes.remove(lSlot);
        Changed();
    }
    
    public void IncreaseSlotCount(int lHowMany)
    {
        lMissileSlotCount += lHowMany;
        Changed();
    }
    
    public void SetSlotCount(byte cNewCount)
    {
        lMissileSlotCount = cNewCount;
        Changed();
    }
    
    public void ClearSlots()
    {
        for(int lSlot : MissileSlotTypes.keySet())
        {
            UnloadSlot(lSlot);
        }
    }

    public int GetEmptySlotCount()
    {
        return lMissileSlotCount - MissileSlotTypes.size();
    }

    public int GetOccupiedSlotCount()
    {
        return MissileSlotTypes.size();
    }
    
    public Map<Integer, Integer> GetTypeCounts()
    {
        Map<Integer, Integer> Result = new HashMap<>();
        
        for(Entry<Integer, Integer> MissileSlotType : MissileSlotTypes.entrySet())
        {
            int lKey = MissileSlotType.getValue();
            
            if(lKey != MISSILE_SLOT_EMPTY_TYPE)
            {
                if(Result.containsKey(lKey))
                {
                    int lNewValue = Result.get(lKey) + 1;
                    Result.put(lKey, lNewValue);
                }
                else
                {
                    Result.put(lKey, 1);
                }
            }
        }
        
        return Result;
    }
    
    public Map<Integer, Integer> GetSlotTypes()
    {
        return MissileSlotTypes;
    }
}
