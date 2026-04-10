package com.apps.fast.launch.launchviews;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.entities.conceptuals.StoredDamagable;
import launch.game.entities.conceptuals.StoredEntity;
import launch.game.systems.MissileSystem;
import launch.game.entities.Movable.MoveOrders;

public class AircraftQuickSelectView extends LaunchView
{
    private TextView txtAircraftType;
    private ImageView imgAircraftType;
    private ImageView imgMissiles;
    private LinearLayout lytMissiles;
    private TextView txtMissiles;
    private TextView txtFuelLevel;
    private TextView txtAircraftStatus;
    private AirplaneInterface aircraft;
    private MissileSystem missileSystem;
    private TextView txtFlightTime;
    private TextView txtFuelUsage;
    private GeoCoord geoTarget;
    private MapEntity targetEntity;
    private MoveOrders order;
    private TextView txtName;
    private TextView txtHP;
    private LinearLayout lytInterceptors;
    private ImageView imgInterceptors;
    private TextView txtInterceptors;
    private MissileSystem interceptorSystem;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param aircraft Reference to the aircraft.
     */
    public AircraftQuickSelectView(LaunchClientGame game, MainActivity activity, AirplaneInterface aircraft, GeoCoord geoTarget, MapEntity entity, MoveOrders order)
    {
        super(game, activity, true);

        this.aircraft = aircraft;
        this.geoTarget = geoTarget;
        this.targetEntity = entity;
        this.order = order;

        if(aircraft.HasMissiles())
        {
            missileSystem = aircraft.GetMissileSystem();
        }

        if(!aircraft.HasMissiles() && aircraft.HasInterceptors())
        {
            interceptorSystem = aircraft.GetInterceptorSystem();
        }

        if(aircraft.HasCargo())
        {
            //system3 = aircraft.GetCargoSystem();
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_aircraft_quickselect, this);

        imgAircraftType = findViewById(R.id.imgAircraftType);
        txtAircraftType = findViewById(R.id.txtAircraftType);
        imgMissiles = findViewById(R.id.imgArsenalType);
        lytMissiles = findViewById(R.id.lytArsenal);
        txtMissiles = findViewById(R.id.txtArmaments);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAircraftStatus = findViewById(R.id.txtInfantryStatus);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        txtFuelUsage = findViewById(R.id.txtFuelUsage);
        txtName = findViewById(R.id.txtName);
        txtHP = findViewById(R.id.txtHPTitle);
        imgInterceptors = findViewById(R.id.imgArsenalType2);
        txtInterceptors = findViewById(R.id.txtArmaments2);
        lytInterceptors = findViewById(R.id.lytArsenal2);

        txtName.setText(aircraft.GetName().length() > 0 ? aircraft.GetName() : context.getString(R.string.unnamed));

        imgAircraftType.setImageBitmap(EntityIconBitmaps.GetAircraftBitmap(context, game, aircraft));

        txtAircraftType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)aircraft, game));

        if(aircraft.HasMissiles())
        {
            lytMissiles.setVisibility(VISIBLE);
        }
        else
        {
            lytMissiles.setVisibility(GONE);
        }

        if(aircraft.HasInterceptors())
        {
            lytInterceptors.setVisibility(VISIBLE);
        }
        else
        {
            lytInterceptors.setVisibility(GONE);
        }

        if(aircraft.HasCargo())
        {
            //TODO
            //imgCargo.setImageResource(R.drawable.icon_cargo);
            //lytCargo.setVisibility(VISIBLE);
        }

        /*For setting up the missile/interceptor count readout.*/
        if(missileSystem != null)
        {
            txtMissiles.setText(context.getString(R.string.aircraft_armaments, missileSystem.GetOccupiedSlotCount(), missileSystem.GetSlotCount()));

            int lowMissileThreshold = missileSystem.GetSlotCount()/3;

            if(missileSystem.GetOccupiedSlotCount() == missileSystem.GetSlotCount())
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(missileSystem.GetOccupiedSlotCount() < lowMissileThreshold)
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
        }

        if(interceptorSystem != null)
        {
            int lowInterceptorThreshold = interceptorSystem.GetSlotCount()/3;

            txtInterceptors.setText(context.getString(R.string.aircraft_armaments, interceptorSystem.GetOccupiedSlotCount(), interceptorSystem.GetSlotCount()));

            if(interceptorSystem.GetOccupiedSlotCount() == interceptorSystem.GetSlotCount())
            {
                txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(interceptorSystem.GetOccupiedSlotCount() < lowInterceptorThreshold)
            {
                txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
        }

        /*For setting up fuel level readout.*/
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraft);

        /*For setting up aircraft status string.*/
        txtAircraftStatus = findViewById(R.id.txtInfantryStatus);

        if(aircraft.Flying())
        {
            Movable flyingAircraft = (Movable)aircraft;

            if(flyingAircraft != null && order != null)
            {
                if(order == MoveOrders.MOVE && flyingAircraft.HasGeoTarget() && flyingAircraft.GetGeoTarget().DistanceTo(geoTarget) <= PositionBuffer())
                {
                    txtAircraftStatus.setText(context.getString(R.string.status_enroute));
                    txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(order == MoveOrders.ATTACK)
                {
                    if(flyingAircraft.GetTarget() != null && flyingAircraft.GetTarget().GetMapEntity(game) != null)
                    {
                        if(targetEntity != null && flyingAircraft.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                        {
                            txtAircraftStatus.setText(context.getString(R.string.status_already_attacking));
                            txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                        else
                        {
                            txtAircraftStatus.setText(context.getString(R.string.status_attacking));
                            txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                    }
                }
                else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.RETURN)
                {
                    txtAircraftStatus.setText(context.getString(R.string.status_returning));
                    txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.MOVE)
                {
                    txtAircraftStatus.setText(context.getString(R.string.status_moving));
                    txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.WAIT)
                {
                    txtAircraftStatus.setText(context.getString(R.string.status_waiting));
                    txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
            }
        }
        else
        {
            if(aircraft.GetFuelDeficit() > 0)
            {
                txtAircraftStatus.setText(context.getString(R.string.status_refueling));
                txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else
            {
                txtAircraftStatus.setText(context.getString(R.string.status_ready));
                txtAircraftStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(aircraft.Flying())
                    aircraft = game.GetAirplane(aircraft.GetID());
                else
                    aircraft = game.GetStoredAirplane(aircraft.GetID());

                if(aircraft.HasMissiles())
                {
                    missileSystem = aircraft.GetMissileSystem();
                }

                if(aircraft.HasInterceptors())
                {
                    interceptorSystem = aircraft.GetInterceptorSystem();
                }

                if(aircraft.HasCargo())
                {
                    //TODO
                    //system3 = aircraft.GetCargoSystem();
                }

                if(aircraft.Flying())
                {
                    Movable flyingAircraft = (Movable)aircraft;

                    TextUtilities.AssignHealthStringAndAppearance(txtHP, flyingAircraft);
                    txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(((LaunchEntity)aircraft).GetEntityType()), flyingAircraft.GetPosition(), geoTarget))));
                    txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));
                    /*For setting up the missile/interceptor count readout.*/

                    if(missileSystem != null)
                    {
                        /*For setting up the missile/interceptor count readout.*/
                        txtMissiles.setText(context.getString(R.string.aircraft_armaments, missileSystem.GetOccupiedSlotCount(), missileSystem.GetSlotCount()));

                        int lowMissileThreshold = missileSystem.GetSlotCount()/3;

                        if(missileSystem.GetOccupiedSlotCount() == missileSystem.GetSlotCount())
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        }
                        else if(missileSystem.GetOccupiedSlotCount() < lowMissileThreshold)
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                        else
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                        }
                    }

                    if(interceptorSystem != null)
                    {
                        int lowInterceptorThreshold = interceptorSystem.GetSlotCount()/3;

                        txtInterceptors.setText(context.getString(R.string.aircraft_armaments, interceptorSystem.GetOccupiedSlotCount(), interceptorSystem.GetSlotCount()));

                        if(interceptorSystem.GetOccupiedSlotCount() == interceptorSystem.GetSlotCount())
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        }
                        else if(interceptorSystem.GetOccupiedSlotCount() < lowInterceptorThreshold)
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                        else
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                        }
                    }

                    /*For setting up fuel level readout.*/
                    txtFuelLevel = findViewById(R.id.txtFuelLevel);
                    TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraft);

                    TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);
                }
                else
                {
                    StoredDamagable storedAircraft = (StoredDamagable)aircraft;
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, storedAircraft);
                    GeoCoord geoStart = aircraft.GetHomeBase().GetMapEntity(game).GetPosition();
                    txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(((LaunchEntity)aircraft).GetEntityType()), geoStart, geoTarget))));
                    txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));

                    if(missileSystem != null)
                    {
                        /*For setting up the missile/interceptor count readout.*/
                        txtMissiles.setText(context.getString(R.string.aircraft_armaments, missileSystem.GetOccupiedSlotCount(), missileSystem.GetSlotCount()));

                        int lowMissileThreshold = missileSystem.GetSlotCount()/3;

                        if(missileSystem.GetOccupiedSlotCount() == missileSystem.GetSlotCount())
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        }
                        else if(missileSystem.GetOccupiedSlotCount() < lowMissileThreshold)
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                        else
                        {
                            txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                        }
                    }

                    if(interceptorSystem != null)
                    {
                        int lowInterceptorThreshold = interceptorSystem.GetSlotCount()/3;

                        txtInterceptors.setText(context.getString(R.string.aircraft_armaments, interceptorSystem.GetOccupiedSlotCount(), interceptorSystem.GetSlotCount()));

                        if(interceptorSystem.GetOccupiedSlotCount() == interceptorSystem.GetSlotCount())
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        }
                        else if(interceptorSystem.GetOccupiedSlotCount() < lowInterceptorThreshold)
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        }
                        else
                        {
                            txtInterceptors.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                        }
                    }

                    /*For setting up fuel level readout.*/
                    txtFuelLevel = findViewById(R.id.txtFuelLevel);
                    TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraft);

                    TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(aircraft != null)
        {
            if(entity == aircraft)
                bUpdate = true;
        }

        if(bUpdate)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Update();
                }
            });
        }
    }

    public float PositionBuffer()
    {
        switch((int) activity.fltZoomLevel)
        {
            case 21:
            case 20:
            case 19:
            case 18: return 0.025f;
            case 17: return 0.075f;
            case 16: return 0.13f;
            case 15: return 0.25f;
            case 14: return 0.347f;
            case 13: return 0.575f;
            case 12: return 1.4f;
            case 11: return 2.9f;
            case 10: return 4f;
            case 9: return 10f;
            case 8: return 25f;
            case 7: return 60f;
            case 6: return 250f;
            case 5: return 400f;
            case 4: return 600f;
            case 3:
            case 2:
            case 1:
            case 0:
            default: return 1000f;
        }
    }
}
