/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm;

import java.net.Socket;
import java.nio.ByteBuffer;
import static launch.comm.LaunchComms.LOG_NAME;
import launch.comm.clienttasks.Task;
import launch.comm.clienttasks.UploadAvatarTask;
import launch.game.Alliance;
import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGameInterface;
import launch.game.User;
import launch.game.treaties.*;
import launch.game.entities.*;
import launch.game.entities.conceptuals.TerrainData;
import launch.game.treaties.Affiliation;
import launch.game.treaties.War;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchLog;
import static launch.utilities.LaunchLog.LogType.COMMS;
import launch.utilities.LaunchReport;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;
import tobcomm.TobComm;

/**
 *
 * @author tobster
 */
public class LaunchClientSession extends LaunchSession
{
    private static final int UPDATE_INTERVAL = 5000;
    private static final int CLIENT_TIMEOUT = 15000;
    private static final short IMAGE_DOWNLOAD_INTERVAL = 500; //To space out image downloads so network/UI performance isn't impacted.
    
    private enum State
    {
        GOOGLE_ID_WAIT,
        LOCATION_WAIT,
        AUTHORISE,
        READY,
        PROCESSING_TASK,
        CLOSED,
    }
    
    private LaunchClientGameInterface gameInterface;
    private State state;
    
    private byte[] cDeviceID;
    private String strGoogleID;
    private String strDeviceName;
    private String strProcessName;
    private boolean bMobile;
    
    private Task currentTask;
    private ShortDelay dlyUpdate = new ShortDelay();
    private int lLatency;
    
    private boolean bDownloadingAvatar = false;
    private boolean bDownloadingImage = false;
    
    private boolean bDownloadingMissingConfig = false;
    
    private boolean bReceivingSnapshot = false;
    
    private int lOurPlayerID;
    
    private ShortDelay dlyImageDownload = new ShortDelay();
        
    public LaunchClientSession(Socket socket, LaunchClientGameInterface gameInterface, byte[] cDeviceID, String strGoogleID, String strDeviceName, String strProcessName, boolean bMobile)
    {
        super(socket);
        
        this.gameInterface = gameInterface;
        this.cDeviceID = cDeviceID;
        this.strGoogleID = strGoogleID;
        this.strDeviceName = strDeviceName;
        this.strProcessName = strProcessName;
        this.bMobile = bMobile;
        this.state = State.GOOGLE_ID_WAIT;
        
        Start();
    }

    @Override
    protected void Process()
    {
        if(IsAlive())
        {
            switch(state)
            {
                case GOOGLE_ID_WAIT:
                {
                    if(strGoogleID != null)
                    {
                        state = State.LOCATION_WAIT;
                    }
                    else
                    {
                        tobComm.SendCommand(AccountUnmigrated);
                    }
                }
                break;
                
                case LOCATION_WAIT:
                {
                    if(strGoogleID != null)
                    {
                        //Waiting for location from game. We cannot progress if it's not available.
                        if(gameInterface.PlayerLocationAvailable())
                        {
                            Authenticate();
                        }
                        else if(gameInterface.GetGameConfigChecksum() == MapEntity.ID_NONE)
                        {
                            //Otherwise, try to download the config if it's missing.
                            if(!bDownloadingMissingConfig)
                            {
                                bDownloadingMissingConfig = true;
                                tobComm.RequestObject(Config, 0, 0, 0);
                            }
                        }
                    }
                    else
                    {
                        state = State.GOOGLE_ID_WAIT;
                        LaunchLog.Log(COMMS, LOG_NAME, "Cannot continue: strGoogleID is null.");
                    } 
                }
                break;
                
                case AUTHORISE:
                {
                    //Waiting for authorisation.
                }
                break;
                
                case READY:
                {
                    //Idle. We can process tasks.
                }
                break;
                
                case PROCESSING_TASK:
                {
                    //Busy. We cannot accept any tasks. Revert to ready when the current task finishes.
                    if(currentTask.Complete())
                    {
                        state = State.READY;
                    }
                }
                break;
                
                case CLOSED:
                {
                    //We are formally dead and should do nothing.
                }
                break;
            }
            
            if(dlyUpdate.Expired() && !gameInterface.ClosingAccount()) //Suppress when closing account, so it doesn't inadvertantly fire a location update and therefore bring the player back out of AWOL.
            {
                //Reset latency measurement.
                lLatency = 0;
                
                if(gameInterface.GetReadyToUpdatePlayer()) //Suppress before snapshot obtained, to prevent privacy zone spoofing.
                {
                    if(state == State.READY || state == State.PROCESSING_TASK)
                    {
                        //Authed. Send player's location.
                        LaunchClientLocation location = gameInterface.GetPlayerLocation();
                        
                        if(location != null)
                        {
                            tobComm.SendObject(LocationUpdate, location.GetData());
                        }
                    }
                    else
                    {
                        //Not authed. Send a keepalive.
                        tobComm.SendCommand(KeepAlive);
                    }
                }
                else
                {
                    //Send keepalive to keep connection open while downloading snapshot.
                    tobComm.SendCommand(KeepAlive);
                }
                
                dlyUpdate.Set(UPDATE_INTERVAL);
            }
        }
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        dlyImageDownload.Tick(lMS);
        
        if(IsAlive())
        {
            lLatency += lMS;
            
            //Periodically send the player's location (or a keepalive). This keeps the player's location up to date, and keeps the session alive.
            dlyUpdate.Tick(lMS);
        }
    }

