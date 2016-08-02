package dekk.pw.pokemate.tasks;

import com.google.common.geometry.S2LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;
import dekk.pw.pokemate.util.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * Created by TimD on 7/21/2016.
 * Credit: https://github.com/mjmfighter/pokemon-go-bot/blob/master/src/main/java/com/mjmfighter/pogobot/LocationWalker.java
 */
public class Navigate extends Task implements Runnable {

    private final LatLng min, max;
    public static final double VARIANCE = Config.getRange();
    private static final List<DirectionsStep[]> routes = new ArrayList<>();
    private static final List<S2LatLng> route = new ArrayList<>();
    public static boolean populated;
    private static final Object lock = new Object();
    static final NavigationType navigationType = Config.getNavigationType();


    public Navigate(final Context context, LatLng min, LatLng max) {
        super(context);
        this.min = new LatLng(min.lat < max.lat ? min.lat : max.lat, min.lng < max.lng ? min.lng : max.lng);
        this.max = new LatLng(min.lat > max.lat ? min.lat : max.lat, min.lng > max.lng ? min.lng : max.lng);
        switch (navigationType) {
            //Untested
            case POKESTOPS:
                populateRoute(context);
                break;
            default:
                populateDirections(context);
                break;
        }
        populated = true;
    }

    public static NavigationType getNavigationType() {
        return navigationType;
    }

    public static List<S2LatLng> getRoute() {
        return route;
    }

    private S2LatLng last;
    private final List<String> ids = new ArrayList<>();

    /**
     * Attempts to generate a route to all found pokestops...
     *
     * @param context
     */
    private void populateRoute(Context context) {
        try {
            Time.sleepRate();
            List<Pokestop> stops = context.getMap().getMapObjects().getPokestops().stream().filter(a ->
                //only pokestops in our region
                a.getLatitude() >= min.lat &&
                a.getLatitude() <= max.lat &&
                a.getLongitude() >= min.lng &&
                a.getLongitude() <= max.lng).collect(Collectors.toList());

            int count = stops.size();
            //System.out.println("Stops found: " + count);
            last = S2LatLng.fromDegrees(context.getLat().doubleValue(), context.getLng().doubleValue());
            while (route.size() < count - 1) {
                List<Pokestop> tempStops = stops.stream().filter(a -> !ids.contains(a.getId())).sorted((a, b) -> {
                    S2LatLng locationA = S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude());
                    S2LatLng locationB = S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude());
                    Double distanceA = last.getEarthDistance(locationA);
                    Double distanceB = last.getEarthDistance(locationB);
                    return distanceA.compareTo(distanceB);
                }).collect(Collectors.toList());

                if (tempStops.size() == 0) {
                    //System.out.println("CRTICAL POKESTOP LOCATION ERROR BREAKING!");
                    break;
                }
                Pokestop first = tempStops.get(0);
                route.add(last = S2LatLng.fromDegrees(first.getLatitude(), first.getLongitude()));
                ids.add(first.getId());
            }
            route.add(S2LatLng.fromDegrees(context.getLat().get(), context.getLng().get()));
        } catch (RemoteServerException e) {
            System.out.println("[Navigate] Error - Hit Rate limiter.");
            //e.printStackTrace();
        } catch (LoginFailedException e) {
            System.out.println("[Navigate] Login Failed.");
            //e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("[Navigate] Null Pointer Exception.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        if (context.isWalking()) {
            return;
        } else if (navigationType == (NavigationType.STREETS) && context.getRoutesIndex() >= getDirections().size()) {
            context.resetRoutesIndex();
        } else if (navigationType == (NavigationType.POKESTOPS) && context.getRoutesIndex() >= route.size()) {
            context.resetRoutesIndex();
        }
        switch (navigationType) {
            case POKESTOPS:
                context.increaseRoutesIndex();
                Walking.walk(context, route.get(context.getRoutesIndex()));
                context.setConsoleString("Navigate", String.format("[%s] - Navigating to waypoint %d of %d", new SimpleDateFormat("HH:mm:ss").format(new Date()), context.getRoutesIndex(), route.size()));
                break;
            case POKEMON:
                //TODO: walk dynamically to nearest pokemon
                break;
            default:
                context.increaseRoutesIndex();
                Walking.walk(context, getDirections().get(context.getRoutesIndex()));
        }
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

    public enum NavigationType {
        STREETS, POKESTOPS, POKEMON
    }
}
