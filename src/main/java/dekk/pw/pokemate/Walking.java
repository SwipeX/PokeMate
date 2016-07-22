package dekk.pw.pokemate;

import com.google.common.geometry.S2LatLng;
import com.google.common.util.concurrent.AtomicDouble;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

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
        double timeRequired = distance / context.getSpeed();
        final AtomicDouble stepsRequired = new AtomicDouble(timeRequired / (timeout / 1000D));
        double deltaLat = diff.latDegrees() / stepsRequired.get();
        double deltaLng = diff.lngDegrees() / stepsRequired.get();
        //Schedule a timer to walk every 100 ms
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
}
