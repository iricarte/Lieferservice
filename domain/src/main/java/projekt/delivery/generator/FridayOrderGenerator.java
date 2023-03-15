package projekt.delivery.generator;

import projekt.base.Location;
import projekt.base.TickInterval;
import projekt.delivery.routing.ConfirmedOrder;
import projekt.delivery.routing.VehicleManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of an {@link OrderGenerator} that represents the incoming orders on an
 * average friday evening.
 * The incoming orders follow a normal distribution.<p>
 * <p>
 * To create a new {@link FridayOrderGenerator} use {@code FridayOrderGenerator.Factory.builder()
 * ...build();}.
 */
public class FridayOrderGenerator implements OrderGenerator {

    private final Random random;
    private final int orderCount;
    private final VehicleManager vehicleManager;
    private final int deliveryInterval;
    private final double maxWeight;
    private final double standardDeviation;
    private final long lastTick;
    private final List<List<ConfirmedOrder>> existingOrders;

    /**
     * Creates a new {@link FridayOrderGenerator} with the given parameters.
     *
     * @param orderCount        The total amount of orders this {@link OrderGenerator} will
     *                          create. It is equal to the sum of
     *                          the size of the lists that are returned for every positive long
     *                          value.
     * @param vehicleManager    The {@link VehicleManager} this {@link OrderGenerator} will
     *                          create orders for.
     * @param deliveryInterval  The amount of ticks between the start and end tick of the
     *                          deliveryInterval of the created orders.
     * @param maxWeight         The maximum weight of a created order.
     * @param standardDeviation The standardDeviation of the normal distribution.
     * @param lastTick          The last tick this {@link OrderGenerator} can return a non-empty
     *                          list.
     * @param seed              The seed for the used {@link Random} instance. If negative a
     *                          random seed will be used.
     */
    private FridayOrderGenerator(int orderCount,
                                 VehicleManager vehicleManager,
                                 int deliveryInterval,
                                 double maxWeight,
                                 double standardDeviation,
                                 long lastTick,
                                 int seed) {
        this.random = seed < 0 ? new Random() : new Random(seed);
        this.orderCount = orderCount;
        this.vehicleManager = vehicleManager;
        this.deliveryInterval = deliveryInterval;
        this.maxWeight = maxWeight;
        this.standardDeviation = standardDeviation;
        this.lastTick = lastTick;
        this.existingOrders = new LinkedList<>();
    }

    @Override
    public List<ConfirmedOrder> generateOrders(long tick) {
        if (this.isValidTick(tick)) {
            List<ConfirmedOrder> confirmedOrders = new ArrayList<>();
            int ordersToGenerate = this.calculateOrdersToGenerate();
            for (int i = 0; i < ordersToGenerate; i++) {
                ConfirmedOrder confirmedOrder = this.generateOrder(tick);
                confirmedOrders.add(confirmedOrder);
            }
            this.existingOrders.add(confirmedOrders);
        }
        return this.existingOrders.stream().skip(tick).findFirst().orElse(new ArrayList<>());
    }

    private boolean isValidTick(long tick) {
        if (tick < 0) {
            throw new IndexOutOfBoundsException(tick);
        }
        return tick < this.lastTick && tick >= this.existingOrders.size();
    }

    private int calculateOrdersToGenerate() {
        return (int) (standardDeviation * (0.25 + random.nextGaussian()) / 2);
    }

    @NotNull
    private ConfirmedOrder generateOrder(long tick) {
        Location randomLocation = this.pickRandomLocation();
        VehicleManager.OccupiedRestaurant randomRestaurant = this.pickRandomRestaurant();
        TickInterval tickInterval = this.generateTickInterval(tick);
        ArrayList<String> randomFoods = this.pickRandomFoods(randomRestaurant);
        double randomWeight = this.getRandomWeight();
        return new ConfirmedOrder(randomLocation, randomRestaurant, tickInterval, randomFoods, randomWeight);
    }

    private Location pickRandomLocation() {
        VehicleManager.OccupiedNeighborhood neighborhood = vehicleManager.getOccupiedNeighborhoods()
                                                                         .stream()
                                                                         .skip(this.random.nextInt(vehicleManager.getOccupiedNeighborhoods()
                                                                                                                 .size()))
                                                                         .findFirst()
                                                                         .orElse(null);
        return Objects.requireNonNull(neighborhood).getComponent().getLocation();
    }

