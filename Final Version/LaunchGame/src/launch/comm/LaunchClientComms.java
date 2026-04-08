/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import static launch.comm.LaunchComms.LOG_NAME;
import launch.comm.clienttasks.*;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGameInterface;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.entities.MissileFactory;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoRectangle;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem.LootType;
import launch.game.treaties.Treaty;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchLog;
import static launch.utilities.LaunchLog.LogType.COMMS;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;
import tobcomm.TobComm;

/**
 *
 * @author tobster
 */
public class LaunchClientComms extends LaunchComms
{
    private static final int CONNECT_DWELL_TIME = 500;
    private static final int RETRY_DWELL_TIME = 2000;
    
    private enum State
    {
        OFFLINE,
        CONNECT,
        CONNECTING,
        PROCESS,
        REINIT
    }
    
    private State state = State.OFFLINE;
            
    private LaunchClientGameInterface gameInterface;
    private String strURL;
    private int lPort;
    
    private LaunchClientSession session;
    
    private byte[] cDeviceID;
    private String strGoogleID;
    private String strDeviceName;
    private String strProcessName;
    
    private Task currentTask;
    
    private ShortDelay dlyReinit = new ShortDelay();
    
    private Queue<Integer> AvatarDownloadQueue = new LinkedList<>();
    private Queue<Integer> ImageDownloadQueue = new LinkedList<>();
    
    private Socket socket;
    
    public LaunchClientComms(LaunchClientGameInterface gameInterface, String strURL, int lPort)
    {
        this.gameInterface = gameInterface;
        this.strURL = strURL;
        this.lPort = lPort;
    }
    
    public void SetDeviceID(byte[] cDeviceID, String strGoogleID, String strDeviceName, String strProcessName)
    {
        this.cDeviceID = cDeviceID;
        this.strGoogleID = strGoogleID;
        this.strDeviceName = strDeviceName;
        this.strProcessName = strProcessName;
        
        if(session != null)
            session.SetGoogleID(strGoogleID);
    }
    
