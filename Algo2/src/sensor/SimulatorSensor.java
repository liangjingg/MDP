package sensor;

import config.Constant;
import map.Map;

public class SimulatorSensor extends Sensor {
	// Simulate the Ardurino sensor
	private String sensorValue;
	private String[] sensorArray;

	// Randomly generate a true map for simulated environment
	public SimulatorSensor() {
		Map trueMap = new Map();
		trueMap.generateMap(Constant.RANDOMMAP);
		setTrueMap(trueMap);
	}

	// Set a map for the simulated environment
	public SimulatorSensor(Map map) {
		setTrueMap(map);
		// trueMap = map;
	}

	// Based on the true simulated map, the sensor will get the valid sensor value
	// for the robot
	private void updateSensorsValue(int x, int y, int direction) {
		// sensorValue will have this: FR, FC, FL, RB, RF, LF
		Map trueMap = getTrueMap();
		String[] sensorValue = new String[6];
		for (int i = 1; i <= Constant.SHORT_SENSOR_MAX_RANGE; i++) {
			if (sensorValue[0] == null
					&& trueMap.getGrid(x + sensorLocation[0][0] + i * sensorDirection[0][0],
							y + sensorLocation[0][1] + i * sensorDirection[0][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[0].length) {
				sensorValue[0] = setSensorValueToObstacle(0, i);
			}
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
			if (sensorValue[3] == null
					&& trueMap.getGrid(x + sensorLocation[3][0] + i * sensorDirection[1][0],
							y + sensorLocation[3][1] + i * sensorDirection[1][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[3].length) {
				sensorValue[3] = setSensorValueToObstacle(3, i);
			}
			if (sensorValue[4] == null
					&& trueMap.getGrid(x + sensorLocation[4][0] + i * sensorDirection[1][0],
							y + sensorLocation[4][1] + i * sensorDirection[1][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[4].length) {
				sensorValue[4] = setSensorValueToObstacle(4, i);
			}
		}
		for (int i = 1; i <= Constant.FAR_SENSOR_MAX_RANGE; i++) {
			if (sensorValue[5] == null
					&& trueMap.getGrid(x + sensorLocation[5][0] + i * sensorDirection[2][0],
							y + sensorLocation[5][1] + i * sensorDirection[2][1]).equals(Constant.OBSTACLE)
					&& i <= Constant.SENSOR_RANGES[5].length) {
				sensorValue[5] = setSensorValueToObstacle(5, i);
			}
		}

		sensorValue = padSensorValue(sensorValue);
		this.sensorValue = String.join(" ", sensorValue);
		for (String i : sensorValue) {
			System.out.print(i + " ");
		}
		System.out.println();
	}

	// Depends on the sensors, it will return the valid values based on the grid
	// offset from the sensor
	private String setSensorValueToObstacle(int s, int i) {
		return "" + (Constant.SENSOR_RANGES[s][i - 1] - 1);
	}

	public String[] getAllSensorsValue(int x, int y, int direction) {
		updateSensorsValue(x, y, direction);
		sensorArray = sensorValue.split(" ");
		return sensorArray;
	}

	private String[] padSensorValue(String[] arr) {
		// Pad whatever value that has no obstacle
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				arr[i] = "" + (Constant.FAR_SENSOR_MAX_RANGE * 10 + Constant.FAR_SENSOR_OFFSET + 1) + ".0";
			}
		}
		return arr;
	}
}