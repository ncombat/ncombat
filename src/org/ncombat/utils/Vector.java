package org.ncombat.utils;

/**
 * An immutable two-dimensional vector.
 */
public class Vector
{
	/** The unit vector along the Cartesian x axis. */
	public static final Vector I = new Vector(1.0, 0.0);
	
	/** The unit vector along the Cartesian y axis. */
	public static final Vector J = new Vector(0.0, 1.0);
	
	/** The two-dimensional zero vector. */
	public static final Vector ZERO = new Vector(0.0, 0.0);
	
	private static final double PI = Math.PI;
	private static final double TWO_PI = 2.0 * Math.PI;
	
	/**
	 * Constructs a vector using the specified coordinates, where theta is
	 * expressed in radians.
	 */
	public static Vector polar(double r, double theta) {
		return new Vector( r * Math.cos(theta), r * Math.sin(theta));
	}
	/**
	 * Constructs a vector using the specified coordinates, where theta is
	 * expressed in radians.
	 */
	public static Vector polarDegrees(double r, double theta) {
		double thetaDegrees = Math.toRadians(theta);
		return polar(r, thetaDegrees);
	}
	
	/*
	 * Returns the angle equivalent to the given angle (in radians) and within
	 * the interval -PI (exclusive) to PI (inclusive).
	 */
	public static double stdAngle(double angle)
	{
		while (angle > PI) {
			angle -= TWO_PI;
		}
		
		while (angle <= -PI) {
			angle += TWO_PI;
		}
		
		return angle;
	}
	
	/*
	 * Returns the angle equivalent to the given angle (in degrees) and within
	 * the interval -180 (exclusive) to 180 (inclusive).
	 */
	public static double stdAngleDegrees(double angle)
	{
		while (angle > 180.0) {
			angle -= 360.0;
		}
		
		while (angle <= -180.0) {
			angle += 360.0;
		}
		
		return angle;
	}
	
	/**
	 * Constructs a unit vector with the orientation expressed in radians.
	 */
	public static Vector unit(double theta) {
		double x = Math.cos(theta);
		double y = Math.sin(theta);
		return new Vector(x,y);
	}
	
	/**
	 * Constructs a unit vector with the orientation expressed in degrees.
	 */
	public static Vector unitDegrees(double theta) {
		return unit(Math.toRadians(theta));
	}
	
	private double x;

	private double y;
	
	/**
	 * Constructs a zero vector.
	 */
	public Vector() {
	}
	
	/**
	 * Constructs a vector having the specified coordinates.
	 */
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the sum of this vector with another vector.
	 */
	public Vector add(Vector that)
	{
		double sumx = this.x + that.x;
		double sumy = this.y + that.y;
		return new Vector(sumx, sumy);
	}
	
	/**
	 * Returns the angle (in radians) between this vector and another
	 * vector.
	 */
	public double angle(Vector that)
	{
		double angle = that.theta() - this.theta();
		return stdAngle(angle);
	}
	
	/**
	 * Returns the angle (in degrees) between this vector and another
	 * vector.
	 */
	public double angleDegrees(Vector that) {
		return Math.toDegrees(this.angle(that));
	}
	
	/**
	 * Returns the component of this vector in a given direction.
	 */
	public double comp(Vector dir) {
		return this.dot(dir.unit());
	}
	
	
	/**
	 * Returns the perpendicular distance to a vector.
	 */
	public double distance(Vector that) {
		return this.subtract(this.proj(that)).r();
	}
	
	/**
	 * Returns the quotient of this vector divided by a scalar dividend.
	 */
	public Vector divide(double divisor)
	{
		double divx = this.x / divisor;
		double divy = this.y / divisor;
		return new Vector(divx, divy);
	}
	
	/**
	 * Returns the dot product of this vector and another vector.
	 */
	public double dot(Vector that) {
		return (this.x * that.x) + (this.y * that.y); 
	}
	
	@Override
	public boolean equals(Object o) {
		Vector that = (Vector) o;
		return ((this.x == that.x) && (this.y == that.y));
	}
	
	@Override
	public int hashCode()
	{
		long xbits = Double.doubleToLongBits(x);
		int xhash = (int)( xbits ^ (xbits >>> 32) );
		
		long ybits = Double.doubleToLongBits(y);
		int yhash = (int)( ybits ^ (ybits >>> 32));
		
		return xhash ^ yhash;
	}
	
	/**
	 * Returns whether or not any component of this vector is infinite.
	 */
	public boolean isInfinite() {
		return (Double.isInfinite(x) || Double.isInfinite(y));
	}
	
	/**
	 * Returns whether or not any component of this vector is not a number
	 * (NaN).
	 */
	public boolean isNaN() {
		return (Double.isNaN(x) || Double.isNaN(y));
	}

	/**
	 * Returns the product of this vector and a scalar multiplicand.
	 */
	public Vector multiply(double factor)
	{
		double prodx = this.x * factor;
		double prody = this.y * factor;
		return new Vector(prodx, prody);
	}

	/**
	 * Returns the unary negation of this vector.
	 */
	public Vector negate() {
		return new Vector(-this.x, -this.y);
	}

	/**
	 * Returns the projection of this vector onto another vector.
	 */
	public Vector proj(Vector that) {
		return that.unit().multiply( this.comp(that)); 
	}

	/**
	 * Returns the magnitude of this vector, often denoted as <code>r</code> in
	 * polar coordinates.
	 */
	public double r() {
		return Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2));
	}

	/**
	 * Returns the difference between this vector and another vector.
	 */
	public Vector subtract(Vector that)
	{
		double diffx = this.x - that.x;
		double diffy = this.y - that.y;
		return new Vector(diffx, diffy);
	}
	
	/**
	 * Returns the angular coordinate of this vector in polar coordinates, often
	 * denoted as <code>theta</code>, in radians.
	 */
	public double theta() {
		return stdAngle( Math.atan2(y, x));
	}
	
	/**
	 * Returns the angular coordinate of this vector in polar coordinates, often
	 * denoted as <code>theta</code>, in degrees.
	 */
	public double thetaDegrees() {
		return Math.toDegrees( theta());
	}
	
	@Override
	public String toString() {
		return "[" + x + "," + y + "]";
	}
	
	/**
	 * Returns the unit vector in the same direction as this vector.
	 */
	public Vector unit() {
		return divide(r());
	}
	
	/**
	 * Returns the x coordinate of this vector.
	 */
	public double x() {
		return x;
	}
	
	/**
	 * Returns the y coordinate of this vector.
	 */
	public double y() {
		return y;
	}

}
