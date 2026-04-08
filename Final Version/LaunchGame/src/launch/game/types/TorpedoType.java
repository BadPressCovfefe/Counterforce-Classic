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
    private static final int DATA_SIZE = 17;
    
    private boolean bNuclear;
    private boolean bHoming;
    private boolean bAirLaunched;
    private boolean bNavalLaunched;
    private float fTorpedoRange;
    private float fTorpedoBlastRadius;
    private int lYield;
    private int lTorpedoSpeed;
    private Map<ResourceType, Long> costs;
    
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
            Map<ResourceType, Long> costs)
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
        this.costs = costs;
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
        costs = LaunchUtilities.ResourcesFromData(bb);
    }

    @Override
    public byte[] GetData()
    {
        byte[] cResourceData = LaunchUtilities.GetResourceData(costs);
        byte[] cBaseData = super.GetData();
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cResourceData.length);
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
        bb.put(cResourceData);
        
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
    
    public Map<ResourceType, Long> GetCosts() 
    {
        Map<ResourceType, Long> normalCosts = new ConcurrentHashMap<>();
        
        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            if(cost.getKey() == ResourceType.WEALTH || cost.getKey() == ResourceType.ENRICHED_URANIUM)
            {
                normalCosts.put(cost.getKey(), cost.getValue());
            }
        }
        
        return normalCosts;
    }
    
    public Map<ResourceType, Long> GetFactoryCosts()
    {
        Map<ResourceType, Long> factoryCosts = new ConcurrentHashMap<>();
        
        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            if(cost.getKey() != ResourceType.WEALTH)
            {
                factoryCosts.put(cost.getKey(), cost.getValue());
            }
        }
        
        return factoryCosts;
    }
    
    @Override
    public int GetHourlyMaintenance()
    {
        int lMaintenance = Defs.TORPEDO_MAINTENANCE;
        
        return lMaintenance;
    }
}
