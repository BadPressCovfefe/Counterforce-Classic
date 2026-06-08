/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

import java.nio.ByteBuffer;
import java.util.Map;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.ResourceSystem;

import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public class Tank extends LandUnit implements NamableInterface
{
    private static final int DATA_SIZE = 9;
    
    private ShortDelay dlyReload;
    private boolean bSelling;
    private ShortDelay dlySelling;
    
    /** New. */
    public Tank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, int lOwnerID)
    {
        super(lID, geoPosition, nHP, nMaxHP, lOwnerID, new ResourceSystem());
        dlyReload = new ShortDelay();
        dlySelling = new ShortDelay();
        SetPointer();
    }
    
    /** From save. */
    public Tank(int lID, GeoCoord geoPosition, short nHP, short nMaxHP, String strName, int lOwnerID, int lUnderAttack, MoveOrders moveOrder, GeoCoord geoTarget, int lReloadRemaining, boolean bVisible, int lVisibleTime, boolean bSelling, int lSelling, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, strName, lOwnerID, lUnderAttack, moveOrder, geoTarget, null, bVisible, lVisibleTime, new ResourceSystem(), 0.0f, Coordinates);
        dlyReload = new ShortDelay(lReloadRemaining);
        this.bSelling = bSelling;
        dlySelling = new ShortDelay(lSelling);
        
        SetPointer();
    }
    
    /** From comms. */
    public Tank(ByteBuffer bb, int lReceivingID)
    {
        super(bb, lReceivingID);
        dlyReload = new ShortDelay(bb);
        bSelling = (bb.get() != 0x00);
        dlySelling = new ShortDelay(bb);
        
        SetPointer();
    }
    
    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        dlyReload.Tick(lMS);
        
        if(bSelling)
        {
            dlySelling.Tick(lMS);
        }
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.putInt(dlyReload.GetRemaining());
        bb.put((byte)(bSelling ? 0xFF : 0x00));
        bb.putInt(dlySelling.GetRemaining());
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    @Override
    public boolean IsCivilian()
    {
        return false;
    }
    
    @Override
    public String GetTypeName()
    {
        return "tank";
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Tank;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.TANK;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Tank)
            return entity.GetID() == lID;
        return false;
    }
    
    @Override
    public void MoveToPosition(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.MOVE;
        
        Changed(false);
    }
    
    @Override
    public void AttackTarget(EntityPointer target)
    {
        this.target = new EntityPointer(target.GetID(), target.GetType());
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    @Override
    public void CaptureTarget(EntityPointer target)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void ProvideRefueling(EntityPointer refuelee)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void SeekRefueling(EntityPointer tanker)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void UnloadLoot(EntityPointer receiver, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void UnloadLoot(GeoCoord geoDropOff, LootType typeToDeliver, int lTypeToDeliver, int lQuantityToDeliver)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void LoadLoot(EntityPointer loot)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void LiberateTarget(EntityPointer target)
    {
        //Tanks don't do this.
    }
    
    @Override
    public void DefendPosition()
    {
        this.moveOrders = MoveOrders.DEFEND;
        Changed(false);
    }
    
    public boolean ReadyToFire()
    {
        return this.moveOrders == MoveOrders.DEFEND;
    }
    
    @Override
    public void SetName(String strName)
    {
        super.SetName(strName);
    }
    
    public void SetReloadTime(int lTime)
    {
        dlyReload.Set(lTime);
        Changed(false);
    }
    
    public boolean GetCanFire()
    {
        return dlyReload.Expired();
    }
    
    public int GetReloadTimeRemaining()
    {
        return dlyReload.GetRemaining();
    }
    
    @Override
    public void AttackTarget(GeoCoord geoTarget)
    {
        this.geoTarget = geoTarget;
        this.moveOrders = MoveOrders.ATTACK;
        Changed(false);
    }
    
    public boolean GetSelling()
    {
        return this.bSelling;
    }
    
    public boolean GetSellTimeExpired()
    {
        return dlySelling.Expired();
    }
    
    public void Sell(int lSellTime)
    {
        bSelling = true;
        dlySelling.Set(lSellTime);
    }
    
    public void CancelSale()
    {
        bSelling = false;
    }
    
    public int GetSellTimeRemaining()
    {
        return dlySelling.GetRemaining();
    }
    
    @Override
    public boolean GetVisible()
    {
        return true;
    }
}
