package com.apps.fast.launch.components;

import android.graphics.Color;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Interceptor;
import launch.game.entities.Missile;
import launch.game.entities.Torpedo;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

public class BlastEffect
{
    private float fltWidth;
    private GeoCoord geoTarget;
    private GoogleMap map;
    private MainActivity activity;
    private Circle circleBlast;
    private BlastEffect me = this;
    private int animFrame;
    private final int MAX_FRAMES = 7;
    private final int SECONDS_PER_FRAME = 1;
    private int SECONDS_SINCE_LAST_FRAME = 0;

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Missile missile)
    {
        this.fltWidth = Math.max(MissileStats.GetMissileEMPRadius(game.GetConfig().GetMissileType(missile.GetType()), missile.GetAirburst()), MissileStats.GetBlastRadius(game.GetConfig().GetMissileType(missile.GetType()), missile.GetAirburst()));
        this.map = map;
        this.activity = activity;
        this.animFrame = 0;
        this.geoTarget = game.GetMissileTarget(missile);

        Begin();
    }

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Interceptor interceptor)
    {
        this.fltWidth = game.GetConfig().GetInterceptorType(interceptor.GetType()).GetBlastRadius();
        this.geoTarget = interceptor.GetPosition().GetCopy();
        this.map = map;
        this.activity = activity;
        this.animFrame = 0;

        Begin();
    }

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Torpedo torpedo)
    {
        this.fltWidth = game.GetConfig().GetTorpedoType(torpedo.GetType()).GetBlastRadius();
        this.geoTarget = torpedo.GetPosition().GetCopy();
        this.map = map;
        this.activity = activity;
        this.animFrame = 0;

        Begin();
    }

    public void Begin()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                CircleOptions optionsBlast = new CircleOptions();
                optionsBlast.center(Utilities.GetLatLng(geoTarget));
                optionsBlast.radius(fltWidth * Defs.METRES_PER_KM);
                optionsBlast.fillColor(Color.argb(0, 255, 255, 255));
                optionsBlast.strokeWidth(0.0f);
                optionsBlast.zIndex(1000f);

                circleBlast = map.addCircle(optionsBlast);
            }
        });
    }

    public void Tick()
    {
        SECONDS_SINCE_LAST_FRAME++;

        if(SECONDS_SINCE_LAST_FRAME >= SECONDS_PER_FRAME)
        {
            Progress();
            SECONDS_SINCE_LAST_FRAME = 0;
        }
    }

    private void Progress()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(animFrame < MAX_FRAMES)
                {
                    if(circleBlast != null)
                    {
                        animFrame++;
                        circleBlast.setFillColor(GetStageColor());
                    }
                }
                else
                {
                    circleBlast.remove();
                    activity.BlastAnimationFinished(me);
                }
            }
        });
    }

    public int GetStageColor()
    {
        // Clamp frame
        int frame = Math.max(1, Math.min(animFrame, MAX_FRAMES));

        // Fade OUT: 255 → 0
        int alpha = 255 - (int)((frame - 1) * (255.0 / (MAX_FRAMES - 1)));

        return Color.argb(alpha, 255, 255, 255);
    }

    public GeoCoord GetPosition()
    {
        return geoTarget;
    }
}
