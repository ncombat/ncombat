package org.ncombat.components;


import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.ncombat.utils.NCombatUtils;


/**
 * @author lmiller
 *
 *	Ship class is primary game object - receives commands from game server.
 *
 */
public class Ship {
	
	public String guid;
	private double y;
	private double x;
	private double course; // TODO: degrees or radians?
	private double velocity;
	private double heading;
	private int power;
	private int missiles;
	private int id;
	private int damage;
	private int shield1;
	private int shield2;
	private double remainingHeadingChange;
	private StringBuffer messages;
	private int pendingAccelerationTime;
	private int accelerationRate;
	private int laserCooldown;
	private MissileTube tube1;
	private MissileTube tube2;
	private int freeMissileTube;
	private boolean negativeTurn;
	NumberFormat xyForm = new DecimalFormat("0000.0");
	

	class MissileTube {
		
		// note there is NOT a missile object - server controls missile generation, movement and lifecycle.
		// Ship controls firing and tubes only.
		int remainingLoadTime = 0;
		
		void update() {
			if (remainingLoadTime > 0) { remainingLoadTime--; }
			// special case: if we ran out of missiles, tubes were disabled by setting load time to -1. 
			// If missles are restored, need to reload tubes.
			if (missiles > 0 && remainingLoadTime == -1) {
				reload();
			}
		}
		
		void fire(int target) {
			if (remainingLoadTime==0) {
				// TODO: here is where I tell the server I am firing at target - server handles from there
				reload();
			}
		}
		
		void reload() {
			if (missiles > 0) {	
				missiles--;
				remainingLoadTime=NCombatUtils.MISSILE_LOAD_TIME_SECONDS;
			}
			else {
				remainingLoadTime = -1; // tube will never be ready
			}	
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO test implementation of main

	}
	
	public Ship() {
		course = 0.0;
		heading = 0.0; 
		power = 1000;
		velocity = 0.0;
		missiles = 25;
		damage = 0;
		shield1 = 100;
		shield2 = 100;
		id = 1;
		tube1 = new MissileTube();
		tube2 = new MissileTube();
		messages = new StringBuffer();
	}
	
	/*
	 * A - Accelerate ship. 
	 * Ax,y accelerate from -5 to 5 km/sec^2 (x) from 0 to 300 seconds of acceleration (y). 
	 * Ex: A2,30 accelerate at 2 km/sec^2 for 30 seconds.
	 * Final speed = acceleration * time of acceleration
	 * Negative values of acceleration allow you to go in reverse.
	 * One km/sec^2 acceleration costs 50 energy units
	 */
	
