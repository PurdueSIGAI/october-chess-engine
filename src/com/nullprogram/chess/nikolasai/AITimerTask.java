package com.nullprogram.chess.nikolasai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private NikolasAI ai;
	
	public AITimerTask(NikolasAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
