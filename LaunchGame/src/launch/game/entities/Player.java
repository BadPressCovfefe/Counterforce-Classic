/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.entities.conceptuals.StoredAirplane;
import launch.utilities.LaunchUtilities;
import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.comm.LaunchSession;
import launch.game.Alliance;
import launch.game.Defs;
import launch.game.User;
import launch.game.systems.*;
import launch.utilities.ShortDelay;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchGame;
import launch.utilities.LaunchLog;

/**
 *
 * @author tobster
 */
public final class Player extends MapEntity implements LaunchSystemListener
{
    private static final int DATA_SIZE = 71;
    private static final int STATS_DATA_SIZE = 48;
   
    private static final int FLAG1_BANNED = 0x01;        //Banned. NOTE: NOT SAVED (well, it is, but the server ignores it), but determined at run time when getting player data.
    private static final int FLAG1_PRISONER = 0x02;       //Unused.
    private static final int FLAG1_RES3 = 0x04;          //Unused.
    private static final int FLAG1_RES4 = 0x08;       //Player possesses a cruise missile system.
    private static final int FLAG1_RES5 = 0x10;       //Player possesses a missile defence system.
    private static final int FLAG1_MP = 0x20;            //The player is a leader in an alliance.
    private static final int FLAG1_AWOL = 0x40;          //The player is AWOL. NOTE: AWOL can only happen to dead players, to stop them being considered for scoring.
    private static final int FLAG1_RSPAWN_PROT = 0x80;   //The player is subject to respawn protection.
    
    private static final int FLAG2_ALLIANCE_REQ_JOIN = 0x01;    //Player is only requesting to join an alliance, and isn't actually in one yet.
    private static final int FLAG2_ADMIN = 0x02;                //Player is an administrator.
    private static final int FLAG2_BOSS = 0x04;                 //Unused.
    private static final int FLAG2_MUTED = 0x08;                //Unused.
    private static final int FLAG2_CHAMPION = 0x10;             //Won the city contest last week.
    private static final int FLAG2_VET = 0x20;                  //Unused.
    private static final int FLAG2_RES6 = 0x40;                 //Unused.
    private static final int FLAG2_RES7 = 0x80;                 //Unused.
    
    //Normal data.
    private String strName;                             //Player's name.
    private int lAvatarID;                              //Avatar ID.
    private long oLastSeen;                             //When they were last seen (UNIX epoch).
    private ShortDelay dlyStateChange;                  //Time before the player may respawn if dead, or respawn protection will cease.
    private int lAllianceID;                            //Alliance ID.
    private ShortDelay dlyAllianceCooloff;              //Time before the player may join another alliance after leaving one.
    private ShortDelay dlyAirdrop;
    private ShortDelay dlyProspect;
    private List<Integer> Blacklist = new ArrayList<>();
    
    //Normal data, condensed into flags.
    private boolean bBanned = false;                    //NOTE: NOT SAVED (well, it is, but the server ignores it), but determined at run time when getting player data.
    private boolean bLeader;
    private boolean bAWOL;
    private boolean bRespawnProtected;
    private boolean bRequestingAllianceJoin;
    private boolean bAdmin;
    private boolean bBoss;
    private boolean bMuted;
    private boolean bChampion;
    private boolean bVeteran;
    private boolean bPrisoner;
    private boolean bMember;                            //The user has payed for membership.
    
    //Stats data.
    private short nWeeklyKills;
    private short nWeeklyDeaths;
    private int lOffenceSpending;
    private int lDefenceSpending;
    private int lDamageInflicted;
    private int lDamageReceived;
    private int lRank;
    private int lExperience;
    private int lTotalKills;
    private int lTotalDeaths;
    private int lDefenseValue;
    private int lOffenseValue;
    private int lNeutralValue;
    private int lWealthYesterday;
    private float fltDistanceTraveled;
    private int lCityCountLastWeek;
    private int lChampionCount;
    private long oWealth;
    private int lKOTHWins;
    
    //Server only.
    private User user = null;
    private long oJoinTime;
    private boolean bRadAlertSent = false;
    private int lWealthToday = 0;
    private float fltDistanceTraveledToday;
    
    //Lists of relevant information and entities. Necessary for optimization.
    private List<Integer> HostilePlayerIDs = new ArrayList<>();
    private List<Integer> OurMissiles = new ArrayList<>();
    private List<Integer> HostileMissiles = new ArrayList<>();
    private List<Structure> OurStructures = new ArrayList<>();
    private List<Integer> OurAircrafts = new ArrayList<>();
    private List<Integer> OurSAMSites = new ArrayList<>();
    private List<Integer> OurStoredAircrafts = new ArrayList<>();
    private List<Integer> NearbyAircrafts = new ArrayList<>();
    private List<Integer> OurBlueprints = new ArrayList<>();
    private List<Integer> OurWarehouses = new ArrayList<>();
    private List<Integer> OurShips = new ArrayList<>();
    private List<Integer> OurTanks = new ArrayList<>();
    
