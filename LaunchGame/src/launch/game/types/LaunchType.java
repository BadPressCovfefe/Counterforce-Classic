/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.types;

import java.nio.ByteBuffer;
import launch.game.entities.LaunchEntity;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public abstract class LaunchType
{
    private static final int DATA_SIZE = 14;
    
    public static final byte ASSET_ID_DEFAULT = -1;
    /**
     * Types have attributes that can either be determined from wider game settings, or be type-specific, in which case the corresponding index property is this value.
     */
    public static final byte INDEX_TYPE_OVERRIDE = -1;
    
    private int lID;
    private boolean bPurchasable;
    private int lDesignerID;                                                    //For custom types, this is the ID of the player that designed it. Developer-designed ones should have a designer ID of LaunchEntity.ID_NONE.
    private boolean bPublic;                                                    //This type is available for public use.
    private String strName;
    private int lAssetID;
    
    public LaunchType(int lID, boolean bPurchasable, int lDesignerID, boolean bPublic, String strName, int lAssetID)
    {
        this.lID = lID;
        this.bPurchasable = bPurchasable;
        this.lDesignerID = lDesignerID;
        this.bPublic = bPublic;
        this.strName = strName;
        this.lAssetID = lAssetID;
    }
    
    public LaunchType(ByteBuffer bb)
    {
        lID = bb.getInt();
        bPurchasable = bb.get() != 0x00;
        lDesignerID = bb.getInt();
        bPublic = (bb.get() != 0x00);
        strName = LaunchUtilities.StringFromData(bb);
        lAssetID = bb.getInt();
    }
    
    public int GetID() { return lID; }
    public String GetName() { return strName; }
    public int GetDesignerID() { return lDesignerID; }
    public boolean GetPublic() { return bPublic || lDesignerID == LaunchEntity.ID_NONE; }
    public boolean GetPurchasable() { return bPurchasable; }
    public int GetAssetID() { return lAssetID; }
    
    public int SetDesignerID(int lID)
    {
        this.lDesignerID = lID;
        
        return lID;
    }
    
    public boolean AvailableToPlayer(int lPlayerID)
    {
        return bPurchasable && (GetPublic() || lPlayerID == lDesignerID);
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + LaunchUtilities.GetStringDataSize(strName));
        
        bb.putInt(lID);
        bb.put((byte)(bPurchasable ? 0xFF : 0x00));
        bb.putInt(lDesignerID);
        bb.put((byte)(bPublic ? 0xFF : 0x00));
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.putInt(lAssetID);
        
        return bb.array();
    }
    
    abstract int GetHourlyMaintenance();
}
