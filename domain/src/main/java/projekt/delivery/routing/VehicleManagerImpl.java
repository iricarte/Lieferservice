package projekt.delivery.routing;

import projekt.base.Location;
import projekt.delivery.event.Event;
import projekt.delivery.event.EventBus;
import projekt.delivery.event.SpawnEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

class VehicleManagerImpl implements VehicleManager {

    final Map<Region.Node, OccupiedNodeImpl<? extends Region.Node>> occupiedNodes;
    final Map<Region.Edge, OccupiedEdgeImpl> occupiedEdges;
    private final Region region;
    private final PathCalculator pathCalculator;
    private final List<VehicleImpl> vehiclesToSpawn = new ArrayList<>();
    private final List<VehicleImpl> vehicles = new ArrayList<>();
    private final Collection<Vehicle> unmodifiableVehicles = Collections.unmodifiableCollection(vehicles);
    private final EventBus eventBus = new EventBus();

    VehicleManagerImpl(Region region, PathCalculator pathCalculator) {
        this.region = region;
        this.pathCalculator = pathCalculator;
        occupiedNodes = toOccupiedNodes(region.getNodes());
        occupiedEdges = toOccupiedEdges(region.getEdges());
    }

    private Map<Region.Node, OccupiedNodeImpl<? extends Region.Node>> toOccupiedNodes(Collection<Region.Node> nodes) {
        Map<Region.Node, OccupiedNodeImpl<? extends Region.Node>> retVal = new HashMap<>();
        for (Region.Node node : nodes) {
            retVal.put(node, this.getOccupiedNode(node));
        }
        return Collections.unmodifiableMap(retVal);
    }

    @NotNull
    private OccupiedNodeImpl<? extends Region.Node> getOccupiedNode(Region.Node node) {
        if (node instanceof Region.Restaurant restaurant) {
            return new OccupiedRestaurantImpl(restaurant, this);
        } else if (node instanceof Region.Neighborhood neighborhood) {
            return new OccupiedNeighborhoodImpl(neighborhood, this);
        } else {
            return new OccupiedNodeImpl<>(node, this);
        }
    }