	public void accelerate(int rate, int duration){
		pendingAccelerationTime = duration;
		accelerationRate = rate;
	}
	
		
/*
 * B - toggles brief mode on and off. When it is on, it deletes headers to speed printout.
*/
	public void setBrief() {
		
	}
	
/*
 * E - send messages. After issuing E command, program asks you to enter a message. Only 69 characters are sen, null line sends no message. To send to just one person type: /3 Hi there (where 3 is the ship number you want to receive private messages.) Otherwise everyone receives everyone else's messages. 
*/
	public void sendMessage(String message){
		
	}
	
/*F - full power to both shields. Faster than doing S1,25S2,25 to put power on shields.
*/
	public void fullShieldPower(){
		
	}
	
/*G - gravimetric sensor range adjustment, Gx. When signing on, sensor range is 30000 KM. This command allows a player to alter the time it takes for status printout on ships.
*/
	public void changeSensorRange(int distance) {
		
	}
	
/*H - gives a brief list of commmands.
*/
	public void help() {
		
	}
	
/*I - intelligence command. IX,Y here are the subcommands:
   1. Print rotation and speed to attain a parallel course
        with ship n. Ex: I1,3 would print:
        SP      ROT     SPD or ROT      SPD
        3       -120.5  10      59.5    -10
        
        Since a ship can go forward or reverse the player has a choice of turning in either of two directions and using the appropriate engine. Since 59.5 is closer, R59.5 is issued. After rotation is complete (about 10 seconds) A-5,2 is issues then the ships will be stationary relative to each other (no matter what angles and speeds ships had been flying). Once parallel, you can rotate your ship to zero the azimuth and fdire, or accelerate to close in.

   2. Find range and azimuth to center of combat zone. Ex: I2 prints AZ: 0180 RNG: 27034 TO CENTER.
        
   3. Print out commander's name, terminal number, user number and kills of ships presently on. EX: I3,2 would give data on ship 2 only. I3 would print data on all ships.

   4. List of last 30 users giving ship, status, kills made, terminal number, usernumber, time on, time off and commanders name.
      STATUS:  = Still playing
                HNG = Ship hung up his phone.
                LPR = Ship lost all power.
                DGN = Ship destroyed by gorn base n.
                DDS = Ship self-destructed.
                PUR = Ship was purged by a monitor.

   5. Map option. Prints map of area out to sensor range (15 * 15 size). Sx = ship x, Gx = Gorn x, **  two or more ships within square. Zero degrees is straight up, negative degrees left and positive degrees righthand side.

   6. Prints damage on, azimuth to and range to gorn bases still on, if within range set by G command or +/- 15 degrees azimuth.
*/
	public void intelligence() {
		
	}
	
	
/* L = fire lasers. 
 * LX Fire 0 to 2000 energy, spread is +/- 1 degree/ Lasers hit all targets within spread regardless of range. 
 * The more power applied to lasers, the longer it takes for the lasers to cool down. 
 * The exact equation for time require for lasers to cool is (POWER/40)+10
 * Ex: L1000 (1000/40)+10 = 35 seconds.
 * Damage caused depends on laser power, range to target, power on shield and shield hit. Since shield 1 is stronger than shield 2, 
 * CD = Coefficient of Damage = (laser pow * 12 * (33-shield pow)/Range/1)/2
 * hit on shield 1 causes:
 * CD percent damage on shield 1
 * CD percent damage on ship itself.
 * hit on shield 2 causes 
 *  CD*2 percent damage on shield 2
 *  CD percent damage on ship itself
*/
	public void fireLaser(int units) {
		
		if ((laserCooldown) == 0) {
			if (power > units) {
				power = power-units;
				laserCooldown=(units/40) + 10;
				messages.append("Firing laser\n");
			}
			else {
				messages.append("Laser not fired, insuffienct power for that setting\n");
			}
		}
		else {		
			messages.append("Lasers not operating, cooling for " + laserCooldown  + " seconds\n");
		}
	}
	
	
/* M - fire missile. MX,Y or MX MY 
 * 2 tubes available to fire, computer fires tubes if they are ready. 
 * It takes 60 seconds to load a tube. Range of missiles is 20000 KM. 
 * Ships have a maximum capacity of 25 missiles. 
 * Missiles are restored after every five kills. 
 * Target must be within +/- 5 degrees azimuth to allow a hit. 
 * Missiles are not fired at non-existent ships or gorns. 
 * Ex: M5 fires one missile at ship 5, if tube 1 were not ready yet, computer would fire tube 2 automatically if it were ready. 
 * For purposes of missile targeting, gorn bases are considered targets 21, 22, 23 and 24 respectively.
 * CD = coeff of damage = (102-(shield pow * 4))/2
 * hit on shield 1 causes:
 * CD percent damage on shield 1
 * CD percent damage on ship itself.
 * hit on shield 2 causes
 * CD*2 percent damaage on shield 2
 * CD percent damage on ship itself
*/
	public void fireMissile(int target){
		MissileTube tube = null;
		if (tube1.remainingLoadTime==0) tube = tube1;
		if (tube2.remainingLoadTime==0) tube = tube2;
		if (tube == null)
		{
			messages.append("Can't fire missile at target " + target + " : " + (missiles == 0 ? "No missiles left" : "tubes not ready"));
		}
		else {
		// fire the missile
				messages.append ("Firing missile from tube " + freeMissileTube + " at target " + target  + "\n");
				tube.fire(target);
		}
	}
		
	/*N - null command, If player wishes to get the latest information about the position of ships, ships destroyed and other related information, and have used only the b,c,e,g,i usually n */
	public void noCommand() {
		
	}
	
	/* P - distribute repair rate between two shields and ship. Px,y. '
	 * The maximum rate of 1.5 percent per second, can be distributed in any manner. 
	 * Px,y is the format, x = rate on shield 1 y = rate on shield 2. 
	 * Ship gets 1.5-x-y
	 * Ex: P1,0 (or P1) gives shield 1 1 percent repair per second, shield 2 gets none, 
	 * so ship gets .5 percent per second. Absent parameters = 0.
	 * Ex. P gives full power to ship. Repair automatically starts 40 seconds after damage occurs. Repair previously taking place is suspended after damage occurs. Repair continues through any action of yours however.
	 * Distribution is initally .5 percent per shield and ship. It is advised to keep repair off by staggering missiles and lasers or if out of missile range by shooting lasers about 1000.
	 */
	public void distributePower(){
		
	}
	
/* R - rotate ship. RX,y Positive or negative rotation. 0 to +/- 360 (X)
 * ex: R180 will rotate at 6 deg/sec which will take about 30 seconds.
 * Rate of rotation can also be varied. RX R180, 1 will rotate at 1 deg/sec instead of 6 dec/sec. 
 * This will take about 180 seconds. Maximum rate of rotation is 6 deg/sec.
*/
	public void rotate(int change) {
		remainingHeadingChange = change;
		if (change < 0) {negativeTurn = true;} else { negativeTurn = false;}
	}
	
/*S - individual shield power adjustment. Sx,y from 0 to 25 power/sec. 
ex: S1,13 shield 1 to power 13.
ex: S2,25 shield 2 to power 25.
Ex FS2 turns S1 and S2on to full and then turns s2 off, faster than S1,25. Shield come on automatically if you sign on within 20000 km of another ship. Their shields do not detect you, however.
*/
	//STOP - destructs ship and logs you off game, but only if you are at least 30000 away from the nearest ship. The only other ways out are through being destroyed by someone or something else, or by losing all power (which isn't hard).
	public void stop(){
		
	}
	
