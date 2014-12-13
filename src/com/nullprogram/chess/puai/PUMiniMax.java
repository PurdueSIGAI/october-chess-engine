package com.nullprogram.chess.puai;

import java.util.Iterator;

import com.nullprogram.chess.Board;
import com.nullprogram.chess.Move;
import com.nullprogram.chess.MoveList;
import com.nullprogram.chess.Piece;
import com.nullprogram.chess.Piece.Side;
import com.nullprogram.chess.Player;

public class PUMiniMax implements Player {
	
	final int endDepth = 3;

	@Override
	public Move takeTurn(Board board, Side side) {
		MoveScore moveScore=predictBestMove(board,0,side);
		System.out.println(moveScore);
		// TODO Auto-generated method stub
		return moveScore.getMove();
	}
	
	private MoveScore predictBestMove(Board board, int depth,Side side) {
		if (depth == endDepth) {
			return new MoveScore(0); // Evaluate
		} else {
			MoveList moveList = board.allMoves(side, true);
			Iterator<Move> i = moveList.iterator();
			MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth + 1,Piece.opposite(side));
				current.setMove(move);
				if (current.getScore() > bestMove.getScore()) {
					bestMove=current;
				}
				board.undo();
			}
			return bestMove;
		}
	}
	
	class MoveScore{

		int score;
		Move move;
		
		public MoveScore(int score) {
			this.score = score;
		}
		public void setMove(Move move) {
			this.move = move;
		}
		public int getScore() {
			return score;
		}
		public Move getMove() {
			return move;
		}
		
		public String toString() {
			return "MoveScore [score=" + score + ", move=" + move + "]";
		}
	}
	
}
