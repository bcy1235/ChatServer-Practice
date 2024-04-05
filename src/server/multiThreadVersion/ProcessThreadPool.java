package server.multiThreadVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

public class ProcessThreadPool {
    private static Thread[] threads;
    private static int THREAD_NUM;

    public static void initialize() {
        THREAD_NUM = 10;
        threads = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            (threads[i] = new Thread(new Processing())).start();
        }
    }

    static class Processing implements Runnable {
        @Override
        public void run() {
            while (true) {
                ByteBuffer task = TaskQueue.getTask();
                if (task == null)
                    continue;

                sendingMessage(task);
            }
        }

        public void sendingMessage(ByteBuffer message) {
            try {
                List<SocketChannel> list = SocketStation.getSocketList();
                if (list == null) {
                    return;
                }

                Iterator<SocketChannel> iterator = list.iterator();
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
