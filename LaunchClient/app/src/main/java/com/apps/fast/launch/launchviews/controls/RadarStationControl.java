package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;

import org.w3c.dom.Text;

import java.util.List;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.RadarStation;
import launch.game.entities.Structure;
import launch.game.systems.MissileSystem;

public class RadarStationControl extends LaunchView
{
    private int lID;

    private boolean bOurStructure;

    private LinearLayout lytScan;
    private ImageView btnScan;
    private RadarStation radarStation;

    public RadarStationControl(LaunchClientGame game, MainActivity activity, int lRadarStationID)
    {
        super(game, activity, true);
        lID = lRadarStationID;

        radarStation = game.GetRadarStation(lRadarStationID);

        if(radarStation != null)
        {
            bOurStructure = (radarStation.GetOwnerID() == game.GetOurPlayerID());
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_radar_station, this);

        lytScan = findViewById(R.id.lytScan);
        btnScan = findViewById(R.id.btnScan);

        if(bOurStructure)
        {
            lytScan.setVisibility(VISIBLE);
            btnScan.setVisibility(VISIBLE);

            if(radarStation.GetRadarActive())
            {
                btnScan.setImageResource(R.drawable.button_radar_inactive);
            }
            else
            {
                btnScan.setImageResource(R.drawable.button_radar_active);
            }

            btnScan.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!radarStation.GetOnline())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                    }
                    else if(!radarStation.GetRadarActive())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.toggle_radar_confirm_radar_station));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                game.RadarScan(radarStation.GetPointer());
                            }
                        });
                        launchDialog.SetOnClickNo(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                    else
                    {
                        game.RadarScan(radarStation.GetPointer());
                    }
                }
            });
        }
        else
        {
            lytScan.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                radarStation = game.GetRadarStation(lID);

                if(radarStation != null && bOurStructure)
                {
                    lytScan.setVisibility(VISIBLE);
                    btnScan.setVisibility(VISIBLE);

                    if(radarStation.GetRadarActive())
                    {
                        btnScan.setImageResource(R.drawable.button_radar_active);
                    }
                    else
                    {
                        btnScan.setImageResource(R.drawable.button_radar_inactive);
                    }
                }
                else
                {
                    lytScan.setVisibility(GONE);
                }
            }
        });
    }
}
