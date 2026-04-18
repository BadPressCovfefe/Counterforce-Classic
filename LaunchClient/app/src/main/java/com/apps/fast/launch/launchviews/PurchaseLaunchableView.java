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
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.entities.AirbaseView;
import com.apps.fast.launch.launchviews.entities.ShipView;
import com.apps.fast.launch.launchviews.entities.SubmarineView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.LaunchablePurchaseSelectionView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;
import launch.game.systems.LaunchSystem.SystemType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 16/10/15.
 */
public class PurchaseLaunchableView extends LaunchView
{
    private enum MissileSortOrder
    {
        BLAST_RADIUS,
        SPEED,
        RANGE,
        BUILD_TIME,
        DAMAGE,
        NUCLEAR,
        EMP,
        ECM,
        STEALTH,
        ACCURACY,
        YIELD,
    }

    private enum InterceptorSortOrder
    {
        RANGE,
        SPEED,
        NUCLEAR,
    }

    private String[] SortOrderNames;

    private void InitialiseSortTitles()
    {
        if(bMissiles)
        {
            SortOrderNames = new String[]
                    {
                            context.getString(R.string.sort_name_blast_radius),
                            context.getString(R.string.sort_name_speed),
                            context.getString(R.string.sort_name_range),
                            context.getString(R.string.sort_name_build_time),
                            context.getString(R.string.sort_name_damage),
                            context.getString(R.string.sort_name_nuclear),
                            context.getString(R.string.sort_name_emp),
                            context.getString(R.string.sort_name_ecm),
                            context.getString(R.string.sort_name_stealth),
                            context.getString(R.string.sort_name_accuracy),
                            context.getString(R.string.sort_name_yield),
                    };
        }
        else
        {
            SortOrderNames = new String[]
                    {
                            context.getString(R.string.sort_name_range),
                            context.getString(R.string.sort_name_speed),
                            context.getString(R.string.sort_name_nuclear),
                    };
        }

    }

    private enum LaunchableType
    {
        MISSILE,
        ICBM,
        ALM,                //Air-Launched Missiles
        HLM,                //Helicopter-Launched Missiles
        INTERCEPTOR,
        ABM,                //Anti-Ballistic Missiles
        TLM,                //Tank-launched missiles
        SLBM,               //Submarine-Launched Ballistic Missiles
        SLM,                //Ship-Launched Missiles
        TORPEDO,
        AIM,
    }

    private LaunchableType listType;

    private Comparator<MissileType> blastRadiusComparator;
    private Comparator<MissileType> speedComparator;
    private Comparator<MissileType> rangeComparator;
    private Comparator<MissileType> buildTimeComparator;
    private Comparator<MissileType> damageComparator;
    private Comparator<MissileType> cepComparator;
    private Comparator<MissileType> yieldComparator;
    private Comparator<InterceptorType> intRangeComparator;
    private Comparator<InterceptorType> intSpeedComparator;
    private MissileSortOrder missileOrder;
    private InterceptorSortOrder interceptorOrder;
    private LinearLayout lytTypes;
    private long oCost = 0;
    private PurchaseLaunchableView me = this;
    private boolean bMissiles = false;
    private int lFittedToID;
    private int lSlotNumber;
    private List<Integer> Types = new ArrayList<>();
    private LinearLayout btnPurchase;
    private TextView txtCount;
    private TextView txtTotalCost;
    private SystemType systemType;
    private MissileSystem system;
    private LaunchEntity host;
    private TextView btnSortBy;
    private ImageView imgInterceptor;
    private ImageView imgMissile;

    private Map<Integer, LaunchablePurchaseSelectionView> LaunchableSelectionViews = new HashMap<>();

