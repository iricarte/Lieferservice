package projekt.delivery.routing;

import projekt.TestBase;
import projekt.base.TickInterval;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleImplTest extends TestBase {

    private ConfirmedOrder order1;
    private ConfirmedOrder order2;

    @Override
    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        super.setup();
        this.order1 = new ConfirmedOrder(neighborhoodLocation,
                                         occupiedRestaurant,
                                         new TickInterval(2, 10),
                                         foodList,
                                         2);
        this.order2 = new ConfirmedOrder(neighborhoodLocation,
                                         occupiedRestaurant,
                                         new TickInterval(4, 10),
                                         foodList,
                                         1);
    }

    @Test
    void moveDirect() {
        VehicleImpl vehicle = ((VehicleImpl) super.vehicle);
        Assertions.assertDoesNotThrow(() -> vehicle.moveDirect(neighborhood));
        Assertions.assertThrows(IllegalArgumentException.class, () -> vehicle.moveDirect(restaurant));
        Assertions.assertDoesNotThrow(() -> vehicle.moveDirect(neighborhood));
        Assertions.assertDoesNotThrow(() -> vehicle.moveDirect(restaurant2));
    }

    @Test
    void moveQueued() {
        VehicleImpl vehicle = ((VehicleImpl) super.vehicle);
        Assertions.assertThrows(IllegalArgumentException.class, () -> vehicle.moveQueued(restaurant));
        Assertions.assertDoesNotThrow(() -> vehicle.moveQueued(neighborhood));
        Assertions.assertDoesNotThrow(() -> vehicle.moveQueued(restaurant));

    }

    @Test
    void loadOrder() {
        VehicleImpl vehicle = ((VehicleImpl) super.vehicle);
        Assertions.assertEquals(0, vehicle.getOrders().size());
        Assertions.assertDoesNotThrow(() -> vehicle.loadOrder(order1));
        Assertions.assertEquals(1, vehicle.getOrders().size());
        Assertions.assertDoesNotThrow(() -> vehicle.loadOrder(order2));
        Assertions.assertEquals(2, vehicle.getOrders().size());
        vehicle.getOrders().clear();
        Assertions.assertEquals(0, vehicle.getOrders().size());
        Assertions.assertDoesNotThrow(() -> vehicle.loadOrder(order1));
        Assertions.assertThrows(VehicleOverloadedException.class, () -> vehicle.loadOrder(order1));

    }

    @Test
    void unloadOrder() {
        VehicleImpl vehicle = ((VehicleImpl) super.vehicle);
        Assertions.assertEquals(0, vehicle.getOrders().size());
        Assertions.assertDoesNotThrow(() -> vehicle.loadOrder(order1));
        Assertions.assertDoesNotThrow(() -> vehicle.loadOrder(order2));
        Assertions.assertEquals(2, vehicle.getOrders().size());

        Assertions.assertDoesNotThrow(() -> vehicle.unloadOrder(order1));
        Assertions.assertIterableEquals(List.of(order2), vehicle.getOrders());
        Assertions.assertDoesNotThrow(() -> vehicle.unloadOrder(order2));
        Assertions.assertTrue(vehicle.getOrders().isEmpty());
    }

    @Test
    void currentWeight() {
        VehicleImpl vehicle = ((VehicleImpl) super.vehicle);
        vehicle.loadOrder(order1);
        Assertions.assertEquals(2, vehicle.getCurrentWeight());
        vehicle.loadOrder(order2);
        Assertions.assertEquals(3, vehicle.getCurrentWeight());
    }
}