/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities.conceptuals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.LaunchEntity;
import launch.game.entities.conceptuals.Resource.ResourceType;

/**
 * Made this a launch entity so that I could "hackily" co-opt EntityUpdated in the client.
 * @author Corbin
 */
public class TerrainData extends LaunchEntity
{
    private boolean bWater;
    
    public TerrainData(boolean bWater)
    {
        super(LaunchEntity.ID_NONE);
        this.bWater = bWater;
    }
    
    public TerrainData(ByteBuffer bb)
    {
        super(LaunchEntity.ID_NONE);
        this.bWater = (bb.get() != 0x00);
    }
    
    @Override
    public void Tick(int lMS)
    {
        
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(1);
        
        bb.put((byte)(bWater ? 0xFF : 0x00));
        
        return bb.array();
    }
    
    @Override
    public byte[] GetFullStatsData(int lAskingID)
    {
        return GetData(lAskingID);
    }
    
    public boolean GetWater()
    {
        return this.bWater;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.TerrainData;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return null;
    }
    
    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        return false;
    }
    
    @Override 
    public boolean GetOwnedBy(int lPlayerID)
    {
        return true;
    }
    
    @Override
    public int GetOwnerID()
    {
        return LaunchEntity.ID_NONE;
    }
    
    @Override
    public String GetTypeName()
    {
        return "TerrainData";
    }
}
