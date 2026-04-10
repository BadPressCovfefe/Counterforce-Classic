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
    private PurchaseButton btnBuildSPAAG;
    private LinearLayout lytProduction;
    private LinearLayout lytBuildTank;
    private LinearLayout lytBuildInfantry;
    private TextView txtPrepTime;
    private PurchaseButton btnBuildMissileTank;
    private PurchaseButton btnBuildSAMTank;
    //private PurchaseButton btnBuildHowitzer;
    private PurchaseButton btnBuildInfantry;
    private ImageView imgProduction;

    private Armory armory;

    private boolean bOurStructure;

    protected FrameLayout lytCargo;
    protected LaunchView cargoSystem;

    public ArmoryControl(LaunchClientGame game, MainActivity activity, int lArmoryID)
    {
        super(game, activity, true);
        lID = lArmoryID;

        Armory armory = game.GetArmory(lArmoryID);

        if(armory != null)
        {
            bOurStructure = (armory.GetOwnerID() == game.GetOurPlayerID());
            this.armory = armory;

            if(game.EntityIsFriendly(armory, game.GetOurPlayer()))
            {
                cargoSystem = new CargoSystemControl(game, activity, armory);
            }
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
        btnBuildSPAAG = findViewById(R.id.btnBuildSPAAG);
        lytProduction = findViewById(R.id.lytProduction);
        txtPrepTime = findViewById(R.id.txtPrepTime);
        imgProduction = findViewById(R.id.imgProduction);
        lytCargo = findViewById(R.id.lytCargo);
        lytBuildTank = findViewById(R.id.lytBuildTank);
        lytBuildInfantry = findViewById(R.id.lytBuildInfantry);
        btnBuildMissileTank = findViewById(R.id.btnBuildMissileTank);
        btnBuildSAMTank = findViewById(R.id.btnBuildSAMTank);
        //btnBuildHowitzer = findViewById(R.id.btnBuildHowitzer);
        btnBuildInfantry = findViewById(R.id.btnBuildInfantry);

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
                lytBuildInfantry.setVisibility(GONE);

                switch(armory.GetProducingType())
                {
                    /*case MISSILE_TANK:
                    {
                        imgProduction.setImageResource(R.drawable.marker_missile_tank);
                    }
                    break;

                    case SAM_TANK:
                    {
                        imgProduction.setImageResource(R.drawable.marker_sam_tank);
                    }
                    break;

                    case HOWITZER:
                    {
                        imgProduction.setImageResource(R.drawable.marker_howitzer);
                    }
                    break;*/

                    case MBT:
                    {
                        imgProduction.setImageResource(R.drawable.marker_tank_east);
                    }
                    break;

                    /*case SPAAG:
                    {
                        imgProduction.setImageResource(R.drawable.marker_aa_gun_east);
                    }
                    break;

                    case INFANTRY:
                    {
                        imgProduction.setImageResource(R.drawable.marker_infantry);
                    }
                    break;*/
                }
            }
            else
            {
                /*if(armory.GetIsBarracks())
                    lytBuildInfantry.setVisibility(VISIBLE);
                else
                    lytBuildTank.setVisibility(VISIBLE);*/

                lytProduction.setVisibility(GONE);
                lytBuildTank.setVisibility(VISIBLE);
            }
        }
        else
        {
            lytProduction.setVisibility(GONE);
        }

        if(game.EntityIsFriendly(armory, game.GetOurPlayer()))
        {
            if(cargoSystem != null)
            {
                lytCargo.addView(cargoSystem);
                lytCargo.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.SystemBackgroundColour));
            }
            else
            {
                lytCargo.setVisibility(GONE);
            }
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

                    if(game.EntityIsFriendly(armory, game.GetOurPlayer()))
                    {
                        cargoSystem = new CargoSystemControl(game, activity, armory);
                        lytCargo.removeAllViews();
                        lytCargo.addView(cargoSystem);
                    }

                    if(bOurStructure)
                    {
                        if(armory.GetProducing())
                        {
                            lytProduction.setVisibility(VISIBLE);
                            txtPrepTime.setText(TextUtilities.GetTimeAmount(armory.GetProdTimeRemaining()));

                            lytBuildTank.setVisibility(GONE);
                            lytBuildInfantry.setVisibility(GONE);
                        }
                        else
                        {
                            /*if(armory.GetIsBarracks())
                                lytBuildInfantry.setVisibility(VISIBLE);
                            else
                                lytBuildTank.setVisibility(VISIBLE);*/

                            lytProduction.setVisibility(GONE);
                            lytBuildTank.setVisibility(VISIBLE);
                        }
                    }
                    else
                    {
                        lytProduction.setVisibility(GONE);
                        lytBuildTank.setVisibility(GONE);
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
