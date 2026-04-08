/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.systems.CargoSystem;

/**
 *
 * @author Corbin
 */
public interface HaulerInterface
{
    int GetID();
    CargoSystem GetCargoSystem();
}
