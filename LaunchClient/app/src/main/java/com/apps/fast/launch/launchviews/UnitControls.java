package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.User;
import launch.game.entities.Airbase;
import launch.game.entities.Airplane;
import launch.game.entities.Capturable;
import launch.game.entities.CargoTruck;
import launch.game.entities.Infantry;
import launch.game.entities.LandUnit;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.entities.Ship;
import launch.game.entities.Shipyard;
import launch.game.entities.Structure;
import launch.game.entities.Movable.MoveOrders;
import launch.game.EntityPointer.EntityType;

public class UnitControls extends LaunchView
{
    private LinearLayout lytMovementOptions;
    private LinearLayout lytAttackOptions;
    private LinearLayout btnMovePlayer;
    private LinearLayout btnMoveAircraft;
    private LinearLayout btnMoveTank;
    private LinearLayout btnMoveTruck;
    private LinearLayout btnMoveShip;
    private LinearLayout btnMoveSubmarine;
    private LinearLayout btnAircraftTarget;
    private LinearLayout btnTankTarget;
    private LinearLayout btnInterceptorTarget;
    private LinearLayout btnMissileTarget;

    private GeoCoord geoCoord;
    private MapEntity entity;

    public UnitControls(LaunchClientGame game, MainActivity activity, GeoCoord geoCoord)
    {
        super(game, activity, true);
        this.entity = null;
        this.geoCoord = geoCoord;

        Setup();
    }

    public UnitControls(LaunchClientGame game, MainActivity activity, MapEntity entity)
    {
        super(game, activity, true);
        this.entity = entity;
        this.geoCoord = geoCoord;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_unit_controls, this);

        lytMovementOptions = findViewById(R.id.lytMovementOptions);
        lytAttackOptions = findViewById(R.id.lytAttackOptions);
        btnMovePlayer = findViewById(R.id.btnMovePlayer);
        btnMoveAircraft = findViewById(R.id.btnMoveAircraft);
        btnMoveTank = findViewById(R.id.btnMoveTank);
        btnMoveTruck = findViewById(R.id.btnMoveTruck);
        btnAircraftTarget = findViewById(R.id.btnAircraftTarget);
        btnTankTarget = findViewById(R.id.btnTargetTank);
        btnInterceptorTarget = findViewById(R.id.btnInterceptorTarget);
        btnMissileTarget = findViewById(R.id.btnMissileTarget);
        btnMoveShip = findViewById(R.id.btnMoveShip);
        btnMoveSubmarine = findViewById(R.id.btnMoveSubmarine);

        //Set onclicklisteners.

        btnMoveAircraft.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SelectForAction(geoCoord, entity, EntityType.AIRPLANE, MoveOrders.MOVE);
                }
            }
        });

        btnMoveTank.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SelectForAction(geoCoord, entity, EntityType.TANK, MoveOrders.MOVE);
                }
            }
        });

        btnMoveTruck.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SelectForAction(geoCoord, entity, EntityType.CARGO_TRUCK, MoveOrders.MOVE);
                }
            }
        });

        btnAircraftTarget.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SelectForAction(geoCoord, entity, EntityType.AIRPLANE, MoveOrders.ATTACK);
                }
            }
        });

        btnTankTarget.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SelectForAction(geoCoord, entity, EntityType.TANK, MoveOrders.ATTACK);
                }
            }
        });

        if(entity instanceof Missile || entity instanceof Airplane)
        {
            btnInterceptorTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetInteractionReady())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                    }
                    else
                    {
                        activity.InterceptorSelectForTarget(entity.GetID(), TextUtilities.GetOwnedEntityName(entity, game), entity);
                    }
                }
            });
        }

        btnMissileTarget.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.MissileSelectForTarget(geoCoord != null ? geoCoord : entity.GetPosition(), entity, entity != null ? entity.GetTypeName() : TextUtilities.GetLatLongString(geoCoord.GetLatitude(), geoCoord.GetLongitude()));
                }
            }
        });

        if(geoCoord != null)
        {
            if(game.GetOurPlayer() != null && game.GetOurPlayer().GetBoss())
            {
                btnMovePlayer.setVisibility(VISIBLE);

                btnMovePlayer.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.SpoofLocation(geoCoord);
                        activity.InformationMode(false);
                    }
                });
            }

            btnTankTarget.setVisibility(GONE);
            btnInterceptorTarget.setVisibility(GONE);
        }
        else if(entity != null)
        {
            if(entity instanceof Structure)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);

                    btnInterceptorTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof CargoTruck)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);
                    btnInterceptorTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof LandUnit)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);
                    btnInterceptorTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof Shipyard)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);
                    btnInterceptorTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof NavalVessel)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                    btnMoveTank.setVisibility(GONE);
                    btnMoveTruck.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);
                    btnInterceptorTarget.setVisibility(GONE);
                    btnTankTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof Airplane)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), game.GetOwner(entity)))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    lytMovementOptions.setVisibility(GONE);
                    //btnKamikaze.setVisibility(GONE);
                    btnTankTarget.setVisibility(GONE);
                    btnMissileTarget.setVisibility(GONE);
                }
            }
            else if(entity instanceof Player)
            {
                if(game.WouldBeFriendlyFire(game.GetOurPlayer(), (Player)entity))
                {
                    lytAttackOptions.setVisibility(GONE);
                }
                else
                {
                    btnInterceptorTarget.setVisibility(GONE);
                }
            }
        }

        Update();
    }

    @Override
    public void Update()
    {

    }
}
