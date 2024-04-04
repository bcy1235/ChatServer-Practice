package server.newMultiThreadVersion;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReadingBox {
    private static List<SocketRoom> socketRoomList;
    private static ConcurrentLinkedDeque<ConcurrentLinkedQueue<byte[]>> queueConcurrentLinkedDeque;
    //    private static List<Thread> threadList;
    private static int ROOM_SIZE;

    static {
        socketRoomList = new LinkedList<>();
        queueConcurrentLinkedDeque = new ConcurrentLinkedDeque<>();
//        threadList = new LinkedList<>();
        ROOM_SIZE = 1000;
    }

    public static void getMessage(ConcurrentLinkedQueue concurrentLinkedQueue) {
        Iterator<ConcurrentLinkedQueue<byte[]>> iterator = queueConcurrentLinkedDeque.iterator();
        while (iterator.hasNext()) {
            byte[] bytes = iterator.next().poll();
            if (bytes == null)
                continue;
            concurrentLinkedQueue.add(bytes);
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

        ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
        queueConcurrentLinkedDeque.add(queue);
        Thread readingThread = new Thread(new Reader(room, queue));
//        threadList.add(readingThread);
        readingThread.start();
    }
}
