package robot;

import java.awt.Image;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import config.Constant;
import connection.ConnectionManager;
import connection.ConnectionSocket;
import map.Map;
import sensor.RealSensor;
import datastruct.Obstacle;

public class RealRobot extends Robot {
	private ConnectionSocket connectionSocket = ConnectionSocket.getInstance();
	private static RealRobot r = null;
	private SimulatorRobot sr = null;
	private boolean acknowledgementCompleted = false;

	private RealRobot(boolean simulator) {
		super();
		this.map = new Map();
		initialise(1, 1, Constant.SOUTH);
		this.sensor = new RealSensor();
		if (simulator) {
			JFrame frame = new JFrame("RealRun");
			frame.setIconImage(new ImageIcon(Constant.SIMULATORICONIMAGEPATH).getImage().getScaledInstance(20, 20,
					Image.SCALE_DEFAULT));
			frame.setLayout(null);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setSize(Constant.MARGINLEFT + Constant.GRIDWIDTH * Constant.BOARDWIDTH + 100,
					Constant.MARGINTOP + Constant.GRIDHEIGHT * Constant.BOARDHEIGHT + 100);
			sr = new SimulatorRobot(frame, this.x, this.y, this.getDirection());
			frame.setVisible(true);
			// sr.displayMessage("Disabled all buttons for the real run.", 2);
		}
	}

	// Singleton and accept a boolean variable to decide if the simulator UI will be
	// displayed
	public static RealRobot getInstance(boolean simulator) {
		if (r == null) {
			r = new RealRobot(simulator);
		}
		return r;
	}

	public SimulatorRobot getSimulatorRobot() {
		return sr;
	}

	public boolean isAcknowledged() {
		return this.acknowledgementCompleted;
	}

