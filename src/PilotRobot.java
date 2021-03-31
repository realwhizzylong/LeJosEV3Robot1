
// Based on the SimpleRobot class, this provides access to the
// sensors, and constructs a MovePilot to control the robot.

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;

public class PilotRobot {
	private EV3TouchSensor leftBump, rightBump;
	private EV3UltrasonicSensor usSensor;
	private EV3GyroSensor gSensor;
	private EV3LargeRegulatedMotor motorL, motorR;
	private EV3MediumRegulatedMotor motorS;
	private SampleProvider leftSP, rightSP, distSP, gyroSP;
	private float[] leftSample, rightSample, distSample, angleSample;
	private MovePilot pilot;
	private OdometryPoseProvider opp;

	public PilotRobot() {
		Brick myEV3 = BrickFinder.getDefault();

		leftBump = new EV3TouchSensor(myEV3.getPort("S1"));
		rightBump = new EV3TouchSensor(myEV3.getPort("S4"));
		usSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		gSensor = new EV3GyroSensor(myEV3.getPort("S2"));
		motorL = new EV3LargeRegulatedMotor(myEV3.getPort("B"));
		motorR = new EV3LargeRegulatedMotor(myEV3.getPort("D"));
		motorS = new EV3MediumRegulatedMotor(myEV3.getPort("C"));

		leftSP = leftBump.getTouchMode();
		rightSP = rightBump.getTouchMode();
		distSP = usSensor.getDistanceMode();
		gyroSP = gSensor.getAngleMode();

		leftSample = new float[leftSP.sampleSize()]; // Size is 1
		rightSample = new float[rightSP.sampleSize()]; // Size is 1
		distSample = new float[distSP.sampleSize()]; // Size is 1
		angleSample = new float[gyroSP.sampleSize()]; // Size is 1

		// Set up the wheels by specifying the diameter of the
		// left (and right) wheels in centimeters, i.e. 4.05 cm.
		// The offset number is the distance between the centre
		// of wheel to the centre of robot (4.9 cm)
		// NOTE: this may require some trial and error to get right!!!
		Wheel leftWheel = WheeledChassis.modelWheel(motorL, 4.15).offset(-5.3);
		Wheel rightWheel = WheeledChassis.modelWheel(motorR, 4.16).offset(5.3);

		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);

		pilot = new MovePilot(myChassis);
		opp = new OdometryPoseProvider(pilot);

		// Reset the value of the gyroscope to zero
		gSensor.reset();
	}

	public void closeRobot() {
		leftBump.close();
		rightBump.close();
		usSensor.close();
		gSensor.close();
	}

	public boolean isLeftBumpPressed() {
		leftSP.fetchSample(leftSample, 0);
		return (leftSample[0] == 1.0);
	}

	public boolean isRightBumpPressed() {
		rightSP.fetchSample(rightSample, 0);
		return (rightSample[0] == 1.0);
	}

	public float getDistance() {
		distSP.fetchSample(distSample, 0);
		return distSample[0];
	}

	public float getAngle() {
		gyroSP.fetchSample(angleSample, 0);
		return angleSample[0];
	}

	public MovePilot getPilot() {
		return pilot;
	}

	public float getX() {
		return opp.getPose().getX();
	}

	public float getY() {
		return opp.getPose().getY();
	}

	public void turnUS(int degree) {
		motorS.rotate(degree);
	}
	
	public float getDegree() {
		return opp.getPose().getHeading();
	}
}