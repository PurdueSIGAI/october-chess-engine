package com.nullprogram.chess.loganai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private LoganAI ai;
	
	public AITimerTask(LoganAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
