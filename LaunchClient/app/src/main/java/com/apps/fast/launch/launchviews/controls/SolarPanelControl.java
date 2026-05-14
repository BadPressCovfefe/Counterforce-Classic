package com.apps.fast.launch.launchviews.controls;

import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.LaunchClientGame;
import launch.game.entities.OreMine;

public class SolarPanelControl extends LaunchView
{
    private int lID;
    private TextView txtGenerationRemaining;

    public SolarPanelControl(LaunchClientGame game, MainActivity activity, int lOreMineID)
    {
        super(game, activity, true);
        lID = lOreMineID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_solar_panel, this);

        txtGenerationRemaining = findViewById(R.id.txtGenerationRemaining);

        Update();
    }

    @Override
    public void Update()
    {
        OreMine solarPanel = game.GetOreMine(lID);

        txtGenerationRemaining.setText(TextUtilities.GetTimeAmount(solarPanel.GetGenerateTimeRemaining()));
    }
}
