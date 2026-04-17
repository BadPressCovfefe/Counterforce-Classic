/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;

/**
 *
 * @author Corbin
 */
public class Warehouse extends Structure
{
    private static final int DATA_SIZE = 8;
    
    private long oWealth;
    
    /** New. */
    public Warehouse(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, null);
        this.oWealth = 0;
    }
    
    /** From save. */
    public Warehouse(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, int lBuildTime, boolean bVisible, int lVisibleTime, int lBuiltByID, long oWealth)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, null);
        this.bVisible = bVisible;
        this.oWealth = oWealth;
    }
    
    /** From comms. */
    public Warehouse(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.oWealth = bb.getLong();
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE);
        bb.put(cBaseData);
        bb.putLong(oWealth);
        
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
        return "bank";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Warehouse)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.WAREHOUSE;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Warehouse;
    }
    
    public void Deposit(long oAmount)
    {
        this.oWealth += oAmount;
        Changed(false);
    }
    
    public void Withdraw(long oAmount)
    {
        this.oWealth -= oAmount;
        Changed(false);
    }
    
    public long GetRemainingCapacity()
    {
        return GetMaxCapacity() - oWealth;
    }
    
    public boolean Full()
    {
        return GetRemainingCapacity() >= 0;
    }
    
    public long GetWealth()
    {
        return oWealth;
    }
    
    public long GetMaxCapacity()
    {
        return Defs.BANK_CAPACITY;
    }
}
