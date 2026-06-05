package vec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import vectorization.vector;

public class VectorServer implements Runnable  {


	private ArrayList<vector> vectorToClients= new ArrayList<vector>();

	@Override
	public void run() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345...");

            while (true) {
                Socket socket = serverSocket.accept();

                // Handle client in a new thread or directly here for simplicity
                try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()) ) {

                    if(vectorToClients.size()>0) {
                    double[] array = vectorToClients.remove(0).toArray();
                    //System.out.println("Sending vector: "+array[0]+", "+array[1]+", "+array[2]+", "+array[3]);
					out.writeObject(array);
                    out.flush();
                    
                    }
                   

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void addVector(vector v) {
		vectorToClients.add(v);
	}
	
	
}
