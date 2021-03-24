package robot;

import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import config.Constant;
import map.*;
import imagecomponent.RobotImageComponent;
import imagecomponent.ImageComponent;
import sensor.SimulatorSensor;
import simulator.SetUpUI;
import timertask.MoveImageTask;
import datastruct.Coordinate;
import datastruct.Obstacle;

public class SimulatorRobot extends Robot {

	private ImageComponent robotImage;
	private JFrame frame;
	private SimulatorMap smap;
	private SetUpUI buttonListener;
	private Timer t = new Timer();
	private int delay = Constant.DELAY;

	public SimulatorRobot(JFrame frame, int x, int y, int direction) {
		super();
		System.out.println(" Simulate robot!");
		initialise(x, y, direction); // From Robot parent class (This sets the x, y and direction of the robot ->
										// From start (0, 19, 1))
		this.frame = frame;
		this.sensor = new SimulatorSensor(); // This uses its own map which is all explored

		/*
		 * THE ORDER OF ADDING THE ROBOT INTO THE FRAME MATTERS OTHERWISE IT WILL APPEAR
		 * UNDERNEATH THE GRID Hence, initialiseRobotImage must run before SimulatorMap
		 * create the UI grid
		 */

		// Place the robot image based on its coordinate in the grid
		initialiseRobotImage(this.x, this.y);

		// Create an unexplored map for the robot
		map = new Map();

		// Create the UI map for display with the robot map
		smap = SimulatorMap.getInstance(frame, map.copy());

		// Add the buttons onto the UI
		buttonListener = new SetUpUI(frame, this, map);

	}

	// Takes the centre of the 3x3 grid where the robot is located
	private void initialiseRobotImage(int x, int y) {
		robotImage = new RobotImageComponent(Constant.ROBOTIMAGEPATHS[this.getDirection()], Constant.ROBOTWIDTH,
				Constant.ROBOTHEIGHT);
		frame.add(robotImage);
		robotImage.setLocation(
				Constant.MARGINLEFT + (Constant.GRIDWIDTH * 3 - Constant.ROBOTWIDTH) / 2 + (x - 1) * Constant.GRIDWIDTH,
				Constant.MARGINTOP + (Constant.GRIDHEIGHT * 3 - Constant.ROBOTHEIGHT) / 2
						+ (y - 1) * Constant.GRIDHEIGHT);
	}

	// Resets the robot position on the UI
	public void resetRobotPositionOnUI() {
		this.x = setValidX(1);
		this.y = setValidY(1);
		toggleValid();
		// TODO: Set this as a constant value
		robotImage.setLocation(
				Constant.MARGINLEFT + (Constant.GRIDWIDTH * 3 - Constant.ROBOTWIDTH) / 2 + (x - 1) * Constant.GRIDWIDTH,
				Constant.MARGINTOP + (Constant.GRIDHEIGHT * 3 - Constant.ROBOTHEIGHT) / 2
						+ (y - 1) * Constant.GRIDHEIGHT);

	}

	// Get the sensor values from the simulated environment
	protected String[] getSensorValues() {
		// FR, FC, FL, RB, RF, LF
		String[] sensorValues = sensor.getAllSensorsValue(this.x, this.y, getDirection());
		return sensorValues;
	}

	// Set the robot direction and update the UI
	public void setDirection(int direction) {
		super.setDirection(direction);
		robotImage.setImage(Constant.ROBOTIMAGEPATHS[direction]);
	}

	public boolean isAcknowledged() {
		return true;
	}

	// Update the map and display on the screen
	public int[] updateMap() {
		int[] isObstacle = super.updateMap();
		smap.setMap(map);
		return isObstacle;
	}

	public void setWaypoint(int x, int y) {
		Coordinate oldWaypoint = new Coordinate(this.getWaypoint().x, this.getWaypoint().y);
		super.setWaypoint(x, y); // Sets the map that is attribute of the robot
		if (!oldWaypoint.equals(this.getWaypoint())) {
			smap.setMap(map);
			if (this.getWaypoint().x != -1 && this.getWaypoint().y != -1) {
				buttonListener.displayMessage("Removed waypoint.", 1);
			} else {
				buttonListener.displayMessage("Successfully set the waypoint: " + x + ", " + y, 1);
			}
		}
	}

	// Simulator purposes
	public String checkMap() {
		Map temp = sensor.getTrueMap();
		if (Map.compare(temp, map)) {
			return "The maps are the same!";
		} else {
			return "The maps are different!";
		}
	}

