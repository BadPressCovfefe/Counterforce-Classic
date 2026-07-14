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

    public DistancedEntityView(Context context, MainActivity activity, MapEntity entity, LaunchClientGame game)
    {
        super(context);

        inflate(context, R.layout.view_distanced_entity, this);

        Setup(activity, entity, game);

        findViewById(R.id.txtLocation).setVisibility(GONE);
    }

    void Setup(MainActivity activity, MapEntity entity, LaunchClientGame game)
    {
        ImageView imgType = findViewById(R.id.imgType);
        ImageView imgOwner = findViewById(R.id.imgOwner);
        TextView txtEntityName = findViewById(R.id.txtEntityName);

        Player owner = null;

        if(entity instanceof Player)
        {
            imgType.setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, (Player)entity));
            txtEntityName.setText(((Player)entity).GetName());
            imgType.setBackground(null);
        }
        else if(entity instanceof Loot)
        {
            Loot loot = ((Loot)entity);

            imgType.setImageBitmap(EntityIconBitmaps.GetLootBitmap(activity, loot));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
        }
        else if(entity instanceof Rubble)
        {
            imgType.setImageResource(EntityIconBitmaps.GetEntityImageResource(game, entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
        }
        else
        {
            imgType.setImageResource(EntityIconBitmaps.GetEntityImageResource(game, entity));
            txtEntityName.setText(TextUtilities.GetEntityTypeAndName(entity, game));
            owner = game.GetPlayer(entity.GetOwnerID());
        }

        if(owner != null)
        {
            imgOwner.setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, owner));
        }
    }
}
