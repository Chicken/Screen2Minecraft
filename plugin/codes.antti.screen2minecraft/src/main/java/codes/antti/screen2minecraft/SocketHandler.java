package codes.antti.screen2minecraft;
import codes.antti.screen2minecraft.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import java.io.*;
import java.net.*;

public class SocketHandler implements Runnable {
    private Socket client;
    private Material[][] oldState;
    private String password;
    private int screenHeight;
    private int screenWidth;
    private String worldName;
    private World world;
    private int xoffset;
    private int yoffset;
    private int zoffset;
    private int bufferSize;
    PrintWriter out;
    BufferedReader in;
    ColorUtils colorUtil;
    JavaPlugin plugin;
    BukkitScheduler scheduler;

    public SocketHandler(Socket socket, JavaPlugin screenshareplugin) {
        client = socket;
        plugin = screenshareplugin;
        colorUtil = new ColorUtils(plugin);
        scheduler = Bukkit.getServer().getScheduler();
    }

    public void run() {
        try {

            // preparing the in and out streams
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // reading configs from client
            password = in.readLine();
            screenHeight = Integer.parseInt(in.readLine());
            screenWidth = Integer.parseInt(in.readLine());
            worldName = in.readLine();
            xoffset = Integer.parseInt(in.readLine());
            yoffset = Integer.parseInt(in.readLine());
            zoffset = Integer.parseInt(in.readLine());
            bufferSize = screenHeight * screenWidth * 3;
            world = Bukkit.getWorld(worldName);

            // close socket if password is wrong
            if(!password.equals(plugin.getConfig().getString("password"))) {
                Bukkit.getLogger().info("Client tried to connect with wrong password!");
                client.close();
                return;
            }

            // saving the old blocks at coords
            Bukkit.getLogger().info("Saving old state of blocks.");

            oldState = new Material[screenWidth][screenHeight];

            for(int z = 0; z < screenHeight; z++) {
                for(int x = 0; x < screenWidth; x++) {
                    oldState[x][z] = new Location(world, x+xoffset,yoffset,z+zoffset).getBlock().getType();
                }
            }

            Bukkit.getLogger().info("Saved old state of blocks.");

            // setting up variables
            int index = 0;
            int[] data = new int[bufferSize];

            // reading data
            while(!client.isClosed()) {
                // read and check if we at the end of buffer
                int cur = in.read();
                if(index != bufferSize-1) {
                    // idk sometimes value is over 255 so if it is, we set it at 255
                    if(cur>255) {
                        cur = 255;
                    }
                    data[index] = cur;
                    index++;

                } else {
                    // loop locations and place blocks
                    for(int z = 0; z < screenHeight; z++) {
                        for(int x = 0; x < screenWidth; x++) {
                            Location loc = new Location(world, x+xoffset,yoffset,z+zoffset);
                            int dataIndex = x*3 + z*3*screenWidth;
                            Material block = Material.valueOf(colorUtil.findClosest(data[dataIndex+2], data[dataIndex+1], data[dataIndex]));
                            if(loc.getBlock().getType()!=block) {
                                scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        loc.getBlock().setType(block);
                                    }
                                });
                            }
                        }
                    }
                    index = 0;
                }
            }

        } catch (IOException e) {
            //catch error
            Bukkit.getLogger().info("Error! Probably client disconnection.");
            Bukkit.getLogger().info("Adding back old blocks.");
            //place back the old blocks
            for(int z = 0; z < screenHeight; z++) {
                for(int x = 0; x < screenWidth; x++) {
                    Location loc = new Location(world, x+xoffset,yoffset,z+zoffset);
                    Material block = oldState[x][z];
                    scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            loc.getBlock().setType(block);
                        }
                    });
                }
            }
        }
    }
}