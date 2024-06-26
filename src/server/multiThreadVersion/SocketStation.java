package server.multiThreadVersion;


import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * class which is used for managing SocketChannel
 */
public class SocketStation {
    private static Selector selector;
    private static List<SocketChannel> list;

    static {
        try {
            selector = Selector.open();
            list = new LinkedList<>();
        } catch (IOException e) {
            System.out.println("SocketStation static block : " + e);
        }
    }

    /**
     * method which is used for register socketChannel to the Selector
     *
     * @param socketChannel which is registered to Selector
     */
    public static void registerList(SocketChannel socketChannel) {
        synchronized (list) {
            list.add(socketChannel);
        }
    }

    public static void registerSelector(SocketChannel socketChannel) {
        synchronized (selector) {
            try {
                socketChannel.register(selector, SelectionKey.OP_READ);
            } catch (ClosedChannelException e) {
                System.out.println("SocketStation register method : " + e);
            }
        }
    }

    /**
     * you can get set of keys which contain valid SocketChannel's.
     * <br> valid mean interest set defined when register SocketChannel to Selector is detected
     *
     * @return valid Selection Key's set. if return value is null, unexpected situation is occurred
     */
    public static Set<SelectionKey> getValidKey() {
        try {
            while (selector.selectNow() != 0) {
                return selector.selectedKeys();
            }
        } catch (IOException e) {
            System.out.println("SocketStation getValidKey method : " + e);
        }
        return null;
    }

    /**
     * @return SocketChannel List
     */
    public static List<SocketChannel> getSocketList() {
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    /**
     * @param socketChannel be removed from SocketStation Management
     */
    public static void deleteFromSelector(SocketChannel socketChannel) {
        synchronized (selector) {
            socketChannel.keyFor(selector).cancel();
        }
    }

    public static void deleteFromList(SocketChannel socketChannel) {
        synchronized (list) {
            list.remove(socketChannel);
        }
    }
}
