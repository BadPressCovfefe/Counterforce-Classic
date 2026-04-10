/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.nio.ByteBuffer;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author Corbin
 */
public class ChatMessage 
{
    private static final int DATA_SIZE = 17;
            
    public enum Channel
    {
        ALL,
        ALLIANCE,
        PRIVATE,
    }
    
    int lFromID;
    private Channel channel;    
    private int lToID;          //The ID of the player this message is being sent to. Only used for Channel.PRIVATE messages.
    private long oCreatedAt;
    private String strMessage;
    
    public ChatMessage(int lFromID, Channel channel, int lToID, String strMessage)
    {
        this.lFromID = lFromID;
        this.channel = channel;
        this.lToID = lToID;
        this.oCreatedAt = System.currentTimeMillis();
        this.strMessage = strMessage;
    }
    
    public ChatMessage(ByteBuffer bb)
    {
        this.lFromID = bb.getInt();
        this.channel = Channel.values()[bb.get()];
        this.lToID = bb.getInt();
        this.oCreatedAt = bb.getLong();
        this.strMessage = LaunchUtilities.StringFromData(bb);
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + LaunchUtilities.GetStringDataSize(strMessage));
        
        bb.putInt(lFromID);
        bb.put((byte) channel.ordinal());
        bb.putInt(lToID);
        bb.putLong(oCreatedAt);
        bb.put(LaunchUtilities.GetStringData(strMessage));
        
        return bb.array();
    }
    
    public String GetMessage()
    {
        return strMessage;
    }
    
    public int GetFromID()
    {
        return lFromID;
    }
    
    public Channel GetChannel()
    {
        return channel;
    }
    
    public int GetToID()
    {
        return lToID;
    }
    
    public long GetCreatedAtTime()
    {
        return oCreatedAt;
    }
}
