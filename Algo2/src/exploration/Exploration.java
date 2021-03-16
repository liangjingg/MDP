package exploration;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import astarpathfinder.FastestPath;
import robot.Robot;
import map.Map;
import config.Constant;
import datastruct.Coordinate;
import datastruct.Obstacle;

public class Exploration {
    private FastestPath fp = new FastestPath();
    private Map map;
    private boolean imageStop;
    private int countOfMoves = 0;

    public void ExplorationAlgo(Robot robot, int time, int percentage, int speed, boolean image_recognition) {
        map = robot.getMap();

        if ((speed == 1) && (time == -1) && (percentage == 100)) {
            if (image_recognition) {
                imageStop = false;
                imageRecExploration(robot);
            } else {
                normalExploration(robot);
            }
        } else {
            limitedExploration(robot, time, percentage, speed);
        }
        cornerCalibration(robot);
    }

    private void limitedExploration(Robot robot, int time, int percentage, int speed) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        robot.setDirection(2);

        do {
            if (time != -1) {
                // to account for the time-limited exploration
                int time_taken = (int) stopwatch.getElapsedTime();
                if (time_taken >= time) {
                    return;
                }
            }
            if (percentage != 100) {
                // to account for the coverage-limited exploration
                if (percentComplete(robot) >= percentage) {
                    return;
                }
            }

            System.out.println("Phase 1");
            move(robot, speed, null);
            cornerCalibration(robot);
        } while (!atPosition(robot, Constant.START));

        Coordinate unexploredTemp = nearestUnexplored(robot, robot.getPosition());
        int[] unexplored = new int[] { unexploredTemp.x, unexploredTemp.y };

        while (unexplored != null) {
            if (time != -1) {
                // to account for the time-limited exploration
                int time_taken = (int) stopwatch.getElapsedTime();
                if (time_taken >= time) {
                    return;
                }
            }
            if (percentage != 100) {
                // to account for the coverage-limited exploration
                if (percentComplete(robot) >= percentage) {
                    return;
                }
            }

            // fastest path to nearest unexplored square
            System.out.println("Phase 2");
            int[] path = fp.FastestPathAlgo(robot, null, unexplored, speed, false, true);
            if ((path == null) || (map.getGrid(unexplored[0], unexplored[1]).equals(Constant.UNEXPLORED))) {
                map.setGrid(unexplored[0], unexplored[1], Constant.OBSTACLE);
            }
            unexploredTemp = nearestUnexplored(robot, robot.getPosition());
            unexplored = new int[] { unexploredTemp.x, unexploredTemp.y };
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPathAlgo(robot, null, Constant.START, speed, true, true);
        }

