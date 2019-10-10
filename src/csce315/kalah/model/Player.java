package csce315.kalah.model;

import java.io.IOException;
import java.util.ArrayList;

import csce315.kalah.Main;
import csce315.kalah.view.PieRuleDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Player {
	
	public enum PLAYER_TYPE {AI, Human, Client, Server};
	private PLAYER_TYPE player_type;
	private AI ai;
	
	/**
	 * Constructor that sets type of Player this will be 
	 * @param type Determines the type of player, either AI or Human
	 */
	public Player(PLAYER_TYPE type, AI ai) {
		player_type = type;
		this.ai = ai;
	}
	
	
	public PLAYER_TYPE getPlayerType() {
		return player_type;
	}
	
	/**
	 * Gets a move from this player, either from the minimax tree if AI or from a GUI click if Human
	 * (or from client/server interaction) 
	 * @return Returns the index of the pit that was played,
	 * 					or -1 if waiting on a human to click a pit,
	 * 					or -2 if the Pie Rule option was exercised,
	 * 					or -3 if waiting on a client machine to send a move,
	 * 					or -4 if waiting on the server machine to send a move
	 */
	public int getMove(Boolean pieOption, ArrayList<Integer> inputBoard) {
		/*
		 * Get move from the current human
		 */
		if(player_type == PLAYER_TYPE.Human) {
			
			if(pieOption) {
				if(showPieOption()) {
					return -2;
				}
			}
			// just return and the move from the Human will trigger an event
			return -1;
		}
		/*
		 * Get move from the AI
		 */
		else if(player_type == PLAYER_TYPE.AI){
			/*
			 * Return number from 1-6
			 * (Get move from min-max tree)
			 */
			//return this.ai.getMax();
			return this.ai.getBestMove(inputBoard);
		}
		/*
		 * Get the move from the Client
		 */
		else if(player_type == PLAYER_TYPE.Client) {
			// just return and the move from the client will trigger an event
			return -3;
		}
		/*
		 * Get the move from the Client
		 */
		else if(player_type == PLAYER_TYPE.Server) {
			// just return and the move from the server will trigger an event
			return -4;
		}
		return 0;
	}
	
	
	/**
	 * Return a reference to this player's AI object
	 * @return This player's AI object
	 */
	public AI getAI() {
		return ai;
	}
	
	private Boolean showPieOption() {
	    try {
	        // Load the fxml file and create a new stage for the popup dialog.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Main.class.getResource("view/PieRuleDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        // Create the dialog Stage.
	        Stage dialogStage = new Stage();
	        dialogStage.setResizable(false);
	        dialogStage.initStyle(StageStyle.UNDECORATED);
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        
	        dialogStage.initOwner(Main.getPrimaryStage());
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        // Set the person into the controller.
	        PieRuleDialogController controller = loader.getController();
	        controller.setDialogStage(dialogStage);

	        // Show the dialog and wait until the user closes it
	        dialogStage.showAndWait();

	        return controller.isYes();
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
}
