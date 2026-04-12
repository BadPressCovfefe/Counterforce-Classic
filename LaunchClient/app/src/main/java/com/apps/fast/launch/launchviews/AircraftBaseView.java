package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.entities.conceptuals.StoredEntity;
import launch.game.systems.CargoSystem;
import launch.game.systems.MissileSystem;

/**
 * Created by tobster on 09/11/15.
 */
public class AircraftBaseView extends LaunchView
{
    private ImageView imgAircraftType;
    private TextView txtAircraftType;
    private TextView txtFuelLevel;
    private TextView txtAircraftStatus;
    private LinearLayout btnMove;
    private AirplaneInterface aircraft;
    private LinearLayout lytFuel;
    private TextView txtName;
    private TextView txtHP;
    private ImageView imgKick;
    private LinearLayout btnCapture;
    private TextView txtMissiles;
    private ImageView imgMissiles;
    private LinearLayout lytMissiles;
    private MissileSystem missileSystem;
    private TextView txtInterceptors;
    private ImageView imgInterceptors;
    private LinearLayout lytInterceptors;
    private MissileSystem interceptorSystem;
    private boolean bWeOwnIt = false;
    private boolean bWeOwnHost = false;
    private int lID;

    private Player ourPlayer;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param bCargo true if this view is for transfering cargo.
     */
    public AircraftBaseView(LaunchClientGame game, MainActivity activity, AirplaneInterface aircraft, boolean bCargo)
    {
        super(game, activity, true);

        this.aircraft = aircraft;
        this.lID = aircraft.GetID();

        if(aircraft.GetOwnerID() == game.GetOurPlayerID())
            bWeOwnIt = true;

        ourPlayer = game.GetOurPlayer();

        if(!bCargo)
        {
            MapEntity host = aircraft.GetHomeBase().GetMapEntity(game);

            if(host != null)
            {
                bWeOwnHost = host.GetOwnedBy(ourPlayer.GetID());
            }
        }

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
        inflate(context, R.layout.view_aircraft_base, this);

        imgAircraftType = findViewById(R.id.imgAircraftType);
        imgMissiles = findViewById(R.id.imgArsenalType);
        txtAircraftType = findViewById(R.id.txtAircraftType);
        txtMissiles = findViewById(R.id.txtArmaments);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAircraftStatus = findViewById(R.id.txtInfantryStatus);
        btnMove = findViewById(R.id.btnMove);
        txtHP = findViewById(R.id.txtHPTitle);
        imgKick = findViewById(R.id.imgKick);
        btnCapture = findViewById(R.id.btnCapture);

        imgInterceptors = findViewById(R.id.imgArsenalType2);
        txtInterceptors = findViewById(R.id.txtArmaments2);
        lytInterceptors = findViewById(R.id.lytArsenal2);

        txtName = findViewById(R.id.txtName);
        lytMissiles = findViewById(R.id.lytArsenal);
        lytFuel = findViewById(R.id.lytFuel);

        TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);

        imgAircraftType.setImageBitmap(EntityIconBitmaps.GetAircraftBitmap(context, game, aircraft));

        txtAircraftType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)aircraft, game));

        if(((StoredEntity)aircraft).DoneBuilding())
        {
            if(aircraft.HasMissiles())
            {
                imgMissiles.setImageResource(R.drawable.marker_missile);
                lytMissiles.setVisibility(VISIBLE);
            }
            else
            {
                lytMissiles.setVisibility(GONE);
            }

            if(aircraft.HasInterceptors())
            {
                imgInterceptors.setImageResource(R.drawable.marker_interceptor);
                lytInterceptors.setVisibility(VISIBLE);
            }
            else
            {
                lytInterceptors.setVisibility(GONE);
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
            else
            {
                lytMissiles.setVisibility(GONE);
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
            else
            {
                lytInterceptors.setVisibility(GONE);
            }

            /*For setting up fuel level readout.*/
            txtFuelLevel = findViewById(R.id.txtFuelLevel);
            TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraft);
        }
        else
        {
            lytMissiles.setVisibility(GONE);
            lytInterceptors.setVisibility(GONE);
            lytFuel.setVisibility(GONE);
            txtHP.setVisibility(GONE);
            btnMove.setVisibility(GONE);
            txtName.setVisibility(GONE);
            imgKick.setVisibility(GONE);
        }

        if(bWeOwnIt && !bWeOwnHost)
        {
            btnMove.setVisibility(VISIBLE);

            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.MoveOrderMode(((LaunchEntity)aircraft).GetPointer(), null);
                }
            });

            imgKick.setVisibility(GONE);
        }
        else if(bWeOwnIt && bWeOwnHost)
        {
            imgKick.setVisibility(GONE);
            btnMove.setVisibility(VISIBLE);
            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.MoveOrderMode(((LaunchEntity)aircraft).GetPointer(), null);
                }
            });
        }
        else if(bWeOwnHost)
        {
            btnMove.setVisibility(GONE);
            imgKick.setVisibility(VISIBLE);
            btnCapture.setVisibility(VISIBLE);

            imgKick.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.kick_aircraft_confirm));
                    launchDialog.SetOnClickYes(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.KickAircraft(((LaunchEntity)aircraft).GetPointer());
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.SetOnClickNo(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            });

            btnCapture.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.capture_aircraft_confirm));
                    launchDialog.SetOnClickYes(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.CaptureEntity(((LaunchEntity)aircraft).GetPointer());
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.SetOnClickNo(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            });
        }
        else
        {
            lytMissiles.setVisibility(GONE);
            lytInterceptors.setVisibility(GONE);
            lytFuel.setVisibility(GONE);
            txtAircraftStatus.setVisibility(GONE);
            txtHP.setVisibility(GONE);
            txtName.setVisibility(GONE);
            btnMove.setVisibility(GONE);
            imgKick.setVisibility(GONE);
            btnCapture.setVisibility(GONE);
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
                if(aircraft instanceof StoredAirplane)
                {
                    StoredAirplane aircraft = game.GetStoredAirplane(lID);

                    if(aircraft != null)
                    {
                        TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);

                        if(aircraft.DoneBuilding())
                        {
                            TextUtilities.AssignHealthStringAndAppearance(txtHP, aircraft);

                            if(aircraft.HasMissiles())
                            {
                                missileSystem = aircraft.GetMissileSystem();
                            }

                            if(aircraft.HasInterceptors())
                            {
                                interceptorSystem = aircraft.GetInterceptorSystem();
                            }

                            String strName = Utilities.GetEntityName(context, aircraft);

                            txtName.setText(strName);

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
                        }
                        else
                        {
                            lytMissiles.setVisibility(GONE);
                            lytInterceptors.setVisibility(GONE);
                            lytFuel.setVisibility(GONE);
                            txtHP.setVisibility(GONE);
                            btnMove.setVisibility(GONE);
                            txtName.setVisibility(GONE);
                        }
                    }
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
            if(entity.ApparentlyEquals((LaunchEntity)aircraft))
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
}
