package launchserver.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import launch.game.LaunchServerGame;
import launch.game.entities.Player;
import launch.game.systems.CargoSystem;
import launch.game.entities.conceptuals.Resource.ResourceType;
import tobcomm.api.GameBridge;
import launchserver.api.Json;

/**
 *
 * GET /health -> "OK" GET /api/status -> JSON snapshot GET
 * /api/object?obj=&inst=&off= -> raw bytes (octet-stream) GET
 * /api/command?cmd=&inst= -> send command (no body) GET
 * /api/request?obj=&inst=&start=&len= -> request object
 *
 * I simplified endpoints here for basic API calls to the discord bot. AKA
 * Status channel/Trace reports
 */
//NOTE: I do utilize .env calls here, but I did not actually make one but will at some point as its more secure. All env calls run defaults.
public final class ApiServer
{

    private final GameBridge bridge;
    private final HttpServer server;

    private final String apiSecret;
    private final boolean protectHealth;

    public ApiServer(GameBridge bridge, int httpPort) throws IOException
    {
        this.bridge = bridge;
        this.server = HttpServer.create(new InetSocketAddress(httpPort), 0);

        this.apiSecret = Optional.ofNullable(System.getenv("API_SECRET")).orElse("CounterforceAdmin").trim();
        this.protectHealth = Boolean.parseBoolean(
                Optional.ofNullable(System.getenv("PROTECT_HEALTH")).orElse("true")
        ); // Just an extra layer boolean to protect the API

        // This is where you create API context calls, will look like; https://ip@port/api/path
        server.createContext("/health", protectHealth ? withAuth(this::health) : this::health);
        server.createContext("/api/status", withAuth(this::status));
        //The twp anbove here are reserved, DO NOT MODIFY THEM

        // Create any further context down below; wrap 'withAuth()' if you want a secured API header to be required to run this command.
        server.createContext("/api/idof", withAuth(this::idof));
        server.createContext("/api/give_membership", withAuth(this::giveMembership));
        server.createContext("/api/remove_membership", withAuth(this::removeMembership));
        server.createContext("/api/quit", withAuth(this::quitServer));
        server.createContext("/api/approve", withAuth(this::approvePlayer));
        server.createContext("/api/reqchecks", withAuth(this::requireChecks));
        server.createContext("/api/award", withAuth(this::award));
        server.createContext("/api/stimulus", withAuth(this::stimulus));
        server.createContext("/api/sendalert", withAuth(this::sendAlert));
        server.createContext("/api/cleanavatars", withAuth(this::cleanAvatars));
        server.createContext("/api/purgeavatars", withAuth(this::purgeAvatars));
        server.createContext("/api/cleanupdead", withAuth(this::cleanupDead));
        server.createContext("/api/compensateplayers", withAuth(this::compensatePlayers));
        server.createContext("/api/park", withAuth(this::parkPlayer));
        server.createContext("/api/compinv", withAuth(this::compassionateInvulnerability));
        server.createContext("/api/rename", withAuth(this::renamePlayer));
        server.createContext("/api/transfer", withAuth(this::transferAccount));
        server.createContext("/api/unbossify", withAuth(this::unbossify));
        server.createContext("/api/unadminify", withAuth(this::unadminify));
        server.createContext("/api/bossify", withAuth(this::bossify));
        server.createContext("/api/report", withAuth(this::report));
        server.createContext("/api/diag", withAuth(this::diag));
        server.createContext("/api/adminify", withAuth(this::adminify));
        server.createContext("/api/performance", withAuth(this::performance));
        server.createContext("/api/event", withAuth(this::event));
        server.createContext("/api/clean", withAuth(this::clean));
        server.createContext("/api/allianceclean", withAuth(this::allianceClean));
        server.createContext("/api/permban", withAuth(this::permBan));
        server.createContext("/api/unban", withAuth(this::unban));
        server.createContext("/api/tempban", withAuth(this::tempBan));
        server.createContext("/api/endhour", withAuth(this::endHour));
        server.createContext("/api/endday", withAuth(this::endDay));
        server.createContext("/api/endweek", withAuth(this::endWeek));
        server.createContext("/api/save", withAuth(this::saveGame));
        server.createContext("/api/ffwd", withAuth(this::fastForward));
        server.createContext("/api/endminute", withAuth(this::endMinute));
        server.createContext("/api/info", withAuth(this::info));

        server.setExecutor(null);
    }

