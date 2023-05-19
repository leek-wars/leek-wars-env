package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaPlus2 extends MaskArea {

	private static int[][] area = MaskAreaCell.generatePlusMask(2);

	public AreaPlus2(Attack attack) {
		super(attack, area);
	}
}
