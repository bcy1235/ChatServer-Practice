package server.newMultiThreadVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Reader implements Runnable {
    private SocketRoom socketRoom;
    private ConcurrentLinkedQueue<byte[]> queue;
    public Reader(SocketRoom room, ConcurrentLinkedQueue concurrentLinkedQueue) {
        socketRoom = room;
        queue = concurrentLinkedQueue;
    }

    @Override
    public void run() {
        while (true) {
            Set<SelectionKey> validKey;
            validKey = socketRoom.getValidKey();
            if (validKey == null) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    System.out.println("Reader run method : " + e);
                }
                continue;
            }

            Iterator<SelectionKey> iterator = validKey.iterator();
            while (iterator.hasNext()) {
                SocketChannel socketChannel = (SocketChannel) iterator.next().channel();
                iterator.remove();

                processingMessage(socketChannel);
            }
        }
    }

    public void processingMessage(SocketChannel socketChannel) {
        ByteBuffer buffer = SocketBuffer.getBuffer(socketChannel);
        byte[] bytes;
        while (true) {
            try {
                int readBytes = socketChannel.read(buffer);
                if (readBytes == -1) {
                    closeAll(socketChannel);
                    return;
                } else if (buffer.position() < 4)
                    return;

                // Message's 1st and 2nd bytes represent message's payload size
                int messageSize = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
                if (buffer.position() < messageSize + 4)
                    return;

                buffer.flip();
                buffer.get(bytes = new byte[messageSize + 4], 0, messageSize + 4);
                buffer.compact();

                queue.add(bytes);
            } catch (IOException e) {
                System.out.println("Reader processingMessage method : " + e);
                closeAll(socketChannel);
                return;
            }
        }
    }

    public void closeAll(SocketChannel socketChannel) {
        synchronized (socketRoom) {
            socketRoom.delete(socketChannel);
        }
        SocketBuffer.delete(socketChannel);
        try {
            socketChannel.close();
        } catch (IOException e) {
            System.out.println("Reader fail to close socketChannel : " + e);
        }
    }
}
