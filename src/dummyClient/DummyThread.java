package dummyClient;

import server.multiThreadVersion.SocketStation;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.PrimitiveIterator;
import java.util.WeakHashMap;

public class DummyThread implements Runnable {
    private final String SERVER_IP;
    private final int SERVER_PORT;
    //    private final int BUF_SIZE;
    private final int SENDING_RATE;
    private final int CLIENT_PORT;
    private final int THREAD_NUM;
    private final int BUF_SIZE;
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;

    DummyThread(String serverIp, int serverPort, int bufSize, int sendingRate, int clientPort, int threadNum) {
        this.SERVER_IP = serverIp;
        this.SERVER_PORT = serverPort;
        this.SENDING_RATE = sendingRate;
        this.CLIENT_PORT = clientPort;
        this.THREAD_NUM = threadNum;
        this.BUF_SIZE = bufSize;
        writeBuffer = ByteBuffer.allocate(200);
        readBuffer = ByteBuffer.allocate(bufSize);
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            socketChannel.bind(new InetSocketAddress(CLIENT_PORT));
            socketChannel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

            while (!socketChannel.finishConnect()) {

            }

            fillBuffer(SENDING_RATE);
            writeBuffer.flip();
            while (true) {
                while (writeBuffer.hasRemaining()) {
                    socketChannel.write(writeBuffer);
                }
                writeBuffer.rewind();

                socketChannel.read(readBuffer);
                readBuffer.clear();

                Thread.sleep(1000);
            }
//            Thread writing = new Thread(new WritingThread(socketChannel, BUF_SIZE, SENDING_RATE, THREAD_NUM));
//            Thread reading = new Thread(new ReadingThread(socketChannel, BUF_SIZE, THREAD_NUM));
//
//            writing.start();
//            reading.start();
//
//            writing.join();
//            reading.join();

        } catch (IOException e) {
            System.out.println("DummyThread fail to create socket! : " + e);
        } catch (InterruptedException e) {
            System.out.println("DummyThread run method : " + e);
        }
    }

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

//    static class ReadingThread implements Runnable {
//        public ByteBuffer readBuffer;
//        private SocketChannel socketChannel;
//        private final int threadNum;
//
//        public ReadingThread(SocketChannel socketChannel, int bufSize, int threadNum) {
//            this.readBuffer = ByteBuffer.allocate(bufSize);
//            this.socketChannel = socketChannel;
//            this.threadNum = threadNum;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    socketChannel.read(readBuffer);
//                } catch (IOException e) {
//                    System.out.println("Reading run method : " + e);
//                }
//                readBuffer.clear();
//
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    System.out.println("DummyThread ReadingThread run method : " + e);
//                }
//            }
//        }
//    }
//
//
//    static class WritingThread implements Runnable {
//        public ByteBuffer writeBuffer;
//        private SocketChannel socketChannel;
//        private final int SENDING_RATE;
//        private final int THREAD_NUM;
//
//        public WritingThread(SocketChannel socketChannel, int bufSize, int byteSec, int threadNum) {
//            this.socketChannel = socketChannel;
//            this.writeBuffer = ByteBuffer.allocate(bufSize);
//            this.SENDING_RATE = byteSec;
//            this.THREAD_NUM = threadNum;
//        }
//
//        @Override
//        public void run() {
//            try {
//                fillBuffer(SENDING_RATE);
//                writeBuffer.flip();
//                while (true) {
//                    while (writeBuffer.hasRemaining()) {
//                        socketChannel.write(writeBuffer);
//                    }
//                    writeBuffer.rewind();
//
//                    synchronized (Thread.currentThread()) {
//                        Thread.sleep(1000);
//                    }
//                }
//            } catch (IOException e) {
//                System.out.println("Writing Thread : " + e);
//            } catch (InterruptedException e) {
//                System.out.println("Writing Thread : " + e);
//            }
//        }
//
//        // Message protocol
//        // First two bytes => thread's number(ID) / next two bytes => message payload length / else => message payload
//        public int fillBuffer(int messageLen) {
//            fillHeader(messageLen);
//
//            byte tmpChar = (byte) (Math.random() * ('z' - 'A') + 'A');
//            for (int i = 0; i < messageLen; i++) {
//                writeBuffer.put(tmpChar);
//            }
//            return messageLen;
//        }
//
//        // Fill message Header(thread id, messageLen) in writeBuffer
//        public void fillHeader(int messageLen) {
//            writeBuffer.put((byte) (messageLen >> 8));
//            writeBuffer.put((byte) (messageLen));
//            writeBuffer.put((byte) (THREAD_NUM >> 8));
//            writeBuffer.put((byte) (THREAD_NUM));
//        }
//    }
}