    //Server only. Exists to allow admins to give membership without the membership check in LaunchServerGame removing it automatically.
    public boolean bAdminMember = false;
    
    private LaunchGame game;
    
    //New player.
    public Player(int lID, String strName, int lAvatarID, int lWealth)
    {
        super(lID, new GeoCoord(), true, 0);
        this.strName = strName;
        this.lAvatarID = lAvatarID;
        this.dlyStateChange = new ShortDelay();
        this.lAllianceID = Alliance.ALLIANCE_ID_UNAFFILIATED;
        this.dlyAllianceCooloff = new ShortDelay();
        this.Blacklist = new ArrayList<>();
        bLeader = false;
        bAWOL = false;
        bRespawnProtected = true;
        bRequestingAllianceJoin = false;
        bAdmin = false;
        bBoss = false;
        bMuted = false;
        bMember = false;
        bChampion = false;
        bVeteran = false;
        bPrisoner = false;
        this.dlyAirdrop = new ShortDelay();
        this.dlyProspect = new ShortDelay();
        
        this.nWeeklyKills = 0;
        this.nWeeklyDeaths = 0;
        this.lOffenceSpending = 0;
        this.lDefenceSpending = 0;
        this.lDamageInflicted = 0;
        this.lDamageReceived = 0;
        this.lTotalKills = 0;
        this.lTotalDeaths = 0;
        this.fltDistanceTraveled = 0.0f;
        this.fltDistanceTraveledToday = 0.0f;
        this.lCityCountLastWeek = 0;
        this.lChampionCount = 0;
        this.lKOTHWins = 0;
        
        this.lRank = 0;
        this.lExperience = 0;
        
        this.lDefenseValue = 0;
        this.lOffenceSpending = 0;
        this.lNeutralValue = 0;
        
        this.oWealth = lWealth;
        
        this.bVisible = true;
        
        SetLastSeen();
    }
    
    //From save.
    public Player(int lID, GeoCoord geoPosition, String strName, int lAvatarID, long oLastSeen, int lStateChange, int lAllianceID, byte cFlags1, byte cFlags2, int lAllianceCooloff, short nKills, short nDeaths, int lOffenceSpending, int lDefenceSpending, int lDamageInflicted, int lDamageReceived, int lRank, int lExperience, int lTotalKills, int lTotalDeaths, boolean bMember, long oJoinTime, int lDefenseValue, int lOffenseValue, int lNeutralValue, float fltDistanceTraveled, float fltDistanceTraveledToday, int lAirdropCooldown, int lProspectCooldown, int lCityCountLastWeek, int lChampionCount, boolean bAdminMember, long oWealth, int lKOTHWins, List<Integer> Blacklist)
    {
        super(lID, geoPosition, true, 0);
        this.strName = strName;
        this.lAvatarID = lAvatarID;
        this.oLastSeen = oLastSeen;
        this.dlyStateChange = new ShortDelay(lStateChange);
        this.dlyAirdrop = new ShortDelay(lAirdropCooldown);
        this.dlyProspect = new ShortDelay(lProspectCooldown);
        this.lAllianceID = lAllianceID;
        this.dlyAllianceCooloff = new ShortDelay(lAllianceCooloff);
        this.Blacklist = Blacklist;
        bLeader = (cFlags1 & FLAG1_MP) != 0x00;
        bAWOL = (cFlags1 & FLAG1_AWOL) != 0x00;
        bPrisoner = (cFlags1 & FLAG1_PRISONER) != 0x00;
        bRespawnProtected = (cFlags1 & FLAG1_RSPAWN_PROT) != 0x00;
        bRequestingAllianceJoin = (cFlags2 & FLAG2_ALLIANCE_REQ_JOIN) != 0x00;
        bAdmin = (cFlags2 & FLAG2_ADMIN) != 0x00;
        bBoss = (cFlags2 & FLAG2_BOSS) != 0x00;
        bMuted = (cFlags2 & FLAG2_MUTED) != 0x00;
        bChampion = (cFlags2 & FLAG2_CHAMPION) != 0x00;
        bVeteran = (cFlags2 & FLAG2_VET) != 0x00;
        this.bMember = bMember;
        this.oJoinTime = oJoinTime;
        this.lDefenseValue = lDefenseValue;
        this.lOffenseValue = lOffenseValue;
        this.lNeutralValue = lNeutralValue;
        this.fltDistanceTraveled = fltDistanceTraveled;
        this.fltDistanceTraveledToday = fltDistanceTraveledToday;
        this.lCityCountLastWeek = lCityCountLastWeek;
        this.lChampionCount = lChampionCount;
        this.bAdminMember = bAdminMember;

        this.nWeeklyKills = nKills;
        this.nWeeklyDeaths = nDeaths;
        this.lOffenceSpending = lOffenceSpending;
        this.lDefenceSpending = lDefenceSpending;
        this.lDamageInflicted = lDamageInflicted;
        this.lDamageReceived = lDamageReceived;
        this.lRank = lRank;
        this.lExperience = lExperience;
        this.lTotalKills = lTotalKills;
        this.lTotalDeaths = lTotalDeaths;
        this.oWealth = oWealth;
        this.lKOTHWins = lKOTHWins;
        
        //For old config transfer only.
        if(lAllianceID == Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            SetIsAnMP(false);
        }
        
        this.bVisible = true;
    }
    
