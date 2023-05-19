package com.leekwars.env.area;

import com.leekwars.env.attack.Attack;
import com.leekwars.env.maps.MaskAreaCell;

public class AreaX3 extends MaskArea {

	private static int[][] area = MaskAreaCell.generateXMask(3);

	public AreaX3(Attack attack) {
		super(attack, area);
	}
}
