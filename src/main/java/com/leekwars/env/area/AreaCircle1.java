package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaCircle1 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateCircleMask(0, 1);

	public AreaCircle1(Attack attack) {
		super(attack, area);
	}
}
