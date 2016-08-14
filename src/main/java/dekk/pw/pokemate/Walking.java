package dekk.pw.pokemate;

import com.google.common.geometry.S2LatLng;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.LatLng;
import dekk.pw.pokemate.tasks.Navigate;
import dekk.pw.pokemate.tasks.TagPokestop;
import dekk.pw.pokemate.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by TimD on 7/21/2016.
 */
public class Walking {

    private static final Logger logger = LogManager.getLogger(Walking.class);
    private static final double VARIANCE = Config.getRange();
    private static double getSmallRandom() {
        return Math.random() * 0.0001 - 0.00005;
    }

    public static void setLocation(Context context) {
        setLocation(false, context);
    }

    private static void setLocation(boolean random, Context context) {
        if (random) {
            context.getApi().setLocation(context.getLat().get() + getSmallRandom(), context.getLng().get() + getSmallRandom(), new Random().nextInt(10));
        } else {
            context.getApi().setLocation(context.getLat().get(), context.getLng().get(), new Random().nextInt(10));
        }
    }

    public static void walk( final Context context, S2LatLng end) { // PokeStop Walking
        context.getWalking().set(true);
        S2LatLng start = S2LatLng.fromDegrees(context.getLat().get(), context.getLng().get());
        S2LatLng diff = end.sub(start);
        double distance = start.getEarthDistance(end);
        long timeout = 200L;
        double timeRequired = distance / Config.getSpeed();
        final AtomicDouble stepsRequired = new AtomicDouble(timeRequired / (timeout / 1000D));
        double deltaLat = diff.latDegrees() / stepsRequired.get();
        double deltaLng = diff.lngDegrees() / stepsRequired.get();
        logger.debug("starting to walk");
        //Schedule a timer to walk every 200 ms


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                context.getApi().setLocation(context.getLat().addAndGet(deltaLat), context.getLng().addAndGet(deltaLng), new Random().nextInt(10));
                stepsRequired.getAndAdd(-1);
                if (stepsRequired.get() <= 0) {
                    logger.debug("Setting a new destination");
                    context.getWalking().set(false);
                    context.addTask(new TagPokestop(context));
                    context.addTask(new Navigate(context, new LatLng(context.getLat().get() - VARIANCE, context.getLng().get() - VARIANCE),
                        new LatLng(context.getLat().get() + VARIANCE, context.getLng().get() + VARIANCE)));
                    cancel();
                }
            }
        }, 0, timeout);
    }

    public static void walk(Context context, DirectionsStep[] steps) {  // Streets Walking
        context.getWalking().set(true);
        if (steps != null) {
            for (DirectionsStep step : steps) {
                logger.debug("WALKER Heading to: [" + step.endLocation.lat + ", " + step.endLocation.lng + "]");
                context.getApi().setLocation(step.startLocation.lat, step.startLocation.lng, 0);
                context.getLat().set(step.startLocation.lat);
                context.getLng().set(step.startLocation.lng);
                S2LatLng start = S2LatLng.fromDegrees(step.startLocation.lat, step.startLocation.lng);
                S2LatLng end = S2LatLng.fromDegrees(step.endLocation.lat, step.endLocation.lng);
                S2LatLng diff = end.sub(start);
                double distance;
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
                     logger.debug( "Set location: [" + context.getLat().get() + ", " + context.getLng().get() + "]");
                    remainingSteps--;
                }
                logger.debug( "Arrived at: [" + step.endLocation.lat + ", " + step.endLocation.lng + "]");
            }
        }else{
            logger.error("WALKING ERROR");
        }
        context.getWalking().set(false);

    }
}
