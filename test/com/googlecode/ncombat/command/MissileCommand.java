package com.googlecode.ncombat.command;

public class MissileCommand implements Command
{
	private int target1;
	private int target2;

	public MissileCommand(int target1) {
		this(target1, 0);
	}

	public MissileCommand(int target1, int target2) {
		this.target1 = target1;
		this.target2 = target2;
	}

	public int getTarget1() {
		return target1;
	}

	public int getTarget2() {
		return target2;
	}
}
