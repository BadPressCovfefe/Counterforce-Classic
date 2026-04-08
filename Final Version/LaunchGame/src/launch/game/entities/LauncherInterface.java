/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.systems.MissileSystem;

/**
 *
 * @author Corbin
 */
public interface LauncherInterface
{
    MissileSystem GetMissileSystem();
    boolean HoldsMissiles();
    boolean HoldsInterceptors();
}
