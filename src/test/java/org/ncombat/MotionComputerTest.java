package org.ncombat;

import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.ncombat.utils.Vector;

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
		
		out.finalPosition = new Vector(-82.3256664135248000, -146.5624740038340000);
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
		
		in.intervalLength = 7.0;
		in.initialPosition = Vector.polarDegrees(5.0, 20.0);
		in.initialVelocity = Vector.ZERO;
		in.initialHeading = Math.toRadians(60.0);
		in.accelRate = 5.0;
		in.accelTime = 7.0;
		in.rotationRate = Math.toRadians(6.0);
		in.rotationTime = 7.0;
		
		out.finalPosition = new Vector(38.0198199330568000, 117.7011992794170000);
		out.finalVelocity = new Vector(5.3534405624444400, 33.8002934598564000);
		out.finalHeading = Math.toRadians(102.0);
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testAccelWithRot2()
	{
		// Negative acceleration
		// Positive rotation
		// Starting in motion
		// Course == heading
		// Accel beyond interval
		// Rotation coincident with interval
		
		in.intervalLength = 3.0;
		in.initialPosition = Vector.polarDegrees(5.0, 20.0);
		in.initialVelocity = Vector.polarDegrees(9.0, -130.0);
		in.initialHeading = Math.toRadians(-130.0);
		in.accelRate = -2.75;
		in.accelTime = 10.0;
		in.rotationRate = Math.toRadians(4.5);
		in.rotationTime = 3.0;
		
		out.finalPosition = new Vector(-5.4815159158057600, -8.9140624984655900);
		out.finalVelocity = new Vector(-1.2721256060954100, -0.0109876530150999);
		out.finalHeading = Math.toRadians(-116.5);
		out.accelTimeLeft = 7.0;
		out.rotationTimeLeft = 0.0;
		
		checkComputation();
	}
	
	@Test public void testAccelWithRot3()
	{
		// Positive acceleration
		// Negative rotation
		// Starting in motion
		// Course != heading
		// Accel coincident with interval
		// Rotation beyond interval
		
		in.intervalLength = 4.0;
		in.initialPosition = Vector.polarDegrees(25.0, 120.0);
		in.initialVelocity = Vector.polarDegrees(9.0, -90.0);
		in.initialHeading = Math.toRadians(-130.0);
		in.accelRate = 9.9;
		in.accelTime = 4.0;
		in.rotationRate = Math.toRadians(-3.6);
		in.rotationTime = 55.5;
		
		out.finalPosition = new Vector(-68.2080789907288000, -70.4499242722185000);
		out.finalVelocity = new Vector(-28.9792914469691000, -35.8351181134541000);
		out.finalHeading = Math.toRadians(-144.4);
		out.accelTimeLeft = 0.0;
		out.rotationTimeLeft = 51.5;
		
		checkComputation();
	}
	
	@Test public void testAccelWithRot4()
	{
		// Negative acceleration
		// Negative rotation
		// Starting in motion
		// Course != heading
		// Accel beyond interval
		// Rotation beyond interval
		
		in.intervalLength = 5.0;
		in.initialPosition = Vector.polarDegrees(200.0, 120.0);
		in.initialVelocity = Vector.polarDegrees(50.0, 45.0);
		in.initialHeading = Math.toRadians(180.0);
		in.accelRate = -2.5;
		in.accelTime = 6.0;
		in.rotationRate = Math.toRadians(-2.5);
		in.rotationTime = 6.0;
		
		out.finalPosition = new Vector(107.9029426355630000, 347.7146141001500000);
		out.finalVelocity = new Vector(47.7564154574216000, 33.9972003096914000);
		out.finalHeading = Math.toRadians(167.5);
		out.accelTimeLeft = 1.0;
		out.rotationTimeLeft = 1.0;
		
		checkComputation();
	}
	
	@Test public void testLinkage1()
	{
		// Accel, Rotation within interval
		// Accel ends first.

		// The combined request.
		in.intervalLength = 10.0;
		in.initialPosition = Vector.polarDegrees(200.0, 120.0);
		in.initialVelocity = Vector.polarDegrees(50.0, 45.0);
		in.initialHeading = Math.toRadians(180.0);
		in.accelRate = -2.5;
		in.accelTime = 3.0;
		in.rotationRate = Math.toRadians(-2.5);
		in.rotationTime = 7.0;
		
		// First leg of the separated requests.
		MotionComputer.Request in2 = new MotionComputer.Request();
		in2.intervalLength = in.accelTime;
		in2.initialPosition = in.initialPosition;
		in2.initialVelocity = in.initialVelocity;
		in2.initialHeading = in.initialHeading;
		in2.accelRate = in.accelRate;
		in2.accelTime = in.accelTime;
		in2.rotationRate = in.rotationRate;
		in2.rotationTime = in.rotationTime;
		out = MotionComputer.compute(in2);
		
		// Second leg of the separated requests.
		in2.intervalLength = out.rotationTimeLeft;
		in2.initialPosition = out.finalPosition;
		in2.initialVelocity = out.finalVelocity;
		in2.initialHeading = out.finalHeading;
		in2.accelRate = 0.0;
		in2.accelTime = 0.0;
		in2.rotationRate = in.rotationRate;
		in2.rotationTime = out.rotationTimeLeft;
		out = MotionComputer.compute(in2);
		
		// Third leg of the separated requests.
		in2.intervalLength = in.intervalLength - Math.max(in.accelTime, in.rotationTime);
		in2.initialPosition = out.finalPosition;
		in2.initialVelocity = out.finalVelocity;
		in2.initialHeading = out.finalHeading;
		in2.accelRate = 0.0;
		in2.accelTime = 0.0;
		in2.rotationRate = 0.0;
		in2.rotationTime = 0.0;
		out = MotionComputer.compute(in2);
		
		
		checkComputation();
	}
	
	@Test public void testLinkage2()
	{
		// Accel, Rotation within interval
		// Rotation ends first.
		
		// The combined request.
		in.intervalLength = 10.0;
		in.initialPosition = Vector.polarDegrees(200.0, 120.0);
		in.initialVelocity = Vector.polarDegrees(50.0, 45.0);
		in.initialHeading = Math.toRadians(180.0);
		in.accelRate = -2.5;
		in.accelTime = 9.0;
		in.rotationRate = Math.toRadians(-2.5);
		in.rotationTime = 3.0;
		
		// First leg of the separated requests.
		MotionComputer.Request in2 = new MotionComputer.Request();
		in2.intervalLength = in.rotationTime;
		in2.initialPosition = in.initialPosition;
		in2.initialVelocity = in.initialVelocity;
		in2.initialHeading = in.initialHeading;
		in2.accelRate = in.accelRate;
		in2.accelTime = in.accelTime;
		in2.rotationRate = in.rotationRate;
		in2.rotationTime = in.rotationTime;
		out = MotionComputer.compute(in2);
		
		// Second leg of the separated requests.
		in2.intervalLength = in.intervalLength - in.rotationTime;
		in2.initialPosition = out.finalPosition;
		in2.initialVelocity = out.finalVelocity;
		in2.initialHeading = out.finalHeading;
		in2.accelRate = in.accelRate;
		in2.accelTime = out.accelTimeLeft;
		in2.rotationRate = 0.0;
		in2.rotationTime = 0.0;
		out = MotionComputer.compute(in2);
		
		checkComputation();
	}
	
	private void checkComputation()
	{
		double positionCoordTol = 1.0e-5;
		double velocityCoordTol = 1.0e-5;
		double headingTol = 1.0e-14;
		double accelTimeTol = 1.0e-14;
		double rotationTimeTol = 1.0e-14;
		
		MotionComputer.Response actual = MotionComputer.compute(in);
		
		assertEquals(out.finalHeading, actual.finalHeading, headingTol);
		assertEquals(out.accelTimeLeft, actual.accelTimeLeft, accelTimeTol);
		assertEquals(out.rotationTimeLeft, actual.rotationTimeLeft, rotationTimeTol);
		assertEquals(out.finalVelocity.x(), actual.finalVelocity.x(), velocityCoordTol);
		assertEquals(out.finalVelocity.y(), actual.finalVelocity.y(), velocityCoordTol);
		assertEquals(out.finalPosition.x(), actual.finalPosition.x(), positionCoordTol);
		assertEquals(out.finalPosition.y(), actual.finalPosition.y(), positionCoordTol);
	}
	
	private void checkComputationExact() {
		MotionComputer.Response actual = MotionComputer.compute(in);
		Assert.assertTrue( equals(out, actual));
	}
	
	private boolean equals(MotionComputer.Response r1, MotionComputer.Response r2)
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
