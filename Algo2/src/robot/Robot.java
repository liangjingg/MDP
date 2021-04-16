package robot;

import config.Constant;
import connection.ConnectionSocket;
import map.Map;
import sensor.Sensor;
import datastruct.Coordinate;
import datastruct.Obstacle;

import java.io.*;

public abstract class Robot {

	// This assumes the Robot have 3 front sensors, 2 left sensor and 1 right far
	// sensor

	protected Sensor sensor;

	private int direction;

	protected int x, y;

	protected Map map;

	protected boolean validObstacleValue;

	protected int[] isObstacle = new int[6];

	protected String[] sensorValues = new String[6];

	protected int[] sensePosition = new int[] { -1, -1, -1 };

	private OutputStreamWriter writer;

	public Robot() {
	}

	// Initialise the robot direction, position and the file to debug if applicable
	public void initialise(int x, int y, int direction) {
		this.x = setValidX(x);
		this.y = setValidY(y);
		this.direction = direction;
		this.validObstacleValue = false; // TODO: Wtf is this
		if (ConnectionSocket.getDebug()) {
			try {
				this.writer = new OutputStreamWriter(
						new FileOutputStream(Constant.FOLDER_TO_WRITE + "" + File.separator + "" + "Output.txt"),
						"UTF-8");
				writer.write("");
			} catch (Exception e) {
				System.out.println("Unable to write into output");
			}
		}
	}

	protected abstract String[] getSensorValues();

	public abstract void forward(int step);

	public abstract void rotateRight();

	public abstract void rotateLeft();

	public abstract void rotate180();

	public abstract boolean captureImage(Obstacle[] image_pos);

	public abstract void calibrate();

	public abstract void rightAlign();

	public abstract void initialCalibrate();

	public abstract void displayMessage(String s, int mode);

	public abstract boolean isAcknowledged();

	public void setImage() {
		Map newMap = map;
		newMap.setGrid(x, y, Constant.WAYPOINT);
	}

	public void setDirection(int direction) {
		this.direction = direction;
		toggleValid();
	}

	// To ensure the robot's position is always within the logical position.
	protected int setValidX(int x) {
		if (x >= Constant.BOARDWIDTH - 1) {
			x = Constant.BOARDWIDTH - 2;
		} else if (x <= 0) {
			x = 1;
		}
		return x;
	}

	protected int setValidY(int y) {
		if (y >= Constant.BOARDHEIGHT - 1) {
			y = Constant.BOARDHEIGHT - 2;
		} else if (y <= 0) {
			y = 1;
		}
		return y;
	}

	public int getDirection() {
		return direction;
	}

	public Coordinate getPosition() {
		return new Coordinate(x, y);
	}

