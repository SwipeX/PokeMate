package dekk.pw.pokemate;

import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.SystemTimeImpl;
import dekk.pw.pokemate.tasks.Task;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TimD on 7/21/2016.
 */
public class Context {
    private OkHttpClient http;
    private PokemonGo api;
    private AtomicDouble lat = new AtomicDouble();
    private AtomicDouble lng = new AtomicDouble();
    private PlayerProfile profile;
    private final AtomicBoolean walking = new AtomicBoolean(false);
    private CredentialProvider credentialProvider;
    private static final SystemTimeImpl time = new SystemTimeImpl();
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private int routesIndex;
    private final LinkedHashMap<String,String> consoleStrings = new LinkedHashMap<>();
    private Map pokeMap;
    private Inventories pokeInventories;


    public Context(PokemonGo go, PlayerProfile profile, boolean walking, CredentialProvider credentialProvider, OkHttpClient http) {

        this.api = go;
        this.profile = profile;
        this.walking.set(walking);
        this.credentialProvider = credentialProvider;
        this.http = http;
        this.routesIndex = 0;

        //This just sets up a standardized order of outputs for the GUI HashMap
        this.consoleStrings.put("Bot Actions", "");
        this.consoleStrings.put("Update", "0XP/H");
        this.consoleStrings.put("TagPokestop", "No PokeStops Tagged");
        this.consoleStrings.put("CatchPokemon", "No Pokemon Caught");
        this.consoleStrings.put("Navigate", "");
        this.consoleStrings.put("Pokemon Management", "");
        this.consoleStrings.put("EvolvePokemon", "No Pokemon Evolved");
        this.consoleStrings.put("ReleasePokemon", "No Pokemon Released");
        this.consoleStrings.put("Egg Management", "");
        this.consoleStrings.put("HatchEgg", "No Eggs Hatched");
        this.consoleStrings.put("IncubateEgg", "No Eggs Incubated");
        this.consoleStrings.put("Item Management", "");
        this.consoleStrings.put("DropItems", "No Items Dropped");
    }

    public static CredentialProvider Login(OkHttpClient httpClient) {
        return Login(null, httpClient);
    }

    private static String getUsernameHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Config.getUsername().getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Config.getUsername();
    }

    private static CredentialProvider Login(Context context, OkHttpClient httpClient) {
        String token;
        try {
            new File("tokens/").mkdir();
            if (Config.getUsername().contains("@")) {
                File tokenFile = new File("tokens/" + Context.getUsernameHash() + ".txt");
                if (tokenFile.exists()) {
                    Scanner scanner = new Scanner(tokenFile);
                    token = scanner.nextLine();
                    scanner.close();
                    if (token != null) {
                        return new GoogleUserCredentialProvider(httpClient, token, time);
                    }
                } else {
                    String access;
                    GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient, time);
                    System.out.println("-----------------------------------------");
                    System.out.println("  Please go to the following URL");
                    System.out.println(GoogleUserCredentialProvider.LOGIN_URL);
                    if (Config.isShowUI()) {
                        Desktop.getDesktop().browse(URI.create(GoogleUserCredentialProvider.LOGIN_URL));
                         access = JOptionPane.showInputDialog("Enter authorization code: ");
                    }
                    else {
                        System.out.println("Enter authorization code: ");
                        Scanner sc = new Scanner(System.in);
                        access = sc.nextLine();
                    }
                    provider.login(access);
                    try (PrintWriter p = new PrintWriter("tokens/" + Context.getUsernameHash() + ".txt")) {
                        p.println(provider.getRefreshToken());
                    }
                    return provider;

                }
            } else {
                return new PtcCredentialProvider(httpClient, Config.getUsername(), Config.getPassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AtomicDouble getLat() {
        return lat;
    }

    public AtomicDouble getLng() {
        return lng;
    }

    public void setLat(AtomicDouble lat) {
        this.lat = lat;
    }

    public void setLng(AtomicDouble lng) {
        this.lng = lng;
    }

    public OkHttpClient getHttp() {
        return http;
    }

    public void setHttp(OkHttpClient http) {
        this.http = http;
    }

    public PokemonGo getApi() { return api; }

    public void setApi(PokemonGo api) {
        this.api = api;
    }

    public PlayerProfile getProfile() {return profile; }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }

    public boolean isWalking() {
        return walking.get();
    }

    public AtomicBoolean getWalking() {
        return walking;
    }

    public int getRoutesIndex() { return routesIndex; }

    public Map getMap() {return this.pokeMap; }

    public void refreshMap() { this.pokeMap = this.api.getMap(); }

    public Inventories getInventories() {return this.pokeInventories; }

    public void refreshInventories() throws LoginFailedException, RemoteServerException {
        this.api.getInventories().updateInventories(true);
        this.pokeInventories = this.api.getInventories();
    }

    public void increaseRoutesIndex() { this.routesIndex++; }

    public void resetRoutesIndex() { this.routesIndex = 0; }

    public LinkedHashMap<String, String> getConsoleStrings() { return consoleStrings; }

    public void addTask(Task task) { executor.submit(task); }

    public void setConsoleString(String key, String text) { this.consoleStrings.put(key, "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + text); }

    public CredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    /**
     * @param pokemon the pokemon for which an IV ratio is desired.
     * @return an integer 0-100 on the individual value of the pokemon.
     */
    public int getIvRatio(Pokemon pokemon) {
        return (pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina()) * 100 / 45;
    }

    public static String millisToTimeString(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
