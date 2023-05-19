package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaX1 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateXMask(1);

	public AreaX1(Attack attack) {
		super(attack, area);
	}
}
