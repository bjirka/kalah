package csce315.kalah.view;

import java.util.ArrayList;

import csce315.kalah.Main;
import csce315.kalah.model.Game;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class BoardViewController {

	@FXML
	private TextArea textArea;
	
	private ArrayList<Shape> pits;	// this will hold Circles (player pits) and Rectangles (end pits) 
	private ArrayList<Label> labels;
	private ArrayList<Label> plus12;
	private ArrayList<ImageView> pitGlows;
	private Rectangle infoBar0;
	private Label infoBar0Message;
	private Label infoBar0Clock;
	private Rectangle infoBar1;
	private Label infoBar1Message;
	private Label infoBar1Clock;
	private Label winnerMessage;
	private Label generalMessage;
	
	private Game game;
	
	private AnchorPane boardLayout;
	
	/**
	 * The MouseClick event handler for the pits
	 */
	EventHandler<MouseEvent> pitClicked = new EventHandler<MouseEvent>() {
		public void handle(MouseEvent event) {
			// turn off all onclick events
			disablePits();
			// get the id of the element that was clicked
			String elementId = event.getPickResult().getIntersectedNode().getId();
			// then parse the int from the id
			int elementIndex = Integer.parseInt(elementId.substring(3));
			// pass that to the Game class to execute the move
			game.humanTurn(elementIndex);
		}
	};
	
	
	public BoardViewController(Game game, int numPlayerPits) {
		int top = 30;
		this.game = game;
		boardLayout = new AnchorPane();
		boardLayout.getStylesheets().add("csce315/kalah/view/BoardView.css");
		
		int rootWidth = 1150;
		if(numPlayerPits > 6) rootWidth = 1450;
		Main.getPrimaryStage().setWidth(rootWidth);
		int totalBoardWidth = 583 + (128 * (numPlayerPits - 2)); // 583 is combined width of ends, 128 is width of each middle piece (-2 because 1 player pit is contained in each of the end pieces)
		int leftX = (int) Math.ceil((rootWidth - totalBoardWidth) / 2);
		int currX = leftX;

		// add bottom info bar
		infoBar0 = new Rectangle(currX + (totalBoardWidth / 2) - 250, top + 147, 500, 200);
		infoBar0.setArcWidth(50);
		infoBar0.setArcHeight(50);
		infoBar0.setFill(Color.rgb(0, 0, 0, 0.75));
		boardLayout.getChildren().add(infoBar0);
		
		infoBar0Message = new Label("Waiting For Blue Player");
		infoBar0Message.relocate(currX + (totalBoardWidth / 2) - 240, top + 303);
		infoBar0Message.translateYProperty().bind(infoBar0.translateYProperty());
		infoBar0Message.getStyleClass().add("info-bar-label");
		boardLayout.getChildren().add(infoBar0Message);
		
		infoBar0Clock = new Label("");
		infoBar0Clock.relocate(currX + (totalBoardWidth / 2) + 140, top + 303);
		infoBar0Clock.translateYProperty().bind(infoBar0.translateYProperty());
		infoBar0Clock.setPrefWidth(100);
		infoBar0Clock.setAlignment(Pos.CENTER_RIGHT);
		infoBar0Clock.getStyleClass().add("info-bar-label");
		boardLayout.getChildren().add(infoBar0Clock);
		
		
		// add top info bar
		infoBar1 = new Rectangle(currX + (totalBoardWidth / 2) - 250, top + 20, 500, 193);
		infoBar1.setArcWidth(50);
		infoBar1.setArcHeight(50);
		infoBar1.setFill(Color.rgb(0, 0, 0, 0.75));
		boardLayout.getChildren().add(infoBar1);
		
		infoBar1Message = new Label("Waiting For Blue Player");
		infoBar1Message.relocate(currX + (totalBoardWidth / 2) - 240, top + 21);
		infoBar1Message.translateYProperty().bind(infoBar1.translateYProperty());
		infoBar1Message.getStyleClass().add("info-bar-label");
		boardLayout.getChildren().add(infoBar1Message);
		
		infoBar1Clock = new Label("");
		infoBar1Clock.relocate(currX + (totalBoardWidth / 2) + 140, top + 21);
		infoBar1Clock.translateYProperty().bind(infoBar1.translateYProperty());
		infoBar1Clock.setPrefWidth(100);
		infoBar1Clock.setAlignment(Pos.CENTER_RIGHT);
		infoBar1Clock.getStyleClass().add("info-bar-label");
		boardLayout.getChildren().add(infoBar1Clock);
		
		// add left background piece which includes left end pit and 1 set of player pits
		ImageView bg_left = new ImageView(new Image("csce315/kalah/view/images/board_bg_left.png"));
		bg_left.setFitWidth(288);
		bg_left.setFitHeight(367);
		bg_left.relocate(currX, top + 0);
		currX += 288;
		boardLayout.getChildren().add(bg_left);
		
		// add the needed player pits in the middle
		for(int i = 0; i < (numPlayerPits - 2); i++) {
			ImageView bg_pit = new ImageView(new Image("csce315/kalah/view/images/board_bg_pit.png"));
			bg_pit.setFitWidth(128);
			bg_pit.setFitHeight(367);
			bg_pit.relocate(currX, top + 0);
			currX += 128;
			boardLayout.getChildren().add(bg_pit);
		}
		
		// add right background piece which includes right end pit and 1 set of player pits
		ImageView bg_right = new ImageView(new Image("csce315/kalah/view/images/board_bg_right.png"));
		bg_right.setFitWidth(295);
		bg_right.setFitHeight(367);
		bg_right.relocate(currX, top + 0);
		boardLayout.getChildren().add(bg_right);
		
		pits = new ArrayList<Shape>();
		//plus1 = new ArrayList<ImageView>();
		plus12 = new ArrayList<Label>();
		pitGlows = new ArrayList<ImageView>();
		
		// x position of the center of the first pit
		currX = 224 + leftX;
		int pitIdNumber = 0;
		
		// add lower circles for pits
		for(int i = 0; i < numPlayerPits; i++) {
			Circle pit = new Circle(currX, top + 258, 56, Color.rgb(0, 0, 0, 0));
			pit.setId("pit"+pitIdNumber);
			pitIdNumber++;
			pits.add(pit);
			boardLayout.getChildren().add(pit);
			
			// add the +1 graphic that will display over the pit when a stone is added
			Label plusOneLabel = new Label("+1");
			plusOneLabel.setPrefWidth(112);
			plusOneLabel.setPrefHeight(112);
			plusOneLabel.setAlignment(Pos.CENTER);
			plusOneLabel.relocate(currX - 56, top + 202);
			plusOneLabel.getStyleClass().add("pit-plus-one");
			plusOneLabel.setVisible(false);
			plus12.add(plusOneLabel);
			boardLayout.getChildren().add(plusOneLabel);
			
			
			// add the glow graphic that will display over the pit when it's selected
			ImageView pitGlow = new ImageView(new Image("csce315/kalah/view/images/pitGlow.png"));
			pitGlow.setFitWidth(150);
			pitGlow.setFitHeight(150);
			pitGlow.relocate(currX - 76, top + 182);
			pitGlow.setVisible(false);
			pitGlows.add(pitGlow);
			boardLayout.getChildren().add(pitGlow);

			currX += 128;
		}
		
		
		// add right end pit
		currX -= 56;
		Rectangle endPit = new Rectangle(currX, top + 121, 113, 193);
		endPit.setId("pit"+pitIdNumber);
		pitIdNumber++;
		endPit.setFill(Color.rgb(0, 0, 0, 0));
		pits.add(endPit);
		boardLayout.getChildren().add(endPit);
		
		// add the +1 graphic to end pit
		{ // just to make local variables
			
			Label plusOneLabel = new Label("+88");
			//plusOneLabel.setBackground(new Background(new BackgroundFill(Color.BLUE,null, null)));
			plusOneLabel.setPrefWidth(122);
			plusOneLabel.setPrefHeight(112);
			plusOneLabel.setAlignment(Pos.CENTER);
			plusOneLabel.relocate(currX-8, top + 162);
			plusOneLabel.getStyleClass().add("pit-plus-one");
			plusOneLabel.setVisible(false);
			plus12.add(plusOneLabel);
			boardLayout.getChildren().add(plusOneLabel);
			
			// add to pitGlows even though it'll never be displayed just to keep the indexes correct in pitGlows
			pitGlows.add(new ImageView());
		}
		
		currX -= 72;
		
		// add upper circles for pits
		for(int i = 0; i < numPlayerPits; i++) {
			Circle pit = new Circle(currX, top + 101, 56, Color.rgb(0, 0, 0, 0));
			pit.setId("pit"+pitIdNumber);
			pitIdNumber++;
			pits.add(pit);
			boardLayout.getChildren().add(pit);
			
			// add the +1 graphic that will display over the pit when a stone is added
			Label plusOneLabel = new Label("+1");
			plusOneLabel.setPrefWidth(112);
			plusOneLabel.setPrefHeight(112);
			plusOneLabel.setAlignment(Pos.CENTER);
			plusOneLabel.relocate(currX - 56, top + 45);
			plusOneLabel.getStyleClass().add("pit-plus-one");
			plusOneLabel.setVisible(false);
			plus12.add(plusOneLabel);
			boardLayout.getChildren().add(plusOneLabel);
			
			// add the glow graphic that will display over the pit when it's selected
			ImageView pitGlow = new ImageView(new Image("csce315/kalah/view/images/pitGlow.png"));
			pitGlow.setFitWidth(150);
			pitGlow.setFitHeight(150);
			pitGlow.relocate(currX - 76, top + 25);
			pitGlow.setVisible(false);
			pitGlows.add(pitGlow);
			boardLayout.getChildren().add(pitGlow);
			
			currX -= 128;
		}
		
		// add left end pit
		currX -= 56;
		endPit = new Rectangle(currX, top + 45, 113, 193);
		endPit.setId("pit"+pitIdNumber);
		pitIdNumber++;
		endPit.setFill(Color.rgb(0, 0, 0, 0));
		pits.add(endPit);
		boardLayout.getChildren().add(endPit);
		
		// add the +1 graphic to end pit
		{ // just to make local variables
		
			Label plusOneLabel = new Label("+88");
			plusOneLabel.setPrefWidth(122);
			plusOneLabel.setPrefHeight(112);
			plusOneLabel.setAlignment(Pos.CENTER);
			plusOneLabel.relocate(currX-8, top + 85);
			plusOneLabel.getStyleClass().add("pit-plus-one");
			plusOneLabel.setVisible(false);
			plus12.add(plusOneLabel);
			boardLayout.getChildren().add(plusOneLabel);
			
			// add to pitGlows even though it'll never be displayed just to keep the indexes correct in pitGlows
			pitGlows.add(new ImageView());
		}
		
		//boardLayout.getChildren().addAll(pits);
		
		
		// add the labels to the board
		labels = new ArrayList<Label>();
		
		currX = 255 + leftX;
		// add lower pit labels
		for(int i = 0; i < numPlayerPits; i++) {
			Label pitLabel = new Label("0");
			pitLabel.relocate(currX, top + 298);
			pitLabel.getStyleClass().add("player-pit-label");
			labels.add(pitLabel);
			currX += 128;
		}
		
		currX -= 44;
		
		// add right end pit label
		Label endPitLabel = new Label("0");
		endPitLabel.relocate(currX, top + 60);
		endPitLabel.getStyleClass().add("end-pit-label");
		labels.add(endPitLabel);
		
		currX -= 84;
		
		// add upper pit labels
		for(int i = 0; i < numPlayerPits; i++) {
			Label pitLabel = new Label("0");
			pitLabel.relocate(currX, top + 24);
			pitLabel.getStyleClass().add("player-pit-label");
			labels.add(pitLabel);
			currX -= 128;
		}
		
		currX -= 44;
		// add left end pit label
		endPitLabel = new Label("0");
		endPitLabel.relocate(currX, top + 245);
		endPitLabel.getStyleClass().add("end-pit-label");
		labels.add(endPitLabel);
		
		boardLayout.getChildren().addAll(labels);
		
		currX = (int) Math.ceil((rootWidth - 400) / 2);
		generalMessage = new Label("RED PLAYER WINS!");
		generalMessage.setBackground(new Background(new BackgroundFill(Color.BLACK,null, null)));
		generalMessage.setPrefWidth(400);
		generalMessage.setPrefHeight(75);
		generalMessage.setAlignment(Pos.CENTER);
		generalMessage.relocate(currX, top + 145);
		generalMessage.getStyleClass().add("general-message");
		generalMessage.setVisible(false);
		boardLayout.getChildren().add(generalMessage);
		
		currX = (int) Math.ceil((rootWidth - 800) / 2);
		winnerMessage = new Label("RED PLAYER WINS!");
		//winnerMessage.setBackground(new Background(new BackgroundFill(Color.BLUE,null, null)));
		winnerMessage.setPrefWidth(800);
		winnerMessage.setPrefHeight(112);
		winnerMessage.setAlignment(Pos.CENTER);
		winnerMessage.relocate(currX, top + 400);
		winnerMessage.getStyleClass().add("winner-message");
		winnerMessage.setVisible(false);
		boardLayout.getChildren().add(winnerMessage);
	
		textArea = new TextArea();
		textArea.setPrefHeight(100);
		textArea.setEditable(false);
		textArea.setVisible(false);
		boardLayout.getChildren().add(textArea);
		AnchorPane.setBottomAnchor(textArea, 0.0);
		AnchorPane.setLeftAnchor(textArea, 0.0);
		AnchorPane.setRightAnchor(textArea, 0.0);
		
	}
	
	public void showInfoBar(int playerId, String message) {
		if(playerId == 0) {
			infoBar0Message.setText(message);
			if(Main.getOptions().getTimeLimit() != 0) {
				//infoBar0Clock.setText("0:" + (Main.getOptions().getTimeLimit()));
			}
			double newY = 45;
			if(infoBar0.getTranslateY() > 0) return;
			TranslateTransition move = new TranslateTransition();
			move.setNode(infoBar0);
			move.setToY(newY);
			move.setDuration(Duration.millis(400));
			move.playFromStart();
		} else {
			infoBar1Message.setText(message);
			if(Main.getOptions().getTimeLimit() != 0) {
				//infoBar1Clock.setText("0:" + (Main.getOptions().getTimeLimit()));
			}
			double newY = -45;
			if(infoBar1.getTranslateY() < 0) return;
			TranslateTransition move = new TranslateTransition();
			move.setNode(infoBar1);
			move.setToY(newY);
			move.setDuration(Duration.millis(400));
			move.playFromStart();
		}
	}
	
	public void hideInfoBar(int playerId) {
		if(playerId == 0) {
			double newY = 0;
			if(infoBar0.getTranslateY() == 0) return;
			TranslateTransition move = new TranslateTransition();
			move.setNode(infoBar0);
			move.setToY(newY);
			move.setDuration(Duration.millis(400));
			move.playFromStart();
		} else {
			double newY = 0;
			if(infoBar1.getTranslateY() == 0) return;
			TranslateTransition move = new TranslateTransition();
			move.setNode(infoBar1);
			move.setToY(newY);
			move.setDuration(Duration.millis(400));
			move.playFromStart();
		}
	}
	
	/**
	 * Returns the clock label of the current player so the game can update it
	 * @param playerId The ID of the player who's turn timer needs to be updated
	 * @return The label to update with the current time remaining
	 */
	public Label getClockLabel(int playerId) {
		if(playerId == 0) {
			return infoBar0Clock;
		} else {
			return infoBar1Clock;
		}
	}
	
	
	public void winnerMessage(String message) {
		winnerMessage.setText(message);
		winnerMessage.setVisible(true);
	}
	
	public void generalMessage(String message, int duration) {
		Platform.runLater(() -> {
			generalMessage.setText(message);
			generalMessage.setVisible(true);
			generalMessage.setOpacity(1);
			
			PauseTransition pause = new PauseTransition(Duration.seconds(duration));
			pause.setOnFinished(e -> {
				FadeTransition fade = new FadeTransition();
				fade.setNode(generalMessage);
				fade.setFromValue(1);
				fade.setToValue(0);
				fade.setDuration(Duration.millis(400));
				fade.onFinishedProperty().set((actionEvent) -> {
					generalMessage.setVisible(false);
				});
				fade.play();
			});
			pause.play();
		});
		
	}
	
	public AnchorPane getBoardLayout() {
		return boardLayout;
	}
	
	
	/**
	 * initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded. Stores all the individual shapes and labels in arrays
	 * to make it easier to work with them.
	 */
	@FXML
	private void initialize() {
		//this isn't called anymore
	}
	
	
	/**
	 * Enable the onClick events for the given player's 6 pits
	 * @param playerId The player ID (either 0 or 1) for the pits to enable
	 */
	public void enablePits(int playerId) {
		int numPits = pits.size();
		if(playerId == 0) {
			for(int i = 0; i < ((numPits / 2) - 1); i++) {
				pits.get(i).addEventHandler(MouseEvent.MOUSE_CLICKED, pitClicked);
			}
		} else {
			for(int i = (numPits / 2); i < (numPits - 1); i++) {
				pits.get(i).addEventHandler(MouseEvent.MOUSE_CLICKED, pitClicked);
			}
		}
	}
	
	
	/**
	 * Turn off onClick events for all pits
	 */
	public void disablePits() {
		int numPits = pits.size();
		for(int i = 0; i < (numPits - 1); i++) {
			pits.get(i).removeEventHandler(MouseEvent.MOUSE_CLICKED, pitClicked);
		}
	}
	
	
	/**
	 * Append a test message to the textarea in the lower half of the GUI
	 * @param text The string to append to the bottom of the textarea
	 */
	public void appendText(String text) {
		textArea.appendText(text);
	}
	
	/**
	 * Set the test message to the textarea in the lower half of the GUI
	 * @param text The string to set the bottom of the textarea to
	 */
	public void addText(String text) {
		textArea.setText(text);
	}
	
	
	/**
	 * This gives the controller access to the Game object so it can call Game methods
	 * @param game The instance of the Game object that is created in Main
	 */
	public void setGame(Game game) {
		this.game = game;
	}
	
	
	/**
	 * A two argument version of the function below to make the third argument optional
	 * @param pitIndex The index of the pit to update
	 * @param newCount The number of stones in the pit
	 */
	public void updatePitDisplay(int pitIndex, int newCount) {
		updatePitDisplay(pitIndex, newCount, true);
	}
	/**
	 * Update the label and pit image to accurately reflect the number of stones in the given pit
	 * @param pitIndex The pit index of the pit to update
	 * @param newCount The number of stones in the pit
	 * @param showAnimation Normally only set to false when board is first initialized, doesn't show animation
	 */
	public void updatePitDisplay(int pitIndex, int newCount, boolean showAnimation) {
		if(newCount < 0) return;

		int oldCount = Integer.parseInt(labels.get(pitIndex).getText());
		int change = newCount - oldCount;
		int duration = 400;
		if(change > 1) {
			duration = 1500;
		}
		labels.get(pitIndex).setText("" + newCount);
		
		if(newCount > 0 && showAnimation) { // the pit that was clicked on will have newCount == 0, don't display +1 for that pit
			plus12.get(pitIndex).setText("+" + change);
			plus12.get(pitIndex).setVisible(true);
		
			FadeTransition fade = new FadeTransition();
			fade.setNode(plus12.get(pitIndex));
			fade.setFromValue(1);
			fade.setToValue(0);
			fade.setDuration(Duration.millis(duration));
			fade.onFinishedProperty().set((actionEvent) -> {
				plus12.get(pitIndex).setVisible(false);
			});
			fade.play();
		}
		
		if(newCount == 0) {
			pits.get(pitIndex).setFill(null);
		} else {
			int imageCount = newCount;
			
			if(pitIndex == (pits.size() / 2 - 1) || pitIndex == (pits.size() - 1)) { // if end pit
				// the images only show up to 20 stones, so anything above that will just show 20
				if(newCount > 20) {
					imageCount = 20;
				}
				
				pits.get(pitIndex).setFill(new ImagePattern(new Image("csce315/kalah/view/images/end_stones_" + imageCount + ".png", false)));
			} else { // else not end pit
				// the images only show up to 10 stones, so anything above that will just show 10
				if(newCount > 10) {
					imageCount = 10;
				}
				
				pits.get(pitIndex).setFill(new ImagePattern(new Image("csce315/kalah/view/images/stones_" + imageCount + ".png", false)));
			}
		}
	}
	
	
	/**
	 * Shows the glow around the given pit, then fades out
	 * @param pitIndex The index of the pit to show the glow for
	 */
	public void showPitGlow(int pitIndex) {
		pitGlows.get(pitIndex).setVisible(true);
		
		FadeTransition fade = new FadeTransition();
		fade.setNode(pitGlows.get(pitIndex));
		fade.setFromValue(1);
		fade.setToValue(0);
		fade.setDuration(Duration.millis(1500));
		fade.onFinishedProperty().set((actionEvent) -> {
			pitGlows.get(pitIndex).setVisible(false);
		});
		fade.play();
	}

}
