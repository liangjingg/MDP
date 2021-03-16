package astarpathfinder;

import robot.Robot;
import map.Map;
import config.Constant;

import java.util.PriorityQueue;

import java.util.Arrays;

public class AStarPathFinder {
    boolean first = true;
    int direction = -1;
    int initialDirection = -1;
    boolean first_penalty = true;
    Node[][] nodeDetails;

    public int[] AStarPathFinderAlgo(Robot robot, int[] start_pos, int[] end_pos, boolean on_grid) {
        // Node start = new Node(start_pos);
        initialDirection = robot.getDirection();
        Node cur = null;

        System.out.printf("End pos: x: %d, y: %d \n", end_pos[0], end_pos[1]);

        // matrix indicating whether is on open list or not
        boolean[][] openList = new boolean[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
        // matrix indicating whether is on closed list or not
        boolean[][] closedList = new boolean[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
        // Contains all the details of the node on the board -> parent, cost etc
        nodeDetails = new Node[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];

        // Node[] open = { start };
        // Node[] closed = {};

        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                openList[i][j] = false;
                closedList[i][j] = false;
                int[] pos = new int[] { i, j };
                nodeDetails[i][j] = new Node(pos);
                nodeDetails[i][j].cost = Constant.MAXFCOST;
                nodeDetails[i][j].g_cost = Constant.MAXFCOST;
                nodeDetails[i][j].h_cost = Constant.MAXFCOST;
                nodeDetails[i][j].parent = null;
            }
        }

        nodeDetails[start_pos[0]][start_pos[1]].g_cost = 0;
        nodeDetails[start_pos[0]][start_pos[1]].h_cost = 0;
        nodeDetails[start_pos[0]][start_pos[1]].cost = 0;

        Node startNode = nodeDetails[start_pos[0]][start_pos[1]];

        PriorityQueue<Node> openQueue = new PriorityQueue<Node>();
        openQueue.add(startNode);
        boolean pathFound = false;
        System.out.println("On grid: " + on_grid);

        while (!openQueue.isEmpty()) {
            // System.out.println("Priority Queue: ");
            // PriorityQueue<Node> queueCopy = new PriorityQueue<Node>(openQueue);
            // while (queueCopy.size() != 0) {
            // Node temp = queueCopy.poll();
            // System.out.printf("x: %d, y: %d, g_cost: %d, h_cost: %d, cost: %d \n",
            // temp.pos[0], temp.pos[1],
            // temp.g_cost, temp.h_cost, temp.cost);
            // }
            cur = openQueue.poll();
            // System.out.printf("Cur - x: %d, y: %d, g_cost: %d, h_cost: %d \n",
            // cur.pos[0], cur.pos[1],
            // nodeDetails[cur.pos[0]][cur.pos[1]].g_cost,
            // nodeDetails[cur.pos[0]][cur.pos[1]].h_cost);

            if (((!on_grid) && (canReach(cur.pos, end_pos, first)))
                    || ((on_grid) && (Arrays.equals(cur.pos, end_pos)))) {
                System.out.println("Path found!");
                pathFound = true;
                break;
            }
            closedList[cur.pos[0]][cur.pos[1]] = true;
            Node[] neighbours = getNeighbours(robot, cur, end_pos); // all the neighbours that are valid
            for (Node neighbour : neighbours) {
                if (neighbour != null) {
                    neighbour.parent = cur;
                    calculateCosts(neighbour, end_pos, robot);
                    // if (neighbour.pos[0] == 3 && neighbour.pos[1] == 1) {
                    // System.out.printf("Neighbour: Pos: x: %d, y: %d, g_cost: %d, h_cost: %d,
                    // cost: %d\n",
                    // neighbour.pos[0], neighbour.pos[1], neighbour.g_cost, neighbour.h_cost,
                    // neighbour.cost);
                    // }
                    // System.out.printf("Neighbour: Pos: x: %d, y: %d, g_cost: %d, h_cost: %d,
                    // cost: %d\n",
                    // neighbour.pos[0], neighbour.pos[1], neighbour.g_cost, neighbour.h_cost,
                    // neighbour.cost);
                    // nodeDetails[neighbour.pos[0]][neighbour.pos[1]] = neighbour; -> Removed this
                    // and fastest path worked
                    if (closedList[neighbour.pos[0]][neighbour.pos[1]])
                        continue;
                    // if (openList[neighbour.pos[0]][neighbour.pos[1]]) {
                    // Node temp = nodeDetails[neighbour.pos[0]][neighbour.pos[1]];
                    // System.out.printf(
                    // "Current in node details: Pos: x: %d, y: %d, g_cost: %d, h_cost: %d, cost:
                    // %d\n",
                    // temp.pos[0], temp.pos[1], temp.g_cost, temp.h_cost, temp.cost);
                    // }
                    if (!openList[neighbour.pos[0]][neighbour.pos[1]]
                            || neighbour.cost < nodeDetails[neighbour.pos[0]][neighbour.pos[1]].cost) { // shd update if
                                                                                                        // h_cost
                                                                                                        // lower??
                        openList[neighbour.pos[0]][neighbour.pos[1]] = true;
                        nodeDetails[neighbour.pos[0]][neighbour.pos[1]] = neighbour;
                        openQueue.add(neighbour); // need replace the old one??
                    }
                }
            }
        }

        if (pathFound) {
            int[] path = reversePath(cur);
            System.out.println(Arrays.toString(path));
            updateDirection(path);
            System.out.println("Path Found");
            return path;

        } else {
            return null;
        }
        // int[] path = reversePath(cur);
        // System.out.println(Arrays.toString(path));
        // updateDirection(path);
        // System.out.println("Path Found");
        // return path;

        // while (true) {
        // cur = lowestCost(open);

        // if (cur == null) {
        // System.out.println("Error: open is empty");
        // break;
        // } else {
        // open = removeNode(open, cur);
        // closed = addNode(closed, cur);

        // // // For troubleshooting
        // // // System.out.println(Arrays.toString(cur.pos));
        // // // System.out.println(cur.cost);
        // // // System.out.println(Arrays.toString(print(open)));
        // // // System.out.println(Arrays.toString(print(closed)));

        // if (((!on_grid) && (can_reach(cur.pos, end_pos, first)))
        // || ((on_grid) && (Arrays.equals(cur.pos, end_pos)))) {
        // System.out.println("Path found!");
        // break;
        // }

        // open = addNeighbours(robot, open, closed, cur, end_pos);

        // if (Arrays.equals(open, new Node[] {})) {
        // set_first(false);
        // System.out.println("Error: No possible path");
        // return null;
        // }
        // }
        // }

        // int[] path = reversePath(cur);
        // System.out.println(Arrays.toString(path));
        // update_direction(path);
        // System.out.println("Path Found");
        // return path;
    }

