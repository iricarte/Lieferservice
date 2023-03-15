package projekt.delivery.routing;

import projekt.base.DistanceCalculator;
import projekt.base.EuclideanDistanceCalculator;
import projekt.base.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

class RegionImpl implements Region {

    private final Map<Location, NodeImpl> nodes = new HashMap<>();
    private final Map<Location, Map<Location, EdgeImpl>> edges = new HashMap<>();
    private final List<EdgeImpl> allEdges = new ArrayList<>();
    private final DistanceCalculator distanceCalculator;

    /**
     * Creates a new, empty {@link RegionImpl} instance using a {@link EuclideanDistanceCalculator}.
     */
    public RegionImpl() {
        this(new EuclideanDistanceCalculator());
    }

    /**
     * Creates a new, empty {@link RegionImpl} instance using the given {@link DistanceCalculator}.
     */
    public RegionImpl(DistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public @Nullable Node getNode(Location location) {
        return this.nodes.get(location);
    }

    @Override
    public @Nullable Edge getEdge(Location locationA, Location locationB) {
        EdgeImpl edge = this.getEdgeHelper(locationA, locationB);
        if (edge == null) {
            return this.getEdgeHelper(locationB, locationA);
        }
        return edge;
    }

    @Nullable
    private EdgeImpl getEdgeHelper(Location locationA, Location locationB) {
        Map<Location, EdgeImpl> locationEdges = this.edges.get(locationA);
        if (locationEdges != null) {
            return locationEdges.get(locationB);
        }
        return null;
    }

    @Override
    public Collection<Node> getNodes() {
        return this.nodes.values().stream().map(node -> (Node) node).toList();
    }

    @Override
    public Collection<Edge> getEdges() {
        return this.allEdges.stream().map(edge -> (Edge) edge).toList();
    }

    @Override
    public DistanceCalculator getDistanceCalculator() {
        return distanceCalculator;
    }

    /**
     * Adds the given {@link NodeImpl} to this {@link RegionImpl}.
     *
     * @param node the {@link NodeImpl} to add.
     */
    void putNode(NodeImpl node) {
        if (this.equals(node.region)) {
            this.nodes.put(node.getLocation(), node);
        } else {
            throw new IllegalArgumentException("Node " + node + " has incorrect region");
        }
    }

    /**
     * Adds the given {@link EdgeImpl} to this {@link RegionImpl}.
     *
     * @param edge the {@link EdgeImpl} to add.
     */
    void putEdge(EdgeImpl edge) {
        if (!this.equals(edge.getRegion())) {
            throw new IllegalArgumentException("Edge " + edge + " has incorrect region");
        }
        if (edge.getNodeA() == null || !this.equals(edge.getNodeA().getRegion())) {
            throw new IllegalArgumentException("NodeA " + edge.getLocationA() + " is not part of the region");
        }
        if (edge.getNodeB() == null || !this.equals(edge.getNodeB().getRegion())) {
            throw new IllegalArgumentException("NodeB " + edge.getLocationB() + " is not part of the region");
        }
        Map<Location, EdgeImpl> existingEdge = this.edges.get(edge.getNodeA().getLocation());
        if (existingEdge == null) {
            existingEdge = new HashMap<>();
        }
        existingEdge.put(edge.getLocationB(), edge);
        this.edges.put(edge.getNodeA().getLocation(), existingEdge);
        this.allEdges.add(edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegionImpl region = (RegionImpl) o;
        return Objects.equals(nodes, region.nodes) && Objects.equals(edges, region.edges);
    }
}
