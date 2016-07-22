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
public class MoveLocation implements Task {

    public void run(Context context) {
        if (!context.isWalking()) {
            try {
                if (context.getApi().getMap().getCatchablePokemon().size() == 0 &&
                        context.getMapObjects().getPokestops().stream().filter(Pokestop::canLoot).count() == 0) {
                    System.out.println("No pokemon or usable pokestops remain in this area. Moving.");
                    MapObjects map = context.getMapObjects();
                    ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
                    if (pokestops.size() > 0) {
                        Collections.sort(pokestops, (a, b) -> {
                            S2LatLng locationA = S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude());
                            S2LatLng locationB = S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude());
                            S2LatLng self = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
                            Double distanceA = self.getEarthDistance(locationA);
                            Double distanceB = self.getEarthDistance(locationB);
                            return distanceA.compareTo(distanceB);
                        });
                        Optional<Pokestop> optional = pokestops.stream().findFirst();
                        if (optional != null && optional.isPresent()) {
                            Pokestop near = optional.get();
                            System.out.println("Attempting to walk to pokestop");
                            Walking.walk(S2LatLng.fromDegrees(near.getLatitude(), near.getLongitude()), context);
                        }
                    } else {
                        Walking.setLocation(true, context);
                    }
                }
            } catch (LoginFailedException | RemoteServerException e) {
                e.printStackTrace();
            }
        }
    }

}
