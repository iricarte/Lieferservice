package projekt.delivery.rating;

import projekt.base.Location;
import projekt.delivery.event.ArrivedAtNodeEvent;
import projekt.delivery.event.DeliverOrderEvent;
import projekt.delivery.event.Event;
import projekt.delivery.event.OrderReceivedEvent;
import projekt.delivery.routing.PathCalculator;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.VehicleManager;
import projekt.delivery.simulation.Simulation;

import java.util.Deque;
import java.util.List;

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

        if (0 < actualDistance && actualDistance < worstDistance * factor) {
            return 1 - (actualDistance / worstDistance * factor);
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
        // TODO: Separate evaluation for actualDistance and worstDistance -> more readable, easier to maintain

        // calculate worstDistance


        double tempWorstDistance = 0.0;

        for (Event event : events) {
            if (event instanceof DeliverOrderEvent) {
                Location restaurantLocation = ((DeliverOrderEvent) event).getOrder().getRestaurant().getComponent().getLocation();
                Location deliveryLocation = ((DeliverOrderEvent) event).getOrder().getLocation();
                /*Deque<Region.Node> listOfNodes = pathCalculator.getPath(region.getNode(restaurantLocation), region.getNode(deliveryLocation));

                double distance = 0.0;
                Region.Node currentNode = null;
                for (Region.Node node : listOfNodes) {
                    if (currentNode != null) {
                        distance += region.getEdge(currentNode, node).getDuration();
                    }
                    currentNode = node;
                }*/
                tempWorstDistance += calculateDistance(region.getNode(restaurantLocation), region.getNode(deliveryLocation));
            }
        }
        worstDistance = 2 * tempWorstDistance;


        // calculate actualDistance
        double tempActualDistance = 0.0;

        for (Event event : events) {

            if (event instanceof ArrivedAtNodeEvent) {
                tempActualDistance += ((ArrivedAtNodeEvent) event).getLastEdge().getDuration();
            }
        }
        actualDistance = tempActualDistance;
    }


    private double calculateDistance(Region.Node start, Region.Node end) {
        Deque<Region.Node> listOfNodes = pathCalculator.getPath(start,end);

        double distance = 0.0;
        Region.Node currentNode = null;
        for (Region.Node node : listOfNodes) {
            if (currentNode == null) {
                distance += region.getEdge(start, node).getDuration();
            } else {
                distance += region.getEdge(currentNode, node).getDuration();
            }
            currentNode = node;
        }

        return distance;
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
