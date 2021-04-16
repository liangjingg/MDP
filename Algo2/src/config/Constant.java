package config;

import java.io.File;
import datastruct.Coordinate;

public class Constant {
	// Actual run constraints
	public static final int TIME = 320;
	public static final int PERCENTAGE = 100;
	public static final int SPEED = 1;
	public static final boolean IMAGE_REC = false;

	// Used for all Real and Simulator Events
	public static final int BOARDWIDTH = 20; // By default, this should be 20. This must be greater than 3 as the Robot
												// assumes to take 3x3 grid.
	public static final int BOARDHEIGHT = 15; // By default, this should be 15. This must be greater than 3 as the Robot
												// assumes to take 3x3 grid.
	public static final int STARTPOINTHEIGHT = 3;
	public static final int STARTPOINTWIDTH = 3;
	public static final int ENDPOINTHEIGHT = 3;
	public static final int ENDPOINTWIDTH = 3;

	// You need to define your own range for your sensors
	public static final double[][] SENSOR_RANGES = { { 7.0, 19.0, 27.0 }, { 7.0, 19.0, 27.0 }, { 7.0, 19.0, 27.0 },
			{ 7.0, 19.0, 27.0 }, { 7.0, 19.0, 27.0 }, { 8.0, 15.0, 27.0, 38.0, 50.0, 60.0 } };

	public static final double[] MAX_SENSOR_LIMIT = { 30.0, 30.0, 30.0, 30.0, 30.0, 50.0 };
	public static final double[] MAX_UNCERTAINTY = { 2.0, 2.0, 2.0, 2.0, 2.0, 2.0 };
	public static final double[] SENSOR_DIST_FROM_BLOCK = { 7.0, 7.0, 7.0, 7.0, 7.0, 7.0 };

	// Sensor constants used only in Simulator
	public static final int SHORT_SENSOR_MAX_RANGE = 3; // This is in number of grid.
	public static final int SHORTSENSOROFFSET = 3; // This is in cm.
	public static final int FAR_SENSOR_MAX_RANGE = 5; // This is in number of grid.
	public static final int FAR_SENSOR_OFFSET = 13; // This is in cm.

	public static final int[][] SENSORDIRECTION = new int[][] { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

	// For Instructing Robot
	public static final int LEFT = 0;
	public static final int FORWARD = 1;
	public static final int RIGHT = 2;
	public static final int BACKWARD = 3;

	// MAX COST
	public static final int MAXFCOST = 9999;

	// For Timertask in UI Simulator
	public static final int DELAY = 15;

	// For Generating Random Map for Simulator. However, it does not guarantee a
	// maneuverable map.
	public static final boolean RANDOMMAP = false;
	public static final int MAXOBSTACLECOUNT = 16;

	// For UI Simulator Display
	public static final int GRIDHEIGHT = 40;
	public static final int GRIDWIDTH = 40;
	public static final int MARGINLEFT = 100;
	public static final int MARGINTOP = 100;
	public static final String SIMULATORICONIMAGEPATH = File.separator + "images" + File.separator
			+ "simulator_icon.ico";

	// Used in Map and possibly used in real run and simulator
	public static final String[] POSSIBLEGRIDLABELS = new String[] { "Unexplored", "Explored", "Obstacle", "Waypoint",
			"Startpoint", "Endpoint" };
	public static final String UNEXPLORED = POSSIBLEGRIDLABELS[0];
	public static final String EXPLORED = POSSIBLEGRIDLABELS[1];
	public static final String OBSTACLE = POSSIBLEGRIDLABELS[2];
	public static final String WAYPOINT = POSSIBLEGRIDLABELS[3];
	public static final String STARTPOINT = POSSIBLEGRIDLABELS[4];
	public static final String ENDPOINT = POSSIBLEGRIDLABELS[5];

	// Map directions
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final String[] DIRECTIONS = new String[] { "North", "East", "South", "West" };
	public static final Coordinate START = new Coordinate(1, 1);
	public static final Coordinate END = new Coordinate(18, 13);

	// Avoid changing these values below
	public static final int ROBOTHEIGHT = 100; // By default, this should be twice of the grid height. GRIDHEIGHT * 2
	public static final int ROBOTWIDTH = 100; // By default, this should be twice of the grid width. GRIDWIDTH * 2
	public static final int HEIGHT = BOARDHEIGHT * GRIDHEIGHT + MARGINTOP;
	public static final int WIDTH = BOARDWIDTH * GRIDWIDTH + MARGINLEFT;

	// Connection Constants
	public static final String IP_ADDRESS = "192.168.11.11";
	// public static final String IP_ADDRESS = "192.168.15.15";
	public static final int PORT = 5020;
	public static final int BUFFER_SIZE = 512;

	public static final String START_EXPLORATION = "ES|";
	public static final String FASTEST_PATH = "FS|";
	public static final String IMAGE_STOP = "I";
	public static final String SEND_ARENA = "SendArena";
	public static final String INITIALISING = "starting";
	public static final String SETWAYPOINT = "waypoint";
	public static final String SENSE_ALL = "Z|";
	public static final String TURN_LEFT = "A|";
	public static final String TURN_RIGHT = "D|";
	public static final String U_TURN = "U|";
	public static final String FORWARD_MOVEMENT = "W|";
	public static final String CALIBRATE = "C|";
	public static final String RIGHTALIGN = "B|";
	public static final String END_TOUR = "N";
	public static final String FOLDER_TO_WRITE = "C:\\Users\\lisas\\Desktop\\NTU\\Year 3\\Semester 2\\CZ3004 Multidisciplinary Project\\FullRepo\\MDP\\Algo2";
	public static final String SETMDF = "MDF";
	public static final String INITIAL_CALIBRATE = "E|";
	// Image path for UI Simulator
	public static final String UNEXPLOREDIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "unexplored_grid.png";
	public static final String EXPLOREDIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "explored_grid.png";
	public static final String OBSTACLEIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "obstacle_grid.png";
	public static final String WAYPOINTIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "waypoint_grid.png";
	public static final String STARTPOINTIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "start_grid.png";
	public static final String ENDPOINTIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "end_grid.png";
	public static final String ROBOTIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "robot.png";
	public static final String ROBOTNIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "carN.gif";
	public static final String ROBOTEIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "carE.gif";
	public static final String ROBOTSIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "carS.gif";
	public static final String ROBOTWIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "carW.gif";
	public static final String DIALOGICONIMAGEPATH = FOLDER_TO_WRITE + File.separator + "images" + File.separator
			+ "letter-r.png";

	public static final String[] GRIDIMAGEPATH = new String[] { UNEXPLOREDIMAGEPATH, EXPLOREDIMAGEPATH,
			OBSTACLEIMAGEPATH, WAYPOINTIMAGEPATH, STARTPOINTIMAGEPATH, ENDPOINTIMAGEPATH };

	public static final String[] ROBOTIMAGEPATHS = new String[] { ROBOTNIMAGEPATH, ROBOTEIMAGEPATH, ROBOTSIMAGEPATH,
			ROBOTWIMAGEPATH };
	// MDF Strings
	public static final String P1String = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

	// Connection Acknowledge
	public static final String IMAGE_ACK = "D";
}
