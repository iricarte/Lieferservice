package projekt.delivery.routing;

import projekt.TestBase;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleManagerImplTest extends TestBase {

    @Override
    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        super.setup();
    }

    @Test
    void getOccupiedRestaurant() {
        VehicleManagerImpl vehicleManager = (VehicleManagerImpl) super.vehicleManager;
        Assertions.assertEquals(restaurant, vehicleManager.getOccupiedRestaurant(restaurant).getComponent());
        Assertions.assertThrowsExactly(NullPointerException.class,
                                       () -> vehicleManager.getOccupiedRestaurant(null),
                                       "Node is null!");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                                       () -> vehicleManager.getOccupiedRestaurant(neighborhood),
                                       "Node " + neighborhood + " is not a restaurant");
    }

    @Test
    void getOccupied() throws ReflectiveOperationException {
        VehicleManagerImpl vehicleManager = (VehicleManagerImpl) super.vehicleManager;
        Assertions.assertEquals(restaurant, vehicleManager.getOccupied(restaurant).getComponent());
        Assertions.assertEquals(edge, vehicleManager.getOccupied(edge).getComponent());
        Assertions.assertThrowsExactly(NullPointerException.class,
                                       () -> vehicleManager.getOccupied(null),
                                       "Component is null!");
        MyOtherComponent component = new MyOtherComponent();
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                                       () -> vehicleManager.getOccupied(component),
                                       "Component is not of recognized subtype: " + component);
        Region.Node badNode = createNode(region, "", null, null);
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                                       () -> vehicleManager.getOccupied(badNode),
                                       "Could not find occupied node for " + badNode);

        Region.Edge badEdge = createEdge(region, "badEdge", restaurant.getLocation(), neighborhood.getLocation(), 0);
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                                       () -> vehicleManager.getOccupied(badEdge),
                                       "Could not find occupied edge for " + badEdge);

    }

    @Test
    void getOccupiedNeighborhood() {
        VehicleManagerImpl vehicleManager = (VehicleManagerImpl) super.vehicleManager;
        Assertions.assertEquals(neighborhood, vehicleManager.getOccupiedNeighborhood(neighborhood).getComponent());
        Assertions.assertThrowsExactly(NullPointerException.class,
                                       () -> vehicleManager.getOccupiedNeighborhood(null),
                                       "Node is null!");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                                       () -> vehicleManager.getOccupiedNeighborhood(restaurant),
                                       "Node " + neighborhood + " is not a neighborhood");
    }

    private static class MyOtherComponent implements Region.Component<MyOtherComponent> {

        @Override
        public Region getRegion() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public int compareTo(@NotNull VehicleManagerImplTest.MyOtherComponent o) {
            return 0;
        }
    }
}