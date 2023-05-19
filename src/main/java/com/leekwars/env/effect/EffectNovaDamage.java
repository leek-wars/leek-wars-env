package com.leekwars.env.effect;

import com.leekwars.env.action.ActionDamage;
import com.leekwars.env.attack.DamageType;
import com.leekwars.env.state.State;

public class EffectNovaDamage extends Effect {

	@Override
	public void apply(State state) {

		// Base damages
		double d = (value1 + jet * value2) * (1 + Math.max(0, caster.getScience()) / 100.0) * aoe * criticalPower * (1 + caster.getPower() / 100.0);

		value = (int) Math.round(d);

		if (value > target.getTotalLife() - target.getLife()) {
			value = target.getTotalLife() - target.getLife();
		}

		state.log(new ActionDamage(DamageType.NOVA, target, value, 0));
		target.removeLife(0, value, caster, DamageType.NOVA, this);
		target.onNovaDamage(value);
	}
}
