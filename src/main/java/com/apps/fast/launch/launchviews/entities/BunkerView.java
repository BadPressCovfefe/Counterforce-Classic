package com.apps.fast.launch.launchviews.entities;

import android.view.View;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.controls.BunkerControl;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;

public class BunkerView extends StructureView
{
    public BunkerView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new BunkerControl(game, activity, structureShadow.GetID());

        super.Setup();

        if(structureShadow.GetOwnerID() == game.GetOurPlayerID())
        {
            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetBunkerName(structureShadow.GetID(), txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });
        }

        imgLogo.setImageResource(R.drawable.icon_bunker);

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
        return game.GetBunker(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetBunkerOnOff(structureShadow.GetID(), bOnline);
    }

    @Override
    protected void Sell()
    {
        game.SellBunker(structureShadow.GetID());
    }

    @Override
    protected void Repair()
    {
        game.RepairBunker(structureShadow.GetID());
    }
}
