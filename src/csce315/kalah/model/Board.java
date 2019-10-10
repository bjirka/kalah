package csce315.kalah.model;

import java.util.ArrayList;
import csce315.kalah.model.Pit.PIT_TYPE;
import csce315.kalah.view.BoardViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class Board {
	
	private ArrayList<Pit> pits;
	private BoardViewController boardController;
	private Game game;
	
	private int numPlayerPits; // number of pits per side (not counting end pits) (range: 4-9)
	
	
	/**
	 * Default constructor sets up the board with 4 stones in each player pit
	 */
	public Board(Game game, ArrayList<Integer> initialBoardState) {
		this.game = game;
		this.numPlayerPits = initialBoardState.size();
		
		boardController = new BoardViewController(game, numPlayerPits);

		// create array of pits with 'numStones' initial stones
		pits = new ArrayList<Pit>();
		for(int i = 0; i < ((numPlayerPits +1) * 2); i++) {
			int owner = 0;	// the first half are owned by player 0
			if(i > numPlayerPits) owner = 1;	// the second half are owned by player 1
			// all are player pits except for #6 and #13
			if(i != numPlayerPits && i != ((numPlayerPits * 2) + 1)) {
				int stoneIndex = i; // the index within the initialStones array
				if(i > numPlayerPits) stoneIndex = (i - (numPlayerPits + 1));
				pits.add(new Pit(initialBoardState.get(stoneIndex), PIT_TYPE.PLAYER_PIT, owner));
				// update the pit image and label
				boardController.updatePitDisplay(i, initialBoardState.get(stoneIndex), false);
			}else {
				// end pits
				pits.add(new Pit(0, PIT_TYPE.END_PIT, owner));
			}
		}
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getBoardState(){
		ArrayList<Integer> boardState = new ArrayList<Integer>();
		
		for(int i = 0; i < pits.size(); i++) {
			boardState.add(pits.get(i).count());
		}
		
		return boardState;
	}
	
	
	/**
	 * 
	 * @param playerId
	 */
	public void enablePits(int playerId) {
		boardController.enablePits(playerId);		
	}
	
	
	/**
	 * 
	 * @param text
	 */
	public void addText(String text) {
		boardController.addText(text);
	}
	
	
	/**
	 * 
	 * @param pitIndex
	 * @param newCount
	 */
	public void updatePitDisplay(int pitIndex, int newCount) {
		boardController.updatePitDisplay(pitIndex, newCount);
	}

	
	/**
	 * This is called when Pie Rule is invoked, it will flip board
	 */
	public void flipBoard() {
		ArrayList<Integer> redPitsStones = new ArrayList<Integer>();
		ArrayList<Integer> bluePitsStones = new ArrayList<Integer>();
		
		// loop through the pits and store the current pit counts
		for(int i = 0; i < pits.size(); i++) {
			if(i < (pits.size() / 2)) {
				redPitsStones.add(pits.get(i).count());
			} else {
				bluePitsStones.add(pits.get(i).count());
			}
		}
		
		// loop through the pits and update the pit counts
		for(int i = 0; i < pits.size(); i++) {
			if(i < (pits.size() / 2)) {
				pits.get(i).removeAll();
				pits.get(i).addStones(bluePitsStones.get(i));
				boardController.updatePitDisplay(i, bluePitsStones.get(i), false);
			} else {
				pits.get(i).removeAll();
				pits.get(i).addStones(redPitsStones.get(i - (pits.size() / 2)));
				boardController.updatePitDisplay(i, redPitsStones.get(i - (pits.size() / 2)), false);
			}
		}
	}
	
	/**
	 * Remove the stones from the given pit and add 1 stone to each successive pit until
	 * the stones run out and excluding the oppenent's end pit
	 * @param pitIndex The index of the pit who's stones are to be distributed
	 * @return Returns The index of the pit that the final stone was placed in 
	 */
	public int distributeStones(int pitIndex) {
		// figure out which player made the move so we know which end pit to skip
		int playerIndex = 0;
		if(pitIndex > numPlayerPits) playerIndex = 1;
		// remove all the stones from the selected pit and store how many stones it had
		int stoneCount = pits.get(pitIndex).removeAll();
		// update the display to show 0 stones for that pit
		boardController.updatePitDisplay(pitIndex, 0);
		
		// loop through each successive pit dropping 1 stone until all the stones have been distributed
		int newIndex = pitIndex + 1;
		int keyFrame = 1;
		Timeline timeline = new Timeline();
		
		boardController.showPitGlow(pitIndex);
		
		while(stoneCount > 0) {
			// for player 0, skip pit 13
			if(playerIndex == 0 && newIndex == ((numPlayerPits * 2) + 1)) newIndex = 0;
			// for player 1, skip pit 6
			if(playerIndex == 1 && newIndex == (numPlayerPits)) newIndex = (numPlayerPits + 1);
			// check if we need to wrap around
			if(newIndex == ((numPlayerPits + 1) * 2)) newIndex = 0;
			// add the stone and get the number of stones the pit now contains
			int newCount = pits.get(newIndex).addStone();
			int newIndex2 = newIndex;
			// update the display to show the new number of stones
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(keyFrame), e -> boardController.updatePitDisplay(newIndex2, newCount)));
			
			stoneCount--;
			newIndex++;
			keyFrame += 100;
		}
		
		newIndex--;
		
		final int lastStoneIndex = newIndex;
		
		timeline.setOnFinished(e -> {
			Platform.runLater(() -> {
				game.makeMove2(pitIndex, lastStoneIndex);
			});
		});
		timeline.play();
		
		return newIndex;
	}
	
	
	/**
	 * Returns the index of the pit that is directly across from the given pit
	 * @param pitIndex The index of the pit who's buddy will be returned
	 * @return The index of the pit that is directly across from the given pit
	 */
	public int getBuddyPit(int pitIndex) {
		// returns the pit across from a given pit
		return (numPlayerPits * 2) - pitIndex;
	}

	
	/**
	 * Returns the index of the end pit for the given player
	 * @param playerIndex The index of the player
	 * @return The index of the end pit for the given player
	 */
	public int getEndPit(int playerIndex) {
		if(playerIndex == 0) return numPlayerPits;
		else return ((numPlayerPits * 2) + 1);
	}

	
	/**
	 * Returns a reference to the given pit
	 * @param pitIndex The index of the pit to return
	 * @return A reference to the given pit 
	 */
	public Pit getPit(int pitIndex) {
		// return a Pit object from the array of pits
		return pits.get(pitIndex);
	}
	
	
	/**
	 * Return an array of indexes of the pits (including the end pit) for the given user
	 * @param playerIndex The index of the user who's pits are to be returned
	 * @return An ArrayList of the pit indexes that belong to the given user
	 */
	public ArrayList<Integer> getPlayerPits(int playerIndex) {
		ArrayList<Integer> playerPits = new ArrayList<Integer>();
		if(playerIndex == 0) {
			for(int i = 0; i < (numPlayerPits + 1); i++) { // add pits 0-6
				playerPits.add(i);
			}
		} else {
			for(int i = (numPlayerPits + 1); i < ((numPlayerPits + 1) * 2); i++) { // add pits 7-13
				playerPits.add(i);
			}
		}
		return playerPits;
	}
	
	/**
	 * Return the pits array
	 * @return the pits array held in the Board
	 */
	public ArrayList<Pit> getPits(){
		return pits;
	}
	
	public AnchorPane getBoardLayout() {
		return boardController.getBoardLayout();
	}
	
	public void showInfoBar(int playerId, String message) {
		boardController.showInfoBar(playerId, message);
	}
	
	public void hideInfoBar(int playerId) {
		boardController.hideInfoBar(playerId);
	}
	
	public Label getClockLabel(int playerId) {
		return boardController.getClockLabel(playerId);
	}
	
	public void winnerMessage(String message) {
		boardController.winnerMessage(message);
	}
	
	public void generalMessage(String message) {
		boardController.generalMessage(message, 1);
	}
	
	public void generalMessage(String message, int duration) {
		boardController.generalMessage(message, duration);
	}
}