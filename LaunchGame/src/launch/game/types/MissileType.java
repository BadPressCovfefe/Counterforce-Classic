/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.types;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.Defs;
import launch.game.entities.LaunchEntity;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.utilities.LaunchUtilities;
/**
 *
 * @author tobster
 */
public class MissileType extends LaunchType
{    
    private static final int DATA_SIZE = 34;
    
    public enum Warhead
    {
        CONVENTIONAL,
        NUCLEAR,
        EMP,
        SONOBUOY,
        NEUTRON,
    }
    
    private boolean bNuclear;
    private boolean bTracking;
    private boolean bECM;
    private boolean bAirLaunched;
    private boolean bGroundLaunched;
    private boolean bICBM;
    private boolean bStealth;
    private boolean bTankLaunched;
    private boolean bShipLaunched;
    private boolean bSubmarineLaunched;
    private boolean bAntiShip;
    private boolean bAntiSubmarine;
    private boolean bArtillery;
    private boolean bBomb;
    private boolean bBunkerBuster;
    private boolean bSonobuoy;
    private float fltMissileRange;
    private float fltEMPRadius;
    private int lYield;
    private float fltAccuracy;
    private float fltMissileSpeed;
    private int lTorpedoType;
    private long oCost;
    
    public MissileType(
            int lID, 
            boolean bPurchasable, 
            String strName, 
            int lAssetID, 
            boolean bNuclear, 
            boolean bTracking, 
            boolean bECM, 
            boolean bAirLaunched, 
            boolean bGroundLaunched,
            boolean bArtillery,
            float fltMissileSpeed, 
            float fltMissileRange,
            boolean bICBM, 
            float fltEMPRadius, 
            boolean bStealth, 
            int lYield, 
            float fltAccuracy,
            boolean bTankLaunched,
            boolean bShipLaunched,
            boolean bSubmarineLaunched,
            boolean bAntiShip,
            boolean bAntiSubmarine,
            boolean bBomb,
            boolean bBunkerBuster,
            int lTorpedoType,
            boolean bSonobuoy,
            long oCost)
    {
        super(lID, bPurchasable, LaunchEntity.ID_NONE, true, strName, lAssetID);
        this.bNuclear = bNuclear;
        this.bTracking = bTracking;
        this.bECM = bECM;
        this.bAirLaunched = bAirLaunched;
        this.bGroundLaunched = bGroundLaunched;
        this.bArtillery = bArtillery;
        this.fltMissileSpeed = fltMissileSpeed;
        this.fltMissileRange = fltMissileRange;
        this.bICBM = bICBM;
        this.fltEMPRadius = fltEMPRadius;
        this.bStealth = bStealth;
        this.lYield = lYield;
        this.fltAccuracy = fltAccuracy;
        this.bTankLaunched = bTankLaunched;
        this.bShipLaunched = bShipLaunched;
        this.bSubmarineLaunched = bSubmarineLaunched;
        this.bAntiShip = bAntiShip;
        this.bAntiSubmarine = bAntiSubmarine;
        this.bBomb = bBomb;
        this.bBunkerBuster = bBunkerBuster;
        this.lTorpedoType = lTorpedoType;
        this.bSonobuoy = bSonobuoy;
        this.oCost = oCost;
    }
    
    public MissileType(ByteBuffer bb)
    {
        super(bb);
        
        byte cFlags1 = bb.get();
        
        bTankLaunched = (cFlags1 & 0x01) != 0x00;
        bShipLaunched = (cFlags1 & 0x02) != 0x00;
        bSubmarineLaunched = (cFlags1 & 0x04) != 0x00;
        bAntiShip = (cFlags1 & 0x08) != 0x00;
        bAntiSubmarine = (cFlags1 & 0x10) != 0x00;
        bArtillery = (cFlags1 & 0x20) != 0x00;
        bBomb = (cFlags1 & 0x40) != 0x00;
        bBunkerBuster = (cFlags1 & 0x80) != 0x00;
        
        byte cFlags2 = bb.get();
        
        bSonobuoy = (cFlags2 & 0x01) != 0x00;
        bStealth = (cFlags2 & 0x02) != 0x00;
        bICBM = (cFlags2 & 0x04) != 0x00;
        bNuclear = (cFlags2 & 0x08) != 0x00;
        bTracking = (cFlags2 & 0x10) != 0x00;
        bECM = (cFlags2 & 0x20) != 0x00;
        bAirLaunched = (cFlags2 & 0x40) != 0x00;
        bGroundLaunched = (cFlags2 & 0x80) != 0x00;
        
        fltMissileSpeed = bb.getFloat();
        fltMissileRange = bb.getFloat();
        fltEMPRadius = bb.getFloat();
        lYield = bb.getInt();
        fltAccuracy = bb.getFloat();
        lTorpedoType = bb.getInt();
        oCost = bb.getLong();
    }

