package projekt.delivery.service;

import projekt.delivery.event.Event;
import projekt.delivery.routing.ConfirmedOrder;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.routing.VehicleManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OurDeliveryService extends AbstractDeliveryService {

    // List of orders that have not yet been loaded onto delivery vehicles
    protected final List<ConfirmedOrder> pendingOrders = new ArrayList<>();

    public OurDeliveryService(VehicleManager vehicleManager) {
        super(vehicleManager);
    }

    @Override
    protected List<Event> tick(long currentTick, List<ConfirmedOrder> newOrders) {
        List<Event> events = vehicleManager.tick(currentTick);
        pendingOrders.addAll(newOrders);
        pendingOrders.sort(Comparator.comparing(confirmedOrder -> confirmedOrder.getDeliveryInterval().start()));
        if (!pendingOrders.isEmpty()) {
            super.handleRestaurants(currentTick);
        }
        return events;
    }

    @Override
    protected void postDispatch(Vehicle vehicle) {
        super.returnToRestaurant(vehicle);
    }

    @Override
    public List<ConfirmedOrder> getPendingOrders() {
        return pendingOrders;
    }

    @Override
    public void reset() {
        super.reset();
        pendingOrders.clear();
    }

    public interface Factory extends DeliveryService.Factory {

        OurDeliveryService create(VehicleManager vehicleManager);
    }
}
