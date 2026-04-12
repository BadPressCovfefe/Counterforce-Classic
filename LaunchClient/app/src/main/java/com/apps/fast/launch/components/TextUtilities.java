package com.apps.fast.launch.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

import com.apps.fast.launch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.types.TorpedoType;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.StoredDamagable;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.types.InterceptorType;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 05/11/15.
 */
public class TextUtilities
{
    private static Context context;

    public enum ShortUnits
    {
        METERS,
        YARDS,
        FEET
    }

    public enum LongUnits
    {
        KILOMETERS,
        STATUTE_MILES,
        NAUTICAL_MILES
    }

    public enum Speeds
    {
        KILOMETERS_PER_HOUR,
        MILES_PER_HOUR,
        KNOTS
    }

    public static final int UNITS_METRIC = 0;
    public static final int UNITS_IMPERIAL = 1;
    public static final int UNITS_MARINE = 2;

    public static final long A_DAY = 86400000;
    public static final long AN_HOUR = 3600000;
    public static final long A_MINUTE = 60000;
    public static final long A_SEC = 1000;

    private static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdfDayAndTime = new SimpleDateFormat("dd/MM HH:mm");
    private static final SimpleDateFormat sdfDayAndFullTime = new SimpleDateFormat("dd/MM HH:mm:ss");
    private static final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yy");

    private static final double COMPASS_DIVISIONS = (Math.PI * 2.0) / 8.0;
    private static final double COMPASS_DIV_OFFSET = COMPASS_DIVISIONS / 2.0;

    private static final float FRACTION_TO_PERCENT = 100.0f;

    private static final float KM_PER_METER = 0.001f;
    private static final float KM_PER_YARD = 0.0009144f;
    private static final float KM_PER_FOOT = 0.0003048f;
    private static final float KM_PER_MILE = 1.60934f;
    private static final float KM_PER_NM = 1.852f;
    private static final float KPH_PER_MPH = 1.60934f;
    private static final float KPH_PER_KNOT = 1.852f;

    private static final String FORMAT_STRING_METERS = "%d m";
    private static final String FORMAT_STRING_YARDS = "%d yd";
    private static final String FORMAT_STRING_FEET = "%d ft";
    private static final String FORMAT_STRING_KILOMETERS = "%.1f km";
    private static final String FORMAT_STRING_STATUTE_MILES = "%.1f mi";
    private static final String FORMAT_STRING_NAUTICAL_MILES = "%.1f nm";
    private static final String FORMAT_STRING_KPH = "%d km/h";
    private static final String FORMAT_STRING_MPH = "%d mph";
    private static final String FORMAT_STRING_KNOTS = "%d kts";
    private static final String FORMAT_STRING_KILOGRAMS = "%d kg";
    private static final String FORMAT_STRING_TONS = "%.0f t";
    private static final String FORMAT_STRING_POUNDS = "%d lbs";

    private static final String FORMAT_STRING_LATENCY = "%d ms";

    private static final int BINARY_THOUSAND = 1024;

    private static ShortUnits shortUnits;
    private static LongUnits longUnits;
    private static Speeds speeds;
    private static String strCurrencySymbol;
    private static boolean bUseLbs;

    public static void Initialise(Context ctx)
    {
        context = ctx;

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        shortUnits = ShortUnits.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_SHORT_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        longUnits = LongUnits.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_LONG_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        speeds = Speeds.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_SPEEDS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        strCurrencySymbol = sharedPreferences.getString(ClientDefs.SETTINGS_CURRENCY, ClientDefs.SETTINGS_CURRENCY_DEFAULT);
        bUseLbs = sharedPreferences.getBoolean(ClientDefs.SETTINGS_USE_LBS, ClientDefs.SETTINGS_USE_LBS_DEFAULT);
    }

    private static String GetShortDistanceStringFromKM(float fltDistanceKM)
    {
        switch(shortUnits)
        {
            case METERS: return String.format(FORMAT_STRING_METERS, (int)((fltDistanceKM / KM_PER_METER) + 0.5f));
            case YARDS: return String.format(FORMAT_STRING_YARDS, (int)((fltDistanceKM / KM_PER_YARD) + 0.5f));
            case FEET: return String.format(FORMAT_STRING_FEET, (int)((fltDistanceKM / KM_PER_FOOT) + 0.5f));
        }

        return "Short unit error!";
    }

    public static String GetDistanceStringFromKM(float fltDistanceKM)
    {
        switch(longUnits)
        {
            case KILOMETERS:
                if(fltDistanceKM < 1.0f)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_KILOMETERS, fltDistanceKM);

            case STATUTE_MILES:
                if(fltDistanceKM < KM_PER_MILE)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_STATUTE_MILES, fltDistanceKM / KM_PER_MILE);

