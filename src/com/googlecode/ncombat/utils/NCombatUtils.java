package com.googlecode.ncombat.utils;

	import java.util.Random;

	public class NCombatUtils {

		public static Random r = new Random();
		
		// do not instantiate.
		private NCombatUtils() {
		}

		public static final int ENGINE_POWER_CONSUMPTION = 40;
		public static int ARENA_SIZE = 10000;
		public static int BASE_TURN_RATE = 6;
		public static int BASE_POWER_PRODUCTION_RATE = 50;
		public static int BASE_REPAIR_RATE= 2;
		public static int MISSILE_RANGE = 20000;
		public static int HUMAN_RANK_FOR_BOT_PROPERTY = -1;
		
		public static int[] getRandomPosition(){
			int[] retValue = {r.nextInt(ARENA_SIZE), r.nextInt(ARENA_SIZE)};
			return retValue;
		}
		
}
