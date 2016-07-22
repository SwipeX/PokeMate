package dekk.pw.pokemate.tasks;

import com.google.common.geometry.S2LatLng;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by TimD on 7/21/2016.
 * This will move if there are no long any availible pokestops or pokemon,
 * either to the nearest pokestop or the nearest random cell.
 */
public class NavigatePokestop implements Task {
    @Override
    public void run(Context context) {
        try {
            int catchable = context.getApi().getMap().getCatchablePokemon().size();
            if (catchable > 0) return;
            if (context.isWalking()) return;
            ArrayList<Pokestop> pokestops = new ArrayList<>(context.getApi().getMap().getMapObjects().getPokestops());
            Optional<Pokestop> optional = pokestops.stream().sorted((a, b) -> {
                S2LatLng locationA = S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude());
                S2LatLng locationB = S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude());
                S2LatLng self = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
                Double distanceA = self.getEarthDistance(locationA);
                Double distanceB = self.getEarthDistance(locationB);
                return distanceA.compareTo(distanceB);
            }).filter(Pokestop::canLoot).findFirst();
            if (optional.isPresent()) {
                Pokestop nearestUnused = optional.get();
                Walking.walk(S2LatLng.fromDegrees(nearestUnused.getLatitude(), nearestUnused.getLongitude()), context);
                System.out.println("Navigating pokestop");
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }

    }
}
