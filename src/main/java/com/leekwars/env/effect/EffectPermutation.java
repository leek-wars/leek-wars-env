package com.leekwars.env.effect;

import com.leekwars.env.state.State;

public class EffectPermutation extends Effect {

	@Override
	public void apply(State fight) {

		fight.invertEntities(caster, target);
	}
}
