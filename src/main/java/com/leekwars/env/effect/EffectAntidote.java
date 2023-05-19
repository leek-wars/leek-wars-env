package com.leekwars.env.effect;

import com.leekwars.env.action.ActionRemovePoisons;
import com.leekwars.env.state.State;

public class EffectAntidote extends Effect {

	@Override
	public void apply(State state) {

		target.clearPoisons(caster);

		// "Les poisons de X sont neutralisés"
		state.log(new ActionRemovePoisons(target));
	}
}