	// Update the map when called and returns the number of grids that the obstacle
	// resides in the direction of the sensors
	public int[] updateMap() {
		// System.out.printf("Senseposition: %d, %d \n", sensePosition[0],
		// sensePosition[1]);
		// Check if there is a need to check for the obstacle around the robot again, it
		// is invalid when the robot has moved or rotated
		if (validObstacleValue) {
			return isObstacle;
		}

		// Copy the current map
		Map newMap = map;

		// Update the Sensor Direction with our Robot's directions
		Sensor.updateSensorDirection(this.direction);

		// Check the position of the stored sensor values are the same as our current
		// robot position and it is a real run
		// System.out.printf(" sensePosition[0]: %d, sensePosition[1]: %d \n",
		// sensePosition[0], sensePosition[1]);
		// System.out.printf("x: %d, y: %d \n", x, y);
		// whats w this error checking...
		if (!(sensePosition[0] == x && sensePosition[1] == y && sensePosition[2] == direction)
				|| !ConnectionSocket.checkConnection()) {
			// System.out.println(" Here");
			this.sensorValues = getSensorValues(); // THIS VALUES IS BY CM (GRID * 10)
			// System.out.println("Got sensor values" + this.sensorValues[0]);
		}
		int[][] sensorLocation = Sensor.sensorLocation;
		int[][] sensorDirection = Sensor.sensorDirection;
		int sensorDirectionValueX, sensorDirectionValueY;

		// -1 assumes that there is no obstacle in the sensor direction
		int[] isObstacle = new int[] { -1, -1, -1, -1, -1, -1 };

		if (ConnectionSocket.getDebug()) {
			System.out.print("The SensorValues are: \n");
			for (int i = 0; i < 6; i++) {
				System.out.print(sensorValues[i]);
				if (i != sensorValues.length - 1) {
					System.out.print(" ");
				}
			}
			System.out.println("\n");
		}

		setGridDist(newMap); // Set the 3x3grid at the robot position to be irreplaceable and explored

		// For each of the sensor value, we will update the map accordingly.
		for (int i = 0; i < 6; i++) {
			// System.out.printf("Currently at sensor %d \n", i + 1);
			double value = 0;
			try {
				value = Double.parseDouble(sensorValues[i]);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			// Find the direction to update the map based on the direction of the sensor
			switch (i) {
			case 0:
			case 1:
			case 2:
				sensorDirectionValueX = sensorDirection[0][0];
				sensorDirectionValueY = sensorDirection[0][1];

				break;
			case 3:
			case 4:
				sensorDirectionValueX = sensorDirection[1][0];
				sensorDirectionValueY = sensorDirection[1][1];

				break;
			case 5:
				sensorDirectionValueX = sensorDirection[2][0];
				sensorDirectionValueY = sensorDirection[2][1];

				break;
			default: // TODO: When will this be called?
				if (i < sensorValues.length - 1) {
					sensorDirectionValueX = sensorDirection[1][0];
					sensorDirectionValueY = sensorDirection[1][1];

				} else {
					sensorDirectionValueX = sensorDirection[2][0];
					sensorDirectionValueY = sensorDirection[2][1];

				}

			}
			double[] sensorThreshold = Constant.SENSOR_RANGES[i];
			for (int h = 0; h < sensorThreshold.length; h++) { // For both threshold values
				int g = h + 1;

				// Update the sensorLocation offset from x position and the grid in the
				// direction of the sensor
				int x = this.x + sensorLocation[i][0] + sensorDirectionValueX * g; // This is to get 2 grids that
																					// can be detected by the sensor??
				int y = this.y + sensorLocation[i][1] + sensorDirectionValueY * g;
				double oldDist = newMap.getDist(x, y);
				if (value <= sensorThreshold[h]) {
					if (h == 0) {
						/*
						 * If it is the far sensor, it has a lower accuracy than the short range sensor.
						 * Only update if the obstacle is determined to be more accurate.
						 */
						x = this.x + sensorLocation[i][0] + sensorDirectionValueX;
						y = this.y + sensorLocation[i][1] + sensorDirectionValueY;
						if (i == 5) { // for far sensors
							if (moreAccurate(g + 2, oldDist)) { // check if new dist < old dist
								// System.out.printf("Sensor %d, Sensor offset: %d, x: %d, y:%d \n", i + 1, g,
								// x, y);
								newMap.setGrid(x, y, Constant.OBSTACLE);
								newMap.setDist(x, y, g + 2);
							}
						} else {
							if (moreAccurate(g, oldDist)) {
								// System.out.printf("Sensor offset: %d, x: %d, y:%d \n", g, x, y);
								System.out.printf("Set x: %d, y: %d to obstacle. \n", x, y);
								newMap.setGrid(x, y, Constant.OBSTACLE);
								newMap.setDist(x, y, g);
							}
						}
					} else {
						if (value - sensorThreshold[h - 1] < sensorThreshold[h] - value) {
							// System.out.printf("%f less than %f \n", value - sensorThreshold[h],
							// sensorThreshold[h] - value);
							System.out.printf("x: %d, y: %d \n", x, y);
							x = this.x + sensorLocation[i][0] + sensorDirectionValueX * h;
							y = this.y + sensorLocation[i][1] + sensorDirectionValueY * h;
							if (i == 5) { // for far sensors
								if (moreAccurate(h + 2, oldDist)) { // check if new dist < old dist
									// System.out.printf("Sensor %d, Sensor offset: %d, x: %d, y:%d \n", i + 1, g,
									// x, y);
									newMap.setGrid(x, y, Constant.OBSTACLE);
									newMap.setDist(x, y, h + 2);
								}
							} else {
								if (moreAccurate(h, oldDist)) {
									// System.out.printf("Sensor offset: %d, x: %d, y:%d \n", g, x, y);
									System.out.printf("Set x: %d, y: %d to obstacle. \n", x, y);
									newMap.setGrid(x, y, Constant.OBSTACLE);
									newMap.setDist(x, y, h);
								}
							}
						} else {
							if (i == 5) { // for far sensors
								if (moreAccurate(g + 2, oldDist)) { // check if new dist < old dist
									// System.out.printf("Sensor %d, Sensor offset: %d, x: %d, y:%d \n", i + 1, g,
									// x, y);
									newMap.setGrid(x, y, Constant.OBSTACLE);
									newMap.setDist(x, y, g + 2);
								}
							} else {
								if (moreAccurate(g, oldDist)) {
									// System.out.printf("Sensor offset: %d, x: %d, y:%d \n", g, x, y);
									System.out.printf("Set x: %d, y: %d to obstacle. \n", x, y);
									newMap.setGrid(x, y, Constant.OBSTACLE);
									newMap.setDist(x, y, g);
								}
							}
						}
					}
					isObstacle[i] = g;
					break; // Stop the moment there is an obstacle in the path
				}

				// No obstacle
				else {
					/* Similar to detecting an obstacle */
					if (i == 5) {
						if (moreAccurate(g + 1, oldDist)) {
							newMap.setGrid(x, y, Constant.EXPLORED);
							newMap.setDist(x, y, g + 1);
						}
					} else {
						if (moreAccurate(g, oldDist)) {
							newMap.setGrid(x, y, Constant.EXPLORED);
							newMap.setDist(x, y, g);
						}
					}
				}
			}
		}

		if (ConnectionSocket.getDebug()) {
			try {
				File file = new File(Constant.FOLDER_TO_WRITE + "" + File.separator + "" + "Output.txt");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st = "", tmp = "";
				while ((tmp = br.readLine()) != null) {
					st += tmp + "\n";
				}
				this.writer = new OutputStreamWriter(
						new FileOutputStream(Constant.FOLDER_TO_WRITE + "" + File.separator + "" + "Output.txt"),
						"UTF-8");
				writer.write(st + "\n\n");
				writer.write("Pos : [" + x + ", " + y + ", " + Constant.DIRECTIONS[direction] + "]\n");
				writer.write("The sensor values are: ");
				for (int i = 0; i < 6; i++) {
					writer.write(sensorValues[i] + " ");
				}
				writer.write("\n");
				writer.write(newMap.print() + "\r\n\n");
				// writer.write(newMap.printDist() + "\r\n\n");
				writer.close();
				br.close();
			} catch (Exception error) {
				System.out.println("Unable to write in output.txt");
			}
		}
		// newMap.print();
		System.arraycopy(isObstacle, 0, this.isObstacle, 0, isObstacle.length);
		validObstacleValue = true;
		return isObstacle;
	}

	// Set the grid at the robot position to be irreplaceable and explored
	private void setGridDist(Map map) {
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				map.setDist(i + x, j + y, -1);
				if (!((map.getGrid(i + x, j + y).equals(Constant.STARTPOINT))
						|| (map.getGrid(i + x, j + y).equals(Constant.ENDPOINT)))) {
					map.setGrid(i + x, j + y, Constant.EXPLORED);
				}
			}
		}
	}

	// Check if the new distance is lesser than the old distance and return true
	// else false
	private boolean moreAccurate(double newDist, double oldDist) {
		return newDist < oldDist;
	}

	private boolean lessUncertainty(double newUncertainty, double oldUncertainty) {
		return newUncertainty < oldUncertainty;
	}

	public Map getMap() {
		return map;
	}

	public void setWaypoint(int x, int y) {
		this.map.setWayPoint(x, y);
	}

	public Coordinate getWaypoint() {
		return map.getWayPoint();
	}

	public void setTrueMap(Map map) {
		this.sensor.setTrueMap(map);
	}

	public Map getTrueMap() {
		return this.sensor.getTrueMap();
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public String[] getMDFString() {
		return map.getMDFString();
	}

	// Make the current obstacle array invalid
	protected void toggleValid() {
		validObstacleValue = false;
	}

}
