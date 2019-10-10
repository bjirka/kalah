package csce315.kalah.view;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import csce315.kalah.Main;
import csce315.kalah.Main.APP_MODE;
import csce315.kalah.model.Client;
import csce315.kalah.model.GameOptions;
import csce315.kalah.model.Player.PLAYER_TYPE;
import csce315.kalah.model.Server;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class OptionsDialogController {
	
	@FXML
	private ComboBox<String> player0Type;
	@FXML
	private ComboBox<String> player1Type;
	@FXML
	private ComboBox<Integer> numPlayerPits;
	@FXML
	private ComboBox<Integer> numStones;
	@FXML
	private CheckBox isRandomStones;
	@FXML
	private Slider timer;
	@FXML
	private Label timerLabel;
	@FXML
	private Label player0Subtext;
	@FXML
	private Label player1Subtext;
	@FXML
	private Button okButton;
	
	private Stage dialogStage;
	private Boolean isOk;
	private String ipAddress;
	private int clientWaitingCount;
	private Server server = null;
	private Client client;
	private int player0ThreadId;
	private int player1ThreadId;
	private ChangeListener<Boolean> client0Listener;
	private ChangeListener<Boolean> client1Listener;
	private GameOptions options;
	

	/**
	 * initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		clientWaitingCount = 0;
		player0ThreadId = Main.getOptions().getPlayer0ThreadId();
		player1ThreadId = Main.getOptions().getPlayer1ThreadId();
		switch(Main.getOptions().getPlayer0Type()) {
		case Human:
			player0Type.setValue("Human");
			break;
		case AI:
			player0Type.setValue("AI");
			break;			
		case Client:
			player0Type.setValue("Remote Client");
			break;
		case Server:
			break;
		}
		
		switch(Main.getOptions().getPlayer1Type()) {
		case Human:
			player1Type.setValue("Human");
			break;
		case AI:
			player1Type.setValue("AI");
			break;			
		case Client:
			player1Type.setValue("Remote Client");
			break;
		case Server:
			break;
		}
		if(Main.getOptions().getPlayer0ThreadId() == -1) {
			player0Subtext.setText("");
		}else {
			player0Subtext.setText("Connected!");
			player0Subtext.getStyleClass().add("connection-successful");
		}
		if(Main.getOptions().getPlayer1ThreadId() == -1) {
			player1Subtext.setText("");
		}else {
			player1Subtext.setText("Connected!");
			player1Subtext.getStyleClass().add("connection-successful");
		}
		numPlayerPits.setValue(Main.getOptions().getNumPlayerPits());
		numStones.setValue(Main.getOptions().getNumStones());
		if(Main.getOptions().getRandomStones()) {
			isRandomStones.setSelected(true);
			isRandomStones.setText("Enabled");
		}
		
		timer.setValue(Main.getOptions().getTimeLimit());
		if(Main.getOptions().getTimeLimit() != 0) {
			timerLabel.setText(Main.getOptions().getTimeLimit() + " seconds");
		}
		
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			ipAddress = localhost.getHostAddress().trim();
		} catch (UnknownHostException e) {
			ipAddress = "IP Error";
		}

	
		// set listener to wait for connection to client when "Remote Client" is set as a player type
		player0Type.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			if(newValue.equals("Remote Client")) {
				// open a thread for this player if we don't already have one
				if(player0ThreadId == -1) {
					server.open();
					player0ThreadId = server.startThread();
				}
				// if we don't already have a successful connection for this player
				if(server.connected(player0ThreadId).get() == false) {
					clientWaitingCount++;
					okButton.setDisable(true);
					player0Subtext.setText("Server Address :  " + ipAddress + "  port : 6673  -  Waiting for connection . . . ");
					player0Subtext.getStyleClass().clear();
					player0Subtext.getStyleClass().add("connection-attempting");
					// create a named listener so we can remove it later if we need to
					client0Listener = (observable, oldValue2, newValue2) -> {
						// we've successfully connected, so display message and enable OK button if not waiting for other connections
						Platform.runLater(() -> {
							player0Subtext.setText("Connected!");
							player0Subtext.getStyleClass().clear();
							player0Subtext.getStyleClass().add("connection-successful");
							clientWaitingCount--;
							if(clientWaitingCount == 0) {
								okButton.setDisable(false);
							}
						});
					};
					server.connected(player0ThreadId).addListener(client0Listener);
				} else { // if we already have a successful connection for this player
					player0Subtext.setText("Connected!");
					player0Subtext.getStyleClass().clear();
					player0Subtext.getStyleClass().add("connection-successful");					
				}
			} else { // we've selected something other than "Remote Client"
				if(oldValue.equals("Remote Client")) {
					server.connected(player0ThreadId).removeListener(client0Listener);
					if(player0ThreadId != -1 && server.connected(player0ThreadId).get() == false) {
						clientWaitingCount--;
						if(clientWaitingCount == 0) {
							okButton.setDisable(false);
						}
					}
				}
				player0Subtext.setText("");
			}
	    });
	    
		
		
		// set listener to wait for connection to client when "Remote Client" is set as a player type
		player1Type.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			if(newValue.equals("Remote Client")) {
				// open a thread for this player if we don't already have one
				if(player1ThreadId == -1) {
					server.open();
					player1ThreadId = server.startThread();
				}
				// if we don't already have a successful connection for this player
				if(server.connected(player1ThreadId).get() == false) {
					clientWaitingCount++;
					okButton.setDisable(true);
					player1Subtext.setText("Server Address :  " + ipAddress + " port : 6673  -  Waiting for connection . . . ");
					player1Subtext.getStyleClass().clear();
					player1Subtext.getStyleClass().add("connection-attempting");
					// create a named listener so we can remove it later if we need to
					client1Listener = (observable, oldValue2, newValue2) -> {
						// we've successfully connected, so display message and enable OK button if not waiting for other connections
						Platform.runLater(() -> {
							player1Subtext.setText("Connected!");
							player1Subtext.getStyleClass().clear();
							player1Subtext.getStyleClass().add("connection-successful");
							clientWaitingCount--;
							if(clientWaitingCount == 0) {
								okButton.setDisable(false);
							}
						});
					};
					server.connected(player1ThreadId).addListener(client1Listener);
				} else { // if we already have a successful connection for this player
					player1Subtext.setText("Connected!");
					player1Subtext.getStyleClass().clear();
					player1Subtext.getStyleClass().add("connection-successful");					
				}
			} else { // we've selected something other than "Remote Client"
				if(oldValue.equals("Remote Client")) {
					server.connected(player1ThreadId).removeListener(client1Listener);
					if(player1ThreadId != -1 && server.connected(player1ThreadId).get() == false) {
						clientWaitingCount--;
						if(clientWaitingCount == 0) {
							okButton.setDisable(false);
						}
					}
				}
				player1Subtext.setText("");
			}
	    });
		
				
		// set listener to change the "Enabled" or "Disabled" text next to the checkbox
		isRandomStones.selectedProperty().addListener((observable, oldValue, newValue) -> {
		        if(newValue) {
		        	isRandomStones.setText("Enabled");
		        }else {
		        	isRandomStones.setText("Disabled");
		        }
		});	
		
		timer.valueProperty().addListener((observable, oldValue, newValue) -> {
			int seconds = (int) Math.round(newValue.doubleValue());
			if(seconds == 0) {
				timerLabel.textProperty().setValue("Unlimited");
			} else {
				timerLabel.textProperty().setValue(seconds + " seconds");
			}
		});
		 
	}
	

	/**
	 * Set the Game Options instance to load the selection options into
	 * @param options The object instance that will hold game options
	 */
	public void setGameOptions(GameOptions options) {
		this.options = options;
	}
	
	
	/**
	 * Set the dialog stage so it can be closed when a button is clicked
	 * @param dialogStage The Stage that was created for this dialog box
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	
	/**
	 * Set a Server instance to be used for Client/Server communication
	 * @param server The Server to be used for Client/Server communication
	 */
	public void setServer(Server server) {
		this.server = server;
	}
	
	
	/**
	 * Set a Client instance to be used for Client/Server communication
	 * @param client The Client to be used for Client/Server communication
	 */
	public void setClient(Client client) {
		this.client = client;
	}
	
	
	/**
	 * Called when user clicks the OK button
	 */
	@FXML
	private void handleOK() {
		//isOk = true;
		options.setAppMode(APP_MODE.Server);
		options.setNumPlayerPits(numPlayerPits.getValue());
		options.setNumStones(numStones.getValue());
		options.setPlayer0Type(getPlayer0Type());
		options.setPlayer1Type(getPlayer1Type());
		options.setPlayer0ThreadId(player0ThreadId);
		options.setPlayer1ThreadId(player1ThreadId);
		options.setRandomStones(isRandomStones.isSelected());
		options.setTimeLimit((int) Math.round(timer.getValue()));
		dialogStage.close();
	}
	
	
	/**
	 * Called when user clicks the OK button
	 */
	@FXML
	private void handleCancel() {
		dialogStage.close();
	}
	
	
	/**
	 * Called when user clicks the Client Mode button
	 */
	@FXML
	private void handleClientMode() {
	    try {
	        // Load the fxml file and create a new stage for the popup dialog.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Main.class.getResource("view/ClientConnectDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        
	        page.getStylesheets().add("csce315/kalah/view/OptionsDialog.css");

	        Scene previousScene = dialogStage.getScene();
	        
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        ClientConnectDialogController controller = loader.getController();
	        controller.setGameOptions(options);
	        controller.setDialogStage(dialogStage);
	        controller.setClient(client);
	        controller.setPreviousStage(previousScene);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	
	
	/**
	 * Get the type (Human, AI, or Client) of player 0
	 * @return Returns the player type
	 */
	public PLAYER_TYPE getPlayer0Type() {
		if(player0Type.getValue().equals("Human")) {
			return PLAYER_TYPE.Human;
		} else if(player0Type.getValue().equals("AI")) {
			return PLAYER_TYPE.AI;
		} else {
			return PLAYER_TYPE.Client;
		}
	}
	
	
	/**
	 * Get the type (Human, AI, or Client) of player 1
	 * @return Returns the player type
	 */
	public PLAYER_TYPE getPlayer1Type() {
		if(player1Type.getValue().equals("Human")) {
			return PLAYER_TYPE.Human;
		} else if(player1Type.getValue().equals("AI")) {
			return PLAYER_TYPE.AI;
		} else {
			return PLAYER_TYPE.Client;
		}
	}
	
	/**
	 * Returns the ID that will be used to reference a thread that's used for Client/Server communication
	 * @return Returns -1 if a thread wasn't created, otherwise returns the thread ID (usually 0 or 1)
	 */
	public int getPlayer0ThreadId() {
		return player0ThreadId;
	}
	
	
	/**
	 * Returns the ID that will be used to reference a thread that's used for Client/Server communication
	 * @return Returns -1 if a thread wasn't created, otherwise returns the thread ID (usually 0 or 1)
	 */
	public int getPlayer1ThreadId() {
		return player1ThreadId;
	}
	
	
	/**
	 * Get the number of player pits that is selected
	 * @return Returns the number of player pits
	 */
	public int getNumPlayerPits() {
		return numPlayerPits.getValue();
	}

	/**
	 * Get the number of stones selected for each pit
	 * @return Returns the number of stones in each pit
	 */
	public int getNumStones() {
		return numStones.getValue();
	}
	
	/**
	 * Get if the Random Stones checkbox is checked
	 * @return Returns true if selected and false otherwise
	 */
	public Boolean isRandomStones() {
		return isRandomStones.isSelected();
	}
	
	
	/**
	 * Lets the Main class know if the OK button is the one that was clicked
	 * @return True if OK was clicked, False otherwise
	 */
	public Boolean isOk() {
		return isOk;
	}
}
