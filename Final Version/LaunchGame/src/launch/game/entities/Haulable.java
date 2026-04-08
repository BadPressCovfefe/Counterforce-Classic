/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.systems.CargoSystem.LootType;

/**
 * Haulable identifies entity types that can be stored inside cargo systems.
 * Examples include StoredInfantry, StoredMissile/SAMTanks, StoredLaunchable, Resource. 
 * @author Corbin
 */
public interface Haulable
{
    LootType GetLootType();
    int GetCargoID();
    long GetWeight();
    long GetQuantity();
    byte[] GetData(int lAskingID);
}
