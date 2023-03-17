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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        this.runSimulations(simulationRuns,
                            simulationSetupHandler,
                            simulationFinishedHandler,
                            simulations,
                            resultHandler);

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

    private void runSimulations(int simulationRuns,
                                SimulationSetupHandler simulationSetupHandler,
                                SimulationFinishedHandler simulationFinishedHandler,
                                Map<ProblemArchetype, Simulation> simulations,
                                ResultHandler resultHandler) {
        Map<RatingCriteria, Double> criteriaToAverage = new HashMap<>();
        Map<RatingCriteria, List<Double>> criteriaToRatings = new HashMap<>();
        criteriaToRatings.put(RatingCriteria.AMOUNT_DELIVERED, new ArrayList<>());
        criteriaToRatings.put(RatingCriteria.IN_TIME, new ArrayList<>());

        for (int i = 0; i < simulationRuns; i++) {
            for (Map.Entry<ProblemArchetype, Simulation> simulationEntry : simulations.entrySet()) {
                Simulation simulation = simulationEntry.getValue();
                if (simulation instanceof BasicDeliverySimulation) {
                    criteriaToRatings.put(RatingCriteria.TRAVEL_DISTANCE, new ArrayList<>());
                }
                ProblemArchetype problem = simulationEntry.getKey();
                simulationSetupHandler.accept(simulation, problem, i);
                simulation.runSimulation(problem.simulationLength());
                this.measureCriteria(simulation, criteriaToRatings);
                if (simulationFinishedHandler.accept(simulation, problem)) {
                    return;
                }
            }
        }
        criteriaToRatings.forEach((criterion, ratings) -> criteriaToAverage.put(criterion,
                                                                                ratings.stream()
                                                                                       .mapToDouble(d -> d)
                                                                                       .sum() / ratings.size()));
        resultHandler.accept(criteriaToAverage);
    }

    private void measureCriteria(Simulation simulation, Map<RatingCriteria, List<Double>> criteriaToRatings) {
        for (Map.Entry<RatingCriteria, List<Double>> criterionToRatings : criteriaToRatings.entrySet()) {
            double ratingForCriterion = simulation.getRatingForCriterion(criterionToRatings.getKey());
            criterionToRatings.getValue().add(ratingForCriterion);
        }
    }
}
