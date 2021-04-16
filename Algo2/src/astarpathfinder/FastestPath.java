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
        astar.setFirst(true);
        int[] path, path1, path2;

        if (waypoint != null && astar.isValid(robot, waypoint.x, waypoint.y)) { // If valid waypoint
            if (!move) {
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
                realFPmove(path, robot);
                System.out.println("Finsh sending");
            } else {
                // get realPath
                String pathString = getRealPath(path);
                FPsimulatorMove(pathString, robot);
                System.out.printf("At goal, x: %d, y: %d \n now", goal.x, goal.y);
            }
        }
        System.out.println(Arrays.toString(path));
        System.out.println("Finished Fastest Path");
        return path;
    }

    private void FPsimulatorMove(String path, Robot robot) {
        String[] moves = path.split("\\|");
        for (int i = 0; i < moves.length; i++) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            char action = moves[i].charAt(0);
            if (action == 'W') {
                int count = 0;
                if (moves[i].length() == 3) {
                    count = Integer.parseInt(moves[i].substring(1, 3));
                } else {
                    count = Integer.parseInt(moves[i].substring(1, 2));
                }
                robot.forward(count);
            } else if (action == 'A') {
                robot.rotateLeft();
            } else if (action == 'D') {
                robot.rotateRight();
            }
        }
    }

    private String getRealPath(int[] path) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int direction : path) {
            if (direction == Constant.FORWARD) {
                count++;
            } else if (count > 0) { // No Longer forward
                if (count < 10) {
                    sb.append("W").append(count).append("|");
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
                    sb.append(Constant.U_TURN);
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
                    sb.append(Constant.U_TURN);
                    count = 1;
                }
            }
        }
        if (count < 10) {
            sb.append("W").append(count).append("|");
        }
        if (count >= 10) {
            sb.append("W").append(count).append("|");
        }
        String msg = sb.toString();
        return msg;
    }

    private void realFPmove(int[] path, Robot robot) {

        // Append all the movement message into one full string and send at once
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int direction : path) {
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
        robot.displayMessage("Message sent for FastestPath real run: " + msg, 2);
        ConnectionSocket.getInstance().sendMessage(msg);
    }

    private void rotateToFaceObstacle(Robot robot, Coordinate endPos) {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int direction = robot.getDirection();
        int x = robot.getPosition().x;
        int y = robot.getPosition().y;
        switch (direction) {
        case Constant.NORTH:
            if (endPos.y == (y - 2)) { // original is posX - 2??
                break;
            } else if (endPos.x == (x + 2) && endPos.y == y) {
                robot.rotateRight();
                break;
            } else if (endPos.x == (x - 2) && endPos.y != y) {
                robot.rotateLeft();
                break;
            } else if (endPos.y == y + 2) {
                robot.rotate180();
                break;
            }
        case Constant.EAST:
            if (endPos.x == (x + 2)) {
                break;
            } else if (endPos.y == (y + 2) && endPos.x == x) {
                robot.rotateRight();
                break;
            } else if (endPos.y == (y - 2) && endPos.x != x) {
                robot.rotateLeft();
                break;
            } else if (endPos.x == (x - 2)) {
                robot.rotate180();
                break;
            }
        case Constant.SOUTH:
            if (endPos.y == (y + 2)) { // why not posX + 2??
                break;
            } else if (endPos.x == (x - 2) && endPos.y == y) {
                robot.rotateRight();
                break;
            } else if (endPos.x == (x + 2) && endPos.y != y) {
                robot.rotateLeft();
                break;
            } else if (endPos.y == (y - 2)) {
                robot.rotate180();
                break;
            }
        case Constant.WEST:
            if (endPos.x == (x - 2)) { // orig is posY
                break;
            } else if (endPos.y == (y - 2) && endPos.x == x) {
                robot.rotateRight();
                break;
            } else if (endPos.y == (y + 2) && endPos.x != x) {
                robot.rotateLeft();
                break;
            } else if (endPos.x == (x + 2)) {
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
