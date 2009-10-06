package com.googlecode.ncombat;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class GameServer
{
	public static final long DEFAULT_CYCLE_PERIOD = 1000; // milliseconds
	
	private static int nextServerNumber = 1;
	
	private Logger log = Logger.getLogger(GameServer.class);
	
	private int serverNumber;
	
	private Timer timer;
	
	private GameServerTimerTask timerTask;
	
	private long cyclePeriod = DEFAULT_CYCLE_PERIOD;
	
	private boolean started;
	
	public GameServer()
	{
		synchronized (GameServer.class) {
			this.serverNumber = nextServerNumber++;
		}
		
		log.info("Game server #" + serverNumber + " is starting.");

		timer = new Timer("NcombatServer", true);
		timerTask = new GameServerTimerTask();
	}
	
	public synchronized void start()
	{
		if (started) return;
		timer.schedule(timerTask, cyclePeriod, cyclePeriod);
		started = true;
	}
	
	public synchronized void stop()
	{
		if (!started) return;
		timerTask.cancel();
		started = false;
	}
	
	public long getCyclePeriod() {
		return cyclePeriod;
	}

	public void setCyclePeriod(long cyclePeriod) {
		this.cyclePeriod = cyclePeriod;
	}
	
	private void runCommandCycle()
	{
		log.debug("Game server #" + serverNumber + ": running command cycle.");
	}
	
	public class GameServerTimerTask extends TimerTask
	{
		@Override
		public void run() {
			runCommandCycle();
		}
	}
}
