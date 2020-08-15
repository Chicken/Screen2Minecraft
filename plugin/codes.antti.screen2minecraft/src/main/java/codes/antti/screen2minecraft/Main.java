package codes.antti.screen2minecraft;
import codes.antti.screen2minecraft.ColorUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import java.awt.Color;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Main extends JavaPlugin {
    private Material[][] oldState;
    private int screenWidth;
    private int screenHeight;
    private int xoffset;
    private int yoffset;
    private int zoffset;
    private int downscale;
    private String worldName;
    World world;
    @Override
    public void onEnable() {
        
        // config
        saveDefaultConfig();
        saveResource("baked_blocks.json", false);

        downscale = getConfig().getInt("downscale");
        worldName = getConfig().getString("world");
        screenWidth = getConfig().getInt("screenWidth");
        screenHeight = getConfig().getInt("screenHeight");
        xoffset = getConfig().getInt("xoffset");
        yoffset = getConfig().getInt("yoffset");
        zoffset = getConfig().getInt("zoffset");
        world = Bukkit.getWorld(worldName);

        // saving old blocks
        getLogger().info("Saving old state of blocks.");

        oldState = new Material[screenWidth][screenHeight];

        for(int z = 0; z < screenHeight/downscale; z++) {
            for(int x = 0; x < screenWidth/downscale; x++) {
                oldState[x][z] = new Location(world, x+xoffset,yoffset,z+zoffset).getBlock().getType();
            }
        }

        getLogger().info("Saved old state of blocks.");

        // starting up the robot and color utils
        getLogger().info("Starting up the stream.");

        ColorUtils colorUtil = new ColorUtils(this);
        Rectangle screenRect = new Rectangle(screenWidth, screenHeight);

        try {
            Robot robot = new Robot();

            // scheduling the screensharing
            new BukkitRunnable(){
                public void run() {
                    BufferedImage screen = robot.createScreenCapture(screenRect);
                    for(int z = 0; z < screenHeight/downscale; z++) {
                        for(int x = 0; x < screenWidth/downscale; x++) {
                            Location loc = new Location(world, x+xoffset,yoffset,z+zoffset);
                            Material block = colorUtil.findClosest(new Color(screen.getRGB(x*downscale, z*downscale)));
                            if(loc.getBlock().getType()!=block) {
                                loc.getBlock().setType(block);
                            }
                        }
                    }
                }
            }.runTaskTimer(this, 0L, 1L);
        } catch (AWTException e) {
            getLogger().severe("Error whilst capturing screen!");
        }

        getLogger().info("Screen to minecraft has started.");
    }

    @Override
    public void onDisable() {
        // setting back old blocks
        for(int z = 0; z < screenHeight/downscale; z++) {
            for(int x = 0; x < screenWidth/downscale; x++) {
                Location loc = new Location(world, x+xoffset,yoffset,z+zoffset);
                Material block = oldState[x][z];
                loc.getBlock().setType(block);
            }
        }
        getLogger().info("Screen to minecraft has been disabled.");
    }
}

