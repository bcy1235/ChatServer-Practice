package server.singleThreadVersion;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * class, which has map, links SocketChannel and ByteBuffer
 */
public class SocketBuffer {
    private static Map<SocketChannel, ByteBuffer> map = new HashMap<>();

    /**
     * method which is used for inserting new socketChannel to the SocketBuffer
     * @param socketChannel key value
     * @param bufSize ByteBuffer's size which owned by SocketChannel
     */
    public static synchronized void insert(SocketChannel socketChannel, int bufSize) {
        map.put(socketChannel, ByteBuffer.allocate(bufSize));
    }

    /**
     * you can get ByteBuffer which is socketChannel's own
     * @param socketChannel owner of ByteBuffer
     * @return socketChannel's ByteBuffer
     */
    public static synchronized ByteBuffer getBuffer(SocketChannel socketChannel) {
        return map.get(socketChannel);
    }

    /**
     * method which is used for deleting socketChannel from SocketBuffer
     * @param socketChannel socketChannel which will be deleted from SocketBuffer
     * @return if socketChannel is in the SocketBuffer, return true, else false
     */
    public static synchronized boolean delete(SocketChannel socketChannel) {
        if (map.remove(socketChannel) != null)
            return true;
        else
            return false;
    }
}
