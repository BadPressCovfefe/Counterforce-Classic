package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.entities.ShipView;
import com.apps.fast.launch.launchviews.entities.SubmarineView;
import com.apps.fast.launch.views.CostView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.LaunchablePurchaseSelectionView;
import com.apps.fast.launch.views.LaunchableSelectionView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import launch.game.Config;
import launch.game.LaunchClientGame;
import launch.game.entities.NavalVessel;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.LaunchUtilities;
import launch.utilities.MissileStats;


public class PurchaseTorpedoView extends LaunchView
{
    private enum TorpedoSortOrder
    {
        BLAST_RADIUS,
        SPEED,
        RANGE,
        BUILD_TIME,
        DAMAGE,
        NUCLEAR,
        YIELD,
        HOMING,
    }

    private String[] SortOrderNames;

    private void InitialiseSortTitles()
    {
        SortOrderNames = new String[]
                {
                        context.getString(R.string.sort_name_blast_radius),
                        context.getString(R.string.sort_name_speed),
                        context.getString(R.string.sort_name_range),
                        context.getString(R.string.sort_name_build_time),
                        context.getString(R.string.sort_name_damage),
                        context.getString(R.string.sort_name_nuclear),
                        context.getString(R.string.sort_name_yield),
                        context.getString(R.string.sort_name_homing),
                };
    }
    private Comparator<TorpedoType> blastRadiusComparator;
    private Comparator<TorpedoType> speedComparator;
    private Comparator<TorpedoType> rangeComparator;
    private Comparator<TorpedoType> buildTimeComparator;
    private Comparator<TorpedoType> damageComparator;
    private Comparator<TorpedoType> yieldComparator;
    private TorpedoSortOrder torpedoOrder;
    private LinearLayout lytTypes;
    public PurchaseTorpedoView me = this;
    private boolean bFittedToPlayer;
    private int lFittedToID;
    private int lSlotNumber;
    private Map<Resource.ResourceType, Long> Costs = new ConcurrentHashMap<>();
    private int lTotalCost = 0;
    private List<Integer> Types = new ArrayList<>();
    private LinearLayout btnPurchase;
    private TextView txtCount;
    private CostView lytCosts;
    private ImageView imgExpandContract;
    private ImageView imgLaunchable;
    private LinearLayout lytHeader;
    private SystemType systemType;
    private MissileSystem system;
    private NavalVessel host;
    private TextView btnSortBy;

    private Map<Integer, LaunchablePurchaseSelectionView> LaunchableSelectionViews = new HashMap<>();

    public PurchaseTorpedoView(LaunchClientGame game, MainActivity activity, int lSlotNumber, NavalVessel vessel)
    {
        super(game, activity, true);

        this.lSlotNumber = lSlotNumber;
        this.host = vessel;
        this.lFittedToID = vessel.GetID();

        if(host instanceof Ship)
        {
            system = ((Ship)host).GetTorpedoSystem();
            systemType = SystemType.SHIP_TORPEDOES;

            Setup();
            SetupTorpedoes();
        }
        else if(host instanceof Submarine)
        {
            system = ((Submarine)host).GetTorpedoSystem();
            systemType = SystemType.SUBMARINE_TORPEDO;

            Setup();
            SetupTorpedoes();
        }

        UpdateSelections();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.activity_purchase_launchable, this);

        InitialiseSortTitles();

        lytTypes = findViewById(R.id.lytTypes);
        lytCosts = findViewById(R.id.lytCosts);
        btnPurchase = findViewById(R.id.btnPurchase);
        imgExpandContract = findViewById(R.id.imgExpandContract);
        txtCount = findViewById(R.id.txtCount);
        btnSortBy = findViewById(R.id.btnSortBy);
        lytHeader = findViewById(R.id.lytHeader);
        imgLaunchable = findViewById(R.id.imgLaunchable);

