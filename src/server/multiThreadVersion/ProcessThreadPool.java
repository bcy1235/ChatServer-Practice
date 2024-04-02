package server.multiThreadVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ProcessThreadPool {
    private static Thread[] threads;
    private static int THREAD_NUM;

    static {
        THREAD_NUM = 5;
        threads = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            (threads[i] = new Thread(new Processing())).start();
        }
    }
    public static void wakeUp() {
        for (Thread thread : threads) {
            if (thread.getState() == Thread.State.WAITING) {
                synchronized (thread) {
                    thread.notify();
                }
            }
        }
    }
    static class Processing implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        System.out.println("Processing run method : " + e);
                    }
                }
                ByteBuffer task = TaskQueue.getTask();
                if (task == null)
                    continue;

                sendingMessage(task);
            }
        }

        public void sendingMessage(ByteBuffer message) {
            try {
                Iterator<SocketChannel> iterator = SocketStation.getSocketList().iterator();
                while (iterator.hasNext()) {
                    SocketChannel socketChannel = iterator.next();

                    int writeBytes = 0;
                    while (writeBytes < message.limit())
                        writeBytes += socketChannel.write(message);

                    message.rewind();
                }
            } catch (IOException e) {
                System.out.println("Sending sendingMessage method : " + e);
            }
        }
    }
}
