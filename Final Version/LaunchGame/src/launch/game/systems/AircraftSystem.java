/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.systems;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.Defs;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

public class AircraftSystem extends LaunchSystem
{
    private static final int DATA_SIZE = 13;
    
    private boolean bOpen;
    private ShortDelay dlyTakeoff;
    private int lTakeoffTime;
    private int lAircraftSlotCount;
    private boolean bReadyToTakeoff = true;
    
    private Map<Integer, StoredAirplane> StoredAirplanes = new ConcurrentHashMap();
    
    /** New. */
    public AircraftSystem(LaunchSystemListener listener, int lTakeoffTime, int lAircraftSlotCount)
    {
        super(listener);
        this.bOpen = false;
        dlyTakeoff = new ShortDelay();
        this.lTakeoffTime = lTakeoffTime;
        this.lAircraftSlotCount = lAircraftSlotCount;
    }
    
    /** From save. */
    public AircraftSystem(boolean bOpen, int lTakeoffTime, int lAircraftSlotCount, Map<Integer, StoredAirplane> StoredAircrafts)
    {
        super();
        this.bOpen = bOpen;
        this.dlyTakeoff = new ShortDelay(lTakeoffTime);
        this.lAircraftSlotCount = lAircraftSlotCount;
        this.StoredAirplanes = StoredAircrafts;
    }
    
    /** From comms. */
    public AircraftSystem(LaunchSystemListener listener, ByteBuffer bb)
    {
        super(listener);
        bOpen = (bb.get() != 0x00);
        lTakeoffTime = bb.getInt();
        lAircraftSlotCount = bb.getInt();
        dlyTakeoff = new ShortDelay(bb);
        StoredAirplanes = LaunchUtilities.StoredAircraftListFromData(bb);
    }
    
    /** Dummy from comms (other players). */
    public AircraftSystem()
    {
        dlyTakeoff = new ShortDelay();
    }
    
    @Override
    public void Tick(int lMS)
    {
        dlyTakeoff.Tick(lMS);
        
        for(StoredAirplane aircraft : StoredAirplanes.values())
        {
            aircraft.Tick(lMS);
        }
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        int planesSize = LaunchUtilities.GetStoredAircraftListDataSize(StoredAirplanes.values());

        int total = Math.addExact(DATA_SIZE, planesSize);
        
        if(total < 0)
        {
            throw new IllegalStateException("AircraftSystem total size negative");
        }

        ByteBuffer bb = ByteBuffer.allocate(total);

        bb.put((byte)(bOpen ? 0xFF : 0x00));
        bb.putInt(lTakeoffTime);
        bb.putInt(lAircraftSlotCount);
        bb.putInt(dlyTakeoff.GetRemaining());

        bb.put(LaunchUtilities.BBFromStoredAircraft(StoredAirplanes.values()));

        return bb.array();
    }


    public int GetSlotCount() { return lAircraftSlotCount; }
    
    public boolean ReadyForTakeoff() { return dlyTakeoff.Expired(); }
    
    public int GetReloadTimeRemaining() { return dlyTakeoff.GetRemaining(); }
    
    public int GetReloadTime() { return lTakeoffTime; }
    
    public void SetReloadTime(int lTakeoffTime)
    {
        this.lTakeoffTime = lTakeoffTime;
        
        if(dlyTakeoff.GetRemaining() > lTakeoffTime)
        {
            dlyTakeoff.Set(lTakeoffTime);
        }
        
        Changed();
    }
    
    public StoredAirplane RemoveAndGetAirplane(int lSlot)
    {
        if(StoredAirplanes.containsKey(lSlot))
        {
            SetReloadTime(lTakeoffTime);
            Changed();
            return StoredAirplanes.remove(lSlot);
        }
        
        return null;
    }
    
    public void IncreaseSlotCount(int lHowMany)
    {
        lAircraftSlotCount += lHowMany;
        LaunchLog.ConsoleMessage(String.format("Increasing slot count by %d. New slot count: %d", lHowMany, lAircraftSlotCount));
        Changed();
    }
    
    public void SetSlotCount(int lNewCount)
    {
        lAircraftSlotCount = lNewCount;
        Changed();
    }

    public int GetEmptySlotCount()
    {
        return lAircraftSlotCount - StoredAirplanes.size();
    }

    public int GetOccupiedSlotCount()
    {
        return StoredAirplanes.size();
    }
    
    public Map<Integer, StoredAirplane> GetStoredAirplanes()
    {
        return StoredAirplanes;
    }
    
    /**
     * Used to determine whether an airbase or aircraft carrier should be rendered on the map.
     * @param lPlayerID
     * @return whether the system contains player aircraft OR HELICOPTERS.
     */
    public boolean HostsPlayerAircraft(int lPlayerID)
    {
        for(StoredAirplane aircraft : StoredAirplanes.values())
        {
            if(aircraft.GetOwnedBy(lPlayerID))
                return true;
        }
        
        return false;
    }
    
    public boolean Full()
    {
        return GetOccupiedSlotCount() >= lAircraftSlotCount;
    }
    
    public void AddAirplane(StoredAirplane storedAircraft)
    {
        if(!Full())
        {
            StoredAirplanes.put(storedAircraft.GetID(), storedAircraft);
            Changed();
        }
    }
    
    public boolean ContainsAirplane(StoredAirplane aircraft)
    {
        return StoredAirplanes.values().contains(aircraft);
    }
    
    public void ToggleOpen()
    {
        this.bOpen = !bOpen;
    }
    
    public boolean GetOpen()
    {
        return this.bOpen;
    }
}
