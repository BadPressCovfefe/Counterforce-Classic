/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launchserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import launch.comm.LaunchServerComms;
import launch.game.Alliance;
import launch.game.Defs;
import launch.game.LaunchServerGame;
import launch.game.treaties.Treaty;
import launch.game.User;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;
import launch.game.systems.MissileSystem;

import launch.game.treaties.War;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchPerf;
import launch.utilities.LaunchReport;
import launch.utilities.LaunchUtilities;
import launch.utilities.MissileStats;
import launch.utilities.TerrainChecker;
import storage.GameLoadSaveListener;
import storage.XMLGameLoader;

/**
 *
 * @author tobster
 */
public class LaunchConsole
{
    //TO DO: Some boilerplate is taken from client app TextUtilities. Ideally this functionality should be librarified universally.
    public static final long A_DAY = 86400000;
    public static final long AN_HOUR = 3600000;
    public static final long A_MINUTE = 60000;
    public static final long A_SEC = 1000;
    
    private LaunchServer instance;
    private LaunchServerGame game;
    
    private boolean bQuit = false;
    
    public LaunchConsole(LaunchServer instance, LaunchServerGame game)
    {
        this.instance = instance;
        this.game = game;
    }
    
    private static String GetTimeAmount(long oTimespan)
    {
        long oDays = oTimespan / A_DAY;
        long oHours = (oTimespan % A_DAY) / AN_HOUR;
        long oMinutes = ((oTimespan % A_DAY) % AN_HOUR) / A_MINUTE;
        long oSeconds = (((oTimespan % A_DAY) % AN_HOUR) % A_MINUTE) / A_SEC;

        if(oDays > 0)
        {
            return oDays + "days, " + oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oHours > 0)
        {
            return oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oMinutes > 0)
        {
            return String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else
        {
            return oSeconds + "s";
        }
    }
    
    /**
     * Take an array of what were space-delimited strings and rebuild them into one string from the given index.
     * @param SpaceDelimitedArgs Originally a command sentence split at spaces into an array.
     * @param lIndex The index of the sentence words to rebuild from.
     * @return The rebuilt subsentence.
     */
    private String EverythingFromAndIncluding(String SpaceDelimitedArgs[], int lIndex)
    {
        StringBuilder Result = new StringBuilder();
        
        for(int i = lIndex; i < SpaceDelimitedArgs.length; i++)
        {
            Result.append(SpaceDelimitedArgs[i]);
            if(i != SpaceDelimitedArgs.length - 1)
                Result.append(" ");
        }
        
        return Result.toString();
    }
    
    public void Tick()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            String strInput = br.readLine();
            
            String args[] = strInput.toLowerCase().split(" ");
            String argsOrigCase[] = strInput.split(" ");
            
            switch(args[0])
            {
                case "server_name":
                {
                    LaunchLog.ConsoleMessage(game.GetConfig().GetServerName());
                }
                break;
                
                case "set_player_id":
                {
                    game.InitializePlayerIDAtomic();
                    LaunchLog.ConsoleMessage(String.format("Player Atomic ID initialized. Value: %d", game.GetPlayerIndex()));
                }
                break;
                
                case "alliance_idof":
                {
                    String strMatch = args[1];
                    
                    LaunchLog.ConsoleMessage(String.format("---=== Alliances matching %s ===---", strMatch));
                    
                    for(Alliance alliance : game.GetAlliances())
                    {
                        if(alliance.GetName().toLowerCase().contains(strMatch.toLowerCase()))
                        {
                            LaunchLog.ConsoleMessage(String.format("%d - %s.", alliance.GetID(), alliance.GetName()));
                        }
                    }
                }
                break;
                
                case "list_relations":
                {
                    Integer lID = Integer.valueOf(args[1]);
                    Alliance alliance = game.GetAlliance(lID);
                    
                    for(Alliance otherAlliance : game.GetAlliances())
                    {
                        if(alliance.GetID() != otherAlliance.GetID())
                        {
                            switch(game.GetAllianceRelationship(alliance.GetID(), otherAlliance.GetID()))
                            {
                                case AFFILIATE: LaunchLog.ConsoleMessage(String.format("%s is affiliated with %s", alliance.GetName(), otherAlliance.GetName())); break;
                                case ENEMY: LaunchLog.ConsoleMessage(String.format("%s is at war with %s", alliance.GetName(), otherAlliance.GetName())); break;
                                case PENDING_TREATY: LaunchLog.ConsoleMessage(String.format("%s has a pending treaty with %s", alliance.GetName(), otherAlliance.GetName())); break;
                                case NEUTRAL: LaunchLog.ConsoleMessage(String.format("%s is neutral with %s", alliance.GetName(), otherAlliance.GetName())); break;
                                default: LaunchLog.ConsoleMessage(String.format("%s has unknown allegiance with %s", alliance.GetName(), otherAlliance.GetName())); break;
                            }
                        }
                    }
                }
                break;
                
                case "reset_xp":
                {
                    LaunchLog.ConsoleMessage("Resetting XP...");
                    
                    for(Player player : game.GetPlayers())
                    {
                        if(player != null)
                        {
                            player.SubtractExperience(player.GetExperience());
                            
                            int lKillCount = player.GetTotalKills();
                            int lStructureCount = player.GetStructures().size();
                            
                            player.AddExperience(Defs.STRUCTURE_BUILT_XP * lStructureCount);
                            player.AddExperience((int)(player.GetDistanceTraveled()/Defs.KM_TRAVELED_PER_XP));
                        }
                    }
                    
                    LaunchLog.ConsoleMessage("XP Reset.");
                }
                break;
                
                case "reset_deposits":
                {
                    LaunchLog.ConsoleMessage("Resetting deposits...");
                    //game.GenerateDeposits();
                }
                break;
                
                case "cleardeposits":
                {
                    LaunchLog.ConsoleMessage("Clearing deposits...");
                    
                    for(ResourceDeposit deposit : game.GetResourceDeposits())
                    {
                        deposit.SetRemove();
                    }
                    
                    LaunchLog.ConsoleMessage("Deposits cleared.");
                }
                break;
                
                case "give_membership":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    
                    if(player != null)
                    {
                        player.SetMemberStatus(true);
                        player.SetAdminMember(true);
                        LaunchLog.ConsoleMessage(String.format("Made %s a member.", player.GetName()));
                    }
                }
                break;
                
                case "remove_membership":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    
                    if(player != null)
                    {
                        player.SetAdminMember(false);
                        LaunchLog.ConsoleMessage(String.format("Removed %s's membership.", player.GetName()));
                    }
                }
                break;
                
