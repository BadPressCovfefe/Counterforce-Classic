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
    private static final Bitmap[] ABMSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] CommandPostBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] AirbaseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] OpenAirbaseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ArmoryBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] BankBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] WarehouseBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ArtilleryGunBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];

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
            if(((MissileSite)structure).CanTakeICBM())
            {
                if (NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_icbm_silo);
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
            if(((SAMSite)structure).GetIsABMSilo())
            {
                if(ABMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ABMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_abmsite);
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
                if(ArtilleryGunBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ArtilleryGunBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_artillery_gun);
                }

                return ArtilleryGunBitmaps[lIndex];
            }
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
            if(AirbaseBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, AirbaseBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_airbase);
            }

            return AirbaseBitmaps[lIndex];
        }

        if(structure instanceof Armory)
        {
            if(ArmoryBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, ArmoryBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_armory);
            }

            return ArmoryBitmaps[lIndex];
        }

        if(structure instanceof Warehouse)
        {
            if(WarehouseBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, WarehouseBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_bank);
            }

            return WarehouseBitmaps[lIndex];
        }

        return null;
    }

    public static Bitmap GetStructureTypeBitmap(Context context, EntityType type)
    {
        switch(type)
        {
            case MISSILE_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case SAM_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case COMMAND_POST: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_command_post), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case AIRBASE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_airbase), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ABM_SILO: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case SENTRY_GUN: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case WATCH_TOWER: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_artillery_gun), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case WAREHOUSE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bank), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case NUCLEAR_MISSILE_SITE: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_icbm_silo), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ARTILLERY_GUN: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_artillery_gun), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            case ARMORY: return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_armory), LaunchUICommon.AllegianceColours[Allegiance.YOU.ordinal()]);
            default: return BitmapFactory.decodeResource(context.getResources(), R.drawable.todo);
        }
    }
}
