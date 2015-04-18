package com.nullprogram.chess.matthewpai;

import com.nullprogram.chess.Board;
import com.nullprogram.chess.Move;

public class Transposition extends MoveScore {
	private Board board;
	private int depthDifference;
	
	public Transposition(double score, Move move, Board board, int depth) {
		super(score, move);
		this.board = board.copy();
		this.depthDifference = depth;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public int getDepthDifference() {
		return depthDifference;
	}

	public void setDepth(int depth) {
		this.depthDifference = depth;
	}
	
}
