package com.apps.fast.launch.launchviews.entities;

import static android.view.Gravity.CENTER_VERTICAL;
import static android.widget.LinearLayout.HORIZONTAL;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import launch.comm.clienttasks.SellEntityTask;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Infantry;
import launch.game.entities.InfantryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.NamableInterface;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.systems.CargoSystem;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchUtilities;

public class InfantryView extends LaunchView implements LaunchUICommon.InfantryInfoProvider
{
    private TextView txtInfantryTitle;

    private InfantryInterface infantryShadow;
    private TextView txtInfantryStatus;
    private LinearLayout btnTransferCargo;
    private LinearLayout btnSetTarget;
    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;
    private LinearLayout btnDigIn;
    private LinearLayout btnCapture;
    private LinearLayout btnLiberate;
    private LinearLayout lytControls;
    private LinearLayout btnSell;
    private TextView txtHP;
    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    protected ImageView imgInfantry;

    private LinearLayout lytReload;
    private TextView txtReloading;

    private TextView txtToTarget;
    private View viewToTarget;
    private boolean bOwnedByPlayer;
    private GeoCoord geoPosition;
    private ResourceSystem resources;
    private LinearLayout lytResources;
    private TextView txtLoad;

    public InfantryView(LaunchClientGame game, MainActivity activity, InfantryInterface infantry)
    {
        super(game, activity, true);
        this.infantryShadow = infantry;

        bOwnedByPlayer = infantryShadow.GetOwnerID() == game.GetOurPlayerID();

        geoPosition = ((Infantry)infantryShadow.GetInfantry()).GetPosition();

        resources = ((Infantry)infantryShadow.GetInfantry()).GetResourceSystem();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_infantry, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        btnMove = findViewById(R.id.btnMove);
        btnDigIn = findViewById(R.id.btnDigIn);
        btnCapture = findViewById(R.id.btnCapture);
        btnLiberate = findViewById(R.id.btnLiberate);
        lytControls = findViewById(R.id.lytControls);
        btnSetTarget = findViewById(R.id.btnSetTarget);
        txtHP = findViewById(R.id.txtHP);
        txtInfantryStatus = findViewById(R.id.txtInfantryStatus);
        btnTransferCargo = findViewById(R.id.btnTransferCargo);
        lytResources = findViewById(R.id.lytResources);
        txtLoad = findViewById(R.id.txtLoad);

        lytReload = (LinearLayout) findViewById(R.id.lytReload);
        txtReloading = (TextView) findViewById(R.id.txtReloading);

        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);
        txtInfantryTitle = findViewById(R.id.txtInfantryTitle);
        imgInfantry = findViewById(R.id.imgInfantry);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        btnSell = findViewById(R.id.btnSell);
        txtLoad = findViewById(R.id.txtLoad);

        txtInfantryTitle.setText(TextUtilities.GetOwnedEntityName(infantryShadow.GetInfantry(), game));
        String strName = Utilities.GetEntityName(context, (NamableInterface)infantryShadow);
        txtName.setText(strName);
        txtNameEdit.setText(strName);
        txtNameButton.setText(strName);

