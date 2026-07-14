/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.systems.ResourceSystem;
import launch.utilities.ShortDelay;


/**
 *
 * @author tobster
 */
public class LogisticsDepot extends Structure
{
    private static final int DATA_SIZE = 8;
    
    private int lWealth;
    private ShortDelay dlyReload;
    
    /** New. */
    public LogisticsDepot(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, new ResourceSystem());
        this.dlyReload = new ShortDelay();
    }
    
    /** From save. */
    public LogisticsDepot(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, boolean bVisible, int lVisibleTime, int lBuiltByID, int lWealth, int lReload)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, new ResourceSystem());
        this.lWealth = lWealth;
        this.dlyReload = new ShortDelay(lReload);
    }
    
    /** From comms. */
    public LogisticsDepot(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.lWealth = bb.getInt();
        this.dlyReload = new ShortDelay(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        dlyReload.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        
        bb.put(cBaseData);
        bb.putInt(lWealth);
        dlyReload.GetData(bb);
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public String GetTypeName()
    {
        return "scrap yard";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof LogisticsDepot)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.LOGISTICS_DEPOT;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.LogisticsDepot;
    }
    
    public int GetWealth()
    {
        return this.lWealth;
    }
    
    public int GetRemainingCapacity()
    {
        return Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY - this.lWealth;
    }
    
    public void AddWealth(int lWealth)
    {
        if(this.lWealth + lWealth > Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY)
        {
            this.lWealth = Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY;
        }
        else 
        {
            this.lWealth += lWealth;
        }
    }
    
    public boolean Full()
    {
        return this.lWealth == Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY;
    }
    
    public void TakeWealth()
    {
        this.lWealth = 0;
    }
    
    public boolean GetReadyToCollect()
    {
        return this.dlyReload.Expired();
    }
    
    public int GetCollectCooldownRemaining()
    {
        return this.dlyReload.GetRemaining();
    }
    
    public void Collected(int lCooldown)
    {
        this.dlyReload.Set(lCooldown);
    }
}
