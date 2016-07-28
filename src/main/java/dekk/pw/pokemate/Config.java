package dekk.pw.pokemate;

import POGOProtos.Inventory.Item.ItemIdOuterClass;

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
    public final static String POKE = "Pok\u00E9";
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
    private static List<Integer> neverTransferPokemon;
	private static List<String> droppedItems;
    private static boolean consoleNotification;
    private static boolean userInterfaceNotification;
    private static boolean uiSystemNotification;
    private static boolean useCustomNamedLocation;
    private static String customNamedLocation;
    private static boolean eggsIncubating;
    private static boolean eggsHatching;
    private static boolean transferPrefersIV;
	private static int cpMinimumForMessage;
    private static Properties properties = new Properties();

    public static void load(String configPath) {
        try {
            properties.load(new FileInputStream(configPath));
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            googleApiKey = properties.getProperty("api_key");
            speed = Double.parseDouble(properties.getProperty("speed", "1.0"));
            ivRatio = Integer.parseInt(properties.getProperty("iv_ratio", "85"));
            minCP = Integer.parseInt(properties.getProperty("min_cp", "1"));
            mapPoints = Integer.parseInt(properties.getProperty("map_points", "50"));
            showUI = Boolean.parseBoolean(properties.getProperty("show", "true"));
            autoEvolving = Boolean.parseBoolean(properties.getProperty("automatic_evolving", "true"));
            range = Double.parseDouble(properties.getProperty("range", ".04"));
            preferredBall = ItemIdOuterClass.ItemId.valueOf(properties.getProperty("preferred_ball", "ITEM_POKE_BALL")).getNumber();
            eggsIncubating = Boolean.parseBoolean(properties.getProperty("eggs_incubating", "true"));
            eggsHatching = Boolean.parseBoolean(properties.getProperty("eggs_hatching", "true"));
            transferPrefersIV = Boolean.parseBoolean(properties.getProperty("transfer-prefers-iv", "false"));
            //whitelist
            String whiteList = properties.getProperty("whitelisted_pokemon", null);
            whiteListedPokemon = new ArrayList<>();
            fillList(whiteList, whiteListedPokemon);
            String neverTransferPokemonNames = properties.getProperty("never_transfer", null);
            neverTransferPokemon = new ArrayList<>();
            fillList(neverTransferPokemonNames, neverTransferPokemon);
            // named location
            useCustomNamedLocation = Boolean.parseBoolean(properties.getProperty("use_location_name", "false"));
            customNamedLocation = properties.getProperty("location_by_name");
            // notification
            consoleNotification = Boolean.parseBoolean(properties.getProperty("console_notification", "true"));
            userInterfaceNotification = Boolean.parseBoolean(properties.getProperty("ui_notification", "true"));
            uiSystemNotification = Boolean.parseBoolean(properties.getProperty("sys_notification", "false"));
			// dropped items
			dropItems = Boolean.parseBoolean(properties.getProperty("drop_items", "true"));
			String droppedItemNames = properties.getProperty("drop_item_list", "ITEM_POTION,ITEM_SUPER_POTION,ITEM_MAX_POTION,ITEM_HYPER_POTION,ITEM_RAZZ_BERRY,ITEM_REVIVE,ITEM_MAX_REVIVE");
			droppedItems = new ArrayList<>();
			fillListString(droppedItemNames, droppedItems);
			// minimum cp for message
			cpMinimumForMessage = Integer.parseInt(properties.getProperty("minimum_cp_for_ui_message", "0"));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
	
	private static void fillListString(String propertiesString, List<String> target) {
        if (propertiesString != null) {
            String[] strings = propertiesString.split(",");
            if (strings != null) {
                for (String string : strings) {
                    if (string.length() > 0)
                        target.add(string);
                }
            }
        }
    }

    private static void fillList(String propertiesString, List<Integer> target) {
        if (propertiesString != null) {
            String[] strings = propertiesString.split(",");
            if (strings != null) {
                for (String string : strings) {
                    if (string.length() > 0)
                        target.add(Integer.parseInt(string));
                }
            }
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

    public static List<Integer> getNeverTransferPokemon() {
        return neverTransferPokemon;
    }

    public static boolean isUseCustomNamedLocation() {
        return useCustomNamedLocation;
    }

    public static String getCustomNamedLocation() {
        return customNamedLocation;
    }

    public static boolean isConsoleNotification() {
        return consoleNotification;
    }

    public static boolean isUserInterfaceNotification() {
        return userInterfaceNotification;
    }

    public static boolean isUiSystemNotification(){
        return uiSystemNotification;
    }

    public static boolean isEggsIncubating() {
        return eggsIncubating;
    }

    public static boolean isEggsHatching() {
        return eggsHatching;
    }
	
	public static List<String> getDroppedItems() {
		return droppedItems;
	}
	
	public static int getMinimumCPForMessage() {
		return cpMinimumForMessage;
	}

    public static boolean isTransferPrefersIV() {
        return transferPrefersIV;
    }

    public static void setTransferPrefersIV(boolean transferPrefersIV) {
        Config.transferPrefersIV = transferPrefersIV;
    }
}
