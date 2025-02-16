package projekt.delivery.routing;

import projekt.base.Location;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

class NodeImpl implements Region.Node {

    protected final Set<Location> connections;
    protected final Region region;
    protected final String name;
    protected final Location location;

    /**
     * Creates a new {@link NodeImpl} instance.
     *
     * @param region      The {@link Region} this {@link NodeImpl} belongs to.
     * @param name        The name of this {@link NodeImpl}.
     * @param location    The {@link Location} of this {@link EdgeImpl}.
     * @param connections All {@link Location}s this {@link NeighborhoodImpl} has an
     *                    {@link Region.Edge} to.
     */
    NodeImpl(Region region, String name, Location location, Set<Location> connections) {
        this.region = region;
        this.name = name;
        this.location = location;
        this.connections = connections;
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
    public Location getLocation() {
        return location;
    }

    public Set<Location> getConnections() {
        return connections;
    }

    @Override
    public @Nullable Region.Edge getEdge(Region.Node other) {
        return this.region.getEdge(this, other);
    }

    @Override
    public Set<Region.Node> getAdjacentNodes() {
        return this.connections.stream().map(region::getNode).collect(Collectors.toSet());
    }

    @Override
    public Set<Region.Edge> getAdjacentEdges() {
        return this.connections.stream().map(l -> this.region.getEdge(this.location, l)).collect(Collectors.toSet());
    }

    @Override
    public int compareTo(Region.Node o) {
        return this.location.compareTo(o.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, connections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeImpl node = (NodeImpl) o;
        return Objects.equals(name, node.name) && Objects.equals(location, node.location) && Objects.equals(connections,
                                                                                                            node.connections);
    }

    @Override
    public String toString() {
        return "NodeImpl(name='" + name + "', " + "location='" + location + "', " + "connections='" + connections
               + "')";
    }
}
