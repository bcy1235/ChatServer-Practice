package server.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * interface which helps resolve buffer's contents
 */
public interface Resolver {
    ByteBuffer resolve(ByteBuffer buffer, int messageLen);
}
