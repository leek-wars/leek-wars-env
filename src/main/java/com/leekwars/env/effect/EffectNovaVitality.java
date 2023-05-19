package com.leekwars.env.effect;

import com.leekwars.env.action.ActionNovaVitality;
import com.leekwars.env.state.State;

public class EffectNovaVitality extends Effect {

	@Override
	public void apply(State state) {

		value = (int) Math.round((value1 + jet * value2) * (1 + caster.getScience() / 100.0) * aoe * criticalPower);

		state.log(new ActionNovaVitality(target, value));
		target.addTotalLife(value, caster);
	}
}
