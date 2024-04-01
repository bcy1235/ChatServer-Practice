package dummyClient;

import utils.MessageResolver;
import utils.Resolver;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DummyThread implements Runnable {
    private final String SERVER_IP;
    private final int SERVER_PORT;
    private final int BUF_SIZE;
    private final int SENDING_RATE;
    private final int CLIENT_PORT;
    private final int threadNum;

    DummyThread(String serverIp, int serverPort, int bufSize, int sendingRate, int clientPort, int threadNum) {
        this.SERVER_IP = serverIp;
        this.SERVER_PORT = serverPort;
        this.BUF_SIZE = bufSize;
        this.SENDING_RATE = sendingRate;
        this.CLIENT_PORT = clientPort;
        this.threadNum = threadNum;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.bind(new InetSocketAddress(CLIENT_PORT));
            socketChannel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            socketChannel.configureBlocking(false);

            Thread writing = new Thread(new WritingThread(socketChannel, BUF_SIZE, SENDING_RATE, threadNum));
//            Thread reading = new Thread(new ReadingThread(socketChannel, BUF_SIZE, threadNum));

            writing.start();
//            reading.start();

            writing.join();
//            reading.join();
        } catch (IOException e) {
            System.out.println("MainClient fail to create socket! : " + e);
        } catch (InterruptedException e) {
            System.out.println("MainClient get interrupted! : " + e);
        }
    }

    static class ReadingThread implements Runnable {
        public ByteBuffer readBuffer;
        private final int SCREEN_SIZE_WIDTH;
        private final int SCREEN_SIZE_HEIGHT;
        private final int BASICGUI_WIDTH;
        private final int BASICGUI_HEIGHT;
        private SocketChannel socketChannel;
        private final Resolver resolver = new MessageResolver();
        private final int threadNum;

        public ReadingThread(SocketChannel socketChannel, int bufSize, int threadNum) {
            this.readBuffer = ByteBuffer.allocate(bufSize);
            this.SCREEN_SIZE_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
            this.SCREEN_SIZE_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
            this.BASICGUI_WIDTH = 300;
            this.BASICGUI_HEIGHT = 300;
            this.socketChannel = socketChannel;
            this.threadNum = threadNum;
        }

        @Override
        public void run() {
            TextArea textArea = createBasicGUI();

            while (true) {
                messageResolve(textArea);
            }
        }

        public TextArea createBasicGUI() {
            JFrame jFrame = new JFrame();
            jFrame.setSize(BASICGUI_WIDTH, BASICGUI_HEIGHT);
            // client gui's location is auto, because it is too hard to control gui when invoke many thread
            jFrame.setLocationByPlatform(true);

            TextArea textArea = new TextArea();
            JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            jFrame.add(scrollPane);
            jFrame.doLayout();
            jFrame.setVisible(true);
            return textArea;
        }

        public void messageResolve(TextArea textArea) {
            try {
                int readBytes = socketChannel.read(readBuffer);
                if (readBuffer.position() < 4)
                    return;

                int messageLen = ((readBuffer.get(2) & 0xFF) << 8) | (readBuffer.get(3) & 0xFF);
                if (readBuffer.position() < messageLen + 4)
                    return;

                readBuffer.clear();

              renderingMessage(textArea, messageLen);
            } catch (IOException e) {
                System.out.println("MainClient's readThread's messageResolve method : " + e);
            }
        }

        public void renderingMessage(TextArea textArea, int messageLen) {
            ByteBuffer write = resolver.resolve(readBuffer.flip(), messageLen);

            byte[] array = write.array();
            int messageOwner = ((array[0] & 0xFF) << 8) + (array[1] & 0xFF);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Thread" + messageOwner + ": ");
            for (int i = 4; i < messageLen + 4; i++) {
                stringBuilder.append((char) array[i]);
            }
            stringBuilder.append('\n');
            textArea.append(stringBuilder.toString());
    }
}

static class WritingThread implements Runnable {
    public ByteBuffer writeBuffer;
    private SocketChannel socketChannel;
    private final int BYTE_SEC;
    private final int THREAD_NUM;

    public WritingThread(SocketChannel socketChannel, int bufSize, int byteSec, int threadNum) {
        this.socketChannel = socketChannel;
        this.writeBuffer = ByteBuffer.allocate(bufSize);
        this.BYTE_SEC = byteSec;
        this.THREAD_NUM = threadNum;
    }

    @Override
    public void run() {
        try {
            int remainBytes = BYTE_SEC;
            long destTime = System.currentTimeMillis() + 200;
            while (true) {
                if (System.currentTimeMillis() > destTime) {
                    destTime += 200;
                    remainBytes = BYTE_SEC;
                }
                if (remainBytes > 0) {
                    int size = fillBuffer(remainBytes);
                    writeBuffer.flip();

                    int writeByte = 0;
                    while (writeByte < size) {
                        writeByte += socketChannel.write(writeBuffer);
                    }
                    writeBuffer.clear();

                    remainBytes -= size;
                }
            }
        } catch (IOException e) {
            System.out.println("Writing Thread throw Exception! : " + e);
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
        writeBuffer.put((byte) (THREAD_NUM >> 8));
        writeBuffer.put((byte) (THREAD_NUM));
        writeBuffer.put((byte) (messageLen >> 8));
        writeBuffer.put((byte) (messageLen));
    }
}
}
