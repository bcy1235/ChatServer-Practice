package server.newMultiThreadVersion;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketRoom {
    private CopyOnWriteArrayList<SocketChannel> socketList;
    private Selector selector;
    private int maxSize;
    private int size;

    public SocketRoom(int maxSize) {
        this.maxSize = maxSize;
        size = 0;
        try {
            selector = Selector.open();
            socketList = new CopyOnWriteArrayList<>();
        } catch (IOException e) {
            System.out.println("Fail to open Selector in SocketRoom Constructor : " + e);
        }
    }

    public void register(SocketChannel socketChannel) {
        try {
            socketChannel.register(selector, SelectionKey.OP_READ);
            synchronized (this) {
                socketList.add(socketChannel);
            }
            size++;
        } catch (ClosedChannelException e) {
            System.out.println("SocketRoom fail to register socketChannel to Selector : " + e);
        }
    }

    public void delete(SocketChannel socketChannel) {
        socketList.remove(socketChannel);
        socketChannel.keyFor(selector).cancel();
        size--;
    }

    public Set<SelectionKey> getValidKey() {
        try {
            if (selector.selectNow() == 0)
                return null;
        } catch (IOException e) {
            System.out.println("SocketRoom's selector fails to selectNow : " + e);
        }
        return selector.selectedKeys();
    }

    public List<SocketChannel> getSocketList() {
        return socketList;
    }
    public boolean isFull() {
        return size == maxSize;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
