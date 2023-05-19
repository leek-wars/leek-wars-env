package com.leekwars.env.area;

import java.util.ArrayList;
import java.util.List;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.Cell;
import com.leekwars.env.maps.Pathfinding;

public class AreaFirstInLine extends Area {

	public AreaFirstInLine(Attack attack) {
		super(attack);
	}

	@Override
	public List<Cell> getArea(Cell launchCell, Cell targetCell) {
		List<Cell> cells = new ArrayList<>();
		Cell cell = Pathfinding.getFirstEntity(launchCell, targetCell, mAttack.getMinRange(), mAttack.getMaxRange());
		if (cell != null) {
			cells.add(cell);
		}
		return cells;
	}
}
