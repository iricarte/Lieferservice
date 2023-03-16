package projekt.delivery.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

class VehicleImpl implements Vehicle {

    private final int id;
    private final double capacity;
    private final List<ConfirmedOrder> orders = new ArrayList<>();
    private final VehicleManagerImpl vehicleManager;
    private final Deque<PathImpl> moveQueue = new LinkedList<>();
    private final VehicleManager.OccupiedRestaurant startingNode;
    private AbstractOccupied<?> occupied;

    public VehicleImpl(int id,
                       double capacity,
                       VehicleManagerImpl vehicleManager,
                       VehicleManager.OccupiedRestaurant startingNode) {
        this.id = id;
        this.capacity = capacity;
        this.occupied = (AbstractOccupied<?>) startingNode;
        this.vehicleManager = vehicleManager;
        this.startingNode = startingNode;
    }

    @Override
    public VehicleManager.Occupied<?> getOccupied() {
        return occupied;
    }

    void setOccupied(AbstractOccupied<?> occupied) {
        this.occupied = occupied;
    }

    @Override
    public List<? extends Path> getPaths() {
        return new LinkedList<>(moveQueue);
    }

    @Override
    public void moveDirect(Region.Node node, BiConsumer<? super Vehicle, Long> arrivalAction) {
        this.checkMoveToNode(node);
        this.moveQueue.clear();
        this.moveFromEdge(arrivalAction);
        this.moveQueued(node, arrivalAction);
    }

    @Override
    public void moveQueued(Region.Node node, BiConsumer<? super Vehicle, Long> arrivalAction) {
        this.checkMoveToNode(node);
        Region.Node startingNode = this.calculateStartingNode();
        Deque<Region.Node> path = this.calculatePath(startingNode, node);
        moveQueue.add(new PathImpl(path, arrivalAction));
    }

    private void moveFromEdge(BiConsumer<? super Vehicle, Long> arrivalAction) {
        if (occupied instanceof OccupiedEdgeImpl currentEdge && this.getPreviousOccupied()
                                                                    .getComponent() instanceof Region.Node previousNode) {
            Deque<Region.Node> path = this.getPathToNextNode(currentEdge, previousNode);
            moveQueue.add(new PathImpl(path, arrivalAction));
        }
    }

    private void checkMoveToNode(Region.Node node) {
        if (occupied.component.equals(node) && moveQueue.isEmpty()) {
            throw new IllegalArgumentException("Vehicle " + getId() + " cannot move to own node " + node);
        }
    }

    private Region.Node calculateStartingNode() {
        return moveQueue.isEmpty()
               || moveQueue.getLast().nodes.isEmpty() ? this.startingNode.getComponent() : moveQueue.getLast().nodes.getLast();
    }

    @Override
    public @Nullable VehicleManager.Occupied<?> getPreviousOccupied() {
        AbstractOccupied.VehicleStats stats = occupied.vehicles.get(this);
        return stats == null ? null : stats.previous;
    }

    private Deque<Region.Node> getPathToNextNode(OccupiedEdgeImpl currentEdge, Region.Node previousNode) {
        if (previousNode.equals(currentEdge.getComponent().getNodeA())) {
            return this.calculatePath(previousNode, currentEdge.getComponent().getNodeB());
        } else {
            return this.calculatePath(previousNode, currentEdge.getComponent().getNodeA());
        }
    }

    @Override
    public int getId() {
        return id;
    }

    private Deque<Region.Node> calculatePath(Region.Node startingNode, Region.Node node) {
        return vehicleManager.getPathCalculator().getPath(startingNode, node);
    }

    @Override
    public double getCapacity() {
        return capacity;
    }

    @Override
    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }

    @Override
    public VehicleManager.Occupied<? extends Region.Node> getStartingNode() {
        return startingNode;
    }

    @Override
    public Collection<ConfirmedOrder> getOrders() {
        return orders;
    }

    @Override
    public void reset() {
        occupied = (AbstractOccupied<?>) startingNode;
        moveQueue.clear();
        orders.clear();
    }

    void move(long currentTick) {
        final Region region = vehicleManager.getRegion();
        if (moveQueue.isEmpty()) {
            return;
        }
        final PathImpl path = moveQueue.peek();
        if (path.nodes().isEmpty()) {
            moveQueue.pop();
            final @Nullable BiConsumer<? super Vehicle, Long> action = path.arrivalAction();
            if (action == null) {
                move(currentTick);
            } else {
                action.accept(this, currentTick);
            }
        } else {
            Region.Node next = path.nodes().peek();
            if (occupied instanceof OccupiedNodeImpl<?> occupiedNode) {
                vehicleManager.getOccupied(region.getEdge(occupiedNode.getComponent(), next))
                              .addVehicle(this, currentTick);
            } else if (occupied instanceof OccupiedEdgeImpl) {
                vehicleManager.getOccupied(next).addVehicle(this, currentTick);
                path.nodes().pop();
            } else {
                throw new AssertionError("Component must be either node or component");
            }
        }
    }

    void loadOrder(ConfirmedOrder order) {
        double nextWeight = order.getWeight() + this.getCurrentWeight();
        if (nextWeight > this.capacity) {
            throw new VehicleOverloadedException(this, nextWeight);
        }
        this.orders.add(order);
    }

    void unloadOrder(ConfirmedOrder order) {
        this.orders.removeIf(confirmedOrder -> confirmedOrder.getOrderID() == order.getOrderID());
    }

    @Override
    public int compareTo(Vehicle o) {
        return Integer.compare(getId(), o.getId());
    }

    @Override
    public String toString() {
        return "VehicleImpl(" + "id=" + id + ", capacity=" + capacity + ", orders=" + orders + ", component="
               + occupied.component + ')';
    }

    private record PathImpl(Deque<Region.Node> nodes, BiConsumer<? super Vehicle, Long> arrivalAction) implements Path {

    }
}
