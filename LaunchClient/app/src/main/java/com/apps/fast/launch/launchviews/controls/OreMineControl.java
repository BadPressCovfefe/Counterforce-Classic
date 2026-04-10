package com.apps.fast.launch.launchviews.controls;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import java.util.List;
import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.Defs;
import launch.game.entities.OreMine;
import launch.game.entities.Player;
import launch.game.entities.OreMine;
import launch.game.entities.conceptuals.Resource;
import launch.utilities.LaunchUtilities;

public class OreMineControl extends LaunchView
{
    private int lID;
    private LinearLayout lytIrradiated;
    private OreMine oreMine;

    public OreMineControl(LaunchClientGame game, MainActivity activity, int lOreMineID)
    {
        super(game, activity, true);
        lID = lOreMineID;

        oreMine = game.GetOreMine(lID);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_ore_mine, this);

        lytIrradiated = findViewById(R.id.lytIrradiated);

        if(oreMine != null)
        {
            Update();
        }
        else
        {
            Finish(true);
        }
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                OreMine oreMine = game.GetOreMine(lID);

                if(oreMine != null)
                {
                    if(game.GetRadioactive(oreMine, true))
                    {
                        lytIrradiated.setVisibility(VISIBLE);
                    }
                    else
                    {
                        lytIrradiated.setVisibility(GONE);
                    }
                }
            }
        });
    }
}
