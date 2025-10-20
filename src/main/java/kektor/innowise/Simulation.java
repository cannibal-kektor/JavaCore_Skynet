package kektor.innowise;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.out;
import static java.util.stream.Collectors.toMap;

public class Simulation {

    private final int numOfIterations;
    public static final Factions[] FACTIONS = Factions.values();

    public Simulation(int numOfIterations) {
        this.numOfIterations = numOfIterations;
    }

    public void start() {
        //parties = num of factions + 1 factory
        var phaser = new Phaser(FACTIONS.length + 1);

        var factory = new RoboFactory(phaser, numOfIterations);
        List<Faction> factionsList = new ArrayList<>();
        for (var faction : FACTIONS) {
            factionsList.add(new Faction(faction.name(), factory, phaser, numOfIterations));
        }

        var simulationResults = executeSimulation(factory, factionsList);
        displayResults(simulationResults);
    }

    private Map<String, Integer> executeSimulation(RoboFactory factory, List<Faction> factions) {
        Map<String, Integer> simulationResults;
        try (var executor = Executors.newCachedThreadPool()) {

            executor.execute(factory);
            Map<String, Future<Integer>> futuresResults = factions.stream()
                    .collect(toMap(Faction::getName, executor::submit));

            simulationResults = new HashMap<>();
            for (var entry : futuresResults.entrySet()) {
                try {
                    simulationResults.put(entry.getKey(), entry.getValue().get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Failed to get result for: " + entry.getKey(), e);
                }
            }
        }
        return simulationResults;
    }

    private void displayResults(Map<String, Integer> simulationResults) {
        int maxArmyNum = simulationResults
                .entrySet()
                .stream()
                .peek(entry -> out.println(entry.getKey() + " faction has an army of " + entry.getValue() + " robots"))
                .map(Map.Entry::getValue)
                .max(Comparator.naturalOrder())
                .orElse(0);

        out.println();

        simulationResults
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == maxArmyNum)
                .forEach(entry -> out.println(entry.getKey() + " has the strongest army of " + entry.getValue() + " robots"));

    }


}
