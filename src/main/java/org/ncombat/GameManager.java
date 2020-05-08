package org.ncombat;

import org.ncombat.combatants.Combatant;
import org.ncombat.combatants.PlayerShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameManager implements InitializingBean
{
	private Logger logger = LoggerFactory.getLogger(GameManager.class);
	
	private GameServer gameServer;
	
	private Map<Integer, Combatant> combatants = new HashMap<Integer, Combatant>();
	
	public GameManager() {
	}

	public void afterPropertiesSet() throws Exception
	{
		logger.info("GameManager is starting.");
		
		gameServer = new GameServer(this);
		gameServer.start();
		
		logger.info("GameManager has started.");
	}
	
	public PlayerShip createPlayerShip(String commander)
	{
		PlayerShip playerShip = gameServer.createPlayerShip(commander);
		
		if (playerShip != null) {
			addCombatant(playerShip);
		}
		
		return playerShip;
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
	}
	
	public void removeCombatant(Combatant combatant)
	{
		synchronized (combatants) {
			combatants.remove(combatant.getId());
		}
	}
}
