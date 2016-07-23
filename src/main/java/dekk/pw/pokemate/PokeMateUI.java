package dekk.pw.pokemate;

import com.google.maps.model.LatLng;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MainApp;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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
    protected GoogleMapView mapComponent;
    protected GoogleMap map;
    protected DirectionsPane directions;
    protected PokeMate poke;
    public static final double XVARIANCE = .006;
    public static final double VARIANCE = .004;
    public static Marker marker;
    public static List<Pokestop> pokestops = new ArrayList<>();
    int[] requiredXp = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("Pokemate UI");
        poke = new PokeMate();
        poke.main(null);
        mapComponent = new GoogleMapView();
        mapComponent.addMapInializedListener(this);
        mapComponent.getWebview().getEngine().setOnAlert((WebEvent<String> event) -> {
        });
        BorderPane bp = new BorderPane();
        bp.setCenter(mapComponent);
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void mapInitialized() {
        Context context = poke.getContext();
        LatLong center = new LatLong(poke.getContext().getLat().get(), poke.getContext().getLng().get());
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
        marker = new Marker(new MarkerOptions().position(center).title("Player"));
        map.addMarker(marker);
        final InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h3>Loading...</h3>")
                .position(center);

        InfoWindow window = new InfoWindow(infoOptions);
        window.open(map, marker);
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    marker.setPosition(new LatLong(context.getLat().get(), context.getLng().get()));
                    int currentZoom = map.getZoom();
                    map.setZoom(currentZoom - 1);
                    map.setZoom(currentZoom);
                });
                try {
                    try {
                        Platform.runLater(() -> {
                            PlayerProfile player = context.getApi().getPlayerProfile();
                            double nextXP = requiredXp[player.getStats().getLevel()] - requiredXp[player.getStats().getLevel() - 1];
                            double curLevelXP = player.getStats().getExperience() - requiredXp[player.getStats().getLevel() - 1];
                            String ratio = new DecimalFormat("#0.00").format(curLevelXP / nextXP * 100.D);
                            window.setContent("<h3>" + player.getUsername() + " (" + player.getStats().getLevel() + ") : " +
                                    ratio + "% " + player.getStats().getExperience() + " total exp </h3>");
                        });
                        for (Pokestop ps : context.getApi().getMap().getMapObjects().getPokestops()) {
                            if (!pokestops.contains(ps)) {
                                Platform.runLater(() -> map.addMarker(new Marker(new MarkerOptions().position(center).title("Pokestop: " + ps.getId()))));
                                pokestops.add(ps);
                            }
                        }
                    } catch (LoginFailedException | RemoteServerException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(UPDATE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
