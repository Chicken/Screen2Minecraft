package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import java.io.*;
import java.net.*;

public class Main extends JavaPlugin {
    private Thread socketHandler;
    private ServerSocket serverSocket;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int bufferSize;
    private BukkitScheduler scheduler;
    private World world;
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
                    scheduler = Bukkit.getServer().getScheduler();
                    world = Bukkit.getServer().getWorld("world");
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
                            for(int y = 0; y < screenHeight/downScale; y++) {
                                for(int x = 0; x < screenWidth/downScale; x++) {
                                    Location loc = new Location(world, x,3,y);
                                    int dataIndex = x*bytesPerPixel*downScale + y*screenWidth*bytesPerPixel*downScale;
                                    Material block = Material.valueOf(findClosest(toHex(data[dataIndex], data[dataIndex+1], data[dataIndex+2])));
                                    scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                                        public void run() {
                                            loc.getBlock().setType(block);
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

    public String findClosest(String hex) {
        int num = Integer.parseInt(hex,16);
        if(num==16711680) {
            return "RED_CONCRETE";
        } 
        if(num<8421504) {
            return "BLACK_CONCRETE";
        } else {
            return "WHITE_CONCRETE";
        }
    }

    public String toHex(int r ,int g ,int b) {
        String hex = "";
        hex += Integer.toHexString(r);
        hex += Integer.toHexString(g);
        hex += Integer.toHexString(b);
        try {
            Integer.parseInt(hex,16);
        } catch (NumberFormatException e) {
            getLogger().info(String.valueOf(r) + " " + String.valueOf(g) + " " + String.valueOf(b));
            return "ff0000";
        }
        return hex;
    }
    public String right(String value, int length) {
        return value.substring(value.length() - length);
    }
}
