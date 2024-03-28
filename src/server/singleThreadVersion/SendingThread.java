package server.singleThreadVersion;


import server.utils.MessageResolver;
import server.utils.Resolver;
import server.utils.SocketBuffer;
import server.utils.SocketStation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * thread which will be sending message for all SocketChannel
 */
public class SendingThread implements Runnable {
    private final Resolver messageResolver = new MessageResolver();
    @Override
    public void run() {
        while (true) {
            Set<SelectionKey> validKey = SocketStation.getValidKey();
            if (validKey == null)
                continue;

            Iterator<SelectionKey> iterator = validKey.iterator();
            while (iterator.hasNext()) {
                SocketChannel socketChannel = (SocketChannel) iterator.next().channel();

                ByteBuffer message = readingMessage(socketChannel);
                if (message == null)
                    continue;

                sendingMessage(socketChannel, message);
            }
        }
    }

    public ByteBuffer readingMessage(SocketChannel socketChannel) {
        ByteBuffer buffer = SocketBuffer.getBuffer(socketChannel);
        try {
            int readBytes = 0;
            while (readBytes < 2) {
                int readSize = socketChannel.read(buffer);

                // skip to reading 2 bytes procedure
                if (readSize == 0 && buffer.position() > 1) {
                    break;
                } else if (readSize == -1) {
                    closeAll(socketChannel);
                    return null;
                }
                readBytes += readSize;
            }

            // Message's first two bytes represent message's payload size
            int messageSize = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
            while (readBytes < messageSize + 2) {
                int readSize = socketChannel.read(buffer);

                // skip to reading message payload
                if (readSize == 0 && buffer.position() > 1) {
                    break;
                } else if (readSize == -1) {
                    closeAll(socketChannel);
                    return null;
                }
                readBytes += readSize;
            }
            return messageResolver.resolve(buffer.flip(), messageSize);
        } catch (IOException e) {
            // have to change with logger
            System.out.println("SendingThread readingMessage method : " + e);
            closeAll(socketChannel);
        }
        return null;
    }

    public void sendingMessage(SocketChannel socketChannel, ByteBuffer message) {
        try {
            Iterator<SocketChannel> iterator = SocketStation.getSocketList().iterator();
            while (iterator.hasNext()) {
                iterator.next().write(message);
                message.rewind();
            }
        } catch (IOException e) {
            // have to change with logger
            System.out.println("SendingThread sendingMessage method : " + e);
        }
    }

    public void closeAll(SocketChannel socketChannel) {
        SocketStation.delete(socketChannel);
        SocketBuffer.delete(socketChannel);
        try {
            socketChannel.close();
        } catch (IOException e) {
            // have to change with logger
            System.out.println("SendingThread closeAll method : " + e);
        }
    }
}
