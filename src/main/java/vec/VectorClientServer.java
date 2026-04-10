package vec;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class VectorClientServer {

    private static final int SERVER_PORT = 12345;
    private static final int CLIENT_PORT = 5010;

    private static class ClientConnection {
        Socket socket;
        ObjectOutputStream out;

        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
        }

        void send(double[] vector) throws IOException {
            out.writeObject(vector);
            out.flush();
        }

        void close() {
            try { out.close(); } catch (IOException ignored) {}
            try { socket.close(); } catch (IOException ignored) {}
        }

        @Override
        public String toString() {
            return socket.toString();
        }
    }

    public static void main(String[] args) {
        List<ClientConnection> clients = new CopyOnWriteArrayList<>();

        System.out.println("VectorClientServer relay starting...");

        // Thread: accept VectorClients
        Thread acceptThread = new Thread(() -> {while(true) {
            try (ServerSocket listener = new ServerSocket(CLIENT_PORT)) {
                System.out.println("Listening for VectorClients on port " + CLIENT_PORT + "...");
                while (true) {
                    Socket c = listener.accept();
                    try {
                        ClientConnection conn = new ClientConnection(c);
                        clients.add(conn);
                       System.out.println("Client connected: " + c);
                    } catch (IOException e) {
                        System.err.println("Failed to init client stream: " + e.getMessage());
                        try { c.close(); } catch (IOException ignored) {}
                    }
                }
            } catch (IOException e) {
               System.err.println("Acceptor stopped: " + e.getMessage());
            }
        }});
        acceptThread.setDaemon(true);
        acceptThread.start();

        // Main loop: receive from VectorServer and broadcast
        while (true) {
            try (Socket serverSocket = new Socket("localhost", SERVER_PORT);
                 ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream())) {

                double[] vector = (double[]) in.readObject();

             for (int i = 0; i < vector.length; i++) {vector[i]*=100.0 ; vector[i]=vector[i]%1.0;}//normalize(vector[i]); 
             System.out.println("sending: " + Arrays.toString(vector));
              
                for (Iterator<ClientConnection> it = clients.iterator(); it.hasNext(); ) {
                    ClientConnection client = it.next();
                    try {
                    	
                        client.send(vector);
                    } catch (IOException e) {
                  //      System.err.println("Removing dead client: " + client);
                        it.remove();
                        client.close();
                    }
                }

                Thread.sleep(500);

            } catch (EOFException e) {
               /// System.err.println("VectorServer closed connection, retrying...");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            } catch (Exception e) {
               // System.err.println("Relay error: " + e);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }
    
    public static double normalize(double value) {
        value = Math.abs(value); // ignore sign for normalization logic

        // Isolate fractional part
        double frac = value % 1.0;

        // If there’s no fractional part, just return 0
        if (frac == 0) return 0.0;

        // Shift decimal until first significant digit
        while (frac < 0.1 && frac > 0.0) {
            frac *= 10.0;
        }

        // Remove any leading whole number part (like 1.023 → 0.023 → 0.23)
        while (frac >= 1.0) {
            frac -= 1.0;
        }

        // Finally, drop an extra 0 if first digit still 0
        while (frac < 0.1 && frac > 0.0) {
            frac *= 10.0;
        }

        return frac;
    }
    
}
