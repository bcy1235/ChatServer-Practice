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
    private final int SENDING_RATE;
    private final int CLIENT_PORT;
    private final int THREAD_NUM;
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;

    DummyThread(String serverIp, int serverPort, int bufSize, int sendingRate, int clientPort, int threadNum) {
        this.SERVER_IP = serverIp;
        this.SERVER_PORT = serverPort;
        this.SENDING_RATE = sendingRate;
        this.CLIENT_PORT = clientPort;
        this.THREAD_NUM = threadNum;
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
}
