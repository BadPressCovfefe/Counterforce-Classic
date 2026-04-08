package com.apps.fast.launch.views;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.StructureIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.*;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 09/11/15.
 */
public class DistancedEntityView extends FrameLayout
{
    public DistancedEntityView(Context context, MainActivity activity, MapEntity entity, GeoCoord geoFrom, LaunchClientGame game)
    {
        super(context);

        inflate(context, R.layout.view_distanced_entity, this);

        Setup(activity, entity, game);

        TextView txtLocation = findViewById(R.id.txtLocation);

        float fltDistance = geoFrom.DistanceTo(entity.GetPosition());
        double dblDirection = geoFrom.BearingTo(entity.GetPosition());

        txtLocation.setText(TextUtilities.GetDistanceStringFromKM(fltDistance) + " " + TextUtilities.QualitativeDirectionFromBearing(dblDirection));
    }

    public DistancedEntityView(Context context, MainActivity activity, LaunchEntity entity, LaunchClientGame game)
    {
        super(context);

        inflate(context, R.layout.view_distanced_entity, this);

        Setup(activity, entity, game);

        findViewById(R.id.txtLocation).setVisibility(GONE);
    }

    void Setup(MainActivity activity, LaunchEntity entity, LaunchClientGame game)
    {
        ImageView imgType = findViewById(R.id.imgType);
        ImageView imgOwner = findViewById(R.id.imgOwner);
        TextView txtEntityName = findViewById(R.id.txtEntityName);

        Player owner = null;

        if(entity instanceof Player)
        {
            imgType.setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, (Player)entity));
            txtEntityName.setText(((Player)entity).GetName());
        }
        else if(entity instanceof Structure)
        {
            imgType.setImageBitmap(StructureIconBitmaps.GetStructureBitmap(activity, game, (Structure)entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(entity.GetOwnerID());
        }
        else if(entity instanceof Missile)
        {
            Missile missile = ((Missile)entity);
            MissileType type = game.GetConfig().GetMissileType(missile.GetType());
            imgType.setImageBitmap(EntityIconBitmaps.GetMissileBitmap(activity, game, type, game.GetAllegiance(game.GetOurPlayer(), missile), type.GetAssetID()));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = type.GetStealth() ? null : game.GetPlayer(((Missile)entity).GetOwnerID());
        }
        else if(entity instanceof Interceptor)
        {
            Interceptor interceptor = ((Interceptor)entity);
            InterceptorType type = game.GetConfig().GetInterceptorType(interceptor.GetType());
            imgType.setImageBitmap(EntityIconBitmaps.GetInterceptorBitmap(activity, game, type, game.GetAllegiance(game.GetOurPlayer(), interceptor), type.GetAssetID()));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(((Interceptor)entity).GetOwnerID());
        }
        else if(entity instanceof Loot)
        {
            Loot loot = ((Loot)entity);

            imgType.setImageBitmap(EntityIconBitmaps.GetLootBitmap(activity, loot));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
        }
        else if(entity instanceof ResourceDeposit)
        {
            ResourceDeposit deposit = (ResourceDeposit)entity;

            imgType.setImageBitmap(EntityIconBitmaps.GetResourceBitmap(activity));
            txtEntityName.setText(TextUtilities.GetResourceDepositTitle(deposit));
        }
        else if(entity instanceof Rubble)
        {
            Rubble rubble = ((Rubble)entity);

            imgType.setImageBitmap(EntityIconBitmaps.GetRubbleBitmap(activity, game, rubble));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
        }
        else if(entity instanceof Airdrop)
        {
            imgType.setImageBitmap(EntityIconBitmaps.GetAirdropBitmap(activity, ((Airdrop)entity)));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
        }
        else if(entity instanceof Airplane)
        {
            imgType.setImageBitmap(EntityIconBitmaps.GetAircraftBitmap(activity, game, (Airplane)entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = ((Airplane)entity).GetStealth() ? null : game.GetPlayer(((Airplane)entity).GetOwnerID());
        }
        else if(entity instanceof Infantry)
        {
            imgType.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(activity, game, (Infantry)entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(((Infantry)entity).GetOwnerID());
        }
        else if(entity instanceof CargoTruck)
        {
            CargoTruck truck = (CargoTruck)entity;

            imgType.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(activity, game, (CargoTruck)entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(((CargoTruck)entity).GetOwnerID());
        }
        else if(entity instanceof Tank)
        {
            imgType.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(activity, game, (Tank)entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(((Tank)entity).GetOwnerID());
        }
        else if(entity instanceof Shipyard)
        {
            imgType.setImageBitmap(EntityIconBitmaps.GetShipyardBitmap(activity, game, (Shipyard)entity));
            txtEntityName.setText(((Shipyard)entity).GetName());
            owner = game.GetPlayer(((Shipyard)entity).GetOwnerID());
        }
        else if(entity instanceof NavalVessel)
        {
            imgType.setImageBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(activity, game, (NavalVessel)entity));
            txtEntityName.setText(TextUtilities.GetOwnedEntityName(entity, game));
            owner = game.GetPlayer(((NavalVessel)entity).GetOwnerID());
        }
        else
        {
            imgType.setImageResource(R.drawable.todo);
            txtEntityName.setText(String.format("Support for %s not implemented!", entity.getClass().getName()));
        }

        if(owner != null)
        {
            imgOwner.setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, owner));
        }
    }
}
