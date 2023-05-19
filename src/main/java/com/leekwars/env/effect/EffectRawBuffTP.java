package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectRawBuffTP extends Effect {

	@Override
	public void apply(State state) {
		value = (int) Math.round((value1 + value2 * jet) * targetCount * criticalPower);
		if (value > 0) {
			stats.setStat(Entity.CHARAC_TP, value);
			target.updateBuffStats(Entity.CHARAC_TP, value, caster);
		}
	}
}
