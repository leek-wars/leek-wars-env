package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaPlus3 extends MaskArea {

	private static int[][] area = MaskAreaCell.generatePlusMask(3);

	public AreaPlus3(Attack attack) {
		super(attack, area);
	}
}
