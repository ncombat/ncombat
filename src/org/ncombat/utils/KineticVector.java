package org.ncombat.utils;



/**
 * KineticVector is the motion handler for all game objects. 
 * @author lmiller
 *
 */
public class KineticVector {
	
	/* Cartesian vector coordinates */
	private double y;
	private double x;
	
	/* Polar vector specification */
	private double magnitude;
	private double angleInRadians;
	
	/**
	 * Constructor for kinetic vector - polar only
	 * @param magnitude
	 * @param angleInRadians
	 */
	public KineticVector(double angleInRadians, double magnitude) {
		this.magnitude = magnitude;
		this.angleInRadians = angleInRadians;
		updateCartesian();
	}
	
	public KineticVector(){
		this.x = 0D;
		this.y = 0D;
		this.magnitude = 0;
	}
	
	public double[] getCartesian(){
		double[] retValue= {angleInRadians,magnitude};
		return retValue;
	}
	
	public double[] getPolar(){
		double[] retValue= {angleInRadians,magnitude};
		return retValue;
	}
	
	public double[] getPolarInDegrees() {
		double[] retValue= {Math.toDegrees(angleInRadians),magnitude};
		return retValue;
	}
	
	public double getAngleInDegrees() {
		return Math.toDegrees(angleInRadians);
	}
	
	public void setAngleInDegrees(double theAngleInDegrees) {
		this.angleInRadians = Math.toRadians(theAngleInDegrees);
	}
	public void setPolar(double newmagnitude, double newAngleInRadians) {
		this.magnitude = newmagnitude;
		this.angleInRadians = newAngleInRadians;
		updateCartesian();
	}
	
	public void setCartesian(double newX, double newY) {
		this.x = newX;
		this.y = newY;
		updatePolar();
	}
	
	private void updateCartesian(){
		this.x = Math.cos( this.angleInRadians ) * this.magnitude;
		this.y = Math.sin( this.angleInRadians ) * this.magnitude;
	}
	
	private void updatePolar(){
		this.magnitude = Math.sqrt( this.x * this.x + this.y * this.y );
		this.angleInRadians = Math.acos( this.x / this.magnitude );
	}
	
	public void setX(double newX){
		x=newX;
		updatePolar();
	}
	
	public void setY(double newY){
		y = newY;
		updatePolar();
	}

	public void setAngleInRadians(double newAngleInRadians){
		this.angleInRadians= newAngleInRadians;
		updateCartesian();
	}
	
	public void setMagnitude(double newMagnitude){
		this.magnitude =  newMagnitude;
		updateCartesian();
	}
	
	public void addVector (KineticVector v2) {
		this.x = x + v2.getCartesian()[0];
		this.y = y + v2.getCartesian()[1];
		updatePolar();
	}
	
	public void showInfo(){
		updateCartesian();
		System.out.println("Magnitude = " + this.magnitude 
				+ " \n Angle_rad = " + this.angleInRadians
				+ " \n Angle_deg = " + Math.toDegrees(this.angleInRadians)
				+ " \n X = " + this.x
				+ " \n Y = " + this.y);
	}
	
	private void info(){
		/*
	//	position(t) = [x0] + [v0]*t + (1/2)*[a0]*t2
	//	velocity(t) = [v0] + [a0]*t
	//	acceleration(t) = [a0] 
		
		Constant Acceleration Equations
		For an object that has an initial velocity u and that is moving in a straight line with constant
		acceleration a, the following equations connect the final velocity v and displacement s in a given
		time t.
	
		v = u+at (1)
		s = 1/2(u+v)t
		s = ut+1/2at^2
		s = vt - 1/2at^2
		v^2 = u^2 + 2as
		
		Note: These equations cannot be used if the acceleration is not constant.
		
		Worked Example 1. 1 1
Amotorbikejoinsa motorwaytraveling at10 ms , andincreases speed to30ms with aconstant 2
acceleration of 1.25 m s along the straight road. How much time does this take, and how far does
the bike travel in this time?
Solution. 1 1
Firstly consider what information has been given, namely u = 10 m s , v = 30 m s and 2
a = 1.25 m s .
The question asks for the values of t and then s.
The equation that connects u,v,a and t is (1). Inserting the known values into (1) gives:
30 = 10+1.25t
20 = 1.25t
? t = 16 s
Now either equation (2), (3), (4) or (5) can be used to calculate s. For example, using (2):
1 1
s = (u+v)t= (10+30) × 16 = 320 m.
2 2
				
				*
				*You can convert polar co-ordinates to Cartesian x,y co-ordinates with:

// polar to Cartesian
double x = Math.cos( angleInRadians ) * radius;
double y = Math.sin( angleInRadians ) * radius;

// Cartesian to polar.
double radius = Math.sqrt( x * x + y * y );
double angleInRadians = Math.acos( x / radius );
				*
				*
				*/
	}

	public double getY() {
		return y;
	}

	public double getX() {
		return x;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getAngleInRadians() {
		return angleInRadians;
	}



	
}

