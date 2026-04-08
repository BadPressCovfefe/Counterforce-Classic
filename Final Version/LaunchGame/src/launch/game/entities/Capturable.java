/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author Corbin
 */
public abstract class Capturable extends Damagable
{
    private static final int DATA_SIZE = 5;
    
    protected int lOwnerID;
    private String strName;
    boolean bContested;
    
    public Capturable(int lID, GeoCoord geoPosition, String strName, short nHP, short nMaxHP, int lOwnerID, boolean bContested)
    {
        super(lID, geoPosition, nHP, nMaxHP, true, 0);
        this.strName = strName;
        this.lOwnerID = lOwnerID;
        this.bContested = bContested;
    }
    
    public Capturable(ByteBuffer bb)
    {
        super(bb);
        lOwnerID = bb.getInt();
        strName = LaunchUtilities.StringFromData(bb);
        bContested = (bb.get() != 0x00);
    }
    
    /**
     * Get data to communicate, base class. Subclasses should override and super call this method.
     * @param lAskingID The ID of the player this data will be sent to.
     * @return The data to communicate.
     */
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + LaunchUtilities.GetStringDataSize(strName));
        bb.put(cBaseData);
        bb.putInt(lOwnerID);
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.put((byte)(bContested ? 0xFF : 0x00));
        
        return bb.array();
    }
    
    @Override
    public void Tick(int lMS)
    {
        
    }
    
    public boolean IsCaptured()
    {
        return this.lOwnerID != LaunchEntity.ID_NONE;
    }
    
    public void Capture(int lCapturedByID)
    {
        this.lOwnerID = lCapturedByID;
        this.bContested = false;
        Changed(false);
    }
    
    public void Abandon()
    {
        this.lOwnerID = LaunchEntity.ID_NONE;
        Changed(false);
    }
    
    public String GetName()
    {
        return strName;
    }
    
    @Override
    public boolean GetOwnedBy(int lID) 
    {
        return lOwnerID == lID;
    }

    @Override
    public int GetOwnerID() 
    {
        return lOwnerID;
    }
    
    public boolean GetContested()
    {
        return bContested;
    }
    
    public void SetContested(boolean bContested)
    {
        this.bContested = bContested;
        Changed(false);
    }
    
    public String GetTypeName()
    {
        return strName;
    }
}
