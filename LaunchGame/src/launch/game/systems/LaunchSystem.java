/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.systems;

/**
 *
 * @author tobster
 */
public abstract class LaunchSystem
{
    private LaunchSystemListener listener = null;
    
    //Used to control displays on the client side and to control upgrades, selling/buying missiles, etc, on the server-side. Not communicated. 
    public enum SystemType
    {
        MISSILE_SITE,
        SAM_SITE,
        AIRCRAFT_MISSILES,
        AIRCRAFT_INTERCEPTORS,
        STORED_AIRCRAFT_MISSILES,
        STORED_AIRCRAFT_INTERCEPTORS,
        SHIP_MISSILES,
        SHIP_INTERCEPTORS,
        SHIP_TORPEDOES,
        SHIP_ARTILLERY,
        TANK_MISSILES,
        TANK_INTERCEPTORS,
        STORED_TANK_MISSILES,
        STORED_TANK_INTERCEPTORS,
        WAREHOUSE,
        FACTORY,
        SUBMARINE_ICBM,
        SUBMARINE_MISSILES,
        SUBMARINE_TORPEDO,
        STRUCTURE_INFANTRY,
        AIRCRAFT_INFANTRY,
        SHIP_INFANTRY,
        SHIP_AIRCRAFT,
        SHIP_CARGO,
        STRUCTURE_AIRCRAFT,
        ARTILLERY_GUN,
        STORED_TANK_ARTILLERY,
        TANK_ARTILLERY,
    }
    
    public LaunchSystem()
    {
        //For saved entities. System listener must be assigned later.
    }
    
    public LaunchSystem(LaunchSystemListener listener)
    {
        this.listener = listener;
    }
    
    public void SetSystemListener(LaunchSystemListener listener)
    {
        this.listener = listener;
    }
    
    public abstract void Tick(int lMS);
    
    public abstract byte[] GetData(int lAskingID);
    
    protected final void Changed()
    {
        if(listener != null)
        {
            listener.SystemChanged(this);
        }
    }
}
