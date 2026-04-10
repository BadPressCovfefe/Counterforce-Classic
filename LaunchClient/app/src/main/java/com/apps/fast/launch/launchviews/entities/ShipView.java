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
import com.apps.fast.launch.launchviews.NavalSystemView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.launchviews.controls.AircraftSystemControl;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem;
import launch.utilities.LaunchUtilities;

/**
 * Created by Corbin
 */
public class ShipView extends LaunchView implements LaunchUICommon.ShipInfoProvider
{
    private Ship shipShadow;

    private TextView txtShipTitle;
    protected ImageView imgShip;
    private TextView txtHP;
    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    private LinearLayout lytControls; //Should only be visible to the owner.
    private LinearLayout btnMove;
    private LinearLayout btnStop;
    private LinearLayout btnScan;
    private ImageView imgScan;
    private LinearLayout btnSonar;
    private LinearLayout btnSeekFuel;
    private LinearLayout btnProvideFuel;
    private LinearLayout btnSell;
    private TextView txtRangeRemaining;
    private TextView txtFuelTitle;

    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private View viewMode;

    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    private TextView txtRange;
    private TextView txtFuel;
    private View viewStats;

    protected LinearLayout lytSystems; //Should only be visible to the owner.
    private TextView txtSentries;
    private TextView txtMissiles; //Clicking this should open a MissileSystemControl.
    private TextView txtInterceptors;
    private TextView txtTorpedoes;
    private TextView txtArtillery;
    private TextView txtAircraft;
    private TextView txtCargo;

    private TextView txtToTarget;
    private View viewToTarget;

    private boolean bOwnedByPlayer;
    private boolean bInPort;
    private GeoCoord geoPosition;

