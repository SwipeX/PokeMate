package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
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
            MapObjects map = context.getApi().getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() > 0) {
                Optional<Pokestop> optional = pokestops.stream().filter(Pokestop::canLoot).sorted((a, b) -> {
                    S2LatLng locationA = S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude());
                    S2LatLng locationB = S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude());
                    S2LatLng self = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
                    Double distanceA = self.getEarthDistance(locationA);
                    Double distanceB = self.getEarthDistance(locationB);
                    return distanceA.compareTo(distanceB);
                }).filter(p -> p.canLoot()).findFirst();
                if (optional.isPresent()) {
                    Pokestop near = optional.get();
                    Walking.setLocation(context);
                    PokestopLootResult result = near.loot();
                    if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.SUCCESS)) {
                        System.out.println("Tagged pokestop [+" + result.getExperience() + "xp]");
                    } else if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.INVENTORY_FULL)) {
                        System.out.println("Tagged pokestop, but bag is full [+" + result.getExperience() + "xp]");
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
