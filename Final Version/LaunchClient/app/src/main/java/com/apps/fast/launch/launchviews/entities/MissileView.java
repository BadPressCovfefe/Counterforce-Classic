package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.DistancedEntityView;
import com.apps.fast.launch.views.EntityControls;

import java.util.List;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.*;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 09/11/15.
 */
public class MissileView extends LaunchView
{
    private int lMissileID;

    private TextView txtToTarget;
    private TextView txtNoTargets;
    private LinearLayout btnLaunchInterceptor;
    private TextView txtSpeed;
    private TextView txtRange;
    private TextView txtRangeTitle;
    private TextView txtBlastRadius;
    private TextView txtMaxDamage;
    private TextView txtEMPRadius;
    private TextView txtBlastRadiusTitle;
    private TextView txtMaxDamageTitle;
    private TextView txtEMPRadiusTitle;
    private TextView txtECMDescription;
    private TextView txtStealthDescription;
    private TextView txtWarhead;
    private TextView txtWarheadTitle;
    private TextView txtAccuracy;

    private TextView txtNoRadarCoverage;

    private ImageView imgMissile;

    public MissileView(LaunchClientGame game, MainActivity activity, int lMissileID)
    {
        super(game, activity, true);
        this.lMissileID = lMissileID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        final Missile missile = game.GetMissile(lMissileID);

        if(missile != null)
        {
            MissileType missileType = game.GetConfig().GetMissileType(missile.GetType());
            MissileType type = game.GetConfig().GetMissileType(missile.GetType());

            if(missileType != null)
            {
                inflate(context, R.layout.view_missile, this);
                ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

                ((TextView) findViewById(R.id.txtMissileTitle)).setText(TextUtilities.GetOwnedEntityName(missile, game));

                txtToTarget = (TextView) findViewById(R.id.txtToTarget);
                txtNoTargets = (TextView) findViewById(R.id.txtNoTargets);
                btnLaunchInterceptor = (LinearLayout) findViewById(R.id.btnLaunchInterceptor);
                txtSpeed = (TextView) findViewById(R.id.txtSpeed);
                txtRange = (TextView) findViewById(R.id.txtRange);
                txtRangeTitle = (TextView) findViewById(R.id.txtRangeTitle);
                txtBlastRadius = (TextView) findViewById(R.id.txtBlastRadius);
                txtMaxDamage = (TextView) findViewById(R.id.txtMaxDamage);
                txtEMPRadius = (TextView) findViewById(R.id.txtEMPRadius);
                txtBlastRadiusTitle = (TextView) findViewById(R.id.txtBlastRadiusTitle);
                txtMaxDamageTitle = (TextView) findViewById(R.id.txtMaxDamageTitle);
                txtEMPRadiusTitle = (TextView) findViewById(R.id.txtEMPRadiusTitle);
                txtWarhead = findViewById(R.id.txtWarhead);
                txtWarheadTitle = findViewById(R.id.txtWarheadTitle);
                txtAccuracy = findViewById(R.id.txtAccuracy);

                txtECMDescription = findViewById(R.id.txtECMDescription);
                txtStealthDescription = findViewById(R.id.txtStealthDescription);

                txtNoRadarCoverage = findViewById(R.id.txtNoRadarCoverage);

                txtECMDescription.setText(context.getString(R.string.ecm_information, TextUtilities.GetAccuracyPercentage(game.GetConfig().GetECMInterceptorChanceReduction())));
                txtStealthDescription.setText(context.getString(R.string.stealth_description, TextUtilities.GetDistanceStringFromKM(Defs.STEALTH_ENGAGEMENT_DISTANCE)));

                txtECMDescription.setVisibility(missileType.GetECM() ? VISIBLE : GONE);
                txtStealthDescription.setVisibility(missileType.GetStealth() ? VISIBLE : GONE);

                btnLaunchInterceptor.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.InterceptorSelectForTarget(lMissileID, TextUtilities.GetOwnedEntityName(missile, game), missile);
                    }
                });

                imgMissile = (ImageView) findViewById(R.id.imgMissile);

