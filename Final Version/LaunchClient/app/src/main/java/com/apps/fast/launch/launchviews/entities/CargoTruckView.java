package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.launchviews.controls.CargoSystemControl;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Collections;
import java.util.List;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.CargoTruck;
import launch.game.entities.CargoTruckInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.NamableInterface;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.systems.CargoSystem;
import launch.game.systems.ResourceSystem;
import launch.utilities.LaunchUtilities;

/**
 * Created by tobster on 09/11/15.
 */
public class CargoTruckView extends LaunchView implements LaunchUICommon.CargoTruckInfoProvider
{
    private TextView txtTruckTitle;
    private CargoTruckInterface truckShadow;
    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;
    private LinearLayout lytControls;
    private TextView txtHP;
    private TextView txtStorage;
    private TextView txtName;
    private TextView txtTruckStatus;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    protected FrameLayout lytCargo;
    private LinearLayout btnTransferCargo;
    private LinearLayout btnSell;
    private ImageView imgTruck;
    protected LaunchView cargoSystem;
    private boolean bOwnedByPlayer;
    private TextView txtToTarget;
    private TextView txtLoad;
    private View viewToTarget;
    private GeoCoord geoPosition;
    private ResourceSystem resources;
    private LinearLayout lytResources;

    public CargoTruckView(LaunchClientGame game, MainActivity activity, CargoTruckInterface truckShadow)
    {
        super(game, activity, true);
        this.truckShadow = truckShadow;

        bOwnedByPlayer = truckShadow.GetOwnerID() == game.GetOurPlayerID();

        if(truckShadow instanceof CargoTruck)
        {
            cargoSystem = new CargoSystemControl(game, activity, (CargoTruck)truckShadow);
            geoPosition = ((CargoTruck)truckShadow).GetPosition();
            resources = ((CargoTruck)truckShadow).GetResourceSystem();
        }
        else
        {
            cargoSystem = null;
            geoPosition = null;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_cargo_truck, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        btnMove = findViewById(R.id.btnMove);
        lytControls = findViewById(R.id.lytControls);
        txtHP = findViewById(R.id.txtHP);
        lytCargo = findViewById(R.id.lytCargo);
        imgTruck = findViewById(R.id.imgTruck);
        txtLoad = findViewById(R.id.txtLoad);

        txtStorage = findViewById(R.id.txtStorage);
        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);
        txtTruckTitle = findViewById(R.id.txtTruckTitle);
        txtTruckStatus = findViewById(R.id.txtTruckStatus);
        btnTransferCargo = findViewById(R.id.btnTransferCargo);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        lytResources = findViewById(R.id.lytResources);
        btnSell = findViewById(R.id.btnSell);

        txtToTarget = findViewById(R.id.txtToTarget);
        viewToTarget = findViewById(R.id.viewToTarget);

        txtTruckTitle.setText(TextUtilities.GetOwnedEntityName((LaunchEntity)truckShadow, game));
        String strName = Utilities.GetEntityName(context, (NamableInterface)truckShadow);
        txtName.setText(strName);
        txtNameButton.setText(strName);

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.MoveOrderMode(((LaunchEntity)truckShadow).GetPointer(), null);
            }
        });

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
                game.SetEntityName(((LaunchEntity)truckShadow).GetPointer(), txtNameEdit.getText().toString());

                txtNameButton.setVisibility(VISIBLE);
                lytNameEdit.setVisibility(GONE);
                Utilities.DismissKeyboard(activity, txtNameEdit);
            }
        });

        if(truckShadow instanceof CargoTruck)
        {
            CargoTruck truck = (CargoTruck)truckShadow;
            TextUtilities.AssignHealthStringAndAppearance(txtHP, truck);

            FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

            UnitControls controls = new UnitControls(game, activity, truck);
            lytUnitControls.removeAllViews();
            lytUnitControls.addView(controls);

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
                            launchDialog.SetMessage(context.getString(R.string.sell_land_unit_confirm, TextUtilities.GetCostStatement(game.GetSaleValue(game.GetLandUnitValue(truck)))));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.SellEntity(truck.GetPointer());
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

                btnCeaseFire.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.UnitCommand(Movable.MoveOrders.WAIT, Collections.singletonList(truck.GetPointer()), null, null, CargoSystem.LootType.NONE, LaunchEntity.ID_NONE, LaunchEntity.ID_NONE);
                    }
                });

                if(truck.GetMoveOrders() != Movable.MoveOrders.WAIT)
                {
                    btnCeaseFire.setVisibility(VISIBLE);
                }
                else
                {
                    btnCeaseFire.setVisibility(GONE);
                }

                Utilities.DrawResourceSystem(context, resources, lytResources);

                txtLoad.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.LoadLootMode(truck);
                    }
                });
            }

            if(game.EntityIsFriendly(game.GetPlayer(truck.GetOwnerID()), game.GetOurPlayer()))
            {
                TextUtilities.AssignCargoTruckStatusString(txtTruckStatus, truck);

                if(truck.GetGeoTarget() != null && game.EntityIsFriendly(truck, game.GetOurPlayer()) && truck.GetMoveOrders() != Movable.MoveOrders.WAIT)
                {
                    viewToTarget.setVisibility(VISIBLE);
                    txtToTarget.setVisibility(VISIBLE);

                    float fltDistanceToTravel = 0;

                    if(truck.HasGeoCoordChain())
                    {
                        fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(truck.GetPosition(), truck.GetCoordinates());
                    }
                    else
                    {
                        fltDistanceToTravel = truck.GetPosition().DistanceTo(truck.GetGeoTarget());
                    }

                    txtToTarget.setText(context.getString(R.string.travel_time_target, TextUtilities.GetTimeAmount((long)(fltDistanceToTravel/Defs.LAND_UNIT_SPEED * Defs.MS_PER_HOUR))));
                }
                else
                {
                    viewToTarget.setVisibility(GONE);
                    txtToTarget.setVisibility(GONE);
                }

                btnTransferCargo.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.TransferCargoMode(null, truck, true);
                    }
                });
            }
            else
            {
                viewToTarget.setVisibility(GONE);
                txtToTarget.setVisibility(GONE);
                txtTruckStatus.setVisibility(GONE);
                txtStorage.setVisibility(GONE);
            }
        }
        else
        {
            StoredCargoTruck truck = ((StoredCargoTruck)truckShadow);
            TextUtilities.AssignHealthStringAndAppearance(txtHP, truck);
        }

        if(bOwnedByPlayer)
        {
            if(cargoSystem != null)
            {
                lytCargo.addView(cargoSystem);
                lytCargo.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));
            }
            else
            {
                lytCargo.setVisibility(GONE);
            }

            txtName.setVisibility(GONE);
            txtNameEdit.setText(truckShadow.GetName());
        }
        else
        {
            btnMove.setVisibility(GONE);
            lytNameEdit.setVisibility(GONE);
            txtNameButton.setVisibility(GONE);
            btnTransferCargo.setVisibility(GONE);
        }
    }

    @Override
    public void Update()
    {
        super.Update();

        if(truckShadow instanceof CargoTruck)
        {
            geoPosition = ((CargoTruck)truckShadow).GetPosition();
        }

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                CargoTruckInterface truckInterface = game.GetCargoTruckInterface(truckShadow.GetID());

                if(truckInterface != null)
                {
                    if(game.EntityIsFriendly((LaunchEntity)truckInterface, game.GetOurPlayer()))
                    {
                        TextUtilities.AssignCargoTruckStatusString(txtTruckStatus, truckInterface);
                    }
                    else
                    {
                        txtTruckStatus.setVisibility(GONE);
                    }

                    if(truckInterface instanceof CargoTruck)
                    {
                        CargoTruck truck = (CargoTruck)truckInterface;
                        TextUtilities.AssignHealthStringAndAppearance(txtHP, truck);

                        if(bOwnedByPlayer)
                        {
                            resources = truck.GetResourceSystem();
                            Utilities.DrawResourceSystem(context, resources, lytResources);
                        }

                        if(bOwnedByPlayer && truck.GetMoveOrders() != Movable.MoveOrders.WAIT)
                        {
                            btnCeaseFire.setVisibility(VISIBLE);
                        }
                        else
                        {
                            btnCeaseFire.setVisibility(GONE);
                        }

                        if(game.EntityIsFriendly(game.GetPlayer(truck.GetOwnerID()), game.GetOurPlayer()))
                        {
                            cargoSystem = new CargoSystemControl(game, activity, truck);
                            lytCargo.removeAllViews();
                            lytCargo.addView(cargoSystem);

                            if(truck.GetGeoTarget() != null && game.EntityIsFriendly(truck, game.GetOurPlayer()) && truck.GetMoveOrders() != Movable.MoveOrders.WAIT)
                            {
                                viewToTarget.setVisibility(VISIBLE);
                                txtToTarget.setVisibility(VISIBLE);

                                float fltDistanceToTravel = 0;

                                if(truck.HasGeoCoordChain())
                                {
                                    fltDistanceToTravel = LaunchUtilities.GetTotalTravelDistance(truck.GetPosition(), truck.GetCoordinates());
                                }
                                else
                                {
                                    fltDistanceToTravel = truck.GetPosition().DistanceTo(truck.GetGeoTarget());
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
                            viewToTarget.setVisibility(GONE);
                            txtToTarget.setVisibility(GONE);
                        }
                    }
                    else
                    {
                        StoredCargoTruck truck = ((StoredCargoTruck)truckInterface);
                        TextUtilities.AssignHealthStringAndAppearance(txtHP, truck);
                    }

                    String strName = Utilities.GetEntityName(context, (NamableInterface)truckInterface);
                    txtName.setText(strName);
                    txtNameButton.setText(strName);
                }
                else
                {
                    Log.i("LaunchWTF", "Truckinterface is null. Finishing... (AircraftView ln 560)");
                    Finish(true);
                }
            }
        });
    }

    @Override
    public boolean IsSingleCargoTruck()
    {
        return true;
    }

    @Override
    public CargoTruckInterface GetCurrentCargoTruck()
    {
        return game.GetCargoTruckInterface(truckShadow.GetID());
    }

    @Override
    public List<CargoTruckInterface> GetCurrentCargoTrucks()
    {
        return null;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals((LaunchEntity)truckShadow))
        {
            Update();
        }
    }
}
