/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game.entities;

import launch.game.GeoCoord;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.LaunchSystem;
import launch.game.systems.LaunchSystemListener;
import launch.game.systems.MissileSystem;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;

public abstract class NavalVessel extends Movable implements LaunchSystemListener, FuelableInterface, NamableInterface
{
    private static final int DATA_SIZE = 16;
    
    protected EntityType type;
    private String strName;
    protected int lOwnerID;
    protected float fltCurrentFuel;
    protected ShortDelay dlySonar;
    protected MissileSystem missiles; //+1 for the byte indicating whether this is null.
    protected MissileSystem torpedoes; //+1 for the byte indicating whether this is null.
    protected List<Float> ContactBearings = new ArrayList<>();
    
    /** New ship from a shipyard. */
    public NavalVessel(int lID, GeoCoord geoPosition, int lOwnerID, short nHP, short nMaxHP, EntityType type, MissileSystem missiles, MissileSystem torpedoes)
    {
        super(lID, geoPosition, nHP, nMaxHP);
        this.type = type;
        this.strName = "";
        this.lOwnerID = lOwnerID;
        this.dlySonar = new ShortDelay();
        this.fltCurrentFuel = 1.0f;
        this.missiles = missiles;
        this.torpedoes = torpedoes;
        
        if(missiles != null)
            missiles.SetSystemListener(this);
        
        if(torpedoes != null)
            torpedoes.SetSystemListener(this);
    }
    
    /** From save. */
    public NavalVessel(int lID, GeoCoord geoPosition, int lOwnerID, short nHP, short nMaxHP, EntityType type, String strName, MoveOrders moveOrders, GeoCoord geoTarget, EntityPointer target, int lSonarCooldown, float fltCurrentFuel, MissileSystem missiles, MissileSystem torpedoes, boolean bVisible, int lVisibleTime, Map<Integer, GeoCoord> Coordinates)
    {
        super(lID, geoPosition, nHP, nMaxHP, moveOrders, geoTarget, target, bVisible, lVisibleTime, Coordinates);
        this.type = type;
        this.strName = strName;
        this.lOwnerID = lOwnerID;
        this.dlySonar = new ShortDelay(lSonarCooldown);
        this.fltCurrentFuel = fltCurrentFuel;
        this.missiles = missiles;
        this.torpedoes = torpedoes;
        
        if(this.missiles != null)
            missiles.SetSystemListener(this);
        
        if(this.torpedoes != null)
            torpedoes.SetSystemListener(this);
        
        SetPointer();
    }
    
    /**
     * A backup feature that allows a structure from a seperate game snapshot to be re-ID'd for incorporation into a different game.
     * @param lNewID The new ID to assign. Should use an atomic method to determine the ID.
     * @return The structure instance with a new ID.
     */
    public NavalVessel ReIDAndReturnSelf(int lNewID)
    {
        this.lID = lNewID;
        return this;
    }
    
    /** From comms.
     * @param bb The byte buffer of received data.
     * @param lReceivingID The player ID of the client for unpacking potentially player-only data. */
    public NavalVessel(ByteBuffer bb, int lReceivingID)
    {
        super(bb);
        type = EntityType.values()[bb.get()];
        strName = LaunchUtilities.StringFromData(bb);
        lOwnerID = bb.getInt();
        dlySonar = new ShortDelay(bb);
        fltCurrentFuel = bb.getFloat();
        
        if((bb.get() != 0x00))
            ContactBearings = ContactBearingsFromData(bb);
        if((bb.get() != 0x00))
            missiles = new MissileSystem(this, bb);
        if((bb.get() != 0x00))
            torpedoes = new MissileSystem(this, bb);
        
        if(missiles != null)
            missiles.SetSystemListener(this);
        
        if(torpedoes != null)
            torpedoes.SetSystemListener(this);
    }

