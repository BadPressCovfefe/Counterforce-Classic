/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package launch.game.entities;

/**
 *
 * @author Corbin
 */
public interface FuelableInterface
{
    float GetMaxFuel();
    float GetCurrentFuel();
    float GetFuelDeficit();
    boolean OutOfFuel();
    void Refuel();
    void Refuel(float fltAmount);
    int GetOwnerID();
}
