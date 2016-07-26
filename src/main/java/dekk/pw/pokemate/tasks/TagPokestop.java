package dekk.pw.pokemate.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.geometry.S2LatLng;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop implements Task {
    public void run(final Context context) {
        try {
            MapObjects map = context.getApi().getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() > 0) {
                List<Pokestop> optional = pokestops.stream().filter(Pokestop::canLoot).sorted((a, b) -> {
                    S2LatLng locationA = S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude());
                    S2LatLng locationB = S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude());
                    S2LatLng self = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
                    Double distanceA = self.getEarthDistance(locationA);
                    Double distanceB = self.getEarthDistance(locationB);
                    return distanceA.compareTo(distanceB);
                }).filter(p -> p.canLoot()).collect(Collectors.toList());
                if (!optional.isEmpty()) {
                	for (int i=0;i<optional.size();i++)
                	{
	                    Pokestop near = optional.get(i);
	                    Walking.setLocation(context);
	                    PokestopLootResult result = near.loot();
	                    if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.SUCCESS)) {
	                        System.out.println("Tagged pokestop [+" + result.getExperience() + "xp]");
	                    } else if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.INVENTORY_FULL)) {
	                        System.out.println("Tagged pokestop, but bag is full [+" + result.getExperience() + "xp]");
	                    } else if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.OUT_OF_RANGE)) {
	                        System.out.println("[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS OUT OF RANGE");
	                    } else if (result.getResult().equals(FortSearchResponseOuterClass.FortSearchResponse.Result.IN_COOLDOWN_PERIOD)) {
	                        System.out.println("[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS IN COOLDOWN");
	                    } else {
	                        System.out.println("Failed pokestop");
	                    }
                	}
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
