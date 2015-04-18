package com.nullprogram.chess.matthewpai;

import java.util.HashMap;

import com.nullprogram.chess.Board;
import com.nullprogram.chess.Move;

public class TranspositionTable {
	
	private static HashMap<Board, Transposition> transpositions = new HashMap<Board, Transposition>();
	
	public static boolean shouldUse(Board b, int depth, int endDepth) {
		if (b != null)
			if (transpositions.containsKey(b))
				return (transpositions.get(b).getDepth() >= endDepth - depth);
		return false;
	}
	
	public static MoveScore getValue(Board b) {
		Transposition trans = transpositions.get(b);
		double score = trans.getScore();
		Move move = trans.getMove();
		return new MoveScore(score, move);
	}
	
	public static Transposition getTransposition(Board b) {
		return transpositions.get(b);
	}
	
	public static void putValue(Board board, MoveScore ms, int depth, int endDepth) {
//		System.out.println(transpositions.size());
		transpositions.put(board, new Transposition(ms.getScore(), ms.getMove(), board, endDepth - depth));
	}
}
