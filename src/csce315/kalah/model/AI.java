package csce315.kalah.model;

import java.util.ArrayList;
import java.util.Random;

//will contain minimax tree and functions to manage it
public class AI {
	
	private ArrayList<Integer> boardState;
	private int playerId;
	private long timer;
	private MinimaxTree gameTree;
	private int turnNumber = 1;
	/**
	 * Constructor
	 * @param boardState The initial state of the board (an array specifiying how many stones are in each pit)
	 * @param playerId The ID of the AI player (either 0 or 1)
	 * @param timer The number of miliseconds allowed per turn
	 */
	public AI(ArrayList<Integer> inputBoard, int playerId, long timer) {
		boardState = inputBoard;
		this.playerId = playerId;
		gameTree = new MinimaxTree(inputBoard,playerId,turnNumber);
		this.timer = timer;
	}
	
	/**
	 * This tells the AI what the last move was so it can update the board
	 * @param pitIndex The index of the last pit that was played
	 */
	/*public void lastMove(int pitIndex, int playerId) {
		//System.out.println("Before move: ");
		//printBoardState();
		//System.out.println("Move: " + pitIndex + " Player: " + playerId);

		int totalPits = boardState.size();
		int player0EndPit = boardState.size() / 2 - 1;
		int player1EndPit = boardState.size() - 1;
		int player0Min = 0;
		int player0Max = boardState.size() / 2 - 2;
		int player1Min = boardState.size() / 2;
		int player1Max = boardState.size() - 2;
		
		int numStones = boardState.get(pitIndex);
		boardState.set(pitIndex, 0);
		int dropPit = pitIndex;
		while(numStones > 0) {
			dropPit++;
			if((playerId == 0 && dropPit == player1EndPit) || (playerId == 1 && dropPit == player0EndPit)) {
				dropPit++;
			}
			
			if(dropPit == totalPits) {
				dropPit = 0;
			}
			
			boardState.set(dropPit, boardState.get(dropPit) + 1);
			numStones--;
		}
		
		if(playerId == 0 && dropPit < ((boardState.size() / 2) - 1) && boardState.get(dropPit) == 1) {
			// dropped in own pit, check for capture
			int buddyPitIndex = (boardState.size() - 2) - dropPit;
			if(boardState.get(buddyPitIndex) != 0) {
				int numCaptureStones = boardState.get(buddyPitIndex);
				boardState.set(buddyPitIndex, 0);
				numCaptureStones += boardState.get(dropPit);
				boardState.set(dropPit, 0);
				boardState.set(boardState.size() / 2 - 1, boardState.get(boardState.size() / 2 - 1) + numCaptureStones);
			}
			
		}
		if(playerId == 1 && dropPit > ((boardState.size() / 2) - 1) && dropPit < (boardState.size() - 1) && boardState.get(dropPit) == 1) {
			// dropped in own pit, check for capture
			int buddyPitIndex = (boardState.size() - 2) - dropPit;
			if(boardState.get(buddyPitIndex) != 0) {
				int numCaptureStones = boardState.get(buddyPitIndex);
				boardState.set(buddyPitIndex, 0);
				numCaptureStones += boardState.get(dropPit);
				boardState.set(dropPit, 0);
				boardState.set(boardState.size() - 1, boardState.get(boardState.size() - 1) + numCaptureStones);
			}
		}
		
		//System.out.println("After move: ");
		//printBoardState();
		
		//Node newRoot = new Node(null,playerMove,0);
		//gameTree.setRoot(newRoot);
		//gameTree = new MinimaxTree(playerMove,playerId);
		
	}*/
	
	/**
	 * Gets the best move from the minimax tree and returns the index of the pit for that move
	 * @return The index of the pit to play
	 */
	/*public int getMax() {
		// very simple AI just picks a random pit on their side of the board that isn't empty
		int minIndex;
		int maxIndex;
		if(playerId == 0) { // lower pits
			minIndex = 0;
			maxIndex = (boardState.size() / 2) - 1;
		} else { // upper pits
			minIndex = (boardState.size() / 2);
			maxIndex = boardState.size() - 1;
		}
		
		ArrayList<Integer> legalPits = new ArrayList<Integer>(); // array of pits that aren't empty
		for(int i = minIndex; i < maxIndex; i++) {
			if(boardState.get(i) != 0) {
				legalPits.add(i);
			}
		}
		
		Random r = new Random();
		int returnVal = legalPits.get( r.nextInt(legalPits.size()) );
		lastMove(returnVal, playerId);
		return returnVal;
	}*/
	
