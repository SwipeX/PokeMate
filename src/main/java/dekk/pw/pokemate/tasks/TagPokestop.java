package dekk.pw.pokemate.tasks;

import com.google.common.geometry.S2LatLng;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

import java.util.*;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop implements Task {
    public void run(final Context context) {
        try {
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
                Pokestop nearest = pokestops.get(0);
                for (Pokestop pokestop : pokestops) {
                    if (pokestop.canLoot()) {
                        nearest = pokestop;
                        break;
                    }
                }
                if (nearest != null) {
                    System.out.println("Attempting to walk to pokestop");
                    Walking.walk(S2LatLng.fromDegrees(nearest.getLatitude(), nearest.getLongitude()), context);
                    PokestopLootResult result = nearest.loot();
                    if (result.wasSuccessful()) {
                        System.out.println("Tagged pokestop [+" + result.getExperience() + "xp]");
                    } else {
                        System.out.println("Failed pokestop");
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
