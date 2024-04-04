package server.newMultiThreadVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable {
    private SocketRoom socketRoom;
    private ConcurrentLinkedQueue<ByteBuffer> messageQueue;

    public Writer(SocketRoom socketRoom, ConcurrentLinkedQueue<ByteBuffer> queue) {
        this.socketRoom = socketRoom;
        this.messageQueue = queue;
    }

    @Override
    public void run() {
        while (true) {
            ByteBuffer byteBuffer = null;
            byteBuffer = messageQueue.poll();
            if (byteBuffer == null) {
                continue;
            }
            sendingMessage(byteBuffer);
        }
    }

    public void sendingMessage(ByteBuffer message) {
        if (message == null)
            return;
        List<SocketChannel> socketChannelList;
        socketChannelList = socketRoom.getSocketList();

        Iterator<SocketChannel> iterator = socketChannelList.iterator();
        while (iterator.hasNext()) {
            SocketChannel socketChannel = iterator.next();
            int writeBytes = 0;
            while (writeBytes < message.limit()) {
                try {
                    writeBytes += socketChannel.write(message);
                } catch (IOException e) {
                    closeAll(socketChannel);
                    System.out.println("Writing sendingMessage method : " + e);
                    break;
                }
            }
            message.rewind();
        }
    }

    public void closeAll(SocketChannel socketChannel) {
        synchronized (socketRoom) {
            socketRoom.delete(socketChannel);
        }
    }
}
