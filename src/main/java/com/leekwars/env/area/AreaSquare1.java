package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaSquare1 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateSquareMask(1);

	public AreaSquare1(Attack attack) {
		super(attack, area);
	}
}
