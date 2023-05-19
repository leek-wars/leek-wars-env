package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaSquare2 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateSquareMask(2);

	public AreaSquare2(Attack attack) {
		super(attack, area);
	}
}