    public void start()
    {
        server.start();
        System.out.println("[API] Intalized API on :" + server.getAddress().getPort());
    }

    public void stop(int delaySeconds)
    {
        server.stop(delaySeconds);
    }

    // The basic API calls, mostly utilized for discord bot monitoring, no need to use these for the game itself.
    private void health(HttpExchange ex) throws IOException
    {
        writeText(ex, 200, "OK");
    }

    private void status(HttpExchange ex) throws IOException
    {
        setJsonCORS(ex.getResponseHeaders());
        byte[] body = Json.serialize(bridge.getStatus()).getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(OutputStream os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void objectGet(HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        int obj = parseInt(q.get("obj"), -1);
        int inst = parseInt(q.getOrDefault("inst", "0"), 0);
        int off = parseInt(q.getOrDefault("off", "0"), 0);
        if(obj < 0)
        {
            writeText(ex, 400, "Missing obj");
            return;
        }

        byte[] data = bridge.getObject(obj, inst, off);
        if(data == null)
        {
            writeText(ex, 404, "Not ready");
            return;
        }

        Headers h = ex.getResponseHeaders();
        h.add("Content-Type", "application/octet-stream");
        h.add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, data.length);
        try(OutputStream os = ex.getResponseBody())
        {
            os.write(data);
        }
    }

    private void command(HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        int cmd = parseInt(q.get("cmd"), -1);
        int inst = parseInt(q.getOrDefault("inst", "0"), 0);
        if(cmd < 0)
        {
            writeText(ex, 400, "Missing cmd");
            return;
        }

        bridge.sendCommand(cmd, inst);
        writeJson(ex, 200, Map.of("ok", true));
    }

    private void request(HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        int obj = parseInt(q.get("obj"), -1);
        int inst = parseInt(q.getOrDefault("inst", "0"), 0);
        int start = parseInt(q.getOrDefault("start", "0"), 0);
        int len = parseInt(q.getOrDefault("len", "0"), 0);
        if(obj < 0)
        {
            writeText(ex, 400, "Missing obj");
            return;
        }

        bridge.requestObject(obj, inst, start, len);
        writeJson(ex, 200, Map.of("queued", true));
    }

    // Start of the logic for all API context created calls. Pretty much mirrors LaunchConsole.java commands, but uses them in an exchangem then pushed 'out'
    private void idof(HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String match = q.get("match");
        if(match == null || match.isBlank())
        {
            writeText(ex, 400, "Missing 'match'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("---=== Players matching %s ===---", match)).append("\n");
        String needle = match.toLowerCase();

        for(launch.game.entities.Player p : game.GetPlayers())
        {
            String name = p.GetName();
            if(name != null && name.toLowerCase().contains(needle))
            {
                sb.append(String.format("%d - %s.", p.GetID(), name)).append("\n");
            }
        }

        var body = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }

    }

    private void giveMembership(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        LaunchServerGame game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            Player player = game.GetPlayer(id);
            if(player != null)
            {
                player.SetMemberStatus(true);
                out = String.format("Made %s a member.", player.GetName());
            }
            else
            {
                out = String.format("Player %d not found.", id);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void removeMembership(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        LaunchServerGame game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            Player player = game.GetPlayer(id);
            if(player != null)
            {
                player.SetMemberStatus(false);
                out = String.format("Removed %s's membership.", player.GetName());
            }
            else
            {
                out = String.format("Player %d not found.", id);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void quitServer(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        new Thread(() ->
        {
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException ignored)
            {
            }
            try
            {
                game.ShutDown();
            }
            catch(Throwable ignored)
            {
            }
        }, "api-quit").start();

        writeText(ex, 200, "OK: shutting down game");
    }

    private void approvePlayer(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        LaunchServerGame game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            if(game.Approve(id, "[deep admin]"))
            {
                out = String.format("Approved player %d account.", id);
            }
            else
            {
                out = String.format("Couldn't approve player %d account.", id);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void requireChecks(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        LaunchServerGame game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            if(game.RequireNewChecks(id, "[deep admin]"))
            {
                out = String.format("Set checks required for player %d account.", id);
            }
            else
            {
                out = String.format("Couldn't set checks required for player %d account.", id);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void award(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        String amtStr = q.get("amount");
        String reasonStr = q.get("reason");

        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }
        if(amtStr == null || amtStr.isBlank())
        {
            writeText(ex, 400, "Missing 'amount'");
            return;
        }
        if(reasonStr == null)
        {
            reasonStr = "";
        } // allow empty reason

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            int amt = Integer.parseInt(amtStr);
            game.Award(id, amt, reasonStr);
            out = String.format("Awarded %d to player %d. Reason: %s", amt, id, reasonStr.isBlank() ? "(none)" : reasonStr);
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id/amount.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void stimulus(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String amtStr = q.get("amount");
        if(amtStr == null || amtStr.isBlank())
        {
            writeText(ex, 400, "Missing 'amount'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int amt = Integer.parseInt(amtStr);
            game.Stimulus(amt);
            out = String.format("Stimulus executed with amount %d.", amt);
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid amount.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void sendAlert(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            game.SendAlert(id);
            out = String.format("Alert sent to player %d.", id);
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void cleanAvatars(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.CleanAvatars();
        String out = "Cleaning up avatars... done.";
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void purgeAvatars(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.PurgeAvatars();
        String out = "Purging avatars... done.";
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void cleanupDead(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        final long A_DAY = 24L * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        int deleted = 0;

        for(launch.game.entities.Player player : game.GetPlayers())
        {
            boolean notFunctioning = !player.Functioning();
            boolean unseen4Days = (player.GetLastSeen() + (4 * A_DAY)) < now;
            boolean joinedWithin7d = (player.GetJoinTime() + (7 * A_DAY)) >= now;
            if(notFunctioning && unseen4Days && joinedWithin7d)
            {
                game.DeletePlayer(player.GetID(), true);
                deleted++;
            }
        }

        String out = "Cleanup dead accounts complete. Deleted: " + deleted;
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void compensatePlayers(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        int samDelta = 75000;
        int msDelta = 75000;
        int icbmDelta = 50000;
        int abmDelta = 50000;
        int sentryDelta = 5000;
        int radarDelta = 25000;
        int laserDelta = 50000;

        int samCount = 0, msCount = 0, icbmCount = 0, abmCount = 0, sentryCount = 0, radarCount = 0, laserCount = 0;

        for(launch.game.entities.SAMSite site : game.GetSAMSites())
        {
            launch.game.entities.Player owner = game.GetOwner(site);
            if(owner != null)
            {
                owner.AddWealth(samDelta);
                samCount++;
            }
        }
        for(launch.game.entities.MissileSite site : game.GetMissileSites())
        {
            launch.game.entities.Player owner = game.GetOwner(site);
            if(owner != null)
            {
                owner.AddWealth(msDelta);
                msCount++;
            }
        }
        for(launch.game.entities.MissileSite silo : game.GetICBMSilos())
        {
            launch.game.entities.Player owner = game.GetOwner(silo);
            if(owner != null)
            {
                owner.AddWealth(icbmDelta);
                icbmCount++;
            }
        }
        for(launch.game.entities.SAMSite silo : game.GetABMSites())
        {
            launch.game.entities.Player owner = game.GetOwner(silo);
            if(owner != null)
            {
                owner.AddWealth(abmDelta);
                abmCount++;
            }
        }
        for(launch.game.entities.SentryGun gun : game.GetSentryGuns())
        {
            launch.game.entities.Player owner = game.GetOwner(gun);
            if(owner != null)
            {
                owner.AddWealth(sentryDelta);
                sentryCount++;
            }
        }
        for(launch.game.entities.RadarStation radar : game.GetRadarStations())
        {
            launch.game.entities.Player owner = game.GetOwner(radar);
            if(owner != null)
            {
                owner.AddWealth(radarDelta);
                radarCount++;
            }
        }

        String out = String.format(
                "Compensation complete.\nSAMSites:%d (+%d) MissileSites:%d (+%d) ICBMSilos:%d (+%d)\n"
                + "ABMSites:%d (+%d) SentryGuns:%d (+%d) RadarStations:%d (+%d) LaserDefenses:%d (+%d)",
                samCount, samDelta, msCount, msDelta, icbmCount, icbmDelta,
                abmCount, abmDelta, sentryCount, sentryDelta, radarCount, radarDelta, laserCount, laserDelta
        );

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void parkPlayer(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            game.ParkPlayer(id);
            out = String.format("Parked player %d for 90 days.", id);
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void compassionateInvulnerability(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        String hoursStr = q.get("hours");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }
        if(hoursStr == null || hoursStr.isBlank())
        {
            writeText(ex, 400, "Missing 'hours'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int id = Integer.parseInt(idStr);
            int hours = Integer.parseInt(hoursStr);
            int ms = hours * launch.game.Defs.MS_PER_HOUR;
            game.SetCompassionateInvulnerability(id, ms);
            out = String.format("Gave player %d %d hrs of compassionate invulnerability.", id, hours);
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id/hours.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void renamePlayer(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        String newName = q.get("name");

        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }
        if(newName == null || newName.isBlank())
        {
            writeText(ex, 400, "Missing 'name'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int playerId = Integer.parseInt(idStr);
            if(game.ChangePlayerName(playerId, newName))
            {
                out = String.format("Renamed %d to %s.", playerId, newName);
            }
            else
            {
                out = String.format("Could not rename %d to %s.", playerId, newName);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid id.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void transferAccount(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String fromStr = q.get("from");
        String toStr = q.get("to");

        if(fromStr == null || fromStr.isBlank())
        {
            writeText(ex, 400, "Missing 'from'");
            return;
        }
        if(toStr == null || toStr.isBlank())
        {
            writeText(ex, 400, "Missing 'to'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        String out;
        try
        {
            int fromId = Integer.parseInt(fromStr);
            int toId = Integer.parseInt(toStr);
            if(game.TransferAccount(fromId, toId))
            {
                out = String.format("Transferred %d to %d.", fromId, toId);
            }
            else
            {
                out = String.format("Could not transfer %d to %d.", fromId, toId);
            }
        }
        catch(NumberFormatException nfe)
        {
            out = "Invalid from/to ids.";
        }

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = out.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void unbossify(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            Player player = game.GetPlayer(id);
            if(player != null)
            {
                player.SetBoss(false);
                String msg = String.format("Unmade %s a boss.", player.GetName());
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 404, "Player not found.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void unadminify(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            Player player = game.GetPlayer(id);
            if(player != null)
            {
                player.SetIsAnAdmin(false);
                String msg = String.format("Unmade %s an administrator.", player.GetName());
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 404, "Player not found.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void bossify(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            Player player = game.GetPlayer(id);
            if(player != null)
            {
                player.SetBoss(true);
                String msg = String.format("Made %s a boss.", player.GetName());
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 404, "Player not found.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void report(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String msg = q.get("message");
        if(msg == null || msg.isBlank())
        {
            writeText(ex, 400, "Missing 'message'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.CreateReport(new launch.utilities.LaunchReport(msg, true));

        writeText(ex, 200, "Report created: \"" + msg + "\"");
    }

    private void diag(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        var comms = game.GetServerComms();

        StringBuilder sb = new StringBuilder();
        sb.append("---=== Diagnostic Report ===---\n\n");
        sb.append("Ticks\n");
        sb.append(String.format("Game tick starts: %d\n", game.GetGameTickStarts()));
        sb.append(String.format("Game tick ends:   %d\n", game.GetGameTickEnds()));
        sb.append(String.format("Comms tick starts:%d\n", game.GetCommTickStarts()));
        sb.append(String.format("Comms tick ends:  %d\n", game.GetCommTickEnds()));
        sb.append("\nServer Comms\n");
        sb.append(String.format("Active sessions:  %d\n", comms.GetActiveSessions()));
        sb.append(String.format("Total opened:     %d\n", comms.GetTotalSessionsOpened()));
        sb.append(String.format("Total closed:     %d\n", comms.GetTotalSessionsClosed()));
        sb.append(String.format("Most ever:        %d\n", comms.GetMostActiveSessions()));
        sb.append("\n-------------------------------");

        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private void performance(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        launch.utilities.LaunchLog.ConsoleMessage("---=== Performance ===---");
        launch.utilities.LaunchPerf.PrintLatestSamples();
        launch.utilities.LaunchLog.ConsoleMessage("-------------------------");

        writeText(ex, 200, "Not Available, I need to modify LaunchPerf and add helper to display samples to be appended as text for API.");
    }

    private void adminify(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            launch.game.entities.Player player = game.GetPlayer(id);
            if(player == null)
            {
                writeText(ex, 404, "Player not found.");
                return;
            }

            player.SetIsAnAdmin(true);
            String msg = String.format("Made %s an administrator.", player.GetName());
            writeText(ex, 200, msg);
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void event(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String msg = q.get("message");
        if(msg == null || msg.isBlank())
        {
            writeText(ex, 400, "Missing 'message'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.CreateEvent(new launch.utilities.LaunchEvent(
                msg,
                launch.utilities.LaunchEvent.SoundEffect.RESPAWN
        ));

        writeText(ex, 200, "Event created: \"" + msg + "\" (sound: RESPAWN)");
    }

    private void clean(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.CleanUpUnownedEntities();
        writeText(ex, 200, "Unowned entity cleanup triggered.");
    }

    private void allianceClean(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.ForceAllianceDisbandChecks();
        writeText(ex, 200, "Alliance disband checks forced.");
    }

    private void permBan(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        String reason = q.get("reason");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }
        if(reason == null)
        {
            reason = "";
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            boolean ok = game.PermaBan(id, reason, "[DEEP ADMIN]");
            if(ok)
            {
                launch.game.entities.Player p = game.GetPlayer(id);
                String name = (p != null ? p.GetName() : ("#" + id));
                String msg = String.format("Perm banned %s. Reason: %s", name, reason.isBlank() ? "(none)" : reason);
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 400, "Could not ban player.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void unban(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            boolean ok = game.ConsoleUnban(id);
            if(ok)
            {
                launch.game.entities.Player p = game.GetPlayer(id);
                String name = (p != null ? p.GetName() : ("#" + id));
                String msg = String.format("Unbanned %s.", name);
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 400, "Could not unban player.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void tempBan(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String idStr = q.get("id");
        String reason = q.get("reason");
        if(idStr == null || idStr.isBlank())
        {
            writeText(ex, 400, "Missing 'id'");
            return;
        }
        if(reason == null)
        {
            reason = "";
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int id = Integer.parseInt(idStr);
            boolean ok = game.TempBan(id, reason, "[DEEP ADMIN]");
            if(ok)
            {
                launch.game.entities.Player p = game.GetPlayer(id);
                String name = (p != null ? p.GetName() : ("#" + id));
                String msg = String.format("Temp banned %s. Reason: %s", name, reason.isBlank() ? "(none)" : reason);
                writeText(ex, 200, msg);
            }
            else
            {
                writeText(ex, 400, "Could not ban player.");
            }
        }
        catch(NumberFormatException e)
        {
            writeText(ex, 400, "Invalid 'id'.");
        }
    }

    private void endHour(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.DebugForceEndOfHour();
        writeText(ex, 200, "Forced end of hour.");
    }

    private void endDay(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.DebugForceEndOfDay();
        writeText(ex, 200, "Forced end of day.");
    }

    private void endWeek(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.DebugForceEndOfWeek();
        writeText(ex, 200, "Forced end of week.");
    }

    private void saveGame(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        boolean debug = false;
        try
        {
            debug = game.GetConfig().DebugMode();
        }
        catch(Throwable ignored)
        {
        }

        if(debug)
        {
            game.Save();
            writeText(ex, 200, "Manual save requested (debug mode).");
        }
        else
        {
            game.Save();
            writeText(ex, 200, "Save requested.");
        }
    }

    private void fastForward(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String tStr = q.get("ticks");
        if(tStr == null || tStr.isBlank())
        {
            writeText(ex, 400, "Missing 'ticks'");
            return;
        }

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        try
        {
            int ticks = Integer.parseInt(tStr);
            game.DebugAdvanceTicks(ticks);
            writeText(ex, 200, "Fast forwarded by " + ticks + " ticks.");
        }
        catch(NumberFormatException nfe)
        {
            writeText(ex, 400, "Invalid 'ticks'.");
        }
    }

    private void endMinute(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        game.DebugForceEndOfMinute();
        writeText(ex, 200, "Forced end of minute.");
    }

    private void info(com.sun.net.httpserver.HttpExchange ex) throws IOException
    {
        Map<String, String> q = readParams(ex);
        String target = q.getOrDefault("target", "").toLowerCase();

        var game = launchserver.api.GameAccess.get();
        if(game == null)
        {
            writeText(ex, 503, "Game not ready");
            return;
        }

        StringBuilder sb = new StringBuilder();

        switch(target)
        {
            case "all":
            {
                sb.append("---=== All entities ===---\n");
                sb.append(String.format("Alliances:     %d\n", game.GetAlliances().size()));
                sb.append(String.format("Wars:          %d\n", game.GetTreaties().size()));
                sb.append(String.format("Players:       %d\n", game.GetPlayers().size()));
                sb.append(String.format("Missiles:      %d\n", game.GetMissiles().size()));
                sb.append(String.format("Interceptors:  %d\n", game.GetInterceptors().size()));
                sb.append(String.format("Missile Sites: %d\n", game.GetMissileSites().size()));
                sb.append(String.format("SAM Sites:     %d\n", game.GetSAMSites().size()));
                sb.append(String.format("Loots:         %d\n", game.GetLoots().size()));
                sb.append(String.format("Radiations:    %d\n", game.GetRadiations().size()));
                sb.append("--------------------------");
                returnText(ex, sb.toString());
                return;
            }
            case "player":
            {
                String idStr = q.get("id");
                if(idStr == null || idStr.isBlank())
                {
                    writeText(ex, 400, "Missing 'id'");
                    return;
                }

                try
                {
                    int id = Integer.parseInt(idStr);
                    launch.game.entities.Player p = game.GetPlayer(id);
                    if(p == null)
                    {
                        writeText(ex, 404, "Player not found.");
                        return;
                    }

                    sb.append(String.format("---=== Player %d - %s ===---\n", p.GetID(), p.GetName()));
                    sb.append(String.format("Avatar ID:            %d\n", p.GetAvatarID()));
                    sb.append(String.format("Wealth:               %d\n", p.GetWealth()));
                    sb.append(String.format("State time remaining: %d\n", p.GetStateTimeRemaining()));
                    sb.append(String.format("Last seen:            %s ago\n", prettyDuration(System.currentTimeMillis() - p.GetLastSeen())));

                    String allianceLine = "<Isn't in one>";
                    try
                    {
                        if(p.GetAllianceIDForDataStorage() != launch.game.Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            var a = game.GetAlliance(p.GetAllianceIDForDataStorage());
                            if(a != null)
                            {
                                if(p.GetIsAnMP())
                                {
                                    allianceLine = a.GetName() + " <LEADER>";
                                }
                                else
                                {
                                    if(p.GetRequestingToJoinAlliance())
                                    {
                                        allianceLine = a.GetName() + " <REQ.JOIN>";
                                    }
                                    else
                                    {
                                        allianceLine = a.GetName();
                                    }
                                }
                            }
                        }
                    }
                    catch(Throwable ignored)
                    {
                    }
                    sb.append(String.format("Alliance:             %s\n", allianceLine));

                    if(p.GetAWOL())
                    {
                        sb.append("AWOL.\n");
                    }
                    if(p.GetRespawnProtected())
                    {
                        sb.append("Respawn protected.\n");
                    }
                    if(p.GetIsAnAdmin())
                    {
                        sb.append("Is an administrator.\n");
                    }

                    launch.game.User found = null;
                    try
                    {
                        for(launch.game.User u : game.GetUsers())
                        {
                            if(u.GetPlayerID() == id)
                            {
                                found = u;
                                break;
                            }
                        }
                    }
                    catch(Throwable ignored)
                    {
                    }

                    if(found != null)
                    {
                        sb.append("\nUser info:\n");
                        try
                        {
                            sb.append("Ban state: ").append(found.GetBanState().name()).append('\n');
                        }
                        catch(Throwable ignored)
                        {
                        }
                        try
                        {
                            if(found.GetUnderAttack())
                            {
                                sb.append("Under attack.\n");
                            }
                        }
                        catch(Throwable ignored)
                        {
                        }
                        try
                        {
                            if(found.GetNuclearEscalation())
                            {
                                sb.append("Nuclear escalation.\n");
                            }
                        }
                        catch(Throwable ignored)
                        {
                        }
                        try
                        {
                            if(found.GetAllyUnderAttack())
                            {
                                sb.append("Ally under attack.\n");
                            }
                        }
                        catch(Throwable ignored)
                        {
                        }
                        try
                        {
                            sb.append(String.format("%d unread reports.\n", found.GetUnreadReports()));
                        }
                        catch(Throwable ignored)
                        {
                        }
                    }

                    String footer = String.format("---=== Player %d - %s ===---", p.GetID(), p.GetName()).replaceAll(".", "-");
                    sb.append(footer);

                    returnText(ex, sb.toString());
                    return;
                }
                catch(NumberFormatException nfe)
                {
                    writeText(ex, 400, "Invalid 'id'.");
                    return;
                }
            }
            default:
            {
                sb.append("Invalid command. Usage for 'info':\n");
                sb.append("'info all' - Display a brief summary of everything.\n");
                sb.append("'info player [id]' - Display information about player [id].");
                returnText(ex, sb.toString());
            }
        }
    }

    // These are the API helpers. DO NOT MODIFY THESE. Only change context of "static void main() (at the bottom)"
    private static void setJsonCORS(Headers headers)
    {
        headers.add("Content-Type", "application/json; charset=utf-8");
        headers.add("Cache-Control", "no-cache");
        headers.add("Access-Control-Allow-Origin", "*");
    }

    private static void writeText(HttpExchange ex, int code, String s) throws IOException
    {
        byte[] body = s.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, body.length);
        try(OutputStream os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private static void writeJson(HttpExchange ex, int code, Object obj) throws IOException
    {
        byte[] body = Json.serialize(obj).getBytes(StandardCharsets.UTF_8);
        setJsonCORS(ex.getResponseHeaders());
        ex.sendResponseHeaders(code, body.length);
        try(OutputStream os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private static int parseInt(String s, int def)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Exception e)
        {
            return def;
        }
    }

    private static Map<String, String> readParams(HttpExchange ex) throws IOException
    {
        Map<String, String> out = new LinkedHashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if(raw != null && !raw.isEmpty())
        {
            mergeParams(out, raw);
        }
        if("post".equalsIgnoreCase(ex.getRequestMethod()))
        {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            // Accept x-www-form-urlencoded for simplicity, makes the api easier here to move data.
            if(body != null && !body.isEmpty())
            {
                mergeParams(out, body);
            }
        }
        return out;
    }

    private static void mergeParams(Map<String, String> out, String raw) throws IOException
    {
        for(String pair : raw.split("&"))
        {
            int eq = pair.indexOf('=');
            if(eq < 0)
            {
                continue;
            }
            String k = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String v = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            out.put(k, v);
        }
    }

    // Authorization API tokens.
    private boolean authorized(HttpExchange ex)
    {
        if(apiSecret.isEmpty())
        {
            return true;
        }
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if(auth == null)
        {
            return false;
        }
        int sp = auth.indexOf(' ');
        if(sp < 0)
        {
            return false;
        }
        String scheme = auth.substring(0, sp).trim();
        String token = auth.substring(sp + 1).trim();
        if(!scheme.equalsIgnoreCase("Bearer"))
        {
            return false;
        }
        return apiSecret.equals(token);
    }

    private void reject(HttpExchange ex, int code, String msg) throws IOException
    {
        byte[] body = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, body.length);
        try(OutputStream os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    // Wraps context handlers to require auth unless it is not wrapped. aka : withAuth()
    private HttpHandler withAuth(CheckedHandler inner)
    {
        return ex ->
        {
            try
            {
                if(!authorized(ex))
                {
                    reject(ex, 403, "forbidden");
                    return;
                }
                inner.handle(ex);
            }
            catch(IOException ioe)
            {
                try
                {
                    reject(ex, 500, "internal IOE exception");
                }
                catch(IOException ignored)
                {
                }
            }
        };
    }

    private static void returnText(com.sun.net.httpserver.HttpExchange ex, String s) throws IOException
    {
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] body = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, body.length);
        try(var os = ex.getResponseBody())
        {
            os.write(body);
        }
    }

    private static String prettyDuration(long ms)
    {
        if(ms < 0)
        {
            ms = 0;
        }
        long sec = ms / 1000;
        long min = sec / 60;
        sec %= 60;
        long hr = min / 60;
        min %= 60;
        long day = hr / 24;
        hr %= 24;
        if(day > 0)
        {
            return String.format("%dd %dh %dm %ds", day, hr, min, sec);
        }
        if(hr > 0)
        {
            return String.format("%dh %dm %ds", hr, min, sec);
        }
        if(min > 0)
        {
            return String.format("%dm %ds", min, sec);
        }
        return String.format("%ds", sec);
    }

    // Handler to throw IOE exceptions.
    @FunctionalInterface
    private interface CheckedHandler
    {

        void handle(HttpExchange ex) throws IOException;
    }

    public static void main(String[] args) throws Exception
    {
        String gameHost = env("GAME_HOST", "launch.notazipbomb.zip");
        int gamePort = Integer.parseInt(env("GAME_PORT", "4030"));
        int httpPort = Integer.parseInt(env("API_PORT", "4035"));

        GameBridge bridge = new GameBridge(gameHost, gamePort);
        ApiServer api = new ApiServer(bridge, httpPort);
        api.start();

        System.out.println("[API] Ready. Endpoints:");
        System.out.println("  GET  /health");
        System.out.println("  GET  /api/status");
        System.out.println("  GET  /api/command?cmd=&inst=");
        System.out.println("  GET  /api/request?obj=&inst=&start=&len=");
        System.out.println("  GET  /api/object?obj=&inst=&off=");
    }

    private static String env(String k, String d)
    {
        String v = System.getenv(k);
        return v == null || v.isBlank() ? d : v;
    }
}
