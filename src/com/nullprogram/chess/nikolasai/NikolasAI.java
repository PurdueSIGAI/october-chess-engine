package com.nullprogram.chess.nikolasai;

import java.util.ArrayList;
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

/**
 * Nikolas Ogg's AI
 */

public class NikolasAI implements Player {
	
	private int endDepth;
	
	private boolean endTurn;

	private Timer timer;
	private AITimerTask timerTask;
	private HashMap<Class, Integer> values;
	private Game game;
	Side mySide;
	
	/** Thread manager. */
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public NikolasAI(Game game) {
		values = setUpValues();
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
		
//		MoveScore bestMove = predictBestMove(board,0,side, Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		
		MoveScore bestMove = new MoveScore(Integer.MIN_VALUE, endDepth);
		
		// Iteratively deepen the minimax search space until time runs out
		do {
			MoveScore moveScore=runThreads(board,0,side, Integer.MIN_VALUE, Integer.MAX_VALUE);
//			System.out.println(moveScore.getDepth() + " " + endDepth + " " + endTurn);
			
			// Check for early checkmates
			if (moveScore.getScore() > 500) {
				bestMove = moveScore;
				break;
			}
			
			if (endTurn) {
				break;
			}
			// We have searched the entire tree to endDepth, get rid of previous bestMove
			bestMove = new MoveScore(Integer.MIN_VALUE, endDepth);
			// Get the best move we have seen
			if (moveScore.getScore() > bestMove.getScore()) {
				bestMove = moveScore;
			}
			endDepth++;
		} while (!endTurn);
		
		timerTask.cancel();
		timer.purge();
		System.out.println("[NikolasAI] Search ended on ply " + (endDepth) + ".");
		System.out.println(bestMove);
		return bestMove.getMove();
	}
	
	private MoveScore runThreads(final Board board, int depth, final Side side, double alpha, double beta) {
		
		/* Spin off threads to evaluate each move's tree. */
        CompletionService<MoveScore> service =
            new ExecutorCompletionService<MoveScore>(executor);
        
        MoveList moves = board.allMoves(side, true);
        moves.shuffle();
        
        int submitted = 0;
        for (final Move move : moves) {
            final Board callboard = board.copy();
            service.submit(new Callable<MoveScore>() {
                public MoveScore call() {
                    callboard.move(move);
                    MoveScore score = predictBestMove(callboard,0,Piece.opposite(side), Integer.MIN_VALUE, Integer.MAX_VALUE);
                    score.setMove(move);
                    return score;
                }
            });
            submitted++;
        }

        /* Gather up results and pick the best move. */
        MoveScore best = new MoveScore(Double.NEGATIVE_INFINITY, endDepth);
        for (int i = 0; i < submitted; i++) {
            try {
                MoveScore m = service.take().get();
                if (best == null || m.getScore() > best.getScore()) {
                	best = m;
                }
            } catch (ExecutionException e) {
                System.out.println("move went unevaluated: " + e.getMessage());
            } catch (InterruptedException e) {
            	System.out.println("move went unevaluated: " + e.getMessage());
            }
        }
		
		
		return best;
	}
	
	
	private MoveScore predictBestMove(Board board, int depth, Side side, double alpha, double beta) {
		if (depth == endDepth || endTurn) {
//			System.out.println("depth: " + depth);
//			System.out.println("we actually evaluated this, right?");
			return new MoveScore(evaluateBoard(board, mySide), depth); // Evaluate
		}
		
		/*
		 * if minNode
		 * 	for every kid
		 * 		if kid < beta
		 * 			beta = kid
		 *		if beta <= alpha
		 *			break
		 *	return bestMove   - beta
		 * if maxNode
		 * 	for every kid
		 * 		if kid > alpha
		 * 			alpha = kid
		 * 		if beta <= alpha
		 * 			break
		 * 	return bestMove   - alpha
		 */
		
		// If we are a minNode (side != mySide)
		if (side != mySide) {
//			System.out.println("min player");
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MAX_VALUE, depth);
			
			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(evaluateBoard(board, mySide), depth);
			}
			
