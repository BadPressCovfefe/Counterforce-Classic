/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.CargoSystem;


/**
 *
 * @author Corbin
 */
public interface CargoTruckInterface
{
    int GetID();
    int GetOwnerID();
    LaunchEntity GetCargoTruck();
    CargoSystem GetCargoSystem();
    String GetName();
    String GetTypeName();
    void SetName(String strName);
    boolean AtFullHealth();
    short GetHP();
    short GetMaxHP();
    short InflictDamage(short nHP);
    void AddHP(short nHP);
    byte[] GetData(int lAskingID);
    boolean GetOwnedBy(int lPlayerID);
    int GetSessionCode();
}
