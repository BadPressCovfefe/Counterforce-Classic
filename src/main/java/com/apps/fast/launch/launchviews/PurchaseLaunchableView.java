package com.apps.fast.launch.launchviews;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.LaunchableSelectionView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.LaunchClientGame;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 16/10/15.
 */
public class PurchaseLaunchableView extends LaunchView
{
    private LinearLayout lytTypes;

    private boolean bMissiles;
    private boolean bFittedToPlayer;
    private int lFittedToStructureID;
    private byte cSlotNumber;

    private int lTotalCost = 0;

    private List<Integer> Types = new ArrayList<>();
    private LinearLayout btnPurchase;
    private TextView txtCount;
    private TextView txtTotalCost;

    private Map<Integer, LaunchableSelectionView> LaunchableSelectionViews = new HashMap<>();

    //For a missile site.
    public PurchaseLaunchableView(LaunchClientGame game, MainActivity activity, MissileSite site, byte cSlotNumber)
    {
        super(game, activity, true);

        lFittedToStructureID = site.GetID();
        this.cSlotNumber = cSlotNumber;

        bMissiles = true;
        bFittedToPlayer = false;
        Setup();
        if(site.CanTakeICBM())
        {
            SetupICBMs();
        }
        else
        {
            SetupMissiles();
        }
        UpdateSelections();
    }
    //For a SAM site.
    public PurchaseLaunchableView(LaunchClientGame game, MainActivity activity, SAMSite site, byte cSlotNumber)
    {
        super(game, activity, true);

        lFittedToStructureID = site.GetID();
        this.cSlotNumber = cSlotNumber;

        bMissiles = false;
        bFittedToPlayer = false;
        Setup();
        if(site.GetIsABMSilo())
        {
            SetupABMs();
        }
        else
        {
            SetupInterceptors();
        }
        UpdateSelections();
    }

    //For a player system.
    public PurchaseLaunchableView(LaunchClientGame game, MainActivity activity, boolean bMissiles, byte cSlotNumber)
    {
        super(game, activity, true);

        this.cSlotNumber = cSlotNumber;

        this.bMissiles = bMissiles;
        bFittedToPlayer = true;
        Setup();
        if(bMissiles)
            SetupMissiles();
        else
            SetupInterceptors();

        UpdateSelections();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.activity_purchase_launchable, this);

        lytTypes = (LinearLayout)findViewById(R.id.lytTypes);

        btnPurchase = (LinearLayout)findViewById(R.id.btnPurchase);
        txtCount = (TextView)findViewById(R.id.txtCount);
        txtTotalCost = (TextView)findViewById(R.id.txtTotalCost);

