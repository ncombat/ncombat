package com.googlecode.ncombat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class GameManager implements InitializingBean
{
	private Logger logger = Logger.getLogger(GameManager.class);
	
	private List<GameServer> gameServers = new ArrayList<GameServer>();
	
	public GameManager() {
	}

	public void afterPropertiesSet() throws Exception
	{
		GameServer initialGameServer = new GameServer();
		initialGameServer.start();
		
		gameServers.add(initialGameServer);
		
		logger.info( getClass().getSimpleName() + " has started.");
	}
}
