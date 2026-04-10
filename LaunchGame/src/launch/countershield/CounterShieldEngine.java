package launch.countershield;

import launch.countershield.model.*;
import launch.countershield.detectors.*;
import launch.countershield.net.NetClassifier;
import launch.game.GeoCoord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import launch.countershield.detectors.SpeedDetector;

public final class CounterShieldEngine
{
    public interface ActionSink
    {
        void onAction(long nowMs, int playerId, PlayerRiskProfile.Action action, double score, String lastDetail);
    }

    private final Map<Integer, PlayerRiskProfile> profiles = new ConcurrentHashMap<>();
    private final SpeedDetector speed;
    private final TeleportDetector teleport;
    private final CommandRateDetector cmdRate;
    private final BannedAppDetector bannedApp;
    private final DeviceMovementOnHomeIPDetector homeMove;
    private final RiskLog riskLog;
    private final ActionSink sink;

    public CounterShieldEngine(String logPath, ActionSink sink)
    {
        this.speed = new SpeedDetector(60.0, 35.0);
        this.teleport = new TeleportDetector(1500.0, 3.0);
        this.cmdRate = new CommandRateDetector(12, 10_000);
        this.bannedApp = new BannedAppDetector();
        this.homeMove = new DeviceMovementOnHomeIPDetector(new NetClassifier(), 800.0, 180.0);
        this.riskLog = new RiskLog(logPath);
        this.sink = sink;
    }

    private PlayerRiskProfile prof(int pid)
    {
        return profiles.computeIfAbsent(pid, k -> new PlayerRiskProfile());
    }

    public void onLocationUpdate(long nowMs, int pid, GeoCoord prev, GeoCoord curr, double seconds, Provider provider, String ip)
    {
        List<ScoreEvent> events = new ArrayList<>();
        events.add(speed.check(nowMs, pid, prev, curr, seconds, provider));
        events.add(teleport.check(nowMs, pid, prev, curr, seconds));
        events.add(homeMove.check(nowMs, pid, curr, ip));
        commit(nowMs, pid, events);
    }

    public void onCommand(long nowMs, int pid, String cmd)
    {
        commit(nowMs, pid, Collections.singletonList(cmdRate.check(nowMs, pid, cmd)));
    }

    public void onBannedApp(long nowMs, int pid, String sig, boolean matched)
    {
        commit(nowMs, pid, Collections.singletonList(bannedApp.check(nowMs, pid, sig, matched)));
    }

    private void commit(long nowMs, int pid, Collection<ScoreEvent> events)
    {
        PlayerRiskProfile p = prof(pid);
        ScoreEvent lastEvt = null;
        for(ScoreEvent e : events)
        {
            if(e != null)
            {
                p.add(e);
                riskLog.append(e, p.score(nowMs));
                lastEvt = e;
            }
        }
        double score = p.score(nowMs);
        PlayerRiskProfile.Action action = p.actionFor(score);
        if(action != PlayerRiskProfile.Action.NONE && lastEvt != null)
        {
            sink.onAction(nowMs, pid, action, score, lastEvt.detail);
        }
    }
}
