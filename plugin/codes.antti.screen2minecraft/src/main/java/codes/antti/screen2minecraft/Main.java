package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.*;

public class Main extends JavaPlugin {
    private Thread socketHandler;
    private ServerSocket serverSocket;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    @Override
    public void onEnable() {
        socketHandler = new Thread() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(1337);
	                clientSocket = serverSocket.accept();
	                out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    int screenWidth = 1366;
                    int screenHeight = 768;
                    int bufferSize = screenWidth * screenHeight * 4;
                    int index = 0;
                    int[] data = new int[bufferSize];

                    while (true) {
                        int cur = in.read();
                        if(index != bufferSize-1) {
                            data[index] = cur;
                            index++;
                        } else {
                            getLogger().info(Integer.toHexString(data[0]));
                            data = new int[bufferSize];
                            index = 0;
                        }
                    }

                } catch (IOException e) {
                    getLogger().info("error");
                }
            }
        };
        socketHandler.start();
        getLogger().info("Screen to minecraft has started.");
    }
    @Override
    public void onDisable() {
        try {
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            getLogger().info("error");
        }
        getLogger().info("Screen to minecraft has been disabled.");
    }
}
