package com.apps.fast.launch.launchviews;

import android.graphics.BitmapFactory;
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
import launch.game.entities.Infantry;
import launch.game.entities.InfantryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable.MoveOrders;

public class InfantryMaintenanceView extends LaunchView implements LaunchUICommon.InfantryInfoProvider
{
    private InfantryInterface infantryShadow = null;
    private Collection InfantryList = null;
    private TextView txtEmptySlots;
    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;
    private LinearLayout btnAttack;


    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param infantry The structure.
     */
    public InfantryMaintenanceView(LaunchClientGame game, MainActivity activity, InfantryInterface infantry)
    {
        super(game, activity, true);
        this.infantryShadow = infantry;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param infantrys List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public InfantryMaintenanceView(LaunchClientGame game, MainActivity activity, Collection infantrys)
    {
        super(game, activity, true);
        this.InfantryList = infantrys;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_infantry_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        TextView txtType = findViewById(R.id.txtTitle);
        ImageView imgInfantry = findViewById(R.id.imgInfantry);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnAttack = findViewById(R.id.btnAttack);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        if(infantryShadow == null && InfantryList.size() == 1)
        {
            infantryShadow = (InfantryInterface)InfantryList.toArray()[0];
            InfantryList.clear();
        }

        InfantryInterface iconControlInfantry = infantryShadow == null ? (InfantryInterface)InfantryList.iterator().next() : infantryShadow;
        imgInfantry.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_infantry), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlInfantry).ordinal()]));
        txtType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)iconControlInfantry, game));

        if(infantryShadow != null)
        {
            txtCount.setVisibility(GONE);
        }

        if(InfantryList != null)
        {
            //Group of structures. Bin the name and state labels and set the count.
            txtCount.setText(Integer.toString(InfantryList.size()));
        }

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(infantryShadow != null)
                    activity.MoveOrderMode(((LaunchEntity)infantryShadow).GetPointer(), null);
                else if(InfantryList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objInfantry : InfantryList.toArray())
                    {
                        InfantryInterface infantry = (InfantryInterface)objInfantry;
                        MovablePointers.add(((LaunchEntity)infantry).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;

        if(infantryShadow != null)
        {
            if(infantryShadow instanceof Infantry)
            {
                Infantry infantry = (Infantry)infantryShadow;

                if(infantry.GetMoveOrders() != MoveOrders.WAIT && infantry.GetMoveOrders() != MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else if(InfantryList != null)
        {
            for(Object objInfantry : InfantryList)
            {
                if(objInfantry instanceof Infantry)
                {
                    Infantry infantry = (Infantry)objInfantry;

                    if(infantry.GetMoveOrders() != MoveOrders.WAIT && infantry.GetMoveOrders() != MoveOrders.DEFEND)
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

                            if(infantryShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(((LaunchEntity)infantryShadow).GetPointer()));
                            }
                            else if(InfantryList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : InfantryList.toArray())
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

        btnAttack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(infantryShadow != null)
                {
                    activity.SetTargetMode(((LaunchEntity)infantryShadow).GetPointer(), null);
                }
                else if(InfantryList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objInfantry : InfantryList.toArray())
                    {
                        InfantryInterface infantry = (InfantryInterface)objInfantry;
                        MovablePointers.add(((LaunchEntity)infantry).GetPointer());
                    }

                    activity.SetTargetMode(null, MovablePointers);
                }
            }
        });

        Update();
    }

    @Override
    public void Update()
    {
        boolean bShowCeaseFire = false;

        if(infantryShadow != null)
        {
            InfantryInterface infantry = GetCurrentInfantry();

            if(infantry instanceof Infantry)
            {
                if(((Infantry)infantry).GetMoveOrders() != MoveOrders.WAIT && ((Infantry)infantry).GetMoveOrders() != MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else if(InfantryList != null)
        {
            for(Object objInfantry : InfantryList)
            {
                if(objInfantry instanceof Infantry)
                {
                    Infantry infantry = (Infantry)objInfantry;

                    if(infantry.GetMoveOrders() != MoveOrders.WAIT && infantry.GetMoveOrders() != MoveOrders.DEFEND)
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
    public boolean IsSingleInfantry()
    {
        return infantryShadow != null;
    }

    @Override
    public InfantryInterface GetCurrentInfantry()
    {
        return (InfantryInterface)(((LaunchEntity)infantryShadow).GetPointer().GetEntity(game));
    }

    @Override
    public List<InfantryInterface> GetCurrentInfantries()
    {
        InfantryInterface controlInfantry = (InfantryInterface)InfantryList.iterator().next();
        List<InfantryInterface> CurrentInfantries = new ArrayList<>();

        for(Object object : InfantryList)
        {
            InfantryInterface infantry = (InfantryInterface)object;
            CurrentInfantries.add(infantry);
        }

        return CurrentInfantries;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(infantryShadow != null)
        {
            if(entity.ApparentlyEquals((LaunchEntity)infantryShadow))
                bUpdate = true;
        }

        if(InfantryList != null)
        {
            for(Object object : InfantryList)
            {
                InfantryInterface infantry = (InfantryInterface)object;

                if(entity.ApparentlyEquals((LaunchEntity)infantry))
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
