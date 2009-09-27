package com.googlecode.ncombat;

import com.googlecode.ncombat.utils.Vector;

public class MotionComputer
{
	public static class Request
	{
		public double intervalLength;
		public Vector initialPosition = Vector.ZERO;
		public Vector initialVelocity = Vector.ZERO;
		public double initialHeading;
		public double accelRate;
		public double accelTime;
		public double rotationRate;
		public double rotationTime;
	}
	
	public static class Response
	{
		public Vector finalPosition = Vector.ZERO;
		public Vector finalVelocity = Vector.ZERO;
		public double finalHeading;
		public double accelTimeLeft;
		public double rotationTimeLeft;
	}
	
	private MotionComputer() {}
	
	public static Response compute(Request in)
	{
		Response out = new Response();
		
		// Motion with rotation requires a different set of equations than motion
		// without rotation.  If rotation occurs during only part of this motion
		// interval, then we must divide it into its rotating and non-rotating
		// sub-intervals and treat them separately.
		
		// TODO: Deal with headings greater than 180.
		
		return out;
	}
}
