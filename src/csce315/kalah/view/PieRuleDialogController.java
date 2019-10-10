package csce315.kalah.view;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class PieRuleDialogController {

	private Boolean isYes;
	private Stage dialogStage;
	
	/**
	 * initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		isYes = false;
	}
	
	public Boolean isYes() {
		return isYes;
	}
	
	@FXML
	private void handleYes() {
		isYes = true;
		dialogStage.close();
	}
	
	@FXML
	private void handleNo() {
		dialogStage.close();
	}
	
	/**
	 * Set the dialog stage so it can be closed when a button is clicked
	 * @param dialogStage The Stage that was created for this dialog box
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
}
