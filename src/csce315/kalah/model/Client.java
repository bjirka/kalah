package csce315.kalah.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import csce315.kalah.Main;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This will handle socket communication with a server in a separate thread 
 *
 */
public class Client {
	private Socket clientSocket = null;
	private ClientThread thread = null;
	private StringProperty infoMessage = new SimpleStringProperty(""); // set when the server sends an INFO message
	private StringProperty beginMessage = new SimpleStringProperty(""); // set when the server sends the BEGIN message
	private Queue<String> incomingMessages = new LinkedList<String>(); // incoming messages are placed here while waiting to be retreived
	private BooleanProperty queueEmpty = new SimpleBooleanProperty(true);
	
	
	/**
	 * Constructor initializes variables
	 */
	public Client() {
	}
	
	
	/**
	 * Creates the thread the client will use for socket communication to the server
	 */
	public void startThread(String serverIpAddress, String port) {
		if(serverIpAddress.equals("")) {
			serverIpAddress = "127.0.0.1";
		}
		thread = new ClientThread(serverIpAddress, port);
		thread.start();
	}
	
	
	/**
	 * Closes the clientSocket if it has been opened and calls the close() function for the thread
	 */
	public void close() {
        try {
        	if(clientSocket != null)
        		clientSocket.close();
        	thread.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	
	/**
	 * Sends a message to the server
	 * @param message The message to be sent to the server
	 */
	public void sendMessage(String message) {
		thread.sendMessage(message);
	}
	
	public String getMessage() {
		String message = null;
		if(incomingMessages.size() > 0) {
			message = incomingMessages.remove();
		}
		if(incomingMessages.size() == 0) {
			queueEmpty.set(true);
		}
		return message;
	}
	
	
	/**
	 * This gets the queue property of a given thread. This will be used to add a listener to the property
	 * so the main thread can be notified when an incoming message is received
	 * @param threadId The ID of the thread to get the queue status of
	 * @return Returns a BooleanProperty set to True if the given thread has an empty queue and False otherwise
	 */
	public BooleanProperty queueEmpty() {
		return thread.isQueueEmpty();
	}
	
	
	/**
	 * This gets the connected property of the thread. This will be used to add a listener to the property
	 * so the main thread can be notified when the connection has been made
	 * @return Returns a BooleanProperty set to True if the thread has successfully connected and False otherwise
	 */
	public BooleanProperty connected() {
		return thread.isConnected();
	}
	
	
	/**
	 * Gets a StringProperty that holds the INFO message from the server. Can use this to add a listener in case
	 * the server hasn't sent the message yet.
	 * @return Returns a StringProperty that contains the INFO message from the server or null
	 */
	public StringProperty getInfo() {
		return infoMessage;
	}
	
	
	/**
	 * Gets a StringProperty that holds the BEGIN message from the server. Can use this to add a listener in case
	 * the server hasn't sent the message yet.
	 * @return Returns a StringProperty that contains the BEGIN message from the server or null
	 */
	public StringProperty getBegin() {
		return beginMessage;
	}
	
	
	private class ClientThread extends Thread{
		private PrintWriter out = null;
		private BufferedReader in = null;
		private BooleanProperty connected = new SimpleBooleanProperty(false);
		private BooleanProperty failure = new SimpleBooleanProperty(false);
		private Boolean foundServer = false;
		private String serverAddress;
		private int port;
		
		
		/**
		 * Default constructor does nothing
		 */
		public ClientThread(String serverAddress, String port) {
			this.serverAddress = serverAddress;
			this.port = Integer.parseInt(port);
		}
		
		
		/**
		 * This is the function that is run when the thread is started
		 */
		public void run() {
			int attempts = 0;
			// try up to 50 times to make a connection to the server
			while(foundServer == false && attempts < 50) {
				try {
					clientSocket = new Socket(serverAddress, port);
					out = new PrintWriter(clientSocket.getOutputStream(), true);
			        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			        foundServer = true;
				} catch (UnknownHostException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				attempts++;
			}
			
			if(foundServer == true) {
				String input;
		        try {
		        	// read the first message from the server
		        	input = in.readLine();
		        	if(input.equals("WELCOME")) { // if the first message is "WELCOME" then a successful connection has been made
		        		connected.set(true);

		        		// second message is INFO message, store it in the infoMessage StringProperty
		        		input = in.readLine();
		        		infoMessage.set(input);

		        		// third message is BEGIN message, store it in the beginMessage StringProperty
		        		input = in.readLine();
		        		Main.debug("Got BEGIN message");
		        		beginMessage.set(input);
		        		
		        		while((input = in.readLine()) != null) {
		        			if(input.equals("BEGIN")) {
		        				beginMessage.set(input);
		        			} else if(input.length() >= 4 && input.substring(0, 4).equals("INFO")) {
		        				Main.debug("Got another INFO message");
		        				infoMessage.set(input);
		        			} else if(input.equals("OK")) {
		        				// do nothing with the OK messages from the server
		        				Main.debug("Client received: " + input + " but ignoring it");
		        			} else {
		        				incomingMessages.add(input);
		        				queueEmpty.set(false);
		        				Main.debug("Client received: " + input);
		        			}
		        		}
					} else {
						// server didn't send "WELCOME" message
						failure.set(true);		
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			} else {
				// no connection made for some reason
				failure.set(true);
			}
		}
		
		
		/**
		 * Sends a message to the server
		 * @param message The message to send to the server
		 */
		private void sendMessage(String message) {
			out.println(message);
		}
		
		
		/**
		 * Returns the BooleanProperty containing the current empty status of the incomming message queue
		 * @return Returns a BooleanProperty that will contain True if the queue is empty and False otherwise 
		 */
		private BooleanProperty isQueueEmpty() {
			return queueEmpty;
		}
		
		
		/**
		 * Returns the BooleanProperty containing the current connection status that a listener can be added to.
		 * @return Returns a BooleanProperty that will contain True if connected and False otherwise 
		 */
		private BooleanProperty isConnected() {
			return connected;
		}
		
		
		/**
		 * Closes any network resources that were opened
		 */
		private void close() {
			try {
				if(out != null)
					out.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
	}

}