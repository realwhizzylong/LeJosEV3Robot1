import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Battery;

public class RobotServer extends Thread {

	public static final int port = 1234;
	public PilotRobot robot;
	
	public RobotServer(PilotRobot r) {
		this.setDaemon(true);
		robot = r;
	}

	public void run() {
		while (true) {
			try {
				ServerSocket server = new ServerSocket(port);
				Socket client = server.accept();
				java.io.OutputStream out = client.getOutputStream();
				DataOutputStream dOut = new DataOutputStream(out);
				dOut.writeUTF("Battery: " + Battery.getVoltage());
				dOut.flush();
				server.close();
			} catch (IOException e) {
				;// We have no exception handling
			}
		}
	}
}
