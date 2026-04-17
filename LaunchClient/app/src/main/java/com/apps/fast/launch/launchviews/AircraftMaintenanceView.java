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

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Infantry;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource;

public class AircraftMaintenanceView extends LaunchView implements LaunchUICommon.AircraftInfoProvider
{
    private AirplaneInterface aircraftShadow = null;
    private Collection AircraftList = null;
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
    private LinearLayout btnReturn;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param aircraft The structure.
     */
    public AircraftMaintenanceView(LaunchClientGame game, MainActivity activity, AirplaneInterface aircraft)
    {
        super(game, activity, true);
        this.aircraftShadow = aircraft;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param aircrafts List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public AircraftMaintenanceView(LaunchClientGame game, MainActivity activity, Collection aircrafts)
    {
        super(game, activity, true);
        this.AircraftList = aircrafts;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_aircraft_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        ImageView imgType = findViewById(R.id.imgAircraftType);
        TextView txtType = findViewById(R.id.txtTitle);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        btnReturn = findViewById(R.id.btnReturn);
        lytModeControls = findViewById(R.id.lytModeControls);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        if(aircraftShadow == null && AircraftList.size() == 1)
        {
            aircraftShadow = (AirplaneInterface)AircraftList.toArray()[0];
            AircraftList.clear();
        }

        AirplaneInterface iconControlAircraft = aircraftShadow == null ? (AirplaneInterface)AircraftList.iterator().next() : aircraftShadow;

        txtType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)iconControlAircraft, game));

        if(aircraftShadow != null)
        {
            TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraftShadow);
            txtFuelLevel.setVisibility(VISIBLE);

            try
            {
                switch(aircraftShadow.GetAircraftType())
                {
                    case BOMBER:
                    {
                        imgType.setImageResource(R.drawable.build_bomber);
                    }
                    break;

                    case FIGHTER:
                    {
                        imgType.setImageResource(R.drawable.build_fighter);
                    }
                    break;

                    case ATTACK_AIRCRAFT:
                    {
                        imgType.setImageResource(R.drawable.build_ground_attack);
                    }
                    break;

                    case REFUELER:
                    {
                        imgType.setImageResource(R.drawable.build_refueler);
                    }
                    break;

                    case MULTI_ROLE:
                    {
                        imgType.setImageResource(R.drawable.build_multi_role);
                    }
                    break;

                    case SSB:
                    {
                        imgType.setImageResource(R.drawable.build_ssb);
                    }
                    break;
                }
            }
            catch(Exception ex) { /* Don't care.*/ }

            txtCount.setVisibility(GONE);

            if(aircraftShadow.HasInterceptors())
            {
                lytModeControls.setVisibility(VISIBLE);

                flasherAuto = new ButtonFlasher(btnAuto);
                flasherSemi = new ButtonFlasher(btnSemi);
                flasherManual = new ButtonFlasher(btnManual);

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!aircraftShadow.GetAuto())
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

                                    game.SetSAMSiteMode(((LaunchEntity)aircraftShadow).GetPointer(), SAMSite.MODE_AUTO);
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
                        if(!aircraftShadow.GetSemiAuto())
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

                                    game.SetSAMSiteMode(((LaunchEntity)aircraftShadow).GetPointer(), SAMSite.MODE_SEMI_AUTO);
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
                        if(!aircraftShadow.GetManual())
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

                                    game.SetSAMSiteMode(((LaunchEntity)aircraftShadow).GetPointer(), SAMSite.MODE_MANUAL);
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
        else if(AircraftList != null)
        {
            //Group of aircraft. Bin the name and state labels and set the count.
            txtCount.setText(Integer.toString(AircraftList.size()));
            txtFuelLevel.setVisibility(GONE);

            imgType.setImageBitmap(EntityIconBitmaps.GetAircraftBitmap(context, game, (AirplaneInterface)AircraftList.iterator().next()));

            final List<EntityPointer> Pointers = new ArrayList<>();
            boolean bNoneHaveInterceptors = true;

            for(Object object : AircraftList)
            {
                AirplaneInterface aircraft = (AirplaneInterface)object;
                Pointers.add(((LaunchEntity)aircraft).GetPointer());

                if(aircraft.HasInterceptors())
                    bNoneHaveInterceptors = false;
            }

            if(bNoneHaveInterceptors)
            {
                lytModeControls.setVisibility(GONE);
            }
            else
            {
                lytModeControls.setVisibility(VISIBLE);

                flasherAuto = new ButtonFlasher(btnAuto);
                flasherSemi = new ButtonFlasher(btnSemi);
                flasherManual = new ButtonFlasher(btnManual);

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
                if(aircraftShadow != null)
                    activity.MoveOrderMode(((LaunchEntity)aircraftShadow).GetPointer(), null);
                else if(AircraftList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objAircraft : AircraftList.toArray())
                    {
                        AirplaneInterface aircraft = (AirplaneInterface)objAircraft;
                        MovablePointers.add(((LaunchEntity)aircraft).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;

        if(aircraftShadow != null)
        {
            if(aircraftShadow instanceof Movable)
            {
                if(((Movable)aircraftShadow).GetMoveOrders() == Movable.MoveOrders.ATTACK)
                    bShowCeaseFire = true;
            }
        }
        else if(AircraftList != null)
        {
            for(Object obj : AircraftList)
            {
                if(obj instanceof Movable)
                {
                    if(((Movable)obj).GetMoveOrders() == Movable.MoveOrders.ATTACK)
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

                            if(aircraftShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(((LaunchEntity)aircraftShadow).GetPointer()));
                            }
                            else if(AircraftList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : AircraftList.toArray())
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

        btnReturn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(aircraftShadow != null)
                    activity.ReturnAircraft(Collections.singletonList(((LaunchEntity) aircraftShadow).GetPointer()));
                else if(AircraftList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objAircraft : AircraftList.toArray())
                    {
                        AirplaneInterface aircraft = (AirplaneInterface)objAircraft;
                        MovablePointers.add(((LaunchEntity)aircraft).GetPointer());
                    }

                    activity.ReturnAircraft(MovablePointers);
                }
            }
        });

        Update();
    }

    @Override
    public void Update()
    {
        boolean bShowCeaseFire = false;

        if(aircraftShadow != null)
        {
            AirplaneInterface aircraft = GetCurrentAircraft();

            if(aircraft != null)
            {
                TextUtilities.AssignFuelPercentageString(txtFuelLevel, aircraftShadow);
                txtFuelLevel.setVisibility(VISIBLE);

                int lOccupiedSlots = 0;
                int lSlotCount = 0;

                if(aircraft.HasMissiles())
                {
                    lOccupiedSlots += aircraft.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += aircraft.GetMissileSystem().GetSlotCount();
                }

                if(aircraft.HasInterceptors())
                {
                    lOccupiedSlots += aircraft.GetInterceptorSystem().GetOccupiedSlotCount();
                    lSlotCount += aircraft.GetInterceptorSystem().GetSlotCount();

                    if(aircraft.HasInterceptors())
                    {
                        if(aircraft.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if(aircraft.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(aircraft.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }
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

                if(aircraft instanceof Movable)
                {
                    if(((Movable)aircraft).GetMoveOrders() == Movable.MoveOrders.ATTACK)
                        bShowCeaseFire = true;
                }
            }
        }
        else
        {
            List<AirplaneInterface> CurrentAircrafts = GetCurrentAircrafts();

            if(CurrentAircrafts != null)
            {
                AirplaneInterface controlAircraft = CurrentAircrafts.get(0);
                txtFuelLevel.setVisibility(GONE);

                int lOccupiedSlots = 0;
                int lSlotCount = 0;

                for(AirplaneInterface aircraft : CurrentAircrafts)
                {
                    if(aircraft.HasMissiles())
                    {
                        lOccupiedSlots += aircraft.GetMissileSystem().GetOccupiedSlotCount();
                        lSlotCount += aircraft.GetMissileSystem().GetSlotCount();
                    }

                    if(aircraft.HasInterceptors())
                    {
                        lOccupiedSlots += aircraft.GetInterceptorSystem().GetOccupiedSlotCount();
                        lSlotCount += aircraft.GetInterceptorSystem().GetSlotCount();
                    }
                }

                if(lSlotCount == 0)
                {
                    txtEmptySlots.setVisibility(GONE);
                }

                txtEmptySlots.setText(context.getString(R.string.empty_slot_count, lOccupiedSlots, lSlotCount));

                if(lOccupiedSlots == lSlotCount)
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                else if (lOccupiedSlots == 0)
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                else
                    txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));

                boolean bAutos = false;
                boolean bSemis = false;
                boolean bManuals = false;
                boolean bbNoneHaveInterceptors = true;

                for(Object obj : AircraftList)
                {
                    AirplaneInterface aircraft = (AirplaneInterface)obj;

                    if(obj instanceof Movable)
                    {
                        if(((Movable)obj).GetMoveOrders() == Movable.MoveOrders.ATTACK)
                            bShowCeaseFire = true;
                    }

                    if(aircraft.HasInterceptors())
                        bbNoneHaveInterceptors = false;

                    if(aircraft.GetAuto())
                        bAutos = true;

                    if(aircraft.GetSemiAuto())
                        bSemis = true;

                    if(aircraft.GetManual())
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
    public boolean IsSingleAircraft()
    {
        return aircraftShadow != null;
    }

    @Override
    public AirplaneInterface GetCurrentAircraft()
    {
        return (AirplaneInterface)(((LaunchEntity)aircraftShadow).GetPointer().GetEntity(game));
    }

    @Override
    public List<AirplaneInterface> GetCurrentAircrafts()
    {
        AirplaneInterface controlAircraft = (AirplaneInterface)AircraftList.iterator().next();
        List<AirplaneInterface> CurrentAircrafts = new ArrayList<>();

        for(Object object : AircraftList)
        {
            AirplaneInterface aircraft = (AirplaneInterface)object;
            CurrentAircrafts.add((AirplaneInterface)(((LaunchEntity)aircraft).GetPointer().GetEntity(game)));
        }

        return CurrentAircrafts;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(aircraftShadow != null)
        {
            if(entity.ApparentlyEquals((LaunchEntity)aircraftShadow))
                bUpdate = true;
        }

        if(AircraftList != null)
        {
            for(Object object : AircraftList)
            {
                AirplaneInterface aircraft = (AirplaneInterface)object;

                if(entity.ApparentlyEquals((LaunchEntity)aircraft))
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
