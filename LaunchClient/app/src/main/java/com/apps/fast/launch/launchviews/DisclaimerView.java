package com.apps.fast.launch.launchviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;

import java.util.Timer;
import java.util.TimerTask;

import launch.game.LaunchClientGame;

public class DisclaimerView extends LaunchView
{
    private static final long TWO_SECONDS = 2000;
    private byte cPageNumber = 1;
    private LinearLayout lytPage1;
    private LinearLayout lytPage2;
    private LinearLayout lytPage3;
    private LinearLayout btnBack;
    private LinearLayout btnNext;
    private CheckBox chkAgree;
    private boolean bAgreed = false;

    public DisclaimerView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.main_disclaimer, this);

        chkAgree = findViewById(R.id.chkAgree);
        lytPage1 = findViewById(R.id.lytPage1);
        lytPage2 = findViewById(R.id.lytPage2);
        lytPage3 = findViewById(R.id.lytPage3);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        btnBack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(cPageNumber == 3)
                {
                    lytPage3.setVisibility(GONE);
                    lytPage2.setVisibility(VISIBLE);
                    lytPage1.setVisibility(GONE);
                    cPageNumber = 2;
                }
                else if(cPageNumber == 2)
                {
                    lytPage3.setVisibility(GONE);
                    lytPage2.setVisibility(GONE);
                    lytPage1.setVisibility(VISIBLE);
                    cPageNumber = 1;
                }
            }
        });

        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(cPageNumber == 2)
                {
                    lytPage3.setVisibility(VISIBLE);
                    lytPage2.setVisibility(GONE);
                    lytPage1.setVisibility(GONE);
                    cPageNumber = 3;
                }
                else if(cPageNumber == 1)
                {
                    lytPage3.setVisibility(GONE);
                    lytPage2.setVisibility(VISIBLE);
                    lytPage1.setVisibility(GONE);
                    cPageNumber = 2;
                }
            }
        });

        chkAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                //We will dismiss the page two seconds after the user checks agree. This boolean check is so the timed event only fires once.
                if(!bAgreed)
                {
                    bAgreed = true;

                    new Timer().schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                            editor.putBoolean(ClientDefs.SETTINGS_DISCLAIMER_ACCEPTED, true);
                            editor.commit();

                            activity.DisclaimerAgreed();
                        }
                    }, TWO_SECONDS);
                }
            }
        });
    }

    @Override
    public void Update()
    {

    }
}
