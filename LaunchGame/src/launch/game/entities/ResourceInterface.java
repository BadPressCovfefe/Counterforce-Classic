/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.systems.ResourceSystem;

/**
 *
 * @author Corbin
 * This is an interface for entities that possess resource systems.
 * Used to make resource transfer code easier.
 */
public interface ResourceInterface
{
    int GetID();
    ResourceSystem GetResourceSystem();
}
