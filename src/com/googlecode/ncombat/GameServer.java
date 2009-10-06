package com.googlecode.ncombat;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class GameServer implements InitializingBean
{
	private Logger logger = Logger.getLogger(GameServer.class);
	
	public GameServer() {
	}

	public void afterPropertiesSet() throws Exception {
		logger.info( getClass().getSimpleName() + " has started.");
	}
}
