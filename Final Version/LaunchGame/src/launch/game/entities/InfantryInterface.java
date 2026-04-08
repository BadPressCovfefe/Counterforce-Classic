/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

/**
 *
 * @author Corbin
 */
public interface InfantryInterface 
{
    int GetID();
    short GetHP();
    short GetMaxHP();
    short InflictDamage(short nDamage);
    void FullyRepair();
    int GetOwnerID();
    String GetName();
    void SetName(String strName);
    LaunchEntity GetInfantry();
    boolean Deployed();
    boolean GetHasFullStats();
    boolean AtFullHealth();
    void AddHP(short nHP);
    byte[] GetData(int lAskingID);
    boolean GetOwnedBy(int lPlayerID);
    int GetSessionCode();
}