        btnPurchase.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.purchase_confirm, TextUtilities.GetCurrencyString(lTotalCost)));
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

                        if(bFittedToPlayer)
                        {
                            if(bMissiles)
                                game.PurchaseMissilesPlayer(cSlotNumber, lTypes);
                            else
                                game.PurchaseInterceptorsPlayer(cSlotNumber, lTypes);
                        }
                        else
                        {
                            if(bMissiles)
                            {
                                /*if(bNukeRespawnCheck && game.GetOurPlayer().GetRespawnProtected())
                                {
                                    final LaunchDialog launchDialogNuke = new LaunchDialog();
                                    launchDialogNuke.SetHeaderLaunch();
                                    launchDialogNuke.SetMessage(context.getString(R.string.nuke_respawn_protected_you, TextUtilities.GetTimeAmount(game.GetOurPlayer().GetStateTimeRemaining())));
                                    launchDialogNuke.SetOnClickYes(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            launchDialogNuke.dismiss();
                                            game.PurchaseMissiles(lFittedToStructureID, cSlotNumber, lTypes);
                                        }
                                    });
                                    launchDialogNuke.SetOnClickNo(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            launchDialogNuke.dismiss();
                                        }
                                    });
                                    launchDialogNuke.show(activity.getFragmentManager(), "");
                                }
                                else
                                {*/
                                    game.PurchaseMissiles(lFittedToStructureID, cSlotNumber, lTypes);
                                //}
                            }
                            else
                                game.PurchaseInterceptors(lFittedToStructureID, cSlotNumber, lTypes);
                        }

                        //TODO: Improve? Currently sets the first pressed type as the preferred missile.
                        /*if(bMissiles)
                            ClientDefs.SetMissilePreferred(lTypes[0]);
                        else
                            ClientDefs.SetInterceptorPreferred(lTypes[0]);*/

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
        findViewById(R.id.imgInterceptor).setVisibility(GONE);

        //Put up to three preferred ones at the top.
        //List<Integer> MissilePreferences = ClientDefs.GetMissilePreferredOrder(context);
        ArrayList<MissileType> MissileTypes = new ArrayList<>();
        /*for(Integer lPreferredMissile : MissilePreferences)
        {
            MissileType type = game.GetConfig().GetMissileType(lPreferredMissile);

            if(type != null)
            {
                if(!type.GetICBM() && game.GetPurchaseable(type))
                {
                    MissileTypes.add(type);
                }
            }
        }*/

        for(MissileType type : game.GetConfig().GetMissileTypes())
        {
            if(!MissileTypes.contains(type) && !type.GetICBM() && type.GetPurchasable())
            {
                MissileTypes.add(type);
            }
        }

        for(final MissileType type : MissileTypes)
        {
            final LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type);

            launchableSelectionView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    boolean bMissileSiteWithoutICBM = false;

                    if(!bFittedToPlayer)
                    {
                        bMissileSiteWithoutICBM = !game.GetMissileSite(lFittedToStructureID).CanTakeICBM();
                    }

                    if(type.GetNuclear() && bFittedToPlayer)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.players_cant_carry_nukes));
                    }
                    else if(type.GetICBM() && bFittedToPlayer)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.players_cant_carry_ICBM));
                    }
                    else if(type.GetICBM() && bMissileSiteWithoutICBM)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.site_not_ICBM));
                    }
                    else
                    {
                        MissileSystem system = bFittedToPlayer ? game.GetOurPlayer().GetMissileSystem() : game.GetMissileSite(lFittedToStructureID).GetMissileSystem();
                        int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                        int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                        if(lFreeSlots == 0)
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_slots));
                        }
                        else if(lAvailableFunds < game.GetConfig().GetMissileCost(type))
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                        }
                        else
                        {
                            launchableSelectionView.IncrementNumber();
                            AddType(type.GetID());
                        }
                    }
                }
            });

            launchableSelectionView.setOnTouchListener(new View.OnTouchListener()
            {
                Handler hand=new Handler();
                Runnable run=new Runnable()
                {
                    @Override
                    public void run()
                    {
                        MissileSystem system = bFittedToPlayer ? game.GetOurPlayer().GetMissileSystem() : game.GetMissileSite(lFittedToStructureID).GetMissileSystem();
                        int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                        int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                        if(lFreeSlots > 0 && lAvailableFunds >= game.GetConfig().GetMissileCost(type))
                        {
                            launchableSelectionView.IncrementNumber();
                            AddType(type.GetID());
                            //Increment every 100ms
                            hand.postDelayed(this, 80);
                        }
                    }
                };

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
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

            lytTypes.addView(launchableSelectionView);
            LaunchableSelectionViews.put(type.GetID(), launchableSelectionView);
        }
    }

    private void SetupICBMs()
    {
        findViewById(R.id.imgInterceptor).setVisibility(GONE);

        //Put up to three preferred ones at the top.
        //List<Integer> MissilePreferences = ClientDefs.GetMissilePreferredOrder(context);
        ArrayList<MissileType> MissileTypes = new ArrayList<>();
        /*for(Integer lPreferredMissile : MissilePreferences)
        {
            MissileType type = game.GetConfig().GetMissileType(lPreferredMissile);

            if(type != null)
            {
                if(type.GetICBM() && game.GetPurchaseable(type))
                {
                    MissileTypes.add(type);
                }
            }
        }*/

        for(MissileType type : game.GetConfig().GetMissileTypes())
        {
            if(!MissileTypes.contains(type) && type.GetICBM() && type.GetPurchasable())
            {
                MissileTypes.add(type);
            }
        }

        for(final MissileType type : MissileTypes)
        {
            final LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type);

            launchableSelectionView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    MissileSystem system = game.GetMissileSite(lFittedToStructureID).GetMissileSystem();
                    int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                    int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                    if (lFreeSlots == 0)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_slots));
                    }
                    else if (lAvailableFunds < game.GetConfig().GetMissileCost(type))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                    }
                    else
                    {
                        launchableSelectionView.IncrementNumber();
                        AddType(type.GetID());
                    }
                }
            });

            lytTypes.addView(launchableSelectionView);
            LaunchableSelectionViews.put(type.GetID(), launchableSelectionView);
        }
    }

    private void SetupABMs()
    {
        findViewById(R.id.imgMissile).setVisibility(GONE);

        //Put up to three preferred ones at the top.
        //List<Integer> InterceptorPreferences = ClientDefs.GetInterceptorPreferredOrder(context);
        ArrayList<InterceptorType> InterceptorTypes = new ArrayList<>();
        /*for(Integer lPreferredInterceptor : InterceptorPreferences)
        {
            InterceptorType type = game.GetConfig().GetInterceptorType(lPreferredInterceptor);

            if(type != null)
            {
                if(type.GetABM() && game.GetPurchaseable(type))
                {
                    InterceptorTypes.add(type);
                }
            }
        }*/

        for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
        {
            if(!InterceptorTypes.contains(type) && type.GetABM() && type.GetPurchasable())
            {
                InterceptorTypes.add(type);
            }
        }

        for(final InterceptorType type : InterceptorTypes)
        {
            final LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type);

            launchableSelectionView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    MissileSystem system = bFittedToPlayer ? game.GetOurPlayer().GetInterceptorSystem() : game.GetSAMSite(lFittedToStructureID).GetInterceptorSystem();
                    int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                    int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                    if (lFreeSlots == 0)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_slots));
                    }
                    else if (lAvailableFunds < game.GetConfig().GetInterceptorCost(type))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                    }
                    else
                    {
                        launchableSelectionView.IncrementNumber();
                        AddType(type.GetID());
                    }
                }
            });

            lytTypes.addView(launchableSelectionView);
            LaunchableSelectionViews.put(type.GetID(), launchableSelectionView);
        }
    }

    private void SetupInterceptors()
    {
        findViewById(R.id.imgMissile).setVisibility(GONE);

        //Put up to three preferred ones at the top.
        //List<Integer> InterceptorPreferences = ClientDefs.GetInterceptorPreferredOrder(context);
        ArrayList<InterceptorType> InterceptorTypes = new ArrayList<>();
        /*for(Integer lPreferredInterceptor : InterceptorPreferences)
        {
            InterceptorType type = game.GetConfig().GetInterceptorType(lPreferredInterceptor);

            if(type != null)
            {
                if(!type.GetABM() && game.GetPurchaseable(type))
                {
                    InterceptorTypes.add(type);
                }
            }
        }*/

        for(InterceptorType type : game.GetConfig().GetInterceptorTypes())
        {
            if(!InterceptorTypes.contains(type) && !type.GetABM() && type.GetPurchasable())
            {
                InterceptorTypes.add(type);
            }
        }

        for(final InterceptorType type : InterceptorTypes)
        {
            final LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type);

            launchableSelectionView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    MissileSystem system = bFittedToPlayer ? game.GetOurPlayer().GetInterceptorSystem() : game.GetSAMSite(lFittedToStructureID).GetInterceptorSystem();
                    int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                    int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                    if (lFreeSlots == 0)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_slots));
                    }
                    else if (lAvailableFunds < game.GetConfig().GetInterceptorCost(type))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                    }
                    else
                    {
                        launchableSelectionView.IncrementNumber();
                        AddType(type.GetID());
                    }
                }
            });

            launchableSelectionView.setOnTouchListener(new View.OnTouchListener()
            {
                Handler hand=new Handler();
                Runnable run=new Runnable()
                {
                    @Override
                    public void run()
                    {
                        MissileSystem system = bFittedToPlayer ? game.GetOurPlayer().GetInterceptorSystem() : game.GetSAMSite(lFittedToStructureID).GetInterceptorSystem();
                        int lFreeSlots = system.GetEmptySlotCount() - Types.size();
                        int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

                        if(lFreeSlots > 0 && lAvailableFunds >= game.GetConfig().GetInterceptorCost(type))
                        {
                            launchableSelectionView.IncrementNumber();
                            AddType(type.GetID());
                            //Increment every 100ms
                            hand.postDelayed(this, 80);
                        }
                    }
                };

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
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

            lytTypes.addView(launchableSelectionView);
            LaunchableSelectionViews.put(type.GetID(), launchableSelectionView);
        }
    }

    @Override
    public void Update()
    {

    }

    private void UpdateSelections()
    {
        final Config config = game.GetConfig();

        MissileSystem system;

        if(bFittedToPlayer)
            system = bMissiles ? game.GetOurPlayer().GetMissileSystem() : game.GetOurPlayer().GetInterceptorSystem();
        else
            system = bMissiles ? game.GetMissileSite(lFittedToStructureID).GetMissileSystem() : game.GetSAMSite(lFittedToStructureID).GetInterceptorSystem();

        final int lFreeSlots = system.GetEmptySlotCount() - Types.size();
        final int lAvailableFunds = game.GetOurPlayer().GetWealth() - lTotalCost;

        if(bMissiles)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run() //This code sets the missile view highlighted red or green for use if the missile is purchaseable.
                {
                    boolean bMissileSiteWithoutICBM = false;

                    if(!bFittedToPlayer)
                    {
                        bMissileSiteWithoutICBM = !game.GetMissileSite(lFittedToStructureID).CanTakeICBM();
                    }

                    for (Map.Entry<Integer, LaunchableSelectionView> entry : LaunchableSelectionViews.entrySet())
                    {
                        MissileType type = config.GetMissileType(entry.getKey());
                        LaunchableSelectionView view = entry.getValue();

                        if (lFreeSlots == 0)
                        {
                            view.SetDisabled();
                        }
                        else if (game.GetConfig().GetMissileCost(type) > lAvailableFunds)
                        {
                            view.SetDisabled();
                        }
                        else if (type.GetNuclear() && bFittedToPlayer || type.GetICBM() && (bFittedToPlayer || bMissileSiteWithoutICBM))
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
                    for (Map.Entry<Integer, LaunchableSelectionView> entry : LaunchableSelectionViews.entrySet())
                    {
                        InterceptorType type = config.GetInterceptorType(entry.getKey());
                        LaunchableSelectionView view = entry.getValue();

                        if (lFreeSlots == 0)
                        {
                            view.SetDisabled();
                        }
                        else if (game.GetConfig().GetInterceptorCost(type) > lAvailableFunds)
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

    private void AddType(int lType)
    {
        Types.add(lType);

        lTotalCost = 0;

        for(Integer lLaunchableType : Types)
        {
            lTotalCost += bMissiles ? game.GetConfig().GetMissileCost(lLaunchableType) : game.GetConfig().GetInterceptorCost(lLaunchableType);
        }

        txtCount.setText(Integer.toString(Types.size()));
        txtTotalCost.setText(TextUtilities.GetCurrencyString(lTotalCost));
        btnPurchase.setVisibility(VISIBLE);

        UpdateSelections();
    }

    private void ReturnToParentView()
    {
        if(bFittedToPlayer)
        {
            activity.SetView(bMissiles ? new PlayerMissileView(game, activity) : new PlayerInterceptorView(game, activity));
        }
        else
        {
            activity.SelectEntity(bMissiles ? game.GetMissileSite(lFittedToStructureID) : game.GetSAMSite(lFittedToStructureID));
        }
    }
}
