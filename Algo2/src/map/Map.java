package map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import astarpathfinder.FastestPathThread;
import config.Constant;
import connection.ConnectionSocket;
import datastruct.Coordinate;
import robot.Robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;

public class Map {

	// possibleGridLabels - 0 = unexplored, 1 = explored, 2 = obstacle, 3 = way
	// point, 4 = start point and 5 = end point
	// Note that grid is by x, y coordinate and this is opposite of the Array
	// position in Java

	private String[][] grid = new String[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private double[][] dist = new double[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private double[][] uncertainty = new double[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private Coordinate waypoint = new Coordinate(-1, -1);
	private String[] MDPString = new String[3];
	private boolean changed = true; // WTF IS THIS

	// Only used for simulation
	public static Random r = new Random();

	// Initialise an unexplored map
	public Map() {
		resetMap();
	}

	// Initialise map with grid String 2D array
	public Map(String[][] grid) {
		initializeMap(grid);
	}

	public Map(int[][] grid) {
		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				if (grid[i][j] < Constant.POSSIBLEGRIDLABELS.length) {
					setGrid(i, j, Constant.POSSIBLEGRIDLABELS[grid[i][j]]);
				} else {
					setGrid(i, j, Constant.UNEXPLORED);
				}
			}
		}
	}

	public Map copy() {
		Map m = new Map(this.grid);
		m.setWayPoint(this.waypoint.x, this.waypoint.y);
		return m;
	}

	public String print(Robot robot) {
		String s = "";
		s += "The current map is: \n\n";
		// System.out.println("The current map is: \n");

		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (i == robot.getPosition().x && j == robot.getPosition().y) {
					String temp = String.format("%3s|", "R");
					s += temp;
					System.out.printf("%3s", temp);
					continue;
				}
				if (i != Constant.BOARDWIDTH - 1) {
					if (grid[i][j] == Constant.EXPLORED || grid[i][j] == Constant.OBSTACLE
							|| grid[i][j] == Constant.WAYPOINT || grid[i][j] == Constant.ENDPOINT) {
						// s+=grid[i][j] + " , ";
						String temp = " ";
						if (grid[i][j] == Constant.EXPLORED) {
							temp = String.format("%3s|", " ");
						} else if (grid[i][j] == Constant.OBSTACLE) {
							temp = String.format("%3s|", "X");
						} else if (grid[i][j] == Constant.WAYPOINT) {
							temp = String.format("%3s|", "W");
						} else if (grid[i][j] == Constant.ENDPOINT) {
							temp = String.format("%3s|", "E");
						}

						s += temp;

						// System.out.print(grid[i][j] + " , " );
						System.out.printf("%3s", temp);
					} else {
						// s+=grid[i][j] + ", ";
						// System.out.print(grid[i][j] + ", " );
						String temp = " ";
						if (grid[i][j] == Constant.UNEXPLORED) {
							temp = String.format("%3s|", "O");
						} else if (grid[i][j] == Constant.STARTPOINT) {
							temp = String.format("%3s|", "S");
						}
						s += temp;
						System.out.printf("%3s", temp);
					}
				} else {
					String temp = " ";
					if (grid[i][j] == Constant.EXPLORED) {
						temp = String.format("%3s|", " ");
					} else if (grid[i][j] == Constant.OBSTACLE) {
						temp = String.format("%3s|", "X");
					} else if (grid[i][j] == Constant.WAYPOINT) {
						temp = String.format("%3s|", "W");
					} else if (grid[i][j] == Constant.ENDPOINT) {
						temp = String.format("%3s|", "E");
					} else if (grid[i][j] == Constant.UNEXPLORED) {
						temp = String.format("%3s|", "O");
					} else if (grid[i][j] == Constant.STARTPOINT) {
						temp = String.format("%3s|", "S");
					}

					s += temp;
					System.out.printf("%3s", temp);
				}
			}
			s += "\n";
			System.out.println();
		}
		s += "\n";
		System.out.println("");
		return s;
	}

	public String printDist() {
		String s = "";
		s += "The current map is: \n\n";
		// System.out.println("The current map is: \n");

		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (i != Constant.BOARDWIDTH - 1) {
					if (grid[i][j] == Constant.EXPLORED || grid[i][j] == Constant.OBSTACLE
							|| grid[i][j] == Constant.WAYPOINT || grid[i][j] == Constant.ENDPOINT) {
						// s+=grid[i][j] + " , ";
						String temp = " ";
						if (grid[i][j] == Constant.EXPLORED) {
							temp = String.format("%3s|", dist[i][j]);
						} else if (grid[i][j] == Constant.OBSTACLE) {
							temp = String.format("%3s|", dist[i][j]);
						} else if (grid[i][j] == Constant.WAYPOINT) {
							temp = String.format("%3s|", dist[i][j]);
						} else if (grid[i][j] == Constant.ENDPOINT) {
							temp = String.format("%3s|", dist[i][j]);
						}

						s += temp;

						// System.out.print(grid[i][j] + " , " );
						System.out.printf("%3s", temp);
					} else {
						// s+=grid[i][j] + ", ";
						// System.out.print(grid[i][j] + ", " );
						String temp = " ";
						if (grid[i][j] == Constant.UNEXPLORED) {
							temp = String.format("%3s|", dist[i][j]);
						} else if (grid[i][j] == Constant.STARTPOINT) {
							temp = String.format("%3s|", dist[i][j]);
						}
						s += temp;
						System.out.printf("%3s", temp);
					}
				} else {
					String temp = " ";
					if (grid[i][j] == Constant.EXPLORED) {
						temp = String.format("%3s|", dist[i][j]);
					} else if (grid[i][j] == Constant.OBSTACLE) {
						temp = String.format("%3s|", dist[i][j]);
					} else if (grid[i][j] == Constant.WAYPOINT) {
						temp = String.format("%3s|", dist[i][j]);
					} else if (grid[i][j] == Constant.ENDPOINT) {
						temp = String.format("%3s|", dist[i][j]);
					} else if (grid[i][j] == Constant.UNEXPLORED) {
						temp = String.format("%3s|", dist[i][j]);
					} else if (grid[i][j] == Constant.STARTPOINT) {
						temp = String.format("%3s|", dist[i][j]);
					}

					s += temp;
					System.out.printf("%3s", temp);
				}
			}
			s += "\n";
			System.out.println();
		}
		s += "\n";
		System.out.println("");
		return s;
	}

	public void initializeMap(String[][] grid) {
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				setGrid(i, j, grid[i][j]);
			}
		}
	}

	// Creates an unexplored map
	public void resetMap() {

		/*
		 * Start point is always 3x3 grid at the bottom left corner and end point is
		 * always 3x3 grid diagonally opposite the start point (top right)
		 */

		// new String[]{"Unexplored", "Explored", "Obstacle",
		// "Waypoint", "Startpoint", "Endpoint"};
		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				// Set the start point grids
				// Set dist to 0 to ensure all values will NOT be overridden
				if (i < Constant.STARTPOINTWIDTH && j < Constant.STARTPOINTHEIGHT) {
					setGrid(i, j, Constant.STARTPOINT);
					setDist(i, j, 0);
				}
				// Set the end point grids
				// Set dist to 0 to ensure all values will NOT be overridden
				else if (i >= Constant.BOARDWIDTH - Constant.ENDPOINTWIDTH
						&& j >= Constant.BOARDHEIGHT - Constant.ENDPOINTHEIGHT) {
					setGrid(i, j, Constant.ENDPOINT);
					setDist(i, j, 0);
				}
				// Set the remaining grids unexplored
				// Set dist to 999999 to ensure all values will be overridden
				else {
					setGrid(i, j, Constant.UNEXPLORED);
					setDist(i, j, 999999);
				}
			}
		}
	}

	// Set the distance of which the grid label is set
	public void setDist(int x, int y, double value) {
		if ((x >= 0) && (x < Constant.BOARDWIDTH) && (y >= 0) && (y < Constant.BOARDHEIGHT)) {
			dist[x][y] = value;
		}
	}

	// Set the distance of which the grid label is set
	public void setUncertainty(int x, int y, double value) {
		if ((x >= 0) && (x < Constant.BOARDWIDTH) && (y >= 0) && (y < Constant.BOARDHEIGHT)) {
			uncertainty[x][y] = value;
		}
	}

	// Set grid label
	public void setGrid(int x, int y, String command) {
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return;
		}
		for (int i = 0; i < Constant.POSSIBLEGRIDLABELS.length; i++) {
			if (command.toUpperCase().equals(Constant.POSSIBLEGRIDLABELS[i].toUpperCase())) {
				changed = true;
				if (i == 3) {
					setWayPoint(x, y);
				} else {
					grid[x][y] = command;
				}

				return;
			}
		}
		System.out.println("grid label error when setting grid");

	}

	// Generate Random Map or Empty Map. Note that this does not guarantee a
	// maneuverable map
	public void generateMap(boolean rand) {
		int k = 0;

		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				// Set the start point grids
				if (i < Constant.STARTPOINTWIDTH && j < Constant.STARTPOINTHEIGHT) {
					setGrid(i, j, Constant.STARTPOINT);
				}
				// Set the end point grids
				else if (i >= Constant.BOARDWIDTH - Constant.ENDPOINTWIDTH
						&& j >= Constant.BOARDHEIGHT - Constant.ENDPOINTHEIGHT) {
					setGrid(i, j, Constant.ENDPOINT);
				}
				// Set the remaining grids explored
				else {
					setGrid(i, j, Constant.EXPLORED);
				}
			}
		}

		if (rand) {
			while (k <= Constant.MAXOBSTACLECOUNT) {
				int x = r.nextInt(Constant.BOARDWIDTH);
				int y = r.nextInt(Constant.BOARDHEIGHT);
				if (getGrid(x, y).equals(Constant.EXPLORED)) {
					setGrid(x, y, Constant.OBSTACLE);
					k++;
				}

			}
		}

	}

	public void setWayPoint(int x, int y) {
		boolean verbose = new Exception().getStackTrace()[1].getClassName().equals("robot.Robot");
		int waypointX = waypoint.x;
		int waypointY = waypoint.y;
		if (x >= Constant.BOARDWIDTH - 1 || x <= 0 || y >= Constant.BOARDHEIGHT - 1 || y <= 0
				|| (getGrid(x, y) != null && !getGrid(x, y).equals(Constant.UNEXPLORED))
						&& !getGrid(x, y).equals(Constant.EXPLORED)) {
			if (!(waypointX == -1 && waypointY == -1)) {
				this.waypoint.x = -1;
				this.waypoint.y = -1;
				if (verbose) {
					System.out.println("The current waypoint is set as: " + "-1" + "," + "-1");
				}
			}
			return;
		}
		this.waypoint.x = x;
		this.waypoint.y = y;
		if (verbose) {
			System.out.println("Successfully set the waypoint: " + x + "," + y);
		}
	}

	public Coordinate getWayPoint() {
		return waypoint;
	}

	protected String[][] getGridMap() {
		return grid;
	}

	public double getUncertainty(int x, int y) {
		// If the x, y is outside the board, it returns an obstacle.
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return 0;
		}
		return uncertainty[x][y];
	}

	public double getDist(int x, int y) {
		// If the x, y is outside the board, it returns an obstacle.
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return 1;
		}
		return dist[x][y];
	}

	public String getGrid(int x, int y) {

		// If the x, y is outside the board, it returns an obstacle.
		if (x < 0 || x >= Constant.BOARDWIDTH || y < 0 || y >= Constant.BOARDHEIGHT) {
			return Constant.OBSTACLE;
		}
		return grid[x][y];
	}

	// Only for simulator purposes
	public static boolean compare(Map a, Map b) {
		String[][] a_grid = a.getGridMap();
		String[][] b_grid = b.getGridMap();
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (a_grid[i][j].compareTo(b_grid[i][j]) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void decodeMDFString(String MDFHexStringP1, String MDFHexStringP2) throws Exception {
		String[][] grid = new String[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
		String MDFBitStringP2;
		try {
			MDFBitStringP2 = new BigInteger(MDFHexStringP2, 16).toString(2);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		int unPaddedLength = MDFBitStringP2.length();
		for (int i = 0; i < MDFHexStringP2.length() * 4 - unPaddedLength; i++) {
			MDFBitStringP2 = "0" + MDFBitStringP2;
		}
		MDFBitStringP2 = MDFBitStringP2.substring(0, MDFBitStringP2.length() - 4);
		for (int x = 0; x < Constant.BOARDWIDTH; x++) {
			for (int y = 0; y < Constant.BOARDHEIGHT; y++) {
				if (MDFBitStringP2.charAt(x * Constant.BOARDHEIGHT + y) == '1') {
					grid[x][y] = Constant.OBSTACLE;
				} else {
					grid[x][y] = Constant.EXPLORED;
				}
			}
		}
		try {
			File folder = new File(Constant.FOLDER_TO_WRITE + "" + File.separator + "sample arena");
			int numOfFiles = folder.list().length;
			System.out.println(numOfFiles);
			File file = new File(Constant.FOLDER_TO_WRITE + "" + File.separator + "sample arena" + File.separator + ""
					+ "samplearena" + (numOfFiles - 1) + ".txt");
			PrintWriter out = new PrintWriter(file);
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				for (int i = 0; i < Constant.BOARDWIDTH; i++) {
					if (i < Constant.STARTPOINTWIDTH && j < Constant.STARTPOINTHEIGHT) {
						out.print("S");
					} else if (i >= Constant.BOARDWIDTH - Constant.ENDPOINTWIDTH
							&& j >= Constant.BOARDHEIGHT - Constant.ENDPOINTHEIGHT) {
						out.print("E");
					} else if (grid[i][j].equals(Constant.EXPLORED)) {
						out.print("U");
					} else if (grid[i][j].equals(Constant.OBSTACLE)) {
						out.print("O");
					}
				}
				out.println();
			}
			out.close();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	// Check if the MDF string stored has changed and make the mdf string if it did
	// change
	public String[] getMDFString() {
		if (changed == false) {
			return this.MDPString;
		}

		changed = false;

		StringBuilder MDFBitStringPart1 = new StringBuilder();
		StringBuilder MDFBitStringPart2 = new StringBuilder();

		MDFBitStringPart1.append("11");
		String[] MDFHexString = new String[] { "", "", "" };

		for (int j = 0; j < Constant.BOARDWIDTH; j++) {
			for (int i = 0; i < Constant.BOARDHEIGHT; i++) {

				if (grid[j][i].equals(Constant.OBSTACLE)) { // Obstacle
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("1");

				} else if (grid[j][i].equals(Constant.UNEXPLORED)) { // Unexplored
					MDFBitStringPart1.append("0");
				} else { // Not obstacle or unexplored (start point/end point/explored)
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("0");
				}

			}
		}
		MDFBitStringPart1.append("11");

		for (int i = 0; i < MDFBitStringPart1.length(); i += 4) {
			// System.out.println(Integer.toString(Integer.parseInt(MDFBitStringPart1.substring(i,
			// i + 4), 2), 16));
			MDFHexString[0] += Integer.toString(Integer.parseInt(MDFBitStringPart1.substring(i, i + 4), 2), 16);
		}

		if ((MDFBitStringPart2.length() % 4) != 0) { // Only pad if the MDF Bit string is not a multiple of 4
			MDFBitStringPart2.insert(0, "0".repeat(4 - (MDFBitStringPart2.length() % 4)));
		}

		for (int i = 0; i < MDFBitStringPart2.length(); i += 4) {
			MDFHexString[2] += Integer.toString(Integer.parseInt(MDFBitStringPart2.substring(i, i + 4), 2), 16);
		}

		int length = 0;
		for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
			for (int i = 0; i < Constant.BOARDWIDTH; i++) {
				if (grid[i][j] != Constant.UNEXPLORED) {
					length++;
				}
			}
		}

		MDFHexString[1] = Integer.toString(length);

		this.MDPString = MDFHexString;
		return MDFHexString;

	}
}
