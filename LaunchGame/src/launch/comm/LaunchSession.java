/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.comm;

import java.net.Socket;
import tobcomm.TobComm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import tobcomm.protocol.ConnectionProvider;
import launch.utilities.LaunchLog;
import launch.utilities.ShortDelay;
import tobcomm.TobCommInterface;
import tobcomm.protocol.ConnectionProvider.ConnectionLogger;
import tobcomm.protocol.TCPProvider;

/**
 *
 * @author tobster
 */
public abstract class LaunchSession implements TobCommInterface, ConnectionLogger
{
    //Objects.
    public static final int Authorise = 0;                       //Request to authorise, using encrypted device ID.
    public static final int UserData = 1;                        //User data. Admin's eyes only.
    public static final int PermBanData = 2;                     //Player is permanently banned, with reason.
    public static final int BanData = 3;                         //Player is banned, with duration and reason.
    public static final int Registration = 4;                    //Account registration request details.
    public static final int GameSnapshot = 5;                    //A new snapshot of the entire game, for every new comms session.
    public static final int LocationUpdate = 6;                  //Regular location data from players.
    public static final int Player = 7;                          //A player.
    public static final int Missile = 8;                         //A missile.
    public static final int Interceptor = 9;                     //An interceptor missile.
    public static final int MissileSite = 10;                    //A missile launch site.
    public static final int SamSite = 11;                        //A SAM site.
    public static final int OreMine = 12;                        //An ore mine.
    public static final int SentryGun = 13;                      //A sentry gun.
    public static final int Loot = 14;                           //A loot cache.
    public static final int Radiation = 15;                      //A radioactive area.
    public static final int AllianceMinor = 16;                  //An alliance minor change (i.e. points change).
    public static final int AllianceMajor = 17;                  //An alliance major change that should trigger a UI refresh (i.e. players joining/leaving, etc).
    public static final int Treaty = 18;                         //A treaty.
    public static final int Avatar = 19;                         //An avatar.
    public static final int Config = 20;                         //The game configuration.
    public static final int Event = 21;                          //An event message.
    public static final int Report = 22;                         //A report message.
    public static final int BuildStructure = 23;                 //A request to build a structure.
    public static final int SellLaunchable = 24;                 //A request to sell a missile.
    public static final int SellInterceptor = 25;                //A request to sell an interceptor.
    public static final int Ban = 26;                            //Ban a player (as an administrator).
    public static final int FullPlayerStats = 27;                //A player's stats.
    public static final int LaunchMissile = 28;                  //A request to launch a missile.
    public static final int LaunchPlayerMissile = 29;            //A request to launch a missile.
    public static final int LaunchInterceptor = 30;              //A request to launch an interceptor.
    public static final int LaunchPlayerInterceptor = 31;        //A request to launch an interceptor.
    public static final int AlertStatus = 32;                    //A request for alert status.
    public static final int EntityNameChange = 33;               //An instruction to change a structure name.
    public static final int RepairEntity = 34;                //A request to remotely repair the missile site with instance number.
    public static final int SAMSiteModeChange = 35;              //An instruction to change a SAM site mode.
    public static final int SellEntity = 36;                  //A request to sell a missile site.
    public static final int PurchaseAircraft = 37;               //A request to purchase an aircraft.
    public static final int CreateAlliance = 38;                 //Alliance creation details.
    public static final int PurchaseLaunchables = 39;            //A request to purchase missiles.
    public static final int PurchaseInterceptors = 40;           //A request to purchase interceptors.
    public static final int ProcessNames = 41;                   //A list of process names, when location spoofing has been suspected.
    public static final int DeviceCheck = 42;                    //Device check information.
    public static final int RenamePlayer = 43;                   //A request for a player to change their name.
    public static final int RenameAlliance = 44;                  //A request to change the name of an alliance.
    public static final int RedescribeAlliance = 45;             //A request to change an alliance description.
    public static final int StructuresOnOff = 46;                //An instruction to bring multiple missile sites online or take them offline.
    public static final int ImgAsset = 47;                       //An image.
    public static final int RadarStation = 48;                   //A radar station.
    public static final int EngagementSpeedChange = 49;       //An instruction to change SAM engagement distance.
    public static final int AddBounty = 50;                      //A request to increase bounty.
    public static final int GiveWealth = 51;                     //A request to transfer wealth.
    public static final int SetFirebaseToken = 52;               //A request to update user firebase token.
    public static final int SolarPanel = 53;                     //A solar panel.
    public static final int CommandPost = 54;                         //A commandPost.
    public static final int ICBMSiloModeChange = 55;             //An instruction to change an ICBM silo mode.
    public static final int Farm = 56;                           //A farm.
    public static final int Airbase = 57;                        //An airbase.
    public static final int MoveOrder = 58;                      //An instruction to move an entity.
    public static final int Airplane = 59;                       //An airplane.
    public static final int ChangeAircraftHomebase = 61;         //An instruction to change aircraft home base.
    public static final int SpoofLocation = 62;                  //An instruction to spoof a location, you cheating bastard.
    public static final int StoredAirplane = 63;                 //A stored airplane.
    public static final int ToggleAircraftReturn = 64;           //A request to toggle fighter auto-return.
    public static final int BankAction = 65;                     //Either withdraw from or deposit into a bank.
    public static final int Bank = 66;                           //A bank.
    public static final int LinkDepot = 67;                      //An instruction to link two depots together.
    public static final int Warehouse = 68;                      //A warehouse.
    public static final int LaunchICBM = 69;                     //A request to launch an ICBM.
    public static final int MissileFactory = 71;                     //A comms factory.
    public static final int SendMessage = 72;                    //A message.
    public static final int Blueprint = 73;                      //A structure blueprint. 
    public static final int PlaceBlueprint = 74;                 //A request to place a blueprint. 
    public static final int SetTaxRate = 75;                     //A request to set an alliance tax rate.
    public static final int Airdrop = 76;                        //An airdrop.
    public static final int AircraftType = 77;                   //An aircraft type/design.
    public static final int Barracks = 78;                       //A barracks structure.
    public static final int SaveAircraftType = 79;               //A new type the player wishes to save on the server.
    public static final int MissileType = 80;                    //A custom designed missile type.
    public static final int InterceptorType = 81;                //A custom designed interceptor type.
    public static final int FullStats = 82;                      //A request to get full stats data on an entity.
    public static final int ElectronicWarfare = 83;              //A request to activate electronic warfare.
    public static final int Infantry = 84;                       //An infantry unit.
    public static final int SAMTank = 85;                        //A SAM tank.
    public static final int Tank = 86;                           //A tank.
    public static final int StoredInfantry = 87;                 //A stored infantry object.
    public static final int Armory = 88;                         //A armory structure.
    public static final int UnitCommand = 89;                    //A command for a commandable unit.
    public static final int LocationEntities = 90;               //A request for all the entities within a rectangle.
    public static final int BasicOKDialog = 91;                  //A message to display via a basic OK dialog on the client.
    public static final int TerrainData = 92;                    //A data package for the client detailing the terrain type at a location.
    public static final int TerrainDataRequest = 93;             //A request from the client for a batch of terrain information about a location.
    public static final int Resource = 94;                       //A resource object.
    public static final int StoredLaunchable = 95;               //A stored launchable object.
    public static final int StoredTank = 96;                     //A stored tank object.
    public static final int City = 97;                           //A city object.
    public static final int ResourceDeposit = 98;                //A resource deposit object.
    public static final int CargoTruck = 99;                     //A cargo truck object.
    public static final int StoredCargoTruck = 100;              //A stored cargo truck object.
    public static final int TransferCargo = 101;                 //A request to transfer cargo.
    public static final int Processor = 102;                     //A processor object.
    public static final int Shipyard = 103;                      //A shipyard object.
    public static final int ShipType = 104;                      //A shiptype.
    public static final int Ship = 105;                          //A ship object.
    public static final int SubmarineType = 106;                 //A submarine type.
    public static final int Submarine = 107;                     //A submarine object.
    public static final int SaveShipType = 108;                  //A new type of ship the player wishes to save on the server.
    public static final int SaveSubmarineType = 109;             //A new type of submarine the player wishes to save.
    public static final int PurchaseInfantry = 110;              //A request to purchase a infantry unit.
    public static final int KOTH = 111;                          //The king of the hill object.
    public static final int PurchaseShip = 112;                  //A request to purchase an existing ship type.
    public static final int PurchaseSubmarine = 113;             //A request to purchase an existing submarine type.
    public static final int NavalVesselSpeedChange = 114;        //A request to change ship/submarine speed.
    public static final int Torpedo = 115;                       //A torpedo object.
    public static final int SonarPing = 116;                     //A sonar ping.
    public static final int RadarScan = 117;                     //A radar scan.
    public static final int ContactBearing = 118;                //A new passive sonar contact.
    public static final int PurchaseTorpedoes = 119;             //A purchase instruction for torpedoes.
    public static final int LaunchTorpedo = 120;                 //A request to launch a torpedo.
    public static final int ArtilleryGun = 121;                  //An artillery gun object.
    public static final int SetArtilleryTarget = 122;            //A request to set an artillery gun target.
    public static final int ScrapYard = 123;                     //A scrap yard.
    public static final int RefuelAircraft = 124;                //A request to refuel an aircraft.
    public static final int LoadLandUnit = 125;                  //A request to load a land unit into a transport.
    public static final int TransferLandUnit = 126;              //A request to transfer a stored land unit between two transports.
    public static final int DropLandUnit = 127;                  //A request to drop a land unit out of a transport.
    public static final int CaptureEntity = 128;                 //A request from a player to capture a structure directly.
    public static final int Distributor = 129;                   //A distributor object.
    public static final int TransferAccount = 130;               //A request from an admin to transfer a player account.
    public static final int SubscriptionUpdate = 131;            //A request to update a player's subsciption status.
    public static final int VerifyPurchase = 132;                //A request to verify an in-app purchase.
    public static final int PurchaseCargoTruck = 133;            //A request to build a cargo truck.
    public static final int RefuelEntity = 134;                  //A request to refuel an entity.
    public static final int MultiUnitCommand = 135;              //A unit command for many units.
    public static final int Rubble = 136;                        //A rubble object.
    public static final int CeaseFire = 137;                     //A request to stop a unit or structure from auto-firing.
    public static final int Helicopter = 138;                    //A helicopter object.
    public static final int StoredHelicopter = 139;              //A stored helicopter object.
    public static final int KickAircraft = 140;                  //A request to kick an aircraft out of an airbase.
    public static final int PurchaseHelicopter = 141;            //A request to buy a helicopter.
    public static final int AdminDelete = 142;                   //A request from an amin to delete an entity.
    public static final int MarketListing = 143;                 //A market listing object.
    public static final int Market = 144;                        //A market object.
    public static final int ListOnMarket = 145;                  //A request to list an item for sale on the market.
    public static final int MigrateAccount = 146;                //A request to migrate an existing player account to a Google account.
    public static final int PurchaseListing = 147;               //A request to purchase a market listing.
    public static final int PurchaseTank = 148;                  //A request to purchase a tank.    
    //149 is unused. 
    public static final int ToggleAirbaseOpen = 150;             //Toggle an aircraft system open or closed.
    //Commands.
    public static final int AccountUnregistered = 0;             //The account must be registered (present user with form).
    public static final int MajorVersionInvalid = 1;             //Notify the client that a major update is available.
    public static final int NameTaken = 2;                       //The player or alliance name already exists.
    public static final int AccountCreateSuccess = 3;            //The account was created successfully.
    public static final int Obliterate = 4;                      //A command to delete a player from the game.
    public static final int DeviceIDUsed = 5;                    //A device ID has already been used. The player needs to log in with their google account.
    public static final int SnapshotBegin = 6;                   //Indicates the start of a requested game snapshot.
    public static final int SnapshotComplete = 7;                //Indicates the end of a requested game snapshot.
    public static final int SnapshotAck = 8;                     //Acknowledges receipt of the end of the snapshot.
    public static final int ImageError = 9;                      //Error reading image data.
    public static final int ActionSuccess = 10;                  //The last action was completed.
    public static final int ActionFailed = 11;                   //The last action failed for an unspecified reason.
    public static final int PurchaseMissileSystem = 12;          //A request to purchase a missile system for a player.
    public static final int PurchaseSAMSystem = 13;              //A request to purchase an air defence system for a player.
    public static final int ReportAck = 14;                      //A client acking a report so it may be deleted.
    public static final int KeepAlive = 15;                      //A keepalive for when location information isn't available.
    public static final int RemovePlayer = 16;                   //A player has left the game and must be removed.
    public static final int RemoveMissile = 17;                  //A missile has been removed from the game.
    public static final int RemoveInterceptor = 18;              //An interceptor has been removed from the game.
    public static final int RemoveMissileSite = 19;              //A missile site has been removed from the game.
    public static final int RemoveSAMSite = 20;                  //A SAM site has been removed from the game.
    public static final int RemoveOreMine = 21;                  //An ore mine has been removed from the game.
    public static final int RemoveSentryGun = 22;                //A sentry gun has been removed from the game.
    public static final int RemoveLoot = 23;                     //A loot has been removed from the game.
    public static final int RemoveRadiation = 24;                //A radioactive area has been removed from the game.
    public static final int RemoveAlliance = 25;                 //An alliance has been removed from the game.
    public static final int RemoveTreaty = 26;                   //A treaty has been removed from the game.
    public static final int Respawn = 27;                        //A request to respawn.
    public static final int PlayerMissileSlotUpgrade = 28;       //A request to upgrade missile slots on player's CMS system.
    public static final int PlayerInterceptorSlotUpgrade = 29;   //A request to upgrade interceptor slots on player's SAM system.
    public static final int MissileSlotUpgrade = 30;             //A request to upgrade missile slots on a missile site (instance no).
    public static final int InterceptorSlotUpgrade = 31;         //A request to upgrade interceptor slots on a SAM site (instance no).
    public static final int PlayerMissileReloadUpgrade = 32;     //A request to upgrade reloading on player's CMS system.
    public static final int PlayerInterceptorReloadUpgrade = 33; //A request to upgrade reloading on player's SAM system.
    public static final int MissileReloadUpgrade = 34;           //A request to upgrade reloading on a missile site (instance no).
    public static final int InterceptorReloadUpgrade = 35;       //A request to upgrade reloading on a SAM site (instance no).
    public static final int SellMissileSystem = 36;              //A request to sell a missile system.
    public static final int SellSAMSystem = 37;                  //A request to sell a SAM system.
    public static final int Heal = 38;                           //A request to fully heal the player.
    public static final int SetAvatar = 39;                      //A request to set an avatar ID.
    public static final int CloseAccount = 40;                   //A request to close the player's account.
    public static final int AlertAllClear = 41;                  //Alert indication that a player is not under attack.
    public static final int AlertUnderAttack = 42;               //Alert indication that a player is under attack.
    public static final int AlertNukeEscalation = 43;            //Alert indication that a player's ally is under attack.
    public static final int AlertAllyUnderAttack = 44;           //Alert indication that a player's ally is under attack.
    public static final int UpgradeMissileSiteNuclear = 45;      //A request to upgrade a missile site to nuclear capabilities.
    public static final int JoinAlliance = 46;                   //A request to join the specified alliance.
    public static final int LeaveAlliance = 47;                  //A request to leave any alliance the player is a member of.
    public static final int DeclareWar = 48;                     //A request for the player's alliance to declare war on the specified alliance.
    public static final int SetAllianceAvatar = 49;              //A request to set an alliance avatar ID.
    public static final int Promote = 50;                        //A request to promote an alliance member to a leader.
    public static final int AcceptJoin = 51;                     //Accept a player into the alliance you lead.
    public static final int RejectJoin = 52;                     //Reject a player's request to join the alliance you lead.
    public static final int Kick = 53;                           //Kick a player from the alliance you lead.
    public static final int ResetName = 54;                      //Reset a player's name (as an administrator).
    public static final int ResetAvatar = 55;                    //Reset a player's avatar (as an administrator).
    public static final int ProposeAffiliation = 56;             //An offer of a peace treaty to another alliance.
    public static final int AcceptAffiliation = 57;              //An acceptance of a peace treaty from another alliance.
    public static final int RejectAffiliation = 58;              //An acceptance of a peace treaty from another alliance.
    public static final int DisplayGeneralError = 59;            //A command to display a generic error on the client, for limited accounts suspected of cheating.
    public static final int PlayerNameTooLong = 60;              // Player name exceeds maximum length
    public static final int PlayerNameTooShort = 61;             // Player name string is 0 length
    public static final int AlreadyRegistered = 62;              // Player's hash already registered
    public static final int RemoveRadarStation = 63;             //A radar station has been removed from the game. 
    public static final int RadarRangeUpgrade = 64;              //A request to upgrade radar range on a radar station.
    public static final int RadarBoostUpgrade = 65;              //A request to upgrade radar accuracy bonus.
    public static final int SentryRangeUpgrade = 66;             //A request to upgrade sentry gun range.
    public static final int UpdateToken = 67;                    //A request to update a firebase token.
    public static final int Unban = 68;                          //A request to unban a player.
    public static final int RemoveSolarPanel = 69;               //A solar panel has been removed from the game.  
    public static final int RemoveCommandPost = 70;                   //A commandPost has been removed from the game.
    public static final int CommandPostHPUpgrade = 71;                //A request to upgrade commandPost HP.
    public static final int SurrenderWar = 72;                   //A request to surrender a war. 
    public static final int BreakAffiliation = 73;               //A request to break affiliation
    public static final int RemoveFarm = 74;                     //A farm has been removed from the game.
    public static final int RemoveAirbase = 75;                  //An airbase has been removed from the game.
    public static final int SellAircraft = 76;                   //A request to sell a Aircraft.
    public static final int AirbaseCapacityUpgrade = 77;         //A request to upgrade airbase capacity.
    public static final int AircraftReturn = 79;                 //A request to return a Aircraft to base.
    public static final int RemoveAircraft = 80;                 //A aircraft has been removed from the game.
    public static final int AcceptSurrender = 81;                //A request to accept a surrender. 
    public static final int RejectSurrender = 82;                //A request to reject a surrender.
    public static final int RemoveStoredAircraft = 83;           //A stored aircraft has been removed fro the game.
    public static final int BankCapacityUpgrade = 84;            //An instruction to upgrade bank capacity.
    public static final int RemoveBank = 85;                     //A bank has been removed from the game.
    public static final int RemoveWarehouse = 86;                //A logistics warehouse has been removed from the game.
    public static final int UnlinkDepot = 87;                    //An instruction to unlink a logistics warehouse.
    public static final int RemoveMissileFactory = 88;           //A comms factory has been removed from the game.
    public static final int RemoveBlueprint = 89;                //A blueprint has been removed from the game.
    public static final int AllianceWithdraw = 90;               //A request to withdraw money from an alliance coffer.
    public static final int AlliancePanic = 91;                  //A request to issue an alliance panic alert.
    public static final int CallAirdrop = 92;                    //A request to call an airdrop.
    public static final int RemoveAirdrop = 93;                  //An airdrop has been removed from the game.
    public static final int RemoveArmory = 94;                   //A armory has been removed from the game.
    public static final int RemoveKOTH = 95;                     //The hill has been removed from the game.
    public static final int RemoveInfantry = 96;                 //An Infantry has been removed from the game.
    public static final int RemoveTank = 97;                     //A tank has been removed from the game.
    public static final int RemoveRubble = 98;                   //A rubble has been removed from the game.
    public static final int RemoveCity = 99;                     //A city has been removed from the game.
    public static final int CancelAffiliation = 100;             //A request to cancel an affiliation request.
    public static final int RemoveResourceDeposit = 101;         //A resource deposit has been removed from the game.
    public static final int Prospect = 102;                      //A request to prospect for a resource deposit.
    public static final int RemoveCargoTruck = 103;              //A cargo truck has been removed from the game.
    public static final int AccountMigrated = 104;               //A Google account migration was successful.
    public static final int RemoveProcessor = 105;               //A processor has been removed from the game.
    public static final int RemoveShip = 106;                    //A ship has been removed from the game.
    public static final int RemoveSubmarine = 107;               //A submarine has been removed from the game.
    public static final int DiveOrSurface = 108;                 //A command for a submarine to dive or surface.
    public static final int RemoveArtilleryGun = 109;            //An artillery gun has been removed from the game.
    public static final int AccountUnmigrated = 110;             //An old user needs to migrate their account to Google.
    public static final int RemoveScrapYard = 111;               //A scrap yard has been removed from the game.
    public static final int UpgradeShipyard = 112;               //A command to upgrade a shipyard's production capacity.
    public static final int RemoveDistributor = 113;             //A distributor has been removed from the game.
    public static final int RemoveHelicopter = 114;              //A helicopter has been removed from the game.
    public static final int RemoveStoredHelicopter = 115;        //A stored helicopter has been removed from the game.
    public static final int LocationSpoof = 116;                 //A signal that the player's account changed and tried to login from a new device far away.
    public static final int RemoveChemical = 117;                //A chemical has been removed from the game.
    public static final int ToggleTruckAutoCollect = 118;        //Toggle whether a truck will auto collect.
    public static final int PurchaseCity = 119;                  //A request to purchase a city.
    public static final int RemoveMarket = 120;                  //A market has been removed from the game.
    public static final int Blacklist = 121;                     //A request to blacklist/whitelist a player.
    public static final int MissileSlotUpgradeToMax = 122;       //Max the slots on a missile battery.
    public static final int InterceptorSlotUpgradeToMax = 123;   //Max out the slots on a SAM.
    
