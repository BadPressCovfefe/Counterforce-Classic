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

/**
 *
 * @author tobster
 */
public class KOTH extends MapEntity
{
    private static final int DATA_SIZE = 9;
    
    public static final int ID_CONTESTED = -1; 
    public static final int ID_EMPTY = -2; 
    
    private float fltRadius;
    private int lKingID;
    private boolean bControlledByAlliance;

    public KOTH(int lID, GeoCoord geoPosition, float fltRadius, int lKingID, boolean bControlledByAlliance)
    {
        super(lID, geoPosition, true, 0);
        this.fltRadius = fltRadius;
        this.lKingID = lKingID;
        this.bControlledByAlliance = bControlledByAlliance;
    }
    
    public KOTH(ByteBuffer bb)
    {
        super(bb);
        this.fltRadius = bb.getFloat();
        this.lKingID = bb.getInt();
        this.bControlledByAlliance = (bb.get() != 0x00);
    }

    @Override
    public void Tick(int lMS)
    {

    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte cBaseData[] = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length);
        bb.put(cBaseData);
        bb.putFloat(fltRadius);
        bb.putInt(lKingID);
        bb.put((byte)(bControlledByAlliance ? 0xFF : 0x00));
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public float GetRadius() { return this.fltRadius; }
    
    public void SetRadius(float fltNewRadius)
    {
        this.fltRadius = fltNewRadius;
        Changed(true);
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return true;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof KOTH)
            return entity.GetID() == lID;
        
        return false;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.KOTH;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.KOTH;
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    @Override
    public String GetTypeName()
    {
        return "hill";
    }
    
    public int GetKingID()
    {
        return lKingID;
    }
    
    public void SetKing(int lKingID, boolean bAlliance)
    {
        this.lKingID = lKingID;
        
        bControlledByAlliance = bAlliance;
    }
    
    public void SetContested()
    {
        lKingID = ID_CONTESTED;
    }
    
    public void SetEmpty()
    {
        lKingID = ID_EMPTY;
    }
    
    public boolean GetContested()
    {
        return lKingID == ID_CONTESTED;
    }
    
    public boolean GetEmpty()
    {
        return lKingID == ID_EMPTY;
    }
    
    public boolean GetOccupiedByAlliance()
    {
        return this.bControlledByAlliance && lKingID != ID_EMPTY && lKingID != ID_CONTESTED;
    }
}
