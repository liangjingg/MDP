package sensor;

import config.Constant;
import map.Map;

public abstract class Sensor {
	// More like sensor interface class to arduino

	// Far sensor range = 80-150
	// Short sensor range = 10-50
	// FR, FC, FL, RB, RF, LF

	public static int[][] sensorLocation = new int[6][2]; // 6x2 Grid -->
	public static int[][] sensorDirection = new int[3][2]; // Assume NORTH is the direction, Represent front, left and
															// right sensor direction

	// Calculates the sensor direction and the position of all my sensors based on
	// offset from my robot position
	public static void updateSensorDirection(int direction) {
		// int direction = direction; // NORTH, EAST, SOUTH, WEST
		// new int [][]{{0, -1},{1, 0},{0, 1},{-1, 0}};
		// SENSOR DIRECTION
		sensorDirection[0] = Constant.SENSORDIRECTION[direction];
		sensorDirection[1] = Constant.SENSORDIRECTION[(direction + 1) % Constant.SENSORDIRECTION.length];
		sensorDirection[2] = Constant.SENSORDIRECTION[((direction - 1) + Constant.SENSORDIRECTION.length)
				% Constant.SENSORDIRECTION.length];

		int[] lastDirection = Constant.SENSORDIRECTION[(direction + 2) % Constant.SENSORDIRECTION.length];

		System.out.printf("Direction : %d, (%s) \n", direction, Constant.DIRECTIONS[direction]);

		// SENSOR LOCATION
		sensorLocation[0] = new int[] { sensorDirection[0][0] + sensorDirection[1][0],
				sensorDirection[0][1] + sensorDirection[1][1] };
		System.out.printf("Sensor 1 Location: %d, %d \n", sensorDirection[0][0] + sensorDirection[1][0],
				sensorDirection[0][1] + sensorDirection[1][1]);
		sensorLocation[1] = sensorDirection[0];
		sensorLocation[2] = new int[] { sensorDirection[0][0] + sensorDirection[2][0],
				sensorDirection[0][1] + sensorDirection[2][1] };
		sensorLocation[3] = new int[] { sensorDirection[1][0] + lastDirection[0],
				sensorDirection[1][1] + lastDirection[1] };
		sensorLocation[4] = new int[] { sensorLocation[0][0], sensorLocation[0][1] };
		sensorLocation[5] = new int[] { sensorLocation[2][0], sensorLocation[2][1] };
	}

	public static void print() {
		System.out.println("Sensor Location \n");
		for (int i = 0; i < sensorLocation.length; i++) {
			for (int j = 0; j < sensorLocation[i].length; j++) {
				System.out.print(sensorLocation[i][j] + ", ");
			}
			System.out.println("");
		}

		System.out.println("Sensor Direction \n");
		for (int i = 0; i < sensorDirection.length; i++) {
			for (int j = 0; j < sensorDirection[i].length; j++) {
				System.out.print(sensorDirection[i][j] + ",");
			}
			System.out.println("");
		}
	}

	public abstract String[] getAllSensorsValue(int x, int y, int direction);

	public abstract Map getTrueMap(); // Only for simulator to display the true map

	public abstract void setTrueMap(Map map); // Only for simulator to load map
}