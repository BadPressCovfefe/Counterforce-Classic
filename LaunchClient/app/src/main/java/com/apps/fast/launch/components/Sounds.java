package com.apps.fast.launch.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;

import com.apps.fast.launch.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import launch.game.entities.Torpedo;
import launch.utilities.ShortDelay;

/**
 * Created by tobster on 16/03/16.
 */
public class Sounds
{
    private static Context context;
    private static Random rand = new Random();

    private static final int MAX_SOUND_INTERVAL = 100;
    private static long oLastSoundPlayed = 0;

    private static List<Integer> NukeExplosions;
    private static List<Integer> NearExplosions;
    private static List<Integer> FarExplosions;
    private static List<Integer> ArtilleryExplosions;
    private static List<Integer> Moneys;
    private static List<Integer> MissileLaunches;
    private static List<Integer> ArtilleryLaunches;
    private static List<Integer> RailgunFires;

    private static List<Integer> WaterExplosions;
    private static List<Integer> ICBMLaunches;
    private static List<Integer> BombDrops;
    private static List<Integer> InterceptorLaunches;
    private static List<Integer> Constructions;
    private static List<Integer> InterceptorMisses;
    private static List<Integer> InterceptorHits;
    private static List<Integer> SentryHits;
    private static List<Integer> SentryMisses;
    private static List<Integer> Reconfigs;
    private static List<Integer> Equips;
    private static List<Integer> Respawns;
    private static List<Integer> Deaths;
    private static List<Integer> Repairs;
    private static List<Integer> Heals;
    private static List<Integer> Transmits;
    private static List<Integer> AircraftMovements;
    private static List<Integer> InfantryMovements;
    private static List<Integer> HelicopterMovements;
    private static List<Integer> TankMovements;
    private static List<Integer> CargoTruckMovements;
    private static List<Integer> Airdrops;
    private static List<Integer> Prospects;
    private static List<Integer> TorpedoLaunches;
    private static List<Integer> Sonars;
    private static List<Integer> CargoLoads;
    private static List<Integer> ShipMovements;
    private static List<Integer> SubmarineDives;
    private static List<Integer> SubmarineSurfaces;
    private static List<Integer> CaravanMoves;
    private static List<Integer> InfantryAttacks;
    private static List<Integer> InfantryCaptures;
    private static List<Integer> Liberates;

    private static List<MediaPlayer> MediaPlayers = new ArrayList<>();

    private static boolean bDisabled;