    @Override
    public void Tick(int lMS)
    {
        super.Tick(lMS);
        
        if(missiles != null)
        {
            missiles.Tick(lMS);
        }
        
        if(torpedoes != null)
        {
            torpedoes.Tick(lMS);
        }
        
        if(dlySonar != null)
        {
            dlySonar.Tick(lMS);
        }
    }

    @Override
    public byte[] GetData(int lAskingID)
    {
        byte[] cContactBearingData = !ContactBearings.isEmpty() ? GetContactBearingData() : new byte[0];
        byte[] cMissilesData = missiles != null ? missiles.GetData(lAskingID) : new byte[0]; 
        byte[] cTorpedoesData = torpedoes != null ? torpedoes.GetData(lAskingID) : new byte[0];
        byte[] cBaseData = super.GetData(lAskingID);
        
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE + cContactBearingData.length + cMissilesData.length + cTorpedoesData.length + cBaseData.length + LaunchUtilities.GetStringDataSize(strName));
        bb.put(cBaseData);
        bb.put((byte)type.ordinal());
        bb.put(LaunchUtilities.GetStringData(strName));
        bb.putInt(lOwnerID);
        bb.putInt(dlySonar.GetRemaining());
        bb.putFloat(fltCurrentFuel);
        bb.put((byte)(!ContactBearings.isEmpty() ? 0xFF : 0x00));
        bb.put(cContactBearingData);
        bb.put((byte)(missiles != null? 0xFF : 0x00));
        bb.put(cMissilesData);
        bb.put((byte)(torpedoes != null? 0xFF : 0x00));
        bb.put(cTorpedoesData);
        
