package datastruct;

import java.util.Objects;

public class Obstacle {
    public final Coordinate coordinates;
    public final int direction;

    public Obstacle(Coordinate coordinates, int direction) {
        this.coordinates = coordinates;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Obstacle)) {
            return false;
        }
        Obstacle other = (Obstacle) obj;
        if (other == this) {
            return true;
        }
        return (other.coordinates.equals(this.coordinates)) && (other.direction == this.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates.x, coordinates.y, direction);
    }

    @Override
    public String toString() {
        return "x: " + this.coordinates.x + ", y:" + this.coordinates.y + ", direction: " + this.direction;
    }
}
