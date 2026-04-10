package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.CargoTruckInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.Submarine;

/**
 * Created by tobster on 09/11/15.
 */
public class SubmarineMaintenanceView extends LaunchView implements LaunchUICommon.SubmarineInfoProvider
{
    private Submarine submarineShadow = null;
    private Collection SubmarineList = null;

    private TextView txtEmptySlots;
    private TextView txtFuelLevel;
    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param submarine The structure.
     */
    public SubmarineMaintenanceView(LaunchClientGame game, MainActivity activity, Submarine submarine)
    {
        super(game, activity, true);
        this.submarineShadow = submarine;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param submarines List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public SubmarineMaintenanceView(LaunchClientGame game, MainActivity activity, Collection submarines)
    {
        super(game, activity, true);
        this.SubmarineList = submarines;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_submarine_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        ImageView imgType = findViewById(R.id.imgSubmarineType);
        TextView txtType = findViewById(R.id.txtTitle);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        Submarine iconControlSubmarine = submarineShadow == null ? (Submarine)SubmarineList.iterator().next() : submarineShadow;
        txtType.setText(TextUtilities.GetEntityTypeAndName(iconControlSubmarine, game));

        //If there is just one submarine in the list, just treat it like we selected a single submarine.
        if(submarineShadow == null && SubmarineList.size() == 1)
        {
            submarineShadow = (Submarine)SubmarineList.toArray()[0];
            SubmarineList.clear();
        }

        if(submarineShadow != null)
        {
            if(submarineShadow.GetNuclear())
                txtFuelLevel.setText(context.getString(R.string.infinite));
            else
                TextUtilities.AssignFuelPercentageString(txtFuelLevel, submarineShadow);

            txtFuelLevel.setVisibility(VISIBLE);
            txtCount.setVisibility(GONE);

            try
            {
                imgType.setImageBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, submarineShadow));
            }
            catch(Exception ex) { /* Don't care.*/ }
        }

        if(SubmarineList != null)
        {
            //Group of structures. Bin the name and state labels and set the count.
            txtCount.setText(Integer.toString(SubmarineList.size()));
            txtFuelLevel.setVisibility(GONE);
        }

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(submarineShadow != null)
                    activity.MoveOrderMode(submarineShadow.GetPointer(), null);
                else if(SubmarineList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objSub : SubmarineList.toArray())
                    {
                        MovablePointers.add(((Submarine)objSub).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;

        if(submarineShadow != null)
        {
            if(submarineShadow.GetMoveOrders() != Movable.MoveOrders.WAIT && submarineShadow.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                bShowCeaseFire = true;
        }
        else if(SubmarineList != null)
        {
            for(Object obj : SubmarineList)
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

                            if(submarineShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(submarineShadow.GetPointer()));
                            }
                            else if(SubmarineList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : SubmarineList)
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

        if(submarineShadow != null)
        {
            Submarine submarine = GetCurrentSubmarine();

            if (submarine != null)
            {
                if(submarineShadow.GetNuclear())
                    txtFuelLevel.setText(context.getString(R.string.infinite));
                else
                    TextUtilities.AssignFuelPercentageString(txtFuelLevel, submarineShadow);

                int lOccupiedSlots = 0;
                int lSlotCount = 0;

                if(submarine.HasMissiles())
                {
                    lOccupiedSlots = submarine.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount = submarine.GetMissileSystem().GetSlotCount();
                }

                if(submarine.HasTorpedoes())
                {
                    lOccupiedSlots = submarine.GetTorpedoSystem().GetOccupiedSlotCount();
                    lSlotCount = submarine.GetTorpedoSystem().GetSlotCount();
                }

                if(submarine.HasICBMs())
                {
                    lOccupiedSlots = submarine.GetICBMSystem().GetOccupiedSlotCount();
                    lSlotCount = submarine.GetICBMSystem().GetSlotCount();
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

                if(submarine.GetMoveOrders() != Movable.MoveOrders.WAIT && submarine.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else
        {
            txtFuelLevel.setVisibility(GONE);
            List<Submarine> CurrentSubmarines = GetCurrentSubmarines();
            Submarine controlSubmarine = CurrentSubmarines.get(0);

            int lOccupiedSlots = 0;
            int lSlotCount = 0;

            for(Submarine submarine : CurrentSubmarines)
            {
                if(submarine.HasMissiles())
                {
                    lOccupiedSlots += submarine.GetMissileSystem().GetOccupiedSlotCount();
                    lSlotCount += submarine.GetMissileSystem().GetSlotCount();
                }

                if(submarine.HasICBMs())
                {
                    lOccupiedSlots += submarine.GetICBMSystem().GetOccupiedSlotCount();
                    lSlotCount += submarine.GetICBMSystem().GetSlotCount();
                }

                if(submarine.HasTorpedoes())
                {
                    lOccupiedSlots += submarine.GetTorpedoSystem().GetOccupiedSlotCount();
                    lSlotCount += submarine.GetTorpedoSystem().GetSlotCount();
                }
            }

            if(lSlotCount == 0)
            {
                txtEmptySlots.setVisibility(GONE);
            }

            txtEmptySlots.setText(context.getString(R.string.empty_slot_count, lOccupiedSlots, lSlotCount));

            if(lOccupiedSlots == lSlotCount)
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if(lOccupiedSlots == 0)
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            else
                txtEmptySlots.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));

            for(Object obj : SubmarineList)
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
    public boolean IsSingleSubmarine()
    {
        return submarineShadow != null;
    }

    @Override
    public Submarine GetCurrentSubmarine()
    {
        return game.GetSubmarine(submarineShadow.GetID());
    }

    @Override
    public List<Submarine> GetCurrentSubmarines()
    {
        Submarine controlSubmarine = (Submarine)SubmarineList.iterator().next();
        List<Submarine> CurrentSubmarines = new ArrayList<>();

        for(Object object : SubmarineList)
        {
            Submarine submarine = (Submarine)object;
            CurrentSubmarines.add(game.GetSubmarine(submarine.GetID()));
        }

        return CurrentSubmarines;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(submarineShadow != null)
        {
            if(entity.ApparentlyEquals(submarineShadow))
                bUpdate = true;
        }

        if(SubmarineList != null)
        {
            for(Object object : SubmarineList)
            {
                Submarine submarine = (Submarine)object;

                if(entity.ApparentlyEquals(submarine))
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
