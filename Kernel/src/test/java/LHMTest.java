import com.potato.kernel.External.LHMHelper;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

/**
 * @param bytes length 4
 */
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
        // Rust `to_ne_bytes()` = native endian; assume little endian on Windows/typical x86
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

class WindowsBridgeJava {
    private static final String IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 55555;
    private static final String CHECK = "fan-control-check";
    private static final String CHECK_RESPONSE = "fan-control-ok";

    private final Process processHandle;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public WindowsBridgeJava(Process processHandle, Socket socket) throws IOException {
        this.processHandle = processHandle;
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    // Equivalent of `try_connect`
    static Socket tryConnect() throws IOException {
        int attempts = 10;
        for (int i = 0; i < attempts; i++) {
            for (int port = DEFAULT_PORT; port <= 65535; port++) {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(IP, port), 200);
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();

                    // write CHECK
                    out.write(CHECK.getBytes());
                    out.flush();

                    int prevTimeout = socket.getSoTimeout();
                    try {
                        socket.setSoTimeout(10);

                        byte[] buf = new byte[CHECK_RESPONSE.length()];
                        int read = 0;
                        while (read < buf.length) {
                            int r = in.read(buf, read, buf.length - read);
                            if (r < 0) {
                                throw new EOFException("Unexpected end of stream");
                            }
                            read += r;
                        }

                        String response = new String(buf);
                        if (!CHECK_RESPONSE.equals(response)) {
                            socket.close();
                            continue;
                        }
                    } catch (SocketTimeoutException e) {
                        socket.close();
                        continue;
                    } finally {
                        // restore timeout
                        try {
                            socket.setSoTimeout(prevTimeout);
                        } catch (IOException ignored) {
                        }
                    }

                    // success
                    return socket;
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    // try next port
                }
            }
            try {
                Thread.sleep(50L * i);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        throw new IOException("No connection found");
    }

    // Equivalent of `send(&mut self, packet: impl Into<Packet>)`
    public void send(Command command) throws IOException {
        send(Packet.fromCommand(command));
    }

    public void sendInt(int value) throws IOException {
        send(Packet.fromInt(value));
    }

    private void send(Packet packet) throws IOException {
        out.write(packet.bytes());
        out.flush();
    }

    // Equivalent of `read<T: From<Packet>>`
    public int readInt() throws IOException {
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

    // Equivalent of `close_and_wait_server`
    public void shutdown() throws IOException, InterruptedException {
        send(Command.Shutdown);
        int exitCode = processHandle.waitFor();
        if (exitCode != 0) {
            throw new IOException("Wrong Windows server exit status: " + exitCode);
        }
        socket.close();
    }
}

public class LHMTest {
    @Test
    void test() throws IOException, InterruptedException {
        System.out.println("Admin: " + isAdmin());

//        Process process = null;
//        Socket socket = null;
//
//        try {
//            // 1\) Start the Windows server (same binary as Rust `spawn_windows_server`)
//            ProcessBuilder pb = new ProcessBuilder(
//                    "./src/main/resources/LibreHardwareMonitorWrapper.exe"
//            );
//            // optional: set working directory, env, etc.
//            pb.redirectErrorStream(true); // swallow or log output if needed
//            process = pb.start();
//
//            // 2\) Connect to the server (handshake with "fan-control-check")
//            socket = WindowsBridgeJava.tryConnect();
//
//            // 3\) Create the bridge
//            WindowsBridgeJava bridge = new WindowsBridgeJava(process, socket);
//
//            // 4\) Example: get hardware list (Rust sends `GetHardware` once in `new()`)
//            // Here we just show how to send a command and read an `int` back.
//            // Actual protocol: after `GetHardware`, server sends one JSON line over the stream.
//            // That JSON part is *not* implemented here; this is just the packet usage demo.
//
//            // Send "get value" for sensor/control index 0
//            bridge.send(Command.GetHardware); // same as Rust `send(Command::GetValue)`
//            bridge.sendInt(0);             // same as Rust `send(I32::from(index))`
//
//            int value = bridge.readInt();  // same as Rust `let value: I32 = self.read()?;`
//            System.out.println("Sensor/control value = " + value);
//
//            // 5\) Update command example
//            bridge.send(Command.Update);
//
//            // 6\) Shutdown (sends `Shutdown` and waits for process)
//            bridge.shutdown();
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            if (process != null) {
//                process.destroyForcibly();
//            }
//            if (socket != null) {
//                try {
//                    socket.close();
//                } catch (IOException ignored) {
//                }
//            }
//        }
        LHMHelper helper = LHMHelper.connect();
        System.out.println(helper.getHardwareList());

        helper.update();

        int value = helper.getValue(0);
        System.out.println("Value: " + value);

        helper.disconnect();
    }
}
