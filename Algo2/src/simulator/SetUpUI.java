package simulator;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import astarpathfinder.AStarPathFinder;
import config.Constant;
import connection.ConnectionSocket;
import exploration.ExplorationThread;
import robot.SimulatorRobot;
import map.Map;
import timertask.EnableButtonTask;
import astarpathfinder.FastestPathThread;

public class SetUpUI implements ActionListener {
	private JFrame frame;
	private int x = 1000;
	private int y = 200;
	private SimulatorRobot r;
	private ArrayList<JComponent> Buttons = new ArrayList<JComponent>();
	private HashMap<String, JLabel> Labels = new HashMap<String, JLabel>();
	private HashMap<String, String> filePath = new HashMap<String, String>();
	private Timer t = new Timer();
	private int step = 1;
	private Map loadedMap;
	private int[] chosenWaypoint = { -1, -1 };
	private int chosenTimeLimit = -1;
	private int chosenPercentage = 100;
	private String[] MDFString = { null, null };
	private int chosenSpeed = 1;
	private boolean isImageRecognition = false;

	private String inputP1String;
	private String inputP2String;
	private JComboBox<String> arenaMap;
	private JTextArea message = null;
	private JScrollPane scrollPane = null;

	// This constructor initialises all the buttons
	public SetUpUI(JFrame frame, SimulatorRobot r, Map map) {
		this.frame = frame;
		this.r = r;
		this.loadedMap = map;
		String[] arr = getArenaMapFileNames();
		String[] waypointXPos = createPossibleInput(1, Constant.BOARDWIDTH - 1);
		String[] waypointYPos = createPossibleInput(1, Constant.BOARDHEIGHT - 1);
		String[] timeList = createPossibleInput(0, 121);
		String[] percentageList = createPossibleInput(0, 101);
		String[] speedList = createPossibleInput(1, 6);
		String[] iamgeRecList = new String[] { "No Image Recognition", "With Image Recognition" };

		// Create the UI Component

		// Fastest Path things
		JLabel fastestPathLabel = new JLabel("Fastest Path");
		JButton fastestPath = new JButton();
		JComboBox<String> waypointX = new JComboBox<String>(waypointXPos);
		JComboBox<String> waypointY = new JComboBox<String>(waypointYPos);
		JLabel invalidWaypoint = new JLabel("Invalid Waypoint!");
		JLabel setWaypointLabel = new JLabel("Set Waypoint: ");

		// Exploration things
		JLabel explorationLabel = new JLabel("Exploration");
		JButton exploration = new JButton();
		JLabel timeLabel = new JLabel("Set exploration time limit (secs): ");
		JLabel percentageLabel = new JLabel("Set exploration coverage limit (%): ");
		JLabel speedLabel = new JLabel("Set speed (secs/step): ");
		JComboBox<String> time = new JComboBox<>(timeList);
		JComboBox<String> percentage = new JComboBox<>(percentageList);
		JComboBox<String> speed = new JComboBox<>(speedList);
		JComboBox<String> imageRec = new JComboBox<>(iamgeRecList);

		// Map things
		JLabel loadMapLabel = new JLabel("Select map:");
		JLabel p1StringLabel = new JLabel("P1: ");
		JLabel p2StringLabel = new JLabel("P2: ");
		JTextField p1String = new JTextField();
		JTextField p2String = new JTextField();
		JButton decodeMDF = new JButton();
		JButton printMDF = new JButton();
		JButton genRandMap = new JButton();
		this.arenaMap = new JComboBox<String>(arr);

		JButton update = new JButton();
		JButton checkMap = new JButton();
		JButton toggleMap = new JButton();

		// Robot controls
		JLabel mcLabel = new JLabel("Manual Control:");
		JButton right = new JButton();
		JButton left = new JButton();
		JButton up = new JButton();

		JButton resetRobot = new JButton();
		JLabel robotView = new JLabel("Robot's View");
		JLabel simulatedMap = new JLabel("Simulated Map");

		JButton returnToStart = new JButton();
		JLabel status = new JLabel("Status");
		message = new JTextArea("\n".repeat(7) + "Initialising the User Interface...");
		scrollPane = new JScrollPane(message);

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Set Icon or Image to the UI Component
		right.setIcon(new ImageIcon(new ImageIcon(".\\images\\right.png").getImage()
				.getScaledInstance(Constant.GRIDWIDTH, Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));
		left.setIcon(new ImageIcon(new ImageIcon(".\\images\\left.png").getImage().getScaledInstance(Constant.GRIDWIDTH,
				Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));
		up.setIcon(new ImageIcon(new ImageIcon(".\\images\\up.png").getImage().getScaledInstance(Constant.GRIDWIDTH,
				Constant.GRIDHEIGHT, Image.SCALE_DEFAULT)));

		// Set text of buttons
		update.setText("Update");
		checkMap.setText("Check Map");
		// toggleMap.setText("Toggle Map");
		resetRobot.setText("Restart");
		exploration.setText("Exploration");
		fastestPath.setText("Fastest Path");
		printMDF.setText("MDF String");
		decodeMDF.setText("Decode MDF");
		genRandMap.setText("Generate Map");
		returnToStart.setText("Return To Start");
		toggleMap.setText("Toggle Map");

		// For the Button to do something, you need to add the button to this Action
		// Listener and set the command for the ActionListener to receive

		// Exploration
		exploration.addActionListener(this);
		exploration.setActionCommand("Exploration");
		speed.addActionListener(this);
		speed.setActionCommand("Set speed");
		speed.setSelectedIndex(0);
		imageRec.addActionListener(this);
		imageRec.setActionCommand("Set image recognition");
		imageRec.setSelectedIndex(0);
		time.addActionListener(this);
		time.setActionCommand("Set time limit");
		time.setSelectedIndex(-1); // This line will print null in console
		percentage.addActionListener(this);
		percentage.setActionCommand("Set % limit");
		percentage.setSelectedIndex(101);

		// Fastest path
		fastestPath.addActionListener(this);
		fastestPath.setActionCommand("Fastest Path");
		waypointX.addActionListener(this);
		waypointX.setActionCommand("Set x coordinate");
		waypointX.setSelectedIndex(-1); // This line will print null in console
		waypointY.addActionListener(this);
		waypointY.setActionCommand("Set y coordinate");
		waypointY.setSelectedIndex(-1); // This line will print null in console

		// Map
		p1String.addActionListener(this);
		p1String.setActionCommand("Set P1 string");
		p2String.addActionListener(this);
		p2String.setActionCommand("Set P2 string");
		decodeMDF.addActionListener(this);
		decodeMDF.setActionCommand("Decode MDF");
		printMDF.addActionListener(this);
		printMDF.setActionCommand("MDF String");
		arenaMap.addActionListener(this);
		arenaMap.setActionCommand("Load Map");
		arenaMap.setSelectedIndex(-1); // This line will print null in console
		genRandMap.addActionListener(this);
		genRandMap.setActionCommand("Generate Map");

		// Manual
		right.addActionListener(this);
		right.setActionCommand("Right");
		left.addActionListener(this);
		left.setActionCommand("Left");
		up.addActionListener(this);
		up.setActionCommand("Up");
		update.addActionListener(this);
		update.setActionCommand("Update");
		toggleMap.addActionListener(this);
		toggleMap.setActionCommand("Toggle Map");
		resetRobot.addActionListener(this);
		resetRobot.setActionCommand("Restart");
		returnToStart.addActionListener(this);
		returnToStart.setActionCommand("Return");

		checkMap.addActionListener(this);
		checkMap.setActionCommand("Check Map");

		// Set the size (x, y, width, height) of the UI label

		// Exploration things
		explorationLabel.setBounds(x, y - 100, 110, 50);
		exploration.setBounds(x + 200, y - 100, 110, 50);
		time.setBounds(x + 200, y - 50, 50, 30);
		timeLabel.setBounds(x, y - 50, 300, 30);
		percentage.setBounds(x + 200, y - 20, 50, 30);
		percentageLabel.setBounds(x, y - 20, 300, 30);
		speed.setBounds(x + 150, y + 10, 50, 30);
		speedLabel.setBounds(x, y + 10, 200, 30);
		imageRec.setBounds(x + 275, y + 10, 175, 30);

		explorationLabel.setLocation(x, y - 100);
		exploration.setLocation(x + 200, y - 100);
		time.setLocation(x + 200, y - 50);
		timeLabel.setLocation(x, y - 50);
		percentage.setLocation(x + 200, y - 20);
		percentageLabel.setLocation(x, y - 20);
		speed.setLocation(x + 150, y + 10);
		speedLabel.setLocation(x, y + 10);
		imageRec.setLocation(x + 275, y + 10);

		// Fastest Path things
		fastestPathLabel.setBounds(x, y + 50, 110, 50);
		fastestPath.setBounds(x + 200, y + 50, 110, 50);
		waypointX.setBounds(x + 85, y + 100, 50, 30);
		waypointY.setBounds(x + 150, y + 100, 50, 30);
		setWaypointLabel.setBounds(x, y + 100, 300, 30);
		invalidWaypoint.setBounds(x + 150, y + 100, 300, 30);

		fastestPathLabel.setLocation(x, y + 50);
		fastestPath.setLocation(x + 200, y + 50);
		waypointX.setLocation(x + 85, y + 100);
		waypointY.setLocation(x + 150, y + 100);
		setWaypointLabel.setLocation(x, y + 100);
		invalidWaypoint.setLocation(x + 150, y + 100);

		// Map things
		p1StringLabel.setBounds(x, y + 130, 50, 30);
		p1String.setBounds(x + 50, y + 130, 100, 30);
		p2StringLabel.setBounds(x + 175, y + 130, 50, 30);
		p2String.setBounds(x + 225, y + 130, 100, 30);
		loadMapLabel.setBounds(x, y + 160, 300, 50);
		arenaMap.setBounds(x + 200, y + 160, 120, 30);
		printMDF.setBounds(x, y + 200, 150, 50);
		decodeMDF.setBounds(x + 175, y + 200, 150, 50);
		genRandMap.setBounds(x + 350, y + 200, 150, 50);
		toggleMap.setBounds(x, y + 250, 150, 50);

		p1StringLabel.setLocation(x, y + 130);
		p1String.setLocation(x + 50, y + 130);
		p2StringLabel.setLocation(x + 175, y + 130);
		p2String.setLocation(x + 225, y + 130);
		loadMapLabel.setLocation(x, y + 160);
		arenaMap.setLocation(x + 200, y + 160);
		printMDF.setLocation(x, y + 200);
		decodeMDF.setLocation(x + 175, y + 200);
		genRandMap.setLocation(x + 350, y + 200);
		toggleMap.setLocation(x, y + 250);

		// Robot controls

		left.setBounds(x + 100, y - 100, 50, 50);
		right.setBounds(x + 200, y - 100, 50, 50);
		up.setBounds(x + 150, y - 100, 50, 50);
		update.setBounds(x + 275, y - 100, 100, 50);
		checkMap.setBounds(x, y - 25, 100, 50);

		// toggleMap.setBounds(x + 150, y - 25, 110, 50);
		resetRobot.setBounds(x + 175, y + 250, 150, 50);
		robotView.setBounds(x - 600, y - 185, 200, 50);
		simulatedMap.setBounds(x + 100, y - 100, 300, 50);
		returnToStart.setBounds(x + 300, y + 50, 140, 50);

		status.setBounds(x, y + 360, 50, 30);
		scrollPane.setBounds(x, y + 300, 500, 163);
		mcLabel.setBounds(x, y - 100, 100, 50);

		left.setLocation(x + 100, y - 100);
		right.setLocation(x + 200, y - 100);
		up.setLocation(x + 150, y - 100);
		update.setLocation(x + 275, y - 100);
		checkMap.setLocation(x, y - 25);
		resetRobot.setLocation(x + 175, y + 250);
		robotView.setLocation(x - 600, y - 185);
		simulatedMap.setLocation(x - 600, y - 185);

		mcLabel.setLocation(x, y - 100);
		returnToStart.setLocation(x + 300, y + 50);
		status.setLocation(x, y + 360);
		scrollPane.setLocation(x, y + 300);

		// Set fonts for the labels
		explorationLabel.setFont(new Font(robotView.getFont().getName(), Font.BOLD, 20));
		fastestPathLabel.setFont(new Font(robotView.getFont().getName(), Font.BOLD, 18));
		// mcLabel.setFont(new Font(mcLabel.getFont().getName(), Font.ITALIC, 13));
		robotView.setFont(new Font(robotView.getFont().getName(), Font.BOLD, 30));
		simulatedMap.setFont(new Font(simulatedMap.getFont().getName(), Font.BOLD, 30));
		status.setFont(new Font(simulatedMap.getFont().getName(), Font.BOLD, 15));
		message.setFont(new Font(simulatedMap.getFont().getName(), Font.ITALIC, 15));

		// Set background colour of components
		message.setBackground(Color.WHITE);

		// Set edittable of components
		message.setEditable(false);

		// Set max row of message
		message.setLineWrap(true);
		message.setWrapStyleWord(true);

		// Add the UI component to the frame

		// exploration
		frame.add(explorationLabel);
		frame.add(exploration);
		frame.add(time);
		frame.add(timeLabel);
		frame.add(percentage);
		frame.add(percentageLabel);
		frame.add(speed);
		frame.add(speedLabel);
		frame.add(imageRec);

		// Fastest path
		frame.add(fastestPath);
		frame.add(fastestPathLabel);
		frame.add(waypointX);
		frame.add(waypointY);
		frame.add(setWaypointLabel);
		frame.add(invalidWaypoint);

		// Map things
		frame.add(p1StringLabel);
		frame.add(p1String);
		frame.add(p2StringLabel);
		frame.add(p2String);
		frame.add(loadMapLabel);
		frame.add(printMDF);
		frame.add(arenaMap);
		frame.add(decodeMDF);
		frame.add(genRandMap);

		// frame.add(mcLabel);
		// frame.add(returnToStart);
		// frame.add(right);
		// frame.add(left);
		// frame.add(up);
		// frame.add(update);
		// frame.add(checkMap);
		frame.add(toggleMap);
		frame.add(resetRobot);
		frame.add(robotView);
		frame.add(simulatedMap);

		// frame.add(status);
		frame.add(scrollPane);

		// Set Visibility of UI Component

		// exploration
		explorationLabel.setVisible(true);
		exploration.setVisible(true);
		time.setVisible(true);
		timeLabel.setVisible(true);
		percentage.setVisible(true);
		percentageLabel.setVisible(true);
		speed.setVisible(true);
		speedLabel.setVisible(true);
		imageRec.setVisible(true);

		// fastest path
		fastestPathLabel.setVisible(true);
		fastestPath.setVisible(true);
		waypointX.setVisible(true);
		waypointY.setVisible(true);
		setWaypointLabel.setVisible(true);
		invalidWaypoint.setVisible(false);

		// map
		p1StringLabel.setVisible(true);
		p1String.setVisible(true);
		p2StringLabel.setVisible(true);
		p2String.setVisible(true);
		loadMapLabel.setVisible(true);
		arenaMap.setVisible(true);
		printMDF.setVisible(true);
		decodeMDF.setVisible(true);
		genRandMap.setVisible(true);

		// manual
		// mcLabel.setVisible(true);
		// right.setVisible(true);
		// left.setVisible(true);
		// up.setVisible(true);
		// update.setVisible(true);
		// checkMap.setVisible(true);
		toggleMap.setVisible(true);
		resetRobot.setVisible(true);
		robotView.setVisible(true);
		simulatedMap.setVisible(false);

		returnToStart.setVisible(true);

		status.setVisible(true);
		message.setVisible(true);
		scrollPane.setVisible(true);

		// Add button to the list of buttons

		// exploration
		Buttons.add(exploration);
		Buttons.add(time);
		Buttons.add(percentage);
		Buttons.add(speed);
		Buttons.add(imageRec);

		// fastest path
		Buttons.add(fastestPath);
		Buttons.add(waypointX);
		Buttons.add(waypointY);

		// map
		Buttons.add(printMDF);
		Buttons.add(decodeMDF);
		Buttons.add(arenaMap);
		Buttons.add(genRandMap);

		// robot
		Buttons.add(right);
		Buttons.add(left);
		Buttons.add(up);
		Buttons.add(update);
		Buttons.add(checkMap);
		Buttons.add(toggleMap);
		Buttons.add(resetRobot);
		Buttons.add(returnToStart);

		// Add label to the hashmap
		// Labels.put("mcLabel", mcLabel);
		Labels.put("robotView", robotView);
		Labels.put("simulatedMap", simulatedMap);
		Labels.put("exploration_label", explorationLabel);
		Labels.put("time_label", timeLabel);
		Labels.put("percentage_label", percentageLabel);
		Labels.put("speed_label", speedLabel);
		Labels.put("fastest_path_label", fastestPathLabel);
		Labels.put("set_waypoint_label", setWaypointLabel);
		Labels.put("invalid_waypoint", invalidWaypoint);
		Labels.put("p1_string_label", p1StringLabel);
		Labels.put("p2_string_label", p2StringLabel);
		Labels.put("loadMapLabel", loadMapLabel);
		// Labels.put("image_cap", image_cap);
		// Labels.put("calibrating", calibrating);

		// Disable all button during the real runs as they are not meant to work for the
		// Real Run
		// if (ConnectionSocket.checkConnection()) {
		// this.disableButtons();
		// }
	}

	public void printConsole(String s, JScrollBar vbar) {
		message.append("\n" + s);
		try {
			String messageText = message.getDocument().getText(0, message.getDocument().getLength());
			if (messageText.charAt(0) == '\n') {
				message.setText(
						message.getDocument().getText(0, message.getDocument().getLength()).replaceFirst("\n", ""));
			}
			String[] arr = messageText.split("\n");
			if (arr.length > 100) {
				message.setText(message.getDocument().getText(0, message.getDocument().getLength())
						.replaceFirst(arr[0] + "\n", ""));
			}
		} catch (Exception e) {
			System.out.println("Error in displayMessage");
		}
		vbar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				Adjustable adjustable = e.getAdjustable();
				adjustable.setValue(adjustable.getMaximum());
				// This is so that the user can scroll down afterwards
				vbar.removeAdjustmentListener(this);
			}
		});
	}

	public void displayMessage(String s, int mode) {
		// Mode 0 only print the message, Mode 1 display onto the console, Mode 2 does
		// both
		JScrollBar vbar = scrollPane.getVerticalScrollBar();
		switch (mode) {
		case 0:
			System.out.println(s);
			break;
		case 1:
			printConsole(s, vbar);
			break;
		case 2:
			System.out.println(s);
			printConsole(s, vbar);
			break;
		}
	}

	private String[] createPossibleInput(int min, int max) {
		String[] arr = new String[max - min + 1];
		int count = 1;
		for (int i = min; i < max; i++) {
			arr[count] = Integer.toString(i);
			count++;
		}
		return arr;
	}

	// Reads all the txt file from the directory sample arena. Returns the array of
	// the file name in the directory.
	public String[] getArenaMapFileNames() {
		File folder = new File(Constant.FOLDER_TO_WRITE + "\\sample arena");
		filePath = new HashMap<String, String>();
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".txt")) {
				filePath.put(file.getName().substring(0, file.getName().lastIndexOf(".txt")), file.getAbsolutePath());
			}
		}
		String[] fileName = new String[filePath.size()];
		int i = filePath.size() - 1;

		for (String key : filePath.keySet()) {
			// System.out.println(key);
			fileName[i] = key;
			i--;
		}
		Arrays.sort(fileName);
		return fileName;
	}

	// Convert the sample arena text file into map and return the map
	public Map getGridfromFile(String path, String fileName, String[][] grid)
			throws Exception, FileNotFoundException, IOException {
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		int heightCount = 0;

		while (line != null) {
			line = line.strip().toUpperCase();

			// Check for invalid map
			if (line.length() != Constant.BOARDWIDTH) {
				displayMessage("The format of the " + fileName + " does not match the board format.", 1);
				throw new Exception("The format of the " + fileName + " does not match the board format.");
			}
			for (int i = 0; i < line.length(); i++) {
				switch (line.charAt(i)) {
				case 'S': // Represent start grid
					grid[i][heightCount] = Constant.STARTPOINT;
					break;

				case 'U': // Represent empty grid as end grid already used 'E'
					// Here, we set to explored instead of Unexplored
					grid[i][heightCount] = Constant.EXPLORED;
					break;

				// case 'W':
				/*
				 * We do not allow waypoint setting as there may be ambiguous situation
				 * 
				 * For example, if user accidentally set wrongly, what should be the grid string
				 * after it resets as one map should only have one waypoint?
				 * 
				 * This might affect the correctness of our mdf string.
				 */
				// grid[i][heightCount] = Constant.POSSIBLEGRIDLABELS[3];
				// break;

				case 'E': // Represent end grid
					grid[i][heightCount] = Constant.ENDPOINT;
					break;

				case 'O': // Represent obstacle grid
					grid[i][heightCount] = Constant.OBSTACLE;
					break;

				default:
					displayMessage("There is unrecognised character symbol in " + fileName + ".\n" + fileName
							+ " failed to load into the program.", 1);
					throw new Exception("There is unrecognised character symbol in " + fileName + ".\n" + fileName
							+ " failed to load into the program.");
				}
			}

			heightCount++;
			line = br.readLine();
		}
		if (heightCount != Constant.BOARDHEIGHT) {
			throw new Exception("The format of the " + fileName + " does not match the board format.");
		}
		br.close();
		loadedMap = new Map(grid);
		displayMessage(fileName + " has loaded successfully.", 2);
		return loadedMap;
	}

	private void disableButtons() {
		for (int i = 0; i < Buttons.size(); i++) {
			Buttons.get(i).setEnabled(false);
		}
	}

	public void enableButtons() {
		for (int i = 0; i < Buttons.size(); i++) {
			Buttons.get(i).setEnabled(true);
		}
	}

	public void disableLabel(String label) {
		Labels.get(label).setVisible(false);
	}

	public void enableLabel(String label) {
		Labels.get(label).setVisible(true);
	}

	// Define all the action of all the button
	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		// System.out.println(" Action: " + action);
		if (action.equals("Right")) {
			displayMessage("Rotate right button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			r.rotateRight();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Left")) {
			displayMessage("Rotate left button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			r.rotateLeft();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Up")) {
			displayMessage("Forward button clicked", 2);
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
				r.toggleMap();
			}
			r.forward(1);
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Update")) {
			disableButtons();
			if (!Labels.get("robotView").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
			}
			int[] isObstacle = r.updateMap();
			for (int i = 0; i < isObstacle.length; i++) {
				System.out.print(isObstacle[i] + " ");
			}
			System.out.println();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Check Map")) {
			disableButtons();
			JOptionPane.showMessageDialog(null, r.checkMap(), "Result of checking map",
					JOptionPane.INFORMATION_MESSAGE);
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Toggle Map")) {
			disableButtons();
			if (r.toggleMap().compareTo("robot") == 0) {
				if (r.checkMap().equals("The maps are the same!") && Labels.get("robotView").isVisible()) {
					disableLabel("robotView");
					enableLabel("simulatedMap");
				} else {
					enableLabel("robotView");
					disableLabel("simulatedMap");
				}
			} else {
				disableLabel("robotView");
				enableLabel("simulatedMap");
			}
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
		}

		else if (action.equals("Restart")) {
			disableButtons();
			enableLabel("robotView");
			disableLabel("simulatedMap");
			ExplorationThread.stopThread();
			FastestPathThread.stopThread();
			r.restartRobotUI();
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (Exception z) {

			}
			r.restartRobot();
			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));
			displayMessage("Restarted the Robot", 2);
		}

		else if (action.equals("Exploration")) {
			if (!ExplorationThread.getRunning()) {
				displayMessage("Exploration Started", 1);
				ExplorationThread.getInstance(r, chosenTimeLimit, chosenPercentage, chosenSpeed, isImageRecognition);
				disableButtons();
				t.schedule(new EnableButtonTask(this), 1);
			}
		}

		else if (action.equals("Set time limit")) {
			JComboBox<String> secs = (JComboBox<String>) e.getSource();
			String secs_chosen = (String) secs.getSelectedItem();
			if (secs_chosen == null) {
				return;
			} else {
				chosenTimeLimit = Integer.parseInt(secs_chosen);
			}
		}

		else if (action.equals("Set % limit")) {
			JComboBox<String> percentage = (JComboBox<String>) e.getSource();
			String perc_chosen = (String) percentage.getSelectedItem();
			if (perc_chosen == null) {
				return;
			} else {
				chosenPercentage = Integer.parseInt(perc_chosen);
			}
		}

		else if (action.equals("Set speed")) {
			JComboBox<String> s = (JComboBox<String>) e.getSource();
			String sp = (String) s.getSelectedItem();
			if (sp != null) {
				chosenSpeed = Integer.parseInt(sp);
			}
		}

		else if (action.equals("Set image recognition")) {
			JComboBox<String> ir = (JComboBox<String>) e.getSource();
			String irc = (String) ir.getSelectedItem();
			if (irc != null) {
				isImageRecognition = irc.equals("With Image Recognition");
				// System.out.println("Image Rec: " + isImageRecognition);
			}
		}

		else if (action.equals("Fastest Path")) {
			// System.out.printf("Waypoint x :%d, y: %d \n", chosenWaypoint[0],
			// chosenWaypoint[1]);
			// r.setWaypoint(chosenWaypoint[0], chosenWaypoint[1]);
			// // if (Arrays.equals(chosenWaypoint, new int[] { -1, -1 })) {
			// // r.setWaypoint(chosenWaypoint[0], chosenWaypoint[1]);
			// // }
			if (loadedMap == null) {
				displayMessage("Map not selected!", 2);
				return;
			}
			try {
				r.setTrueMap(loadedMap);
				r.setMap(loadedMap);
				if (Labels.get("simulatedMap").isVisible()) {
					System.out.println("Toggles map!");
					enableLabel("robotView");
					disableLabel("simulatedMap");
					r.toggleMap();
					r.toggleMap();
				}

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			// Need to be after the map is set because it got cleared
			int[] waypoint = r.getWaypoint();
			// System.out.printf("Old Waypoint x :%d, y: %d \n", waypoint[0], waypoint[1]);
			r.setWaypoint(chosenWaypoint[0], chosenWaypoint[1]);
			// if (loadedMap.getGrid(chosenWaypoint[0],
			// chosenWaypoint[1]).equals(Constant.OBSTACLE)) {
			// displayMessage("Invalid waypoint - Waypoint is an obstacle!", 2);
			// return;
			// }

			if (!FastestPathThread.getRunning()) {
				displayMessage("Fastest Path Started", 1);
				FastestPathThread.getInstance(r, waypoint, chosenSpeed);
				disableButtons();
				t.schedule(new EnableButtonTask(this), 1);
			}
			// if (!FastestPathThread.getRunning() && ExplorationThread.getCompleted()) {
			// displayMessage("Fastest Path Started", 1);
			// FastestPathThread.getInstance(r, waypoint, chosenSpeed);
			// disableButtons();
			// t.schedule(new EnableButtonTask(this), 1);
			// } else if (!ExplorationThread.getCompleted()) {
			// displayMessage("You need to run exploration first.", 2);
			// }
		}

		else if (action.equals("Set x coordinate")) {
			// new AStarPathFinder();
			JComboBox<String> waypoint_x = (JComboBox<String>) e.getSource();
			String selected_waypoint_x = (String) waypoint_x.getSelectedItem();
			if (selected_waypoint_x == null) {
				chosenWaypoint[0] = -1;
			} else {
				chosenWaypoint[0] = Integer.parseInt(selected_waypoint_x);
			}
			// int[] old_waypoint = r.getWaypoint();
			// r.setWaypoint(chosenWaypoint[0], chosenWaypoint[1]);
			// if (!Arrays.equals(old_waypoint, r.getWaypoint())) {
			// enableLabel("robotView");
			// disableLabel("simulatedMap");
			// }

		}

		else if (action.equals("Set y coordinate")) {
			// new AStarPathFinder();
			JComboBox<String> waypoint_y = (JComboBox<String>) e.getSource();
			String selected_waypoint_y = (String) waypoint_y.getSelectedItem();
			if (selected_waypoint_y == null) {
				chosenWaypoint[1] = -1;
			} else {
				chosenWaypoint[1] = Integer.parseInt(selected_waypoint_y);
			}
			// int[] old_waypoint = r.getWaypoint();
			// r.setWaypoint(chosenWaypoint[0], chosenWaypoint[1]);
			// if (!Arrays.equals(old_waypoint, r.getWaypoint())) {
			// enableLabel("robotView");
			// disableLabel("simulatedMap");
			// }
		}

		else if (action.equals("Return")) {
			r.resetRobotPositionOnUI();
		}

		else if (action.equals("Set P1 string")) {
			JTextField input = (JTextField) e.getSource();
			inputP1String = input.getText();
			displayMessage(inputP1String, 0);
		}

		else if (action.equals("Set P2 string")) {
			JTextField input = (JTextField) e.getSource();
			inputP2String = input.getText();
			displayMessage(inputP2String, 0);
		}

		else if (action.equals("MDF String")) {
			MDFString = loadedMap.getMDFString();
			displayMessage("The MDF String is: ", 2);
			displayMessage("Part 1: " + MDFString[0], 2);
			displayMessage("Part 2: " + MDFString[2], 2);
		}

		else if (action.equals("Decode MDF")) {
			if (inputP1String == null || inputP2String == null) {
				displayMessage("Values have not been set yet.", 2);
				return;
			}
			String p1String = inputP1String;
			String p2String = inputP2String;
			if (p1String.length() != 76 || p2String.length() != 76) {
				displayMessage("Invalid format!", 2);
				return;
			}
			try {
				loadedMap.decodeMDFString(p1String, p2String);
				String[] newMaps = getArenaMapFileNames();
				this.arenaMap.removeAllItems();
				for (String newMap : newMaps) {
					this.arenaMap.addItem(newMap);
				}
			} catch (Exception err) {
				displayMessage(err.getMessage(), 1);
			}
			// displayMessage("The MDF String is: ", 2);
			// displayMessage("Part 1: " + MDFString[0], 2);
			// displayMessage("Part 2: " + MDFString[2], 2);
		}

		else if (action.equals("Load Map")) {
			JComboBox<String> arenaMap = (JComboBox<String>) e.getSource();
			String selectedFile = (String) arenaMap.getSelectedItem();
			if (selectedFile == null || selectedFile.equals("Choose a map to load")) {
				return;
			}
			String[][] grid = new String[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
			disableButtons();
			try {
				Map map = getGridfromFile(filePath.get(selectedFile), selectedFile, grid);
				this.loadedMap = map;
				r.setTrueMap(map);
				if (Labels.get("simulatedMap").isVisible()) {
					enableLabel("robotView");
					disableLabel("simulatedMap");
					r.toggleMap();
					r.toggleMap();
				}
			} catch (FileNotFoundException f) {
				System.out.println("File not found");
			} catch (IOException IO) {
				System.out.println("IOException when reading" + selectedFile);
			} catch (Exception eX) {
				System.out.println(eX.getMessage());
			}

			t.schedule(new EnableButtonTask(this), Constant.DELAY * (step * Constant.GRIDWIDTH + 1));

		} else if (action.equals("Generate Map")) {
			Map map = new Map();
			map.generateMap(true);
			this.loadedMap = map;
			r.setTrueMap(map);
			if (Labels.get("simulatedMap").isVisible()) {
				enableLabel("robotView");
				disableLabel("simulatedMap");
				r.toggleMap();
				r.toggleMap();
			}
			// disableButtons();
			// try {
			// Map map = getGridfromFile(filePath.get(selectedFile), selectedFile, grid);
			// this.loadedMap = map;
			// r.setTrueMap(map);
			// if (Labels.get("simulatedMap").isVisible()) {
			// enableLabel("robotView");
			// disableLabel("simulatedMap");
			// r.toggleMap();
			// r.toggleMap();
			// }
			// } catch (FileNotFoundException f) {
			// System.out.println("File not found");
			// } catch (IOException IO) {
			// System.out.println("IOException when reading" + selectedFile);
			// } catch (Exception eX) {
			// System.out.println(eX.getMessage());
			// }

			// t.schedule(new EnableButtonTask(this), Constant.DELAY * (step *
			// Constant.GRIDWIDTH + 1));

		}

	}
}
