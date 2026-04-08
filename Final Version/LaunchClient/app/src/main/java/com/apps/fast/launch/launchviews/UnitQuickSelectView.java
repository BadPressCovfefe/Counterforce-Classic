package com.apps.fast.launch.launchviews;

import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.CargoTruck;
import launch.game.entities.FuelableInterface;
import launch.game.entities.Infantry;
import launch.game.entities.LandUnit;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Tank;
import launch.game.entities.Movable.MoveOrders;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.CargoSystem;

public class UnitQuickSelectView extends LaunchView
{
    private TextView txtTankType;
    private ImageView imgTankType;
    private TextView txtTruckCargo;
    private TextView txtUnitStatus;
    private LandUnit unit;
    private TextView txtFlightTime;
    private GeoCoord geoTarget;
    private MapEntity targetEntity;
    private GeoCoord geoCoordToHandle;
    private MoveOrders order;
    private TextView txtName;
    private TextView txtHP;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     */
    public UnitQuickSelectView(LaunchClientGame game, MainActivity activity, LandUnit unit, GeoCoord geoTarget, MapEntity entity, MoveOrders order)
    {
        super(game, activity, true);
        this.unit = unit;
        this.geoTarget = geoTarget;
        this.targetEntity = entity;
        this.order = order;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_unit_quickselect, this);

        imgTankType = findViewById(R.id.imgTankType);
        txtTankType = findViewById(R.id.txtTankType);
        txtUnitStatus = findViewById(R.id.txtTankStatus);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        txtName = findViewById(R.id.txtName);
        txtHP = findViewById(R.id.txtHPTitle);
        txtTruckCargo = findViewById(R.id.txtTruckCargo);

        geoCoordToHandle = geoTarget != null ? geoTarget : targetEntity.GetPosition();

        txtName.setText(unit.GetName().length() > 0 ? unit.GetName() : context.getString(R.string.unnamed));
        imgTankType.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, unit));
        txtTankType.setText(TextUtilities.GetEntityTypeAndName(unit, game));

        if(unit instanceof CargoTruck)
        {
            CargoTruck truck = (CargoTruck)unit;

            CargoSystem system = truck.GetCargoSystem();

            if(system != null)
            {
                txtTruckCargo.setVisibility(VISIBLE);
                txtTruckCargo.setText(context.getString(R.string.remaining_of_total_string, TextUtilities.GetWeightStringFromKG(system.GetUsedCapacity()), TextUtilities.GetWeightStringFromKG(system.GetCapacity())));
            }
        }

        if(unit.GetGeoTarget() != null && unit.GetMoveOrders() == MoveOrders.MOVE)
        {
            if(unit.HasGeoCoordChain())
            {
                if(unit.GetLastCoordinate().DistanceTo(geoCoordToHandle) <= PositionBuffer())
                {
                    txtUnitStatus.setText(context.getString(R.string.status_enroute));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    txtUnitStatus.setText(context.getString(R.string.status_moving));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
            }
            else
            {
                if(unit.GetGeoTarget().DistanceTo(geoCoordToHandle) <= PositionBuffer())
                {
                    txtUnitStatus.setText(context.getString(R.string.status_enroute));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    txtUnitStatus.setText(context.getString(R.string.status_moving));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
            }
        }
        else if(unit.GetMoveOrders() == MoveOrders.ATTACK)
        {
            if(unit.GetTarget() != null && unit.GetTarget().GetMapEntity(game) != null)
            {
                if(targetEntity != null && unit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                {
                    txtUnitStatus.setText(context.getString(R.string.status_already_attacking));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
                else
                {
                    txtUnitStatus.setText(context.getString(R.string.status_attacking));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
        }
        else if(unit.GetMoveOrders() == MoveOrders.CAPTURE)
        {
            if(unit.GetTarget() != null && unit.GetTarget().GetMapEntity(game) != null)
            {
                if(targetEntity != null && unit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                {
                    txtUnitStatus.setText(context.getString(R.string.status_already_capturing));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
                else
                {
                    txtUnitStatus.setText(context.getString(R.string.status_capturing));
                    txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
        }
        else if(unit.GetMoveOrders() == MoveOrders.LIBERATE)
        {
            if(unit.GetTarget() != null && unit.GetTarget().GetMapEntity(game) != null)
            {
                if(targetEntity != null && unit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                {
                    txtUnitStatus.setText(context.getString(R.string.status_already_liberating));
                    txtUnitStatus.setTextColor(Color.MAGENTA);
                }
                else
                {
                    txtUnitStatus.setText(context.getString(R.string.status_liberating));
                    txtUnitStatus.setTextColor(Color.MAGENTA);
                }
            }
        }
        else if(unit.GetMoveOrders() == MoveOrders.WAIT)
        {
            txtUnitStatus.setText(context.getString(R.string.status_waiting));
            txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
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
                LandUnit updateUnit = null;

                if(unit instanceof Tank)
                {
                    updateUnit = game.GetTank(unit.GetID());
                }
                else if(unit instanceof Infantry)
                {
                    updateUnit = game.GetInfantry(unit.GetID());
                }
                else if(unit instanceof CargoTruck)
                {
                    updateUnit = game.GetCargoTruck(unit.GetID());
                }

                if(updateUnit != null)
                {
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, updateUnit);
                    txtFlightTime.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.LAND_UNIT_SPEED, updateUnit.GetPosition(), geoCoordToHandle))));

                    if(updateUnit.GetGeoTarget() != null && updateUnit.GetMoveOrders() == MoveOrders.MOVE)
                    {
                        if(updateUnit.HasGeoCoordChain())
                        {
                            if(updateUnit.GetLastCoordinate().DistanceTo(geoCoordToHandle) <= PositionBuffer())
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_enroute));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                            }
                            else
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_moving));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                            }
                        }
                        else
                        {
                            if(updateUnit.GetGeoTarget().DistanceTo(geoCoordToHandle) <= PositionBuffer())
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_enroute));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                            }
                            else
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_moving));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                            }
                        }
                    }
                    else if(updateUnit.GetMoveOrders() == MoveOrders.ATTACK)
                    {
                        if(updateUnit.GetTarget() != null && updateUnit.GetTarget().GetMapEntity(game) != null)
                        {
                            if(targetEntity != null && updateUnit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_already_attacking));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            }
                            else
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_attacking));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            }
                        }
                    }
                    else if(updateUnit.GetMoveOrders() == MoveOrders.CAPTURE)
                    {
                        if(updateUnit.GetTarget() != null && updateUnit.GetTarget().GetMapEntity(game) != null)
                        {
                            if(targetEntity != null && updateUnit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_already_capturing));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            }
                            else
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_capturing));
                                txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            }
                        }
                    }
                    else if(unit.GetMoveOrders() == MoveOrders.LIBERATE)
                    {
                        if(unit.GetTarget() != null && unit.GetTarget().GetMapEntity(game) != null)
                        {
                            if(targetEntity != null && unit.GetTarget().GetMapEntity(game).ApparentlyEquals(targetEntity))
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_already_liberating));
                                txtUnitStatus.setTextColor(Color.MAGENTA);
                            }
                            else
                            {
                                txtUnitStatus.setText(context.getString(R.string.status_liberating));
                                txtUnitStatus.setTextColor(Color.MAGENTA);
                            }
                        }
                    }
                    else if(updateUnit.GetMoveOrders() == MoveOrders.WAIT)
                    {
                        txtUnitStatus.setText(context.getString(R.string.status_waiting));
                        txtUnitStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(unit != null)
        {
            if(entity.ApparentlyEquals(unit))
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
