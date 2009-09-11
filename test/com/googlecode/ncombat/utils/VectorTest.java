package com.googlecode.ncombat.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class VectorTest extends TestCase
{
	public void testAbs()
	{
		Vector v1 = new Vector(3.0, 4.0);
		assertEquals(5.0, v1.abs());
		
		Vector v2 = new Vector(4.0, 3.0);
		assertEquals(5.0, v2.abs());
		
		Vector v3 = new Vector(-3.0, 4.0);
		assertEquals(5.0, v3.abs());
	}
	
	public void testAddSubtractNegate()
	{
		Vector v1 = new Vector(1.0, 2.0);
		Vector v2 = new Vector(10.0, 11.0);
		Vector sum = new Vector(11.0, 13.0);
		Vector diff = new Vector(-9.0, -9.0);
		
		assertEquals(sum, v1.add(v2));
		assertEquals(sum, v2.add(v1));
		
		assertEquals(diff, v1.subtract(v2));
		assertEquals(diff.negate(), v2.subtract(v1));
	}
	
	public void testAngle()
	{
		double sqr3 = Math.sqrt(3.0);
		
		Map<Double,Vector> vectors = new HashMap<Double,Vector>();
		
		vectors.put(0.0, Vector.I);
		vectors.put(30.0, new Vector(sqr3, 1.0));
		vectors.put(45.0, new Vector(1.0, 1.0));
		vectors.put(60.0, new Vector(1.0, sqr3));
		vectors.put(90.0, Vector.J);
		vectors.put(120.0, new Vector(-1.0, sqr3));
		vectors.put(135.0, new Vector(-1.0, 1.0));
		vectors.put(150.0, new Vector(-sqr3, 1.0));
		vectors.put(180.0, Vector.I.negate());
		vectors.put(210.0, new Vector(-sqr3, -1.0));
		vectors.put(225.0, new Vector(-1.0, -1.0));
		vectors.put(270.0, Vector.J.negate());
		vectors.put(315.0, new Vector(1.0, -1.0));
		vectors.put(330.0, new Vector(sqr3, -1.0));
		
		double tol = 1.0e-5;
		
		ArrayList<Double> angles = new ArrayList<Double>(vectors.keySet());
		for (int i = 0 ; i < angles.size() ; i++) {
			double angle1 = angles.get(i);
			Vector vector1 = vectors.get(angle1);
			for (int j = 0 ; j < angles.size() ; j++) {
				double angle2 = angles.get(j);
				Vector vector2 = vectors.get(angle2);
				double angleDiff = angle1 - angle2;
				if (angleDiff < 0.0) angleDiff += 360.0;
				if (angleDiff > 180.0) angleDiff = 360.0 - angleDiff;
				assertEquals("(1) angle1: " + angle1 + ", angle2: " + angle2,
								angleDiff, vector1.angleDegrees(vector2), tol);
				assertEquals("(2) angle1: " + angle1 + ", angle2: " + angle2,
						angleDiff, vector2.angleDegrees(vector1), tol);

			}
		}
	}
	
	public void testCtorsAndGetters()
	{
		Vector v1 = new Vector();
		assertEquals(0.0, v1.getX());
		assertEquals(0.0, v1.getY());
		
		double x = 1.0;
		double y = 3.0;
		Vector v2 = new Vector(x,y);
		assertEquals(x, v2.getX());
		assertEquals(y, v2.getY());
	}
	
	public void testDot()
	{
		Vector v1 = new Vector(1.5, 2.5);
		Vector v2 = new Vector(-2.0, 6.0);
		double dot = 12.0;
		assertEquals(dot, v1.dot(v2));
		assertEquals(dot, v2.dot(v1));
	}
	
	public void testHashCode()
	{
		double x = 27.2;
		double y = 3.1416;
		Vector v = new Vector(x, y);
		int expectedHash = new Double(x).hashCode() ^ 
								new Double(y).hashCode();
		assertEquals(expectedHash, v.hashCode());
	}
	
	public void testIsInfinite()
	{
		Vector v1 = new Vector(Double.POSITIVE_INFINITY, 10.0);
		Vector v2 = new Vector(25.0, Double.NEGATIVE_INFINITY);
		Vector v3 = new Vector(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Vector v4 = new Vector(25.0, 10.0);
		assertEquals(true, v1.isInfinite());
		assertEquals(true, v2.isInfinite());
		assertEquals(true, v3.isInfinite());
		assertEquals(false, v4.isInfinite());
	}
	
	public void testIsNaN()
	{
		Vector v1 = new Vector(Double.NaN, 10.0);
		Vector v2 = new Vector(25.0, Double.NaN);
		Vector v3 = new Vector(Double.NaN, Double.NaN);
		Vector v4 = new Vector(25.0, 10.0);
		assertEquals(true, v1.isNaN());
		assertEquals(true, v2.isNaN());
		assertEquals(true, v3.isNaN());
		assertEquals(false, v4.isNaN());
	}
	
	public void testMultiplyDivide()
	{
		Vector v = new Vector(1.5, 2.50);
		double r = 5.0;
		Vector prod = new Vector(7.5, 12.50);
		Vector quot = new Vector(0.3, 0.50);
		assertEquals(prod, v.multiply(r));
		assertEquals(quot, v.divide(r));
	}
	
	public void testProjCompDistance()
	{
		double x75 = 2.0 * Math.cos( Math.toRadians(75.0));
		double y75 = 2.0 * Math.sin( Math.toRadians(75.0));
		Vector v75 = new Vector(x75, y75);
		Vector v45 = new Vector(2.71828, 2.71828);
		double sqr3 = Math.sqrt(3.0);
		double sqr2 = Math.sqrt(2.0);
		
		double tol = 1.0e-16;

		Vector proj = v75.proj(v45);
		assertEquals(sqr3 / sqr2, proj.getX(), tol);
		assertEquals(sqr3 / sqr2, proj.getY(), tol);
		
		assertEquals(sqr3, v75.comp(v45), tol);
		assertEquals(1.0, v75.distance(v45), tol);
	}
	
	public void testToString()
	{
		Vector v = new Vector(-1.25,3.14);
		String s = "[-1.25,3.14]";
		assertEquals(s, v.toString());
	}
	
	public void testUnit()
	{
		Vector v = new Vector(3.0, 4.0);
		Vector u = new Vector(3.0/5.0, 4.0/5.0);
		assertEquals(u, v.unit());
	}
}
