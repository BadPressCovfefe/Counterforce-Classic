package com.apps.fast.launch.launchviews;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
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
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.Bank;
import launch.game.entities.Armory;
import launch.game.entities.CommandPost;
import launch.game.entities.MissileFactory;
import launch.game.entities.Distributor;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Processor;
import launch.game.entities.ScrapYard;
import launch.game.entities.Warehouse;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.RadarStation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class StructureMaintenanceView extends LaunchView implements LaunchUICommon.StructureOnOffInfoProvider
{
    private Structure structureShadow = null;
    private Collection StructureList = null;

    private TextView txtState;
    private TextView txtHealth;
    private TextView txtEmptySlots;
    private TextView txtCost;
    //private TextView txtTime;
    private ImageView imgPower;
    private LinearLayout btnCeaseFire;
    private LinearLayout btnAttack;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param structure The structure.
     */
    public StructureMaintenanceView(LaunchClientGame game, MainActivity activity, Structure structure)
    {
        super(game, activity, true);
        this.structureShadow = structure;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param structures List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public StructureMaintenanceView(LaunchClientGame game, MainActivity activity, Collection structures)
    {
        super(game, activity, true);
        this.StructureList = structures;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_structure_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        ImageView imgType = findViewById(R.id.imgType);
        TextView txtType = findViewById(R.id.txtType);
        TextView txtName = findViewById(R.id.txtName);
        LinearLayout btnPower = findViewById(R.id.btnPower);
        LinearLayout lytModeControls = findViewById(R.id.lytModeControls);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        txtState = findViewById(R.id.txtState);
        txtHealth = findViewById(R.id.txtHealth);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        txtCost = findViewById(R.id.txtCost);
        //txtTime = findViewById(R.id.txtTime);
        imgPower = findViewById(R.id.imgPower);
        btnAttack = findViewById(R.id.btnAttack);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        LaunchUICommon.SetPowerButtonOnClickListener(activity, btnPower, this, game);

        Structure iconControlStructure = structureShadow == null ? (Structure)StructureList.iterator().next() : structureShadow;
        txtType.setText(TextUtilities.GetEntityTypeAndName(iconControlStructure, game));

        if(structureShadow != null)
        {
            //Individual structure. Show name if it has one, and bin the count label.
            if (structureShadow.GetName().length() > 0)
                txtName.setText(structureShadow.GetName());
            else
                txtName.setVisibility(GONE);

            txtCount.setVisibility(GONE);

            if(structureShadow instanceof ArtilleryGun)
            {
                if(((ArtilleryGun)structureShadow).HasFireOrder())
                {
                    btnCeaseFire.setVisibility(VISIBLE);
                }
                else
                {
                    btnCeaseFire.setVisibility(GONE);
                }
            }
        }

        if(StructureList != null)
        {
            //Group of structures. Bin the name and state labels and set the count.
            txtName.setVisibility(GONE);
            txtState.setVisibility(GONE);
            //txtTime.setVisibility(GONE);
            txtCount.setText(Integer.toString(StructureList.size()));

            boolean bShowCeaseFire = false;

            for(Object objStructure : StructureList.toArray())
            {
                Structure structure = (Structure)objStructure;

                if(structure instanceof ArtilleryGun)
                {
                    if(((ArtilleryGun)structure).HasFireOrder())
                    {
                        bShowCeaseFire = true;
                        break;
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

                        if(structureShadow != null)
                        {
                            game.CeaseFire(Collections.singletonList(structureShadow.GetPointer()));
                        }
                        else if(StructureList != null)
                        {
                            List<EntityPointer> SitePointers = new ArrayList<>();

                            for(Object objStructure : StructureList.toArray())
                            {
                                Structure structure = (Structure)objStructure;
                                SitePointers.add(structure.GetPointer());
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

        if(StructureList == null && iconControlStructure instanceof ArtilleryGun)
        {
            btnAttack.setVisibility(VISIBLE);

            btnAttack.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(structureShadow != null)
                    {
                        activity.SetTargetMode(((LaunchEntity)structureShadow).GetPointer(), null);
                    }
                    else if(StructureList != null)
                    {
                        List<EntityPointer> SitePointers = new ArrayList<>();

                        for(Object objStructure : StructureList.toArray())
                        {
                            Structure structure = (Structure)objStructure;
                            SitePointers.add(structure.GetPointer());
                        }

                        activity.SetTargetMode(null, SitePointers);
                    }
                }
            });
        }
        else
        {
            btnAttack.setVisibility(GONE);
        }

        if(iconControlStructure instanceof MissileSite)
        {
            if(((MissileSite)iconControlStructure).CanTakeICBM())
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_icbm_silo), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));

                lytModeControls.setVisibility(VISIBLE);

                flasherAuto = new ButtonFlasher(btnAuto);
                flasherSemi = new ButtonFlasher(btnSemi);
                flasherManual = new ButtonFlasher(btnManual);

                if(structureShadow != null)
                {
                    btnAuto.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if (!game.GetMissileSite(structureShadow.GetID()).GetAuto())
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

                                        game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_AUTO);
                                    }
                                });
                                launchDialog.SetOnClickNo(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view) {
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
                            if (!game.GetMissileSite(structureShadow.GetID()).GetSemiAuto())
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

                                        game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_SEMI_AUTO);
                                    }
                                });
                                launchDialog.SetOnClickNo(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view) {
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
                        public void onClick(View view) {
                            if (!game.GetMissileSite(structureShadow.GetID()).GetManual())
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

                                        game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_MANUAL);
                                    }
                                });
                                launchDialog.SetOnClickNo(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view) {
                                        launchDialog.dismiss();
                                    }
                                });
                                launchDialog.show(activity.getFragmentManager(), "");
                            }
                        }
                    });
                }
                else if(StructureList != null)
                {
                    final List<Integer> IDs = new ArrayList<>();

                    for(Object object : StructureList)
                    {
                        Structure structure = (Structure)object;
                        IDs.add(structure.GetID());
                    }

                    btnAuto.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.SetICBMSiloModes(IDs, MissileSite.MODE_AUTO);
                        }
                    });

                    btnSemi.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.SetICBMSiloModes(IDs, MissileSite.MODE_SEMI_AUTO);
                        }
                    });

                    btnManual.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.SetICBMSiloModes(IDs, MissileSite.MODE_MANUAL);
                        }
                    });
                }
            }
            else
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
            }
        }
        else if(iconControlStructure instanceof SAMSite)
        {
            lytModeControls.setVisibility(VISIBLE);

            SAMSite samSite = ((SAMSite)iconControlStructure);

            if(samSite.GetIsABMSilo())
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
            }
            else
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
            }

            flasherAuto = new ButtonFlasher(btnAuto);
            flasherSemi = new ButtonFlasher(btnSemi);
            flasherManual = new ButtonFlasher(btnManual);

            if(structureShadow != null)
            {
                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (!game.GetSAMSite(structureShadow.GetID()).GetAuto())
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

                                    game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_AUTO);
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
                        if (!game.GetSAMSite(structureShadow.GetID()).GetSemiAuto())
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

                                    game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_SEMI_AUTO);
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
                        if (!game.GetSAMSite(structureShadow.GetID()).GetManual())
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

                                    game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_MANUAL);
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
            else if(StructureList != null)
            {
                final List<EntityPointer> Pointers = new ArrayList<>();

                for(Object object : StructureList)
                {
                    Structure structure = (Structure)object;
                    Pointers.add(structure.GetPointer());
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
        else if(iconControlStructure instanceof SentryGun)
        {
            SentryGun gun = (SentryGun)iconControlStructure;

            if(gun.GetIsWatchTower())
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_artillery_gun), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
            }
            else
            {
                imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
            }
        }
        else if(iconControlStructure instanceof CommandPost)
        {
            imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_command_post), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
        }
        else if(iconControlStructure instanceof Airbase)
        {
            imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_airbase), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
        }
        else if(iconControlStructure instanceof Armory)
        {
            imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_armory), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
        }
        else if(iconControlStructure instanceof Warehouse)
        {
            imgType.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bank), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), iconControlStructure).ordinal()]));
        }

        Update();
    }

    @Override
    public void Update()
    {
        if(structureShadow != null)
        {
            Structure structure = GetCurrentStructure();

            if (structure != null)
            {
                TextUtilities.SetStructureState(txtState, structure);
                TextUtilities.AssignHealthStringAndAppearance(txtHealth, structure);

                int lOccupiedSlots = 0;
                int lSlotCount = 0;

                if (structure instanceof MissileSite)
                {
                    lOccupiedSlots = ((MissileSite) structure).GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount = ((MissileSite) structure).GetMissileSystem().GetSlotCount();

                    MissileSite missileSite = ((MissileSite)structure);

                    if(missileSite.CanTakeICBM())
                    {
                        if(missileSite.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if(missileSite.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(missileSite.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }
                    }
                }
                else if (structure instanceof SAMSite)
                {
                    lOccupiedSlots = ((SAMSite) structure).GetInterceptorSystem().GetOccupiedSlotCount();
                    lSlotCount = ((SAMSite) structure).GetInterceptorSystem().GetSlotCount();

                    SAMSite samSite = (SAMSite)structure;

                    if(samSite.GetAuto())
                    {
                        flasherAuto.TurnGreen(context);
                    }
                    else
                    {
                        flasherAuto.TurnOff(context);
                    }

                    if(samSite.GetSemiAuto())
                    {
                        flasherSemi.TurnGreen(context);
                    }
                    else
                    {
                        flasherSemi.TurnOff(context);
                    }

                    if(samSite.GetManual())
                    {
                        flasherManual.TurnGreen(context);
                    }
                    else
                    {
                        flasherManual.TurnOff(context);
                    }
                }
                else if(structure instanceof ArtilleryGun)
                {
                    if(structureShadow instanceof ArtilleryGun)
                    {
                        ArtilleryGun artillery = (ArtilleryGun)structureShadow;

                        if(artillery.HasFireOrder())
                        {
                            btnCeaseFire.setVisibility(VISIBLE);
                        }
                        else
                        {
                            btnCeaseFire.setVisibility(GONE);
                        }

                        if(artillery.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if(artillery.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(artillery.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }
                    }
                }
                else if(structure instanceof Airbase)
                {
                    Airbase airbase = ((Airbase)structure);

                    lOccupiedSlots = airbase.GetAircraftSystem().GetOccupiedSlotCount();
                    lSlotCount = airbase.GetAircraftSystem().GetSlotCount();
                }
                else
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

                if ((structure.GetOffline()) || (structure.GetSelling()))
                {
                    txtCost.setText(TextUtilities.GetCurrencyString(Defs.OFFLINE_MAINTENANCE_COST)); //Used to be 0. Changed to 10% of online cost as detailed in LaunchGame. -Corbin
                    //txtTime.setVisibility(GONE);
                }
                else
                {
                    txtCost.setText(TextUtilities.GetCurrencyString(game.GetConfig().GetMaintenanceCost(structure)));
                    //txtTime.setText(TextUtilities.GetTimeAmount(structure.GetChargeOwnerTimeRemaining()));
                    //txtTime.setVisibility(VISIBLE);
                }

                imgPower.setImageResource(structure.GetRunning() ? R.drawable.button_online : R.drawable.button_offline);
            }
        }
        else
        {
            List<Structure> CurrentStructures = GetCurrentStructures();
            Structure controlStructure = CurrentStructures.get(0);

            boolean bShowCeaseFire = false;

            for(Structure structure : CurrentStructures)
            {
                if(structure instanceof ArtilleryGun)
                {
                    if(((ArtilleryGun)structure).HasFireOrder())
                    {
                        bShowCeaseFire = true;
                        break;
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

            TextUtilities.AssignHealthStringAndAppearance(txtHealth, CurrentStructures);

            int lOccupiedSlots = 0;
            int lSlotCount = 0;

            if(controlStructure instanceof MissileSite)
            {
                MissileSite missileSite = ((MissileSite) controlStructure);

                if(missileSite.CanTakeICBM())
                {
                    boolean bAutos = false;
                    boolean bSemis = false;
                    boolean bManuals = false;

                    for(Structure structure : CurrentStructures)
                    {
                        lOccupiedSlots += ((MissileSite) structure).GetMissileSystem().GetOccupiedSlotCount();
                        lSlotCount += ((MissileSite) structure).GetMissileSystem().GetSlotCount();

                        if(missileSite.GetAuto())
                            bAutos = true;

                        if(missileSite.GetSemiAuto())
                            bSemis = true;

                        if(missileSite.GetManual())
                            bManuals = true;
                    }

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
                else
                {
                    for(Structure structure : CurrentStructures)
                    {
                        lOccupiedSlots += ((MissileSite) structure).GetMissileSystem().GetOccupiedSlotCount();
                        lSlotCount += ((MissileSite) structure).GetMissileSystem().GetSlotCount();

                    }
                }
            }
            else if(controlStructure instanceof SAMSite)
            {
                boolean bAutos = false;
                boolean bSemis = false;
                boolean bManuals = false;

                for(Structure structure : CurrentStructures)
                {
                    lOccupiedSlots += ((SAMSite) structure).GetInterceptorSystem().GetOccupiedSlotCount();
                    lSlotCount += ((SAMSite) structure).GetInterceptorSystem().GetSlotCount();

                    SAMSite samSite = (SAMSite)structure;

                    if(samSite.GetAuto())
                        bAutos = true;

                    if(samSite.GetSemiAuto())
                        bSemis = true;

                    if(samSite.GetManual())
                        bManuals = true;
                }

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
            else if(controlStructure instanceof ArtilleryGun)
            {
                boolean bAutos = false;
                boolean bSemis = false;
                boolean bManuals = false;

                for(Structure structure : CurrentStructures)
                {
                    ArtilleryGun artilleryGun = (ArtilleryGun)structure;

                    if(artilleryGun.GetAuto())
                        bAutos = true;

                    if(artilleryGun.GetSemiAuto())
                        bSemis = true;

                    if(artilleryGun.GetManual())
                        bManuals = true;
                }

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
            else if(controlStructure instanceof Airbase)
            {
                for(Structure structure : CurrentStructures)
                {
                    Airbase airbase = ((Airbase)structure);

                    lOccupiedSlots += airbase.GetAircraftSystem().GetOccupiedSlotCount();
                    lSlotCount += airbase.GetAircraftSystem().GetSlotCount();
                }
            }
            else
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

            int lNumberRunning = 0;
            int lNumberOffline = 0;

            for(Structure structure : CurrentStructures)
            {
                if(structure.GetRunning())
                    lNumberRunning++;
                else if(structure.GetOffline())
                    lNumberOffline++;
            }

            if(lNumberRunning > 0 && lNumberOffline == 0)
                imgPower.setImageResource(R.drawable.button_online);
            else if(lNumberRunning > 0 && lNumberOffline > 0)
                imgPower.setImageResource(R.drawable.button_onoffmix);
            else
                imgPower.setImageResource(R.drawable.button_offline);

            txtCost.setText(TextUtilities.GetCurrencyString(game.GetConfig().GetMaintenanceCost(controlStructure) * lNumberRunning));
        }
    }

    @Override
    public boolean IsSingleStructure()
    {
        return structureShadow != null;
    }

    @Override
    public Structure GetCurrentStructure()
    {
        return (Structure)structureShadow.GetPointer().GetMapEntity(game);
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        Structure controlStructure = (Structure)StructureList.iterator().next();
        List<Structure> CurrentStructures = new ArrayList<>();

        for(Object object : StructureList)
        {
            Structure structure = (Structure)object;

            CurrentStructures.add((Structure)structure.GetPointer().GetMapEntity(game));
        }

        return CurrentStructures;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        if(structureShadow != null)
        {
            game.SetStructureOnOff(structureShadow.GetID(), structureShadow.GetEntityType(), bOnline);
        }
        else
        {
            List<Integer> IDs = new ArrayList<>();

            for(Object object : StructureList)
            {
                Structure structure = (Structure)object;
                IDs.add(structure.GetID());
            }

            Structure controlStructure = (Structure)StructureList.iterator().next();

            game.SetStructuresOnOff(IDs, controlStructure.GetEntityType(), bOnline);
        }
    }

    @Override
    public void EntityUpdated(LaunchEntity launchEntity)
    {
        boolean bUpdate = false;

        if(launchEntity instanceof MapEntity)
        {
            MapEntity entity = (MapEntity)launchEntity;

            if(structureShadow != null)
            {
                if(entity.ApparentlyEquals(structureShadow))
                    bUpdate = true;
            }

            if(StructureList != null)
            {
                for(Object object : StructureList)
                {
                    Structure structure = (Structure)object;

                    if(entity.ApparentlyEquals(structure))
                    {
                        bUpdate = true;
                        break;
                    }
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
