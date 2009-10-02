package com.googlecode.ncombat;

public class MaxAccelTime
{
	public long lastUpdateTime; // ms.

	// Ship state
	boolean enginesBlown;
	double heat;
	double accelRate;
	double accelTimeLeft;
	double rotateRate;
	double rotateTimeLeft;

	// Ship specifications
	double heatWarning = 5000;
	double heatMax = 8000;
	double heatPerAccel = 35;

	public MaxAccelTime() {
	}

	public void process(long updateTime)
	{
		// Calculate the time that has passed since our last update (in seconds).
		double tDiff = ((double)(updateTime - lastUpdateTime)) / 1000.0;

		// We will compute the actual time accelerating, which is subject to the
		// player's commands, engine overheat, energy exhaustion, and the passing
		// of time.
		double tAcc = 0.0;
		double tBlow = 0.0;

		double absAccelRate = Math.abs(accelRate);

		if ((absAccelRate > 0.0) && (accelTimeLeft > 0.0))
		{
			// In no case will we accelerate more than the requested time or more than
			// the time since the last update.
			tAcc = Math.min(accelTimeLeft, tDiff);

			// Determine the amount of time for which continuous acceleration at the
			// requested rate would destroy the engines.

			tBlow = (heatMax - heat) / heatPerAccel / absAccelRate;

			if (tBlow <= tAcc) {
				// The user has requested acceleration excessive enough to destroy
				// the engines, which will occur unless we die from energy exhaustion
				// first.  This definitely caps the amount of acceleration we will
				// be able to produce.
				tAcc = tBlow;
			}
			else {
				// The engines will not be destroyed due to excessive acceleration.
				tBlow = 0.0;
			}

		}
	}
}
