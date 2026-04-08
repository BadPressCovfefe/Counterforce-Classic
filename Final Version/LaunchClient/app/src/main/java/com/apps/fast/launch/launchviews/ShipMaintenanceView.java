package com.apps.fast.launch.launchviews;

import android.view.View;
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
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.Movable;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Ship;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;
import launch.game.entities.Submarine;

/**
 * Created by tobster on 09/11/15.
 */
public class ShipMaintenanceView extends LaunchView implements LaunchUICommon.ShipInfoProvider
{
    private Ship shipShadow = null;
    private Collection ShipList = null;

    private TextView txtEmptySlots;
    private TextView txtFuelLevel;
    private LinearLayout btnMove;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private LinearLayout btnCeaseFire;
    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;
    private LinearLayout lytModeControls;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param ship The structure.
     */
    public ShipMaintenanceView(LaunchClientGame game, MainActivity activity, Ship ship)
    {
        super(game, activity, true);
        this.shipShadow = ship;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param ships List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public ShipMaintenanceView(LaunchClientGame game, MainActivity activity, Collection ships)
    {
        super(game, activity, true);
        this.ShipList = ships;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_ship_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        ImageView imgType = findViewById(R.id.imgShipType);
        TextView txtType = findViewById(R.id.txtTitle);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        lytModeControls = findViewById(R.id.lytModeControls);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        Ship iconControlShip = shipShadow == null ? (Ship)ShipList.iterator().next() : shipShadow;
        txtType.setText(TextUtilities.GetEntityTypeAndName(iconControlShip, game));

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        //If there is just one submarine in the list, just treat it like we selected a single submarine.
        if(shipShadow == null && ShipList.size() == 1)
        {
            shipShadow = (Ship)ShipList.toArray()[0];
            ShipList.clear();
        }

        if(shipShadow != null)
        {
            if(shipShadow.GetNuclear())
                txtFuelLevel.setText(context.getString(R.string.infinite));
            else
                TextUtilities.AssignFuelPercentageString(txtFuelLevel, shipShadow);

            txtFuelLevel.setVisibility(VISIBLE);
            txtCount.setVisibility(GONE);

            try
            {
                imgType.setImageBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, shipShadow));
            }
            catch(Exception ex) { /* Don't care.*/ }

            if(shipShadow.HasInterceptors())
            {
                lytModeControls.setVisibility(VISIBLE);

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (!game.GetSAMSite(shipShadow.GetID()).GetAuto())
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
                        if (!game.GetSAMSite(shipShadow.GetID()).GetSemiAuto())
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
                        if (!game.GetSAMSite(shipShadow.GetID()).GetManual())
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
        }

        if(ShipList != null)
        {
            //Group of structures. Bin the name and state labels and set the count.
            txtCount.setText(Integer.toString(ShipList.size()));
            txtFuelLevel.setVisibility(GONE);

            boolean bShowModes = false;

            for(Object object : ShipList)
            {
                Ship ship = (Ship)object;

                if(ship.HasInterceptors())
                {
                    bShowModes = true;
                    break;
                }
            }

            if(bShowModes)
            {
                lytModeControls.setVisibility(VISIBLE);

                final List<EntityPointer> Pointers = new ArrayList<>();

                for(Object object : ShipList)
                {
                    Ship ship = (Ship)object;
                    Pointers.add(ship.GetPointer());
                }

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.SetSAMSiteModes(Pointers, SAMSite.MODE_AUTO);
                    }
                });

