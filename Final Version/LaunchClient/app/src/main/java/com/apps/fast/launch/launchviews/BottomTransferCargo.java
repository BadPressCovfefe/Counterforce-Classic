package com.apps.fast.launch.launchviews;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.entities.AirplaneView;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Armory;
import launch.game.entities.LandUnit;
import launch.game.entities.LauncherInterface;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.entities.Ship;
import launch.game.entities.Shipyard;
import launch.game.entities.Structure;
import launch.game.entities.Submarine;
import launch.game.entities.Warehouse;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.CargoSystem.LootType;
import launch.game.entities.Airbase;
import launch.game.entities.Haulable;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LaunchEntity;

public class BottomTransferCargo extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtSelectReciever;
    private TextView txtTransferCargoHint;
    private LinearLayout btnTransferCargo;
    private ImageView imgTransferCargo;
    private LinearLayout lytContents;
    private EditText txtPriceEach;
    private TextView txtTotalPrice;
    private LinearLayout lytIncrement;
    private LinearLayout lytSalePrice;
    private LinearLayout lytTotalPrice;
    private EditText txtQuantityEdit;
    private LinearLayout btnDropCargo;
    private ImageView imgDropCargo;

    private LaunchEntity receiver;
    private LaunchEntity deliverer;
    private LootType typeToDeliver;
    private int lTypeID;
    private int lQuantityToDeliver;
    private boolean bLoadAsCargo = true;
    private boolean bAtMax = true;
    private int lIncrementAmount = 1000;
    private long oTotalPrice = 0;
    private float fltPriceEach = Defs.DEFAULT_PRICE_EACH;

    private GoogleMap map;
    private Polyline targetTrajectory;
    private Haulable haulable;
    private LaunchEntity hauler;
    private boolean bFromCargo;
    private boolean bCreatingListing;
    private GeoCoord geoFrom;
    private GeoCoord geoTo;
    private float fltLoadDistance;

    private List<AircraftBaseView> StoredAircrafts = new ArrayList<>();

    /** Delivering a haulable. */
    public BottomTransferCargo(LaunchClientGame game, MainActivity activity, LaunchEntity hauler, Haulable haulable, boolean bFromCargo)
    {
        super(game, activity, true);
        this.hauler = hauler;
        this.haulable = haulable;
        this.typeToDeliver = haulable.GetLootType();
        this.lTypeID = haulable.GetCargoID();
        this.lQuantityToDeliver = (int)haulable.GetQuantity();
        this.deliverer = hauler;
        this.bFromCargo = bFromCargo;
        this.geoFrom = game.GetPosition(hauler);
        this.oTotalPrice = (long)(lQuantityToDeliver * fltPriceEach);

        Setup();
    }

    /** Boarding a land unit into a hauler. */
    public BottomTransferCargo(LaunchClientGame game, MainActivity activity, Haulable haulable)
    {
        super(game, activity, true);
        this.haulable = haulable;
        this.typeToDeliver = haulable.GetLootType();
        this.lTypeID = haulable.GetCargoID();
        this.lQuantityToDeliver = 0;
        this.deliverer = ((LandUnit)haulable);
        this.geoFrom = ((MapEntity)haulable).GetPosition();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_transfer_cargo, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtSelectReciever = findViewById(R.id.txtSelectReciever);
        btnTransferCargo = findViewById(R.id.btnTransferCargo);
        imgTransferCargo = findViewById(R.id.imgTransferCargo);
        lytContents = findViewById(R.id.lytContents);
        txtTransferCargoHint = findViewById(R.id.txtTransferCargoHint);
        lytSalePrice = findViewById(R.id.lytSalePrice);
        lytTotalPrice = findViewById(R.id.lytTotalPrice);
        txtPriceEach = findViewById(R.id.txtPriceEach);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtQuantityEdit = findViewById(R.id.txtQuantityEdit);
        lytIncrement = findViewById(R.id.lytIncrement);

        btnDropCargo = findViewById(R.id.btnDropCargo);
        imgDropCargo = findViewById(R.id.imgDropCargo);

        if(hauler instanceof Airplane)
        {
            imgDropCargo.setImageResource(R.drawable.button_airdrop_cargo);
            btnTransferCargo.setVisibility(GONE);
        }
        else if(hauler instanceof Structure)
        {
            //btnTransferCargo.setVisibility(GONE);
        }

        if(haulable instanceof LandUnit)
        {
            SetupLoadLandUnit();
        }
        else if(haulable instanceof StoredTank || haulable instanceof StoredCargoTruck || haulable instanceof StoredInfantry)
        {
            SetupLandUnitTransfer();
        }
        else
        {
            SetupCargoTransfer();
        }
    }

    protected void SetupCargoTransfer()
    {
        txtQuantityEdit.setText(String.valueOf(lQuantityToDeliver));

        txtQuantityEdit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                String strInput = txtQuantityEdit.getText().toString().trim();

                if(!strInput.isEmpty())
                {
                    lQuantityToDeliver = Integer.parseInt(strInput);
                }

                Update();
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        txtPriceEach.setText(String.valueOf(fltPriceEach));

        txtPriceEach.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                String strInput = txtPriceEach.getText().toString().trim();

                try
                {
                    if(!strInput.isEmpty())
                    {
                        fltPriceEach = Float.parseFloat(strInput);

                        if(fltPriceEach < Defs.MINIMUM_PRICE_EACH)
                        {
                            fltPriceEach = Defs.MINIMUM_PRICE_EACH;
                        }

                        oTotalPrice = (long)(lQuantityToDeliver * fltPriceEach);
                    }
                }
                catch(Exception ignored)
                {
                    activity.ShowBasicOKDialog("You must input a valid price.");
                }

                Update();
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        btnTransferCargo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(hauler != null && haulable != null && receiver != null)
                {
                    if((haulable.GetLootType() == LootType.INTERCEPTORS || haulable.GetLootType() == LootType.MISSILES) && receiver instanceof LauncherInterface && receiver instanceof HaulerInterface)
                    {
                        //The player is transferring launchables and the receiver has cargo and a missile system. Give the player the option of which one they want to load it into.
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.cargo_transfer_option_launchables));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = true;
                                TransferCargo();
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = false;
                                TransferCargo();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                    else
                    {
                        TransferCargo();
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_receiver));
                }
            }
        });

        if(hauler instanceof Airplane)
        {
            imgDropCargo.setImageResource(R.drawable.button_airdrop_cargo);
            btnTransferCargo.setVisibility(GONE);
        }

        if(!(hauler instanceof Player))
        {
            btnDropCargo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(hauler != null && haulable != null && lQuantityToDeliver > 0)
                    {
                        if(!(hauler instanceof Structure) || ((Structure)hauler).GetOnline())
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderPurchase();
                            launchDialog.SetMessage(context.getString(R.string.drop_cargo_confirm));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    receiver = null;
                                    TransferCargo();
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
                            activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                        }
                    }
                }
            });
        }
        else
        {
            btnDropCargo.setVisibility(GONE);
        }

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    protected void SetupLandUnitTransfer()
    {
        lytIncrement.setVisibility(GONE);
        lytSalePrice.setVisibility(GONE);
        lytTotalPrice.setVisibility(GONE);

        btnTransferCargo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(hauler != null && haulable != null && receiver != null)
                {
                    if(geoTo != null && geoFrom != null && geoTo.DistanceTo(geoFrom) > fltLoadDistance)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                    }
                    else if(!game.LoadHaulableValid(haulable, receiver, hauler))
                    {
                        if(receiver instanceof Submarine)
                        {
                            //Display "submarines can't receive loot."
                            activity.ShowBasicOKDialog(context.getString(R.string.submarines_cant_receive_cargo));
                        }
                        else if(!(hauler instanceof Shipyard) && receiver instanceof Ship && !((Ship)receiver).HasAmphibious())
                        {
                            //Display "The ship does not have amphibious capability. Only a shipyard or port can transfer cargo to or from it."
                            activity.ShowBasicOKDialog(context.getString(R.string.ship_cant_receive_cargo));
                        }
                        else if(receiver instanceof Airplane)
                        {
                            //Airplane must land before they can receive cargo.
                            activity.ShowBasicOKDialog(context.getString(R.string.aircraft_must_land_for_cargo));
                        }
                        else if(receiver instanceof StoredAirplane && !((StoredAirplane)receiver).HasCargo())
                        {
                            //Display "This aircraft cannot receive cargo."
                            activity.ShowBasicOKDialog(context.getString(R.string.aircraft_cant_receive_cargo));
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.cargo_receiver_invalid));
                        }
                    }
                    else
                    {
                        TransferLandUnit();
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_receiver));
                }
            }
        });

        if(deliverer instanceof Armory || deliverer instanceof Warehouse || deliverer instanceof Shipyard || deliverer instanceof Airplane || deliverer instanceof StoredAirplane || (deliverer instanceof Ship && ((Ship)deliverer).HasAmphibious()))
        {
            btnDropCargo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(hauler != null && haulable != null)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.drop_unit_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                receiver = null;
                                DropLandUnit();
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
            btnDropCargo.setVisibility(GONE);
        }

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    protected void SetupLoadLandUnit()
    {
        lytIncrement.setVisibility(GONE);
        lytSalePrice.setVisibility(GONE);
        lytTotalPrice.setVisibility(GONE);

        btnTransferCargo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(receiver != null)
                {
                    if(geoTo != null && geoFrom != null && geoTo.DistanceTo(geoFrom) > fltLoadDistance)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.target_out_of_range));
                    }
                    else if(lQuantityToDeliver > haulable.GetQuantity())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_resources));
                    }
                    else if(!game.LoadHaulableValid(haulable, receiver, hauler))
                    {
                        if(receiver instanceof Submarine)
                        {
                            //Display "submarines can't receive loot."
                            activity.ShowBasicOKDialog(context.getString(R.string.submarines_cant_receive_cargo));
                        }
                        else if(!(hauler instanceof Shipyard) && receiver instanceof Ship && !((Ship)receiver).HasAmphibious())
                        {
                            //Display "The ship does not have amphibious capability. Only a shipyard or port can transfer cargo to or from it.
                            activity.ShowBasicOKDialog(context.getString(R.string.ship_cant_receive_cargo));
                        }
                        else if(receiver instanceof Airplane)
                        {
                            //Airplane must land before they can receive cargo.
                            activity.ShowBasicOKDialog(context.getString(R.string.aircraft_must_land_for_cargo));
                        }
                        else if(receiver instanceof StoredAirplane && !((StoredAirplane)receiver).HasCargo())
                        {
                            //Display "This aircraft cannot receive cargo."
                            activity.ShowBasicOKDialog(context.getString(R.string.aircraft_cant_receive_cargo));
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.cargo_receiver_invalid));
                        }
                    }
                    else if(receiver instanceof HaulerInterface && !((HaulerInterface)receiver).GetCargoSystem().WeightCanFit(haulable.GetQuantity()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.cargo_cant_fit, TextUtilities.GetWeightStringFromKG(haulable.GetQuantity())));
                    }
                    else
                    {
                        LoadLandUnit();
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_receiver));
                }
            }
        });

        btnDropCargo.setVisibility(GONE);

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    private void TransferCargo()
    {
        if(receiver == null)
        {
            //Receiver is null, so we are dropping cargo on the ground.
            game.TransferCargo(deliverer.GetPointer(), null, typeToDeliver, lTypeID, lQuantityToDeliver, bLoadAsCargo, bFromCargo);
        }
        else if(receiver instanceof Structure && !((Structure)receiver).GetOnline())
        {
            activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
        }
        else if(haulable.GetLootType() == LootType.INTERCEPTORS || haulable.GetLootType() == LootType.MISSILES)
        {
            if(game.LaunchableTransferLegal(hauler, receiver, haulable.GetCargoID(), haulable.GetLootType() == LootType.MISSILES, bLoadAsCargo))
            {
                game.TransferCargo(deliverer.GetPointer(), receiver.GetPointer(), typeToDeliver, lTypeID, lQuantityToDeliver, bLoadAsCargo, bFromCargo);
            }
        }
        else
        {
            if(game.CargoTransferLegal(hauler, receiver, typeToDeliver))
            {
                game.TransferCargo(deliverer.GetPointer(), receiver.GetPointer(), typeToDeliver, lTypeID, lQuantityToDeliver, bLoadAsCargo, bFromCargo);
            }
        }

        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        if(deliverer instanceof MapEntity)
        {
            activity.SelectEntity((MapEntity)deliverer);
        }
        else if(deliverer instanceof StoredAirplane)
        {
            MainNormalView mainView = new MainNormalView(game, activity);
            activity.SetView(mainView);
            mainView.BottomLayoutShowView(new AirplaneView(game, activity, (AirplaneInterface)deliverer, true));
        }
        /*else if(lootReceiver instanceof StoredTrain)
        {
            //TODO: When trains are added, all the cargo methods will need to be updated to include them.
        }*/
    }

    private void DropLandUnit()
    {
        game.DropLandUnit(((LaunchEntity)haulable).GetPointer());

        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        if(hauler instanceof MapEntity)
        {
            activity.SelectEntity((MapEntity)hauler);
        }
        else if(hauler instanceof StoredAirplane)
        {
            MainNormalView mainView = new MainNormalView(game, activity);
            activity.SetView(mainView);
            mainView.BottomLayoutShowView(new AirplaneView(game, activity, (AirplaneInterface)hauler, true));
        }
        /*else if(lootReceiver instanceof StoredTrain)
        {
            //TODO: When trains are added, all the cargo methods will need to be updated to include them.
        }*/
    }

    private void LoadLandUnit()
    {
        if(receiver != null)
        {
            game.LoadLandUnit(((LaunchEntity)haulable).GetPointer(), receiver.GetPointer());
        }

        activity.InformationMode(false);
    }

    private void TransferLandUnit()
    {
        game.TransferLandUnit(deliverer.GetPointer(), receiver.GetPointer(), ((LaunchEntity)haulable).GetPointer());


        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        if(deliverer instanceof MapEntity)
        {
            activity.SelectEntity((MapEntity)deliverer);
        }
        else if(deliverer instanceof StoredAirplane)
        {
            MainNormalView mainView = new MainNormalView(game, activity);
            activity.SetView(mainView);
            mainView.BottomLayoutShowView(new AirplaneView(game, activity, (AirplaneInterface)deliverer, true));
        }
        /*else if(lootReceiver instanceof StoredTrain)
        {
            //TODO: When trains are added, all the cargo methods will need to be updated to include them.
        }*/
    }

    public void EntitySelected(MapEntity entity, Polyline targetTrajectory, GoogleMap map)
    {
        this.receiver = entity;
        txtSelectReciever.setText(context.getString(R.string.cargo_receiver_selected, TextUtilities.GetEntityTypeAndName(entity, game)));
        this.targetTrajectory = targetTrajectory;
        this.map = map;
        this.geoTo = entity.GetPosition();

        //This entire class could probably be made more efficient, without having so many different entities. (Hauler, receiver, deliverer, etc)
        if(deliverer instanceof Shipyard || receiver instanceof Shipyard || hauler instanceof Shipyard)
        {
            fltLoadDistance = Defs.SHIPYARD_LOAD_DISTANCE;
        }
        else
        {
            fltLoadDistance = Defs.LOAD_DISTANCE;
        }

        if(haulable.GetLootType() == LootType.RESOURCES && haulable.GetCargoID() != ResourceType.WEALTH.ordinal())
        {
            if(entity instanceof HaulerInterface)
            {
                HaulerInterface hauler = (HaulerInterface)entity;

                if(hauler.GetCargoSystem() != null)
                {
                    if(haulable.GetQuantity() > hauler.GetCargoSystem().GetRemainingCapacity())
                    {
                        lQuantityToDeliver = (int)hauler.GetCargoSystem().GetRemainingCapacity();
                        txtQuantityEdit.setText(String.valueOf(lQuantityToDeliver));
                    }
                    else
                    {
                        lQuantityToDeliver = (int)haulable.GetQuantity();
                        txtQuantityEdit.setText(String.valueOf(lQuantityToDeliver));
                    }
                }
            }
            else
            {
                lQuantityToDeliver = (int)haulable.GetQuantity();
                txtQuantityEdit.setText(String.valueOf(lQuantityToDeliver));
            }
        }

        if(geoTo != null && geoFrom != null && geoTo.DistanceTo(geoFrom) > fltLoadDistance)
        {
            txtTransferCargoHint.setVisibility(VISIBLE);
            txtTransferCargoHint.setText(context.getString(R.string.target_out_of_range));
        }
        else if(!game.LoadHaulableValid(haulable, receiver, hauler))
        {
            txtTransferCargoHint.setVisibility(VISIBLE);

            if(receiver instanceof Submarine)
            {
                txtTransferCargoHint.setText(context.getString(R.string.submarines_cant_receive_cargo));
            }
            else if(!(hauler instanceof Shipyard) && receiver instanceof Ship && !((Ship)receiver).HasAmphibious())
            {
                txtTransferCargoHint.setText(context.getString(R.string.ship_cant_receive_cargo));
            }
            else if(receiver instanceof Airplane)
            {
                txtTransferCargoHint.setText(context.getString(R.string.aircraft_must_land_for_cargo));
            }
            else if(receiver instanceof StoredAirplane && !((StoredAirplane)receiver).HasCargo())
            {
                txtTransferCargoHint.setText(context.getString(R.string.aircraft_cant_receive_cargo));
            }
            else
            {
                txtTransferCargoHint.setText(context.getString(R.string.cargo_receiver_invalid));
            }
        }
        else if(receiver instanceof Structure && !((Structure)receiver).GetOnline())
        {
            txtTransferCargoHint.setText(context.getString(R.string.structure_offline));
        }
        else
        {
            txtTransferCargoHint.setVisibility(GONE);
        }

        if(entity instanceof Airbase)
        {
            DisplayStoredAircraft((Airbase)entity);
            lytSalePrice.setVisibility(GONE);
            lytTotalPrice.setVisibility(GONE);
        }
        else if(entity instanceof Ship && ((Ship)entity).HasAircraft())
        {
            DisplayStoredAircraft((Ship)entity);
            lytSalePrice.setVisibility(GONE);
            lytTotalPrice.setVisibility(GONE);
        }
        else
        {
            lytSalePrice.setVisibility(GONE);
            lytTotalPrice.setVisibility(GONE);
            lytContents.setVisibility(GONE);
            //txtTransferCargoHint.setVisibility(GONE);
            //txtTransferCargoHint.setText(context.getString(R.string.select_hauler));
        }

        Update();
    }

    public void DisplayStoredAircraft(Airbase airbase)
    {
        /**
         * Change txtSelectReceiver to "Select an aircraft from the airbase to continue."
         * If haulable is a missile, display aircraft that have missiles.
         * If haulable is a interceptor, display aircraft that have interceptors.
         * If haulable is a resource, tank, or infantry, display aircraft that have cargo bays.
         */

        lytContents.removeAllViews();
        StoredAircrafts.clear();

        List<StoredAirplane> AircraftToDisplay = new ArrayList<>();

        if(!airbase.GetAircraftSystem().GetStoredAirplanes().isEmpty())
        {
            switch(haulable.GetLootType())
            {
                case MISSILES:
                {
                    for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.GetPrepRemaining() == 0 && (aircraft.HasMissiles() || aircraft.HasCargo()))
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;

                case INTERCEPTORS:
                {
                    for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.GetPrepRemaining() == 0 && (aircraft.HasInterceptors() || aircraft.HasCargo()))
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;

                case RESOURCES:
                case STORED_CARGO_TRUCK:
                case STORED_INFANTRY:
                case STORED_TANK:
                case INFANTRY:
                case TANK:
                case CARGO_TRUCK:
                {
                    for(StoredAirplane aircraft : airbase.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.GetPrepRemaining() == 0 && aircraft.HasCargo())
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;
            }
        }

        if(!AircraftToDisplay.isEmpty())
        {
            txtTransferCargoHint.setText(context.getString(R.string.transfer_cargo_airbase_aircraft));
            lytContents.setVisibility(VISIBLE);

            for(StoredAirplane aircraft : AircraftToDisplay)
            {
                AircraftBaseView aircraftView = new AircraftBaseView(game, activity, aircraft, true);
                StoredAircrafts.add(aircraftView);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                aircraftView.setLayoutParams(layoutParams);

                aircraftView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        receiver = aircraft;
                        txtSelectReciever.setText(context.getString(R.string.cargo_receiver_selected, TextUtilities.GetEntityTypeAndName(aircraft, game)));
                        txtTransferCargoHint.setVisibility(GONE);
                        Update();
                    }
                });

                lytContents.addView(aircraftView);
            }
        }
    }

    public void DisplayStoredAircraft(Ship ship)
    {
        /**
         * Change txtSelectReceiver to "Select an aircraft from the airbase to continue."
         * If haulable is a missile, display aircraft that have missiles.
         * If haulable is a interceptor, display aircraft that have interceptors.
         * If haulable is a resource, tank, or infantry, display aircraft that have cargo bays.
         */

        lytContents.removeAllViews();
        StoredAircrafts.clear();

        List<StoredAirplane> AircraftToDisplay = new ArrayList<>();

        if(!ship.GetAircraftSystem().GetStoredAirplanes().isEmpty())
        {
            switch(haulable.GetLootType())
            {
                case MISSILES:
                {
                    for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.HasMissiles() || aircraft.HasCargo())
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;

                case INTERCEPTORS:
                {
                    for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.HasInterceptors() || aircraft.HasCargo())
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;

                case RESOURCES:
                case STORED_CARGO_TRUCK:
                case STORED_INFANTRY:
                case STORED_TANK:
                case INFANTRY:
                case TANK:
                case CARGO_TRUCK:
                {
                    for(StoredAirplane aircraft : ship.GetAircraftSystem().GetStoredAirplanes().values())
                    {
                        if(aircraft.HasCargo())
                            AircraftToDisplay.add(aircraft);
                    }
                }
                break;
            }
        }

        if(!AircraftToDisplay.isEmpty())
        {
            txtTransferCargoHint.setText(context.getString(R.string.transfer_cargo_airbase_aircraft));
            lytContents.setVisibility(VISIBLE);

            for(StoredAirplane aircraft : AircraftToDisplay)
            {
                AircraftBaseView aircraftView = new AircraftBaseView(game, activity, aircraft, true);
                StoredAircrafts.add(aircraftView);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                aircraftView.setLayoutParams(layoutParams);

                aircraftView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        receiver = aircraft;
                        txtSelectReciever.setText(context.getString(R.string.cargo_receiver_selected, TextUtilities.GetEntityTypeAndName(aircraft, game)));
                        txtTransferCargoHint.setVisibility(GONE);
                    }
                });

                lytContents.addView(aircraftView);
            }
        }
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(receiver == null)
                {
                    txtTransferCargoHint.setVisibility(VISIBLE);
                }
                else
                {
                    txtTransferCargoHint.setVisibility(GONE);

                    if(geoTo != null && geoFrom != null && geoTo.DistanceTo(geoFrom) > fltLoadDistance)
                    {
                        txtSelectReciever.setText(context.getString(R.string.out_of_range));
                        txtSelectReciever.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    }
                    else if(!game.LoadHaulableValid(haulable, receiver, hauler))
                    {
                        txtSelectReciever.setText(context.getString(R.string.target_invalid));
                        txtSelectReciever.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    }
                    else
                    {
                        txtSelectReciever.setText(context.getString(R.string.selected_loot, TextUtilities.GetOwnedEntityName(receiver, game)));
                        txtSelectReciever.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }

                    GeoCoord geoTo = game.GetPosition(receiver);

                    if(geoTo != null)
                    {
                        List<LatLng> points = new ArrayList<LatLng>();

                        GeoCoord geoFrom = game.GetPosition(deliverer);

                        if(geoFrom != null)
                        {
                            points.add(Utilities.GetLatLng(geoFrom));
                            points.add(Utilities.GetLatLng(geoTo));
                            targetTrajectory.setPoints(points);
                        }
                    }
                }
            }
        });
    }
}
