package projekt.delivery.routing;

import projekt.base.Location;
import projekt.delivery.service.BasicDeliveryService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

public class TestBase {
    public Location neighborhoodLocation;
    public Location neighborhoodLocation2;
    public Location restaurantLocation;
    public Location restaurantLocation2;

    public RegionImpl region;
    public String food;
    public List<String> foodList;
    public RestaurantImpl restaurant;
    public RestaurantImpl restaurant2;
    public NeighborhoodImpl neighborhood;
    public NeighborhoodImpl neighborhood2;
    public EdgeImpl edge;
    public EdgeImpl edge2;
    public EdgeImpl edge3;
    public VehicleManagerImpl vehicleManager;
    public VehicleImpl vehicle;
    public VehicleManagerImpl.OccupiedRestaurant occupiedRestaurant;
    public VehicleManagerImpl.OccupiedRestaurant occupiedRestaurant2;
    public BasicDeliveryService deliveryService;

    public static NodeImpl createNode(RegionImpl region, String name, Location location, Set<Location> connections) {
        return new NodeImpl(region, name, location, connections);
    }

    @BeforeEach
    public void setup() {
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
        vehicleManager = createVehicleManagerImpl(region);
        occupiedRestaurant = vehicleManager.getOccupiedRestaurant(restaurant);
        occupiedRestaurant2 = vehicleManager.getOccupiedRestaurant(restaurant2);
        vehicle = createVehicle(1, 3, vehicleManager, occupiedRestaurant);
        addVehicleToVehicleManagerImpl(vehicleManager, vehicle);
        addVehicleToOccupied(occupiedRestaurant, vehicle);
        deliveryService = new BasicDeliveryService(vehicleManager);
    }

    public static RegionImpl createRegion() {
        return new RegionImpl();
    }

    public static RestaurantImpl createRestaurant(RegionImpl region,
                                                  String name,
                                                  Location location,
                                                  Set<Location> connections,
                                                  List<String> foodList) {
        return new RestaurantImpl(region, name, location, connections, foodList);
    }

    public static NeighborhoodImpl createNeighborhood(RegionImpl region,
                                                      String name,
                                                      Location location,
                                                      Set<Location> connections) {
        return new NeighborhoodImpl(region, name, location, connections);
    }

    public static EdgeImpl createEdge(RegionImpl region, String name, Location from, Location to, long distance) {
        return new EdgeImpl(region, name, from, to, distance);
    }

    public static void addNodesToRegion(RegionImpl region, NodeImpl... nodes) {
        for (NodeImpl node : nodes) {
            region.putNode(node);
        }
    }

    public static void addEdgesToRegion(RegionImpl region, Region.Edge... edge) {
        for (Region.Edge e : edge) {
            region.getEdges().add(e);
        }
    }

    public static void addEdgesAttributeToRegion(RegionImpl region, Location locationA, Map<Location, EdgeImpl> nodes) {
        for (Map.Entry<Location, EdgeImpl> nodesEntry : nodes.entrySet()) {
            if (locationA.compareTo(nodesEntry.getKey()) > 0) {
                throw new IllegalArgumentException(String.format("locationA %s must be <= locationB %s",
                                                                 locationA,
                                                                 nodesEntry.getKey()));
            }
            region.putEdge(nodesEntry.getValue());
        }
    }

    public static VehicleManagerImpl createVehicleManagerImpl(RegionImpl region) {
        return new VehicleManagerImpl(region, new CachedPathCalculator(new DijkstraPathCalculator()));
    }

    public static VehicleImpl createVehicle(int id,
                                            double capacity,
                                            VehicleManagerImpl vehicleManager,
                                            VehicleManagerImpl.OccupiedRestaurant startingRestaurant) {
        return new VehicleImpl(id, capacity, vehicleManager, startingRestaurant);
    }

    public static void addVehicleToVehicleManagerImpl(VehicleManagerImpl vehicleManager, Vehicle vehicle) {
        vehicleManager.addVehicle(vehicle.getStartingNode().getComponent().getLocation(), vehicle.getCapacity());
    }

    public static void addVehicleToOccupied(VehicleManagerImpl.Occupied<?> occupied, VehicleImpl vehicle) {
        occupied.addVehicle(vehicle, 0);
    }

}
