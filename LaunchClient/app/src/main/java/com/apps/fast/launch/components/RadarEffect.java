package com.apps.fast.launch.components;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;

import launch.game.Defs;
import launch.game.GeoCoord;

public class RadarEffect
{
    private MainActivity activity;
    private float fltRadiusKM;
    private LatLng center;
    private boolean bVisible;
    private float fltDegrees;
    private final float DEGREES_ROTATION_PER_TICK = 25;
    private boolean bFriendly;
    private RadarEffect me = this;
    private GoogleMap map;
    private Circle radarCircle;
    private Circle stealthCircle;
    private Polyline line;

    public RadarEffect(MainActivity activity, GoogleMap map, GeoCoord geoCenter, float fltRadiusKM, boolean bFriendly, boolean bVisible)
    {
        this.activity = activity;
        this.map = map;
        this.center = Utilities.GetLatLng(geoCenter);
        this.fltRadiusKM = fltRadiusKM;
        this.bFriendly = bFriendly;
        this.bVisible = bVisible;

        Begin();
    }

    private void Begin()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                radarCircle = map.addCircle(new CircleOptions()
                        .center(center)
                        .visible(bVisible)
                        .radius(fltRadiusKM * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(activity, bFriendly ? R.attr.FriendlyRadarBackground : R.attr.EnemyRadarBackground))
                        .strokeColor(Utilities.ColourFromAttr(activity, bFriendly ? R.attr.FriendlyRadarOutline : R.attr.EnemyRadarOutline))
                        .strokeWidth(3.0f));

                stealthCircle = map.addCircle(new CircleOptions()
                        .center(center)
                        .visible(bVisible)
                        .radius(fltRadiusKM * Defs.STEALTH_DETECTION_FRACTION * Defs.METRES_PER_KM)
                        .strokeColor(Utilities.ColourFromAttr(activity, bFriendly ? R.attr.FriendlyRadarOutline : R.attr.EnemyRadarOutline))
                        .strokeWidth(3.0f));

                line = map.addPolyline(new PolylineOptions()
                        .add(center)
                        .visible(bVisible)
                        .add(SphericalUtil.computeOffset(center, fltRadiusKM * Defs.METRES_PER_KM, 0))
                        .geodesic(true)
                        .width(3.0f)
                        .color(Utilities.ColourFromAttr(activity, bFriendly ? R.attr.FriendlyRadarOutline : R.attr.EnemyRadarOutline)));
            }
        });
    }

    public void ToggleVisible(boolean bVisible)
    {
        this.bVisible = bVisible;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(radarCircle != null)
                    radarCircle.setVisible(bVisible);

                if(stealthCircle != null)
                    stealthCircle.setVisible(bVisible);

                if(line != null)
                    line.setVisible(bVisible);
            }
        });
    }

    public void Tick()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(bVisible && line != null)
                    line.setPoints(Arrays.asList(center, SphericalUtil.computeOffset(center, fltRadiusKM * Defs.METRES_PER_KM, fltDegrees += DEGREES_ROTATION_PER_TICK)));
            }
        });
    }

    public void SetCenter(LatLng center)
    {
        this.center = center;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(radarCircle != null)
                {
                    radarCircle.setCenter(center);
                }

                if(stealthCircle != null)
                {
                    stealthCircle.setCenter(center);
                }

                if(line != null)
                {
                    line.setPoints(Arrays.asList(center, SphericalUtil.computeOffset(center, fltRadiusKM * Defs.METRES_PER_KM, fltDegrees)));
                }
            }
        });
    }

    public void Finish()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(radarCircle != null)
                    radarCircle.remove();

                if(stealthCircle != null)
                    stealthCircle.remove();

                if(line != null)
                    line.remove();

                radarCircle = null;
                stealthCircle = null;
                line = null;
            }
        });
    }
}
