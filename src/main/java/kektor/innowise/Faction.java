package kektor.innowise;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;

public class Faction implements Callable<Integer> {

    private static final int PER_DAY_LIMIT = 5;

    private final String name;
    private final Phaser phase;
    private final int numOfIterations;
    private final RoboFactory roboFactory;
    private final Map<RoboPart, Integer> roboStorage = new EnumMap<>(RoboPart.class);

    public Faction(String name, RoboFactory roboFactory, Phaser phase, int numOfIterations) {
        this.name = name;
        this.phase = phase;
        this.numOfIterations = numOfIterations;
        this.roboFactory = roboFactory;
    }

    @Override
    public Integer call() {
        for (int i = 0; i < numOfIterations; i++)
            captureRoboParts();
        return calculateTotalRobots();
    }

    private void captureRoboParts() {
        phase.arriveAndAwaitAdvance();
        int consumedTodayNum = 0;
        while (!roboFactory.isEmpty() && consumedTodayNum < PER_DAY_LIMIT) {
            var roboPart = roboFactory.consumePart();
            if (roboPart != null) {
                consumedTodayNum++;
                roboStorage.compute(roboPart, (_, v) -> v == null ? 1 : v + 1);
            }
        }
    }

    private Integer calculateTotalRobots() {
        return roboStorage
                .entrySet()
                .stream()
                .mapToInt(entry ->
                        switch (entry.getKey()) {
                            case HEAD, TORSO -> entry.getValue();
                            case HAND, FEET -> entry.getValue() / 2;
                        })
                .reduce(Integer::min)
                .orElse(0);
    }

    public String getName() {
        return name;
    }

}