    public static void Init(Context ctx)
    {
        NearExplosions = new ArrayList<>();
        ArtilleryExplosions = new ArrayList<>();
        NukeExplosions = new ArrayList<>();
        ArtilleryLaunches = new ArrayList<>();
        RailgunFires = new ArrayList<>();
        FarExplosions = new ArrayList<>();
        Moneys = new ArrayList<>();
        MissileLaunches = new ArrayList<>();
        ICBMLaunches = new ArrayList<>();
        InterceptorLaunches = new ArrayList<>();
        Constructions = new ArrayList<>();
        InterceptorMisses = new ArrayList<>();
        InterceptorHits = new ArrayList<>();
        SentryHits = new ArrayList<>();
        SentryMisses = new ArrayList<>();
        Reconfigs = new ArrayList<>();
        Equips = new ArrayList<>();
        Respawns = new ArrayList<>();
        Deaths = new ArrayList<>();
        Repairs = new ArrayList<>();
        Heals = new ArrayList<>();
        Transmits = new ArrayList<>();
        AircraftMovements = new ArrayList<>();
        InfantryMovements = new ArrayList<>();
        TankMovements = new ArrayList<>();
        HelicopterMovements = new ArrayList<>();
        CargoTruckMovements = new ArrayList<>();
        Airdrops = new ArrayList<>();
        Prospects = new ArrayList<>();
        TorpedoLaunches = new ArrayList<>();
        Sonars = new ArrayList<>();
        CargoLoads = new ArrayList<>();
        ShipMovements = new ArrayList<>();
        SubmarineDives = new ArrayList<>();
        SubmarineSurfaces = new ArrayList<>();
        CaravanMoves = new ArrayList<>();
        BombDrops = new ArrayList<>();
        WaterExplosions = new ArrayList<>();
        InfantryAttacks = new ArrayList<>();
        InfantryCaptures = new ArrayList<>();
        Liberates = new ArrayList<>();

        NearExplosions.add(R.raw.nearexplosion1);
        NearExplosions.add(R.raw.nearexplosion2);
        NearExplosions.add(R.raw.nearexplosion3);
        NearExplosions.add(R.raw.nearexplosion4);
        NearExplosions.add(R.raw.nearexplosion5);
        NearExplosions.add(R.raw.nearexplosion6);

        FarExplosions.add(R.raw.farexplosion1);
        FarExplosions.add(R.raw.farexplosion2);

        Moneys.add(R.raw.money1);

        MissileLaunches.add(R.raw.missilelaunch1);
        MissileLaunches.add(R.raw.missilelaunch2);
        MissileLaunches.add(R.raw.missilelaunch3);

        ICBMLaunches.add(R.raw.icbmlaunch);

        InterceptorLaunches.add(R.raw.interceptor1);
        InterceptorLaunches.add(R.raw.interceptor2);
        InterceptorLaunches.add(R.raw.interceptor3);
        InterceptorLaunches.add(R.raw.interceptor4);

        Constructions.add(R.raw.construction1);

        InterceptorMisses.add(R.raw.interceptormiss1);

        InterceptorHits.add(R.raw.interceptorhit1);

        SentryHits.add(R.raw.sentryhit1);

        SentryMisses.add(R.raw.sentrymiss1);
        SentryMisses.add(R.raw.sentrymiss2);
        SentryMisses.add(R.raw.sentrymiss3);

        Reconfigs.add(R.raw.reconfig1);

        Equips.add(R.raw.equip1);

        Respawns.add(R.raw.respawn1);

        Transmits.add(R.raw.transmission);

        Deaths.add(R.raw.death1);
	    Deaths.add(R.raw.death3);

        AircraftMovements.add(R.raw.aircraftmove1);

        InfantryMovements.add(R.raw.infantrymove1);

        HelicopterMovements.add(R.raw.helicoptermove1);

        TankMovements.add(R.raw.tankmove1);

        CargoTruckMovements.add(R.raw.cargotruckmove1);

        Airdrops.add(R.raw.airdrop);

        Prospects.add(R.raw.prospect1);

        TorpedoLaunches.add(R.raw.torpedolaunch1);

        Sonars.add(R.raw.sonar1);
        Sonars.add(R.raw.sonar2);
        Sonars.add(R.raw.sonar3);

        CargoLoads.add(R.raw.loadcargo1);

        /*ArtilleryExplosions.add(R.raw.artilleryexplosion1);
        ArtilleryExplosions.add(R.raw.artilleryexplosion2);
        ArtilleryExplosions.add(R.raw.artilleryexplosion3);*/

        ArtilleryLaunches.add(R.raw.artilleryfire1);
        ArtilleryLaunches.add(R.raw.artilleryfire2);
        ArtilleryLaunches.add(R.raw.artilleryfire3);
        ArtilleryLaunches.add(R.raw.artilleryfire4);
        ArtilleryLaunches.add(R.raw.artilleryexplosion1);
        ArtilleryLaunches.add(R.raw.artilleryexplosion2);
        ArtilleryLaunches.add(R.raw.artilleryexplosion3);

        NukeExplosions.add(R.raw.nukeexplosion1);

        ShipMovements.add(R.raw.shipmovement1);

        SubmarineDives.add(R.raw.submarinedive1);

        SubmarineSurfaces.add(R.raw.submarinesurface1);

        CaravanMoves.add(R.raw.caravanmove1);

        BombDrops.add(R.raw.bombdrop1);

        RailgunFires.add(R.raw.railgunfire1);

        WaterExplosions.add(R.raw.waterexplosion1);
        WaterExplosions.add(R.raw.waterexplosion2);
        WaterExplosions.add(R.raw.waterexplosion3);
        WaterExplosions.add(R.raw.waterexplosion4);

        InfantryAttacks.add(R.raw.infantryattack1);
        InfantryAttacks.add(R.raw.infantryattack2);
        InfantryAttacks.add(R.raw.infantryattack3);
        InfantryAttacks.add(R.raw.infantryattack4);
        InfantryAttacks.add(R.raw.infantryattack5);

        InfantryCaptures.add(R.raw.infantrycapture1);

        Liberates.add(R.raw.liberate);

        context = ctx;

        SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        bDisabled = sharedPreferences.getBoolean(ClientDefs.SETTINGS_DISABLE_AUDIO, ClientDefs.SETTINGS_DISABLE_AUDIO_DEFAULT);
    }

    public static void SetDisabled(boolean bSetDisabled)
    {
        bDisabled = bSetDisabled;
    }

