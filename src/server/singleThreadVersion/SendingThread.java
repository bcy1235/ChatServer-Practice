package server.singleThreadVersion;


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
    @Override
    public void run() {
        while (true) {
            Set<SelectionKey> validKey = SocketStation.getValidKey();
            if (validKey == null)
                continue;

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
                }
                else if (buffer.position() < 4)
                    return;

                // Message's 1st and 2nd bytes represent message's payload size
                int messageSize = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
                if (buffer.position() < messageSize + 4)
                    return;

                buffer.flip();
                buffer.get(bytes = new byte[messageSize + 4], 0, messageSize + 4);
                buffer.compact();

                sendingMessage(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                // have to change with logger
                System.out.println("SendingThread readingMessage method : " + e);
                closeAll(socketChannel);
                return;
            }
        }
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
            System.out.println("SendingThread sendingMessage method : " + e);
        }
    }

    public void closeAll(SocketChannel socketChannel) {
        SocketStation.delete(socketChannel);
        SocketBuffer.delete(socketChannel);
        try {
            socketChannel.close();
        } catch (IOException e) {
            System.out.println("SendingThread closeAll method : " + e);
        }
    }
}
