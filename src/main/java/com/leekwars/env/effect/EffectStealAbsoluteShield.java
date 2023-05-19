package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectStealAbsoluteShield extends Effect {

	@Override
	public void apply(State state) {

		value = previousEffectTotalValue;
		if (value > 0) {
			stats.setStat(Entity.CHARAC_ABSOLUTE_SHIELD, value);
			target.updateBuffStats(Entity.CHARAC_ABSOLUTE_SHIELD, value, caster);
		}
	}
}
