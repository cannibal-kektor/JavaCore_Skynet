package kektor.innowise;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.LockSupport;

import static java.lang.Math.random;

public class RoboFactory implements Runnable {

    public static final RoboPart[] ROBO_PARTS = RoboPart.values();
    public static final int MAX_OUTPUT_LIMIT = 11;

    private final Queue<RoboPart> roboParts = new ConcurrentLinkedQueue<>();
    private final Phaser phaser;
    private final int numOfIterations;

    public RoboFactory(Phaser phaser, int numOfIterations) {
        this.phaser = phaser;
        this.numOfIterations = numOfIterations;
    }

    @Override
    public void run() {
        int registeredParties = phaser.getRegisteredParties();
        for (int i = 0; i < numOfIterations; i++) {
            //w8 all factions
            while (phaser.getArrivedParties() != registeredParties - 1) {
                LockSupport.parkNanos(1000);
            }
            produceRoboParts();
        }
    }

    public RoboPart consumePart() {
        return roboParts.poll();
    }

    public boolean isEmpty() {
        return roboParts.isEmpty();
    }

    private void produceRoboParts() {
        int numOfProducedToday = (int) (random() * MAX_OUTPUT_LIMIT);
        for (int i = 0; i < numOfProducedToday; i++) {
            RoboPart value = ROBO_PARTS[(int) (random() * ROBO_PARTS.length)];
            roboParts.add(value);
        }
        phaser.arrive();
    }
}
