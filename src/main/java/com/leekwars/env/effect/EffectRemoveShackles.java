package com.leekwars.env.effect;

import com.leekwars.env.action.ActionRemoveShackles;
import com.leekwars.env.state.State;

public class EffectRemoveShackles extends Effect {

	@Override
	public void apply(State state) {
		target.removeShackles();

		// "Les entraves de X sont retirées"
		state.log(new ActionRemoveShackles(target));
	}
}
