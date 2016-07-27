package dekk.pw.pokemate;

import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.*;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

    private static CredentialProvider Login(Context context, OkHttpClient httpClient) {
        String token;
        try {
            if (Config.getUsername().contains("@")) {
                File tokenFile = new File("token.txt");
                if (tokenFile.exists()) {
                    Scanner scanner = new Scanner(tokenFile);
                    token = scanner.nextLine();
                    if (token != null) {
                        return new GoogleCredentialProvider(httpClient, token);
                    }
                } else {
                    return new GoogleCredentialProvider(httpClient, new GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener() {
                        @Override
                        public void onInitialOAuthComplete(GoogleAuthJson googleAuthJson) {

                        }

                        @Override
                        public void onTokenIdReceived(GoogleAuthTokenJson googleAuthTokenJson) {
                            System.out.println("Token received: " + googleAuthTokenJson);
                            try (PrintWriter p = new PrintWriter("token.txt")) {
                                p.write(googleAuthTokenJson.getRefreshToken());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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

    public PokemonGo getApi() {
        return api;
    }

    public void setApi(PokemonGo api) {
        this.api = api;
    }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }

    public boolean isWalking() {
        return walking.get();
    }

    AtomicBoolean getWalking() {
        return walking;
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
