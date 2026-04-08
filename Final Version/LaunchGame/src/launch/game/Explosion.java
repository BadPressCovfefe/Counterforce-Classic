/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

/**
 *
 * @author Corbin
 */
public class Explosion
{
    private int lYield;
    private boolean bNuclear;
    private boolean bICBM;
    private boolean bBunkerBuster;
    private boolean bAntiShip;
    private boolean bAntiSubmarine;
    private boolean bArtillery;
    private boolean bAirburst;
    private float fltAccuracy; //For use with missile explosions.
    
    public Explosion(int lYield, boolean bNuclear, boolean bICBM, boolean bBunkerBuster, boolean bAntiShip, boolean bAntiSubmarine, boolean bArtillery, boolean bAirburst, float fltAccuracy)
    {
        this.lYield = lYield;
        this.bNuclear = bNuclear;
        this.bICBM = bICBM;
        this.bBunkerBuster = bBunkerBuster;
        this.bAntiShip = bAntiShip;
        this.bAntiSubmarine = bAntiSubmarine;
        this.bArtillery = bArtillery;
        this.bAirburst = bAirburst;
        this.fltAccuracy = fltAccuracy;
    }
    
    public int GetYield() { return this.lYield; }
    
    public boolean GetNuclear() { return bNuclear; }
    
    public boolean GetICBM() { return bICBM; }
    
    public boolean GetArtillery() { return bArtillery; }
    
    public boolean GetAntiShip() { return bAntiShip; }
    
    public boolean GetBunkerBuster() { return bBunkerBuster; }
    
    public boolean GetAirburst() { return bAirburst; }
    
    public boolean GetAntiSubmarine() { return bAntiSubmarine; }
    
    public float GetAccuracy() { return fltAccuracy; }
    
}
