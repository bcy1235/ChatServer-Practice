package dummyClient;
public class DummyProcess {
    private static final String SERVER_IP = "192.168.35.38";
    private static final int SERVER_PORT = 40000;
    private static final int THREAD_NUM = 150;
    private static final int BUF_SIZE = 5000;
    // sending rate per each thread
    private static final int sendingRate = 100;
    public static void main(String[] args) throws InterruptedException {
        int processNumber = Integer.parseInt(args[0]);
        for (int i = processNumber * THREAD_NUM; i < processNumber * THREAD_NUM + THREAD_NUM; i++) {
            new Thread(new DummyThread(SERVER_IP, SERVER_PORT, BUF_SIZE, sendingRate, 15000 + i, i)).start();
        }
    }
}
