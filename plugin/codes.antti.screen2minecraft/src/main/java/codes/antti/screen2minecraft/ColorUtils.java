package codes.antti.screen2minecraft;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorUtils {
    HashMap<Material, Color> blocks;
    HashMap<Color, Material> cache;

    public ColorUtils(JavaPlugin plugin) {
        // loading colors
        blocks = new HashMap();
        cache = new HashMap();

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
                blocks.put(Material.valueOf(blockData.get("game_id_13").getAsString().split(":")[1].toUpperCase()), new Color(blockData.get("red").getAsInt(), blockData.get("green").getAsInt(), blockData.get("blue").getAsInt()));
            }

            plugin.getLogger().info("Loaded " + String.valueOf(blocks.size()) + " blocks / colors.");
        } catch (IOException e) {
            plugin.getLogger().severe("Error while trying to load colors.");
        }
    }

    public Material findClosest(Color color) {
        double closestDist = Double.MAX_VALUE;
        Material closestBlock = Material.RED_CONCRETE;
        if(cache.containsKey(color)) {
            return cache.get(color);
        }
        for(Map.Entry<Material, Color> entry : blocks.entrySet()) {
            Material name = entry.getKey();
            Color value = entry.getValue();
            double distance = dist(color, value);
            if(distance<closestDist) {
                closestDist = distance;
                closestBlock = name; 
            }    
        }
        cache.put(color, closestBlock);
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