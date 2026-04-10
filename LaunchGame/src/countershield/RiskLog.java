package countershield;

import countershield.model.ScoreEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import launch.utilities.LaunchLog;
import launch.game.Defs;

public final class RiskLog {
    private final String path ;

    public RiskLog(String path) { this.path = path; }

    public synchronized void append(ScoreEvent e, double totalScore) {
        if (e == null) return;
        try (FileWriter fw = new FileWriter("logs/COUNTERSHIELD/%s/%s.log", true)) {
            String line = String.format(
                "{\"ts\":\"%s\",\"player\":%d,\"detector\":\"%s\",\"score\":%.2f,\"total\":%.2f," +
                "\"severity\":\"%s\",\"detail\":\"%s\"}\n",
                Instant.ofEpochMilli(e.ts), e.playerId, e.detector, e.score, totalScore,
                e.severity, e.detail.replace("\"","'")
            );
            fw.write(line);
        } catch (IOException ex) {
        }
    }
}
