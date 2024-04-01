package server.singleThreadVersion;

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
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
            new Thread(new Sending()).start();
            // actual listening part

            while (true) {
                SocketChannel connectedChannel = serverSocketChannel.accept();
                connectedChannel.configureBlocking(false);

                SocketBuffer.insert(connectedChannel, BUF_SIZE);
                SocketStation.register(connectedChannel);
            }
        } catch (IOException e) {
            System.out.println("Listening main method : " + e);
        }
    }
}
