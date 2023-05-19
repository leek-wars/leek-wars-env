package com.leekwars.env.turret;

import com.leekwars.env.state.Entity;

public class Turret extends Entity {

    @Override
    public int getType() {
        return Entity.TYPE_TURRET;
    }
}