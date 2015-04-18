package com.nullprogram.chess.matthewpai;

import com.nullprogram.chess.Move;

public class MoveScore{

	double score;
	Move move;
	private int depth;
	
	public MoveScore(double score, Move move) {
		super();
		this.score = score;
		this.move = move;
	}
	public MoveScore(double score, int depth) {
		this.score = score;
		this.depth = depth;
	}
	public MoveScore(double score) {
		this.score = score;
	}
	public void setMove(Move move) {
		this.move = move;
	}
	public double getScore() {
		return score;
	}
	public Move getMove() {
		return move;
	}
	
	public String toString() {
		return "MoveScore [score=" + score + ", move=" + move + "]";
	}
	public double getDepth() {
		return depth;
	}
}
