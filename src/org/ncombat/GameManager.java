package org.ncombat;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class GameManager implements InitializingBean
{
	private Logger logger = Logger.getLogger(GameManager.class);
	
	private GameServer gameServer;
	
	private Map<Integer, Combatant> combatants = new HashMap<Integer, Combatant>();
	
	public GameManager() {
	}

	public void afterPropertiesSet() throws Exception
	{
		logger.info("GameManager is starting.");
		
		gameServer = new GameServer();
		gameServer.start();
		
		logger.info("GameManager has started.");
	}
	
	public Combatant getCombatant(Integer combatantId)
	{
		if (combatantId == null) return null;
		synchronized (combatants) {
			return combatants.get(combatantId);
		}
	}
	
	public void addCombatant(Combatant combatant)
	{
		synchronized (combatants) {
			combatants.put( combatant.getId(), combatant);
		}
		
		gameServer.addCombatant(combatant);
	}
}
