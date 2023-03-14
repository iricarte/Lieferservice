package projekt.base;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

/**
 * A tuple for the x- and y-coordinates of a point.
 */
public final class Location implements Comparable<Location> {

    private final static Comparator<Location> COMPARATOR = Comparator.comparing(Location::getX)
                                                                     .thenComparing(Location::getY);

    private final int x;
    private final int y;

    /**
     * Instantiates a new {@link Location} object using {@code x} and {@code y} as coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of this location.
     *
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of this location.
     *
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Adds the coordinates of this location and the other location and returns a new
     * {@link Location} object with the resulting coordinates.
     *
     * @param other the other {@link Location} object to get the second set of coordinates from
     * @return a new {@link Location} object with the sum of coordinates from both locations
     */
    public Location add(Location other) {
        return new Location(x + other.x, y + other.y);
    }

    /**
     * Subtracts the coordinates of this location from the other location and returns a new
     * {@link Location} object with the resulting coordinates.
     *
     * @param other the other {@link Location} object to get the second set of coordinates from
     * @return a new {@link Location} object with the difference of coordinates from both locations
     */
    public Location subtract(Location other) {
        return new Location(x - other.x, y - other.y);
    }

    @Override
    public int compareTo(@NotNull Location o) {
        if (o.x == this.x && o.y == this.y) {
            return 0;
        } else if (o.x < this.x || (o.x == this.x && o.y < this.y)) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (Math.signum(x) == -1) {
            hashCode += 1000000000;
        }
        if (Math.signum(y) == -1) {
            hashCode += 10000;
        }
        hashCode += Math.abs(x) * 100000;
        hashCode += Math.abs(y);
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return x == location.x && y == location.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }
}
