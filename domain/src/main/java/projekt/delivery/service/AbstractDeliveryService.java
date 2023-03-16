package projekt.delivery.service;

import projekt.base.Location;
import projekt.delivery.event.Event;
import projekt.delivery.event.OrderReceivedEvent;
import projekt.delivery.routing.ConfirmedOrder;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.routing.VehicleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractDeliveryService implements DeliveryService {

    protected final VehicleManager vehicleManager;
    private final Object lock = new Object();

    private List<ConfirmedOrder> unprocessedOrders = new ArrayList<>();

    protected AbstractDeliveryService(VehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @Override
    public void deliver(List<ConfirmedOrder> confirmedOrders) {
        synchronized (lock) {
            unprocessedOrders.addAll(confirmedOrders);
        }
    }

    @Override
    public List<Event> tick(long currentTick) {
        // Schedule new orders
        List<ConfirmedOrder> newOrders = Collections.emptyList();
        synchronized (lock) {
            if (!unprocessedOrders.isEmpty()) {
                newOrders = unprocessedOrders;
                unprocessedOrders = new ArrayList<>();
            }
        }

        //add a OrderReceivedEvent for each order
        newOrders.stream()
                 .map(order -> OrderReceivedEvent.of(currentTick, order))
                 .forEach(vehicleManager.getEventBus()::queuePost);

        return tick(currentTick, newOrders);
    }

    /**
     * Executes the current tick.
     *
     * @param currentTick The tick to execute.
     * @param newOrders   All new {@link ConfirmedOrder}s that have been ordered during the last
     *                    tick.
     * @return A {@link List} containing all {@link Event}s that occurred during the tick.
     */
    protected abstract List<Event> tick(long currentTick, List<ConfirmedOrder> newOrders);

    @Override
    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }

    protected void handleRestaurants(long currentTick) {
        this.vehicleManager.getOccupiedRestaurants()
                           .forEach(restaurant -> this.assignOrdersToVehicles(currentTick, restaurant));
    }

    protected void assignOrdersToVehicles(long currentTick, VehicleManager.OccupiedRestaurant restaurant) {
        restaurant.getVehicles().forEach(vehicle -> {
            this.loadOrdersIntoVehicle(currentTick, vehicle, restaurant);
            this.dispatchFirstOrderToDeliver(vehicle);
        });
    }

    protected void loadOrdersIntoVehicle(long currentTick,
                                         Vehicle vehicle,
                                         VehicleManager.OccupiedRestaurant restaurant) {
        for (ConfirmedOrder order : this.getOrdersFromRestaurant(restaurant)) {
            if (this.hasCapacity(vehicle, order)) {
                restaurant.loadOrder(vehicle, order, currentTick);
                this.getPendingOrders().remove(0);
            } else {
                break;
            }
        }
    }

    protected void dispatchFirstOrderToDeliver(Vehicle vehicle) {
        if (vehicle.getOrders().stream().findFirst().isPresent()) {
            ConfirmedOrder order = vehicle.getOrders().stream().findFirst().get();
            this.dispatchVehicleToDeliver(vehicle, order.getLocation());
        }
    }

    @NotNull
    private List<ConfirmedOrder> getOrdersFromRestaurant(VehicleManager.OccupiedRestaurant restaurant) {
        return getPendingOrders().stream()
                                 .filter(confirmedOrder -> confirmedOrder.getRestaurant().equals(restaurant))
                                 .toList();
    }

    private boolean hasCapacity(Vehicle vehicle, ConfirmedOrder order) {
        return vehicle.getCurrentWeight() + order.getWeight() <= vehicle.getCapacity();
    }

    @Override
    public List<ConfirmedOrder> getPendingOrders() {
        return unprocessedOrders;
    }

    protected void dispatchVehicleToDeliver(Vehicle vehicle, Location destination) {
        if (vehicle.getPaths().isEmpty()) {
            this.moveToDeliver(vehicle, this.vehicleManager.getRegion().getNode(destination));
            this.postDispatch(vehicle);
        }
    }

    protected void moveToDeliver(Vehicle vehicle, Region.Node destination) {
        VehicleManager.OccupiedNeighborhood neighborhood = this.vehicleManager.getOccupiedNeighborhood(destination);
        vehicle.moveQueued(neighborhood.getComponent(), this.executeDelivery(destination));
    }

    protected void postDispatch(Vehicle vehicle) {
        this.returnToRestaurant(vehicle);
    }

    @NotNull
    protected BiConsumer<Vehicle, Long> executeDelivery(Region.Node destinationNode) {
        return (vehicle, tick) -> this.deliverOrder(tick, vehicle, destinationNode);
    }

    @SuppressWarnings("unchecked")
    protected void returnToRestaurant(Vehicle vehicle) {
        vehicle.moveQueued(vehicle.getStartingNode().getComponent(), null);
    }

    protected void deliverOrder(long currentTick, Vehicle vehicle, Region.Node destinationNode) {
        VehicleManager.OccupiedNeighborhood neighborhood = vehicleManager.getOccupiedNeighborhood(destinationNode);
        for (ConfirmedOrder order : new ArrayList<>(vehicle.getOrders())) {
            if (Objects.equals(this.vehicleManager.getRegion().getNode(order.getLocation()), destinationNode)) {
                neighborhood.deliverOrder(vehicle, order, currentTick);
            }
        }
    }

    @Override
    public void reset() {
        unprocessedOrders.clear();
        vehicleManager.reset();
    }
}
