/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.LaunchEntity;
import launch.game.systems.CargoSystem;
import launch.game.systems.MissileSystem;

/**
 *
 * @author Corbin
 */
public interface AirplaneInterface extends FuelableInterface
{    
    int GetID();
    int GetOwnerID();
    short GetHP();
    short GetMaxHP();
    short InflictDamage(short nDamage);
    boolean GetCarrierCompliant();
    boolean GetStealth();
    boolean HasCannon();
    boolean HasScanner();
    boolean CanTransferFuel();
    boolean GroundAttack();
    boolean GetElectronicWarfare();
    EntityType GetAircraftType();
    boolean HasMissiles();
    boolean HasInterceptors();
    boolean HasCargo();
    EntityPointer GetHomeBase();
    String GetName();
    void SetName(String strName);
    boolean WillAutoReturn();
    void ToggleAutoReturn();
    boolean Flying();
    MissileSystem GetMissileSystem();
    MissileSystem GetInterceptorSystem();
    CargoSystem GetCargoSystem();
    LaunchEntity GetAirplane();
    int GetElecWarfareReload();
    boolean IsOffensive();
    boolean IsDefensive();
    boolean IsNeutral();
    byte[] GetData(int lAskingID);
    boolean GetOwnedBy(int lPlayerID);
    int GetSessionCode();
    boolean GetAuto();
    boolean GetSemiAuto();
    boolean GetManual();
    byte GetMode();
    void SetMode(byte cMode);
}
