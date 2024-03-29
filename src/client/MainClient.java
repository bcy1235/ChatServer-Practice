package client;

import java.io.File;
import java.io.IOException;

public class MainClient {
    private static int PROCESS_NUM = 1;
    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < PROCESS_NUM; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.command("java", "client.DummyProcess", String.valueOf(i));
            processBuilder.directory(new File("C:\\project\\ChatServer-Practice\\out\\production\\ChatServer-Practice"));
            Process process = processBuilder.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    process.destroy();
                }
            });
        }

        System.out.println("Enter any key if you want exit");
        System.in.read();
    }
}