	// Just for displaying the true map in the simulator
	public String toggleMap() {
		Map temp = sensor.getTrueMap();
		if (Map.compare(temp, smap.getMap())) {
			smap.setMap(map.copy());
			return "robot";
		} else {
			smap.setMap(temp.copy());
			return "simulated";
		}
	}

	// Reset the robot
	public void restartRobot() {
		this.x = setValidX(0);
		this.y = setValidY(0);
		robotImage.setLocation(
				Constant.MARGINLEFT + (Constant.GRIDWIDTH * 3 - Constant.ROBOTWIDTH) / 2 + (x - 1) * Constant.GRIDWIDTH,
				Constant.MARGINTOP + (Constant.GRIDHEIGHT * 3 - Constant.ROBOTHEIGHT) / 2
						+ (y - 1) * Constant.GRIDHEIGHT);
		setDirection(Constant.SOUTH);
		this.sensor = new SimulatorSensor();
		this.map = new Map();
		smap.setMap(map);
	}

	// Move the robot to move forward on the screen as well
	@Override
	public void forward(int step) {
		String s;
		this.x = setValidX(this.x + Constant.SENSORDIRECTION[this.getDirection()][0]);
		this.y = setValidX(this.y + Constant.SENSORDIRECTION[this.getDirection()][1]);
		switch (this.getDirection()) {
		case Constant.NORTH:
			s = "Up";
			break;
		case Constant.EAST:
			s = "Right";
			break;
		case Constant.SOUTH:
			s = "Down";
			break;
		case Constant.WEST:
			s = "Left";
			break;
		default:
			s = "Error";
		}
		toggleValid();
		// System.out.println("Move simulator by " + step);
		for (int i = 0; i < step * Constant.GRIDWIDTH; i++) {
			// System.out.printf("MOVE IMAGE by %d, x: %d, y: %d, i: %d \n", step,
			// this.getPosition()[0], this.getPosition()[1], i);
			t.schedule(new MoveImageTask(robotImage, s, 1), delay * (i + 1));
		}
	}

	@Override
	public void rotate180() {
		setDirection((this.getDirection() + 2) % 4);
	}

	// Move the robot backward. Only used in real run in case of failed movement
	// from Ardurino
	public void backward(int step) {
		String s;
		this.x = setValidX(this.x + Constant.SENSORDIRECTION[(this.getDirection() + 2) % 4][0]); // Move back without
																									// turning
		this.y = setValidX(this.y + Constant.SENSORDIRECTION[(this.getDirection() + 2) % 4][1]);
		switch (this.getDirection()) {
		case Constant.NORTH:
			s = "Up";
			break;
		case Constant.EAST:
			s = "Right";
			break;
		case Constant.SOUTH:
			s = "Down";
			break;
		case Constant.WEST:
			s = "Left";
			break;
		default:
			s = "Error";
		}
		toggleValid();
		for (int i = 0; i < step * Constant.GRIDWIDTH; i++) {
			t.schedule(new MoveImageTask(robotImage, s, 1), delay * (i + 1));
		}
	}

	@Override
	public void rotateRight() {
		// In the actual robot, this will also send the command to rotate right
		setDirection((this.getDirection() + 1) % 4);
	}

	@Override
	public void rotateLeft() {
		// In the actual robot, this will also send the command to rotate left
		setDirection((this.getDirection() + 3) % 4);
	}

	// Set the map of the robot
	public void setMap(Map map) {
		this.map = map;
		smap.setMap(map);
	}

	public void setSimulatorMap(Map map) {
		smap.setMap(map);
	}

	// Restart the timer so it does not make the image move
	public void restartRobotUI() {
		t.cancel();
		t.purge();
		t = new Timer();
	}

	// Simulate the delay in capturing image
	public boolean captureImage(Obstacle[] image_pos) {
		System.out.println("Capturing Image");
		buttonListener.displayMessage("Capturing image at " + getPosition() + " now", 1);
		buttonListener.displayMessage("Sent message: C(" + image_pos[0].coordinates.y + "," + image_pos[0].coordinates.x
				+ ":" + image_pos[1].coordinates.y + "," + image_pos[1].coordinates.x + ":" + image_pos[2].coordinates.y
				+ "," + image_pos[2].coordinates.x + ")", 1);
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	// Simulate the delay in calibration
	public void calibrate() {
		buttonListener.displayMessage("Calibrating", 1);
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// Simulate the delay in right_align
	public void rightAlign() {
		buttonListener.displayMessage("Calibrating", 1);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void initialCalibrate() {
		buttonListener.displayMessage("Initial calibrating", 1);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void displayMessage(String s, int mode) {
		buttonListener.displayMessage(s, mode);
	}

}
