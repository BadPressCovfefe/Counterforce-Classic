/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game.entities;

/**
 *
 * @author Corbin
 */
public class AllianceScore 
{
    //time lasted
    //Most kills
    //Wars won
    private static final int DATA_SIZE = 28; //Plus the string data length of strAllianceName.
    
    private int lID;
    private long oCreatedOn;
    private long oEndedOn;
    private int lKills;
    private int lWarsWon;
    private String strAllianceName;
    
    public AllianceScore(int lID, long oCreatedOn, long oEndedOn, int lKills, int lWarsWon, String strAllianceName)
    {
        this.lID = lID;
        this.oCreatedOn = oCreatedOn;
        this.oEndedOn = oEndedOn;
        this.lKills = lKills;
        this.lWarsWon = lWarsWon;
        this.strAllianceName = strAllianceName;
    }
    
    public int GetID()
    {
        return lID;
    }
    
    public long GetCreatedOn()
    {
        return oCreatedOn;
    }
    
    public int GetKills()
    {
        return lKills;
    }
    
    public int GetWarsWon()
    {
        return lWarsWon;
    }
    
    public String GetAllianceName()
    {
        return strAllianceName;
    }
    
    public long GetEndedOn()
    {
        return oEndedOn;
    }
}
