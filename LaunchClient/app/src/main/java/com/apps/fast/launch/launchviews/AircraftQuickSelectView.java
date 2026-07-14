package com.apps.fast.launch.launchviews;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable;
import launch.game.entities.conceptuals.StoredDamagable;
import launch.game.systems.LaunchSystem;
import launch.game.systems.MissileSystem;
import launch.game.entities.Movable.MoveOrders;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

public class AircraftQuickSelectView extends LaunchView
{
    private TextView txtAircraftType;
    private ImageView imgAircraftType;
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
    private LinearLayout lytArmament;
    private MissileSystem interceptorSystem;
    private AircraftQuickSelectView me;

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
        this.me = this;

        if(aircraft.HasMissiles())
        {
            missileSystem = aircraft.GetMissileSystem();
        }

        if(!aircraft.HasMissiles() && aircraft.HasInterceptors())
        {
            interceptorSystem = aircraft.GetInterceptorSystem();
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_aircraft_quickselect, this);

        imgAircraftType = findViewById(R.id.imgAircraftType);
        txtAircraftType = findViewById(R.id.txtAircraftType);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAircraftStatus = findViewById(R.id.txtInfantryStatus);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        txtFuelUsage = findViewById(R.id.txtFuelUsage);
        txtName = findViewById(R.id.txtName);
        lytArmament = findViewById(R.id.lytArmament);

        //Armament layout.
        lytArmament.removeAllViews();

