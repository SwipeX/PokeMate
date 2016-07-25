package dekk.pw.pokemate.tasks;

import com.google.common.geometry.S2LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

import java.util.*;

/**
 * Created by TimD on 7/21/2016.
 * Credit: https://github.com/mjmfighter/pokemon-go-bot/blob/master/src/main/java/com/mjmfighter/pogobot/LocationWalker.java
 */
public class Navigate implements Task {

    private final LatLng min, max;
    private static List<DirectionsStep[]> routes = new ArrayList<>();
    private int routesIndex = 0;

    public Navigate(Context context, LatLng min, LatLng max) {
        this.min = new LatLng(min.lat < max.lat ? min.lat : max.lat, min.lng < max.lng ? min.lng : max.lng);
        this.max = new LatLng(min.lat > max.lat ? min.lat : max.lat, min.lng > max.lng ? min.lng : max.lng);
        populateDirections(context);
    }

    @Override
    public void run(Context context) {
        if (context.isWalking()) {
            return;
        } else if (routesIndex >= routes.size()) {
            routesIndex = 0;
        }
        Walking.walk(context, routes.get(routesIndex++));
    }


    private LatLng getNextLocation() {
        Random ran = new Random();
        double nextLat = min.lat + (max.lat - min.lat) * ran.nextDouble();
        double nextLng = min.lng + (max.lng - min.lng) * ran.nextDouble();
        return new LatLng(nextLat, nextLng);
    }

    private DirectionsStep[] queryDirections(Context context, LatLng start, LatLng end) {
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
            DirectionsStep[] steps = queryDirections(context, current, next);
            if (steps != null) {
                routes.add(steps);
                current = next;
                next = getNextLocation();
                i++;
            } else {
                next = getNextLocation();
            }
        }
        DirectionsStep[] steps = queryDirections(context, next, start);
        if (steps != null) {
            routes.add(steps);
        }
    }

    public synchronized static List<DirectionsStep[]> getDirections() {
        return routes;
    }
}
