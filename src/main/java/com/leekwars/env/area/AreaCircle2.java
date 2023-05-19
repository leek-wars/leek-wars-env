package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaCircle2 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateCircleMask(0, 2);

	public AreaCircle2(Attack attack) {
		super(attack, area);
	}
}