    private Map<Region.Edge, OccupiedEdgeImpl> toOccupiedEdges(Collection<Region.Edge> edges) {
        Map<Region.Edge, OccupiedEdgeImpl> retVal = new HashMap<>();
        for (Region.Edge edge : edges) {
            retVal.put(edge, new OccupiedEdgeImpl(edge, this));
        }
        return Collections.unmodifiableMap(retVal);
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public PathCalculator getPathCalculator() {
        return pathCalculator;
    }

    @Override
    public List<OccupiedRestaurant> getOccupiedRestaurants() {
        return occupiedNodes.values()
                            .stream()
                            .filter(OccupiedRestaurant.class::isInstance)
                            .map(OccupiedRestaurant.class::cast)
                            .toList();
    }

    @Override
    public OccupiedRestaurant getOccupiedRestaurant(Region.Node node) {
        if (node == null) {
            throw new NullPointerException("Node is null!");
        }
        if (occupiedNodes.get(node) instanceof OccupiedRestaurant restaurant) {
            return restaurant;
        } else {
            throw new IllegalArgumentException("Node " + node + " is not a restaurant");
        }
    }

    @Override
    public <C extends Region.Component<C>> AbstractOccupied<C> getOccupied(C component) {
        if (component == null) {
            throw new NullPointerException("Component is null!");
        }
        if (component instanceof Region.Node node) {
            return this.getAbstractOccupiedNode(node);
        } else if (component instanceof Region.Edge edge) {
            return this.getAbstractOccupiedEdge(edge);
        } else {
            throw new IllegalArgumentException(
                    "Component is not of recognized subtype: " + component.getClass().getName());
        }
    }

    @NotNull
    @SuppressWarnings("unchecked") //upcast, should be fine
    private <C extends Region.Component<C>> AbstractOccupied<C> getAbstractOccupiedEdge(Region.Edge component) {
        OccupiedEdgeImpl occupiedEdge = this.occupiedEdges.get(component);
        if (occupiedEdge == null) {
            throw new IllegalArgumentException("Could not find occupied edge for " + component);
        }
        return (AbstractOccupied<C>) occupiedEdge;
    }

    @NotNull
    @SuppressWarnings("unchecked") //upcast, should be fine
    private <C extends Region.Component<C>> AbstractOccupied<C> getAbstractOccupiedNode(Region.Node component) {
        OccupiedNodeImpl<? extends Region.Node> occupiedNode = this.occupiedNodes.get(component);
        if (occupiedNode == null) {
            throw new IllegalArgumentException("Could not find occupied node for " + component);
        }
        return (AbstractOccupied<C>) occupiedNode;
    }

    @Override
    public Collection<OccupiedNeighborhood> getOccupiedNeighborhoods() {
        return occupiedNodes.values()
                            .stream()
                            .filter(OccupiedNeighborhood.class::isInstance)
                            .map(OccupiedNeighborhood.class::cast)
                            .toList();
    }

    @Override
    public OccupiedNeighborhood getOccupiedNeighborhood(Region.Node node) {
        if (node == null) {
            throw new NullPointerException("Node is null!");
        }
        if (occupiedNodes.get(node) instanceof OccupiedNeighborhood neighborhood) {
            return neighborhood;
        } else {
            throw new IllegalArgumentException("Node " + node + " is not a neighborhood");
        }
    }

    @Override
    public Collection<Occupied<? extends Region.Node>> getOccupiedNodes() {
        return Collections.unmodifiableCollection(occupiedNodes.values());
    }

    @Override
    public Collection<Occupied<? extends Region.Edge>> getOccupiedEdges() {
        return Collections.unmodifiableCollection(occupiedEdges.values());
    }

    @Override
    public List<Event> tick(long currentTick) {
        for (VehicleImpl vehicle : vehiclesToSpawn) {
            spawnVehicle(vehicle, currentTick);
        }
        vehiclesToSpawn.clear();
        // It is important that nodes are ticked before edges
        // This only works because edge ticking is idempotent
        // Otherwise, there may be two state changes in a single tick.
        // For example, a node tick may move a vehicle onto an edge.
        // Ticking this edge afterwards does not move the vehicle further along the edge
        // compared to a vehicle already on the edge.
        occupiedNodes.values().forEach(occupiedNode -> occupiedNode.tick(currentTick));
        occupiedEdges.values().forEach(occupiedEdge -> occupiedEdge.tick(currentTick));
        return eventBus.popEvents(currentTick);
    }

    private void spawnVehicle(VehicleImpl vehicle, long currentTick) {
        vehicles.add(vehicle);
        OccupiedRestaurantImpl warehouse = (OccupiedRestaurantImpl) vehicle.getOccupied();
        warehouse.vehicles.put(vehicle, new AbstractOccupied.VehicleStats(currentTick, null));
        getEventBus().queuePost(SpawnEvent.of(currentTick, vehicle, warehouse.getComponent()));
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    public void reset() {
        for (AbstractOccupied<?> occupied : getAllOccupied()) {
            occupied.reset();
        }

        for (Vehicle vehicle : getAllVehicles()) {
            vehicle.reset();
        }

        vehiclesToSpawn.addAll(getVehicles().stream().map(VehicleImpl.class::cast).toList());

        vehicles.clear();
    }

    private Set<AbstractOccupied<?>> getAllOccupied() {
        Set<AbstractOccupied<?>> retVal = new HashSet<>();
        retVal.addAll(this.occupiedNodes.values());
        retVal.addAll(this.occupiedEdges.values());
        return Collections.unmodifiableSet(retVal);
    }

    @Override
    public Collection<Vehicle> getAllVehicles() {
        Collection<Vehicle> allVehicles = new ArrayList<>(getVehicles());
        allVehicles.addAll(vehiclesToSpawn);
        return allVehicles;
    }

    @Override
    public Collection<Vehicle> getVehicles() {
        return unmodifiableVehicles;
    }

    @SuppressWarnings("UnusedReturnValue")
    Vehicle addVehicle(Location startingLocation, double capacity) {
        OccupiedNodeImpl<? extends Region.Node> occupied = getOccupiedNode(startingLocation);

        if (!(occupied instanceof OccupiedRestaurant)) {
            throw new IllegalArgumentException("Vehicles can only spawn at restaurants!");
        }

        final VehicleImpl vehicle = new VehicleImpl(vehicles.size() + vehiclesToSpawn.size(),
                                                    capacity,
                                                    this,
                                                    (OccupiedRestaurant) occupied);
        vehiclesToSpawn.add(vehicle);
        vehicle.setOccupied(occupied);
        return vehicle;
    }

    private OccupiedNodeImpl<? extends Region.Node> getOccupiedNode(Location location) {
        return occupiedNodes.values()
                            .stream()
                            .filter(node -> node.getComponent().getLocation().equals(location))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Could not find node with given predicate"));
    }
}
