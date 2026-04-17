package com.apps.fast.launch.launchviews.entities;

import android.view.View;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.controls.SentryGunControl;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.Armory;
import launch.game.entities.LaunchEntity;
import launch.game.entities.SentryGun;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class SentryGunView extends StructureView
{
    public SentryGunView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new SentryGunControl(game, activity, structureShadow.GetID());

        super.Setup();

        if(structureShadow instanceof SentryGun)
        {
            SentryGun sentry = (SentryGun)structureShadow;

            if(sentry.GetIsWatchTower())
                imgLogo.setImageResource(R.drawable.build_artillery_gun);
            else
                imgLogo.setImageResource(R.drawable.build_sentry_gun);
        }

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
        return game.GetSentryGun(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.SENTRY_GUN, bOnline);
    }
}
