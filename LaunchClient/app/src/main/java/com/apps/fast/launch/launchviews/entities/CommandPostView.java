package com.apps.fast.launch.launchviews.entities;

import android.view.View;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class CommandPostView extends StructureView
{
    public CommandPostView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
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

        imgLogo.setImageResource(R.drawable.build_bunker);

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
        return game.GetCommandPost(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.COMMAND_POST, bOnline);
    }
}
