package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectRawRelativeShield extends Effect {

	@Override
	public void apply(State state) {
		value = (int) Math.round((value1 + jet * value2) * aoe * criticalPower);
		if (value > 0) {
			stats.setStat(Entity.CHARAC_RELATIVE_SHIELD, value);
			target.updateBuffStats(Entity.CHARAC_RELATIVE_SHIELD, value, caster);
		}
	}
}
