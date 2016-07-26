package dekk.pw.pokemate;

import com.google.common.geometry.S2LatLng;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.maps.model.DirectionsStep;
import com.pokegoapi.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by TimD on 7/21/2016.
 */
public class Walking {

    public static double getSmallRandom() {
        return Math.random() * 0.0001 - 0.00005;
    }

    public static void setLocation(Context context) {
        setLocation(false, context);
    }

    public static void setLocation(boolean random, Context context) {
        if (random) {
            context.getApi().setLocation(context.getLat().get() + getSmallRandom(), context.getLng().get() + getSmallRandom(), 0);
        } else {
            context.getApi().setLocation(context.getLat().get(), context.getLng().get(), 0);
        }
    }

    public static void walk(S2LatLng end, final Context context) {
        if (context.isWalking())
            return;
        context.getWalking().set(true);
        S2LatLng start = S2LatLng.fromDegrees(context.getLat().get(), context.getLng().get());
        S2LatLng diff = end.sub(start);
        double distance = start.getEarthDistance(end);
        long timeout = 200L;
        double timeRequired = distance / Config.getSpeed();
        final AtomicDouble stepsRequired = new AtomicDouble(timeRequired / (timeout / 1000D));
        double deltaLat = diff.latDegrees() / stepsRequired.get();
        double deltaLng = diff.lngDegrees() / stepsRequired.get();
        //Schedule a timer to walk every 200 ms
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                context.getLat().addAndGet(deltaLat);
                context.getLng().addAndGet(deltaLng);
                stepsRequired.getAndAdd(-1);
                if (stepsRequired.get() <= 0) {
                    System.out.println("Destination reached.");
                    context.getWalking().set(false);
                    cancel();
                }
                //   System.out.println(context.getLat().get() + " " + context.getLng().get() + " " + stepsRequired);
            }
        }, 0, timeout);
    }

    public static void walk(Context context, DirectionsStep[] steps) {
        new Thread(() -> {
            context.getWalking().set(true);
            if (steps != null) {
                for (DirectionsStep step : steps) {
                    //Log.i("WALKER", "Heading to: [" + step.endLocation.lat + ", " + step.endLocation.lng + "]");
                    context.getApi().setLocation(step.startLocation.lat, step.startLocation.lng, 0);
                    context.getLat().set(step.startLocation.lat);
                    context.getLng().set(step.startLocation.lng);
                    S2LatLng start = S2LatLng.fromDegrees(step.startLocation.lat, step.startLocation.lng);
                    S2LatLng end = S2LatLng.fromDegrees(step.endLocation.lat, step.endLocation.lng);
                    S2LatLng diff = end.sub(start);
                    double distance = step.distance.inMeters;
                    distance = start.getEarthDistance(end);
                    long timeout = 350L;
                    double timeRequired = distance / Config.getSpeed();
                    int stepsRequired = (int) (timeRequired / (new Long(timeout).doubleValue() / 1000));
                    double deltaLat = diff.latDegrees() / stepsRequired;
                    double deltaLng = diff.lngDegrees() / stepsRequired;
                    int remainingSteps = stepsRequired;
                    while (remainingSteps >= 0) {
                        context.getLat().addAndGet(deltaLat);
                        context.getLng().addAndGet(deltaLng);
                        setLocation(context);
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                         //  Log.i("WALKER", "Set location: [" + context.getLat().get() + ", " + context.getLng().get() + "]");
                        remainingSteps--;
                    }
                    //Log.i("WALKER", "Arrived at: [" + step.endLocation.lat + ", " + step.endLocation.lng + "]");
                }
            }else{
                System.out.println("WALKING ERROR");
            }
            context.getWalking().set(false);
        }).start();
    }
}
