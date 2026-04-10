package vec;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class VectorClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5010;

        System.out.println("VectorClient waiting for processed vectors...");
        while (true) {
            try {
                Socket socket = new Socket(host, port);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                System.out.println("Connected to VectorClientServer!");

                while (true) {
                    try {
                        double[] received = (double[]) in.readObject();
                        System.out.println("Received processed vector: " + Arrays.toString(received));
                    } catch (EOFException e) {
                        System.out.println("Server closed connection, reconnecting...");
                        break;
                    }
                }

                socket.close();
                Thread.sleep(1000); // wait before reconnect

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("Connection error: " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }
}
