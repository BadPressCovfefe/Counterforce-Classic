package com.apps.fast.launch.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import androidx.core.graphics.ColorUtils;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;

import java.util.HashMap;
import java.util.Map;

import launch.game.Alliance;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Interceptor;
import launch.game.entities.KOTH;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Loot;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.entities.Rubble;
import launch.game.entities.Shipyard;
import launch.game.types.InterceptorType;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;
import launch.game.LaunchGame.Allegiance;
import launch.game.types.TorpedoType;

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
    private static final Bitmap[] RubbleBitmaps = new Bitmap[Allegiance.values().length];

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

    public static Bitmap GetRubbleBitmap(Context context, LaunchClientGame game, Rubble rubble)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), rubble);

        return GetTintedResBitmap(context, R.drawable.marker_rubble, RubbleBitmaps, allegiance);
    }

    public static Bitmap GetShipyardBitmap(Context context, LaunchClientGame game, Shipyard shipyard)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), shipyard);

        return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_shipyard), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static Bitmap GetMissileBitmap(MainActivity activity, LaunchClientGame game, MissileType type, Allegiance allegiance, int lAssetID)
    {
        Context context = activity;

        Bitmap bitmap = CheckAndGetCustomBitmap(activity, game, lAssetID, allegiance);

        if(bitmap != null && type.GetECM())
        {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
        }

        return bitmap == null ? LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missile), LaunchUICommon.AllegianceColours[allegiance.ordinal()]) : bitmap;
    }

    public static Bitmap GetTorpedoBitmap(MainActivity activity, LaunchClientGame game, TorpedoType type, Allegiance allegiance, int lAssetID)
    {
        Context context = activity;

        return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_torpedo), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static Bitmap GetInterceptorBitmap(MainActivity activity, LaunchClientGame game, InterceptorType type, Allegiance allegiance, int lAssetID)
    {
        Context context = activity;

        Bitmap bitmap = CheckAndGetCustomBitmap(activity, game, lAssetID, allegiance);

        //Return the result if we have one, otherwise fall through to returning a default icon.
        if(bitmap != null && activity.GetTheme() != ClientDefs.THEME_CLASSIC)
        {
            if(type.GetNuclear())
            {
                Bitmap baseMap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(baseMap);
                canvas.drawBitmap(bitmap, 32, 32, null);
                canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

                return baseMap;
            }
            else
                return bitmap;
        }

        if(type.GetNuclear())
        {
            bitmap = GetTintedResBitmap(activity, R.drawable.marker_interceptor_classic, DefaultInterceptorBitmaps, allegiance);
            Bitmap baseMap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(baseMap);
            canvas.drawBitmap(bitmap, 32, 32, null);
            canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

            return baseMap;
        }
        else
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

    public static Bitmap GetLootBitmap(Context context, Loot loot)
    {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_loot);
    }

    public static Bitmap GetAircraftMarker(Context context, LaunchClientGame game, AirplaneInterface aircraft)
    {
        Allegiance allegiance;

        if(aircraft != null)
        {
            allegiance = game.GetAllegiance(game.GetOurPlayer(), aircraft.GetAirplane());

            Bitmap bitmap = GetAircraftRoleMarkerBitmap(context, game.GetOwner((LaunchEntity)aircraft), aircraft.GetAircraftType());

            if(bitmap != null)
            {
                return LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    public static Bitmap GetAircraftOverlay(Context context, LaunchClientGame game, AirplaneInterface aircraft)
    {
        Allegiance allegiance;

        if(aircraft != null)
        {
            allegiance = game.GetAllegiance(game.GetOurPlayer(), aircraft.GetAirplane());

            Bitmap bitmap = GetAircraftRoleOverlayBitmap(context, game.GetOwner((LaunchEntity)aircraft), aircraft.GetAircraftType());

            if(bitmap != null)
            {
                return LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    public static Bitmap GetAircraftRoleMarkerBitmap(Context context, Player owner, EntityType aircraftType)
    {
        Bitmap bitmap = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(ClientDefs.SETTINGS_DYNAMIC_MARKERS, ClientDefs.SETTINGS_DYNAMIC_MARKERS_DEFAULT))
        {
            switch(aircraftType)
            {
                case BOMBER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bomber_strategic); break;
                case FIGHTER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_fighter_strategic); break;
                case SSB: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ssb_strategic); break;
                case REFUELER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_refueler_strategic); break;
                case MULTI_ROLE: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_multi_role_strategic); break;
                case ATTACK_AIRCRAFT: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_strategic); break;

                default: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.todo); break;
            }
        }
        else
        {
            switch(aircraftType)
            {
                case BOMBER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bomber); break;
                case FIGHTER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_fighter); break;
                case SSB: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ssb); break;
                case REFUELER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_tanker); break;
                case MULTI_ROLE: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_multi_role); break;
                case ATTACK_AIRCRAFT: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_aircraft); break;

                default: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.todo); break;
            }
        }

        return bitmap;
    }

    public static Bitmap GetAircraftRoleOverlayBitmap(Context context, Player owner, EntityType aircraftType)
    {
        Bitmap bitmap = null;

        switch(aircraftType)
        {
            case BOMBER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bomber); break;
            case FIGHTER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_fighter); break;
            case SSB: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ssb); break;
            case REFUELER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_tanker); break;
            case MULTI_ROLE: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_multi_role); break;
            case ATTACK_AIRCRAFT: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_aircraft); break;

            default: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.todo); break;
        }

        return bitmap;
    }

    public static Bitmap GetOwnedNavalMarkerBitmap(Context context, LaunchClientGame game, NavalVessel vessel)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), vessel);

        return LaunchUICommon.TintBitmap(GetNavalMarkerBitmap(context, vessel.GetEntityType()), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static Bitmap GetOwnedNavalOverlayBitmap(Context context, LaunchClientGame game, NavalVessel vessel)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), vessel);

        return LaunchUICommon.TintBitmap(GetNavalOverlayBitmap(context, vessel.GetEntityType()), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static Bitmap GetKOTHBitmap(Context context, LaunchClientGame game)
    {
        Allegiance allegiance = Allegiance.UNAFFILIATED;
        KOTH kingOfTheHill = game.GetKOTH();

        if(kingOfTheHill != null)
        {
            if(!kingOfTheHill.GetEmpty() && !kingOfTheHill.GetContested())
            {
                if(kingOfTheHill.GetOccupiedByAlliance())
                {
                    Alliance alliance = game.GetAlliance(kingOfTheHill.GetKingID());

                    if(alliance != null)
                    {
                        allegiance = game.GetAllegiance(game.GetOurPlayer(), alliance);
                    }
                }
                else
                {
                    Player player = game.GetPlayer(kingOfTheHill.GetKingID());

                    if(player != null)
                    {
                        allegiance = game.GetAllegiance(game.GetOurPlayer(), player);
                    }
                }
            }
        }

        return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_koth), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static int GetKOTHCircleColor(Context context, LaunchClientGame game)
    {
        Allegiance allegiance = Allegiance.UNAFFILIATED;
        KOTH kingOfTheHill = game.GetKOTH();

        if(kingOfTheHill != null)
        {
            if(!kingOfTheHill.GetEmpty() && !kingOfTheHill.GetContested())
            {
                if(kingOfTheHill.GetOccupiedByAlliance())
                {
                    Alliance alliance = game.GetAlliance(kingOfTheHill.GetKingID());

                    if(alliance != null)
                    {
                        allegiance = game.GetAllegiance(game.GetOurPlayer(), alliance);
                    }
                }
                else
                {
                    Player player = game.GetPlayer(kingOfTheHill.GetKingID());

                    if(player != null)
                    {
                        allegiance = game.GetAllegiance(game.GetOurPlayer(), player);
                    }
                }
            }
        }

        int color = LaunchUICommon.AllegianceColours[allegiance.ordinal()];
        return ColorUtils.setAlphaComponent(color, 96); // 128 = 50% alpha
    }

    public static Bitmap GetNavalMarkerBitmap(Context context, EntityType type)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(ClientDefs.SETTINGS_DYNAMIC_MARKERS, ClientDefs.SETTINGS_DYNAMIC_MARKERS_DEFAULT))
        {
            switch(type)
            {
                case FRIGATE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_frigate_strategic);
                case DESTROYER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_destroyer_strategic);
                case SUPER_CARRIER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_carrier_strategic);
                case ATTACK_SUB: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_sub_strategic);
                case SSBN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ssbn_strategic);
            }
        }
        else
        {
            switch(type)
            {
                case FRIGATE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_frigate);
                case DESTROYER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_destroyer);
                case SUPER_CARRIER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_carrier);
                case ATTACK_SUB: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_sub);
                case SSBN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ssbn);
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    public static Bitmap GetNavalOverlayBitmap(Context context, EntityType type)
    {
        switch(type)
        {
            case FRIGATE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_frigate);
            case DESTROYER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_destroyer);
            case SUPER_CARRIER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_carrier);
            case ATTACK_SUB: return BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_attack_sub);
            case SSBN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_ssbn);
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    public static int GetEntityImageResource(LaunchClientGame game, MapEntity entity)
    {
        if(entity instanceof Missile)
        {
            Missile missile = (Missile)entity;

            MissileType missileType = game.GetConfig().GetMissileType(missile.GetType());

            if(missileType.GetICBM())
            {
                if(missileType.GetSubmarineLaunched())
                {
                    return R.drawable.image_slbm;
                }
                else
                {
                    return R.drawable.image_icbm;
                }
            }
            else
            {
                return R.drawable.image_missile;
            }
        }
        else if(entity instanceof Interceptor)
        {
            Interceptor interceptor = (Interceptor)entity;

            InterceptorType interceptorType = game.GetConfig().GetInterceptorType(interceptor.GetType());

            if(interceptorType.GetABM())
            {
                return R.drawable.image_abm;
            }
            else
            {
                return R.drawable.image_interceptor;
            }
        }

        switch(entity.GetEntityType())
        {
            case MISSILE_SITE: return R.drawable.build_missile_site;
            case NUCLEAR_MISSILE_SITE: return R.drawable.build_icbm_silo;
            case SAM_SITE: return R.drawable.build_sam_site;
            case ABM_SILO: return R.drawable.build_abm_site;
            case TANK: return R.drawable.build_tank;
            case COMMAND_POST: return R.drawable.build_bunker;
            case WAREHOUSE: return R.drawable.build_bank;
            case LOGISTICS_DEPOT: return R.drawable.build_logistics_depot;
            case SOLAR_PANEL: return R.drawable.build_solar_panel;
            case FARM: return R.drawable.build_farm;
            case ORE_MINE: return R.drawable.build_ore_mine;
            case AIRBASE: return R.drawable.build_airbase;
            case ARTILLERY_GUN: return R.drawable.build_artillery_gun;
            case SENTRY_GUN: return R.drawable.build_sentry_gun;
            case ARMORY: return R.drawable.build_armory;
            case FRIGATE: return R.drawable.build_frigate;
            case DESTROYER: return R.drawable.build_destroyer;
            case SUPER_CARRIER: return R.drawable.build_super_carrier;
            case ATTACK_SUB: return R.drawable.build_attack_sub;
            case SSBN: return R.drawable.build_ssbn;
            case FIGHTER: return R.drawable.build_fighter;
            case BOMBER: return R.drawable.build_bomber;
            case REFUELER: return R.drawable.build_refueler;
            case ATTACK_AIRCRAFT: return R.drawable.build_ground_attack;
            case SSB: return R.drawable.build_ssb;
            case MULTI_ROLE: return R.drawable.build_multi_role;
            case RUBBLE: return R.drawable.image_rubble;
            case SHIPYARD: return R.drawable.image_shipyard;
            case TORPEDO: return R.drawable.image_torpedo;
        }

        return R.drawable.todo;
    }
}
