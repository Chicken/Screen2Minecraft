package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.*;
import java.net.*;

public class Main extends JavaPlugin {
    private Thread socketHandler;
    private ServerSocket serverSocket;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int bufferSize;
    public int screenWidth;
    public int screenHeight;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        socketHandler = new Thread() {
            public void run() {
                try {
                    screenWidth = config.getInt("screenWidth");
                    screenHeight = config.getInt("screenHeight");
                    bufferSize = screenWidth * screenHeight * config.getInt("bytesPerPixel");

                    serverSocket = new ServerSocket(config.getInt("port"));
	                clientSocket = serverSocket.accept();
	                out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    int index = 0;
                    int[] data = new int[bufferSize];

                    while (true) {
                        int cur = in.read();
                        if(index != bufferSize-1) {
                            data[index] = cur;
                            index++;
                        } else {
                            getLogger().info(Integer.toHexString(data[2]) + Integer.toHexString(data[1]) + Integer.toHexString(data[0]));
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
