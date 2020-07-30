package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
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
    public int downScale;
    public int bytesPerPixel;
    @Override
    public void onEnable() {
        JavaPlugin plugin = this;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        socketHandler = new Thread() {
            public void run() {
                try {
                    screenWidth = config.getInt("screenWidth");
                    screenHeight = config.getInt("screenHeight");
                    downScale = config.getInt("downScale");
                    bytesPerPixel = config.getInt("bytesPerPixel");
                    bufferSize = screenWidth * screenHeight * bytesPerPixel;

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
                            for(int y = 0; y < screenHeight/downScale; y++) {
                                for(int x = 0; x < screenWidth/downScale; x++) {
                                    Location loc = new Location(Bukkit.getServer().getWorld("world"), x,3,y);
                                    //need a data array of all blocks and their rgb 
                                    //then find closest one to the color at current x,y and get their material
                                    //Material.valueOf("blockname")
                                    //data[x*downScale*bytesPerPixel + y*screenWidth*downScale*bytesPerPixel];
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        public void run() {
                                            loc.getBlock().setType(Material.BEDROCK);
                                        }
                                    });
                                }
                            }
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
            if(clientSocket != null) {
                out.close();
                in.close();
                clientSocket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            getLogger().info("error");
        }
        getLogger().info("Screen to minecraft has been disabled.");
    }
}
