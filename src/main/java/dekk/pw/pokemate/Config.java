package dekk.pw.pokemate;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static boolean autoEvolving;
    private static double range;
    private static int mapPoints;
    private static List<Integer> whiteListedPokemon;

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("config.properties"));
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            googleApiKey = properties.getProperty("api-key");
            speed = Double.parseDouble(properties.getProperty("speed", "1.0"));
            ivRatio = Integer.parseInt(properties.getProperty("iv-ratio", "85"));
            minCP = Integer.parseInt(properties.getProperty("min-cp", "1"));
            mapPoints = Integer.parseInt(properties.getProperty("map-points", "50"));
            showUI = Boolean.parseBoolean(properties.getProperty("show", "true"));
            dropItems = Boolean.parseBoolean(properties.getProperty("drop_items", "true"));
            autoEvolving = Boolean.parseBoolean(properties.getProperty("automatic-evolving", "true"));
            range = Double.parseDouble(properties.getProperty("range", ".04"));
            preferredBall = ItemIdOuterClass.ItemId.valueOf(properties.getProperty("preferred_ball", "ITEM_POKE_BALL")).getNumber();
            //whitelist
            String whiteList = properties.getProperty("whitelisted-pokemon", "");
            String[] strings = whiteList.split(",");
            if (strings != null) {
                whiteListedPokemon = new ArrayList<>();
                for (String string : strings) {
                    whiteListedPokemon.add(Integer.parseInt(string));
                }
            }
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

    public static boolean isAutoEvolving() {
        return autoEvolving;
    }

    public static double getRange() {
        return range;
    }

    public static int getMapPoints() {
        return mapPoints;
    }

    public static boolean isWhitelistEnabled() {
        List<Integer> poke = getWhitelistedPokemon();
        return poke != null && poke.size() > 0;
    }

    public static List<Integer> getWhitelistedPokemon() {
        return whiteListedPokemon;
    }

}
