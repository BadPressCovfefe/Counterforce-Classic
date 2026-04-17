/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm;

import launch.game.entities.conceptuals.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import tobcomm.TobComm;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import launch.game.Alliance;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchGame;
import launch.game.LaunchServerGameInterface;
import launch.game.User;
import launch.game.treaties.Treaty;
import launch.game.entities.*;
import launch.game.entities.MissileFactory.ChatChannel;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoRectangle;
import launch.game.LaunchGame.Allegiance;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem.LootType;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.*;
import launch.utilities.*;
import static launch.utilities.LaunchLog.LogType.CHEATING;
import static launch.utilities.LaunchLog.LogType.DEVICE_CHECKS;

/**
 *
 * @author tobster
 */
public class LaunchServerSession extends LaunchSession
{
    private static final int SERVER_TIMEOUT = 60000;
    
    private static Object AvatarMutex = new Object(); //To synchronise I/O on new avatars, preventing multiple use of the same filename with concurrent avatar uploads.
    private static int lNewAvatarID = Defs.THE_GREAT_BIG_NOTHING;
    
    private int lID;
    
    private LaunchServerGameInterface gameInterface;
    
    private boolean bRegistered = false;
    private User AuthenticatedUser = null;
    private boolean bSendingReport = false;
    private boolean bCanReceiveUpdates = false;
    
    //private List<MapEntity> CameraChangedEntities = new ArrayList<>();
    private GeoRectangle geoCameraBounds;
    
    private String strIPAddress;
    
    private int lSnapshotReports = 0;
    
    public static final Random random = new Random();
    
    public LaunchServerSession(int lID, Socket socket, LaunchServerGameInterface gameInterface)
    {
        super(socket);
        this.lID = lID;
        strIPAddress = connection.GetAddress();
        this.gameInterface = gameInterface;
        Start();
    }
    
    @Override
    protected void Process()
    {
        if(bRegistered && bCanReceiveUpdates)
        {
            //Send events when we can and when applicable.
            if(!bSendingReport)
            {
                if(AuthenticatedUser.HasReports())
                {
                    bSendingReport = true;
                    LaunchReport report = AuthenticatedUser.GetNextReport();
                    
                    if(report != null)
                        tobComm.SendObject(Report, 0, 0, report.GetData());
                }
            }
        }
    }

