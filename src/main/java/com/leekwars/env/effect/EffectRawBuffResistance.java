package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectRawBuffResistance extends Effect {

	@Override
	public void apply(State state) {

		value = (int) Math.round((value1 + jet * value2) * aoe * criticalPower);
		if (value > 0) {
			stats.setStat(Entity.CHARAC_RESISTANCE, value);
			target.updateBuffStats(Entity.CHARAC_RESISTANCE, value, caster);
		}
	}
}
