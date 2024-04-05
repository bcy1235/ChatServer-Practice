package server.newMultiThreadVersion;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WritingBox {
    private static List<SocketRoom> socketRoomList;
    private static ConcurrentLinkedDeque<Queue<ByteBuffer>> messageQueueList;
    private static int ROOM_SIZE;

    static {
        socketRoomList = new LinkedList<>();
        messageQueueList = new ConcurrentLinkedDeque<>();
        ROOM_SIZE = 30;
    }

    public static void push(byte[] message) {
        Iterator<Queue<ByteBuffer>> iterator = messageQueueList.iterator();
        while (iterator.hasNext()) {
            iterator.next().add(ByteBuffer.wrap(message));
        }
    }

    public static void register(SocketChannel socketChannel) {
        if (socketRoomList.isEmpty()) {
            createNewRoom(socketChannel);
        } else {
            Iterator<SocketRoom> iterator = socketRoomList.iterator();
            while (iterator.hasNext()) {
                SocketRoom room = iterator.next();
                synchronized (room) {
                    if (room.isFull())
                        continue;

                    room.register(socketChannel);
                }
                return;
            }
            // 소켓 룸이 모두 꽉찬 경우
            createNewRoom(socketChannel);
        }
    }

    private static void createNewRoom(SocketChannel socketChannel) {
        SocketRoom room = new SocketRoom(ROOM_SIZE);
        room.register(socketChannel);
        socketRoomList.add(room);

        ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
        messageQueueList.add(queue);
        Thread writingThread = new Thread(new Writer(room, queue));
//        threadList.add(readingThread);
        writingThread.start();
    }
}
