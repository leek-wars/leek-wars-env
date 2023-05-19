package com.leekwars.env.area;

import java.util.ArrayList;
import java.util.List;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.Cell;

public class MaskArea extends Area {

	private int[][] area;

	public MaskArea(Attack attack, int[][] area) {
		super(attack);
		this.area = area;
	}

	@Override
	public List<Cell> getArea(Cell launchCell, Cell targetCell) {
		int x = targetCell.getX(), y = targetCell.getY();
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (int i = 0; i < area.length; i++) {
			Cell c = targetCell.getMap().getCell(x + area[i][0], y + area[i][1]);
			if (c == null || !c.isWalkable())
				continue;
			cells.add(c);
		}
		return cells;
	}
}
