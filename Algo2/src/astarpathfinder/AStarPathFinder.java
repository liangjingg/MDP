package astarpathfinder;

import robot.Robot;
import map.Map;
import config.Constant;
import datastruct.Coordinate;
import datastruct.Node;

import java.util.PriorityQueue;

import java.util.Arrays;

public class AStarPathFinder {
    boolean first = true;
    int direction = -1;
    int initialDirection = -1;
    boolean firstPenalty = true;
    Node[][] nodeDetails;

    public int[] AStarPathFinderAlgo(Robot robot, Coordinate startPos, Coordinate endPos, boolean onGrid) {
        initialDirection = robot.getDirection();
        Node cur = null;
        // matrix indicating whether is on open list or not
        boolean[][] openList = new boolean[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
        // matrix indicating whether is on closed list or not
        boolean[][] closedList = new boolean[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];
        // Contains all the details of the node on the board -> parent, cost etc
        nodeDetails = new Node[Constant.BOARDWIDTH][Constant.BOARDHEIGHT];

        for (int i = 0; i < Constant.BOARDWIDTH; i++) {
            for (int j = 0; j < Constant.BOARDHEIGHT; j++) {
                openList[i][j] = false;
                closedList[i][j] = false;
                Coordinate pos = new Coordinate(i, j);
                nodeDetails[i][j] = new Node(pos);
                nodeDetails[i][j].cost = Constant.MAXFCOST;
                nodeDetails[i][j].g_cost = Constant.MAXFCOST;
                nodeDetails[i][j].h_cost = Constant.MAXFCOST;
                nodeDetails[i][j].parent = null;
            }
        }
        int startPosX = startPos.x;
        int startPosY = startPos.y;
        nodeDetails[startPosX][startPosY].g_cost = 0;
        nodeDetails[startPosX][startPosY].h_cost = 0;
        nodeDetails[startPosX][startPosY].cost = 0;

        Node startNode = nodeDetails[startPosX][startPosY];

        PriorityQueue<Node> openQueue = new PriorityQueue<Node>();
        openQueue.add(startNode);
        boolean pathFound = false;

        while (!openQueue.isEmpty()) {
            cur = openQueue.poll();
            if (((!onGrid) && (canReach(cur.pos, endPos, first))) || ((onGrid) && (cur.pos.equals(endPos)))) {
                pathFound = true;
                break;
            }
            closedList[cur.pos.x][cur.pos.y] = true;
            Node[] neighbours = getNeighbours(robot, cur); // all the neighbours that are valid
            for (Node neighbour : neighbours) {
                if (neighbour != null) {
                    neighbour.parent = cur;
                    calculateCosts(neighbour, endPos, robot);
                    if (closedList[neighbour.pos.x][neighbour.pos.y])
                        continue;
                    if (!openList[neighbour.pos.x][neighbour.pos.y]
                            || neighbour.cost < nodeDetails[neighbour.pos.x][neighbour.pos.y].cost) { // shd update if
                                                                                                      // h_cost
                                                                                                      // lower??
                        openList[neighbour.pos.x][neighbour.pos.y] = true;
                        nodeDetails[neighbour.pos.x][neighbour.pos.y] = neighbour;
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

    private boolean canReach(Coordinate cur, Coordinate endPos, boolean first) {
        int x = endPos.x;
        int y = endPos.y;
        int[][] pos;
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
            if (cur.x == coordinates[0] && cur.y == coordinates[1]) {
                return true;
            }
        }
        return false;
    }

    private Node[] getNeighbours(Robot robot, Node cur) {
        Node[] neighbours = new Node[4];
        int count = 0;
        int x = cur.pos.x;
        int y = cur.pos.y;
        int[][] neighboursPos = { { x, y + 1 }, { x - 1, y }, { x + 1, y }, { x, y - 1 } };

        for (int i = 0; i < 4; i++) {
            if (isValid(robot, neighboursPos[i][0], neighboursPos[i][1])) {
                // add neighbours (must be a valid grid to move to)
                Node neighbour = new Node(new Coordinate(neighboursPos[i][0], neighboursPos[i][1]));
                neighbours[count] = neighbour;
                count++;
            }
        }
        return neighbours;
    }

    public boolean isValid(Robot robot, int posX, int posY) {
        Map map = robot.getMap();
        int x = posX;
        int y = posY;
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

    private void calculateCosts(Node node, Coordinate endPos, Robot robot) {
        node.h_cost = calculateHCost(node, endPos);
        node.g_cost = calculateGCost(node);
        node.updateCost();
    }

    private int calculateHCost(Node cur, Coordinate endPos) { // distance from the end
        int x = Math.abs(cur.pos.x - endPos.x);
        int y = Math.abs(cur.pos.y - endPos.y);

        if ((cur.parent.pos.x == cur.pos.y) && (x == 0)) {
            return y;
        } else if ((cur.parent.pos.x == cur.pos.y) && (y == 0)) {
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
        } else if ((!firstPenalty) && (prev.parent == null)) {
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
            if (second.pos.x == cur.pos.x) {
                if (second.pos.y > cur.pos.y) {
                    if (direction == Constant.NORTH) {
                        return Constant.FORWARD;
                    } else if (direction == Constant.EAST) {
                        return Constant.LEFT;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.BACKWARD;
                    } else if (direction == Constant.WEST) {
                        return Constant.RIGHT;
                    }
                } else if (second.pos.y < cur.pos.y) {
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
            } else if (second.pos.y == cur.pos.y) {
                if (second.pos.x > cur.pos.x) {
                    if (direction == Constant.NORTH) {
                        return Constant.LEFT;
                    } else if (direction == Constant.EAST) {
                        return Constant.BACKWARD;
                    } else if (direction == Constant.SOUTH) {
                        return Constant.RIGHT;
                    } else if (direction == Constant.WEST) {
                        return Constant.FORWARD;
                    }
                } else if (second.pos.x < cur.pos.x) {
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
            if ((first.pos.x == second.pos.x) && (second.pos.x == cur.pos.x)) {
                if (((first.pos.y > second.pos.y) && (second.pos.y > cur.pos.y))
                        || ((first.pos.y < second.pos.y) && (second.pos.y < cur.pos.y))) {
                    return Constant.FORWARD;
                } else {
                    return Constant.BACKWARD;
                }
            } else if ((first.pos.y == second.pos.y) && (second.pos.y == cur.pos.y)) {
                if (((first.pos.x > second.pos.x) && (second.pos.x > cur.pos.x))
                        || ((first.pos.x < second.pos.x) && (second.pos.x < cur.pos.x))) {
                    return Constant.FORWARD;
                } else {
                    return Constant.BACKWARD;
                }
            } else if (first.pos.x == second.pos.x) {
                if (first.pos.y < second.pos.y) {
                    if (second.pos.x < cur.pos.x) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                } else {
                    if (second.pos.x > cur.pos.x) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                }
            } else {
                if (first.pos.x < second.pos.x) {
                    if (second.pos.y > cur.pos.y) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                } else {
                    if (second.pos.y < cur.pos.y) {
                        return Constant.LEFT;
                    } else {
                        return Constant.RIGHT;
                    }
                }
            }
        }
        return -2; // error
    }

    private int[] reversePath(Node node) {
        int[] path = { directionToGo(node) };
        Node cur = node.parent;

        if (cur == null) {
            return null;
        }

        while (cur.parent != null) {
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
        this.firstPenalty = first_penalty;
    }
}