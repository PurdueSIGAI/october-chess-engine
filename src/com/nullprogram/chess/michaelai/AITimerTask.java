package com.nullprogram.chess.michaelai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private MichaelAI ai;
	
	public AITimerTask(MichaelAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
