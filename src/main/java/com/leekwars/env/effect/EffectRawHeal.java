package com.leekwars.env.effect;

import com.leekwars.env.action.ActionHeal;
import com.leekwars.env.state.State;

public class EffectRawHeal extends Effect {

	@Override
	public void apply(State state) {

		value = (int) Math.round((value1 + jet * value2) * aoe * criticalPower * targetCount);

		if (target.getLife() + value > target.getTotalLife()) {
			value = target.getTotalLife() - target.getLife();
		}
		state.log(new ActionHeal(target, value));
		target.addLife(caster, value);
	}
}
