package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.Submarine;
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem;
import launch.utilities.LaunchUtilities;

/**
 * Created by tobster on 09/11/15.
 */
public class SubmarineView extends LaunchView implements LaunchUICommon.SubmarineInfoProvider
{
    private Submarine submarineShadow;

    private TextView txtSubmarineTitle;
    protected ImageView imgSubmarine;
    private TextView txtHP;
    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;

    private LinearLayout lytControls; //Should only be visible to the owner.
    private LinearLayout btnMove;
    private LinearLayout btnStop;
    private LinearLayout btnSonar;
    private LinearLayout btnSeekFuel;
    private LinearLayout btnProvideFuel;
    private LinearLayout btnSell;
    private TextView txtRangeRemaining;
    private TextView txtFuelTitle;
    private TextView txtRange;
    private TextView txtFuel;
    protected LinearLayout lytSystems; //Should only be visible to the owner.
    private TextView txtMissiles; //Clicking this should open a MissileSystemControl.
    private TextView txtTorpedoes;
    private TextView txtICBMs;

    private TextView txtToTarget;
    private View viewToTarget;

    private boolean bOwnedByPlayer;
    private boolean bInPort;
    private GeoCoord geoPosition;

    public SubmarineView(LaunchClientGame game, MainActivity activity, Submarine submarine)
    {
        super(game, activity, true);
        this.submarineShadow = submarine;

        bOwnedByPlayer = submarineShadow.GetOwnerID() == game.GetOurPlayerID();

        geoPosition = submarineShadow.GetPosition();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_submarine, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        txtSubmarineTitle = findViewById(R.id.txtSubmarineTitle);
        imgSubmarine = findViewById(R.id.imgSubmarine);
        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);
        txtHP = findViewById(R.id.txtHP);

        txtRange = findViewById(R.id.txtRange);
        txtFuel = findViewById(R.id.txtFuel);
        txtFuelTitle = findViewById(R.id.txtFuelTitle);
        txtRangeRemaining = findViewById(R.id.txtRangeRemaining);

        lytControls = findViewById(R.id.lytControls);
        btnMove = findViewById(R.id.btnMove);
        btnStop = findViewById(R.id.btnStop);
        btnSonar = findViewById(R.id.btnSonar);
        btnSeekFuel = findViewById(R.id.btnSeekFuel);
        btnProvideFuel = findViewById(R.id.btnProvideFuel);
        btnSell = findViewById(R.id.btnSell);