    @Override
    public void Tick(int lMS)
    {
        switch(state)
        {
            case OFFLINE:
            {
                //Do nothing.
            }
            break;
            
            case CONNECT:
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Establish a new connection to the server.
                        try
                        {
                            socket = new Socket(InetAddress.getByName(strURL), lPort);
                        }
                        catch(Exception ex)
                        {
                            //Didn't work. Reinitialise.
                            LaunchLog.Log(COMMS, LOG_NAME, "Could not create socket. Reinitializing...");
                            Reinitialise();
                        }
                    }
                }).start();
                
                LaunchLog.Log(COMMS, LOG_NAME, "Socket created. Going to dwell.");
                //Half a second for things to get their shit in a sock before proceeding further.
                dlyReinit.Set(CONNECT_DWELL_TIME);
                state = State.CONNECTING;
            }
            break;
            
            case CONNECTING:
            {
                dlyReinit.Tick(lMS);
                
                if(dlyReinit.Expired())
                {
                    boolean bOkay = false;
                    
                    if(socket != null)
                    {
                        boolean bMobile = gameInterface.GetConnectionMobile();
                        session = new LaunchClientSession(socket, gameInterface, cDeviceID, strGoogleID, strDeviceName, strProcessName, bMobile);
                        LaunchLog.Log(COMMS, LOG_NAME, "Connection established. Going to processing.");
                        state = State.PROCESS;
                        bOkay = true;
                    }
                    
                    if(!bOkay)
                    {
                        LaunchLog.Log(COMMS, LOG_NAME, "Connection didn't establish properly. Reinitialising.");
                        Reinitialise();
                    }
                }
            }
            break;
            
            case PROCESS:
            {
                if(session.IsAlive())
                {
                    session.Tick(lMS);
                    
                    //Present the session a task when it's ready and we have one.
                    if(session.CanAcceptTask() && currentTask != null)
                    {
                        //Don't restart finished tasks.
                        if(!currentTask.Complete())
                        {
                            LaunchLog.Log(COMMS, LOG_NAME, "We have a task, and the session can accept it. Starting.");
                            session.StartTask(currentTask);
                        }
                    }
                    
                    //Prune the current task once it's complete.
                    if(currentTask != null)
                    {
                        if(currentTask.Complete())
                        {
                            LaunchLog.Log(COMMS, LOG_NAME, "Pruning the current task as it has finished.");
                            currentTask = null;
                        }
                    }
                    
                    //Download avatars and custom asset images whenever we can.
                    if(session.CanDownloadAnImage())
                    {
                        if(AvatarDownloadQueue.size() > 0)
                        {
                            session.DownloadAvatar(AvatarDownloadQueue.poll());
                        }
                        else if(ImageDownloadQueue.size() > 0)
                        {
                            session.DownloadImage(ImageDownloadQueue.poll());
                        }
                    }
                }
                else
                {
                    //Session died. Go to reinit.
                    LaunchLog.Log(COMMS, LOG_NAME, "Socket timed out. Going to reinitialise.");
                    Reinitialise();
                }
            }
            break;
            
            case REINIT:
            {
                //Dwell until ready to try a new session.
                dlyReinit.Tick(lMS);

                if(dlyReinit.Expired())
                {
                    LaunchLog.Log(COMMS, LOG_NAME, "Reinit delay expired. Reconnecting.");
                    state = State.CONNECT;
                }
            }
            break;
        }
    }

    @Override
    public void InterruptAll()
    {
        session.Close();
    }
    
    private void Reinitialise()
    {
        socket = null;
        dlyReinit.Set(RETRY_DWELL_TIME);
        state = State.REINIT;
        gameInterface.SetLatency(Defs.LATENCY_DISCONNECTED);
    }
    
    public void Suspend()
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Suspended.");
        state = State.OFFLINE;
        
        if(session != null)
        {
            session.Close();
        }
        
        //Kill the current task as a catch all incase the game was stuck and the player is trying to "fix" it.
        currentTask = null;
        gameInterface.SetLatency(Defs.LATENCY_DISCONNECTED);
    }
    
    public void Resume()
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Resumed.");
        
        if(state == State.OFFLINE)
        {
            state = State.CONNECT;
        }
    }
    
    public int GetReinitRemaining()
    {
        if(state == State.PROCESS)
            return session.GetTimeoutRemaining();
        
        return dlyReinit.GetRemaining();
    }
    
    public boolean GetDoingAnything()
    {
        if(state == State.PROCESS)
            return !session.GetTimingOut();
        
        return false;
    }
    
    public int GetDownloadRate()
    {
        if(state == State.PROCESS)
            return session.GetDownloadRate();
        
        return 0;
    }
    
    public void Register(String strGoogleID, String strUsername, int lAvatarID, byte[] cDeviceID)
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Register called.");
        currentTask = new RegisterTask(gameInterface, strGoogleID, strUsername, lAvatarID, cDeviceID);
    }
    
    public void UploadAvatar(byte[] cData)
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Upload avatar called.");
        currentTask = new UploadAvatarTask(gameInterface, cData);
    }
    
    public void DownloadAvatar(int lAvatarID)
    {
        if(!AvatarDownloadQueue.contains(lAvatarID))
        {
            LaunchLog.Log(COMMS, LOG_NAME, String.format("Queued avatar %d for download.", lAvatarID));
            AvatarDownloadQueue.add(lAvatarID);
        }
        else
        {
            LaunchLog.Log(COMMS, LOG_NAME, String.format("Avatar %d already queued.", lAvatarID));
        }
    }
    
    public void DownloadImage(int lAssetID)
    {
        if(!ImageDownloadQueue.contains(lAssetID))
        {
            LaunchLog.Log(COMMS, LOG_NAME, String.format("Queued image %d for download.", lAssetID));
            ImageDownloadQueue.add(lAssetID);
        }
        else
        {
            LaunchLog.Log(COMMS, LOG_NAME, String.format("Image %d already queued.", lAssetID));
        }
    }
    
    public void Respawn()
    {
        LaunchLog.Log(COMMS, LOG_NAME, "Respawn called.");
        currentTask = new RespawnTask(gameInterface);
    }
    
    public void SetToken(String strToken)
    {
        currentTask = new SetTokenTask(gameInterface, strToken);
    }
    
    public void LaunchMissile(int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, SystemType systemType, boolean bAirburst)
    {
        currentTask = new LaunchSomethingTask(gameInterface, lSiteID, lSlotNo, geoTarget, target, systemType, bAirburst);
    }
    
    public void LaunchInterceptor(int lSiteID, int lSlotNo, int lTargetID, EntityType targetType, SystemType systemType)
    {
        currentTask = new LaunchSomethingTask(gameInterface, lSiteID, lSlotNo, lTargetID, targetType, systemType);
    }
    
    public void LaunchTorpedo(int lSiteID, int lSlotNo, GeoCoord geoTarget, EntityPointer target, SystemType systemType)
    {
        currentTask = new LaunchSomethingTask(gameInterface, lSiteID, lSlotNo, geoTarget, target, systemType);
    }
    
    public void PurchaseLaunchables(int lSiteID, int lSlotNumber, int[] lTypes, SystemType systemType)
    {
        currentTask = new PurchaseLaunchablesTask(gameInterface, lSiteID, lSlotNumber, lTypes, systemType);
    }
    
    public void SellLaunchable(int lSiteID, int lSlotNumber, SystemType systemType)
    {
        currentTask = new SellLaunchableTask(gameInterface, lSiteID, lSlotNumber, systemType);
    }
    
    public void PurchaseMissileSystem()
    {
        currentTask = new PurchaseMissileSystemTask(gameInterface);
    }
    
    public void PurchaseAircraftSlotUpgrade(int lSiteID)
    {
        currentTask = new PurchaseAircraftSlotUpgradeTask(gameInterface, lSiteID);
    }
    
    public void PurchaseBankCapacityUpgrade(int lSiteID)
    {
        currentTask = new BankCapacityUpgradeTask(gameInterface, lSiteID);
    }

    public void PurchaseMissileSlotUpgradePlayer()
    {
        currentTask = new PurchaseSlotUpgradeTask(gameInterface, true);
    }
            
    public void PurchaseMissileSlotUpgrade(int lSiteID)
    {
        currentTask = new PurchaseSlotUpgradeTask(gameInterface, true, lSiteID);
    }
    
    public void PurchaseMissileSlotUpgradeToMax(int lSiteID)
    {
        currentTask = new MaxUpgradeSlotsTask(gameInterface, true, lSiteID);
    }
    
    public void PurchaseInterceptorSlotUpgradeToMax(int lSiteID)
    {
        currentTask = new MaxUpgradeSlotsTask(gameInterface, false, lSiteID);
    }

    public void PurchaseMissileReloadUpgradePlayer()
    {
        currentTask = new PurchaseReloadUpgradeTask(gameInterface, true);
    }
            
    public void PurchaseMissileReloadUpgrade(int lSiteID)
    {
        currentTask = new PurchaseReloadUpgradeTask(gameInterface, true, lSiteID);
    }
    
    public void PurchaseSAMSystem()
    {
        currentTask = new PurchaseSAMSystemTask(gameInterface);
    }

    public void PurchaseSAMSlotUpgradePlayer()
    {
        currentTask = new PurchaseSlotUpgradeTask(gameInterface, false);
    }
            
    public void PurchaseSAMSlotUpgrade(int lSiteID)
    {
        currentTask = new PurchaseSlotUpgradeTask(gameInterface, false, lSiteID);
    }

    public void PurchaseSAMReloadUpgradePlayer()
    {
        currentTask = new PurchaseReloadUpgradeTask(gameInterface, false);
    }
            
    public void PurchaseSAMReloadUpgrade(int lSiteID)
    {
        currentTask = new PurchaseReloadUpgradeTask(gameInterface, false, lSiteID);
    }
    
    public void PurchaseRadarRangeUpgrade(int lSiteID)
    {
        currentTask = new PurchaseRadarRangeUpgradeTask(gameInterface, lSiteID);
    }
    
    public void PurchaseRadarBoostUpgrade(int lSiteID)
    {
        currentTask = new PurchaseRadarBoostUpgradeTask(gameInterface, lSiteID);
    }
    
    public void PurchaseSentryRangeUpgrade(int lSiteID)
    {
        currentTask = new PurchaseSentryRangeUpgradeTask(gameInterface, lSiteID);
    }
    
    public void PurchaseCommandPostHPUpgrade(int lSiteID)
    {
        //currentTask = new PurchaseCommandPostHPUpgradeTask(gameInterface, lSiteID);
    }
    
    public void ConstructStructure(EntityType structureType, ResourceType type, int lCommandPostID, GeoCoord geoRemoteBuild, boolean bUseSubstitutes)
    {
        currentTask = new BuildStructureTask(gameInterface, structureType, type, lCommandPostID, geoRemoteBuild, bUseSubstitutes);
    }
    
    /**
     * Purchasing an existing aircraft type.
     * @param lAirbaseID the id of the airbase the aircraft will go in.
     * @param lAircraftTypeID the id of the aircraft type to purchase.
     */
    public void ConstructAircraft(EntityPointer homebase, EntityType type, boolean bUseSubstitutes)
    {
        currentTask = new BuildAircraftTask(gameInterface, homebase, type, bUseSubstitutes);
    }
    
    public void ConstructShip(int lShipyardID, EntityType type, boolean bUseSubstitutes)
    {
        currentTask = new BuildShipTask(gameInterface, lShipyardID, type, bUseSubstitutes);
    }
    
    public void SaveSubmarineDesign(String strDesignName, boolean bPublic, boolean bNuclear, float fltFuelCapacity, int lArmor, int lMissileCount, int lTorpedoCount, int lICBMCount)
    {
        currentTask = new SaveShipTask(gameInterface, strDesignName, bPublic, bNuclear, fltFuelCapacity, lArmor, lMissileCount, lTorpedoCount, lICBMCount);
    }
    
    public void SellEntity(EntityPointer pointer)
    {
        currentTask = new SellEntityTask(gameInterface, pointer);
    }
    
    public void SellMissileSystem()
    {
        currentTask = new SellSystemTask(gameInterface, true);
    }
    
    public void SellSAMSystem()
    {
        currentTask = new SellSystemTask(gameInterface, false);
    }
    
    public void SetOnlineOffline(int lSiteID, EntityType structureType, boolean bOnline)
    {
        currentTask = new SiteOnlineOfflineTask(gameInterface, lSiteID, structureType, bOnline);
    }
    
    public void SetMultipleOnlineOffline(List<Integer> SiteIDs, EntityType structureType, boolean bOnline)
    {
        currentTask = new SiteOnlineOfflineTask(gameInterface, SiteIDs, structureType, bOnline);
    }
    
    public void RepairEntity(EntityPointer pointer)
    {
        currentTask = new RepairTask(gameInterface, pointer);
    }
    
    public void HealPlayer()
    {
        currentTask = new HealTask(gameInterface);
    }
    
    public void GiveWealth(int lReceiver, int lAmount, ResourceType type)
    {
        currentTask = new GiveWealthTask(gameInterface, lReceiver, lAmount, type);
    }
    
    public void SetSAMSiteMode(EntityPointer pointer, byte cMode)
    {
        currentTask = new SAMSiteModeTask(gameInterface, pointer, cMode);
    }
    
    public void SetMultipleSAMSiteModes(List<EntityPointer> Pointers, byte cMode)
    {
        currentTask = new SAMSiteModeTask(gameInterface, Pointers, cMode);
    }
    
    public void SetICBMSiloMode(int lSiteID, byte cMode)
    {
        currentTask = new ICBMSiloModeTask(gameInterface, lSiteID, cMode);
    }
    
    public void SetMultipleICBMSiloModes(List<Integer> SiteIDs, byte cMode)
    {
        currentTask = new ICBMSiloModeTask(gameInterface, SiteIDs, cMode);
    }
    
    public void SetEntityName(EntityPointer pointer, String strName)
    {
        currentTask = new EntityRenameTask(gameInterface, pointer, strName);
    }
    
    public void SetSAMEngagementSpeed(int lSiteID, float fltSpeed)
    {
        currentTask = new SAMSiteEngagementSpeedTask(gameInterface, lSiteID, fltSpeed);
    }
    
    public void SetPlayerName(String strName)
    {
        currentTask = new RenameTask(gameInterface, strName, RenameTask.Context.Player);
    }
    
    public void SetAllianceName(String strName)
    {
        currentTask = new RenameTask(gameInterface, strName, RenameTask.Context.AllianceName);
    }
    
    public void SetAllianceDescription(String strName)
    {
        currentTask = new RenameTask(gameInterface, strName, RenameTask.Context.AllianceDescription);
    }
    
    public void CloseAccount()
    {
        currentTask = new CloseAccountTask(gameInterface);
    }
    
    public void SetAvatar(int lAvatarID, boolean bIsAlliance)
    {
        currentTask = new SetAvatarTask(gameInterface, lAvatarID, bIsAlliance);
    }
    
    public void UpgradeToNuclear(int lMissileSiteID)
    {
        currentTask = new UpgradeToNuclearTask(gameInterface, lMissileSiteID);
    }
    
    public void CreateAlliance(String strName, String strDescription, int lAvatarID)
    {
        currentTask = new CreateAllianceTask(gameInterface, strName, strDescription, lAvatarID);
    }
    
    public void JoinAlliance(int lAllianceID)
    {
        currentTask = new JoinAllianceTask(gameInterface, lAllianceID);
    }
    
    public void LeaveAlliance()
    {
        currentTask = new LeaveAllianceTask(gameInterface);
    }
    
    public void DeclareWar(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.WAR);
    }
    
    public void SurrenderWar(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.SURRENDER_PROPOSAL);
    }
    
    public void CancelAffiliation(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.CANCEL_AFFILIATION);
    }
    
    public void OfferAffiliation(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.AFFILIATION_REQUEST);
    }
    
    public void AcceptAffiliation(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.AFFILIATION);
    }
    
    public void RejectAffiliation(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.AFFILIATION_REJECT);
    }
    
    public void AcceptSurrender(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.ACCEPT_SURRENDER);
    }
    
    public void RejectSurrender(int lAllianceID)
    {
        currentTask = new TreatyTask(gameInterface, lAllianceID, Treaty.Type.REJECT_SURRENDER);
    }
    
    public void Promote(int lPromotee)
    {
        currentTask = new PromoteTask(gameInterface, lPromotee);
    }

    public void AcceptJoin(int lPlayer)
    {
        currentTask = new AllianceJoinTask(gameInterface, lPlayer, false);
    }

    public void RejectJoin(int lPlayer)
    {
        currentTask = new AllianceJoinTask(gameInterface, lPlayer, true);
    }

    public void Kick(int lPlayer)
    {
        currentTask = new KickTask(gameInterface, lPlayer);
    }

    public void Ban(String strReason, int lPlayer, boolean bPermanent)
    {
        currentTask = new BanTask(gameInterface, strReason, lPlayer, bPermanent);
    }
    
    public void Unban(int lPlayerID)
    {
        currentTask = new UnbanTask(gameInterface, lPlayerID);
    }

    public void ResetAvatar(int lPlayer)
    {
        currentTask = new AdminPlayerCommandTask(gameInterface, lPlayer, LaunchSession.ResetAvatar);
    }

    public void ResetName(int lPlayer)
    {
        currentTask = new AdminPlayerCommandTask(gameInterface, lPlayer, LaunchSession.ResetName);
    }
    
    public void DeviceCheck(boolean bCompleteFailure, boolean bAPIFailure, int lFailureCode, boolean bProfileMatch, boolean bBasicIntegrity)
    {
        currentTask = new DeviceCheckTask(gameInterface, bCompleteFailure, bAPIFailure, lFailureCode, bProfileMatch, bBasicIntegrity);
    }
    
    public boolean GetWarStats(int lWarID)
    {
        if(DirectCommsPossible())
        {
            TobComm tobComm = session.GetTobComm();
            tobComm.RequestObject(LaunchSession.Treaty, lWarID);
            return true;
        }
        
        return false;
    }
    
    public boolean GetPlayerStats(int lPlayerID)
    {
        if(DirectCommsPossible())
        {
            TobComm tobComm = session.GetTobComm();
            tobComm.RequestObject(LaunchSession.FullPlayerStats, lPlayerID);
            return true;
        }
        
        return false;
    }
    
    public boolean GetFullStats(EntityPointer entity)
    {
        if(DirectCommsPossible())
        {
            TobComm tobComm = session.GetTobComm();
            tobComm.SendObject(LaunchSession.FullStats, entity.GetData());
            return true;
        }
        
        return false;
    }
    
    public boolean GetLocationEntities(GeoRectangle geoRect)
    {
        if(DirectCommsPossible())
        {
            TobComm tobComm = session.GetTobComm();
            tobComm.SendObject(LaunchSession.LocationEntities, geoRect.GetData());
            return true;
        }
        
        return false;
    }
    
    public boolean GetTerrainData(GeoCoord geoCoord)
    {
        if(DirectCommsPossible())
        {
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putFloat(geoCoord.GetLatitude());
            bb.putFloat(geoCoord.GetLongitude());
            
            TobComm tobComm = session.GetTobComm();
            tobComm.SendObject(LaunchSession.TerrainDataRequest, bb.array());
            return true;
        }
        
        return false;
    }

    public boolean GetUserData(int lPlayerID)
    {
        if(DirectCommsPossible())
        {
            TobComm tobComm = session.GetTobComm();
            tobComm.RequestObject(LaunchSession.UserData, lPlayerID);
            return true;
        }

        return false;
    }
    
    /**
     * Report if experimental direct-comms (blast messages at the server without a task) are possible.
     * @return True if the connection seems established and stable. False otherwise.
     */
    public boolean DirectCommsPossible()
    {
        if(state == State.PROCESS)
        {
            if(session != null)
            {
                return session.IsAlive();
            }
        }
        
        return false;
    }
    
    public String GetState()
    {
        return state.name();
    }
    
    public String GetSessionState()
    {
        if(session == null)
            return "NULL";
        
        if(session.IsAlive())
            return session.GetState();
        
        return "DEAD";
    }
    
    public void MoveOrder(List<EntityPointer> Movables, Map<Integer, GeoCoord> Coordinates)
    {
        currentTask = new MoveOrderTask(gameInterface, Movables, Coordinates);
    }
    
    public void ToggleAircraftReturn(EntityPointer aircraft)
    {
        currentTask = new AircraftToggleReturnTask(gameInterface, aircraft);
    }
    
    public void ChangeAircraftHomebase(EntityPointer entity, EntityPointer homebase)
    {
        currentTask = new ChangeHomebaseTask(gameInterface, entity, homebase);
    }
    
    public void SpoofLocation(LaunchClientLocation spoofedLocation)
    {
        currentTask = new SpoofLocationTask(gameInterface, spoofedLocation);
    }
    
    public void BankAction(int lBankID, int lAmount, boolean bWithdraw)
    {
        currentTask = new BankActionTask(gameInterface, lBankID, lAmount, bWithdraw);
    }
    
    public void LinkDepots(int lFrom, int lTo)
    {
        currentTask = new LinkDepotTask(gameInterface, lFrom, lTo);
    }
    
    public void UnlinkDepot(int lDepotID)
    {
        currentTask = new UnlinkDepotTask(gameInterface, lDepotID);
    }
    
    public void AdminAccountTransfer(int lFrom, int lTo)
    {
        currentTask = new TransferAccountTask(gameInterface, lFrom, lTo);
    }
    
    public void SendMessage(int lReceiverID, MissileFactory.ChatChannel channel, String strMessage)
    {
        currentTask = new SendMessageTask(gameInterface, lReceiverID, channel, strMessage);
    }
    
    public void PlaceBlueprint(EntityType type, ResourceType resourceType, GeoCoord geoPosition)
    {
        currentTask = new PlaceBlueprintTask(gameInterface, type, resourceType, geoPosition);
    }
    
    public void FireElectronicWarfare(int lAircraftID, EntityPointer target)
    {
        currentTask = new ElectronicWarfareTask(gameInterface, lAircraftID, target);
    }
    
    public void SetAllianceTaxRate(float fltTaxRate)
    {
        currentTask = new SetAllianceTaxRateTask(gameInterface, fltTaxRate);
    }
    
    public void AllianceWithdraw(int lAmount)
    {
        currentTask = new AllianceWithdrawTask(gameInterface, lAmount);
    }
    
    public void AlliancePanic(int lAllianceID)
    {
        currentTask = new AlliancePanicTask(gameInterface, lAllianceID);
    }
    
    public void CallAirdrop()
    {
        currentTask = new CallAirdropTask(gameInterface);
    }
    
    public void PurchaseInfantry(int lArmoryID, boolean bUseSubstitutes)
    {
        currentTask = new PurchaseInfantryTask(gameInterface, lArmoryID, bUseSubstitutes);
    }
    
    public void PurchaseCargoTruck(int lWarehouseID, boolean bUseSubstitutes)
    {
        currentTask = new PurchaseCargoTruckTask(gameInterface, lWarehouseID, bUseSubstitutes);
    }
    
    public void PurchaseTank(int lArmoryID, EntityType type, boolean bUseSubstitutes)
    {
        currentTask = new PurchaseTankTask(gameInterface, lArmoryID, type, bUseSubstitutes);
    }
    
    public void PurchaseAircraft(EntityPointer origin, EntityType type, boolean bUseSubstitutes)
    {
        currentTask = new BuildAircraftTask(gameInterface, origin, type, bUseSubstitutes);
    }
    
    public void PurchaseShip(int lShipyardID, EntityType type, boolean bUseSubstitutes)
    {
        currentTask = new BuildShipTask(gameInterface, lShipyardID, type, bUseSubstitutes);
    }
    
    public void UnitCommand(MoveOrders command, List<EntityPointer> Commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver)
    {
        currentTask = new UnitCommandTask(gameInterface, command, Commandables, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver);
    }
    
    public void Prospect()
    {
        currentTask = new ProspectTask(gameInterface);
    }
    
    public void TransferCargo(EntityPointer entityFrom, EntityPointer entityTo, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver, boolean bLoadAsCargo, boolean bFromCargo)
    {
        currentTask = new TransferCargoTask(gameInterface, entityFrom, entityTo, typeToDeliver, lDeliverID, lQuantityToDeliver, bLoadAsCargo, bFromCargo);
    }
    
    public void NavalSpeedChange(EntityPointer vessel, boolean bIncrease)
    {
        currentTask = new NavalSpeedChangeTask(gameInterface, vessel, bIncrease);
    }
    
    public void DiveOrSurface(int lSubmarineID)
    {
        currentTask = new SubmarineDiveTask(gameInterface, lSubmarineID);
    }
    
    public void SonarPing(EntityPointer pinger)
    {
        currentTask = new SonarPingTask(gameInterface, pinger);
    }
    
    public void ToggleAirbaseOpen(EntityPointer host)
    {
        currentTask = new ToggleAirbaseOpenTask(gameInterface, host);
    }
    
    public void RadarScan(EntityPointer scanner)
    {
        currentTask = new RadarScanTask(gameInterface, scanner);
    }
    
    public void KickAircraft(EntityPointer aircraft)
    {
        currentTask = new KickAircraftTask(gameInterface, aircraft);
    }
    
    public void SetArtilleryTarget(EntityPointer pointer, GeoCoord geoTarget, float fltRadius)
    {
        currentTask = new SetArtilleryTargetTask(gameInterface, pointer, geoTarget, fltRadius);
    }
    
    public void CeaseFire(List<EntityPointer> Pointers)
    {
        currentTask = new CeaseFireTask(gameInterface, Pointers);
    }
    
    public void RefuelAircraftAtBase(EntityPointer airbase, EntityPointer aircraft)
    {
        currentTask = new RefuelAircraftTask(gameInterface, airbase, aircraft);
    }
    
    public void LoadLandUnit(EntityPointer unit, EntityPointer transport)
    {
        currentTask = new LoadLandUnitTask(gameInterface, unit, transport);
    }
    
    public void DropLandUnit(EntityPointer unit)
    {
        currentTask = new DropLandUnitTask(gameInterface, unit);
    }
    
    public void TransferLandUnit(EntityPointer sender, EntityPointer receiver, EntityPointer unit)
    {
        currentTask = new TransferLandUnitTask(gameInterface, sender, receiver, unit);
    }
    
    public void CaptureEntity(EntityPointer pointer)
    {
        currentTask = new CaptureEntityTask(gameInterface, pointer);
    }
    
    public void UpgradeShipyard(int lShipyardID)
    {
        currentTask = new UpgradeShipyardTask(gameInterface, lShipyardID);
    }
    
    public void AdminDelete(EntityPointer pointer)
    {
        currentTask = new AdminDeleteTask(gameInterface, pointer);
    }
    
    public void ListOnMarket(LaunchClientGameInterface gameInterface, int lMarketID, EntityPointer pointerDeliverer, LootType typeOfListing, int lTypeID, int lQuantity, float fltPriceEach)
    {
        currentTask = new ListOnMarketTask(gameInterface, lMarketID, pointerDeliverer, typeOfListing, lTypeID, lQuantity, fltPriceEach);
    }
    
    public void PurchaseListing(int lListingID, int lAmountToPurchase)
    {
        currentTask = new PurchaseListingTask(gameInterface, lListingID, lAmountToPurchase);
    }
    
    public void Obliterate(int lPlayerID)
    {
        currentTask = new ObliteratePlayerTask(gameInterface, lPlayerID);
    }
    
    public void MigrateAccount(byte[] cIMEI, String strGoogleID)
    {
        //currentTask = new MigrateAccountTask(gameInterface, cIMEI, strGoogleID);
        
        if(DirectCommsPossible())
        {
            ByteBuffer bb = ByteBuffer.allocate(cDeviceID.length + 3 + LaunchUtilities.GetStringDataSize(strGoogleID));
            bb.put(cDeviceID);
            bb.put(LaunchUtilities.GetStringData(strGoogleID));
            
            TobComm tobComm = session.GetTobComm();
            tobComm.SendObject(LaunchSession.MigrateAccount, bb.array());
        }
    }
    
    public void NotifySubscriptionUpdate(boolean bSubscribed)
    {
        currentTask = new UpdateSubscriptionTask(gameInterface, bSubscribed);
    }
    
    public void VerifyPurchase(String strPurchaseToken)
    {
        if(DirectCommsPossible())
        {
            ByteBuffer bb = ByteBuffer.allocate(LaunchUtilities.GetStringDataSize(strPurchaseToken));
            bb.put(LaunchUtilities.GetStringData(strPurchaseToken));
            
            TobComm tobComm = session.GetTobComm();
            tobComm.SendObject(LaunchSession.VerifyPurchase, bb.array());
        }
        else
        {
            LaunchLog.ConsoleMessage("Direct comms not possible. Abandoning VerifyPurchase...");
        }
    }
    
    public void ToggleTruckAutoCollect(int lTruckID)
    {
        currentTask = new ToggleTruckAutoCollectTask(gameInterface, lTruckID);
    }
    
    public void Blacklist(int lPlayerID)
    {
        currentTask = new BlacklistPlayerTask(gameInterface, lPlayerID);
    }
}
