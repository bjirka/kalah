package csce315.kalah.model;

import java.util.ArrayList;
public class Node {
	/**
	* Value to represent the usefulness of a move
	*/
	double utilityVal;
	
	/**
	* Will store the ancestor move so the next move
	* can easily be retrieved
	*/
	Node previousNode;
	
	/** a terminal node means this is the final move the AI will look at
	* the next move will take into account all terminal nodes utility values
	*/
	boolean isTerminal;
	
	/**
	 *  represents the current Board State held in the Node
	 */
	ArrayList<Integer> boardState = new ArrayList<Integer>();
	
	/**
	 * represents the child moves of the Node
	 */
	ArrayList<Node> childMoves = new ArrayList<Node>();
	
	boolean producesDouble = false;
	
	int indexOfMove;
	public Node() {
		utilityVal = 0;
		isTerminal = false;
	}
	
	public Node(ArrayList<Integer> newState, int movePit) {
		indexOfMove = movePit;
		boardState = newState;
	}
	
	public Node(double initialUtility) {
		utilityVal = initialUtility;
	}
	
	public void addParent(Node n) {
		previousNode = n;
	}
	
	public void setToTerminal() {
		isTerminal = true;
	}
	
	public double getUtilityVal() {
		return utilityVal;
	}
	
	public ArrayList<Integer> getBoard() {
		return boardState;
	}
	
	public void setBoard(ArrayList<Integer> newBoard) {
		boardState = newBoard;
	}
	
	public void addChild(Node child) {
		childMoves.add(child);
	}
	
	public void setUtilityVal(double utilityVal) {
		this.utilityVal = utilityVal;
	}
	
	public ArrayList<Node> getChildren(){
		return childMoves;
	}
	
	public Node maxNode(Node nodeToBeCompared) {
		if(this.utilityVal >= nodeToBeCompared.getUtilityVal()) {
			return this;
		}
		else {
			return nodeToBeCompared;
		}
	}
	
	public Node minNode(Node nodeToBeCompared) {
		if(this.utilityVal <= nodeToBeCompared.getUtilityVal()) {
			return this;
		}
		else {
			return nodeToBeCompared;
		}
	}
	
	public int getMove(){
		return indexOfMove;
	}
	
	public void setMove(int newMove) {
		indexOfMove = newMove;
	}
	
	public Node getPreviousNode(){
		return previousNode;
	}
	
	public boolean getProducesDouble() {
		return producesDouble;
	}
	
	public void setProducesDouble(boolean b) {
		producesDouble = b;
	}
	public int compare(Node n) {
		if(this.utilityVal > n.getUtilityVal()) {
			return 1;
		}
		else if(this.utilityVal < n.getUtilityVal()) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
	

