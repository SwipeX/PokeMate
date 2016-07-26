package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import com.google.common.geometry.S2LatLng;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.api.inventory.Item;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;
import dekk.pw.pokemate.util.LatLngComparator;

import java.util.ArrayList;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop implements Task {
    public void run(final Context context) {
        try {
            MapObjects map = context.getApi().getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() == 0) {
                return;
            }

            final S2LatLng self = S2LatLng.fromDegrees(context.getApi().getLatitude(), context.getApi().getLongitude());
            final LatLngComparator comparator = new LatLngComparator(self);

            pokestops.stream()
                    .filter(Pokestop::canLoot)
                    .sorted((stopA, stopB) -> comparator.compare(locate(stopA), locate(stopB)))
                    .findFirst()
                    .ifPresent(near -> {
                        Walking.setLocation(context);
                        try {
                            String result = resultMessage(near.loot());
                            PokeMateUI.toast(result);
                        } catch (LoginFailedException | RemoteServerException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private String resultMessage(final PokestopLootResult result) {
        switch (result.getResult()) {
            case SUCCESS:
                String retstr = "Tagged pokestop [+" + result.getExperience() + "xp]" + "\n\tItems Awarded: ";
				for (ItemAwardOuterClass.ItemAward item : result.getItemsAwarded()) {
					retstr += ("\n\t\t" + item.getItemId().name());
				}
				return retstr;
            case INVENTORY_FULL:
                return "Tagged pokestop, but bag is full [+" + result.getExperience() + "xp]";
            case OUT_OF_RANGE:
                return "[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS OUT OF RANGE";
            case IN_COOLDOWN_PERIOD:
                return "[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS IN COOLDOWN";
            default:
                return "Failed Pokestop";
        }
    }

    private S2LatLng locate(final Pokestop pokestop) {
        return S2LatLng.fromDegrees(pokestop.getLatitude(), pokestop.getLongitude());
    }
}