    @Override
    public void ObjectReceived(int lObject, int lInstanceNumber, int lOffset, byte[] cData)
    {
        ByteBuffer bb = ByteBuffer.wrap(cData);
        
        switch(lObject)
        {
            case Authorise:
            {
                byte[] cDeviceID = new byte[Security.SHA256_SIZE];
                bb.get(cDeviceID);
                String strGoogleID = LaunchUtilities.StringFromData(bb);
                short nMajorVersion = bb.getShort();
                String strIMEI = Security.BytesToHexString(cDeviceID); //This is the old way of identifying accounts.
                String strDeviceName = LaunchUtilities.StringFromData(bb);
                String strDataDirectory = LaunchUtilities.StringFromData(bb);
                GeoCoord geoLogin = new GeoCoord(bb.getFloat(), bb.getFloat());
                byte cFlags = bb.get();

                if(nMajorVersion != Defs.MAJOR_VERSION)
                {
                    tobComm.SendCommand(MajorVersionInvalid);
                }
                else
                {
                    //Attempt to verify user via Google id. If this is null, check the IMEI.
                    User user = gameInterface.VerifyID(strGoogleID);

                    if(user != null)
                    {
                        user.SetLastNetwork(strIPAddress, Defs.IsMobile(cFlags));

                        switch(user.GetBanState())
                        {
                            case NOT:
                            {
                                Player player = gameInterface.GetGame().GetPlayer(user.GetPlayerID());

                                if(player != null && (strDataDirectory.length() > Defs.EXPECTED_DATA_DIR_LENGTH || !strDataDirectory.contains(Defs.DATA_DIR_SHOULD_MATCH)))
                                {
                                        gameInterface.AdminReport(new LaunchReport(String.format("Player %s has funky data directory %s. Probable emulator.", player.GetName(), strDataDirectory), true, player.GetID()));
                                        gameInterface.GetCounterShield().checkFingerprintAsync(player.GetID(), strDeviceName);
                                }

                                if(user.GetDeviceID().isEmpty())
                                {
                                    user.SetDeviceID(strDeviceName);
                                }
                                else
                                {
                                    if(player != null && !user.GetDeviceID().equals(strDeviceName))
                                    {
                                        //TODO: If deviceID has changed AND the player is now far away from where they were before, deny the login, as this is most likely a location exploit where players exchange login info.
                                        gameInterface.AdminReport(new LaunchReport(String.format("Player %s device hash changed, which is strange. Device is now a %s.", player.GetName(), strDeviceName), true, player.GetID()));
                                        
                                        gameInterface.GetCounterShield().onDeviceHashUpdate(System.currentTimeMillis(), player.GetID(), strDeviceName);
                                        gameInterface.GetCounterShield().checkFingerprintAsync(player.GetID(), strDeviceName);

                                        if(player.GetPosition().DistanceTo(geoLogin) >= Defs.LOGIN_LOCATION_SPOOF_THRESHOLD)
                                        {
                                            gameInterface.AdminReport(new LaunchReport(String.format("Player %s is attempting to login from a different device far away from their last login.", player.GetName(), strDeviceName), true, player.GetID()));
                                            //tobComm.SendCommand(LocationSpoof);
                                            //break;
                                        }
                                    }
                                }
                                
                                if(player != null)
                                {
                                    bRegistered = true;
                                    AuthenticatedUser = user;
                                }

                                ByteBuffer bbReply = ByteBuffer.allocate(12);
                                bbReply.putShort(Defs.MAJOR_VERSION);
                                bbReply.putShort(Defs.MINOR_VERSION);
                                bbReply.putInt(gameInterface.GetGameConfigChecksum());
                                bbReply.putInt(user.GetPlayerID());

                                tobComm.SendObject(Authorise, 0, 0, bbReply.array());

                                tobComm.SendCommand(UpdateToken);

                                //Request device check if not up to date.
                                if(AuthenticatedUser.DeviceCheckRequired())
                                {
                                    LaunchLog.Log(DEVICE_CHECKS, player.GetName(), "Device checks required. Requesting...");
                                    tobComm.RequestObject(DeviceCheck);
                                }
                            }
                            break;

                            case TIME_BANNED_ACK:
                            case TIME_BANNED:
                            {
                                user.AckBan();

                                //Banned. Send reason and duration.
                                String strBanReason = user.GetBanReason();
                                int lBanReasonLength = LaunchUtilities.GetStringDataSize(strBanReason);
                                byte[] cBanReasonBytes = LaunchUtilities.GetStringData(strBanReason);

                                ByteBuffer bbReply = ByteBuffer.allocate(8 + lBanReasonLength);
                                bbReply.putLong(user.GetBanDurationRemaining());
                                bbReply.put(cBanReasonBytes);

                                tobComm.SendObject(BanData, bbReply.array());
                            }
                            break;

                            case PERMABANNED:
                            {
                                //Permabanned. Send reason.
                                String strBanReason = user.GetBanReason();
                                int lBanReasonLength = LaunchUtilities.GetStringDataSize(strBanReason);
                                byte[] cBanReasonBytes = LaunchUtilities.GetStringData(strBanReason);

                                ByteBuffer bbReply = ByteBuffer.allocate(lBanReasonLength);
                                bbReply.put(cBanReasonBytes);

                                tobComm.SendObject(PermBanData, bbReply.array());
                            }
                            break;
                        }
                    }
                    else if(gameInterface.VerifyID(strIMEI) != null)
                    {
                        //The user's account is using the old IMEI method of authorization. Migrate them to Google.
                        LaunchLog.ConsoleMessage("gameInterface.VerifyID(strIMEI) != null [LaunchServerSession]");
                        tobComm.SendCommand(AccountUnmigrated);
                    }
                    else
                    {
                        tobComm.SendCommand(AccountUnregistered);
                    }
                }
            }
            break;
            
            case Registration:
            {
                String strGoogleID = LaunchUtilities.StringFromData(bb);
                String strPlayerName = LaunchUtilities.StringFromData(bb);
                byte[] cDeviceID = new byte[Security.SHA256_SIZE];
                bb.get(cDeviceID);
                String strIMEI = Security.BytesToHexString(cDeviceID);
                int lAvatarID = bb.getInt();

                LaunchLog.ConsoleMessage("Attempting to register new player:");
                LaunchLog.ConsoleMessage("GoogleID: " + strGoogleID);
                LaunchLog.ConsoleMessage("PlayerName: " + strPlayerName);
                LaunchLog.ConsoleMessage("DeviceID: " + cDeviceID.toString());
                LaunchLog.ConsoleMessage("AvatarID: " + lAvatarID);

                if (strPlayerName.length() > Defs.MAX_PLAYER_NAME_LENGTH)
                {
                    tobComm.SendCommand(PlayerNameTooLong);
                }
                else if (strPlayerName.length() == 0)
                {
                    tobComm.SendCommand(PlayerNameTooShort);
                }
                else if (!gameInterface.CheckPlayerNameAvailable(strPlayerName))
                {
                    tobComm.SendCommand(NameTaken);
                }
                else if((gameInterface.CheckDeviceIDAlreadyUsed(cDeviceID)))
                {
                    tobComm.SendCommand(DeviceIDUsed);
                }
                else if(gameInterface.VerifyID(strGoogleID) != null)
                {
                    tobComm.SendCommand(AlreadyRegistered);
                }
                else
                {
                    // All checks passed — create the new account
                    User newUser = gameInterface.CreateAccount(strIMEI, strGoogleID, strPlayerName, lAvatarID);
                    newUser.SetGoogleID(strGoogleID); //Store raw Google ID for later login verification

                    tobComm.SendCommand(AccountCreateSuccess);

                    if(gameInterface.GetIpAddressProscribed(strIPAddress))
                    {
                        newUser.Proscribe();
                        gameInterface.NotifyIPProscribed(newUser);
                    }
                }
            }
            break;

            case MigrateAccount:
            {
                byte[] cDeviceID = new byte[Security.SHA256_SIZE];
                bb.get(cDeviceID);
                String strGoogleID = LaunchUtilities.StringFromData(bb);
                String strIMEI = Security.BytesToHexString(cDeviceID); //This is the old way of identifying accounts.

                if(gameInterface.VerifyID(strIMEI) != null)
                {
                    LaunchLog.ConsoleMessage("case MigrateAccount gameInterface.VerifyID(strIMEI) != null [LaunchServerSession]");
                    
                    if(gameInterface.MigrateAccount(strIMEI, strGoogleID))
                    {
                        LaunchLog.ConsoleMessage("Sending command AccountMigrated to client...");
                        tobComm.SendCommand(AccountMigrated);
                    }
                }
                else
                {
                    LaunchLog.ConsoleMessage("case MigrateAccount FAILED gameInterface.VerifyID(strIMEI) == null [LaunchServerSession]");
                }
            }
            break;
            
            case Avatar:
            {
                synchronized(AvatarMutex)
                {
                    File file = new File(String.format(Defs.IMAGE_FILE_FORMAT, Defs.LOCATION_AVATARS, lNewAvatarID));
                    
                    while(file.exists() || lNewAvatarID == Defs.THE_GREAT_BIG_NOTHING)
                    {
                        lNewAvatarID++;
                        file = new File(String.format(Defs.IMAGE_FILE_FORMAT, Defs.LOCATION_AVATARS, lNewAvatarID));
                    }

                    try
                    {
                        FileOutputStream out = new FileOutputStream(file);
                        out.write(cData);
                        out.close();
                        
                        RandomAccessFile rafImage = new RandomAccessFile(String.format(Defs.IMAGE_FILE_FORMAT, Defs.LOCATION_AVATARS, lNewAvatarID), "r");
                        byte[] cResult = new byte[(int)rafImage.length()];
                        rafImage.readFully(cResult);
                        
                        tobComm.SendObject(Avatar, lNewAvatarID, cResult);
                    }
                    catch (IOException ex)
                    {
                        tobComm.SendCommand(ActionFailed, 0);
                    }
                }
            }
            break;
            
            default:
            {
                if(bRegistered)
                {
                    switch(lObject)
                    {                        
                        case TerrainDataRequest:
                        {
                            GeoCoord geoCoord = new GeoCoord(bb.getFloat(), bb.getFloat());
                            
                            TerrainData data = new TerrainData(TerrainChecker.CoordinateIsWater(geoCoord));
                            tobComm.SendObject(LaunchSession.TerrainData, data.GetData());
                        }
                        break;
                        
                        case FullStats:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                            LaunchEntity entity = pointer.GetEntity(gameInterface.GetGame());
                            tobComm.SendObject(entity.GetSessionCode(), entity.GetFullStatsData(AuthenticatedUser.GetPlayerID()));
                        }
                        break;
            
                        case LocationUpdate:
                        {
                            LaunchClientLocation location = new LaunchClientLocation(bb);
                            Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                            
                            //Check for proscribed locations if this is the player's first location.
                            if(!player.GetPosition().GetValid())
                            {
                                if(gameInterface.GetLocationProscribed(location.GetGeoCoord()))
                                {
                                    AuthenticatedUser.Proscribe();
                                    gameInterface.NotifyLocationProscribed(AuthenticatedUser);
                                }
                            }     
                                
                            gameInterface.UpdatePlayerLocation(AuthenticatedUser.GetPlayerID(), location);
                            
                            //Reply with KeepAlive, to provide the client with a latency measurement.
                            tobComm.SendCommand(KeepAlive);
                            
                            //Check for location spoofing, and send a warning if it's hit.
                            LocationSpoofCheck spoofCheck = new LocationSpoofCheck(AuthenticatedUser.GetPreviousLocation(), location);

                            if(spoofCheck.GetType() == LocationSpoofCheck.Type.SPOOF)
                            {
                                //Register possible spoof, but do nothing yet (it gets confirmed with process names).
                                tobComm.RequestObject(ProcessNames);

                                if(!player.GetBoss())
                                    LaunchLog.Log(CHEATING, player.GetName(), String.format("Possible GPS spoof. Distance %skm, Speed %skph.", spoofCheck.GetDistance(), spoofCheck.GetSpeed()));

                                gameInterface.SpoofWarnings(AuthenticatedUser.GetPlayerID(), spoofCheck);
                            }
                            else if(random.nextFloat() < Defs.PROCESS_CHECK_RANDOM_CHANCE) //Randomly request at low frequency as well as first time.
                            {
                                //Demand list of running apps for the first location for spoof-on-entry detection and spot checks.
                                tobComm.RequestObject(ProcessNames);
                            }

                            //Check for multiaccounting if applicable.
                            if(AuthenticatedUser.DoMultiAccountDetection())
                            {
                                gameInterface.MultiAccountingCheck(AuthenticatedUser.GetPlayerID());
                            }

                            /*LaunchLog.Log(LOCATIONS, player.GetName(), String.format("(%.6f, %.6f) @ %.1fm %s - %s %.1fm %.1fkph",
                                    location.GetLatitude(),
                                    location.GetLongitude(),
                                    location.GetAccuracy(),
                                    location.GetLocationTypeName(),
                                    spoofCheck.GetType().name(),
                                    spoofCheck.GetDistance(),
                                    spoofCheck.GetSpeed()));*/

                            AuthenticatedUser.SetPreviousLocation(location);
                            
                            //Clear alarm statuses.
                            AuthenticatedUser.ClearAlarms();
                        }
                        break;
                        
                        case ProcessNames:
                        {
                            String strProcessNames = LaunchUtilities.StringFromData(bb);
                            
                            //LaunchLog.Log(CHEATING, strLogName, String.format("Got the following list of processes:\n%s.", strProcessNames));
                            String strPlayerName = gameInterface.GetPlayerName(AuthenticatedUser.GetPlayerID());
                            
                            //Store a hash of it.
                            try
                            {
                                String strAppListHash = Security.BytesToHexString(Security.GetSHA256(strProcessNames.getBytes())).substring(0, User.INFO_HASH_LENGTH);
                                AuthenticatedUser.SetAppListShortHash(strAppListHash);
                            }
                            catch (NoSuchAlgorithmException ex) { /* All devices that can run the launch server should support SHA256 or nothing will work! */ }
                            
                            int lLines = strProcessNames.split("\n").length;
                            
                            if(lLines <= Defs.BANNABLY_LOW_PROCESS_NUMBER)
                            {
                                gameInterface.PermaBan(AuthenticatedUser.GetPlayerID(), "Blocking the game from seeing device processes.", "[SERVER]");
                                
                                Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                                gameInterface.AdminReport(new LaunchReport(String.format("Player %s was banned by the server for hiding device processes. Processes: %d.", strPlayerName, lLines), true, player.GetID()));
                            }
                            else if(lLines <= Defs.SUSPICIOUSLY_LOW_PROCESS_NUMBER)
                            {
                                Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                                gameInterface.AdminReport(new LaunchReport(String.format("Player %s has a suspiciously low number of processes (%d).", strPlayerName, lLines), true, player.GetID()));
                            }
                            
                            for(LaunchBannedApp bannedApp : gameInterface.GetGame().GetConfig().GetMajorCheatingApps())
                            {
                                if(bannedApp.Matches(strProcessNames))
                                {
                                    gameInterface.PermaBan(AuthenticatedUser.GetPlayerID(), String.format("Having %s (%s).", bannedApp.GetName(), bannedApp.GetDescription()), "[SERVER]");
                                    LaunchLog.Log(LaunchLog.LogType.CHEATING, strPlayerName, String.format("User has majorly banned app %s", bannedApp.GetName()));
                                    return;
                                }
                            }
                            
                            List<LaunchBannedApp> NaughtyApps = new ArrayList();
                            
                            for(LaunchBannedApp bannedApp : gameInterface.GetGame().GetConfig().GetMinorCheatingApps())
                            {
                                if(bannedApp.Matches(strProcessNames))
                                {
                                    LaunchLog.Log(LaunchLog.LogType.CHEATING, strPlayerName, String.format("User has majorly banned app %s", bannedApp.GetName()));
                                    NaughtyApps.add(bannedApp);
                                }
                            }
                            
                            if(NaughtyApps.size() == 1)
                            {
                                gameInterface.TempBan(AuthenticatedUser.GetPlayerID(), String.format("Having %s (%s).", NaughtyApps.get(0).GetName(), NaughtyApps.get(0).GetDescription()), "[SERVER]");
                            }
                            else if(NaughtyApps.size() > 1)
                            {
                                String strReason = "Having the following:";
                                
                                for(LaunchBannedApp app : NaughtyApps)
                                {
                                    strReason += "\n";
                                    strReason += String.format("%s (%s)", app.GetName(), app.GetDescription());
                                }
                                
                                gameInterface.TempBan(AuthenticatedUser.GetPlayerID(), strReason, "[SERVER]");
                            }
                        }
                        break;
                        
                        case KickAircraft:
                        {
                            EntityPointer aircraft = new EntityPointer(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.KickAircraft(AuthenticatedUser.GetPlayerID(), aircraft),
                                    "Aircraft kicked.",
                                    "Could not kick aircraft.");
                        }
                        break;
                        
                        case PurchaseTank:
                        {
                            int lArmoryID = bb.getInt();
                            EntityType type = EntityType.values()[bb.get()];
                            boolean bUseSubstitutes = (bb.get() != 0x00);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseTank(AuthenticatedUser.GetPlayerID(), lArmoryID, type, bUseSubstitutes),
                                    "Tank purchased.",
                                    "Could not purchase tank.");
                        }
                        break;
                        
                        case PurchaseInfantry:
                        {
                            int lArmoryID = bb.getInt();
                            boolean bUseSubstitutes = (bb.get() != 0x00);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseInfantry(AuthenticatedUser.GetPlayerID(), lArmoryID, bUseSubstitutes),
                                    "Infantry purchased.",
                                    "Could not purchase infantry.");
                        }
                        break;

                        case BuildStructure:
                        {
                            EntityType structureType = EntityType.values()[bb.get()];
                            ResourceType type = ResourceType.values()[bb.get()];
                            int lCommandPostID = bb.getInt();
                            GeoCoord geoRemoteBuild = null;
                            
                            if(bb.get() != 0x00)
                                geoRemoteBuild = new GeoCoord(bb.getFloat(), bb.getFloat());
                            
                            boolean bUseSubstitutes = (bb.get() != 0x00);

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ConstructStructure(AuthenticatedUser.GetPlayerID(), structureType, type, lCommandPostID, geoRemoteBuild, bUseSubstitutes),
                                    "Player purchased a structure.",
                                    "Player couldn't build a structure.");
                        }
                        break;
                        
                        case PlaceBlueprint:
                        {
                            EntityType structureType = EntityType.values()[bb.get()];
                            ResourceType resourceType = ResourceType.values()[bb.get()];
                            GeoCoord geoPosition = new GeoCoord(bb.getFloat(), bb.getFloat());

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PlaceBlueprint(AuthenticatedUser.GetPlayerID(), structureType, resourceType, geoPosition),
                                    "Player placed a blueprint.",
                                    "Player couldn't placed a blueprint.");
                        }
                        break;
                        
