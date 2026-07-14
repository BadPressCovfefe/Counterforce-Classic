package com.apps.fast.launch.launchviews;

import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Tank;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;
import launch.game.entities.TankInterface;

public class TankMaintenanceView extends LaunchView implements LaunchUICommon.TankInfoProvider
{
    private Tank tankShadow = null;
    private Collection TankList = null;
    private TextView txtCost;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     */
    public TankMaintenanceView(LaunchClientGame game, MainActivity activity, Tank tank)
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

        Tank iconControlTank = tankShadow == null ? (Tank)TankList.iterator().next() : tankShadow;
        txtType.setText(TextUtilities.GetEntityTypeAndName(iconControlTank, game));

        boolean bShowCeaseFire = false;
        boolean bShowAttack = false;

        if(TankList != null)
        {
            txtCount.setText(Integer.toString(TankList.size()));
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
                txtCost.setText(TextUtilities.GetCurrencyString(Defs.TANK_MAINTENANCE_COST));

                if(tank.GetMoveOrders() != Movable.MoveOrders.WAIT && tank.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;
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

            txtCost.setText(TextUtilities.GetCurrencyString(Defs.TANK_MAINTENANCE_COST * TankList.size()));
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
    public List<Tank> GetCurrentTanks()
    {
        List<Tank> CurrentTanks = new ArrayList<>();

        for(Object object : TankList)
        {
            Tank tank = (Tank)object;
            CurrentTanks.add(game.GetTank(tank.GetID()));
        }

        return CurrentTanks;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(tankShadow != null)
        {
            if(entity.ApparentlyEquals(tankShadow))
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
