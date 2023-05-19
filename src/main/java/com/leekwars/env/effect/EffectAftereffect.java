package com.leekwars.env.effect;

import com.leekwars.env.action.ActionDamage;
import com.leekwars.env.attack.DamageType;
import com.leekwars.env.state.State;

public class EffectAftereffect extends Effect {

	@Override
	public void apply(State state) {

		value = (int) Math.round((value1 + value2 * jet) * (1 + (double) caster.getScience() / 100) * aoe * criticalPower);
		value = Math.max(0, value);

		if (target.getLife() < value) {
			value = target.getLife();
		}
		int erosion = (int) Math.round(value * erosionRate);

		state.log(new ActionDamage(DamageType.AFTEREFFECT, target, value, erosion));
		target.removeLife(value, erosion, caster, DamageType.AFTEREFFECT, this);
	}

	@Override
	public void applyStartTurn(State state) {

		if (target.getLife() < value) {
			value = target.getLife();
		}
		int erosion = (int) Math.round(value * erosionRate);

		state.log(new ActionDamage(DamageType.AFTEREFFECT, target, value, erosion));
		target.removeLife(value, erosion, caster, DamageType.AFTEREFFECT, this);
	}
}
