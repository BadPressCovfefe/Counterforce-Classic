package countershield;

import countershield.model.*;
import countershield.detectors.*;
import countershield.net.NetClassifier;
import countershield.net.ExternalRiskServices;
import countershield.net.ExternalRiskServices.RiskResult;
import launch.game.GeoCoord;
import launch.utilities.LaunchLog;

import java.util.*;
import java.util.concurrent.*;

/**
 * CounterShieldEngine
 * -------------------
 * 
 * Includes:
 * - Speed, teleport, and command rate detectors
 * - Banned app and home IP movement detector
 * - Duplication exploit detector
 * - Emulator detector (hash/vendor swap detection)
 * - External risk integrations (IP + fingerprint)
 */
public final class CounterShieldEngine {

    public interface ActionSink {
        void onAction(long nowMs, int playerId, PlayerRiskProfile.Action action,
                      double score, String lastDetail);
    }

    // Internal state
    private final Map<Integer, PlayerRiskProfile> profiles = new ConcurrentHashMap<>();
    private final Map<Integer, GeoCoord> lastCoord = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastFixMs = new ConcurrentHashMap<>();

    // Detectors
    private final SpeedDetector speed;
    private final TeleportDetector teleport;
    private final CommandRateDetector cmdRate;
    private final BannedAppDetector bannedApp;
    private final DeviceMovementOnHomeIPDetector homeMove;
    private final DuplicationExploitDetector duplication;
    private final EmulatorTopDetector emulatorTop;

    private final RiskLog riskLog;
    private final ActionSink sink;

