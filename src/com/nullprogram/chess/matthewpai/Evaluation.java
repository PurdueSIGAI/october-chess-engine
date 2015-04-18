package com.nullprogram.chess.matthewpai;

import java.util.HashMap;

import com.nullprogram.chess.Board;
import com.nullprogram.chess.MoveList;
import com.nullprogram.chess.Piece;
import com.nullprogram.chess.Position;
import com.nullprogram.chess.Piece.Side;
import com.nullprogram.chess.pieces.Archbishop;
import com.nullprogram.chess.pieces.Bishop;
import com.nullprogram.chess.pieces.Chancellor;
import com.nullprogram.chess.pieces.King;
import com.nullprogram.chess.pieces.Knight;
import com.nullprogram.chess.pieces.Pawn;
import com.nullprogram.chess.pieces.Queen;
import com.nullprogram.chess.pieces.Rook;

/**
 * Given a state of the board, evaluate the board with respect to the given side.
 * @param board
 * @param side
 * @return
 */

public class Evaluation {
	private static HashMap<Class, Integer> values;
	
	
	
	
	
	
	
	
	
	

	
	static double material = 1;
	static double pawns = 0.5;
	static double mobility = 0.1;
	static double kingSafety = 0.5;
	
	

	public static double evaluateBoard(Board board, Side side) {
		
		if (board.checkmate(side)) {
			return -10000000;
		} else if (board.checkmate(side.value() == side.WHITE.value() ? side.BLACK : side.WHITE))
			return 10000000;
		
		double myPoints = 0;
		double enemyPoints = 0;
		
		for (int i = 0; i < board.getWidth(); i++) {
			for (int j = 0; j < board.getHeight(); j++) {
				Piece p = board.getPiece(new Position(i, j));
				if (p != null) {
					if (p.getSide().equals(side)) {
						myPoints += material * getPieceValue(p);
//						if (p instanceof Pawn)
//							myPoints += pawns * pawnSpecial(p, i, j, board, side);
						if (!(p instanceof Queen))
							myPoints += mobility * p.getMoves(false).size();
						if (p instanceof King)
							myPoints += kingSafety * p.getMoves(true).size();
					} else {
						enemyPoints += material * getPieceValue(p);
//						if (p instanceof Pawn)
//							enemyPoints += pawns * pawnSpecial(p, i, j, board, side);
						if (!(p instanceof Queen))
							enemyPoints += mobility * p.getMoves(false).size();
						if (p instanceof King)
							enemyPoints += kingSafety * p.getMoves(true).size();
					}
				}
			}
		}
		double runningPoints = myPoints - enemyPoints;
		
		if (runningPoints < 0 && (board.stalemate() || board.threeFold() || board.fiftyMoveRule())) {
			runningPoints += 1 * (myPoints / enemyPoints);
		} else if (runningPoints > 0 && (board.stalemate() || board.threeFold() || board.fiftyMoveRule())) {
			runningPoints -= 2 * (enemyPoints / myPoints);
		}
		
		if (board.checkmate(side)) {
			runningPoints -= 10000000;
		} else if (board.checkmate(side.value() == side.WHITE.value() ? side.BLACK : side.WHITE))
			runningPoints += 10000000;
		
		// Calculate Center
		for (int ac = 3; ac < 5; ac++)
			for (int ca = 3; ca < 5; ca++)
				runningPoints += calculateCenter(ac, ca, side);

		return runningPoints;
	}	
	
	private static double calculateCenter(int n, int m, Side side) {
		double runningPoints = 0;
		if (board == null || side == null)
			return 0;
		if (board.getPiece(new Position(n, m)) != null) {
			int asd = board.getPiece(new Position(n, m)).getSide().value();
			int bsd = side.value();
			if (asd == bsd)
				runningPoints += 0.5;
		} else if (board.getPiece(new Position(n, m)) != null)
			runningPoints -= 0.5;
		return runningPoints;
	}
	
	
	private static int i;
	private static int j;
	private static Board board;
	
	private static double pawnSpecial(Piece p, int i, int j, Board board, Side side) {
//		Evaluation.i = i;
//		Evaluation.j = j;
//		Evaluation.board = board;
		if (p.getSide() == Side.WHITE)
			return (0.1) * (j - 2);
		else
			return (0.1) * (6 - j);
//		double tally = 0;
//		tally += doubledPawns(p);
//		tally += blockedPawns(p);
//		tally += isolatedPawns(p);
//		return tally;
	}

	@Deprecated
	private static double isolatedPawns(Piece p) {
		for (int abc = 0; abc < 7; abc++) {
			if (i > 0) {
				Piece l = board.getPiece(new Position(i - 1, abc));
				if (l != null && l.getSide().equals(p.getSide()) && l instanceof Pawn)
					return 0;
			}
			if (i < 7) {
				Piece r = board.getPiece(new Position(i + 1, abc));
				if (r != null && r.getSide().equals(p.getSide()) && r instanceof Pawn)
					return 0;
			}
		}
		return 0.5;
	}

	@Deprecated
	private static double blockedPawns(Piece p) {
		Piece w;
		if (p.getSide().equals(Side.WHITE))
			w = board.getPiece(new Position(i, j + 1));
		else
			w = board.getPiece(new Position(i, j - 1));
		if (w != null && !w.getSide().equals(p.getSide()))
			return 0.5;
		else
			return 0;
	}

	@Deprecated
	private static double doubledPawns(Piece p) {
		double tally = 0;
		for (int x = 0; x < 8; x++) {
			if (x == j)
				continue;
			Piece n = board.getPiece(new Position(i, x));
			if (n != null && n.getSide().equals(p.getSide()) && n instanceof Pawn)
				tally += 0.5;
		}
		return 0;
	}

	private static int getPieceValue(Piece p) {
		try {
			return values.get(p.getClass());
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static HashMap<Class, Integer> setUpValues() {
		values = new HashMap<Class, Integer>();
		values.put(Archbishop.class, 4);
		values.put(Bishop.class, 3);
		values.put(Chancellor.class, 4);
		values.put(King.class, 1000);
		values.put(Knight.class, 3);
		values.put(Pawn.class, 1);
		values.put(Queen.class, 9);
		values.put(Rook.class, 5);
		return values;
	}

}
