package csce315.kalah.view;

import csce315.kalah.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RootViewController {
	
	private Main mainApp;

	/**
	 * initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {

	}
	
	@FXML
	private void menuCloseClicked() {
		Platform.exit();
	}
	
	@FXML
	private void menuNewClicked() {
		mainApp.showOptionsDialog(mainApp.getOptions(), mainApp.getServer(), mainApp.getClient());
	}
	
	public void setMain(Main main) {
		this.mainApp = main;
	}
}