    public Player(ByteBuffer bb, int lReceivingID)
    {
        super(bb);
        strName = LaunchUtilities.StringFromData(bb);
        lAvatarID = bb.getInt();
        oLastSeen = bb.getLong();
        dlyStateChange = new ShortDelay(bb);
        dlyAirdrop = new ShortDelay(bb);
        dlyProspect = new ShortDelay(bb);
        lAllianceID = bb.getInt();
        byte cFlags1 = bb.get();
        byte cFlags2 = bb.get();
        dlyAllianceCooloff = new ShortDelay(bb);
        lRank = bb.getInt();
        lExperience = bb.getInt();
        bMember = (bb.get() != 0x00);
        lDefenseValue = bb.getInt();
        lOffenseValue = bb.getInt();
        lNeutralValue = bb.getInt();
        oJoinTime = bb.getLong();
        oWealth = bb.getLong();
        Blacklist = LaunchUtilities.IntListFromData(bb);

        bBanned = (cFlags1 & FLAG1_BANNED) != 0x00;
        bPrisoner = (cFlags1 & FLAG1_PRISONER) != 0x00;
        bLeader = (cFlags1 & FLAG1_MP) != 0x00;
        bAWOL = (cFlags1 & FLAG1_AWOL) != 0x00;
        bRespawnProtected = (cFlags1 & FLAG1_RSPAWN_PROT) != 0x00;
        bRequestingAllianceJoin = (cFlags2 & FLAG2_ALLIANCE_REQ_JOIN) != 0x00;
        bAdmin = (cFlags2 & FLAG2_ADMIN) != 0x00;
        bBoss = (cFlags2 & FLAG2_BOSS) != 0x00;
        bMuted = (cFlags2 & FLAG2_MUTED) != 0x00;
        bChampion = (cFlags2 & FLAG2_CHAMPION) != 0x00;
        bVeteran = (cFlags2 & FLAG2_VET) != 0x00;
        
        //If there are bytes remaining, this is a "full" image complete with stats.
        if(bb.hasRemaining())
        {
            nWeeklyKills = bb.getShort();
            nWeeklyDeaths = bb.getShort();
            lOffenceSpending = bb.getInt();
            lDefenceSpending = bb.getInt();
            lDamageInflicted = bb.getInt();
            lDamageReceived = bb.getInt();
            lTotalKills = bb.getInt();
            lTotalDeaths = bb.getInt();
            lWealthYesterday = bb.getInt();
            fltDistanceTraveled = bb.getFloat();
            lCityCountLastWeek = bb.getInt();
            lChampionCount = bb.getInt();
            lKOTHWins = bb.getInt();
            bHasFullStats = true;
        }
        else
            bHasFullStats = false;
    }
    
    public void SetUser(User user)
    {
        this.user = user;
    }
    
    public User GetUser() { return user; }

    @Override
    public void Tick(int lMS)
    {
        dlyStateChange.Tick(lMS);
        dlyAllianceCooloff.Tick(lMS);
        dlyAirdrop.Tick(lMS);
        dlyProspect.Tick(lMS);
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        GeoCoord geoToSend = new GeoCoord(0.0f, 0.0f);
        
        if(lAskingID == this.lID || (game != null && game.GetPlayer(lAskingID) != null && game.GetPlayer(lAskingID).GetIsAnAdmin()))
        {
            geoToSend = this.geoPosition;
        }
        
        byte[] cBaseData = super.GetData(lAskingID, geoToSend);
        byte[] cBlacklistData = LaunchUtilities.GetIntListData(Blacklist);
        
        ByteBuffer bb = ByteBuffer.allocate(cBaseData.length + DATA_SIZE + LaunchUtilities.GetStringDataSize(strName) + cBlacklistData.length);
        
        bb.put(cBaseData);
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.putInt(lAvatarID);
        bb.putLong(oLastSeen);
        dlyStateChange.GetData(bb);
        dlyAirdrop.GetData(bb);
        dlyProspect.GetData(bb);
        bb.putInt(lAllianceID);
        bb.put(GetFlags1());
        bb.put(GetFlags2());
        dlyAllianceCooloff.GetData(bb);
        bb.putInt(lRank);
        bb.putInt(lExperience);
        bb.put((byte)(bMember? 0xFF : 0x00));
        bb.putInt(lDefenseValue);
        bb.putInt(lOffenseValue);
        bb.putInt(lNeutralValue);
        bb.putLong(oJoinTime);
        bb.putLong(oWealth);
        bb.put(cBlacklistData);
        
        return bb.array();
    }
    
