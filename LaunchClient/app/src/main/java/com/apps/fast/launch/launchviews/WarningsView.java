package com.apps.fast.launch.launchviews;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static androidx.core.content.ContextCompat.startActivity;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.views.WarningView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import launch.game.Alliance;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Missile;
import launch.game.entities.Player;
import launch.game.types.MissileType;
import launch.utilities.LaunchLog;

/**
 * Created by tobster on 16/10/15.
 */
public class WarningsView extends LaunchView
{
    private LinearLayout lytThreats;
    private TextView txtNoThreats;
    private TextView txtNoProblems;
    private TextView txtWarning;
    private FrameLayout lytNotifications;
    private LinearLayout btnNotifications;
    private FrameLayout lytAirCover;
    private FrameLayout lytAttack;
    private FrameLayout lytExpenses;
    private FrameLayout lytWealthCap;
    private FrameLayout lytRadiation;

    private Map<Integer, WarningView> ThreatViewMap;

    private Comparator<WarningView> flightTimeComparator;

    public WarningsView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_warnings, this);

        ThreatViewMap = new HashMap();

        txtNoThreats = findViewById(R.id.txtNoThreats);
        txtNoProblems = findViewById(R.id.txtNoProblems);
        txtWarning = findViewById(R.id.txtWarning);

        lytNotifications = findViewById(R.id.lytNotifications);
        btnNotifications = findViewById(R.id.btnNotifications);
        lytAirCover = findViewById(R.id.lytAirCover);
        lytAttack = findViewById(R.id.lytAttack);
        lytExpenses = findViewById(R.id.lytExpenses);
        lytWealthCap = findViewById(R.id.lytWealthCap);
        lytRadiation = findViewById(R.id.lytRadiation);

        lytThreats = findViewById(R.id.lytThreats);
        lytThreats.removeAllViews();

        lytWealthCap.setVisibility(GONE);

        flightTimeComparator = new Comparator<WarningView>()
        {
            @Override
            public int compare(WarningView warningView, WarningView warningViewTOther)
            {
                return (int)(warningView.GetTimeToTarget() - warningViewTOther.GetTimeToTarget());
            }
        };

        btnNotifications.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                btnNotifications.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        NotificationManagerCompat manager = NotificationManagerCompat.from(view.getContext());
                        boolean enabled = manager.areNotificationsEnabled();

                        // ✅ Already enabled → nothing to do
                        if(enabled)
                        {
                            LaunchLog.ConsoleMessage("Notifications already enabled.");
                            return;
                        }

                        // Android 13+ (runtime permission required)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        {
                            // Check if permission already granted
                            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED)
                            {
                                // Should not happen if enabled == false, but just in case
                                LaunchLog.ConsoleMessage("Permission granted but notifications disabled.");
                            }
                            else
                            {
                                // 🔍 Check if we should show rationale
                                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS))
                                {
                                    // 🚫 User already denied → send to settings
                                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                                    activity.startActivity(intent);
                                }
                                else
                                {
                                    // ❗ First time asking → request permission
                                    requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                                }
                            }
                        }
                        else
                        {
                            // Pre-Android 13 → just open notification settings
                            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                            activity.startActivity(intent);
                        }
                    }
                });
            }
        });

        ((TextView)findViewById(R.id.txtWealthCapText)).setText(context.getString(R.string.warning_wealth_cap_text, TextUtilities.GetCurrencyString(Defs.WEALTH_CAP)));

        //Create initial list of missile threats.
        BuildInitialThreats();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for(WarningView warningView : ThreatViewMap.values())
                {
                    warningView.Update();
                }

                //Assess problems.
                boolean bNoProblems = true;

                //TODO: If notifications are not enabled...
                NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                boolean bNotifsEnabled = manager.areNotificationsEnabled();

                if(!bNotifsEnabled)
                {
                   bNoProblems = false;
                   lytNotifications.setVisibility(VISIBLE);
                }
                else
                {
                    lytNotifications.setVisibility(GONE);
                }

                //No air cover.
                if(game.GetPlayerHasNoAirCover(game.GetOurPlayer()))
                {
                    bNoProblems = false;
                    lytAirCover.setVisibility(VISIBLE);
                }
                else
                {
                    lytAirCover.setVisibility(GONE);
                }

                //No attack capability.
                if(game.GetPlayerHasNoAirOffenseCapability(game.GetOurPlayer()))
                {
                    bNoProblems = false;
                    lytAttack.setVisibility(VISIBLE);
                }
                else
                {
                    lytAttack.setVisibility(GONE);
                }

                //High expenses.
                if(game.GetPlayerTotalHourlyIncome(game.GetOurPlayer()) <= 0)
                {
                    bNoProblems = false;
                    lytExpenses.setVisibility(VISIBLE);
                }
                else
                {
                    lytExpenses.setVisibility(GONE);
                }

                /*if(bWealthCapExceeded)
                {
                    bNoProblems = false;
                    lytWealthCap.setVisibility(VISIBLE);
                }
                else
                {
                    lytWealthCap.setVisibility(GONE);
                }*/

                //Radiation.
                if(game.GetRadioactive(game.GetOurPlayer(), false))
                {
                    bNoProblems = false;
                    lytRadiation.setVisibility(VISIBLE);
                }
                else
                {
                    lytRadiation.setVisibility(GONE);
                }

                txtNoProblems.setVisibility(bNoProblems ? VISIBLE : GONE);
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof Player)
        {
            if (entity.GetID() == game.GetOurPlayerID())
            {
                //Our player has changed position. Reassess all missiles.
                for(Missile missile : game.GetMissiles())
                {
                    MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                    if (game.ThreatensPlayerOptimised(missile, game.GetOurPlayer(), game.GetMissileTarget(missile), type)) {
                        //Has become a threat?
                        if (!ThreatViewMap.containsKey(missile.GetID()))
                        {
                            AddThreat(missile);
                        }
                    }
                    else
                    {
                        //No longer a threat?
                        if (ThreatViewMap.containsKey(missile.GetID())) {
                            RemoveThreat(missile);
                        }
                    }
                }
            }
        }
        else if(entity instanceof Missile)
        {
            Missile missile = (Missile)entity;

            if(ThreatViewMap.containsKey(entity.GetID()))
            {
                MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                //Reassess threat.
                if(!game.ThreatensPlayerOptimised(missile, game.GetOurPlayer(), game.GetMissileTarget(missile), type))
                {
                    RemoveThreat(missile);
                }
            }
            else
            {
                MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                //Assess if new threat.
                if(game.ThreatensPlayerOptimised(missile, game.GetOurPlayer(), game.GetMissileTarget(missile), type))
                {
                    AddThreat(missile);
                }
            }
        }
    }

    @Override
    public void EntityRemoved(LaunchEntity entity)
    {
        if(entity instanceof Missile)
        {
            if(ThreatViewMap.containsKey(entity.GetID()))
            {
                //Remove threat.
                lytThreats.removeView(ThreatViewMap.get(entity.GetID()));
                ThreatViewMap.remove(entity.GetID());
            }
        }
    }

    private void BuildInitialThreats()
    {
        //bWealthCapExceeded = game.GetOurPlayer().GetWealth() >= Defs.WEALTH_CAP - game.GetHourlyIncome(game.GetOurPlayer());

        for (Missile missile : game.GetMissiles())
        {
            MissileType type = game.GetConfig().GetMissileType(missile.GetType());

            if (game.ThreatensPlayerOptimised(missile, game.GetOurPlayer(), game.GetMissileTarget(missile), type))
            {
                ThreatViewMap.put(missile.GetID(), new WarningView(activity, game, missile));
            }
        }

        GenerateEntireList();
    }

    private void GenerateEntireList()
    {
        final ArrayList<WarningView> WarningViews = new ArrayList<>(ThreatViewMap.values());
        Collections.sort(WarningViews, flightTimeComparator);

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytThreats.removeAllViews();
                txtNoThreats.setVisibility(VISIBLE);

                for(WarningView warningView : WarningViews)
                {
                    txtNoThreats.setVisibility(GONE);
                    lytThreats.addView(warningView);
                }
            }
        });
    }

    private void AddThreat(final Missile missile)
    {
        final WarningView warningView = new WarningView(activity, game, missile);

        ThreatViewMap.put(missile.GetID(), warningView);

        //See if it should go at the end of the list, otherwise rebuild the entire list.
        for(int i = 0; i < lytThreats.getChildCount(); i++)
        {
            WarningView currentWarningView = (WarningView) lytThreats.getChildAt(i);

            if(currentWarningView.GetTimeToTarget() < warningView.GetTimeToTarget())
            {
                if(lytThreats.getChildCount() == i)
                {
                    //End of the list. Append to the end and leave it at that.
                    activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            txtNoThreats.setVisibility(GONE);
                            lytThreats.addView(warningView);
                        }
                    });
                }
            }
            else
            {
                //Somewhere inside the list. Rebuild the list and break.
                GenerateEntireList();
                break;
            }
        }
    }

    private void RemoveThreat(final Missile missile)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytThreats.removeView(ThreatViewMap.get(missile.GetID()));

                if(lytThreats.getChildCount() == 0)
                {
                    txtNoThreats.setVisibility(VISIBLE);
                }
            }
        });

        ThreatViewMap.remove(missile.GetID());
    }
}
