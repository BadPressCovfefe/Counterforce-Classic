package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.DistancedEntityView;
import com.apps.fast.launch.views.EntityControls;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Loot;
import launch.game.entities.MapEntity;
import launch.game.entities.Radiation;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.TerrainData;

/**
 * Created by tobster on 09/11/15.
 */
public class MapClickView extends LaunchView
{
    private GeoCoord geoClick;
    private TextView txtRadioactive;
    private TextView txtCalculating;
    private LinearLayout lytEntities;
    private TextView txtNearestEntities;
    private LinearLayout lytGettingTerrainData;
    private LinearLayout lytTerrain;
    private TextView txtGettingData;
    private TextView txtWater;
    private LinearLayout btnAttack;
    private LinearLayout btnMoveAircraft;
    boolean bRadioactive = false;
    int lRadExpiry;

    public MapClickView(LaunchClientGame game, MainActivity activity, LatLng latLng)
    {
        super(game, activity, true);
        geoClick = new GeoCoord(latLng.latitude, latLng.longitude, true);
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_map_click, this);
        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        txtRadioactive = findViewById(R.id.txtRadioactive);
        txtCalculating = findViewById(R.id.txtCalculating);
        lytEntities = findViewById(R.id.lytEntities);
        btnAttack =  findViewById(R.id.btnAttack);
        btnMoveAircraft = findViewById(R.id.btnMoveAircraft);
        txtNearestEntities = findViewById(R.id.txtNearestEntities);

        lytGettingTerrainData = findViewById(R.id.lytGettingTerrainData);
        lytTerrain = findViewById(R.id.lytTerrain);
        txtWater = findViewById(R.id.txtWater);
        txtGettingData = findViewById(R.id.txtGettingData);

        FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

        UnitControls controls = new UnitControls(game, activity, geoClick);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        if(!game.GetTerrainData(geoClick))
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    txtGettingData.setText(context.getString(R.string.cant_get_terrain_data));
                    txtGettingData.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            });
        }

        /*btnAttack.setOnClickListener(new OnClickListener()
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
                    activity.MissileSelectForTarget(geoClick, null, TextUtilities.GetLatLongString(geoClick.GetLatitude(), geoClick.GetLongitude()));
                }
            }
        });*/

        ((TextView)findViewById(R.id.txtPlayerJoins)).setText(TextUtilities.GetLatLongString(geoClick.GetLatitude(), geoClick.GetLongitude()));

        txtRadioactive.setVisibility(GONE);

        //Spark up the comparisons on another thread as they're a bit intensive.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for(Radiation radiation : game.GetRadiations())
                {
                    if(!bRadioactive)
                    {
                        if(geoClick.DistanceTo(radiation.GetPosition()) <= radiation.GetRadius())
                        {
                            bRadioactive = true;
                            lRadExpiry = radiation.GetExpiryRemaining();
                        }
                    }
                }

                final List<MapEntity> Nearest = game.GetNearestEntities(geoClick, ClientDefs.NEAREST_ENTITY_COUNT);

                //Populate the layout.
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(bRadioactive)
                        {
                            txtRadioactive.setVisibility(VISIBLE);
                            txtRadioactive.setText(context.getString(R.string.warning_radiation, TextUtilities.GetTimeAmount(lRadExpiry)));
                        }

                        txtCalculating.setVisibility(GONE);

                        txtNearestEntities.setVisibility(VISIBLE);

                        for(final MapEntity entity : Nearest)
                        {
                            if((entity.GetVisible() || game.EntityIsFriendly(entity, game.GetOurPlayer())) && !(entity instanceof Loot))
                            {
                                DistancedEntityView nev = new DistancedEntityView(context, activity, entity, geoClick, game);

                                nev.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.SelectEntity(entity);
                                    }
                                });

                                lytEntities.addView(nev);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void Update()
    {

    }

    public void LoadTerrainData(TerrainData data)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytGettingTerrainData.setVisibility(GONE);
                lytTerrain.setVisibility(VISIBLE);
                txtWater.setVisibility(VISIBLE);

                if(data.GetWater())
                {
                    txtWater.setText(context.getString(R.string.terrain_type_water));
                }
                else
                {
                    txtWater.setText(context.getString(R.string.terrain_type_land));
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof TerrainData)
        {
            LoadTerrainData((TerrainData)entity);
        }
    }
}
