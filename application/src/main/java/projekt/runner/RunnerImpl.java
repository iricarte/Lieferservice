package projekt.runner;

import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.archetype.ProblemGroup;
import projekt.delivery.rating.RatingCriteria;
import projekt.delivery.service.DeliveryService;
import projekt.delivery.simulation.BasicDeliverySimulation;
import projekt.delivery.simulation.Simulation;
import projekt.delivery.simulation.SimulationConfig;
import projekt.runner.handler.ResultHandler;
import projekt.runner.handler.SimulationFinishedHandler;
import projekt.runner.handler.SimulationSetupHandler;

import java.util.HashMap;
import java.util.Map;

public class RunnerImpl implements Runner {

    @Override
    public void run(ProblemGroup problemGroup,
                    SimulationConfig simulationConfig,
                    int simulationRuns,
                    DeliveryService.Factory deliveryServiceFactory,
                    SimulationSetupHandler simulationSetupHandler,
                    SimulationFinishedHandler simulationFinishedHandler,
                    ResultHandler resultHandler) {

        Map<ProblemArchetype, Simulation> simulations = this.createSimulations(problemGroup,
                                                                               simulationConfig,
                                                                               deliveryServiceFactory);

        Map<RatingCriteria, Double> result = this.runSimulations(simulationRuns,
                                                                 simulationSetupHandler,
                                                                 simulationFinishedHandler,
                                                                 simulations);

        resultHandler.accept(result);
    }

    @Override
    public Map<ProblemArchetype, Simulation> createSimulations(ProblemGroup problemGroup,
                                                               SimulationConfig simulationConfig,
                                                               DeliveryService.Factory deliveryServiceFactory) {
        Map<ProblemArchetype, Simulation> retVal = new HashMap<>();
        for (ProblemArchetype problem : problemGroup.problems()) {
            BasicDeliverySimulation simulation = new BasicDeliverySimulation(simulationConfig,
                                                                             problem.raterFactoryMap(),
                                                                             deliveryServiceFactory.create(problem.vehicleManager()),
                                                                             problem.orderGeneratorFactory());
            retVal.put(problem, simulation);
        }
        return retVal;
    }

    private Map<RatingCriteria, Double> runSimulations(int simulationRuns,
                                                       SimulationSetupHandler simulationSetupHandler,
                                                       SimulationFinishedHandler simulationFinishedHandler,
                                                       Map<ProblemArchetype, Simulation> simulations) {
        Map<RatingCriteria, Double> criteriaToAverage = new HashMap<>();
        for (int i = 0; i < simulationRuns; i++) {
            for (Map.Entry<ProblemArchetype, Simulation> simulationEntry : simulations.entrySet()) {
                Simulation simulation = simulationEntry.getValue();
                ProblemArchetype problem = simulationEntry.getKey();
                simulationSetupHandler.accept(simulation, problem, i);
                simulation.runSimulation(1000);
                this.measureCriteria(simulation, RatingCriteria.TRAVEL_DISTANCE, criteriaToAverage);
                this.measureCriteria(simulation, RatingCriteria.AMOUNT_DELIVERED, criteriaToAverage);
                this.measureCriteria(simulation, RatingCriteria.IN_TIME, criteriaToAverage);
                if (simulationFinishedHandler.accept(simulation, problem)) {
                    return criteriaToAverage;
                }
            }
        }
        return criteriaToAverage;
    }

    private void measureCriteria(Simulation simulation,
                                 RatingCriteria criteria,
                                 Map<RatingCriteria, Double> criteriaToAverage) {
        double ratingForCriterion = simulation.getRatingForCriterion(RatingCriteria.TRAVEL_DISTANCE);
        criteriaToAverage.merge(criteria,
                                ratingForCriterion,
                                (existingRationing, newRating) -> (existingRationing + newRating) / 2);
    }
}
