package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    public HashMap<String, Color> blocks = new HashMap<String, Color>();
    public int screenWidth;
    public int screenHeight;
    public int downScale;
    public int bytesPerPixel;
    
    @Override
    public void onEnable() {
        JavaPlugin plugin = this;
        plugin.saveDefaultConfig();
        plugin.saveResource("baked_blocks.json", false);
        FileConfiguration config = plugin.getConfig();
        socketHandler = new Thread() {
            public void run() {
                try {
                    InputStream blockInputStream = plugin.getResource("baked_blocks.json");
                    InputStreamReader blockIsReader = new InputStreamReader(blockInputStream);
                    BufferedReader blockReader = new BufferedReader(blockIsReader);
                    StringBuffer blockStringBuilder = new StringBuffer();
                    String curStr;

                    while((curStr = blockReader.readLine())!= null){
                       blockStringBuilder.append(curStr);
                    }

                    String bakedBlocks = blockStringBuilder.toString();
                    JsonArray blockDatas = (JsonArray)(new Gson()).fromJson(bakedBlocks, JsonArray.class);
                    for(int k = 0; k < blockDatas.size(); k++) {
                        JsonObject blockData = blockDatas.get(k).getAsJsonObject();
                        blocks.put(blockData.get("game_id_13").getAsString().split(":")[1].toUpperCase(), new Color(blockData.get("red").getAsInt(), blockData.get("green").getAsInt(), blockData.get("blue").getAsInt()));
                    }

                    getLogger().info("Loaded " + String.valueOf(blocks.size()) + " blocks.");

					scheduler = Bukkit.getServer().getScheduler();
					world = Bukkit.getServer().getWorlds().get(0);
                    screenWidth = config.getInt("screenWidth");
                    screenHeight = config.getInt("screenHeight");
                    downScale = config.getInt("downScale");
                    bytesPerPixel = config.getInt("bytesPerPixel");
                    bufferSize = screenWidth * screenHeight * bytesPerPixel;

                    serverSocket = new ServerSocket(config.getInt("port"));
                    getLogger().info("Waiting for connection.");
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
                    getLogger().info("Error! Probably client disconnection, reload the plugin.");
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
        for(Map.Entry<String, Color> entry : blocks.entrySet()) {
            String name = entry.getKey();
            Color value = entry.getValue();
            double distance = dist(new Color(r, g, b), value);
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
        int g2 = b.getGreen();
        int b2 = b.getBlue();
        double drp2 = Math.pow(r1-r2,2);
        double dgp2 = Math.pow(g1-g2,2);
        double dbp2 = Math.pow(b1-b2,2);
        int t = (r1+r2)/2;
        return Math.sqrt( 2 * drp2 + 4 * dgp2 + 3 * dbp2 + t * ( drp2 - dbp2 ) / 256);
    }
}

