package com.googlecode.ncombat;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.ncombat.MotionComputer.Response;
import com.googlecode.ncombat.utils.Vector;

public class MotionComputerTest
{
	private MotionComputer.Request in;
	private MotionComputer.Response out;
	
	@Before
	public void setUp() {
		in = new MotionComputer.Request();
		out = new MotionComputer.Response();
	}
	
	@Test
	public void testInertiaAtRest()
	{
		// Sitting still changes nothing.
		// Zeros in, zeros out.
		
		in.intervalLength = 2.0;
		in.initialPosition = Vector.ZERO;
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = 0.0;
		in.accelRate = 0.0;
		in.accelTime = 0.0;
		in.rotationRate = 0.0;
		in.rotationTime = 0.0;
		
		out.finalPosition = Vector.ZERO;
		out.finalVelocity = Vector.ZERO;
		out.finalHeading = 0.0;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputationExact();
	}
	
	@Test public void testSittingStill()
	{
		// Sitting still changes nothing
		// even with non-zero position and heading.
		
		in.intervalLength = 10.0;
		in.initialPosition = new Vector(1.2, 3.4);
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = 0.567;
		in.accelRate = 0.0;
		in.accelTime = 0.0;
		in.rotationRate = 0.0;
		in.rotationTime = 0.0;
		
		out.finalPosition = in.initialPosition;
		out.finalVelocity = in.initialVelocity;
		out.finalHeading = in.initialHeading;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputationExact();
	}
	
