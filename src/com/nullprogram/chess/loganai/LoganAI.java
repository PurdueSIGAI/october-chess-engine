package com.nullprogram.chess.loganai;

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

public class LoganAI implements Player {

    private int endDepth;
    private boolean endTurn;
    private int turnNumber;
    
    private int materialScore;
    private int pawnRaceScore;
    
    private static final int SCORE_CHECKMATE = 9313;

    private Timer timer;
    private AITimerTask timerTask;
    private HashMap<Class, Integer> values;
    private Game game;
    Side mySide;
    public LoganAI(Game game) {
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
        endDepth = 1;

        MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);

        // Iteratively deepen the minimax search space until time runs out
        do {
            MoveScore moveScore=predictBestMove(board,0,side, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (endTurn) {
                break;
            }
            // We have searched the entire tree to endDepth, get rid of previous bestMove
            bestMove = new MoveScore(Integer.MIN_VALUE);
            // Get the best move we have seen
            if (moveScore.getScore() > bestMove.getScore()) {
                bestMove = moveScore;
            }
            if (bestMove.getScore() == SCORE_CHECKMATE) {
            	break;
            }
            
            endDepth++;
        } while (!endTurn);

        timerTask.cancel();
        timer.purge();
        turnNumber++;
        System.out.println("[LOGAN AI] Search ended on ply " + endDepth + " with a score of " + bestMove.getScore());
        return bestMove.getMove();
    }

    private MoveScore predictBestMove(Board board, int depth, Side side, double alpha, double beta) {
        if (depth == endDepth || endTurn) {
            return new MoveScore(evaluateBoard(board, mySide)); // Evaluate
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
            MoveScore bestMove = new MoveScore(Integer.MAX_VALUE);

            // Get all the children
            MoveList moveList = board.allMoves(side, true);
            if (moveList.isEmpty()) {
                // There are no children, so evaluate and return
                return new MoveScore(evaluateBoard(board, mySide));
            }

            Iterator<Move> i = moveList.iterator();
            // Iterate through all the children
            //			System.out.printf("alpha %d, beta %d\n", alpha, beta);
            while (i.hasNext()) {
                Move move = i.next();
                board.move(move);;
                MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
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
            MoveScore bestMove = new MoveScore(Integer.MIN_VALUE);
            //			System.out.println("max player");
            // Get all the children
            MoveList moveList = board.allMoves(side, true);
            if (moveList.isEmpty()) {
                // There are no children, so evaluate and return
                return new MoveScore(evaluateBoard(board, mySide));
            }

            Iterator<Move> i = moveList.iterator();
            // Iterate through all the children
            //			System.out.printf("depth %d, alpha %d, beta %d\n", depth, alpha, beta);
            while (i.hasNext()) {
                Move move = i.next();
                board.move(move);;
                MoveScore current = predictBestMove(board, depth+1, Piece.opposite(side), alpha, beta);
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
            return new MoveScore(evaluateBoard(board, mySide)); // Evaluate
        } else {
            MoveList moveList = board.allMoves(side, true);
            if (moveList.isEmpty()) {
                return new MoveScore(evaluateBoard(board, mySide));
            }
            Iterator<Move> i = moveList.iterator();
            MoveScore bestMove = null;
            if(side==mySide){
                bestMove=new MoveScore(Integer.MIN_VALUE);
            }else{
                bestMove=new MoveScore(Integer.MAX_VALUE);
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
    }

    //Weights
    private static final int MATERIAL_WEIGHT = 90;
    private static final int PAWNRACE_WEIGHT = 10;

    //Multipliers
    private static final double CHECK_MULTIPLIER = 0.2;
    private static final double QUEEN_MULTIPLIER = 0.2;
    private static final double KILL_MULTIPLIER = 0.5;


    /**
     * Given a state of the board, evaluate the board with respect to the given side.
     * @param board
     * @param side
     * @return
     */
    private double evaluateBoard(Board board, Side side) {
        //We don't want to lose...
        if (board.checkmate(side) || board.fiftyMoveRule() || board.threeFold()) {
            return -SCORE_CHECKMATE;
        }
        else if (board.checkmate(Piece.opposite(side))) {
        	return SCORE_CHECKMATE;
        }

        int score = 0;
        double multiplier = 1;

        materialScore = getMaterialScore(board, side) * MATERIAL_WEIGHT;   
        pawnRaceScore = pawnRace(board, side) * PAWNRACE_WEIGHT;

        
        score += materialScore;
        score += pawnRaceScore;
        
        
        multiplier += checkAdvantage(board, side);
        multiplier += queenAdvantage(board, side);
        if (materialScore > 0) {
        	multiplier += killAdvantage(board, side);
        }
        
        return score * multiplier;
    }
    
    private static final double MAX_KILL_PRESSURE = 12;
    private double killAdvantage(Board board, Side side) {
    	double score = 0;
    	Position kingPosition = board.findKing(Piece.opposite(side));
    	
    	for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Piece p = board.getPiece(new Position(i, j));
            	if (p != null && p.getClass() != King.class && p.getClass() != Pawn.class) {
            		score += manhattanDistance(p.getPosition(), kingPosition);
            	}
            }
    	}
    	
    	return MAX_KILL_PRESSURE / score * KILL_MULTIPLIER;
    }
    
    private double checkAdvantage(Board board, Side side) {
    	if (board.check(side)) {
            return -CHECK_MULTIPLIER;
        }
        else if (board.check(Piece.opposite(side))) {
            return CHECK_MULTIPLIER;
        }
        else {
        	return 0;
        }
    }
    
    private double queenAdvantage(Board board, Side side) {
    	int queenCount = 0;
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Piece p = board.getPiece(new Position(i, j));
            	if (p != null && p.getClass() == Queen.class) {
            		if (p.getSide() == side) {
            			queenCount++;
            		}
            		else {
            			queenCount--;
            		}
            	}
            }
        }
        
        return queenCount * QUEEN_MULTIPLIER;
    }

    private int getMaterialScore(Board board, Side side) {
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
    
    private int pawnRace(Board board, Side side) {
    	int score = 0;
    	for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Piece p = board.getPiece(new Position(i, j));
                if (p != null) {
                    if (p.getSide().equals(side) && p.getClass() == Pawn.class) {
                    	if (side == Side.BLACK) {
                            score += 8 - p.getPosition().getY();
                    	}
                    	else {
                    		score += p.getPosition().getY();
                    	}
                    }
                }
            }
        }
    	
    	return score;
    }

    private int manhattanDistance(Position p1, Position p2) {
        int xdiff = Math.abs(p1.getX() - p2.getX());
        int ydiff = Math.abs(p1.getY() - p2.getY());
        return xdiff + ydiff;
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
