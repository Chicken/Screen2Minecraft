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
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

public class Main extends JavaPlugin {
    private Thread socketHandler;
    private ServerSocket serverSocket;
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int bufferSize;
    private BukkitScheduler scheduler;
    private World world;
    public HashMap<String, String> blocks = new HashMap<String, String>();
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
                    blocks.put("WHITE_CONCRETE", "CFD5D6");
                    blocks.put("ORANGE_CONCRETE","E06101");
                    blocks.put("MAGENTA_CONCRETE","A9309F");
                    blocks.put("LIGHT_BLUE_CONCRETE","2489C7");
                    blocks.put("YELLOW_CONCRETE","F1AF15");
                    blocks.put("LIME_CONCRETE","5EA919");
                    blocks.put("PINK_CONCRETE","D6658F");
                    blocks.put("GRAY_CONCRETE","373A3E");
                    blocks.put("LIGHT_GRAY_CONCRETE","7D7D73");
                    blocks.put("CYAN_CONCRETE","157788");
                    blocks.put("PURPLE_CONCRETE","64209C");
                    blocks.put("BLUE_CONCRETE","2D2F8F");
                    blocks.put("BROWN_CONCRETE","603C20");
                    blocks.put("GREEN_CONCRETE","495B24");
                    blocks.put("RED_CONCRETE","8E2121");
                    blocks.put("BLACK_CONCRETE","080A0F");
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
                            if(cur>255) {
                                cur = 255;
                            }
                            data[index] = cur;
                            index++;
                        } else {
                            for(int y = 0; y < screenHeight/downScale; y++) {
                                for(int x = 0; x < screenWidth/downScale; x++) {
                                    Location loc = new Location(world, x,3,y);
                                    int dataIndex = x*bytesPerPixel*downScale + y*screenWidth*bytesPerPixel*downScale;
                                    Material block = Material.valueOf(findClosest(data[dataIndex+2], data[dataIndex+1], data[dataIndex]));
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

    public String findClosest(int r, int g, int b) {
        double closestDist = Double.MAX_VALUE;
        String closestBlock = "";
        for(Map.Entry<String, String> entry : blocks.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            double distance = dist(new Color(r, g, b), Color.decode("#"+value));
            if(distance<closestDist) {
                closestDist = distance;
                closestBlock = name; 
            }    
        }
        return closestBlock;
    }

    public double dist(Color a, Color b) {
        int r1 = a.getRed();
        int g1 = a.getGreen();
        int b1 = a.getBlue();
        int r2 = b.getRed();
        int g2 = a.getGreen();
        int b2 = a.getBlue();
        double drp2 = Math.pow(r1-r2,2);
        double dgp2 = Math.pow(g1-g2,2);
        double dbp2 = Math.pow(b1-b2,2);
        int t = (r1+r2)/2;
        return Math.sqrt( 2 * drp2 + 4 * dgp2 + 3 * dbp2 + t * ( drp2 - dbp2 ) / 256);
    }
}
