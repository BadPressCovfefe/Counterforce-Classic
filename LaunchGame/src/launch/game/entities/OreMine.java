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
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.ResourceSystem;
import launch.utilities.ShortDelay;


/**
 *
 * @author tobster
 */
public class OreMine extends Structure
{
    private static final int DATA_SIZE = 5;
    
    private EntityType type;
    private ShortDelay dlyGenerate;
    
    /** New. */
    public OreMine(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID, boolean bRespawnProtected, int lBootTime, EntityType type, int lGenerateTime)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, bRespawnProtected, lBootTime, new ResourceSystem());
        this.type = type;
        this.dlyGenerate = new ShortDelay(lGenerateTime);
    }
    
    /** From save. */
    public OreMine(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, byte cFlags, int lStateTime, boolean bVisible, int lVisibleTime, int lBuiltByID, EntityType type, int lGenerateTime)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, cFlags, lStateTime, bVisible, lVisibleTime, lBuiltByID, new ResourceSystem());
        this.type = type;
        this.dlyGenerate = new ShortDelay(lGenerateTime);
    }
    
    /** From comms. */
    public OreMine(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        this.type = EntityType.values()[bb.get()];
        this.dlyGenerate = new ShortDelay(bb);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
    }
    
    public void Tick(int lMS, boolean bGenerate)
    {
        Tick(lMS);
        
        if(bGenerate)
        {
            dlyGenerate.Tick(lMS);
        }
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.put((byte)type.ordinal());
        dlyGenerate.GetData(bb);
        
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
        switch(type)
        {
            case SOLAR_PANEL: return "solar array";
            case FARM: return "farm";
            case ORE_MINE: return "ore mine";
            default: return "ORE MINE TYPE UNKNOWN";
        }
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof OreMine)
            return entity.GetID() == lID;
        
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return type;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.OreMine;
    }
    
    public int GetGenerateTimeRemaining()
    {
        return dlyGenerate.GetRemaining();
    }
    
    public boolean GetGenerate()
    {
        return dlyGenerate.Expired();
    }
    
    public void SetGenerateTime(int lGenerateTime)
    {
        this.dlyGenerate.Set(lGenerateTime);
    }
}
