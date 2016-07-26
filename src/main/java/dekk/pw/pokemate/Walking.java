package dekk.pw.pokemate;

import com.google.common.geometry.S2LatLng;
import com.google.maps.model.DirectionsStep;
import com.pokegoapi.util.Log;

/**
 * Created by TimD on 7/21/2016.
 */
public class Walking {

    private static double getSmallRandom() {
        return Math.random() * 0.0001 - 0.00005;
    }

    public static void setLocation(Context context) {
        setLocation(false, context);
    }

    private static void setLocation(boolean random, Context context) {
        if (random) {
            context.getApi().setLocation(context.getLat().get() + getSmallRandom(), context.getLng().get() + getSmallRandom(), 0);
        } else {
            context.getApi().setLocation(context.getLat().get(), context.getLng().get(), 0);
        }
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
