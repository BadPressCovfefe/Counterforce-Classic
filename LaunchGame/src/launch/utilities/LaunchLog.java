/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;



/**
 *
 * @author tobster
 */
public class LaunchLog
{
    private static final String LOG_FORMAT = "logs/%s/%s/%s.log";
    private static final String CONSOLE_MESSAGE_FORMAT = "%s: (%s) %s";
    private static final String FILE_MESSAGE_FORMAT = "%s - %s";
    private static final String FORMAT_TIME_LOG_NAME = "%s (%s)";
    
    // CF Bot Endpoint Config
    private static final String BOT_LOG_URL =
    System.getenv().getOrDefault("BOT_LOG_URL", "http://launch.notazipbomb.zip:4036/log").trim();
    private static final String LOG_SHARED_SECRET =
    System.getenv().getOrDefault("LOG_SHARED_SECRET", "CounterforceAdmin").trim();

    
    private static final DateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat dateFormatTime = new SimpleDateFormat("HHmmss");
    
    public enum LogType
    {
        SESSION,
        COMMS,
        APPLICATION,
        GAME,
        TASKS,
        SERVICES,
        LOCATIONS,
        POISON,
        PERFORMANCE,
        CHEATING,
        NOTIFICATIONS,
        DEVICE_CHECKS,
        SAM_SITE_AI,
        DEBUG
    }
    
    private final static String[] LogFolders = new String[]
    {
        "sessions",
        "comms",
        "application",
        "game",
        "tasks",
        "services",
        "locations",
        "poison",
        "performance",
        "cheating",
        "notifications",
        "device_checks",
        "sam_site_ai",
        "debug"
    };
    
    private final static boolean[] EnabledFileLogs = new boolean[LogType.values().length];
    private final static boolean[] EnabledConsoleLogs = new boolean[LogType.values().length];
    
    public static void SetFileLoggingEnabled(LogType type, boolean bEnabled)
    {
        EnabledFileLogs[type.ordinal()] = bEnabled;
    }
    
    public static void SetConsoleLoggingEnabled(LogType type, boolean bEnabled)
    {
        EnabledConsoleLogs[type.ordinal()] = bEnabled;
    }
    
    public static void SetLoggingEnabled(LogType type, boolean bConsoleLogging, boolean bFileLogging)
    {
        EnabledFileLogs[type.ordinal()] = bFileLogging;
        EnabledConsoleLogs[type.ordinal()] = bConsoleLogging;
    }
    
    public synchronized static void Log(LogType type, String strLogName, String strMessage)
    {
        Date now = Calendar.getInstance().getTime();
        String strTime = dateFormatTime.format(now);

        if (EnabledFileLogs[type.ordinal()]) 
        {
            /*File file = new File(String.format(LOG_FORMAT, LogFolders[type.ordinal()], dateFormatDay.format(now), strLogName));
            file.getParentFile().mkdirs();
            
            try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) 
            {
                printWriter.println(String.format(FILE_MESSAGE_FORMAT, strTime, strMessage));
            } 
            catch (Exception ex) 
            {
                System.out.println("LOG ERROR!: " + ex.getMessage());
            }*/
        }

        if (EnabledConsoleLogs[type.ordinal()]) 
        {
            System.out.println(String.format(CONSOLE_MESSAGE_FORMAT, strTime, strLogName, strMessage));
        }

        try 
        {
            if(strMessage != null) 
            {
                String lower = strMessage.toLowerCase();
                
                boolean looksBad =
                    lower.contains("exception") ||
                    lower.contains("nullpointer") ||
                    lower.contains("stacktrace") ||
                    lower.contains("error") ||
                    lower.contains("fatal");

                if(looksBad) 
                {
                    postToBot(type.name(), strMessage, null, strLogName);
                }
            }
        } 
        catch (Exception ignore) 
        {
            System.err.println("Failed to mirror log to bot: " + ignore.getMessage());
        }
    }


    
    public static String GetTimeFormattedLogName(String strLogName)
    {
        return String.format(FORMAT_TIME_LOG_NAME, strLogName, dateFormatTime.format(Calendar.getInstance().getTime()));
    }
    
    public static void ConsoleMessage(String strMessage)
    {
        System.out.println(strMessage);
    }
    
    private static void postToBot(String level, String message, String stack, String context) {
        if (BOT_LOG_URL.isEmpty()) return;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"level\":").append(jsonQuote(level != null ? level : "INFO")).append(',')
              .append("\"message\":").append(jsonQuote(message != null ? message : "")).append(',')
              .append("\"stack\":").append(jsonQuote(stack != null ? stack : "")).append(',')
              .append("\"context\":").append(jsonQuote(context != null ? context : "")).append("}");
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(BOT_LOG_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if (!LOG_SHARED_SECRET.isEmpty()) {
                conn.setRequestProperty("X-Log-Secret", LOG_SHARED_SECRET);
            }
            try (OutputStream os = conn.getOutputStream()) { os.write(body); }
            conn.getResponseCode();
            try { conn.getInputStream().close(); } catch (Exception ignore) {}
            conn.disconnect();
        } catch (Exception ignored) { /* never throw from logger */ }
    }
    
    private static String jsonQuote(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
    
    public static void ReportException(Throwable t, String context) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            if (context != null && !context.isBlank()) pw.println(context);
            t.printStackTrace(pw);
            pw.flush();
            String stack = sw.toString();
            System.err.println(stack);
            postToBot("EXCEPTION", t.toString(), stack, context);
        } catch (Exception ignored) {}
    }


}
