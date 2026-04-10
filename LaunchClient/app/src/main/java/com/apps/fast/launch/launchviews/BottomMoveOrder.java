package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.CargoTruck;
import launch.game.entities.FuelableInterface;
import launch.game.entities.LandUnit;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable.MovableType;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.NavalVessel;
import launch.game.entities.Tank;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.utilities.LaunchUtilities;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomMoveOrder extends LaunchView
{
    private TextView txtCommandInstructions;
    private TextView txtCommandTitle;
    private TextView txtOutOfRange;
    private TextView txtFlightTime;
    private LinearLayout btnCommand;
    private LinearLayout btnUndo;
    private LinearLayout btnCancel;
    private TextView txtFuelUsage;
    private TextView txtAirspaceWarning;
    private TextView txtManualReturnWarning;
    private TextView txtAutoReturnWarning;

    private int lCoordinateIndex = 0;
    private static final int lMaxCoordinates = 16;
    private Map<Integer, GeoCoord> Coordinates = new HashMap<>();
    private EntityPointer movable;
    private List<EntityPointer> MovableList = new ArrayList<>();
    private Map<EntityPointer, Polyline> MovableTrajectories = new ConcurrentHashMap<>();
    private MoveOrders command;
    private GoogleMap map;
    private Polyline movableTrajectory;
    private Marker movementMarker;
    private MovableType movableType;

    public BottomMoveOrder(LaunchClientGame game, MainActivity activity, MoveOrders command, List<EntityPointer> MovableList)
    {
        super(game, activity, true);
        this.command = command;
        this.MovableList = MovableList;

        Setup();
    }

    public BottomMoveOrder(LaunchClientGame game, MainActivity activity, MoveOrders command, EntityPointer movable)
    {
        super(game, activity, true);
        this.command = command;
        this.movable = movable;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_move_order, this);

        txtCommandTitle = findViewById(R.id.txtCommandTitle);
        txtCommandInstructions = findViewById(R.id.txtCommandInstructions);
        txtOutOfRange = findViewById(R.id.txtOutOfRange);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        btnCommand = findViewById(R.id.btnCommand);
        btnUndo = findViewById(R.id.btnUndo);
        btnCancel = findViewById(R.id.btnCancel);
        txtFuelUsage = findViewById(R.id.txtFuelUsage);
        txtAirspaceWarning = findViewById(R.id.txtAirspaceWarning);
        txtManualReturnWarning = findViewById(R.id.txtManualReturnWarning);
        txtAutoReturnWarning = findViewById(R.id.txtAutoReturnWarning);

        txtCommandTitle.setText(R.string.title_move_order);
        txtCommandInstructions.setText(R.string.move_instructions);

        btnCommand.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(Coordinates.isEmpty())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_target_map));
                }
                else
                {
                    IssueCommand();
                }
            }
        });

        btnUndo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!Coordinates.isEmpty())
                {
                    UndoLastCoordinate();
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
       if(movable != null)
       {
           game.MoveOrder(Collections.singletonList(movable), Coordinates);
       }
       else if(!MovableTrajectories.isEmpty())
       {
           game.MoveOrder(new ArrayList<>(MovableTrajectories.keySet()), Coordinates);
       }

        activity.InformationMode(false);
    }

    public void LocationSelected(GeoCoord geoLocation, EntityPointer movable, Polyline trajectory, Map<EntityPointer, Polyline> Trajectories, Marker movementMarker, GoogleMap map)
    {
        if(Coordinates.size() < lMaxCoordinates)
        {
            Coordinates.put(lCoordinateIndex, geoLocation);
            lCoordinateIndex++;
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.max_coordinates_reached, lMaxCoordinates));
        }

        this.movable = movable;
        this.MovableTrajectories = Trajectories;
        this.movableTrajectory = trajectory;
        this.map = map;
        this.movementMarker = movementMarker;

        Update();
    }

    public void UndoLastCoordinate()
    {
        if(lCoordinateIndex > 0)
        {
            lCoordinateIndex--;
        }
        else
        {
            if(movableTrajectory != null)
            {
                movableTrajectory.remove();
                movableTrajectory = null;
            }

            if(movementMarker != null)
            {
                movementMarker.remove();
                movementMarker = null;
            }

            if(!MovableTrajectories.isEmpty())
            {
                for(Polyline line : MovableTrajectories.values())
                {
                    if(line != null)
                    {
                        line.remove();
                    }
                }
            }
        }

        Coordinates.remove(lCoordinateIndex);
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
                if(Coordinates.isEmpty())
                {
                    txtCommandInstructions.setText(R.string.move_instructions);
                    txtFlightTime.setVisibility(GONE);
                    txtFuelUsage.setVisibility(GONE);
                }
                else
                {
                    if(movable != null)
                    {
                        if(Coordinates.size() > 1)
                        {
                            List<LatLng> points = new ArrayList<LatLng>();

                            LaunchEntity movableEntity = movable.GetEntity(game);
                            GeoCoord geoFrom = game.GetPosition(movableEntity);
                            points.add(Utilities.GetLatLng(geoFrom));

                            float fltDistance = 0;
                            float fltSpeed = 0;

                            if(movableEntity instanceof AirplaneInterface)
                                fltSpeed = Defs.GetAircraftSpeed(movableEntity.GetEntityType());
                            else if(movableEntity instanceof LandUnit)
                                fltSpeed = Defs.LAND_UNIT_SPEED;
                            else if(movableEntity instanceof NavalVessel)
                                fltSpeed = Defs.NAVAL_SPEED;

                            for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                            {
                                GeoCoord geoCoord = Coordinates.get(i);

                                if(geoCoord != null)
                                {
                                    if(i > LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()))
                                    {
                                        GeoCoord geoLast = Coordinates.get(i - 1);

                                        if(geoLast != null)
                                        {
                                            fltDistance += geoLast.DistanceTo(geoCoord);
                                        }
                                    }
                                    else
                                    {
                                        fltDistance += geoFrom.DistanceTo(geoCoord);
                                    }

                                    points.add(Utilities.GetLatLng(geoCoord));
                                }
                            }

                            if(movementMarker != null)
                            {
                                GeoCoord geoFinal = Coordinates.get(LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()));

                                if(geoFinal != null)
                                {
                                    movementMarker.setPosition(Utilities.GetLatLng(geoFinal));
                                }
                            }

                            movableTrajectory.setPoints(points);
                            txtCommandInstructions.setText(context.getString(R.string.coordinates_selected, Coordinates.size(), lMaxCoordinates));

                            long oTimeMS = (long)((fltDistance/fltSpeed) * Defs.MS_PER_HOUR);
                            txtFlightTime.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount(oTimeMS)));

                            if(movableEntity instanceof FuelableInterface)
                            {
                                float fltMaxRange = 0.0f;

                                if(movableEntity instanceof CargoTruck)
                                    fltMaxRange = Defs.CARGO_TRUCK_RANGE;
                                else if(movableEntity instanceof Tank)
                                    fltMaxRange = Defs.TANK_RANGE;
                                else if(movableEntity instanceof NavalVessel)
                                    fltMaxRange = Defs.NAVAL_RANGE;
                                else if(movableEntity instanceof AirplaneInterface)
                                    fltMaxRange = Defs.GetAircraftRange(((AirplaneInterface)movableEntity).GetAircraftType());

                                txtOutOfRange.setVisibility(fltDistance > game.GetFuelableRange(((FuelableInterface) movableEntity).GetCurrentFuel(), fltMaxRange) ? VISIBLE : GONE);
                                txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, ((FuelableInterface) movableEntity), fltDistance)));
                            }
                            else
                            {
                                txtOutOfRange.setVisibility(GONE);
                                txtFuelUsage.setVisibility(GONE);
                            }
                        }
                        else
                        {
                            GeoCoord geoTarget = Coordinates.get(0);

                            //Not adding a null check on geoTarget here because I want this to cause a crash if this coordinate is null. That shouldn't happen.
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
                                movableTrajectory.setPoints(points);

                                if(movementMarker != null)
                                    movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

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
                                movableTrajectory.setPoints(points);

                                if(movementMarker != null)
                                    movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

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
                                movableTrajectory.setPoints(points);

                                txtFlightTime.setVisibility(VISIBLE);
                            }
                            else if(movable.GetEntity(game) instanceof NavalVessel)
                            {
                                NavalVessel vessel = ((NavalVessel)movable.GetEntity(game));
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
                                {
                                    txtFuelUsage.setVisibility(GONE);
                                }

                                if(movementMarker != null)
                                    movementMarker.setPosition(Utilities.GetLatLng(geoTarget));

                                points.add(Utilities.GetLatLng(geoFrom));
                                points.add(Utilities.GetLatLng(geoTarget));
                                movableTrajectory.setPoints(points);

                                txtFlightTime.setVisibility(VISIBLE);
                            }
                        }
                    }
                    else if(MovableTrajectories != null)
                    {
                        txtFlightTime.setVisibility(GONE);
                        txtOutOfRange.setVisibility(GONE);
                        txtFuelUsage.setVisibility(GONE);

                        if(Coordinates.size() > 1)
                        {
                            for(Entry<EntityPointer, Polyline> entry : MovableTrajectories.entrySet())
                            {
                                List<LatLng> points = new ArrayList<LatLng>();
                                GeoCoord geoStart = game.GetPosition(entry.getKey().GetEntity(game));
                                points.add(Utilities.GetLatLng(geoStart));

                                for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                {
                                    GeoCoord geoCoord = Coordinates.get(i);

                                    if(geoCoord != null)
                                    {
                                        points.add(Utilities.GetLatLng(geoCoord));
                                    }
                                }

                                if(movementMarker != null)
                                {
                                    GeoCoord geoFinal = Coordinates.get(LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()));

                                    if(geoFinal != null)
                                    {
                                        movementMarker.setPosition(Utilities.GetLatLng(geoFinal));
                                    }
                                }

                                entry.getValue().setPoints(points);
                            }

                            txtCommandInstructions.setText(context.getString(R.string.coordinates_selected, Coordinates.size(), lMaxCoordinates));
                        }
                        else
                        {
                            GeoCoord geoTarget = Coordinates.get(0);

                            for(Entry<EntityPointer, Polyline> entry : MovableTrajectories.entrySet())
                            {
                                GeoCoord geoStart = game.GetPosition(entry.getKey().GetEntity(game));

                                entry.getValue().setPoints(Arrays.asList(Utilities.GetLatLng(geoStart), Utilities.GetLatLng(geoTarget)));
                            }

                            if(movementMarker != null)
                                movementMarker.setPosition(Utilities.GetLatLng(geoTarget));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void EntityRemoved(final LaunchEntity entity)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for(Entry<EntityPointer, Polyline> entry : MovableTrajectories.entrySet())
                {
                    if(entry.getKey().GetEntity(game).ApparentlyEquals(entity))
                    {
                        Polyline line = entry.getValue();
                        line.remove();
                        line = null;
                        MovableList.remove(entry.getKey());
                    }
                }
            }
        });
    }
}
