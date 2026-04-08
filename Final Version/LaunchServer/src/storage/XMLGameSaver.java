/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

import launch.game.entities.conceptuals.StoredAirplane;
import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import launch.game.Alliance;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchServerGame;
import launch.game.entities.*;
import launch.game.systems.MissileSystem;
import launch.game.User;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.ShipProductionOrder;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.AircraftSystem;
import launch.game.systems.CargoSystem;
import launch.game.systems.ResourceSystem;

import launch.game.treaties.Treaty;
import launch.game.treaties.Treaty.Type;
import launch.game.treaties.War;
import launch.utilities.LaunchUtilities;
import launch.utilities.ShortDelay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author tobster
 */
public class XMLGameSaver
{
    
    public static void SaveGameToXMLFile(GameLoadSaveListener listener, LaunchServerGame game, String strGameFile)
    {
        /*try
        {
            Document doc;
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //Create game node.
            doc = docBuilder.newDocument();
            Element eleGame = doc.createElement(XMLDefs.GAME);
            doc.appendChild(eleGame);
            
            //Save users.
            Element elements = AddNode(doc, eleGame, XMLDefs.USERS);

            for(User user : game.GetUsers())
            {
                Player player = game.GetPlayer(user.GetPlayerID());
                String strPlayerName = player == null? "[DEAD ACCOUNT]" : player.GetName();
                
                Element eleUser = AddNode(doc, elements, XMLDefs.USER, XMLDefs.NAME, strPlayerName);
                AddNode(doc, eleUser, XMLDefs.IMEI, user.GetIMEI());
                AddNode(doc, eleUser, XMLDefs.PLAYERID, user.GetPlayerID());
                AddNode(doc, eleUser, XMLDefs.BAN_STATE, user.GetBanState().ordinal());
                AddNode(doc, eleUser, XMLDefs.NEXT_BAN_TIME, user.GetNextBanTime());
                AddNode(doc, eleUser, XMLDefs.BAN_DURATION_REMAINING, user.GetBanDurationRemaining());
                AddNode(doc, eleUser, XMLDefs.BAN_REASON, user.GetBanReason());
                AddNode(doc, eleUser, XMLDefs.LAST_IP, user.GetLastIP());
                AddNode(doc, eleUser, XMLDefs.LAST_CONNECTION_MOBILE, user.GetLastTypeMobile());
                AddNode(doc, eleUser, XMLDefs.LAST_CHECKED, user.GetDeviceCheckedDate());
                AddNode(doc, eleUser, XMLDefs.LAST_CHECK_FAILED, user.GetLastDeviceCheckFailed());
                AddNode(doc, eleUser, XMLDefs.CHECK_API_FAILED, user.GetDeviceChecksAPIFailed());
                AddNode(doc, eleUser, XMLDefs.PROSCRIBED, user.GetProscribed());
                AddNode(doc, eleUser, XMLDefs.CHECK_FAIL_CODE, user.GetDeviceChecksFailCode());
                AddNode(doc, eleUser, XMLDefs.PROFILE_MATCH, user.GetProfileMatch());
                AddNode(doc, eleUser, XMLDefs.BASIC_INTEGRITY, user.GetBasicIntegrity());
                AddNode(doc, eleUser, XMLDefs.APPROVED, user.GetApproved());
                AddNode(doc, eleUser, XMLDefs.EXPIRED, user.GetExpiredOn());
                AddNode(doc, eleUser, XMLDefs.DEVICE_HASH, user.GetDeviceShortHash());
                AddNode(doc, eleUser, XMLDefs.APP_LIST_HASH, user.GetAppListShortHash());
                AddNode(doc, eleUser, XMLDefs.TOKEN, user.GetToken());
                Element eleReports = doc.createElement(XMLDefs.REPORTS);
                
                for(LaunchReport report : user.GetReports())
                {
                    Element eleReport = doc.createElement(XMLDefs.REPORT);
                    AddNode(doc, eleReport, XMLDefs.TIME_START, report.GetStartTime());
                    AddNode(doc, eleReport, XMLDefs.TIME_END, report.GetEndTime());
                    AddNode(doc, eleReport, XMLDefs.MESSAGE, report.GetMessage());
                    AddNode(doc, eleReport, XMLDefs.IS_MAJOR, report.GetMajor());
                    AddNode(doc, eleReport, XMLDefs.LEFT_ID, report.GetLeftID());
                    AddNode(doc, eleReport, XMLDefs.RIGHT_ID, report.GetRightID());
                    AddNode(doc, eleReport, XMLDefs.TIMES, report.GetTimes());
                    AddNode(doc, eleReport, XMLDefs.FLAGS, report.GetFlags());
                    eleReports.appendChild(eleReport);
                }
                
                eleUser.appendChild(eleReports);
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(new File("users.xml")));
        }
        catch(ParserConfigurationException ex)
        {
            listener.SaveError(String.format("XML parser configuration error when saving users from %s.", "users.xml"));
        }
        catch(TransformerException ex)
        {
            listener.SaveError(String.format("Transformer error when saving users from %s.", "users.xml"));
        }*/
        
        try
        {
            Document doc;
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //Create game node.
            doc = docBuilder.newDocument();
            Element eleGame = doc.createElement(XMLDefs.GAME);
            doc.appendChild(eleGame);
            
            // Save game stats
            Element eleGameStats = AddNode(doc, eleGame, XMLDefs.GAME_STATS);

            AddNode(doc, eleGameStats, XMLDefs.NEXT_PLAYER_ID, game.GetPlayerIndex());
            
            //Save users.
            Element elements = AddNode(doc, eleGame, XMLDefs.USERS);

            for(User user : game.GetUsers())
            {
                Player player = game.GetPlayer(user.GetPlayerID());
                
                //Don't save users that have a null player. In the past, this woul store the player as a dead acount. No more.
                if(player != null)
                {
                    String strPlayerName = player.GetName();

                    Element eleUser = AddNode(doc, elements, XMLDefs.USER, XMLDefs.NAME, strPlayerName);
                    
                    if(user.GetGoogleID() != null && !user.GetGoogleID().isEmpty())
                        AddNode(doc, eleUser, XMLDefs.GOOGLE_ID, user.GetGoogleID());
                    
                    AddNode(doc, eleUser, XMLDefs.IMEI, user.GetIMEI());
                    AddNode(doc, eleUser, XMLDefs.PLAYERID, user.GetPlayerID());
                    AddNode(doc, eleUser, XMLDefs.BAN_STATE, user.GetBanState().ordinal());
                    AddNode(doc, eleUser, XMLDefs.NEXT_BAN_TIME, user.GetNextBanTime());
                    AddNode(doc, eleUser, XMLDefs.BAN_DURATION_REMAINING, user.GetBanDurationRemaining());
                    AddNode(doc, eleUser, XMLDefs.BAN_REASON, user.GetBanReason());
                    AddNode(doc, eleUser, XMLDefs.LAST_IP, user.GetLastIP());
                    AddNode(doc, eleUser, XMLDefs.LAST_CONNECTION_MOBILE, user.GetLastTypeMobile());
                    AddNode(doc, eleUser, XMLDefs.LAST_CHECKED, user.GetDeviceCheckedDate());
                    AddNode(doc, eleUser, XMLDefs.LAST_CHECK_FAILED, user.GetLastDeviceCheckFailed());
                    AddNode(doc, eleUser, XMLDefs.CHECK_API_FAILED, user.GetDeviceChecksAPIFailed());
                    AddNode(doc, eleUser, XMLDefs.PROSCRIBED, user.GetProscribed());
                    AddNode(doc, eleUser, XMLDefs.CHECK_FAIL_CODE, user.GetDeviceChecksFailCode());
                    AddNode(doc, eleUser, XMLDefs.PROFILE_MATCH, user.GetProfileMatch());
                    AddNode(doc, eleUser, XMLDefs.BASIC_INTEGRITY, user.GetBasicIntegrity());
                    AddNode(doc, eleUser, XMLDefs.APPROVED, user.GetApproved());
                    AddNode(doc, eleUser, XMLDefs.EXPIRED, user.GetExpiredOn());
                    AddNode(doc, eleUser, XMLDefs.DEVICE_HASH, user.GetDeviceID());
                    AddNode(doc, eleUser, XMLDefs.APP_LIST_HASH, user.GetAppListShortHash());
                    AddNode(doc, eleUser, XMLDefs.TOKEN, user.GetToken());
                }
                    
                /*Element eleReports = doc.createElement(XMLDefs.REPORTS);
                
                for(LaunchReport report : user.GetReports())
                {
                    Element eleReport = doc.createElement(XMLDefs.REPORT);
                    AddNode(doc, eleReport, XMLDefs.TIME_START, report.GetStartTime());
                    AddNode(doc, eleReport, XMLDefs.TIME_END, report.GetEndTime());
                    AddNode(doc, eleReport, XMLDefs.MESSAGE, report.GetMessage());
                    AddNode(doc, eleReport, XMLDefs.IS_MAJOR, report.GetMajor());
                    AddNode(doc, eleReport, XMLDefs.LEFT_ID, report.GetLeftID());
                    AddNode(doc, eleReport, XMLDefs.RIGHT_ID, report.GetRightID());
                    AddNode(doc, eleReport, XMLDefs.TIMES, report.GetTimes());
                    AddNode(doc, eleReport, XMLDefs.FLAGS, report.GetFlags());
                    eleReports.appendChild(eleReport);
                }
                
                eleUser.appendChild(eleReports);*/
            }
            
            //Save alliances.
            elements = AddNode(doc, eleGame, XMLDefs.ALLIANCES);

            for(Alliance alliance : game.GetAlliances())
            {
                Element eleAlliance = AddNode(doc, elements, XMLDefs.ALLIANCE, XMLDefs.ID, Integer.toString(alliance.GetID()), XMLDefs.NAME, alliance.GetName());
                AddNode(doc, eleAlliance, XMLDefs.DESCRIPTION, alliance.GetDescription());
                AddNode(doc, eleAlliance, XMLDefs.AVATAR, alliance.GetAvatarID());
                AddNode(doc, eleAlliance, XMLDefs.WEALTH, alliance.GetWealth());
                AddNode(doc, eleAlliance, XMLDefs.TAX_RATE, alliance.GetTaxRate());
                AddNode(doc, eleAlliance, XMLDefs.WARS_WON, alliance.GetWarsWon());
                AddNode(doc, eleAlliance, XMLDefs.WARS_LOST, alliance.GetWarsLost());
                AddNode(doc, eleAlliance, XMLDefs.ALLIANCE_DISBAND_COUNT, alliance.GetEnemyAllianceDisbands());
                AddNode(doc, eleAlliance, XMLDefs.FOUNDER_NAME, alliance.GetFounder());
                AddNode(doc, eleAlliance, XMLDefs.AFFILIATIONS_BROKEN, alliance.GetAffiliationsBroken());
                AddNode(doc, eleAlliance, XMLDefs.ICBM_COUNT, alliance.GetICBMCount());
                AddNode(doc, eleAlliance, XMLDefs.ABM_COUNT, alliance.GetABMCount());
                AddNode(doc, eleAlliance, XMLDefs.FOUNDED_TIME, alliance.GetFoundedTime());               
            }
            
            //Save treaties.
            elements = AddNode(doc, eleGame, XMLDefs.TREATIES);
            
            for(Treaty treaty : game.GetTreaties())
            {
                Element eleTreaty = AddNode(doc, elements, XMLDefs.TREATY, XMLDefs.ID, Integer.toString(treaty.GetID()));
                AddNode(doc, eleTreaty, XMLDefs.ALLIANCE1, treaty.GetAllianceID1());
                AddNode(doc, eleTreaty, XMLDefs.ALLIANCE2, treaty.GetAllianceID2());
                Type type = treaty.GetType();
                AddNode(doc, eleTreaty, XMLDefs.TYPE, type.ordinal());
                
                if(type == Type.WAR)
                {
                    War war = (War)treaty;
                    
                    AddNode(doc, eleTreaty, XMLDefs.KILLS1, war.GetKills1());
                    AddNode(doc, eleTreaty, XMLDefs.DEATHS1, war.GetDeaths1());
                    AddNode(doc, eleTreaty, XMLDefs.OFFENCE_SPENDING1, war.GetOffenceSpending1());
                    AddNode(doc, eleTreaty, XMLDefs.DEFENCE_SPENDING1, war.GetDefenceSpending1());
                    AddNode(doc, eleTreaty, XMLDefs.DAMAGE_INFLICTED1, war.GetDamageInflicted1());
                    AddNode(doc, eleTreaty, XMLDefs.DAMAGE_RECEIVED1, war.GetDamageReceived1());
                    AddNode(doc, eleTreaty, XMLDefs.INCOME1, war.GetIncome1());
                    AddNode(doc, eleTreaty, XMLDefs.KILLS2, war.GetKills2());
                    AddNode(doc, eleTreaty, XMLDefs.DEATHS2, war.GetDeaths2());
                    AddNode(doc, eleTreaty, XMLDefs.OFFENCE_SPENDING2, war.GetOffenceSpending2());
                    AddNode(doc, eleTreaty, XMLDefs.DEFENCE_SPENDING2, war.GetDefenceSpending2());
                    AddNode(doc, eleTreaty, XMLDefs.DAMAGE_INFLICTED2, war.GetDamageInflicted2());
                    AddNode(doc, eleTreaty, XMLDefs.DAMAGE_RECEIVED2, war.GetDamageReceived2());
                    AddNode(doc, eleTreaty, XMLDefs.INCOME2, war.GetIncome2());
                    AddNode(doc, eleTreaty, XMLDefs.START_TIME, war.GetStartTime());
                }
            }
            
            //Save players.
            elements = AddNode(doc, eleGame, XMLDefs.PLAYERS);

            for(Player player : game.GetPlayers())
            {
                Element elePlayer = AddNode(doc, elements, XMLDefs.PLAYER, XMLDefs.ID, Integer.toString(player.GetID()), XMLDefs.NAME, player.GetName());
                AddPositionNode(doc, elePlayer, XMLDefs.POSITION, player.GetPosition());
                AddNode(doc, elePlayer, XMLDefs.AVATAR, player.GetAvatarID());
                AddNode(doc, elePlayer, XMLDefs.LAST_SEEN, player.GetLastSeen());
                AddNode(doc, elePlayer, XMLDefs.STATE_CHANGE, player.GetStateTimeRemaining());
                AddNode(doc, elePlayer, XMLDefs.ALLIANCE_ID, player.GetAllianceIDForDataStorage());
                AddNode(doc, elePlayer, XMLDefs.FLAGS1, player.GetFlags1());
                AddNode(doc, elePlayer, XMLDefs.FLAGS2, player.GetFlags2());
                AddNode(doc, elePlayer, XMLDefs.ALLIANCE_COOLOFF_TIME, player.GetAllianceCooloffRemaining());
                AddNode(doc, elePlayer, XMLDefs.KILLS, player.GetKills());
                AddNode(doc, elePlayer, XMLDefs.DEATHS, player.GetDeaths());
                AddNode(doc, elePlayer, XMLDefs.OFFENCE_SPENDING, player.GetOffenceSpending());
                AddNode(doc, elePlayer, XMLDefs.DEFENCE_SPENDING, player.GetDefenceSpending());
                AddNode(doc, elePlayer, XMLDefs.DAMAGE_INFLICTED, player.GetDamageInflicted());
                AddNode(doc, elePlayer, XMLDefs.DAMAGE_RECEIVED, player.GetDamageReceived());
                AddNode(doc, elePlayer, XMLDefs.RANK, player.GetRank());
                AddNode(doc, elePlayer, XMLDefs.EXPERIENCE, player.GetExperience());
                AddNode(doc, elePlayer, XMLDefs.TOTAL_KILLS, player.GetTotalKills());
                AddNode(doc, elePlayer, XMLDefs.TOTAL_DEATHS, player.GetTotalDeaths());
                AddNode(doc, elePlayer, XMLDefs.IS_A_MEMBER, player.IsAMember());
                AddNode(doc, elePlayer, XMLDefs.JOIN_TIME, player.GetJoinTime());
                AddNode(doc, elePlayer, XMLDefs.DEFENSE_VALUE, player.GetDefenseValue());
                AddNode(doc, elePlayer, XMLDefs.OFFENSE_VALUE, player.GetOffenseValue());
                AddNode(doc, elePlayer, XMLDefs.NEUTRAL_VALUE, player.GetNeutralValue());
                AddNode(doc, elePlayer, XMLDefs.DISTANCE_TRAVELED, player.GetDistanceTraveled());
                AddNode(doc, elePlayer, XMLDefs.DISTANCE_TRAVELED_TODAY, player.GetDistanceTraveledToday());
                AddNode(doc, elePlayer, XMLDefs.AIRDROP_COOLDOWN, player.GetAirdropCooldownRemaining());
                AddNode(doc, elePlayer, XMLDefs.PROSPECT_COOLDOWN, player.GetProspectCooldownRemaining());
                AddNode(doc, elePlayer, XMLDefs.CITY_COUNT_LAST_WEEK, player.GetCityCountLastWeek());
                AddNode(doc, elePlayer, XMLDefs.CHAMP_COUNT, player.GetChampionCount());
                AddNode(doc, elePlayer, XMLDefs.ADMIN_MEMBER, player.GetAdminMember());
                AddNode(doc, elePlayer, XMLDefs.BLACKLIST, LaunchUtilities.GetIntListData(player.GetBlacklist()));
                AddCargoSystem(doc, elePlayer, player.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.MISSILES);
            
            for(Missile missile : game.GetMissiles())
            {
                Element eleMissile = AddNode(doc, elements, XMLDefs.MISSILE, XMLDefs.ID, missile.GetID());
                AddPositionNode(doc, eleMissile, XMLDefs.POSITION, missile.GetPosition());
                AddNode(doc, eleMissile, XMLDefs.TYPE, missile.GetType());
                AddNode(doc, eleMissile, XMLDefs.OWNER_ID, missile.GetOwnerID());
                AddPositionNode(doc, eleMissile, XMLDefs.TARGET, missile.GetTarget());
                AddPositionNode(doc, eleMissile, XMLDefs.ORIGIN, missile.GetOrigin());
                AddNode(doc, eleMissile, XMLDefs.FUZE_MODE, missile.GetAirburst());
                AddNode(doc, eleMissile, XMLDefs.SPEED, missile.GetSpeed());

                if(missile.GetTracking())
                    AddEntityPointerNode(doc, eleMissile, XMLDefs.TARGET_ENTITY, missile.GetTargetEntity());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.INTERCEPTORS);
            
            for(Interceptor interceptor : game.GetInterceptors())
            {
                Element eleInterceptor = AddNode(doc, elements, XMLDefs.INTERCEPTOR, XMLDefs.ID, interceptor.GetID());
                AddPositionNode(doc, eleInterceptor, XMLDefs.POSITION, interceptor.GetPosition());
                AddNode(doc, eleInterceptor, XMLDefs.OWNER_ID, interceptor.GetOwnerID());
                AddNode(doc, eleInterceptor, XMLDefs.TARGET_ID, interceptor.GetTargetID());
                AddNode(doc, eleInterceptor, XMLDefs.TYPE, interceptor.GetType());
                AddNode(doc, eleInterceptor, XMLDefs.PLAYER_LAUNCHED, interceptor.GetPlayerLaunched());
                AddNode(doc, eleInterceptor, XMLDefs.TARGET_TYPE, interceptor.GetTargetType().ordinal());
                AddNode(doc, eleInterceptor, XMLDefs.LAUNCHED_BY_ID, interceptor.GetLaunchedByID());
                AddNode(doc, eleInterceptor, XMLDefs.DISTANCE_TRAVELED, interceptor.GetDistanceTraveled());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.TORPEDOES);
            
            for(Torpedo torpedo : game.GetTorpedoes())
            {
                Element eleTorpedo = AddNode(doc, elements, XMLDefs.TORPEDO, XMLDefs.ID, torpedo.GetID());
                AddPositionNode(doc, eleTorpedo, XMLDefs.POSITION, torpedo.GetPosition());
                AddNode(doc, eleTorpedo, XMLDefs.TYPE, torpedo.GetType());
                AddNode(doc, eleTorpedo, XMLDefs.OWNER_ID, torpedo.GetOwnerID());
                AddNode(doc, eleTorpedo, XMLDefs.HOMING, torpedo.GetHoming());
                AddNode(doc, eleTorpedo, XMLDefs.RANGE, torpedo.GetRange());
                AddNode(doc, eleTorpedo, XMLDefs.DISTANCE_TRAVELED, torpedo.GetDistanceTraveled());
                AddPositionNode(doc, eleTorpedo, XMLDefs.TARGET, torpedo.GetGeoTarget());
                AddNode(doc, eleTorpedo, XMLDefs.STATE, torpedo.GetState().ordinal());
                
                if(torpedo.GetTarget() != null)
                    AddEntityPointerNode(doc, eleTorpedo, XMLDefs.TARGET_ENTITY, torpedo.GetTarget());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.MISSILE_SITES);
            
            for(MissileSite missileSite : game.GetMissileSites())
            {
                Element eleMissileSite = AddNode(doc, elements, XMLDefs.MISSILE_SITE, XMLDefs.ID, missileSite.GetID(), XMLDefs.NAME, missileSite.GetName());
                AddPositionNode(doc, eleMissileSite, XMLDefs.POSITION, missileSite.GetPosition());
                AddNode(doc, eleMissileSite, XMLDefs.HP, missileSite.GetHP());
                AddNode(doc, eleMissileSite, XMLDefs.MAX_HP, missileSite.GetMaxHP());
                AddNode(doc, eleMissileSite, XMLDefs.OWNER_ID, missileSite.GetOwnerID());
                AddNode(doc, eleMissileSite, XMLDefs.FLAGS, missileSite.GetFlags());
                AddNode(doc, eleMissileSite, XMLDefs.STATE_TIME, missileSite.GetStateTimeRemaining());
                AddNode(doc, eleMissileSite, XMLDefs.ICBM_SILO, missileSite.CanTakeICBM());
                AddMissileSystem(doc, eleMissileSite, missileSite.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                AddNode(doc, eleMissileSite, XMLDefs.MODE, missileSite.GetMode());
                AddNode(doc, eleMissileSite, XMLDefs.RETALIATING, missileSite.Retaliating());
                AddNode(doc, eleMissileSite, XMLDefs.RETALIATE_TIME, missileSite.GetRetaliateTime());
                AddNode(doc, eleMissileSite, XMLDefs.TARGET_ID, missileSite.GetRetaliationTarget());
                AddNode(doc, eleMissileSite, XMLDefs.VISIBLE, missileSite.GetVisible());
                AddNode(doc, eleMissileSite, XMLDefs.VISIBLE_TIME, missileSite.GetVisibleTimeRemaining());
                AddNode(doc, eleMissileSite, XMLDefs.BUILT_BY_ID, missileSite.GetBuiltByID());
                AddResourceSystem(doc, eleMissileSite, missileSite.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.ARTILLERY_GUNS);
            
            for(ArtilleryGun artillery : game.GetArtilleryGuns())
            {
                Element eleArtilleryGun = AddNode(doc, elements, XMLDefs.ARTILLERY_GUN, XMLDefs.ID, artillery.GetID(), XMLDefs.NAME, artillery.GetName());
                AddPositionNode(doc, eleArtilleryGun, XMLDefs.POSITION, artillery.GetPosition());
                AddNode(doc, eleArtilleryGun, XMLDefs.HP, artillery.GetHP());
                AddNode(doc, eleArtilleryGun, XMLDefs.MAX_HP, artillery.GetMaxHP());
                AddNode(doc, eleArtilleryGun, XMLDefs.OWNER_ID, artillery.GetOwnerID());
                AddNode(doc, eleArtilleryGun, XMLDefs.FLAGS, artillery.GetFlags());
                AddNode(doc, eleArtilleryGun, XMLDefs.STATE_TIME, artillery.GetStateTimeRemaining());
                AddMissileSystem(doc, eleArtilleryGun, artillery.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                AddNode(doc, eleArtilleryGun, XMLDefs.VISIBLE, artillery.GetVisible());
                AddNode(doc, eleArtilleryGun, XMLDefs.VISIBLE_TIME, artillery.GetVisibleTimeRemaining());
                AddNode(doc, eleArtilleryGun, XMLDefs.MODE, artillery.GetMode());
                AddNode(doc, eleArtilleryGun, XMLDefs.BUILT_BY_ID, artillery.GetBuiltByID());
                AddResourceSystem(doc, eleArtilleryGun, artillery.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                
                if(artillery.HasFireOrder())
                {
                    AddPositionNode(doc, eleArtilleryGun, XMLDefs.ARTILLERY_TARGET, artillery.GetFireOrder().GetGeoTarget());
                    AddNode(doc, eleArtilleryGun, XMLDefs.RADIUS, artillery.GetFireOrder().GetRadius());
                }
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SAM_SITES);
            
            for(SAMSite samSite : game.GetSAMSites())
            {
                Element eleSAMSite = AddNode(doc, elements, XMLDefs.SAM_SITE, XMLDefs.ID, samSite.GetID(), XMLDefs.NAME, samSite.GetName());
                AddPositionNode(doc, eleSAMSite, XMLDefs.POSITION, samSite.GetPosition());
                AddNode(doc, eleSAMSite, XMLDefs.HP, samSite.GetHP());
                AddNode(doc, eleSAMSite, XMLDefs.MAX_HP, samSite.GetMaxHP());
                AddNode(doc, eleSAMSite, XMLDefs.OWNER_ID, samSite.GetOwnerID());
                AddNode(doc, eleSAMSite, XMLDefs.FLAGS, samSite.GetFlags());
                AddNode(doc, eleSAMSite, XMLDefs.STATE_TIME, samSite.GetStateTimeRemaining());
                AddNode(doc, eleSAMSite, XMLDefs.MODE, samSite.GetMode());
                AddMissileSystem(doc, eleSAMSite, samSite.GetInterceptorSystem(), XMLDefs.INTERCEPTOR_SYSTEM);
                AddNode(doc, eleSAMSite, XMLDefs.ANTI_BALLISTIC, samSite.GetIsABMSilo());
                AddNode(doc, eleSAMSite, XMLDefs.ENGAGEMENT_SPEED, samSite.GetEngagementSpeed());
                AddNode(doc, eleSAMSite, XMLDefs.VISIBLE, samSite.GetVisible());
                AddNode(doc, eleSAMSite, XMLDefs.VISIBLE_TIME, samSite.GetVisibleTimeRemaining());
                AddNode(doc, eleSAMSite, XMLDefs.BUILT_BY_ID, samSite.GetBuiltByID());
                AddResourceSystem(doc, eleSAMSite, samSite.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SENTRY_GUNS);
            
            for(SentryGun sentryGun : game.GetSentryGuns())
            {
                Element eleSentryGun = AddNode(doc, elements, XMLDefs.SENTRY_GUN, XMLDefs.ID, sentryGun.GetID(), XMLDefs.NAME, sentryGun.GetName());
                AddPositionNode(doc, eleSentryGun, XMLDefs.POSITION, sentryGun.GetPosition());
                AddNode(doc, eleSentryGun, XMLDefs.HP, sentryGun.GetHP());
                AddNode(doc, eleSentryGun, XMLDefs.MAX_HP, sentryGun.GetMaxHP());
                AddNode(doc, eleSentryGun, XMLDefs.OWNER_ID, sentryGun.GetOwnerID());
                AddNode(doc, eleSentryGun, XMLDefs.FLAGS, sentryGun.GetFlags());
                AddNode(doc, eleSentryGun, XMLDefs.STATE_TIME, sentryGun.GetStateTimeRemaining());
                AddNode(doc, eleSentryGun, XMLDefs.RELOAD_REMAINING, sentryGun.GetReloadTimeRemaining());
                AddNode(doc, eleSentryGun, XMLDefs.VISIBLE, sentryGun.GetVisible());
                AddNode(doc, eleSentryGun, XMLDefs.VISIBLE_TIME, sentryGun.GetVisibleTimeRemaining());
                AddNode(doc, eleSentryGun, XMLDefs.BUILT_BY_ID, sentryGun.GetBuiltByID());
                AddNode(doc, eleSentryGun, XMLDefs.WATCH_TOWER, sentryGun.GetIsWatchTower());
                AddResourceSystem(doc, eleSentryGun, sentryGun.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SCRAP_YARDS);
            
            for(ScrapYard yard : game.GetScrapYards())
            {
                Element eleScrapYard = AddNode(doc, elements, XMLDefs.SCRAP_YARD, XMLDefs.ID, yard.GetID(), XMLDefs.NAME, yard.GetName());
                AddPositionNode(doc, eleScrapYard, XMLDefs.POSITION, yard.GetPosition());
                AddNode(doc, eleScrapYard, XMLDefs.HP, yard.GetHP());
                AddNode(doc, eleScrapYard, XMLDefs.MAX_HP, yard.GetMaxHP());
                AddNode(doc, eleScrapYard, XMLDefs.OWNER_ID, yard.GetOwnerID());
                AddNode(doc, eleScrapYard, XMLDefs.FLAGS, yard.GetFlags());
                AddNode(doc, eleScrapYard, XMLDefs.STATE_TIME, yard.GetStateTimeRemaining());
                AddNode(doc, eleScrapYard, XMLDefs.VISIBLE, yard.GetVisible());
                AddNode(doc, eleScrapYard, XMLDefs.VISIBLE_TIME, yard.GetVisibleTimeRemaining());
                AddNode(doc, eleScrapYard, XMLDefs.BUILT_BY_ID, yard.GetBuiltByID());
                AddResourceSystem(doc, eleScrapYard, yard.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.ORE_MINES);
            
            for(OreMine oreMine : game.GetOreMines())
            {
                Element eleOreMine = AddNode(doc, elements, XMLDefs.ORE_MINE, XMLDefs.ID, oreMine.GetID(), XMLDefs.NAME, oreMine.GetName());
                AddPositionNode(doc, eleOreMine, XMLDefs.POSITION, oreMine.GetPosition());
                AddNode(doc, eleOreMine, XMLDefs.HP, oreMine.GetHP());
                AddNode(doc, eleOreMine, XMLDefs.MAX_HP, oreMine.GetMaxHP());
                AddNode(doc, eleOreMine, XMLDefs.OWNER_ID, oreMine.GetOwnerID());
                AddNode(doc, eleOreMine, XMLDefs.FLAGS, oreMine.GetFlags());
                AddNode(doc, eleOreMine, XMLDefs.STATE_TIME, oreMine.GetStateTimeRemaining());
                AddNode(doc, eleOreMine, XMLDefs.TYPE, oreMine.GetType().ordinal());
                AddNode(doc, eleOreMine, XMLDefs.VISIBLE, oreMine.GetVisible());
                AddNode(doc, eleOreMine, XMLDefs.VISIBLE_TIME, oreMine.GetVisibleTimeRemaining());
                AddNode(doc, eleOreMine, XMLDefs.BUILT_BY_ID, oreMine.GetBuiltByID());
                AddNode(doc, eleOreMine, XMLDefs.DEPOSIT_ID, oreMine.GetDepositID());
                AddResourceSystem(doc, eleOreMine, oreMine.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.RADAR_STATIONS);
            
            for(RadarStation radarStation : game.GetRadarStations())
            {
                Element eleRadarStation = AddNode(doc, elements, XMLDefs.RADAR_STATION, XMLDefs.ID, radarStation.GetID(), XMLDefs.NAME, radarStation.GetName());
                AddPositionNode(doc, eleRadarStation, XMLDefs.POSITION, radarStation.GetPosition());
                AddNode(doc, eleRadarStation, XMLDefs.HP, radarStation.GetHP());
                AddNode(doc, eleRadarStation, XMLDefs.MAX_HP, radarStation.GetMaxHP());
                AddNode(doc, eleRadarStation, XMLDefs.OWNER_ID, radarStation.GetOwnerID());
                AddNode(doc, eleRadarStation, XMLDefs.FLAGS, radarStation.GetFlags());
                AddNode(doc, eleRadarStation, XMLDefs.STATE_TIME, radarStation.GetStateTimeRemaining());
                AddNode(doc, eleRadarStation, XMLDefs.RADAR_ACTIVE, radarStation.GetRadarActive());
                AddNode(doc, eleRadarStation, XMLDefs.VISIBLE, radarStation.GetVisible());
                AddNode(doc, eleRadarStation, XMLDefs.VISIBLE_TIME, radarStation.GetVisibleTimeRemaining());
                AddNode(doc, eleRadarStation, XMLDefs.BUILT_BY_ID, radarStation.GetBuiltByID());
                AddResourceSystem(doc, eleRadarStation, radarStation.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.PROCESSORS);
            
            for(Processor processor : game.GetProcessors())
            {
                Element eleProcessor = AddNode(doc, elements, XMLDefs.PROCESSOR, XMLDefs.ID, processor.GetID(), XMLDefs.NAME, processor.GetName());
                AddPositionNode(doc, eleProcessor, XMLDefs.POSITION, processor.GetPosition());
                AddNode(doc, eleProcessor, XMLDefs.HP, processor.GetHP());
                AddNode(doc, eleProcessor, XMLDefs.MAX_HP, processor.GetMaxHP());
                AddNode(doc, eleProcessor, XMLDefs.OWNER_ID, processor.GetOwnerID());
                AddNode(doc, eleProcessor, XMLDefs.FLAGS, processor.GetFlags());
                AddNode(doc, eleProcessor, XMLDefs.STATE_TIME, processor.GetStateTimeRemaining());
                AddNode(doc, eleProcessor, XMLDefs.TYPE, processor.GetType().ordinal());
                AddNode(doc, eleProcessor, XMLDefs.VISIBLE, processor.GetVisible());
                AddNode(doc, eleProcessor, XMLDefs.VISIBLE_TIME, processor.GetVisibleTimeRemaining());
                AddNode(doc, eleProcessor, XMLDefs.BUILT_BY_ID, processor.GetBuiltByID());
                AddResourceSystem(doc, eleProcessor, processor.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.DISTRIBUTORS);
            
            for(Distributor distributor : game.GetDistributors())
            {
                Element eleDistributor = AddNode(doc, elements, XMLDefs.DISTRIBUTOR, XMLDefs.ID, distributor.GetID(), XMLDefs.NAME, distributor.GetName());
                AddPositionNode(doc, eleDistributor, XMLDefs.POSITION, distributor.GetPosition());
                AddNode(doc, eleDistributor, XMLDefs.HP, distributor.GetHP());
                AddNode(doc, eleDistributor, XMLDefs.MAX_HP, distributor.GetMaxHP());
                AddNode(doc, eleDistributor, XMLDefs.OWNER_ID, distributor.GetOwnerID());
                AddNode(doc, eleDistributor, XMLDefs.FLAGS, distributor.GetFlags());
                AddNode(doc, eleDistributor, XMLDefs.STATE_TIME, distributor.GetStateTimeRemaining());
                AddNode(doc, eleDistributor, XMLDefs.TYPE, distributor.GetType().ordinal());
                AddNode(doc, eleDistributor, XMLDefs.VISIBLE, distributor.GetVisible());
                AddNode(doc, eleDistributor, XMLDefs.VISIBLE_TIME, distributor.GetVisibleTimeRemaining());
                AddNode(doc, eleDistributor, XMLDefs.BUILT_BY_ID, distributor.GetBuiltByID());
                AddResourceSystem(doc, eleDistributor, distributor.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.COMMAND_POSTS);
            
            for(CommandPost commandPost : game.GetCommandPosts())
            {
                Element eleCommandPost = AddNode(doc, elements, XMLDefs.COMMAND_POST, XMLDefs.ID, commandPost.GetID(), XMLDefs.NAME, commandPost.GetName());
                AddPositionNode(doc, eleCommandPost, XMLDefs.POSITION, commandPost.GetPosition());
                AddNode(doc, eleCommandPost, XMLDefs.HP, commandPost.GetHP());
                AddNode(doc, eleCommandPost, XMLDefs.MAX_HP, commandPost.GetMaxHP());
                AddNode(doc, eleCommandPost, XMLDefs.OWNER_ID, commandPost.GetOwnerID());
                AddNode(doc, eleCommandPost, XMLDefs.FLAGS, commandPost.GetFlags());
                AddNode(doc, eleCommandPost, XMLDefs.STATE_TIME, commandPost.GetStateTimeRemaining());
                AddNode(doc, eleCommandPost, XMLDefs.VISIBLE, commandPost.GetVisible());
                AddNode(doc, eleCommandPost, XMLDefs.VISIBLE_TIME, commandPost.GetVisibleTimeRemaining());
                AddNode(doc, eleCommandPost, XMLDefs.BUILT_BY_ID, commandPost.GetBuiltByID());
                AddResourceSystem(doc, eleCommandPost, commandPost.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.BANKS);
            
            for(Bank bank : game.GetBanks())
            {
                Element eleBank = AddNode(doc, elements, XMLDefs.BANK, XMLDefs.ID, bank.GetID(), XMLDefs.NAME, bank.GetName());
                AddPositionNode(doc, eleBank, XMLDefs.POSITION, bank.GetPosition());
                AddNode(doc, eleBank, XMLDefs.HP, bank.GetHP());
                AddNode(doc, eleBank, XMLDefs.MAX_HP, bank.GetMaxHP());
                AddNode(doc, eleBank, XMLDefs.OWNER_ID, bank.GetOwnerID());
                AddNode(doc, eleBank, XMLDefs.FLAGS, bank.GetFlags());
                AddNode(doc, eleBank, XMLDefs.STATE_TIME, bank.GetStateTimeRemaining());
                AddNode(doc, eleBank, XMLDefs.VISIBLE, bank.GetVisible());
                AddNode(doc, eleBank, XMLDefs.VISIBLE_TIME, bank.GetVisibleTimeRemaining());
                AddNode(doc, eleBank, XMLDefs.BUILT_BY_ID, bank.GetBuiltByID());
                AddResourceSystem(doc, eleBank, bank.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.WAREHOUSES);
            
            for(Warehouse warehouse : game.GetWarehouses())
            {
                Element eleWarehouse = AddNode(doc, elements, XMLDefs.WAREHOUSE, XMLDefs.ID, warehouse.GetID(), XMLDefs.NAME, warehouse.GetName());
                AddPositionNode(doc, eleWarehouse, XMLDefs.POSITION, warehouse.GetPosition());
                AddNode(doc, eleWarehouse, XMLDefs.HP, warehouse.GetHP());
                AddNode(doc, eleWarehouse, XMLDefs.MAX_HP, warehouse.GetMaxHP());
                AddNode(doc, eleWarehouse, XMLDefs.OWNER_ID, warehouse.GetOwnerID());
                AddNode(doc, eleWarehouse, XMLDefs.FLAGS, warehouse.GetFlags());
                AddNode(doc, eleWarehouse, XMLDefs.STATE_TIME, warehouse.GetStateTimeRemaining());
                AddNode(doc, eleWarehouse, XMLDefs.PRODUCING, warehouse.GetProducing());
                AddNode(doc, eleWarehouse, XMLDefs.PREP_TIME, warehouse.GetProdTimeRemaining());
                AddNode(doc, eleWarehouse, XMLDefs.VISIBLE, warehouse.GetVisible());
                AddNode(doc, eleWarehouse, XMLDefs.VISIBLE_TIME, warehouse.GetVisibleTimeRemaining());
                AddNode(doc, eleWarehouse, XMLDefs.BUILT_BY_ID, warehouse.GetBuiltByID());
                AddResourceSystem(doc, eleWarehouse, warehouse.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                AddCargoSystem(doc, eleWarehouse, warehouse.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.AIRBASES);
            
            for(Airbase airBase : game.GetAirbases())
            {
                Element eleAirbase = AddNode(doc, elements, XMLDefs.AIRBASE, XMLDefs.ID, airBase.GetID(), XMLDefs.NAME, airBase.GetName());
                AddPositionNode(doc, eleAirbase, XMLDefs.POSITION, airBase.GetPosition());
                AddNode(doc, eleAirbase, XMLDefs.HP, airBase.GetHP());
                AddNode(doc, eleAirbase, XMLDefs.MAX_HP, airBase.GetMaxHP());
                AddNode(doc, eleAirbase, XMLDefs.OWNER_ID, airBase.GetOwnerID());
                AddNode(doc, eleAirbase, XMLDefs.FLAGS, airBase.GetFlags());
                AddNode(doc, eleAirbase, XMLDefs.STATE_TIME, airBase.GetStateTimeRemaining());
                AddAircraftSystem(doc, eleAirbase, airBase.GetAircraftSystem(), XMLDefs.AIRCRAFT_SYSTEM);
                AddNode(doc, eleAirbase, XMLDefs.VISIBLE, airBase.GetVisible());
                AddNode(doc, eleAirbase, XMLDefs.VISIBLE_TIME, airBase.GetVisibleTimeRemaining());
                AddNode(doc, eleAirbase, XMLDefs.BUILT_BY_ID, airBase.GetBuiltByID());
                AddResourceSystem(doc, eleAirbase, airBase.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.ARMORIES);
            
            for(Armory armory : game.GetArmories())
            {
                Element eleArmory = AddNode(doc, elements, XMLDefs.ARMORY, XMLDefs.ID, armory.GetID(), XMLDefs.NAME, armory.GetName());
                AddPositionNode(doc, eleArmory, XMLDefs.POSITION, armory.GetPosition());
                AddNode(doc, eleArmory, XMLDefs.HP, armory.GetHP());
                AddNode(doc, eleArmory, XMLDefs.MAX_HP, armory.GetMaxHP());
                AddNode(doc, eleArmory, XMLDefs.OWNER_ID, armory.GetOwnerID());
                AddNode(doc, eleArmory, XMLDefs.FLAGS, armory.GetFlags());
                AddNode(doc, eleArmory, XMLDefs.STATE_TIME, armory.GetStateTimeRemaining());
                AddNode(doc, eleArmory, XMLDefs.PRODUCING, armory.GetProducing());
                AddNode(doc, eleArmory, XMLDefs.PREP_TIME, armory.GetProdTimeRemaining());
                AddNode(doc, eleArmory, XMLDefs.STRUCTURE_TANK_TYPE, armory.GetProducingType().ordinal());
                AddNode(doc, eleArmory, XMLDefs.VISIBLE, armory.GetVisible());
                AddNode(doc, eleArmory, XMLDefs.VISIBLE_TIME, armory.GetVisibleTimeRemaining());
                AddCargoSystem(doc, eleArmory, armory.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                AddNode(doc, eleArmory, XMLDefs.BUILT_BY_ID, armory.GetBuiltByID());
                AddNode(doc, eleArmory, XMLDefs.BARRACKS, armory.GetIsBarracks());
                AddResourceSystem(doc, eleArmory, armory.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.INFANTRIES);
            
            for(Infantry infantry : game.GetInfantries())
            {
                Element eleInfantry = AddNode(doc, elements, XMLDefs.INFANTRY, XMLDefs.ID, infantry.GetID(), XMLDefs.NAME, infantry.GetName());
                AddPositionNode(doc, eleInfantry, XMLDefs.POSITION, infantry.GetPosition());
                AddPositionNode(doc, eleInfantry, XMLDefs.TARGET, infantry.GetGeoTarget());
                AddNode(doc, eleInfantry, XMLDefs.HP, infantry.GetHP());
                AddNode(doc, eleInfantry, XMLDefs.MAX_HP, infantry.GetMaxHP());
                AddNode(doc, eleInfantry, XMLDefs.OWNER_ID, infantry.GetOwnerID());
                AddNode(doc, eleInfantry, XMLDefs.DETECTED, infantry.GetVisible());
                AddNode(doc, eleInfantry, XMLDefs.MOVE_ORDERS, (byte)infantry.GetMoveOrders().ordinal());
                AddNode(doc, eleInfantry, XMLDefs.LAST_BEARING, infantry.GetPosition().GetLastBearing());
                AddNode(doc, eleInfantry, XMLDefs.VISIBLE, infantry.GetVisible());
                AddNode(doc, eleInfantry, XMLDefs.VISIBLE_TIME, infantry.GetVisibleTimeRemaining());
                AddNode(doc, eleInfantry, XMLDefs.UNDER_ATTACK_TIME, infantry.GetUnderAttackTimeRemaining());
                AddNode(doc, eleInfantry, XMLDefs.CURRENT_FUEL, infantry.GetCurrentFuel());
                AddResourceSystem(doc, eleInfantry, infantry.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                
                if(infantry.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleInfantry, XMLDefs.COORDINATE_CHAIN, infantry.GetCoordinates());
                
                if(infantry.HasTarget())
                    AddEntityPointerNode(doc, eleInfantry, XMLDefs.TARGET_ENTITY, infantry.GetTarget());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.TANKS);
            
            for(Tank tank : game.GetTanks())
            {
                Element eleTank = AddNode(doc, elements, XMLDefs.TANK, XMLDefs.ID, tank.GetID(), XMLDefs.NAME, tank.GetName());
                AddPositionNode(doc, eleTank, XMLDefs.POSITION, tank.GetPosition());
                AddPositionNode(doc, eleTank, XMLDefs.TARGET, tank.GetGeoTarget());
                AddNode(doc, eleTank, XMLDefs.HP, tank.GetHP());
                AddNode(doc, eleTank, XMLDefs.MAX_HP, tank.GetMaxHP());
                AddNode(doc, eleTank, XMLDefs.OWNER_ID, tank.GetOwnerID());
                AddNode(doc, eleTank, XMLDefs.DETECTED, tank.GetVisible());
                AddNode(doc, eleTank, XMLDefs.MOVE_ORDERS, (byte)tank.GetMoveOrders().ordinal());
                AddNode(doc, eleTank, XMLDefs.RELOAD_REMAINING, tank.GetReloadTimeRemaining());                
                AddNode(doc, eleTank, XMLDefs.VISIBLE, tank.GetVisible());
                AddNode(doc, eleTank, XMLDefs.VISIBLE_TIME, tank.GetVisibleTimeRemaining());
                AddNode(doc, eleTank, XMLDefs.UNDER_ATTACK_TIME, tank.GetUnderAttackTimeRemaining());
                AddNode(doc, eleTank, XMLDefs.TANK_TYPE, tank.GetType().ordinal());
                AddNode(doc, eleTank, XMLDefs.MODE, tank.GetMode());
                AddNode(doc, eleTank, XMLDefs.CURRENT_FUEL, tank.GetCurrentFuel());
                AddResourceSystem(doc, eleTank, tank.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                
                if(tank.GetMissileSystem() != null)
                    AddMissileSystem(doc, eleTank, tank.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                
                if(tank.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleTank, XMLDefs.COORDINATE_CHAIN, tank.GetCoordinates());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.CARGO_TRUCKS);
            
            for(CargoTruck truck : game.GetCargoTrucks())
            {
                Element eleTruck = AddNode(doc, elements, XMLDefs.CARGO_TRUCK, XMLDefs.ID, truck.GetID(), XMLDefs.NAME, truck.GetName());
                AddPositionNode(doc, eleTruck, XMLDefs.POSITION, truck.GetPosition());
                AddPositionNode(doc, eleTruck, XMLDefs.TARGET, truck.GetGeoTarget());
                AddNode(doc, eleTruck, XMLDefs.HP, truck.GetHP());
                AddNode(doc, eleTruck, XMLDefs.MAX_HP, truck.GetMaxHP());
                AddNode(doc, eleTruck, XMLDefs.OWNER_ID, truck.GetOwnerID());
                AddNode(doc, eleTruck, XMLDefs.DETECTED, truck.GetVisible());
                AddNode(doc, eleTruck, XMLDefs.MOVE_ORDERS, (byte)truck.GetMoveOrders().ordinal());
                AddCargoSystem(doc, eleTruck, truck.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                AddNode(doc, eleTruck, XMLDefs.DELIVER_TYPE, truck.GetTypeToDeliver().ordinal());
                AddNode(doc, eleTruck, XMLDefs.CARGO_ID, truck.GetDeliverTypeID());
                AddNode(doc, eleTruck, XMLDefs.QUANTITY_TO_DELIVER, truck.GetQuantityToDeliver());
                AddNode(doc, eleTruck, XMLDefs.VISIBLE, truck.GetVisible());
                AddNode(doc, eleTruck, XMLDefs.VISIBLE_TIME, truck.GetVisibleTimeRemaining());
                AddNode(doc, eleTruck, XMLDefs.UNDER_ATTACK_TIME, truck.GetUnderAttackTimeRemaining());
                AddNode(doc, eleTruck, XMLDefs.CURRENT_FUEL, truck.GetCurrentFuel());
                AddResourceSystem(doc, eleTruck, truck.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                
                if(truck.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleTruck, XMLDefs.COORDINATE_CHAIN, truck.GetCoordinates());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SHIPS);
            
            for(Ship ship : game.GetShips())
            {
                Element eleShip = AddNode(doc, elements, XMLDefs.SHIP, XMLDefs.ID, ship.GetID(), XMLDefs.NAME, ship.GetName());
                AddPositionNode(doc, eleShip, XMLDefs.POSITION, ship.GetPosition());
                AddNode(doc, eleShip, XMLDefs.HP, ship.GetHP());
                AddNode(doc, eleShip, XMLDefs.MAX_HP, ship.GetMaxHP());
                AddNode(doc, eleShip, XMLDefs.OWNER_ID, ship.GetOwnerID());
                AddNode(doc, eleShip, XMLDefs.MOVE_ORDERS, (byte)ship.GetMoveOrders().ordinal());
                AddPositionNode(doc, eleShip, XMLDefs.TARGET, ship.GetGeoTarget());
                AddNode(doc, eleShip, XMLDefs.SONAR_COOLDOWN, ship.GetSonarCooldownRemaining());
                AddNode(doc, eleShip, XMLDefs.CURRENT_FUEL, ship.GetCurrentFuel());
                AddNode(doc, eleShip, XMLDefs.RADAR_ACTIVE, ship.GetRadarActive());
                AddNode(doc, eleShip, XMLDefs.VISIBLE, ship.GetVisible());
                AddNode(doc, eleShip, XMLDefs.VISIBLE_TIME, ship.GetVisibleTimeRemaining());
                AddNode(doc, eleShip, XMLDefs.MODE, ship.GetMode());
                AddNode(doc, eleShip, XMLDefs.SHIP_TYPE, ship.GetEntityType().ordinal());
                
                if(ship.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleShip, XMLDefs.COORDINATE_CHAIN, ship.GetCoordinates());
                if(ship.HasMissiles())
                    AddMissileSystem(doc, eleShip, ship.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                if(ship.HasTorpedoes())
                    AddMissileSystem(doc, eleShip, ship.GetTorpedoSystem(), XMLDefs.TORPEDO_SYSTEM);
                if(ship.HasInterceptors())
                    AddMissileSystem(doc, eleShip, ship.GetInterceptorSystem(), XMLDefs.INTERCEPTOR_SYSTEM);
                if(ship.HasCargo())
                    AddCargoSystem(doc, eleShip, ship.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                if(ship.HasAircraft())
                    AddAircraftSystem(doc, eleShip, ship.GetAircraftSystem(), XMLDefs.AIRCRAFT_SYSTEM);
                if(ship.HasArtillery())
                    AddMissileSystem(doc, eleShip, ship.GetArtillerySystem(), XMLDefs.ARTILLERY_SYSTEM);
                if(ship.HasSentries())
                    AddSentrySystem(doc, eleShip, ship.GetSentryGuns(), XMLDefs.SENTRY_SYSTEM);
                if(ship.HasTarget())
                    AddEntityPointerNode(doc, eleShip, XMLDefs.TARGET_ENTITY, ship.GetTarget());
                
                if(ship.HasFireOrder())
                {
                    AddPositionNode(doc, eleShip, XMLDefs.ARTILLERY_TARGET, ship.GetFireOrder().GetGeoTarget());
                    AddNode(doc, eleShip, XMLDefs.RADIUS, ship.GetFireOrder().GetRadius());
                }
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SUBMARINES);
            
            for(Submarine submarine : game.GetSubmarines())
            {
                Element eleSubmarine = AddNode(doc, elements, XMLDefs.SUBMARINE, XMLDefs.ID, submarine.GetID(), XMLDefs.NAME, submarine.GetName());
                AddPositionNode(doc, eleSubmarine, XMLDefs.POSITION, submarine.GetPosition());
                AddNode(doc, eleSubmarine, XMLDefs.HP, submarine.GetHP());
                AddNode(doc, eleSubmarine, XMLDefs.MAX_HP, submarine.GetMaxHP());
                AddNode(doc, eleSubmarine, XMLDefs.OWNER_ID, submarine.GetOwnerID());
                AddNode(doc, eleSubmarine, XMLDefs.MOVE_ORDERS, (byte)submarine.GetMoveOrders().ordinal());
                AddPositionNode(doc, eleSubmarine, XMLDefs.TARGET, submarine.GetGeoTarget());
                AddNode(doc, eleSubmarine, XMLDefs.SONAR_COOLDOWN, submarine.GetSonarCooldownRemaining());
                AddNode(doc, eleSubmarine, XMLDefs.CURRENT_FUEL, submarine.GetCurrentFuel());
                AddNode(doc, eleSubmarine, XMLDefs.SUBMERGED, submarine.Submerged());
                AddNode(doc, eleSubmarine, XMLDefs.DIVING, submarine.Diving());
                AddNode(doc, eleSubmarine, XMLDefs.SUBMERGE_TIME, submarine.GetSubmergeTimeRemaining());
                AddNode(doc, eleSubmarine, XMLDefs.VISIBLE, submarine.GetVisible());
                AddNode(doc, eleSubmarine, XMLDefs.VISIBLE_TIME, submarine.GetVisibleTimeRemaining());
                AddNode(doc, eleSubmarine, XMLDefs.SUBMARINE_TYPE, submarine.GetEntityType().ordinal());
                
                if(submarine.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleSubmarine, XMLDefs.COORDINATE_CHAIN, submarine.GetCoordinates());
                if(submarine.HasMissiles())
                    AddMissileSystem(doc, eleSubmarine, submarine.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                if(submarine.HasTorpedoes())
                    AddMissileSystem(doc, eleSubmarine, submarine.GetTorpedoSystem(), XMLDefs.TORPEDO_SYSTEM);
                if(submarine.HasICBMs())
                    AddMissileSystem(doc, eleSubmarine, submarine.GetICBMSystem(), XMLDefs.ICBM_SYSTEM);
                if(submarine.HasTarget())
                    AddEntityPointerNode(doc, eleSubmarine, XMLDefs.TARGET_ENTITY, submarine.GetTarget());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.AIRCRAFTS);
            
            for(Airplane aircraft : game.GetAirplanes())
            {
                Element eleAircraft = AddNode(doc, elements, XMLDefs.AIRCRAFT, XMLDefs.ID, aircraft.GetID());
                AddPositionNode(doc, eleAircraft, XMLDefs.POSITION, aircraft.GetPosition());
                AddPositionNode(doc, eleAircraft, XMLDefs.TARGET, aircraft.GetGeoTarget());
                AddNode(doc, eleAircraft, XMLDefs.HP, aircraft.GetHP());
                AddNode(doc, eleAircraft, XMLDefs.MAX_HP, aircraft.GetMaxHP());
                AddNode(doc, eleAircraft, XMLDefs.OWNER_ID, aircraft.GetOwnerID());
                AddNode(doc, eleAircraft, XMLDefs.MOVE_ORDERS, aircraft.GetMoveOrders().ordinal());
                AddEntityPointerNode(doc, eleAircraft, XMLDefs.HOME_BASE, aircraft.GetHomeBase());
                AddNode(doc, eleAircraft, XMLDefs.NAME, aircraft.GetName());
                AddNode(doc, eleAircraft, XMLDefs.MAX_FUEL, aircraft.GetMaxFuel());
                AddNode(doc, eleAircraft, XMLDefs.CURRENT_FUEL, aircraft.GetCurrentFuel());
                AddNode(doc, eleAircraft, XMLDefs.AUTO_RETURN, aircraft.WillAutoReturn());
                AddNode(doc, eleAircraft, XMLDefs.STEALTH, aircraft.GetStealth());
                AddNode(doc, eleAircraft, XMLDefs.HAS_SCANNER, aircraft.HasScanner());
                AddNode(doc, eleAircraft, XMLDefs.HAS_CANNON, aircraft.HasCannon());
                AddNode(doc, eleAircraft, XMLDefs.FUEL_TRANSFER, aircraft.CanTransferFuel());
                AddNode(doc, eleAircraft, XMLDefs.GROUND_ATTACK, aircraft.GroundAttack());
                AddNode(doc, eleAircraft, XMLDefs.ELECTRONIC_WARFARE, aircraft.GetElectronicWarfare());
                AddNode(doc, eleAircraft, XMLDefs.AIRCRAFT_ROLE, aircraft.GetAircraftType().ordinal());
                AddNode(doc, eleAircraft, XMLDefs.RADAR_ACTIVE, aircraft.GetRadarActive());
                AddNode(doc, eleAircraft, XMLDefs.RELOAD_REMAINING, aircraft.GetCannonReloadRemaining());
                AddNode(doc, eleAircraft, XMLDefs.ELECTRONIC_WARFARE_RELOAD, aircraft.GetElecWarfareReload());
                AddNode(doc, eleAircraft, XMLDefs.VISIBLE, aircraft.GetVisible());
                AddNode(doc, eleAircraft, XMLDefs.VISIBLE_TIME, aircraft.GetVisibleTimeRemaining());
                AddNode(doc, eleAircraft, XMLDefs.TIME_AIRBORNE, aircraft.GetTimeAirborne());
                AddNode(doc, eleAircraft, XMLDefs.MODE, aircraft.GetMode());
                
                if(aircraft.HasGeoCoordChain())
                    AddGeoCoordChainNode(doc, eleAircraft, XMLDefs.COORDINATE_CHAIN, aircraft.GetCoordinates());
                if(aircraft.HasMissiles())
                    AddMissileSystem(doc, eleAircraft, aircraft.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
                if(aircraft.HasInterceptors())
                    AddMissileSystem(doc, eleAircraft, aircraft.GetInterceptorSystem(), XMLDefs.INTERCEPTOR_SYSTEM);
                if(aircraft.HasCargo())
                    AddCargoSystem(doc, eleAircraft, aircraft.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                if(aircraft.HasTarget())
                    AddEntityPointerNode(doc, eleAircraft, XMLDefs.TARGET_ENTITY, aircraft.GetTarget());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.BLUEPRINTS);
            
            for(Blueprint blueprint : game.GetBlueprints())
            {
                Element eleBlueprint = AddNode(doc, elements, XMLDefs.BLUEPRINT, XMLDefs.ID, blueprint.GetID());
                AddPositionNode(doc, eleBlueprint, XMLDefs.POSITION, blueprint.GetPosition());
                AddNode(doc, eleBlueprint, XMLDefs.EXPIRY, blueprint.GetExpiryRemaining());
                AddNode(doc, eleBlueprint, XMLDefs.CREATED_BY_ID, blueprint.GetOwnerID());
                AddNode(doc, eleBlueprint, XMLDefs.TYPE, blueprint.GetType().ordinal());
                AddNode(doc, eleBlueprint, XMLDefs.RESOURCE_TYPE, blueprint.GetResourceType().ordinal());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.LOOTS);
            
            for(Loot loot : game.GetLoots())
            {
                Element eleLoot = AddNode(doc, elements, XMLDefs.LOOT, XMLDefs.ID, loot.GetID());
                AddPositionNode(doc, eleLoot, XMLDefs.POSITION, loot.GetPosition());
                AddNode(doc, eleLoot, XMLDefs.LOOT_TYPE, loot.GetLootType().ordinal());
                AddNode(doc, eleLoot, XMLDefs.TYPE, loot.GetCargoID());
                AddNode(doc, eleLoot, XMLDefs.QUANTITY, loot.GetQuantity());
                AddNode(doc, eleLoot, XMLDefs.EXPIRY, loot.GetExpiryRemaining());
            }
            
            
            elements = AddNode(doc, eleGame, XMLDefs.RUBBLES);
            
            for(Rubble rubble : game.GetRubbles())
            {
                Element eleRubble = AddNode(doc, elements, XMLDefs.RUBBLE, XMLDefs.ID, rubble.GetID());
                AddPositionNode(doc, eleRubble, XMLDefs.POSITION, rubble.GetPosition());
                AddNode(doc, eleRubble, XMLDefs.STRUCTURE_TYPE, rubble.GetStructureType().ordinal());
                AddNode(doc, eleRubble, XMLDefs.RESOURCE_TYPE, rubble.GetResourceType().ordinal());
                AddNode(doc, eleRubble, XMLDefs.OWNER_ID, rubble.GetOwnerID());
                AddNode(doc, eleRubble, XMLDefs.EXPIRY, rubble.GetExpiryRemaining());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.AIRDROPS);
            
            for(Airdrop airdrop : game.GetAirdrops())
            {
                Element eleAirdrop = AddNode(doc, elements, XMLDefs.AIRDROP, XMLDefs.ID, airdrop.GetID());
                AddNode(doc, eleAirdrop, XMLDefs.OWNER_ID, airdrop.GetOwnerID());
                AddPositionNode(doc, eleAirdrop, XMLDefs.POSITION, airdrop.GetPosition());
                AddNode(doc, eleAirdrop, XMLDefs.EXPIRY, airdrop.GetArrivalRemaining());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.RESOURCE_DEPOSITS);
            
            for(ResourceDeposit deposit : game.GetResourceDeposits())
            {
                Element eleDeposit = AddNode(doc, elements, XMLDefs.RESOURCE_DEPOSIT, XMLDefs.ID, deposit.GetID());
                AddPositionNode(doc, eleDeposit, XMLDefs.POSITION, deposit.GetPosition());
                AddNode(doc, eleDeposit, XMLDefs.TYPE, deposit.GetType().ordinal());
                AddNode(doc, eleDeposit, XMLDefs.RESOURCES, deposit.GetReserves());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.RADIATIONS);
            
            for(Radiation radiation : game.GetRadiations())
            {
                Element eleRadiation = AddNode(doc, elements, XMLDefs.RADIATION, XMLDefs.ID, radiation.GetID());
                AddPositionNode(doc, eleRadiation, XMLDefs.POSITION, radiation.GetPosition());
                AddNode(doc, eleRadiation, XMLDefs.RADIUS, radiation.GetRadius());
                AddNode(doc, eleRadiation, XMLDefs.EXPIRY, radiation.GetExpiryRemaining());
                AddNode(doc, eleRadiation, XMLDefs.OWNER_ID, radiation.GetCreatedByID());
            }
            
            elements = AddNode(doc, eleGame, XMLDefs.SHIPYARDS);
            
            for(Shipyard shipyard : game.GetShipyards())
            {
                Element eleShipyard = AddNode(doc, elements, XMLDefs.SHIPYARD, XMLDefs.ID, shipyard.GetID());
                AddNode(doc, eleShipyard, XMLDefs.NAME, shipyard.GetName());
                AddNode(doc, eleShipyard, XMLDefs.PORT, shipyard.GetPortOnly());
                AddPositionNode(doc, eleShipyard, XMLDefs.POSITION, shipyard.GetPosition());
                AddPositionNode(doc, eleShipyard, XMLDefs.OUTPUT_COORD, shipyard.GetOutputCoord());
                AddNode(doc, eleShipyard, XMLDefs.HP, shipyard.GetHP());
                AddNode(doc, eleShipyard, XMLDefs.OWNER_ID, shipyard.GetOwnerID());
                AddNode(doc, eleShipyard, XMLDefs.MAX_HP, shipyard.GetMaxHP());
                AddNode(doc, eleShipyard, XMLDefs.CONTESTED, shipyard.GetContested());
                AddCargoSystem(doc, eleShipyard, shipyard.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                AddNode(doc, eleShipyard, XMLDefs.PRODUCTION_CAPACITY, shipyard.GetProductionCapacity());
                AddShipyardQueue(doc, eleShipyard, shipyard, XMLDefs.QUEUE);
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(strGameFile)));
        }
        catch(ParserConfigurationException ex)
        {
            listener.SaveError(String.format("XML parser configuration error when saving game from %s.", strGameFile));
            throw new RuntimeException(String.format("Transformer error when saving game from %s.", strGameFile));
        }
        catch(TransformerException ex)
        {
            listener.SaveError(String.format("Transformer error when saving game from %s.", strGameFile));
            throw new RuntimeException(String.format("Transformer error when saving game from %s.", strGameFile));
        }
    }
    
    private static Element AddMissileSystem(Document doc, Element parent, MissileSystem missileSystem, String strTagName)
    {
        Element eleMissileSystem = doc.createElement(strTagName);
        AddNode(doc, eleMissileSystem, XMLDefs.RELOAD_REMAINING, missileSystem.GetReloadTimeRemaining());
        AddNode(doc, eleMissileSystem, XMLDefs.RELOAD_TIME, missileSystem.GetReloadTime());
        AddNode(doc, eleMissileSystem, XMLDefs.SLOT_COUNT, missileSystem.GetSlotCount());
        
        Element eleSlots = AddNode(doc, eleMissileSystem, XMLDefs.SLOTS);
        
        for(int l = 0; l < missileSystem.GetSlotCount(); l++)
        {
            if(missileSystem.GetSlotHasMissile(l))
            {
                AddNode(doc, eleSlots, XMLDefs.SLOT, XMLDefs.NUMBER, l, XMLDefs.TYPE, missileSystem.GetSlotMissileType(l), XMLDefs.PREP_TIME, missileSystem.GetSlotPrepTimeRemaining(l));
            }
        }
        
        parent.appendChild(eleMissileSystem);
        return eleMissileSystem;
    }
    
    private static Element AddSentrySystem(Document doc, Element parent, List<ShortDelay> Sentries, String strTagName)
    {
        Element eleSentrySystem = doc.createElement(strTagName);
        
        Element eleSentryGuns = AddNode(doc, eleSentrySystem, XMLDefs.CIWS_GUNS);
        
        for(int l = 0; l < Sentries.size(); l++)
        {
            AddNode(doc, eleSentryGuns, XMLDefs.CIWS_GUN, XMLDefs.RELOAD_REMAINING, Sentries.get(l).GetRemaining());
        }
        
        parent.appendChild(eleSentrySystem);
        return eleSentrySystem;
    }
    
    private static Element AddAircraftSystem(Document doc, Element parent, AircraftSystem aircraftSystem, String strTagName)
    {
        Element eleAircraftSystem = doc.createElement(strTagName);
        AddNode(doc, eleAircraftSystem, XMLDefs.OPEN, aircraftSystem.GetOpen());
        AddNode(doc, eleAircraftSystem, XMLDefs.RELOAD_REMAINING, aircraftSystem.GetReloadTimeRemaining());
        AddNode(doc, eleAircraftSystem, XMLDefs.SLOT_COUNT, aircraftSystem.GetSlotCount());
        
        Element eleStoredAircrafts = AddNode(doc, eleAircraftSystem, XMLDefs.STORED_AIRCRAFTS);
        
        for(StoredAirplane aircraft : aircraftSystem.GetStoredAirplanes().values())
        {
            Element eleAircraft = AddNode(doc, eleStoredAircrafts, XMLDefs.STORED_AIRCRAFT, XMLDefs.ID, aircraft.GetID());
            AddNode(doc, eleAircraft, XMLDefs.HP, aircraft.GetHP());
            AddNode(doc, eleAircraft, XMLDefs.MAX_HP, aircraft.GetMaxHP());
            AddNode(doc, eleAircraft, XMLDefs.OWNER_ID, aircraft.GetOwnerID());
            AddNode(doc, eleAircraft, XMLDefs.NAME, aircraft.GetName());
            AddNode(doc, eleAircraft, XMLDefs.CURRENT_FUEL, aircraft.GetCurrentFuel());
            AddEntityPointerNode(doc, eleAircraft, XMLDefs.HOME_BASE, aircraft.GetHomeBase());
            AddNode(doc, eleAircraft, XMLDefs.AUTO_RETURN, aircraft.WillAutoReturn());
            AddNode(doc, eleAircraft, XMLDefs.AIRCRAFT_ROLE, aircraft.GetAircraftType().ordinal());
            AddNode(doc, eleAircraft, XMLDefs.PREP_TIME, aircraft.GetPrepRemaining());
            AddNode(doc, eleAircraft, XMLDefs.ELECTRONIC_WARFARE_RELOAD, aircraft.GetElecWarfareReload());
            AddNode(doc, eleAircraft, XMLDefs.MODE, aircraft.GetMode());

            if(aircraft.HasMissiles())
                AddMissileSystem(doc, eleAircraft, aircraft.GetMissileSystem(), XMLDefs.MISSILE_SYSTEM);
            if(aircraft.HasInterceptors())
                AddMissileSystem(doc, eleAircraft, aircraft.GetInterceptorSystem(), XMLDefs.INTERCEPTOR_SYSTEM);
            if(aircraft.HasCargo())
                AddCargoSystem(doc, eleAircraft, aircraft.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
        }
        
        parent.appendChild(eleAircraftSystem);
        return eleAircraftSystem;
    }
    
    private static Element AddCargoSystem(Document doc, Element parent, CargoSystem cargoSystem, String strTagName)
    {
        Element eleCargoSystem = doc.createElement(strTagName);
        AddNode(doc, eleCargoSystem, XMLDefs.CAPACITY, cargoSystem.GetCapacity());
        
        AddResourceMap(doc, eleCargoSystem, cargoSystem.GetResourceMap(), XMLDefs.RESOURCES);
        
        Element eleStoredInfantries = AddNode(doc, eleCargoSystem, XMLDefs.STORED_INFANTRIES);
        
        for(StoredInfantry infantry : cargoSystem.GetInfantries())
        {
            Element eleStoredInfantry = AddNode(doc, eleStoredInfantries, XMLDefs.STORED_INFANTRY, XMLDefs.ID, infantry.GetID());
                AddNode(doc, eleStoredInfantry, XMLDefs.HP, infantry.GetHP());
                AddNode(doc, eleStoredInfantry, XMLDefs.MAX_HP, infantry.GetMaxHP());                    
                AddNode(doc, eleStoredInfantry, XMLDefs.OWNER_ID, infantry.GetOwnerID());
                AddNode(doc, eleStoredInfantry, XMLDefs.NAME, infantry.GetName());
                AddNode(doc, eleStoredInfantry, XMLDefs.PREP_TIME, infantry.GetPrepRemaining());
                AddNode(doc, eleStoredInfantry, XMLDefs.CURRENT_FUEL, infantry.GetCurrentFuel());
                AddResourceSystem(doc, eleStoredInfantry, infantry.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
        }
        
        Element eleStoredTanks = AddNode(doc, eleCargoSystem, XMLDefs.STORED_TANKS);
        
        for(StoredTank tank : cargoSystem.GetTanks())
        {
            Element eleStoredTank = AddNode(doc, eleStoredTanks, XMLDefs.STORED_TANK, XMLDefs.ID, tank.GetID());
                AddNode(doc, eleStoredTank, XMLDefs.HP, tank.GetHP());
                AddNode(doc, eleStoredTank, XMLDefs.MAX_HP, tank.GetMaxHP());      
                AddNode(doc, eleStoredTank, XMLDefs.NAME, tank.GetName());
                AddNode(doc, eleStoredTank, XMLDefs.OWNER_ID, tank.GetOwnerID());
                AddNode(doc, eleStoredTank, XMLDefs.PREP_TIME, tank.GetPrepRemaining());
                AddNode(doc, eleStoredTank, XMLDefs.IS_MISSILES, tank.IsAnMBT());
                AddNode(doc, eleStoredTank, XMLDefs.TANK_TYPE, tank.GetType().ordinal());
                AddNode(doc, eleStoredTank, XMLDefs.MODE, tank.GetMode());
                AddNode(doc, eleStoredTank, XMLDefs.CURRENT_FUEL, tank.GetCurrentFuel());
                AddResourceSystem(doc, eleStoredTank, tank.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
                
                if(tank.GetMissileSystem() != null)
                    AddMissileSystem(doc, eleStoredTank, tank.GetMissileSystem(), XMLDefs.TANK_MISSILE_SYSTEM);
        }
        
        Element eleStoredCargoTrucks = AddNode(doc, eleCargoSystem, XMLDefs.STORED_CARGO_TRUCKS);
        
        for(StoredCargoTruck truck : cargoSystem.GetCargoTrucks())
        {
            Element eleStoredCargoTruck = AddNode(doc, eleStoredCargoTrucks, XMLDefs.STORED_CARGO_TRUCK, XMLDefs.ID, truck.GetID());
                AddNode(doc, eleStoredCargoTruck, XMLDefs.HP, truck.GetHP());
                AddNode(doc, eleStoredCargoTruck, XMLDefs.MAX_HP, truck.GetMaxHP());  
                AddNode(doc, eleStoredCargoTruck, XMLDefs.NAME, truck.GetName());
                AddNode(doc, eleStoredCargoTruck, XMLDefs.OWNER_ID, truck.GetOwnerID());
                AddNode(doc, eleStoredCargoTruck, XMLDefs.PREP_TIME, truck.GetPrepRemaining());
                AddCargoSystem(doc, eleStoredCargoTruck, truck.GetCargoSystem(), XMLDefs.CARGO_SYSTEM);
                AddNode(doc, eleStoredCargoTruck, XMLDefs.CURRENT_FUEL, truck.GetCurrentFuel());
                AddResourceSystem(doc, eleStoredCargoTruck, truck.GetResourceSystem(), XMLDefs.RESOURCE_CONTAINER);
        }
        
        parent.appendChild(eleCargoSystem);
        return eleCargoSystem;
    }
    
    private static Element AddResourceSystem(Document doc, Element parent, ResourceSystem system, String strTagName)
    {
        Element eleCargoSystem = doc.createElement(strTagName);
        AddNode(doc, eleCargoSystem, XMLDefs.CAPACITY, system.GetCapacity());
        
        AddResourceMap(doc, eleCargoSystem, system.GetTypes(), XMLDefs.RESOURCES);
        
        parent.appendChild(eleCargoSystem);
        return eleCargoSystem;
    }
    
    private static Element AddResourceMap(Document doc, Element parent, Map<ResourceType, Long> resources, String strTagName)
    {
        Element eleResources = doc.createElement(strTagName);
        
        for(Entry<ResourceType, Long> resource : resources.entrySet())
        {
            AddNode(doc, eleResources, XMLDefs.RESOURCE, XMLDefs.TYPE, resource.getKey().name(), XMLDefs.QUANTITY, resource.getValue());
        }
        
        parent.appendChild(eleResources);
        return eleResources;
    }
    
    private static Element AddShipyardQueue(Document doc, Element parent, Shipyard shipyard, String strTagName)
    {
        Element eleQueue = doc.createElement(strTagName);
        
        Element eleProductionOrders = AddNode(doc, eleQueue, XMLDefs.PRODUCTION_ORDERS);
        
        for(ShipProductionOrder order : shipyard.GetQueue())
        {
            Element eleProductionOrder = AddNode(doc, eleProductionOrders, XMLDefs.PRODUCTION_ORDER);
                AddNode(doc, eleProductionOrder, XMLDefs.PRODUCING_FOR_ID, order.GetProducingForID());
                AddNode(doc, eleProductionOrder, XMLDefs.PREP_TIME, order.GetConstructionTimeRemaining()); 
                AddNode(doc, eleProductionOrder, XMLDefs.SHIP_TYPE, order.GetTypeUnderConstruction().ordinal());
        }
        
        parent.appendChild(eleQueue);
        return eleQueue;
    }
    
    private static Element AddNode(Document doc, Element parent, String strTagName)
    {
        Element element = doc.createElement(strTagName);
        parent.appendChild(element);
        return element;
    }
    
    private static Element AddNode(Document doc, Element parent, String strTagName, String strAtt1Name, Object objAtt1Value, String strAtt2Name, Object objAtt2Value)
    {
        Element element = doc.createElement(strTagName);
        element.setAttribute(strAtt1Name, objAtt1Value.toString());
        element.setAttribute(strAtt2Name, objAtt2Value.toString());
        parent.appendChild(element);
        return element;
    }
    
    private static Element AddNode(Document doc, Element parent, String strTagName, String strAtt1Name, Object objAtt1Value, String strAtt2Name, Object objAtt2Value, String strAtt3Name, Object objAtt3Value)
    {
        Element element = doc.createElement(strTagName);
        element.setAttribute(strAtt1Name, objAtt1Value.toString());
        element.setAttribute(strAtt2Name, objAtt2Value.toString());
        element.setAttribute(strAtt3Name, objAtt3Value.toString());
        parent.appendChild(element);
        return element;
    }
    
    private static Element AddNode(Document doc, Element parent, String strTagName, String strAttName, Object objValue)
    {
        Element element = doc.createElement(strTagName);
        element.setAttribute(strAttName, objValue.toString());
        parent.appendChild(element);
        return element;
    }
    
    private static Element AddNode(Document doc, Element parent, String strTagName, Object objValue)
    {
        Element element = doc.createElement(strTagName);

        String value;

        if(objValue instanceof byte[]) 
        {
            value = Base64.getEncoder().encodeToString((byte[]) objValue);
        } 
        else 
        {
            value = objValue.toString();
        }

        element.appendChild(doc.createTextNode(value));
        parent.appendChild(element);
        
        return element;
    }
    
    private static Element AddPositionNode(Document doc, Element eleParent, String strTagName, GeoCoord geoPosition)
    {
        Element elePosition = AddNode(doc, eleParent, strTagName);
        if(geoPosition == null)
        {
            AddNode(doc, elePosition, XMLDefs.LATITUDE, Float.toString(0));
            AddNode(doc, elePosition, XMLDefs.LONGITUDE, Float.toString(0));
        }
        else
        {
            AddNode(doc, elePosition, XMLDefs.LATITUDE, Float.toString(geoPosition.GetLatitude()));
            AddNode(doc, elePosition, XMLDefs.LONGITUDE, Float.toString(geoPosition.GetLongitude()));
        }
        return elePosition;
    }
    
    private static Element AddEntityPointerNode(Document doc, Element eleParent, String strTagName, EntityPointer entity)
    {
        Element elePointer = AddNode(doc, eleParent, strTagName);
        
        AddNode(doc, elePointer, XMLDefs.TYPE, entity.GetType().ordinal());
        AddNode(doc, elePointer, XMLDefs.ID, entity.GetID());
        
        return elePointer;
    }
    
    private static Element AddGeoCoordChainNode(Document doc, Element parent, String strTagName, Map<Integer, GeoCoord> Coordinates)
    {
        Element eleCoordMap = doc.createElement(strTagName);
        
        Element eleCoordinates = AddNode(doc, eleCoordMap, XMLDefs.COORDINATES);
        
        for(Entry<Integer, GeoCoord> coordinate : Coordinates.entrySet())
        {
            int lOrder = coordinate.getKey();
            GeoCoord coord = coordinate.getValue();
            AddNode(doc, eleCoordinates, XMLDefs.COORDINATE, XMLDefs.ORDER, lOrder, XMLDefs.LATITUDE, coord.GetLatitude(), XMLDefs.LONGITUDE, coord.GetLongitude());
        }
        
        parent.appendChild(eleCoordMap);
        return eleCoordMap;
    }
}
