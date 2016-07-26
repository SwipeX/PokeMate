package dekk.pw.pokemate.tasks;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

import java.util.*;

/**
 * Created by TimD on 7/21/2016.
 * Credit: https://github.com/mjmfighter/pokemon-go-bot/blob/master/src/main/java/com/mjmfighter/pogobot/LocationWalker.java
 */
public class Navigate extends Task {

    private final LatLng min, max;
    private static List<DirectionsStep[]> routes = new ArrayList<>();
    private int routesIndex = 0;
    private static final Object lock = new Object();

    public Navigate(final Context context, LatLng min, LatLng max) {
        super(context);
        this.min = new LatLng(min.lat < max.lat ? min.lat : max.lat, min.lng < max.lng ? min.lng : max.lng);
        this.max = new LatLng(min.lat > max.lat ? min.lat : max.lat, min.lng > max.lng ? min.lng : max.lng);
        populateDirections(context);
    }

    @Override
    public void run() {
        if (context.isWalking()) {
            return;
        } else if (routesIndex >= getDirections().size()) {
            routesIndex = 0;
        }
        Walking.walk(context, getDirections().get(routesIndex++));
    }


    private LatLng getNextLocation() {
        Random ran = new Random();
        double nextLat = min.lat + (max.lat - min.lat) * ran.nextDouble();
        double nextLng = min.lng + (max.lng - min.lng) * ran.nextDouble();
        return new LatLng(nextLat, nextLng);
    }

    private DirectionsStep[] queryDirections(LatLng start, LatLng end) {
        DirectionsStep[] stepsToTake = null;
        GeoApiContext ctx = new GeoApiContext().setApiKey(Config.getGoogleApiKey());
        DirectionsApiRequest request = DirectionsApi.newRequest(ctx)
                .origin(start)
                .destination(end)
                .mode(TravelMode.WALKING);

        try {
            DirectionsResult result = request.await();
            if (result.routes.length > 0) {
                DirectionsRoute directionsRoute = result.routes[0];
                if (directionsRoute.legs.length > 0) {
                    DirectionsLeg legs = directionsRoute.legs[0];
                    if (legs.steps.length > 0) {
                        stepsToTake = legs.steps;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stepsToTake;
    }

    private void populateDirections(Context context) {
        int i = 0;
        LatLng start = new LatLng(context.getLat().get(), context.getLng().get());
        LatLng current = start;
        LatLng next = getNextLocation();
        while (i < Config.getMapPoints()) {
            DirectionsStep[] steps = queryDirections(current, next);
            if (steps != null) {
                getDirections().add(steps);
                current = next;
                next = getNextLocation();
                i++;
            } else {
                next = getNextLocation();
            }
        }
        DirectionsStep[] steps = queryDirections(next, start);
        if (steps != null) {
            getDirections().add(steps);
        }
    }

    public static List<DirectionsStep[]> getDirections() {
        synchronized (lock) {
            return routes;
        }
    }
}
