package vec;



import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DummyTurnListener implements Runnable {

    private static final int PORT = 5023;
    private volatile boolean isWhiteTurn = false;
    private volatile boolean running = true;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //System.out.println("🟢 TurnListener active on port " + PORT);

            while (running) {
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

//                    System.out.println("Client connected for turn updates: " + clientSocket.getInetAddress());

                    while (running && !clientSocket.isClosed()) {
                        try {
                            boolean received = in.readBoolean();
                            isWhiteTurn = received;
                            //System.out.println("🔄 Turn updated: isWhiteTurn = " + isWhiteTurn);
                        } catch (EOFException eof) {
                            // client closed connection normally
//                            System.out.println("👋 Client disconnected.");
                            break;
                        }
                    }

                } catch (IOException inner) {
                   // System.err.println("⚠ TurnListener connection error: " + inner);
                }
            }

        } catch (IOException e) {
           // System.err.println("❌ TurnListener fatal error: " + e.getMessage());
        }
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void stop() {
        running = false;
        System.out.println("🛑 TurnListener stopped.");
    }

    public static void main(String[] args) {
        DummyTurnListener listener = new DummyTurnListener();
        new Thread(listener).start();
    }
}
