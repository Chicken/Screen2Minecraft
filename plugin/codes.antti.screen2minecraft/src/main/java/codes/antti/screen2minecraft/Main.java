package codes.antti.screen2minecraft;
import codes.antti.screen2minecraft.SocketHandler;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.*;

public class Main extends JavaPlugin {
    private ServerSocket serverSocket;
    JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        // config and resources
        saveDefaultConfig();
        saveResource("baked_blocks.json", false);

        // starting server
        try {
            serverSocket = new ServerSocket(plugin.getConfig().getInt("port"));

             // thread to handle socket connections
            new Thread() {
                public void run() {
                    while(!serverSocket.isClosed()) {
                        try {
                            Socket client = serverSocket.accept();
                            Runnable socketHandler = new SocketHandler(client, plugin);
                            new Thread(socketHandler).start();
                            getLogger().info("Client connected.");
                        } catch(IOException e) {
                            getLogger().severe("Error trying to connect client.");
                        }
                    }
                }
            }.start();

        } catch(IOException e) {
            getLogger().severe("Error trying to start server on port " + String.valueOf(plugin.getConfig().getInt("port")));
        }
        getLogger().info("Screen to minecraft has started.");
    }

    @Override
    public void onDisable() {
        try {
            serverSocket.close();
        } catch(IOException e) {
            getLogger().severe("Error trying to close server.");
        }
        getLogger().info("Screen to minecraft has been disabled.");
    }
}

