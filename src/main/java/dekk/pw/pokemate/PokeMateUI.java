package dekk.pw.pokemate;

import com.google.maps.model.DirectionsStep;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.DirectionsRenderer;
import com.lynden.gmapsfx.shapes.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.inventory.Item;
import dekk.pw.pokemate.tasks.Navigate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEvent;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class PokeMateUI extends Application implements MapComponentInitializedListener {

    public static final int UPDATE_TIME = 5000;
    boolean directions;
    protected static GoogleMapView mapComponent;
    protected GoogleMap map;
    protected static PokeMate poke;
    public static final double XVARIANCE = Config.getRange() * 1.5;
    public static final double VARIANCE = Config.getRange();
    public static Marker marker;
    int[] requiredXp = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    //public static void main(String[] args) {
    //    launch(args);
    //}
    public static void setPoke(PokeMate p) {
        poke = p;
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
        stage.setHeight(660);
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
        //Marker of current player, thread to update a 'hack refresh'
        marker = new Marker(new MarkerOptions().position(center).title("Player").icon("icons/trainer.gif"));
        map.addMarker(marker);
        final InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h3>Loading...</h3>")
                .position(center);

        InfoWindow window = new InfoWindow(infoOptions);
        window.open(map, marker);
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    List<DirectionsStep[]> directionsSteps = Navigate.getDirections();
                    if (directionsSteps != null && directionsSteps.size() > Config.getMapPoints() - 1 && !directions) {
                        synchronized (poke) {
                            List<LatLong> locs = new ArrayList<>();
                            for (DirectionsStep[] steps : Navigate.getDirections()) {
                                for (DirectionsStep step : steps) {
                                    step.polyline.decodePath().forEach(a -> locs.add(new LatLong(a.lat, a.lng)));
                                }
                            }
                            LatLong[] array = locs.toArray(new LatLong[0]);
                            MVCArray mvc = new MVCArray(array);

                            PolylineOptions polyOpts = new PolylineOptions()
                                    .path(mvc)
                                    .strokeColor("red")
                                    .strokeWeight(2)
                                    .strokeOpacity(0.8);

                            Polyline poly = new Polyline(polyOpts);
                            map.addMapShape(poly);
                            directions = true;
                        }
                    }
                    marker.setPosition(new LatLong(context.getLat().get(), context.getLng().get()));
                    int currentZoom = map.getZoom();
                    map.setZoom(currentZoom - 1);
                    map.setZoom(currentZoom);
                });
                try {
                    Platform.runLater(() -> {
                        PlayerProfile player = context.getApi().getPlayerProfile();
						long runTime = System.currentTimeMillis() - PokeMate.startTime;
                        double nextXP = requiredXp[player.getStats().getLevel()] - requiredXp[player.getStats().getLevel() - 1];
                        double curLevelXP = player.getStats().getExperience() - requiredXp[player.getStats().getLevel() - 1];
                        String ratio = new DecimalFormat("#0.00").format(curLevelXP / nextXP * 100.D);
						window.setContent("<h4>" + player.getUsername() + "</h4><h5>Current Level: " + player.getStats().getLevel() +" - Progress: " + ratio + 
						"%</h5><h5>XP to next level: "  + new DecimalFormat("###,###,###").format( nextXP- curLevelXP) + 
						"</h5><h5>Runtime: " + millisToTimeString(runTime) + "</h5>");
                        //Update Pokemon table
                        context.getApi().getInventories().getPokebank().getPokemons().sort((a, b) -> b.getCp() - a.getCp());
                        String rows = "\"";
                        for (Pokemon pokemon : context.getApi().getInventories().getPokebank().getPokemons()) {
                            if (pokemon.getPokemonFamily() != null) {
                                rows += "<tr> <td><img src=\'icons/" + pokemon.getPokemonId().getNumber() + ".png\'></td> <td>" + pokemon.getCp() + "</td> <td>" + pokemon.getCandy() + "</td> <td>" + context.getIvRatio(pokemon) + "</td> </tr>";
                            }
                        }
                        rows += "\"";
                        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-body').innerHTML = " + rows);
                        String itemsList = "\"";
                        for (Item item : context.getApi().getInventories().getItemBag().getItems()) {
                            if (item.getCount() > 0) {
                                String imgSrc = "icons/items/" + item.getItemId().getNumber() + ".png";
                                itemsList += "<tr><td><img style=\'width: 70px; height: 70px; \' " +
                                        "src=\'" + imgSrc + "\'" + "></td><td>" + item.getCount() + "</td></tr>";
                            }
                        }
                        itemsList += "\"";
                        mapComponent.getWebview().getEngine().executeScript("document.getElementById('info-items').innerHTML = " + itemsList);
                    });
                    Thread.sleep(UPDATE_TIME);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }).start();
    }

    public static void toast(String message) {
        Platform.runLater(() ->
                mapComponent.getWebview().getEngine().executeScript(
                        "$.notify(\"" + message + "\", {\n\tanimate: {\n\t\tenter: \'animated bounceInDown\',\n\t\texit: \'animated bounceOutUp\'\n\t}\n});"));
    }

	private static String millisToTimeString(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
