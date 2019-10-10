package csce315.kalah;
	
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import csce315.kalah.model.Client;
import csce315.kalah.model.Game;
import csce315.kalah.model.GameOptions;
import csce315.kalah.model.Player.PLAYER_TYPE;
import csce315.kalah.model.Server;
import csce315.kalah.model.SimpleTimer;
import csce315.kalah.view.OptionsDialogController;
import csce315.kalah.view.RootViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	
	public enum APP_MODE {Server, Client};
	private static Stage primaryStage;
	private BorderPane rootLayout;
	private static Server server;
	private static Client client;
	private static GameOptions options;
	private ArrayList<Integer> initialBoardState;
	private static SimpleTimer timer = new SimpleTimer();
	private static long startTime;
	private static long endTime;

	private int readyCount = 0;
	
	/**
	 * The beginning of the JavaFX application 
	 */
	@Override
	public void start(Stage primaryStage) {
		//this.primaryStage = primaryStage;
		setPrimaryStage(primaryStage);
		getPrimaryStage().setTitle("Kalah!  -  by CSCE 315 Group 2");
		
		// set the application icon
		getPrimaryStage().getIcons().add(new Image("csce315/kalah/view/app_icon.png"));
		
		initRootLayout();
		
		server = new Server();
		setServer(server);
		client = new Client();
		setClient(client);
		options = new GameOptions();
		setOptions(options);
		
		showOptionsDialog(options, server, client);
		
		//loadGame();

	}
	
	
	/**
	 * Called when the JavaFX application closes, this will close the resouces that the client/server use
	 */
	public void stop() {
		try {
			server.close();
			client.close();
		} catch(Exception e) {
			
		}
	}
	
	
	/**
	 * Just launches the JavaFX application
	 * @param args Command line arguments aren't used
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	
	/**
	 * Takes what was set in the Options dialog window and starts a new game
	 */
	private void loadGame() {
		Main.debug("Player 1 thread id: " + Main.getOptions().getPlayer1ThreadId());
		if(options.getAppMode() == APP_MODE.Server) {
			
			// add a waiting message until all clients are ready
		    StackPane stack = new StackPane();
		    Label waitingLabel = new Label("Waiting for Client");
		    stack.getChildren().add(waitingLabel);
			rootLayout.setCenter(stack);
			
			
			initialBoardState = Game.initialBoardState(options.getNumPlayerPits(), options.getNumStones(), options.getRandomStones());
			
			String infoMessage1 = "INFO " + options.getNumPlayerPits() + " " + options.getNumStones() + " " + (options.getTimeLimit() * 1000);
			String infoMessage2 = "";
			if(options.getRandomStones()) {
				infoMessage2 = "R";
				for(int i = 0; i < initialBoardState.size(); i++) {
					infoMessage2 += " " + initialBoardState.get(i);
				}
			}else {
				infoMessage2 = "S";
			}
			
			boolean waitForReady = false;
			
			if(options.getPlayer0Type() == PLAYER_TYPE.Client) {
				// add listener for READY message from this client
				server.getReady(options.getPlayer0ThreadId()).addListener((observer, oldValue, newValue) -> {
					if(newValue != "") {
						//gotClientReady(initialBoardState);
						gotClientReady();
						server.getReady(options.getPlayer0ThreadId()).set("");
					}
				});
				waitForReady = true;
				
				// need to send INFO message to client 0
				server.sendMessage(options.getPlayer0ThreadId(), infoMessage1 + " F " + infoMessage2);
				Main.debug("Send INFO message to 0");
			}
			
			if(options.getPlayer1Type() == PLAYER_TYPE.Client) {
				// add listener for READY message from this client
				server.getReady(options.getPlayer1ThreadId()).addListener((observer, oldValue, newValue) -> {
					if(newValue != "") {
						//gotClientReady(initialBoardState);
						gotClientReady();
						server.getReady(options.getPlayer1ThreadId()).set("");
					}
				});
				waitForReady = true;
				
				// need to send INFO message to client 1
				server.sendMessage(options.getPlayer1ThreadId(), infoMessage1 + " S " + infoMessage2);
				Main.debug("Send INFO message to 1");
			}
			
			
			// if there are no client players then no need to wait for READY messages, just start the game
			if(!waitForReady) {
				createServerGame();
				//Platform.runLater(() -> {
				//	Game game = new Game(options.getPlayer0Type(), options.getPlayer1Type(), initialBoardState, options.getTimeLimit() * 1000);
			
				//	rootLayout.setCenter(game.getBoardLayout());
				//});
			}
			
			
		// Client mode
		} else if(options.getAppMode() == APP_MODE.Client) {
			// add a listener to the client's infoMessage property
			client.getInfo().addListener((observer, oldValue, newValue) -> {
				Main.debug("Client INFO changed to: " + newValue);
				if(newValue != "") {
					createClientGame(newValue);
					client.getInfo().set("");
				}
			});
			// check to see if infoMessage was already set before the listener was added
			if(!client.getInfo().get().equals("")) {
				Main.debug("INFO message already set");
				createClientGame(client.getInfo().get());
			}
			
			// add a waiting message until server is ready
		    StackPane stack = new StackPane();
		    Label waitingLabel = new Label("Waiting for Server");
		    stack.getChildren().add(waitingLabel);
			rootLayout.setCenter(stack);
			
			
		} else {
			// cancel was clicked so close the program
			Platform.exit();
		}
		
	}
	
	
	/**
	 * A static function that other classes can use to get the primaryStage
	 * @return The application's primary stage
	 */
	static public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	static public SimpleTimer getTimer() {
		return timer;
	}
	
	static public void setStartTime(long start) {
		startTime = start;
	}
	
	static public long getStartTime() {
		return startTime;
	}
	
	static public void setEndTime(long end) {
		endTime = end;
	}
	
	static public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the static primaryStage value
	 * @param primaryStage The primary stage for the application
	 */
	private void setPrimaryStage(Stage primaryStage) {
		Main.primaryStage = primaryStage;
	}
	
	static public Server getServer() {
		return server;
	}
	
	private void setServer(Server server) {
		Main.server = server;
	}
	
	static public Client getClient() {
		return client;
	}
	
	private void setClient(Client client) {
		Main.client = client;
	}
	
	static public GameOptions getOptions() {
		return options;
	}
	
	private void setOptions(GameOptions options) {
		Main.options = options;
	}
	
	
	private void gotClientReady(){
		Main.debug("Got READY");
		readyCount++;
		
		int readyNeeded = 0;
		if(options.getPlayer0Type() == PLAYER_TYPE.Client) {
			readyNeeded++;
		}
		if(options.getPlayer1Type() == PLAYER_TYPE.Client) {
			readyNeeded++;
		}
		
		if(readyCount >= readyNeeded) {
			Main.debug("All READY messages have been received");
			createServerGame();
		}
	}
	
	
	private void createServerGame() {
		if(options.getPlayer0Type() == PLAYER_TYPE.Client) {
			server.sendMessage(options.getPlayer0ThreadId(), "BEGIN");
		}
		if(options.getPlayer1Type() == PLAYER_TYPE.Client) {
			server.sendMessage(options.getPlayer1ThreadId(), "BEGIN");
		}
		
		Platform.runLater(() -> {
			Main.debug("MAKING NEW GAME <<<<<<<<<");
			Main.debug("Board size: " + initialBoardState.size());
			Game game = new Game(options.getPlayer0Type(), options.getPlayer1Type(), initialBoardState, options.getTimeLimit() * 1000);
	
			rootLayout.setCenter(game.getBoardLayout());
		});
	}
	
	
	/**
	 * Parses the INFO message from the server and creates a Game instance
	 * @param infoMessage The INFO message that the server has sent
	 */
	private void createClientGame(String infoMessage){
		Main.debug("Sending READY");
		client.sendMessage("READY");

		// split the message on spaces and store in an ArrayList
		ArrayList<String> msgParts = new ArrayList<String>(Arrays.asList(infoMessage.split(" ")));
		
		ArrayList<Integer> initialBoardState = new ArrayList<Integer>();
		
		// fill and ArrayList with the number of stones each pit will initially contain
		if(msgParts.get(5).equals("S")){ // standard distribution of stones
			int numPlayerPits = Integer.parseInt(msgParts.get(1));
			int numStones = Integer.parseInt(msgParts.get(2));
			for(int i = 0; i < numPlayerPits; i++) {
				initialBoardState.add(numStones);		
			}	
		} else { // random distribution of stones
			int numPlayerPits = Integer.parseInt(msgParts.get(1));
			for(int i = 6; i < (6 + numPlayerPits); i++) {
				initialBoardState.add(Integer.parseInt(msgParts.get(i)));
			}
		}
		
		client.getBegin().addListener((observer, oldValue, newValue) -> {
			if(newValue != "") {
				clientBegin(msgParts.get(4), initialBoardState, msgParts.get(3));
				client.getBegin().set("");
			}
		});
		if(client.getBegin().get().equals("BEGIN")) {
			clientBegin(msgParts.get(4), initialBoardState, msgParts.get(3));
		}


	}

	
	private void clientBegin(String firstOrSecond, ArrayList<Integer> initialBoardState, String milliseconds) {
		Platform.runLater(() -> {
			Game game;
			if(firstOrSecond.equals("F")) { // if this client goes first
				game = new Game(options.getPlayer0Type(), options.getPlayer1Type(), initialBoardState, Integer.parseInt(milliseconds));
			} else { // if this client goes second
				game = new Game(options.getPlayer1Type(), options.getPlayer0Type(), initialBoardState, Integer.parseInt(milliseconds));
			}
				
			// add the board view to the display
			rootLayout.setCenter(game.getBoardLayout());
		});
	}
	
	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/RootView.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			RootViewController controller = loader.getController();
			controller.setMain(this);
			
			// show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			getPrimaryStage().setScene(scene);
			getPrimaryStage().setResizable(false);
			getPrimaryStage().show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Displays the dialog windows that contains the game options, also has button to switch to client mode
	 * @param options The GameOptions instance that will be filled with the game options
	 * @param server The Server instance used for client/server connection
	 * @param client The Client instance used for client/server connection
	 * @return Returns the controller for the options dialog (I don't think this is used anywhere though) 
	 */
	public OptionsDialogController showOptionsDialog(GameOptions options, Server server, Client client) {
	    try {
	        // Load the fxml file and create a new stage for the popup dialog.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Main.class.getResource("view/OptionsDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("csce315/kalah/view/OptionsDialog.css");
	        
	        // Create the dialog Stage.
	        Stage dialogStage = new Stage();
	        dialogStage.setResizable(false);
	        dialogStage.setTitle("Game Options");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initOwner(getPrimaryStage());
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        OptionsDialogController controller = loader.getController();
	        controller.setGameOptions(options);
	        controller.setDialogStage(dialogStage);
	        controller.setServer(server);
	        controller.setClient(client);

	        // Show the dialog and wait until the user closes it
	        dialogStage.showAndWait();

	        loadGame();
	        
	        return controller;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
	/**
	 * Used for outputting debugging messages to the console, can limit messages to only from Server or only from Client
	 * @param message The message to display in the console
	 */
	static public void debug(String message) {
		//String appMode = Main.getOptions().getAppMode().toString();
		//if(appMode.equals("Client"))
		//	System.out.println(appMode + ": " + message);
	}
}
