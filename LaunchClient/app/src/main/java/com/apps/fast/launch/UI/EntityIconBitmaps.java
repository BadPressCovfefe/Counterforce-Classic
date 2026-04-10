package com.apps.fast.launch.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.map.LaunchClusterItem;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.google.maps.android.clustering.Cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.Airdrop;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Loot;
import launch.game.entities.NavalVessel;
import launch.game.entities.Player;
import launch.game.entities.Rubble;
import launch.game.entities.Ship;
import launch.game.entities.Shipyard;
import launch.game.entities.Submarine;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;
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

        if(shipyard.GetPortOnly())
        {
            if(shipyard.Destroyed())
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_port_destroyed), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_port), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }
        else
        {
            if(shipyard.Destroyed())
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_shipyard_destroyed), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(shipyard.GetProducing())
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_shipyard_producing), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_shipyard_not_producing), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }
    }

    public static Bitmap GetMissileBitmap(MainActivity activity, LaunchClientGame game, MissileType type, Allegiance allegiance, int lAssetID)
    {
        Context context = activity;

        Bitmap bitmap = CheckAndGetCustomBitmap(activity, game, lAssetID, allegiance);

        if(bitmap != null && activity.GetTheme() != ClientDefs.THEME_CLASSIC)
        {
            if(type.GetECM())
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

                Bitmap baseMap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(baseMap);
                canvas.drawBitmap(classicBitmap, 32, 32, null);

                return baseMap;
            }
            else if(type.GetStealth())
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_classic_missile_stealth, DefaultMissileBitmaps, allegiance);

                if(type.GetECM())
                {
                    Canvas canvas = new Canvas(classicBitmap);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
                }

                return classicBitmap;
            }
            else if(type.GetArtillery())
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_shell_classic, DefaultMissileBitmaps, allegiance);

                return classicBitmap;
            }
            else
            {
                classicBitmap = GetTintedResBitmap(activity, R.drawable.marker_classic_missile, DefaultMissileBitmaps, allegiance);

                if(type.GetECM())
                {
                    Canvas canvas = new Canvas(classicBitmap);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ecm), 0, 0, null);
                }

                return classicBitmap;
            }
        }
    }

    public static Bitmap GetTorpedoBitmap(MainActivity activity, LaunchClientGame game, TorpedoType type, Allegiance allegiance, int lAssetID)
    {
        Context context = activity;

        if(activity.GetTheme() != ClientDefs.THEME_CLASSIC)
        {
            return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_torpedo), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }
        else
        {
            return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_torpedo_classic), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }
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
        if(loot.GetLootType() != CargoSystem.LootType.RESOURCES)
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_container);
        else
            return GetResourceTypeBitmap(context, ResourceType.values()[loot.GetCargoID()]);
    }

    public static Bitmap GetAirdropBitmap(Context context, Airdrop airdrop)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_airdrop);

        return LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
    }

    public static Bitmap GetAircraftBitmap(Context context, LaunchClientGame game, AirplaneInterface aircraft)
    {
        Allegiance allegiance;

        if(aircraft instanceof AirplaneInterface)
        {
            AirplaneInterface airplane = (AirplaneInterface)aircraft;

            if(airplane.GetOwnerID() == game.GetOurPlayerID() || !airplane.GetStealth())
                allegiance = game.GetAllegiance(game.GetOurPlayer(), airplane.GetAirplane());
            else
                allegiance = Allegiance.NEUTRAL;

            Bitmap bitmap = GetAircraftRoleBitmap(context, game.GetOwner((LaunchEntity)airplane), airplane.GetAircraftType());

            if(bitmap != null)
            {
                bitmap = LaunchUICommon.TintBitmap(bitmap, LaunchUICommon.AllegianceColours[allegiance.ordinal()]);

                if(airplane.Flying() && game.AircraftIsNuclearArmed(aircraft) && !airplane.GetStealth())
                {
                    Bitmap baseMap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(baseMap);
                    canvas.drawBitmap(bitmap, 32, 32, null);
                    canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_nuclear), 0, 0, null);

                    return baseMap;
                }

                return bitmap;
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    //TODO: when adding custom designs, this method needs to have a parameter for "icontype."
    public static Bitmap GetAircraftRoleBitmap(Context context, Player owner, EntityType aircraftType)
    {
        Bitmap bitmap = null;

        switch(aircraftType)
        {
            case BOMBER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bomber); break;
            case FIGHTER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_fighter_default); break;
            case STEALTH_FIGHTER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_stealth_fighter_default); break;
            case STEALTH_BOMBER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_stealth_bomber_default); break;
            case SSB: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_fast_bomber); break;
            case AWACS: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_awacs); break;
            case REFUELER: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_tanker); break;
            case CARGO_PLANE: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_cargoplane); break;
            case MULTI_ROLE: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_strikefighter); break;
            case ATTACK_AIRCRAFT: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_attack_aircraft); break;

            default: bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.todo); break;
        }

        return bitmap;
    }

    public static Bitmap GetOwnedNavalBitmap(Context context, LaunchClientGame game, NavalVessel vessel)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), vessel);

        return LaunchUICommon.TintBitmap(GetNavalBitmap(context, vessel.GetEntityType()), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
    }

    public static Bitmap GetNavalBitmap(Context context, EntityType type)
    {
        switch(type)
        {
            case FRIGATE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_frigate);
            case DESTROYER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_destroyer);
            case AMPHIB: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_amphib);
            case CARGO_SHIP: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_cargo);
            case FLEET_OILER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_fleet_oiler);
            case SUPER_CARRIER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_ship_supercarrier);
            case ATTACK_SUB: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_submarine_small);
            case SSBN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_submarine_medium);
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
    }

    public static Bitmap GetResourceBitmap(Context context)
    {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_resources);
    }

    public static Bitmap GetResourceTypeBitmap(Context context, ResourceType type)
    {
        switch(type)
        {
            case OIL: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_oil);
            case IRON: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_iron);
            case COAL: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_coal);
            case CROPS: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_crops);
            case URANIUM: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_uranium);
            case GOLD: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_gold);
            case ELECTRICITY: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_electricity);
            case WEALTH: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_wealth);
            case CONCRETE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_concrete);
            case LUMBER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_lumber);
            case FOOD: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_food);
            case FUEL: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_fuel);
            case STEEL: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_steel);
            case CONSTRUCTION_SUPPLIES: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_construction_supplies);
            case MACHINERY: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_machinery);
            case ELECTRONICS: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_electronics);
            case MEDICINE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_medicine);
            case ENRICHED_URANIUM: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_fissile_material);
            default: return BitmapFactory.decodeResource(context.getResources(), R.drawable.resource_icon_not_found);
        }
    }
}
