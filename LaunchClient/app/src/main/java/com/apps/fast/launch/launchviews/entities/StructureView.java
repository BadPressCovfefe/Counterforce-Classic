package com.apps.fast.launch.launchviews.entities;

import static android.view.Gravity.CENTER_VERTICAL;
import static android.widget.LinearLayout.HORIZONTAL;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.ResourceSystem;

/**
 * Created by tobster on 09/11/15.
 */
public abstract class StructureView extends LaunchView implements LaunchUICommon.StructureOnOffInfoProvider
{
    protected ImageView imgLogo;
    private TextView txtHP;

    private TextView txtName;
    protected TextView txtNameButton;
    protected LinearLayout lytNameEdit;
    protected EditText txtNameEdit;
    protected LinearLayout btnApplyName;
    private View viewAdmin;
    private LinearLayout btnAdminDelete;
    private TextView txtOffline;
    private TextView txtBooting;
    private TextView txtOnline;
    private TextView txtLoad;
    private TextView txtDecommissioning;
    private LinearLayout btnPower;
    private ImageView imgPower;
    private LinearLayout btnSell;
    private LinearLayout btnCancelSale;
    private LinearLayout btnRepair;
    protected FrameLayout lytConfig;
    protected LaunchView systemView;
    protected Structure structureShadow;