        stopwatch.stop();
        System.out.println("Exploration Complete!");
    }

    private void normalExploration(Robot robot) {
        do {
            move(robot, 1, null);
            // robot.right_align();
            // corner_calibration(robot);
        } while (!atPosition(robot, Constant.START));

        Coordinate unexploredTemp = nearestUnexplored(robot, robot.getPosition()); // Returns the
        // System.out.println("Unexplored: " + Arrays.toString(unexplored));
        int[] unexplored = new int[] { unexploredTemp.x, unexploredTemp.y };
        while (unexplored != null) {
            // fastest path to nearest unexplored square
            System.out.println("Phase 2");
            int[] path = fp.FastestPathAlgo(robot, null, unexplored, 1, false, true);
            if ((path == null) || (map.getGrid(unexplored[0], unexplored[1]).equals(Constant.UNEXPLORED))) {
                // No path to the nearest unexplored/Remains unexplored -> Because it is an
                // obstacle
                map.setGrid(unexplored[0], unexplored[1], Constant.OBSTACLE);
            }

            unexploredTemp = nearestUnexplored(robot, robot.getPosition());
            unexplored = new int[] { unexploredTemp.x, unexploredTemp.y };
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPathAlgo(robot, null, Constant.START, 1, true, true);
        }

        System.out.println("Exploration Complete!");
    }

    private void imageRecExploration(Robot robot) {
        Set<Obstacle> checkedObstacles = new HashSet<>();
        // int[][] checkedObstacles = { { 0 } };
        boolean unexplored = false;
        Coordinate needTake = null;
        Coordinate whereToGo = null;
        boolean move = false;

        Coordinate defaultCoord = new Coordinate(-1, -1);
        Obstacle defaultPos = new Obstacle(defaultCoord, -1); // x , y and direction of the robot
        Obstacle[] obsPos = new Obstacle[] { defaultPos, defaultPos, defaultPos }; // 3 x 3 array //use arraylist
        do {
            checkedObstacles = move(robot, 1, checkedObstacles);
            System.out.println("Checked obstacles:");
            for (Obstacle o : checkedObstacles) {
                System.out.printf("x: %d, y: %d, direction: %d, ", o.coordinates.x, o.coordinates.y, o.direction);
            }
            System.out.println();
            // if (this.countOfMoves % 4 == 0) {
            // robot.rightAlign();
            // }
            cornerCalibration(robot);
        } while (!atPosition(robot, Constant.START));

        // Robot is at the start
        cornerCalibration(robot);
        if (!this.imageStop) {
            this.imageStop = robot.captureImage(obsPos);
        }

        // If there is an obstacle whose coordinate isnt checked yet (not including
        // direction), go there
        if (!this.imageStop) {
            // nearest obstacle whose coordinate isnt in checked obstacles
            needTake = pictureTaken(robot, robot.getPosition(), checkedObstacles);
            System.out.println("Need take: " + needTake);
            // go next to the obstacle
            whereToGo = nextToObstacle(robot, needTake);
            System.out.println("Where to go (Picture taken): " + whereToGo);
        }
        // else, go to the nearest unexplored
        if (whereToGo == null) {
            unexplored = true;
            needTake = null;
            whereToGo = nearestUnexplored(robot, robot.getPosition());
            System.out.println("Where to go (Nearest unexplored): " + whereToGo);
        }
        int[] goTo = null;
        while ((whereToGo != null) && !(this.imageStop)) {
            System.out.println("Phase 2");
            goTo = new int[] { whereToGo.x, whereToGo.y };
            int[] path = fp.FastestPathAlgo(robot, null, goTo, 1, true, true);
            if ((unexplored)
                    && ((path == null) || (map.getGrid(whereToGo.x, whereToGo.y).equals(Constant.UNEXPLORED)))) {
                map.setGrid(whereToGo.x, whereToGo.y, Constant.OBSTACLE);
                // int[][] temp = new int[checkedObstacles.size()][3];
                // System.arraycopy(checkedObstacles, 0, temp, 0, checkedObstacles.size());
                // temp[checkedObstacles.size()] = whereToGo;
                // checkedObstacles = temp;
                checkedObstacles.add(new Obstacle(whereToGo, -1));
            } else { // if not unexplored (pictureTaken) or path not null and grid is not unexplored
                     // (successfully do fastest path)
                System.out.println("Not unexplored (Nearest obstacle)");
                move = obstacleOnRight(robot, needTake); // false if need take is null
                System.out.println("obstacle on right " + move);
            }

            // go in a circle around d block (will be an island if not reachable during the
            // first phase)
            if ((path != null) && move) {
                System.out.println("Valid path + move");
                do {
                    System.out.println("MOVEEEE");
                    checkedObstacles = move(robot, 1, checkedObstacles);
                    // System.out.println(Arrays.deepToString(checkedObstacles));
                } while ((!atPosition(robot, goTo)) && !imageStop);
            }

            imageRecognition(robot, checkedObstacles);

            unexplored = false;
            needTake = pictureTaken(robot, robot.getPosition(), checkedObstacles);
            whereToGo = nextToObstacle(robot, needTake);
            System.out.println("Where to go (Picture taken): " + whereToGo);

            if (whereToGo == null) {
                unexplored = true;
                needTake = null;
                whereToGo = nearestUnexplored(robot, robot.getPosition());
                System.out.println("Where to go (Nearest unexplored): " + whereToGo);
            }

            if (whereToGo == null) {
                // to return to start after each "island"
                fp.FastestPathAlgo(robot, null, Constant.START, 1, true, true);
                cornerCalibration(robot);
            } else { // means all explored?
                // to corner calibrate after each "island"
                fp.FastestPathAlgo(robot, null, nearestCorner(robot), 1, true, true);
                cornerCalibration(robot);
                if (!this.imageStop) {
                    this.imageStop = robot.captureImage(obsPos);
                }
            }

            robot.updateMap();
        }

        whereToGo = nearestUnexplored(robot, robot.getPosition());
        goTo = new int[] { whereToGo.x, whereToGo.y };
        while ((whereToGo != null) && this.imageStop) {
            // fastest path to nearest unexplored square
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));

            int[] path = fp.FastestPathAlgo(robot, null, goTo, 1, false, true);
            if ((path == null) || (map.getGrid(whereToGo.x, whereToGo.y).equals(Constant.UNEXPLORED))) {
                map.setGrid(whereToGo.x, whereToGo.y, Constant.OBSTACLE);
            }
            whereToGo = nearestUnexplored(robot, robot.getPosition());
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 4");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPathAlgo(robot, null, Constant.START, 1, true, true);
        }

        System.out.println("Exploration Complete!");
    }

    private Set<Obstacle> move(Robot robot, int speed, Set<Obstacle> checkedObstacles) {
        System.out.println(
                Arrays.toString(robot.getPosition()) + " Direction: " + Constant.DIRECTIONS[robot.getDirection()]);
        robot.updateMap();
        // System.out.println(obstacles);

        if (!connection.ConnectionSocket.checkConnection()) {
            try {
                TimeUnit.SECONDS.sleep(speed);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        // Right Wall Hugging
        if (isRightEmpty(robot)) {
            System.out.println("Right is empty");
            robot.rotateRight();
            if (isFrontEmpty(robot)) {
                System.out.println("Front is empty");
                robot.forward(1);
                this.countOfMoves += 1;
                return checkedObstacles;
            } else { // Right empty but turn right and front not empty?? (Inaccuracy of sensors?)

                robot.rotateLeft();
                if ((checkedObstacles != null) && (!rightWall(robot))) {
                    checkedObstacles = imageRecognition(robot, checkedObstacles);
                }
            }
        } else if ((checkedObstacles != null) && (!rightWall(robot))) { // right not empty
            checkedObstacles = imageRecognition(robot, checkedObstacles);
        }
        if (isFrontEmpty(robot)) { // Robot is along wall now
            System.out.println("Right not empty but front empty");
            robot.forward(1);
            this.countOfMoves += 1;
            return checkedObstacles;
        } else {
            System.out.println("Right and Front not empty, turn left!");
            robot.rotateLeft(); // Robot faces north
            if ((checkedObstacles != null) && (!rightWall(robot))) {
                checkedObstacles = imageRecognition(robot, checkedObstacles);
                System.out.println("Here image stop: " + this.imageStop);
            }
        }
        if (isFrontEmpty(robot)) {
            robot.forward(1);
            this.countOfMoves += 1;
            return checkedObstacles;
        } else {
            robot.rotateLeft(); // Robot reverses
            if ((checkedObstacles != null) && (!rightWall(robot))) {
                checkedObstacles = imageRecognition(robot, checkedObstacles);
            }
        }
        if (isFrontEmpty(robot)) {
            robot.forward(1);
            this.countOfMoves += 1;
        } else {
            System.out.println("Error during exploration phase 1. All 4 sides blocked.");
        }
        return checkedObstacles;
    }

    // Still figuring out this function
    private Set<Obstacle> imageRecognition(Robot robot, Set<Obstacle> checkedObstacles) {
        if (this.imageStop) {
            return checkedObstacles;
        }
        System.out.println("Image Recognition Function: " + Arrays.toString(robot.getPosition()) + " Direction: "
                + Constant.DIRECTIONS[robot.getDirection()]);
        int x = robot.getPosition()[0];
        int y = robot.getPosition()[1];
        int direction = robot.getDirection();
        Coordinate defaultCoord = new Coordinate(-1, -1);
        Obstacle defaultPos = new Obstacle(defaultCoord, -1); // x , y and direction of the robot
        Obstacle[] obsPos = new Obstacle[] { defaultPos, defaultPos, defaultPos }; // 3 x 3 array //use arraylist
        boolean takePic = false;

        // Checking the 3x3 grid on the right of the robot based on where it is facing
        switch (direction) {
        case Constant.NORTH:
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map.getGrid(x + 2 + j, y - 1 + i).equals(Constant.OBSTACLE)) {
                        Coordinate coordinate = new Coordinate(x + 2 + j, y - 1 + i);
                        obsPos[i] = new Obstacle(coordinate, Constant.NORTH);
                        break;
                    }
                }
            }
            break;
        case Constant.SOUTH:
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map.getGrid(x - 2 - j, y + 1 - i).equals(Constant.OBSTACLE)) {
                        Coordinate coordinate = new Coordinate(x - 2 - j, y + 1 - i);
                        obsPos[i] = new Obstacle(coordinate, Constant.SOUTH);
                        break;
                    }
                }
            }
            break;
        case Constant.EAST:
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map.getGrid(x + 1 - i, y + 2 + j).equals(Constant.OBSTACLE)) {
                        Coordinate coordinate = new Coordinate(x + 1 - i, y + 2 + j);
                        obsPos[i] = new Obstacle(coordinate, Constant.EAST);
                        break;
                    }
                }
            }
            break;
        case Constant.WEST:
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map.getGrid(x - 1 + i, y - 2 - j).equals(Constant.OBSTACLE)) {
                        Coordinate coordinate = new Coordinate(x - 1 + i, y - 2 - j);
                        obsPos[i] = new Obstacle(coordinate, Constant.WEST);
                        break;
                    }
                }
            }
            break;
        }

        // Check if they are the default values or wall or checked obstacles
        for (int k = 0; k < obsPos.length; k++) {
            System.out.printf("Checking obstacle at x: %d, y: %d, direction: %d \n", obsPos[k].coordinates.x,
                    obsPos[k].coordinates.y, obsPos[k].direction);
            if (withinMap(obsPos[k].coordinates.x, obsPos[k].coordinates.y) && !checkedObstacles.contains(obsPos[k])) {
                if (k == 2 || !isFrontEmpty(robot)) {
                    takePic = true;
                }
            } else {
                obsPos[k] = defaultPos;
            }
        }

        if (takePic) {
            for (int k = 0; k < obsPos.length; k++) {
                if (!obsPos[k].equals(defaultPos)) {
                    checkedObstacles.add(obsPos[k]);
                }
            }
            this.imageStop = robot.captureImage(obsPos);
            System.out.println("Image stop: " + this.imageStop);
        }
        System.out.println("Finish image recognition  function");
        // if (isFrontEmpty(robot) && !(Arrays.equals(obsPos[2], defaultPos))) {
        // takePic = true;
        // } else if (!isFrontEmpty(robot)) {
        // for (int i = 0; i < obsPos.length; i++) {
        // if (!(Arrays.equals(obsPos[i], defaultPos))) {
        // takePic = true;
        // break;
        // }
        // }
        // }
        // for (int m = 0; m < 3; m++) {
        // // There is an obstacle
        // if (!(Arrays.equals(obsPos[m], defaultPos))) {
        // if (isFrontEmpty(robot) ) {
        // checkedObstacles[0][0] = m + 1; // this is the initial array {{0}}
        // } // means take pic only if m = 2 or front not empty?
        // if ((checkedObstacles[0][0] > 2) || (!isFrontEmpty(robot))) {
        // takePic = true;
        // }
        // }
        // }
        // System.out.println("Direction: " + robot.getDirection());
        // if (takePic) {
        // for (Obstacle obs: obsPos) {
        // if (!(obs.equals(defaultPos)) {
        // System.out.printf("Iterate through obstacles: %d, y: %d \n", obs.x, obs.y);
        // checkedObstacles.add(obs);
        // // int len = checked_obstacles.length; // adds to the checked obstacles ->
        // // change to arrya list?? LOL
        // // int[][] temp = new int[len + 1][3];
        // // System.arraycopy(checked_obstacles, 0, temp, 0, len);
        // // temp[len] = obs;
        // // checked_obstacles = temp;
        // }
        // }
        // // checkedObstacles[0][0] = 0;
        // this.image_stop = robot.captureImage(obsPos);
        // }

        return checkedObstacles;
    }

    private boolean withinMap(int x, int y) {
        return (x < Constant.BOARDWIDTH) && (x >= 0) && (y < Constant.BOARDHEIGHT) && (y >= 0);
    }

    private boolean rightWall(Robot robot) {
        int direction = robot.getDirection();
        int[] pos = robot.getPosition();
        switch (direction) {
        case Constant.NORTH:
            return pos[0] == Constant.BOARDWIDTH - 2;
        case Constant.SOUTH:
            return pos[0] == 1;
        case Constant.EAST:
            return pos[1] == Constant.BOARDHEIGHT - 2;
        case Constant.WEST:
            return pos[1] == 1;
        default:
            return true;
        }
    }

    private boolean isRightEmpty(Robot robot) {
        int[] obstacles = robot.updateMap(); // updateMap returns a list of 6 integers -> Converts to a list
                                             // of 6 booleans if obstacles or not for each sensor
        if (obstacles[3] == 1 || obstacles[4] == 1) {
            return false;
        }
        int[] position = robot.getPosition();
        int direction = robot.getDirection();
        Map map = robot.getMap();

        switch (direction) { // Detect for two sensors so can move by 2 grid
        case Constant.EAST:
            position[1] += 2; // Increase y by 2
            break;
        case Constant.WEST:
            position[1] -= 2; // Decrease y by 2
            break;
        case Constant.SOUTH:
            position[0] -= 2; // Decrease x by 2
            break;
        case Constant.NORTH:
            position[0] += 2; // Increase x by 2
            break;
        }
        return map.getGrid(position[0], position[1]).equals(Constant.EXPLORED)
                || map.getGrid(position[0], position[1]).equals(Constant.STARTPOINT)
                || map.getGrid(position[0], position[1]).equals(Constant.ENDPOINT);
    }

    public boolean isFrontEmpty(Robot robot) {
        int[] obstacles = robot.updateMap();
        return (obstacles[0] != 1) && (obstacles[1] != 1) && (obstacles[2] != 1);
    };

    private void cornerCalibration(Robot robot) {
        int[] pos = robot.getPosition();
        if (!(((pos[0] == 1) || (pos[0] == Constant.BOARDWIDTH - 2))
                && ((pos[1] == 1) || (pos[1] == Constant.BOARDHEIGHT - 2)))) {
            return;
        }
        System.out.println("At corner!");
        robot.updateMap();
        int direction = robot.getDirection();
        if ((pos[0] == 1) && (pos[1] == 13)) {
            switch (direction) {
            case Constant.NORTH:
                robot.rotateRight();
                robot.rotateRight();
                break;
            case Constant.EAST:
                robot.rotateRight();
                break;
            case Constant.WEST:
                robot.rotateLeft();
                break;
            default:
                break;
            }
        } else if ((pos[0] == Constant.BOARDWIDTH - 2) && (pos[1] == Constant.BOARDHEIGHT - 2)) {
            switch (direction) {
            case Constant.WEST:
                robot.rotateRight();
                robot.rotateRight();
                break;
            case Constant.NORTH:
                robot.rotateRight();
                break;
            case Constant.SOUTH:
                robot.rotateLeft();
                break;
            default:
                break;
            }
        } else if ((pos[0] == Constant.BOARDWIDTH - 2) && (pos[1] == 1)) {
            switch (direction) {
            case Constant.SOUTH:
                robot.rotateRight();
                robot.rotateRight();
                break;
            case Constant.WEST:
                robot.rotateRight();
                break;
            case Constant.EAST:
                robot.rotateLeft();
                break;
            default:
                break;
            }
        } else if ((pos[0] == 1) && (pos[1] == 1)) {
            switch (direction) {
            case Constant.EAST:
                robot.rotateRight();
                robot.rotateRight();
                break;
            case Constant.SOUTH:
                robot.rotateRight();
                break;
            case Constant.NORTH:
                robot.rotateLeft();
                break;
            default:
                break;
            }
        }
        robot.calibrate();
        int newdirection = robot.getDirection();

        switch (Math.abs(direction - newdirection + 4) % 4) {
        case 1:
            robot.rotateRight();
            break;
        case 2:
            robot.rotateRight();
            robot.rotateRight();
            break;
        case 3:
            robot.rotateLeft();
            break;
        }
    }

    private int[] nearestCorner(Robot robot) {
        int[] pos = robot.getPosition();
        int[][] corners = new int[][] { { 1, 1 }, { 1, Constant.BOARDHEIGHT - 2 }, { Constant.BOARDWIDTH - 2, 1 },
                { Constant.BOARDWIDTH - 2, Constant.BOARDHEIGHT - 2 } };
        int[] costs = new int[4];
        int cheapest_index = 0;

        for (int i = 0; i < 4; i++) {
            boolean valid = true;
            int x = corners[i][0];
            int y = corners[i][1];
            Map map = robot.getMap();
            int[][] grid = new int[][] { { x - 1, y - 1 }, { x, y - 1 }, { x + 1, y - 1 }, { x - 1, y }, { x, y },
                    { x + 1, y }, { x - 1, y + 1 }, { x, y + 1 }, { x + 1, y + 1 } };
            for (int[] grids : grid) {
                if (!map.getGrid(grids[0], grids[1]).equals(Constant.EXPLORED)) {
                    valid = false;
                }
            }
            if (valid) {
                costs[i] = Math.abs(pos[0] - corners[i][0]) + Math.abs(pos[1] - corners[i][1]);
                if (costs[i] < costs[cheapest_index]) {
                    cheapest_index = i;
                }
            }
        }

        return corners[cheapest_index];
    }

    private boolean atPosition(Robot robot, int[] goal) {
        int[] pos = robot.getPosition();
        return (Arrays.equals(pos, goal));
    };

    private Coordinate nearestUnexplored(Robot robot, int[] start) {
        Map map = robot.getMap();
        int lowestCost = Constant.MAXFCOST;
        Coordinate cheapestPos = null;
        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                if (map.getGrid(i, j).equals(Constant.UNEXPLORED)) {
                    int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                    if (cost < lowestCost) {
                        cheapestPos = new Coordinate(i, j);
                        lowestCost = cost;
                    }
                }
            }
        }
        return cheapestPos;
    }

    private Coordinate nextToObstacle(Robot robot, Coordinate next) {
        if (next == null) {
            return null;
        }

        int x = next.x;
        int y = next.y;
        int[][] order = new int[][] { { x - 1, y - 2 }, { x, y - 2 }, { x + 1, y - 2 }, { x + 2, y - 1 }, { x + 2, y },
                { x + 2, y + 1 }, { x + 1, y + 2 }, { x, y + 2 }, { x - 1, y + 2 }, { x - 2, y + 1 }, { x - 2, y },
                { x - 2, y - 1 } };
        Map map = robot.getMap();

        for (int[] pos : order) {
            if ((withinMap(pos[0], pos[1])) && (map.getGrid(pos[0], pos[1]).equals(Constant.EXPLORED))) {
                return new Coordinate(pos[0], pos[1]);
            }
        }
        return null;
    }

    private Coordinate pictureTaken(Robot robot, int[] start, Set<Obstacle> checkedObstacles) {
        Map map = robot.getMap();
        int lowestCost = Constant.MAXFCOST;
        // Coordinate cheapestPos = null;
        int x = 0;
        int y = 0;

        Set<Coordinate> coordinates = checkedObstacles.stream().map(obstacle -> obstacle.coordinates)
                .collect(Collectors.toSet());

        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                if (map.getGrid(i, j).equals(Constant.OBSTACLE)) {
                    // boolean not_inside = true;
                    if (!coordinates.contains(new Coordinate(i, j))) {
                        // not_inside = false;
                        int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                        if (cost < lowestCost) {
                            // cheapestPos = new Coordinate(i, j);
                            x = i;
                            y = j;
                            lowestCost = cost;
                        }
                    }
                    // for (Obstacle obs : checkedObstacles) {
                    // Coordinate cur = new Coordinate(i, j);
                    // if (obs.coordinates.equals(cur)) {
                    // not_inside = false;
                    // break;
                    // }
                    // }
                    // if (not_inside) {
                    // int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                    // if (cost < lowest_cost) {
                    // cheapest_pos = new int[] { i, j };
                    // lowest_cost = cost;
                    // }
                    // }
                }
            }
        }
        return new Coordinate(x, y);
    }

    private int[] furthest(Robot robot, int[][] checked_obstacles) {
        Map map = robot.getMap();
        int highest_cost = -1;
        int[] ex_pos = null;

        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                if (map.getGrid(i, j).equals(Constant.OBSTACLE)) {
                    boolean not_inside = true;
                    for (int k = 1; k < checked_obstacles.length; k++) {
                        int[] o_pos = { checked_obstacles[k][0], checked_obstacles[k][1] };
                        int[] cur = { i, j };
                        if (Arrays.equals(o_pos, cur)) {
                            not_inside = false;
                            break;
                        }
                    }
                    if (not_inside) {
                        int cost = Math.abs(Constant.END[0] - i) + Math.abs(Constant.END[1] - j);
                        if (cost > highest_cost) {
                            ex_pos = new int[] { i, j };
                            highest_cost = cost;
                        }
                    }
                }
            }
        }
        return ex_pos;
    }

    private boolean obstacleOnRight(Robot robot, Coordinate obstacle) {
        if (obstacle == null) {
            return false;
        }
        int direction = robot.getDirection();
        int[] pos = robot.getPosition();
        System.out.printf("Obstacle on right: x: %d, y: %d, direction: %d", pos[0], pos[1], direction);
        switch (direction) {
        case Constant.NORTH:
            if (obstacle.x == (pos[0] - 2)) { // why not pos[0] + 2??
                break;
            } else if (obstacle.y == (pos[1] + 2)) {
                robot.rotateRight();
                break;
            } else if (obstacle.y == (pos[1] - 2)) {
                robot.rotateLeft();
                break;
            } else {
                robot.rotateRight();
                robot.rotateRight();
                break;
            }
        case Constant.EAST:
            if (obstacle.y == (pos[1] + 2)) {
                System.out.println("First case");
                break;
            } else if (obstacle.x == (pos[0] - 2)) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (obstacle.x == (pos[0] + 2)) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else {
                System.out.println("Fourth case");
                robot.rotateRight();
                robot.rotateRight();
                break;
            }
        case Constant.SOUTH:
            if (obstacle.x == (pos[0] + 2)) { // why not pos[0] - 2??
                System.out.println("First case");
                break;
            } else if (obstacle.y == (pos[1] - 2)) {
                System.out.println("Second case");
                robot.rotateRight();
                break;
            } else if (obstacle.y == (pos[1] + 2)) {
                System.out.println("Third case");
                robot.rotateLeft();
                break;
            } else {
                System.out.println("Fourth case");
                robot.rotateRight();
                robot.rotateRight();
                break;
            }
        case Constant.WEST:
            if (obstacle.y == (pos[1] + 2)) { // why not pos[1] -2 ??
                break;
            } else if (obstacle.x == (pos[0] + 2)) {
                robot.rotateRight();
                break;
            } else if (obstacle.x == (pos[0] - 2)) {
                robot.rotateLeft();
                break;
            } else {
                robot.rotateRight();
                robot.rotateRight();
                break;
            }
        }
        return true;
    }

    private int percentComplete(Robot robot) {
        Map map = robot.getMap();
        int unexplored = 0;
        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                if (map.getGrid(i, j).equals(Constant.UNEXPLORED)) {
                    unexplored++;
                }
            }
        }
        return ((300 - unexplored) / 3);
    }
}