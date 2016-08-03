package dekk.pw.pokemate;

import POGOProtos.Map.Fort.FortDataOuterClass.FortData;
import com.google.maps.model.DirectionsStep;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.tasks.Navigate;
import dekk.pw.pokemate.util.Time;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEvent;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.controlsfx.control.Notifications;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class PokeMateUI extends Application implements MapComponentInitializedListener {

    private static final int UPDATE_TIME = 1000;
    private static final double XVARIANCE = Config.getRange() * 1.5;
    private static final double VARIANCE = Config.getRange();
    private static final String NOTIFY = "$.notify({\n" +
            "icon: '%s',\n" +
            "message: '%s',\n" +
            "},{\n" +
            "icon_type: 'img'," +
            "type: \"info\",\n" +
            "animate: {\n" +
            "enter: 'animated bounceInDown',\n" +
            "exit: 'animated bounceOutUp'\n" +
            "},\n" +
            "});";
    private static Marker marker;
    private static GoogleMapView mapComponent;
    private static PokeMate poke;
    private static String messagesForLog = "";
    private static int experienceGained = 0;
    private static long lastExperience = 0;
    private GoogleMap map;
    private boolean directions;
    private final int[] requiredXp = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    //public static void main(String[] args) {
    //    launch(args);
    //}
    static void setPoke(PokeMate p) {
        poke = p;
    }

    public static void toast(String message, String title, String image) {

        if (Config.isConsoleNotification())
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + message);
        messagesForLog += "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + message + "\\r\\n\\r\\n";

        if (Config.isShowUI() && Config.isUserInterfaceNotification()) Platform.runLater(() -> mapComponent.getWebview().getEngine().executeScript(String.format(NOTIFY, image, message)));
        if (Config.isShowUI() && Config.isUiSystemNotification()) Platform.runLater(() -> Notifications.create()
                .graphic(new ImageView(new Image(image, 64, 64, false, false)))
                .title(title)
                .text(message)
                .darkStyle()
                .show());
    }

    public static void addMessageToLog(String message) {
        messagesForLog += "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + message + "\\r\\n\\r\\n";
    }

    private static String millisToTimeString(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("Pokemate UI");
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        //This needs to be set to the resources directory, however, it is not play along nicely.
        mapComponent = new GoogleMapView("/map.html");
        mapComponent.addMapInializedListener(this);
        mapComponent.getWebview().getEngine().setOnAlert((WebEvent<String> event) -> {
        });
        BorderPane bp = new BorderPane();
        bp.setCenter(mapComponent);
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.setWidth(1100);
        stage.setHeight(674);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        stage.getIcons().add(new Image(classloader.getResourceAsStream("icon.png")));
        stage.show();
    }

    @Override
    public void mapInitialized() {
        Context context = PokeMate.getContext();
        LatLong center = new LatLong(PokeMate.getContext().getLat().get(), PokeMate.getContext().getLng().get());
        MapOptions options = new MapOptions();
        options.center(center)
                .mapMarker(true)
                .zoom(16)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.ROADMAP);
        map = mapComponent.createMap(options);


        //Highlight area we can walk in
        LatLong min = new LatLong(context.getLat().get() - VARIANCE, context.getLng().get() - XVARIANCE);
        LatLong miny = new LatLong(context.getLat().get() - VARIANCE, context.getLng().get() + XVARIANCE);
        LatLong minx = new LatLong(context.getLat().get() + VARIANCE, context.getLng().get() - XVARIANCE);
        LatLong max = new LatLong(context.getLat().get() + VARIANCE, context.getLng().get() + XVARIANCE);

        LatLong[] pAry = new LatLong[]{min, minx, max, miny};
        MVCArray pmvc = new MVCArray(pAry);

        PolygonOptions polygOpts = new PolygonOptions()
                .paths(pmvc)
                .strokeColor("blue")
                .strokeWeight(2)
                .editable(false)
                .clickable(false)
                .fillColor("lightBlue")
                .fillOpacity(0.3);

        Polygon pg = new Polygon(polygOpts);
        map.addMapShape(pg);
        Time.sleep(5000);
        try {
            for(FortData gym : context.getMap().getMapObjects().getGyms()) {

                LatLong position = new LatLong(gym.getLatitude(), gym.getLongitude());
                Marker gymMap = new Marker(new MarkerOptions().position(position).title(gym.getId()).icon("icons/gym.png"));
                map.addMarker(gymMap);
            }

            for(Pokestop pokestop : context.getMap().getMapObjects().getPokestops()) {

                LatLong position = new LatLong(pokestop.getLatitude(), pokestop.getLongitude());
                Marker pokestopMap = new Marker(new MarkerOptions().position(position).title(pokestop.getId()).icon("icons/pokestop_small.png"));
                map.addMarker(pokestopMap);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        //Marker of current player, thread to update a 'hack refresh'
        marker = new Marker(new MarkerOptions().position(center).title("Player").icon("icons/trainer.gif"));
        map.addMarker(marker);
        final InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h3>Loading...</h3>")
                .position(center);

        InfoWindow window = new InfoWindow(infoOptions);
        map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> window.open(map, marker));
        window.open(map, marker);
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    List<DirectionsStep[]> directionsSteps = Navigate.getDirections();
                    if (Navigate.getNavigationType() == Navigate.NavigationType.STREETS &&directionsSteps != null && Navigate.populated && !directions) {
                        synchronized (poke) {
                            List<LatLong> locs = new ArrayList<>();
                            for (DirectionsStep[] steps : directionsSteps) {
                                for (DirectionsStep step : steps) {
                                    step.polyline.decodePath().forEach(a -> locs.add(new LatLong(a.lat, a.lng)));
                                }
                            }
                            LatLong[] array = locs.toArray(new LatLong[0]);
                            MVCArray mvc = new MVCArray(array);

                            PolylineOptions polyOpts = new PolylineOptions()
                                    .path(mvc)
                                    .strokeColor("red")
                                    .strokeWeight(1)
                                    .strokeOpacity(0.8);

                            Polyline poly = new Polyline(polyOpts);
                            map.addMapShape(poly);
                            directions = true;
                        }
                    } else if (Navigate.getNavigationType() == Navigate.NavigationType.POKESTOPS && Navigate.populated && !directions) {
                        List<LatLong> locs = new ArrayList<>();
                        Navigate.getRoute().forEach(a -> locs.add(new LatLong(a.latDegrees(), a.lngDegrees())));

                        LatLong[] array = locs.toArray(new LatLong[0]);

                        MVCArray mvc = new MVCArray(array);

                        PolylineOptions polyOpts = new PolylineOptions()
                                .path(mvc)
                                .strokeColor("red")
                                .strokeWeight(1)
                                .strokeOpacity(0.8);

                        Polyline poly = new Polyline(polyOpts);
                        map.addMapShape(poly);
                        directions = true;
                    }
                    marker.setPosition(new LatLong(context.getLat().get(), context.getLng().get()));
                    int currentZoom = map.getZoom();
                    map.setZoom(currentZoom - 1);
                    map.setZoom(currentZoom);
                });

                //Update Thread
                try {
                    Platform.runLater(() -> {
                        updatePlayer(context, window);
                        updatePokemon(context);
                        updateItems(context);
                        updateLog();
                        updateIncubators(context);
                        updateEggs(context);

                    });
                    Thread.sleep(UPDATE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateEggs(Context context) {
        String eggsList = "\"";
        try {
            for (EggPokemon egg : context.getInventories().getHatchery().getEggs()) {
                String imgSrc = "icons/items/egg.png";
                String walked = new DecimalFormat("#0.#").format(egg.getEggKmWalked());
                String percent = new DecimalFormat("#0.#").format(((egg.getEggKmWalked() * 100) / (egg.getEggKmWalkedTarget() * 100)) * 100);
                String percentClass;
                double percentTmp = Double.valueOf(percent.replace(",", "."));

                if (percentTmp >= 66) {
                    percentClass = " progress-bar-success";
                } else if (percentTmp >= 33) {
                    percentClass = " progress-bar-warning";
                } else {
                    percentClass = " progress-bar-danger";
                }

                eggsList += "<tr><td style='width:72px;'><img style=\'width: 70px; height: 70px;\' " +
                        "src=\'" + imgSrc + "\'" + "></td>" +
                        "<td>Incubated : " + (egg.isIncubate() ? "<b style='color:#00ff00;'>yes</b>" : "<b style='color:#ff0000;'>no</b>") + "<br/>State : " + walked + "/" + egg.getEggKmWalkedTarget() + "km<br/>" +
                        "<div class='progress'><div class='progress-bar active progress-bar-striped" + percentClass + "' role='progressbar' aria-valuenow='" + percent + "' aria-valuemin='0' aria-valuemax='100' style='min-width: 2em; width: " + percent.replace(",", ".") + "%;'>" + percent + "%</div></div></td></tr>";
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
        eggsList += "\"";
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-eggs').innerHTML = " + eggsList);
    }

    private void updateIncubators(Context context) {
        String incubatorsList = "\"";
        try {
            for (EggIncubator incubator : context.getInventories().getIncubators()) {
                String imgSrc = "icons/items/" + (incubator.getUsesRemaining() > 0 ? "902" : "901") + ".png";
                String walked = new DecimalFormat("#0.#").format(incubator.getKmCurrentlyWalked());
                incubatorsList += "<tr><td style='width:72px;'><img style=\'width: 70px; height: 70px;\' " +
                        "src=\'" + imgSrc + "\'" + "></td>" +
                        "<td style='width: 200px;'>Currently: " + (incubator.isInUse() ? "<b style='color:#ff0000;'>In use</b>" : "<b style='color:#00ff00;'>unused</b>") +
                        "<br/>Remaining use : " + (incubator.getUsesRemaining() > 0 ? incubator.getUsesRemaining() : "\u221e") +
                        "<br/>Km walked : " + walked + "</td></tr>";
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
        incubatorsList += "\"";
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-incubators').innerHTML = " + incubatorsList);
    }

    private void updatePlayer(Context context, InfoWindow window) {
        PlayerProfile player = context.getProfile();
        long runTime = System.currentTimeMillis() - PokeMate.startTime;
        try {
            double nextXP = requiredXp[player.getStats().getLevel()] - requiredXp[player.getStats().getLevel() - 1];
            double curLevelXP = player.getStats().getExperience() - requiredXp[player.getStats().getLevel() - 1];
            long curTotalXP = player.getStats().getExperience();
            if (curTotalXP > lastExperience) {
                if (lastExperience != 0) {
                    experienceGained += curTotalXP - lastExperience;
                }
                lastExperience = curTotalXP;
            }

            String ratio = new DecimalFormat("#0.00").format(curLevelXP / nextXP * 100.D);
            window.setContent("<h4>" + player.getPlayerData().getUsername() + "</h4><h5>Current Level: " + player.getStats().getLevel() + " - Progress: " + ratio +
                "%</h5><h5>XP/Hour: " + new DecimalFormat("###,###,###").format((experienceGained / (runTime / 3.6E6))) + "</h5><h5>XP to next level: " + new DecimalFormat("###,###,###").format(nextXP - curLevelXP) +
                "</h5><h5>Runtime: " + millisToTimeString(runTime) + "</h5>");
        }  catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private void updateItems(Context context) {
        String itemsList = "\"";

            for (Item item : context.getInventories().getItemBag().getItems()) {
                if (item.getCount() > 0) {
                    String imgSrc = "icons/items/" + item.getItemId().getNumber() + ".png";
                    itemsList += "<tr><td><img style=\'width: 70px; height: 70px;\' " +
                            "src=\'" + imgSrc + "\'" + "></td><td>" + item.getCount() + "</td></tr>";
                }
            }

        itemsList += "\"";
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-items').innerHTML = " + itemsList);
    }

    private void updatePokemon(Context context) {
        //Update Pokemon table
        String pokeFilter = mapComponent.getWebview().getEngine().executeScript("$( \"#pokeFilter\" ).val();").toString();
        String pokeSortType = mapComponent.getWebview().getEngine().executeScript("$( \"#pokeSortType\" ).val();").toString();
        String pokeSort = pokeFilter + "-" + pokeSortType;
        try {
                switch (pokeSort) {
                    case "pokedex-des":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> b.getPokemonId().getNumber() - a.getPokemonId().getNumber());
                        break;
                    case "pokedex-asc":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> a.getPokemonId().getNumber() - b.getPokemonId().getNumber());
                        break;
                    case "cp-des":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> b.getCp() - a.getCp());
                        break;
                    case "cp-asc":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> a.getCp() - b.getCp());
                        break;
                    case "recent-des":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> Long.compare(b.getCreationTimeMs(), a.getCreationTimeMs()));
                        break;
                    case "recent-asc":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> Long.compare(a.getCreationTimeMs(), b.getCreationTimeMs()));
                        break;
                    case "candy-des":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> {
                            try {
                                return b.getCandy() - a.getCandy();
                            } catch (LoginFailedException e) {
                                e.printStackTrace();
                                return 0;
                            } catch (RemoteServerException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        });
                        break;
                    case "candy-asc":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> {
                            try {
                                return a.getCandy() - b.getCandy();
                            } catch (LoginFailedException e) {
                                e.printStackTrace();
                                return 0;
                            } catch (RemoteServerException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        });
                        break;
                    case "iv-des":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> context.getIvRatio(b) - context.getIvRatio(a));
                        break;
                    case "iv-asc":
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> context.getIvRatio(a) - context.getIvRatio(b));
                        break;
                    default:
                        context.getInventories().getPokebank().getPokemons().sort((a, b) -> b.getCp() - a.getCp());
                        break;
                }
        String rows = "\"";
        for (Pokemon pokemon : context.getInventories().getPokebank().getPokemons()) {
            if (pokemon.getPokemonFamily() != null) {
                rows += "<tr> <td><img width=\'80\' height=\'80\' src=\'icons/" + pokemon.getPokemonId().getNumber() + ".png\'></td> <td>" + pokemon.getCp() + "</td> <td>" + pokemon.getCandy() + "</td> <td>" + context.getIvRatio(pokemon) + "</td> </tr>";
            }
        }

        rows += "\"";
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-body').innerHTML = " + rows);

        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private void updateLog() {
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('logTextArea').value = document.getElementById('logTextArea').value + \"" + messagesForLog + "\"");
        mapComponent.getWebview().getEngine().executeScript("document.getElementById('logTextArea').scrollTop = document.getElementById('logTextArea').scrollHeight");
        messagesForLog = "";
    }
}
