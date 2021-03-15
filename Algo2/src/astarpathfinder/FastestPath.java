package astarpathfinder;

import config.Constant;
import connection.ConnectionSocket;
import exploration.Exploration;
import robot.Robot;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FastestPath {
    public int[] FastestPath(Robot robot, int[] waypoint, int[] goal, int speed, boolean on_grid, boolean move) {
        // on_grid is true in exploration but not image recognition exploration and not
        // phase 3 of exploration (back to the start)and false in fastest path
        // could represent the sensors i guess
        AStarPathFinder astar = new AStarPathFinder();
        astar.setDirection(robot.getDirection());
        astar.setFirst(true); // wtf -> this is set in the old code when the open list is empty? -> means no
                              // possible path
        int[] path, path1, path2;

        if (astar.isValid(robot, waypoint)) { // If valid waypoint
            // move is false when running fastest path
            // removing first turn penalty to find fastest path due to initial calibration
            // before timing
            if (!move) {// ????
                astar.setFirstTurnPenalty(false);
            }

            // if way point was set: go to way point first
            path = astar.AStarPathFinderAlgo(robot, robot.getPosition(), waypoint, on_grid);
            if (path != null) {
                astar.setFirstTurnPenalty(true);
                path1 = path;
                path2 = astar.AStarPathFinderAlgo(robot, waypoint, goal, on_grid);
                path = new int[path1.length + path2.length];
                System.arraycopy(path1, 0, path, 0, path1.length);
                System.arraycopy(path2, 0, path, path1.length, path2.length);
            }
        } else {
            astar.setFirstTurnPenalty(true);
            path = astar.AStarPathFinderAlgo(robot, robot.getPosition(), goal, on_grid);
        }
        System.out.println(Arrays.toString(path));
        if ((path != null) && move) {
            if (ConnectionSocket.checkConnection() && FastestPathThread.getRunning()) {
                realFPmove(path, robot);
                System.out.println("Finsh sending");
                // move(robot, path, speed);
            } else {
                // get realPath
                System.out.println(getRealPath(path));
                move(robot, path, speed);
            }
        }
        System.out.println(Arrays.toString(path));
        System.out.println(Arrays.toString(robot.getWaypoint()));
        System.out.println("Finished Fastest Path");
        return path;
    }

    private String getRealPath(int[] path) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int direction : path) {
            if (direction == Constant.FORWARD) {
                count++;
            } else if (count > 0) { // No Longer forward
                if (count < 10) {
                    sb.append("W").append("0").append(count).append("|");
                } else if (count >= 10) {
                    sb.append("W").append(count).append("|");
                }
                if (direction == Constant.RIGHT) {
                    sb.append(Constant.TURN_RIGHT);
                    count = 1;
                } else if (direction == Constant.LEFT) {
                    sb.append(Constant.TURN_LEFT);
                    count = 1;
                } else if (direction == Constant.BACKWARD) {
                    sb.append(Constant.TURN_RIGHT).append(Constant.TURN_RIGHT);
                    count = 1;
                } else {
                    System.out.println("Error!");
                    return null;
                }
            } else {
                if (direction == Constant.RIGHT) {
                    sb.append(Constant.TURN_RIGHT);
                    count = 1;
                } else if (direction == Constant.LEFT) {
                    sb.append(Constant.TURN_LEFT);
                    count = 1;
                } else if (direction == Constant.BACKWARD) {
                    sb.append(Constant.TURN_RIGHT).append(Constant.TURN_RIGHT);
                    count = 1;
                }
            }
        }
        if (count < 10) {
            sb.append("W").append("0").append(count).append("|");
        }
        if (count >= 10) {
            sb.append("W").append(count).append("|");
        }
        String msg = sb.toString();
        System.out.println("Actual msg! " + msg);
        return msg;
    }

    private void realFPmove(int[] path, Robot robot) {

        // Append all the movement message into one full string and send at once
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int direction : path) {
            System.out.printf("count: %d, direction: %d\n", count, direction);
            if (direction == Constant.FORWARD) {
                count++;
            } else if (count > 0) {
                if (count < 10) {
                    sb.append("W").append("0").append(count).append("|");
                } else if (count >= 10) {
                    sb.append("W").append(count).append("|");
                }
                if (direction == Constant.RIGHT) {
                    sb.append(Constant.TURN_RIGHT);
                    count = 1; // Right means turn right + Go forward
                } else if (direction == Constant.LEFT) {
                    sb.append(Constant.TURN_LEFT);
                    count = 1;
                } else if (direction == Constant.BACKWARD) {
                    sb.append(Constant.TURN_RIGHT).append(Constant.TURN_RIGHT);
                    count = 1;
                } else {
                    System.out.println("Error!");
                    return;
                }
            } else {
                if (direction == Constant.RIGHT) {
                    sb.append(Constant.TURN_RIGHT);
                    count = 1;
                } else if (direction == Constant.LEFT) {
                    sb.append(Constant.TURN_LEFT);
                    count = 1;
                } else if (direction == Constant.BACKWARD) {
                    sb.append(Constant.TURN_RIGHT).append(Constant.TURN_RIGHT);
                    count = 1;
                }
            }
        }
        if (count < 10) {
            sb.append("W").append("0").append(count).append("|");
        }
        if (count >= 10) {
            sb.append("W").append(count).append("|");
        }
        String msg = sb.toString();
        System.out.println("Actual msg! " + msg);
        // [1, 0, 1, 0, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1]
        // Map 1: W02|A|W09|D|W08|A|W02|A|W02|D|W06|D|W4
        // msg = "W03|D|W03|D|D";
        robot.displayMessage("Message sent for FastestPath real run: " + msg, 2);
        ConnectionSocket.getInstance().sendMessage(msg);
    }

    private void move(Robot robot, int[] path, int speed) {
        Exploration ex = new Exploration(); // change isfrontempty to robot method?
        // Move the robot based on the path
        for (int direction : path) {
            if (!connection.ConnectionSocket.checkConnection()) {
                try {
                    TimeUnit.SECONDS.sleep(speed);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            if (direction == Constant.FORWARD) {
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else if (direction == Constant.RIGHT) {
                // robot.updateMap(); // Is this needed ? -> No sensors needed right... ->Only
                // simulatedMap
                robot.rotateRight();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else if (direction == Constant.LEFT) {
                // robot.updateMap();
                robot.rotateLeft();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else if (direction == Constant.BACKWARD) {
                // robot.updateMap();
                robot.rotateRight();
                // robot.updateMap();
                robot.rotateRight();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        robot.updateMap();
    }
}
