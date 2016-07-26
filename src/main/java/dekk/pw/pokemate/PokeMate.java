package dekk.pw.pokemate;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.tasks.TaskController;
import javafx.application.Application;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Created by TimD on 7/21/2016.
 */
public class PokeMate {
    public static final Path CONFIG_PROPERTIES = Paths.get("config.properties");
    private static Context context;
    public static long startTime;

    public PokeMate() throws IOException, LoginFailedException, RemoteServerException {
        if (!Files.exists(CONFIG_PROPERTIES)) {
            System.out.println("You are required to use a config.properties file to run the application.");
            System.exit(1);
        }
        PokeMateUI.setPoke(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        OkHttpClient http = builder.build();
        CredentialProvider auth = null;
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
