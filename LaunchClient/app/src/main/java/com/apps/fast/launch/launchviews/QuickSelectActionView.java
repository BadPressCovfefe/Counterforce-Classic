package com.apps.fast.launch.launchviews;

import static launch.comm.LaunchSession.MoveOrder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;

import java.util.ArrayList;
import java.util.List;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.ArtilleryInterface;
import launch.game.entities.CargoTruck;
import launch.game.entities.Infantry;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.Ship;
import launch.game.entities.Tank;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.StoredAirplane;

public class QuickSelectActionView extends LaunchView
{
    private LinearLayout lytAvailableUnits;
    private TextView btnCancel;

    private GeoCoord geoTarget;
    private MapEntity targetEntity;
    private GeoCoord geoCoordToHandle;
    private MoveOrders order;
    private TextView txtNoUnits;
    private EntityType type;

    public QuickSelectActionView(LaunchClientGame game, MainActivity activity, GeoCoord geoTarget, MapEntity entity, EntityType type, MoveOrders order)
    {
        super(game, activity, true);
        this.geoTarget = geoTarget;
        this.type = type;
        this.targetEntity = entity;
        this.order = order;

        Setup();
        Update();
        RebuildList();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_select_aircraft, this);

        txtNoUnits = (TextView)findViewById(R.id.txtNoAircraft);
        lytAvailableUnits = findViewById(R.id.lytAvailableAircrafts);
        btnCancel = findViewById(R.id.btnCancel);

