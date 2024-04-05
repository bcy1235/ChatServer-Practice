package client;

import utils.Timer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * class used for checking server performance
 */
public class MainClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 40000;
    private static final int BUF_SIZE = 5000;
    private static final int SENDING_RATE = 30;
    private static final int THREAD_NUM = 55555;
    private static final int INTERVAL = 1000;

    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.bind(new InetSocketAddress(35353));
            socketChannel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            socketChannel.configureBlocking(false);


            Thread writing = new Thread(new WritingThread(socketChannel, BUF_SIZE, SENDING_RATE, THREAD_NUM, INTERVAL));
            Thread reading = new Thread(new ReadingThread(socketChannel, BUF_SIZE, THREAD_NUM));

            writing.start();
            reading.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Timer.makeResultFile();
            }));
        } catch (IOException e) {
            System.out.println("MainClient fail to create socket! : " + e);
        }
    }


    static class ReadingThread implements Runnable {
        public ByteBuffer readBuffer;
        private SocketChannel socketChannel;
        private final int THREAD_NUM;
        private StringBuilder stringBuilder = new StringBuilder();

        public ReadingThread(SocketChannel socketChannel, int bufSize, int threadNum) {
            this.readBuffer = ByteBuffer.allocate(bufSize);
            this.socketChannel = socketChannel;
            this.THREAD_NUM = threadNum;
        }

        @Override
        public void run() {
            messageResolve();
        }

        public void messageResolve() {
            try {
                while (true) {
                    socketChannel.read(readBuffer);
                    if (readBuffer.position() < 4)
                        continue;

                    readBuffer.flip();
                    byte front = readBuffer.get();
                    byte back = readBuffer.get();
                    readBuffer.compact();

                    int messageLen = ((front & 0xFF) << 8) | (back & 0xFF);
                    if (readBuffer.position() < messageLen + 2) {
                        readBuffer.rewind();
                        continue;
                    }

                    readBuffer.flip();
                    renderingMessage(messageLen);
                }
            } catch (IOException e) {
                System.out.println("MainClient's readThread's messageResolve method : " + e);
            }
        }

        public void renderingMessage(int messageLen) {
            byte front = readBuffer.get();
            byte back = readBuffer.get();
            int messageOwner = ((front & 0xFF) << 8) + (back & 0xFF);
            if (messageOwner == THREAD_NUM) {
                // time check
                Timer.timerStop();
            }

            stringBuilder.append("Thread" + messageOwner + ": ");
            for (int i = 4; i < messageLen + 4; i++) {
                stringBuilder.append((char) readBuffer.get());
            }
            readBuffer.compact();
            stringBuilder.append('\n');
            System.out.print(stringBuilder.toString());
            stringBuilder.setLength(0);
        }
    }

    static class WritingThread implements Runnable {
        public ByteBuffer writeBuffer;
        private SocketChannel socketChannel;
        private final int SENDING_RATE;
        private final int THREAD_NUM;
        private final int INTERVAL;


        public WritingThread(SocketChannel socketChannel, int bufSize, int byteSec, int threadNum, int interval) {
            this.socketChannel = socketChannel;
            this.writeBuffer = ByteBuffer.allocate(bufSize);
            this.SENDING_RATE = byteSec;
            this.THREAD_NUM = threadNum;
            this.INTERVAL = interval;
        }

        @Override
        public void run() {
            try {
                fillBuffer(SENDING_RATE);
                writeBuffer.flip();
                while (true) {
                    Timer.timerStart();
                    while (writeBuffer.hasRemaining()) {
                        socketChannel.write(writeBuffer);
                    }
                    writeBuffer.rewind();

                    synchronized (Thread.currentThread()) {
                        Thread.sleep(INTERVAL);
                    }
                }
            } catch (IOException e) {
                System.out.println("Writing Thread : " + e);
            } catch (InterruptedException e) {
                System.out.println("Writing Thread : " + e);
            }
        }

        // Message protocol
        // First two bytes => thread's number(ID) / next two bytes => message payload length / else => message payload
        public int fillBuffer(int messageLen) {
            fillHeader(messageLen);

            byte tmpChar = (byte) (Math.random() * ('z' - 'A') + 'A');
            for (int i = 0; i < messageLen; i++) {
                writeBuffer.put(tmpChar);
            }
            return messageLen;
        }

        // Fill message Header(thread id, messageLen) in writeBuffer
        public void fillHeader(int messageLen) {
            writeBuffer.put((byte) (messageLen >> 8));
            writeBuffer.put((byte) (messageLen));
            writeBuffer.put((byte) (THREAD_NUM >> 8));
            writeBuffer.put((byte) (THREAD_NUM));
        }
    }
}
