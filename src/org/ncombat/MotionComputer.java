package com.googlecode.ncombat;

import com.googlecode.ncombat.utils.Vector;

public class MotionComputer
{
	private static final double TWO_PI = 2.0 * Math.PI;
	
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

	/**
	 * Calculates the final kinematic state of a point body given an initial
	 * kinematic state, a period of time, and the parameters of the kinetic
	 * influence of the world at large on the body.
	 */
	public static Response compute(Request in)
	{
		Request origRequest = in;
		Response out = null;
		
		/*
		 * If rotation or acceleration occurs during only part of the requested
		 * interval, we subdivide the interval in order to simplify the
		 * mathematics. During each subinterval, acceleration will either occur
		 * during the entire subinterval or not at all. Likewise for rotation.
		 */
		boolean partialAccel = ((in.accelTime > 0) && (in.accelTime < in.intervalLength));
		boolean partialRotation = ((in.rotationTime > 0) && (in.rotationTime < in.intervalLength));
		boolean subdividing = partialAccel || partialRotation;
		
		if  (subdividing) {
			/*
			 * We need to modify the request object when subdividing an
			 * interval, so we copy it to avoid befuddling the caller by
			 * changing the mutable request object out from under him.
			 */
			in = new Request();
			in.initialPosition = origRequest.initialPosition;
			in.initialVelocity = origRequest.initialVelocity;
			in.initialHeading = origRequest.initialHeading;
			in.accelRate = origRequest.accelRate;
			in.accelTime = origRequest.accelTime;
			in.rotationRate = origRequest.rotationRate;
			in.rotationTime = origRequest.rotationTime;
		}
		
		double timeLeft = origRequest.intervalLength;
		
		while (timeLeft > 0.0)
		{
			if (out == null) {
				if (subdividing) {
					in.intervalLength = timeLeft;
					
					if ((in.accelTime > 0.0) && (in.accelTime < in.intervalLength)) {
						in.intervalLength = in.accelTime;
					}
					if ((in.rotationTime > 0.0) && (in.rotationTime < in.intervalLength)) {
						in.intervalLength = in.rotationTime;
					}
				}
			}
			else {
				in.intervalLength = timeLeft;
				
				if ((out.accelTimeLeft > 0.0) && (out.accelTimeLeft < in.intervalLength)) {
					in.intervalLength = out.accelTimeLeft;
				}
				if ((out.rotationTimeLeft > 0.0) && (out.rotationTimeLeft < in.intervalLength)) {
					in.intervalLength = out.rotationTimeLeft;
				}
				
				in.initialPosition = out.finalPosition;
				in.initialVelocity = out.finalVelocity;
				in.initialHeading = out.finalHeading;
				in.accelRate = (out.accelTimeLeft > 0.0 ? in.accelRate : 0.0);
				in.accelTime = out.accelTimeLeft;
				in.rotationRate = (out.rotationTimeLeft > 0.0 ? in.rotationRate : 0.0);
				in.rotationTime = out.rotationTimeLeft;
			}
			
			out = simpleCompute(in);
			
			timeLeft -= in.intervalLength;
		}

		return out;
	}
	
	/**
	 * Simple computer that requires that rotation and acceleration occur at
	 * constant (possibly zero) rates throughout the entire interval. This
	 * restriction simplifies the math and is possible because of the efforts of
	 * {@link #compute(Request)} to subdivide complicated time intervals.
	 */
	private static Response simpleCompute(Request in)
	{
		if (in.intervalLength <= 0.0) {
			throw new IllegalArgumentException("intervalLength must be > 0");
		}
		
		if (in.accelTime < 0.0) {
			throw new IllegalArgumentException("accelTime must be >= 0");
		}
		else if ((in.accelTime > 0.0) && (in.accelTime < in.intervalLength)) {
			throw new IllegalArgumentException("accelTime must be >= intervalLength or zero");
		}
		
		if (in.rotationTime < 0.0) {
			throw new IllegalArgumentException("rotationTime must be >= 0");
		}
		else if ((in.rotationTime > 0.0) && (in.rotationTime < in.intervalLength)) {
			throw new IllegalArgumentException("rotationTime must be >= intervalLength or zero");
		}
		
		Response out = new Response();
		
		// Calculate finalHeading.
		out.finalHeading = in.initialHeading + (in.rotationRate * in.intervalLength);
		if ( Math.abs(out.finalHeading) > TWO_PI) {
			out.finalHeading = Math.IEEEremainder(out.finalHeading, TWO_PI);
		}
		if (out.finalHeading > Math.PI) {
			out.finalHeading -= TWO_PI;
		}
		else if (out.finalHeading <= -Math.PI) {
			out.finalHeading += TWO_PI;
		}
		
		/*
		 * Calculate finalPosition and finalVelocity;
		 */
		double a = in.accelRate;
		double w = in.rotationRate;
		double t = in.intervalLength;
		double h0 = in.initialHeading;
		double hf = out.finalHeading;
		if (in.rotationTime > 0.0) {
			// Using rotating equations of motion.
			
			double sini = Math.sin(h0);
			double cosi = Math.cos(h0);
			double cosf = Math.cos(hf);
			double sinf = Math.sin(hf);
			
			out.finalVelocity = new Vector(sinf - sini, -cosf + cosi).multiply(a/w).add(in.initialVelocity);
			
			Vector p1 = new Vector(cosf - cosi, sinf - sini).multiply(-a/w/w);
			Vector p2 = new Vector(-sini, cosi).multiply(a/w).add(in.initialVelocity).multiply(t);
			out.finalPosition = p1.add(p2).add(in.initialPosition);
		}
		else {
			// Using non-rotating equations of motion.
			
			out.finalVelocity = Vector.unit(h0).multiply(a*t).add(in.initialVelocity);
			
			Vector p1 = Vector.unit(h0).multiply(a*t*t/2.0);
			Vector p2 = in.initialVelocity.multiply(t);
			out.finalPosition = p1.add(p2).add(in.initialPosition);
		}
		
		// Calculate accelTimeLeft.
		if (in.accelTime > 0.0) {
			out.accelTimeLeft = in.accelTime - in.intervalLength;
		}
		
		// Calculate rotationTimeLeft.
		if (in.rotationTime > 0.0) {
			out.rotationTimeLeft = in.rotationTime - in.intervalLength;
		}
		
		return out;
	}
}
