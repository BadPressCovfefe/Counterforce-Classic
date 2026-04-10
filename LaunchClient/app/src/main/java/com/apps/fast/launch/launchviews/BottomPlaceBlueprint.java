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

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Blueprint;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.LaunchEntity;
import launch.game.entities.ResourceDeposit;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.TerrainData;

public class BottomPlaceBlueprint extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtPlaceBlueprint;
    private LinearLayout btnPlaceBlueprint;
    private TextView txtTooClose;
    private LinearLayout lytGettingTerrainData;
    private TextView txtGettingData;
    private EntityType type;
    private ResourceType resourceType;
    private List<Blueprint> PlacedBlueprints = new ArrayList<>();
    private GoogleMap map;
    private List<CircleOptions> SeparationCircleOptions = new ArrayList<>();
    private List<Circle> DrawnSeparationCircles = new ArrayList<>();
    private Marker markerBlueprint = null;
    private MarkerOptions markerBlueprintOptions = null;
    private GeoCoord geoLocation = null;
    private boolean bCurrentLocationIsValid = true;

    public BottomPlaceBlueprint(LaunchClientGame game, MainActivity activity, EntityType type, ResourceType resourceType)
    {
        super(game, activity, true);
        this.type = type;
        this.resourceType = resourceType;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_place_blueprint, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtPlaceBlueprint = findViewById(R.id.txtPlaceBlueprint);
        btnPlaceBlueprint = findViewById(R.id.btnPlaceBlueprint);
        lytGettingTerrainData = findViewById(R.id.lytGettingTerrainData);
        txtGettingData = findViewById(R.id.txtGettingData);
        txtTooClose = findViewById(R.id.txtTooClose);

        btnPlaceBlueprint.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(geoLocation != null)
                {
                    if(bCurrentLocationIsValid)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.confirm_place_blueprint, TextUtilities.GetCurrencyString(game.GetConfig().GetBlueprintCost()), TextUtilities.GetTimeAmount(game.GetConfig().GetBlueprintExpiry())));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                PlaceBlueprint();
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
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.blueprint_too_close));
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.select_place_blueprint));
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        DrawSeparationCircles();

        Update();
    }

    private void PlaceBlueprint()
    {
        if(bCurrentLocationIsValid)
        {
            activity.InformationMode(true);
            activity.RebuildMap();

            if(bCurrentLocationIsValid)
            {
                game.PlaceBlueprint(type, resourceType, geoLocation);

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(markerBlueprint != null)
                            markerBlueprint.remove();

                        if(!DrawnSeparationCircles.isEmpty())
                        {
                            for(Circle circle : DrawnSeparationCircles)
                            {
                                circle.remove();
                            }
                            DrawnSeparationCircles.clear();
                        }
                    }
                });
            }
            else
            {
                activity.ShowBasicOKDialog(context.getString(R.string.blueprint_too_close));
            }
        }
    }

    public void LocationSelected(GeoCoord geoLocation, GoogleMap map)
    {
        this.map = map;
        this.geoLocation = geoLocation;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                bCurrentLocationIsValid = game.BlueprintFitsInLocation(type, geoLocation);

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtTooClose.setVisibility(bCurrentLocationIsValid ? GONE : VISIBLE);
                    }
                });

                if(type == EntityType.ORE_MINE)
                {
                    ResourceDeposit deposit = game.GetNearbyDeposit(geoLocation);

                    bCurrentLocationIsValid = deposit != null;
                }

                DrawSeparationCircles();
            }
        }).start();

        if(!game.GetTerrainData(geoLocation))
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    txtGettingData.setText(context.getString(R.string.cant_get_terrain_data));
                    txtGettingData.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtGettingData.setBackgroundColor(Utilities.ColourFromAttr(context, R.attr.EnemyBackgroundColour));
                }
            });
        }

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
                if(geoLocation == null)
                {
                    txtPlaceBlueprint.setVisibility(VISIBLE);
                }
                else
                {
                    txtPlaceBlueprint.setVisibility(GONE);
                }

                if(type != EntityType.ORE_MINE || geoLocation == null)
                {
                    lytGettingTerrainData.setVisibility(GONE);
                }
                else
                {
                    lytGettingTerrainData.setVisibility(VISIBLE);
                }
            }
        });
    }

    private void DrawSeparationCircles()
    {
        if(geoLocation != null)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(markerBlueprint != null)
                        markerBlueprint.remove();

                    if(bCurrentLocationIsValid)
                    {
                        markerBlueprintOptions = new MarkerOptions()
                                .position(Utilities.GetLatLng(geoLocation))
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(StructureIconBitmaps.GetBlueprintBitmap(activity, game, type)));

                        markerBlueprint = map.addMarker(markerBlueprintOptions);
                    }

                    if(!DrawnSeparationCircles.isEmpty())
                    {
                        for(Circle circle : DrawnSeparationCircles)
                        {
                            circle.remove();
                        }
                        DrawnSeparationCircles.clear();
                    }

                    if(!SeparationCircleOptions.isEmpty())
                    {
                        SeparationCircleOptions.clear();
                    }

                    for(Structure structure : game.GetNearbyStructures(geoLocation, 3))
                    {
                        CircleOptions options = new CircleOptions()
                                .center(Utilities.GetLatLng(structure.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(structure.GetEntityType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(activity, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(5.0f);

                        SeparationCircleOptions.add(options);
                        DrawnSeparationCircles.add(map.addCircle(options));
                    }

                    for(Blueprint blueprint : game.GetBlueprints())
                    {
                        CircleOptions options = new CircleOptions()
                                .center(Utilities.GetLatLng(blueprint.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(blueprint.GetType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(activity, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(5.0f);

                        SeparationCircleOptions.add(options);
                        DrawnSeparationCircles.add(map.addCircle(options));
                    }
                }
            });
        }
    }

    @Override
    public void MapCleared()
    {
        if(!SeparationCircleOptions.isEmpty() && markerBlueprintOptions != null)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    for(CircleOptions circle : SeparationCircleOptions)
                    {
                        DrawnSeparationCircles.add(map.addCircle(circle));
                    }

                    if(map != null)
                        markerBlueprint = map.addMarker(markerBlueprintOptions);
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
                //If we want to limit structures to water/land only placement, this is where we tell the player.
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof TerrainData && type == EntityType.ORE_MINE)
        {
            LoadTerrainData((TerrainData)entity);
        }
    }
}
