package dekk.pw.pokemate;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.tasks.TaskController;
import javafx.application.Application;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by TimD on 7/21/2016.
 */
public class PokeMate {

    private static Context context;

    public static long startTime;

    public PokeMate() throws IOException, LoginFailedException, RemoteServerException {
        PokeMateUI.setPoke(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        OkHttpClient http = builder.build();
        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = null;
        auth = Context.Login(http);
        System.out.println("Logged in as " + Config.getUsername());
        PokemonGo go = new PokemonGo(auth, http);
        context = new Context(go, go.getPlayerProfile(), false, auth, http);
        context.getLat().set(Double.parseDouble(Config.getProperties().getProperty("latitude")));
        context.getLng().set(Double.parseDouble(Config.getProperties().getProperty("longitude")));
        go.setLocation(context.getLat().get(), context.getLng().get(), 0);
        if (Config.isShowUI()) {
            new Thread(() -> Application.launch(PokeMateUI.class, null)).start();
        }
        TaskController controller = new TaskController(context);
        controller.start();
        startTime = System.currentTimeMillis();
    }

    public static void main(String[] args) throws RemoteServerException, IOException, LoginFailedException {
        new PokeMate();
    }

    public static Context getContext() {
        return context;
    }
}
