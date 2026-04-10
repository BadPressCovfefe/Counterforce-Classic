package tobcomm.api;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tobcomm.TobComm;
import tobcomm.TobCommInterface;
import tobcomm.protocol.ConnectionProvider;
import tobcomm.protocol.TCPProvider;

/**
 * This bridges tobsters TobComm binary protocol to a java API that I can use for HTTP Routes, youre welcome.
 * - This is how it works below:
 * - Connects via TCPProvider to the game server.
 * - Feeds incoming bytes to TobComm.ProcessBytes().
 * - Implements TobCommInterface callbacks to keep a tiny in-memory snapshot.
 * - Exposes helpers to send commands / request objects.
 */

//NOTE: DO NOT MODIFY THIS CLASS.

public final class GameBridge implements TobCommInterface, ConnectionProvider.ConnectionLogger {

    private final ConnectionProvider conn;
    private final TobComm comm;

    //a status snapshot for /api/status.
    private final Map<String, Object> status = new ConcurrentHashMap<>();

    //raw object payloads keyed by obj:inst:off (usable for /api/object fetches)
    private final Map<String, byte[]> objects = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public GameBridge(String host, int port) {
        this.conn = new TCPProvider(host, port, this);
        if (!conn.Initialise()) {
            throw new IllegalStateException("TCPProvider.Initialise() failed");
        }
        this.comm = new TobComm(this);
        status.put("connectedTo", host + ":" + port);
        startReaderThread();
    }

    private static String key(int obj, int inst, int off) {
        return obj + ":" + inst + ":" + off;
    }

    private void startReaderThread() {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[8192];
            status.put("bridgeReader", "started");
            while (running && !conn.Died()) {
                try {
                    if (conn.DataAvailable()) {
                        int n = conn.Read(buf);
                        if (n > 0) {
                            comm.ProcessBytes(Arrays.copyOf(buf, n));
                        }
                    } else {
                        Thread.sleep(5);
                    }
                } catch (Throwable th) {
                    status.put("error", "reader: " + th.getClass().getSimpleName() + ": " + th.getMessage());
                }
            }
            status.put("bridgeReader", "stopped");
        }, "tobcomm-reader");
        t.setDaemon(true);
        t.start();
    }

    public void shutdown() {
        running = false;
        try { conn.Close(); } catch (Throwable ignored) {}
    }

    // Essentially the tobcomm interface.

    @Override public void BytesToSend(byte[] cData) {
        conn.Write(cData);
    }

    @Override public void ObjectReceived(int obj, int inst, int off, byte[] data) {
        objects.put(key(obj, inst, off), data);
        // snapshot values.
        status.put("lastObject", Map.of("obj", obj, "inst", inst, "off", off, "len", data.length));
        status.put("lastObjectAt", System.currentTimeMillis());
    }

    @Override public void CommandReceived(int cmd, int inst) {
        status.put("lastCommand", Map.of("cmd", cmd, "inst", inst));
        status.put("lastCommandAt", System.currentTimeMillis());
    }

    @Override public void ObjectRequested(int obj, int inst, int off, int len) {;
        status.put("lastRequest", Map.of("obj", obj, "inst", inst, "off", off, "len", len));
    }

    @Override public void Error(String err) {
        status.put("error", err);
    }

    @Override public void SyncObjectsProcessed() {
        status.put("syncFlushedAt", System.currentTimeMillis());
    }
    

    @Override public void ConnectionLog(String s) {
        status.put("connLog", s);
    }
    
    //This portion is the API used by the http layer.

    public Map<String, Object> getStatus() {
        return status;
    }

    public byte[] getObject(int obj, int inst, int off) {
        return objects.get(key(obj, inst, off));
    }

    public void sendCommand(int cmd, int inst) {
        comm.SendCommand(cmd, inst);
    }

    public void requestObject(int obj, int inst, int start, int len) {
        comm.RequestObject(obj, inst, start, len);
    }
}
