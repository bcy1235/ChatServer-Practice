package client;
public class DummyProcess {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 40000;
    private static final int THREAD_NUM = 5;
    private static final int BUF_SIZE = 15_000;
    // byte/sec : message sending rate per each DummyThread
    private static final int BYTE_SEC = 300;
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < THREAD_NUM; i++) {
            new Thread(new DummyThread(SERVER_IP, SERVER_PORT, BUF_SIZE, BYTE_SEC, 12354 + i, i)).start();
        }

        synchronized (Thread.currentThread()) {
            Thread.currentThread().wait();
        }
    }
}
