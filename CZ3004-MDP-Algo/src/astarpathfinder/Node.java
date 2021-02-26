package astarpathfinder;

public class Node implements Comparable<Node> {
    public int[] pos;
    public Node parent = null;
    public int g_cost = 0;
    public int h_cost = 0;
    public int cost = g_cost + h_cost;

    public Node(int[] pos) {
        this.pos = pos;
    }

    public void updateCost() {
        this.cost = this.h_cost + this.g_cost;
    }

    @Override
    public int compareTo(Node o) {
        if (this.cost < o.cost)
            return -1;
        else if (this.cost > o.cost)
            return 1;
        else {
            if (h_cost < o.h_cost)
                return -1;
            if (h_cost > o.h_cost)
                return 1;
            else
                return 0;
        }
    }
}