            case NAUTICAL_MILES:
                if(fltDistanceKM < KM_PER_NM)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_NAUTICAL_MILES, fltDistanceKM / KM_PER_NM);
        }

        return "Long unit error!";
    }

    public static String GetWeightStringFromKG(long kg)
    {
        if(kg >= 1000000)
        {
            return String.format(FORMAT_STRING_TONS, (float)(kg/1000));
        }

        return String.format(FORMAT_STRING_KILOGRAMS, kg);
    }

    public static String GetDistanceStringFromM(float fltDistanceM)
    {
        return GetDistanceStringFromKM(fltDistanceM * KM_PER_METER);
    }

    public static String AssignSpeedFromKPH(float fltSpeed)
    {
        switch(speeds)
        {
            case KILOMETERS_PER_HOUR: return String.format(FORMAT_STRING_KPH, (int)(fltSpeed + 0.5f));
            case MILES_PER_HOUR: return String.format(FORMAT_STRING_MPH, (int)((fltSpeed / KPH_PER_MPH) + 0.5f));
            case KNOTS: return String.format(FORMAT_STRING_KNOTS, (int)((fltSpeed / KPH_PER_KNOT) + 0.5f));
        }

        return "Speed unit error!";
    }

    public static void AssignMachSpeedFromKph(TextView textView, float fltSpeed)
    {
        float machValue = fltSpeed * 0.000809848f;

        textView.setText(context.getString(R.string.mach, String.format("%.1f", machValue)));

        if(machValue < 4)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
        else if(machValue >= 4 && machValue < 9)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }
        else if(machValue >= 9)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
    }

    public static String GetCurrencyString(int lMoney)
    {
        /*if(lMoney >= 1000000000)
        {
            return strCurrencySymbol + String.format("%.0f", (float)(lMoney/1000000000)) + "t";
        }
        else if(lMoney >= 1000000)
        {
            return strCurrencySymbol + String.format("%.1f", (float)(lMoney/1000000)) + "b";
        }
        else if(lMoney >= 1000)
        {
            return strCurrencySymbol + String.format("%.1f", (float)(lMoney/1000)) + "m";
        }*/

        return strCurrencySymbol + lMoney;
    }

    public static String GetCurrencyString(float fltMoney)
    {
        return strCurrencySymbol + String.format("%.1f", fltMoney);
    }

    public static String GetCurrencyString(long oMoney)
    {
        if(oMoney == 0)
            return context.getString(R.string.free);

        return strCurrencySymbol + oMoney;
    }

    public static String GetTimeAmount(long oTimespan)
    {
        long oDays = oTimespan / A_DAY;
        long oHours = (oTimespan % A_DAY) / AN_HOUR;
        long oMinutes = ((oTimespan % A_DAY) % AN_HOUR) / A_MINUTE;
        long oSeconds = (((oTimespan % A_DAY) % AN_HOUR) % A_MINUTE) / A_SEC;

        if(oDays > 0)
        {
            return oDays + "days, " + oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oHours > 0)
        {
            return oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oMinutes > 0)
        {
            return String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else
        {
            return oSeconds + "s";
        }
    }

    public static String GetLatencyString(long oLatency)
    {
        return String.format(FORMAT_STRING_LATENCY, oLatency);
    }

    public static String GetFutureTime(long oInFuture)
    {
        long oTime = System.currentTimeMillis() + oInFuture;

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(oTime);

        if(oInFuture > A_DAY)
        {
            return sdfDayAndTime.format(cal.getTime());
        }

        return sdfTime.format(cal.getTime());
    }

    public static String GetDate(Calendar cal)
    {
        return sdfDate.format(cal.getTime());
    }

    public static String GetTime(Calendar cal)
    {
        return sdfTime.format(cal.getTime());
    }

    public static String GetTime(long oTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfTime.format(cal.getTime());
    }

    public static String GetDateAndTime(long oTime)
    {
        if(oTime == 0)
            return "Never";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfDayAndTime.format(cal.getTime());
    }

    public static String GetDateAndTimeRange(long oStart, long oEnd)
    {
        //TO DO: String(s) from resource, and condense the month/day if same?
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(oStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(oEnd);

        return String.format("%s - %s", sdfDayAndTime.format(calStart.getTime()), sdfDayAndTime.format(calEnd.getTime()));
    }

    public static String GetDateAndFullTime(long oTime)
    {
        if(oTime == 0)
            return "Never";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfDayAndFullTime.format(cal.getTime());
    }

    public static String QualitativeDirectionFromBearing(double dblBearing)
    {
        //Normalise.
        if(dblBearing < 0)
        {
            dblBearing += (2 * Math.PI);
        }

        if(dblBearing > COMPASS_DIV_OFFSET)
        {
            if(dblBearing < COMPASS_DIVISIONS + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.northeast);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 2) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.east);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 3) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.southeast);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 4) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.south);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 5) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.southwest);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 6) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.west);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 7) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.northwest);
            }
        }

        return context.getString(R.string.north);
    }

    public static String GetEntityTypeAndName(LaunchEntity entity, LaunchGame game)
    {
        if(entity instanceof Player)
        {
            return ((Player)entity).GetName();
        }
        else if(entity instanceof Missile)
        {
            return GetMissileAndType((Missile)entity, game);
        }
        else if(entity instanceof Interceptor)
        {
            return GetInterceptorAndType((Interceptor)entity, game);
        }
        else if(entity instanceof Torpedo)
        {
            return GetTorpedoAndType((Torpedo)entity, game);
        }
        else if(entity instanceof MissileSite)
        {
            MissileSite missileSite = ((MissileSite)entity);
            if(missileSite.CanTakeICBM())
            {
                return "ICBM Silo";
            }
            return context.getString(R.string.missile_site);
        }
        else if(entity instanceof SAMSite)
        {
            SAMSite samSite = ((SAMSite)entity);
            if(samSite.GetIsABMSilo())
            {
                return "ABM Silo";
            }
            return context.getString(R.string.sam_site);
        }
        else if(entity instanceof SentryGun)
        {
            SentryGun gun = (SentryGun)entity;

            if(gun.GetIsWatchTower())
            {
                return context.getString(R.string.artillery_gun);
            }
            else
            {
                return context.getString(R.string.sentry_gun);
            }
        }
        else if(entity instanceof ArtilleryGun)
        {
            return context.getString(R.string.artillery_gun);
        }
        else if(entity instanceof CommandPost)
        {
            return context.getString(R.string.command_post);
        }
        else if(entity instanceof Bank)
        {
            return context.getString(R.string.bank);
        }
        else if(entity instanceof Warehouse)
        {
            return context.getString(R.string.bank);
        }
        else if(entity instanceof Airbase)
        {
            return context.getString(R.string.airbase);
        }
        else if(entity instanceof Armory)
        {
            return context.getString(R.string.armory);
        }
        else if(entity instanceof AirplaneInterface)
        {
            AirplaneInterface airplane = (AirplaneInterface)entity;

            switch(airplane.GetAircraftType())
            {
                case FIGHTER: return context.getString(R.string.fighter_title);
                case BOMBER: return context.getString(R.string.bomber_title);
                case ATTACK_AIRCRAFT: return context.getString(R.string.attack_aircraft_title);
                case REFUELER: return context.getString(R.string.refueler_title);
                case MULTI_ROLE: return context.getString(R.string.multi_role_title);
                case SSB: return context.getString(R.string.ssb_title);
            }
        }
        else if(entity instanceof NavalVessel)
        {
            switch(entity.GetEntityType())
            {
                case FRIGATE: return context.getString(R.string.frigate_title);
                case DESTROYER: return context.getString(R.string.destroyer_title);
                case AMPHIB: return context.getString(R.string.assault_ship_title);
                case CARGO_SHIP: return context.getString(R.string.cargo_ship_title);
                case FLEET_OILER: return context.getString(R.string.fleet_oiler_title);
                case SUPER_CARRIER: return context.getString(R.string.super_carrier_title);
                case ATTACK_SUB: return context.getString(R.string.attack_sub_title);
                case SSBN: return context.getString(R.string.ssbn_title);
            }
        }
        else if(entity instanceof Rubble)
        {
            switch(((Rubble)entity).GetStructureType())
            {
                case NUCLEAR_MISSILE_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.nuke_site_title));
                case SENTRY_GUN: return context.getString(R.string.rubble_title_format, context.getString(R.string.sentry_gun));
                case WATCH_TOWER: return context.getString(R.string.rubble_title_format, context.getString(R.string.artillery_gun));
                case AIRBASE: return context.getString(R.string.rubble_title_format, context.getString(R.string.airbase));
                case ARMORY: return context.getString(R.string.rubble_title_format, context.getString(R.string.armory));
                case ABM_SILO: return context.getString(R.string.rubble_title_format, context.getString(R.string.abm_silo_title));
                case COMMAND_POST: return context.getString(R.string.rubble_title_format, context.getString(R.string.command_post));
                case BANK: return context.getString(R.string.rubble_title_format, context.getString(R.string.bank));
                case SAM_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.sam_site));
                case MISSILE_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.missile_site));
                case ARTILLERY_GUN: return context.getString(R.string.rubble_title_format, context.getString(R.string.artillery_gun));
                default: return "NOT IMPLEMENTED! (Rubble name)";
            }
        }
        else if(entity instanceof Shipyard)
        {
            Shipyard shipyard = (Shipyard)entity;

            return shipyard.GetName();
        }
        else if(entity instanceof Loot)
        {
            return GetLootContentString(((Loot)entity), game);
        }
        else if(entity instanceof TankInterface)
        {
            return context.getString(R.string.mbt);
        }

        return "NOT IMPLEMENTED! (GetEntityTypeAndName)";
    }

    public static String GetOwnedEntityName(LaunchEntity entity, LaunchGame game)
    {
        Player owner = game.GetOwner(entity);
        String ownerName;

        if(owner != null)
        {
            ownerName = owner.GetName();

            if(entity instanceof Player)
            {
                Player player = (Player)entity;
                return player.GetName();
            }
            if(entity instanceof Missile)
            {
                Missile missile = (Missile)entity;
                MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                if(game.GetPlayer(missile.GetOwnerID()) != null)
                {
                    return context.getString(R.string.owners_entity, ownerName, GetMissileAndType(missile, game));
                }
            }
            else if(entity instanceof Interceptor)
            {
                Interceptor interceptor = (Interceptor)entity;
                return context.getString(R.string.owners_entity, ownerName, GetInterceptorAndType(interceptor, game));
            }
            else if(entity instanceof Torpedo)
            {
                Torpedo torpedo = (Torpedo)entity;
                return context.getString(R.string.owners_entity, ownerName, GetTorpedoAndType(torpedo, game));
            }
            else if(entity instanceof MissileSite)
            {
                if(((MissileSite)entity).CanTakeICBM())
                {
                    MissileSite missileSite = (MissileSite)entity;
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.nuke_site_title));
                }
                else
                {
                    MissileSite missileSite = (MissileSite) entity;
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.missile_site_title));
                }
            }
            else if(entity instanceof Rubble)
            {
                return GetEntityTypeAndName(entity, game);
            }
            else if(entity instanceof SAMSite)
            {
                SAMSite samSite = (SAMSite)entity;

                if(samSite.GetIsABMSilo())
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.abm_silo_title));
                }
                else
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.sam_site_title));
                }
            }
            else if(entity instanceof SentryGun)
            {
                SentryGun gun = (SentryGun)entity;

                if(gun.GetIsWatchTower())
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.artillery_gun_title));
                }
                else
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.sentry_gun_title));
                }
            }
            else if(entity instanceof ArtilleryGun)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.artillery_gun_title));
            }
            else if(entity instanceof CommandPost)
            {
                CommandPost bunker = (CommandPost)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.command_post_title));
            }
            else if(entity instanceof Bank)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.bank_title));
            }
            else if(entity instanceof Warehouse)
            {
                Warehouse depot = (Warehouse)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.bank_title));
            }
            else if(entity instanceof Airbase)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_airbase));
            }
            else if(entity instanceof Armory)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_armory));
            }
            else if(entity instanceof AirplaneInterface)
            {
                AirplaneInterface airplane = (AirplaneInterface)entity;

                switch(airplane.GetAircraftType())
                {
                    case FIGHTER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.fighter_name));
                    case BOMBER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.bomber_name));
                    case ATTACK_AIRCRAFT: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.attack_aircraft_name));
                    case REFUELER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.refueler_name));
                    case MULTI_ROLE: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.multi_role_name));
                    case SSB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.ssb_name));
                }
            }
            else if(entity instanceof NavalVessel)
            {
                NavalVessel vessel = (NavalVessel)entity;
                EntityType type = vessel.GetEntityType();

                switch(type)
                {
                    case FRIGATE: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.frigate_name));
                    case DESTROYER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.destroyer_name));
                    case AMPHIB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.assault_ship_name));
                    case CARGO_SHIP: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.cargo_ship_name));
                    case FLEET_OILER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.fleet_oiler_name));
                    case SUPER_CARRIER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.super_carrier_name));
                    case ATTACK_SUB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.attack_sub_name));
                    case SSBN: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.ssbn_name));
                }
            }
            else if(entity instanceof Shipyard)
            {
                Shipyard shipyard = ((Shipyard)entity);
                return shipyard.GetName();
            }
            else if(entity instanceof TankInterface)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.title_mbt));
            }
        }
        else
        {
            return GetEntityTypeAndName(entity, game);
        }

        return "UNOWNED ENTITY (GetOwnedEntityName)";
    }

    public static String GetEntityTypeName(EntityType entityType)
    {
        switch(entityType)
        {
            case MISSILE_SITE:
            {
                return context.getString(R.string.construct_name_cms);
            }

            case NUCLEAR_MISSILE_SITE:
            {
                return context.getString(R.string.construct_name_ncms);
            }

            case SAM_SITE:
            {
                return context.getString(R.string.construct_name_sam);
            }

            case ABM_SILO:
            {
                return context.getString(R.string.construct_name_abm_silo);
            }

            case SENTRY_GUN:
            {
                return context.getString(R.string.construct_name_sentry_gun);
            }

            case WATCH_TOWER:
            {
                return context.getString(R.string.construct_name_artillery_gun);
            }

            case COMMAND_POST:
            {
                return context.getString(R.string.construct_name_command_post);
            }

            case AIRBASE:
            {
                return context.getString(R.string.construct_name_airbase);
            }

            case BANK:
            {
                return context.getString(R.string.construct_name_bank);
            }

            case WAREHOUSE:
            {
                return context.getString(R.string.construct_name_bank);
            }

            case ARMORY:
            {
                return context.getString(R.string.construct_name_armory);
            }

            case ARTILLERY_GUN:
            {
                return context.getString(R.string.construct_name_artillery_gun);
            }

            case MBT:
            {
                return context.getString(R.string.mbt_construct_name);
            }

            case FRIGATE:
            {
                return context.getString(R.string.frigate_name);
            }

            case DESTROYER:
            {
                return context.getString(R.string.destroyer_name);
            }

            case SUPER_CARRIER:
            {
                return context.getString(R.string.super_carrier_name);
            }

            case CARGO_SHIP:
            {
                return context.getString(R.string.cargo_ship_name);
            }

            case AMPHIB:
            {
                return context.getString(R.string.assault_ship_name);
            }

            case FLEET_OILER:
            {
                return context.getString(R.string.fleet_oiler_name);
            }

            case ATTACK_SUB:
            {
                return context.getString(R.string.attack_sub_name);
            }

            case SSBN:
            {
                return context.getString(R.string.ssbn_name);
            }

            case FIGHTER:
            {
                return context.getString(R.string.fighter_name);
            }

            case BOMBER:
            {
                return context.getString(R.string.bomber_name);
            }

            case ATTACK_AIRCRAFT:
            {
                return context.getString(R.string.attack_aircraft_name);
            }

            case REFUELER:
            {
                return context.getString(R.string.refueler_name);
            }

            case MULTI_ROLE:
            {
                return context.getString(R.string.multi_role_name);
            }

            case SSB:
            {
                return context.getString(R.string.ssb_name);
            }

            default: return "NOT IMPLEMENTED! (GetEntityTypeName)";
        }
    }

    public static String GetEntityTypeTitle(EntityType entityType)
    {
        switch(entityType)
        {
            case MISSILE_SITE:
            {
                return context.getString(R.string.missile_site);
            }

            case NUCLEAR_MISSILE_SITE:
            {
                return context.getString(R.string.nuke_site_title);
            }

            case SAM_SITE:
            {
                return context.getString(R.string.sam_site);
            }

            case ABM_SILO:
            {
                return context.getString(R.string.abm_silo_title);
            }

            case SENTRY_GUN:
            {
                return context.getString(R.string.sentry_gun);
            }

            case ARTILLERY_GUN:
            case WATCH_TOWER:
            {
                return context.getString(R.string.artillery_gun);
            }

            case COMMAND_POST:
            {
                return context.getString(R.string.command_post);
            }

            case AIRBASE:
            {
                return context.getString(R.string.airbase);
            }

            case BANK:
            case WAREHOUSE:
            {
                return context.getString(R.string.bank);
            }

            case ARMORY:
            {
                return context.getString(R.string.armory);
            }

            case MBT:
            {
                return context.getString(R.string.mbt);
            }

            case FRIGATE:
            {
                return context.getString(R.string.frigate_title);
            }

            case DESTROYER:
            {
                return context.getString(R.string.destroyer_title);
            }

            case SUPER_CARRIER:
            {
                return context.getString(R.string.super_carrier_title);
            }

            case CARGO_SHIP:
            {
                return context.getString(R.string.cargo_ship_title);
            }

            case AMPHIB:
            {
                return context.getString(R.string.assault_ship_title);
            }

            case FLEET_OILER:
            {
                return context.getString(R.string.fleet_oiler_title);
            }

            case ATTACK_SUB:
            {
                return context.getString(R.string.attack_sub_title);
            }

            case SSBN:
            {
                return context.getString(R.string.ssbn_title);
            }

            case FIGHTER:
            {
                return context.getString(R.string.fighter_title);
            }

            case BOMBER:
            {
                return context.getString(R.string.bomber_title);
            }

            case ATTACK_AIRCRAFT:
            {
                return context.getString(R.string.attack_aircraft_title);
            }

            case REFUELER:
            {
                return context.getString(R.string.refueler_title);
            }

            case MULTI_ROLE:
            {
                return context.getString(R.string.multi_role_title);
            }

            case SSB:
            {
                return context.getString(R.string.ssb_title);
            }

            default: return "NOT IMPLEMENTED! (GetEntityTypeName)";
        }
    }

    public static String GetMissileAndType(Missile missile, LaunchGame game)
    {
        return context.getString(R.string.missile_type, game.GetConfig().GetMissileType(missile.GetType()).GetName(), context.getString(R.string.missile));
    }

    public static String GetInterceptorAndType(Interceptor interceptor, LaunchGame game)
    {
        return context.getString(R.string.missile_type, game.GetConfig().GetInterceptorType(interceptor.GetType()).GetName(), context.getString(R.string.interceptor));
    }

    public static String GetTorpedoAndType(Torpedo torpedo, LaunchGame game)
    {
        return context.getString(R.string.missile_type, game.GetConfig().GetTorpedoType(torpedo.GetType()).GetName(), context.getString(R.string.torpedo));
    }

    public static void SetStructureState(TextView textView, Structure structure)
    {
        if(structure.GetOnline())
        {
            textView.setText(R.string.state_online_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }

        if(structure.GetBooting())
        {
            textView.setText(R.string.state_booting_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }

        if(structure.GetSelling())
        {
            textView.setText(R.string.state_selling_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }

        if(structure.GetOffline())
        {
            textView.setText(R.string.state_offline_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static String GetPercentStringFromFraction(float fltFraction)
    {
        return String.format("%.0f%%", fltFraction * FRACTION_TO_PERCENT);
    }

    public static String GetLatLongString(float fltLatitude, float fltLongitude)
    {
        return context.getString(R.string.lat_long, String.format("%.6f", fltLatitude), String.format("%.6f", fltLongitude));
    }

    public static void AssignHealthStringAndAppearance (TextView txtHealth, Damagable damagable)
    {
        if(damagable.Destroyed())
        {
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));

            txtHealth.setText(context.getString(R.string.health_destroyed));
        }
        else
        {
            txtHealth.setText(context.getString(R.string.hps, damagable.GetHP(), damagable.GetMaxHP()));

            float fltDamageRatio = (float) damagable.GetHP() / (float) damagable.GetMaxHP();
            if (fltDamageRatio > 0.999f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if (fltDamageRatio > 0.5f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }


    public static void AssignHealthStringAndAppearance(TextView txtHealth, StoredDamagable damagable)
    {
        if(damagable.Destroyed())
        {
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));

            txtHealth.setText(context.getString(R.string.health_destroyed));
        }
        else
        {
            txtHealth.setText(context.getString(R.string.hps, damagable.GetHP(), damagable.GetMaxHP()));

            float fltDamageRatio = (float) damagable.GetHP() / (float) damagable.GetMaxHP();
            if (fltDamageRatio > 0.999f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if (fltDamageRatio > 0.5f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignHealthStringAndAppearance (TextView txtHealth, List<Structure> Structures)
    {
        short nMinHealth = Short.MAX_VALUE;
        short nMaxHealth = 0;
        short nMinMaxHealth = Short.MAX_VALUE;
        short nMaxMaxHealth = 0;

        for(Structure structure: Structures)
        {
            if(structure != null)
            {
                nMinHealth = (short)Math.min(nMinHealth, structure.GetHP());
                nMaxHealth = (short)Math.max(nMaxHealth, structure.GetHP());
                nMinMaxHealth = (short)Math.min(nMinMaxHealth, structure.GetMaxHP());
                nMaxMaxHealth = (short)Math.max(nMaxMaxHealth, structure.GetMaxHP());
            }
        }

        String strHealthRangeString = (nMinHealth == nMaxHealth) ? Short.toString(nMinHealth) : String.format("%d-%d", nMinHealth, nMaxHealth);
        String strHealthMaxString = (nMinMaxHealth == nMaxMaxHealth) ? Short.toString(nMinMaxHealth) : String.format("%d-%d", nMinMaxHealth, nMaxMaxHealth);
        String strHealthString = String.format("%s/%s", strHealthRangeString, strHealthMaxString);

        txtHealth.setText(strHealthString);

        float fltDamageRatio = (float) nMinHealth / (float) nMinMaxHealth;
        if (fltDamageRatio > 0.999f)
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if (fltDamageRatio > 0.5f)
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static String GetDamageString(int lDamage)
    {
        return context.getString(R.string.hp, lDamage);
    }

    public static String GetMultiplierString(float fltMultiplier)
    {
        return context.getString(R.string.multiplier, String.format("%.2f", fltMultiplier));
    }

    public static String GetConnectionSpeed(int lBytesPerSecond)
    {
        if(lBytesPerSecond < BINARY_THOUSAND)
        {
            //Bytes per second.
            return context.getString(R.string.value_unknown_download, lBytesPerSecond, context.getString(R.string.bytes_per_sec));
        }
        else if(lBytesPerSecond < BINARY_THOUSAND * BINARY_THOUSAND)
        {
            //KB per sec.
            return context.getString(R.string.value_unknown_download, (int)((float)(lBytesPerSecond) / (float)(BINARY_THOUSAND) + 0.5f), context.getString(R.string.kilobytes_per_sec));
        }
        else
        {
            //MB per sec.
            return context.getString(R.string.value_unknown_download, (int)((float)(lBytesPerSecond) / (float)(BINARY_THOUSAND * BINARY_THOUSAND) + 0.5f), context.getString(R.string.megabytes_per_sec));
        }
    }

    /**
     * Return a damage efficiency string, that is units of the player's currency symbol per hit point.
     * @param fltDamageEfficiency The incoming number, will be reduced to 2dp.
     * @return The damage efficiency string, e.g. "£69.69/hp".
     */
    public static String GetDamageEfficiency(float fltDamageEfficiency)
    {
        return context.getString(R.string.hp_efficiency, strCurrencySymbol, String.format("%.2f", fltDamageEfficiency));
    }

    public static String GetAccuracyPercentage(float fltHitChance)
    {
        return context.getString(R.string.interceptor_accuracy, String.format("%.0f%%", (fltHitChance * 100)));
    }

    public static void AssignPercentageString(TextView textView, float fltFloat, LaunchClientGame game)
    {
        textView.setText(String.format("%.0f%%", (fltFloat * 100)));
        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
    }

    public static void AssignAccuracyPercentage(TextView textView, float fltHitChance, LaunchClientGame game)
    {
        textView.setText(GetAccuracyPercentage(fltHitChance));

        if(fltHitChance >= game.GetConfig().GetMaxWeaponSystemAccuracy())
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.75f)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.5f && fltHitChance < 0.75f)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
        }
        else
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignAccuracyBoostPercentage(TextView textView, float fltHitChance, LaunchClientGame game)
    {
        if(fltHitChance >= game.GetConfig().GetMaxRadarAccuracyBoost())
        {
            textView.setText(context.getString(R.string.high_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.35f)
        {
            textView.setText(context.getString(R.string.high_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.15f && fltHitChance < 0.35f)
        {
            textView.setText(context.getString(R.string.medium_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }
        else
        {
            textView.setText(context.getString(R.string.low_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignFuelPercentageString(TextView textView, FuelableInterface fuelable)
    {
        float lFuelLevel = (float) fuelable.GetCurrentFuel();
        float lFuelCapacity = (float) fuelable.GetMaxFuel();
        float fltPercent = (float)lFuelLevel/lFuelCapacity;

        textView.setText(String.format("%.0f%%", (fltPercent * 100)));

        if(fltPercent >= 1.0)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(fltPercent < 0.5)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
    }

    public static void AssignYieldStringAndAppearance(TextView textView, LaunchType launchType, LaunchClientGame game)
    {
        if(launchType instanceof MissileType)
        {
            /**
             * If the type is anti submarine and nuclear, do the yield as normal.
             * If the type is anti submarine and tyetodeploy != LaunchEntity.NONE, get the torpedo type.
             */

            MissileType type = ((MissileType)launchType);

            float yield = type.GetYield();

            if(yield < 1f && type.GetEMPRadius() > 0.0f)
            {
                textView.setText(context.getString(R.string.warhead_emp));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.EMPTextColour));
            }
            else if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
            else if(type.GetAntiSubmarine() && type.GetTorpedoType() != LaunchEntity.ID_NONE)
            {
                TorpedoType torpedoType = game.GetConfig().GetTorpedoType(type.GetTorpedoType());

                if(torpedoType != null)
                {
                    textView.setText(torpedoType.GetName());
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
            }
            else if(yield < 0.01f)
            {
                textView.setText(context.getString(R.string.warhead_kinetic));
            }
            else
            {
                if(bUseLbs)
                {
                    yield *= 2.205;
                    textView.setText(String.format(FORMAT_STRING_POUNDS, (int)yield));
                }
                else
                    textView.setText(String.format(FORMAT_STRING_KILOGRAMS, (int)yield));

                if(yield > 500)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield > 250)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
        }
        else if(launchType instanceof InterceptorType)
        {
            InterceptorType type = ((InterceptorType)launchType);

            float yield = type.GetYield();

            if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
        }
        if(launchType instanceof TorpedoType)
        {
            TorpedoType type = ((TorpedoType)launchType);

            float yield = type.GetYield();

            if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
            else
            {
                if(bUseLbs)
                {
                    yield *= 2.205;
                    textView.setText(String.format(FORMAT_STRING_POUNDS, (int)yield));
                }
                else
                    textView.setText(String.format(FORMAT_STRING_KILOGRAMS, (int)yield));

                if(yield > 500)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield > 250)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
        }
    }

    public static void AssignMissileRangeAppearance(TextView textView, float range, boolean bMissile)
    {
        if(bMissile)
        {
            if(range > 5000)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if(range > 1500)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
        else
        {
            if(range > 550)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if(range > 250)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignTorpedoRangeAppearance(TextView textView, float range)
    {
        if(range > 14)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(range > 8)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static void AssignBuildTimeAppearance(TextView textView, long buildTime)
    {
        if(buildTime < Defs.MS_PER_DAY)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(buildTime < Defs.MS_PER_DAY * 3)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static String GetMachFromKPH(float KPH)
    {
        float fltMach = KPH/Defs.KPH_PER_MACH;

        return String.format("%.1f", fltMach);
    }

    public static String GetMetricTonsFromKG(float lKG)
    {
        float fltTons = lKG/Defs.KG_PER_TON;

        return String.format("%.0f", fltTons);
    }

    public static void AssignAircraftStatusString(TextView textView, AirplaneInterface aircraft)
    {
        if(aircraft.Flying())
        {
            Movable flyingAircraft = (Movable)aircraft;

            if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.RETURN)
            {
                textView.setText(context.getString(R.string.status_returning));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.ATTACK)
            {
                textView.setText(context.getString(R.string.status_attacking));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.SEEK_FUEL || flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.PROVIDE_FUEL)
            {
                textView.setText(context.getString(R.string.status_refueling));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.WAIT)
            {
                textView.setText(context.getString(R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
        }
        else
        {
            StoredDamagable storedAircraft = ((StoredDamagable)aircraft);

            if(!storedAircraft.DoneBuilding())
            {
                textView.setText(context.getString(R.string.unit_constructing, TextUtilities.GetTimeAmount(storedAircraft.GetPrepRemaining())));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(((FuelableInterface)storedAircraft).GetFuelDeficit() > 0)
            {
                textView.setText(context.getString(R.string.status_refueling));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(!storedAircraft.AtFullHealth())
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static void AssignTankStatusString(TextView textView, TankInterface tankInterface)
    {
        if(tankInterface instanceof Tank)
        {
            Tank tank = (Tank)tankInterface;

            if(tank.GetMoveOrders() == MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(tank.GetMoveOrders() == MoveOrders.WAIT)
            {
                textView.setText(context.getString(tank.IsASPAAG() ? R.string.status_defending : R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
        else if(tankInterface instanceof StoredTank)
        {
            StoredTank tank = (StoredTank)tankInterface;

            if(tank.GetHP() < tank.GetMaxHP()/1.5f) //HP is less than 2/3.
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static void AssignCargoTruckStatusString(TextView textView, CargoTruckInterface truckInterface)
    {
        if(truckInterface instanceof CargoTruck)
        {
            CargoTruck truck = (CargoTruck)truckInterface;

            if(truck.GetMoveOrders() == MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(truck.GetMoveOrders() == MoveOrders.WAIT)
            {
                textView.setText(context.getString(R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
            }
        }
        else if(truckInterface instanceof StoredCargoTruck)
        {
            StoredCargoTruck truck = (StoredCargoTruck)truckInterface;

            if(truck.GetHP() < truck.GetMaxHP()/1.5f) //HP is less than 2/3.
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static String GetFuelUsageString(LaunchGame game, FuelableInterface fuelable, GeoCoord geoTarget)
    {
        GeoCoord geoStart = game.GetPosition((LaunchEntity)fuelable);
        float fltDistanceTravelled = geoStart.DistanceTo(geoTarget);

        float fltMaxRange = Defs.CARGO_TRUCK_RANGE;

        if(fuelable instanceof NavalVessel)
            fltMaxRange = Defs.NAVAL_RANGE;
        else if(fuelable instanceof AirplaneInterface)
            fltMaxRange = Defs.GetAircraftRange(((AirplaneInterface)fuelable).GetAircraftType());
        else if(fuelable instanceof Tank)
            fltMaxRange = Defs.TANK_RANGE;

        float fuelPercent = fltDistanceTravelled/game.GetFuelableRange(fuelable.GetMaxFuel(), fltMaxRange);

        return GetPercentStringFromFraction(fuelPercent);
    }

    public static String GetFuelUsageString(LaunchGame game, FuelableInterface fuelable, float fltDistance)
    {
        float fltMaxRange = Defs.CARGO_TRUCK_RANGE;

        if(fuelable instanceof NavalVessel)
            fltMaxRange = Defs.NAVAL_RANGE;
        else if(fuelable instanceof AirplaneInterface)
            fltMaxRange = Defs.GetAircraftRange(((AirplaneInterface)fuelable).GetAircraftType());
        else if(fuelable instanceof Tank)
            fltMaxRange = Defs.TANK_RANGE;

        GeoCoord geoStart = game.GetPosition((LaunchEntity)fuelable);
        float fuelPercent = fltDistance/game.GetFuelableRange(fuelable.GetMaxFuel(), fltMaxRange);

        return GetPercentStringFromFraction(fuelPercent);
    }

    public static String GetCostStatement(Map<ResourceType, Long> costs)
    {
        if(costs.isEmpty())
            return context.getString(R.string.free).toLowerCase();

        List<String> parts = new ArrayList<>();

        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            parts.add(GetResourceQuantityStringWithName(cost.getKey(), cost.getValue()));
        }

        if(parts.isEmpty())
        {
            return "";
        }
        else if(parts.size() == 1)
        {
            return parts.get(0);
        }
        else if(parts.size() == 2)
        {
            return parts.get(0) + " and " + parts.get(1);
        }
        else
        {
            String allButLast = String.join(", ", parts.subList(0, parts.size() - 1));
            String last = parts.get(parts.size() - 1);
            return allButLast + ", and " + last;
        }
    }

    public static String GetResourceQuantityString(ResourceType type, long oQuantity)
    {
        switch(type)
        {
            case WEALTH:
            {
                return GetCurrencyString(oQuantity);
            }

            default:
            {
                return GetWeightStringFromKG(oQuantity);
            }
        }
    }

    public static String GetResourceQuantityStringWithName(ResourceType type, long oQuantity)
    {
        return "REMOVE TODO";
    }

    public static String GetLootContentString(Loot loot, LaunchGame game)
    {
        return "[NOT IMPLEMENTED! (GetLootContentString)]";
    }
}
