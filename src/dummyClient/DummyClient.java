package dummyClient;

import java.io.File;
import java.io.IOException;

public class DummyClient {
    private static int PROCESS_NUM = 5;
    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < PROCESS_NUM; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder();

            processBuilder.command("java", "dummyClient.DummyProcess", String.valueOf(i));
            processBuilder.directory(new File("C:\\project\\ChatServer-Practice\\out\\production\\ChatServer-Practice"));
            Process process = processBuilder.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    process.destroy();
                }
            });
        }

        System.out.println("Enter Any Key");
        System.in.read();
    }
}
