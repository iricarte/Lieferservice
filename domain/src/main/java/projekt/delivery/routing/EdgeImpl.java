package projekt.delivery.routing;

import projekt.base.Location;

import java.util.Comparator;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a weighted edge in a graph.
 */
@SuppressWarnings("ClassCanBeRecord")
class EdgeImpl implements Region.Edge {

    private final Region region;
    private final String name;
    private final Location locationA;
    private final Location locationB;
    private final long duration;

    /**
     * Creates a new {@link EdgeImpl} instance.
     * @param region The {@link Region} this {@link EdgeImpl} belongs to.
     * @param name The name of this {@link EdgeImpl}.
     * @param locationA The start of this {@link EdgeImpl}.
     * @param locationB The end of this {@link EdgeImpl}.
     * @param duration The length of this {@link EdgeImpl}.
     */
    EdgeImpl(
        Region region,
        String name,
        Location locationA,
        Location locationB,
        long duration
    ) {
        this.region = region;
        this.name = name;
        // locations must be in ascending order
        if (locationA.compareTo(locationB) > 0) {
            throw new IllegalArgumentException(String.format("locationA %s must be <= locationB %s", locationA, locationB));
        }
        this.locationA = locationA;
        this.locationB = locationB;
        this.duration = duration;
    }

    /**
     * Returns the start of this {@link EdgeImpl}.
     * @return The start of this {@link EdgeImpl}.
     */
    public Location getLocationA() {
        return locationA;
    }

    /**
     * Returns the end of this {@link EdgeImpl}.
     * @return The end of this {@link EdgeImpl}.
     */
    public Location getLocationB() {
        return locationB;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public Region.Node getNodeA() {
        return this.region.getNode(locationA);
    }

    @Override
    public Region.Node getNodeB() {
        return this.region.getNode(locationB);
    }

    @Override
    public int compareTo(Region.@NotNull Edge o) {
        Comparator<Region.Edge> cmpA = Comparator.comparing(Region.Edge::getNodeA);
        Comparator<Region.Edge> cmpB = Comparator.comparing(Region.Edge::getNodeB);
        return cmpA.thenComparing(cmpB).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EdgeImpl edge = (EdgeImpl) o;
        return duration == edge.duration && name.equals(edge.name) && locationA.equals(edge.locationA) && locationB.equals(edge.locationB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, locationA, locationB, duration);
    }

    @Override
    public String toString() {
        return "EdgeImpl(name='" + name + "', " +
            "locationA='" + locationA + "', " +
            "locationB='" + locationB + "', " +
            "duration='" + duration + "')";
    }
}
