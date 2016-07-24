package dekk.pw.pokemate;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PtcLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.tasks.TaskController;
import javafx.application.Application;
import okhttp3.OkHttpClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by TimD on 7/21/2016.
 */
public class PokeMate {

    private static Context context;
    private static TaskController taskControllor;

    public PokeMate() throws IOException, LoginFailedException, RemoteServerException {
        PokeMateUI.setPoke(this);
        new Thread(() -> Application.launch(PokeMateUI.class, null)).start();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        OkHttpClient http = builder.build();
        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth;
        if (Config.getUsername().contains("@")) {
            auth = new GoogleLogin(http).login(Config.getUsername(), Config.getPassword());
            if (auth.hasToken()) {
                new PrintWriter("token.txt").println(auth.getToken().getContents());
            }
        } else {
            auth = new PtcLogin(http).login(Config.getUsername(), Config.getPassword());
        }
        System.out.println("Logged in as " + Config.getUsername());
        PokemonGo go = new PokemonGo(auth, http);
        context = new Context(go, go.getPlayerProfile(), false, auth, http);
        context.getLat().set(Double.parseDouble(Config.getProperties().getProperty("latitude")));
        context.getLng().set(Double.parseDouble(Config.getProperties().getProperty("longitude")));
        go.setLocation(context.getLat().get(), context.getLng().get(), 0);
        taskControllor = new TaskController(context);
        taskControllor.start();
    }

    public static void main(String[] args) throws RemoteServerException, IOException, LoginFailedException {
        new PokeMate();
    }

    public static Context getContext() {
        return context;
    }
}
