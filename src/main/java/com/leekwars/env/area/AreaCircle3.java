package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaCircle3 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateCircleMask(0, 3);

	public AreaCircle3(Attack attack) {
		super(attack, area);
	}
}
