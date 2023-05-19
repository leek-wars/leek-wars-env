package com.leekwars.env.effect;

import com.leekwars.env.action.ActionKill;
import com.leekwars.env.attack.DamageType;
import com.leekwars.env.state.State;

public class EffectKill extends Effect {

	@Override
	public void apply(State state) {

		value = target.getLife();
		state.log(new ActionKill(caster, target));
		target.removeLife(value, 0, caster, DamageType.DIRECT, this);
	}
}