    public byte[] GetFullStatsData(int lAskingID)
    {
        byte[] cStandardData = GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(cStandardData.length + STATS_DATA_SIZE);
        bb.put(cStandardData);
        bb.putShort(nWeeklyKills);
        bb.putShort(nWeeklyDeaths);
        bb.putInt(lOffenceSpending);
        bb.putInt(lDefenceSpending);
        bb.putInt(lDamageInflicted);
        bb.putInt(lDamageReceived);
        bb.putInt(lTotalKills);
        bb.putInt(lTotalDeaths);
        bb.putInt(lWealthYesterday);
        bb.putFloat(fltDistanceTraveled);
        bb.putInt(lCityCountLastWeek);
        bb.putInt(lChampionCount);
        bb.putInt(lKOTHWins);
        
        return bb.array();
    }
    
    public String GetName() { return strName; }
    
    public int GetAvatarID() { return lAvatarID; }
    
    public void SetAvatarID(int lAvatarID)
    {
        this.lAvatarID = lAvatarID;
        Changed(false);
    }
    
    public long GetWealth()
    { 
        return oWealth; 
    }
    
    public long GetLastSeen() { return oLastSeen; }
    
    /**
     * Get the player's RAW alliance ID.
     * WARNING: For saving the game only. This doesn't imply whether they're a full or speculative member!
     * If you need the alliance the player is a full member of, use GetAllianceMemberID().
     * @return The player's alliance ID property to be written to a game save file.
     */
    public int GetAllianceIDForDataStorage()
    {
        return lAllianceID;
    }
    
    /**
     * Get the alliance the player is a full member of, or return ALLIANCE_ID_UNAFFILIATED if they aren't a full member of one.
     * @return The player's alliance, or ALLIANCE_ID_UNAFFILIATED if they aren't a full member of an alliance.
     */
    public int GetAllianceMemberID()
    {
        if(GetRequestingToJoinAlliance())
        {
            return Alliance.ALLIANCE_ID_UNAFFILIATED;
        }
        
        return lAllianceID;
    }
    
    /**
     * Get the alliance the player is attempting to join, or return ALLIANCE_ID_UNAFFILIATED if they aren't attempting to join one.
     * @return The alliance the player is attempting to join, or ALLIANCE_ID_UNAFFILIATED if they aren't attempting to join one.
     */
    public int GetAllianceJoiningID()
    {
        //Return the ID of the alliance the player is requesting to join. If they're already in one, the answer is "no alliance".
        if(!GetRequestingToJoinAlliance())
        {
            return Alliance.ALLIANCE_ID_UNAFFILIATED;
        }
        
        return lAllianceID;
    }
    
    public void SetAllianceID(int lAllianceID)
    {
        this.lAllianceID = lAllianceID;
        SetRequestingToJoinAlliance(false);
        Changed(false);
    }
    
    public void SetAllianceRequestToJoin(int lAllianceID)
    {
        this.lAllianceID = lAllianceID;
        SetRequestingToJoinAlliance(true);
        Changed(false);
    }
    
    public void RejectAllianceRequestToJoin()
    {
        lAllianceID = Alliance.ALLIANCE_ID_UNAFFILIATED;
        SetRequestingToJoinAlliance(false);
        Changed(false);
    }
    
    public void SetAllianceCooloffTime(int lAllianceCooloff)
    {
        dlyAllianceCooloff.Set(lAllianceCooloff);
    }
    
    public boolean GetAllianceCooloffExpired() { return dlyAllianceCooloff.Expired(); }
    
    public int GetAllianceCooloffRemaining() { return dlyAllianceCooloff.GetRemaining(); }
    
    public short GetKills() { return nWeeklyKills; }
    
    public short GetDeaths() { return nWeeklyDeaths; }
    
    public int GetOffenceSpending() { return lOffenceSpending; }
    
    public int GetDefenceSpending() { return lDefenceSpending; }
    
    public int GetDamageInflicted() { return lDamageInflicted; }
    
    public int GetDamageReceived() { return lDamageReceived; }
    
    public int GetStateTimeRemaining() { return dlyStateChange.GetRemaining(); }
    
    public boolean GetStateTimeExpired() { return dlyStateChange.Expired(); }

    public byte GetFlags1()
    {
        bBanned = GetBanned_Server();
        
        byte cFlags1 = 0x00;
        cFlags1 |= bBanned ? FLAG1_BANNED : 0x00;
        cFlags1 |= bPrisoner ? FLAG1_PRISONER : 0x00;
        cFlags1 |= bLeader ? FLAG1_MP : 0x00;
        cFlags1 |= bAWOL ? FLAG1_AWOL : 0x00;
        cFlags1 |= bRespawnProtected ? FLAG1_RSPAWN_PROT : 0x00;
        return cFlags1;
    }
    
    public byte GetFlags2()
    {
        byte cFlags2 = 0x00;
        cFlags2 |= bRequestingAllianceJoin ? FLAG2_ALLIANCE_REQ_JOIN : 0x00;
        cFlags2 |= bAdmin ? FLAG2_ADMIN : 0x00;
        cFlags2 |= bBoss ? FLAG2_BOSS : 0x00;
        cFlags2 |= bMuted ? FLAG2_MUTED : 0x00;
        cFlags2 |= bChampion ? FLAG2_CHAMPION : 0x00;
        cFlags2 |= bVeteran ? FLAG2_VET : 0x00;
        return cFlags2;
    }
    
