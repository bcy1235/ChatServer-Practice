package server.utils;


import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * class which is used for managing SocketChannel
 */
public class SocketStation {
    private static Object stationLock = new Object();
    private static Selector selector;
    private static List<SocketChannel> list;

    static {
        try {
            selector = Selector.open();
            list = new LinkedList<>();
        } catch (IOException e) {
            // have to change with logger
            System.out.println("SocketStation static block : " + e);
        }
    }

    /**
     * method which is used for register socketChannel to the Selector
     * @param socketChannel which is registered to Selector
     */
    public static void register(SocketChannel socketChannel) {
        synchronized (stationLock) {
            try {
                socketChannel.register(selector, SelectionKey.OP_READ);
                list.add(socketChannel);
            } catch (ClosedChannelException e) {
                // have to change with logger
                System.out.println("SocketStation register method : " + e);
            }
        }
    }

    /**
     * you can get set of keys which contain valid SocketChannel's.
     * <br> valid mean interest set defined when register SocketChannel to Selector is detected
     * @return valid Selection Key's set. if return value is null, unexpected situation is occurred
     */
    public static Set<SelectionKey> getValidKey() {
        try {
            while (selector.selectNow() == 0){
                return selector.selectedKeys();
            }
        } catch (IOException e) {
            // have to change with logger
            System.out.println("SocketStation getValidKey method : " + e);
        }
        // unexpected situation
        return null;
    }

    /**
     * @return SocketChannel List
     */
    public static List<SocketChannel> getSocketList() {
        return list;
    }

    /**
     *
     * @param socketChannel be removed from SocketStation Management
     */
    public static void delete(SocketChannel socketChannel) {
        synchronized (stationLock) {
            socketChannel.keyFor(selector).cancel();
            list.remove(socketChannel);
        }
    }
}