                btnSemi.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.SetSAMSiteModes(Pointers, SAMSite.MODE_SEMI_AUTO);
                    }
                });

                btnManual.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.SetSAMSiteModes(Pointers, SAMSite.MODE_MANUAL);
                    }
                });
            }
        }

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(shipShadow != null)
                    activity.MoveOrderMode((shipShadow).GetPointer(), null);
                else if(ShipList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objShip : ShipList.toArray())
                    {
                        MovablePointers.add(((Ship)objShip).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;

        if(shipShadow != null)
        {
            if(shipShadow.GetMoveOrders() != Movable.MoveOrders.WAIT && shipShadow.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                bShowCeaseFire = true;
        }
        else if(ShipList != null)
        {
            for(Object obj : ShipList)
            {
                if(obj instanceof Movable)
                {
                    if(((Movable)obj).GetMoveOrders() != Movable.MoveOrders.WAIT && ((Movable)obj).GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;
                }
            }
        }

        if(bShowCeaseFire)
        {
            btnCeaseFire.setVisibility(VISIBLE);

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

                            if(shipShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(shipShadow.GetPointer()));
                            }
                            else if(ShipList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : ShipList)
                                {
                                    LaunchEntity entity = (LaunchEntity)objEntity;
                                    SitePointers.add(entity.GetPointer());
                                }

                                game.CeaseFire(SitePointers);
                            }
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

        Update();
    }

    @Override
    public void Update()
    {
        boolean bShowCeaseFire = false;

        if(shipShadow != null)
        {
            Ship ship = GetCurrentShip();

            if (ship != null)
            {
                if(shipShadow.GetNuclear())
                    txtFuelLevel.setText(context.getString(R.string.infinite));
                else
                    TextUtilities.AssignFuelPercentageString(txtFuelLevel, shipShadow);

                int lOccupiedSlots = 0;
                int lSlotCount = 0;

                if(ship.HasMissiles())
                {
                    lOccupiedSlots += ship.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetMissileSystem().GetSlotCount();
                }

                if(ship.HasArtillery())
                {
                    lOccupiedSlots += ship.GetArtillerySystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetArtillerySystem().GetSlotCount();
                }

                if(ship.HasInterceptors())
                {
                    lOccupiedSlots += ship.GetInterceptorSystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetInterceptorSystem().GetSlotCount();

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

                if(lSlotCount == 0)
                {
                    txtEmptySlots.setVisibility(GONE);
                }

                txtEmptySlots.setText(context.getString(R.string.empty_slot_count, lOccupiedSlots, lSlotCount));

                //int OfflineUpkeepCost = (int) 0.1 * game.GetConfig().GetMaintenanceCost(structure); //In keeping with 10% offline cost from LaunchGame. -Corbin

                if (lOccupiedSlots == lSlotCount)
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                else if (lOccupiedSlots == 0)
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                else
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));

                if(ship.GetMoveOrders() != Movable.MoveOrders.WAIT && ship.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else
        {
            txtFuelLevel.setVisibility(GONE);
            List<Ship> CurrentShips = GetCurrentShips();
            Ship controlShip = CurrentShips.get(0);

            int lOccupiedSlots = 0;
            int lSlotCount = 0;

            boolean bAutos = false;
            boolean bSemis = false;
            boolean bManuals = false;
            boolean bbNoneHaveInterceptors = true;

            for(Ship ship : CurrentShips)
            {
                if(ship.HasMissiles())
                {
                    lOccupiedSlots += ship.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetMissileSystem().GetSlotCount();
                }

                if(ship.HasArtillery())
                {
                    lOccupiedSlots += ship.GetArtillerySystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetArtillerySystem().GetSlotCount();
                }

                if(ship.HasInterceptors())
                {
                    lOccupiedSlots += ship.GetInterceptorSystem().GetOccupiedSlotCount();
                    lSlotCount += ship.GetInterceptorSystem().GetSlotCount();
                    bbNoneHaveInterceptors = false;
                }

                if(ship.GetAuto())
                    bAutos = true;

                if(ship.GetSemiAuto())
                    bSemis = true;

                if(ship.GetManual())
                    bManuals = true;
            }

            if(bbNoneHaveInterceptors)
            {
                lytModeControls.setVisibility(GONE);
            }
            else
            {
                lytModeControls.setVisibility(VISIBLE);

                if(bAutos)
                {
                    flasherAuto.TurnGreen(context);
                }
                else
                {
                    flasherAuto.TurnOff(context);
                }

                if(bSemis)
                {
                    flasherSemi.TurnGreen(context);
                }
                else
                {
                    flasherSemi.TurnOff(context);
                }

                if(bManuals)
                {
                    flasherManual.TurnGreen(context);
                }
                else
                {
                    flasherManual.TurnOff(context);
                }
            }

            if(lSlotCount == 0)
            {
                txtEmptySlots.setVisibility(GONE);
            }

            txtEmptySlots.setText(context.getString(R.string.empty_slot_count, lOccupiedSlots, lSlotCount));

            if (lOccupiedSlots == lSlotCount)
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if (lOccupiedSlots == 0)
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            else
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));

            for(Object obj : ShipList)
            {
                if(obj instanceof Movable)
                {
                    if(((Movable)obj).GetMoveOrders() != Movable.MoveOrders.WAIT && ((Movable)obj).GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;
                }
            }
        }

        if(bShowCeaseFire)
        {
            btnCeaseFire.setVisibility(VISIBLE);
        }
        else
        {
            btnCeaseFire.setVisibility(GONE);
        }
    }

    @Override
    public boolean IsSingleShip()
    {
        return shipShadow != null;
    }

    @Override
    public Ship GetCurrentShip()
    {
        return game.GetShip(shipShadow.GetID());
    }

    @Override
    public List<Ship> GetCurrentShips()
    {
        Ship controlShip = (Ship)ShipList.iterator().next();
        List<Ship> CurrentShips = new ArrayList<>();

        for(Object object : ShipList)
        {
            Ship ship = (Ship)object;
            CurrentShips.add(game.GetShip(ship.GetID()));
        }

        return CurrentShips;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(shipShadow != null)
        {
            if(entity.ApparentlyEquals(shipShadow))
                bUpdate = true;
        }

        if(ShipList != null)
        {
            for(Object object : ShipList)
            {
                Ship ship = (Ship)object;

                if(entity.ApparentlyEquals(ship))
                {
                    bUpdate = true;
                    break;
                }
            }
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
