package dummyClient;
public class DummyProcess {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 40000;
    private static final int THREAD_NUM = 5;
    private static final int BUF_SIZE = 5000;
    // byte/sec : message sending rate per each thread
    private static final int BYTE_SEC = 1024;
    public static void main(String[] args) throws InterruptedException {
        int processNumber = Integer.parseInt(args[0]);
        for (int i = processNumber * THREAD_NUM; i < processNumber * THREAD_NUM + THREAD_NUM; i++) {
            new Thread(new DummyThread(SERVER_IP, SERVER_PORT, BUF_SIZE, BYTE_SEC, 15000 + i, i)).start();
        }

        synchronized (Thread.currentThread()) {
            Thread.currentThread().wait();
        }
    }
}