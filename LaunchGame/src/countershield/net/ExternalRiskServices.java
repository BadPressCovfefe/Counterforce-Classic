package countershield.net;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * Handles communication with external anti-cheat microservices (IP / Device).
 * 
 * Ports:
 *  - 4047 IP Risk Lookup
 *  - 4049 Fingerprint Checker
 * 
 * Requires header:
 *   Authorization: Bearer CounterforceAdmin8
 */
public final class ExternalRiskServices {

    private static final String AUTH_TOKEN = "Bearer CounterforceAdmin8";
    private static final String IP_CHECK_URL = "http://launch.notazipbomb.zip:4047/lookup";
    private static final String FINGERPRINT_CHECK_URL = "http://launch.notazipbomb.zip:4049/fingerprint-check";

    // Public check methods

    public static RiskResult checkIP(String ip) {
        try {
            JSONObject body = new JSONObject().put("ip", ip);
            JSONObject json = postJson(IP_CHECK_URL, body);
            return parseResult(json);
        } catch (Exception e) {
            return new RiskResult(-1, "error", "ip_check_failed: " + e.getMessage());
        }
    }

    public static RiskResult checkFingerprint(String fingerprint) {
        try {
            JSONObject body = new JSONObject().put("fingerprint", fingerprint);
            JSONObject json = postJson(FINGERPRINT_CHECK_URL, body);
            return parseResult(json);
        } catch (Exception e) {
            return new RiskResult(-1, "error", "fingerprint_check_failed: " + e.getMessage());
        }
    }

    // HTTP JSON Post helper

    private static JSONObject postJson(String url, JSONObject payload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", AUTH_TOKEN);
        conn.setDoOutput(true);
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(100000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
            os.flush();
        }

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
        }

        if (code != 200) {
            throw new IOException("Server returned HTTP response code: " + code + " body=" + sb);
        }

        return new JSONObject(sb.toString());
    }

    // Result parser

    private static RiskResult parseResult(JSONObject json) {
        int score = json.optInt("risk_score", 0);
        String conf = json.optString("confidence", "unknown");
        String reasons = "";

        if (json.has("reasons")) {
            if (json.get("reasons") instanceof org.json.JSONArray) {
                reasons = json.getJSONArray("reasons").toString();
            } else {
                reasons = json.optString("reasons");
            }
        }

        return new RiskResult(score, conf, reasons);
    }

    // Result container

    public static class RiskResult {
        public final int score;
        public final String confidence;
        public final String reasons;

        public RiskResult(int score, String confidence, String reasons) {
            this.score = score;
            this.confidence = confidence;
            this.reasons = reasons;
        }
    }
}
