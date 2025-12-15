package com.potato.kernel.External;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class LHMHelper {
    private static final String IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 55555;
    private static final String CHECK = "fan-control-check";
    private static final String CHECK_RESPONSE = "fan-control-ok";

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

    public static LHMHelper connect() throws IOException {
        File projectRoot = new File(System.getProperty("user.dir"));
        File lhmExe = new File(projectRoot, "../LibreHardwareMonitorWrapper/LibreHardwareMonitorWrapper.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(lhmExe.getAbsolutePath(), "--log=error").inheritIO();
        Process process = processBuilder.start();

        Socket socket = new Socket();
        int port = DEFAULT_PORT;

        // fixme connection
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (port < 65535) {  // to match the implementation of wrapper
            try {
                socket.connect(new InetSocketAddress(IP, DEFAULT_PORT));
                break;
            } catch (IOException e) {
                port++;
            }
        }
        if (port == 65535) {
            throw new IOException("Could not connect to server");
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
        int exitCode = processHandle.waitFor();
        if (exitCode != 0) {
            throw new IOException("LHM process exited with code: " + exitCode);
        }
        socket.close();
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
