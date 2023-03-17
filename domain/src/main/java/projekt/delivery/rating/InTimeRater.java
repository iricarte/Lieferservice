package projekt.delivery.rating;

import projekt.delivery.event.DeliverOrderEvent;
import projekt.delivery.event.Event;
import projekt.delivery.event.OrderReceivedEvent;
import projekt.delivery.routing.ConfirmedOrder;
import projekt.delivery.simulation.Simulation;

import java.util.List;


/**
 * Rates the observed {@link Simulation} based on the punctuality of the orders.<p>
 * <p>
 * To create a new {@link InTimeRater} use {@code InTimeRater.Factory.builder()...build();}.
 */
public class InTimeRater implements Rater {

    public static final RatingCriteria RATING_CRITERIA = RatingCriteria.IN_TIME;

    private final long ignoredTicksOff;
    private final long maxTicksOff;

    private double actualTotalTicksOff;
    private double maxTotalTicksOff;

    /**
     * Creates a new {@link InTimeRater} instance.
     *
     * @param ignoredTicksOff The amount of ticks this {@link InTimeRater} ignores when dealing
     *                        with an {@link ConfirmedOrder} that didn't get
     *                        delivered in time.
     * @param maxTicksOff     The maximum amount of ticks too late/early this {@link InTimeRater}
     *                        considers.
     */
    private InTimeRater(long ignoredTicksOff, long maxTicksOff) {
        if (ignoredTicksOff < 0) {
            throw new IllegalArgumentException(String.valueOf(ignoredTicksOff));
        }
        if (maxTicksOff <= 0) {
            throw new IllegalArgumentException(String.valueOf(maxTicksOff));
        }

        this.ignoredTicksOff = ignoredTicksOff;
        this.maxTicksOff = maxTicksOff;
    }

    // Calculates and returns Score due to Criteria IN_TIME / if orders are delivered in Time
    @Override
    public double getScore() {

        if (maxTotalTicksOff != 0.0 && actualTotalTicksOff != 0.0) {
            return 1.0 - (actualTotalTicksOff / maxTotalTicksOff);
        } else if (actualTotalTicksOff == 0.0 && maxTotalTicksOff != 0.0) {
            return 1.0;
        } else {
            return 0.0;
        }

    }

    // Calculates actualTotalTicksOff and maxTotalTicksOff to evaluate the Score @getScore()
    public void onTick(List<Event> events, long tick) {

        // @param deliveryInterval The {@link TickInterval} in which the {@link ConfirmedOrder} should be delivered.
        // Duration: The TickInterval in which the ConfirmedOrder was actually delivered

        double deliveredCounter = 0.0;
        double receivedCounter = 0.0;
        double tempActualTotalTicksOff = 0.0;

        for (Event event : events) {

            if (event instanceof DeliverOrderEvent) {

                long actualDeliveryTick = ((DeliverOrderEvent) event).getOrder().getActualDeliveryTick();
                //long deliveryIntervallDuration = ((DeliverOrderEvent) event).getOrder().getDeliveryInterval().getDuration();
                long deliveryIntervallEnd = ((DeliverOrderEvent) event).getOrder().getDeliveryInterval().end();

                // long ticksOff = actualDeliveryTick - deliveryIntervallEnd - ignoredTicksOff;
                long ticksOff;

                if (actualDeliveryTick > ((DeliverOrderEvent) event).getOrder().getDeliveryInterval().start() &&
                    actualDeliveryTick < (deliveryIntervallEnd + ignoredTicksOff)) {
                    tempActualTotalTicksOff += 0.0;
                } else {
                    ticksOff = actualDeliveryTick - deliveryIntervallEnd - ignoredTicksOff;
                    if (ticksOff > 0.0) {
                        tempActualTotalTicksOff += Math.min(ticksOff, maxTicksOff);
                    }
                }
                deliveredCounter++;
            }

            if (event instanceof OrderReceivedEvent) {
                receivedCounter++;
            }
        }

        //double notDelivered = receivedCounter - deliveredCounter;
        actualTotalTicksOff = (receivedCounter - deliveredCounter) * maxTicksOff + tempActualTotalTicksOff;
        maxTotalTicksOff = receivedCounter * maxTicksOff;
    }

    /**
     * A {@link Rater.Factory} for creating a new {@link InTimeRater}.
     */
    @Override
    public RatingCriteria getRatingCriteria() {
        return RATING_CRITERIA;
    }

    public static class Factory implements Rater.Factory {

        public final long ignoredTicksOff;
        public final long maxTicksOff;

        private Factory(long ignoredTicksOff, long maxTicksOff) {
            this.ignoredTicksOff = ignoredTicksOff;
            this.maxTicksOff = maxTicksOff;
        }

        /**
         * Creates a new {@link InTimeRater.FactoryBuilder}.
         *
         * @return The created {@link InTimeRater.FactoryBuilder}.
         */
        public static FactoryBuilder builder() {
            return new FactoryBuilder();
        }

        @Override
        public InTimeRater create() {
            return new InTimeRater(ignoredTicksOff, maxTicksOff);
        }
    }

    /**
     * A {@link Rater.FactoryBuilder} form constructing a new {@link InTimeRater.Factory}.
     */
    public static class FactoryBuilder implements Rater.FactoryBuilder {

        public long ignoredTicksOff = 5;
        public long maxTicksOff = 25;

        private FactoryBuilder() {
        }

        public FactoryBuilder setIgnoredTicksOff(long ignoredTicksOff) {
            this.ignoredTicksOff = ignoredTicksOff;
            return this;
        }

        public FactoryBuilder setMaxTicksOff(long maxTicksOff) {
            this.maxTicksOff = maxTicksOff;
            return this;
        }

        @Override
        public Factory build() {
            return new Factory(ignoredTicksOff, maxTicksOff);
        }
    }
}
