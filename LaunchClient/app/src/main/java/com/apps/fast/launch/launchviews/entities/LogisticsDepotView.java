package com.apps.fast.launch.launchviews.entities;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.launchviews.controls.BankControl;
import com.apps.fast.launch.launchviews.controls.LogisticsDepotControl;

import java.util.List;

import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;

public class LogisticsDepotView extends StructureView
{
    public LogisticsDepotView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new LogisticsDepotControl(game, activity, structureShadow.GetID());

        super.Setup();

        imgLogo.setImageResource(R.drawable.build_logistics_depot);

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
        return game.GetLogisticsDepot(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.LOGISTICS_DEPOT, bOnline);
    }
}
