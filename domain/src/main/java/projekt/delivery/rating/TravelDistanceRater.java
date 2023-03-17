package projekt.delivery.rating;

import projekt.delivery.event.ArrivedAtNodeEvent;
import projekt.delivery.event.Event;
import projekt.delivery.event.OrderReceivedEvent;
import projekt.delivery.routing.PathCalculator;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.VehicleManager;
import projekt.delivery.simulation.Simulation;

import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Rates the observed {@link Simulation} based on the distance traveled by all vehicles.<p>
 * <p>
 * To create a new {@link TravelDistanceRater} use {@code TravelDistanceRater.Factory.builder()..
 * .build();}.
 */
public class TravelDistanceRater implements Rater {

    public static final RatingCriteria RATING_CRITERIA = RatingCriteria.TRAVEL_DISTANCE;

    private final Region region;
    private final PathCalculator pathCalculator;
    private final double factor;
    private double actualDistance;
    private double worstDistance;

    private TravelDistanceRater(VehicleManager vehicleManager, double factor) {
        region = vehicleManager.getRegion();
        pathCalculator = vehicleManager.getPathCalculator();
        this.factor = factor;
    }

    @Override
    public double getScore() {
        if (0 <= actualDistance && actualDistance < worstDistance * factor) {
            return 1 - (actualDistance / (worstDistance * factor));
        } else {
            return 0;
        }
    }

    @Override
    public RatingCriteria getRatingCriteria() {
        return RATING_CRITERIA;
    }

    @Override
    public void onTick(List<Event> events, long tick) {
        for (Event event : events) {
            if (event instanceof OrderReceivedEvent deliverOrderEvent) {
                Region.Restaurant restaurant = deliverOrderEvent.getOrder().getRestaurant().getComponent();
                Region.Node destination = region.getNode(deliverOrderEvent.getOrder().getLocation());

                Deque<Region.Node> path = pathCalculator.getPath(restaurant, destination);
                worstDistance += calculateDistance(path, restaurant) * 2;
            } else if (event instanceof ArrivedAtNodeEvent arrivedAtNodeEvent) {
                actualDistance += arrivedAtNodeEvent.getLastEdge().getDuration();
            }
        }
    }

    private double calculateDistance(Deque<Region.Node> path, Region.Node startNode) {
        Region.Node currentNode = path.pop();
        double totalDistance = Objects.requireNonNull(region.getEdge(startNode, currentNode)).getDuration();
        while (!path.isEmpty()) {
            Region.Node nextNode = path.pop();
            totalDistance += Objects.requireNonNull(region.getEdge(currentNode, nextNode)).getDuration();
            currentNode = nextNode;
        }
        return totalDistance;
    }

    /**
     * A {@link Rater.Factory} for creating a new {@link TravelDistanceRater}.
     */
    public static class Factory implements Rater.Factory {

        public final VehicleManager vehicleManager;
        public final double factor;

        private Factory(VehicleManager vehicleManager, double factor) {
            this.vehicleManager = vehicleManager;
            this.factor = factor;
        }

        /**
         * Creates a new {@link TravelDistanceRater.FactoryBuilder}.
         *
         * @return The created {@link TravelDistanceRater.FactoryBuilder}.
         */
        public static FactoryBuilder builder() {
            return new FactoryBuilder();
        }

        @Override
        public TravelDistanceRater create() {
            return new TravelDistanceRater(vehicleManager, factor);
        }
    }

    /**
     * A {@link Rater.FactoryBuilder} form constructing a new {@link TravelDistanceRater.Factory}.
     */
    public static class FactoryBuilder implements Rater.FactoryBuilder {

        public VehicleManager vehicleManager;
        public double factor = 0.5;

        private FactoryBuilder() {
        }

        @Override
        public Factory build() {
            return new Factory(vehicleManager, factor);
        }

        public FactoryBuilder setVehicleManager(VehicleManager vehicleManager) {
            this.vehicleManager = vehicleManager;
            return this;
        }

        public FactoryBuilder setFactor(double factor) {
            if (factor < 0) {
                throw new IllegalArgumentException("factor must be positive");
            }

            this.factor = factor;
            return this;
        }
    }
}