    public void SetLastSeen()
    {
        oLastSeen = System.currentTimeMillis();
    }
    
    public boolean GetAWOL()
    {
        return bAWOL;
    }
    
    public void SetAWOL(boolean bAWOL)
    {
        this.bAWOL = bAWOL;
        Changed(false);
    }
    
    public boolean GetRespawnProtected()
    {
        return bRespawnProtected;
    }
    
    public boolean GetRequestingToJoinAlliance()
    {
        return bRequestingAllianceJoin;
    }
    
    public boolean GetIsAnMP()
    {
        return bLeader;
    }
    
    public void SetRespawnProtected(boolean bProtected)
    {
        this.bRespawnProtected = bProtected;
    }
    
    public void SetRequestingToJoinAlliance(boolean bRequestingToJoin)
    {
        this.bRequestingAllianceJoin = bRequestingToJoin;
        Changed(false);
    }
    
    public void SetCompassionateInvulnerability(int lProtectionTime)
    {
        dlyStateChange.Set(lProtectionTime);
        SetRespawnProtected(true);
        Changed(false);
    }
    
    /** Artificially advance the player's "last seen" time by the specified amount.
     * @param oParkTime Time to advance the player's "last seen" time by, in ms. */
    public void Park(long oParkTime)
    {
        oLastSeen += oParkTime;
        Changed(false);
    }
    
    public void SetIsAnMP(boolean bIsAnMP)
    {
        this.bLeader = bIsAnMP;
        Changed(false);
    }
    
    public long ChargeWealth(long oWealth)
    {
        if(oWealth > this.oWealth)
        {
            this.oWealth = 0;
            return 0;
        }
        else
        {
            this.oWealth -= oWealth;
            
            return oWealth;
        }
    }
    
    public boolean SubtractWealth(long oWealth)
    {
        if(oWealth >= 0)
        {
            if(oWealth <= this.oWealth)
            {
                this.oWealth -= oWealth;
                return true;
            }
        }
        
        return false;
    }
    
    public void AddWealth(long oWealth)
    {
        this.oWealth += oWealth;
        Changed(false);
    }
    
    public boolean IsRespawnProtected()
    {
        return bRespawnProtected;
    }
    
