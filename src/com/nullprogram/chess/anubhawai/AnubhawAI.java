package com.nullprogram.chess.anubhawai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

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

public class AnubhawAI implements Player {

	private boolean endTurn;

	private Timer timer;
	private AITimerTask timerTask;
	private HashMap<Class, Integer> values;
	private Game game;
	Side mySide;
	private final Executor executor = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors());
	MoveScore finalBestMove;

	public AnubhawAI(Game game) {
		values = setUpValues();
		this.game = game;
		timer = new Timer();
	}

	@Override
	public Move takeTurn(Board board, Side side) {
		endTurn = false;
		mySide = side;
		// Schedule a timer for time completion
		timerTask = new AITimerTask(this);
		timer.schedule(timerTask, Chess.AI_MAX_TIME);
		int endDepth = 1;

		MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);

		// Iteratively deepen the minimax search space until time runs out
		do {
			// MoveScore moveScore = predictBestMove(board, 0, side,
			// Integer.MIN_VALUE, Integer.MAX_VALUE, endDepth);
			MoveScore moveScore = parallelPrediction(board, 0, side,
					Integer.MIN_VALUE, Integer.MAX_VALUE, endDepth);
			if (endTurn) {
				break;
			}
			// We have searched the entire tree to endDepth, get rid of previous
			// bestMove
			bestMove = new MoveScore(Integer.MIN_VALUE);
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

	private MoveScore parallelPrediction(Board board, final int depth, final Side side,
			final double alpha, final double beta, final int finalDepth) {
		MoveList moves = board.allMoves(side, true);
		CompletionService<MoveScore> service = new ExecutorCompletionService<MoveScore>(
				executor);
		finalBestMove = new MoveScore(Integer.MIN_VALUE);
		for (final Move move : moves) {
			final Board callboard = board.copy();
			service.submit(new Callable<MoveScore>() {
				public MoveScore call() {
					callboard.move(move);
					MoveScore current = predictBestMove(callboard, depth + 1,
							Piece.opposite(side), alpha, beta, finalDepth);
					current.setMove(move);
					return current;
				}
			});
		}
		finalBestMove = new MoveScore(Integer.MIN_VALUE);
		for (int i = 0; i < moves.size(); i++) {
			try {
				MoveScore curScore = service.take().get();
				if (curScore != null
						&& curScore.getScore() > finalBestMove.getScore()) {
					finalBestMove = curScore;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return finalBestMove;
	}

	private MoveScore predictBestMove(Board board, int depth, Side side,
			double alpha, double beta, int finalDepth) {
		if (depth == finalDepth || endTurn) {
			return new MoveScore(evaluateBoard(board, mySide)); // Evaluate
		}

		/*
		 * if minNode for every kid if kid < beta beta = kid if beta <= alpha
		 * break return bestMove - beta if maxNode for every kid if kid > alpha
		 * alpha = kid if beta <= alpha break return bestMove - alpha
		 */

		// If we are a minNode (side != mySide)
		if (side != mySide) {
			// System.out.println("min player");
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MAX_VALUE);

			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(evaluateBoard(board, mySide));
			}

			Iterator<Move> i = moveList.iterator();
			// Iterate through all the children
			// System.out.printf("alpha %d, beta %d\n", alpha, beta);
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth + 1,
						Piece.opposite(side), alpha, beta, finalDepth);
				current.setMove(move);
				// System.out.println("  " + current);

				if (current.getScore() < beta) {
					beta = current.getScore();
					bestMove = current;
				}

				board.undo();

				if (beta <= alpha) {
					break;
				}
			}
			// System.out.println("best " + bestMove);
			return bestMove;
		} else {
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);
			// System.out.println("max player");
			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(evaluateBoard(board, mySide));
			}

			Iterator<Move> i = moveList.iterator();
			// Iterate through all the childrenkingInsafetyValueSide
			// System.out.printf("depth %d, alpha %d, beta %d\n", depth, alpha,
			// beta);
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth + 1,
						Piece.opposite(side), alpha, beta, finalDepth);
				current.setMove(move);
				// System.out.println("  " + current);

				if (current.getScore() > alpha) {
					alpha = current.getScore();
					bestMove = current;
				}

				board.undo();

				if (beta <= alpha) {
					break;
				}
			}
			// System.out.println("best " + bestMove);
			return bestMove;
		}
	}

	private MoveScore predictBestMove(Board board, int depth, Side side,
			int finalDepth) {
		if (depth == finalDepth || endTurn) {
			return new MoveScore(evaluateBoard(board, mySide)); // Evaluate
		} else {
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				return new MoveScore(evaluateBoard(board, mySide));
			}
			Iterator<Move> i = moveList.iterator();
			MoveScore bestMove = null;
			if (side == mySide) {
				bestMove = new MoveScore(Integer.MIN_VALUE);
			} else {
				bestMove = new MoveScore(Integer.MAX_VALUE);
			}
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth + 1,
						Piece.opposite(side), finalDepth);
				current.setMove(move);
				if (side == mySide) {
					if (current.getScore() > bestMove.getScore()) {
						bestMove = current;
					} else if (current.getScore() == bestMove.getScore()
							&& Math.random() < 0.3) {
						bestMove = current;
					}
				} else {
					if (current.getScore() < bestMove.getScore()) {
						bestMove = current;
					} else if (current.getScore() == bestMove.getScore()
							&& Math.random() < 0.3) {
						bestMove = current;
					}
				}
				board.undo();
			}
			return bestMove;
		}
	}

	class MoveScore {

		double score;
		Move move;

		public MoveScore(double score) {
			this.score = score;
		}

		public MoveScore(double score, Move move) {
			this.score = score;
			this.move = move;
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
	}

	private double evaluateBoard(final Board b, Side side) {
		double material = materialValue(b, side);
		double kingSafety = kingInsafetyValue(b, side);
		double mobility = mobilityValue(b, side);
		double checkMateMe = b.checkmate(side) ? -1000 : 0;
		double checkMateThem = b.checkmate(Piece.opposite(side)) ? 1000 : 0;
		return material * 1.0 + kingSafety * 0.15 + mobility * .01
				+ checkMateMe + checkMateThem;
	}

	/**
	 * Add up the material value of the board only.
	 *
	 * @param b
	 *            board to be evaluated
	 * @return material value of the board
	 */
	private double materialValue(Board b, Side side) {
		double value = 0;
		for (int y = 0; y < b.getHeight(); y++) {
			for (int x = 0; x < b.getWidth(); x++) {
				Position pos = new Position(x, y);
				Piece p = b.getPiece(pos);
				if (p != null) {
					value += values.get(p.getClass()) * p.getSide().value();
				}
			}
		}
		return value * side.value();
	}

	/**
	 * Determine the safety of each king. Higher is worse.
	 *
	 * @param b
	 *            board to be evaluated
	 * @return king insafety score
	 */
	private double kingInsafetyValue(final Board b, Side side) {
		return kingInsafetyValueSide(b, Piece.opposite(side))
				- kingInsafetyValueSide(b, side);
	}

	/**
	 * Helper function: determine safety of a single king.
	 *
	 * @param b
	 *            board to be evaluated
	 * @param s
	 *            side of king to be checked
	 * @return king insafety score
	 */
	private double kingInsafetyValueSide(final Board b, Side side) {
		/* Trace lines away from the king and count the spaces. */
		Position king = b.findKing(side);
		if (king == null) {
			/* Weird, but may happen during evaluation. */
			return Double.POSITIVE_INFINITY;
		}
		MoveList list = new MoveList(b, false);
		/* Take advantage of the Rook and Bishop code. */
		Rook.getMoves(b.getPiece(king), list);
		Bishop.getMoves(b.getPiece(king), list);
		return list.size();
	}

	/**
	 * Mobility score for this board.
	 *
	 * @param b
	 *            board to be evaluated
	 * @return score for this board
	 */
	private double mobilityValue(final Board b, Side side) {
		return b.allMoves(side, false).size()
				- b.allMoves(Piece.opposite(side), false).size();
	}

	private int getPieceValue(Piece p) {
		return values.get(p.getClass());
	}

	private HashMap<Class, Integer> setUpValues() {
		HashMap<Class, Integer> values = new HashMap<Class, Integer>();
		values.put(Archbishop.class, 4);
		values.put(Bishop.class, 3);
		values.put(Chancellor.class, 4);
		values.put(King.class, 1000);
		values.put(Knight.class, 3);
		values.put(Pawn.class, 1);
		values.put(Queen.class, 20);
		values.put(Rook.class, 5);
		return values;
	}

	public boolean isEndTurn() {
		return endTurn;
	}

	public void setEndTurn(boolean endTurn) {
		this.endTurn = endTurn;
	}
}