    // Async thread pool for external risk services
    private final ExecutorService asyncPool =
            Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "CounterShield-Async");
                t.setDaemon(true);
                return t;
            });

    // Constructor
    public CounterShieldEngine(String logPath, ActionSink sink) {
        LaunchLog.ConsoleMessage("Starting COUNTERSHIELD engine...");

        this.speed = new SpeedDetector(60.0, 35.0);
        this.teleport = new TeleportDetector(1500.0, 3.0);
        this.cmdRate = new CommandRateDetector(12, 10_000);
        this.bannedApp = new BannedAppDetector();
        this.homeMove = new DeviceMovementOnHomeIPDetector(new NetClassifier(), 800.0, 180.0);
        this.duplication = new DuplicationExploitDetector(this);
        this.emulatorTop = new EmulatorTopDetector();

        this.riskLog = new RiskLog(logPath);
        this.sink = sink;

        LaunchLog.ConsoleMessage("COUNTERSHIELD Started.");
    }

    private PlayerRiskProfile prof(int pid) {
        return profiles.computeIfAbsent(pid, k -> new PlayerRiskProfile());
    }

    // Movement and Command Checks
    public void onLocationUpdate(long nowMs, int pid,
                                 GeoCoord prevIgnored, GeoCoord curr,
                                 double secondsIgnored,
                                 Provider provider,
                                 String ip) {

        GeoCoord prev = lastCoord.get(pid);
        long last = lastFixMs.getOrDefault(pid, nowMs);
        double elapsedSeconds = Math.max((nowMs - last) / 1000.0, 0.0);

        if (prev == null) {
            lastCoord.put(pid, curr);
            lastFixMs.put(pid, nowMs);
            //LaunchLog.ConsoleMessage(String.format("[Engine] FIRST_FIX pid=%d coord=%s", pid, curr));
            return;
        }

        List<ScoreEvent> events = new ArrayList<>(4);
        events.add(convert(speed.check(nowMs, pid, prev, curr, elapsedSeconds, provider)));
        events.add(convert(teleport.check(nowMs, pid, prev, curr, elapsedSeconds)));
        events.add(convert(homeMove.check(nowMs, pid, curr, ip)));

        lastCoord.put(pid, curr);
        lastFixMs.put(pid, nowMs);

        commit(nowMs, pid, events);
    }

    public void onCommand(long nowMs, int pid, String cmd) {
        commit(nowMs, pid, Collections.singletonList(convert(cmdRate.check(nowMs, pid, cmd))));
    }

    public void onBannedApp(long nowMs, int pid, String sig, boolean matched) {
        commit(nowMs, pid, Collections.singletonList(convert(bannedApp.check(nowMs, pid, sig, matched))));
    }

    // Device Hash / Emulator Checks
    public void onDeviceHashUpdate(long nowMs, int pid, String deviceHash) {
        if (deviceHash == null || deviceHash.isEmpty()) return;
        ScoreEvent evt = emulatorTop.check(nowMs, pid, deviceHash);
        if (evt != null) commit(nowMs, pid, Collections.singletonList(evt));

        // perform cleanup pass
        emulatorTop.cleanup(nowMs);
    }

    // Duplication Checks
    public void onResourceAction(long nowMs, int pid, String action, String type, double amount, double networth) {
        duplication.onResourceAction(nowMs, pid, action, type, amount, networth);
    }

    public void resetDuplicationDaily() {
        duplication.resetDaily();
    }

    // External Risk Checks
    public void checkIpAsync(int pid, String ip) {
        asyncPool.submit(() -> {
            try {
                RiskResult ipRisk = ExternalRiskServices.checkIP(ip);
                if (ipRisk.score >= 0) {
                    /*LaunchLog.ConsoleMessage(String.format(
                        "[ExternalRisk] PID=%d IP=%s Score=%d Reasons=%s",
                        pid, ip, ipRisk.score, ipRisk.reasons));*/
                    prof(pid).addExternalRisk(pid, "ip", ipRisk.score, ipRisk.reasons);
                    evaluateExternalAction(pid, "IP");
                } else {
                    //LaunchLog.ConsoleMessage(String.format("[ExternalRisk] PID=%d IP OK (score=%d)", pid, ipRisk.score));
                }
            } catch (Exception e) {
                //LaunchLog.ConsoleMessage("[ExternalRisk] IP check failed: " + e.getMessage());
            }
        });
    }

    public void checkFingerprintAsync(int pid, String fingerprint) {
        asyncPool.submit(() -> {
            try {
                RiskResult devRisk = ExternalRiskServices.checkFingerprint(fingerprint);
                if (devRisk.score >= 0) {
                    /*LaunchLog.ConsoleMessage(String.format(
                        "[ExternalRisk] PID=%d DeviceScore=%d Reasons=%s",
                        pid, devRisk.score, devRisk.reasons));*/
                    prof(pid).addExternalRisk(pid, "device", devRisk.score, devRisk.reasons);

                    evaluateExternalAction(pid, "Fingerprint");
                } else {
                    //LaunchLog.ConsoleMessage(String.format("[ExternalRisk] PID=%d Device OK (score=%d)", pid, devRisk.score));
                }
            } catch (Exception e) {
                //LaunchLog.ConsoleMessage("[ExternalRisk] Device check failed: " + e.getMessage());
            }
        });
    }

    private void evaluateExternalAction(int pid, String source) {
        long nowMs = System.currentTimeMillis();
        double totalScore = prof(pid).score(nowMs);
        PlayerRiskProfile.Action act = prof(pid).actionFor(totalScore);
        if (act != PlayerRiskProfile.Action.NONE) {
            sink.onAction(nowMs, pid, act, totalScore, "External:" + source);
        }
    }

    private void commit(long nowMs, int pid, Collection<ScoreEvent> events) {
        PlayerRiskProfile p = prof(pid);
        ScoreEvent lastEvt = null;

        for (ScoreEvent e : events) {
            if (e != null) {
                p.add(e);
                lastEvt = e;
            }
        }

        double liveScore = p.score(nowMs);

        for (ScoreEvent e : events) {
            if (e != null) riskLog.append(e, liveScore);
        }

        //LaunchLog.ConsoleMessage(String.format("[AntiCheat] PID=%d Score=%.2f", pid, liveScore));

        PlayerRiskProfile.Action action = p.actionFor(liveScore);
        if (action != PlayerRiskProfile.Action.NONE && lastEvt != null) {
            sink.onAction(nowMs, pid, action, liveScore, lastEvt.detail);
        }
    }

    private ScoreEvent convert(ScoreEvent e) {
        if (e == null) return null;
        return new ScoreEvent(e.ts, e.playerId, e.detector, e.score, e.severity, e.detail);
    }

    public void shutdown() {
        asyncPool.shutdown();
        duplication.shutdown();
    }
}
