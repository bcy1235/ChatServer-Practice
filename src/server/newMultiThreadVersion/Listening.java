package server.newMultiThreadVersion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * main thread Waiting for new connection
 */
public class Listening {
    private static int SERVER_PORT = 40000;
    private static int BUF_SIZE = 3000;

    public static void main(String[] args) {
        try {
            MessageStation.start();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT), 1000);
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // actual listening part
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    SocketChannel channel = serverSocketChannel.accept();
                    channel.configureBlocking(false);

                    SocketBuffer.insert(channel, BUF_SIZE);
                    ReadingBox.register(channel);
                    WritingBox.register(channel);
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Listening main method : " + e);
        }
    }

}
