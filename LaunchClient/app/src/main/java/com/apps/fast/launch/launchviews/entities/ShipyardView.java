package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.EmptyShipyardSlotView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.launchviews.controls.LogisticsDepotControl;
import com.apps.fast.launch.launchviews.controls.NavalProductionOrderView;
import com.apps.fast.launch.launchviews.controls.ShipyardControl;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Shipyard;
import launch.game.entities.conceptuals.ShipProductionOrder;

/**
 * Created by Corbin.
 */
public class ShipyardView extends StructureView
{
    public ShipyardView(LaunchClientGame game, MainActivity activity, Shipyard shipyard)
    {
        super(game, activity, shipyard);
    }

    @Override
    protected void Setup()
    {
        systemView = new ShipyardControl(game, activity, structureShadow.GetID());

        super.Setup();

        imgLogo.setImageResource(R.drawable.image_shipyard);

        lytConfig.addView(systemView);
        Update();
    }

    @Override
    public void Update()
    {
        super.Update();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Structure structure = GetCurrentStructure();

                if(structure != null)
                {
                    if (!structure.GetSelling())
                        systemView.Update();
                }
            }
        });
    }

    @Override
    public boolean IsSingleStructure()
    {
        return true;
    }

    @Override
    public Structure GetCurrentStructure()
    {
        return game.GetShipyard(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.SHIPYARD, bOnline);
    }
}
