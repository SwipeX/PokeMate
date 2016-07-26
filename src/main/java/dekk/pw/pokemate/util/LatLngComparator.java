package dekk.pw.pokemate.util;

import com.google.common.geometry.S2LatLng;

import java.util.Comparator;

/**
 * @author Kyle Stevenson
 * @since 07/26/2016
 */
public class LatLngComparator implements Comparator<S2LatLng> {
    private final S2LatLng me;

    public LatLngComparator(final S2LatLng me) {
        this.me = me;
    }

    @Override
    public int compare(final S2LatLng locA, final S2LatLng locB) {
        return me.getDistance(locA).compareTo(me.getDistance(locB));
    }
}