	// Difference between this function and acknowledge is that it returns the
	// sensor value arr after sending sense message to Ardurino
	@Override
	protected String[] getSensorValues() {
		// FR, FM, FL, RB, RF, LF is the arrangement of our sensor values
		// Accept only all integer sensor values or all double sensor values
		Pattern sensorPattern = Pattern.compile("\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+");
		Pattern sensorPattern2 = Pattern.compile(
				"\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+");
		String s;
		String[] arr = null;
		connectionSocket.sendMessage(Constant.SENSE_ALL);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.SENSE_ALL, 1);
		}
		boolean completed = false;

		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			System.out.println("Send from arduino: " + s);
			// System.out.println(sensorPattern.matcher(s).matches() ||
			// sensorPattern2.matcher(s).matches());
			try {
				arr = s.split("\\|");
				for (int i = 0; i < arr.length; i++) {
					try {
						double d = Double.parseDouble(arr[i]);
					} catch (NumberFormatException e) {
						System.out.println(e.getMessage());
					}
				}
				System.out.println(Arrays.toString(arr));
				System.out.println(arr.length);
				if (arr.length == 6) {
					break;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			// if (sensorPattern.matcher(s).matches() ||
			// sensorPattern2.matcher(s).matches()) {
			// System.out.println("Sensor values: " + s);
			// arr = s.split("|");
			// break;
			// }
		}
		System.arraycopy(arr, 0, sensorValues, 0, 6);
		this.sensePosition[0] = x;
		this.sensePosition[1] = y;
		this.sensePosition[2] = getDirection();
		return arr;
	}

	// Receive sensor values to acknowledge the action had performed by the Ardurino
	private boolean acknowledge() {
		Pattern sensorPattern = Pattern.compile("\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+[|]{1}\\d+");
		Pattern sensorPattern2 = Pattern.compile(
				"\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+[.]\\d+[|]{1}\\d+");
		String s;
		String[] arr = null;

		boolean completed = false;
		this.acknowledgementCompleted = false;

		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			try {
				arr = s.split("\\|");
				for (int i = 0; i < arr.length; i++) {
					try {
						double d = Double.parseDouble(arr[i]);
					} catch (NumberFormatException e) {
						System.out.println(e.getMessage());
					}
				}
				if (arr.length == 7) {
					break;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			;
			if (ConnectionSocket.getDebug()) {
				System.out.println("In get sensor values");
				System.out.println(s);
			}
			if (sr != null) {
				sr.displayMessage("Received Message: " + s, 1);
			}
			// if (sensorPattern.matcher(s).matches() ||
			// sensorPattern2.matcher(s).matches()) {
			// arr = s.split("\\|");
			// break;
			// }
		}

		// 1 indicates the Ardurino has performed the action successfully and hence
		// return true
		if (Integer.parseInt(arr[6]) == 1) {
			System.arraycopy(arr, 0, sensorValues, 0, 6);
			this.sensorValues = arr;
			this.sensePosition[0] = x;
			this.sensePosition[1] = y;
			this.sensePosition[2] = getDirection();
			this.acknowledgementCompleted = true;
			sendMDFString();
			sendPosition();
			System.out.println("Sending MDF and position");
			return true;
		}

		// 0 indicates the Ardurino has failed to performed the action and hence return
		// true
		System.arraycopy(arr, 0, sensorValues, 0, 6);
		this.sensePosition[0] = x;
		this.sensePosition[1] = y;
		this.sensePosition[2] = getDirection();
		this.acknowledgementCompleted = true;
		sendMDFString();
		sendPosition();
		return false;

	}

	// Send the mdf string every 4 times we receive the sensor value
	public void sendMDFString() {
		String[] arr2 = this.getMDFString();
		connectionSocket.sendMessage("M{\"map\":[{\"explored\": \"" + arr2[0] + "\",\"length\":" + arr2[1]
				+ ",\"obstacle\":\"" + arr2[2] + "\"}]}|");
		if (sr != null) {
			sr.displayMessage("Sent message: M{\"map\":[{\"explored\": \"" + arr2[0] + "\",\"length\":" + arr2[1]
					+ ",\"obstacle\":\"" + arr2[2] + "\"}]}|", 1);
		}
	}

	// Send the forward message with how many steps and update the robot position
	@Override
	public void forward(int step) {
		connectionSocket.sendMessage(Constant.FORWARD_MOVEMENT);
		this.x = setValidX(this.x + Constant.SENSORDIRECTION[this.getDirection()][0]);
		this.y = setValidX(this.y + Constant.SENSORDIRECTION[this.getDirection()][1]);
		if (sr != null) {
			sr.forward(step);
			sr.displayMessage("Sent message: W0" + Integer.toString(step) + "|", 1);
		}
		toggleValid();
		if (!acknowledge()) {
			this.x = setValidX(this.x - Constant.SENSORDIRECTION[this.getDirection()][0]);
			this.y = setValidX(this.y - Constant.SENSORDIRECTION[this.getDirection()][1]);
			if (sr != null) {
				sr.backward(step);
			}
		}
	}

	// Send the rotate right message and update the robot direction
	@Override
	public void rotateRight() {
		connectionSocket.sendMessage(Constant.TURN_RIGHT);
		setDirection((this.getDirection() + 1) % 4);
		if (sr != null) {
			sr.rotateRight();
			sr.displayMessage("Sent message: " + Constant.TURN_RIGHT, 1);
		}
		acknowledge();
	}

	@Override
	public void rotate180() {
		connectionSocket.sendMessage(Constant.U_TURN);
		setDirection((this.getDirection() + 2) % 4);
		if (sr != null) {
			sr.rotate180();
			sr.displayMessage("Sent message: " + Constant.U_TURN, 1);
		}
		acknowledge();
	}

	// Send the rotate left message and update the robot direction
	@Override
	public void rotateLeft() {
		connectionSocket.sendMessage(Constant.TURN_LEFT);
		setDirection((this.getDirection() + 3) % 4);
		if (sr != null) {
			sr.rotateLeft();
			sr.displayMessage("Sent message: " + Constant.TURN_LEFT, 1);
		}
		acknowledge();
	}

	// Send the image position and the command to take picture
	public boolean captureImage(Obstacle[] image_pos) {
		try {
			TimeUnit.MILLISECONDS.sleep(1250);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		connectionSocket.sendMessage("C(" + image_pos[0].coordinates.y + "," + image_pos[0].coordinates.x + ":"
				+ image_pos[1].coordinates.y + "," + image_pos[1].coordinates.x + ":" + image_pos[2].coordinates.y + ","
				+ image_pos[2].coordinates.x + ")");
		// x and y is inverted in
		// Real Run.

		if (sr != null) {
			sr.captureImage(image_pos);
			sr.displayMessage("Sent message: C(" + image_pos[0].coordinates.y + "," + image_pos[0].coordinates.x + ":"
					+ image_pos[1].coordinates.y + "," + image_pos[1].coordinates.x + ":" + image_pos[2].coordinates.y
					+ "," + image_pos[2].coordinates.x + ")", 1);
		}
		boolean completed = false;
		String s;
		ArrayList<String> buffer = ConnectionManager.getBuffer();
		for (String b : buffer) {
			System.out.println("In buffer: " + b);
		}
		while (!completed) {
			s = connectionSocket.receiveMessage().trim();
			completed = checkImageAcknowledge(s);
			if (completed && s.equals(Constant.IMAGE_ACK)) {
				return false;
			} else if (completed && s.equals(Constant.IMAGE_STOP)) {
				return true;
			} else {
				for (int i = 0; i < buffer.size(); i++) {
					completed = checkImageAcknowledge(buffer.get(i));
					i += 1;
					if (completed) {
						buffer.remove(i);
						break;
					}
				}
			}
		}
		return false;
	}

	// Get the image acknowledgement from RPI
	private boolean checkImageAcknowledge(String s) {
		if (s.equals(Constant.IMAGE_ACK) || s.equals(Constant.IMAGE_STOP)) {
			return true;
		}
		return false;
	}

	public void sendPosition() {
		connectionSocket
				.sendMessage("R{\"robotPosition\":[" + y + "," + x + "," + ((getDirection() + 3) % 4) * 90 + "]}|");
		if (sr != null) {
			sr.displayMessage("Sent message: " + "R{\"robotPosition\":[" + y + "," + x + ","
					+ ((getDirection() + 3) % 4) * 90 + "]}", 1);
		}
	}

	// Send the calibrate command
	public void calibrate() {
		connectionSocket.sendMessage(Constant.CALIBRATE);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.CALIBRATE, 1);
		}
		acknowledge();
	}

	// Send the right align command
	public void rightAlign() {
		connectionSocket.sendMessage(Constant.RIGHTALIGN);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.RIGHTALIGN, 1);
		}
		// acknowledge();
	}

	public void initialCalibrate() {
		connectionSocket.sendMessage(Constant.INITIAL_CALIBRATE);
		if (sr != null) {
			sr.displayMessage("Sent message: " + Constant.INITIAL_CALIBRATE, 1);
		}
	}

	// This will override the update map from the robot class
	public int[] updateMap() {
		int[] isObstacle = super.updateMap();

		// If there is a simulator, this will display the robot movement on screen
		if (sr != null) {
			sr.setMap(this.getMap());
		}
		return isObstacle;
	}

	public void displayMessage(String s, int mode) {
		if (sr != null) {
			sr.displayMessage(s, mode);
		} else {
			System.out.println(s);
		}
	}

	public Map getTrueMap() {
		return this.sensor.getTrueMap();
	}
}