    public PurchaseLaunchableView(LaunchClientGame game, MainActivity activity, LaunchEntity host, SystemType systemType, int lSlotNumber)
    {
        super(game, activity, true);

        this.lSlotNumber = lSlotNumber;
        this.host = host;
        this.lFittedToID = host.GetID();
        this.systemType = systemType;

        switch(systemType)
        {
            case MISSILE_SITE:
            {
                MissileSite site = (MissileSite)host;
                system = site.GetMissileSystem();
                bMissiles = true;

                if(site.CanTakeICBM())
                {
                    listType = LaunchableType.ICBM;
                }
                else
                {
                    listType = LaunchableType.MISSILE;
                }

                Setup();
                SetupMissiles();
            }
            break;

            case STORED_AIRCRAFT_MISSILES:
            case AIRCRAFT_MISSILES:
            {
                AirplaneInterface aircraft = (AirplaneInterface)host;

                system = aircraft.GetMissileSystem();
                bMissiles = true;

                Setup();
                SetupMissiles();
                listType = LaunchableType.ALM;
            }
            break;

            case SHIP_MISSILES:
            {
                system = ((Ship)host).GetMissileSystem();

                bMissiles = true;
                Setup();
                SetupMissiles();
                listType = LaunchableType.MISSILE;
            }
            break;

            case SUBMARINE_MISSILES:
            {
                system = ((Submarine)host).GetMissileSystem();
                bMissiles = true;

                Setup();
                SetupMissiles();
                listType = LaunchableType.MISSILE;
            }
            break;

            case SUBMARINE_ICBM:
            {
                system = ((Submarine)host).GetICBMSystem();
                bMissiles = true;

                Setup();
                SetupMissiles();
                listType = LaunchableType.ICBM;
            }
            break;

            case SAM_SITE:
            {
                SAMSite site = (SAMSite) host;
                system = site.GetInterceptorSystem();

                if(site.GetIsABMSilo())
                {
                    listType = LaunchableType.ABM;
                }
                else
                {

                    listType = LaunchableType.INTERCEPTOR;
                }

                Setup();
                SetupInterceptors();
            }
            break;

            case AIRCRAFT_INTERCEPTORS:
            case STORED_AIRCRAFT_INTERCEPTORS:
            {
                AirplaneInterface aircraft = (AirplaneInterface)host;
                system = aircraft.GetInterceptorSystem();

                Setup();
                SetupInterceptors();
                listType = LaunchableType.AIM;
            }
            break;

            case SHIP_INTERCEPTORS:
            {
                system = ((Ship)host).GetInterceptorSystem();

                Setup();
                SetupInterceptors();
                listType = LaunchableType.INTERCEPTOR;
            }
            break;
        }

        UpdateSelections();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.activity_purchase_launchable, this);

        InitialiseSortTitles();

        lytTypes = findViewById(R.id.lytTypes);
        btnPurchase = findViewById(R.id.btnPurchase);
        txtTotalCost = findViewById(R.id.txtTotalCost);
        imgInterceptor = findViewById(R.id.imgInterceptor);
        imgMissile = findViewById(R.id.imgMissile);
        txtCount = findViewById(R.id.txtCount);
        btnSortBy = findViewById(R.id.btnSortBy);

        if(bMissiles)
            missileOrder = MissileSortOrder.RANGE;
        else
            interceptorOrder = InterceptorSortOrder.SPEED;

        blastRadiusComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(MissileStats.GetBlastRadius(typeOne, false) < MissileStats.GetBlastRadius(typeTOther, false))
                    return 1;
                if(MissileStats.GetBlastRadius(typeOne, false) > MissileStats.GetBlastRadius(typeTOther, false))
                    return -1;

