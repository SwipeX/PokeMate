package dekk.pw.pokemate;

import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.SystemTimeImpl;
import dekk.pw.pokemate.tasks.TaskController;
import dekk.pw.pokemate.tasks.Update;
import dekk.pw.pokemate.util.LatLongFromLocation;
import javafx.application.Application;
import okhttp3.OkHttpClient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Created by TimD on 7/21/2016.
 */
public class PokeMate {
    public static final Path CONFIG_PROPERTIES = Paths.get("config.properties");
    public static long startTime;
    private static Context context;

    private static final Logger logger = LogManager.getLogger(PokeMate.class);

    private double getSmallRandom() {
        return Math.random() * 0.0003 - 0.0003;
    }

    private PokeMate() throws LoginFailedException, RemoteServerException {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        OkHttpClient http = builder.build();
        CredentialProvider auth;

        AtomicDouble lat;
        AtomicDouble lng;
        // Co-ordinates by location name
        if (Config.isUseCustomNamedLocation()) {
            LatLongFromLocation fromLocation = new LatLongFromLocation(Config.getGoogleApiKey());
            String namedLocation = Config.getCustomNamedLocation();
            fromLocation.parseLocation(namedLocation);

            lat = fromLocation.getLatitude();
            lng = fromLocation.getLongitude();

            logger.info("Using Custom Location {} with lat/lon {}, {}", namedLocation, lat, lng);

        } else { // Use given co-ordindates instead
            AtomicDouble alat = new AtomicDouble();
            alat.set(Double.parseDouble(Config.getProperties().getProperty("latitude"))+getSmallRandom());
            lat = alat;

            AtomicDouble alng = new AtomicDouble();
            alng.set(Double.parseDouble(Config.getProperties().getProperty("longitude"))+getSmallRandom());
            lng = alng ;

            logger.info("Using Coordinates {}, {}", lat, lng);
        }

        auth = Context.Login(http);

        logger.info("Logged in as {}", Config.getUsername());

        //PokemonGo go = new PokemonGo(auth, http);
        PokemonGo go = new PokemonGo(auth, http, new SystemTimeImpl());

        context = new Context(go, go.getPlayerProfile(), false, auth, http);
        context.setLat(lat);
        context.setLng(lng);
        go.setLocation(context.getLat().get(), context.getLng().get(), 0);
        if (Config.isShowUI()) {
            PokeMateUI.setPoke(this);
            new Thread(() -> Application.launch(PokeMateUI.class, "")).start();
        }
        new Update(context).runOnce();
        TaskController controller = new TaskController(context);
        controller.start();
        startTime = System.currentTimeMillis();
    }

    public static void main(String[] args) throws RemoteServerException, IOException, LoginFailedException {
        File configProperties;
        if (args.length == 0) {
            configProperties = new File("config.properties");
            logger.info("Using default config.properties location");
        } else {
            configProperties = new File(args[0]);
            logger.info("Using configuration file: {}", configProperties.toPath());
        }

        if (!configProperties.exists()) {
            logger.error("ERROR - Could not find the required config.properties file: {}", configProperties.getAbsolutePath());
            System.exit(1);
        }

        Config.load(configProperties.getPath());
        new PokeMate();
    }

    public static Context getContext() {
        return context;
    }
}
