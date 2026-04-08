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
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.ResourceSystem;

import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Warehouse extends Structure implements HaulerInterface, LaunchSystemListener
{
    private static final int DATA_SIZE = 5;
    
    private boolean bProducing;
    private ShortDelay dlyBuild;
    private CargoSystem storage;
    
    /** New. */
    public Warehouse(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.bProducing = false;
        this.dlyBuild = new ShortDelay();
        this.storage = new CargoSystem(this, Defs.WAREHOUSE_MAX_STORAGE_KG);
    }
    
    /** From save. */
    public Warehouse(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, boolean bProducing, int lBuildTime, boolean bVisible, int lVisibleTime, int lBuiltByID, CargoSystem storage, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.bVisible = bVisible;
        this.bProducing = bProducing;
        this.dlyBuild = new ShortDelay(lBuildTime);
        this.storage = storage;
        storage.SetSystemListener(this);
    }
    
    /** From comms. */
    public Warehouse(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.bProducing = (bb.get() != 0x00);
        this.dlyBuild = new ShortDelay(bb.getInt());
        storage = new CargoSystem(this, bb);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        dlyBuild.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cCargoSystemData = storage.GetData(lAskingID);
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE + cCargoSystemData.length);
        bb.put(cBaseData);
        bb.put((byte)(bProducing? 0xFF : 0x00));
        bb.putInt(dlyBuild.GetRemaining());
        bb.put(cCargoSystemData);
        
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
        return "warehouse";
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
    
    @Override
    public CargoSystem GetCargoSystem()
    {
        return storage;
    }

    @Override
    public void SystemChanged(LaunchSystem system) 
    {
        Changed(false);
    }
    
    public void TruckProduced()
    {
        bProducing = false;
        Changed(true);
    }
    
    public boolean GetProducing()
    {
        return this.bProducing;
    }
    
    public int GetProdTimeRemaining()
    {
        return dlyBuild.GetRemaining();
    }
    
    public void SetProducing(int lBuildTime)
    {
        bProducing = true;
        dlyBuild.Set(lBuildTime);
        Changed(true);
    }
    
    public boolean ProductionFinished()
    {
        return bProducing && dlyBuild.Expired();
    }
}