			Iterator<Move> i = moveList.iterator();
			// Iterate through all the children
//			System.out.printf("alpha %d, beta %d\n", alpha, beta);
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
				/*if (current.getDepth() != endDepth) {
					board.undo();
					continue;
				}*/
//				System.out.println("sadf");
				current.setMove(move);
//				System.out.println("  " + current);
				
				if (current.getScore() < beta) {
					beta = current.getScore();
					bestMove = current;
				}
				
				board.undo();
				
				if (beta <= alpha) {
					break;
				}
			}
//			System.out.println("best " + bestMove);
			return bestMove;
		} else {
			// Store our best move
			MoveScore bestMove = new MoveScore(Integer.MIN_VALUE, depth);
//			System.out.println("max player");
			// Get all the children
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				// There are no children, so evaluate and return
				return new MoveScore(evaluateBoard(board, mySide), depth);
			}
			
			Iterator<Move> i = moveList.iterator();
			// Iterate through all the children
//			System.out.printf("depth %d, alpha %d, beta %d\n", depth, alpha, beta);
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
				/*if (current.getDepth() != endDepth) {
//					System.out.println(current.getDepth() + " " + endDepth);
					board.undo();
					continue;
				}*/
//				System.out.println("fdsa");
				current.setMove(move);
//				System.out.println("  " + current);
				
				if (current.getScore() > alpha) {
					alpha = current.getScore();
					bestMove = current;
				}
				
				board.undo();
				
				if (beta <= alpha) {
					break;
				}
			}
