package datastruct;

public class Coordinate {
    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        if (other == this) {
            return true;
        }

        return (other.x == this.x) && (other.y == this.y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }
}
