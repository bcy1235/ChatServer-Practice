package server.newMultiThreadVersion;
import java.util.concurrent.ConcurrentLinkedQueue;
    public class MessageStation {
        private static ConcurrentLinkedQueue<byte[]> concurrentLinkedQueue;
        private static final int COLLECTOR_NUM = 1;
        private static final int SPLITTER_NUM = 5;

    public static void start() {
        concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < COLLECTOR_NUM; i++) {
            new Thread(new Collector()).start();
        }
        for (int i = 0; i < SPLITTER_NUM; i++) {
            new Thread(new Splitter()).start();
        }
    }

    static class Collector implements Runnable {
        @Override
        public void run() {
            while (true) {
                ReadingBox.getMessage(concurrentLinkedQueue);
            }
        }
    }

    static class Splitter implements Runnable {
        @Override
        public void run() {
            while (true) {
                byte[] message;
                message = concurrentLinkedQueue.poll();
                if (message == null)
                    continue;
                WritingBox.push(message);
            }
        }
    }

}
