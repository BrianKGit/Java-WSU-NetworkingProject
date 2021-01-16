// A Link class for data communication between two processes on the same host.
//

import java.net.*;

public class SimpleLink implements Link {
	private DatagramSocket clientSocket;
	private String hostname;
	private InetAddress destination;	
	private int destinationPort;
	
	// Set up a link between senderPort and receiverPort
	public SimpleLink (int senderPort, int receiverPort) throws Exception {
	
		// Open a UDP datagram socket for senderPort
		clientSocket = new DatagramSocket(senderPort);
		
		hostname = "127.0.0.1";  // loopback
		destination = InetAddress.getByName(hostname);		
		
		// Port number
		destinationPort = receiverPort;

	}
	
	// Send message in sendingBuffer of length lengthMessageSent
	public void sendFrame (byte[] sendingBuffer, int lengthMessageSent) throws Exception {

		// Create a packet
		DatagramPacket sendPacket = 
			new DatagramPacket(sendingBuffer, lengthMessageSent, destination, destinationPort);

		// Send a message
		clientSocket.send(sendPacket);
	}

	// Receive message and place in receivingBuffer and return its length 
	public int receiveFrame (byte[] receivingBuffer) throws Exception {

		// Create a packet
		DatagramPacket receivedPacket = 
			new DatagramPacket(receivingBuffer, receivingBuffer.length);
		
		// Receive a message
		clientSocket.receive(receivedPacket);
		return receivedPacket.getLength();
	}
		
	// Close connection
	public void disconnect() throws Exception {
		// Close the socket
		clientSocket.close();
	}
}
