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

import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airplane;
import launch.game.entities.SAMSite;
import launch.game.entities.Structure;
import launch.game.entities.Tank;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.utilities.LaunchUtilities;

public class TankMaintenanceView extends LaunchView implements LaunchUICommon.TankInfoProvider
{
    private TankInterface tankShadow = null;
    private Collection TankList = null;
    private TextView txtEmptySlots;
    private LinearLayout btnMove;
    private LinearLayout btnAttack;
    private LinearLayout btnCeaseFire;
    private LinearLayout lytModeControls;
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
     */
    public TankMaintenanceView(LaunchClientGame game, MainActivity activity, TankInterface tank)
    {
        super(game, activity, true);
        this.tankShadow = tank;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     */
    public TankMaintenanceView(LaunchClientGame game, MainActivity activity, Collection tanks)
    {
        super(game, activity, true);
        this.TankList = tanks;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_tank_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        TextView txtType = findViewById(R.id.txtTitle);
        ImageView imgTank = findViewById(R.id.imgTank);
        lytModeControls = findViewById(R.id.lytModeControls);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);
        btnAttack = findViewById(R.id.btnAttack);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        TankInterface iconControlTank = tankShadow == null ? (TankInterface)TankList.iterator().next() : tankShadow;
        txtType.setText(TextUtilities.GetEntityTypeAndName(iconControlTank.GetTank(), game));

        switch(iconControlTank.GetType())
        {
            case MISSILE_TANK:
            {
                imgTank.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missile_tank), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlTank).ordinal()]));
            }
            break;

            case SAM_TANK:
            {
                imgTank.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sam_tank), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlTank).ordinal()]));
            }
            break;

            case MBT:
            {
                imgTank.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_tank_east), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlTank).ordinal()]));
            }
            break;

            case SPAAG:
            {
                imgTank.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_aa_gun_east), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlTank).ordinal()]));
            }
            break;
        }

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(tankShadow != null)
                    activity.MoveOrderMode(((LaunchEntity)tankShadow).GetPointer(), null);
                else if(TankList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objTank : TankList.toArray())
                    {
                        TankInterface tank = (TankInterface)objTank;
                        MovablePointers.add(((LaunchEntity)tank).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;
        boolean bShowAttack = false;

        if(tankShadow != null)
        {
            if(tankShadow instanceof Tank)
            {
                Tank tank = (Tank)tankShadow;

                if(tankShadow.IsAnMBT() || tankShadow.HasArtillery())
                    btnAttack.setVisibility(GONE);

                if(tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;

                if(tank.HasInterceptors() || tank.HasArtillery())
                {
                    lytModeControls.setVisibility(VISIBLE);

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
            }
        }
        else if(TankList != null)
        {
            final List<EntityPointer> Pointers = new ArrayList<>();
            boolean bDisplayModes = false;

            for(Object objTank : TankList)
            {
                if(objTank instanceof Tank)
                {
                    Tank tank = (Tank)objTank;

                    if(tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;

                    if(tank.HasInterceptors() || tank.HasArtillery())
                    {
                        bDisplayModes = true;
                        Pointers.add(tank.GetPointer());
                    }

                    if(tank.IsAnMBT() || tank.HasArtillery())
                    {
                        bShowAttack = true;
                    }

                    if(tank.HasFireOrder())
                    {
                        bShowCeaseFire = true;
                    }
                }
            }

            txtCount.setText(Integer.toString(TankList.size()));

            if(bDisplayModes)
            {
                lytModeControls.setVisibility(VISIBLE);

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

                            if(tankShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(((LaunchEntity)tankShadow).GetPointer()));
                            }
                            else if(TankList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : TankList.toArray())
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
        else
        {
            btnCeaseFire.setVisibility(GONE);
        }

        if(bShowAttack)
        {
            btnAttack.setVisibility(VISIBLE);

            btnAttack.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(tankShadow != null)
                    {
                        activity.SetTargetMode(((LaunchEntity)tankShadow).GetPointer(), null);
                    }
                    else if(TankList != null)
                    {
                        List<EntityPointer> MovablePointers = new ArrayList<>();

                        for(Object objTank : TankList.toArray())
                        {
                            TankInterface tank = (TankInterface)objTank;
                            MovablePointers.add(((LaunchEntity)tank).GetPointer());
                        }

                        activity.SetTargetMode(null, MovablePointers);
                    }
                }
            });
        }
        else
        {
            btnAttack.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        boolean bShowCeaseFire = false;

        if(tankShadow != null)
        {
            Tank tank = GetCurrentTank();

            if(tank != null)
            {
                if(tank.GetMissileSystem() != null)
                {
                    int lOccupiedSlots = 0;
                    int lSlotCount = 0;

                    lOccupiedSlots += tank.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += tank.GetMissileSystem().GetSlotCount();

                    if(lSlotCount == 0)
                    {
                        txtEmptySlots.setVisibility(GONE);
                    }

                    txtEmptySlots.setText(context.getString(R.string.empty_slot_count, lOccupiedSlots, lSlotCount));

                    //int OfflineUpkeepCost = (int) 0.1 * game.GetConfig().GetMaintenanceCost(structure); //In keeping with 10% offline cost from LaunchGame. -Corbin

                    if(lOccupiedSlots == lSlotCount)
                        txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    else if (lOccupiedSlots == 0)
                        txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    else
                        txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }

                if(tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;

                if(tank.HasInterceptors())
                {
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
        }
        else
        {
            for(Object objTank : TankList)
            {
                if(objTank instanceof Tank)
                {
                    Tank tank = (Tank)objTank;

                    if(tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;
                }
            }

            List<TankInterface> CurrentTanks = GetCurrentTanks();
            TankInterface controlTank = CurrentTanks.get(0);

            int lOccupiedSlots = 0;
            int lSlotCount = 0;

            if(controlTank.GetMissileSystem() != null)
            {
                for(TankInterface tank : CurrentTanks)
                {
                    if(tank.GetMissileSystem() != null)
                    {
                        lOccupiedSlots += tank.GetMissileSystem().GetOccupiedSlotCount();
                        lSlotCount += tank.GetMissileSystem().GetSlotCount();
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
            }
            else
            {
                txtEmptySlots.setVisibility(GONE);
            }

            if(controlTank.HasInterceptors() && controlTank instanceof Tank)
            {
                boolean bAutos = false;
                boolean bSemis = false;
                boolean bManuals = false;

                for(TankInterface tankInterface : CurrentTanks)
                {
                    lOccupiedSlots += tankInterface.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += tankInterface.GetMissileSystem().GetSlotCount();

                    if(tankInterface instanceof Tank)
                    {
                        Tank tank = (Tank)tankInterface;

                        if(tank.GetAuto())
                            bAutos = true;

                        if(tank.GetSemiAuto())
                            bSemis = true;

                        if(tank.GetManual())
                            bManuals = true;
                    }
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
    public boolean IsSingleTank()
    {
        return tankShadow != null;
    }

    @Override
    public Tank GetCurrentTank()
    {
        return game.GetTank(tankShadow.GetID());
    }

    @Override
    public List<TankInterface> GetCurrentTanks()
    {
        TankInterface controlTank = (Tank)TankList.iterator().next();
        List<TankInterface> CurrentTanks = new ArrayList<>();

        for(Object object : TankList)
        {
            TankInterface tank = (TankInterface)object;
            CurrentTanks.add(game.GetTankInterface(tank.GetID()));
        }

        return CurrentTanks;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(tankShadow != null)
        {
            if(entity.ApparentlyEquals(tankShadow.GetTank()))
                bUpdate = true;
        }

        if(TankList != null)
        {
            for(Object object : TankList)
            {
                TankInterface tank = (TankInterface)object;

                if(entity.ApparentlyEquals(tank.GetTank()))
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