        OnClickListener expandContract = new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(btnPurchase.getVisibility() == GONE)
                {
                    lytCosts.setVisibility(VISIBLE);
                    imgExpandContract.setImageResource(R.drawable.button_expand);
                }
                else if(btnPurchase.getVisibility() == VISIBLE)
                {
                    lytCosts.setVisibility(GONE);
                    imgExpandContract.setImageResource(R.drawable.button_contract);
                }
            }
        };

        imgLaunchable.setOnClickListener(expandContract);
        imgExpandContract.setOnClickListener(expandContract);
        txtCount.setOnClickListener(expandContract);

        torpedoOrder = TorpedoSortOrder.RANGE;

        blastRadiusComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                if(typeOne.GetBlastRadius() < typeTOther.GetBlastRadius())
                    return 1;
                if(typeOne.GetBlastRadius() > typeTOther.GetBlastRadius())
                    return -1;

                return 0;
            }
        };

        speedComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                if(typeOne.GetTorpedoSpeed() < typeTOther.GetTorpedoSpeed())
                    return 1;
                if(typeOne.GetTorpedoSpeed() > typeTOther.GetTorpedoSpeed())
                    return -1;

                return 0;
            }
        };

        rangeComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                if(typeOne.GetTorpedoRange() < typeTOther.GetTorpedoRange())
                    return 1;
                if(typeOne.GetTorpedoRange() > typeTOther.GetTorpedoRange())
                    return -1;

                return 0;
            }
        };

        buildTimeComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                if(MissileStats.GetTorpedoBuildTime(typeOne, false, false) < MissileStats.GetTorpedoBuildTime(typeTOther, false, false))
                    return 1;
                if(MissileStats.GetTorpedoBuildTime(typeOne, false, false) > MissileStats.GetTorpedoBuildTime(typeTOther, false, false))
                    return -1;

                return 0;
            }
        };

        damageComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                if(MissileStats.GetMaxDamage(typeOne) < MissileStats.GetMaxDamage(typeTOther))
                    return 1;
                if(MissileStats.GetMaxDamage(typeOne) > MissileStats.GetMaxDamage(typeTOther))
                    return -1;

                return 0;
            }
        };

        yieldComparator = new Comparator<TorpedoType>()
        {
            @Override
            public int compare(TorpedoType typeOne, TorpedoType typeTOther)
            {
                //If both types are nuclear and typeTOther has greater yield than typeOne or if neither are nuclear and typeTOther has greater yield or if typeTOther is nuclear and typeOne isn't, return 1. The second else if is the reverse scenario.
                if((typeOne.GetNuclear() && typeTOther.GetNuclear() && typeOne.GetYield() < typeTOther.GetYield()) || (!typeOne.GetNuclear() && typeTOther.GetNuclear()) || (!typeOne.GetNuclear() && !typeTOther.GetNuclear() && typeOne.GetYield() < typeTOther.GetYield()))
                    return 1;
                else if((typeOne.GetNuclear() && typeTOther.GetNuclear() && typeOne.GetYield() > typeTOther.GetYield()) || (typeOne.GetNuclear() && !typeTOther.GetNuclear()) || (!typeOne.GetNuclear() && !typeTOther.GetNuclear() && typeOne.GetYield() > typeTOther.GetYield()))
                    return -1;

                return 0;
            }
        };

        btnSortBy.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Display dialog with sort by options.
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.sort_missile_types_by));

                builder.setSingleChoiceItems(SortOrderNames, torpedoOrder.ordinal(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        torpedoOrder = TorpedoSortOrder.values()[i];
                        dialogInterface.dismiss();

                        SetupTorpedoes();
                    }
                });

                builder.show();
            }
        });

        btnPurchase.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.purchase_confirm, TextUtilities.GetCostStatement(Costs)));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        final int[] lTypes = new int[Types.size()];
                        boolean bNukeRespawnCheck = false;

                        for(int i = 0; i < Types.size(); i++)
                        {
                            lTypes[i] = Types.get(i);

                            if(game.GetConfig().GetTorpedoType(lTypes[i]).GetNuclear())
                                bNukeRespawnCheck = true;
                        }

                        game.PurchaseLaunchables(lFittedToID, lSlotNumber, lTypes, systemType);
                        Sounds.PlayEquip();

                        ReturnToParentView();
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
        });
    }

    private void SetupTorpedoes()
    {
        lytTypes.removeAllViews();
        LaunchableSelectionViews.clear();
        imgLaunchable.setImageDrawable(context.getDrawable(R.drawable.marker_torpedo_classic));

        ArrayList<TorpedoType> TorpedoTypes = new ArrayList<>();

        for(TorpedoType type : game.GetConfig().GetTorpedoTypes())
        {
            if(!TorpedoTypes.contains(type) && type.GetPurchasable())
            {
                TorpedoTypes.add(type);
            }
        }

        DrawTorpedoTypesList(TorpedoTypes);
    }

    @Override
    public void Update()
    {

    }

    private void UpdateSelections()
    {
        final Config config = game.GetConfig();

        switch(systemType)
        {
            case SHIP_TORPEDOES:
            {
                system = game.GetShip(lFittedToID).GetTorpedoSystem();
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                system = game.GetSubmarine(lFittedToID).GetTorpedoSystem();
            }
            break;
        }

        final int lFreeSlots = system.GetEmptySlotCount() - Types.size();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run() //This code sets the missile view highlighted red or green for use if the missile is purchaseable.
            {
                for (Map.Entry<Integer, LaunchablePurchaseSelectionView> entry : LaunchableSelectionViews.entrySet())
                {
                    TorpedoType type = config.GetTorpedoType(entry.getKey());
                    LaunchablePurchaseSelectionView view = entry.getValue();

                    if(lFreeSlots == 0)
                    {
                        view.SetDisabled();
                    }
                    else if(!game.GetOurPlayer().GetCargoSystem().ContainsQuantities(type.GetCosts()))
                    {
                        view.SetDisabled();
                    }
                    else
                    {
                        view.SetNotHighlighted();
                    }
                }
            }
        });
    }

    public void AddType(int lType)
    {
        Types.add(lType);

        Map<Resource.ResourceType, Long> TotalCosts = new ConcurrentHashMap<>();
        Config config = game.GetConfig();

        for(Integer lLaunchableType : Types)
        {
            TorpedoType type = config.GetTorpedoType(lLaunchableType);

            LaunchUtilities.AddResourceMapsTogether(TotalCosts, type.GetCosts());
        }

        Costs = TotalCosts;
        lytCosts.SetCosts(Costs, game);
        txtCount.setText(Integer.toString(Types.size()));
        btnPurchase.setVisibility(VISIBLE);
        lytHeader.setVisibility(VISIBLE);

        UpdateSelections();
    }

    private void ReturnToParentView()
    {
        switch(systemType)
        {
            case SHIP_TORPEDOES:
            {
                MainNormalView mainView = new MainNormalView(game, activity);
                activity.SetView(mainView);
                mainView.BottomLayoutShowView(new ShipView(game, activity, (Ship)host));
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                MainNormalView mainView = new MainNormalView(game, activity);
                activity.SetView(mainView);
                mainView.BottomLayoutShowView(new SubmarineView(game, activity, (Submarine)host));
            }
            break;
        }
    }

    private void DrawTorpedoTypesList(ArrayList<TorpedoType> ApplicableTypes)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytTypes.removeAllViews();
                LaunchableSelectionViews.clear();

                ArrayList<TorpedoType> TypesSorted = new ArrayList();

                for(TorpedoType type : ApplicableTypes)
                {
                    TypesSorted.add(type);
                }

                switch(torpedoOrder)
                {
                    case BLAST_RADIUS:
                    {
                        Collections.sort(TypesSorted, blastRadiusComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_blast_radius));
                    }
                    break;

                    case SPEED:
                    {
                        Collections.sort(TypesSorted, speedComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_speed));
                    }
                    break;

                    case RANGE:
                    {
                        Collections.sort(TypesSorted, rangeComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_range));
                    }
                    break;

                    case BUILD_TIME:
                    {
                        Collections.sort(TypesSorted, buildTimeComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_build_time));
                    }
                    break;

                    case DAMAGE:
                    {
                        Collections.sort(TypesSorted, damageComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_damage));
                    }
                    break;

                    case NUCLEAR:
                    {
                        for(TorpedoType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetNuclear())
                            {
                                TypesSorted.remove(type);
                            }
                        }
                        btnSortBy.setText(context.getString(R.string.sort_name_nuclear));
                    }
                    break;

                    case HOMING:
                    {
                        for(TorpedoType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetHoming())
                            {
                                TypesSorted.remove(type);
                            }
                        }
                        btnSortBy.setText(context.getString(R.string.sort_name_homing));
                    }
                    break;

                    case YIELD:
                    {
                        Collections.sort(TypesSorted, yieldComparator);
                        btnSortBy.setText(context.getString(R.string.sort_name_yield));
                    }
                    break;
                }

                for(final TorpedoType type : TypesSorted)
                {
                    final LaunchablePurchaseSelectionView launchableSelectionView = new LaunchablePurchaseSelectionView(activity, game, type, host, me);
                    lytTypes.addView(launchableSelectionView);
                    LaunchableSelectionViews.put(type.GetID(), launchableSelectionView);
                }
            }
        });
    }

    public MissileSystem GetSystem() { return system; }

    public List<Integer> GetTypes() { return Types; }
}
