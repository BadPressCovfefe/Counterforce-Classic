/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import launch.game.EntityPointer;
import tobcomm.TobComm;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;
import launch.game.systems.MissileSystem;

/**
 *
 * @author tobster
 */
public class LaunchSomethingTask extends Task
{
    private enum LaunchType
    {
        MISSILE,
        INTERCEPTOR,
        TORPEDO,
    }
            
    private LaunchType type;
    
    //Missiles
    public LaunchSomethingTask(LaunchClientGameInterface gameInterface, int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, MissileSystem.SystemType systemType, boolean bAirburst)
    {
        super(gameInterface);
        
        type = LaunchType.MISSILE;
        
        byte[] cTargetData = target != null ? target.GetData(): new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(19 + cTargetData.length);
        bb.putInt(lSiteID);
        bb.putInt(lSlotNo);
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.put((byte)(target != null ? 0xFF : 0x00));
        bb.put(cTargetData);
        bb.put((byte) systemType.ordinal());
        bb.put((byte)(bAirburst ? 0xFF : 0x00));
        cData = bb.array();
    }
    
    //Interceptors
    public LaunchSomethingTask(LaunchClientGameInterface gameInterface, int lSiteID, int lSlotNo, int lTargetID, EntityPointer.EntityType targetType, MissileSystem.SystemType systemType)
    {
        super(gameInterface);
        
        type = LaunchType.INTERCEPTOR;
        
        ByteBuffer bb = ByteBuffer.allocate(14);
        bb.putInt(lSiteID);
        bb.putInt(lSlotNo);
        bb.putInt(lTargetID);
        bb.put((byte) targetType.ordinal());
        bb.put((byte) systemType.ordinal());
        cData = bb.array();
    }
    
    //Torpedoes
    public LaunchSomethingTask(LaunchClientGameInterface gameInterface, int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, MissileSystem.SystemType systemType)
    {
        super(gameInterface);
        
        type = LaunchType.TORPEDO;
        
        byte[] cTargetData = target != null ? target.GetData(): new byte[0];
        
        ByteBuffer bb = ByteBuffer.allocate(18 + cTargetData.length);
        bb.putInt(lSiteID);
        bb.putInt(lSlotNo);
        bb.putFloat(geoTarget.GetLatitude());
        bb.putFloat(geoTarget.GetLongitude());
        bb.put((byte)(target != null ? 0xFF : 0x00));
        bb.put(cTargetData);
        bb.put((byte) systemType.ordinal());
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        switch(type)
        {
            case MISSILE: comm.SendObject(LaunchSession.LaunchMissile, cData); break;
            case INTERCEPTOR: comm.SendObject(LaunchSession.LaunchInterceptor, cData); break;
            case TORPEDO: comm.SendObject(LaunchSession.LaunchTorpedo, cData); break;
        }
    }
}
