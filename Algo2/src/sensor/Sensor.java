package sensor;

import config.Constant;
import map.Map;

public abstract class Sensor {
	private Map trueMap;
	public static int[][] sensorLocation = new int[6][2];
	public static int[][] sensorDirection = new int[3][2];

	// Calculates the sensor direction and the position of sensors based on
	// offset from robot position
	public static void updateSensorDirection(int direction) {

		// SENSOR DIRECTION
		sensorDirection[0] = Constant.SENSORDIRECTION[direction];
		sensorDirection[1] = Constant.SENSORDIRECTION[(direction + 1) % Constant.SENSORDIRECTION.length];
		sensorDirection[2] = Constant.SENSORDIRECTION[((direction - 1) + Constant.SENSORDIRECTION.length)
				% Constant.SENSORDIRECTION.length];

		int[] lastDirection = Constant.SENSORDIRECTION[(direction + 2) % Constant.SENSORDIRECTION.length];
		// SENSOR LOCATION
		sensorLocation[0] = new int[] { sensorDirection[0][0] + sensorDirection[1][0],
				sensorDirection[0][1] + sensorDirection[1][1] };
		sensorLocation[1] = sensorDirection[0];
		sensorLocation[2] = new int[] { sensorDirection[0][0] + sensorDirection[2][0],
				sensorDirection[0][1] + sensorDirection[2][1] };
		sensorLocation[3] = new int[] { sensorDirection[1][0] + lastDirection[0],
				sensorDirection[1][1] + lastDirection[1] };
		sensorLocation[4] = new int[] { sensorLocation[0][0], sensorLocation[0][1] };
		sensorLocation[5] = sensorDirection[2];
	}

	public abstract String[] getAllSensorsValue(int x, int y, int direction);

	// public abstract Map getTrueMap(); // Only for simulator to display the true
	// map

	public Map getTrueMap() {
		// return null;
		return trueMap;
	}

	// This is not valid action for the robot
	public void setTrueMap(Map map) {
		this.trueMap = map;
	}

	// public abstract void setTrueMap(Map map); // Only for simulator to load map
}