    private void updateDirection(int[] path) {
        if (path != null) {
            for (int value : path) {
                switch (value) {
                    case Constant.LEFT:
                        direction = (direction + 3) % 4;
                        break;
                    case Constant.RIGHT:
                        direction = (direction + 1) % 4;
                        break;
                    case Constant.BACKWARD:
                        direction = (direction + 2) % 4;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean canReach(int[] cur, int[] end, boolean first) {
        int x = end[0];
        int y = end[1];
        int[][] pos;
        // System.out.println("First: " + first);
        if (first) {
            pos = new int[][] { { x - 1, y - 2 }, { x, y - 2 }, { x + 1, y - 2 }, { x + 2, y - 1 }, { x + 2, y },
                    { x + 2, y + 1 }, { x + 1, y + 2 }, { x, y + 2 }, { x - 1, y + 2 }, { x - 2, y + 1 }, { x - 2, y },
                    { x - 2, y - 1 } };
        } else {
            pos = new int[][] { { x - 1, y - 3 }, { x, y - 3 }, { x + 1, y - 3 }, { x + 3, y - 1 }, { x + 3, y },
                    { x + 3, y + 1 }, { x + 1, y + 3 }, { x, y + 3 }, { x - 1, y + 3 }, { x - 3, y + 1 }, { x - 3, y },
                    { x - 3, y - 1 } };
        }

        for (int[] coordinates : pos) {
            System.out.printf("coordinate - x: %d, coordinate - y: %d, cur -x: %d, cur-y: %d \n", coordinates[0],
                    coordinates[1], cur[0], cur[1]);
            if (Arrays.equals(cur, coordinates)) {
                return true;
            }
        }
        return false;
    }

    // private String[] print(Node[] list) {
    //     String[] new_list = new String[list.length];

    //     if (list.length < 1) {
    //         return new String[] {};
    //     }

    //     for (int i = 0; i < list.length; i++) {
    //         int[] pos = list[i].pos;
    //         new_list[i] = Arrays.toString(pos);
    //     }

    //     return new_list;
    // }

    // private Node lowestCost(Node[] list) { // Find the node with the lowest cost in the list
    //     int cost;

    //     if (list.length > 0) {
    //         int l_cost = list[0].cost;
    //         Node l_node = list[0];

    //         for (int i = 0; i < list.length; i++) {
    //             cost = list[i].cost;
    //             if (cost <= l_cost) {
    //                 l_cost = cost;
    //                 l_node = list[i];
    //             }
    //         }

    //         return l_node;
    //     } else {
    //         return null;
    //     }
    // }

    // private Node[] removeNode(Node[] list, Node node) {
    //     int index = -1;

    //     if (list.length < 2) {
    //         return new Node[] {};
    //     }

    //     Node[] new_list = new Node[list.length - 1];

    //     for (int i = 0; i < list.length; i++) {
    //         if (list[i] == node) {
    //             index = i;
    //             break;
    //         }
    //     }

    //     if (index > -1) {
    //         System.arraycopy(list, 0, new_list, 0, index);
    //         for (int j = index; j < new_list.length; j++) {
    //             new_list[j] = list[j + 1];
    //         }
    //     }

    //     return new_list;
    // }

    // private Node[] addNode(Node[] list, Node node) {
    //     Node[] new_list = new Node[list.length + 1];

    //     System.arraycopy(list, 0, new_list, 0, list.length);
    //     new_list[list.length] = node;

    //     return new_list;
    // }

    // private Node[] addNeighbours(Robot robot, Node[] open, Node[] closed, Node cur, int[] end_pos) {
    //     Node[] neighbours = new Node[4];
    //     int count = 0;
    //     int x = cur.pos[0];
    //     int y = cur.pos[1];
    //     int[][] neighbours_pos = { { x, y + 1 }, { x - 1, y }, { x + 1, y }, { x, y - 1 } };

    //     for (int i = 0; i < 4; i++) {
    //         if (isValid(robot, neighbours_pos[i])) {
    //             // add neighbours (must be a valid grid to move to)
    //             Node neighbour = new Node(neighbours_pos[i]);
    //             neighbour.parent = cur;
    //             neighbour.cost = findCost(neighbour, end_pos, robot);
    //             neighbours[count] = neighbour;
    //             count++;
    //         }
    //     }

    //     for (int j = 0; j < count; j++) {
    //         Node node = neighbours[j];
    //         // if (find_index(node, closed) == -1) { // if not in closed
    //         // int index = find_index(node, open);
    //         // if ((index > -1) && (node.cost < open[index].cost)) {
    //         // open[index] = node;
    //         // }
    //         // if (index == -1) { // if not in open
    //         // open = add_node(open, node);
    //         // }
    //         // }
    //         open = addNode(open, node);
    //     }

    //     return open;
    // }

    private Node[] getNeighbours(Robot robot, Node cur, int[] end_pos) {
        Node[] neighbours = new Node[4];
        int count = 0;
        int x = cur.pos[0];
        int y = cur.pos[1];
        int[][] neighbours_pos = { { x, y + 1 }, { x - 1, y }, { x + 1, y }, { x, y - 1 } };

        for (int i = 0; i < 4; i++) {
            if (isValid(robot, neighbours_pos[i])) {
                // add neighbours (must be a valid grid to move to)
                Node neighbour = new Node(neighbours_pos[i]);
                neighbours[count] = neighbour;
                count++;
            }
        }
        return neighbours;
    }

    public boolean isValid(Robot robot, int[] pos) {
        if (pos == null) {
            return false;
        }

        Map map = robot.getMap();
        int x = pos[0];
        int y = pos[1];
        int[][] robotPos = { { x - 1, y + 1 }, { x, y + 1 }, { x + 1, y + 1 }, { x - 1, y }, { x, y }, { x + 1, y },
                { x - 1, y - 1 }, { x, y - 1 }, { x + 1, y - 1 } };

        if ((x > 0) && (x < Constant.BOARDWIDTH - 1) && (y > 0) && (y < Constant.BOARDHEIGHT - 1)) {
            for (int[] coordinates : robotPos) {
                if (map.getGrid(coordinates[0], coordinates[1]).equals(Constant.OBSTACLE)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    // private int findCost(Node node, int[] end_pos, Robot robot) {
    //     node.h_cost = calculateHCost(node, end_pos);
    //     node.g_cost = calculateGCost(node);
    //     node.updateCost();
    //     return node.cost;
    // }

    private void calculateCosts(Node node, int[] end_pos, Robot robot) {
        node.h_cost = calculateHCost(node, end_pos);
        node.g_cost = calculateGCost(node);
        node.updateCost();
    }

    private int calculateHCost(Node cur, int[] end) { // distance from the end
        int x = Math.abs(cur.pos[0] - end[0]);
        int y = Math.abs(cur.pos[1] - end[1]);

        if ((cur.parent.pos[0] == cur.pos[0]) && (x == 0)) {
            return y;
        } else if ((cur.parent.pos[1] == cur.pos[1]) && (y == 0)) {
            return x;
        } else {
            return (x + y);
        }
    }

    private int calculateGCost(Node cur) { // penalty for turning
        Node prev = cur.parent;

        if (prev == null) {
            // cur node is the start
            return 0;
        } else if ((!first_penalty) && (prev.parent == null)) {
            return prev.g_cost + 1;
        } else {
            int direction = directionToGo(cur);

            if (direction == Constant.FORWARD) {
                return prev.g_cost + 1;
            } else if ((direction == Constant.LEFT) || (direction == Constant.RIGHT)) {
                return prev.g_cost + 3;
            } else {
                return prev.g_cost + 5;
            }
        }
    }

    private int directionToGo(Node cur) {
        Node second = cur.parent;
        if (second == null) {
            // start
            return -1;
        }
        Node first = second.parent;
        if (first == null) { // This is only 1 grid away from the start
            if (second.pos[0] == cur.pos[0]) {
                if (second.pos[1] > cur.pos[1]) {
                    if (direction == Constant.NORTH) {
                        return Constant.FORWARD;
                    } else if (direction == Constant.EAST) {
                        return Constant.LEFT;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.BACKWARD;
                    } else if (direction == Constant.WEST) {
                        return Constant.RIGHT;
                    }
                } else if (second.pos[1] < cur.pos[1]) {
                    if (direction == Constant.NORTH) {
                        return Constant.BACKWARD;
                    } else if (direction == Constant.EAST) {
                        return Constant.RIGHT;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.FORWARD;
                    } else if (direction == Constant.WEST) {
                        return Constant.LEFT;
                    }
                }
            } else if (second.pos[1] == cur.pos[1]) {
                if (second.pos[0] > cur.pos[0]) {
                    if (direction == Constant.NORTH) {
                        return Constant.LEFT;
                    } else if (direction == Constant.EAST) {
                        return Constant.BACKWARD;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.RIGHT;
                    } else if (direction == Constant.WEST) {
                        return Constant.FORWARD;
                    }
                } else if (second.pos[0] < cur.pos[0]) {
                    if (direction == Constant.NORTH) {
                        return Constant.RIGHT;
                    } else if (direction == Constant.EAST) {
                        return Constant.FORWARD;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.LEFT;
                    } else if (direction == Constant.WEST) {
                        return Constant.BACKWARD;
                    }
                }
            }
        } else { // this is to cater to backward
            if ((first.pos[0] == second.pos[0]) && (second.pos[0] == cur.pos[0])) {
                if (((first.pos[1] > second.pos[1]) && (second.pos[1] > cur.pos[1]))
                        || ((first.pos[1] < second.pos[1]) && (second.pos[1] < cur.pos[1]))) {
                    return Constant.FORWARD;
                } else {
                    return Constant.BACKWARD;
                }
            } else if ((first.pos[1] == second.pos[1]) && (second.pos[1] == cur.pos[1])) {
                if (((first.pos[0] > second.pos[0]) && (second.pos[0] > cur.pos[0]))
                        || ((first.pos[0] < second.pos[0]) && (second.pos[0] < cur.pos[0]))) {
                    return Constant.FORWARD;
                } else {
                    return Constant.BACKWARD;
                }
            } else if (first.pos[0] == second.pos[0]) {
                if (first.pos[1] < second.pos[1]) {
                    if (second.pos[0] < cur.pos[0]) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                } else {
                    if (second.pos[0] > cur.pos[0]) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                }
            } else {
                if (first.pos[0] < second.pos[0]) {
                    if (second.pos[1] > cur.pos[1]) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                } else {
                    if (second.pos[1] < cur.pos[1]) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                }
            }
        }
        return -2; // error
    }

    // private int find_index(Node node, Node[] list) {
    //     if (list.length > 0) {
    //         for (int i = 0; i < list.length; i++) {
    //             if (Arrays.equals(list[i].pos, node.pos)) {
    //                 return i;
    //             }
    //         }
    //     }
    //     return -1;
    // }

    private int[] reversePath(Node node) {
        int[] path = { directionToGo(node) };
        Node cur = node.parent;

        if (cur == null) {
            return null;
        }

        while (cur.parent != null) {
            // System.out.printf("reverse Path cur: x: %d, y: %d \n", cur.pos[0],
            // cur.pos[1]);
            // System.out.printf("reverse Path cur PARENT: x: %d, y: %d \n",
            // cur.parent.pos[0], cur.parent.pos[1]);
            int direction = directionToGo(cur);
            if (direction >= 0) {
                int[] temp_path = new int[path.length + 1];
                System.arraycopy(path, 0, temp_path, 1, path.length);
                temp_path[0] = direction;
                path = temp_path;
                cur = cur.parent;
            }
        }

        return path;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setFirstTurnPenalty(boolean first_penalty) {
        this.first_penalty = first_penalty;
    }
}