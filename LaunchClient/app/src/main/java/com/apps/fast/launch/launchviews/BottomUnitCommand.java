package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirbaseInterface;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.LandUnit;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable.MovableType;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.entities.Ship;
import launch.game.entities.Structure;
import launch.game.entities.Submarine;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.systems.CargoSystem;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomUnitCommand extends LaunchView
{
    private enum CommandCategory
    {
        ATTACK,
        MOVE,
        CAPTURE,
    }

    private TextView txtCommandInstructions;
    private TextView txtCommandTitle;
    private TextView txtOutOfRange;
    private TextView txtFlightTime;
    private TextView txtFriendlyFire;
    private LinearLayout btnCommand;
    private ImageView imgCommand;
    private LinearLayout btnCancel;
    private TextView txtFuelUsage;
    private TextView txtAirspaceWarning;
    private TextView txtManualReturnWarning;
    private TextView txtAutoReturnWarning;

    private boolean bShowFlightTime;
    private boolean bShowFuelUsage;
    private boolean bShowOutOfRange;
    private boolean bShowAirspaceWarning;
    private boolean bShowAutoReturnWarning;

    //private LaunchEntity movableEntity;

    private GeoCoord geoTarget = null;
    private EntityPointer target = null;
    private EntityPointer movable;
    private List<EntityPointer> MovableEntities;
    private Map<EntityPointer, Polyline> MovableTrajectories = new ConcurrentHashMap<>();
    private MoveOrders command;

    private GoogleMap map;
    private Polyline targetTrajectory;
    private Marker movementMarker;
    private MovableType movableType;

    public BottomUnitCommand(LaunchClientGame game, MainActivity activity, MoveOrders command, EntityPointer movable)
    {
        super(game, activity, true);
        this.command = command;
        this.movable = movable;

        Setup();
    }

    public BottomUnitCommand(LaunchClientGame game, MainActivity activity, MoveOrders command, List<EntityPointer> MovableEntities)
    {
        super(game, activity, true);
        this.command = command;
        this.MovableEntities = MovableEntities;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_unit_command, this);

        txtCommandTitle = findViewById(R.id.txtCommandTitle);
        txtCommandInstructions = findViewById(R.id.txtCommandInstructions);
        txtOutOfRange = findViewById(R.id.txtOutOfRange);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        txtFriendlyFire = findViewById(R.id.txtFriendlyFire);
        btnCommand = findViewById(R.id.btnCommand);
        imgCommand = findViewById(R.id.imgCommand);
        btnCancel = findViewById(R.id.btnCancel);
        txtFuelUsage = findViewById(R.id.txtFuelUsage);
        txtAirspaceWarning = findViewById(R.id.txtAirspaceWarning);
        txtManualReturnWarning = findViewById(R.id.txtManualReturnWarning);
        txtAutoReturnWarning = findViewById(R.id.txtAutoReturnWarning);

        switch(command)
        {
            case MOVE:
            {
                txtCommandTitle.setText(R.string.title_move_order);
                txtCommandInstructions.setText(R.string.move_instructions);
                imgCommand.setImageResource(R.drawable.button_move);
            }
            break;

            case SEEK_FUEL:
            {
                if(movable != null)
                {
                    if(movable.GetEntity(game) instanceof AirplaneInterface)
                    {
                        txtCommandTitle.setText(R.string.title_seek_fuel_aircraft);
                        txtCommandInstructions.setText(R.string.seek_fuel_aircraft_instructions);
                        imgCommand.setImageResource(R.drawable.button_seek_fuel);
                    }
                    else if(movable.GetEntity(game) instanceof NavalVessel)
                    {
                        txtCommandTitle.setText(R.string.title_seek_fuel_naval);
                        txtCommandInstructions.setText(R.string.seek_fuel_naval_instructions);

                        if(movable.GetEntity(game) instanceof Ship)
                            imgCommand.setImageResource(R.drawable.button_seek_fuel_ship);
                        else if(movable.GetEntity(game) instanceof Submarine)
                            imgCommand.setImageResource(R.drawable.button_seek_fuel_submarine);
                    }
                }
            }
            break;

            case PROVIDE_FUEL:
            {
                if(movable != null)
                {
                    if(movable.GetEntity(game) instanceof AirplaneInterface)
                    {
                        txtCommandTitle.setText(R.string.title_provide_fuel_aircraft);
                        txtCommandInstructions.setText(R.string.provide_fuel_aircraft_instructions);
                        imgCommand.setImageResource(R.drawable.button_provide_fuel);
                    }
                    else if(movable.GetEntity(game) instanceof NavalVessel)
                    {
                        txtCommandTitle.setText(R.string.title_provide_fuel_naval);
                        txtCommandInstructions.setText(R.string.provide_fuel_naval_instructions);

                        if(movable.GetEntity(game) instanceof Ship)
                            imgCommand.setImageResource(R.drawable.button_provide_fuel_ship);
                        else if(movable.GetEntity(game) instanceof Submarine)
                            imgCommand.setImageResource(R.drawable.button_provide_fuel_submarine);
                    }
                }
            }
            break;

            case ATTACK:
            {
                txtCommandTitle.setText(R.string.title_set_target);
                txtCommandInstructions.setText(R.string.target_instructions);
                imgCommand.setImageResource(R.drawable.button_attack);
            }
            break;
        }

        btnCommand.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(geoTarget == null && target == null)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_target_map));
                }
                else if(command == command.CAPTURE && target != null)
                {
                    MapEntity targetEntity = target.GetMapEntity(game);

                    if(targetEntity != null)
                    {
                        Player targetOwner = game.GetOwner(targetEntity);

                        IssueCommand();
                    }
                }
                else
                {
                    IssueCommand();
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    private void IssueCommand()
    {
        boolean bIssueCommand = true;
        boolean bFriendlyFire = false;
        boolean bBullyingOrNuisance = false;

        if(target != null)
        {
            if(command == MoveOrders.ATTACK)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(target.GetMapEntity(game))))
                {
                    bFriendlyFire = true;
                }
                else if(game.GetAttackIsBullying(game.GetOurPlayer(), game.GetOwner(target.GetMapEntity(game))))
                {
                    bBullyingOrNuisance = true;
                }
            }
            else if(command == MoveOrders.LIBERATE)
            {
                MapEntity targetEntity = target.GetMapEntity(game);

                int lBuilderID = LaunchEntity.ID_NONE;
                boolean bNotCaptured = false;

                if(targetEntity instanceof Structure)
                {
                    lBuilderID = ((Structure)targetEntity).GetBuiltByID();
                }

                Player builder = game.GetPlayer(lBuilderID);
                Player owner = game.GetOwner(targetEntity);

                if(targetEntity != null)
                {
                    if(lBuilderID == targetEntity.GetOwnerID())
                        bNotCaptured = true;

                    if(targetEntity instanceof Player)
                    {
                        //Liberating a player. Player should be captured and it *should not* be friendly fire between us and them.
                        if(game.WouldBeFriendlyFire(game.GetOurPlayer(), (Player)targetEntity))
                        {
                            bFriendlyFire = true;
                        }
                    }
                    else if(targetEntity instanceof Structure)
                    {
                        //Player is a city or structure. It should not be friendly fire between us and the owner of the structure.
                        //If the builder of the structure is AWOL, show a warning.
                        //If the structure or city is not captured, show a warning. It cannot be liberated.
                        if(game.WouldBeFriendlyFire(game.GetOurPlayer(), owner))
                        {
                            bFriendlyFire = true;
                        }

                        if(game.GetAttackIsBullying(game.GetOurPlayer(), game.GetOwner(targetEntity)))
                        {
                            bBullyingOrNuisance = true;
                        }
                    }
                }
            }
        }

        if(movable != null)
        {
            if(movable.GetEntity(game) instanceof Airplane)
            {
                if(target != null && target.GetMapEntity(game) instanceof AirbaseInterface)
                {
                    if(game.AircraftCanLandAtAirbase(((Airplane)movable.GetEntity(game)), ((AirbaseInterface)target.GetMapEntity(game))))
                    {
                        this.command = MoveOrders.RETURN;
                    }
                }
            }

            if(bFriendlyFire)
            {
                activity.ShowBasicOKDialog(context.getString(R.string.deliberate_friendly_fire));
                bIssueCommand = false;
            }
            else if(bBullyingOrNuisance)
            {
                if(game.GetOurPlayer().GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD)
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_elites, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
                else
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_noobs, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));

                bIssueCommand = false;
            }

            if(bIssueCommand)
            {
                game.UnitCommand(command, Collections.singletonList(movable), target, geoTarget, CargoSystem.LootType.NONE, 0, 0);
            }
        }
        else if(MovableEntities != null)
        {
            if(bFriendlyFire)
            {
                activity.ShowBasicOKDialog(context.getString(R.string.deliberate_friendly_fire));
                bIssueCommand = false;
            }
            else if(bBullyingOrNuisance)
            {
                if(game.GetOurPlayer().GetTotalValue() < Defs.WEAKLING_VALUE_THRESHOLD)
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_elites, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));
                else
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_attack_noobs, Defs.WEAKLING_VALUE_THRESHOLD, Defs.WEAKLING_VALUE_THRESHOLD));

                bIssueCommand = false;
            }

            if(bIssueCommand)
            {
                game.UnitCommand(command, MovableEntities, target, geoTarget, CargoSystem.LootType.NONE, 0, 0);
            }
        }

       activity.InformationMode(false);
    }

    public void LocationSelected(GeoCoord geoLocation, Polyline trajectory, Map<EntityPointer, Polyline> Trajectories, Marker marker, GoogleMap map)
    {
        geoTarget = geoLocation;
        this.map = map;
        this.targetTrajectory = trajectory;
        this.movementMarker = marker;
        this.MovableTrajectories = Trajectories;

        Update();
    }

    public void TargetSelected(MapEntity targetEntity, Polyline trajectory, Map<EntityPointer, Polyline> Trajectories, Marker marker, GoogleMap map)
    {
        this.target = targetEntity.GetPointer();
        this.map = map;
        this.targetTrajectory = trajectory;
        this.geoTarget = targetEntity.GetPosition();
        this.movementMarker = marker;
        this.MovableTrajectories = Trajectories;

        txtFlightTime.setText(context.getString(R.string.target_format, TextUtilities.GetEntityTypeAndName(targetEntity, game)));

        if(command == MoveOrders.ATTACK || command == MoveOrders.CAPTURE || command == MoveOrders.LIBERATE)
        {
            if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(targetEntity)))
            {
                txtFriendlyFire.setVisibility(VISIBLE);
            }
            else if(game.GetAttackIsBullying(game.GetOurPlayer(), game.GetOwner(targetEntity)))
            {
                txtFriendlyFire.setVisibility(VISIBLE);
                txtFriendlyFire.setText(context.getString(R.string.player_size_difference_warning));
            }
            /*else if(command == MoveOrders.CAPTURE && targetEntity instanceof Structure && game.GetProtectedByCapital((Structure)targetEntity))
            {
                txtFriendlyFire.setVisibility(VISIBLE);
                txtFriendlyFire.setText(context.getString(R.string.structure_too_close_to_capital));
            }*/
            else
            {
                txtFriendlyFire.setVisibility(GONE);
            }
        }

        //TODO: Logic should be here that distinguishes what to do with the target.

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
                if(geoTarget == null)
                {
                    txtCommandInstructions.setVisibility(VISIBLE);
                    txtFlightTime.setVisibility(GONE);
                    txtFuelUsage.setVisibility(GONE);
                }
                else
                {
                    if(movable != null)
                    {
                        if(movable.GetEntity(game) instanceof Airplane)
                        {
                            Airplane aircraft = ((Airplane) movable.GetEntity(game));
                            GeoCoord geoFrom;
                            List<LatLng> points = new ArrayList<LatLng>();

                            geoFrom = aircraft.GetPosition();
                            float fltDistance = geoFrom.DistanceTo(geoTarget);
                            txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(aircraft.GetAircraftType()), geoFrom, geoTarget))));
                            txtOutOfRange.setVisibility(fltDistance > game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetAircraftType())) ? VISIBLE : GONE);
                            txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));

                            points.add(Utilities.GetLatLng(geoFrom));
                            points.add(Utilities.GetLatLng(geoTarget));
                            targetTrajectory.setPoints(points);

                            if(movementMarker != null)
                                movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

                            txtCommandInstructions.setVisibility(GONE);
                            txtFlightTime.setVisibility(VISIBLE);
                            txtFuelUsage.setVisibility(VISIBLE);
                        }
                        else if(movable.GetEntity(game) instanceof StoredAirplane)
                        {
                            //TODO: When aircraft carriers are added, we will need to change lHomeBaseID to an entitypointer to identify the airbase or ship that contains the aircraft.
                            StoredAirplane aircraft = ((StoredAirplane) movable.GetEntity(game));
                            GeoCoord geoFrom;
                            List<LatLng> points = new ArrayList<LatLng>();

                            geoFrom = aircraft.GetHomeBase().GetMapEntity(game).GetPosition();
                            float fltDistance = geoFrom.DistanceTo(geoTarget);
                            txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(aircraft.GetAircraftType()), geoFrom, geoTarget))));
                            txtOutOfRange.setVisibility(fltDistance > game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetAircraftType())) ? VISIBLE : GONE);
                            txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));

                            points.add(Utilities.GetLatLng(geoFrom));
                            points.add(Utilities.GetLatLng(geoTarget));
                            targetTrajectory.setPoints(points);

                            if(movementMarker != null)
                                movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

                            txtCommandInstructions.setVisibility(GONE);
                            txtFlightTime.setVisibility(VISIBLE);
                            txtFuelUsage.setVisibility(VISIBLE);
                        }
                        else if(movable.GetEntity(game) instanceof LandUnit)
                        {
                            LandUnit unit = (LandUnit)movable.GetEntity(game);

                            GeoCoord geoFrom = unit.GetPosition();
                            List<LatLng> points = new ArrayList<LatLng>();

                            txtFlightTime.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.LAND_UNIT_SPEED, geoFrom, geoTarget))));
                            txtOutOfRange.setVisibility(GONE);
                            txtFuelUsage.setVisibility(GONE);

                            if(movementMarker != null)
                                movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

                            points.add(Utilities.GetLatLng(geoFrom));
                            points.add(Utilities.GetLatLng(geoTarget));
                            targetTrajectory.setPoints(points);

                            txtCommandInstructions.setVisibility(GONE);
                            txtFlightTime.setVisibility(VISIBLE);
                        }
                        else if(movable.GetEntity(game) instanceof NavalVessel)
                        {
                            NavalVessel vessel = ((NavalVessel) movable.GetEntity(game));
                            GeoCoord geoFrom;
                            List<LatLng> points = new ArrayList<LatLng>();

                            geoFrom = vessel.GetPosition();
                            float fltDistance = geoFrom.DistanceTo(geoTarget);
                            txtFlightTime.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.NAVAL_SPEED, geoFrom, geoTarget))));
                            txtOutOfRange.setVisibility(fltDistance > game.GetFuelableRange(vessel.GetCurrentFuel(), Defs.NAVAL_RANGE) ? VISIBLE : GONE);

                            if(!vessel.GetNuclear())
                            {
                                txtFuelUsage.setVisibility(VISIBLE);
                                txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, vessel, geoTarget)));
                            }
                            else
                                txtFuelUsage.setVisibility(GONE);

                            if(movementMarker != null)
                                movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

                            points.add(Utilities.GetLatLng(geoFrom));
                            points.add(Utilities.GetLatLng(geoTarget));
                            targetTrajectory.setPoints(points);

                            txtCommandInstructions.setVisibility(GONE);
                            txtFlightTime.setVisibility(VISIBLE);
                        }
                    }
                    else if(MovableEntities != null)
                    {
                        for(Map.Entry<EntityPointer, Polyline> entry : MovableTrajectories.entrySet())
                        {
                            GeoCoord geoStart = game.GetPosition(entry.getKey().GetEntity(game));
                            entry.getValue().setPoints(Arrays.asList(Utilities.GetLatLng(geoStart), Utilities.GetLatLng(geoTarget)));
                        }

                        txtFlightTime.setVisibility(GONE);
                        txtOutOfRange.setVisibility(GONE);
                        txtFuelUsage.setVisibility(GONE);
                        txtCommandInstructions.setVisibility(GONE);

                        if(movementMarker != null)
                            movementMarker.setPosition(Utilities.GetLatLng(geoTarget));
                    }
                }
            }
        });
    }
}
