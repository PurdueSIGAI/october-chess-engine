package com.nullprogram.chess.matthewpai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private MatthewPAI ai;
	
	public AITimerTask(MatthewPAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