    @Override
    public void Close()
    {
        super.Close();
        state = State.CLOSED;
        gameInterface.SetLatency(Defs.LATENCY_DISCONNECTED);
    }
    
    private void Authenticate()
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Authenticating...");
        
        state = State.AUTHORISE;

        byte cAuthFlags = 0x00;

        if(bMobile)
            cAuthFlags |= Defs.AUTH_FLAG_MOBILE;
        
        int lGoogleIDSize = LaunchUtilities.GetStringDataSize(strGoogleID);
        int lDeviceNameSize = LaunchUtilities.GetStringDataSize(strDeviceName);
        int lProcessNameSize = LaunchUtilities.GetStringDataSize(strProcessName);

        ByteBuffer bb = ByteBuffer.allocate(cDeviceID.length + 11 + lGoogleIDSize + lDeviceNameSize + lProcessNameSize);

        bb.put(cDeviceID);
        bb.put(LaunchUtilities.GetStringData(strGoogleID));
        bb.putShort(Defs.MAJOR_VERSION);
        bb.put(LaunchUtilities.GetStringData(strDeviceName));
        bb.put(LaunchUtilities.GetStringData(strProcessName));
        bb.putFloat(gameInterface.GetPlayerLocation().GetGeoCoord().GetLatitude());
        bb.putFloat(gameInterface.GetPlayerLocation().GetGeoCoord().GetLongitude());
        bb.put(cAuthFlags);

        tobComm.SendObject(Authorise, bb.array());
        
