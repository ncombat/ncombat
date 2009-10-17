package org.ncombat;

import org.ncombat.utils.Vector;

public class GornBase extends Combatant
{
	public static final int NUM_SHIELDS = 1;
	
	public GornBase(Vector position) {
		super(position);
	}

	@Override
	public int getNumShields() {
		return NUM_SHIELDS;
	}
}
