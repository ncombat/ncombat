package org.ncombat.combatants;

import org.ncombat.GornShieldArray;
import org.ncombat.utils.Vector;

public class GornBase extends Combatant
{
	public GornBase(Vector position) {
		super(position);
		setShields( new GornShieldArray());
	}

	@Override
	public void update(long updateTime) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void completeGameCycle() {
		// TODO Auto-generated method stub
	}
}
