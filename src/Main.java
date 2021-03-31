
// A simple application that uses the Subsumption architecture to create a 
// bumper car, that drives forward, and changes direction given a collision.

import lejos.hardware.Button;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Main {

	public static void main(String[] args) {
		PilotRobot me = new PilotRobot();
		PilotMonitor myMonitor = new PilotMonitor(me, 400);
		RobotServer aServer = new RobotServer(me);
		// Set up the behaviours for the Arbitrator and construct it.
		Behavior tal = new TravelAndLearn(me);
		Behavior[] bArray = { tal };

		Arbitrator arby = new Arbitrator(bArray);

		// Note that in the Arbritrator constructor, a message is sent
		// to stdout. The following prints eight black lines to clear
		// the message from the screen
		for (int i = 0; i < 8; i++)
			System.out.println("");

		// Start the Pilot Monitor
		myMonitor.start(); 
		aServer.start();

		// Tell the user to start
		myMonitor.setMessage("Press a key to start");
		Button.waitForAnyPress();
		// Start the Arbitrator
		arby.go();
	}
}