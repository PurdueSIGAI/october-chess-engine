package com.nullprogram.chess.kumarai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private KumarAI ai;
	
	public AITimerTask(KumarAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
