package codes.antti.screen2minecraft;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorUtils {
    HashMap<String, Color> blocks;

    public ColorUtils(JavaPlugin plugin) {
        // loading colors
        blocks = new HashMap<String, Color>();

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

            plugin.getLogger().info("Loaded " + String.valueOf(blocks.size()) + " blocks / colors.");
        } catch (IOException e) {
            plugin.getLogger().severe("Error while trying to load colors.");
        }
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