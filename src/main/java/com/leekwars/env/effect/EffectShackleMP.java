package com.leekwars.env.effect;

import com.leekwars.env.state.Entity;
import com.leekwars.env.state.State;

public class EffectShackleMP extends Effect {

	@Override
	public void apply(State state) {
		// Base shackle : base × (1 + magic / 100)
		value = (int) Math.round((value1 + jet * value2) * (1.0 + Math.max(0, caster.getMagic()) / 100.0) * aoe * criticalPower);
		if (value > 0) {
			stats.setStat(Entity.CHARAC_MP, -value);
			target.updateBuffStats(Entity.CHARAC_MP, -value, caster);
		}
	}
}