    @Override
    public byte[] GetData()
    {
        byte[] cBaseData = super.GetData();
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        
        byte cFlags1 = 0;
        
        if(bTankLaunched) cFlags1 |= 0x01;
        if(bShipLaunched) cFlags1 |= 0x02;
        if(bSubmarineLaunched) cFlags1 |= 0x04;
        if(bAntiShip) cFlags1 |= 0x08;
        if(bAntiSubmarine) cFlags1 |= 0x10;
        if(bArtillery) cFlags1 |= 0x20;
        if(bBomb) cFlags1 |= 0x40;
        if(bBunkerBuster) cFlags1 |= 0x80;
            
        byte cFlags2 = 0;
        
        if(bSonobuoy) cFlags2 |= 0x01;
        if(bStealth) cFlags2 |= 0x02;
        if(bICBM) cFlags2 |= 0x04;
        if(bNuclear) cFlags2 |= 0x08;
        if(bTracking) cFlags2 |= 0x10;
        if(bECM) cFlags2 |= 0x20;
        if(bAirLaunched) cFlags2 |= 0x40;
        if(bGroundLaunched) cFlags2 |= 0x80;
        
        bb.put(cFlags1);
        bb.put(cFlags2);
        bb.putFloat(fltMissileSpeed);
        bb.putFloat(fltMissileRange);
        bb.putFloat(fltEMPRadius);
        bb.putInt(lYield);
        bb.putFloat(fltAccuracy);
        bb.putInt(lTorpedoType);
        bb.putLong(oCost);        
        
        return bb.array();
    }
    
    public int GetYield() { return this.lYield; }
    
    public int GetTorpedoType() { return lTorpedoType; }
    
    public boolean GetStealth() { return bStealth; }
    
    public boolean GetNuclear() { return bNuclear; }
    
    public boolean GetTracking() { return bTracking; }
    
    public boolean GetECM() { return bECM; }
    
    public boolean GetAirLaunched() { return bAirLaunched; }
    
    public boolean GetGroundLaunched() { return bGroundLaunched; }
    
    public boolean GetTankLaunched() { return bTankLaunched; }
    
    public boolean GetShipLaunched() { return bShipLaunched; }
    
    public boolean GetSubmarineLaunched() { return bSubmarineLaunched; }
    
    public boolean GetArtillery() { return bArtillery; }
    
    public boolean GetAntiShip() { return bAntiShip; }
    
    public boolean GetAntiSubmarine() { return bAntiSubmarine; }
    
    public boolean GetBunkerBuster() { return bBunkerBuster; }
    
    public boolean GetBomb() { return bBomb; }
    
    public float GetMissileSpeed() { return fltMissileSpeed; }
    
    public float GetMissileRange() { return fltMissileRange; }
    
    public boolean GetICBM() { return bICBM; }
    
    public float GetEMPRadius() { return fltEMPRadius; }
    
    public float GetAccuracy() { return fltAccuracy; }
    
    public boolean GetSonobuoy() { return this.bSonobuoy; }
    
    public long GetCost()
    {
        return this.oCost;
    }
    
    @Override
    public int GetHourlyMaintenance()
    {
        int lMaintenance = bICBM ? Defs.MISSILE_ICBM_MAINTENANCE : Defs.MISSILE_MAINTENANCE;
        
        return lMaintenance;
    }
}
