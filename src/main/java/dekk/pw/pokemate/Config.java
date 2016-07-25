package dekk.pw.pokemate;

import POGOProtos.Inventory.Item.ItemIdOuterClass;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class Config {
    private static double speed;
    private static int preferredBall;
    private static String googleApiKey;
    private static String username;
    private static String password;
    private static int ivRatio;
    private static int minCP;
    private static boolean showUI;
    private static boolean dropItems;
    private static double range_x;
    private static double range_y;
    private static boolean evolving;

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("config.properties"));
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            googleApiKey = properties.getProperty("api-key");
            speed = Double.parseDouble(properties.getProperty("speed"));
            ivRatio = Integer.parseInt(properties.getProperty("iv-ratio"));
            minCP = Integer.parseInt(properties.getProperty("min-cp"));
            showUI = Boolean.parseBoolean(properties.getProperty("show"));
            dropItems = Boolean.parseBoolean(properties.getProperty("drop_items"));
            range_x = Double.parseDouble(properties.getProperty("range_x","0.04"));
            range_y = Double.parseDouble(properties.getProperty("range_y","0.04"));
            evolving = Boolean.parseBoolean(properties.getProperty("automatic-evolving"));
            if (properties.getProperty("preferred_ball").equals(""))
            	preferredBall=-1;
            else preferredBall = ItemIdOuterClass.ItemId.valueOf(properties.getProperty("preferred_ball")).getNumber();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public static double getSpeed() {
        return speed;
    }

    public static int getPreferredBall() {
        return preferredBall;
    }

    public static String getGoogleApiKey() {
        return googleApiKey;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static int getIvRatio() {
        return ivRatio;
    }

    public static int getMinCP() {
        return minCP;
    }

    public static boolean isShowUI() {
        return showUI;
    }

    public static boolean isDropItems() {
        return dropItems;
    }

    public static Properties getProperties() {
        return properties;
    }
    
    public static double getrange_x(){
    	return range_x;
    }
    public static double getrange_y(){
    	return range_y;
    }
    public static boolean isEvolvingActive() {
    	return evolving;
    }
}