        geoCoordToHandle = geoTarget != null ? geoTarget : targetEntity.GetPosition();

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });
    }

    @Override
    public void Update()
    {
        //Removed due to performance issues. TO DO: Respond only to changes that affect the entries.
        //RebuildList();
    }

    private void RebuildList()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Player ourPlayer = game.GetOurPlayer();
                lytAvailableUnits.removeAllViews();

                switch(type)
                {
                    case AIRPLANE:
                    {
                        List<AirplaneInterface> Aircrafts = new ArrayList<>();

                        boolean bAircraftsAvailable = false;

                        if(order == MoveOrders.MOVE)
                        {
                            for(Airbase airbase : game.GetAirbases())
                            {
                                if(game.EntityIsFriendly(airbase, game.GetOurPlayer()))
                                {
                                    if(!airbase.GetAircraftSystem().GetStoredAirplanes().isEmpty())
                                    {
                                        for(AirplaneInterface storedAircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                                        {
                                            if(storedAircraft.GetOwnerID() == game.GetOurPlayerID() && ((StoredAirplane)storedAircraft).DoneBuilding() && !storedAircraft.GetAirplane().ApparentlyEquals(targetEntity))
                                            {
                                                Aircrafts.add(storedAircraft);
                                            }
                                        }
                                    }
                                }
                            }

                            for(Ship ship : game.GetShips())
                            {
                                if(game.EntityIsFriendly(ship, game.GetOurPlayer()))
                                {
                                    if(ship.HasAircraft())
                                    {
                                        if(!ship.GetAircraftSystem().GetStoredAirplanes().isEmpty())
                                        {
                                            for(AirplaneInterface storedAircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                                            {
                                                if(storedAircraft.GetOwnerID() == game.GetOurPlayerID() && ((StoredAirplane)storedAircraft).DoneBuilding() && !storedAircraft.GetAirplane().ApparentlyEquals(targetEntity))
                                                {
                                                    Aircrafts.add(storedAircraft);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        for(final Airplane aircraft : game.GetAirplanes())
                        {
                            if(aircraft.GetOwnerID() == ourPlayer.GetID() && !aircraft.GetAirplane().ApparentlyEquals(targetEntity))
                            {
                                if(order == MoveOrders.MOVE && aircraft.GetGeoTarget() != null && aircraft.GetMoveOrders() == Tank.MoveOrders.MOVE)
                                {
                                    if(aircraft.HasGeoCoordChain())
                                    {
                                        if(aircraft.GetLastCoordinate().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Aircrafts.add(aircraft);
                                        }
                                    }
                                    else
                                    {
                                        if(aircraft.GetGeoTarget().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Aircrafts.add(aircraft);
                                        }
                                    }
                                }
                                else if(order == MoveOrders.ATTACK)
                                {
                                    if(targetEntity != null)
                                    {
                                        if(game.TargetIsLegal(aircraft.GetPointer(), targetEntity.GetPointer()))
                                        {
                                            Aircrafts.add(aircraft);
                                        }
                                    }
                                }
                                else
                                {
                                    Aircrafts.add(aircraft);
                                }
                            }
                        }

                        for(final AirplaneInterface aircraft : Aircrafts)
                        {
                            if(aircraft.GetOwnerID() == ourPlayer.GetID())
                            {
                                if(aircraft.Flying() || aircraft.GetHomeBase().GetMapEntity(game) instanceof Ship || ((Airbase)aircraft.GetHomeBase().GetMapEntity(game)).GetOnline())
                                {
                                    if(game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetAircraftType())) > game.GetAircraftPosition(aircraft).DistanceTo(geoCoordToHandle))
                                    {
                                        bAircraftsAvailable = true;

                                        AircraftQuickSelectView aircraftView = new AircraftQuickSelectView(game, activity, aircraft, geoCoordToHandle, targetEntity, order);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                                        layoutParams.weight = 1.0f;
                                        aircraftView.setLayoutParams(layoutParams);
                                        lytAvailableUnits.addView(aircraftView);

                                        aircraftView.setOnClickListener(new OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                switch(order)
                                                {
                                                    case ATTACK:
                                                    {
                                                        activity.DesignateAttackTarget((Airplane)aircraft, targetEntity, false);
                                                    }
                                                    break;

                                                    case MOVE: activity.DesignateMoveToPosition(aircraft.GetAirplane(), geoCoordToHandle, targetEntity); break;
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        txtNoUnits.setVisibility(bAircraftsAvailable ? GONE : VISIBLE);
                    }
                    break;

                    case TANK:
                    {
                        List<Tank> Tanks = new ArrayList<>();

                        boolean bTanksAvailable = false;

                        for(final Tank tank : game.GetTanks())
                        {
                            if(tank.GetOwnerID() == ourPlayer.GetID() && !tank.ApparentlyEquals(targetEntity))
                            {
                                //If the order is ATTACK, SPAAGS and tanks already attacking this target should be excluded.
                                if(order == MoveOrders.ATTACK)
                                {
                                    if(!tank.IsASPAAG())
                                    {
                                        if(tank.GetMoveOrders() == MoveOrders.ATTACK)
                                        {
                                            if(tank.GetTarget() != null)
                                            {
                                                MapEntity target = tank.GetTarget().GetMapEntity(game);

                                                if(targetEntity != null && (target == null || !targetEntity.ApparentlyEquals(target)))
                                                {
                                                    Tanks.add(tank);
                                                }
                                            }
                                            else
                                            {
                                                Tanks.add(tank);
                                            }
                                        }
                                        else
                                        {
                                            Tanks.add(tank);
                                        }
                                    }
                                }
                                else if(order == MoveOrders.MOVE && tank.GetGeoTarget() != null && tank.GetMoveOrders() == Tank.MoveOrders.MOVE)
                                {
                                    if(tank.HasGeoCoordChain())
                                    {
                                        if(tank.GetLastCoordinate().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Tanks.add(tank);
                                        }
                                    }
                                    else
                                    {
                                        if(tank.GetGeoTarget().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Tanks.add(tank);
                                        }
                                    }
                                }
                                else
                                {
                                    Tanks.add(tank);
                                }
                            }
                        }

                        for(final Tank tank : Tanks)
                        {
                            bTanksAvailable = true;

                            UnitQuickSelectView tankView = new UnitQuickSelectView(game, activity, tank, geoCoordToHandle, targetEntity, order);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 1.0f;
                            tankView.setLayoutParams(layoutParams);
                            lytAvailableUnits.addView(tankView);

                            tankView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    switch(order)
                                    {
                                        case ATTACK: activity.DesignateAttackTarget(tank, targetEntity, false); break;
                                        case MOVE: activity.DesignateMoveToPosition(tank, geoCoordToHandle, null); break;
                                    }
                                }
                            });
                        }

                        txtNoUnits.setVisibility(bTanksAvailable ? GONE : VISIBLE);
                        txtNoUnits.setText(context.getString(R.string.no_available_tanks));
                    }
                    break;

                    case INFANTRY:
                    {
                        List<Infantry> Infantries = new ArrayList<>();

                        boolean bInfantriesAvailable = false;

                        for(final Infantry infantry : game.GetInfantries())
                        {
                            if(infantry.GetOwnerID() == ourPlayer.GetID() && !infantry.ApparentlyEquals(targetEntity))
                            {
                                if(order == MoveOrders.MOVE && infantry.GetGeoTarget() != null && infantry.GetMoveOrders() == Tank.MoveOrders.MOVE)
                                {
                                    if(infantry.HasGeoCoordChain())
                                    {
                                        if(infantry.GetLastCoordinate().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Infantries.add(infantry);
                                        }
                                    }
                                    else
                                    {
                                        if(infantry.GetGeoTarget().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Infantries.add(infantry);
                                        }
                                    }
                                }
                                else
                                {
                                    Infantries.add(infantry);
                                }
                            }
                        }

                        for(final Infantry infantry : Infantries)
                        {
                            bInfantriesAvailable = true;

                            UnitQuickSelectView unitView = new UnitQuickSelectView(game, activity, infantry, geoCoordToHandle, targetEntity, order);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 1.0f;
                            unitView.setLayoutParams(layoutParams);
                            lytAvailableUnits.addView(unitView);

                            unitView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    switch(order)
                                    {
                                        case ATTACK: activity.DesignateAttackTarget(infantry, targetEntity, false); break;
                                        case CAPTURE: activity.DesignateCaptureTarget(infantry); break;
                                        case MOVE: activity.DesignateMoveToPosition(infantry, geoCoordToHandle, null); break;
                                    }
                                }
                            });
                        }

                        txtNoUnits.setVisibility(bInfantriesAvailable ? GONE : VISIBLE);
                        txtNoUnits.setText(context.getString(R.string.no_available_infantries));
                    }
                    break;

                    case CARGO_TRUCK:
                    {
                        List<CargoTruck> Trucks = new ArrayList<>();

                        boolean bTrucksAvailable = false;

                        for(final CargoTruck truck : game.GetCargoTrucks())
                        {
                            if(truck.GetOwnerID() == ourPlayer.GetID() && !truck.ApparentlyEquals(targetEntity))
                            {
                                if(order == MoveOrders.MOVE && truck.GetGeoTarget() != null && truck.GetMoveOrders() == Tank.MoveOrders.MOVE)
                                {
                                    if(truck.HasGeoCoordChain())
                                    {
                                        if(truck.GetLastCoordinate().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Trucks.add(truck);
                                        }
                                    }
                                    else
                                    {
                                        if(truck.GetGeoTarget().DistanceTo(geoCoordToHandle) > PositionBuffer())
                                        {
                                            Trucks.add(truck);
                                        }
                                    }
                                }
                                else
                                {
                                    Trucks.add(truck);
                                }
                            }
                        }

                        for(final CargoTruck truck : Trucks)
                        {
                            bTrucksAvailable = true;

                            UnitQuickSelectView unitView = new UnitQuickSelectView(game, activity, truck, geoCoordToHandle, targetEntity, order);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 1.0f;
                            unitView.setLayoutParams(layoutParams);
                            lytAvailableUnits.addView(unitView);

                            unitView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    activity.DesignateMoveToPosition(truck, geoCoordToHandle, null);
                                }
                            });
                        }

                        txtNoUnits.setVisibility(bTrucksAvailable ? GONE : VISIBLE);
                        txtNoUnits.setText(context.getString(R.string.no_available_trucks));
                    }
                    break;

                    case ARTILLERY_GUN:
                    {
                        List<ArtilleryInterface> Artillery = new ArrayList<>();

                        boolean bArtilleryAvailable = false;

                        for(final Ship ship : game.GetShips())
                        {
                            if(ship.GetOwnerID() == ourPlayer.GetID() && ship.HasArtillery())
                            {
                                if(ship.GetArtillerySystem().GetReadySlotCount() > 0 && !ship.ApparentlyEquals(targetEntity))
                                {
                                    if(ship.GetPosition().DistanceTo(geoCoordToHandle) <= Defs.ARTILLERY_RANGE)
                                        Artillery.add(ship);
                                }
                            }
                        }

                        for(final ArtilleryGun artillery : game.GetArtilleryGuns())
                        {
                            if(artillery.GetOwnerID() == ourPlayer.GetID() && !artillery.ApparentlyEquals(targetEntity))
                            {
                                if(artillery.GetMissileSystem().GetReadySlotCount() > 0 && artillery.GetPosition().DistanceTo(geoCoordToHandle) <= Defs.ARTILLERY_RANGE)
                                    Artillery.add(artillery);
                            }
                        }

                        for(final Tank tank : game.GetHowitzers())
                        {
                            if(tank.GetOwnerID() == ourPlayer.GetID() && !tank.ApparentlyEquals(targetEntity))
                            {
                                if(tank.GetMissileSystem().GetReadySlotCount() > 0 && tank.GetPosition().DistanceTo(geoCoordToHandle) <= Defs.ARTILLERY_RANGE)
                                    Artillery.add(tank);
                            }
                        }

                        for(final ArtilleryInterface artillery : Artillery)
                        {
                            bArtilleryAvailable = true;

                            ArtilleryQuickSelectView artilleryView = new ArtilleryQuickSelectView(game, activity, artillery, geoCoordToHandle);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 1.0f;
                            artilleryView.setLayoutParams(layoutParams);
                            lytAvailableUnits.addView(artilleryView);

                            artilleryView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    activity.DesignateArtilleryTarget((MapEntity)artillery, geoCoordToHandle, targetEntity);
                                }
                            });
                        }

                        txtNoUnits.setVisibility(bArtilleryAvailable ? GONE : VISIBLE);
                        txtNoUnits.setText(context.getString(R.string.no_available_artillery));
                    }
                    break;
                }
            }
        });
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
