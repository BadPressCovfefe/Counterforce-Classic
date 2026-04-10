/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities.conceptuals;

import launch.game.entities.*;
import java.nio.ByteBuffer;
import launch.game.EntityPointer;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

/**
 *
 * @author Corbin
 */
public abstract class StoredEntity extends LaunchEntity
{
    private static final int DATA_SIZE = 8;
    
    protected int lOwnerID;
    protected String strName;
    protected ShortDelay dlyPrep = new ShortDelay(); //If this storable is something that needs to be *produced,* such as a ship or aircraft, this is the prep time.
    
    /** New. */
    public StoredEntity(int lID, int lOwnerID, int lPrep)
    {
        super(lID);
        this.strName = "";
        this.lOwnerID = lOwnerID;
        this.dlyPrep = new ShortDelay(lPrep);
    }
    
    /** From save. */
    public StoredEntity(int lID, int lOwnerID, int lPrep, String strName)
    {
        super(lID);
        this.strName = strName;
        this.lOwnerID = lOwnerID;
        this.dlyPrep = new ShortDelay(lPrep);
    }
    
    public StoredEntity(ByteBuffer bb)
    {
        super(bb);
        lOwnerID = bb.getInt();
        dlyPrep = new ShortDelay(bb.getInt());
        strName = LaunchUtilities.StringFromData(bb);
    }
    
    @Override
    public void Tick(int lMS)
    {
        dlyPrep.Tick(lMS);
    }
    
    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cBaseData.length + LaunchUtilities.GetStringDataSize(strName));
        bb.put(cBaseData);
        bb.putInt(lOwnerID);
        bb.putInt(dlyPrep.GetRemaining());
        bb.put(LaunchUtilities.GetStringData(strName));
        return bb.array();
    }
    
    abstract long GetStoredWeight();
    
    public boolean DoneBuilding()
    {
        return dlyPrep.Expired();
    }
    
    public int GetPrepRemaining()
    {
        return dlyPrep.GetRemaining();
    }
    
    public String GetName() { return strName; }
    
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
    
    @Override
    public int GetOwnerID()
    {
        return this.lOwnerID;
    }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }
    
    public abstract EntityPointer GetHost();
}
