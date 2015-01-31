package com.nullprogram.chess.puai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private PUMiniMax ai;
	
	public AITimerTask(PUMiniMax ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
