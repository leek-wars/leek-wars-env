package com.leekwars.env.area;

import java.util.ArrayList;
import java.util.List;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.Cell;

public class AreaLaserLine extends Area {

	public AreaLaserLine(Attack attack) {
		super(attack);
	}

	@Override
	public List<Cell> getArea(Cell launchCell, Cell targetCell) {

		ArrayList<Cell> cells = new ArrayList<Cell>();
		int dx = 0, dy = 0;
		if (launchCell.getX() == targetCell.getX()) {
			if (launchCell.getY() > targetCell.getY())
				dy = -1;
			else
				dy = 1;
		} else if (launchCell.getY() == targetCell.getY()) {
			if (launchCell.getX() > targetCell.getX())
				dx = -1;
			else
				dx = 1;
		} else
			return cells;

		int x = launchCell.getX(), y = launchCell.getY();
		for (int i = mAttack.getMinRange(); i <= mAttack.getMaxRange(); i++) {

			Cell c = launchCell.getMap().getCell(x + dx * i, y + dy * i);
			if (c == null) {
				break;
			}
			if (mAttack.needLos() && !c.isWalkable()) {
				break;
			} else if (mAttack.needLos() && !c.isWalkable()) {
				break;
			}
			cells.add(c);
		}
		return cells;
	}
}
