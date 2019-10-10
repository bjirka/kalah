package csce315.kalah.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import csce315.kalah.Main;
import csce315.kalah.Main.APP_MODE;
import csce315.kalah.model.Pit.PIT_TYPE;
import csce315.kalah.model.Player.PLAYER_TYPE;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class Game {
	private Player[] players;
	private Board board;
	private AI ai;
	private int whosTurn;
	private int player1TurnCount;
	private int winner;
	private boolean isGameOver;
	private boolean hasLegalMove;
	
	private String currentMove; // this will keep track of which pits were played for the current turn to send to clients
	private Queue<String> clientMoveQueue; // this will hold the queue of moves from the client when the client sends multiple moves in one message
	
	private int numPlayerPits;  // number of pits per side (not counting end pits) (range: 4-9)
	private int moveTimerInit; // number of milliseconds to make a move, if 0 then infinite time
	//private SimpleTimer timer = new SimpleTimer();
	
	
	private ChangeListener<Boolean> client0Listener;
	private ChangeListener<Boolean> client1Listener;
	private ChangeListener<Boolean> serverListener;

	
	
	/**
	 * Default constructor, setup the two players and who goes first
	 */
	public Game(PLAYER_TYPE player0Type, PLAYER_TYPE player1Type, ArrayList<Integer> initialBoardState, int moveTimer) {
		resetGame(player0Type, player1Type, initialBoardState, moveTimer);
	}
	
	public void resetGame(PLAYER_TYPE player0Type, PLAYER_TYPE player1Type, ArrayList<Integer> initialBoardState, int moveTimer) {
		this.numPlayerPits = initialBoardState.size();
		this.moveTimerInit = moveTimer;
		this.player1TurnCount = 0;
		this.currentMove = "";
		
		board = new Board(this, initialBoardState);
		
		players = new Player[2];
		
		if(player0Type == PLAYER_TYPE.Human) {
			players[0] = new Player(PLAYER_TYPE.Human, null);
		} else if(player0Type == PLAYER_TYPE.AI) {
			players[0] = new Player(PLAYER_TYPE.AI, new AI(board.getBoardState(), 0, this.moveTimerInit));
		} else if(player0Type == PLAYER_TYPE.Client) {
			players[0] = new Player(PLAYER_TYPE.Client, null);
		} else if(player0Type == PLAYER_TYPE.Server) {
			players[0] = new Player(PLAYER_TYPE.Server, null);
		}
		if(player1Type == PLAYER_TYPE.Human) {
			players[1] = new Player(PLAYER_TYPE.Human, null);
		} else if(player1Type == PLAYER_TYPE.AI) {
			//System.out.println("Game satisfaction");
			players[1] = new Player(PLAYER_TYPE.AI, new AI(board.getBoardState(), 1, this.moveTimerInit));
		} else if(player1Type == PLAYER_TYPE.Client) {
			players[1] = new Player(PLAYER_TYPE.Client, null);
		} else if(player1Type == PLAYER_TYPE.Server) {
			players[1] = new Player(PLAYER_TYPE.Server, null);
		}
		
		
		client0Listener = (observable, oldValue, newValue) -> {
			if(newValue == false) {
				Main.debug("server listener 0 triggered");
				String clientsMove = Main.getServer().getMessage(Main.getOptions().getPlayer0ThreadId());
				Main.debug(clientsMove);
				clientTurn(clientsMove, 0);
			}
		};
		
		client1Listener = (observable, oldValue, newValue) -> {
			if(newValue == false) {
				Main.debug("server listener 1 triggered");
				String clientsMove = Main.getServer().getMessage(Main.getOptions().getPlayer1ThreadId());
				Main.debug(clientsMove);
				clientTurn(clientsMove, 1);
			}
		};
		
		serverListener = (observable, oldValue, newValue) -> {
			if(newValue == false) {
				String s = Main.getClient().getMessage();
				Main.debug("listener triggered" + " " + s);
				serverTurn(s);
			}
		};
		
		whosTurn = 0; // Player 0 will go first 
		nextTurn();		
	}
	
	
	/**
	 * Either waits for the a human player to make a move or makes the move that the AI selected
	 */
	private void nextTurn() {
		if(whosTurn == 1) {
			player1TurnCount++;
		}
		boolean pieOption = false;
		if(player1TurnCount == 1 && whosTurn == 1) {
			board.hideInfoBar(0);
			board.showInfoBar(1, "Blue Player's Turn");
			pieOption = true;
		}
		
		Main.setStartTime(System.currentTimeMillis());
		
		// if not a client then start the timer now, client's timer will start when the OK message is received
		if(players[whosTurn].getPlayerType() != PLAYER_TYPE.Client) {
			// start the timer for this turn if not set for unlimited time
			Main.setStartTime(System.currentTimeMillis());
			if(moveTimerInit > 0) {
				Main.getTimer().setTimer(moveTimerInit/1000);
				//timer.setTimer(moveTimerInit/1000);		
				Main.getTimer().start();
			}
		}
		if(moveTimerInit > 0) {
			board.getClockLabel(whosTurn).textProperty().bind(Bindings.format("0:%02d", Main.getTimer().timeLeftProperty()));
		}

		
		int pitIndex = players[whosTurn].getMove(pieOption, board.getBoardState());

		// if pitIndex == -1 then it's a humans turn
		if(pitIndex == -1) {
			// turn on click events
			board.enablePits(whosTurn);
			// waiting for human
			if(whosTurn == 0) {
				board.hideInfoBar(1);
				if(players[1-whosTurn].getPlayerType() == PLAYER_TYPE.Server) {
					board.showInfoBar(0, "Your Turn");
				} else {
					board.showInfoBar(0, "Red Player's Turn");
				}
			} else {
				board.hideInfoBar(0);
				if(players[1-whosTurn].getPlayerType() == PLAYER_TYPE.Server) {
					board.showInfoBar(1, "Your Turn");	
				} else {
					board.showInfoBar(1, "Blue Player's Turn");
				}
			}
		} else if(pitIndex == -2) { // means Pie Rule was invoked
			makePieRuleMove();
		} else if(pitIndex == -3) { // means waiting on client machine to send move
			// waiting for client
			board.addText("Waiting for client to send play (whosturn = " + whosTurn + ")\n");
			if(whosTurn == 0) {
				board.hideInfoBar(1);
				board.showInfoBar(0, "Waiting for Red Player");
				Main.debug("adding server 0 listener");
				Main.getServer().queueEmpty(Main.getOptions().getPlayer0ThreadId()).addListener(client0Listener);
				if(Main.getServer().queueEmpty(Main.getOptions().getPlayer0ThreadId()).get() == false) {
					String clientsMove = Main.getServer().getMessage(Main.getOptions().getPlayer0ThreadId());
					clientTurn(clientsMove, 0);
				}
			}
			if(whosTurn == 1) {
				board.hideInfoBar(0);
				board.showInfoBar(1, "Waiting for Blue Player");
				Main.debug("adding server 1 listener");
				Main.getServer().queueEmpty(Main.getOptions().getPlayer1ThreadId()).addListener(client1Listener);
				if(Main.getServer().queueEmpty(Main.getOptions().getPlayer1ThreadId()).get() == false) {
					String clientsMove = Main.getServer().getMessage(Main.getOptions().getPlayer1ThreadId());
					clientTurn(clientsMove, 1);
				}
			}
			board.addText("Waiting for client to send play2 (whosturn = " + whosTurn + ")\n");
		} else if(pitIndex == -4) { // means waiting on server machine to send move
			// waiting for server
			if(whosTurn == 0) {
				board.hideInfoBar(1);
				board.showInfoBar(0, "Waiting for Red Player");
				//board.showInfoBar(0, whosTurn + " " + players[whosTurn].getPlayerType());
			}else {
				board.hideInfoBar(0);
				board.showInfoBar(1, "Waiting for Blue Player");
				//board.showInfoBar(0, whosTurn + " " + players[whosTurn].getPlayerType());
			}
			Main.getClient().queueEmpty().addListener(serverListener);
			if(Main.getClient().queueEmpty().get() == false) {
				serverTurn(Main.getClient().getMessage());
			}
			board.addText("Waiting for server to send play\n");
		} else {
			// otherwise the AI has made a selection
			makeMove(pitIndex);
		}
	}
	
	
	
	/**
	 * 
	 */
	
	private void makeMove(int pitIndex) {
		
		//if(players[whosTurn].getPlayerType() != PLAYER_TYPE.Client && moveTimerInit > 0) {
		if(Main.getOptions().getAppMode() != APP_MODE.Client && moveTimerInit > 0) {
			Main.setEndTime(System.currentTimeMillis());
			long totalTime = Main.getEndTime() - Main.getStartTime();
			if(totalTime > moveTimerInit) {
				displayResult(1-whosTurn);
				board.generalMessage("Too Much Time!", 10);
				return;
			}
		}

		currentMove += pitIndex + " ";
		
		// start the stone distribution animation (will call makeMove2() when it's done)
		int lastPitIndex = board.distributeStones(pitIndex);
		Pit lastPit = board.getPit(lastPitIndex);


		// if last pit is in the end pit, but the player has no other moves then need to end the turn
		hasLegalMove = false;
		if(lastPit.getType() == PIT_TYPE.END_PIT) {
			ArrayList<Integer> playersPits = board.getPlayerPits(whosTurn);
			Main.debug("checking for remaining legal moves for " + whosTurn);
			String debugOutput = "";
			for(int i = 0; i < playersPits.size(); i++) {
				Pit pit = board.getPit(playersPits.get(i));
				debugOutput += " " + pit.count();
				if(pit.getType() == PIT_TYPE.PLAYER_PIT && pit.count() > 0) {
					hasLegalMove = true;
					//break;
				}
			}
			Main.debug(debugOutput);
		}
		
		Main.debug("Has legal move: " + hasLegalMove);
		
		// send the move to client players if needed
		if(lastPit.getType() != PIT_TYPE.END_PIT || !hasLegalMove) { 
			if(whosTurn == 0) { // player 0's turn, send message to player 1's thread if it's a remote player
				if(Main.getOptions().getPlayer1Type() == PLAYER_TYPE.Client) {
					Main.debug("Send move to client (to 1): " + currentMove + " Player 1 Type: " + players[1].getPlayerType());
					Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), currentMove.trim());
				} else if(players[1].getPlayerType() == PLAYER_TYPE.Server) {
					Main.debug("Send move to server: " + currentMove + " Player 1 Type: " + players[1].getPlayerType());
					Main.getClient().sendMessage(currentMove.trim());
				}
			} else { // player 1's turn, send message to player 0's thread if it's a remote player
				if(players[0].getPlayerType() == PLAYER_TYPE.Client) {
					Main.debug("Send move to client (to 0): " + currentMove + " Player 0 Type: " + players[0].getPlayerType());
					Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), currentMove.trim());
				} else if(players[0].getPlayerType() == PLAYER_TYPE.Server) {
					Main.debug("Send move to server: " + currentMove + " Player 0 Type: " + players[0].getPlayerType());
					Main.getClient().sendMessage(currentMove.trim());
				}
			}
			
			currentMove = "";
		}
		
		
		// if this is the server, then check if the game is over and send message indicating so to the client
		if(Main.getOptions().getAppMode() == APP_MODE.Server) {
			isGameOver = isGameOver(lastPitIndex);
			if(isGameOver) {
				board.addText("GAME IS OVER! 1\n");
				/*
				 * Send Winner or LOSER to client
				 */
				determineWinner(lastPitIndex);
				Main.debug("Determined the winner is: " + winner);
				if(winner == 0) { //if player 0 won
					if(players[0].getPlayerType() == PLAYER_TYPE.Client) {//client is player 0 -> client wins
						/*
						 * Send "WINNNER" to client
						 */
						Main.debug("Send move to client (to 0): " + "WINNER" + " Player 0 Type: " + players[0].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "WINNER");
					}
					if(players[1].getPlayerType() == PLAYER_TYPE.Client){//client is player 1 -> client loses
						/*
						 * Send "LOSER" to client
						 */
						Main.debug("Send move to client (to 1): " + "LOSER" + " Player 1 Type: " + players[1].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "LOSER");
					}
				} else if(winner == 1) { //if player 1 won
					if(players[1].getPlayerType() == PLAYER_TYPE.Client) {//client is player 1 -> client wins
						/*
						 * Send "WINNER" to client
						 */
						Main.debug("Send move to client (to 1): " + "WINNER" + " Player 1 Type: " + players[1].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "WINNER");
					}
					if(players[0].getPlayerType() == PLAYER_TYPE.Client){//client is player 0 -> client loses
						/*
						 * Send "LOSER" to client
						 */
						Main.debug("Send move to client (to 0): " + "LOSER" + " Player 0 Type: " + players[0].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "LOSER");
					}
				} else { //game is a draw
					/*
					 * Send DRAW to clients
					 */
					if(players[1].getPlayerType() == PLAYER_TYPE.Client) { //player 1 is a client -> send tie message
						Main.debug("Send move to client (to 1): " + "TIE" + " Player 1 Type: " + players[1].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "TIE");
					}
					if(players[0].getPlayerType() == PLAYER_TYPE.Client){ //player 0 is a client -> send tie message
						Main.debug("Send move to client (to 0): " + "TIE" + " Player 0 Type: " + players[0].getPlayerType());
						Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "TIE");
					}
				}
			}
		}
	}
	/**
	 * This finishes executing a move after the animation from the first part finishes
	 * and determines who's turn is next
	 * @param pitIndex The index of the pit that was played
	 */
	protected void makeMove2(int pitIndex, int lastPitIndex) {
		Pit lastPit = board.getPit(lastPitIndex);
		// if the other player is AI, then tell it's AI object what move was made
		/*if(players[1-whosTurn].getPlayerType() == PLAYER_TYPE.AI) {
			players[1-whosTurn].getAI().lastMove(pitIndex,whosTurn);
		}*/
		board.addText("Player " + whosTurn + " making move: " + pitIndex + "\n");
		
		// check if a capture is needed
		if(lastPit.getOwner() == whosTurn && lastPit.count() == 1 && lastPit.getType() == PIT_TYPE.PLAYER_PIT) {
			// capture stones from opposite pit if opposite pit isn't empty
			int buddyPitIndex = board.getBuddyPit(lastPitIndex);
			// check if opposite pit is empty
			if(board.getPit(buddyPitIndex).count() != 0) {
				//board.addText("CAPTURE!\n");
				board.generalMessage("CAPTURE!");
				int buddyPitStones = board.getPit(buddyPitIndex).removeAll();
				board.updatePitDisplay(buddyPitIndex, 0);
				int lastPitStones = lastPit.removeAll();
				board.updatePitDisplay(lastPitIndex, 0);
				int endPitIndex = board.getEndPit(whosTurn);
				board.getPit(endPitIndex).addStones(buddyPitStones + lastPitStones);
				board.updatePitDisplay(endPitIndex, board.getPit(endPitIndex).count());
			}
		
		}


		// landed in end pit and its a client or server's turn then there should be another move in the queue
		if(lastPit.getType() == PIT_TYPE.END_PIT && (players[whosTurn].getPlayerType() == PLAYER_TYPE.Client || players[whosTurn].getPlayerType() == PLAYER_TYPE.Server)) {
			if(!clientMoveQueue.isEmpty()) {
				makeMove(Integer.parseInt(clientMoveQueue.remove()));
				return;
			}
		}
		
		
		if(Main.getOptions().getAppMode() == APP_MODE.Client) {
			isGameOver = isGameOver(-1);
			if(isGameOver) {
				board.addText("GAME IS OVER! 2\n");
				// move all of opponents remaining stones to their end pit
				captureRemainingStones();
				
				return;
			}
		}else {
			if(isGameOver) {
				board.addText("GAME IS OVER! 2\n");
				// move all of opponents remaining stones to their end pit
				captureRemainingStones();
				
				// display winner
				displayResult(winner);
				
				return;
			}
		}

		
		if(lastPit.getType() != PIT_TYPE.END_PIT || !hasLegalMove) { 
			whosTurn = 1- whosTurn;
		} else {
			board.generalMessage("Move Again");
		}
		
		nextTurn();		
	}
	
	
	/**
	 * Generates an array of integers that represents the number of stones each pit on a side will start with
	 * @param numPlayerPits
	 * @param numStones
	 * @return
	 */
	static public ArrayList<Integer> initialBoardState(int numPlayerPits, int numStones, Boolean isRandomStones){
		// populate an array with the number of stones each pit will start with, either a uniform amount or random
		Random rand = new Random();
		int totalStones = numPlayerPits * numStones;
		ArrayList<Integer> initialStones = new ArrayList<Integer>();
		for(int i = 0; i < numPlayerPits; i++) {
			if(!isRandomStones) { // for a uniform number of stones in each pit
				initialStones.add(numStones);
			} else { // for a random number of stones in each pit
				if(i == numPlayerPits - 1) { // the last pit will contain the remaining stones
					initialStones.add(totalStones);
				} else {
					int numRandStones = rand.nextInt(totalStones - (numPlayerPits - i)) + 1;
					// don't let any pit have more than half the total number of stones
					if(numRandStones > (numPlayerPits * numStones / 2)) numRandStones = (int) Math.floor(numPlayerPits * numStones / 2);
					totalStones -= numRandStones;
					initialStones.add(numRandStones);
				}
			}
		}
		
		return initialStones;
	}
	/**
	 * This is called when the pie rule is invoked, 
	 * tells the board to flip the pits then sets turn to player 0
	 */
	private void makePieRuleMove() {
		if(players[whosTurn].getPlayerType() != PLAYER_TYPE.Client && moveTimerInit > 0) {
			Main.setEndTime(System.currentTimeMillis());
			long totalTime = Main.getEndTime() - Main.getStartTime();
			if(totalTime > moveTimerInit) {
				displayResult(1-whosTurn);
				board.generalMessage("Too Much Time!", 10);
				return;
			}
		}
		
		Main.debug("Send move to clients: P");
		board.generalMessage("Pie Rule Envoked!");
		board.flipBoard();
		if(players[0].getPlayerType() == PLAYER_TYPE.Client) {
			Main.debug("Send move to client (to 0): " + "P" + " Player 0 Type: " + players[0].getPlayerType());
			Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "P");
		} else if(players[0].getPlayerType() == PLAYER_TYPE.Server) {
			Main.debug("Send move to server: " + "P" + " Player 0 Type: " + players[0].getPlayerType());
			Main.getClient().sendMessage("P");
		}
		whosTurn = 0;
		nextTurn();
	}
	
	/**
	 * This is triggered by a human user clicking on a pit
	 * @param pitIndex The index of the pit that the user clicked on
	 */
	public void humanTurn(int pitIndex) {
		makeMove(pitIndex);
	}

	/**
	 * Server's Turn
	 * @param move
	 */
	public void serverTurn(String move) {
		if(move.equals("WINNER")){
			Main.debug("" + whosTurn);
			captureRemainingStones();
			//displayResult(1-whosTurn);
			if(players[0].getPlayerType() == PLAYER_TYPE.Server) {
				displayResult(1);
			} else {
				displayResult(0);
			}
		} else if(move.equals("LOSER")) {
			captureRemainingStones();
			Main.debug("" + whosTurn);
			//displayResult(whosTurn);
			if(players[0].getPlayerType() == PLAYER_TYPE.Server) {
				displayResult(0);
			} else {
				displayResult(1);
			}
		} else if(move.equals("TIE")) {
			captureRemainingStones();
			displayResult(-1);
		} else if(move.equals("P")) { //if server want to envoke pie rule
			Main.getClient().sendMessage("OK"); // send OK message back to server
			Platform.runLater(() -> {
				makePieRuleMove();
			});
		} else if(move.equals("ILLEGAL")) {
			board.generalMessage("Illegal Move!", 10);
			try {
				Thread.sleep(100); // wait for a bit for the second message to come
			} catch (InterruptedException e) { }
			serverTurn(Main.getClient().getMessage());
		} else if(move.equals("TIME")) {
			board.generalMessage("Too Much Time!", 10);
			try {
				Thread.sleep(100); // wait for a bit for the second message to come
			} catch (InterruptedException e) { }
			serverTurn(Main.getClient().getMessage());
		} else {
			Main.getClient().sendMessage("OK"); // send OK message back to server
			clientMoveQueue = new LinkedList<String>(Arrays.asList(move.trim().split(" ")));
			Main.getClient().queueEmpty().removeListener(serverListener);
			Main.debug("inside serverTurn");
			Platform.runLater(() -> {
				makeMove(Integer.parseInt(clientMoveQueue.remove()));
			});
		}
	}
	
	
	public void clientTurn(String move, int playerId) {
		// stop client timer, check if move is over timer, if so send TIME and LOSER
		Main.setEndTime(System.currentTimeMillis());
		
		clientMoveQueue = new LinkedList<String>(Arrays.asList(move.trim().split(" ")));

		long totalTime = Main.getEndTime() - Main.getStartTime();
		Main.debug("Total Time: " + totalTime + " End Time: " + Main.getEndTime() + " Start Time: " + Main.getStartTime());
		if(totalTime > (moveTimerInit * clientMoveQueue.size()) && moveTimerInit > 0) {
			if(playerId == 0) {
				Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "TIME");
				Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "LOSER");
				displayResult(1);
				board.generalMessage("Too Much Time!");
				if(players[1].getPlayerType() == PLAYER_TYPE.Client) {
					Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "TIME");
					Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "WINNER");
				}
				return;
			} else {
				Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "TIME");
				Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "LOSER");
				displayResult(0);
				board.generalMessage("Too Much Time!");
				if(players[0].getPlayerType() == PLAYER_TYPE.Client) {
					Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "ILLEGAL");
					Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "WINNER");
				}
				return;
			}
		}
		
		Main.debug("Time took: " + totalTime + " milliseconds of " + moveTimerInit + " limit");

		
		// determine if move illegal, if so send ILLEGAL then LOSER
		String result = isLegalMove(move, playerId);
		if(playerId == 0) {
			Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), result);
			if(result.equals("ILLEGAL")) {
				Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "LOSER");
				board.generalMessage("Illegal Move!");
				displayResult(1);
				if(players[1].getPlayerType() == PLAYER_TYPE.Client) {
					Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "ILLEGAL");
					Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "WINNER");
				}
			}
		}else {
			Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), result);
			if(result.equals("ILLEGAL")) {
				Main.getServer().sendMessage(Main.getOptions().getPlayer1ThreadId(), "LOSER");
				board.generalMessage("Illegal Move!");
				displayResult(0);
				if(players[0].getPlayerType() == PLAYER_TYPE.Client) {
					Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "ILLEGAL");
					Main.getServer().sendMessage(Main.getOptions().getPlayer0ThreadId(), "WINNER");
				}
			}
		}
		
		if(move.equals("P")) {
			Platform.runLater(() -> {
				makePieRuleMove();
			});
		} else {
			//clientMoveQueue = new LinkedList<String>(Arrays.asList(move.trim().split(" ")));
			if(playerId == 0) {
				Main.getServer().queueEmpty(Main.getOptions().getPlayer0ThreadId()).removeListener(client0Listener);
			} else {
				Main.getServer().queueEmpty(Main.getOptions().getPlayer1ThreadId()).removeListener(client1Listener);
			}
			Main.debug("inside clientTurn " + move);
			Platform.runLater(() -> {
				makeMove(Integer.parseInt(clientMoveQueue.remove()));
			});
		}
	}
	
	
	/**
	 * Determines if the last move received from the client was legal
	 * @param move The move string that was passed from the client
	 * @param playerId The client's player id
	 * @return Returns "OK" if the move is ok and "ILLEGAL" if there was something wrong with the move
	 */
	private String isLegalMove(String move, int playerId) {
		// check if "P" move was sent any time other than the second player's first turn
		if(move.equals("P")) {
			if(playerId == 0) {
				return "ILLEGAL"; // player 0 can't use "P" move
			} else if(player1TurnCount != 1) {
				return "ILLEGAL"; // player 1 can only use "P" on their first move
			}
		} else {
			
			// check if each move is:
			//	1) inside the range of the player's pits
			//	2) not empty
			//	3) the correct number of moves send (1 move unless landing in an end pit)
			
			ArrayList<Integer> boardState = new ArrayList<Integer>(board.getBoardState());
			
			ArrayList<Integer> firstPitIndex = new ArrayList<Integer>();
			firstPitIndex.add(0); // index of player 0's first pit
			firstPitIndex.add(boardState.size() / 2); // index of player 1's first pit
			ArrayList<Integer> lastPitIndex = new ArrayList<Integer>();
			lastPitIndex.add(boardState.size() / 2 - 2); // index of player 0's last pit (not including end pit
			lastPitIndex.add(boardState.size() - 2); // index of player 1's last pit (not including end pit)
			ArrayList<Integer> endPitIndex = new ArrayList<Integer>();
			endPitIndex.add(boardState.size() / 2 - 1); // index of player 0's end pit
			endPitIndex.add(boardState.size() - 1); // index of player 1's end pit
			
			ArrayList<String> moves = new ArrayList<String>(Arrays.asList(move.trim().split(" ")));
			for(int i = 0; i < moves.size(); i++) {
				int currentMove = Integer.parseInt(moves.get(i));
				Main.debug("Checking legality of move: " + currentMove);
				if(currentMove < firstPitIndex.get(playerId) || currentMove > lastPitIndex.get(playerId)) {
					return "ILLEGAL"; // index wasn't one of the player's pits
				}
				if(boardState.get(currentMove) == 0) {
					return "ILLEGAL"; // can't play an empty pit
				}
				
				
				
				///////////////////////
				
				// remove all the stones from the selected pit and store how many stones it had
				int stoneCount = boardState.get(currentMove);
				boardState.set(currentMove,0);
				// loop through each successive pit dropping 1 stone until all the stones have been distributed
				int newIndex = currentMove + 1;
				//int dropPit= currentMove;
				while(stoneCount > 0) {
					//dropPit++;
					//if(playerId == 0 && newIndex != (boardState.size()/2 - 1) && newIndex < boardState.size()) {
					if(newIndex != endPitIndex.get(1-playerId) && newIndex < boardState.size()) {
						boardState.set(newIndex, boardState.get(newIndex)+1);
						stoneCount--;
					}
					if(newIndex == boardState.size()) {
						newIndex = 0;
					} else {
						newIndex++;
					}	
				}
				
				int lastPit = --newIndex;
				int dropPit = lastPit;
				
				Main.debug("lastPit: " + lastPit + " playerId: " + playerId + " dropPit: " + dropPit);
				if(lastPit >= firstPitIndex.get(playerId) && lastPit <= lastPitIndex.get(playerId) && boardState.get(lastPit) == 1) {
					// dropped in own pit, check for capture
					int buddyPitIndex = (boardState.size() - 2) - lastPit;
					if(boardState.get(buddyPitIndex) != 0) {
						int numCaptureStones = boardState.get(buddyPitIndex);
						boardState.set(buddyPitIndex, 0);
						numCaptureStones += boardState.get(dropPit);
						boardState.set(dropPit, 0);
						numCaptureStones += 1; 
						boardState.set(endPitIndex.get(playerId), boardState.get(endPitIndex.get(playerId)) + numCaptureStones);
					}
				}
	
				/* Check if resulting capture is a game over */
				boolean GameOver = true;
				for(int j = 0; j < (boardState.size()/2 - 1); ++j) { //Iterate over first half of board
					if(boardState.get(j) > 0) {
						GameOver = false;
						break;
					}
				}
				for(int j = boardState.size()/2; j < boardState.size()-1; ++j) { //Iterate over second half
					if(boardState.get(j) > 0) {
						GameOver = false;
						break;
					}
				}
				if(GameOver) {
					// make sure there this is the last move
					if(i != moves.size() - 1) {
						return "ILLEGAL"; // if the game is over then this should be the last move
					}
				}
				
				// if last pit was and end pit then this shouldn't be the last move in the list of moves
				if(lastPit == endPitIndex.get(playerId)) {
					if(i == moves.size() - 1 && !GameOver) {
						return "ILLEGAL"; // there should be another move in the list, but there's not
					}
				} else {
					// didn't land in an end pit, so this should be the last move
					if(i != moves.size() - 1) {
						return "ILLEGAL"; // this should be the last move of the turn
					}
				}
	
				
				////////////////////////
				
			}
		}

		return "OK";
	}
	
	
	/**
	 * Returns a reference to the board
	 * @return A reference to the board
	 */
	public Board getBoard() {
		return board;
	}
	
	public AnchorPane getBoardLayout() {
		return board.getBoardLayout();
	}
	
	/**
	 * Returns who's turn it currently is
	 * @return The index of the player (either 0 or 1) whos turn it currently is
	 */
	public int getWhosTurn() {
		return whosTurn;
	}
	
	
	/**
	 * Returns a reference to the AI class
	 * @return A reference to the AI class
	 */
	public AI getAI() {
		return ai;
	}
	
	/**
	 * Checks if the game is over, either because one player has more than half the stones in their house
	 * or one player has no stones in any of their pits
	 * @return True if either condition is met and the game is over, False otherwise
	 */
	public boolean isGameOver(int lastPitIndex) {
		Main.debug("Checking if GAME OVER");
		
		ArrayList<Integer> tempBoardState = new ArrayList<Integer>(board.getBoardState());
		
		// if lastPitIndex isn't -1 then we need to check for capture
		if(lastPitIndex != -1) {
			// only 1 stone in the pit and not an end pit, then check if buddy is empty
			if(tempBoardState.get(lastPitIndex) == 1 && lastPitIndex != (tempBoardState.size() / 2 - 1) && lastPitIndex != (tempBoardState.size() - 1)) {
				int buddyIndex = tempBoardState.size() - 2 - lastPitIndex;
				Main.debug("BuddyIndex: " + buddyIndex + " tempBoardState.size(): " + tempBoardState.size() + " lastPitIndex: " + lastPitIndex);
				if(tempBoardState.get(buddyIndex) > 0) { // buddy isn't empty, so capture
					int capturedStones = tempBoardState.get(buddyIndex);
					tempBoardState.set(buddyIndex, 0);
					capturedStones += 1;
					tempBoardState.set(lastPitIndex, 0);
					if(lastPitIndex < (tempBoardState.size() / 2)) {
						tempBoardState.set((tempBoardState.size() / 2 - 1), capturedStones);
					} else {
						tempBoardState.set((tempBoardState.size() - 1), capturedStones);
					}
				}
			}
		}
		
		// check if all the current player's pits are empty
		boolean player0Empty = true;
		boolean player1Empty = true;
		if(Main.getOptions().getAppMode() == APP_MODE.Server){
			Main.debug("Confirm Server Mode");
			Main.debug("Player " + whosTurn + " turn");
			for(int i = 0; i < (tempBoardState.size() / 2 - 1); i++) {
				Main.debug("" + i  + ": " + tempBoardState.get(i));
				if(tempBoardState.get(i) > 0) player0Empty = false;
			}
			for(int i = (tempBoardState.size() / 2); i < (tempBoardState.size() - 1); i++) {
				Main.debug("" + i  + ": " + tempBoardState.get(i));
				if(tempBoardState.get(i) > 0) player1Empty = false;
			}
			/*
			ArrayList<Integer> player0Pits = board.getPlayerPits(whosTurn);
			for(int i = 0; i < player0Pits.size(); i++) {
				Pit pit = board.getPit(player0Pits.get(i));
				Main.debug("" + i  + ": " + pit.count());
				if(pit.getType() == PIT_TYPE.PLAYER_PIT && pit.count() > 0) player0Empty = false;
			}
			ArrayList<Integer> player1Pits = board.getPlayerPits(1-whosTurn);
			for(int i = 0; i < player1Pits.size(); i++) {
				Pit pit = board.getPit(player1Pits.get(i));
				Main.debug("" + i  + ": " + pit.count());
				if(pit.getType() == PIT_TYPE.PLAYER_PIT && pit.count() > 0) player1Empty = false;
			}
			*/
			if(!player0Empty && !player1Empty) {
				return false;
			}

			return true;
		} else {
			Main.debug("App Mode Client");
			if(!Main.getClient().queueEmpty().get()) {
				Main.debug("Queue Not Empty");
				String msg = Main.getClient().getMessage();
				Main.debug("msg: " + msg);
				serverTurn(msg);
				Main.debug("Returning 'true' (game is over)");
				return true;
			}
			return false;
		}
	}
	
	
	/**
	 * Check the number of stones in each end pit and determine who won
	 */
	private void determineWinner(int lastPitIndex) {
		// number of stones currently in each end pit
		int player0Stones = board.getPit(numPlayerPits).count();
		int player1Stones = board.getPit((numPlayerPits * 2) + 1).count();

		
		// if lastPitIndex isn't -1 then we need to check for capture
		if(lastPitIndex != -1) {
			// only 1 stone in the pit and not an end pit, then check if buddy is empty
			if(board.getBoardState().get(lastPitIndex) == 1 && lastPitIndex != (board.getBoardState().size() / 2 - 1) && lastPitIndex != (board.getBoardState().size() - 1)) {
				int buddyIndex = board.getBoardState().size() - 2 - lastPitIndex;
				Main.debug("BuddyIndex: " + buddyIndex + " tempBoardState.size(): " + board.getBoardState().size() + " lastPitIndex: " + lastPitIndex);
				if(board.getBoardState().get(buddyIndex) > 0) { // buddy isn't empty, so capture
					int capturedStones = board.getBoardState().get(buddyIndex);
					if(lastPitIndex < (board.getBoardState().size() / 2)) {
						player0Stones += capturedStones;
						player1Stones -= capturedStones;
					} else {
						player0Stones -= capturedStones;
						player1Stones += capturedStones;
					}
				}
			}
		}
		
		
		// add the ones that are still on the board for each player
		ArrayList<Integer> player0Pits = board.getPlayerPits(0);
		ArrayList<Integer> player1Pits = board.getPlayerPits(1);
		for(int i = 0; i < player0Pits.size(); i++) {
			Pit pit0 = board.getPit(player0Pits.get(i));
			Pit pit1 = board.getPit(player1Pits.get(i));
			if(pit0.getType() == PIT_TYPE.PLAYER_PIT) {
				player0Stones += pit0.count();
				player1Stones += pit1.count();
			}
		}
		
		
		Main.debug("Final endpit tally::  Player 0: " + player0Stones + " Player 1: " + player1Stones);
		if(player0Stones > player1Stones) {
			winner = 0;
		} else if(player1Stones > player0Stones) {
			winner = 1;
		} else {
			winner = -1;
		}
		//displayResult(winner);
	}
	
	
	/**
	 * Display the results of the game
	 * @param playerId
	 */
	private void displayResult(int playerId) {
		board.hideInfoBar(0);
		board.hideInfoBar(1);
		Main.debug("Winner is " + playerId);
		Platform.runLater(() -> {
			if(playerId == 0) {
				board.getBoardLayout().setBackground(new Background(new BackgroundFill(Color.rgb(153, 60, 56), null, null)));
				//board.addText("RED PLAYER WON!\n");
				board.winnerMessage("RED PLAYER WON!");
			} else if(playerId == 1) {
				board.getBoardLayout().setBackground(new Background(new BackgroundFill(Color.rgb(40, 142, 161), null, null)));
				//board.addText("BLUE PLAYER WON!\n");
				board.winnerMessage("BLUE PLAYER WON!");
			} else {
				board.getBoardLayout().setBackground(new Background(new BackgroundFill(Color.rgb(107, 94, 99), null, null)));
				//board.addText("TIE!\n");
				board.winnerMessage("IT'S A TIE!");
			}
		});
	}
	
	
	/**
	 * Once a winner is determined
	 * place all stones on players side
	 * into player pit
	 */
	private void captureRemainingStones() {
			Main.debug("Whos turn inside capture remaining " + whosTurn);
			ArrayList<Integer> player0Pits = board.getPlayerPits(0);
			ArrayList<Integer> player1Pits = board.getPlayerPits(1);
			int player0TotalStones = 0;
			int player1TotalStones = 0;
			for(int i = 0; i < player0Pits.size(); i++) {
				Pit pit0 = board.getPit(player0Pits.get(i));
				Pit pit1 = board.getPit(player1Pits.get(i));
				if(pit0.getType() == PIT_TYPE.PLAYER_PIT) {
					player0TotalStones += pit0.removeAll();
					player1TotalStones += pit1.removeAll();
				}
			}
			Main.debug("Player 0 stones captured: " + player0TotalStones + " Player 1 stones captures: " + player1TotalStones);
			Platform.runLater(() -> {
				for(int i = 0; i < player0Pits.size(); i++) {
					board.updatePitDisplay(player0Pits.get(i), 0);
					board.updatePitDisplay(player1Pits.get(i), 0);
				}
			});
			Pit end0Pit = board.getPit(player0Pits.get(player0Pits.size() - 1));
			Pit end1Pit = board.getPit(player1Pits.get(player1Pits.size() - 1));
			end0Pit.addStones(player0TotalStones);
			end1Pit.addStones(player1TotalStones);
			Platform.runLater(() -> {
				board.updatePitDisplay(player0Pits.get(player0Pits.size() - 1), end0Pit.count());
				board.updatePitDisplay(player1Pits.get(player1Pits.size() - 1), end1Pit.count());
			});
	}

}
