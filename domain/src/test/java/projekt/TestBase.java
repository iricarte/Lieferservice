package projekt;

import projekt.base.Location;
import projekt.delivery.routing.CachedPathCalculator;
import projekt.delivery.routing.DijkstraPathCalculator;
import projekt.delivery.routing.PathCalculator;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.routing.VehicleManager;
import projekt.delivery.service.BasicDeliveryService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

public class TestBase {
    public Location neighborhoodLocation;
    public Location neighborhoodLocation2;
    public Location restaurantLocation;
    public Location restaurantLocation2;

    public Region region;
    public String food;
    public List<String> foodList;
    public Region.Restaurant restaurant;
    public Region.Restaurant restaurant2;
    public Region.Neighborhood neighborhood;
    public Region.Neighborhood neighborhood2;
    public Region.Edge edge;
    public Region.Edge edge2;
    public Region.Edge edge3;
    public VehicleManager vehicleManager;
    public Vehicle vehicle;
    public VehicleManager.OccupiedRestaurant occupiedRestaurant;
    public VehicleManager.OccupiedRestaurant occupiedRestaurant2;
    public BasicDeliveryService deliveryService;

    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        neighborhoodLocation = new Location(1, 1);
        neighborhoodLocation2 = new Location(3, 3);
        restaurantLocation = new Location(0, 0);
        restaurantLocation2 = new Location(2, 2);
        region = createRegion();
        food = "food";
        foodList = List.of(food);
        restaurant = createRestaurant(region,
                                      "R",
                                      restaurantLocation,
                                      Set.of(neighborhoodLocation, neighborhoodLocation2, restaurantLocation2),
                                      foodList);
        restaurant2 = createRestaurant(region, "R2", restaurantLocation2, Set.of(restaurantLocation), foodList);
        neighborhood = createNeighborhood(region, "N", neighborhoodLocation, Set.of(restaurantLocation));
        neighborhood2 = createNeighborhood(region, "N2", neighborhoodLocation2, Set.of(restaurantLocation));
        edge = createEdge(region, "RN", restaurantLocation, neighborhoodLocation, 1);
        edge2 = createEdge(region, "RN2", restaurantLocation, neighborhoodLocation2, 1);
        edge3 = createEdge(region, "RR2", restaurantLocation, restaurantLocation2, 1);
        addNodesToRegion(region, restaurant, neighborhood, restaurant2, neighborhood2);
        addEdgesToRegion(region, edge, edge2, edge3);
        addEdgesAttributeToRegion(region,
                                  restaurantLocation,
                                  Map.of(neighborhoodLocation,
                                         edge,
                                         neighborhoodLocation2,
                                         edge2,
                                         restaurantLocation2,
                                         edge3));
        vehicleManager = createVehicleManager(region);
        occupiedRestaurant = vehicleManager.getOccupiedRestaurant(restaurant);
        occupiedRestaurant2 = vehicleManager.getOccupiedRestaurant(restaurant2);
        vehicle = createVehicle(1, 3, vehicleManager, occupiedRestaurant);
        addVehicleToVehicleManager(vehicleManager, vehicle);
        addVehicleToOccupied(occupiedRestaurant, vehicle);
        deliveryService = new BasicDeliveryService(vehicleManager);
    }

    public static Region createRegion() throws ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.RegionImpl").getDeclaredConstructor();
        constructor.setAccessible(true);
        return (Region) constructor.newInstance();
    }

    public static Region.Restaurant createRestaurant(Region region,
                                                     String name,
                                                     Location location,
                                                     Set<Location> connections,
                                                     List<String> foodList) throws ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.RestaurantImpl")
                                          .getDeclaredConstructor(Region.class,
                                                                  String.class,
                                                                  Location.class,
                                                                  Set.class,
                                                                  List.class);
        constructor.setAccessible(true);
        return (Region.Restaurant) constructor.newInstance(region, name, location, connections, foodList);
    }

    public static Region.Neighborhood createNeighborhood(Region region,
                                                         String name,
                                                         Location location,
                                                         Set<Location> connections) throws
                                                                                    ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.NeighborhoodImpl")
                                          .getDeclaredConstructor(Region.class,
                                                                  String.class,
                                                                  Location.class,
                                                                  Set.class);
        constructor.setAccessible(true);
        return (Region.Neighborhood) constructor.newInstance(region, name, location, connections);
    }

    public static Region.Edge createEdge(Region region, String name, Location from, Location to, long distance) throws
                                                                                                                ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.EdgeImpl")
                                          .getDeclaredConstructor(Region.class,
                                                                  String.class,
                                                                  Location.class,
                                                                  Location.class,
                                                                  long.class);
        constructor.setAccessible(true);
        return (Region.Edge) constructor.newInstance(region, name, from, to, distance);
    }

    public static void addNodesToRegion(Region region, Region.Node... node) throws ReflectiveOperationException {
        for (Region.Node n : node) {
            addNodeToRegion(region, n);
        }
    }

    public static void addEdgesToRegion(Region region, Region.Edge... edge) throws ReflectiveOperationException {
        for (Region.Edge e : edge) {
            addEdgeToRegion(region, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addEdgesAttributeToRegion(Region region,
                                                 Location locationA,
                                                 Map<Location, Region.Edge> nodes) throws ReflectiveOperationException {

        for (Location locationB : nodes.keySet()) {
            if (locationA.compareTo(locationB) > 0) {
                throw new IllegalArgumentException(String.format("locationA %s must be <= locationB %s",
                                                                 locationA,
                                                                 locationB));
            }
        }

        Field edgesField = region.getClass().getDeclaredField("edges");
        edgesField.setAccessible(true);
        ((Map<Location, Map<Location, Region.Edge>>) edgesField.get(region)).put(locationA, new HashMap<>(nodes));
    }

    public static VehicleManager createVehicleManager(Region region) throws ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.VehicleManagerImpl")
                                          .getDeclaredConstructor(Region.class, PathCalculator.class);
        constructor.setAccessible(true);
        return (VehicleManager) constructor.newInstance(region, new CachedPathCalculator(new DijkstraPathCalculator()));
    }

    @SuppressWarnings("JavaReflectionInvocation")
    public static Vehicle createVehicle(int id,
                                        double capacity,
                                        VehicleManager vehicleManager,
                                        VehicleManager.OccupiedRestaurant startingRestaurant) throws
                                                                                              ReflectiveOperationException {
        Constructor<?> constructor = Class.forName("projekt.delivery.routing.VehicleImpl")
                                          .getDeclaredConstructor(int.class,
                                                                  double.class,
                                                                  Class.forName(
                                                                          "projekt.delivery.routing.VehicleManagerImpl"),
                                                                  VehicleManager.OccupiedRestaurant.class);
        constructor.setAccessible(true);
        return (Vehicle) constructor.newInstance(id, capacity, vehicleManager, startingRestaurant);
    }

    public static void addVehicleToVehicleManager(VehicleManager vehicleManager, Vehicle vehicle) throws
                                                                                                  ReflectiveOperationException {
        Field vehiclesField = vehicleManager.getClass().getDeclaredField("vehicles");
        vehiclesField.setAccessible(true);
        ((List<Vehicle>) vehiclesField.get(vehicleManager)).add(vehicle);
    }

    public static void addVehicleToOccupied(VehicleManager.Occupied<?> occupied, Vehicle vehicle) throws
                                                                                                  ReflectiveOperationException {
        Field vehiclesField = Class.forName("projekt.delivery.routing.AbstractOccupied").getDeclaredField("vehicles");
        vehiclesField.setAccessible(true);
        ((Map<Vehicle, ?>) vehiclesField.get(occupied)).put(vehicle, null);
    }

    @SuppressWarnings("unchecked")
    public static void addNodeToRegion(Region region, Region.Node node) throws ReflectiveOperationException {
        Field nodes = region.getClass().getDeclaredField("nodes");
        nodes.setAccessible(true);
        ((Map<Location, Region.Node>) nodes.get(region)).put(node.getLocation(), node);
    }

    @SuppressWarnings("unchecked")
    public static void addEdgeToRegion(Region region, Region.Edge edge) throws ReflectiveOperationException {
        Field allEdges = region.getClass().getDeclaredField("allEdges");
        allEdges.setAccessible(true);
        ((List<Region.Edge>) allEdges.get(region)).add(edge);
    }
}