    private VehicleManager.OccupiedRestaurant pickRandomRestaurant() {
        return vehicleManager.getOccupiedRestaurants()
                             .stream()
                             .skip(this.random.nextInt(vehicleManager.getOccupiedRestaurants().size()))
                             .findFirst()
                             .orElse(null);
    }

    @NotNull
    private TickInterval generateTickInterval(long tick) {
        return new TickInterval(tick, tick + deliveryInterval);
    }

    private ArrayList<String> pickRandomFoods(VehicleManager.OccupiedRestaurant restaurant) {
        ArrayList<String> pickedFoods = new ArrayList<>();
        int randomAmountOfFoods = this.random.nextInt(1, 10);
        List<String> availableFoods = restaurant.getComponent().getAvailableFood();
        for (int i = 0; i < randomAmountOfFoods; i++) {
            pickedFoods.add(this.pickFood(availableFoods));
        }
        return pickedFoods;
    }

    private double getRandomWeight() {
        return this.random.nextDouble(0, this.maxWeight);
    }

    private String pickFood(List<String> availableFoods) {
        return availableFoods.get(this.random.nextInt(availableFoods.size()));
    }

    /**
     * A {@link OrderGenerator.Factory} for creating a new {@link FridayOrderGenerator}.
     */
    public static class Factory implements OrderGenerator.Factory {

        public final int orderCount;
        public final VehicleManager vehicleManager;
        public final int deliveryInterval;
        public final double maxWeight;
        public final double standardDeviation;
        public final long lastTick;
        public final int seed;

        private Factory(int orderCount,
                        VehicleManager vehicleManager,
                        int deliveryInterval,
                        double maxWeight,
                        double standardDeviation,
                        long lastTick,
                        int seed) {
            this.orderCount = orderCount;
            this.vehicleManager = vehicleManager;
            this.deliveryInterval = deliveryInterval;
            this.maxWeight = maxWeight;
            this.standardDeviation = standardDeviation;
            this.lastTick = lastTick;
            this.seed = seed;
        }

        /**
         * Creates a new {@link FridayOrderGenerator.FactoryBuilder}.
         *
         * @return The created {@link FridayOrderGenerator.FactoryBuilder}.
         */
        public static FridayOrderGenerator.FactoryBuilder builder() {
            return new FridayOrderGenerator.FactoryBuilder();
        }

        @Override
        public OrderGenerator create() {
            return new FridayOrderGenerator(orderCount,
                                            vehicleManager,
                                            deliveryInterval,
                                            maxWeight,
                                            standardDeviation,
                                            lastTick,
                                            seed);
        }
    }

    /**
     * A {@link OrderGenerator.FactoryBuilder} form constructing a new
     * {@link FridayOrderGenerator.Factory}.
     */
    public static class FactoryBuilder implements OrderGenerator.FactoryBuilder {

        public int orderCount = 1000;
        public VehicleManager vehicleManager = null;
        public int deliveryInterval = 15;
        public double maxWeight = 0.5;
        public double standardDeviation = 0.5;
        public long lastTick = 480;
        public int seed = -1;

        private FactoryBuilder() {
        }

        public FactoryBuilder setOrderCount(int orderCount) {
            this.orderCount = orderCount;
            return this;
        }

        public FactoryBuilder setVehicleManager(VehicleManager vehicleManager) {
            this.vehicleManager = vehicleManager;
            return this;
        }

        public FactoryBuilder setDeliveryInterval(int deliveryInterval) {
            this.deliveryInterval = deliveryInterval;
            return this;
        }

        public FactoryBuilder setMaxWeight(double maxWeight) {
            this.maxWeight = maxWeight;
            return this;
        }

        public FactoryBuilder setStandardDeviation(double standardDeviation) {
            this.standardDeviation = standardDeviation;
            return this;
        }

        public FactoryBuilder setLastTick(long lastTick) {
            this.lastTick = lastTick;
            return this;
        }

        public FactoryBuilder setSeed(int seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Factory build() {
            Objects.requireNonNull(vehicleManager);
            return new Factory(orderCount,
                               vehicleManager,
                               deliveryInterval,
                               maxWeight,
                               standardDeviation,
                               lastTick,
                               seed);
        }
    }
}