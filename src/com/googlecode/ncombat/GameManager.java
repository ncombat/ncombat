package com.googlecode.ncombat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class GameManager implements InitializingBean
{
	private Logger logger = Logger.getLogger(GameManager.class);
	
	private List<GameServer> gameServers = new ArrayList<GameServer>();
	
	private int nextCombatantId = 1;
	private Map<Integer, Combatant> combatants = new HashMap<Integer, Combatant>();
	
	public GameManager() {
	}

	public void afterPropertiesSet() throws Exception
	{
		GameServer initialGameServer = new GameServer();
		initialGameServer.start();
		
		gameServers.add(initialGameServer);
		
		logger.info( getClass().getSimpleName() + " has started.");
	}
	
	public Combatant getCombatant(Integer combatantId)
	{
		if (combatantId == null) return null;
		synchronized (combatants) {
			return combatants.get(combatantId);
		}
	}
	
	public int addCombatant(Combatant combatant)
	{
		int combatantId = 0;
		synchronized (combatants) {
			combatantId = nextCombatantId++;
			combatants.put(combatantId, combatant);
		}
		return combatantId;
	}
}
