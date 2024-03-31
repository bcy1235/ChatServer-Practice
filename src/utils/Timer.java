package utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * class used for calculate average time
 */
public class Timer {
    private static Queue<Long> start;

    static {
            start = new LinkedList<>();
    }

    /**
     * used when you want to check performance code area where in front of start entry point
     */
    public static synchronized void checkStart() {
        start.add(System.nanoTime());
    }

    /**
     * @return if you didn't invoke checkStart() then return false, else true
     */
    public static synchronized boolean checkOver() {
        if (start.isEmpty())
            return false;

        Double timeElapsed = (double) (System.nanoTime() - start.poll()) / 1_000_000_000D;
        System.out.format("%f (sec)\n", timeElapsed);
        return true;
    }


}
