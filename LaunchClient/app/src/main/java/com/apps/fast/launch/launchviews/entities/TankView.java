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
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.ArtilleryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.NamableInterface;
import launch.game.entities.SAMSite;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.CargoSystem;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchUtilities;

public class TankView extends LaunchView implements LaunchUICommon.TankInfoProvider
{
    private TextView txtTankTitle;
    private LinearLayout btnSetTarget;
    private LinearLayout lytReload;
    private TextView txtReloading;

    private TankInterface tankShadow;
    private TextView txtTankStatus;

    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;
    private LinearLayout lytControls;
    private LinearLayout btnSell;
    private TextView txtHP;
    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    protected ImageView imgTank;
    private FrameLayout lytLaunchables;

    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private View viewMode;
    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;
    private TextView txtToTarget;
    private View viewToTarget;
    private LaunchView launchableSystem;
    private boolean bOwnedByPlayer;

    public TankView(LaunchClientGame game, MainActivity activity, TankInterface tank)
    {
        super(game, activity, true);
        this.tankShadow = tank;

        bOwnedByPlayer = tankShadow.GetOwnerID() == game.GetOurPlayerID();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_tank, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        btnMove = findViewById(R.id.btnMove);
        btnSetTarget = findViewById(R.id.btnSetTarget);
        lytControls = findViewById(R.id.lytControls);
        txtHP = findViewById(R.id.txtHP);
        txtTankStatus = findViewById(R.id.txtTankStatus);
        lytLaunchables = findViewById(R.id.lytLaunchables);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        lytReload = (LinearLayout) findViewById(R.id.lytReload);
        txtReloading = (TextView) findViewById(R.id.txtReloading);

        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);
        txtTankTitle = findViewById(R.id.txtTankTitle);
        imgTank = findViewById(R.id.imgTank);

        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);
        viewMode = findViewById(R.id.viewMode);
        btnSell = findViewById(R.id.btnSell);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        txtTankTitle.setText(TextUtilities.GetOwnedEntityName((LaunchEntity)tankShadow, game));

        txtToTarget = findViewById(R.id.txtToTarget);
        viewToTarget = findViewById(R.id.viewToTarget);

        View viewAdmin = findViewById(R.id.viewAdmin);
        LinearLayout btnAdminDelete = findViewById(R.id.btnAdminDelete);

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
                    launchDialog.SetMessage(context.getString(R.string.admin_delete_confirm, ((LaunchEntity)GetCurrentTank()).GetTypeName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AdminDelete(((LaunchEntity)GetCurrentTank()).GetPointer());
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

        if(tankShadow instanceof Tank)
        {
            Tank tank = (Tank)tankShadow;
            FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

            UnitControls controls = new UnitControls(game, activity, tank);
            lytUnitControls.removeAllViews();
            lytUnitControls.addView(controls);

            if(tank.GetGeoTarget() != null && game.EntityIsFriendly(tank, game.GetOurPlayer()) && tank.GetMoveOrders() != Movable.MoveOrders.WAIT)
            {
                viewToTarget.setVisibility(VISIBLE);
                txtToTarget.setVisibility(VISIBLE);

                float fltDistanceToTravel = 0;

                if(tank.HasGeoCoordChain())
                {
                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(tank.GetPosition(), tank.GetCoordinates());
                }
                else
                {
                    fltDistanceToTravel = tank.GetPosition().DistanceTo(tank.GetGeoTarget());
                }

                txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.LAND_UNIT_SPEED * Defs.MS_PER_HOUR))));
            }
            else
            {
                viewToTarget.setVisibility(GONE);
                txtToTarget.setVisibility(GONE);
            }

            if(bOwnedByPlayer)
            {
                btnSell.setVisibility(VISIBLE);

                btnSell.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.InBattle(game.GetOurPlayer()))
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderPurchase();
                            launchDialog.SetMessage(context.getString(R.string.sell_land_unit_confirm, TextUtilities.GetCostStatement(game.GetSaleValue(game.GetLandUnitValue(tank)))));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.SellEntity(tank.GetPointer());
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
                            activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_sell));
                        }
                    }
                });

                if(tank.IsAnMBT())
                {
                    btnSetTarget.setVisibility(VISIBLE);

                    btnSetTarget.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetTargetMode(new EntityPointer(tank.GetID(), tank.GetEntityType()), null);
                        }
                    });
                }
                else
                {
                    btnSetTarget.setVisibility(GONE);
                }
            }

            TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);

            if((tank.HasInterceptors() || tank.HasArtillery()) && bOwnedByPlayer)
            {
                lytMode.setVisibility(VISIBLE);
                viewMode.setVisibility(VISIBLE);

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetTank(tank.GetID()).GetAuto())
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

                                    game.SetSAMSiteMode(tank.GetPointer(), SAMSite.MODE_AUTO);
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
                        if(!game.GetTank(tank.GetID()).GetSemiAuto())
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

                                    game.SetSAMSiteMode(tank.GetPointer(), SAMSite.MODE_SEMI_AUTO);
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
                        if(!game.GetTank(tank.GetID()).GetManual())
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

                                    game.SetSAMSiteMode(tank.GetPointer(), SAMSite.MODE_MANUAL);
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

            btnCeaseFire.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(tank.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
                }
            });

            if(tank.GetMoveOrders() != MoveOrders.WAIT && bOwnedByPlayer)
                btnCeaseFire.setVisibility(VISIBLE);

            if(game.EntityIsFriendly(game.GetPlayer(tank.GetOwnerID()), game.GetOurPlayer()))
            {
                TextUtilities.AssignTankStatusString(txtTankStatus, tankShadow);
                txtName.setVisibility(GONE);
                txtNameButton.setVisibility(VISIBLE);

                if(tank.GetGeoTarget() != null && tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND && game.GetTravelTime(Defs.LAND_UNIT_SPEED, tank.GetPosition(), game.GetMovableTarget(tank)) > 0)
                {
                    viewToTarget.setVisibility(VISIBLE);
                    txtToTarget.setVisibility(VISIBLE);

                    float fltDistanceToTravel = 0;

                    if(tank.HasGeoCoordChain())
                    {
                        fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(tank.GetPosition(), tank.GetCoordinates());
                    }
                    else
                    {
                        fltDistanceToTravel = tank.GetPosition().DistanceTo(tank.GetGeoTarget());
                    }

                    txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.LAND_UNIT_SPEED * Defs.MS_PER_HOUR))));
                }
                else
                {
                    viewToTarget.setVisibility(GONE);
                    txtToTarget.setVisibility(GONE);
                }
            }
            else
            {
                txtNameButton.setVisibility(GONE);
                txtTankStatus.setVisibility(GONE);
                txtName.setVisibility(VISIBLE);
                viewToTarget.setVisibility(GONE);
                txtToTarget.setVisibility(GONE);
            }
        }
        else
        {
            StoredTank tank = (StoredTank)tankShadow;

            TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);
        }

        String strName = Utilities.GetEntityName(context, (NamableInterface)tankShadow);
        txtName.setText(strName);
        txtNameButton.setText(strName);

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.MoveOrderMode(((LaunchEntity)tankShadow).GetPointer(), null);
            }
        });

        if(bOwnedByPlayer)
        {
            txtName.setVisibility(GONE);
            txtNameEdit.setText(tankShadow.GetName());

            if(launchableSystem != null)
            {
                lytLaunchables.setVisibility(VISIBLE);
                lytLaunchables.addView(launchableSystem);
                lytLaunchables.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));
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

            btnMove.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.MoveOrderMode(((LaunchEntity)tankShadow).GetPointer(), null);
                }
            });

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(((LaunchEntity)tankShadow).GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                    txtHP.setVisibility(VISIBLE);
                }
            });
        }
        else
        {
            btnMove.setVisibility(GONE);
            lytLaunchables.setVisibility(GONE);
            txtTankStatus.setVisibility(GONE);
        }
    }

    @Override
    public void Update()
    {
        super.Update();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TankInterface tankInterface = game.GetTankInterface(tankShadow.GetID());

                if(tankInterface != null)
                {
                    if(launchableSystem != null)
                        launchableSystem.Update();

                    if(game.EntityIsFriendly((LaunchEntity)tankShadow, game.GetOurPlayer()))
                    {
                        TextUtilities.AssignTankStatusString(txtTankStatus, tankShadow);
                    }
                    else
                    {
                        txtTankStatus.setVisibility(GONE);
                    }

                    if(tankInterface instanceof Tank)
                    {
                        Tank tank = (Tank)tankInterface;

                        //Reload.
                        long oReloadTimeRemaining = tank.GetReloadTimeRemaining();

                        if (oReloadTimeRemaining > 0)
                        {
                            lytReload.setVisibility(VISIBLE);
                            txtReloading.setText(TextUtilities.GetTimeAmount(oReloadTimeRemaining));
                        }
                        else
                        {
                            lytReload.setVisibility(GONE);
                        }

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);

                        if(tank.GetMoveOrders() != MoveOrders.WAIT && bOwnedByPlayer)
                            btnCeaseFire.setVisibility(VISIBLE);
                        else
                            btnCeaseFire.setVisibility(GONE);

                        if(tank.GetOwnedBy(game.GetOurPlayerID()))
                        {
                            if(tankShadow.HasInterceptors() || tankShadow.HasArtillery())
                            {
                                lytMode.setVisibility(VISIBLE);
                                viewMode.setVisibility(VISIBLE);

                                if(tank.GetAuto())
                                {
                                    flasherAuto.TurnGreen(context);
                                }
                                else
                                {
                                    flasherAuto.TurnOff(context);
                                }

                                if(tank.GetSemiAuto())
                                {
                                    flasherSemi.TurnGreen(context);
                                }
                                else
                                {
                                    flasherSemi.TurnOff(context);
                                }

                                if(tank.GetManual())
                                {
                                    flasherManual.TurnGreen(context);
                                }
                                else
                                {
                                    flasherManual.TurnOff(context);
                                }
                            }
                        }

                        if(game.EntityIsFriendly(game.GetPlayer(tank.GetOwnerID()), game.GetOurPlayer()))
                        {
                            if(tank.GetGeoTarget() != null && tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND && game.GetTravelTime(Defs.LAND_UNIT_SPEED, tank.GetPosition(), game.GetMovableTarget(tank)) > 0)
                            {
                                viewToTarget.setVisibility(VISIBLE);
                                txtToTarget.setVisibility(VISIBLE);

                                float fltDistanceToTravel = 0;

                                if(tank.HasGeoCoordChain())
                                {
                                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(tank.GetPosition(), tank.GetCoordinates());
                                }
                                else
                                {
                                    fltDistanceToTravel = tank.GetPosition().DistanceTo(tank.GetGeoTarget());
                                }

                                txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.LAND_UNIT_SPEED * Defs.MS_PER_HOUR))));
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
                        }
                    }
                    else
                    {
                        StoredTank tank = (StoredTank)tankInterface;

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);
                    }

                    String strName = Utilities.GetEntityName(context, (NamableInterface)tankInterface);
                    txtName.setText(strName);
                    txtNameButton.setText(strName);
                }
                else
                {
                    Log.i("LaunchWTF", "TankInterface is null. Finishing... (TankView ln 259)");
                    Finish(true);
                }
            }
        });
    }

    @Override
    public boolean IsSingleTank()
    {
        return false;
    }

    @Override
    public Tank GetCurrentTank()
    {
        return null;
    }

    @Override
    public List<TankInterface> GetCurrentTanks()
    {
        return null;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals((LaunchEntity)tankShadow))
        {
            Update();
        }
    }
}
