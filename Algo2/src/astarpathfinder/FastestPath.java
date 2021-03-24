package astarpathfinder;

import config.Constant;
import connection.ConnectionSocket;
import datastruct.Coordinate;
import exploration.Exploration;
import robot.Robot;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FastestPath {
    public int[] FastestPathAlgo(Robot robot, Coordinate waypoint, Coordinate goal, int speed, boolean onGrid,
            boolean move) {
        AStarPathFinder astar = new AStarPathFinder();
        astar.setDirection(robot.getDirection());
        astar.setFirst(true); // wtf -> this is set in the old code when the open list is empty? -> means no
                              // possible path
        int[] path, path1, path2;

        if (waypoint != null && astar.isValid(robot, waypoint.x, waypoint.y)) { // If valid waypoint
            // move is false when running fastest path
            // removing first turn penalty to find fastest path due to initial calibration
            // before timing
            if (!move) {// ????
                astar.setFirstTurnPenalty(false);
            }
            Coordinate startPos = robot.getPosition();
            // if way point was set: go to way point first
            path = astar.AStarPathFinderAlgo(robot, startPos, waypoint, onGrid);
            if (path != null) {
                astar.setFirstTurnPenalty(true);
                path1 = path;
                path2 = astar.AStarPathFinderAlgo(robot, waypoint, goal, onGrid);
                path = new int[path1.length + path2.length];
                System.arraycopy(path1, 0, path, 0, path1.length);
                System.arraycopy(path2, 0, path, path1.length, path2.length);
            }
        } else {
            astar.setFirstTurnPenalty(true);
            path = astar.AStarPathFinderAlgo(robot, robot.getPosition(), goal, onGrid);
        }
        System.out.println(Arrays.toString(path));
        if ((path != null) && move) {
            if (ConnectionSocket.checkConnection() && FastestPathThread.getRunning()) {
                move(robot, path, speed, onGrid, goal);
                System.out.println("Finsh sending");
            } else {
                // get realPath
                System.out.println(getRealPath(path));
                move(robot, path, speed, onGrid, goal);
                System.out.printf("At goal, x: %d, y: %d \n now", goal.x, goal.y);
            }
        }
        System.out.println(Arrays.toString(path));
        // System.out.println(Arrays.toString(robot.getWaypoint()));
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
            // System.out.printf("count: %d, direction: %d\n", count, direction);
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

    private void rotateToFaceObstacle(Robot robot, Coordinate endPos) {
        int direction = robot.getDirection();
        int x = robot.getPosition().x;
        int y = robot.getPosition().y;
        System.out.println("Direction: " + direction);
        switch (direction) {
        case Constant.NORTH:
            if (endPos.y == (y - 2)) { // original is posX - 2??
                System.out.println("First case");
                break;
            } else if (endPos.x == (x + 2) && endPos.y == y) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (endPos.x == (x - 2) && endPos.y != y) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else if (endPos.y == y + 2) {
                System.out.println("Fourth case");
                // robot.rotateRight();
                // robot.rotateRight();
                robot.rotate180();
                break;
            }
        case Constant.EAST:
            if (endPos.x == (x + 2)) {
                System.out.println("First case");
                break;
            } else if (endPos.y == (y + 2) && endPos.x == x) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (endPos.y == (y - 2) && endPos.x != x) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else if (endPos.x == (x - 2)) {
                System.out.println("Fourth case");
                // robot.rotateRight();
                // robot.rotateRight();
                robot.rotate180();
                break;
            }
        case Constant.SOUTH:
            if (endPos.y == (y + 2)) { // why not posX + 2??
                System.out.println("First case");
                break;
            } else if (endPos.x == (x - 2) && endPos.y == y) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (endPos.x == (x + 2) && endPos.y != y) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else if (endPos.y == (y - 2)) {
                System.out.println("Fourth case");
                // robot.rotateRight();
                // robot.rotateRight();
                robot.rotate180();
                break;
            }
        case Constant.WEST:
            if (endPos.x == (x - 2)) { // orig is posY
                System.out.println("First case");
                break;
            } else if (endPos.y == (y - 2) && endPos.x == x) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (endPos.y == (y + 2) && endPos.x != x) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else if (endPos.x == (x + 2)) {
                System.out.println("Fourth case");
                // robot.rotateRight();
                // robot.rotateRight();
                robot.rotate180();
                break;
            }
        }
        robot.updateMap();
    }

    private void move(Robot robot, int[] path, int speed, boolean onGrid, Coordinate endPos) {
        Exploration ex = new Exploration();
        // Move the robot based on the path
        for (int direction : path) {
            System.out.printf("Fastest path move: x: %d, y: %d \n", robot.getPosition().x, robot.getPosition().y);
            // System.out.println("Move " + direction);
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
                // simulatedMap
                robot.rotateRight();
                robot.updateMap();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else if (direction == Constant.LEFT) {
                robot.rotateLeft();
                robot.updateMap();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else if (direction == Constant.BACKWARD) {
                robot.rotateRight();
                robot.updateMap();
                robot.rotateRight();
                robot.updateMap();
                if (ex.isFrontEmpty(robot)) {
                    robot.forward(1);
                } else {
                    return;
                }
            } else {
                return;
            }
            robot.updateMap();
        }
        if (!onGrid) {
            System.out.println("Rotate to face obstacle!!");
            rotateToFaceObstacle(robot, endPos);
        }
    }
}
