package map;

import javax.swing.JFrame;
import javax.swing.JLabel;

import config.Constant;
import datastruct.Coordinate;
import imagecomponent.*;

import java.util.HashMap;

public class SimulatorMap {
	private ImageComponent[][] gridCells = new ImageComponent[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
	private JLabel[] axis = new JLabel[Constant.BOARDWIDTH + Constant.BOARDHEIGHT];
	private JFrame frame;
	private Map map;
	private static SimulatorMap simulatorMap = null;
	private HashMap<String, String> gridToImagePath = new HashMap<String, String>();

	// Singleton
	private SimulatorMap(JFrame frame, Map map) {
		this.frame = frame;
		this.map = map;

		// Create the hashmap for the grid label mapped to the grid image path
		for (int i = 0; i < Constant.POSSIBLEGRIDLABELS.length; i++) {
			gridToImagePath.put(Constant.POSSIBLEGRIDLABELS[i], Constant.GRID_IMAGE_PATHS[i]);
		}
		initializeMapOnUI(frame);

		// Create the number for the axis
		for (int i = 0; i < axis.length; i++) {
			if (i < Constant.BOARDWIDTH) {
				axis[i] = new JLabel("" + i);
				axis[i].setBounds(Constant.MARGINLEFT + (int) (Constant.GRIDWIDTH * 3 / 8) + i * Constant.GRIDWIDTH,
						Constant.MARGINTOP - 30, 20, 20);
				axis[i].setLocation(Constant.MARGINLEFT + (int) (Constant.GRIDWIDTH * 3 / 8) + i * Constant.GRIDWIDTH,
						Constant.MARGINTOP - 30);
			} else {
				axis[i] = new JLabel("" + (i - Constant.BOARDWIDTH));
				axis[i].setBounds(Constant.MARGINLEFT - 25,
						Constant.MARGINTOP + (i - Constant.BOARDWIDTH) * Constant.GRIDHEIGHT, 20, 20);
				axis[i].setLocation(Constant.MARGINLEFT - 25,
						Constant.MARGINTOP + (i - Constant.BOARDWIDTH) * Constant.GRIDHEIGHT);
			}
			frame.add(axis[i]);
			axis[i].setVisible(true);
		}

	}

	public static SimulatorMap getInstance(JFrame frame, Map map) {
		if (simulatorMap == null) {
			simulatorMap = new SimulatorMap(frame, map);
		}
		return simulatorMap;
	}

	// Create the map onto UI with the GridImageComponents
	public void initializeMapOnUI(JFrame frame) {
		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				String gridValue = map.getGrid(i, j); // explored/obstacle wtv
				createUIGrid(i, j, gridToImagePath.get(gridValue));
			}
		}
	}

	// Create the Grid Image Component for UI Display
	private void createUIGrid(int i, int j, String path) {
		gridCells[i][j] = new GridImageComponent(path, Constant.GRIDWIDTH, Constant.GRIDHEIGHT);
		gridCells[i][j].setLocation(Constant.MARGINLEFT + i * Constant.GRIDWIDTH,
				Constant.MARGINTOP + j * Constant.GRIDHEIGHT);
		frame.add(gridCells[i][j]);
	}

	// Update the Image of the Grid Image Component with the old and new map
	private void updateMapOnUI(Map oldMap, Map newMap) {
		String[][] oldGridValue = oldMap.getGridMap();
		String[][] newGridValue = newMap.getGridMap();
		Coordinate waypoint = newMap.getWayPoint();
		Coordinate oldWaypoint = oldMap.getWayPoint();
		if (waypoint.x != -1 && waypoint.y != -1) {
			gridCells[waypoint.x][waypoint.y].setImage(gridToImagePath.get(Constant.WAYPOINT));
		}
		for (int i = 0; i < Constant.BOARDWIDTH; i++) {
			for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
				if ((oldGridValue[i][j].compareTo(newGridValue[i][j]) != 0)
						&& gridToImagePath.containsKey(newGridValue[i][j])) {
					gridCells[i][j].setImage(gridToImagePath.get(newGridValue[i][j]));
				}
			}
		}
	}

	public Map getMap() {
		return map;
	}

	// Allow the caller to set and update the UI with the map object
	public void setMap(Map map) {
		updateMapOnUI(this.map, map);
		this.map = map.copy();
	}

}