	public Node maxVal(Node inputNode, Node alpha, Node beta) {
		// if the Node represents an end state in the game
		if(inputNode.getChildren().size() == 0) {
			return inputNode;
		}
		
		Node currentMaxNode = new Node(Double.NEGATIVE_INFINITY);
		ArrayList<Node> listOfChildren;
		listOfChildren = inputNode.getChildren();
		
		// calculate the max value of inputNode from each of the children of
		// input Node recursively call minVal
		for(int i = 0; i < inputNode.getChildren().size(); i++) {
			if(!inputNode.getProducesDouble()) {
				currentMaxNode = currentMaxNode.maxNode(minVal(listOfChildren.get(i),alpha,beta));
			}
			else {
				currentMaxNode = currentMaxNode.maxNode(maxVal(listOfChildren.get(i),alpha,beta));
			}
			if(currentMaxNode.compare(beta) >= 0 ) {
				return currentMaxNode;
			}
			alpha = currentMaxNode.maxNode(alpha);
		}
		return currentMaxNode;
	}

	public Node minVal(Node inputNode, Node alpha, Node beta) {
		// end state of the game
		if(inputNode.getChildren().size() == 0) {
			return inputNode;
		}
		
		Node currentMinNode = new Node(Double.POSITIVE_INFINITY);
		ArrayList<Node> listOfChildren;
		listOfChildren = inputNode.getChildren();
		
		// calculate the min value of inputNode from its children
		for(int i = 0; i < inputNode.getChildren().size(); i++) {
			if(!inputNode.getProducesDouble()) {
				currentMinNode = currentMinNode.minNode(maxVal(listOfChildren.get(i),alpha,beta));
			}
			else {
				currentMinNode = currentMinNode.minNode(minVal(listOfChildren.get(i),alpha,beta));
			}
			if(currentMinNode.compare(alpha) <= 0 ) {
				return currentMinNode;
			}
			beta = currentMinNode.minNode(beta);
		}
		return currentMinNode;
	}

	// function that will call minVal and maxVal to find the value 
	// of the root node in the minimax tree depending on which player moves first
	public Node minimaxDecision() {
		// true will represent if A.I. is making the first move
		Node alpha = new Node(Double.NEGATIVE_INFINITY);
		Node beta = new Node(Double.POSITIVE_INFINITY);
		if(playerId == 0) {
			return maxVal(gameTree.getRoot(),alpha,beta);
		}
		else {
			return minVal(gameTree.getRoot(),alpha,beta);
		}
		
	}
	
	// Minimax returns a node with a Board class and list of moves that got to that board
	// so now we need to take the first move in the list of previous moves and return the integer
	// of pit that corresponds to the move
	public int getBestMove(ArrayList<Integer> inputBoard) {
		gameTree = new MinimaxTree(inputBoard, playerId, turnNumber);
		Node bestNode;
		bestNode = minimaxDecision();
		System.out.println("The utility value is: " + bestNode.getUtilityVal());
		
		Node ancestorOfBest;
		ancestorOfBest = gameTree.getAncestorNode(bestNode);
		System.out.println("The move is: " + ancestorOfBest.getMove());
		++turnNumber;
		
		return ancestorOfBest.getMove();
		
		
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public MinimaxTree getGameTree() {
		return gameTree;
	}
	
	/*private void printBoardState() {
		for(int i = boardState.size() - 1; i > boardState.size() / 2 - 1; i--) {
			System.out.print(boardState.get(i) + " ");
		}
		
		System.out.println("");
		System.out.print("  ");
		
		for(int i = 0; i < boardState.size() / 2; i++) {
			System.out.print(boardState.get(i) + " ");
		}
		
		System.out.println("");
	}*/
}