                case "set_champion":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    
                    if(player != null)
                    {
                        player.SetChampion(true);
                        LaunchLog.ConsoleMessage(String.format("Made %s the champion.", player.GetName()));
                    }
                }
                break;
                
                case "set_veteran":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    
                    if(player != null)
                    {
                        player.SetVeteran(true);
                        LaunchLog.ConsoleMessage(String.format("Made %s a veteran.", player.GetName()));
                    }
                }
                break;
                
                case "launch":
                {
                    Integer lCount = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(Integer.parseInt(args[2]));
                    
                    LaunchLog.ConsoleMessage(String.format("Launching %d missiles.", lCount));
                    game.LaunchDebugMissiles(player, lCount);
                }
                break;
                
                case "attack":
                {
                    Integer lCount = Integer.parseInt(args[1]);
                    Player target = game.GetPlayer(Integer.parseInt(args[2]));
                    
                    if(target != null)
                    {
                        LaunchLog.ConsoleMessage(String.format("Launching %d missiles at %s.", lCount, target.GetName()));
                        game.LaunchDebugAttack(target, lCount);
                    }  
                    else
                    {
                        LaunchLog.ConsoleMessage("Target player does not exist. What are you doing? Get your life figured out.");
                    }
                }
                break;
                
                case "loadshipyards":
                {
                    XMLGameLoader.LoadShipyards("shipyards.txt", game);
                }
                break;
                
                case "devmessage":
                {
                    Integer lID = Integer.valueOf(args[1]);
                    String strBody = EverythingFromAndIncluding(argsOrigCase, 2);
                    
                    User user = game.GetPlayer(lID).GetUser();
                    
                    if(user != null && strBody != null)
                    {
                        game.SendUserAlert(user, "Message From CF Developers!", strBody, false, true);
                        LaunchLog.ConsoleMessage(String.format("Sending %s message with body: %s", game.GetPlayer(lID).GetName(), strBody));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Could not send message, one of the inputs is null.");
                    }
                }
                break;
                
                case "inttimes":
                {
                    for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
                    {
                        LaunchLog.ConsoleMessage(String.format("%s. Build time hrs: %s", type.GetName(), String.valueOf(MissileStats.GetInterceptorBuildTime(type, false, false)/Defs.MS_PER_HOUR)));
                    }
                }
                break;
                
                case "missiletimes":
                {
                    for(MissileType type : game.GetConfig().GetMissileTypes())
                    {
                        if(type.GetPurchasable())
                            LaunchLog.ConsoleMessage(String.format("%s. Build time hrs: %s", type.GetName(), String.valueOf(MissileStats.GetMissileBuildTime(type, false, false)/Defs.MS_PER_HOUR)));
                    }
                }
                break;
                
                case "md":
                {
                    for(MissileType type : game.GetConfig().GetMissileTypes())
                    {
                        LaunchLog.ConsoleMessage(String.format("%s. Max damage: %s", type.GetName(), String.valueOf(MissileStats.GetMaxDamage(type))));
                    }
                }
                break;
                
                case "removewealth":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Integer lAmount = Integer.parseInt(args[2]);
                    String strReason = EverythingFromAndIncluding(argsOrigCase, 3);
                    game.RemoveWealth(lID, lAmount, strReason);
                }
                break;
                
                case "discreteaward":
                {
                    Player player = game.GetPlayer(Integer.parseInt(args[1]));
                    Integer lAmount = Integer.parseInt(args[2]);
                    
                    if(player != null)
                    {
                        player.AddWealth(lAmount);
                    }
                }
                break;
                
                case "kick":
                {
                    Player player = game.GetPlayer(Integer.parseInt(args[1]));
                    
                    if(player != null)
                    {
                        Alliance alliance = game.GetAlliance(player.GetAllianceMemberID());
                        
                        if(alliance != null)
                        {
                            player.SetAllianceID(Alliance.ALLIANCE_ID_UNAFFILIATED);
                            player.SetIsAnMP(false);
                            game.CreateEvent(new LaunchEvent(String.format("[ADMIN] %s was kicked from their alliance.", player.GetName()), LaunchEvent.SoundEffect.RESPAWN));
                            game.AllianceCleanupCheck(alliance, LaunchEntity.ID_NONE, false, false);
                        }
                        else
                        {
                            LaunchLog.ConsoleMessage("Could not kick player. Alliance Null.");
                        }
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Could not kick player. Player Null.");
                    }
                }
                break;
                
                case "list":
                {
                    switch(args[1])
                    {       
                        case "user":
                        {
                            LaunchLog.ConsoleMessage("---=== Users ===---");
                            
                            for(User user : game.GetUsers())
                            {
                                Player player = game.GetPlayer(user.GetPlayerID());
                                LaunchLog.ConsoleMessage(String.format("%s - %s (%d). %s, %d unread reports.", user.GetIdentityKey(), player.GetName(), user.GetPlayerID(), user.GetBanState().name(), user.GetUnreadReports()));
                            }
                            
                            LaunchLog.ConsoleMessage("-------------------");
                        }
                        break;
                        
                        case "player":
                        {
                            LaunchLog.ConsoleMessage("---=== Players ===---");
                            
                            for(Player player : game.GetPlayers())
                            {
                                boolean bPrint = true;
                                
                                if(args.length > 2)
                                {
                                    if(!player.GetName().toLowerCase().contains(args[2]))
                                        bPrint = false;
                                }
                                    
                                if(bPrint)
                                    LaunchLog.ConsoleMessage(String.format("%d - %s.", player.GetID(), player.GetName()));
                            }
                            
                            LaunchLog.ConsoleMessage("---------------------");
                        }
                        break;
                        
                        case "admin":
                        {
                            LaunchLog.ConsoleMessage("---=== Administrators ===---");
                            
                            for(Player player : game.GetPlayers())
                            {
                                if(player.GetIsAnAdmin())
                                {
                                    LaunchLog.ConsoleMessage(String.format("%d - %s.", player.GetID(), player.GetName()));
                                }
                            }
                            
                            LaunchLog.ConsoleMessage("---------------------");
                        }
                        break;
                        
                        //TO DO: other entities.
                        
                        default:
                        {
                            LaunchLog.ConsoleMessage("Invalid command. Usage for 'list':");
                            LaunchLog.ConsoleMessage("'list user'  - Display list of users.");
                            LaunchLog.ConsoleMessage("'list player'- Display list of players.");
                            LaunchLog.ConsoleMessage("'list admin' - Display list of administrators.");
                            //TO DO: other entities.
                        }
                        break;
                    }
                }
                break;
                
                case "info":
                {
                    switch(args[1])
                    {
                        case "all":
                        {
                            LaunchLog.ConsoleMessage("---=== All entities ===---");
                            LaunchLog.ConsoleMessage(String.format("Alliances:     %d", game.GetAlliances().size()));
                            LaunchLog.ConsoleMessage(String.format("Wars:          %d", game.GetTreaties().size()));
                            LaunchLog.ConsoleMessage(String.format("Players:       %d", game.GetPlayers().size()));
                            LaunchLog.ConsoleMessage(String.format("Missiles:      %d", game.GetMissiles().size()));
                            LaunchLog.ConsoleMessage(String.format("Interceptors:  %d", game.GetInterceptors().size()));
                            LaunchLog.ConsoleMessage(String.format("Missile Sites: %d", game.GetMissileSites().size()));
                            LaunchLog.ConsoleMessage(String.format("SAM Sites:     %d", game.GetSAMSites().size()));
                            LaunchLog.ConsoleMessage(String.format("Loots:         %d", game.GetLoots().size()));
                            LaunchLog.ConsoleMessage(String.format("Radiations:    %d", game.GetRadiations().size()));
                            LaunchLog.ConsoleMessage("--------------------------");
                        }
                        break;
                        
                        case "player":
                        {
                            int lID = Integer.parseInt(args[2]);
                            
                            Player player = game.GetPlayer(lID);
                            
                            if(player != null)
                            {
                                LaunchLog.ConsoleMessage(String.format("---=== Player %d - %s ===---", player.GetID(), player.GetName()));
                                PrintEntityInfo(player);
                                LaunchLog.ConsoleMessage(String.format("Avatar ID:            %d", player.GetAvatarID()));
                                LaunchLog.ConsoleMessage(String.format("Wealth:               %d", player.GetWealth()));
                                LaunchLog.ConsoleMessage(String.format("State time remaining: %d", player.GetStateTimeRemaining()));
                                LaunchLog.ConsoleMessage(String.format("Last seen:            %s ago", GetTimeAmount(System.currentTimeMillis() - player.GetLastSeen())));
                                
                                String strAlliance = "<Isn't in one>";
                                
                                if(player.GetAllianceIDForDataStorage() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                                {
                                    Alliance alliance = game.GetAlliance(player.GetAllianceIDForDataStorage());
                                    
                                    if(player.GetIsAnMP())
                                        strAlliance = String.format("%s <LEADER>", alliance.GetName());
                                    else if(player.GetRequestingToJoinAlliance())
                                        strAlliance = String.format("%s <REQ.JOIN>", alliance.GetName());
                                    else
                                        strAlliance = String.format("%s", alliance.GetName());
                                }
                                
                                LaunchLog.ConsoleMessage(String.format("Alliance:           : %s", strAlliance));
                                
                                if(player.GetAWOL()) LaunchLog.ConsoleMessage("AWOL.");
                                if(player.GetRespawnProtected()) LaunchLog.ConsoleMessage("Respawn protected.");
                                if(player.GetIsAnAdmin()) LaunchLog.ConsoleMessage("Is an administrator.");
                                
                                LaunchLog.ConsoleMessage("\nCarried systems:");
                                
                                User user = null;
                                for(User userItr : game.GetUsers())
                                {
                                    if(userItr.GetPlayerID() == lID)
                                    {
                                        user = userItr;
                                        break;
                                    }
                                }
                                
                                if(user != null)
                                {
                                    LaunchLog.ConsoleMessage("\nUser info:");
                                    LaunchLog.ConsoleMessage("Ban state: " + user.GetBanState().name());
                                    if(user.GetUnderAttack()) LaunchLog.ConsoleMessage("Under attack.");
                                    if(user.GetNuclearEscalation()) LaunchLog.ConsoleMessage("Nuclear escalation.");
                                    if(user.GetAllyUnderAttack()) LaunchLog.ConsoleMessage("Ally under attack.");
                                    LaunchLog.ConsoleMessage(String.format("%d unread reports.", user.GetUnreadReports()));
                                }
                                
                                LaunchLog.ConsoleMessage(String.format("---=== Player %d - %s ===---", player.GetID(), player.GetName()).replaceAll(".", "-"));
                            }
                            else
                            {
                                LaunchLog.ConsoleMessage(String.format("Player with ID %d does not exist!", lID));
                            }
                        }
                        break;
                        
                        //TO DO: other entities.
                        
                        default:
                        {
                            LaunchLog.ConsoleMessage("Invalid command. Usage for 'info':");
                            LaunchLog.ConsoleMessage("'info all' - Display a brief summary of everything.");
                            LaunchLog.ConsoleMessage("'info player [id]' - Display information about player [id].");
                            //TO DO: other entities.
                        }
                        break;
                    }
                }
                break;
                
                case "idof":
                {
                    String strMatch = args[1];
                    
                    LaunchLog.ConsoleMessage(String.format("---=== Players matching %s ===---", strMatch));
                    
                    for(Player player : game.GetPlayers())
                    {
                        if(player.GetName().toLowerCase().contains(strMatch.toLowerCase()))
                        {
                            LaunchLog.ConsoleMessage(String.format("%d - %s.", player.GetID(), player.GetName()));
                        }
                    }
                }
                break;
                
                case "hashof":
                {
                    String strMatch = args[1];
                    
                    LaunchLog.ConsoleMessage(String.format("---=== Players matching device hash %s ===---", strMatch));
                    
                    for(Player player : game.GetPlayers())
                    {
                        User user = player.GetUser();
                        
                        if(user != null)
                        {
                            if(user.GetDeviceID().toLowerCase().contains(strMatch.toLowerCase()))
                            {
                                LaunchLog.ConsoleMessage(String.format("%d - %s Hash: %s.", player.GetID(), player.GetName(), user.GetDeviceID()));
                            }
                        }
                    }
                }
                break;
                
                case "ipof":
                {
                    String strMatch = args[1];
                    
                    LaunchLog.ConsoleMessage(String.format("---=== Players matching IP %s ===---", strMatch));
                    
                    for(Player player : game.GetPlayers())
                    {
                        User user = player.GetUser();
                        
                        if(user != null)
                        {
                            if(user.GetLastIP().toLowerCase().contains(strMatch.toLowerCase()))
                            {
                                LaunchLog.ConsoleMessage(String.format("%d - %s Last IP: %s.", player.GetID(), player.GetName(), user.GetLastIP()));
                            }
                        }
                    }
                }
                break;
                
                case "apphashof":
                {
                    String strMatch = args[1];
                    
                    LaunchLog.ConsoleMessage(String.format("---=== Players matching app hash %s ===---", strMatch));
                    
                    for(Player player : game.GetPlayers())
                    {
                        User user = player.GetUser();
                        
                        if(user != null)
                        {
                            if(user.GetAppListShortHash().toLowerCase().contains(strMatch.toLowerCase()))
                            {
                                LaunchLog.ConsoleMessage(String.format("%d - %s App hash: %s.", player.GetID(), player.GetName(), user.GetAppListShortHash()));
                            }
                        }
                    }
                }
                break;
                
                case "ffwd":
                {
                    LaunchLog.ConsoleMessage("Fast forwarding...");
                    game.DebugAdvanceTicks(Integer.parseInt(args[1]));
                }
                break;
                
                case "endminute":
                {
                    LaunchLog.ConsoleMessage("Forcing end of minute.");
                    game.DebugForceEndOfMinute();
                }
                break;
                
                case "endhour":
                {
                    LaunchLog.ConsoleMessage("Forcing end of hour.");
                    game.DebugForceEndOfHour();
                }
                break;
                
                case "endday":
                {
                    LaunchLog.ConsoleMessage("Forcing end of day.");
                    game.DebugForceEndOfDay();
                }
                break;
                
                case "endweek":
                {
                    LaunchLog.ConsoleMessage("Forcing end of week.");
                    game.DebugForceEndOfWeek();
                }
                break;
                
                case "save":
                {
                    if(game.GetConfig().DebugMode())
                    {
                        //Game won't save itself in debug mode. We must force it.
                        LaunchLog.ConsoleMessage("In debug mode; forcing manual save.");
                        instance.SaveTheGame();
                    }
                    else
                    {
                        game.Save();
                    }
                }
                break;
                
                case "tempban":
                {
                    if(game.TempBan(Integer.parseInt(args[1]), strInput.substring(args[0].length() + args[1].length() + 2), "[DEEP ADMIN]"))
                    {
                        Player player = game.GetPlayer(Integer.parseInt(args[1]));
                        LaunchLog.ConsoleMessage(String.format("Temp banned %s.", player.GetName()));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Could not ban player.");
                    }
                }
                break;
                
                case "permban":
                {
                    if(game.PermaBan(Integer.parseInt(args[1]), strInput.substring(args[0].length() + args[1].length() + 2), "[DEEP ADMIN]"))
                    {
                        Player player = game.GetPlayer(Integer.parseInt(args[1]));
                        LaunchLog.ConsoleMessage(String.format("Perm banned %s.", player.GetName()));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Could not ban player.");
                    }
                }
                break;
                
                case "unban":
                {
                    if(game.ConsoleUnban(Integer.parseInt(args[1])))
                    {
                        Player player = game.GetPlayer(Integer.parseInt(args[1]));
                        LaunchLog.ConsoleMessage(String.format("Unbanned %s.", player.GetName()));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Could not unban player.");
                    }
                }
                break;
                
                case "clean":
                {
                    game.CleanUpUnownedEntities();
                }
                break;
                
                case "allianceclean":
                {
                    game.ForceAllianceDisbandChecks();
                }
                break;
                
                case "quit":
                case "exit":
                {
                    game.ShutDown();
                    bQuit = true;
                }
                break;
                
                case "logstop":
                {
                    for(LaunchLog.LogType logType : LaunchLog.LogType.values())
                    {
                        LaunchLog.SetConsoleLoggingEnabled(logType, false);
                    }
                    
                    LaunchLog.ConsoleMessage("Game console logging disabled.");
                }
                break;
                
                case "logresume":
                {
                    for(LaunchLog.LogType logType : LaunchLog.LogType.values())
                    {
                        LaunchLog.SetConsoleLoggingEnabled(logType, true);
                    }
                    
                    LaunchLog.ConsoleMessage("Game console logging enabled.");
                }
                break;
                
                case "event":
                {
                    String strAnnouncement = strInput.substring(args[0].length() + 1);
                    LaunchLog.ConsoleMessage(String.format("Announcing \"%s\"", strAnnouncement));
                    game.CreateEvent(new LaunchEvent(strAnnouncement, LaunchEvent.SoundEffect.RESPAWN));
                }
                break;
                
                case "report":
                {
                    String strAnnouncement = strInput.substring(args[0].length() + 1);
                    LaunchLog.ConsoleMessage(String.format("Announcing \"%s\"", strAnnouncement));
                    game.CreateReport(new LaunchReport(strAnnouncement, true));
                }
                break;
                
                case "diag":
                {
                    LaunchServerComms comms = game.GetServerComms();
                    
                    LaunchLog.ConsoleMessage("---=== Diagnostic Report ===---");
                    LaunchLog.ConsoleMessage("");
                    LaunchLog.ConsoleMessage("Ticks");
                    LaunchLog.ConsoleMessage(String.format("Game tick starts: %d", game.GetGameTickStarts()));
                    LaunchLog.ConsoleMessage(String.format("Game tick starts: %d", game.GetGameTickEnds()));
                    LaunchLog.ConsoleMessage(String.format("Comms tick ends:  %d", game.GetCommTickStarts()));
                    LaunchLog.ConsoleMessage(String.format("Comms tick ends:  %d", game.GetCommTickEnds()));
                    LaunchLog.ConsoleMessage(String.format("Comms tick ends:  %d", game.GetCommTickEnds()));
                    LaunchLog.ConsoleMessage("");
                    LaunchLog.ConsoleMessage("Server Comms");
                    LaunchLog.ConsoleMessage(String.format("Active sessions:  %d", comms.GetActiveSessions()));
                    LaunchLog.ConsoleMessage(String.format("Total opened:     %d", comms.GetTotalSessionsOpened()));
                    LaunchLog.ConsoleMessage(String.format("Total closed:     %d", comms.GetTotalSessionsClosed()));
                    LaunchLog.ConsoleMessage(String.format("Most ever:        %d", comms.GetMostActiveSessions()));
                    LaunchLog.ConsoleMessage("");
                    
                    LaunchLog.ConsoleMessage("-------------------------------");
                }
                break;
                
                case "adminify":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    player.SetIsAnAdmin(true);
                    LaunchLog.ConsoleMessage(String.format("Made %s an administrator.", player.GetName()));
                }
                break;
                
                case "bossify":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    player.SetBoss(true);
                    LaunchLog.ConsoleMessage(String.format("Made %s a boss.", player.GetName()));
                }
                break;
                
                case "bossall":
                {
                    for (Player player : game.GetPlayers())
                    {
                        player.GetID();
                        player.SetBoss(true);
                        LaunchLog.ConsoleMessage(String.format("Made %s a boss.", player.GetName()));
                    }
                }
                
                case "unbossify":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    player.SetBoss(false);
                    LaunchLog.ConsoleMessage(String.format("Unmade %s a boss.", player.GetName()));
                }
                break;
                
                case "unadminify":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Player player = game.GetPlayer(lID);
                    player.SetIsAnAdmin(false);
                    LaunchLog.ConsoleMessage(String.format("Unmade %s an administrator.", player.GetName()));
                }
                break;
                
                case "transfer":
                {
                    Integer lFromID = Integer.parseInt(args[1]);
                    Integer lToID = Integer.parseInt(args[2]);
                    if(game.TransferAccount(lFromID, lToID))
                        LaunchLog.ConsoleMessage(String.format("Transferred %d to %d.", lFromID, lToID));
                    else
                        LaunchLog.ConsoleMessage(String.format("Could not transfer %d to %d.", lFromID, lToID));
                }
                break;
                
                case "rename":
                {
                    Integer lPlayerID = Integer.parseInt(args[1]);
                    String strNewName = argsOrigCase[2];
                    
                    //De-split to allow spaces in the player name, if applicable.
                    for(int i = 3; i < argsOrigCase.length; i++)
                    {
                        strNewName += " ";
                        strNewName += argsOrigCase[i];
                    }
                    
                    if(game.ChangePlayerName(lPlayerID, strNewName))
                        LaunchLog.ConsoleMessage(String.format("Renamed %d to %s.", lPlayerID, strNewName));
                    else
                        LaunchLog.ConsoleMessage(String.format("Could not rename %d to %s.", lPlayerID, strNewName));
                }
                break;
                
                case "performance":
                {
                    LaunchLog.ConsoleMessage("---=== Performance ===---");
                    LaunchPerf.PrintLatestSamples();
                    LaunchLog.ConsoleMessage("-------------------------");
                    
                }
                break;
                
                case "compinv":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Integer lTimeHours = Integer.parseInt(args[2]);
                    
                    int lTimeMs = lTimeHours * Defs.MS_PER_HOUR;
                    
                    game.SetCompassionateInvulnerability(lID, lTimeMs);
                    
                    LaunchLog.ConsoleMessage(String.format("Gave player %s %shrs of compassionate invulnerability.", args[1], args[2]));
                }
                break;
                
                case "testalert":
                {
                    game.TestAlert(Integer.parseInt(args[1]));
                    
                    LaunchLog.ConsoleMessage(String.format("Testing alert for player %s.", args[1]));
                }
                break;
                
                case "park":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    
                    game.ParkPlayer(lID);
                    
                    LaunchLog.ConsoleMessage(String.format("Parked player %s for 90 days.", args[1]));
                }
                break;
                
                case "entitycount":
                {
                    LaunchLog.ConsoleMessage("---=== Countable Entity Stats ===---");
                    
                    int lPlayers = 0;
                    int lAlive = 0;
                    int lAWOL = 0;
                    int lRespawnProtected = 0;
                    int lLeader = 0;
                    int lAdmin = 0;
                    
                    for(Player player : game.GetPlayers())
                    {
                        lPlayers++;
                        
                        if(player.GetAWOL())
                        {
                            lAWOL++;
                        }
                        else 
                        {
                            if(player.IsRespawnProtected())
                            {
                                lRespawnProtected++;
                            }

                            if(player.GetIsAnMP())
                            {
                                lLeader++;
                            }

                            if(player.GetIsAnAdmin())
                            {
                                lAdmin++;
                            }
                        }
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Players: %d, of which %d are alive and %d are AWOL.", lPlayers, lAlive, lAWOL));
                    LaunchLog.ConsoleMessage(String.format("%d are respawn protected.", lRespawnProtected));
                    LaunchLog.ConsoleMessage(String.format("%d are alliance leaders.", lLeader));
                    LaunchLog.ConsoleMessage(String.format("%d are admins.", lAdmin));
                    LaunchLog.ConsoleMessage("");
                    
                    LaunchLog.ConsoleMessage(String.format("Structures: %d", game.GetAllStructures().size()));
                    
                    int lNuclearSites = 0;
                    int lOnline = 0;
                    int lBooting = 0;
                    int lSelling = 0;
                    
                    for(MissileSite missileSite : game.GetMissileSites())
                    {
                        if(missileSite.CanTakeICBM())
                            lNuclearSites++;
                        
                        if(missileSite.GetOnline())
                            lOnline++;
                        
                        if(missileSite.GetBooting())
                            lBooting++;
                        
                        if(missileSite.GetSelling())
                            lSelling++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Missile Sites: %d, of which %d can take nukes.", game.GetMissileSites().size(), lNuclearSites));
                    LaunchLog.ConsoleMessage(String.format("%d are online, %d are booting and %d are being sold.", lOnline, lBooting, lSelling));
                    
                    lOnline = 0;
                    lBooting = 0;
                    lSelling = 0;
                    
                    for(SAMSite samSite : game.GetSAMSites())
                    {
                        if(samSite.GetOnline())
                            lOnline++;
                        
                        if(samSite.GetBooting())
                            lBooting++;
                        
                        if(samSite.GetSelling())
                            lSelling++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("SAM Sites: %d", game.GetSAMSites().size()));
                    LaunchLog.ConsoleMessage(String.format("%d are online, %d are booting and %d are being sold.", lOnline, lBooting, lSelling));
                    
                    lOnline = 0;
                    lBooting = 0;
                    lSelling = 0;
                    
                    for(SentryGun sentryGun : game.GetSentryGuns())
                    {
                        if(sentryGun.GetOnline())
                            lOnline++;
                        
                        if(sentryGun.GetBooting())
                            lBooting++;
                        
                        if(sentryGun.GetSelling())
                            lSelling++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Sentry Guns: %d", game.GetSentryGuns().size()));
                    LaunchLog.ConsoleMessage(String.format("%d are online, %d are booting and %d are being sold.", lOnline, lBooting, lSelling));
                    
                    lOnline = 0;
                    lBooting = 0;
                    lSelling = 0;
                    int lCompeting = 0;
                    
                    for(OreMine oreMine : game.GetOreMines())
                    {
                        if(oreMine.GetOnline())
                            lOnline++;
                        
                        if(oreMine.GetBooting())
                            lBooting++;
                        
                        if(oreMine.GetSelling())
                            lSelling++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Ore Mines: %d, of which %d are too close to other ones.", game.GetOreMines().size(), lCompeting));
                    LaunchLog.ConsoleMessage(String.format("%d are online, %d are booting and %d are being sold.", lOnline, lBooting, lSelling));
                    LaunchLog.ConsoleMessage("");
                    
                    lOnline = 0;
                    lBooting = 0;
                    lSelling = 0;                   
                    
                    for(RadarStation radarStation : game.GetRadarStations())
                    {
                        if(radarStation.GetOnline())
                            lOnline++;
                        
                        if(radarStation.GetBooting())
                            lBooting++;
                        
                        if(radarStation.GetSelling())
                            lSelling++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Radar Stations: %d", game.GetRadarStations().size()));
                    LaunchLog.ConsoleMessage(String.format("%d are online, %d are booting and %d are being sold.", lOnline, lBooting, lSelling));
                    LaunchLog.ConsoleMessage("");
                    
                    long oLootTotal = 0;
                    
                    for(Loot loot : game.GetLoots())
                    {
                        oLootTotal += loot.GetValue();
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("Loots: %d, totalling £%d.", game.GetLoots().size(), oLootTotal));
                    LaunchLog.ConsoleMessage("");
                    
                    LaunchLog.ConsoleMessage(String.format("Alliances: %d", game.GetAlliances().size()));
                    
                    int lAffiliation = 0;
                    int lWar = 0;
                    int lAffiliationProposal = 0;
                    
                    for(Treaty treaty : game.GetTreaties())
                    {
                        if(treaty.GetType() == Treaty.Type.AFFILIATION)
                            lAffiliation++;
                        
                        if(treaty.GetType() == Treaty.Type.WAR)
                            lWar++;
                        
                        if(treaty.GetType() == Treaty.Type.AFFILIATION_REQUEST)
                            lAffiliationProposal++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("There are %d wars, %d affiliations and %d proposals.", lWar, lAffiliation, lAffiliationProposal));
                    LaunchLog.ConsoleMessage("");
                    
                    LaunchLog.ConsoleMessage("------------------------------------");
                }
                break;
                        
                case "cleanupdead":
                {
                    for(Player player : game.GetPlayers())
                    {
                        if(!player.Functioning() && (player.GetLastSeen() + (4 * A_DAY)) < System.currentTimeMillis() && (player.GetJoinTime() + (7 * A_DAY)) >= System.currentTimeMillis())
                        {
                            Integer lID = player.GetID();
                            game.DeletePlayer(lID, true);
                        }
                    }
                }
                break;
                
                case "weaponcount":
                {
                    LaunchLog.ConsoleMessage("---=== Countable Weapon Stats ===---");
                    
                    Map<Integer, Integer> MissileSiloCounts = new HashMap();
                    Map<Integer, Integer> MissileAirborneCounts = new HashMap();
                    int lTotal = 0;
                    int lTotalAirborne = 0;
                    
                    for(MissileType type : game.GetConfig().GetMissileTypes())
                    {
                        MissileSiloCounts.put(type.GetID(), 0);
                        MissileAirborneCounts.put(type.GetID(), 0);
                    }
                    
                    for(MissileSite missileSite : game.GetMissileSites())
                    {
                        MissileSystem system = missileSite.GetMissileSystem();
                        
                        for(byte c = (byte)0; c < system.GetSlotCount(); c++)
                        {
                            if(system.GetSlotHasMissile(c))
                            {
                                int lType = system.GetSlotMissileType(c);
                                int lCount = MissileSiloCounts.get(lType) + 1;
                                MissileSiloCounts.put(lType, lCount);
                                lTotal++;
                            }
                        }
                    }
                    
                    for(Missile missile : game.GetMissiles())
                    {
                        int lType = missile.GetType();
                        int lCount = MissileAirborneCounts.get(lType) + 1;
                        MissileAirborneCounts.put(lType, lCount);
                        lTotal++;
                        lTotalAirborne++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("All Missiles: %d, of which %d are airborne.", lTotal, lTotalAirborne));
                    
                    for(MissileType type : game.GetConfig().GetMissileTypes())
                    {
                        LaunchLog.ConsoleMessage(String.format("%s: %d siloed, %d airborne.", type.GetName(), MissileSiloCounts.get(type.GetID()), MissileAirborneCounts.get(type.GetID())));
                    }
                    
                    LaunchLog.ConsoleMessage("");
                    
                    Map<Integer, Integer> InterceptorSiloCounts = new HashMap();
                    Map<Integer, Integer> InterceptorAirborneCounts = new HashMap();
                    lTotal = 0;
                    lTotalAirborne = 0;
                    
                    for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
                    {
                        InterceptorSiloCounts.put(type.GetID(), 0);
                        InterceptorAirborneCounts.put(type.GetID(), 0);
                    }
                    
                    for(SAMSite samSite : game.GetSAMSites())
                    {
                        MissileSystem system = samSite.GetInterceptorSystem();
                        
                        for(byte c = (byte)0; c < system.GetSlotCount(); c++)
                        {
                            if(system.GetSlotHasMissile(c))
                            {
                                int lType = system.GetSlotMissileType(c);
                                int lCount = InterceptorSiloCounts.get(lType) + 1;
                                InterceptorSiloCounts.put(lType, lCount);
                                lTotal++;
                            }
                        }
                    }
                    
                    for(Interceptor interceptor : game.GetInterceptors())
                    {
                        int lType = interceptor.GetType();
                        int lCount = InterceptorAirborneCounts.get(lType) + 1;
                        InterceptorAirborneCounts.put(lType, lCount);
                        lTotal++;
                        lTotalAirborne++;
                    }
                    
                    LaunchLog.ConsoleMessage(String.format("All Interceptors: %d, of which %d are airborne.", lTotal, lTotalAirborne));
                    
                    for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
                    {
                        LaunchLog.ConsoleMessage(String.format("%s: %d siloed, %d airborne.", type.GetName(), InterceptorSiloCounts.get(type.GetID()), InterceptorAirborneCounts.get(type.GetID())));
                    }
                    
                    LaunchLog.ConsoleMessage("");
                    
                    LaunchLog.ConsoleMessage("------------------------------------");
                }
                break;
                
                case "giveshit":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    
                    if(game.TestGiveShitty(lID))
                    {
                        LaunchLog.ConsoleMessage(String.format("Gave player %s a shitty test missile.", args[1]));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage(String.format("Couldn't give player %s a shitty test missile.", args[1]));
                    }
                }
                break;
                
                case "cleanavatars":
                {
                    LaunchLog.ConsoleMessage("Cleaning up avatars...");
                    game.CleanAvatars();
                }
                break;
                
                case "purgeavatars":
                {
                    LaunchLog.ConsoleMessage("Purging avatars...");
                    game.PurgeAvatars();
                }
                break;
                
                case "award":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    Integer lAmount = Integer.parseInt(args[2]);
                    String strReason = EverythingFromAndIncluding(argsOrigCase, 3);
                    game.Award(lID, lAmount, strReason);
                }
                break;
                
                case "stimulus":
                {
                    Integer lAmount = Integer.parseInt(args[1]);
                    game.Stimulus(lAmount);
                }
                break;
                
                case "sendalert":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    game.SendAlert(lID);
                }
                break;
                
                case "approve":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    
                    if(game.Approve(lID, "[deep admin]"))
                    {
                        LaunchLog.ConsoleMessage(String.format("Approved player %s account.", args[1]));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage(String.format("Couldn't approve player %s account.", args[1]));
                    }
                }
                break;
                
                case "reqchecks":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    
                    if(game.RequireNewChecks(lID, "[deep admin]"))
                    {
                        LaunchLog.ConsoleMessage(String.format("Set checks required for player %s account.", args[1]));
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage(String.format("Couldn't set checks required for player %s account.", args[1]));
                    }
                }
                break;
                
                case "listrestricted":
                {
                    LaunchLog.ConsoleMessage("---=== Restricted Accounts ===---");

                    for(User user : game.GetUsers())
                    {
                        if(user.AccountRestricted())
                        {
                            Player player = game.GetPlayer(user.GetPlayerID());
                            String strReason = "Unknown";
                            if(user.GetDeviceCheckedDate() == 0)
                                strReason = "Never checked";
                            else if(user.GetLastDeviceCheckFailed())
                                strReason = "Check attempted, failed completely.";
                            else if(user.GetDeviceChecksAPIFailed())
                                strReason = "Check attempted, API failed.";
                            else
                                strReason = "Check completed, their device failed checks.";

                            LaunchLog.ConsoleMessage(String.format("%d - %s - %s", player.GetID(), player.GetName(), strReason));
                        }
                    }

                    LaunchLog.ConsoleMessage("---------------------------------");
                }
                break;
                
                case "eowreport":
                {
                    LaunchLog.ConsoleMessage("---=== Virtual end of week report ===---");
                    
                    Map<Player, Integer> Winnings = new HashMap();
                    
                    for(Treaty treaty : game.GetTreaties())
                    {
                        if(treaty instanceof War)
                        {
                            War war = (War)treaty;
                            Alliance alliance1 = game.GetAlliance(treaty.GetAllianceID1());
                            Alliance alliance2 = game.GetAlliance(treaty.GetAllianceID2());
                            LaunchLog.ConsoleMessage(String.format("War [%s] vs [%s]", alliance1.GetName(), alliance2.GetName()));
                            LaunchLog.ConsoleMessage(String.format("Total cost: £%d", war.GetTotalSpending()));
                            LaunchLog.ConsoleMessage(String.format("Kills: %d - %d", war.GetKills1(), war.GetKills2()));
                            LaunchLog.ConsoleMessage(String.format("Deaths: %d - %d", war.GetDeaths1(), war.GetDeaths2()));
                            LaunchLog.ConsoleMessage(String.format("Income: £%d - £%d", war.GetIncome1(), war.GetIncome2()));
                            LaunchLog.ConsoleMessage(String.format("Offence Efficiency: £%.1f/hp - £%.1f/hp", war.GetOffenceEfficiency1(), war.GetOffenceEfficiency2()));
                            LaunchLog.ConsoleMessage(String.format("Defence Efficiency: £%.1f/hp - £%.1f/hp", war.GetDefenceEfficiency1(), war.GetDefenceEfficiency2()));
                            LaunchLog.ConsoleMessage(String.format("Overall: %d - %d", war.GetWonFactors1(), war.GetWonFactors2()));
                            
                            List<Player> Winners = new ArrayList();
                            
                            int lPerPlayerWinnings = 0;
                            
                            if(war.GetWonFactors1() > war.GetWonFactors2())
                            {
                                Winners = game.GetMembers(alliance1);
                                lPerPlayerWinnings = (int)((((float)war.GetTotalSpending() / (float)Winners.size()) * 0.333f) + 0.5f);
                                LaunchLog.ConsoleMessage(String.format("%s wins! £%d each!", alliance1.GetName(), lPerPlayerWinnings));
                            }
                            else if(war.GetWonFactors2() > war.GetWonFactors1())
                            {
                                Winners = game.GetMembers(alliance2);
                                lPerPlayerWinnings = (int)((((float)war.GetTotalSpending() / (float)Winners.size()) * 0.333f) + 0.5f);
                                LaunchLog.ConsoleMessage(String.format("%s wins! £%d each!", alliance2.GetName(), lPerPlayerWinnings));
                            }
                            else
                                LaunchLog.ConsoleMessage("Draw!");
                            
                            for(Player player : Winners)
                            {
                                if(Winnings.containsKey(player))
                                {
                                    int lWinnings = Winnings.get(player);
                                    lWinnings += lPerPlayerWinnings;
                                    Winnings.put(player, lWinnings);
                                }
                                else
                                    Winnings.put(player, lPerPlayerWinnings);
                            }
                            
                            LaunchLog.ConsoleMessage("");
                        }
                    }
                    
                    LaunchLog.ConsoleMessage("Individual player winnings:");
                    
                    for(Map.Entry<Player, Integer> entry : Winnings.entrySet())
                    {
                        LaunchLog.ConsoleMessage(String.format("%s - £%d", entry.getKey().GetName(), entry.getValue()));
                    }
                    
                    LaunchLog.ConsoleMessage("----------------------------------------");
                }
                break;
                
                case "blessname":
                {
                    String strName = strInput.replace("blessname ", "");
                    strName = LaunchUtilities.BlessName(strName);
                    LaunchLog.ConsoleMessage("Blessed version: " + strName);
                }
                break;
                
                case "blessallnames":
                {
                    game.BlessAllNames();
                }
                break;
                
                case "obliterate":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    game.DeletePlayer(lID, true);
                }
                break;
                
                case "recover":
                {
                    Integer lID = Integer.parseInt(args[1]);
                    String strFile = argsOrigCase[2];
                    
                    LaunchLog.ConsoleMessage(String.format("Attempting to recover for player ID %d from %s ...", lID, strFile));
                    
                    //Create a dummy game to contain the file's game contents.
                    LaunchServerGame container = new LaunchServerGame(game.GetConfig(), null, 0);
                    
                    //Declare a local listener for the game loader function.
                    class LocalLoadListener implements GameLoadSaveListener
                    {
                        boolean bErrors = false;
                        
                        @Override
                        public void LoadError(String strDescription)
                        {
                            LaunchLog.ConsoleMessage(String.format("An error occurred when loading: %s", strDescription));
                            bErrors = true;
                        }

                        @Override
                        public void LoadWarning(String strDescription) { /*Ignore warnings.*/ }

                        @Override
                        public void SaveError(String strDescription) { /*Not called here.*/ }
                        
                        @Override
                        public void StartAPI() { /*Not called here.*/ }
                    }
                    
                    LocalLoadListener listener = new LocalLoadListener();
                        
                    //Attempt to load the file.
                    XMLGameLoader.LoadGame(listener, strFile, container);
                    
                    if(listener.bErrors)
                    {
                        LaunchLog.ConsoleMessage("Errors occurred. The restoration will not proceed.");
                    }
                    else
                    {
                        LaunchLog.ConsoleMessage("Game snapshot successfully loaded from file. Commencing restoration...");
                        game.PerformRestoration(lID, container);
                    }
                }
                break;
                
                case "banbomb":
                {
                    Integer lPlayerID = Integer.parseInt(args[1]);
                    Float fltRadius = Float.parseFloat(args[2]);
                    
                    game.BanBomb(lPlayerID, fltRadius);
                }
                break;
                
                case "listproscriptions":
                {
                    game.ListProscriptions();
                }
                break;
                
                case "removeoremines":
                {
                    for(OreMine oreMine : game.GetOreMines())
                    {
                        Player owner = game.GetPlayer(oreMine.GetOwnerID());
                        oreMine.InflictDamage(oreMine.GetMaxHP());
                        owner.AddWealth(game.GetConfig().GetOreMineStructureCost());
                    }
                }
                break;
                
                default:
                {
                    LaunchLog.ConsoleMessage("---=== Available Commands ===---");
                    LaunchLog.ConsoleMessage("countquads         - Count all the cells in the quadtree.");
                    LaunchLog.ConsoleMessage("list [type] [opt]  - List all entities of [type]. [opt] = optional filter.");
                    LaunchLog.ConsoleMessage("info [type] [id]   - Display information about entity [id] of type [type].");
                    LaunchLog.ConsoleMessage("idof [name]        - Return matching IDs of players with [name] in name.");
                    LaunchLog.ConsoleMessage("ffwd [ticks]       - Advance the game by [ticks] ticks.");
                    LaunchLog.ConsoleMessage("endweek            - Force end of week.");
                    LaunchLog.ConsoleMessage("endday             - Force end of day.");
                    LaunchLog.ConsoleMessage("save               - Save the game and take a backup.");
                    LaunchLog.ConsoleMessage("tempban [id] [why] - Temporarily ban player [id] for reason [why].");
                    LaunchLog.ConsoleMessage("permban [id] [why] - Permanently ban player [id] for reason [why].");
                    LaunchLog.ConsoleMessage("unban [id]         - Unban player [id]");
                    LaunchLog.ConsoleMessage("clean              - Remove all unowned stuff from the game.");
                    LaunchLog.ConsoleMessage("allianceclean      - Force alliance cleanup check on all alliances.");
                    LaunchLog.ConsoleMessage("quit'/'exit        - Save, backup, and shut down the server.");
                    LaunchLog.ConsoleMessage("logstop            - Stop logging game messages to console.");
                    LaunchLog.ConsoleMessage("logresume          - Resume logging game messages to console.");
                    LaunchLog.ConsoleMessage("event [text]       - Announce [text] event to all players in the game.");
                    LaunchLog.ConsoleMessage("report [text]      - Announce [text] report to all players in the game.");
                    LaunchLog.ConsoleMessage("diag               - Report diagnostic statistics.");
                    LaunchLog.ConsoleMessage("adminify [id]      - Set player [id] to be an administrator.");
                    LaunchLog.ConsoleMessage("unadminify [id]    - Unset player [id] to be an administrator.");
                    LaunchLog.ConsoleMessage("transfer [id] [id] - Transfer account from player [id] to player [id].");
                    LaunchLog.ConsoleMessage("rename [id] [name] - Rename player [id] to [name].");
                    LaunchLog.ConsoleMessage("weapcost           - Print all weapon price information.");
                    LaunchLog.ConsoleMessage("performance        - Print (and log) a latest tick performance report.");
                    LaunchLog.ConsoleMessage("compinv [id] [hrs] - Grant [id] compassionate invulnerability for [hrs].");
                    LaunchLog.ConsoleMessage("testalert [id]     - Tell player [id] they're under attack.");
                    LaunchLog.ConsoleMessage("park [id]          - Protect player [id]'s account from going AWOL.");
                    LaunchLog.ConsoleMessage("entitycount        - Count all entities in the game.");
                    LaunchLog.ConsoleMessage("weaponcount        - Count all missiles/interceptors by type in the game.");
                    LaunchLog.ConsoleMessage("giveshit [id]      - Give [id] a shitty test missile, if possible.");
                    LaunchLog.ConsoleMessage("cleanavatars       - Invoke the avatar cleanup procedure.");
                    LaunchLog.ConsoleMessage("purgeavatars       - Delete every avatar in the game.");
                    LaunchLog.ConsoleMessage("award [id] [£] [?] - Award player [id] [£] for reason [?].");
                    LaunchLog.ConsoleMessage("eowreport          - Print a virtual end of week report.");
                    LaunchLog.ConsoleMessage("blessname [name]   - Test the name-blessing mechanism with [name].");
                    LaunchLog.ConsoleMessage("blessallnames      - Force the blessing of everything in the game.");
                    LaunchLog.ConsoleMessage("approve [id]       - Approve [id]'s weird account for launching stuff.");
                    LaunchLog.ConsoleMessage("reqchecks [id]     - Flag [id]'s account as requiring checks on next login.");
                    LaunchLog.ConsoleMessage("listrestricted     - List all restricted accounts.");
                    LaunchLog.ConsoleMessage("obliterate [id]    - Really really delete player with [id].");
                    LaunchLog.ConsoleMessage("recover [id] [file]- Attempt to recover [id]'s structures from backup file [file].");
                    LaunchLog.ConsoleMessage("banbomb [id] [dist]- Set off a perma ban bomb at [id]'s location with blast radius [dist]km.");
                    LaunchLog.ConsoleMessage("listproscriptions  - List the currently effective proscriptions.");
                    LaunchLog.ConsoleMessage("cleanupdead        - Delete all dead players who have not been online in 7 days.");
                    LaunchLog.ConsoleMessage("sendalert [id]     - Send a generic firebase alert to player with [id].");
                    LaunchLog.ConsoleMessage("updatetokens       - Update the firebase tokens for all currently online users.");
                    LaunchLog.ConsoleMessage("makesupporter[id]  - Change the supporter tier of the player with [id].");
                    LaunchLog.ConsoleMessage("removewealth[id][$]- Remove money from this player.");
                    LaunchLog.ConsoleMessage("bossify [id]       - Turn [id] into a boss, to stop location updates.");   
                    LaunchLog.ConsoleMessage("unbossify [id]     - Remove boss status from [id].");
                    LaunchLog.ConsoleMessage("removeoremines     - Remove all ore mines and reimburse the owners.");
                        
                    if(game.GetConfig().DebugMode())
                    {
                        LaunchLog.ConsoleMessage("attack [id] [sec]  - Attack player [id] (rand type+player, [sec] away).");
                    }
                    
                    LaunchLog.ConsoleMessage("--------------------------------");
                }
            }
        }
        catch(Exception ex)
        {
            LaunchLog.ConsoleMessage(ex.toString());
            //LaunchLog.ConsoleMessage("Invalid command syntax. Try <command> <help>.");
        }
    }
    
    private void PrintEntityInfo(MapEntity entity)
    {
        LaunchLog.ConsoleMessage(String.format("Position: (%.6f, %.6f)", entity.GetPosition().GetLatitude(), entity.GetPosition().GetLongitude()));
    }
    
    private void PrintDamageableInfo(Damagable damagable)
    {
        LaunchLog.ConsoleMessage(String.format("HP:       %d / %d", damagable.GetHP(), damagable.GetMaxHP()));
    }
    
    private void PrintMissileSystem(MissileSystem system, boolean bIsMissiles)
    {
        LaunchLog.ConsoleMessage(String.format("Reload remaining: %s", GetTimeAmount(system.GetReloadTimeRemaining())));
        LaunchLog.ConsoleMessage(String.format("Reload time:      %s", GetTimeAmount(system.GetReloadTime())));
        LaunchLog.ConsoleMessage("Weapon slots:");
        
        int lMissileSlots = system.GetSlotCount();
        
        for(int lSlot = 0; lSlot < lMissileSlots; lSlot++)
        {
            if(system.GetSlotHasMissile(lSlot))
            {
                String strPrepTime = system.GetSlotReadyToFire(lSlot)? "ready to fire" : String.format("ready in %s", GetTimeAmount(system.GetSlotPrepTimeRemaining(lSlot)));
                
                int lType = system.GetSlotMissileType(lSlot);
                String strTypeName = bIsMissiles? game.GetConfig().GetMissileType(lType).GetName() : game.GetConfig().GetInterceptorType(lType).GetName();
                
                LaunchLog.ConsoleMessage(String.format("%d - %s (%d), %s.", lSlot + 1, strTypeName, lType, strPrepTime));
            }
            else
            {
                LaunchLog.ConsoleMessage(String.format("%d - Empty", lSlot + 1));
            }
        }
    }
    
    public boolean Quat()
    {
        return bQuit;
    }
}