    public StructureView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, true);
        structureShadow = (Structure)structure;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_structure, this);
        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        ((TextView)findViewById(R.id.txtPlayerJoins)).setText(TextUtilities.GetOwnedEntityName(structureShadow, game));

        imgLogo = findViewById(R.id.imgLogo);
        txtHP = findViewById(R.id.txtHPTitle);

        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);

        txtOffline = findViewById(R.id.txtOffline);
        txtBooting = findViewById(R.id.txtBooting);
        txtOnline = findViewById(R.id.txtOnline);
        txtDecommissioning = findViewById(R.id.txtDecommissioning);
        btnPower = findViewById(R.id.btnPower);
        imgPower = findViewById(R.id.imgPower);
        btnSell = findViewById(R.id.btnSell);
        btnRepair = findViewById(R.id.btnRepair);
        lytConfig = findViewById(R.id.lytConfig);
        btnCancelSale = findViewById(R.id.btnCancelSale);
        txtLoad = findViewById(R.id.txtLoad);

        FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

        UnitControls controls = new UnitControls(game, activity, structureShadow);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        viewAdmin = findViewById(R.id.viewAdmin);
        btnAdminDelete = findViewById(R.id.btnAdminDelete);

        if(game.GetOurPlayer().GetIsAnAdmin())
        {
            viewAdmin.setVisibility(VISIBLE);
            btnAdminDelete.setVisibility(VISIBLE);

            btnAdminDelete.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Structure structure = GetCurrentStructure();

                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.admin_delete_confirm, structure.GetTypeName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AdminDelete(structure.GetPointer());
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
            });
        }
        else
        {
            viewAdmin.setVisibility(GONE);
            btnAdminDelete.setVisibility(GONE);
        }

        if(structureShadow.GetOwnerID() == game.GetOurPlayerID())
        {
            if(!structureShadow.Destroyed())
            {
                txtName.setVisibility(GONE);

                txtNameButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.ExpandView();
                        txtNameButton.setVisibility(GONE);
                        lytNameEdit.setVisibility(VISIBLE);
                    }
                });

                txtNameEdit.setText(structureShadow.GetName());

                LaunchUICommon.SetPowerButtonOnClickListener(activity, btnPower, this, game);

                btnSell.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Structure structure = GetCurrentStructure();

                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderPurchase();
                        launchDialog.SetMessage(context.getString(R.string.decommission_confirm, structure.GetTypeName(), TextUtilities.GetCostStatement(game.GetSaleValue(structure)), TextUtilities.GetTimeAmount(game.GetConfig().GetDecommissionTime(structure))));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                Sell();
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
                });

                btnCancelSale.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderPurchase();
                        launchDialog.SetMessage(context.getString(R.string.cancel_decommission_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                Sell();
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
                });

                btnRepair.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(!game.InBattle(game.GetOurPlayer()))
                        {
                            Structure structure = GetCurrentStructure();

                            Map<Resource.ResourceType, Long> Costs = game.GetRepairCost(structure);

                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderHealth();
                            launchDialog.SetMessage(context.getString(R.string.repair_confirm, structure.GetTypeName(), TextUtilities.GetCostStatement(Costs)));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();

                                    if(game.GetOurPlayer().GetWealth() >= Costs.get(Resource.ResourceType.WEALTH))
                                    {
                                        game.RepairEntity(structureShadow.GetPointer());
                                    }
                                    else
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                                    }
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
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_repair));
                        }
                    }
                });
            }
            else
            {
                lytConfig.setVisibility(GONE);
            }

            txtLoad.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.LoadLootMode(structureShadow);
                }
            });
        }
        else
        {
            txtNameButton.setVisibility(GONE);
            btnPower.setVisibility(GONE);
        }

        lytConfig.setBackgroundColor(Utilities.ColourFromAttr(context, game.EntityIsFriendly(structureShadow, game.GetOurPlayer()) ? R.attr.SystemBackgroundColour : R.attr.EnemyBackgroundColour));
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Structure structure = GetCurrentStructure();

                if(structure != null)
                {
                    String strName = Utilities.GetEntityName(context, structure);

                    txtName.setText(strName);
                    txtNameButton.setText(strName);

                    TextUtilities.AssignHealthStringAndAppearance(txtHP, structure);

                    txtOffline.setVisibility(structure.GetOffline() ? VISIBLE : GONE);
                    txtBooting.setVisibility(structure.GetBooting() ? VISIBLE : GONE);
                    txtOnline.setVisibility(structure.GetOnline() ? VISIBLE : GONE);
                    txtDecommissioning.setVisibility(structure.GetSelling() ? VISIBLE : GONE);

                    if(structure.GetOwnerID() == game.GetOurPlayerID())
                    {
                        if(structure.GetSelling())
                        {
                            btnPower.setVisibility(GONE);
                        }
                        else
                        {
                            btnPower.setVisibility(VISIBLE);

                            imgPower.setImageResource(structure.GetRunning() ? R.drawable.button_online : R.drawable.button_offline);
                        }

                        btnRepair.setVisibility(structure.AtFullHealth() ? GONE : VISIBLE);
                    }
                    else
                    {
                        btnPower.setVisibility(GONE);
                        btnRepair.setVisibility(GONE);
                    }

                    if(!structure.GetSelling())
                    {
                        btnSell.setVisibility((structure.GetOwnerID() == game.GetOurPlayerID()) && game.GetOurPlayer().Functioning() ? VISIBLE : GONE);
                        lytConfig.setVisibility(VISIBLE);
                        btnCancelSale.setVisibility(GONE);
                    }
                    else
                    {
                        btnCancelSale.setVisibility((structure.GetOwnerID() == game.GetOurPlayerID()) && game.GetOurPlayer().Functioning() ? VISIBLE : GONE);
                        btnSell.setVisibility(GONE);
                        lytConfig.setVisibility(GONE);
                    }

                    if (structure.GetBooting())
                    {
                        txtBooting.setText(context.getString(R.string.state_booting, TextUtilities.GetTimeAmount(structure.GetStateTimeRemaining())));
                    }

                    if (structure.GetSelling())
                    {
                        txtDecommissioning.setText(context.getString(R.string.state_decommissioning, TextUtilities.GetTimeAmount(structure.GetStateTimeRemaining())));
                    }
                }
                else
                {
                    Log.i("LaunchWTF", "Structure is null. Finishing...");
                    Finish(true);
                }
            }
        });
    }

    protected void Sell()
    {
        game.SellEntity(structureShadow.GetPointer());
    }
}
