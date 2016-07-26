package dekk.pw.pokemate;

import POGOProtos.Enums.PokemonIdOuterClass;
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
    private static List<Integer> neverTransferPokemons;
    private static List<Integer> alwaysTransferPokemons;

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
            String whiteList = properties.getProperty("whitelisted-pokemon", null);
            if (whiteList != null) {
                String[] strings = whiteList.split(",");
                if (strings != null) {
                    whiteListedPokemon = new ArrayList<>();
                    for (String string : strings) {
                        whiteListedPokemon.add(Integer.parseInt(string));
                    }
                }
            }

            String neverTransferPokemonNames = properties.getProperty("never-transfer", "");
            neverTransferPokemons = new ArrayList<>();
            fillTransferList(neverTransferPokemonNames, neverTransferPokemons);

            String alwaysTransferPokemonNames = properties.getProperty("always-transfer", "");
            alwaysTransferPokemons = new ArrayList<>();
            fillTransferList(alwaysTransferPokemonNames, alwaysTransferPokemons);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private static void fillTransferList(String pokemonNames, List<Integer> target) {
        if (pokemonNames.length() > 0) { // To make sure that the config option is not empty.
            for (String pokemonName : pokemonNames.split(",")) {
                try {
                    target.add(PokemonIdOuterClass.PokemonId.valueOf(pokemonName).getNumber());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace(); // User attempted to ignore a non-existent pokemon.
                    JOptionPane.showMessageDialog(null, "Invalid pokemon in config:\n" + e.getMessage());
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

    public static List<Integer> getNeverTransferPokemons() {
        return neverTransferPokemons;
    }

    public static List<Integer> getAlwaysTransferPokemons() {
        return alwaysTransferPokemons;
    }

}
