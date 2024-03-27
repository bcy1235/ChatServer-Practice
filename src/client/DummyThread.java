package client;

import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class DummyThread implements Runnable {
    private final String SERVER_IP;
    private final int SERVER_PORT;
    private final int BUF_SIZE;
    private final int BYTE_SEC;
    private final int CLIENT_PORT;

    DummyThread(String serverIp, int serverPort, int bufSize, int byteSec, int clientPort) {
        this.SERVER_IP = serverIp;
        this.SERVER_PORT = serverPort;
        this.BUF_SIZE = bufSize;
        this.BYTE_SEC = byteSec;
        this.CLIENT_PORT = clientPort;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.bind(new InetSocketAddress(SERVER_IP, CLIENT_PORT));
            socketChannel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            socketChannel.configureBlocking(false);

            Thread writing = new Thread(new WritingThread(socketChannel, BUF_SIZE, BYTE_SEC));
            Thread reading = new Thread(new ReadingThread(socketChannel, BUF_SIZE));


            writing.start();
            reading.start();

            writing.join();
            reading.join();
        } catch (IOException e) {
            System.out.println("DummyThread fail to create socket! : " + e);
        } catch (InterruptedException e) {
            System.out.println("DummyThread get interrupted! : " + e);
        }
    }

    static class ReadingThread implements Runnable {
        public ByteBuffer readBuffer;
        private final int SCREEN_SIZE_WIDTH;
        private final int SCREEN_SIZE_HEIGHT;
        private final int BASICGUI_WIDTH;
        private final int BASICGUI_HEIGHT;
        private SocketChannel socketChannel;

        public ReadingThread(SocketChannel socketChannel, int bufSize) {
            this.readBuffer = ByteBuffer.allocate(bufSize);
            this.SCREEN_SIZE_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
            this.SCREEN_SIZE_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
            this.BASICGUI_WIDTH = 300;
            this.BASICGUI_HEIGHT = 300;
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
                TextArea textArea = createBasicGUI();

                while (true) {
                    messageResolve(textArea);
                }
            } catch (IOException e) {
                System.out.println("Reading Thread Throw Exception! : " + e);
            }

        }

        public TextArea createBasicGUI() {
            JFrame jFrame = new JFrame();
            jFrame.setSize(BASICGUI_WIDTH, BASICGUI_HEIGHT);
            jFrame.setLocationByPlatform(true);

            TextArea textArea = new TextArea();
            JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            jFrame.add(scrollPane);
            jFrame.doLayout();
            jFrame.setVisible(true);
            return textArea;
        }

        public void messageResolve(TextArea textArea) throws IOException {
            int readByte = 0;
            while (readByte < 2) {
                int readSize = socketChannel.read(readBuffer);
                if (readSize == 0 && readBuffer.position() > 1)
                    break;
                readByte += readSize;
            }
            readBuffer.flip();

            byte front = readBuffer.get();
            byte back = readBuffer.get();
            int messageLen = ((front & 0xFF) << 8) | (back & 0xFF);
            readBuffer.compact();
            while (readByte < messageLen + 2) {
                int readSize = socketChannel.read(readBuffer);
                if (readSize == 0)
                    break;
                readByte += socketChannel.read(readBuffer);
            }
            readBuffer.flip();

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < messageLen; i++) {
                stringBuilder.append((char) readBuffer.get());
            }
            textArea.append(stringBuilder.append('\n').toString());
            readBuffer.compact();
        }
    }

    static class WritingThread implements Runnable {
        public ByteBuffer writeBuffer;
        private SocketChannel socketChannel;
        private final int BYTE_SEC;

        public WritingThread(SocketChannel socketChannel, int bufSize, int byteSec) {
            this.socketChannel = socketChannel;
            this.writeBuffer = ByteBuffer.allocate(bufSize);
            this.BYTE_SEC = byteSec;
        }

        @Override
        public void run() {
            try {
                int remainBytes = BYTE_SEC;
                long destTime = System.currentTimeMillis() + 1000;
                while (true) {
                    if (System.currentTimeMillis() > destTime) {
                        destTime += 1000;
                        remainBytes = BYTE_SEC;
                    }
                    if (remainBytes > 0) {
                        int size = fillBuffer((int) (Math.random() * remainBytes));
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

        public int fillBuffer(int len) {
            writeBuffer.put((byte) (len >> 8));
            writeBuffer.put((byte) (len));

            byte tmpChar = (byte) (Math.random() * ('z' - 'A') + 'A');
            for (int i = 2; i < len + 2; i++) {
                writeBuffer.put(tmpChar);
            }
            return ((writeBuffer.get(0) & 0xFF) << 8) | (writeBuffer.get(1) & 0xFF);
        }
    }
}
