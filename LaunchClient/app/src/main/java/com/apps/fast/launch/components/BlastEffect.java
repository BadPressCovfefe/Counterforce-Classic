package com.apps.fast.launch.components;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private Marker blastMarker;
    private BlastEffect me = this;
    private LaunchClientGame game;
    private boolean bAirburst;

    private int animFrame;
    private final int FRAMES = 6;
    private final int SECONDS_PER_FRAME = 1;
    private int SECONDS_SINCE_LAST_FRAME = 0;

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Missile missile)
    {
        this.fltWidth = Math.max(MissileStats.GetMissileEMPRadius(game.GetConfig().GetMissileType(missile.GetType()), bAirburst), MissileStats.GetBlastRadius(game.GetConfig().GetMissileType(missile.GetType()), bAirburst));
        this.map = map;
        this.activity = activity;
        this.animFrame = 1;
        this.game = game;
        this.bAirburst = missile.GetAirburst();
        this.geoTarget = game.GetMissileTarget(missile);

        Begin();
    }

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Interceptor interceptor)
    {
        this.fltWidth = game.GetConfig().GetInterceptorType(interceptor.GetType()).GetBlastRadius();
        this.geoTarget = interceptor.GetPosition().GetCopy();
        this.map = map;
        this.activity = activity;
        this.animFrame = 1;
        this.game = game;
        this.bAirburst = false;

        Begin();
    }

    public BlastEffect(MainActivity activity, GoogleMap map, LaunchClientGame game, Torpedo torpedo)
    {
        this.fltWidth = game.GetConfig().GetTorpedoType(torpedo.GetType()).GetBlastRadius();
        this.geoTarget = torpedo.GetPosition().GetCopy();
        this.map = map;
        this.activity = activity;
        this.animFrame = 1;
        this.game = game;
        this.bAirburst = false;

        Begin();
    }

    public void Begin()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                MarkerOptions blastEffect = new MarkerOptions();
                blastEffect.position(Utilities.GetLatLng(geoTarget));
                blastEffect.anchor(0.5f, 0.5f);
                blastEffect.icon(GetStageDrawable());

                blastMarker = map.addMarker(blastEffect);
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
                if(animFrame < FRAMES)
                {
                    if(blastMarker != null)
                    {
                        animFrame++;
                        blastMarker.setIcon(GetStageDrawable());
                    }
                }
                else
                {
                    blastMarker.remove();
                    //activity.BlastAnimationFinished(me);
                }
            }
        });
    }

    public BitmapDescriptor GetStageDrawable()
    {
        return BitmapDescriptorFactory.fromResource(R.drawable.todo);
    }

    public GeoCoord GetPosition()
    {
        return geoTarget;
    }
}