//			System.out.println("best " + bestMove);
			return bestMove;
		}
	}
	
	private MoveScore predictBestMove(Board board, int depth,Side side) {
		if (depth == endDepth || endTurn) {
			return new MoveScore(evaluateBoard(board, mySide), depth); // Evaluate
		} else {
			MoveList moveList = board.allMoves(side, true);
			if (moveList.isEmpty()) {
				return new MoveScore(evaluateBoard(board, mySide), depth);
			}
			Iterator<Move> i = moveList.iterator();
			MoveScore bestMove = null;
			if(side==mySide){
				bestMove=new MoveScore(Integer.MIN_VALUE, depth);
			}else{
				bestMove=new MoveScore(Integer.MAX_VALUE, depth);
			}
			while (i.hasNext()) {
				Move move = i.next();
				board.move(move);
				MoveScore current = predictBestMove(board, depth + 1,Piece.opposite(side));
				current.setMove(move);
				if(side==mySide){
					if (current.getScore() > bestMove.getScore()) {
						bestMove=current;
					} else if (current.getScore() == bestMove.getScore() && Math.random() < 0.3) {
						bestMove=current;
					}
				}else{
					if (current.getScore() < bestMove.getScore()) {
						bestMove=current;
					} else if (current.getScore() == bestMove.getScore() && Math.random() < 0.3) {
						bestMove=current;
					}
				}
				board.undo();
			}
			return bestMove;
		}
	}
	
	class MoveScore{

		double score;
		Move move;
		int depth;
		
		
		public MoveScore(double score, int depth) {
			this.score = score;
			this.depth = depth;
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
		public int getDepth() {
			return depth;
		}
		public void setDepth(int depth) {
			this.depth = depth;
		}
		
		public String toString() {
			return "MoveScore [score=" + score + ", move=" + move + ", depth=" + depth + "]";
		}
	}
	
	// Weights
	private static final double MATERIAL_WEIGHT = 1.0;
	private static final double KING_SAFETY_WEIGHT = 0.15;
	private static final double MOBILITY_WEIGHT = 0.01;
	private static final double PAWN_WEIGHT = 0.1;
	
	/**
	 * Given a state of the board, evaluate the board with respect to the given side.
	 * @param board
	 * @param side
	 * @return
	 */
	private double evaluateBoard(Board board, Side side) {
		/*
		 * Evaluation ideas:
		 * 	available moves
		 *  how well the kings can move & king safety
		 *  area of board under control
		 *  center control
		 *  pawn distance to other side (straight line distance, if stuff is in the way it doesn't count)
		 *  checkmates
		 *  checks
		 *  
		 *  Avoiding / preferring ties
		 */
		ArrayList<Piece> myPieces = new ArrayList<Piece>();
		ArrayList<Piece> theirPieces = new ArrayList<Piece>();
		for (int i = 0; i < board.getWidth(); i++) {
			for (int j = 0; j < board.getHeight(); j++) {
				Piece p = board.getPiece(new Position(i, j));
				if (p != null) {
					if (p.getSide().equals(side)) {
						myPieces.add(p);
					} else {
						theirPieces.add(p);
					}
				}
			}
		}
		
		double runningPoints = 0;
		
		// Calculate the value of the pieces on the board
		double materialValue = getMaterialScore(board, side, myPieces, theirPieces);
		runningPoints += materialValue * MATERIAL_WEIGHT;
		
		double kingSafety = getKingSafetyScore(board, side);
		runningPoints += kingSafety * KING_SAFETY_WEIGHT;
		
		double mobility = getMobilityScore(board, side);
		runningPoints += mobility * MOBILITY_WEIGHT;
		
		double pawn = getPawnScore(board, myPieces, theirPieces);
		runningPoints += pawn * PAWN_WEIGHT;
		
		
		// If we are losing, favor ties
		// If we are winning, try to avoid ties
		if (runningPoints < 0 && (board.stalemate() || board.threeFold())) {
			runningPoints += 1;
		} else if (runningPoints > 0 && (board.stalemate() || board.threeFold())) {
			runningPoints -= 2;
		}
		
		if (board.checkmate(side)) {
			runningPoints -= 10000;
		}
		if (board.checkmate(Piece.opposite(side))) {
			runningPoints += 10000;
		}
		return runningPoints;
	}
	
	/**
	 * Returns a difference of the value of pieces on the board
	 * @param board
	 * @param side
	 * @return
	 */
	private double getMaterialScore(Board board, Side side, ArrayList<Piece> myPieces, ArrayList<Piece> theirPieces) {
		double myPoints = 0;
		double enemyPoints = 0;
		
		for (Piece p : myPieces) {
			myPoints += getPieceValue(p);
		}
		for (Piece p : theirPieces) {
			enemyPoints += getPieceValue(p);
		}
		return myPoints - enemyPoints;
	}
	
	/**
	 * Get the king safety score with respect to the side
	 * @param board
	 * @param side
	 * @return
	 */
	private double getKingSafetyScore(Board board, Side side) {
		return getKingSafetyScoreSide(board, Piece.opposite(side)) - getKingSafetyScoreSide(board, side);
	}
	
	/**
	 * Return a measure of king safety for a given side
	 * To do this, check how many pieces can attack the king currently.
	 * Then check for all the possible moves of the king how many pieces can attack that square.
	 * @param board
	 * @param side
	 * @return
	 */
	private double getKingSafetyScoreSide(Board board, Side side) {
		Position kingPos = board.findKing(side);
		if (kingPos == null) {
			return 0;
		}
		Piece king = board.getPiece(kingPos);
//		MoveList list = new MoveList(board, false);
//		Rook.getMoves(king, list);
//		Bishop.getMoves(king, list);
//		return list.size();
		return king.getMoves(true).size();
//		int numImmediateAttackers = 0;
//		Side attacker = Piece.opposite(side);
//		if (board.check(side)) {
//			// We are in check, find all pieces that are causing it
//	        numImmediateAttackers += getNumAttackers(kingPos, board, attacker);
//		}
//		Piece king = board.getPiece(kingPos);
//		int numFutureAttackers = 0;
//		for (Move move : king.getMoves(false)) {
//			board.move(move);
//			numFutureAttackers += getNumAttackers(king.getPosition(), board, attacker);
//			board.undo();
//		}
////		System.out.println("Current attackers: " + numImmediateAttackers);
////		System.out.println("Future attackers: " + numFutureAttackers);
//		return numImmediateAttackers + 0.5 * numFutureAttackers;
	}
	
	/**
	 * Get the difference in mobilty score
	 * This is scaled to be between -1 and 1
	 * @param board
	 * @param side
	 * @return
	 */
	private double getMobilityScore(Board board, Side side) {
		
		double rawScore = board.allMoves(side, false).size() - board.allMoves(Piece.opposite(side), false).size();
		//double finalScore = rawScore / (5.0 + Math.abs(rawScore));
		return rawScore;
	}
	
	private double getPawnScore(Board board, ArrayList<Piece> myPieces, ArrayList<Piece> theirPieces) {
		double score = 0;
		
		for (Piece p : myPieces) {
			if (!(p instanceof Pawn)) {
				continue;
			}
			int count = 0;
			boolean open = true;
			if (mySide.equals(Piece.Side.BLACK)) {
				Position pos = p.getPosition();
				for (int i = 0; i < pos.getY(); i++) {
					Piece o = board.getPiece(new Position(pos.getX(), pos.getY()));
					if (o != null && o.getSide().equals(Piece.opposite(mySide))) {
						open = false;
					}
					count++;
				}
			} else {
				Position pos = p.getPosition();
				for (int i = pos.getY() + 1; i < board.getHeight(); i++) {
					Piece o = board.getPiece(new Position(pos.getX(), pos.getY()));
					if (o != null && o.getSide().equals(Piece.opposite(mySide))) {
						open = false;
					}
					count++;
				}
			}
			if (open) {
				score += (8 - count);
			}
		}
		
		Side theirSide = Piece.opposite(mySide);
		for (Piece p : theirPieces) {
			if (!(p instanceof Pawn)) {
				continue;
			}
			int count = 0;
			boolean open = true;
			if (theirSide.equals(Piece.Side.BLACK)) {
				Position pos = p.getPosition();
				for (int i = 0; i < pos.getY(); i++) {
					Piece o = board.getPiece(new Position(pos.getX(), pos.getY()));
					if (o != null && o.getSide().equals(Piece.opposite(theirSide))) {
						open = false;
					}
					count++;
				}
			} else {
				Position pos = p.getPosition();
				for (int i = pos.getY() + 1; i < board.getHeight(); i++) {
					Piece o = board.getPiece(new Position(pos.getX(), pos.getY()));
					if (o != null && o.getSide().equals(Piece.opposite(theirSide))) {
						open = false;
					}
					count++;
				}
			}
			if (open) {
				score -= (8 - count);
			}
		}
		return score;
	}
	
	/**
	 * Get the number of pieces of a side able to attack a position
	 * @param pos
	 * @param board
	 * @param side
	 * @return
	 */
	private int getNumAttackers(Position pos, Board board, Side side) {
		int numAttackers = 0;
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Piece p = board.getPiece(new Position(x, y));
                if ((p != null) &&
                    (p.getSide() == side) &&
                    p.getMoves(false).containsDest(pos)) {

                    numAttackers++;
                }
            }
        }
        return numAttackers;
	}
	
	private int getPieceValue(Piece p) {
		return values.get(p.getClass());
	}
	
	private HashMap<Class, Integer> setUpValues() {
		HashMap<Class, Integer> values = new HashMap<Class, Integer>();
		values.put(Archbishop.class, 6);
		values.put(Bishop.class, 3);
		values.put(Chancellor.class, 8);
		values.put(King.class, 1000);
		values.put(Knight.class, 3);
		values.put(Pawn.class, 1);
		values.put(Queen.class, 9);
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
