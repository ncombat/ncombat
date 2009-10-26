package org.ncombat.combatants;

import org.ncombat.GornShieldArray;

public class GornBase extends Combatant
{
	public GornBase(String commander) {
		super(commander);
		this.shields = new GornShieldArray();
	}

	@Override
	public void update(long updateTime) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void completeGameCycle() {
		// TODO Auto-generated method stub
	}

	@Override
	protected AttackResult onLaserHit(Combatant attacker, double power) {
		return new AttackResult();
	}

	@Override
	protected AttackResult onMissileHit(Combatant attacker) {
		return new AttackResult();
	}
}