    private static final int MESSAGE_BUFFER_SIZE = 10240;
    private static final int COMMS_THREAD_SLEEP = 20;
    private static final int ONE_SECOND = 1000;
    
    protected ConnectionProvider connection;
    
    protected TobComm tobComm;
    
    private int lIdleTime = 0;
    private boolean bDead = false;
    
    protected String strLogName;
    
    private Thread processThread;
    
    //Bursting reduces number of TCP packets when sending lots of objects (i.e. during game snapshot).
    private boolean bBursting = false;
    private List<byte[]> BurstList;
    
    //Synchronised buffering of send packets removes concurrency issues with TCP socket writes.
    private Queue<byte[]> MessageSendList = new ConcurrentLinkedQueue();
    
    //Connection monitoring.
    private int lTotalDownloaded = 0;
    private int lTotalUploaded = 0;
    private int lDownloadRateCounter = 0;
    private int lDownloadRate = 0;
    private int lUploadRateCounter = 0;
    private int lUploadRate = 0;
    private ShortDelay dlyConnectionRates = new ShortDelay();
    
    public LaunchSession(Socket socket)
    {
        this.connection = new TCPProvider(socket, this);
        strLogName = LaunchLog.GetTimeFormattedLogName(connection.GetAddress());
        tobComm = new TobComm(this);
        //LaunchLog.Log(SESSION, strLogName, "Session created.");
        
        processThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //LaunchLog.Log(SESSION, strLogName, "Session thread created.");
                
                if(!connection.Initialise())
                {
                    //Errored.
                    //LaunchLog.Log(SESSION, strLogName, "Error initialising session.");
                    Close();
                }
                
                while(!bDead)
                {
                    if(connection.DataAvailable())
                    {
                        try
                        {
                            lIdleTime = 0;
                            byte[] cMessage = new byte[MESSAGE_BUFFER_SIZE];
                            int lRead = connection.Read(cMessage);

                            if(lRead > 0)
                            {
                                lTotalDownloaded += lRead;
                                lDownloadRateCounter += lRead;
                                tobComm.ProcessBytes(Arrays.copyOfRange(cMessage, 0, lRead));
                            }
                        }
                        catch(OutOfMemoryError ex)
                        {
                            
                        }  
                    }
                    
                    if(connection.Died())
                    {
                        //Session errored.
                        //LaunchLog.Log(SESSION, strLogName, "Session errored while getting data.");
                        Close();
                    }
                    
                    Process();
                    
                    while(!MessageSendList.isEmpty())
                    {
                        byte[] cData = MessageSendList.poll();

                        connection.Write(cData);

                        if(connection.Died())
                        {
                            //Output stream errored.
                            //LaunchLog.Log(SESSION, strLogName, String.format("Session error writing %d bytes.", cData.length));
                            Close();
                        }
                        else
                        {
                            lTotalUploaded += cData.length;
                            lUploadRateCounter += cData.length;
                        }
                    }
                    
                    try
                    {
                        Thread.sleep(COMMS_THREAD_SLEEP);
                    }
                    catch(InterruptedException ex) { /* Don't care */ }
                }
                
                //LaunchLog.Log(SESSION, strLogName, "Session thread finished normally.");
            }
        });
    }
    
    protected final void Start()
    {
        processThread.start();
    }
    
    protected abstract void Process();
    
    public void Tick(int lMS)
    {
        lIdleTime += lMS;

        if(lIdleTime > GetTimeout())
        {
            //Session timed out.
            //LaunchLog.Log(SESSION, strLogName, "Session timed out. Closing.");
            Close();
        }
        
        dlyConnectionRates.Tick(lMS);
        
        if(dlyConnectionRates.Expired())
        {
            lDownloadRate = lDownloadRateCounter;
            lDownloadRateCounter = 0;
            lUploadRate = lUploadRateCounter;
            lUploadRateCounter = 0;
            dlyConnectionRates.Set(ONE_SECOND);
        }
    }
    
    public int GetTimeoutRemaining()
    {
        return GetTimeout() - lIdleTime;
    }
    
    public boolean GetTimingOut()
    {
        return lIdleTime > ONE_SECOND;
    }
    
    public void Close()
    {
        if(!bDead)
        {
            bDead = true;
            
            connection.Close();
            
            try
            {
                processThread.interrupt();
            }
            catch(Exception ex) 
            { 
                //LaunchLog.Log(SESSION, strLogName, "Error: could not interrupt processThread..");
            }

            //LaunchLog.Log(SESSION, strLogName, String.format("Session closed after downloading %dB & uploading %dB.", lTotalDownloaded, lTotalUploaded));
        }
        else
        {
            //LaunchLog.Log(SESSION, strLogName, "The session is already closed.");
        }
    }
    
    public final boolean IsAlive() { return !bDead; }
    
    public int GetDownloadRate() { return lDownloadRate; }
    
    public int GetUploadRate() { return lUploadRate; }
    
    protected void StartBurst()
    {
        BurstList = new ArrayList();
        bBursting = true;
    }
    
    protected void EndBurst()
    {
        bBursting = false;
        
        int lSize = 0;
        
        for(byte[] array : BurstList)
        {
            if(array != null)
                lSize += array.length;
        }
        
        byte[] cBurstData = new byte[lSize];
        int lOffset = 0;
        
        for(byte[] array : BurstList)
        {
            if(array != null)
            {
                System.arraycopy(array, 0, cBurstData, lOffset, array.length);
                lOffset += array.length;
            }
        }
        
        MessageSendList.add(cBurstData);
        
        //Explicitly Free the memory (otherwise lots of these could hang around).
        BurstList = null;
    }

    @Override
    public void BytesToSend(byte[] cData)
    {
        if(!bDead)
        {
            if(bBursting)
            {
                BurstList.add(cData);
            }
            else
            {
                MessageSendList.add(cData);
            }
        }
        else
        {
            //LaunchLog.Log(SESSION, strLogName, String.format("Not sending %d bytes. The session is dead.", cData.length));
        }
    }

    @Override
    public void Error(String strErrorText)
    {
        //LaunchComm errored. Bin the connection.
        //LaunchLog.Log(SESSION, strLogName, "Connection Error: " + strErrorText);
        Close();
    }

    @Override
    public void ConnectionLog(String strLog)
    {
        //LaunchLog.Log(SESSION, strLogName, "Connection Report: " + strLog);
    }
    
    protected abstract int GetTimeout();
}
