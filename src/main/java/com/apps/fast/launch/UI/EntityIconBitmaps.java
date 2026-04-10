package com.apps.fast.launch.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.Interceptor;
import launch.game.entities.Loot;
import launch.game.entities.Missile;
import launch.game.entities.Player;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;
import launch.game.LaunchGame.Allegiance;

/**
 * A utility class that shades default and custom entity bitmaps by allegiance, and caches them in RAM.
 */
public class EntityIconBitmaps
{
    private static final Bitmap[] DefaultPlayerBitmaps = new Bitmap[Allegiance.values().length];
    private static final Bitmap[] DeadPlayerBitmaps = new Bitmap[Allegiance.values().length];
    private static final Bitmap[] DefaultMissileBitmaps = new Bitmap[Allegiance.values().length];
    private static final Bitmap[] DefaultNukeBitmaps = new Bitmap[Allegiance.values().length];
    private static final Bitmap[] DefaultInterceptorBitmaps = new Bitmap[Allegiance.values().length];

    private static final Map<Integer, Bitmap[]> CustomAssets = new HashMap<>();

    public static Bitmap GetDefaultPlayerBitmap(Context context, LaunchClientGame game, Player player)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), player);
        return GetTintedResBitmap(context, R.drawable.marker_player, DefaultPlayerBitmaps, allegiance);
    }

    public static Bitmap GetDeadPlayerBitmap(Context context, LaunchClientGame game, Player player)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), player);
        return GetTintedResBitmap(context, R.drawable.marker_player_dead, DeadPlayerBitmaps, allegiance);
    }

    public static Bitmap GetMissileBitmap(MainActivity activity, LaunchClientGame game, Missile missile, int lAssetID)
    {
        MissileType type = game.GetConfig().GetMissileType(missile.GetType());
        Allegiance allegiance;
        Context context = activity;

        if(type.GetStealth())
        {
            allegiance = Allegiance.NEUTRAL;
        }
        else
        {
            allegiance = game.GetAllegiance(game.GetOurPlayer(), missile);
        }

        Bitmap bitmap = CheckAndGetCustomBitmap(activity, game, lAssetID, allegiance);

        if(bitmap != null && activity.GetTheme() != ClientDefs.THEME_CLASSIC)
        {
            if(type.GetICBM())
            {
                Bitmap baseMap = Bitmap.createBitmap(128,128,bitmap.getConfig());
                Canvas canvas = new Canvas(baseMap);
                canvas.drawBitmap(bitmap, 32, 0, null);
                canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_icbm), 0, 0, null);

                return baseMap;
            }
            else if(type.GetNuclear())
            {
                Bitmap baseMap = Bitmap.createBitmap(128, 128, bitmap.getConfig());
                Canvas canvas = new Canvas(baseMap);
                canvas.drawBitmap(bitmap, 32, 32, null);
                canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

                if(type.GetECM())
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 32, 32, null);

                return baseMap;
            }
            else if(type.GetECM())
            {
                Canvas canvas = new Canvas(bitmap);
                canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
            }

            return bitmap;
        }
        else
        {
            Bitmap classicBitmap;

            if(type.GetICBM())
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_missilenuke_classic, DefaultNukeBitmaps, allegiance);

                Bitmap baseMap = Bitmap.createBitmap(128, 128, classicBitmap.getConfig());
                Canvas canvas = new Canvas(baseMap);
                canvas.drawBitmap(classicBitmap, 32, 32, null);

                return baseMap;
            }
            else if(type.GetStealth())
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_classic_missile_stealth, DefaultMissileBitmaps, allegiance);

                if(type.GetNuclear())
                {
                    Bitmap baseMap = Bitmap.createBitmap(128, 128, classicBitmap.getConfig());
                    Canvas canvas = new Canvas(baseMap);
                    canvas.drawBitmap(classicBitmap, 32, 32, null);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

                    if(type.GetECM())
                        canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 32, 32, null);

                    return baseMap;
                }
                else if(type.GetECM())
                {
                    Canvas canvas = new Canvas(classicBitmap);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
                }

                return classicBitmap;
            }
            else
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_classic_missile, DefaultMissileBitmaps, allegiance);

                if(type.GetNuclear())
                {
                    Bitmap baseMap = Bitmap.createBitmap(128, 128, classicBitmap.getConfig());
                    Canvas canvas = new Canvas(baseMap);
                    canvas.drawBitmap(classicBitmap, 32, 32, null);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

                    if(type.GetECM())
                        canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 32, 32, null);

                    return baseMap;
                }
                else if(type.GetECM())
                {
                    Canvas canvas = new Canvas(classicBitmap);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
                }

                return classicBitmap;
            }
        }
    }

    public static Bitmap GetInterceptorBitmap(MainActivity activity, LaunchClientGame game, Interceptor interceptor, int lAssetID)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), interceptor);

        Bitmap bitmap = CheckAndGetCustomBitmap(activity, game, lAssetID, allegiance);

        //Return the result if we have one, otherwise fall through to returning a default icon.
        if(bitmap != null && activity.GetTheme() != ClientDefs.THEME_CLASSIC)
            return bitmap;

        return GetTintedResBitmap(activity, R.drawable.marker_interceptor_classic, DefaultInterceptorBitmaps, allegiance);
    }

    private static Bitmap GetTintedResBitmap(Context context, int lResID, Bitmap[] Container, Allegiance allegiance)
    {
        int lIndex = allegiance.ordinal();

        if(Container[lIndex] == null)
        {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), lResID);
            bitmap = LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            Container[lIndex] = bitmap;
        }

        return Container[lIndex];
    }

    /**
     * First stage check and get custom assets. Creates the bitmap array in the custom asset container if it doesn't already exist, and returns the nullable result from GetTintedCustomBitmap.
     * @param activity MainActivity reference.
     * @param lAssetID Asset ID.
     * @param allegiance Relationship to the player for tinting purposes.
     * @return The result from GetTintedCustomBitmap, which may be null if we don't have the bitmap and the default must be used until one is downloaded, or if the default asset ID was specified.
     */
    private static Bitmap CheckAndGetCustomBitmap(MainActivity activity, LaunchClientGame game, int lAssetID, Allegiance allegiance)
    {
        //Default asset, return null?
        if(lAssetID == LaunchType.ASSET_ID_DEFAULT)
        {
            return null;
        }

        //Create container for allegiance-tinted bitmaps for this asset.
        if(!CustomAssets.containsKey(lAssetID))
        {
            CustomAssets.put(lAssetID, new Bitmap[Allegiance.values().length]);
        }

        return GetTintedCustomBitmap(activity, game, lAssetID, CustomAssets.get(lAssetID), allegiance);
    }

    /**
     * Gets a tinted custom (server-stored) bitmap, or instigates a download for it if we don't yet have it.
     * @param activity MainActivity reference.
     * @param lAssetID ID of the asset.
     * @param Container Container in which to store the asset if it hasn't yet been tinted.
     * @param allegiance Relationship to the player for tinting purposes.
     * @return The tinted bitmap, or null if we don't have it and have had to download it.
     */
    private static Bitmap GetTintedCustomBitmap(MainActivity activity, LaunchClientGame game, int lAssetID, Bitmap[] Container, Allegiance allegiance)
    {
        int lIndex = allegiance.ordinal();

        if(Container[lIndex] == null)
        {
            Bitmap bitmap = ImageAssets.GetImageAsset(activity, game, lAssetID);

            //We haven't yet downloaded the image.
            if(bitmap == null)
                return null;

            bitmap = LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            Container[lIndex] = bitmap;
        }

        return Container[lIndex];
    }
}
