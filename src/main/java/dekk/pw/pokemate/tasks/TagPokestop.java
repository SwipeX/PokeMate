package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.*;
/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop extends Task {

    TagPokestop(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            MapObjects map = context.getApi().getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() == 0) {
                return;
            }

            pokestops.stream()
                    .filter(Pokestop::canLoot)
                    .forEach(near -> {
                        Walking.setLocation(context);
                        try {
                            String result = resultMessage(near.loot());
                            PokeMateUI.toast(result, Config.POKE + "Stop interaction!", "icons/pokestop.png");
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
                String retstr = "Tagged pokestop [+" + result.getExperience() + "xp]";
                Map<Integer, Integer> receivedItems = new HashMap<Integer, Integer>();

                //check what items we got from the pokestop
                for (ItemAwardOuterClass.ItemAward item : result.getItemsAwarded()) {
                    receivedItems.put(item.getItemId().getNumber(), receivedItems.getOrDefault((item.getItemId().getNumber()),0) + 1);
                }
                //build the rest of the string
                for (Map.Entry<Integer, Integer> item : receivedItems.entrySet()) {
                    retstr += " - " + StringConverter.convertItem(ItemId.valueOf(item.getKey()).name()) + "(x" + item.getValue() + ")";
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

}
