package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectBuffStrength extends Effect {

	@Override
	public void apply(State state) {

		value = (int) Math.round((value1 + value2 * jet) * (1 + (double) caster.getScience() / 100) * aoe * criticalPower);
		if (value > 0) {
			stats.setStat(Entity.CHARAC_STRENGTH, value);
			target.updateBuffStats(Entity.CHARAC_STRENGTH, value, caster);
		}
	}
}
