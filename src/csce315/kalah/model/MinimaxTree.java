package csce315.kalah.model;
import java.util.ArrayList;
public class MinimaxTree {
	Node root;
	int maxLookAhead = 6;
	int depth = 0;
	int aiIndex;
	int turnNumber;
	
	public MinimaxTree() {
		root = null;
	}
	
	public MinimaxTree(ArrayList<Integer> inputBoard, int aiIndex, int turn) {
		root = new Node(inputBoard,0);
		this.aiIndex = aiIndex;
		createTree(root,0, aiIndex);
		turnNumber = turn;
	}
	
	
	/**
	 *  function to create the minimax tree based on the first board state 
	 * @param startNode
	 * @param inputBoard
	 * @param lookahead
	 */
	public void createTree(Node startNode, int lookahead, int player) {
		//System.out.println(lookahead);
		if(lookahead < maxLookAhead) {
			// calculate all different board states of the input board state -> nextMoves
			ArrayList<ArrayList<Integer>> possibleMoves = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> listOfMoves = new ArrayList<Integer>();
			ArrayList<Boolean> doubleMoves = new ArrayList<Boolean>();
			if(player == 0) {
				for(int i = 0; i < (startNode.getBoard().size() / 2) - 1; i++) {
					if(startNode.getBoard().get(i) != 0) {
						ArrayList<Integer> temp = new ArrayList<Integer>(startNode.getBoard());
						temp = futureMove(i,temp, player);
						possibleMoves.add(temp);
						listOfMoves.add(i);
					}
					if(startNode.getBoard().get(i) == ((startNode.getBoard().size() / 2) - 1) - i){
						doubleMoves.add(true);
					}
					else {
						doubleMoves.add(false);
					}
					
				}
			}
			else {
				for(int i = startNode.getBoard().size() / 2; i < startNode.getBoard().size()-1; i++) {
					if(startNode.getBoard().get(i) != 0) {
						ArrayList<Integer> temp = new ArrayList<Integer>(startNode.getBoard());
						//temp = futureMove(i,temp);
						temp = futureMove(i,temp, player);
						possibleMoves.add(temp);
						listOfMoves.add(i);	
					}
					if(startNode.getBoard().get(i) == (startNode.getBoard().size() - 1) - i) {
						doubleMoves.add(true);
					}
					else {
						doubleMoves.add(false);
					}
					
				}
				/*
				 * Check if first AI turn
				 * If so, add a -1 board to tree
				 */
				if(turnNumber == 1) {
					ArrayList<Integer> temp = new ArrayList<Integer>(startNode.getBoard());
					//temp = futureMove(i,temp);
					temp = futureMove(-1,temp, player);
					possibleMoves.add(temp);
				}
			}
			//System.out.println("The number of possible Moves: " + possibleMoves.size());
			// for move in possibleMoves
			for(int i = 0; i < possibleMoves.size(); i++) {
				// create node with move -> currentNode with parent startNode
				Node currentNode = new Node();
				
				currentNode.setBoard(possibleMoves.get(i));
				currentNode.setMove(listOfMoves.get(i));
				currentNode.setProducesDouble(doubleMoves.get(i));
				currentNode.addParent(startNode);
				// add move to the children of startNode
				startNode.addChild(currentNode);
				
				// createTree(currentNode,count + 1)
				if(currentNode.getProducesDouble() == true) {
					createTree(currentNode,lookahead + 1, player);
				}
				else {
					createTree(currentNode, lookahead + 1, 1 - player);
				}
			}
		}
		else {
			// give a utility value to the node and set to terminal
			if(aiIndex == 0) {
				startNode.setUtilityVal(calculateUtilityValue(startNode));
				startNode.setToTerminal();
			}
			else {
				//System.out.println("Calculating utility");
				startNode.setUtilityVal(calculateUtilityValue(startNode));
				//System.out.println(startNode.getUtilityVal());
				startNode.setToTerminal();
			}
		}
	}
	
