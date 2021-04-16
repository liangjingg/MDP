package sensor;

/** Represents the class for the sensor during the actual run */
public class RealSensor extends Sensor {
	private int x;
	private int y;
	private int direction;
	private String arr[] = new String[6];

	public RealSensor() {
		super();
	}

	public RealSensor(int x, int y, int direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
	}

	@Override
	public String[] getAllSensorsValue(int x, int y, int direction) {
		if (this.x == x && this.y == y && this.direction == direction) {
			this.x = x;
			this.y = y;
			this.direction = y;
			return arr;
		}
		return null;
	}
}