        LaunchLog.Log(COMMS, LOG_NAME, "Authentication packet sent.");
    }
    
    private void Authenticated()
    {
        state = State.READY;
        gameInterface.Authenticated();
        
        //Enable object sync, to process the incoming snapshot on one thread (instead of one thread for every freakin' object).
        tobComm.ObjectSyncEnable();
        tobComm.RequestObject(GameSnapshot);
                
        //Reset update interval for quick player refresh.
        dlyUpdate.ForceExpiry();
    }
    
    public boolean CanAcceptTask()
    {
        //Can we accept a task?
        if(state == State.READY)
            return true;

        //Can we accept a register or upload avatar task, which is a special case because we're still in Authorise state?
        if(state == State.AUTHORISE)
        {
            if(currentTask != null)
            {
                return currentTask.Complete();
            }
            
            return true;
        }
        
        //We cannot accept a task.
        return false;
    }
    
    public boolean CanDownloadAnImage()
    {
        return state == State.READY && !bDownloadingAvatar && !bDownloadingImage && dlyImageDownload.Expired();
    }
    
    public void DownloadAvatar(int lAvatarID)
    {
        dlyImageDownload.Set(IMAGE_DOWNLOAD_INTERVAL);
        bDownloadingAvatar = true;
        tobComm.RequestObject(Avatar, lAvatarID);
    }
    
    public void DownloadImage(int lImageID)
    {
        dlyImageDownload.Set(IMAGE_DOWNLOAD_INTERVAL);
        bDownloadingImage = true;
        tobComm.RequestObject(ImgAsset, lImageID);
    }
    
    public void StartTask(Task task)
    {
        if(state != State.AUTHORISE)
        {
            //Only progress to processing task when authorised, so the registration task doesn't send us doolally.
            state = State.PROCESSING_TASK;
        }
        
        currentTask = task;
        currentTask.Start(tobComm);
    }
    
    private boolean UploadingAvatar()
    {
        if(currentTask != null)
        {
            if(!currentTask.Complete() && currentTask instanceof UploadAvatarTask)
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void ObjectReceived(int lObject, int lInstanceNumber, int lOffset, byte[] cData)
    {
        try
        {
            ByteBuffer bb = ByteBuffer.wrap(cData);
        
            switch(lObject)
            {
                case Authorise:
                {
                    short nMajorVersion = bb.getShort();
                    short nMinorVersion = bb.getShort();
                    int lConfigChecksum = bb.getInt();
                    int lPlayerID = bb.getInt();

                    //Store the player ID and notify the game interface of the player ID.
                    lOurPlayerID = lPlayerID;
                    gameInterface.SetOurPlayerID(lPlayerID);

                    LaunchLog.ConsoleMessage("Authorised. Requesting config...");
                    
                    if(gameInterface.VerifyVersion(nMajorVersion, nMinorVersion))
                    {
                        if(lConfigChecksum != gameInterface.GetGameConfigChecksum())
                        {
                            tobComm.RequestObject(Config);
                        }
                        else
                        {
                            Authenticated();
                        }
                    }
                }
                break;

                case UserData:
                {
                    User user = new User(bb);
                    gameInterface.ReceiveUser(user);
                }
                break;

                case BanData:
                {
                    long oDuration = bb.getLong();
                    String strBanReason = LaunchUtilities.StringFromData(bb);

                    gameInterface.TempBanned(strBanReason, oDuration);
                }
                break;

                case PermBanData:
                {
                    String strBanReason = LaunchUtilities.StringFromData(bb);

                    gameInterface.PermBanned(strBanReason);
                }
                break;

                case Event:
                {
                    gameInterface.EventReceived(new LaunchEvent(ByteBuffer.wrap(cData)));
                }
                break;

                case Report:
                {
                    gameInterface.ReportReceived(new LaunchReport(ByteBuffer.wrap(cData)));

                    //Ack the report if outside of snapshot.
                    if(!bReceivingSnapshot)
                    {
                        tobComm.SendCommand(ReportAck);
                    }
                }
                break;

                case Config:
                {
                    gameInterface.SetConfig(new Config(cData));

                    if(!bDownloadingMissingConfig)
                        Authenticated();
                    
                    bDownloadingMissingConfig = false;
                }
                break;

                case Avatar:
                {
                    if(UploadingAvatar())
                    {
                        currentTask.HandleObject(lObject, lInstanceNumber, lOffset, cData);
                    }
                    else
                    {
                        gameInterface.AvatarReceived(lInstanceNumber, cData);
                        bDownloadingAvatar = false;
                    }
                }
                break;

                case BasicOKDialog:
                {
                    gameInterface.ShowBasicOKDialog(LaunchUtilities.StringFromData(bb));
                }
                break;

                case TerrainData:
                {
                    gameInterface.ReceiveTerrainData(new TerrainData(bb));
                }
                break;

                case Player:
                {
                    gameInterface.ReceivePlayer(new Player(bb, lOurPlayerID));
                }
                break;

                case Missile:
                {
                    gameInterface.ReceiveMissile(new Missile(bb));
                }
                break;

                case Torpedo:
                {
                    gameInterface.ReceiveTorpedo(new Torpedo(bb));
                }
                break;

                case Interceptor:
                {
                    gameInterface.ReceiveInterceptor(new Interceptor(bb));
                }
                break;

                case MissileSite:
                {
                    gameInterface.ReceiveMissileSite(new MissileSite(bb, lOurPlayerID));
                }
                break;

                case SamSite:
                {
                    gameInterface.ReceiveSAMSite(new SAMSite(bb, lOurPlayerID));
                }
                break;

                case OreMine:
                {
                    gameInterface.ReceiveOreMine(new OreMine(bb, lOurPlayerID));
                }
                break;

                case Processor:
                {
                    gameInterface.ReceiveProcessor(new Processor(bb, lOurPlayerID));
                }
                break;

                case Distributor:
                {
                    gameInterface.ReceiveDistributor(new Distributor(bb, lOurPlayerID));
                }
                break;

                case ResourceDeposit:
                {
                    gameInterface.ReceiveResourceDeposit(new ResourceDeposit(bb));
                }
                break;

                case MissileFactory:
                {
                    gameInterface.ReceiveMissileFactory(new MissileFactory(bb, lOurPlayerID));
                }
                break;

                case RadarStation:
                {
                    gameInterface.ReceiveRadarStation(new RadarStation(bb, lOurPlayerID));
                }
                break;

                case CommandPost:
                {
                    gameInterface.ReceiveCommandPost(new CommandPost(bb, lOurPlayerID));
                }
                break;

                case Airbase:
                {
                    Airbase airbase = new Airbase(bb, lOurPlayerID);
                    
                    LaunchLog.ConsoleMessage(String.format("Object Received: case AIRBASE. Airbase ID: %d.", airbase.GetID()));
                    
                    gameInterface.ReceiveAirbase(airbase);
                }
                break;

                case Barracks:
                case Armory:
                {
                    gameInterface.ReceiveArmory(new Armory(bb, lOurPlayerID));
                }
                break;

                case Infantry:
                {
                    gameInterface.ReceiveInfantry(new Infantry(bb, lOurPlayerID));
                }
                break;

                case Shipyard:
                {
                    gameInterface.ReceiveShipyard(new Shipyard(bb, lOurPlayerID));
                }
                break;

                case Tank:
                {
                    gameInterface.ReceiveTank(new Tank(bb, lOurPlayerID));
                }
                break;

                case Ship:
                {
                    gameInterface.ReceiveShip(new Ship(bb, lOurPlayerID));
                }
                break;

                case Submarine:
                {
                    gameInterface.ReceiveSubmarine(new Submarine(bb, lOurPlayerID));
                }
                break;

                case CargoTruck:
                {
                    gameInterface.ReceiveCargoTruck(new CargoTruck(bb, lOurPlayerID));
                }
                break;

                case Airdrop:
                {
                    gameInterface.ReceiveAirdrop(new Airdrop(bb));
                }
                break;

                case Bank:
                {
                    gameInterface.ReceiveBank(new Bank(bb, lOurPlayerID));
                }
                break;

                case Warehouse:
                {
                    gameInterface.ReceiveWarehouse(new Warehouse(bb, lOurPlayerID));
                }
                break;

                case Airplane:
                {
                    gameInterface.ReceiveAircraft(new Airplane(bb));
                }
                break;

                case StoredAirplane:
                {
                    //gameInterface.ReceiveStoredAircraft(new StoredAircraft(bb));
                }
                break;

                case StoredTank:
                {
                    //gameInterface.ReceiveStoredTank(new StoredTank(bb));
                }
                break;

                case StoredCargoTruck:
                {
                    //gameInterface.ReceiveStoredCargoTruck(new StoredCargoTruck(bb));
                }
                break;

                case Blueprint:
                {
                    gameInterface.ReceiveBlueprint(new Blueprint(bb));
                }
                break;

                case SentryGun:
                {
                    gameInterface.ReceiveSentryGun(new SentryGun(bb, lOurPlayerID));
                }
                break;

                case ArtilleryGun:
                {
                    gameInterface.ReceiveArtilleryGun(new ArtilleryGun(bb, lOurPlayerID));
                }
                break;

                case ScrapYard:
                {
                    gameInterface.ReceiveScrapYard(new ScrapYard(bb, lOurPlayerID));
                }
                break;

                case Loot:
                {
                    gameInterface.ReceiveLoot(new Loot(bb));
                }
                break;

                case Rubble:
                {
                    gameInterface.ReceiveRubble(new Rubble(bb));
                }
                break;
                
                case KOTH:
                {
                    gameInterface.ReceiveKOTH(new KOTH(bb));
                }
                break;

                case Radiation:
                {
                    gameInterface.ReceiveRadiation(new Radiation(bb));
                }
                break;

                case AllianceMinor:
                {
                    gameInterface.ReceiveAlliance(new Alliance(bb), false);
                }
                break;

                case AllianceMajor:
                {
                    gameInterface.ReceiveAlliance(new Alliance(bb), true);
                }
                break;

                case Treaty:
                {
                    byte cType = bb.get();

                    Treaty treaty = null;

                    switch(launch.game.treaties.Treaty.Type.values()[cType])
                    {
                        case WAR: treaty = new War(bb); break;
                        case AFFILIATION: treaty = new Affiliation(bb); break;
                        case AFFILIATION_REQUEST: treaty = new AffiliationRequest(bb); break;
                        case SURRENDER_PROPOSAL: treaty = new SurrenderProposal(bb); break;
                    }

                    gameInterface.ReceiveTreaty(treaty);
                }
                break;

                case FullPlayerStats:
                {
                    gameInterface.ReceivePlayer(new Player(bb, lOurPlayerID));
                }
                break;

                case ImgAsset:
                {
                    gameInterface.ImageReceived(lInstanceNumber, cData);
                    bDownloadingImage = false;
                }
                break;

                default:
                {
                    //The current task must handle this object.
                    if(currentTask != null)
                        currentTask.HandleObject(lObject, lInstanceNumber, lOffset, cData);
                }
            }
        }
        catch(Exception ex)
        {
            //Not really interested. This is just here to catch an OutOfMemoryError on a few types of phones.
        }
    }

    @Override
    public void CommandReceived(int lCommand, int lInstanceNumber)
    {
        switch(lCommand)
        {
            case AccountUnregistered:
            {
                //Notify game client unregistered.
                gameInterface.AccountUnregistered();
            }
            break;
            
            case MajorVersionInvalid:
            {
                //Notify game client version wrong.
                gameInterface.MajorVersionInvalid();
            }
            break;
            
            case LocationSpoof:
            {
                //Notify game client version wrong.
                gameInterface.FailedSpoofCheck();
            }
            break;
            
            case AccountUnmigrated:
            {
                LaunchLog.ConsoleMessage("case AccountUnmigrated [LaunchClientSession]");
                gameInterface.AccountUnmigrated();
            }
            break;
            
            case AccountMigrated:
            {
                Authenticate();
            }
            break;
            
            case AccountCreateSuccess:
            {
                //Immediately authorise.
                Authenticate();
                
                gameInterface.AccountMigrated(false);
                
                //Notify task so it can die.
                currentTask.HandleCommand(lCommand, lInstanceNumber);
            }
            break;
            
            case KeepAlive:
            {
                //Notify game of latency. Only when fully authed, as this will override the snapshot download speed display.
                if((state == State.READY || state == State.PROCESSING_TASK) && !bReceivingSnapshot)
                {
                    gameInterface.SetLatency(lLatency);
                }
            }
            break;
            
            case SnapshotBegin:
            {
                bReceivingSnapshot = true;
                gameInterface.SnapshotBegin();
                gameInterface.SetLatency(Defs.LATENCY_DISCONNECTED);
            }
            break;
            
            case SnapshotComplete:
            {
                //Just flush the sync buffer and process the objects. We will ack once the thread has processed them all.
                tobComm.ObjectSyncFlush();
            }
            break;
            
            case RemovePlayer:
            {
                gameInterface.RemovePlayer(lInstanceNumber);
            }
            break;
            
            case RemoveMissile:
            {
                gameInterface.RemoveMissile(lInstanceNumber);
            }
            break;
            
            case RemoveInterceptor:
            {
                gameInterface.RemoveInterceptor(lInstanceNumber);
            }
            break;
            
            case RemoveMissileSite:
            {
                gameInterface.RemoveMissileSite(lInstanceNumber);
            }
            break;
            
            case RemoveSAMSite:
            {
                gameInterface.RemoveSAMSite(lInstanceNumber);
            }
            break;
            
            case RemoveSentryGun:
            {
                gameInterface.RemoveSentryGun(lInstanceNumber);
            }
            break;
            
            case RemoveArtilleryGun:
            {
                gameInterface.RemoveArtilleryGun(lInstanceNumber);
            }
            break;
            
            case RemoveScrapYard:
            {
                gameInterface.RemoveScrapYard(lInstanceNumber);
            }
            break;
            
            case RemoveOreMine:
            {
                gameInterface.RemoveOreMine(lInstanceNumber);
            }
            break;
            
            case RemoveProcessor:
            {
                gameInterface.RemoveProcessor(lInstanceNumber);
            }
            break;
            
            case RemoveDistributor:
            {
                gameInterface.RemoveDistributor(lInstanceNumber);
            }
            break;
            
            case RemoveRadarStation:
            {
                gameInterface.RemoveRadarStation(lInstanceNumber);
            }
            break;
            
            case RemoveCommandPost:
            {
                gameInterface.RemoveCommandPost(lInstanceNumber);
            }
            break;
            
            case RemoveAirbase:
            {
                gameInterface.RemoveAirbase(lInstanceNumber);
            }
            break;
            
            case RemoveArmory:
            {
                gameInterface.RemoveArmory(lInstanceNumber);
            }
            break;
            
            case RemoveInfantry:
            {
                gameInterface.RemoveInfantry(lInstanceNumber);
            }
            break;
            
            case RemoveTank:
            {
                gameInterface.RemoveTank(lInstanceNumber);
            }
            break;
            
            case RemoveShip:
            {
                gameInterface.RemoveShip(lInstanceNumber);
            }
            break;
            
            case RemoveSubmarine:
            {
                gameInterface.RemoveSubmarine(lInstanceNumber);
            }
            break;
            
            case RemoveCargoTruck:
            {
                gameInterface.RemoveCargoTruck(lInstanceNumber);
            }
            break;
            
            case RemoveAirdrop:
            {
                gameInterface.RemoveAirdrop(lInstanceNumber);
            }
            break;
            
            case RemoveBank:
            {
                gameInterface.RemoveBank(lInstanceNumber);
            }
            break;
            
            case RemoveWarehouse:
            {
                gameInterface.RemoveWarehouse(lInstanceNumber);
            }
            break;
            
            case RemoveAircraft:
            {
                gameInterface.RemoveAircraft(lInstanceNumber);
            }
            break;
            
            case RemoveStoredAircraft:
            {
                gameInterface.RemoveStoredAircraft(lInstanceNumber);
            }
            break;
            
            case RemoveBlueprint:
            {
                gameInterface.RemoveBlueprint(lInstanceNumber);
            }
            break;
            
            case RemoveLoot:
            {
                gameInterface.RemoveLoot(lInstanceNumber);
            }
            break;
            
            case RemoveRubble:
            {
                gameInterface.RemoveRubble(lInstanceNumber);
            }
            break;
            
            case RemoveRadiation:
            {
                gameInterface.RemoveRadiation(lInstanceNumber);
            }
            break;
            
            case RemoveResourceDeposit:
            {
                gameInterface.RemoveResourceDeposit(lInstanceNumber);
            }
            break;
            
            case RemoveAlliance:
            {
                gameInterface.RemoveAlliance(lInstanceNumber);
            }
            break;
            
            case RemoveTreaty:
            {
                gameInterface.RemoveWar(lInstanceNumber);
            }
            break;
            
            case UpdateToken:
            {
                gameInterface.UpdateToken();
            }
            
            case ImageError:
            {
                bDownloadingAvatar = false;
                bDownloadingImage = false;
            }
            break;
            
            default:
            {
                //The current task must handle this command.
                if(currentTask != null)
                    currentTask.HandleCommand(lCommand, lInstanceNumber);
            }
        }
    }

    @Override
    public void ObjectRequested(int lObject, int lInstanceNumber, int lOffset, int lLength)
    {
        switch(lObject)
        {
            case ProcessNames:
            {
                tobComm.SendObject(ProcessNames, LaunchUtilities.GetStringData(gameInterface.GetProcessNames()));
            }
            break;
            
            case DeviceCheck:
            {
                tobComm.SendObject(ProcessNames, LaunchUtilities.GetStringData(gameInterface.GetProcessNames()));
                gameInterface.DeviceCheckRequested();
            }
            break;
            
            default:
            {
                //Shouldn't happen.
            }
        }
    }

    @Override
    public void SyncObjectsProcessed()
    {
        //The sync thread has processed all snapshot objects. Now we can ack.
        bReceivingSnapshot = false;
        tobComm.SendCommand(SnapshotAck, 0);
        gameInterface.SnapshotFinish();
    }
    
    public String GetState()
    {
        return state.name();
    }
    
    /**
     * For experimental direct communications.
     * @return The TobComm instance.
     */
    public TobComm GetTobComm()
    {
        return tobComm;
    }

    @Override
    protected int GetTimeout()
    {
        return CLIENT_TIMEOUT;
    }
    
    public void SetGoogleID(String strGoogleID)
    {
        this.strGoogleID = strGoogleID;
    }
}
