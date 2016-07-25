package dekk.pw.pokemate;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PtcLogin;
import okhttp3.OkHttpClient;

import java.io.File;
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
    private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo;

    public Context(PokemonGo go, PlayerProfile profile, boolean walking, RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo, OkHttpClient http) {
        this.api = go;
        this.profile = profile;
        this.walking.set(walking);
        this.authInfo = authInfo;
        this.http = http;
    }

    public static RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo Login(OkHttpClient httpClient) {
        return Login(null, httpClient);
    }

    public static RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo Login(Context context, OkHttpClient httpClient) {
        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = context == null ? null : context.authInfo;
        String token = null;
        try {
            if (Config.getUsername().contains("@")) {
                auth = new GoogleLogin(httpClient).login(Config.getUsername(),Config.getPassword());
                //The below code is not functional, token needs verification.
//                if (auth == null) {
//                    File file = new File("token.txt");
//                    if (file.exists()) {
//                        Scanner scanner = new Scanner(file);
//                        token = scanner.nextLine();
//                    }
//                    if (token != null) {
//                        auth = new GoogleLogin(httpClient).login(token);
//                    } else {
//                        auth = new GoogleLogin(httpClient).login(Config.getUsername(), Config.getPassword());
//                    }
//                    try (PrintWriter p = new PrintWriter("token.txt")) {
//                        p.println(auth.getToken().getContents());
//                    }
//                } else {
//                    token = auth.getToken().getContents();
//                    auth = new GoogleLogin(httpClient).login(token);
//                    try (PrintWriter p = new PrintWriter("token.txt")) {
//                        p.println(token);
//                    }
//                }
            } else {
                auth = new PtcLogin(httpClient).login(Config.getUsername(), Config.getPassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auth;
    }

    public AtomicDouble getLat() {
        return lat;
    }

    public AtomicDouble getLng() {
        return lng;
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

    public RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    /**
     * @param pokemon the pokemon for which an IV ratio is desired.
     * @return an integer 0-100 on the individual value of the pokemon.
     */
    public int getIvRatio(Pokemon pokemon) {
        return (pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina()) * 100 / 45;
    }
}
