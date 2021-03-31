
// Based on the RobotMonitor class, this displays the robot
// state on the LCD screen; however, it works with the PilotRobot
// class that exploits a MovePilot to control the Robot.

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import java.text.DecimalFormat;

public class PilotMonitor extends Thread {
	private int delay;
	public PilotRobot robot;
	private String msg;
	private static final int ROW_COUNT = 6, COL_COUNT = 7;

	public static int[][] mapGrid = new int[ROW_COUNT][COL_COUNT];

	{
		for (int i = 0; i < ROW_COUNT; i++) {
			for (int j = 0; j < COL_COUNT; j++) {
				if (i == 0 && j == 0) {
					mapGrid[i][j] = 0;// Start positon
				} else {
					mapGrid[i][j] = -1;// Represent undetected
				}
			}
		}
	}

	public static boolean isAllDetected() {
		for (int i = 0; i < ROW_COUNT; i++) {
			for (int j = 0; j < COL_COUNT; j++) {
				if (mapGrid[i][j] == -1) {
					return false;
				}
			}
		}
		return true;
	}

	GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();

	// Make the monitor a daemon and set
	// the robot it monitors and the delay
	public PilotMonitor(PilotRobot r, int d) {
		this.setDaemon(true);
		delay = d;
		robot = r;
		msg = "";
	}

	// Allow extra messages to be displayed
	public void resetMessage() {
		this.setMessage("");
	}

	// Clear the message that is displayed
	public void setMessage(String str) {
		msg = str;
	}

	// The monitor writes various bits of robot state to the screen, then
	// sleeps.
	public void run() {
		// The decimalformat here is used to round the number to three significant
		// digits
		DecimalFormat df = new DecimalFormat("####0.000");

		/*
		 * while (TextMode) { lcd.clear(); lcd.setFont(Font.getDefaultFont());
		 * lcd.drawString("Robot Monitor", lcd.getWidth() / 2, 0, GraphicsLCD.HCENTER);
		 * lcd.setFont(Font.getSmallFont());
		 *
		 * lcd.drawString("LBump: " + robot.isLeftBumpPressed(), 0, 20, 0);
		 * lcd.drawString("RBump: " + robot.isRightBumpPressed(), 0, 30, 0);
		 * lcd.drawString("Dist: " + robot.getDistance(), 0, 40, 0);
		 * lcd.drawString("Angle: " + robot.getAngle(), 0, 50, 0);
		 *
		 * // Note that the following exploit additional information available from the
		 * // MovePilot. This could be extended to include speed, angular velocity, pose
		 * // etc. lcd.drawString("Motion: " + robot.getPilot().isMoving(), 0, 60, 0);
		 * lcd.drawString("  type: " + robot.getPilot().getMovement().getMoveType(), 0,
		 * 70, 0); lcd.drawString(msg, 0, 100, 0);
		 *
		 * try { sleep(delay); } catch (Exception e) { // We have no exception handling
		 * ; } }
		 */
		
		//draw grids on robot screen
		while (true) {
			lcd.clear();
			lcd.setFont(Font.getDefaultFont());
			lcd.drawString("Map", lcd.getWidth() / 2, 0, GraphicsLCD.HCENTER);
			lcd.setFont(Font.getSmallFont());

			lcd.drawLine(42, 15, 132, 15);
			lcd.drawLine(42, 30, 132, 30);
			lcd.drawLine(42, 45, 132, 45);
			lcd.drawLine(42, 60, 132, 60);
			lcd.drawLine(42, 75, 132, 75);
			lcd.drawLine(42, 90, 132, 90);
			lcd.drawLine(42, 105, 132, 105);
			lcd.drawLine(42, 120, 132, 120);

			lcd.drawLine(42, 15, 42, 120);
			lcd.drawLine(57, 15, 57, 120);
			lcd.drawLine(72, 15, 72, 120);
			lcd.drawLine(87, 15, 87, 120);
			lcd.drawLine(102, 15, 102, 120);
			lcd.drawLine(117, 15, 117, 120);
			lcd.drawLine(132, 15, 132, 120);

			//display initial value of each cell
			for (int i = 0; i < ROW_COUNT; i++) {
				for (int j = 0; j < COL_COUNT; j++) {
					lcd.drawString(String.valueOf(mapGrid[i][j]), 46 + 15 * i, 112 - 15 * j, 0);
				}
			}

			try {
				sleep(delay);
			} catch (Exception e) {
				// We have no exception handling
				;
			}

		}
	}

	public static int getArrayValue(int x, int y) {
		return mapGrid[x][y];
	}

	public static void setArrayValue(int x, int y, int newValue) {
		mapGrid[x][y] = newValue;
	}
}