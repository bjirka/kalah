package csce315.kalah.model;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import csce315.kalah.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * This will handle socket communication with a client in a separate thread
 *
 */
public class Server {
	private ServerSocket serverSocket = null;
	private ArrayList<ServerThread> threads;
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	/**
	 * Constructor makes server socket connection
	 */
	public Server() {
		threads = new ArrayList<ServerThread>();
	}
	
	public void open() {
		if(serverSocket == null) {
			try {
				serverSocket = new ServerSocket(6673);
			} catch (IOException e) {
				Main.debug("Port number is probably in use!");
				//e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Creates a new thread, each thread will handle socket communication for one client
	 * @return Returns the ID number of the thread that will be used to reference it later
	 */
	public int startThread() {
		ServerThread thread = new ServerThread();
		thread.start();
		threads.add(thread);
		return (threads.size() - 1);
	}
	
	
	/**
	 * Closes the serverSocket if it has been opened and calls the close() function for each thread
	 */
	public void close() {
        try {
        	if(serverSocket != null)
        		serverSocket.close();
        	for(int i = 0; i < threads.size(); i++) {
        		threads.get(i).close();
        	}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	
	/**
	 * Sends a message to a specified client
	 * @param threadId The ID of the thread that the client connection has been made in
	 * @param message The message to be sent to the client
	 */
	public void sendMessage(int threadId, String message) {
		Main.debug("Server sending (to " + threadId + "): " + message);
		threads.get(threadId).sendMessage(message);
	}
	
	public String getMessage(int threadId) {
		return threads.get(threadId).getMessage();
	}
	
	
	/**
	 * This gets the queue property of a given thread. This will be used to add a listener to the property
	 * so the main thread can be notified when an incoming message is received
	 * @param threadId The ID of the thread to get the queue status of
	 * @return Returns a BooleanProperty set to True if the given thread has an empty queue and False otherwise
	 */
	public BooleanProperty queueEmpty(int threadId) {
		return threads.get(threadId).isQueueEmpty();
	}
	
	
	/**
	 * This gets the connected property of a given thread. This will be used to add a listener to the property
	 * so the main thread can be notified when the connection has been made
	 * @param threadId The ID of the thread to get the connection status of
	 * @return Returns a BooleanProperty set to True if the given thread has successfully connected and False otherwise
	 */
	public BooleanProperty connected(int threadId) {
		return threads.get(threadId).isConnected();
	}
	
	
	/**
	 * Gets a StringProperty that holds the READY message from the given client. Can use this to add a listener in case
	 * the client hasn't sent the message yet.
	 * @return Returns a StringProperty that contains the READY message from the given client or null
	 */
	public StringProperty getReady(int threadId) {
		return threads.get(threadId).getReady();
	}
	
	
	/**
	 * This class will be run as its own thread which will handle communication with a single client
	 *
	 */
	private class ServerThread extends Thread{
		private Socket clientSocket = null;
		private PrintWriter out = null;
		private BufferedReader in = null;
		private BooleanProperty connected = new SimpleBooleanProperty(false);
		private Queue<String> incomingMessages = new LinkedList<String>();
		private BooleanProperty queueEmpty = new SimpleBooleanProperty(true);
		private StringProperty readyMessage = new SimpleStringProperty(""); // set when the client sends a READY message

		
		
		/**
		 * Default constructor doesn't do anything
		 */
		public ServerThread() {
		}
		
		
		/**
		 * This is the function that is run when the thread is started
		 */
		public void run() {
			
			// listen for a client connection
			try {
				clientSocket = serverSocket.accept();
				out = new PrintWriter(clientSocket.getOutputStream(), true);
		        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		        connected.set(true);
			} catch (IOException e) {
				//e.printStackTrace();
			}
			
			
			// if we're connected then send the WELCOME message then start infinite loop
			if(connected.get()) {
				out.println("WELCOME");
				
				// infinite loop waiting for messages from the client
				String input;
		        try {
		        	
		        	// first message from clients should be READY
		        	input = in.readLine();
		        	readyMessage.set(input);
		        	
					while((input = in.readLine()) != null) {
						if(input.equals("READY")) {
							readyMessage.set(input);
						} else if(input.equals("OK")) {
							// OK message from the client, start the turn timer
							Main.setStartTime(System.currentTimeMillis());
							if(Main.getOptions().getTimeLimit() > 0) {
								Main.getTimer().setTimer(Main.getOptions().getTimeLimit());
								//timer.setTimer(moveTimerInit/1000);
								//board.getClockLabel(whosTurn).textProperty().bind(Bindings.format("0:%02d", Main.getTimer().timeLeftProperty()));		
								Main.getTimer().start();
							}
							//Main.debug("Server received: " + input + " but ignoring it");
						} else {
							// "input" is the message from the client, need to process it and give it to the main thread
							incomingMessages.add(input);
							queueEmpty.set(false);
							Main.debug("Server received: " + input);
						}
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
		
		
		/**
		 * Sends a message to the client that this thread is connected to
		 * @param message The message to be sent
		 */
		private void sendMessage(String message) {
			out.println(message);
		}
		
		private String getMessage() {
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
		 * Gets a StringProperty that holds the READY message from this client. Can use this to add a listener in case
		 * the client hasn't sent the message yet.
		 * @return Returns a StringProperty that contains the READY message from this client or null
		 */
		public StringProperty getReady() {
			return readyMessage;
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
				if(clientSocket != null)
					clientSocket.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
	}
}