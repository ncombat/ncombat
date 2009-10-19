package org.ncombat.utils;

/**
 * Performs various mathematical calculations that are ubiquitous in Ncombat,
 * primarily related to the spatial relationships between the combatants.
 * 
 * <p>
 * IMPORTANT NOTE:
 * 
 * <p>
 * Angles in Ncombat are reported to the user and accepted from the user in
 * degrees. Furthermore, the orientation of the user's polar coordinate system
 * is different than the system normally used in pure mathematics. In this
 * class, these user angles are referred to as "degree angles" for short, but
 * more specifically they are angles measured in degrees where clockwise
 * rotation yields positive angles and counter-clockwise rotation yields
 * negative angles. When headings are expressed in degrees in this class
 * ("degree headings"), zero degrees is along the positive y-axis, headings on
 * the left of the axis are negative and headings on the right of the axis are
 * positive.
 * 
 * <p>
 * This is in contrast to the standard polar coordinate system used in
 * mathematics, in which angles are measured in radians and counter-clockwise
 * rotation yields positive angles, clockwise rotation thus yielding negative
 * ones. Standardized angles run from -PI (exclusive) to PI (inclusive). We call
 * those "radian angles" in this class. Headings (which we call
 * "radian headings") corresponding to theta in the standard (r,theta)
 * coordinate system, are zero along the positive x-axis, with positive headings
 * above the axis and negative ones below it.
 * 
 * <p>
 * When quantities in this class are referred to as "degree" quantities or
 * "radian" quantities, it is to be understood that the quantities are also with
 * respect to the corresponding coordinate conventions described above.
 */
public class NcombatMath
{
	/**
	 * Calculates the radian angle through which a ship would need to rotate in
	 * order to be pointing the same direction it is moving.
	 */
	public static double radianCourse(Vector velocity, double radianHeading) {
		if (velocity.equals(Vector.ZERO)) return 0.0;
		return Vector.stdAngle( velocity.theta() - radianHeading);
	}
	
	/**
	 * Calculates the degree angle through which a ship would need to rotate in
	 * order to be pointing the same direction it is moving.
	 */
	public static double degreeCourse(Vector velocity, double radianHeading) {
		return degreeAngle( radianCourse(velocity, radianHeading));
	}
	
	/**
	 * Calculates the radian angle through which the velocity vectory of object 2
	 * would have to rotate to put it on a course directly to object 1. A course
	 * of zero indicates that object 2 is moving directly towards object 1.
	 */
	public static double radianCourse(Vector pos1, Vector pos2, Vector velocity2)
	{
		double dirFrom2To1 = pos1.subtract(pos2).theta();
		double radianCourse = dirFrom2To1 - velocity2.theta();
		return Vector.stdAngle(radianCourse);
	}
	
	/**
	 * Calculates the degree angle through which the velocity vectory of object 2
	 * would have to rotate to put it on a course directly to object 1. A course
	 * of zero indicates that object 2 is moving directly towards object 1.
	 */
	public static double degreeCourse(Vector pos1, Vector pos2, Vector velocity2) {
		return degreeAngle( radianCourse(pos1, pos2, velocity2));
	}

	/**
	 * Determines the radian azimuth of object 2 with respect to object 1. This
	 * is the angle through which object 1 would need to rotate to be pointing
	 * directly at object 2.
	 */
	public static double radianAzimuth(Vector pos1, double heading1, Vector pos2) {
		Vector pathFrom1To2 = pos2.subtract(pos1);
		double finalHeading = pathFrom1To2.theta();
		double azimuth = finalHeading - heading1;
		return azimuth;
	}

	/**
	 * Determines the degree azimuth of object 2 with respect to object 1. This
	 * is the angle through which object 1 would need to rotate to be pointing
	 * directly at object 2.
	 */
	public static double degreeAzimuth(Vector pos1, double radianHeading1, Vector pos2) {
		return degreeAngle(radianAzimuth(pos1, radianHeading1, pos2));
	}

	/**
	 * Determines the degree angle equivalent to the given radian angle.
	 */
	public static double degreeAngle(double radianAngle)
	{
		double degreeAngle = -Math.toDegrees(radianAngle);
		return Vector.stdAngleDegrees(degreeAngle);
	}
	
	/**
	 * Determines the radian angle equivalent to the given degree angle.
	 */
	public static double radianAngle(double degreeAngle)
	{
		double radianAngle = -Math.toRadians(degreeAngle);
		return Vector.stdAngle(radianAngle);
	}

	/**
	 * Calculates the degree heading equivalent to the given radian heading.
	 */
	public static double degreeHeading(double radianHeading)
	{
		double degreeHeading = 90.0 - Math.toDegrees(radianHeading);
		return Vector.stdAngleDegrees(degreeHeading);
	}
	
	/**
	 * Calculates the radian heading equivalent to the given degree heading.
	 */
	public static double radianHeading(double degreeHeading)
	{
		double radianHeading = Math.toRadians(90.0 - degreeHeading);
		return Vector.stdAngle(radianHeading);
	}
}
