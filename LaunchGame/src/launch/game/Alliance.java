/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import launch.game.entities.MapEntity;
import launch.utilities.LaunchUtilities;
import launch.game.entities.LaunchEntityListener;
import launch.game.treaties.Treaty;

/**
 *
 * @author tobster
 */
public class Alliance
{
    private static final int DATA_SIZE = 48;
    
    public static final int ALLIANCE_ID_UNAFFILIATED = Defs.THE_GREAT_BIG_NOTHING;
    public static final int ALLIANCE_AVATAR_DEFAULT = Defs.THE_GREAT_BIG_NOTHING;
    public static final int ALLIANCE_MAX_DESCRIPTION_CHARS = 140;
    
    private LaunchEntityListener listener = null;
    
    private int lID;
    private String strName;
    private String strDescription;
    private int lAvatarID;
    private int lWealth;
    private float fltTaxRate;
    private int lWarsWon;
    private int lWarsLost;
    private int lEnemyAllianceDisbands;
    private String strFounderName;
    private int lAffiliationsBroken;
    private int lICBMCount;
    private int lABMCount;
    private long oFoundedTime;
    
    private List<Integer> OurTreaties = new ArrayList<>();
    
    //New.
    public Alliance(int lID, String strName, String strDescription, int lAvatarID, String strFounderName)
    {
        this.lID = lID;
        this.strName = strName;
        this.strDescription = strDescription;
        this.lAvatarID = lAvatarID;
        this.lWealth = 0;
        this.fltTaxRate = Defs.DEFAULT_TAX_RATE;
        this.lWarsWon = 0;
        this.lWarsLost = 0;
        this.lEnemyAllianceDisbands = 0;
        this.strFounderName = strFounderName;
        this.lAffiliationsBroken = 0;
        this.lICBMCount = 0;
        this.lABMCount = 0;
        this.oFoundedTime = System.currentTimeMillis();
    }
    
    //From save.
    public Alliance(int lID, String strName, String strDescription, int lAvatarID, int lWealth, float fltTaxRate, int lWarsWon, int lWarsLost, int lEnemyAllianceDisbands, String strFounderName, int lAffiliationsBroken, int lICBMCount, int lABMCount, long oFoundedTime)
    {
        this.lID = lID;
        this.strName = strName;
        this.strDescription = strDescription;
        this.lAvatarID = lAvatarID;
        this.lWealth = lWealth;
        this.fltTaxRate = fltTaxRate;
        
        this.lWarsWon = lWarsWon;
        this.lWarsLost = lWarsLost;
        this.lEnemyAllianceDisbands = lEnemyAllianceDisbands;
        this.strFounderName = strFounderName;
        this.lAffiliationsBroken = lAffiliationsBroken;
        this.lICBMCount = lICBMCount;
        this.lABMCount = lABMCount;
        this.oFoundedTime = oFoundedTime;
    }
    
    //Communicated.
    public Alliance(ByteBuffer bb)
    {
        lID = bb.getInt();
        strName = LaunchUtilities.StringFromData(bb);
        strDescription = LaunchUtilities.StringFromData(bb);
        lAvatarID = bb.getInt();
        lWealth = bb.getInt();
        fltTaxRate = bb.getFloat();
        
        lWarsWon = bb.getInt();
        lWarsLost = bb.getInt();
        lEnemyAllianceDisbands = bb.getInt();
        strFounderName = LaunchUtilities.StringFromData(bb);
        lAffiliationsBroken = bb.getInt();
        lICBMCount = bb.getInt();
        lABMCount = bb.getInt();
        oFoundedTime = bb.getLong();
    }
    
    public void SetAvatarID(int lAvatarID)
    {
        this.lAvatarID = lAvatarID;
        Changed();
    }
    
    public int GetID() { return lID; }
    
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed();
    }
    
    public String GetName() { return strName; }
    
    public void SetDescription(String strDescription)
    {
        this.strDescription = strDescription;
        Changed();
    }
    
    public String GetDescription() { return strDescription; }
    
    public int GetAvatarID() { return lAvatarID; }
    
    public void SetListener(LaunchEntityListener listener) { this.listener = listener; }
    
    private void Changed()
    {
        if(listener != null)
        {
            listener.EntityChanged(this);
        }
    }
    
    public byte[] GetData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + LaunchUtilities.GetStringDataSize(strName) + LaunchUtilities.GetStringDataSize(strDescription) + LaunchUtilities.GetStringDataSize(strFounderName));
        
        bb.putInt(lID);
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.put(LaunchUtilities.GetStringData(strDescription));
        bb.putInt(lAvatarID);
        bb.putInt(lWealth);
        bb.putFloat(fltTaxRate);
        bb.putInt(lWarsWon);
        bb.putInt(lWarsLost);
        bb.putInt(lEnemyAllianceDisbands);
        bb.put(LaunchUtilities.GetStringData(strFounderName));
        bb.putInt(lAffiliationsBroken);
        bb.putInt(lICBMCount);
        bb.putInt(lABMCount);
        bb.putLong(oFoundedTime);
        
        return bb.array();
    }
    
    public List<Integer> GetTreaties()
    {
        return OurTreaties;
    }
    
    public void AddTreaty(int lTreatyID)
    {
        if(!OurTreaties.contains(lTreatyID))
            OurTreaties.add(lTreatyID);
    }
    
    public void RemoveTreaty(int lTreatyID)
    {
        if(OurTreaties.contains(lTreatyID))
            OurTreaties.remove(OurTreaties.indexOf(lTreatyID));
    }
    
    public void AddWealth(int lAmount)
    {
        this.lWealth += lAmount;
        Changed();
    }
    
    public boolean SubtractWealth(int lWealth)
    {
        if(this.lWealth >= lWealth)
        {
            this.lWealth -= lWealth;
            Changed();
            return true;
        }
        
        return false;
    }
    
    public void SetWealth(int lWealth)
    {
        this.lWealth = lWealth;
        Changed();
    }
    
    public void SetTaxRate(float fltRate)
    {
        this.fltTaxRate = fltRate;
        Changed();
    }
    
    public int GetWealth()
    {
        return this.lWealth;
    }
    
    public float GetTaxRate() { return fltTaxRate; }
    
    public void WarWon()
    {
        this.lWarsWon++;
    }
    
    public int GetWarsWon()
    {
        return lWarsWon;
    }
    
    public void WarLost()
    {
        this.lWarsLost++;
    }
    
    public int GetWarsLost()
    {
        return lWarsLost;
    }
    
    public void AddEnemyAllianceDisband()
    {
        this.lEnemyAllianceDisbands++;
    }
    
    public int GetEnemyAllianceDisbands()
    {
        return lEnemyAllianceDisbands;
    }
    
    public String GetFounder()
    {
        return strFounderName;
    }
    
    public void AffiliationBroken()
    {
        this.lAffiliationsBroken++;
    }
    
    public int GetAffiliationsBroken()
    {
        return lAffiliationsBroken;
    }
    
    public void SetICBMCount(int lCount)
    {
        this.lICBMCount = lCount;
    }
    
    public int GetICBMCount()
    {
        return this.lICBMCount;
    }
    
    public void SetABMCount(int lCount)
    {
        this.lABMCount = lCount;
    }
    
    public int GetABMCount()
    {
        return this.lABMCount;
    }
    
    public long GetFoundedTime()
    {
        return this.oFoundedTime;
    }
    
    /**
     * This method is for the periodic re-calculation of derivative stats.
     * It should not be modified to reset constantly-tracked stats. For example, 
     * it should reset resource counts and ship counts, but not war wins.
     */
    public void ResetTrackableStats()
    {
        
    }
}