	@Test public void testRotation1()
	{
		// Rotation changes heading.
		// Positive rotation is OK.
		// Rotation can stop within interval.
		// Stationary rotation doesn't move you.
		// Positive initial and final heading.
		
		in.intervalLength = 10.0;
		in.initialPosition = new Vector(1.21, 3.41);
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = 0.5671;
		in.accelRate = 0.0;
		in.accelTime = 0.0;
		in.rotationRate = 1.5;
		in.rotationTime = 1.65;
		
		out.finalPosition = in.initialPosition;
		out.finalVelocity = in.initialVelocity;
		out.finalHeading = 3.0421;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	@Test public void testRotation2()
	{
		// Rotation changes heading.
		// Negative rotation is OK.
		// Negative initial and final heading.		
		// Rotation can stop coincident with interval.
		// Unaccelerated rotation doesn't change your velocity.
		
		in.intervalLength = 2.1;
		in.initialPosition = new Vector(1.212, 3.412);
		in.initialVelocity = new Vector(0.1, 0.25);
		in.initialHeading = -0.333;
		in.accelRate = 0.0;
		in.accelTime = 0.0;
		in.rotationRate = -0.8;
		in.rotationTime = 2.1;
		
		out.finalPosition = new Vector(1.422, 3.937);
		out.finalVelocity = in.initialVelocity;
		out.finalHeading = -2.013;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testRotation3()
	{
		// Rotation changes heading.
		// Rotation can continue beyond interval.
		// Canonical heading is between -PI and PI; heading changes sign.
		
		in.intervalLength = 5.5;
		in.initialPosition = new Vector(1.213, 3.413);
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = -0.5236;
		in.accelRate = 0.0;
		in.accelTime = 0.0;
		in.rotationRate = -1.650129;
		in.rotationTime = 30.0;
		
		out.finalPosition = in.initialPosition;
		out.finalVelocity = in.initialVelocity;
		out.finalHeading = 2.96706111435917000;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 24.5;
		
		checkComputation();
	}
	
	@Test public void testAccelWithoutRot1()
	{
		// Positive acceleration
		// Starting from rest
		// Accel coincident with interval
		
		in.intervalLength = 2.0;
		in.initialPosition = new Vector(1.214, 3.414);
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = 1.23456;
		in.accelRate = 5.0;
		in.accelTime = 2.0;
		in.rotationRate = 0.0;
		in.rotationTime = 0.0;
		
		out.finalPosition = new Vector(4.5133651808517700, 12.8540312183479000);
		out.finalVelocity = new Vector(3.2993651808517700, 9.4400312183479000);
		out.finalHeading = in.initialHeading;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testAccelWithoutRot2()
	{
		// Negative acceleration
		// Starting in motion
		// Course == heading
		// Accel beyond interval
		
		in.intervalLength = 4.0;
		in.initialPosition = Vector.polarDegrees(100.0, 30.0);
		in.initialVelocity = Vector.polarDegrees(10.0, 170.0);
		in.initialHeading = Math.toRadians(170.0);
		in.accelRate = -5.0;
		in.accelTime = 20.0;
		in.rotationRate = 0.0;
		in.rotationTime = 0.0;
		
		out.finalPosition = in.initialPosition;
		out.finalVelocity = in.initialVelocity.negate();
		out.finalHeading = in.initialHeading;
		out.accelTimeLeft = 16.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testAccelWithoutRot3()
	{
		// Positive acceleration
		// Starting in motion
		// Course != heading
		// Acceleration within interval
		
		in.intervalLength = 10.0;
		in.initialPosition = Vector.polarDegrees(25.0, 215.0);
		in.initialVelocity = Vector.polarDegrees(15.0, -80.0);
		in.initialHeading = Math.toRadians(170.0);
		in.accelRate = 3.5;
		in.accelTime = 3.0;
		in.rotationRate = 0.0;
		in.rotationTime = 0.0;
		
		out.finalPosition = new Vector(-9.9422965671275100, -159.3256150623530000);
		out.finalVelocity = new Vector(-7.7357587416242300, -12.9488104296804000);
		out.finalHeading = in.initialHeading;
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testAccelWithRot1()
	{
		// Positive acceleration
		// Positive rotation
		// Starting from rest
		// Accel coincident with interval
		// Rotation coincident with interval
	}
	
	@Test public void testAccelWithRot2()
	{
		// Negative acceleration
		// Positive rotation
		// Starting in motion
		// Course == heading
		// Accel beyond interval
		// Rotation within interval
	}
	
	@Test public void testAccelWithRot3()
	{
		// Negative acceleration
		// Negative rotation
		// Starting in motion
		// Course != heading
		// Acceleration within interval
		// Rotation within interval (finishes after accel)
	}
	
	// Unaccelerated motion is linear.
	
	private Response response(Vector finalPosition, Vector finalVelocity,
			double finalHeading, double accelTimeLeft,
			double rotationTimeLeft)
	{
		Response response = new Response();
		response.finalPosition = finalPosition;
		response.finalVelocity = finalVelocity;
		response.finalHeading = finalHeading;
		response.accelTimeLeft = accelTimeLeft;
		response.rotationTimeLeft = rotationTimeLeft;
		return response;
	}
	
	private void checkComputation()
	{
		double positionCoordTol = 1.0e-5;
		double velocityCoordTol = 1.0e-5;
		double headingTol = 1.0e-14;
		double accelTimeTol = 1.0e-14;
		double rotationTimeTol = 1.0e-14;
		
		Response actual = MotionComputer.compute(in);
		
		assertEquals(out.finalPosition.x(), actual.finalPosition.x(), positionCoordTol);
		assertEquals(out.finalPosition.y(), actual.finalPosition.y(), positionCoordTol);
		assertEquals(out.finalVelocity.x(), actual.finalVelocity.x(), velocityCoordTol);
		assertEquals(out.finalVelocity.y(), actual.finalVelocity.y(), velocityCoordTol);
		assertEquals(out.finalHeading, actual.finalHeading, headingTol);
		assertEquals(out.accelTimeLeft, actual.accelTimeLeft, accelTimeTol);
		assertEquals(out.rotationTimeLeft, actual.rotationTimeLeft, rotationTimeTol);
	}
	
	private void checkComputationExact() {
		Response actual = MotionComputer.compute(in);
		Assert.assertTrue( equals(out, actual));
	}
	
	private boolean equals(Response r1, Response r2)
	{
		if ((r1 == null) && (r2 == null)) return true;
		if ((r1 == null) || (r2 == null)) return false;
		if (r1 == r2) return true;
		if (!r1.finalPosition.equals(r2.finalPosition)) return false;
		if (!r1.finalVelocity.equals(r2.finalVelocity)) return false;
		if (r1.finalHeading != r2.finalHeading) return false;
		if (r1.accelTimeLeft != r2.accelTimeLeft) return false;
		if (r1.rotationTimeLeft != r2.rotationTimeLeft) return false;
		return true;
	}
}
