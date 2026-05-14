package com.apps.fast.launch.launchviews.controls;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.LaunchClientGame;
import launch.game.entities.OreMine;

public class FarmControl extends LaunchView
{
    private int lID;
    private TextView txtGenerationRemaining;
    private LinearLayout lytIrradiated;

    public FarmControl(LaunchClientGame game, MainActivity activity, int lFarmID)
    {
        super(game, activity, true);
        lID = lFarmID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_farm, this);

        txtGenerationRemaining = findViewById(R.id.txtGenerationRemaining);
        lytIrradiated = findViewById(R.id.lytIrradiated);

        Update();
    }

    @Override
    public void Update()
    {
        OreMine farm = game.GetOreMine(lID);

        txtGenerationRemaining.setText(TextUtilities.GetTimeAmount(farm.GetGenerateTimeRemaining()));

        if(game.GetRadioactive(farm, true))
        {
            lytIrradiated.setVisibility(VISIBLE);
        }
        else
        {
            lytIrradiated.setVisibility(GONE);
        }
    }
}
