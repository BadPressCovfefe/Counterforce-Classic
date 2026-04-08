package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.controls.CargoSystemControl;
import com.apps.fast.launch.launchviews.controls.MissileSystemControl;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable;
import launch.game.entities.SAMSite;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.utilities.LaunchUtilities;

public class AirplaneView extends LaunchView implements LaunchUICommon.AircraftInfoProvider
{
    private AirplaneInterface aircraftShadow;
    public boolean bFromAirbase;

    protected TextView txtToTarget;
    protected View viewToTarget;
    private LinearLayout btnLaunchInterceptor;

    protected TextView txtSpeed;
    private TextView txtFuelTitle;
    private TextView txtFuelLevel;
    private TextView txtAircraftStatus;
    private LinearLayout btnRefuel;
    private LinearLayout btnProvideFuel;
    private LinearLayout btnMove;
    private LinearLayout btnScan;
    private ImageView imgScan;
    private LinearLayout btnReturn;
    private LinearLayout btnKamikaze;
    private LinearLayout btnSetNewHomebase;
    private LinearLayout btnElectronicWarfare;
    private LinearLayout btnAttack;
    private LinearLayout lytControls;
    private FrameLayout lytAircraftSystem;
    protected TextView txtAircraftTitle;
    //private TextView txtFlaresTitle;
    //private TextView txtFlares;
    private TextView txtOrphan;
    protected LinearLayout btnToggleReturn;
    private ImageView imgToggleReturn;
    private TextView txtHP;

    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    protected LinearLayout btnCeaseFire;

    protected ImageView imgAircraft;

    protected TextView txtMissiles;
    protected FrameLayout lytMissiles;
    protected TextView txtInterceptors;
    protected FrameLayout lytInterceptors;
    protected TextView txtCargo;
    protected FrameLayout lytCargo;

    protected LaunchView missileSystem;  //Missiles
    protected LaunchView interceptorSystem; //Interceptors
    protected LaunchView cargoSystem; //Cargo

    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private View viewMode;

    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    private boolean bOwnedByPlayer;
    private boolean bFlying;

    private GeoCoord geoPosition;
    private MapEntity homebase;
    private boolean bInAirbase = false;

    public AirplaneView(LaunchClientGame game, MainActivity activity, AirplaneInterface aircraft, boolean bFromAirbase)
    {
        super(game, activity, true);
        this.aircraftShadow = aircraft;

        bFlying = aircraftShadow.Flying();
        bOwnedByPlayer = aircraftShadow.GetOwnerID() == game.GetOurPlayerID();
        homebase = aircraft.GetHomeBase().GetMapEntity(game);

        if(bFlying)
        {
            geoPosition = ((Airplane)aircraft.GetAirplane()).GetPosition();
        }
        else
        {
            geoPosition = homebase.GetPosition();
            bInAirbase = homebase instanceof Airbase;
        }

        if(aircraft.HasMissiles())
        {
            missileSystem = new MissileSystemControl(game, activity, aircraftShadow.GetID(), aircraftShadow.GetAirplane(), true);
        }

        if(aircraft.HasInterceptors())
        {
            interceptorSystem = new MissileSystemControl(game, activity, aircraftShadow.GetID(), aircraftShadow.GetAirplane(), false);
        }

        if(aircraft.HasCargo())
        {
            cargoSystem = new CargoSystemControl(game, activity, (HaulerInterface)aircraftShadow.GetAirplane());
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_aircraft, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        txtToTarget = (TextView) findViewById(R.id.txtToTarget);
        viewToTarget = findViewById(R.id.viewToTarget);
        btnLaunchInterceptor = (LinearLayout) findViewById(R.id.btnLaunchInterceptor);
        txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        imgAircraft = (ImageView) findViewById(R.id.imgAircraft);
        txtFuelTitle = findViewById(R.id.txtFuelTitle);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAircraftStatus = findViewById(R.id.txtInfantryStatus);
        btnRefuel = findViewById(R.id.btnRefuel);
        btnProvideFuel = findViewById(R.id.btnProvideFuel);
        btnMove = findViewById(R.id.btnMove);
        btnScan = findViewById(R.id.btnScan);
        imgScan = findViewById(R.id.imgScan);
        btnReturn = findViewById(R.id.btnDigIn);
        btnKamikaze = findViewById(R.id.btnKamikaze);
        btnAttack = findViewById(R.id.btnAttack);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        lytControls = findViewById(R.id.lytControls);
        txtAircraftTitle = findViewById(R.id.txtAircraftTitle);
        //txtFlaresTitle = findViewById(R.id.txtFlaresTitle);
        //txtFlares = findViewById(R.id.txtFlares);
        txtOrphan = findViewById(R.id.txtOrphan);
        btnToggleReturn = findViewById(R.id.btnToggleReturn);
        imgToggleReturn = findViewById(R.id.imgToggleReturn);
        btnSetNewHomebase = findViewById(R.id.btnSetNewHomebase);
        btnElectronicWarfare = findViewById(R.id.btnElectronicWarfare);
        txtHP = findViewById(R.id.txtHPTitle);

        txtMissiles = findViewById(R.id.txtMissiles);
        lytMissiles = findViewById(R.id.lytMissiles);
        txtInterceptors = findViewById(R.id.txtInterceptors);
        lytInterceptors = findViewById(R.id.lytInterceptors);
        txtCargo = findViewById(R.id.txtCargo);
        lytCargo = findViewById(R.id.lytCargo);

        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);

        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);
        viewMode = findViewById(R.id.viewMode);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        View viewAdmin = (View)findViewById(R.id.viewAdmin);
        LinearLayout btnAdminDelete = (LinearLayout)findViewById(R.id.btnAdminDelete);

