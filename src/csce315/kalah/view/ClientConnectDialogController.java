package csce315.kalah.view;

import csce315.kalah.Main.APP_MODE;
import csce315.kalah.model.Client;
import csce315.kalah.model.GameOptions;
import csce315.kalah.model.Player.PLAYER_TYPE;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientConnectDialogController {
	
	@FXML
	private TextField serverAddress;
	@FXML
	private TextField serverPort;
	@FXML
	private Label serverAddressSubtext;
	@FXML
	private ComboBox<String> player0Type;
	@FXML
	private Button okButton;
	
	private Scene previousScene;
	private Stage dialogStage;
	private Client client;
	private GameOptions options;
	
	/**
	 * initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		okButton.setDisable(true);
		player0Type.setValue("Human");
		serverAddressSubtext.setText("");
	}
	
	
	/**
	 * Set the Game Options instance to load the selection options into
	 * @param options The object instance that will hold game options
	 */
	public void setGameOptions(GameOptions options) {
		this.options = options;
	}

	
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	
	/**
	 * Set a Client instance to be used for Client/Server communication
	 * @param client The Client to be used for Client/Server communication
	 */
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setPreviousStage(Scene previousScene) {
		this.previousScene = previousScene;
	}
	
	
	/**
	 * Called when user clicks the OK button
	 */
	@FXML
	private void handleOK() {
		options.setAppMode(APP_MODE.Client);
		options.setPlayer0Type(getPlayer0Type());
		options.setPlayer1Type(PLAYER_TYPE.Server);
		dialogStage.close();
	}
	
	
	@FXML
	private void handleCancel() {
		dialogStage.setScene(previousScene);
	}
	
	@FXML
	private void handleConnect() {
		serverAddressSubtext.setText("Connecting . . .");
		serverAddressSubtext.getStyleClass().clear();
		serverAddressSubtext.getStyleClass().add("connection-attempting");
		client.startThread(serverAddress.getText(), serverPort.getText());
		client.connected().addListener((observable, oldValue, newValue) -> {
			// have to use runLater() or there will be thread issues
			Platform.runLater(() -> {
				serverAddressSubtext.setText("Connected!");
				serverAddressSubtext.getStyleClass().clear();
				serverAddressSubtext.getStyleClass().add("connection-successful");
				okButton.setDisable(false);
			});
		});
	}
	
	/**
	 * Get the type (Human, AI) of player 0
	 * @return Returns the player type
	 */
	public PLAYER_TYPE getPlayer0Type() {
		if(player0Type.getValue().equals("Human")) {
			return PLAYER_TYPE.Human;
		} else {
			return PLAYER_TYPE.AI;
		}
	}
}
