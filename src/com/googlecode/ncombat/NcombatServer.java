package com.googlecode.ncombat;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Central NCombat server managing combat zone operations. 
 */
public class NcombatServer
{
	public static final long DEFAULT_CYCLE_PERIOD = 1000; // milliseconds
	
	private Timer timer;
	
	private NCombatTimerTask timerTask;
	
	private long cyclePeriod = DEFAULT_CYCLE_PERIOD;
	
	private boolean started;
	
	public NcombatServer()
	{
		timer = new Timer("NcombatServer", true);
		timerTask = new NCombatTimerTask();
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
	}
	
	public class NCombatTimerTask extends TimerTask
	{
		@Override
		public void run() {
			runCommandCycle();
		}
	}
}
