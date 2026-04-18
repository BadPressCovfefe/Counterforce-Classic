package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Player;
import launch.game.entities.Warehouse;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.CargoSystem;

public class WarehouseControl extends LaunchView
{
    private int lID;
    private boolean bOurStructure;
    private Warehouse warehouse;
    public WarehouseControl(LaunchClientGame game, MainActivity activity, int lWarehouseID)
    {
        super(game, activity, true);
        lID = lWarehouseID;

        if(game.GetWarehouse(lID) != null)
        {
            bOurStructure = (game.GetWarehouse(lID).GetOwnerID() == game.GetOurPlayerID());
            warehouse = game.GetWarehouse(lID);
        }
        else
        {
            Finish(true);
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_bank, this);

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Warehouse warehouse = game.GetWarehouse(lID);

                if(warehouse != null)
                {
                    bOurStructure = warehouse.GetOwnedBy(game.GetOurPlayerID());
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals(warehouse))
        {
            Update();
        }
    }
}
