package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.StructureIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.CommandPost;
import launch.game.entities.LaunchEntity;
import launch.game.entities.ResourceDeposit;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.TerrainData;

public class BottomBuildShipyard extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtBuildShipyard;
    private LinearLayout btnBuildShipyard;
    private TextView txtTooFar;
    private TextView txtNotWater;
    private GoogleMap map;
    private Marker markerOutput = null;
    private Circle outputCircle = null;
    private MarkerOptions markerOutputOptions = null;
    private CircleOptions outputCircleOptions = null;
    private GeoCoord geoOutput = null;
    private boolean bTooFar = true;
    boolean bWater = false;
    
    public BottomBuildShipyard(LaunchClientGame game, MainActivity activity, GoogleMap map)
    {
        super(game, activity, true);
        this.map = map;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_build_shipyard, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtBuildShipyard = findViewById(R.id.txtBuildShipyard);
        btnBuildShipyard = findViewById(R.id.btnBuildShipyard);
        txtTooFar = findViewById(R.id.txtTooFar);
        txtNotWater = findViewById(R.id.txtNotWater);

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                outputCircleOptions = new CircleOptions()
                        .center(Utilities.GetLatLng(game.GetOurPlayer().GetPosition()))
                        .radius(Defs.SHIPYARD_OUTPUT_DISTANCE * Defs.METRES_PER_KM)
                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InfoColour))
                        .strokeWidth(3.0f);

                outputCircle = map.addCircle(outputCircleOptions);
            }
        });

        btnBuildShipyard.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(geoOutput != null)
                {
                    if(!bWater)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.output_not_water));
                    }
                    else if(bTooFar)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.too_far_from_shipyard));
                    }
                    else
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderConstruct();
                        launchDialog.SetMessage(context.getString(R.string.construct_confirm, TextUtilities.GetEntityTypeName(EntityType.SHIPYARD), TextUtilities.GetCurrencyString(Defs.SHIPYARD_STRUCTURE_COST)));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                BuildShipyard();
                            }
                        });
                        launchDialog.SetOnClickNo(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.select_shipyard_output));
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
                activity.ResetInteractionMode();
                activity.RemoveTargettingMapUI();

                if(markerOutput != null)
                    markerOutput.remove();

                if(outputCircle != null)
                    outputCircle.remove();
            }
        });

        Update();
    }

    private void BuildShipyard()
    {
        game.ConstructShipyard(geoOutput);

        activity.InformationMode(false);
        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        geoOutput = null;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(markerOutput != null)
                    markerOutput.remove();

                if(outputCircle != null)
                    outputCircle.remove();
            }
        });
    }

    public void LocationSelected(GeoCoord geoOutput, GoogleMap map)
    {
        this.map = map;
        this.geoOutput = geoOutput;

        game.GetTerrainData(geoOutput);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                bTooFar = geoOutput.DistanceTo(game.GetOurPlayer().GetPosition()) > Defs.SHIPYARD_OUTPUT_DISTANCE;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtTooFar.setVisibility(bTooFar ? VISIBLE : GONE);
                    }
                });
            }
        }).start();

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(geoOutput == null)
                {
                    txtBuildShipyard.setVisibility(VISIBLE);
                }
                else
                {
                    txtBuildShipyard.setVisibility(GONE);
                }
            }
        });
    }

    @Override
    public void MapCleared()
    {
        if(outputCircleOptions != null && markerOutputOptions != null)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(map != null)
                    {
                        markerOutput = map.addMarker(markerOutputOptions);
                        outputCircle = map.addCircle(outputCircleOptions);
                    }
                }
            });
        }
    }

    public void LoadTerrainData(TerrainData data)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                bWater = data.GetWater();

                if(bWater)
                {
                    txtNotWater.setVisibility(GONE);
                }
                else
                {
                    txtNotWater.setVisibility(VISIBLE);
                }

                if(markerOutput != null)
                {
                    markerOutput.remove();
                    markerOutput = null;
                }

                markerOutputOptions = new MarkerOptions()
                        .position(Utilities.GetLatLng(geoOutput))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(bWater? R.drawable.marker_movement_waypoint : R.drawable.marker_attack_waypoint));

                markerOutput = map.addMarker(markerOutputOptions);
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
