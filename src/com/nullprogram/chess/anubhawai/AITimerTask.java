package com.nullprogram.chess.anubhawai;

import java.util.TimerTask;


public class AITimerTask extends TimerTask {

	private AnubhawAI ai;
	
	public AITimerTask(AnubhawAI ai) {
		this.ai = ai;
	}
	
	@Override
	public void run() {
		// End the current turn
		System.out.println("Ending turn");
		ai.setEndTurn(true);
	}

}
