package countershield.detectors;

import countershield.model.ScoreEvent;
import countershield.model.ScoreEvent.Severity;
import launch.utilities.LaunchLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EmulatorTopDetector
 * -------------------
 * Detects suspicious device hash changes within a 10-minute monitoring window.
 * Flags when a player's reported device hash changes drastically (e.g. vendor swap).
 */
public final class EmulatorTopDetector {

    private static final long MONITOR_WINDOW_MS = 10 * 60 * 1000;

    private static final class Track {
        String hash;
        String vendor;
        long startMs;
    }

    private final Map<Integer, Track> tracks = new ConcurrentHashMap<>();

    public EmulatorTopDetector() {
        LaunchLog.ConsoleMessage("[Detector] EmulatorTopDetector active (10-min watch window)");
    }

    /**
     * Called whenever a device hash is reported for a player.
     *
     * @param nowMs current timestamp
     * @param pid player ID
     * @param hash current device hash string
     * @return ScoreEvent if a suspicious change detected, otherwise null
     */
    public ScoreEvent check(long nowMs, int pid, String hash) {
        if (hash == null || hash.isEmpty()) return null;

        Track t = tracks.get(pid);

        if (t == null) {
            t = new Track();
            t.hash = hash;
            t.vendor = extractVendor(hash);
            t.startMs = nowMs;
            tracks.put(pid, t);
            return null;
        }

        if (nowMs - t.startMs > MONITOR_WINDOW_MS) {
            tracks.remove(pid);
            return null;
        }

        String newVendor = extractVendor(hash);

        // If vendor changes drastically, flag it
        if (!t.vendor.equalsIgnoreCase(newVendor)) {
            double score = 65.0; 
            Severity sev = Severity.HIGH;

            // Heuristic escalation
            if (isDrasticVendorChange(t.vendor, newVendor)) {
                score = 90.0;
                sev = Severity.CRITICAL;
            }

            String detail = String.format(
                "Device vendor change detected: %s → %s within 10min. Possible emulator/spoof.",
                t.vendor, newVendor);

            t.hash = hash;
            t.vendor = newVendor;
            t.startMs = nowMs;

            LaunchLog.ConsoleMessage("[Detector] " + detail);

            return new ScoreEvent(nowMs, pid, "EmulatorTopDetector", score, sev, detail);
        }

        return null;
    }

    private String extractVendor(String hash) {
        // crude heuristic: extract alphabetic prefix or vendor tokens
        String lower = hash.toLowerCase();
        if (lower.contains("samsung")) return "Samsung";
        if (lower.contains("xiaomi") || lower.contains("mi")) return "Xiaomi";
        if (lower.contains("huawei") || lower.contains("honor")) return "Huawei";
        if (lower.contains("oneplus")) return "OnePlus";
        if (lower.contains("oppo")) return "Oppo";
        if (lower.contains("vivo")) return "Vivo";
        if (lower.contains("google")) return "Google";
        if (lower.contains("emulator") || lower.contains("bluestacks") || lower.contains("memu")) return "Emulator";
        return "Unknown";
    }

    private boolean isDrasticVendorChange(String oldVendor, String newVendor) {
        if (oldVendor.equals("Emulator") || newVendor.equals("Emulator")) return true;
        if (oldVendor.equals("Unknown") || newVendor.equals("Unknown")) return false;
        return !oldVendor.equalsIgnoreCase(newVendor);
    }

    public void cleanup(long nowMs) {
        tracks.entrySet().removeIf(e -> nowMs - e.getValue().startMs > MONITOR_WINDOW_MS);
    }
}
