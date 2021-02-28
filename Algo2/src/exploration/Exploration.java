package exploration;

import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import astarpathfinder.FastestPath;
import robot.Robot;
import map.Map;
import config.Constant;

public class Exploration {
    private FastestPath fp = new FastestPath();
    private Map map;
    private boolean image_stop;

    public void ExplorationAlgo(Robot robot, int time, int percentage, int speed, boolean image_recognition) {
        map = robot.getMap();

        if ((speed == 1) && (time == -1) && (percentage == 100)) {
            if (image_recognition) {
                image_stop = false;
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

        int[] unexplored = nearestUnexplored(robot, robot.getPosition());

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
            int[] path = fp.FastestPath(robot, null, unexplored, speed, false, true);
            if ((path == null) || (map.getGrid(unexplored[0], unexplored[1]).equals(Constant.UNEXPLORED))) {
                map.setGrid(unexplored[0], unexplored[1], Constant.OBSTACLE);
            }
            unexplored = nearestUnexplored(robot, robot.getPosition());
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPath(robot, null, Constant.START, speed, true, true);
        }

        stopwatch.stop();
        System.out.println("Exploration Complete!");
    }

    private void normalExploration(Robot robot) {
        do {
            move(robot, 1, null);
            // corner_calibration(robot);
        } while (!atPosition(robot, Constant.START));

        int[] unexplored = nearestUnexplored(robot, robot.getPosition()); // Returns the

        while (unexplored != null) {
            // fastest path to nearest unexplored square
            System.out.println("Phase 2");
            int[] path = fp.FastestPath(robot, null, unexplored, 1, false, true);
            if ((path == null) || (map.getGrid(unexplored[0], unexplored[1]).equals(Constant.UNEXPLORED))) {
                // No path to the nearest unexplored/Remains unexplored -> Because it is an
                // obstacle
                map.setGrid(unexplored[0], unexplored[1], Constant.OBSTACLE);
            }

            unexplored = nearestUnexplored(robot, robot.getPosition());
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPath(robot, null, Constant.START, 1, true, true);
        }

        System.out.println("Exploration Complete!");
    }

    private void imageRecExploration(Robot robot) {
        int[][] checked_obstacles = { { 0 } };
        boolean unexplored = false;
        int[] need_take = null;
        int[] go_to = null;
        boolean move = false;

        do {
            checked_obstacles = move(robot, 1, checked_obstacles);
            System.out.println(Arrays.deepToString(checked_obstacles));
            cornerCalibration(robot);
        } while (!atPosition(robot, Constant.START));

        cornerCalibration(robot);
        if (!this.image_stop) {
            this.image_stop = robot.captureImage(new int[][] { { -1, -1, -1 }, { -1, -1, -1 }, { -1, -1, -1 } });
        }

        if (!this.image_stop) {
            need_take = pictureTaken(robot, robot.getPosition(), checked_obstacles);
            go_to = next_to_obstacle(robot, need_take);
        }

        if (go_to == null) {
            unexplored = true;
            need_take = null;
            go_to = nearestUnexplored(robot, robot.getPosition());
        }

        while ((go_to != null) && !(this.image_stop)) {
            // fastest path to nearest obstacle that photos have not been taken photo of
            System.out.println("Phase 2");
            int[] path = fp.FastestPath(robot, null, go_to, 1, true, true);
            if ((unexplored) && ((path == null) || (map.getGrid(go_to[0], go_to[1]).equals(Constant.UNEXPLORED)))) {
                map.setGrid(go_to[0], go_to[1], Constant.OBSTACLE);
                int[][] temp = new int[checked_obstacles.length + 1][3];
                System.arraycopy(checked_obstacles, 0, temp, 0, checked_obstacles.length);
                temp[checked_obstacles.length] = go_to;
                checked_obstacles = temp;
            } else {
                move = obstacle_on_right(robot, need_take);
            }

            if ((path != null) && move) {
                do {
                    checked_obstacles = move(robot, 1, checked_obstacles);
                    System.out.println(Arrays.deepToString(checked_obstacles));
                } while ((!atPosition(robot, go_to)) && !image_stop);
            }

            imageRecognition(robot, checked_obstacles);

            unexplored = false;
            need_take = pictureTaken(robot, robot.getPosition(), checked_obstacles);
            go_to = next_to_obstacle(robot, need_take);

            if (go_to == null) {
                unexplored = true;
                need_take = null;
                go_to = nearestUnexplored(robot, robot.getPosition());
            }

            if (go_to == null) {
                // to return to start after each "island"
                fp.FastestPath(robot, null, Constant.START, 1, true, true);
                cornerCalibration(robot);
            } else {
                // to corner calibrate after each "island"
                fp.FastestPath(robot, null, nearest_corner(robot), 1, true, true);
                cornerCalibration(robot);
                if (!this.image_stop) {
                    this.image_stop = robot
                            .captureImage(new int[][] { { -1, -1, -1 }, { -1, -1, -1 }, { -1, -1, -1 } });
                }
            }

            robot.updateMap();
        }

        go_to = nearestUnexplored(robot, robot.getPosition());
        while ((go_to != null) && this.image_stop) {
            // fastest path to nearest unexplored square
            System.out.println("Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));

            int[] path = fp.FastestPath(robot, null, go_to, 1, false, true);
            if ((path == null) || (map.getGrid(go_to[0], go_to[1]).equals(Constant.UNEXPLORED))) {
                map.setGrid(go_to[0], go_to[1], Constant.OBSTACLE);
            }

            go_to = nearestUnexplored(robot, robot.getPosition());
            robot.updateMap();
        }

        if (!atPosition(robot, Constant.START)) {
            // fastest path to start point
            System.out.println("Phase 4");
            System.out.println(Arrays.toString(robot.getPosition()));
            fp.FastestPath(robot, null, Constant.START, 1, true, true);
        }

        System.out.println("Exploration Complete!");
    }

    // private boolean[] crash(int[] isObstacle) {
    // boolean[] crash = new boolean[6];
    // for (int i = 0; i < 6; i++) {
    // if (isObstacle[i] == 1) {
    // crash[i] = true;
    // } else {
    // crash[i] = false;
    // }
    // }
    // return crash;
    // }

    private int[][] move(Robot robot, int speed, int[][] checked_obstacles) {
        System.out.println(Arrays.toString(robot.getPosition()));
        int[][] checked = checked_obstacles; // Initially null if normal exploration
        int[] obstacles = robot.updateMap();
        System.out.println(obstacles);

        if (!connection.ConnectionSocket.checkConnection()) {
            try {
                TimeUnit.SECONDS.sleep(speed);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        // Right Wall Hugging
        if (isRightEmpty(robot)) {
            System.out.println("Rotate right");
            robot.rotateRight();
            if (isFrontEmpty(robot)) {
                robot.forward(1);
                return checked;
            } else { // At Right Corner
                robot.rotateLeft();
                if ((checked != null) && (!right_wall(robot))) { // In Normal Exploration this will never be called?
                    checked = imageRecognition(robot, checked);
                }
            }
        } else if ((checked != null) && (!right_wall(robot))) { // Wont run during normal exploration
            checked = imageRecognition(robot, checked);
        }
        if (isFrontEmpty(robot)) { // Robot is along wall now
            System.out.println("Go front");
            robot.forward(1);
            return checked;
        } else {
            robot.rotateLeft(); // Robot faces north
            if ((checked != null) && (!right_wall(robot))) {
                checked = imageRecognition(robot, checked);
            }
        }
        if (isFrontEmpty(robot)) {
            robot.forward(1);
            return checked;
        } else {
            robot.rotateLeft(); // Robot reverses
            if ((checked != null) && (!right_wall(robot))) {
                checked = imageRecognition(robot, checked);
            }
        }
        if (isFrontEmpty(robot)) {
            robot.forward(1);
        } else {
            System.out.println("Error during exploration phase 1. All 4 sides blocked.");
        }
        return checked;
    }

    // Still figuring out this function
    private int[][] imageRecognition(Robot robot, int[][] checked_obstacles) {
        if (this.image_stop) {
            return checked_obstacles;
        }

        int x = robot.getPosition()[0];
        int y = robot.getPosition()[1];
        int[] default_pos = new int[] { -1, -1, -1 }; // x , y and direction of the robot
        int[][] obs_pos = new int[][] { default_pos, default_pos, default_pos }; // 3 x 3 array
        boolean take_pic = false;
        int direction = robot.getDirection();

        // Checking the 3x3 grid on the right of the robot based on where it is facing
        switch (direction) {
            case Constant.NORTH:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (Arrays.equals(obs_pos[i], default_pos)
                                && map.getGrid(x + 2 + j, y - 1 + i).equals(Constant.OBSTACLE)) {
                            obs_pos[i] = new int[] { x + 2 + j, y - 1 + i, Constant.NORTH };
                        }
                    }
                }
                break;
            case Constant.SOUTH:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (Arrays.equals(obs_pos[i], default_pos)
                                && map.getGrid(x - 2 - j, y + 1 - i).equals(Constant.OBSTACLE)) {
                            obs_pos[i] = new int[] { x - 2 - j, y + 1 - i, Constant.SOUTH };
                        }
                    }
                }
                break;
            case Constant.EAST:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (Arrays.equals(obs_pos[i], default_pos)
                                && map.getGrid(x + 1 - i, y + 2 + j).equals(Constant.OBSTACLE)) {
                            obs_pos[i] = new int[] { x + 1 - i, y + 2 + j, Constant.EAST };
                        }
                    }
                }
                break;
            case Constant.WEST:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (Arrays.equals(obs_pos[i], default_pos)
                                && map.getGrid(x - 1 + i, y - 2 - j).equals(Constant.OBSTACLE)) {
                            obs_pos[i] = new int[] { x - 1 + i, y - 2 - j, Constant.WEST };
                        }
                    }
                }
                break;
        }

        // Check if they are the default values or wall or checked obstacles
        for (int k = 0; k < 3; k++) {
            if (!within_map(obs_pos[k][0], obs_pos[k][1])) { // obs_pos[k][0] = x, obs_pos[k][1] = y
                obs_pos[k] = default_pos;
            } else {
                for (int[] obstacles : checked_obstacles) {
                    if (Arrays.equals(obstacles, obs_pos[k])) { // obs_pos[k] is already in checked obstacles
                        obs_pos[k] = default_pos;
                        break;
                    }
                }
            }
        }

        for (int m = 0; m < 3; m++) {
            // There is an obstacle
            if (!(Arrays.equals(obs_pos[m], default_pos))) { // Only left those that are already not checked
                if (isFrontEmpty(robot)) {
                    checked_obstacles[0][0] = m + 1;
                }
                if ((checked_obstacles[0][0] > 2) || (!isFrontEmpty(robot))) {
                    take_pic = true;
                }
            }
        }

        if (take_pic) {
            for (int[] obs : obs_pos) {
                if (!(Arrays.equals(obs, default_pos))) {
                    int len = checked_obstacles.length;
                    int[][] temp = new int[len + 1][3];
                    System.arraycopy(checked_obstacles, 0, temp, 0, len);
                    temp[len] = obs;
                    checked_obstacles = temp;
                }
            }
            checked_obstacles[0][0] = 0;
            this.image_stop = robot.captureImage(obs_pos);
        }

        return checked_obstacles;
    }

    private boolean within_map(int x, int y) {
        return (x <= 19) && (x >= 0) && (y <= 14) && (y >= 0);
    }

    private boolean right_wall(Robot robot) {
        int direction = robot.getDirection();
        int[] pos = robot.getPosition();
        switch (direction) {
            case Constant.NORTH:
                return pos[0] == 18;
            case Constant.SOUTH:
                return pos[0] == 1;
            case Constant.EAST:
                return pos[1] == 13;
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
                || map.getGrid(position[0], position[1]).equals(Constant.ENDPOINT); // basically if it is
        // obstacle/unexplored or waypoint ->
        // dont go
    }

    public boolean isFrontEmpty(Robot robot) {
        int[] obstacles = robot.updateMap();
        return (obstacles[0] != 1) && (obstacles[1] != 1) && (obstacles[2] != 1);
    };

    private void cornerCalibration(Robot robot) {
        int[] pos = robot.getPosition();
        if (!(((pos[0] == 1) || (pos[0] == 18)) && ((pos[1] == 1) || (pos[1] == 13)))) {
            return;
        }
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
        } else if ((pos[0] == 18) && (pos[1] == 13)) {
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
        } else if ((pos[0] == 18) && (pos[1] == 1)) {
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

    private int[] nearest_corner(Robot robot) {
        int[] pos = robot.getPosition();
        int[][] corners = new int[][] { { 1, 1 }, { 1, 13 }, { 18, 1 }, { 18, 13 } };
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

    private int[] nearestUnexplored(Robot robot, int[] start) {
        Map map = robot.getMap();
        int lowest_cost = 9999;
        int[] cheapest_pos = null;
        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                if (map.getGrid(i, j).equals(Constant.UNEXPLORED)) {
                    int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                    if (cost < lowest_cost) {
                        cheapest_pos = new int[] { i, j };
                        lowest_cost = cost;
                    }
                }
            }
        }
        return cheapest_pos;
    }

    private int[] next_to_obstacle(Robot robot, int[] next) {
        if (next == null) {
            return null;
        }

        int x = next[0];
        int y = next[1];
        int[][] order = new int[][] { { x - 1, y - 2 }, { x, y - 2 }, { x + 1, y - 2 }, { x + 2, y - 1 }, { x + 2, y },
                { x + 2, y + 1 }, { x + 1, y + 2 }, { x, y + 2 }, { x - 1, y + 2 }, { x - 2, y + 1 }, { x - 2, y },
                { x - 2, y - 1 } };
        Map map = robot.getMap();

        for (int[] pos : order) {
            if ((within_map(pos[0], pos[1])) && (map.getGrid(pos[0], pos[1]).equals(Constant.EXPLORED))) {
                return pos;
            }
        }
        return null;
    }

    private int[] pictureTaken(Robot robot, int[] start, int[][] checked_obstacles) {
        Map map = robot.getMap();
        int lowest_cost = 9999;
        int[] cheapest_pos = null;

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
                        int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                        if (cost < lowest_cost) {
                            cheapest_pos = new int[] { i, j };
                            lowest_cost = cost;
                        }
                    }
                }
            }
        }
        return cheapest_pos;
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

    private boolean obstacle_on_right(Robot robot, int[] obstacle) {
        if (obstacle == null) {
            return false;
        }
        int direction = robot.getDirection();
        int[] pos = robot.getPosition();

        switch (direction) {
            case Constant.NORTH:
                if (obstacle[0] == (pos[0] - 2)) {
                    break;
                } else if (obstacle[1] == (pos[1] + 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[1] == (pos[1] - 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Constant.SOUTH:
                if (obstacle[0] == (pos[0] + 2)) {
                    break;
                } else if (obstacle[1] == (pos[1] - 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[1] == (pos[1] + 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Constant.EAST:
                if (obstacle[1] == (pos[1] + 2)) {
                    break;
                } else if (obstacle[0] == (pos[0] - 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[0] == (pos[0] + 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Constant.WEST:
                if (obstacle[1] == (pos[1] + 2)) {
                    break;
                } else if (obstacle[0] == (pos[0] + 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[0] == (pos[0] - 2)) {
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
                if (map.getGrid(i, j).equals("Unexplored")) {
                    unexplored++;
                }
            }
        }
        return ((300 - unexplored) / 3);
    }
}