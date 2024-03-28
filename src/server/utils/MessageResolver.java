package server.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * class which helps resolve message
 */
public class MessageResolver implements Resolver {
    @Override
    public ByteBuffer resolve(ByteBuffer buffer, int messageLen) {
        int actualLength = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
        if (actualLength != messageLen) {
            // have to change with logger
            System.out.println("MessageResolver resolve method : " + "Invalid Message!! " + Thread.currentThread().getStackTrace());
            return null;
        }

        byte[] bytes = new byte[messageLen + 2];
        for (int i = 0; i < messageLen + 2; i++) {
            bytes[i] = buffer.get();
        }
        buffer.compact();
        return ByteBuffer.wrap(bytes);
    }
}
