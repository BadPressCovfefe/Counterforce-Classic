/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.systems;

import java.nio.ByteBuffer;
import java.util.List;
import launch.game.systems.LaunchSystem.SystemType;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public class LaunchableTransfer
{
    private static final int DATA_SIZE = 10;
    
    private SystemType transferFromType;
    private int lTransferFromID;
    private List<Integer> TypesList;
    private SystemType transferToType;
    private int lTransferToID;
    
    /** New. */
    public LaunchableTransfer(SystemType transferFromType, int lTransferFromID, List<Integer> TypesList, SystemType transferToType, int lTransferToID)
    {
        this.transferFromType = transferFromType;
        this.lTransferFromID = lTransferFromID;
        this.TypesList = TypesList;
        this.transferToType = transferToType;
        this.lTransferToID = lTransferToID;
    }
    
    /** From comms. */
    public LaunchableTransfer(ByteBuffer bb)
    {
        this.transferFromType = SystemType.values()[bb.get()];
        this.lTransferFromID = bb.getInt();
        this.TypesList = LaunchUtilities.IntListFromData(bb);
        this.transferToType = SystemType.values()[bb.get()];
        this.lTransferToID = bb.getInt();
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + (TypesList.size() * 4));
        
        bb.put((byte)transferFromType.ordinal());
        bb.putInt(lTransferFromID);
        bb.put(LaunchUtilities.GetIntListData(TypesList));
        bb.put((byte)transferToType.ordinal());
        bb.putInt(lTransferToID);
        
        return bb.array();
    }
}
