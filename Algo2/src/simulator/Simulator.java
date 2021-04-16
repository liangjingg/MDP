package simulator;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import config.Constant;
import robot.SimulatorRobot;

public class Simulator {
	public final static String TITLE = "Simulator";
	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	// Create the window frame for simulation purpose
	public Simulator() {
		JFrame frame = new JFrame(TITLE);
		frame.setLayout(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(Constant.MARGINLEFT + Constant.GRIDWIDTH * Constant.BOARDWIDTH + 100,
				Constant.MARGINTOP + Constant.GRIDHEIGHT * Constant.BOARDHEIGHT + 100);
		new SimulatorRobot(frame, 0, 0, 2);

		frame.setVisible(true);

	}

}