        if(aircraft.GetAircraftType() == EntityPointer.EntityType.BOMBER || aircraft.GetAircraftType() == EntityPointer.EntityType.SSB || aircraft.GetAircraftType() == EntityPointer.EntityType.FIGHTER || aircraft.GetAircraftType() == EntityPointer.EntityType.MULTI_ROLE)
        {
            if(missileSystem != null)
            {
                if(missileSystem.GetReadySlotCount() > 0)
                {
                    //Has missiles. Include them in the list.
                    for(Map.Entry<Integer, Integer> typeCount : missileSystem.GetTypeCounts().entrySet())
                    {
                        MissileType type = game.GetConfig().GetMissileType(typeCount.getKey());

                        LinearLayout row = new LinearLayout(activity);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setGravity(Gravity.CENTER_VERTICAL);

                        ImageView image = new ImageView(activity);

                        image.setImageBitmap(EntityIconBitmaps.GetMissileBitmap(activity, game, type, LaunchGame.Allegiance.UNAFFILIATED, type.GetAssetID()));

                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        imageParams.setMarginEnd(8);
                        image.setLayoutParams(imageParams);

                        TextView text = new TextView(activity);

                        text.setAllCaps(true);
                        text.setText(type.GetName() + " x" + typeCount.getValue());

                        row.addView(image);
                        row.addView(text);

                        lytArmament.addView(row);
                    }
                }

                if(missileSystem.GetEmptySlotCount() > 0)
                {
                    LinearLayout row = new LinearLayout(activity);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);

                    ImageView image = new ImageView(activity);

                    image.setImageResource(R.drawable.button_add);

                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    imageParams.setMarginEnd(8);
                    image.setLayoutParams(imageParams);
                    image.setBackground(context.getDrawable(R.drawable.detail_button));

                    image.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new PurchaseLaunchableView(game, activity, aircraft.GetAirplane(), LaunchSystem.SystemType.STORED_AIRCRAFT_MISSILES, geoTarget, targetEntity, order));
                        }
                    });

                    TextView text = new TextView(activity);

                    text.setAllCaps(true);
                    text.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    text.setText(context.getString(R.string.empty) + " x" + missileSystem.GetEmptySlotCount());

                    text.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new PurchaseLaunchableView(game, activity, aircraft.GetAirplane(), LaunchSystem.SystemType.STORED_AIRCRAFT_MISSILES, geoTarget, targetEntity, order));
                        }
                    });

                    row.addView(image);
                    row.addView(text);

                    lytArmament.addView(row);
                }
            }

            if(interceptorSystem != null)
            {
                if(interceptorSystem.GetReadySlotCount() > 0)
                {
                    //Has interceptors. Include them in the list.
                    for(Map.Entry<Integer, Integer> typeCount : interceptorSystem.GetTypeCounts().entrySet())
                    {
                        InterceptorType type = game.GetConfig().GetInterceptorType(typeCount.getKey());

                        LinearLayout row = new LinearLayout(activity);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setGravity(Gravity.CENTER_VERTICAL);

                        ImageView image = new ImageView(activity);

                        image.setImageBitmap(EntityIconBitmaps.GetInterceptorBitmap(activity, game, type, LaunchGame.Allegiance.UNAFFILIATED, type.GetAssetID()));

                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        imageParams.setMarginEnd(8);
                        image.setLayoutParams(imageParams);

                        TextView text = new TextView(activity);

                        text.setAllCaps(true);
                        text.setText(type.GetName() + " x" + typeCount.getValue());

                        row.addView(image);
                        row.addView(text);

                        lytArmament.addView(row);
                    }
                }

                if(interceptorSystem.GetEmptySlotCount() > 0)
                {
                    LinearLayout row = new LinearLayout(activity);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);

                    ImageView image = new ImageView(activity);

                    image.setImageResource(R.drawable.button_add);

                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    imageParams.setMarginEnd(8);
                    image.setLayoutParams(imageParams);
                    image.setBackground(context.getDrawable(R.drawable.detail_button));

                    image.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new PurchaseLaunchableView(game, activity, aircraft.GetAirplane(), LaunchSystem.SystemType.STORED_AIRCRAFT_MISSILES, geoTarget, targetEntity, order));
                        }
                    });

                    TextView text = new TextView(activity);

                    text.setAllCaps(true);
                    text.setText(context.getString(R.string.empty) + " x" + interceptorSystem.GetEmptySlotCount());

                    text.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new PurchaseLaunchableView(game, activity, aircraft.GetAirplane(), LaunchSystem.SystemType.STORED_AIRCRAFT_MISSILES, geoTarget, targetEntity, order));
                        }
                    });

                    row.addView(image);
                    row.addView(text);

                    lytArmament.addView(row);
                }
            }
        }
        else if(aircraft.GetAircraftType() == EntityPointer.EntityType.REFUELER)
        {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView image = new ImageView(activity);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            textParams.setMarginEnd(8);

            TextView text = new TextView(activity);

            text.setAllCaps(true);
            text.setText(context.getString(R.string.none));
            text.setLayoutParams(textParams);

            row.addView(image);
            row.addView(text);

            lytArmament.addView(row);
        }
        else if(aircraft.GetAircraftType() == EntityPointer.EntityType.ATTACK_AIRCRAFT)
        {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView image = new ImageView(activity);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            textParams.setMarginEnd(8);

            TextView text = new TextView(activity);

            text.setAllCaps(true);
            text.setText(context.getString(R.string.cannon));
            text.setLayoutParams(textParams);

            row.addView(image);
            row.addView(text);

            lytArmament.addView(row);
        }

        txtName.setText(aircraft.GetName().length() > 0 ? aircraft.GetName() : context.getString(R.string.unnamed));

        switch(aircraft.GetAircraftType())
        {
            case BOMBER:
            {
                imgAircraftType.setImageResource(R.drawable.build_bomber);
            }
            break;

            case FIGHTER:
            {
                imgAircraftType.setImageResource(R.drawable.build_fighter);
            }
            break;

            case ATTACK_AIRCRAFT:
            {
                imgAircraftType.setImageResource(R.drawable.build_ground_attack);
            }
            break;

            case REFUELER:
            {
                imgAircraftType.setImageResource(R.drawable.build_refueler);
            }
            break;

            case MULTI_ROLE:
            {
                imgAircraftType.setImageResource(R.drawable.build_multi_role);
            }
            break;

            case SSB:
            {
                imgAircraftType.setImageResource(R.drawable.build_ssb);
            }
            break;
        }

        txtAircraftType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)aircraft, game));

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

                    txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(aircraft.GetAircraftType()), flyingAircraft.GetPosition(), geoTarget))));
                    txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));

                    /*For setting up fuel level readout.*/
                    txtFuelLevel = findViewById(R.id.txtFuelLevel);
                    TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraft);

                    TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);
                }
                else
                {
                    GeoCoord geoStart = aircraft.GetHomeBase().GetMapEntity(game).GetPosition();
                    txtFlightTime.setText(context.getString(R.string.flight_time_target, TextUtilities.GetTimeAmount(game.GetTravelTime(Defs.GetAircraftSpeed(aircraft.GetAircraftType()), geoStart, geoTarget))));
                    txtFuelUsage.setText(context.getString(R.string.fuel_percent_for_trip, TextUtilities.GetFuelUsageString(game, aircraft, geoTarget)));

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
