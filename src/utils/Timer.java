package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * class used for calculate average time
 */
public class Timer {
    private static Queue<Long> start;
    private static long sum;
    private static long maxTime;
    private static long minTime;
    private static int count;

    static {
        start = new LinkedList<>();
        maxTime = -1;
        minTime = 0x0FFFFFFFFFFFFFFFL;
        count = 0;
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

        long nowTime = System.nanoTime() - start.poll();

        maxTime = Math.max(nowTime, maxTime);
        minTime = Math.min(nowTime, minTime);
        sum += nowTime;
        count++;
        return true;
    }

    public static synchronized void makeFile() {
        try {
            FileWriter fileWriter = new FileWriter("result.txt", false);

            sum -= (maxTime - minTime);
            count -= 2;
            double avg = (double) (sum / (count)) / 1_000_000_000;
            String output = String.format("" +
                    "Sum : %f(sec)\n" +
                    "Count : %d\n" +
                    "MaxTime : %f(sec)\n" +
                    "MinTime : %f(sec)\n" +
                    "Average : %f(sec)"
                    , (double) sum / 1_000_000_000
                    , count
                    , (double) maxTime / 1_000_000_000
                    , (double) minTime / 1_000_000_000
                    , avg);
            fileWriter.write(output);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Timer's makeFile method : " + e);
        }
    }
}
