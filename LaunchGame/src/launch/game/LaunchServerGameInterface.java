/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import java.util.List;
import java.util.Map;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.MissileFactory.ChatChannel;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem.LootType;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchReport;
import launch.utilities.LocationSpoofCheck;
import launch.game.systems.LaunchSystem.SystemType;
import countershield.CounterShieldEngine;


public interface LaunchServerGameInterface
{
    User VerifyID(String strIMEI); //todo: add second method of verifying user via password/username, or build it into this one.
    boolean CheckDeviceIDAlreadyUsed(byte[] cDeviceID);
    boolean MigrateAccount(String strOldIMEI, String strGoogleID);
    int GetGameConfigChecksum();
    boolean CheckPlayerNameAvailable(String strPlayerName);
    User CreateAccount(String strIMEI, String strGoogleID, String strPlayerName, int lAvatarID);
    void UpdatePlayerLocation(int lPlayerID, LaunchClientLocation location);
    boolean SpoofLocation(int lPlayerID, LaunchClientLocation location);
    boolean CallAirdrop(int PlayerID);
    boolean Obliterate(int lAdminID, int lPlayerID);
    boolean PurchaseInfantry(int lPlayerID, int lArmoryID, boolean bUseSubstitutes);
    boolean PurchaseCargoTruck(int lPlayerID, int lWarehouseID, boolean bUseSubstitutes);
    boolean PurchaseTank(int lPlayerID, int lArmoryID, EntityType type, boolean bUseSubstitutes);
    boolean ConstructStructure(int lPlayerID, EntityType structureType, ResourceType type, int lCommandPostID, GeoCoord geoRemotebuild, boolean bUseSubstitutes);
    boolean PlaceBlueprint(int lPlayerID, EntityType structureType, ResourceType resourceType, GeoCoord geoPosition);
    boolean ElectronicWarfare(int lPlayerID, int lAircraftID, EntityPointer target);
    boolean PurchaseLaunchables(int lPlayerID, int lMissileSiteID, int lSlotNo, List<Integer> lMissileTypes, SystemType systemType);
    boolean AirbaseCapacityUpgrade(int lPlayerID, int lAirbaseID);
    boolean MissileSlotUpgrade(int lPlayerID, int lMissileSiteID);
    boolean InterceptorSlotUpgrade(int lPlayerID, int lSAMSiteID);
    boolean MissileSlotUpgradeToMax(int lPlayerID, int lMissileSiteID);
    boolean PurchaseAircraft(int lPlayerID, EntityPointer host, EntityType aircraftType, boolean bUseSubstitutes);
    boolean InterceptorSlotUpgradeToMax(int lPlayerID, int lSAMSiteID);
    boolean MissileReloadUpgrade(int lPlayerID, int lMissileSiteID);
    boolean InterceptorReloadUpgrade(int lPlayerID, int lSAMSiteID);
    boolean SentryRangeUpgrade(int lPlayerID, int lSentryGunID);
    boolean CommandPostHPUpgrade(int lPlayerID, int lCommandPostID);
    boolean BankAction(int lPlayerID, int lBankID, long oAmount, boolean bWithdraw);
    boolean CeaseFire(int lPlayerID, List<EntityPointer> Pointers);
    boolean RefuelEntity(int lPlayerID, EntityPointer entity);
    boolean KickAircraft(int lPlayerID, EntityPointer aircraft);
    boolean Blacklist(int lPlayerID, int lOtherPlayerID);
    boolean RefuelAircraftAtBase(int lPlayerID, EntityPointer pointerAirbase, EntityPointer pointerAircraft);
    boolean LoadLandUnit(int lPlayerID, EntityPointer unit, EntityPointer transport);
    boolean TransferLandUnit(int lPlayerID, EntityPointer sender, EntityPointer receiver, EntityPointer unit);
    boolean DropLandUnit(int lPlayerID, EntityPointer unit);
    boolean CaptureEntity(int lPlayerID, EntityPointer structure);
    boolean AdminDelete(int lPlayerID, EntityPointer pointer);
    boolean PurchaseShip(int lPlayerID, int lShipyardID, EntityType shipType, boolean bUseSubstitutes);
    boolean MoveOrder(int lPlayerID, List<EntityPointer> Movables, Map<Integer, GeoCoord> Coordinates);
    boolean UnitCommand(int lPlayerID, boolean bAnnounce, MoveOrders command, List<EntityPointer> Commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver);
    boolean MultiUnitCommand(int lPlayerID, MoveOrders command, List<EntityPointer> commandables, EntityPointer target, GeoCoord geoTarget, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver);
    boolean TransferCargo(int lPlayerID, EntityPointer entityFrom, EntityPointer entityTo, LootType typeToDeliver, int lDeliverID, int lQuantityToDeliver, boolean bLoadAsCargo, boolean bFromCargo);
    boolean LaunchMissile(int lPlayerID, int lSiteID, int lSlotNo, float fltTargetLatitude, float fltTargetLongitude, EntityPointer target, SystemType systemType, boolean bAirburst);
    boolean LaunchInterceptor(int lPlayerID, int lSiteID, int lSlotNo, int lTargetID, EntityType targetType, SystemType systemType);
    boolean LaunchTorpedo(int lPlayerID, int lSiteID, int lSlotNo, float fltTargetLatitude, float fltTargetLongitude, EntityPointer target, SystemType systemType);
    boolean Prospect(int lPlayerID);
    boolean DiveOrSurface(int lPlayerID, int lSubmarineID);
    boolean SellEntity(int lPlayerID, EntityPointer pointer);
    boolean SonarPing(int PlayerID, EntityPointer pinger);
    boolean ToggleAirbaseOpen(int PlayerID, EntityPointer host);
    boolean RadarScan(int lPlayerID, EntityPointer scanner);
    boolean SellLaunchable(int lPlayerID, int lMissileSiteID, int lSlotIndex, SystemType systemType);
    boolean GiveWealth(int lGiverID, int lReceiverID, int lAmount, ResourceType type);
    boolean SetStructuresOnOff(int lPlayerID, List<Integer> SiteIDs, boolean bOnline, EntityType structureType);
    boolean RepairEntity(int lPlayerID, EntityPointer pointer);
    boolean HealPlayer(int lPlayerID);
    boolean SetAvatar(int lPlayerID, int lAvatarID);
    boolean SetAllianceAvatar(int lPlayerID, int lAvatarID);
    boolean SetSAMSiteModes(int lPlayerID, List<EntityPointer> SitePointers, byte cMode);
    boolean SetArtilleryTarget(int lPlayerID, EntityPointer pointer, GeoCoord geoTarget, float fltRadius);
    boolean SetICBMSiloModes(int lPlayerID, List<Integer> SiteIDs, byte cMode);
    boolean SetSAMSiteEngagementSpeed(int lPlayerID, int lSiteID, float fltDistance);
    boolean SetEntityName(int lPlayerID, EntityPointer pointer, String strName);
    boolean CloseAccount(int lPlayerID);
    boolean UpgradeToNuclear(int lPlayerID, int lMissileSiteID);
    boolean UpgradeShipyard(int lPlayerID, int lShipyardID);
    boolean JoinAlliance(int lPlayerID, int lAllianceID);
    boolean LeaveAlliance(int lPlayerID);
    boolean DeclareWar(int lPlayerID, int lAllianceID);
    boolean ProposeSurrender(int lPlayerID, int lAllianceID);
    boolean AcceptSurrender(int lPlayerID, int lAllianceID);
    boolean RejectSurrender(int lPlayerID, int lAllianceID);
    boolean BreakAffiliation(int lPlayerID, int lAllianceID);
    boolean ProposeAffiliation(int lPlayerID, int lAllianceID);
    boolean AcceptAffiliation(int lPlayerID, int lAllianceID);
    boolean RejectAffiliation(int lPlayerID, int lAllianceID);
    boolean CancelAffiliation(int lPlayerID, int lAllianceID);
    boolean Promote(int lPromotor, int lPromotee);
    boolean AcceptJoin(int lLeaderID, int lMemberID);
    boolean RejectJoin(int lLeaderID, int lMemberID);
    boolean Kick(int lLeaderID, int lMemberID);
    boolean Unban(int lUnbannerID, int lUnbannedID);
    boolean TransferAccountAdmin(int lAdminID, int lFromID, int lToID);
    LaunchGame GetGame(); //ONLY TO BE USED IN THIS CONTEXT FOR GETTING LISTS OF ENTITIES OR CONFIG DATA.
    void BadAvatar(int lAvatarID);
    void BadImage(int lImageID);
    boolean CreateAlliance(int lCreatorID, String strName, String strDescription, int lAvatarID);
    boolean TempBan(int lPlayerID, String strReason, String strBanner);
    boolean PermaBan(int lPlayerID, String strReason, String strBanner);
    boolean SetToken(int lPlayerID, String strToken);
    boolean AvatarReset(int lPlayerAdminID, int lPlayerToResetID);
    boolean NameReset(int lPlayerAdminID, int lPlayerToResetID);
    void SpoofWarnings(int lPlayerID, LocationSpoofCheck spoofCheck);
    void MultiAccountingCheck(int lPlayerID);
    boolean TransferAccount(int lOldPlayerID, int lNewPlayerID);
    boolean ChangePlayerName(int lPlayerID, String strNewName);
    boolean ChangeAllianceName(int lPlayerID, String strNewName);
    boolean ChangeAllianceDescription(int lPlayerID, String strNewDescription);
    boolean ToggleAircraftReturn(int lPlayerID, EntityPointer aircraft);
    boolean ChangeAircraftHomebase(int lPlayerID, EntityPointer entity, EntityPointer homebase);
    boolean SendMessage(int lPlayerID, int lReceiverID, ChatChannel channel, String strMessage);
    boolean SetAllianceTaxRate(int lPlayerID, float fltTaxRate);
    boolean AllianceWithdraw(int lPlayerID, int lAmount);
    boolean AlliancePanic(int lPlayerID, int lAllianceID);
    void SendUserAlert(User user, String strTitle, String strBody, boolean bRespectInterval, boolean bSendWhileOnline);
    
    String GetPlayerName(int lPlayerID);
    User GetUser(int lPlayerID);
    void AdminReport(LaunchReport report);
    void NotifyDeviceChecksCompleteFailure(String strPlayerName);
    void NotifyDeviceChecksAPIFailure(String strPlayerName);
    void NotifyDeviceCheckFailure(User user);
    void NotifyIPProscribed(User user);
    void NotifyLocationProscribed(User user);
    void NotifyAccountRestricted(User user);
    
    boolean GetIpAddressProscribed(String strIPAddress);
    boolean GetLocationProscribed(GeoCoord geoLocation);
	
	CounterShieldEngine GetCounterShield();
}
