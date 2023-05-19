package com.leekwars.env.util;

public interface RandomGenerator {
	public void seed(long seed);
	public int getInt(int min, int max);
	public long getLong(long min, long max);
	public double getDouble();
}
