package client;
public class DummyProcess {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 54321;
    private static final int THREAD_NUM = 5;
    private static final int BUF_SIZE = 1027;
    // byte/sec : message sending rate per each DummyThread
    private static final int BYTE_SEC = 100;
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < THREAD_NUM; i++) {
            new Thread(new DummyThread(SERVER_IP, SERVER_PORT, BUF_SIZE, BYTE_SEC)).start();
        }

        synchronized (Thread.currentThread()) {
            Thread.currentThread().wait();
        }
    }
}