                imgMissile.setImageBitmap(EntityIconBitmaps.GetMissileBitmap(activity, game, type, game.GetAllegiance(game.GetOurPlayer(), missile), type.GetAssetID()));

                //txtBlastRadius.setVisibility(type.GetBlastRadius() != 0 ? VISIBLE : GONE);
                //txtMaxDamage.setVisibility(type.GetMaxDamage() != 0 ? VISIBLE : GONE);
                txtEMPRadius.setVisibility(game.EntityIsFriendly(missile, game.GetOurPlayer()) && MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()) != 0 ? VISIBLE : GONE);
                //txtBlastRadiusTitle.setVisibility(type.GetBlastRadius() != 0 ? VISIBLE : GONE);
                //txtMaxDamageTitle.setVisibility(type.GetMaxDamage() != 0 ? VISIBLE : GONE);
                txtEMPRadiusTitle.setVisibility(game.EntityIsFriendly(missile, game.GetOurPlayer()) && MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()) != 0 ? VISIBLE : GONE);

                TextUtilities.AssignYieldStringAndAppearance(txtWarhead, type, game);
                txtWarhead.setVisibility(game.EntityIsFriendly(missile, game.GetOurPlayer()) ? VISIBLE : GONE);
                txtWarheadTitle.setVisibility(game.EntityIsFriendly(missile, game.GetOurPlayer()) ? VISIBLE : GONE);
                TextUtilities.AssignMachSpeedFromKph(txtSpeed, missile.GetSpeed());

                if(!type.GetArtillery())
                {
                    txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetMissileRange(missileType)));
                }
                else
                {
                    txtRange.setVisibility(GONE);
                    txtRangeTitle.setVisibility(GONE);
                }

                //txtBlastRadius.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetBlastRadius(missileType)));
                txtEMPRadius.setText(TextUtilities.GetDistanceStringFromKM(MissileStats.GetMissileEMPRadius(type, missile.GetAirburst())));
                txtAccuracy.setText(TextUtilities.GetPercentStringFromFraction(type.GetAccuracy()));
                //txtMaxDamage.setText(TextUtilities.GetDamageString(game.GetConfig().GetMissileMaxDamage(missileType)));

                TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetMissileRange(), true);

                if(type.GetAccuracy() >= 0.75f)
                    txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.GoodColour));
                else if(type.GetAccuracy() > 0.4f)
                    txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.WarningColour));
                else
                    txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.BadColour));

                Update();
            }
            else
            {
                Finish(true);
            }
        }
        else
        {
            Finish(true);
        }
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Missile missile = game.GetMissile(lMissileID);

                if(missile != null)
                {
                    MissileType missileType = game.GetConfig().GetMissileType(missile.GetType());

                    if(missileType != null)
                    {
                        txtECMDescription.setVisibility(missileType.GetECM() ? VISIBLE : GONE);

                        txtToTarget.setText(context.getString(R.string.missile_to_target, TextUtilities.GetTimeAmount(game.GetTimeToTarget(missile))));

                        txtNoRadarCoverage.setVisibility(GONE);
                        //Create list of potential targets.
                        List<Player> Targets = game.GetAffectedPlayers(game.GetMissileTarget(missile), MissileStats.GetBlastRadius(missileType, missile.GetAirburst()));

                        if(Targets.size() > 0)
                        {
                            txtNoTargets.setVisibility(GONE);
                            LinearLayout lytTargets = (LinearLayout) findViewById(R.id.lytTargets);

                            lytTargets.removeAllViews();

                            for(final Player entity : Targets)
                            {
                                DistancedEntityView nev = new DistancedEntityView(context, activity, entity, missile.GetPosition(), game);

                                nev.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.SelectEntity(entity);
                                    }
                                });

                                lytTargets.addView(nev);
                            }
                        }
                        else
                        {
                            txtNoTargets.setVisibility(VISIBLE);
                        }

                        btnLaunchInterceptor.setVisibility(game.GetOurPlayer().Functioning() ? VISIBLE : GONE);
                }
                    else
                    {
                        Finish(true);
                    }
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }
}