    private static void PlaySound(List<Integer> FromLibrary)
    {
        if(!bDisabled)
        {
            if(System.currentTimeMillis() > oLastSoundPlayed + MAX_SOUND_INTERVAL)
            {
                if(!FromLibrary.isEmpty())
                {
                    Log.i("Sound", "Playing sound.");
                    int lSound = FromLibrary.get(rand.nextInt(FromLibrary.size()));

                    MediaPlayer mediaPlayer = MediaPlayer.create(context, lSound);
                    mediaPlayer.start();

                    MediaPlayers.add(mediaPlayer);

                    //Clean up finished media players.
                    for (int i = 0; i < MediaPlayers.size(); i++)
                    {
                        try
                        {
                            if (!MediaPlayers.get(i).isPlaying())
                            {
                                Log.i("Sound", "Cleaning up a dead sound.");
                                MediaPlayers.get(i).release();
                                MediaPlayers.remove(i--);
                            }
                        }
                        catch(Exception ex)
                        {
                            Log.i("Sound", "Couldn't clean up the sound, so let's just remove it.");

                            try
                            {
                                MediaPlayers.remove(i--);
                            }
                            catch(Exception secondEx)
                            {
                                //Irony.
                            }
                        }
                    }
                }
                else
                {
                    Log.i("Sound", "No sounds for that library.");
                }

                oLastSoundPlayed = System.currentTimeMillis();
            }
            else
            {
                Log.i("Sound", "Skipping a sound as too many are playing.");
            }
        }
    }

    public static void PlayNearExplosion()
    {
        PlaySound(NearExplosions);
    }

    public static void PlayFarExplosion()
    {
        PlaySound(NearExplosions);
    }

    public static void PlayMoney()
    {
        PlaySound(Moneys);
    }

    public static void PlayMissileLaunch()
    {
        PlaySound(MissileLaunches);
    }

    public static void PlayICBMLaunch() { PlaySound(ICBMLaunches); }

    public static void PlayBombDrop() { PlaySound(BombDrops); }

    public static void PlayInterceptorLaunch()
    {
        PlaySound(InterceptorLaunches);
    }

    public static void PlayConstruction()
    {
        PlaySound(Constructions);
    }

    public static void PlayInterceptorMiss()
    {
        PlaySound(InterceptorMisses);
    }

    public static void PlayInterceptorHit()
    {
        PlaySound(InterceptorHits);
    }

    public static void PlaySentryGunHit()
    {
        PlaySound(SentryHits);
    }

    public static void PlaySentryGunMiss()
    {
        PlaySound(SentryMisses);
    }

    public static void PlayReconfig()
    {
        PlaySound(Reconfigs);
    }

    public static void PlayEquip() { PlaySound(Equips); }

    public static void PlayRespawn() { PlaySound(Respawns); }

    public static void PlayDeath() { PlaySound(Deaths); }

    public static void PlayRepair() { PlaySound(Repairs); }

    public static void PlayTransmit() { PlaySound(Transmits); }

    public static void PlayHeal() { PlaySound(Heals); }

    public static void PlayAircraftMovement() { PlaySound(AircraftMovements); }

    public static void PlayInfantryMovement() { PlaySound(InfantryMovements); }

    public static void PlayHelicopterMovement() { PlaySound(HelicopterMovements); }

    public static void PlayTankMovement() { PlaySound(TankMovements); }

    public static void PlayCargoTruckMovement() { PlaySound(CargoTruckMovements); }

    public static void PlayAirdrop() { PlaySound(Airdrops); }

    public static void PlayProspect() { PlaySound(Prospects); }

    public static void PlayTorpedoLaunch() { PlaySound(TorpedoLaunches); }

    public static void PlaySonar() { PlaySound(Sonars); }

    public static void PlayLoadCargo() { PlaySound(CargoLoads); }

    public static void PlayArtilleryFire() { PlaySound(ArtilleryLaunches); }

    public static void PlayArtilleryExplosion() { PlaySound(ArtilleryExplosions); }

    public static void PlayNukeExplosion() { PlaySound(NukeExplosions); }

    public static void PlayShipMovement() { PlaySound(ShipMovements); }

    public static void PlaySubmarineDive() { PlaySound(SubmarineDives); }

    public static void PlaySubmarineSurface() { PlaySound(SubmarineSurfaces); }

    public static void PlayCaravanMove() { PlaySound(CaravanMoves); }

    public static void PlayRailgunFire() { PlaySound(RailgunFires); }

    public static void PlayWaterExplosion() { PlaySound(WaterExplosions); }

    public static void PlayInfantryAttack() { PlaySound(InfantryAttacks); }

    public static void PlayInfantryCapture() { PlaySound(InfantryCaptures); }

    public static void PlayLiberate() { PlaySound(Liberates); }
}