                return 0;
            }
        };

        speedComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(typeOne.GetMissileSpeed() < typeTOther.GetMissileSpeed())
                    return 1;
                if(typeOne.GetMissileSpeed() > typeTOther.GetMissileSpeed())
                    return -1;

                return 0;
            }
        };

        rangeComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(typeOne.GetMissileRange() < typeTOther.GetMissileRange())
                    return 1;
                if(typeOne.GetMissileRange() > typeTOther.GetMissileRange())
                    return -1;

                return 0;
            }
        };

        buildTimeComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(MissileStats.GetMissileBuildTime(typeOne, false, false) < MissileStats.GetMissileBuildTime(typeTOther, false, false))
                    return 1;
                if(MissileStats.GetMissileBuildTime(typeOne, false, false) > MissileStats.GetMissileBuildTime(typeTOther, false, false))
                    return -1;

                return 0;
            }
        };

        damageComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(MissileStats.GetMaxDamage(typeOne) < MissileStats.GetMaxDamage(typeTOther))
                    return 1;
                if(MissileStats.GetMaxDamage(typeOne) > MissileStats.GetMaxDamage(typeTOther))
                    return -1;

                return 0;
            }
        };

        cepComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                if(typeOne.GetAccuracy() > typeTOther.GetAccuracy())
                    return 1;
                if(typeOne.GetAccuracy() < typeTOther.GetAccuracy())
                    return -1;

                return 0;
            }
        };

        yieldComparator = new Comparator<MissileType>()
        {
            @Override
            public int compare(MissileType typeOne, MissileType typeTOther)
            {
                //If both types are nuclear and typeTOther has greater yield than typeOne or if neither are nuclear and typeTOther has greater yield or if typeTOther is nuclear and typeOne isn't, return 1. The second else if is the reverse scenario.
                if((typeOne.GetNuclear() && typeTOther.GetNuclear() && typeOne.GetYield() < typeTOther.GetYield()) || (!typeOne.GetNuclear() && typeTOther.GetNuclear()) || (!typeOne.GetNuclear() && !typeTOther.GetNuclear() && typeOne.GetYield() < typeTOther.GetYield()))
                    return 1;
                else if((typeOne.GetNuclear() && typeTOther.GetNuclear() && typeOne.GetYield() > typeTOther.GetYield()) || (typeOne.GetNuclear() && !typeTOther.GetNuclear()) || (!typeOne.GetNuclear() && !typeTOther.GetNuclear() && typeOne.GetYield() > typeTOther.GetYield()))
                    return -1;

                return 0;
            }
        };

        intSpeedComparator = new Comparator<InterceptorType>()
        {
            @Override
            public int compare(InterceptorType typeOne, InterceptorType typeTOther)
            {
                if(typeOne.GetInterceptorSpeed() < typeTOther.GetInterceptorSpeed())
                    return 1;
                if(typeOne.GetInterceptorSpeed() > typeTOther.GetInterceptorSpeed())
                    return -1;

                return 0;
            }
        };

        intRangeComparator = new Comparator<InterceptorType>()
        {
            @Override
            public int compare(InterceptorType typeOne, InterceptorType typeTOther)
            {
                if(typeOne.GetInterceptorRange() < typeTOther.GetInterceptorRange())
                    return 1;
                if(typeOne.GetInterceptorRange() > typeTOther.GetInterceptorRange())
                    return -1;

                return 0;
            }
        };

        if(btnSortBy != null)
        {
            btnSortBy.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //Display dialog with sort by options.
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.sort_missile_types_by));

                    builder.setSingleChoiceItems(SortOrderNames, bMissiles ? missileOrder.ordinal() : interceptorOrder.ordinal(), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if(bMissiles)
                            {
                                missileOrder = MissileSortOrder.values()[i];
                                dialogInterface.dismiss();

                                SetupMissiles();
                            }
                            else
                            {
                                interceptorOrder = InterceptorSortOrder.values()[i];
                                dialogInterface.dismiss();

                                SetupInterceptors();
                            }
                        }
                    });

                    builder.show();
                }
            });
        }

        btnPurchase.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.purchase_confirm, TextUtilities.GetCurrencyString(oCost)));
                launchDialog.SetOnClickYes(new View.OnClickListener()
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

                            if(bMissiles)
                            {
                                if(game.GetConfig().GetMissileType(lTypes[i]).GetNuclear())
                                    bNukeRespawnCheck = true;
                            }
                        }

                        game.PurchaseLaunchables(lFittedToID, lSlotNumber, lTypes, systemType);
                        Sounds.PlayEquip();

                        ReturnToParentView();
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

    private void SetupMissiles()
    {
        lytTypes.removeAllViews();
        LaunchableSelectionViews.clear();
        imgMissile.setVisibility(VISIBLE);

        ArrayList<MissileType> MissileTypes = new ArrayList<>();

        for(MissileType type : game.GetConfig().GetMissileTypes())
        {
            if(!MissileTypes.contains(type) && TypeIsAppropriate(type) && type.GetPurchasable())
            {
                MissileTypes.add(type);
            }
        }

        DrawMissileTypesList(MissileTypes);
    }

    private void SetupInterceptors()
    {
        lytTypes.removeAllViews();
        LaunchableSelectionViews.clear();
        imgInterceptor.setVisibility(VISIBLE);

        ArrayList<InterceptorType> InterceptorTypes = new ArrayList<>();

        for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
        {
            if(!InterceptorTypes.contains(type) && TypeIsAppropriate(type) && type.GetPurchasable())
            {
                InterceptorTypes.add(type);
            }
        }

        DrawInterceptorTypesList(InterceptorTypes);
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
            case MISSILE_SITE:
            {
                MissileSite site = game.GetMissileSite(lFittedToID);
                system = site.GetMissileSystem();
            }
            break;

            case SAM_SITE:
            {
                SAMSite site = game.GetSAMSite(lFittedToID);
                system = site.GetInterceptorSystem();
            }
            break;

            case STORED_AIRCRAFT_MISSILES:
            case AIRCRAFT_MISSILES:
            {
                system = game.GetAirplaneInterface(lFittedToID).GetMissileSystem();
            }
            break;

            case STORED_AIRCRAFT_INTERCEPTORS:
            case AIRCRAFT_INTERCEPTORS:
            {
                system = game.GetAirplaneInterface(lFittedToID).GetInterceptorSystem();
            }
            break;

            case SHIP_ARTILLERY:
            {
                system = game.GetShip(lFittedToID).GetArtillerySystem();
            }
            break;

            case TANK_INTERCEPTORS:
            case TANK_MISSILES:
            case TANK_ARTILLERY:
            {
                system = game.GetTankInterface(lFittedToID).GetMissileSystem();
            }
            break;

            case ARTILLERY_GUN:
            {
                system = game.GetArtilleryGun(lFittedToID).GetMissileSystem();
            }
            break;
        }

        final int lFreeSlots = system.GetEmptySlotCount() - Types.size();

        if(bMissiles)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run() //This code sets the missile view highlighted red or green for use if the missile is purchaseable.
                {
                    for(Map.Entry<Integer, LaunchablePurchaseSelectionView> entry : LaunchableSelectionViews.entrySet())
                    {
                        MissileType type = config.GetMissileType(entry.getKey());
                        LaunchablePurchaseSelectionView view = entry.getValue();

                        if(lFreeSlots == 0)
                        {
                            view.SetDisabled();
                        }
                        else if(game.GetOurPlayer().GetWealth() < type.GetCost())
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
        else
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    for(Map.Entry<Integer, LaunchablePurchaseSelectionView> entry : LaunchableSelectionViews.entrySet())
                    {
                        InterceptorType type = config.GetInterceptorType(entry.getKey());
                        LaunchablePurchaseSelectionView view = entry.getValue();

                        if (lFreeSlots == 0)
                        {
                            view.SetDisabled();
                        }
                        else if(game.GetOurPlayer().GetWealth() < type.GetCost())
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
    }

    public void AddType(int lType)
    {
        Types.add(lType);

        long oTotalCost = 0;
        Config config = game.GetConfig();

        for(Integer lLaunchableType : Types)
        {
            if(bMissiles)
            {
                MissileType type = config.GetMissileType(lLaunchableType);

                oTotalCost += type.GetCost();
            }
            else
            {
                InterceptorType type = config.GetInterceptorType(lLaunchableType);

                oTotalCost += type.GetCost();
            }
        }

        oCost = oTotalCost;
        txtTotalCost.setText(TextUtilities.GetCurrencyString(oCost));
        txtTotalCost.setTextColor(Utilities.ColourFromAttr(activity, game.GetOurPlayer().GetWealth() >= oCost ? R.attr.GoodColour : R.attr.BadColour));
        txtCount.setText(Integer.toString(Types.size()));
        txtCount.setVisibility(VISIBLE);
        btnPurchase.setVisibility(VISIBLE);

        UpdateSelections();
    }

    private void ReturnToParentView()
    {
        switch(systemType)
        {
            case STORED_AIRCRAFT_MISSILES:
            case STORED_AIRCRAFT_INTERCEPTORS:
            {
                MainNormalView mainView = new MainNormalView(game, activity);
                activity.SetView(mainView);

                MapEntity hostBase = null;

                if(host instanceof AirplaneInterface)
                {
                    if(((AirplaneInterface)host).GetHomeBase() != null)
                        hostBase = ((AirplaneInterface)host).GetHomeBase().GetMapEntity(game);
                }

                if(hostBase != null)
                {
                    if(hostBase instanceof Ship)
                        mainView.BottomLayoutShowView(new ShipView(game, activity, (Ship)hostBase));
                    else if(hostBase instanceof Airbase)
                        mainView.BottomLayoutShowView(new AirbaseView(game, activity, (Airbase)hostBase));
                }
            }
            break;

            case SUBMARINE_ICBM:
            case SUBMARINE_MISSILES:
            {
                MainNormalView mainView = new MainNormalView(game, activity);
                activity.SetView(mainView);
                mainView.BottomLayoutShowView(new SubmarineView(game, activity, (Submarine)host));
            }
            break;

            case SAM_SITE:
            case MISSILE_SITE:
            case AIRCRAFT_MISSILES:
            case AIRCRAFT_INTERCEPTORS:
            case ARTILLERY_GUN:
            case SHIP_ARTILLERY:
            case TANK_INTERCEPTORS:
            case TANK_MISSILES:
            case TANK_ARTILLERY:
            {
                activity.ReturnToMainView();
            }
            break;
        }
    }

    private void DrawMissileTypesList(ArrayList<MissileType> ApplicableTypes)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytTypes.removeAllViews();
                LaunchableSelectionViews.clear();

                ArrayList<MissileType> TypesSorted = new ArrayList();

                for(MissileType type : ApplicableTypes)
                {
                    if(TypeIsAppropriate(type))
                        TypesSorted.add(type);
                }

                switch(missileOrder)
                {
                    case BLAST_RADIUS:
                    {
                        Collections.sort(TypesSorted, blastRadiusComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_blast_radius));
                    }
                    break;

                    case SPEED:
                    {
                        Collections.sort(TypesSorted, speedComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_speed));
                    }
                    break;

                    case RANGE:
                    {
                        Collections.sort(TypesSorted, rangeComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_range));
                    }
                    break;

                    case BUILD_TIME:
                    {
                        Collections.sort(TypesSorted, buildTimeComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_build_time));
                    }
                    break;

                    case DAMAGE:
                    {
                        Collections.sort(TypesSorted, damageComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_damage));
                    }
                    break;

                    case NUCLEAR:
                    {
                        for(MissileType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetNuclear())
                            {
                                TypesSorted.remove(type);
                            }
                        }

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_nuclear));
                    }
                    break;

                    case EMP:
                    {
                        for(MissileType type : new ArrayList<>(TypesSorted))
                        {
                            if(MissileStats.GetMissileEMPRadius(type, true) <= 0)
                            {
                                TypesSorted.remove(type);
                            }
                        }

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_emp));
                    }
                    break;

                    case ECM:
                    {
                        for(MissileType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetECM())
                            {
                                TypesSorted.remove(type);
                            }
                        }

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_ecm));
                    }
                    break;

                    case STEALTH:
                    {
                        for(MissileType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetStealth())
                            {
                                TypesSorted.remove(type);
                            }
                        }

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_stealth));
                    }
                    break;

                    case ACCURACY:
                    {
                        Collections.sort(TypesSorted, cepComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_accuracy));
                    }
                    break;

                    case YIELD:
                    {
                        Collections.sort(TypesSorted, yieldComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_yield));
                    }
                    break;
                }

                for(final MissileType type : TypesSorted)
                {
                    final LaunchablePurchaseSelectionView launchablePurchaseSelectionView = new LaunchablePurchaseSelectionView(activity, game, type, host, me);
                    lytTypes.addView(launchablePurchaseSelectionView);
                    LaunchableSelectionViews.put(type.GetID(), launchablePurchaseSelectionView);

                    launchablePurchaseSelectionView.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            int lFreeSlots = GetSystem().GetEmptySlotCount() - GetTypes().size();

                            if(lFreeSlots == 0)
                            {
                                activity.ShowBasicOKDialog(activity.getString(R.string.insufficient_slots));
                            }
                            else if(game.GetOurPlayer().GetWealth() < type.GetCost())
                            {
                                activity.ShowBasicOKDialog(activity.getString(R.string.insufficient_wealth));
                            }
                            else
                            {
                                launchablePurchaseSelectionView.IncrementNumber();
                                AddType(type.GetID());
                            }
                        }
                    });

                    launchablePurchaseSelectionView.setOnTouchListener(new View.OnTouchListener()
                    {
                        Handler hand=new Handler();
                        Runnable run=new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                int lFreeSlots = GetSystem().GetEmptySlotCount() - GetTypes().size();

                                if(lFreeSlots > 0 && game.GetOurPlayer().GetWealth() >= type.GetCost())
                                {
                                    launchablePurchaseSelectionView.IncrementNumber();
                                    AddType(type.GetID());
                                    //Increment every 100ms
                                    hand.postDelayed(this, 80);
                                }
                            }
                        };

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent)
                        {
                            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                            {
                                hand.postDelayed(run, 500);
                            }

                            if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP)
                            {
                                hand.removeCallbacks(run);
                            }

                            return false;
                        }
                    });
                }
            }
        });
    }

    private void DrawInterceptorTypesList(ArrayList<InterceptorType> ApplicableTypes)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytTypes.removeAllViews();
                LaunchableSelectionViews.clear();

                ArrayList<InterceptorType> TypesSorted = new ArrayList();

                for(InterceptorType type : ApplicableTypes)
                {
                    if(TypeIsAppropriate(type))
                        TypesSorted.add(type);
                }

                switch(interceptorOrder)
                {
                    case SPEED:
                    {
                        Collections.sort(TypesSorted, intSpeedComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_speed));
                    }
                    break;

                    case RANGE:
                    {
                        Collections.sort(TypesSorted, intRangeComparator);

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_range));
                    }
                    break;

                    case NUCLEAR:
                    {
                        for(InterceptorType type : new ArrayList<>(TypesSorted))
                        {
                            if(!type.GetNuclear())
                            {
                                TypesSorted.remove(type);
                            }
                        }

                        if(btnSortBy != null)
                            btnSortBy.setText(context.getString(R.string.sort_name_nuclear));
                    }
                    break;
                }

                for(final InterceptorType type : TypesSorted)
                {
                    final LaunchablePurchaseSelectionView launchablePurchaseSelectionView = new LaunchablePurchaseSelectionView(activity, game, type, host, me);
                    lytTypes.addView(launchablePurchaseSelectionView);
                    LaunchableSelectionViews.put(type.GetID(), launchablePurchaseSelectionView);

                    launchablePurchaseSelectionView.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            int lFreeSlots = GetSystem().GetEmptySlotCount() - GetTypes().size();

                            if(lFreeSlots == 0)
                            {
                                activity.ShowBasicOKDialog(activity.getString(R.string.insufficient_slots));
                            }
                            else if(game.GetOurPlayer().GetWealth() < type.GetCost())
                            {
                                activity.ShowBasicOKDialog(activity.getString(R.string.insufficient_wealth));
                            }
                            else
                            {
                                launchablePurchaseSelectionView.IncrementNumber();
                                AddType(type.GetID());
                            }
                        }
                    });

                    launchablePurchaseSelectionView.setOnTouchListener(new View.OnTouchListener()
                    {
                        Handler hand=new Handler();
                        Runnable run=new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                int lFreeSlots = GetSystem().GetEmptySlotCount() - GetTypes().size();

                                if(lFreeSlots > 0 && game.GetOurPlayer().GetWealth() >= type.GetCost())
                                {
                                    launchablePurchaseSelectionView.IncrementNumber();
                                    AddType(type.GetID());
                                    //Increment every 100ms
                                    hand.postDelayed(this, 80);
                                }
                            }
                        };

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent)
                        {
                            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                            {
                                hand.postDelayed(run, 500);
                            }

                            if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP)
                            {
                                hand.removeCallbacks(run);
                            }

                            return false;
                        }
                    });
                }
            }
        });
    }

    private boolean TypeIsAppropriate(LaunchType type)
    {
        if(type instanceof MissileType)
        {
            MissileType missileType = (MissileType)type;

            switch(systemType)
            {
                case MISSILE_SITE: return !missileType.GetArtillery() && ((!missileType.GetSubmarineLaunched() && game.GetMissileSite(lFittedToID).CanTakeICBM() && missileType.GetICBM()) || (!game.GetMissileSite(lFittedToID).CanTakeICBM() && !missileType.GetICBM() && missileType.GetGroundLaunched()));
                case AIRCRAFT_MISSILES:
                case STORED_AIRCRAFT_MISSILES: return missileType.GetAirLaunched();
                case SHIP_MISSILES: return missileType.GetShipLaunched() && !missileType.GetICBM() && !missileType.GetArtillery();
                case SHIP_ARTILLERY: return missileType.GetShipLaunched() && missileType.GetArtillery();
                case SUBMARINE_ICBM: return missileType.GetSubmarineLaunched() && missileType.GetICBM();
                case SUBMARINE_MISSILES: return missileType.GetSubmarineLaunched() && !missileType.GetICBM();
                case TANK_ARTILLERY:
                case ARTILLERY_GUN: return missileType.GetArtillery() && !missileType.GetShipLaunched();
                case TANK_MISSILES: return missileType.GetTankLaunched();
            }
        }
        else if(type instanceof InterceptorType)
        {
            InterceptorType interceptorType = (InterceptorType)type;

            switch(systemType)
            {
                case SAM_SITE: return !interceptorType.GetAirLaunched() && ((game.GetSAMSite(lFittedToID).GetIsABMSilo() && interceptorType.GetABM()) || (!game.GetSAMSite(lFittedToID).GetIsABMSilo() && !interceptorType.GetABM()));
                case AIRCRAFT_INTERCEPTORS:
                case STORED_AIRCRAFT_INTERCEPTORS: return interceptorType.GetAirLaunched();
                case SHIP_INTERCEPTORS: return interceptorType.GetShipLaunched();
                case TANK_INTERCEPTORS: return interceptorType.GetTankLaunched();
            }
        }

        return false;
    }

    public MissileSystem GetSystem() { return system; }

    public List<Integer> GetTypes() { return Types; }
}
