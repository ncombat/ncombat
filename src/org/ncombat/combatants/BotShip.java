package org.ncombat.combatants;

import org.ncombat.utils.Vector;

public class BotShip extends Ship
{
	public BotShip(Vector position, Vector velocity) {
		super(position);
		setVelocity(velocity);
		setHeading(velocity.theta());
	}

	@Override
	public void completeGameCycle() {
		// Eventually commands will be generated here.
	}
}
