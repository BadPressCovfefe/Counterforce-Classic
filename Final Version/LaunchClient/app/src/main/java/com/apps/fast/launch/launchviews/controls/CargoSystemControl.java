package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.List;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.Armory;
import launch.game.entities.CargoTruck;
import launch.game.entities.Haulable;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Player;
import launch.game.entities.ScrapYard;
import launch.game.entities.Ship;
import launch.game.entities.Shipyard;
import launch.game.entities.Structure;
import launch.game.entities.Warehouse;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.systems.CargoSystem;

public class CargoSystemControl extends LaunchView
{
    private LinearLayout lytCapacity;
    private TextView txtCapacity;
    private LinearLayout lytContents;
    private LinearLayout btnLoadCargo;

    private CargoSystem system;
    private HaulerInterface host;
    private int lFittedToID;
    private boolean bDisplayLoad = true;
    private boolean bShowCapacity = true;
    private boolean bOwnedByPlayer;

    private List<HaulableSlotControl> Haulables;

    public CargoSystemControl(LaunchClientGame game, MainActivity activity, HaulerInterface host)
    {
        super(game, activity, true);
        this.host = host;
        this.lFittedToID = host.GetID();

        bOwnedByPlayer = ((LaunchEntity)host).GetOwnedBy(game.GetOurPlayerID());

        if(host instanceof Airplane)
        {
            system = host.GetCargoSystem();
            bDisplayLoad = false; //Flying aircraft cannot load cargo, of course.
        }
        else if(host instanceof StoredAirplane)
        {
            system = ((StoredAirplane)host).GetCargoSystem();
        }
        else if(host instanceof CargoTruck)
        {
            system = ((CargoTruck)host).GetCargoSystem();
        }
        else if(host instanceof Ship)
        {
            system = ((Ship)host).GetCargoSystem();
        }
        else if(host instanceof Warehouse)
        {
            system = ((Warehouse)host).GetCargoSystem();
        }
        else if(host instanceof Armory)
        {
            system = ((Armory)host).GetCargoSystem();
        }
        else if(host instanceof Shipyard)
        {
            system = ((Shipyard)host).GetCargoSystem();
        }
        else if(host instanceof Player)
        {
            system = ((Player)host).GetCargoSystem();
            bDisplayLoad = false;
            bShowCapacity = false;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_cargo_system, this);

        lytCapacity = findViewById(R.id.lytCapacity);
        txtCapacity = findViewById(R.id.txtCapacity);
        lytContents = findViewById(R.id.lytContents);
        btnLoadCargo = findViewById(R.id.btnLoadCargo);

        GenerateSlotTable(system);

        if(bShowCapacity)
        {
            long lowCapacityThreshold = system.GetCapacity()/3;

            long oUsedCapacity = system.GetUsedCapacity();

            if(oUsedCapacity >= 1000000)
            {
                oUsedCapacity = oUsedCapacity/1000;
            }

            txtCapacity.setText(context.getString(R.string.cargo_capacity, TextUtilities.GetWeightStringFromKG(oUsedCapacity), TextUtilities.GetWeightStringFromKG(system.GetCapacity())));

            if(system.GetUsedCapacity() == system.GetCapacity())
            {
                txtCapacity.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(system.GetUsedCapacity() < lowCapacityThreshold)
            {
                txtCapacity.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else
            {
                txtCapacity.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }

            if(game.EntityIsFriendly((LaunchEntity)host, game.GetOurPlayer()))
            {
                lytCapacity.setVisibility(VISIBLE);
            }
            else
            {
                lytCapacity.setVisibility(GONE);
            }
        }

        if(bDisplayLoad && bOwnedByPlayer)
        {
            btnLoadCargo.setVisibility(VISIBLE);

            btnLoadCargo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(host.GetCargoSystem().Full())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.cant_load_cargo_full));
                    }
                    else if(host instanceof Structure && !((Structure)host).GetOnline())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                    }
                    else
                    {
                        activity.LoadLootMode((LaunchEntity)host);
                    }
                }
            });
        }
        else
        {
            btnLoadCargo.setVisibility(GONE);
        }

        if(game.EntityIsFriendly((LaunchEntity)host, game.GetOurPlayer()))
        {
            lytContents.setVisibility(VISIBLE);
        }
        else
        {
            lytContents.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        switch(((LaunchEntity)host).GetEntityType())
        {
            case CARGO_TRUCK: host = game.GetCargoTruck(lFittedToID); break;
            case SHIP: host = game.GetShip(lFittedToID); break;
            case STORED_AIRPLANE: host = game.GetStoredAirplane(lFittedToID); break;
            case WAREHOUSE: host = game.GetWarehouse(lFittedToID); break;
            case AIRPLANE: host = game.GetAirplane(lFittedToID); break;
            case STORED_CARGO_TRUCK: host = game.GetStoredCargoTruck(lFittedToID); break;
            case SHIPYARD: host = game.GetShipyard(lFittedToID); break;
            case PLAYER: host = game.GetOurPlayer(); break;
        }

        system = GetCargoSystem();

        if(system != null)
        {
            if(host != null && game.EntityIsFriendly((LaunchEntity)host, game.GetOurPlayer()))
            {
                lytContents.setVisibility(VISIBLE);

                if(bShowCapacity)
                {
                    lytCapacity.setVisibility(VISIBLE);

                    long oUsedCapacity = system.GetUsedCapacity();

                    if(oUsedCapacity >= 1000000)
                    {
                        oUsedCapacity = oUsedCapacity/1000;
                    }

                    txtCapacity.setText(context.getString(R.string.cargo_capacity, TextUtilities.GetWeightStringFromKG(oUsedCapacity), TextUtilities.GetWeightStringFromKG(system.GetCapacity())));
                }
                else
                {
                    lytCapacity.setVisibility(GONE);
                }

                for(HaulableSlotControl haulable : Haulables)
                {
                    haulable.Update();
                }
            }
            else
            {
                lytCapacity.setVisibility(GONE);
                btnLoadCargo.setVisibility(GONE);
                lytContents.setVisibility(GONE);
            }
        }
    }

    private void GenerateSlotTable(CargoSystem system)
    {
        lytContents.removeAllViews();
        Haulables = new ArrayList<>();

        if(system != null)
        {
            for(Haulable haulable : system.GetContents())
            {
                if(haulable.GetQuantity() > 0)
                {
                    HaulableSlotControl slotControl = new HaulableSlotControl(game, activity, haulable);
                    Haulables.add(slotControl);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                    layoutParams.weight = 1.0f;
                    slotControl.setLayoutParams(layoutParams);

                    if(bOwnedByPlayer)
                    {
                        slotControl.setOnClickListener(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                if(!(host instanceof Structure) || ((Structure)host).GetOnline())
                                {
                                    activity.TransferCargoMode((LaunchEntity)host, haulable, true);
                                }
                                else
                                {
                                    activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                                }
                            }
                        });

                        if((host instanceof ScrapYard || host instanceof Warehouse || host instanceof Shipyard || host instanceof Airplane || host instanceof StoredAirplane) && haulable instanceof LaunchEntity)
                        {
                            slotControl.setOnLongClickListener(new OnLongClickListener()
                            {
                                @Override
                                public boolean onLongClick(View view)
                                {
                                    if(!(host instanceof Structure) || ((Structure)host).GetOnline())
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
                                                game.DropLandUnit(((LaunchEntity)haulable).GetPointer());
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

                                    return false;
                                }
                            });
                        }
                    }

                    lytContents.addView(slotControl);
                }
            }
        }
        else
        {
            Finish(true);
        }
    }

    private CargoSystem GetCargoSystem()
    {
        if(host != null)
        {
            switch(((LaunchEntity)host).GetEntityType())
            {
                case CARGO_TRUCK: return game.GetCargoTruck(lFittedToID).GetCargoSystem();
                case SHIP: return game.GetShip(lFittedToID).GetCargoSystem();
                case STORED_AIRPLANE: return game.GetStoredAirplane(lFittedToID).GetCargoSystem();
                case WAREHOUSE: return game.GetWarehouse(lFittedToID).GetCargoSystem();
                case AIRPLANE: return game.GetAirplane(lFittedToID).GetCargoSystem();
                case STORED_CARGO_TRUCK: return game.GetStoredCargoTruck(lFittedToID).GetCargoSystem();
                case SHIPYARD: return game.GetShipyard(lFittedToID).GetCargoSystem();
                case PLAYER: return game.GetOurPlayer().GetCargoSystem();
            }
        }

        return null;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals((LaunchEntity)host))
        {
            CargoSystem system = GetCargoSystem();

            if(system != null)
                GenerateSlotTable(system);
        }
    }
}