	public void setRoot(Node newRoot) {
		root = newRoot;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public ArrayList<Integer> futureMove(int pitIndex, ArrayList<Integer> inputBoard, int player) {
		
		// remove all the stones from the selected pit and store how many stones it had
		
		if(pitIndex == -1) {
			ArrayList<Integer> redPitsStones = new ArrayList<Integer>();
			ArrayList<Integer> bluePitsStones = new ArrayList<Integer>();
			
			// loop through the pits and store the current pit counts
			for(int i = 0; i < inputBoard.size(); i++) {
				if(i < (inputBoard.size() / 2)) {
					redPitsStones.add(inputBoard.get(i));
				} else {
					bluePitsStones.add(inputBoard.get(i));
				}
			}
			int size = inputBoard.size();
			inputBoard.clear();
			for(int i = 0; i < size; i++) {
				if(i < (size / 2)) {
					inputBoard.add(redPitsStones.get(i));
				} else {
					inputBoard.add(bluePitsStones.get(i));
				}
			}
			return inputBoard;
		}
		int stoneCount = inputBoard.get(pitIndex);
		inputBoard.set(pitIndex,0);
		// loop through each successive pit dropping 1 stone until all the stones have been distributed
		int newIndex = pitIndex + 1;
		int dropPit= pitIndex;
		while(stoneCount > 0) {
			dropPit++;
			if(aiIndex == 0 && newIndex != (inputBoard.size()/2 - 1) && newIndex < inputBoard.size()) {
				inputBoard.set(newIndex, inputBoard.get(newIndex)+1);
				stoneCount--;
				if(stoneCount == 0 && dropPit < ((inputBoard.size() / 2) - 1) && inputBoard.get(dropPit) == 1) {
					// dropped in own pit, check for capture
					int buddyPitIndex = (inputBoard.size() - 2) - dropPit;
					if(inputBoard.get(buddyPitIndex) != 0) {
						int numCaptureStones = inputBoard.get(buddyPitIndex);
						inputBoard.set(buddyPitIndex, 0);
						numCaptureStones += inputBoard.get(dropPit);
						inputBoard.set(dropPit, 0);
						numCaptureStones += 1; 
						inputBoard.set(inputBoard.size() / 2 - 1, inputBoard.get(inputBoard.size() / 2 - 1) + numCaptureStones);
					}
					
				}
			}
			else if(aiIndex == 1 && newIndex != (inputBoard.size() / 2) - 1 && newIndex < inputBoard.size()) {
				inputBoard.set(newIndex, inputBoard.get(newIndex) + 1);
				stoneCount--;
				if(stoneCount == 0 && dropPit > ((inputBoard.size() / 2) - 1) && dropPit < (inputBoard.size() - 1) && inputBoard.get(dropPit) == 1) {
					// dropped in own pit, check for capture
					int buddyPitIndex = (inputBoard.size() - 2) - dropPit;
					if(inputBoard.get(buddyPitIndex) != 0) {
						int numCaptureStones = inputBoard.get(buddyPitIndex);
						inputBoard.set(buddyPitIndex, 0);
						numCaptureStones += inputBoard.get(dropPit);
						inputBoard.set(dropPit, 0);
						numCaptureStones += 1;
						inputBoard.set(inputBoard.size() - 1, inputBoard.get(inputBoard.size() - 1) + numCaptureStones);
					}
				}
			}
			if(newIndex == inputBoard.size()) {
				newIndex = 0;
			}
			else {
				newIndex++;
			}
		}
		/* Check if resulting capture is a game over */
		boolean GameOver = true;
		for(int i = 0; i < (inputBoard.size()/2 - 1); ++i) { //Iterate over first half of board
			if(inputBoard.get(i) > 0) {
				GameOver = false;
				break;
			}
		}
		for(int i = inputBoard.size()/2; i < inputBoard.size()-1; ++i) { //Iterate over second half
			if(inputBoard.get(i) > 0) {
				GameOver = false;
				break;
			}
		}
		if(GameOver) { //Capture all stones
			int sum1 = 0;
			int sum2 = 0;
			for(int i = 0; i < (inputBoard.size()/2 - 1); ++i) { //Iterate over first half of board
				sum1 += inputBoard.get(i);
				inputBoard.set(i, 0);
			}
			for(int i = inputBoard.size()/2; i < inputBoard.size()-1; ++i) { //Iterate over second half
				sum2 += inputBoard.get(i);
				inputBoard.set(i, 0);
			}
			inputBoard.set(inputBoard.size()/2 - 1, sum1);
			inputBoard.set(inputBoard.size() - 1, sum2);
		}
		return inputBoard;
	}
	
	public Node getAncestorNode(Node n) {
		if(n.getPreviousNode() == root) {
			return n;
		}
		else {
			return getAncestorNode(n.getPreviousNode());
		}
	}
	/**
	 * Calculate the utility value of a node based on board state
	 * @param node
	 * @return
	 */
	public int calculateUtilityValue(Node node) {
		//System.out.println("Calculating Utility");
		/*
		 * Important things to look at
		 * 1. Avoid capture of a large amount of stones
		 * 2. Favor move thats gives a second turn
		 * 4. Set up double moves
		 */
		ArrayList<Integer> board = node.getBoard();
		
		int utilityVal = 0;
		
		/*AI is red player*/
		if(aiIndex == 0) {
			int redEndPit = board.size()/2-1;
			int blueEndPit = board.size()-1;
			int totalStones = 0;
			
			for(int i = 0; i < board.size(); ++i) {//calculate the total number of stones in game
				totalStones += board.get(i);
			}
			
			if(redEndPit > totalStones/2) { //AI wins
				utilityVal += 5000;
			}
			else if(blueEndPit > totalStones/2) { //AI loses
				utilityVal -= 5000;
			}
			else { //Calculate complex utility value for AI
				
				/*Assign values for stones in end pit */
				utilityVal += 5*board.get(redEndPit);
				utilityVal -= 5*board.get(blueEndPit);
				
				int playerStones = 0;
				int aiStones = 0;
				
				/*Sum the number of stones each player has */
				for(int i = 0; i < (board.size()/2); ++i) { //Iterate over first half of board
					aiStones += board.get(i);
				}
				for(int i = board.size()/2; i < board.size(); ++i) { //Iterate over second half
					playerStones += board.get(i);
				}
				
				utilityVal += aiStones - playerStones;//difference in stones held
				
				
				if(board.get(redEndPit-1) == 0) {//favor pit before end pit that is 
					utilityVal += 10;
				}
				if(board.get(redEndPit-1) == 1) {//favor pit before end pit that results in a double move
					utilityVal += 30;
				}

				/*Avoid Capture*/
				for(int i = 0; i < (board.size()/2 - 1); ++i) { //Iterate over first half of board
					int buddyIndex = (board.size() - 2) - i;
					if(board.get(buddyIndex) == 0) { //potential player Capture
						int capturableNumber = 1;
						/*Check other side of board to see if there is a move that will result in AI pit being captured*/
						for(int j = buddyIndex-1; j > board.size()/2; --j) {
							if(board.get(j) == capturableNumber) {
								utilityVal -= 12*board.get(i);
							}
							++capturableNumber;
						}
					}
				}
				
				/*Favor Capture*/
				for(int i = 0; i < (board.size()/2 - 1); ++i) { //Iterate over first half of board
					int buddyIndex = (board.size() - 2) - i;
					if(board.get(i) == 0) { //potential AI Capture
						int capturableNumber = 1;
						/*Check other side of board to see if there is a move that will result in AI capturing a pit*/
						for(int j = i-1; j > 0; --j) {
							if(board.get(j) == capturableNumber) {
								utilityVal += 8*board.get(buddyIndex);
							}
							++capturableNumber;
						}
					}
				}
				
				
				
			}
		}
		/*AI is blue player*/
		else if(aiIndex == 1) {
			int redEndPit = board.size()/2-1;
			int blueEndPit = board.size()-1;
			int totalStones = 0;
			
			for(int i = 0; i < board.size(); ++i) {//calculate the total number of stones in game
				totalStones += board.get(i);
			}
			
			if(blueEndPit > totalStones/2) { //AI wins
				utilityVal += 1000;
			}
			else if(redEndPit > totalStones/2) { //AI loses
				utilityVal -= 1000;
			}
			else { //Calculate complex utility value for AI
				utilityVal += 5*board.get(blueEndPit);
				utilityVal -= 5*board.get(redEndPit);
				int playerStones = 0;
				int aiStones = 0;
				
				/*Sum the number of stones each player has */
				for(int i = board.size()/2; i < board.size()-1; ++i) { //Iterate over first half of board
					aiStones += board.get(i);
				}
				for(int i = 0; i < board.size()/2-1; ++i) { //Iterate over second half
					playerStones += board.get(i);
				}
				utilityVal += aiStones - playerStones;
				
				
				if(board.get(blueEndPit-1) == 0) {
					utilityVal += 10;
				}
				if(board.get(blueEndPit-1) == 1) {
					utilityVal += 20;
				}
				
				/*Avoid Capture*/
				for(int i = board.size()/2; i < (board.size()-1); ++i) { //Iterate over second half of board
					int buddyIndex = (board.size() - 2) - i;
					if(board.get(buddyIndex) == 0) { //potential player Capture
						int capturableNumber = 1;
						/*Check other side of board to see if there is a move that will result in AI pit being captured*/
						for(int j = buddyIndex-1; j > 0; --j) {
							if(board.get(j) == capturableNumber) {
								utilityVal -= 10*board.get(i);
							}
							++capturableNumber;
						}
					}
				}
				
				/*Favor Capture*/
				for(int i = board.size()/2; i < (board.size()-1); ++i) { //Iterate over second half of board
					int buddyIndex = (board.size() - 2) - i;
					if(board.get(i) == 0) { //potential AI Capture
						int capturableNumber = 1;
						/*Check other side of board to see if there is a move that will result in AI pit being captured*/
						for(int j = i-1; j > board.size()/2; --j) {
							if(board.get(j) == capturableNumber) {
								utilityVal += 5*board.get(buddyIndex);
							}
							++capturableNumber;
						}
					}
				}
				
				
			}
		}
		
		return utilityVal;
	}
}