package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.controls.ArtillerySystemControl;
import com.apps.fast.launch.launchviews.controls.MissileSystemControl;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.List;

import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.ArtilleryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.SAMSite;
import launch.game.entities.Structure;

public class ArtilleryGunView extends StructureView
{
    protected LaunchView systemView;

    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;

    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    public ArtilleryGunView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new ArtillerySystemControl(game, activity, structureShadow.GetID(), (ArtilleryInterface)structureShadow);

        super.Setup();

        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        ArtilleryGun artillery = (ArtilleryGun)structureShadow;

        if(artillery != null)
        {
            imgLogo.setImageResource(R.drawable.icon_artillery_gun);
            lytMode.setVisibility(VISIBLE);

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

                btnAuto.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.GetArtilleryGun(structureShadow.GetID()).GetAuto())
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
                        if(!game.GetArtilleryGun(structureShadow.GetID()).GetSemiAuto())
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
                        if(!game.GetArtilleryGun(structureShadow.GetID()).GetManual())
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
                    if(!structure.GetSelling() && game.EntityIsFriendly(structure, game.GetOurPlayer()))
                        systemView.Update();

                    if(structureShadow.GetOwnerID() == game.GetOurPlayerID() && !structure.GetSelling() && game.GetOurPlayer().Functioning())
                    {
                        ArtilleryGun artillery = (ArtilleryGun)structure;

                        if(artillery.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if(artillery.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(artillery.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }

                        lytMode.setVisibility(VISIBLE);
                    }
                    else
                    {
                        lytMode.setVisibility(GONE);
                    }
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
        return game.GetArtilleryGun(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.ARTILLERY_GUN, bOnline);
    }
}
