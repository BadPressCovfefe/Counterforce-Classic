/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.Haulable;
import launch.game.entities.LaunchEntity;
import launch.game.entities.NamableInterface;
import launch.game.entities.ResourceInterface;
import launch.game.entities.SAMSite;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.game.systems.ResourceSystem;


/**
 *
 * @author Corbin
 */
public class StoredTank extends StoredDamagable implements LaunchSystemListener, NamableInterface, TankInterface, Haulable, ResourceInterface
{
    private static final int DATA_SIZE = 12;
    private byte cMode;
    private EntityType type;
    private MissileSystem launchables;
    private EntityPointer host;
    private float fltFuel;
    private ResourceSystem resources;
    
    /** From save. */
    public StoredTank(int lID, short nHP, short nMaxHP, String strName, int lOwnerID, int lPrep, byte cMode, EntityType type, MissileSystem missileSystem, EntityPointer host, ResourceSystem resources, float fltFuel)
    {
        super(lID, lOwnerID, nHP, nMaxHP, lPrep, strName);
        this.type = type;
        this.host = host;
        this.launchables = missileSystem;
        launchables.SetSystemListener(this);
        this.fltFuel = fltFuel;
        this.resources = resources;
        SetPointer();
    }
    
    /** From missile tank. */
    public StoredTank(int lNewID, Tank tank, EntityPointer host)
    {
        super(lNewID, tank.GetOwnerID(), tank.GetHP(), Defs.TANK_MAX_HP, 0, tank.GetName());
        this.type = tank.GetType();
        this.host = host;
        this.launchables = tank.GetMissileSystem();
        this.fltFuel = tank.GetCurrentFuel();
        this.resources = tank.GetResourceSystem();
        SetPointer();
    }
    
    /** From comms. */
    public StoredTank(ByteBuffer bb)
    {
        super(bb);
        cMode = bb.get();
        type = EntityType.values()[bb.get()];
        host = new EntityPointer(bb);
        fltFuel = bb.getFloat();
        resources = new ResourceSystem(bb);
        
        if(bb.get() != 0x00)
            launchables = new MissileSystem(this, bb);
        
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        if(launchables != null)
            launchables.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cResourceData = resources.GetData();
        byte[] cMissileSystemData = launchables != null ? launchables.GetData(lAskingID) : new byte[0];
        byte[] cBaseData = super.GetData(lAskingID);
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cMissileSystemData.length + cResourceData.length);
        bb.put(cBaseData);
        bb.put(cMode);
        bb.put((byte)type.ordinal());
        bb.put(host.GetData());
        bb.putFloat(fltFuel);
        bb.put(cResourceData);
        bb.put((byte)(launchables != null ? 0xFF : 0x00));
        
        if(launchables != null)
            bb.put(cMissileSystemData);
        
        SetPointer();
        
        return bb.array();
    }
    
    @Override
    public long GetWeight()
    {
        return 1;
    }
    
    @Override
    public long GetStoredWeight()
    {
        return GetWeight();
    }

    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof StoredTank)
            return entity.GetID() == lID;
        return false;
    }

    @Override
    public EntityPointer.EntityType GetEntityType()
    {
        return EntityType.STORED_TANK;
    }

    @Override
    public int GetSessionCode()
    {
        return LaunchSession.StoredTank;
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        Changed(false);
    }
    
    @Override
    public long GetQuantity()
    {
        return 1; //Tanks are not stackable.
    }
    
    @Override
    public boolean IsAnMBT()
    {
        return type == EntityType.MBT;
    }
    
    @Override
    public boolean IsASPAAG()
    {
        return type == EntityType.SPAAG;
    }
    
    @Override
    public boolean IsMissiles()
    {
        return type == EntityType.MISSILE_TANK;
    }
    
    @Override
    public boolean IsInterceptors()
    {
        return type == EntityType.SAM_TANK;
    }
    
    @Override
    public boolean IsArtillery()
    {
        return type == EntityType.HOWITZER;
    }
    
    @Override
    public boolean HasMissiles()
    {
        return IsMissiles() && launchables != null;
    }
    
    @Override
    public boolean HasInterceptors()
    {
        return IsInterceptors() && launchables != null;
    }
    
    @Override
    public boolean HasArtillery()
    {
        return IsArtillery() && launchables != null;
    }
    
    @Override
    public String GetTypeName()
    {
        switch(type)
        {
            case MBT: return "main battle tank";
            case SPAAG: return "SPAAG";
            case MISSILE_TANK: return "missile artillery";
            case SAM_TANK: return "SAM artillery";
            case HOWITZER: return "self-propelled howitzer";
        }
        
        return "UNKNOWN TANK TYPE";
    }
    
    @Override
    public LootType GetLootType()
    {
        return LootType.STORED_TANK;
    }

    @Override
    public int GetCargoID()
    {
        return lID;
    }
    
    @Override
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
    
    public void SetHost(EntityPointer newHost)
    {
        this.host = newHost;
    }
    
    @Override
    public EntityPointer GetHost()
    {
        return this.host;
    }
    
    public boolean Capture(int lPlayerID)
    {
        if(lOwnerID != lPlayerID)
        {
            lOwnerID = lPlayerID;
            Changed(false);
            return true;
        }
        
        return false;
    }
    
    @Override
    public LaunchEntity GetTank()
    {
        return this;
    }
    
    @Override
    public EntityType GetType()
    {
        return this.type;
    }
    
    @Override
    public MissileSystem GetMissileSystem()
    {
        return this.launchables;
    }
    
    @Override
    public boolean GetAuto() { return cMode == SAMSite.MODE_AUTO; }
    
    @Override
    public boolean GetSemiAuto() { return cMode == SAMSite.MODE_SEMI_AUTO; }
    
    @Override
    public boolean GetManual() { return cMode == SAMSite.MODE_MANUAL; }
    
    @Override
    public byte GetMode() { return cMode; }
    
    @Override
    public void SetMode(byte cMode)
    {
        this.cMode = cMode;
        Changed(true);
    }
    
    public float GetCurrentFuel()
    {
        return this.fltFuel;
    }
    
    @Override
    public ResourceSystem GetResourceSystem()
    {
        return resources;
    }
    
    public float GetFuelDeficit()
    {
        return 1.0f - fltFuel;
    }
}
