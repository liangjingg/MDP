package sensor;

import config.Constant;
import map.Map;

public class SimulatorSensor extends Sensor {
	// Simulate the Ardurino sensor
	private Map trueMap;
	private String sensorValue;
	private String[] sensorArray;

	// Randomly generate a true map for simulated environment
	public SimulatorSensor() {
		trueMap = new Map();
		trueMap.generateMap(Constant.RANDOMMAP);
	}

	// Set a map for the simulated environment
	public SimulatorSensor(Map map) {
		trueMap = map;
	}

	// Based on the true simulated map, the sensor will get the valid sensor value
	// for the robot
	private void updateSensorsValue(int x, int y, int direction) {
		// sensorValue will have this: FR, FC, FL, RB, RF, LF

		String[] sensorValue = new String[6];
		for (int i = 1; i <= Constant.SHORTSENSORMAXRANGE; i++) { // ShortSensorMaxRange=3
			// System.out.printf("Sensor 1 Old Location: %d %d, Sensor 1 New Location: %d %d
			// \n", x + sensorLocation[0][0],
			// y + sensorLocation[0][1], x + sensorLocation[0][0] + i *
			// sensorDirection[0][0],
			// y + sensorLocation[0][1] + i * sensorDirection[0][1]);
			// System.out.println(trueMap.getGrid(x + sensorLocation[0][0] + i *
			// sensorDirection[0][0],
			// y + sensorLocation[0][1] + i * sensorDirection[0][1]));
			if (sensorValue[0] == null
					&& trueMap.getGrid(x + sensorLocation[0][0] + i * sensorDirection[0][0],
							y + sensorLocation[0][1] + i * sensorDirection[0][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[0].length) {
				sensorValue[0] = setSensorValueToObstacle(0, i);
			} // getSensorValue returns (Constant.SENSOR_RANGES[0][i - 1] - 1);
				// System.out.println("Sensor Value 1 " + sensorValue[0]);
				// System.out.printf("Sensor 2: %d, %d \n", x + sensorLocation[1][0] + i *
				// sensorDirection[0][0],
				// y + sensorLocation[1][1] + i * sensorDirection[0][1]);
				// System.out.println(trueMap.getGrid(x + sensorLocation[1][0] + i *
				// sensorDirection[0][0],
				// y + sensorLocation[1][1] + i * sensorDirection[0][1]));
			if (sensorValue[1] == null
					&& trueMap.getGrid(x + sensorLocation[1][0] + i * sensorDirection[0][0],
							y + sensorLocation[1][1] + i * sensorDirection[0][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[1].length) {
				sensorValue[1] = setSensorValueToObstacle(1, i);
			}
			if (sensorValue[2] == null && trueMap
					.getGrid(x + sensorLocation[2][0] + i * sensorDirection[0][0],
							y + sensorLocation[2][1] + i * sensorDirection[0][1]) // If outside of grid -> Obstacle
					.equals(Constant.OBSTACLE) && i <= Constant.SENSOR_RANGES[2].length) {
				sensorValue[2] = setSensorValueToObstacle(2, i);
			}
			// System.out.println("Sensor Value 2 " + sensorValue[1]);
			// System.out.printf("Sensor 4: %d, %d \n", x + sensorLocation[3][0] + i *
			// sensorDirection[1][0],
			// y + sensorLocation[3][1] + i * sensorDirection[1][1]);
			if (sensorValue[3] == null
					&& trueMap.getGrid(x + sensorLocation[3][0] + i * sensorDirection[1][0],
							y + sensorLocation[3][1] + i * sensorDirection[1][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[3].length) {
				sensorValue[3] = setSensorValueToObstacle(3, i);
			}
			// System.out.println("Sensor Value 4 " + sensorValue[3]);
			// System.out.printf("Sensor 5: %d, %d \n", x + sensorLocation[4][0] + i *
			// sensorDirection[1][0],
			// y + sensorLocation[4][1] + i * sensorDirection[1][1]);
			if (sensorValue[4] == null
					&& trueMap.getGrid(x + sensorLocation[4][0] + i * sensorDirection[1][0],
							y + sensorLocation[4][1] + i * sensorDirection[1][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[4].length) {
				sensorValue[4] = setSensorValueToObstacle(4, i);
			}
			// System.out.println("Sensor Value 5 " + sensorValue[4]);
		}
		// System.out.println("Sensor Value 6 " + sensorValue[5]);
		for (int i = 1; i <= Constant.FARSENSORMAXRANGE; i++) {
			// System.out.printf("Sensor 6: %d, %d \n", x + sensorLocation[5][0] + i *
			// sensorDirection[2][0],
			// y + sensorLocation[5][1] + i * sensorDirection[2][1]);
			if (sensorValue[5] == null
					&& trueMap.getGrid(x + sensorLocation[5][0] + i * sensorDirection[2][0],
							y + sensorLocation[5][1] + i * sensorDirection[2][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[5].length) {
				sensorValue[5] = setSensorValueToObstacle(5, i);
				// System.out.println("Sensor Value 6 " + sensorValue[4]);
			}
			// System.out.printf("Sensor 3: %d, %d \n", x + sensorLocation[2][0] + i *
			// sensorDirection[0][0],
			// y + sensorLocation[2][1] + i * sensorDirection[0][1]);
			// System.out.println("Sensor Value 3 " + sensorValue[2]);
		}

		sensorValue = padSensorValue(sensorValue); // Initially: null, null, null, 11.85, 11.85, null
		this.sensorValue = String.join(" ", sensorValue);
		for (String i : sensorValue) {
			System.out.print(i + " "); // Becomes 84.0 84.0 84.0 11.85 12.65 84.0
		}
		System.out.println();
	}

	// Depends on the sensors, it will return the valid values based on the grid
	// offset from the sensor
	private String setSensorValueToObstacle(int s, int i) {
		// System.out.printf("s: %d, i: %d, Range: %f \n", s, i,
		// Constant.SENSOR_RANGES[s][i - 1]);
		return "" + (Constant.SENSOR_RANGES[s][i - 1] - 1); // WHy -1? TODO
	}

	// public static final double[][] SENSOR_RANGES = {{13.54, 21.46},
	// {10.0, 19.35},
	// {12, 21.555},
	// {12.85, 22.7},
	// {13.65, 25.25},
	// {19.4, 24.8, 32.15, 43, 51.75, 62.0}};

	public String[] getAllSensorsValue(int x, int y, int direction) {
		// This is to simulate getting values from Arduino
		updateSensorsValue(x, y, direction);
		sensorArray = sensorValue.split(" ");
		return sensorArray;
	}

	private String[] padSensorValue(String[] arr) {
		// Pad whatever value that has no obstacle
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				arr[i] = "" + (Constant.FARSENSORMAXRANGE * 10 + Constant.FARSENSOROFFSET + 1) + ".0";
			}
		}
		return arr;
	}

	public Map getTrueMap() {
		return trueMap;
	}

	public void setTrueMap(Map map) {
		trueMap = map;
	}

}