    public boolean Functioning()
    {
        //Can be hit or detected. Immediately influential on the game.
        return !GetAWOL();
    }

    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(true);
    }
    
    public boolean GetIsAnAdmin()
    {
        return bAdmin;
    }
    
    public boolean GetBoss()
    {
        return bBoss;
    }
    
    public boolean Muted()
    {
        return bMuted;
    }
    
    public void SetIsAnAdmin(boolean bIsAdmin)
    {
        this.bAdmin = bIsAdmin;
        Changed(false);
    }
    
    public void SetBoss(boolean bIsBoss)
    {
        this.bBoss = bIsBoss;
        Changed(false);
    }
    
    public void ChangeName(String strNewName)
    {
        strName = strNewName;
        Changed(false);
    }
    
    /**
     * Increment the number of kills this player has achieved.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     */
    public void IncrementKills()
    {
        nWeeklyKills++;
        lTotalKills++;
    }
    
    public float GetDistanceTraveled()
    {
        return this.fltDistanceTraveled;
    }
    
    public float GetDistanceTraveledToday()
    {
        return this.fltDistanceTraveledToday;
    }
    
    public void ResetTravelToday()
    {
        this.fltDistanceTraveledToday = 0.0f;
    }
    
    public void Traveled(float fltDistanceInKM)
    {
        this.fltDistanceTraveled += fltDistanceInKM;
        this.fltDistanceTraveledToday += fltDistanceInKM;
    }
    
    /**
     * Add some offence spending by the player.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     * @param lAmount The amount spent.
     */
    public void AddOffenceSpending(int lAmount)
    {
        lOffenceSpending += lAmount;
    }
    
    /**
     * Add some defence spending by the player.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     * @param lAmount The amount spent.
     */
    public void AddDefenceSpending(int lAmount)
    {
        lDefenceSpending += lAmount;
    }
    
    /**
     * Add some damage to what the player has inflicted on others.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     * @param nDamage HPs of damage inflicted.
     */
    public void AddDamageInflicted(short nDamage)
    {
        lDamageInflicted += nDamage;
    }
    
    /**
     * Add some damage to what the player has received.
     * Don't call this from within this class; it includes damage to stuff owned by the player and must be driven externally to this component.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     * @param nDamage HPs of damage inflicted.
     */
    public void AddDamageReceived(short nDamage)
    {
        lDamageReceived += nDamage;
    }
    
    /**
     * Reset the player's stats, i.e. at the end of the week.
     * Don't call Changed(); This forms part of statistics and isn't automatically communicated.
     */
    public void ResetStats()
    {
        nWeeklyKills = 0;
        nWeeklyDeaths = 0;
        lOffenceSpending = 0;
        lDefenceSpending = 0;
        lDamageInflicted = 0;
        lDamageReceived = 0;
    }

    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == this.lID;
    }

    @Override
    public boolean ApparentlyEquals(LaunchEntity entity)
    {
        if(entity instanceof Player)
            return entity.GetID() == lID;
        return false;
    }
    
    /**
     * SERVER ONLY. Get status of the player being banned, to decide if their stuff should be sent to other players.
     * @return True if the player is banned, and should be effectively excluded from the game.
     */
    public boolean GetBanned_Server()
    {
        if(user != null)
            return user.GetBanState() != User.BanState.NOT;
        
        //Fail deadly.
        return true;
    }
    
    /**
     * CLIENT ONLY. Get status of the player being banned, to decide if they should not be rendered.
     * @return True if the player's banned flag is enabled.
     */
    public boolean GetBanned_Client()
    {
        return bBanned;
    }
    
    /**
     * Copy stats to this player. This allows full stats to be maintained in UI elements while players are online (and thus would otherwise overwrite the stats with zeroes).
     * @param statsCopy The old player reference to copy the stats from to the new, otherwise statless player reference.
     */
    public void StatsCopy(Player statsCopy)
    {
        this.nWeeklyKills = statsCopy.nWeeklyKills;
        this.nWeeklyDeaths = statsCopy.nWeeklyDeaths;
        this.lDamageInflicted = statsCopy.lDamageInflicted;
        this.lOffenceSpending = statsCopy.lOffenceSpending;
        this.lDamageReceived = statsCopy.lDamageReceived;
        this.lDefenceSpending = statsCopy.lDefenceSpending;
        this.lRank = statsCopy.lRank;
        this.lExperience = statsCopy.lExperience;
        this.lTotalKills = statsCopy.lTotalKills;
        this.lTotalDeaths = statsCopy.lTotalDeaths;
    }
    
    public void RankUp()
    {
        this.lRank++;
        Changed(false);
    }
    
    public void RankDown()
    {
        this.lRank--;
        Changed(false);
    }
    
    public void AddExperience(int lEXP)
    {
        this.lExperience += lEXP;
        Changed(false);
    }
    
    public void SubtractExperience(int lEXP)
    {
        this.lExperience = Math.max(0, lExperience - lEXP);
        Changed(false);
    }
    
    public int GetRank()
    {
        return this.lRank;
    }
    
    public int GetExperience()
    {
        return this.lExperience;
    }
    
    public int GetTotalKills()
    {
        return this.lTotalKills;
    }
    
    public int GetTotalDeaths()
    {
        return this.lTotalDeaths;
    }    
    
    public float GetKDR()
    {
        if(Float.valueOf(lTotalKills) > 0 && Float.valueOf(lTotalDeaths) > 0)
        {
            return Float.valueOf(lTotalKills)/Float.valueOf(lTotalDeaths);
        }
        if(Float.valueOf(lTotalKills) > 0 && Float.valueOf(lTotalKills) == 0)
        {
            return Float.valueOf(lTotalKills);
        }
        
        return 0;
    }
    
    public void SetJoinTime(long oJoinTime)
    {
        this.oJoinTime = oJoinTime;
    }
    
    public long GetJoinTime()
    {
        return this.oJoinTime;
    }
    
    public int GetOffenseValue()
    {
        return this.lOffenseValue;
    }
    
    public int GetDefenseValue()
    {
        return this.lDefenseValue;
    }
    
    public void SetOffenseValue(int newValue)
    {
        this.lOffenseValue = newValue;
        Changed(false);
    }
    
    public void SetDefenseValue(int newValue)
    {
        this.lDefenseValue = newValue;
        Changed(false);
    }
    
    public int GetNeutralValue()
    {
        return this.lNeutralValue;
    }
    
    public void SetNeutralValue(int newValue)
    {
        this.lNeutralValue = newValue;
    }
    
    public int GetTotalValue()
    {
        return GetNeutralValue() + GetOffenseValue() + GetDefenseValue();
    }
    
    public void AddHostilePlayer(int lPlayerID)
    {
        if(!HostilePlayerIDs.contains(lPlayerID))
            HostilePlayerIDs.add(lPlayerID);
        
        if(!Blacklisted(lPlayerID))
            Blacklist(lPlayerID);
    }
    
    public void RemoveHostilePlayer(int lPlayerID)
    {
        if(HostilePlayerIDs.contains(lPlayerID))
            HostilePlayerIDs.remove(HostilePlayerIDs.indexOf(lPlayerID));
    }
    
    public void ClearHostilePlayers()
    {
        HostilePlayerIDs.clear();
    }
    
    public boolean PlayerIsHostile(Player player)
    {
        if(player.GetID() != this.lID)
        {
            return HostilePlayerIDs.contains(player.GetID());
        }
        
        return false;
    }
    
    public boolean HasFlyingMissiles()
    {
        return !OurMissiles.isEmpty();
    }
    
    public List<Integer> GetMissiles()
    {
        return OurMissiles;
    }
    
    public void AddOwnedEntity(LaunchEntity entity)
    {
        try
        {
            if(entity instanceof Blueprint && !OurBlueprints.contains(entity.GetID()))
                OurBlueprints.add(entity.GetID());

            if(entity instanceof Missile && !OurMissiles.contains(entity.GetID()))
                OurMissiles.add(entity.GetID());

            if(entity instanceof Structure && !OurStructures.contains(((Structure)entity)))
                OurStructures.add(((Structure)entity));

            if(entity instanceof Airplane && !OurAircrafts.contains(entity.GetID()))
                OurAircrafts.add(entity.GetID());

            if(entity instanceof StoredAirplane && !OurStoredAircrafts.contains(entity.GetID()))
                OurStoredAircrafts.add(entity.GetID());

            if(entity instanceof SAMSite && !OurSAMSites.contains(entity.GetID()))
                OurSAMSites.add(entity.GetID());

            if(entity instanceof Warehouse && !OurWarehouses.contains(entity.GetID()))
                OurWarehouses.add(entity.GetID());
            
            if(entity instanceof Ship && !OurShips.contains(entity.GetID()))
                OurShips.add(entity.GetID());
            
            if(entity instanceof Tank && !OurTanks.contains(entity.GetID()))
                OurTanks.add(entity.GetID());
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
            LaunchLog.ConsoleMessage(ex.getMessage());
        }  
    }
    
    public void RemoveBlueprint(int lBlueprintID)
    {
        if(OurBlueprints.contains(lBlueprintID))
            OurBlueprints.remove(OurBlueprints.indexOf(lBlueprintID));
    }
    
    public void RemoveMissile(int lMissileID)
    {
        if(OurMissiles.contains(lMissileID))
            OurMissiles.remove(OurMissiles.indexOf(lMissileID));
    }
    
    public void RemoveStructure(Structure structure)
    {
        if(OurStructures.contains(structure))
            OurStructures.remove(structure);
    }
    
    public void RemoveAircraft(int lAircraftID)
    {
        if(OurAircrafts.contains(lAircraftID))
            OurAircrafts.remove(OurAircrafts.indexOf(lAircraftID));
    }
    
    public void RemoveStoredAircraft(int lStoredAircraftID)
    {
        if(OurStoredAircrafts.contains(lStoredAircraftID))
            OurStoredAircrafts.remove(OurStoredAircrafts.indexOf(lStoredAircraftID));
    }
    
    public void RemoveSAMSite(int lSAMSiteID)
    {
        if(OurSAMSites.contains(lSAMSiteID))
            OurSAMSites.remove(OurSAMSites.indexOf(lSAMSiteID));
    }
    
    public void RemoveWarehouse(int lWarehouseID)
    {
        if(OurWarehouses.contains(lWarehouseID))
            OurWarehouses.remove(OurWarehouses.indexOf(lWarehouseID));
    }
    
    public void RemoveShip(int lShipID)
    {
        if(OurShips.contains(lShipID))
            OurShips.remove(OurShips.indexOf(lShipID));
    }
    
    public void RemoveTank(int lTankID)
    {
        if(OurTanks.contains(lTankID))
            OurTanks.remove(OurTanks.indexOf(lTankID));
    }
    
    public void AddHostileMissile(int lMissileID)
    {
        synchronized(this)
        {
            if(!HostileMissiles.contains(lMissileID))
                HostileMissiles.add(lMissileID);
        }
    }
    
    public void RemoveHostileMissile(int lMissileID)
    {
        if(HostileMissiles.contains(lMissileID))
            HostileMissiles.remove(HostileMissiles.indexOf(lMissileID));
    }
    
    public List<Integer> GetHostileMissiles()
    {
        return HostileMissiles;
    }
    
    public boolean UnderAttack()
    {
        return !HostileMissiles.isEmpty() || !HostilePlayerIDs.isEmpty();
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------
    // Player's Structure Methods.
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public boolean HasStructures()
    {
        return !OurStructures.isEmpty();
    }
    
    public List<Structure> GetStructures()
    {
        return OurStructures;
    }
    
    public List<Integer> GetBlueprints()
    {
        return OurBlueprints;
    }
    
    public boolean HasAircraft()
    {
        return !OurAircrafts.isEmpty();
    }
    
    public List<Integer> GetAircrafts()
    {
        return OurAircrafts;
    }
    
    public boolean HasStoredAircraft()
    {
        return !OurStoredAircrafts.isEmpty();
    }
    
    public List<Integer> GetStoredAircrafts()
    {
        return OurStoredAircrafts;
    }
    
    public void AddSAMSite(int lSAMSiteID)
    {
        if(!OurSAMSites.contains(lSAMSiteID))
            OurSAMSites.add(lSAMSiteID);
    }
    
    public void AddWarehouse(int lWarehouseID)
    {
        if(!OurWarehouses.contains(lWarehouseID))
            OurWarehouses.add(lWarehouseID);
    }
    
    public void AddShip(int lShipID)
    {
        if(!OurShips.contains(lShipID))
            OurShips.add(lShipID);
    }
    
    public void AddTank(int lTankID)
    {
        if(!OurTanks.contains(lTankID))
            OurTanks.add(lTankID);
    }
    
    public List<Integer> GetWarehouses()
    {
        return OurWarehouses;
    }
    
    public List<Integer> GetSAMSites()
    {
        return OurSAMSites;
    }
    
    public List<Integer> GetShips()
    {
        return OurShips;
    }
    
    public List<Integer> GetTanks()
    {
        return OurTanks;
    }
    
    public void AddNearbyAircraft(int lAircraftID)
    {
        if(!NearbyAircrafts.contains(lAircraftID))
            NearbyAircrafts.add(lAircraftID);
    }
    
    public void RemoveNearbyAircraft(int lAircraftID)
    {
        if(NearbyAircrafts.contains(lAircraftID))
            NearbyAircrafts.remove(NearbyAircrafts.indexOf(lAircraftID));
    }
    
    public List<Integer> GetNearbyAircrafts()
    {
        return NearbyAircrafts;
    }
    
    public boolean AircraftNearby()
    {
        return !NearbyAircrafts.isEmpty();
    }
    
    public void RadAlertSent()
    {
        this.bRadAlertSent = true;
    }
    
    public void ResetRadAlert()
    {
        this.bRadAlertSent = false;
    }
    
    public boolean GetRadAlertSent()
    {
        return this.bRadAlertSent;
    }
    
    public void AddDailyWealth(int lAmount)
    {
        this.lWealthToday += lAmount;
    }
    
    public int GetWealthToday()
    {
        return this.lWealthToday;
    }
    
    public void SetWealthYesterday()
    {
        this.lWealthYesterday = lWealthToday;
        this.lWealthToday = 0;
        Changed(true);
    }
    
    public int GetWealthYesterday()
    {
        return lWealthYesterday;
    }
    
    @Override
    public EntityType GetEntityType()
    {
        return EntityType.PLAYER;
    }
    
    @Override
    public int GetSessionCode()
    {
        return LaunchSession.Player;
    }
    
    @Override
    public int GetOwnerID()
    {
        return lID;
    }
    
    @Override
    public String GetTypeName()
    {
        return "player";
    }
    
    public boolean IsAMember()
    {
        return this.bMember;
    }
    
    public void SetMemberStatus(boolean bMember)
    {
        this.bMember = bMember;
    }
    
    public boolean GetCanCallAirdrop()
    {
        return dlyAirdrop.Expired();
    }
    
    public boolean GetCanProspect()
    {
        return dlyProspect.Expired();
    }
    
    public int GetAirdropCooldownRemaining()
    {
        return dlyAirdrop.GetRemaining();
    }
    
    public int GetProspectCooldownRemaining()
    {
        return dlyProspect.GetRemaining();
    }
    
    public void CalledAirdrop(int lCooldown)
    {
        dlyAirdrop.Set(lCooldown);
    }
    
    public void Prospected(int lCooldown)
    {
        dlyProspect.Set(lCooldown);
    }
    
    public boolean GetPlayerIsNoob()
    {
        return false;
    }
    
    public void SetCityCountLastWeek(int lCount)
    {
        this.lCityCountLastWeek = lCount;
    }
    
    public int GetCityCountLastWeek()
    {
        return this.lCityCountLastWeek;
    }
    
    public boolean GetChampion()
    {
        return this.bChampion;
    }
    
    public void SetChampion(boolean bChampion)
    {
        if(bChampion)
            this.lChampionCount++;
        
        this.bChampion = bChampion;
    }
    
    public int GetChampionCount()
    {
        return this.lChampionCount;
    }
    
    public boolean GetVeteran()
    {
        return this.bVeteran;
    }
    
    public void SetVeteran(boolean bVeteran)
    {
        this.bVeteran = bVeteran;
    }
    
    public void SetAdminMember(boolean bAdminMember)
    {
        this.bAdminMember = bMember;
    }
    
    public boolean GetAdminMember()
    {
        return this.bAdminMember;
    }
    
    public boolean Blacklisted(int lPlayerID)
    {
        return Blacklist.contains(lPlayerID);
    }
    
    public void Blacklist(int lPlayerID)
    {
        if(Blacklist.contains(lPlayerID))
        {
            Whitelist(lPlayerID);
        }
        else
        {
            Blacklist.add(lPlayerID);
        }
    }
    
    public void Whitelist(int lPlayerID)
    {
        if(Blacklist.contains(lPlayerID))
            Blacklist.remove(Blacklist.indexOf(lPlayerID));
    }
    
    public List<Integer> GetBlacklist()
    {
        return new ArrayList<>(Blacklist);
    }
    
    public boolean GetPrisoner()
    {
        return bPrisoner;
    }
    
    public void SetPrisoner(boolean bPrisoner)
    {
        this.bPrisoner = bPrisoner;
    }
    
    public void SetGame(LaunchGame game)
    {
        this.game = game;
    }
    
    public int GetKOTHWins()
    {
        return this.lKOTHWins;
    }
    
    public void WonKOTH()
    {
        this.lKOTHWins++;
    }
}
