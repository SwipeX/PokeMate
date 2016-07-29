package dekk.pw.pokemate;

import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.util.SystemTimeImpl;
import okhttp3.OkHttpClient;

import java.security.MessageDigest;
import java.util.Scanner;
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
    private AtomicBoolean walking = new AtomicBoolean(false);
    private CredentialProvider credentialProvider;

    public Context(PokemonGo go, PlayerProfile profile, boolean walking, CredentialProvider credentialProvider, OkHttpClient http) {
        this.api = go;
        this.profile = profile;
        this.walking.set(walking);
        this.credentialProvider = credentialProvider;
        this.http = http;
    }

    public static CredentialProvider Login(OkHttpClient httpClient) {
        return Login(null, httpClient);
    }

    public static String getUsernameHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Config.getUsername().getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Config.getUsername();
    }

    public static CredentialProvider Login(Context context, OkHttpClient httpClient) {
        String token = null;
        try {
            if (Config.getUsername().contains("@")) {
                GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient);
                System.out.println("-----------------------------------------");
                System.out.println("  Please go to the following URL");
                System.out.println(GoogleUserCredentialProvider.LOGIN_URL);
                System.out.println("Enter authorisation code:");
                Scanner sc = new Scanner(System.in);
                String access = sc.nextLine();
                provider.login(access);
                return provider;
            } else {
                return new PtcCredentialProvider(httpClient, Config.getUsername(), Config.getPassword(), new SystemTimeImpl());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String millisToTimeString(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public AtomicDouble getLat() {
        return lat;
    }

    public void setLat(AtomicDouble lat) {
        this.lat = lat;
    }

    public AtomicDouble getLng() {
        return lng;
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

    public PokemonGo getApi() {
        return api;
    }

    public void setApi(PokemonGo api) {
        this.api = api;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }

    public boolean isWalking() {
        return walking.get();
    }

    public AtomicBoolean getWalking() {
        return walking;
    }

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
}
