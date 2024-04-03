package server.multiThreadVersion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * main thread Waiting for new connection
 */
public class Listening {
    private static int SERVER_PORT = 40000;
    private static int BUF_SIZE = 3000;

    public static void main(String[] args) {
        try {
            ProcessThreadPool.initialize();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
            new Thread(new Reading()).start();

            // actual listening part
            while (true) {
                SocketChannel connectedChannel = serverSocketChannel.accept();
                if (connectedChannel == null)
                    continue;

                connectedChannel.configureBlocking(false);
                SocketBuffer.insert(connectedChannel, BUF_SIZE);
                SocketStation.registerSelector(connectedChannel);
                SocketStation.registerList(connectedChannel);
            }
        } catch (IOException e) {
            System.out.println("Listening main method : " + e);
        }
    }
}