    public ShipView(LaunchClientGame game, MainActivity activity, Ship ship)
    {
        super(game, activity, true);
        this.shipShadow = ship;

        bOwnedByPlayer = shipShadow.GetOwnerID() == game.GetOurPlayerID();

        geoPosition = shipShadow.GetPosition();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_ship, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        txtShipTitle = findViewById(R.id.txtShipTitle);
        imgShip = findViewById(R.id.imgShip);
        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);
        txtHP = findViewById(R.id.txtHP);

        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);
        viewMode = findViewById(R.id.viewMode);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        txtRange = findViewById(R.id.txtRange);
        txtFuel = findViewById(R.id.txtFuel);
        txtFuelTitle = findViewById(R.id.txtFuelTitle);
        txtRangeRemaining = findViewById(R.id.txtRangeRemaining);
        viewStats = findViewById(R.id.viewStats);
        lytControls = findViewById(R.id.lytControls);
        btnMove = findViewById(R.id.btnMove);
        btnStop = findViewById(R.id.btnStop);
        btnScan = findViewById(R.id.btnScanner);
        imgScan = findViewById(R.id.imgScanner);
        btnSonar = findViewById(R.id.btnSonar);
        btnSeekFuel = findViewById(R.id.btnSeekFuel);
        btnProvideFuel = findViewById(R.id.btnProvideFuel);
        btnSell = findViewById(R.id.btnSell);

        txtToTarget = findViewById(R.id.txtToTarget);
        viewToTarget = findViewById(R.id.viewToTarget);

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
                    launchDialog.SetMessage(context.getString(R.string.admin_delete_confirm, ((LaunchEntity)GetCurrentShip()).GetTypeName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AdminDelete(((LaunchEntity)GetCurrentShip()).GetPointer());
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

        FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

        UnitControls controls = new UnitControls(game, activity, shipShadow);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        if(shipShadow.GetGeoTarget() != null && game.EntityIsFriendly(shipShadow, game.GetOurPlayer()) && shipShadow.GetMoveOrders() != Movable.MoveOrders.WAIT)
        {
            viewToTarget.setVisibility(VISIBLE);
            txtToTarget.setVisibility(VISIBLE);

            float fltDistanceToTravel = 0;

            if(shipShadow.HasGeoCoordChain())
            {
                fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(shipShadow.GetPosition(), shipShadow.GetCoordinates());
            }
            else
            {
                fltDistanceToTravel = shipShadow.GetPosition().DistanceTo(shipShadow.GetGeoTarget());
            }

            txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.NAVAL_SPEED * Defs.MS_PER_HOUR))));
        }
        else
        {
            viewToTarget.setVisibility(GONE);
            txtToTarget.setVisibility(GONE);
        }

        lytSystems = findViewById(R.id.lytSystems);
        txtSentries = findViewById(R.id.txtSentries);
        txtMissiles = findViewById(R.id.txtMissiles);
        txtTorpedoes = findViewById(R.id.txtTorpedoes);
        txtInterceptors = findViewById(R.id.txtInterceptors);
        txtArtillery = findViewById(R.id.txtArtillery);
        txtAircraft = findViewById(R.id.txtAircraft);
        txtCargo = findViewById(R.id.txtCargo);

        txtShipTitle.setText(TextUtilities.GetOwnedEntityName(shipShadow, game));
        imgShip.setImageBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, shipShadow));
        TextUtilities.AssignHealthStringAndAppearance(txtHP, shipShadow);
        String strName = Utilities.GetEntityName(context, shipShadow);
        txtName.setText(strName);
        txtNameButton.setText(strName);

        txtRange.setText(shipShadow.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetDistanceStringFromKM(game.GetFuelableRange(shipShadow.GetCurrentFuel(), Defs.NAVAL_RANGE)));
        txtFuel.setText(shipShadow.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetPercentStringFromFraction(shipShadow.GetCurrentFuel()));

        if(game.ShipInPort(shipShadow))
        {
            bInPort = true;

            if(!shipShadow.AtFullHealth() && game.EntityIsFriendly(shipShadow, game.GetOurPlayer()))
            {
                txtToTarget.setVisibility(VISIBLE);
                txtToTarget.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
                txtToTarget.setText(context.getString(R.string.in_port_repairing));
            }
            else
            {
                txtToTarget.setVisibility(GONE);
            }
        }

        if(bOwnedByPlayer)
        {
            txtName.setVisibility(GONE);

            if(shipShadow.HasSentries())
                txtSentries.setText(context.getString(R.string.ship_ciws_count_stat, shipShadow.GetReadySentryCount(), shipShadow.GetSentryCount()));
            else
                txtSentries.setVisibility(GONE);

            if(shipShadow.HasMissiles())
            {
                txtMissiles.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SHIP_MISSILES, shipShadow));
                    }
                });
            }
            else
            {
                txtMissiles.setVisibility(GONE);
            }

            if(shipShadow.HasInterceptors())
            {
                lytMode.setVisibility(VISIBLE);
                viewMode.setVisibility(VISIBLE);

                if(shipShadow.GetAuto())
                {
                    flasherAuto.TurnGreen(context);
                }
                else
                {
                    flasherAuto.TurnOff(context);
                }

                if(shipShadow.GetSemiAuto())
                {
                    flasherSemi.TurnGreen(context);
                }
                else
                {
                    flasherSemi.TurnOff(context);
                }

                if(shipShadow.GetManual())
                {
                    flasherManual.TurnGreen(context);
                }
                else
                {
                    flasherManual.TurnOff(context);
                }

                txtInterceptors.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SHIP_INTERCEPTORS, shipShadow));
                    }
                });

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetShip(shipShadow.GetID()).GetAuto())
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

                                    game.SetSAMSiteMode(shipShadow.GetPointer(), SAMSite.MODE_AUTO);
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
                        if(!game.GetShip(shipShadow.GetID()).GetSemiAuto())
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

                                    game.SetSAMSiteMode(shipShadow.GetPointer(), SAMSite.MODE_SEMI_AUTO);
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
                        if(!game.GetShip(shipShadow.GetID()).GetManual())
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

                                    game.SetSAMSiteMode(shipShadow.GetPointer(), SAMSite.MODE_MANUAL);
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
                txtInterceptors.setVisibility(GONE);
            }

            if(shipShadow.HasTorpedoes())
            {
                txtTorpedoes.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SHIP_TORPEDOES, shipShadow));
                    }
                });
            }
            else
            {
                txtTorpedoes.setVisibility(GONE);
            }

            if(shipShadow.HasArtillery())
            {
                txtArtillery.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SHIP_ARTILLERY, shipShadow));
                    }
                });
            }
            else
            {
                txtArtillery.setVisibility(GONE);
            }

            if(shipShadow.HasCargo())
            {
                txtCargo.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SHIP_CARGO, shipShadow));
                    }
                });
            }
            else
            {
                txtCargo.setVisibility(GONE);
            }

            if(shipShadow.HasAircraft())
            {
                txtAircraft.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new AircraftSystemControl(game, activity, shipShadow));
                    }
                });
            }
            else
            {
                txtAircraft.setVisibility(GONE);
            }

            txtName.setVisibility(GONE);
            txtNameEdit.setText(shipShadow.GetName());

            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.MoveOrderMode(shipShadow.GetPointer(), null);
                }
            });

            if(!shipShadow.GetNuclear() || (shipShadow.HasCargo() && shipShadow.GetCargoSystem().ContainsResourceType(Resource.ResourceType.FUEL)))
            {
                btnProvideFuel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.ProvideFuelMode(shipShadow.GetPointer());
                    }
                });
            }
            else
            {
                btnProvideFuel.setVisibility(GONE);
            }

            if(!shipShadow.GetNuclear())
            {
                btnSeekFuel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SeekFuelMode(shipShadow.GetPointer());
                    }
                });
            }
            else
            {
                btnSeekFuel.setVisibility(GONE);
            }

            btnStop.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(shipShadow.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
                }
            });

            if(shipShadow.HasSonar())
            {
                btnSonar.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(shipShadow.SonarReady())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderLaunch();
                            launchDialog.SetMessage(context.getString(R.string.sonar_ping_confirm_ship));
                            launchDialog.SetOnClickYes(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.SonarPing(shipShadow.GetPointer());
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
                            activity.ShowBasicOKDialog(context.getString(R.string.cant_ping_cooldown, TextUtilities.GetTimeAmount(shipShadow.GetSonarCooldownRemaining())));
                        }
                    }
                });
            }
            else
            {
                btnSonar.setVisibility(GONE);
            }

            if(shipShadow.HasScanner())
            {
                btnScan.setVisibility(VISIBLE);

                if(shipShadow.GetRadarActive())
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
                        if(!shipShadow.GetRadarActive())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderLaunch();
                            launchDialog.SetMessage(context.getString(R.string.toggle_radar_confirm_ship));
                            launchDialog.SetOnClickYes(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.RadarScan(shipShadow.GetPointer());
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
                            game.RadarScan(shipShadow.GetPointer());
                        }
                    }
                });
            }
            else
            {
                btnScan.setVisibility(GONE);
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

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(shipShadow.GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });

            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!bInPort)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.not_in_port_cant_sell, TextUtilities.GetDistanceStringFromKM(Defs.SHIPYARD_REPAIR_DISTANCE)));
                    }
                    else if(game.InBattle(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_sell));
                    }
                    else
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.sell_naval_vessel_confirm, TextUtilities.GetCostStatement(game.GetSaleValue(Defs.GetNavalBuildCost(shipShadow.GetEntityType())))));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                game.SellEntity(shipShadow.GetPointer());
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
                }
            });
        }
        else
        {
            btnSell.setVisibility(GONE);
            lytControls.setVisibility(GONE);
            lytNameEdit.setVisibility(GONE);
            txtNameButton.setVisibility(GONE);
            txtFuelTitle.setVisibility(GONE);
            txtFuel.setVisibility(GONE);
            txtRangeRemaining.setVisibility(GONE);
            txtRange.setVisibility(GONE);
            lytSystems.setVisibility(GONE);
        }
    }

    @Override
    public void Update()
    {
        super.Update();

        geoPosition = shipShadow.GetPosition();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Ship ship = game.GetShip(shipShadow.GetID());

                if(ship != null)
                {
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, ship);
                    String strName = Utilities.GetEntityName(context, ship);
                    txtName.setText(strName);
                    txtNameButton.setText(strName);

                    if(bOwnedByPlayer && ship.HasScanner())
                    {
                        if(ship.GetRadarActive())
                        {
                            imgScan.setImageResource(R.drawable.button_radar_active);
                        }
                        else
                        {
                            imgScan.setImageResource(R.drawable.button_radar_inactive);
                        }
                    }

                    if(ship.GetGeoTarget() != null && game.EntityIsFriendly(ship, game.GetOurPlayer()) && ship.GetMoveOrders() != Movable.MoveOrders.WAIT)
                    {
                        viewToTarget.setVisibility(VISIBLE);
                        txtToTarget.setVisibility(VISIBLE);

                        float fltDistanceToTravel = 0;

                        if(ship.HasGeoCoordChain())
                        {
                            fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(ship.GetPosition(), ship.GetCoordinates());
                        }
                        else
                        {
                            fltDistanceToTravel = ship.GetPosition().DistanceTo(ship.GetGeoTarget());
                        }

                        txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.NAVAL_SPEED * Defs.MS_PER_HOUR))));
                    }
                    else if(!shipShadow.AtFullHealth() && game.EntityIsFriendly(shipShadow, game.GetOurPlayer()) && game.ShipInPort(shipShadow))
                    {
                        bInPort = true;
                        txtToTarget.setVisibility(VISIBLE);
                        txtToTarget.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
                        txtToTarget.setText(context.getString(R.string.in_port_repairing));
                    }
                    else
                    {
                        viewToTarget.setVisibility(GONE);
                        txtToTarget.setVisibility(GONE);
                    }

                    if(ship.GetOwnedBy(game.GetOurPlayerID()))
                    {
                        if(ship.HasSentries())
                            txtSentries.setText(context.getString(R.string.ship_ciws_count_stat, ship.GetReadySentryCount(), ship.GetSentryCount()));

                        if(ship.HasInterceptors())
                        {
                            if(ship.GetAuto())
                            {
                                flasherAuto.TurnGreen(context);
                            }
                            else
                            {
                                flasherAuto.TurnOff(context);
                            }

                            if(ship.GetSemiAuto())
                            {
                                flasherSemi.TurnGreen(context);
                            }
                            else
                            {
                                flasherSemi.TurnOff(context);
                            }

                            if(ship.GetManual())
                            {
                                flasherManual.TurnGreen(context);
                            }
                            else
                            {
                                flasherManual.TurnOff(context);
                            }
                        }

                        txtRange.setText(ship.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetDistanceStringFromKM(game.GetFuelableRange(ship.GetCurrentFuel(), Defs.NAVAL_RANGE)));
                        txtFuel.setText(ship.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetPercentStringFromFraction(ship.GetCurrentFuel()));
                    }
                }
                else
                {
                    Log.i("LaunchWTF", "ship is null. Finishing... (ShipView ln 400 ish)");
                    Finish(true);
                }
            }
        });
    }

    @Override
    public boolean IsSingleShip()
    {
        return true;
    }

    @Override
    public Ship GetCurrentShip()
    {
        return game.GetShip(shipShadow.GetID());
    }

    @Override
    public List<Ship> GetCurrentShips()
    {
        return null;
    }
}
