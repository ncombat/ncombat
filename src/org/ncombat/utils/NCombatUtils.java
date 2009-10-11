package org.ncombat.utils;

	import java.util.Random;

	public final class NCombatUtils {

		public static Random r = new Random();

		// constants
		// TODO: should become Enum or better yet properties file (watch out for gold plating)
		public static final int MISSILE_LOAD_TIME_SECONDS = 30;
		public static int ARENA_SIZE = 10000;
		public static int BASE_TURN_RATE = 6;
		public static int BASE_POWER_PRODUCTION_RATE = 50;
		public static int BASE_REPAIR_RATE= 2;
		public static int MISSILE_RANGE = 20000;
		public static final int ENGINE_POWER_CONSUMPTION = 40;
		
	
		// do not instantiate.
		private NCombatUtils() {
		}
		
		public static int[] getRandomPosition(){
			int[] retValue = {r.nextInt(ARENA_SIZE), r.nextInt(ARENA_SIZE)};
			return retValue;
		}
		
}
