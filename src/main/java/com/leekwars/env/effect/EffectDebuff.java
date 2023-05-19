package com.leekwars.env.effect;

import com.leekwars.env.action.ActionReduceEffects;
import com.leekwars.env.state.State;

public class EffectDebuff extends Effect {

	@Override
	public void apply(State state) {
		value = (int) ((value1 + jet * value2) * aoe * criticalPower * targetCount);
		target.reduceEffects((double) value / 100, caster);

		// "Les effets de X sont réduits de Y%"
		state.log(new ActionReduceEffects(target, value));
	}
}
