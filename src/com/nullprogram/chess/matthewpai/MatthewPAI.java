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

public class MatthewPAI implements Player {
	
	private int endDepth;
	
	private boolean endTurn;

	private Timer timer;
	private AITimerTask timerTask;
	private HashMap<Class, Integer> values;
	private Game game;
	Side mySide;
	
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
		endDepth = 1;
		
		MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);
		
		
		// Iteratively deepen the minimax search space until time runs out
		do {
			MoveScore moveScore=predictBestMove(board,0,side, Integer.MIN_VALUE, Integer.MAX_VALUE);
			// We have searched the entire tree to endDepth, get rid of previous bestMove
			// This allows us to compare only full trees and the depth we are currently searching if the turn ends
			if (!endTurn) {
				bestMove = new MoveScore(Integer.MIN_VALUE);
			}
			// Get the best move we have seen
			if (moveScore.getScore() > bestMove.getScore()) {
				bestMove = moveScore;
			}
			endDepth++;
		} while (!endTurn);
		
		timerTask.cancel();
		timer.purge();
		System.out.println("Search ended on ply " + endDepth + ".");
		System.out.println(bestMove);
		return bestMove.getMove();
	}
		
	private MoveScore predictBestMove(Board board, int depth, Side side, double alpha, double beta) {
		if (TranspositionTable.shouldUse(board, depth, endDepth))
			return TranspositionTable.getValue(board);
		
		if (depth == endDepth || endTurn) {
			return new MoveScore(Evaluation.evaluateBoard(board, mySide)); // Evaluate
		}
		
		boolean isMinNode = (side != mySide);
		// Store our best move
		MoveScore bestMove = new MoveScore(isMinNode ? Integer.MAX_VALUE : Integer.MIN_VALUE);
		
		// Get all the children
		MoveList moveList = board.allMoves(side, true);
		if (moveList.isEmpty()) {
			// There are no children, so evaluate and return
			return new MoveScore(Evaluation.evaluateBoard(board, side));
			
			//
			//
			//
			//
			// return new MoveScore(Evaluation.evaluateBoard(board, mySide));
		}
		Iterator<Move> i = moveList.iterator();
		// Iterate through all the children
		while (i.hasNext()) {
			Move move = i.next();
			board.move(move);
			MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
			current.setMove(move);
			
			if (isMinNode ? current.getScore() < beta : current.getScore() > alpha) {
				if (isMinNode)
					beta = current.getScore();
				else
					alpha = current.getScore();
				bestMove = current;
			}
			
			board.undo();
			
			if (beta <= alpha) {
				break;
			}
		}
		TranspositionTable.putValue(board, bestMove, depth, endDepth);
		return bestMove;
	}
	
	public boolean isEndTurn() {
		return endTurn;
	}

	public void setEndTurn(boolean endTurn) {
		this.endTurn = endTurn;
	}
}
