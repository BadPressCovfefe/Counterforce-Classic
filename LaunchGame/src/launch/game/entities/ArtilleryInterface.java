/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

import launch.game.entities.conceptuals.FireOrder;

/**
 *
 * @author Corbin
 */
public interface ArtilleryInterface
{
    FireOrder GetFireOrder();
    boolean HasFireOrder();
    void RoundsComplete();
    void FireForEffect(FireOrder order);
}