        return bb.array();
    }
    
    private byte[] GetContactBearingData()
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + (ContactBearings.size() * Float.BYTES));
        bb.putInt(ContactBearings.size());
        
        for(Float fltBearing : ContactBearings)
        {
            bb.putFloat(fltBearing);
        }
        
        return bb.array();
    }
    
    private List<Float> ContactBearingsFromData(ByteBuffer bb)
    {
        List<Float> Result = new ArrayList<>();
        
        int lBearingCount = bb.getInt();
        
        for(int i = 0; i < lBearingCount; i++)
        {
            Result.add(bb.getFloat());
        }
        
        return Result;
    }
    
    public boolean HasPassiveContact()
    {
        return !ContactBearings.isEmpty();
    }
    
    public List<Float> GetContactBearings() { return ContactBearings; }
    
    public void ClearContactBearings()
    {
        ContactBearings.clear();
    }
    
    public void AddContactBearing(float fltBearing)
    {
        ContactBearings.add(fltBearing);
    }
    
    @Override
    public String GetName() { return strName; }
    
    public void SetName(String strName)
    {
        this.strName = strName;
        Changed(false);
    }
        
    @Override
    public int GetOwnerID()
    {
        return lOwnerID;
    }
    
    public boolean GetNuclear()
    {
        return this.type == EntityType.SSBN || this.type == EntityType.SUPER_CARRIER;
    }
    
    public boolean HasSonar()
    {
        return this.type == EntityType.ATTACK_SUB || this.type == EntityType.SSBN || this.type == EntityType.FRIGATE || this.type == EntityType.DESTROYER;
    }
    
    public int GetSonarCooldownRemaining()
    {
        return dlySonar.GetRemaining();
    }
    
    public boolean SonarReady()
    {
        return HasSonar() && dlySonar.Expired();
    }
    
    public void SonarPing()
    {
        if(HasSonar())
        {
            //TODO: When the visibility update is ready, we'll need this to reveal the ship and start the visibility timer.
            dlySonar.Set(Defs.SONAR_PING_COOLDOWN);
        }
    }
    
    public boolean HasMissiles()
    {
        return missiles != null;
    }
    
    public MissileSystem GetMissileSystem()
    {
        return missiles;
    }
    
    public boolean HasTorpedoes()
    {
        return torpedoes != null;
    }
    
    public MissileSystem GetTorpedoSystem()
    {
        return torpedoes;
    }
    
    public void SetOwner(int lNewOwnerID)
    {
        this.lOwnerID = lNewOwnerID;
    }
    
    @Override
    public boolean GetOwnedBy(int lID)
    {
        return lID == lOwnerID;
    }

    @Override
    public float GetCurrentFuel()
    {
        return fltCurrentFuel;
    }

    @Override
    public float GetFuelDeficit()
    {
        return GetMaxFuel() - fltCurrentFuel;
    }

    @Override
    public boolean OutOfFuel()
    {
        return fltCurrentFuel <= 0.0f;
    }
    
    @Override
    public void SystemChanged(LaunchSystem system)
    {
        //One of our systems changed, therefore we changed.
        Changed(false);
    }
    
    /**
     * Add fuel, in TONS, to the fuel tank. Any time this is called, input MUST BE IN TONS.
     * @return the amount of fuel added
     */
    public float AddFuel(float fltFuelToAddInTons)
    {
        float fltFuelAddedInTons = 0;
        
        if(GetFuelDeficit() > 0)
        {
            if(GetFuelDeficit() >= fltFuelToAddInTons)
            {
                fltCurrentFuel += fltFuelToAddInTons;
                return fltFuelToAddInTons; //We took all the fuel, so return the whole amount.
            }
            else
            {
                fltFuelAddedInTons = fltFuelToAddInTons - GetFuelDeficit();
                fltCurrentFuel += GetFuelDeficit();
                
                return fltFuelAddedInTons;
            }
        }
        
        return 0;
    }
    
    public void UseFuel(float fltFuel)
    {
        this.fltCurrentFuel -= fltFuel;
        
        if(this.fltCurrentFuel < 0.0f)
            fltCurrentFuel = 0.0f;
    }
    
    public boolean HasFuelRemaining()
    {
        if(GetNuclear())
            return true;
        else
            return fltCurrentFuel > 0.0f;
    }
    
    /**
     * 
     * @param fltFuelInTons the fuel to add, IN TONS.
     * @return the amount of fuel transferred.
     */
    public float TransferRefuel(float fltFuel)
    {
        if(this.fltCurrentFuel + fltFuel > 1.0f)
        {
            float fltFuelTransferred = 1.0f - fltCurrentFuel;
            this.fltCurrentFuel = 1.0f;
            Changed(false);
            return fltFuelTransferred;
        }
        else
        {
            this.fltCurrentFuel += fltFuel;
            Changed(false);
            return fltFuel;
        }
    }
    
    public boolean Moving()
    {
        return moveOrders == MoveOrders.MOVE || moveOrders == MoveOrders.PROVIDE_FUEL || moveOrders == MoveOrders.SEEK_FUEL;
    }
    
    public void Capture(int lPlayerID)
    {
        this.lOwnerID = lPlayerID;
    }
    
    @Override
    public String GetTypeName()
    {
        switch(type)
        {
            case FRIGATE: return "frigate";
            case DESTROYER: return "destroyer";
            case AMPHIB: return "assault ship";
            case CARGO_SHIP: return "cargo ship";
            case FLEET_OILER: return "fleet oiler";
            case SUPER_CARRIER: return "aircraft carrier";
            case ATTACK_SUB: return "attack submarine";
            case SSBN: return "SSBN";
        }
        
        return "[ship class unspecified]";
    }
    
    @Override
    public void Refuel()
    {
        this.fltCurrentFuel = 1.0f;
        Changed(false);
    }
    
    @Override
    public void Refuel(float fltAmount)
    {
        this.fltCurrentFuel += fltAmount;
        
        if(fltCurrentFuel > 1.0f)
            fltCurrentFuel = 1.0f;
                    
        Changed(false);
    }
    
    @Override
    public float GetMaxFuel()
    {
        return 1.0f;
    }
    
    public boolean FuelFull()
    {
        return this.fltCurrentFuel == 1.0f;
    }
}
