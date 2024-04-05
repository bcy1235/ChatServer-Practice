package dummyClient;
public class DummyProcess {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 40000;
    private static final int THREAD_NUM = 50;
    private static final int BUF_SIZE = 30000;
    // sending rate per each thread
    private static final int SENDING_RATE = 100;
    // (ms)
    private static final int INTERVAL = 1000;
    public static void main(String[] args) throws InterruptedException {
        int processNumber = Integer.parseInt(args[0]);
        for (int i = processNumber * THREAD_NUM; i < processNumber * THREAD_NUM + THREAD_NUM; i++) {
            Thread thread = new Thread(new DummyThread(SERVER_IP, SERVER_PORT, BUF_SIZE, SENDING_RATE, 20000 + i, i));
            while (thread.getState() == Thread.State.NEW) {
                thread.start();
            }
        }
    }
}
