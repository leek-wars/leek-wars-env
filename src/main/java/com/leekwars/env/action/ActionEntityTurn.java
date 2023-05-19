package com.leekwars.env.action;

import com.alibaba.fastjson.JSONArray;
import com.leekwars.env.state.Entity;

public class ActionEntityTurn implements Action {

	private final int id;

	public ActionEntityTurn(Entity leek) {
		if (leek == null)
			this.id = -1;
		else {
			this.id = leek.getFId();
		}
	}

	@Override
	public JSONArray getJSON() {
		JSONArray retour = new JSONArray();
		retour.add(Action.LEEK_TURN);
		retour.add(id);
		return retour;
	}
}
