package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Screen to minecraft has started.");
    }
    @Override
    public void onDisable() {
        getLogger().info("Screen to minecraft has been disabled.");
    }
}
