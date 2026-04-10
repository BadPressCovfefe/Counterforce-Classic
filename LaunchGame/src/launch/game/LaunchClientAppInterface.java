/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.game;

import launch.comm.clienttasks.Task.TaskMessage;
import launch.game.entities.Interceptor;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.entities.Torpedo;
import launch.game.treaties.Treaty;
import launch.game.types.LaunchType;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchReport;

public interface LaunchClientAppInterface
{
    void GameTicked(int lMS);
    
    void SaveConfig(Config config);
    void SaveAvatar(int lAvatarID, byte[] cData);
    void AvatarUploaded(int lAvatarID);
    void SaveImage(int lImageID, byte[] cData);
    
    boolean PlayerLocationAvailable();
    LaunchClientLocation GetPlayerLocation();
    
    void EntityUpdated(LaunchEntity entity);
    void EntityRemoved(LaunchEntity entity);
    void TreatyUpdated(Treaty treaty);
    void ShowBasicOKDialog(final String strMessage);
    
    void MajorChanges();
    void Authenticated();
    void AccountUnregistered();
    void AccountUnmigrated();
    void FailedSpoofCheck();
    void AccountNameTaken();
    void DeviceIDUsed();
    void AccountMigrated(boolean bDisplaySuccess);
    void MajorVersionMismatch();
    void MinorVersionMismatch();
    
    void ActionSucceeded();
    void ActionFailed();
    
    void ShowTaskMessage(TaskMessage message);
    void DismissTaskMessage();
    
    void NewEvent(LaunchEvent event);
    void NewReport(LaunchReport report);
    
    void Quit();
    
    void AllianceCreated();
    
    String GetProcessNames();
    
    boolean GetConnectionMobile();
    
    void DeviceChecksRequested();
    
    void DisplayGeneralError();
    
    void TempBanned(String strReason, long oDuration);
    void PermBanned(String strReason);
    
    void ReceiveUser(User user);
    
    void UpdateFirebaseToken();
    
    void MissileExploded(Missile missile);
    void InterceptorReachedTarget(Interceptor interceptor);
    void TorpedoExploded(Torpedo torpedo);
    
    void TypeAdded(LaunchType type);
}
