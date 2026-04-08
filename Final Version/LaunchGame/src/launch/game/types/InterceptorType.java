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
public class InterceptorType extends LaunchType
{
    private static final int DATA_SIZE = 28;
    
    private int lInterceptorSpeed;
    private int lInterceptorCost;
    private int lPrepTime;
    private float fInterceptorRange;
    private boolean bABM;
    private boolean bAirLaunched;
    private float fltBlastRadius;
    private float fltYield;
    private boolean bTankLaunched;
    private boolean bShipLaunched;
    private Map<ResourceType, Long> costs;
    
    public InterceptorType(int lID, boolean bPurchasable, String strName, int lAssetID, int lInterceptorCost, int lInterceptorSpeed, float fInterceptorRange, int lPrepTime, boolean bABM, boolean bAirLaunched, float fltBlastRadius, float fltYield, boolean bTankLaunched, boolean bShipLaunched, Map<ResourceType, Long> costs)
    {
        super(lID, bPurchasable, LaunchEntity.ID_NONE, true, strName, lAssetID);
        this.lInterceptorCost = lInterceptorCost;
        this.lInterceptorSpeed = lInterceptorSpeed;
        this.fInterceptorRange = fInterceptorRange;
        this.lPrepTime = lPrepTime;
        this.bABM = bABM;
        this.bAirLaunched = bAirLaunched;
        this.fltBlastRadius = fltBlastRadius;
        this.fltYield = fltYield;
        this.bTankLaunched = bTankLaunched;
        this.bShipLaunched = bShipLaunched;
        this.costs = costs;
    }
    
    public InterceptorType(ByteBuffer bb)
    {
        super(bb);
        lInterceptorCost = bb.getInt();
        lInterceptorSpeed = bb.getInt();
        fInterceptorRange = bb.getFloat();
        lPrepTime = bb.getInt();
        bABM = (bb.get() != 0x00);
        bAirLaunched = (bb.get() != 0x00);
        fltBlastRadius = bb.getFloat();
        fltYield = bb.getFloat();
        bTankLaunched = (bb.get() != 0x00);
        bShipLaunched = (bb.get() != 0x00);
        costs = LaunchUtilities.ResourcesFromData(bb);
    }

    @Override
    public byte[] GetData()
    {
        byte[] cResourceData = LaunchUtilities.GetResourceData(costs);
        byte[] cBaseData = super.GetData();
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + cResourceData.length);
        
        bb.put(cBaseData);
        bb.putInt(lInterceptorCost);
        bb.putInt(lInterceptorSpeed);
        bb.putFloat(fInterceptorRange);
        bb.putInt(lPrepTime);
        bb.put((byte)(bABM? 0xFF : 0x00));
        bb.put((byte)(bAirLaunched? 0xFF : 0x00));
        bb.putFloat(fltBlastRadius);
        bb.putFloat(fltYield);
        bb.put((byte)(bTankLaunched? 0xFF : 0x00));
        bb.put((byte)(bShipLaunched? 0xFF : 0x00));
        bb.put(cResourceData);
        
        return bb.array();
    }
    
    public int GetInterceptorSpeed() { return lInterceptorSpeed; }
    
    public float GetInterceptorRange() { return fInterceptorRange; }
    
    public int GetInterceptorCost() { return lInterceptorCost; }
    
    public int GetPrepTime() { return lPrepTime; }
    
    public boolean GetABM() { return bABM; }
    
    public boolean GetAirLaunched() { return bAirLaunched; }
    
    public boolean GetTankLaunched() { return bTankLaunched; }
    
    public boolean GetShipLaunched() { return bShipLaunched; }
    
    public boolean GetNuclear()
    {
        return this.fltYield > 0;
    }
    
    public float GetYield() { return fltYield; }
    
    public float GetBlastRadius() { return fltBlastRadius; }
    
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
        int lMaintenance = bABM ? Defs.INTERCEPTOR_ABM_MAINTENANCE : Defs.INTERCEPTOR_MAINTENANCE;
        
        return lMaintenance;
    }
}
