package com.apps.fast.launch.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.apps.fast.launch.R;
import com.apps.fast.launch.components.Utilities;

import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.Bank;
import launch.game.entities.Armory;
import launch.game.entities.CommandPost;
import launch.game.entities.MissileFactory;
import launch.game.entities.Distributor;
import launch.game.entities.Processor;
import launch.game.entities.ScrapYard;
import launch.game.entities.Warehouse;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.RadarStation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

import launch.game.LaunchGame.Allegiance;

/**
 * A utility class that shades structure bitmaps by allegiance, combines the power status, and caches the bitmaps in RAM.
 */
public class StructureIconBitmaps
{
    private enum StructureIndexRunStatus
    {
        ONLINE,
        OFFLINE,
        BOOTING,
        SELLING
    }

    private static final Bitmap[] MissileSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] NuclearMissileSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] SAMSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] SentryGunBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] WatchTowerBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] OreMineBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] RadarStationBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ABMSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] CommandPostBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] AirbaseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] OpenAirbaseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ArmoryBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] BarracksBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] BankBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] WarehouseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] MissileFactoryBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ProcessorBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] DistributorBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ArtilleryGunBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ScrapYardBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];

    private static void GenerateStructureBitmap(Context context, Bitmap[] Container, int lIndex, Allegiance allegiance, StructureIndexRunStatus runStatus, int lRes)
    {
        Bitmap icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), lRes), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        Bitmap overlay = null;

        switch(runStatus)
        {
            case SELLING: overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_selling); break;
            case OFFLINE: overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_offline); break;
            case BOOTING: overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_booting); break;
            case ONLINE: overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_online); break;
        }

        Bitmap bitmap = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(icon, new Matrix(), null);
        canvas.drawBitmap(overlay, 0, 0, null);

        Container[lIndex] = bitmap;
    }

    public static Bitmap GetStructureBitmap(Context context, LaunchClientGame game, Structure structure)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), structure);
        StructureIndexRunStatus runStatus;

        if(structure.GetSelling())
            runStatus = StructureIndexRunStatus.SELLING;
        else if(structure.GetOffline())
            runStatus = StructureIndexRunStatus.OFFLINE;
        else if(structure.GetBooting())
            runStatus = StructureIndexRunStatus.BOOTING;
        else
            runStatus = StructureIndexRunStatus.ONLINE;

        int lIndex = (allegiance.ordinal() * StructureIndexRunStatus.values().length) + runStatus.ordinal();

        if(structure instanceof MissileSite)
        {
            if(((MissileSite)structure).CanTakeICBM() && structure.GetOffline())
            {
                if(NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke_offline);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else if(((MissileSite)structure).CanTakeICBM() && structure.GetBooting())
            {
                if(NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke_booting);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else if(((MissileSite)structure).CanTakeICBM())
            {
                if (NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else
            {
                if(MissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, MissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesite);
                }

                return MissileSiteBitmaps[lIndex];
            }
        }

        if(structure instanceof SAMSite)
        {
            if(((SAMSite)structure).GetIsABMSilo() && structure.GetOffline())
            {
                if (ABMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ABMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_abmsite_offline);
                }

                return ABMSiteBitmaps[lIndex];
            }
            else if(((SAMSite)structure).GetIsABMSilo())
            {
                if (ABMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ABMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_abmsite_online);
                }

                return ABMSiteBitmaps[lIndex];
            }
            else
            {
                if(SAMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, SAMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_samsite);
                }

                return SAMSiteBitmaps[lIndex];
            }
        }

        if(structure instanceof SentryGun)
        {
            SentryGun gun = (SentryGun)structure;

            if(!gun.GetIsWatchTower())
            {
                if(SentryGunBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, SentryGunBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_sentry);
                }

                return SentryGunBitmaps[lIndex];
            }
            else
            {
                if(WatchTowerBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, WatchTowerBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_artillery_gun);
                }

                return WatchTowerBitmaps[lIndex];
            }
        }

        if(structure instanceof ArtilleryGun)
        {
            if(ArtilleryGunBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, ArtilleryGunBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_artillery_gun);
            }

            return ArtilleryGunBitmaps[lIndex];
        }

        if(structure instanceof OreMine)
        {
            if(OreMineBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, OreMineBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_oremine);
            }

            return OreMineBitmaps[lIndex];
        }

        if(structure instanceof RadarStation)
        {
            if(RadarStationBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, RadarStationBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_radarstation);
            }

            return RadarStationBitmaps[lIndex];
        }

        if(structure instanceof CommandPost)
        {
            if(CommandPostBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, CommandPostBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_command_post);
            }

            return CommandPostBitmaps[lIndex];
        }

        if(structure instanceof Airbase)
        {
            Airbase airbase = (Airbase)structure;

            if(airbase.GetAircraftSystem().GetOpen())
            {
                if(OpenAirbaseBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, OpenAirbaseBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_airbase_open);
                }

                return OpenAirbaseBitmaps[lIndex];
            }
            else
            {
                if(AirbaseBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, AirbaseBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_airbase);
                }

                return AirbaseBitmaps[lIndex];
            }
        }

        if(structure instanceof Armory)
        {
            Armory armory = (Armory)structure;

            if(armory.GetIsBarracks())
            {
                if(BarracksBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, BarracksBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_barracks);
                }

                return BarracksBitmaps[lIndex];
            }
            else
            {
                if(ArmoryBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ArmoryBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_armory);
                }

                return ArmoryBitmaps[lIndex];
            }
        }

        if(structure instanceof Bank)
        {
            if(BankBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, BankBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_bank);
            }

            return BankBitmaps[lIndex];
        }

        if(structure instanceof Warehouse)
        {
            if(WarehouseBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, WarehouseBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_warehouse);
            }

            return WarehouseBitmaps[lIndex];
        }

        if(structure instanceof Processor)
        {
            if(ProcessorBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, ProcessorBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_processor);
            }

            return ProcessorBitmaps[lIndex];
        }

        if(structure instanceof Distributor)
        {
            if(DistributorBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, DistributorBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_distributor);
            }

            return DistributorBitmaps[lIndex];
        }

        if(structure instanceof ScrapYard)
        {
            if(ScrapYardBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, ScrapYardBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_scrap_yard);
            }

            return ScrapYardBitmaps[lIndex];
        }

        return null;
    }

    public static Bitmap GetStructureTypeBitmap(Context context, EntityType type)
    {
        switch(type)
        {
            case MISSILE_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case SAM_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case BANK: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bank), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case COMMAND_POST: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_command_post), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case AIRBASE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_airbase), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ABM_SILO: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_offline), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ORE_MINE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_oremine), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case SENTRY_GUN: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case WATCH_TOWER: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_artillery_gun), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case RADAR_STATION: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_radarstation), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case WAREHOUSE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_warehouse), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case NUCLEAR_MISSILE_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ARTILLERY_GUN: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_artillery_gun), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ARMORY: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_armory), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case BARRACKS: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_barracks), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case SCRAP_YARD: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_scrap_yard), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case PROCESSOR: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_processor), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case DISTRIBUTOR: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_distributor), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            default: return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
        }
    }

    public static Bitmap GetBlueprintBitmap(Context context, LaunchClientGame game, EntityType structureType)
    {
        switch(structureType)
        {
            case MISSILE_SITE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_missilesite);
            case SAM_SITE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_samsite);
            case BANK: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_bank);
            case COMMAND_POST: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_command_post);
            case AIRBASE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_airbase);
            case ABM_SILO: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_abmsite);
            case ORE_MINE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_oremine);
            case SENTRY_GUN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_sentry);
            case WATCH_TOWER: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_artillery_gun);
            case RADAR_STATION: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_radarstation);
            case WAREHOUSE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_logisticsdepot);
            case NUCLEAR_MISSILE_SITE: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_missilesitenuke);
            case ARTILLERY_GUN: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_artillery_gun);
            case ARMORY: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_armory);
            case BARRACKS: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_barracks);
            case SCRAP_YARD: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_scrap_yard);
            case PROCESSOR: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_processor);
            case DISTRIBUTOR: return BitmapFactory.decodeResource(context.getResources(), R.drawable.blueprint_distributor);
            default: return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
        }
    }
}
