/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import launch.comm.clienttasks.Task.TaskMessage;
import launch.game.entities.*;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.TerrainData;
import launch.game.treaties.*;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchReport;

/**
 *
 * @author tobster
 */
public interface LaunchClientGameInterface
{
    void ReceivePlayer(Player player);
    void ReceiveMissile(Missile missile);
    void ReceiveTorpedo(Torpedo torpedo);
    void ReceiveInterceptor(Interceptor interceptor);
    void ReceiveMissileSite(MissileSite missileSite);
    void ReceiveSAMSite(SAMSite samSite);
    void ReceiveOreMine(OreMine oreMine);
    void ReceiveProcessor(Processor processor);
    void ReceiveDistributor(Distributor distributor);
    void ReceiveMissileFactory(MissileFactory factory);
    void ReceiveRadarStation(RadarStation radarStation);
    void ReceiveAirbase(Airbase airbase);
    void ReceiveArmory(Armory armory);
    void ReceiveAirdrop(Airdrop airdrop);
    void ReceiveBank(Bank bank);
    void ReceiveScrapYard(ScrapYard yard);
    void ReceiveWarehouse(Warehouse warehouse);
    void ReceiveAircraft(Airplane aircraft);
    void ReceiveBlueprint(Blueprint blueprint);
    void ReceiveCommandPost(CommandPost commandPost);
    void ReceiveSentryGun(SentryGun sentryGun);
    void ReceiveArtilleryGun(ArtilleryGun artillery);
    void ReceiveResourceDeposit(ResourceDeposit deposit);
    void ReceiveLoot(Loot loot);
    void ReceiveRubble(Rubble rubble);
    void ReceiveRadiation(Radiation radiation);
    void ReceiveKOTH(KOTH koth);
    void ReceiveAlliance(Alliance alliance, boolean bMajor);
    void ReceiveTreaty(Treaty treaty);
    void ReceiveUser(User user);
    void ReceiveInfantry(Infantry infantry);
    void ReceiveShipyard(Shipyard shipyard);
    void ReceiveTank(Tank tank);
    void ReceiveCargoTruck(CargoTruck truck);
    void ReceiveStoredInfantry(StoredInfantry infantry);
    void ReceiveTerrainData(TerrainData data);
    void ReceiveShip(Ship ship);
    void ReceiveSubmarine(Submarine submarine);
    void ReceiveContactBearing(EntityPointer vessel, float fltBearing);
    
    void RemovePlayer(int lID);
    void RemoveMissile(int lID);
    void RemoveInterceptor(int lID);
    void RemoveTorpedo(int lID);
    void RemoveMissileSite(int lID);
    void RemoveSAMSite(int lID);
    void RemoveOreMine(int lID);
    void RemoveProcessor(int lID);
    void RemoveRadarStation(int lID);
    void RemoveAirbase(int lID);
    void RemoveArmory(int lID);
    void RemoveAirdrop(int lID);
    void RemoveBank(int lID);
    void RemoveAircraft(int lID);
    void RemoveStoredAircraft(int lID);
    void RemoveBlueprint(int lID);
    void RemoveCommandPost(int lID);
    void RemoveSentryGun(int lID);
    void RemoveLoot(int lID);
    void RemoveRubble(int lID);
    void RemoveResourceDeposit(int lID);
    void RemoveRadiation(int lID);
    void RemoveWarehouse(int lID);
    void RemoveInfantry(int lID);
    void RemoveTank(int lID);
    void RemoveCargoTruck(int lID);
    void RemoveShip(int lID);
    void RemoveSubmarine(int lID);
    void RemoveArtilleryGun(int lID);
    void RemoveScrapYard(int lID);
    void RemoveDistributor(int lID);
    
    void RemoveAlliance(int lID);
    void RemoveWar(int lID);
    
    boolean PlayerLocationAvailable();
    LaunchClientLocation GetPlayerLocation();
    int GetGameConfigChecksum();
    void SetConfig(Config config);
    void AvatarReceived(int lAvatarID, byte[] cData);
    void AvatarUploaded(int lAvatarID);
    void ImageReceived(int lImageID, byte[] cData);
    
    void Authenticated();
    void AccountUnregistered();
    void AccountNameTaken();
    void DeviceIDUsed();
    void AccountUnmigrated();
    void FailedSpoofCheck();
    void AccountMigrated(boolean bDisplaySuccess);
    void SetOurPlayerID(int lPlayerID);
    boolean GetReadyToUpdatePlayer();
    
    boolean VerifyVersion(short nMajorVersion, short nMinorVersion);
    void MajorVersionInvalid();
    
    void SetLatency(int lLatency);
    
    void SnapshotBegin();
    void SnapshotFinish();
    
    void ShowTaskMessage(TaskMessage message);
    void DismissTaskMessage();
    
    void ShowBasicOKDialog(String strMessage);
    
    void EventReceived(LaunchEvent event);
    void ReportReceived(LaunchReport report);
    
    void AccountClosed();
    boolean ClosingAccount();
    
    void AllianceCreated();
    
    String GetProcessNames();
    
    boolean GetConnectionMobile();
    
    void DeviceCheckRequested();
    
    void DisplayGeneralError();
    
    void TempBanned(String strReason, long oDuration);
    void PermBanned(String strReason);
    
    void UpdateToken();
}
