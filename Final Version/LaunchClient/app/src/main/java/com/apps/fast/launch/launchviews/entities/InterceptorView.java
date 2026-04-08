package com.apps.fast.launch.launchviews.entities;

import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.Interceptor;
import launch.game.entities.Missile;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 09/11/15.
 */
public class InterceptorView extends LaunchView
{
    private int lInterceptorID;

    private TextView txtToTarget;

    private TextView txtSpeed;

    private TextView txtHitChance;

    private TextView txtIntercepting;


    public InterceptorView(LaunchClientGame game, MainActivity activity, int lInterceptorID)
    {
        super(game, activity, true);
        this.lInterceptorID = lInterceptorID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        Interceptor interceptor = game.GetInterceptor(lInterceptorID);

        if(interceptor != null)
        {
            InterceptorType interceptorType = game.GetConfig().GetInterceptorType(interceptor.GetType());

            if(interceptorType != null)
            {
                inflate(context, R.layout.view_interceptor, this);
                ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

                txtSpeed = (TextView)findViewById(R.id.txtSpeed);
                txtHitChance = (TextView)findViewById(R.id.txtHitChance);
                txtIntercepting = findViewById(R.id.txtIntercepting);
                txtToTarget = (TextView)findViewById(R.id.txtToTarget);

                ((TextView) findViewById(R.id.txtInterceptorTitle)).setText(TextUtilities.GetOwnedEntityName(interceptor, game));

                switch(interceptor.GetTargetType())
                {
                    case MISSILE:
                    {
                        Missile missileTarget = game.GetMissile(interceptor.GetTargetID());
                        MissileType missileType = game.GetConfig().GetMissileType(missileTarget.GetType());
                        ((TextView) findViewById(R.id.txtIntercepting)).setText(context.getString(R.string.intercepting, TextUtilities.GetOwnedEntityName(missileTarget, game)));
                        TextUtilities.AssignAccuracyPercentage(txtHitChance, game.GetInterceptorHitChance(interceptorType.GetInterceptorSpeed(), missileTarget.GetSpeed(), interceptorType, missileType), game);
                    }
                    break;

                    case AIRPLANE:
                    {
                        Airplane aircraftTarget = game.GetAirplane(interceptor.GetTargetID());
                        ((TextView) findViewById(R.id.txtIntercepting)).setText(context.getString(R.string.intercepting, TextUtilities.GetOwnedEntityName(aircraftTarget, game)));
                        txtToTarget.setVisibility(GONE);
                        TextUtilities.AssignAccuracyPercentage(txtHitChance, game.GetInterceptorHitChance(interceptorType.GetInterceptorSpeed(), Defs.GetAircraftSpeed(aircraftTarget.GetAircraftType()), interceptorType, null), game);
                    }
                    break;
                }

                TextUtilities.AssignMachSpeedFromKph(txtSpeed, game.GetConfig().GetInterceptorSpeed(interceptorType));

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
                Interceptor interceptor = game.GetInterceptor(lInterceptorID);

                if(interceptor != null)
                {
                    switch(interceptor.GetTargetType())
                    {
                        case MISSILE:
                        {
                            Missile target = game.GetMissile(interceptor.GetTargetID());

                            if(target != null)
                            {
                                InterceptorType interceptorType = game.GetConfig().GetInterceptorType(interceptor.GetType());
                                MissileType missileType = game.GetConfig().GetMissileType(target.GetType());
                                GeoCoord geoMissileTarget = game.GetMissileTarget(target);
                                GeoCoord geoIntercept = target.GetPosition().InterceptPoint(geoMissileTarget, target.GetSpeed(), interceptor.GetPosition(), game.GetConfig().GetInterceptorSpeed(interceptorType));
                                long oInterceptTime = game.GetTimeToTarget(interceptor.GetPosition(), geoIntercept, game.GetConfig().GetInterceptorSpeed(interceptorType));

                                txtToTarget.setText(context.getString(R.string.missile_to_target, TextUtilities.GetTimeAmount(oInterceptTime)));
                            }
                            else
                            {
                                Finish(true);
                            }
                        }
                        break;

                        default:
                        {
                            //Do nothing. Targeted planes and helicopters have no information that needs updated.
                        }
                        break;
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
