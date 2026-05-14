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
import launch.game.entities.conceptuals.Resource;
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

    private Tank tankShadow;
    private TextView txtTankStatus;

    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;
    private LinearLayout btnSell;
    private ImageView imgSell;
    private View viewBottom;
    private TextView txtHP;
    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    protected ImageView imgTank;
    private TextView txtToTarget;
    private View viewToTarget;
    private boolean bOwnedByPlayer;

    public TankView(LaunchClientGame game, MainActivity activity, Tank tank)
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
        txtHP = findViewById(R.id.txtHP);
        txtTankStatus = findViewById(R.id.txtTankStatus);
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

        btnSell = findViewById(R.id.btnSell);
        imgSell = findViewById(R.id.imgSell);
        viewBottom = findViewById(R.id.viewBottom);

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

        FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

        UnitControls controls = new UnitControls(game, activity, tankShadow);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        if(tankShadow.GetGeoTarget() != null && game.EntityIsFriendly(tankShadow, game.GetOurPlayer()) && tankShadow.GetMoveOrders() != Movable.MoveOrders.WAIT)
        {
            viewToTarget.setVisibility(VISIBLE);
            txtToTarget.setVisibility(VISIBLE);

            float fltDistanceToTravel = 0;

            if(tankShadow.HasGeoCoordChain())
            {
                fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(tankShadow.GetPosition(), tankShadow.GetCoordinates());
            }
            else
            {
                fltDistanceToTravel = tankShadow.GetPosition().DistanceTo(tankShadow.GetGeoTarget());
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
            if(tankShadow.GetSelling())
            {
                txtName.setVisibility(GONE);
                txtNameEdit.setVisibility(GONE);
                txtNameButton.setVisibility(GONE);
                viewBottom.setVisibility(GONE);
                btnMove.setVisibility(GONE);
                btnSetTarget.setVisibility(GONE);
                btnCeaseFire.setVisibility(GONE);
                imgSell.setImageResource(R.drawable.button_cancel_decommission);
            }
            else
            {
                imgSell.setImageResource(R.drawable.button_decommission);
            }

            btnSell.setVisibility(VISIBLE);

            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.decommission_confirm, context.getString(R.string.mbt_construct_name), TextUtilities.GetCurrencyString(game.GetSaleValue(Defs.TANK_BUILD_COST).get(Resource.ResourceType.WEALTH)), TextUtilities.GetTimeAmount(Defs.DECOMMISSION_TIME)));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.SellEntity(tankShadow.GetPointer());
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

            btnSetTarget.setVisibility(VISIBLE);

            btnSetTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.SetTargetMode(new EntityPointer(tankShadow.GetID(), tankShadow.GetEntityType()), null);
                }
            });
        }

        TextUtilities.AssignHealthStringAndAppearance(txtHP, tankShadow);

        btnCeaseFire.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(tankShadow.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
            }
        });

        if(tankShadow.GetMoveOrders() != MoveOrders.WAIT && bOwnedByPlayer)
            btnCeaseFire.setVisibility(VISIBLE);

        if(game.EntityIsFriendly(game.GetPlayer(tankShadow.GetOwnerID()), game.GetOurPlayer()))
        {
            TextUtilities.AssignTankStatusString(txtTankStatus, tankShadow);
            txtName.setVisibility(GONE);
            txtNameButton.setVisibility(VISIBLE);

            if(tankShadow.GetGeoTarget() != null && tankShadow.GetMoveOrders() != Movable.MoveOrders.WAIT && tankShadow.GetMoveOrders() != Movable.MoveOrders.DEFEND && game.GetTravelTime(Defs.LAND_UNIT_SPEED, tankShadow.GetPosition(), game.GetMovableTarget(tankShadow)) > 0)
            {
                viewToTarget.setVisibility(VISIBLE);
                txtToTarget.setVisibility(VISIBLE);

                float fltDistanceToTravel = 0;

                if(tankShadow.HasGeoCoordChain())
                {
                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(tankShadow.GetPosition(), tankShadow.GetCoordinates());
                }
                else
                {
                    fltDistanceToTravel = tankShadow.GetPosition().DistanceTo(tankShadow.GetGeoTarget());
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
                Tank tank = game.GetTank(tankShadow.GetID());

                if(tank != null)
                {
                    if(game.EntityIsFriendly(tank, game.GetOurPlayer()))
                    {
                        TextUtilities.AssignTankStatusString(txtTankStatus, tank);
                    }
                    else
                    {
                        txtTankStatus.setVisibility(GONE);
                    }

                    //Reload.
                    long oReloadTimeRemaining = tank.GetReloadTimeRemaining();

                    if(oReloadTimeRemaining > 0)
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

                        if(tank.GetSelling())
                        {
                            txtNameButton.setVisibility(GONE);
                            viewBottom.setVisibility(GONE);
                            lytReload.setVisibility(GONE);
                            btnMove.setVisibility(GONE);
                            btnSetTarget.setVisibility(GONE);
                            btnCeaseFire.setVisibility(GONE);
                            txtToTarget.setVisibility(GONE);
                            imgSell.setImageResource(R.drawable.button_cancel_decommission);
                        }
                        else
                        {
                            imgSell.setImageResource(R.drawable.button_decommission);
                        }
                    }
                    else
                    {
                        txtToTarget.setVisibility(GONE);
                        viewToTarget.setVisibility(GONE);
                    }

                    String strName = Utilities.GetEntityName(context, tank);
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
    public List<Tank> GetCurrentTanks()
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