	// Vx track ship x. The only data you will receive will be the general data for the ship you are tracking (first line) and a list of all the existing ships and their courses toward you (second line) and your two data lines. When the ship you are tracking is destroyed, you go out of tracking mode and back into the normal output mode. IF tracking while not in breif mode, the second line our output is headed by a #S heading# entry for all existing ships.
	public void trackShip() {
		
	}
	
	// Z - zero power on both shield. Useful utility command to help conserve ships energy.
	public void zeroShields() {
		shield1=0;
		shield2=0;
	}
	
	public void update(){	
		
		if (power >= ((accelerationRate) * NCombatUtils.ENGINE_POWER_CONSUMPTION)) {
			if (pendingAccelerationTime > 0) {
				velocity = velocity + accelerationRate;
				pendingAccelerationTime--;
				power = power-((accelerationRate) * NCombatUtils.ENGINE_POWER_CONSUMPTION);
			}
		}
		else {messages.append("No acceleration, power depleted - burn will resume when power restored\n");}
		
		if (remainingHeadingChange != 0) { // there is a heading change still pending

			// if remainingHeadingChange is > -6 and < 6, then add the value to heading
			
			if ((remainingHeadingChange > -6) && (remainingHeadingChange < 6)) {
				heading = heading + remainingHeadingChange;
				remainingHeadingChange = 0;
			}
			else {
				// need to increment
				if (negativeTurn) { // less than 0, CCW rotation
					heading = heading - NCombatUtils.BASE_TURN_RATE;
					remainingHeadingChange = remainingHeadingChange + NCombatUtils.BASE_TURN_RATE;
				}
				else { // CW rotation
					heading = heading + NCombatUtils.BASE_TURN_RATE;
					remainingHeadingChange = remainingHeadingChange - NCombatUtils.BASE_TURN_RATE;
				}
			}

		}
		// adjust values
		if (heading < 0) { heading = 360 + heading; }
		if (heading > 360) { heading = heading - 360; }
	
		// adjust position
		
		x = x + (velocity * (Math.cos(Math.toRadians(heading))));
		y = y + (velocity * (Math.sin(Math.toRadians(heading))));
		
		// keep ship within boundaries
		if (x > NCombatUtils.ARENA_SIZE) { x = 0; }
		if (x < 0) { x = NCombatUtils.ARENA_SIZE; }
		if (y > NCombatUtils.ARENA_SIZE) { y = 0; }
		if (y < 0) { y = NCombatUtils.ARENA_SIZE; }
		
		// missile time 
		tube1.update();
		tube2.update();
		
		// laserTime
		if (laserCooldown > 0) {laserCooldown--;}
		
		//update power and damage
		power = power + NCombatUtils.BASE_POWER_PRODUCTION_RATE;
		if (power> 2000) {power = 2000;}
		if (damage > 0) { damage = damage - NCombatUtils.BASE_REPAIR_RATE; }
	}
	
	
	public String getStatus() {
		StringBuffer retValue = new StringBuffer();
		retValue.append(messages);
		retValue.append("SP\tDMG\tP1\tP2\tSpeed\tPower\tCourse\tX\tY\tAzimuth\tRange\tHeading\n");
		retValue.append(id + "\t" + damage + "\t" + shield1 + "\t" + shield2 + "\t" + velocity + "\t" + power + "\t" + heading + "\t" + xyForm.format(x) + "\t" + xyForm.format(y) +"\t\t");
		messages=new StringBuffer();
		this.update();
		return retValue.toString();
	}

	public int getAccelerationRate() {
		return accelerationRate;
	}

	public void setAccelerationRate(int accelerationRate) {
		this.accelerationRate = accelerationRate;
	}

	public double getCourse() {
		return course;
	}

	public void setCourse(double course) {
		this.course = course;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public StringBuffer getMessages() {
		return messages;
	}

	public void setMessages(StringBuffer messages) {
		this.messages = messages;
	}

	public int getMissiles() {
		return missiles;
	}

	public void setMissiles(int missiles) {
		this.missiles = missiles;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getShield1() {
		return shield1;
	}

	public void setShield1(int shield1) {
		this.shield1 = shield1;
	}

	public int getShield2() {
		return shield2;
	}

	public void setShield2(int shield2) {
		this.shield2 = shield2;
	}

	public int getId() {
		return id;
	}

	public int getRemainingHeadingChange() {
		return 0;
	}

	public int getTube1Time() {
		return tube1.remainingLoadTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ship other = (Ship) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public int getTube2Time() {
		return tube2.remainingLoadTime;
	}

	public double getVelocity() {
		return velocity;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	
}
