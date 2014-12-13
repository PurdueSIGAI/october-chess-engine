package com.nullprogram.chess.puai;

import java.util.HashMap;
import java.util.Iterator;

import com.nullprogram.chess.Board;
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

public class PUMiniMax implements Player {
	
	static final int END_DEPTH = 3;
	private HashMap<Class, Integer> values;
	private Game game;
	
	public PUMiniMax(Game game) {
		values = setUpValues();
		this.game = game;
	}

	@Override
	public Move takeTurn(Board board, Side side) {
		MoveScore moveScore=predictBestMove(board,0,side);
		System.out.println(moveScore);
		// TODO Auto-generated method stub
		return moveScore.getMove();
	}
	
	private MoveScore predictBestMove(Board board, int depth,Side side) {
		if (depth == END_DEPTH) {
			return new MoveScore(evaluateBoard(board, side)); // Evaluate
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
	
	
	/**
	 * Given a state of the board, evaluate the board with respect to the given side.
	 * @param board
	 * @param side
	 * @return
	 */
	private int evaluateBoard(Board board, Side side) {
		int myPoints = 0;
		int enemyPoints = 0;
		
		for (int i = 0; i < board.getWidth(); i++) {
			for (int j = 0; j < board.getHeight(); j++) {
				Piece p = board.getPiece(new Position(i, j));
				if (p != null) {
					if (p.getSide().equals(side)) {
						myPoints += getPieceValue(p);
					} else {
						enemyPoints += getPieceValue(p);
					}
				}
			}
		}
		return myPoints - enemyPoints;
	}
	
	private int getPieceValue(Piece p) {
		return values.get(p.getClass());
	}
	
	private HashMap<Class, Integer> setUpValues() {
		HashMap<Class, Integer> values = new HashMap<Class, Integer>();
		values.put(new Archbishop(null).getClass(), 4);
		values.put(new Bishop(null).getClass(), 3);
		values.put(new Chancellor(null).getClass(), 4);
		values.put(new King(null).getClass(), 1000);
		values.put(new Knight(null).getClass(), 3);
		values.put(new Pawn(null).getClass(), 1);
		values.put(new Queen(null).getClass(), 9);
		values.put(new Rook(null).getClass(), 5);
		return values;
	}
}
