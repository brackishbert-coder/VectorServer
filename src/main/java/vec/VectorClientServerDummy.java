package vec;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import game.BoardUtils;
import game.VectorMoveValidator;
import game.LegalMoveLibrary;

public class VectorClientServerDummy {

	private static final int SERVER_PORT = 12345;
	private static final int CLIENT_PORT = 5010;

	static int currentVectorIndex = 0;
	private static DummyTileListener tileListener;
	private static DummyTurnListener turnListener;
	private static boolean prevTurn;

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
			try {
				out.close();
			} catch (IOException ignored) {
			}
			try {
				socket.close();
			} catch (IOException ignored) {
			}
		}

		@Override
		public String toString() {
			return socket.toString();
		}
	}

	public static void main(String[] args) {
		List<ClientConnection> clients = new CopyOnWriteArrayList<>();
		tileListener = new DummyTileListener();

		new Thread(tileListener).start();
		turnListener = new DummyTurnListener();
		BoardUtils.create_a_eight_by_eight_of_tiles();
		new Thread(turnListener).start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("VectorClientServer relay starting...");

		Thread acceptThread = new Thread(() -> {
			while (true) {
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
							try {
								c.close();
							} catch (IOException ignored) {
							}
						}
					}
				} catch (IOException e) {
					System.err.println("Acceptor stopped: " + e.getMessage());
				}
			}
		});
		acceptThread.start();

		// Main loop: receive from VectorServer and broadcast
		while (true) {

			

			//System.out.println("🔄 Turn updated: isWhiteTurn = " + turn);
			
			for (Iterator<ClientConnection> it = clients.iterator(); it.hasNext();) {
				ClientConnection client = it.next();
				try {
					boolean turn = turnListener.isWhiteTurn();double[] vector = {};
					vector = LegalMoveLibrary.getRandomValidMoveNormalizedSynced(!turn);
					if(turn &&BoardUtils.isWhitePiece(vector)) {
						System.out.println(
								"vector send: " + vector[0] + " " + vector[1] + " " + vector[2] + " " + vector[3]
										+ "turn: " + turn + " Legal: " + VectorMoveValidator.isLegalMove(vector, false)
										+ " is White " + BoardUtils.isWhitePiece(vector));
						client.send(vector);
						
					}else if(!turn&&BoardUtils.isBlackPiece(vector)) {
						System.out.println(
								"vector send: " + vector[0] + " " + vector[1] + " " + vector[2] + " " + vector[3]
										+ "turn: " + turn + " Legal: " + VectorMoveValidator.isLegalMove(vector, false)
										+  " is Black " + BoardUtils.isBlackPiece(vector));
						client.send(vector);
					}
					prevTurn=turn;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (IOException e) {
					// System.err.println("Removing dead client: " + client);
					clients.remove(client);
					client.close();
				}
			}
		}

	}

	public static double normalize(double value) {
		value = Math.abs(value); // ignore sign for normalization logic

		// Isolate fractional part
		double frac = value % 1.0;

		// If there’s no fractional part, just return 0
		if (frac == 0)
			return 0.0;

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
