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
import launch.game.entities.ResourceDeposit;
import launch.game.entities.Structure;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.TerrainData;

public class BottomBuildStructure extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtBuildStructure;
    private LinearLayout btnBuildStructure;
    private TextView txtTooFar;
    private EntityType type;
    private ResourceType resourceType;
    private GoogleMap map;
    private List<CircleOptions> SeparationCircleOptions = new ArrayList<>();
    private List<Circle> DrawnSeparationCircles = new ArrayList<>();
    private Marker markerStructure = null;
    private Circle separationCircle = null;
    private MarkerOptions markerStructureOptions = null;
    private CircleOptions separationCircleOptions = null;
    private GeoCoord geoLocation = null;
    private boolean bUseSubstitutes = false;
    private boolean bTooClose = true; //Too close to other structures.
    private boolean bTooFar = true; //Too far from the command post.
    private boolean bDepositNearby = false; //No deposit nearby. (Only matters for ore mines.)
    private CommandPost commandPost;
    public BottomBuildStructure(LaunchClientGame game, MainActivity activity, int lCommandPostID, EntityType type, ResourceType resourceType, boolean bUseSubstitutes)
    {
        super(game, activity, true);
        this.commandPost = game.GetCommandPost(lCommandPostID);
        this.type = type;
        this.resourceType = resourceType;
        this.bUseSubstitutes = bUseSubstitutes;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_build_structure, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtBuildStructure = findViewById(R.id.txtBuildStructure);
        btnBuildStructure = findViewById(R.id.btnBuildStructure);
        txtTooFar = findViewById(R.id.txtTooFar);

        btnBuildStructure.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(geoLocation != null)
                {
                    if(bTooFar)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.too_far_from_post));
                    }
                    else if(bTooClose)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.too_close_to_structures));
                    }
                    else
                    {
                        Map<ResourceType, Long> costsToUse = game.GetConfig().GetStructureBuildCost(type, resourceType);

                        if(!bUseSubstitutes)
                            costsToUse = game.GetRequiredCost(costsToUse);
                        else
                            costsToUse = game.GetSubstitutionCost(costsToUse);

                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderConstruct();
                        launchDialog.SetMessage(context.getString(R.string.construct_confirm, TextUtilities.GetEntityTypeName(type), TextUtilities.GetCostStatement(costsToUse)));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                BuildStructure();
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
                    activity.ShowBasicOKDialog(context.getString(R.string.select_build_structure));
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

                if(markerStructure != null)
                    markerStructure.remove();

                if(separationCircle != null)
                    separationCircle.remove();

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

        DrawSeparationCircles();

        Update();
    }

    private void BuildStructure()
    {
        game.ConstructStructure(type, resourceType, commandPost.GetID(), geoLocation, bUseSubstitutes);

        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        activity.SelectEntity(commandPost);

        geoLocation = null;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(markerStructure != null)
                    markerStructure.remove();

                if(separationCircle != null)
                    separationCircle.remove();

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

    public void LocationSelected(GeoCoord geoLocation, GoogleMap map)
    {
        this.map = map;
        this.geoLocation = geoLocation;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                bTooClose = !game.GetNearbyStructures(geoLocation, game.GetConfig().GetStructureSeparation(EntityType.COMMAND_POST)).isEmpty();
                bTooFar = geoLocation.DistanceTo(commandPost.GetPosition()) > Defs.COMMAND_POST_RADIUS;
                bDepositNearby = game.GetNearbyDeposit(geoLocation) != null;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtTooFar.setVisibility(bTooFar ? VISIBLE : GONE);
                    }
                });

                DrawSeparationCircles();
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
                if(geoLocation == null)
                {
                    txtBuildStructure.setVisibility(VISIBLE);
                }
                else
                {
                    txtBuildStructure.setVisibility(GONE);
                }
            }
        });
    }

    private void DrawSeparationCircles()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(markerStructure != null)
                    markerStructure.remove();

                if(separationCircle != null)
                    separationCircle.remove();

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

                if(geoLocation != null)
                {
                    separationCircleOptions = new CircleOptions()
                            .center(Utilities.GetLatLng(geoLocation))
                            .radius(game.GetConfig().GetStructureSeparation(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(activity, R.attr.StructureSeparationRadiusColour))
                            .strokeWidth(5.0f);

                    separationCircle = map.addCircle(separationCircleOptions);

                    markerStructureOptions = new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromBitmap(StructureIconBitmaps.GetStructureTypeBitmap(activity, type)));

                    markerStructure = map.addMarker(markerStructureOptions);

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

                    if(type == EntityType.ORE_MINE)
                    {
                        for(ResourceDeposit deposit : game.GetResourceDeposits())
                        {
                            if(deposit.GetPosition().DistanceTo(commandPost.GetPosition()) <= Defs.DEPOSIT_RADIUS + Defs.COMMAND_POST_RADIUS)
                            {
                                CircleOptions options = new CircleOptions()
                                        .center(Utilities.GetLatLng(deposit.GetPosition()))
                                        .radius(Defs.DEPOSIT_RADIUS * Defs.METRES_PER_KM)
                                        .strokeWidth(0.0f)
                                        .fillColor(Utilities.ColourFromAttr(activity, R.attr.LootRadiusColour));

                                SeparationCircleOptions.add(options);
                                DrawnSeparationCircles.add(map.addCircle(options));
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void MapCleared()
    {
        if(!SeparationCircleOptions.isEmpty() && markerStructureOptions != null)
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
                    {
                        markerStructure = map.addMarker(markerStructureOptions);
                        separationCircle = map.addCircle(separationCircleOptions);
                    }
                }
            });
        }
    }
}
