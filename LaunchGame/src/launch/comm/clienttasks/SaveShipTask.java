/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm.clienttasks;

import java.nio.ByteBuffer;
import launch.comm.LaunchSession;
import tobcomm.TobComm;
import launch.game.LaunchClientGameInterface;
import launch.utilities.LaunchUtilities;

/**
 *
 * @author tobster
 */
public class SaveShipTask extends Task
{
    boolean bShip; //Otherwise, submarine.
    
    /** Ship. */
    public SaveShipTask(LaunchClientGameInterface gameInterface, String strDesignName, boolean bPublic, boolean bHasSonar, boolean bHasScanner, boolean bNuclear, float fltFuelCapacity, int lArmor, int lCargoCapacity, int lMissileCount, int lInterceptorCount, int lTorpedoCount, int lArtilleryCount, int lSentryGunCount, int lAircraftCount, boolean bAmphibious, boolean bSupport)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        bShip = true;
        
        ByteBuffer bb = ByteBuffer.allocate(42 + LaunchUtilities.GetStringDataSize(strDesignName));
        bb.put((byte)(bPublic? 0xFF : 0x00));
        bb.put(LaunchUtilities.GetStringData(strDesignName));
        bb.put((byte)(bHasSonar? 0xFF : 0x00));
        bb.put((byte)(bHasScanner? 0xFF : 0x00));
        bb.put((byte)(bNuclear? 0xFF : 0x00));
        bb.putFloat(fltFuelCapacity);
        bb.putInt(lArmor);
        bb.putInt(lCargoCapacity);
        bb.putInt(lMissileCount);
        bb.putInt(lInterceptorCount);
        bb.putInt(lTorpedoCount);
        bb.putInt(lArtilleryCount);
        bb.putInt(lSentryGunCount);
        bb.putInt(lAircraftCount);
        bb.put((byte)(bAmphibious? 0xFF : 0x00));
        bb.put((byte)(bSupport? 0xFF : 0x00));
        
        cData = bb.array();
    }
    
    /** Submarine. */
    public SaveShipTask(LaunchClientGameInterface gameInterface, String strDesignName, boolean bPublic, boolean bNuclear, float fltFuelCapacity, int lArmor, int lMissileCount, int lTorpedoCount, int lICBMCount)
    {
        super(gameInterface);
        gameInterface.ShowTaskMessage(TaskMessage.CONFIGURING);
        bShip = false;
        
        ByteBuffer bb = ByteBuffer.allocate(22 + LaunchUtilities.GetStringDataSize(strDesignName));
        bb.put((byte)(bPublic? 0xFF : 0x00));
        bb.put(LaunchUtilities.GetStringData(strDesignName));
        bb.put((byte)(bNuclear? 0xFF : 0x00));
        bb.putFloat(fltFuelCapacity);
        bb.putInt(lArmor);
        bb.putInt(lMissileCount);
        bb.putInt(lTorpedoCount);
        bb.putInt(lICBMCount);
        
        cData = bb.array();
    }
    
    @Override
    public void Start(TobComm comm)
    {
        if(bShip)
            comm.SendObject(LaunchSession.SaveShipType, cData);
        else
            comm.SendObject(LaunchSession.SaveSubmarineType, cData);
    }
}
