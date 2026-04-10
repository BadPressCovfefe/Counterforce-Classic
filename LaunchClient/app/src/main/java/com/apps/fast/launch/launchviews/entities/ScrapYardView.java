package com.apps.fast.launch.launchviews.entities;

import android.view.View;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.controls.ArmoryControl;
import com.apps.fast.launch.launchviews.controls.ProcessorControl;
import com.apps.fast.launch.launchviews.controls.ScrapYardControl;

import java.util.List;

import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;

public class ScrapYardView extends StructureView
{
    protected LaunchView systemView;

    public ScrapYardView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new ScrapYardControl(game, activity, structureShadow.GetID());

        super.Setup();

        if(structureShadow.GetOwnerID() == game.GetOurPlayerID())
        {
            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetEntityName(structureShadow.GetPointer(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });
        }

        imgLogo.setImageResource(R.drawable.icon_scrap_yard);

        lytConfig.addView(systemView);
        Update();
    }

    @Override
    public void Update()
    {
        super.Update();
    }

    @Override
    public boolean IsSingleStructure()
    {
        return true;
    }

    @Override
    public Structure GetCurrentStructure()
    {
        return game.GetScrapYard(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.SCRAP_YARD, bOnline);
    }
}
