package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaX2 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateXMask(2);

	public AreaX2(Attack attack) {
		super(attack, area);
	}
}
