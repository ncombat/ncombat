package com.googlecode.ncombat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.googlecode.ncombat.command.CommandBatch;

public class GameServer implements DisposableBean
{
	public static final long DEFAULT_CYCLE_PERIOD = 1000; // milliseconds
	
	private static final long PLAYER_SYNC_TIMEOUT = 5000; // milliseconds
	
	private static int nextServerNumber = 1;
	
	private Logger log = Logger.getLogger(GameServer.class);
	
	private int serverNumber;
	private Timer timer;
	private GameServerTimerTask timerTask;
	private long cyclePeriod = DEFAULT_CYCLE_PERIOD;
	private boolean started;
	
	//------------------------------------------------------------
	// The following variables are protected by the cycle monitor.
	//------------------------------------------------------------
	
	private Object cycleMonitor = new Object();
	private boolean paused;
	private List<Combatant> combatants = new ArrayList<Combatant>();
	private List<CommandBatch> commandBatches = new ArrayList<CommandBatch>();
	
	//-------------------------------------------------
	// End of variables protected by the cycle monitor.
	//-------------------------------------------------
	
	/*
	 * This object is used by the GameController to synchronize the game cycle
	 * with the incoming HTTP command submissions from the players. After
	 * submitting a command batch to the queue, the GameController waits on this
	 * monitor for the game cycle to complete so that they might return their
	 * messages and accept the next round of commands.
	 */
	private Object playerSyncMonitor = new Object();
	
	public GameServer()
	{
		synchronized (GameServer.class) {
			this.serverNumber = nextServerNumber++;
		}
		
		log.info("GameServer #" + serverNumber + " is starting.");

		timer = new Timer("GameServer" + serverNumber, true);
		timerTask = new GameServerTimerTask();
	}
	
	public void destroy() throws Exception {
		stop();
	}
	
	private void runGameCycle()
	{
		log.debug("Entering game cycle.");
		
		synchronized (cycleMonitor) {
			long updateTime = System.currentTimeMillis();
			
			for (Combatant combatant : combatants) {
				if (combatant.isAlive()) {
					if (!paused) {
						combatant.update(updateTime);
					}
					combatant.setLastUpdateTime(updateTime);
				}
			}
			
			List<CommandBatch> commandBatches = drainCommandBatches();
			
			if (!paused) {
				for (CommandBatch commandBatch : commandBatches) {
					Combatant combatant = commandBatch.getCombatant();
					if (combatant.isAlive()) {
						combatant.processCommands(commandBatch);
					}
				}
				
				for (Combatant combatant : combatants) {
					if (combatant.isAlive()) {
						combatant.completeGameCycle();
					}
				}
			}
		}
		
		notifyPlayers();
		
		log.debug("Exiting game cycle.");
	}
	
	public boolean isPaused() {
		synchronized (cycleMonitor) {
			return paused;
		}
	}

	public void setPaused(boolean paused) {
		synchronized (cycleMonitor) {
			this.paused = paused;
		}
	}

	public void addCombatant(Combatant combatant)
	{
		combatant.setGameServer(this);
		synchronized (cycleMonitor) {
			combatants.add(combatant);
		}
	}
	
	public void addCommandBatch(CommandBatch commandBatch) {
		synchronized (cycleMonitor) {
			commandBatches.add(commandBatch);
		}
	}
	
	private List<CommandBatch> drainCommandBatches() {
		synchronized (cycleMonitor) {
			List<CommandBatch> drainedBatches = new ArrayList(commandBatches);
			commandBatches.clear();
			return drainedBatches;
		}
	}
	
	public void playerSync()
	{
		synchronized (playerSyncMonitor) {
			try {
				playerSyncMonitor.wait(PLAYER_SYNC_TIMEOUT);
			}
			catch (InterruptedException e) {
				log.warn("playerSync() caught exception.", e);
			}
		}
	}
	
	private void notifyPlayers()
	{
		synchronized (playerSyncMonitor) {
			playerSyncMonitor.notifyAll();
		}
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
	
	public class GameServerTimerTask extends TimerTask
	{
		@Override
		public void run() {
			runGameCycle();
		}
	}
}
