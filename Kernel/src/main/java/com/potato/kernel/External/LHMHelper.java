package com.potato.kernel.External;

import com.potato.kernel.Config;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.potato.kernel.External.ExternalTools.resolveToolsDir;
import static com.potato.kernel.Utils.ProcessUtil.*;

/**
 * the helper to connect with the LibreHardwareMonitor wrapper
 * <p>
 * the implementation is based on the rust implementation in project {@link <a href="https://github.com/wiiznokes/fan-control">...</a>}
 * <p>
 * note: if you need to get hardware info, DO NOT use it, use {@code HardwareManager} instead
 */
public class LHMHelper {
    private static final String IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 55555;
    private static final String CHECK = "fan-control-check";
    private static final String CHECK_RESPONSE = "fan-control-ok";
    private static final int PORT_FIND_RANGE = 50;

    private final Process processHandle;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    private static boolean hasConnected = false;

    private LHMHelper(Process process, Socket socket) throws IOException {
        this.processHandle = process;
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    /**
     * connect to wrapper by using socket
     * <p>
     * the wrapper will be opened on 55555 port, but if it is occupied, it will try the next port until it finds a free one or reaches 55555 + 50
     * <p>
     * so, the connection will do the same thing, check port 55555 firstly
     *
     * @return
     * @throws IOException
     */
    public static LHMHelper connect() throws IOException {
        Path toolsDir = resolveToolsDir();

        // Put wrapper logs in a writable per-user directory.
        Path logDir = Paths.get(System.getProperty("user.home"), ".ClinicAssistant", "logs");
        Files.createDirectories(logDir);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path wrapperLog = logDir.resolve("LibreHardwareMonitorWrapper_" + ts + ".log");

        File lhmExe = new File(toolsDir.toFile(), Config.WRAPPER_LOCATION);
        if (!lhmExe.exists()) {
            throw new FileNotFoundException(
                    "LibreHardwareMonitor wrapper not found: " + lhmExe.getAbsolutePath()
                            + " (clinic.externalToolsDir=" + toolsDir.toAbsolutePath() + ")"
            );
        }

        ProcessBuilder processBuilder = new ProcessBuilder(lhmExe.getAbsolutePath(), "--log=error");
        File wrapperWorkDir = lhmExe.getParentFile();
        if (wrapperWorkDir != null && wrapperWorkDir.isDirectory()) {
            processBuilder.directory(wrapperWorkDir);
        }
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(wrapperLog.toFile());
        Process process = processBuilder.start();

        Socket socket = null;

        boolean connected = false;
        long timeoutSeconds = System.currentTimeMillis() + Config.CONNECTION_TIMEOUT * 1000L;

        // the wrapper needs some time to start, so we just need to make sure the connection is successful within the timeout limitation
        while (System.currentTimeMillis() < timeoutSeconds && !connected) {
            if (!process.isAlive()) {
                int exit = -1;
                try {
                    exit = process.exitValue();
                } catch (IllegalThreadStateException ignored) {
                }
                throw new IOException("LibreHardwareMonitorWrapper exited early (exitCode=" + exit + "). See log: " + wrapperLog.toAbsolutePath());
            }

            int port = DEFAULT_PORT;
            while (port < DEFAULT_PORT + PORT_FIND_RANGE) {  // to match the implementation of wrapper
                try {
                    socket = new Socket(IP, port);

                    connected = true;
                    break;
                } catch (IOException e) {
                    port++;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (!connected) {
            throw new IOException("Connection to LHM timed out. See log: " + wrapperLog.toAbsolutePath());
        }

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(CHECK.getBytes());
        outputStream.flush();

        byte[] buffer = new byte[CHECK_RESPONSE.length()];
        int read = 0;
        while (read < buffer.length) {
            int r = socket.getInputStream().read(buffer, read, buffer.length - read);
            if (r < 0) {
                throw new IOException("Unexpected end of stream");
            }
            read += r;
        }

        String response = new String(buffer);
        if (!CHECK_RESPONSE.equals(response)) {
            throw new IOException("Invalid response from LHM");
        }

        hasConnected = true;

        return new LHMHelper(process, socket);
    }

    private void send(Packet packet) throws IOException {
        out.write(packet.bytes());
        out.flush();
    }

    private void send(Command command) throws IOException {
        send(Packet.fromCommand(command));
    }

    private void send(int value) throws IOException {
        send(Packet.fromInt(value));
    }

    private int readInt() throws IOException {
        byte[] buf = new byte[4];
        int read = 0;
        while (read < 4) {
            int r = in.read(buf, read, 4 - read);
            if (r < 0) {
                throw new EOFException("Can't read packet");
            }
            read += r;
        }
        Packet packet = new Packet(buf);
        return packet.toInt();
    }

    public String getHardwareList() throws IOException {
        if (!hasConnected) {
            throw new IllegalStateException("LHM is not connected!");
        }

        send(Command.GetHardware);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String json = bufferedReader.readLine();
        if (json == null) {
            throw new IOException("No data received from LHM");
        }

        return json;
    }

    public int getValue(int index) throws IOException {
        send(Command.GetValue);
        send(index);
        return readInt();
    }

    public void update() throws IOException {
        send(Command.Update);
    }

    public void disconnect() throws IOException, InterruptedException {
        send(Command.Shutdown);
        socket.close();
        forceKillProcess(processHandle);
    }
}

enum Command {
    GetHardware(0),
    SetAuto(1),
    SetValue(2),
    GetValue(3),
    Shutdown(4),
    Update(5);

    private final int code;

    Command(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

record Packet(byte[] bytes) {
    Packet {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Packet must be 4 bytes");
        }
    }

    public static Packet fromCommand(Command command) {
        int value = command.code();
        return new Packet(intToBytes(value));
    }

    public static Packet fromInt(int value) {
        return new Packet(intToBytes(value));
    }

    public int toInt() {
        return bytesToInt(bytes);
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value),
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24)
        };
    }

    private static int bytesToInt(byte[] b) {
        return ((b[0] & 0xFF)) |
                ((b[1] & 0xFF) << 8) |
                ((b[2] & 0xFF) << 16) |
                ((b[3] & 0xFF) << 24);
    }
}
