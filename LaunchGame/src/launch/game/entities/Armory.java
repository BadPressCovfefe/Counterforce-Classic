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
public class Armory extends Structure implements HaulerInterface, LaunchSystemListener
{
    public static final int DATA_SIZE = 7;
    
    private boolean bBarracks;
    private EntityType type;
    private boolean bProducing;
    private ShortDelay dlyBuild; 
    private CargoSystem cargo;
    
    /** New. */
    public Armory(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, boolean bBarracks, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, resources);
        this.bBarracks = bBarracks;
        this.bProducing = false;
        this.dlyBuild = new ShortDelay();
        this.bVisible = false;
        this.type = EntityType.MBT;
        this.cargo = new CargoSystem(this, 0);
    }
    
    /** From save. */
    public Armory(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, int lBuildTime, boolean bProducing, EntityType type, boolean bVisible, int lVisibleTime, CargoSystem cargo, int lBuiltByID, boolean bBarracks, ResourceSystem resources)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, resources);
        this.bBarracks = bBarracks;
        this.bProducing = bProducing;
        this.dlyBuild = new ShortDelay(lBuildTime);
        this.bVisible = bVisible;
        this.type = type;
        this.cargo = cargo;
        
        if(this.cargo == null)
            this.cargo = new CargoSystem(this, 0);
        
        this.cargo.SetSystemListener(this);
    }
    
    /** From comms. */
    public Armory(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        bBarracks = (bb.get() != 0x00);
        dlyBuild = new ShortDelay(bb.getInt());
        bProducing = (bb.get() != 0x00);
        type = EntityType.values()[bb.get()];
        cargo = new CargoSystem(this, bb);
        SetPointer(); //To prevent the client from getting EntityType.ARMORY when it should be barracks. Not sure why this was necessary here but for, say, ICBM silos/missile sites.
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
        byte[] cCargoSystemData = cargo.GetData(lAskingID);
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE + cCargoSystemData.length);
        bb.put(cBaseData);
        bb.put((byte)(bBarracks ? 0xFF : 0x00));
        dlyBuild.GetData(bb);
        bb.put((byte)(bProducing? 0xFF : 0x00));
        bb.put((byte)type.ordinal());
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
        return bBarracks ? "barracks" : "armory";
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Armory)
            return entity.GetID() == lID;
        
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return bBarracks ? EntityType.BARRACKS : EntityType.ARMORY;
    }
    
    @Override
    public int GetSessionCode()
    {
        return bBarracks ? LaunchSession.Barracks : LaunchSession.Armory;
    }
    
    public void TankProduced()
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
    
    public void SetProducing(EntityType type, int lBuildTime)
    {
        this.type = type;
        bProducing = true;
        dlyBuild.Set(lBuildTime);
        Changed(true);
    }
    
    public boolean ProductionFinished()
    {
        return bProducing && dlyBuild.Expired();
    }
    
    public EntityType GetProducingType()
    {
        return type;
    }

    @Override
    public CargoSystem GetCargoSystem() 
    {
        return cargo;
    }

    @Override
    public void SystemChanged(LaunchSystem system) 
    {
        Changed(false);
    }
    
    public boolean GetIsBarracks()
    {
        return this.bBarracks;
    }
}
