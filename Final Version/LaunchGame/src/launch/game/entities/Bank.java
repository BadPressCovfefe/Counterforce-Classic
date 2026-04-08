/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.GeoCoord;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.ResourceSystem;

/**
 *
 * @author tobster
 */
public class Bank extends Structure
{
    private static final int DATA_SIZE = 0;
    
    /** New. */
    public Bank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
    }
    
    /** From save. */
    public Bank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, int lUsedCapacity, int lMaxCapacity, boolean bVisible, int lVisibleTime, int lBuiltByID, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.bVisible = bVisible;
    }
    
    /** From comms. */
    public Bank(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
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
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    /*public void Deposit(int lAmount)
    {
        this.lUsedCapacity += lAmount;
        Changed(false);
    }
    
    public void Withdraw(int lAmount)
    {
        this.lUsedCapacity -= lAmount;
        Changed(false);
    }
    
    public int GetRemainingCapacity()
    {
        return lMaxCapacity - lUsedCapacity;
    }
    
    public boolean Full()
    {
        return GetRemainingCapacity() >= 0;
    }
    
    public int GetWealth()
    {
        return lUsedCapacity;
    }
    
    public void SetCapacity(int lNewCapacity)
    {
        this.lMaxCapacity = lNewCapacity;
        Changed(false);
    }
    
    public int GetMaxCapacity()
    {
        return this.lMaxCapacity;
    }*/

    @Override
    public String GetTypeName()
    {
        return "bank";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Bank)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.BANK;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Bank;
    }
}
