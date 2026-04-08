/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.EntityPointer.EntityType;
import launch.game.systems.MissileSystem;


/**
 *
 * @author Corbin
 */
public interface TankInterface
{
    int GetID();
    int GetOwnerID();
    LaunchEntity GetTank();
    MissileSystem GetMissileSystem();
    String GetName();
    void SetName(String strName);
    boolean IsAnMBT();
    boolean IsASPAAG();
    String GetTypeName();
    short InflictDamage(short nDamage);
    short GetMaxHP();
    boolean AtFullHealth();
    void AddHP(short nHP);
    byte[] GetData(int lAskingID);
    boolean GetOwnedBy(int lPlayerID);
    int GetSessionCode();
    boolean IsMissiles();
    boolean IsInterceptors();
    boolean IsArtillery();
    boolean HasMissiles();
    boolean HasInterceptors();
    boolean HasArtillery();
    EntityType GetType();
    boolean GetAuto();
    boolean GetSemiAuto();
    boolean GetManual();
    byte GetMode();
    void SetMode(byte cMode);
}
