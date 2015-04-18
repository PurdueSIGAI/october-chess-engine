package com.nullprogram.chess.matthewpai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;

import com.nullprogram.chess.Board;
import com.nullprogram.chess.Chess;
import com.nullprogram.chess.Game;
import com.nullprogram.chess.Move;
import com.nullprogram.chess.MoveList;
import com.nullprogram.chess.Piece;
import com.nullprogram.chess.Piece.Side;
import com.nullprogram.chess.Player;
import com.nullprogram.chess.Position;
import com.nullprogram.chess.pieces.Archbishop;
import com.nullprogram.chess.pieces.Bishop;
import com.nullprogram.chess.pieces.Chancellor;
import com.nullprogram.chess.pieces.King;
import com.nullprogram.chess.pieces.Knight;
import com.nullprogram.chess.pieces.Pawn;
import com.nullprogram.chess.pieces.Queen;
import com.nullprogram.chess.pieces.Rook;

/**
 *  @author Matthew Page
 *  @date April 18 2015
 *  @version 3.0
 *  
 *  A chess AI for Purdue SIGART's chess AI competition
 *  
 *  General Searching Features Implemented:
 *  	- Minimax with Alpha Beta Pruning
 *   	- Iterative Deepening
 *  	- Transposition Tables
 */

public class MatthewPAI implements Player {
	
	private int endDepth;
	
	private boolean endTurn;

	private Timer timer;
	private AITimerTask timerTask;
	private HashMap<Class, Integer> values;
	private Game game;
	Side mySide;
	
	/**
	 * A constructor called at the beginning of the game
	 * @param game
	 */
	public MatthewPAI(Game game) {
		values = Evaluation.setUpValues();
		this.game = game;
		timer = new Timer();
	}

	@Override
	public Move takeTurn(Board board, Side side) {
		endTurn = false;
		mySide=side;
		// Schedule a timer for time completion
		timerTask = new AITimerTask(this);
		timer.schedule(timerTask, Chess.AI_MAX_TIME);
		endDepth = 0;
		
		
		
		MoveScore bestMove = new MoveScore(Integer.MIN_VALUE, endDepth);
		
		// Iteratively deepen the minimax search space until time runs out
		do {
			MoveScore moveScore=predictBestMove(board,0,side, Integer.MIN_VALUE, Integer.MAX_VALUE);
			
			if (endTurn) {
				break;
			}
			// We have searched the entire tree to endDepth, get rid of previous bestMove
			bestMove = new MoveScore(Integer.MIN_VALUE, endDepth);
			// Get the best move we have seen
			if (moveScore.getScore() > 1000)
				return moveScore.getMove();
			if (moveScore.getScore() > bestMove.getScore()) {
				bestMove = moveScore;
			}
			endDepth++;
		} while (!endTurn);
		timerTask.cancel();
		timer.purge();
		System.out.println("Search ended on ply " + (endDepth-1) + ".");
		System.out.println(bestMove);
		return bestMove.getMove();
	}
	
	private MoveScore predictBestMove(Board board, int depth, Side side, double alpha, double beta) {
		if (depth == endDepth || endTurn) {
			return new MoveScore(Evaluation.evaluateBoard(board, mySide), depth); // Evaluate
		}
		
		// If we are a minNode (side != mySide)
		if (side != mySide) {
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MAX_VALUE, depth);
			
			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(Evaluation.evaluateBoard(board, mySide), depth);
			}
			
			Iterator<Move> i = moveList.iterator();
			// Iterate through all the children
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				
				
/*				if (board.checkmate()) {
					board.undo();
					if (board.checkmate(mySide))
						return new MoveScore(Integer.MIN_VALUE, depth);
					else
						return new MoveScore(Integer.MAX_VALUE, depth);
				}
*/				
				
				MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
				/*if (current.getDepth() != endDepth) {
					board.undo();
					continue;
				}*/
				current.setMove(move);
				
				if (current.getScore() < beta) {
					beta = current.getScore();
					bestMove = current;
				}
				
				board.undo();
				
				if (beta <= alpha) {
					break;
				}
			}
			return bestMove;
		} else {
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MIN_VALUE, depth);
			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(Evaluation.evaluateBoard(board, mySide), depth);
			}
			
			Iterator<Move> i = moveList.iterator();
			// Iterate through all the children
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				
				
/*				if (board.checkmate()) {
					board.undo();
					if (board.checkmate(mySide))
						return new MoveScore(Integer.MAX_VALUE, depth);
					else
						return new MoveScore(Integer.MIN_VALUE, depth);
				}
*/				
				
				MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
				/*if (current.getDepth() != endDepth) {
					board.undo();
					continue;
				}*/
				current.setMove(move);
				
				if (current.getScore() > alpha) {
					alpha = current.getScore();
					bestMove = current;
				}
				
				board.undo();
				
				if (beta <= alpha) {
					break;
				}
			}
			return bestMove;
		}
	}	
	
	public boolean isEndTurn() {
		return endTurn;
	}

	public void setEndTurn(boolean endTurn) {
		this.endTurn = endTurn;
	}
}