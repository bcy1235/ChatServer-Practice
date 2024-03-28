package client;

import java.io.File;
import java.io.IOException;

public class MainClient {
    public static void main(String[] args) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("java", "client.DummyProcess");
        processBuilder.directory(new File("C:\\project\\ChatServer-Practice\\out\\production\\ChatServer-Practice"));
        Process process = processBuilder.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                process.destroy();
            }
        });

        process.waitFor();
    }
}