        txtToTarget = findViewById(R.id.txtToTarget);
        viewToTarget = findViewById(R.id.viewToTarget);

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.MoveOrderMode(infantryShadow.GetInfantry().GetPointer(), null);
            }
        });

        if(infantryShadow instanceof Infantry)
        {
            Infantry infantry = (Infantry)infantryShadow;
            imgInfantry.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, infantry));
            TextUtilities.AssignHealthStringAndAppearance(txtHP, infantry);

            FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

            UnitControls controls = new UnitControls(game, activity, infantry);
            lytUnitControls.removeAllViews();
            lytUnitControls.addView(controls);

            if(game.EntityIsFriendly(game.GetPlayer(infantryShadow.GetOwnerID()), game.GetOurPlayer()))
            {
                //TODO: We need all the infantry stats here. We're going to need them all in Infantry.java and StoredInfantry, too.
                TextUtilities.AssignInfantryStatusString(txtInfantryStatus, infantryShadow);

                if(infantry.GetGeoTarget() != null && game.EntityIsFriendly(infantry, game.GetOurPlayer()) && infantry.GetMoveOrders() != Movable.MoveOrders.WAIT  && infantry.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                {
                    viewToTarget.setVisibility(VISIBLE);
                    txtToTarget.setVisibility(VISIBLE);

                    float fltDistanceToTravel = 0;

                    if(infantry.HasGeoCoordChain())
                    {
                        fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(infantry.GetPosition(), infantry.GetCoordinates());
                    }
                    else if(infantry.GetMoveOrders() == MoveOrders.CAPTURE || infantry.GetMoveOrders() == MoveOrders.ATTACK || infantry.GetMoveOrders() == MoveOrders.LIBERATE)
                    {
                        if(infantry.GetTarget() != null && infantry.GetTarget().GetMapEntity(game) != null)
                        {
                            fltDistanceToTravel = infantry.GetPosition().DistanceTo(infantry.GetTarget().GetMapEntity(game).GetPosition());
                        }
                    }
                    else
                    {
                        fltDistanceToTravel = infantry.GetPosition().DistanceTo(infantry.GetGeoTarget());
                    }

                    if(bOwnedByPlayer)
                    {
                        btnCeaseFire.setVisibility(VISIBLE);
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
                                    launchDialog.SetMessage(context.getString(R.string.sell_land_unit_confirm, TextUtilities.GetCostStatement(game.GetSaleValue(game.GetLandUnitValue(infantry)))));
                                    launchDialog.SetOnClickYes(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            launchDialog.dismiss();
                                            game.SellEntity(infantry.GetPointer());
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
                txtInfantryStatus.setVisibility(GONE);
                viewToTarget.setVisibility(GONE);
                txtToTarget.setVisibility(GONE);
            }

            if(infantry.GetMobile())
            {
                btnDigIn.setVisibility(VISIBLE);
                //btnTurn.setVisibility(GONE);
            }
            else
            {
                btnDigIn.setVisibility(GONE);
                //btnTurn.setVisibility(VISIBLE);
            }

            btnTransferCargo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.TransferCargoMode(null, infantry, true);
                }
            });

            /*btnTurn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.TurnInfantryMode(infantryShadow.GetID());
                }
            });*/

            btnCeaseFire.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(infantry.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
                }
            });

            btnDigIn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(infantry.GetMobile())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.dig_in_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.UnitCommand(Movable.MoveOrders.DEFEND, Collections.singletonList(infantryShadow.GetInfantry().GetPointer()), null, null, CargoSystem.LootType.NONE, 0, 0);
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

            btnCapture.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.CaptureTargetMode(infantryShadow.GetInfantry().GetPointer());
                }
            });

            btnLiberate.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.LiberateTargetMode(infantryShadow.GetInfantry().GetPointer());
                }
            });
        }
        else
        {
            StoredInfantry infantry = (StoredInfantry)infantryShadow;
            TextUtilities.AssignHealthStringAndAppearance(txtHP, infantry);
            btnDigIn.setVisibility(GONE);
            btnCapture.setVisibility(GONE);
            btnLiberate.setVisibility(GONE);
        }

        if(bOwnedByPlayer)
        {
            if(infantryShadow instanceof Infantry)
            {
                Infantry infantry = (Infantry)infantryShadow;

                btnSetTarget.setVisibility(VISIBLE);

                btnSetTarget.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SetTargetMode(new EntityPointer(infantry.GetID(), infantry.GetEntityType()), null);
                    }
                });

                Utilities.DrawResourceSystem(context, resources, lytResources);

                txtLoad.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.LoadLootMode(infantry);
                    }
                });
            }

            txtName.setVisibility(GONE);

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
                    activity.MoveOrderMode(infantryShadow.GetInfantry().GetPointer(), null);
                }
            });

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(((LaunchEntity)infantryShadow).GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });
        }
        else
        {
            btnMove.setVisibility(GONE);
            //btnTurn.setVisibility(GONE);
            btnCapture.setVisibility(GONE);
            btnLiberate.setVisibility(GONE);
            btnDigIn.setVisibility(GONE);
            lytNameEdit.setVisibility(GONE);
            txtNameButton.setVisibility(GONE);
            btnTransferCargo.setVisibility(GONE);
        }
    }

    @Override
    public void Update()
    {
        super.Update();

        if(infantryShadow.Deployed())
        {
            geoPosition = ((Infantry)infantryShadow.GetInfantry()).GetPosition();
        }

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                InfantryInterface infantryInterface = game.GetInfantryInterface(infantryShadow.GetID());

                if(infantryInterface != null)
                {
                    if(game.EntityIsFriendly(infantryShadow.GetInfantry(), game.GetOurPlayer()))
                    {
                        TextUtilities.AssignInfantryStatusString(txtInfantryStatus, infantryShadow);
                    }
                    else
                    {
                        txtInfantryStatus.setVisibility(GONE);
                    }

                    if(infantryInterface instanceof Infantry)
                    {
                        Infantry infantry = ((Infantry)infantryInterface);

                        //Reload.
                        long oReloadTimeRemaining = infantry.GetReloadTimeRemaining();

                        if (oReloadTimeRemaining > 0)
                        {
                            lytReload.setVisibility(VISIBLE);
                            txtReloading.setText(TextUtilities.GetTimeAmount(oReloadTimeRemaining));
                        }
                        else
                        {
                            lytReload.setVisibility(GONE);
                        }

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, infantry);

                        if(game.EntityIsFriendly(game.GetPlayer(infantryShadow.GetOwnerID()), game.GetOurPlayer()))
                        {
                            //TODO: We need all the infantry stats here. We're going to need them all in Infantry.java and StoredInfantry, too.
                            TextUtilities.AssignInfantryStatusString(txtInfantryStatus, infantryInterface);

                            if(infantry.GetGeoTarget() != null && game.EntityIsFriendly(infantry, game.GetOurPlayer()) && infantry.GetMoveOrders() != Movable.MoveOrders.WAIT  && infantry.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                            {
                                viewToTarget.setVisibility(VISIBLE);
                                txtToTarget.setVisibility(VISIBLE);

                                float fltDistanceToTravel = 0;

                                if(infantry.HasGeoCoordChain())
                                {
                                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(infantry.GetPosition(), infantry.GetCoordinates());
                                }
                                else if(infantry.GetMoveOrders() == MoveOrders.CAPTURE || infantry.GetMoveOrders() == MoveOrders.ATTACK || infantry.GetMoveOrders() == MoveOrders.LIBERATE)
                                {
                                    if(infantry.GetTarget() != null && infantry.GetTarget().GetMapEntity(game) != null)
                                    {
                                        fltDistanceToTravel = infantry.GetPosition().DistanceTo(infantry.GetTarget().GetMapEntity(game).GetPosition());
                                    }
                                }
                                else
                                {
                                    fltDistanceToTravel = infantry.GetPosition().DistanceTo(infantry.GetGeoTarget());
                                }

                                if(bOwnedByPlayer)
                                    btnCeaseFire.setVisibility(VISIBLE);

                                txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.LAND_UNIT_SPEED * Defs.MS_PER_HOUR))));
                            }
                            else
                            {
                                viewToTarget.setVisibility(GONE);
                                txtToTarget.setVisibility(GONE);
                                btnCeaseFire.setVisibility(GONE);
                            }
                        }
                        else
                        {
                            viewToTarget.setVisibility(GONE);
                            txtToTarget.setVisibility(GONE);
                            txtInfantryStatus.setVisibility(GONE);
                            btnTransferCargo.setVisibility(GONE);
                            btnCeaseFire.setVisibility(GONE);
                        }

                        if(infantry.GetOwnedBy(game.GetOurPlayerID()))
                        {
                            resources = infantry.GetResourceSystem();
                            Utilities.DrawResourceSystem(context, resources, lytResources);
                        }
                    }
                    else
                    {
                        StoredInfantry infantry = (StoredInfantry)infantryInterface;

                        TextUtilities.AssignHealthStringAndAppearance(txtHP, infantry);

                        if(game.EntityIsFriendly(game.GetPlayer(infantryShadow.GetOwnerID()), game.GetOurPlayer()))
                        {
                            //TODO: We need all the infantry stats here. We're going to need them all in Infantry.java and StoredInfantry, too.
                            TextUtilities.AssignInfantryStatusString(txtInfantryStatus, infantryInterface);
                        }
                        else
                        {
                            txtInfantryStatus.setVisibility(GONE);
                        }
                    }

                    String strName = Utilities.GetEntityName(context, (NamableInterface)infantryInterface);
                    txtName.setText(strName);
                    txtNameButton.setText(strName);
                }
                else
                {
                    Log.i("LaunchWTF", "AirplaneInterface is null. Finishing... (AircraftView ln 560)");
                    Finish(true);
                }
            }
        });
    }

    private void DrawResourceSystem()
    {
        if(resources != null && !resources.GetTypes().isEmpty())
        {
            lytResources.removeAllViews();
            int lViewsAdded = 0;

            for(Map.Entry<Resource.ResourceType, Long> entry : resources.GetTypes().entrySet())
            {
                Resource.ResourceType type = entry.getKey();
                long oAmount = entry.getValue();

                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                int lTopPadding = 0;
                int lBottomPadding = 0;

                if(lViewsAdded == 0)
                    lTopPadding = 4;
                else if(lViewsAdded == resources.GetTypes().size() - 1)
                    lBottomPadding = 4;

                row.setPadding(4, lTopPadding, 0, lBottomPadding);
                row.setGravity(CENTER_VERTICAL);

                ImageView icon = new ImageView(context);

                icon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                icon.setImageBitmap(EntityIconBitmaps.GetResourceTypeBitmap(getContext(), type));

                TextView txt = new TextView(getContext());

                LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(Utilities.GetdpToPx(context, 64), LinearLayout.LayoutParams.WRAP_CONTENT);

                txtParams.setMargins(4, 0, 8, 0);

                txt.setLayoutParams(txtParams);
                txt.setGravity(Gravity.END);
                txt.setText(String.valueOf(oAmount));
                txt.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                txt.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));

                row.addView(icon);
                row.addView(txt);
                lytResources.addView(row);
                lViewsAdded++;
            }
        }
    }

    @Override
    public boolean IsSingleInfantry()
    {
        return true;
    }

    @Override
    public InfantryInterface GetCurrentInfantry()
    {
        return game.GetInfantryInterface(infantryShadow.GetID());
    }

    @Override
    public List<InfantryInterface> GetCurrentInfantries()
    {
        return null;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals((LaunchEntity)infantryShadow))
        {
            Update();
        }
    }
}