                        case SellLaunchable:
                        {
                            int lSiteID = bb.getInt();
                            int lSlotNo = bb.getInt();
                            SystemType systemType = SystemType.values()[bb.get()];
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SellLaunchable(AuthenticatedUser.GetPlayerID(), lSiteID, lSlotNo, systemType),
                                    "Player sold a launchable.",
                                    "Player couldn't sell a launchable.");
                        }
                        break;

                        case PurchaseLaunchables:
                        {
                            int lMissileSiteID = bb.getInt();
                            int lSlotNo = bb.getInt();
                            List<Integer> lTypes = LaunchUtilities.IntListFromData(bb);
                            SystemType systemType = SystemType.values()[bb.get()];

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseLaunchables(AuthenticatedUser.GetPlayerID(), lMissileSiteID, lSlotNo, lTypes, systemType),
                                    "Player purchased launchables.",
                                    "Player couldn't purchase launchables.");
                        }
                        break;
						
                        case BankAction:
                        {
                            int lBankID = bb.getInt();
                            int lAmount = bb.getInt();
                            boolean bWithdraw = (bb.get() != 0x00);

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                gameInterface.BankAction(AuthenticatedUser.GetPlayerID(), lBankID, lAmount, bWithdraw),
                                "Bank action performed.",
                                "Could not perform banking action.");
                        }
                        break;
                        
                        case ElectronicWarfare:
                        {
                            int lAircraftID = bb.getInt();
                            EntityPointer target = new EntityPointer(bb);

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ElectronicWarfare(AuthenticatedUser.GetPlayerID(), lAircraftID, target),
                                    "Aircraft used electronic warfare.",
                                    "Player couldn't use electronic warfare.");
                        }
                        break;
                        
                        case CeaseFire:
                        {
                            List<EntityPointer> Pointers = LaunchUtilities.PointerListFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CeaseFire(AuthenticatedUser.GetPlayerID(), Pointers),
                                    "Ceased fire.",
                                    "Could not cease fire.");
                        }
                        break;
                        
                        case LaunchMissile:
                        {
                            int lSiteID = bb.getInt();
                            int lSlotNo = bb.getInt();
                            float fltTargetLatitude = bb.getFloat();
                            float fltTargetLongitude = bb.getFloat();
                            EntityPointer target = null;
                            
                            if(bb.get() != 0x00)
                                target = new EntityPointer(bb);
                            
                            SystemType systemType = SystemType.values()[bb.get()];
                            boolean bAirburst = (bb.get() != 0x00);

                            if(true/*!AuthenticatedUser.AccountRestricted()*/)
                            {
                                HandleSimpleResult(tobComm, lInstanceNumber,
                                        gameInterface.LaunchMissile(AuthenticatedUser.GetPlayerID(), lSiteID, lSlotNo, fltTargetLatitude, fltTargetLongitude, target, systemType, bAirburst),
                                        "Missile launched.",
                                        "Could not launch missile.");
                            }
                            else
                            {
                                tobComm.SendCommand(DisplayGeneralError);
                                gameInterface.NotifyAccountRestricted(AuthenticatedUser);
                            }
                        }
                        break;
                        
                        case LaunchInterceptor:
                        {
                            int lSiteID = bb.getInt();
                            int lSlotNo = bb.getInt();
                            int lTargetID = bb.getInt();
                            EntityType targetType = EntityType.values()[bb.get()];
                            SystemType systemType = SystemType.values()[bb.get()];

                            if(true/*!AuthenticatedUser.AccountRestricted()*/)
                            {
                                HandleSimpleResult(tobComm, lInstanceNumber,
                                        gameInterface.LaunchInterceptor(AuthenticatedUser.GetPlayerID(), lSiteID, lSlotNo, lTargetID, targetType, systemType),
                                        "Interceptor launched.",
                                        "Could not launch interceptor.");
                            }
                            else
                            {
                                tobComm.SendCommand(DisplayGeneralError);
                                gameInterface.NotifyAccountRestricted(AuthenticatedUser);
                            }
                        }
                        break;
                        
                        case LaunchTorpedo:
                        {
                            int lSiteID = bb.getInt();
                            int lSlotNo = bb.getInt();
                            float fltTargetLatitude = bb.getFloat();
                            float fltTargetLongitude = bb.getFloat();
                            EntityPointer target = null;
                            if(bb.get() != 0x00)
                                target = new EntityPointer(bb);
                            SystemType systemType = SystemType.values()[bb.get()];

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.LaunchTorpedo(AuthenticatedUser.GetPlayerID(), lSiteID, lSlotNo, fltTargetLatitude, fltTargetLongitude, target, systemType),
                                    "Torpedo launched.",
                                    "Could not launch torpedo.");
                        }
                        break;
                        
                        case SendMessage:
                        {
                            int lReceiverID = bb.getInt();
                            ChatChannel channel = ChatChannel.values()[bb.get()];
                            String strMessage = LaunchUtilities.StringFromData(bb);

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SendMessage(AuthenticatedUser.GetPlayerID(), lReceiverID, channel, strMessage),
                                    "Message sent.",
                                    "Could not send message.");
                        }
                        break;
                        
                        case SAMSiteModeChange:
                        {
                            byte cMode = bb.get();
                            List<EntityPointer> Pointers = LaunchUtilities.PointerListFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetSAMSiteModes(AuthenticatedUser.GetPlayerID(), Pointers, cMode),
                                    "SAM site mode changed.",
                                    "Could not change SAM site mode.");
                        }
                        break;
                        
                        case SetArtilleryTarget:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                            GeoCoord geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
                            float fltRadius = bb.getFloat();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetArtilleryTarget(AuthenticatedUser.GetPlayerID(), pointer, geoTarget, fltRadius),
                                    "Artillery target set.",
                                    "Could not set artillery target.");
                        }
                        break;
                            
                        case ICBMSiloModeChange:
                        {
                            byte cMode = bb.get();
                            
                            List<Integer> SiteIDs = new ArrayList();
                            
                            if(lInstanceNumber != MapEntity.ID_NONE)
                            {
                                SiteIDs.add(lInstanceNumber);
                            }
                            else
                            {
                                SiteIDs = LaunchUtilities.IntListFromData(bb);
                            }
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetICBMSiloModes(AuthenticatedUser.GetPlayerID(), SiteIDs, cMode),
                                    "ICBM silo mode changed.",
                                    "Could not change ICBM silo mode.");
                        }
                        break;
                        
                        case EngagementSpeedChange:
                        {
                            int lSiteID = bb.getInt();
                            float fltDistance = bb.getFloat();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetSAMSiteEngagementSpeed(AuthenticatedUser.GetPlayerID(), lSiteID, fltDistance),
                                    "SAM site engagement distance changed.",
                                    "Could not change SAM site engagement distance.");
                        }
                        break;
                            
                        case EntityNameChange:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                            String strName = LaunchUtilities.StringFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetEntityName(AuthenticatedUser.GetPlayerID(), pointer, strName),
                                    "Entity name changed.",
                                    "Could not change entity name.");
                        }
                        break;
                        
                        case CreateAlliance:
                        {
                            String strName = LaunchUtilities.StringFromData(bb);
                            String strDescription = LaunchUtilities.StringFromData(bb);
                            int lAvatarID = bb.getInt();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CreateAlliance(AuthenticatedUser.GetPlayerID(), strName, strDescription, lAvatarID),
                                    "Alliance created.",
                                    "Could not create alliance.");
                        }
                        break;
                        
                        case RenamePlayer:
                        {
                            String strName = LaunchUtilities.StringFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ChangePlayerName(AuthenticatedUser.GetPlayerID(), strName),
                                    "Player renamed.",
                                    "Could not rename player.");
                        }
                        break;
                        
                        case RenameAlliance:
                        {
                            String strName = LaunchUtilities.StringFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ChangeAllianceName(AuthenticatedUser.GetPlayerID(), strName),
                                    "Alliance renamed.",
                                    "Could not rename alliance.");
                        }
                        break;
                        
                        case RedescribeAlliance:
                        {
                            String strDescription = LaunchUtilities.StringFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ChangeAllianceDescription(AuthenticatedUser.GetPlayerID(), strDescription),
                                    "Alliance redescribed.",
                                    "Could not redescribe alliance.");
                        }
                        break;
                            
                        case RepairEntity:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RepairEntity(AuthenticatedUser.GetPlayerID(), pointer),
                                    "Entity repaired.",
                                    "Could not repair entity.");
                        }
                        break;
                        
                        case RefuelAircraft:
                        {
                            EntityPointer airbase = new EntityPointer(bb);
                            EntityPointer aircraft = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RefuelAircraftAtBase(AuthenticatedUser.GetPlayerID(), airbase, aircraft),
                                    "Aircraft refueled.",
                                    "Could not refuel aircraft.");
                        }
                        break;
                        
                        case LoadLandUnit:
                        {
                            EntityPointer unit = new EntityPointer(bb);
                            EntityPointer transport = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.LoadLandUnit(AuthenticatedUser.GetPlayerID(), unit, transport),
                                    "Unit loaded.",
                                    "Could not load unit.");
                        }
                        break;
                        
                        case DropLandUnit:
                        {
                            EntityPointer unit = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.DropLandUnit(AuthenticatedUser.GetPlayerID(), unit),
                                    "Unit dropped.",
                                    "Could not drop unit.");
                        }
                        break;
                        
                        
                        case CaptureEntity:
                        {
                            EntityPointer structure = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CaptureEntity(AuthenticatedUser.GetPlayerID(), structure),
                                    "Structure captured.",
                                    "Could not capture structure.");
                        }
                        break;
                        
                        case AdminDelete:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AdminDelete(AuthenticatedUser.GetPlayerID(), pointer),
                                    "Entity deleted.",
                                    "Could not delete entity.");
                        }
                        break;
                        
                        case TransferLandUnit:
                        {
                            EntityPointer sender = new EntityPointer(bb);
                            EntityPointer receiver = new EntityPointer(bb);
                            EntityPointer unit = new EntityPointer(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.TransferLandUnit(AuthenticatedUser.GetPlayerID(), sender, receiver, unit),
                                    "Unit transferred.",
                                    "Could not transfer unit.");
                        }
                        break;
                            
                        case SellEntity:
                        {
                            EntityPointer pointer = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SellEntity(AuthenticatedUser.GetPlayerID(), pointer),
                                    "Entity sold.",
                                    "Could not sell entity.");
                        }
                        break;
                        
                        case StructuresOnOff:
                        {
                            boolean bOnline = bb.get() != 0x00;
                            List<Integer> SiteIDs = new ArrayList();
                            
                            if(lInstanceNumber != MapEntity.ID_NONE)
                            {
                                SiteIDs.add(lInstanceNumber);
                            }
                            else
                            {
                                SiteIDs = LaunchUtilities.IntListFromData(bb);
                            }
                                
                            EntityType structureType = EntityType.values()[bb.get()];
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                gameInterface.SetStructuresOnOff(AuthenticatedUser.GetPlayerID(), SiteIDs, bOnline, structureType),
                                "Missile site set online/offline.",
                                "Could not set missile site online/offline.");
                        }
                        break;
                        
                        case DeviceCheck:
                        {
                            Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                            
                            boolean bCompleteFailure = bb.get() != 0x00;
                            boolean bAPIFailure = bb.get() != 0x00;
                            int lFailureCode = bb.getInt();
                            boolean bProfileMatch = bb.get() != 0x00;
                            boolean bBasicIntegrity = bb.get() != 0x00;
                            
                            if(bCompleteFailure)
                            {
                                LaunchLog.Log(DEVICE_CHECKS, player.GetName(), "Device checks failed completely!");
                                gameInterface.NotifyDeviceChecksCompleteFailure(player.GetName());
                            }
                            else if(bAPIFailure)
                            {
                                LaunchLog.Log(DEVICE_CHECKS, player.GetName(), String.format("Device check API failed with code %d", lFailureCode));
                                gameInterface.NotifyDeviceChecksAPIFailure(player.GetName());
                            }
                            else
                            {
                                LaunchLog.Log(DEVICE_CHECKS, player.GetName(), "Device checks complete.");
                                
                                if(!bProfileMatch && !bBasicIntegrity)
                                {
                                    LaunchLog.Log(DEVICE_CHECKS, player.GetName(), "Both device checks checks failed!");
                                    gameInterface.NotifyDeviceCheckFailure(AuthenticatedUser);
                                }
                                
                                LaunchLog.Log(DEVICE_CHECKS, player.GetName(), bProfileMatch? "Device profile okay." : "Device profile check failed.");
                                LaunchLog.Log(DEVICE_CHECKS, player.GetName(), bProfileMatch? "Basic integrity okay." : "Basic integrity check failed.");
                            }
                            
                            AuthenticatedUser.UpdateDeviceChecks(bCompleteFailure, bAPIFailure, lFailureCode, bProfileMatch, bBasicIntegrity);
                        }
                        break;
                        
                        case Ban:
                        {
                            String strReason = LaunchUtilities.StringFromData(bb);
                            boolean bPermanent = bb.get() != 0x00;
                            Player banner = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    bPermanent ? gameInterface.PermaBan(lInstanceNumber, strReason, banner.GetName()) :
                                                 gameInterface.TempBan(lInstanceNumber, strReason, banner.GetName()),
                                    "Issued a ban.",
                                    "Could not issue a ban.");
                        }
                        break;
                            
                        case SetFirebaseToken:
                        {
                            String strToken = LaunchUtilities.StringFromData(bb);
                            Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                     gameInterface.SetToken(AuthenticatedUser.GetPlayerID(), strToken),
                                    "Changed a token.",
                                    "Could not change a token.");
                        }
                        break;
                            
                        case GiveWealth:
                        {
                            int lAmount = bb.getInt();
                            int lReceiverID = bb.getInt();
                            ResourceType type = ResourceType.values()[bb.get()];
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.GiveWealth(AuthenticatedUser.GetPlayerID(), lReceiverID, lAmount, type),
                                    "Resources given.",
                                    "Could not give resources.");
                        }
                        break;
                            
                        case ToggleAircraftReturn:
                        {
                            EntityPointer aircraft = new EntityPointer(bb);
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ToggleAircraftReturn(AuthenticatedUser.GetPlayerID(), aircraft),
                                    "Fighter auto return toggled.",
                                    "Could not toggle fighter return.");
                        }
                        break;
                            
                        case ChangeAircraftHomebase:
                        {
                            EntityPointer entity = new EntityPointer(bb);
                            EntityPointer homebase = new EntityPointer(bb);

                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ChangeAircraftHomebase(AuthenticatedUser.GetPlayerID(), entity, homebase),
                                    "Bomber homebase changed.",
                                    "Could not change bomber homebase.");
                        }
                        break;
                            
                        case PurchaseAircraft:
                        {
                            EntityType type = EntityType.values()[bb.get()];
                            EntityPointer homebase = new EntityPointer(bb);
                            boolean bUseSubstitutes = (bb.get() != 0x00);
                               
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseAircraft(AuthenticatedUser.GetPlayerID(), homebase, type, bUseSubstitutes),
                                    "Aircraft purchased.",
                                    "Could not purchase aircraft.");
                        }
                        break;
                        
                        case RefuelEntity:
                        {
                            EntityPointer entity = new EntityPointer(bb);
                               
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RefuelEntity(AuthenticatedUser.GetPlayerID(), entity),
                                    "Entity refueled.",
                                    "Could not refuel entity.");
                        }
                        break;
                        
                        case PurchaseShip:
                        {
                            EntityType type = EntityType.values()[bb.get()];
                            int lShipyardID = bb.getInt();
                            boolean bUseSubstitutes = (bb.get() != 0x00);
                               
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseShip(AuthenticatedUser.GetPlayerID(), lShipyardID, type, bUseSubstitutes),
                                    "Ship purchased.",
                                    "Could not purchase ship.");
                        }
                        break;
                        
                        case PurchaseCargoTruck:
                        {
                            int lWarehouseID = bb.getInt();
                            boolean bUseSubstitutes = (bb.get() != 0x00);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.PurchaseCargoTruck(AuthenticatedUser.GetPlayerID(), lWarehouseID, bUseSubstitutes),
                                    "Purchased cargo truck.",
                                    "Could not purchase cargo truck.");
                        }
                        break;
                            
                        case SpoofLocation:
                        {
                            LaunchClientLocation location = new LaunchClientLocation(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SpoofLocation(AuthenticatedUser.GetPlayerID(), location),
                                    "Player location spoofed.",
                                    "Could not spoof player location.");    
                        }
                        break;
                        
                        case TransferAccount:
                        {
                            int lFromID = bb.getInt();
                            int lToID = bb.getInt();
                                
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.TransferAccountAdmin(AuthenticatedUser.GetPlayerID(), lFromID, lToID),
                                    "Account tranferred.",
                                    "Could not transfer account.");
                        }
                        break; 
                        
                        case SetTaxRate:
                        {
                            float fltTaxRate = bb.getFloat();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetAllianceTaxRate(AuthenticatedUser.GetPlayerID(), fltTaxRate),
                                    "Tax rate changed.",
                                    "Could not change tax rate.");
                        }
                        break;
                        
                        case UnitCommand:
                        {
                            MoveOrders command = MoveOrders.values()[bb.get()];
                            List<EntityPointer> Commandables = LaunchUtilities.PointerListFromData(bb);
                            EntityPointer target = bb.get() != 0x00 ? new EntityPointer(bb) : null;
                            GeoCoord geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
                            LootType typeToDeliver = LootType.values()[bb.get()];
                            int lDeliverID = bb.getInt();
                            int lQuantityToDeliver = bb.getInt();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.UnitCommand(AuthenticatedUser.GetPlayerID(), true, command, Commandables, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver),
                                    "Target set.",
                                    "Could not set target.");
                        }
                        break;
                        
                        case MoveOrder:
                        {
                            List<EntityPointer> Movables = LaunchUtilities.PointerListFromData(bb);
                            Map<Integer, GeoCoord> Coordinates = LaunchUtilities.GeoCoordMapFromData(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.MoveOrder(AuthenticatedUser.GetPlayerID(), Movables, Coordinates),
                                    "Movement order given.",
                                    "Could not give move order.");
                        }
                        break;
                        
                        case MultiUnitCommand:
                        {
                            MoveOrders command = MoveOrders.values()[bb.get()];
                            List<EntityPointer> commandables = LaunchUtilities.PointerListFromData(bb);
                            EntityPointer target = bb.get() != 0x00 ? new EntityPointer(bb) : null;
                            GeoCoord geoTarget = new GeoCoord(bb.getFloat(), bb.getFloat());
                            LootType typeToDeliver = LootType.values()[bb.get()];
                            int lDeliverID = bb.getInt();
                            int lQuantityToDeliver = bb.getInt();
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.MultiUnitCommand(AuthenticatedUser.GetPlayerID(), command, commandables, target, geoTarget, typeToDeliver, lDeliverID, lQuantityToDeliver),
                                    "Targets set.",
                                    "Could not set targets.");
                        }
                        break;
                        
                        case TransferCargo:
                        {
                            EntityPointer entityFrom = bb.get() != 0x00 ? new EntityPointer(bb) : null;
                            EntityPointer entityTo = bb.get() != 0x00 ? new EntityPointer(bb) : null;
                            LootType typeToDeliver = bb.get() != 0x00 ? LootType.values()[bb.get()] : null;
                            int lDeliverID = bb.getInt();
                            int lQuantityToDeliver = bb.getInt();
                            boolean bLoadAsCargo = (bb.get() != 0x00);
                            boolean bFromCargo = (bb.get() != 0x00);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.TransferCargo(AuthenticatedUser.GetPlayerID(), entityFrom, entityTo, typeToDeliver, lDeliverID, lQuantityToDeliver, bLoadAsCargo, bFromCargo),
                                    "Cargo transferred.",
                                    "Could not transfer cargo.");
									
							/*if(result && gameInterface.GetCounterShield() != null) 
							{
                                try 
								{
                                    int pid = AuthenticatedUser.GetPlayerID();

                                    double currentNetworth = gameInterface.GetPlayerNetworth(pid);

                                    gameInterface.GetCounterShield().onResourceAction(System.currentTimeMillis(), pid, "TransferCargo", typeToDeliver != null ? typeToDeliver.name() : "UNKNOWN", lQuantityToDeliver, currentNetworth);

                                }
								catch(Exception e)
								{
                                    LaunchLog.ConsoleMessage("[CounterShield] Failed to record cargo transfer for duplication check: " + e.getMessage());
                                }
                            }*/
                        }
                        break;
                        
                        case SonarPing:
                        {
                            EntityPointer pinger = new EntityPointer(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SonarPing(AuthenticatedUser.GetPlayerID(), pinger),
                                    "Performed sonar ping.",
                                    "Could not perform sonar ping.");
                        }
                        break;
                        
                        case ToggleAirbaseOpen:
                        {
                            EntityPointer host = new EntityPointer(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ToggleAirbaseOpen(AuthenticatedUser.GetPlayerID(), host),
                                    "Toggled airbase.",
                                    "Could not toggle airbase.");
                        }
                        break;
                        
                        case RadarScan:
                        {
                            EntityPointer scanner = new EntityPointer(bb);
                            
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RadarScan(AuthenticatedUser.GetPlayerID(), scanner),
                                    "Performed radar scan.",
                                    "Could not perform radar scan.");
                        }
                        break;
                    }
                }
                else
                {
                    tobComm.SendCommand(AccountUnregistered);
                }
            }
        }
    }
    
    private void HandleSimpleResult(TobComm launchComm, int lInstanceNumber, boolean bResult, String strPositiveLog, String strNegativeLog)
    {
        //Handles a simple action success/action fail Session object outcome.
        if(bResult)
        {
            launchComm.SendCommand(ActionSuccess, lInstanceNumber);
            LaunchLog.ConsoleMessage(strPositiveLog);
        }
        else
        {
            launchComm.SendCommand(ActionFailed, lInstanceNumber);
            LaunchLog.ConsoleMessage(strNegativeLog);
        }
    }

    @Override
    public void CommandReceived(int lCommand, int lInstanceNumber)
    {
        switch(lCommand)
        {
            case KeepAlive:
            {
                tobComm.SendCommand(KeepAlive, 0);
            }
            break;
            
            case SetAvatar:
            {
                HandleSimpleResult(tobComm, lInstanceNumber,
                        gameInterface.SetAvatar(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                        "Avatar set.",
                        "Could not set avatar.");
            }
            break;
            
            default:
            {
                if(bRegistered)
                {
                    switch(lCommand)
                    {
                        case ReportAck:
                        {
                            AuthenticatedUser.AcknowledgeReport();
                            bSendingReport = false;
                        }
                        break;

                        case SnapshotAck:
                        {
                            bCanReceiveUpdates = true;
                            
                            AuthenticatedUser.AcknowledgeReports(lSnapshotReports);
                        }
                        break;
                        
                        case Obliterate:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Obliterate(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Player obliterated.",
                                    "Could not obliterate player.");
                        }
                        break;
                        
                        case Prospect:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Prospect(AuthenticatedUser.GetPlayerID()),
                                    "Prospect successful.",
                                    "Could not prospect.");
                        }
                        break;
                        
                        case DiveOrSurface:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.DiveOrSurface(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Submarine dive/surface successful.",
                                    "Could not dive/surface submarine.");
                        }
                        break;
                        
                        case MissileSlotUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.MissileSlotUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Slots upgraded.",
                                    "Could not upgrade slots.");
                        }
                        break;
                            
                        case AirbaseCapacityUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AirbaseCapacityUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Capacity upgraded.",
                                    "Could not upgrade capacity.");
                        }
                        break;

                        case InterceptorSlotUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.InterceptorSlotUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Slots upgraded.",
                                    "Could not upgrade slots.");
                        }
                        break;
                        
                        case MissileSlotUpgradeToMax:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.MissileSlotUpgradeToMax(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Slots upgraded to max.",
                                    "Could not upgrade slots to max.");
                        }
                        break;
                        
                        case InterceptorSlotUpgradeToMax:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.InterceptorSlotUpgradeToMax(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Slots upgraded to max.",
                                    "Could not upgrade slots to max.");
                        }
                        break;
                        
                        case MissileReloadUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.MissileReloadUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Reload upgraded.",
                                    "Could not upgrade reload.");
                        }
                        break;

                        case InterceptorReloadUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.InterceptorReloadUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Reload upgraded.",
                                    "Could not upgrade reload.");
                        }
                        break;
                        
                        case CallAirdrop:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CallAirdrop(AuthenticatedUser.GetPlayerID()),
                                    "Airdrop called.",
                                    "Could not call airdrop.");
                        }
                        break;
                            
                        /*case RadarRangeUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RadarRangeUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Radar range upgraded.",
                                    "Could not upgrade radar range.");
                        }
                        break;
                            
                        case RadarBoostUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RadarBoostUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Radar accuracy boost upgraded.",
                                    "Could not upgrade radar accuracy boost.");
                        }
                        break;*/
                            
                        case CommandPostHPUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CommandPostHPUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "CommandPost HP upgraded.",
                                    "Could not upgrade commandPost hp.");
                        }
                        break;
                            
                        case SentryRangeUpgrade:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SentryRangeUpgrade(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Sentry gun range upgraded.",
                                    "Could not upgrade sentry gun range.");
                        }
                        break;
                        
                        case Heal:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.HealPlayer(AuthenticatedUser.GetPlayerID()),
                                    "Player healed.",
                                    "Could not heal player.");
                        }
                        break;
                        
                        case CloseAccount:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CloseAccount(AuthenticatedUser.GetPlayerID()),
                                    "Account closed.",
                                    "Could not close account.");
                        }
                        break;
                        
                        case UpgradeMissileSiteNuclear:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.UpgradeToNuclear(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Missile site upgraded to nuclear.",
                                    "Could not upgrade missile site to nuclear.");
                        }
                        break;
                        
                        case UpgradeShipyard:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.UpgradeShipyard(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Shipyard upgraded.",
                                    "Could not upgrade shipyard.");
                        }
                        break;
                        
                        case JoinAlliance:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.JoinAlliance(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Join request acknowledged",
                                    "Could not acknowledge join request.");
                        }
                        break;
                        
                        case LeaveAlliance:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.LeaveAlliance(AuthenticatedUser.GetPlayerID()),
                                    "Left alliance.",
                                    "Could not leave alliance.");
                        }
                        break;
                        
                        case DeclareWar:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.DeclareWar(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Declared war.",
                                    "Could not declare war.");
                        }
                        break;
                            
                        case SurrenderWar:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ProposeSurrender(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Proposed surrender.",
                                    "Could not propose surrender.");
                        }
                        break;
                            
                        case RejectSurrender:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RejectSurrender(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Rejected surrender.",
                                    "Could not reject surrender.");
                        }
                        break;
                            
                        case AcceptSurrender:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AcceptSurrender(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Accepted surrender.",
                                    "Could not accept surrender.");
                        }
                        break;
                        
                        case BreakAffiliation:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.BreakAffiliation(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Broke affiliation.",
                                    "Could not break affiliation.");
                        }
                        break;
                            
                        case ProposeAffiliation:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.ProposeAffiliation(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Proposed affiliation.",
                                    "Could not propose affiliation.");
                        }
                        break;
                        
                        case AcceptAffiliation:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AcceptAffiliation(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Accepted affiliation.",
                                    "Could not accept affiliation.");
                        }
                        break;
                        
                        case RejectAffiliation:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RejectAffiliation(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Rejected affiliation.",
                                    "Could not reject affiliation.");
                        }
                        break;
                        
                        case CancelAffiliation:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.CancelAffiliation(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Cancelled affiliation.",
                                    "Could not cancel affiliation.");
                        }
                        break;
                        
                        case Promote:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Promote(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Promoted player.",
                                    "Could not promote player.");
                        }
                        break;
                        
                        case AcceptJoin:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AcceptJoin(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Accepted join.",
                                    "Could not accept join.");
                        }
                        break;
                        
                        case RejectJoin:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.RejectJoin(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Rejected join.",
                                    "Could not reject join.");
                        }
                        break;
            
                        case SetAllianceAvatar:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.SetAllianceAvatar(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Alliance avatar set.",
                                    "Could not set alliance avatar.");
                        }
                        break;
                        
                        case Kick:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Kick(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Kicked.",
                                    "Could not kick.");
                        }
                        break;
                        
                        case Blacklist:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Blacklist(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Blacklisted player.",
                                    "Could not backlist player.");
                        }
                        break;
                        
                        case ResetAvatar:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AvatarReset(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "ResetAvatar command succeeded.",
                                    "ResetAvatar command did not succeed.");
                        }
                        break;
                        
                        case ResetName:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.NameReset(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "ResetName command succeeded.",
                                    "ResetName command did not succeed.");
                        }
                        break;
                            
                        case Unban:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.Unban(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Unbanned.",
                                    "Could not unban.");
                        }
                        break;
                        
                        case AllianceWithdraw:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AllianceWithdraw(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Money withdrawn.",
                                    "Could not withdraw money.");
                        }
                        break;
                        
                        case AlliancePanic:
                        {
                            HandleSimpleResult(tobComm, lInstanceNumber,
                                    gameInterface.AlliancePanic(AuthenticatedUser.GetPlayerID(), lInstanceNumber),
                                    "Panic issued.",
                                    "Could not issue panic.");
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void ObjectRequested(int lObject, int lInstanceNumber, int lOffset, int lLength)
    {
        switch(lObject)
        {
            case GameSnapshot:
            {
                //If the session is authenticated, send a complete game snapshot.
                if(bRegistered)
                {
                    LaunchGame game = gameInterface.GetGame();
                    
                    //Send snapshot begin command.
                    tobComm.SendCommand(SnapshotBegin, 0);
                    
                    //Start burst mode, so all of this goes in a minimal number of TCP packets.
                    StartBurst();
                    
                    //Send the player first, to fix some null pointer crashes.
                    int lTheirPlayerID = AuthenticatedUser.GetPlayerID();
                    Player player = game.GetPlayer(lTheirPlayerID);
                    tobComm.SendObject(Player, lTheirPlayerID, 0, player.GetData(lTheirPlayerID));
                    
                    LaunchEntity lastHandled = null;
                    
                    try
                    {
                        //Send all alliances.
                        for(Alliance alliance : game.GetAlliances())
                        {
                            tobComm.SendObject(AllianceMinor, alliance.GetData());
                        }

                        //Send all wars.
                        for(Treaty treaty : game.GetTreaties())
                        {
                            tobComm.SendObject(Treaty, treaty.GetData());
                        }

                        //Send all players.
                        for(Player entity : game.GetPlayers())
                        {
                            //We just sent their player; No need to send it again.
                            if(!entity.ApparentlyEquals(player))
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Player, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all missiles.
                        for(Missile entity : game.GetMissiles())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Missile, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all interceptors.
                        for(Interceptor entity : game.GetInterceptors())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Interceptor, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all torpedoes.
                        for(Torpedo entity : game.GetTorpedoes())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Torpedo, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all missile sites.
                        for(MissileSite entity : game.GetMissileSites())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(MissileSite, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all SAM sites.
                        for(SAMSite entity : game.GetSAMSites())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(SamSite, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all sentry guns.
                        for(SentryGun entity : game.GetSentryGuns())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(SentryGun, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all sentry guns.
                        for(ScrapYard entity : game.GetScrapYards())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(ScrapYard, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all artillery guns.
                        for(ArtilleryGun entity : game.GetArtilleryGuns())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(ArtilleryGun, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all ore mines.
                        for(OreMine entity : game.GetOreMines())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(OreMine, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all ore mines.
                        for(Processor entity : game.GetProcessors())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Processor, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all ore mines.
                        for(MissileFactory entity : game.GetMissileFactorys())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(MissileFactory, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all radar stations.
                        for(RadarStation entity : game.GetRadarStations())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(RadarStation, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all bunkers.
                        for(CommandPost entity : game.GetCommandPosts())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(CommandPost, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all airbases.
                        for(Airbase entity : game.GetAirbases())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Airbase, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all ships.
                        for(Ship entity : game.GetShips())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Ship, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all submarines.
                        for(Submarine entity : game.GetSubmarines())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Submarine, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all barracks.
                        for(Armory entity : game.GetArmories())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Armory, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all banks.
                        for(Bank entity : game.GetBanks())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Bank, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all warehouses.
                        for(Warehouse entity : game.GetWarehouses())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Warehouse, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all planes. Note that stored aircraft are not sent separately in the snapshot as they are sent inside airbases themselves.
                        for(Airplane entity : game.GetAirplanes())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Airplane, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all the player's infantries. Note that stored infantry will not be sent separately, as they will be sent inside whatever object stores them.
                        for(Infantry entity : game.GetInfantries())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Infantry, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all the player's tanks.
                        for(Tank entity : game.GetTanks())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Tank, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all the player's trucks.
                        for(CargoTruck entity : game.GetCargoTrucks())
                        {
                            if(entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()) || game.GetAllegiance(entity, player) == Allegiance.ALLY || entity.GetVisible())
                            {
                                lastHandled = entity;
                                tobComm.SendObject(CargoTruck, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all airdrops.
                        for(Airdrop entity : game.GetAirdrops())
                        {
                            if(entity.GetOwnerID() == lTheirPlayerID)
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Airdrop, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all rubbles.
                        for(Rubble entity : game.GetRubbles())
                        {
                            if(entity.GetOwnerID() == lTheirPlayerID)
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Rubble, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all blueprints.
                        for(Blueprint entity : game.GetBlueprints())
                        {
                            if(entity.GetOwnerID() == lTheirPlayerID)
                            {
                                lastHandled = entity;
                                tobComm.SendObject(Blueprint, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                            }
                        }

                        //Send all shipyards.
                        for(Shipyard entity : game.GetShipyards())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Shipyard, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all radiations.
                        for(Radiation entity : game.GetRadiations())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Radiation, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        //Send all resource deposits.
                        for(ResourceDeposit entity : game.GetResourceDeposits())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(ResourceDeposit, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }

                        for(LaunchReport report : AuthenticatedUser.GetReports())
                        {
                            tobComm.SendObject(Report, 0, 0, report.GetData());
                            lSnapshotReports++;
                        }

                        for(Loot entity : game.GetLoots())
                        {
                            lastHandled = entity;
                            tobComm.SendObject(Loot, entity.GetID(), 0, entity.GetData(lTheirPlayerID));
                        }
                    }
                    catch(Exception ex)
                    {
                        if(lastHandled != null)
                        {
                            LaunchLog.ConsoleMessage(String.format("Session error while trying to send %s %d.", lastHandled.GetTypeName(), lastHandled.GetID()));
                        }
                    }
                    
                    tobComm.SendCommand(SnapshotComplete, 0);
                    
                    //End burst mode, to go back to one TCP packet per thing.
                    EndBurst();
                }
                else
                {
                    tobComm.SendCommand(AccountUnregistered);
                }
            }
            break;
            
            case Avatar:
            {
                //Avatar requested.
                RandomAccessFile rafImage;
                
                try
                {
                    rafImage = new RandomAccessFile(String.format(Defs.IMAGE_FILE_FORMAT, Defs.LOCATION_AVATARS, lInstanceNumber), "r");
                    byte[] cResult = new byte[(int)rafImage.length()];
                    rafImage.readFully(cResult);

                    tobComm.SendObject(Avatar, lInstanceNumber, cResult);
                }
                catch(Exception ex)
                {
                    //The avatar is bad, flag this to the game so that all players using it can be reset.
                    gameInterface.BadAvatar(lInstanceNumber);
                    tobComm.SendCommand(ImageError, lInstanceNumber);
                }
            }
            break;
            
            case Config:
            {
                //Game config requested.
                LaunchGame game = gameInterface.GetGame();
                tobComm.SendObject(LaunchSession.Config, 0, lOffset, game.GetConfig().GetData());
            }
            break;
            
            case ImgAsset:
            {
                //Image requested.
                RandomAccessFile rafImage;
                
                try
                {
                    rafImage = new RandomAccessFile(String.format(Defs.IMAGE_FILE_FORMAT, Defs.LOCATION_IMGASSETS, lInstanceNumber), "r");
                    byte[] cResult = new byte[(int)rafImage.length()];
                    rafImage.readFully(cResult);

                    tobComm.SendObject(ImgAsset, lInstanceNumber, cResult);
                }
                catch(Exception ex)
                {
                    //The avatar is bad, flag this to the game so that all players using it can be reset.
                    gameInterface.BadImage(lInstanceNumber);
                    tobComm.SendCommand(ImageError, lInstanceNumber);
                }
            }
            break; 
            
            case Treaty:
            {
                Treaty treaty = gameInterface.GetGame().GetTreaty(lInstanceNumber);
                
                if(treaty != null)
                {
                    tobComm.SendObject(Treaty, treaty.GetData());
                }
            }
            break;
            
            case FullPlayerStats:
            {
                Player fullPlayer = gameInterface.GetGame().GetPlayer(lInstanceNumber);
                
                if(fullPlayer != null && AuthenticatedUser != null)
                {
                    tobComm.SendObject(FullPlayerStats, fullPlayer.GetFullStatsData(AuthenticatedUser.GetPlayerID()));
                }
            }
            break;
            
            case UserData:
            {
                boolean bSuccess = false;
                
                if(AuthenticatedUser != null)
                {
                    Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                    
                    if(player != null)
                    {
                        if(player.GetIsAnAdmin())
                        {
                            User user = gameInterface.GetUser(lInstanceNumber);
                            
                            tobComm.SendObject(lObject, lInstanceNumber, user.GetData());
                        }
                    }
                }
                
                if(!bSuccess)
                {
                    tobComm.SendCommand(ActionFailed);
                }
            }
            break;
        }
    }
    
    public void SendEntity(LaunchEntity entity)
    {
        if(bRegistered && entity != null)
        {
            if(entity instanceof MapEntity mapEntity)
            {
                if(entity instanceof Blueprint || entity instanceof Rubble || entity instanceof Airdrop)
                {
                    if(!entity.GetOwnedBy(AuthenticatedUser.GetPlayerID()))
                        return;
                }
                
                if(mapEntity instanceof LandUnit || mapEntity instanceof Ship || mapEntity instanceof AirplaneInterface)
                {
                    if(!mapEntity.GetVisible())
                    {
                        Player player = gameInterface.GetGame().GetPlayer(AuthenticatedUser.GetPlayerID());
                        
                        if(player != null)
                        {
                            if(gameInterface.GetGame().GetAllegiance(player, mapEntity) != Allegiance.ALLY && gameInterface.GetGame().GetAllegiance(player, mapEntity) != Allegiance.YOU)
                            {
                                return;
                            }
                        } 
                    }
                }
            }
            
            tobComm.SendObject(entity.GetSessionCode(), entity.GetID(), 0, entity.GetData(AuthenticatedUser.GetPlayerID()));
        }   
    }
    
    public void RemoveEntity(LaunchEntity entity)
    {
        if(bRegistered)
        {
            if(entity instanceof Player)
                tobComm.SendCommand(RemovePlayer, entity.GetID());
            else if(entity instanceof Missile)
                tobComm.SendCommand(RemoveMissile, entity.GetID());
            else if(entity instanceof Interceptor)
                tobComm.SendCommand(RemoveInterceptor, entity.GetID());
            else if(entity instanceof MissileSite)
                tobComm.SendCommand(RemoveMissileSite, entity.GetID());
            else if(entity instanceof SAMSite)
                tobComm.SendCommand(RemoveSAMSite, entity.GetID());
            else if(entity instanceof OreMine)
                tobComm.SendCommand(RemoveOreMine, entity.GetID());
            else if(entity instanceof MissileFactory)
                tobComm.SendCommand(RemoveMissileFactory, entity.GetID());
            else if(entity instanceof RadarStation)
                tobComm.SendCommand(RemoveRadarStation, entity.GetID());
            else if(entity instanceof CommandPost)
                tobComm.SendCommand(RemoveCommandPost, entity.GetID());
            else if(entity instanceof SentryGun)
                tobComm.SendCommand(RemoveSentryGun, entity.GetID());
            else if(entity instanceof ScrapYard)
                tobComm.SendCommand(RemoveScrapYard, entity.GetID());
            else if(entity instanceof ArtilleryGun)
                tobComm.SendCommand(RemoveArtilleryGun, entity.GetID());
            else if(entity instanceof Loot)
                tobComm.SendCommand(RemoveLoot, entity.GetID());
            else if(entity instanceof ResourceDeposit)
                tobComm.SendCommand(RemoveResourceDeposit, entity.GetID());
            else if(entity instanceof Radiation)
                tobComm.SendCommand(RemoveRadiation, entity.GetID());
            else if(entity instanceof Airbase)
                tobComm.SendCommand(RemoveAirbase, entity.GetID());
            else if(entity instanceof Armory)
                tobComm.SendCommand(RemoveArmory, entity.GetID());
            else if(entity instanceof Airplane)
                tobComm.SendCommand(RemoveAircraft, entity.GetID());
            else if(entity instanceof Bank)
                tobComm.SendCommand(RemoveBank, entity.GetID());
            else if(entity instanceof Warehouse)
                tobComm.SendCommand(RemoveWarehouse, entity.GetID());
            else if(entity instanceof Processor)
                tobComm.SendCommand(RemoveProcessor, entity.GetID());
            else if(entity instanceof Distributor)
                tobComm.SendCommand(RemoveDistributor, entity.GetID());
            else if(entity instanceof StoredAirplane)
                tobComm.SendCommand(RemoveStoredAircraft, entity.GetID());
            else if(entity instanceof InfantryInterface)
                tobComm.SendCommand(RemoveInfantry, entity.GetID());
            else if(entity instanceof Tank)
                tobComm.SendCommand(RemoveTank, entity.GetID());
            else if(entity instanceof CargoTruck)
                tobComm.SendCommand(RemoveCargoTruck, entity.GetID());
            else if(entity instanceof Blueprint)
                tobComm.SendCommand(RemoveBlueprint, entity.GetID());
        }
    }
    
    public void SendAlliance(Alliance alliance, boolean bMajor)
    {
        tobComm.SendObject(bMajor ? AllianceMajor : AllianceMinor, alliance.GetID(), alliance.GetData());
    }
    
    public void SendContactBearing(EntityPointer vessel, float fltBearing)
    {
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.put(vessel.GetData());
        bb.putFloat(fltBearing);
        
        tobComm.SendObject(ContactBearing, bb.array());
    }
    
    public void SendTreaty(Treaty treaty)
    {
        tobComm.SendObject(Treaty, treaty.GetID(), treaty.GetData());
    }
    
    public void RemoveAlliance(Alliance alliance)
    {
        tobComm.SendCommand(RemoveAlliance, alliance.GetID());
    }
    
    public void RemoveTreaty(Treaty treaty)
    {
        tobComm.SendCommand(RemoveTreaty, treaty.GetID());
    }
    
    public void SendEvent(LaunchEvent event, boolean bSpecific, int lPlayerID)
    {
        int lTheirPlayerID = AuthenticatedUser.GetPlayerID();
        
        if(!bSpecific || bSpecific && lTheirPlayerID == lPlayerID)
            tobComm.SendObject(Event, event.GetData());
    }
    
    public void SendOKDialog(String strMessage, boolean bSpecific, int lPlayerID)
    {
        int lTheirPlayerID = AuthenticatedUser.GetPlayerID();
        
        ByteBuffer bb = ByteBuffer.allocate(LaunchUtilities.GetStringDataSize(strMessage));
        bb.put(LaunchUtilities.GetStringData(strMessage));
        
        if(!bSpecific || bSpecific && lTheirPlayerID == lPlayerID)
            tobComm.SendObject(BasicOKDialog, bb.array());
    }
    
    public void UpdateToken()
    {
        tobComm.SendCommand(UpdateToken);
    }
    
    public User GetAuthenticatedUser()
    {
        return AuthenticatedUser;
    }
    
    public boolean CanReceiveUpdates()
    {
        return bCanReceiveUpdates;
    }
    
    public int GetID() { return lID; }

    @Override
    protected int GetTimeout()
    {
        return SERVER_TIMEOUT;
    }

    @Override
    public void SyncObjectsProcessed()
    {
        throw new UnsupportedOperationException("The server shouldn't use this feature.");
    }
}
