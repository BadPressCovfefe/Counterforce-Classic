package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.controls.ICBMSystemControl;
import com.apps.fast.launch.views.ButtonFlasher;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class NukeSiteView extends StructureView
{
    protected LaunchView systemView;

    private LinearLayout lytMode;
    private ImageButton btnAuto;
    private ImageButton btnSemi;
    private ImageButton btnManual;
    private LinearLayout lytEngageRange;

    private ButtonFlasher flasherAuto;
    private ButtonFlasher flasherSemi;
    private ButtonFlasher flasherManual;

    public NukeSiteView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new ICBMSystemControl(game, activity, structureShadow.GetID(), true, false);

        super.Setup();

        lytEngageRange = findViewById(R.id.lytRange);
        lytMode = findViewById(R.id.lytMode);
        btnAuto = findViewById(R.id.btnModeAuto);
        btnSemi = findViewById(R.id.btnModeSemi);
        btnManual = findViewById(R.id.btnModeManual);

        flasherAuto = new ButtonFlasher(btnAuto);
        flasherSemi = new ButtonFlasher(btnSemi);
        flasherManual = new ButtonFlasher(btnManual);

        imgLogo.setImageResource(R.drawable.build_icbm_silo);
        lytEngageRange.setVisibility(GONE);

        btnAuto.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetMissileSite(structureShadow.GetID()).GetAuto())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderSAMControl();
                    launchDialog.SetMessage(context.getString(R.string.confirm_auto_silo));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_AUTO);
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
                if(!game.GetMissileSite(structureShadow.GetID()).GetSemiAuto())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderSAMControl();
                    launchDialog.SetMessage(context.getString(R.string.confirm_semi_silo));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_SEMI_AUTO);
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
                if(!game.GetMissileSite(structureShadow.GetID()).GetManual())
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderSAMControl();
                    launchDialog.SetMessage(context.getString(R.string.confirm_manual_silo));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            game.SetICBMSiloMode(structureShadow.GetID(), MissileSite.MODE_MANUAL);
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
                    if(game.EntityIsFriendly(structure, game.GetOurPlayer()) && structureShadow.GetOwnerID() == game.GetOurPlayerID() && !structure.GetSelling() && game.GetOurPlayer().Functioning())
                    {
                        MissileSite missileSite = (MissileSite) structure;

                        if (missileSite.GetAuto())
                        {
                            flasherAuto.TurnGreen(context);
                        }
                        else
                        {
                            flasherAuto.TurnOff(context);
                        }

                        if (missileSite.GetSemiAuto())
                        {
                            flasherSemi.TurnGreen(context);
                        }
                        else
                        {
                            flasherSemi.TurnOff(context);
                        }

                        if(missileSite.GetManual())
                        {
                            flasherManual.TurnGreen(context);
                        }
                        else
                        {
                            flasherManual.TurnOff(context);
                        }

                        lytMode.setVisibility(VISIBLE);
                    }

                    if(!structure.GetSelling())
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
        return game.GetMissileSite(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.MISSILE_SITE, bOnline);
    }
}
