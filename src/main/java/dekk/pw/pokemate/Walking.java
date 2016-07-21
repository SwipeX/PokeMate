package dekk.pw.pokemate;

import com.google.common.geometry.S2LatLng;
import dekk.pw.pokemate.tasks.MoveLocation;

/**
 * Created by TimD on 7/21/2016.
 */
public class Walking {

    public static double getSmallRandom() {
        return Math.random() * 0.0001 - 0.00005;
    }

    public static void walk(S2LatLng end, final Context context) {
        if (context.isWalking())
            return;
        context.setWalking(true);
        S2LatLng start = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
        S2LatLng diff = end.sub(start);
        double stepsRequired = 20;
        final double deltaLat = diff.latDegrees() / stepsRequired;
        final double deltaLng = diff.lngDegrees() / stepsRequired;
        double x = start.latDegrees();
        double y = start.lngDegrees();
        for (int i = 0; i < stepsRequired + 1; i++) {
            x += deltaLat;
            y += deltaLng;
        }
        context.getApi().setLocation(x, y, 0);
        context.setWalking(false);
        MoveLocation.resetTime();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
