package codes.antti.screen2minecraft;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.*;
import java.io.*;

public class Main extends JavaPlugin {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread thread;
        
    @Override
    public void onEnable() {
        thread = new Thread() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(1337);
                    getLogger().info("Screen to minecraft has started.");
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    int count = 0;
                    int[] data = new int[14745600];
                    //14745600
                    while (true) {
                        int input = in.read();
                        data[count] = input;
                        count++;
                        if(count==14745600) {
                            getLogger().info("Debug: " + Integer.toHexString(data[0]) + Integer.toHexString(data[1]) + Integer.toHexString(data[2]));
                            count = 0;
                        }
                    }
                } catch (IOException e) {
                    getLogger().info("err");
                }
            }
        };
        thread.start();

    }
    @Override
    public void onDisable() {
        try {
            in.close();
	        out.close();
            clientSocket.close();
            serverSocket.close();
        } catch(IOException e) {
            getLogger().info("Something went wrong with the sockets.");
        }
        getLogger().info("Screen to minecraft has been disabled.");
    }
}
