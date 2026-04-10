/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.entities.conceptuals.Resource.ResourceType;

/**
 *
 * @author Corbin
 */
public class AllianceRecord //TODO: Needs to implement AchievementListener.
{
    /**
     * This class tracks all server stats and accomplishments/achievements.
     * This data should update once per day, and prior to each update it should 
     * print a file with the current data into a folder for that day.
     * Achievement tracking will need an AchievementListener shared by the LaunchServerGame.
     */
    
    //Server-wide stats
    private Map<ResourceType, Long> TotalResources = new ConcurrentHashMap<>();
    //private List<AllianceRecord> AllianceRecords = new ArrayList<>();
    private long oTotalMissiles;
    private long oTotalInterceptors;
    private long oTotalTorpedoes;
    private long oTotalNukes;                                                   //Does not count ICBMs or nuclear interceptors.
    private long oTotalICBMs;
    private long oTotalAircraft;
    private long oTotalTrucks;
    private long oTotalShips;
    private long oTotalShipsTonnage;
    private long oTotalInfantry;
    private long oTotalStructures;
    private long oNukesDetonated;                                               //All nukes ever detonated.
    private long oMissilesLaunched;
    private long oInterceptorsLaunched;
    
    //Players
    private int lHighestMoneyPlayerID;                                          //The ID of the player with the most money.
    private int lHighestOffensePlayerID;
    private int lHighestDefensePlayerID;
    private int lMostKillsPlayerID;
    private int lMostStructuresPlayerID;
    private int lMostICBMsPlayerID;
    private int lMostMissilesPlayerID;
    private int lMostInterceptorsPlayerID;
    private int lMostAircraftPlayerID;
    private int lMostShipsPlayerID;
    private int lMostInfantryPlayerID;
    private int lMostTanksPlayerID;
    private int lMostShipTonnagePlayerID;
    private int lMostTrucksPlayerID;
    
    //Alliances
    private int lWealthiestAllianceID;
    private int lBiggestAllianceID;
    private int lOffensiveAllianceID;                                           //ID of the alliance with the most offense.
    private int lDefensiveAllianceID;                                           //ID of the alliance with the most defense.
    private int lMostKillsAllianceID;
    private int lMostWarWinsAllianceID;
    private int lMostFaithfulAllianceID;                                        //The alliance that has broken the least number of affiliations.
    private int lMostStructuresAllianceID;
    private int lMostICBMsAllianceID;
    private int lMostMissilesAllianceID;
    private int lMostInterceptorsAllianceID;
    private int lMostAircraftAllianceID;
    private int lMostShipsAllianceID;
    private int lMostInfantryAllianceID;
    private int lMostTanksAllianceID;
    private int lMostShipTonnageAllianceID;
    private int lMostTrucksAllianceID;
    
    private LaunchServerGame game;
    
    //Achievements TODO
    
    public AllianceRecord(LaunchServerGame game)
    {
        this.game = game;
    }
    
    
    
}
