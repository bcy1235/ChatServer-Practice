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

                sendingMessage(message);
            }
        }
    }

    public ByteBuffer readingMessage(SocketChannel socketChannel) {
        ByteBuffer buffer = SocketBuffer.getBuffer(socketChannel);
        try {
            int readBytes = 0;
            while (readBytes < 4) {
                int readSize = socketChannel.read(buffer);

                // skip to reading 4 bytes procedure
                if (buffer.position() == buffer.limit()) {
                    break;
                } else if (readSize == -1) {
                    closeAll(socketChannel);
                    return null;
                }
                readBytes += readSize;
            }

            // Message's 3rd and 4th bytes represent message's payload size
            int messageSize = ((buffer.get(2) & 0xFF) << 8) | (buffer.get(3) & 0xFF);
            while (readBytes < messageSize + 4) {
                int readSize = socketChannel.read(buffer);

                // skip to reading message payload
                if (buffer.position() == buffer.limit()) {
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

    public void sendingMessage(ByteBuffer message) {
        try {
            Iterator<SocketChannel> iterator = SocketStation.getSocketList().iterator();
            while (iterator.hasNext()) {
                SocketChannel socketChannel = iterator.next();

                int writeBytes = 0;
                while (writeBytes < message.limit())
                    writeBytes += socketChannel.write(message);

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
