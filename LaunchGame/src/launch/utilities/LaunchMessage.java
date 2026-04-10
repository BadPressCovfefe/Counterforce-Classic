/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.utilities;

import java.nio.ByteBuffer;

/**
 *
 * @author Corbin
 */
public class LaunchMessage
{
    private final static int DATA_SIZE = 13; //DATA_SIZE does not take into account strMessage, which is added in GetData().
    
    private static final byte CHANNEL_ALL = 0;      
    private static final byte CHANNEL_ALLIANCE = 1;     
    private static final byte CHANNEL_AFFILIATES = 2;
    
    private int senderID;
    private byte cChannel;
    private long oTimeStamp;

    private String strMessage;
	
    
    public LaunchMessage(int senderID, byte cChannel, String strMessage, long oTimeStamp)
    {
        this.senderID = senderID;
        this.cChannel = cChannel;
        this.strMessage = strMessage;
        this.oTimeStamp = oTimeStamp;
    }

    public LaunchMessage(ByteBuffer bb)
    {
        senderID = bb.getInt();
        cChannel = bb.get();
        strMessage = LaunchUtilities.StringFromData(bb);
        oTimeStamp = bb.getLong();
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + LaunchUtilities.GetStringDataSize(strMessage));
        bb.putInt(senderID);
        bb.put(cChannel);
        bb.put(LaunchUtilities.GetStringData(strMessage));
        bb.putLong(oTimeStamp);
        return bb.array();
    }
    
    public int GetSenderID() { return senderID; }
    public byte GetChannel() { return cChannel; }
    public String GetMessage() { return strMessage; }
    public boolean GetToAll() { return cChannel == CHANNEL_ALL; }
    public boolean GetToAllies() { return cChannel == CHANNEL_ALLIANCE; }
    public boolean GetToAffiliates() { return cChannel == CHANNEL_AFFILIATES; }	
    public long GetTimeStamp() { return oTimeStamp; }
}
