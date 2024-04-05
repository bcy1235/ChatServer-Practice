package server.multiThreadVersion;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * TaskQueue manages socketChannel detected by Sending Thread
 */
public class TaskQueue {
    private static Queue<ByteBuffer> queue = new LinkedList<>();

    /**
     * @param buffer detected and have to be processed
     */
    public static synchronized void register(ByteBuffer buffer) {
        queue.add(buffer);
    }

    /**
     * @return ByteBuffer detected by Sending
     */
    public static synchronized ByteBuffer getTask() {
        if (queue.isEmpty())
            return null;
        else
            return queue.poll();
    }

}
