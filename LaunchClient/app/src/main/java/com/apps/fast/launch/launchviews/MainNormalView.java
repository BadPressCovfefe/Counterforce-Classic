package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.Locatifier;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.List;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.treaties.Treaty;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchUtilities;

/**
 * Created by tobster on 15/07/16.
 */
public class MainNormalView extends LaunchView
{
    private LinearLayout lytEmpty;
    private FrameLayout lytBottom;
    private ImageView imgSelect;
    private TextView txtMoney;
    private TextView txtGPS;
    private TextView txtSignal;
    private ImageView imgGPS;
    private ImageView imgSignal;

    private TextView txtPrivacyZone;
    private TextView txtMain;
    private TextView txtEvent1;
    private TextView txtEvent2;
    private TextView txtEvent3;

    private TextView txtDebug;
    private TextView txtSelecting;

    private ImageView imgRadiation;

    private LaunchView bottomView;

    private LinearLayout lytMoneyPoints;
    private LinearLayout lytGPSSig;

    private boolean bRendering = false; //To synchronise the render thread when lots of stuff is going on.

    public MainNormalView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.main_normal, this);

        lytEmpty = findViewById(R.id.lytEmpty);
        lytBottom = findViewById(R.id.lytBottom);
        imgSelect = findViewById(R.id.imgSelect);

        txtMoney = findViewById(R.id.txtMoney);
        txtGPS = findViewById(R.id.txtGPS);
        txtSignal = findViewById(R.id.txtSignal);
        imgGPS = findViewById(R.id.imgGPS);
        imgSignal = findViewById(R.id.imgSignal);

        txtPrivacyZone = findViewById(R.id.txtPrivacyZone);
        txtMain = findViewById(R.id.txtMain);
        imgRadiation = findViewById(R.id.imgRadiation);
        txtEvent1 = findViewById(R.id.txtEvent1);
        txtEvent2 = findViewById(R.id.txtEvent2);
        txtEvent3 = findViewById(R.id.txtEvent3);
        txtDebug = findViewById(R.id.txtDebug);
        txtSelecting = findViewById(R.id.txtSelecting);

        lytMoneyPoints = findViewById(R.id.lytMoneyPoints);
        lytGPSSig = findViewById(R.id.lytGPSSig);

        if(activity.GetInteractionMode() == MainActivity.InteractionMode.STANDARD)
        {
            BottomLayoutShowView(new BottomNormalView(game, activity));
        }

        if(activity.GetMapModeZoom())
        {
            txtSelecting.setVisibility(GONE);
            imgSelect.setAlpha(0.5f);
        }
        else
        {
            txtSelecting.setVisibility(VISIBLE);
            imgSelect.setAlpha(1.0f);
        }

        imgSelect.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetMapModeZoomOrSelect(false);
                Update();
                activity.ReturnToMainView();
            }
        });

        lytMoneyPoints.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!game.GetInteractionReady())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                }
                else
                {
                    activity.SetView(new WealthRulesView(game, activity));
                }
            }
        });

        lytGPSSig.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.GoTo(activity.GetLocatifier().GetLocation());
            }
        });

        lytGPSSig.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                activity.SetView(new GPSView(game, activity));
                return true;
            }
        });

        try
        {
            txtDebug.setVisibility(game.GetConfig().DebugMode() ? VISIBLE : GONE);
        }
        catch(Exception ex)
        {
            //Uninterested.
        }
    }

    public void BottomLayoutShowView(final LaunchView launchView)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(bottomView != null)
                {
                    lytBottom.removeView(bottomView);
                }

                bottomView = launchView;
                lytBottom.addView(bottomView);
            }
        });
    }

    @Override
    public void Update()
    {
        if(!bRendering)
        {
            bRendering = true;

            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    LaunchClientLocation location = activity.GetLocatifier().GetLocation();

                    if(location == null)
                    {
                        txtGPS.setText(context.getString(R.string.value_unknown));
                        txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        imgGPS.setVisibility(GONE);
                    }
                    else
                    {
                        txtGPS.setText(TextUtilities.GetDistanceStringFromM(location.GetAccuracy()));
                        imgGPS.setVisibility(VISIBLE);

                        if (game.GetInPrivacyZone())
                        {
                            txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
                            imgGPS.setColorFilter(Utilities.ColourFromAttr(context, R.attr.InfoColour));
                        }
                        else
                        {
                            if(location.GetAccuracy() > Defs.REQUIRED_ACCURACY * Defs.METRES_PER_KM)
                            {
                                txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                imgGPS.setColorFilter(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            }
                            else
                            {
                                Locatifier.Quality locationQuality = activity.GetLocatifier().GetLocationQuality();

                                if (locationQuality == Locatifier.Quality.GOOD)
                                {
                                    txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                    imgGPS.setColorFilter(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else if (locationQuality == Locatifier.Quality.A_BIT_OLD)
                                {
                                    txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                                    imgGPS.setColorFilter(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                                }
                                else
                                {
                                    txtGPS.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                    imgGPS.setColorFilter(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }
                            }
                        }
                    }

                    long oLatency = game.GetLatency();

                    if (oLatency == Defs.LATENCY_DISCONNECTED)
                    {
                        imgSignal.setColorFilter(Utilities.ColourFromAttr(context, R.attr.BadColour));
                        txtSignal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));

                        if (game.GetCommsDoingAnything())
                            txtSignal.setText(TextUtilities.GetConnectionSpeed(game.GetCommsDownloadRate()));
                        else
                            txtSignal.setText(context.getString(R.string.value_unknown_reconnect, game.GetCommsReinitRemaining() / TextUtilities.A_SEC));
                    }
                    else
                    {
                        imgSignal.setColorFilter(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        txtSignal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        txtSignal.setText(TextUtilities.GetLatencyString(oLatency));
                    }

                    txtPrivacyZone.setVisibility(game.GetInPrivacyZone() ? VISIBLE : GONE);

                    //Write event text items.
                    List<LaunchEvent> Events = game.GetEvents();

                    txtEvent1.setVisibility(GONE);
                    txtEvent2.setVisibility(GONE);
                    txtEvent3.setVisibility(GONE);

                    if(!Events.isEmpty())
                    {
                        LaunchEvent event = Events.get(0);

                        if (event.GetTime() > Utilities.GetServerTime() - ClientDefs.EVENT_MAIN_SCREEN_PERSISTENCE)
                        {
                            txtEvent1.setText(event.GetMessage());
                            txtEvent1.setVisibility(VISIBLE);
                        }
                    }

                    if (Events.size() > 1)
                    {
                        LaunchEvent event = Events.get(1);

                        if (event.GetTime() > Utilities.GetServerTime() - ClientDefs.EVENT_MAIN_SCREEN_PERSISTENCE)
                        {
                            txtEvent2.setText(event.GetMessage());
                            txtEvent2.setVisibility(VISIBLE);
                        }
                    }

                    if (Events.size() > 2)
                    {
                        LaunchEvent event = Events.get(2);

                        if (event.GetTime() > Utilities.GetServerTime() - ClientDefs.EVENT_MAIN_SCREEN_PERSISTENCE)
                        {
                            txtEvent3.setText(event.GetMessage());
                            txtEvent3.setVisibility(VISIBLE);
                        }
                    }

                    if(activity.GetMapModeZoom())
                    {
                        txtSelecting.setVisibility(GONE);
                        imgSelect.setAlpha(0.5f);
                    }
                    else
                    {
                        txtSelecting.setVisibility(VISIBLE);
                        imgSelect.setAlpha(1.0f);
                    }

                    bRendering = false;
                }
            });
        }

        OurPlayerUpdated();

        if(bottomView != null)
        {
            bottomView.Update();
        }
    }

    public LaunchView GetBottomView()
    {
        return bottomView;
    }

    public void ExpandBottomView()
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = context.getResources().getInteger(R.integer.MiddleViewWeightCollapsed);
        lytEmpty.setLayoutParams(params);
    }

    public void ContractBottomView()
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = context.getResources().getInteger(R.integer.MiddleViewWeightNormal);
        lytEmpty.setLayoutParams(params);
    }

    /**
     * Call from setup or whenever our player updates to update the UI elements that pertain to our player.
     */
    private void OurPlayerUpdated()
    {
        final Player ourPlayer = game.GetOurPlayer();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(ourPlayer == null)
                {
                    txtMoney.setText(activity.getString(R.string.value_unknown));
                }
                else
                {
                    txtMain.setText("");
                    txtMain.setVisibility(GONE);

                    txtMoney.setText(TextUtilities.GetCurrencyString(ourPlayer.GetWealth()));

                    //Spawn a thread to check for radioactivity, so as not to hang the UI thread.
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            long oLatency = game.GetLatency();

                            if(oLatency == Defs.LATENCY_DISCONNECTED)
                            {
                                activity.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        imgRadiation.setVisibility(VISIBLE);
                                        imgRadiation.setImageResource(R.drawable.icon_no_signal);
                                    }
                                });
                            }
                            else if(game.GetRadioactive(ourPlayer, false))
                            {
                                activity.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        imgRadiation.setVisibility(VISIBLE);
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
                                        imgRadiation.setVisibility(GONE);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity != null)
        {
            if(bottomView != null)
                bottomView.EntityUpdated(entity);

            if(entity instanceof Player)
            {
                Player ourPlayer = game.GetOurPlayer();

                if(ourPlayer != null)
                {
                    if(ourPlayer.ApparentlyEquals(entity))
                        OurPlayerUpdated();
                }
            }
        }
    }

    @Override
    public void TreatyUpdated(Treaty treaty)
    {
        bottomView.TreatyUpdated(treaty);
    }
}
