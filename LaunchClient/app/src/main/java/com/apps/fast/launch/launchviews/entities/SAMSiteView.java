package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.controls.MissileSystemControl;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.SAMSite;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class SAMSiteView extends StructureView
{
    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;

    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    private TextView txtEngageDistance;
    protected TextView txtEngageDistanceButton;
    protected LinearLayout lytEngageDistanceEdit;
    protected EditText txtEngageDistanceEdit;
    protected LinearLayout btnApplyEngageDistance;
    private LinearLayout lytEngageRange;

    protected SAMSite samSite;

    public SAMSiteView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
        samSite = ((SAMSite)structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new MissileSystemControl(game, activity, structureShadow.GetID(), structureShadow, false);

        super.Setup();

        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        txtEngageDistanceButton = findViewById(R.id.txtEngageDistanceButton);
        lytEngageDistanceEdit = findViewById(R.id.lytEngageDistanceEdit);
        txtEngageDistanceEdit = findViewById(R.id.txtEngageDistanceEdit);
        btnApplyEngageDistance = findViewById(R.id.btnApplyEngageDistance);
        lytEngageRange = findViewById(R.id.lytRange);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        imgLogo.setImageResource(R.drawable.build_sam_site);
        lytMode.setVisibility(VISIBLE);


        if(structureShadow.GetOwnerID() == game.GetOurPlayerID() && !structureShadow.GetSelling())
        {
            lytEngageRange.setVisibility(VISIBLE);

            txtEngageDistanceButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ExpandView();
                    txtEngageDistanceButton.setVisibility(GONE);
                    lytEngageDistanceEdit.setVisibility(VISIBLE);
                }
            });

            btnAuto.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetSAMSite(structureShadow.GetID()).GetAuto())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.confirm_auto));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_AUTO);
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                }
            });

            btnSemi.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetSAMSite(structureShadow.GetID()).GetSemiAuto())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.confirm_semi));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_SEMI_AUTO);
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                }
            });

            btnManual.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetSAMSite(structureShadow.GetID()).GetManual())
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.confirm_manual));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                game.SetSAMSiteMode(structureShadow.GetPointer(), SAMSite.MODE_MANUAL);
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                }
            });
        }
        else
        {
            txtEngageDistanceButton.setVisibility(GONE);
        }

        if(game.EntityIsFriendly(structureShadow, game.GetOurPlayer()))
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
                    if(structureShadow.GetOwnerID() == game.GetOurPlayerID() && !structure.GetSelling() && game.GetOurPlayer().Functioning())
                    {
                        SAMSite samSite = (SAMSite) structure;

                        if(samSite.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if(samSite.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(samSite.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }

                        txtEngageDistanceButton.setText(String.format("%s (Tap to edit)", TextUtilities.AssignSpeedFromKPH(samSite.GetEngagementSpeed())));

                        lytMode.setVisibility(VISIBLE);
                    }
                    else
                    {
                        lytMode.setVisibility(GONE);
                    }

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
        return game.GetSAMSite(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.SAM_SITE, bOnline);
    }
}
