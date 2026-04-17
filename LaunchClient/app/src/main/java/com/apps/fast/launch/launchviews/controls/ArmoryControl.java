package com.apps.fast.launch.launchviews.controls;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.PurchaseButton;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.Armory;
import launch.game.entities.LaunchEntity;

public class ArmoryControl extends LaunchView
{
    private int lID;

    private PurchaseButton btnBuildMBT;
    private LinearLayout lytProduction;
    private LinearLayout lytBuildTank;
    private TextView txtQueue;
    private TextView txtPrepTime;
    private ImageView imgProduction;

    private Armory armory;

    private boolean bOurStructure;

    public ArmoryControl(LaunchClientGame game, MainActivity activity, int lArmoryID)
    {
        super(game, activity, true);
        lID = lArmoryID;

        Armory armory = game.GetArmory(lArmoryID);

        if(armory != null)
        {
            bOurStructure = (armory.GetOwnerID() == game.GetOurPlayerID());
            this.armory = armory;
        }
        else
        {
            bOurStructure = false;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_armory, this);

        btnBuildMBT = findViewById(R.id.btnBuildMBT);
        lytProduction = findViewById(R.id.lytProduction);
        txtPrepTime = findViewById(R.id.txtPrepTime);
        imgProduction = findViewById(R.id.imgProduction);
        lytBuildTank = findViewById(R.id.lytBuildTank);
        txtQueue = findViewById(R.id.txtQueue);

        Armory armory = game.GetArmory(lID);

        btnBuildMBT.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.MBT, null, Defs.TANK_BUILD_COST);
        //btnBuildSPAAG.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.SPAAG, null, Defs.TANK_BUILD_COST);
        //btnBuildMissileTank.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.MISSILE_TANK, null, Defs.TANK_BUILD_COST);
        //btnBuildSAMTank.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.SAM_TANK, null, Defs.TANK_BUILD_COST);
        //btnBuildHowitzer.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.HOWITZER, null, Defs.TANK_BUILD_COST);
        //btnBuildInfantry.SetUnit(game, activity, armory.GetPointer(), EntityPointer.EntityType.INFANTRY, null, Defs.INFANTRY_UNIT_BUILD_COST);

        if(bOurStructure)
        {
            if(armory.GetProducing())
            {
                lytProduction.setVisibility(VISIBLE);
                txtPrepTime.setText(TextUtilities.GetTimeAmount(armory.GetProdTimeRemaining()));
                lytBuildTank.setVisibility(GONE);
            }
            else
            {
                /*if(armory.GetIsBarracks())
                    lytBuildInfantry.setVisibility(VISIBLE);
                else
                    lytBuildTank.setVisibility(VISIBLE);*/

                lytProduction.setVisibility(GONE);
                txtQueue.setVisibility(GONE);
                lytBuildTank.setVisibility(VISIBLE);
            }
        }
        else
        {
            lytProduction.setVisibility(GONE);
            txtQueue.setVisibility(GONE);
            lytBuildTank.setVisibility(GONE);
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
                Armory armory = game.GetArmory(lID);

                if(armory != null)
                {
                    bOurStructure = armory.GetOwnedBy(game.GetOurPlayerID());

                    if(bOurStructure)
                    {
                        if(armory.GetProducing())
                        {
                            lytProduction.setVisibility(VISIBLE);
                            txtPrepTime.setText(TextUtilities.GetTimeAmount(armory.GetProdTimeRemaining()));

                            lytBuildTank.setVisibility(GONE);
                        }
                        else
                        {
                            lytProduction.setVisibility(GONE);
                            lytBuildTank.setVisibility(VISIBLE);
                            txtQueue.setVisibility(GONE);
                        }
                    }
                    else
                    {
                        lytProduction.setVisibility(GONE);
                        lytBuildTank.setVisibility(GONE);
                        txtQueue.setVisibility(GONE);
                    }
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals(armory))
        {
            Update();
        }
    }
}
