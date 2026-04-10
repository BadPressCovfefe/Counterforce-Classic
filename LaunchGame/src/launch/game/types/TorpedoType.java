/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.types;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.Defs;
import launch.game.entities.LaunchEntity;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public class TorpedoType extends LaunchType
{    
    private static final int DATA_SIZE = 25;
    
    private boolean bNuclear;
    private boolean bHoming;
    private boolean bAirLaunched;
    private boolean bNavalLaunched;
    private float fTorpedoRange;
    private float fTorpedoBlastRadius;
    private int lYield;
    private int lTorpedoSpeed;
    private long oCost;
    
    public TorpedoType(
            int lID, 
            boolean bPurchasable, 
            String strName, 
            int lAssetID, 
            boolean bNuclear, 
            boolean bHoming,
            boolean bAirLaunched,
            boolean bNavalLaunched,
            float fTorpedoRange, 
            float fTorpedoBlastRadius,
            int lYield,
            int lTorpedoSpeed,
            long oCost)
    {
        super(lID, bPurchasable, LaunchEntity.ID_NONE, true, strName, lAssetID);
        this.bNuclear = bNuclear;
        this.bHoming = bHoming;
        this.bAirLaunched = bAirLaunched;
        this.bNavalLaunched = bNavalLaunched;
        this.fTorpedoRange = fTorpedoRange;
        this.fTorpedoBlastRadius = fTorpedoBlastRadius;
        this.lYield = lYield;
        this.lTorpedoSpeed = lTorpedoSpeed;
        this.oCost = oCost;
    }
    
    public TorpedoType(ByteBuffer bb)
    {
        super(bb);
        byte cFlags = bb.get();
        
        bNuclear = (cFlags & 0x01) != 0x00;
        bHoming = (cFlags & 0x02) != 0x00;
        bAirLaunched = (cFlags & 0x04) != 0x00;
        bNavalLaunched = (cFlags & 0x08) != 0x00;
        
        fTorpedoRange = bb.getFloat();
        fTorpedoBlastRadius = bb.getFloat();
        lYield = bb.getInt();
        lTorpedoSpeed = bb.getInt();
        oCost = bb.getLong();
    }

    @Override
    public byte[] GetData()
    {
        byte[] cBaseData = super.GetData();
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        
        byte cFlags = 0x00;
        
        if(bNuclear) cFlags |= 0x01;
        if(bHoming) cFlags |= 0x02;
        if(bAirLaunched) cFlags |= 0x04;
        if(bNavalLaunched) cFlags |= 0x08;
            
        bb.put(cFlags);
        bb.putFloat(fTorpedoRange);
        bb.putFloat(fTorpedoBlastRadius);
        bb.putInt(lYield);
        bb.putInt(lTorpedoSpeed);
        bb.putLong(oCost);
        
        return bb.array();
    }
    
    public int GetYield()
    {
        return this.lYield;
    }
    
    public boolean GetNuclear() { return bNuclear; }
    
    public boolean GetHoming() { return bHoming; }
    
    public boolean GetAirLaunched() { return bAirLaunched; }
    
    public boolean GetNavalLaunched() { return bNavalLaunched; }
    
    public int GetTorpedoSpeed() { return lTorpedoSpeed; }
    
    public float GetTorpedoRange() { return fTorpedoRange; }
    
    public float GetBlastRadius() { return fTorpedoBlastRadius; }
    
    public long GetCost() { return oCost; }
    
    @Override
    public int GetHourlyMaintenance()
    {
        int lMaintenance = Defs.TORPEDO_MAINTENANCE;
        
        return lMaintenance;
    }
}