        lytSystems = findViewById(R.id.lytSystems);
        txtMissiles = findViewById(R.id.txtMissiles);
        txtTorpedoes = findViewById(R.id.txtTorpedoes);
        txtICBMs = findViewById(R.id.txtICBMs);

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
                    launchDialog.SetMessage(context.getString(R.string.admin_delete_confirm, ((LaunchEntity)GetCurrentSubmarine()).GetTypeName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AdminDelete(((LaunchEntity)GetCurrentSubmarine()).GetPointer());
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

        UnitControls controls = new UnitControls(game, activity, submarineShadow);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        if(submarineShadow.GetGeoTarget() != null && game.EntityIsFriendly(submarineShadow, game.GetOurPlayer()) && submarineShadow.GetMoveOrders() != Movable.MoveOrders.WAIT)
        {
            viewToTarget.setVisibility(VISIBLE);
            txtToTarget.setVisibility(VISIBLE);

            float fltDistanceToTravel = 0;

            if(submarineShadow.HasGeoCoordChain())
            {
                fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(submarineShadow.GetPosition(), submarineShadow.GetCoordinates());
            }
            else
            {
                fltDistanceToTravel = submarineShadow.GetPosition().DistanceTo(submarineShadow.GetGeoTarget());
            }

            txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.NAVAL_SPEED * Defs.MS_PER_HOUR))));
        }
        else
        {
            viewToTarget.setVisibility(GONE);
            txtToTarget.setVisibility(GONE);
        }

        txtSubmarineTitle.setText(TextUtilities.GetOwnedEntityName(submarineShadow, game));

        if(submarineShadow.GetEntityType() == EntityPointer.EntityType.ATTACK_SUB)
        {
            imgSubmarine.setImageResource(R.drawable.build_attack_sub);
        }
        else
        {
            imgSubmarine.setImageResource(R.drawable.build_ssbn);
        }

        TextUtilities.AssignHealthStringAndAppearance(txtHP, submarineShadow);
        String strName = Utilities.GetEntityName(context, submarineShadow);
        txtName.setText(strName);
        txtNameButton.setText(strName);

        txtRange.setText(submarineShadow.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetDistanceStringFromKM(game.GetFuelableRange(submarineShadow.GetCurrentFuel(), Defs.NAVAL_RANGE)));
        txtFuel.setText(submarineShadow.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetPercentStringFromFraction(submarineShadow.GetCurrentFuel()));

        if(game.EntityIsFriendly(submarineShadow, game.GetOurPlayer()) && game.ShipInPort(submarineShadow))
        {
            bInPort = true;
            txtToTarget.setVisibility(VISIBLE);
            txtToTarget.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
            txtToTarget.setText(context.getString(R.string.in_port_repairing));
        }
        else
        {
            txtToTarget.setVisibility(GONE);
        }

        if(bOwnedByPlayer)
        {
            txtName.setVisibility(GONE);

            if(submarineShadow.HasMissiles())
            {
                txtMissiles.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SUBMARINE_MISSILES, submarineShadow));
                    }
                });
            }
            else
            {
                txtMissiles.setVisibility(GONE);
            }

            if(submarineShadow.HasTorpedoes())
            {
                txtTorpedoes.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SUBMARINE_TORPEDO, submarineShadow));
                    }
                });
            }
            else
            {
                txtTorpedoes.setVisibility(GONE);
            }

            if(submarineShadow.HasICBMs())
            {
                txtICBMs.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetView(new NavalSystemView(game, activity, LaunchSystem.SystemType.SUBMARINE_ICBM, submarineShadow));
                    }
                });
            }
            else
            {
                txtICBMs.setVisibility(GONE);
            }

            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.MoveOrderMode(submarineShadow.GetPointer(), null);
                }
            });

            if(!submarineShadow.GetNuclear())
            {
                btnProvideFuel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.ProvideFuelMode(submarineShadow.GetPointer());
                    }
                });

                btnSeekFuel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SeekFuelMode(submarineShadow.GetPointer());
                    }
                });
            }
            else
            {
                btnProvideFuel.setVisibility(GONE);
                btnSeekFuel.setVisibility(GONE);
            }

            btnStop.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(submarineShadow.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
                }
            });

            if(submarineShadow.HasSonar())
            {
                btnSonar.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(submarineShadow.SonarReady())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderLaunch();
                            launchDialog.SetMessage(context.getString(R.string.sonar_ping_confirm_submarine));
                            launchDialog.SetOnClickYes(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.SonarPing(submarineShadow.GetPointer());
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
                            activity.ShowBasicOKDialog(context.getString(R.string.cant_ping_cooldown, TextUtilities.GetTimeAmount(submarineShadow.GetSonarCooldownRemaining())));
                        }
                    }
                });
            }
            else
            {
                btnSonar.setVisibility(GONE);
            }

            txtNameButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ExpandView();
                    txtNameButton.setVisibility(GONE);
                    lytNameEdit.setVisibility(VISIBLE);
                    txtHP.setVisibility(GONE);
                }
            });

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(submarineShadow.GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                    txtHP.setVisibility(VISIBLE);
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
                        launchDialog.SetMessage(context.getString(R.string.sell_naval_vessel_confirm, TextUtilities.GetCostStatement(game.GetNavalVesselValue(submarineShadow))));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                game.SellEntity(submarineShadow.GetPointer());
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

        geoPosition = submarineShadow.GetPosition();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Submarine submarine = game.GetSubmarine(submarineShadow.GetID());

                if(submarine != null)
                {
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, submarine);
                    String strName = Utilities.GetEntityName(context, submarine);
                    txtName.setText(strName);
                    txtNameButton.setText(strName);

                    if(submarine.GetGeoTarget() != null  && game.EntityIsFriendly(submarine, game.GetOurPlayer()) && submarine.GetMoveOrders() != Movable.MoveOrders.WAIT && game.GetTravelTime(Defs.NAVAL_SPEED, submarine.GetPosition(), game.GetMovableTarget(submarine)) > 0)
                    {
                        viewToTarget.setVisibility(VISIBLE);
                        txtToTarget.setVisibility(VISIBLE);

                        float fltDistanceToTravel = 0;

                        if(submarine.HasGeoCoordChain())
                        {
                            fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(submarine.GetPosition(), submarine.GetCoordinates());
                        }
                        else
                        {
                            fltDistanceToTravel = submarine.GetPosition().DistanceTo(submarine.GetGeoTarget());
                        }

                        long oTimeMS = (long)(fltDistanceToTravel/Defs.NAVAL_SPEED * Defs.MS_PER_HOUR);
                        txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount(oTimeMS)));
                    }
                    else
                    {
                        viewToTarget.setVisibility(GONE);
                        txtToTarget.setVisibility(GONE);
                    }

                    if(!submarineShadow.AtFullHealth() && game.EntityIsFriendly(submarineShadow, game.GetOurPlayer()) && game.ShipInPort(submarineShadow))
                    {
                        bInPort = true;
                        txtToTarget.setVisibility(VISIBLE);
                        txtToTarget.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
                        txtToTarget.setText(context.getString(R.string.in_port_repairing));
                    }
                    else
                    {
                        txtToTarget.setVisibility(GONE);
                    }

                    if(submarine.GetOwnedBy(game.GetOurPlayerID()))
                    {
                        txtRange.setText(submarine.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetDistanceStringFromKM(game.GetFuelableRange(submarine.GetCurrentFuel(), Defs.NAVAL_RANGE)));
                        txtFuel.setText(submarine.GetNuclear() ? context.getString(R.string.infinite) : TextUtilities.GetPercentStringFromFraction(submarine.GetCurrentFuel()));
                    }
                }
                else
                {
                    Log.i("LaunchWTF", "submarine is null. Finishing... (SubmarineView ln 400 ish)");
                    Finish(true);
                }
            }
        });
    }

    @Override
    public boolean IsSingleSubmarine()
    {
        return true;
    }

    @Override
    public Submarine GetCurrentSubmarine()
    {
        return game.GetSubmarine(submarineShadow.GetID());
    }

    @Override
    public List<Submarine> GetCurrentSubmarines()
    {
        return null;
    }
}