        if(game.GetOurPlayer().GetIsAnAdmin())
        {
            viewAdmin.setVisibility(VISIBLE);
            btnAdminDelete.setVisibility(VISIBLE);

            btnAdminDelete.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.admin_delete_confirm, ((LaunchEntity)GetCurrentAircraft()).GetTypeName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AdminDelete(((LaunchEntity)GetCurrentAircraft()).GetPointer());
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
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
            viewAdmin.setVisibility(GONE);
            btnAdminDelete.setVisibility(GONE);
        }

        txtAircraftTitle.setText(TextUtilities.GetOwnedEntityName(aircraftShadow.GetAirplane(), game));
        imgAircraft.setImageBitmap(EntityIconBitmaps.GetAircraftBitmap(context, game, aircraftShadow));

        /*For setting up fuel level readout.*/
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraftShadow);
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, Defs.GetAircraftSpeed(aircraftShadow.GetAircraftType()));

        if(bOwnedByPlayer)
        {
            txtName.setVisibility(GONE);
            txtNameEdit.setText(aircraftShadow.GetName());
            //txtFlares.setText(String.valueOf(aircraftShadow.GetFlareCount()));

            if(aircraftShadow.HasInterceptors() || aircraftShadow.HasCannon() || aircraftShadow.GroundAttack())
            {
                btnAttack.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(bInAirbase && homebase != null && !aircraftShadow.Flying())
                        {
                            if(!((Airbase) homebase).GetOnline())
                            {
                                activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_takeoff));
                            }
                            else
                                activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                        }
                        else
                            activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                    }
                });
            }
            else
            {
                btnAttack.setVisibility(GONE);
            }

            txtNameButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ExpandView();
                    txtNameButton.setVisibility(GONE);
                    lytNameEdit.setVisibility(VISIBLE);
                }
            });

            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bInAirbase && homebase != null && !aircraftShadow.Flying())
                    {
                        if(!((Airbase) homebase).GetOnline())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_takeoff));
                        }
                        else
                            activity.MoveOrderMode(aircraftShadow.GetAirplane().GetPointer(), null);
                    }
                    else
                        activity.MoveOrderMode(aircraftShadow.GetAirplane().GetPointer(), null);
                }
            });

            imgToggleReturn.setImageResource(aircraftShadow.WillAutoReturn() ? R.drawable.button_manual_return : R.drawable.button_auto_return);

            btnToggleReturn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(aircraftShadow.WillAutoReturn())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.manual_return_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.ToggleAircraftReturn(aircraftShadow.GetAirplane().GetPointer());
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                    else
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.auto_return_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.ToggleAircraftReturn(aircraftShadow.GetAirplane().GetPointer());
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                }
            });

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(((LaunchEntity)aircraftShadow).GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });

            if(missileSystem != null)
            {
                lytMissiles.addView(missileSystem);
                lytMissiles.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));
            }
            else
            {
                lytMissiles.setVisibility(GONE);
                txtMissiles.setVisibility(GONE);
            }

            if(interceptorSystem != null)
            {
                lytInterceptors.addView(interceptorSystem);
                lytInterceptors.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));

                lytMode.setVisibility(VISIBLE);
                viewMode.setVisibility(VISIBLE);

                if(aircraftShadow.GetAuto())
                {
                    flasherAuto.TurnGreen(context);
                }
                else
                {
                    flasherAuto.TurnOff(context);
                }

                if(aircraftShadow.GetSemiAuto())
                {
                    flasherSemi.TurnGreen(context);
                }
                else
                {
                    flasherSemi.TurnOff(context);
                }

                if(aircraftShadow.GetManual())
                {
                    flasherManual.TurnGreen(context);
                }
                else
                {
                    flasherManual.TurnOff(context);
                }

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetAirplaneInterface(aircraftShadow.GetID()).GetAuto())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderSAMControl();
                            launchDialog.SetMessage(context.getString(R.string.confirm_auto));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();

                                    game.SetSAMSiteMode(aircraftShadow.GetAirplane().GetPointer(), SAMSite.MODE_AUTO);
                                }
                            });
                            launchDialog.SetOnClickNo(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                }
                            });
                            launchDialog.show(activity.getFragmentManager(), "");
                        }
                    }
                });

                btnSemi.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetAirplaneInterface(aircraftShadow.GetID()).GetSemiAuto())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderSAMControl();
                            launchDialog.SetMessage(context.getString(R.string.confirm_semi));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();

                                    game.SetSAMSiteMode(aircraftShadow.GetAirplane().GetPointer(), SAMSite.MODE_SEMI_AUTO);
                                }
                            });
                            launchDialog.SetOnClickNo(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                }
                            });
                            launchDialog.show(activity.getFragmentManager(), "");
                        }
                    }
                });

                btnManual.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetAirplaneInterface(aircraftShadow.GetID()).GetManual())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderSAMControl();
                            launchDialog.SetMessage(context.getString(R.string.confirm_manual));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();

                                    game.SetSAMSiteMode(aircraftShadow.GetAirplane().GetPointer(), SAMSite.MODE_MANUAL);
                                }
                            });
                            launchDialog.SetOnClickNo(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                }
                            });
                            launchDialog.show(activity.getFragmentManager(), "");
                        }
                    }
                });
            }
            else
            {
                lytInterceptors.setVisibility(GONE);
                txtInterceptors.setVisibility(GONE);
            }

            if(cargoSystem != null)
            {
                lytCargo.addView(cargoSystem);
                lytCargo.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));
            }
            else
            {
                txtCargo.setVisibility(GONE);
                lytCargo.setVisibility(GONE);
            }
        }
        else
        {
            txtMissiles.setVisibility(GONE);
            lytMissiles.setVisibility(GONE);
            txtInterceptors.setVisibility(GONE);
            lytInterceptors.setVisibility(GONE);
            txtCargo.setVisibility(GONE);
            lytCargo.setVisibility(GONE);
        }

        if(bFlying)
        {
            SetupFlyingAircraft();
        }
        else
        {
            SetupStoredAircraft();
        }
    }

    public void SetupFlyingAircraft()
    {
        Airplane aircraft = (Airplane)aircraftShadow.GetAirplane();

        if(game.EntityIsFriendly(game.GetPlayer(aircraftShadow.GetOwnerID()), game.GetOurPlayer()))
        {
            txtAircraftStatus.setVisibility(VISIBLE);
            TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);

            if(aircraft.Orphaned())
                txtOrphan.setVisibility(VISIBLE);
            else
                txtOrphan.setVisibility(GONE);

            if(aircraft.GetMoveOrders() != Movable.MoveOrders.WAIT)
            {
                viewToTarget.setVisibility(VISIBLE);
                txtToTarget.setVisibility(VISIBLE);

                float fltDistanceToTravel = 0;

                if(aircraft.HasGeoCoordChain())
                {
                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(aircraft.GetPosition(), aircraft.GetCoordinates());
                }
                else
                {
                    GeoCoord geoTarget = aircraft.GetTarget() != null && aircraft.GetTarget().GetMapEntity(game) != null ? aircraft.GetTarget().GetMapEntity(game).GetPosition() : aircraft.GetGeoTarget();

                    if(geoTarget != null)
                        fltDistanceToTravel = aircraft.GetPosition().DistanceTo(geoTarget);
                }

                txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.GetAircraftSpeed(aircraft.GetAircraftType()) * Defs.MS_PER_HOUR))));
            }
            else
            {
                viewToTarget.setVisibility(GONE);
                txtToTarget.setVisibility(GONE);
            }
        }
        else
        {
            txtToTarget.setVisibility(GONE);
            viewToTarget.setVisibility(GONE);
            txtFuelTitle.setVisibility(GONE);
            txtFuelLevel.setVisibility(GONE);
            txtAircraftStatus.setVisibility(GONE);
            //txtFlaresTitle.setVisibility(GONE);
            //txtFlares.setVisibility(GONE);
            txtOrphan.setVisibility(GONE);
        }

        if(bOwnedByPlayer)
        {
            btnCeaseFire.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cease_fire_confirm));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.CeaseFire(Collections.singletonList(aircraft.GetPointer()));
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
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

            if(aircraft.GetMoveOrders() != Movable.MoveOrders.WAIT && aircraft.GetMoveOrders() != Movable.MoveOrders.MOVE)
            {
                btnCeaseFire.setVisibility(VISIBLE);
            }
            else
            {
                btnCeaseFire.setVisibility(GONE);
            }

            btnLaunchInterceptor.setVisibility(GONE);
            btnReturn.setVisibility(VISIBLE);
            //btnSetNewHomebase.setVisibility(VISIBLE);

            btnKamikaze.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bInAirbase && homebase != null && !aircraftShadow.Flying())
                    {
                        if(!((Airbase) homebase).GetOnline())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_takeoff));
                        }
                        else
                            activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                    }
                    else
                        activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                }
            });

            btnRefuel.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.SeekFuelMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()));
                }
            });

            if(aircraft.CanTransferFuel())
            {
                btnProvideFuel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.ProvideFuelMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()));
                    }
                });
            }
            else
            {
                btnProvideFuel.setVisibility(GONE);
            }

            if(aircraft.HasScanner())
            {
                btnScan.setVisibility(VISIBLE);

                if(aircraft.GetRadarActive())
                {
                    imgScan.setImageResource(R.drawable.button_radar_active);
                }
                else
                {
                    imgScan.setImageResource(R.drawable.button_radar_inactive);
                }

                btnScan.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!aircraft.GetRadarActive())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderLaunch();
                            launchDialog.SetMessage(context.getString(R.string.toggle_radar_confirm_aircraft));
                            launchDialog.SetOnClickYes(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.RadarScan(aircraft.GetPointer());
                                    Update();
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
                        else
                        {
                            game.RadarScan(aircraft.GetPointer());
                            Update();
                        }
                    }
                });
            }
            else
            {
                btnScan.setVisibility(GONE);
            }

            btnReturn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(aircraftShadow.GetHomeBase().GetMapEntity(game) != null)
                    {
                        activity.ReturnAircraft(Collections.singletonList(aircraftShadow.GetAirplane().GetPointer()));

                        if(homebase instanceof Airbase && !((Airbase)homebase).GetOnline())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_land));
                        }
                    }
                    else
                        activity.ShowBasicOKDialog(context.getString(R.string.aircraft_orphaned_cant_return));
                }
            });

            btnSetNewHomebase.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ChooseNewHomebaseMode(aircraft);
                }
            });

            btnElectronicWarfare.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ElectronicWarfareMode(aircraft);
                }
            });
        }
        else
        {
            btnSetNewHomebase.setVisibility(GONE);
            btnToggleReturn.setVisibility(GONE);
            txtNameButton.setVisibility(GONE);
            lytControls.setVisibility(GONE);
            lytMissiles.setVisibility(GONE);
            lytInterceptors.setVisibility(GONE);
            lytCargo.setVisibility(GONE);
            btnLaunchInterceptor.setVisibility(VISIBLE);
            btnAttack.setVisibility(GONE);
            btnRefuel.setVisibility(GONE);

            btnLaunchInterceptor.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.InterceptorSelectForTarget(aircraftShadow.GetID(), TextUtilities.GetOwnedEntityName(aircraftShadow.GetAirplane(), game), aircraft);
                }
            });
        }

        if(!aircraft.GetElectronicWarfare() || !bOwnedByPlayer)
            btnElectronicWarfare.setVisibility(GONE);

        Update();
    }

    public void SetupStoredAircraft()
    {
        btnLaunchInterceptor.setVisibility(GONE);
        btnSetNewHomebase.setVisibility(GONE);
        btnReturn.setVisibility(GONE);
        txtToTarget.setVisibility(GONE);
        viewToTarget.setVisibility(GONE);
        btnScan.setVisibility(GONE);
        btnProvideFuel.setVisibility(GONE);
        btnElectronicWarfare.setVisibility(GONE);

        if(game.EntityIsFriendly(game.GetPlayer(aircraftShadow.GetOwnerID()), game.GetOurPlayer()))
        {
            txtAircraftStatus.setVisibility(VISIBLE);
            TextUtilities.AssignAircraftStatusString(txtAircraftStatus, (StoredAirplane)aircraftShadow);
        }
        else
        {
            txtFuelTitle.setVisibility(GONE);
            txtFuelLevel.setVisibility(GONE);
            txtAircraftStatus.setVisibility(GONE);
            //txtFlaresTitle.setVisibility(GONE);
            //txtFlares.setVisibility(GONE);
            txtOrphan.setVisibility(GONE);
        }

        if(bOwnedByPlayer)
        {
            btnKamikaze.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bInAirbase && homebase != null && !aircraftShadow.Flying())
                    {
                        if(!((Airbase) homebase).GetOnline())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_takeoff));
                        }
                        else
                            activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                    }
                    else
                        activity.SetTargetMode(new EntityPointer(aircraftShadow.GetID(), aircraftShadow.GetAirplane().GetEntityType()), null);
                }
            });

            btnReturn.setVisibility(GONE);
        }
        else
        {
            btnToggleReturn.setVisibility(GONE);
            txtNameButton.setVisibility(GONE);
            lytControls.setVisibility(GONE);
            lytMissiles.setVisibility(GONE);
            lytInterceptors.setVisibility(GONE);
            lytCargo.setVisibility(GONE);
            btnAttack.setVisibility(GONE);
            btnRefuel.setVisibility(GONE);
            btnProvideFuel.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        super.Update();

        if(aircraftShadow.Flying())
        {
            geoPosition = ((Airplane)aircraftShadow.GetAirplane()).GetPosition();
        }
        else
        {
            geoPosition = homebase.GetPosition();
        }

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AirplaneInterface aircraftInterface = aircraftShadow.Flying() ? game.GetAirplane(aircraftShadow.GetID()) : game.GetStoredAirplane(aircraftShadow.GetID());

                if(aircraftInterface != null)
                {
                    imgToggleReturn.setImageResource(aircraftShadow.WillAutoReturn() ? R.drawable.button_manual_return : R.drawable.button_auto_return);

                    if(aircraftInterface.Flying())
                    {
                        Airplane aircraft = (Airplane)aircraftShadow.GetAirplane();

                        if(bOwnedByPlayer)
                        {
                            if(aircraft.HasScanner())
                            {
                                if(aircraft.GetRadarActive())
                                {
                                    imgScan.setImageResource(R.drawable.button_radar_active);
                                }
                                else
                                {
                                    imgScan.setImageResource(R.drawable.button_radar_inactive);
                                }
                            }

                            if(aircraft.GetMoveOrders() != Movable.MoveOrders.WAIT && aircraft.GetMoveOrders() != Movable.MoveOrders.MOVE)
                            {
                                btnCeaseFire.setVisibility(VISIBLE);
                            }
                            else
                            {
                                btnCeaseFire.setVisibility(GONE);
                            }
                        }

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, aircraft);

                        if(game.EntityIsFriendly(game.GetPlayer(aircraftShadow.GetOwnerID()), game.GetOurPlayer()))
                        {
                            TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraftShadow);
                            TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);

                            if(aircraft.Orphaned())
                                txtOrphan.setVisibility(VISIBLE);
                            else
                                txtOrphan.setVisibility(GONE);

                            if(aircraft.GetGeoTarget() != null && aircraft.GetMoveOrders() != Movable.MoveOrders.WAIT)
                            {
                                viewToTarget.setVisibility(VISIBLE);
                                txtToTarget.setVisibility(VISIBLE);

                                float fltDistanceToTravel = 0;

                                if(aircraft.HasGeoCoordChain())
                                {
                                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(aircraft.GetPosition(), aircraft.GetCoordinates());
                                }
                                else
                                {
                                    GeoCoord geoTarget = aircraft.GetTarget() != null && aircraft.GetTarget().GetMapEntity(game) != null? aircraft.GetTarget().GetMapEntity(game).GetPosition() : aircraft.GetGeoTarget();
                                    fltDistanceToTravel = aircraft.GetPosition().DistanceTo(geoTarget);
                                }

                                txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.GetAircraftSpeed(aircraft.GetAircraftType()) * Defs.MS_PER_HOUR))));
                            }
                            else
                            {
                                viewToTarget.setVisibility(GONE);
                                txtToTarget.setVisibility(GONE);
                            }
                        }

                        String strName = Utilities.GetEntityName(context, aircraft);
                        txtName.setText(strName);
                        txtNameButton.setText(strName);
                    }
                    else
                    {
                        StoredAirplane aircraft = (StoredAirplane)aircraftShadow.GetAirplane();

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, aircraft);

                        if(game.EntityIsFriendly(game.GetPlayer(aircraftShadow.GetOwnerID()), game.GetOurPlayer()))
                        {
                            TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraftShadow);
                            TextUtilities.AssignAircraftStatusString(txtAircraftStatus, aircraft);
                        }

                        String strName = Utilities.GetEntityName(context, aircraft);
                        txtName.setText(strName);
                        txtNameButton.setText(strName);
                    }
                    //txtFlares.setText(String.valueOf(aircraftInterface.GetFlareCount()));

                    if(missileSystem != null)
                        missileSystem.Update();

                    if(interceptorSystem != null)
                        interceptorSystem.Update();
                }
                else
                {
                    Log.i("LaunchWTF", "AirplaneInterface is null. Finishing... (AircraftView ln 560)");
                    Finish(true);
                }
            }
        });
    }

    public void Sell()
    {
        game.SellEntity(aircraftShadow.GetAirplane().GetPointer());
    }

    @Override
    public boolean IsSingleAircraft()
    {
        return true;
    }

    @Override
    public AirplaneInterface GetCurrentAircraft()
    {
        return aircraftShadow.Flying() ? game.GetAirplane(aircraftShadow.GetID()) : game.GetStoredAirplane(aircraftShadow.GetID());
    }

    @Override
    public List<AirplaneInterface> GetCurrentAircrafts()
    {
        return null;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals((LaunchEntity)aircraftShadow))
        {
            if(aircraftShadow.HasCargo())
                cargoSystem = new CargoSystemControl(game, activity, (HaulerInterface)aircraftShadow);
        }
    }
}
