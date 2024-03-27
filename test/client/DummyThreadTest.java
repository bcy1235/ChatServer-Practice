package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

class DummyThreadTest {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 54321;
    private static final int BUF_SIZE = 1027;
    private static final int BYTE_SEC = 100;
    @Test
    public void testFillBuffer() throws IOException {
        DummyThread.WritingThread writingThread = new DummyThread.WritingThread(null, BUF_SIZE, BYTE_SEC);

        int testLen = 1024;
        int len = writingThread.fillBuffer(testLen);
        for (int i = 2; i < testLen + 2; i++) {
            System.out.print((char) writingThread.writeBuffer.get(i));
        }

        Assertions.assertEquals(testLen, len);
    }
    @Test
    public void testMessageResolver() throws IOException {
        DummyThread.ReadingThread readingThread = new DummyThread.ReadingThread(null, BUF_SIZE);
        DummyThread.WritingThread writingThread = new DummyThread.WritingThread(null, BUF_SIZE, BYTE_SEC);


    }
}