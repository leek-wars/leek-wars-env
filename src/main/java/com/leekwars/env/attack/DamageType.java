package com.leekwars.env.attack;

public enum DamageType {
    DIRECT(101),
	NOVA(107),
	RETURN(108),
    LIFE(109),
    POISON(110),
    AFTEREFFECT(110);

    public int value;
    DamageType(int v) {
        this.value = v;
    }
}
