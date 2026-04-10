package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.List;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Processor;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class BuildDistributorView extends LaunchView
{
    private PurchaseButton btnIron;
    private PurchaseButton btnCoal;
    private PurchaseButton btnOil;
    private PurchaseButton btnCrops;
    private PurchaseButton btnUranium;
    private PurchaseButton btnElectricity;
    private PurchaseButton btnConcrete;
    private PurchaseButton btnLumber;
    private PurchaseButton btnFood;
    private PurchaseButton btnFuel;
    private PurchaseButton btnSteel;
    private PurchaseButton btnConstructionSupplies;
    private PurchaseButton btnMachinery;
    private PurchaseButton btnElectronics;
    private PurchaseButton btnMedicine;
    private PurchaseButton btnFissile;
    private TextView btnCancel;

    public BuildDistributorView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_build_distributor, this);

        btnIron = findViewById(R.id.btnIron);
        btnCoal = findViewById(R.id.btnCoal);
        btnOil = findViewById(R.id.btnOil);
        btnCrops = findViewById(R.id.btnCrops);
        btnUranium = findViewById(R.id.btnUranium);
        btnElectricity = findViewById(R.id.btnElectricity);
        btnConcrete = findViewById(R.id.btnConcrete);
        btnLumber = findViewById(R.id.btnLumber);
        btnFood = findViewById(R.id.btnFood);
        btnFuel = findViewById(R.id.btnFuel);
        btnSteel = findViewById(R.id.btnSteel);
        btnConstructionSupplies = findViewById(R.id.btnConstructionSupplies);
        btnMachinery = findViewById(R.id.btnMachinery);
        btnElectronics = findViewById(R.id.btnElectronics);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnFissile = findViewById(R.id.btnFissile);
        btnCancel = findViewById(R.id.btnCancel);

        btnIron.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.IRON, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnCoal.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.COAL, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnOil.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.OIL, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnCrops.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CROPS, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnUranium.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.URANIUM, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnElectricity.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ELECTRICITY, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnConcrete.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CONCRETE, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnLumber.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.LUMBER, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnFood.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.FOOD, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnFuel.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.FUEL, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnSteel.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.STEEL, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnConstructionSupplies.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CONSTRUCTION_SUPPLIES, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnMachinery.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.MACHINERY, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnElectronics.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ELECTRONICS, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnMedicine.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.MEDICINE, Defs.DISTRIBUTOR_STRUCTURE_COST);
        btnFissile.SetUnit(game, activity,  game.GetOurPlayer().GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ENRICHED_URANIUM, Defs.DISTRIBUTOR_STRUCTURE_COST);

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new BuildViewNew(game, activity));
            }
        });
    }

    public void ConfirmPurchase(ResourceType type)
    {
        final LaunchDialog launchDialog = new LaunchDialog();
        launchDialog.SetHeaderConstruct();
        launchDialog.SetMessage(context.getString(R.string.construct_confirm, TextUtilities.GetDistributorName(type), TextUtilities.GetCostStatement(Defs.DISTRIBUTOR_STRUCTURE_COST)));
        launchDialog.SetOnClickYes(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                launchDialog.dismiss();
                game.ConstructStructure(EntityType.DISTRIBUTOR, type, LaunchEntity.ID_NONE, null, false);
                activity.ReturnToMainView();
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

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //Building.
                List<Structure> NearbyStructures = game.GetNearbyStructures(game.GetOurPlayer());

                if(!NearbyStructures.isEmpty())
                {
                    activity.SetView(new BuildViewNew(game, activity));
                }
            }
        });
    }
}
